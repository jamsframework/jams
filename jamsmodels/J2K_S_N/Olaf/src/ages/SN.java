/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ages;

import weighting.AreaAggregator1;
import io.LayeredSoilParaReader;
import io.StationReader;
import java.io.File;
import java.util.Calendar;
import oms3.Compound;
import oms3.annotations.*;
import io.EntityReader;
import io.ManagementParaReader;
import static oms3.annotations.Role.*;

/**
 * AgES watershed model. (J2000) equiv.
 * 
 * @author od
 */
public class SN extends Compound {

    @Description("Attribute set.")
    @Role(PARAMETER)
    @In public String attrSet;

    @Description("Attribute set n")
    @Role(PARAMETER)
    @In public String attrSet_n;

    @Description("Attribute set n")
    @Role(PARAMETER)
    @In public String attrSet_pool;

    @Description("Attribute set n")
    @Role(PARAMETER)
    @In public String attrSet_hru;
    @Description("Attribute set n")
    @Role(PARAMETER)
    @In public String attrSet_reach;

    @Role(Role.PARAMETER + Role.OUTPUT)
    @In public File outFile;
    @Role(Role.PARAMETER + Role.OUTPUT)
    @In public File outFile_n;
    @Role(Role.PARAMETER + Role.OUTPUT)
    @In public File outFile_pool;
    @Role(Role.PARAMETER + Role.OUTPUT)
    @In public File outFile_hru;
    @Role(Role.PARAMETER + Role.OUTPUT)
    @In public File outFile_reach;

    @Role(Role.PARAMETER + Role.OUTPUT)
    @In public File balFile;

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

    @Description("Management")
    @Role(PARAMETER + INPUT)
    @In public File mgmtFile;
    @Role(PARAMETER + INPUT)
    @In public File tillFile;
    @Role(PARAMETER + INPUT)
    @In public File rotFile;
    @Role(PARAMETER + INPUT)
    @In public File fertFile;
    @Role(PARAMETER + INPUT)
    @In public File cropFile;
    @Role(PARAMETER + INPUT)
    @In public File hruRotFile;

    @Description("DB function")
    @Role(PARAMETER)
    @In public boolean equalWeights;

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

    @Description("Power of IDW function for regionalisation")
    @Role(PARAMETER)
    @In public double pidwTmean;

    @Description("Power of IDW function for regionalisation")
    @Role(PARAMETER)
    @In public double pidwTmin;

    @Description("Power of IDW function for regionalisation")
    @Role(PARAMETER)
    @In public double pidwTmax;
    
    @Description("Power of IDW function for regionalisation")
    @Role(PARAMETER)
    @In public double pidwAhum;

    @Description("Power of IDW function for regionalisation")
    @Role(PARAMETER)
    @In public double pidwPrecip;

    @Description("Power of IDW function for regionalisation")
    @Role(PARAMETER)
    @In public double pidwSunh;

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

    @Description("K-Value for the riverbed")
    @Unit("cm/d")
    @In public double Ksink;

    // sn
    // TODO map further
    @Description("water-use distribution parameter for Transpiration")
    @Role(PARAMETER)
    @In public double BetaW;

    @Description("Layer MPS diffusion factor > 0 [-]  resistance default = 10")
    @Role(PARAMETER)
    @In public double kdiff_layer;

    @Description("Indicates fertilazation optimization with plant demand")
    @Role(PARAMETER)
    @In public double opti;

    @Description("Date to start reduction")
    @Role(PARAMETER)
    @In public java.util.Calendar startReduction;

    @Description("Date to end reduction")
    @Role(PARAMETER)
    @In public java.util.Calendar endReduction;

    @Description("half-live time of nitrate in groundwater RG1 (time to reduce the amount of nitrate to its half) in a. If the value is 0 denitrifikation is inaktive (1 - 5)")
    @Role(PARAMETER)
    @In public double halflife_RG1;

    @Description("half-live time of nitrate in groundwater RG2 (time to reduce the amount of nitrate to its half) in a. If the value is 0 denitrifikation is inaktive (1 - 5)")
    @Role(PARAMETER)
    @In public double halflife_RG2;

    @Description("Indicates PIADIN application")
    @Role(PARAMETER)
    @In public int piadin;

    @Description(" rate constant between N_activ_pool and N_stabel_pool = 0.00001")
    @Role(PARAMETER)
    @In public double Beta_trans;

    @Description(" rate factor between N_activ_pool and NO3_Pool to be calibrated 0.001 - 0.003")
    @Role(PARAMETER)
    @In public double Beta_min;

    @Description(" rate factor between Residue_pool and NO3_Pool to be calibrated 0.1 - 0.02")
    @Role(PARAMETER)
    @In public double Beta_rsd;

    @Description(" percolation coefitient to calibrate = 0.2")
    @Role(PARAMETER)
    @In public double Beta_NO3;

    @Description("nitrogen uptake distribution parameter to calibrate = 1 - 15")
    @Role(PARAMETER)
    @In public double Beta_Ndist;

    @Description("infiltration bypass parameter to calibrate = 0 - 1")
    @Role(PARAMETER)
    @In public double infil_conc_factor;

    @Description("denitrfcation saturation factor normally at 0.95 to calibrate 0 - 1")
    @Role(PARAMETER)
    @In public double denitfac;

    @Description("concentration of Nitrate in rain = 0 - 0.05")
    @Unit("kgN/(mm * ha)")
    @Role(PARAMETER)
    @In public double deposition_factor;

    @Description("Light Extinct Coefficient [-0.65]")
    @Role(PARAMETER)
    @In public double LExCoef;

    @Description("Factor of rootdepth 0 - 10 default 1")
    @Role(PARAMETER)
    @In public double rootfactor;

    @Description("Temperature lag coefficient perhaps to calibrate, typcal value 0.8, range  0 - 1")
    @Role(PARAMETER)
    @In public double temp_lag;

    @Description("switch for mulch drilling scenario")
    @Role(PARAMETER)
    @In public double sceno;

    @Description("maximum percolation rate out of soil")
    @Unit("mm/d")
    @Role(PARAMETER)
    @In public double geoMaxPerc;

    @Description("Relativ size of the groundwaterN damping tank RG1 0 - 10 to calibrate in -")
    @Role(PARAMETER)
    @In public double N_delay_RG1;

    @Description("Relativ size of the groundwaterN damping tank RG2 0 - 10 to calibrate in -")
    @Role(PARAMETER)
    @In public double N_delay_RG2;

    @Description("HRU Concentration for RG1")
    @Role(PARAMETER)
    @Unit("mgN/l")
    @In  public double N_concRG1;

    @Description("HRU Concentration for RG2")
    @Role(PARAMETER)
    @Unit("mgN/l")
    @In  public double N_concRG2;


    // </editor-fold>

    EntityReader paramReader = new EntityReader();
    ManagementParaReader mgmtReader = new ManagementParaReader();
    LayeredSoilParaReader soilReader = new LayeredSoilParaReader();

    StationReader tminReader = new StationReader();
    StationReader tmaxReader = new StationReader();
    StationReader ahumReader = new StationReader();
    StationReader precipReader = new StationReader();
    StationReader sunhReader = new StationReader();
    StationReader windReader = new StationReader();

    AreaAggregator1 basinAggr = new AreaAggregator1();
    InitProcesses initHRU = new InitProcesses(this);

    PrepTemporal prep = new PrepTemporal(this);
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
        in2in("stFile", soilReader);
        in2in("gwFile", paramReader);

        in2in("mgmtFile", mgmtReader);
        in2in("tillFile", mgmtReader);
        in2in("rotFile", mgmtReader);
        in2in("fertFile", mgmtReader);
        in2in("cropFile", mgmtReader);
        in2in("hruRotFile", mgmtReader);

        in2in("dataFileTmin", tminReader, "dataFile");
        in2in("dataFileTmax", tmaxReader, "dataFile");
        in2in("dataFileAhum", ahumReader, "dataFile");
        in2in("dataFilePrecip", precipReader, "dataFile");
        in2in("dataFileSunh", sunhReader, "dataFile");
        in2in("dataFileWind", windReader, "dataFile");

        // station reader 
        conn(tminReader, "Tmean");
        conn(tminReader, "Tmin");
        conn(tmaxReader, "Tmax");
        conn(ahumReader, "Ahum");
        conn(precipReader, "Precip");
        conn(sunhReader, "Sunh");
        conn(windReader, "Wind");

        out2in(paramReader, "hrus", soilReader);
        out2in(soilReader, "hrus", mgmtReader);
        out2in(mgmtReader, "hrus", basinAggr, initHRU);
        out2in(basinAggr, "basin_area", initHRU, temporal);
        out2in(paramReader, "reaches", temporal, "reaches");
        
//        out2in(initHRU, "hrus", temporal, "hrus");
        
        out2in(tminReader, "xCoord", prep, "xCoordTmean");
        out2in(tminReader, "yCoord", prep, "yCoordTmean");
        out2in(tminReader, "elevation", prep, "elevationTmean");

        out2in(initHRU, "hrus", prep);
        out2in(prep, "hrus", temporal);

        initializeComponents();
    }
}
