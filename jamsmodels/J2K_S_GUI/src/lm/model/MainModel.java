/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.model;

import java.util.Properties;
import lm.Componet.*;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class MainModel  {

    private Config_data config;
    private MultiModel multireader;
    private SaveModel savemodel;

    public MainModel(){
    config =new Config_data();
    multireader= new MultiModel();
    
    }
    public void SetPaths(String Path){
       config.Check(Path);
    }

    public void createSaveModel(){
        this.savemodel=new SaveModel(multireader);
    }

    //GetterMethoden
    public Config_data getLMConfig(){
        return config;
    }

    public MultiModel getMultiReader(){
        return multireader;
    }
    public SaveModel getSaveModel(){
        return savemodel;
    }

}
