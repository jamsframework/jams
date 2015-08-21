/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lm.Componet.errors;

import java.util.ArrayList;

/**
 *
 * @author MultiMedia
 */
public interface Ierror_delete_LM_ID {
    
    //parameter für ArrayList, ID , decision
    
    public void setArrayListElement(String s);
    public String getArrayListElement(int i);
    
    public void setArrayList (ArrayList a);
    public ArrayList getArrayList();
    
    public void setID (Integer iD);
    public Integer getID();
    
    public void setDecision(ArrayList decision);
    public ArrayList getDecision();
    
    
}
