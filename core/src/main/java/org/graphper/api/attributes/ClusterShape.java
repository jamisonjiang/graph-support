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

import java.io.Serializable;
import org.graphper.api.ext.ClusterShapePost;
import org.graphper.api.ext.ShapePropCalc;
import org.graphper.draw.CustomizeShapeRender;

/**
 * The description object of the cluster shape, which describes a series of characteristics of the
 * shape. For some implementations of system default, please check {@link ClusterShapeEnum}.
 *
 * <p>Custom cluster shapes can directly implement this interface and assign values to clusters
 * directly. A series of descriptions of this interface play an important role in the layout of the
 * cluster container size, center of gravity and line segment clipping, but if you need to render
 * the cluster, you need to implement {@link CustomizeShapeRender}, and register through SPI or
 * manually call {@link CustomizeShapeRender#register(CustomizeShapeRender)}.
 *
 * <p>Unlike {@link NodeShape}, other cluster shapes except {@link ClusterShapeEnum#RECT} no
 * guarantee that cluster container will surround all nodes under {@link Layout#DOT} and {@link Layout#DOTQ} engines but will
 * try best estimated the container size by {@link #minContainerSize(double, double)} method, but
 * still have the following principles as much as possible surround all nodes if you want to
 * {@code ClusterShape} works well under {@link Layout#DOT} and {@link Layout#DOTQ} engines:
 * <ul>
 *   <li>The gap between internal box and external box is as small ass possible, it means output
 *   of {@link #minContainerSize(double, double)} of current shape close enough than input.
 *   e.g, {@link ClusterShapeEnum#RECT} no gap between internal and external boxes.
 *   <li>Internal nodes are kept isolated from external nodes of cluster, it means interact edges
 *   from internal nodes to external nodes as little as possible.
 *   <li>Avoid cluster nesting as much as possible if cluster shapes is not {@link ClusterShapeEnum#RECT}
 *   (or the cluster shape no gap between internal and external box like RECT shape), the error in
 *   evaluation will be magnified in this case.
 *   <li>Manual adjust {@link org.graphper.api.Cluster.ClusterBuilder#margin(double)} reserve enough
 *   internal space to avoid nodes overflow cluster container.
 * </ul>
 *
 * @author Jamison Jiang
 * @see ClusterShapeEnum System supports shapes by default
 * @see CustomizeShapeRender Renderer for custom shapes.
 */
public interface ClusterShape extends ShapePropCalc, ClusterShapePost, Serializable {

  /**
   * Returns the shape name.
   *
   * @return the shape name
   */
  String getName();

  /**
   * Returns the cluster shape attribute description object.
   *
   * @return cluster shape attribute description object
   */
  default ShapePropCalc getShapePropCalc() {
    return this;
  }
}
