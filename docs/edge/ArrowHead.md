# ArrowHead

The **arrowhead** attribute defines the **shape of the arrow** at the **head (destination) of an edge**. Your **Java Graphviz version** supports the following **six arrow styles**.

------

## **Supported Arrowhead Styles**

| **Arrowhead** | **Effect**            | **Example DOT Code**         |
| ------------- | --------------------- | ---------------------------- |
| `NORMAL`      | Default arrowhead     | `a -> b [arrowhead=normal];` |
| `NONE`        | No arrow at the head  | `a -> b [arrowhead=none];`   |
| `DOT`         | Small dot arrow       | `a -> b [arrowhead=dot];`    |
| `VEE`         | Wide "V" shaped arrow | `a -> b [arrowhead=vee];`    |
| `BOX`         | Small box arrow       | `a -> b [arrowhead=box];`    |
| `CURVE`       | Curved arrow          | `a -> b [arrowhead=curve];`  |

⚠ **Note:** Only these **six arrowhead styles** are supported so far.

## **Behavior**

- **Defines the shape of the arrow at the edge head (target node)**.
- **Applies only to directed graphs (`digraph`)**.

------

## **Usage in DOT**

```dot
digraph G {
    a -> b [label="Default (Normal)", arrowhead=normal];
    a -> c [label="No Arrow", arrowhead=none];
    a -> d [label="Dot", arrowhead=dot];
    a -> e [label="Vee", arrowhead=vee];
    a -> f [label="Box", arrowhead=box];
    a -> g [label="Curve", arrowhead=curve];
}
```

### **Explanation**

- **`a -> b [arrowhead=normal]`** → Standard **default arrowhead**.
- **`a -> c [arrowhead=none]`** → No arrow at the head.
- **`a -> d [arrowhead=dot]`** → Small dot arrow.
- **`a -> e [arrowhead=vee]`** → Wide "V" shaped arrow.
- **`a -> f [arrowhead=box]`** → Small box arrow.
- **`a -> g [arrowhead=curve]`** → Curved arrow.

------

## **Usage in Java**

```java
Node a = Node.builder().id("a").build();
Node b = Node.builder().id("b").build();
Node c = Node.builder().id("c").build();
Node d = Node.builder().id("d").build();
Node e = Node.builder().id("e").build();
Node f = Node.builder().id("f").build();
Node g = Node.builder().id("g").build();

// Default arrowhead (normal)
Line normalArrow = Line.builder(a, b)
    .label("Default (Normal)")
    .arrowHead(ArrowShape.NORMAL) // Default arrow
    .build();

// No arrowhead
Line noArrow = Line.builder(a, c)
    .label("No Arrow")
    .arrowHead(ArrowShape.NONE) // No arrow
    .build();

// Dot arrowhead
Line dotArrow = Line.builder(a, d)
    .label("Dot")
    .arrowHead(ArrowShape.DOT) // Small dot
    .build();

// Vee arrowhead
Line veeArrow = Line.builder(a, e)
    .label("Vee")
    .arrowHead(ArrowShape.VEE) // Wide "V" shape
    .build();

// Box arrowhead
Line boxArrow = Line.builder(a, f)
    .label("Box")
    .arrowHead(ArrowShape.BOX) // Small box arrow
    .build();

// Curve arrowhead
Line curveArrow = Line.builder(a, g)
    .label("Curve")
    .arrowHead(ArrowShape.CURVE) // Curved arrow
    .build();

Graphviz graph = Graphviz.digraph()
    .addLine(normalArrow)
    .addLine(noArrow)
    .addLine(dotArrow)
    .addLine(veeArrow)
    .addLine(boxArrow)
    .addLine(curveArrow)
    .build();
```