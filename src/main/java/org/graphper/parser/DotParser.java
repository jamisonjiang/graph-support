package org.graphper.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.graphper.api.Graphviz;
import org.graphper.parser.grammar.DOTLexer;
import org.graphper.parser.grammar.DOTParser;

public class DotParser {

    private CharStream charStream;

    public DotParser(File file, Charset charset) throws IOException {

        Reader r = new InputStreamReader(new FileInputStream(file), charset);
        charStream = CharStreams.fromReader(r, file.getCanonicalPath());
    }

    public DotParser(InputStream in, Charset charset) throws IOException {
        this(in, charset, "anonymous InputStream");
    }

    public DotParser(InputStream in, Charset charset, String sourceName) throws IOException {

        Reader r = new InputStreamReader(in, charset);
        charStream = CharStreams.fromReader(r, sourceName);
    }

    public DotParser(String in) throws IOException {
        this(in, "anonymous String");
    }

    public DotParser(String in, String sourceName) throws IOException {

        charStream = CharStreams.fromString(in, sourceName);
    }

    public Graphviz parse() throws ParseException {

        return parse(charStream);
    }

    public static Graphviz parse(CharStream charStream) throws ParseException {

        try {
            DOTLexer lexer = new DOTLexer(charStream);
            DOTParser p = new DOTParser(new CommonTokenStream(lexer));
            DOTParser.GraphContext graphCtx = p.graph();

            GraphvizListener gl = new GraphvizListener();
            new ParseTreeWalker().walk(gl, graphCtx);

            return gl.getGraphviz();
        } catch (ParseException pe) {
            pe.setSourceName(charStream.getSourceName());
            throw pe;
        }
    }
}
