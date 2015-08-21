/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.Componet.Vector;

/**
 *
 * @author Jens Wipprich ==> jen
 * jens.wipprich (at) uni-jena.de
 */
public interface LMArableVector {


    public LMArableVector getVector();

    public Boolean CeckIfCorrect();

    public Boolean isEmpty();

    //Getter and Setter Methods
    //Getter and Setter -----> CID
    public void setCID(LMFertVector Vector);  
    public LMFertVector getCID();
    //Getter and Setter ----->Date
    public void setDate(String s);
    public String getDate();
    //Getter and Setter ----->TID
    public void setTID(LMTillVector Vector);
    public LMTillVector getTID();
    //Getter and Setter ----->FID
    public void setFID(LMFertVector Vector);
    public LMFertVector getFID();
    //Getter and Setter ----->FAmount
    public void setFAmount(Double d);
    public Double getFAmount();
    //Getter and Setter ----->PLANT
    public void setPLANT(Boolean b);
    public Boolean getPLANT();
    //Getter and Setter ----->HARVEST
    public void setHARVEST(Boolean b);
    public Boolean getHARVEST();
    //Getter and Setter ----->FRACHARV
    public void setFRACHARV(Double d);
    public Double getFRACHARV();
}
