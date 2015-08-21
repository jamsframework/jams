/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.model;

import lm.Componet.*;
import java.io.File;
import java.util.Observable;
import java.util.Properties;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class Config_data extends Observable implements LMConfig{

    private static  String lmArable=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("");
    private static  String till=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("");
    private static  String fert=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("");
    private static  String crop=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("");
    private static  String Path=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("");
    private static  String cropRotation=java.util.ResourceBundle.getBundle("ressources/Ressources").getString("");

    public void Check(String Path){
        this.Path=Path;

        File file=new File(Path +java.util.ResourceBundle.getBundle("ressources/Ressources").getString("/")  +java.util.ResourceBundle.getBundle("lm/properties/properties").getString("lmArable"));
        if(file.exists()){
            String t = file.getAbsolutePath();
            this.setLmArable(t);
        }
        file=new File(Path +java.util.ResourceBundle.getBundle("ressources/Ressources").getString("/")  +java.util.ResourceBundle.getBundle("lm/properties/properties").getString("fert"));
        if(file.exists()){
            String t = file.getAbsolutePath();
            this.setFert(t);
        }
        file=new File(Path +java.util.ResourceBundle.getBundle("ressources/Ressources").getString("/")  +java.util.ResourceBundle.getBundle("lm/properties/properties").getString("till"));
        if(file.exists()){
            String t = file.getAbsolutePath();
            this.setTill(t);
        }
        file=new File(Path +java.util.ResourceBundle.getBundle("ressources/Ressources").getString("/")  +java.util.ResourceBundle.getBundle("lm/properties/properties").getString("crop"));
        if(file.exists()){
            String t = file.getAbsolutePath();
            this.setCrop(t);
        }
        file=new File(Path +java.util.ResourceBundle.getBundle("ressources/Ressources").getString("/") + java.util.ResourceBundle.getBundle("lm/properties/properties").getString("cropRotation"));
        if(file.exists()){
            String t = file.getAbsolutePath();
            this.setCropRotation(t);
        }
        System.out.println(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("2.   NOTIFYOBERSERVS"));
    }
    public void tellObserver(int i){
        setChanged();
        notifyObservers(i);
    }

    public void setTill(String t){
                this.till=t;
    }
    public String getTill(){
        return this.till;
    }
    public void setCrop(String c){
                this.crop=c;
    }
    public String getCrop(){
        return this.crop;
    }
    public void setFert(String f){
                this.fert=f;
    }
    public String getFert(){
        return this.fert;
    }
    public void setLmArable (String a){
        this.lmArable=a;
    }
    public String getLmArable(){
        return this.lmArable;
    }
     public void setCropRotation(String s) {
        cropRotation=s;
    }

    public String getCropRotation() {
        return cropRotation;
    }
    public String getPath(){
        return Path;
    }
    public void setPath(String Path){
        this.Path=Path;
    }


    public void print(){

        System.out.println(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("HURRA!"));
    
    }

    public String[] getAllPaths(){
        return new String[2];
    }

    public void setAllPaths(String[] paths){

    }
    public boolean exsit (String absolutPath){
        boolean exist;
        File file = new File(absolutPath);
        if(file.exists()){
            exist=true;
        }else{
            exist=false;
        }
        return exist;
    }


}
