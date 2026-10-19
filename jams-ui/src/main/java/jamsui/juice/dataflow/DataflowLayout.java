/*
 * DataflowLayout.java
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

import jamsui.juice.dataflow.DataflowGraph.Edge;
import jamsui.juice.dataflow.DataflowGraph.Node;
import jamsui.juice.dataflow.DataflowGraph.NodeKind;
import jamsui.juice.dataflow.DataflowGraph.Port;
import java.awt.font.FontRenderContext;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Layered left-to-right layout of a {@link DataflowGraph}. Edges spanning
 * several layers are routed through dummy positions that are shared by all
 * edges leaving the same output port, edges to earlier components (feedback
 * from the previous iteration) are routed above the graph and entity
 * parameters are distributed by a bus line below the graph.
 *
 * @author Sven Kralisch
 */
public class DataflowLayout {

    public static final int HEAD = 38, PAD = 8, ROW = 18;
    static final int GAP_X = 90, MARGIN = 40, TOP = 84;

    public static class Box {

        public final Node node;
        public double x, y, w, h;
        int layer;

        Box(Node node) {
            this.node = node;
        }

        public boolean contains(double px, double py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }

    public static class Route {

        public final Edge edge;
        public final Path2D path;
        public final Point2D end;

        Route(Edge edge, Path2D path, Point2D end) {
            this.edge = edge;
            this.path = path;
            this.end = end;
        }
    }

    /**
     * element of a layer: a box or a dummy position of an output port
     */
    private static class Elem {

        Box box;
        Port port;
        int layer;
        double y, h;
        double desired;
        int sourceOrder;

        boolean isDummy() {
            return box == null;
        }
    }

    /**
     * connection between two elements of neighbouring layers
     */
    private static class Segment {

        Elem a, b;
        double offA, offB;
    }

    private final DataflowGraph graph;
    private final Map<Node, Box> boxes = new HashMap<>();
    private final List<Route> routes = new ArrayList<>();
    private final List<Box> boxList = new ArrayList<>();
    private double width, height, busY;

    public DataflowLayout(DataflowGraph graph, FontRenderContext frc) {
        this.graph = graph;
        layout(frc);
    }

    public List<Box> getBoxes() {
        return boxList;
    }

    public Box getBox(Node node) {
        return boxes.get(node);
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getBusY() {
        return busY;
    }

    public static boolean isDocument(Node n) {
        return n.kind == NodeKind.DATASTORE || n.kind == NodeKind.PARAMETERS;
    }

    public Point2D portPoint(Port p) {
        Box b = boxes.get(p.node);
        double py;
        if (isDocument(p.node)) {
            py = b.y + b.h / 2;
        } else {
            py = b.y + HEAD + PAD + p.index * ROW + ROW / 2.0;
        }
        return new Point2D.Double(p.input ? b.x : b.x + b.w, py);
    }

    public double rowY(Box b, int row) {
        return b.y + HEAD + PAD + row * ROW + ROW / 2.0;
    }

    private void layout(FontRenderContext frc) {

        // box sizes
        for (Node n : graph.getNodes()) {
            Box b = new Box(n);
            double inW = 0, outW = 0;
            for (Port p : n.inputs) {
                inW = Math.max(inW, DataflowRenderer.portWidth(p.label, frc));
            }
            for (Port p : n.outputs) {
                outW = Math.max(outW, DataflowRenderer.portWidth(p.label, frc));
            }
            for (String s : n.states) {
                outW = Math.max(outW, DataflowRenderer.portWidth("↻ " + s, frc));
            }
            double titleW = DataflowRenderer.titleWidth(n.title, frc);
            double subW = DataflowRenderer.subWidth(n.subtitle, frc);
            if (isDocument(n)) {
                b.w = Math.max(110, Math.max(titleW, subW) + 30);
                b.h = HEAD + 6;
            } else {
                b.w = Math.max(150, Math.max(Math.max(titleW, subW) + 22, inW + outW + 44));
                int rows = Math.max(1, Math.max(n.inputs.size(), n.outputs.size() + n.states.size()));
                b.h = HEAD + 2 * PAD + rows * ROW;
            }
            boxes.put(n, b);
            boxList.add(b);
        }

        // layer assignment (longest path) on the forward data edges
        Map<Node, List<Edge>> in = new HashMap<>(), out = new HashMap<>();
        for (Edge e : graph.getEdges()) {
            if (e.feedback || e.parameter) {
                continue;
            }
            out.computeIfAbsent(e.from.node, k -> new ArrayList<>()).add(e);
            in.computeIfAbsent(e.to.node, k -> new ArrayList<>()).add(e);
        }
        List<Node> sorted = new ArrayList<>(graph.getNodes());
        sorted.sort(Comparator.comparingInt(n -> n.order));
        Map<Node, Integer> layer = new HashMap<>();
        int maxLayer = 0;
        for (int iter = 0; iter < sorted.size() + 1; iter++) {
            boolean changed = false;
            for (Node n : sorted) {
                if (n.kind == NodeKind.PARAMETERS) {
                    layer.put(n, 0);
                    continue;
                }
                int l = 0;
                for (Edge e : in.getOrDefault(n, new ArrayList<>())) {
                    l = Math.max(l, layer.getOrDefault(e.from.node, 0) + 1);
                }
                if (!Integer.valueOf(l).equals(layer.get(n))) {
                    layer.put(n, l);
                    changed = true;
                }
                maxLayer = Math.max(maxLayer, l);
            }
            if (!changed) {
                break;
            }
        }
        // sources are moved next to their first consumer, outputs to the end
        for (Node n : sorted) {
            if (n.kind == NodeKind.OUTPUT) {
                layer.put(n, maxLayer + 1);
            }
        }
        for (Node n : sorted) {
            if (n.kind != NodeKind.PARAMETERS && in.getOrDefault(n, new ArrayList<>()).isEmpty() && out.containsKey(n)) {
                int min = Integer.MAX_VALUE;
                for (Edge e : out.get(n)) {
                    min = Math.min(min, layer.get(e.to.node));
                }
                layer.put(n, Math.max(0, min - 1));
            }
        }
        int nLayers = 0;
        for (Node n : sorted) {
            nLayers = Math.max(nLayers, layer.get(n) + 1);
            boxes.get(n).layer = layer.get(n);
        }

        // layer elements: boxes and shared dummies per output port
        List<List<Elem>> layers = new ArrayList<>();
        for (int i = 0; i < nLayers; i++) {
            layers.add(new ArrayList<>());
        }
        Map<Box, Elem> boxElems = new HashMap<>();
        for (Node n : sorted) {
            if (n.kind == NodeKind.PARAMETERS) {
                continue;
            }
            Elem el = new Elem();
            el.box = boxes.get(n);
            el.layer = layer.get(n);
            el.h = el.box.h;
            el.sourceOrder = n.order;
            layers.get(el.layer).add(el);
            boxElems.put(el.box, el);
        }

        Map<String, Elem> dummies = new HashMap<>();
        Map<Edge, List<Elem>> chains = new HashMap<>();
        for (Edge e : graph.getEdges()) {
            if (e.feedback || e.parameter) {
                continue;
            }
            int ls = layer.get(e.from.node), lt = layer.get(e.to.node);
            List<Elem> chain = new ArrayList<>();
            for (int l = ls + 1; l < lt; l++) {
                String key = System.identityHashCode(e.from) + "@" + l;
                Elem d = dummies.get(key);
                if (d == null) {
                    d = new Elem();
                    d.port = e.from;
                    d.layer = l;
                    d.h = ROW;
                    d.sourceOrder = e.from.node.order;
                    dummies.put(key, d);
                    layers.get(l).add(d);
                }
                chain.add(d);
            }
            chains.put(e, chain);
        }

        // segments between neighbouring layers
        List<Segment> segments = new ArrayList<>();
        Map<String, Boolean> seen = new HashMap<>();
        for (Edge e : graph.getEdges()) {
            if (e.feedback || e.parameter) {
                continue;
            }
            Box sb = boxes.get(e.from.node), tb = boxes.get(e.to.node);
            Elem prev = boxElems.get(sb);
            double prevOff = portPointRel(e.from, sb);
            for (Elem d : chains.get(e)) {
                addSegment(segments, seen, prev, prevOff, d, ROW / 2.0);
                prev = d;
                prevOff = ROW / 2.0;
            }
            addSegment(segments, seen, prev, prevOff, boxElems.get(tb), portPointRel(e.to, tb));
        }
        Map<Elem, List<Segment>> left = new HashMap<>(), right = new HashMap<>();
        for (Segment s : segments) {
            right.computeIfAbsent(s.a, k -> new ArrayList<>()).add(s);
            left.computeIfAbsent(s.b, k -> new ArrayList<>()).add(s);
        }

        // initial order: execution order
        for (List<Elem> l : layers) {
            l.sort(Comparator.comparingInt((Elem el) -> el.sourceOrder).thenComparing(el -> el.isDummy() ? 1 : 0));
            stack(l);
        }

        // crossing reduction (barycenter) and vertical placement
        for (int round = 0; round < 24; round++) {
            boolean down = round % 2 == 0;
            boolean placeOnly = round >= 12;
            for (int k = 0; k < nLayers; k++) {
                int li = down ? k : nLayers - 1 - k;
                List<Elem> l = layers.get(li);
                for (Elem el : l) {
                    el.desired = desired(el, down ? left : right, down, round >= 20 ? (down ? right : left) : null);
                }
                if (!placeOnly) {
                    l.sort(Comparator.comparingDouble(el -> el.desired + el.h / 2));
                }
                place(l);
            }
        }

        // normalise vertical positions
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (List<Elem> l : layers) {
            for (Elem el : l) {
                minY = Math.min(minY, el.y);
                maxY = Math.max(maxY, el.y + el.h);
            }
        }
        int feedbackCount = 0;
        for (Edge e : graph.getEdges()) {
            if (e.feedback) {
                feedbackCount++;
            }
        }
        double top = TOP + 20 + Math.min(feedbackCount, 12) * 10;
        double shift = top - minY;
        for (List<Elem> l : layers) {
            for (Elem el : l) {
                el.y += shift;
                if (el.box != null) {
                    el.box.y = el.y;
                }
            }
        }
        maxY += shift;

        // horizontal positions
        double[] layerX = new double[nLayers];
        double[] layerW = new double[nLayers];
        double x = MARGIN;
        for (int i = 0; i < nLayers; i++) {
            double w = 0;
            for (Elem el : layers.get(i)) {
                if (el.box != null) {
                    w = Math.max(w, el.box.w);
                }
            }
            layerW[i] = w;
            layerX[i] = x;
            for (Elem el : layers.get(i)) {
                if (el.box != null) {
                    el.box.x = x;
                }
            }
            x += w + GAP_X;
        }
        width = x - GAP_X + MARGIN + 80;

        // lanes for the edges from the components filling an entity
        // collection to the collection node
        List<Edge> sourceEdges = new ArrayList<>();
        for (Edge e : graph.getEdges()) {
            if (e.parameter && e.to.node.kind == NodeKind.PARAMETERS) {
                sourceEdges.add(e);
            }
        }

        // parameter sources below the graph
        busY = maxY + 50 + sourceEdges.size() * 5;
        double paramY = busY;
        Map<Node, Integer> paramIndex = new HashMap<>();
        for (Node n : sorted) {
            if (n.kind == NodeKind.PARAMETERS) {
                paramIndex.put(n, paramIndex.size());
                Box b = boxes.get(n);
                b.x = MARGIN;
                b.y = paramY - b.h / 2;
                paramY += b.h + 14;
            }
        }
        height = paramY + 70;

        // routes
        int lane = 0;
        for (Edge e : graph.getEdges()) {
            Point2D s = portPoint(e.from), t = portPoint(e.to);
            Path2D path = new Path2D.Double();
            path.moveTo(s.getX(), s.getY());
            if (e.parameter && e.to.node.kind == NodeKind.PARAMETERS) {
                // down in the gap right of the source's layer, left below the
                // graph and from the left into the collection node
                int k = sourceEdges.indexOf(e);
                int l = boxes.get(e.from.node).layer;
                double gx = layerX[l] + layerW[l] + 10 + (k % 8) * 4;
                double ly = maxY + 16 + k * 5;
                double lx = Math.max(4, 14 - paramIndex.get(e.to.node) * 4);
                corner(path, s.getX(), s.getY(), gx, s.getY(), gx, ly);
                corner(path, gx, s.getY(), gx, ly, lx, ly);
                corner(path, gx, ly, lx, ly, lx, t.getY());
                corner(path, lx, ly, lx, t.getY(), t.getX(), t.getY());
                path.lineTo(t.getX(), t.getY());
            } else if (e.parameter) {
                double vx = t.getX() - 12;
                path.lineTo(vx, s.getY());
                path.lineTo(vx, t.getY());
                path.lineTo(t.getX(), t.getY());
            } else if (e.feedback) {
                double ly = top - 14 - (lane++ % 12) * 10;
                double ox = s.getX() + 14, ix = t.getX() - 18;
                corner(path, s.getX(), s.getY(), ox, s.getY(), ox, ly);
                corner(path, ox, s.getY(), ox, ly, ix, ly);
                corner(path, ox, ly, ix, ly, ix, t.getY());
                corner(path, ix, ly, ix, t.getY(), t.getX(), t.getY());
                path.lineTo(t.getX(), t.getY());
            } else {
                double cx = s.getX(), cy = s.getY();
                for (Elem d : chains.get(e)) {
                    double dy = d.y + ROW / 2.0;
                    curve(path, cx, cy, layerX[d.layer], dy);
                    path.lineTo(layerX[d.layer] + layerW[d.layer], dy);
                    cx = layerX[d.layer] + layerW[d.layer];
                    cy = dy;
                }
                curve(path, cx, cy, t.getX(), t.getY());
            }
            routes.add(new Route(e, path, t));
        }
    }

    private static void addSegment(List<Segment> segments, Map<String, Boolean> seen, Elem a, double offA, Elem b, double offB) {
        String key = System.identityHashCode(a) + ":" + offA + ">" + System.identityHashCode(b) + ":" + offB;
        if (seen.put(key, true) != null) {
            return;
        }
        Segment s = new Segment();
        s.a = a;
        s.b = b;
        s.offA = offA;
        s.offB = offB;
        segments.add(s);
    }

    private double portPointRel(Port p, Box b) {
        if (isDocument(p.node)) {
            return b.h / 2;
        }
        return HEAD + PAD + p.index * ROW + ROW / 2.0;
    }

    /**
     * desired top position of an element, derived from its connections
     */
    private static double desired(Elem el, Map<Elem, List<Segment>> primary, boolean primaryIsLeft, Map<Elem, List<Segment>> secondary) {
        double sum = 0;
        int n = 0;
        for (Segment s : primary.getOrDefault(el, new ArrayList<>())) {
            sum += primaryIsLeft ? (s.a.y + s.offA - s.offB) : (s.b.y + s.offB - s.offA);
            n++;
        }
        if (secondary != null) {
            for (Segment s : secondary.getOrDefault(el, new ArrayList<>())) {
                sum += primaryIsLeft ? (s.b.y + s.offB - s.offA) : (s.a.y + s.offA - s.offB);
                n++;
            }
        }
        return n == 0 ? el.y : sum / n;
    }

    private static double sep(Elem a, Elem b) {
        if (a.isDummy() && b.isDummy()) {
            return 6;
        }
        if (a.isDummy() || b.isDummy()) {
            return 14;
        }
        return 28;
    }

    private static void stack(List<Elem> l) {
        double y = 0;
        for (int i = 0; i < l.size(); i++) {
            Elem el = l.get(i);
            el.y = y;
            el.desired = y;
            y += el.h + (i + 1 < l.size() ? sep(el, l.get(i + 1)) : 0);
        }
    }

    /**
     * Places the elements of a layer as close as possible to their desired
     * positions while keeping their order and minimum distances (isotonic
     * regression, pool adjacent violators).
     */
    private static void place(List<Elem> l) {
        int n = l.size();
        if (n == 0) {
            return;
        }
        double[] off = new double[n];
        for (int i = 1; i < n; i++) {
            off[i] = off[i - 1] + l.get(i - 1).h + sep(l.get(i - 1), l.get(i));
        }
        double[] val = new double[n];
        int[] cnt = new int[n];
        int[] start = new int[n];
        int blocks = 0;
        for (int i = 0; i < n; i++) {
            val[blocks] = l.get(i).desired - off[i];
            cnt[blocks] = 1;
            start[blocks] = i;
            blocks++;
            while (blocks > 1 && val[blocks - 2] > val[blocks - 1]) {
                double v = (val[blocks - 2] * cnt[blocks - 2] + val[blocks - 1] * cnt[blocks - 1]) / (cnt[blocks - 2] + cnt[blocks - 1]);
                cnt[blocks - 2] += cnt[blocks - 1];
                val[blocks - 2] = v;
                blocks--;
            }
        }
        for (int b = 0; b < blocks; b++) {
            for (int i = start[b]; i < start[b] + cnt[b]; i++) {
                l.get(i).y = val[b] + off[i];
            }
        }
    }

    private static void curve(Path2D path, double x1, double y1, double x2, double y2) {
        if (Math.abs(y1 - y2) < 0.5) {
            path.lineTo(x2, y2);
            return;
        }
        double dx = Math.max(30, (x2 - x1) / 2);
        path.curveTo(x1 + dx, y1, x2 - dx, y2, x2, y2);
    }

    /**
     * line from (x0,y0) via the corner (x1,y1) towards (x2,y2) with a rounded
     * corner
     */
    private static void corner(Path2D path, double x0, double y0, double x1, double y1, double x2, double y2) {
        double r = 8;
        double d1 = Math.hypot(x1 - x0, y1 - y0), d2 = Math.hypot(x2 - x1, y2 - y1);
        if (d1 < 0.5 || d2 < 0.5) {
            path.lineTo(x1, y1);
            return;
        }
        r = Math.min(r, Math.min(d1, d2) / 2);
        double ax = x1 - (x1 - x0) / d1 * r, ay = y1 - (y1 - y0) / d1 * r;
        double bx = x1 + (x2 - x1) / d2 * r, by = y1 + (y2 - y1) / d2 * r;
        path.lineTo(ax, ay);
        path.quadTo(x1, y1, bx, by);
    }
}
