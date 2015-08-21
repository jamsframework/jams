package staging.j2k;

import staging.j2k.types.Reach;
import staging.j2k.types.HRU;
import climate.RainCorrectionRichter;
import io.ArrayGrabber;
import io.Output1;
import io.StationUpdater;
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
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/staging/j2k/Temporal.java $")
@VersionInfo
    ("$Id: Temporal.java 961 2010-02-11 20:35:32Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
public class Temporal  extends Iteration {

    private static final Logger log = Logger.getLogger("oms3.model."
            + Temporal.class.getSimpleName());

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

    StationUpdater updateTmean = new StationUpdater();
    StationUpdater updateTmin = new StationUpdater();
    StationUpdater updateTmax = new StationUpdater();
    StationUpdater updateAhum = new StationUpdater();
    StationUpdater updatePrecip = new StationUpdater();
    StationUpdater updateSunh = new StationUpdater();
    StationUpdater updateWind = new StationUpdater();

    RainCorrectionRichter rainCorr = new RainCorrectionRichter();

    Output1 out = new Output1();

    Main model;
    SurfaceProcesses sProc;
    SubSurfaceProcesses ssProc;
    ReachRouting reachRout;

    Temporal(Main model) {
        this.model = model;
        sProc = new SurfaceProcesses(model, this);
        ssProc = new SubSurfaceProcesses(model, this);
        reachRout = new ReachRouting(model);
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
        conditional(updateTmean, "moreData");
        in2in("basin_area", sProc);

        // all updater
        upd(updateTmean, "Tmean");
        upd(updateTmin, "Tmin");
        upd(updateTmax, "Tmax");
        upd(updateAhum, "Ahum");
        upd(updatePrecip, "Precip");
        upd(updateSunh, "Sunh");
        upd(updateWind, "Wind");

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
        out2in(updateTmean, "time", rainCorr);
        out2in(updatePrecip, "dataArray", rainCorr, "precip");
        out2in(updateTmean, "dataArray", rainCorr, "temperature");
        out2in(updateTmean, "regCoeff", rainCorr, "tempRegCoeff");
        
        out2in(rainCorr, "rcorr", sProc, "dataArrayRcorr");

        // surface processes
        out2in(updateTmean, "time", sProc);
        in2in("hrus", sProc);

        // subsurface processes
        in2in("basin_area", ssProc);
        out2in(sProc, "hrus", ssProc, "hrus");
        out2in(updateTmean, "time", ssProc);

        // reach routing
        in2in("basin_area", reachRout);
        in2in("reaches", reachRout);
        out2in(ssProc, "hrus", reachRout, "hrus");
        
        // output
        field2in(model, "outFile", out);
        out2in(updateTmean, "time", out);
        out2in(ssProc, "solRad", out);
        out2in(ssProc, "rhum", out);
        out2in(reachRout, "catchmentSimRunoff", out);
        out2in(reachRout, "catchmentRD1", out);
        out2in(reachRout, "catchmentRD2", out);
        out2in(reachRout, "catchmentRG1", out);
        out2in(reachRout, "catchmentRG2", out);

        initializeComponents();
    }
}
