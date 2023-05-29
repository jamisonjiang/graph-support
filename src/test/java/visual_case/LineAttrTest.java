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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.graphper.api.FloatLabel;
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.LineStyle;
import org.graphper.api.attributes.Port;

public class LineAttrTest extends GraphvizVisual {

  private static final Node a = Node.builder().label("a").build();
  private static final Node b = Node.builder().label("b").build();
  private static final Node c = Node.builder().label("c").build();
  private static final Node d = Node.builder().label("d").build();
  private static final Node e = Node.builder().label("e").build();
  private static final Node f = Node.builder().label("f").build();
  private static final Node g = Node.builder().label("g").build();
  private static final Node h = Node.builder().label("h").build();

  static Stream<Line> labelCases() {
    return Stream.of(
        Line.builder(a, b).color(Color.RED).fontColor(Color.GREEN).label("a -> b").build(),
        Line.builder(c, d).color(Color.GOLD).fontColor(Color.BLUE)
            .label("First Text\nSecond Text\nThird Text").build()
    );
  }

  static Stream<Line> floatLabelCases() {
    return Stream.of(
        Line.builder(e, f).floatLabels(
            FloatLabel.builder()
                .label("center")
                .offset(0)

                .lengthRatio(0.5)
                .build()
        ).build(),
        Line.builder(g, h).floatLabels(
            FloatLabel.builder()
                .label("tail")
                .offset(-1)
                .lengthRatio(0)
                .fontSize(20)
                .build(),
            FloatLabel.builder()
                .label("center")
                .offset(0)
                .lengthRatio(0.5)
                .build(),
            FloatLabel.builder()
                .label("head")
                .offset(0.5)
                .lengthRatio(1)
                .fontSize(60)
                .build()
        ).build()
    );
  }

  static Stream<GraphvizBuilder> weightCases() {
    GraphvizBuilder graphvizBuilder = Graphviz.digraph()
        .nodeSep(3)
        .addLine(buildWeightLine(1, a, b))
        .addLine(buildWeightLine(1, a, c))
        .addLine(buildWeightLine(1, a, d));
    return Stream.of(graphvizBuilder)
        .flatMap(
            g -> {
              try {
                return Stream.of(
                    g.clone().addLine(buildWeightLine(1, a, e)),
                    g.clone().addLine(buildWeightLine(5, a, e))
                );
              } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
              }
            }
        );
  }

  static Line buildWeightLine(double weight, Node from, Node to) {
    return Line.builder(from, to)
        .weight(weight)
        .floatLabels(
            FloatLabel.builder()
                .label("weight=" + weight)
                .lengthRatio(0.5)
                .build()
        )
        .build();
  }

  static Stream<Line> headTailClipCases() {
    return Stream.of(
        Line.builder(a, b).label("headclip=true,tailclip=false").headclip(true).tailclip(false)
            .build(),
        Line.builder(a, b).label("headclip=false,tailclip=true").headclip(false).tailclip(true)
            .build(),
        Line.builder(a, b).label("headclip=false,tailclip=false").headclip(false).tailclip(false)
            .build(),
        Line.builder(a, b).label("headclip=true,tailclip=true").headclip(true).tailclip(true)
            .build()
    );
  }

  static Stream<Line> styleCases() {
    return Stream.of(
        Line.builder(a, b).label("style=DOTTED").style(LineStyle.DOTTED).build(),
        Line.builder(a, b).label("style=DASHED").style(LineStyle.DASHED).build(),
        Line.builder(a, b).label("style=BOLD").style(LineStyle.BOLD).build(),
        Line.builder(a, b).label("style=INVIS").style(LineStyle.INVIS).build(),
        Line.builder(a, b).label("style=SOLID").style(LineStyle.SOLID).build()
    );
  }

  static Stream<Line> arrowHeadTailCases() {
    List<Line> lines = new ArrayList<>(ArrowShape.values().length);
    for (ArrowShape arrowShape : ArrowShape.values()) {
      lines.add(
          Line.builder(a, b)
              .label("shape=" + arrowShape.name())
              .arrowTail(arrowShape)
              .arrowHead(arrowShape)
              .arrowSize(0.4 * (lines.size() + 1))
              .build()
      );
    }
    return lines.stream();
  }


  static Stream<Line> dirCases() {
    List<Line> lines = new ArrayList<>(Dir.values().length);
    for (Dir dir : Dir.values()) {
      lines.add(Line.builder(a, b).label("dir=" + dir.name()).dir(dir).build());
    }
    return lines.stream();
  }

  static Stream<Line> tailHeadPortCases() {
    List<Line> lines = new ArrayList<>((int) Math.pow(Port.values().length, 2));
    for (Port tailPort : Port.values()) {
      for (Port headPort : Port.values()) {
        lines.add(
            Line.builder(a, b)
                .floatLabels(
                    FloatLabel.builder()
                        .label("tailPort=" + tailPort.name())
                        .build(),
                    FloatLabel.builder()
                        .label("headPort=" + headPort.name())
                        .lengthRatio(1)
                        .build()
                )
                .tailPort(tailPort)
                .headPort(headPort)
                .build()
        );
      }
    }
    return lines.stream();
  }

  static Stream<Line> penWidthCases() {
    return Stream.of(
        Line.builder(a, b).label("penWidth=0.5").penWidth(0.5).build(),
        Line.builder(a, b).label("penWidth=1").penWidth(1).build(),
        Line.builder(a, b).label("penWidth=2").penWidth(2).build(),
        Line.builder(a, b).label("penWidth=3").penWidth(3).build(),
        Line.builder(a, b).label("penWidth=4").penWidth(4).build()
    );
  }

  @ParameterizedTest
  @MethodSource("labelCases")
  public void testLabel(Line line) {
    visual("line_label_test", line);
  }

  @ParameterizedTest
  @MethodSource("floatLabelCases")
  public void testFloatLabel(Line line) {
    visual("float_label_line", line);
  }

  @Test
  public void testControlPoints() {
    Graphviz graphviz = Graphviz.digraph()
        .label("controlPoints_test")
        .addLine(a, b)
        .addLine(
            Line.builder(a, b)
                .label("this line will show control points")
                .controlPoints(true)
                .build()
        )
        .build();
    visual(graphviz);
  }

  @Test
  public void testShowboxes() {
    Graphviz graphviz = Graphviz.digraph()
        .label("showboxes_test")
        .addLine(a, b)
        .addLine(a, c)
        .addLine(a, d)
        .addLine(a, e)
        .addLine(b, f)
        .addLine(c, g)
        .addLine(a, f)
        .addLine(
            Line.builder(a, f).showboxes(true).build()
        )
        .addLine(
            Line.builder(a, g).showboxes(true).build()
        )
        .build();
    visual(graphviz);
  }

  @Test
  public void testRadian() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Graphviz graphviz = Graphviz
        .digraph()
        .label("radian_test")
        .rankSep(2)
        .addLine(a, b)
        .addLine(a, b)
        .addLine(a, b)
        .addLine(a, b)
        .addLine(Line.builder(a, b).label("radian=5").radian(5).build())
        .addLine(Line.builder(a, b).label("radian=40").radian(40).build())
        .addLine(Line.builder(a, b).label("radian=50").radian(50).build())
        .addLine(a, a)
        .build();

    visual(graphviz);
  }

  @ParameterizedTest
  @MethodSource("weightCases")
  public void testWeight(GraphvizBuilder graphvizBuilder) {
    visual(graphvizBuilder.build());
  }

  @ParameterizedTest
  @MethodSource("headTailClipCases")
  public void testHeadTailClip(Line line) {
    visual("headclip_tailclip_test", line);
  }

  @Test
  public void testMinlen() {
    Graphviz graphviz = Graphviz.digraph()
        .label("minlen_test")
        .addLine(Line.builder(a, b).label("minlen=1").minlen(1).build())
        .addLine(Line.builder(a, c).label("minlen=2").minlen(2).build())
        .addLine(Line.builder(a, d).label("minlen=3").minlen(3).build())
        .build();
    visual(graphviz);
  }

  @ParameterizedTest
  @MethodSource("styleCases")
  public void testStyle(Line line) {
    visual("style_test", line);
  }

  @ParameterizedTest
  @MethodSource("arrowHeadTailCases")
  public void testArrowHeadTail(Line line) {
    visual("arrowHead_arrowTail_test", line);
  }

  @ParameterizedTest
  @MethodSource("dirCases")
  public void testDir(Line line) {
    visual("dir_test", line);
  }

  @ParameterizedTest
  @MethodSource("tailHeadPortCases")
  public void testTailHeadPort(Line line) {
    visual("tailPort_headPort_test", line);
  }

  @Test
  public void testHref() {
    Graphviz graphviz = Graphviz.digraph()
        .label("href_test")
        .addLine(Line.builder(a, b).label("This is a link").href("https://github.com/").build())
        .build();
    visual(graphviz);
  }

  @ParameterizedTest
  @MethodSource("penWidthCases")
  public void testPenWidth(Line line) {
    visual("penWidth_test", line);
  }

  private void visual(String label, Line line) {
    Graphviz graphviz = Graphviz.digraph()
        .label(label)
        .addLine(line)
        .build();
    visual(graphviz);
  }
}
