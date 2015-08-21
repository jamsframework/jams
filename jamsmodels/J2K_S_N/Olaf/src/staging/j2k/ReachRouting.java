/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package staging.j2k;

import staging.j2k.types.Reach;
import staging.j2k.types.HRU;
import java.util.List;
import oms3.ComponentAccess;
import oms3.Compound;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Initialize;
import oms3.annotations.Out;
import oms3.util.Threads;
import oms3.util.Threads.CompList;
import routing.J2KProcessReachRouting;

/**
 *
 * @author od
 */
public class ReachRouting {

    @In public List<Reach> reaches;
    @In public List<HRU> hrus; // only to sync
    
    @In public double basin_area;
    
    @Out public double channelStorage;
    @Out public double catchmentRD1;
    @Out public double catchmentRD2;
    @Out public double catchmentRG1;
    @Out public double catchmentRG2;
    @Out public double catchmentSimRunoff;

    Main model;
    
    CompList<Reach> list;
    Reach outlet;

    ReachRouting(Main model) {
        this.model = model;
    }

    @Execute
    public void execute() throws Exception {
        if (list == null) {
            list = new CompList<Reach>(reaches) {

                @Override
                public Compound create(Reach reach) {
                    return new Processes(reach);
                }
            };

            for (Compound c : list) {
                ComponentAccess.callAnnotated(c, Initialize.class, true);
            }

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
        
//        long now1 = System.currentTimeMillis();
        Threads.seq_e(list);
//        long now2 = System.currentTimeMillis();
//        System.out.println("   Reach Routing :  " + (now2 - now1));


        // aggregate channel storage
        channelStorage = 0;
        for (Reach reach : reaches) {
            channelStorage += reach.channelStorage;
        }

        // catchment weighting
        catchmentRD1 = outlet.outRD1 * basin_area;
        catchmentRD2 = outlet.outRD2 * basin_area;
        catchmentRG1 = outlet.outRG1 * basin_area;
        catchmentRG2 = outlet.outRG2 * basin_area;
        // convert l/day -> qm/s
        catchmentSimRunoff = outlet.simRunoff / (double)(1000*86400);
//        if (catchmentSimRunoff != 0) {
//            System.out.println("total: " + catchmentSimRunoff + "/" + catchmentRD1 + "/" + catchmentRD2 + "/" + catchmentRG1 + "/" + catchmentRG2);
//        }
    }

    public class Processes extends Compound {

        public Reach reach;

        J2KProcessReachRouting routing = new J2KProcessReachRouting();

        Processes(Reach reach) {
            this.reach = reach;
        }

        @Initialize
        public void initialize() {
            // array selection
            field2in(this, "reach", routing);
            field2in(model, "flowRouteTA", routing);
            field2in(model, "tempRes", routing);
            
            initializeComponents();
        }
    }
}
