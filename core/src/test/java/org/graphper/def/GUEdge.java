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

package org.graphper.def;

public class GUEdge extends AbstractUndirectedEdge<GNode, GUEdge> {

  private static final long serialVersionUID = -8287058500544888071L;

  protected GUEdge(GNode left, GNode right) {
    super(left, right);
  }

  @Override
  public GUEdge copy() {
    return clone();
  }

  public static GUEdge newEdge(GNode left, GNode right) {
    return new GUEdge(left, right);
  }

  @Override
  protected GUEdge clone() {
    try {
      return (GUEdge) super.clone();
    } catch (CloneNotSupportedException e) {
      return new GUEdge(either(), other(either()));
    }
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj) && obj.getClass() == GUEdge.class;
  }

  @Override
  public int hashCode() {
    return super.hashCode() + GUEdge.class.hashCode();
  }
}
