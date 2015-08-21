/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lm.Componet.*;
import java.io.File;
import java.io.FileReader;
import java.util.Observable;
import java.util.Properties;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class Config_data extends Observable implements LMConfig{
    
    private Boolean lmArableCheck=false;
    private Boolean tillCheck=false;
    private Boolean fertCheck=false;
    private Boolean cropCheck=false;
    private Boolean PathCheck=false;
    private Boolean cropRotationCheck=false;
    private Boolean landuseCheck=false;
    private Boolean hru_rot_ackerCheck=false;

    private String lmArable;
    private String till;
    private String fert;
    private String crop;
    private String path;
    private String cropRotation;
    private String landuse;
    private String hru_rot_acker;

    private String lmArableHead;
    private String tillHead;
    private String fertHead;
    private String cropHead;
    private String cropRotationHead;
    private String landuseHead;
    private String hru_rot_ackerHead;

    public void saveHeader(){
        try {
            FileReader readerCrop = new FileReader(crop);
            FileReader readerTill = new FileReader(till);
            FileReader readerFert = new FileReader(fert);
            FileReader readerLmArable = new FileReader(lmArable);
            FileReader readerCropRotation = new FileReader(cropRotation);
            FileReader readerLanduse = new FileReader(landuse);
            FileReader readerHru_rot_acker = new FileReader(hru_rot_acker);
            BufferedReader BuffReaderCrop = new BufferedReader(readerCrop);
            BufferedReader BuffReaderTill = new BufferedReader(readerTill);
            BufferedReader BuffReaderFert = new BufferedReader(readerFert);
            BufferedReader BuffReaderLmArable = new BufferedReader(readerLmArable);
            BufferedReader BuffReaderCropRotation = new BufferedReader(readerCropRotation);            
            BufferedReader BuffReaderLanduse = new BufferedReader(readerLanduse);
            BufferedReader BuffReaderHru_rot_acker = new BufferedReader(readerHru_rot_acker);

            saveCropHead(BuffReaderCrop);
            saveTillHead(BuffReaderTill);
            saveFertHead(BuffReaderFert);
            saveLMArableHead(BuffReaderLmArable);
            saveCropRotationHead(BuffReaderCropRotation);
            saveLanduseHead(BuffReaderLanduse);
            saveHru_rot_acker(BuffReaderHru_rot_acker);



        } catch (FileNotFoundException ex) {

        }
     }

    private void saveCropHead(BufferedReader toUse){
        try{
            String t="#";
            String s="";
            while(t.startsWith("#")){
                t=toUse.readLine();
                if(t.startsWith("CID")){
                    s=s+t;
                }else{
                    s=s+t+System.lineSeparator();
                }
            }
            setCropHead(t);

        }catch(IOException e){

        }
    }

    private void saveTillHead(BufferedReader toUse){
        try{
            String t="#";
            String s="";
            while(t.startsWith("#")){
                t=toUse.readLine();
                if(t.startsWith("TID")){
                    s=s+t;
                }else{
                    s=s+t+System.lineSeparator();
                }
            }
            setTillHead(s);
        }catch(IOException e){

        }
    }

    private void saveFertHead(BufferedReader toUse){
        try{
            String t="#";
            String s="";
            while(t.startsWith("#")){
                t=toUse.readLine();
                if(t.startsWith("FID")){
                    s=s+t;
                }else{
                    s=s+t+System.lineSeparator();
                }
            }
            setFertHead(s);
        }catch(IOException e){

        }
    }

    private void saveLMArableHead(BufferedReader toUse){
        try{
            String t="#";
            String s="";
            while(t.startsWith("#")){
                t=toUse.readLine();
                  if(!t.startsWith("#")){
                      String[]head=t.split("\t");
                      if(head[0].equals("CID")){
                          String[]head2=new String[head.length+1];
                          head2[0]="ID";
                          for(int i=0;i<head.length;i++){
                              head2[i+1]=head[i];
                          }
                          head=head2.clone();
                      }
                      if(!head[3].equals("YEAR")){
                          String[]head3=new String[head.length+1];
                          head3[0]=head[0];
                          head3[1]=head[1];
                          head3[2]=head[2];
                          head3[3]="YEAR";
                          for(int i=3;i<head.length;i++){
                              head3[i+1]=head[i];
                          }
                          head=head3.clone();
                      }
                      t="";
                      for(int i=0;i<head.length;i++){
                          if(i+1==head.length){
                                t=t+head[i];
                          }else{
                                t=t+head[i]+"\t";
                          }
                      }
                     s=s+t;
                  }else{

                    s=s+t+System.lineSeparator();
                }
                
            }
            setLmArableHead(s);
        }catch(IOException e){

        }
    }

    private void saveCropRotationHead(BufferedReader toUse){
        
            String t="";
            t = (t+"RID");
            for(int i=0;i<20;i++){
                t=t+"\t"+"AID";
            }
            setCropRotationHead(t);
        
    }
    
    private void saveLanduseHead(BufferedReader toUse){
        try{
            String t="#";
            while(t.startsWith("#")){
                t=toUse.readLine();
            }
            setLanduseHead(t);if(t.equals(java.util.ResourceBundle.getBundle("lm/properties/properties").getString("landuseHead"))){
            }else{
                
            }
        }catch(IOException e){

        }
    }
    
    private void saveHru_rot_acker(BufferedReader toUse){
        try{
            String t="#";
            while(t.startsWith("#")){
                t=toUse.readLine();
            }
            setHru_rot_ackerHead(t);
        }catch(IOException e){

        }
    }
    
    public void setNewPath(String s){
        this.path=s;
        this.Check();
        setChanged();
        notifyObservers(0);
    }
    
    public void checkLmArablePath(String s,Boolean b){
        //Check if lmArable exsist
        if(b){
           File file=new File(path +java.util.ResourceBundle.getBundle("ressources/Ressources").getString("/")  +java.util.ResourceBundle.getBundle("lm/properties/properties").getString("lmArable"));
           if(file.exists()){
                String t = file.getAbsolutePath();
                this.setLmArable(t);
                lmArableCheck=true;
            }else{
               this.setLmArable("");
                lmArableCheck= false;
           }    
        }else{
            File file =new File(s);
            if(file.exists()&&s.endsWith(java.util.ResourceBundle.getBundle("lm/properties/properties").getString("lmArable"))){
                String t = file.getAbsolutePath();
                this.setLmArable(t);
                lmArableCheck=true;
            }else{
                String t = file.getAbsolutePath();
                this.setLmArable(t);
                lmArableCheck= false;
            }
            
        }
        setChanged();
        notifyObservers(0);  
    }
    
    public void checkCropPath(String s, Boolean b){
        //Check if lmArable exsist
        // b stands from Path (true)
        if(b){
           File file=new File(path +java.util.ResourceBundle.getBundle("ressources/Ressources").getString("/")  +java.util.ResourceBundle.getBundle("lm/properties/properties").getString("crop"));
           if(file.exists()){
                String t = file.getAbsolutePath();
                this.setCrop(t);
                cropCheck=true;
            }else{
                this.setCrop("");
                cropCheck= false;
           }    
        }else{
            File file =new File(s);
            if(file.exists()&&s.endsWith(java.util.ResourceBundle.getBundle("lm/properties/properties").getString("crop"))){
                String t = file.getAbsolutePath();
                this.setCrop(t);
                cropCheck=true;
            }else{
                String t = file.getAbsolutePath();
                this.setCrop(t);
                cropCheck= false;
            }
            
        }
        setChanged();
        notifyObservers(0); 
    }
    
    public void checkTillPath(String s,Boolean b){
        if(b){
           File file=new File(path +java.util.ResourceBundle.getBundle("ressources/Ressources").getString("/")  +java.util.ResourceBundle.getBundle("lm/properties/properties").getString("till"));
           if(file.exists()){
                String t = file.getAbsolutePath();
                this.setTill(t);
                tillCheck=true;
            }else{
                this.setTill("");
                tillCheck= false;
           }    
        }else{
            File file =new File(s);
            if(file.exists()&&s.endsWith(java.util.ResourceBundle.getBundle("lm/properties/properties").getString("till"))){
                String t = file.getAbsolutePath();
                this.setTill(t);
                tillCheck=true;
            }else{
                String t = file.getAbsolutePath();
                this.setTill(t);
                tillCheck= false;
            }
            
        }
        setChanged();
        notifyObservers(0); 
    }
    
    public void checkFertPath(String s,Boolean b){
        
        if(b){
           File file=new File(path +java.util.ResourceBundle.getBundle("ressources/Ressources").getString("/")  +java.util.ResourceBundle.getBundle("lm/properties/properties").getString("fert"));
           if(file.exists()){
                String t = file.getAbsolutePath();
                this.setFert(t);
                fertCheck=true;
            }else{
                this.setFert("");
                fertCheck= false;
           }    
        }else{
            File file =new File(s);
            if(file.exists()&&s.endsWith(java.util.ResourceBundle.getBundle("lm/properties/properties").getString("fert"))){
                String t = file.getAbsolutePath();
                this.setFert(t);
                fertCheck=true;
            }else{
                String t = file.getAbsolutePath();
                this.setFert(t);
                fertCheck= false;
            }
            
        }
        setChanged();
        notifyObservers(0); 
    }
    
    public void checkCropRotationPath(String s,Boolean b){
        //Check if lmArable exsist
        if(b){
           File file=new File(path +java.util.ResourceBundle.getBundle("ressources/Ressources").getString("/")  +java.util.ResourceBundle.getBundle("lm/properties/properties").getString("cropRotation"));
           if(file.exists()){
                String t = file.getAbsolutePath();
                this.setCropRotation(t);
                cropRotationCheck=true;
            }else{
                this.setCropRotation("");
                cropRotationCheck= false;
           }    
        }else{
            File file =new File(s);
            if(file.exists()&&s.endsWith(java.util.ResourceBundle.getBundle("lm/properties/properties").getString("cropRotation"))){
                String t = file.getAbsolutePath();
                this.setCropRotation(t);
                cropRotationCheck=true;
            }else{
                String t = file.getAbsolutePath();
                this.setCropRotation(t);
                cropRotationCheck= false;
            }
            
        }
        setChanged();
        notifyObservers(0);  
    }
    
    public void checkLandusePath(String s,Boolean b){
        //Check if lmArable exsist
        
       if(b){
           File file=new File(path +java.util.ResourceBundle.getBundle("ressources/Ressources").getString("/")  +java.util.ResourceBundle.getBundle("lm/properties/properties").getString("landuse"));
           if(file.exists()){
                String t = file.getAbsolutePath();
                this.setLanduse(t);
                landuseCheck=true;
            }else{
                this.setLanduse("");
                landuseCheck= false;
           }    
        }else{
            File file =new File(s);
            if(file.exists()&&s.endsWith(java.util.ResourceBundle.getBundle("lm/properties/properties").getString("landuse"))){
                String t = file.getAbsolutePath();
                this.setLanduse(t);
                landuseCheck=true;
            }else{
                String t = file.getAbsolutePath();
                this.setLanduse(t);
                landuseCheck= false;
            }
            
        }
        setChanged();
        notifyObservers(0); 
    }
    
    public void checkHru_rot_ackerPath(String s,Boolean b){
        if(b){
           File file=new File(path +java.util.ResourceBundle.getBundle("ressources/Ressources").getString("/")  +java.util.ResourceBundle.getBundle("lm/properties/properties").getString("hru_rot_acker"));
           if(file.exists()){
                String t = file.getAbsolutePath();
                this.setHru_rot_acker(t);
                hru_rot_ackerCheck=true;
            }else{
                this.setHru_rot_acker("");
                hru_rot_ackerCheck= false;
           }    
        }else{
            File file =new File(s);
            if(file.exists()&&s.endsWith(java.util.ResourceBundle.getBundle("lm/properties/properties").getString("hru_rot_acker"))){
                String t = file.getAbsolutePath();
                this.setHru_rot_acker(t);
                hru_rot_ackerCheck=true;
            }else{
                String t = file.getAbsolutePath();
                this.setHru_rot_acker(t);
                hru_rot_ackerCheck= false;
            }
            
        }
        setChanged();
        notifyObservers(0); 
    }
    
    public void checkCropFromKey(String s){
        this.setCrop(s);
        File file =new File(s);
        if(file.exists()&&s.endsWith(java.util.ResourceBundle.getBundle("lm/properties/properties").getString("crop"))){               
                cropCheck=true;
            }else{
                cropCheck= false;
            }
        setChanged();
        notifyObservers(1); 
    }
    
    public void checkFertFromKey(String s){
        this.setFert(s);
        File file =new File(s);
        if(file.exists()&&s.endsWith(java.util.ResourceBundle.getBundle("lm/properties/properties").getString("fert"))){               
                fertCheck=true;
            }else{
                fertCheck= false;
            }
        setChanged();
        notifyObservers(1); 
    }
    
    public void checkTillFromKey(String s){
        this.setTill(s);
        File file =new File(s);
        if(file.exists()&&s.endsWith(java.util.ResourceBundle.getBundle("lm/properties/properties").getString("till"))){               
                tillCheck=true;
            }else{
                tillCheck= false;
            }
        setChanged();
        notifyObservers(1); 
    }
    public void checkCropRotationFromKey(String s){
        this.setCropRotation(s);
        File file =new File(s);
        if(file.exists()&&s.endsWith(java.util.ResourceBundle.getBundle("lm/properties/properties").getString("cropRotation"))){               
                cropRotationCheck=true;
            }else{
                cropRotationCheck= false;
            }
        setChanged();
        notifyObservers(1); 
    }
    public void checkLanduseFromKey(String s){
        this.setCrop(s);
        File file =new File(s);
        if(file.exists()&&s.endsWith(java.util.ResourceBundle.getBundle("lm/properties/properties").getString("landuse"))){               
                landuseCheck=true;
            }else{
                landuseCheck= false;
            }
        setChanged();
        notifyObservers(1); 
    }
    public void checkHru_rot_ackerFromKey(String s){
        this.setCrop(s);
        File file =new File(s);
        if(file.exists()&&s.endsWith(java.util.ResourceBundle.getBundle("lm/properties/properties").getString("hru_rot_acker"))){               
                hru_rot_ackerCheck=true;
            }else{
                hru_rot_ackerCheck= false;
            }
        setChanged();
        notifyObservers(1); 
    }
    public void checkLmArableFromKey(String s){
        this.setLmArable(s);
        File file =new File(s);
        if(file.exists()&&s.endsWith(java.util.ResourceBundle.getBundle("lm/properties/properties").getString("lmArable"))){               
                lmArableCheck=true;
            }else{
                lmArableCheck= false;
            }
        setChanged();
        notifyObservers(1); 
    }
        
    
    

    public void Check(){
        checkLmArablePath(path,true);
        checkFertPath(path,true);
        checkTillPath(path,true);
        checkCropPath(path,true);
        checkCropRotationPath(path,true);
        checkLandusePath(path,true);
        checkHru_rot_ackerPath(path,true);
        
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
        return path;
    }
    public void setPath(String Path){
        this.path=Path;
    }
    
    /**
     * @return the landuse
     */
    public String getLanduse() {
        return landuse;
    }

    /**
     * @param landuse the landuse to set
     */
    public void setLanduse(String landuse) {
        this.landuse = landuse;
    }

    /**
     * @return the Hru_rot_acker
     */
    public String getHru_rot_acker() {
        return hru_rot_acker;
    }

    /**
     * @param Hru_rot_acker the Hru_rot_acker to set
     */
    public void setHru_rot_acker(String Hru_rot_acker) {
        this.hru_rot_acker = Hru_rot_acker;
    }

    public String[] getAllPaths(){
        String[] s =new String[7];
        s[0]=lmArable;
        s[1]=crop;
        s[2]=fert;
        s[3]=till;
        s[4]=cropRotation;
        s[5]=landuse;
        s[6]=hru_rot_acker;
        return s;
    }

    public void setAllPaths(String[] paths){

    }
    public Boolean checkIfAllRight(){
        return  lmArableCheck&&tillCheck&&fertCheck&&cropCheck&&cropRotationCheck&&landuseCheck&&hru_rot_ackerCheck;
    }

    /**
     * @return the lmArableHead
     */
    public String getLmArableHead() {
        return lmArableHead;
    }

    /**
     * @param lmArableHead the lmArableHead to set
     */
    public void setLmArableHead(String lmArableHead) {
        this.lmArableHead = lmArableHead;
    }

    /**
     * @return the tillHead
     */
    public String getTillHead() {
        return tillHead;
    }

    /**
     * @param tillHead the tillHead to set
     */
    public void setTillHead(String tillHead) {
        this.tillHead = tillHead;
    }

    /**
     * @return the fertHead
     */
    public String getFertHead() {
        return fertHead;
    }

    /**
     * @param fertHead the fertHead to set
     */
    public void setFertHead(String fertHead) {
        this.fertHead = fertHead;
    }

    /**
     * @return the cropHead
     */
    public String getCropHead() {
        return cropHead;
    }

    /**
     * @param cropHead the cropHead to set
     */
    public void setCropHead(String cropHead) {
        this.cropHead = cropHead;
    }

    /**
     * @return the cropRotationHead
     */
    public String getCropRotationHead() {
        return cropRotationHead;
    }

    /**
     * @param cropRotationHead the cropRotationHead to set
     */
    public void setCropRotationHead(String cropRotationHead) {
        this.cropRotationHead = cropRotationHead;
    }

    /**
     * @return the landuseHead
     */
    public String getLanduseHead() {
        return landuseHead;
    }

    /**
     * @param landuseHead the landuseHead to set
     */
    public void setLanduseHead(String landuseHead) {
        this.landuseHead = landuseHead;
    }

    /**
     * @return the hru_rot_ackerHead
     */
    public String getHru_rot_ackerHead() {
        return hru_rot_ackerHead;
    }

    /**
     * @param hru_rot_ackerHead the hru_rot_ackerHead to set
     */
    public void setHru_rot_ackerHead(String hru_rot_ackerHead) {
        this.hru_rot_ackerHead = hru_rot_ackerHead;
    }

    public Boolean getLmArableCheck() {
        return lmArableCheck;
    }

    public Boolean getCropCheck() {
        return cropCheck;
    }

    public Boolean getTillCheck() {
        return tillCheck;
    }

    public Boolean getFertCheck() {
        return fertCheck;
    }

    public Boolean getCropRotationCheck() {
        return cropRotationCheck;
    }

    public Boolean getLanduseCheck() {
        return landuseCheck;
    }

    public Boolean getHru_rot_ackerCheck() {
        return hru_rot_ackerCheck;
    }

    

}
