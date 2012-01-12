/*
 * JAMS.java
 * Created on 2. Oktober 2005, 16:05
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
 * GNU General Publiccc License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams;

import java.awt.Font;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sven Kralisch
 */
public class JAMS {

    /**
     * Verbosity level 0 of 3
     */
    public static final int SILENT = 0;
    /**
     * Verbosity level 1 of 3
     */
    public static final int STANDARD = 1;
    /**
     * Verbosity level 2 of 3
     */
    public static final int VERBOSE = 2;
    /**
     * Verbosity level 3 of 3
     */
    public static final int VVERBOSE = 3;
    /**
     * Resource bundle containing all string literals for some localization
     */
    private static ResourceBundle resources = java.util.ResourceBundle.getBundle("resources/i18n/JAMSBundle");
    /**
     * Default character encoding
     */
    private static String charset = "ISO-8859-1";
    /**
     * The standard font
     */
    public static final Font STANDARD_FONT = new java.awt.Font(Font.MONOSPACED, Font.PLAIN, 11);
    /**
     * Default name of model output file
     */
    public static final String DEFAULT_MODEL_PARAMETER_FILENAME = "model.jmp";
    /**
     * Default name of parameter output file
     */
    public static final String DEFAULT_PARAMETER_FILENAME = "default.jap";

    /**
     * Return a localized string 
     * @param key A resource key
     * @return A localized string belonging to the resource key
     */
    public static String i18n(String key) {
        if (getResources().containsKey(key)) {
            return getResources().getString(key);
        } else {
            Logger.getLogger(JAMS.class.getName()).log(Level.INFO, "Could not find i18n key \"" + key + "\", using the key as result!", new JAMSException("Invalid resource key"));
            return key;
        }
    }

    /**
     * @return the resources
     */
    public static ResourceBundle getResources() {
        return resources;
    }

    /**
     * @param aResources the resources to set
     */
    public static void setResources(ResourceBundle aResources) {
        resources = aResources;
    }

    /**
     * @return the charset
     */
    public static String getCharset() {
        return charset;
    }

    /**
     * @param aCharset the charset to set
     */
    public static void setCharset(String aCharset) {
        charset = aCharset;
    }
}
