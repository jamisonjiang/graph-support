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

import java.io.Serializable;

public class GEdge extends AbstractDirectedEdge<GNode, GEdge> implements Cloneable, Serializable {

  private static final long serialVersionUID = 1079080593981114004L;

  protected GEdge(GNode from, GNode to) {
    super(from, to);
  }

  @Override
  public GEdge reverse() {
    return new GEdge(to(), from());
  }

  @Override
  public GEdge copy() {
    return clone();
  }

  public static GEdge newEdge(GNode from, GNode to) {
    return new GEdge(from, to);
  }

  @Override
  protected GEdge clone() {
    try {
      return (GEdge) super.clone();
    } catch (CloneNotSupportedException e) {
      return new GEdge(from(), to());
    }
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj) && obj.getClass() == GEdge.class;
  }

  @Override
  public int hashCode() {
    return super.hashCode() + GEdge.class.hashCode();
  }
}
