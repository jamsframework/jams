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

    public static  String lmArable="";
    public static  String till="";
    public static  String fert="";
    public static  String crop="";
    public static  String Path="";
    public static  String cropRotationPath="";

    public void tellObserver(int i);
    public void print();
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
    public boolean exsit (String absolutPath);
}
