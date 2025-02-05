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
import org.graphper.parser.grammar.HTMLLexer;
import org.graphper.parser.grammar.HTMLParser;

public class HtmlParser {

  private HtmlParser() {
  }

  public static HtmlListener parse(String htmlStr) {
    if (StringUtils.isEmpty(htmlStr) || htmlStr.length() < 3
        || (!htmlStr.contains(LT) && !htmlStr.contains(GT))) {
      return null;
    }

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
