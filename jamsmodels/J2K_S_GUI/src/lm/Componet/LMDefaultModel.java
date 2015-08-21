/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.Componet;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeMap;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public interface LMDefaultModel {

    public void createDefaultModel();

    public TreeMap getLMFert();
    public TreeMap getLMTill();
    public TreeMap getLMCrop();
    public TreeMap getLMArable();
    public TreeMap getLMCropRotation();

    public void tellObservers(int i);

    public void SystemPrint();

    public void setPaths(String lmArable,String crop,String till,String fert,String cropRotation);

}
