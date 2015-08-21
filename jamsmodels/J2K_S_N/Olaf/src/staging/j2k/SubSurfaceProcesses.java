package staging.j2k;

import staging.j2k.types.HRU;
import groundwater.J2KProcessGroundwater;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import oms3.ComponentAccess;
import oms3.Compound;
import oms3.annotations.*;
import oms3.util.Threads;
import routing.J2KProcessRouting1;
import soilWater.J2KProcessLumpedSoilWater1;
import static oms3.util.Threads.*;
import static oms3.annotations.Role.*;

@Description
    ("InitHRU Context component.")
@Author
    (name = "Olaf David")
@Keywords
    ("Utilities")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/staging/j2k/SubSurfaceProcesses.java $")
@VersionInfo
    ("$Id: SubSurfaceProcesses.java 952 2010-02-11 19:55:02Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
public class SubSurfaceProcesses {

    private static final Logger log = Logger.getLogger("oms3.model." +
            SubSurfaceProcesses.class.getSimpleName());

    Main model;
    Temporal timeLoop;
  
    @Description("HRU list")
    @In @Out public List<HRU> hrus;

    @Description("current time")
    @In public Calendar time;

    @Description("basin area")
    @In public double basin_area;

    // basin aggregates  (HRU weighted)
    @Out public double precip;
    @Out public double tmean;
    @Out public double rhum;
    @Out public double wind;
    @Out public double solRad;
    @Out public double netRad;
    @Out public double snowDepth;
    @Out public double satMPS;
    @Out public double satLPS;
    @Out public double rs;
    @Out public double ra;

    //  basin aggregated  (Basin weighted)
    @Out public double rain;
    @Out public double snow;
    @Out public double potET;
    @Out public double refET;
    @Out public double actET;
    @Out public double netSnow;
    @Out public double netRain;
    @Out public double thoughfall;
    @Out public double interception;
    @Out public double intercStorage;
    @Out public double snowTotSWE;
    @Out public double snowMelt;
    @Out public double actMPS;
    @Out public double actLPS;
    @Out public double actDPS;
    @Out public double percolation;
    @Out public double actRG1;
    @Out public double actRG2;
    @Out public double outRD1;
    @Out public double outRD2;
    @Out public double outRG1;
    @Out public double outRG2;
    
    CompList<HRU> list;

    public SubSurfaceProcesses(Main model, Temporal timeLoop) {
        this.model = model;
        this.timeLoop = timeLoop;
    }

    @Execute
    public void execute() throws Exception {

        if (list == null) {
            System.out.println(" Creating Subsurface Processes ...");
            list = new CompList<HRU>(hrus) {

                @Override
                public Compound create(HRU hru) {
                    return new Processes(hru);
                }
            };

            System.out.println(" Init Subsurface Processes ....");
            for (Compound c : list) {
                ComponentAccess.callAnnotated(c, Initialize.class, true);
            }
        }
//        long now1 = System.currentTimeMillis();
        Threads.seq_e(list);
//        long now2 = System.currentTimeMillis();
//        System.out.println("   Subsurface Loop:  " + (now2 - now1));

        // HRU weighting
        precip = tmean = rhum = wind = solRad = netRad = snowDepth =
                satMPS = satLPS = rs = ra = 0;
        for (HRU hru : hrus) {
            double aw = hru.area_weight;
            precip += hru.precip / aw;
            tmean += hru.tmean / aw;
            rhum += hru.rhum / aw;
            wind += hru.wind / aw;
            solRad += hru.solRad / aw;
            netRad += hru.netRad / aw;
            snowDepth += hru.snowDepth / aw;
            satMPS += hru.satLPS / aw;
            rs += hru.rs / aw;
            ra += hru.ra / aw;
        }

        // basin Weighting
        rain=snow=potET=refET=actET=netSnow=netRain=thoughfall=interception=
            intercStorage=snowTotSWE=snowMelt=actMPS=actLPS=actDPS=percolation=
            actRG1=actRG2=outRD1=outRD2=outRG1=outRG2=0;

        for (HRU hru : hrus) {
            rain += hru.rain / basin_area;
            snow += hru.snow / basin_area;
            potET += hru.potET / basin_area;
            refET += hru.refET / basin_area;
            actET += hru.actET / basin_area;
            netSnow += hru.netSnow / basin_area;
            netRain += hru.netRain / basin_area;
            thoughfall += hru.thoughfall / basin_area;
            interception += hru.interception / basin_area;
            intercStorage += hru.intercStorage / basin_area;
            snowTotSWE += hru.snowTotSWE / basin_area;
            snowMelt += hru.snowMelt / basin_area;
            actMPS += hru.actMPS / basin_area;
            actLPS += hru.actLPS / basin_area;
            actDPS += hru.actDPS / basin_area;
            percolation += hru.percolation / basin_area;
            actRG1 += hru.actRG1 / basin_area;
            actRG2 += hru.actRG2 / basin_area;
            outRD1 += hru.outRD1 / basin_area;
            outRD2 += hru.outRD2 / basin_area;
            outRG1 += hru.outRG1 / basin_area;
            outRG2 += hru.outRG2 / basin_area;
        }

//        System.out.println(actMPS + "/" + actLPS + "/" + actDPS);
//        System.out.println(netRain + "/" + netSnow);
//        System.out.println(snow + "/" + rain);

//        if (log.isLoggable(Level.INFO)) {
//            log.info("Basin area :" + basin_area);
//        }
    }

//    @Finalize
//    public void done() {
//        for (Compound compound : list) {
//            compound.finalizeComponents();
//        }
//    }

    public class Processes extends Compound {

        public HRU hru;
        
        J2KProcessLumpedSoilWater1 soil = new J2KProcessLumpedSoilWater1();
        J2KProcessGroundwater gw = new J2KProcessGroundwater();
        J2KProcessRouting1 routing = new J2KProcessRouting1();

        Processes(HRU hru) {
            this.hru = hru;
        }

        @Initialize
        public void initialize() {
            // soil
            field2in(model, "FCAdaptation", soil);
            field2in(model, "ACAdaptation", soil);
            field2in(model, "satStartLPS", soil);
            field2in(model, "satStartMPS", soil);
            field2in(model, "soilMaxDPS", soil);
            field2in(model, "soilPolRed", soil);
            field2in(model, "soilLinRed", soil);
            field2in(model, "soilMaxInfSummer", soil);
            field2in(model, "soilMaxInfWinter", soil);
            field2in(model, "soilMaxInfSnow", soil);
            field2in(model, "soilImpGT80", soil);
            field2in(model, "soilImpLT80", soil);
            field2in(model, "soilDistMPSLPS", soil);
            field2in(model, "soilDiffMPSLPS", soil);
            field2in(model, "soilOutLPS", soil);
            field2in(model, "soilLatVertLPS", soil);
            field2in(model, "soilMaxPerc", soil);
            field2in(model, "soilConcRD1", soil);
            field2in(model, "soilConcRD2", soil);
            field2in(SubSurfaceProcesses.this, "time", soil);
            field2in(this, "hru", soil);

            // groundwater
            field2in(model, "gwCapRise", gw);
            field2in(model, "gwRG1RG2dist", gw);
            field2in(model, "gwRG1Fact", gw);
            field2in(model, "gwRG2Fact", gw);
            field2in(model, "initRG1", gw);
            field2in(model, "initRG2", gw);
            out2in(soil, "hru", gw, "hru");
            
            // routing
            out2in(gw, "hru", routing, "hru");

            initializeComponents();
        }
    }
}
