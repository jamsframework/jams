/*
 * CTSConfigurator.java
 *
 * Created on 2. September 2007, 00:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.gui.spreadsheet;

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
public class CTSConfigurator {
    
    private JDialog parent;
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
    
    private JPanel[] datapanels;
    private JScrollPane graphScPane;
    
    private String[] headers;
    
    private JLabel edTitle = new JLabel("      Plot Title: ");
    private JLabel edLeft  = new JLabel(" Left axis title: ");
    private JLabel edRight = new JLabel("Right axis title: ");
    
    private JTextField edTitleField = new JTextField(14);
    private JTextField edLeftField = new JTextField(14);
    private JTextField edRightField = new JTextField(14);
    
    private Vector<GraphProperties> propVector = new Vector<GraphProperties>();
    
//    private String[] headers;
//    //private String[] colors = {"yellow","orange","red","pink","magenta","cyan","blue","green","gray","lightgray","black"};
    //private String[] colors = {"red","blue","green","black","magenta","cyan","yellow","gray","orange","lightgray","pink"};
//    private String[] types = {"Line","Bar","Area","Line and Base","Dot","Difference","Step","StepArea"};
//    private String[] positions = {"left","right"};
    
    /* test*/
//    private Color[] colors_ = {Color.RED, Color.BLUE};
    
//    private String[] legendEntries;
    
    private int index;
    
    HashMap<String, Color> colorTable = new HashMap<String, Color>();
    
    int[] rows, columns;
    JTable table;
    
    CTSPlot ctsplot;
    
    /*buttons*/
    JButton addbutton = new JButton("add graph");
    JButton plotbutton = new JButton("plot");
    JButton deletebutton = new JButton("delete");
    JButton propbutton = new JButton("properties");
    
    int graphCount=0;
    
    /** Creates a new instance of CTSConfigurator */
    public CTSConfigurator() {
        /* open CTSConf */
    }
    /*
    public CTSConfigurator(JAMSTableModel tmodel){
        this.tmodel = tmodel;
    }
     **/
    
    public CTSConfigurator(JDialog parent, JTable table){
        
        this.table = table;
        
        this.rows = table.getSelectedRows();
        this.columns = table.getSelectedColumns();
        this.graphCount = columns.length;
        this.headers = new String[graphCount];/* hier aufpassen bei reselection xxx reselecton -> neue instanz */
        this.parent = parent;
//        this.legendEntries = new String[graphCount];
        
//        for(int k=0;k<graphCount;k++){
//            headers[k] = table.getColumnName(columns[k]);
//            legendEntries[k] = headers[k];
//        }
        
        
        createPanel();
        
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
        
        mainpanel = new JPanel();
        mainpanel.setLayout(new BorderLayout());
        
        plotpanel = new JPanel();
        plotpanel.setLayout(new BorderLayout());
        
        frame = new JPanel();
        frame.setLayout(new GridLayout(3,1));
        
        graphScPane = new JScrollPane();
        
        optionpanel = new JPanel();
        optionpanel.setLayout(new GridLayout(3,1));
        graphpanel = new JPanel();
        graphpanel.setLayout(new GridLayout(graphCount,1));
        southpanel = new JPanel();
        southpanel.setLayout(new FlowLayout());
        
        
        
        edTitlePanel = new JPanel();
        edTitlePanel.setLayout(new FlowLayout());
        edLeftAxisPanel = new JPanel();
        edLeftAxisPanel.setLayout(new FlowLayout());
        edRightAxisPanel = new JPanel();
        edRightAxisPanel.setLayout(new FlowLayout());
               
        edTitleField.setColumns(20);
        edTitleField.setText("Plot Title");
        edTitleField.addActionListener(actChanged);
        edLeftField.setColumns(20);
        edLeftField.setText("Left Axis Title");
        edLeftField.addActionListener(actChanged);
        edRightField.setColumns(20);
        edRightField.setText("Right Axis Title");
        edRightField.addActionListener(actChanged);
        
        edTitlePanel.add(edTitle);
        edTitlePanel.add(edTitleField);
        edLeftAxisPanel.add(edLeft);
        edLeftAxisPanel.add(edLeftField);
        edRightAxisPanel.add(edRight);
        edRightAxisPanel.add(edRightField);
        
        optionpanel.add(edTitlePanel);
        optionpanel.add(edLeftAxisPanel);
        optionpanel.add(edRightAxisPanel);
        
        for(int k=1;k<=graphCount;k++){
            
            GraphProperties prop = new GraphProperties(parent, table);
            //propVector.add(new GraphProperties(parent,table));
            
            prop.setSelectedColumn(columns[k-1]);
            prop.setSelectedRows(rows);
//            prop.setColor((String) colorchoice.getSelectedItem());
//            prop.setPosition((String) poschoice.getSelectedItem());
//            prop.setRendererType(typechoice.getSelectedIndex());
            prop.setName(table.getColumnName(k));
            prop.setLegendName(table.getColumnName(k));
            
            propVector.add(prop);
            graphpanel.add(propVector.get(k-1).getGraphPanel());
            
        }
        
        
        //JComboBox colortest = new JComboBox(colors_);
        //southpanel.add(colortest);
        
        
        addbutton.addActionListener(addbuttonclick);
        plotbutton.addActionListener(plotbuttonclick);
        propbutton.addActionListener(propbuttonclick);
        
        //optionpanel.add(addbutton);
        //southpanel.add(deletebutton);
        southpanel.add(addbutton);
        southpanel.add(propbutton);
        southpanel.add(plotbutton);
        
        
        
        
        graphScPane = new JScrollPane(graphpanel);
        
        frame.add(graphScPane);
        //frame.add(optionpanel);
        frame.add(southpanel);
        frame.add(optionpanel);
        mainpanel.add(frame, BorderLayout.WEST);
        /** CTSConfigurator will be added to CTSViewer ******
         * frame.pack();
         * frame.setVisible(true);
         */
    }
    
    private void addGraph(){
        
        GraphProperties prop = new GraphProperties(parent,table);
        //prop.showPropDlg();
        
        //if(prop.getResult()){
        graphCount++;
        graphpanel.setLayout(new GridLayout(graphCount,1));
        graphpanel.add(prop.getGraphPanel());
        propVector.add(prop);
        
        
        //}
        
        graphScPane.setViewportView(graphpanel);
        frame.repaint();
        
        
    }
    
    public JPanel getPanel(){
        return mainpanel;
    }
    
    public JPanel getCTSPlot(){
        return ctsplot.getPanel();
    }
    
    public JButton getDeleteButton(){
        return deletebutton;
    }
    
    public void setIndex(int index){
        this.index = index;
    }
    
    private void disableEnableFunct(){
        for(int k=0;k<graphCount;k++){
            
        }
    }
    
    public void setParent(JDialog parent){
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
    
    public void timePlot(){
//
//
//        /* Festlegen welche cols zu valueLeft und welche zu valueRight gehören!! */
//
//        /* CTSPlot initialisieren */
        ctsplot = new CTSPlot();
//
//       System.out.println("CTSPlot ctsplot = new CTSPlot();");
//
//        /* Parameter festlegen */
//
        ctsplot.setTitle(edTitleField.getText());
        ctsplot.setLeftAxisTitle(edLeftField.getText());
        ctsplot.setRightAxisTitle(edRightField.getText());
//
//
//        //ctsplot.setGraphCountRight(columns.length);
         /*
        plotframe = new JDialog();
        plotframe.setLayout(new FlowLayout());
         System.out.println("plotframe.setLayout(new FlowLayout());");
        plotframe.add(ctsplot.getPanel());
         System.out.println("plotframe.add(ctsplot.getPanel());");
        plotframe.pack();
        plotframe.setVisible(true);
          **/
//
//
//         //System.out.println("plotframe.setVisible(true)");
//
//        //JAMSCalendar test = new JAMSCalendar();
//        //if(table.getValueAt(rows[0], columns[0]).getClass() != test.getClass()){
        int numActiveLeft=0;
        int numActiveRight=0;
        int corr=0;
        boolean typechosen_R=false;
        boolean typechosen_L=false;
        String[] colorLeft = new String[graphCount];
        String[] colorRight = new String[graphCount];
        String[] titleLeft = new String[graphCount];
        String[] titleRight = new String[graphCount];
//
//        /* zuordnung der graphen */
        for(int i=0; i<graphCount; i++){
            
            GraphProperties prop = propVector.get(i);
            
            if(prop.getPosition()== "left"){
                
                ctsplot.setTypeLeft(0);
                
                colorLeft[i - numActiveRight - corr] = prop.getColor();
                titleLeft[i - numActiveRight - corr] = prop.getLegendName();
                numActiveLeft++;
            }
            if(prop.getPosition() == "right"){
                ctsplot.setTypeRight(1);
                
                colorRight[i - numActiveLeft - corr] = prop.getColor();
                titleRight[i - numActiveLeft - corr] = prop.getLegendName();
                numActiveRight++;
            }
        }
        
        String[] legendLeft = new String[numActiveLeft];
        String[] legendRight = new String[numActiveRight];
        
        for(int n=0; n<numActiveLeft; n++){
            legendLeft[n] = titleLeft[n];
        }
        for(int n=0; n<numActiveRight; n++){
            legendRight[n] = titleRight[n];
        }
        ctsplot.setTitleLeft(legendLeft);
        ctsplot.setTitleRight(legendRight);
        
        ctsplot.setGraphCountLeft(numActiveLeft);
        ctsplot.setGraphCountRight(numActiveRight);
        
        ctsplot.setColorLeft(colorLeft);
        ctsplot.setColorRight(colorRight);
        ctsplot.setTitleLeft(titleLeft);
        ctsplot.setTitleRight(titleRight);
        /* CTSPlot erstellen */
        
        mainpanel.removeAll(); /* nullPionterEx at first startup? */
        mainpanel.add(frame, BorderLayout.NORTH);
        ctsplot.createPlot();
        mainpanel.add(ctsplot.getPanel());
        //mainpanel.add(ctsplot.getChartPanel(), BorderLayout.CENTER);
        //System.out.println("ctsplot.createPlot();");
       
        double[] valueLeft = new double[numActiveLeft];
        double[] valueRight = new double[numActiveRight];
        
        
       
                
        /* jedesmal fragen, ob der graph zu valueLEFT GEHÖRT (COMBObOX ABFRAGEN) */
        /* ACHTUNG: Funktioniert noch nicht bei addGraph() */
        int rowcount = table.getRowCount();
        int c = 0;
        
        for(int k=0;k<rowcount;k++){
                    
                    int corrLeft = 0;
                    int corrRight = 0;
                    Object value = null;

                    for(int i=0;i<graphCount;i++){
                        
                        GraphProperties prop = propVector.get(i);
                        //value = (Double) table.getValueAt(rows[k],columns[i]);
                            int rows[] = prop.getSelectedRows();
                            int col = prop.getSelectedColumn();
                            
                            if(!(k<rows[0]) && !(k>rows[rows.length-1])){

                                value = table.getValueAt(k,col);
                                if(value.getClass() != java.lang.Double.class){
                                        value = 0.0;
                                    }

                                if(prop.getPosition() == "left"){
                                        //valueLeft[i - corrLeft] = (Double) table.getValueAt(rows[k],columns[i]);
                                    valueLeft[i - corrLeft] = (Double) value;
                                    corrRight++;

                                }

                                if(prop.getPosition() == "right"){
                                    //valueRight[i - corrRight] = (Double) table.getValueAt(rows[k],columns[i]);
                                    valueRight[i - corrRight] = (Double) value;
                                    corrLeft++;
                                }
                            }else{
                                corrRight++;
                                corrLeft++;
                                corr++;
                            }
                    }                 
                ctsplot.plot((JAMSCalendar)table.getValueAt(k,0), valueLeft, valueRight);
            }

        frame.repaint();


    }
    
    
    
    
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
            addGraph();
        }
    };
    
    ActionListener plotbuttonclick = new ActionListener(){
        public void actionPerformed(ActionEvent e) {
            timePlot();
        }
    };
    
    ActionListener actChanged = new ActionListener(){
        public void actionPerformed(ActionEvent e) {
            //timePlot();
        }
    };
    
    
}
