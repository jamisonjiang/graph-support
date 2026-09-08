/*
 * Copyright 2022 The graph-support project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.graphper.layout.ext;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.graphper.api.FloatLabel;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Tend;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.layout.FloatLabelBatchInvoker;
import org.graphper.layout.LineClip;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Guards the float label extension point of {@link LineClip} from <em>outside</em>
 * {@code org.graphper.layout}.
 *
 * <p>{@code LineClip} is public and abstract, so subclasses live in user code. This test compiles
 * and runs such a subclass from a foreign package, which is the only way to make the compatibility
 * claim real: everything it names has to be public or protected, so if the hook were declared in
 * terms of a package private type again, this class would stop compiling.
 *
 * <p>The three things being pinned down are:
 * <ol>
 *   <li>the released {@code protected void setFloatLabel(LineDrawProp)} signature still exists, so
 *       code compiled against 1.5.3 keeps linking;</li>
 *   <li>a foreign subclass can both call and override it, and an override is what the layout
 *       engine's batch dispatches through;</li>
 *   <li>no public or protected member of {@code LineClip} mentions an inaccessible type.</li>
 * </ol>
 */
public class LineClipExtensionPointTest {

  private static final FlatPoint MARKER = new FlatPoint(-4242, -2424);

  /**
   * The 1.5.3 signature must stay resolvable, byte for byte: a call site compiled against it emits
   * {@code setFloatLabel:(Lorg/graphper/draw/LineDrawProp;)V} and would otherwise fail with
   * {@link NoSuchMethodError}.
   */
  @Test
  public void releasedHookSignatureIsStillDeclaredAndProtected() throws Exception {
    Method hook = LineClip.class.getDeclaredMethod("setFloatLabel", LineDrawProp.class);

    Assertions.assertTrue(Modifier.isProtected(hook.getModifiers()),
                          "setFloatLabel(LineDrawProp) must stay protected, was " + hook);
    Assertions.assertEquals(void.class, hook.getReturnType());
    Assertions.assertFalse(Modifier.isFinal(hook.getModifiers()),
                           "the hook must remain overridable");
  }

  /**
   * A foreign subclass calls the hook through {@code super}, using only public and protected types,
   * and gets the same answer the layout engine produced. A stub that merely accepted the call
   * without placing anything would fail here.
   */
  @Test
  public void foreignSubclassCanCallTheHookAndGetTheEngineResult() throws Exception {
    Node tail = Node.builder().id("ext-call-tail").label("tail").build();
    Node head = Node.builder().id("ext-call-head").label("head").build();
    Line line = endpointLabeled(tail, head, "head label", "tail label");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().rankdir(Rankdir.LR).addLine(line).build());
    LineDrawProp prop = draw.getLineDrawProp(line);
    Map<FloatLabel, FlatPoint> fromEngine = new HashMap<>(prop.getFloatLabelFlatCenters());
    Assertions.assertEquals(2, fromEngine.size(), "the engine did not place both endpoint labels");

    RecordingLineClip clip = new RecordingLineClip(draw, true);
    clip.callHook(prop);

    Assertions.assertEquals(Collections.singletonList(prop), clip.seen);
    Assertions.assertEquals(fromEngine, prop.getFloatLabelFlatCenters(),
                            "calling the restored hook changed the placement");
  }

  /**
   * The engine's batch has to reach the foreign override, otherwise an existing subclass silently
   * stops intercepting. The override writes a marker instead of delegating, so the assertion only
   * holds if the batch dispatched through it and nothing overwrote its work.
   */
  @Test
  public void engineBatchDispatchesThroughTheForeignOverride() throws Exception {
    Node tail = Node.builder().id("ext-batch-tail").label("tail").build();
    Node head = Node.builder().id("ext-batch-head").label("head").build();
    Line first = endpointLabeled(tail, head, "first head", "first tail");
    Line second = endpointLabeled(tail, head, "second head", "second tail");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().rankdir(Rankdir.LR).addLine(first).addLine(second).build());

    SubstitutingLineClip clip = new SubstitutingLineClip(draw);
    FloatLabelBatchInvoker.runBatch(clip, draw, draw.lines());

    Assertions.assertEquals(draw.lines().size(), clip.seen.size(),
                            "the batch skipped the overridable hook for some lines");
    for (LineDrawProp prop : draw.lines()) {
      Assertions.assertTrue(clip.seen.contains(prop), "line was not offered to the override");
      Map<FloatLabel, FlatPoint> centers = prop.getFloatLabelFlatCenters();
      Assertions.assertFalse(centers.isEmpty());
      for (Map.Entry<FloatLabel, FlatPoint> entry : centers.entrySet()) {
        Assertions.assertSame(MARKER, entry.getValue(),
                              "the override did not fully replace the default placement of "
                                  + entry.getKey().getLabel());
      }
    }
  }

  /**
   * Mechanical check for the defect class itself: an inheritable member of a public class must not
   * mention a type that a foreign subclass cannot name, including inside a generic argument.
   */
  @Test
  public void inheritableSurfaceOfLineClipOnlyMentionsAccessibleTypes() {
    List<String> leaks = new ArrayList<>();
    for (Method method : LineClip.class.getDeclaredMethods()) {
      int modifiers = method.getModifiers();
      if (!Modifier.isPublic(modifiers) && !Modifier.isProtected(modifiers)) {
        continue;
      }
      collectLeaks(method.getGenericReturnType(), method, leaks);
      for (Type parameter : method.getGenericParameterTypes()) {
        collectLeaks(parameter, method, leaks);
      }
    }

    Assertions.assertTrue(leaks.isEmpty(),
                          "LineClip exposes inaccessible types to out-of-package subclasses: "
                              + leaks);
  }

  private static void collectLeaks(Type type, Method method, List<String> leaks) {
    if (type instanceof Class<?>) {
      Class<?> raw = (Class<?>) type;
      while (raw.isArray()) {
        raw = raw.getComponentType();
      }
      if (!raw.isPrimitive() && !Modifier.isPublic(raw.getModifiers())) {
        leaks.add(method.getName() + " -> " + raw.getName());
      }
      return;
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterized = (ParameterizedType) type;
      collectLeaks(parameterized.getRawType(), method, leaks);
      for (Type argument : parameterized.getActualTypeArguments()) {
        collectLeaks(argument, method, leaks);
      }
      return;
    }
    if (type instanceof WildcardType) {
      WildcardType wildcard = (WildcardType) type;
      for (Type bound : wildcard.getUpperBounds()) {
        collectLeaks(bound, method, leaks);
      }
      for (Type bound : wildcard.getLowerBounds()) {
        collectLeaks(bound, method, leaks);
      }
    }
  }

  private static Line endpointLabeled(Node tail, Node head, String headText, String tailText) {
    return Line.builder(tail, head)
        .floatLabels(FloatLabel.builder().label(headText).tend(Tend.HEAD).build(),
                     FloatLabel.builder().label(tailText).tend(Tend.TAIL).build())
        .build();
  }

  /**
   * A foreign subclass that delegates to the inherited behaviour. Note what it needs to compile:
   * the protected {@code drawGraph} field and the protected hook, nothing else.
   */
  private static final class RecordingLineClip extends LineClip {

    private final List<LineDrawProp> seen = new ArrayList<>();

    private final boolean delegate;

    private RecordingLineClip(DrawGraph drawGraph, boolean delegate) {
      this.drawGraph = drawGraph;
      this.delegate = delegate;
    }

    private void callHook(LineDrawProp lineDrawProp) {
      setFloatLabel(lineDrawProp);
    }

    @Override
    protected void setFloatLabel(LineDrawProp lineDrawProp) {
      seen.add(lineDrawProp);
      if (delegate) {
        super.setFloatLabel(lineDrawProp);
      }
    }
  }

  /**
   * A foreign subclass that replaces the inherited behaviour instead of delegating to it.
   */
  private static final class SubstitutingLineClip extends LineClip {

    private final List<LineDrawProp> seen = new ArrayList<>();

    private SubstitutingLineClip(DrawGraph drawGraph) {
      this.drawGraph = drawGraph;
    }

    @Override
    protected void setFloatLabel(LineDrawProp lineDrawProp) {
      seen.add(lineDrawProp);
      FloatLabel[] labels = lineDrawProp.lineAttrs().getFloatLabels();
      if (labels == null) {
        return;
      }
      for (FloatLabel label : labels) {
        lineDrawProp.addFloatLabelCenter(label, MARKER);
      }
    }
  }
}
