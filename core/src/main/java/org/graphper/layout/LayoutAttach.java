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

import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.draw.DrawGraph;

public abstract class LayoutAttach {
  private boolean haveClusters;

  protected final DrawGraph drawGraph;

  public LayoutAttach(DrawGraph drawGraph) {
    this.drawGraph = drawGraph;
  }

  public void markHaveCluster() {
    this.haveClusters = true;
  }

  public boolean haveClusters() {
    return haveClusters;
  }

  public GraphContainer clusterDirectContainer(GraphContainer parent, ANode node) {
    return clusterDirectContainer(getGraphviz(), parent, node);
  }

  public static GraphContainer clusterDirectContainer(Graphviz graphviz,
                                               GraphContainer parent, ANode node) {
    if (node.getContainer() == parent || graphviz == null) {
      return null;
    }

    GraphContainer father;
    GraphContainer current = node.getContainer();
    while ((father = graphviz.effectiveFather(current)) != parent && father != null) {
      current = father;
    }

    return father == parent ? current : null;
  }

  public DrawGraph getDrawGraph() {
    return drawGraph;
  }

  public Graphviz getGraphviz() {
    return drawGraph.getGraphviz();
  }

  @SuppressWarnings("rawtypes")
  public abstract LayoutGraph getLayoutGraph();
}
