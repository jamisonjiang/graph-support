# Layout

The **layout** attribute controls the **algorithm used to arrange nodes and edges** in the graph. Different layout engines produce **different visual structures** based on their algorithms.

------

## **Supported Layouts and Behavior**

| **Layout** | **Description**                                              |
| ---------- | ------------------------------------------------------------ |
| **DOT**    | **Classic hierarchical layout** for directed graphs. Tries to keep edges in a consistent direction (e.g., top to bottom or left to right). Reduces edge crossings and optimizes edge length. Provides the highest quality layout but may take longer for large graphs. |
| **DOTQ**   | **Optimized hierarchical layout** for directed graphs. Similar to DOT but with improved performance for large graphs through optimized x-position calculation. Provides faster layout times while maintaining good visual quality. |
| **FDP**    | **Force-directed placement (FDP)**, where nodes are treated as charged particles that repel each other, and edges act as springs pulling nodes together. Ideal for **undirected graphs**. |
| **JFDP**   | **Optimized force-directed placement (JFDP)** with improved stability, degree-based scaling of attractive forces, and efficient repulsion calculations. |
| **GFDP**   | **Grid-based force-directed placement (GFDP)**, optimized for **dense graphs** by considering only local node interactions within their cell area. Reduces computational complexity. |

------

## **DOT vs DOTQ Comparison**

| **Aspect** | **DOT** | **DOTQ** |
|------------|---------|----------|
| **Quality** | Highest quality layout | Good quality layout |
| **Performance** | Slower for large graphs | Faster for large graphs |
| **Algorithm** | Classic network simplex | Optimized network simplex + Brandes/Köpf |
| **Default nslimit** | 100,000 | 5,000 |
| **Best for** | Small to medium graphs, highest quality requirements | Large graphs, performance requirements |

------

## **Usage in DOT**

```dot
graph G {
    layout=dotq  # Use optimized DOT layout
    a--b
    a--b
    a--c
    a--c
    a--d
    a--d
    a--e
    a--e
    b--c
    b--c
    b--d
    b--d
    b--e
    b--e
    c--d
    c--d
    c--e
    c--e
    d--e
    d--e
}
```

### **Explanation:**

- **`layout=dot`** → Uses the **classic hierarchical layout** (best for directed graphs with highest quality requirements).
- **`layout=dotq`** → Uses the **optimized hierarchical layout** (best for large directed graphs with performance requirements).
- **`layout=fdp`** → Uses a **force-directed layout** (best for undirected graphs).

------

## **Usage in Java**

```java
// Define nodes
Node a = Node.builder().label("a").build();
Node b = Node.builder().label("b").build();
Node c = Node.builder().label("c").build();
Node d = Node.builder().label("d").build();
Node e = Node.builder().label("e").build();

// Define graph with DOTQ layout for better performance
Graphviz graph = Graphviz.graph()
    .layout(Layout.DOTQ) // Use optimized DOT layout
    .addLine(Line.builder(a, b).build())
    .addLine(Line.builder(a, b).build())
    .addLine(Line.builder(a, c).build())
    .addLine(Line.builder(a, c).build())
    .addLine(Line.builder(a, d).build())
    .addLine(Line.builder(a, d).build())
    .addLine(Line.builder(a, e).build())
    .addLine(Line.builder(a, e).build())
    .addLine(Line.builder(b, c).build())
    .addLine(Line.builder(b, c).build())
    .addLine(Line.builder(b, d).build())
    .addLine(Line.builder(b, d).build())
    .addLine(Line.builder(b, e).build())
    .addLine(Line.builder(b, e).build())
    .addLine(Line.builder(c, d).build())
    .addLine(Line.builder(c, d).build())
    .addLine(Line.builder(c, e).build())
    .addLine(Line.builder(c, e).build())
    .addLine(Line.builder(d, e).build())
    .addLine(Line.builder(d, e).build())
    .build();
```