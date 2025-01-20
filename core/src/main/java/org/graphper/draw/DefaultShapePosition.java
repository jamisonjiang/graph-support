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

package org.graphper.draw;

import java.util.Objects;
import org.graphper.api.ext.ShapePropCalc;
import org.graphper.util.Asserts;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.ext.DefaultBox;
import org.graphper.api.ext.ShapePosition;

/**
 * An object with container properties and a shape descriptor implemented by default.
 *
 * @author Jamison Jiang
 */
public class DefaultShapePosition extends DefaultBox implements ShapePosition {

  private final ShapePropCalc shapeProp;

  public DefaultShapePosition(double x, double y, double height,
                              double width, NodeShape nodeShape) {
    super(x - width / 2, x + width / 2, y - height / 2, y + height / 2);
    Asserts.nullArgument(nodeShape, "nodeShape");
    this.shapeProp = nodeShape;
  }

  @Override
  public ShapePropCalc shapeProp() {
    return shapeProp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    DefaultShapePosition that = (DefaultShapePosition) o;
    return shapeProp == that.shapeProp;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), shapeProp);
  }

}
