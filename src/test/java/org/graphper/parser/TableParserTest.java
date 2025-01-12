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

import org.junit.jupiter.api.Test;

public class TableParserTest {

  @Test
  public void testParse() {
    String html = "<<table color=\"red\" border=\"1\">\n"
        + "         <tr>\n"
        + "         <!-- sss -->"
        + "             <td color=\"red\" border=\"1\">\n"
        + "                 <table color=\"blue\" border=\"1\">\n"
        + "                     <tr><td>111</td></tr>\n"
        + "                     <tr><td>222</td></tr>\n"
        + "                 </table>\n"
        + "             </td>\n"
        + "             <td>second 333</td>\n"
        + "         </tr>\n"
        + "     </table>>";

    TableParser.parse(html);
  }
}
