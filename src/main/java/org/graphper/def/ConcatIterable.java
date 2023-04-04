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
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

/**
 * A combiner that can assemble multiple Iterable objects without requiring a lot of element space,
 * and provides a converter and a filter, and the elements perform type conversion and element
 * filtering at the same time when iterating.
 *
 * <p>The following is an example that combines multiple iterators and performs transformations and
 * filters during iteration.
 * <pre>
 * <Strong>1. Indication of the type to be combined:</Strong>
 *   {@code
 *   public interface Animal { String category(); }
 *   public class Dog implements Animal {
 *      public String category() { return "Mammals"; }
 *   }
 *   public class Cat implements Animal {
 *      public String category() { return "Mammals"; }
 *   }
 *   public class Fowl implements Animal {
 *      public String category() { return "Phasianidae"; }
 *   }
 *   }
 *
 * <Strong>2. Aggregate multiple animal collections:</Strong>
 *  {@code
 *  List<Dog> dogs = ...;
 *  Queue<Cat> cats = ...;
 *  Set<Fowl> fowls = ...;
 *  // Convert function
 *  Function<Animal, String> converter = Animal::categorize;
 *  // Filter function
 *  Predicate<Animal> filter = animal -> animal.categorize().startsWith(”M“);
 *  // Combine the multiple Iterables
 *  ConcatIterable<Animal, String> combinerIterable = new ConcatIterable<>(
 *    filter, converter, dogs, cats, fowls);
 *  // Iterate elements
 *  for(String s : combinerIterable) {
 *    ....
 *  }
 *  }
 * </pre>
 *
 * @param <P> primitive element type
 * @param <T> element type
 * @author Jamison Jiang
 */
public class ConcatIterable<P, T> implements Iterable<T> {

  /**
   * Iterator collection.
   */
  private final Collection<? extends Iterable<? extends P>> iterables;

  /**
   * Element filter, filtering is done before conversion.
   */
  private final Predicate<? super T> filter;

  /**
   * Element converter, must pass filter before transform.
   */
  private final Function<? super P, ? extends T> converter;

  /**
   * Creates a combined iterator from the passed iterator array, using {@code converter} as the
   * element converter.
   *
   * @param converter element converter
   * @param iterables all iterators to be grouped
   * @throws NullPointerException if the converter is null or iterables is empty
   */
  @SafeVarargs
  public ConcatIterable(Function<? super P, ? extends T> converter,
                        Iterable<? extends P>... iterables) {
    this(converter, Stream.of(iterables).filter(Objects::nonNull).collect(Collectors.toList()));
  }

  /**
   * Creates a combined iterator from the passed iterator array, using {@code filter} as the element
   * filter and using {@code converter} as the element converter.
   *
   * @param filter    element filter
   * @param converter element converter
   * @param iterables all iterators to be grouped
   * @throws NullPointerException if the converter is null or iterables is empty
   */
  @SafeVarargs
  public ConcatIterable(Predicate<? super T> filter,
                        Function<? super P, ? extends T> converter,
                        Iterable<? extends P>... iterables) {
    this(filter, converter, Stream.of(iterables).filter(Objects::nonNull)
        .collect(Collectors.toList()));
  }

  /**
   * Creates a combined iterator from the passed iterator collection, using {@code converter} as the
   * element converter.
   *
   * @param converter element converter
   * @param iterables all iterators to be grouped
   * @throws IllegalArgumentException empty iterator collection
   * @throws NullPointerException     if the converter is null
   */
  public ConcatIterable(Function<? super P, ? extends T> converter,
                        Collection<? extends Iterable<? extends P>> iterables) {
    this(null, converter, iterables);
  }

  /**
   * Creates a combined iterator from the passed iterator collection, using {@code filter} as the
   * element filter and using {@code converter} as the element converter.
   *
   * @param filter    element filter
   * @param converter element converter
   * @param iterables all iterators to be grouped
   * @throws IllegalArgumentException empty iterator collection
   * @throws NullPointerException     if the converter is null
   */
  public ConcatIterable(Predicate<? super T> filter,
                        Function<? super P, ? extends T> converter,
                        Collection<? extends Iterable<? extends P>> iterables) {
    Asserts.illegalArgument(CollectionUtils.isEmpty(iterables), "iterables is empty!");
    Asserts.nullArgument(converter, "converter");
    this.iterables = iterables;
    this.filter = filter;
    this.converter = converter;
  }

  @Override
  public Iterator<T> iterator() {
    Iterator<? extends Iterator<? extends P>> iterator = iterables.stream()
        .map(Iterable::iterator)
        .filter(Iterator::hasNext)
        .collect(Collectors.toList())
        .iterator();

    if (!iterator.hasNext()) {
      return Collections.emptyIterator();
    }
    if (filter == null) {
      return new ConcatIterator<>(iterator, converter);
    } else {
      return new FilterContactIterator<>(iterator, converter, filter);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConcatIterable<?, ?> that = (ConcatIterable<?, ?>) o;
    return Objects.equals(iterables, that.iterables)
        && Objects.equals(filter, that.filter)
        && Objects.equals(converter, that.converter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(iterables, filter, converter);
  }

  /**
   * Iterators for ConcatIterable.
   *
   * @param <P> primitive element type
   * @param <T> element type
   */
  private static class ConcatIterator<P, T> implements Iterator<T> {

    private final Iterator<? extends Iterator<? extends P>> iterators;

    private Iterator<? extends P> curItr;

    private final Function<? super P, ? extends T> converter;

    private ConcatIterator(Iterator<? extends Iterator<? extends P>> iterators,
                           Function<? super P, ? extends T> converter) {
      this.iterators = iterators;
      this.converter = converter;
    }

    @Override
    public boolean hasNext() {
      if (iterators.hasNext()) {
        return true;
      }
      if (curItr == null) {
        return false;
      }

      return curItr.hasNext();
    }

    @Override
    public T next() {
      if (curItr == null) {
        curItr = iterators.next();
      }

      if (curItr.hasNext()) {
        return converter.apply(curItr.next());
      }

      if (!iterators.hasNext()) {
        throw new NoSuchElementException();
      }

      curItr = iterators.next();
      return converter.apply(curItr.next());
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Concat Iterator not support delete!");
    }
  }

  /**
   * Iterator to ConcatIterable with filtering capabilities.
   *
   * @param <P> primitive element type
   * @param <T> element type
   */
  public static final class FilterContactIterator<P, T> extends ConcatIterator<P, T> {

    private T current;

    private boolean currentIsConsumer = true;

    private final Predicate<? super T> filter;

    private FilterContactIterator(Iterator<? extends Iterator<? extends P>> iterators,
                                  Function<? super P, ? extends T> converter,
                                  Predicate<? super T> filter) {
      super(iterators, converter);
      Asserts.nullArgument(filter, "filter");
      this.filter = filter;
    }

    @Override
    public boolean hasNext() {
      if (!super.hasNext()) {
        if (current == null) {
          return false;
        }

        return !currentIsConsumer;
      }

      if (!currentIsConsumer) {
        return true;
      }

      do {
        current = safeNext();
      } while (!filter.test(current) && super.hasNext());

      if (!super.hasNext() && !filter.test(current)) {
        return false;
      }

      currentIsConsumer = false;
      return true;
    }

    @Override
    public T next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      currentIsConsumer = true;
      return current;
    }

    private T safeNext() {
      if (super.hasNext()) {
        return super.next();
      }

      return null;
    }
  }
}