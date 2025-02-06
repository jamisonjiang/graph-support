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

import static org.graphper.api.Html.bold;
import static org.graphper.api.Html.italic;
import static org.graphper.api.Html.underline;

import org.graphper.api.Html.LabelTag;
import org.graphper.draw.ExecuteException;
import org.junit.jupiter.api.Test;

public class HtmlTagTest {

  @Test
  public void testLabelTagCycleDependency() throws ExecuteException {
    LabelTag t1 = bold("text");

    Graphviz graphviz = Graphviz.digraph()
        .addNode(Node.builder().labelTag(t1.br().font("font").bold(italic(underline(t1)))).build())
        .build();
    graphviz.toSvgStr();
  }
}
