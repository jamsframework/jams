package routing;

import staging.j2k.types.HRU;
import staging.j2k.types.Reach;
import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description
    ("J2KProcessRouting. Passes the output of the entities as input to the respective reach or unit")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("Routing")
@VersionInfo
    ("$Id: J2KProcessRouting.java 952 2010-02-11 19:55:02Z odavid $")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/routing/J2KProcessRouting.java $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
public class J2KProcessRouting  {
    
//    @Description("HRU statevar RD1 inflow")
//    // TODO READWRITE subst
//    public double inRD1;
//
//    @Description("HRU statevar RD2 inflow")
//    // TODO READWRITE subst
//    public double inRD2;
//
//    @Description("HRU statevar RG1 inflow")
//    // TODO READWRITE subst
//    public double inRG1;
//
//    @Description("HRU statevar RG2 inflow")
//    // TODO READWRITE subst
//    public double inRG2;
//
    @Description("HRU statevar groundwater excess")
    // TODO READWRITE subst
    public double inGWExcess;
    
    @Description("HRU statevar RD1 outflow")
    // TODO READWRITE subst
    public double outRD1;
    
    @Description("HRU statevar RD2 outflow")
    // TODO READWRITE subst
    public double outRD2;
    
    @Description("HRU statevar RD2 outflow")
    // TODO READWRITE subst
    public double outRG1;
    
    @Description("HRU statevar RG2 outflow")
    // TODO READWRITE subst
    public double outRG2;

    // TODO EntityRef
    @Description("Current HRU")
    @In public HRU hru;
    
    @Execute
    public void execute() {
        route(hru);
        
//        hru.inGWExcess = 0;
    }

    public static void route(HRU hru) {
        HRU toPoly = hru.to_hru;
        Reach toReach = hru.to_reach;

        if (toPoly != null) {
            toPoly.inRD1 += hru.outRD1;
            toPoly.inRD2 += hru.outRD2;
            toPoly.inRG1 += hru.outRG1;
            toPoly.inRG2 += hru.outRG2;
        } else if (toReach != null) {
            toReach.inRD1 += hru.outRD1;
            toReach.inRD2 += hru.outRD2;
            toReach.inRG1 += hru.outRG1;
            toReach.inRG2 += hru.outRG2;
        } else {
            System.out.println("Current entity ID: " + hru + " has no receiver.");
        }
        hru.outRD1 = 0;
        hru.outRD2 = 0;
        hru.outRG1 = 0;
        hru.outRG2 = 0;
    }
}
