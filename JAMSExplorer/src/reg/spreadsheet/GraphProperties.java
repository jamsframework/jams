/*
 * GraphProperties.java
 *
 * Created on 29. September 2007, 17:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package reg.spreadsheet;

/**
 *
 * @author p4riro
 */
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.event.*;
import javax.swing.JButton;
import javax.swing.JPanel;

import java.lang.Math.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import java.text.*;

import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.*;
import org.jfree.util.ShapeUtilities.*;

import jams.data.JAMSCalendar;
import jams.gui.LHelper;
import java.util.Vector;


public class GraphProperties {
//    
//    private static ImageIcon UP_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("jams/components/gui/resources/arrowup.png")).getImage().getScaledInstance(9, 5, Image.SCALE_SMOOTH));
//    private static ImageIcon DOWN_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("jams/components/gui/resources/arrowdown.png")).getImage().getScaledInstance(9, 5, Image.SCALE_SMOOTH));
    
    JFrame parent;
    
    URL url1 = this.getClass().getResource("/reg/resources/images/arrowup.png");
    ImageIcon up_icon = new ImageIcon(new ImageIcon(url1).getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH));
    
    URL url2 = this.getClass().getResource("/reg/resources/images/arrowdown.png");
    ImageIcon down_icon = new ImageIcon(new ImageIcon(url2).getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH));
    
    URL url3 = this.getClass().getResource("/reg/resources/images/correct.png");
    ImageIcon plot_icon = new ImageIcon(url3);
    
    URL url4 = this.getClass().getResource("/reg/resources/images/add.png");
    ImageIcon add_icon = new ImageIcon(url4);
    
    URL url5 = this.getClass().getResource("/reg/resources/images/remove.png");
    ImageIcon rem_icon = new ImageIcon(new ImageIcon(url5).getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH));
    //ImageIcon(getModel().getRuntime().getClassLoader().getResource("jams/components/gui/resources/root.png
    
    GraphProperties thisProp;
    
    JTable table;
    
    TimeSeries ts;
    //TimeSeriesCollection dataset;
    
    XYSeries xys;
    
    
    JScrollPane scpane;
    
    int index = 0;
    String legendName;
    int color;
    String name;
    String position; // left/right
    int type; //renderer index
    int[] d_range = new int[2];
    
    boolean is_x_series = false;
    boolean result = false;
    boolean x_changed;
    
    int selectedColumn;
    int[] rowSelection;
    int x_series_col;
    
    int stroke_type;
    int shape_type;
    int size_type;
    int outline_type;
    
    double data_range_start;
    double data_range_end;
    
    int plotType;
    
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
    JTextField setLegend;
    
    JTSConfigurator ctsconf;
    JXYConfigurator cxyconf;
    STPConfigurator stpconf;
    
    //XYPair[] data;
    double[] x_dataIntervals;
    double[] y_data;
    
    private String[] colors = {"red","blue","green","black","magenta","cyan","yellow","gray","orange","lightgray","pink"};
    private String[] types = {"Line","Bar","Area","Line and Base","Dot","Step","StepArea","Difference"};
    private String[] positions = {"left","right"}; 
    
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
    
    //String column[];
    
    /** Creates a new instance of GraphProperties */
    public GraphProperties(JTSConfigurator ctsconf) {
        
        this.parent = ctsconf;
        this.plotType = 0;
        
        this.ctsconf = ctsconf;
        this.thisProp = this;
        
        this.table = ctsconf.table;
        this.position = "left";
        this.name = "Graph Name";
        this.legendName = this.name;
        
        this.selectedColumn = 0;
        this.rowSelection = null;
        
        columnCount = table.getColumnCount();
        rowCount = table.getRowCount();
        
        String[] timeIntervals = new String[table.getRowCount()];
        for(int i=0; i<table.getRowCount(); i++){
            timeIntervals[i] = table.getValueAt(i,0).toString();
        }
        
        timechoice_START = new JComboBox(timeIntervals);
        timechoice_START.setPreferredSize(new Dimension(40,14));
        timechoice_START.addActionListener(timeListener);
        
        timechoice_END = new JComboBox(timeIntervals);
        timechoice_END.setPreferredSize(new Dimension(40,14));
        timechoice_END.addActionListener(timeListener);
        
        colorTable.put("yellow", Color.yellow);
        colorTable.put("orange", Color.orange);
        colorTable.put("red", Color.red);
        colorTable.put("pink", Color.pink);
        colorTable.put("magenta", Color.magenta);
        colorTable.put("cyan", Color.cyan);
        colorTable.put("blue", Color.blue);
        colorTable.put("green", Color.green);
        colorTable.put("gray", Color.gray);
        colorTable.put("lightgray", Color.lightGray);
        colorTable.put("black", Color.black);
        colorTable.put("white", Color.WHITE);
        
//        for(int i=0;i<columnCount;i++){ 
//               
//                if(i!=0){
//                    column[i] = table.getColumnName(i);
//                }else{
//                    column[i] = "---";
//                }
//        }
        
        createPanel();
        //applyTSProperties();
        
    }
    
    public GraphProperties(JXYConfigurator cxyconf) {
        
        
        this.parent = cxyconf;
        
        this.plotType = 1;

        this.cxyconf = cxyconf;
        this.thisProp = this;
        
        this.table = cxyconf.table;
        //this.color = "red";
        this.position = "left";
        this.name = "Graph Name";
        this.legendName = this.name;
        
        this.rowSelection = null;
        
        
        columnCount = table.getColumnCount();
        rowCount = table.getRowCount();
        //data = new XYPair[table.getRowCount()];

        rowSelection = table.getSelectedRows();
        x_series_col = table.getSelectedColumn();
        
        datachoice_panel = new JPanel();
        datachoice_panel.setLayout(new FlowLayout());
        datachoice_panel.setSize(20,50);
        datachoice_max = new JButton("max");
        //datachoice_max.setPreferredSize(new Dimension(5,10));
        
        datachoice_max.addActionListener(max_listener);
        
        datachoice_START = new JTextField();
        datachoice_START.setPreferredSize(new Dimension(40,14));
        datachoice_START.addMouseListener(dataSTARTListener);
        
        datachoice_START.getDocument().addDocumentListener(d_start_listener);

        datachoice_END = new JTextField();
        datachoice_END.setPreferredSize(new Dimension(40,14));
        datachoice_END.addMouseListener(dataENDListener);
        
        datachoice_END.getDocument().addDocumentListener(d_end_listener);
        
        datachoice_panel.add(datachoice_END);
        datachoice_panel.add(datachoice_max);
        
        //column = new String[columnCount];
        
//        for(int i=0;i<columnCount;i++){ 
//
//               column[i] = table.getColumnName(i);
//     
//            }
        
        createPanel();     
    }
    
    public GraphProperties(STPConfigurator stpconf) {
        //for stacked time plot
        this.parent = stpconf;
        this.plotType = 2;
        //super(parent, "Select Properties");
        //this.parent = parent;
        //setLayout(new FlowLayout());
        //Point parentloc = parent.getLocation();
        this.stpconf = stpconf;
        this.thisProp = this;
        //setLocation(parentloc.x + 30, parentloc.y + 30);
        
        //this.table = stpconf.table;
        //this.color = "red";
        this.position = "left";
        this.name = "Graph Name";
        this.legendName = this.name;
        
        this.selectedColumn = 0;
        this.rowSelection = null;
    
        columnCount = stpconf.getColumnCount();
        rowCount = stpconf.getRowCount();

        String[] timeIntervals = new String[rowCount];
        for(int i=0; i<rowCount; i++){
            timeIntervals[i] = stpconf.timeVector.get(i).toString();
        }
        
        timechoice_START = new JComboBox(timeIntervals);
        timechoice_START.setPreferredSize(new Dimension(40,14));
        timechoice_START.addActionListener(timeListener);
        
        timechoice_END = new JComboBox(timeIntervals);
        timechoice_END.setPreferredSize(new Dimension(40,14));
        timechoice_END.addActionListener(timeListener);
        
        colorTable.put("yellow", Color.yellow);
        colorTable.put("orange", Color.orange);
        colorTable.put("red", Color.red);
        colorTable.put("pink", Color.pink);
        colorTable.put("magenta", Color.magenta);
        colorTable.put("cyan", Color.cyan);
        colorTable.put("blue", Color.blue);
        colorTable.put("green", Color.green);
        colorTable.put("gray", Color.gray);
        colorTable.put("lightgray", Color.lightGray);
        colorTable.put("black", Color.black);
        colorTable.put("white", Color.WHITE);
        
        
        
        createPanel();
        //applyTSProperties();
        
    }
    
    public void createPanel(){
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new FlowLayout());
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new FlowLayout());
        
        addButton = new JButton();
        remButton = new JButton();
        plotButton = new JButton();
        upButton = new JButton();
        downButton = new JButton();
        customizeButton = new JButton("customize");
        
        upButton.setIcon(up_icon);
        downButton.setIcon(down_icon);
        plotButton.setIcon(plot_icon);
        addButton.setIcon(add_icon);
        remButton.setIcon(rem_icon);
        
        plotButton.setToolTipText("plot graph");
        upButton.setToolTipText("move up");
        downButton.setToolTipText("move down");
        addButton.setToolTipText("add graph");
        remButton.setToolTipText("remove button");
        
        invBox = new JCheckBox("invert axis");
        isXAxis = new JRadioButton("set X");
        isXAxis.addActionListener(isXListener);
        
        addButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addButton.setPreferredSize(new Dimension(20,14));
        
        addButton.addActionListener(addListener);
        remButton.addActionListener(removeListener);
        upButton.addActionListener(upListener);
        downButton.addActionListener(downListener);
        customizeButton.addActionListener(customize_listener);
        
//        remButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        remButton.setPreferredSize(new Dimension(20,14));
//        upButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        upButton.setPreferredSize(new Dimension(20,14));
//        plotButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        plotButton.setPreferredSize(new Dimension(20,14));
//        downButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        downButton.setPreferredSize(new Dimension(20,14));
        
        colorchoice = new JComboBox(colors);
        colorchoice.setPreferredSize(new Dimension(40,14));
        colorchoice.setSelectedIndex(0);
        //colorchoice.addActionListener(okListener);
        
        typechoice = new JComboBox(types);
        typechoice.setPreferredSize(new Dimension(40,14));
        typechoice.setSelectedIndex(0);
        //typechoice.addActionListener(okListener);
        
        poschoice = new JComboBox(positions);
        poschoice.setPreferredSize(new Dimension(40,14));
        poschoice.setSelectedIndex(0);
        poschoice.addActionListener(rendererListener);
        //poschoice.addActionListener(okListener);
       
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("CANCEL");
        //JButton propButton = new JButton("...");

        //JLabel namelabel = new JLabel();
        JLabel setNameLabel =   new JLabel("        Name:");
        JLabel setColumnLabel = new JLabel("  Set Column:");
        JLabel setLegendLabel = new JLabel("Legend Entry:");
        nameLabel = new JLabel();

        String[] column = new String[columnCount];
        for(int i=0;i<columnCount;i++){ 
            
             switch(plotType){
                 case 0:
                    if(i!=0){
                        column[i] = table.getColumnName(i);
                    }else{
                        column[i] = "---";
                    } break;
                  
                 case 1:    
                    column[i] = table.getColumnName(i);
                    break;
                    
                 case 2:
                    column = stpconf.getHeaders();
                    break;
                    
             }
        }
            
//                if(this.plotType == 0){
//                    if(i!=0){
//                        column[i] = table.getColumnName(i);
//                    }else{
//                        column[i] = "---";
//                    }
//                }else{
//                    column[i] = table.getColumnName(i);
//                    }
            
        
        
//        Class test = table.getValueAt(0, 0).getClass();

//            for(int i=0;i<columnCount;i++){ 
//                if(this.plotType == 0){
//                    if(i!=0){
//                        column[i] = table.getColumnName(i);
//                    }else{
//                        column[i] = "---";
//                    }
//                }else{
//                    column[i] = table.getColumnName(i);
//                    }
//            }
        
        setColumn = new JComboBox(column);
        setColumn.setPreferredSize(new Dimension(40,14));
        setColumn.setSelectedIndex(1);
        nameLabel.setText((String) setColumn.getSelectedItem());
        
        String name = (String) setColumn.getSelectedItem();

        setName = new JTextField(name, 14);
        setName.setPreferredSize(new Dimension(40,14));
        setLegend = new JTextField(name, 14);
        setLegend.setPreferredSize(new Dimension(40,14));
        
        namePanel.add(setNameLabel);
        namePanel.add(setName);
        legendPanel.add(setLegendLabel);
        legendPanel.add(setLegend);
        
        this.datapanel.setLayout(new FlowLayout());
        this.graphpanel.setLayout(new FlowLayout());
        
        this.graphpanel.add(setColumn);
        this.graphpanel.add(poschoice);
        this.graphpanel.add(typechoice);
        this.graphpanel.add(colorchoice);
         
        this.datapanel.add(nameLabel);

        this.buttonpanel.add(okButton);
        this.buttonpanel.add(cancelButton);
        
        cr_dlg = new CustomizeRendererDlg();
        
        //linecolor label
        colorlabel = new JLabel("      ");
        colorlabel.setOpaque(true);
        colorlabel.setBackground(getSeriesPaint());
        //shapecolorlabel
        scolorlabel = new JLabel("");
        scolorlabel.setOpaque(false);
        scolorlabel.setBackground(getSeriesPaint());

        //plotButton.addActionListener(okListener);
    }
    
    public void applySelectedColumn(){
        selectedColumn = setColumn.getSelectedIndex();
    }
    
    public void applyTSProperties(){
        JAMSCalendar time;
        double value;
        int timeSTART = getTimeSTART();
        int timeEND = getTimeEND();
        selectedColumn = setColumn.getSelectedIndex();
        //color = (String) colorchoice.getSelectedItem();
        ts = new TimeSeries(getLegendName(), Second.class);
        
        for(int i=timeSTART; i<=timeEND; i++){
            
            time =  (JAMSCalendar) table.getValueAt(i,0); //ONLY FOR TIME SERIES TABLE WITH TIME IN COL 0!!!
//            if(!setColumn.getSelectedItem().equals("---")){
            value = (Double) table.getValueAt(i, selectedColumn);
            ts.add(new Second(new Date(time.getTimeInMillis())), value);
//            }
        }
        cr_dlg.updateColors();
    }

    public void applyXYProperties(){

        selectedColumn = setColumn.getSelectedIndex();
        
        xys = new XYSeries(getLegendName());
       
        for(int i=this.d_range[0]; i<=this.d_range[1]; i++){
            xys.add(cxyconf.sorted_Row[i].col[x_series_col], cxyconf.sorted_Row[i].col[selectedColumn]);
            
        }
        
        cr_dlg.updateColors();    
    }
    
    public void applySTPProperties(Vector<double[]> rowVector, Vector<JAMSCalendar> timeVector){
        JAMSCalendar time;
        double value;
        int timeSTART = getTimeSTART();
        int timeEND = getTimeEND();
        selectedColumn = setColumn.getSelectedIndex();
        //color = (String) colorchoice.getSelectedItem();
        ts = new TimeSeries(getLegendName(), Second.class);
        
        double row[] = new double[columnCount];
        
        for(int i=timeSTART; i<=timeEND; i++){
            
            row = rowVector.get(i);
            time =  timeVector.get(i); //ONLY FOR TIME SERIES TABLE WITH TIME IN COL 0!!!
//            if(!setColumn.getSelectedItem().equals("---")){
            value = row[selectedColumn];//table.getValueAt(i, selectedColumn);
            ts.add(new Second(new Date(time.getTimeInMillis())), value);
//            }
        }
        
    }

    public TimeSeries getTS(){
        return ts;
    }
    
    public XYSeries getXYS(){
        return xys;
    }
    
    public void setIndex(int index){
        this.index = index;
    }
    
    public int getIndex(){
        return index;
    }
 
    public void setColorLabelColor(){
        if(linesVisible) colorlabel.setBackground(getSeriesPaint());
        else colorlabel.setBackground(getSeriesFillPaint());
  
    }
    
    public void setSColorLabelColor(){
        scolorlabel.setBackground(getSeriesFillPaint());
    }
    
    private void setPossibleTimeIntervals(){
        int s = timechoice_START.getSelectedIndex();
        int e = timechoice_END.getSelectedIndex();
        
        if(s >= e){
            timechoice_END.setSelectedIndex(s);
        }    
    }
    
    public boolean isXSeries(){
        return is_x_series;
    }
    
    public boolean getResult(){
        return result;
    }
    
    public JPanel getGraphPanel(){
        return datapanel;
    }
    
    public void setIsXSeries(boolean xseries){
        is_x_series = xseries;          // Probleme abfangen?
    }
    
    public void setXSeries(int col){
        x_series_col = col;          // Probleme abfangen?
    }
    
    public int getXSeriesCol(){
        return this.x_series_col;
    }
    
    public void setXIntervals(int[] range){
        this.d_range = range;
//        if(!isXSeries()){
//        setDataSTART(data[d_range[0]].x);
//        setDataEND(data[d_range[1]].x);
//        }
    }
    
    public void setXChanged(boolean state){
        this.x_changed = state;
    }
    
//    public void setDataSelection(){
//        this.rowSelection = table.getSelectedRows();
//        this.selectedColumn = table.getSelectedColumn();
//    }
    
    public void setSelectedColumn(int col){
        this.selectedColumn = col;
        this.setColumn.setSelectedIndex(col);
        //setLegendName((String)setColumn.getSelectedItem());
        //setName((String)setColumn.getSelectedItem());
        
        
    }
    
    public void setSelectedRows(int[] rows){
        this.rowSelection = rows;
    }
    
//    public void setColor(String color){
////        this.color = color;
//        colorchoice.setSelectedItem(color);
//    }
    
//    public void setColor(int index){
//        
//        //colorchoice.setSelectedIndex(index);
//        this.color = index;
//    }
    
    public void setLegendName(String legendName){
        this.legendName = legendName;
        setLegend.setText(legendName);
        
    }
    
    public void setName(String name){
        this.name = name;
        setName.setText(name);
        //nameLabel.setText(name);
    }
    
    public void setPosition(String position){
        this.position = position;
        poschoice.setSelectedItem(position);
//        if(position.equals("left")) poschoice.setSelectedIndex(0);
//        if(position.equals("right")) poschoice.setSelectedIndex(1);
    }
    
    public void setRendererType(int type){
        this.type = type;
        //typechoice.setSelectedIndex(type);
    }
    
    public void setTimeSTART(int index){
        timechoice_START.setSelectedIndex(index);
    }
    
    public void setTimeEND(int index){
        timechoice_END.setSelectedIndex(index);
    }
    
    public void setDataSTART(double d_start){
        data_range_start = d_start;
        String s;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(4);
        s = nf.format(d_start);
        datachoice_START.setText(s);
    }
    
    public void setDataEND(double d_end){
        data_range_end = d_end;
        String s;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(4);
        s = nf.format(d_end);
        datachoice_END.setText(s);
    }
    
    public int getColor(){
        return this.color;
    }
    
    public String getLegendName(){
        return this.setLegend.getText();
    }
    
    public String getName(){
        if(this.selectedColumn != 0){
            name = table.getColumnName(selectedColumn);
        } 
//        else {
//            
//            name = this.name;
//        }
        
        return name;
    }
    
    public int getSelectedColumn(){
        return this.selectedColumn;
    }
    
    public int[] getSelectedRows(){
        return this.rowSelection;
    }
    
    public String getPosition(){
        return (String) getPosChoice().getSelectedItem();
    }
    
    public int getRendererType(){
        return this.type;
    }
    
    public int getTimeSTART(){
        return timechoice_START.getSelectedIndex();
    }

    public int getTimeEND(){
        return timechoice_END.getSelectedIndex();
//        return 5000;
    }
    
    public double readDataSTART(){
        String text = datachoice_START.getText();
        double d_start;
        d_start = new Double(text);
        return d_start;
    }
    
    public double getDataSTART(){
        
        return data_range_start;
    }
    
    public double getDataEND(){
        
        return data_range_end;
    }
    
    
    
    public double readDataEND(){
        double d_end = new Double(datachoice_END.getText());
        return d_end;
    }

    public boolean axisInverted(){
        return invBox.isSelected();
    }
    
    /** GUI return **/
    public JCheckBox getInvBox(){
        return invBox;
    }
    
    public JToggleButton getIsXAxisButton(){
        return isXAxis;
    }
    
    public JLabel getNameLabel(){
        return nameLabel;
    }
    
//    public JAMSItemLabel getItemLabel(){
//        return this.item_label;
//    }
  
    public JComboBox getPosChoice(){
        return poschoice;
    }
    
    public JComboBox getTypeChoice(){
        return typechoice;
    }
    
    public JComboBox getColorChoice(){
        return colorchoice;
    }
    
    public JTextField getLegendField(){
        return setLegend;
    }
    
    public JComboBox getDataChoice(){
        return setColumn;
    }
    
    public JComboBox getTimeChoiceSTART(){
        return timechoice_START;
    }
    
    public JComboBox getTimeChoiceEND(){
        return timechoice_END;
    }
    
    public JTextField getDataChoiceSTART(){
        return datachoice_START;
    }
    
    public JTextField getDataChoiceEND(){
        return datachoice_END;
    }
    
    public JButton getMaxButton(){
        return datachoice_max;
    }
    
    public JButton getAddButton(){
        return addButton;
    }
    
    public JButton getRemButton(){
        return remButton;
    }
    
    public JButton getUpButton(){
        return upButton;
    }
    
    public JButton getDownButton(){
        return downButton;
    }
    
    public JButton getPlotButton(){
        return this.plotButton;
    }
    
    public JButton getCustomizeButton(){
        return this.customizeButton;
    }
    
    public JLabel getColorLabel(){
        return this.colorlabel;
    }
    
    public JLabel getSColorLabel(){
        return this.scolorlabel;
    }
    
    //Methods for renderer configuration
    public void setSeriesStroke(Stroke stroke){
        this.series_stroke = stroke;
    }
       
    public void setSeriesShape(Shape shape){
        this.series_shape = shape;
    }
    
    public void setSeriesPaint(Color paint){
        this.series_paint = paint;
        cr_dlg.setStrokeButtonColor(paint);
    }
    
    public void setSeriesFillPaint(Color fill){
        this.series_fill_paint = fill;
        cr_dlg.setFillButtonColor(fill);
    }
    
    public void setSeriesOutlinePaint(Color out){
        this.series_outline_paint = out;
        cr_dlg.setOutlineButtonColor(out);
    }
    
    public void setSeriesOutlineStroke(Stroke stroke){
        this.series_outline_stroke = stroke;
    }
    
    public void setLinesVisible(boolean flag){
        this.linesVisible = flag;
    }
    
 
    
    public void setShapesVisible(boolean flag){
        this.shapesVisible = flag;
        //cr_dlg.shapes_vis_box.setSelected(flag);
    }
    
    
    public void setStroke(int type){
        stroke_type = type;
        float width;
        
        switch(type){
            case 0: width = 0.0f; break;
            case 1: width = 0.5f; break;
            case 2: width = 1.0f; break;
            case 3: width = 2.0f; break;
            case 4: width = 3.0f; break;
            case 5: width = 4.0f; break;
            case 6: width = 5.0f; break;
            case 7: width = 6.0f; break;
            case 8: width = 7.0f; break;
            case 9: width = 8.0f; break;
            case 10: width = 9.0f; break;
            case 11: width = 10.0f; break;
            case 12: width = 12.0f; break;
           
            default: width = 1.0f; break;         
        }
        setSeriesStroke(new BasicStroke(width));  
    }
    
    public void setOutlineStroke(int type){
        
        outline_type = type;
        float width;
        
        switch(type){
            case 0: width = 0.0f; break;
            case 1: width = 0.5f; break;
            case 2: width = 1.0f; break;
            case 3: width = 2.0f; break;
            case 4: width = 3.0f; break;
            case 5: width = 4.0f; break;
            case 6: width = 5.0f; break;
            case 7: width = 6.0f; break;
            case 8: width = 7.0f; break;
            case 9: width = 8.0f; break;
            case 10: width = 9.0f; break;
            case 11: width = 10.0f; break;
            case 12: width = 12.0f; break;
            default: width = 1.0f; break;         
        }
        setSeriesOutlineStroke(new BasicStroke(width));  
    }
    
    public void setOutlineVisible(boolean state){
        this.outlineVisible = state;
    }
    
    public boolean getOutlineVisible(){
        return this.outlineVisible;
    }
     
    
    
    public void setShape(int type, int size){
        
        shape_type = type;
        size_type = size;
        
        int dim =  size;
        int coord;
        coord = dim/2;
        
        switch(type){
            
            
            case 0: setSeriesShape(new java.awt.Rectangle(-coord,-coord,size,size));
                break;
                
            case 1: 
                java.awt.geom.Ellipse2D.Double circle = new java.awt.geom.Ellipse2D.Double(-coord,-coord,size,size);
                setSeriesShape(circle);
                break;
                
            case 2: 
                Shape up_triangle = org.jfree.util.ShapeUtilities.createUpTriangle(size);
                setSeriesShape(up_triangle);
                break;
                
            case 3: 
                Shape down_triangle = org.jfree.util.ShapeUtilities.createDownTriangle(size);
                setSeriesShape(down_triangle);
                break;
                
             case 4: 
                Shape diamond = org.jfree.util.ShapeUtilities.createDiamond(size);
                setSeriesShape(diamond);
                break;
                
             case 5: 
                float t = size*0.07f; 
                Shape diag_cross = org.jfree.util.ShapeUtilities.createDiagonalCross(size, t);
                setSeriesShape(diag_cross);
                break;
                
             case 6: 
                float d = size*0.07f; 
                Shape reg_cross = org.jfree.util.ShapeUtilities.createRegularCross(size, d);
                setSeriesShape(reg_cross);
                break;      
                //Shape triangle = org.jfree.util.ShapeUtilities.createUpTriangle(size);
               //setSeriesShape(org.jfree.util.ShapeUtilities.createUpTriangle(100f));
               
               // setSeriesShape(triangle);
//            case 2: setSeriesShape(new java.awt.Rectangle()); break;
//            case 3: setSeriesShape(new java.awt.Rectangle()); break;
//            case 4: setSeriesShape(new java.awt.Rectangle()); break;
            default: setSeriesShape(new java.awt.Rectangle()); break;         
        }
       
    }
    
    public int getStrokeType(){
        return stroke_type;
    }
    
    public int getShapeType(){
        return shape_type;
    }
    
    public int getSizeType(){
        return size_type;
    }
    
    public int getOutlineType(){        
        return outline_type;
    }    
    
    public Stroke getSeriesStroke(){
        return this.series_stroke;
    }
    
    public Shape getSeriesShape(){
        return this.series_shape;
    }
    
    public Color getSeriesPaint(){
        return this.series_paint;
    }
    
    public Color getSeriesFillPaint(){
        return this.series_fill_paint;
    }
    
    public Stroke getSeriesOutlineStroke(){
        return this.series_outline_stroke;
    }
    
    public Color getSeriesOutlinePaint(){
        return this.series_outline_paint;
    }
    
    public boolean getLinesVisible(){
        return this.linesVisible;
    }
    
    public boolean getShapesVisible(){
        return this.shapesVisible;
    }
    
    private void drawLegendItem(){
        JPanel pane = new JPanel();
        pane.setSize(20,20);
        
    }
    
    public void setOutlineSlider(int value){
            cr_dlg.setOutlineSlider(value);
        }
        
        public void setStrokeSlider(int value){
            cr_dlg.setStrokeSlider(value);
        }
        
        public void setShapeSlider(int value){
            cr_dlg.setShapeSlider(value);
        }
        
        public void setShapesVisBox(boolean state){
            cr_dlg.setShapesVisBox(state);
        }
        
        public void setLinesVisBox(boolean state){
            cr_dlg.setLinesVisBox(state);
        }
        
        public void setShapeBox(int index){
            cr_dlg.setShapeBox(index);
        }
    
    
    /*** Action Listener ***/
//    ActionListener okListener = new ActionListener(){
//        public void actionPerformed(ActionEvent te){
//
//            if(plotType == 0){
//                applyTSProperties();
//                ctsconf.plotGraph(thisProp);
//            }
//            if(plotType == 1){
//
//            }
//        }
//    };
    
    ActionListener timeListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            setPossibleTimeIntervals();
            //setVisible(false);
        }
    };
    
    ActionListener rendererListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            if(plotType == 0) ctsconf.handleRenderer();
            if(plotType == 1) cxyconf.handleRenderer();
            
            //cxyconf.handleRenderer();
            //setVisible(false);
        }
    };
    
    ActionListener addListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            
//            if(plotType == 0){
//                ctsconf.addGraph(index+1);
//            }
//            if(plotType == 1){
//                cxyconf.addGraph(index+1);
//                
//            }
            if(plotType == 0){
                ctsconf.addGraph(thisProp);
            }
            if(plotType == 1){
                cxyconf.addGraph(thisProp);
                
            }
            
            //setVisible(false);
        }
    };
    
    ActionListener removeListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            
            
            if(plotType == 0){
                ctsconf.removeGraph(thisProp);
            }
            if(plotType == 1){
                cxyconf.removeGraph(thisProp);
                
            }
                
            
            //setVisible(false);
        }
    };
    
    ActionListener upListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            
            if(plotType == 0){
                ctsconf.upGraph(thisProp);
                //applyTSProperties();
            }
            if(plotType == 1){
                cxyconf.upGraph(thisProp);
                
                //applyXYProperties();
            }
            
            
            
            //setVisible(false);
        }
    };
    
    ActionListener downListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            
            if(plotType == 0){
                ctsconf.downGraph(thisProp);
                //applyTSProperties();
            }
            if(plotType == 1){
                cxyconf.downGraph(thisProp);
                
                //applyXYProperties();
            }            
        }
    };
    
    ActionListener isXListener = new ActionListener(){
        public void actionPerformed(ActionEvent xe){
            
            
            cxyconf.xChanged(thisProp);
            cxyconf.setMaxDataIntervals(thisProp);
        }
    };
    
    ItemListener endListener = new ItemListener(){
        public void itemStateChanged(ItemEvent xe){
            
//            time_END = timechoice_END.getSelectedIndex();
        }
    };
    
    ActionListener max_listener = new ActionListener(){
        public void actionPerformed(ActionEvent me){
            
            cxyconf.setMaxDataIntervals(thisProp);
        }
    };
    
    ActionListener customize_listener = new ActionListener(){
        public void actionPerformed(ActionEvent me){
            
            //CustomizeRendererDlg cr_dlg = new CustomizeRendererDlg();
            
            
            if(getPosition() == "left"){
                if(plotType == 0) setRendererType(ctsconf.getRendererLeft());
                if(plotType == 1) setRendererType(cxyconf.getRendererLeft());
                
            }
            if(getPosition() == "right"){
                if(plotType == 0) setRendererType(ctsconf.getRendererRight());
                if(plotType == 1) setRendererType(cxyconf.getRendererRight());
            }
            cr_dlg.handleGUI();
            
          
            
            cr_dlg.setVisible(true);
        }
    };
    
    MouseAdapter dataSTARTListener = new MouseAdapter(){
        public void mouseClicked(){
            datachoice_START.selectAll();
        }
    };
    
    MouseAdapter dataENDListener = new MouseAdapter(){
        public void mouseClicked(){
            datachoice_END.selectAll();
        }
    };
    
    DocumentListener d_start_listener = new DocumentListener(){
        public void changedUpdate(DocumentEvent e){
            cxyconf.dStartChanged(true);
            
        }
        public void removeUpdate(DocumentEvent e){
            cxyconf.dStartChanged(true);
           
        }
        public void insertUpdate(DocumentEvent e){
            cxyconf.dStartChanged(true);
            
        }
    };
    
    DocumentListener d_end_listener = new DocumentListener(){
        public void changedUpdate(DocumentEvent e){
            cxyconf.dEndChanged(true);
            
        }
        public void removeUpdate(DocumentEvent e){
            cxyconf.dEndChanged(true);
            
        }
        public void insertUpdate(DocumentEvent e){
            cxyconf.dEndChanged(true);
            
        }
    };

    private class CustomizeRendererDlg extends JDialog{
 
        boolean result = false;
        int max;
        String side;
        int side_index;
        int position;
        
        JDialog thiscrd = this;
        
        JPanel optionspanel;
        JPanel colorpanel;
        JPanel buttonpanel;
        
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
        JButton cancel_button;
        
        final String[] SHAPES = {"Square", "Circle", "Triangle up", "Triangle down", "Diamond", "Cross diagonal", "Cross regular"};//, "Square", "Star"};
        final String[] COLORS = {"custom","red","blue","green","black","magenta","cyan","yellow","gray","orange","lightgray","pink"};
        final String[] SHAPE_COLORS = {"custom","white","red","blue","green","black","magenta","cyan","yellow","gray","orange","lightgray","pink"};
        final String[] RENDERER = {"Line and Shape","Bar","Area","Step","StepArea","Difference"};
        
        final int MIN = 0;
        final int MAX = 12;
        
        final int STROKE = 2;
        final int SHAPE = 5;
        final int OUTLINE = 1;

        public CustomizeRendererDlg(){
            super(parent, "Customize Series Paint", true);
            
            Point parentloc = parent.getLocation();
            setLocation(parentloc.x + 50, parentloc.y + 50);
            
            try{
                line_color = (Color) getSeriesPaint();
                shape_fill = (Color) getSeriesFillPaint();
                outline_color = (Color) getSeriesOutlinePaint();
            }catch(Exception cce){
                line_color = Color.RED;
                shape_fill = Color.RED;
                outline_color = Color.GRAY;
            }
            
            
            createPanel();
        }
        
        public void updateColors(){
            
            line_color = getSeriesPaint();
            shape_fill = getSeriesFillPaint();
            outline_color = getSeriesOutlinePaint();
            
            stroke_button.setBackground(line_color);
            fill_button.setBackground(shape_fill);
            outline_button.setBackground(outline_color);
            
        }
        
        public void setStrokeButtonColor(Color lc){
            stroke_button.setBackground(lc);
        }
        
        public void setFillButtonColor(Color fc){
            fill_button.setBackground(fc);
        }
        
        public void setOutlineButtonColor(Color oc){
            outline_button.setBackground(oc);
        }
        
        public void setOutlineSlider(int value){
            outline_slider.setValue(value);
        }
        
        public void setStrokeSlider(int value){
            stroke_slider.setValue(value);
        }
        
        public void setShapeSlider(int value){
            shape_slider.setValue(value);
        }
        
        public void setShapesVisBox(boolean state){
            shapes_vis_box.setSelected(state);
        }
        
        public void setLinesVisBox(boolean state){
            lines_vis_box.setSelected(state);
        }
        
        public void setShapeBox(int index){
            shape_box.setSelectedIndex(index);
        }
        
        
        
        
        
        
        void createPanel(){
            optionspanel = new JPanel();
            colorpanel = new JPanel();
            buttonpanel = new JPanel();
            GridBagLayout gbl = new GridBagLayout();
            BorderLayout brl = new BorderLayout();
            GridBagLayout option_gbl = new GridBagLayout();
            GridBagLayout button_gbl = new GridBagLayout();

//            setLayout(gbl);
            setLayout(brl);
            optionspanel.setLayout(option_gbl);
            
            ok_button = new JButton("Set Properties");
            cancel_button = new JButton("Cancel");
            
            ok_button.addActionListener(ok);
            cancel_button.addActionListener(cancel);
            
            apply_button = new JButton("apply");
            apply_button.addActionListener(apply);
            
//            renderer_label = new JLabel("Renderer Type:");
//            renderer_box = new JComboBox(RENDERER);
//            renderer_box.setSelectedIndex()
            stroke_label = new JLabel("Stroke:");
//            stroke_box = new JComboBox(STROKES);
//            stroke_box.setSelectedIndex(2);
            
            shape_label = new JLabel("Shape:");
            shape_box = new JComboBox(SHAPES);
            shape_box.setSelectedIndex(0);

            paint_label = new JLabel("Color:");
            paint_box = new JComboBox(COLORS);
            paint_box.setSelectedIndex(1);
            
            outline_stroke_label = new JLabel("Outline Stroke:");
//            outline_stroke_box = new JComboBox(STROKES);
//            outline_stroke_box.setSelectedIndex(2);
            
            outline_paint_label = new JLabel("Outline Color:");
            outline_paint_box = new JComboBox(SHAPE_COLORS);
            outline_paint_box.setSelectedIndex(3);
            
            fill_label = new JLabel("Color:");
            fill_box = new JComboBox(SHAPE_COLORS);
            fill_box.setSelectedIndex(2);
            
            shape_size_label = new JLabel("Size");
//            shape_size_box = new JComboBox(SIZES);
//            shape_size_box.setSelectedIndex(2);
            //shape_size_box.setEnabled(false);
            //paint_box.setSelectedIndex()
            lines_visible_label = new JLabel("Lines");
            lines_vis_box = new JCheckBox();
            lines_vis_box.setSelected(true);
            shapes_visible_label = new JLabel("Shapes");
            shapes_vis_box = new JCheckBox();
            shapes_vis_box.setSelected(true);
            
            stroke_slider = new JSlider(JSlider.HORIZONTAL, MIN, MAX, STROKE);
            shape_slider = new JSlider(JSlider.HORIZONTAL, MIN, MAX, SHAPE);
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
            
            stroke_button = new JButton("color");
            fill_button = new JButton("color");
            outline_button = new JButton("color");
            
            stroke_button.setBackground(line_color);
            fill_button.setBackground(shape_fill);
            outline_button.setBackground(outline_color);
            
            stroke_button.setSize(15,15);
            fill_button.setSize(15,15);
            outline_button.setSize(15,15);
            
            stroke_button.addActionListener(stroke_button_listener);
            fill_button.addActionListener(fill_button_listener);
            outline_button.addActionListener(outline_button_listener);
            //optionpanel
            LHelper.addGBComponent(optionspanel, option_gbl, new JLabel("Line"),    0, 0, 1, 1, 0, 0);
            LHelper.addGBComponent(optionspanel, option_gbl, new JLabel("Symbol"),    4, 0, 1, 1, 0, 0);

                //lines
            LHelper.addGBComponent(optionspanel, option_gbl, stroke_label, 0, 1, 1, 1, 0, 0);
            LHelper.addGBComponent(optionspanel, option_gbl, stroke_slider,   1, 1, 1, 2, 0, 0);
            LHelper.addGBComponent(optionspanel, option_gbl, paint_label,  0, 3, 1, 1, 0, 0);
            LHelper.addGBComponent(optionspanel, option_gbl, stroke_button,    1, 3, 1, 1, 0, 0);
            LHelper.addGBComponent(optionspanel, option_gbl, lines_visible_label, 0, 4, 1, 1, 0, 0);
            LHelper.addGBComponent(optionspanel, option_gbl, lines_vis_box,1, 4, 1, 1, 0, 0);
            LHelper.addGBComponent(optionspanel, option_gbl, shapes_visible_label, 0, 5, 1, 1, 0, 0);
            LHelper.addGBComponent(optionspanel, option_gbl, shapes_vis_box,1, 5, 1, 1, 0, 0);
                //divider
            LHelper.addGBComponent(optionspanel, option_gbl, divider,      2, 1, 1, 8, 1, 1);
                //shapes
            LHelper.addGBComponent(optionspanel, option_gbl, shape_label,  4, 1, 1, 1, 0, 0);
            LHelper.addGBComponent(optionspanel, option_gbl, shape_box,    5, 1, 1, 1, 0, 0);
            LHelper.addGBComponent(optionspanel, option_gbl, shape_size_label,4, 2, 1, 1, 0, 0);
            LHelper.addGBComponent(optionspanel, option_gbl, shape_slider,5, 2, 1, 2, 0, 0);
            LHelper.addGBComponent(optionspanel, option_gbl, fill_label,    4, 4, 1, 1, 0, 0);
            LHelper.addGBComponent(optionspanel, option_gbl, fill_button,    5, 4, 1, 1, 0, 0);          
            LHelper.addGBComponent(optionspanel, option_gbl, outline_stroke_label,4, 5, 1, 1, 0, 0);
            LHelper.addGBComponent(optionspanel, option_gbl, outline_slider,5, 5, 1, 2, 0, 0);
            LHelper.addGBComponent(optionspanel, option_gbl, outline_paint_label,4, 7, 1, 1, 0, 0);
            LHelper.addGBComponent(optionspanel, option_gbl, outline_button,5, 7, 1, 1, 0, 0);
                  
            //buttonpanel
            LHelper.addGBComponent(buttonpanel, button_gbl, ok_button, 0, 0, 1, 1, 1, 1);
            LHelper.addGBComponent(buttonpanel, button_gbl, cancel_button, 1, 0, 1, 1, 1, 1);
            //LHelper.addGBComponent(buttonpanel, button_gbl, apply_button, 2, 0, 1, 1, 1, 1);
            
            //this-panel
//            LHelper.addGBComponent(this, gbl, optionspanel, 0, 0, 1, 6, 1, 1);
//            LHelper.addGBComponent(this, gbl, colorpanel  , 1, 0, 1, 5, 1, 1);
//            LHelper.addGBComponent(this, gbl, buttonpanel , 1, 5, 1, 1, 1, 1);
            
            add(optionspanel, brl.CENTER);
            add(buttonpanel, brl.SOUTH);
            
            //default values
            setStroke(stroke_slider.getValue());
            setShape(shape_box.getSelectedIndex(), shape_slider.getValue());
            //setSeriesPaint(line_color);
//            if(outline_paint_box.getSelectedIndex()==0){
//                setOutlineVisible(false);
//            }else{
//                setOutlineVisible(true);
//                setSeriesOutlinePaint(colorTable.get((String)outline_paint_box.getSelectedItem()));
//            }
            
            //setSeriesOutlinePaint(outline_color);
            //setSeriesFillPaint(shape_fill);
            setOutlineStroke(outline_slider.getValue());
            setLinesVisible(lines_vis_box.isSelected());
            setShapesVisible(shapes_vis_box.isSelected());
            
            handleGUI();
            
            pack();
            setVisible(false);
        }
        
        
        private void handleGUI(){
            if(getRendererType() == 0){ //line and shape
                 
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
            if(getRendererType() == 2){ //area
                
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
            if(getRendererType() == 5){ //difference
                
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
            
            if(getRendererType() == 1 || getRendererType() == 3 || getRendererType() == 4){ //bars and steps
                
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
        
        

        ActionListener ok = new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                
                setStroke(stroke_slider.getValue());
                setShape(shape_box.getSelectedIndex(), shape_slider.getValue());
                //setSeriesPaint(colorTable.get((String)paint_box.getSelectedItem()));
                //setSeriesPaint(series_paint);
                //setSeriesOutlinePaint(series_outline_paint);
                //setSeriesFillPaint(colorTable.get((String)fill_box.getSelectedItem()));
                //setSeriesFillPaint(series_fill_paint);
                setSeriesPaint(line_color);
                setSeriesFillPaint(shape_fill);
                setSeriesOutlinePaint(outline_color);
                setOutlineStroke(outline_slider.getValue());
                setLinesVisible(lines_vis_box.isSelected());
                setShapesVisible(shapes_vis_box.isSelected());
                result = true;
                
                
                
                
                //ACHTUNG!!! Typen-Abh�ngig! XY oder TS?
//                if(plotType == 0) ctsconf.plotAllGraphs();
//                if(plotType == 1) cxyconf.plotAllGraphs();
 
                setVisible(false);
                
                updateColors();
                setColorLabelColor();
                setSColorLabelColor();
            }
        };
        
        ActionListener apply = new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                setStroke(stroke_slider.getValue());
                setShape(shape_box.getSelectedIndex(), shape_slider.getValue());
                //setSeriesPaint(colorTable.get((String)paint_box.getSelectedItem()));
                //setSeriesPaint(line_color);
                //setSeriesOutlinePaint(outline_color);
                //setSeriesFillPaint(colorTable.get((String)fill_box.getSelectedItem()));
                //setSeriesFillPaint(shape_fill);
                setSeriesPaint(line_color);
                setSeriesFillPaint(shape_fill);
                setSeriesOutlinePaint(outline_color);
                
                setOutlineStroke(outline_slider.getValue());
                setLinesVisible(lines_vis_box.isSelected());
                setShapesVisible(shapes_vis_box.isSelected());
                result = true;
                
                
                
                //ACHTUNG!!! Typen-Abhängig! XY oder TS?
//                if(plotType == 0) ctsconf.plotAllGraphs();  
//                if(plotType == 1) cxyconf.plotAllGraphs();
                
                updateColors();
                
            }
        };
        
        ActionListener stroke_button_listener = new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                Color new_line_color = JColorChooser.showDialog(thiscrd, "Choose Line Color", line_color);
                if(new_line_color != null){
                    line_color = new_line_color;
                }
                
 
                stroke_button.setBackground(line_color);
            }
        };
        
        ActionListener fill_button_listener = new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                Color new_shape_fill = JColorChooser.showDialog(thiscrd, "Choose Shape Color", shape_fill);
                if(new_shape_fill != null){
                    shape_fill = new_shape_fill;
                }
                fill_button.setBackground(shape_fill);
            }
        };
        
        ActionListener outline_button_listener = new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                Color new_outline_color = JColorChooser.showDialog(thiscrd, "Choose Outline Color", outline_color);
                if(new_outline_color != null){
                    outline_color = new_outline_color;
                }
                
                outline_button.setBackground(outline_color);
                
            }
        };
        
        ActionListener cancel = new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                   setVisible(false);            
            }
        };
        
        
        

    }
    
    public class ColorButton extends JButton{
        
        Color color;
        
        public ColorButton(Color color){
            this.color = color;
        }
        
        public void paint(Graphics g){
            
            g.setColor(color);
            g.fillRect(1,1,14,14);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(0,0,15,15);
        }
        
        public void setColor(Color newColor){
            this.color = newColor;
        }
    }
    
    public class ColorLabel extends JLabel{
        
        Color shape_fill;
        Color outline_color;
        Color line_color;
        Shape shape;
        
        public ColorLabel(Color shape_fill, Color outline_color, Shape shape){
            this.shape_fill = shape_fill;
            this.outline_color = outline_color;
            this.shape = shape;
        }
        
        public void paint(Graphics2D g){
            
            g.setColor(shape_fill);
            g.fill(shape);
            g.setColor(outline_color);
            g.draw(shape);
        }
        
        public void paint(Graphics g){
            g.setColor(Color.BLACK);
            g.fillRect(0,0,15,15);
        }
        
        public void setSymbol(Shape shape, Color shape_fill, Color outline_color){
            this.shape_fill = shape_fill;
            this.outline_color = outline_color;
            this.shape = shape;
        }
        
        
    }
    
    private class ItemPanel extends JPanel{
        
        Shape shape; 
        Color line_color;
        Color outline_color;
        Color shape_fill;
        
        public ItemPanel(){
            shape = getSeriesShape();
            line_color = (Color) getSeriesPaint();
            outline_color = (Color) getSeriesOutlinePaint();
            shape_fill = (Color) getSeriesFillPaint();
        }
        
        public void paintComponent(Graphics2D g){
 
            g.setColor(shape_fill);
            g.fill(shape);
            g.setColor(outline_color);
            g.draw(shape);
            
            g.setColor(Color.BLACK);
            g.fillRect(0,0,5,5);
        }
    }
}
    
   
    
 

