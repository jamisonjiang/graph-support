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

package org.graphper.def;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Special {@link ConcatIterable}, the aggregated iterator is still the original element type, the
 * converter uses {@link Function#identity()}.
 *
 * @param <T> element type
 * @author Jamison Jiang
 */
public class BiConcatIterable<T> extends ConcatIterable<T, T> {

  /**
   * Creates a combined iterator from the passed iterator array.
   *
   * @param iterables all iterators to be grouped
   * @throws NullPointerException if the converter is null or iterables is empty
   */
  @SafeVarargs
  public BiConcatIterable(Iterable<? extends T>... iterables) {
    super(Function.identity(), iterables);
  }

  /**
   * Creates a combined iterator from the passed iterator array, using {@code filter} as the element
   * filter.
   *
   * @param filter    element filter
   * @param iterables all iterators to be grouped
   * @throws NullPointerException if the converter is null or iterables is empty
   */
  @SafeVarargs
  public BiConcatIterable(Predicate<? super T> filter, Iterable<? extends T>... iterables) {
    super(filter, Function.identity(), iterables);
  }

  /**
   * Creates a combined iterator from the passed iterator collection.
   *
   * @param iterables all iterators to be grouped
   * @throws IllegalArgumentException empty iterator collection
   * @throws NullPointerException     if the converter is null
   */
  public BiConcatIterable(Collection<? extends Iterable<? extends T>> iterables) {
    super(Function.identity(), iterables);
  }

  /**
   * Creates a combined iterator from the passed iterator collection, using {@code filter} as the
   * element filter.
   *
   * @param filter    element filter
   * @param iterables all iterators to be grouped
   * @throws IllegalArgumentException empty iterator collection
   * @throws NullPointerException     if the converter is null
   */
  public BiConcatIterable(Predicate<? super T> filter,
                          Collection<? extends Iterable<? extends T>> iterables) {
    super(filter, Function.identity(), iterables);
  }
}
