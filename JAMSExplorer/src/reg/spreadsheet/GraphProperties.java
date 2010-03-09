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
import jams.gui.tools.GUIHelper;
import java.util.Vector;


public class GraphProperties {

    JTable table;
    
    TimeSeries ts;
    //TimeSeriesCollection dataset;
    XYSeries xys;

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

    String[] columns;

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }
    String[] timeIntervals;

    int stroke_type;
    int shape_type;
    int size_type;
    int outline_type;

    int timeSTART, timeEND;

    double data_range_start;
    double data_range_end;
    
    int plotType;
    
    JTSConfigurator ctsconf;
    JXYConfigurator cxyconf;
    STPConfigurator stpconf;
    
    //XYPair[] data;
    double[] x_dataIntervals;
    double[] y_data;
    
//    private String[] colors = {"red","blue","green","black","magenta","cyan","yellow","gray","orange","lightgray","pink"};
//    private String[] types = {"Line","Bar","Area","Line and Base","Dot","Step","StepArea","Difference"};
//    private String[] positions = {"left","right"};
    
    //Variables for renderer options
    Stroke series_stroke;
    Shape series_shape;
    Color series_paint;
    Stroke series_outline_stroke;
    Color series_outline_paint;
    Color series_fill_paint;
    
    boolean outlineVisible;
    boolean linesVisible;
    boolean shapesVisible;
    
    HashMap<String, Color> colorTable = new HashMap<String, Color>();

    String headers[];
    int columnCount = 0;
    int rowCount = 0;

    /** Creates a new instance of GraphProperties */
    public GraphProperties(JTSConfigurator ctsconf) {
        
        this.plotType = 0;
        
        this.ctsconf = ctsconf;
        
        this.table = ctsconf.table;
        this.position = "left";
        this.name = "Graph Name";

        this.selectedColumn = 0;
        this.rowSelection = null;
        
        columnCount = table.getColumnCount();
        rowCount = table.getRowCount();
        
        timeIntervals = new String[table.getRowCount()];
        for(int i=0; i<table.getRowCount(); i++){
            timeIntervals[i] = table.getValueAt(i,0).toString();
        }

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

        createProperties();
        //applyTSProperties();
        
    }
    
    public GraphProperties(JXYConfigurator cxyconf) {

        this.plotType = 1;
        this.cxyconf = cxyconf;

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

        createProperties();
    }
    
    public GraphProperties(STPConfigurator stpconf) {

        this.plotType = 2;

        this.stpconf = stpconf;

        this.position = "left";
        this.name = "Graph Name";
        this.legendName = this.name;
        
        this.selectedColumn = 0;
        this.rowSelection = null;
    
        columnCount = stpconf.getColumnCount();
        rowCount = stpconf.getRowCount();

        timeIntervals = new String[rowCount];
        for(int i=0; i<rowCount; i++){
            timeIntervals[i] = stpconf.timeVector.get(i).toString();
        }
 
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

        createProperties();
        //applyTSProperties();
        
    }
    
    public GraphProperties(JAMSSpreadSheet sheet, STPConfigurator stpconf) {

        this.plotType = 0;
        
        this.table = sheet.table;
        this.position = "left";
        this.name = "Graph Name";
        this.legendName = this.name;
        
        this.selectedColumn = 0;
        this.rowSelection = null;
        
        columnCount = table.getColumnCount();
        rowCount = table.getRowCount();
        
        timeIntervals = new String[table.getRowCount()];
        for(int i=0; i<table.getRowCount(); i++){
            timeIntervals[i] = table.getValueAt(i,0).toString();
        }

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
        
        createProperties();
    }
    
    public void createProperties(){
        //JPanel namePanel = new JPanel();
        //namePanel.setLayout(new FlowLayout());
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new FlowLayout());
 
        columns = new String[columnCount];
        for(int i=0;i<columnCount;i++){ 
            
             switch(plotType){
                 case 0:
                    if(i!=0){
                        columns[i] = table.getColumnName(i);
                    }else{
                        columns[i] = "---";
                    } break;
                  
                 case 1:    
                    columns[i] = table.getColumnName(i);
                    break;
                    
                 case 2:
                    columns = stpconf.getHeaders();
                    break;
                    
             }
        }
    }
    
    public void setSelectedColumn(int column){
        this.selectedColumn = column;
    }
    
    public void applyTSProperties(){
        JAMSCalendar time;
        double value;

        try{
            ts = new TimeSeries(getLegendName(), Second.class);
        }catch(IllegalArgumentException illex){
            ts = new TimeSeries("timeseries", Second.class);
        }
//        System.out.println("applyTS col: "+selectedColumn);

        for(int i=timeSTART; i<=timeEND; i++){
            
            time =  (JAMSCalendar) table.getValueAt(i,0); //ONLY FOR TIME SERIES TABLE WITH TIME IN COL 0!!!
//            if(!setColumn.getSelectedItem().equals("---")){
            if(selectedColumn>0){
                value = (Double) table.getValueAt(i, selectedColumn);

            ts.add(new Second(new Date(time.getTimeInMillis())), value);
//          }
            }
        }
//        cr_dlg.updateColors();
    }

    public void applyXYProperties(){

        xys = new XYSeries(getLegendName());
       
        for(int i=this.d_range[0]; i<=this.d_range[1]; i++){
            xys.add(cxyconf.sorted_Row[i].col[x_series_col], cxyconf.sorted_Row[i].col[selectedColumn]);
            
        }
        
//        cr_dlg.updateColors();
    }
    
    public void applySTPProperties(Vector<double[]> rowVector, Vector<JAMSCalendar> timeVector){
        JAMSCalendar time;
        double value;

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

    public int getPlotType(){
        return this.plotType;
    }

    public String[] getTimeIntervals(){
        return timeIntervals;
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

    
    public void setPossibleTimeIntervals(){
        int s = timeSTART;
        int e = timeEND;
        
        if(s >= e){
            timeEND = s;
        }    
    }
    
    public boolean isXSeries(){
        return is_x_series;
    }
    
    public boolean getResult(){
        return result;
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
    
    public void setSelectedRows(int[] rows){
        this.rowSelection = rows;
    }

    public void setLegendName(String legendName){
        this.legendName = legendName;
    }

    public void setName(String name){
        this.name = name;

    }
    
    public void setPosition(String position){
        this.position = position;
    }
    
    public void setRendererType(int type){
        this.type = type;
    }
    
    public void setTimeSTART(int index){
        timeSTART = index;
    }
    
    public void setTimeEND(int index){
        timeEND = index;
    }
    
    public void setDataSTART(double d_start){
        data_range_start = (double) d_start;
        
    }
    
    public void setDataEND(double d_end){
        data_range_end =  (double)d_end;
       
    }
    
    public int getColor(){
        return this.color;
    }
    
    public String getLegendName(){

        return this.legendName;
    }
    
    public String getName(){

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
        return timeSTART;
    }

    public int getTimeEND(){
        return timeEND;
//        return 5000;
    }

    public double getDataSTART(){
        
        return data_range_start;
    }
    
    public double getDataEND(){
        
        return data_range_end;
    }

//    public boolean axisInverted(){
//        return axisInverted;
//    }
    
    
    //Methods for renderer configuration
    public void setSeriesStroke(Stroke stroke){
        this.series_stroke = stroke;
    }
       
    public void setSeriesShape(Shape shape){
        this.series_shape = shape;
    }
    
    public void setSeriesPaint(Color paint){
        this.series_paint = paint;

    }
    
    public void setSeriesFillPaint(Color fill){
        this.series_fill_paint = fill;

    }
    
    public void setSeriesOutlinePaint(Color out){
        this.series_outline_paint = out;

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
            case 0: width = 0.2f; break;
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
            case 1: width = 0.3f; break;
            case 2: width = 0.7f; break;
            case 3: width = 1.5f; break;
            case 4: width = 2.0f; break;
            case 5: width = 3.0f; break;
            case 6: width = 4.0f; break;
            case 7: width = 5.0f; break;
            case 8: width = 6.0f; break;
            case 9: width = 7.0f; break;
            case 10: width = 8.0f; break;
            case 11: width = 10.0f; break;
            case 12: width = 12.0f; break;
            default: width = 0.0f; break;
        }

        if(width == 0.0f) setOutlineVisible(false);
        else setOutlineVisible(true);

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

}
    
   
    
 

