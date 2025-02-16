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
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Rank;
import org.graphper.api.attributes.Rankdir;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SubgraphTest extends GraphvizVisual  {


  @BeforeAll
  public void init() {
    System.setProperty("rankdir", "LR");
  }

  @Test
  public void test1()  {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();
    Node g = Node.builder().label("g").build();

    Graphviz graphviz = Graphviz
        .digraph()
        .addLine(c, d)
        .addLine(b, g)

        .startClus()
        .addLine(f, e)
        .addLine(d, e)
        .addLine(e, g)
        .startSub()
        .rank(Rank.MIN)
        .addNode(g)
        .endSub()
        .endClus()

        .startSub()
        .rank(Rank.SAME)
        .addLine(a, b)
        .addLine(a, c)
        .endSub()

        .build();

    visual(graphviz);
  }

  @Test
  public void testClusterRank()  {
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

    /*
    digraph G {
        a->b
        a->c
        a->g
        c->d
        d->f
        g->f
        c->b
        d->h
        h->e
        b->h
        b->e
        f->h

        d->e
        c->h

        d->i
        h->i
        i->e

        b->j
        g->j

        j->l
        h->l
        e->l

        {
            // rank=sink
            c
        }

        subgraph cluster_A {
            d,e,c,h
            subgraph cluster_B {
                d,h,i
            }
            subgraph cluster_E {
                m->n
            }
            m->h
            n->i
        }

        subgraph cluster_C {
            g,b,f,j
            subgraph cluster_D {
                j->k
                g->k
                {
                    rank=min
                    j
                }
            }
        }
    }
     */
    Graphviz graphviz = Graphviz
        .digraph()
        .scale(6)
//        .splines(Splines.SPLINE)
        .addLine(a, b)
        .addLine(a, c)
        .addLine(a, g)
        .addLine(c, d)
        .addLine(d, f)
        .addLine(g, f)
        .addLine(c, b)
        .addLine(d, h)
        .addLine(h, e)
        .addLine(b, h)
        .addLine(b, e)
        .addLine(f, h)

        .addLine(d, e)
        .addLine(c, h)

        .addLine(d, i)
        .addLine(h, i)
        .addLine(i, e)

        .addLine(b, j)
        .addLine(g, j)

        .addLine(j, l)
        .addLine(h, l)
        .addLine(e, l)

        .subgraph(
            Subgraph.builder()
//                .rank(Rank.SINK)
                .addNode(c)
                .build()
        )

        .cluster(
            Cluster.builder()
                .addNode(d)
                .addNode(e)
                .addNode(c)
                .addNode(h)
                .cluster(
                    Cluster.builder()
                        .addNode(d)
                        .addNode(h)
                        .addNode(i)
                        .build()
                )
                .cluster(
                    Cluster.builder()
                        .addLine(m, n)
                        .build()
                )
                .addLine(m, h)
                .addLine(n, i)
                .build()
        )

        .cluster(
            Cluster.builder()
                .addNode(b)
                .addNode(g)
                .addNode(f)
                .addNode(j)
                .cluster(
                    Cluster.builder()
                        .margin(2, 3)
                        .addLine(j, k)
                        .addLine(g, k)
                        .subgraph(
                            Subgraph.builder()
                                .rank(Rank.MIN)
                                .addNode(j)
                                .build()
                        )
                        .build()
                )
                .build()
        )
        .build();

    visual(graphviz);
  }

  @Test
  public void comparison()  {
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

    Graphviz graphviz = Graphviz.digraph()
        .addLine(c, d, h, i, e)
        .addLine(c, h)
        .addLine(d, i)
        .addLine(d, e)
        .addLine(m, n)
        .addLine(m, h)
        .addLine(n, i)
        .build();

    visual(graphviz);
  }

  @Test
  public void tempGraphTest()  {
    Node n_357863579 = Node.builder().label("357863579").build();
    Node n_2052915500 = Node.builder().label("2052915500").build();
    Node n_940060004 = Node.builder().label("940060004").build();
    Node n_586617651 = Node.builder().label("586617651").build();
    Node n_127618319 = Node.builder().label("127618319").build();
    Node n_114132791 = Node.builder().label("114132791").build();
    Node n_1627960023 = Node.builder().label("1627960023").build();
    Node n_1720435669 = Node.builder().label("1720435669").build();
    Node n_785992331 = Node.builder().label("785992331").build();
    Node n_940553268 = Node.builder().label("940553268").build();
    Node n_1811044090 = Node.builder().label("1811044090").build();
    Node n_566034357 = Node.builder().label("566034357").build();
    Node n_1789550256 = Node.builder().label("1789550256").build();
    Node n_234698513 = Node.builder().label("234698513").build();
    Node n_1068934215 = Node.builder().label("1068934215").build();
    Node n_1798286609 = Node.builder().label("1798286609").build();
    Node n_328638398 = Node.builder().label("328638398").build();
    Node n_2036958521 = Node.builder().label("2036958521").build();
    Node n_3447021 = Node.builder().label("3447021").build();
    Node n_1020923989 = Node.builder().label("1020923989").build();
    Node n_1032616650 = Node.builder().label("1032616650").build();
    Node n_99347477 = Node.builder().label("99347477").build();
    Node n_1945604815 = Node.builder().label("1945604815").build();
    Node n_440434003 = Node.builder().label("440434003").build();

    Graphviz graphviz = Graphviz
        .digraph()
        .scale(5)
        .label("Temp Graph Temp Graph Temp Graph Temp Graph Temp Graph Temp Graph Temp Graph Temp Graph Temp Graph Temp Graph Temp Graph\nTemp Graph\nTemp Graph\nTemp Graph\nTemp Graph")
        .labeljust(Labeljust.LEFT)
        .addLine(n_1945604815, n_1720435669)
        .addLine(n_1720435669, n_1020923989)
        .addLine(n_1032616650, n_99347477)
        .addLine(n_114132791, n_3447021)
        .addLine(n_1720435669, n_1798286609)
        .addLine(n_114132791, n_1032616650)
        .addLine(n_940060004, n_234698513)
        .addLine(n_127618319, n_1789550256)
        .addLine(n_114132791, n_586617651)
        .addLine(n_328638398, n_1789550256)
        .addLine(n_1627960023, n_785992331)
        .addLine(n_1627960023, n_1945604815)
        .addLine(n_785992331, n_940060004)
        .addLine(n_1020923989, n_1068934215)
        .addLine(n_99347477, n_1789550256)
        .addLine(n_234698513, n_2052915500)
        .addLine(n_2036958521, n_328638398)
        .addLine(n_1627960023, n_357863579)
        .addLine(n_3447021, n_440434003)
        .addLine(n_566034357, n_940553268)
        .addLine(n_1720435669, n_114132791)
        .addLine(n_940553268, n_440434003)
        .addLine(n_357863579, n_1811044090)
        .addLine(n_1811044090, n_114132791)
        .addLine(n_586617651, n_328638398)
        .addLine(n_1811044090, n_566034357)
        .addLine(n_357863579, n_1720435669)
        .addLine(n_2052915500, n_440434003)
        .addLine(n_1068934215, n_127618319)
        .addLine(n_1020923989, n_2052915500)
        .addLine(n_1798286609, n_2036958521)
        .build();

    visual(graphviz);
  }

  @Test
  public void testClusterMinCross()  {
    /*
 digraph {
     label="GRAPHVIZ DEMO"
     1->4
     1->2[label="1-->2"]
     1->3
     4->6
     2->5
     2->8
     3->6
     3->8
     5->7[ltail="cluster_2",lhead="cluster_3"]
     6->7
     8->7
     2->9
     2->10
     12->6[lhead="cluster_1"]
     12->11
     7->13
     3->9

     subgraph cluster_1 {
         2,3,4,5,6
         6->11
         fontsize=28
         labeljust=l
         labelloc=b
         label="Nice to meet you!
How old are you?
I'm fine thank you, and you?
Me too!
Where are you from?"

         subgraph cluster_2 {
             3,5,6
             {rank=same 3->5[label="3->5"]}
             label="1111111111111111111111111
22222222222222222
3333333333333
4444444444444444
555555
66666
77777
888888
9999999"
fontsize=36
labelloc=b
         }
     }
     subgraph cluster_3 {
        7,8,9
        label="Hello World|Hello World|Hello World
Hello World
Hello World
Hello World
Hello World
Hello World
Hello World"
     }
 }
     */

    Node n1 = Node.builder().label("1").build();
    Node n2 = Node.builder().label("2").build();
    Node n3 = Node.builder().label("33333333333333333333333333333333333333333\n"
                                       + "333333333333333333333333333333\n3333333333333333333333333").build();
    Node n4 = Node.builder().label("4").build();
    Node n5 = Node.builder().label("5").build();
    Node n6 = Node.builder().label("6").build();
    Node n7 = Node.builder().label("7").build();
    Node n8 = Node.builder().label("8").build();
    Node n9 = Node.builder().label("9").build();
    Node n10 = Node.builder().label("10").build();
    Node n11 = Node.builder().label("11").build();
    Node n12 = Node.builder().label("12").build();
    Node n13 = Node.builder().label("13").build();

    Graphviz graphviz = Graphviz.digraph()
        .scale(6)
        .tempNode(Node.builder().shape(NodeShapeEnum.TRIANGLE).build())
        .rankdir(Rankdir.TB)
//        .splines(Splines.SPLINE)
        .addLine(n1, n4)
        .addLine(Line.builder(n1, n2).label("1 --> 2").build())
//        .labeljust(Labeljust.LEFT)
//        .labelloc(Labelloc.TOP)
        .label("GRAPHVIZ DEMO")
        .addLine(n1, n2)
        .addLine(n1, n3)
        .addLine(n4, n6)
        .addLine(n2, n5)
        .addLine(n2, n8)
        .addLine(n3, n6)
        .addLine(n3, n8)
        .addLine(Line.builder(n5, n7).ltail("cluster_2").lhead("cluster_3").build())
//        .addLine(n5, n7)
        .addLine(Line.builder(n6, n7).build())
        .addLine(n8, n7)
        .addLine(n2, n9)
        .addLine(n2, n10)
        .addLine(Line.builder(n12, n6).build())
        .addLine(n12, n11)
        .addLine(n7, n13)
        .addLine(n3, n9)
        .cluster(
            Cluster.builder()
                .id("cluster_1")
                .labeljust(Labeljust.LEFT)
                .labelloc(Labelloc.BOTTOM)
                .label("cluster_1 Nice to meet you!\nHow old are you?\nI'm fine thank you, and you?\nMe too!\nWhere are you from?")
                .fontSize(24)
                .addNode(n2)
                .addNode(n3)
                .addNode(n4)
                .addNode(n5)
                .addNode(n6)
                .addLine(n6, n11)
                .cluster(
                    Cluster.builder()
                        .id("cluster_2")
                        .labelloc(Labelloc.BOTTOM)
                        .margin(1, 2)
                        .label("cluster_2 1111111111111111111111111\n22222222222222222\n3333333333333\n4444444444444444\n555555\n66666\n77777\n888888\n9999999")
                        .fontSize(36)
                        .addNode(n3)
                        .addNode(n5)
                        .addNode(n6)

                        .startSub()
                        .rank(Rank.SAME)
                        .addLine(Line.builder(n3, n5).label("3 --> 5").build())
                        .endSub()

                        .build()
                )
                .build()
        )
        .cluster(
            Cluster.builder()
                .id("cluster_3")
                .labeljust(Labeljust.RIGHT)
                .label("cluster_3 Hello World|Hello World|Hello World\nHello World\nHello World\nHello World\nHello World\nHello World\nHello World")
                .addNode(n7)
                .addNode(n8)
                .addNode(n9)
                .build()
        )
        .build();

    visual(graphviz);
  }

  @Test
  public void testComplexCluster()  {
    /*
    digraph world_dynamics {
       size="6,6";
       S8 -> 9;
       S24 -> 27;
       S24 -> 25;
       S1 -> 10;
       S1 -> 2;
       S35 -> 36;
       S35 -> 43;
       S30 -> 31;
       S30 -> 33;
       9 -> 42;
       9 -> T1;
       25 -> T1;
       25 -> 26;
       27 -> T24;
       2 -> 3;
       2 -> 16;

       2 -> 18;
       10 -> 11;
       10 -> 14;
       10 -> T1;

       31 -> 32;
       33 -> T30;
       33 -> 34;
       42 -> 4;
       26 -> 4;
       3 -> 4;

       38 -> 4;
       40 -> 19;
       4 -> 5;
       19 -> 21;

       6 -> 7;
       15 -> T1;
       22 -> 23;
       22 -> T35;

       23 -> T1;

       subgraph cluster_A {
           11 -> 4;
           14 -> 15;
           37 -> 39;
           37 -> 41;
           37 -> 38;
           37 -> 40;
           13 -> 19;
           12 -> 29;
           43 -> 38;
           43 -> 40;
           36 -> 19;
           32 -> 23;
           34 -> 29;
           39 -> 15;
           41 -> 29;
       }
       subgraph cluster_B {
           19 -> 20;
           19 -> 28;
           5 -> 6;
           5 -> T35;
           5 -> 23;
           21 -> 22;
           20 -> 15;
           28 -> 29;
       }

       subgraph cluster_C {
           2 -> 17;
           2 -> T1;
       }
       subgraph cluster_D {
           10 -> 13;
           10 -> 12;
           31 -> T1;
       }
       subgraph cluster_E {
           16 -> 15;
           17 -> 19;
           18 -> 29;
           29 -> T30;
           7 -> T8;
           23 -> T24;
       }
     }
     */
    Node n_10 = Node.builder().label("10").build();
    Node n_11 = Node.builder().label("11").build();
    Node n_12 = Node.builder().label("12").build();
    Node n_13 = Node.builder().label("13").build();
    Node n_14 = Node.builder().label("14").build();
    Node n_15 = Node.builder().label("15").build();
    Node n_16 = Node.builder().label("16").build();
    Node n_17 = Node.builder().label("17").build();
    Node n_18 = Node.builder().label("18").build();
    Node n_19 = Node.builder().label("19").build();
    Node n_2 = Node.builder().label("2").build();
    Node n_3 = Node.builder().label("3").build();
    Node n_4 = Node.builder().label("4").build();
    Node n_5 = Node.builder().label("5").build();
    Node n_6 = Node.builder().label("6").build();
    Node n_7 = Node.builder().label("7").build();
    Node n_9 = Node.builder().label("9").build();
    Node n_S1 = Node.builder().label("S1").build();
    Node n_20 = Node.builder().label("20").build();
    Node n_21 = Node.builder().label("21").build();
    Node n_22 = Node.builder().label("22").build();
    Node n_23 = Node.builder().label("23").build();
    Node n_25 = Node.builder().label("25").build();
    Node n_26 = Node.builder().label("26").build();
    Node n_S8 = Node.builder().label("S8").build();
    Node n_27 = Node.builder().label("27").build();
    Node n_28 = Node.builder().label("28").build();
    Node n_29 = Node.builder().label("29").build();
    Node n_T1 = Node.builder().label("T1").build();
    Node n_31 = Node.builder().label("31").build();
    Node n_32 = Node.builder().label("32").build();
    Node n_33 = Node.builder().label("33").build();
    Node n_34 = Node.builder().label("34").build();
    Node n_36 = Node.builder().label("36").build();
    Node n_37 = Node.builder().label("37").build();
    Node n_T8 = Node.builder().label("T8").build();
    Node n_38 = Node.builder().label("38").build();
    Node n_39 = Node.builder().label("39").build();
    Node n_40 = Node.builder().label("40").build();
    Node n_41 = Node.builder().label("41").build();
    Node n_42 = Node.builder().label("42").build();
    Node n_43 = Node.builder().label("43").build();
    Node n_T24 = Node.builder().label("T24").build();
    Node n_T30 = Node.builder().label("T30").build();
    Node n_T35 = Node.builder().label("T35").build();
    Node n_S24 = Node.builder().label("S24").build();
    Node n_S30 = Node.builder().label("S30").build();
    Node n_S35 = Node.builder().label("S35").build();


    Graphviz graphviz = Graphviz
        .digraph()
        .scale(4)
//        .splines(Splines.SPLINE)
        .addNode(
            Node.builder()
                .label("ds")
                .fontSize(36)
                .build()
        )
        .addLine(Line.builder(n_S8, n_9).build())
        .addLine(Line.builder(n_S24, n_27).build())
        .addLine(Line.builder(n_S24, n_25).build())
        .addLine(Line.builder(n_S1, n_10).build())
        .addLine(Line.builder(n_S1, n_2).build())
        .addLine(Line.builder(n_S35, n_36).build())
        .addLine(Line.builder(n_S35, n_43).build())
        .addLine(Line.builder(n_S30, n_31).build())
        .addLine(Line.builder(n_S30, n_33).build())
        .addLine(Line.builder(n_9, n_42).build())
        .addLine(Line.builder(n_9, n_T1).build())
        .addLine(Line.builder(n_25, n_T1).build())
        .addLine(Line.builder(n_25, n_26).build())
        .addLine(Line.builder(n_27, n_T24).build())
        .addLine(Line.builder(n_2, n_3).build())
        .addLine(Line.builder(n_2, n_16).build())

        .addLine(Line.builder(n_2, n_18).build())
        .addLine(Line.builder(n_10, n_11).build())
        .addLine(Line.builder(n_10, n_14).build())
        .addLine(Line.builder(n_10, n_T1).build())

        .addLine(Line.builder(n_31, n_32).build())
        .addLine(Line.builder(n_33, n_T30).build())
        .addLine(Line.builder(n_33, n_34).build())
        .addLine(Line.builder(n_42, n_4).build())
        .addLine(Line.builder(n_26, n_4).build())
        .addLine(Line.builder(n_3, n_4).build())

        .addLine(Line.builder(n_38, n_4).build())
        .addLine(Line.builder(n_40, n_19).build())
        .addLine(Line.builder(n_4, n_5).build())
        .addLine(Line.builder(n_19, n_21).build())

        .addLine(Line.builder(n_6, n_7).build())
        .addLine(Line.builder(n_15, n_T1).build())
        .addLine(Line.builder(n_22, n_23).build())
        .addLine(Line.builder(n_22, n_T35).build())

        .addLine(Line.builder(n_23, n_T1).build())

        .cluster(
            Cluster.builder()
                .label("HELLO WORLD HELLO WORLD HELLO WORLD HELLO WORLD HELLO WORLD\n"
                           + "HELLO WORLD HELLO WORLD HELLO WORLD HELLO WORLD\n"
                           + "HELLO WORLD HELLO WORLD HELLO WORLD HELLO WORLD\n"
                           + "HELLO WORLD HELLO WORLD HELLO WORLD HELLO WORLD\n"
                           + "HELLO WORLD HELLO WORLD HELLO WORLD HELLO WORLD\n"
                           + "HELLO WORLD HELLO WORLD HELLO WORLD HELLO WORLD\n"
                )
                .fontSize(35)
                .margin(2)
                .addLine(Line.builder(n_11, n_4).build())
                .addLine(Line.builder(n_14, n_15).build())
                .addLine(Line.builder(n_37, n_39).build())
                .addLine(Line.builder(n_37, n_41).build())
                .addLine(Line.builder(n_37, n_38).build())
                .addLine(Line.builder(n_37, n_40).build())
                .addLine(Line.builder(n_13, n_19).build())
                .addLine(Line.builder(n_12, n_29).build())
                .addLine(Line.builder(n_43, n_38).build())
                .addLine(Line.builder(n_43, n_40).build())
                .addLine(Line.builder(n_36, n_19).build())
                .addLine(Line.builder(n_32, n_23).build())
                .addLine(Line.builder(n_34, n_29).build())
                .addLine(Line.builder(n_39, n_15).build())
                .addLine(Line.builder(n_41, n_29).build())
                .build()
        )
        .cluster(
            Cluster.builder()
                .addLine(Line.builder(n_19, n_20).build())
                .addLine(Line.builder(n_19, n_28).build())
                .addLine(Line.builder(n_5, n_6).build())
                .addLine(Line.builder(n_5, n_T35).build())
                .addLine(Line.builder(n_5, n_23).build())
                .addLine(Line.builder(n_21, n_22).build())
                .addLine(Line.builder(n_20, n_15).build())
                .addLine(Line.builder(n_28, n_29).build())
                .build()
        )
        .cluster(
            Cluster.builder()
                .addLine(Line.builder(n_2, n_17).build())
                .addLine(Line.builder(n_2, n_T1).build())
                .build()
        )
        .cluster(
            Cluster.builder()
                .addLine(Line.builder(n_10, n_13).build())
                .addLine(Line.builder(n_10, n_12).build())
                .addLine(Line.builder(n_31, n_T1).build())
                .build()
        )
        .cluster(
            Cluster.builder()
                .addLine(Line.builder(n_16, n_15).build())
                .addLine(Line.builder(n_17, n_19).build())
                .addLine(Line.builder(n_18, n_29).build())
                .addLine(Line.builder(n_29, n_T30).build())
                .addLine(Line.builder(n_7, n_T8).build())
                .addLine(Line.builder(n_23, n_T24).build())
                .build()
        )
        .build();

    visual(graphviz);
  }

  @Test
  public void testSimplyCluster()  {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();

    Graphviz graphviz = Graphviz.digraph()
//        .splines(Splines.NONE)
        .addLine(a, b, c)
        .addLine(a, c)
        .addLine(a, e)
        .addLine(e, c)

        .startClus()
        .addNode(b)
        .addLine(d, c)
        .endClus()

        .startClus()
        .addLine(e, f)
        .endClus()
        .build();

    visual(graphviz);
  }

  @Test
  public void testClusterClassicGraph()  {
    Node start = Node.builder().label("start").build();
    Node end = Node.builder().label("end").build();
    Node a0 = Node.builder().label("a0").build();
    Node a1 = Node.builder().label("a1").build();
    Node a2 = Node.builder().label("a2").build();
    Node a3 = Node.builder().label("a3").build();
    Node b0 = Node.builder().label("b0").build();
    Node b1 = Node.builder().label("b1").build();
    Node b2 = Node.builder().label("b2").build();
    Node b3 = Node.builder().label("b3").build();

    GraphvizBuilder builder  = Graphviz.digraph()
        .startClus()
        .label("process #1")
        .addLine(a0, a1, a2, a3)
        .endClus()

        .startClus()
        .startClus()
        .color(Color.GREEN)
        .startClus()
        .startClus()
        .color(Color.RED)
        .bgColor(Color.ORANGE)
        .startClus()
        .startClus()
        .startClus()
        .startClus()
        .labelloc(Labelloc.BOTTOM)
        .label("process #2")
        .addLine(b0, b1, b2, b3)
        .endClus()
        .endClus()
        .endClus()
        .endClus()
        .endClus()
        .endClus()
        .endClus()
        .endClus()

        .addLine(start, a0)
        .addLine(start, b0)
        .addLine(a1, b3)
        .addLine(b2, a3)
        .addLine(Line.builder(a3, a0).controlPoints(true).build())
        .addLine(a3, end)
        .addLine(b3, end);

    visual(builder.build());
    visual(builder.tempNode(Node.builder().shape(NodeShapeEnum.CIRCLE).build()).build());
    visual(builder.tempNode(Node.builder().shape(NodeShapeEnum.PLAIN).build()).build());
    visual(builder.tempNode(Node.builder().shape(NodeShapeEnum.PLAINTEXT).build()).build());
    visual(builder.tempNode(Node.builder().shape(NodeShapeEnum.RECT).build()).build());
    visual(builder.tempNode(Node.builder().shape(NodeShapeEnum.TRIANGLE).build()).build());
    visual(builder.tempNode(Node.builder().shape(NodeShapeEnum.DIAMOND).build()).build());
  }

  @Test
  public void testScrambleNode()  {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();

    Graphviz graphviz = Graphviz.digraph()
        .addLine(a, d)
        .addLine(a, b)
        .addLine(a, c)
        .cluster(
            Cluster.builder()
                .id("cluster_0")
                .addNode(a)
                .addNode(b)
                .build()
        )
        .cluster(
            Cluster.builder()
                .id("cluster_1")
                .addNode(a)
                .addNode(c)
                .build()
        )
        .build();

    visual(graphviz);
  }
}
