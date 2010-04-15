/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.optimizer;

import jams.data.JAMSDouble;
import jams.data.JAMSString;
import jams.model.JAMSComponent;
import jams.model.JAMSFullModelState;
import jams.model.JAMSVarDescription;
import jams.model.Model;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Christian Fischer
 */
public class JAMSSnapshotExecutor extends JAMSComponent{
    //this enumeration is not nice, but is there a better method?
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSDouble in1;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSString inName1;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSDouble in2;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSString inName2;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSDouble in3;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSString inName3;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSDouble in4;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSString inName4;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSDouble in5;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSString inName5;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSDouble in6;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSString inName6;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSDouble in7;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSString inName7;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSDouble in8;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSString inName8;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSDouble in9;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSString inName9;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSDouble in10;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSString inName10;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSDouble out1;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSString outName1;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSDouble out2;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSString outName2;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSDouble out3;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSString outName3;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSDouble out4;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSString outName4;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSDouble out5;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "parameter input"
            )
            public JAMSString outName5;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "if you dont want to execute the jams model completly in every iteration, you can specify a JAMS - Snapshot which is loaded before execution"
            )
            public JAMSString snapshotFile;
    
    private final int inValueCount = 10;
    private final int outValueCount = 5;
    @Override
    public void run(){
        double inValues[] = new double[inValueCount];
        String inNames[]  = new String[inValueCount];
        
        double outValues[] = new double[outValueCount];
        String outNames[]  = new String[outValueCount];
        
        if (in1 != null)    inValues[0] = in1.getValue();
        if (in2 != null)    inValues[1] = in2.getValue();
        if (in3 != null)    inValues[2] = in3.getValue();
        if (in4 != null)    inValues[3] = in4.getValue();
        if (in5 != null)    inValues[4] = in5.getValue();
        if (in6 != null)    inValues[5] = in6.getValue();
        if (in7 != null)    inValues[6] = in7.getValue();
        if (in8 != null)    inValues[7] = in8.getValue();
        if (in9 != null)    inValues[8] = in9.getValue();
        if (in10!= null)    inValues[9] = in10.getValue();
        
        if (inName1 != null)    inNames[0] = inName1.getValue();
        if (inName2 != null)    inNames[1] = inName2.getValue();
        if (inName3 != null)    inNames[2] = inName3.getValue();
        if (inName4 != null)    inNames[3] = inName4.getValue();
        if (inName5 != null)    inNames[4] = inName5.getValue();
        if (inName6 != null)    inNames[5] = inName6.getValue();
        if (inName7 != null)    inNames[6] = inName7.getValue();
        if (inName8 != null)    inNames[7] = inName8.getValue();
        if (inName9 != null)    inNames[8] = inName9.getValue();
        if (inName10!= null)    inNames[9] = inName10.getValue();
                        
        if (outName1 != null)    outNames[0] = outName1.getValue();
        if (outName2 != null)    outNames[1] = outName2.getValue();
        if (outName3 != null)    outNames[2] = outName3.getValue();
        if (outName4 != null)    outNames[3] = outName4.getValue();
        if (outName5 != null)    outNames[4] = outName5.getValue();
        
        JAMSFullModelState state = null;
        try{
            new JAMSFullModelState(new File(this.snapshotFile.getValue()));
        }catch(ClassNotFoundException e){
            this.getModel().getRuntime().sendHalt("class not found, so that model state could not loaded:" + e.toString());
        }catch(IOException ioe){
            this.getModel().getRuntime().sendHalt("could not read file, so that model state could not loaded:" + ioe.toString());
        }
        Model model = state.getModel();                            
        for (int i=0;i<inValueCount;i++){
            String key = inNames[i];
            double value = inValues[i];
            if (key != null){                
                ((JAMSDouble) model.getRuntime().getDataHandles().get(key)).setValue(value);         
            }
        }
        try{
            model.getRuntime().resume(state.getSmallModelState());                
        }catch(Exception e){
            this.getModel().getRuntime().sendHalt(e.toString());
        }
        
        for (int i=0;i<outValueCount;i++){
            String key = outNames[i];
            if (key != null){                
                outValues[i] = ((JAMSDouble) model.getRuntime().getDataHandles().get(key)).getValue();       
            }
        }
        model = null;
        // collect some garbage ;)
        Runtime.getRuntime().gc();    
        
        if (outName1 != null)    out1.setValue(outValues[0]);
        if (outName2 != null)    out2.setValue(outValues[1]);
        if (outName3 != null)    out3.setValue(outValues[2]);
        if (outName4 != null)    out4.setValue(outValues[3]);
        if (outName5 != null)    out5.setValue(outValues[4]);                
    }
    
}
