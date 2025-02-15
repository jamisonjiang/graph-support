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

3. **\<B\>**

   - Makes the text bold.

   - Example (DOT)

     ```dot
     n3 [label=<
        <B>Bold Text</B>
     >];
     ```
     
   - Example (Java)

     ```java
     LabelTag label = bold("Bold Text");
     Node.builder().labelTag(label).build();
     ```
   
4. **\<I\>**

   - Makes the text italicized.

   - Example (DOT)

     ```dot
     n4 [label=<
         <I>Italic Text</I>
     >];
     ```
     
   - Example (Java)

     ```java
     LabelTag label = italic("Italic Text");
     Node.builder().labelTag(label).build();
     ```
   
5. **\<U\>**

   - Underlines the text.

   - Example (DOT)

     ```dot
     n5 [label=<
         <U>Underlined Text</U>
     >];
     ```
     
   - Example (Java)

     ```java
     LabelTag label = underline("Underlined Text");
     Node.builder().labelTag(label).build();
     ```
   
6. **\<S\>**

   - Strikes through the text.

   - Example (DOT)

     ```dot
     n6 [label=<
         <S>Strikethrough Text</S>
     >];
     ```
     
   - Example (Java)

     ```java
     LabelTag label = strikeThrough("Strikethrough Text");
     Node.builder().labelTag(label).build();
     ```
   
7. **\<O\>**

   * Places a line over the text, creating an overline effect.

   * **Example (DOT)**:

     ```dot
     n7 [label=< <O>Overlined Text</O> >];
     ```

   * **Example (Java)**:

     ```
     LabelTag label = overline("Overlined Text");
     Node.builder().labelTag(label).build();
     ```

8. **\<SUB\>**

   - Adds subscript formatting to text.

   - Example (DOT)

     ```dot
     n7 [label=<
         H<SUB>2</SUB>O
     >];
     ```
     
   - Example (Java)

     ```java
     LabelTag label = text("H").subscript("2").text("O");
     Node.builder().labelTag(label).build();
     ```

9. **\<SUP\>**

   - Adds superscript formatting to text.

   - Example (DOT)

     ```dot
     n8 [label=<
         X<SUP>2</SUP> + Y<SUP>2</SUP>
     >];
     ```
     
   - Example (Java)

     ```java
     LabelTag label = text("X").superscript("2").text(" + ").text("Y").superscript("2");
     Node.builder().labelTag(label).build();
     ```

10. **\<VT\>**

   - Aligns the content vertically at the top of the label, useful when multiple text elements with varying font sizes are placed on the same line.

   - Example (DOT)

     ```dot
     n10 [label=<
         <VT>Top-Aligned</VT>
         <FONT POINT-SIZE=40>NEXT</FONT>
     >];
     ```
     
   - Example (Java)

     ```java
     LabelTag label = top("Top-Aligned Text").font("NEXT", fontAttrs().pointSize(40));
     Node.builder().labelTag(label).build();
     ```

11. **\<VB\>**

    - Aligns the content vertically at the bottom of the label, useful for aligning text in a row with different font sizes.

    - Example (DOT)

      ```dot
      n11 [label=<
          <VB>Bottom-Aligned Text</VB>
          <FONT POINT-SIZE=40>NEXT</FONT>
      >];
      ```
      
    - Example (Java)

      ```java
      LabelTag label = bottom("Bottom-Aligned Text").font("NEXT", fontAttrs().pointSize(40));
      Node.builder().labelTag(label).build();
      ```

12. **\<VC\>**

    - Vertically centers the content, ensuring balanced alignment between text elements of different heights.

    - Example (DOT)

      ```dot
      n12 [label=<
          <VC>Vertically Centered Text</VC>
          <FONT POINT-SIZE=40>NEXT</FONT>
      >];
      ```
      
    - Example (Java)

      ```java
      LabelTag label = verticalCenter("Vertically Centered Text").font("NEXT", fontAttrs().pointSize(40));
      Node.builder().labelTag(label).build();
      ```

13. **\<HL\>**

    - Horizontally aligns the text to the left, ensuring that rows with different widths start at the same position.

    - Example (DOT)

      ```dot
      n13 [label=<
          <HL>Left-Aligned Text</HL><BR/>
          The second row has a longer length.
      >];
      ```
      
    - Example (Java)

      ```java
      LabelTag label = left("Left-Aligned Text")
              .br()
              .text("The second row has a longer length.");
      Node.builder().labelTag(label).build();
      ```

14. **\<HR\>**

    - Horizontally aligns the text to the right, ensuring that rows with different widths are aligned to the right edge.

    - Example (DOT)

      ```dot
      n14 [label=<
          <HR>Right-Aligned Text</HR><BR/>
          The second row has a longer length.
      >];
      ```
      
    - Example (Java)

      ```java
      LabelTag label = right("Right-Aligned Text")
              .br()
              .text("The second row has a longer length.");
      Node.builder().labelTag(label).build();
      ```

15. **\<HC\>**

    - Horizontally centers the content, making sure rows of text are centered within the label.

    - Example (DOT)

      ```dot
      n15 [label=<
          <HC>Horizontally Centered Text</HC><BR/>
          The second row has a longer length.
      >];
      ```
      
    - Example (Java)

      ```java
      LabelTag label = horizontalCenter("Horizontally Centered Text")
              .br()
              .text("The second row has a longer length.");
      Node.builder().labelTag(label).build();
      ```

------

### **Comprehensive DOT Example**

```dot
digraph {
    n1 [label=< 
        Line 1<BR/>Line 2<BR/>Line 3<BR/>
        <FONT POINT-SIZE="16" COLOR="blue" FACE="Arial">Styled Text</FONT><BR/>
        <B>Bold</B> <I>Italic</I> <U>Underline</U><BR/>
        H<SUB>2</SUB>O + CO<SUB>2</SUB><BR/>
        <S><FONT COLOR="red">Strikethrough and Colored</FONT></S><BR/>
        <HR><FONT COLOR="green">Right Aligned</FONT></HR><BR/>
        <HL>Left Aligned</HL><BR/>
        <HC>Centered Text</HC><BR/>
        <O>Overlined Text</O><BR/>
        <FONT POINT-SIZE="12" COLOR="purple" FACE="Impact">Impact Font</FONT><BR/>
        <B><I><U><S>Complex Formatting</S></U></I></B><BR/>
        <SUP>Superscript Text</SUP> and <SUB>Subscript Text</SUB><BR/>
        <VT>Top-Aligned</VT>
        <FONT POINT-SIZE=40>NEXT</FONT><BR/>
        <VB>Bottom-Aligned Text</VB>
        <FONT POINT-SIZE=40>NEXT</FONT><BR/>
        <VC>Vertically Centered Text</VC>
        <FONT POINT-SIZE=40>NEXT</FONT><BR/>
    >];
}
```

------

### **Comprehensive Java Example**

```java
 LabelTag label = text("Line 1").br()
        .text("Line 2").br()
        .text("Line 3").br()
        .font("Styled Text", fontAttrs().pointSize(16).color(Color.BLUE).face("Arial")).br()
        .bold("Bold ").italic("Italic ").underline("Underline").br()
        .text("H").subscript("2").text("O + CO").subscript("2").br()
        .strikeThrough(font("Strikethrough and Colored", fontAttrs().color(Color.RED)))
        .br()
        .right(font("Right Aligned", fontAttrs().color(Color.GREEN))).br()
        .left("Left Aligned").br()
        .horizontalCenter("Centered Text").br()
        .overline("Overlined Text").br()
        .font("Impact Font", fontAttrs().pointSize(12).color(Color.PURPLE).face("Impact")).br()
        .bold(italic(underline(strikeThrough("Complex Formatting")))).br()  // Correct order for Complex Formatting
        .superscript("Superscript Text").text(" and ").subscript("Subscript Text").br()
        .top("Top-Aligned").font("NEXT", fontAttrs().pointSize(40)).br()
        .bottom("Bottom-Aligned Text").font("NEXT", fontAttrs().pointSize(40)).br()
        .verticalCenter("Vertically Centered Text").font("NEXT", fontAttrs().pointSize(40));

Node.builder().labelTag(label).build();
```