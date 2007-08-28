/*
 * SpaceTools.java
 *
 * Created on 16. Februar 2007, 10:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.tspaces;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.io.GenericDataWriter;
import org.unijena.jams.model.*;
import com.ibm.tspaces.*;
import java.io.*;
import java.util.*;
/**
 *
 * @author ncb
 */
public class SpaceTools {
    TupleSpace tsJAMS;
    boolean tsJAMSActive = false;
    /** Creates a new instance of SpaceTools */
    public SpaceTools() {
       
    }
    public SpaceTools(String spaceName,String hostName){
        setSpace(spaceName,hostName);
    }
    
    public boolean setSpace(String spaceName,String hostName){
        try{
          tsJAMS = new TupleSpace(spaceName,hostName);
          tsJAMSActive = true;
               
        }
        catch (Exception e)
        {
             System.out.println(e.toString()+" Unable to open space " + spaceName + " at host "+hostName);
             tsJAMSActive = false;
        }
        return tsJAMSActive;
    }   
    
    public int patternExists(String key, Class classValue){
       int counted = -1;
        if (tsJAMSActive) {
            try{

              Tuple template = new Tuple(key,new Field(classValue));
              counted = tsJAMS.countN(template);

            }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
       return counted;         
    }
    /**
     * 
     * @param key 
     * @return 
     */
    public Number getNumber(String key){
        int value = -1;
        Number number = null;
        if (tsJAMSActive) {
            try{
              Tuple template = new Tuple(key,new Field(Number.class));
              Tuple tupleRead =tsJAMS.read(template);
              number = (Number)tupleRead.getField(1).getValue();
            }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
       return number;    
    }
     public String getString(String key){
        String result = null;
        if (tsJAMSActive) {
            try{
              Tuple template = new Tuple(key,new Field(String.class));
              Tuple tupleRead =tsJAMS.read(template);
              result = (String)tupleRead.getField(1).getValue();
            }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
       return result;    
    }
     
      public String getStringN(String key){
        String result = null;
        if (tsJAMSActive) {
            try{
              Tuple template = new Tuple(key,new Field(String.class));
              Tuple tupleSet =tsJAMS.scan(template);
              for( Enumeration e = tupleSet.fields(); e.hasMoreElements(); ) {
                  Field f = (Field)e.nextElement();
                  Tuple tuple = (Tuple)f.getValue();
                  result = result + (String)tuple.getField(1).getValue();
              }
              
            }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
       return result;    
    }
      
     
    public void setNumber(String key,Number value){
         if (tsJAMSActive) {
            try{
              Tuple template = new Tuple(key,new Field(Number.class));  
              int counted = tsJAMS.countN(template);
              if (counted>0){
                  //delete existing Tuples
                  tsJAMS.delete(template);
              }
              Tuple tupleToWrite = new Tuple();
              tupleToWrite.add(new Field(key));
              tupleToWrite.add(new Field(value));
              tsJAMS.write(tupleToWrite);
                         }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
      }
       
     public void addNumber(String key,Number addValue){
         if (tsJAMSActive) {
            double doubleValue = 0; 
            Number number;
            try{
              Tuple template = new Tuple(key,new Field(Number.class));  
              int counted = tsJAMS.countN(template);
              Tuple tupleToTake;
              if (counted>0){
                  //take existing Tuples
                  tupleToTake = tsJAMS.take(template);
                  doubleValue = ((Number)tupleToTake.getField(1).getValue()).doubleValue();
                  doubleValue = doubleValue + addValue.doubleValue();
                  addValue = new Double(doubleValue);
              }
              Tuple tupleToWrite = new Tuple();
              tupleToWrite.add(new Field(key));
              tupleToWrite.add(new Field(addValue));
              tsJAMS.write(tupleToWrite);
                         }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
      }
    public TupleSpace getTupleSpace(){
        return tsJAMS;
    }
}
