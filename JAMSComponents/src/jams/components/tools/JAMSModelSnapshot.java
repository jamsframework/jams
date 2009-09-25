/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.tools;

import jams.tools.JAMSTools;
import jams.model.*;
import jams.data.JAMSBoolean;
import jams.data.JAMSEntity;
import jams.data.JAMSInteger;
import jams.data.JAMSString;
import jams.runtime.StandardRuntime;

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
            public JAMSBoolean takeSnapshot;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSBoolean loadSnapshot;
      
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Workspace directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSString snapshotFile;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSEntity data;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSBoolean holdInMemory;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSInteger freezeState;
        
    public void freeze(){
        if (this.getModel().getRuntime() instanceof StandardRuntime){
            StandardRuntime runtime = (StandardRuntime)this.getModel().getRuntime();
            
            String fileName = null;
            if (dirName != null && snapshotFile != null)
                fileName = JAMSTools.CreateAbsoluteFileName(dirName.getValue() , snapshotFile.getValue());
            
            if (takeSnapshot != null && takeSnapshot.getValue()){                  
                runtime.sendInfoMsg("Taking Snapshot" + " (" + this.getInstanceName() + ")");
                Snapshot snapshot = this.getModel().getModelState(holdInMemory != null && holdInMemory.getValue(),
                        fileName,this);                               
                data.setObject("snapshot", snapshot);
            }
            if (loadSnapshot != null && loadSnapshot.getValue()){                
                Snapshot snapshot = null;
                try{
                    snapshot = (Snapshot)data.getObject("snapshot");
                }catch(Exception e){
                    System.out.println("Entity does not contain any snapshot-data," + e.toString());
                }
                runtime.sendInfoMsg("Restoring Snapshot" + " (" + this.getInstanceName() + ")");
                this.getModel().setModelState(snapshot);
            }
        }else{
            this.getModel().getRuntime().println("Snapshoting not supported by runtime!");                        
        } 
    }
    
    public void init() {          
        if (freezeState.getValue() == 0)
            freeze();
    }
    
    public void run(){        
        if (freezeState.getValue() == 1)
            freeze();
    }
    
    public void cleanup(){        
        if (freezeState.getValue() == 2)
            freeze();
    }
}
