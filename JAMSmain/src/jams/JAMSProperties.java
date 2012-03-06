/*
 * JAMSProperties.java
 * Created on 18. April 2006, 23:11
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams;

import jams.tools.JAMSTools;
import java.io.*;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

/**
 *
 * @author S. Kralisch
 */
public class JAMSProperties extends Observable implements SystemProperties, Serializable {

    private Properties properties = new Properties();
    private String defaultFilename = "";
    private HashMap<String, JAMSProperty> propertyMap = new HashMap<String, JAMSProperty>();
    private static JAMSProperties theProperties;

    /**
     * Creates a new JAMSProperties object
     *
     * @param properties A java.util.Properties object containing the properties
     */
    public JAMSProperties(Properties properties) {
        if (properties != null) {
            this.properties = properties;
        }
    }

    /**
     * Loads properties from a file
     *
     * @param fileName The name of the file to read properties from
     * @throws java.io.IOException
     */
    public void load(String fileName) throws IOException {
        try {
            properties.load(new FileInputStream(fileName));
            defaultFilename = fileName;
            convertToBoolean();

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
            JAMSTools.handle(ex);
        }
    }

    /*
     * Maintain compatibility to older property files
     */
    private void convertToBoolean() {
        if ("1".equals(properties.getProperty(ERRORDLG_IDENTIFIER))) {
            properties.setProperty(ERRORDLG_IDENTIFIER, "true");
        }
        if ("1".equals(properties.getProperty(WINDOWENABLE_IDENTIFIER))) {
            properties.setProperty(WINDOWENABLE_IDENTIFIER, "true");
        }
        if ("1".equals(properties.getProperty(GUICONFIG_IDENTIFIER))) {
            properties.setProperty(GUICONFIG_IDENTIFIER, "true");
        }
        if ("1".equals(properties.getProperty(USE_DEFAULT_WS_PATH))) {
            properties.setProperty(USE_DEFAULT_WS_PATH, "true");
        }
    }

    /**
     * Saves properties to a file
     *
     * @param fileName The name of the file to save properties to
     * @throws java.io.IOException
     */
    public void save(String fileName) throws IOException {
        try {
            properties.store(new FileOutputStream(fileName), JAMS.i18n("JAMS_configuration_file"));
            defaultFilename = fileName;
        } catch (Exception ex) {
            JAMSTools.handle(ex);
        }
    }

    /**
     * Gets a property value
     *
     * @param key The identifier for the property
     * @return The property value
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Gets a property value or default value if property does not exist
     *
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
     *
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
     *
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

    /**
     * Creates a string representation of this object
     *
     * @return A string representation of this object
     */
    @Override
    public String toString() {
        return properties.toString();
    }

    /**
     * Creates a new JAMSProperties object
     *
     * @return The JAMSProperties object
     */
    public static JAMSProperties createProperties() {

        if (theProperties == null) {
            Properties p = new Properties();
            p.setProperty(MODEL_IDENTIFIER, "");
            p.setProperty(LIBS_IDENTIFIER, ".");
            p.setProperty(DEBUG_IDENTIFIER, "1");
            p.setProperty(VERBOSITY_IDENTIFIER, "false");
            p.setProperty(INFOLOG_IDENTIFIER, "");
            p.setProperty(ERRORLOG_IDENTIFIER, "");
            p.setProperty(ERRORDLG_IDENTIFIER, "true");
            p.setProperty(WINDOWENABLE_IDENTIFIER, "true");
            p.setProperty(WINDOWWIDTH_IDENTIFIER, "900");
            p.setProperty(WINDOWHEIGHT_IDENTIFIER, "600");
            p.setProperty(GUICONFIG_IDENTIFIER, "true");
            p.setProperty(GUICONFIGWIDTH_IDENTIFIER, "600");
            p.setProperty(GUICONFIGHEIGHT_IDENTIFIER, "600");
            p.setProperty(CHARSET_IDENTIFIER, "");
            p.setProperty(PROFILING_IDENTIFIER, "false");
            p.setProperty(USE_DEFAULT_WS_PATH, "true");

            theProperties = new JAMSProperties(p);
            theProperties.setDefaultFilename(System.getProperty("user.dir") + File.separator + JAMS.DEFAULT_PARAMETER_FILENAME);
        }

        return theProperties;
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
     *
     * @param defaultFilename The default file name
     */
    public void setDefaultFilename(String defaultFilename) {
        this.defaultFilename = defaultFilename;
    }
}
