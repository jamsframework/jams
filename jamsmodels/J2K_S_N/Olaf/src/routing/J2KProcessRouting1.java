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
    ("$Id: J2KProcessRouting1.java 952 2010-02-11 19:55:02Z odavid $")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/routing/J2KProcessRouting1.java $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
public class J2KProcessRouting1  {
    
    @Description("Current HRU")
    @In public HRU hru;
    
    @Execute
    public void execute() {
        route(hru);
//        hru.inGWExcess = 0;
    }

    public static void route(HRU hru) {
        HRU to_poly = hru.to_hru;
        Reach to_reach = hru.to_reach;

        if (to_poly != null) {
            to_poly.inRD1 += hru.outRD1;
            to_poly.inRD2 += hru.outRD2;
            to_poly.inRG1 += hru.outRG1;
            to_poly.inRG2 += hru.outRG2;
        } else if (to_reach != null) {
            to_reach.inRD1 += hru.outRD1;
            to_reach.inRD2 += hru.outRD2;
            to_reach.inRG1 += hru.outRG1;
            to_reach.inRG2 += hru.outRG2;
        } else {
            System.out.println("Current entity ID: " + hru + " has no receiver.");
        }
//        hru.outRD1 = 0;
//        hru.outRD2 = 0;
//        hru.outRG1 = 0;
//        hru.outRG2 = 0;
    }
}
