/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.model;

import java.util.Properties;
import java.util.TreeMap;
import lm.Componet.*;
import lm.Componet.Vector.LMCropRotationVector;
import lm.Componet.Vector.LMCropVector;
import lm.Componet.Vector.LMFertVector;
import lm.Componet.Vector.LMTillVector;
import lm.model.DefaultVector.ArableID;

/**
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class MainModel  {

    private Config_data config;
    private MultiModel multireader;
    private MultiModel backup;
    private SaveModel saveModel;
    private CRSaveModel crSaveModel;
    private CropModel cropModel;
    private CropModel backupCropModel;
    private TillModel tillModel;
    private TillModel backupTillModel;
    private FertModel fertModel;
    private FertModel backupFertModel;
    private Save save;
    private Boolean activ;

    public MainModel(){
    activ=false;    
    }
    
    public void startNewConfig(){
        config =new Config_data();
    }
    
    public void startNewModel(){
        multireader= new MultiModel();
        activ=true;
        
    }
    
    public void save(){
        save=new Save();
        save.setDefaultModel(multireader, config);
        save.SaveThis(config.getPath());
    }

    public void save(String path){
        save=new Save();
        save.setDefaultModel(multireader, config);
        save.SaveThis(path+"/parameter");
    }

    public void saveSaveModel() throws CloneNotSupportedException{
        multireader=(MultiModel)saveModel.getMultiModel().clone();
    }
    public void saveCRSaveModel() throws CloneNotSupportedException{
        multireader=(MultiModel)crSaveModel.getDefaultModel().clone();
    }
    public void cancelSaveModel() throws CloneNotSupportedException{
        multireader=(MultiModel)backup.clone();
    }
    public void cancelCRSaveModel() throws CloneNotSupportedException{
        multireader=(MultiModel)backup.clone();
    }
    
    public void createCropModel() throws CloneNotSupportedException{
//        backupCropModel=(CropModel)cropModel.clone();
        cropModel=new CropModel();
        cropModel.createCropModel(multireader.getLMCrop());
    }
    
    public void createTillModel() throws CloneNotSupportedException{
//        backupTillModel=(TillModel)tillModel.clone();
        tillModel=new TillModel();
        tillModel.createTillModel(multireader.getLMTill());
    }
//    
//    public void createFertModel() throws CloneNotSupportedException{
//        backupFertModel=(FertModel)fertModel.clone();
//    }

    public void createSaveModel() throws CloneNotSupportedException{
        this.backup=(MultiModel)multireader.clone();
        
        this.saveModel=new SaveModel();
        this.saveModel.createSaveModel((MultiModel)multireader.clone());

        
    }
    public void createCRSaveModel() throws CloneNotSupportedException{
        this.backup=(MultiModel)multireader.clone();
        this.crSaveModel=new CRSaveModel((MultiModel)multireader.clone());
    }
    public void createCRFromSaveModel(){
        this.crSaveModel=new CRSaveModel(saveModel.getMultiModel());
    }

    public Boolean isActiv(){
        return activ;
    }
    public void setActiv(){
        activ=true;
    }

    //GetterMethoden
    public Config_data getLMConfig(){
        return config;
    }

    public MultiModel getMultiReader(){
        return multireader;
    }
    public SaveModel getSaveModel(){
        return saveModel;
    }
    public CRSaveModel getCRSaveModel(){
        return this.crSaveModel;
    }

    public MultiModel getBackup(){
        return backup;
    }
    
    public CropModel getCropModel(){
        return cropModel;
    }
    
    public TillModel getTillModel(){
        return tillModel;
    }
    
    public FertModel getFertModel(){
        return fertModel;
    }

}
