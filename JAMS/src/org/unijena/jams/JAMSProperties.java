/*
 * JAMSProperties.java
 * Created on 18. April 2006, 23:11
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package org.unijena.jams;

import java.io.*;
import java.util.*;

/**
 *
 * @author S. Kralisch
 */
public class JAMSProperties extends Observable {
    
    public static final String DEFAULT_FILENAME = "default.jap";
    public static final String MODEL_IDENTIFIER = "model";
    public static final String LIBS_IDENTIFIER = "libs";
    public static final String SERVER_IDENTIFIER = "server";
    public static final String SERVER_ACCOUNT_IDENTIFIER = "serveraccount";
    public static final String SERVER_PASSWORD_IDENTIFIER = "serverpassword";
    public static final String SERVER_EXCLUDES_IDENTIFIER = "excludes";
    public static final String WORKSPACE_IDENTIFIER = "workspace";
    
    private Properties properties = new Properties();
    private String defaultFilename = "";
    private HashMap<String, JAMSProperty> propertyMap = new HashMap<String, JAMSProperty>();
    
    
    public JAMSProperties() {
    }
    
    public JAMSProperties(Properties properties) {
        this.properties = properties;
    }
    
    public void load(String fileName) throws IOException {
        try {
            properties.load(new FileInputStream(fileName));
            defaultFilename = fileName;
            
            for (Object key : properties.keySet()) {
                JAMSProperty property = propertyMap.get(key);
                if (property == null) {
                    property = new JAMSProperty((String) key);
                    propertyMap.put((String) key, property);
                }
                property.setChanged();
                property.notifyObservers();
            }
            
        } catch (Exception ex) {
            JAMS.handle(ex);
        }
    }
    
    public void save(String fileName) throws IOException {
        try {
            properties.store(new FileOutputStream(fileName), "JAMS configuration file");
            defaultFilename = fileName;
        } catch (Exception ex) {
            JAMS.handle(ex);
        }
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        if (getProperty(key) != null) {
            return getProperty(key);
        } else {
            return defaultValue;
        }
    }
    
    public void setProperty(String key, String value) {
        
        JAMSProperty property = propertyMap.get(key);
        if (property == null) {
            property = new JAMSProperty(key);
            propertyMap.put(key, property);
        }
        
        properties.setProperty(key, value);
        property.setChanged();
        property.notifyObservers();
        
    }
    
    public void addObserver(String key, Observer obs) {
        JAMSProperty property = propertyMap.get(key);
        if (property == null) {
            property = new JAMSProperty(key);
            propertyMap.put(key, property);
        }
        property.addObserver(obs);
    }
    
    public String toString() {
        return properties.toString();
    }
    
    public static JAMSProperties createJAMSProperties() {
        Properties p = new Properties();
        p.setProperty("model", "");
        p.setProperty("libs", "lib");
        p.setProperty("debug", "1");
        p.setProperty("verbose", "0");
        p.setProperty("infolog", "");
        p.setProperty("errorlog", "");
        p.setProperty("windowenable", "1");
        p.setProperty("windowwidth", "800");
        p.setProperty("windowheight", "600");
        p.setProperty("guiconfig", "1");
        p.setProperty("guiconfigwidth", "600");
        p.setProperty("guiconfigheight", "600");
        
        JAMSProperties jp = new JAMSProperties(p);
        jp.setDefaultFilename(System.getProperty("user.dir") + System.getProperty("file.separator") + JAMSProperties.DEFAULT_FILENAME);
        
        return jp;
    }
    
    public String getDefaultFilename() {
        return defaultFilename;
    }
    
    public void setDefaultFilename(String defaultFilename) {
        this.defaultFilename = defaultFilename;
    }
    
}
