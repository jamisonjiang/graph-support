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

package visual_case;

import helper.GraphvizVisual;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.Subgraph.SubgraphBuilder;
import org.graphper.api.attributes.Rank;

public class SubgraphAttrTest extends GraphvizVisual {

  static Stream<SubgraphBuilder> rankCase1() {
    SubgraphBuilder builder = Subgraph.builder();
    return Stream.of(builder)
        .flatMap(
            s -> Stream.of(
                s.clone().rank(Rank.MIN),
                s.clone().rank(Rank.SOURCE)
            )
        );
  }

  static Stream<SubgraphBuilder> rankCase2() {
    SubgraphBuilder builder = Subgraph.builder();
    return Stream.of(builder)
        .flatMap(
            s -> Stream.of(
                s.clone().rank(Rank.MAX),
                s.clone().rank(Rank.SINK)
            )
        );
  }

  @ParameterizedTest
  @MethodSource("rankCase1")
  public void testRankMinSource(SubgraphBuilder subgraphBuilder) {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();

    Graphviz graphviz = Graphviz.digraph()
        .label("rank_min_source_test")
        .addLine(a, b, c, d)
        .subgraph(subgraphBuilder.addNode(d).build())
        .build();
    visual(graphviz);
  }

  @ParameterizedTest
  @MethodSource("rankCase2")
  public void testRankMaxSink(SubgraphBuilder subgraphBuilder) {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();

    Graphviz graphviz = Graphviz.digraph()
        .label("rank_min_sink_test")
        .addLine(a, b, c, d)
        .subgraph(subgraphBuilder.addNode(a).build())
        .build();
    visual(graphviz);
  }

  @Test
  public void testSame() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();

    Graphviz graphviz = Graphviz.digraph()
        .label("rank_same_test")
        .addLine(a, b, c, d)
        .startSub()
        .rank(Rank.SAME)
        .addNode(b, c, d)
        .endSub()
        .build();
    visual(graphviz);
  }
}
