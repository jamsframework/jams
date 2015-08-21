package lm.Componet.Vector;

import java.awt.Color;
import java.util.TreeMap;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public interface LMArableID {

    public String toString();

    public void AddVector(LMArableVector lmArableVector);
    public void AddVector(Integer i);
    public void ChangeAYearAndCheckTheRest(int vector,int year);
    public void DeleteVector(Integer i);

    public LMArableID  clone();

    public Boolean isEmpty();

    public Boolean isFallow();
    
    public void setADate();
    

    //Getter And Setter MEthods
    public void setID(Integer ID);
    public Integer getID();


    //Getter And Setter ------>Color
    public void setColor(Color color);
    public Color getColor();

    //Getter And Setter ------>Duration
    public void setDuration(int duration);
    public int getDuration();

    //Getter And Setter ------>ArableList
    public void setArableList(TreeMap treeMap);
    public TreeMap<Integer,LMArableVector> getTreeMap();

    public void setCropID(int i);
    public int getCropID();

    public void setRelativeBegin(int i);
    public int getRelativeBegin();

    public void setRelativeEnd(int i);
    public int getRelativeEnd();

    public void setIsFallow(Boolean b);
    
    public void set_Nitrogen(double i);
    public double get_Nitrogen();
    
    public void set_Phosphorus(double i);
    public double get_Phosphorus();
    
    public void set_Example3(int i);
    public int get_Example3();
    
    public void set_Example4(int i);
    public int get_Example4();
    
}
