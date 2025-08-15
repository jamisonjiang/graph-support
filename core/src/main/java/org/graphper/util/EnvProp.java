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

package org.graphper.util;

import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Layout;

public final class EnvProp {

  // distance deviation tolerance
  public static final double CLIP_DIST_ERROR = 0.1;

  private EnvProp() {
  }

  public static boolean qualityCheck() {
    return System.getProperty("graph.quality.check") != null;
  }

  public static boolean useLocalImgConverter() {
    return Boolean.TRUE.toString().equals(System.getProperty("use.local.img.converter"));
  }

  public static Rankdir defaultRankdir() {
    return Rankdir.rankdir(System.getProperty("rankdir"));
  }

  public static Layout defaultLayout() {
    return Layout.layout(System.getProperty("layout"));
  }

  public static boolean useV1Coordinate() {
    return Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty("dot.coordinate.v1"));
  }

  public static boolean parallelLineDistinction() {
    return Boolean.TRUE.toString()
        .equalsIgnoreCase(System.getProperty("parallel.lines.case.distinction"));
  }

  public static boolean ignoreBoxCheck() {
    return Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty("box.border.check.ignore"));
  }
}
