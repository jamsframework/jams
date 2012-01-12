/*
 * JAMSFileFilter.java
 * Created on 29. August 2006, 09:28
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

    public static final String PROPERTY_EXTENSION = ".jap";

    private static FileFilter propertyFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(PROPERTY_EXTENSION);
        }
        @Override
        public String getDescription() {
            return JAMS.i18n("JAMS_Preferences_(*.jap)");
        }
    };
    private static FileFilter modelFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".jam") || f.getName().toLowerCase().endsWith(".xml");
        }
        @Override
        public String getDescription() {
            return JAMS.i18n("JAMS_Model_(*.jam;_*.xml)");
        }
    };
    private static FileFilter xmlFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml");
        }
        @Override
        public String getDescription() {
            return JAMS.i18n("XML_FILE_(*.xml)");
        }
    };
    private static FileFilter serializationFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".ser");
        }
        @Override
        public String getDescription() {
            return JAMS.i18n("Serialization_(ser)");
        }
    };
    private static FileFilter jarFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".jar");
        }
        @Override
        public String getDescription() {
            return JAMS.i18n("Java_Archive_(*.jar)");
        }
    };
    private static FileFilter parameterFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".jmp");
        }
        @Override
        public String getDescription() {
            return JAMS.i18n("JAMS_Model_Parameter_(*.jmp)");
        }
    };
    
    private static FileFilter epsFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".eps");
        }
        @Override
        public String getDescription() {
            return JAMS.i18n("Encapsulated_Postscript_(*.eps)");
        }
    };

    private static FileFilter ttpFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".ttp") || f.getName().toLowerCase().endsWith(".dtp");
        }
        @Override
        public String getDescription() {
            return JAMS.i18n("Plot_template_(*.ttp)");
        }
    };

    private static FileFilter datFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".dat") || f.getName().toLowerCase().endsWith(".dtp");
        }
        @Override
        public String getDescription() {
            return JAMS.i18n("Spreadsheet_data_(*.dat)");
        }
    };

    private static FileFilter oddFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".odd") || f.getName().toLowerCase().endsWith(".xml");
        }
        @Override
        public String getDescription() {
            return JAMS.i18n("Optimization_Description_Document_(*.odd)");
        }
    };


    private static FileFilter shapeFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".shp");
        }
        @Override
        public String getDescription() {
            return JAMS.i18n("Shapefiles_(*.shp)");
        }
    };

    /**
     *
     * @return The filter ser files
     */
    public static FileFilter getSerFilter() {
        return serializationFilter;
    }
    /**
     *
     * @return The filter for EPS files
     */
    public static FileFilter getEpsFilter() {
        return epsFilter;
    }

    /**
     *
     * @return The filter for EPS files
     */
    public static FileFilter getDatFilter() {
        return datFilter;
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
     * @return The filter for xml files
     */
    public static FileFilter getXMLFilter() {
        return xmlFilter;
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

    /**
     *
     * @return The filter for TTP files
     */
    public static FileFilter getTtpFilter() {
        return ttpFilter;
    }

    /**
     * 
     * @return the shape file filter
     */
    public static FileFilter getShapeFilter() {
        return shapeFilter;
    }

    /**
     * @return the oddFilter
     */
    public static FileFilter getOddFilter() {
        return oddFilter;
    }


}
