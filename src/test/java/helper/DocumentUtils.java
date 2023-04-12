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

package helper;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Objects;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.w3c.dom.Document;

public class DocumentUtils {

  private DocumentUtils() {
  }

  private static final TransformerFactory tfac;

  static {
    tfac = TransformerFactory.newInstance();
  }

  public static String getClassPath() {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    return Objects.requireNonNull(loader.getResource("")).getPath();
  }

  public static String getTestPngPath() {
    return getClassPath() + getRelativeTestPngPath();
  }

  public static String getRelativeTestPngPath() {
    return "/testpng/";
  }

  public static String getTestSerialPath() {
    return getClassPath() + "/serial/";
  }

  public static String getTableCasesPath() {
    return getClassPath() + "/table/";
  }

  public static String getVisualHtmlTemplatePath() {
    return getClassPath() + "/graph-visual-template.html";
  }

  public static String getVisualHtmlPath() {
    return getClassPath() + "/visual/graph-visual.html";
  }

  public static String docToXml(Document document) throws TransformerException, IOException {
    if (document == null) {
      return null;
    }

    String result;
    try (StringWriter strWtr = new StringWriter()) {
      StreamResult strResult = new StreamResult(strWtr);
      Transformer transformer = tfac.newTransformer();
      transformer.transform(
          new DOMSource(document.getDocumentElement()),
          strResult
      );
      result = strResult.getWriter().toString();
    }

    return result;
  }

  public static void svgDocToImg(InputStream in, OutputStream os)
      throws IOException, TranscoderException {
    try (OutputStream out = new BufferedOutputStream(os)) {
      Transcoder transcoder = new PNGTranscoder();
      TranscoderInput input = new TranscoderInput(in);
      TranscoderOutput output = new TranscoderOutput(out);
      transcoder.transcode(input, output);
    } finally {
      in.close();
    }
  }
}
