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
import org.graphper.util.CollectionUtils;

public class ALine<N extends ANode, E extends ALine<N, E>> extends AbstractDirectedEdge<N, E> {

  private static final long serialVersionUID = 7155243474988517017L;
  protected final Line line;

  protected final LineAttrs lineAttrs;

  // All parallel lines
  protected List<E> parallelLineRecord;

  public ALine(N from, N to, Line line, LineAttrs lineAttrs) {
    super(from, to);
    this.line = line;
    this.lineAttrs = lineAttrs;
  }

  public ALine(N from, N to, double weight, Line line, LineAttrs lineAttrs) {
    super(from, to, weight);
    this.line = line;
    this.lineAttrs = lineAttrs;
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
    return line;
  }

  public boolean isVirtual() {
    return line == null;
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

  public E parallelLine(int no) {
    return CollectionUtils.isEmpty(parallelLineRecord) ? (E) this : parallelLineRecord.get(no);
  }

  public void addParallelEdge(E edge) {
    if (parallelLineRecord == null) {
      parallelLineRecord = new ArrayList<>(2);
      parallelLineRecord.add((E) this);
    }

    parallelLineRecord.add(edge);
  }

  public LineAttrs lineAttrs() {
    return lineAttrs;
  }

  public boolean isHide() {
    if (isVirtual() || lineAttrs() == null) {
      return true;
    }

    return lineAttrs().getStyles().contains(LineStyle.INVIS);
  }
}
