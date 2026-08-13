# **Href**

The **href** attribute (alias: **url**) adds a **clickable hyperlink** to the entire graph, making the graph title interactive in `SVG` outputs.

Only relative links and the `http`, `https`, and `mailto` schemes are emitted. Dangerous schemes
are omitted.

------

## **Examples**

### **DOT Syntax**

```dot
digraph G {
    href="https://github.com/";  // Clickable graph link
    label="Clickable Graph";
    a
}
```

------

### **Java Usage**

```java
Node a = Node.builder().label("a").build();

Graphviz graph = Graphviz.digraph()
    .label("Clickable Graph")
    .href("https://github.com/") // Set hyperlink
    .addNode(a)
    .build();
```
