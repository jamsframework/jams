/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;
import lm.Componet.Vector.*;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class Save {
    
    private MultiModel defaultModel;
    private Config_data config;

    public Save(){

    }

    public void SaveThis(String place){


        File crop =new File(place +"/crop.par");
        File till =new File(place +"/till.par");
        File fert =new File(place +"/fert.par");
        File lmArable =new File(place +"/lmArable.par");
        File cropRotation =new File(place +"/croprotation.par");
        File landuse =new File (place +"/landuse.par");
        File hru_rot_acker =new File (place +"/hru_rot_acker.par");
        File placeFile=new File(place);
        try{
        if(!placeFile.exists()){
            placeFile.mkdir();
        }
        if(!crop.exists()){
            crop.createNewFile();
        }
        if(!till.exists()){
            till.createNewFile();
        }
        if(!fert.exists()){
            fert.createNewFile();
        }
        if(!lmArable.exists()){
            lmArable.createNewFile();
        }
        if(!cropRotation.exists()){
            cropRotation.createNewFile();
        }
        if(!landuse.exists()){
            landuse.createNewFile();
        }
        if(!hru_rot_acker.exists()){
            hru_rot_acker.createNewFile();
        }
        this.saveCropRotation(cropRotation);
        this.saveCrop(crop);
        this.saveFert(fert);
        this.saveTill(till);
        this.savelmArable(lmArable);

        }catch(IOException ioe){

        }

    }

    private void saveCrop(File f) throws IOException{
        BufferedWriter userfile = new BufferedWriter(new FileWriter(f));
        String data="";
        userfile.write(data);
        userfile.write(config.getCropHead()+System.lineSeparator());
        TreeMap<Integer,LMCropVector> treeMap=defaultModel.getLMCrop();
        if(!treeMap.isEmpty()){
            Integer key=treeMap.firstKey();
            while(key!=null){
                userfile.write(treeMap.get(key).getRowForSave()+System.lineSeparator());
                key=treeMap.higherKey(key);
            }
        }
        userfile.write("# End of File crop.par!");
        userfile.close();
    }

    private void saveTill(File f) throws IOException{
        BufferedWriter userfile = new BufferedWriter(new FileWriter(f));
        String data="";
        userfile.write(data);
        userfile.write(config.getTillHead()+System.lineSeparator());
        TreeMap<Integer,LMTillVector> treeMap=defaultModel.getLMTill();
        if(!treeMap.isEmpty()){
            Integer key=treeMap.firstKey();
            while(key!=null){
                userfile.write(treeMap.get(key).getRowForSave()+System.lineSeparator());
                key=treeMap.higherKey(key);
            }
        }
        userfile.write("# End of File till.par!");
        userfile.close();
    }

    private void saveFert(File f) throws IOException{
        BufferedWriter userfile = new BufferedWriter(new FileWriter(f));
        String data="";
        userfile.write(data);
        userfile.write(config.getFertHead()+System.lineSeparator());
        TreeMap<Integer,LMFertVector> treeMap=defaultModel.getLMFert();
        if(!treeMap.isEmpty()){
            Integer key=treeMap.firstKey();
            while(key!=null){
                userfile.write(treeMap.get(key).getRowForSave()+System.lineSeparator());
                key=treeMap.higherKey(key);
            }
        }
        userfile.write("# End of File fert.par!");
        userfile.close();
    }

    private void savelmArable(File f) throws IOException{
        BufferedWriter userfile = new BufferedWriter(new FileWriter(f));
        String data="";
        userfile.write(data);
        userfile.write(config.getLmArableHead()+System.lineSeparator());
        TreeMap<Integer,LMArableID> treeMapID=defaultModel.getLMArable();
        if(!treeMapID.isEmpty()){
            Integer key=treeMapID.firstKey();
            while(key!=null){
                TreeMap<Integer,LMArableVector>treeMapVector=treeMapID.get(key).getTreeMap();
                if(!treeMapVector.isEmpty()){
                    Integer key2=treeMapVector.firstKey();
                    while(key2!=null){
                        userfile.write(treeMapVector.get(key2).getRowForSave(key)+System.lineSeparator());
                        key2=treeMapVector.higherKey(key2);
                    }
                }
                key=treeMapID.higherKey(key);
            }
        }
        userfile.write("# End of File lmArable.par!");
        userfile.close();
    }

    private void saveCropRotation(File f) throws IOException{
        BufferedWriter userfile = new BufferedWriter(new FileWriter(f));
        String data="";
        userfile.write(data);
        userfile.write(config.getCropRotationHead()+System.lineSeparator());
        TreeMap<Integer,LMCropRotationVector> treeMap=defaultModel.getLMCropRotation();
        if(!treeMap.isEmpty()){
            Integer key=treeMap.firstKey();
            while(key!=null){
                userfile.write(treeMap.get(key).getRowForSave()+System.lineSeparator());
                key=treeMap.higherKey(key);
            }

        }
        userfile.write("# End of File croprotation.par!");
        userfile.close();

        }
    
    private void saveLanduse(File f)throws IOException{
        BufferedWriter userfile = new BufferedWriter(new FileWriter(f));
        String data="";
        userfile.write(data);
        userfile.write(config.getLanduseHead()+System.lineSeparator());
        TreeMap<Integer,LMLanduseVector> treeMap=defaultModel.getLMLanduse();
        if(!treeMap.isEmpty()){
            Integer key=treeMap.firstKey();
            while(key!=null){
                userfile.write(treeMap.get(key).getRowForSave()+System.lineSeparator());
                key=treeMap.higherKey(key);
            }

        }
        userfile.write("# End of File landuse.par!");
        userfile.close();

    }
    
    private void saveHru_rot_acker(File f)throws IOException{
        BufferedWriter userfile = new BufferedWriter(new FileWriter(f));
        String data="";
        userfile.write(data);
        userfile.write(config.getHru_rot_ackerHead()+System.lineSeparator());
        TreeMap<Integer,LMHru_rot_ackerVector> treeMap=defaultModel.getLMHru_rot_acker();
        if(!treeMap.isEmpty()){
            Integer key=treeMap.firstKey();
            while(key!=null){
                userfile.write(treeMap.get(key).getRowForSave()+System.lineSeparator());
                key=treeMap.higherKey(key);
            }

        }
        userfile.write("# End of File hru_rot_acker.par!");
        userfile.close();
    }
    
    public void setDefaultModel(MultiModel mm, Config_data c){
        defaultModel=mm;
        config=c;
    }
}
