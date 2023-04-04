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

public class SymmetryShapeCenterCalc implements ShapeCenterCalc, Serializable {

  private static final long serialVersionUID = -299277822910531114L;

  public static final SymmetryShapeCenterCalc SSPC = new SymmetryShapeCenterCalc();

  @Override
  public double leftWidth(Double width) {
    return half(width);
  }

  @Override
  public double rightWidth(Double width) {
    return half(width);
  }

  @Override
  public double topHeight(Double height) {
    return half(height);
  }

  @Override
  public double bottomHeight(Double height) {
    return half(height);
  }

  private double half(Double val) {
    if (val == null) {
      return 0;
    }

    return val / 2;
  }
}
