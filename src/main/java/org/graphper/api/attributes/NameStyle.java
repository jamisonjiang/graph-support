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
import java.util.Objects;

class NameStyle implements NodeStyle, LineStyle, ClusterStyle, Serializable {
  private static final long serialVersionUID = -5828913514471808562L;

  static final NameStyle DASHED = new NameStyle("dashed");
  static final NameStyle DOTTED = new NameStyle("dotted");
  static final NameStyle SOLID = new NameStyle("solid");
  static final NameStyle INVIS = new NameStyle("invis");
  static final NameStyle BOLD = new NameStyle("bold");
  static final NameStyle ROUNDED = new NameStyle("rounded");

  private final String name;

  NameStyle(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NameStyle nameStyle = (NameStyle) o;
    return Objects.equals(name, nameStyle.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String getName() {
    return name;
  }
}
