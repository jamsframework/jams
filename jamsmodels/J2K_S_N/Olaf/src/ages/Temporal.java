package ages;

import staging.j2k.types.Reach;
import staging.j2k.types.HRU;
import climate.RainCorrectionRichter;
import io.OutputSummary;
import io.OutputSummaryList;
import io.StationUpdater;
import climate.TmeanCalc;
import java.util.List;
import java.util.logging.Logger;
import oms3.annotations.*;
import oms3.control.Iteration;
import static oms3.annotations.Role.*;

@Description
    ("TimeLoop Context component.")
@Author
    (name = "Olaf David")
@Keywords
    ("Utilities")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/ages/Temporal.java $")
@VersionInfo
    ("$Id: Temporal.java 994 2010-02-19 20:44:19Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
public class Temporal  extends Iteration {

    private static final Logger log = Logger.getLogger("oms3.model." + Temporal.class.getSimpleName());

    @Description("HRU list, initialized")
    @In public List<HRU> hrus;
    
    @In public List<Reach> reaches;

    @Description("basin area")
    @In public double basin_area;

    @In public double[] xCoordTmean;
    @In public double[] yCoordTmean;
    @In public double[] xCoordTmax;
    @In public double[] yCoordTmax;
    @In public double[] xCoordTmin;
    @In public double[] yCoordTmin;
    @In public double[] xCoordAhum;
    @In public double[] yCoordAhum;
    @In public double[] xCoordPrecip;
    @In public double[] yCoordPrecip;
    @In public double[] xCoordSunh;
    @In public double[] yCoordSunh;
    @In public double[] xCoordWind;
    @In public double[] yCoordWind;

    @In public double[]  elevationTmean;
    @In public double[]  elevationTmin;
    @In public double[]  elevationTmax;
    @In public double[]  elevationAhum;
    @In public double[]  elevationPrecip;
    @In public double[]  elevationSunh;
    @In public double[]  elevationWind;

    StationUpdater updateTmin = new StationUpdater();
    StationUpdater updateTmax = new StationUpdater();
    StationUpdater updateAhum = new StationUpdater();
    StationUpdater updatePrecip = new StationUpdater();
    StationUpdater updateSunh = new StationUpdater();
    StationUpdater updateWind = new StationUpdater();

    TmeanCalc tmeanCalc = new TmeanCalc();

    RainCorrectionRichter rainCorr = new RainCorrectionRichter();

    OutputSummary out;
    OutputSummary out_n;
    OutputSummary out_pool;
    OutputSummaryList out_hru = new OutputSummaryList();
    OutputSummaryList out_reach = new OutputSummaryList();

    SN model;
    SurfaceProcesses sProc;
    SubSurfaceProcesses ssProc;
    ReachRouting reachRout;

    Temporal(SN model) {
        this.model = model;
        sProc = new SurfaceProcesses(model, this);
        ssProc = new SubSurfaceProcesses(model, this);
        reachRout = new ReachRouting(model);
        out = new OutputSummary(new Object[] {ssProc, reachRout});
        out_n = new OutputSummary(new Object[] {ssProc, reachRout});
        out_pool = new OutputSummary(new Object[] {ssProc, reachRout});
    }

    private void upd(Object updater, String climate) {
        field2in(model, "startTime", updater);
        field2in(model, "endTime", updater);
        field2in(model, "dataFile" + climate, updater, "dataFile");
        out2in(updater, "dataArray", sProc, "dataArray" + climate);
        out2in(updater, "regCoeff", sProc, "regCoeff" + climate);
    }
  
    @Initialize
    public void init() throws Exception {
        conditional(updateTmin, "moreData");

        in2in("basin_area", sProc);

        // all updater
        upd(updateTmin, "Tmin");
        upd(updateTmax, "Tmax");
        upd(updateAhum, "Ahum");
        upd(updatePrecip, "Precip");
        upd(updateSunh, "Sunh");
        upd(updateWind, "Wind");

        // tmean calculation
        out2in(updateTmin, "dataArray", tmeanCalc, "tmin");
        out2in(updateTmax, "dataArray", tmeanCalc, "tmax");
        in2in("elevationTmin", tmeanCalc, "elevation");
        out2in(tmeanCalc, "dataArray", sProc, "dataArrayTmean");
        out2in(tmeanCalc, "regCoeff", sProc, "regCoeffTmean");

        // rain correction
        field2in(model, "pIDW", rainCorr);
        field2in(model, "tempNIDW", rainCorr);
        field2in(model, "snow_trans", rainCorr);
        field2in(model, "snow_trs", rainCorr);
        field2in(model, "regThres", rainCorr);

        in2in("elevationTmean", rainCorr, "tempElevation");
        in2in("elevationPrecip", rainCorr, "rainElevation");
        in2in("xCoordTmean", rainCorr, "tempXCoord");
        in2in("yCoordTmean", rainCorr, "tempYCoord");
        in2in("xCoordPrecip", rainCorr, "rainXCoord");
        in2in("yCoordPrecip", rainCorr, "rainYCoord");
        
        out2in(updateTmin, "time", rainCorr);
        out2in(updatePrecip, "dataArray", rainCorr, "precip");
        out2in(tmeanCalc, "dataArray", rainCorr, "temperature");
        out2in(tmeanCalc, "regCoeff", rainCorr, "tempRegCoeff");

        out2in(rainCorr, "rcorr", sProc, "dataArrayRcorr");

        // surface processes
        out2in(updateTmin, "time", sProc);
        in2in("hrus", sProc);

        // subsurface processes
        in2in("basin_area", ssProc);
        out2in(sProc, "hrus", ssProc, "hrus");
        out2in(updateTmin, "time", ssProc);

        // reach routing
        in2in("basin_area", reachRout);
        in2in("reaches", reachRout);
        out2in(ssProc, "hrus", reachRout, "hrus");
        
        // output
        field2in(model, "outFile", out);
        field2in(model, "attrSet", out);
        out2in(updateTmin, "time", out);
        out2in(reachRout, "reaches", out);

        // n
        field2in(model, "outFile_n", out_n, "outFile");
        field2in(model, "attrSet_n", out_n, "attrSet");
        out2in(updateTmin, "time", out_n);
        out2in(reachRout, "reaches", out_n);

        // pool_n
        field2in(model, "outFile_pool", out_pool, "outFile");
        field2in(model, "attrSet_pool", out_pool, "attrSet");
        out2in(updateTmin, "time", out_pool);
        out2in(reachRout, "reaches", out_pool);

        // hru output
        field2in(model, "outFile_hru", out_hru, "outFile");
        field2in(model, "attrSet_hru", out_hru, "attrSet");
        out2in(updateTmin, "time", out_hru);
        out2in(reachRout, "hrus", out_hru, "list");

        // reach output
        field2in(model, "outFile_reach", out_reach, "outFile");
        field2in(model, "attrSet_reach", out_reach, "attrSet");
        out2in(updateTmin, "time", out_reach);
        out2in(reachRout, "reaches", out_reach, "list");

        initializeComponents();
    }
}
