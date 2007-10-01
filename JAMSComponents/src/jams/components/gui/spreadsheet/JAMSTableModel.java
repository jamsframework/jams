/*
 * JAMSTableModel.java
 *
 * Created on 27. März 2007, 15:17
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package jams.components.gui.spreadsheet;

/**
 *
 * @author Robert Riedel
 */
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import java.io.*;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

//import jams.components.*;

public class JAMSTableModel extends AbstractTableModel implements Serializable {
    

    /* JAMS works with double[]-Arrays as row data of input/output data */
    private Vector<double[]> arrayVector = new Vector<double[]>();
    private Vector<String> timeAxis = new Vector<String>();
    private Vector<JAMSCalendar> timeVector = new Vector<JAMSCalendar>();
    private int rows=0;
    private int columns=0;
    
    private String[] colnames;
    private boolean colnamesset = false;
    private boolean timeRuns = false;
    

    
    
    
    /** Creates a new instance of JAMSTableModel */
    
    public JAMSTableModel() {
    }
    
    /* Konstruktor noch nicht auf time umgestellt!! */
    public JAMSTableModel(Vector<double[]> arrayData){
        this.arrayVector = arrayData;
        //worksWithArray = true;
        //int columns=0;
        int columnscount=0;
        
        if(!this.arrayVector.isEmpty()){
            for(int i=0; i < arrayVector.size(); i++){
                if(columnscount < arrayVector.get(i).length){
                   columnscount = arrayVector.get(i).length;
                }
            }
        }
        this.columns=columnscount;
        this.colnames = new String[columns];
    }
    
    public JAMSTableModel getTableModel(){
        return this;
    }
    /*
    public JAMSTableModel(int columns){
        this.columns=columns;
        data.ensureCapacity(columns);
        worksWithArray = false;
        
        
        for(int i=0; i<columns; i++){
            Vector column = new Vector();
            column.ensureCapacity(this.rows);
            data.addElement(column);
        }
    }
     **/
    /*
    public JAMSTableModel(Vector<Vector> inputdata){
        this.data = inputdata;
        this.columns = inputdata.size();
        worksWithArray = false;
    }
    */
    
    
    /*
     *expecting inputdata is a Vector of Vectors
     *
    private void rowCounter(){
       int rows=0;
       int size=0;
       Vector sub1 = (Vector)data.get(0);
       rows=sub1.size();
       for(int i=0;i<columns;i++){

           sub1 = (Vector)data.get(i);
           size = sub1.size();
           
           if(size > rows){
               rows = size;
           }
        }
       this.rows=rows;
    }
    */
    /* Interface implementation and overrided Methods 
     ********************************************************************/
    
    public int getColumnCount(){
        return columns;
    }
    
    public int getRowCount(){
       
        return arrayVector.size();
        
    }
    
    public String getColumnName(int index){
        String name;

        if(colnamesset == true){
            if(index < colnames.length){
                name = colnames[index];
            }
            else{
                name = "COL"+index;
            }
        }
        else {
            name = "COL"+index; //TODO: Column Names können gesetzt werden
        }
        return name;
    }
    /*
    public Class getColumnClass(int index){
        return String.class;
    }
    */
    public boolean isCellEditable(int rowIndex, int columnIndex){
        return rowIndex < rows && columnIndex < columns;
    }
    
    public Object getValueAt(int rowIndex, int columnIndex){
       //Vector rowvector=(Vector) data.get(columnIndex);
           Object value=null; //kann was passieren?
           
           if(timeRuns == false){
               if(columnIndex < this.arrayVector.get(rowIndex).length  ){

                    value = (double) arrayVector.get(rowIndex)[columnIndex];
               }
               else{
                   value = "-";
               }
           }
           
           if(timeRuns == true){ //kann timeRuns true sein, aber trotzdem scheiße bauen?
               if(columnIndex < this.arrayVector.get(rowIndex).length + 1){ /** +1 */
                    if (columnIndex == 0){
                       // value = timeVector.get(rowIndex).toString();
                        value = timeVector.get(rowIndex);
                    } else {
                        value =  arrayVector.get(rowIndex)[columnIndex - 1]; //koennen negative Indizes auftreten?
                    }
                    
               }
               else{
                   value = "-";
               }
           }
           /*
           double[] rowarray;    
           rowarray = (double[]) this.arrayVector.get(rowIndex);
           value = rowarray[columnIndex];       
*/
           return value;
    }
    
    public double[] getColumnArray(int col){
        
        int size = arrayVector.size();
        double[] colArray = new double[size];
        
        if(timeRuns == true){
            
            
            if(col != 0){
                
                for(int k=0;k<size;k++){
                    colArray[k] = arrayVector.get(k)[col];
                }
            }else{
                /* was nun?? */
            }
        }else{
            
            for(int k=0;k<size;k++){
                    colArray[k] = arrayVector.get(k)[col];
            }
        }
        return colArray;
    }
   
    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
        /*
        if(worksWithArray == false){
            data.get(columnIndex).setElementAt(aValue, rowIndex);
        }
         */
         

    }
    
    /*** **************************************************************** ***/
   
    public Vector<double[]> getDataVector(){
        return this.arrayVector;
    }
   
    

    public void setNewDataVector(Vector<double[]> inputvector){
        this.arrayVector = (Vector<double[]>) inputvector;
       // this.arrayVector = (Vector) inputvector.clone();
        
        //int columns=0;
        
            if(!arrayVector.isEmpty()){
                for(int i=0; i < arrayVector.size(); i++){
                    if(timeRuns == false){
                        if(columns < arrayVector.get(i).length){
                           columns = arrayVector.get(i).length;
                           //this.colnamesset=false;
                        }
                    } else {
                        if(columns < arrayVector.get(i).length + 1){
                           columns = arrayVector.get(i).length + 1;
                           //this.colnamesset=false;
                    }
                }
            }
        }
        this.columns=columns;
         //TODO: hier möglichkeit für colnames schaffen!!
        
        
    }
    
    public Vector<JAMSCalendar> getTimeVector(){
        if(timeRuns == true){
            return this.timeVector;
        }
        else{
            return null;
        }
    }
    
    public void setTimeVector(Vector<JAMSCalendar> timeVector){
       
        this.timeVector = timeVector; //casten?
        timeRuns = true;
    }

    
    public void addRowArray(double[] rowdata){
        arrayVector.add(rowdata);
        if(this.columns < rowdata.length){
            this.columns  = rowdata.length;
        }
        rows++;
    }
    /*
    public void addTimeString(String time){
        timeAxis.add(time);
    }
     **/
    
    public void addTime(JAMSCalendar time){
        JAMSCalendar nextTime = time.clone();
        timeVector.add(nextTime);
    }
    
    /*
    public JAMSCalendar getTime(){
        
       
    }
     **/
    
    /* old method for save procedure */
    public String[] getCoulumnNameArray(){
        String[] cnames = new String[columns];
        if (this.colnamesset == false){
             for(int i=0; i<columns; i++){
                 cnames[i]="COL "+i;
             }
        }
        else{
            cnames = colnames;         
        }
        
        return cnames;       
    }
    
    public void setColumnNames(String[] names){
        
        if(columns==0){
            this.columns = names.length;
            this.colnames = names;
        }
        
        if(names.length > columns){
            this.columns = names.length;
        }
        
       
        if (names.length == columns){
            //java.lang.System.arraycopy(names, 0, this.colnames, 0, this.columns);
            this.colnames = names;
        }
        else{
            String[] cnames = new String[columns];
            
            if (names.length > columns){
                
                for(int i=0; i < columns; i++){
                    cnames[i] = names[i];
                }
            }
            if (names.length < columns){
                for(int i=0; i < names.length; i++){
                    cnames[i] = names[i];
                }
                for (int j = names.length; j < columns; j++){
                    cnames[j] = "Col "+j;
                }
            }
            //java.lang.System.arraycopy(cnames, 0, this.colnames, 0, this.columns);
            this.colnames=cnames;
        }

        this.colnamesset = true;
         
    }
    
    public void setTimeRuns(boolean isTimeRunning){
        this.timeRuns = isTimeRunning;
    }
    /*
     public void addValue(double value, int columnIndex){
       // if (columnIndex < numberOfColumns){                       // hier noch else-Verhalten!!
           data.get(columnIndex).addElement(value);       
        //}
    }
     **/
   
}
