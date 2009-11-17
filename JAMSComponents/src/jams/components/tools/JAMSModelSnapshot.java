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
            public JAMSBoolean saveIterator;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSBoolean enable;
                
    public void freeze(){
        if (this.getModel().getRuntime() instanceof StandardRuntime){
            StandardRuntime runtime = (StandardRuntime)this.getModel().getRuntime();
            
            String fileName = null;
            if (snapshotFile != null)
                fileName = JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath() , snapshotFile.getValue());
            
            if (takeSnapshot != null && takeSnapshot.getValue()){                  
                runtime.sendInfoMsg("Taking Snapshot" + " (" + this.getInstanceName() + ")");
                Snapshot snapshot = this.getModel().getModelState(holdInMemory != null && holdInMemory.getValue(),
                        fileName);                               
                data.setObject("snapshot", snapshot);
            }
            if (loadSnapshot != null && loadSnapshot.getValue()){  
                Snapshot snapshot = null;            
                if (snapshotFile == null){                    
                    try{
                        snapshot = (Snapshot)data.getObject("snapshot");
                    }catch(Exception e){
                        System.out.println("Entity does not contain any snapshot-data," + e.toString());
                    }
                }else{
                    snapshot = new JAMSSnapshot(fileName);
                }
                runtime.sendInfoMsg("Restoring Snapshot" + " (" + this.getInstanceName() + ")");
                if (saveIterator!=null){
                    if (saveIterator.getValue())
                        this.getModel().setModelState(snapshot,true);
                    else
                        this.getModel().setModelState(snapshot,false);
                }else
                    this.getModel().setModelState(snapshot,false);                
            }
        }else{
            this.getModel().getRuntime().println("Snapshoting not supported by runtime!");                        
        } 
    }
        
    public void run(){    
        if (enable!=null)
            if (!enable.getValue())
                return;
        freeze();
    }    
}
