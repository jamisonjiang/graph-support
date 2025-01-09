package org.graphper.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.Stream;
import org.apache_gs.commons.lang3.ArrayUtils;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Cluster;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Line.LineBuilder;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.ClusterStyle;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.LineStyle;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.NodeStyle;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Rank;
import org.graphper.draw.svg.SvgConstants;
import org.graphper.parser.grammar.DOTParser;

public class ParserUtils {

    public static Map<String, String> getAttrMap(DOTParser.Attr_listContext attr_list) {
        if (attr_list == null) {
            return Collections.emptyMap();
        }

        Map<String, String> attrMap = new HashMap<>();
        for (DOTParser.A_listContext al : attr_list.a_list()) {
            int amount = al.id_().size() / 2;
            for (int c = 0; c < amount; c++) {
                String left = al.id_().get(2 * c).getText();
                String right = al.id_().get(2 * c + 1).getText();
                attrMap.put(left, right);
            }
        }
        return attrMap;
    }

    public static Map<String, String> getAttrMap(DOTParser.A_listContext a_list) {
        if (a_list == null) {
            return Collections.emptyMap();
        }

        Map<String, String> attrMap = new HashMap<>();
        int acount = a_list.id_().size() / 2;
        for (int c = 0; c < acount; c++) {
            String left = a_list.id_().get(2 * c).getText();
            String right = a_list.id_().get(2 * c + 1).getText();
            attrMap.put(left, right);
        }

        return attrMap;
    }

    public static void subgraphAttributes(DOTParser.Attr_listContext attr_list, Subgraph.SubgraphBuilder l) {
        Map<String, String> attrMap = getAttrMap(attr_list);
        attrMap.forEach((key, value) -> subgraphAttribute(key, value, l));
    }

    public static void subgraphAttributes(DOTParser.A_listContext a_list, Subgraph.SubgraphBuilder l) {
        Map<String, String> attrMap = getAttrMap(a_list);
        attrMap.forEach((key, value) -> subgraphAttribute(key, value, l));
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
        Map<String, String> attrMap = getAttrMap(a_list);
        attrMap.forEach((key, value) -> clusterAttribute(key, value, l));
    }

    public static void graphAttributes(DOTParser.Attr_listContext attr_list, Graphviz.GraphvizBuilder gb) {
        Map<String, String> attrMap = getAttrMap(attr_list);
        attrMap.forEach((key, value) -> graphAttribute(key, value, gb));
    }

    public static void graphAttributes(DOTParser.A_listContext a_list,Graphviz.GraphvizBuilder gb) {
        Map<String, String> attrMap = getAttrMap(a_list);
        attrMap.forEach((key, value) -> graphAttribute(key, value, gb));
    }

    public static void graphAttribute(String key, String value, Graphviz.GraphvizBuilder gb) {
        switch (key.toLowerCase()) {
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
                gb.label(value);
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
                sb.label(value);
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

        attrMap.entrySet().forEach(e -> {
            switch (e.getKey().toLowerCase()) {
                case "color":
                    l.color(colorOf(e.getValue()));
                    break;
                case "fillcolor":
                    l.fillColor(colorOf(e.getValue()));
                    break;
                case "fixedsize":
                    setBoolean(l::fixedSize, e.getValue());
                    break;
                case "fontcolor":
                    l.fontColor(colorOf(e.getValue()));
                    break;
                case "fontname":
                    l.fontName(e.getValue());
                    break;
                case "fontsize":
                    setDouble(l::fontSize, e.getValue());
                    break;
                case "height":
                    setDouble(l::height, e.getValue());
                    break;
                case "href":
                    l.href(e.getValue());
                    break;
                case "image":
                    l.image(e.getValue());
                    break;
                case "label":
                    l.label(e.getValue());
                    break;
                case "labelloc":
                    setEnum(l::labelloc, Labelloc.class, e.getValue().toUpperCase());
                    break;
                case "margin":
                    Double[] margin = arrayConvert(e.getValue(), Double::parseDouble, Double.class);
                    if (!ArrayUtils.isEmpty(margin)) {
                        if (margin.length == 1) {
                            l.margin(margin[0]);
                        } else {
                            l.margin(margin[0], margin[1]);
                        }
                    }
                    break;
                case "penwidth":
                    setDouble(l::penWidth, e.getValue());
                    break;
                case "shape":
                    setEnum(l::shape, NodeShapeEnum.class, e.getValue().toUpperCase());
                    break;
                case "sides":
                    setInteger(l::sides, e.getValue().toUpperCase());
                    break;
                case "style":
                    NodeStyle[] nodeStyles = arrayConvert(e.getValue().toUpperCase(),
                                                          NodeStyle::valueOf, NodeStyle.class);
                    if (nodeStyles != null) {
                        l.style(nodeStyles);
                    }
                    break;
                case "tooltip":
                    l.tooltip(e.getValue());
                    break;
                case "url":
                    l.href(e.getValue());
                    break;
                case "width":
                    setDouble(l::width, e.getValue());
                    break;
                default:
                    break;
            }
        });

        String imageHeight = attrMap.get("imageHeight");
        String imageWidth = attrMap.get("imageWidth");
        if (imageHeight != null && imageWidth != null) {
            try {
                l.imageSize(Double.valueOf(imageHeight), Double.valueOf(imageWidth));
            } catch (NumberFormatException e) {
            }
        }
    }

    public static void lineAttributes(DOTParser.Attr_listContext attr_list, Line.LineBuilder builder) {
        Map<String, String> attrMap = getAttrMap(attr_list);
        attrMap.entrySet().forEach(e -> {
            switch (e.getKey().toLowerCase()) {
                case "arrowhead":
                    setEnum(builder::arrowHead, ArrowShape.class, e.getValue().toUpperCase());
                    break;
                case "arrowsize":
                    setDouble(builder::arrowSize, e.getValue());
                    break;
                case "arrowtail":
                    setEnum(builder::arrowTail, ArrowShape.class, e.getValue().toUpperCase());
                    break;
                case "color":
                    builder.color(colorOf(e.getValue()));
                    break;
                case "dir":
                    setEnum(builder::dir, Dir.class, e.getValue().toUpperCase());
                    break;
                case "fontcolor":
                    builder.fontColor(colorOf(e.getValue()));
                    break;
                case "fontname":
                    builder.fontName(e.getValue());
                    break;
                case "fontsize":
                    setDouble(builder::fontSize, e.getValue());
                    break;
                case "headclip":
                    setBoolean(builder::headclip, e.getValue());
                    break;
                case "href":
                    builder.href(e.getValue());
                    break;
                case "label":
                    builder.label(e.getValue());
                    break;
                case "lhead":
                    builder.lhead(e.getValue());
                    break;
                case "ltail":
                    builder.ltail(e.getValue());
                    break;
                case "minlen":
                    setInteger(builder::minlen, e.getValue());
                    break;
                case "penWidth":
                    setDouble(builder::penWidth, e.getValue());
                    break;
                case "showboxes":
                    setBoolean(builder::showboxes, e.getValue());
                    break;
                case "style":
                    LineStyle[] lineStyles = arrayConvert(e.getValue().toUpperCase(),
                                                          LineStyle::valueOf, LineStyle.class);
                    if (lineStyles != null) {
                        builder.style(lineStyles);
                    }
                    break;
                case "tailclip":
                    setBoolean(builder::tailclip, e.getValue());
                    break;
                case "tailPort":
                    setEnum(builder::tailPort, Port.class, e.getValue().toUpperCase());
                    break;
                case "tooltip":
                    builder.tooltip(e.getValue());
                    break;
                case "url":
                    builder.href(e.getValue());
                    break;
                case "weight":
                    setDouble(builder::weight, e.getValue());
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
            if (enumConstant.name().equals(name)) {
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
}
