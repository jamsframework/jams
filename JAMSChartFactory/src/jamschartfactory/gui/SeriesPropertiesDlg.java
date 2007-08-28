/*
 * SeriesPropertiesDlg.java
 *
 * Created on 4. Mai 2006, 11:26
 */

package jamschartfactory.gui;

import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;

/**
 *
 * @author  c0krpe
 */
public class SeriesPropertiesDlg extends javax.swing.JDialog {
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;
    
    private XYPlot thePlot;
    private int seriesID;
    private XYLineAndShapeRenderer lineRend = null;
    private XYBarRenderer barRend = null;
    private boolean barSeries = false;
    private boolean lineSeries = false;
    
    /**
     * Creates new form LineSeriesPropertiesDlg
     */
    public SeriesPropertiesDlg(XYPlot xyp, XYLineAndShapeRenderer renderer, int seriesID) {
        this.lineSeries = true;
        this.barSeries = false;
        this.seriesID = seriesID;
        this.thePlot = xyp;
        lineRend = renderer;
        this.setModal(false);
        
        initComponents();
        this.jTabbedPane1.remove(1);
        initLineDialog();
        initDialog();
    }
    
    /**
     * Creates new form LineSeriesPropertiesDlg
     */
    public SeriesPropertiesDlg(XYPlot xyp, XYBarRenderer renderer, int seriesID) {
        this.lineSeries = false;
        this.barSeries = true;
        this.seriesID = seriesID;
        this.thePlot = xyp;
        barRend = renderer;
        this.setModal(false);
        
        initComponents();
        this.jTabbedPane1.remove(0);
        
        initBarDialog();
        initDialog();
    }
    
    public void initLineDialog(){
        this.lineColor.setBackground((java.awt.Color)lineRend.getSeriesPaint(0));
        this.shapeColor.setBackground((java.awt.Color)lineRend.getSeriesOutlinePaint(0));
        this.lineWidth.setText(""+((java.awt.BasicStroke)lineRend.getSeriesStroke(0)).getLineWidth());
        //for some reason the shape size is doubled by each call, therefore it is halfed here! sorry
        this.shapeSize.setText(""+(int)(lineRend.getSeriesShape(0).getBounds2D().getHeight() / 2));
        this.cbLines.setSelected(lineRend.getSeriesLinesVisible(0));
        this.cbShapes.setSelected(lineRend.getSeriesShapesVisible(0));
    }
    
    public void initBarDialog(){
        this.barColor.setBackground((java.awt.Color)barRend.getSeriesPaint(0));
        this.barOutlineColor.setBackground((java.awt.Color)barRend.getSeriesOutlinePaint(0));
        this.tf_barOutlineWidth.setText(""+((java.awt.BasicStroke)barRend.getSeriesOutlineStroke(0)).getLineWidth());
    }
    public void initDialog(){
        //the Title
        this.setTitle("" + thePlot.getDataset(seriesID).getSeriesKey(0).toString()+ " properties dialog");
        //retrieve sytem fonts
        java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        String[] fontSizes = new String[] { "8", "9", "10", "11", "12", "14", "16",
        "18", "20", "22", "24", "26", "28", "36", "48", "72" };
        
        //the axis panel
        this.axisTitle.setText(thePlot.getRangeAxis(seriesID).getLabel());
        if(seriesID == 0){
            this.cbOwnAxis.setEnabled(false);
        }
        this.tf_axisMin.setText(""+thePlot.getRangeAxis(seriesID).getLowerBound());
        this.tf_axisMax.setText(""+thePlot.getRangeAxis(seriesID).getUpperBound());
        System.out.println("Axis:" + thePlot.getRangeAxis(seriesID).toString());
        //this.tf_axisIntervall.setText(""+thePlot.getRangeAxis(seriesID).get)
        
        this.cob_labelFontFamily.setModel(new javax.swing.DefaultComboBoxModel(fontNames));
        this.cob_labelFontFamily.setSelectedItem(thePlot.getRangeAxis(seriesID).getLabelFont().getFamily());
        this.tf_labelFontSize.setText(""+thePlot.getRangeAxis(seriesID).getLabelFont().getSize());
        this.cob_labelFontStyle.setSelectedIndex(thePlot.getRangeAxis(seriesID).getLabelFont().getStyle());
        
        this.cob_ticksFontFamily.setModel(new javax.swing.DefaultComboBoxModel(fontNames));
        this.cob_ticksFontFamily.setSelectedItem(thePlot.getRangeAxis(seriesID).getTickLabelFont().getFamily());
        this.tf_ticksFontSize.setText(""+thePlot.getRangeAxis(seriesID).getLabelFont().getSize());
        this.cob_ticksFontStyle.setSelectedIndex(thePlot.getRangeAxis(seriesID).getLabelFont().getStyle());
        
        //the plot itself
        this.plotBGColor.setBackground((java.awt.Color)thePlot.getBackgroundPaint());
        this.cb_XGridLines.setSelected(thePlot.isDomainGridlinesVisible());
        this.cb_YGridLines.setSelected(thePlot.isRangeGridlinesVisible());
        this.xGridLineColor.setBackground((java.awt.Color)thePlot.getDomainGridlinePaint());
        this.yGridLineColor.setBackground((java.awt.Color)thePlot.getRangeGridlinePaint());
        this.xGridLineWidth.setText(""+ ((java.awt.BasicStroke)thePlot.getDomainGridlineStroke()).getLineWidth());
        this.yGridLineWidth.setText(""+ ((java.awt.BasicStroke)thePlot.getRangeGridlineStroke()).getLineWidth());
        
    }
    
    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        lineSeriesPattern = new javax.swing.JPanel();
        linePanel = new javax.swing.JPanel();
        lineColor = new javax.swing.JPanel();
        lineWidth = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        cbLines = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lineType = new javax.swing.JTextField();
        shapePanel = new javax.swing.JPanel();
        shapeColor = new javax.swing.JPanel();
        shapeSize = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        cbShapes = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        shapeType = new javax.swing.JTextField();
        barSeriesPattern = new javax.swing.JPanel();
        barPanel = new javax.swing.JPanel();
        barColor = new javax.swing.JPanel();
        tf_barOutlineWidth = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        lineType1 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        barOutlineColor = new javax.swing.JPanel();
        axesPattern = new javax.swing.JPanel();
        labelPanel = new javax.swing.JPanel();
        cbOwnAxis = new javax.swing.JCheckBox();
        axisTitle = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        labelFontPanel = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        tf_labelFontSize = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        cob_labelFontStyle = new javax.swing.JComboBox();
        cob_labelFontFamily = new javax.swing.JComboBox();
        tickPanel = new javax.swing.JPanel();
        cb_axisMin = new javax.swing.JCheckBox();
        cb_axisMax = new javax.swing.JCheckBox();
        cb_axisIntervall = new javax.swing.JCheckBox();
        tf_axisMin = new javax.swing.JTextField();
        tf_axisMax = new javax.swing.JTextField();
        tf_axisIntervall = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        cob_tickFontFamily = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        tf_ticksFontSize = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        cob_ticksFontStyle = new javax.swing.JComboBox();
        cob_ticksFontFamily = new javax.swing.JComboBox();
        plotPattern = new javax.swing.JPanel();
        linePanel1 = new javax.swing.JPanel();
        xGridLineColor = new javax.swing.JPanel();
        xGridLineWidth = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        cb_XGridLines = new javax.swing.JCheckBox();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        xGridLineType = new javax.swing.JTextField();
        linePanel2 = new javax.swing.JPanel();
        yGridLineColor = new javax.swing.JPanel();
        yGridLineWidth = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        cb_YGridLines = new javax.swing.JCheckBox();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        yGridLineType = new javax.swing.JTextField();
        linePanel3 = new javax.swing.JPanel();
        plotBGColor = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();

        setTitle("Series properties");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        setButton.setFont(new java.awt.Font("Arial", 0, 11));
        setButton.setText("SET");
        setButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setButtonActionPerformed(evt);
            }
        });

        cancelButton.setFont(new java.awt.Font("Arial", 0, 11));
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        mainPanel.setLayout(new java.awt.BorderLayout());

        mainPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTabbedPane1.setFont(new java.awt.Font("Arial", 0, 11));
        lineSeriesPattern.setLayout(new java.awt.GridBagLayout());

        lineSeriesPattern.setFont(new java.awt.Font("Arial", 0, 11));
        linePanel.setLayout(new java.awt.GridBagLayout());

        linePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true), "Line", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11)));
        lineColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lineColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lineColorMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout lineColorLayout = new org.jdesktop.layout.GroupLayout(lineColor);
        lineColor.setLayout(lineColorLayout);
        lineColorLayout.setHorizontalGroup(
            lineColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 66, Short.MAX_VALUE)
        );
        lineColorLayout.setVerticalGroup(
            lineColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 12, Short.MAX_VALUE)
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        linePanel.add(lineColor, gridBagConstraints);

        lineWidth.setFont(new java.awt.Font("Arial", 0, 11));
        lineWidth.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        linePanel.add(lineWidth, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel1.setText("Width:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        linePanel.add(jLabel1, gridBagConstraints);

        cbLines.setFont(new java.awt.Font("Arial", 0, 11));
        cbLines.setText("draw lines");
        cbLines.setActionCommand("draw line");
        cbLines.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbLines.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 0);
        linePanel.add(cbLines, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel2.setText("Color:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        linePanel.add(jLabel2, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel4.setText("Type:");
        jLabel4.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        linePanel.add(jLabel4, gridBagConstraints);

        lineType.setFont(new java.awt.Font("Arial", 0, 11));
        lineType.setText("not impl. yet");
        lineType.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        linePanel.add(lineType, gridBagConstraints);

        lineSeriesPattern.add(linePanel, new java.awt.GridBagConstraints());

        shapePanel.setLayout(new java.awt.GridBagLayout());

        shapePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true), "Shape", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11)));
        shapeColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        shapeColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                shapeColorMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout shapeColorLayout = new org.jdesktop.layout.GroupLayout(shapeColor);
        shapeColor.setLayout(shapeColorLayout);
        shapeColorLayout.setHorizontalGroup(
            shapeColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 66, Short.MAX_VALUE)
        );
        shapeColorLayout.setVerticalGroup(
            shapeColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 12, Short.MAX_VALUE)
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        shapePanel.add(shapeColor, gridBagConstraints);

        shapeSize.setFont(new java.awt.Font("Arial", 0, 11));
        shapeSize.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        shapePanel.add(shapeSize, gridBagConstraints);

        jLabel5.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel5.setText("Size:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        shapePanel.add(jLabel5, gridBagConstraints);

        cbShapes.setFont(new java.awt.Font("Arial", 0, 11));
        cbShapes.setText("draw shapes");
        cbShapes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbShapes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 3);
        shapePanel.add(cbShapes, gridBagConstraints);

        jLabel6.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel6.setText("Color:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        shapePanel.add(jLabel6, gridBagConstraints);

        jLabel7.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel7.setText("Type:");
        jLabel7.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        shapePanel.add(jLabel7, gridBagConstraints);

        shapeType.setFont(new java.awt.Font("Arial", 0, 11));
        shapeType.setText("not impl. yet");
        shapeType.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        shapePanel.add(shapeType, gridBagConstraints);

        lineSeriesPattern.add(shapePanel, new java.awt.GridBagConstraints());

        jTabbedPane1.addTab("Line and Shape Pattern", lineSeriesPattern);

        barSeriesPattern.setLayout(new java.awt.GridBagLayout());

        barSeriesPattern.setFont(new java.awt.Font("Arial", 0, 11));
        barPanel.setLayout(new java.awt.GridBagLayout());

        barPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true), "Bar", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11)));
        barColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        barColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                barColorMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout barColorLayout = new org.jdesktop.layout.GroupLayout(barColor);
        barColor.setLayout(barColorLayout);
        barColorLayout.setHorizontalGroup(
            barColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 66, Short.MAX_VALUE)
        );
        barColorLayout.setVerticalGroup(
            barColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 12, Short.MAX_VALUE)
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        barPanel.add(barColor, gridBagConstraints);

        tf_barOutlineWidth.setFont(new java.awt.Font("Arial", 0, 11));
        tf_barOutlineWidth.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        barPanel.add(tf_barOutlineWidth, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel3.setText("OutlineWidth:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        barPanel.add(jLabel3, gridBagConstraints);

        jLabel10.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel10.setText("BarColor:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        barPanel.add(jLabel10, gridBagConstraints);

        jLabel14.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel14.setText("Type:");
        jLabel14.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        barPanel.add(jLabel14, gridBagConstraints);

        lineType1.setFont(new java.awt.Font("Arial", 0, 11));
        lineType1.setText("not impl. yet");
        lineType1.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        barPanel.add(lineType1, gridBagConstraints);

        jLabel15.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel15.setText("OutlineColor:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        barPanel.add(jLabel15, gridBagConstraints);

        barOutlineColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        barOutlineColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                barOutlineColorMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout barOutlineColorLayout = new org.jdesktop.layout.GroupLayout(barOutlineColor);
        barOutlineColor.setLayout(barOutlineColorLayout);
        barOutlineColorLayout.setHorizontalGroup(
            barOutlineColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 66, Short.MAX_VALUE)
        );
        barOutlineColorLayout.setVerticalGroup(
            barOutlineColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 12, Short.MAX_VALUE)
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        barPanel.add(barOutlineColor, gridBagConstraints);

        barSeriesPattern.add(barPanel, new java.awt.GridBagConstraints());

        jTabbedPane1.addTab("Bar Pattern", barSeriesPattern);

        axesPattern.setLayout(new java.awt.GridBagLayout());

        axesPattern.setFont(new java.awt.Font("Arial", 0, 11));
        labelPanel.setLayout(new java.awt.GridBagLayout());

        labelPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true), "Label", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11), new java.awt.Color(0, 102, 255)));
        cbOwnAxis.setFont(new java.awt.Font("Arial", 0, 11));
        cbOwnAxis.setSelected(true);
        cbOwnAxis.setText("own y-axis");
        cbOwnAxis.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbOwnAxis.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 2);
        labelPanel.add(cbOwnAxis, gridBagConstraints);

        axisTitle.setFont(new java.awt.Font("Arial", 0, 11));
        axisTitle.setText("jTextField1");
        axisTitle.setMaximumSize(new java.awt.Dimension(62, 19));
        axisTitle.setMinimumSize(new java.awt.Dimension(62, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 3);
        labelPanel.add(axisTitle, gridBagConstraints);

        jLabel8.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel8.setText("Title:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 0);
        labelPanel.add(jLabel8, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        axesPattern.add(labelPanel, gridBagConstraints);

        labelFontPanel.setLayout(new java.awt.GridBagLayout());

        labelFontPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true), "Label Font", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11), new java.awt.Color(0, 102, 255)));
        jLabel11.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel11.setText("Family:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        labelFontPanel.add(jLabel11, gridBagConstraints);

        tf_labelFontSize.setFont(new java.awt.Font("Arial", 0, 11));
        tf_labelFontSize.setText("11");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 35;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 3);
        labelFontPanel.add(tf_labelFontSize, gridBagConstraints);

        jLabel12.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel12.setText("Size:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        labelFontPanel.add(jLabel12, gridBagConstraints);

        jLabel13.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel13.setText("Style:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        labelFontPanel.add(jLabel13, gridBagConstraints);

        cob_labelFontStyle.setFont(new java.awt.Font("Arial", 0, 11));
        cob_labelFontStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Plain", "Bold", "Italic" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 3);
        labelFontPanel.add(cob_labelFontStyle, gridBagConstraints);

        cob_labelFontFamily.setFont(new java.awt.Font("Arial", 0, 11));
        cob_labelFontFamily.setMaximumSize(new java.awt.Dimension(35, 22));
        cob_labelFontFamily.setMinimumSize(new java.awt.Dimension(35, 22));
        cob_labelFontFamily.setPreferredSize(new java.awt.Dimension(35, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        labelFontPanel.add(cob_labelFontFamily, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        axesPattern.add(labelFontPanel, gridBagConstraints);

        tickPanel.setLayout(new java.awt.GridBagLayout());

        tickPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true), "Tick Marks", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11), new java.awt.Color(0, 102, 255)));
        cb_axisMin.setFont(new java.awt.Font("Arial", 0, 11));
        cb_axisMin.setSelected(true);
        cb_axisMin.setText("minimum:");
        cb_axisMin.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cb_axisMin.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 3);
        tickPanel.add(cb_axisMin, gridBagConstraints);

        cb_axisMax.setFont(new java.awt.Font("Arial", 0, 11));
        cb_axisMax.setSelected(true);
        cb_axisMax.setText("maximum:");
        cb_axisMax.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cb_axisMax.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 3);
        tickPanel.add(cb_axisMax, gridBagConstraints);

        cb_axisIntervall.setFont(new java.awt.Font("Arial", 0, 11));
        cb_axisIntervall.setSelected(true);
        cb_axisIntervall.setText("intervall:");
        cb_axisIntervall.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cb_axisIntervall.setEnabled(false);
        cb_axisIntervall.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 3);
        tickPanel.add(cb_axisIntervall, gridBagConstraints);

        tf_axisMin.setFont(new java.awt.Font("Arial", 0, 11));
        tf_axisMin.setText("jTextField1");
        tf_axisMin.setMaximumSize(new java.awt.Dimension(62, 19));
        tf_axisMin.setMinimumSize(new java.awt.Dimension(62, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        tickPanel.add(tf_axisMin, gridBagConstraints);

        tf_axisMax.setFont(new java.awt.Font("Arial", 0, 11));
        tf_axisMax.setText("jTextField2");
        tf_axisMax.setMaximumSize(new java.awt.Dimension(62, 19));
        tf_axisMax.setMinimumSize(new java.awt.Dimension(62, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        tickPanel.add(tf_axisMax, gridBagConstraints);

        tf_axisIntervall.setFont(new java.awt.Font("Arial", 0, 11));
        tf_axisIntervall.setText("jTextField3");
        tf_axisIntervall.setEnabled(false);
        tf_axisIntervall.setMaximumSize(new java.awt.Dimension(62, 19));
        tf_axisIntervall.setMinimumSize(new java.awt.Dimension(62, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        tickPanel.add(tf_axisIntervall, gridBagConstraints);

        jLabel9.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel9.setText("Automatic:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 0);
        tickPanel.add(jLabel9, gridBagConstraints);

        axesPattern.add(tickPanel, new java.awt.GridBagConstraints());

        cob_tickFontFamily.setLayout(new java.awt.GridBagLayout());

        cob_tickFontFamily.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true), "Tick marks font", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11), new java.awt.Color(0, 102, 255)));
        jLabel17.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel17.setText("Family:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        cob_tickFontFamily.add(jLabel17, gridBagConstraints);

        tf_ticksFontSize.setFont(new java.awt.Font("Arial", 0, 11));
        tf_ticksFontSize.setText("11");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 35;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        cob_tickFontFamily.add(tf_ticksFontSize, gridBagConstraints);

        jLabel18.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel18.setText("Size:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 3);
        cob_tickFontFamily.add(jLabel18, gridBagConstraints);

        jLabel19.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel19.setText("Style:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 3);
        cob_tickFontFamily.add(jLabel19, gridBagConstraints);

        cob_ticksFontStyle.setFont(new java.awt.Font("Arial", 0, 11));
        cob_ticksFontStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Plain", "Bold", "Italic" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        cob_tickFontFamily.add(cob_ticksFontStyle, gridBagConstraints);

        cob_ticksFontFamily.setFont(new java.awt.Font("Arial", 0, 11));
        cob_ticksFontFamily.setMaximumSize(new java.awt.Dimension(35, 22));
        cob_ticksFontFamily.setMinimumSize(new java.awt.Dimension(35, 22));
        cob_ticksFontFamily.setPreferredSize(new java.awt.Dimension(35, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 3);
        cob_tickFontFamily.add(cob_ticksFontFamily, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        axesPattern.add(cob_tickFontFamily, gridBagConstraints);

        jTabbedPane1.addTab("Axis", axesPattern);

        plotPattern.setLayout(new java.awt.GridBagLayout());

        linePanel1.setLayout(new java.awt.GridBagLayout());

        linePanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true), "X-Grid lines", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11)));
        xGridLineColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        xGridLineColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                xGridLineColorMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout xGridLineColorLayout = new org.jdesktop.layout.GroupLayout(xGridLineColor);
        xGridLineColor.setLayout(xGridLineColorLayout);
        xGridLineColorLayout.setHorizontalGroup(
            xGridLineColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 66, Short.MAX_VALUE)
        );
        xGridLineColorLayout.setVerticalGroup(
            xGridLineColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 12, Short.MAX_VALUE)
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        linePanel1.add(xGridLineColor, gridBagConstraints);

        xGridLineWidth.setFont(new java.awt.Font("Arial", 0, 11));
        xGridLineWidth.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        linePanel1.add(xGridLineWidth, gridBagConstraints);

        jLabel16.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel16.setText("Width:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        linePanel1.add(jLabel16, gridBagConstraints);

        cb_XGridLines.setFont(new java.awt.Font("Arial", 0, 11));
        cb_XGridLines.setText("draw grid lines");
        cb_XGridLines.setActionCommand("draw line");
        cb_XGridLines.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cb_XGridLines.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 0);
        linePanel1.add(cb_XGridLines, gridBagConstraints);

        jLabel20.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel20.setText("Color:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        linePanel1.add(jLabel20, gridBagConstraints);

        jLabel21.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel21.setText("Type:");
        jLabel21.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        linePanel1.add(jLabel21, gridBagConstraints);

        xGridLineType.setFont(new java.awt.Font("Arial", 0, 11));
        xGridLineType.setText("not impl. yet");
        xGridLineType.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        linePanel1.add(xGridLineType, gridBagConstraints);

        plotPattern.add(linePanel1, new java.awt.GridBagConstraints());

        linePanel2.setLayout(new java.awt.GridBagLayout());

        linePanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true), "Y-Grid lines", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11)));
        yGridLineColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        yGridLineColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                yGridLineColorMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout yGridLineColorLayout = new org.jdesktop.layout.GroupLayout(yGridLineColor);
        yGridLineColor.setLayout(yGridLineColorLayout);
        yGridLineColorLayout.setHorizontalGroup(
            yGridLineColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 66, Short.MAX_VALUE)
        );
        yGridLineColorLayout.setVerticalGroup(
            yGridLineColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 12, Short.MAX_VALUE)
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        linePanel2.add(yGridLineColor, gridBagConstraints);

        yGridLineWidth.setFont(new java.awt.Font("Arial", 0, 11));
        yGridLineWidth.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        linePanel2.add(yGridLineWidth, gridBagConstraints);

        jLabel22.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel22.setText("Width:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        linePanel2.add(jLabel22, gridBagConstraints);

        cb_YGridLines.setFont(new java.awt.Font("Arial", 0, 11));
        cb_YGridLines.setText("draw grid lines");
        cb_YGridLines.setActionCommand("draw line");
        cb_YGridLines.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cb_YGridLines.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 0);
        linePanel2.add(cb_YGridLines, gridBagConstraints);

        jLabel23.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel23.setText("Color:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        linePanel2.add(jLabel23, gridBagConstraints);

        jLabel24.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel24.setText("Type:");
        jLabel24.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        linePanel2.add(jLabel24, gridBagConstraints);

        yGridLineType.setFont(new java.awt.Font("Arial", 0, 11));
        yGridLineType.setText("not impl. yet");
        yGridLineType.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        linePanel2.add(yGridLineType, gridBagConstraints);

        plotPattern.add(linePanel2, new java.awt.GridBagConstraints());

        linePanel3.setLayout(new java.awt.GridBagLayout());

        linePanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true), "Plot colors", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11)));
        plotBGColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        plotBGColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                plotBGColorMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout plotBGColorLayout = new org.jdesktop.layout.GroupLayout(plotBGColor);
        plotBGColor.setLayout(plotBGColorLayout);
        plotBGColorLayout.setHorizontalGroup(
            plotBGColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 66, Short.MAX_VALUE)
        );
        plotBGColorLayout.setVerticalGroup(
            plotBGColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 12, Short.MAX_VALUE)
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        linePanel3.add(plotBGColor, gridBagConstraints);

        jLabel26.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel26.setText("Background color:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        linePanel3.add(jLabel26, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        plotPattern.add(linePanel3, gridBagConstraints);

        jTabbedPane1.addTab("Plot properties", plotPattern);

        mainPanel.add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(250, Short.MAX_VALUE)
                .add(setButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cancelButton)
                .addContainerGap())
            .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );

        layout.linkSize(new java.awt.Component[] {cancelButton, setButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(setButton))
                .addContainerGap())
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void plotBGColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_plotBGColorMouseClicked
        this.plotBGColor.setBackground(javax.swing.JColorChooser.showDialog(this, "Line color", this.plotBGColor.getBackground()));
    }//GEN-LAST:event_plotBGColorMouseClicked

    private void yGridLineColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yGridLineColorMouseClicked
        this.yGridLineColor.setBackground(javax.swing.JColorChooser.showDialog(this, "Line color", this.yGridLineColor.getBackground()));
    }//GEN-LAST:event_yGridLineColorMouseClicked

    private void xGridLineColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xGridLineColorMouseClicked
        this.xGridLineColor.setBackground(javax.swing.JColorChooser.showDialog(this, "Line color", this.xGridLineColor.getBackground()));
    }//GEN-LAST:event_xGridLineColorMouseClicked

    private void barOutlineColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_barOutlineColorMouseClicked
        this.barOutlineColor.setBackground(javax.swing.JColorChooser.showDialog(this, "Line color", this.barOutlineColor.getBackground()));
    }//GEN-LAST:event_barOutlineColorMouseClicked

    private void barColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_barColorMouseClicked
        this.barColor.setBackground(javax.swing.JColorChooser.showDialog(this, "Line color", this.barColor.getBackground()));
    }//GEN-LAST:event_barColorMouseClicked

    private void shapeColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_shapeColorMouseClicked
        this.shapeColor.setBackground(javax.swing.JColorChooser.showDialog(this, "Shape color", this.shapeColor.getBackground()));
    }//GEN-LAST:event_shapeColorMouseClicked

    private void lineColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lineColorMouseClicked
        this.lineColor.setBackground(javax.swing.JColorChooser.showDialog(this, "Line color", this.lineColor.getBackground()));
    }//GEN-LAST:event_lineColorMouseClicked
    
    private void setButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setButtonActionPerformed
        if(this.lineSeries){
            lineRend.setSeriesPaint(0,lineColor.getBackground());
            lineRend.setSeriesStroke(0, new java.awt.BasicStroke(Float.parseFloat(lineWidth.getText())));
            lineRend.setSeriesLinesVisible(0,this.cbLines.isSelected());
            lineRend.setSeriesShapesVisible(0,this.cbShapes.isSelected());
            lineRend.setSeriesShape(0, org.jfree.util.ShapeUtilities.createDiagonalCross(Integer.parseInt(this.shapeSize.getText()),0));
            lineRend.setSeriesShapesFilled(0, false);
            
            lineRend.setOutlinePaint(shapeColor.getBackground());
            
            
            
        } 
        if(this.barSeries){
            barRend.setSeriesPaint(0, this.barColor.getBackground(), true);
            barRend.setSeriesOutlinePaint(0, this.barOutlineColor.getBackground(), true);
            barRend.setSeriesOutlineStroke(0, new java.awt.BasicStroke(Float.parseFloat(this.tf_barOutlineWidth.getText())));
            
        }
        
        thePlot.getRangeAxis(seriesID).setLabel(this.axisTitle.getText());
        if(!this.cbOwnAxis.isSelected()){
            thePlot.getRangeAxis(seriesID).setVisible(false);
            thePlot.mapDatasetToRangeAxis(seriesID,0);
            thePlot.getRangeAxis(0).setAutoRange(true);
        }else{
            thePlot.getRangeAxis(seriesID).setVisible(true);
            thePlot.mapDatasetToRangeAxis(seriesID,seriesID);
        }
        thePlot.getRangeAxis(seriesID).setLabelFont(new java.awt.Font((String)this.cob_labelFontFamily.getSelectedItem(), this.cob_labelFontStyle.getSelectedIndex(), Integer.parseInt(this.tf_labelFontSize.getText())));
        thePlot.getRangeAxis(seriesID).setTickLabelFont(new java.awt.Font((String)this.cob_ticksFontFamily.getSelectedItem(), this.cob_ticksFontStyle.getSelectedIndex(), Integer.parseInt(this.tf_ticksFontSize.getText())));
        
        double upperRange = Double.parseDouble(this.tf_axisMax.getText());
        double lowerRange = Double.parseDouble(this.tf_axisMin.getText());
        
        thePlot.getRangeAxis(seriesID).setAutoRange(true);
        
        double autoUpperRange = thePlot.getRangeAxis(seriesID).getUpperBound();
        double autoLowerRange = thePlot.getRangeAxis(seriesID).getLowerBound();
        
        if(!this.cb_axisMin.isSelected() && !this.cb_axisMax.isSelected()){
            thePlot.getRangeAxis(seriesID).setRange(lowerRange, upperRange);
        }
        if(this.cb_axisMin.isSelected() && !this.cb_axisMax.isSelected()){
            thePlot.getRangeAxis(seriesID).setAutoRange(false);
            thePlot.getRangeAxis(seriesID).setRange(autoLowerRange, upperRange);
        }
        if(!this.cb_axisMin.isSelected() && this.cb_axisMax.isSelected()){
            thePlot.getRangeAxis(seriesID).setAutoRange(false);
            thePlot.getRangeAxis(seriesID).setRange(lowerRange, autoUpperRange);
        }
        if(this.cb_axisMin.isSelected() && this.cb_axisMax.isSelected()){
            thePlot.getRangeAxis(seriesID).setAutoRange(true);//Range(lowerRange, upperRange);
        }
        thePlot.setBackgroundPaint(this.plotBGColor.getBackground());
        thePlot.setDomainGridlinesVisible(this.cb_XGridLines.isSelected());
        thePlot.setRangeGridlinesVisible(this.cb_YGridLines.isSelected());
        thePlot.setDomainGridlinePaint(this.xGridLineColor.getBackground());
        thePlot.setRangeGridlinePaint(this.yGridLineColor.getBackground());
        
        java.awt.BasicStroke str = (java.awt.BasicStroke)thePlot.getDomainGridlineStroke();
        thePlot.setDomainGridlineStroke(new java.awt.BasicStroke(Float.parseFloat(this.xGridLineWidth.getText()), str.getEndCap(), str.getLineJoin(), str.getMiterLimit(), str.getDashArray(), str.getDashPhase()));
        thePlot.setRangeGridlineStroke(new java.awt.BasicStroke(Float.parseFloat(this.yGridLineWidth.getText()), str.getEndCap(), str.getLineJoin(), str.getMiterLimit(), str.getDashArray(), str.getDashPhase()));
    }//GEN-LAST:event_setButtonActionPerformed
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog
    
    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel axesPattern;
    private javax.swing.JTextField axisTitle;
    private javax.swing.JPanel barColor;
    private javax.swing.JPanel barOutlineColor;
    private javax.swing.JPanel barPanel;
    private javax.swing.JPanel barSeriesPattern;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox cbLines;
    private javax.swing.JCheckBox cbOwnAxis;
    private javax.swing.JCheckBox cbShapes;
    private javax.swing.JCheckBox cb_XGridLines;
    private javax.swing.JCheckBox cb_YGridLines;
    private javax.swing.JCheckBox cb_axisIntervall;
    private javax.swing.JCheckBox cb_axisMax;
    private javax.swing.JCheckBox cb_axisMin;
    private javax.swing.JComboBox cob_labelFontFamily;
    private javax.swing.JComboBox cob_labelFontStyle;
    private javax.swing.JPanel cob_tickFontFamily;
    private javax.swing.JComboBox cob_ticksFontFamily;
    private javax.swing.JComboBox cob_ticksFontStyle;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel labelFontPanel;
    private javax.swing.JPanel labelPanel;
    public javax.swing.JPanel lineColor;
    private javax.swing.JPanel linePanel;
    private javax.swing.JPanel linePanel1;
    private javax.swing.JPanel linePanel2;
    private javax.swing.JPanel linePanel3;
    private javax.swing.JPanel lineSeriesPattern;
    private javax.swing.JTextField lineType;
    private javax.swing.JTextField lineType1;
    public javax.swing.JTextField lineWidth;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel plotBGColor;
    private javax.swing.JPanel plotPattern;
    private javax.swing.JButton setButton;
    public javax.swing.JPanel shapeColor;
    private javax.swing.JPanel shapePanel;
    public javax.swing.JTextField shapeSize;
    private javax.swing.JTextField shapeType;
    private javax.swing.JTextField tf_axisIntervall;
    private javax.swing.JTextField tf_axisMax;
    private javax.swing.JTextField tf_axisMin;
    private javax.swing.JTextField tf_barOutlineWidth;
    private javax.swing.JTextField tf_labelFontSize;
    private javax.swing.JTextField tf_ticksFontSize;
    private javax.swing.JPanel tickPanel;
    private javax.swing.JPanel xGridLineColor;
    private javax.swing.JTextField xGridLineType;
    private javax.swing.JTextField xGridLineWidth;
    private javax.swing.JPanel yGridLineColor;
    private javax.swing.JTextField yGridLineType;
    private javax.swing.JTextField yGridLineWidth;
    // End of variables declaration//GEN-END:variables
    
    private int returnStatus = RET_CANCEL;
}
