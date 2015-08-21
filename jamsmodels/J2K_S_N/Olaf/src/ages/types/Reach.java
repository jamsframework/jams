/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ages.types;

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
    public double deepsink;
    
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

    public double SurfaceN_in;
    public double InterflowN_sum;
    public double N_RG1_in;
    public double N_RG2_in;
    public double N_RG1_out;
    public double N_RG2_out;
    
    public double actRD1_N;
    public double actRD2_N;
    public double actRG1_N;
    public double actRG2_N;
    public double DeepsinkW;
    public double DeepsinkN;
    public double simRunoff_N;
    public double channelStorage_N;
    public double SurfaceNabs;
    public double InterflowNabs;

    public double outRD1_N;
    public double outRD2_N;
    public double outRG1_N;
    public double outRG2_N;


    @Override
    public String toString() {
        return "Reach[id=" + ID + "]";
    }
}
