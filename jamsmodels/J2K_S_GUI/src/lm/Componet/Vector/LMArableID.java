package lm.Componet.Vector;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public interface LMArableID {

    public Boolean isEmpty();

    public Boolean isLast();

    public Boolean CeckIfCorrect();

    public String toString();

    //Getter And Setter MEthods
    //Getter And Setter ------>ID
    public void setID(int i);
    public int getID();
    //Getter And Setter ------>Stufe
    public void setStufe(int i);
    public int getStufe();
    //Getter And Setter ------>maxStufe
    public void setMaxStufe(int i);
    public int getMaxStufe();

}
