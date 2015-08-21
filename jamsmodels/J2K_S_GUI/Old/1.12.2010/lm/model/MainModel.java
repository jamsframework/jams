/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.model;

import lm.Componet.*;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class MainModel  {

    private BufferedFileReader reader;
    private Config_data config;
    private MultiReader multireader;

    public MainModel(){
    config =new Config_data();
    reader =new BufferedFileReader();
    multireader= new MultiReader();
    
    }
    public void SetPaths(String Path){
       config.Check(Path);
    }

    //GetterMethoden
    public Config_data getLMConfig(){
        return config;
    }
    public BufferedFileReader getBufferedFileReader(){
        return reader;
    }
    public MultiReader getMultiReader(){
        return multireader;
    }

}
