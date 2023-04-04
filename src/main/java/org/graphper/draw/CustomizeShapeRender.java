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
 * Renderer for custom node shapes. No matter how many types of rendering methods are supported in
 * the future, brushes corresponding to the rendering type will be provided for editing in this
 * method.
 *
 * <p>There are two ways to register a custom node shape renderer, the first is to manually call
 * the {@link #register(CustomizeShapeRender)} method, and the second is to register using SPI.
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
   * Register a custom node shape renderer. If the shape has the same name, the priority
   * registration will take effect first, you can use {@link #registered(String)} to check whether
   * the corresponding shape has been registered.
   *
   * @param customizeShapeRender customize renderer
   * @throws NullPointerException     null render
   * @throws IllegalArgumentException null shape name
   */
  public static void register(CustomizeShapeRender customizeShapeRender) {
    Asserts.nullArgument(customizeShapeRender, "custimizeNodeShape");
    Asserts.illegalArgument(customizeShapeRender.getShapeName() == null,
                            "CustimizeNodeShape can not return null shapeName");
    customizeNodeShapeMap().computeIfAbsent(customizeShapeRender.getShapeName(),
                                            s -> customizeShapeRender);
  }

  /**
   * Returns the registered node shape renderer for the specified shape.
   *
   * @param shapeName shape name
   * @return the node shape renderer
   */
  public static CustomizeShapeRender getCustomizeShapeRender(String shapeName) {
    if (CUSTOMIZE_REGISTER == null) {
      return null;
    }
    return CUSTOMIZE_REGISTER.get(shapeName);
  }

  /**
   * Returns the shape whether registered.
   *
   * @param shapeName shape name
   * @return <tt>true</tt> if the shape have been registered
   */
  public static boolean registered(String shapeName) {
    return getCustomizeShapeRender(shapeName) != null;
  }

  // --------------------------------- Customize draw method ---------------------------------

  /**
   * Returns the node shape name.
   *
   * @return node shape name
   */
  public abstract String getShapeName();

  /**
   * Draw node shapes under the svg structure.
   *
   * @param nodeBrush    svg brush for drawing
   * @param nodeDrawProp node draw properties
   */
  public abstract void drawSvg(SvgBrush nodeBrush, NodeDrawProp nodeDrawProp);

  // --------------------------------- private method ---------------------------------

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
