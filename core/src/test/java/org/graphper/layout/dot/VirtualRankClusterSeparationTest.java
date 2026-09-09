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

import java.util.HashMap;
import java.util.Map;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.def.DedirectedEdgeGraph;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.dot.RankContent.RankNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VirtualRankClusterSeparationTest {

  @Test
  public void virtualOnlyRankStillSeparatesAdjacentClusters() {
    Node leftMember = Node.builder().id("left_member").build();
    Node rightMember = Node.builder().id("right_member").build();
    Cluster leftCluster = Cluster.builder().id("left").addNode(leftMember).build();
    Cluster rightCluster = Cluster.builder().id("right").addNode(rightMember).build();
    Graphviz graphviz = Graphviz.digraph().cluster(leftCluster).cluster(rightCluster).build();

    DrawGraph drawGraph = new DrawGraph(graphviz);
    ClusterDrawProp leftProp = new ClusterDrawProp(leftCluster);
    ClusterDrawProp rightProp = new ClusterDrawProp(rightCluster);
    drawGraph.clusterPut(leftCluster, leftProp);
    drawGraph.clusterPut(rightCluster, rightProp);

    Map<Node, DNode> nodeMap = new HashMap<>();
    DotDigraph layoutGraph = new DotDigraph(4, graphviz, nodeMap);
    layoutGraph.add(realNode(leftMember), leftCluster);
    layoutGraph.add(realNode(rightMember), rightCluster);

    DotAttachment attachment = new DotAttachment(layoutGraph, drawGraph, nodeMap);
    attachment.markHaveCluster();

    DNode leftVirtual = virtualNode(leftCluster);
    DNode rightVirtual = virtualNode(rightCluster);
    DedirectedEdgeGraph<DNode, DLine> proxy = new DedirectedEdgeGraph<>(2);
    proxy.add(leftVirtual);
    proxy.add(rightVirtual);

    RankContent ranks = new RankContent(proxy, 50, false, null);
    RankNode rank = ranks.get(1);
    rank.set(0, leftVirtual);
    rank.set(1, rightVirtual);
    leftVirtual.setRankIndex(0);
    rightVirtual.setRankIndex(1);
    Assertions.assertTrue(rank.noNormalNode());

    new ClassicCoordinate(1000, ranks, attachment, proxy);

    Assertions.assertTrue(leftProp.getRightBorder() + 12 <= rightProp.getLeftBorder(),
                          "virtual-only rank lost the adjacent cluster border limit edge");
  }

  @Test
  public void longEdgeLabelNodeStillExpandsItsCluster() {
    Node tail = Node.builder().id("tail").build();
    Node head = Node.builder().id("head").build();
    Cluster cluster = Cluster.builder().id("cluster").addNode(tail).build();
    Graphviz graphviz = Graphviz.digraph().cluster(cluster).addNode(head).build();

    DrawGraph drawGraph = new DrawGraph(graphviz);
    ClusterDrawProp clusterProp = new ClusterDrawProp(cluster);
    drawGraph.clusterPut(cluster, clusterProp);

    Map<Node, DNode> nodeMap = new HashMap<>();
    DotDigraph layoutGraph = new DotDigraph(2, graphviz, nodeMap);
    layoutGraph.add(realNode(tail), cluster);
    layoutGraph.add(realNode(head), graphviz);

    DotAttachment attachment = new DotAttachment(layoutGraph, drawGraph, nodeMap);
    attachment.markHaveCluster();

    DNode labelNode = virtualNode(cluster);
    labelNode.setLabelLine(Line.builder(tail, head).label("long edge label").build());
    labelNode.setWidth(200);
    DedirectedEdgeGraph<DNode, DLine> proxy = new DedirectedEdgeGraph<>(1);
    proxy.add(labelNode);

    RankContent ranks = new RankContent(proxy, 50, false, null);
    new ClassicCoordinate(1000, ranks, attachment, proxy);

    Assertions.assertTrue(clusterProp.getRightBorder() - clusterProp.getLeftBorder() >= 220,
                          "long-edge label node did not expand its cluster");
  }

  @Test
  public void displayingLabelsAreNotRoutingVirtualNodes() {
    Node tail = Node.builder().id("tail").build();
    Node head = Node.builder().id("head").build();
    Graphviz graphviz = Graphviz.digraph().addNode(tail, head).build();

    DNode routing = virtualNode(graphviz);
    Assertions.assertTrue(routing.isRoutingVirtual());

    routing.setLabelLine(Line.builder(tail, head).label("label").build());
    Assertions.assertTrue(routing.isLabelNode());
    Assertions.assertFalse(routing.isRoutingVirtual());

    DNode flatLabel = new DNode(null, 80, 20, 20,
                                new DLine(realNode(tail), realNode(head), null, 1, 1));
    Assertions.assertTrue(flatLabel.isFlatLabelNode());
    Assertions.assertFalse(flatLabel.isRoutingVirtual());
  }

  @Test
  public void longEdgeLabelNodeStillKeepsDistanceFromAdjacentCluster() {
    Node member = Node.builder().id("member").build();
    Node head = Node.builder().id("head").build();
    Cluster cluster = Cluster.builder().id("cluster").addNode(member).build();
    Graphviz graphviz = Graphviz.digraph().cluster(cluster).addNode(head).build();

    DrawGraph drawGraph = new DrawGraph(graphviz);
    ClusterDrawProp clusterProp = new ClusterDrawProp(cluster);
    drawGraph.clusterPut(cluster, clusterProp);

    Map<Node, DNode> nodeMap = new HashMap<>();
    DotDigraph layoutGraph = new DotDigraph(2, graphviz, nodeMap);
    DNode memberNode = realNode(member);
    layoutGraph.add(memberNode, cluster);
    layoutGraph.add(realNode(head), graphviz);

    DotAttachment attachment = new DotAttachment(layoutGraph, drawGraph, nodeMap);
    attachment.markHaveCluster();

    memberNode.setRank(1);
    DNode labelNode = virtualNode(graphviz);
    labelNode.setLabelLine(Line.builder(member, head).label("label").build());
    labelNode.setWidth(80);
    DedirectedEdgeGraph<DNode, DLine> proxy = new DedirectedEdgeGraph<>(2);
    proxy.add(memberNode);
    proxy.add(labelNode);

    RankContent ranks = new RankContent(proxy, 50, false, null);
    RankNode rank = ranks.get(1);
    rank.set(0, memberNode);
    rank.set(1, labelNode);
    memberNode.setRankIndex(0);
    labelNode.setRankIndex(1);

    new ClassicCoordinate(1000, ranks, attachment, proxy);

    Assertions.assertTrue(clusterProp.getRightBorder() + 18 <= labelNode.getX(),
                          "long-edge label node lost its adjacent cluster border limit edge");
  }

  private DNode realNode(Node node) {
    return new DNode(new NodeDrawProp(node, node.nodeAttrs()), 20, 20, 20);
  }

  private DNode virtualNode(GraphContainer container) {
    DNode node = DNode.newRoutingVirtualNode(20, container);
    node.setRank(1);
    return node;
  }
}
