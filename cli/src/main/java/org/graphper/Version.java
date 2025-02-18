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

package org.graphper;

import java.util.Properties;

/**
 * Utility class for retrieving version information from the application's dependencies.
 *
 * @author Jamison Jiang
 */
public class Version {

  private Version() {
  }

  public static String getVersionFromPom() {
    try {
      Properties properties = new Properties();
      properties.load(Main.class.getResourceAsStream(
          "/META-INF/maven/org.graphper/graph-support-cli/pom.properties"));
      return properties.getProperty("version", "Unknown");
    } catch (Exception e) {
      return "Unknown";
    }
  }

  public static String getAntlrVersion() {
    try {
      Properties properties = new Properties();
      properties.load(Main.class.getResourceAsStream(
          "/META-INF/maven/org.antlr/antlr4-runtime/pom.properties"));
      return properties.getProperty("version", "Unknown");
    } catch (Exception e) {
      return "Unknown";
    }
  }

  public static String getBatikVersion() {
    try {
      Properties properties = new Properties();
      properties.load(Main.class.getResourceAsStream(
          "/META-INF/maven/org.apache.xmlgraphics/batik-transcoder/pom.properties"));
      return properties.getProperty("version", "Unknown");
    } catch (Exception e) {
      return "Unknown";
    }
  }

  public static String getFopVersion() {
    try {
      Properties properties = new Properties();
      properties.load(Main.class.getResourceAsStream(
          "/META-INF/maven/org.apache.xmlgraphics/fop-core/pom.properties"));
      return properties.getProperty("version", "Unknown");
    } catch (Exception e) {
      return "Unknown";
    }
  }
}
