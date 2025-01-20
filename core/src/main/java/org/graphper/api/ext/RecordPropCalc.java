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

public class RecordPropCalc implements ShapePropCalc, Serializable {

  private static final long serialVersionUID = -8655733264629048199L;

  public static final int CORNER_LEN = 40;

  private final boolean radianCorner;

  public RecordPropCalc(boolean radianCorner) {
    this.radianCorner = radianCorner;
  }

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth) {
    return new FlatPoint(innerHeight, innerWidth);
  }

  @Override
  public boolean in(Box box, FlatPoint point) {
    return Math.abs(box.getX() - point.getX()) <= box.getWidth() / 2
        && Math.abs(box.getY() - point.getY()) <= box.getHeight() / 2;
  }

  public boolean isRadianCorner() {
    return radianCorner;
  }
}
