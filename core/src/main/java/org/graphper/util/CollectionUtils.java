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

import java.util.Collection;

/**
 * A collection function aggregates.
 *
 * @author Jamison Jiang
 */
public final class CollectionUtils {

  private CollectionUtils() {
  }

  /**
   * Determine whether the collection is a null value or an empty collection.
   *
   * @param collection the collection which be detected
   * @return <tt>true</tt> if the collections is null or empty
   */
  public static boolean isEmpty(Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

  /**
   * Determine whether the collection is not a null value and an empty collection.
   *
   * @param collection the collection which be detected
   * @return <tt>true</tt> if the collections is not null and empty
   */
  public static boolean isNotEmpty(Collection<?> collection) {
    return !isEmpty(collection);
  }
}
