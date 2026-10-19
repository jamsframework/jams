/*
 * DataflowDialog.java
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
import jams.gui.tools.GUIHelper;
import jams.meta.ModelNode;
import jamsui.juice.dataflow.DataflowGraph.Edge;
import jamsui.juice.dataflow.DataflowGraph.Node;
import jamsui.juice.dataflow.DataflowGraph.Port;
import jamsui.juice.dataflow.DataflowLayout.Box;
import jamsui.juice.dataflow.DataflowLayout.Route;
import java.awt.BasicStroke;
import jamsui.juice.gui.ModelView;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;

/**
 * Window that shows the data flow of the model of a JUICE model view.
 *
 * @author Sven Kralisch
 */
public class DataflowDialog extends JDialog {

    private final ModelView view;
    private final DataflowGraph.Options options = new DataflowGraph.Options();
    private final Canvas canvas = new Canvas();
    private final JScrollPane scroll = new JScrollPane(canvas);
    private DataflowGraph graph;
    private DataflowLayout layout;
    private DataflowRenderer renderer;
    private double zoom = 1;
    private Node selected;
    private Edge selectedEdge;
    private static File lastDir;
    private JTextField search;
    private JLabel searchCount;
    private Color searchDefaultBg;
    private static final Color SEARCH_NO_MATCH = new Color(255, 214, 214);
    private List<Box> searchMatches = List.of();
    private int searchIndex = -1;

    public DataflowDialog(Frame owner, ModelView view) {
        super(owner, false);
        this.view = view;
        init();
        rebuild();
        setSize(new Dimension(Math.min(1400, owner.getWidth()), Math.min(850, owner.getHeight())));
        setLocationRelativeTo(owner);
    }

    private void init() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JToolBar bar = new JToolBar();
        bar.setFloatable(false);

        search = new JTextField(14);
        search.setMaximumSize(search.getPreferredSize());
        search.setToolTipText(JAMS.i18n("DF_Search_Tooltip"));
        searchDefaultBg = search.getBackground();
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchChanged();
            }
        });
        search.addActionListener(e -> searchNext());
        searchCount = new JLabel();

        bar.add(new JLabel(JAMS.i18n("DF_Search_Label") + " "));
        bar.add(search);
        bar.add(searchCount);
        bar.addSeparator();

        JCheckBox params = new JCheckBox(JAMS.i18n("DF_Show_Parameters"), options.showParameters);
        params.addActionListener(e -> {
            options.showParameters = params.isSelected();
            rebuild();
        });
        JCheckBox constants = new JCheckBox(JAMS.i18n("DF_Show_Constants"), options.showConstants);
        constants.addActionListener(e -> {
            options.showConstants = constants.isSelected();
            rebuild();
        });
        JCheckBox unused = new JCheckBox(JAMS.i18n("DF_Show_Unused"), options.showUnused);
        unused.addActionListener(e -> {
            options.showUnused = unused.isSelected();
            rebuild();
        });
        JCheckBox states = new JCheckBox(JAMS.i18n("DF_Show_States"), options.showStates);
        states.addActionListener(e -> {
            options.showStates = states.isSelected();
            rebuild();
        });
        JTextField hide = new JTextField(16);
        hide.setMaximumSize(hide.getPreferredSize());
        hide.setToolTipText(JAMS.i18n("DF_Hide_Tooltip"));
        hide.addActionListener(e -> {
            options.hidePattern = hide.getText();
            rebuild();
        });

        bar.add(params);
        bar.add(constants);
        bar.add(unused);
        bar.add(states);
        bar.addSeparator();
        bar.add(new JLabel(JAMS.i18n("DF_Hide_Pattern") + " "));
        bar.add(hide);
        bar.addSeparator();
        bar.add(button(JAMS.i18n("DF_Refresh"), () -> rebuild()));
        bar.addSeparator();
        bar.add(button("−", () -> setZoom(zoom / 1.25)));
        bar.add(button("+", () -> setZoom(zoom * 1.25)));
        bar.add(button("100 %", () -> zoomToSelection()));
        bar.add(button(JAMS.i18n("DF_Zoom_Fit"), () -> fit()));
        bar.addSeparator();
        bar.add(button(JAMS.i18n("DF_Export_SVG"), () -> export(false)));
        bar.add(button(JAMS.i18n("DF_Export_PNG"), () -> export(true)));

        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getHorizontalScrollBar().setUnitIncrement(24);
        scroll.setWheelScrollingEnabled(true);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(bar, BorderLayout.NORTH);
        getContentPane().add(scroll, BorderLayout.CENTER);

        ToolTipManager.sharedInstance().registerComponent(canvas);
    }

    private static JButton button(String label, Runnable r) {
        JButton b = new JButton(label);
        b.addActionListener(e -> r.run());
        return b;
    }

    /**
     * Rebuilds the graph from the current state of the model.
     */
    public final void rebuild() {
        graph = DataflowGraph.build(view.getModelDescriptor(), options);
        layout = new DataflowLayout(graph, DataflowExport.FRC);
        renderer = new DataflowRenderer(layout);
        searchMatches = List.of();
        searchIndex = -1;
        if (selected != null) {
            Box b = boxOf(selected);
            selected = b == null ? null : b.node;
            renderer.setSelected(selected);
        } else if (selectedEdge != null) {
            selectedEdge = edgeOf(selectedEdge);
            renderer.setSelectedEdge(selectedEdge);
        }
        setTitle(DataflowExport.title(graph));
        canvas.revalidate();
        canvas.repaint();
    }

    private void setZoom(double z) {
        JViewport vp = scroll.getViewport();
        setZoom(z, new Point2D.Double(
                (vp.getViewPosition().x + vp.getWidth() / 2.0) / zoom,
                (vp.getViewPosition().y + vp.getHeight() / 2.0) / zoom));
    }

    /**
     * Sets the zoom factor and centers the view on the given point (in
     * diagram coordinates).
     */
    private void setZoom(double z, Point2D center) {
        JViewport vp = scroll.getViewport();
        zoom = Math.max(0.1, Math.min(4, z));
        canvas.revalidate();
        canvas.repaint();
        SwingUtilities.invokeLater(() -> {
            int x = (int) (center.getX() * zoom - vp.getWidth() / 2.0);
            int y = (int) (center.getY() * zoom - vp.getHeight() / 2.0);
            canvas.scrollRectToVisible(new Rectangle(Math.max(0, x), Math.max(0, y), vp.getWidth(), vp.getHeight()));
        });
    }

    /**
     * Zooms to 100 % and centers the view on the selected component, if any.
     */
    private void zoomToSelection() {
        if (selectedEdge != null) {
            for (Route r : layout.getRoutes()) {
                if (r.edge == selectedEdge) {
                    Rectangle2D bounds = r.path.getBounds2D();
                    setZoom(1, new Point2D.Double(bounds.getCenterX(), bounds.getCenterY()));
                    return;
                }
            }
        }
        Box b = selected == null ? null : boxOf(selected);
        if (b == null) {
            setZoom(1);
        } else {
            setZoom(1, new Point2D.Double(b.x + b.w / 2, b.y + b.h / 2));
        }
    }

    /**
     * Recomputes the matches for the current search text and jumps to the
     * first one.
     */
    private void searchChanged() {
        String needle = search.getText().trim().toLowerCase();
        searchMatches = new ArrayList<>();
        if (!needle.isEmpty()) {
            for (Box b : layout.getBoxes()) {
                if (b.node.title.toLowerCase().contains(needle)) {
                    searchMatches.add(b);
                }
            }
        }
        searchIndex = searchMatches.isEmpty() ? -1 : 0;
        search.setBackground(!needle.isEmpty() && searchMatches.isEmpty() ? SEARCH_NO_MATCH : searchDefaultBg);
        searchCount.setText(searchMatches.isEmpty() ? "" : (searchIndex + 1) + "/" + searchMatches.size());
        goToSearchMatch();
    }

    /**
     * Jumps to the next search match, cycling back to the first after the
     * last one.
     */
    private void searchNext() {
        if (searchMatches.isEmpty()) {
            return;
        }
        searchIndex = (searchIndex + 1) % searchMatches.size();
        searchCount.setText((searchIndex + 1) + "/" + searchMatches.size());
        goToSearchMatch();
    }

    private void goToSearchMatch() {
        if (searchIndex < 0) {
            return;
        }
        Box b = searchMatches.get(searchIndex);
        select(b);
        setZoom(1, new Point2D.Double(b.x + b.w / 2, b.y + b.h / 2));
    }

    /**
     * Finds the box of a node, or of the node with the same component after
     * a rebuild.
     */
    private Box boxOf(Node n) {
        for (Box b : layout.getBoxes()) {
            if (b.node == n || (n.descriptor != null && b.node.descriptor == n.descriptor)
                    || (n.descriptor == null && b.node.descriptor == null && b.node.kind == n.kind && b.node.title.equals(n.title))) {
                return b;
            }
        }
        return null;
    }

    private static boolean samePort(DataflowGraph.Port a, DataflowGraph.Port b) {
        return a.input == b.input && a.label.equals(b.label) && sameNode(a.node, b.node);
    }

    private static boolean sameNode(Node a, Node b) {
        return a == b || (a.descriptor != null && a.descriptor == b.descriptor)
                || (a.descriptor == null && b.descriptor == null && a.kind == b.kind && a.title.equals(b.title));
    }

    /**
     * Finds the edge corresponding to the given one after a rebuild.
     */
    private Edge edgeOf(Edge e) {
        for (Route r : layout.getRoutes()) {
            if (r.edge == e || (samePort(r.edge.from, e.from) && samePort(r.edge.to, e.to))) {
                return r.edge;
            }
        }
        return null;
    }

    /**
     * Returns all edges whose route passes the given point (in view
     * coordinates).
     */
    private List<Edge> edgesAt(Point p) {
        double x = p.x / zoom, y = p.y / zoom, tol = Math.max(3, 4 / zoom);
        Rectangle2D hit = new Rectangle2D.Double(x - tol, y - tol, 2 * tol, 2 * tol);
        BasicStroke stroke = new BasicStroke(2f);
        List<Edge> result = new ArrayList<>();
        for (Route r : layout.getRoutes()) {
            if (!r.path.getBounds2D().intersects(hit)) {
                continue;
            }
            Shape s = stroke.createStrokedShape(r.path);
            if (s.intersects(hit)) {
                result.add(r.edge);
            }
        }
        return result;
    }

    private static String describe(Edge e) {
        return "<html>" + e.from.node.title + (e.from.node.kind == DataflowGraph.NodeKind.COMPONENT
                || e.from.node.kind == DataflowGraph.NodeKind.CONTEXT ? "." + e.from.label : "")
                + " &rarr; <b>" + e.to.node.title + "." + e.to.label + "</b></html>";
    }

    private void fit() {
        JViewport vp = scroll.getViewport();
        setZoom(Math.min(vp.getWidth() / layout.getWidth(), vp.getHeight() / layout.getHeight()));
    }

    private void export(boolean png) {
        JFileChooser fc = new JFileChooser(lastDir != null ? lastDir
                : (view.getSavePath() != null ? view.getSavePath().getParentFile() : null));
        String ext = png ? "png" : "svg";
        fc.setFileFilter(new FileNameExtensionFilter(ext.toUpperCase(), ext));
        String base = view.getSavePath() != null ? view.getSavePath().getName().replaceAll("\\.jam$", "") : graph.getModelName();
        fc.setSelectedFile(new File(fc.getCurrentDirectory(), base + "_dataflow." + ext));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith("." + ext)) {
            f = new File(f.getPath() + "." + ext);
        }
        lastDir = f.getParentFile();
        try {
            if (png) {
                DataflowExport.exportPNG(layout, DataflowExport.title(graph), f, 2);
            } else {
                DataflowExport.exportSVG(layout, DataflowExport.title(graph), f);
            }
        } catch (IOException ex) {
            GUIHelper.showErrorDlg(this, ex.getMessage(), JAMS.i18n("Error"));
        }
    }

    private Box boxAt(Point p) {
        double x = p.x / zoom, y = p.y / zoom;
        for (Box b : layout.getBoxes()) {
            if (b.contains(x, y)) {
                return b;
            }
        }
        return null;
    }

    private void select(Box b) {
        Node n = b == null ? null : b.node;
        selected = n;
        selectedEdge = null;
        renderer.setSelected(n);
        canvas.repaint();
        if (n != null && n.descriptor != null && n.descriptor.getNode() != null && view.getTree() != null) {
            ModelNode mn = n.descriptor.getNode();
            TreePath path = new TreePath(mn.getPath());
            view.getTree().setSelectionPath(path);
            view.getTree().scrollPathToVisible(path);
        }
    }

    /**
     * Selects a single connection. Repeated clicks on overlapping edges
     * cycle through them.
     */
    private void select(List<Edge> edges) {
        int i = edges.indexOf(selectedEdge);
        selectedEdge = edges.get((i + 1) % edges.size());
        selected = null;
        renderer.setSelectedEdge(selectedEdge);
        canvas.repaint();
    }

    private class Canvas extends JComponent {

        private Point dragStart;

        Canvas() {
            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    dragStart = e.getPoint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    setCursor(Cursor.getDefaultCursor());
                    dragStart = null;
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    Box b = boxAt(e.getPoint());
                    List<Edge> edges = b == null ? edgesAt(e.getPoint()) : List.of();
                    if (edges.isEmpty()) {
                        select(b);
                    } else {
                        select(edges);
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragStart == null) {
                        return;
                    }
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    JViewport vp = scroll.getViewport();
                    Point pos = vp.getViewPosition();
                    pos.translate(dragStart.x - e.getX(), dragStart.y - e.getY());
                    pos.x = Math.max(0, Math.min(pos.x, getWidth() - vp.getWidth()));
                    pos.y = Math.max(0, Math.min(pos.y, getHeight() - vp.getHeight()));
                    vp.setViewPosition(pos);
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (e.isControlDown()) {
                        setZoom(e.getWheelRotation() < 0 ? zoom * 1.1 : zoom / 1.1);
                    } else {
                        scroll.dispatchEvent(SwingUtilities.convertMouseEvent(Canvas.this, e, scroll));
                    }
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
            addMouseWheelListener(ma);
        }

        @Override
        public Dimension getPreferredSize() {
            if (layout == null) {
                return new Dimension(100, 100);
            }
            return new Dimension((int) Math.ceil(layout.getWidth() * zoom), (int) Math.ceil(layout.getHeight() * zoom));
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            double x = e.getX() / zoom, y = e.getY() / zoom;
            for (Box b : layout.getBoxes()) {
                if (!b.contains(x - 8, y) && !b.contains(x + 8, y)) {
                    continue;
                }
                for (Port p : b.node.inputs) {
                    if (near(p, x, y, b)) {
                        return p.tooltip != null ? p.tooltip : p.label;
                    }
                }
                for (Port p : b.node.outputs) {
                    if (near(p, x, y, b)) {
                        return p.tooltip != null ? p.tooltip : p.label;
                    }
                }
                if (b.contains(x, y) && b.node.descriptor != null) {
                    return "<html><b>" + b.node.title + "</b><br>" + b.node.descriptor.getClazz().getName() + "</html>";
                }
            }
            if (boxAt(e.getPoint()) == null) {
                List<Edge> edges = edgesAt(e.getPoint());
                if (edges.size() == 1) {
                    return describe(edges.get(0));
                } else if (edges.size() > 1) {
                    StringBuilder sb = new StringBuilder("<html>");
                    for (Edge edge : edges) {
                        sb.append(describe(edge).replace("<html>", "").replace("</html>", "")).append("<br>");
                    }
                    return sb.append("</html>").toString();
                }
            }
            return null;
        }

        private boolean near(Port p, double x, double y, Box b) {
            Point2D pt = layout.portPoint(p);
            if (Math.abs(pt.getY() - y) > DataflowLayout.ROW / 2.0) {
                return false;
            }
            return p.input ? x <= b.x + b.w / 2 + 8 : x >= b.x + b.w / 2 - 8;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (renderer == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.scale(zoom, zoom);
            renderer.paint(g2, DataflowExport.title(graph));
            g2.dispose();
        }
    }
}
