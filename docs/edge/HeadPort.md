# HeadPort

The **headport** attribute specifies **which part of the destination (head) node** the edge should connect to. This allows fine-grained control over **how edges attach to nodes**, improving edge routing and layout clarity.

------

## **Behavior**

- **Determines which part of the head (destination) node the edge should attach to**.
- **Equivalent to using `node:port` notation (`a -> b:sw` is the same as `a -> b [headport=sw]`)**.
- **Works best with nodes that have distinct ports (e.g., `record`, `Mrecord`, or non-circular shapes)**.
- **Improves edge readability and reduces edge overlap in complex graphs**.

------

## **Available Ports for Head Connection**

| **Port** | **Description**                                     | **Example DOT Code**    |
| -------- | --------------------------------------------------- | ----------------------- |
| `n`      | Connects at the **top** (north) of the node         | `a -> b [headport=n];`  |
| `s`      | Connects at the **bottom** (south) of the node      | `a -> b [headport=s];`  |
| `e`      | Connects at the **right** (east) of the node        | `a -> b [headport=e];`  |
| `w`      | Connects at the **left** (west) of the node         | `a -> b [headport=w];`  |
| `ne`     | Connects at the **top-right corner** (northeast)    | `a -> b [headport=ne];` |
| `nw`     | Connects at the **top-left corner** (northwest)     | `a -> b [headport=nw];` |
| `se`     | Connects at the **bottom-right corner** (southeast) | `a -> b [headport=se];` |
| `sw`     | Connects at the **bottom-left corner** (southwest)  | `a -> b [headport=sw];` |

⚠ **Note:** `headport` only affects the **head (destination) node**. Use **`tailport`** to control the connection point at the **source (tail) node**.

------

## **Usage in DOT**

```dot
digraph G {
    a -> b [label="Default Connection"];  // Default behavior (centers the edge)
    a -> c [label="Connects at Bottom", headport=s];  // Connects to bottom of `c`
    a -> d [label="Connects at Top-Left", headport=nw];  // Connects to top-left of `d`
    a -> e [label="Connects at Bottom-Right", headport=se];  // Connects to bottom-right of `e`

    // Equivalent notation using "node:port"
    a -> f:sw [label="Equivalent to headport=sw"];
}
```

### **Explanation**

- **`a -> b`** → Uses **default connection** (typically center of node).
- **`a -> c [headport=s]`** → Edge **connects to the bottom** of `c`.
- **`a -> d [headport=nw]`** → Edge **connects to the top-left corner** of `d`.
- **`a -> e [headport=se]`** → Edge **connects to the bottom-right corner** of `e`.
- **`a -> f:sw`** → **Equivalent to `a -> f [headport=sw]`**, edge connects to bottom-left.

------

## **Usage in Java**

```java
Node a = Node.builder().id("a").build();
Node b = Node.builder().id("b").build();
Node c = Node.builder().id("c").build();
Node d = Node.builder().id("d").build();
Node e = Node.builder().id("e").build();
Node f = Node.builder().id("f").build();

// Default connection (center)
Line defaultEdge = Line.builder(a, b)
    .label("Default Connection")
    .build();

// Connects at bottom (south)
Line bottomEdge = Line.builder(a, c)
    .label("Connects at Bottom")
    .headPort(Port.SOUTH) // Connects to bottom of head node
    .build();

// Connects at top-left (northwest)
Line topLeftEdge = Line.builder(a, d)
    .label("Connects at Top-Left")
    .headPort(Port.NORTH_WEST) // Connects to top-left of head node
    .build();

// Connects at bottom-right (southeast)
Line bottomRightEdge = Line.builder(a, e)
    .label("Connects at Bottom-Right")
    .headPort(Port.SOUTH_EAST) // Connects to bottom-right of head node
    .build();

// Equivalent to "a -> f:sw"
Line bottomLeftEdge = Line.builder(a, f)
    .label("Equivalent to headport=sw")
    .headPort(Port.SOUTH_WEST) // Connects to bottom-left
    .build();

Graphviz graph = Graphviz.digraph()
    .addLine(defaultEdge)
    .addLine(bottomEdge)
    .addLine(topLeftEdge)
    .addLine(bottomRightEdge)
    .addLine(bottomLeftEdge)
    .build();
```