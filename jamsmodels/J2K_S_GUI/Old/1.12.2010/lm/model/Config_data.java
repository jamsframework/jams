/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.model;

import lm.Componet.*;
import java.io.File;
import java.util.Observable;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class Config_data extends Observable implements LMConfig{

    private static  String lmArable="";
    private static  String till="";
    private static  String fert="";
    private static  String crop="";
    private static  String Path="";

    public void Check(String Path){
        this.Path=Path;
        File file=new File(Path +"/lmArable.par");
        if(file.exists()){
            String t = file.getAbsolutePath();
            this.setLmArable(t);
        }
        file=new File(Path +"/fert.par");
        if(file.exists()){
            String t = file.getAbsolutePath();
            this.setFert(t);
        }
        file=new File(Path +"/till.par");
        if(file.exists()){
            String t = file.getAbsolutePath();
            this.setTill(t);
        }
        file=new File(Path +"/crop.par");
        if(file.exists()){
            String t = file.getAbsolutePath();
            this.setCrop(t);
        }
        System.out.println("2.   notifyOberservs");
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
    public String getPath(){
        return Path;
    }
    public void setPath(String Path){
        this.Path=Path;
    }


    public void print(){

        System.out.println("Hurra!");
    
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
