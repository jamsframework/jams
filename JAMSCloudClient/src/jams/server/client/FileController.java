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
import jams.server.entities.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author christian
 */
public class FileController {

    private static final Logger log = Logger.getLogger( Controller.class.getName() );
    
    Controller ctrl;
    
    public FileController(Controller ctrl) {
        this.ctrl = ctrl;        
    }

    public String getHashCode(java.io.File f) {
        try {
            return org.apache.commons.codec.digest.DigestUtils.md5Hex(new FileInputStream(f));
        } catch (IOException fnfe) {
            log.log(Level.SEVERE, fnfe.toString(), fnfe);
            return null;
        }
    }
    
    public Files getHashCode(Files files) {
        log.fine(JAMS.i18n("retrieving_hash_codes_of_server_files"));
        
        return (Files) ctrl.client.httpPost(ctrl.getServerURL() + "/file/hash", "POST", files, Files.class);
    }

    public jams.server.entities.File uploadFile(File f) {
        return (jams.server.entities.File) ctrl.getClient().httpFileUpload(ctrl.getServerURL() + "/file/upload", f, jams.server.entities.File.class);
    }
    
    public jams.server.entities.File uploadFile(InputStream f) {
        return (jams.server.entities.File) ctrl.getClient().httpFileUpload(ctrl.getServerURL() + "/file/upload", f, jams.server.entities.File.class);
    }

    public Map<File, jams.server.entities.File> uploadFile(File files[])  {
        Map<File, jams.server.entities.File> mapping = exists(files);
                
        int counter = 0;
        for (File f : files) {
            log.fine(JAMS.i18n("uploading_file_%1_(_%2_of_%3_)")
                    .replace("%1", f.getName())
                    .replace("%2", Integer.toString(counter++))
                    .replace("%3", Integer.toString(files.length))
            );
                    
            if (mapping.get(f)==null)
                mapping.put(f, uploadFile(f));
        }
        return mapping;
    }

    public File get(int id) {
        log.fine(JAMS.i18n("retrieving_file_with_id") + " " + id);
        return (File) ctrl.client.httpGet(ctrl.getServerURL() + "/file/" + id, File.class);
    }
    
    public InputStream getFileAsStream(jams.server.entities.File file){
        log.fine(JAMS.i18n("getting_file_stream_with_id") + file.getId());
        return ctrl.client.getStream(ctrl.getServerURL() + "/file/" + file.getId() + "/getStream");
    }
    

    public boolean exists(java.io.File f) {
        return !exists(new java.io.File[]{f}).isEmpty();
    }

    public Map<File, jams.server.entities.File> exists(java.io.File f[]) {        
        Map<File, jams.server.entities.File> fileMapping = new HashMap<File, jams.server.entities.File>();
        Files files = new Files();        
        int k = 0;
        for (File f1 : f) {
            log.fine(JAMS.i18n("calculating_hash_keys")  + " (" + (++k) + " of " + f.length + ")");
            files.add(new jams.server.entities.File(0, getHashCode(f1)));
            fileMapping.put(f1,null);
        }
        Files result = (Files) ctrl.getClient().httpPost(ctrl.getServerURL() + "/file/exists", "POST", files, Files.class);
                
        for (int i = 0; i < files.getFiles().size(); i++) {
            for (jams.server.entities.File x : result.getFiles()) {
                if (x.getHash().equals(files.getFiles().get(i).getHash())) {
                    fileMapping.put(f[i],x);
                    break;
                }
            }
        }
        return fileMapping;
    }
}
