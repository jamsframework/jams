/*
 * NNOptimizer.java
 *
 * Created on 8. November 2007, 11:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.optimizer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import jams.data.JAMSDouble;
import jams.data.JAMSInteger;
import jams.data.JAMSString;
import jams.model.Component;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSContext;
import jams.model.JAMSVarDescription;

//import jams.components.optimizer.
@JAMSComponentDescription(
        title="NNOptimizer",
        author="Christian Fischer",
        description="under construction, do not use!!"
        )
public class RepeatContext extends JAMSContext {
    /*
     *  Component variables
     */    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "number of iterations"
            )
            public JAMSInteger maxIteration;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "current iteration count"
            )
            public JAMSInteger currentIteration;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "data to be written in file"
            )
            public JAMSDouble[] output;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "name of file"
            )
            public JAMSString fileName;
           
                            
    private void singleRun() {    	        
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            try {
                comp.init();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            try {
                comp.run();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        currentIteration.setValue(currentIteration.getValue()+1);
    }
    
   
    
    public void run() {        
        while (currentIteration.getValue() < maxIteration.getValue())   {
            BufferedWriter writer = null;
            if (fileName != null){
                try{
                    writer = new BufferedWriter(new FileWriter(this.fileName.getValue(),true));
                }catch(Exception e){
                    System.out.println(e.toString());
                }
            }
            singleRun();
            if (this.output!= null){
                //double data[] = output.getValue();
                try{
                for (int i=0;i<output.length;i++){                    
                    writer.write(output[i].getValue() + "\t");                    
                }
                writer.write("\n");
                }catch(Exception e){
                        System.out.println(e.toString());
                }
            }
            try{
                writer.close();
            }catch(Exception e){
                System.out.println(e.toString());
            }
        }        
    }       
}
