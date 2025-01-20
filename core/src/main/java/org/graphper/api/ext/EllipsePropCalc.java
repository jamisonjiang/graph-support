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

package org.graphper.api.ext;

import static org.graphper.api.attributes.NodeShapeEnum.ELLIPSE;

import java.io.Serializable;
import org.graphper.def.FlatPoint;

public class EllipsePropCalc implements ShapePropCalc, Serializable {

  private static final long serialVersionUID = 5285076865672929036L;

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth) {
    double th = ELLIPSE.topHeight(innerHeight);
    double bh = ELLIPSE.bottomHeight(innerHeight);
    double lw = ELLIPSE.leftWidth(innerWidth);
    double rw = ELLIPSE.rightWidth(innerWidth);
    double lwidth = Math.max(lw, rw);
    double lheight = Math.max(th, bh);

    double boxHalfHeight = innerHeight / 2;
    double boxHalfWidth = innerWidth / 2;
    boolean boxNotInEllipse = ellipseFormula(lwidth, lheight, boxHalfWidth, boxHalfHeight) > 1;

    if (boxNotInEllipse) {
      double scale = lwidth / lheight;
      lwidth = Math.sqrt(
          Math.pow(boxHalfWidth, 2) + Math.pow(boxHalfHeight, 2) * Math.pow(scale, 2)
      );
      lheight = lwidth / scale;
    }

    return new FlatPoint(lheight * 2, lwidth * 2);
  }

  @Override
  public boolean in(Box box, FlatPoint point) {
    double w = ELLIPSE.leftWidth(box.getWidth());
    double h = ELLIPSE.topHeight(box.getHeight());
    return ellipseFormula(w, h, point.getX() - box.getX(),
                          point.getY() - box.getY()) <= 1;
  }

  private double ellipseFormula(double a, double b, double x, double y) {
    return Math.pow(x, 2) / Math.pow(a, 2) + Math.pow(y, 2) / Math.pow(b, 2);
  }

}
