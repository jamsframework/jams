/*
 * WMSControlPanel.java
 *
 * Created on October 23, 2008, 1:58 PM
 */
package gw.ui;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.wms.Capabilities;
import gov.nasa.worldwind.wms.CapabilitiesRequest;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.ListModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListDataListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author  od
 */
public class WMSControl extends javax.swing.JPanel {

    private static final String[] servers = new String[]{
        "http://wms.jpl.nasa.gov/wms.cgi",
        "http://neowms.sci.gsfc.nasa.gov/wms/wms",
        "http://gisdata.usgs.net/servlet/com.esri.wms.Esrimap?WMTVER=1.1.0&ServiceName=USGS_WMS_LANDSAT7&REQUEST=capabilities",
        "http://www.mapsherpa.com/cgi-bin/wms_iodra?SERVICE=wms&VERSION=1.1.1&REQUEST=getcapabilities",
        "http://columbo.nrlssc.navy.mil/ogcwms/servlet/WMSServlet/USGS_Rocky_Mountain_Mapping_Center.wms?SERVICE=WMS&REQUEST=GetCapabilities",
        "http://columbo.nrlssc.navy.mil/ogcwms/servlet/WMSServlet/USDA_Forest_Service_Geodata_Clearinghouse.wms?SERVICE=WMS&REQUEST=GetCapabilities",
        "http://columbo.nrlssc.navy.mil/ogcwms/servlet/WMSServlet/USDA_NRCS_Maps.wms?SERVICE=WMS&REQUEST=GetCapabilities"
    };
    ActionListener connectL;
    ActionListener cancelL;
    SwingWorker w;

    /** Creates new form WMSControlPanel */
    public WMSControl(FancyPanel wwp) {
        initComponents();
        setupComponents(wwp);
    }

    public void setupComponents(final FancyPanel wwp) {

        for (String s : servers) {
            serverCombo.addItem(s);
        }

        cancelL = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (!w.isDone()) {
                    w.cancel(true);
                }
            }
        };
        connectL = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                mapList.setModel(new DefaultListModel());
                mapList.repaint();
                connectButton.setText(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_Cancel"));
                serverCombo.setEnabled(false);
                connectButton.removeActionListener(connectL);
                connectButton.addActionListener(cancelL);
                numberLabel.setText("");
                progressLabel.setIcon(new ImageIcon(
                        getClass().getResource("/gw/resources/ajax-loader.gif"))); // NOI18N

                w = new SwingWorker<Object, Object>() {

                    @Override
                    public Object doInBackground() {
                        String server = (String) serverCombo.getSelectedItem();
                        Capabilities caps;
                        try {
                            URI serverURI = new URI(server.trim());
                            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                            docBuilderFactory.setNamespaceAware(true);
                            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                            if (isCancelled()) {
                                return "";                           
                            }
                            CapabilitiesRequest req = new CapabilitiesRequest(serverURI);
                            if (isCancelled()) {
                                return null;
                            }
                            Document doc = docBuilder.parse(req.toString());
                            if (isCancelled()) {
                                return null;
                            }
                            caps = Capabilities.parse(doc);
                            if (isCancelled()) {
                                return null;
                            }
                            Element[] namedLayerCaps = caps.getNamedLayers();
                            if (namedLayerCaps == null) {
                                return null;
                            }

                            final List<LayerInfo> l = new ArrayList<LayerInfo>();
                            ListModel m = new ListModel() {

                                @Override
                                public int getSize() {
                                    return l.size();
                                }

                                @Override
                                public Object getElementAt(int index) {
                                    return l.get(index);
                                }

                                @Override
                                public void addListDataListener(ListDataListener l) {
                                }

                                @Override
                                public void removeListDataListener(ListDataListener l) {
                                }
                            };

                            int i = 0;
                            for (Element layerCaps : namedLayerCaps) {
                                Element[] styles = caps.getLayerStyles(layerCaps);
                                if (styles == null) {
                                    LayerInfo layerInfo = createLayerInfo(caps, layerCaps, null);
                                    l.add(layerInfo);
                                    numberLabel.setText(Integer.toString(i++));
                                    if (isCancelled()) {
                                        return null;
                                    }
                                } else {
                                    for (Element style : styles) {
                                        LayerInfo layerInfo = createLayerInfo(caps, layerCaps, style);
                                        l.add(layerInfo);
                                        numberLabel.setText(Integer.toString(i++));
                                        if (isCancelled()) {
                                            return null;
                                        }
                                    }
                                }
                            }
                            Collections.sort(l);
                            numberLabel.setText(m.getSize() + java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_Layer_from") + serverURI.getHost());
                            mapList.setModel(m);
                            mapList.repaint();
                        } catch (Exception E) {
                            E.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        if (isCancelled()) {
                            mapList.removeAll();
                        }
                        connectButton.setText("Connect ...");
                        serverCombo.setEnabled(true);
                        connectButton.removeActionListener(cancelL);
                        connectButton.addActionListener(connectL);
                        progressLabel.setIcon(null); // NOI18N
                    }
                };
                w.execute();
            }
        };

        connectButton.addActionListener(connectL);

        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] o = mapList.getSelectedValues();
                for (Object elem : o) {
                    LayerInfo li = (LayerInfo) elem;
                    Layer l = new WMSTiledImageLayer(li.caps, li.params);

                    l.setEnabled(false);
                    l.setEnabled(true);
                    l.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000);
                    l.setValue(AVKey.URL_READ_TIMEOUT, 30000);
                    l.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);
//                    System.out.println(li.caps.getAbstract());
//                    System.out.println(li.caps.getAccessConstraints());
//                    System.out.println(li.caps.getContactOrganization());
//                    System.out.println(li.caps.getContactPerson());
//                    System.out.println(li.caps.getFees());
//                    System.out.println(li.caps.getOnlineResource());
//                    System.out.println(li.caps.getVendorSpecificCapabilities());
//                    System.out.println(li.caps.getVersion());
//                    System.out.println(li.params.getEntries());

                    wwp.addLayer(l);

                }
                mapList.clearSelection();
            }
        });

    }

    private LayerInfo createLayerInfo(
            Capabilities caps, Element layerCaps, Element style) {
        AVListImpl params = new AVListImpl();
        params.setValue(AVKey.LAYER_NAMES, caps.getLayerName(layerCaps));
        if (style != null) {
            params.setValue(AVKey.STYLE_NAMES, caps.getStyleName(layerCaps, style));
        }

        params.setValue(AVKey.TITLE, makeTitle(caps, params));
        return new LayerInfo(caps, params);
    }

    private static String makeTitle(Capabilities caps, AVListImpl params) {
        String layerNames = params.getStringValue(AVKey.LAYER_NAMES);
        String styleNames = params.getStringValue(AVKey.STYLE_NAMES);
        String[] lNames = layerNames.split(",");
        String[] sNames = styleNames != null ? styleNames.split(",") : null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <
                lNames.length; i++) {
            if (sb.length() > 0) {
                sb.append(", ");
            }

            String layerName = lNames[i];
            Element layer = caps.getLayerByName(layerName);
            String layerTitle = caps.getLayerTitle(layer);
            sb.append(layerTitle != null ? layerTitle : layerName);

            if (sNames == null || sNames.length <= i) {
                continue;
            }

            String styleName = sNames[i];
            Element style = caps.getLayerStyleByName(layer, styleName);
            if (style == null) {
                continue;
            }

            sb.append(" : ");
            String styleTitle = caps.getStyleTitle(layer, style);
            sb.append(styleTitle != null ? styleTitle : styleName);
        }

        return sb.toString();
    }

    private static class LayerInfo
            implements Comparable {

        Capabilities caps;
        AVListImpl params;

        public LayerInfo(Capabilities caps, AVListImpl params) {
            this.caps = caps;
            this.params = params;
        }

        @Override
        public String toString() {
            return params.getStringValue(AVKey.TITLE);
        }

        @Override
        public int compareTo(Object o) {
            return toString().compareTo(o.toString());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        serverCombo = new javax.swing.JComboBox();
        connectButton = new javax.swing.JButton();
        progressLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        mapList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        numberLabel = new javax.swing.JLabel();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "   Step 1: Connect to  WMS URL   ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BOTTOM));
        jPanel1.setOpaque(false);

        serverCombo.setEditable(true);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("gw/resources/language"); // NOI18N
        connectButton.setText(bundle.getString("L_Connect")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(serverCombo, 0, 242, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(155, 155, 155)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connectButton)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(serverCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                .addComponent(progressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(connectButton))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "   Step 2: Select Map(s) and Add   ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BOTTOM));
        jPanel2.setOpaque(false);

        jScrollPane1.setViewportView(mapList);

        addButton.setText(bundle.getString("L_Add_Selected")); // NOI18N

        numberLabel.setText(" ");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(numberLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(addButton))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(numberLabel)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton connectButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList mapList;
    private javax.swing.JLabel numberLabel;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JComboBox serverCombo;
    // End of variables declaration//GEN-END:variables
}
