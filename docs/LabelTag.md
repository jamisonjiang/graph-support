# Label Tag

The **Label Tag** is an **HTML-like structure** designed to provide a **rich text formatting experience**.

------

### **Supported Tags**

1. **\<BR\>**

   - Inserts a line break.

   - Example (DOT)

     ```dot
     n1 [label=<Line 1<BR/>Line 2<BR/>Line 3>];
     ```

   - Example (Java)

     ```java
     LabelTag label = text("Line 1").br().text("Line 2").br().text("Line 3");
     Node.builder().labelTag(label).build();
     ```

2. **\<FONT\>**

   - Styles the font with size, color, and typeface.

   - Attributes

     - `POINT-SIZE`: Specifies font size (e.g., `16`).
     - `COLOR`: Sets the font color (e.g., `blue`, `#FF0000`).
     - `FACE`: Specifies the font family (e.g., `Arial`, `Courier`).

   - Example (DOT)

     ```dot
     n2 [label=<
         <FONT POINT-SIZE="16" COLOR="blue" FACE="Arial">Hello, World!</FONT>
     >];
     ```

   - Example (Java)

     ```java
     LabelTag label = font("Hello, World!", fontAttrs().color(Color.BLUE).pointSize(16).face("Arial"));
     Node.builder().labelTag(label).build();
     ```

3. **``**

   - Makes the text bold.

   - Example (DOT)

     :

     ```dot
     n3 [label=<
         <B>Bold Text</B>
     >];
     ```

   - Example (Java)

     :

     ```java
     LabelTag label = bold("Bold Text");
     Node.builder().labelTag(label).build();
     ```

4. **``**

   - Makes the text italicized.

   - Example (DOT)

     :

     ```dot
     n4 [label=<
         <I>Italic Text</I>
     >];
     ```

   - Example (Java)

     :

     ```java
     LabelTag label = italic("Italic Text");
     Node.builder().labelTag(label).build();
     ```

5. **``**

   - Underlines the text.

   - Example (DOT)

     :

     ```dot
     n5 [label=<
         <U>Underlined Text</U>
     >];
     ```

   - Example (Java)

     :

     ```java
     LabelTag label = underline("Underlined Text");
     Node.builder().labelTag(label).build();
     ```

6. **``**

   - Strikes through the text.

   - Example (DOT)

     :

     ```dot
     n6 [label=<
         <S>Strikethrough Text</S>
     >];
     ```

   - Example (Java)

     :

     ```java
     LabelTag label = strikeThrough("Strikethrough Text");
     Node.builder().labelTag(label).build();
     ```

7. **``**

   - Adds subscript formatting to text.

   - Example (DOT)

     :

     ```dot
     n7 [label=<
         H<SUB>2</SUB>O
     >];
     ```

   - Example (Java)

     :

     ```java
     LabelTag label = text("H").subscript("2").text("O");
     Node.builder().labelTag(label).build();
     ```

8. **``**

   - Adds superscript formatting to text.

   - Example (DOT)

     :

     ```dot
     n8 [label=<
         X<SUP>2</SUP> + Y<SUP>2</SUP>
     >];
     ```

   - Example (Java)

     :

     ```java
     LabelTag label = text("X").superscript("2").text(" + ").text("Y").superscript("2");
     Node.builder().labelTag(label).build();
     ```

9. **``**

   - Aligns content vertically at the top.

   - Example (DOT)

     :

     ```dot
     n9 [label=<
         <VT>Top-Aligned Text</VT>
     >];
     ```

   - Example (Java)

     :

     ```java
     LabelTag label = top("Top-Aligned Text");
     Node.builder().labelTag(label).build();
     ```

10. **``**

    - Aligns content vertically at the bottom.

    - Example (DOT)

      :

      ```dot
      n10 [label=<
          <VB>Bottom-Aligned Text</VB>
      >];
      ```

    - Example (Java)

      :

      ```java
      LabelTag label = bottom("Bottom-Aligned Text");
      Node.builder().labelTag(label).build();
      ```

11. **``**

    - Centers content vertically.

    - Example (DOT)

      :

      ```dot
      n11 [label=<
          <VC>Vertically Centered Text</VC>
      >];
      ```

    - Example (Java)

      :

      ```java
      LabelTag label = verticalCenter("Vertically Centered Text");
      Node.builder().labelTag(label).build();
      ```

12. **``**

    - Aligns content horizontally to the left.

    - Example (DOT)

      :

      ```dot
      n12 [label=<
          <HL>Left-Aligned Text</HL>
      >];
      ```

    - Example (Java)

      :

      ```java
      LabelTag label = left("Left-Aligned Text");
      Node.builder().labelTag(label).build();
      ```

13. **``**

    - Aligns content horizontally to the right.

    - Example (DOT)

      :

      ```dot
      n13 [label=<
          <HR>Right-Aligned Text</HR>
      >];
      ```

    - Example (Java)

      :

      ```java
      LabelTag label = right("Right-Aligned Text");
      Node.builder().labelTag(label).build();
      ```

14. **``**

    - Centers content horizontally.

    - Example (DOT)

      :

      ```dot
      n14 [label=<
          <HC>Horizontally Centered Text</HC>
      >];
      ```

    - Example (Java)

      :

      ```java
      LabelTag label = center("Horizontally Centered Text");
      Node.builder().labelTag(label).build();
      ```

------

### **Comprehensive DOT Example**

```dot
digraph {
    n1 [label=<Line 1<BR/>Line 2<BR/>Line 3>];
    n2 [label=<
        <FONT POINT-SIZE="16" COLOR="blue" FACE="Arial">Styled Text</FONT>
    >];
    n3 [label=<
        <B>Bold</B> <I>Italic</I> <U>Underline</U>
    >];
    n4 [label=<H<SUB>2</SUB>O + CO<SUB>2</SUB>>];
    n5 [label=<
        <S><FONT COLOR="red">Strikethrough and Colored</FONT></S>
    >];
    n6 [label=<
        <VB>Bottom Aligned Text</VB>
    >];
    n7 [label=<
        <VC>Vertically Centered</VC>
    >];
}
```

------

### **Comprehensive Java Example**

```java
LabelTag label = bold("Bold Text")
    .br()
    .italic("Italic Text")
    .br()
    .underline("Underlined Text")
    .br()
    .text("H").subscript("2").text("O + CO").subscript("2")
    .br()
    .strikeThrough(font("Strikethrough Text", fontAttrs().color(Color.RED).pointSize(18)))
    .br()
    .top("Top-Aligned Text")
    .br()
    .bottom("Bottom-Aligned Text")
    .br()
    .verticalCenter(bold("Vertically Centered Text"));

Node.builder().labelTag(label).build();
```