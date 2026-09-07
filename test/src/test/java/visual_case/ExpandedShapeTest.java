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
import helper.VisualTags;
import java.util.Locale;
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.Subgraph.SubgraphBuilder;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.LineStyle;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Rank;
import org.graphper.api.attributes.Rankdir;
import org.junit.jupiter.api.Test;

@VisualTags({"gallery", "shape", "node", "arrow", "arrowhead", "arrowtail", "filled", "hollow"})
public class ExpandedShapeTest extends GraphvizVisual {

  @Test
  public void testExpandedShapeGallery() {
    NodeShapeEnum[] shapes = {
        NodeShapeEnum.OVAL, NodeShapeEnum.NONE, NodeShapeEnum.RECTANGLE,
        NodeShapeEnum.SQUARE, NodeShapeEnum.POLYGON, NodeShapeEnum.HOUSE,
        NodeShapeEnum.INVHOUSE, NodeShapeEnum.DOUBLECIRCLE, NodeShapeEnum.DOUBLEOCTAGON,
        NodeShapeEnum.TRIPLEOCTAGON, NodeShapeEnum.M_DIAMOND, NodeShapeEnum.M_SQUARE,
        NodeShapeEnum.M_CIRCLE, NodeShapeEnum.TAB, NodeShapeEnum.FOLDER,
        NodeShapeEnum.BOX3D, NodeShapeEnum.COMPONENT
    };
    ArrowShape[] arrows = {
        ArrowShape.NORMAL, ArrowShape.ONORMAL, ArrowShape.INV, ArrowShape.OINV,
        ArrowShape.BOX, ArrowShape.OBOX, ArrowShape.DOT, ArrowShape.ODOT,
        ArrowShape.DIAMOND, ArrowShape.ODIAMOND, ArrowShape.VEE, ArrowShape.CROW,
        ArrowShape.TEE, ArrowShape.CURVE, ArrowShape.ICURVE, ArrowShape.NONE
    };
    GraphvizBuilder gallery = Graphviz.digraph()
        .label("Expanded shapes: filled / unfilled nodes; arrows at tail and head")
        .rankdir(Rankdir.LR)
        .nodeSep(0.25)
        .rankSep(0.5);
    Node[] previousHeads = new Node[6];
    for (int first = 0; first < shapes.length; first += previousHeads.length) {
      SubgraphBuilder tails = Subgraph.builder().rank(Rank.SAME);
      SubgraphBuilder heads = Subgraph.builder().rank(Rank.SAME);
      for (int row = 0; row < previousHeads.length && first + row < shapes.length; row++) {
        int index = first + row;
        NodeShapeEnum shape = shapes[index];
        ArrowShape arrow = arrows[index % arrows.length];
        Node tail = Node.builder().id(shape.name() + "_filled")
            .shape(shape).label(shape.getName()).fontSize(12)
            .color(Color.INDIGO).fillColor(Color.BISQUE).build();
        Node head = Node.builder().id(shape.name() + "_unfilled")
            .shape(shape).label(shape.getName()).fontSize(12)
            .color(Color.INDIGO).build();
        tails.addNode(tail);
        heads.addNode(head);
        gallery.addLine(Line.builder(tail, head)
                            .label(arrow.name().toLowerCase(Locale.ROOT)).fontSize(10)
                            .dir(Dir.BOTH).arrowHead(arrow).arrowTail(arrow)
                            .arrowSize(1).color(Color.INDIGO).build());
        // Invisible links keep six sample rows aligned across the three column groups.
        if (previousHeads[row] != null) {
          gallery.addLine(Line.builder(previousHeads[row], tail).style(LineStyle.INVIS).build());
        }
        previousHeads[row] = head;
      }
      gallery.subgraph(tails.build()).subgraph(heads.build());
    }

    // The helper exercises toSvg() and registers SVG/PNG/PDF previews in graph-visual.html.
    visual(gallery.build());
  }
}
