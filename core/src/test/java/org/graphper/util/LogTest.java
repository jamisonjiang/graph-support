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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

class LogTest {

  private String previousLogging;

  @BeforeEach
  void enableLogging() {
    previousLogging = System.getProperty("graphper.logging.enabled");
    System.setProperty("graphper.logging.enabled", "true");
  }

  @AfterEach
  void restoreLogging() {
    if (previousLogging == null) {
      System.clearProperty("graphper.logging.enabled");
    } else {
      System.setProperty("graphper.logging.enabled", previousLogging);
    }
  }

  @TempDir
  Path directory;

  @Test
  void switchSilencesAllLevelsAndCanReenableExistingLoggers() throws Throwable {
    String previous = System.getProperty("graphper.logging.enabled");
    List<String> calls = new ArrayList<>();
    Logger backend = (Logger) Proxy.newProxyInstance(getClass().getClassLoader(),
        new Class<?>[]{Logger.class}, (proxy, method, args) -> {
          calls.add(method.getName());
          return method.getReturnType() == boolean.class ? true : null;
        });
    Object notFormatted = new Object() {
      @Override
      public String toString() {
        throw new AssertionError("Muted arguments must not be formatted");
      }
    };
    try {
      for (boolean delegated : new boolean[]{false, true}) {
        calls.clear();
        System.setProperty("graphper.logging.enabled", "FALSE");
        try (Harness log = delegated ? new Harness(backend, null) : new Harness("missing")) {
          assertFalse(log.enabled("isDebugEnabled"));
          assertFalse(log.enabled("isWarnEnabled"));
          for (String level : new String[]{"debug", "info", "warn", "error"}) {
            log.arguments(level, "hidden {}", notFormatted);
            log.throwable(level, "hidden", new Exception("hidden"));
          }
          assertEquals("", log.output());
          assertTrue(calls.isEmpty());
          System.setProperty("graphper.logging.enabled", "true");
          assertTrue(log.enabled("isWarnEnabled"));
          log.arguments("warn", "visible {}", "again");
          if (delegated) {
            assertTrue(calls.contains("warn"));
            assertEquals("", log.output());
          } else {
            assertTrue(log.output().contains("visible again"));
          }
          int count = calls.size();
          String output = log.output();
          System.setProperty("graphper.logging.enabled", "false");
          log.arguments("error", "muted again {}", notFormatted);
          assertEquals(count, calls.size());
          assertEquals(output, log.output());
          System.clearProperty("graphper.logging.enabled");
          assertFalse(log.enabled("isWarnEnabled"));
        }
      }
      System.setProperty("graphper.logging.enabled", "false");
      try (Harness log = new Harness(NOPLogger.NOP_LOGGER,
          new AssertionError("Muted logging must not initialize the backend"))) {
        log.arguments("error", "muted");
        assertEquals("", log.output());
      }
    } finally {
      if (previous == null) {
        System.clearProperty("graphper.logging.enabled");
      } else {
        System.setProperty("graphper.logging.enabled", previous);
      }
    }
  }

  @Test
  void loggingIsSilentByDefaultAndForUnrecognizedValues() throws Throwable {
    for (String value : new String[]{null, "false", "", "yes", "1"}) {
      if (value == null) {
        System.clearProperty("graphper.logging.enabled");
      } else {
        System.setProperty("graphper.logging.enabled", value);
      }
      try (Harness log = new Harness(NOPLogger.NOP_LOGGER,
          new AssertionError("Disabled logging must not initialize SLF4J"))) {
        assertFalse(log.enabled("isDebugEnabled"));
        assertFalse(log.enabled("isWarnEnabled"));
        for (String level : new String[]{"debug", "info", "warn", "error"}) {
          log.arguments(level, "hidden");
          log.throwable(level, "hidden", new Exception("hidden"));
        }
        assertEquals("", log.output());
      }
      try (Harness log = new Harness("missing")) {
        log.arguments("warn", "hidden");
        log.throwable("error", "hidden", new Exception("hidden"));
        assertEquals("", log.output());
      }
    }
  }

  @Test
  void absentBackendFormatsLiterallyAndRetainsThrowableTraces() throws Throwable {
    try (Harness log = new Harness("missing")) {
      assertFalse(log.enabled("isDebugEnabled"));
      assertTrue(log.enabled("isWarnEnabled"));
      Object notFormatted = new Object() {
        @Override
        public String toString() {
          throw new AssertionError("Disabled logging must not format arguments");
        }
      };
      log.arguments("debug", "hidden {}", notFormatted);
      log.arguments("info", "hidden {}", notFormatted);
      assertEquals("", log.output());
      log.arguments("warn", "literal {} / {} / {}", "$1\\folder", null);
      log.arguments("warn", "escaped \\{} then {} and \\\\{}", "first", "second");
      log.arguments("warn", "extra {}", "one", "ignored");
      log.arguments("warn", null, "ignored");
      log.arguments("warn", "null array {}", (Object[]) null);
      log.throwable("warn", "explicit null {}", null);
      log.arguments("warn", "object null {}", (Object) null);
      IllegalStateException failure = new IllegalStateException("original failure");
      log.arguments("error", "trailing {}", "value", failure);
      log.throwable("error", "explicit {}", failure);
      String output = log.output();
      assertTrue(output.contains("literal $1\\folder / null / {}"), output);
      assertTrue(output.contains("escaped {} then first and \\second"), output);
      assertTrue(output.contains("extra one"), output);
      assertFalse(output.contains("ignored"), output);
      assertTrue(output.contains(" - null"), output);
      assertTrue(output.contains("null array {}"), output);
      assertTrue(output.contains("explicit null {}"), output);
      assertTrue(output.contains("object null null"), output);
      assertTrue(output.contains("trailing value"), output);
      assertTrue(output.contains("explicit {}"), output);
      assertTrue(output.contains("java.lang.IllegalStateException: original failure"), output);
      assertTrue(output.contains("at " + LogTest.class.getName()), output);
      assertFalse(output.contains("InvocationTargetException"), output);
    }
  }

  @Test
  void publicInterfaceDispatchPreservesOverloadsAndDynamicFlags() throws Throwable {
    AtomicBoolean enabled = new AtomicBoolean(false);
    List<Method> methods = new ArrayList<>();
    List<Object[]> calls = new ArrayList<>();
    Logger backend = (Logger) Proxy.newProxyInstance(getClass().getClassLoader(),
        new Class<?>[]{Logger.class, BackendMarker.class}, (proxy, method, args) -> {
          if (method.getName().startsWith("is")) {
            return enabled.get();
          }
          methods.add(method);
          calls.add(args);
          return null;
        });
    assertFalse(Modifier.isPublic(backend.getClass().getModifiers()));
    try (Harness log = new Harness(backend, null)) {
      assertFalse(log.enabled("isDebugEnabled"));
      assertFalse(log.enabled("isWarnEnabled"));
      enabled.set(true);
      assertTrue(log.enabled("isDebugEnabled"));
      assertTrue(log.enabled("isWarnEnabled"));
      Throwable failure = new IllegalArgumentException("original");
      for (String level : new String[]{"debug", "info", "warn", "error"}) {
        Object[] arguments = {"$\\value", failure};
        log.arguments(level, "{}", arguments);
        log.throwable(level, "literal {}", failure);
        log.throwable(level, "null {}", null);
        int index = methods.size() - 3;
        assertSame(Object[].class, methods.get(index).getParameterTypes()[1]);
        assertSame(arguments, calls.get(index)[1]);
        assertSame(Throwable.class, methods.get(index + 1).getParameterTypes()[1]);
        assertSame(failure, calls.get(index + 1)[1]);
        assertEquals("null {}", calls.get(index + 2)[0]);
        assertEquals(null, calls.get(index + 2)[1]);
      }
      assertEquals("", log.output());
    }
  }

  @Test
  void nopBackendNeverFallsBackToStderr() throws Throwable {
    try (Harness log = new Harness(NOPLogger.NOP_LOGGER, null)) {
      assertFalse(log.enabled("isDebugEnabled"));
      assertFalse(log.enabled("isWarnEnabled"));
      log.arguments("warn", "must remain silent");
      log.throwable("error", "must remain silent", new Exception("hidden"));
      assertEquals("", log.output());
    }
  }

  @Test
  void missingBrokenOrInaccessibleMetadataFallsBackWithoutUsingContextLoader() throws Throwable {
    for (String mode : new String[]{"missing", "linkage", "security", "incomplete", "initializer"}) {
      try (Harness log = new Harness(mode)) {
        log.arguments("warn", "fallback {}", mode);
        assertTrue(log.output().contains("fallback " + mode), log.output());
      }
    }
  }

  @Test
  void factoryFailuresAndBackendFailuresDoNotBreakLogging() throws Throwable {
    for (Throwable failure : new Throwable[]{new NoClassDefFoundError("broken backend"),
        new IllegalStateException("broken backend")}) {
      try (Harness log = new Harness(NOPLogger.NOP_LOGGER, failure)) {
        log.arguments("error", "factory fallback");
        assertTrue(log.output().contains("factory fallback"), log.output());
      }
      Logger backend = (Logger) Proxy.newProxyInstance(getClass().getClassLoader(),
          new Class<?>[]{Logger.class}, (proxy, method, args) -> {
            throw failure;
          });
      try (Harness log = new Harness(backend, null)) {
        log.arguments("warn", "invocation fallback");
        assertTrue(log.output().contains("invocation fallback"), log.output());
        assertFalse(log.enabled("isDebugEnabled"));
        assertTrue(log.enabled("isWarnEnabled"));
      }
      try (Harness log = new Harness(backend, null)) {
        assertFalse(log.enabled("isDebugEnabled"));
        log.arguments("warn", "flag fallback");
        assertTrue(log.output().contains("flag fallback"), log.output());
      }
    }
  }

  @Test
  void reflectedFatalErrorsAreRethrownUnwrapped() throws Throwable {
    for (Error fatal : new Error[]{new OutOfMemoryError("test only"), new ThreadDeath()}) {
      try (Harness log = new Harness(NOPLogger.NOP_LOGGER, fatal)) {
        assertSame(fatal, assertThrows(fatal.getClass(), log::create));
      }
      Logger backend = (Logger) Proxy.newProxyInstance(getClass().getClassLoader(),
          new Class<?>[]{Logger.class}, (proxy, method, args) -> {
            throw fatal;
          });
      try (Harness log = new Harness(backend, null)) {
        assertSame(fatal, assertThrows(fatal.getClass(), () -> log.arguments("warn", "fatal")));
        assertSame(fatal, assertThrows(fatal.getClass(), () -> log.enabled("isDebugEnabled")));
        assertEquals("", log.output());
      }
    }
  }

  @Test
  void coreBytecodeHasNoTypedSlf4jOrJulReferences() throws Exception {
    Path root = Paths.get(Log.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    List<Path> classes;
    try (Stream<Path> paths = Files.walk(root)) {
      classes = paths.filter(path -> path.toString().endsWith(".class"))
          .collect(Collectors.toList());
    }
    assertTrue(classes.size() > 100, "Scan the real core output, not only the facade");
    for (Path path : classes) {
      try (DataInputStream input = new DataInputStream(Files.newInputStream(path))) {
        assertEquals(0xCAFEBABE, input.readInt());
        input.readInt();
        int count = input.readUnsignedShort();
        String[] utf = new String[count];
        Set<Integer> literals = new HashSet<>();
        for (int i = 1; i < count; i++) {
          int tag = input.readUnsignedByte();
          switch (tag) {
            case 1:
              utf[i] = input.readUTF();
              break;
            case 8:
              literals.add(input.readUnsignedShort());
              break;
            case 3:
            case 4:
            case 9:
            case 10:
            case 11:
            case 12:
            case 17:
            case 18:
              input.readInt();
              break;
            case 5:
            case 6:
              input.readLong();
              i++;
              break;
            case 7:
            case 16:
            case 19:
            case 20:
              input.readUnsignedShort();
              break;
            case 15:
              input.readUnsignedByte();
              input.readUnsignedShort();
              break;
            default:
              throw new AssertionError("Unknown constant pool tag " + tag);
          }
        }
        for (int i = 1; i < count; i++) {
          if (utf[i] == null) {
            continue;
          }
          String value = utf[i];
          assertFalse(value.contains("org/slf4j") || value.contains("java/util/logging")
              || value.contains("java.util.logging"), path + ": " + value);
          if (value.contains("org.slf4j")) {
            assertTrue(literals.contains(i) && path.endsWith("Log$Backend.class")
                && (value.equals("org.slf4j.Logger") || value.equals("org.slf4j.LoggerFactory")),
                path + ": " + value);
          }
        }
      }
    }
  }

  private interface BackendMarker {
  }

  private final class Harness implements AutoCloseable {

    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final PrintStream previous = System.err;
    private final PrintStream capture = new PrintStream(bytes, true, "UTF-8");
    private final URLClassLoader loader;
    private final Class<?> facade;
    private Object log;

    private Harness(String mode) throws Exception {
      this(mode, null, null);
    }

    private Harness(Logger backend, Throwable failure) throws Exception {
      this("backend", backend, failure);
    }

    private Harness(String mode, Logger backend, Throwable failure) throws Exception {
      if (mode.equals("backend") || mode.equals("incomplete") || mode.equals("initializer")) {
        Path source = directory.resolve("LoggerFactory.java");
        String body = mode.equals("incomplete") ? "" : "public static org.slf4j.Logger logger;"
            + "public static Throwable failure;"
            + "public static org.slf4j.Logger getLogger(Class<?> type) throws Throwable {"
            + "if (failure != null) throw failure; return logger;}";
        if (mode.equals("initializer")) {
          body += "static { if (true) throw new IllegalStateException(\"broken initializer\"); }";
        }
        Files.write(source, ("package org.slf4j; public class LoggerFactory {" + body + "}")
            .getBytes(StandardCharsets.UTF_8));
        assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, null,
            "-classpath", Paths.get(Logger.class.getProtectionDomain().getCodeSource()
                .getLocation().toURI()).toString(), "-d", directory.toString(), source.toString()));
      }
      loader = new URLClassLoader(new URL[]{directory.toUri().toURL(),
          Log.class.getProtectionDomain().getCodeSource().getLocation()},
          LogTest.class.getClassLoader()) {
        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
          if (name.startsWith("org.slf4j.")) {
            if (mode.equals("missing")) {
              throw new ClassNotFoundException(name);
            }
            if (mode.equals("linkage")) {
              throw new NoClassDefFoundError(name);
            }
            if (mode.equals("security")) {
              throw new SecurityException(name);
            }
          }
          if (name.equals(Log.class.getName()) || name.startsWith(Log.class.getName() + "$")
              || name.equals("org.slf4j.LoggerFactory")) {
            Class<?> type = findLoadedClass(name);
            if (type == null) {
              type = findClass(name);
            }
            if (resolve) {
              resolveClass(type);
            }
            return type;
          }
          return super.loadClass(name, resolve);
        }
      };
      if (mode.equals("backend")) {
        Class<?> factory = loader.loadClass("org.slf4j.LoggerFactory");
        factory.getField("logger").set(null, backend);
        factory.getField("failure").set(null, failure);
      }
      facade = loader.loadClass(Log.class.getName());
      System.setErr(capture);
    }

    private Object create() throws Throwable {
      if (log == null) {
        log = invoke(facade.getMethod("getLogger", Class.class), null, LogTest.class);
      }
      return log;
    }

    private void arguments(String level, String message, Object... arguments) throws Throwable {
      invoke(facade.getMethod(level, String.class, Object[].class), create(), message, arguments);
    }

    private void throwable(String level, String message, Throwable failure) throws Throwable {
      invoke(facade.getMethod(level, String.class, Throwable.class), create(), message, failure);
    }

    private boolean enabled(String method) throws Throwable {
      return (Boolean) invoke(facade.getMethod(method), create());
    }

    private Object invoke(Method method, Object target, Object... arguments) throws Throwable {
      try {
        return method.invoke(target, arguments);
      } catch (InvocationTargetException e) {
        throw e.getCause();
      }
    }

    private String output() throws IOException {
      return bytes.toString("UTF-8");
    }

    @Override
    public void close() throws IOException {
      System.setErr(previous);
      capture.close();
      loader.close();
    }
  }
}
