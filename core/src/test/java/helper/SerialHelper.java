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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.function.Consumer;

public class SerialHelper {

  public static <T> void testSerial(String path, T obj, Consumer<T> afterDeserial)
      throws IOException, ClassNotFoundException {
    File file = new File(path);
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }

    try (OutputStream os = Files.newOutputStream(file.toPath());
        ObjectOutputStream oos = new ObjectOutputStream(os)) {
      oos.writeObject(obj);

      readObj(afterDeserial, file);
    }
  }

  public  static <T> void readObj(Consumer<T> afterDeserial, File file)
      throws IOException, ClassNotFoundException {
    try (InputStream is = Files.newInputStream(file.toPath());
        ObjectInputStream ois = new ObjectInputStream(is)) {
      T o = (T) ois.readObject();
      afterDeserial.accept(o);
    }
  }
}
