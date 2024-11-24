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

import java.io.Serializable;
import org.graphper.api.Assemble;
import org.graphper.api.Cluster;
import org.graphper.api.attributes.ClusterShape;
import org.graphper.api.attributes.ClusterStyle;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;

/**
 * Cluster's rendering description object.
 *
 * @author Jamison Jiang
 */
public class ClusterDrawProp extends ContainerDrawProp implements Serializable {

  private static final long serialVersionUID = -1571306141541457089L;

  private int clusterNo;

  private FlatPoint margin;

  private final Cluster cluster;

  private ClusterShape clusterShape;

  public ClusterDrawProp(Cluster cluster) {
    Asserts.nullArgument(cluster, "cluster");
    this.cluster = cluster;
    this.clusterShape = cluster.clusterAttrs().getShape();
    this.clusterShape = this.clusterShape.post(cluster.clusterAttrs());
    convertTable(cluster.clusterAttrs().getTable());
  }

  /**
   * Returns current cluster.
   *
   * @return current cluster
   */
  public Cluster getCluster() {
    return cluster;
  }

  /**
   * Returns the number of current cluster, this is set by the system according to the order of the
   * cluster.
   *
   * @return number of current cluster
   */
  public int getClusterNo() {
    return clusterNo;
  }

  /**
   * Set cluster number.
   *
   * @param clusterNo cluster number
   */
  public void setClusterNo(int clusterNo) {
    this.clusterNo = clusterNo;
  }

  public void setMargin(FlatPoint margin) {
    this.margin = margin;
  }

  @Override
  public boolean containsRounded() {
    return cluster.clusterAttrs().getStyles().contains(ClusterStyle.ROUNDED);
  }

  @Override
  public Labelloc labelloc() {
    return cluster.clusterAttrs().getLabelloc();
  }

  @Override
  public Labeljust labeljust() {
    return cluster.clusterAttrs().getLabeljust();
  }

  @Override
  public FlatPoint margin() {
    if (margin != null) {
      return margin;
    }
    return cluster.clusterAttrs().getMargin();
  }

  @Override
  public String containerId() {
    return cluster.id();
  }

  @Override
  public Assemble assemble() {
    return cluster.clusterAttrs().getAssemble();
  }

  @Override
  public ClusterShape shapeProp() {
    return clusterShape != null ? clusterShape : cluster.clusterAttrs().getShape();
  }
}
