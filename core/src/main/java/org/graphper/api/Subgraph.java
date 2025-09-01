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

import java.io.Serializable;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.Rank;
import org.graphper.util.Asserts;

/**
 * Subgraph, mainly has the following usage scenarios:
 * <ul>
 *   <li> Specify common styles for some nodes and lines;
 *   <li> Under the {@link Layout#DOT} and {@link Layout#DOTQ} engines, combine the {@link Rank} attribute to specify
 *   the rank of nodes in the graph.
 * </ul>
 *
 * @author Jamison Jiang
 */
public class Subgraph extends GraphContainer implements Serializable {

  private static final long serialVersionUID = -6058438891682789815L;

  // Rank of subgraph
  private Rank rank;

  private Subgraph() {
  }

  /**
   * Returns the rank of subgraph
   *
   * @return rank of subgraph
   */
  public Rank getRank() {
    return rank;
  }

  /**
   * Returns a {@link SubgraphBuilder}.
   *
   * @return SubgraphBuilder
   */
  public static SubgraphBuilder builder() {
    return new SubgraphBuilder();
  }

  // ------------------------------------------ Subgraph Builder ---------------------------------------

  /**
   * {@link Subgraph} builder, used to build a {@link Subgraph}.
   */
  public static class SubgraphBuilder extends
      GraphContainerBuilder<Subgraph, SubgraphBuilder> implements Cloneable {

    private SubgraphBuilder() {
    }

    /**
     * Set the {@link Rank} type of {@code Subgraph}.
     *
     * <p>In the {@link Layout#DOT} and {@link Layout#DOTQ}, all nodes have a specified rank. When
     * the nodes in the current subgraph are designated as a certain {@link Rank}, the ranks of all
     * nodes are close to the real rank described by this rank type.
     *
     * @param rank the rank type of subgraph
     * @return Subgraph builder
     */
    public SubgraphBuilder rank(Rank rank) {
      initContainer().rank = rank;
      return this;
    }

    @Override
    protected SubgraphBuilder self() {
      return this;
    }

    @Override
    protected Subgraph newContainer() {
      return new Subgraph();
    }

    @Override
    protected Subgraph copy() {
      Subgraph sub = new Subgraph();
      sub.rank = initContainer().rank;
      return sub;
    }

    @Override
    public SubgraphBuilder clone() {
      SubgraphBuilder repl = new SubgraphBuilder();
      repl.container = copy();
      supplyFields(repl.container);
      return repl;
    }
  }

  /**
   * A {@link Subgraph} builder to connect directly to the parent container builder.
   *
   * @param <G> the container type of the parent container
   * @param <B> the builder type of the parent container
   */
  public static class IntegrationSubgraphBuilder<G extends GraphContainer, B extends GraphContainerBuilder<G, B>> extends
      GraphContainerBuilder<Subgraph, IntegrationSubgraphBuilder<G, B>> {

    private final B parentBuilder;

    IntegrationSubgraphBuilder(B parentBuilder) {
      Asserts.nullArgument(parentBuilder, "parentBuilder");
      this.parentBuilder = parentBuilder;
    }

    /**
     * Set the {@link Rank} type of {@code Subgraph}.
     *
     * <p>In the {@link Layout#DOT} and {@link Layout#DOTQ}, all nodes have a specified rank. When
     * the nodes in the current subgraph are designated as a certain {@link Rank}, the ranks of all
     * nodes are close to the real rank described by this rank type.
     *
     * @param rank the rank type of subgraph
     * @return IntegrationSubgraphBuilder
     */
    public IntegrationSubgraphBuilder<G, B> rank(Rank rank) {
      initContainer().rank = rank;
      return this;
    }

    /**
     * End the operation of the current {@link Subgraph} builder, return to the builder of the
     * parent container, and continue the construction process of the parent container.
     *
     * @return parent builder
     */
    public B endSub() {
      Subgraph subgraph = build();
      parentBuilder.subgraph(subgraph);
      return parentBuilder;
    }

    @Override
    protected IntegrationSubgraphBuilder<G, B> self() {
      return this;
    }

    @Override
    protected Subgraph newContainer() {
      return new Subgraph();
    }

    @Override
    protected Subgraph copy() {
      Subgraph sub = new Subgraph();
      sub.rank = initContainer().rank;
      return sub;
    }
  }
}
