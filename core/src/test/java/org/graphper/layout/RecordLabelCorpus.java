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

/**
 * The frozen record-label string corpus, shared by the geometry golden test and the parser
 * round-trip test.
 *
 * <p>Every string here also appears in {@link CellLabelCompilerTest}; they encode the accumulated
 * edge cases of the record grammar (escapes, unbalanced closing braces, blank fields, deep
 * nesting, port ids). The grammar is frozen, so this corpus is expected to stay stable: new record
 * capabilities go through {@code Html.record(...)} instead.
 */
public final class RecordLabelCorpus {

  private RecordLabelCorpus() {
  }

  /**
   * Strings that compile successfully.
   */
  public static final String[] VALID = {
      "",
      " ",
      "      ",
      "   a",
      "   a    ",
      "|",
      "||",
      "<P0>||",
      "   |    ",
      "   |  |   ",
      "   |  |",
      "{}",
      "  {}   ",
      "  {   } ",
      "  {   }",
      "  {{   }}} ",
      "{   }",
      "1||",
      "1\\||",
      "1\\|\\|",
      "1\\\\|\\|",
      "1\\ \\|\\|",
      "1\\ \\|\\ |",
      "1\\|\\  |",
      "1  \\ |\\  |",
      "1  \\ |\\  | ",
      "|||",
      "|||\\ ",
      "|1||<*>2||",
      "|1||<&&>\\<*\\>2||",
      "1|{<P0>2}}|3",
      "|1||2||{}",
      "|1||2||{|}",
      "|1||2||{|}}",
      " | 1| |2| |{ |} }  ",
      "123",
      "1|{{2| 3    11 }|4}|5|",
      "{<p1>1|{{<p2>2|<p3>3}|<p4>4}|<p5>5|}",
      "{|1|{{2|3}|4}|5|}",
      "1|{{{2|3}|4}|5|}",
      "1|{{{2|3}|4}|5|}}}}}}",
      "1|{{{2|3}|4}|5|}}}}} } ",
      "1|{{{2|33333\n33n\n3}|4}|5|}}}} } }",
      "<f0> 0x10ba8| <f1>",
  };

  /**
   * Strings that must be rejected with {@link LabelFormatException}.
   */
  public static final String[] INVALID = {
      "\\",
      "||\\",
      "||2\\",
      "1|{2",
      "1|2{2}}",
      "1|<oo>{2}}",
      "1|{<P2}}",
      "1|{P>2}}",
      "1|{\\<P>2}}",
      "1|{<P\\>2}}",
      "1|{<P|>2}}",
      "1|{<P|2>2}}",
      "1|{<<P2>>2}}",
      "1|2{2|3}",
      "1|{{{{2|3}|4}|5|}",
      "1|11|3|2|5|{6|}}888",
      "1|11|3|2|5|{6|}7}",
      "1|{{2|3}|4\\}|5|",
  };
}
