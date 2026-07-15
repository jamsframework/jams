/*
 * SystemProperties.java
 * Created on 5. November 2009, 16:25
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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

import java.io.IOException;
import java.util.Observer;
import java.util.Properties;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public interface SystemProperties {

    String LIBS_IDENTIFIER = "libs";
    String DEBUG_IDENTIFIER = "debug";
    String VERBOSITY_IDENTIFIER = "verbose";
    String DEBUG_MODE = "debugmode";
    String INFOLOG_IDENTIFIER = "infolog";
    String ERRORLOG_IDENTIFIER = "errorlog";
    String ERRORDLG_IDENTIFIER = "errordlg";
    String WINDOWENABLE_IDENTIFIER = "windowenable";
    String WINDOWWIDTH_IDENTIFIER = "windowwidth";
    String WINDOWHEIGHT_IDENTIFIER = "windowheight";
    String GUICONFIG_IDENTIFIER = "guiconfig";
    String GUICONFIGWIDTH_IDENTIFIER = "guiconfigwidth";
    String GUICONFIGHEIGHT_IDENTIFIER = "guiconfigheight";
    String CHARSET_IDENTIFIER = "charset";
    String LOCALE_IDENTIFIER = "forcelocale";
    String WINDOWONTOP_IDENTIFIER = "windowontop";
    String USERNAME_IDENTIFIER = "username";
    String HELPBASEURL_IDENTIFIER = "helpbaseurl";
    String MODEL_IDENTIFIER = "model";
    String SERVER_ACCOUNT_IDENTIFIER = "serveraccount";
    String SERVER_EXCLUDES_IDENTIFIER = "excludes";
    String SERVER_IDENTIFIER = "server";
    String SERVER_PASSWORD_IDENTIFIER = "serverpassword";
    String WORKSPACE_IDENTIFIER = "workspace";
    String PROFILING_IDENTIFIER = "profiling";
    String USE_DEFAULT_WS_PATH = "defaultworkspace";
    String DOCBOOK_HOME_PATH = "docbook-home";
    String AUTO_PREPROCESSING = "preprocessing";
    String FLOAT_FORMAT = "floatformat";
    String RECENT_FILES = "recentfiles";
    String RECENT_TIMESERIES_OF_OBJECTIVE_CONFIGURATION = "objectiveconfiguration_recent_timeseries";
    String MAX_RECENT_FILES = "maxrecentfiles";
    String MAX_LIB_CLASSES = "maxlibclasses";
    String DOCBOOK_HOME = "docbook-home";
    String EXPLORER_DECIMAL_DIGITS = "explorerdigits";
    String AUTO_SAVE_LOGS = "autosavelogs";
    String AUTO_SAVE_PARAMS = "autosaveparams";
    
    /**
     * Adds an observer for some property
     * @param key The identifier for the property
     * @param obs The java.util.Observer object
     */
    void addObserver(String key, Observer obs);

    /**
     * Gets a property value
     * @param key The identifier for the property
     * @return The property value
     */
    String getProperty(String key);

    /**
     * Gets a property value or default value if property does not exist
     * @param key The identifier for the property
     * @param defaultValue The default value
     * @return The property value
     */
    String getProperty(String key, String defaultValue);

    /**
     * Loads properties from a file
     * @param fileName The name of the file to read properties from
     * @throws java.io.IOException
     */
    void load(String fileName) throws IOException;

    /**
     * Saves properties to a file
     * @param fileName The name of the file to save properties to
     * @throws java.io.IOException
     */
    void save(String fileName) throws IOException;

    /**
     * Saves properties to a file
     * @throws java.io.IOException
     */
    void save() throws IOException;
    
    /**
     * Sets a property value
     * @param key The identifier for the property
     * @param value The value of the property
     */
    void setProperty(String key, String value);

    Properties getProperties();
    
    @Override
    String toString();

}
