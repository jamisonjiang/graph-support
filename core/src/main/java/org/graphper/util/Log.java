/*
 * Copyright 2022 The graph-support project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.graphper.util;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Internal logging facade, public only for use across core packages. Not a provider SPI.
 *
 * <p>SLF4J is resolved through this class's defining loader, never an unrelated context loader.
 * Missing or broken SLF4J falls back to stderr for warnings and errors only. A working backend,
 * including SLF4J's NOP logger, retains full control over filtering and formatting.
 *
 * <p>Core logging is disabled by default. Set {@code -Dgraphper.logging.enabled=true} to enable
 * logging, subject to backend levels. The property is checked on every call, so changes through
 * {@code System.setProperty} also affect existing loggers. Only {@code true} (case-insensitive)
 * enables logging; an absent, unrecognized, or inaccessible property keeps logging disabled.
 * This switch does not control third-party libraries or CLI-owned loggers.
 *
 * @internal
 */
public final class Log {

  private static final String[] LEVELS = {"debug", "info", "warn", "error"};
  private static final Backend BACKEND = Backend.load();

  private final String name;
  private final Class<?> type;
  private volatile Object logger;
  private volatile boolean initialized;

  private Log(Class<?> type) {
    name = type.getName();
    this.type = type;
    if (loggingEnabled()) {
      initialize();
    }
  }

  /** Returns an internal logger named after the supplied class. */
  public static Log getLogger(Class<?> type) {
    return new Log(type);
  }

  /** Queries the backend's current debug level; false without a backend. */
  public boolean isDebugEnabled() {
    return enabled(0);
  }

  /** Queries the warning level; true without a backend only when core logging is enabled. */
  public boolean isWarnEnabled() {
    return enabled(2);
  }

  /** Logs a debug message with literal {} placeholders and an optional trailing throwable. */
  public void debug(String message, Object... arguments) {
    write(0, message, arguments, null, false);
  }

  /** Logs a literal debug message and an explicitly supplied, possibly null throwable. */
  public void debug(String message, Throwable throwable) {
    write(0, message, null, throwable, true);
  }

  /** Logs an informational message with {} placeholders and an optional trailing throwable. */
  public void info(String message, Object... arguments) {
    write(1, message, arguments, null, false);
  }

  /** Logs a literal informational message and an explicitly supplied, possibly null throwable. */
  public void info(String message, Throwable throwable) {
    write(1, message, null, throwable, true);
  }

  /** Logs a warning with literal {} placeholders and an optional trailing throwable. */
  public void warn(String message, Object... arguments) {
    write(2, message, arguments, null, false);
  }

  /** Logs a literal warning and an explicitly supplied, possibly null throwable. */
  public void warn(String message, Throwable throwable) {
    write(2, message, null, throwable, true);
  }

  /** Logs an error with literal {} placeholders and an optional trailing throwable. */
  public void error(String message, Object... arguments) {
    write(3, message, arguments, null, false);
  }

  /** Logs a literal error and an explicitly supplied, possibly null throwable. */
  public void error(String message, Throwable throwable) {
    write(3, message, null, throwable, true);
  }

  private boolean enabled(int level) {
    if (!loggingEnabled()) {
      return false;
    }
    initialize();
    Object target = logger;
    if (target != null) {
      try {
        return (Boolean) BACKEND.enabled[level == 0 ? 0 : 1].invoke(target);
      } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
        propagateError(e);
        logger = null;
      }
    }
    return level >= 2;
  }

  private void write(int level, String message, Object[] arguments, Throwable throwable,
                      boolean explicitThrowable) {
    if (!loggingEnabled()) {
      return;
    }
    initialize();
    Object target = logger;
    if (target != null) {
      try {
        if (explicitThrowable) {
          BACKEND.throwable[level].invoke(target, message, throwable);
        } else {
          BACKEND.arguments[level].invoke(target, message, arguments);
        }
        return;
      } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
        propagateError(e);
        logger = null;
      }
    }
    if (level < 2) {
      return;
    }
    int count = arguments == null ? 0 : arguments.length;
    if (!explicitThrowable && count > 0 && arguments[count - 1] instanceof Throwable) {
      throwable = (Throwable) arguments[--count];
    }
    String text = format(message, arguments, count);
    PrintStream err = System.err;
    synchronized (err) {
      err.println("[" + LEVELS[level] + "] " + name + " - " + text);
      if (throwable != null) {
        throwable.printStackTrace(err);
      }
    }
  }

  private static boolean loggingEnabled() {
    try {
      return "true".equalsIgnoreCase(System.getProperty("graphper.logging.enabled"));
    } catch (SecurityException e) {
      return false;
    }
  }

  private void initialize() {
    if (initialized) {
      return;
    }
    synchronized (this) {
      if (initialized) {
        return;
      }
      if (BACKEND != null) {
        try {
          logger = BACKEND.factory.invoke(null, type);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
          propagateError(e);
        }
      }
      initialized = true;
    }
  }

  private static String format(String message, Object[] arguments, int count) {
    if (message == null || count == 0) {
      return message;
    }
    StringBuilder text = new StringBuilder();
    int start = 0;
    int argument = 0;
    while (argument < count) {
      int placeholder = message.indexOf("{}", start);
      if (placeholder < 0) {
        break;
      }
      if (placeholder > start && message.charAt(placeholder - 1) == '\\') {
        text.append(message, start, placeholder - 1);
        if (placeholder < start + 2 || message.charAt(placeholder - 2) != '\\') {
          text.append("{}");
          start = placeholder + 2;
          continue;
        }
      } else {
        text.append(message, start, placeholder);
      }
      try {
        text.append(String.valueOf(arguments[argument++]));
      } catch (RuntimeException e) {
        text.append("[FAILED toString()]");
      }
      start = placeholder + 2;
    }
    return text.append(message, start, message.length()).toString();
  }

  private static void propagateError(Throwable failure) {
    // Reflection wraps backend errors, including fatal VM errors which must never be swallowed.
    if (failure instanceof InvocationTargetException) {
      Throwable cause = failure.getCause();
      if (cause instanceof Error && !(cause instanceof LinkageError)) {
        throw (Error) cause;
      }
    }
  }

  private static final class Backend {

    private final Method factory;
    private final Method[] arguments = new Method[4];
    private final Method[] throwable = new Method[4];
    private final Method[] enabled = new Method[2];

    private Backend() throws ReflectiveOperationException {
      ClassLoader loader = Log.class.getClassLoader();
      Class<?> api = Class.forName("org.slf4j.Logger", false, loader);
      factory = Class.forName("org.slf4j.LoggerFactory", false, loader)
          .getMethod("getLogger", Class.class);
      for (int i = 0; i < LEVELS.length; i++) {
        // Use the public interface, not a potentially inaccessible backend implementation class.
        arguments[i] = api.getMethod(LEVELS[i], String.class, Object[].class);
        throwable[i] = api.getMethod(LEVELS[i], String.class, Throwable.class);
      }
      enabled[0] = api.getMethod("isDebugEnabled");
      enabled[1] = api.getMethod("isWarnEnabled");
    }

    private static Backend load() {
      try {
        return new Backend();
      } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
        propagateError(e);
        return null;
      }
    }
  }
}
