package lm.Componet.Vector;

import java.util.ArrayList;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public interface LMTillVector {

    public void setAll(ArrayList<String> a);

    public LMTillVector getVector();

    public Boolean CeckIfCorrect();

    public Boolean isEmpty();

    public void print();
    //Getter And Setter Methods
    //Getter And Setter ------->TID
    public void setTID(int i);
    public int getTID();
    //Getter And Setter ------->tillnm
    public void settillnm(String s);
    public String gettillnm();
    //Getter And Setter ------->desc
    public void setdesc(String s);
    public String getdesc();
    //Getter And Setter ------->effmix
    public void seteffmix(Double d);
    public Double geteffmix();
    //Getter And Setter ------->deptil
    public void setdeptil(Double d);
    public Double getdeptil();

}