/*
 * UserController.java
 * Created on 20.04.2014, 14:46:13
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.server.client;

import jams.JAMS;
import jams.server.client.sync.FileSync;
import jams.server.client.sync.DirectorySync;
import jams.server.entities.Files;
import jams.server.entities.Workspace;
import jams.server.entities.WorkspaceFileAssociation;
import jams.server.entities.WorkspaceFileAssociations;
import jams.server.entities.Workspaces;
import jams.tools.FileTools;
import static jams.tools.LogTools.log;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import javax.ws.rs.ProcessingException;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class WorkspaceController {  
    private final FileController fileController;
    private final HTTPClient client;
    private final String urlStr;
    
    /**
     * @param ctrl Controller interface
     */
    public WorkspaceController(Controller ctrl) {
        client = ctrl.getClient();
        urlStr = ctrl.getServerURL();
        fileController = ctrl.files();
    }
    
    /**
     * deletes a workspace
     * @param ws that should be deletes
     * @return the deleted workspace
     */
    public Workspace delete(Workspace ws) {
        log(getClass(), Level.FINE, JAMS.i18n("Delete_workspace_with_id:{0}"),ws.getId());
        return client.httpGet(urlStr + "/workspace/" + ws.getId() + "/delete", Workspace.class);
    }
    
    /**
     * deletes all workspaces of the user
     */
    public void deleteAll() {
        log(getClass(), Level.FINE, JAMS.i18n("Delete_all_workspace_of_the_user."));
        client.httpGet(urlStr + "/workspace/reset", String.class);
    }

    /**
     * creates a new workspace
     * @param ws to be created (id is not valid at this time)
     * @return the newly created workspace
     */
    public Workspace create(Workspace ws) {
        log(getClass(), Level.FINE, JAMS.i18n("creating_a_new_workspace."));
        return client.httpPost(urlStr + "/workspace/create", "PUT", ws, Workspace.class);
    }

    /**
     * find the workspace based on the id
     * @param id of the workspace to be found
     * @return the workspace with that id or an empty list
     */
    public Workspaces find(int id) {
        log(getClass(), Level.FINE, JAMS.i18n("searching_for_workspace_with_id_{0}"),id);
        return client.httpGet(urlStr + "/workspace/" + id, Workspaces.class);
    }
    
    /**
     * ensures the existance of the workspace with id and title. if it is not
     * existing a new workspace is created
     * @param id
     * @param name
     * @return the workspace with the desired properties
     */
    public Workspace ensureExistance(int id, String name) {
        log(getClass(), Level.FINE, JAMS.i18n("ensures_existance_of_workspace_with_id_{0}_and_name_{1}"),id, name);        
        Workspaces workspaces = null;
        if (id > 0){
            workspaces = this.find(id);
        }
        
        if (workspaces == null || workspaces.getWorkspaces().isEmpty()){
            Workspace ws = new Workspace();            
            ws.setName(name);
            ws = create(ws);
            return ws;
        }        
        return workspaces.getWorkspaces().get(0);
    }

    /**
     * finds all workspaces with a given name
     * @param name of the workspace to be found
     * @return all workspaces with that name
     */
    public Workspaces findAll(String name) {
        log(getClass(), Level.FINE, JAMS.i18n("searching_for_all_workspaces__named_{0}"),name);        
        if (name == null) {
            return client.httpGet(urlStr + "/workspace/find", Workspaces.class);
        } else {
            return client.httpGet(urlStr + "/workspace/find?name=" + name, Workspaces.class);
        }

    }

    /**
     * attaches a file to a workspace
     * @param ws workspace to which the file should be attached
     * @param wfa file to be attached
     * @return the modified workspace object
     */
    public Workspace attachFile(Workspace ws, WorkspaceFileAssociation wfa) {
        log(getClass(), Level.FINE, JAMS.i18n("attaches_the_file_{0}_to_the_workspace_{1}"),wfa.getFile(),ws.getName());   
        ArrayList wfas = new ArrayList<>();
        wfas.add(wfa);
        return attachFiles(ws, wfas);
    }
    
    /**
     * attaches several files to a workspace
     * @param ws workspace to which the file should be attached
     * @param wfas file that should be attached to the workspace
     * @return the modified workspace object
     */
    public Workspace attachFiles(Workspace ws, Collection<WorkspaceFileAssociation> wfas) {
        //log(getClass(), Level.FINE, JAMS.i18n("attaches_the_file_{0}_to_the_workspace_{1}"),wfa.getFile(),ws.getName());     
        WorkspaceFileAssociations request = new WorkspaceFileAssociations(wfas);        
        return client.httpPost(urlStr + "/workspace/" + ws.getId() + "/assign", "POST", request, Workspace.class);
    }
    
    /*private Workspace mapWorkspaceFilesToWorkspace(Workspace ws, List<WorkspaceFile> files) throws IOException{            
        Map<File, jams.server.entities.File> mapping = fileController.uploadFile(
                new AbstractDataSupplier<File, List<WorkspaceFile>>(files) {

                    @Override
                    public int size() {
                        return input.size();
                    }

                    @Override
                    public File get(int i) {
                        return input.get(i).getLocalFile();
                    }
                }
        );

        log(getClass(),Level.FINE,JAMS.i18n("mapping_files_to_workspace"));
        int counter = 0;
        Set<WorkspaceFileAssociation> filesInServerWorkspace = new TreeSet<>();
        filesInServerWorkspace.addAll(ws.getFiles());
        
        for (WorkspaceFile wf : files) { 
            if (!wf.getLocalFile().exists()) {
                continue;
            }
            if (wf.getLocalFile().getPath().startsWith("..")){
                log(getClass(),Level.SEVERE, "relative path detected, which cannot be processed {0}", wf.getLocalFile().getPath());
                continue;
            }
            jams.server.entities.File serverFile = mapping.get(wf.getLocalFile());
            if (serverFile == null) {
                log(getClass(),Level.SEVERE, JAMS.i18n("Unable_to_upload_%1").replace("%1", wf.getLocalFile().toString()));
                continue;
            }
            WorkspaceFileAssociation wfa = ws.getFile(wf.getRelativePath());
                        
            //always reattach, even if it takes some time
            if (wfa!=null){ 
                filesInServerWorkspace.remove(wfa);                
            }
            try{
                ws = attachFile(ws, serverFile, wf.getRole(), wf.getRelativePath());
            }catch(ProcessingException pe){
                log(getClass(),Level.WARNING, serverFile + " was not mapped to workspace ", pe);
            }
            log(getClass(),Level.FINE, ("Mapped %1 of %2 files to workspace")
                    .replace("%1", Integer.toString(counter++))
                    .replace("%2", Integer.toString(files.size()))
                    );
        }   
        //detach files which are not in ws anymore
        for (WorkspaceFileAssociation wfa : filesInServerWorkspace){
            ws = detachFile(ws, wfa.getPath());            
        }
        log(getClass(),Level.FINE, JAMS.i18n("Upload_of_workspace_is_complete!"));
        return ws;
    }*/
    
    /**
     * detaches a file from a workspace
     * @param ws workspace from which a file should be detached
     * @param relativePath path to that file within the workspace
     * @return the modified workspace object 
     */
    public Workspace detachFile(Workspace ws, String relativePath) {
        log(getClass(), Level.FINE, JAMS.i18n("detaches_the_file_{0}_from_the_workspace_{1}"),relativePath,ws.getName()); 
        String encodedPath;
        try {
            encodedPath = URLEncoder.encode(relativePath, "UTF8");
        } catch (UnsupportedEncodingException uee) {
            throw new ProcessingException(uee);
        }
        return client.httpGet(urlStr + "/workspace/" + ws.getId() + "/detach?"+
                "RELATIVE_PATH=" + encodedPath, jams.server.entities.Workspace.class);
    }

    /**
     * detach all files from a workspace
     * @param ws workspace to which the file should be attached
     * @return the modified workspace object
     */
    public Workspace detachAll(Workspace ws) {
        log(getClass(), Level.FINE, JAMS.i18n("detaches_all_files_from_the_workspace_{0}"),ws.getName());     
        return client.httpGet(urlStr + "/workspace/" + ws.getId() + "/detachAll", Workspace.class);
    }
    
    /**
     * downloads a file to a target location
     * @param target to which the file should be saved
     * @param wfa the file to be downloaded
     * @return the target file
     * @throws IOException
     */
    public File downloadFile(File target, jams.server.entities.WorkspaceFileAssociation wfa) throws IOException{
        log(getClass(), Level.FINE, JAMS.i18n("downloading_{0}_to_{1}"),wfa.getFileName(), target.getAbsoluteFile()); 
        return client.download(urlStr + "/workspace/download/" + wfa.getWorkspace().getId() + "/" + wfa.getID(), target);
    }

    /**
     * downloads a workspace to a target location
     * an intermediate zip containing the workspace is created and deleted
     * when the operation has finished
     * 
     * @param target to which the worksapce should be saved
     * @param ws the workspace to be downloaded
     * @return the target file
     * @throws IOException
     */
    public File downloadWorkspace(File target, Workspace ws) throws IOException{
        log(getClass(), Level.FINE, JAMS.i18n("downloading_workspace_{0}_to_{1}"),ws.getId(), target.getAbsoluteFile()); 
        File zip = client.download(urlStr + "/workspace/download/" + ws.getId(), target);
        FileTools.unzipFile(zip, new File(target, ws.getName()), true);
        return target;
    }
            
    /**
     *
     * @param localWsDirectory
     * @param remoteWs
     * @return
     */
    public DirectorySync getSynchronizationList(File localWsDirectory, Workspace remoteWs) {
        DirectorySync rootSync = new DirectorySync(this, null, localWsDirectory);

        Files files = new Files();
        HashMap<Integer, jams.server.entities.File> lookupTable = new HashMap<>();
        
        for (WorkspaceFileAssociation wfa : remoteWs.getFiles()) {
            files.add(wfa.getFile());            
        }
        
        files = fileController.getHashCode(files);
        for (jams.server.entities.File f : files.getFiles()){           
            lookupTable.put(f.getId(), f);
        }
                
        for (WorkspaceFileAssociation wfa : remoteWs.getFiles()) {
            //need to take this file, because wfa does not necessarly know the hash code
            jams.server.entities.File f = lookupTable.get(wfa.getFile().getId());
            //set the workspace because this is not valid, when it is returned
            if (wfa.getWorkspace()==null){
                wfa.setWorkspace(remoteWs);
            }
            wfa.getFile().setHash(f.getHash());
            rootSync.createSyncEntry(wfa.getPath(), wfa);            
        }

        rootSync.updateSyncMode();
        
        return rootSync;
    }
    
    /**
     *
     * @param root
     * @return
     */
    public boolean synchronizeWorkspace(DirectorySync root) {
        log(getClass(), Level.FINE, JAMS.i18n("Start_synchronization_of_{0}"), 
                root.getLocalFileName());
        if (!root.isDoSync())
            return true;
        
        if (root.getSyncMode() == FileSync.SyncMode.NOTHING){
            return true;
        }
        
        if (root.getSyncMode() == FileSync.SyncMode.CREATE ||
            root.getSyncMode() == FileSync.SyncMode.DUPLICATE){
            if (!root.getTargetFile().mkdirs()){
                return false;
            }
        }
        
        if (root.getSyncMode() == FileSync.SyncMode.UPDATE){
            //do nothing
        }
        
        boolean success = true;
        for (FileSync fs : root.getChildren()){
            success &= synchronizeWorkspace(fs);
        }
        if (root.getParent()==null)
            log(getClass(),Level.INFO, JAMS.i18n("Synchronization_of_%1_is_completed").replace("%1", root.getLocalFileName()));
        else
            log(getClass(),Level.FINE, JAMS.i18n("Synchronization_of_%1_is_completed").replace("%1", root.getLocalFileName()));
        return success;
    }
    
    private boolean synchronizeWorkspace(FileSync root){
        if (root instanceof DirectorySync){
            return synchronizeWorkspace((DirectorySync)root);
        }
        
        if (!root.isDoSync())
            return true;
        
        if (root.getSyncMode() == FileSync.SyncMode.NOTHING){
            return true;
        }
        
        if (root.getSyncMode() == FileSync.SyncMode.CREATE ||
            root.getSyncMode() == FileSync.SyncMode.DUPLICATE ||
            root.getSyncMode() == FileSync.SyncMode.UPDATE){
            try{
                this.downloadFile(root.getTargetFile(), root.getServerFile());
            }catch(IOException ioe){
                log(getClass(),Level.SEVERE, "Unable to download " + root.getServerFile().getFileName(), ioe);
                return false;
            }
        }
        return true;
    }
    
    public FileController getFileController(){
        return fileController;
    }
}