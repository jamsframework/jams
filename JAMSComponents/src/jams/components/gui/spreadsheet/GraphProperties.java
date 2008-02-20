/*
 * GraphProperties.java
 *
 * Created on 29. September 2007, 17:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.gui.spreadsheet;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.axis.Timeline;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.*;
import org.unijena.jams.data.JAMSBoolean;
import org.unijena.jams.data.JAMSCalendar;
import org.unijena.jams.data.JAMSDouble;
import org.unijena.jams.data.JAMSInteger;
import org.unijena.jams.data.JAMSString;
import org.unijena.jams.data.JAMSStringArray;
import org.unijena.jams.model.JAMSGUIComponent;
import org.unijena.jams.model.JAMSVarDescription;


public class GraphProperties {
//    
//    private static ImageIcon UP_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("jams/components/gui/resources/arrowup.png")).getImage().getScaledInstance(9, 5, Image.SCALE_SMOOTH));
//    private static ImageIcon DOWN_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("jams/components/gui/resources/arrowdown.png")).getImage().getScaledInstance(9, 5, Image.SCALE_SMOOTH));
    
    URL url1 = this.getClass().getResource("/jams/components/gui/resources/arrowup.png");
    ImageIcon up_icon = new ImageIcon(url1);
    
    URL url2 = this.getClass().getResource("/jams/components/gui/resources/arrowdown.png");
    ImageIcon down_icon = new ImageIcon(url2);
    
    URL url3 = this.getClass().getResource("/jams/components/gui/resources/correct.png");
    ImageIcon plot_icon = new ImageIcon(url3);
    
    URL url4 = this.getClass().getResource("/jams/components/gui/resources/add.png");
    ImageIcon add_icon = new ImageIcon(url4);
    
    URL url5 = this.getClass().getResource("/jams/components/gui/resources/remove.png");
    ImageIcon rem_icon = new ImageIcon(url5);
    //ImageIcon(getModel().getRuntime().getClassLoader().getResource("jams/components/gui/resources/root.png
    
    JTable table;
    
    TimeSeries ts;
    //TimeSeriesCollection dataset;
    
    XYSeries xys;
    
    
    JScrollPane scpane;
    
    int index = 0;
    String legendName;
    String color;
    String name;
    String position; // left/right
    int type; //renderer index
    
    boolean is_x_series = false;
    boolean result = false;
    
    int selectedColumn;
    int[] rowSelection;
    int x_series_col;
    
    JComboBox setColumn;
    JComboBox colorchoice;
    JComboBox typechoice;
    JComboBox poschoice;
    
    JComboBox timechoice_START;
    JComboBox timechoice_END;
    
    JComboBox datachoice_START;
    JComboBox datachoice_END;
    
    JButton addButton;
    JButton remButton;
    JButton plotButton;
    JButton upButton;
    JButton downButton;
    
    JCheckBox invBox;
    JRadioButton isXAxis;

    JLabel nameLabel;
    
    JTextField setName;
    JTextField setLegend;
    
    JTSConfigurator ctsconf;
    JXYConfigurator cxyconf;
    
    String[] x_dataIntervals;
    
    private String[] colors = {"red","blue","green","black","magenta","cyan","yellow","gray","orange","lightgray","pink"};
    private String[] types = {"Line","Bar","Area","Line and Base","Dot","Difference","Step","StepArea"};
    private String[] positions = {"left","right"};
    
    JPanel graphpanel = new JPanel();
    JPanel datapanel = new JPanel();
    JPanel buttonpanel = new JPanel();
    
    
    
    
    
    /** Creates a new instance of GraphProperties */
    public GraphProperties(JDialog parent, JTable table, JTSConfigurator ctsconf) {
        
        //super(parent, "Select Properties");
        //this.parent = parent;
        //setLayout(new FlowLayout());
        //Point parentloc = parent.getLocation();
        this.ctsconf = ctsconf;
        //setLocation(parentloc.x + 30, parentloc.y + 30);
        
        this.table = table;
        this.color = "red";
        this.position = "left";
        this.name = "Graph Name";
        this.legendName = this.name;
        
        this.selectedColumn = 0;
        this.rowSelection = null;
    
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
        
        
        createPanel();
        applyTSProperties();
        
    }
    
    public GraphProperties(JDialog parent, JTable table, JXYConfigurator cxyconf) {
        
        //super(parent, "Select Properties");
        //this.parent = parent;
        //setLayout(new FlowLayout());
        //Point parentloc = parent.getLocation();
        this.cxyconf = cxyconf;
        //setLocation(parentloc.x + 30, parentloc.y + 30);
        
        this.table = table;
        this.color = "red";
        this.position = "left";
        this.name = "Graph Name";
        this.legendName = this.name;
        
        //this.selectedColumn = 0;
        this.rowSelection = null;
    
        x_dataIntervals = new String[table.getRowCount()];
        
        x_series_col = cxyconf.columns[0];
        for(int i=0; i<table.getRowCount(); i++){
            x_dataIntervals[i] = table.getValueAt(i, x_series_col).toString();
        }
        
        datachoice_START = new JComboBox(x_dataIntervals);
        datachoice_START.setPreferredSize(new Dimension(40,14));
        //datachoice_START.addActionListener(dataListener);
        
        datachoice_END = new JComboBox(x_dataIntervals);
        datachoice_END.setPreferredSize(new Dimension(40,14));
        //datachoice_END.addActionListener(dataListener);
        
        createPanel();
        
        
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
        isXAxis = new JRadioButton();
        
        addButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addButton.setPreferredSize(new Dimension(20,14));
        
        addButton.addActionListener(addListener);
        remButton.addActionListener(removeListener);
        upButton.addActionListener(upListener);
        downButton.addActionListener(downListener);
        
        remButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        remButton.setPreferredSize(new Dimension(20,14));
        upButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        upButton.setPreferredSize(new Dimension(20,14));
        plotButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        plotButton.setPreferredSize(new Dimension(20,14));
        downButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
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
        //poschoice.addActionListener(okListener);
       
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("CANCEL");
        //JButton propButton = new JButton("...");

        //JLabel namelabel = new JLabel();
        JLabel setNameLabel =   new JLabel("        Name:");
        JLabel setColumnLabel = new JLabel("  Set Column:");
        JLabel setLegendLabel = new JLabel("Legend Entry:");
        nameLabel = new JLabel();

//        JTextField setName = new JTextField("Plot Name", 14);
//        JTextField setLegend = new JTextField("Legend Entry", 14);

        String[] column = new String[table.getColumnCount()];
        for(int i=0;i<table.getColumnCount();i++){      
            column[i] = table.getColumnName(i);
        }
        
        
        
       
        

        
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
        
       // this.graphpanel.add(propButton);
        
        this.datapanel.add(nameLabel);
//        this.datapanel.add(setColumn);
//        this.datapanel.add(poschoice);
//        this.datapanel.add(typechoice);
//        this.datapanel.add(colorchoice);
        //this.datapanel.add(propButton);
        //this.datapanel.add(nameLabel);
        
        this.buttonpanel.add(okButton);
        this.buttonpanel.add(cancelButton);
        
        plotButton.addActionListener(okListener);
    }
    
    /*    
    public void setRowSelection(int[] rows){
        this.rowSelection = rowSelection;
    }
     **/
    
    public void applyTSProperties(){
        JAMSCalendar time;
        double value;
        selectedColumn = setColumn.getSelectedIndex();
        color = (String) colorchoice.getSelectedItem();
        ts = new TimeSeries(setLegend.getText(), Second.class);
        //dataset = new TimeSeriesCollection(ts);
        
        for(int i=getTimeSTART(); i<=getTimeEND(); i++){
            
            time =  (JAMSCalendar) table.getValueAt(i,0); //ONLY FOR TIME SERIES TABLE WITH TIME IN COL 0!!!
            value = (Double) table.getValueAt(i, selectedColumn);
            ts.add(new Second(new Date(time.getTimeInMillis())), value);
        }
    }
    
    public void applyXYProperties(){
        double x_value;
        double value;
        selectedColumn = setColumn.getSelectedIndex();
        color = (String) colorchoice.getSelectedItem();
        xys = new XYSeries(setLegend.getText());
        //dataset = new TimeSeriesCollection(ts);
        
        for(int i=getDataSTART(); i<=getDataEND(); i++){
            
            x_value = (Double) table.getValueAt(i, x_series_col); //ONLY FOR TIME SERIES TABLE WITH TIME IN COL 0!!!
            value = (Double) table.getValueAt(i, selectedColumn);
            xys.add(x_value, value);
        }
    }
    
//    public TimeSeriesCollection getDataset(){
//        return dataset;
//    }
    
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
    
    private void setPossibleDataIntervals(){
        int s = datachoice_START.getSelectedIndex();
        int e = datachoice_END.getSelectedIndex();
        
        if(s >= e){
            datachoice_END.setSelectedIndex(s);
        }    
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
    
    public void setDataSelection(){
        this.rowSelection = table.getSelectedRows();
        this.selectedColumn = table.getSelectedColumn();
    }
    
    public void setSelectedColumn(int col){
        this.selectedColumn = col;
        setColumn.setSelectedIndex(col);
        nameLabel.setText((String)setColumn.getSelectedItem());
    }
    
    public void setSelectedRows(int[] rows){
        this.rowSelection = rows;
    }
    
    public void setColor(String color){
        this.color = color;
        colorchoice.setSelectedItem(color);
    }
    
    public void setColor(int index){
        
        colorchoice.setSelectedIndex(index);
        this.color = (String) colorchoice.getSelectedItem();
    }
    
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
    }
    
    public void setRendererType(int type){
        this.type = type;
        typechoice.setSelectedIndex(type);
    }
    
    public void setTimeSTART(int index){
        timechoice_START.setSelectedIndex(index);
    }
    
    public void setTimeEND(int index){
        timechoice_END.setSelectedIndex(index);
    }
    
    public void setDataSTART(int index){
        datachoice_START.setSelectedIndex(index);
    }
    
    public void setDataEND(int index){
        datachoice_END.setSelectedIndex(index);
    }
    
    public String getColor(){
        return this.color;
    }
    
    public String getLegendName(){
        return this.setLegend.getText();
    }
    
    public String getName(){
        if(this.selectedColumn != 0){
            name = table.getColumnName(selectedColumn);
        } else {
            name = this.name;
        }
        
        return name;
    }
    
    public int getSelectedColumn(){
        return this.selectedColumn;
    }
    
    public int[] getSelectedRows(){
        return this.rowSelection;
    }
    
    public String getPosition(){
        return this.position;
    }
    
    public int getRendererType(){
        return this.type;
    }
    
    public int getTimeSTART(){
        return timechoice_START.getSelectedIndex();
    }

    public int getTimeEND(){
        return timechoice_END.getSelectedIndex();
    }
    
    public int getDataSTART(){
        return datachoice_START.getSelectedIndex();
    }

    public int getDataEND(){
        return datachoice_END.getSelectedIndex();
    }

    public boolean axisInverted(){
        return invBox.isSelected();
    }
    
    /** GUI return **/
    public JCheckBox getInvBox(){
        return invBox;
    }
    
    public JRadioButton getIsXAxisButton(){
        return isXAxis;
    }
    
    public JLabel getNameLabel(){
        return nameLabel;
    }
  
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
    
    public JComboBox getDataChoiceSTART(){
        return datachoice_START;
    }
    
    public JComboBox getDataChoiceEND(){
        return datachoice_END;
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
    
   
    /*** Action Listener ***/
    ActionListener okListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            applyTSProperties();
            ctsconf.plotGraph(index);
        }
    };
    
    ActionListener timeListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            setPossibleTimeIntervals();
            //setVisible(false);
        }
    };
    
    ActionListener addListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            ctsconf.addGraph(index+1);
            //setVisible(false);
        }
    };
    
    ActionListener removeListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            
                ctsconf.removeGraph(index);
            
            //setVisible(false);
        }
    };
    
    ActionListener upListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            ctsconf.upGraph(index);
            applyTSProperties();
            
            //setVisible(false);
        }
    };
    
    ActionListener downListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            ctsconf.downGraph(index);
            applyTSProperties();
            
            //setVisible(false);
        }
    };
    
    
   
    
 
}
