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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class ClassUtils {

  private ClassUtils() {
  }

  /**
   * Returns a map of the values of all non-static properties of the object.
   *
   * @param obj object to be acquired
   * @return a map of attributes and attribute values
   * @throws  SecurityException
   *          If a security manager, <i>s</i>, is present and any of the
   *          following conditions is met:
   *
   *          <ul>
   *
   *          <li> the caller's class loader is not the same as the
   *          class loader of this class and invocation of
   *          {@link SecurityManager#checkPermission
   *          s.checkPermission} method with
   *          {@code RuntimePermission("accessDeclaredMembers")}
   *          denies access to the declared fields within this class.
   *
   *          <li> the caller's class loader is not the same as or an
   *          ancestor of the class loader for the current class and
   *          invocation of {@link SecurityManager#checkPackageAccess
   *          s.checkPackageAccess()} denies access to the package
   *          of this class.
   *
   *          <li>if the request is denied.
   *
   *          </ul>
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
}
