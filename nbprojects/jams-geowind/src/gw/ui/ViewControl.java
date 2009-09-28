/*
 * ViewControl.java
 *
 * Created on October 21, 2008, 12:45 PM
 */
package gw.ui;

import gov.nasa.worldwind.AnaglyphSceneController;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.globes.FlatGlobe;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.SkyColorLayer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.view.BasicOrbitView;
import gov.nasa.worldwind.view.FlatOrbitView;
import gov.nasa.worldwind.view.FlyToOrbitViewStateIterator;
import gov.nasa.worldwind.view.OrbitView;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JSlider;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import gw.api.Events;
import java.util.ResourceBundle;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.jdesktop.swingx.StackLayout;
import org.jdesktop.xswingx.JXSearchField;
import org.jdesktop.xswingx.JXSearchField.SearchMode;

/**
 *
 * @author  od
 */
public class ViewControl extends javax.swing.JPanel {

    static final String LBL_LatLong = "Lat/Long";
    static final String LBL_Mercator = "Mercator";
    static final String LBL_Sinusoidal = "Sinusoidal";
    static final String LBL_ModSinusoidal = "Mod Sinusoidal";
//
    SpinnerNumberModel latModel = new SpinnerNumberModel(40.55d, -90d, 90d, 5d);
    SpinnerNumberModel longModel = new SpinnerNumberModel(-105.1d, -180d, 180d, 5d);
    SpinnerNumberModel altModel = new SpinnerNumberModel(43, 5, 20000, 10);
    PropertyChangeListener viewListener;
    boolean suspendEvents = false;
//
    Earth roundEarthModel = new Earth();
    EarthFlat flatEarthModel = new EarthFlat();
    FlatOrbitView flatOrbitView = new FlatOrbitView();
    BasicOrbitView orbitView = new BasicOrbitView();
    AnaglyphSceneController asc;
//
    Position initPos;
    ResourceBundle bundle = ResourceBundle.getBundle("gw/resources/language");

    private static class TN {

        Toponym tn;
        String msg;

        TN(Toponym tn) {
            this.tn = tn;
        }

        TN(String s) {
            msg = s;
        }

        @Override
        public String toString() {
            return tn != null ? (tn.getName() + ", " + tn.getCountryCode()) : msg;
        }
    }

    /** Creates new form ViewControl */
    public ViewControl(WorldWindow ww) {
        asc = (AnaglyphSceneController) ww.getSceneController();
        initComponents();
        setupComponents(ww);
    }

    private void doSearch(final String loc) {

        searchResult.setEnabled(false);
        waitingLabel.setVisible(true);

        SwingWorker w = new SwingWorker<Object, Object>() {

            @Override
            public Object doInBackground() {


                ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
                searchCriteria.setQ(loc);
                searchCriteria.setMaxRows(50);
                final List<TN> l = new ArrayList<TN>();
                try {
                    ToponymSearchResult res = WebService.search(searchCriteria);
                    if (res.getTotalResultsCount() == 0) {
                        l.add(new TN(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_No_Results.")));
                    } else {
                        for (Toponym t : res.getToponyms()) {
                            l.add(new TN(t));
                        }
                        searchResult.setEnabled(true);
                    }
                } catch (Exception ex) {
                    l.add(new TN(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_No_Service")));
                }
                searchResult.setModel(new AbstractListModel() {

                    @Override
                    public int getSize() {
                        return l.size();
                    }

                    @Override
                    public Object getElementAt(int index) {
                        return l.get(index);
                    }
                });
                return null;
            }

            @Override
            protected void done() {
                waitingLabel.setVisible(false);
            }
        };
        w.execute();
    }

    static class ImageIOFilter extends FileFilter {

//        List<String> fmt = Arrays.asList(ImageIO.getReaderFileSuffixes());
        List<String> fmt = Arrays.asList("gif", "png", "tif", "bmp", "jpg");

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String ext = ext(f);
            if (ext != null && fmt.contains(ext)) {
                return true;
            }
            return false;
        }

        //The description of this filter
        @Override
        public String getDescription() {
            return "Image Files (*.gif *.png *.tif *.bmp *.jpg)";
        }
    }

    private static String ext(File f) {
        return f.toString().substring(f.toString().lastIndexOf('.') + 1);
    }

    private void setupComponents(final WorldWindow ww) {

        picButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new ImageIOFilter());
                fc.setDialogTitle("Save Snapshot Image");
                int returnVal = fc.showSaveDialog((Component) ww);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    saveComponentAsImage((Component) ww, file);
                }
            }
        });

        jPanel7.setLayout(new StackLayout());
        jPanel7.add(jScrollPane1, StackLayout.TOP);
        jPanel7.add(waitingLabel, StackLayout.TOP);
        waitingLabel.setVisible(false);

        xSearchField.setSearchMode(SearchMode.REGULAR);
        xSearchField.setLayoutStyle(JXSearchField.LayoutStyle.MAC);
        xSearchField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!xSearchField.getText().isEmpty()) {
                    doSearch(xSearchField.getText());
                }
            }
        });

        searchResult.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (!searchResult.isEnabled()) {
                    return;
                }
                int index = searchResult.locationToIndex(e.getPoint());
                if (index != -1 && e.getClickCount() == 2) {
                    Toponym cb = ((TN) searchResult.getModel().getElementAt(index)).tn;
                    fly(Position.fromDegrees(cb.getLatitude(), cb.getLongitude(), 0), 30000, ww);
                }
            }
        });

        // Adjust north south 
        northButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                OrbitView vw = (OrbitView) ww.getView();
                FlyToOrbitViewStateIterator pan = FlyToOrbitViewStateIterator.createPanToIterator(
                        vw,
                        ww.getModel().getGlobe(),
                        vw.getCenterPosition(),
                        Angle.ZERO, Angle.ZERO, vw.getZoom());
                vw.applyStateIterator(pan);
            }
        });

        // call back listener for 
        viewListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                suspendEvents = true;
                OrbitView view = (OrbitView) ww.getView();
                pitchSlider.setValue((int) view.getPitch().degrees);
                headingSlider.setValue((int) view.getHeading().degrees);
                fovSlider.setValue((int) view.getFieldOfView().degrees);
                suspendEvents = false;
            }
        };

        orbitView.addPropertyChangeListener(viewListener);
        flatOrbitView.addPropertyChangeListener(viewListener);

        ww.getModel().setGlobe(roundEarthModel);
        ww.setView(orbitView);
        ww.setInputHandler(ww.getInputHandler());

        // Location 
        latSpinner.setModel(latModel);
        longSpinner.setModel(longModel);
        altSpinner.setModel(altModel);

        goButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fly(Position.fromDegrees(latModel.getNumber().doubleValue(), longModel.getNumber().doubleValue(), 0),
                        altModel.getNumber().intValue() * 1000, ww);
            }
        });

        // reset to (almost original view
        resetButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fly(initPos, 19234000, ww);
            }
        });

        // Exaggeration
        exaggSlider.setValue((int) asc.getVerticalExaggeration());
        exaggSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider s = (JSlider) e.getSource();
                if (!s.getValueIsAdjusting()) {
                    asc.setVerticalExaggeration((double) s.getValue());
                    update();
                }
            }
        });

        // Anaglyph
        angleSlider.setValue((int) (asc.getFocusAngle().degrees * 10));
        angleSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider s = (JSlider) e.getSource();
                if (!s.getValueIsAdjusting()) {
                    Angle focusAngle = Angle.fromDegrees((double) s.getValue() / 10);
                    asc.setFocusAngle(focusAngle);
                    update();
                }
            }
        });

        anaglyphCheck.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean sel = ((JCheckBox) e.getSource()).isSelected();
                angleSlider.setEnabled(sel);
                angleLabel.setEnabled(sel);
                asc.setDisplayMode(sel ? AnaglyphSceneController.DISPLAY_MODE_STEREO
                        : AnaglyphSceneController.DISPLAY_MODE_MONO);
                update();
            }
        });

        // Position
        OrbitView view = (OrbitView) ww.getView();
        pitchSlider.setValue((int) view.getPitch().degrees);
        headingSlider.setValue((int) view.getHeading().degrees);
        fovSlider.setValue((int) view.getFieldOfView().degrees);

        ChangeListener c = new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                if (!suspendEvents) {
                    OrbitView view = (OrbitView) ww.getView();

                    // Stop iterators first
                    view.stopStateIterators();

                    // Save current eye position
                    Position pos = view.getEyePosition();

                    // Set view heading, pitch and fov
                    view.setHeading(Angle.fromDegrees(headingSlider.getValue()));
                    view.setPitch(Angle.fromDegrees(pitchSlider.getValue()));
                    view.setFieldOfView(Angle.fromDegrees(fovSlider.getValue()));
                    view.setZoom(0);

                    // Restore eye position
                    view.setCenterPosition(pos);

                    // Redraw
                    update();
                }
            }
        };

        pitchSlider.addChangeListener(c);
        headingSlider.addChangeListener(c);
        fovSlider.addChangeListener(c);

        // Globe 
        flatCheckbox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean sel = ((JCheckBox) e.getSource()).isSelected();
                projCombo.setEnabled(sel);
                projLabel.setEnabled(sel);
                setFlatProjection(sel, ww);
            }
        });

        projCombo.setModel(new DefaultComboBoxModel(new String[]{
                    LBL_LatLong, LBL_Mercator, LBL_Sinusoidal, LBL_ModSinusoidal
                }));

        projCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String item = (String) projCombo.getSelectedItem();
                String projection = FlatGlobe.PROJECTION_LAT_LON;
                if (item.equals(LBL_LatLong)) {
                    projection = FlatGlobe.PROJECTION_LAT_LON;
                } else if (item.equals(LBL_Mercator)) {
                    projection = FlatGlobe.PROJECTION_MERCATOR;
                } else if (item.equals(LBL_Sinusoidal)) {
                    projection = FlatGlobe.PROJECTION_SINUSOIDAL;
                } else if (item.equals(LBL_ModSinusoidal)) {
                    projection = FlatGlobe.PROJECTION_MODIFIED_SINUSOIDAL;
                }
                flatEarthModel.setProjection(projection);
                update();
            }
        });
        initPos = orbitView.getCenterPosition();
    }

    private void update() {
        firePropertyChange(Events.GF_UPDATE, null, null);
    }

    /**
     * 
     * @param lat [deg]
     * @param lon [deg]
     * @param alt [m]
     * @param ww
     */
    private void fly(Position pos, int alt, WorldWindow ww) {
        BasicOrbitView vw = (BasicOrbitView) ww.getView();
        FlyToOrbitViewStateIterator pan = FlyToOrbitViewStateIterator.createPanToIterator(
                vw, ww.getModel().getGlobe(), pos,
                Angle.ZERO, Angle.ZERO, alt);
        vw.applyStateIterator(pan);
    }

    private void setFlatProjection(boolean flat, WorldWindow ww) {
        boolean isFlat = ww.getModel().getGlobe() instanceof FlatGlobe;
        if (flat == isFlat) {
            return;
        }
        if (!flat) {
            //     orbitView.setCenterPosition(flatOrbitView.getCenterPosition());
            orbitView.setEyePosition(flatOrbitView.getCurrentEyePosition());
            orbitView.setZoom(flatOrbitView.getZoom());
            orbitView.setHeading(flatOrbitView.getHeading());
            orbitView.setFarClipDistance(flatOrbitView.getFarClipDistance());
            orbitView.setNearClipDistance(flatOrbitView.getNearClipDistance());
            orbitView.setFieldOfView(flatOrbitView.getFieldOfView());
            //     orbitView.setPitch(flatOrbitView.getPitch());

            LayerList layers = ww.getModel().getLayers();
            for (int i = 0; i < layers.size(); i++) {
                if (layers.get(i) instanceof SkyColorLayer) {
                    layers.set(i, new SkyGradientLayer());
                }
            }
            ww.getModel().setGlobe(roundEarthModel);
            ww.setView(orbitView);
            ww.setInputHandler(ww.getInputHandler());
        } else {
//            flatOrbitView.setCenterPosition(orbitView.getCenterPosition());
            flatOrbitView.setEyePosition(orbitView.getCurrentEyePosition());
            flatOrbitView.setZoom(orbitView.getZoom());
            flatOrbitView.setHeading(orbitView.getHeading());
            flatOrbitView.setFarClipDistance(orbitView.getFarClipDistance());
            flatOrbitView.setNearClipDistance(orbitView.getNearClipDistance());
            flatOrbitView.setFieldOfView(orbitView.getFieldOfView());
            //   flatOrbitView.setPitch(orbitView.getPitch());

            LayerList layers = ww.getModel().getLayers();
            for (int i = 0; i < layers.size(); i++) {
                if (layers.get(i) instanceof SkyGradientLayer) {
                    layers.set(i, new SkyColorLayer());
                }
            }
            ww.getModel().setGlobe(flatEarthModel);
            ww.setView(flatOrbitView);
            ww.setInputHandler(ww.getInputHandler());
        }
    }

    private void saveComponentAsImage(Component comp, File file) {
        Dimension size = comp.getSize();
        BufferedImage image = new BufferedImage(size.width, size.height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        comp.paint(g2);
        g2.dispose();
        try {
            ImageIO.write(image, ext(file), file);
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
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

        jScrollPane1 = new javax.swing.JScrollPane();
        searchResult = new javax.swing.JList();
        waitingLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        anaglyphCheck = new javax.swing.JCheckBox();
        angleSlider = new javax.swing.JSlider();
        angleLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        exaggSlider = new javax.swing.JSlider();
        jLabel2 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        latSpinner = new javax.swing.JSpinner();
        longSpinner = new javax.swing.JSpinner();
        altSpinner = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        goButton = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        flatCheckbox = new javax.swing.JCheckBox();
        projCombo = new javax.swing.JComboBox();
        projLabel = new javax.swing.JLabel();
        jToolBar1 = new javax.swing.JToolBar();
        northButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        picButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        pitchSlider = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        headingSlider = new javax.swing.JSlider();
        jLabel7 = new javax.swing.JLabel();
        fovSlider = new javax.swing.JSlider();
        jPanel6 = new javax.swing.JPanel();
        xSearchField = new org.jdesktop.xswingx.JXSearchField();
        jPanel7 = new javax.swing.JPanel();

        searchResult.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(searchResult);

        waitingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        waitingLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gw/resources/ajax-loader.gif"))); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " Anaglyph ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BOTTOM));
        jPanel1.setOpaque(false);

        anaglyphCheck.setText("Stereo");
        anaglyphCheck.setOpaque(false);

        angleSlider.setMajorTickSpacing(10);
        angleSlider.setMaximum(50);
        angleSlider.setMinorTickSpacing(5);
        angleSlider.setPaintLabels(true);
        angleSlider.setPaintTicks(true);
        angleSlider.setValue(20);
        angleSlider.setEnabled(false);
        angleSlider.setOpaque(false);

        angleLabel.setText(bundle.getString("L_FocusAngle") + " [1/10 °]:");
        angleLabel.setEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(angleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                    .addComponent(angleSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                    .addComponent(anaglyphCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(angleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(angleSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(anaglyphCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("L_EXAGGERATION"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BOTTOM));
        jPanel3.setOpaque(false);

        exaggSlider.setMajorTickSpacing(1);
        exaggSlider.setMaximum(4);
        exaggSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        exaggSlider.setPaintLabels(true);
        exaggSlider.setPaintTicks(true);
        exaggSlider.setValue(1);
        exaggSlider.setOpaque(false);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("gw/resources/language"); // NOI18N
        jLabel2.setText(bundle.getString("L_Factor")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(exaggSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(39, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exaggSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " " + bundle.getString("L_LOCATION") + " ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BOTTOM));
        jPanel4.setOpaque(false);

        jLabel4.setText("Lat [°]:");

        jLabel6.setText(bundle.getString("L_Altitude")); // NOI18N

        goButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gw/resources/golocation.png"))); // NOI18N
        goButton.setContentAreaFilled(false);

        jLabel8.setText("Long [°]:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel6)
                    .addComponent(jLabel4))
                .addGap(13, 13, 13)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(latSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                    .addComponent(longSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                    .addComponent(altSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(57, 57, 57)
                .addComponent(goButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(latSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(longSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(altSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(goButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(42, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " " + bundle.getString("L_GLOBE") + " ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BOTTOM));

        flatCheckbox.setText(bundle.getString("L_Flat"));

        projCombo.setEnabled(false);

        projLabel.setText(bundle.getString("L_Projection")); // NOI18N
        projLabel.setEnabled(false);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        northButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gw/resources/compass.png"))); // NOI18N
        northButton.setToolTipText(bundle.getString("L_Adjust")); // NOI18N
        jToolBar1.add(northButton);

        resetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gw/resources/globe-16x16.png"))); // NOI18N
        resetButton.setToolTipText(bundle.getString("L_Reset2Orig")); // NOI18N
        resetButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(resetButton);

        picButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gw/resources/camera.png"))); // NOI18N
        picButton.setToolTipText(bundle.getString("L_Take_Snapshot")); // NOI18N
        jToolBar1.add(picButton);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(projLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(flatCheckbox)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(projCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(flatCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(projLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(projCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "  Position  ", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BOTTOM));

        pitchSlider.setMaximum(90);
        pitchSlider.setValue(0);

        jLabel1.setText(bundle.getString("L_Pitch")); // NOI18N

        jLabel3.setText(bundle.getString("L_Heading")); // NOI18N

        headingSlider.setMaximum(180);
        headingSlider.setMinimum(-180);
        headingSlider.setValue(0);

        jLabel7.setText(bundle.getString("L_Field")); // NOI18N

        fovSlider.setMaximum(120);
        fovSlider.setMinimum(10);
        fovSlider.setValue(45);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel1)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(headingSlider, 0, 0, Short.MAX_VALUE)
                    .addComponent(fovSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                    .addComponent(pitchSlider, 0, 0, Short.MAX_VALUE)))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pitchSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(headingSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(fovSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " " + bundle.getString("L_Search") + " ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BOTTOM));

        xSearchField.setInstantSearchDelay(500);
        xSearchField.setToolTipText(bundle.getString("L_Search_Geonames")); // NOI18N

        jPanel7.setLayout(null);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(xSearchField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
            .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(xSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(57, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner altSpinner;
    private javax.swing.JCheckBox anaglyphCheck;
    private javax.swing.JLabel angleLabel;
    private javax.swing.JSlider angleSlider;
    private javax.swing.JSlider exaggSlider;
    private javax.swing.JCheckBox flatCheckbox;
    private javax.swing.JSlider fovSlider;
    private javax.swing.JButton goButton;
    private javax.swing.JSlider headingSlider;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JSpinner latSpinner;
    private javax.swing.JSpinner longSpinner;
    private javax.swing.JButton northButton;
    private javax.swing.JButton picButton;
    private javax.swing.JSlider pitchSlider;
    private javax.swing.JComboBox projCombo;
    private javax.swing.JLabel projLabel;
    private javax.swing.JButton resetButton;
    private javax.swing.JList searchResult;
    private javax.swing.JLabel waitingLabel;
    private org.jdesktop.xswingx.JXSearchField xSearchField;
    // End of variables declaration//GEN-END:variables
}
