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

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * An ANTLR listener that intercepts syntax errors when parsing DOT input and
 * throws a {@link ParseException} with a detailed error message.
 *
 * @author Jamison Jiang
 */
public class DotSyntaxErrorListener extends BaseErrorListener {

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer,
                          Object offendingSymbol,
                          int line, int charPositionInLine,
                          String msg,
                          RecognitionException e) {
    if (offendingSymbol == null) {
      throw new ParseException("line " + line + ":" + charPositionInLine + " " + msg);
    }

    // Cast the offendingSymbol to CommonToken
    CommonToken token = (CommonToken) offendingSymbol;

    // Retrieve the input stream from the token source
    CharStream inputStream = token.getTokenSource().getInputStream();

    // Extract the full input text
    String fullText = inputStream.toString();

    // Split the input text into lines
    String[] lines = fullText.split("\r?\n");

    // Get the specific line where the error occurred
    String errorLine = (line > 0 && line <= lines.length) ? lines[line - 1] : "<unknown line>";

    // Build a detailed error message
    StringBuilder errorBuilder = new StringBuilder();
    errorBuilder.append("line ")
        .append(line)
        .append(":")
        .append(charPositionInLine)
        .append(" ")
        .append(msg)
        .append(System.lineSeparator())
        .append(errorLine)
        .append(System.lineSeparator());

    // Add a caret indicating the position of the error
    for (int i = 0; i < charPositionInLine; i++) {
      errorBuilder.append(' ');
    }
    errorBuilder.append('^');

    // Throw a ParseCancellationException with the detailed error message
    throw new ParseException(errorBuilder.toString());
  }
}
