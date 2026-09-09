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

package org.graphper.draw.svg.node;

import java.util.Locale;
import org.graphper.api.NodeAttrs;
import org.graphper.api.SecurityPolicy;
import org.graphper.def.FlatPoint;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.NodeEditor;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Adds positioned images to SVG nodes. */
public class NodeImageEditor extends AbstractNodeShapeEditor implements NodeEditor<SvgBrush> {

  private static final Logger log = LoggerFactory.getLogger(NodeImageEditor.class);

  /** Keeps a rejected data URI from filling the log with base64. */
  private static final int MAX_LOGGED_REFERENCE_LENGTH = 120;

  @Override
  public boolean edit(NodeDrawProp node, SvgBrush brush) {
    NodeAttrs nodeAttrs = node.nodeAttrs();
    if (nodeAttrs.getImage() == null) {
      return true;
    }

    SecurityPolicy securityPolicy = brush.drawBoard().graphAttrs().getSecurityPolicy();
    String image = securityPolicy.sanitizeImage(nodeAttrs.getImage());
    if (image == null) {
      logRejected(node, nodeAttrs.getImage(), securityPolicy);
      return true;
    }

    Element imageEle = brush.getOrCreateShapeEleById(IMAGE_ELE, IMAGE_ELE);
    imageEle.setAttribute(XLINK + COLON + HREF, image);

    if (nodeAttrs.getImageSize() != null) {
      FlatPoint imageSize = nodeAttrs.getImageSize();
      double width = imageSize.getWidth();
      double height = imageSize.getHeight();
      imageEle.setAttribute(WIDTH, String.valueOf(width));
      imageEle.setAttribute(HEIGHT, String.valueOf(height));
      imageEle.setAttribute(X, String.valueOf(node.getX() - width / 2));
      imageEle.setAttribute(Y, String.valueOf(node.getY() - height / 2));
    } else {
      imageEle.setAttribute(WIDTH, String.valueOf(node.getWidth()));
      imageEle.setAttribute(HEIGHT, String.valueOf(node.getHeight()));
      imageEle.setAttribute(X, String.valueOf(node.getLeftBorder()));
      imageEle.setAttribute(Y, String.valueOf(node.getUpBorder()));
    }
    return true;
  }

  /**
   * The layout has already reserved the image box by the time the reference is rejected, so the
   * node renders as an empty area. Without this record the drop is completely silent, which is the
   * hardest failure to diagnose.
   */
  private void logRejected(NodeDrawProp node, String image, SecurityPolicy securityPolicy) {
    log.warn(
        "Node {} image reference was rejected by the security policy and will not be "
            + "rendered, but its space is still reserved: {}. {}",
        nodeDescription(node),
        abbreviate(image),
        enableHint(image, securityPolicy));
  }

  /**
   * Explains the exact switch that would let this particular reference through, so the message is
   * actionable instead of merely descriptive.
   */
  static String enableHint(String image, SecurityPolicy securityPolicy) {
    String reference = image == null ? "" : image.trim();
    String lower = reference.toLowerCase(Locale.ROOT);
    if (lower.startsWith("data:")) {
      return "Embedded data images are allowed by default; this one was refused because it is not"
          + " a supported base64 raster type (png, jpeg, gif, webp, bmp) or it exceeds"
          + " SecurityPolicy.Builder#maxImageBytes ("
          + securityPolicy.getMaxImageBytes()
          + " bytes).";
    }
    if (lower.startsWith("http://") || lower.startsWith("https://")) {
      if (!securityPolicy.isAllowRemoteImages()) {
        return "Remote images are denied by default. Enable them with"
            + " Graphviz.GraphvizBuilder#securityPolicy and a SecurityPolicy that calls"
            + " allowRemoteImages(true).allowRemoteImageHost(\"<host>\"); the graph-support CLI"
            + " exposes the same switch as --allow-image-host <host>.";
      }
      return "The host is not in the allow list "
          + securityPolicy.getAllowedRemoteImageHosts()
          + ". Add it with SecurityPolicy.Builder#allowRemoteImageHost, or with the graph-support"
          + " CLI option --allow-image-host <host>.";
    }
    if (securityPolicy.getLocalImageBaseDirectory() == null) {
      return "Filesystem images are denied by default because no local image base directory is"
          + " set. Set one with SecurityPolicy.Builder#localImageBaseDirectory, or with the"
          + " graph-support CLI option --image-dir <directory>.";
    }
    return "The reference does not resolve inside the local image base directory "
        + securityPolicy.getLocalImageBaseDirectory()
        + ".";
  }

  private static String nodeDescription(NodeDrawProp node) {
    NodeAttrs nodeAttrs = node.nodeAttrs();
    if (nodeAttrs.getId() != null) {
      return nodeAttrs.getId();
    }
    if (nodeAttrs.getLabel() != null) {
      return abbreviate(nodeAttrs.getLabel());
    }
    return "#" + node.nodeNo();
  }

  private static String abbreviate(String image) {
    if (image == null) {
      return "null";
    }
    return image.length() <= MAX_LOGGED_REFERENCE_LENGTH
        ? image
        : image.substring(0, MAX_LOGGED_REFERENCE_LENGTH) + "...(" + image.length() + " chars)";
  }
}
