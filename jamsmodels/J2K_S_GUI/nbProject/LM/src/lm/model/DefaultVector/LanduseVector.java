/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lm.model.DefaultVector;

import java.util.ArrayList;
import lm.Componet.Vector.LMCropVector;
import lm.Componet.Vector.LMLanduseVector;

/**
 *
 * @author MultiMedia
 */
public class LanduseVector implements LMLanduseVector {
    
    private Integer id;
    private Double albedo;
    private Integer rSCO_1;
    private Integer rSCO_2;
    private Integer rSCO_3;
    private Integer rSCO_4;
    private Integer rSCO_5;
    private Integer rSCO_6;
    private Integer rSCO_7;
    private Integer rSCO_8;
    private Integer rSCO_9;
    private Integer rSCO_10;
    private Integer rSCO_11;
    private Integer rSCO_12;
    private Integer lAI_d1;
    private Integer lAI_d2;
    private Integer lAI_d3;
    private Integer lAI_d4;
    private Double effHeight_d1;
    private Double effHeight_d2;
    private Double effHeight_d3;
    private Double effHeight_d4;
    private Double rootDepth;
    private Double sealedGrade;
    
    public LanduseVector(){
        this.id=0;
        this.albedo=null;
        this.rSCO_1=null;
        this.rSCO_2=null;
        this.rSCO_3=null;
        this.rSCO_4=null;
        this.rSCO_5=null;
        this.rSCO_6=null;
        this.rSCO_7=null;
        this.rSCO_8=null;
        this.rSCO_9=null;
        this.rSCO_10=null;
        this.rSCO_11=null;
        this.rSCO_12=null;
        this.lAI_d1=null;
        this.lAI_d2=null;
        this.lAI_d3=null;
        this.lAI_d4=null;
        this.effHeight_d1=null;
        this.effHeight_d2=null;
        this.effHeight_d3=null;
        this.effHeight_d4=null;
        this.rootDepth=null;
        this.sealedGrade=null;
    }
    
    public LanduseVector(ArrayList<String> a){
        this.id=Integer.parseInt(a.get(0));
        this.albedo=Double.valueOf(a.get(1));
        this.rSCO_1=Integer.parseInt(a.get(2));
        this.rSCO_2=Integer.parseInt(a.get(3));
        this.rSCO_3=Integer.parseInt(a.get(4));
        this.rSCO_4=Integer.parseInt(a.get(5));
        this.rSCO_5=Integer.parseInt(a.get(6));
        this.rSCO_6=Integer.parseInt(a.get(7));
        this.rSCO_7=Integer.parseInt(a.get(8));
        this.rSCO_8=Integer.parseInt(a.get(9));
        this.rSCO_9=Integer.parseInt(a.get(10));
        this.rSCO_10=Integer.parseInt(a.get(11));
        this.rSCO_11=Integer.parseInt(a.get(12));
        this.rSCO_12=Integer.parseInt(a.get(13));
        this.lAI_d1=Integer.parseInt(a.get(14));
        this.lAI_d2=Integer.parseInt(a.get(15));
        this.lAI_d3=Integer.parseInt(a.get(16));
        this.lAI_d4=Integer.parseInt(a.get(17));
        this.effHeight_d1=Double.valueOf(a.get(18));
        this.effHeight_d2=Double.valueOf(a.get(19));
        this.effHeight_d3=Double.valueOf(a.get(20));
        this.effHeight_d4=Double.valueOf(a.get(21));
        this.rootDepth=Double.valueOf(a.get(22));
        this.sealedGrade=Double.valueOf(a.get(23));
    }
    
    public void setAll(ArrayList<String> a){
        this.id=Integer.parseInt(a.get(0));
        this.albedo=Double.valueOf(a.get(1));
        this.rSCO_1=Integer.parseInt(a.get(2));
        this.rSCO_2=Integer.parseInt(a.get(3));
        this.rSCO_3=Integer.parseInt(a.get(4));
        this.rSCO_4=Integer.parseInt(a.get(5));
        this.rSCO_5=Integer.parseInt(a.get(6));
        this.rSCO_6=Integer.parseInt(a.get(7));
        this.rSCO_7=Integer.parseInt(a.get(8));
        this.rSCO_8=Integer.parseInt(a.get(9));
        this.rSCO_9=Integer.parseInt(a.get(10));
        this.rSCO_10=Integer.parseInt(a.get(11));
        this.rSCO_11=Integer.parseInt(a.get(12));
        this.rSCO_12=Integer.parseInt(a.get(13));
        this.lAI_d1=Integer.parseInt(a.get(14));
        this.lAI_d2=Integer.parseInt(a.get(15));
        this.lAI_d3=Integer.parseInt(a.get(16));
        this.lAI_d4=Integer.parseInt(a.get(17));
        this.effHeight_d1=Double.valueOf(a.get(18));
        this.effHeight_d2=Double.valueOf(a.get(19));
        this.effHeight_d3=Double.valueOf(a.get(20));
        this.effHeight_d4=Double.valueOf(a.get(21));
        this.rootDepth=Double.valueOf(a.get(22));
        this.sealedGrade=Double.valueOf(a.get(23));
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
    
    public LMLanduseVector clone(){
        return new LanduseVector(this.getAll());
    }
    
    private ArrayList<String> getAll() {
        ArrayList<String> a =new ArrayList();
        a.add(this.id +"");
        a.add(this.albedo+"");
        a.add(this.rSCO_1+"");
        a.add(this.rSCO_2+"");
        a.add(this.rSCO_3+"");
        a.add(this.rSCO_4+"");
        a.add(this.rSCO_5+"");
        a.add(this.rSCO_6+"");
        a.add(this.rSCO_7+"");
        a.add(this.rSCO_8+"");
        a.add(this.rSCO_9+"");
        a.add(this.rSCO_10+"");
        a.add(this.rSCO_11+"");
        a.add(this.rSCO_12+"");
        a.add(this.lAI_d1+"");
        a.add(this.lAI_d2+"");
        a.add(this.lAI_d3+"");
        a.add(this.lAI_d4+"");
        a.add(this.effHeight_d1+"");
        a.add(this.effHeight_d2+"");
        a.add(this.effHeight_d3+"");
        a.add(this.effHeight_d4+"");
        a.add(this.rootDepth+"");
        a.add(this.sealedGrade+"");        
        return a;
    }
    
    public Boolean isEmpty() {
        if(id==0){
            return true;
        }else{
            return false;
        }
    }
    

    public void setLID(Integer id) {
        this.id=id;
    }

    public Integer getLID() {
        return id;
    }

    public void setAlbedo(Double d) {
        this.albedo=d;
    }

    public Double getAlbedo() {
        return albedo;
    }

    public void setRSC0_1(Integer i) {
        this.rSCO_1=i;
    }

    public Integer getRSCO_1() {
        return rSCO_1;
    }

    public void setRSC0_2(Integer i) {
        this.rSCO_2=i;
    }

    public Integer getRSCO_2() {
        return rSCO_2;
    }

    public void setRSC0_3(Integer i) {
        this.rSCO_3=i;
    }

    public Integer getRSCO_3() {
        return rSCO_3;
    }

    public void setRSC0_4(Integer i) {
        this.rSCO_4=i;
    }

    public Integer getRSCO_4() {
        return rSCO_4;
    }

    public void setRSC0_5(Integer i) {
        this.rSCO_5=i;
    }

    public Integer getRSCO_5() {
        return rSCO_5;
    }

    public void setRSC0_6(Integer i) {
        this.rSCO_6=i;
    }

    public Integer getRSCO_6() {
        return rSCO_6;
    }

    public void setRSC0_7(Integer i) {
        this.rSCO_7=i;
    }

    public Integer getRSCO_7() {
        return rSCO_7;
    }

    public void setRSC0_8(Integer i) {
        this.rSCO_8=i;
    }

    public Integer getRSCO_8() {
        return rSCO_8;
    }

    public void setRSC0_9(Integer i) {
        this.rSCO_9=i;
    }

    public Integer getRSCO_9() {
        return rSCO_9;
    }

    public void setRSC0_10(Integer i) {
        this.rSCO_10=i;
    }

    public Integer getRSCO_10() {
        return rSCO_10;
    }

    public void setRSC0_11(Integer i) {
        this.rSCO_11=i;
    }

    public Integer getRSCO_11() {
        return rSCO_11;
    }

    public void setRSC0_12(Integer i) {
        this.rSCO_12=i;
    }

    public Integer getRSCO_12() {
        return rSCO_12;
    }

    public void setLAI_d1(Integer i) {
        this.lAI_d1=i;
    }

    public Integer getLAI_d1() {
        return lAI_d1;
    }

    public void setLAI_d2(Integer i) {
        this.lAI_d2=i;
    }

    public Integer getLAI_d2() {
        return lAI_d2;
    }

    public void setLAI_d3(Integer i) {
        this.lAI_d3=i;
    }

    public Integer getLAI_d3() {
        return lAI_d3;
    }

    public void setLAI_d4(Integer i) {
        this.lAI_d4=i;
    }

    public Integer getLAI_d4() {
        return lAI_d4;
    }

    public void setEffHeight_d1(Double d) {
        this.effHeight_d1=d;
    }

    public Double getEffHeight_d1() {
        return effHeight_d1;
    }

    public void setEffHeight_d2(Double d) {
        this.effHeight_d2=d;
    }

    public Double getEffHeight_d2() {
        return effHeight_d2;
    }

    public void setEffHeight_d3(Double d) {
        this.effHeight_d3=d;
    }

    public Double getEffHeight_d3() {
        return effHeight_d3;
    }

    public void setEffHeight_d4(Double d) {
        this.effHeight_d4=d;
    }

    public Double getEffHeight_d4() {
        return effHeight_d4;
    }

    public void setRootDepth(Double d) {
        this.rootDepth=d;
    }

    public Double getRootDepth() {
        return rootDepth;
    }

    public void setSealedGrade(Double d) {
        this.sealedGrade=d;
    }

    public Double getSealedGrade() {
        return sealedGrade;
    }
    
}
