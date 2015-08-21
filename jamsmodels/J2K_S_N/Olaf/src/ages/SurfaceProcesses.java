package ages;

import ages.types.HRU;
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
import regionalization.Regionalization1;
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
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/ages/SurfaceProcesses.java $")
@VersionInfo
    ("$Id: SurfaceProcesses.java 996 2010-02-19 21:17:43Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
public class SurfaceProcesses {

    private static final Logger log = Logger.getLogger("oms3.model." +
            SurfaceProcesses.class.getSimpleName());

    SN model;
    Temporal timeLoop;
  
    @Description("HRU list")
    @In @Out public List<HRU> hrus;

    @In public Calendar time;

    @In public double[] dataArrayTmean;
    @In public double[] regCoeffTmean;

    @In public double[] dataArrayTmin;
    @In public double[] regCoeffTmin;

    @In public double[] dataArrayTmax;
    @In public double[] regCoeffTmax;

    @In public double[] dataArrayAhum;
    @In public double[] regCoeffAhum;

    @In public double[] dataArrayPrecip;    // uncorrected values
    @In public double[] regCoeffPrecip;
    @In public double[] dataArrayRcorr;     // corrected rain (Richter) values

    @In public double[] dataArraySunh;
    @In public double[] regCoeffSunh;

    @In public double[] dataArrayWind;
    @In public double[] regCoeffWind;

    @Description("basin area")
    @In public double basin_area;

    public SurfaceProcesses(SN model, Temporal timeLoop) {
        this.model = model;
        this.timeLoop = timeLoop;
    }

    private static SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
    CompList<HRU> list;

    long last = System.currentTimeMillis();

    @Execute
    public void execute() throws Exception {

        if (list == null) {
            System.out.println(" Creating Surface Processes ...");
            list = new CompList<HRU>(hrus) {

                @Override
                public Compound create(HRU hru) {
                    return new Processes(hru, SurfaceProcesses.this);
                }
            };

            System.out.println(" Init Surface Processes ...");
            for (Compound c : list) {
                ComponentAccess.callAnnotated(c, Initialize.class, true);
            }
        }
        if (time.get(Calendar.DAY_OF_MONTH) == 1) {
        long now = System.currentTimeMillis();
            System.out.println(f.format(time.getTime()) + " [" + (now - last) + " ms]");
            last = now;
        }
            
//        long now1 = System.currentTimeMillis();
//        Threads.par_e(list);

//        System.out.print(f.format(time.getTime()) + Arrays.toString(dataArrayTmean) + " " + Arrays.toString(regCoeffTmean) +  " 1 ");
//        for (HRU hru : hrus) {
//            System.out.print(hru.tmean + " ");
//        }
//        System.out.println();
       
        Threads.seq_e(list);

//        System.out.print(f.format(time.getTime()) + " 2 ");
//        for (HRU hru : hrus) {
//            System.out.print(hru.potET + " ");
//        }
//        System.out.println();

//        if (++count == 3)
//            System.exit(1);

//        long now2 = System.currentTimeMillis();
//        System.out.println("   Surface Loop:  " + (now2 - now1));

//        for (HRU hru : hrus) {
//            System.out.println(HRU.soils(hru));
//        }
//        System.out.println();

    }

    @Finalize
    public void done() {
        for (Compound compound : list) {
            compound.finalizeComponents();
        }
    }

    public class Processes extends Compound {

        public HRU hru;
        SurfaceProcesses loop;
        
        ArrayGrabber ag = new ArrayGrabber();
        
        Regionalization1 tmeanReg = new Regionalization1();
        Regionalization1 tminReg = new Regionalization1();
        Regionalization1 tmaxReg = new Regionalization1();
        Regionalization1 ahumReg = new Regionalization1();
        Regionalization1 precipReg = new Regionalization1();
        Regionalization1 sunhReg = new Regionalization1();
        Regionalization1 windReg = new Regionalization1();

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
            field2in(hru, "order" + climate, reg, "statOrder");
            field2in(model, "rsqThreshold" + climate, reg, "rsqThreshold");
            field2in(model, "elevCorr" + climate, reg, "elevationCorrection");
            field2in(timeLoop, "elevation" + climate, reg, "statElevation");
        }

        @Initialize
        public void initialize() {
            // ArrayGrabber
            field2in(model, "tempRes", ag);
            field2in(loop, "time", ag);
            field2in(hru, "extRadArray", ag);
            field2in(hru, "LAIArray", ag);
            field2in(hru, "effHArray", ag);
            field2in(hru.landuse, "RSC0", ag, "rsc0Array");
            field2in(hru, "slAsCfArray", ag);

            // Regionalization tmean
            reg(tmeanReg, "Tmean");
            out2field(tmeanReg, "dataValue", hru, "tmean");
            val2in(-273.0, tmeanReg, "fixedMinimum");

            // Regionalization tmin
            reg(tminReg, "Tmin");
            out2field(tminReg, "dataValue", hru, "tmin");
            val2in(-273.0, tminReg, "fixedMinimum");

            // Regionalization tmax
            reg(tmaxReg, "Tmax");
            out2field(tmaxReg, "dataValue", hru, "tmax");
            val2in(-273.0, tmaxReg, "fixedMinimum");
            
            // Regionalization ahum
            reg(ahumReg, "Ahum");
            out2field(ahumReg, "dataValue", hru, "ahum");
            val2in(0.0, ahumReg, "fixedMinimum");
            
            // Regionalization precip (using rcorr)
            field2in(loop, "dataArrayRcorr", precipReg, "dataArray");
            field2in(loop, "regCoeffPrecip", precipReg, "regCoeff");
            field2in(hru, "elevation", precipReg, "entityElevation");
            field2in(hru, "statWeightsPrecip", precipReg, "statWeights");
            field2in(hru, "orderPrecip", precipReg, "statOrder");
            field2in(model, "rsqThresholdPrecip", precipReg, "rsqThreshold");
            field2in(model, "elevCorrPrecip", precipReg, "elevationCorrection");
            field2in(timeLoop, "elevationPrecip" , precipReg, "statElevation");
            out2field(precipReg, "dataValue", hru, "precip");
            val2in(0.0, precipReg, "fixedMinimum");
            
            // Regionalization sunh
            reg(sunhReg, "Sunh");
            out2field(sunhReg, "dataValue", hru, "sunh");
            val2in(0.0, sunhReg, "fixedMinimum");
            
            // Regionalization wind
            reg(windReg, "Wind");
            out2field(windReg, "dataValue", hru, "wind");
            val2in(0.0, windReg, "fixedMinimum");

            // CalcRelativeHumidity
            out2in(tmeanReg, "dataValue", relHum, "tmean"); // there are also shadow copies
            out2in(ahumReg, "dataValue", relHum, "ahum");
            out2field(relHum, "rhum", hru, "rhum");

            // CalcDailySolarRadiation
            field2in(model,"angstrom_a", solrad);
            field2in(model,"angstrom_b", solrad);
            field2in(loop, "time", solrad);
            field2in(hru, "latitude", solrad);
            out2in(ag, "actExtRad", solrad);
            out2in(ag, "actSlAsCf", solrad);
            out2in(sunhReg, "dataValue", solrad, "sunh");   // "sunh
            out2field(solrad, "solRad", hru);
            out2field(solrad, "sunhmax", hru);

            // CalcDailyNetRadiation
            out2in(tmeanReg, "dataValue", netrad, "tmean");
            out2in(relHum, "rhum", netrad);
            out2in(ag, "actExtRad", netrad, "extRad");
            out2in(solrad, "solRad", netrad);
            field2in(hru.landuse, "albedo", netrad);
            field2in(hru, "elevation", netrad);
            out2field(netrad, "netRad", hru);

            // PenmanMonteith
            field2in(model, "tempRes", et);
            out2in(windReg, "dataValue", et, "wind");
            out2in(tmeanReg, "dataValue", et, "tmean");
            out2in(relHum, "rhum", et);
            field2in(hru, "elevation", et);
            field2in(hru, "area", et);
            out2in(netrad, "netRad", et);
            out2in(ag, "actRsc0", et);
            out2in(ag, "actEffH", et);
//            out2in(ag, "actLAI", et);    
            field2in(hru, "LAI", et, "actLAI");   // TODO problem
            
            out2field(et, "actET", hru);      // zero this out.
            out2field(et, "potET", hru);
            out2field(et, "rs", hru);
            out2field(et, "ra", hru);

            // CalcRainSnowParts
            field2in(model, "snow_trans", rsParts);
            field2in(model, "snow_trs", rsParts);
            field2in(hru, "area", rsParts);
            out2in(precipReg, "dataValue", rsParts, "precip");
            out2in(tmeanReg, "dataValue", rsParts, "tmean");
            out2in(tmeanReg, "dataValue", rsParts, "tmin"); // not a typo!!!
            out2field(rsParts, "rain", hru);
            out2field(rsParts, "snow", hru);

            // J2KProcessInterception
            field2in(model, "a_rain", intc);
            field2in(model, "a_snow", intc);
            field2in(model, "snow_trans", intc);
            field2in(model, "snow_trs", intc);

            out2in(et, "potET", intc);
//            out2in(ag, "actLAI", intc);   
            field2in(hru, "LAI", intc, "actLAI");   // TODO problem
            out2in(et, "actET", intc);
//            field2in(hru, "actET", intc, "actET");  // r/w
              out2field(intc, "actET", hru, "actET");
            field2in(hru, "area", intc);
            field2inout(hru, "intercStorage", intc);  // r/w
            out2in(rsParts, "rain", intc);
            out2in(rsParts, "snow", intc);
            out2in(tmeanReg, "dataValue", intc, "tmean");

            out2field(intc, "interception", hru);
            out2field(intc, "throughfall", hru);
            out2field(intc, "netRain", hru);
            out2field(intc, "netSnow", hru);

            // J2KProcessSnow
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


            out2field(snow, "netRain", hru);
            out2field(snow, "netSnow", hru);
//            field2inout(hru, "netSnow", snow);
//            field2inout(hru, "netRain", snow);
            
            out2field(snow, "snowMelt", hru);
            field2inout(hru, "snowTotSWE", snow);
            field2inout(hru, "drySWE", snow);
            field2inout(hru, "totDens", snow);
            field2inout(hru, "dryDens", snow);
            field2inout(hru, "snowDepth", snow);
            field2inout(hru, "snowAge", snow);
            field2inout(hru, "snowColdContent", snow);

            initializeComponents();
        }
    }
}
