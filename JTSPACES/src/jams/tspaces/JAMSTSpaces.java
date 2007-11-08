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
        
        spaceTools.deleteSpace(deletePriority);
        JAMSString excl = new JAMSString("****initJAMSTSpaces****");
         JAMSString monitor=new JAMSString("JAMSTSpaces.init");
        if (!spaceTools.monitorExistsInTupleSpace(excl)){
            spaceTools.takeMonitor(monitor);
            // now we are in a uncritical area and need to reread the pattern exkl
            if (!spaceTools.monitorExistsInTupleSpace(excl)){
                spaceTools.setMonitor(excl);
                
                spaceTools.setString(new JAMSString("state"),new JAMSString("init"));
                spaceTools.setInteger(new JAMSString("requestedJVM"),requestedJVM);
                spaceTools.setInteger(new JAMSString("assignedJVM"),new JAMSInteger(0));
            }
            spaceTools.setMonitor(monitor);
        }
        spaceTools.synchronizeJVM(requestedJVM.getValue());
        super.init();
    }
    
    public void run() {
        getModel().getRuntime().println("Space is RUNNING");
        try{
            //Transaction trans = new Transaction();
            //trans.addTupleSpace(spaceTools.tsJAMS);
            //trans.beginTrans();
            JAMSString monitor=new JAMSString("JAMSTSpaces.run");
            spaceTools.takeMonitor(monitor);
            JAMSString state = spaceTools.readStringW(new JAMSString("state"));
            if (state.equals(new JAMSString("init"))){
                spaceTools.setString(new JAMSString("state"),new JAMSString("run"));
            }
            spaceTools.setMonitor(monitor);
            //trans.commitTrans();
        } catch (Exception e){
            System.out.println(e.toString());
        }
        super.run();
    }
    
    public void cleanup() {
        super.cleanup();
        JAMSString monitor=null;
        try{
            getModel().getRuntime().println("CLEANUP Space");
            monitor=new JAMSString("JAMSTSpaces.cleanup");
            spaceTools.takeMonitor(monitor);
            JAMSString state = spaceTools.readStringW(new JAMSString("state"));
            if (state.equals(new JAMSString("run"))){
                spaceTools.setString(new JAMSString("state"),new JAMSString("cleanup"));
            }
           // spaceTools.setMonitor(monitor);
            Tuple template,takeTuple,tupleSet,tupleToWrite;
            int assignedJVM = spaceTools.readIntegerW(new JAMSString("assignedJVM")).getValue();
            System.out.println("assignedJVM :" + assignedJVM);
            
            spaceTools.addInteger(new JAMSString("assignedJVM"),new JAMSInteger(-1));
            assignedJVM = spaceTools.readIntegerW(new JAMSString("assignedJVM")).getValue();
            System.out.println("decreased assignedJVM :" + assignedJVM);
         
            JAMSInteger resultsWritten = spaceTools.readIntegerW(new JAMSString("resultsWritten"));
            JAMSInteger sampleCount = spaceTools.readIntegerW(new JAMSString("sampleCount"));
           
            boolean stopComputing = resultsWritten.getValue()>=sampleCount.getValue();
            // if ((assignedJVM == 1)||(stopComputing)){
            if (stopComputing){
                // time to say goodbye to the space and flush result
                // to decrease the counter based on assigned jvms, it is insecure, if any machine crashes during the run
                // prevent n-times writing
               // spaceTools.takeMonitor();
                state = spaceTools.readStringW(new JAMSString("state"));
                if (!state.equals(new JAMSString("done"))){
                    spaceTools.setString(new JAMSString("state"),new JAMSString("done"));
                 
                    getModel().getRuntime().println("RUN completed, new state is: done.");
                    //write the result
                    String fileName = spaceTools.takeString(new JAMSString("fileName")).getValue();
                    Writer writer = new BufferedWriter(new FileWriter(fileName));
                    String header = spaceTools.takeString(new JAMSString("header")).getValue();
                    // int counter = spaceTools.patternExistsN(new JAMSString("result"),JAMSString.class);
                    int counter = spaceTools.readIntegerW(new JAMSString("sampleCount")).getValue();
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
                    //spaceTools.tsJAMSActive = false;
                 
                }
              //spaceTools.setMonitor();  
            }
            
            
        } catch (Exception e){
            System.out.println(e.toString());
        }
        finally{
            spaceTools.setMonitor(monitor);
        }
        //super.cleanup();
    }
    
    
}
