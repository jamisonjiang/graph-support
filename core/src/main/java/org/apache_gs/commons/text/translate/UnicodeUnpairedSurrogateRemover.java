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
package org.apache_gs.commons.text.translate;

import java.io.IOException;
import java.io.Writer;

/**
 * Helper subclass to CharSequenceTranslator to remove unpaired surrogates.
 *
 * @since 1.0
 */
public class UnicodeUnpairedSurrogateRemover extends CodePointTranslator {
    /**
     * Implementation of translate that throws out unpaired surrogates.
     * {@inheritDoc}
     */
    @Override
    public boolean translate(final int codePoint, final Writer writer) throws IOException {
        // If true, it is a surrogate. Write nothing and say we've translated. Otherwise return false, and don't translate it.
        return codePoint >= Character.MIN_SURROGATE && codePoint <= Character.MAX_SURROGATE;
    }
}

