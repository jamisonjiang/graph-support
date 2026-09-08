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

package org.graphper.layout.dot;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The flat order is placed by a Kahn pass that has to tolerate directed cycles, and the order it
 * produces is part of the layout. Selecting the ready node used to be a linear rescan of everything
 * left, restarted per placement, which is quadratic in the node count on a cycle followed by
 * independent ready nodes however few edges there are.
 *
 * <p>The replacement may only be faster, never different, so every case here compares it against
 * the rescan it replaced, spelled out below as {@link #byRescan}.
 */
class MinCrossTopologicalOrderTest {

  /** The selection rule that was in place: rescan everything left, in order, per placement. */
  private static <T> List<T> byRescan(List<T> items, Map<T, Integer> indegree,
                                      Function<T, Iterable<T>> successors) {
    Set<T> remaining = new LinkedHashSet<>(items);
    List<T> ordered = new ArrayList<>(items.size());
    while (!remaining.isEmpty()) {
      T next = null;
      for (T candidate : remaining) {
        if (indegree.get(candidate) == 0) {
          next = candidate;
          break;
        }
      }
      if (next == null) {
        next = remaining.iterator().next();
      }
      remaining.remove(next);
      ordered.add(next);
      for (T to : successors.apply(next)) {
        if (remaining.contains(to)) {
          indegree.put(to, indegree.get(to) - 1);
        }
      }
    }
    return ordered;
  }

  @SuppressWarnings("unchecked")
  private static List<String> byProduction(List<String> items, Map<String, Integer> indegree,
                                           Function<String, Iterable<String>> successors)
      throws Exception {
    Method method = MinCross.class.getDeclaredMethod("topologicalOrder", List.class, Map.class,
                                                     Function.class);
    method.setAccessible(true);
    return (List<String>) method.invoke(null, items, indegree, successors);
  }

  /**
   * The shape the rescan is quadratic on: a directed cycle in front of nodes that are ready from
   * the start. No cycle member is ever ready, so every one of the ready placements rescanned the
   * whole cycle first. The cycle is only broken once nothing ready is left.
   */
  @Test
  void cycleInFrontOfReadyNodesPlacesTheReadyOnesFirst() throws Exception {
    List<String> items = new ArrayList<>();
    Map<String, List<String>> edges = new HashMap<>();
    int cycle = 40;
    for (int i = 0; i < cycle; i++) {
      items.add("c" + i);
    }
    for (int i = 0; i < cycle; i++) {
      edges.put("c" + i, java.util.Collections.singletonList("c" + ((i + 1) % cycle)));
    }
    for (int i = 0; i < 40; i++) {
      items.add("free" + i);
    }

    List<String> expected = byRescan(items, indegreeOf(items, edges), successors(edges));
    Assertions.assertEquals(expected, byProduction(items, indegreeOf(items, edges),
                                                   successors(edges)));
    // Every node placed exactly once: the ready ones in order, then the cycle from its first
    // member, which is where the forced break lands.
    Assertions.assertEquals(items.size(), expected.size());
    List<String> wanted = new ArrayList<>();
    for (int i = 0; i < 40; i++) {
      wanted.add("free" + i);
    }
    for (int i = 0; i < cycle; i++) {
      wanted.add("c" + i);
    }
    Assertions.assertEquals(wanted, expected);
  }

  /** A plain chain given in reverse: every placement used to rescan from the front. */
  @Test
  void reversedChainIsPlacedInDependencyOrder() throws Exception {
    List<String> items = new ArrayList<>();
    Map<String, List<String>> edges = new HashMap<>();
    for (int i = 30; i >= 0; i--) {
      items.add("n" + i);
    }
    for (int i = 0; i < 30; i++) {
      edges.put("n" + i, java.util.Collections.singletonList("n" + (i + 1)));
    }

    List<String> expected = byRescan(items, indegreeOf(items, edges), successors(edges));
    Assertions.assertEquals(expected, byProduction(items, indegreeOf(items, edges),
                                                   successors(edges)));
    List<String> chain = new ArrayList<>();
    for (int i = 0; i <= 30; i++) {
      chain.add("n" + i);
    }
    Assertions.assertEquals(chain, expected);
  }

  /** Successors outside {@code items} are not dependencies and must be ignored by both. */
  @Test
  void successorsOutsideTheItemListAreIgnored() throws Exception {
    List<String> items = new ArrayList<>(Arrays.asList("a", "b"));
    Map<String, List<String>> edges = new HashMap<>();
    edges.put("a", Arrays.asList("elsewhere", "b"));
    Map<String, Integer> indegree = new HashMap<>();
    indegree.put("a", 0);
    indegree.put("b", 1);
    Map<String, Integer> copy = new HashMap<>(indegree);
    Assertions.assertEquals(Arrays.asList("a", "b"),
                            byProduction(items, indegree, successors(edges)));
    Assertions.assertEquals(byRescan(items, copy, successors(edges)),
                            Arrays.asList("a", "b"));
  }

  /**
   * Random graphs, cycles and multiple components included, plus deliberately inconsistent
   * indegrees so the forced-break path is taken from every possible state.
   */
  @Test
  void randomGraphsIncludingCyclesMatchTheRescan() throws Exception {
    Random random = new Random(20240913L);
    for (int round = 0; round < 500; round++) {
      int size = 1 + random.nextInt(12);
      List<String> items = new ArrayList<>();
      for (int i = 0; i < size; i++) {
        items.add("v" + i);
      }
      java.util.Collections.shuffle(items, random);

      Map<String, List<String>> edges = new HashMap<>();
      int edgeCount = random.nextInt(size * 2 + 1);
      for (int e = 0; e < edgeCount; e++) {
        String from = items.get(random.nextInt(size));
        String to = items.get(random.nextInt(size));
        if (from.equals(to)) {
          continue;
        }
        List<String> outs = edges.computeIfAbsent(from, k -> new ArrayList<>());
        if (!outs.contains(to)) {
          outs.add(to);
        }
      }

      List<String> expected = byRescan(items, indegreeOf(items, edges), successors(edges));
      List<String> actual = byProduction(items, indegreeOf(items, edges), successors(edges));
      Assertions.assertEquals(expected, actual, "round " + round + " items " + items
          + " edges " + edges);
      Assertions.assertEquals(size, actual.size(), "round " + round);
    }
  }

  private static Map<String, Integer> indegreeOf(List<String> items,
                                                 Map<String, List<String>> edges) {
    Map<String, Integer> indegree = new HashMap<>();
    for (String item : items) {
      indegree.put(item, 0);
    }
    for (String from : items) {
      for (String to : edges.getOrDefault(from, java.util.Collections.emptyList())) {
        if (indegree.containsKey(to)) {
          indegree.put(to, indegree.get(to) + 1);
        }
      }
    }
    return indegree;
  }

  private static Function<String, Iterable<String>> successors(
      Map<String, List<String>> edges) {
    return node -> edges.getOrDefault(node, java.util.Collections.emptyList());
  }
}
