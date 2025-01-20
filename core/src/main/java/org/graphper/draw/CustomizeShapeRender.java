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

package org.graphper.draw;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.attributes.ClusterShape;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.util.Asserts;
import org.graphper.draw.svg.SvgConstants;

/**
 * Renderer for custom shapes. This abstract class allows registering and rendering custom shapes.
 * Regardless of the number of rendering methods supported in the future, brushes corresponding to
 * the rendering type can be provided for editing within this class.
 *
 * <p>There are two ways to register a custom shape renderer:
 * <ul>
 *   <li>Manually calling the {@link #register(CustomizeShapeRender)} method.</li>
 *   <li>Registering using the Service Provider Interface (SPI).</li>
 * </ul>
 *
 * <p>This pairing of a ({@link NodeShape} or {@link ClusterShape}) and its {@code CustomizeShapeRender}
 * implementation ensures a complete lifecycle for defining, describing, and rendering the node shape.
 *
 * @author Jamison Jiang
 * @see NodeShape
 * @see ClusterShape
 */
@SuppressWarnings("all")
public abstract class CustomizeShapeRender {

  private static volatile Map<String, CustomizeShapeRender> CUSTOMIZE_REGISTER;

  static {
    ServiceLoader<CustomizeShapeRender> customizeShapeRenders = ServiceLoader.load(
        CustomizeShapeRender.class);
    for (CustomizeShapeRender customizeShapeRender : customizeShapeRenders) {
      register(customizeShapeRender);
    }
  }

  /**
   * Registers a custom shape renderer. If a shape with the same name already exists, the first
   * registered shape renderer will take precedence. Use {@link #registered(String)} to check if the
   * corresponding shape has been registered.
   *
   * @param customizeShapeRender the custom shape renderer to register
   * @throws NullPointerException     if the provided renderer is {@code null}
   * @throws IllegalArgumentException if the shape name returned by {@link #getShapeName()} is
   *                                  {@code null}
   */
  public static void register(CustomizeShapeRender customizeShapeRender) {
    Asserts.nullArgument(customizeShapeRender, "custimizeNodeShape");
    Asserts.illegalArgument(customizeShapeRender.getShapeName() == null,
                            "CustimizeNodeShape can not return null shapeName");
    customizeNodeShapeMap().computeIfAbsent(customizeShapeRender.getShapeName(),
                                            s -> customizeShapeRender);
  }

  /**
   * Returns the registered shape renderer for the specified shape.
   *
   * @param shapeName the name of the shape
   * @return the shape renderer, or {@code null} if no renderer is registered for the given shape
   */
  public static CustomizeShapeRender getCustomizeShapeRender(String shapeName) {
    if (CUSTOMIZE_REGISTER == null) {
      return null;
    }
    return CUSTOMIZE_REGISTER.get(shapeName);
  }

  /**
   * Checks whether a shape with the specified name has been registered.
   *
   * @param shapeName the name of the shape
   * @return {@code true} if the shape has been registered, {@code false} otherwise
   */
  public static boolean registered(String shapeName) {
    return getCustomizeShapeRender(shapeName) != null;
  }

  // --------------------------------- Customize draw method ---------------------------------

  /**
   * Returns the name of the shape.
   *
   * @return the shape name
   */
  public abstract String getShapeName();

  /**
   * Draws a node shape within an SVG structure.
   *
   * <p>The node shape can consist of multiple {@link org.graphper.draw.svg.Element} objects,
   * forming a complete SVG structure. Each element represents a distinct part of the shape, such as
   * the outline, internal details, or decorations. The rendering logic is determined by the
   * specific implementation of this method.</p>
   *
   * <p>This method focuses solely on rendering the outline of the shape. It does not consider
   * other node attributes such as color, labels, pen width, or style, which will be handled by
   * other dedicated handlers. The rendered shape is constructed using multiple SVG elements and is
   * automatically added to the {@link SvgConstants#SHAPE_GROUP_KEY} group via the
   * {@link SvgBrush#getOrCreateShapeEleById(String, String)} method.</p>
   *
   * <p>Example usage:</p>
   * <pre>
   * {@code
   * // Example implementation for drawing a complex node outline with multiple elements
   * public void drawNodeSvg(SvgBrush nodeBrush, NodeDrawProp nodeDrawProp) {
   *     // Create a rectangle as part of the node outline
   *     Element rectElement = nodeBrush.getOrCreateShapeEleById("rect", "rect");
   *     rectElement.setAttribute("x", String.valueOf(nodeDrawProp.getX()));
   *     rectElement.setAttribute("y", String.valueOf(nodeDrawProp.getY()));
   *     rectElement.setAttribute("width", String.valueOf(nodeDrawProp.getWidth()));
   *     rectElement.setAttribute("height", String.valueOf(nodeDrawProp.getHeight()));
   *
   *     // Create a border circle as another part of the node outline
   *     Element circleElement = nodeBrush.getOrCreateShapeEleById("circle", "circle");
   *     circleElement.setAttribute("cx", String.valueOf(nodeDrawProp.getX() + nodeDrawProp.getWidth() / 2));
   *     circleElement.setAttribute("cy", String.valueOf(nodeDrawProp.getY() + nodeDrawProp.getHeight() / 2));
   *     circleElement.setAttribute("r", String.valueOf(Math.min(nodeDrawProp.getWidth(), nodeDrawProp.getHeight()) / 2));
   * }
   * }
   * </pre>
   *
   * @param nodeBrush    the SVG brush used for drawing, which provides utility methods for creating
   *                     and interacting with SVG elements
   * @param nodeDrawProp the properties of the node to be drawn, such as size, color, position, and
   *                     styling information
   */
  public void drawNodeSvg(SvgBrush nodeBrush, NodeDrawProp nodeDrawProp) {
  }

  /**
   * Draws a cluster shape within an SVG structure.
   *
   * <p>The cluster shape can consist of multiple {@link org.graphper.draw.svg.Element} objects,
   * forming a complete SVG structure. Each element represents a distinct part of the cluster, such as
   * the outline, boundaries, or decorations. The rendering logic is determined by the specific
   * implementation of this method.</p>
   *
   * <p>This method focuses solely on rendering the outline or boundary of the cluster. It does not
   * handle other cluster attributes such as color, labels, or styles, which will be processed by
   * other dedicated handlers. The cluster outline is constructed using multiple SVG elements and is
   * automatically added to the {@link SvgConstants#SHAPE_GROUP_KEY} group via the
   * {@link SvgBrush#getOrCreateShapeEleById(String, String)} method.</p>
   *
   * <p>Example usage:</p>
   * <pre>
   * {@code
   * // Example implementation for drawing a cluster outline with multiple elements
   * public void drawClusterSvg(SvgBrush clusterBrush, ClusterDrawProp clusterDrawProp) {
   *     // Create a rectangle to represent the cluster boundary
   *     Element rectElement = clusterBrush.getOrCreateShapeEleById("cluster_rect", "rect");
   *     rectElement.setAttribute("x", String.valueOf(clusterDrawProp.getX()));
   *     rectElement.setAttribute("y", String.valueOf(clusterDrawProp.getY()));
   *     rectElement.setAttribute("width", String.valueOf(clusterDrawProp.getWidth()));
   *     rectElement.setAttribute("height", String.valueOf(clusterDrawProp.getHeight()));
   *
   *     // Create a decorative element (e.g., a dashed border) for the cluster
   *     Element dashedBorder = clusterBrush.getOrCreateShapeEleById("cluster_border", "path");
   *     dashedBorder.setAttribute("d", "M ..."); // Specify the path data for the dashed border
   *     dashedBorder.setAttribute("stroke-dasharray", "5,5");
   * }
   * }
   * </pre>
   *
   * @param clusterBrush    the SVG brush used for drawing, providing utility methods for creating
   *                        and grouping SVG elements
   * @param clusterDrawProp the properties of the cluster to be drawn, such as size, position,
   *                        and boundaries
   */
  public void drawClusterSvg(SvgBrush clusterBrush, ClusterDrawProp clusterDrawProp) {
  }

  // --------------------------------- private method ---------------------------------

  /**
   * Returns the map containing all registered custom shape renderers. Initializes the map if it has
   * not been created yet.
   *
   * @return the map of custom shape renderers
   */
  private static Map<String, CustomizeShapeRender> customizeNodeShapeMap() {
    if (CUSTOMIZE_REGISTER == null) {
      synchronized (CustomizeShapeRender.class) {
        if (CUSTOMIZE_REGISTER == null) {
          CUSTOMIZE_REGISTER = new ConcurrentHashMap<>();
        }
      }
    }
    return CUSTOMIZE_REGISTER;
  }
}