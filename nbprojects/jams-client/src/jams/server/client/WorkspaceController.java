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

import jams.server.entities.Workspace;
import jams.server.entities.WorkspaceFileAssociation;
import jams.server.entities.Workspaces;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author christian
 */
public class WorkspaceController extends Controller {

    FileController fileController = null;

    public WorkspaceController(HTTPClient client, String serverUrl) {
        super(client, serverUrl);

        fileController = this.getFileController();
    }
        
    public Workspace remove(Workspace ws) {
        return (Workspace) client.httpGet(serverURL + "/workspace/" + ws.getId() + "/delete", Workspace.class);
    }

    public Workspace create(Workspace ws) {
        return (Workspace) client.httpPost(serverURL + "/workspace/create", "PUT", ws, Workspace.class);
    }
    
    public Workspace find(int id) {
        return (Workspace) client.httpGet(serverURL + "/workspace/" + id, Workspace.class);
    }
    
    public Workspaces findAll(String name) {
        if (name==null)
            return (Workspaces) client.httpGet(serverURL + "/workspace/find", Workspaces.class);
        else
            return (Workspaces) client.httpGet(serverURL + "/workspace/find?name="+name, Workspaces.class);
    }

    public Workspace assignFile(Workspace ws, jams.server.entities.File f, int role, String relativePath) {
        String encodedPath = null;
        try {
            encodedPath = URLEncoder.encode(relativePath, "UTF8");
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        return (Workspace) client.httpGet(serverURL + "/workspace/"+ws.getId()+"/assign?FILE_ID=" + f.getId() + "&ROLE=" + role + "&RELATIVE_PATH=" + encodedPath, jams.server.entities.Workspace.class);
    }
    
    public Workspace detachFile(Workspace ws, String relativePath) {
        String encodedPath = null;
        try {
            encodedPath = URLEncoder.encode(relativePath, "UTF8");
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        return (Workspace) client.httpGet(serverURL + "/workspace/"+ws.getId()+"/detach?RELATIVE_PATH=" + encodedPath, jams.server.entities.Workspace.class);
    }
    
    public File downloadFile(File target, Workspace ws, jams.server.entities.File f) {
        //return (Object) client.httpGet(serverURL + "/workspace/download/file?WORKSPACE_ID=" + ws.getId() + "&FILE_ID=" + f.getId(), Object.class);
        return client.download(serverURL + "/workspace/download/"+ws.getId() + "/" + f.getId(), target);
    }
    
    public File downloadWorkspace(File target, Workspace ws) {   
        File zip = client.download(serverURL + "/workspace/download/"+ws.getId(), target);
        try{
            unzip(zip, target);
        }catch(IOException ioe){
            log(Level.SEVERE, ioe.toString(), ioe);
        }
        return target;
    }
    
    public class WorkspaceFile {

        File localFile;
        String relativPath;
        int role;

        public WorkspaceFile(File localFile, int role, String relativePath) {
            this.localFile = localFile;
            this.relativPath = relativePath;
            this.role = role;
        }
    }
    
    private List<File> recursiveFindFiles(File directory, List<File> list, String regex) {        
        for (File f : directory.listFiles()) {
            if (f.isDirectory()) {
                recursiveFindFiles(f, list, regex);              
            } else if (regex==null || f.getName().toLowerCase().matches(regex)) {
                list.add(f);
            }
        }
        return list;
    }

    private ArrayList<WorkspaceFile> collectLibs(File dir, int role) {
        ArrayList<WorkspaceFile> uploadList = new ArrayList<WorkspaceFile>();
        
        List<File> libList = recursiveFindFiles(dir, new ArrayList<File>(), ".*\\.(jar)");

        Path path = Paths.get(dir.getPath());
        
        for (File f : libList) {
            Path p = Paths.get(f.getPath());

            String newPath = path.relativize(p).toString();
                        
            if (role == jams.server.entities.WorkspaceFileAssociation.ROLE_COMPONENTSLIBRARY)
                newPath = "/componentsLib/" + newPath;
            else
                newPath = "/runtimeLib/" + newPath;
                
            WorkspaceFile wsfile = new WorkspaceFile(f,role,newPath);

            uploadList.add(wsfile);
        }
        return uploadList;
    }

    public Workspace uploadWorkspace(String name, List<WorkspaceFile> files) {
        Workspace ws = new Workspace();
        ws.setName(name);
        ws = create(ws);
        if (ws == null) {
            return null;
        }

        WorkspaceFile wsFileListArray[] = files.toArray(new WorkspaceFile[0]);
        File list[] = new File[wsFileListArray.length];
        for (int i = 0; i < list.length; i++) {
            list[i] = wsFileListArray[i].localFile;
        }
        
        Map<File,jams.server.entities.File> mapping = fileController.uploadFile(list);
        
        for (WorkspaceFile wf: files) {        
            if (!wf.localFile.exists() || wf.localFile.getPath().startsWith("..")){
                continue;
            }
            jams.server.entities.File serverFile = mapping.get(wf.localFile);
            if (serverFile == null) {
                log(Level.SEVERE, "Couldn't upload file: " + wf.localFile);
            }
            ws = assignFile(ws, serverFile, wf.role, wf.relativPath);
        }
        return ws;
    }

    public Workspace uploadWorkspace(String name, File wsDirectory, File modelFile, File componentsLibPath[], File runtimeLibPath[], String fileExclusion) {

        List<File> wsFileList = recursiveFindFiles(wsDirectory, new ArrayList<File>(), fileExclusion);

        if (modelFile == null || !modelFile.exists()) {
            return null;
        }

        ArrayList<WorkspaceFile> fullUploadList = new ArrayList<>();

        Path wsDirPath = Paths.get(wsDirectory.getPath());
                    
        for (File f : wsFileList) {
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

            WorkspaceFile wsfile = new WorkspaceFile(f, role, p.toString());
            fullUploadList.add(wsfile);
        }
        for (File path : componentsLibPath) {
            fullUploadList.addAll(collectLibs(path, jams.server.entities.WorkspaceFileAssociation.ROLE_COMPONENTSLIBRARY));
        }
        for (File path : runtimeLibPath) {
            fullUploadList.addAll(collectLibs(path, jams.server.entities.WorkspaceFileAssociation.ROLE_RUNTIMELIBRARY));
        }
        
        WorkspaceFile modelfile2 = new WorkspaceFile(modelFile, 
                jams.server.entities.WorkspaceFileAssociation.ROLE_MODEL, 
                wsDirPath.relativize(Paths.get(modelFile.getPath())).toString());
        
        fullUploadList.add(modelfile2);
        
        return uploadWorkspace(name, fullUploadList);
    }
            
    private File buildDirectoryHierarchyFor(String entryName, File destDir) {
        entryName = entryName.replace("\\", "/");
        int lastIndex = entryName.lastIndexOf('/');
        //String entryFileName = entryName.substring(lastIndex + 1);
        String internalPathToEntry = entryName.substring(0, lastIndex + 1);
        //System.out.println("extract to_:" + internalPathToEntry + " file:" + entryFileName);
        return new File(destDir, internalPathToEntry);
    }
    
    private void unzip(File zip, File destDir)throws IOException {
        if (!destDir.exists()) {
            destDir.mkdir();
        }

        ZipInputStream zipFile = new ZipInputStream(new FileInputStream(zip));
        byte[] buffer = new byte[16384];
        int len;
        ZipEntry entry = null;
        while ((entry = zipFile.getNextEntry()) != null) {
            String entryFileName = entry.getName();
            entryFileName = entryFileName.replace("\\", "/");
            File dir = buildDirectoryHierarchyFor(entryFileName, destDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!entry.isDirectory()) {
                BufferedOutputStream bos = new BufferedOutputStream(
                        new FileOutputStream(new File(destDir, entryFileName)));

                while ((len = zipFile.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }

                bos.flush();
                bos.close();
            }
        }
        zipFile.close();
        zip.delete();
    }        
}
