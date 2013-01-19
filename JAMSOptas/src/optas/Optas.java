/*
 * optas.java
 * Created on 9. Oktober 2012, 16:05
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
package optas;

import jams.JAMS;
import jams.JAMSException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Optas{
    private static ResourceBundle resources = java.util.ResourceBundle.getBundle("optas/resources/OPTASBundle");
    
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
}