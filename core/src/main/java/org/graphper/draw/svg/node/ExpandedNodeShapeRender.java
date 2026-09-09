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

package org.graphper.draw.svg.node;

import java.util.List;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.CirclePropCalc;
import org.graphper.api.ext.PeripheryNodePropCalc;
import org.graphper.api.ext.PolygonNodePropCalc;
import org.graphper.api.ext.RegularPolylinePropCalc;
import org.graphper.api.ext.ShapePropCalc;
import org.graphper.def.FlatPoint;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;
import org.graphper.draw.svg.SvgEditor;

final class ExpandedNodeShapeRender implements SvgConstants {

  static final String DECORATION = "data-node-decoration";

  private ExpandedNodeShapeRender() {}

  static void draw(NodeDrawProp node, SvgBrush brush) {
    NodeShapeEnum shape = (NodeShapeEnum) node.nodeAttrs().getShape();
    ShapePropCalc calc = shape.getShapePropCalc();
    if (calc instanceof PeripheryNodePropCalc) {
      PeripheryNodePropCalc periphery = (PeripheryNodePropCalc) calc;
      for (int i = 0; i < periphery.getBorders(); i++) {
        Box box = periphery.innerBox(node, i);
        Element border;
        if (periphery.getOutline() instanceof CirclePropCalc) {
          border = ellipse(brush, "outline" + i, box);
        } else {
          border =
              polygon(
                  brush,
                  "outline" + i,
                  ((RegularPolylinePropCalc) periphery.getOutline()).calcPoints(box));
        }
        if (i > 0) {
          decoration(border);
        }
      }
      return;
    }
    if (shape == NodeShapeEnum.M_CIRCLE) {
      ellipse(brush, "outline", node);
      double halfChord = Math.sqrt(1 - .75 * .75) / 2;
      line(brush, node, "mark0", .5 - halfChord, .125, .5 + halfChord, .125);
      line(brush, node, "mark1", .5 - halfChord, .875, .5 + halfChord, .875);
      return;
    }

    List<FlatPoint> points = ((PolygonNodePropCalc) calc).calcPoints(node);
    polygon(brush, "outline", points);
    switch (shape) {
      case M_DIAMOND:
      case M_SQUARE:
        for (int i = 0; i < points.size(); i++) {
          FlatPoint p = points.get(i);
          FlatPoint prev = points.get((i + points.size() - 1) % points.size());
          FlatPoint next = points.get((i + 1) % points.size());
          Element mark = brush.getOrCreateShapeEleById("mark" + i, PATH_ELE);
          mark.setAttribute(
              D,
              SvgEditor.pointsToSvgPath(
                  false,
                  p.getX() + .15 * (prev.getX() - p.getX()),
                  p.getY() + .15 * (prev.getY() - p.getY()),
                  p.getX() + .15 * (next.getX() - p.getX()),
                  p.getY() + .15 * (next.getY() - p.getY())));
          decoration(mark);
        }
        break;
      case TAB:
        line(brush, node, "tab", 0, .15, .45, .15);
        break;
      case FOLDER:
        line(brush, node, "fold", 0, .15, .4, .15);
        break;
      case BOX3D:
        line(brush, node, "front", 0, .15, .85, .15, .85, 1);
        line(brush, node, "depth", .85, .15, 1, 0);
        break;
      case COMPONENT:
        line(brush, node, "terminal0", .05, .2, .3, .2, .3, .4, .05, .4, .05, .2);
        line(brush, node, "terminal1", .05, .6, .3, .6, .3, .8, .05, .8, .05, .6);
        break;
      default:
        break;
    }
  }

  private static Element ellipse(SvgBrush brush, String id, Box box) {
    Element element = brush.getOrCreateShapeEleById(id, ELLIPSE_ELE);
    element.setAttribute(CX, String.valueOf(box.getX()));
    element.setAttribute(CY, String.valueOf(box.getY()));
    element.setAttribute(RX, String.valueOf(box.getWidth() / 2));
    element.setAttribute(RY, String.valueOf(box.getHeight() / 2));
    return element;
  }

  private static Element polygon(SvgBrush brush, String id, List<FlatPoint> points) {
    double[] coordinates = new double[points.size() * 2 + 2];
    for (int i = 0; i < points.size(); i++) {
      coordinates[i * 2] = points.get(i).getX();
      coordinates[i * 2 + 1] = points.get(i).getY();
    }
    coordinates[coordinates.length - 2] = coordinates[0];
    coordinates[coordinates.length - 1] = coordinates[1];
    Element element = brush.getOrCreateShapeEleById(id, POLYGON_ELE);
    element.setAttribute(POINTS, SvgEditor.generatePolylinePoints(coordinates));
    return element;
  }

  private static void line(SvgBrush brush, Box box, String id, double... coordinates) {
    for (int i = 0; i < coordinates.length; i += 2) {
      coordinates[i] = box.getLeftBorder() + coordinates[i] * box.getWidth();
      coordinates[i + 1] = box.getUpBorder() + coordinates[i + 1] * box.getHeight();
    }
    Element element = brush.getOrCreateShapeEleById(id, PATH_ELE);
    // Separate open paths use only M/L, supported by the Java 8 raster converter.
    element.setAttribute(D, SvgEditor.pointsToSvgPath(false, coordinates));
    decoration(element);
  }

  private static void decoration(Element element) {
    element.setAttribute(DECORATION, "true");
    element.setAttribute(FILL, NONE);
  }
}
