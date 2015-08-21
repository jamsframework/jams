/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.tools;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

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
        try {
            ResourceBundle resources = java.util.ResourceBundle.getBundle(thePropertyFile);

            Enumeration<String> keys = resources.getKeys();
            while (keys != null && keys.hasMoreElements()) {
                String key = keys.nextElement();
                if (key.startsWith(thePropertyGroupName)) {
                    properties.add(resources.getString(key));
                }
            }
        } catch (MissingResourceException mre) {
            System.out.println("No resource file found!");
        }
        return properties;
    }
}
