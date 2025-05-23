# graph-support

## About

`graph-support` is a lightweight Java re-implementation of [Graphviz](https://graphviz.org/) for **parsing**, **layout**, and **rendering** graphs.

## **Highlights**

- **Dual Support for Java API and DOT Script**:
   Use the **Java API** or **DOT script**, depending on which method is more convenient for your use case.
- **Lightweight Design**:
   `graph-support` is designed to be **lightweight**, ensuring stability and efficiency. Unlike other graphing tools, `graph-support` **does not require** Graphviz binaries, JavaScript runtime engines, or external renderers, making it ideal for **embedding into applications**.
- **Multi-Platform Support**:
   Compatible with **Mac, Linux, Windows, and Android platforms**, ensuring seamless integration across different environments.

------

## Useful information

* [Release](https://github.com/jamisonjiang/graph-support/releases)
* [Documentation](./docs)
* Quick build: 
  * Latest stable [OpenJDK 8](https://adoptium.net/)
  * Latest stable [Apache Maven](https://maven.apache.org/)
  * Run `mvn clean install`

## Using by CLI

1. **Download** the [latest CLI JAR](https://github.com/jamisonjiang/graph-support/releases/tag/1.5.0).

2. Render by file

   * Prepare a DOT file

   * Run

     ```java
     java -jar graph-support-cli.jar example.dot -o example -Tpng
     ```

3. Render by script string

   ```java
   java -jar graph-support-cli.jar -s "digraph {a->b->c->d}" -o test -Tpng
   ```

4. Help

   ```shell
   java -jar graph-support-cli.jar -h
   ```

   Shows usage and options.

------

## Using in Code

### Render by Java API

If you **do not need** DOT parsing, only the Core is required:

```xml
<dependency>
    <groupId>org.graphper</groupId>
    <artifactId>graph-support-core</artifactId>
    <version>1.5.1</version>
</dependency>
```

#### Example

```java
public class Example {
    public static void main(String[] args) {
        // Node definition
        Node nd_1 = Node.builder().label("Node 1").build();
        Node nd_2 = Node.builder().label("Node 2").build();
        Node nd_3_a = Node.builder().label("Above Right Node 3").build();
        Node nd_3_l = Node.builder().label("Left of Node 3").build();
        Node nd_3 = Node.builder().label("Node 3").build();
        Node nd_3_r = Node.builder().label("Right of Node 3").build();
        Node nd_4 = Node.builder().label("Node 4").build();

        Graphviz graphviz = Graphviz.digraph()
            // Node attribute template
            .tempNode(Node.builder().shape(NodeShapeEnum.RECT).build())
            // Edges in root graph
            .addLine(nd_3_a, nd_3_r)
            .addLine(nd_1, nd_2, nd_3, nd_4)
            // Use Cluster to wrap the corresponding nodes and edges
            .cluster(
                Cluster.builder()
                    // Edge attribute template
                    .tempLine(Line.tempLine().color(Color.GREY).arrowHead(ArrowShape.NONE).build())
                    // Use Subgraph to keep certain nodes on the same rank
                    .subgraph(
                        Subgraph.builder()
                            .rank(Rank.SAME)
                            .addNode(nd_3_l, nd_3, nd_3_r)
                            .build()
                    )
                    .addLine(nd_3_l, nd_3, nd_3_r)
                    .build()
            )
            .build();

        // Save PNG
        try {
            graphviz.toFile(FileType.PNG).save("./", "example");
            System.out.println("Saved 'example.png'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Render by Dot Script

If you want to **parse** a `.dot` string or file, import the DOT module:

```xml
<dependency>
    <groupId>org.graphper</groupId>
    <artifactId>graph-support-dot</artifactId>
    <version>1.5.1</version>
</dependency>
```

Then:

```java
import org.graphper.dot.DotParser;

public class DotExample {
    public static void main(String[] args) {
        String dot = "digraph { a -> b }";

        Graphviz graphviz = DotParser.parse(dot);
        try {
            graphviz.toFile(FileType.PNG).save("./", "example");
            System.out.println("Saved 'example.png'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

------

## Examples

### Record node

![Record Node](test/picture/node_record.png)

### HTML Label

![Record Node](test/picture/rich_text.png)

### HTML Table

![Record Node](test/picture/table.png)

### Edge router

![Record Node](test/picture/line_router.png)

### Layout

![Record Node](test/picture/layout.png)

### Edge Router Debug

Some properties can be used to debug how edges are routed:

#### Show control points

<img src="test/picture/show_control_points.png" width="477" alt="Show Control Points" />

#### Show edge router boxes

<img src="test/picture/show_boxes.png" width="379" alt="Show Boxes" />

#### Show GRID in ORTHO

<img src="test/picture/show_grid.png" width="449" alt="Show Grid" />

### Edge Port

<img src="test/picture/line_port.png" width="591" alt="Edge Port" />

------

## Layout Engine Only (Advanced)

If you want **only** the layout engine to compute node coordinates or line segments (without generating images), you can do:

```java
Node a = Node.builder().label("a").build();
Node b = Node.builder().label("b").build();

Graphviz graphviz = Graphviz.digraph()
    .addLine(a, b)
    .build();

// Use the layout engine directly; skip rendering
DrawGraph drawGraph = Layout.DOT.getLayoutEngine().layout(graphviz);

for (NodeDrawProp node : drawGraph.nodes()) {
  double x = node.getX();
  double y = node.getY();
  // Integrate these coordinates in your custom GUI or app
}

for (LineDrawProp line : drawGraph.lines()) {
  // Each line can have multiple segments or Bezier curves
  if (line.isBesselCurve()) {
    // interpret sets of 4 control points
  } else {
    // a simple polyline
  }
}
```

------

## Contributing

We welcome developers to contribute new features, bug fixes, or improvements! Some areas we’d love help with:

- More node/arrow shapes
- Additional styling options
- New line-routing or layout algorithms

Simply **fork** the repo, create a feature branch, and open a Pull Request. Thank you for your interest in **graph-support**!