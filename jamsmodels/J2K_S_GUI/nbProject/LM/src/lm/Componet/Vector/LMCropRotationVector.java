package lm.Componet.Vector;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public interface LMCropRotationVector {

    public void setElementAsFallow(Integer id);
    public boolean isEmpty();

    public void addElement(LMArableID lmArableID);
    public void deleteElement(Integer i);

    public void addVector(TreeMap<Integer,LMCropRotationElement> cre,int multiplikator );
    public void insertElementAtPosition(Integer position, LMArableID aid);

    public void insertElementLeftFromPosition(Integer position,LMArableID aid);

    public void insertElementRightFromPosition(Integer position,LMArableID aid);

    public void maximizeFallow(Integer position, Integer years);

    public void reduceFallow(int element);

    public LMCropRotationElement getLMCropRotationElement(Integer i);

    public String getRowForSave();

    public LMCropRotationVector clone(TreeMap<Integer,LMArableID> aidTree);


    
    //Setter And Getter Methods
    //Setter And Getter  ---->CID
    public void setCRE(TreeMap<Integer,LMCropRotationElement> a);
    public TreeMap<Integer,LMCropRotationElement> getCRE();

    public void setAID(ArrayList a);
    public ArrayList getAID();

    public void setUseDays(int i);
    public int getUseDays();

    public void setFallowDays(int i);
    public int getFallowDays();

    public void setArableUses(TreeMap<Integer,Integer> i);
    public TreeMap<Integer,Integer> getArableUses();

    public void setLastDay(int i);
    public int getLastDay();

}
