/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lm.Componet.Vector;

import java.util.ArrayList;

/**
 *
 * @author MultiMedia
 */
public interface LMHru_rot_ackerVector {
    
    public void setAll(ArrayList<String> a);
    public String getRowForSave();
    public LMHru_rot_ackerVector clone();
    public Boolean isEmpty();
    
    //Getter and Setter der ID
    public void setID (Integer i);
    public Integer getID();
    
    //Getter and Setter der RID
    public void setRID (Integer i);
    public Integer getRID();
    
    //Getter and Setter der REDU
    public void setREDU (Integer i);
    public Integer getREDU();
    
}
