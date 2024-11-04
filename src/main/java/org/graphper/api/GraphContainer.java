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

package org.graphper.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.graphper.api.Subgraph.IntegrationSubgraphBuilder;
import org.graphper.def.UnaryConcatIterable;
import org.graphper.def.VertexIndex;
import org.graphper.util.Asserts;
import org.graphper.util.ClassUtils;
import org.graphper.util.CollectionUtils;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Cluster.IntegrationClusterBuilder;

/**
 * A common container for graphs, clusters, and subgraphs. {@code GraphContainer} has a hierarchical
 * structure, and there can be <tt>n</tt> subgraphs or clusters in the current container.The
 * elements (nodes or edges) added by the current container and the elements added by all
 * sub-containers of any depth are the elements inside the current container. Use {@link #nodes()}
 * and {@link #lines()} to get all the elements inside the container.
 *
 * <p>{@code GraphContainer} is constructed using {@link GraphContainerBuilder}, which is immutable
 * after construction, and a {@code GraphContainer} can act as a sub-container among multiple
 * {@code GraphContainers} at the same time.
 *
 * @author Jamison Jiang
 * @see Cluster
 * @see Subgraph
 * @see Graphviz
 */
@SuppressWarnings("all")
public abstract class GraphContainer extends VertexIndex {

  // Container Id
  protected String id;

  // ---------------------- basic properties ----------------------
  // Subgraphs that directly belong to the current container
  protected volatile List<Subgraph> subgraphs;

  // Clusters that directly belong to the current container
  protected volatile List<Cluster> clusters;

  // Lines that directly belong to the current container
  protected volatile Set<Line> lines;

  // Nodes that directly belong to the current container
  protected volatile Set<Node> nodes;

  // The node template attribute value
  protected volatile Map<String, Object> nodeAttrsMap;

  // The line template attribute value
  protected volatile Map<String, Object> lineAttrsMap;

  /**
   * Returns the container id.
   *
   * @return the container id
   */
  public String id() {
    return id;
  }

  /**
   * Returns whether the container is {@link Graphviz}。
   *
   * @return <tt>true</tt> if current container is {@code Graphviz}
   */
  public boolean isGraphviz() {
    return this instanceof Graphviz;
  }

  /**
   * Returns whether the container is {@link Subgraph}。
   *
   * @return <tt>true</tt> if current container is {@code Subgraph}
   */
  public boolean isSubgraph() {
    return this instanceof Subgraph;
  }

  /**
   * Returns whether the container is {@link Cluster}。
   *
   * @return <tt>true</tt> if current container is {@code Cluster}
   */
  public boolean isCluster() {
    return this instanceof Cluster;
  }

  /**
   * Returns whether the container is empty, empty means does not contain any nodes and lines. This
   * means recursively starting from the current container, looking for all {@link Subgraph} and
   * {@link Cluster} that can be traversed, none of which contain any nodes and lines.
   *
   * @return <tt>true</tt> if the container is empty
   */
  public boolean isEmpty() {
    if (absoluteEmpty()) {
      return true;
    }

    // Recursively all subgraphs
    for (Subgraph subgraph : subgraphs()) {
      if (!subgraph.isEmpty()) {
        return false;
      }
    }

    // Recursively all clusters
    for (Cluster cluster : clusters()) {
      if (!cluster.isEmpty()) {
        return false;
      }
    }

    return CollectionUtils.isEmpty(nodes) && CollectionUtils.isEmpty(lines);
  }

  /**
   * Determines whether the container is absolutely empty. This method is non-recursive.
   *
   * @return container is absolutely empty
   */
  public boolean absoluteEmpty() {
    return CollectionUtils.isEmpty(subgraphs) && CollectionUtils.isEmpty(clusters)
        && CollectionUtils.isEmpty(nodes) && CollectionUtils.isEmpty(lines);
  }

  /**
   * Returns all subgraphs of the current graph.
   *
   * @return all subgraphs
   */
  public List<Subgraph> subgraphs() {
    return CollectionUtils.isNotEmpty(subgraphs)
        ? Collections.unmodifiableList(subgraphs)
        : Collections.emptyList();
  }

  /**
   * Returns all clusters of the current graph.
   *
   * @return all clusters
   */
  public List<Cluster> clusters() {
    return CollectionUtils.isNotEmpty(clusters)
        ? Collections.unmodifiableList(clusters)
        : Collections.emptyList();
  }

  /**
   * Returns all nodes inside the current container, including all {@link Subgraph}s and
   * {@link Cluster} of any depths. If a node exists in multiple subgraphs/clusters, this node will
   * be iterated repeatedly.
   *
   * @return all nodes
   */
  public Iterable<Node> nodes() {
    if (CollectionUtils.isEmpty(subgraphs) && CollectionUtils.isEmpty(clusters)) {
      return CollectionUtils.isEmpty(nodes)
          ? Collections.emptyList()
          : Collections.unmodifiableSet(nodes);
    }

    List<Iterable<Node>> iterables = null;
    if (CollectionUtils.isNotEmpty(nodes)) {
      iterables = new ArrayList<>(1);
      iterables.add(nodes);
    }
    for (Subgraph subgraph : subgraphs()) {
      if (iterables == null) {
        iterables = new ArrayList<>(1);
      }
      iterables.add(subgraph.nodes());
    }
    for (Cluster cluster : clusters()) {
      if (iterables == null) {
        iterables = new ArrayList<>(1);
      }
      iterables.add(cluster.nodes());
    }

    if (CollectionUtils.isEmpty(iterables)) {
      return Collections.emptyList();
    }

    return new UnaryConcatIterable<>(iterables);
  }

  /**
   * Returns the node added directly in the container, excluding nodes in any child elements.
   *
   * @return all directly nodes
   */
  public Set<Node> directNodes() {
    return CollectionUtils.isEmpty(nodes)
        ? Collections.emptySet()
        : Collections.unmodifiableSet(nodes);
  }

  /**
   * Returns node number inside the current container, including all {@link Subgraph}s and
   * {@link Cluster} of any depths. If a node exists in multiple subgraphs/clusters, this node will
   * be counted repeatedly.
   *
   * @return node number
   */
  public int nodeNum() {
    int n = CollectionUtils.isNotEmpty(nodes) ? nodes.size() : 0;
    for (Subgraph subgraph : subgraphs()) {
      n += subgraph.nodeNum();
    }
    for (Cluster cluster : clusters()) {
      n += cluster.nodeNum();
    }
    return n;
  }

  /**
   * Returns all lines inside the current container, including all {@link Subgraph}s and
   * {@link Cluster} of any depths. If a line exists in multiple subgraphs/clusters, this line will
   * be iterated repeatedly.
   *
   * @return all lines
   */
  public Iterable<Line> lines() {
    if (CollectionUtils.isEmpty(subgraphs) && CollectionUtils.isEmpty(clusters)) {
      return CollectionUtils.isEmpty(lines)
          ? Collections.emptyList()
          : Collections.unmodifiableSet(lines);
    }
    List<Iterable<Line>> iterables = null;
    if (CollectionUtils.isNotEmpty(lines)) {
      iterables = new ArrayList<>(1);
      iterables.add(lines);
    }
    for (Subgraph subgraph : subgraphs()) {
      if (iterables == null) {
        iterables = new ArrayList<>(1);
      }
      iterables.add(subgraph.lines());
    }
    for (Cluster cluster : clusters()) {
      if (iterables == null) {
        iterables = new ArrayList<>(1);
      }
      iterables.add(cluster.lines());
    }

    if (CollectionUtils.isEmpty(iterables)) {
      return Collections.emptyList();
    }

    return new UnaryConcatIterable<>(iterables);
  }

  /**
   * Returns the lines added directly in the container, excluding lines in any child elements.
   *
   * @return all directly lines
   */
  public Set<Line> directLines() {
    return CollectionUtils.isEmpty(lines)
        ? Collections.emptySet()
        : Collections.unmodifiableSet(lines);
  }

  /**
   * Returns line number inside the current container, including all {@link Subgraph}s and
   * {@link Cluster} of any depths. If a line exists in multiple subgraphs/clusters, this node will
   * be counted repeatedly.
   *
   * @return line number
   */
  public int lineNum() {
    int n = CollectionUtils.isNotEmpty(lines) ? lines.size() : 0;
    for (Subgraph subgraph : subgraphs()) {
      n += subgraph.lineNum();
    }
    for (Cluster cluster : clusters()) {
      n += cluster.lineNum();
    }
    return n;
  }

  /**
   * Returns whether the node is in the current container.
   *
   * @param node the detected node
   * @return <tt>true</tt> if node in current container
   */
  public boolean containsNode(Node node) {
    if (CollectionUtils.isNotEmpty(nodes) && nodes.contains(node)) {
      return true;
    }

    for (Subgraph subgraph : subgraphs()) {
      if (subgraph.containsNode(node)) {
        return true;
      }
    }

    for (Cluster cluster : clusters()) {
      if (cluster.containsNode(node)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns whether the line is in the current container.
   *
   * @param line the detected line
   * @return <tt>true</tt> if line in current container
   */
  public boolean containsLine(Line line) {
    if (CollectionUtils.isNotEmpty(lines) && lines.contains(line)) {
      return true;
    }

    for (Subgraph subgraph : subgraphs()) {
      if (subgraph.containsLine(line)) {
        return true;
      }
    }

    for (Cluster cluster : clusters()) {
      if (cluster.containsLine(line)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns whether the container is in the current container.
   *
   * @param container graph container
   * @return <tt>true</tt> if container in current container
   */
  public boolean containsContainer(GraphContainer container) {
    if (container == null || container == this) {
      return false;
    }

    for (Subgraph subgraph : subgraphs()) {
      if (subgraph == container || subgraph.containsContainer(container)) {
        return true;
      }
    }

    for (Cluster cluster : clusters()) {
      if (cluster == container || cluster.containsContainer(container)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns whether the current container is transparent, which means it is purely redundant on the
   * container's recursive tree, bringing no features.
   *
   * @return <tt>true</tt> if current container is transparent
   */
  public boolean isTransparent() {
    return this instanceof Subgraph && ((Subgraph) this).getRank() == null;
  }

  /**
   * Returns the template value of the field corresponding to {@link Node#nodeAttrs()} in the
   * current container.
   *
   * @param fieldName field name of node
   * @return node template attribute value
   */
  public Object getNodeAttr(String fieldName) {
    if (nodeAttrsMap == null) {
      return null;
    }
    return nodeAttrsMap.get(fieldName);
  }

  /**
   * Returns the template value of the field corresponding to {@link Line#lineAttrs()} in the
   * current container.
   *
   * @param fieldName field name of line
   * @return line template attribute value
   */
  public Object getLineAttr(String fieldName) {
    if (lineAttrsMap == null) {
      return null;
    }
    return lineAttrsMap.get(fieldName);
  }

  /**
   * Returns whether there is a node attribute template.
   *
   * @return <tt>true</tt> if have node attribute template
   */
  public boolean haveNodeTemp() {
    return nodeAttrsMap != null;
  }

  /**
   * Returns whether there is a line attribute template.
   *
   * @return <tt>true</tt> if have line attribute template
   */
  public boolean haveLineTemp() {
    return lineAttrsMap != null;
  }

  /**
   * Returns whether there is clusters.
   *
   * @return <tt>true</tt> if have clusters
   */
  public boolean haveChildCluster() {
    return CollectionUtils.isNotEmpty(clusters);
  }

  // ---------------------------------- methods provided to builders ----------------------------------

  synchronized void addSubgraph(Subgraph subgraph) {
    Asserts.nullArgument(subgraph, "subgraph");
    if (subgraphs == null) {
      subgraphs = new ArrayList<>();
    }

    subgraphs.add(subgraph);
  }

  synchronized void addCluster(Cluster cluster) {
    Asserts.nullArgument(cluster, "cluster");
    if (clusters == null) {
      clusters = new ArrayList<>();
    }

    clusters.add(cluster);
  }

  synchronized void addLine(Line line) {
    Asserts.nullArgument(line, "line");
    if (lines == null) {
      lines = new TreeSet<>();
    }

    lines.add(line);
  }

  synchronized void addNode(Node node) {
    Asserts.nullArgument(node, "node");
    if (nodes == null) {
      nodes = new TreeSet<>();
    }

    nodes.add(node);
  }

  // ---------------------------------- Graph Container Builder ----------------------------------

  /**
   * Root builder for {@code GraphContainer}.
   *
   * @param <G> the type of {@code GraphContainer}
   * @param <B> the type of {@code GraphContainerBuilder}
   * @see Cluster.ClusterBuilder
   * @see Subgraph.SubgraphBuilder
   * @see GraphvizBuilder
   * @see Subgraph.IntegrationSubgraphBuilder
   * @see Cluster.IntegrationClusterBuilder
   */
  public abstract static class GraphContainerBuilder<G extends GraphContainer, B extends GraphContainerBuilder<G, B>> {

    // The node template attribute value
    protected volatile Map<String, Object> nodeAttrsMap;

    // The line template attribute value
    protected volatile Map<String, Object> lineAttrsMap;

    // A container to hold templates
    protected volatile G container;

    /**
     * Returns the current builder, use this mainly for generic constraints.
     *
     * @return this object
     */
    protected abstract B self();

    /**
     * Create a graph container.
     *
     * @return graph container
     */
    protected abstract G newContainer();

    /**
     * Set graph container id, if you don't set it manually, it will be assigned automatically when
     * rendering, it is used to judge the uniqueness of the graph container.
     *
     * @param id container id
     * @return container builder
     */
    public B id(String id) {
      initContainer().id = id;
      return self();
    }

    /**
     * Add a {@link Subgraph} to the current container, and the corresponding <tt>dot script</tt>
     * semantics are as follows:
     * <pre>{@code
     * digraph G {
     *     // Subgraph in Graphviz
     *     subgraph {
     *         rank=same
     *         a->b
     *
     *         // Subgraph inside Subgraph
     *         {
     *             c->d
     *         }
     *     }
     *
     *     subgraph cluster_0 {
     *         // Subgraph in Cluster
     *         {
     *             rank=max
     *             e->f
     *         }
     *     }
     * }}
     * </pre>
     * The content of the above script converts into an example of Java code:
     * <pre>{@code
     *     Graphviz.digraph()
     *         // Subgraph in Graphviz
     *         .subgraph(
     *             Subgraph.builder()
     *                 .rank(Rank.SAME)
     *                 .addLine(a, b)
     *                 // Subgraph inside Subgraph
     *                 .subgraph(
     *                     Subgraph.builder()
     *                         .addLine(c, d)
     *                         .build()
     *                 )
     *                 .build()
     *         )
     *         .cluster(
     *             Cluster.builder()
     *                 // Subgraph in Cluster
     *                 .subgraph(
     *                     Subgraph.builder()
     *                         .rank(Rank.MAX)
     *                         .addLine(e, f)
     *                         .build()
     *                 )
     *                 .build()
     *         )
     *         .build();
     * }
     * </pre>
     *
     * @param subgraph subgraph
     * @return container builder
     * @throws NullPointerException null subgraph
     */
    public B subgraph(Subgraph subgraph) {
      Asserts.nullArgument(subgraph, "subgraph");
      initContainer().addSubgraph(subgraph);
      return self();
    }

    /**
     * Add a {@link Cluster} to the current container, and the corresponding <tt>dot script</tt>
     * semantics are as follows:
     * <pre>{@code
     * digraph G {
     *     {
     *         rank=same
     *         a->b
     *
     *         // Cluster inside Subgraph
     *         subgraph cluster_0 {
     *             c->d
     *         }
     *     }
     *
     *     // Cluster in Graphviz
     *     subgraph cluster_1 {
     *         // Cluster in Cluster
     *         subgraph cluster_2 {
     *             e->f
     *         }
     *     }
     * }}
     * </pre>
     * The content of the above script converts into an example of Java code:
     * <pre>{@code
     *     Graphviz.digraph()
     *         .subgraph(
     *             Subgraph.builder()
     *                 .rank(Rank.SAME)
     *                 .addLine(a, b)
     *                 // Cluster inside Subgraph
     *                 .cluster(
     *                     Cluster.builder()
     *                         .addLine(c, d)
     *                         .build()
     *                 )
     *                 .build()
     *         )
     *         // Cluster in Graphviz
     *         .cluster(
     *             Cluster.builder()
     *                 // Cluster in Cluster
     *                 .cluster(
     *                     Cluster.builder()
     *                         .addLine(e, f)
     *                         .build()
     *                 )
     *                 .build()
     *         )
     *         .build();
     * }
     * </pre>
     *
     * @param cluster cluster
     * @return container builder
     * @throws NullPointerException null cluster
     */
    public B cluster(Cluster cluster) {
      Asserts.nullArgument(cluster, "cluster");
      initContainer().addCluster(cluster);
      return self();
    }

    /**
     * Mark the start of construction of a {@link Subgraph}, end with
     * {@link IntegrationSubgraphBuilder#endSub()}.
     *
     * <p>Compared with {@link #subgraph(Subgraph)}, it will not have too deep nesting.The
     * following is a nested example of a two-level subgraph:
     * <pre>{@code
     * Graphviz.digraph()
     *         .addLine(c, d)
     *
     *         // First level subgraph start
     *         .startSub()
     *         .addLine(f, e)
     *
     *         // Second level subgraph start
     *         .startSub()
     *         .rank(Rank.MIN)
     *         .addNode(g)
     *         // Second level subgraph end
     *         .endSub()
     *
     *         // First level subgraph end
     *         .endSub()
     *
     *         // Graphviz end
     *         .build();
     * }</pre>
     *
     * @return {@code IntegrationSubgraphBuilder}
     */
    public IntegrationSubgraphBuilder<G, B> startSub() {
      return new IntegrationSubgraphBuilder(this);
    }

    /**
     * Mark the start of construction of a {@link Cluster}, end with
     * {@link IntegrationClusterBuilder#endClus()}.
     *
     * <p>Compared with {@link #cluster(Cluster)}, it will not have too deep nesting.The following
     * is a nested example of a two-level cluster:
     * <pre>{@code
     * Graphviz.digraph()
     *         .addLine(c, d)
     *
     *         // First level cluster start
     *         .startClus()
     *         .addLine(f, e)
     *
     *         // Second level cluster start
     *         .startClus()
     *         .addNode(g)
     *         // Second level cluster end
     *         .endClus()
     *
     *         // First level cluster end
     *         .endClus()
     *
     *         // Graphviz end
     *         .build();
     * }</pre>
     *
     * @return {@code IntegrationClusterBuilder}
     */
    public IntegrationClusterBuilder<G, B> startClus() {
      return new IntegrationClusterBuilder(this);
    }

    /**
     * Set a node template, and the style attributes of all subsequent nodes will inherit the
     * template node style when they are not manually set.
     *
     * <p>Node attribute templates can take effect on all child nodes of all levels in the graph.
     * If a node has multiple node attribute templates at different levels, the one closest to the
     * current node will take effect first. If a property is not manually set and does not exist in
     * the template, the system default value will be used. A <tt>dot script</tt> and code example
     * using node templates is as follows:
     * <pre>{@code
     * digraph G {
     *     node[shape=rect]
     *      // The rect shape set in the root container takes effect
     *     a
     *     subgraph cluster_0 {
     *         node[shape=triangle]
     *         // The triangle shape set in the cluster takes effect
     *         b
     *         {
     *             node[shape=circle]
     *              // The circle shape set in the subgraph takes effect
     *             c
     *         }
     *     }
     *     subgraph cluster_1 {
     *         // The rect shape set in the root container takes effect
     *         d
     *     }
     * }
     * }</pre>
     * The content of the above script converts into an example of Java code:
     * <pre>{@code
     * Graphviz.digraph()
     *         .tempNode(Node.builder().shape(NodeShapeEnum.RECT).build())
     *         // The rect shape set in the root container takes effect
     *         .addNode(a)
     *         .cluster(
     *             Cluster.builder()
     *                 .tempNode(Node.builder().shape(NodeShapeEnum.TRIANGLE).build())
     *                 // The triangle shape set in the cluster takes effect
     *                 .addNode(b)
     *                 .subgraph(
     *                     Subgraph.builder()
     *                         .tempNode(Node.builder().shape(NodeShapeEnum.CIRCLE).build())
     *                         // The circle shape set in the subgraph takes effect
     *                         .addNode(c)
     *                         .build()
     *                 )
     *                 .build()
     *         )
     *         .cluster(
     *             Cluster.builder()
     *                 // The rect shape set in the root container takes effect
     *                 .addNode(d)
     *                 .build()
     *         )
     *         .build();
     * }</pre>
     *
     * @param node template node
     * @return container builder
     * @throws NullPointerException null template node
     */
    public B tempNode(Node node) {
      Asserts.nullArgument(node, "node");
      NodeAttrs nodeAttrs = node.nodeAttrs();

      if (nodeAttrs != null) {
        try {
          this.nodeAttrsMap = ClassUtils.propValMap(nodeAttrs);
        } catch (Exception ignore) {
        }
      }

      return self();
    }

    /**
     * Set a line template, and the style attributes of all subsequent lines will inherit the
     * template line style when they are not manually set. In order to build unnecessary nodes when
     * building the template line, it is best to use {@link Line#tempLine()}to build the template
     * line.
     *
     * <p>Line attribute templates can take effect on all child lines of all levels in the graph.
     * If a line has multiple line attribute templates at different levels, the one closest to the
     * current line will take effect first. If a property is not manually set and does not exist in
     * the template, the system default value will be used. A <tt>dot script</tt> and code example
     * using line templates is as follows:
     * <pre>{@code
     *  digraph G {
     *      edge[arrowhead=dot]
     *       // The dot arrow shape set in the root container takes effect
     *      a->b
     *      subgraph cluster_0 {
     *          edge[arrowhead=vee]
     *          // The vee arrow shape set in the cluster takes effect
     *          c->d
     *          {
     *              edge[arrowhead=curve]
     *               // The curve arrow shape set in the subgraph takes effect
     *              e->f
     *          }
     *      }
     *      subgraph cluster_1 {
     *          // The dot shape set in the root container takes effect
     *          g->h
     *      }
     *  }
     * }</pre>
     * The content of the above script converts into an example of Java code:
     * <pre>{@code
     * Graphviz.digraph()
     *         .tempLine(Line.tempLine().arrowHead(ArrowShape.DOT).build())
     *         // The dot arrow shape set in the root container takes effect
     *         .addLine(a, b)
     *         .cluster(
     *             Cluster.builder()
     *                 .tempLine(Line.tempLine().arrowHead(ArrowShape.VEE).build())
     *                 // The vee arrow shape set in the cluster takes effect
     *                 .addLine(c, d)
     *                 .subgraph(
     *                     Subgraph.builder()
     *                         .tempLine(Line.tempLine().arrowHead(ArrowShape.CURVE).build())
     *                         // The curve arrow shape set in the subgraph takes effect
     *                         .addLine(e, f)
     *                         .build()
     *                 )
     *                 .build()
     *         )
     *         .cluster(
     *             Cluster.builder()
     *                 // The dot shape set in the root container takes effect
     *                 .addLine(g, h)
     *                 .build()
     *         )
     *         .build();
     * }</pre>
     *
     * @param line template line
     * @return container builder
     * @throws NullPointerException null template line
     */
    public B tempLine(Line line) {
      Asserts.nullArgument(line, "line");
      LineAttrs lineAttrs = line.lineAttrs();

      if (lineAttrs != null) {
        try {
          this.lineAttrsMap = ClassUtils.propValMap(lineAttrs);
        } catch (Exception ignore) {
        }
      }

      return self();
    }

    /**
     * Add a node to the container.
     *
     * @param node node to be added to this graph
     * @return container builder
     * @throws NullPointerException null node
     */
    public B addNode(Node node) {
      Asserts.nullArgument(node, "node");
      initContainer().addNode(node);
      return self();
    }

    /**
     * Add node array to the container.
     *
     * @param nodes node array to be added to this graph
     * @return container builder
     * @throws IllegalArgumentException node array is <tt>null</tt> or empty
     */
    public B addNode(Node... nodes) {
      Asserts.illegalArgument(nodes == null || nodes.length == 0, "nodes can not be empty");
      for (Node node : nodes) {
        if (node != null) {
          initContainer().addNode(node);
        }
      }
      return self();
    }

    /**
     * Add a line consisting of {@code tail} and {@code head}. If {@link Graphviz#isDirected()} is
     * true, the representative is a direction edge to {@code head} from {@code tail}.
     *
     * @param tail tail node
     * @param head head node
     * @return container builder
     * @throws NullPointerException tail or head is null
     */
    public B addLine(Node tail, Node head) {
      Asserts.nullArgument(tail, "tail");
      Asserts.nullArgument(head, "head");
      return addLine(Line.builder(tail, head).build());
    }

    /**
     * Use a series of nodes to create lines, so the node array cannot be less than 2 (the number of
     * minimum nodes that form a line). Each adjacent node in the array forms a line. If
     * {@link Graphviz#isDirected()} is true, all the sides of the composition have a direction. The
     * direction is to point to the latter node from the first node of the two adjacent nodes.
     *
     * @param nodes a series of node array formed a line
     * @return container builder
     * @throws IllegalArgumentException the size of node array less than 2
     */
    public B addLine(Node... nodes) {
      Asserts.illegalArgument(nodes == null || nodes.length == 0, "nodes can not be empty");
      Asserts.illegalArgument(nodes.length < 2, "nodes can not be less than 2");

      for (int i = 0; i < nodes.length - 1; i++) {
        addLine(Line.builder(nodes[i], nodes[i + 1]).build());
      }

      return self();
    }

    /**
     * Add a line to the container.
     *
     * @param line line to be added to this graph
     * @return container builder
     * @throws NullPointerException null line
     */
    public B addLine(Line line) {
      Asserts.nullArgument(line, "line");
      initContainer().addNode(line.head());
      initContainer().addNode(line.tail());
      initContainer().addLine(line);

      return self();
    }

    /**
     * Build a {@code GraphContainer}.
     *
     * @return container builder
     * @see Graphviz
     * @see Subgraph
     * @see Cluster
     */
    public G build() {
      G repl = copy();
      supplyFields(repl);
      return repl;
    }

    /**
     * The copy of the graph container created during the build.
     *
     * @return the copy of graph container
     */
    protected abstract G copy();

    protected G initContainer() {
      if (container == null) {
        synchronized (this) {
          if (container == null) {
            container = newContainer();
          }
        }
      }

      return container;
    }

    protected void supplyFields(G repl) {
      G g = initContainer();
      repl.id = g.id;
      synchronized (g) {
        if (g.subgraphs != null) {
          repl.subgraphs = new ArrayList<>(g.subgraphs);
        }
        if (g.clusters != null) {
          repl.clusters = new ArrayList<>(g.clusters);
        }
        if (g.nodes != null) {
          repl.nodes = new TreeSet<>(g.nodes);
        }
        if (g.lines != null) {
          repl.lines = new TreeSet<>(g.lines);
        }
      }
      if (nodeAttrsMap != null) {
        repl.nodeAttrsMap = new HashMap<>(nodeAttrsMap);
      }
      if (lineAttrsMap != null) {
        repl.lineAttrsMap = new HashMap<>(lineAttrsMap);
      }
    }
  }
}
