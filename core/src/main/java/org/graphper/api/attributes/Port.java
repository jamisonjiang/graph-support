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

package org.graphper.api.attributes;

import java.util.Objects;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.PortPosition;
import org.graphper.api.ext.RatioPortPosition;
import org.graphper.util.Asserts;

/**
 * Port Position: where on a node an edge should be aimed.
 *
 * @author Jamison Jiang
 */
public enum Port implements PortPosition {

  CENTER(-1, "c", new RatioPortPosition(0, 0)),

  WEST(0, "w", new RatioPortPosition(-0.5, 0)),

  NORTH_WEST(1, "nw", new RatioPortPosition(-0.5, -0.5)),

  NORTH(2, "n", new RatioPortPosition(0, -0.5)),

  NORTH_EAST(3, "ne", new RatioPortPosition(0.5, -0.5)),

  EAST(4, "e", new RatioPortPosition(0.5, 0)),

  SOUTH_EAST(5, "se", new RatioPortPosition(0.5, 0.5)),

  SOUTH(6, "s", new RatioPortPosition(0, 0.5)),

  SOUTH_WEST(7, "sw", new RatioPortPosition(-0.5, 0.5));

  private final RatioPortPosition portPosition;

  private final int no;

  private final String code;

  Port(int no, String code, RatioPortPosition portPosition) {
    this.no = no;
    this.code = code;
    this.portPosition = portPosition;
  }

  @Override
  public double horOffset(Box box) {
    return portPosition.horOffset(box);
  }

  @Override
  public double verOffset(Box box) {
    return portPosition.verOffset(box);
  }

  public int getNo() {
    return no;
  }

  public boolean isAxis() {
    return this == Port.WEST || this == Port.NORTH || this == Port.EAST || this == Port.SOUTH;
  }

  public Port pre() {
    if (no == 0) {
      return valueOf(maxNo() - 1);
    }
    return valueOf(no - 1);
  }

  public Port next() {
    if (no == maxNo() - 1) {
      return valueOf(0);
    }
    return valueOf(no + 1);
  }

  public static int maxNo() {
    return values().length - 1;
  }

  public static Port valueOf(int no) {
    Asserts.illegalArgument(no < 0 || no >= Port.values().length,
                            "Port no must between 0 and " + (Port.values().length - 1));
    return Port.values()[no + 1];
  }

  public double horOffsetRatio() {
    return portPosition.getxRatio();
  }

  public double verOffsetRatio() {
    return portPosition.getyRatio();
  }

  public static Port valueOfCode(String code) {
    for (Port port : values()) {
      if (Objects.equals(port.code, code)) {
        return port;
      }
    }

    return null;
  }
}
