package lm.model.DefaultVector;

import java.util.ArrayList;
import lm.Componet.Vector.LMArableID;
/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class ArableID implements LMArableID,Comparable<ArableID> {

    private Integer ID;
    private Integer Stufe;
    private int maxStufe;


    public ArableID(){
        this.ID=0;
        this.Stufe=0;
        this.maxStufe=0;
    }

    public ArableID(int ID , int Stufe){
        this.ID=ID;
        this.Stufe=Stufe;
    }

    public ArableID(ArrayList<String> a){
        this.ID=Integer.parseInt(a.get(0));
        this.Stufe=Integer.parseInt(a.get(1));
        this.maxStufe=Integer.parseInt(a.get(2));
    }
    public ArableID(int ID , int Stufe , int maxStufe){
        this.ID=ID;
        this.Stufe=Stufe;
        this.maxStufe=maxStufe;
    }

    public Boolean isEmpty() {
        return this.ID==0;
        }

    public Boolean isLast() {
        return this.Stufe==this.maxStufe;
    }
    public Boolean CeckIfCorrect() {
        throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("NOT SUPPORTED YET."));
    }

    //Getter And Setter MEthods
    //Getter And Setter ------>ID
    public void setID(int i) {
        this.ID=i;
    }
    public int getID() {
        return this.ID;
    }
    //Getter And Setter ------>Stufe
    public void setStufe(int i) {
        this.Stufe=i;
    }
    public int getStufe() {
        return this.Stufe;
    }
    //Getter And Setter ------>MaxStufe
    public void setMaxStufe(int i) {
        this.maxStufe=i;
    }
    public int getMaxStufe() {
        return this.maxStufe;
    }

    public String toString() {
        return (ID+"/"+Stufe);
    }

    public int compareTo(ArableID t) {
        if(ID.compareTo(t.ID) == 0){
            return Stufe.compareTo(t.getStufe());
        } else {
            return ID.compareTo(t.getID());
        }

    }
}
