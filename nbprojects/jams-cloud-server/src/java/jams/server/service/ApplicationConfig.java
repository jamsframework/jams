/*
 * ApplicationConfig.java
 * Created on 01.03.2014, 21:37:11
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

import java.io.File;
import java.io.FileReader;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application {

    public static String SERVER_UPLOAD_DIRECTORY = null;//"e:/test_server/uploaded/";
    public static String SERVER_TMP_DIRECTORY=  null;//"E:/test_server/tmp/";
    public static String SERVER_EXEC_DIRECTORY = null;//"E:/test_server/exec/";
    public static String SERVER_MAX_MEM = null;
    
    static Properties p = new Properties() {
        {
            Logger log = Logger.getLogger(ApplicationConfig.class.getName());                         
            log.info("Try to set paths..");
            try {
                load(new FileReader("settings.properties"));
                SERVER_UPLOAD_DIRECTORY = getProperty("upload-directory");
                SERVER_TMP_DIRECTORY = getProperty("tmp-directory");
                SERVER_EXEC_DIRECTORY = getProperty("exec-directory");
                SERVER_MAX_MEM = getProperty("server-max-mem");
                if (SERVER_MAX_MEM == null) {
                    SERVER_MAX_MEM = "8g";
                }

                log.info("Setting of paths was successful [" +
                        SERVER_UPLOAD_DIRECTORY + ", " + 
                        SERVER_TMP_DIRECTORY + ", " + 
                        SERVER_EXEC_DIRECTORY + "]");
            } catch (Throwable ioe) {
                log.log(Level.SEVERE, ioe.getMessage(), ioe);
                ioe.printStackTrace();
            }
        }
    };       
    @Override
    public Set<Class<?>> getClasses() {                                        
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        if (!resources.contains(MultiPartFeature.class))
            resources.add(MultiPartFeature.class);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(jams.server.service.FileFacadeREST.class);
        resources.add(jams.server.service.JobFacadeREST.class);
        resources.add(jams.server.service.ServerInformationREST.class);
        resources.add(jams.server.service.UserFacadeREST.class);
        resources.add(jams.server.service.WorkspaceFacadeREST.class);
    }
    
}
