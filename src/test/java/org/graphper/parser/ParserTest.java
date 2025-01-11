package org.graphper.parser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.graphper.api.FileType;
import org.graphper.api.Graphviz;
import org.graphper.draw.ExecuteException;
import org.graphper.parser.grammar.HTMLLexer;
import org.graphper.parser.grammar.HTMLParser;
import org.graphper.parser.grammar.HTMLParser.HtmlAttributeContext;
import org.graphper.parser.grammar.HTMLParser.HtmlContentContext;
import org.graphper.parser.grammar.HTMLParser.HtmlElementContext;
import org.graphper.parser.grammar.HTMLParserBaseListener;
import org.graphper.util.CollectionUtils;
import org.junit.jupiter.api.Test;

/**
 * Visual tests for DotParser
 */
public class ParserTest {

    @Test
    public void cluster() throws Exception {

        String file = "./src/test/resources/parser/cluster.dot";
        parseAndRender(file);
    }

    @Test
    public void edgeSubgraphLeft() throws Exception {

        String file = "./src/test/resources/parser/edgeSubgraphLeft.dot";
        parseAndRender(file);
    }

    @Test
    public void edgeSubgraphMiddle() throws Exception {

        String file = "./src/test/resources/parser/edgeSubgraphMiddle.dot";
        parseAndRender(file);
    }

    @Test
    public void edgeSubgraphRight() throws Exception {

        String file = "./src/test/resources/parser/edgeSubgraphRight.dot";
        parseAndRender(file);
    }

    @Test
    public void edgeSubgraphCombined() throws Exception {

        String file = "./src/test/resources/parser/edgeSubgraphCombined.dot";
        parseAndRender(file);
    }

    private static void parseAndRender(String file) throws IOException, ExecuteException {

        Graphviz graphviz = new DotParser(new File(file), StandardCharsets.UTF_8).parse();

        File dir = new File("./target/graphviz");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        graphviz.toFile(FileType.SVG).save("./target/graphviz", graphviz.id());
    }

    @Test
    public void testHtml() {
        String html = "<table color=\"red\" border=\"1\">\n"
            + "         <tr>\n"
            + "             <td color=\"red\" border=\"1\">\n"
            + "                 <table color=\"blue\" border=\"1\">\n"
            + "                     <tr><td>111</td></tr>\n"
            + "                     <tr><td>222</td></tr>\n"
            + "                 </table>\n"
            + "             </td>\n"
            + "             <td>second 333</td>\n"
            + "         </tr>\n"
            + "     </table>";

        HTMLLexer lexer = new HTMLLexer(CharStreams.fromString(html));
        HTMLParser p = new HTMLParser(new CommonTokenStream(lexer));

        p.removeErrorListeners();
        lexer.removeErrorListeners();

        DotSyntaxErrorListener dotSyntaxErrorListener = new DotSyntaxErrorListener();
        p.addErrorListener(dotSyntaxErrorListener);
        lexer.addErrorListener(dotSyntaxErrorListener);

        HTMLParserBaseListener listener = new HTMLParserBaseListener() {

            @Override
            public void enterHtmlElement(HtmlElementContext ctx) {
//                System.out.println("--------------------------");
//                System.out.println(ctx.getText());
            }

            @Override
            public void enterHtmlAttribute(HtmlAttributeContext ctx) {
//                System.out.println(ctx.getText());
            }

            @Override
            public void enterHtmlContent(HtmlContentContext ctx) {
                if (CollectionUtils.isEmpty(ctx.htmlElement())) {
                    System.out.println(ctx.getText());
                }
            }
        };
        new ParseTreeWalker().walk(listener, p.htmlDocument());


    }

}
