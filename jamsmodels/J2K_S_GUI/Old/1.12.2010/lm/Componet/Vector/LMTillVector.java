/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.Componet.Vector;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public interface LMTillVector {

    public LMTillVector getVector();

    public Boolean CeckIfCorrect();

    public Boolean isEmpty();

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
