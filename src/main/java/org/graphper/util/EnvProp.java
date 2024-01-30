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

public final class EnvProp {

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

  public static boolean usePortAxisExpander() {
    return Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty("port_axis.node.expander"));
  }

  public static boolean useV1Coordinate() {
    return Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty("dot.coordinate.v1"));
  }

  public static boolean parallelLineDistinction() {
    return Boolean.TRUE.toString()
        .equalsIgnoreCase(System.getProperty("parallel.lines.case.distinction"));
  }
}
