package jams.components.gui;

import jams.components.io.ShapeTool;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.SchemaException;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.unijena.jams.data.*;
import org.unijena.jams.model.JAMSGUIComponent;
import org.unijena.jams.model.JAMSVarDescription;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import javax.swing.BorderFactory;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MapCreator3D extends JAMSGUIComponent implements MouseListener {
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "Name of SLD-File containing layer style information")
    public JAMSString stylesFileName;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "ID of a style in the SLD-File")
    public JAMSInteger styleID;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "Data file directory name")
    public JAMSString dirName;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.RUN, description = "Collection of hru objects")
    public JAMSEntityCollection hrus;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "Name of hru attribute to add for mapping")
    public JAMSStringArray showAttr;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "Number of ranges for classification attribute")
    public JAMSStringArray numOfRanges;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "Color shading the ranges")
    public JAMSStringArray rangeColor;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "Name of shapefile to add as a layer to the map")
    public JAMSString shapeFileName1;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "Name of shapefile to add as a layer to the map")
    public JAMSString shapeFileName2;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "Name of shapefile to add as a layer to the map")
    public JAMSString shapeFileName3;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "Colors for extra shapefiles")
    public JAMSStringArray shapeColors;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.RUN, description = "Original shape file name")
    public JAMSString baseShape;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HeightMapFile"
            )
            public JAMSString heightMap;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HeightMapFile"
            )
            public JAMSInteger resolution = null;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "HeightMapFile"
            )
            public JAMSBoolean light = null;
    
    
    private JPanel panel;
    
    private DefaultMapLayer[] optLayers = new DefaultMapLayer[3];
    
    private MapCollection[] mc;
    
    private int numOfParams, infoidx;
    
    private String mapFTypeName = "mapFType";
    
    private DefaultMutableTreeNode top, last;
    
    private JTree tree;
    
    private boolean finished = false;
    
    private MapContext map;
    
    private Styled3DMapPane mp;
    
    private JTextPane info;
    
    private JSplitPane p, splitPane;
    private JAMSAscGridReader asg;
    public JScrollPane treeView;
    
    private Feature selectedF = null;
    
    @SuppressWarnings({"unchecked","deprecation"})
    public void run() throws Exception {
        
        if (!System.getProperty("os.name").contains("Windows")) {
            return;
        }
        info.setText("3D Map Pane ... initializing");
        if (shapeFileName1 == null)
            shapeFileName1 = new JAMSString("");
        if (shapeFileName2 == null)
            shapeFileName2 = new JAMSString("");
        if (shapeFileName3 == null)
            shapeFileName3 = new JAMSString("");
        
        FeatureSource fs = null;
        String[] otherLayers = { shapeFileName1.getValue(),
        shapeFileName2.getValue(), shapeFileName3.getValue() };
        for (int i = 0; i <= 2; i++) {
            if (!otherLayers[i].equals("")) {
                try {
                    java.io.File shpFile = new java.io.File(dirName.getValue()
                    + "/" + otherLayers[i]);
                    java.net.URL shpUrl = shpFile.toURI().toURL();
                    String sourcename = shpFile.getName().split("\\.")[0];
                    fs = new ShapefileDataStore(shpUrl)
                    .getFeatureSource(sourcename);
                } catch (Exception e) {
                }
                DefaultMapLayer layer = new DefaultMapLayer(fs, getStyle(fs, i));
                layer.setTitle(otherLayers[i]);
                optLayers[i] = layer;
            }
        }
        
        numOfParams = showAttr.getValue().length;
        mc = new MapCollection[numOfParams];
        
        for (int i = 0; i <= numOfParams - 1; i++) {
            
            FeatureCollection fc = FeatureCollections.newCollection();
            GeometryAttributeType geo = (GeometryAttributeType) AttributeTypeFactory
                    .newAttributeType("geo", MultiPolygon.class);
            AttributeType newAt = AttributeTypeFactory.newAttributeType(
                    "newAt", Object.class);
            FeatureTypeBuilder mapFeatureType = FeatureTypeBuilder
                    .newInstance(mapFTypeName);
            mapFeatureType.addType(geo);
            mapFeatureType.addType(newAt);
            DefaultFeatureType ft = null;
            try {
                ft = (DefaultFeatureType) mapFeatureType.getFeatureType();
            } catch (SchemaException e) {
                e.printStackTrace();
            }
            
            Iterator<JAMSEntity> hrusIterate = hrus.getEntities().iterator();
            Set<Double> s = new TreeSet<Double>();
            JAMSEntity e;
            
            while (hrusIterate.hasNext()) {
                e = hrusIterate.next();
                Feature newFeature = ft.create(new Object[] {
                    e.getGeometry("geom"),
                    e.getDouble(showAttr.getValue()[i]) }, new Integer(
                        new Double(e.getDouble("ID")).intValue()).toString());
                
                fc.add(newFeature);
                s.add(e.getDouble(showAttr.getValue()[i]));
            }
            mc[i] = new MapCollection(showAttr.getValue()[i], fc, s, rangeColor
                    .getValue()[i], Integer.parseInt(numOfRanges.getValue()[i]));
            DefaultMutableTreeNode mapNode = new DefaultMutableTreeNode(mc[i]
                    .getDesc());
            top.add(mapNode);
            Object[] nodeContent = mc[i].getRanges();
            DefaultMutableTreeNode entry = null;
            for (int j = 1; j <= nodeContent.length - 1; j++) {
                entry = new DefaultMutableTreeNode("<= " + Math.round((Double)nodeContent[j]*100000)/100000.0,
                        false);
                mapNode.add(entry);
            }
            for (int k = 0; k <= 2; k++) {
                if (!otherLayers[k].equals("")) {
                    mc[i].getMapContext().addLayer(optLayers[k]);
                }
            }
        }
        
        DefaultMutableTreeNode layerEntry = null;
        for (int i = 0; i <= 2; i++) {
            if (!otherLayers[i].equals("")) {
                layerEntry = new DefaultMutableTreeNode(optLayers[i].getTitle());
                top.add(layerEntry);
            }
        }
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        if (mp == null)
            mp = new Styled3DMapPane();
        info.setText("3D Map Pane ... loading map");
        asg = new JAMSAscGridReader(this.dirName + "/" + this.heightMap.toString());
        boolean light = true;
        int Resolution = 256;
        
        if (resolution != null) {
            Resolution = resolution.getValue();
        }
        if (this.light != null) {
            light = this.light.getValue();
        }
        
        mp.light = light;
        mp.xRes = Resolution;
        mp.yRes = Resolution;
        mp.textureWidth = 2.0*Resolution;
        mp.textureHeight = 2.0*Resolution;
        
        map = new DefaultMapContext();
        //mp.setMapArea(mc[0].asCollectionDataStore().getCollection().getBounds());
        setMap(mc[0]);
        mp.setHeightMap(asg);
        info.setText("3D Map Pane ... calculating normals");
        mp.init();
        p.setDividerLocation(0.80);
        splitPane.setDividerLocation(0.70);
        finished = true;
        tree.setVisible(true);
        panel.repaint();
        mp.repaint();
        info.setText("ready");
        mp.invalidate();
        mp.repaint();
    }
    
    class ShapeExport extends AbstractAction {
        private ImageIcon icon;
        
        public ShapeExport() {
            putValue(SHORT_DESCRIPTION,
                    "Export actual map to ESRI shape format");
            URL url = this.getClass().getResource("resources/export.gif");
            icon = new ImageIcon(url);
            putValue(Action.SMALL_ICON, icon);
        }
        
        public void actionPerformed(ActionEvent e) {
            ShapeTool expPanel;
            try {
                int idx;
                if (last == null || last.isRoot()) idx = 0;
                else if (last.isLeaf()) idx = top.getIndex(last.getParent());
                else idx = top.getIndex(last);
                expPanel = new ShapeTool(mc[idx].asCollectionDataStore(),
                        baseShape, mc[idx].getDesc(), p, treeView);
                p.setTopComponent(expPanel);
                p.setDividerLocation(0.80);
            } catch (Exception e1) {}
        }
    }
    
    protected Feature getSelectedFeature() {
        return this.selectedF;
    }
    
    private void setMap(MapCollection mc) throws Exception {
        map = mc.getMapContext();
        //mp.addMouseListener(this);
        mp.setHeightMap(asg);
        mp.setContext(map);
    }
    
    public Style getStyle(FeatureSource fs, int i) {
        PointSymbolizer ps = null;
        LineSymbolizer ls = null;
        PolygonSymbolizer ms = null;
        StyleBuilder sb = new StyleBuilder();
        Style style = null;
        Class geoType = fs.getSchema().getDefaultGeometry().getType();
        if (geoType.equals(LineString.class)
        || geoType.equals(MultiLineString.class)) {
            ls = sb.createLineSymbolizer(Color.decode("#"
                    + shapeColors.getValue()[i]), 2);
            style = sb.createStyle(ls);
        } else if (geoType.equals(Point.class)
        || geoType.equals(MultiPoint.class)) {
            Mark pointMarker = sb.createMark(StyleBuilder.MARK_SQUARE, Color
                    .decode("#" + shapeColors.getValue()[i]));
            ps = sb.createPointSymbolizer(sb.createGraphic(null, pointMarker,
                    null));
            style = sb.createStyle(ps);
        } else if (geoType.equals(Polygon.class)
        || geoType.equals(MultiPolygon.class)) {
            ms = sb.createPolygonSymbolizer(Color.decode("#"
                    + shapeColors.getValue()[i]));
            style = sb.createStyle(ms);
        }
        return style;
    }
    
    public void mouseClicked(MouseEvent e) {
        
    }
    
    public void mouseEntered(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }
    
    public void mouseExited(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }
    
    public void mousePressed(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }
    
    public void mouseReleased(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }
    
    class MyLayerIcon implements Icon {
        private int idx;
        
        public MyLayerIcon(Integer idx) {
            this.idx = idx - numOfParams;
        }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(0, 0, 20, 13, 5, 5);
            Class geoType = optLayers[idx].getFeatureSource().getSchema()
            .getDefaultGeometry().getType();
            g2d.setColor(Color.decode("#" + shapeColors.getValue()[idx]));
            if (geoType.equals(LineString.class)
            || geoType.equals(MultiLineString.class)) {
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(4, 4, 13, 6);
                g2d.drawLine(13, 6, 15, 9);
            } else if (geoType.equals(Point.class)
            || geoType.equals(MultiPoint.class)) {
                g2d.fillRect(4, 4, 4, 4);
                g2d.fillRect(13, 6, 4, 4);
            } else if (geoType.equals(Polygon.class)
            || geoType.equals(MultiPolygon.class)) {
                int[] xPoints = new int[] { 2, 5, 9, 17 };
                int[] yPoints = new int[] { 3, 10, 12, 2 };
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
    
    class MyIcon implements Icon {
        private Integer a, b;
        
        public MyIcon(Integer a, Integer b) {
            this.a = a;
            this.b = b;
        }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (finished) {
                g.drawRect(1, 1, 19, 14);
                g.setColor(Color.BLACK);
                g.setColor((Color) mc[a].getColors()[mc[a].getRanges().length-b-1]);
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
    
    public class NodeRenderer extends DefaultTreeCellRenderer {
/*        private ImageIcon iconProject = new ImageIcon(ClassLoader
                .getSystemResource("resources/images/root.png"));
 
        private ImageIcon iconRange = new ImageIcon(ClassLoader
                .getSystemResource("resources/images/map.png"));*/
        
        private Icon blatt;
        
        private Icon blatt2;
        
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            
            super.getTreeCellRendererComponent(tree, value, sel, expanded,
                    leaf,
                    
                    row, hasFocus);
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            DefaultMutableTreeNode parentnode = (DefaultMutableTreeNode) node
                    .getParent();
            int idxToParentNode = 0;
            int idxToTopNode = 0;
            if (!node.isRoot()) {
                idxToParentNode = parentnode.getIndex(node);
                idxToTopNode = top.getIndex(parentnode);
            }
            
            if (node.isRoot()) {
                //this.setIcon(iconProject);
            }
            if (!leaf && !node.isRoot()) {
                //this.setIcon(iconRange);
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
        
        public GISPanel() throws Exception {
            this.setLayout(new BorderLayout());
            
            top = new DefaultMutableTreeNode("Map List");
            tree = new JTree(top);
            tree.setCellRenderer(new NodeRenderer());
            tree.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
                            .getLastSelectedPathComponent();
                    last = node;
                    int idxToTopNode;
                    if (node.isLeaf())
                        idxToTopNode = top.getIndex(node.getParent());
                    else
                        idxToTopNode = top.getIndex(node);
                    if (e.getClickCount() == 1
                            & (top.isNodeChild(node) || node.isLeaf())) {
                        
                        try {
                            setMap(mc[idxToTopNode]);
                            //mp.setReset(true);
                            mp.repaint();
                            
                            if (last == null || last.isRoot())
                                infoidx = 0;
                            else if (last.isLeaf())
                                infoidx = top.getIndex(last.getParent());
                            else
                                infoidx = top.getIndex(last);
                        } catch (Exception e1) {
                        }
                    }
                }
            });
                        
            // create info pane
            info = new JTextPane();
            info.setEditable(false);
            info.setText("3D Map Pane has not been started yet, please wait ..");
                        
            // create slider panel
            JSlider js = new JSlider();
            js.setMinimum(-20);
            js.setMaximum(20);
            js.setValue(0);
            js.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    JSlider source = (JSlider)e.getSource();
                    mp.hScale = (float)Math.exp(((double)source.getValue())/10.0);
                }
            });

            JPanel sliderPanel = new JPanel();
            sliderPanel.setBorder(BorderFactory.createTitledBorder("Vertical exaggeration"));
            sliderPanel.add(js);

            // create panel to display info and slider
            JPanel miscPanel = new JPanel();
            miscPanel.setLayout(new BorderLayout());
            miscPanel.add(sliderPanel, BorderLayout.NORTH);
            miscPanel.add(info, BorderLayout.CENTER);            

            treeView = new JScrollPane(tree);
            
            // create split pane to display tree and misc panel
            p = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            p.setTopComponent(treeView);
            p.setBottomComponent(miscPanel);
            
            
            boolean light2 = true;
            int Resolution = 256;
            
            if (resolution != null) {
                Resolution = resolution.getValue();
            }
            if (light != null) {
                light2 = light.getValue();
            }
            if (mp == null)
                mp = new Styled3DMapPane();
            mp.light = light2;
            mp.setHeightMap(asg);
            mp.xRes = Resolution;
            mp.yRes = Resolution;
            mp.textureWidth = 2.0*Resolution;
            mp.textureHeight = 2.0*Resolution;
            
            //mp.init();
            
            
            URL url = this.getClass().getResource("resources/select.gif");
            ImageIcon iconSelect = new ImageIcon(url);
            /*JButton selectB = new JButton(iconSelect);
            selectB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    mp.setState(JMapPane.Select);
                }
            });*/
            
            Action export = new ShapeExport();
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setRightComponent(p);
            splitPane.setLeftComponent(mp);
            //this.add(jtb, BorderLayout.NORTH);
            this.add(splitPane);
        }
    }
    
    @Override
    public JPanel getPanel() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                panel = new GISPanel();
            }
        } catch (Exception e) {
            System.out.println("Error during initialization of GIS Panel-->" + e.toString());
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
    
}