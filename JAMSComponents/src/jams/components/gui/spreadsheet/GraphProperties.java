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
import org.unijena.jams.data.JAMSBoolean;
import org.unijena.jams.data.JAMSCalendar;
import org.unijena.jams.data.JAMSDouble;
import org.unijena.jams.data.JAMSInteger;
import org.unijena.jams.data.JAMSString;
import org.unijena.jams.data.JAMSStringArray;
import org.unijena.jams.model.JAMSGUIComponent;
import org.unijena.jams.model.JAMSVarDescription;


public class GraphProperties extends JDialog {
    
    JTable table;
    
    JScrollPane scpane;
    
    int index;
    String legendName;
    String color;
    String name;
    String position; // left/right
    int type; //renderer index
    
    boolean result = false;
    
    int selectedColumn;
    int[] rowSelection;
    
    JComboBox setColumn;
    JComboBox colorchoice;
    JComboBox typechoice;
    JComboBox poschoice;
    
    JComboBox setColumn1;
    JComboBox colorchoice1;
    JComboBox typechoice1;
    JComboBox poschoice1;
    
    JLabel nameLabel;
    JLabel colorLabel;
    JLabel typeLabel;
    JLabel posLabel;
    
    JTextField setName;
    JTextField setLegend;
    
    private String[] colors = {"red","blue","green","black","magenta","cyan","yellow","gray","orange","lightgray","pink"};
    private String[] types = {"Line","Bar","Area","Line and Base","Dot","Difference","Step","StepArea"};
    private String[] positions = {"left","right"};
    
    JPanel graphpanel = new JPanel();
    JPanel selectColPanel = new JPanel();
    JPanel datapanel = new JPanel();
    JPanel buttonpanel = new JPanel();
    
    HashMap<String, Color> colorTable = new HashMap<String, Color>();
    
    
    
    
    
    /** Creates a new instance of GraphProperties */
    public GraphProperties(JDialog parent, JTable table) {
        
        //super(parent, "Select Properties");
        //this.parent = parent;
        //setLayout(new FlowLayout());
        //Point parentloc = parent.getLocation();
        //setLocation(parentloc.x + 30, parentloc.y + 30);
        
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
        
        this.table = table;
        this.color = "red";
        this.position = "left";
        this.name = "Graph Name";
        this.legendName = this.name;
        
        this.selectedColumn = 0;
        this.rowSelection = null;
        
        //setLayout(new GridLayout(5,1));
        
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new FlowLayout());
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new FlowLayout());
        selectColPanel.setLayout(new FlowLayout());
        
        colorchoice = new JComboBox(colors);
        colorchoice.setSelectedIndex(0);
        typechoice = new JComboBox(types);
        typechoice.setSelectedIndex(0);
        poschoice = new JComboBox(positions);
        poschoice.setSelectedIndex(0);
        
        colorchoice1 = colorchoice;
        typechoice1 = typechoice;
        poschoice1 = poschoice;
                
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("CANCEL");
        JButton propButton = new JButton("...");

        //JLabel namelabel = new JLabel();
        JLabel setNameLabel =   new JLabel("        Name:");
        JLabel setColumnLabel = new JLabel("  Set Column:");
        JLabel setLegendLabel = new JLabel("Legend Entry:");
        nameLabel = new JLabel();
        colorLabel = new JLabel();
        typeLabel = new JLabel();
        posLabel = new JLabel();
        
        nameLabel.setBackground(Color.WHITE);
        colorLabel.setBackground(Color.WHITE);
        typeLabel.setBackground(Color.WHITE);
        posLabel.setBackground(Color.WHITE);

//        JTextField setName = new JTextField("Plot Name", 14);
//        JTextField setLegend = new JTextField("Legend Entry", 14);

        String[] column = new String[table.getColumnCount()];
        for(int i=0;i<table.getColumnCount();i++){
            
            column[i] = table.getColumnName(i);
        }
        
        setColumn = new JComboBox(column);
        setColumn.setSelectedIndex(1);
        
        nameLabel.setText((String) setColumn.getSelectedItem());
        
        String name = (String) setColumn.getSelectedItem();

        setName = new JTextField(name, 14);
        setLegend = new JTextField(name, 14);
        
        namePanel.add(setNameLabel);
        namePanel.add(setName);
        legendPanel.add(setLegendLabel);
        legendPanel.add(setLegend);
        
        this.selectColPanel.add(setColumn);
        
        this.datapanel.setLayout(new FlowLayout());
        
        /** Group Layout **/
        GroupLayout layout = new GroupLayout(graphpanel);
        graphpanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
        
        //vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(setColumn).addComponent(setLegendLabel).addComponent(setLegend).addComponent(setColumn));
       
        
        this.graphpanel.add(setColumn);
        this.graphpanel.add(poschoice);
        this.graphpanel.add(typechoice);
        this.graphpanel.add(colorchoice);
        
       // this.graphpanel.add(propButton);
        
//        this.datapanel.add(colorLabel);
//        this.datapanel.add(nameLabel);
//        this.datapanel.add(posLabel);
//        this.datapanel.add(typeLabel);
        
//        this.datapanel.add(setColumn);
//        this.datapanel.add(poschoice);
//        this.datapanel.add(typechoice);
//        this.datapanel.add(colorchoice);
        this.datapanel.add(propButton);
        //this.datapanel.add(nameLabel);
        
        this.buttonpanel.add(okButton);
        this.buttonpanel.add(cancelButton);
        
        okButton.addActionListener(okListener);
        propButton.addActionListener(propAction);
            
        add(graphpanel);
        add(selectColPanel);
        add(namePanel);
        add(legendPanel);
        add(buttonpanel);
        
        pack();
    }
    
    /*    
    public void setRowSelection(int[] rows){
        this.rowSelection = rowSelection;
    }
     **/
    
    public void applyProperties(){
        setSelectedColumn(setColumn.getSelectedIndex());
        int[] rows = new int[table.getRowCount()];
        
        for(int i=0;i<table.getRowCount();i++){
            rows[i] = i;
        }
        setSelectedRows(rows);
        setColor((String) colorchoice.getSelectedItem());
        setPosition((String) poschoice.getSelectedItem());
        setRendererType(typechoice.getSelectedIndex());
        setName(setName.getText());
        setLegendName(setLegend.getText());
        setVisible(false);
        this.result = true;
    }
    
    public void showPropDlg(){
        setVisible(true);
    }
    
    public boolean getResult(){
        return result;
    }
    
    public JPanel getGraphPanel(){
        return datapanel;
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
        colorLabel.setBackground(colorTable.get(color));
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
        posLabel.setText(position);
    }
    
    public void setRendererType(int type){
        this.type = type;
        typechoice.setSelectedIndex(type);
        typeLabel.setText((String) typechoice.getItemAt(type));
    }
    
    public String getColor(){
        return this.color;
    }
    
    public String getLegendName(){
        return this.legendName;
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
   
    /*** Action Listener ***/
    ActionListener okListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            applyProperties();
            setVisible(false);
        }
    };
    
    ActionListener cancelListener = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            result = false;
            setVisible(false);
        }
    };
    
    ActionListener propAction = new ActionListener(){
        public void actionPerformed(ActionEvent te){
            
            showPropDlg();
        }
    };
    
    
}
