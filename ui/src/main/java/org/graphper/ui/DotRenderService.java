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

package org.graphper.ui;

import java.nio.charset.StandardCharsets;
import org.antlr.v4.runtime.CharStreams;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.SecurityPolicy;
import org.graphper.draw.common.BatikImgConverter;
import org.graphper.parser.DotParser;
import org.graphper.parser.ParseException;
import org.graphper.parser.PostGraphComponents;

/**
 * Turns DOT source into the resources the editor needs: an SVG string for the live preview and byte
 * payloads for SVG and PNG export.
 *
 * <p>Every parse runs under a {@link SecurityPolicy}. The no-argument constructor keeps the secure
 * default (embedded base64 raster images only) unless {@link #ALLOWED_IMAGE_HOSTS_PROPERTY} or
 * {@link #IMAGE_BASE_DIRECTORY_PROPERTY} names an explicit opt-in; the graph-support CLI sets those
 * from {@code ui --allow-image-host <host>} and {@code ui --image-dir <directory>}. Embedders that
 * build the editor themselves can pass a policy to {@link #DotRenderService(SecurityPolicy)}.</p>
 *
 * @author Jamison Jiang
 */
public class DotRenderService {

  /** Comma separated hostnames whose {@code http}/{@code https} images may be loaded. */
  public static final String ALLOWED_IMAGE_HOSTS_PROPERTY = "graph.support.image.allowed.hosts";

  /** Directory that filesystem image references must resolve inside. */
  public static final String IMAGE_BASE_DIRECTORY_PROPERTY = "graph.support.image.base.dir";

  private final SecurityPolicy securityPolicy;

  private final PostGraphComponents policyComponents = new PostGraphComponents() {
    @Override
    public void postGraphviz(GraphvizBuilder graphvizBuilder) {
      graphvizBuilder.securityPolicy(securityPolicy);
    }
  };

  /**
   * Creates a service whose policy comes from {@link #ALLOWED_IMAGE_HOSTS_PROPERTY} and
   * {@link #IMAGE_BASE_DIRECTORY_PROPERTY}, falling back to {@link SecurityPolicy#defaultPolicy()}
   * when neither is set.
   */
  public DotRenderService() {
    this(policyFromSystemProperties());
  }

  /**
   * Creates a service that renders under the given policy.
   *
   * @param securityPolicy the policy to render under, {@code null} for the secure default
   */
  public DotRenderService(SecurityPolicy securityPolicy) {
    this.securityPolicy = securityPolicy == null ? SecurityPolicy.defaultPolicy() : securityPolicy;
  }

  /**
   * Gets the policy every parse of this service runs under.
   *
   * @return the security policy
   */
  public SecurityPolicy securityPolicy() {
    return securityPolicy;
  }

  /**
   * Reads the image opt-ins from the system properties. Anything malformed is ignored rather than
   * thrown, because the editor must still open; the CLI validates the same values up front and
   * reports them there.
   *
   * @return the configured policy, or {@link SecurityPolicy#defaultPolicy()} when nothing is set
   */
  static SecurityPolicy policyFromSystemProperties() {
    String hosts = System.getProperty(ALLOWED_IMAGE_HOSTS_PROPERTY);
    String baseDirectory = System.getProperty(IMAGE_BASE_DIRECTORY_PROPERTY);
    if (StringUtils.isBlank(hosts) && StringUtils.isBlank(baseDirectory)) {
      return SecurityPolicy.defaultPolicy();
    }
    SecurityPolicy.Builder builder = SecurityPolicy.builder();
    boolean changed = false;
    if (StringUtils.isNotBlank(hosts)) {
      for (String host : hosts.split(",")) {
        if (StringUtils.isBlank(host)) {
          continue;
        }
        try {
          builder.allowRemoteImageHost(host.trim());
          changed = true;
        } catch (IllegalArgumentException ignore) {
          // A malformed host simply stays denied.
        }
      }
      builder.allowRemoteImages(changed);
    }
    if (StringUtils.isNotBlank(baseDirectory)) {
      try {
        builder.localImageBaseDirectory(baseDirectory.trim());
        changed = true;
      } catch (RuntimeException ignore) {
        // An unusable directory simply stays denied.
      }
    }
    return changed ? builder.build() : SecurityPolicy.defaultPolicy();
  }

  /**
   * Parses and lays out the given DOT source and returns the rendered SVG.
   *
   * @param dot the DOT source to render
   * @return the rendered SVG document as a string
   * @throws IllegalArgumentException if the source produces an empty graph
   * @throws IllegalStateException    if the graph cannot be rendered
   */
  public String renderSvg(String dot) {
    Graphviz graphviz = parse(dot);
    if (graphviz.isEmpty()) {
      throw new IllegalArgumentException("Graph is empty");
    }
    try {
      return graphviz.toSvgStr();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to render graph", e);
    }
  }

  /**
   * Checks the given DOT source for syntax errors without laying the graph out, so the editor can
   * flag mistakes while typing. Only genuine syntax errors are reported; blank input and other
   * non-syntax conditions (such as an empty graph) return {@code null}.
   *
   * @param dot the DOT source to check
   * @return the first syntax error, or {@code null} when the source parses cleanly
   */
  public DotError findError(String dot) {
    if (StringUtils.isBlank(dot)) {
      return null;
    }
    try {
      parse(dot);
      return null;
    } catch (ParseException e) {
      return DotError.fromMessage(e.getMessage());
    } catch (RuntimeException e) {
      // Non-syntax problems (for example an empty graph) are surfaced by the render status only.
      return null;
    }
  }

  /**
   * Mirrors {@link DotParser#parse(String)} but attaches this service's security policy, which the
   * string overload has no hook for.
   */
  private Graphviz parse(String dot) {
    if (StringUtils.isEmpty(dot)) {
      throw new IllegalArgumentException("Empty dot");
    }
    return DotParser.parse(CharStreams.fromString(dot, "anonymous String"), policyComponents);
  }

  /**
   * Encodes an SVG document as UTF-8 bytes for saving to disk.
   *
   * @param svg the SVG document
   * @return the UTF-8 encoded bytes
   */
  public byte[] svgBytes(String svg) {
    return svg.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Transcodes static SVG to PNG under this service's pixel budget. External resources and active
   * SVG content are rejected, including image references to explicitly allowed hosts or files.
   *
   * @param svg the SVG document
   * @return the encoded PNG bytes
   * @throws IllegalStateException if the SVG cannot be transcoded
   */
  public byte[] pngBytes(String svg) {
    try {
      return new BatikImgConverter().pngBytes(svg, securityPolicy);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to export PNG", e);
    }
  }
}
