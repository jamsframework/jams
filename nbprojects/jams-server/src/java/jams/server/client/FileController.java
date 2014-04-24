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

/**
 *
 * @author christian
 */
public class FileController extends Controller{    
    public FileController(HTTPClient client, String serverUrl){
        super(client, serverUrl);
    }
    
    private String getHashCode(java.io.File f) throws JAMSClientException{
        try {
            return org.apache.commons.codec.digest.DigestUtils.md5Hex(new FileInputStream(f));
        } catch (IOException fnfe) {
            fnfe.printStackTrace();
            throw new JAMSClientException(fnfe.toString(), JAMSClientException.ExceptionType.UNKNOWN, fnfe);
        }
    }

    public jams.server.entities.File uploadFile(File f) throws JAMSClientException{
        jams.server.entities.File result = this.findServerFile(f);

        if (result == null) {
            return (jams.server.entities.File) client.httpFileUpload(serverURL + "/file/upload", f, jams.server.entities.File.class);
        } else {
            log("File was already uploaded!");
        }

        return result;
    }

    public jams.server.entities.File[] uploadFile(File f[]) throws JAMSClientException{
        Files filesExisting = this.findServerFile(f);
        jams.server.entities.File files[] = filesExisting.getFiles().toArray(new jams.server.entities.File[0]);
        for (int i = 0; i < files.length; i++) {
            if (files[i] == null) {
                files[i] = uploadFile(f[i]);
            }
        }
        return files;
    }

    public jams.server.entities.File findServerFile(File f) throws JAMSClientException{
        String hashCode = getHashCode(f);

        Files filesIn = new Files(new jams.server.entities.File(0, hashCode));
        Files filesOut = (Files) client.httpPost(serverURL + "/file/exists", "POST", filesIn, Files.class);
        if (filesOut != null && !filesOut.getFiles().isEmpty()) {
            jams.server.entities.File result = filesOut.getFiles().get(0);
            if (!result.equals(jams.server.entities.File.NON_FILE)) {
                return result;
            }
        }
        return null;
    }

    public Files findServerFile(File f[]) throws JAMSClientException{
        jams.server.entities.Files files = new jams.server.entities.Files();
        for (File f1 : f) {
            files.add(new jams.server.entities.File(0, getHashCode(f1)));
        }
        Files filesOut = (Files) client.httpPost(serverURL + "/file/exists", "POST", files, Files.class);
        for (int i = 0; i < f.length; i++) {
            if (filesOut.getFiles().get(i).equals(jams.server.entities.File.NON_FILE)) {
                filesOut.getFiles().set(i, null);
            }
        }
        return filesOut;
    }
}
