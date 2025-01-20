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

import org.graphper.api.ClusterAttrs;
import org.graphper.api.attributes.ClusterShape;
import org.graphper.util.Asserts;

/**
 * {@link ClusterShape} post processing.
 *
 * @author Jamison Jiang
 */
public interface ClusterShapePost {

  /**
   * When some characteristics of the shape need to be changed according to the rest of the cluster
   * attributes, use this method to post-create the enhanced {@link ClusterShape} to replace the
   * original {@link ClusterShape}.
   *
   * @param clusterAttrs cluster attribute
   * @return post {@code ClusterShape}
   */
  default ClusterShape post(ClusterAttrs clusterAttrs) {
    Asserts.nullArgument(clusterAttrs, "clusterAttrs");
    return clusterAttrs.getShape();
  }
}
