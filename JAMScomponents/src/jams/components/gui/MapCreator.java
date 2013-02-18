/*
 * MapCreator.java
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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPolygon;
import jams.data.Attribute;
import jams.data.Attribute.Entity.NoSuchAttributeException;
import jams.data.DefaultDataFactory;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.geotools.filter.text.cql2.CQLException;
import org.geotools.map.DefaultMapLayer;
import jams.data.*;
import jams.model.JAMSGUIComponent;
import jams.model.JAMSVarDescription;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.gui.swing.JMapPane;
import org.geotools.map.DefaultMapContext;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import jams.components.io.ShapeTool;
import jams.model.JAMSComponentDescription;
import java.io.Serializable;
import java.util.HashSet;
import org.geotools.gui.swing.event.SelectionChangeListener;
import org.geotools.gui.swing.event.SelectionChangedEvent;

/**
 * Viewer component for JAMS entities and parameter. Each parameter map is implemented by
 * a MapContext to add up in a stack of thematic maps. On the top a set of optional vector layers
 * could be drawn.
 * Based on GeoTools 2.5.2 (gt-mappane-2.5.5 - not part of standard geotools distribution, so
 * unsupported)
 * Note: In future and stable GeoTools 2.6 JMapPane will be rewritten and change to MapWidget...
 *
 * @author C. Schwartze
 */

@JAMSComponentDescription(
        title="MapCreator",
        author="Christian Schwartze",
        description="Viewer component for JAMS entities, parameter and optional vector layers.",
        date="2010-10-22"
        )

public class MapCreator extends JAMSGUIComponent implements MouseListener {

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Name of SLD-File containing layer style information"
        )
        public Attribute.String stylesFileName;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "ID of a style in the SLD-File"
        )
        public Attribute.Integer styleID;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Collection of hru objects"
        )
        public Attribute.EntityCollection hrus;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Name of hru attribute to add for mapping"
        )
        public Attribute.StringArray showAttr;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Number of ranges for classification attribute"
        )
        public Attribute.StringArray numOfRanges;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Color shading the ranges"
        )
        public Attribute.StringArray rangeColor;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Name of shapefile to add as a layer to the map"
        )
        public Attribute.String shapeFileName1;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Name of shapefile to add as a layer to the map"
        )
        public Attribute.String shapeFileName2;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Name of shapefile to add as a layer to the map"
        )
        public Attribute.String shapeFileName3;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Colors for extra shapefiles"
        )
        public Attribute.StringArray shapeColors;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Original shape file name"
        )
        public Attribute.String baseShape;

    transient private JPanel panel, waitPanel;
    transient protected GISPanel gispanel;
    transient private DefaultMapLayer[] optLayers = new DefaultMapLayer[3];
    transient private MapCollection[] mc;
    private int numOfParams,  infoidx;
    private String mapFTypeName = "mapFType";
    private DefaultMutableTreeNode top,  last;
    private JTree tree;
    private boolean finished = false;
    transient private DefaultMapContext map;
    transient private JMapPane mp;
    transient private JTextPane info;
    transient private JSplitPane mainSplitPane;
    transient private JSplitPane legendPane;
    transient public JScrollPane treeView;
    private SimpleFeature selectedF = null;
    private CoordinateReferenceSystem crs;
    transient private DefaultMapContext topmap;
    transient private DefaultMapLayer selection = null;
    private Envelope fullExtent;
    private String[] otherLayers;
    private int div_hor;
    private Style selectStyle;

    static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);

    Set<SelectionChangeListener> listenerSet = new HashSet<SelectionChangeListener>();

    public void addSelectionChangeListener(SelectionChangeListener listener){
        this.listenerSet.add(listener);
    }

    @Override
    public void run() throws Exception {

        if (panel == null) {
            return;
        }
        
        java.net.URL shp = new java.io.File(baseShape.getValue().split(";")[0]).toURI().toURL();
        ShapefileDataStore shpDs = new ShapefileDataStore(shp);
        crs = shpDs.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem();

        if (crs==null) {
            ((JLabel)waitPanel.getComponents()[0]).setText("<html><center>No *.PRJ file found for<br>"+shp.getFile()+"<center><html>");
            ((JLabel)waitPanel.getComponents()[0]).setIcon(new ImageIcon(getModel().getRuntime().getClassLoader().getResource("jams/components/gui/resources/error.png")));
            return;
        }

        gispanel.removeAll();

        if (shapeFileName1 == null) {
            shapeFileName1 = getModel().getRuntime().getDataFactory().createString();
        }
        if (shapeFileName2 == null) {
            shapeFileName2 = getModel().getRuntime().getDataFactory().createString();
        }
        if (shapeFileName3 == null) {
            shapeFileName3 = getModel().getRuntime().getDataFactory().createString();
        }

        otherLayers = new String[]{shapeFileName1.getValue(), shapeFileName2.getValue(), shapeFileName3.getValue()};

        /* Reading additional shapefiles */
        int j = 0;
        for (String s : otherLayers) {
            DefaultMapLayer layer = null;
            if (!s.isEmpty()) {
                try {
                    java.io.File shpFile = new java.io.File(getModel().getWorkspaceDirectory().getPath() + "/" + otherLayers[j]);
                    java.net.URL shpUrl = shpFile.toURI().toURL();
                    ShapefileDataStore ds = new ShapefileDataStore(shpUrl);
                    List<Name> featureNames = ds.getNames();
                    FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = ds.getFeatureSource(featureNames.get(0));

                    String geoType = featureSource.getSchema().getGeometryDescriptor().getType().getName().toString();
                    if (geoType.equals("LineString") || geoType.equals("MultiLineString")) {
                        layer = new DefaultMapLayer(featureSource, gispanel.getLineStyle(shapeColors.getValue()[j]));
                    } else if (geoType.equals("Point") || geoType.equals("MultiPoint")) {
                        layer = new DefaultMapLayer(featureSource, gispanel.getPointStyle(shapeColors.getValue()[j]));
                    } else if (geoType.equals("Polygon") || geoType.equals("MultiPolygon")) {
                        layer = new DefaultMapLayer(featureSource, gispanel.getSurfaceStyle(shapeColors.getValue()[j]));
                    }
                    layer.setTitle(new File(otherLayers[j]).getName());
                    optLayers[j] = layer;
                } catch (Exception e) {
                    this.getModel().getRuntime().handle(e);

                }
            }
            j++;
        }

        mp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (mp.getState() == JMapPane.Select) {
                    try {
                        gispanel.getFeatureInfo(e.getX(), e.getY());
                        for (SelectionChangeListener l : listenerSet){
                            l.selectionChanged(new SelectionChangedEvent(mp,null));
                        }
                    } catch (CQLException ex) {
                        MapCreator.this.getModel().getRuntime().handle(ex);
                    } catch (IOException ex) {
                        MapCreator.this.getModel().getRuntime().handle(ex);
                    }
                }
            }
        });

        gispanel.addToolbar();

        /* Define a set of maps (MapCollection) and add them to MapPanel */
        numOfParams = showAttr.getValue().length;
        mc = new MapCollection[numOfParams];
        getCollections();
        for (int i = 0; i < numOfParams; i++) {
            if (mc[i] == null) {
                continue;
            }
            gispanel.addMap(mc[i]);
            infoidx = i;
        }

        /* Add additional maps to topmap context */
        for (DefaultMapLayer l : optLayers) {
            if ((l != null) && (topmap != null)) {
                topmap.addLayer(l);
            }
        }
        
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }

        finished = true;

        if (topmap == null) {
            panel.removeAll();
            JLabel label = new JLabel("No geometry features found in entity set!", SwingConstants.CENTER);
            label.setVerticalTextPosition(JLabel.BOTTOM);
            label.setHorizontalTextPosition(JLabel.CENTER);
            label.setFont(new Font("Dialog", Font.BOLD, 14));           
            panel.setLayout(new BorderLayout());
            panel.add(label, BorderLayout.CENTER);
            panel.updateUI();
            return;
        }
        
        fullExtent = topmap.getAreaOfInterest();

        mp.setMapArea(topmap.getLayerBounds());

        div_hor = gispanel.getSize().width - mainSplitPane.getInsets().right - mainSplitPane.getDividerSize() - 150;
        int div_ver = gispanel.getSize().height - mainSplitPane.getInsets().bottom - mainSplitPane.getDividerSize() - 150;

        mainSplitPane.setLeftComponent(mp);
        mainSplitPane.setRightComponent(legendPane);
        mainSplitPane.setDividerLocation(div_hor);
        legendPane.setDividerLocation(div_ver);

        gispanel.add(mainSplitPane);

    }

    @Override
    public JPanel getPanel() {
        try {
            gispanel = new GISPanel();
            panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(gispanel, BorderLayout.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
            this.getModel().getRuntime().handle(e);
        }

        if (panel == null) {
            panel = new JPanel();
            JTextField text = new JTextField("Available only under Windows!");
            text.setEditable(false);
            text.setBorder(null);
            panel.add(text);
        }
        return panel;
    }

    protected SimpleFeature getSelectedFeature() {
        return this.selectedF;
    }

    private void getCollections() throws NoSuchAttributeException, Exception {

        for (int i = 0; i <= numOfParams - 1; i++) {

            FeatureCollection<SimpleFeatureType, SimpleFeature> fc = FeatureCollections.newCollection();

            SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
            sftb.setName(mapFTypeName);
            sftb.setCRS(crs);
            sftb.add("geo", MultiPolygon.class);
            sftb.add("newAt", Object.class);
            SimpleFeatureType mapFType = sftb.buildFeatureType();

            Iterator<Attribute.Entity> hrusIterate = hrus.getEntities().iterator();
            Set<Double> s = new TreeSet<Double>();
            Attribute.Entity e = null;

            while (hrusIterate.hasNext()) {

                e = hrusIterate.next();
                
                if (!e.existsAttribute("geom")) {
                    continue;
                }
                
                SimpleFeature f = SimpleFeatureBuilder.template(mapFType, new Integer(
                        new Double(e.getDouble("ID")).intValue()).toString());
                f.setAttribute("geo", e.getGeometry("geom"));
                f.setAttribute("newAt", e.getDouble(showAttr.getValue()[i]));
                fc.add(f);
                s.add(e.getDouble(showAttr.getValue()[i]));
            }
            
            if (fc.isEmpty()) {
                continue;
            }
            
            mc[i] = new MapCollection(showAttr.getValue()[i], fc, s, rangeColor.getValue()[i], Integer.parseInt(numOfRanges.getValue()[i]), crs);
            DefaultMutableTreeNode mapNode = new DefaultMutableTreeNode(mc[i].getDesc());
            top.add(mapNode);

            Object[] nodeContent = mc[i].getRanges();
            DefaultMutableTreeNode entry = null;
           
            for (int j = 1; j <= nodeContent.length - 1; j++) {
                entry = new DefaultMutableTreeNode("<= " + Math.round((Double) nodeContent[j] * 100) / 100.0,
                        false);
                mapNode.add(entry);
            }
        }
        
        DefaultMutableTreeNode layerEntry = null;
        int i = 0;
        for (DefaultMapLayer l : optLayers) {
            if (l != null) {
                layerEntry = new DefaultMutableTreeNode(optLayers[i].getTitle());
                top.add(layerEntry);
            }
            i++;
        }
    }

    /* Icon style for additional layers in legend */
    class MyLayerIcon implements Icon {

        private int idx;

        public MyLayerIcon(Integer idx) {
            this.idx = idx - numOfParams;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {

            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(0, 0, 20, 13, 5, 5);
            String geoType = optLayers[idx].getFeatureSource().getSchema().getGeometryDescriptor().getType().getName().toString();
            g2d.setColor(Color.decode("#" + shapeColors.getValue()[idx]));
            if (geoType.equals("LineString") || geoType.equals("MultiLineString")) {
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(4, 4, 13, 6);
                g2d.drawLine(13, 6, 15, 9);
            } else if (geoType.equals("Point") || geoType.equals("MultiPoint")) {
                g2d.fillRect(4, 4, 4, 4);
                g2d.fillRect(13, 6, 4, 4);
            } else if (geoType.equals("Polygon") || geoType.equals("MultiPolygon")) {
                int[] xPoints = new int[]{2, 5, 9, 17};
                int[] yPoints = new int[]{3, 10, 12, 2};
                g2d.fillPolygon(xPoints, yPoints, 4);
            }
        }

        public int getIconWidth() {
            return 30;
        }

        public int getIconHeight() {
            return 13;
        }
    }

    /* Colored Icon style for parameter ranges in map legend */
    class MyIcon implements Icon, Serializable {

        private Integer a,  b;

        public MyIcon(Integer a, Integer b) {
            this.a = a;
            this.b = b;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (finished) {
                g.drawRect(1, 1, 19, 14);
                g.setColor(Color.BLACK);
                g.setColor((Color) mc[a].getColors()[mc[a].getRanges().length - b - 1]);
                g.fillRect(1, 1, 19, 14);
            } else {
                tree.setVisible(false);
            }
        }

        public int getIconWidth() {
            return 30;
        }

        public int getIconHeight() {
            return 20;
        }
    }

    /* Rendering different kinds of legend entries */
    public class NodeRenderer extends DefaultTreeCellRenderer {

        private ImageIcon iconProject =
                new ImageIcon(getModel().getRuntime().getClassLoader().getResource("jams/components/gui/resources/root.png"));
        private ImageIcon iconRange =
                new ImageIcon(getModel().getRuntime().getClassLoader().getResource("jams/components/gui/resources/map.png"));
        private Icon blatt;
        private Icon blatt2;

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel, expanded,
                    leaf,
                    row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            DefaultMutableTreeNode parentnode = (DefaultMutableTreeNode) node.getParent();
            int idxToParentNode = 0;
            int idxToTopNode = 0;

            if (!node.isRoot()) {
                idxToParentNode = parentnode.getIndex(node);
                idxToTopNode = top.getIndex(parentnode);
            }

            if (node.isRoot()) {
                this.setIcon(iconProject);
            }
            if (!leaf && !node.isRoot()) {
                this.setIcon(iconRange);
            }
            if (leaf && node.getParent() == top) {
                blatt2 = new MyLayerIcon(top.getIndex(node));
                this.setIcon(blatt2);
            } else if (leaf) {
                blatt = new MyIcon(idxToTopNode, idxToParentNode);
                this.setIcon(blatt);
            }
            return this;
        }
    }

    public class GISPanel extends JPanel {

        private StyleBuilder sb;

        private Style createFromSLD(String str) {
            File sld = new File(str);
            SLDParser stylereader;
            try {
                stylereader = new SLDParser(styleFactory, sld);
                Style[] style = stylereader.readXML();
                return style[0];
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                System.exit(0);
            }
            return null;
        }

        public Style getLineStyle(String c) {
            sb = new StyleBuilder();
            LineSymbolizer ls = sb.createLineSymbolizer(Color.decode("#" + c), 2);
            return sb.createStyle(ls);
        }

        public Style getPointStyle(String c) {
            sb = new StyleBuilder();
            Mark pointMarker = sb.createMark(StyleBuilder.MARK_SQUARE, Color.decode("#" + c));
            PointSymbolizer ps = sb.createPointSymbolizer(sb.createGraphic(null, pointMarker,
                    null,0,7,0));
            return sb.createStyle(ps);
        }

        public Style getSurfaceStyle(String c) {
            sb = new StyleBuilder();
            PolygonSymbolizer pgs = sb.createPolygonSymbolizer(Color.decode("#" + c), Color.decode("#000000"), 1);
            return sb.createStyle(pgs);
        }

        public void addMap(MapCollection m) {
            try {
                /* Remove selection layer and all additional layers from map stack */
                topmap.removeLayer(selection);
                for (DefaultMapLayer l : optLayers) {
                    if (l != null) {
                        topmap.removeLayer(l);
                    }
                }
            } catch (Exception e) {
            }
            map = m.getMapContext();
            topmap = map;
            for (DefaultMapLayer l : optLayers) {
                if (l != null) {
                    topmap.addLayer(l);
                }
            }
            mp.setContext(map);
            mp.setRenderer(new StreamingRenderer());
        }

        public void addToolbar() {

            URL url_zoomIn = this.getClass().getResource("resources/zoom_in.png");
            ImageIcon icon_zoomIn = new ImageIcon(url_zoomIn);
            URL url_zoomOut = this.getClass().getResource("resources/zoom_out.png");
            ImageIcon icon_zoomOut = new ImageIcon(url_zoomOut);
            URL url_zoomExtent = this.getClass().getResource("resources/zoom_extent.png");
            ImageIcon icon_zoomExtent = new ImageIcon(url_zoomExtent);
            URL url_pan = this.getClass().getResource("resources/pan.png");
            ImageIcon icon_pan = new ImageIcon(url_pan);
            URL url_select = this.getClass().getResource("resources/select.png");
            ImageIcon icon_select = new ImageIcon(url_select);
            URL url_export = this.getClass().getResource("resources/export.png");
            ImageIcon icon_export = new ImageIcon(url_export);

            JPanel buttons = new JPanel();
            JButton zoomInButton = new JButton(icon_zoomIn);
            final Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            zoomInButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    mp.setCursor(defaultCursor);
                    mp.setState(JMapPane.ZoomIn);
                }
            });
            buttons.add(zoomInButton);

            JButton zoomOutButton = new JButton(icon_zoomOut);
            zoomOutButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    mp.setCursor(defaultCursor);
                    mp.setState(JMapPane.ZoomOut);
                }
            });
            buttons.add(zoomOutButton);

            JButton zoomExtentButton = new JButton(icon_zoomExtent);
            zoomExtentButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    mp.setCursor(defaultCursor);
                    mp.setMapArea(fullExtent);
                    mp.setReset(true);
                    mp.repaint();

                }
            });
            buttons.add(zoomExtentButton);

            JButton panButton = new JButton(icon_pan);
            final Cursor panCursor = new Cursor(Cursor.MOVE_CURSOR);
            panButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    mp.setCursor(panCursor);
                    mp.setState(JMapPane.Pan);
                }
            });
            buttons.add(panButton);

            JButton selectButton = new JButton(icon_select);
            final Cursor selectCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
            selectButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    mp.setCursor(selectCursor);
                    mp.setState(JMapPane.Select);

                }
            });
            buttons.add(selectButton);

            JButton exportButton = new JButton(icon_export);
            exportButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    mp.setCursor(defaultCursor);
                    try {
                        int idx;
                        if (last == null || last.isRoot()) idx = 0;
                        else if (last.isLeaf()) idx = top.getIndex(last.getParent());
                        else if (top.isNodeChild(last)) idx = top.getIndex(last);
                        else idx = top.getIndex(top);
                        JPanel exportPanel = new ShapeTool(mc[idx], baseShape, legendPane);
                        mainSplitPane.setRightComponent(exportPanel);
                        mainSplitPane.setDividerLocation(div_hor);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "For export please select an attribute layer!");
                    }
                }
            });
            buttons.add(exportButton);

            this.add(buttons, BorderLayout.NORTH);
        }

        public GISPanel() throws Exception {
            mp = new JMapPane();
            mp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            legendPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            info = new JTextPane();
            info.setContentType("text/html");
            info.setText("<html><br><center>No feature selected</center></html>");
            info.setEditable(false);

            top = new DefaultMutableTreeNode("Map List");
            tree = new JTree(top);
            tree.setCellRenderer(new NodeRenderer());
            tree.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (node != null) {
                        last = node;
                        int idxToTopNode;
                        if (node.isLeaf()) {
                            idxToTopNode = top.getIndex(node.getParent());
                        } else {
                            idxToTopNode = top.getIndex(node);
                        }
                        if (e.getClickCount() == 1 & idxToTopNode >= 0 & (top.isNodeChild(node) || node.isLeaf())) {

                            try {

                                addMap(mc[idxToTopNode]);
                                mp.setReset(true);
                                mp.repaint();

                                if (last == null || last.isRoot()) {
                                    infoidx = 0;
                                } else if (last.isLeaf()) {
                                    infoidx = top.getIndex(last.getParent());
                                } else {
                                    infoidx = top.getIndex(last);
                                }
                            } catch (Exception e1) {
                                MapCreator.this.getModel().getRuntime().handle(e1);
                            }
                        }
                    }
                }
            });

            treeView = new JScrollPane(tree);
            legendPane.setTopComponent(treeView);
            legendPane.setBottomComponent(info);
            this.setLayout(new BorderLayout());

            waitPanel = new JPanel();
            Icon waiticon = new ImageIcon(getModel().getRuntime().getClassLoader().getResource("jams/components/gui/resources/wait.gif"));
            waitPanel.setLayout(new BorderLayout());
            JLabel label = new JLabel("Please wait...!", waiticon, SwingConstants.CENTER);
            label.setVerticalTextPosition(JLabel.BOTTOM);
            label.setHorizontalTextPosition(JLabel.CENTER);
            label.setFont(new Font("Dialog", Font.BOLD, 14));
            waitPanel.add(label);
            mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            this.add(waitPanel);
        }

        /* Uses new GeoAPI Filter and CQL */
        private void getFeatureInfo(int a, int b) throws CQLException, IOException {
            double x = (double) a;
            double y = (double) b;
            Rectangle bounds = mp.getBounds();
            double width = mp.getMapArea().getWidth();
            double height = mp.getMapArea().getHeight();
            double mapX = (x * width / (double) bounds.width) + mp.getMapArea().getMinX();
            double mapY = ((bounds.getHeight() - y) * height / (double) bounds.height) + mp.getMapArea().getMinY();
            Filter f1 = CQL.toFilter("CONTAINS(geo, POINT(" + mapX + " " + mapY + "))");
            int idx = 0;
            try {
                if (last == null || last.isRoot()) {
                    idx = 0;
                } else if (last.isLeaf()) {
                    idx = top.getIndex(last.getParent());
                } else {
                    idx = top.getIndex(last);
                }

                FeatureCollection<SimpleFeatureType, SimpleFeature> result = mc[idx].asCollectionDataStore().getFeatureSource(mc[idx].asCollectionDataStore().getTypeNames()[0]).getFeatures(f1);

                if (result.size() == 0) {
                    info.setText("<html><br><center>No feature selected</center></html>");
                    try {
                        topmap.removeLayer(selection);
                    } catch (Exception e) {
                    }
                } else {
                    selectedF = null;
                    selectedF = result.features().next();
                    String paramvalue = String.valueOf(Math.round((Double) selectedF.getAttribute("newAt") * 100) / 100.0); //selectedF.getAttribute("newAt").toString();
                    String infoTxt = "<html><center><b>HRU-ID:</b><br>" + selectedF.getIdentifier() + "<br><b>" + mc[infoidx].getDesc() + ":</b><br>" + paramvalue + "</center></html>";
                    info.setText(infoTxt);

                    /* Mark selected feature by means of a new layer at top map context (SLD from file) */
                    try {
                        topmap.removeLayer(selection);
                    } catch (Exception e) {
                    }
                    selection = new DefaultMapLayer(result, createFromSLD(getModel().getWorkspaceDirectory().getPath() + "/" + stylesFileName.getValue()), "selection");
                    topmap.addLayer(1, selection);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "No attribute layer selected!");
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (mp.getState() == JMapPane.Select) {
            JOptionPane.showMessageDialog(null, "selection");
        }
    }

    public void mousePressed(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseReleased(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseEntered(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseExited(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException{
        stream.defaultReadObject();
        optLayers = new DefaultMapLayer[3];
    }
}
