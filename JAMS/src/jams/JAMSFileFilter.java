/*
 * JAMSFileFilter.java
 * Created on 29. August 2006, 09:28
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

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author S. Kralisch
 */
public class JAMSFileFilter {
    
    private static FileFilter propertyFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".jap");
        }
        @Override
        public String getDescription() {
            return JAMS.resources.getString("JAMS_Preferences_(*.jap)");
        }
    };
    private static FileFilter modelFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".jam") || f.getName().toLowerCase().endsWith(".xml");
        }
        @Override
        public String getDescription() {
            return JAMS.resources.getString("JAMS_Model_(*.jam;_*.xml)");
        }
    };
    private static FileFilter jarFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".jar");
        }
        @Override
        public String getDescription() {
            return JAMS.resources.getString("Java_Archive_(*.jar)");
        }
    };
    private static FileFilter parameterFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".jmp");
        }
        @Override
        public String getDescription() {
            return JAMS.resources.getString("JAMS_Model_Parameter_(*.jmp)");
        }
    };
    
    private static FileFilter epsFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".eps");
        }
        @Override
        public String getDescription() {
            return JAMS.resources.getString("Encapsulated_Postscript_(*.eps)");
        }
    };
    
    /**
     *
     * @return The filter for EPS files
     */
    public static FileFilter getEpsFilter() {
        return epsFilter;
    }

    /**
     *
     * @return The filter for property files
     */
    public static FileFilter getPropertyFilter() {
        return propertyFilter;
    }
    
    /**
     *
     * @return The filter for model files
     */
    public static FileFilter getModelFilter() {
        return modelFilter;
    }

    /**
     *
     * @return The filter for parameter files
     */
    public static FileFilter getParameterFilter() {
        return parameterFilter;
    }
    
/*    public static FileFilter getModelConfigFilter() {
        return modelConfigFilter;
    }
*/
    /**
     *
     * @return The filter for JAR files
     */
    public static FileFilter getJarFilter() {
        return jarFilter;
    }
}
