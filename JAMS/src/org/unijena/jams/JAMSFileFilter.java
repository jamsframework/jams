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

package org.unijena.jams;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author S. Kralisch
 */
public class JAMSFileFilter {
    
    private static FileFilter propertyFilter = new FileFilter() {
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".jap");
        }
        public String getDescription() {
            return "JAMS Preferences (*.jap)";
        }
    };
    private static FileFilter modelFilter = new FileFilter() {
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".jam") || f.getName().toLowerCase().endsWith(".xml");
        }
        public String getDescription() {
            return "JAMS Model (*.jam; *.xml)";
        }
    };
/*    private static FileFilter modelConfigFilter = new FileFilter() {
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".jmc");
        }
        public String getDescription() {
            return "JAMS model configuration(*.jmc)";
        }
    };
 */
    private static FileFilter jarFilter = new FileFilter() {
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".jar");
        }
        public String getDescription() {
            return "Java Archive (*.jar)";
        }
    };
    private static FileFilter parameterFilter = new FileFilter() {
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".jmp");
        }
        public String getDescription() {
            return "JAMS Model Parameter (*.jmp)";
        }
    };
    
    public static FileFilter getPropertyFilter() {
        return propertyFilter;
    }
    
    public static FileFilter getModelFilter() {
        return modelFilter;
    }

    public static FileFilter getParameterFilter() {
        return parameterFilter;
    }
    
/*    public static FileFilter getModelConfigFilter() {
        return modelConfigFilter;
    }
*/
    public static FileFilter getJarFilter() {
        return jarFilter;
    }
}
