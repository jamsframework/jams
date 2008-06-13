/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.tools;

import org.unijena.jams.model.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import org.unijena.jams.data.JAMSBoolean;
import org.unijena.jams.data.JAMSEntity;
import org.unijena.jams.data.JAMSString;
import org.unijena.jams.runtime.StandardRuntime;

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
            public JAMSBoolean snapshotFile;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSEntity data;
    
    ByteArrayOutputStream snapshot = null;
    
    public void init() {          
    }
    
    public void run(){
        if (this.getModel().getRuntime() instanceof StandardRuntime){
            StandardRuntime runtime = (StandardRuntime)this.getModel().getRuntime();
            
            String fileName = null;
            if (dirName != null && snapshotFile != null)
                fileName = dirName.getValue() + "/" + snapshotFile.getValue();
            
            if (takeSnapshot != null && takeSnapshot.getValue()){  
                runtime.sendInfoMsg("Taking Snapshot");
                snapshot = runtime.GetRuntimeState(fileName);
                data.setObject("snapshot", snapshot);
            }
            if (loadSnapshot != null && loadSnapshot.getValue()){
                byte snapShotByteArray[] = null;
                if (fileName != null){
                    try{
                        FileInputStream fis = new FileInputStream(fileName);
                        snapShotByteArray = new byte[fis.available()];
                        fis.read(snapShotByteArray);
                        fis.close();                       
                    }catch(Exception e){
                        this.getModel().getRuntime().println("Could not open or read snapshot file, because " + e.toString());
                    }
                }else{
                    if (data != null){
                        if (data.existsAttribute("snapshot")){
                            try{
                                snapshot = (ByteArrayOutputStream)data.getObject("snapshot");
                            }catch(Exception e){
                                
                            }
                        }
                    }
                    snapShotByteArray = snapshot.toByteArray();
                }                
                runtime.sendInfoMsg("Restoring Snapshot");
                runtime.SetRuntimeState(new ByteArrayInputStream(snapShotByteArray));
            }
        }else{
            this.getModel().getRuntime().println("Snapshoting not supported by runtime!");                        
        }       
    }
}
