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

import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;

/**
 * Completes a DOT attribute name to a full {@code name = ""} assignment. {@link DotAutoCompletion}
 * recognises this type and drops the caret between the quotes so the value can be typed right away.
 *
 * @author Jamison Jiang
 */
final class DotAttributeCompletion extends ShorthandCompletion {

  DotAttributeCompletion(CompletionProvider provider, String attribute) {
    super(provider, attribute, attribute + " = \"\"", "Graphviz attribute");
  }
}
