/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gw.ui.util;

/**
 *
 * @author hbusch
 */
public class Tools {

    /**
     * get name with "id" from a list of names
     * @param theNames
     * @return fitting name or null
     */
    public static String geFittingIdName(String[] theNames) {
        String theName;
        for (int i = 0; i < theNames.length; i++) {
            theName = theNames[i];
            if (theName.endsWith("id") || theName.endsWith("ID")) {
                return theName;
            }
        }
        return null;
    }

}
