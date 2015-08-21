/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lm.model;

import java.util.ArrayList;
import java.util.Observable;
import java.util.TreeMap;
import javax.swing.DefaultListModel;
import lm.Componet.LMDefaultModel;
import lm.Componet.LMTillModel;
import lm.Componet.Vector.LMTillVector;
import lm.model.DefaultVector.TillVector;

/**
 *
 * @author MultiMedia
 */
public class TillModel extends Observable implements LMTillModel, Cloneable {
    
    private TreeMap<Integer,LMTillVector>lMTill= new TreeMap();
    private DefaultListModel tIDListModel = new DefaultListModel();
    
    public void createTillModel(TreeMap<Integer,LMTillVector> t){
        lMTill =t;
        createTIDListModel();
    }
    
    public TillModel clone(){
        TillModel b = new TillModel();
        if(!lMTill.isEmpty()){
            Integer key = lMTill.firstKey();
            while(key!=null){
                b.lMTill.put(key,lMTill.get(key));
                key=lMTill.higherKey(key);
            }
        }
        b.createTIDListModel();
        return b;
    }
    
    public void createTIDListModel(){
        tIDListModel.clear();
        tIDListModel.addElement("-");
        if(!lMTill.isEmpty()){
            Integer i =lMTill.firstKey();
            while(i!=null){
                tIDListModel.addElement(i + "\n<" + lMTill.get(i).gettillnm() + ">" );
                i=lMTill.higherKey(i);
            }
        }
    }
    
    public void AddTillVector(){
       lMTill.put(lMTill.lastKey()+1,new TillVector());
        lMTill.get(lMTill.lastKey()).setTID(lMTill.lastKey());
        tIDListModel.addElement(lMTill.lastKey()+"\t<>");
    }
    public void DeleteTillVector(Integer key, int i){
        tIDListModel.removeElementAt(i);
        lMTill.remove(key);
    }
    public void setTillVector(Integer index,ArrayList a){
            lMTill.get(index).setAll(a);
    }
    public LMTillVector getTillVector(int ID){
        return lMTill.get(ID);
    }

    public LMTillModel getDefaultModel() {
        return this;
    }

    public DefaultListModel getTIDListModel() {
        return tIDListModel;
    }

    public Boolean isAktiv() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
