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

/**
 * A parser that reads DOT language input (e.g., <i>.dot</i> files or strings)
 * and converts it into a {@link Graphviz} data structure for further use
 * within Java code.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Parsing from a File
 * DotParser parser = new DotParser(new File("path/to/graph.dot"), StandardCharsets.UTF_8);
 * Graphviz graphviz = parser.parse();
 *
 * // Parsing from a string
 * String dotSource = "digraph G { a -> b; }";
 * DotParser stringParser = new DotParser(dotSource);
 * Graphviz stringGraph = stringParser.parse();
 * }</pre>
 *
 * @author johannes
 */
public class DotParser {

    private final CharStream charStream;

    /**
     * Creates a parser that reads DOT input from a file using the specified charset.
     *
     * @param file    the DOT file to parse
     * @param charset the charset used to read the file
     * @throws IOException    if an I/O error occurs opening or reading the file
     */
    public DotParser(File file, Charset charset) throws IOException {
        try(Reader r = new InputStreamReader(Files.newInputStream(file.toPath()), charset)) {
            charStream = CharStreams.fromReader(r, file.getCanonicalPath());
        }
    }

    /**
     * Creates a parser that reads DOT input from an {@link InputStream} using the specified charset.
     *
     * <p>The {@code sourceName} is set to {@code "anonymous InputStream"} by default.</p>
     *
     * @param in      an input stream containing DOT data
     * @param charset the charset used to read the stream
     * @throws IOException    if an I/O error occurs while reading the stream
     */
    public DotParser(InputStream in, Charset charset) throws IOException {
        this(in, charset, "anonymous InputStream");
    }

    /**
     * Creates a parser that reads DOT input from an {@link InputStream} using the specified charset
     * and associates it with a given source name, useful for error reporting.
     *
     * @param in         an input stream containing DOT data
     * @param charset    the charset used to read the stream
     * @param sourceName a descriptive name for this source (e.g., a file path)
     * @throws IOException    if an I/O error occurs while reading the stream
     */
    public DotParser(InputStream in, Charset charset, String sourceName) throws IOException {
        try(Reader r = new InputStreamReader(in, charset)) {
            charStream = CharStreams.fromReader(r, sourceName);
        }
    }

    /**
     * Creates a parser that reads DOT input from a string. The {@code sourceName} is set to
     * {@code "anonymous String"} by default.
     *
     * @param in the DOT input as a string
     */
    public DotParser(String in) {
        this(in, "anonymous String");
    }

    /**
     * Creates a parser that reads DOT input from a string and associates it with a given source name,
     * useful for error reporting.
     *
     * @param in         the DOT input as a string
     * @param sourceName a descriptive name for this source (e.g., a file path)
     */
    public DotParser(String in, String sourceName) {

        charStream = CharStreams.fromString(in, sourceName);
    }

    /**
     * Parses the DOT data provided via the constructor, returning a {@link Graphviz}
     * instance representing the parsed graph.
     *
     * <p>This is a convenience method that calls {@link #compile(CharStream)} under
     * the hood. If parsing fails due to syntax or lexical errors, an exception may be
     * thrown (see {@link DotSyntaxErrorListener}).</p>
     *
     * @return a {@code Graphviz} object representing the parsed graph
     * @throws ParseException if dot script is illegal
     */
    public Graphviz parse() {
        return compile(charStream);
    }

    /**
     * Parses the DOT data provided via the constructor, returning a {@link Graphviz} instance
     * representing the parsed graph, with optional post-graph modifications.
     *
     * <p>The {@code postGraphComponents} may alter or augment the parse results before
     * the final {@code Graphviz} object is created.</p>
     *
     * @param postGraphComponents an optional post-processing hook applied after parsing
     * @return a {@code Graphviz} object representing the parsed graph with post-graph modifications
     * @throws ParseException if dot script is illegal
     */
    public Graphviz parse(PostGraphComponents postGraphComponents) {

        return compile(charStream, postGraphComponents);
    }

    /**
     * A static utility method that compiles a given {@link CharStream} (DOT source)
     * into a {@link Graphviz} instance, without any post-graph modifications.
     *
     * <p>If syntax errors are encountered, they will be recorded by {@link DotSyntaxErrorListener}
     * and may cause exceptions or error messages. For advanced handling, consider
     * calling {@link #compile(CharStream, PostGraphComponents)}.</p>
     *
     * @param charStream the DOT input stream to parse
     * @return a {@code Graphviz} object representing the parsed graph
     * @throws ParseException if dot script is illegal
     */
    public static Graphviz compile(CharStream charStream) {
        return compile(charStream, null);
    }

    /**
     * A static utility method that compiles a given {@link CharStream} (DOT source)
     * into a {@link Graphviz} instance, applying optional post-graph modifications.
     *
     * <p>It uses ANTLR to tokenize and parse the input via {@link DOTLexer} and {@link DOTParser},
     * then walks the parse tree to extract nodes and build a {@code Graphviz} model.
     * If {@code postGraphComponents} is provided, it is used to adjust or augment
     * the parse results before returning the final model.</p>
     *
     * @param charStream          the DOT input stream to parse
     * @param postGraphComponents an optional post-processing hook applied after parsing
     * @return a {@code Graphviz} object representing the parsed graph
     * @throws ParseException if dot script is illegal
     */
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
