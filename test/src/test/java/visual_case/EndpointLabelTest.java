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

package visual_case;

import helper.GraphvizVisual;
import helper.VisualTags;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Rankdir;
import org.graphper.parser.DotParser;
import org.junit.jupiter.api.Test;

@VisualTags({"endpoint-label", "float-label"})
public class EndpointLabelTest extends GraphvizVisual {

  @Test
  @VisualTags({"headlabel", "taillabel", "owner-edge", "vertical-edge", "narrow-label"})
  public void narrowLabelsStayBesideVerticalOwnerEdge() throws Exception {
    visual(DotParser.parse("digraph G {"
        + "nodesep=.486111; ranksep=.833333;"
        + "source [shape=rect,width=4.656169,height=2.498698];"
        + "target [shape=rect,width=3.639323,height=.895671];"
        + "source->target [label=<<TABLE BGCOLOR=\"#fff0a8\" FIXEDSIZE=\"TRUE\" WIDTH=\"63\" HEIGHT=\"17\"><TR><TD>body</TD></TR></TABLE>>,"
        + "taillabel=<<TABLE BGCOLOR=\"#d6f5c4\" FIXEDSIZE=\"TRUE\" WIDTH=\"8\" HEIGHT=\"15\"><TR><TD></TD></TR></TABLE>>,"
        + "headlabel=<<TABLE BGCOLOR=\"#b8e5ff\" FIXEDSIZE=\"TRUE\" WIDTH=\"8\" HEIGHT=\"15\"><TR><TD></TD></TR></TABLE>>];"
        + "}"));
  }

  @Test
  @VisualTags({"taillabel", "owner-edge", "vertical-edge", "wide-label"})
  public void wideTailLabelStaysBesideVerticalOwnerEdge() throws Exception {
    visual(DotParser.parse("digraph G {"
        + "nodesep=.486111; ranksep=.833333;"
        + "target [shape=rect,width=3.811930,height=1.124674];"
        + "source [shape=rect,width=2.142985,height=.666667];"
        + "source->target [taillabel=<<TABLE BGCOLOR=\"#ffd7a8\" FIXEDSIZE=\"TRUE\" WIDTH=\"202\" HEIGHT=\"15\"><TR><TD>wide tail label</TD></TR></TABLE>>];"
        + "}"));
  }

  @Test
  @VisualTags({"headlabel", "parallel-edge", "html-label"})
  public void parallelHtmlHeadLabels() throws Exception {
    visual(DotParser.parse("digraph G {"
        + "rankdir=LR; nodesep=0.5; ranksep=1;"
        + "ingress [shape=ellipse]; dispatcher [shape=rect,width=2,height=.7];"
        + "worker [shape=rect,width=1.5,height=.7]; archive [shape=ellipse];"
        + "priority [shape=diamond]; retry [shape=diamond]; monitor [shape=note];"
        + "lane1 [shape=point,style=invis]; lane2 [shape=point,style=invis]; lane3 [shape=point,style=invis];"
        + "ingress->dispatcher [label=\"request\"];"
        + "dispatcher->worker [minlen=5,headlabel=<<TABLE BGCOLOR=\"#ffd7a8\" FIXEDSIZE=\"TRUE\" WIDTH=\"135\" HEIGHT=\"18\"><TR><TD>accepted</TD></TR></TABLE>>];"
        + "dispatcher->worker [minlen=5,headlabel=<<TABLE BGCOLOR=\"#b8e5ff\" FIXEDSIZE=\"TRUE\" WIDTH=\"110\" HEIGHT=\"18\"><TR><TD>deferred</TD></TR></TABLE>>];"
        + "dispatcher->worker [minlen=5,headlabel=<<TABLE BGCOLOR=\"#f7c7dc\" FIXEDSIZE=\"TRUE\" WIDTH=\"125\" HEIGHT=\"18\"><TR><TD>replayed</TD></TR></TABLE>>];"
        + "dispatcher->lane1->lane2->lane3->worker [style=invis];"
        + "priority->worker [minlen=2,headlabel=\"priority\"]; retry->worker [minlen=2,headlabel=\"retry\"];"
        + "worker->archive [label=\"persist\"]; worker->monitor [style=dashed];"
        + "}"));
  }

  @Test
  @VisualTags({"taillabel", "parallel-edge", "html-label"})
  public void parallelHtmlTailLabels() throws Exception {
    visual(DotParser.parse("digraph G {"
        + "rankdir=LR; nodesep=0.5; ranksep=1;"
        + "source [shape=rect,width=1.5,height=.7]; sink [shape=rect,width=2,height=.7];"
        + "scheduler [shape=ellipse]; cache [shape=cylinder]; audit [shape=note];"
        + "success [shape=diamond]; failure [shape=diamond];"
        + "stage1 [shape=point,style=invis]; stage2 [shape=point,style=invis]; stage3 [shape=point,style=invis];"
        + "scheduler->source [label=\"dispatch\"]; cache->source [style=dashed];"
        + "source->sink [minlen=5,taillabel=<<TABLE BGCOLOR=\"#d6f5c4\" FIXEDSIZE=\"TRUE\" WIDTH=\"135\" HEIGHT=\"18\"><TR><TD>primary route</TD></TR></TABLE>>];"
        + "source->sink [minlen=5,taillabel=<<TABLE BGCOLOR=\"#e6d5ff\" FIXEDSIZE=\"TRUE\" WIDTH=\"110\" HEIGHT=\"18\"><TR><TD>fallback</TD></TR></TABLE>>];"
        + "source->sink [minlen=5,taillabel=<<TABLE BGCOLOR=\"#fff0a8\" FIXEDSIZE=\"TRUE\" WIDTH=\"125\" HEIGHT=\"18\"><TR><TD>shadow copy</TD></TR></TABLE>>];"
        + "source->stage1->stage2->stage3->sink [style=invis];"
        + "source->audit [taillabel=\"audit\",style=dashed];"
        + "sink->success; sink->failure;"
        + "}"));
  }

  @Test
  @VisualTags({"headlabel", "taillabel", "parallel-edge"})
  public void mixedTextEndpointLabels() throws Exception {
    visual(DotParser.parse("digraph G {"
        + "rankdir=LR; nodesep=.45;"
        + "client [shape=ellipse]; gateway [shape=box]; service [shape=box];"
        + "queue [shape=cylinder]; cache [shape=cylinder]; audit [shape=note];"
        + "result [shape=ellipse]; alert [shape=diamond];"
        + "hop1 [shape=point,style=invis]; hop2 [shape=point,style=invis]; hop3 [shape=point,style=invis];"
        + "client->gateway [label=\"HTTPS\"]; queue->gateway [style=dashed];"
        + "gateway->service [minlen=5,headlabel=\"sync in\",taillabel=\"sync out\"];"
        + "gateway->service [minlen=5,headlabel=\"async in\",taillabel=\"async out\"];"
        + "gateway->service [minlen=5,headlabel=\"retry in\",taillabel=\"retry out\"];"
        + "gateway->hop1->hop2->hop3->service [style=invis];"
        + "gateway->cache [headlabel=\"cache write\"];"
        + "service->result [label=\"response\"]; service->audit [style=dashed];"
        + "audit->alert [taillabel=\"threshold\"];"
        + "}"));
  }

  @Test
  @VisualTags({"headlabel", "taillabel", "reversed-path", "cycle"})
  public void labelsFollowSemanticEndpointsOnReversedPaths() throws Exception {
    visual(DotParser.parse("digraph G {"
        + "rankdir=LR;"
        + "input->a [label=\"enter\"];"
        + "node [shape=ellipse]; a; b; c; d; input; side; output;"
        + "node [shape=point,style=invis]; ab1; ab2; bc1; bc2; cd1; cd2;"
        + "a->ab1->ab2->b [style=invis];"
        + "b->bc1->bc2->c [style=invis];"
        + "c->cd1->cd2->d [style=invis];"
        + "a->b [minlen=3,headlabel=\"head b\",taillabel=\"tail a\"];"
        + "b->c [minlen=3,headlabel=\"head c\",taillabel=\"tail b\"];"
        + "c->d [minlen=3,headlabel=\"head d\",taillabel=\"tail c\"];"
        + "d->a [minlen=3,headlabel=\"head a\",taillabel=\"tail d\"];"
        + "b->side [style=dashed,headlabel=\"side\"];"
        + "c->output [label=\"exit\"]; side->output [style=dotted];"
        + "}"));
  }

  @Test
  @VisualTags({"headlabel", "taillabel", "rankdir"})
  public void endpointLabelsAcrossRankDirections() {
    for (Rankdir rankdir : Rankdir.values()) {
      Node ingress = Node.builder().id("ingress_" + rankdir).label("ingress").build();
      Node source = Node.builder().id("source_" + rankdir).label("source " + rankdir).build();
      Node target = Node.builder().id("target_" + rankdir).label("target " + rankdir).build();
      Node observer = Node.builder().id("observer_" + rankdir).label("observer").build();
      Node success = Node.builder().id("success_" + rankdir).label("success").build();
      Node failure = Node.builder().id("failure_" + rankdir).label("failure").build();
      Node hop1 = Node.builder().id("hop1_" + rankdir).label("")
          .shape(org.graphper.api.attributes.NodeShapeEnum.POINT)
          .style(org.graphper.api.attributes.NodeStyle.INVIS).build();
      Node hop2 = Node.builder().id("hop2_" + rankdir).label("")
          .shape(org.graphper.api.attributes.NodeShapeEnum.POINT)
          .style(org.graphper.api.attributes.NodeStyle.INVIS).build();
      Node hop3 = Node.builder().id("hop3_" + rankdir).label("")
          .shape(org.graphper.api.attributes.NodeShapeEnum.POINT)
          .style(org.graphper.api.attributes.NodeStyle.INVIS).build();
      Line labeled = Line.builder(source, target)
          .minlen(4)
          .floatLabels(
              org.graphper.api.FloatLabel.builder().label("tail " + rankdir)
                  .tend(org.graphper.api.attributes.Tend.TAIL).build(),
              org.graphper.api.FloatLabel.builder().label("head " + rankdir)
                  .tend(org.graphper.api.attributes.Tend.HEAD).build())
          .build();
      visual(Graphviz.digraph().rankdir(rankdir)
                 .addLine(ingress, source)
                 .addLine(Line.builder(observer, source)
                              .floatLabels(org.graphper.api.FloatLabel.builder()
                                               .label("observed " + rankdir)
                                               .tend(org.graphper.api.attributes.Tend.HEAD).build())
                              .build())
                 .addLine(labeled)
                 .addLine(Line.builder(source, target)
                              .minlen(4)
                              .floatLabels(org.graphper.api.FloatLabel.builder()
                                               .label("parallel " + rankdir)
                                               .tend(org.graphper.api.attributes.Tend.HEAD).build())
                              .build())
                 .addLine(Line.builder(source, hop1).style(org.graphper.api.attributes.LineStyle.INVIS).build())
                 .addLine(Line.builder(hop1, hop2).style(org.graphper.api.attributes.LineStyle.INVIS).build())
                 .addLine(Line.builder(hop2, hop3).style(org.graphper.api.attributes.LineStyle.INVIS).build())
                 .addLine(Line.builder(hop3, target).style(org.graphper.api.attributes.LineStyle.INVIS).build())
                 .addLine(target, success)
                 .addLine(target, failure)
                 .addLine(observer, failure)
                 .build());
    }
  }
}
