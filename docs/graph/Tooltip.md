# **Tooltip**

The **tooltip** attribute defines **hover text** for the entire graph, but it only works when the graph has an **href (clickable link)** and is rendered in **SVG format**.

------

## **Examples**

### **DOT Syntax**

```dot
digraph G {
    href="https://github.com/";  // Tooltip requires href
    tooltip="This is a graph tooltip";  // Hover text for the graph
    label="Hover Over Me";
    
    subgraph cluster_0 {
        label="Subgraph";
        a -> b;
    }
}
```

------

### **Java Usage**

```java
Node a = Node.builder().label("a").build();

Graphviz graph = Graphviz.digraph()
    .href("https://example.com") // Tooltip requires href
    .tooltip("This is a graph tooltip") // Set hover text
    .label("Hover Over Me")
    .addNode(a)
    .build();
```