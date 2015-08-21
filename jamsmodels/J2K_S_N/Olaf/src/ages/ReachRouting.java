/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ages;

import ages.types.Reach;
import ages.types.HRU;
import java.util.List;
import oms3.ComponentAccess;
import oms3.Compound;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Initialize;
import oms3.annotations.Out;
import oms3.util.Threads;
import oms3.util.Threads.CompList;
import routing.J2KProcessReachRoutingN;

/**
 *
 * @author od
 */
public class ReachRouting {

    @In @Out public List<Reach> reaches;
    @In @Out public List<HRU> hrus; // only to sync
    
    @In public double basin_area;
    
    @Out public double channelStorage_w;
    @Out public double catchmentRD1_w;
    @Out public double catchmentRD2_w;
    @Out public double catchmentRG1_w;
    @Out public double catchmentRG2_w;
    @Out public double channelStorage;
    @Out public double catchmentRD1;
    @Out public double catchmentRD2;
    @Out public double catchmentRG1;
    @Out public double catchmentRG2;

    @Out public double catchmentSimRunoff;
    @Out public double catchmentSimRunoffN;
    @Out public double DeepsinkN;
    @Out public double DeepsinkW;
    @Out public double catchmentNRD1;
    @Out public double catchmentNRD2;
    @Out public double catchmentNRG1;
    @Out public double catchmentNRG2;
    @Out public double catchmentNRD1_w;
    @Out public double catchmentNRD2_w;
    @Out public double catchmentNRG1_w;
    @Out public double catchmentNRG2_w;

    SN model;
    
    CompList<Reach> list;
    Reach outlet;

    ReachRouting(SN model) {
        this.model = model;
    }

    @Execute
    public void execute() throws Exception {
        if (list == null) {
             System.out.println("Creating reaches ...");
            list = new CompList<Reach>(reaches) {

                @Override
                public Compound create(Reach reach) {
                    return new Processes(reach);
                }
            };

            System.out.println("Init reaches ...");
            for (Compound c : list) {
                ComponentAccess.callAnnotated(c, Initialize.class, true);
            }

            System.out.println("Find outlet reaches ...");
            for (Reach reach : reaches) {
                if (reach.to_reach == null) {
                    outlet = reach;
                    break;
                }
            }
            if (outlet == null) {
                throw new RuntimeException("no reach outlet found");
            }
        }

//        for (Reach r : reaches) {
//            System.out.println("Reach " + r.inRD1 + " " + r.inRD2 + " " + r.inRG1 + " " + r.inRG2 + " " + r.inAddIn);
//        }
        
//        System.out.print("Routing ... ");
//        System.out.flush();
        Threads.seq_e(list);
//        System.out.println(" done");
//        long now2 = System.currentTimeMillis();
//        System.out.println("   Reach Routing :  " + (now2 - now1));

        // aggregate channel storage
        channelStorage = 0;
        for (Reach reach : reaches) {
            channelStorage += reach.channelStorage;
            DeepsinkN += reach.DeepsinkN;
            DeepsinkW += reach.DeepsinkW;
        }

        // catchment weighting
        catchmentRD1 = outlet.outRD1;
        catchmentRD2 = outlet.outRD2;
        catchmentRG1 = outlet.outRG1;
        catchmentRG2 = outlet.outRG2;
        catchmentNRD1 = outlet.outRD1_N;
        catchmentNRD2 = outlet.outRD2_N;
        catchmentNRG1 = outlet.outRG1_N;
        catchmentNRG2 = outlet.outRG2_N;
        
        catchmentNRD1_w = catchmentNRD1 / basin_area;
        catchmentNRD2_w = catchmentNRD2 / basin_area;
        catchmentNRG1_w = catchmentNRG1 / basin_area;
        catchmentNRG2_w = catchmentNRG2 / basin_area;
        catchmentRD1_w = catchmentRD1 / basin_area;
        catchmentRD2_w = catchmentRD2 / basin_area;
        catchmentRG1_w = catchmentRG1 / basin_area;
        catchmentRG2_w = catchmentRG2 / basin_area;
        channelStorage_w = channelStorage / basin_area;

        
        // convert l/day -> qm/s
        catchmentSimRunoff = outlet.simRunoff / (double)(1000*86400);
        catchmentSimRunoffN = outlet.simRunoff_N;

//        if (catchmentSimRunoff != 0) {
//            System.out.println("total: " + catchmentSimRunoff + "/" + catchmentRD1 + "/" + catchmentRD2 + "/" + catchmentRG1 + "/" + catchmentRG2);
//        }
    }

    public class Processes extends Compound {

        public Reach reach;

        J2KProcessReachRoutingN routing = new J2KProcessReachRoutingN();

        Processes(Reach reach) {
            this.reach = reach;
        }

        @Initialize
        public void initialize() {
            // array selection
            field2in(this, "reach", routing);
            field2in(model, "flowRouteTA", routing);
            field2in(model, "Ksink", routing);
            
            initializeComponents();
        }
    }
}
