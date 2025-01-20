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

import java.io.Serializable;
import org.graphper.def.FlatPoint;

public class CirclePropCalc implements ShapePropCalc, Serializable {

  private static final long serialVersionUID = 4004804900027011314L;

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth) {
    double diameter = Math.sqrt(Math.pow(innerHeight, 2) + Math.pow(innerWidth, 2));
    return new FlatPoint(diameter, diameter);
  }

  @Override
  public boolean in(Box box, FlatPoint flatPoint) {
    double r = Math.pow(flatPoint.getX() - box.getX(), 2)
        + Math.pow(flatPoint.getY() - box.getY(), 2);
    return Math.sqrt(r) <= box.getWidth() / 2;
  }

  @Override
  public void ratio(FlatPoint boxSize) {
    squareRatio(boxSize);
  }
}
