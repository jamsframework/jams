/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AttributeTableProperties.java
 *
 * Created on Jan 9, 2009, 1:16:32 PM
 */
package gw.ui.layerproperies;

import gw.api.Events;
import gw.layers.SimpleFeatureLayer;
import gw.ui.util.ProxyTableModel;
import gw.util.ColorBlend;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import javax.swing.table.TableModel;
import ngmf.io.CSProperties;
import ngmf.io.DataIO;
import ngmf.util.Tables;

/**
 *
 * @author ShoverRobot
 */
public class AttributeTableProperties extends javax.swing.JPanel {

    SimpleFeatureLayer layer;
    ProxyTableModel tm;

    /** Creates new form AttributeTableProperties */
    public AttributeTableProperties(File attrData, SimpleFeatureLayer layer, ProxyTableModel table, int index) {
        initComponents();
        setupComponents(table, index);
        this.layer = layer;
        this.tm = table;
        addAttrData(attrData);
    }

    private void setupComponents(final ProxyTableModel table, final int index) {
        maxColorChooser.setColor(table.getMaxColor(index));

        maxColorChooser.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    table.setMaxColor(maxColorChooser.getColor(), index);
                    table.setMinColor(minColorChooser.getColor(), index);
                    layer.setAttrMaxColor(maxColorChooser.getColor());
                    layer.setAttrMinColor(minColorChooser.getColor());
                    layer.redraw();
                    updateColorLegend();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                update();
            }
        });

        minColorChooser.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Color maxColor = maxColorChooser.getColor();
                Color minColor = minColorChooser.getColor();
                try {
                    table.setMaxColor(maxColor, index);
                    table.setMinColor(minColor, index);
                    layer.setAttrMaxColor(maxColor);
                    layer.setAttrMinColor(minColor);
                    layer.redraw();
                    updateColorLegend();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        updateColorLegend();
    }

    private void updateColorLegend() {
        Color maxColor = maxColorChooser.getColor();
        Color minColor = minColorChooser.getColor();
        double percentage;
        Color actColor;
        for (int i = 1; i < 10; i++) {
            percentage = 1- (i / 10.0);
            actColor = ColorBlend.mixColors(minColor, maxColor, (float)percentage);

            if (i == 1) {
                jcolorPanel1.setBackground(actColor);
            }
            if (i == 2) {
                jcolorPanel2.setBackground(actColor);
            }
            if (i == 3) {
                jcolorPanel3.setBackground(actColor);
            }
            if (i == 4) {
                jcolorPanel4.setBackground(actColor);
            }
            if (i == 5) {
                jcolorPanel5.setBackground(actColor);
            }
            if (i == 6) {
                jcolorPanel6.setBackground(actColor);
            }
            if (i == 7) {
                jcolorPanel7.setBackground(actColor);
            }
            if (i == 8) {
                jcolorPanel8.setBackground(actColor);
            }
            if (i == 9) {
                jcolorPanel9.setBackground(actColor);
            }
        }
        update();
    }

    public void setMinMaxValue(int col) {
        jMinValueLabel.setText(Double.toString(tm.getMinValue(col)));
        jMaxValueLabel.setText(Double.toString(tm.getMaxValue(col)));
        update();
    }

    public void addAttrData(File file) {
        if (file.getName().endsWith("csd")) {
            Reader r = null;
            try {
                r = new FileReader(file);
                CSProperties csp = DataIO.properties(r, java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_data"));
                TableModel m = Tables.fromCSP(csp, layer.getFeatures().length);
                tm.addTableModel(m);
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    r.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        tm.fireTableStructureChanged();
        update();
    }

    private void update() {
        firePropertyChange(Events.GF_UPDATE, null, layer);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        maxColorChooser = new net.java.dev.colorchooser.ColorChooser();
        jLabel3 = new javax.swing.JLabel();
        minColorChooser = new net.java.dev.colorchooser.ColorChooser();
        jLabel1 = new javax.swing.JLabel();
        jMaxValueLabel = new javax.swing.JLabel();
        jMinValueLabel = new javax.swing.JLabel();
        jcolorPanel1 = new javax.swing.JPanel();
        jcolorPanel2 = new javax.swing.JPanel();
        jcolorPanel3 = new javax.swing.JPanel();
        jcolorPanel4 = new javax.swing.JPanel();
        jcolorPanel5 = new javax.swing.JPanel();
        jcolorPanel6 = new javax.swing.JPanel();
        jcolorPanel7 = new javax.swing.JPanel();
        jcolorPanel8 = new javax.swing.JPanel();
        jcolorPanel9 = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(145, 99));

        javax.swing.GroupLayout maxColorChooserLayout = new javax.swing.GroupLayout(maxColorChooser);
        maxColorChooser.setLayout(maxColorChooserLayout);
        maxColorChooserLayout.setHorizontalGroup(
            maxColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 14, Short.MAX_VALUE)
        );
        maxColorChooserLayout.setVerticalGroup(
            maxColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 14, Short.MAX_VALUE)
        );

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("gw/resources/language"); // NOI18N
        jLabel3.setText(bundle.getString("L_Max_Color")); // NOI18N

        minColorChooser.setColor(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout minColorChooserLayout = new javax.swing.GroupLayout(minColorChooser);
        minColorChooser.setLayout(minColorChooserLayout);
        minColorChooserLayout.setHorizontalGroup(
            minColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 14, Short.MAX_VALUE)
        );
        minColorChooserLayout.setVerticalGroup(
            minColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 14, Short.MAX_VALUE)
        );

        jLabel1.setText(bundle.getString("L_Min_Color")); // NOI18N

        javax.swing.GroupLayout jcolorPanel1Layout = new javax.swing.GroupLayout(jcolorPanel1);
        jcolorPanel1.setLayout(jcolorPanel1Layout);
        jcolorPanel1Layout.setHorizontalGroup(
            jcolorPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        jcolorPanel1Layout.setVerticalGroup(
            jcolorPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jcolorPanel2Layout = new javax.swing.GroupLayout(jcolorPanel2);
        jcolorPanel2.setLayout(jcolorPanel2Layout);
        jcolorPanel2Layout.setHorizontalGroup(
            jcolorPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        jcolorPanel2Layout.setVerticalGroup(
            jcolorPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jcolorPanel3Layout = new javax.swing.GroupLayout(jcolorPanel3);
        jcolorPanel3.setLayout(jcolorPanel3Layout);
        jcolorPanel3Layout.setHorizontalGroup(
            jcolorPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        jcolorPanel3Layout.setVerticalGroup(
            jcolorPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jcolorPanel4Layout = new javax.swing.GroupLayout(jcolorPanel4);
        jcolorPanel4.setLayout(jcolorPanel4Layout);
        jcolorPanel4Layout.setHorizontalGroup(
            jcolorPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        jcolorPanel4Layout.setVerticalGroup(
            jcolorPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jcolorPanel5Layout = new javax.swing.GroupLayout(jcolorPanel5);
        jcolorPanel5.setLayout(jcolorPanel5Layout);
        jcolorPanel5Layout.setHorizontalGroup(
            jcolorPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        jcolorPanel5Layout.setVerticalGroup(
            jcolorPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jcolorPanel6Layout = new javax.swing.GroupLayout(jcolorPanel6);
        jcolorPanel6.setLayout(jcolorPanel6Layout);
        jcolorPanel6Layout.setHorizontalGroup(
            jcolorPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        jcolorPanel6Layout.setVerticalGroup(
            jcolorPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jcolorPanel7Layout = new javax.swing.GroupLayout(jcolorPanel7);
        jcolorPanel7.setLayout(jcolorPanel7Layout);
        jcolorPanel7Layout.setHorizontalGroup(
            jcolorPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        jcolorPanel7Layout.setVerticalGroup(
            jcolorPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jcolorPanel8Layout = new javax.swing.GroupLayout(jcolorPanel8);
        jcolorPanel8.setLayout(jcolorPanel8Layout);
        jcolorPanel8Layout.setHorizontalGroup(
            jcolorPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        jcolorPanel8Layout.setVerticalGroup(
            jcolorPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jcolorPanel9Layout = new javax.swing.GroupLayout(jcolorPanel9);
        jcolorPanel9.setLayout(jcolorPanel9Layout);
        jcolorPanel9Layout.setHorizontalGroup(
            jcolorPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        jcolorPanel9Layout.setVerticalGroup(
            jcolorPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(maxColorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jMinValueLabel)
                            .addComponent(jMaxValueLabel)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jcolorPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcolorPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcolorPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcolorPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcolorPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcolorPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcolorPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcolorPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcolorPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(minColorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(maxColorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(jMaxValueLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jcolorPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jcolorPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jcolorPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jcolorPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jcolorPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jcolorPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jcolorPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jcolorPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jcolorPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(minColorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMinValueLabel)
                            .addComponent(jLabel1))))
                .addContainerGap(21, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jMaxValueLabel;
    private javax.swing.JLabel jMinValueLabel;
    private javax.swing.JPanel jcolorPanel1;
    private javax.swing.JPanel jcolorPanel2;
    private javax.swing.JPanel jcolorPanel3;
    private javax.swing.JPanel jcolorPanel4;
    private javax.swing.JPanel jcolorPanel5;
    private javax.swing.JPanel jcolorPanel6;
    private javax.swing.JPanel jcolorPanel7;
    private javax.swing.JPanel jcolorPanel8;
    private javax.swing.JPanel jcolorPanel9;
    private net.java.dev.colorchooser.ColorChooser maxColorChooser;
    private net.java.dev.colorchooser.ColorChooser minColorChooser;
    // End of variables declaration//GEN-END:variables
}
