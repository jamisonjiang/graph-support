# **K **

The **k** attribute sets the **spring constant** for force-directed layouts, specifically **FDP, JFDP, and GFDP**. It controls the "stiffness" of the virtual springs that determine node spacing.

------

## **Usage in DOT**

```dot
graph G {
    layout=fdp;  
    K=2.0;  // Stronger springs pull nodes closer
    
    a -- b;
    b -- c;
    c -- d;
}
```

------

## **Usage in Java**

```java
Node a = Node.builder().label("a").build();
Node b = Node.builder().label("b").build();
Node c = Node.builder().label("c").build();

Graphviz graph = Graphviz.graph()
    .layout(Layout.FDP)  // Use FDP layout
    .k(2.0)  // Stronger springs pull nodes closer
    .addLine(a, b, c)
    .build();
```

