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
import jams.data.SingleDataSupplier;
import jams.server.client.error.ErrorHandler;
import jams.server.entities.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.codec.digest.DigestUtils;
import static jams.tools.LogTools.log;
import java.util.HashMap;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class FileController {

    HTTPClient client;
    String urlStr;

    /**
     * ensures the construction of a working FileController
     *
     * @param ctrl the parent controller
     */
    public FileController(Controller ctrl) {
        this.client = ctrl.getClient();
        this.urlStr = ctrl.getServerURL();
    }

    /**
     * calculates the hashcode of a local file
     *
     * @param f the file of which the hashcode should be calculated
     * @return hashcode of the local file as hex-string
     * @throws java.io.IOException
     */
    public String getHashCode(java.io.File f) throws IOException {
        return DigestUtils.md5Hex(new FileInputStream(f));
    }

    /**
     * updates the hashcode of files located on the server
     *
     * @param files of which the hashcodes should be updated
     * @return files object with the desired information
     */
    public Files getHashCode(Files files) {
        log(this.getClass(), Level.FINE, JAMS.i18n("retrieving_hash_codes_of_server_files"));
        return client.httpPost(urlStr + "/file/hash", "POST", files, Files.class);
    }

    /**
     * uploades a file to the server
     *
     * @param f the file to be uploaded
     * @return the new file object from the server
     */
    public jams.server.entities.File uploadFile(File f) {
        log(this.getClass(), Level.FINE, JAMS.i18n("uploading_{0}"), f);
        return client.httpFileUpload(
                urlStr + "/file/upload", f, jams.server.entities.File.class);
    }

    /**
     * uploades the content of an inputstream to the server
     *
     * @param f the InputStream to be uploaded
     * @return the new file object from the server
     * @throws java.io.IOException
     */
    public jams.server.entities.File uploadFile(InputStream f) throws IOException {
        log(this.getClass(), Level.FINE, JAMS.i18n("uploading_stream"));
        return client.httpFileUpload(
                urlStr + "/file/upload", f, jams.server.entities.File.class);
    }

    /**
     * uploades the content of an inputstream to the server
     *
     * @param files a list of files to be uploaded
     * @param handler : error handling when a file cannot be uploaded for some reason
     * @return a mapping between local files and corresponding files on the
     * server
     * @throws java.io.IOException
     */
    public Map<File, jams.server.entities.File> uploadFile(Iterable<File> files, ErrorHandler<File> handler) throws IOException {
        //check which files are already existing on the server
        Map<File, jams.server.entities.File> mapping = find(files);

        for (File f : files) {            
            //upload those files which are not on the server yet
            if (!mapping.containsKey(f)) {
                log(this.getClass(), Level.FINE, JAMS.i18n("uploading_file_{0}"), f.getName());
                //catch all exception, because we don't want to stop uploading process if just
                //one file can't be uploaded TODO introduce interface for error handling
                try {
                    mapping.put(f, uploadFile(f));
                } catch (Throwable t) {
                    if (!handler.handleError(f, t)){
                        throw t;
                    }
                }
            }
        }
        return mapping;
    }

    /**
     * uploades the content of an inputstream to the server
     *
     * @param file
     * @return the content if the file as stream
     * @throws java.io.IOException
     */
    public InputStream getFileAsStream(jams.server.entities.File file) throws IOException {
        log(this.getClass(), Level.FINE, "{0}{1}",
                JAMS.i18n("getting_file_stream_with_id"), file.getId());
        return client.getStream(urlStr + "/file/" + file.getId() + "/getStream");
    }

    /**
     * finds the file for a specific id
     *
     * @param id of the desired file
     * @return the servers file object
     */
    public jams.server.entities.File find(int id) {
        log(this.getClass(), Level.FINE, "{0} {1}", JAMS.i18n("retrieving_file_with_id"), id);
        return client.httpGet(urlStr + "/file/" + id, jams.server.entities.File.class);
    }

    /**
     * finds the server file for a specific local file based on its hashcode
     *
     * @param f the desired file
     * @return the servers file object
     * @throws java.io.IOException
     */
    public Map<File, jams.server.entities.File> find(java.io.File f) throws IOException {
        return find(new SingleDataSupplier<>(f));
    }

    /**
     * finds the server file for a list of local files based on their hashcode
     *
     * @param files the desired files
     * @return a mapping of local files and the corresponding server files
     * @throws java.io.IOException
     */
    public Map<File, jams.server.entities.File> find(Iterable<java.io.File> files) throws IOException {
        Map<File, jams.server.entities.File> fileMapping = new HashMap<>();
        Map<String, java.io.File> hashMapping = new HashMap<>();
        Files request = new Files();

        for (java.io.File f : files) {
            log(this.getClass(), Level.FINE, JAMS.i18n("calculating_hash_key_of_{0}"), f.getName());
            try {
                String hash = getHashCode(f);
                request.add(new jams.server.entities.File(0, hash));
                hashMapping.put(hash, f);
            } catch (IOException ioe) {
                log(this.getClass(), Level.WARNING, ioe,
                        JAMS.i18n("unable_to_calculate_hashkey_of_{0}"), f);
            }
        }
        log(this.getClass(), Level.FINE, JAMS.i18n("searching_for_existing_files"));
        Files response = client.httpPost(urlStr + "/file/exists", "POST", request, Files.class);

        for (jams.server.entities.File rf : response.getFiles()) {
            File f = hashMapping.get(rf.getHash());
            if (f != null) {
                fileMapping.put(f, rf);
            }
        }
        return fileMapping;
    }
}
