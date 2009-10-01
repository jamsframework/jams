/*
 * JAMSTSpaces.java
 *
 * Created on 6. Februar 2007, 17:53
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
@JAMSComponentDescription(
title="Title",
        author="Author",
        description="Description"
        )
public class JAMSTSpaces  extends JAMSContext {
    
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
    
    TupleSpace tsJAMS;
    SpaceTools spaceTools;   
    
    
    public void init() {
         spaceTools = new SpaceTools("tsJAMS",tSpaceIP.getValue());      
         getModel().getRuntime().println("INIT Space");
         //tsJAMS = spaceTools.getTupleSpace();
         spaceTools.addNumber("CountedClients",new Integer(1));
         int counted = spaceTools.getNumber("CountedClients").intValue();
         System.out.println("actual Clients" + counted);
          
      
       
        super.init();     
    }
    
    public void run() {
      getModel().getRuntime().println("Space is RUNNING");
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
              getModel().getRuntime().println("CLEANUP Space");
        Tuple template,takeTuple,tupleSet,tupleToWrite;
         int counted = spaceTools.getNumber("CountedClients").intValue();
         System.out.println("actual Clients:" + counted);
         if (counted == 1){
                  //time to say goodbye to the space and flush result
             if (spaceTools.patternExists("result",String.class)>0){
                 System.out.println(spaceTools.getStringN("result"));
             }
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
