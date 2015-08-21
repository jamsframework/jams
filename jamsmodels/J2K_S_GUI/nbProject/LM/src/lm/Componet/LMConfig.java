/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.Componet;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public interface LMConfig {

    

    public void tellObserver(int i);
    public String[] getAllPaths();
    public void setAllPaths(String[] paths);
    public void setTill(String t);
    public String getTill();
    public void setCrop(String c);
    public String getCrop();
    public void setFert(String f);
    public String getFert();
    public void setLmArable (String a);
    public String getLmArable();
    public void setCropRotation(String s);
    public String getCropRotation();
    public String getPath();
    public void setPath(String Path);
    public void setLanduse(String s);
    public String getLanduse();
    public void setHru_rot_acker(String s);
    public String getHru_rot_acker();
    
    public Boolean getLmArableCheck();
    public Boolean getCropCheck();
    public Boolean getTillCheck();
    public Boolean getFertCheck();
    public Boolean getCropRotationCheck();
    public Boolean getLanduseCheck();
    public Boolean getHru_rot_ackerCheck();
    
    
    
   
}
