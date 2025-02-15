# Node Attributes

## Common attributes for Dot/API

Below is a sample of completed documentation for the `graph-support` library, specifically describing how to use DOT node attributes effectively.

------

# **graph-support: Node Attributes**

The `graph-support` library provides a robust API for defining and manipulating graph nodes, supporting a wide range of attributes such as labels, shapes, colors, sizes, and advanced layouts.

## **Node Attributes**

### **ID**

Sets a unique identifier for the node. If not specified, it is automatically assigned during rendering.
 **Note**: If multiple nodes share the same ID, only one will be rendered, though all will occupy space.

**Usage**:

```java
Node node = Node.builder().id("uniqueId").build();
```

------

### **Label**

Defines the node's text content. Labels can be simple strings or **Cell Expressions** for complex layouts.

- **Cell Expressions** allow horizontal and vertical layouts of fields within a node.
- Special characters like braces `{}`, vertical bars `|`, and angle brackets `<>` must be escaped with a backslash (`\`).

#### Example: Using a Simple Label

```java
Node node = Node.builder().label("Simple Label").build();
```

#### Example: Using Cell Expressions

```java
Node node = Node.builder().label("{A | B | {C | D}}").shape(NodeShapeEnum.RECORD).build();
```

#### Example: Advanced Cell Expression with Links

```java
Graphviz.digraph()
    .addNode(
        Node.builder()
            .shape(NodeShapeEnum.RECORD)
            .label("<f0> Left|<f1> Center|<f2> Right")
            .build()
    )
    .addLine(Line.builder("node1", "node2").tailCell("f0").headCell("f2").build())
    .build();
```

------

### **Dimensions (Height and Width)**

Controls the node's dimensions. If `fixedSize` is set to `true`, the height and width are fixed to these values. Otherwise, these dimensions act as minimum constraints.

**Usage**:

```java
Node node = Node.builder().width(1.5).height(2.0).fixedSize(true).build();
```

------

### **Shape**

Specifies the geometric shape of the node. For supported shapes, see `NodeShapeEnum`.

**Example**:

```java
Node node = Node.builder().shape(NodeShapeEnum.ELLIPSE).build();
```

------

### **Color and FillColor**

Defines the border and fill colors of the node.

**Usage**:

```java
Node node = Node.builder().color(Color.RED).fillColor(Color.LIGHT_GRAY).build();
```

------

------

### **Label Positioning**

- Horizontal Alignment

  : Set using 

  ```
  labeljust
  ```

  - Options: `Labeljust.LEFT`, `Labeljust.CENTER`, `Labeljust.RIGHT`.

- Vertical Alignment

  : Set using 

  ```
  labelloc
  ```

  - Options: `Labelloc.TOP`, `Labelloc.CENTER`, `Labelloc.BOTTOM`.

**Usage**:

```java
Node node = Node.builder()
    .label("Positioned Label")
    .labeljust(Labeljust.CENTER)
    .labelloc(Labelloc.TOP)
    .build();
```

------

### **Margin**

Sets the horizontal and vertical margin of the node.

**Usage**:

```java
Node node = Node.builder().margin(0.2, 0.4).build();
```

------

### **Image**

Assigns an image to the node. Supported formats include PNG and SVG.

**Usage**:

```java
// Http image
Node node = Node.builder()
    .image("https://example.com/image.png")
    .imageSize(1.0, 1.0)
    .build();

// Local image
Node node = Node.builder()
    .image("file:///E:/demo//example.jpg")
    .imageSize(1.0, 1.0)
    .build();
```

------

### **Tooltip**

Sets a tooltip to display additional information on hover, only take effect when node set href attribute

**Usage**:

```java
Node node = Node.builder().tooltip("This is a tooltip").build();
```

------

### **HTML Table as Label**

Allows the use of an HTML-like table structure for the label.

**Usage**:

```java
Table table = Html.table()
    .tr(Html.td().text("Header 1"), Html.td().text("Header 2"))
    .tr(Html.td().text("Row 1, Col 1"), Html.td().text("Row 1, Col 2"));

Node node = Node.builder().table(table).build();
```

------

## **Comprehensive Example**

```java
Graphviz.digraph()
    .addNode(Node.builder()
        .id("node1")
        .label("Hello World")
        .shape(NodeShapeEnum.RECORD)
        .color(Color.BLACK)
        .fillColor(Color.YELLOW)
        .fontName("Arial")
        .fontSize(12)
        .tooltip("This is node 1")
        .build()
    )
    .addNode(Node.builder()
        .id("node2")
        .labelTag(bold("Rich Text").br().italic("Example"))
        .shape(NodeShapeEnum.CIRCLE)
        .build()
    )
    .addLine(Line.builder("node1", "node2").build())
    .build();
```

This example demonstrates how to combine multiple attributes into a single graph structure.

## Only for Java API

### **LabelTag**

Assigns an HTML-like label structure for rich text formatting. Use this for bold, italic, or nested formatting.

**Usage**:

```java
LabelTag tag = bold("Main Title")
    .br()
    .italic("Subtitle")
    .underline(font("Important", fontAttrs().color(Color.BLUE).pointSize(18)));

Node node = Node.builder().labelTag(tag).build();
```

------

### **Font Attributes**

Customizes the font used for labels, including the font name, size, and color.

**Usage**:

```java
Node node = Node.builder()
    .fontName("Arial")
    .fontSize(12)
    .fontColor(Color.BLUE)
    .build();
```

------

### **Assemble**

Sets an `Assemble` object for precise layout of subcomponents within the node.

**Usage**:

```java
Assemble assemble = Assemble.builder()
    .width(1.0)
    .height(0.5)
    .addCell(0, 0, Node.builder().label("Left").build())
    .addCell(0.5, 0, Node.builder().label("Right").build())
    .build();

Node node = Node.builder().assemble(assemble).build();
```

