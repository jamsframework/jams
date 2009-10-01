/*
 * SpaceTools.java
 *
 * Created on 16. Februar 2007, 10:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.tspaces;

import com.ibm.tspaces.*;
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
    // the general monitor
    /**
     * This is the general Monitor variable
     */
    JAMSString generalMonitor = new JAMSString("GENERAL***MONITOR***");
    JAMSString monitorPrefix = new JAMSString("***MONITOR***");
    // all other keys are built with monitor as pre-element e.g. ***MONITOR***state
    JAMSString[] validMonitors={
        monitorPrefix,
        new JAMSString("state"),
        new JAMSString("requestedJVM"),
        new JAMSString("assignedJVM"),
        new JAMSString("currentCount"),
        new JAMSString("sampleCount"),
        new JAMSString("resultsWritten"),
        new JAMSString("RandomParaSampler.updateValues"),
     new JAMSString("init"),
     new JAMSString("run"),
     new JAMSString("cleanup"),
    new JAMSString("RandomParaSampler.init"),
             new JAMSString("RandomParaSampler.run"),
     new JAMSString("RandomParaSampler.run.hasNext"),
     new JAMSString("RandomParaSampler.cleanup"),
    new JAMSString("JAMSTSpaces.init"),
     new JAMSString("JAMSTSpaces.run"),
     new JAMSString("JAMSTSpaces.cleanup"),};
    
    
    /**
     * Creates a new instance of SpaceTools
     * @param there are no parameters, it is the constructor 
     */
    public SpaceTools() {
        
    }
    public SpaceTools(JAMSString spaceName,JAMSString hostName){
        setSpace(spaceName,hostName);
    }
    /**
     * 
     * @param requestedJVM 
     */
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
        } catch (Exception e){
            System.out.println(e.toString());
        };
    }
    public void deleteSpace(){
        this.deleteSpace(new JAMSString("complete"));
    }
    
    public void deleteSpace(JAMSString deletePriority) {
        try{
            if (tsJAMS != null){
                if (deletePriority.equals(new JAMSString("complete"))){
                    tsJAMS.deleteAll();
                }
                
                JAMSString state = this.readString(new JAMSString("state"));
                if (state != null){
                    // to do further details cccc
                    if ((state.equals(deletePriority)) && (state.equals(new JAMSString("done")))){
                        tsJAMS.deleteAll();
                    }
                }
                ;
            }
        } catch (Exception e){
        }
    }
    
    /**
     * 
     * @param spaceName 
     * @param hostName 
     * @return 
     */
    public boolean setSpace(JAMSString spaceName,JAMSString hostName){
        try{
            // TupleSpace.setTSCmdImpl("com.ibm.tspaces.TSCmdLocalImpl");
            TupleSpace.setUserName("anonymous");
            TupleSpace.setPassword("");
            tsJAMS = new TupleSpace(spaceName.toString(),hostName.toString());
            if (tsJAMS != null){
                JAMSString state = this.readString(new JAMSString("state"));
                if (state != null){
                    // to do further details cccc
//                     if (state.equals("done")){
//                         System.exit(0);
//                     }
                }
                ;
            }
            tsJAMSActive = true;
            
        } catch (Exception e) {
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
                
            } catch (Exception e) {
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
                
            } catch (Exception e) {
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
                
            } catch (Exception e) {
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
                
            } catch (Exception e) {
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
                } else{
                    if (wait) tupleGet = tsJAMS.waitToTake(template);
                    else tupleGet = tsJAMS.take(template);
                    if (tupleGet != null)
                        resultObject = (JAMSSerializableData)tupleGet.getField(1).getValue();
                }
                //    String className = resultObject.getClass().getName();
            } catch (Exception e) {
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
            try {
                JAMSSerializableData superResultObject;
                superResultObject = getObject(key, wait, read);
                if (superResultObject != null){
                    String className = superResultObject.getClass().getName();
                    if (className.equals("org.unijena.jams.data.JAMSInteger"))
                        resultObject = (JAMSInteger) superResultObject;
                }
                
            } catch (Exception e) {
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
            try {
                JAMSSerializableData superResultObject;
                superResultObject = getObject(key, wait, read);
                if (superResultObject != null){
                    String className = superResultObject.getClass().getName();
                    if (className.equals("org.unijena.jams.data.JAMSDouble"))
                        resultObject = (JAMSDouble) superResultObject;
                }
                
            } catch (Exception e) {
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
            try {
                JAMSSerializableData superResultObject;
                superResultObject = getObject(key, wait, read);
                if (superResultObject != null){
                    String className = superResultObject.getClass().getName();
                    if (className.equals("org.unijena.jams.data.JAMSString"))
                        resultObject = (JAMSString) superResultObject;
                }
                
            } catch (Exception e) {
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
            } catch (Exception e) {
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
                
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        return result;
    }
    public void setString(JAMSString key,JAMSString value){
        if (tsJAMSActive) {
            try{
                this.setObject(key,value,JAMSString.class);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
    
    public void setStringSyncMonitor(JAMSString key,JAMSString value){
        if (tsJAMSActive) {
            try{
                this.setObjectSyncMonitor(key,value,JAMSString.class);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
    
    public void setStringSync(JAMSString key,JAMSString value){
        if (tsJAMSActive) {
            try{
                this.setObjectSync(key,value,JAMSString.class);
            } catch (Exception e) {
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
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
    
    public void setMonitorD(){
        this.setMonitorD(this.generalMonitor);
    }
    
    public void setMonitorD(JAMSString key){
        this.deleteMonitor(key);
        this.setMonitor(key);
    }
    
    public void setMonitor(){
        this.setMonitor(this.generalMonitor);
    }
    
    public void setMonitor(JAMSString key){
        if (tsJAMSActive) {
            try{
                key = new JAMSString(this.monitorPrefix.toString()+key.toString());
                Tuple tupleToWrite = new Tuple();
                tupleToWrite.add(new Field(key));
                tsJAMS.write(tupleToWrite);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
    
    public boolean monitorIsValid(JAMSString key){
        boolean isValid = false;
        if (Arrays.asList( this.validMonitors ).contains( key )){
            isValid = true;
        }
        return isValid;
    }
    
    public boolean monitorExistsInTupleSpace(JAMSString key){
        boolean exists = false;
        if (tsJAMSActive) {
            try{
                key = new JAMSString(this.monitorPrefix.toString()+key.toString());
                Tuple template =new Tuple(key);
                if (this.tsJAMS.countN(template)>0)
                    exists = true;
                
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        return exists;
    }
    public void takeMonitor(){
        this.takeMonitor(this.generalMonitor);
    }
    
    public void takeMonitor(JAMSString key){
        if (tsJAMSActive) {
            try{
                key = new JAMSString(this.monitorPrefix.toString()+key.toString());
                Tuple template =new Tuple(key);
                this.tsJAMS.waitToTake(template);
                
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
    public void deleteMonitor(){
        this.deleteMonitor(this.generalMonitor);
    }
    public void deleteMonitor(JAMSString key){
        if (tsJAMSActive) {
            try{
                key = new JAMSString(this.monitorPrefix.toString()+key.toString());
                Tuple template =new Tuple(key);
                this.tsJAMS.delete(template);
                
            } catch (Exception e) {
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
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
    /**
     * used to change objects inside the general monitor environment. 
     * All changes are locked with the general monitor variable
     * @param key name of the key
     * @param value the value (any serializable object)
     * @param classPattern associated class 
     */
    public void setObjectSync(JAMSString key,JAMSSerializableData value,Class classPattern){
        this.setObject(key,value,classPattern,new JAMSString("sync"));
    }
    /**
     * used to change objects inside a monitor environment. 
     * All changes are locked with a specific monitor variable based on the key, if the key is not 
     * declared as a valid monitor, the general monitor is used (similiar to setObjectSync}
     * @param key name of the key (implicites a valid monitor with the same name)
     * @param value value of the key
     * @param classPattern associated class
     */
    public void setObjectSyncMonitor(JAMSString key,JAMSSerializableData value,Class classPattern){
        this.setObject(key,value,classPattern,new JAMSString("syncMonitor"));
    }
    
    public void setObject(JAMSString key,JAMSSerializableData value,Class classPattern){
        this.setObject(key,value,classPattern,new JAMSString("none"));
    }
    public void setObject(JAMSString key,JAMSSerializableData value,Class classPattern,JAMSString synchronizedType){
        if (tsJAMSActive) {
            JAMSString monitorName = null;
            Transaction trans = null;
            try{
                
                if (synchronizedType.equals(new JAMSString("syncMonitor"))){
                    // check if there is a valid synchcronisation key in the space
                    if (this.monitorIsValid(key)){
                        //the key is valid (entry in array this.validKeys)
                        monitorName = key;
                    } else{
                        //there is no valid synchronized key, so take the general monitor key (empty String )
                        monitorName = new JAMSString("");
                    }
                    
                }
                if (synchronizedType.equals(new JAMSString("sync"))){
                    monitorName =  new JAMSString("");
                }
                
                if (monitorName!=null){
                    //there are some critical areas
                    this.takeMonitor(monitorName);
                }
                
                Class className = value.getClass();
                Tuple template = new Tuple(key,new Field(classPattern));
                // int counted = tsJAMS.countN(template);
                // if (counted>0){
                //delete existing Tuples
                tsJAMS.delete(template);
                //  }
                Tuple tupleToWrite = new Tuple();
                tupleToWrite.add(new Field(key));
                tupleToWrite.add(new Field(value));
                //    tupleToWrite.getField(0).setName("name");
                tsJAMS.write(tupleToWrite);
                
                
                
                // this.deleteKey(monitorKey);
            } catch (Exception e) {
                System.out.println(e.toString());
            } finally {
                if (monitorName!=null){
                    this.setMonitor(monitorName);
                    
                }
            }
            
        }
    }
    
    public void setDouble(JAMSString key,JAMSDouble value){
        if (tsJAMSActive) {
            try{
                this.setObject(key,value,JAMSDouble.class);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
    
    public void setInteger(JAMSString key,JAMSInteger integer){
        if (tsJAMSActive) {
            try{
                Tuple template = new Tuple(key,new Field(Integer.class));
                this.setObject(key,integer,JAMSInteger.class);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
    public void setIntegerSyncKey(JAMSString key,JAMSInteger integer){
        if (tsJAMSActive) {
            try{
                Tuple template = new Tuple(key,new Field(Integer.class));
                this.setObjectSyncMonitor(key,integer,JAMSInteger.class);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
    
    public void setIntegerSync(JAMSString key,JAMSInteger integer){
        if (tsJAMSActive) {
            try{
                Tuple template = new Tuple(key,new Field(Integer.class));
                this.setObjectSync(key,integer,JAMSInteger.class);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
    
    
    
    public int addInteger(JAMSString key,JAMSInteger addValue){
        
        int intValue = addValue.getValue();
        //  Tuple template = new Tuple(key,new Field(JAMSSerializableData.class));
        
        //     if (tsJAMS)
        
        try{
            
            
            
            //Transaction trans = new Transaction();
            // trans.addTupleSpace(this.tsJAMS);
            // trans.beginTrans();
            Tuple template = new Tuple(key,new Field(JAMSInteger.class));
            //   int counted = tsJAMS.countN(template);
            Tuple tupleToTake;
            // if (counted>0){
            //take existing Tuples
            // int templateExists =tsJAMS.countN(template);
            tupleToTake = tsJAMS.waitToTake(template);
            //tupleToTake = tsJAMS.take(template);
            intValue = ((JAMSInteger)tupleToTake.getField(1).getValue()).getValue();
            intValue = intValue + addValue.getValue();
            // tsJAMS.delete(tupleToTake);
//              }
//             else{
//
//                  System.out.println("Warning: Try to  add (take) a value in space, but key "+key.getValue()+" was missing!");
//             }
            Tuple tupleToWrite = new Tuple();
            tupleToWrite.add(new Field(key));
            tupleToWrite.add(new Field(new JAMSInteger(intValue)));
            tsJAMS.write(tupleToWrite);
            //trans.commitTrans();
            
            
        } catch (Exception e) {
            System.out.println(e.toString());
            System.out.println("Exception in module addInteger, key:"+key+" addValue:"+addValue);
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
        } catch (Exception e) {
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