/*
 * ProfileControlPanel.java
 *
 * Created on October 21, 2008, 10:50 AM
 */
package gw.ui.layerproperies;

import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.view.OrbitView;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import gw.api.Events;

/**
 *
 * @author  od
 */
public class ProfileLayerProperties extends javax.swing.JPanel {

    TerrainProfileLayer layer;
    OrbitView view;

    /** Creates new form ProfileControlPanel */
    public ProfileLayerProperties(TerrainProfileLayer layer, OrbitView view) {
        initComponents();
        this.layer = layer;
        this.view = view;
        setupComponents(layer);
    }

    private void setupComponents(final TerrainProfileLayer layer) {

        jSlider1.setValue((int) (layer.getOpacity() * 100));
        jSlider1.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                int value = slider.getValue();
                layer.setOpacity((double) value / 100);
                update();
            }
        });

        showEyeCheck.setSelected(layer.getShowEyePosition());
        showEyeCheck.setEnabled(layer.getFollow().equals(TerrainProfileLayer.FOLLOW_EYE));
        keepProps.setSelected(layer.getKeepProportions());
        zeroBased.setSelected(layer.getZeroBased());


        keepProps.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                layer.setKeepProportions(((JCheckBox) e.getSource()).isSelected());
                update();
            }
        });

        cbSize.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String size = (String) cbSize.getSelectedItem();
                if (size.equals(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_Small"))) {
                    layer.setSize(new Dimension(250, 100));
                } else if (size.equals(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_Medium"))) {
                    layer.setSize(new Dimension(450, 140));
                } else if (size.equals(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_Large"))) {
                    layer.setSize(new Dimension(655, 240));
                }
                update();
            }
        });

        cbFollow.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String size = (String) cbFollow.getSelectedItem();
                if (size.equals(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_View"))) {
                    layer.setFollow(TerrainProfileLayer.FOLLOW_VIEW);
                    showEyeCheck.setEnabled(false);
                    lengthSlider.setEnabled(true);
                } else if (size.equals(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_Cursor"))) {
                    layer.setFollow(TerrainProfileLayer.FOLLOW_CURSOR);
                    showEyeCheck.setEnabled(false);
                    lengthSlider.setEnabled(true);
                } else if (size.equals(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_Eye"))) {
                    layer.setFollow(TerrainProfileLayer.FOLLOW_EYE);
                    showEyeCheck.setEnabled(true);
                    lengthSlider.setEnabled(true);
                } else if (size.equals(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_None"))) {
                    layer.setFollow(TerrainProfileLayer.FOLLOW_NONE);
                    showEyeCheck.setEnabled(false);
                    lengthSlider.setEnabled(false);
                } else if (size.equals(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_Object"))) {
                    layer.setFollow(TerrainProfileLayer.FOLLOW_OBJECT);
                    showEyeCheck.setEnabled(true);
                    lengthSlider.setEnabled(true);
                    layer.setObjectPosition(view.getEyePosition());
                    layer.setObjectHeading(view.getHeading());
                }
                update();
            }
        });

        showEyeCheck.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                layer.setShowEyePosition(((JCheckBox) e.getSource()).isSelected());
                update();
            }
        });

        zeroBased.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                layer.setZeroBased(((JCheckBox) e.getSource()).isSelected());
                update();
            }
        });

        lengthSlider.setValue((int) (layer.getProfileLenghtFactor() * 100));
        lengthSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider s = (JSlider) e.getSource();
                layer.setProfileLengthFactor((double) s.getValue() / 100);
                update();
            }
        });
    }

    private void update() {
        firePropertyChange(Events.GF_UPDATE, null, null);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cbSize = new javax.swing.JComboBox();
        cbFollow = new javax.swing.JComboBox();
        lengthSlider = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        keepProps = new javax.swing.JCheckBox();
        showEyeCheck = new javax.swing.JCheckBox();
        zeroBased = new javax.swing.JCheckBox();
        jSlider1 = new javax.swing.JSlider();
        jLabel4 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("gw/resources/language"); // NOI18N
        jLabel1.setText(bundle.getString("L_Display")); // NOI18N

        jLabel2.setText(bundle.getString("L_Follow")); // NOI18N

        cbSize.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Small", "Medium", "Large" }));

        cbFollow.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "View", "Cursor", "Eye", "None", "Object" }));

        lengthSlider.setMajorTickSpacing(1);
        lengthSlider.setMaximum(120);
        lengthSlider.setMinimum(1);
        lengthSlider.setMinorTickSpacing(1);
        lengthSlider.setValue(5);

        jLabel3.setText("Profile  length:");

        keepProps.setText(bundle.getString("L_Keep_proportions")); // NOI18N

        showEyeCheck.setText(bundle.getString("L_Show_eye")); // NOI18N

        zeroBased.setText(bundle.getString("L_Zero_based")); // NOI18N

        jSlider1.setMajorTickSpacing(25);
        jSlider1.setMinorTickSpacing(5);
        jSlider1.setPaintLabels(true);
        jSlider1.setPaintTicks(true);
        jSlider1.setPaintTrack(false);

        jLabel4.setText(bundle.getString("L_Opacity")); // NOI18N

        jSeparator3.setBackground(new java.awt.Color(208, 208, 191));
        jSeparator3.setForeground(new java.awt.Color(236, 233, 216));
        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator3.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(showEyeCheck)
                            .addComponent(zeroBased))
                        .addGap(48, 48, 48))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lengthSlider, 0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cbFollow, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cbSize, 0, 86, Short.MAX_VALUE))))
                        .addGap(18, 18, 18)
                        .addComponent(keepProps, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel4)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(cbSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1)
                                    .addComponent(keepProps))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(cbFollow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addComponent(showEyeCheck)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(zeroBased)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lengthSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jSeparator3))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addGap(2, 2, 2)
                            .addComponent(jLabel4)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbFollow;
    private javax.swing.JComboBox cbSize;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JCheckBox keepProps;
    private javax.swing.JSlider lengthSlider;
    private javax.swing.JCheckBox showEyeCheck;
    private javax.swing.JCheckBox zeroBased;
    // End of variables declaration//GEN-END:variables
}
