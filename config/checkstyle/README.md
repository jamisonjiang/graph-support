# Static Checks

Run these commands from the repository root (optionally select modules with `-pl`):

```sh
JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Djava.awt.headless=true" mvn --fail-at-end validate
JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Djava.awt.headless=true" mvn --fail-at-end checkstyle:check@static-checks
JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Djava.awt.headless=true" mvn --fail-at-end checkstyle:check@google-style
```

Both inherited executions run during `validate`, so normal `mvn install` also
fails on violations, even when tests are skipped. Plain `checkstyle:check` uses
the custom plugin configuration, not the `google-style` execution. Maven may skip
dependent modules after a failure, even with `--fail-at-end`; use `-pl` to inspect
those modules separately.

The build pins `org.apache.maven.plugins:maven-checkstyle-plugin:3.3.1` and
`com.puppycrawl.tools:checkstyle:9.3`, both compatible with Java 8. These are
build-plugin dependencies only; no runtime dependency or ArchUnit is added.

## Scope

- Production Java in each module's configured source directory is checked.
- Tests, resources, and generated build source roots are not checked.
- Vendored `org/apache_gs` is excluded. The twelve checked-in ANTLR Java files
  are suppressed by exact module-relative paths, not their entire directory.
- `static-checks` retains `AvoidStarImport`, `RedundantImport`, `UnusedImports`
  (including Javadoc usage), and the core boundary below.
- `google-style` uses the local `google-style.xml`, a focused subset of the
  engine's bundled `google_checks.xml` from Checkstyle 9.3. Selected check
  properties, tokens, messages, and unspecified engine defaults are retained.
  Its severity is `warning`, so Maven's
  `violationSeverity` is explicitly `warning` to fail on warnings and errors.
- Both executions print violations and use the existing `suppressions.xml`.
  Both configs require `checkstyle.suppressions.file`, wired through Maven's
  `suppressionsFileExpression` (the plugin default for `static-checks`, explicit
  for `google-style`). Generated-file exceptions apply to both executions.

Only these Google-style categories are enabled:

- Tabs: `FileTabCharacter`, reporting each line.
- Indentation: `Indentation` and `CommentsIndentation`; two-space basic, brace,
  case, and array indentation, four-space throws and continuation indentation.
- Whitespace: `WhitespaceAfter`, `WhitespaceAround`, `EmptyLineSeparator`,
  `GenericWhitespace`, `NoWhitespaceBeforeCaseDefaultColon`, `MethodParamPad`,
  `NoWhitespaceBefore`, and `ParenPad`.
- Wrapping: `NoLineWrap` for package/import declarations, five `SeparatorWrap`
  instances (dot, comma, ellipsis, array declarator, method reference), and
  `OperatorWrap`.
- Line length: `LineLength`, 100 columns, with Google's package/import and URL
  ignore pattern.
- Import ordering: `CustomImportOrder`, alphabetized static imports first,
  followed by a blank line and alphabetized non-static imports. Wildcard,
  redundant, and unused imports remain covered by `static-checks`.
- Overloaded method grouping: `OverloadMethodsDeclarationOrder`.
- Javadoc: all eleven bundled checks, `NonEmptyAtclauseDescription`,
  `InvalidJavadocPosition`, `JavadocTagContinuationIndentation`, `SummaryJavadoc`,
  `JavadocParagraph`, `RequireEmptyLineBeforeBlockTagGroup`, `AtclauseOrder`,
  `JavadocMethod`, `MissingJavadocMethod`, `MissingJavadocType`, and
  `SingleLineJavadoc`. Google 9.3's scopes and exemptions are unchanged, including
  public method checks, protected-or-public type documentation, and the configured
  `Override`/`Test` method exemptions.

This is not the full Google coding ruleset. Naming, filename/type matching,
variable usage distance, multiple-variable declarations, empty blocks/catches,
Unicode/escape conventions, brace placement/requirements, annotation placement,
statement counts, modifiers, and other coding rules are not enabled. No blanket
naming suppression is needed because naming modules are absent.

Results and caches are separate under each module's `target`: `checkstyle-result.xml`
and `checkstyle-cachefile` for static checks; `checkstyle-google-result.xml` and
`checkstyle-google-cachefile` for Google checks. Existing sources have Google-style
violations, so validation is expected to fail until they are addressed. Checking
does not reformat or automatically apply changes.

## IntelliJ Formatting

IntelliJ's GoogleStyle formatter scheme targets the broader Google standard,
not precisely this subset, Checkstyle's Google rules, or google-java-format.
Align a custom scheme with GoogleStyle (two-space indentation, 100-column margin,
Google import ordering, no wildcard imports), then use Maven as the checking
authority. Formatting alone cannot fix every Javadoc or overload-grouping violation.
No IDE scheme or automatic formatting is applied by this build; the existing IDE
Checkstyle configuration is separate from Maven's pinned engine and focused rules.

## Core Boundary

Only core enables `core-platform-dependency`. It rejects source references to
`java.awt`, `javax.swing`, `javax.imageio`, `javax.print`, `javax.sound`,
`javax.net.ssl`, `java.nio.file`, `java.util.Base64`,
`javafx`, `android`, `org.apache.batik`, and `org.apache.fop` packages.
AST matching covers imports, static imports, and fully qualified references,
including variable types. Comments and string literals are intentionally
ignored so optional reflective loading remains possible.

The exact-path adapter allowlist is in `suppressions.xml`: `AWTMeasureText`,
`AWTextRender`, `DefaultImgConverter`, `AndroidImgConverter`,
`BatikImgConverter`, and its SVG/image validation helper `SecureSvg`.
These exceptions suppress only the boundary rule, never style checks.
UI and other modules retain the style checks without the core boundary.

This is a syntactic source guard, not bytecode or transitive dependency analysis.
`TlsImageLoader`, `LocalImageLoader`, and `NioGraphResourceSaver` are also explicit
adapter exceptions. The rule restricts `java.nio.file`, not all of `java.nio`.
It cannot resolve names: an expression using variables named like a forbidden
package prefix can also match. Reflection strings are not policed, and references
through project wrapper types are not traced. Adding a platform adapter requires
reviewing its exact-path exception rather than excluding a package or directory.
