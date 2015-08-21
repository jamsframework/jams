/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.Componet.Vector;

import java.util.ArrayList;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public interface LMFertVector {

    public LMFertVector getVector();

    public boolean isEmpty();

    public Boolean CeckIfCorrect();

    //Setter And Getter  ---->ID
    public void setID(int i);
    public int getID();
    //Setter And Getter  ---->fertnm
    public void setfertnm(String s);
    public String getfertnm();
    //Setter And Getter  ---->fminn
    public void setfminn(double i);
    public double getfminn();
    //Setter And Getter  ---->fminp
    public void setfminp(double i);
    public double getfminp();
    //Setter And Getter  ---->forgn
    public void setforgn(double i);
    public double getforgn();
    //Setter And Getter  ---->forgp
    public void setforgp(double i);
    public double getforgp();
    //Setter And Getter  ---->fnh3n
    public void setfnh3n(double i);
    public double getfnh3n();
    //Setter And Getter  ---->setbactpdb
    public void setbactpdb(double i);
    public double getbactpdb();
    //Setter And Getter  ---->setbactldb
    public void setbactldb(double i);
    public double getbactldb();
    //Setter And Getter  ---->setbactddb
    public void setbactddb(double i);
    public double getbactddb();
    //Setter And Getter  ---->desc
    public void setdesc(String s);
    public String getdesc();

}