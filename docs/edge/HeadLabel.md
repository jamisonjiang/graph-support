# **Headlabel**

The **headlabel** attribute specifies **a label that appears near the head (destination) of an edge**. This is useful for **annotating the end of an edge** with additional information. Additionally, **`headlabel` supports HTML tags** and **tables** for rich text formatting and structured content.

------

## **Behavior**

- **Places a label near the head (destination) node of an edge**.
- **Does not affect the position of the main edge label (`label`)**.
- **Works with all layout engines**.
- **Supports HTML-like tags** and **tables** for rich formatting and structured labels.

------

## **Usage in DOT**

### **Basic Usage: Head Label**

```dot
digraph G {
    ranksep=3
    a -> b [label="Main Label", headlabel="Head Label"];  // Adds a label near the head node
}
```

### **Using HTML Formatting in `headlabel`**

```dot
digraph G {
    ranksep=3
    a -> b [label="Main Label", headlabel=<<B>Bold Text</B>>];  // Head label with bold text
    b -> c [label="Main Label", headlabel=<<FONT COLOR="red">Red Text</FONT>>];  // Head label with red text
}
```

### **Using Tables in `headlabel`**

```dot
digraph G {
    ranksep=3
    a -> b [label="Main Label", headlabel=<
        <TABLE BORDER="1" CELLBORDER="1" CELLSPACING="0">
            <TR><TD>Row 1, Col 1</TD><TD>Row 1, Col 2</TD></TR>
            <TR><TD>Row 2, Col 1</TD><TD>Row 2, Col 2</TD></TR>
        </TABLE>
    >];  // Head label with a table structure
}
```

------

## **Usage in Java**

### **Basic Usage: Head Label**

```java
Node a = Node.builder().id("a").build();
Node b = Node.builder().id("b").build();

FloatLabel headLabel = FloatLabel.builder()
    .tend(Tend.HEAD)
    .label("Head Label")
    .build();

// Edge with main label and head label
Line edgeWithHeadLabel = Line.builder(a, b)
    .label("Main Label")
    .floatLabels(headLabel)  // Label at the head of the edge
    .build();

Graphviz graph = Graphviz.digraph()
    .rankSep(2)
    .addLine(edgeWithHeadLabel)
    .build();
```

### **Using HTML Formatting in `headlabel`**

```java
Node a = Node.builder().id("a").build();
Node b = Node.builder().id("b").build();

FloatLabel tailLabel = FloatLabel.builder()
    .tend(Tend.HEAD)
    .labelTag(italic(underline("Head label")))
    .build();

// Edge with main label and tail label
Line edgeWithTailLabel = Line.builder(a, b)
    .label("Main Label")
    .floatLabels(tailLabel)  // Label at the tail of the edge
    .build();

Graphviz graph = Graphviz.digraph()
    .rankSep(2)
    .addLine(edgeWithTailLabel)
    .build();
```

### **Using Tables in `headlabel`**

```java
Node a = Node.builder().id("a").build();
Node b = Node.builder().id("b").build();

FloatLabel tailLabel = FloatLabel.builder()
    .tend(Tend.HEAD)
    .table(
        table()
            .border(1)
            .cellBorder(1)
            .cellSpacing(0)
            .tr(td().text("Row 1, Col 1"), td().text("Row 1, Col 2"))
            .tr(td().text("Row 2, Col 1"), td().text("Row 2, Col 2"))
    )
    .build();

// Edge with main label and tail label
Line edgeWithTailLabel = Line.builder(a, b)
    .label("Main Label")
    .floatLabels(tailLabel)  // Label at the tail of the edge
    .build();

Graphviz graph = Graphviz.digraph()
    .rankSep(2)
    .addLine(edgeWithTailLabel)
    .build();
```