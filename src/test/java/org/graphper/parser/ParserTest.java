package org.graphper.parser;

import org.graphper.api.FileType;
import org.graphper.api.Graphviz;
import org.graphper.draw.ExecuteException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

}
