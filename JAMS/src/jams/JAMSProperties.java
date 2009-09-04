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
package jams;

import java.io.*;
import java.util.*;

/**
 *
 * @author S. Kralisch
 */
public class JAMSProperties extends Observable {

    /**
     * Identifier for model value
     */
    public static final String MODEL_IDENTIFIER = "model";
    /**
     * Identifier for libraries value
     */
    public static final String LIBS_IDENTIFIER = "libs";
    /**
     * Identifier for server value
     */
    public static final String SERVER_IDENTIFIER = "server";
    /**
     * Identifier for server account value
     */
    public static final String SERVER_ACCOUNT_IDENTIFIER = "serveraccount";
    /**
     * Identifier for server password value
     */
    public static final String SERVER_PASSWORD_IDENTIFIER = "serverpassword";
    /**
     * Identifier for excludes value
     */
    public static final String SERVER_EXCLUDES_IDENTIFIER = "excludes";
    /**
     * Identifier for workspace value
     */
    public static final String WORKSPACE_IDENTIFIER = "workspace";

    private Properties properties = new Properties();
    private String defaultFilename = "";
    private HashMap<String, JAMSProperty> propertyMap = new HashMap<String, JAMSProperty>();

    /**
     * Creates a new JAMSProperties object
     * @param properties A java.util.Properties object containing the properties
     */
    public JAMSProperties(Properties properties) {
        if (properties != null )
            this.properties = properties;
    }

    /**
     * Loads properties from a file
     * @param fileName The name of the file to read properties from
     * @throws java.io.IOException
     */
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

    /**
     * Saves properties to a file
     * @param fileName The name of the file to save properties to
     * @throws java.io.IOException
     */
    public void save(String fileName) throws IOException {
        try {
            properties.store(new FileOutputStream(fileName), JAMS.resources.getString("JAMS_configuration_file"));
            defaultFilename = fileName;
        } catch (Exception ex) {
            JAMS.handle(ex);
        }
    }

    /**
     * Gets a property value
     * @param key The identifier for the property
     * @return The property value
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Gets a property value or default value if property does not exist
     * @param key The identifier for the property
     * @param defaultValue The default value
     * @return The property value
     */
    public String getProperty(String key, String defaultValue) {
        if (getProperty(key) != null) {
            return getProperty(key);
        } else {
            return defaultValue;
        }
    }

    /**
     * Sets a property value
     * @param key The identifier for the property
     * @param value The value of the property
     */
    public void setProperty(String key, String value) {

        JAMSProperty property = propertyMap.get(key);
        if (property == null) {
            property = new JAMSProperty(key);
            propertyMap.put(key, property);
        }

        if ((properties.getProperty(key) == null) || (!properties.getProperty(key).equals(value))) {
            //something has changed
            properties.setProperty(key, value);
            property.setChanged();
            property.notifyObservers();
        }

    }

    /**
     * Adds an observer for some property
     * @param key The identifier for the property
     * @param obs The java.util.Observer object
     */
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

    /**
     * Creates a new JAMSProperties object
     * @return The JAMSProperties object
     */
    public static JAMSProperties createJAMSProperties() {
        Properties p = new Properties();
        p.setProperty("model", "");
        p.setProperty("libs", "lib");
        p.setProperty("debug", "1");
        p.setProperty("verbose", "0");
        p.setProperty("infolog", "");
        p.setProperty("errorlog", "");
        p.setProperty("errordlg", "1");
        p.setProperty("windowenable", "1");
        p.setProperty("windowwidth", "900");
        p.setProperty("windowheight", "600");
        p.setProperty("guiconfig", "1");
        p.setProperty("guiconfigwidth", "600");
        p.setProperty("guiconfigheight", "600");
        p.setProperty("splashtimeout", "1000");

        JAMSProperties jp = new JAMSProperties(p);
        jp.setDefaultFilename(System.getProperty("user.dir") + File.separator + JAMS.DEFAULT_PARAMETER_FILENAME);

        return jp;
    }

    /**
     *
     * @return The default file name for storing JAMS properties
     */
    public String getDefaultFilename() {
        return defaultFilename;
    }

    /**
     * Set the default file name for storing JAMS properties
     * @param defaultFilename The default file name
     */
    public void setDefaultFilename(String defaultFilename) {
        this.defaultFilename = defaultFilename;
    }
}
