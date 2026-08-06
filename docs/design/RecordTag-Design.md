# RecordTag — Structured API for Record Labels

> Status: **Implemented.** Sections below marked *As built* record where the delivered design
> departs from this draft, and why.
> Author: (discussion between Jamison & assistant)
> Scope: `core/` API surface, `dot/` parser, SVG rendering.

## 0. As built — summary of departures from the draft

| Draft said | As built | Why |
|---|---|---|
| `CellLabelCompiler` stays **unchanged**; `RecordTagCompiler` is a parallel compiler (§1.3, §3.5) | `CellLabelCompiler` became a pure `String → RecordTag` **parser**; `RecordTagCompiler` is the **single** geometry backend both front-ends feed | Two parallel geometry implementations would have to agree forever. The draft's own test plan (§8, "structural equivalence") was really a request for a regression net around that duplication. One backend removes the problem instead of guarding it. |
| `recordTag` slot on `NodeAttrs`, `LineAttrs`, `ClusterAttrs`, `GraphAttrs`, `FloatLabel` (§3.3) | `NodeAttrs` only | Record compilation happens exclusively in `AbstractLayoutEngine.nodeContainerSet`. The other four slots were public API that nothing would ever read. `RecordTagApiTest#testRecordTagIsNodeOnly` locks this down. |
| `recordRichTextSet` "reuses `HtmlConvertor.accessLabelTag`" (§4.2) | `HtmlConvertor.toPositionedTexts` was extracted so the **layout pass is shared** and only the consumer differs | `accessLabelTag` produced `Node` objects for an `Assemble`, not positions. "Reuse" without that split would have meant copying the positioning maths — the same duplication trap as the geometry backend, one layer up. |
| Grammar's string parsing is DOT-specific and should live in `dot/` (§1.3 goal 4) | The string parser stays in `core/`, frozen | `label("<p1>1\|2")` on a plain `Node.builder()` is documented public API of the standalone `graph-support-core` artifact, and `dot` depends on `core` (never the reverse). Moving it would break core-only users. The responsibility line that mattered was *parse tree vs geometry*, not *module*. |
| Port ids in angle-bracket labels: "not present" (§5.2 item 4) | Confirmed: a **hard parse error**, not a silent drop | `HTMLParser.g4`'s `htmlElement` accepts only a whitelist of tags, so `<f0>` fails as an unknown tag. Matches Graphviz. Ports plus rich text are Java-API only. |

Two bugs were found and fixed by the render checks rather than by reasoning:

1. `PositionedText` scaled sizes by `PIXEL` a second time — `NodeBuilder#width/#height` already scale on
   the way in, so rich runs landed roughly 6× too far right.
2. Glyphs were centred using the font size as their height. The measured height of a line is slightly
   larger, so every rich run sat about a pixel above the plain path. Distinguishing normal runs from
   sub/superscript needed an explicit flag (`PositionedText#isScriptShift`), because both are tagged
   `Labelloc.TOP`.

---

## 1. Motivation

### 1.1 The current architecture smell

The `core/` module is supposed to be a **pure Java API layer** — users construct a `Graphviz` instance by calling typed builder methods, no string parsing required.

The one exception is `CellLabelCompiler`
(`core/src/main/java/org/graphper/layout/CellLabelCompiler.java`):

* It lives in `core/layout/`.
* It parses a DOT-flavoured string grammar (`"{a|b|<p>c}"`) into a `RootCell` tree.
* It is the **only** way to build a record node today — users have no
  structured API, they must hand-assemble the string even from Java code.

So `core/` has a string grammar baked into it, which conflicts with the
pure-API direction the rest of the module follows (c.f. `Html.table()`,
`Html.labelTag()`, `Html.bold()` for HTML labels).

### 1.2 The missing feature

Graphviz supports mixing **record structure** with **inline HTML
formatting** when a record node uses an angle-bracket label:

```dot
n [shape=record, label=<{<i>a</i>|<u>b</u>|<font color="red">c</font>}>];
```

Real `dot` (v12.2.1) renders this as three record cells with italic `a`,
underlined `b`, and red `c`. (Verified with local `dot -Tsvg`.)

`graph-support` parses the `<...>` payload as an HTML label and therefore
treats `{` / `|` as literal text (they fall into the lexer's `HTML_TEXT`
catch-all). The record structure is lost, leaving a single inline run of
rich text with visible `{` / `|` characters. This is a **silent mis-render**
— no error, no warning.

### 1.3 Goals

1. Give users a **structured Java API** to build record labels —
   parallel in style to `Html.table()` / `Html.labelTag()`.
2. Support **record + inline HTML formatting** (the Graphviz gap above).
3. **Do not break** existing string-based record labels
   (`label="{a|b|c}"`). `CellLabelCompiler` stays in place unchanged.
4. Keep the DOT-specific record grammar parsing in the `dot/` module,
   not in `core/`.

### 1.4 Non-goals

* Deprecating or removing `CellLabelCompiler`.
* Unifying HTML and record grammars in `HTMLParser.g4` (we'll keep them
  separate — see §5).
* Rewriting `Cell` or the SVG record-draw path wholesale.
* Supporting HTML tags that don't translate to per-fragment styling
  (e.g. `<TABLE>` inside a record cell is out of scope).

---

## 2. Design Overview

The design is a direct analogue of the `LabelTag` design. Three parallel
"label kinds" on every attrs class:

```
NodeAttrs
  ├─ String   label       ← plain text (already exists)
  ├─ LabelTag labelTag    ← HTML inline tags (already exists)
  ├─ Table    table       ← HTML table (already exists)
  └─ RecordTag recordTag  ← NEW: record structure + optional inline HTML
```

User API added on `Html` class (still one entry class, shared namespace
with existing HTML factories):

```java
Html.record(cell("a"), cell("b").id("f0"), vertical(cell("c"), cell("d")))
Html.cell("a")
Html.cell(Html.italic("a"))                 // rich cell text
Html.cell().id("f0").text(Html.bold("x"))
Html.verticalCell(...)                      // {} nesting
Html.horizontalCell(...)
```

DOT parser (`dot/`) learns to translate both:

* `label="{<f0>a|b}"`   → string passed to `label` slot (as today)
* `label=<{<i>a</i>|b}>` → `RecordTag` AST passed to `recordTag` slot (new)

Layout (`core/layout/`) dispatches on presence of `recordTag` first,
falls back to the existing `CellLabelCompiler` string path.

---

## 3. `core/` API Changes

### 3.1 New: `Html.RecordTag`

File: `core/src/main/java/org/graphper/api/Html.java` (extend existing).

Mirrors `LabelTag`'s design: a root holding a flat list of siblings.
Each sibling is a `BasicRecordCell` describing one record cell with:

* an optional port ID (`<f0>`)
* cell body — **either** a plain `String`, **or** a `LabelTag` (rich text),
  **or** a nested `RecordTag` (for `{...}` nesting / orientation flip)
* orientation of its children (only for nested `RecordTag`)

```java
public static class RecordTag implements Serializable {

    /** Horizontal: children laid out left→right; Vertical: top→bottom.
     *  Mirrors the top-level record semantics: top-level defaults to
     *  horizontal, each nesting flips. */
    private boolean horizontal;

    private List<BasicRecordCell> cells;   // flat sibling list

    // package-private; created via Html.record(...) / Html.verticalCell(...)
    RecordTag(boolean horizontal) { this.horizontal = horizontal; }

    public boolean isHorizontal() { return horizontal; }
    public List<BasicRecordCell> getCells() { ... }

    // Chainable add methods, parallel to LabelTag.bold(...)/italic(...):
    public RecordTag cell(String text);
    public RecordTag cell(BasicRecordCell cell);
    public RecordTag cell(LabelTag richText);
    public RecordTag nested(RecordTag inner);          // adds a {…} child
}

public static class BasicRecordCell implements Serializable {
    private String id;              // <port> identifier, optional
    private String text;            // leaf plain text  (exactly one of
    private LabelTag textTag;       // text / textTag / nested is non-null)
    private RecordTag nested;

    // Fluent setters (identity-return chaining):
    public BasicRecordCell id(String portId);
    public BasicRecordCell text(String t);
    public BasicRecordCell text(LabelTag tag);
    // nested is populated by RecordTag.nested(...) not directly here
}
```

Invariant enforced in `verify()`:
exactly one of `{text, textTag, nested}` is non-null.

### 3.2 New static factory methods on `Html`

All return freshly-built immutable-ish builders, same style as existing
`table()`, `labelTag()`, `bold(...)`:

```java
public static RecordTag record(BasicRecordCell... cells);           // horizontal root
public static RecordTag verticalRecord(BasicRecordCell... cells);   // vertical root

public static BasicRecordCell cell();                               // empty, for fluent
public static BasicRecordCell cell(String text);
public static BasicRecordCell cell(LabelTag richText);
public static BasicRecordCell cell(RecordTag nested);

// Sugar for the (common) orientation-flip nested cell:
public static BasicRecordCell vertical(BasicRecordCell... cells);   // {…} inside horizontal parent
public static BasicRecordCell horizontal(BasicRecordCell... cells); // {…} inside vertical parent
```

Example:

```java
// Equivalent to label="{<f0>a|{b|c}|<f1>d}" (string form)
Html.record(
    Html.cell("a").id("f0"),
    Html.vertical(
        Html.cell("b"),
        Html.cell("c")
    ),
    Html.cell("d").id("f1")
);

// Rich-text example — impossible today, Graphviz-compatible:
Html.record(
    Html.cell(Html.italic("a")),
    Html.cell(Html.underline("b")),
    Html.cell(Html.font("c", Html.fontAttrs().color(Color.RED)))
);
```

### 3.3 New slot on attrs classes

Add to every attrs class alongside `label` / `labelTag` / `table`:

| File | New field |
|---|---|
| `core/api/NodeAttrs.java` | `RecordTag recordTag;` |
| `core/api/LineAttrs.java` | `RecordTag recordTag;` |
| `core/api/ClusterAttrs.java` | `RecordTag recordTag;` |
| `core/api/GraphAttrs.java` | `RecordTag recordTag;` |
| `core/api/FloatLabel.java` | `RecordTag recordTag;` |

Corresponding builder setters:

```java
// Node.java — mirrors labelTag(LabelTag):
public NodeBuilder recordTag(RecordTag recordTag) {
    nodeAttrs.recordTag = recordTag;
    return this;
}
```

Identical pattern on `Line.java`, `Graphviz.java`, `FloatLabel.java`,
`ClusterAttrs` builder. No mutual-exclusion assertion (follows existing
`label` / `labelTag` / `table` precedent — last-write-wins, consumer
picks precedence).

### 3.4 Precedence rules at consumption time

When a node has `shape=record` or `shape=Mrecord`:

```
recordTag   (new, wins first)
  > labelTag + string-mimicking {…|…} text in label     (legacy fallback)
  > label (plain string, passed to CellLabelCompiler)   (legacy)
```

When `shape` is *not* record, `recordTag` is **ignored** (same way
`labelTag` has no effect on `shape=plaintext` without a `<TABLE>`).
Optionally we log a warning once.

For non-record shapes, the existing `table` / `labelTag` / `label`
precedence is untouched.

### 3.5 New: `RecordTagCompiler`

File: `core/src/main/java/org/graphper/layout/RecordTagCompiler.java`

Counterpart of `CellLabelCompiler`. Input is already an AST, so it
skips the tokenizer/parser phases and goes straight to `Cell` geometry:

```java
public class RecordTagCompiler {

    public static RootCell compile(
            RecordTag recordTag,
            String fontName,
            double fontSize,
            FlatPoint margin,
            FlatPoint minCellSize,
            boolean defaultVer);
    // Mirrors CellLabelCompiler.compile(...) signature so the dispatch
    // in AbstractLayoutEngine only differs in the input type.
}
```

Internally:

1. Walk the `RecordTag` tree, produce a `RootCell` whose structure
   matches the existing `CellLabelCompiler` output (so SVG rendering
   doesn't need to change for plain-text cells).
2. For a cell whose body is a `LabelTag` (rich text): either
   (a) pre-measure with `LabelTagUtils.measure(labelTag, attrs)`,
       store the `LabelTag` on a new `Cell.labelTag` field;
   (b) extend SVG renderer to consult it — see §4.

### 3.6 `Cell` gains one optional rich-text field

File: `core/src/main/java/org/graphper/layout/Cell.java`

```java
// New, optional. When non-null, renderer should draw the cell as rich
// text instead of plain cell.label.
protected LabelTag labelTag;

public LabelTag getLabelTag() { return labelTag; }
void setLabelTag(LabelTag labelTag) { this.labelTag = labelTag; }
```

`CellLabelCompiler` never sets this field (stays fully backward-compatible).
Only `RecordTagCompiler` populates it.

---

## 4. SVG Rendering Changes

### 4.1 Current state

`NodeShapeEditor.recordTextSet(...)`
(`core/draw/svg/node/NodeShapeEditor.java:376-391`) draws one
`<text>` element per cell, using only node-level font attributes. It
does not support per-cell bold/italic/color.

`NodeLabelEditor.edit(...)` already bails out when `nodeDrawProp.getCell() != null`
(line 48 area), leaving record drawing entirely to `NodeShapeEditor`.

### 4.2 Proposed change

In `NodeShapeEditor.recordTextSet`, before falling through to the
plain-text path, check:

```java
if (cell.getLabelTag() != null) {
    // Delegate to a mini HtmlConvertor-style path: lower the LabelTag
    // into a set of positioned <text> spans inside the cell's box,
    // using TextTagValue to carry bold/italic/color per fragment.
    recordRichTextSet(nodeDrawProp, brush, cell, labelCenter);
    return;
}
// existing plain-text draw:
```

`recordRichTextSet` is a small new helper that reuses
`HtmlConvertor.accessLabelTag` / `textTagToCell` logic but writes
directly into the `SvgBrush` element tree (instead of building an
`Assemble` of sub-nodes). The output is standard SVG: one `<text>`
element per styled fragment with `font-weight`/`font-style`/
`text-decoration`/`fill` attributes, exactly like `NodeLabelEditor`'s
`setFontStyle`.

Alternative considered — **lower each rich cell to an `Assemble`** of
sub-nodes (same as HTML labels do): rejected because it would break
`RootCell.getCellById(...)` port lookups for edges (`tailCell`/`headCell`
DOT attributes), and it complicates hit-testing.

### 4.3 Measurement path

`RecordTagCompiler.setCellSize(Cell c)` — the rich-cell branch:

```java
if (c.labelTag != null) {
    LabelAttributes attrs = new LabelAttributes();
    attrs.setFontName(fontName);
    attrs.setFontSize(fontSize);
    size = LabelTagUtils.measure(c.labelTag, attrs);
} else {
    size = FontUtils.measure(c.getLabel(), fontName, fontSize, 0);
}
```

Reuse `LabelTagUtils.measure` — it already exists and is exactly what
`HtmlConvertor` uses for the same purpose.

---

## 5. `dot/` Parser Changes

### 5.1 Path A: `label="{...}"` — string (UNCHANGED)

Keep existing behaviour:
`ParserUtils.labelHandle` → plain string → `NodeAttrs.label` →
`CellLabelCompiler.compile` at layout time. No change.

### 5.2 Path B: `label=<{<i>a</i>|<u>b</u>|}>` — new

This is where the work happens. The approach chosen (one of three
discussed): **`HtmlListener` post-processing** — grammar stays untouched.

**Reasoning**: modifying `HTMLLexer.g4` / `HTMLParser.g4` to make `{`
and `|` structural tokens only inside record shapes is intrusive (the
lexer doesn't know about `shape` at tokenization time, so it'd have to
introduce a sub-mode toggle or conditionally re-interpret tokens in the
parser). Post-processing operates on the already-built `LabelTag` tree
and is localized to the DOT module.

#### Flow

1. `DOTLexer` tokenizes `<...>` as `HTML_STRING` (today's behaviour).
2. `HtmlParser.parse(...)` + `HtmlListener` build a `LabelTag` tree
   where the inline tags (`<i>`, `<u>`, `<font>`, `<b>`, `<br/>`, ...)
   are proper `BasicLabelTag` children and the `{` / `|` characters
   sit inside `BasicLabelTag` text nodes of type `TEXT`.
3. **New step** in `ParserUtils.labelHandle`: if
   `nodeBuilder` has `shape ∈ {record, Mrecord}` **and**
   `htmlListener.getLabelTag() != null`, run
   `RecordTagFromLabelTag.convert(labelTag)` →
   optional `RecordTag`. Put the result into `recordTag` slot (not
   `labelTag`).
4. If conversion fails (malformed record structure), fall back to
   routing to `labelTag` as today plus a warning log.

#### The conversion algorithm

`RecordTagFromLabelTag` (new class in `dot/parser/`):

```
input:  LabelTag tree, where text leaves may contain '{' / '|' / '}' characters
output: RecordTag AST, where '{' / '|' are consumed as structure and the
        surviving text is split into per-cell LabelTag slices
```

Algorithm:

1. **Linearize** the `LabelTag`'s flat `getTags()` list into an ordered
   stream of atoms:
   * `OPEN`       from `{` characters in a TEXT leaf
   * `CLOSE`      from `}` characters in a TEXT leaf
   * `SPLIT`      from `|` characters in a TEXT leaf
   * `TEXT(s)`    from the surviving text fragments of a TEXT leaf
   * `RICH(tag)`  for any non-TEXT `BasicLabelTag` (the whole sub-tree
                  becomes one opaque atom — an `<i>` or `<font>` span)
   Respect backslash escapes `\{` `\|` `\}` `\<` `\>` the same way
   `CellLabelCompiler.tokenizer` does.
2. Group adjacent `TEXT(s)` and `RICH(tag)` atoms between two
   `SPLIT` / `OPEN` / `CLOSE` boundaries into **one cell body**. If
   the group contains only TEXT atoms and is a single string, use
   `BasicRecordCell.text(String)`; otherwise wrap them in a new
   `LabelTag` and use `BasicRecordCell.text(LabelTag)`.
3. Handle `OPEN` / `CLOSE` by pushing / popping a `RecordTag` frame
   onto a stack, alternating orientation at each level. Top-level is
   horizontal, same as `CellLabelCompiler.compile(label, false)`.
4. Port IDs `<f0>` are **not** present — an HTML-mode label has
   `<i>`, `<u>`, etc. as real tags, not as port IDs. (This matches
   Graphviz: in angle-bracket labels you lose `<port>` syntax.)

   If the user really wants port IDs with HTML formatting, they must
   combine via the Java API: `Html.cell(Html.italic("a")).id("f0")`.

#### Where the code lives

| Concern | File |
|---|---|
| Trigger the conversion | `dot/src/main/java/org/graphper/parser/ParserUtils.java` (edit `labelHandle`) |
| LabelTag → RecordTag converter | `dot/src/main/java/org/graphper/parser/RecordTagFromLabelTag.java` (new) |
| Unit tests | `dot/src/test/java/org/graphper/parser/RecordTagFromLabelTagTest.java` (new) |

### 5.3 `RecordTag` is *not* exposed as a new DOT grammar

Users cannot write a syntax like `label=record{…}` in DOT. The new
slot is reachable only from two routes:
* Java API (new, §3).
* DOT parser post-processing of the angle-bracket HTML label case (§5.2).

---

## 6. Layout Dispatch

Current (`AbstractLayoutEngine.nodeContainerSet` :649-654):

```java
if (nodeDrawProp.noChildrenCell() && isRecordShape(nodeShape)) {
    RootCell rootCell = CellLabelCompiler.compile(
        nodeAttrs.getLabel(), ...);
    nodeDrawProp.setCell(rootCell);
}
```

Proposed:

```java
if (nodeDrawProp.noChildrenCell() && isRecordShape(nodeShape)) {
    RootCell rootCell;
    if (nodeAttrs.getRecordTag() != null) {
        rootCell = RecordTagCompiler.compile(
            nodeAttrs.getRecordTag(), nodeAttrs.getFontName(),
            getFontSize(nodeAttrs), nodeAttrs.getMargin(),
            new FlatPoint(height, width), needFlip);
    } else {
        rootCell = CellLabelCompiler.compile(
            nodeAttrs.getLabel(), nodeAttrs.getFontName(),
            getFontSize(nodeAttrs), nodeAttrs.getMargin(),
            new FlatPoint(height, width), needFlip);
    }
    labelBox = new FlatPoint(rootCell.getHeight(), rootCell.getWidth());
    nodeDrawProp.setCell(rootCell);
}
```

Identical for line/cluster/graph dispatch, wherever `CellLabelCompiler`
is called (grep shows it's only called from `AbstractLayoutEngine.java:650`,
so there's one site to edit).

---

## 7. Backward Compatibility

* **String record labels (`label="{a|b}"`)**: unaffected. Same parse
  path, same `CellLabelCompiler`, same `RootCell`, same SVG output.
  Graphviz line terminators `\n`, `\l`, and `\r` are decoded by the DOT lexer into alignment-aware
  line markers. Measurement normalizes those markers to newlines; SVG rendering aligns each line
  against its record-cell width. This is a compatibility extension to text content, not a new
  structural record grammar feature.
* **HTML labels without record structure (`label=<<b>x</b>>`)**: unaffected.
  `ParserUtils.labelHandle` only attempts the RecordTag conversion when
  `shape ∈ {record, Mrecord}` *and* the label parsed into a
  `LabelTag`. Non-record shapes skip the new path entirely.
* **Existing `LabelTag` / `Table` users**: unaffected. New `recordTag`
  slot is independent.
* **Serialization**: all new classes implement `Serializable` and
  carry explicit `serialVersionUID` values, following the project
  convention (e.g. `Html.java` classes).
* **Existing `CellLabelCompilerTest`**: must continue to pass
  unchanged. New `RecordTagCompilerTest` added.

---

## 8. Testing Plan — as built

Ordering mattered more than coverage. The geometry golden file was generated from the
**pre-refactor** implementation and committed before any production code moved, so the refactor's
review question is simply "did the golden file change?".

| Test | Location | Purpose |
|---|---|---|
| `RecordGeometryGoldenTest` | `core/src/test` | Full `RootCell` geometry (path / id / isHor / w / h / offset) for the whole corpus × 5 parameter combinations, against a golden file generated before the refactor. This is the real regression net: the pre-existing `CellLabelCompilerTest` asserts only tree depth, leaf count and port lookup, and so does not constrain any of the geometry that moved. |
| `RecordLabelCorpus` | `core/src/test` | The ~44 valid + 18 rejected label strings, shared by the golden and round-trip tests. |
| `RecordTagRoundTripTest` | `core/src/test` | `parse → serialise → parse` over the corpus, asserting both structure and compiled geometry. Proves `RecordTag` can express the entire frozen grammar, which is the precondition for `CellLabelCompiler` being demotable to a parser. Replaces the draft's hand-written per-string equivalents. |
| `RecordRenderGeometryTest` | `core/src/test` | Renders svg and asserts invariants: no text outside the node outline, no two runs at one point, declaration order preserved left-to-right, vertical records stacking downwards, styles reaching the svg, and rich runs sharing the plain path's exact baseline. |
| `RecordTagApiTest` | `core/src/test` | API surface, including that only `Node` exposes a `recordTag` slot. |
| `RecordTagFromLabelTagTest` | `dot/src/test` | Conversion unit cases plus end-to-end `DotParser` cases, including shape-declared-after-label, non-record shapes keeping braces literal, and quoted labels staying on the string path. |
| `CellLabelCompilerTest` | unchanged | Kept as-is; passes untouched. |

`DeterministicMeasureText` (a test-scoped `MeasureText` SPI with `order() == -1`) is what makes the
golden file possible: `AWTMeasureText` normally wins and its numbers depend on the host's installed
fonts and JDK. Visual checks were rendered *without* it, against real AWT metrics.

## 9. Implementation Phases — as built

All phases landed. The draft's phase list was reordered so the regression baseline exists before the
refactor:

1. Deterministic measurement SPI.
2. Geometry golden baseline generated from unmodified code.
3. `CellLabelCompiler` → parser; geometry extracted into `RecordTagCompiler`; golden file
   byte-identical.
4. `recordTag` API narrowed to `NodeAttrs`.
5. Layout dispatch: `recordTag` if present, otherwise parse the label string; both into
   `RecordTagCompiler`.
6. `HtmlConvertor.toPositionedTexts` extracted; rich record cells rendered; the pre-existing bug where
   node-level `fontStyle` was ignored on record shapes fixed at the same time (`SvgEditor.setFontStyle`
   is now shared by both text paths).
7. `dot/` conversion (`RecordTagFromLabelTag`), grammar files untouched.
8. Docs: `docs/node/RecordTag.md`, freeze notice on `docs/node/Label.md`.

## 10. Open Questions — resolved

1. **Name** `RecordTag` vs `Record` vs `RecordLabel` → `RecordTag`, for symmetry with `LabelTag`.
2. **Port id setter name** → `id(String)`, as drafted.
3. **`recordTag` on non-record shapes** → ignored, no warning. The layout dispatch only consults it
   inside the `isRecordShape` branch, so a non-record shape never reads it.
4. **Do backslash escapes survive ANTLR?** → **Yes.** Verified: `<a\|b>` reaches the listener as the
   literal text `a\|b`, so `RecordTagFromLabelTag` honours record escapes itself. Covered by
   `RecordTagFromLabelTagTest#escapedRecordCharactersStayLiteral`.
5. **Font defaults in rich cells** → node-level `fontName`/`fontSize`/`fontColor`/`fontStyle` seed the
   `LabelAttributes`, inner tags refine them. Same rule as `HtmlConvertor.setTextValue`, because it is
   literally the same code path.
6. **`headCell`/`tailCell` for rich cells** → works. `RecordTagCompiler` registers the port id on the
   `Cell` regardless of body kind, so `RootCell.getCellById` resolves. Verified end to end by
   rendering an edge with `tailCell` into a record whose middle cell is bold.
7. **Non-record shapes with `<{…|…}>`** → unchanged, braces stay literal text. Covered by
   `RecordTagFromLabelTagTest#nonRecordShapeKeepsBracesAsText`.

## 11. Files Touched — as built

### New — production
```
core/src/main/java/org/graphper/layout/RecordTagCompiler.java      single geometry backend
dot/src/main/java/org/graphper/parser/RecordTagFromLabelTag.java   LabelTag -> RecordTag
docs/node/RecordTag.md
```

### New — tests
```
core/src/test/java/org/graphper/layout/RecordLabelCorpus.java
core/src/test/java/org/graphper/layout/RecordGeometryGoldenTest.java
core/src/test/java/org/graphper/layout/RecordTagRoundTripTest.java
core/src/test/java/org/graphper/layout/RecordRenderGeometryTest.java
core/src/test/java/org/graphper/layout/DeterministicMeasureText.java
core/src/test/resources/META-INF/services/org.graphper.layout.MeasureText
core/src/test/resources/record-geometry-golden.txt
dot/src/test/java/org/graphper/parser/RecordTagFromLabelTagTest.java
```

### Modified
```
core/api/Html.java                     RecordTag + BasicRecordCell + factories; LabelTag#add
core/api/NodeAttrs.java, Node.java     recordTag slot + builder method
core/layout/CellLabelCompiler.java     now String -> RecordTag only; geometry removed;
                                       LabelAstNode demoted to a parser-internal parse tree
core/layout/RecordTagCompiler.java     (new) owns setCellSize/postSizeHandle/alignMinSize/addChild
core/layout/Cell.java                  + labelTag field and accessors
core/layout/AbstractLayoutEngine.java  dispatch: recordTag, else parse(label); one backend
core/layout/HtmlConvertor.java         + toPositionedTexts, textAlign split behind a consumer
core/draw/svg/SvgEditor.java           + shared setFontStyle
core/draw/svg/node/NodeShapeEditor.java + recordRichTextSet; plain cells now get fontStyle too
core/draw/svg/node/NodeLabelEditor.java uses the shared setFontStyle
dot/parser/ParserUtils.java            labelHandle gains a recordTag route, gated on shape
docs/node/Label.md                     freeze notice on the string grammar
```

### Untouched, as intended
```
core/layout/CellLabelCompiler tokenizer + grammar rules   frozen, no new features
core/test/.../CellLabelCompilerTest.java                  passes unchanged
dot/src/main/antlr4/.../HTMLLexer.g4                      unchanged
dot/src/main/antlr4/.../HTMLParser.g4                     unchanged
dot/src/main/antlr4/.../DOTLexer.g4                       unchanged
dot/src/main/antlr4/.../DOTParser.g4                      unchanged
```

## 12. Summary

A structured `RecordTag` API lets users build record labels from pure
Java — the same way `LabelTag` and `Table` already let them build HTML
labels. The DOT parser learns to translate the previously-broken
`label=<{<i>a</i>|<u>b</u>}>` syntax into a `RecordTag`, finally
closing that feature gap with Graphviz. The existing string-based
`CellLabelCompiler` path is untouched so all current code continues to
work.

The design mirrors existing project conventions (`Html.*` factories,
attrs-slot pattern, `LabelTagUtils.measure`, `RootCell` plumbing)
everywhere it can, to minimize new concepts.
