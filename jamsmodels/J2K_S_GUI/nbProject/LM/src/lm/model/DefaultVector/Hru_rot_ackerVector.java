/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lm.model.DefaultVector;

import java.util.ArrayList;
import lm.Componet.Vector.LMHru_rot_ackerVector;
import lm.Componet.Vector.LMLanduseVector;

/**
 *
 * @author MultiMedia
 */
public class Hru_rot_ackerVector implements LMHru_rot_ackerVector {
    
    private Integer iD;
    private Integer rID;
    private Integer rEDU;
    
    public Hru_rot_ackerVector(){
        this.iD=0;
        this.rID=null;
        this.rEDU=null;
    }
    
    public Hru_rot_ackerVector(ArrayList<String> a){
        this.iD=Integer.parseInt(a.get(0));
        this.rID=Integer.parseInt(a.get(1));
        this.rEDU=Integer.parseInt(a.get(2));
    }
    
    public void setAll(ArrayList<String> a) {
        this.iD=Integer.parseInt(a.get(0));
        this.rID=Integer.parseInt(a.get(1));
        this.rEDU=Integer.parseInt(a.get(2));
    }

    public String getRowForSave() {
        ArrayList<String>a=this.getAll();
        String s="";
        s=s+a.get(0);
        for(int i=1;i<a.size();i++){
           s=s+"\t"+a.get(i);
        }
        return s;
    }

    public LMHru_rot_ackerVector clone() {
        return new Hru_rot_ackerVector(this.getAll());
    }

    public Boolean isEmpty() {
        if(iD==0){
            return true;
        }else{
            return false;
        }
    }
    
    private ArrayList<String> getAll(){
        ArrayList<String> a =new ArrayList();
        a.add(iD+"");
        a.add(rID+"");
        a.add(rEDU+"");
        return a;
    }
    

    public void setID(Integer i) {
        this.iD=i;
    }

    public Integer getID() {
        return this.iD;
    }

    public void setRID(Integer i) {
        this.rID=i;
    }

    public Integer getRID() {
        return this.rID;
    }

    public void setREDU(Integer i) {
        this.rEDU=i;
    }

    public Integer getREDU() {
        return this.rEDU;
    }
    
}
