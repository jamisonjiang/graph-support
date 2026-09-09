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
import org.graphper.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Computes a label center coordinate for an alignment within a rectangle. */
public abstract class LabelPositionCalc implements Serializable {

  private static final long serialVersionUID = -7315061356667594850L;

  private static final Logger log = LoggerFactory.getLogger(LabelPositionCalc.class);

  /** Validates the bounds and label size, then computes the aligned center coordinate. */
  public double centerPos(FlatPoint upperLeft, FlatPoint lowerRight, FlatPoint labelSize) {
    Asserts.nullArgument(upperLeft, "upperLeft");
    Asserts.nullArgument(lowerRight, "lowerRight");
    Asserts.nullArgument(labelSize, "labelSize");

    if (labelSize.getWidth() > Math.abs(lowerRight.getX() - upperLeft.getX())
        || labelSize.getHeight() > Math.abs(lowerRight.getY() - upperLeft.getY())) {
      log.debug("The label routerBox size is larger than the outer container");
    }

    if (labelSize.getWidth() < 0 || labelSize.getHeight() < 0) {
      log.debug("label routerBox size is less than 0");
    }

    return centerPos0(upperLeft, lowerRight, labelSize);
  }

  protected abstract double centerPos0(
      FlatPoint upperLeft, FlatPoint lowerRight, FlatPoint labelSize);

  /** Positions the label against the left border. */
  public static class LeftLabelPositionCalc extends LabelPositionCalc {

    private static final long serialVersionUID = -8949864737194759650L;

    @Override
    protected double centerPos0(FlatPoint upperLeft, FlatPoint lowerRight, FlatPoint labelSize) {
      return upperLeft.getX() + labelSize.getWidth() / 2;
    }
  }

  /** Positions the label against the right border. */
  public static class RightLabelPositionCalc extends LabelPositionCalc {

    private static final long serialVersionUID = -8949864737194759650L;

    @Override
    protected double centerPos0(FlatPoint upperLeft, FlatPoint lowerRight, FlatPoint labelSize) {
      return lowerRight.getX() - labelSize.getWidth() / 2;
    }
  }

  /** Positions the label against the top border. */
  public static class TopLabelPositionCalc extends LabelPositionCalc {

    private static final long serialVersionUID = -8949864737194759650L;

    @Override
    protected double centerPos0(FlatPoint upperLeft, FlatPoint lowerRight, FlatPoint labelSize) {
      return upperLeft.getY() + labelSize.getHeight() / 2;
    }
  }

  /** Positions the label against the bottom border. */
  public static class BottomLabelPositionCalc extends LabelPositionCalc {

    private static final long serialVersionUID = -8949864737194759650L;

    @Override
    protected double centerPos0(FlatPoint upperLeft, FlatPoint lowerRight, FlatPoint labelSize) {
      return lowerRight.getY() - labelSize.getHeight() / 2;
    }
  }

  /** Centers the label horizontally. */
  public static class HorCenterLabelPositionCalc extends LabelPositionCalc {

    private static final long serialVersionUID = -8949864737194759650L;

    @Override
    protected double centerPos0(FlatPoint upperLeft, FlatPoint lowerRight, FlatPoint labelSize) {
      return (upperLeft.getX() + lowerRight.getX()) / 2;
    }
  }

  /** Centers the label vertically. */
  public static class VerCenterLabelPositionCalc extends LabelPositionCalc {

    private static final long serialVersionUID = -8949864737194759650L;

    @Override
    protected double centerPos0(FlatPoint upperLeft, FlatPoint lowerRight, FlatPoint labelSize) {
      return (upperLeft.getY() + lowerRight.getY()) / 2;
    }
  }
}
