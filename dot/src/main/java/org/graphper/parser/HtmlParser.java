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

package org.graphper.parser;

import static org.graphper.draw.svg.SvgConstants.GT;
import static org.graphper.draw.svg.SvgConstants.LT;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Html.LabelTag;
import org.graphper.api.Html.Table;
import org.graphper.parser.grammar.HTMLLexer;
import org.graphper.parser.grammar.HTMLParser;

/**
 * Provides a utility for parsing HTML-like strings and generating an {@link HtmlListener}
 * that holds the resulting structure (e.g., a {@code Table}, {@code LabelTag}, or plain text).
 *
 * @author Jamison Jiang
 */
public class HtmlParser {

  private HtmlParser() {
  }

  /**
   * Parses the provided HTML-like string, returning an {@link HtmlListener} which
   * contains the parsed result (such as a {@link Table}, {@link LabelTag}, or raw text).
   *
   * <p>This method enforces minimal validation:
   * the string must be enclosed by {@code '<'} and {@code '>'} and must have a length of
   * at least 3. If any of these checks fail, {@code null} is returned.</p>
   *
   * <p>If parsing completes successfully, the returned {@link HtmlListener} can be queried
   * for the root structure via methods like {@link HtmlListener#getTable()},
   * {@link HtmlListener#getLabelTag()}, or {@link HtmlListener#getLabel()}.</p>
   *
   * @param htmlStr a string containing an HTML-like snippet enclosed in {@code '<' '>'}
   * @return an {@link HtmlListener} containing the parsed result, or {@code null} if validation fails
   */
  public static HtmlListener parse(String htmlStr) {
    if (StringUtils.isEmpty(htmlStr) || htmlStr.length() < 3
        || (!htmlStr.startsWith(LT) && !htmlStr.endsWith(GT))) {
      return null;
    }

    // Remove the leading '<' and trailing '>'
    htmlStr = htmlStr.substring(1);
    htmlStr = htmlStr.substring(0, htmlStr.length() - 1);
    htmlStr = htmlStr.trim();

    HTMLLexer lexer = new HTMLLexer(CharStreams.fromString(htmlStr));
    HTMLParser p = new HTMLParser(new CommonTokenStream(lexer));

    p.removeErrorListeners();
    lexer.removeErrorListeners();

    HtmlSyntaxErrorListener htmlSyntaxErrorListener = new HtmlSyntaxErrorListener();
    p.addErrorListener(htmlSyntaxErrorListener);
    lexer.addErrorListener(htmlSyntaxErrorListener);

    HtmlListener listener = new HtmlListener();
    new ParseTreeWalker().walk(listener, p.htmlTag());

    return listener;
  }
}