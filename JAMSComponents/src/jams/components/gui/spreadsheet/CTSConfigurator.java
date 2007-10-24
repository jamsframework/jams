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
    private JScrollPane optionScPane;
    private JScrollPane mainScPane;
    
    private String[] headers;
    
    private JLabel edTitle = new JLabel("Plot Title: ");
    private JLabel edLeft  = new JLabel("Left axis title: ");
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
        
        JLabel nameLabel = new JLabel("Name");
        JLabel posLabel = new JLabel("Position");
        JLabel typeLabel = new JLabel("Renderer");
        JLabel colorLabel = new JLabel("Colour");
        JLabel dataLabel = new JLabel("Select Data");
        JLabel timeLabel = new JLabel("Time Interval");
        JLabel emptyTimeLabel = new JLabel("    ");
        JLabel legendLabel = new JLabel("Legend Entry");
      
        nameLabel.setBackground(Color.DARK_GRAY);
        posLabel.setBackground(Color.DARK_GRAY);
        typeLabel.setBackground(Color.DARK_GRAY);
        colorLabel.setBackground(Color.DARK_GRAY);
        dataLabel.setBackground(Color.DARK_GRAY);
        timeLabel.setBackground(Color.DARK_GRAY);
        
        mainpanel = new JPanel();
        mainpanel.setLayout(new BorderLayout());
        
        plotpanel = new JPanel();
        plotpanel.setLayout(new BorderLayout());
        
        frame = new JPanel();
        frame.setLayout(new BorderLayout());
        
        graphScPane = new JScrollPane();
                
        optionpanel = new JPanel();
        optionpanel.setLayout(new FlowLayout());
        graphpanel = new JPanel();
        
        //graphpanel.setLayout(new GridLayout(graphCount,1));
        
        GroupLayout gLayout = new GroupLayout(graphpanel);
        graphpanel.setLayout(gLayout);
        gLayout.setAutoCreateGaps(true);
        gLayout.setAutoCreateContainerGaps(true);
        GroupLayout.SequentialGroup hGroup = gLayout.createSequentialGroup();
        GroupLayout.SequentialGroup vGroup = gLayout.createSequentialGroup();
        Group group1 = gLayout.createParallelGroup();
        Group group2 = gLayout.createParallelGroup();
        Group group3 = gLayout.createParallelGroup();
        Group group4 = gLayout.createParallelGroup();
        Group group5 = gLayout.createParallelGroup();
        Group group6 = gLayout.createParallelGroup();
        Group group7 = gLayout.createParallelGroup();
        Group group8 = gLayout.createParallelGroup();
        Group group9 = gLayout.createParallelGroup();
        Group group10 = gLayout.createParallelGroup();
        Group group11 = gLayout.createParallelGroup();
        Group group12 = gLayout.createParallelGroup();
        Group group13 = gLayout.createParallelGroup();
        Group group14 = gLayout.createParallelGroup();
        Group group15 = gLayout.createParallelGroup();
        
        southpanel = new JPanel();
        southpanel.setLayout(new FlowLayout());
        
        edTitlePanel = new JPanel();
        edTitlePanel.setLayout(new FlowLayout());
        edLeftAxisPanel = new JPanel();
        edLeftAxisPanel.setLayout(new FlowLayout());
        edRightAxisPanel = new JPanel();
        edRightAxisPanel.setLayout(new FlowLayout());
        
        
        
        
        /*
        for(int k=0;k<headers.length;k++){
         
           datapanels.add(new JPanel());
           datapanels.get(k).setLayout(new FlowLayout());
         
           activate.add(new JCheckBox(headers[k],true));
           //datachoice.add(new JComboBox(headers));
           poschoice.add(new JComboBox(positions));
           typechoice.add(new JComboBox(types));
           colorchoice.add(new JComboBox(colors));
        }
         */
        
//        datapanels = new JPanel[graphCount];
//        activate = new JCheckBox[graphCount];
//        poschoice = new JComboBox[graphCount];
//        typechoice = new JComboBox[graphCount];
//        colorchoice = new JComboBox[graphCount];
        
        //createActionListener();
        
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
        
        optionpanel.add(edTitle);
        optionpanel.add(edTitleField);
        optionpanel.add(edLeft);
        optionpanel.add(edLeftField);
        optionpanel.add(edRight);
        optionpanel.add(edRightField);
        
        group1.addComponent(nameLabel);
        group2.addComponent(posLabel);
        group3.addComponent(typeLabel);
        group4.addComponent(colorLabel);
        group5.addComponent(legendLabel);
        group6.addComponent(dataLabel);
        group7.addComponent(timeLabel);
        group8.addComponent(emptyTimeLabel);
//        group9.addComponent();
//        group10.addComponent(emptyTimeLabel);
//        group11.addComponent(emptyTimeLabel);
//        group12.addComponent(emptyTimeLabel);
//        group13.addComponent(emptyTimeLabel);
//        group14.addComponent(emptyTimeLabel);
//        group15.addComponent(emptyTimeLabel);
        
        
        vGroup.addGroup(gLayout.createParallelGroup(Alignment.BASELINE)
        .addComponent(nameLabel).addComponent(posLabel).addComponent(typeLabel)
        .addComponent(colorLabel).addComponent(legendLabel).addComponent(dataLabel)
        .addComponent(timeLabel).addComponent(emptyTimeLabel));
        
        for(int k=1;k<=graphCount;k++){
            
            GraphProperties prop = new GraphProperties(parent, table);
            //propVector.add(new GraphProperties(parent,table));
            
            prop.setSelectedColumn(columns[k-1]);
            prop.setSelectedRows(rows);
            prop.setTimeSTART(rows[0]);
            prop.setTimeEND(rows[rows.length - 1]);
            if( k<=12){
                prop.setColor(k-1);
            }
//            prop.setColor((String) colorchoice.getSelectedItem());
//            prop.setPosition((String) poschoice.getSelectedItem());
//            prop.setRendererType(typechoice.getSelectedIndex());
            prop.setName(table.getColumnName(k));
            prop.setLegendName(table.getColumnName(k));
            
            prop.getPlotButton().addActionListener(plotbuttonclick);
            
            JLabel space1 = new JLabel(" ");
            JLabel space2 = new JLabel(" ");
            
            group1.addComponent(prop.getNameLabel());
            group2.addComponent(prop.getPosChoice());
            group3.addComponent(prop.getTypeChoice());
            group4.addComponent(prop.getColorChoice());
            group5.addComponent(prop.getLegendField());
            group6.addComponent(prop.getDataChoice());
            group7.addComponent(prop.getTimeChoiceSTART());
            group8.addComponent(prop.getTimeChoiceEND());
            group9.addComponent(space1);
            group10.addComponent(prop.getPlotButton());
            group11.addComponent(space2);
            group12.addComponent(prop.getAddButton());
            group13.addComponent(prop.getRemButton());
            group14.addComponent(prop.getUpButton());
            group15.addComponent(prop.getDownButton());
            
            
            vGroup.addGroup(gLayout.createParallelGroup(Alignment.BASELINE)
            .addComponent(prop.getNameLabel()).addComponent(prop.getPosChoice())
            .addComponent(prop.getTypeChoice()).addComponent(prop.getColorChoice())
            .addComponent(prop.getDataChoice()).addComponent(prop.getLegendField())
            .addComponent(prop.getTimeChoiceSTART()).addComponent(prop.getTimeChoiceEND())
            .addComponent(space1).addComponent(prop.getPlotButton())
            .addComponent(space2).addComponent(prop.getAddButton())
            .addComponent(prop.getRemButton())
            .addComponent(prop.getUpButton()).addComponent(prop.getDownButton()));
            
            propVector.add(prop);
            //graphpanel.add(propVector.get(k-1).getGraphPanel());
            
        }
        hGroup.addGroup(group1);
        hGroup.addGroup(group2);
        hGroup.addGroup(group3);
        hGroup.addGroup(group4);
        hGroup.addGroup(group5);
        hGroup.addGroup(group6);
        hGroup.addGroup(group7);
        hGroup.addGroup(group8);
        hGroup.addGroup(group9);
        hGroup.addGroup(group10);
        hGroup.addGroup(group11);
        hGroup.addGroup(group12);
        hGroup.addGroup(group13);
        hGroup.addGroup(group14);
        hGroup.addGroup(group15);
        
        gLayout.setHorizontalGroup(hGroup);
        gLayout.setVerticalGroup(vGroup);

        addbutton.addActionListener(addbuttonclick);
        plotbutton.addActionListener(plotbuttonclick);
        propbutton.addActionListener(propbuttonclick);

        frame.add(graphpanel, BorderLayout.NORTH);
        //frame.add(optionpanel);
        frame.add(optionpanel, BorderLayout.CENTER);
 
        optionpanel.setBorder(new EtchedBorder());

        mainpanel.add(frame, BorderLayout.NORTH);
        
        mainScPane = new JScrollPane(mainpanel);
        mainScPane.createVerticalScrollBar();
        mainScPane.createHorizontalScrollBar();
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
        
        int row_start = 0;
        int row_end = 0;
        
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
            int s = prop.getTimeSTART();
            int e = prop.getTimeEND();
            
            if(s > row_start){
                row_start = s;
            }
            if(e > row_end){
                row_end = e;
            }
            
            if(prop.getPosition()== "left"){
                
                ctsplot.setTypeLeft(prop.getRendererType());
                
                colorLeft[i - numActiveRight - corr] = prop.getColor();
                titleLeft[i - numActiveRight - corr] = prop.getLegendName();
                numActiveLeft++;
            }
            if(prop.getPosition() == "right"){
                ctsplot.setTypeRight(prop.getRendererType());
                
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
        mainpanel.add(ctsplot.getPanel(), BorderLayout.CENTER);
        //mainpanel.add(ctsplot.getChartPanel(), BorderLayout.CENTER);
        //System.out.println("ctsplot.createPlot();");
       
        double[] valueLeft = new double[numActiveLeft];
        double[] valueRight = new double[numActiveRight];
        
        
       
                
        /* jedesmal fragen, ob der graph zu valueLEFT GEHÖRT (COMBObOX ABFRAGEN) */
        /* ACHTUNG: Funktioniert noch nicht bei addGraph() */
        //int rowcount = table.getRowCount();
        int c = 0;
        
        for(int k=row_start; k<=row_end; k++){
                    
                    int corrLeft = 0;
                    int corrRight = 0;
                    Object value = null;

                    for(int i=0;i<graphCount;i++){
                        
                        GraphProperties prop = propVector.get(i);
                        //value = (Double) table.getValueAt(rows[k],columns[i]);
                            //int rows[] = prop.getSelectedRows();
                            int col = prop.getSelectedColumn();
                            int s = prop.getTimeSTART();
                            int e = prop.getTimeEND();
                            
                            if(!(k<s) && !(k>e)){

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
    
    
    public void createActionListener(){
        
        activationChange = new ActionListener[graphCount];
        
        for(int k=0;k<graphCount;k++){
            /* reicht hier ein listener für alle boxes? scheint so... */
            activationChange[k] = new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    //timePlot();
                    
                }
            };
            
            
        }
        
        
    }
}
