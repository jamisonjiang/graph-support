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

package org.graphper.ui;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;

/**
 * Builds the completion set for the DOT editor: keywords, attribute assignments, common attribute
 * values, and a few block templates. {@code digraph} and {@code subgraph} are offered only as
 * templates so they are never suggested twice.
 *
 * @author Jamison Jiang
 */
final class DotCompletionProvider {

  private static final String[] KEYWORDS = {
      "strict", "graph", "digraph", "subgraph", "node", "edge"
  };

  private static final String[] ATTRIBUTES = {
      "label", "xlabel", "shape", "style", "color", "fillcolor", "bgcolor", "fontcolor",
      "fontname", "fontsize", "rankdir", "rank", "layout", "splines", "nodesep", "ranksep",
      "minlen", "weight", "constraint", "dir", "arrowhead", "arrowtail", "arrowsize",
      "headlabel", "taillabel", "headport", "tailport", "lhead", "ltail", "width", "height",
      "samehead", "sametail", "fixedsize", "margin", "penwidth", "peripheries", "orientation",
      "sides", "regular",
      "compound", "concentrate", "overlap", "tooltip", "href"
  };

  private static final String[] VALUES = {
      "TB", "BT", "LR", "RL", "dot", "dotq", "fdp", "jfdp", "gfdp", "spline",
      "polyline", "ortho", "solid", "dashed", "dotted", "bold", "filled", "rounded", "box",
      "ellipse", "circle", "diamond", "point", "record", "cylinder", "normal", "vee", "both",
      "forward", "back", "same", "min", "max", "source", "sink", "true", "false",
      "rectangle", "oval", "none", "square", "polygon", "house", "invhouse",
      "doublecircle", "doubleoctagon", "tripleoctagon", "Mdiamond", "Msquare", "Mcircle",
      "tab", "folder", "box3d", "component", "inv", "tee", "crow", "icurve",
      "onormal", "oinv", "obox", "odot", "odiamond", "empty", "invempty", "open"
  };

  private DotCompletionProvider() {
  }

  static DefaultCompletionProvider create() {
    DefaultCompletionProvider provider = new DefaultCompletionProvider();
    provider.setAutoActivationRules(true, null);
    addKeywords(provider);
    addAttributes(provider);
    addValues(provider);
    addTemplates(provider);
    return provider;
  }

  private static void addKeywords(DefaultCompletionProvider provider) {
    for (String keyword : KEYWORDS) {
      // digraph and subgraph are contributed as templates instead, avoiding duplicate suggestions.
      if (!"digraph".equals(keyword) && !"subgraph".equals(keyword)) {
        provider.addCompletion(new BasicCompletion(provider, keyword, "DOT keyword"));
      }
    }
  }

  private static void addAttributes(DefaultCompletionProvider provider) {
    for (String attribute : ATTRIBUTES) {
      provider.addCompletion(new DotAttributeCompletion(provider, attribute));
    }
  }

  private static void addValues(DefaultCompletionProvider provider) {
    for (String value : VALUES) {
      provider.addCompletion(new BasicCompletion(provider, value, "Common DOT value"));
    }
  }

  private static void addTemplates(DefaultCompletionProvider provider) {
    provider.addCompletion(new ShorthandCompletion(provider, "digraph", "digraph G {\n  \n}",
                                                   "Directed graph template"));
    provider.addCompletion(new ShorthandCompletion(provider, "subgraph",
                                                   "subgraph cluster_name {\n  \n}",
                                                   "Cluster template"));
    provider.addCompletion(new ShorthandCompletion(provider, "nodeattrs",
                                                   "node [shape=box, style=rounded];",
                                                   "Default node attributes"));
    provider.addCompletion(new ShorthandCompletion(provider, "edgeattrs",
                                                   "edge [arrowhead=normal];",
                                                   "Default edge attributes"));
  }
}
