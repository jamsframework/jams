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
import java.lang.Math.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Comparator;
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
    
    GraphProperties thisProp;
    
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
    int[] d_range = new int[2];
    
    boolean is_x_series = false;
    boolean result = false;
    boolean x_changed;
    
    int selectedColumn;
    int[] rowSelection;
    int x_series_col;
    
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
    
    XYPair[] data;
    double[] x_dataIntervals;
    double[] y_data;
    
    private String[] colors = {"red","blue","green","black","magenta","cyan","yellow","gray","orange","lightgray","pink"};
    private String[] types = {"Line","Bar","Area","Line and Base","Dot","Step","StepArea","Difference"};
    private String[] positions = {"left","right"}; 
    
    JPanel graphpanel = new JPanel();
    JPanel datapanel = new JPanel();
    JPanel buttonpanel = new JPanel();
    
    
    
    
    
    /** Creates a new instance of GraphProperties */
    public GraphProperties(JDialog parent, JTable table, JTSConfigurator ctsconf) {
        
        this.plotType = 0;
        //super(parent, "Select Properties");
        //this.parent = parent;
        //setLayout(new FlowLayout());
        //Point parentloc = parent.getLocation();
        this.ctsconf = ctsconf;
        this.thisProp = this;
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
        
        this.plotType = 1;

        this.cxyconf = cxyconf;
        this.thisProp = this;
        
        this.table = table;
        this.color = "red";
        this.position = "left";
        this.name = "Graph Name";
        this.legendName = this.name;
        
        this.rowSelection = null;
        
        data = new XYPair[table.getRowCount()];

        rowSelection = table.getSelectedRows();
        x_series_col = table.getSelectedColumn();

        datachoice_START = new JTextField();
        datachoice_START.setPreferredSize(new Dimension(40,14));
        datachoice_START.addMouseListener(dataSTARTListener);

        datachoice_END = new JTextField();
        datachoice_END.setPreferredSize(new Dimension(40,14));
        datachoice_END.addMouseListener(dataENDListener);
        
        
        
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
        isXAxis.addActionListener(isXListener);
        
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
         
        this.datapanel.add(nameLabel);

        this.buttonpanel.add(okButton);
        this.buttonpanel.add(cancelButton);
        
        plotButton.addActionListener(okListener);
    }
    
    public void applyTSProperties(){
        JAMSCalendar time;
        double value;
        selectedColumn = setColumn.getSelectedIndex();
        color = (String) colorchoice.getSelectedItem();
        ts = new TimeSeries(setLegend.getText(), Second.class);
        
        for(int i=getTimeSTART(); i<=getTimeEND(); i++){
            
            time =  (JAMSCalendar) table.getValueAt(i,0); //ONLY FOR TIME SERIES TABLE WITH TIME IN COL 0!!!
            value = (Double) table.getValueAt(i, selectedColumn);
            ts.add(new Second(new Date(time.getTimeInMillis())), value);
        }
    }
    
    public void applyXYProperties(){
        
//        if(selectedColumn != setColumn.getSelectedIndex()){
//            //selectedColumn = setColumn.getSelectedIndex();
//            writeXYPairs(); 
//        }

        color = (String) colorchoice.getSelectedItem();
        xys = new XYSeries(setLegend.getText());
       
        //sort xy data
        
        //check data intervals
        //int[] d_range = setPossibleDataIntervals();
        //if(!isXSeries()){
            //cxyconf.setXIntervals();
            //write xy series
            for(int i=this.d_range[0]; i<=this.d_range[1]; i++){
                xys.add(data[i].x, data[i].y);
                System.out.println("Row "+i+": ("+data[i].x+","+data[i].y+") ");
            }
      //}
    }
    
    public void writeXYPairs(){
        selectedColumn = setColumn.getSelectedIndex();
        for(int i=0; i<table.getRowCount(); i++){
            
              data[i] = new XYPair((Double) table.getValueAt(i, x_series_col),
                                    (Double) table.getValueAt(i, selectedColumn));
        }
        java.util.Arrays.sort(data);
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
 
    public int[] setInitialDataIntervals(){
        
//        double row_start = (Double) table.getValueAt(rowSelection[0], x_series_col);
//        double row_end = (Double) table.getValueAt(rowSelection[rowSelection.length - 1], x_series_col);
        int[] range = new int[2];
        
        double row_start = data[0].x;
        double row_end = data[data.length -1].x;
        range[0] = 0;
        range[1] = data.length - 1;
        
        double start, end;
        
//        for(int i=0; i<rowSelection.length; i++){
//            start = (Double) table.getValueAt(rowSelection[i], x_series_col);
//            end = (Double) table.getValueAt(rowSelection[i], x_series_col);
//            if(start < row_start) row_start = start;
//            if(end > row_end) row_end = end;
//        }
        
        for(int i=0; i<data.length; i++){
            start = data[i].x;
            end = data[i].x;
            if(start < row_start){ 
                
                range[0] = i;
                row_start = start;
                    
            }
            if(end > row_end){
                range[1] = i;
                row_end = end;
            }
        }
        setDataSTART(row_start);
        setDataEND(row_end);
        
        return range;
        
        
    }
    
//    public int[] setPossibleDataIntervals(){
//        double possible_start, possible_end;
//        
//        //int[] d_range = new int[2];  //end index
//        
//        double start = readDataSTART();
//        double end = readDataEND();
//        
//        possible_start = readDataSTART();
//        possible_end = readDataEND();
//        
//        double start_diff, end_diff, start_diff_min, end_diff_min; 
//        start_diff_min = Math.abs(data[0].x - start);
//        end_diff_min = Math.abs(data[0].x - end);
//        //start = data[0].x;
//        //end = data[data.length -1].x;
//        
//        
//        for(int i=0; i<data.length; i++){
//            
//            start_diff = Math.abs(data[i].x - start);
//            end_diff = Math.abs(data[i].x - end);
//            
//            if(start_diff < start_diff_min){
//                start_diff_min = start_diff;
//                possible_start = data[i].x;
//                d_range[0] = i;
//            }
//            if(end_diff < end_diff_min){
//                end_diff_min = end_diff;
//                possible_end = data[i].x;
//                d_range[1] = i;
//            }
//        }
//
//        setDataSTART(possible_start);
//        setDataEND(possible_end);
////        if(possible_start >= possible_end){
////            datachoice_END.setText(""+possible_start);
////        }
//        return d_range;
//    }
    
     public int[] setPossibleDataIntervals(){
        double possible_start, possible_end;
        
        int[] range = new int[2];
        
        double start = readDataSTART();
        double end = readDataEND();

        int i=0;
        boolean out_of_boundaries = (start < data[0].x) || (start > data[data.length -1].x);
        
        if(end < start) end = start;
        
        if(!out_of_boundaries){
            
            while(!(start >= data[i].x && start <= data[i+1].x)){
                i++;
            }
            start = data[i].x;
            range[0] = i;
        }else{
            if(start < data[0].x){
                start = data[0].x;
                range[0] = 0;
            }
            if(start > data[0].x){
                start = data[data.length -1].x;
                range[0] = data.length -1;
            }
        }

        setDataSTART(start);
        
        
        out_of_boundaries = (end < data[0].x) || (end > data[data.length -1].x);
        if(!out_of_boundaries){
            
            while(!(end >= data[i].x && end <= data[i+1].x)){
                i++;
            }
            end = data[i].x;
            range[1] = i;
            
        }else{
            if(end < data[0].x){
                end = data[0].x;
                range[1] = 0;
            }
            if(end > data[0].x){
                end = data[data.length -1].x;
                range[1] = data.length -1;
            }
        }
        setDataEND(end);
//        if(possible_end >= possible_end){
//            datachoice_END.setText(""+possible_end);
//        }
        d_range = range;
        return d_range;
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
        if(!isXSeries()){
        setDataSTART(data[d_range[0]].x);
        setDataEND(data[d_range[1]].x);
        }
    }
    
    public void setXChanged(boolean state){
        this.x_changed = state;
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
    
    public void setDataSTART(double d_start){
        data_range_start = d_start;
        d_start=Math.round(d_start*1000.)/1000.;
        datachoice_START.setText(""+d_start);
    }
    
    public void setDataEND(double d_end){
        data_range_end = d_end;
        d_end=Math.round(d_end*1000.)/1000.;
        datachoice_END.setText(""+d_end);
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
    
    public JTextField getDataChoiceSTART(){
        return datachoice_START;
    }
    
    public JTextField getDataChoiceEND(){
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

            if(plotType == 0){
                applyTSProperties();
                ctsconf.plotGraph(thisProp);
            }
            if(plotType == 1){
                //writeXYPairs();
//                if(x_changed){
//                    writeXYPairs();
//                    x_changed=false;
//                }
                cxyconf.setXIntervals();
                applyXYProperties();
                cxyconf.plotGraph(thisProp);
            }
        }
    };
    
    ActionListener timeListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            setPossibleTimeIntervals();
            //setVisible(false);
        }
    };
    
    ActionListener rendererListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            ctsconf.handleRenderer();
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
                applyTSProperties();
            }
            if(plotType == 1){
                cxyconf.upGraph(thisProp);
                
                applyXYProperties();
            }
            
            
            
            //setVisible(false);
        }
    };
    
    ActionListener downListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            
            if(plotType == 0){
                ctsconf.downGraph(thisProp);
                applyTSProperties();
            }
            if(plotType == 1){
                cxyconf.downGraph(thisProp);
                
                applyXYProperties();
            }
            
            
            
            //setVisible(false);
        }
    };
    
    ActionListener isXListener = new ActionListener(){
        public void actionPerformed(ActionEvent xe){
            
            cxyconf.xChanged(thisProp);
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
    
    
    
//    KeyAdapter intervalENDListener = new KeyAdapter(){
//        public void keyPressed(KeyEvent pe){
//            
//        }
//        public void keyReleased(KeyEvent pe){
//            
//        }
//        public void keyTyped(KeyEvent pe){
//            if(isXAxis.isEnabled()) cxyconf.setENDIntervals(getDataEND());
//        }
//        
//    };
//    
//    KeyAdapter intervalSTARTListener = new KeyAdapter(){
//        public void keyPressed(KeyEvent pe){
//            
//        }
//        public void keyReleased(KeyEvent pe){
//            
//        }
//        public void keyTyped(KeyEvent pe){
//            
//            if(isXAxis.isEnabled()) cxyconf.setSTARTIntervals(getDataSTART());
//        }
//        
//    };
    
    
    
}
  
    class XYPair implements Comparable<XYPair>{
        
        double x, y;
        public XYPair(double x, double y){
            this.x = x;
            this.y = y;
        }
        
        public int compareTo(XYPair arg){
            
            if(x < arg.x) return -1;
            if(x > arg.x) return 1;
            return 0;

        }
    }
    
   
    
 

