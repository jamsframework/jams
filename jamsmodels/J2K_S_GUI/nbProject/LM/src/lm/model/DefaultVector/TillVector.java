package lm.model.DefaultVector;

import java.util.ArrayList;
import lm.Componet.Vector.LMTillVector;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class TillVector implements LMTillVector {
    
    private int ID;
    private String tillnm;
    private String desc;
    private Double effmix;
    private Double deptil;


    public TillVector(){
     this.ID=0;
     this.tillnm=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("");
     this.desc=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("");
     this.effmix=0.0;
     this.deptil=0.0;
    }

    public TillVector (ArrayList<String> a){
     this.ID=Integer.parseInt(a.get(0));
     this.tillnm=a.get(1);
     this.desc=a.get(2);
     this.effmix=Double.valueOf(a.get(3));
     this.deptil=Double.valueOf(a.get(4));
    }

    public void setAll (ArrayList<String> a){
        this.ID=Integer.parseInt(a.get(0));
        this.tillnm=a.get(1);
        this.desc=a.get(2);
        this.effmix=Double.valueOf(a.get(3));
        this.deptil=Double.valueOf(a.get(4));
    }

    public LMTillVector getVector() {
        return this;
    }
    public Boolean CeckIfCorrect() {
        throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("NOT SUPPORTED YET."));
    }

    public Boolean isEmpty() {
        if(this.ID==0){
            return true;
        }else{
            return false;
        }
    }

    public String getRowForSave(){
        ArrayList<String>a=this.getAll();
        String s="";
        s=s+a.get(0);
        for(int i=1;i<a.size();i++){
           s=s+"\t"+a.get(i);
        }
        return s;
    }

    public LMTillVector clone(){
        return new TillVector(this.getAll());

    }

    public ArrayList<String> getAll(){
        ArrayList<String> a=new ArrayList();
        a.add(ID+"");
        a.add(tillnm+"");
        a.add(desc);
        a.add(effmix+"");
        a.add(deptil+"");
        return a;
    }

    public void print(){
        System.out.println(ID + "  " + tillnm + "  " + desc+ "  " + effmix + "  " + deptil);
    }
    //Getter And Setter Methods
    //Getter And Setter ------->TID
    public void setTID(int i) {
        this.ID=i;
    }
     public int getTID() {
        return this.ID;
    }
    //Getter And Setter ------->tillnm
    public void settillnm(String s) {
        this.tillnm=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("");
    }
    public String gettillnm() {
        return this.tillnm;
    }
    //Getter And Setter ------->desc
    public void setdesc(String s) {
        this.desc=s;
    }
    public String getdesc() {
        return this.desc;
    }
    //Getter And Setter ------->effmix
    public void seteffmix(Double d) {
        this.effmix=d;
    }
    public Double geteffmix() {
        return this.effmix;
    }
    //Getter And Setter ------->deptil
    public void setdeptil(Double d) {
        this.deptil=d;
    }
    public Double getdeptil() {
        return this.deptil;
    }

}