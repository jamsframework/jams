/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.tools;

import java.util.MissingResourceException;

/**
 *
 * @author chris
 */
public class Resources {
    public static String get(String key){
        try{
            return java.util.ResourceBundle.getBundle("optas/resources/OPTASBundle").getString(key);
        }catch(MissingResourceException mre){
            System.out.println("resource missing:" + key);
            return key;
        }
    }
}
