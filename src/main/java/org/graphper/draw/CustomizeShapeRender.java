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
import org.graphper.draw.svg.SvgBrush;
import org.graphper.util.Asserts;

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
 * @author Jamison Jiang
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
   * Draws node shapes within an SVG structure.
   * <p>
   * The node shape can consist of multiple {@link org.graphper.draw.svg.Element} objects, forming a
   * complete SVG structure. Each element represents a distinct part of the shape, such as the
   * outline, internal details.
   *
   * @param nodeBrush    the SVG brush used for drawing, which provides utility methods for
   *                     interacting with the SVG structure
   * @param nodeDrawProp the properties of the node to be drawn, such as size, color, and position
   */
  public void drawNodeSvg(SvgBrush nodeBrush, NodeDrawProp nodeDrawProp) {
  }

  /**
   * Draws cluster shapes within an SVG structure.
   *
   * @param clusterBrush    the SVG brush used for drawing, providing functionality to add and
   *                        modify SVG elements
   * @param clusterDrawProp the properties of the cluster to be drawn, including size, color,
   *                        boundaries, and labels
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