/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.tools;

import jams.data.Attribute;
import jams.model.*;
import jams.data.JAMSString;
import jams.runtime.StandardRuntime;
import jams.tools.FileTools;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Christian Fischer
 */
public class JAMSModelSnapshot extends JAMSComponent{                  
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public Attribute.String snapshotFile;
    
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public Attribute.Boolean enable;
                
    public void freeze(){
        if (this.getModel().getRuntime() instanceof StandardRuntime){
            StandardRuntime runtime = (StandardRuntime)this.getModel().getRuntime();
            
            String fileName = null;
            if (snapshotFile != null)
                fileName = FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath() , snapshotFile.getValue());
            //think about it .. after that call the model is NOT in an standardized state
            //.. i am not sure if this fact can 
            //runtime.pause();
            File file = new File(fileName);
            FullModelState state = runtime.getFullModelState();
            try {
                state.writeToFile(file);
            } catch (IOException e) {
                runtime.sendErrorMsg(jams.JAMS.i18n("Unable_to_save_model_state_because,") + e.toString());
                runtime.handle(e, true);
            }    
            /*try{
                runtime.resume(state.getSmallModelState());
            }catch(Exception e){
                runtime.sendErrorMsg(jams.JAMS.i18n("Unable_to_resume_model_execution_because") + e.toString());
                runtime.handle(e, true);
            }*/
        }else{
            this.getModel().getRuntime().sendInfoMsg(jams.JAMS.i18n("Snapshoting_not_supported_by_runtime_"));            
        } 
    }
        
    public void run(){    
        if (enable!=null)
            if (!enable.getValue())
                return;
        freeze();
    }    
}
