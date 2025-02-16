# HeadClip

The **headclip** attribute controls whether an edge **clips at the boundary of the head node** (the target node) or extends **inside the node shape**.

------

## **Behavior**

- **`headclip=true` (default)** → The edge stops at the **boundary of the head node**.
- **`headclip=false`** → The edge extends **inside the head node**, connecting directly to its center.

------

## **Usage in DOT**

```dot
digraph G {
    a -> b [label="Default (Clipped)"];  // Default behavior (headclip=true)
    a -> c [label="Not Clipped", headclip=false];
}
```

### **Explanation**

- **`a -> b`** → Default behavior (**headclip=true**), edge **stops at the boundary** of node `b`.
- **`a -> c`** → Edge extends **inside** node `c` (**headclip=false**).

------

## **Usage in Java**

```java
Node a = Node.builder().id("a").build();
Node b = Node.builder().id("b").build();
Node c = Node.builder().id("c").build();

// Default behavior (clipped at the head node boundary)
Line defaultEdge = Line.builder(a, b)
    .label("Default (Clipped)")
    .build();

// Edge extends inside the head node
Line unclippedEdge = Line.builder(a, c)
    .label("Not Clipped")
    .headclip(false) // Allows the edge to extend inside the target node
    .build();

Graphviz graph = Graphviz.digraph()
    .addLine(defaultEdge)
    .addLine(unclippedEdge)
    .build();
```