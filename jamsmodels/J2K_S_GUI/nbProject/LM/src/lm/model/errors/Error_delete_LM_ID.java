/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lm.model.errors;

import java.util.ArrayList;
import lm.Componet.errors.Ierror_delete_LM_ID;

/**
 *
 * @author MultiMedia
 */
public class Error_delete_LM_ID implements Ierror_delete_LM_ID {
    
    ArrayList<String> a;
    Integer iD;
    ArrayList<Integer> decision;
    
    public Error_delete_LM_ID(){
        a=new ArrayList<String>();
        decision=new ArrayList<Integer>();
        
    }

    public void setArrayList(ArrayList a) {
        this.a=a;
    }

    public ArrayList getArrayList() {
        return a;
    }

    public void setID(Integer iD) {
        this.iD=iD;
    }

    public Integer getID() {
        return iD;
    }

    public void setDecision(ArrayList decision) {
        this.decision=decision;
    }

    public ArrayList getDecision() {
        return decision;
    }

    public void setArrayListElement(String s) {
        a.add(s);
    }

    public String getArrayListElement(int i) {
        return a.get(i);
    }

    public void setDecisionElement(Integer i, Integer d) {
        decision.set(i, d);
    }
    
}
