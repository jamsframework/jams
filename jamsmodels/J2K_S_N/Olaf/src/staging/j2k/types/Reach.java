/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package staging.j2k.types;

/**
 *
 * @author od
 */
public class Reach {

    public int ID;
    public double length;
    public int to_reachID;
    public double slope;
    public double rough;
    public double width;
    
    public Reach to_reach;

    public double inRD1;
    public double inRD2;
    public double inRG1;
    public double inRG2;

    public double outRD1;
    public double outRD2;
    public double outRG1;
    public double outRG2;

    public double actRD1;
    public double actRD2;
    public double actRG1;
    public double actRG2;

    public double inAddIn;
    public double outAddIn;
    public double actAddIn;

    public double channelStorage;
    public double simRunoff;


    @Override
    public String toString() {
        return "Reach[id=" + ID + "]";
    }
}
