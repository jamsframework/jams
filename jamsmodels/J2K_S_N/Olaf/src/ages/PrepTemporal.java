package ages;

import ages.types.HRU;
import io.StationUpdater;
import climate.TmeanCalc;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import oms3.ComponentAccess;
import oms3.Compound;
import oms3.annotations.*;
import oms3.control.Iteration;
import oms3.util.Threads;
import oms3.util.Threads.CompList;
import regionalization.Regionalization1;
import soilTemp.TempAvgSumlayer;
import static oms3.annotations.Role.*;

@Description
    ("TimeLoop Context component.")
@Author
    (name = "Olaf David")
@Keywords
    ("Utilities")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/ages/PrepTemporal.java $")
@VersionInfo
    ("$Id: PrepTemporal.java 994 2010-02-19 20:44:19Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
public class PrepTemporal extends Iteration {

    @Description("HRU list, initialized")
    @In @Out public List<HRU> hrus;
    
    @In public double[] xCoordTmean;
    @In public double[] yCoordTmean;
    @In public double[] elevationTmean;
   
    SN model;
    PrepHRU prepHRU;

    StationUpdater updateTmin = new StationUpdater();
    StationUpdater updateTmax = new StationUpdater();

    TmeanCalc tmeanCalc = new TmeanCalc();

    PrepTemporal(SN model) {
        this.model = model;
        prepHRU = new PrepHRU(model, this);
    }

    private void upd(Object updater, String climate) {
        field2in(model, "startTime", updater);
        field2in(model, "endTime", updater);
        field2in(model, "dataFile" + climate, updater, "dataFile");
        out2in(updater, "dataArray", prepHRU, "dataArray" + climate);
        out2in(updater, "regCoeff", prepHRU, "regCoeff" + climate);
    }

    @Initialize
    public void init() throws Exception {
        conditional(updateTmin, "moreData");
//
        upd(updateTmin, "Tmin");
        upd(updateTmax, "Tmax");

         // tmean calculation
        out2in(updateTmin, "dataArray", tmeanCalc, "tmin");
        out2in(updateTmax, "dataArray", tmeanCalc, "tmax");
        in2in("elevationTmean", tmeanCalc, "elevation");
        out2in(tmeanCalc, "dataArray", prepHRU, "dataArrayTmean");
        out2in(tmeanCalc, "regCoeff", prepHRU, "regCoeffTmean");

        out2in(updateTmin, "time", prepHRU);
        in2in("hrus", prepHRU);
        out2out("hrus", prepHRU);
//
        initializeComponents();
    }


    public static class PrepHRU {

        SN model;
        PrepTemporal timeLoop;
        @In @Out public List<HRU> hrus;

        @In public Calendar time;
        @In public double[] dataArrayTmean;
        @In public double[] regCoeffTmean;
        @In public double[] dataArrayTmin;
        @In public double[] regCoeffTmin;
        @In public double[] dataArrayTmax;
        @In public double[] regCoeffTmax;

        public PrepHRU(SN model, PrepTemporal timeLoop) {
            this.model = model;
            this.timeLoop = timeLoop;
        }
        
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        CompList<HRU> list;
        long last = System.currentTimeMillis();

        @Execute
        public void execute() throws Exception {
            if (list == null) {
                System.out.println("Pre Processes ...");
                list = new CompList<HRU>(hrus) {

                    @Override
                    public Compound create(HRU hru) {
                        return new Processes(hru, PrepHRU.this);
                    }
                };

//                System.out.println(" Prep Init...");
                for (Compound c : list) {
                    ComponentAccess.callAnnotated(c, Initialize.class, true);
                }
            }
//            if (time.get(Calendar.DAY_OF_MONTH) == 1) {
//                long now = System.currentTimeMillis();
//                System.out.println("Prep " + f.format(time.getTime()) + " " + (now - last));
//                last = now;
//            }

            Threads.seq_e(list);
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
            public PrepHRU hruLoop;
            
            Regionalization1 tmeanReg = new Regionalization1();
            TempAvgSumlayer tempAvg = new TempAvgSumlayer();

            Processes(HRU hru, PrepHRU loop) {
                this.hru = hru;
                this.hruLoop = loop;
            }

            @Initialize
            public void initialize() {

                // Regionalization
                field2in(hruLoop, "dataArrayTmean", tmeanReg, "dataArray");
                field2in(hruLoop, "regCoeffTmean", tmeanReg, "regCoeff");
                field2in(hru, "elevation", tmeanReg, "entityElevation");
                field2in(hru, "statWeightsTmean", tmeanReg, "statWeights");
                field2in(hru, "orderTmean", tmeanReg, "statOrder");
                field2in(model, "rsqThresholdTmean", tmeanReg, "rsqThreshold");
                field2in(model, "elevCorrTmean", tmeanReg, "elevationCorrection");
                field2in(timeLoop, "elevationTmean", tmeanReg, "statElevation");
                val2in(-273.0, tmeanReg, "fixedMinimum");

                // temp avg
                out2in(tmeanReg, "dataValue", tempAvg, "tmeanpre");   // tmean value
                field2in(hru.soilType, "horizons", tempAvg);
                out2field(tempAvg, "surfacetemp", hru);
                out2field(tempAvg, "soil_Temp_Layer", hru);
                field2inout(hru, "tmeanavg", tempAvg);
                field2inout(hru, "tmeansum", tempAvg);
                field2inout(hru, "i", tempAvg, "i");

                initializeComponents();
            }
        }
    }
}
