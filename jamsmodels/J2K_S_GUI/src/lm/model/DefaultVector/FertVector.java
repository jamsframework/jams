package lm.model.DefaultVector;

import java.util.ArrayList;
import lm.Componet.Vector.*;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class FertVector implements LMFertVector {

    private int ID;
    private String fertnm;
    private Double fminn;
    private Double fminp;
    private Double forgn;
    private Double forgp;
    private Double fnh3n;
    private Double bactpdb;
    private Double bactldb;
    private Double bactddb;
    private String desc;

    public FertVector() {
        this.ID=0;
        this.fertnm=null;
        this.fminn=null;
        this.fminp=null;
        this.forgn=null;
        this.forgp=null;
        this.fnh3n=null;
        this.bactpdb=null;
        this.bactldb=null;
        this.bactddb=null;
        this.desc=null;
    }
    public FertVector(ArrayList<String> toVector){
        this.ID=Integer.parseInt(toVector.get(0));
        this.fertnm=toVector.get(1);
        this.fminn=Double.valueOf(toVector.get(2));
        this.fminp=Double.valueOf(toVector.get(3));
        this.forgn=Double.valueOf(toVector.get(4));
        this.forgp=Double.valueOf(toVector.get(5));
        this.fnh3n=Double.valueOf(toVector.get(6));
        this.bactpdb=Double.valueOf(toVector.get(7));
        this.bactldb=Double.valueOf(toVector.get(8));
        this.bactddb=Double.valueOf(toVector.get(9));
        this.desc=toVector.get(10);
    }

    public LMFertVector getVector() {
        return this;
    }

    public boolean isEmpty(){
        if (this.ID==0){
            return true;
        }else{
            return false;
        }
    }

    public void setAll(ArrayList<String> toVector){
        this.ID=Integer.parseInt(toVector.get(0));
        this.fertnm=toVector.get(1);
        this.fminn=Double.valueOf(toVector.get(2));
        this.fminp=Double.valueOf(toVector.get(3));
        this.forgn=Double.valueOf(toVector.get(4));
        this.forgp=Double.valueOf(toVector.get(5));
        this.fnh3n=Double.valueOf(toVector.get(6));
        this.bactpdb=Double.valueOf(toVector.get(7));
        this.bactldb=Double.valueOf(toVector.get(8));
        this.bactddb=Double.valueOf(toVector.get(9));
        this.desc=toVector.get(10);
    }

    public Boolean CeckIfCorrect(){
        throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("NOT SUPPORTED YET."));

    }

    //Getter And Setter Methods
    //Getter And Setter ---->ID
    public void setID(int i) {
        this.ID=i;
    }
    public int getID() {
        return this.ID;
    }
    //Getter And Setter ---->fertnm
    public void setfertnm(String s) {
        this.fertnm=s;
    }
    public String getfertnm() {
        return this.fertnm;
    }
    //Getter And Setter ---->fminn
    public void setfminn(Double i) {
        this.fminn=i;
    }
    public Double getfminn() {
        return this.fminn;
    }
    //Getter And Setter ---->fminp
    public void setfminp(Double i) {
        this.fminp=i;
    }
    public Double getfminp() {
        return this.fminp;
    }
    //Getter And Setter ---->forgn
    public void setforgn(Double i) {
        this.forgn=i;
    }
    public Double getforgn() {
        return this.forgn;
    }
    //Getter And Setter ---->forgp
    public void setforgp(Double i) {
        this.forgp=i;
    }
    public Double getforgp() {
        return this.forgp;
    }
    //Getter And Setter ---->fnh3n
    public void setfnh3n(Double i) {
        this.fnh3n=i;
    }
    public Double getfnh3n() {
        return this.fnh3n;
    }
    //Getter And Setter ---->bactpdb
    public void setbactpdb(Double i) {
        this.bactpdb=i;
    }
    public Double getbactpdb() {
        return this.bactpdb;
    }
    //Getter And Setter ---->bactldb
    public void setbactldb(Double i) {
        this.bactldb=i;
    }
    public Double getbactldb() {
        return this.bactldb;
    }
    //Getter And Setter ---->bactddb
    public void setbactddb(Double i) {
        this.bactddb=i;
    }
    public Double getbactddb() {
        return this.bactddb;
    }
    //Getter And Setter ---->desc
    public void setdesc(String s) {
        this.desc=s;
    }
    public String getdesc() {
        return this.desc;
    }

}
