/*
 * DataflowRenderer.java
 * Created on 19.10.2026
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jamsui.juice.dataflow;

import jams.JAMS;
import jamsui.juice.dataflow.DataflowGraph.Edge;
import jamsui.juice.dataflow.DataflowGraph.Node;
import jamsui.juice.dataflow.DataflowGraph.NodeKind;
import jamsui.juice.dataflow.DataflowGraph.Port;
import jamsui.juice.dataflow.DataflowGraph.PortKind;
import jamsui.juice.dataflow.DataflowLayout.Box;
import jamsui.juice.dataflow.DataflowLayout.Route;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashSet;
import java.util.Set;

/**
 * Paints a {@link DataflowLayout} with Java2D. The same code is used for the
 * screen, PNG export and SVG export (via Batik's SVGGraphics2D).
 *
 * @author Sven Kralisch
 */
public class DataflowRenderer {

    static final Font TITLE = new Font("SansSerif", Font.BOLD, 13);
    static final Font SUB = new Font("SansSerif", Font.PLAIN, 11);
    static final Font PORT = new Font("Monospaced", Font.PLAIN, 12);
    static final Font PORT_BOLD = PORT.deriveFont(Font.BOLD);
    static final Font HEADING = new Font("SansSerif", Font.BOLD, 20);
    static final Font NOTE = new Font("SansSerif", Font.ITALIC, 11);

    static final Color BG = Color.WHITE;
    static final Color CARD = Color.WHITE;
    static final Color LINE = new Color(0xc9cfd8);
    static final Color INK = new Color(0x1f2328);
    static final Color INK2 = new Color(0x3d444d);
    static final Color INK3 = new Color(0x6b7280);
    static final Color PARAM = new Color(0x8a6a2f);
    static final Color TIME = new Color(0x6b7280);
    static final Color UNUSED = new Color(0x9ca3af);
    static final Color CONSTANT = new Color(0x9ca3af);
    static final Color SELECT = new Color(0x1f6fb5);

    static final Color[] PALETTE = {
        new Color(0xc0392b), new Color(0x1f6fb5), new Color(0x2e7d32), new Color(0xc26a00),
        new Color(0x6d4fc2), new Color(0x16867a), new Color(0xad1457), new Color(0x5d6d1e),
        new Color(0x0e7490), new Color(0xb45309), new Color(0x7c3aed), new Color(0x9f1239)
    };

    private final DataflowLayout layout;
    private Node selected;
    private Edge selectedEdge;

    public DataflowRenderer(DataflowLayout layout) {
        this.layout = layout;
    }

    public void setSelected(Node selected) {
        this.selected = selected;
        this.selectedEdge = null;
    }

    /**
     * Selects a single connection: only the edge and its two nodes are
     * highlighted.
     */
    public void setSelectedEdge(Edge edge) {
        this.selectedEdge = edge;
        this.selected = null;
    }

    static double portWidth(String s, FontRenderContext frc) {
        return PORT.getStringBounds(s, frc).getWidth();
    }

    static double titleWidth(String s, FontRenderContext frc) {
        return TITLE.getStringBounds(s, frc).getWidth();
    }

    static double subWidth(String s, FontRenderContext frc) {
        return SUB.getStringBounds(s, frc).getWidth();
    }

    public static Color edgeColor(Edge e) {
        if (e.parameter) {
            return PARAM;
        }
        if (e.time) {
            return TIME;
        }
        return PALETTE[e.from.node.colorIndex % PALETTE.length];
    }

    public void paint(Graphics2D g, String title) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g.setColor(BG);
        g.fillRect(0, 0, (int) Math.ceil(layout.getWidth()), (int) Math.ceil(layout.getHeight()));

        g.setColor(INK);
        g.setFont(HEADING);
        g.drawString(title, DataflowLayout.MARGIN, 40);
        g.setColor(INK3);
        g.setFont(SUB);
        g.drawString(JAMS.i18n("DF_Subtitle"), DataflowLayout.MARGIN, 62);

        Set<Edge> highlighted = new HashSet<>();
        if (selectedEdge != null) {
            highlighted.add(selectedEdge);
        } else if (selected != null) {
            for (Route r : layout.getRoutes()) {
                if (r.edge.from.node == selected || r.edge.to.node == selected) {
                    highlighted.add(r.edge);
                }
            }
        }

        boolean anySelected = selected != null || selectedEdge != null;

        // edges below the boxes, the selected edge on top of the others
        Route top = null;
        for (Route r : layout.getRoutes()) {
            if (r.edge == selectedEdge) {
                top = r;
                continue;
            }
            boolean dim = anySelected && !highlighted.contains(r.edge);
            paintRoute(g, r, dim);
        }
        if (top != null) {
            paintRoute(g, top, false);
        }

        for (Box b : layout.getBoxes()) {
            boolean dim = anySelected && b.node != selected && !touches(b.node, highlighted);
            paintBox(g, b, dim);
        }

        paintLegend(g);
    }

    private static boolean touches(Node n, Set<Edge> edges) {
        for (Edge e : edges) {
            if (e.from.node == n || e.to.node == n) {
                return true;
            }
        }
        return false;
    }

    private void paintRoute(Graphics2D g, Route r, boolean dim) {
        Color c = edgeColor(r.edge);
        if (dim) {
            c = fade(c);
        }
        float width = r.edge == selectedEdge ? 3.2f : (r.edge.parameter ? 1.3f : 1.6f);
        Stroke stroke = r.edge.feedback
                ? new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, new float[]{6f, 4f}, 0f)
                : new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
        g.setStroke(stroke);
        g.setColor(c);
        g.draw(r.path);
        arrow(g, r.end, c);
    }

    private static void arrow(Graphics2D g, Point2D end, Color c) {
        Path2D a = new Path2D.Double();
        double x = end.getX() - 1, y = end.getY();
        a.moveTo(x, y);
        a.lineTo(x - 8, y - 4);
        a.lineTo(x - 8, y + 4);
        a.closePath();
        g.setColor(c);
        g.fill(a);
    }

    private void paintBox(Graphics2D g, Box b, boolean dim) {
        Node n = b.node;
        Color ink = dim ? fade(INK) : INK;
        Color ink2 = dim ? fade(INK2) : INK2;
        Color ink3 = dim ? fade(INK3) : INK3;
        Color line = n == selected ? SELECT : (dim ? fade(LINE) : LINE);

        if (DataflowLayout.isDocument(n)) {
            double fold = 12;
            Path2D doc = new Path2D.Double();
            doc.moveTo(b.x, b.y);
            doc.lineTo(b.x + b.w - fold, b.y);
            doc.lineTo(b.x + b.w, b.y + fold);
            doc.lineTo(b.x + b.w, b.y + b.h);
            doc.lineTo(b.x, b.y + b.h);
            doc.closePath();
            g.setColor(CARD);
            g.fill(doc);
            g.setColor(n.kind == NodeKind.PARAMETERS ? (dim ? fade(PARAM) : PARAM) : ink3);
            g.setStroke(new BasicStroke(n == selected ? 2f : 1.2f));
            g.draw(doc);
            Path2D f = new Path2D.Double();
            f.moveTo(b.x + b.w - fold, b.y);
            f.lineTo(b.x + b.w - fold, b.y + fold);
            f.lineTo(b.x + b.w, b.y + fold);
            g.draw(f);
            g.setColor(ink);
            g.setFont(TITLE);
            g.drawString(n.title, (float) b.x + 10, (float) b.y + 18);
            g.setColor(ink3);
            g.setFont(SUB);
            g.drawString(n.subtitle, (float) b.x + 10, (float) b.y + 33);
            for (Port p : n.outputs) {
                Color c = p.kind == PortKind.PARAMETER ? PARAM : PALETTE[n.colorIndex % PALETTE.length];
                dot(g, layout.portPoint(p), dim ? fade(c) : c);
            }
            return;
        }

        RoundRectangle2D rect = new RoundRectangle2D.Double(b.x, b.y, b.w, b.h, 14, 14);
        g.setColor(CARD);
        g.fill(rect);
        float width = n.kind == NodeKind.OUTPUT ? 2f : 1.2f;
        if (n == selected) {
            width = 2.4f;
        }
        Stroke border = n.kind == NodeKind.CONTEXT
                ? new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, new float[]{4f, 3f}, 0f)
                : new BasicStroke(width);
        g.setStroke(border);
        g.setColor(n.kind == NodeKind.OUTPUT && n != selected ? ink2 : line);
        g.draw(rect);
        g.setStroke(new BasicStroke(1f));
        g.setColor(dim ? fade(LINE) : LINE);
        g.draw(new Line2D.Double(b.x, b.y + DataflowLayout.HEAD, b.x + b.w, b.y + DataflowLayout.HEAD));

        g.setColor(ink);
        g.setFont(TITLE);
        g.drawString(n.title, (float) b.x + 10, (float) b.y + 17);
        g.setColor(ink3);
        g.setFont(SUB);
        g.drawString(n.subtitle, (float) b.x + 10, (float) b.y + 31);

        g.setFont(PORT);
        for (Port p : n.inputs) {
            Point2D pt = layout.portPoint(p);
            Color c = portColor(p, null);
            if (p.kind != PortKind.CONSTANT) {
                dot(g, pt, dim ? fade(c) : c);
            }
            g.setColor(p.kind == PortKind.CONSTANT ? (dim ? fade(CONSTANT) : CONSTANT) : ink2);
            if (isSelectedPort(p)) {
                g.setFont(PORT_BOLD);
                g.setColor(SELECT);
            }
            g.drawString(p.label, (float) b.x + 10, (float) pt.getY() + 4);
            g.setFont(PORT);
        }
        for (Port p : n.outputs) {
            Point2D pt = layout.portPoint(p);
            Color c = portColor(p, n);
            dot(g, pt, dim ? fade(c) : c);
            g.setColor(p.kind == PortKind.UNUSED ? ink3 : ink2);
            if (isSelectedPort(p)) {
                g.setFont(PORT_BOLD);
                g.setColor(SELECT);
            }
            double tw = g.getFontMetrics().getStringBounds(p.label, g).getWidth();
            g.drawString(p.label, (float) (b.x + b.w - 10 - tw), (float) pt.getY() + 4);
            g.setFont(PORT);
            if (p.kind == PortKind.UNUSED) {
                g.setColor(dim ? fade(UNUSED) : UNUSED);
                g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, new float[]{3f, 3f}, 0f));
                g.draw(new Line2D.Double(pt.getX(), pt.getY(), pt.getX() + 18, pt.getY()));
                g.setFont(NOTE);
                g.setColor(ink3);
                g.drawString(JAMS.i18n("DF_unused"), (float) pt.getX() + 22, (float) pt.getY() + 4);
                g.setFont(PORT);
            }
        }
        int row = n.outputs.size();
        for (String s : n.states) {
            double y = layout.rowY(b, row++);
            String label = "↻ " + s;
            double tw = g.getFontMetrics().getStringBounds(label, g).getWidth();
            g.setColor(ink3);
            g.drawString(label, (float) (b.x + b.w - 10 - tw), (float) y + 4);
        }
    }

    private boolean isSelectedPort(Port p) {
        return selectedEdge != null && (selectedEdge.from == p || selectedEdge.to == p);
    }

    private static Color portColor(Port p, Node owner) {
        if (p.time) {
            return TIME;
        }
        switch (p.kind) {
            case PARAMETER:
                return PARAM;
            case UNUSED:
                return UNUSED;
            case CONSTANT:
                return CONSTANT;
            default:
                if (owner != null) {
                    return PALETTE[owner.colorIndex % PALETTE.length];
                }
                return INK3;
        }
    }

    private static void dot(Graphics2D g, Point2D p, Color c) {
        g.setColor(c);
        g.fill(new Ellipse2D.Double(p.getX() - 4, p.getY() - 4, 8, 8));
    }

    private void paintLegend(Graphics2D g) {
        double y = layout.getHeight() - 30;
        double x = DataflowLayout.MARGIN;
        g.setFont(TITLE);
        g.setColor(INK);
        g.drawString(JAMS.i18n("DF_Legend"), (float) x, (float) y);
        x += 80;
        g.setFont(SUB);
        x = legendItem(g, x, y, PALETTE[0], null, JAMS.i18n("DF_Legend_Data"));
        x = legendItem(g, x, y, PALETTE[1], new float[]{6f, 4f}, JAMS.i18n("DF_Legend_Feedback"));
        x = legendItem(g, x, y, TIME, null, JAMS.i18n("DF_Legend_Time"));
        x = legendItem(g, x, y, PARAM, null, JAMS.i18n("DF_Legend_Parameters"));
        x = legendItem(g, x, y, UNUSED, new float[]{3f, 3f}, JAMS.i18n("DF_Legend_Unused"));
        g.setColor(INK3);
        g.drawString("↻ " + JAMS.i18n("DF_Legend_State"), (float) x + 10, (float) y);
    }

    private static double legendItem(Graphics2D g, double x, double y, Color c, float[] dash, String label) {
        g.setColor(c);
        g.setStroke(dash == null ? new BasicStroke(1.8f)
                : new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, dash, 0f));
        g.draw(new Line2D.Double(x, y - 4, x + 26, y - 4));
        g.setColor(INK2);
        g.drawString(label, (float) x + 32, (float) y);
        return x + 32 + g.getFontMetrics().getStringBounds(label, g).getWidth() + 28;
    }

    private static Color fade(Color c) {
        return new Color(
                (int) (c.getRed() * 0.25 + 255 * 0.75),
                (int) (c.getGreen() * 0.25 + 255 * 0.75),
                (int) (c.getBlue() * 0.25 + 255 * 0.75));
    }
}
