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

import jams.server.entities.User;
import jams.server.entities.Workspace;
import jams.server.entities.WorkspaceFileAssociation;
import jams.tools.FileTools;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author christian
 */
public class Utilities {        
    
    public static java.io.File zipWorkspace(File targetDirectory, Workspace ws) throws IOException, ZipException {
        //get model file
        List<WorkspaceFileAssociation> files = ws.getFiles();        
        targetDirectory.mkdirs();
        
//        java.io.File JAPFile = adaptJAPFile(targetDirectory, ws);    
                
        final java.io.File zipFile = new java.io.File(targetDirectory.getAbsolutePath() + "/ws.zip");
        ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(zipFile));
                                                                
//        String jamsUIPath, jamsUIDirectory = "";
        
        for (WorkspaceFileAssociation wfa : files) {
//            //ignore existing jap.file
//            if (wfa.getFileName().equalsIgnoreCase(JAPFile.getName())){
//                continue;
//            }
//            if (    (
//                    wfa.getPath().toLowerCase().endsWith("jams-ui.jar") ||
//                    wfa.getPath().toLowerCase().endsWith("jams-starter.jar") ||
//                    wfa.getPath().toLowerCase().endsWith("juice-starter.jar")
//                    ) && wfa.getRole() == WorkspaceFileAssociation.ROLE_EXECUTABLE){
//                jamsUIPath = wfa.getPath();      
//                int lastSlash = Math.max(wfa.getPath().lastIndexOf("/"),  wfa.getPath().lastIndexOf("\\"));
//                if (lastSlash != -1){                    
//                    jamsUIDirectory = jamsUIPath.substring(0, lastSlash+1);
//                }
//                                
//                FileTools.zipFile(JAPFile, JAPFile.getName(), stream);
//            }
            final java.io.File file = new java.io.File(wfa.getFile().getLocation());
//            if (wfa.getRole() == WorkspaceFileAssociation.ROLE_RUNTIMELIBRARY) {
                FileTools.zipFile(file, wfa.getPath(), stream);
//            } else if (wfa.getRole() == WorkspaceFileAssociation.ROLE_COMPONENTSLIBRARY) {
//                FileTools.zipFile(file, wfa.getPath(), stream);
//            } else {
//                FileTools.zipFile(file, wfa.getPath(), stream);
//            }
        }        
        
        stream.close();
        
        return zipFile;
    }
                                            
//    private static String getJAPContent(Workspace ws){
//        String libPath = "";
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm");
//        File japFile = null;
//        
//        for (WorkspaceFileAssociation wfa : ws.getFiles()){
//            if (wfa.getRole() == WorkspaceFileAssociation.ROLE_COMPONENTSLIBRARY){
//                String path = wfa.getPath();
//                while (path.startsWith("/") || path.startsWith("\\")){
//                    path = path.substring(1);
//                }
//                libPath += path + ";";                
//            }
//            if (wfa.getRole() == WorkspaceFileAssociation.ROLE_JAPFILE){
//                japFile = new File(wfa.getFile().getLocation());
//            }
//        }
//        if (japFile == null) {
//            return "#JAMS configuration file\n"
//                    + "#" + sdf.format(new Date()) + "\n"
//                    + "infolog=\n"
//                    + "libs=" + libPath + "\n"
//                    + "guiconfig=0\n"
//                    + "model=\n"
//                    + "charset=\n"
//                    + "username=\n"
//                    + "windowenable=0\n"
//                    + "errordlg=0\n"
//                    + "windowontop=0\n"
//                    + "windowheight=600\n"
//                    + "forcelocale=\n"
//                    + "verbose=1\n"
//                    + "debug=3\n"
//                    + "guiconfigheight=600\n"
//                    + "windowwidth=900\n"
//                    + "guiconfigwidth=600\n"
//                    + "errorlog=\n"
//                    + "helpbaseurl=\n";
//        }else{
//            JAMSProperties properties = new JAMSProperties(null);
//            try {
//                properties.load(japFile.getAbsolutePath());
//            } catch (IOException ex) {
//                Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            properties.setProperty("libs", libPath);
//            properties.setProperty("guiconfig", "0");
//            properties.setProperty("windowenable", "0");
//            properties.setProperty("errordlg", "0");
//            
//            String result = "";
//            for (String key : properties.getKeys()){
//                String value = properties.getProperty(key);
//                result += key + "=" + value + "\n";
//            }
//            return result;
//        }
//    }    
//    private static java.io.File adaptJAPFile(java.io.File targetDirectory, Workspace ws) throws IOException{      
//        java.io.File japFile = new java.io.File(targetDirectory + "/default.jap");
//        
//        try (BufferedWriter bos = new BufferedWriter(new FileWriter(japFile.getAbsoluteFile()))) {
//            bos.write(getJAPContent(ws));
//        }
//        
//        return japFile;
//    }
                            
    public static StreamingOutput streamFile(java.io.File f) throws IOException {                        
        return new StreamFile(f, false);
    }
    
    public static StreamingOutput streamWorkspace(java.io.File target, Workspace ws) throws IOException {         
        final java.io.File zipFile = zipWorkspace(target, ws);
                
        return new StreamFile(zipFile, true);
    }
    
    private static class StreamFile implements StreamingOutput {

        java.io.File f = null;
        boolean deleteAfterClose = false;
        public StreamFile(java.io.File f, boolean deleteAfterClose) {
            super();
            this.f = f;
            this.deleteAfterClose = deleteAfterClose;
        }

        @Override
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
            
    static public void deleteWorkspace(User user, Workspace ws){
        if (user == null || ws == null)
            return;
        
        java.io.File target = new java.io.File(ApplicationConfig.SERVER_EXEC_DIRECTORY + "/" + user.getLogin() + "/" + ws.getId());
        //never ever climb up
        if (user.getLogin().contains("..")){
            return;
        }
        if (!target.exists())
            return;
        FileTools.deleteRecursive(target);        
    }            
}
