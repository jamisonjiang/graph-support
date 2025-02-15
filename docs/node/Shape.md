# Shape

Specifies the shape of the node. The shape of a node determines its appearance and how it will be rendered in the graph. 

**Usage**:

Dot

```dot
digraph G {
    node [shape=ellipse];  // Set the shape of all nodes to ellipse
    a;
    b [shape=box];  // Explicitly set the shape of node 'b' to box
}
```

Java

```java
Node node = Node.builder()
    .shape(NodeShapeEnum.ELLIPSE)  // Set the shape of the node to ellipse
    .build();
```

## Available Shapes

```dot
digraph shapes_demo {
    size=10
    edge[style=invis]
    // Basic Shapes
    note           [shape=note           label="note"];
    plain          [shape=plain          label="plain"];
    plaintext      [shape=plaintext      label="plaintext"];
    underline      [shape=underline      label="underline"];
    ellipse        [shape=ellipse        label="ellipse"];
    circle         [shape=circle         label="circle"];
    box            [shape=box            label="box"];
    rect           [shape=rect           label="rect"];
    point          [shape=point          label="point"];
    triangle       [shape=triangle       label="triangle"];
    invtriangle    [shape=invtriangle    label="invtriangle"];
    diamond        [shape=diamond        label="diamond"];
    trapezium      [shape=trapezium      label="trapezium"];
    invtrapezium   [shape=invtrapezium   label="invtrapezium"];
    parallelogram  [shape=parallelogram  label="parallelogram"];
    star           [shape=star           label="star"];
    cylinder       [shape=cylinder       label="cylinder"];

    // Regular Polygons
    pentagon       [shape=pentagon       label="pentagon"];
    hexagon        [shape=hexagon        label="hexagon"];
    septagon       [shape=septagon       label="septagon"];
    octagon        [shape=octagon        label="octagon"];

    // A regular polyline (by default 4 sides, but can be overridden in some Graphviz variants)
    regular_polyline [shape=regular_polyline label="regular_polyline" sides=20];

    // Record-Based Shapes
    // Demonstrating a record with two fields
    record_example [shape=record   label="<f1>field1 | <f2>field2"];
    mrecord_example[shape=m_record label="<f1>field1 | <f2>field2"];

    note -> plain -> plaintext -> underline -> ellipse
    circle -> box -> rect -> point -> triangle
    invtriangle -> diamond -> trapezium -> invtrapezium -> parallelogram
    star -> cylinder -> pentagon -> hexagon -> septagon
    octagon -> regular_polyline -> record_example -> mrecord_example
}
```

![](E:\github\graph-support\docs\images\node_shape.png)