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

package org.graphper.draw.common;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.graphper.api.SecurityPolicy;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/** Closed, static SVG subset shared by the Batik raster and PDF entry points. */
final class SecureSvg {

  private static final String SVG_NS = "http://www.w3.org/2000/svg";
  private static final String XLINK_NS = "http://www.w3.org/1999/xlink";
  // Exact declaration emitted by SvgDocument. No other DTD, including an internal subset, is read.
  private static final String PROJECT_DOCTYPE = "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" "
      + "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">";
  static final int MAX_INPUT_CHARS = 16 * 1024 * 1024;
  static final int MAX_DEPTH = 128;
  static final int MAX_ELEMENTS = 200_000;
  static final int MAX_ATTRIBUTES_PER_ELEMENT = 128;
  static final int MAX_ATTRIBUTES = 1_000_000;
  static final int MAX_IMAGES = 256;
  static final long MAX_IMAGE_BYTES = 32L * 1024 * 1024;
  static final long MAX_ENCODED_IMAGE_BYTES = 48L * 1024 * 1024;
  static final long MAX_RASTER_IMAGE_BYTES = 128L * 1024 * 1024;
  private static final Set<String> ELEMENTS = new HashSet<>(Arrays.asList(
      "svg", "g", "a", "defs", "title", "desc", "path", "rect", "circle", "ellipse",
      "line", "polyline", "polygon", "text", "tspan", "image",
      "linearGradient", "radialGradient", "stop", "clipPath"));
  private static final Set<String> ATTRIBUTES = new HashSet<>(Arrays.asList(
      "id", "class", "data-node-decoration", "width", "height", "viewBox",
      "preserveAspectRatio", "version",
      "x", "y", "x1", "y1", "x2", "y2", "cx", "cy", "r", "rx", "ry", "dx", "dy",
      "d", "points", "transform", "fill", "fill-opacity", "fill-rule", "opacity",
      "stroke", "stroke-width", "stroke-opacity", "stroke-dasharray", "stroke-dashoffset",
      "stroke-linecap", "stroke-linejoin", "stroke-miterlimit", "font-family", "font-size",
      "font-style", "font-weight", "text-anchor", "text-decoration", "dominant-baseline",
      "alignment-baseline", "baseline-shift", "textLength", "lengthAdjust", "rotate",
      "offset", "stop-color", "stop-opacity", "gradientUnits", "gradientTransform",
      "spreadMethod", "fx", "fy", "clip-path", "clip-rule", "clipPathUnits"));

  private SecureSvg() {
  }

  static String prepare(String svg, SecurityPolicy policy, boolean raster) throws Exception {
    if (svg == null || svg.length() > MAX_INPUT_CHARS) {
      throw new IllegalArgumentException("SVG exceeds the input character limit " + MAX_INPUT_CHARS);
    }
    // Strip only a literal declaration in the prolog, never search/replace inside XML content.
    int prolog = 0;
    if (svg.startsWith("\uFEFF")) {
      prolog++;
    }
    if (svg.startsWith("<?xml ", prolog)) {
      int end = svg.indexOf("?>", prolog);
      if (end >= 0) {
        prolog = end + 2;
      }
    }
    while (prolog < svg.length() && " \t\r\n".indexOf(svg.charAt(prolog)) >= 0) {
      prolog++;
    }
    if (svg.startsWith(PROJECT_DOCTYPE, prolog)) {
      svg = svg.substring(0, prolog) + svg.substring(prolog + PROJECT_DOCTYPE.length());
    }
    preflight(svg);
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    factory.setXIncludeAware(false);
    factory.setExpandEntityReferences(false);
    javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver((publicId, systemId) -> {
      throw new IllegalArgumentException("External SVG entities are forbidden");
    });
    org.w3c.dom.Document document = builder.parse(new InputSource(new StringReader(svg)));
    Element root = document.getDocumentElement();
    if (!"svg".equals(root.getLocalName()) || !SVG_NS.equals(root.getNamespaceURI())) {
      throw new IllegalArgumentException("An SVG namespace root is required");
    }
    validate(document);
    if (raster) {
      // Batik uses floats. Normalize to px so unit conversion/rounding cannot exceed our budget.
      float pixelWidth = (float) length(root.getAttribute("width"));
      float pixelHeight = (float) length(root.getAttribute("height"));
      double width = Math.ceil(pixelWidth);
      double height = Math.ceil(pixelHeight);
      long maximum = policy.getMaxOutputPixels();
      if (!Double.isFinite(width) || !Double.isFinite(height) || width <= 0 || height <= 0
          || width > Integer.MAX_VALUE || height > Integer.MAX_VALUE
          || width > maximum / height) {
        throw new IllegalArgumentException("Rendered image " + width + "x" + height
            + " exceeds the security policy pixel limit " + maximum);
      }
      root.setAttribute("width", Float.toString(pixelWidth));
      root.setAttribute("height", Float.toString(pixelHeight));
    }
    // No image I/O until the entire document and output dimensions have passed validation.
    ImageBudget images = new ImageBudget();
    NodeList imageElements = root.getElementsByTagNameNS(SVG_NS, "image");
    for (int i = 0; i < imageElements.getLength(); i++) {
      Element image = (Element) imageElements.item(i);
      Node href = image.getAttributeNodeNS(XLINK_NS, "href");
      if (href == null) {
        href = image.getAttributeNode("href");
      }
      if (href != null) {
        String embedded = validateImage(href.getNodeValue().trim(), policy, images);
        image.removeAttributeNode((org.w3c.dom.Attr) href);
        image.setAttributeNS(XLINK_NS, "xlink:href", embedded);
      }
    }
    TransformerFactory transformers = TransformerFactory.newInstance();
    transformers.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    transformers.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    transformers.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    Transformer transformer = transformers.newTransformer();
    StringWriter output = new StringWriter();
    transformer.transform(new DOMSource(root), new StreamResult(output));
    return output.toString();
  }

  private static void preflight(String svg) throws Exception {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setXIncludeAware(false);
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    XMLReader reader = factory.newSAXParser().getXMLReader();
    reader.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    reader.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    // Include xmlns declarations in the attribute budgets, not just ordinary attributes.
    reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
    reader.setEntityResolver((publicId, systemId) -> {
      throw new IllegalArgumentException("External SVG entities are forbidden");
    });
    DefaultHandler handler = new DefaultHandler() {
      private int depth;
      private int elements;
      private int attributes;
      private int images;

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attrs) {
        if (++depth > MAX_DEPTH) {
          throw new IllegalArgumentException("SVG exceeds the XML nesting depth limit " + MAX_DEPTH);
        }
        if (++elements > MAX_ELEMENTS) {
          throw new IllegalArgumentException("SVG exceeds the element limit " + MAX_ELEMENTS);
        }
        attributes += attrs.getLength();
        if (attrs.getLength() > MAX_ATTRIBUTES_PER_ELEMENT || attributes > MAX_ATTRIBUTES) {
          throw new IllegalArgumentException("SVG exceeds the XML attribute limit");
        }
        if (!SVG_NS.equals(uri) || !ELEMENTS.contains(localName)) {
          throw new IllegalArgumentException("Unsupported active or foreign SVG element: " + localName);
        }
        if ("image".equals(localName) && ++images > MAX_IMAGES) {
          throw new IllegalArgumentException("SVG exceeds the image count limit " + MAX_IMAGES);
        }
      }

      @Override
      public void endElement(String uri, String localName, String qName) {
        depth--;
      }

      @Override
      public void processingInstruction(String target, String data) {
        throw new IllegalArgumentException("SVG processing instructions are forbidden");
      }
    };
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(new InputSource(new StringReader(svg)));
  }

  private static void validate(Node node) {
    if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE
        || node.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
      throw new IllegalArgumentException("SVG processing instructions and entities are forbidden");
    }
    if (node instanceof Element) {
      Element element = (Element) node;
      String tag = element.getLocalName();
      if (!SVG_NS.equals(element.getNamespaceURI()) || !ELEMENTS.contains(tag)) {
        throw new IllegalArgumentException("Unsupported active or foreign SVG element: " + tag);
      }
      if ("image".equals(tag) && element.hasAttribute("href")
          && element.hasAttributeNS(XLINK_NS, "href")) {
        throw new IllegalArgumentException("Ambiguous SVG image href attributes are forbidden");
      }
      NamedNodeMap attributes = element.getAttributes();
      for (int i = attributes.getLength() - 1; i >= 0; i--) {
        Node attribute = attributes.item(i);
        String name = attribute.getLocalName();
        String namespace = attribute.getNamespaceURI();
        String value = attribute.getNodeValue().trim();
        if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespace)) {
          continue;
        }
        if (XMLConstants.XML_NS_URI.equals(namespace) && "space".equals(name)) {
          continue;
        }
        if (XLINK_NS.equals(namespace) && "title".equals(name)) {
          element.removeAttributeNode((org.w3c.dom.Attr) attribute);
          continue;
        }
        if ("href".equals(name) && (namespace == null
            || XLINK_NS.equals(namespace))) {
          if ("a".equals(tag)) {
            // Hyperlinks have no meaning in an exported bitmap/PDF and need no resource access.
            element.removeAttributeNode((org.w3c.dom.Attr) attribute);
          } else if (!"image".equals(tag)) {
            throw new IllegalArgumentException("SVG href references, including gradient chains, "
                + "are forbidden outside images and navigation links");
          }
          continue;
        }
        if ("a".equals(tag) && namespace == null && "target".equals(name)) {
          element.removeAttributeNode((org.w3c.dom.Attr) attribute);
          continue;
        }
        // DOT names become IDs verbatim. Metadata values are not CSS/resource expressions;
        // parentheses and backslashes here cannot load resources or introduce selectors.
        if (namespace == null && ("id".equals(name) || "class".equals(name)
            || "data-node-decoration".equals(name))) {
          continue;
        }
        if (namespace != null || !ATTRIBUTES.contains(name) || value.indexOf('\\') >= 0
            || value.indexOf('(') >= 0 && !"transform".equals(name)
            && !"gradientTransform".equals(name)
            && !value.matches("url\\(#[A-Za-z_][A-Za-z0-9_.:-]*\\)")
            && !value.matches("rgba?\\([0-9.,% +\\-]+\\)")) {
          throw new IllegalArgumentException("Unsupported SVG attribute: " + attribute.getNodeName());
        }
      }
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
      validate(child);
    }
  }

  private static String validateImage(String reference, SecurityPolicy policy, ImageBudget budget)
      throws Exception {
    // Clamp the loader itself, so even a caller's larger per-image policy cannot allocate past the
    // remaining document budget. The encoded allowance includes a conservative data URI header.
    long remaining = Math.min(MAX_IMAGE_BYTES - budget.bytes,
        (MAX_ENCODED_IMAGE_BYTES - budget.encodedBytes - 32) / 4 * 3);
    if (remaining <= 0) {
      throw new IllegalArgumentException("SVG exceeds the aggregate image byte limit");
    }
    SecurityPolicy.Builder bounded = SecurityPolicy.builder()
        .allowRemoteImages(policy.isAllowRemoteImages())
        .connectTimeoutMillis(policy.getConnectTimeoutMillis())
        .readTimeoutMillis(policy.getReadTimeoutMillis())
        .maxImageBytes((int) Math.min(policy.getMaxImageBytes(), remaining))
        .maxImagePixels(policy.getMaxImagePixels()).maxOutputPixels(policy.getMaxOutputPixels());
    for (String host : policy.getAllowedRemoteImageHosts()) {
      bounded.allowRemoteImageHost(host);
    }
    if (policy.getLocalImageBaseDirectory() != null) {
      bounded.localImageBaseDirectory(policy.getLocalImageBaseDirectory());
    }
    byte[] bytes = SecureImageLoader.load(reference, bounded.build());
    budget.bytes += bytes.length;
    try (ImageInputStream input = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
      Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
      if (!readers.hasNext()) {
        throw new IllegalArgumentException("SVG image must contain a supported raster image");
      }
      ImageReader reader = readers.next();
      try {
        String format = reader.getFormatName().toLowerCase(Locale.ROOT);
        if (!Arrays.asList("png", "jpeg", "gif", "bmp").contains(format)) {
          throw new IllegalArgumentException("Unsupported embedded raster format");
        }
        String mime = "image/" + format;
        if (reference.regionMatches(true, 0, "data:", 0, 5)
            && !reference.substring(5, reference.indexOf(';')).equalsIgnoreCase(mime)) {
          throw new IllegalArgumentException("SVG image MIME type does not match its raster format");
        }
        reader.setInput(input, false, true);
        int width = reader.getWidth(0);
        int height = reader.getHeight(0);
        if (width <= 0 || height <= 0 || (long) width * height > policy.getMaxImagePixels()) {
          throw new IllegalArgumentException("Embedded image exceeds the security policy pixel limit");
        }
        long pixels = (long) width * height;
        // Allow up to 16 bits in each of four channels for the supported raster formats.
        if (pixels > (MAX_RASTER_IMAGE_BYTES - budget.rasterBytes) / 8) {
          throw new IllegalArgumentException("SVG exceeds the aggregate decoded raster byte limit");
        }
        budget.rasterBytes += pixels * 8;
        if (reader.getNumImages(true) != 1) {
          throw new IllegalArgumentException("SVG images must contain a single raster frame");
        }
        // Validate the raster payload, not only its dimensions, before handing it to Batik.
        if (reader.read(0) == null) {
          throw new IllegalArgumentException("SVG image raster cannot be decoded");
        }
        long encodedBytes = 4L * ((bytes.length + 2L) / 3) + mime.length() + 13;
        if (encodedBytes > MAX_ENCODED_IMAGE_BYTES - budget.encodedBytes) {
          throw new IllegalArgumentException("SVG exceeds the aggregate encoded image byte limit");
        }
        budget.encodedBytes += encodedBytes;
        return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
      } finally {
        reader.dispose();
      }
    }
  }

  private static final class ImageBudget {
    private long bytes;
    private long encodedBytes;
    private long rasterBytes;
  }

  private static double length(String value) {
    String normalized = value.trim();
    double factor = 1;
    if (normalized.endsWith("pt")) {
      factor = 96D / 72D;
      normalized = normalized.substring(0, normalized.length() - 2);
    } else if (normalized.endsWith("px")) {
      normalized = normalized.substring(0, normalized.length() - 2);
    }
    // Percentages and context-dependent dimensions cannot safely bound a raster allocation.
    return Double.parseDouble(normalized) * factor;
  }
}
