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

package org.graphper.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.Set;
import org.graphper.api.Graphviz;
import org.junit.jupiter.api.Test;

class CoreResilienceTest {

  private static final String SERVICES = "META-INF/services/";
  private static final String MEASURE = SERVICES + "org.graphper.layout.MeasureText";
  private static final String FONT = SERVICES + "org.graphper.layout.FontSelector";
  private static final String CONVERTER = SERVICES + "org.graphper.draw.common.SvgConverter";
  private static final String FIXTURE = "org.graphper.util.CoreResilienceFixture";

  @Test
  void realCoreLayoutsAndSvgDoNotDiscoverConvertersWithoutOptionalApis() throws Throwable {
    try (IsolatedLoader loader = new IsolatedLoader()) {
      String[] svgs = (String[]) invoke(loader, "render");
      for (String svg : svgs) {
        assertTrue(svg.contains("<svg"));
      }
      // The last two results exercise an unlaid-out CommonDrawBoard directly.
      for (int i = 0; i < svgs.length - 2; i++) {
        assertTrue(svgs[i].contains("first"));
        assertTrue(svgs[i].contains("second"));
        assertTrue(svgs[i].contains("<path"));
        assertFalse(svgs[i].contains("NaN"));
        assertFalse(svgs[i].contains("Infinity"));
      }
      assertEquals(0, loader.converterDiscovery);
      assertFalse(loader.attempted.contains("org.graphper.draw.common.BatikImgConverter"));
      assertFalse(loader.attempted.contains("org.graphper.draw.common.DefaultImgConverter"));
      assertFalse(loader.attempted.contains("org.graphper.draw.common.AndroidImgConverter"));
      assertFalse(loader.attempted.contains("org.graphper.draw.common.SecureImageLoader"));
      assertTrue(loader.attempted.stream().anyMatch(name -> name.startsWith("java.awt.")));
      assertTrue(loader.attempted.contains("android.graphics.Rect"));
      assertFalse(loader.attempted.stream().anyMatch(name -> name.startsWith("javax.")));
      assertArrayEquals(new double[]{50, 40}, (double[]) invoke(loader, "measure"));
      assertArrayEquals(new String[]{"Times New Roman", "Times New Roman", "Viewer Font", "false"},
          (String[]) invoke(loader, "fonts"));
    }
  }

  @Test
  void missingServiceDescriptorsStillHaveFontFallbacks() throws Throwable {
    try (IsolatedLoader loader = new IsolatedLoader()) {
      loader.descriptors.put(MEASURE, Collections.emptyList());
      loader.descriptors.put(FONT, Collections.emptyList());
      assertArrayEquals(new double[]{50, 40}, (double[]) invoke(loader, "measure"));
      assertEquals("Times New Roman", ((String[]) invoke(loader, "fonts"))[0]);
    }
  }

  @Test
  void brokenProvidersDoNotHideSupportedCustomProvidersOrChangePriority() throws Throwable {
    try (IsolatedLoader loader = new IsolatedLoader()) {
      String entries = providers("BrokenConstructor", "BrokenLinkage", "BrokenEnvironment",
          "BrokenOrder", "UnsupportedFont", "LaterFont", "CustomFont", "TiedFont");
      loader.descriptors.put(MEASURE, Collections.singletonList(entries));
      loader.descriptors.put(FONT, Collections.singletonList(
          providers("BrokenFontEnumeration") + entries));
      assertArrayEquals(new String[]{"CustomFont", "TiedFont", "LaterFont"},
          (String[]) invoke(loader, "providerNames"));
      assertArrayEquals(new double[]{13, 7}, (double[]) invoke(loader, "measure"));
      assertEquals("Custom SVG Font", ((String[]) invoke(loader, "fonts"))[0]);
    }
  }

  @Test
  void malformedDescriptorDoesNotHideProvidersInTheNextResource() throws Throwable {
    try (IsolatedLoader loader = new IsolatedLoader()) {
      List<String> descriptors = new ArrayList<>();
      descriptors.add("not a legal provider name\n");
      descriptors.add("missing.optional.Provider\n" + providers("CustomFont"));
      loader.descriptors.put(MEASURE, descriptors);
      assertArrayEquals(new String[]{"CustomFont"}, (String[]) invoke(loader, "providerNames"));
    }
  }

  @Test
  void nonAdvancingDiscoveryHasAFiniteFailureBudget() {
    assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
      try (IsolatedLoader loader = new IsolatedLoader()) {
        loader.stalledService = MEASURE;
        assertEquals(0, invoke(loader, "providerCount"));
        assertTrue(loader.failedPolls > 0);
        assertTrue(loader.failedPolls <= 32);
      }
    });
  }

  @Test
  void rasterDiscoveryIsLazyAndKeepsCustomPriorityEvenWithImages() throws Throwable {
    try (IsolatedLoader loader = new IsolatedLoader()) {
      loader.descriptors.put(CONVERTER, Collections.singletonList(
          providers("BrokenConverter", "LaterConverter", "CustomConverter")));
      invoke(loader, "render");
      assertEquals(0, loader.converterDiscovery);
      assertEquals("CustomConverter", invoke(loader, "raster"));
      assertEquals("CustomConverter", invoke(loader, "raster"));
      assertEquals(1, loader.converterDiscovery);
    }
  }

  @Test
  void customConverterRetainsPriorityOverAvailableNativeImageConverter() throws Throwable {
    try (IsolatedLoader loader = new IsolatedLoader()) {
      loader.denyDesktop = false;
      loader.descriptors.put(CONVERTER, Collections.singletonList(
          "org.graphper.draw.common.DefaultImgConverter\n" + providers("CustomConverter")));
      assertEquals("CustomConverter", invoke(loader, "raster"));
      assertTrue(loader.attempted.contains("org.graphper.draw.common.DefaultImgConverter"));
    }
  }

  @Test
  void unavailableRasterDoesNotPoisonLaterSvgRendering() throws Throwable {
    try (IsolatedLoader loader = new IsolatedLoader()) {
      assertTrue(((String) invoke(loader, "unsupportedRaster"))
          .contains("No secure converter available for PNG"));
      assertEquals(1, loader.converterDiscovery);
      assertTrue(((String[]) invoke(loader, "render"))[0].contains("<svg"));
      assertEquals(1, loader.converterDiscovery);
    }
  }

  private static String providers(String... names) {
    StringBuilder entries = new StringBuilder();
    for (String name : names) {
      entries.append(FIXTURE).append('$').append(name).append('\n');
    }
    return entries.toString();
  }

  private static Object invoke(IsolatedLoader loader, String method) throws Throwable {
    Thread thread = Thread.currentThread();
    ClassLoader previous = thread.getContextClassLoader();
    thread.setContextClassLoader(loader);
    try {
      Class<?> fixture = loader.loadClass(FIXTURE);
      assertSame(loader, fixture.getClassLoader());
      assertSame(loader, loader.loadClass(Graphviz.class.getName()).getClassLoader());
      return fixture.getMethod(method).invoke(null);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    } finally {
      thread.setContextClassLoader(previous);
    }
  }

  private static final class IsolatedLoader extends URLClassLoader {
    private final URLClassLoader coreResources;
    private final Map<String, List<String>> descriptors = new HashMap<>();
    private final Set<String> attempted = new HashSet<>();
    private boolean denyDesktop = true;
    private String stalledService;
    private int converterDiscovery;
    private int failedPolls;

    private IsolatedLoader() {
      super(new URL[]{Graphviz.class.getProtectionDomain().getCodeSource().getLocation(),
          CoreResilienceTest.class.getProtectionDomain().getCodeSource().getLocation()},
          CoreResilienceTest.class.getClassLoader());
      coreResources = new URLClassLoader(new URL[]{getURLs()[0]}, null);
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException {
      attempted.add(name);
      if ((denyDesktop && (name.startsWith("java.awt.") || name.startsWith("javax.")))
          || name.startsWith("android.") || name.startsWith("org.apache.batik.")
          || name.startsWith("org.apache.fop.")) {
        throw new ClassNotFoundException("Optional API denied: " + name);
      }
      // Never delegate core APIs to the parent: doing so would test its unrestricted runtime.
      // JDK, JUnit and logging APIs remain parent-loaded; fixture results use only JDK types.
      if (name.startsWith("org.graphper.") || name.startsWith("org.apache_gs.")) {
        Class<?> loaded = findLoadedClass(name);
        if (loaded == null) {
          loaded = findClass(name);
        }
        if (resolve) {
          resolveClass(loaded);
        }
        return loaded;
      }
      return super.loadClass(name, resolve);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
      if (CONVERTER.equals(name)) {
        converterDiscovery++;
      }
      if (name.equals(stalledService)) {
        return new Enumeration<URL>() {
          @Override
          public boolean hasMoreElements() {
            failedPolls++;
            throw new ServiceConfigurationError("Resource enumeration cannot advance");
          }

          @Override
          public URL nextElement() {
            throw new AssertionError("No resource available");
          }
        };
      }
      if (descriptors.containsKey(name)) {
        List<URL> resources = new ArrayList<>();
        for (String text : descriptors.get(name)) {
          resources.add(new URL(null, "memory:provider-" + resources.size(), new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL url) {
              return new URLConnection(url) {
                @Override
                public void connect() {
                }

                @Override
                public ByteArrayInputStream getInputStream() {
                  return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
                }
              };
            }
          }));
        }
        return Collections.enumeration(resources);
      }
      // Exclude the ordinary test classpath's DeterministicMeasureText SPI override.
      return name.startsWith(SERVICES) ? coreResources.getResources(name) : super.getResources(name);
    }

    @Override
    public void close() throws IOException {
      try {
        coreResources.close();
      } finally {
        super.close();
      }
    }
  }
}
