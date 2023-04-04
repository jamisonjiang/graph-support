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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.graphper.layout.Mark;

class Acyclic extends Mark<DNode> {

  private static final Logger log = LoggerFactory.getLogger(Acyclic.class);

  private final DotDigraph digraph;

  private Set<DNode> accessStack;

  Acyclic(DotDigraph digraph) {
    this.digraph = digraph;

    acyclic();
  }

  private void acyclic() {
    DLine line;
    while ((line = reverseLine()) != null) {

      if (log.isDebugEnabled()) {
        log.debug("Cycle line: {}", line);
      }

      // Self loop
      if (Objects.equals(line.from(), line.to())) {
        digraph.removeEdge(line);
        if (!line.isVirtual()) {
          line.from().addSelfLine(line);
        }
      } else {
        // Reverse loop line
        digraph.reverseEdge(line);
      }
    }
  }

  private DLine reverseLine() {
    if (accessStack == null) {
      accessStack = new HashSet<>(digraph.vertexNum());
    } else {
      accessStack.clear();
    }
    clear();

    DLine line;
    for (DNode v : digraph) {
      if (isMark(v)) {
        continue;
      }

      if ((line = dfs(v)) != null) {
        return line;
      }
    }

    return null;
  }

  private DLine dfs(DNode v) {
    mark(v);

    accessStack.add(v);

    DLine l;
    for (DLine line : digraph.adjacent(v)) {
      DNode w = line.other(v);

      if (accessStack.contains(w)) {
        return line;
      }

      if (isMark(w)) {
        continue;
      }

      if ((l = dfs(w)) != null) {
        return l;
      }
    }

    accessStack.remove(v);

    return null;
  }
}
