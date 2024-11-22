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

package org.graphper.layout;

/**
 * Provides a predefined font order based on global popularity statistics.
 *
 * <p>This implementation uses a static list of the most popular fonts worldwide,
 * ranking them in a fixed sequence. The order is independent of system availability
 * and reflects general usage trends across different regions and applications.</p>
 *
 * @author Jamison Jiang
 */
public class DefaultFontOrder extends StaticFontOrder {

  @Override
  protected String[] fontOrder() {
    return new String[]{
        "Arial",
        "Times New Roman",
        "SansSerif",
        "Calibri",
        "Helvetica",
        "Georgia",
        "Verdana",
        "Comic Sans MS",
        "Trebuchet MS",
        "Courier New",
        "Cambria",
        "Garamond",
        "Palatino",
        "Lucida Sans",
        "Lucida Console",
        "Futura",
        "Franklin Gothic",
        "Myriad",
        "Roboto",
        "Open Sans",
        "Baskerville",
        "Rockwell",
        "Century Gothic",
        "Tahoma",
        "Gill Sans",
        "Bodoni",
        "Copperplate",
        "Eurostile",
        "Museo",
        "Proxima Nova",
        "Lato",
        "Ubuntu",
        "DIN",
        "Arial Narrow",
        "Impact",
        "Book Antiqua",
        "Optima",
        "Segoe UI",
        "Brush Script",
        "Didot",
        "Helvetica Neue",
        "Raleway",
        "Montserrat",
        "Oswald",
        "Avenir",
        "Roboto Condensed",
        "PT Sans",
        "Source Sans Pro",
        "Merriweather",
        "Candara",
        "Courier Prime"
    };
  }
}