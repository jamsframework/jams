/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gw.ui.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author od
 */
public class Files {

      public static class LayerFilter extends FileFilter {

        List<String> fmt = Arrays.asList("shp", "tiff", "tif", "kmz");

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String ext = getFileExt(f);
            if (ext != null && fmt.contains(ext)) {
                if (ext.equalsIgnoreCase("shp")) {
                    return getSibling(f, "dbf").exists() && getSibling(f, "prj").exists();
                }
                return true;
            }
            return false;
        }

        //The description of this filter
        @Override
        public String getDescription() {
            return java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_Layer_Files");
        }
    }

    public static String getFileExt(File f) {
        if (f.getName().lastIndexOf('.') > -1) {
            return f.getName().substring(f.getName().lastIndexOf('.') + 1);
        } else {
            return "";
        }
    }

    public static File getFileBase(File f) {
        if (f.toString().lastIndexOf('.') > -1) {
            return new File(f.toString().substring(0, f.toString().lastIndexOf('.')));
        } else {
            return f;
        }
    }

    /**
     * same base, different extension.
     * @param file
     * @param ext
     * @return
     */
    public static File getSibling(File file, String ext) {
        return new File(getFileBase(file).toString() + '.' + ext);
    }
}
