/*
 * JAMSTSpaces.java
 *
 * Created on 6. Februar 2007, 17:53
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.tspaces;
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
@JAMSComponentDescription(
title="Title",
        author="Author",
        description="Description"
        )
public class JAMSTSpaces  extends JAMSContext {
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Unique key of the modeller framework e.g. name of the user. Important: combination of modellerKey and IP have to be unique"
            )
            public JAMSString modelerKey;  
      
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Number of requested JVMs. Modell will not start untill these machines are assigned"
            )
            public JAMSInteger requestedJVM;
    
      @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "IP or qualified computer name"
            )
            public JAMSString tSpaceIP;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "partitioning of the model [sampler]"
            )
            public JAMSString modelPartitioning;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Flag for dis/enabling this sampler"
            )
            public JAMSBoolean enable;
    
      
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Parameter to delete existing tuples, possible values: complete, done, init, run, cleanup"
            )
            public JAMSString deletePriority;
    
    TupleSpace tsJAMS;
    SpaceTools spaceTools;   
    int tempInt;
    
    public void init() {
         spaceTools = new SpaceTools(modelerKey,tSpaceIP);
         
          Transaction trans = new Transaction();
          try{
                 trans.addTupleSpace(spaceTools.tsJAMS);
                 trans.beginTrans();
                 spaceTools.deleteSpace(deletePriority);
                 trans.commitTrans();
                 JAMSString exkl = new JAMSString("****initJAMSTSpaces****");
                 if (!spaceTools.patternExists(exkl)){
                     spaceTools.tsJAMS.write(new Tuple(new JAMSString("****initJAMSTSpaces****")));
                     JAMSString actualState = spaceTools.readString(new JAMSString("state"));;
                    //muss noch geklï¿½rt werden cccc
//                    if ((actualState != null) && (actualState.equals(new JAMSString("done")))){
//                              tempInt = tsJAMS.deleteAll();
//                    } 
                    if (actualState == null){
                          spaceTools.setString(new JAMSString("state"),new JAMSString("init"));
                          spaceTools.setInteger(new JAMSString("requestedJVM"),requestedJVM); 
                          spaceTools.setInteger(new JAMSString("assignedJVM"),new JAMSInteger(0));
                     }
                    spaceTools.tsJAMS.delete(new Tuple(new JAMSString("****initJAMSTSpaces****"))); 
                    
                 } 
                  else{
                     Thread.currentThread().sleep(300);
                }
                trans.commitTrans();
          }
          catch (Exception e) {System.out.println(e.toString());}
         spaceTools.synchronizeJVM(requestedJVM.getValue());
         super.init();     
    }
    
    public void run() {
      getModel().getRuntime().println("Space is RUNNING");
      try{
          Transaction trans = new Transaction();
          trans.addTupleSpace(spaceTools.tsJAMS);
          trans.beginTrans();
          JAMSString state = spaceTools.readStringW(new JAMSString("state"));
          if (state.equals(new JAMSString("init"))){
              spaceTools.setString(new JAMSString("state"),new JAMSString("run"));
          }
          trans.commitTrans();
      }
       catch (Exception e){
            System.out.println(e.toString());
        }
   /*     
        double precip = this.precip.getValue();
        double runoff;
        
     //   double a = this.a.getValue();
      //  double b = this.b.getValue();
        double c = this.c.getValue();
        double sqrt=0;
   
        for (int i=0; i<1000;i++)
        {
            sqrt=Math.sqrt(i);
           
        }
             System.out.println("Done sqrt" );
        
        if(a+b > 1.0){
            System.out.println("Constraint violated: a + b is larger than 1.0");
            return;
        }
        
        runoff = (1 - a - b) * precip + c * storage_old;
        storage_old = a * precip + (1-c) * storage_old;
        
        this.simRunoff.setValue(runoff);
        getModel().getRuntime().println("Time: " + time.get(JAMSCalendar.YEAR) + " Runoff: " + runoff);
    **/
       super.run();
    }
    
    public void cleanup() {
        super.cleanup();
        try{
            getModel().getRuntime().println("CLEANUP Space");
            Transaction trans = new Transaction();
            trans.addTupleSpace(spaceTools.tsJAMS);
            trans.beginTrans();
            JAMSString state = spaceTools.readStringW(new JAMSString("state"));
            if (state.equals(new JAMSString("run"))){
               spaceTools.setString(new JAMSString("state"),new JAMSString("cleanup"));
            }
            trans.commitTrans();
            trans.beginTrans();
            Tuple template,takeTuple,tupleSet,tupleToWrite;
            int assignedJVM = spaceTools.readIntegerW(new JAMSString("assignedJVM")).getValue();
            System.out.println("assignedJVM :" + assignedJVM);
           
            spaceTools.addInteger(new JAMSString("assignedJVM"),new JAMSInteger(-1));
            trans.commitTrans();
            trans.beginTrans();
            JAMSInteger currentCount = spaceTools.readIntegerW(new JAMSString("currentCount"));
            JAMSInteger sampleCount = spaceTools.readIntegerW(new JAMSString("sampleCount"));
            trans.commitTrans(); 
            boolean stopComputing = currentCount.getValue()>=sampleCount.getValue();
            if ((assignedJVM == 1)||(stopComputing)){
                      //time to say goodbye to the space and flush result

                     //System.out.println(spaceTools.getStringN("result"));
                 state = spaceTools.readStringW(new JAMSString("state"));
                 if (!state.equals(new JAMSString("done"))){
                     spaceTools.setString(new JAMSString("state"),new JAMSString("done"));
                     trans.beginTrans();
                     getModel().getRuntime().println("RUN completed, new state is: done.");
                     //write the result
                     String fileName = spaceTools.takeString(new JAMSString("fileName")).getValue();
                     Writer writer = new BufferedWriter(new FileWriter(fileName));
                     String header = spaceTools.takeString(new JAMSString("header")).getValue();
                     int counter = spaceTools.patternExistsN(new JAMSString("result"),JAMSString.class);
                     writer.write(header+"\n");
                     writer.flush();
                     String line;
                     for (int i=0;i<counter;i++){
                         line = spaceTools.takeString(new JAMSString("result")).getValue();
                         writer.write(line+"\n");
                         writer.flush();
                     }
                     writer.flush();
                     writer.close();
                     spaceTools.tsJAMSActive = false;
                     trans.commitTrans();
                 }
             }   
             
             
        }
        catch (Exception e){
            System.out.println(e.toString());
        }

//                 if (count > 0){
//                     Writer writer = new BufferedWriter(new FileWriter("c:/result.txt"));
//                     String line;
//                     tupleSet = tsJAMS.scan(template);
//                     for( Enumeration e = tupleSet.fields(); e.hasMoreElements(); ) {
//                          Field f = (Field)e.nextElement();
//                          Tuple tuple = (Tuple)f.getValue();
//                          line = (String)tuple.getField(1).getValue();
//                          writer.write(line);
//
//                      }
//                     writer.flush();
//                     writer.close();
//                 }  
            
              //flush result TupleSpace and erase all assigned TupelSpaces if counted = 1 ...
      
        
      //  super.run();
    }
    
    /** Creates a new instance of JAMSTSpaces */
   /** public JAMSTSpaces() {
    }*/
    
}
