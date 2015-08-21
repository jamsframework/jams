package staging.j2k;

import staging.j2k.types.HRU;
import climate.CalcRelativeHumidity;
import interception.J2KProcessInterception;
import io.ArrayGrabber;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import oms3.ComponentAccess;
import oms3.Compound;
import oms3.annotations.*;
import oms3.util.Threads;
import potet.PenmanMonteith;
import radiation.CalcDailyNetRadiation;
import radiation.CalcDailySolarRadiation;
import regionalization.Regionalization;
import snow.CalcRainSnowParts;
import snow.J2KProcessSnow;
import static oms3.util.Threads.*;
import static oms3.annotations.Role.*;

@Description
    ("InitHRU Context component.")
@Author
    (name = "Olaf David")
@Keywords
    ("Utilities")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/staging/j2k/SurfaceProcesses.java $")
@VersionInfo
    ("$Id: SurfaceProcesses.java 961 2010-02-11 20:35:32Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
public class SurfaceProcesses {

    private static final Logger log = Logger.getLogger("oms3.model." +
            SurfaceProcesses.class.getSimpleName());

    Main model;
    Temporal timeLoop;
  
    @Description("HRU list")
    @In @Out public List<HRU> hrus;

    @In public Calendar time;

    // corrected rain values
    @In public double[] dataArrayRcorr;

    @In public double[] dataArrayTmean;
    @In public double[] regCoeffTmean;

    @In public double[] dataArrayTmin;
    @In public double[] regCoeffTmin;

    @In public double[] dataArrayTmax;
    @In public double[] regCoeffTmax;

    @In public double[] dataArrayAhum;
    @In public double[] regCoeffAhum;

    @In public double[] dataArrayPrecip;
    @In public double[] regCoeffPrecip;

    @In public double[] dataArraySunh;
    @In public double[] regCoeffSunh;

    @In public double[] dataArrayWind;
    @In public double[] regCoeffWind;

    @Description("basin area")
    @In public double basin_area;

    public SurfaceProcesses(Main model, Temporal timeLoop) {
        this.model = model;
        this.timeLoop = timeLoop;
    }

    private static SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
    CompList<HRU> list;

    long last = System.currentTimeMillis();

    @Execute
    public void execute() throws Exception {

        if (list == null) {
            long l = System.currentTimeMillis();
            list = new CompList<HRU>(hrus) {

                @Override
                public Compound create(HRU hru) {
                    return new Processes(hru, SurfaceProcesses.this);
                }
            };
            long n = System.currentTimeMillis();
            System.out.println(" Creating Surface Processes ... " + (n-l));

            l = System.currentTimeMillis();
            for (Compound c : list) {
                ComponentAccess.callAnnotated(c, Initialize.class, true);
            }
            n = System.currentTimeMillis();
            System.out.println(" Init Surface Processes ..." + (n-l));
        }
        if (time.get(Calendar.DAY_OF_MONTH) == 1) {
            long now = System.currentTimeMillis();
            System.out.println(f.format(time.getTime()) + " " + (now - last));
            last = now;
        }
            
//        long now1 = System.currentTimeMillis();
//        Threads.par_e(list);
        Threads.seq_e(list);
//        long now2 = System.currentTimeMillis();
//        System.out.println("   Surface Loop:  " + (now2 - now1));

//        for (HRU hru : hrus) {
//            System.out.println(HRU.soils(hru));
//        }
//        System.out.println();
    }

//    @Finalize
//    public void done() {
//        for (Compound compound : list) {
//            compound.finalizeComponents();
//        }
//    }

    public class Processes extends Compound {

        public HRU hru;
        SurfaceProcesses loop;
        
        ArrayGrabber ag = new ArrayGrabber();
        
        Regionalization tmeanReg = new Regionalization();
        Regionalization tminReg = new Regionalization();
        Regionalization tmaxReg = new Regionalization();
        Regionalization ahumReg = new Regionalization();
        Regionalization precipReg = new Regionalization();
        Regionalization sunhReg = new Regionalization();
        Regionalization windReg = new Regionalization();

        CalcRelativeHumidity relHum = new CalcRelativeHumidity();
        CalcDailySolarRadiation solrad = new CalcDailySolarRadiation();
        CalcDailyNetRadiation netrad = new CalcDailyNetRadiation();
        PenmanMonteith et = new PenmanMonteith();
        CalcRainSnowParts rsParts = new CalcRainSnowParts();
        J2KProcessInterception intc = new J2KProcessInterception();
        J2KProcessSnow snow = new J2KProcessSnow();

        Processes(HRU hru, SurfaceProcesses loop) {
            this.hru = hru;
            this.loop = loop;
        }

        private void reg(Object reg, String climate) {
            field2in(loop, "dataArray" + climate, reg, "dataArray");
            field2in(loop, "regCoeff" + climate, reg, "regCoeff");
            field2in(hru, "elevation", reg, "entityElevation");
            field2in(hru, "statWeights" + climate, reg, "statWeights");
            field2in(model, "elevCorr" + climate, reg, "elevationCorrection");
            field2in(model, "rsqThreshold" + climate, reg, "rsqThreshold");
            field2in(timeLoop, "elevation" + climate, reg, "statElevation");
            field2in(timeLoop, "xCoord" + climate, reg, "statX");
            field2in(timeLoop, "yCoord" + climate, reg, "statY");
        }

        @Initialize
        public void initialize() {
            // array selection
            field2in(model, "tempRes", ag);
            field2in(loop, "time", ag);
            field2in(hru, "extRadArray", ag);
            field2in(hru, "LAIArray", ag);
            field2in(hru, "effHArray", ag);
            field2in(hru.landuse, "RSC0", ag, "rsc0Array");
            field2in(hru, "slAsCfArray", ag);

            // Regionalization
            reg(tmeanReg, "Tmean");
            reg(tminReg, "Tmin");
            reg(tmaxReg, "Tmax");
            reg(ahumReg, "Ahum");
            reg(sunhReg, "Sunh");
            reg(precipReg, "Precip");
            reg(windReg, "Wind");

            // Rel Humidity
            out2in(tmeanReg, "dataValue", relHum, "tmean"); // there are also shadow copies
            out2in(ahumReg, "dataValue", relHum, "ahum");
            out2field(relHum, "rhum", hru, "rhum");

            // solrad
            field2in(model,"angstrom_a", solrad);
            field2in(model,"angstrom_b", solrad);
            field2in(loop, "time", solrad);
            field2in(hru, "latitude", solrad);
            out2in(ag, "actExtRad", solrad);
            out2in(ag, "actSlAsCf", solrad);
            out2in(sunhReg, "dataValue", solrad, "sunh");   // "sunh
            out2field(solrad, "solRad", hru);

            // netrad
            out2in(tmeanReg, "dataValue", netrad, "tmean");
            field2in(hru, "rhum", netrad);
            out2in(ag, "actExtRad", netrad, "extRad");
            out2in(solrad, "solRad", netrad);
            field2in(hru.landuse, "albedo", netrad);
            field2in(hru, "elevation", netrad);
            out2field(netrad, "netRad", hru);

            // ET (Penn/Mont)
            field2in(model, "tempRes", et);
            out2in(windReg, "dataValue", et, "wind");
            out2in(tmeanReg, "dataValue", et, "tmean");
            field2in(hru, "rhum", et);
            field2in(hru, "elevation", et);
            field2in(hru, "area", et);
            out2in(netrad, "netRad", et);
            out2in(ag, "actRsc0", et);
            out2in(ag, "actEffH", et);
            out2in(ag, "actLAI", et);
            
            out2field(et, "potET", hru);
            out2field(et, "rs", hru);
            out2field(et, "ra", hru);

            // rain snow parts
            field2in(model, "snow_trans", rsParts);
            field2in(model, "snow_trs", rsParts);
            field2in(hru, "area", rsParts);
            out2in(precipReg, "dataValue", rsParts, "precip");
            out2in(tmeanReg, "dataValue", rsParts, "tmean");
            out2in(tmeanReg, "dataValue", rsParts, "tmin"); // not a typo!!!
            out2field(rsParts, "rain", hru);
            out2field(rsParts, "snow", hru);

            // interception
            field2in(model, "a_rain", intc);
            field2in(model, "a_snow", intc);
            field2in(model, "snow_trans", intc);
            field2in(model, "snow_trs", intc);
            field2in(hru, "area", intc);
            out2in(et, "potET", intc);
            out2in(ag, "actLAI", intc);
            out2in(rsParts, "rain", intc);
            out2in(rsParts, "snow", intc);
            out2in(tmeanReg, "dataValue", intc, "tmean");
            field2inout(hru, "actET", intc, "actET");  // r/w
            field2inout(hru, "intercStorage", intc, "intercStorage");  // r/w

            out2field(intc, "interception", hru);
            out2field(intc, "throughfall", hru);
            out2field(intc, "netRain", hru);
            out2field(intc, "netSnow", hru);

            // snow
            field2in(model, "baseTemp", snow);
            field2in(model, "t_factor", snow);
            field2in(model, "r_factor", snow);
            field2in(model, "g_factor", snow);
            field2in(model, "snowCritDens", snow);
            field2in(model, "ccf_factor", snow);

            field2in(hru, "area", snow);
            out2in(ag, "actSlAsCf", snow);
            out2in(tmeanReg, "dataValue", snow, "tmean");
            out2in(tmeanReg, "dataValue", snow, "tmin");   // not a typo
            out2in(tmeanReg, "dataValue", snow, "tmax");   // not a typo
            out2in(intc, "netRain", snow);
            out2in(intc, "netSnow", snow);

            out2field(snow, "netRain", hru, "netRain");
            out2field(snow, "netSnow", hru, "netSnow");
            out2field(snow, "snowMelt", hru, "snowMelt");
            
            field2inout(hru, "snowTotSWE", snow, "snowTotSWE");
            field2inout(hru, "drySWE", snow, "drySWE");
            field2inout(hru, "totDens", snow, "totDens");
            field2inout(hru, "dryDens", snow, "dryDens");
            field2inout(hru, "snowDepth", snow, "snowDepth");
            field2inout(hru, "snowAge", snow, "snowAge");
            field2inout(hru, "snowColdContent", snow, "snowColdContent");

            initializeComponents();
        }
    }
}
