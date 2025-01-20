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

import java.util.Objects;
import javax.xml.transform.TransformerFactory;

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
}
