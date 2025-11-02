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

import java.util.ArrayList;
import java.util.List;
import org.graphper.api.Line;
import org.graphper.api.LineAttrs;
import org.graphper.api.attributes.LineStyle;
import org.graphper.def.AbstractDirectedEdge;
import org.graphper.draw.LineDrawProp;
import org.graphper.util.CollectionUtils;

public class ALine<N extends ANode, E extends ALine<N, E>> extends AbstractDirectedEdge<N, E> {

  private static final long serialVersionUID = 7155243474988517017L;
  protected final LineDrawProp lineDrawProp;

  // All parallel lines
  protected List<E> parallelLineRecord;

  public ALine(N from, N to, LineDrawProp lineDrawProp) {
    super(from, to);
    this.lineDrawProp = lineDrawProp;
  }

  public ALine(N from, N to, double weight, LineDrawProp lineDrawProp) {
    super(from, to, weight);
    this.lineDrawProp = lineDrawProp;
  }

  @Override
  public E reverse() {
    return null;
  }

  @Override
  public E copy() {
    return null;
  }

  public Line getLine() {
    return lineDrawProp != null ? lineDrawProp.getLine() : null;
  }

  public boolean isVirtual() {
    return lineDrawProp == null;
  }

  /**
   * Returns whether it is a parallel edge aggregation edge, which means that the current edge
   * replaces multiple parallel edges.
   *
   * @return <tt>true</tt> if have parallel edges
   */
  public boolean isParallelMerge() {
    return CollectionUtils.isNotEmpty(parallelLineRecord);
  }

  public int getParallelNums() {
    return CollectionUtils.isEmpty(parallelLineRecord) ? 1 : parallelLineRecord.size();
  }

  @SuppressWarnings("unchecked")
  public E parallelLine(int no) {
    return CollectionUtils.isEmpty(parallelLineRecord) ? (E) this : parallelLineRecord.get(no);
  }

  @SuppressWarnings("unchecked")
  public void addParallelEdge(E edge) {
    if (parallelLineRecord == null) {
      parallelLineRecord = new ArrayList<>(2);
      parallelLineRecord.add((E) this);
    }

    parallelLineRecord.add(edge);
  }

  public LineAttrs lineAttrs() {
    return lineDrawProp != null ? lineDrawProp.lineAttrs() : null;
  }

  public boolean isHide() {
    if (isVirtual() || lineAttrs() == null) {
      return true;
    }

    return lineAttrs().getStyles().contains(LineStyle.INVIS);
  }

  public LineDrawProp getLineDrawProp() {
    return lineDrawProp;
  }
}
