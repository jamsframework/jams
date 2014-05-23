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

import jams.server.entities.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author christian
 */
public class FileController extends Controller {

    public FileController(HTTPClient client, String serverUrl) {
        super(client, serverUrl);
    }

    private String getHashCode(java.io.File f) {
        try {
            return org.apache.commons.codec.digest.DigestUtils.md5Hex(new FileInputStream(f));
        } catch (IOException fnfe) {
            log(Level.SEVERE, fnfe.toString(), fnfe);
            return null;
        }
    }

    public jams.server.entities.File uploadFile(File f) {
        return (jams.server.entities.File) client.httpFileUpload(serverURL + "/file/upload", f, jams.server.entities.File.class);
    }

    public Map<File, jams.server.entities.File> uploadFile(File files[])  {
        Map<File, jams.server.entities.File> mapping = exists(files);
                
        for (File f : files) {
            if (mapping.get(f)==null)
                mapping.put(f, uploadFile(f));
        }
        return mapping;
    }

    public File get(int id) {
        return (File) client.httpGet(serverURL + "/file/" + id, File.class);
    }

    public boolean exists(java.io.File f) {
        return !exists(new java.io.File[]{f}).isEmpty();
    }

    public Map<File, jams.server.entities.File> exists(java.io.File f[]) {
        Map<File, jams.server.entities.File> fileMapping = new HashMap<File, jams.server.entities.File>();
        Files files = new Files();
        for (File f1 : f) {
            files.add(new jams.server.entities.File(0, getHashCode(f1)));
            fileMapping.put(f1,null);
        }
        Files result = (Files) client.httpPost(serverURL + "/file/exists", "POST", files, Files.class);
                
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
