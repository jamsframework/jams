/*
 * SpaceTools.java
 *
 * Created on 16. Februar 2007, 10:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.tspaces;
//import org.unijena.jams.JAMS;
//import org.unijena.jams.data.*;
//import org.unijena.jams.io.GenericDataWriter;
//import org.unijena.jams.model.*;
import com.ibm.tspaces.*;
import java.lang.*;
import java.io.*;
import java.util.*;
import org.unijena.jams.data.*;
        
        
/**
 *
 * @author ncb
 */
public class SpaceTools {
    TupleSpace tsJAMS;
    int threadID;
    int tempInt;
  //  int timeout = 100;
    boolean tsJAMSActive = false;
    /** Creates a new instance of SpaceTools */
    public SpaceTools() {
       
    }
    public SpaceTools(JAMSString spaceName,JAMSString hostName){
        setSpace(spaceName,hostName);
    }
     public void synchronizeJVM(int requestedJVM){
         /** Use this method to create a Space which synchronizes the JVM.
          * Space will be blocked until the requested JVMs are associated
          */ 
        //first search for a space with the same name
         //a timeout value less 0 means to wait forever
         try{
            
         //    if (!TupleSpace.exists(spaceName,hostName)){
                
                int assignedJVM;
              
                //check the counted JVMs
                assignedJVM = this.addInteger(new JAMSString("assignedJVM"),new JAMSInteger(1));
                requestedJVM = this.readIntegerW(new JAMSString("requestedJVM")).getValue();
                while (assignedJVM<requestedJVM){
                    Thread.currentThread().sleep(100);
                    assignedJVM = this.readIntegerW(new JAMSString("assignedJVM")).getValue();
                }
         }
         catch (Exception e){
           System.out.println(e.toString());
         };
    // tSpace(spaceName,hostName);
         //return true;
    }
    
    public void deleteSpace(JAMSString deletePriority) {
            try{
                if (tsJAMS != null){
                     if (deletePriority.equals(new JAMSString("complete"))){
                         tsJAMS.deleteAll(); 
                     }
                     
                     JAMSString state = this.readString(new JAMSString("state"));
                     if (state != null){
                         // noch zu kl�ren ccc
                         if ((state.equals(deletePriority)) && (state.equals(new JAMSString("done")))){
                             tsJAMS.deleteAll(); 
                         }
                     }
                         ;
                 } 
            }
            catch (Exception e){
            }
    }
     
    public boolean setSpace(JAMSString spaceName,JAMSString hostName){
        try{
             tsJAMS = new TupleSpace(spaceName.toString(),hostName.toString());
             if (tsJAMS != null){
                 JAMSString state = this.readString(new JAMSString("state"));
                 if (state != null){
                     // noch zu kl�ren ccc
//                     if (state.equals("done")){
//                         System.exit(0);
//                     }
                 }
                     ;
             }    
             tsJAMSActive = true;
              
        }
        catch (Exception e)
        {
             System.out.println(e.toString()+" Unable to open space " + spaceName + " at host "+hostName);
             tsJAMSActive = false;
        }
        return tsJAMSActive;
    }   
    
    public boolean patternExists(JAMSString key){
       boolean exists = false;
        if (tsJAMSActive) {
            try{

              Tuple template = new Tuple(key);
              int counted = tsJAMS.countN(template);
              if (counted > 0)
                  exists = true;

            }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
       return exists;   
    }  
    
      public boolean patternExists(JAMSString key, Class classValue){
        boolean exists = false;
        if (tsJAMSActive) {
            try{

              Tuple template = new Tuple(key,new Field(classValue));
              int counted = tsJAMS.countN(template);
              if (counted > 0)
                  exists = true;

            }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
       return exists;   
    }  
      
      
       public int patternExistsN(JAMSString key, Class classValue){
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
        public int patternExistsN(JAMSString key){
       int counted = -1;
        if (tsJAMSActive) {
            try{

              Tuple template = new Tuple(key);
              counted = tsJAMS.countN(template);

            }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
       return counted;         
    }
        
             public JAMSSerializableData readObject(JAMSString key){
         return this.getObject(key,false,true);
     }
    
     public JAMSSerializableData readObjectW(JAMSString key){
         return this.getObject(key,true,true);
     }   
      public JAMSSerializableData takeObject(JAMSString key){
         return this.getObject(key,false,false);
     }
    
     public JAMSSerializableData takeObjectW(JAMSString key){
         return this.getObject(key,true,false);
     }   
     
    public JAMSSerializableData getObject(JAMSString key , boolean wait, boolean read){
        JAMSSerializableData resultObject = null;
        if (tsJAMSActive) {
            try{
             //     Tuple template = new Tuple(key,new Field(JAMSString.class));  
              Tuple template = new Tuple(key,new Field(JAMSSerializableData.class));
              
              Tuple tupleGet = null;
              if (read){
                  if (wait) tupleGet = tsJAMS.waitToRead(template);
                  else tupleGet = tsJAMS.read(template);
                  if (tupleGet != null)
                    resultObject = (JAMSSerializableData)tupleGet.getField(1).getValue();
              }
              else{
                if (wait) tupleGet = tsJAMS.waitToTake(template);
                else tupleGet = tsJAMS.take(template);
                if (tupleGet != null)
                    resultObject = (JAMSSerializableData)tupleGet.getField(1).getValue();
             }
           //    String className = resultObject.getClass().getName();
         }
         catch (Exception e)
         {
            System.out.println(e.toString());
         }
        }
       return resultObject;   
    }   
        
    /**
     * 
     * @param key 
     * @return 
     */
     public JAMSInteger readInteger(JAMSString key){
         return this.getInteger(key,false,true);
     }
    
     public JAMSInteger readIntegerW(JAMSString key){
         return this.getInteger(key,true,true);
     }   
      public JAMSInteger takeInteger(JAMSString key){
         return this.getInteger(key,false,false);
     }
    
     public JAMSInteger takeIntegerW(JAMSString key){
         return this.getInteger(key,true,false);
     }   
     
    public JAMSInteger getInteger(JAMSString key , boolean wait, boolean read){
        JAMSInteger resultObject = null;
        if (tsJAMSActive) {
             try
             {
                 JAMSSerializableData superResultObject;
                 superResultObject = getObject(key, wait, read);
                 if (superResultObject != null){
                     String className = superResultObject.getClass().getName();
                     if (className.equals("org.unijena.jams.data.JAMSInteger"))
                        resultObject = (JAMSInteger) superResultObject;
                 }

             }
             catch (Exception e)
             {
                System.out.println(e.toString());
             }
        }
       return resultObject;   
    }   
    
       public JAMSDouble readDouble(JAMSString key){
         return this.getDouble(key,false,true);
     }
    
     public JAMSDouble readDoubleW(JAMSString key){
         return this.getDouble(key,true,true);
     }   
      public JAMSDouble takeDouble(JAMSString key){
         return this.getDouble(key,false,false);
     }
    
     public JAMSDouble takeDoubleW(JAMSString key){
         return this.getDouble(key,true,false);
     }   
     
    public JAMSDouble getDouble(JAMSString key , boolean wait, boolean read){
          JAMSDouble resultObject = null;
        if (tsJAMSActive) {
             try
             {
                 JAMSSerializableData superResultObject;
                 superResultObject = getObject(key, wait, read);
                 if (superResultObject != null){
                     String className = superResultObject.getClass().getName();
                     if (className.equals("org.unijena.jams.data.JAMSDouble"))
                        resultObject = (JAMSDouble) superResultObject;
                 }

             }
             catch (Exception e)
             {
                System.out.println(e.toString());
             }
        }
       return resultObject;   
    }  
    
     public JAMSString readString(JAMSString key){
         return this.getString(key,false,true);
     }
    
     public JAMSString readStringW(JAMSString key){
         return this.getString(key,true,true);
     }   
      public JAMSString takeString(JAMSString key){
         return this.getString(key,false,false);
     }
    
     public JAMSString takeStringW(JAMSString key){
         return this.getString(key,true,false);
     }   
     
    public JAMSString getString(JAMSString key , boolean wait, boolean read){
       JAMSString resultObject = null;
        if (tsJAMSActive) {
             try
             {
                 JAMSSerializableData superResultObject;
                 superResultObject = getObject(key, wait, read);
                 if (superResultObject != null){
                     String className = superResultObject.getClass().getName();
                     if (className.equals("org.unijena.jams.data.JAMSString"))
                        resultObject = (JAMSString) superResultObject;
                 }

             }
             catch (Exception e)
             {
                System.out.println(e.toString());
             }
        }
       return resultObject;   
    }   
    

    
      public JAMSSerializable getJAMSObjectW(JAMSString key){
        JAMSSerializable result = null;
        if (tsJAMSActive) {
            try{
              Tuple template = new Tuple(key,new Field(String.class));
              Tuple tupleRead =tsJAMS.waitToRead(template);
              if (tupleRead != null)
                result = (JAMSSerializable)tupleRead.getField(1).getValue();
            }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
       return result;    
    }
     
      public JAMSSerializable[] getJAMSObjectArray(JAMSString key){
        JAMSSerializable result[] = null;
        if (tsJAMSActive) {
            try{
              Tuple template = new Tuple(key,new Field(String.class));
              Tuple tupleSet =tsJAMS.scan(template);
              if (tupleSet != null){
                  int i=0;
                  for( Enumeration e = tupleSet.fields(); e.hasMoreElements(); ) {
                      Field f = (Field)e.nextElement();
                      Tuple tuple = (Tuple)f.getValue();
                      result[i] = (JAMSSerializable) tuple.getField(1).getValue();
                      i++;
                  }
              }   
              
            }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
       return result;    
    }
     public void setString(JAMSString key,JAMSString value){
         if (tsJAMSActive) {
            try{
                this.setObject(key,value,JAMSString.class);
                         }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
      }  
     
     public void writeString(JAMSString key,JAMSString value){
         if (tsJAMSActive) {
            try{
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
     
      public void addJAMSObj(JAMSString key,JAMSSerializable value){
         if (tsJAMSActive) {
            try{
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
      
          public void setObject(JAMSString key,JAMSSerializableData value,Class classPattern){
         if (tsJAMSActive) {
            try{
              Class className = value.getClass();  
              Tuple template = new Tuple(key,new Field(classPattern));  
         //     JAMSString result = (JAMSString)template.getField(0).getValue();
           //   String name = template.getField(0).getName();
           //  template.getField(0).setName("name");
           //   name = template.getField(0).getName();
            //    Integer obj = new Integer(4);
                //OwnTSClass obj = new OwnTSClass("User Object 1");
            //    Tuple writeTemplate = new Tuple(key,value);  
           //     Tuple searchTemplate = new Tuple(new Field (JAMSSerializable.class));//new Field(JAMSBoolean.class));   
               // template = searchTemplate;
          //    tsJAMS.write(writeTemplate);
           //   template = new Tuple(JAMSString.class);
          //    Tuple mytuple = tsJAMS.read(searchTemplate);
              int counted = tsJAMS.countN(template);
              if (counted>0){
                  //delete existing Tuples
                  tsJAMS.delete(template);
              }
              Tuple tupleToWrite = new Tuple();
              tupleToWrite.add(new Field(key));
              tupleToWrite.add(new Field(value));
          //    tupleToWrite.getField(0).setName("name");
              tsJAMS.write(tupleToWrite);
                         }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
      }  
          
     public void setDouble(JAMSString key,JAMSDouble value){
         if (tsJAMSActive) {
            try{
                this.setObject(key,value,JAMSDouble.class);
                         }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
      }  
     
    public void setInteger(JAMSString key,JAMSInteger integer){
         if (tsJAMSActive) {
            try{
              Tuple template = new Tuple(key,new Field(Integer.class));  
             this.setObject(key,integer,JAMSInteger.class);
            }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
      }
      
    public int addInteger(JAMSString key,JAMSInteger addValue){
         
            int intValue = addValue.getValue(); 
            try{
            Transaction trans = new Transaction();
            trans.addTupleSpace(this.tsJAMS);
           // trans.beginTrans();  
              Tuple template = new Tuple(key,new Field(JAMSInteger.class));  
            //  int counted = tsJAMS.countN(template);
              Tuple tupleToTake;
            //  if (counted>0){
                  //take existing Tuples
              int templateExists =tsJAMS.countN(template);
                  tupleToTake = tsJAMS.take(template);
                  intValue = ((JAMSInteger)tupleToTake.getField(1).getValue()).getValue();
                  intValue = intValue + addValue.getValue();
                 // tsJAMS.delete(tupleToTake);
          //    }
              Tuple tupleToWrite = new Tuple();
              tupleToWrite.add(new Field(key));
              tupleToWrite.add(new Field(new JAMSInteger(intValue)));
              tsJAMS.write(tupleToWrite);
            //  trans.commitTrans();          
            }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        
         return intValue;
      }
     public double addDouble(String key,Double addValue){
        
            double doubleValue = addValue.doubleValue(); 
            try{
              Tuple template = new Tuple(key,new Field(Number.class));  
            //  int counted = tsJAMS.countN(template);
              Tuple tupleToTake;
           //   if (counted>0){
                  //take existing Tuples
                  tupleToTake = tsJAMS.waitToRead(template);
                  doubleValue = ((Number)tupleToTake.getField(1).getValue()).doubleValue();
                  doubleValue = doubleValue + addValue.doubleValue();
            //  }
              Tuple tupleToWrite = new Tuple();
              tupleToWrite.add(new Field(key));
              tupleToWrite.add(new Field(new Double(doubleValue)));
              tsJAMS.write(tupleToWrite);
                         }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        
        return doubleValue; 
      }
    public void setThreadID(){
        threadID=Thread.currentThread().hashCode();
    } 
       
    public void lockSpace(){
      this.setString(new JAMSString("ThreadID"+threadID),new JAMSString("transactionLocked"));       
    }
    public boolean isLocked(){
      boolean returnValue = true;  
      JAMSString result = this.readString(new JAMSString("ThreadID"+threadID));
      if (result == null) returnValue = false;
      return returnValue;  
    }
    
    public JAMSString releaseSpace(){
      return this.takeString(new JAMSString("ThreadID"+threadID));       
    }
    
    public TupleSpace getTupleSpace(){
        return tsJAMS;
    }
}
