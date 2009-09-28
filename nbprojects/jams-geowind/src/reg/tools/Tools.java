/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.tools;

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;
import reg.viewer.Constants;

/**
 *
 * @author hbusch
 */
public class Tools {

    /**
     * get all properties starting withthe PropertyGroupName
     * @param thePropertyFile (relative path and name)
     * @param thePropertyGroupName
     * @return Vector of Strings
     */
    public static Vector<String> getPropertyGroup(String thePropertyFile, String thePropertyGroupName) {
        Vector<String> properties = new Vector<String>();
        ResourceBundle resources = java.util.ResourceBundle.getBundle(thePropertyFile);

        Enumeration<String> keys = resources.getKeys();
        while (keys != null && keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.startsWith(thePropertyGroupName)) {
                properties.add(resources.getString(key));
            }
        }
        return properties;
    }
}
