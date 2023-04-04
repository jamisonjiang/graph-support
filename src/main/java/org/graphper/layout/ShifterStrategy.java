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

package org.graphper.layout;

import org.graphper.def.FlatPoint;
import org.graphper.layout.dot.RouterBox;
import org.graphper.draw.ContainerDrawProp;

/**
 * A translation strategy for basic elements.
 *
 * @author Jamison Jiang
 */
public interface ShifterStrategy {

  /**
   * The point movement strategy.
   *
   * @param point point to move
   */
  void movePoint(FlatPoint point);

  /**
   * The {@link ContainerDrawProp} movement strategy.
   *
   * @param containerDrawProp ContainerDrawProp to move
   */
  void moveContainerDrawProp(ContainerDrawProp containerDrawProp);

  /**
   * The {@link RouterBox} movement strategy.
   *
   * @param routerBox RouterBox to move
   */
  void moveBox(RouterBox routerBox);
}
