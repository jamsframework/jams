/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lm.model.errors;

import java.util.ArrayList;
import lm.Componet.Vector.LMArableID;
import lm.Componet.errors.Ierror_change_duration_LM_ID;

/**
 *
 * @author MultiMedia
 */
public class Error_change_duration_LM_ID implements Ierror_change_duration_LM_ID {
    
    private ArrayList<String> a;
    private Integer iD;
    private ArrayList<Integer> decision;
    private ArrayList<Boolean> possible;
    private int minus;
    private int plus;
    private LMArableID aid;
    
    

    public Error_change_duration_LM_ID(){
        a=new ArrayList<String>();
        decision=new ArrayList<Integer>();
        possible=new ArrayList<Boolean>();
        
    }
    
    public Boolean isEmpty(){
        return a.isEmpty();
    }
    public Boolean noChange(){
        if(minus==0&&plus==0){
            return true;
        }
        return false;   
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

    public void setPossible(ArrayList b) {
        this.possible=b;
    }

    public ArrayList getPossible() {
        return possible;
    }

    public void setMinus(int i) {
        minus=i;
    }

    public int getMinus() {
        return minus;
    }

    public void setPlus(int i) {
        plus=i;
    }

    public int getPlus() {
        return plus;
    }

    public void setNewAID(LMArableID aid) {
        this.aid=aid;
    }

    public LMArableID getNewAID() {
        return aid;
    }

    public Boolean completeSmaller() {
        for(int i=0;i<decision.size();i++){
            if(decision.get(i)!=-1){
                return false;
            }
        }
        return true;
    }
    
}
