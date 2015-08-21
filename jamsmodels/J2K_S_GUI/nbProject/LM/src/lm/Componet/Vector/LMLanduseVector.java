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
public interface LMLanduseVector {
    
    public void setAll(ArrayList<String> a);
    public String getRowForSave();
    public LMLanduseVector clone();
    public Boolean isEmpty();
    
    //Getter and Setter der LID
    public void setLID (Integer id);
    public Integer getLID ();
    
    //Getter and Setter der albedo
    public void setAlbedo(Double d);
    public Double getAlbedo();
    
    //Getter and Setter der RSC0_1
    public void setRSC0_1 (Integer i);
    public Integer getRSCO_1();
    
    //Getter and Setter der RSC0_2
    public void setRSC0_2 (Integer i);
    public Integer getRSCO_2();
    
    //Getter and Setter der RSC0_3
    public void setRSC0_3 (Integer i);
    public Integer getRSCO_3();
    
    //Getter and Setter der RSC0_4
    public void setRSC0_4 (Integer i);
    public Integer getRSCO_4();
    
    //Getter and Setter der RSC0_5
    public void setRSC0_5 (Integer i);
    public Integer getRSCO_5();
    
    //Getter and Setter der RSC0_6
    public void setRSC0_6 (Integer i);
    public Integer getRSCO_6();
    
    //Getter and Setter der RSC0_7
    public void setRSC0_7 (Integer i);
    public Integer getRSCO_7();
    
    //Getter and Setter der RSC0_8
    public void setRSC0_8 (Integer i);
    public Integer getRSCO_8();
    
    //Getter and Setter der RSC0_9
    public void setRSC0_9 (Integer i);
    public Integer getRSCO_9();
    
    //Getter and Setter der RSC0_10
    public void setRSC0_10 (Integer i);
    public Integer getRSCO_10();
    
    //Getter and Setter der RSC0_11
    public void setRSC0_11 (Integer i);
    public Integer getRSCO_11();
    
    //Getter and Setter der RSC0_12
    public void setRSC0_12 (Integer i);
    public Integer getRSCO_12();
    
     //Getter and Setter der LAI_d1
    public void setLAI_d1 (Integer i);
    public Integer getLAI_d1();
            
    //Getter and Setter der LAI_d2
    public void setLAI_d2 (Integer i);
    public Integer getLAI_d2();
    
    //Getter and Setter der LAI_d3
    public void setLAI_d3 (Integer i);
    public Integer getLAI_d3();
    
    //Getter and Setter der LAI_d4
    public void setLAI_d4 (Integer i);
    public Integer getLAI_d4();
    
    //Getter and Setter der effHeight_d1
    public void setEffHeight_d1 (Double d);
    public Double getEffHeight_d1();
    
    //Getter and Setter der effHeight_d2
    public void setEffHeight_d2 (Double d);
    public Double getEffHeight_d2();
    
    //Getter and Setter der effHeight_d3
    public void setEffHeight_d3 (Double d);
    public Double getEffHeight_d3();
    
    //Getter and Setter der effHeight_d4
    public void setEffHeight_d4 (Double d);
    public Double getEffHeight_d4();
    
    //Getter and Setter der rootDepth
    public void setRootDepth (Double d);
    public Double getRootDepth();
    
    //Getter and Setter der sealedGrade
    public void setSealedGrade (Double d);
    public Double getSealedGrade();
    
}
