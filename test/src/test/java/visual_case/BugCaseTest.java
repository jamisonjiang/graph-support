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

import static org.graphper.api.Html.table;
import static org.graphper.api.Html.td;

import helper.GraphvizVisual;
import org.graphper.api.Cluster;
import org.graphper.api.FloatLabel;
import org.graphper.api.Graphviz;
import org.graphper.api.Html;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Rank;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Splines;
import org.graphper.api.attributes.Tend;
import org.junit.jupiter.api.Test;

public class BugCaseTest extends GraphvizVisual {

  @Test
  public void wrongFlatEdgeLabelIn_1_0_1() {
    Node sh0045 = Node.builder().label("sh0045").build();
    Node sh0020 = Node.builder().label("sh0020").build();
    Node sh0006 = Node.builder()
        .shape(NodeShapeEnum.RECT)
        .label("sh0006")
        .width(1.25)
        .height(0.661241)
        .color(Color.ofRGB("#000006"))
        .build();
    Node sh0007 = Node.builder()
        .shape(NodeShapeEnum.RECT)
        .label("sh0007")
        .width(1.25)
        .height(0.661241)
        .color(Color.ofRGB("#000007"))
        .build();
    Node sh0008 = Node.builder()
        .shape(NodeShapeEnum.RECT)
        .label("sh0008")
        .width(1.25)
        .height(0.661241)
        .color(Color.ofRGB("#000008"))
        .build();
    Node sh0042 = Node.builder().label("sh0042").build();
    Node sh0016 = Node.builder().label("sh0016").build();
    Node sh0013 = Node.builder().label("sh0013").build();
    Node sh0033 = Node.builder().label("sh0033").build();

    Graphviz graphviz = Graphviz.digraph()
        .addLine(Line.builder(sh0045, sh0020)
                     .arrowTail(ArrowShape.NONE)
                     .arrowHead(ArrowShape.NONE)
                     .color(Color.ofRGB("#0000CC"))
                     .build())
        .startClus()
        .startClus()
        .color(Color.ofRGB("#000004"))
        .label("sssssssssssssssssssssssssssssss")
        .startClus()
        .addNode(sh0006, sh0007, sh0008)
        .endClus()
        .endClus()
        .endClus()

        .tempLine(Line.tempLine().arrowTail(ArrowShape.NONE).minlen(1).build())
        .addLine(Line.builder(sh0042, sh0020)
                     .color(Color.ofRGB("#000044"))
                     .label("11111111111111111110")
                     .build())
        .addLine(Line.builder(sh0016, sh0008)
                     .color(Color.ofRGB("#000048"))
                     .label("11111111111111111110")
                     .build())
        .addLine(Line.builder(sh0016, sh0020)
                     .color(Color.ofRGB("#00004C"))
                     .label("22222222222222222222")
                     .build())
        .addLine(Line.builder(sh0016, sh0033)
                     .color(Color.ofRGB("#000054"))
                     .label("3333333333333333333")
                     .build())
        .addLine(Line.builder(sh0008, sh0013)
                     .color(Color.ofRGB("#000058"))
                     .label("44444444444444444444")
                     .build())
        .addLine(Line.builder(sh0020, sh0008)
                     .color(Color.ofRGB("#000060"))
                     .label("555555555555555555555555")
                     .build())
        .addLine(Line.builder(sh0007, sh0016)
                     .color(Color.ofRGB("#000044"))
                     .label("66666666666666666666666")
                     .build())
        .build();

    visual(graphviz);
  }

  @Test
  public void testAlignNodePos() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();
    Node g = Node.builder().label("g").build();

    Graphviz graphviz = Graphviz.digraph()
        .addLine(a, b)
        .addLine(g, b)
        .addLine(b, c)
        .addLine(b, d)
        .addLine(c, e)
        .addLine(d, e, f)
        .build();

    visual(graphviz);
  }

  @Test
  public void testAlignNodePos2() {
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
  public void testTailHeadLabel() {
    Node a = Node.builder().shape(NodeShapeEnum.RECORD)
        .label("|center|").build();
    Node b = Node.builder().label("left").build();
    Node c = Node.builder().label("right").build();

    Graphviz graphviz = Graphviz.digraph()
        .splines(Splines.ORTHO)
        .nodeSep(3)
        .tempNode(Node.builder().shape(NodeShapeEnum.RECT).build())
        .addLine(Line.builder(a, b)
                     .floatLabels(
                         FloatLabel.builder()
                             .tend(Tend.TAIL)
                             .lengthRatio(0)
                             .table(Html.table().tr(Html.td().height(20).width(40).bgColor(Color.BLACK)))
                             .build(),
                         FloatLabel.builder()
                             .tend(Tend.HEAD)
                             .lengthRatio(1)
                             .table(Html.table().tr(Html.td().height(20).width(20).bgColor(Color.BLACK)))
                             .build()
                     )
                     .build())
        .addLine(Line.builder(a, c)
                     .floatLabels(
                         FloatLabel.builder()
                             .tend(Tend.TAIL)
                             .lengthRatio(0)
                             .table(Html.table().tr(Html.td().height(20).width(20).bgColor(Color.BLACK)))
                             .build(),
                         FloatLabel.builder()
                             .tend(Tend.HEAD)
                             .lengthRatio(1)
                             .table(Html.table().tr(Html.td().height(20).width(20).bgColor(Color.BLACK)))
                             .build()
                     )
                     .build())
        .build();

    visual(graphviz);
  }

  @Test
  public void testWrongCluster() {
    Node sh0010 = Node.builder().label("sh0010")
        .width(1.380631)
        .height(0.459156)
        .build();
    Node sh0011 = Node.builder().label("sh0011")
        .width(0.849366)
        .height(0.458888)
        .build();
    Node sh0012 = Node.builder().label("sh0012")
        .shape(NodeShapeEnum.RECT)
        .width(0.805556)
        .height(1.098891)
        .build();
    Node sh0013 = Node.builder().label("sh0013")
        .shape(NodeShapeEnum.RECT)
        .width(0.416667)
        .height(1.098891)
        .build();
    Node zacl0004 = Node.builder().shape(NodeShapeEnum.POINT).width(.01).height(.01).margin(0)
        .build();

    Graphviz graphviz = Graphviz.digraph()
        .nodeSep(0.486111)
        .rankSep(0.833333)
        .rankdir(Rankdir.LR)
        .addLine(Line.builder(zacl0004, sh0010).minlen(0)
                     .table(Html.table().fixedSize(true).width(143).height(19).tr(Html.td()))
                     .build())
        .addLine(
            Line.builder(sh0011, zacl0004).arrowTail(ArrowShape.NONE).arrowHead(ArrowShape.NONE)
                .table(Html.table().fixedSize(true).width(248).height(19).tr(Html.td())).minlen(0)
                .build())
        .addLine(
            Line.builder(sh0011, zacl0004).arrowTail(ArrowShape.NONE).arrowHead(ArrowShape.NONE)
                .table(Html.table().fixedSize(true).width(48).height(19).tr(Html.td())).minlen(0)
                .build())
        .addLine(
            Line.builder(sh0011, zacl0004).arrowTail(ArrowShape.NONE).arrowHead(ArrowShape.NONE)
                .table(Html.table().fixedSize(true).width(248).height(19).tr(Html.td())).minlen(0)
                .build())
        .addNode(sh0012, sh0013)
        .cluster(
            Cluster.builder()
                .id("6pa")
                .addNode(zacl0004)
                .cluster(
                    Cluster.builder()
                        .id("6p0")
                        .label("670")
                        .fontName("Elephant")
                        .cluster(
                            Cluster.builder()
                                .id("6")
                                .table(
                                    Html.table().fixedSize(true).width(65).height(14).tr(Html.td())
                                )
                                .cluster(
                                    Cluster.builder()
                                        .id("6i")
                                        .cluster(
                                            Cluster.builder()
                                                .id("6p1")
                                                .addNode(sh0010, sh0011)
                                                .build()
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build()
        )
        .addLine(Line.builder(sh0012, zacl0004).arrowTail(ArrowShape.NONE)
                     .arrowHead(ArrowShape.NONE).build())
        .addLine(Line.builder(zacl0004, sh0013).arrowTail(ArrowShape.NONE)
                     .arrowHead(ArrowShape.NONE).build())
        .build();

    visual(graphviz);
  }

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
        .label("tail")
        .build();

    Graphviz graphviz = Graphviz.digraph()
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
  public void testFlatPortLine() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();

    Graphviz graphviz = Graphviz.digraph()
        .startSub()
        .rank(Rank.SAME)
        .addLine(Line.builder(a, b).label("Line 1")
                     .tailPort(Port.WEST).headPort(Port.EAST).build())
        .addLine(Line.builder(a, b).label("Line 2")
                     .tailPort(Port.WEST).headPort(Port.EAST).build())
        .addLine(Line.builder(a, b).label("Line 3")
                     .tailPort(Port.EAST).headPort(Port.WEST).build())
        .addLine(Line.builder(a, b).label("Line 4")
                     .tailPort(Port.EAST).headPort(Port.WEST).build())
        .addLine(Line.builder(a, b).label("Line 5")
                     .tailPort(Port.SOUTH_EAST).headPort(Port.SOUTH_WEST).build())
        .addLine(Line.builder(a, b).label("Line 6")
                     .tailPort(Port.SOUTH_EAST).headPort(Port.SOUTH_WEST).build())
        .endSub()
        .build();

    visual(graphviz);
  }

  @Test
  public void testEscapeXmlCharacter() {
    Graphviz graphviz = Graphviz.digraph()
        .addNode(Node.builder().label("ü").build())
        .addNode(Node.builder().label("€").build())
        .addNode(Node.builder().id("¢").label("¢").build())
        .addNode(Node.builder().label("&uuml;").build())
        .addNode(Node.builder().label("椭圆").build())
        .addNode(Node.builder().label("<table><tr><td>cell</td></tr></table>").build())
        .addNode(Node.builder()
                     .label("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                                + "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"200\">\n</svg>")
                     .build())
        .build();
    visual(graphviz);
  }

  @Test
  public void testFontMeasure() {
//    String fontName = "DejaVu Sans Mono";
//    String fontName = "Mistral";
//    String fontName = "SansSerif";
    String fontName = "Times New Roman";
    Html.Table table = Html.table()
        .border( 1 )
        .cellBorder( 0 )
        .cellSpacing( 0 )
        .cellPadding( 0 )
        .tr(Html.td().text("Test test test" ).fontName(fontName ).align(Labeljust.CENTER ) )
        .tr(Html.td().text("Another test test" ).fontName(fontName ).align(Labeljust.CENTER ) )
        .tr(Html.td().cellPadding(1 ).bgColor(Color.BLACK ).height(1 ).colSpan(2) )
        .tr(Html.td().text("someProperty: Test test123" ).fontName(fontName ).align(Labeljust.RIGHT) )
        .tr(Html.td().text("description: This is a test description" ).fontName(fontName ).align(Labeljust.LEFT) )
        .tr(Html.td().text("anotherProperty: Another12" ).fontName(fontName ).align(Labeljust.LEFT ) )
        .tr(Html.td().text("see: http://example.com/" ).fontName(fontName ).align(Labeljust.LEFT ) )
        .tr(Html.td().text("yetAnother: yetAnotherTest" ).fontName(fontName ).align(Labeljust.LEFT ) )
        ;
    Node node = Node.builder().color( Color.BLACK ).table( table ).build();

    visual(Graphviz.graph().addNode(node).build());
  }

  @Test
  public void testParallelCell() {
    Node a = Node.builder().label("<2>2|<3>3|<4>4|<5>5|<6>6|<7>7|").build();
    Node b = Node.builder().label("<0>0|<1>1|<8>8|<9>9|<10>10|<11>11|").build();
    Node c = Node.builder().label("<0>X|<1>X|<2>X|<3>X|<4>X|<5>X|<6>X|<7>X|<8>X|<9>X|<10>X|<11>X|<12>.|<13>.|<14>.|<15>.|<16>.|<17>.|<18>.|<19>.|<20>.|<21>.|<22>.|<23>.|<24>.|<25>.|<26>.|<27>.|<28>.|<29>.|<30>.|<31>.|").build();

    Graphviz graphviz = Graphviz.digraph()
        .rankdir(Rankdir.RL)
        .rankSep(1.5)
        .tempNode(Node.builder().shape(NodeShapeEnum.RECORD).build())
        .startSub()
        .rank(Rank.SOURCE)
        .addNode(a)
        .endSub()
        .startSub()
        .rank(Rank.SINK)
        .addNode(b)
        .endSub()
        .addLine(Line.builder(a, c).tailCell("2").headCell("2").build())
        .addLine(Line.builder(a, c).tailCell("3").headCell("3").build())
        .addLine(Line.builder(a, c).tailCell("4").headCell("4").build())
        .addLine(Line.builder(a, c).tailCell("5").headCell("5").build())
        .addLine(Line.builder(a, c).tailCell("6").headCell("6").build())
        .addLine(Line.builder(a, c).tailCell("7").headCell("7").build())

        .addLine(Line.builder(b, c).tailCell("0").headCell("0").headPort(Port.EAST).build())
        .addLine(Line.builder(b, c).tailCell("1").headCell("1").build())
        .addLine(Line.builder(b, c).tailCell("8").headCell("8").build())
        .addLine(Line.builder(b, c).tailCell("9").headCell("9").build())
        .addLine(Line.builder(b, c).tailCell("10").headCell("10").build())
        .addLine(Line.builder(b, c).tailCell("11").headCell("11").build())
        .build();
    visual(graphviz);
  }

  @Test
  public void wrongCellAlign() {
    Graphviz graphviz = Graphviz.digraph()
        .tempNode(Node.builder().shape(NodeShapeEnum.RECORD).build())
        .addNode(Node.builder().label("{hello|{|sssskkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk}|{fffffffffffffffff|}|{||||}|{|{|||}||}|{|}}").build())
        .addNode(Node.builder().label("{hello|{|{||{||{|}}}}|{|sssskkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk}|{fffffffffffffffff|}|{||||}||}").build())
        .build();

    visual(graphviz);
  }

  @Test
  public void newLineMeasureTextWrong() {
    Node n = Node.builder()
        .shape(NodeShapeEnum.RECT)
        .label("HELLO WORLD\n"
                   + "HELLO WORLD HELLO WORLD HELLO WORLD HELLO WORLD\n"
                   + "HELLO WORLD HELLO WORLD HELLO WORLD HELLO WORLD\n"
                   + "HELLO WORLD HELLO WORLD HELLO WORLD HELLO WORLD\n"
                   + "HELLO WORLD HELLO WORLD HELLO WORLD HELLO WORLD\n"
                   + "HELLO WORLD HELLO WORLD HELLO WORLD HELLO WORLD\n"
        )
        .build();

    visual(Graphviz.digraph().addNode(n).build());
  }

  @Test
  public void wrongLabelloc() {
    Node A = Node.builder().label("A").build();
    Node B = Node.builder().label("B").build();
    Node C = Node.builder().label("C").build();
    Node AA = Node.builder().label("AA").build();
    Node BB = Node.builder().label("BB").build();
    Node CC = Node.builder().label("CC").build();

    Graphviz graphviz = Graphviz.digraph()
        .rankdir(Rankdir.BT)
        .addLine(BB, C)
        .cluster(
            Cluster.builder()
                .label("Cluster A")
                .labelloc(Labelloc.BOTTOM)
                .addLine(C, A)
                .addLine(C, B)
                .build()
        )
        .cluster(
            Cluster.builder()
                .label("Cluster B")
                .labelloc(Labelloc.BOTTOM)
                .addLine(CC, AA)
                .addLine(CC, BB)
                .build()
        )
        .build();

    visual(graphviz);
  }

  @Test
  public void clusterOverlap() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node f = Node.builder().label("f").build();
    Node l = Node.builder().label("l").build();
    Node aa = Node.builder().label("aa").build();
    Node bb = Node.builder().label("bb").build();
    Node cc = Node.builder().label("cc").build();
    Node ss = Node.builder().label("ss").build();
    Node tt = Node.builder().label("tt").build();
    Node gg = Node.builder().label("gg").build();
    Node pp = Node.builder().label("pp").build();
    Node ff = Node.builder().label("ff").build();
    Node _12 = Node.builder().label("12").build();
    Node _45 = Node.builder().label("45").build();
    Node _34 = Node.builder().label("34").build();
    Node _98 = Node.builder().label("98").build();

    Graphviz graphviz = Graphviz.digraph()
        .addLine(c, bb)
        .addLine(b, f)
        .addLine(f, gg)
        .addLine(f, ss)
        .addLine(b, d)
        .addLine(c, d)
        .addLine(aa, d)
        .addLine(l, aa)

        .startClus()
        .label("Cluster A")
        .addLine(a, c)
        .addLine(b, c)
        .endClus()

        .startClus()
        .label("Cluster B")
        .addLine(aa, cc)
        .addLine(bb, cc)
        .endClus()

        .startClus()
        .label("Cluster C")
        .addLine(ss, tt)
        .addLine(gg, pp)
        .addLine(d, f)
        .addLine(l, ff)
        .addLine(ff, ss)
        .endClus()

        .startClus()
        .label("Cluster D")
        .addLine(_12, _45)
        .addLine(_34, _98)
        .addLine(_34, _45)
        .addLine(_45, _98)
        .endClus()

        .build();

    visual(graphviz);
  }

  @Test
  public void testMultiFontName() {
    visual(Graphviz.digraph()
               .addNode(Node.builder()
                            .label("Hello, 你好, مرحبا, Δ, अ \uD83C\uDD2F \uD83C\uDD2F \uD83C\uDD2F welcome 欢迎")
                            .fontColor(Color.RED).build())
               .addNode(Node.builder().label("\uD835\uDD6C \uD835\uDD6C ").build())
               .addNode(Node.builder().label("مرحبا بالعالم").build())
               .addNode(Node.builder()
                            .shape(NodeShapeEnum.RECORD)
                            .label("ملاعلاب ابحرم| الحروف العربية")
                            .build())
               .addNode(Node.builder()
                            .label("مرحبا بالعالم 2023")
                            .build())
               .addNode(Node.builder().label("a").fontName("Elephant").build())
               .addNode(Node.builder().label("你好世界, 你好世界")
                            .fontName("Arial")
                            .build())
               .build());
  }
}
