package lm.Componet.Vector;

import java.util.ArrayList;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public interface LMFertVector {


    public boolean isEmpty();

    public Boolean CeckIfCorrect();

    public void setAll(ArrayList<String> toVector);

    public ArrayList<String> getAll();

    public String getRowForSave();

    public LMFertVector clone();

    //Setter And Getter Methods
    //Setter And Getter  ---->ID
    public void setID(int i);
    public int getID();
    //Setter And Getter  ---->fertnm
    public void setfertnm(String s);
    public String getfertnm();
    //Setter And Getter  ---->fminn
    public void setfminn(Double i);
    public Double getfminn();
    //Setter And Getter  ---->fminp
    public void setfminp(Double i);
    public Double getfminp();
    //Setter And Getter  ---->forgn
    public void setforgn(Double i);
    public Double getforgn();
    //Setter And Getter  ---->forgp
    public void setforgp(Double i);
    public Double getforgp();
    //Setter And Getter  ---->fnh3n
    public void setfnh3n(Double i);
    public Double getfnh3n();
    //Setter And Getter  ---->setbactpdb
    public void setbactpdb(Double i);
    public Double getbactpdb();
    //Setter And Getter  ---->setbactldb
    public void setbactldb(Double i);
    public Double getbactldb();
    //Setter And Getter  ---->setbactddb
    public void setbactddb(Double i);
    public Double getbactddb();
    //Setter And Getter  ---->desc
    public void setdesc(String s);
    public String getdesc();

}