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

    public String getRowForSave(int ID);

    public LMArableVector clone();

    public void print();

    //Getter and Setter Methods
    //Getter and Setter -----> CID
    public void setCID(Integer key);
    public Integer getCID();
    //Getter and Setter ----->YEAR
    public void setYear(Integer year);
    public Integer getYear();
    //Getter and Setter ----->Date
    public void setDate(Integer s);
    public Integer getDate();
    //Getter and Setter ----->TID
    public void setTID(Integer key);
    public Integer getTID();
    //Getter and Setter ----->FID
    public void setFID(Integer Vector);
    public Integer getFID();
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
