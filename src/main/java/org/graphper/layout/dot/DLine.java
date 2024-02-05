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

package org.graphper.layout.dot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.graphper.def.AbstractDirectedEdge;
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;
import org.graphper.api.Line;
import org.graphper.api.LineAttrs;
import org.graphper.api.attributes.LineStyle;

class DLine extends AbstractDirectedEdge<DNode, DLine> {

  private static final long serialVersionUID = -4923098199188113451L;

  private final Line line;

  // The cut value
  private double cutVal;

  // All parallel lines
  private List<DLine> parallelLineRecord;

  // Limitations of network simplicity method between two vertices of an edge
  private int limit;

  private final boolean realTimeLimit;

  // The size of the edge label
  private final FlatPoint labelSize;

  private final LineAttrs lineAttrs;

  DLine(DNode left, DNode right, Line line,
        LineAttrs lineAttrs, double weight, int limit) {
    this(left, right, line, lineAttrs, weight, limit, null);
  }

  DLine(DNode left, DNode right, double weight, int limit, boolean realTimeLimit) {
    this(left, right, null, null, weight, limit, null, realTimeLimit);
  }

  DLine(DNode left, DNode right, Line line,
        LineAttrs lineAttrs, double weight,
        int limit, FlatPoint labelSize) {
    this(left, right, line, lineAttrs, weight, limit, labelSize, false);
  }

  DLine(DNode left, DNode right, Line line,
        LineAttrs lineAttrs, double weight, int limit,
        FlatPoint labelSize, boolean realTimeLimit) {
    super(left, right, weight);

    this.line = line;
    this.limit = limit;
    this.labelSize = labelSize;
    if (line != null) {
      Asserts.nullArgument(lineAttrs, "lineAttrs");
    }
    this.lineAttrs = lineAttrs;
    this.realTimeLimit = realTimeLimit;
  }

  Line getLine() {
    return line;
  }

  LineAttrs lineAttrs() {
    return lineAttrs;
  }

  DNode getLowRankNode() {
    return from().getRankIgnoreModel() < to().getRankIgnoreModel() ? from() : to();
  }

  DNode getLargeRankNode() {
    return from().getRankIgnoreModel() >= to().getRankIgnoreModel() ? from() : to();
  }

  double getCutVal() {
    return cutVal;
  }

  void setCutVal(double cutVal) {
    this.cutVal = cutVal;
  }

  void setLimit(int limit) {
    this.limit = limit;
  }

  /**
   * Edge Length Limits for the Network Simplex Method. Represents how many ranks the edge spans at
   * least.
   *
   * @return Edge Length Limits
   */
  int limit() {
    if (realTimeLimit) {
      return (int) from().rightWidth() + limit + (int) to().leftWidth();
    }
    return limit;
  }

  /**
   * Edge slack, represents the rank difference between two nodes.
   *
   * @return edge slack
   */
  int slack() {
    return to().getRank() - from().getRank();
  }

  /**
   * The length by which the sides need to be reduced to become "tightest".
   *
   * @return length could reduce
   */
  int reduceLen() {
    int slack = Math.abs(slack());

    return slack - limit();
  }

  /**
   * Returns whether it is a parallel edge aggregation edge, which means that the current edge
   * replaces multiple parallel edges.
   *
   * @return <tt>true</tt> if have parallel edges
   */
  boolean isParallelMerge() {
    return CollectionUtils.isNotEmpty(parallelLineRecord);
  }

  /**
   * Returns whether the two nodes of an edge are at the same rank.
   *
   * @return <tt>true</tt> if ranks of two nodes is same
   */
  boolean isSameRank() {
    return from().getRankIgnoreModel() == to().getRankIgnoreModel();
  }

  /**
   * Returns whether the edge is adjacent at the same rank.
   *
   * @return <tt>true</tt> if edge is adjacent at the same rank
   */
  boolean isSameRankAdj() {
    return isSameRank() && Math.abs(from().getRankIndex() - to().getRankIndex()) == 1;
  }

  int getParallelNums() {
    return CollectionUtils.isEmpty(parallelLineRecord) ? 1 : parallelLineRecord.size();
  }

  DLine parallelLine(int no) {
    return CollectionUtils.isEmpty(parallelLineRecord) ? this : parallelLineRecord.get(no);
  }

  void addParallelEdge(DLine edge) {
    if (parallelLineRecord == null) {
      parallelLineRecord = new ArrayList<>(2);
      parallelLineRecord.add(this);
    }

    parallelLineRecord.add(edge);
  }

  boolean isVirtual() {
    return line == null;
  }

  FlatPoint getLabelSize() {
    return labelSize;
  }

  boolean haveLabel() {
    if (isParallelMerge()) {
      for (int i = 0; i < getParallelNums(); i++) {
        DLine l = parallelLine(i);
        if (l == this) {
          if (labelSize != null) {
            return true;
          }
        } else if (l.getLabelSize() != null) {
          return true;
        }
      }

      return false;
    }

    return labelSize != null;
  }

  boolean isReversal() {
    if (isVirtual()) {
      return false;
    }
    return line.tail() == to().getNode();
  }

  boolean isHide() {
    if (isVirtual()) {
      return false;
    }

    return lineAttrs().getStyles().contains(LineStyle.INVIS);
  }

  @Override
  public double weight() {
    if (!isParallelMerge()) {
      return line != null ? line.weight() : weight;
    }

    double w = 0;
    for (int i = 0; i < parallelLineRecord.size(); i++) {
      DLine l = parallelLineRecord.get(i);
      if (l.isVirtual()) {
        continue;
      }

      w += l.lineAttrs().getWeight() == null ? 1D : l.lineAttrs().getWeight();
    }

    return w;
  }

  @Override
  public DLine reverse() {
    return new DLine(right, left, line, lineAttrs,
                     weight, limit, labelSize, realTimeLimit);
  }

  @Override
  public DLine copy() {
    DLine repl = new DLine(left, right, line, lineAttrs,
                           weight, limit, labelSize, realTimeLimit);
    repl.cutVal = cutVal;
    return repl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    DLine line1 = (DLine) o;
    return Double.compare(line1.cutVal, cutVal) == 0 &&
        limit == line1.limit &&
        Objects.equals(line, line1.line);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), line, cutVal, limit);
  }

  @Override
  public String toString() {
    return "{from:" + left + "," +
        "to:" + right + "," +
        "weight:" + weight + "," +
        "limit:" + limit + "," +
        "cutval:" + cutVal + "}";
  }
}
