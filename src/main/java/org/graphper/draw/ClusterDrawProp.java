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
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;
import org.graphper.api.Cluster;
import org.graphper.api.attributes.Labelloc;

/**
 * Cluster's rendering description object.
 *
 * @author Jamison Jiang
 */
public class ClusterDrawProp extends ContainerDrawProp implements Serializable {

  private static final long serialVersionUID = -1571306141541457089L;

  private int clusterNo;

  private final Cluster cluster;

  public ClusterDrawProp(Cluster cluster) {
    Asserts.nullArgument(cluster, "cluster");
    this.cluster = cluster;
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

  @Override
  protected Labelloc labelloc() {
    return cluster.clusterAttrs().getLabelloc();
  }

  @Override
  protected FlatPoint margin() {
    return cluster.clusterAttrs().getMargin();
  }

  @Override
  protected String containerId() {
    return cluster.id();
  }
}
