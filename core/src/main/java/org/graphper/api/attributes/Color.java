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

package org.graphper.api.attributes;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.def.ColorFormatException;

/**
 * Basic color properties.
 *
 * @author Jamison Jiang
 */
public abstract class Color implements Serializable {

  private static final long serialVersionUID = 2828231898265525591L;

  public static final Color BLACK = Color.ofRGB("#000000");

  public static final Color WHITE = Color.ofRGB("#ffffff");

  public static final Color RED = Color.ofRGB("#ff0000");

  public static final Color ORANGE = Color.ofRGB("#ffa500");

  public static final Color YELLOW = Color.ofRGB("#ffff00");

  public static final Color GREEN = Color.ofRGB("#00ff00");

  public static final Color BLUE = Color.ofRGB("#0000ff");

  public static final Color INDIGO = Color.ofRGB("#4b0082");

  public static final Color PURPLE = Color.ofRGB("#800080");

  public static final Color GOLD = Color.ofRGB("#ffd700");

  public static final Color GREY = Color.ofRGB("#c0c0c0");

  public static final Color PINK = Color.ofRGB("#ffc0cb");

  public static final Color BISQUE = Color.ofRGB("#ffe4c4");

  public static final Color LIGHT_GREY = Color.ofRGB("#d3d3d3");

  public static final Color LIGHT_BLUE = Color.ofRGB("#ffff00");

  public static final Color CHARTREUSE = Color.ofRGB("#7fff00");

  /**
   * Returns a string describing current color.
   *
   * @return string describing current color
   */
  public abstract String value();

  /**
   * Returns a color object based on the passed color string in RGB format, the format description
   * allows spaces.
   *
   * @param rgb a color string in RGB format
   * @return Color object
   * @throws ColorFormatException the format of the color does not conform to RGB
   */
  public static Color ofRGB(String rgb) {
    return new RgbColor(rgb);
  }

  // ------------------------------------------ Subclass ---------------------------------------

  abstract static class AbstractColor extends Color implements Serializable {

    private static final long serialVersionUID = -7757877504759666027L;

    protected final String colorVal;

    protected AbstractColor(String color) {
      this.colorVal = formatNormal(color);
    }

    protected static void emptyColorValid(String color) {
      if (StringUtils.isEmpty(color)) {
        throw new ColorFormatException("color can not be empty");
      }
    }

    @Override
    public String value() {
      return colorVal;
    }

    protected abstract String formatNormal(String color) throws ColorFormatException;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      AbstractColor that = (AbstractColor) o;
      return Objects.equals(colorVal, that.colorVal);
    }

    @Override
    public int hashCode() {
      return Objects.hash(colorVal);
    }
  }

  static class RgbColor extends AbstractColor {

    private static final long serialVersionUID = -1294848178007135771L;

    private static final Pattern pattern = Pattern.compile("^#([0-9a-fA-F]{6}|[0-9a-fA-F]{3})$");

    RgbColor(String color) {
      super(color);
    }

    @Override
    protected String formatNormal(String color) throws ColorFormatException {
      emptyColorValid(color);

      String c = color.trim();
      Matcher matcher = pattern.matcher(color);
      if (!matcher.find()) {
        throw new ColorFormatException("Illegal RGB color param [" + color + "]");
      }

      if (c.length() == 4) {
        c = c + "000";
      }
      return c;
    }

    @Override
    public String toString() {
      return "RgbColor{" +
          "colorVal='" + colorVal + '\'' +
          '}';
    }
  }
}