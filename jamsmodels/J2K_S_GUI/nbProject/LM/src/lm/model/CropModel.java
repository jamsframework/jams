/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lm.model;

import java.util.ArrayList;
import java.util.Observable;
import java.util.TreeMap;
import javax.swing.DefaultListModel;
import lm.Componet.LMCropModel;
import lm.Componet.LMDefaultModel;
import lm.Componet.Vector.LMCropVector;
import lm.model.DefaultVector.CropVector;

/**
 *
 * @author MultiMedia
 */
public class CropModel extends Observable implements Cloneable, LMCropModel {
    
    private TreeMap<Integer,LMCropVector> lMCrop =new TreeMap();
    private DefaultListModel cIDListModel=new DefaultListModel();
    
    public void createCropModel(TreeMap<Integer,LMCropVector> t){
        lMCrop =t;
        createCIDListModel();
    }

    public CropModel clone (){
        CropModel b = new CropModel();
        if(!lMCrop.isEmpty()){
            Integer key=lMCrop.firstKey();
            while(key!=null){
                b.lMCrop.put(key, lMCrop.get(key).clone());
                key=lMCrop.higherKey(key);
            }
        }
        b.createCIDListModel();
        return b;
    }
    
    public void createCIDListModel(){
        cIDListModel.clear();
        cIDListModel.addElement("-");
        if(!lMCrop.isEmpty()){
            Integer i =lMCrop.firstKey();
            while(i!=null){
                cIDListModel.addElement(i+"\t<"+lMCrop.get(i).getCpnm()+">");
                i=lMCrop.higherKey(i);
            }
        }
    }
    
    public void AddCropVector(){
       lMCrop.put(lMCrop.lastKey()+1,new CropVector());
        lMCrop.get(lMCrop.lastKey()).setID(lMCrop.lastKey());
        cIDListModel.addElement(lMCrop.lastKey()+"\t<>");
    }
    public void DeleteCropVector(Integer key, int i){
        cIDListModel.removeElementAt(i);
        lMCrop.remove(key);
    }
    public void setCropVector(Integer index,ArrayList a){
            lMCrop.get(index).setAll(a);
    }
    public LMCropVector getCropVector(int ID){
        return lMCrop.get(ID);
    }

    public LMCropModel getLMCropModel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Boolean isAktiv() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public DefaultListModel getCIDListModel(){
        return cIDListModel;
    }
    
}
