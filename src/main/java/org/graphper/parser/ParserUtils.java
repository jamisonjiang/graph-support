package org.graphper.parser;

import org.grapher.parser.grammar.DOTParser;
import org.graphper.api.*;
import org.graphper.api.attributes.*;

import java.util.HashMap;
import java.util.Map;

public class ParserUtils {

    public static String label(String l) {

        if (l.startsWith("\"")) {
            l = l.substring(1);
        }
        if (l.endsWith("\"")) {
            l = l.substring(0, l.length() - 1);
        }

        return l;
    }

    public static  Map<String, String> getAttrMap(DOTParser.Attr_listContext attr_list) {
        Map<String, String> attrMap = new HashMap<>();

        if (attr_list != null) {

            for (DOTParser.A_listContext al : attr_list.a_list()) {

                int acount = al.id_().size() / 2;
                for (int c = 0; c < acount; c++) {
                    String left = al.id_().get(2 * c).getText();
                    String right = al.id_().get(2 * c + 1).getText();

                    attrMap.put(left, right);
                }
            }
        }
        return attrMap;
    }

    public static void subgraphAttributes(DOTParser.Attr_listContext attr_list, Subgraph.SubgraphBuilder l) {

        Map<String, String> attrMap = getAttrMap(attr_list);
        attrMap.entrySet().forEach(e -> {

            subgraphAttribute(e.getKey(), e.getValue(), l);
        });
    }

    public static void subgraphAttribute(String key, String value, Subgraph.SubgraphBuilder sb) {

        switch (key.toLowerCase()) {
            case "rank": sb.rank(Rank.valueOf(value.toUpperCase())); break;
        }
    }

    public static void clusterAttributes(DOTParser.Attr_listContext attr_list, Cluster.ClusterBuilder l) {

        Map<String, String> attrMap = getAttrMap(attr_list);
        attrMap.entrySet().forEach(e -> {

            clusterAttribute(e.getKey(), e.getValue(), l);
        });
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
                sb.fontSize(Double.valueOf(value));
                break;
            case "href":
                sb.href(value);
                break;
            case "label":
                sb.label(label(value));
                break;
            case "labeljust":
                sb.labeljust(Labeljust.valueOf(value.toUpperCase()));
                break;
            case "labelloc":
                sb.labelloc(Labelloc.valueOf(value.toUpperCase()));
                break;
            case "margin":
                sb.margin(Double.valueOf(value));
                break;
            case "penwidth":
                sb.penWidth(Double.valueOf(value));
                break;
            case "style":
                sb.style(ClusterStyle.valueOf(value.toUpperCase()));
                break;
            case "tooltip":
                sb.tooltip(value);
                break;
            case "url":
                sb.href(value);
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
                    l.fixedSize(Boolean.valueOf(e.getValue()));
                    break;
                case "fontcolor":
                    l.fontColor(colorOf(e.getValue()));
                    break;
                case "fontname":
                    l.fontName(e.getValue());
                    break;
                case "fontsize":
                    l.fontSize(Double.valueOf(e.getValue()));
                    break;
                case "height":
                    l.height(Double.valueOf(e.getValue()));
                    break;
                case "href":
                    l.href(e.getValue());
                    break;
                case "image":
                    l.image(e.getValue());
                    break;
                case "label":
                    l.label(label(e.getValue()));
                    break;
                case "labelloc":
                    l.labelloc(Labelloc.valueOf(e.getValue().toUpperCase()));
                    break;
                case "margin":
                    l.margin(Double.valueOf(e.getValue()));
                    break;
                case "penwidth":
                    l.penWidth(Double.valueOf(e.getValue()));
                    break;
                case "shape":
                    l.shape(NodeShapeEnum.valueOf(e.getValue().toUpperCase()));
                    break;
                case "sides":
                    l.sides(Integer.valueOf(e.getValue().toUpperCase()));
                    break;
                case "style":
                    l.style(NodeStyle.valueOf(e.getValue().toUpperCase()));
                    break;
                case "tooltip":
                    l.tooltip(e.getValue());
                    break;
                case "url":
                    l.href(e.getValue());
                    break;
                case "width":
                    l.width(Double.valueOf(e.getValue()));
                    break;
                default: break;
            }
        });

        String imageHeight = attrMap.get("imageHeight");
        String imageWidth = attrMap.get("imageWidth");
        if (imageHeight != null && imageWidth != null) {
            l.imageSize(Double.valueOf(imageHeight), Double.valueOf(imageWidth));
        }
    }


    public static void lineAttributes(DOTParser.Attr_listContext attr_list, Line.LineBuilder builder) {

        Map<String, String> attrMap = getAttrMap(attr_list);
        attrMap.entrySet().forEach(e -> {
            switch (e.getKey().toLowerCase()) {
                case "arrowhead": builder.arrowHead(ArrowShape.valueOf(e.getValue().toUpperCase())); break;
                case "arrowsize": builder.arrowSize(Double.valueOf(e.getValue())); break;
                case "arrowtail": builder.arrowTail(ArrowShape.valueOf(e.getValue().toUpperCase())); break;
                case "color": builder.color(colorOf(e.getValue())); break;
                case "dir": builder.dir(Dir.valueOf(e.getValue().toUpperCase())); break;
                case "fontcolor": builder.fontColor(colorOf(e.getValue())); break;
                case "fontname": builder.fontName(e.getValue()); break;
                case "fontsize": builder.fontSize(Double.valueOf(e.getValue())); break;
                case "headclip": builder.headclip(Boolean.valueOf(e.getValue())); break;
                case "href": builder.href(e.getValue()); break;
                case "label": builder.label(ParserUtils.label(e.getValue())); break;
                case "lhead": builder.lhead(e.getValue()); break;
                case "ltail": builder.ltail(e.getValue()); break;
                case "minlen": builder.minlen(Integer.valueOf(e.getValue())); break;
                case "penWidth": builder.penWidth(Double.valueOf(e.getValue())); break;
                case "showboxes": builder.showboxes(Boolean.valueOf(e.getValue())); break;
                case "style": builder.style(LineStyle.valueOf(e.getValue().toUpperCase())); break;
                case "tailclip": builder.tailclip(Boolean.valueOf(e.getValue())); break;
                case "tailPort": builder.tailPort(Port.valueOf(e.getValue().toUpperCase())); break;
                case "tooltip": builder.tooltip(e.getValue()); break;
                case "url": builder.href(e.getValue()); break;
                case "weight": builder.weight(Double.valueOf(e.getValue())); break;
            }
        });
    }

    private static Color colorOf(String color) {

        switch (color.toLowerCase()) {
            case "black": return Color.BLACK;
            case "white": return Color.WHITE;
            case "red": return Color.RED;
            case "orange": return Color.ORANGE;
            case "yellow": return Color.YELLOW;
            case "green": return Color.GREEN;
            case "blue": return Color.BLUE;
            case "indigo": return Color.INDIGO;
            case "purple": return Color.PURPLE;
            case "gold": return Color.GOLD;
            case "grey": return Color.GREY;
            case "pink": return Color.PINK;
            default : return Color.ofRGB(color);
        }
    }
}
