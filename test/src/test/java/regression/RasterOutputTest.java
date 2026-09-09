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

package regression;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.apache.xmlgraphics.image.codec.tiff.TIFFDecodeParam;
import org.apache.xmlgraphics.image.codec.tiff.TIFFImageDecoder;
import org.apache.xmlgraphics.image.codec.util.SeekableStream;
import org.graphper.api.FileType;
import org.graphper.api.GraphResource;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.SecurityPolicy;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Rankdir;
import org.graphper.draw.ExecuteException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class RasterOutputTest {

  @ParameterizedTest
  @EnumSource(value = FileType.class, names = {"PNG", "JPG", "JPEG", "TIFF"})
  public void plainGraphRendersWithBatik(FileType fileType) throws IOException, ExecuteException {
    Graphviz graphviz = Graphviz.digraph()
        .addNode(Node.builder().label("plain raster output").build())
        .build();

    if (fileType == FileType.TIFF && !ImageIO.getImageWritersByFormatName("TIFF").hasNext()) {
      ExecuteException failure = Assertions.assertThrows(ExecuteException.class,
          () -> graphviz.toFile(fileType));
      Throwable cause = failure;
      while (cause.getCause() != null) {
        cause = cause.getCause();
      }
      Assertions.assertTrue(cause.getMessage().contains("TIFF export requires an ImageIO TIFF writer"));
      return;
    }

    try (GraphResource resource = graphviz.toFile(fileType)) {
      byte[] bytes = resource.bytes();
      assertSignature(fileType, bytes);
      if (fileType == FileType.TIFF) {
        // Decode independently of the ImageIO writer, including on Java 8.
        try (SeekableStream input = SeekableStream.wrapInputStream(
            new ByteArrayInputStream(bytes), true)) {
          RenderedImage rendered = new TIFFImageDecoder(input, new TIFFDecodeParam())
              .decodeAsRenderedImage(0);
          Assertions.assertTrue(rendered.getWidth() > 0);
          Assertions.assertTrue(rendered.getHeight() > 0);
          Assertions.assertNotNull(rendered.getData());
        }
      }
    }
  }

  @ParameterizedTest
  @EnumSource(value = FileType.class, names = {"TIFF", "PDF"})
  public void approvedLocalImageIsPresentInActualGraphExport(FileType fileType,
                                                            @TempDir Path directory)
      throws Exception {
    Path file = directory.resolve("image.png");
    BufferedImage source = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < 10; y++) {
      for (int x = 0; x < 10; x++) {
        source.setRGB(x, y, 0xffff0000);
      }
    }
    Assertions.assertTrue(ImageIO.write(source, "png", file.toFile()));
    SecurityPolicy policy = SecurityPolicy.builder().localImageBaseDirectory(directory).build();
    for (String reference : new String[]{file.toUri().toString(), "image.png"}) {
      Assertions.assertEquals(file.toRealPath().toUri().toString(), policy.sanitizeImage(reference));
      // LR exposes why the canvas center is not a reliable image sample (including when another
      // integration test sets graph.rankdir globally). Keep that geometry in the regression fixture.
      Graphviz graph = Graphviz.digraph().securityPolicy(policy).rankdir(Rankdir.LR)
          .addNode(Node.builder().shape(NodeShapeEnum.BOX).label("").image(reference).build())
          .build();
      Assertions.assertTrue(graph.toSvgStr().contains("<image"),
          "The graph must retain the approved image before conversion");
      if (fileType == FileType.TIFF && !ImageIO.getImageWritersByFormatName("TIFF").hasNext()) {
        Assertions.assertThrows(ExecuteException.class, () -> graph.toFile(fileType));
        continue;
      }
      // TIFF/PDF have no native graph fallback: exercise the real Batik/FOP conversion path.
      try (GraphResource resource = graph.toFile(fileType)) {
        byte[] bytes = resource.bytes();
        assertSignature(fileType, bytes);
        if (fileType == FileType.TIFF) {
          // Use FOP's existing XML Graphics dependency, also on JDKs without an ImageIO TIFF reader.
          try (SeekableStream input = SeekableStream.wrapInputStream(
              new ByteArrayInputStream(bytes), true)) {
            RenderedImage rendered = new TIFFImageDecoder(input, new TIFFDecodeParam())
                .decodeAsRenderedImage(0);
            Raster pixels = rendered.getData();
            int redPixels = 0;
            int firstRedX = -1;
            int firstRedY = -1;
            for (int y = rendered.getMinY(); y < rendered.getMinY() + rendered.getHeight(); y++) {
              for (int x = rendered.getMinX(); x < rendered.getMinX() + rendered.getWidth(); x++) {
                if (rendered.getColorModel().getRGB(pixels.getDataElements(x, y, null)) == 0xffff0000) {
                  if (redPixels == 0) {
                    firstRedX = x;
                    firstRedY = y;
                  }
                  redPixels++;
                }
              }
            }
            // Only the source image is red. Require its pixels, not just a TIFF signature or a
            // single coincidental pixel; margins and aspect-ratio fitting can move it off-center.
            Assertions.assertTrue(redPixels >= source.getWidth() * source.getHeight(),
                "TIFF must contain the source image: " + rendered.getWidth() + "x"
                    + rendered.getHeight() + " redPixels="
                    + redPixels + " firstRed=" + firstRedX + "," + firstRedY + " SVG="
                    + graph.toSvgStr());
          }
        } else {
          String pdf = new String(bytes, StandardCharsets.ISO_8859_1);
          Assertions.assertTrue(pdf.matches("(?s).*/Subtype\\s*/Image\\b.*"),
              "PDF must embed a raster image object, not only an empty graph outline");
        }
      }
    }
  }

  private void assertSignature(FileType fileType, byte[] bytes) {
    Assertions.assertTrue(bytes.length >= 4, "Raster output is unexpectedly short");
    switch (fileType) {
      case PNG:
        Assertions.assertArrayEquals(new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47},
                                     new byte[]{bytes[0], bytes[1], bytes[2], bytes[3]});
        break;
      case JPG:
      case JPEG:
        Assertions.assertEquals((byte) 0xff, bytes[0]);
        Assertions.assertEquals((byte) 0xd8, bytes[1]);
        break;
      case TIFF:
        boolean littleEndian = bytes[0] == 'I' && bytes[1] == 'I'
            && bytes[2] == 0x2a && bytes[3] == 0;
        boolean bigEndian = bytes[0] == 'M' && bytes[1] == 'M'
            && bytes[2] == 0 && bytes[3] == 0x2a;
        Assertions.assertTrue(littleEndian || bigEndian, "Invalid TIFF signature");
        break;
      case PDF:
        Assertions.assertEquals("%PDF", new String(bytes, 0, 4, StandardCharsets.US_ASCII));
        break;
      default:
        Assertions.fail("Unexpected raster type " + fileType);
    }
  }
}
