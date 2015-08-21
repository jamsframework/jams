/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package staging.j2k;

import weighting.AreaAggregator;
import io.StandardEntityReader;
import io.StationReader;
import java.io.File;
import java.util.Calendar;
import oms3.annotations.*;
import static oms3.annotations.Role.*;

/**
 * CEAP watershed component. (J2000) equiv.
 * 
 * @author od
 */
public class Main extends oms3.Compound {

    // <editor-fold desc="Model Parameter">

    // <editor-fold desc=" Simulation Time ">
    @Description("Start of simulation")
    @Role(PARAMETER)
    @In public Calendar startTime;
    
    @Description("End of simulation")
    @Role(PARAMETER)
    @In public Calendar endTime;
    // </editor-fold>

    // <editor-fold desc=" Files ">
    @Description("HRU parameter file name")
    @Role(PARAMETER + INPUT)
    @In public File hruFile;

    @Description("Reach parameter file name")
    @Role(PARAMETER + INPUT)
    @In public File reachFile;

    @Description("Land use parameter file name")
    @Role(PARAMETER + INPUT)
    @In public File luFile;

    @Description("Soil Type parameter file name")
    @Role(PARAMETER + INPUT)
    @In public File stFile;

    @Description("Hydrogeology parameter file name")
    @Role(PARAMETER + INPUT)
    @In public File gwFile;

    @Description("Mean temperature File")
    @Role(PARAMETER + INPUT)
    @In public File dataFileTmean;

    @Description("Min temperature File")
    @Role(PARAMETER + INPUT)
    @In public File dataFileTmin;

    @Description("Max temperature File")
    @Role(PARAMETER + INPUT)
    @In public File dataFileTmax;

    @Description("AHum File")
    @Role(PARAMETER + INPUT)
    @In public File dataFileAhum;

    @Description("Precip temperature File")
    @Role(PARAMETER + INPUT)
    @In public File dataFilePrecip;
    
    @Description("Sunshine hours File")
    @Role(PARAMETER + INPUT)
    @In public File dataFileSunh;

    @Description("Wind speed File")
    @Role(PARAMETER + INPUT)
    @In public File dataFileWind;
    // </editor-fold>

    @Description("Projection [GK, UTMZZL]")
    @Role(PARAMETER)
    @In public String projection;

    @Description("temporal resolution")
    @Unit("d | h")
    @Role(PARAMETER)
    @In public String tempRes;

    @Description("location from Greenwich")
    @Unit("w | e")
    @Role(PARAMETER)
    @In public String locGrw;

    @Description("longitude of time zone")
    @Unit("deg")
    @Role(PARAMETER)
    @In public double longTZ;

    @Description("field capacity adaptation factor")
    @Role(PARAMETER)
    @In public double FCAdaptation;

    @Description("air capacity adaptation factor")
    @Role(PARAMETER)
    @In public double ACAdaptation;

    @Description("start saturation of LPS")
    @Role(PARAMETER)
    @In public double satStartLPS;

    @Description("start saturation of MPS")
    @Role(PARAMETER)
    @In public double satStartMPS;

    @Description("Number of IDW stations")
    @Role(PARAMETER)
    @In public int nidwTmean;

    @Description("Power of IDW function for regionalisation")
    @Role(PARAMETER)
    @In public double pidwTmean;

    @Description("Number of IDW stations")
    @Role(PARAMETER)
    @In public int nidwTmin;

    @Description("Power of IDW function for regionalisation")
    @Role(PARAMETER)
    @In public double pidwTmin;

    @Description("Number of IDW stations")
    @Role(PARAMETER)
    @In public int nidwTmax;

    @Description("Power of IDW function for regionalisation")
    @Role(PARAMETER)
    @In public double pidwTmax;
    
    @Description("Number of IDW stations")
    @Role(PARAMETER)
    @In public int nidwAhum;

    @Description("Power of IDW function for regionalisation")
    @Role(PARAMETER)
    @In public double pidwAhum;

    @Description("Number of IDW stations")
    @Role(PARAMETER)
    @In public int nidwPrecip;

    @Description("Power of IDW function for regionalisation")
    @Role(PARAMETER)
    @In public double pidwPrecip;

    @Description("Number of IDW stations")
    @Role(PARAMETER)
    @In public int nidwSunh;

    @Description("Power of IDW function for regionalisation")
    @Role(PARAMETER)
    @In public double pidwSunh;

    @Description("Number of IDW stations")
    @Role(PARAMETER)
    @In public int nidwWind;

    @Description("Power of IDW function for regionalisation")
    @Role(PARAMETER)
    @In public double pidwWind;

    @Description("number of temperature station for IDW")
    @Role(PARAMETER)
    @In public int tempNIDW;

    @Description("power for IDW function")
    @Role(PARAMETER)
    @In public double pIDW;

    @Description("regression threshold")
    @Role(PARAMETER)
    @In public double regThres;

    @Description("snow_trs")
    @Role(PARAMETER)
    @In public double snow_trs;

    @Description("snow_trans")
    @Role(PARAMETER)
    @In public double snow_trans;

    @Description("Apply elevation correction to measured data")
    @Role(PARAMETER)
    @In public int elevCorrTmean;

    @Description("Minimum value for elevation correction application")
    @Role(PARAMETER)
    @In public double rsqThresholdTmean;

    @Description("Apply elevation correction to measured data")
    @Role(PARAMETER)
    @In public int elevCorrTmin;

    @Description("Minimum value for elevation correction application")
    @Role(PARAMETER)
    @In public double rsqThresholdTmin;

    @Description("Apply elevation correction to measured data")
    @Role(PARAMETER)
    @In public int elevCorrTmax;

    @Description("Minimum value for elevation correction application")
    @Role(PARAMETER)
    @In public double rsqThresholdTmax;

    @Description("Apply elevation correction to measured data")
    @Role(PARAMETER)
    @In public int elevCorrAhum;

    @Description("Minimum value for elevation correction application")
    @Role(PARAMETER)
    @In public double rsqThresholdAhum;
    @Description("Apply elevation correction to measured data")

    @Role(PARAMETER)
    @In public int elevCorrPrecip;

    @Description("Minimum value for elevation correction application")
    @Role(PARAMETER)
    @In public double rsqThresholdPrecip;

    @Role(PARAMETER)
    @In public int elevCorrSunh;

    @Description("Minimum value for elevation correction application")
    @Role(PARAMETER)
    @In public double rsqThresholdSunh;

    @Role(PARAMETER)
    @In public int elevCorrWind;

    @Description("Minimum value for elevation correction application")
    @Role(PARAMETER)
    @In public double rsqThresholdWind;

    @Description("Angstrom factor a")
    @Role(PARAMETER)
    @In public double angstrom_a;

    @Description("Angstrom factor b")
    @Role(PARAMETER)
    @In public double angstrom_b;

    @Role(Role.PARAMETER + Role.OUTPUT)
    @In public File outFile;

    @Description("Interception parameter a_rain")
    @Role(PARAMETER)
    @In public double a_rain;

    @Description("Interception parameter a_snow")
    @Role(PARAMETER)
    @In public double a_snow;

    @Description("base temperature")
    @Role(PARAMETER)
    @In public double baseTemp;

    @Description("temperature factor for snowmelt")
    @Role(PARAMETER)
    @In public double t_factor;

    @Description("rain factor for snowmelt")
    @Role(PARAMETER)
    @In public double r_factor;

    @Description("ground factor for snowmelt")
    @Role(PARAMETER)
    @In public double g_factor;

    @Description("critical density")
    @Role(PARAMETER)
    @In public double snowCritDens;

    @Description("cold content factor")
    @Role(PARAMETER)
    @In public double ccf_factor;

    @Description("maximum depression storage")
    @Unit("mm")
    @Role(PARAMETER)
    @In public double soilMaxDPS;

    @Description("poly reduction of ETP")
    @Role(PARAMETER)
    @In public double soilPolRed;

    @Description("linear reduction of ETP")
    @Role(PARAMETER)
    @In public double soilLinRed;

    @Description("maximum infiltration rate in summer")
    @Unit("mm/d")
    @Role(PARAMETER)
    @In public double soilMaxInfSummer;

    @Description("maximum infiltration rate in winter")
    @Unit("mm/d")
    @Role(PARAMETER)
    @In public double soilMaxInfWinter;

    @Description("maximum infiltration rate on snow")
    @Unit("mm/d")
    @Role(PARAMETER)
    @In public double soilMaxInfSnow;

    @Description("maximum infiltration part on sealed areas (gt 80%)")
    @Role(PARAMETER)
    @In public double soilImpGT80;

    @Description("maximum infiltration part on sealed areas (lt 80%)")
    @Role(PARAMETER)
    @In public double soilImpLT80;

    @Description("MPS/LPS distribution coefficient for inflow")
    @Role(PARAMETER)
    @In public double soilDistMPSLPS;

    @Description("MPS/LPS diffusion coefficient")
    @Role(PARAMETER)
    @In public double soilDiffMPSLPS;

    @Description("LPS outflow coefficient")
    @Role(PARAMETER)
    @In public double soilOutLPS;

    @Description("LPS lateral-vertical distribution coefficient")
    @Role(PARAMETER)
    @In public double soilLatVertLPS;

    @Description("maximum percolation rate")
    @Unit("mm/d")
    @Role(PARAMETER)
    @In public double soilMaxPerc;

    @Description("concentration coefficient for RD1")
    @Role(PARAMETER)
    @In public double soilConcRD1;

    @Description("concentration coefficient for RD2")
    @Role(PARAMETER)
    @In public double soilConcRD2;

    @Description("RG1 correction factor")
    @Role(PARAMETER)
    @In public double gwRG1Fact;

    @Description("RG2 correction factor")
    @Role(PARAMETER)
    @In public double gwRG2Fact;

    @Description("RG1 RG2 distribution factor")
    @Role(PARAMETER)
    @In public double gwRG1RG2dist;

    @Description("capilary rise factor")
    @Role(PARAMETER)
    @In public double gwCapRise;

    @Description("relative initial RG1 storage")
    @Role(PARAMETER)
    @In public double initRG1;

    @Description("relative initial RG2 storage")
    @Role(PARAMETER)
    @In public double initRG2;
  
    @Description("flow routing coefficient TA")
    @Role(PARAMETER)
    @In public double flowRouteTA;

    // </editor-fold>
    StandardEntityReader paramReader = new StandardEntityReader();

    StationReader tmeanReader = new StationReader();
    StationReader tminReader = new StationReader();
    StationReader tmaxReader = new StationReader();
    StationReader ahumReader = new StationReader();
    StationReader precipReader = new StationReader();
    StationReader sunhReader = new StationReader();
    StationReader windReader = new StationReader();

    AreaAggregator basinAggr = new AreaAggregator();
    InitProcesses initHRU = new InitProcesses(this);
    Temporal temporal = new Temporal(this);

    private void conn(Object reader, String type) {
        out2in(reader, "xCoord", initHRU, "xCoord" + type);
        out2in(reader, "xCoord", temporal, "xCoord" + type);
        out2in(reader, "yCoord", initHRU, "yCoord" + type);
        out2in(reader, "yCoord", temporal, "yCoord" + type);
        out2in(reader, "elevation", temporal, "elevation" + type);
    }

    @Initialize
    public void init() {
        in2in("hruFile", paramReader);
        in2in("reachFile", paramReader);
        in2in("luFile", paramReader);
        in2in("stFile", paramReader);
        in2in("gwFile", paramReader);

        in2in("dataFileTmean", tmeanReader, "dataFile");
        in2in("dataFileTmin", tminReader, "dataFile");
        in2in("dataFileTmax", tmaxReader, "dataFile");
        in2in("dataFileAhum", ahumReader, "dataFile");
        in2in("dataFilePrecip", precipReader, "dataFile");
        in2in("dataFileSunh", sunhReader, "dataFile");
        in2in("dataFileWind", windReader, "dataFile");

        // station reader 
        conn(tmeanReader, "Tmean");
        conn(tminReader, "Tmin");
        conn(tmaxReader, "Tmax");
        conn(ahumReader, "Ahum");
        conn(precipReader, "Precip");
        conn(sunhReader, "Sunh");
        conn(windReader, "Wind");

        out2in(paramReader, "hrus", basinAggr, initHRU);
        out2in(basinAggr, "basin_area", initHRU, temporal);

        out2in(initHRU, "hrus", temporal, "hrus");
        out2in(paramReader, "reaches", temporal, "reaches");
        
        initializeComponents();
    }
}
