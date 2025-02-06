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

package regression;

import helper.GraphvizVisual;
import org.graphper.api.Cluster;
import org.graphper.api.FloatLabel;
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.LineStyle;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Rank;
import org.graphper.api.attributes.Rankdir;
import org.junit.jupiter.api.Test;

public class PortTest extends GraphvizVisual {

  @Test
  public void testAllPortPosition() {
    /*
    digraph {
        rankdir=LR
        // "n","ne","e","se","s","sw","w","nw","c","_"
        node[height=0.001,width=0.001,fontsize=4]
        tail[fontsize=14,shape=rect]
        n[label="n"]
        ne[label="ne"]
        e[label="e"]
        se[label="se"]
        s[label="s"]
        sw[label="sw"]
        w[label="w"]
        nw[label="nw"]
        // 9[label="c"]
        // 10[label="_"]
        w -> tail:w[dir=back]
        nw -> tail:nw[dir=back]
        n -> tail:n[dir=back]
        ne -> tail:ne[dir=back]
        // 9 -> tail:c[dir=back]
        tail:e -> e:w
        // 10 -> tail:_[dir=back]
        tail:se -> se
        tail:s -> s
        tail:sw -> sw

        {
            rank=same;
            w,tail,e
        }
    }
     */
    Node n = Node.builder().label("n").build();
    Node ne = Node.builder().label("ne").build();
    Node e = Node.builder().label("e").build();
    Node se = Node.builder().label("se").build();
    Node s = Node.builder().label("s").build();
    Node sw = Node.builder().label("sw").build();
    Node w = Node.builder().label("w").build();
    Node nw = Node.builder().label("nw").build();
    Node tail = Node.builder()
//        .shape(NodeShapeEnum.RECT)
        .label("tail")
        .build();

    Graphviz graphviz = Graphviz.digraph()
//        .tempLine(Line.tempLine().tailclip(false).headclip(false).controlPoints(true).build())
//        .rankdir(Rankdir.BT)
        .addLine(Line.builder(w, tail).dir(Dir.BACK).headPort(Port.WEST).build())
        .addLine(Line.builder(nw, tail).dir(Dir.BACK).headPort(Port.NORTH_WEST).build())
        .addLine(Line.builder(n, tail).dir(Dir.BACK).headPort(Port.NORTH).build())
        .addLine(Line.builder(ne, tail).dir(Dir.BACK).headPort(Port.NORTH_EAST).build())
        .addLine(Line.builder(tail, e).tailPort(Port.EAST).headPort(Port.WEST).build())
        .addLine(Line.builder(tail, se).tailPort(Port.SOUTH_EAST).build())
        .addLine(Line.builder(tail, s).tailPort(Port.SOUTH).build())
        .addLine(Line.builder(tail, sw).tailPort(Port.SOUTH_WEST).build())
        .startSub()
        .rank(Rank.SAME)
        .addNode(w)
        .addNode(tail)
        .addNode(e)
        .endSub()

//        .rankdir(Rankdir.LR)
//        .addLine(Line.builder(n1, n2).tailPort(Port.NORTH_WEST).headPort(Port.SOUTH).build())
        .build();

    visual(graphviz);

    /*
    digraph {
        rankdir=LR
        // "n","ne","e","se","s","sw","w","nw","c","_"
        node[height=0.001,width=0.001,fontsize=4]
        tail[fontsize=14,shape=rect]
        n[label="n"]
        ne[label="ne"]
        e[label="e"]
        se[label="se"]
        s[label="s"]
        sw[label="sw"]
        w[label="w"]
        nw[label="nw"]
        // 9[label="c"]
        // 10[label="_"]
        w -> tail:w[dir=back]
        nw -> tail:nw[dir=back]
        n -> tail:n[dir=back]
        tail:ne -> ne
        // 9 -> tail:c[dir=back]
        tail:e -> e:w
        // 10 -> tail:_[dir=back]
        tail:se -> se
        tail:s -> s:n
        sw -> tail:sw[dir=back]

        {
            rank=same;
            s,tail,n
        }
    }
     */
    graphviz = Graphviz.digraph()
        .rankdir(Rankdir.LR)
        .tempNode(Node.builder().shape(NodeShapeEnum.TRIANGLE).build())
        .addLine(Line.builder(w, tail).dir(Dir.BACK).headPort(Port.WEST).build())
        .addLine(Line.builder(nw, tail).dir(Dir.BACK).headPort(Port.NORTH_WEST).build())
        .addLine(Line.builder(tail, n).headPort(Port.SOUTH).build())
        .addLine(Line.builder(tail, ne).tailPort(Port.NORTH_EAST).build())
        .addLine(Line.builder(tail, e).tailPort(Port.EAST).headPort(Port.WEST).build())
        .addLine(Line.builder(tail, se).tailPort(Port.SOUTH_EAST).build())
        .addLine(
            Line.builder(s, tail).dir(Dir.BACK).tailPort(Port.NORTH).headPort(Port.SOUTH).build())
        .addLine(Line.builder(sw, tail).dir(Dir.BACK).headPort(Port.SOUTH_WEST).build())
        .startSub()
        .rank(Rank.SAME)
        .addNode(s)
        .addNode(tail)
        .addNode(n)
        .endSub()
        .build();

    visual(graphviz);
  }

  @Test
  public void testPortLine()  {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();

    Graphviz graphviz = Graphviz.digraph()
        .addLine(
            Line.builder(a, b)
                .tailPort(Port.NORTH)
                .headPort(Port.SOUTH_EAST)
                .showboxes(true)
                .build()
        )
        .build();

    visual(graphviz);
  }

  @Test
  public void testSelfLine()  {
    /*
    digraph G {
        a:w->a:ne
        a:w->a:e[label="1111\n2222"]
        a:w->a:e[label="3333\n4444"]

        b:w->b:sw
        b:w->b:sw
        b:w->b:s
        b:w->b:se
        b:w->b:se[label="WEST -> SOUTH_WEST"]
        b:w->b:se

        c:w->c:nw[label="2222\n333333333333\n4444"]
        c:w->c:nw[label="hello world\nhello world\nhello world\nhello world"]
        c:sw->c:n
    }
     */

    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();
    Node g = Node.builder().label("g").build();
    Node h = Node.builder().label("h").build();
    Node i = Node.builder().label("i").build();
    Node j = Node.builder().label("j").build();
    Node k = Node.builder().label("k").build();
    Node l = Node.builder().label("l").build();
    Node m = Node.builder().label("m").build();
    Node n = Node.builder().label("n").build();

    GraphvizBuilder graphvizBuilder = Graphviz.digraph()
        .scale(7)
//        .splines(Splines.ORTHO)
        .addLine(Line.builder(a, a).tailPort(Port.WEST).headPort(Port.NORTH_EAST).build())
        .addLine(Line.builder(a, a).tailPort(Port.WEST).build())
        .addLine(
            Line.builder(a, a).tailPort(Port.WEST).headPort(Port.EAST)
                .label("1111\n2222")
                .build()
        )
        .addLine(
            Line.builder(a, a).tailPort(Port.WEST).headPort(Port.EAST)
                .label("3333\n4444")
                .build()
        )

        .addLine(Line.builder(b, b).tailPort(Port.WEST).headPort(Port.SOUTH_WEST).build())
        .addLine(Line.builder(b, b).tailPort(Port.WEST).headPort(Port.SOUTH_WEST).build())
        .addLine(Line.builder(b, b).tailPort(Port.WEST).headPort(Port.SOUTH).build())
        .addLine(Line.builder(b, b).tailPort(Port.WEST).headPort(Port.SOUTH_EAST).build())
        .addLine(Line.builder(b, b).tailPort(Port.NORTH_WEST).headPort(Port.SOUTH_WEST).label("WEST -> SOUTH_WEST").build())
        .addLine(Line.builder(b, b).tailPort(Port.NORTH_WEST).headPort(Port.SOUTH_WEST).build())


        .addLine(Line.builder(c, c).tailPort(Port.WEST).headPort(Port.NORTH_WEST).label("2222\n333333333333\n4444").build())
        .addLine(Line.builder(c, c).tailPort(Port.WEST).headPort(Port.NORTH_WEST).label("hello world\nhello world\nhello world\nhello world").build())
        .addLine(Line.builder(c, c).tailPort(Port.SOUTH_WEST).headPort(Port.NORTH).build())

        .addLine(
            Line.builder(d, d).tailPort(Port.WEST).headPort(Port.EAST)
                .label("Graphviz self loop")
                .build()
        )
        .addLine(
            Line.builder(d, d).tailPort(Port.WEST).headPort(Port.EAST)
                .label("Port line label")
                .build()
        )
        .addLine(Line.builder(d, d).tailPort(Port.WEST).headPort(Port.EAST).build())
        .addLine(Line.builder(d, d).tailPort(Port.NORTH).headPort(Port.NORTH_EAST).build())
        .addLine(Line.builder(d, d).tailPort(Port.NORTH_WEST).headPort(Port.NORTH).build())

        .addLine(Line.builder(e, e).dir(Dir.BOTH).arrowSize(0.6).tailPort(Port.NORTH_WEST).headPort(Port.EAST).build())
        .addLine(Line.builder(e, e).dir(Dir.BOTH).arrowSize(0.3).tailPort(Port.EAST).headPort(Port.SOUTH).label("EAST->SOUTH").build())

        .addLine(
            Line.builder(f, f)
                .tailPort(Port.NORTH)
                .headPort(Port.EAST)
                .floatLabels(
                    FloatLabel.builder()
                        .label("Tail")
                        .lengthRatio(0)
                        .build(),
                    FloatLabel.builder()
                        .label("Center")
                        .lengthRatio(0.5)
                        .build(),
                    FloatLabel.builder()
                        .label("Head")
                        .lengthRatio(1)
                        .build()
                )
                .build()
        )

        .addLine(Line.builder(g, g).tailPort(Port.NORTH_EAST).headPort(Port.EAST).build())
        .addLine(Line.builder(g, g).tailPort(Port.NORTH_EAST).headPort(Port.EAST).build())
        .addLine(g, g)

        .addLine(Line.builder(c, h).style(LineStyle.INVIS).build())

        .addLine(h, h)
        .addLine(h, h)
        .addLine(h, h)
        .addLine(Line.builder(h, h).tailPort(Port.SOUTH_WEST).headPort(Port.SOUTH_WEST).build())
        .addLine(Line.builder(h, h).label("Self Line H -> Self Line H").build())

        .addLine(Line.builder(i, i).tailPort(Port.WEST).headPort(Port.WEST).build())
        .addLine(Line.builder(i, i).tailPort(Port.NORTH_WEST).headPort(Port.NORTH_WEST).build())

        .addLine(Line.builder(j, j).tailPort(Port.NORTH).headPort(Port.NORTH).build())
        .addLine(Line.builder(j, j).tailPort(Port.NORTH_EAST).headPort(Port.NORTH_EAST).build())

        .addLine(Line.builder(k, k).tailPort(Port.EAST).headPort(Port.EAST).build())
        .addLine(Line.builder(k, k).tailPort(Port.SOUTH_EAST).headPort(Port.SOUTH_EAST).build())

        .addLine(Line.builder(l, l).tailPort(Port.SOUTH).headPort(Port.SOUTH).build())
        .addLine(Line.builder(l, l).tailPort(Port.SOUTH_WEST).headPort(Port.SOUTH_WEST).build())

        .addLine(Line.builder(m, m).tailPort(Port.SOUTH).headPort(Port.SOUTH).arrowSize(0.3).dir(Dir.BOTH).build())
        .addLine(Line.builder(m, m).tailPort(Port.NORTH).headPort(Port.NORTH).arrowSize(0.5).dir(Dir.BOTH).build())

        .addLine(Line.builder(n, n).headclip(false).build())
        .addLine(Line.builder(n, n).tailclip(false).build())
        .addLine(Line.builder(n, n).tailclip(false).headclip(false).build())
        .addLine(n, n)
        .addLine(n, n)
        .addLine(n, n)
        .addLine(n, n)
        .addLine(n, n)
        .addLine(n, n)
        .addLine(n, n)
        .addLine(n, n)
        .addLine(Line.builder(n, n).label("fff").build())
        .addLine(Line.builder(n, n).label("self line").build())
        .addLine(n, n)
        .addLine(Line.builder(n, n).label("yyyyy").build())

        .cluster(
            Cluster.builder()
                .addNode(c)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addNode(h, i, j, k, l, m, n)
                .build()
        )
        ;

    visual(graphvizBuilder.build());

    visual(graphvizBuilder.rankdir(Rankdir.LR)
             .tempNode(Node.builder().shape(NodeShapeEnum.RECT).build())
             .build());
    visual(graphvizBuilder.rankdir(Rankdir.RL)
             .tempNode(Node.builder().shape(NodeShapeEnum.POINT).build())
             .build());
    visual(graphvizBuilder.rankdir(Rankdir.BT)
                        .tempNode(Node.builder().shape(NodeShapeEnum.TRIANGLE).build())
                        .build());
  }

  @Test
  public void testParallelLine()  {
    Node a = Node.builder().label("a").width(3).build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").width(3).build();
    Node g = Node.builder().label("g").build();
    Node h = Node.builder().label("h").build();
    Node i = Node.builder().label("i").build();
    Node j = Node.builder().label("j").build();
    Node k = Node.builder().label("k").build();
    Node l = Node.builder().label("l").build();
    Node m = Node.builder().label("m").build();
    Node n = Node.builder().label("n").build();
    Node o = Node.builder().label("o").build();
    Node p = Node.builder().label("p").build();

    Graphviz graphviz = Graphviz.digraph()
//        .rankdir(Rankdir.LR)
//        .splines(Splines.POLYLINE)
//        .rankSep(0.2)
//        .nodeSep(0.1)
        .tempNode(Node.builder().shape(NodeShapeEnum.RECT).build())
        .addLine(Line.builder(a, b).tailPort(Port.SOUTH_WEST).dir(Dir.BOTH).build())
        .addLine(Line.builder(a, c).tailPort(Port.SOUTH_WEST).build())
        .addLine(Line.builder(a, d).build())
        .addLine(a, e)
        .addLine(a, g)
        .addLine(a, h)
        .addLine(Line.builder(b, d).tailPort(Port.WEST).build())
        .addLine(
            Line.builder(b, f)
                .tailPort(Port.WEST)
                .headPort(Port.EAST)
                .build()
        )
        .addLine(
            Line.builder(c, f)
                .tailPort(Port.WEST)
                .headPort(Port.WEST)
                .build()
        )
        .addLine(
            Line.builder(e, f)
                .tailPort(Port.WEST)
                .headPort(Port.EAST)
                .build()
        )

        .addLine(
            Line.builder(h, f)
                .tailPort(Port.EAST)
                .headPort(Port.WEST)
                .build()
        )
        .addLine(
            Line.builder(d, f)
                .tailPort(Port.EAST)
                .build()
        )
        .addLine(g, f)

        .startSub()
        .rank(Rank.SAME)
        .tempNode(Node.builder().height(0.05).width(0.05).build())
        .addNode(b, c, d, e, g, h)

        .addLine(
            Line.builder(e, h)
                .tailPort(Port.SOUTH)
                .headPort(Port.NORTH)
                .label("1111\n2222\n3333\n4444")
                .build()
        )
        .addLine(
            Line.builder(e, h)
                .tailPort(Port.SOUTH)
                .headPort(Port.NORTH)
                .build()
        )
        .addLine(
            Line.builder(e, h)
                .tailPort(Port.SOUTH)
                .headPort(Port.NORTH)
                .build()
        )

        .addLine(
            Line.builder(e, g)
                .tailPort(Port.WEST)
                .headPort(Port.EAST)
                .build()
        )
        .addLine(
            Line.builder(e, g)
                .tailPort(Port.WEST)
                .headPort(Port.SOUTH_EAST)
//                .showboxes(true)
                .build()
        )
        .endSub()
//        .splines(Splines.POLYLINE)

        .startSub()
        // Why k and l not in the first rank?
        .rank(Rank.SOURCE)
        .tempNode(Node.builder().shape(NodeShapeEnum.ELLIPSE).build())
        .addLine(Line.builder(k, l).tailPort(Port.NORTH_EAST).headPort(Port.NORTH_WEST).label("k:ne -> l:nw").build())
        .addLine(Line.builder(k, l).tailPort(Port.NORTH_EAST).headPort(Port.NORTH_WEST).build())
        .addLine(Line.builder(k, l).tailPort(Port.EAST).headPort(Port.WEST).label("k:e -> l:w").build())
        .addLine(Line.builder(k, l).tailPort(Port.EAST).headPort(Port.WEST).build())
        .addLine(Line.builder(k, l).tailPort(Port.EAST).headPort(Port.WEST).build())
        .addLine(Line.builder(k, l).tailPort(Port.SOUTH_EAST).headPort(Port.SOUTH_WEST).label("k:se -> l:sw").build())
        .addLine(Line.builder(k, l).tailPort(Port.SOUTH_EAST).headPort(Port.SOUTH_WEST).build())
//
//        .addLine(Line.builder(k, l).tailPort(Port.NORTH_EAST).headPort(Port.NORTH_WEST).label(" ").build())
//        .addLine(Line.builder(k, l).tailPort(Port.NORTH_EAST).headPort(Port.NORTH_WEST).build())
//        .addLine(Line.builder(k, l).tailPort(Port.EAST).headPort(Port.WEST).build())
//        .addLine(Line.builder(k, l).tailPort(Port.EAST).headPort(Port.WEST).build())
//        .addLine(Line.builder(k, l).tailPort(Port.SOUTH_EAST).headPort(Port.SOUTH_WEST).build())
//        .addLine(Line.builder(k, l).tailPort(Port.SOUTH_EAST).headPort(Port.SOUTH_WEST).build())

        .addLine(Line.builder(k, l).tailPort(Port.NORTH_WEST).headPort(Port.NORTH_EAST).showboxes(false).build())
        .addLine(Line.builder(k, l).tailPort(Port.NORTH_WEST).headPort(Port.NORTH_EAST).showboxes(false).build())
        .endSub()

//        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_WEST).headPort(Port.NORTH_WEST).label("1").build())
//        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_WEST).headPort(Port.NORTH_WEST).label("2").build())
//        .addLine(Line.builder(i, j).tailPort(Port.SOUTH).headPort(Port.NORTH).label("3").build())
//        .addLine(Line.builder(i, j).tailPort(Port.SOUTH).headPort(Port.NORTH).label("4").build())
//        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_EAST).headPort(Port.NORTH_EAST).label("5").build())
//        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_EAST).headPort(Port.NORTH_EAST).label("6").build())

        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_WEST).headPort(Port.NORTH_WEST).build())
        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_WEST).headPort(Port.NORTH_WEST).build())
        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_WEST).headPort(Port.NORTH_WEST).build())
        .addLine(Line.builder(i, j).tailPort(Port.SOUTH).headPort(Port.NORTH).build())
        .addLine(Line.builder(i, j).tailPort(Port.SOUTH).headPort(Port.NORTH).build())
        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_EAST).headPort(Port.NORTH_EAST).build())
        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_EAST).headPort(Port.NORTH_EAST).build())
        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_EAST).headPort(Port.NORTH_EAST).build())
        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_EAST).headPort(Port.NORTH_EAST).build())
        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_EAST).headPort(Port.NORTH_EAST).build())

//        .addLine(i, j).addLine(i, j).addLine(i, j).addLine(i, j).addLine(i, j).addLine(i, j)

//        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_WEST).build())
//        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_WEST).build())
//        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_WEST).build())
//        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_WEST).build())
//        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_WEST).build())
//        .addLine(Line.builder(i, j).tailPort(Port.SOUTH_WEST).build())

        .addLine(Line.builder(m, n).tailPort(Port.WEST).headPort(Port.WEST).build())
        .addLine(Line.builder(m, n).tailPort(Port.EAST).headPort(Port.EAST).build())
        .addLine(Line.builder(m, n).tailPort(Port.EAST).headPort(Port.EAST).build())
        .addLine(Line.builder(m, n).tailPort(Port.EAST).headPort(Port.EAST).build())
        .addLine(Line.builder(m, n).tailPort(Port.EAST).headPort(Port.EAST).build())
        .addLine(Line.builder(m, n).tailPort(Port.NORTH).headPort(Port.NORTH).build())
        .addLine(Line.builder(m, n).tailPort(Port.NORTH).headPort(Port.NORTH).build())

        .startSub()
        .rank(Rank.SAME)
        .addNode(f, i, m)
        .addLine(Line.builder(o, p).tailPort(Port.NORTH_EAST).headPort(Port.NORTH_WEST).build())
        .addLine(Line.builder(o, p).tailPort(Port.NORTH_EAST).headPort(Port.NORTH_WEST).build())
        .addLine(Line.builder(o, p).tailPort(Port.EAST).headPort(Port.WEST).build())
        .addLine(Line.builder(o, p).tailPort(Port.EAST).headPort(Port.WEST).build())
        .addLine(Line.builder(o, p).tailPort(Port.SOUTH_EAST).headPort(Port.SOUTH_WEST).build())
        .addLine(Line.builder(o, p).tailPort(Port.SOUTH_EAST).headPort(Port.SOUTH_WEST).build())

        .addLine(Line.builder(o, p).tailPort(Port.WEST).headPort(Port.EAST).build())
        .addLine(Line.builder(o, p).tailPort(Port.WEST).headPort(Port.EAST).build())
        .endSub()
        .build();

    visual(graphviz);
  }

  @Test
  public void testGraphvizBadCase1()  {
    /*
    digraph {
        ranksep=0
        // Puzzling line segment draw.
        a:nw->b:se
        a:nw->b:se

        // Edge does not start from n.
        c:n->d

        {
            rank=same
            // Edge cross node?
            // But in the following line segments of the same type, the port is e to avoid crossing the node.
            e:se->f:ne
        }

        {
            rank=max
            // The port is e to avoid crossing the node, but as above c:n->d, edges does not end from e.
            h:se->i:e
        }

        // Self line g:w->g:e not use port attribute, but if the order of g:w->g:e and g->g is reversed, this will work fine.
        g:w->g:e
        g->g
    }
     */

    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();
    Node g = Node.builder().label("g").build();
    Node h = Node.builder().label("h").build();
    Node i = Node.builder().label("i").build();

    Graphviz graphviz = Graphviz.digraph()
//        .rankSep(0.1)
//        .splines(Splines.SPLINE)
//        .splines(Splines.ROUNDED)
        .addLine(Line.builder(a, b).tailPort(Port.NORTH_WEST).headPort(Port.SOUTH_EAST).radian(5).build())
        .addLine(Line.builder(a, b).tailPort(Port.NORTH_WEST).headPort(Port.SOUTH_EAST).radian(5).build())
        .addLine(Line.builder(c, d).tailPort(Port.NORTH).build())

        .startSub()
        .rank(Rank.SAME)
        .addLine(Line.builder(e, f).tailPort(Port.SOUTH_EAST).headPort(Port.NORTH_EAST).build())
        .endSub()

        .startSub()
        .rank(Rank.MAX)
        .addLine(Line.builder(h, i).tailPort(Port.SOUTH_EAST).headPort(Port.EAST).build())
        .endSub()

        .addLine(Line.builder(g, g).tailPort(Port.WEST).headPort(Port.EAST).build())
        .addLine(g, g)

        .build();

    visual(graphviz);
  }

  @Test
  public void testGraphvizBadCase2()  {
    /*
    digraph G {
        // Parallel edge missing
        a:s->b:n
        a:s->b:n
        a:se->b:ne
        a:se->b:ne
        a:sw->b:nw
        a:sw->b:nw
        a:w->b:w
        a:w->b:w

        c:se->d:nw
        c:se->d:nw
        // If this edge is deleted, the parallel edge is restored
        c:se->d:nw[label="hhhh"]
    }
     */

    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();

    Graphviz graphviz = Graphviz.digraph()
        .addLine(Line.builder(a, b).tailPort(Port.SOUTH).headPort(Port.NORTH).build())
        .addLine(Line.builder(a, b).tailPort(Port.SOUTH).headPort(Port.NORTH).build())
        .addLine(Line.builder(a, b).tailPort(Port.SOUTH_EAST).headPort(Port.NORTH_EAST).build())
        .addLine(Line.builder(a, b).tailPort(Port.SOUTH_EAST).headPort(Port.NORTH_EAST).build())
        .addLine(Line.builder(a, b).tailPort(Port.SOUTH_WEST).headPort(Port.NORTH_WEST).build())
        .addLine(Line.builder(a, b).tailPort(Port.SOUTH_WEST).headPort(Port.NORTH_WEST).build())
        .addLine(Line.builder(a, b).tailPort(Port.WEST).headPort(Port.WEST).build())
        .addLine(Line.builder(a, b).tailPort(Port.WEST).headPort(Port.WEST).build())

        .addLine(Line.builder(c, d).tailPort(Port.SOUTH_EAST).headPort(Port.NORTH_WEST).build())
        .addLine(Line.builder(c, d).tailPort(Port.SOUTH_EAST).headPort(Port.NORTH_WEST).build())
        .addLine(Line.builder(c, d).tailPort(Port.SOUTH_EAST).headPort(Port.NORTH_WEST).label("hhhh").build())
        .build();

    visual(graphviz);
  }
}
