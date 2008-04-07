/*
 * CTSConfigurator.java
 *
 * Created on 2. September 2007, 00:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.gui.spreadsheet;

import java.net.URL;
import jams.components.gui.*;
import java.awt.Color;
import java.util.HashMap;
import java.util.Vector;
import java.awt.event.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.BorderFactory.*;
import javax.swing.border.*;
import javax.swing.GroupLayout.*;

import java.io.*;

import java.util.Iterator;

import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
/**
 *
 * @author Robert Riedel
 */
public class JXYConfigurator extends JFrame{

        GroupLayout gLayout;
        
        GroupLayout.SequentialGroup hGroup;
        GroupLayout.SequentialGroup vGroup;
        Group group1;
        Group group2;
        Group group3;
        Group group4;
        Group group5;
        Group group6;
        Group group7;
        Group group8;
        Group group9;
        Group group10;
        Group group11;
        Group group12;
        Group group13;
        Group group14;
        Group group15;
    
    //private Vector<ActionListener> addAction = new Vector<ActionListener>();    
        
    private JFrame parent;
    private JFrame thisDlg;
    private JPanel frame;
    private JPanel mainpanel;
    private JPanel plotpanel;
    private JPanel optionpanel;
    private JPanel graphpanel;
    private JPanel southpanel;
    //private Vector<JPanel> datapanels = new Vector<JPanel>();
    private JPanel edTitlePanel;
    private JPanel edLeftAxisPanel;
    private JPanel edRightAxisPanel;
    private JPanel edTimeAxisPanel;
    
    private JSplitPane split_hor = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private JSplitPane split_vert = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    
    private JPanel[] datapanels;
    private JScrollPane graphScPane;
    private JScrollPane plotScPane;
    private JScrollPane mainScPane;
    private JScrollPane optScPane;
    
    private String[] headers;
    
    private JLabel edTitle = new JLabel("Plot Title: ");
    private JLabel edLeft  = new JLabel("Left axis title: ");
    private JLabel edXAxis = new JLabel("X axis title");
    
    private JLabel edRight = new JLabel("Right axis title: ");
    private JLabel rLeftLabel = new JLabel("Renderer left");
    private JLabel rRightLabel= new JLabel("Renderer right");
    private JLabel invLeftLabel = new JLabel("Invert left axis");
    private JLabel invRightLabel = new JLabel("Invert right axis");
    
    private JTextField edTitleField = new JTextField(14);
    private JTextField edLeftField = new JTextField(14);
    private JTextField edRightField = new JTextField(14);
    private JTextField edXAxisField = new JTextField(14);
    
    private String[] types = {"Line","Bar","Area","Line and Base","Dot","Step","StepArea","Difference"};
    
    private JComboBox rLeftBox = new JComboBox(types);
    private JComboBox rRightBox = new JComboBox(types);
    
    private JCheckBox invLeftBox = new JCheckBox("Invert left Axis");
    private JCheckBox invRightBox = new JCheckBox("Invert right Axis");
    
    private ButtonGroup isXAxisGroup = new ButtonGroup();
    
    private JButton applyButton = new JButton("Apply");
    private JButton addButton = new JButton("Add Graph");
    
    private Vector<GraphProperties> propVector = new Vector<GraphProperties>();
    private JAMSXYPlot jxys = new JAMSXYPlot();
    
    public XYRow[] sorted_Row;
    
//    private String[] headers;
//    //private String[] colors = {"yellow","orange","red","pink","magenta","cyan","blue","green","gray","lightgray","black"};
    //private String[] colors = {"red","blue","green","black","magenta","cyan","yellow","gray","orange","lightgray","pink"};
//    private String[] types = {"Line","Bar","Area","Line and Base","Dot","Difference","Step","StepArea"};
//    private String[] positions = {"left","right"};
    
    /* test*/
//    private Color[] colors_ = {Color.RED, Color.BLUE};
    
//    private String[] legendEntries;
    
    private int x_series_index;
    private int colour_cnt;
    
    double row_start;
    double row_end;
    
    HashMap<String, Color> colorTable = new HashMap<String, Color>();
    
    int[] rows, columns;
    JTable table;
    
    CTSPlot ctsplot;
    
    /*buttons*/

    
    int graphCount=0;
//    Vector<JCheckBox> activate = new Vector<JCheckBox>();
//    Vector<JComboBox> datachoice = new Vector<JComboBox>();
//    Vector<JComboBox> poschoice = new Vector<JComboBox>();
//    Vector<JComboBox> typechoice = new Vector<JComboBox>();
//    Vector<JComboBox> colorchoice = new Vector<JComboBox>();
    
    JCheckBox[] activate;
    JComboBox[] datachoice;
    JComboBox[] poschoice;
    JComboBox[] typechoice;
    JComboBox[] colorchoice;
    
    
    /* ActionListener */
    ActionListener[] activationChange;
    
    /** Creates a new instance of CTSConfigurator */
    public JXYConfigurator() {
        /* open CXYConf */
    }
    /*
    public CTSConfigurator(JAMSTableModel tmodel){
        this.tmodel = tmodel;
    }
     **/
    
    public JXYConfigurator(JFrame parent, JTable table){
        
        this.parent = parent;
        //super(parent, "JAMS JTS Viewer");
        setTitle("JAMS XYPlot Viewer");
        URL url = this.getClass().getResource("/jams/components/gui/resources/JAMSicon16.png");
        ImageIcon icon = new ImageIcon(url);
        setIconImage(icon.getImage());
        
        setLayout(new FlowLayout());
        Point parentloc = parent.getLocation();
        setLocation(parentloc.x + 30, parentloc.y + 30);
        
        this.table = table;
        
        this.rows = table.getSelectedRows();
        this.columns = table.getSelectedColumns();
        this.graphCount = columns.length;
        this.headers = new String[graphCount];/* hier aufpassen bei reselection xxx reselecton -> neue instanz */

        writeSortedData(columns[0]);
//        this.legendEntries = new String[graphCount];
        
//        for(int k=0;k<graphCount;k++){
//            headers[k] = table.getColumnName(columns[k]);
//            legendEntries[k] = headers[k];
//        }
        
        
        setSize(680,480);
        //setMinimumSize(new Dimension(680,480));
        createPanel();
        //timePlot();
        pack();
        setVisible(true);
        
    }
    
//    public void setTable(JTable table){
//
//        this.table = table;
//        this.rows = table.getSelectedRows();
//        this.columns = table.getSelectedColumns();
//        this.graphCount = columns.length;
//        this.headers = new String[graphCount];/* hier aufpassen bei reselection xxx reselecton -> neue instanz */
//        this.parent = parent;
//        this.legendEntries = new String[graphCount];
//
//        for(int k=0;k<graphCount;k++){
//            headers[k] = table.getColumnName(columns[k]);
//            legendEntries[k] = headers[k];
//        }
//
//        /* now call createPanel() */
//    }
    
    public void createPanel(){
        
        thisDlg = this;
        colour_cnt = 0;
        /* create ColorMap */
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
        
        JLabel nameLabel = new JLabel("Name");
        JLabel posLabel = new JLabel("Position");
        JLabel typeLabel = new JLabel("Renderer");
        JLabel colorLabel = new JLabel("Colour");
        JLabel dataLabel = new JLabel("Select Data / Legend Entry");
        JLabel timeLabel = new JLabel("Data Range");
        JLabel emptyTimeLabel = new JLabel("    ");
        JLabel legendLabel = new JLabel("Legend Entry");
      
        nameLabel.setBackground(Color.DARK_GRAY);
        posLabel.setBackground(Color.DARK_GRAY);
        typeLabel.setBackground(Color.DARK_GRAY);
        colorLabel.setBackground(Color.DARK_GRAY);
        dataLabel.setBackground(Color.DARK_GRAY);
        timeLabel.setBackground(Color.DARK_GRAY);
        
        //mainpanel = new JPanel();
        setLayout(new BorderLayout());
        //mainpanel.setBackground(Color.WHITE);
        
        plotpanel = new JPanel();
        plotpanel.setLayout(new BorderLayout());
        
        frame = new JPanel();
        frame.setLayout(new BorderLayout());
        //frame.setSize(640,80);
        
        graphScPane = new JScrollPane();
                
        optionpanel = new JPanel();
        //optionpanel.setLayout(new FlowLayout());
        graphpanel = new JPanel();
        
        initGroupUI();
    
        southpanel = new JPanel();
        southpanel.setLayout(new FlowLayout());
        
        edTitlePanel = new JPanel();
        edTitlePanel.setLayout(new FlowLayout());
        edLeftAxisPanel = new JPanel();
        edLeftAxisPanel.setLayout(new FlowLayout());
        edRightAxisPanel = new JPanel();
        edRightAxisPanel.setLayout(new FlowLayout());
//        
//        
//        
//        
//        /*
//        for(int k=0;k<headers.length;k++){
//         
//           datapanels.add(new JPanel());
//           datapanels.get(k).setLayout(new FlowLayout());
//         
//           activate.add(new JCheckBox(headers[k],true));
//           //datachoice.add(new JComboBox(headers));
//           poschoice.add(new JComboBox(positions));
//           typechoice.add(new JComboBox(types));
//           colorchoice.add(new JComboBox(colors));
//        }
//         */
//        
////        datapanels = new JPanel[graphCount];
////        activate = new JCheckBox[graphCount];
////        poschoice = new JComboBox[graphCount];
////        typechoice = new JComboBox[graphCount];
////        colorchoice = new JComboBox[graphCount];
//        
//        //createActionListener();
//        
        //edTitleField.setColumns(20);
        edTitleField.setText("Plot Title");
        edTitleField.setSize(40,10);
        edTitleField.addActionListener(plotbuttonclick);
        //edLeftField.setColumns(20);
        edLeftField.setText("Left Axis Title");
        edLeftField.addActionListener(plotbuttonclick);
        edLeftField.setSize(40,10);
        //edRightField.setColumns(20);
        edRightField.setText("Right Axis Title");
        edRightField.addActionListener(plotbuttonclick);
        edRightField.setSize(40,10);
        
        edXAxisField.setText("x axis title");
        edXAxisField.addActionListener(plotbuttonclick);
        applyButton.addActionListener(plotbuttonclick);
        
//        edTitlePanel.add(edTitle);
//        edTitlePanel.add(edTitleField);
//        edLeftAxisPanel.add(edLeft);
//        edLeftAxisPanel.add(edLeftField);
//        edRightAxisPanel.add(edRight);
//        edRightAxisPanel.add(edRightField);
        
        optionpanel.add(edTitle);
        optionpanel.add(edTitleField);
        optionpanel.add(edLeft);
        optionpanel.add(edLeftField);
        optionpanel.add(edRight);
        optionpanel.add(edRightField);
        optionpanel.add(applyButton);
        
        GraphProperties prop;
        
        for(int k=0;k<graphCount;k++){
            
            prop = new GraphProperties(parent, table, this);
            prop.setSelectedColumn(columns[k]);
            prop.setXSeries(columns[0]);
            prop.setSelectedRows(rows);

            if(k==0){ //initial x axis
                prop.getIsXAxisButton().setSelected(true);
                prop.setIsXSeries(true);
                prop.getDataChoice().setEnabled(false);
                prop.getDataChoiceSTART().setEnabled(true);
                prop.getDataChoiceEND().setEnabled(true);
                prop.getColorChoice().setEnabled(false);
                prop.getPosChoice().setEnabled(false);
                prop.getLegendField().setEnabled(false);
                
                prop.getAddButton().setEnabled(false);
                prop.getRemButton().setEnabled(false);
                prop.getUpButton().setEnabled(false);
                prop.getDownButton().setEnabled(false);
            }else{
                prop.getDataChoiceSTART().setEnabled(false);
                prop.getDataChoiceEND().setEnabled(false);
            }
                prop.getAddButton().setEnabled(true);
                prop.getRemButton().setEnabled(true);
                prop.getUpButton().setEnabled(true);
                prop.getDownButton().setEnabled(true);

            prop.setColor(k%11);
            colour_cnt=k;
            
//            prop.setColor((String) colorchoice.getSelectedItem());
//            prop.setPosition((String) poschoice.getSelectedItem());
//            prop.setRendererType(typechoice.getSelectedIndex());
            prop.setName(table.getColumnName(k+1));
            prop.setLegendName(table.getColumnName(k+1));
            
            
            propVector.add(k,prop);
        }
        
        //initial data intervals
        int[] range = setInitialDataIntervals();
        
        //set data intervals
        for(int k=0; k<propVector.size(); k++){
            
            prop = propVector.get(k);
            
            prop.setXIntervals(range);
            prop.setDataSTART(this.row_start);
            prop.setDataEND(this.row_end);
            prop.applyXYProperties();
            addPropGroup(propVector.get(k));

        }
        
        finishGroupUI();
        createOptionPanel();
    
        /* initialise JTSPlot */
        //JAMSTimePlot jts = new JAMSTimePlot(propVector);
        jxys.setPropVector(propVector);
        jxys.createPlot();
 
        JPanel graphPanel = new JPanel();
        JPanel optPanel = new JPanel();
        graphPanel.add(graphpanel);
        optPanel.add(optionpanel);
        
        graphScPane = new JScrollPane(graphPanel);
        //graphScPane.setPreferredSize(new Dimension(200,150));
        graphScPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        optionpanel.setBorder(new EtchedBorder());
        plotScPane = new JScrollPane(jxys.getPanel());
        optScPane = new JScrollPane(optPanel);
        split_hor.add(optScPane, 0);
        split_hor.add(graphScPane, 1);
        split_vert.add(split_hor, 0);
        split_vert.add(plotScPane, 1);
        add(split_vert);
//        add(frame, BorderLayout.NORTH);
//        add(plotScPane, BorderLayout.CENTER);
        
        plotAllGraphs();
        //jts.plotRight(1, "rightAxisName", true);
    
    }
    
    private void writeSortedData(int x_col){
        int row_nr = table.getRowCount();
        int col_nr = table.getColumnCount();
        sorted_Row = new XYRow[row_nr];
        double[] rowarray; 
        
        for(int i=0; i<row_nr; i++){
            
            rowarray = new double[col_nr];
            for(int j=0; j<col_nr; j++){
                try{
                   rowarray[j] = (Double) table.getValueAt(i,j);

                }catch(ClassCastException cce){
                   rowarray[j] = 0;
                }
            }
            //double[] array = rowarray;
            sorted_Row[i] = new XYRow(rowarray, x_col);
            
        }
        java.util.Arrays.sort(sorted_Row);
    }
    
    private void resortData(int x_col){
        for(int i=0; i<sorted_Row.length; i++){         
                sorted_Row[i].setCompareIndex(x_col);    
        }
        java.util.Arrays.sort(sorted_Row);       
    }
    
    public int[] setInitialDataIntervals(){
        
//        double row_start = (Double) table.getValueAt(rowSelection[0], x_series_col);
//        double row_end = (Double) table.getValueAt(rowSelection[rowSelection.length - 1], x_series_col);
        int[] range = new int[2];
        int x_column = propVector.get(x_series_index).getSelectedColumn();
        
        this.row_start = sorted_Row[0].col[x_column];
        this.row_end = sorted_Row[sorted_Row.length -1].col[x_column];
        range[0] = 0;
        range[1] = sorted_Row.length -1;
        
        //double start, end;
        
//        for(int i=0; i<rowSelection.length; i++){
//            start = (Double) table.getValueAt(rowSelection[i], x_series_col);
//            end = (Double) table.getValueAt(rowSelection[i], x_series_col);
//            if(start < row_start) row_start = start;
//            if(end > row_end) row_end = end;
//        }
        
//        for(int i=0; i<sorted_Row.length; i++){
//            start = data[i].x;
//            end = data[i].x;
//            if(start < row_start){ 
//                
//                range[0] = i;
//                row_start = start;
//                    
//            }
//            if(end > row_end){
//                range[1] = i;
//                row_end = end;
//            }
//        }
        
        return range;
        
        
    }
    
    public int[] setPossibleDataIntervals(){
        double possible_start, possible_end;
        
        int[] range = new int[2];
        GraphProperties x_prop = propVector.get(x_series_index);
        
        double start = x_prop.readDataSTART();
        double end = x_prop.readDataEND();

        int i=0;
        int x_col = x_prop.getSelectedColumn();
        boolean out_of_boundaries = (start < sorted_Row[0].col[x_col]) || (start > sorted_Row[sorted_Row.length-1].col[x_col]);
        
        if(end < start) end = start;
        
        if(!out_of_boundaries){
            
            while(!(start >= sorted_Row[i].col[x_col] && start <= sorted_Row[i+1].col[x_col])){
                i++;
            }
            start = sorted_Row[i+1].col[x_col];
            range[0] = i;
        }else{
            if(start < sorted_Row[0].col[x_col]){
                start = sorted_Row[0].col[x_col];
                range[0] = 0;
            }
            if(start > sorted_Row[sorted_Row.length-1].col[x_col]){
                start = sorted_Row[sorted_Row.length-1].col[x_col];
                range[0] = sorted_Row.length -1;
            }
        }

        out_of_boundaries = (end < sorted_Row[0].col[x_col]) || (end > sorted_Row[sorted_Row.length-1].col[x_col]);
        if(!out_of_boundaries){
            
            while(!(end >= sorted_Row[i].col[x_col] && end <= sorted_Row[i+1].col[x_col])){
                i++;
            }
            end = sorted_Row[i].col[x_col];
            range[1] = i;
            
        }else{
            if(end < sorted_Row[0].col[x_col]){
                end = sorted_Row[0].col[x_col];
                range[1] = 0;
            }
            if(end > sorted_Row[sorted_Row.length-1].col[x_col]){
                end = sorted_Row[sorted_Row.length-1].col[x_col];
                range[1] = sorted_Row.length -1;
            }
        }
        
        this.row_start = start;
        this.row_end = end;

        return range;
    }
    
    public void addGraph(GraphProperties prop){
        
        int i = propVector.indexOf(prop);
        double d_start, d_end;
        GraphProperties newProp = new GraphProperties(parent, table, this);
        colour_cnt++;
        
        AddGraphDlg dlg = new AddGraphDlg();
        dlg.setVisible(true);
        
        if(dlg.getOK()){
            newProp.setPosition(dlg.getSide());
            i = dlg.getPosition();
            dlg.dispose();
        }
        
        if(i>0){
            d_start = prop.getDataSTART();
            d_end = prop.getDataEND();
            newProp.setDataSTART(d_start);
            newProp.setDataEND(d_end);
        }
        newProp.setColor(colour_cnt%11);
        propVector.add(newProp);
        
        graphCount = propVector.size();
        
        initGroupUI();
        
        for(int k=0;k<graphCount;k++){
            
            newProp = propVector.get(k);
//            newProp.setIndex(k);
            //prop.getPlotButton().addActionListener(plotbuttonclick);
            
            if(newProp.isXSeries()){
                 x_series_index = k;
            }
            addPropGroup(newProp);
            
            
            
            //graphpanel.add(propVector.get(k-1).getGraphPanel());
            
        }
        xChanged(propVector.get(x_series_index));
        finishGroupUI();
        //mainpanel.repaint();
        //frame.updateUI();
        //pack();
        repaint();
    }
    
    public void removeGraph(GraphProperties prop){
        
        if(graphCount > 1){
        GraphProperties newProp;
        propVector.remove(propVector.indexOf(prop));
        graphCount = propVector.size();
        
        initGroupUI();
        
        for(int k=0;k<graphCount;k++){
            
            newProp = propVector.get(k);
//            prop.setIndex(k);
            //prop.getPlotButton().addActionListener(plotbuttonclick);
            
            if(newProp.isXSeries()){
                 x_series_index = k;
            }
            addPropGroup(newProp);
            
            
            
            //graphpanel.add(propVector.get(k-1).getGraphPanel());
            
        }
        xChanged(propVector.get(x_series_index));
        finishGroupUI();
        //mainpanel.updateUI();
//        pack();
        repaint();  
        }
    }
    
    public void upGraph(GraphProperties prop){
        
        int i = propVector.indexOf(prop);
               
        
        boolean xChanged = false;
        GraphProperties newProp;
        //GraphProperties xProp = propVector.get(x_series_index);

        
        if(i-1>=0 && i-1<graphCount){
            propVector.remove(prop);
            propVector.add(i-1, prop);
        
            handleRenderer();
            initGroupUI();

            for(int k=0;k<graphCount;k++){

                newProp = propVector.get(k);
                //prop.setIndex(k);
                if(newProp.isXSeries()){
                    x_series_index = k;
                    xChanged = true;     
                }
                
                
                addPropGroup(newProp);
            }
            
            xChanged(propVector.get(x_series_index));
            finishGroupUI();
            repaint();
        }
    }
    
    public void downGraph(GraphProperties prop){
        
        int i = propVector.indexOf(prop);
        int x_series = columns[0];
        boolean xChanged = false;
        if(i<propVector.size()){
        GraphProperties newProp = propVector.get(i+1);
        
        if(i+1>=0 && i+1<graphCount){
            
            propVector.remove(i+1);
            propVector.add(i, newProp);

            graphCount = propVector.size();
            handleRenderer();
            initGroupUI();

            for(int k=0;k<graphCount;k++){

                newProp = propVector.get(k);
                newProp.setIndex(k);
                if(prop.isXSeries()){
                    x_series_index = k;
                    xChanged = true;
                }
                //prop.getPlotButton().addActionListener(plotbuttonclick);

                addPropGroup(newProp);
                
            }
            
            xChanged(propVector.get(x_series));
            
            finishGroupUI();
            
            
            repaint();
        }
        }
    }
    
    private void updatePropVector(){
        
        for(int i=0; i<propVector.size(); i++){
            propVector.get(i).applyXYProperties();
        }
    }
    
    public void xChanged(GraphProperties prop){
        
        int index = propVector.indexOf(prop);
        //x_series_col = columns[index];
        x_series_index = index;
        
        resortData(propVector.get(x_series_index).getSelectedColumn());
        
        for(int i=0; i<propVector.size(); i++){
            if(i != index){
//                propVector.get(i).setXSeries(columns[x_series_index]);
                propVector.get(i).setXSeries(propVector.get(x_series_index).getSelectedColumn());
                propVector.get(i).setXChanged(true);
                propVector.get(i).setIsXSeries(false);
                propVector.get(i).getDataChoice().setEnabled(true);
                propVector.get(i).getDataChoiceSTART().setEnabled(false);
                propVector.get(i).getDataChoiceEND().setEnabled(false);
                propVector.get(i).getColorChoice().setEnabled(true);
                propVector.get(i).getPosChoice().setEnabled(true);
                propVector.get(i).getAddButton().setEnabled(true);
                propVector.get(i).getRemButton().setEnabled(true);
                propVector.get(i).getUpButton().setEnabled(true);
                propVector.get(i).getDownButton().setEnabled(true);
                propVector.get(i).getLegendField().setEnabled(true);
                propVector.get(i).applyXYProperties();
            }
            
        }
        propVector.get(index).setXChanged(true);
        propVector.get(index).setIsXSeries(true);
        propVector.get(index).setXSeries(propVector.get(x_series_index).getSelectedColumn());
        propVector.get(index).getDataChoice().setEnabled(false);
        propVector.get(index).getDataChoiceSTART().setEnabled(true);
        propVector.get(index).getDataChoiceEND().setEnabled(true);
        propVector.get(index).getColorChoice().setEnabled(false);
        propVector.get(index).getPosChoice().setEnabled(false);
        propVector.get(index).getAddButton().setEnabled(false);
        propVector.get(index).getRemButton().setEnabled(false);
        propVector.get(index).getUpButton().setEnabled(false);
        propVector.get(index).getDownButton().setEnabled(false);
        propVector.get(index).getLegendField().setEnabled(false);
        propVector.get(index).applyXYProperties();
    }
       
//    public void setENDIntervals(double end){
//        for(int i=0; i<propVector.size(); i++){
//            if(propVector.get(i).isXAxis.isEnabled()) propVector.get(i).setDataEND(end);
//            
//        }
//    }
    
    public void plotGraph(GraphProperties prop){
       
            //propVector.get(i).applyProperties();
//            if(propVector.get(i).getPosChoice().getSelectedItem() == "left"){
//                jxys.plotLeft(rLeftBox.getSelectedIndex(), edLeftField.getText(), edXAxisField.getText(), invLeftBox.isSelected());
//            }
//            if(propVector.get(i).getPosChoice().getSelectedItem() == "right"){
//                jxys.plotRight(rRightBox.getSelectedIndex(), edRightField.getText(), edXAxisField.getText(), invRightBox.isSelected());
//            }
            if(prop.getPosChoice().getSelectedItem() == "left"){
                jxys.plotLeft(rLeftBox.getSelectedIndex(), edLeftField.getText(), edXAxisField.getText(), invLeftBox.isSelected());
            }
            if(prop.getPosChoice().getSelectedItem() == "right"){
                jxys.plotRight(rRightBox.getSelectedIndex(), edRightField.getText(), edXAxisField.getText(), invRightBox.isSelected());
            }
    }    
    
    public void plotAllGraphs(){
    updatePropVector();
            int l=0;
            int r=0;
            for(int i=0; i<propVector.size(); i++){
                if(propVector.get(i).getPosChoice().getSelectedItem() == "left"){
                    l++;
                }
                if(propVector.get(i).getPosChoice().getSelectedItem() == "right"){
                    r++;
                }
            }
            if(l>0){
                jxys.plotLeft(rLeftBox.getSelectedIndex(), edLeftField.getText(), edXAxisField.getText(), invLeftBox.isSelected());
            }
            if(r>0){
                jxys.plotRight(rRightBox.getSelectedIndex(), edRightField.getText(), edXAxisField.getText(), invRightBox.isSelected()); 
            }
            
            jxys.setTitle(edTitleField.getText());
    }
    
    public void handleRenderer(){
        int r=0, l=0;
        for(int i=0; i<propVector.size(); i++){
                if(propVector.get(i).getPosChoice().getSelectedItem() == "left"){
                    l++;
                }
                if(propVector.get(i).getPosChoice().getSelectedItem() == "right"){
                    r++;
                }
            }
        
        if(((l<2 || l>2) || (r<2 || r>2)) && rLeftBox.getItemCount()==8){
            rLeftBox.removeItemAt(7);
            rRightBox.removeItemAt(7);           
        }
 
        if((l == 2 || r == 2) && rLeftBox.getItemCount()==7){
            rLeftBox.addItem("Difference");
            rRightBox.addItem("Difference");  
        }     
    }
    
    private void createOptionPanel(){
        GroupLayout optLayout = new GroupLayout(optionpanel);
        optionpanel.setLayout(optLayout);
        optLayout.setAutoCreateGaps(true);
        optLayout.setAutoCreateContainerGaps(true);
        
        addButton.addActionListener(addbuttonclick);
        
        GroupLayout.SequentialGroup optHGroup = optLayout.createSequentialGroup();
        GroupLayout.SequentialGroup optVGroup = optLayout.createSequentialGroup();
        
        optVGroup.addGroup(optLayout.createParallelGroup()
        .addComponent(edTitle).addComponent(edTitleField));
        optVGroup.addGroup(optLayout.createParallelGroup()
        .addComponent(edLeft).addComponent(edLeftField));
        optVGroup.addGroup(optLayout.createParallelGroup()
        .addComponent(edRight).addComponent(edRightField));
        optVGroup.addGroup(optLayout.createParallelGroup()
        .addComponent(edXAxis).addComponent(edXAxisField));
        optVGroup.addGroup(optLayout.createParallelGroup()
        .addComponent(rLeftLabel).addComponent(rLeftBox));
        optVGroup.addGroup(optLayout.createParallelGroup()
        .addComponent(rRightLabel).addComponent(rRightBox));
        optVGroup.addGroup(optLayout.createParallelGroup()
        .addComponent(invLeftBox).addComponent(addButton));
        optVGroup.addGroup(optLayout.createParallelGroup()
        .addComponent(invRightBox).addComponent(applyButton));
        
        optHGroup.addGroup(optLayout.createParallelGroup().
                addComponent(edTitle).addComponent(edLeft).addComponent(edRight)
                .addComponent(edXAxis).addComponent(rLeftLabel).addComponent(rRightLabel)
                .addComponent(invLeftBox).addComponent(invRightBox));
        
        optHGroup.addGroup(optLayout.createParallelGroup().
                addComponent(edTitleField).addComponent(edLeftField).addComponent(edRightField)
                .addComponent(edXAxisField).addComponent(rLeftBox).addComponent(rRightBox).addComponent(addButton).addGap(1,1,1)
                .addComponent(applyButton));
        
        
        optLayout.setHorizontalGroup(optHGroup);
        optLayout.setVerticalGroup(optVGroup);
    }
    
    private void initGroupUI(){
        
        graphpanel.removeAll();
        
        JLabel nameLabel = new JLabel("Name");
        JLabel posLabel = new JLabel("Position");
        JLabel typeLabel = new JLabel("Colour / Position");
        JLabel colorLabel = new JLabel("Type/Colour");
        JLabel dataLabel = new JLabel("Data / Legend Entry");
        JLabel timeLabel = new JLabel("Data Range");
        JLabel emptyTimeLabel = new JLabel("    ");
        JLabel legendLabel = new JLabel("Legend Entry: ");
        
        gLayout = new GroupLayout(graphpanel);
        graphpanel.setLayout(gLayout);
        gLayout.setAutoCreateGaps(true);
        gLayout.setAutoCreateContainerGaps(true);
        
        hGroup = gLayout.createSequentialGroup();
        vGroup = gLayout.createSequentialGroup();
        group1 = gLayout.createParallelGroup();
        group2 = gLayout.createParallelGroup();
        group3 = gLayout.createParallelGroup();
        group4 = gLayout.createParallelGroup();
        group5 = gLayout.createParallelGroup();
        group6 = gLayout.createParallelGroup();
        group7 = gLayout.createParallelGroup();
        group8 = gLayout.createParallelGroup();
        group9 = gLayout.createParallelGroup();
        group10 = gLayout.createParallelGroup();
        group11 = gLayout.createParallelGroup();
        group12 = gLayout.createParallelGroup();
        group13 = gLayout.createParallelGroup();
        group14 = gLayout.createParallelGroup();
        group15 = gLayout.createParallelGroup();

              

        group1.addComponent(dataLabel);
        group2.addComponent(timeLabel);
        group3.addComponent(typeLabel);
        //group4.addComponent(colorLabel);

//        group9.addComponent();
//        group10.addComponent(emptyTimeLabel);
//        group11.addComponent(emptyTimeLabel);
//        group12.addComponent(emptyTimeLabel);
//        group13.addComponent(emptyTimeLabel);
//        group14.addComponent(emptyTimeLabel);
//        group15.addComponent(emptyTimeLabel);
   
        vGroup.addGroup(gLayout.createParallelGroup(Alignment.LEADING)
        .addComponent(dataLabel).addComponent(timeLabel).addComponent(typeLabel));
        
//        vGroup.addGroup(gLayout.createParallelGroup(Alignment.BASELINE)
//        .addComponent(dataLabel).addComponent(timeLabel).addComponent(typeLabel));

    }
    
    private void addPropGroup(GraphProperties prop){
        
            isXAxisGroup.add(prop.getIsXAxisButton());
            JLabel space1 = new JLabel(" ");
            JLabel space2 = new JLabel(" ");
            JLabel space3 = new JLabel(" ");
            JLabel space4 = new JLabel(" ");
            JLabel space5 = new JLabel("   ");
            JLabel space6 = new JLabel("   ");
            JTextField lf = prop.getLegendField();
            
            group6.addComponent(space5).addComponent(space6);
            
            group1.addComponent(prop.getDataChoice()).addComponent(lf).addGap(20);
            group2.addComponent(prop.getDataChoiceSTART()).addComponent(prop.getDataChoiceEND());
            group3.addComponent(prop.getColorChoice()).addComponent(prop.getPosChoice());
                       

            group9.addComponent(space3);
            group10.addComponent(prop.getIsXAxisButton());
            group11.addComponent(space4);
            
            group13.addComponent(prop.getRemButton());
            group14.addComponent(prop.getUpButton());
            group15.addComponent(prop.getDownButton());
                        
            vGroup.addGroup(gLayout.createParallelGroup(Alignment.LEADING)
            .addComponent(prop.getDataChoice()).addComponent(prop.getDataChoiceSTART()).addComponent(space5)
            .addComponent(prop.getColorChoice()).addComponent(space5).addComponent(prop.getIsXAxisButton()));
            vGroup.addGroup(gLayout.createParallelGroup(Alignment.TRAILING)
            .addComponent(lf).addComponent(prop.getDataChoiceEND()).addComponent(space6)
            .addComponent(prop.getPosChoice())
            .addComponent(space3)
            .addComponent(space4).addComponent(prop.getRemButton())
            .addComponent(prop.getUpButton()).addComponent(prop.getDownButton()));
            vGroup.addGroup(gLayout.createParallelGroup().addGap(20));
            
//            vGroup.addGroup(gLayout.createParallelGroup(Alignment.BASELINE)
//            .addComponent(prop.getNameLabel()).addComponent(prop.getPosChoice())
//            .addComponent(prop.getTypeChoice()).addComponent(prop.getColorChoice())
//            .addComponent(prop.getDataChoice()).addComponent(prop.getLegendField())
//            .addComponent(prop.getTimeChoiceSTART()).addComponent(prop.getTimeChoiceEND())
//            .addComponent(space1).addComponent(prop.getPlotButton())
//            .addComponent(space2).addComponent(prop.getAddButton())
//            .addComponent(prop.getRemButton())
//            .addComponent(prop.getUpButton()).addComponent(prop.getDownButton()));
    }
    
    private void finishGroupUI(){
        
        hGroup.addGroup(group1);
        hGroup.addGroup(group2);
        hGroup.addGroup(group6);
        hGroup.addGroup(group3);  
//        hGroup.addGroup(group4);
//        hGroup.addGroup(group5);
//        hGroup.addGroup(group6);
//        hGroup.addGroup(group7);
//        hGroup.addGroup(group8);
        
        hGroup.addGroup(group9);
        hGroup.addGroup(group10);
        hGroup.addGroup(group11);
        hGroup.addGroup(group12);
        hGroup.addGroup(group13);
        hGroup.addGroup(group14);
        hGroup.addGroup(group15);
        
        gLayout.setHorizontalGroup(hGroup);
        gLayout.setVerticalGroup(vGroup);
        
        
    }
    
    public JPanel getPanel(){
        return mainpanel;
    }
    
    public JPanel getCTSPlot(){
        return ctsplot.getPanel();
    }

    public void setParent(JFrame parent){
        this.parent = parent;
    }
    
    /*
    public void addGraph(){
        for(int i=0; i<headers.length; i++){
     
            datapanels.get(i).add(datachoice.get(i));
            datapanels.get(i).add(poschoice.get(i));
            datapanels.get(i).add(typechoice.get(i));
            datapanels.get(i).add(colorchoice.get(i));
     
            graphpanel.add(datapanels.get(i));
     
        }
    }
     */
    private void editProperties(){
        JDialog propDlg = new JDialog(parent,"Properties");
        int ct = headers.length;
        
        JLabel[] labels = new JLabel[ct];
        JTextField[] textFields = new JTextField[ct];
        JPanel[] inputpanels = new JPanel[ct];
        
        JPanel proppanel = new JPanel();
        proppanel.setLayout(new GridLayout(ct,1));
        
        for(int i=0; i<ct ; i++){
            labels[i] = new JLabel(headers[i]);
            textFields[i] = new JTextField(headers[i]);
            inputpanels[i] = new JPanel();
            inputpanels[i].setLayout(new FlowLayout());
            inputpanels[i].add(labels[i]);
            inputpanels[i].add(textFields[i]);
        }
        
        
        JScrollPane propPane = new JScrollPane(proppanel);
        
        
    }
    

    
//    public void timePlot(){
////
////
////        /* Festlegen welche cols zu valueLeft und welche zu valueRight gehören!! */
////
////        /* CTSPlot initialisieren */
//        ctsplot = new CTSPlot();
////
////       System.out.println("CTSPlot ctsplot = new CTSPlot();");
////
////        /* Parameter festlegen */
////
//        ctsplot.setTitle(edTitleField.getText());
//        ctsplot.setLeftAxisTitle(edLeftField.getText());
//        ctsplot.setRightAxisTitle(edRightField.getText());
////
////
////        //ctsplot.setGraphCountRight(columns.length);
//         /*
//        plotframe = new JDialog();
//        plotframe.setLayout(new FlowLayout());
//         System.out.println("plotframe.setLayout(new FlowLayout());");
//        plotframe.add(ctsplot.getPanel());
//         System.out.println("plotframe.add(ctsplot.getPanel());");
//        plotframe.pack();
//        plotframe.setVisible(true);
//          **/
////
////
////         //System.out.println("plotframe.setVisible(true)");
////
////        //JAMSCalendar test = new JAMSCalendar();
////        //if(table.getValueAt(rows[0], columns[0]).getClass() != test.getClass()){
//        int numActiveLeft=0;
//        int numActiveRight=0;
//        int corr=0;
//        
//        int row_start = 0;
//        int row_end = 0;
//        
//        boolean typechosen_R=false;
//        boolean typechosen_L=false;
//        String[] colorLeft = new String[graphCount];
//        String[] colorRight = new String[graphCount];
//        String[] titleLeft = new String[graphCount];
//        String[] titleRight = new String[graphCount];
////
////        /* zuordnung der graphen */
//        for(int i=0; i<graphCount; i++){
//            
//            GraphProperties prop = propVector.get(i);
//            int s = prop.getTimeSTART();
//            int e = prop.getTimeEND();
//            
//            if(s > row_start){
//                row_start = s;
//            }
//            if(e > row_end){
//                row_end = e;
//            }
//            
//            if(prop.getPosition()== "left"){
//                
//                ctsplot.setTypeLeft(prop.getRendererType());
//                
//                colorLeft[i - numActiveRight] = prop.getColor();
//                titleLeft[i - numActiveRight] = prop.getLegendName();
//                numActiveLeft++;
//            }
//            if(prop.getPosition() == "right"){
//                ctsplot.setTypeRight(prop.getRendererType());
//                
//                colorRight[i - numActiveLeft] = prop.getColor();
//                titleRight[i - numActiveLeft] = prop.getLegendName();
//                numActiveRight++;
//            }
//        }
//        
//        String[] legendLeft = new String[numActiveLeft];
//        String[] legendRight = new String[numActiveRight];
//        
//        for(int n=0; n<numActiveLeft; n++){
//            legendLeft[n] = titleLeft[n];
//        }
//        for(int n=0; n<numActiveRight; n++){
//            legendRight[n] = titleRight[n];
//        }
//        ctsplot.setTitleLeft(legendLeft);
//        ctsplot.setTitleRight(legendRight);
//        
//        ctsplot.setGraphCountLeft(numActiveLeft);
//        ctsplot.setGraphCountRight(numActiveRight);
//        
//        ctsplot.setColorLeft(colorLeft);
//        ctsplot.setColorRight(colorRight);
//        ctsplot.setTitleLeft(titleLeft);
//        ctsplot.setTitleRight(titleRight);
//        /* CTSPlot erstellen */
//        
//        //removeAll(); /* nullPionterEx at first startup? */
//        //add(frame, BorderLayout.NORTH);
//        ctsplot.createPlot();
//        //plotScPane = new JScrollPane(ctsplot.getPanel());
//        plotScPane.setViewportView(ctsplot.getPanel());
//        //mainpanel.add(ctsplot.getChartPanel(), BorderLayout.CENTER);
//        //System.out.println("ctsplot.createPlot();");
//       
//        double[] valueLeft = new double[numActiveLeft];
//        double[] valueRight = new double[numActiveRight];
//        
//        
//       
//                
//        /* jedesmal fragen, ob der graph zu valueLEFT GEHÖRT (COMBObOX ABFRAGEN) */
//        /* ACHTUNG: Funktioniert noch nicht bei addGraph() */
//        //int rowcount = table.getRowCount();
//        int c = 0;
//        
//        for(int k=row_start; k<=row_end; k++){
//                    
//                    int corrLeft = 0;
//                    int corrRight = 0;
//                    Object value = null;
//
//                    for(int i=0;i<graphCount;i++){
//                        
//                        GraphProperties prop = propVector.get(i);
//                        //value = (Double) table.getValueAt(rows[k],columns[i]);
//                            //int rows[] = prop.getSelectedRows();
//                            int col = prop.getSelectedColumn();
//                            int s = prop.getTimeSTART();
//                            int e = prop.getTimeEND();
//                            
//                            if(!(k<s) && !(k>e)){
//
//                                value = table.getValueAt(k,col);
//                                if(value.getClass() != java.lang.Double.class){
//                                        value = 0.0;
//                                    }
//
//                                if(prop.getPosition() == "left"){
//                                        //valueLeft[i - corrLeft] = (Double) table.getValueAt(rows[k],columns[i]);
//                                    valueLeft[i - corrLeft] = (Double) value;
//                                    corrRight++;
//
//                                }
//
//                                if(prop.getPosition() == "right"){
//                                    //valueRight[i - corrRight] = (Double) table.getValueAt(rows[k],columns[i]);
//                                    valueRight[i - corrRight] = (Double) value;
//                                    corrLeft++;
//                                }
//                            }else{
//                                corrRight++;
//                                corrLeft++;
//                                corr++;
//                            }
//                    }                 
//                ctsplot.plot((JAMSCalendar)table.getValueAt(k,0), valueLeft, valueRight);
//            }
//
//        repaint();
//        //pack();
//
//
//
//    }
    
    
    
    
    /****** EVENT HANDLING ******/
    
    
    ActionListener titleListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            ctsplot.getChart().setTitle(edTitleField.getText());
        }
    };
    
    ActionListener propbuttonclick = new ActionListener(){
        public void actionPerformed(ActionEvent e) {
            ctsplot.getChartPanel().doEditChartProperties();
        }
    };
    
    ActionListener addbuttonclick = new ActionListener(){
        public void actionPerformed(ActionEvent e) {
            addGraph(propVector.get(0));
        }
    };
    
    ActionListener plotbuttonclick = new ActionListener(){
        public void actionPerformed(ActionEvent e) {
            
            int[] range = setPossibleDataIntervals();
            for(int i=0; i<propVector.size(); i++){
                GraphProperties prop = propVector.get(i);
                
                prop.setXIntervals(range);
                prop.setDataSTART(row_start);
                prop.setDataEND(row_end);
                prop.applyXYProperties();

            }
            plotAllGraphs();
        }
    };
    
    ActionListener actChanged = new ActionListener(){
        public void actionPerformed(ActionEvent e) {
            //timePlot();
        }
    };
    
    
    
    
    
    

    

    
    public void createActionListener(){
        
        Vector<ActionListener> addAction = new Vector<ActionListener>();
        
        for(int k=0;k<graphCount;k++){
            /* reicht hier ein listener für alle boxes? scheint so... */
            activationChange[k] = new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    //timePlot();
                    
                }
            };
            
            
        }
        
        
    }
    
    private class AddGraphDlg extends JDialog{
 
        boolean result;
        int max;
        String side;
        int side_index;
        int position;
       
        JSpinner posSpinner;
        JComboBox sideChoice;
        JButton okButton;
        
        public AddGraphDlg(){
            super(thisDlg, "Add Graph", true);
            URL url = this.getClass().getResource("/jams/components/gui/resources/JAMSicon16.png");
            ImageIcon icon = new ImageIcon(url);
            setIconImage(icon.getImage());
            Point parentloc = thisDlg.getLocation();
            setLocation(parentloc.x + 50, parentloc.y + 50);
            createPanel();
        }
        
        void createPanel(){
            setLayout(new FlowLayout());
            
            max = propVector.size();
            String[] posArray = {"left","right"};
            posSpinner = new JSpinner(new SpinnerNumberModel(max,0,max,1));
            sideChoice = new JComboBox(posArray);
            sideChoice.setSelectedIndex(0);
            JButton okButton = new JButton("OK");
            
            add(sideChoice);
            add(posSpinner);
            add(okButton);
            okButton.addActionListener(ok);
            pack();
        }
        
        String getSide(){
            if(side_index == 0) side = "left";
            else side = "right";
            
            return side;
        }
        int getPosition(){
            return position;
        }
        boolean getOK(){
            return result;
        }

        ActionListener ok = new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                //e.getActionCommand(); ...
                
                side_index = sideChoice.getSelectedIndex();
                position = (Integer) posSpinner.getValue();
                result = true;
                setVisible(false);
                
            }
        };
    }
}

class XYRow implements Comparable<XYRow>{
        
        double[] col;
        int compare_index;
        public XYRow(double[] rowdata, int compare_index){
            this.col = rowdata;
            this.compare_index = compare_index;
        }
        
        public void setCompareIndex(int compare_index){
            this.compare_index = compare_index;
        }
        
        public int compareTo(XYRow arg){
            
            if(col[compare_index] < arg.col[compare_index]) return -1;
            if(col[compare_index] > arg.col[compare_index]) return 1;
            return 0;

        }
    }

