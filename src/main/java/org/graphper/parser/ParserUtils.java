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
package org.graphper.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.Stream;
import org.apache_gs.commons.lang3.ArrayUtils;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Cluster;
import org.graphper.api.Graphviz;
import org.graphper.api.Html.Attrs;
import org.graphper.api.Html.Table;
import org.graphper.api.Html.Td;
import org.graphper.api.Line;
import org.graphper.api.Line.LineBuilder;
import org.graphper.api.Node;
import org.graphper.api.Node.NodeBuilder;
import org.graphper.api.Subgraph;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.ClusterStyle;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.LineStyle;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.NodeStyle;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Rank;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Splines;
import org.graphper.draw.svg.SvgConstants;
import org.graphper.parser.grammar.DOTParser;

public class ParserUtils {

    private ParserUtils() {
    }

    public static Map<String, String> getAttrMap(DOTParser.Attr_listContext attr_list) {
        if (attr_list == null) {
            return Collections.emptyMap();
        }

        Map<String, String> attrMap = new HashMap<>(attr_list.getChildCount());
        for (DOTParser.A_listContext al : attr_list.a_list()) {
            parseAttrs(al, attrMap::put);
        }
        return attrMap;
    }

    private static void parseAttrs(DOTParser.A_listContext a_list, BiConsumer<String, String> pairConsumer) {
        if (a_list == null) {
            return;
        }
        // Iterate through the id_ and value lists to extract key-value pairs
        for (int i = 0; i < a_list.id_().size(); i+=2) {
            // Get the key (id_)
            String key = parseId(a_list.id_(i), false);

            if (i == a_list.id_().size() - 1) {
                continue;
            }

            // Get the value associated with the key
            String value = parseId(a_list.id_(i + 1), isLabel(key));

            pairConsumer.accept(key, value);
        }
    }

    private static String parseId(DOTParser.Id_Context idCtx, boolean isLabel) {
        // Depending on the type of the ID, return the appropriate text
        if (idCtx.ID() != null) {
            return idCtx.ID().getText(); // For standard IDs like 'label'
        } else if (idCtx.STRING() != null) {
            if (isLabel) {
                return "\"" + idCtx.STRING().getText() + "\"";
            }
            return idCtx.STRING().getText(); // For string values
        } else if (idCtx.NUMBER() != null) {
            return idCtx.NUMBER().getText(); // For numeric values
        } else if (idCtx.HTML_STRING() != null) {
            return idCtx.HTML_STRING().getText();
        }
        return "";
    }

    public static void subgraphAttributes(DOTParser.Attr_listContext attr_list, Subgraph.SubgraphBuilder l) {
        Map<String, String> attrMap = getAttrMap(attr_list);
        attrMap.forEach((key, value) -> subgraphAttribute(key, value, l));
    }

    public static void subgraphAttributes(DOTParser.A_listContext a_list, Subgraph.SubgraphBuilder l) {
        parseAttrs(a_list, (key, value) -> subgraphAttribute(key, value, l));
    }

    public static void subgraphAttribute(String key, String value, Subgraph.SubgraphBuilder sb) {
        switch (key.toLowerCase()) {
            case "rank":
                setEnum(sb::rank, Rank.class, value.toUpperCase());
                break;
            default:
                break;
        }
    }

    public static void clusterAttributes(DOTParser.Attr_listContext attr_list, Cluster.ClusterBuilder l) {
        Map<String, String> attrMap = getAttrMap(attr_list);
        attrMap.forEach((key, value) -> clusterAttribute(key, value, l));
    }

    public static void clusterAttributes(DOTParser.A_listContext a_list, Cluster.ClusterBuilder l) {
        parseAttrs(a_list, (key, value) -> clusterAttribute(key, value, l));
    }

    public static void graphAttributes(DOTParser.Attr_listContext attr_list, Graphviz.GraphvizBuilder gb) {
        Map<String, String> attrMap = getAttrMap(attr_list);
        attrMap.forEach((key, value) -> graphAttribute(key, value, gb));
    }

    public static void graphAttributes(DOTParser.A_listContext a_list,Graphviz.GraphvizBuilder gb) {
        parseAttrs(a_list, (key, value) -> graphAttribute(key, value, gb));
    }

    public static void graphAttribute(String key, String value, Graphviz.GraphvizBuilder gb) {
        switch (key.toLowerCase()) {
            case "layout":
                setEnum(gb::layout, Layout.class, value);
                break;
            case "rankdir":
                setEnum(gb::rankdir, Rankdir.class, value);
                break;
            case "splines":
                setEnum(gb::splines, Splines.class, value);
                break;
            case "bgcolor":
                gb.bgColor(colorOf(value));
                break;
            case "fontcolor":
                gb.fontColor(colorOf(value));
                break;
            case "fontname":
                gb.fontName(value);
                break;
            case "fontsize":
                setDouble(gb::fontSize, value);
                break;
            case "href":
                gb.href(value);
                break;
            case "label":
                labelHandle(gb::label, gb::table, value);
                break;
            case "labeljust":
                setEnum(gb::labeljust, Labeljust.class, value.toUpperCase());
                break;
            case "labelloc":
                setEnum(gb::labelloc, Labelloc.class, value.toUpperCase());
                break;
            case "tooltip":
                gb.tooltip(value);
                break;
            case "url":
                gb.href(value);
                break;
            case "size":
                Double[] size = arrayConvert(value, Double::parseDouble, Double.class);
                if (!ArrayUtils.isEmpty(size)) {
                    if (size.length == 1) {
                        gb.scale(size[0]);
                    } else {
                        gb.scale(size[0], size[1]);
                    }
                }
                break;
            case "margin":
                Double[] margin = arrayConvert(value, Double::parseDouble, Double.class);
                if (!ArrayUtils.isEmpty(margin)) {
                    if (margin.length == 1) {
                        gb.margin(margin[0]);
                    } else {
                        gb.margin(margin[0], margin[1]);
                    }
                }
                break;
            default:
                break;
        }
    }

    public static void clusterAttribute(String key, String value, Cluster.ClusterBuilder sb) {
        switch (key.toLowerCase()) {
            case "bgcolor":
                sb.bgColor(colorOf(value));
                break;
            case "color":
                sb.color(colorOf(value));
                break;
            case "fontcolor":
                sb.fontColor(colorOf(value));
                break;
            case "fontname":
                sb.fontName(value);
                break;
            case "fontsize":
                setDouble(sb::fontSize, value);
                break;
            case "href":
                sb.href(value);
                break;
            case "label":
                labelHandle(sb::label, sb::table, value);
                break;
            case "labeljust":
                setEnum(sb::labeljust, Labeljust.class, value.toUpperCase());
                break;
            case "labelloc":
                setEnum(sb::labelloc, Labelloc.class, value.toUpperCase());
                break;
            case "margin":
                Double[] margin = arrayConvert(value, Double::parseDouble, Double.class);
                if (!ArrayUtils.isEmpty(margin)) {
                    if (margin.length == 1) {
                        sb.margin(margin[0]);
                    } else {
                        sb.margin(margin[0], margin[1]);
                    }
                }
                break;
            case "penwidth":
                setDouble(sb::penWidth, value);
                break;
            case "style":
                ClusterStyle[] clusterStyles = arrayConvert(value.toUpperCase(),
                                                            ClusterStyle::valueOf,
                                                            ClusterStyle.class);
                if (clusterStyles != null) {
                    sb.style(clusterStyles);
                }
                break;
            case "tooltip":
                sb.tooltip(value);
                break;
            case "url":
                sb.href(value);
                break;
            default:
                break;
        }
    }

    public static void nodeAttributes(DOTParser.Attr_listContext attr_list, Node.NodeBuilder l) {
        Map<String, String> attrMap = getAttrMap(attr_list);
        nodeAttributes(l, attrMap);
    }

    public static void nodeAttributes(NodeBuilder nodeBuilder, Map<String, String> attrMap) {
        if (nodeBuilder == null || attrMap == null) {
            return;
        }

        attrMap.forEach((key, value) -> {
            switch (key.toLowerCase()) {
                case "color":
                    nodeBuilder.color(colorOf(value));
                    break;
                case "fillcolor":
                    nodeBuilder.fillColor(colorOf(value));
                    break;
                case "fixedsize":
                    setBoolean(nodeBuilder::fixedSize, value);
                    break;
                case "fontcolor":
                    nodeBuilder.fontColor(colorOf(value));
                    break;
                case "fontname":
                    nodeBuilder.fontName(value);
                    break;
                case "fontsize":
                    setDouble(nodeBuilder::fontSize, value);
                    break;
                case "height":
                    setDouble(nodeBuilder::height, value);
                    break;
                case "href":
                    nodeBuilder.href(value);
                    break;
                case "image":
                    nodeBuilder.image(value);
                    break;
                case "label":
                    labelHandle(nodeBuilder::label, nodeBuilder::table, value);
                    break;
                case "labelloc":
                    setEnum(nodeBuilder::labelloc, Labelloc.class, value.toUpperCase());
                    break;
                case "margin":
                    Double[] margin = arrayConvert(value, Double::parseDouble, Double.class);
                    if (!ArrayUtils.isEmpty(margin)) {
                        if (margin.length == 1) {
                            nodeBuilder.margin(margin[0]);
                        } else {
                            nodeBuilder.margin(margin[0], margin[1]);
                        }
                    }
                    break;
                case "penwidth":
                    setDouble(nodeBuilder::penWidth, value);
                    break;
                case "shape":
                    setEnum(nodeBuilder::shape, NodeShapeEnum.class, value.toUpperCase());
                    break;
                case "sides":
                    setInteger(nodeBuilder::sides, value.toUpperCase());
                    break;
                case "style":
                    setNodeStyle(nodeBuilder::style, value);
                    break;
                case "tooltip":
                    nodeBuilder.tooltip(value);
                    break;
                case "url":
                    nodeBuilder.href(value);
                    break;
                case "width":
                    setDouble(nodeBuilder::width, value);
                    break;
                default:
                    break;
            }
        });

        String imageHeight = attrMap.get("imageHeight");
        String imageWidth = attrMap.get("imageWidth");
        if (imageHeight != null && imageWidth != null) {
            try {
                nodeBuilder.imageSize(Double.valueOf(imageHeight), Double.valueOf(imageWidth));
            } catch (NumberFormatException e) {
            }
        }
    }

    public static void lineAttributes(Map<String, String> attrMap, Line.LineBuilder builder) {
        if (attrMap == null || builder == null) {
            return;
        }

        attrMap.forEach((key, value) -> {
            switch (key.toLowerCase()) {
                case "arrowhead":
                    setEnum(builder::arrowHead, ArrowShape.class, value.toUpperCase());
                    break;
                case "arrowsize":
                    setDouble(builder::arrowSize, value);
                    break;
                case "arrowtail":
                    setEnum(builder::arrowTail, ArrowShape.class, value.toUpperCase());
                    break;
                case "color":
                    builder.color(colorOf(value));
                    break;
                case "dir":
                    setEnum(builder::dir, Dir.class, value.toUpperCase());
                    break;
                case "fontcolor":
                    builder.fontColor(colorOf(value));
                    break;
                case "fontname":
                    builder.fontName(value);
                    break;
                case "fontsize":
                    setDouble(builder::fontSize, value);
                    break;
                case "headclip":
                    setBoolean(builder::headclip, value);
                    break;
                case "headcell":
                    builder.headCell(value);
                    break;
                case "headport":
                    builder.headPort(Port.valueOfCode(value.toLowerCase()));
                    break;
                case "href":
                    builder.href(value);
                    break;
                case "label":
                    labelHandle(builder::label, builder::table, value);
                    break;
                case "lhead":
                    builder.lhead(value);
                    break;
                case "ltail":
                    builder.ltail(value);
                    break;
                case "minlen":
                    setInteger(builder::minlen, value);
                    break;
                case "penwidth":
                    setDouble(builder::penWidth, value);
                    break;
                case "showboxes":
                    setBoolean(builder::showboxes, value);
                    break;
                case "style":
                    LineStyle[] lineStyles = arrayConvert(value.toUpperCase(),
                                                          LineStyle::valueOf, LineStyle.class);
                    if (lineStyles != null) {
                        builder.style(lineStyles);
                    }
                    break;
                case "tailclip":
                    setBoolean(builder::tailclip, value);
                    break;
                case "tailport":
                    builder.tailPort(Port.valueOfCode(value.toLowerCase()));
                    break;
                case "tailcell":
                    builder.tailCell(value);
                    break;
                case "tooltip":
                    builder.tooltip(value);
                    break;
                case "url":
                    builder.href(value);
                    break;
                case "weight":
                    setDouble(builder::weight, value);
                    break;
                default:
                    break;
            }
        });
    }

    public static void setLinePort(LineBuilder lineBuilder, String p1, String p2, boolean isTail) {
        if (StringUtils.isEmpty(p1) && StringUtils.isEmpty(p2)) {
            return;
        }

        if (StringUtils.isEmpty(p2)) {
            Port port = Port.valueOfCode(p1);
            if (isTail) {
                if (port == null) {
                    lineBuilder.tailCell(p1);
                } else {
                    lineBuilder.tailPort(port);
                }
            } else {
                if (port == null) {
                    lineBuilder.headCell(p1);
                } else {
                    lineBuilder.headPort(port);
                }
            }
            return;
        }

        if (isTail) {
            lineBuilder.tailCell(p1);
            lineBuilder.tailPort(Port.valueOfCode(p2));
        } else {
            lineBuilder.headCell(p1);
            lineBuilder.headPort(Port.valueOfCode(p2));
        }
    }

    public static void setTableAttributes(Table table, String key, String value) {
        if (table == null || StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return;
        }

        setCommonAttributes(table, key, value);

        switch (key.toLowerCase()) {
            case "cellborder":
                setInteger(table::cellBorder, value);
                break;
            case "cellspacing":
                setInteger(table::cellSpacing, value);
                break;
            case "border":
                setInteger(table::border, value);
                break;
            default:
                break;
        }
    }
    public static void setTdAttributes(Td td, String key, String value) {
        if (td == null || StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return;
        }

        setCommonAttributes(td, key, value);

        switch (key.toLowerCase()) {
            case "border":
                setInteger(td::border, value);
                break;
            case "rowspan":
                setInteger(td::rowSpan, value);
                break;
            case "colspan":
                setInteger(td::colSpan, value);
                break;
            case "text":
                td.text(value);
                break;
            case "fontcolor":
                td.fontColor(colorOf(value));
                break;
            case "fontname":
                td.fontName(value);
                break;
            case "fontsize":
                setInteger(td::fontSize, value);
                break;
            case "shape":
                setEnum(td::shape, NodeShapeEnum.class, value);
                break;
            default:
                break;
        }
    }

    private static void setCommonAttributes(Attrs attrs, String key, String value) {
        if (attrs == null || StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return;
        }

        switch (key.toLowerCase()) {
            case "id":
                attrs.id(value);
                break;
            case "align":
                setEnum(attrs::align, Labeljust.class, value);
                break;
            case "valign":
                setEnum(attrs::valign, Labelloc.class, value);
                break;
            case "bgcolor":
                attrs.bgColor(colorOf(value));
                break;
            case "cellpadding":
                setInteger(attrs::cellPadding, value);
                break;
            case "color":
                attrs.color(colorOf(value));
                break;
            case "fixedsize":
                setBoolean(attrs::fixedSize, value);
                break;
            case "width":
                setInteger(attrs::width, value);
                break;
            case "height":
                setInteger(attrs::height, value);
                break;
            case "href":
                attrs.href(value);
                break;
            case "tooltip":
                attrs.tooltip(value);
                break;
            case "style":
                setNodeStyle(attrs::style, value);
                break;
            default:
                break;
        }
    }


    private static Color colorOf(String color) {
        switch (color.toLowerCase()) {
            case "black":
                return Color.BLACK;
            case "white":
                return Color.WHITE;
            case "red":
                return Color.RED;
            case "orange":
                return Color.ORANGE;
            case "yellow":
                return Color.YELLOW;
            case "green":
                return Color.GREEN;
            case "blue":
                return Color.BLUE;
            case "indigo":
                return Color.INDIGO;
            case "purple":
                return Color.PURPLE;
            case "gold":
                return Color.GOLD;
            case "grey":
                return Color.GREY;
            case "bisque":
                return Color.BISQUE;
            case "lightgrey":
                return Color.LIGHT_GREY;
            case "lightblue":
                return Color.LIGHT_BLUE;
            case "chartreuse":
                return Color.CHARTREUSE;
            default:
                try {
                    return Color.ofRGB(color);
                } catch (Exception e) {
                    return null;
                }
        }
    }

    private static void setDouble(DoubleConsumer doubleConsumer, String val) {
        try {
            doubleConsumer.accept(Double.parseDouble(val));
        } catch (NumberFormatException ex) {
        }
    }

    private static void setInteger(IntConsumer intConsumer, String val) {
        try {
            intConsumer.accept(Integer.parseInt(val));
        } catch (NumberFormatException ex) {
        }
    }

    private static void setBoolean(Consumer<Boolean> boolConsumer, String val) {
        try {
            boolConsumer.accept(Boolean.parseBoolean(val.toLowerCase()));
        } catch (NumberFormatException ex) {
        }
    }

    private static <T extends Enum<T>> void setEnum(Consumer<T> consumer, Class<T> enumClass, String name) {
        for (T enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.name().equalsIgnoreCase(name)) {
                consumer.accept(enumConstant);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] arrayConvert(String val, Function<String, T> eleMapFunc, Class<T> clazz) {
        if (StringUtils.isEmpty(val)) {
            return null;
        }

        try {
            // Use the class type to create an array of the correct type.
            return Stream.of(val.split(SvgConstants.COMMA))
                .map(eleMapFunc)
                .toArray(size -> (T[]) java.lang.reflect.Array.newInstance(clazz, size));
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isLabel(String key) {
        key = key.trim();
        return "label".equalsIgnoreCase(key) || "taillabel".equalsIgnoreCase(key)
            || "headlabel".equalsIgnoreCase(key);
    }

    private static void labelHandle(Consumer<String> labelConsumer,
                                    Consumer<Table> tableConsumer, String label) {
        Table table = TableParser.parse(label);
        if (table != null) {
            tableConsumer.accept(table);
        } else {
            if (label.startsWith("\"") && label.endsWith("\"")) {
                label = label.substring(1, label.length() - 1);
            }
            labelConsumer.accept(label);
        }
    }

    private static void setNodeStyle(Consumer<NodeStyle[]> styleConsumer, String style) {
        NodeStyle[] nodeStyles = arrayConvert(style.toUpperCase(),
                                              NodeStyle::valueOf,
                                              NodeStyle.class);
        if (nodeStyles != null) {
            styleConsumer.accept(nodeStyles);
        }
    }
}
