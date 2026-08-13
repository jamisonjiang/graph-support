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

import java.io.IOException;
import org.graphper.api.FileType;
import org.graphper.api.GraphResource;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.draw.ExecuteException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class RasterOutputTest {

  @ParameterizedTest
  @EnumSource(value = FileType.class, names = {"PNG", "JPG", "JPEG", "TIFF"})
  public void plainGraphRendersWithBatik(FileType fileType) throws IOException, ExecuteException {
    Graphviz graphviz = Graphviz.digraph()
        .addNode(Node.builder().label("plain raster output").build())
        .build();

    try (GraphResource resource = graphviz.toFile(fileType)) {
      assertSignature(fileType, resource.bytes());
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
      default:
        Assertions.fail("Unexpected raster type " + fileType);
    }
  }
}
