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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for working with reflection and class instantiation.
 */
@SuppressWarnings("all")
public class ClassUtils {

  private ClassUtils() {
  }

  /**
   * Creates a new instance of the specified class without parameters.
   *
   * @param clazz The class to instantiate.
   * @return The new instance of the class.
   * @throws Exception If instantiation fails.
   */
  public static Object newObject(Class<?> clazz) throws Exception {
    return newObject(clazz, null, (Object[]) null);
  }

  /**
   * Creates a new instance of the specified class with a single parameter.
   *
   * @param clazz         The class to instantiate.
   * @param parameterType The type of the parameter.
   * @param param         The parameter value.
   * @return The new instance of the class.
   * @throws Exception If instantiation fails.
   */
  public static Object newObjectOne(Class<?> clazz, Class<?> parameterType, Object param)
      throws Exception {
    return newObject(clazz, new Class[]{parameterType}, param);
  }

  /**
   * Creates a new instance of the specified class with parameters.
   *
   * @param clazz  The class to instantiate.
   * @param params The parameter values.
   * @return The new instance of the class.
   * @throws Exception If instantiation fails.
   */
  public static Object newObject(Class<?> clazz, Object... params) throws Exception {
    if (params == null || params.length == 0) {
      return newObject(clazz);
    }

    Class<?>[] parameterTypes = new Class[params.length];
    for (int i = 0; i < params.length; i++) {
      Asserts.illegalArgument(params[i] == null, "Constructor can not have null parameter");
      parameterTypes[i] = getClass(params[i]);
    }
    return newObject(clazz, parameterTypes, params);
  }

  /**
   * Creates a new instance of the specified class with parameters.
   *
   * @param clazz          The class to instantiate.
   * @param parameterTypes The types of the parameters.
   * @param params         The parameter values.
   * @return The new instance of the class.
   * @throws Exception If instantiation fails.
   */
  public static Object newObject(Class<?> clazz, Class<?>[] parameterTypes, Object... params)
      throws Exception {
    Asserts.nullArgument(clazz, "clazz");
    Constructor<?> constructor = clazz.getConstructor(parameterTypes);
    return constructor.newInstance(params);
  }

  /**
   * Invokes a method with no parameters on the specified object.
   *
   * @param obj        The object on which to invoke the method.
   * @param methodName The name of the method to invoke.
   * @return The result of the method invocation.
   * @throws Exception If method invocation fails.
   */
  public static Object invoke(Object obj, String methodName) throws Exception {
    return invoke(obj, methodName, null, (Object[]) null);
  }

  /**
   * Invokes a method with parameters on the specified object.
   *
   * @param obj        The object on which to invoke the method.
   * @param methodName The name of the method to invoke.
   * @param params     The parameter values.
   * @return The result of the method invocation.
   * @throws Exception If method invocation fails.
   */
  public static Object invoke(Object obj, String methodName, Object... params) throws Exception {
    if (params == null || params.length == 0) {
      return invoke(obj, methodName);
    }
    Class<?>[] parameterTypes = new Class[params.length];
    for (int i = 0; i < params.length; i++) {
      Asserts.illegalArgument(params[i] == null, "Method can not have null parameter");
      parameterTypes[i] = getClass(params[i]);
    }
    return invoke(obj, methodName, parameterTypes, params);
  }

  /**
   * Invokes a method with a single parameter on the specified object.
   *
   * @param obj           The object on which to invoke the method.
   * @param methodName    The name of the method to invoke.
   * @param parameterType The type of the parameter.
   * @param param         The parameter value.
   * @return The result of the method invocation.
   * @throws Exception If method invocation fails.
   */
  public static Object invokeOne(Object obj, String methodName,
                                 Class<?> parameterType, Object param) throws Exception {
    return invoke(obj, methodName, new Class[]{parameterType}, param);
  }

  /**
   * Invokes a method with parameters on the specified object.
   *
   * @param obj            The object on which to invoke the method.
   * @param methodName     The name of the method to invoke.
   * @param parameterTypes The types of the parameters.
   * @param params         The parameter values.
   * @return The result of the method invocation.
   * @throws Exception If method invocation fails.
   */
  public static Object invoke(Object obj, String methodName, Class<?>[] parameterTypes,
                              Object... params) throws Exception {
    Asserts.nullArgument(obj, "obj");
    Method method = obj.getClass().getMethod(methodName, parameterTypes);
    return method.invoke(obj, params);
  }

  /**
   * Retrieves the value of a field from the specified object.
   *
   * @param obj       The object from which to retrieve the field value.
   * @param fieldName The name of the field.
   * @return The value of the field.
   * @throws Exception If field retrieval fails.
   */
  public static Object getField(Object obj, String fieldName) throws Exception {
    Asserts.nullArgument(obj, "obj");
    Class<?> clazz = obj.getClass();
    Field field = clazz.getField(fieldName);
    if (!field.isAccessible()) {
      field.setAccessible(true);
    }
    return field.get(obj);
  }

  /**
   * Invokes a static method on the specified class with no parameters.
   *
   * @param clazz      The class on which to invoke the static method.
   * @param methodName The name of the static method to invoke.
   * @return The result of the static method invocation.
   * @throws Exception If static method invocation fails.
   */
  public static Object invokeStatic(Class<?> clazz, String methodName) throws Exception {
    return invokeStatic(clazz, methodName, null, (Object[]) null);
  }

  /**
   * Invokes a static method on the specified class with parameters.
   *
   * @param clazz          The class on which to invoke the static method.
   * @param methodName     The name of the static method to invoke.
   * @param parameterTypes The types of the parameters.
   * @param params         The parameter values.
   * @return The result of the static method invocation.
   * @throws Exception If static method invocation fails.
   */
  public static Object invokeStatic(Class<?> clazz, String methodName,
                                    Class<?>[] parameterTypes, Object... params) throws Exception {
    Asserts.nullArgument(clazz, "class");
    Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
    method.setAccessible(true);
    return method.invoke(null, params);
  }

  /**
   * Retrieves the value of a static field from the specified class.
   *
   * @param clazz     The class from which to retrieve the static field value.
   * @param fieldName The name of the static field.
   * @return The value of the static field.
   * @throws Exception If static field retrieval fails.
   */
  public static Object getStaticField(Class<?> clazz, String fieldName) throws Exception {
    Asserts.nullArgument(clazz, "class");
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(null);
  }

  /**
   * Returns a map of the values of all non-static properties of the object.
   *
   * @param obj object to be acquired
   * @return a map of attributes and attribute values
   * @throws SecurityException           If a security manager, <i>s</i>, is present and any of the
   *                                     following conditions is met:
   *
   *                                     <ul>
   *
   *                                     <li> the caller's class loader is not the same as the
   *                                     class loader of this class and invocation of
   *                                     {@link SecurityManager#checkPermission
   *                                     s.checkPermission} method with
   *                                     {@code RuntimePermission("accessDeclaredMembers")}
   *                                     denies access to the declared fields within this class.
   *
   *                                     <li> the caller's class loader is not the same as or an
   *                                     ancestor of the class loader for the current class and
   *                                     invocation of {@link SecurityManager#checkPackageAccess
   *                                     s.checkPackageAccess()} denies access to the package
   *                                     of this class.
   *
   *                                     <li>if the request is denied.
   *
   *                                     </ul>
   * @throws IllegalAccessException      if this {@code Field} object is enforcing Java language
   *                                     access control and the underlying field is inaccessible.
   * @throws IllegalArgumentException    if the specified object is not an instance of the class or
   *                                     interface declaring the underlying field (or a subclass or
   *                                     implementor thereof).
   * @throws NullPointerException        if the specified object is null and the field is an
   *                                     instance field.
   * @throws ExceptionInInitializerError if the initialization provoked by this method fails.
   */
  public static Map<String, Object> propValMap(Object obj) throws IllegalAccessException {
    if (obj == null) {
      return Collections.emptyMap();
    }

    Class<?> cls = obj.getClass();

    Field[] fields = cls.getDeclaredFields();
    if (fields.length == 0) {
      return Collections.emptyMap();
    }

    Map<String, Object> map = new HashMap<>(fields.length);
    for (Field field : fields) {
      if (Modifier.isStatic(field.getModifiers())) {
        continue;
      }

      String name = field.getName();
      field.setAccessible(true);
      Object propVal = field.get(obj);
      if (propVal == null) {
        continue;
      }
      field.setAccessible(false);
      map.put(name, propVal);
    }

    return map;
  }

  public static void modifyField(Object obj, String fieldName, Object value)
      throws NoSuchFieldException, IllegalAccessException {
    Asserts.nullArgument(obj, "Null object");
    Asserts.nullArgument(fieldName, "Null field name");

    Class<?> clazz = obj.getClass();
    Field field = clazz.getDeclaredField(fieldName);
    if (Modifier.isStatic(field.getModifiers())) {
      return;
    }

    boolean accessible = field.isAccessible();
    field.setAccessible(true);
    field.set(obj, value);
    field.setAccessible(accessible);
  }

  private static Class<?> getClass(Object obj) {
    Class<?> clazz = obj.getClass();
    if (clazz == Integer.class) {
      return Integer.TYPE;
    } else if (clazz == Long.class) {
      return Long.TYPE;
    } else if (clazz == Short.class) {
      return Short.TYPE;
    } else if (clazz == Byte.class) {
      return Byte.TYPE;
    } else if (clazz == Double.class) {
      return Double.TYPE;
    } else if (clazz == Float.class) {
      return Float.TYPE;
    } else if (clazz == Character.class) {
      return Character.TYPE;
    } else if (clazz == Boolean.class) {
      return Boolean.TYPE;
    } else {
      return clazz;
    }
  }
}
