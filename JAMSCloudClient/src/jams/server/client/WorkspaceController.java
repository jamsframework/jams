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
import jams.JAMSException;
import jams.meta.ModelIO;
import jams.server.entities.Files;
import jams.server.entities.Workspace;
import jams.server.entities.WorkspaceFileAssociation;
import jams.server.entities.Workspaces;
import jams.tools.FileTools;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author christian
 */
public class WorkspaceController {
    private static final Logger log = Logger.getLogger( Controller.class.getName() );            
    FileController fileController = null;
    Controller ctrl = null;
    
    /**
     * @param ctrl Controller interface
     */
    public WorkspaceController(Controller ctrl) {
        this.ctrl = ctrl;

        fileController = ctrl.getFileController();
    }

    /**
     * @return the controller
     */
    public Controller getController(){
        return ctrl;
    }
    
    public Workspace remove(Workspace ws) {
        return (Workspace) ctrl.getClient().httpGet(ctrl.getServerURL() + "/workspace/" + ws.getId() + "/delete", Workspace.class);
    }
    
    public void removeAll() {
        ctrl.getClient().httpGet(ctrl.getServerURL() + "/workspace/reset", String.class);
    }

    public Workspace create(Workspace ws) {
        return (Workspace) ctrl.getClient().httpPost(ctrl.getServerURL() + "/workspace/create", "PUT", ws, Workspace.class);
    }

    public Workspace find(int id) {
        return (Workspace) ctrl.getClient().httpGet(ctrl.getServerURL() + "/workspace/" + id, Workspace.class);
    }
    
    public Workspace ensureExistance(int id, String title) {
        Workspace ws = null;
        if (id != -1){
            ws = this.find(id);            
        }
        if (ws == null){
            ws = new Workspace();            
            ws.setName(title);
            ws = create(ws);
            if (ws == null) {
                return null;
            }
        }        
        return ws;
    }

    public Workspaces findAll(String name) {
        if (name == null) {
            return (Workspaces) ctrl.getClient().httpGet(ctrl.getServerURL() + "/workspace/find", Workspaces.class);
        } else {
            return (Workspaces) ctrl.getClient().httpGet(ctrl.getServerURL() + "/workspace/find?name=" + name, Workspaces.class);
        }
    }

    public Workspace attachFile(Workspace ws, jams.server.entities.File f, int role, String relativePath) {
        String encodedPath = null;
        try {
            encodedPath = URLEncoder.encode(relativePath, "UTF8");
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        return (Workspace) ctrl.getClient().httpGet(ctrl.getServerURL() + "/workspace/" + ws.getId() + "/assign?FILE_ID=" + f.getId() + "&ROLE=" + role + "&RELATIVE_PATH=" + encodedPath, jams.server.entities.Workspace.class);
    }
    
    public Workspace detachFile(Workspace ws, String relativePath) {
        String encodedPath = null;
        try {
            encodedPath = URLEncoder.encode(relativePath, "UTF8");
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        return (Workspace) ctrl.getClient().httpGet(ctrl.getServerURL() + "/workspace/" + ws.getId() + "/detach?RELATIVE_PATH=" + encodedPath, jams.server.entities.Workspace.class);
    }

    public File downloadFile(File target, jams.server.entities.WorkspaceFileAssociation wfa) {
        return ctrl.getClient().download(ctrl.getServerURL() + "/workspace/download/" + wfa.getWorkspace().getId() + "/" + wfa.getID(), target);
    }

    public File downloadWorkspace(File target, Workspace ws) {
        File zip = ctrl.getClient().download(ctrl.getServerURL() + "/workspace/download/" + ws.getId(), target);
        try {
            FileTools.unzipFile(zip, new File(target, ws.getName()), true);
        } catch (IOException ioe) {
            log.log(Level.SEVERE, ioe.toString(), ioe);
        }
        return target;
    }
                    
    private Workspace mapWorkspaceFilesToWorkspace(Workspace ws, Collection<WorkspaceFile> files) {        
        File fileList[] = WorkspaceFile.convertWorkspaceFileToFile(files);        
        Map<File, jams.server.entities.File> mapping = fileController.uploadFile(fileList);

        log.fine(JAMS.i18n("mapping_files_to_workspace"));
        int counter = 0;
        Set<WorkspaceFileAssociation> filesInServerWorkspace = new TreeSet<>();
        filesInServerWorkspace.addAll(ws.getFiles());
        
        Workspace wsTmp = null;
        for (WorkspaceFile wf : files) { 
            if (!wf.getLocalFile().exists()) {
                continue;
            }
            if (wf.getLocalFile().getPath().startsWith("..")){
                log.log(Level.SEVERE, "relative path detected, which cannot be processed " + wf.getLocalFile().getPath());
                continue;
            }
            jams.server.entities.File serverFile = mapping.get(wf.getLocalFile());
            if (serverFile == null) {
                log.log(Level.SEVERE, JAMS.i18n("Unable_to_upload_%1").replace("%1", wf.getLocalFile().toString()));
                continue;
            }
            WorkspaceFileAssociation wfa = ws.getFile(wf.getRelativePath());
                        
            //always reattach, even if it takes some time
            if (wfa!=null){ 
                filesInServerWorkspace.remove(wfa);                
            }
            wsTmp = attachFile(ws, serverFile, wf.getRole(), wf.getRelativePath());
            if (wsTmp != null) {
                ws = wsTmp;
            }
            log.fine(("Mapped %1 of %2 files to workspace")
                    .replace("%1", Integer.toString(counter++))
                    .replace("%2", Integer.toString(files.size()))
                    );
        }   
        //detach files which are not in ws anymore
        for (WorkspaceFileAssociation wfa : filesInServerWorkspace){
            wsTmp = detachFile(ws, wfa.getPath());
            if (wsTmp != null)
                ws = wsTmp;
        }
        log.fine(JAMS.i18n("Upload_of_workspace_is_complete!"));
        return ws;
    }

    private Collection<WorkspaceFile> getClassPathFromManifest(WorkspaceFile jar){
        TreeSet<WorkspaceFile> uploadList = new TreeSet<>();
        try{            
            File directory = jar.getLocalFile().getParentFile();
            JarInputStream jarStream = new JarInputStream(new FileInputStream(jar.getLocalFile()));
            Manifest mf = jarStream.getManifest();
            if (mf == null){
                jarStream.close();
                return uploadList;
            }
            Attributes mainAttributes = mf.getMainAttributes();
            if (mainAttributes==null){
                jarStream.close();
                return uploadList;
            }
            if (mf.getMainAttributes().getValue("Class-Path")==null){
                jarStream.close();
                return uploadList;
            }
            
            String classPaths[] = mf.getMainAttributes().getValue("Class-Path").split(" ");
            for (String path : classPaths){
                File f = new File(directory, path);
                //make sure the file is existing
                if (!f.exists() || f.isDirectory()){
                    System.out.println("Missing library: " + f.getAbsolutePath() + " mentioned in manifest of " + jar.getLocalFile());
                    continue;
                }
                //make relative path
                String parent = FileTools.getParent(jar.getRelativePath());
                String newPath = parent + "/" + path;
                WorkspaceFile wsfile = new WorkspaceFile(f, jar.getRole(), newPath);
                uploadList.add(wsfile);
            }
            jarStream.close();
        }catch(Throwable t){
            t.printStackTrace();            
        }        
        return uploadList;
    }
    
    private Collection<WorkspaceFile> findLibraries(File dir, int role) {
        TreeSet<WorkspaceFile> uploadList = new TreeSet<>();

        //make absolute .. 
        dir = dir.getAbsoluteFile();
        
        if (dir.isDirectory()) {
            Collection<File> libList = FileTools.getFilesByRegEx(dir, ".*\\.(jar)", true);

            Path path = Paths.get(dir.getPath());

            for (File f : libList) {
                Path p = Paths.get(f.getPath());

                String newPath = path.relativize(p).toString();

                WorkspaceFile wsfile = new WorkspaceFile(f, role, newPath);

                uploadList.add(wsfile);
                uploadList.addAll(getClassPathFromManifest(wsfile));
            }
        }else{
            String newPath = dir.getName();
            WorkspaceFile wsfile = new WorkspaceFile(dir, role, newPath);
            uploadList.add(wsfile);
            uploadList.addAll(getClassPathFromManifest(wsfile));
        }
        
        //now read the manifest
        
        
        
        return uploadList;
    }
               
    private WorkspaceFile getWorkspaceFile(File f, File workspace) {
        Path wsDirPath = Paths.get(workspace.getPath());

        Path p = Paths.get(f.getPath());
        p = wsDirPath.relativize(p);

        int role = jams.server.entities.WorkspaceFileAssociation.ROLE_OTHER;

        if (p.startsWith("input/")) {
            role = jams.server.entities.WorkspaceFileAssociation.ROLE_INPUT;
        } else if (p.startsWith("output/")) {
            role = jams.server.entities.WorkspaceFileAssociation.ROLE_OUTPUT;
        } else if (p.endsWith("config.txt")) {
            role = jams.server.entities.WorkspaceFileAssociation.ROLE_CONFIG;
        }

        try {
            if (ModelIO.getStandardModelIO().validateModelFile(f)) {
                role = jams.server.entities.WorkspaceFileAssociation.ROLE_MODEL;
            }
        } catch (JAMSException je) {
        }
        
        return new WorkspaceFile(f, role, p.toString());
    }
    
    private Collection<WorkspaceFile> findWorkspaceFiles(
            File workspaceDirectory,
            File componentLibaries[],
            File jamsLibaries,
            String fileExclusion){
        
        TreeSet<WorkspaceFile> workspaceFileList = new TreeSet<WorkspaceFile>(){
            @Override
            public boolean addAll(Collection<? extends WorkspaceFile> wf){
                boolean result = super.addAll(wf);
                log.fine(JAMS.i18n("Number_of_collected_files_is_%1")
                    .replace("%1", Integer.toString(size())));
                return result;
            }
            @Override
            public boolean add(WorkspaceFile wf){
                boolean result = super.add(wf);
                log.fine(JAMS.i18n("Number_of_collected_files_is_%1")
                    .replace("%1", Integer.toString(size())));
                return result;
            }
        };
                                
        log.fine(JAMS.i18n("Number_of_collected_files_is_%1").replace("%1", "0"));
        //order is crucial 1. add runtime libs
        File[] uiLibs = Utilities.buildFullClassPath(jamsLibaries);

        Path parent = Paths.get(jamsLibaries.getParentFile().getPath());
        
        boolean executableFound = false;
        //order is crucial 1. add runtime libs
        for (File lib : uiLibs) {
            Path p = Paths.get(lib.getPath());
            String newPath = parent.relativize(p).toString();

            if (lib.equals(jamsLibaries)){
                WorkspaceFile wf = new WorkspaceFile(lib, jams.server.entities.WorkspaceFileAssociation.ROLE_EXECUTABLE, newPath);
                executableFound = true;
                if (workspaceFileList.contains(wf)){
                    workspaceFileList.remove(wf);
                }
                workspaceFileList.add(wf);
            }else{
                workspaceFileList.add(new WorkspaceFile(lib, jams.server.entities.WorkspaceFileAssociation.ROLE_RUNTIMELIBRARY, newPath));
            }
        }
        //order is crucial 2. add component libs
        for (File path : componentLibaries) {
            Collection<WorkspaceFile> libList = findLibraries(path, jams.server.entities.WorkspaceFileAssociation.ROLE_COMPONENTSLIBRARY);
            for (WorkspaceFile wf : libList){
                if (workspaceFileList.contains(wf)){
                    WorkspaceFile wf2 = workspaceFileList.ceiling(wf);
                    //replace if newer and not part of the runtime library
                    if (wf2.getRole() != jams.server.entities.WorkspaceFileAssociation.ROLE_RUNTIMELIBRARY &&
                        wf2.getLocalFile().lastModified() < wf.getLocalFile().lastModified() ){
                        workspaceFileList.remove(wf2);
                        workspaceFileList.add(wf);
                    }
                }else{
                    workspaceFileList.add(wf);
                }
            }            
        }
        
        //order is crucial 3. add other files
        Collection<File> file = FileTools.getFilesByRegEx(workspaceDirectory, fileExclusion, false);
        for (File f : file) {
            WorkspaceFile wsfile = getWorkspaceFile(f, workspaceDirectory);            
            workspaceFileList.add(wsfile);            
        }        
                                
        if (!executableFound){
            log.log(Level.SEVERE, "Workspace does not contain any executable file!");
            return null;
        }
        return workspaceFileList;
    }

    public Workspace uploadWorkspace(
            int id,
            String title,
            File workspaceDirectory,
            File componentLibaries[],
            File jamsLibaries,
            String fileExclusion
    ) {

        Workspace ws = ensureExistance(id, title);  
        if (ws == null)
            return null;
        
        Collection<WorkspaceFile> fileList = null;
        fileList = findWorkspaceFiles(workspaceDirectory, componentLibaries, 
                        jamsLibaries, fileExclusion);
        
        log.fine("uploading_%1_files"
                .replace("%1",Integer.toString(fileList.size())));
                
        return mapWorkspaceFilesToWorkspace(ws, fileList);
    }
            
    public DirectorySync getSynchronizationList(File localWsDirectory, Workspace remoteWs) {
        DirectorySync rootSync = new DirectorySync(this, null, localWsDirectory);

        Files files = new Files();
        HashMap<Integer, jams.server.entities.File> lookupTable = new HashMap();
        
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
    
    public boolean synchronizeWorkspace(DirectorySync root){
        log.log(Level.FINE, JAMS.i18n("Start_synchronization_of_%1")
                .replace("%1",root.getLocalFileName()));
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
            log.log(Level.INFO, JAMS.i18n("Synchronization_of_%1_is_completed").replace("%1", root.getLocalFileName()));
        else
            log.log(Level.FINE, JAMS.i18n("Synchronization_of_%1_is_completed").replace("%1", root.getLocalFileName()));
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
        
        File f = null;
        if (root.getSyncMode() == FileSync.SyncMode.CREATE ||
            root.getSyncMode() == FileSync.SyncMode.DUPLICATE ||
            root.getSyncMode() == FileSync.SyncMode.UPDATE){
            f = this.downloadFile(root.getTargetFile(), root.getServerFile());
        }
         
        return f == null;
    }
}