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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.graphper.api.Graphviz;
import org.graphper.parser.grammar.DOTLexer;
import org.graphper.parser.grammar.DOTParser;

public class DotParser {

    private final CharStream charStream;

    public DotParser(File file, Charset charset) throws IOException {
        try(Reader r = new InputStreamReader(Files.newInputStream(file.toPath()), charset)) {
            charStream = CharStreams.fromReader(r, file.getCanonicalPath());
        }
    }

    public DotParser(InputStream in, Charset charset) throws IOException {
        this(in, charset, "anonymous InputStream");
    }

    public DotParser(InputStream in, Charset charset, String sourceName) throws IOException {
        try(Reader r = new InputStreamReader(in, charset)) {
            charStream = CharStreams.fromReader(r, sourceName);
        }
    }

    public DotParser(String in) throws IOException {
        this(in, "anonymous String");
    }

    public DotParser(String in, String sourceName) {

        charStream = CharStreams.fromString(in, sourceName);
    }

    public Graphviz parse() {
        return compile(charStream);
    }

    public Graphviz parse(PostGraphComponents postGraphComponents) {

        return compile(charStream, postGraphComponents);
    }

    public static Graphviz compile(CharStream charStream) {
        return compile(charStream, null);
    }

    public static Graphviz compile(CharStream charStream, PostGraphComponents postGraphComponents) {
        DOTLexer lexer = new DOTLexer(charStream);
        DOTParser p = new DOTParser(new CommonTokenStream(lexer));

        p.removeErrorListeners();
        lexer.removeErrorListeners();

        DotSyntaxErrorListener dotSyntaxErrorListener = new DotSyntaxErrorListener();
        p.addErrorListener(dotSyntaxErrorListener);
        lexer.addErrorListener(dotSyntaxErrorListener);

        DOTParser.GraphContext graphCtx = p.graph();

        ParseTreeWalker parseTreeWalker = new ParseTreeWalker();
        NodeExtractor nodeExtractor = new NodeExtractor(postGraphComponents);
        parseTreeWalker.walk(nodeExtractor, graphCtx);

        GraphvizListener gl = new GraphvizListener(nodeExtractor, postGraphComponents);
        parseTreeWalker.walk(gl, graphCtx);
        return gl.getGraphviz();
    }
}
