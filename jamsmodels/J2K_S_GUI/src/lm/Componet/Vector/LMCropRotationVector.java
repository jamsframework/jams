package lm.Componet.Vector;

import java.util.ArrayList;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public interface LMCropRotationVector {

    public boolean isEmpty();

    public Boolean CeckIfCorrect();

    //Setter And Getter Methods
    //Setter And Getter  ---->ID
    public void setID(int i);
    public int getID();
    //Setter And Getter  ---->CID
    public void setCID(ArrayList a);
    public ArrayList getCID();

}
