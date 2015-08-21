package lm.model.DefaultVector;

import java.util.ArrayList;
import lm.Componet.Vector.LMCropRotationVector;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class CropRotationVector implements LMCropRotationVector{

    private int ID;
    private ArrayList<Integer> CID;

    public CropRotationVector(){
        ID=0;
        CID=null;
    }
    public CropRotationVector(int i,ArrayList a){
        ID=i;
        CID=a;

    }


    public boolean isEmpty() {
        throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("NOT SUPPORTED YET."));
    }

    public Boolean CeckIfCorrect() {
        throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("NOT SUPPORTED YET."));
    }

    public void setID(int i) {
        throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("NOT SUPPORTED YET."));
    }

    public int getID() {
        throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("NOT SUPPORTED YET."));
    }

    public void setCID(ArrayList a) {
        throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("NOT SUPPORTED YET."));
    }

    public ArrayList getCID() {
        throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("NOT SUPPORTED YET."));
    }

}
