/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.Componet;

import java.util.TreeMap;
import lm.Componet.Vector.LMArableID;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public interface LMDefaultModel {
    
    public Object clone()throws CloneNotSupportedException;

    public void createDefaultModel();

    public TreeMap getLMFert();
    public TreeMap getLMTill();
    public TreeMap getLMCrop();
    public TreeMap<Integer,LMArableID> getLMArable();
    public TreeMap getLMCropRotation();
    public TreeMap getLMLanduse();
    public TreeMap getLMHru_rot_acker();

    public void tellObservers(int i);

    public void SystemPrint();

    public void setPaths(String [] s);

}
