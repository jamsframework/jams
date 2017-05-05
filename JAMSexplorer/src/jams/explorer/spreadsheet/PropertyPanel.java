/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.explorer.spreadsheet;

import jams.JAMS;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.event.*;
import javax.swing.JButton;
import javax.swing.JPanel;

import java.lang.Math.*;

import java.util.HashMap;
import java.util.Locale;

import org.jfree.util.ShapeUtilities.*;

import jams.gui.tools.GUIHelper;

/**
 *
 * @author robertriedel
 */
public class PropertyPanel {

    /*
 * GraphProperties.java
 *
 * Created on 29. September 2007, 17:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
     */
    /**
     *
     * @author p4riro
     */
//
//    private static ImageIcon UP_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("jams/components/gui/resources/arrowup.png")).getImage().getScaledInstance(9, 5, Image.SCALE_SMOOTH));
//    private static ImageIcon DOWN_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("jams/components/gui/resources/arrowdown.png")).getImage().getScaledInstance(9, 5, Image.SCALE_SMOOTH));
    JFrame parent;

    ImageIcon up_icon = JAMS.getScaledIcon("jams/explorer/resources/images/arrowup.png", 6, 6);
    ImageIcon down_icon = JAMS.getScaledIcon("jams/explorer/resources/images/arrowdown.png", 6, 6);
    ImageIcon plot_icon = JAMS.getIcon("jams/explorer/resources/images/correct.png");
    ImageIcon add_icon = JAMS.getIcon("jams/explorer/resources/images/add.png");
    ImageIcon rem_icon = JAMS.getScaledIcon("jams/explorer/resources/images/remove.png", 8, 8);

    JScrollPane scpane;

    int index = 0;

    int plotType;

    String legendName;
    int color;
    String name;
    int position; // left/right
    int type; //renderer index
    int[] d_range = new int[2];

    boolean is_x_series = false;
    boolean result = false;
    boolean x_changed;

    JComboBox setColumn;
    JComboBox colorchoice;
    JComboBox typechoice;
    JComboBox poschoice;

    JComboBox timechoice_START;
    JComboBox timechoice_END;

    JTextField datachoice_START;
    JTextField datachoice_END;
    JPanel datachoice_panel;
    JButton datachoice_max;

    JButton addButton;
    JButton remButton;
    JButton plotButton;
    JButton upButton;
    JButton downButton;

    JButton customizeButton;

    JCheckBox invBox;
    JToggleButton isXAxis;

    JLabel nameLabel;

    JTextField setName;
    JLabel setLegend;

    JTSConfigurator ctsconf;
    JXYConfigurator cxyconf;
    STPConfigurator stpconf;

    private String[] colors = {"red", "blue", "green", "black", "magenta", "cyan", "yellow", "gray", "orange", "lightgray", "pink"};
    private String[] types = {
        JAMS.i18n("LINE"),
        JAMS.i18n("BAR"),
        JAMS.i18n("LINE_AND_SHAPE"),
        "Area", "Line and Base", "Dot", "Step", "StepArea", "Difference"};
    private String[] positions = {
        JAMS.i18n("LEFT"),
        JAMS.i18n("RIGHT")
    };

    JPanel graphpanel = new JPanel();
    JPanel datapanel = new JPanel();
    JPanel buttonpanel = new JPanel();

    //Variables for renderer options
    Stroke series_stroke;
    Shape series_shape;
    Color series_paint;
    Stroke series_outline_stroke;
    Color series_outline_paint;
    Color series_fill_paint;

    JLabel colorlabel;
    JLabel scolorlabel;

    boolean outlineVisible;
    boolean linesVisible;
    boolean shapesVisible;

    HashMap<String, Color> colorTable = new HashMap<String, Color>();

    CustomizeRendererDlg cr_dlg;

    String headers[];
    int columnCount = 0;
    int rowCount = 0;

    GraphProperties prop;

    //String column[];
    /**
     * Creates a new instance of GraphProperties
     */
    /**
     * BEIM INITIALISIEREN: PROP ÜBERGEBEN UND ALLE PARAMETER AUSLESEN
     */
    public PropertyPanel(JTSConfigurator ctsconf, GraphProperties prop) {

        this.parent = ctsconf;
        this.ctsconf = ctsconf;
        this.prop = prop;
        this.plotType = 0;

        timechoice_START = new JComboBox(prop.getTimeIntervals());
        timechoice_START.setPreferredSize(new Dimension(40, 14));
        timechoice_START.addMouseListener(timeListener);
        timechoice_START.addActionListener(timeListener2);

        timechoice_END = new JComboBox(prop.getTimeIntervals());
        timechoice_END.setPreferredSize(new Dimension(40, 14));
        timechoice_END.addMouseListener(timeListener);
        timechoice_END.addActionListener(timeListener2);

//        colorTable.put("yellow", Color.yellow);
//        colorTable.put("orange", Color.orange);
//        colorTable.put("red", Color.red);
//        colorTable.put("pink", Color.pink);
//        colorTable.put("magenta", Color.magenta);
//        colorTable.put("cyan", Color.cyan);
//        colorTable.put("blue", Color.blue);
//        colorTable.put("green", Color.green);
//        colorTable.put("gray", Color.gray);
//        colorTable.put("lightgray", Color.lightGray);
//        colorTable.put("black", Color.black);
//        colorTable.put("white", Color.WHITE);
//        for(int i=0;i<columnCount;i++){
//
//                if(i!=0){
//                    column[i] = table.getColumnName(i);
//                }else{
//                    column[i] = "---";
//                }
//        }
        createPanel();

        updateGUI();
        //applyTSProperties();

    }

    public PropertyPanel(JXYConfigurator cxyconf, GraphProperties prop) {

        this.parent = cxyconf;
        this.cxyconf = cxyconf;
        this.prop = prop;
        this.plotType = prop.getPlotType();

        datachoice_panel = new JPanel();
        datachoice_panel.setLayout(new FlowLayout());
        datachoice_panel.setSize(20, 50);
        datachoice_max = new JButton("max");
        //datachoice_max.setPreferredSize(new Dimension(5,10));

        datachoice_max.addActionListener(max_listener);

        datachoice_START = new JTextField();
        datachoice_START.setHorizontalAlignment(JTextField.RIGHT);
        datachoice_START.setPreferredSize(new Dimension(40, 14));
        datachoice_START.addActionListener(dataSTARTListener);

        datachoice_START.getDocument().addDocumentListener(d_start_listener);

        datachoice_END = new JTextField();
        datachoice_END.setPreferredSize(new Dimension(40, 14));
        datachoice_END.setHorizontalAlignment(JTextField.RIGHT);
        datachoice_END.addActionListener(dataENDListener);

        datachoice_END.getDocument().addDocumentListener(d_end_listener);

        datachoice_panel.add(datachoice_END);
        datachoice_panel.add(datachoice_max);

        createPanel();

        updateGUI();
    }

    public void createPanel() {

        /*

        Hier sämtliche Daten aus GraphProperties holen!

         */
//        JPanel legendPanel = new JPanel();
//        legendPanel.setLayout(new FlowLayout());
        addButton = new JButton();
        remButton = new JButton();
        plotButton = new JButton();
        upButton = new JButton();
        downButton = new JButton();
        customizeButton = new JButton(JAMS.i18n("CUSTOMIZE"));

        upButton.setIcon(up_icon);
        downButton.setIcon(down_icon);
        plotButton.setIcon(plot_icon);
        addButton.setIcon(add_icon);
        remButton.setIcon(rem_icon);

        plotButton.setToolTipText(JAMS.i18n("PLOT_GRAPH"));
        upButton.setToolTipText(JAMS.i18n("MOVE_UP"));
        downButton.setToolTipText(JAMS.i18n("MOVE_DOWN"));
        addButton.setToolTipText(JAMS.i18n("ADD_GRAPH"));
        remButton.setToolTipText(JAMS.i18n("REMOVE_BUTTON"));

//        invBox = new JCheckBox("invert axis");
        isXAxis = new JRadioButton(JAMS.i18n("SET_X"));
        isXAxis.addActionListener(isXListener);

        addButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addButton.setPreferredSize(new Dimension(20, 14));

        addButton.addActionListener(addListener);
        remButton.addActionListener(removeListener);
        upButton.addActionListener(upListener);
        downButton.addActionListener(downListener);
        customizeButton.addActionListener(customize_listener);

//        remButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        remButton.setPreferredSize(new Dimension(20, 14));
//        upButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        upButton.setPreferredSize(new Dimension(20, 14));
//        plotButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        plotButton.setPreferredSize(new Dimension(20, 14));
//        downButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        downButton.setPreferredSize(new Dimension(20, 14));

        colorchoice = new JComboBox(colors);
        colorchoice.setPreferredSize(new Dimension(40, 14));
        colorchoice.setSelectedIndex(0);
        //colorchoice.addActionListener(okListener);

        typechoice = new JComboBox(types);
        typechoice.setPreferredSize(new Dimension(40, 14));
        typechoice.setSelectedIndex(0);
        //typechoice.addActionListener(okListener);

        poschoice = new JComboBox(positions);
        poschoice.setPreferredSize(new Dimension(40, 14));
        poschoice.setSelectedIndex(0);
        poschoice.addActionListener(rendererListener);
        //poschoice.addActionListener(okListener);

        JButton okButton = new JButton(JAMS.i18n("OK"));
        JButton cancelButton = new JButton(JAMS.i18n("CANCEL"));
        //JButton propButton = new JButton("...");

        //JLabel namelabel = new JLabel();
//        JLabel setNameLabel =   new JLabel("        Name:");
//        JLabel setColumnLabel = new JLabel("  Set Column:");
//        JLabel setLegendLabel = new JLabel("Legend Entry:");
        nameLabel = new JLabel();

//        String[] column = new String[columnCount];
        // COLUMN NAMES AUS PROP!! //
//        for(int i=0;i<columnCount;i++){
//
//             switch(plotType){
//                 case 0:
//                    if(i!=0){
//                        column[i] = table.getColumnName(i);
//                    }else{
//                        column[i] = "---";
//                    } break;
//
//                 case 1:
//                    column[i] = table.getColumnName(i);
//                    break;
//
//                 case 2:
//                    column = stpconf.getHeaders();
//                    break;
//             }
//        }
        setLegend = new JLabel(name);

        setColumn = new JComboBox(prop.getColumns());
        setColumn.setPreferredSize(new Dimension(40, 14));

        setColumn.setSelectedIndex(prop.getSelectedColumn());

//        setColumn.addMouseListener(column_listener);
        nameLabel.setText(prop.getColumns()[prop.getSelectedColumn()]);

        setName = new JTextField(name, 14);
        setName.setPreferredSize(new Dimension(40, 14));

        setLegend.setPreferredSize(new Dimension(40, 14));

//        String name = (String) setColumn.getSelectedItem();
//        setLegendName(prop.get);
        //namePanel.add(setNameLabel);
        //namePanel.add(setName);
//        legendPanel.add(setLegendLabel);
//        legendPanel.add(setLegend);
        this.datapanel.setLayout(new FlowLayout());
        this.graphpanel.setLayout(new FlowLayout());

        this.graphpanel.add(setColumn);
        this.graphpanel.add(poschoice);
        this.graphpanel.add(typechoice);
        this.graphpanel.add(colorchoice);

        this.datapanel.add(nameLabel);

        this.buttonpanel.add(okButton);
        this.buttonpanel.add(cancelButton);

        cr_dlg = new CustomizeRendererDlg(prop.getName());

        setColumn.addActionListener(columnChangedListener);

        //linecolor label
        colorlabel = new JLabel("      ");
        colorlabel.setOpaque(true);
        colorlabel.setBackground(prop.getSeriesPaint());
        //shapecolorlabel
        scolorlabel = new JLabel("");
        scolorlabel.setOpaque(false);
        scolorlabel.setBackground(prop.getSeriesPaint());

        //plotButton.addActionListener(okListener);
    }

//    public void setIndex(int index){
//        this.index = index;
//    }
//
//    public int getIndex(){
//        return index;
//    }
    private void updateGUI() {

        setLegendField(prop.getLegendName());
        if (this.plotType == 0) {
            int start = prop.getTimeSTART();
            int end = prop.getTimeEND();

            setTimeSTART(start);
            setTimeEND(end);

//                System.out.println("--------------");
//                System.out.println("start:"+prop.getTimeSTART());
//                System.out.println("end:"+prop.getTimeEND());
        }
        if (this.plotType == 1) {

            //setXGUI();
            if (prop.isXSeries()) {
                isXAxis.setSelected(true);
                setDataSTART(prop.getDataSTART());
                setDataEND(prop.getDataEND());
            }
        }
        //System.out.println("dataSTART: "+prop.getName()+" : "+prop.getDataSTART());
//                if(prop.isXSeries()){
//                    isXAxis.setSelected(true);
//                    setDataSTART(prop.getDataSTART());
//                    setDataEND(prop.getDataEND());
//
//                }else{
//                    isXAxis.setSelected(false);
//                    isXAxis.setVisible(false);
//                }

        this.setPosition(prop.getPosition());
        this.setColorLabelColor();
        this.setSColorLabelColor();
        this.setLegendField(prop.getLegendName());

        this.setLegend.setText(prop.getLegendName());
        this.setLinesVisBox(prop.getLinesVisible());
        this.setShapesVisBox(prop.getShapesVisible());
        this.setStrokeSlider(prop.getStrokeType());
        this.setShapeSlider(prop.getSizeType());
        this.setOutlineSlider(prop.getOutlineType());

        this.setShapeBox(prop.getShapeType());

        cr_dlg.setNameLabel(name);

        /*GUIHelper.addGBComponent(optionspanel, option_gbl, stroke_button,    1, 3, 1, 1, 0, 0);
            
                //legend//divider
            //shapesGUIHelper.addGBComponent(optionspanel, option_gbl, fill_button,    5, 4, 1, 1, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, outline_button,5, 7, 1, 1, 0, 0);
         */
    }

    public void setColorLabelColor() {
        if (prop.getPlotType() == 1) {
            if (prop.getShapesVisible() && ((cxyconf.getRendererLeft() == 0) && (prop.getPosition() == 0)) || ((cxyconf.getRendererRight() == 0) && (prop.getPosition() == 1))) {

                colorlabel.setBackground(prop.getSeriesFillPaint());
            } else {
                colorlabel.setBackground(prop.getSeriesPaint());
            }
        }
        if (prop.getPlotType() == 0) {
            if (prop.getShapesVisible() && ((ctsconf.getRendererLeft() == 0) && (prop.getPosition() == 0)) || ((ctsconf.getRendererRight() == 0) && (prop.getPosition() == 1))) {

                colorlabel.setBackground(prop.getSeriesFillPaint());
            } else {
                colorlabel.setBackground(prop.getSeriesPaint());
            }
        }
    }

    public void setSColorLabelColor() {
        scolorlabel.setBackground(prop.getSeriesFillPaint());
    }

    public JPanel getGraphPanel() {
        return datapanel;
    }

    public void setXChanged(boolean state) {
        this.x_changed = state;
    }

//    public void setDataSelection(){
//        this.rowSelection = table.getSelectedRows();
//        this.selectedColumn = table.getSelectedColumn();
//    }
//    public void setColor(String color){
////        this.color = color;
//        colorchoice.setSelectedItem(color);
//    }
//    public void setColor(int index){
//
//        //colorchoice.setSelectedIndex(index);
//        this.color = index;
//    }
    public void setLegendField(String s) {
        cr_dlg.setLegendField(s);
    }

    public void setPosition(int position) {
        this.position = position;
        poschoice.setSelectedItem(position);
//        if(position.equals("left")) poschoice.setSelectedIndex(0);
//        if(position.equals("right")) poschoice.setSelectedIndex(1);
    }

    public void setTimeSTART(int index) {
        timechoice_START.setSelectedIndex(index);
    }

    public void setTimeEND(int index) {
        timechoice_END.setSelectedIndex(index);
    }

    public void setDataSTART(double d_start) {

        String f_string;
        if (d_start > 100) {
            f_string = "%3.0f";
        } else {
            f_string = "%3.2f";
        }

        String s = String.format(Locale.US, f_string, d_start);

        datachoice_START.setText(s);
    }

    public void setDataEND(double d_end) {

        String f_string;
        if (d_end > 100) {
            f_string = "%3.0f";
        } else {
            f_string = "%3.2f";
        }

        String s = String.format(Locale.US, f_string, d_end);
//        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
//        nf.setMaximumFractionDigits(4);
//        s = nf.format(data_range_end);
        datachoice_END.setText(s);
    }

    private double readDataSTART() {
        String text = datachoice_START.getText();
        double d_start;
        d_start = new Double(text);
        return d_start;
    }

    private double readDataEND() {
        double d_end = new Double(datachoice_END.getText());
        return d_end;
    }

    public boolean axisInverted() {
        return invBox.isSelected();
    }

    /**
     * GUI return *
     */
    public JCheckBox getInvBox() {
        return invBox;
    }

    public JToggleButton getIsXAxisButton() {
        return isXAxis;
    }

    public JLabel getNameLabel() {
        return nameLabel;
    }

    public JComboBox getPosChoice() {
        return poschoice;
    }

    public JComboBox getTypeChoice() {
        return typechoice;
    }

    public JComboBox getColorChoice() {
        return colorchoice;
    }

    public JLabel getLegendLabel() {
        return setLegend;
    }

    public JComboBox getDataChoice() {
        return setColumn;
    }

    public JComboBox getTimeChoiceSTART() {
        return timechoice_START;
    }

    public JComboBox getTimeChoiceEND() {
        return timechoice_END;
    }

    public JTextField getDataChoiceSTART() {
        return datachoice_START;
    }

    public JTextField getDataChoiceEND() {
        return datachoice_END;
    }

    public JButton getMaxButton() {
        return datachoice_max;
    }

    public JButton getAddButton() {
        return addButton;
    }

    public JButton getRemButton() {
        return remButton;
    }

    public JButton getUpButton() {
        return upButton;
    }

    public JButton getDownButton() {
        return downButton;
    }

    public JButton getPlotButton() {
        return this.plotButton;
    }

    public JButton getCustomizeButton() {
        return this.customizeButton;
    }

    public JLabel getColorLabel() {
        return this.colorlabel;
    }

    public JLabel getSColorLabel() {
        return this.scolorlabel;
    }

    //Methods for renderer configuration
    public void setSeriesPaint(Color paint) {
        this.series_paint = paint;
        cr_dlg.setStrokeButtonColor(paint);
    }

    public void setSeriesFillPaint(Color fill) {
        this.series_fill_paint = fill;
        cr_dlg.setFillButtonColor(fill);
    }

    public void setSeriesOutlinePaint(Color out) {
        this.series_outline_paint = out;
        cr_dlg.setOutlineButtonColor(out);
    }

    private void drawLegendItem() {
        JPanel pane = new JPanel();
        pane.setSize(20, 20);

    }

    public void setOutlineSlider(int value) {
        cr_dlg.setOutlineSlider(value);
    }

    public void setStrokeSlider(int value) {
        cr_dlg.setStrokeSlider(value);
    }

    public void setShapeSlider(int value) {
        cr_dlg.setShapeSlider(value);
    }

    public void setShapesVisBox(boolean state) {
        cr_dlg.setShapesVisBox(state);
    }

    public void setLinesVisBox(boolean state) {
        cr_dlg.setLinesVisBox(state);
    }

    public void setShapeBox(int index) {
        cr_dlg.setShapeBox(index);
    }

    MouseAdapter timeListener = new MouseAdapter() {
        public void mouseReleased(MouseEvent me) {
//            prop.setPossibleTimeIntervals();
            prop.setTimeSTART(timechoice_START.getSelectedIndex());
            prop.setTimeEND(timechoice_END.getSelectedIndex());
            //setVisible(false);
        }

        public void mousePressed(MouseEvent me) {
//            prop.setPossibleTimeIntervals();
            prop.setTimeSTART(timechoice_START.getSelectedIndex());
            prop.setTimeEND(timechoice_END.getSelectedIndex());
            //setVisible(false);
        }

        public void mouseClicked(MouseEvent me) {
//            prop.setPossibleTimeIntervals();
            prop.setTimeSTART(timechoice_START.getSelectedIndex());
            prop.setTimeEND(timechoice_END.getSelectedIndex());
            //setVisible(false);
        }
    };

    ActionListener timeListener2 = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            prop.setTimeSTART(timechoice_START.getSelectedIndex());
            prop.setTimeEND(timechoice_END.getSelectedIndex());
        }
    };

    ActionListener rendererListener = new ActionListener() {
        public void actionPerformed(ActionEvent te) {
//            System.out.println("plotType: "+plotType);
//            System.out.println("ctsconf:" + ctsconf.toString());

            if (plotType == 0) {

                prop.setPosition(poschoice.getSelectedIndex());
                ctsconf.handleRenderer();

            }
            if (plotType == 1) {
                prop.setPosition(poschoice.getSelectedIndex());
                cxyconf.handleRenderer();

            }

            //cxyconf.handleRenderer();
            //setVisible(false);
        }
    };

    ActionListener addListener = new ActionListener() {
        public void actionPerformed(ActionEvent te) {

            if (plotType == 0) {
                ctsconf.addGraph(prop);
            }
            if (plotType == 1) {
                cxyconf.addGraph(prop);
            }

            //setVisible(false);
        }
    };

    ActionListener removeListener = new ActionListener() {
        public void actionPerformed(ActionEvent te) {

            if (plotType == 0) {
                ctsconf.removeGraph(prop);
            }
            if (plotType == 1) {
                cxyconf.removeGraph(prop);

            }

            //setVisible(false);
        }
    };

    ActionListener upListener = new ActionListener() {
        public void actionPerformed(ActionEvent te) {

            if (plotType == 0) {
                ctsconf.upGraph(prop);
                //applyTSProperties();
            }
            if (plotType == 1) {
                cxyconf.upGraph(prop);

                //applyXYProperties();
            }

            //setVisible(false);
        }
    };

    ActionListener downListener = new ActionListener() {
        public void actionPerformed(ActionEvent te) {

            if (plotType == 0) {
                ctsconf.downGraph(prop);
                //applyTSProperties();
            }
            if (plotType == 1) {
                cxyconf.downGraph(prop);

                //applyXYProperties();
            }
        }
    };

    ActionListener isXListener = new ActionListener() {
        public void actionPerformed(ActionEvent xe) {

            cxyconf.xChanged(prop);
            cxyconf.setMaxDataIntervals(prop);
            updateGUI();
        }
    };

    ItemListener endListener = new ItemListener() {
        public void itemStateChanged(ItemEvent xe) {

//            time_END = timechoice_END.getSelectedIndex();
        }
    };

    ActionListener max_listener = new ActionListener() {
        public void actionPerformed(ActionEvent me) {

            cxyconf.setMaxDataIntervals(prop);
            updateGUI();
        }
    };

    ActionListener columnChangedListener = new ActionListener() {
        public void actionPerformed(ActionEvent me) {

            String name = (String) setColumn.getSelectedItem();
            prop.setName(name);
            prop.setSelectedColumn(setColumn.getSelectedIndex());

            setLegend.setText(name);
            cr_dlg.setLegendField(name);
            cr_dlg.setNameLabel(name);
        }
    };

    ActionListener customize_listener = new ActionListener() {
        public void actionPerformed(ActionEvent me) {

            //CustomizeRendererDlg cr_dlg = new CustomizeRendererDlg();
//            if(prop.getPosition().compareTo("left") == 0){
//                if(plotType == 0) prop.setRendererType(ctsconf.getRendererLeft());
//                if(plotType == 1) prop.setRendererType(cxyconf.getRendererLeft());
//
//            }
//            if(prop.getPosition().compareTo("right") == 0){
//                if(plotType == 0) prop.setRendererType(ctsconf.getRendererRight());
//                if(plotType == 1) prop.setRendererType(cxyconf.getRendererRight());
//            }
            cr_dlg.handleGUI();
            cr_dlg.updateName(prop.getName());

            cr_dlg.setVisible(true);
        }
    };
//// ACHTUNG: für die TextFields reichen Action Listener nicht!!
    ActionListener dataSTARTListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            datachoice_START.selectAll();

        }
    };

    ActionListener dataENDListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            datachoice_END.selectAll();
        }

    };

    MouseAdapter column_listener = new MouseAdapter() {
        public void mouseReleased(MouseEvent me) {
            prop.setSelectedColumn(setColumn.getSelectedIndex());

        }
    };

    DocumentListener d_start_listener = new DocumentListener() {
        double d_start;

        public void changedUpdate(DocumentEvent e) {
//            cxyconf.dStartChanged(true);
            changeValue();
        }

        public void removeUpdate(DocumentEvent e) {
            changeValue();

        }

        public void insertUpdate(DocumentEvent e) {
            changeValue();

        }

        private void changeValue() {
            try {
                d_start = new Double(datachoice_START.getText());
            } catch (NumberFormatException nfe) {
                d_start = 0;
            }
            prop.setDataSTART(d_start);
            cxyconf.dStartChanged(true);
//            System.out.println("d_changed!");
        }
    };

//    DocumentListener legend_changed_listener = new DocumentListener(){
//        public void changedUpdate(DocumentEvent e){
//            setLegendName(setLegend.getText());
//
//        }
//        public void removeUpdate(DocumentEvent e){
//            setLegendName(setLegend.getText());
//
//        }
//        public void insertUpdate(DocumentEvent e){
//            setLegendName(setLegend.getText());
//
//        }
//    };
    DocumentListener d_end_listener = new DocumentListener() {
        double d_end;

        public void changedUpdate(DocumentEvent e) {
            changeValue();

        }

        public void removeUpdate(DocumentEvent e) {
            changeValue();

        }

        public void insertUpdate(DocumentEvent e) {
            changeValue();

        }

        private void changeValue() {
            try {
                d_end = new Double(datachoice_END.getText());
            } catch (NumberFormatException nfe) {
                d_end = 0;
            }
            prop.setDataEND(d_end);
            cxyconf.dEndChanged(true);
//            System.out.println("d_end_changed");
        }
    };

    private class CustomizeRendererDlg extends JDialog {

        boolean result = false;
        int max;
        String side;
        int side_index;
        int position;

        JDialog thiscrd = this;

        JPanel optionspanel;
        JPanel colorpanel;
        JPanel buttonpanel;
        JPanel namepanel;

        Color line_color;
        Color shape_fill;
        Color outline_color;

        JLabel renderer_label;
        JLabel stroke_label;
        JLabel shape_label;
        JLabel paint_label;
        JLabel outline_stroke_label;
        JLabel outline_paint_label;
        JLabel lines_visible_label;
        JLabel shapes_visible_label;
        JLabel fill_label;
        JLabel shape_size_label;

        JLabel setLegendLabel;
        JLabel nameLabel;
        JTextField setLegendField;

        JComboBox renderer_box;
        JComboBox stroke_box; //list for different strokes!
        JComboBox shape_box; //list for different shapes!!
        JComboBox paint_box;
        JComboBox outline_stroke_box;
        JComboBox outline_paint_box;//color chooser!!
        JComboBox fill_box;
        JCheckBox shapes_vis_box;
        JCheckBox lines_vis_box;
        JComboBox shape_size_box;

        JSlider stroke_slider;
        JSlider shape_slider;
        JSlider outline_slider;

        JSeparator divider;

        JButton stroke_button;
        JButton fill_button;
        JButton outline_button;

        JButton ok_button;
        JButton apply_button;

        final String[] SHAPES = {
            JAMS.i18n("SQUARE"),
            JAMS.i18n("CIRCLE"),
            JAMS.i18n("TRIANGLE_UP"),
            JAMS.i18n("TRIANGLE_DOWN"),
            JAMS.i18n("DIAMOND"),
            JAMS.i18n("CROSS_DIAGONAL"),
            JAMS.i18n("CROSS_REGULAR")};//, "Square", "Star"};
        final String[] COLORS = {"custom", "red", "blue", "green", "black", "magenta", "cyan", "yellow", "gray", "orange", "lightgray", "pink"};
        final String[] SHAPE_COLORS = {"custom", "white", "red", "blue", "green", "black", "magenta", "cyan", "yellow", "gray", "orange", "lightgray", "pink"};
        final String[] RENDERER = {"Line and Shape", "Bar", "Area", "Step", "StepArea", "Difference"};

        final int MIN = 0;
        final int MAX = 12;

        final int STROKE = 2;
        final int SHAPE = 5;
        final int OUTLINE = 1;

        public CustomizeRendererDlg(String series_name) {
            super(parent, JAMS.i18n("CUSTOMIZE_RENDERER"), true);

            Point parentloc = parent.getLocation();
            setLocation(parentloc.x + 50, parentloc.y + 50);

            try {
                line_color = (Color) prop.getSeriesPaint();
                shape_fill = (Color) prop.getSeriesFillPaint();
                outline_color = (Color) prop.getSeriesOutlinePaint();
            } catch (Exception cce) {
                line_color = Color.RED;
                shape_fill = Color.RED;
                outline_color = Color.GRAY;
            }

            createPanel();
        }

        public void updateName(String name) {

            nameLabel.setText(name);
        }

        public void updateColors() {

            line_color = prop.getSeriesPaint();
            shape_fill = prop.getSeriesFillPaint();
            outline_color = prop.getSeriesOutlinePaint();

            stroke_button.setBackground(line_color);
            fill_button.setBackground(shape_fill);
            outline_button.setBackground(outline_color);

        }

        public void setStrokeButtonColor(Color lc) {
            stroke_button.setBackground(lc);
        }

        public void setFillButtonColor(Color fc) {
            fill_button.setBackground(fc);
        }

        public void setOutlineButtonColor(Color oc) {
            outline_button.setBackground(oc);
        }

        public void setOutlineSlider(int value) {
            outline_slider.setValue(value);
        }

        public void setStrokeSlider(int value) {
            stroke_slider.setValue(value);
        }

        public void setShapeSlider(int value) {
            shape_slider.setValue(value);
        }

        public void setShapesVisBox(boolean state) {
            shapes_vis_box.setSelected(state);
        }

        public void setLinesVisBox(boolean state) {
            lines_vis_box.setSelected(state);
        }

        public void setShapeBox(int index) {
            shape_box.setSelectedIndex(index);
        }

        public void setLegendField(String legendName) {
            setLegendField.setText(legendName);
        }

        void createPanel() {
            optionspanel = new JPanel();
            colorpanel = new JPanel();
            buttonpanel = new JPanel();
            namepanel = new JPanel();
            GridBagLayout gbl = new GridBagLayout();
            BorderLayout brl = new BorderLayout();
            GridBagLayout option_gbl = new GridBagLayout();
            GridBagLayout button_gbl = new GridBagLayout();

//            setLayout(gbl);
            setLayout(brl);
            optionspanel.setLayout(option_gbl);

            ok_button = new JButton(JAMS.i18n("CLOSE"));
            ok_button.addActionListener(ok);

            apply_button = new JButton(JAMS.i18n("APPLY"));
            apply_button.addActionListener(apply);

            paint_label = new JLabel(JAMS.i18n("COLOR:"));
//            renderer_label = new JLabel("Renderer Type:");
//            renderer_box = new JComboBox(RENDERER);
//            renderer_box.setSelectedIndex()
            stroke_label = new JLabel(JAMS.i18n("STROKE:"));
            nameLabel = new JLabel(prop.getName());
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
//            stroke_box = new JComboBox(STROKES);
//            stroke_box.setSelectedIndex(2);

            shape_label = new JLabel(JAMS.i18n("SHAPE:"));
            shape_box = new JComboBox(SHAPES);
            shape_box.setSelectedIndex(prop.getShapeType());
            shape_box.addActionListener(shape_listener2);
            shape_box.addMouseListener(shape_listener);

            outline_stroke_label = new JLabel(JAMS.i18n("OUTLINE_STROKE:"));
//            outline_stroke_box = new JComboBox(STROKES);
//            outline_stroke_box.setSelectedIndex(2);

            outline_paint_label = new JLabel(JAMS.i18n("OUTLINE_COLOR:"));
            outline_paint_box = new JComboBox(SHAPE_COLORS);
            outline_paint_box.setSelectedIndex(prop.getShapeType());

            fill_label = new JLabel(JAMS.i18n("COLOR:"));
            fill_box = new JComboBox(SHAPE_COLORS);
            fill_box.setSelectedIndex(2);

            setLegendLabel = new JLabel(JAMS.i18n("LEGEND_NAME:"));
            setLegendField = new JTextField();

            shape_size_label = new JLabel(JAMS.i18n("SIZE"));
//            shape_size_box = new JComboBox(SIZES);
//            shape_size_box.setSelectedIndex(2);
            //shape_size_box.setEnabled(false);
            //paint_box.setSelectedIndex()
            lines_visible_label = new JLabel(JAMS.i18n("LINES"));
            lines_vis_box = new JCheckBox();
            lines_vis_box.setSelected(prop.getLinesVisible());
            shapes_visible_label = new JLabel(JAMS.i18n("SHAPES"));
            shapes_vis_box = new JCheckBox();
            shapes_vis_box.setSelected(prop.getLinesVisible());

            stroke_slider = new JSlider(JSlider.HORIZONTAL, MIN, MAX, STROKE);
            shape_slider = new JSlider(JSlider.HORIZONTAL, 1, MAX, SHAPE);
            outline_slider = new JSlider(JSlider.HORIZONTAL, MIN, MAX, OUTLINE);

            stroke_slider.setMajorTickSpacing(4);
            stroke_slider.setMinorTickSpacing(1);
            stroke_slider.setPaintTicks(true);
            stroke_slider.setPaintLabels(true);
            stroke_slider.setSnapToTicks(true);

            shape_slider.setMajorTickSpacing(4);
            shape_slider.setMinorTickSpacing(1);
            shape_slider.setPaintTicks(true);
            shape_slider.setPaintLabels(true);
            shape_slider.setSnapToTicks(true);

            outline_slider.setMajorTickSpacing(4);
            outline_slider.setMinorTickSpacing(1);
            outline_slider.setPaintTicks(true);
            outline_slider.setPaintLabels(true);
            outline_slider.setSnapToTicks(true);

            divider = new JSeparator(JSeparator.VERTICAL);

//            stroke_button = new ColorButton(line_color);
//            fill_button = new ColorButton(shape_fill);
//            outline_button = new ColorButton(outline_color);
            stroke_button = new JButton(JAMS.i18n("COLOR"));
            fill_button = new JButton(JAMS.i18n("COLOR"));
            outline_button = new JButton(JAMS.i18n("COLOR"));

            stroke_button.setBackground(line_color);
            fill_button.setBackground(shape_fill);
            outline_button.setBackground(outline_color);

            stroke_button.setSize(15, 15);
            fill_button.setSize(15, 15);
            outline_button.setSize(15, 15);

            stroke_button.addActionListener(stroke_button_listener);
            fill_button.addActionListener(fill_button_listener);
            outline_button.addActionListener(outline_button_listener);

            lines_vis_box.addActionListener(lines_visible_listener);
            shapes_vis_box.addActionListener(shapes_visible_listener);

            stroke_slider.addMouseListener(stroke_slider_listener);
            shape_slider.addMouseListener(shape_listener);
            outline_slider.addMouseListener(outline_stroke_listener);

            setLegendField.addKeyListener(legend_listener);

            shape_box.addMouseListener(shape_listener);
            //name

            //optionpanel
            GUIHelper.addGBComponent(optionspanel, option_gbl, new JLabel(JAMS.i18n("LINE")), 0, 0, 1, 1, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, new JLabel(JAMS.i18n("SYMBOL")), 4, 0, 1, 1, 0, 0);

            //lines
            GUIHelper.addGBComponent(optionspanel, option_gbl, stroke_label, 0, 1, 1, 1, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, stroke_slider, 1, 1, 1, 2, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, paint_label, 0, 3, 1, 1, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, stroke_button, 1, 3, 1, 1, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, lines_visible_label, 0, 4, 1, 1, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, lines_vis_box, 1, 4, 1, 1, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, shapes_visible_label, 0, 5, 1, 1, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, shapes_vis_box, 1, 5, 1, 1, 0, 0);
            //legend
            GUIHelper.addGBComponent(optionspanel, option_gbl, setLegendLabel, 0, 6, 1, 1, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, setLegendField, 1, 6, 1, 1, 0, 0);
            //divider
            GUIHelper.addGBComponent(optionspanel, option_gbl, divider, 2, 1, 1, 8, 1, 1);
            //shapes
            GUIHelper.addGBComponent(optionspanel, option_gbl, shape_label, 4, 1, 1, 1, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, shape_box, 5, 1, 1, 1, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, shape_size_label, 4, 2, 1, 1, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, shape_slider, 5, 2, 1, 2, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, fill_label, 4, 4, 1, 1, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, fill_button, 5, 4, 1, 1, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, outline_stroke_label, 4, 5, 1, 1, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, outline_slider, 5, 5, 1, 2, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, outline_paint_label, 4, 7, 1, 1, 0, 0);
            GUIHelper.addGBComponent(optionspanel, option_gbl, outline_button, 5, 7, 1, 1, 0, 0);

            //buttonpanel
            GUIHelper.addGBComponent(buttonpanel, button_gbl, ok_button, 0, 0, 1, 1, 1, 1);
            GUIHelper.addGBComponent(buttonpanel, button_gbl, apply_button, 1, 0, 1, 1, 1, 1);
//            GUIHelper.addGBComponent(buttonpanel, button_gbl, cancel_button, 1, 0, 1, 1, 1, 1);

            namepanel.add(nameLabel);

            //this-panel
//            GUIHelper.addGBComponent(this, gbl, optionspanel, 0, 0, 1, 6, 1, 1);
//            GUIHelper.addGBComponent(this, gbl, colorpanel  , 1, 0, 1, 5, 1, 1);
//            GUIHelper.addGBComponent(this, gbl, buttonpanel , 1, 5, 1, 1, 1, 1);
            add(namepanel, brl.NORTH);
            add(optionspanel, brl.CENTER);
            add(buttonpanel, brl.SOUTH);

            //default values IN GRAPH PROPERTIES!!!
//            setStroke(stroke_slider.getValue());
//            setShape(shape_box.getSelectedIndex(), shape_slider.getValue());
//
//            setOutlineStroke(outline_slider.getValue());
//            setLinesVisible(lines_vis_box.isSelected());
//            setShapesVisible(shapes_vis_box.isSelected());
            handleGUI();

            pack();
            setVisible(false);
        }

        private void handleGUI() {
            if (prop.getRendererType() == 0) { //line and shape

                //renderer_box.setEnabled(true);
                stroke_slider.setEnabled(true); //list for different strokes!
                shape_box.setEnabled(true); //list for different shapes!!
                stroke_button.setEnabled(true);
                outline_slider.setEnabled(true);
                outline_button.setEnabled(true);//color chooser!!
                fill_button.setEnabled(true);
                shapes_vis_box.setEnabled(true);
                lines_vis_box.setEnabled(true);
                shape_slider.setEnabled(true);
            }
            if (prop.getRendererType() == 2) { //area

                //renderer_box.setEnabled(true);
                stroke_slider.setEnabled(false); //list for different strokes!
                shape_box.setEnabled(false); //list for different shapes!!
                stroke_button.setEnabled(true);
                outline_slider.setEnabled(false);
                outline_button.setEnabled(false);//color chooser!!
                fill_button.setEnabled(false);
                shapes_vis_box.setEnabled(false);
                lines_vis_box.setEnabled(false);
                shape_slider.setEnabled(false);
            }
            if (prop.getRendererType() == 5) { //difference

                //renderer_box.setEnabled(true);
                stroke_slider.setEnabled(true); //list for different strokes!
                shape_box.setEnabled(true); //list for different shapes!!
                stroke_button.setEnabled(true);
                outline_slider.setEnabled(false);
                outline_button.setEnabled(false);//color chooser!!
                fill_button.setEnabled(false);
                shapes_vis_box.setEnabled(true);
                lines_vis_box.setEnabled(true);
                shape_slider.setEnabled(true);
            }

            if (prop.getRendererType() == 1 || prop.getRendererType() == 3 || prop.getRendererType() == 4) { //bars and steps

                //renderer_box.setEnabled(true);
                stroke_slider.setEnabled(false); //list for different strokes!
                shape_box.setEnabled(false); //list for different shapes!!
                stroke_button.setEnabled(true);
                outline_slider.setEnabled(false);
                outline_button.setEnabled(false);//color chooser!!
                fill_button.setEnabled(false);
                shapes_vis_box.setEnabled(false);
                lines_vis_box.setEnabled(false);
                shape_slider.setEnabled(false);
            }
        }

        private void setNameLabel(String name) {
            nameLabel.setText(name);
        }

        MouseAdapter stroke_slider_listener = new MouseAdapter() {

            public void mouseReleased(MouseEvent me) {
                prop.setStroke(stroke_slider.getValue());
            }
        };

        MouseAdapter shape_listener = new MouseAdapter() {

            public void mouseReleased(MouseEvent me) {
                prop.setShape(shape_box.getSelectedIndex(), shape_slider.getValue());
            }
        };

        ActionListener shape_listener2 = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                prop.setShape(shape_box.getSelectedIndex(), shape_slider.getValue());

            }
        };

        MouseAdapter outline_stroke_listener = new MouseAdapter() {

            public void mouseReleased(MouseEvent me) {
                prop.setOutlineStroke(outline_slider.getValue());
            }
        };

        KeyAdapter legend_listener = new KeyAdapter() {

            public void keyTyped(KeyEvent ke) {
                prop.setLegendName(setLegendField.getText());

                setLegend.setText(setLegendField.getText());

            }

            public void keyReleased(KeyEvent ke) {
                prop.setLegendName(setLegendField.getText());

                setLegend.setText(setLegendField.getText());

            }
        };

        ActionListener lines_visible_listener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                prop.setLinesVisible(lines_vis_box.isSelected());
            }
        };

        ActionListener shapes_visible_listener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                prop.setShapesVisible(shapes_vis_box.isSelected());
            }
        };
//                -setStroke(stroke_slider.getValue());
//                -setShape(shape_box.getSelectedIndex(), shape_slider.getValue());
//
//                setSeriesPaint(line_color);
//                setSeriesFillPaint(shape_fill);
//                setSeriesOutlinePaint(outline_color);
//                -setOutlineStroke(outline_slider.getValue());
//                -setLinesVisible(lines_vis_box.isSelected());
//                -setShapesVisible(shapes_vis_box.isSelected());
//                result = true;
//
//                setLegendName(setLegendField.getText());

        ActionListener ok = new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                setVisible(false);

                updateColors();
                setColorLabelColor();
                setSColorLabelColor();
            }
        };

        ActionListener apply = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateColors();
                setColorLabelColor();
                setSColorLabelColor();
                if (ctsconf != null) {
                    ctsconf.plotAllGraphs();
                }
                if (cxyconf != null) {
                    cxyconf.plotAllGraphs();
                }
            }
        };

        ActionListener stroke_button_listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color new_line_color = JColorChooser.showDialog(thiscrd, "Choose Line Color", line_color);
                if (new_line_color != null) {
                    line_color = new_line_color;
                }

                prop.setSeriesPaint(line_color);
                stroke_button.setBackground(line_color);
            }
        };

        ActionListener fill_button_listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color new_shape_fill = JColorChooser.showDialog(thiscrd, "Choose Shape Color", shape_fill);
                if (new_shape_fill != null) {
                    shape_fill = new_shape_fill;
                }
                prop.setSeriesFillPaint(shape_fill);
                fill_button.setBackground(shape_fill);
            }
        };

        ActionListener outline_button_listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color new_outline_color = JColorChooser.showDialog(thiscrd, "Choose Outline Color", outline_color);
                if (new_outline_color != null) {
                    outline_color = new_outline_color;
                }

                prop.setSeriesOutlinePaint(outline_color);
                outline_button.setBackground(outline_color);

            }
        };

        ActionListener cancel = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                //WAS PASSIERT BEI CANCEL?
            }
        };

    }

    public class ColorButton extends JButton {

        Color color;

        public ColorButton(Color color) {
            this.color = color;
        }

        public void paint(Graphics g) {

            g.setColor(color);
            g.fillRect(1, 1, 14, 14);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(0, 0, 15, 15);
        }

        public void setColor(Color newColor) {
            this.color = newColor;
        }
    }

    public class ColorLabel extends JLabel {

        Color shape_fill;
        Color outline_color;
        Color line_color;
        Shape shape;

        public ColorLabel(Color shape_fill, Color outline_color, Shape shape) {
            this.shape_fill = shape_fill;
            this.outline_color = outline_color;
            this.shape = shape;
        }

        public void paint(Graphics2D g) {

            g.setColor(shape_fill);
            g.fill(shape);
            g.setColor(outline_color);
            g.draw(shape);
        }

        public void paint(Graphics g) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 15, 15);
        }

        public void setSymbol(Shape shape, Color shape_fill, Color outline_color) {
            this.shape_fill = shape_fill;
            this.outline_color = outline_color;
            this.shape = shape;
        }

    }

//    private class ItemPanel extends JPanel{
//
//        Shape shape;
//        Color line_color;
//        Color outline_color;
//        Color shape_fill;
//
//        public ItemPanel(){
//            shape = getSeriesShape();
//            line_color = (Color) getSeriesPaint();
//            outline_color = (Color) getSeriesOutlinePaint();
//            shape_fill = (Color) getSeriesFillPaint();
//        }
//
//        public void paintComponent(Graphics2D g){
//
//            g.setColor(shape_fill);
//            g.fill(shape);
//            g.setColor(outline_color);
//            g.draw(shape);
//
//            g.setColor(Color.BLACK);
//            g.fillRect(0,0,5,5);
//        }
//    }
}
