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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.attributes.Rank;
import org.graphper.layout.dot.SubgraphMerge.MergeNode;

public class SubgraphMergeTest {

  @Test
  public void testNewSubgraphMerge() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();

    Graphviz graphviz = Graphviz.digraph()
        .addNode(a)
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addLine(b, a)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SINK)
                .addLine(c, d)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addLine(d, e)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .rank(Rank.MAX)
                .addLine(e, f)
                .build()
        )
        .build();

    TLayout tLayout = new TLayout();
    tLayout.layout(graphviz);
    DotAttachment dotAttachment = tLayout.dotAttachment;

    DNode db = dotAttachment.get(b);
    DNode dc = dotAttachment.get(c);
    DNode dd = dotAttachment.get(d);
    DNode df = dotAttachment.get(f);

    SubgraphMerge subgraphMerge = SubgraphMerge
        .newSubgraphMerge(graphviz, tLayout.dotAttachment, null);

    MergeNode mb = subgraphMerge.getMergeNode(db);
    MergeNode mf = subgraphMerge.getMergeNode(df);

    Assertions.assertNotNull(mb);
    Assertions.assertEquals(mb.getRank(), Rank.SAME);

    Assertions.assertNotNull(mf);
    Assertions.assertEquals(mf.getRank(), Rank.SINK);
  }

  @Test
  public void testSubgrahOppositRankException() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();

    Graphviz graphviz = Graphviz.digraph()
        .addNode(a)
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addLine(b, a)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SOURCE)
                .addLine(c, d)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addLine(d, e)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .rank(Rank.MAX)
                .addLine(e, f)
                .build()
        )
        .build();

    TLayout tLayout = new TLayout();
    tLayout.layout(graphviz);

    Assertions.assertThrows(SubgrahOppositRankException.class, () -> SubgraphMerge
        .newSubgraphMerge(graphviz, tLayout.dotAttachment, null));
  }
}
