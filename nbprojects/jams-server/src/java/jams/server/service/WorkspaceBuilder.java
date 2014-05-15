/*
 * WorkspaceHandler.java
 * Created on 23.04.2014, 13:31:28
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

package jams.server.service;

import jams.server.entities.Workspace;
import jams.server.entities.WorkspaceFileAssociation;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author christian
 */
public class WorkspaceBuilder {
    private void zipFile(java.io.File file, String fileName, ZipOutputStream zipOut) throws IOException {
        FileInputStream inFile = new FileInputStream(file);
        zipOut.putNextEntry(new ZipEntry(fileName));

        byte[] buf = new byte[65536];
        int len;
        // Der Inhalt der Datei wird in die Zip-Datei kopiert.
        while ((len = inFile.read(buf)) > 0) {
            zipOut.write(buf, 0, len);
        }
        zipOut.closeEntry();
        inFile.close();
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm");
    private String getJAPContent(boolean gui){
        return "#JAMS configuration file\n" +  
                         sdf.format(new Date()) + "\n" + 
                         "infolog=\n" + 
                        "libs=" + "comp_lib/;\n" + 
                        (gui ? "guiconfig=1\n" : "guiconfig=0\n") + 
                        "model=\n" + 
                        "charset=\n" + 
                        "username=\n" + 
                        (gui ? "windowenable=1\n" : "windowenable=0\n") + 
                        (gui ? "errordlg=1\n" : "errordlg=0\n") + 
                        "windowontop=0\n"+
                        "windowheight=600\n"+
                        "forcelocale=\n"+
                        "verbose=1\n"+
                        "debug=3\n"+
                        "guiconfigheight=600\n"+
                        "windowwidth=900\n"+
                        "guiconfigwidth=600\n"+
                        "errorlog=\n"+
                        "helpbaseurl=\n";
    }    
    private java.io.File[] buildJAPFile(java.io.File f) throws IOException{      
        java.io.File japFile1 = new java.io.File(f + "/default.jap");
        java.io.File japFile2 = new java.io.File(f + "/nogui.jap");
        
        BufferedWriter bos = new BufferedWriter(new FileWriter(japFile1.getAbsoluteFile()));
        bos.write(getJAPContent(true));
        bos.close();
        
        bos = new BufferedWriter(new FileWriter(japFile2.getAbsoluteFile()));
        bos.write(getJAPContent(false));
        bos.close();
        
        return new java.io.File[]{japFile1, japFile2};
    }
    
    private String batContent(boolean windows, boolean x64, boolean gui, String modelFile){
        if (windows) {
            return "@echo off\n"
                    + "SET platform=" + (x64 ? "win64" : "win32") + "\n"
                    + "SET VM=java\n"
                    + "SET OPTIONS=-Xms128M -Xmx1024M -XX:MaxPermSize=128m -Dsun.java2d.d3d=false -Djava.library.path=bin/%platform% -splash:\n"
                    + "@echo on\n"
                    + "%VM% %OPTIONS% -jar runtime_lib/jams-ui.jar " + (gui ? "-c runtime_lib/default.jap" : "-c runtime_lib/nogui.jap") + " -n -m " + modelFile + " "
                    + (gui ? "" : ">info.log 2>&1&");
        }else{
            if (gui)
                return "java -Xms128M -Xmx2096M -Dsun.java2d.d3d=false -jar runtime_lib/jams-ui.jar -c runtime_lib/default.jap" + " -m " + modelFile;
            else{
                return "java -Xms128M -Xmx2096M -Dsun.java2d.d3d=false -jar runtime_lib/jams-ui.jar -c runtime_lib/nogui.jap -n -m " + modelFile + ">info.log 2>&1&";
            }
        }
    }
    
    private void writeFile(String f, String content) throws IOException{
        BufferedWriter bos = new BufferedWriter(new FileWriter(f));
        bos.write(content);
        bos.close();
    }
    
    private java.io.File[] buildStartFiles(java.io.File f, String modelFile) throws IOException{
        writeFile(f + "/win32.bat", batContent(true, false, true, modelFile));
        writeFile(f + "/win64.bat", batContent(true, true, true, modelFile));
        
        writeFile(f + "/win32_nogui.bat", batContent(true, false, false, modelFile));
        writeFile(f + "/win64_nogui.bat", batContent(true, true, false, modelFile));

        writeFile(f + "/start.sh", batContent(false, false, true, modelFile));
        writeFile(f + "/start_nogui.sh", batContent(false, true, false, modelFile));                             
        
        return new java.io.File[]{
            new java.io.File(f + "/win32.bat"),
            new java.io.File(f + "/win64.bat"),
            new java.io.File(f + "/win32_nogui.bat"),
            new java.io.File(f + "/win64_nogui.bat"),
            new java.io.File(f + "/start.sh"),
            new java.io.File(f + "/start_nogui.sh"),            
        };
    }
    
    java.io.File zipWorkspace(Workspace ws) throws IOException {
        //get model file
        List<WorkspaceFileAssociation> files = ws.getAssociatedFiles();
        
        WorkspaceFileAssociation modelFile = null;
        for (WorkspaceFileAssociation wfa : files) {
            if (wfa.getRole() == WorkspaceFileAssociation.ROLE_MODEL){
                modelFile = wfa;
                break;
            }
        }
        
        if (modelFile == null){
            return null;
        }
        
        java.io.File tmpDir = new java.io.File(ApplicationConfig.SERVER_TMP_DIRECTORY + "/workspace_" + ws.getId());
        tmpDir.mkdirs();
        
        java.io.File JAPFile[] = buildJAPFile(tmpDir);    
        java.io.File startFile[] = buildStartFiles(tmpDir, modelFile.getPath());
        
        final java.io.File zipFile = new java.io.File(tmpDir.getAbsolutePath() + "ws.zip");
        ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(zipFile));
                
        zipFile(JAPFile[0], "/runtime_lib/" + JAPFile[0].getName(), stream);
        zipFile(JAPFile[1], "/runtime_lib/" + JAPFile[1].getName(), stream);
                                
        for (java.io.File f : startFile){
            zipFile(f, "/"  +f.getName(), stream);
        }
        
        for (WorkspaceFileAssociation wfa : files) {
            final java.io.File file = new java.io.File(wfa.getFile().getLocation());
            if (wfa.getRole() == WorkspaceFileAssociation.ROLE_RUNTIMELIBRARY) {
                zipFile(file, "/runtime_lib/" + wfa.getPath(), stream);
            } else if (wfa.getRole() == WorkspaceFileAssociation.ROLE_COMPONENTSLIBRARY) {
                zipFile(file, "/comp_lib/" + wfa.getPath(), stream);
            } else {
                zipFile(file, wfa.getPath(), stream);
            }
        }        
                
        stream.close();
        
        return zipFile;
    }
    
    StreamingOutput streamFile(java.io.File f) throws IOException {                        
        return new StreamFile(f, false);
    }
    
    StreamingOutput streamWorkspace(Workspace ws) throws IOException {        
        final java.io.File zipFile = zipWorkspace(ws);
                
        return new StreamFile(zipFile, true);
    }
    
    private class StreamFile implements StreamingOutput {

        java.io.File f = null;
        boolean deleteAfterClose = false;
        public StreamFile(java.io.File f, boolean deleteAfterClose) {
            super();
            this.f = f;
            this.deleteAfterClose = deleteAfterClose;
        }

        public void write(OutputStream arg0) throws IOException, WebApplicationException {
            BufferedOutputStream bus = new BufferedOutputStream(arg0);
            try {
                FileInputStream fizip = new FileInputStream(f);
                byte buffer[] = new byte[65536];
                int fread = 0;
                while ((fread = fizip.read(buffer)) > 0) {
                    bus.write(buffer, 0, fread);
                    bus.flush();
                }
                bus.close();
                fizip.close();
                if (deleteAfterClose)
                    f.delete();
            } catch (Exception e) {
                //TODO .. error handling??
                e.printStackTrace();
            }
        }
    }        
    
    private File buildDirectoryHierarchyFor(String entryName, File destDir) {
        entryName = entryName.replace("\\", "/");
        int lastIndex = entryName.lastIndexOf('/');
        //String entryFileName = entryName.substring(lastIndex + 1);
        String internalPathToEntry = entryName.substring(0, lastIndex + 1);
        //System.out.println("extract to_:" + internalPathToEntry + " file:" + entryFileName);
        return new File(destDir, internalPathToEntry);
    }
    
    void unzip(File zip, File destDir)throws IOException {
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
