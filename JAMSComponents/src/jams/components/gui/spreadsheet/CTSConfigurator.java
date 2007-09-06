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
    
    private JFrame parent;
    private JPanel frame;
    private JDialog plotframe;
    private JPanel optionpanel;
    private JPanel graphpanel;
    private JPanel southpanel;
    //private Vector<JPanel> datapanels = new Vector<JPanel>();
    
    private JPanel[] datapanels;
    
    private String[] headers;
    private String[] colors = {"yellow","orange","red","pink","magenta","cyan","blue","green","gray","lightgray","black"};
    private String[] types = {"line","bar","area","dot"};
    private String[] positions = {"left","right"};
    
    private int index;
    
    HashMap<String, Color> colorTable = new HashMap<String, Color>();
    
    int[] rows, columns;
    JTable table;
    
    CTSPlot ctsplot;
    
    /*buttons*/
    JButton addbutton = new JButton("add graph");
    JButton plotbutton = new JButton("plot");
    JButton deletebutton = new JButton("delete");
    
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
    
    public CTSConfigurator(JTable table){
        
        this.table = table;         
        this.rows = table.getSelectedRows();
        this.columns = table.getSelectedColumns();
        this.graphCount = columns.length;
        this.headers = new String[graphCount];/* hier aufpassen bei reselection xxx reselecton -> neue instanz */
        this.parent = parent;
        
        
        for(int k=0;k<graphCount;k++){
            headers[k] = table.getColumnName(columns[k]);
        }
        
        
        createPanel();
              
    }
    
    private void createPanel(){
        
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
        

        frame = new JPanel();
        frame.setLayout(new GridLayout(2,2));        
        
        
        //optionpanel = new JPanel();
        //optionpanel.setLayout(new GridLayout(3,1));
        graphpanel = new JPanel();
        graphpanel.setLayout(new GridLayout(headers.length,1));
        southpanel = new JPanel();
        southpanel.setLayout(new FlowLayout());
        
        
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
        
        datapanels = new JPanel[graphCount];
        activate = new JCheckBox[graphCount];
        poschoice = new JComboBox[graphCount];
        typechoice = new JComboBox[graphCount];
        colorchoice = new JComboBox[graphCount];
        
        createActionListener();
        
        for(int k=0;k<graphCount;k++){
            activate[k] = new JCheckBox(headers[k], true);
            activate[k].addActionListener(actChanged);
            poschoice[k] = new JComboBox(positions);
            typechoice[k] = new JComboBox(types);
            colorchoice[k] = new JComboBox(colors);     
        }
        
                
            
        
        addbutton.addActionListener(addbuttonclick);  
        plotbutton.addActionListener(plotbuttonclick);
        //optionpanel.add(addbutton);
        southpanel.add(deletebutton);
        southpanel.add(plotbutton);
        
        for(int i=0; i<headers.length; i++){
            
            //datapanels.get(i).add(datachoice.get(i));
            //datachoice.get(i).setSelectedItem(headers[i]);
            datapanels[i].add(activate[i]);
            datapanels[i].add(poschoice[i]);
            poschoice[i].setSelectedItem("left");
            datapanels[i].add(typechoice[i]);
            typechoice[i].setSelectedItem("line");
            datapanels[i].add(colorchoice[i]);
            colorchoice[i].setSelectedIndex(i);
            
            graphpanel.add(datapanels[i]);
        }    
    
        
        frame.add(graphpanel);
        //frame.add(optionpanel);
        frame.add(southpanel);        
        /** CTSConfigurator will be added to CTSViewer ******
        frame.pack();
        frame.setVisible(true);
         */
    }
    public JPanel getPanel(){
        return frame;
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
    public void timePlot(){
        /* very primitive version!!*/
        
        /* Festlegen welche cols zu valueLeft und welche zu valueRight gehören!! */
        
        /* CTSPlot initialisieren */
        ctsplot = new CTSPlot();
        System.out.println("CTSPlot ctsplot = new CTSPlot();");
         
         /* Parameter festlegen */

        //ctsplot.setGraphCountRight(columns.length);
         /*
        plotframe = new JDialog(); 
        plotframe.setLayout(new FlowLayout());
         System.out.println("plotframe.setLayout(new FlowLayout());");
        plotframe.add(ctsplot.getPanel());
         System.out.println("plotframe.add(ctsplot.getPanel());");
        plotframe.pack();
        plotframe.setVisible(true);
          **/


         System.out.println("plotframe.setVisible(true)");
        
        //JAMSCalendar test = new JAMSCalendar();
        //if(table.getValueAt(rows[0], columns[0]).getClass() != test.getClass()){
        int numActiveLeft=0;
        int numActiveRight=0;
        
        /* zuordnung der graphen */
        for(int i=0;i<graphCount;i++){
            
            if(activate[i].isSelected()){
                if(poschoice[i].getSelectedItem() == "left"){
                    numActiveLeft++;
                }
                if(poschoice[i].getSelectedItem() == "right"){
                    numActiveRight++;
                }
                
            }
        }
        
       ctsplot.setGraphCountLeft(numActiveLeft);
       ctsplot.setGraphCountRight(numActiveRight); 
        /* CTSPlot erstellen */
        
        ctsplot.createPlot();
        System.out.println("ctsplot.createPlot();");
        
        double[] valueLeft = new double[numActiveLeft];
        double[] valueRight = new double[numActiveRight];
        
        //Double value;
        
        /* jedesmal fragen, ob der graph zu valueLEFT GEHÖRT (COMBObOX ABFRAGEN) */
            for(int k=0;k<rows.length;k++){
                for(int i=0;i<graphCount;i++){
                    //value = (Double) table.getValueAt(rows[k],columns[i]);
                    if(activate[i].isSelected()){
                        if(poschoice[i].getSelectedItem() == "left"){
                            valueLeft[i] = (Double) table.getValueAt(rows[k],columns[i]);
                            System.out.println(table.getValueAt(rows[k],columns[i]));
                        }
                        if(poschoice[i].getSelectedItem() == "right"){
                            valueRight[i] = (Double) table.getValueAt(rows[k],columns[i]);
                        }
                    }
                }
                /*
                for(int j=0;j<graphCount;j++){
                    valueRight[j] = (Double) table.getValueAt(rows[k],columns[j]);
                }
                */
                
                ctsplot.plot((JAMSCalendar)table.getValueAt(rows[k],0), valueLeft, valueRight);
            }
        
    }
    
    
    /****** EVENT HANDLING ******/
    ActionListener addbuttonclick = new ActionListener(){
         public void actionPerformed(ActionEvent e) {
                
            } 
    };
    
    ActionListener plotbuttonclick = new ActionListener(){
         public void actionPerformed(ActionEvent e) {
             System.out.println("actionPerformed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                timePlot();
            } 
    };
    
   ActionListener actChanged = new ActionListener(){
         public void actionPerformed(ActionEvent e) {
                timePlot();
            } 
    };
    
    
   public void createActionListener(){ 
       
        activationChange = new ActionListener[graphCount];
        
        for(int k=0;k<graphCount;k++){
            /* reicht hier ein listener für alle boxes? scheint so... */
           activationChange[k] = new ActionListener(){
                 public void actionPerformed(ActionEvent e) {
                        timePlot();

                    } 
            };
   
        
        }
   
   
   }
}
