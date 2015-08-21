package lm.model.DefaultVector;

import java.util.ArrayList;
import lm.Componet.Vector.LMArableVector;
import lm.Componet.Vector.LMCropVector;
import lm.Componet.Vector.LMFertVector;
import lm.Componet.Vector.LMTillVector;
import lm.Componet.Vector.LMArableID;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class ArableVector implements LMArableVector {

    private Integer CID;
    private Integer year;
    private Integer Date;
    private Integer TID;
    private Integer FID;
    private Double FAmount;
    private Boolean PLANT;
    private Boolean HARVEST;
    private Double FRACHARV;

    

    public ArableVector(){
    this.CID=0;
    this.year=1;
    this.Date=1;
    this.TID=0;
    this.FID=0;
    this.FAmount=null;
    this.PLANT=false;
    this.HARVEST=false;
    this.FRACHARV=null;
    }
    public ArableVector(Integer cropVector, Integer date, Integer tillVector, Integer fertVector, Double faMount, Boolean plant, Boolean harvest, Double fracharv) {
        this.CID=cropVector;
        this.Date=date;
        this.TID=tillVector;
        this.FID=fertVector;
        this.FAmount=faMount;
        this.PLANT=plant;
        this.HARVEST=harvest;
        this.FRACHARV=fracharv;
    }

    public ArableVector(Integer cropVector,Integer year, Integer date, Integer tillVector, Integer fertVector, Double faMount, Boolean plant, Boolean harvest, Double fracharv) {
        this.CID=cropVector;
        this.year=year;
        this.Date=date;
        this.TID=tillVector;
        this.FID=fertVector;
        this.FAmount=faMount;
        this.PLANT=plant;
        this.HARVEST=harvest;
        this.FRACHARV=fracharv;
    }

    public LMArableVector getVector() {
        return this;
    }

    public Boolean CeckIfCorrect() {
        throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("NOT SUPPORTED YET."));
    }

    public Boolean isEmpty() {
        if (Date==null){
            return true;
        }else{
            return false;
        }
    }

    public void print(){
        System.out.println(CID + "  " + Date + "  " + TID + "  " + FID + "   ......");
    }

    public String getRowForSave(int ID){
        String s=ID + "\t"+CID  + "\t" + Date +"\t" + year +"\t";
        if(TID==null){
            s=s+"-"+"\t";
        }else{
            s=s+TID+"\t";
        }

        if(FID==null){
            s=s+"-"+"\t";
        }else{
            s=s+FID+"\t";
        }

        if(FAmount==null){
            s=s+"-"+"\t";
        }else{
            s=s+FAmount+"\t";
        }

        if(!PLANT){
            s=s+"-"+"\t";
        }else{
            s=s+"1"+"\t";
        }

        if(!HARVEST){
            s=s+"-"+"\t";
        }else{
            s=s+"1"+"\t";
        }

        if(FRACHARV==null){
            s=s+"-"+"\t";
        }else{
            s=s+FRACHARV+"\t";
        }
        return s;
    }

    public LMArableVector clone(){
        return new ArableVector(CID,year,Date,TID,FID,FAmount,PLANT,HARVEST,FRACHARV);
    }
    //Getter And Setter Methods
    //Getter And Setter ------>CID
    public void setCID(Integer key) {
        this.CID=key;
    }
    public Integer getCID() {
        return this.CID;
    }
    //Getter And Setter ----->Year
    public void setYear(Integer year){
        this.year=year;
    }
    public Integer getYear(){
        return this.year;
    }
    //Getter And Setter ------>Date
    public void setDate(Integer s) {
        this.Date=s;
    }
    public Integer getDate() {
        return this.Date;
    }
    //Getter And Setter ------>TID
    public void setTID(Integer key) {
        this.TID=key;
    }
    public Integer getTID() {
        return this.TID;
    }
    //Getter And Setter ------>FID
    public void setFID(Integer key) {
       
         this.FID=key;
        
    }
    public Integer getFID() {
        return this.FID;
    }
    //Getter And Setter ------>FAmount
    public void setFAmount(Double d) {
        this.FAmount=d;
    }
    public Double getFAmount() {
        return this.FAmount;
    }
    //Getter And Setter ------>PLANT
    public void setPLANT(Boolean b) {
        this.PLANT=b;
    }
    public Boolean getPLANT() {
        return this.PLANT;
    }
    //Getter And Setter ------>HARVEST
    public void setHARVEST(Boolean b) {
        this.HARVEST=b;
    }
    public Boolean getHARVEST() {
        return this.HARVEST;
    }
    //Getter And Setter ------>FRACHARV
    public void setFRACHARV(Double d) {
        this.FRACHARV=d;
    }
    public Double getFRACHARV() {
        return this.FRACHARV;
    }
}
