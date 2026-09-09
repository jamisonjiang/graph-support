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

import java.util.Collections;
import org.graphper.api.FloatLabel;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.NodeAttrs;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Tend;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Proves that {@link ExternalLabelPlacer} does not build the alternative positions of a label whose
 * preferred position is accepted.
 *
 * <p>No counter is added to the production class for this. Instead the placement is handed an
 * endpoint whose box getters are a tripwire: generating the alternatives is the only part of the
 * algorithm that reads the endpoint box, because it anchors every candidate to a border of that box
 * and derives the preferred normal direction from its centre. So an untouched tripwire means the
 * alternatives were never generated, and a touched one means they were.
 *
 * <p>The tripwire node is deliberately not registered in the {@link DrawGraph}, so it never enters
 * the obstacle set and can only be reached through {@code Placement.endpoint}.
 */
public class ExternalLabelPlacerLazyCandidateTest {

  /**
   * One search invokes {@code addCandidate} 4 directions * 3 lanes * (1 or 2 tangent bases + 2 * 8
   * tangent steps) = 210 times, and each invocation anchors its candidate to exactly one border of
   * the endpoint box. {@code normalDirection} does not add to the count because
   * {@code ContainerDrawProp} answers {@code getX/getY/getWidth/getHeight} from its border fields
   * instead of the getters.
   */
  private static final int BORDER_READS_PER_SEARCH = 210;

  @Test
  public void acceptedPreferredPositionGeneratesNoAlternatives() throws Exception {
    Fixture fixture = new Fixture();
    // Far away from every node, edge and label of the graph, so the preferred box is unobstructed.
    FlatPoint preferred = new FlatPoint(fixture.draw.getMaxX() + 500,
                                        fixture.draw.getMaxY() + 500);

    FlatPoint placed = fixture.place(preferred);

    Assertions.assertSame(preferred, placed,
                          "an unobstructed label must keep the exact preferred position");
    Assertions.assertEquals(0, fixture.endpoint.borderReads,
                            "alternative positions were generated for a label that did not move");
  }

  @Test
  public void rejectedPreferredPositionGeneratesTheBoundedAlternativeSet() throws Exception {
    Fixture fixture = new Fixture();
    // On top of the head node, so the preferred box always intersects an obstacle.
    NodeDrawProp head = fixture.draw.getNodeDrawProp(fixture.head);
    FlatPoint preferred = new FlatPoint(head.getX(), head.getY());

    FlatPoint placed = fixture.place(preferred);

    Assertions.assertNotSame(preferred, placed, "a blocked label must be moved");
    Assertions.assertEquals(BORDER_READS_PER_SEARCH, fixture.endpoint.borderReads,
                            "the alternative set is no longer the bounded fixed-size set");
  }

  @Test
  public void searchRunsOncePerBlockedLabelOnly() throws Exception {
    Fixture blocked = new Fixture();
    NodeDrawProp head = blocked.draw.getNodeDrawProp(blocked.head);
    blocked.place(new FlatPoint(head.getX(), head.getY()));

    Fixture free = new Fixture();
    free.place(new FlatPoint(free.draw.getMaxX() + 500, free.draw.getMaxY() + 500));

    Assertions.assertEquals(BORDER_READS_PER_SEARCH, blocked.endpoint.borderReads);
    Assertions.assertEquals(0, free.endpoint.borderReads);
  }

  /**
   * A laid out single edge plus a float label that is not part of the graph, so the placement can be
   * driven directly with an arbitrary preferred position.
   */
  private static final class Fixture {

    private final Node head;

    private final DrawGraph draw;

    private final LineDrawProp prop;

    private final FloatLabel label;

    private final TripwireEndpoint endpoint;

    private Fixture() throws Exception {
      Node tail = Node.builder().id("lazy-tail").label("tail").build();
      this.head = Node.builder().id("lazy-head").label("head").build();
      this.label = FloatLabel.builder().label("probe").tend(Tend.HEAD).build();
      Line line = Line.builder(tail, head).floatLabels(label).build();
      this.draw = Layout.DOT.getLayoutEngine().layout(
          Graphviz.digraph().rankdir(Rankdir.LR).addLine(line).build());
      this.prop = draw.getLineDrawProp(line);
      this.endpoint = new TripwireEndpoint(head);
    }

    private FlatPoint place(FlatPoint preferred) {
      FlatPoint anchor = prop.get(prop.size() - 1);
      ExternalLabelPlacer.Placement placement = new ExternalLabelPlacer.Placement(
          prop, label, anchor, preferred, new FlatPoint(20, 60), null, endpoint);
      ExternalLabelPlacer.place(draw, Collections.singletonList(placement));
      FlatPoint placed = prop.getFloatLabelFlatCenters().get(label);
      Assertions.assertNotNull(placed, "the label was not placed at all");
      return placed;
    }
  }

  private static final class TripwireEndpoint extends NodeDrawProp {

    private int borderReads;

    private TripwireEndpoint(Node node) {
      super(node, new NodeAttrs());
    }

    @Override
    public double getLeftBorder() {
      borderReads++;
      return 0;
    }

    @Override
    public double getRightBorder() {
      borderReads++;
      return 0;
    }

    @Override
    public double getUpBorder() {
      borderReads++;
      return 0;
    }

    @Override
    public double getDownBorder() {
      borderReads++;
      return 0;
    }
  }
}
