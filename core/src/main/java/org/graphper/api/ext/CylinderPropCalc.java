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
import org.graphper.api.attributes.NodeShapeEnum;

public class CylinderPropCalc implements ShapePropCalc, Serializable {

  private static final long serialVersionUID = 2065821057594213750L;

  public static final int TOP_LEN = 6;

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth) {
    return new FlatPoint(innerHeight + (3 * TOP_LEN), innerWidth);
  }

  @Override
  public boolean in(Box box, FlatPoint point) {
    return NodeShapeEnum.RECT.in(box, point);
  }

  @Override
  public FlatPoint labelCenter(FlatPoint labelSize, Box box) {
    return new FlatPoint(box.getX(), box.getY() + ((double) TOP_LEN / 2));
  }
}
