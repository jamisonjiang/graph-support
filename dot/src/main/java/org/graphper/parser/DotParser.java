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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Graphviz;
import org.graphper.parser.grammar.DOTLexer;
import org.graphper.parser.grammar.DOTParser;
import org.graphper.util.Asserts;

/**
 * Provides static utility methods to read DOT language input (e.g., <i>.dot</i> files,
 * strings, or input streams) and convert them into a {@link Graphviz} data structure
 * for further use within Java code.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Parsing from a File
 * Graphviz graphFromFile = DotParser.parse(new File("path/to/graph.dot"));
 *
 * // Parsing from a String
 * String dotSource = "digraph G { a -> b; }";
 * Graphviz graphFromString = DotParser.parse(dotSource);
 * }</pre>
 *
 * @author johannes
 */
public class DotParser {

    private DotParser() {
    }

    /**
     * Reads DOT input from the specified file and returns a {@link Graphviz} representation of the
     * parsed graph.
     *
     * @param file the file containing DOT data
     * @return a {@code Graphviz} object representing the parsed graph
     * @throws NullPointerException if {@code File} is null
     * @throws IOException          if an I/O error occurs while reading the file
     * @throws ParseException       if the DOT script contains syntax errors
     */
    public static Graphviz parse(File file) throws IOException {
        Asserts.nullArgument(file);
        return parse(file, StandardCharsets.UTF_8);
    }

    /**
     * Reads DOT input from the specified file using the given charset and returns a {@link Graphviz}
     * representation of the parsed graph.
     *
     * @param file    the file containing DOT data
     * @param charset the character set to use for reading the file
     * @return a {@code Graphviz} object representing the parsed graph
     * @throws NullPointerException if {@code File} is null
     * @throws IOException          if an I/O error occurs while reading the file
     * @throws ParseException       if the DOT script contains syntax errors
     */
    public static Graphviz parse(File file, Charset charset) throws IOException {
        Asserts.nullArgument(file);
        charset = charset == null ? StandardCharsets.UTF_8 : charset;
        return parse(Files.newInputStream(file.toPath()), charset);
    }

    /**
     * Reads DOT input from the specified {@link InputStream} using the given charset, assuming the
     * source name is "anonymous InputStream". Returns a {@link Graphviz} representation of the parsed
     * graph.
     *
     * @param in      the {@link InputStream} containing DOT data
     * @param charset the character set to use for reading the stream
     * @return a {@code Graphviz} object representing the parsed graph
     * @throws NullPointerException if {@code InputStream} is null
     * @throws IOException          if an I/O error occurs while reading the stream
     * @throws ParseException       if the DOT script contains syntax errors
     */
    public static Graphviz parse(InputStream in, Charset charset) throws IOException {
        return parse(in, charset, "anonymous InputStream");
    }

    /**
     * Reads DOT input from the specified {@link InputStream} using the given charset, associating the
     * resulting stream with a custom {@code sourceName} (useful for error reporting). Returns a
     * {@link Graphviz} representation of the parsed graph.
     *
     * @param in         the {@link InputStream} containing DOT data
     * @param charset    the character set to use for reading the stream
     * @param sourceName a descriptive name for the input (e.g., file path or identifier)
     * @return a {@code Graphviz} object representing the parsed graph
     * @throws NullPointerException if {@code InputStream} is null
     * @throws IOException          if an I/O error occurs while reading the stream
     * @throws ParseException       if the DOT script contains syntax errors
     */
    public static Graphviz parse(InputStream in, Charset charset, String sourceName) throws IOException {
        Asserts.nullArgument(in);
        charset = charset == null ? StandardCharsets.UTF_8 : charset;
        try(Reader r = new InputStreamReader(in, charset)) {
            CharStream charStream = CharStreams.fromReader(r, sourceName);
            return parse(charStream);
        }
    }

    /**
     * Reads DOT input from the specified string, using "anonymous String" as the source name.
     *
     * @param in the DOT script as a string
     * @return a {@code Graphviz} object representing the parsed graph
     * @throws IllegalArgumentException if the DOT script is null or empty
     * @throws ParseException           if the DOT script contains syntax errors
     */
    public static Graphviz parse(String in) {
        return parse(in, "anonymous String");
    }

    /**
     * Reads DOT input from the specified string, associating the content with the given
     * {@code sourceName} (useful for error reporting).
     *
     * @param in         the DOT script as a string
     * @param sourceName a descriptive name for the input (e.g., file path or identifier)
     * @return a {@code Graphviz} object representing the parsed graph
     * @throws IllegalArgumentException if the DOT script is null or empty
     * @throws ParseException           if the DOT script contains syntax errors
     */
    public static Graphviz parse(String in, String sourceName) {
        Asserts.illegalArgument(StringUtils.isEmpty(in), "Empty dot");
        CharStream charStream = CharStreams.fromString(in, sourceName);
        return parse(charStream);
    }

    /**
     * A convenience method that parses DOT data from the given {@link CharStream} and returns a
     * {@code Graphviz} representation of the parsed graph. No post-processing hook is applied.
     *
     * @param charStream the {@link CharStream} containing DOT data
     * @return a {@code Graphviz} object representing the parsed graph
     * @throws NullPointerException if {@code CharStream} is null
     * @throws ParseException       if the DOT script contains syntax errors
     */
    public static Graphviz parse(CharStream charStream) {
        return parse(charStream, null);
    }

    /**
     * A static utility method that compiles a given {@link CharStream} (DOT source) into a
     * {@link Graphviz} instance, applying optional post-graph modifications.
     *
     * <p>It uses ANTLR to tokenize and parse the input via {@link DOTLexer} and {@link DOTParser},
     * then walks the parse tree to extract nodes and build a {@code Graphviz} model. If
     * {@code postGraphComponents} is provided, it is used to adjust or augment the parse results
     * before returning the final model.</p>
     *
     * @param charStream          the DOT input stream to parse
     * @param postGraphComponents an optional post-processing hook applied after parsing
     * @return a {@code Graphviz} object representing the parsed graph
     * @throws NullPointerException if {@code CharStream} is null
     * @throws ParseException       if dot script is illegal
     */
    public static Graphviz parse(CharStream charStream, PostGraphComponents postGraphComponents) {
        Asserts.nullArgument(charStream);
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
