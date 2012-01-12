/*
 * MapLegend.java
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package jams.components.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;

/**
 * Handles variable coloring of map features used in MapCreator class by means of SLD
 *
 * @author C. Schwartze
 */
public class MapLegend extends JPanel {

    public class ColorLegend extends JPanel {

        public String paramname;

        public ColorLegend(String param) {
            this.paramname = param;
        }

        @Override
        public void paintComponent(Graphics g) {
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            g.drawString("Legend", 10, 20);
            g.setFont(new Font("SansSerif", Font.ITALIC, 12));
            g.drawString(paramname, 10, 40);
            drawColorLegend(g);
        }
    }
    private Map m;
    public static Style style;

    private static String rgbToHex(int r, int g, int b) {
        String[] RGB = new String[256];
        int k = 0;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                RGB[k] = hex[i] + hex[j];
                k++;
            }
        }
        return "#" + RGB[r] + RGB[g] + RGB[b];
    }

    private static Style generateSLD(Map cmap) throws Exception {
        String sld = "<UserStyle><FeatureTypeStyle>";
        for (int i = 0; i < cmap.size() - 1; i++) {
            String hexColor = rgbToHex(((Color) cmap.keySet().toArray()[cmap.size() - i - 1]).getRed(),
                    ((Color) cmap.keySet().toArray()[cmap.size() - i - 1]).getGreen(),
                    ((Color) cmap.keySet().toArray()[cmap.size() - i - 1]).getBlue());
            sld += "<rule>" +
                    "<Filter><And>" +
                    "<PropertyIsLessThanOrEqualTo>" +
                    "<PropertyName>newAt</PropertyName>" +
                    "<Literal>" + cmap.get(cmap.keySet().toArray()[i + 1]) + "</Literal>" +
                    "</PropertyIsLessThanOrEqualTo>" +
                    "<PropertyIsGreaterThan>" +
                    "<PropertyName>newAt</PropertyName>" +
                    "<Literal>" + cmap.get(cmap.keySet().toArray()[i]) + "</Literal>" +
                    "</PropertyIsGreaterThan>" +
                    "</And></Filter>" +
                    "<PolygonSymbolizer>" +
                    "<Fill>" +
                    "<CssParameter name='fill'>" + hexColor + "</CssParameter>" +
                    "</Fill>" +
                    "<Stroke>" +
                    "<CssParameter name='stroke'>#000000</CssParameter>" +
                    "<CssParameter name='stroke-width'>1</CssParameter>" +
                    "</Stroke>" +
                    "</PolygonSymbolizer>" +
                    "</rule>";
        }
        sld += "</FeatureTypeStyle></UserStyle>";
        ByteArrayInputStream stringInput = new ByteArrayInputStream(sld.getBytes());
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
        SLDParser parse = new SLDParser(styleFactory, stringInput);
        return parse.readXML()[0];
    }

    public static Map coloring(Set<Double> d, int numRanges, String rangeColor) throws Exception {
        Double val = Math.rint(new Double(510.0 / (d.size() - 1)) * 1000) / 1000.;
        Map<Integer, Double> colmap = new TreeMap<Integer, Double>();
        Object[] li = d.toArray();
        colmap.put(1, (Double) li[0]);
        colmap.put(510, (Double) li[li.length - 1]);
        if (val > 1.0) {
            int col = 1;
            for (int v = 2; v <= 509; v++) {
                if (v < col * val) {
                    colmap.put(v, (Double) li[col - 1]);
                } else {
                    colmap.put(v, (Double) li[col]);
                    col++;
                }
            }
        } else {
            int col = 2;
            for (int v = 2/*1*/; v <= li.length - 1; v++) {
                if (Math.floor(0.0 + (new Double(v) * val)) == col) {
                    colmap.put(col, (Double) li[v]);
                    col++;
                }
            }
        }

        if (numRanges > d.size()) {
            numRanges = d.size();
        }
        int range = new Double(Math.round(colmap.size() / (numRanges))).intValue();
        Map<Color, Double> colors = new LinkedHashMap<Color, Double>();
        colors.put(new Color(0, 0, 0), Double.NEGATIVE_INFINITY);
        if (rangeColor.equalsIgnoreCase("red")) {
            int rangecounts = 1;
            for (int i = range; i <= 510; i += range) {
                if (rangecounts == numRanges) {
                    colors.put(new Color(255, i - 255, i - 255), (Double) colmap.get(510));
                } else {
                    if (i <= 255) {
                        colors.put(new Color(i, 0, 0), (Double) colmap.get(i));
                    } else {
                        colors.put(new Color(255, i - 255, i - 255), (Double) colmap.get(i));
                    }
                }
                rangecounts++;
            }
        } else if (rangeColor.equalsIgnoreCase("green")) {
            int rangecounts = 1;
            for (int i = range; i <= 510; i += range) {
                if (rangecounts == numRanges) {
                    colors.put(new Color(i - 255, 255, i - 255), (Double) colmap.get(510));
                } else {
                    if (i <= 255) {
                        colors.put(new Color(0, i, 0), (Double) colmap.get(i));
                    } else {
                        colors.put(new Color(i - 255, 255, i - 255), (Double) colmap.get(i));
                    }
                }
                rangecounts++;
            }
        } else if (rangeColor.equalsIgnoreCase("blue")) {
            int rangecounts = 1;
            for (int i = range; i <= 510; i += range) {
                if (rangecounts == numRanges) {
                    colors.put(new Color(i - 255, i - 255, 255), (Double) colmap.get(510));
                } else {
                    if (i <= 255) {
                        colors.put(new Color(0, 0, i), (Double) colmap.get(i));
                    } else {
                        colors.put(new Color(i - 255, i - 255, 255), (Double) colmap.get(i));
                    }
                }
                rangecounts++;
            }
        } else {
            System.out.println("Not supported color for rangeColor");
        }
        style = generateSLD(colors);
        return colors;
    }

    private void drawColorLegend(Graphics g) {
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (int i = 1; i <= m.size() - 1; i++) {
            g.setColor((Color) m.keySet().toArray()[i]);
            g.fillRect(10, 30 + (20 * i), 35, 15);
            g.setColor(Color.BLACK);
            g.drawRect(9, 30 + (20 * i) - 1, 35, 15);
            g.drawString("<= " + (m.get(m.keySet().toArray()[i])).toString(), 55, 42 + (20 * i));
        }
    }
}
