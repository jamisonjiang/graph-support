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

package org.graphper.layout.dot;

import org.graphper.def.FlatPoint;
import org.graphper.api.ext.ShapePosition;
import org.graphper.draw.ClusterDrawProp;

public abstract class PathClip<P> {

  protected abstract FlatPoint pathFrom(P path);

  protected abstract FlatPoint pathTo(P path);

  protected abstract P fromArrowClip(double arrowSize, P path);

  protected abstract P toArrowClip(double arrowSize, P path);

  protected abstract P clusterClip(ClusterDrawProp clusterDrawProp, P path);

  protected abstract P nodeClip(ShapePosition node, P path, boolean firstStart);

  protected boolean isNull(P path) {
    return path == null;
  }

  protected boolean isNotNull(P path) {
    return !isNull(path);
  }
}
