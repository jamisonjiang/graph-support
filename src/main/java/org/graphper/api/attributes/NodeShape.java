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
 * Represents the description object of a node shape, defining its characteristics and properties
 * such as dimensions, center of gravity, and line-clipping behavior. This interface is essential
 * for determining the layout and container dimensions of a node shape.
 *
 * <p>To create and use a custom node shape:</p>
 * <ol>
 *   <li>Implement the {@code NodeShape} interface to describe the shape's properties and behaviors.</li>
 *   <li>Implement the {@link CustomizeShapeRender} abstract class to define the rendering logic for the shape.</li>
 *   <li>Register the renderer using {@link CustomizeShapeRender#register(CustomizeShapeRender)} or via SPI.</li>
 * </ol>
 *
 * <p>Example of creating and registering a custom node shape:</p>
 * <pre>
 * {@code
 * // Implement the characteristics of the custom shape
 * public class ArrowNodeShape implements NodeShape {
 *     // Implement methods to describe node shape properties...
 * }
 *
 * // Implement the rendering logic for the custom shape
 * public class ArrowNodeShapeRender extends CustomizeShapeRender {
 *     public String getShapeName() {
 *         return "arrow";
 *     }
 *
 *     public void drawNodeSvg(SvgBrush nodeBrush, NodeDrawProp nodeDrawProp) {
 *         // Custom rendering logic for the arrow shape...
 *     }
 * }
 *
 * // Register the custom shape renderer
 * CustomizeShapeRender.register(new ArrowNodeShapeRender());
 *
 * // Use the custom shape
 * NodeShape shape = new ArrowNodeShape();
 * Node node = Node.builder().shape(shape).build();
 * }
 * </pre>
 *
 * <p>This pairing of a {@code NodeShape} and its {@link CustomizeShapeRender} implementation
 * ensures a complete lifecycle for defining, describing, and rendering the node shape.</p>
 *
 * @author Jamison Jiang
 * @see CustomizeShapeRender Renderer for custom shapes
 * @see NodeShapeEnum System-supported default shapes
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
