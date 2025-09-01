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

import java.util.Objects;
import org.graphper.def.FlatPoint;
import org.graphper.draw.LineDrawProp;
import org.graphper.layout.ALine;
import org.graphper.util.Asserts;

class DLine extends ALine<DNode, DLine> {

  private static final long serialVersionUID = -4923098199188113451L;

  // The cut value
  private double cutVal;

  // Limitations of network simplicity method between two vertices of an edge
  private int limit;

  private final boolean realTimeLimit;

  // The size of the edge label
  private final FlatPoint labelSize;

  DLine(DNode left, DNode right, LineDrawProp lineDrawProp, double weight, int limit) {
    this(left, right, lineDrawProp, weight, limit, null);
  }

  DLine(DNode left, DNode right, double weight, int limit, boolean realTimeLimit) {
    this(left, right, null, weight, limit, null, realTimeLimit);
  }

  DLine(DNode left, DNode right, LineDrawProp lineDrawProp, double weight,
        int limit, FlatPoint labelSize) {
    this(left, right, lineDrawProp, weight, limit, labelSize, false);
  }

  DLine(DNode left, DNode right, LineDrawProp lineDrawProp, double weight, int limit,
        FlatPoint labelSize, boolean realTimeLimit) {
    super(left, right, weight, lineDrawProp);

    this.limit = limit;
    this.labelSize = labelSize;
    if (lineDrawProp != null) {
      Asserts.nullArgument(lineDrawProp.lineAttrs(), "lineAttrs");
    }
    this.realTimeLimit = realTimeLimit;
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

  @Override
  public double weight() {
    if (!isParallelMerge()) {
      return getLine() != null ? getLine().weight() : weight;
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
    return new DLine(right, left, lineDrawProp,
                     weight, limit, labelSize, realTimeLimit);
  }

  @Override
  public DLine copy() {
    DLine repl = new DLine(left, right, lineDrawProp,
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
        Objects.equals(getLine(), line1.getLine());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getLine(), cutVal, limit);
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
