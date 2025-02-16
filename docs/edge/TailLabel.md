# TailLabel

The **taillabel** attribute specifies **a label that appears near the tail (source) of an edge**. This is useful for **annotating the start of an edge** with additional information.

------

## **Behavior**

- **Places a label near the tail (source) node of an edge**.
- **Does not affect the position of the main edge label (`label`)**.
- **Works with all Graphviz layout engines**.
- **Can be styled using attributes like `fontcolor`, `fontsize`, and `fontname`**.

------

## **Usage in DOT**

```dot
digraph G {
    a -> b [label="Main Label", taillabel="Tail Label"];  // Adds a label near the tail node
}
```

### **Explanation**

- `a -> b [label="Main Label", taillabel="Tail Label"]`

   →

  - `"Main Label"` appears **in the middle of the edge**.
  - `"Tail Label"` appears **near node `a` (tail)**.

------

## **Usage in Java**

The Java API does not support `taillabel` directly; use `FloatLabel` to achieve a similar effect.

```java
Node a = Node.builder().id("a").build();
Node b = Node.builder().id("b").build();

FloatLabel tailLabel = FloatLabel.builder()
    .tend(Tend.TAIL)
    .label("Tail Label")
    .build();

// Edge with main label and tail label
Line edgeWithTailLabel = Line.builder(a, b)
    .label("Main Label")
    .floatLabels(tailLabel)  // Label at the tail of the edge
    .build();

Graphviz graph = Graphviz.digraph()
    .addLine(edgeWithTailLabel)
    .build();
```

