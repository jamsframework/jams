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
public class JTSConfigurator extends JFrame{

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
    
    private Vector<ActionListener> addAction = new Vector<ActionListener>();    
        
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
    private JLabel timeFormatLabel = new JLabel("Time format");
    
    private JTextField edTitleField = new JTextField(14);
    private JTextField edLeftField = new JTextField(14);
    private JTextField edRightField = new JTextField(14);
    private JTextField edXAxisField = new JTextField(14);
    
    private String[] types = {"Line","Bar","Area","Line and Base","Dot","Step","StepArea","Difference"};
    
    private JComboBox rLeftBox = new JComboBox(types);
    private JComboBox rRightBox = new JComboBox(types);
    
    private JCheckBox invLeftBox = new JCheckBox("Invert left Axis");
    private JCheckBox invRightBox = new JCheckBox("Invert right Axis");
    
    private JCheckBox timeFormat_yy = new JCheckBox("yy");
    private JCheckBox timeFormat_mm = new JCheckBox("mm");
    private JCheckBox timeFormat_dd = new JCheckBox("dd");
    private JCheckBox timeFormat_hm = new JCheckBox("hh:mm");
    
    private JButton applyButton = new JButton("Apply");
    private JButton addButton = new JButton("Add Graph");
    
    private Vector<GraphProperties> propVector = new Vector<GraphProperties>();
    private JAMSTimePlot jts = new JAMSTimePlot();
    
//    private String[] headers;
//    //private String[] colors = {"yellow","orange","red","pink","magenta","cyan","blue","green","gray","lightgray","black"};
    //private String[] colors = {"red","blue","green","black","magenta","cyan","yellow","gray","orange","lightgray","pink"};
//    private String[] types = {"Line","Bar","Area","Line and Base","Dot","Difference","Step","StepArea"};
//    private String[] positions = {"left","right"};
    
    /* test*/
//    private Color[] colors_ = {Color.RED, Color.BLUE};
    
//    private String[] legendEntries;
    
    private int index;
    private int colour_cnt;
    
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
    public JTSConfigurator() {
        /* open CTSConf */
    }
    /*
    public CTSConfigurator(JAMSTableModel tmodel){
        this.tmodel = tmodel;
    }
     **/
    
    public JTSConfigurator(JFrame parent, JTable table){
        
        //super(parent, "JAMS JTS Viewer");
        this.parent = parent;
        setTitle("JAMS JTS Viewer");
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
        JLabel timeLabel = new JLabel("Time Interval");
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
        
        edXAxisField.setText("Time");
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
        
        for(int k=0;k<graphCount;k++){
            
            GraphProperties prop = new GraphProperties(parent, table, this);
            //propVector.add(new GraphProperties(parent,table));
            
            prop.setIndex(k);
            
            prop.setSelectedColumn(columns[k]);
            prop.setSelectedRows(rows);
            prop.setTimeSTART(rows[0]);
            prop.setTimeEND(rows[rows.length - 1]);
            
            
            prop.setColor(k%11);
            colour_cnt = k;
            
//            prop.setColor((String) colorchoice.getSelectedItem());
//            prop.setPosition((String) poschoice.getSelectedItem());
//            prop.setRendererType(typechoice.getSelectedIndex());
            prop.setName(table.getColumnName(k+1));
            prop.setLegendName(table.getColumnName(k+1));
            
            //prop.getPlotButton().addActionListener(plotbuttonclick);
            
            prop.applyTSProperties();
            addPropGroup(prop);
      
            propVector.add(k,prop);
            
            //graphpanel.add(propVector.get(k-1).getGraphPanel());
            
        }
        
        finishGroupUI();
        createOptionPanel();
    
        /* initialise JTSPlot */
        //JAMSTimePlot jts = new JAMSTimePlot(propVector);
        jts.setPropVector(propVector);
        jts.createPlot();
 
        JPanel graphPanel = new JPanel();
        JPanel optPanel = new JPanel();
        graphPanel.add(graphpanel);
        optPanel.add(optionpanel);
        
        graphScPane = new JScrollPane(graphPanel);
        //graphScPane.setPreferredSize(new Dimension(200,150));
        graphScPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        optionpanel.setBorder(new EtchedBorder());
        plotScPane = new JScrollPane(jts.getPanel());
        optScPane = new JScrollPane(optPanel);
        split_hor.add(optScPane, 0);
        split_hor.add(graphScPane, 1);
        split_vert.add(split_hor, 0);
        split_vert.add(plotScPane, 1);
        add(split_vert);
//        add(frame, BorderLayout.NORTH);
//        add(plotScPane, BorderLayout.CENTER);
        
        //jts.plotLeft(0, "leftAxisName", "Time", false);
        jts.setDateFormat(timeFormat_yy.isSelected(), timeFormat_mm.isSelected(),
                                timeFormat_dd.isSelected(), timeFormat_hm.isSelected());
        plotAllGraphs();
        //jts.plotRight(1, "rightAxisName", true);
    
    }
    
    public void addGraph(GraphProperties prop){
        
        int i = propVector.indexOf(prop);
        int t_s, t_e;
        GraphProperties newProp = new GraphProperties(parent, table, this);
        colour_cnt++;
        
        AddGraphDlg dlg = new AddGraphDlg();
        dlg.setVisible(true);
        
        if(dlg.getOK()){
            newProp.setPosition(dlg.getSide());
            i = dlg.getPosition();
            dlg.dispose();
        }
        
        newProp.setColor(colour_cnt % 11);
        
        if(i>=0){
            t_s = prop.getTimeChoiceSTART().getSelectedIndex();
            t_e = prop.getTimeChoiceEND().getSelectedIndex();
            newProp.getTimeChoiceSTART().setSelectedIndex(t_s);
            newProp.getTimeChoiceEND().setSelectedIndex(t_e);
        }
        propVector.add(i,newProp);
        
        graphCount = propVector.size();
        
        //Renderer Box Handler
        handleRenderer();
//        if(graphCount<2 && rLeftBox.getItemCount()==8){
//            rLeftBox.removeItemAt(7);
//            rRightBox.removeItemAt(7);
//            
//        }
//        if(graphCount>=2 && rLeftBox.getItemCount()==7){
//            rLeftBox.addItem("Difference");
//            rRightBox.addItem("Difference");  
//        }
        
        initGroupUI();
        
        for(int k=0;k<graphCount;k++){
            
            newProp = propVector.get(k);
            newProp.setIndex(k);
            //prop.getPlotButton().addActionListener(plotbuttonclick);
            
            addPropGroup(newProp);
            
            
            
            //graphpanel.add(propVector.get(k-1).getGraphPanel());
            
        }
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
        
        handleRenderer();
        
        initGroupUI();
        
        for(int k=0;k<graphCount;k++){
            
            newProp = propVector.get(k);
            //newProp.setIndex(k);
            //prop.getPlotButton().addActionListener(plotbuttonclick);
            
            addPropGroup(newProp);
            
            
            
            //graphpanel.add(propVector.get(k-1).getGraphPanel());
            
        }
        finishGroupUI();
        //mainpanel.updateUI();
//        pack();
        repaint();
        }
    }
    
    public void upGraph(GraphProperties prop){
        
        int i = propVector.indexOf(prop);
        //GraphProperties prop = propVector.get(i);
        
        if(i-1>=0 && i-1<graphCount){
            propVector.remove(prop);
            propVector.add(i-1, prop);
        

            initGroupUI();

            for(int k=0;k<graphCount;k++){

                prop = propVector.get(k);
                //prop.setIndex(k);
                //prop.getPlotButton().addActionListener(plotbuttonclick);

                addPropGroup(prop);



                //graphpanel.add(propVector.get(k-1).getGraphPanel());

            }
            finishGroupUI();
            
            
            repaint();
        }
    }
    
    public void downGraph(GraphProperties prop){
        
        int i = propVector.indexOf(prop);
        
        if(i<propVector.size()){
        GraphProperties newProp = propVector.get(i+1);
        
        if(i+1>=0 && i+1<graphCount){
            
            propVector.remove(i+1);
            propVector.add(i, newProp);

            graphCount = propVector.size();

            initGroupUI();

            for(int k=0;k<graphCount;k++){

                newProp = propVector.get(k);
                newProp.setIndex(k);
                //prop.getPlotButton().addActionListener(plotbuttonclick);

                addPropGroup(newProp);
                
            }
            finishGroupUI();
            
            
            repaint();
        }
        }
    }
    
    private void updatePropVector(){
        
        for(int i=0; i<propVector.size(); i++){
            propVector.get(i).applyTSProperties();
        }
    }
    
//    public void plotGraph(int i){
//       
//            //propVector.get(i).applyProperties();
//            if(propVector.get(i).getPosChoice().getSelectedItem() == "left"){
//                jts.plotLeft(rLeftBox.getSelectedIndex(), edLeftField.getText(), edXAxisField.getText(), invLeftBox.isSelected());
//            }
//            if(propVector.get(i).getPosChoice().getSelectedItem() == "right"){
//                jts.plotRight(rRightBox.getSelectedIndex(), edRightField.getText(), edXAxisField.getText(), invRightBox.isSelected());
//            }
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
                jts.plotLeft(rLeftBox.getSelectedIndex(), edLeftField.getText(), edXAxisField.getText(), invLeftBox.isSelected());
            }
            if(prop.getPosChoice().getSelectedItem() == "right"){
                jts.plotRight(rRightBox.getSelectedIndex(), edRightField.getText(), edXAxisField.getText(), invRightBox.isSelected());
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
                jts.plotLeft(rLeftBox.getSelectedIndex(), edLeftField.getText(), edXAxisField.getText(), invLeftBox.isSelected());
            }
            if(r>0){
                jts.plotRight(rRightBox.getSelectedIndex(), edRightField.getText(), edXAxisField.getText(), invRightBox.isSelected()); 
            }
            
            jts.setTitle(edTitleField.getText());
            jts.setDateFormat(timeFormat_yy.isSelected(), timeFormat_mm.isSelected(),
                                timeFormat_dd.isSelected(), timeFormat_hm.isSelected());
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
        
        if((l<2 || l>2) && rLeftBox.getItemCount()==8){
            rLeftBox.removeItemAt(7);
        }
        
        if((r<2 || r>2) && rRightBox.getItemCount()==8){
            rRightBox.removeItemAt(7);
        }
 
        if((l == 2) && rLeftBox.getItemCount()==7){
            rLeftBox.addItem("Difference");
        }     
        
        if((r == 2) && rRightBox.getItemCount()==7){
            rRightBox.addItem("Difference");
        } 
    }
    
    private void createOptionPanel(){
        GroupLayout optLayout = new GroupLayout(optionpanel);
        JPanel timeFormatPanel = new JPanel();
        timeFormatPanel.add(timeFormat_dd);
        timeFormatPanel.add(timeFormat_mm);
        timeFormatPanel.add(timeFormat_yy);
        timeFormatPanel.add(timeFormat_hm);
        
        timeFormat_yy.setSelected(true);
        timeFormat_mm.setSelected(true);
        timeFormat_dd.setSelected(true);
        timeFormat_hm.setSelected(false);
        
        addButton.addActionListener(addbuttonclick);
        
        optionpanel.setLayout(optLayout);
        optLayout.setAutoCreateGaps(true);
        optLayout.setAutoCreateContainerGaps(true);
        
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
        .addComponent(invRightBox));
        optVGroup.addGroup(optLayout.createParallelGroup().addComponent(timeFormatLabel));
        optVGroup.addGroup(optLayout.createParallelGroup().addComponent(timeFormatPanel).addComponent(applyButton));
        
        
        optHGroup.addGroup(optLayout.createParallelGroup().
                addComponent(edTitle).addComponent(edLeft).addComponent(edRight)
                .addComponent(edXAxis).addComponent(rLeftLabel).addComponent(rRightLabel)
                .addComponent(invLeftBox).addComponent(invRightBox).addComponent(timeFormatLabel)
                .addComponent(timeFormatPanel));
        
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
        JLabel timeLabel = new JLabel("Time Interval");
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
            JLabel space1 = new JLabel(" ");
            JLabel space2 = new JLabel(" ");
            JLabel space3 = new JLabel(" ");
            JLabel space4 = new JLabel(" ");
            JLabel space5 = new JLabel("   ");
            JLabel space6 = new JLabel("   ");
            JTextField lf = prop.getLegendField();
            
            group6.addComponent(space5).addComponent(space6);
            
            group1.addComponent(prop.getDataChoice()).addComponent(lf).addGap(20);
            group2.addComponent(prop.getTimeChoiceSTART()).addComponent(prop.getTimeChoiceEND());
            group3.addComponent(prop.getColorChoice()).addComponent(prop.getPosChoice());
                       

            group9.addComponent(space3);
            
            group11.addComponent(space4);
            
            group13.addComponent(prop.getRemButton());
            group14.addComponent(prop.getUpButton());
            group15.addComponent(prop.getDownButton());
                        
            vGroup.addGroup(gLayout.createParallelGroup(Alignment.LEADING)
            .addComponent(prop.getDataChoice()).addComponent(prop.getTimeChoiceSTART()).addComponent(space5)
            .addComponent(prop.getColorChoice()));
            vGroup.addGroup(gLayout.createParallelGroup(Alignment.TRAILING)
            .addComponent(lf).addComponent(prop.getTimeChoiceEND()).addComponent(space6)
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

    public void setIndex(int index){
        this.index = index;
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
    
    ActionListener timeListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            jts.setDateFormat(timeFormat_yy.isSelected(), timeFormat_mm.isSelected(),
                                timeFormat_dd.isSelected(), timeFormat_hm.isSelected());
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
            Point parentloc = parent.getLocation();
            setLocation(parentloc.x + 50, parentloc.y + 50);
            createPanel();
        }
        
        void createPanel(){
            setLayout(new FlowLayout());
            
            max = propVector.size();
            String[] posArray = {"left","right"};
            posSpinner = new JSpinner(new SpinnerNumberModel(max,1,max,1));
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





