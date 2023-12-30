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
import org.graphper.api.ext.NodeShapePost;
import org.graphper.draw.CustomizeShapeRender;
import org.graphper.api.ext.ShapeCenterCalc;
import org.graphper.api.ext.ShapePropCalc;

/**
 * The description object of the node shape, which describes a series of characteristics of the
 * shape. For some implementations of system default, please check {@link NodeShapeEnum}.
 *
 * <p>Custom node shapes can directly implement this interface and assign values to nodes directly.
 * A series of descriptions of this interface play an important role in the layout of the node
 * container size, center of gravity and line segment clipping, but if you need to render the node,
 * you need to implement {@link CustomizeShapeRender}, and register through SPI or manually call
 * {@link CustomizeShapeRender#register(CustomizeShapeRender)}.
 *
 * @author Jamison Jiang
 * @see NodeShapeEnum System supports shapes by default
 * @see CustomizeShapeRender Renderer for custom shapes.
 */
public interface NodeShape extends ShapeCenterCalc, ShapePropCalc, NodeShapePost, Serializable {

  /**
   * Returns the shape name.
   *
   * @return the shape name
   */
  String getName();

  /**
   * Returns the base height of the shape.
   *
   * @return the base height of the shape
   */
  double getDefaultHeight();

  /**
   * Returns the base width of the shape.
   *
   * @return the base width of the shape
   */
  double getDefaultWidth();

  /**
   * Returns the node shape attribute description object.
   *
   * @return node shape attribute description object
   */
  default ShapePropCalc getShapePropCalc() {
    return this;
  }
}
