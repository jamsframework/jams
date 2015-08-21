/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lm.Componet.errors;

import java.util.ArrayList;
import lm.Componet.Vector.LMArableID;

/**
 *
 * @author MultiMedia
 */
public interface Ierror_change_duration_LM_ID {
    
    
    
    
    public void setArrayListElement(String s);
    public String getArrayListElement(int i);
    
    public void setArrayList (ArrayList a);
    public ArrayList getArrayList();
    
    public void setID (Integer iD);
    public Integer getID();
    
    public void setDecision(ArrayList<Integer> decision);
    public ArrayList<Integer> getDecision();
    
    public void setPossible(ArrayList b);
    public ArrayList getPossible();
    
    public void setMinus(int i);
    public int getMinus();
    
    public void setPlus(int i);
    public int getPlus();
    
    public void setNewAID(LMArableID aid);
    public LMArableID getNewAID();
    
    public Boolean isEmpty();
    public Boolean noChange();
    
    public Boolean completeSmaller();
    
}
