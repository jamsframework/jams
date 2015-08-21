package ages;

import groundwater.J2KProcessGroundwaterSN;
import groundwater.J2KGroundwaterN;
import soilWater.J2KNSoilLayer;
import routing.J2KNRoutinglayer;
import soilTemp.J2KSoilTemplayer;
import crop.J2KSNDormancy;
import crop.PotCropGrowth;
import crop.PlantGrowthStress;
import potet.ETPETP;
import ages.types.HRU;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import oms3.ComponentAccess;
import oms3.Compound;
import oms3.annotations.*;
import oms3.util.Threads;
import routing.J2KProcessHorizonRouting;
import management.ManageLanduseSzeno;
import soilWater.J2KProcessLayeredSoilWater2008;
import static oms3.util.Threads.*;
import static oms3.annotations.Role.*;

@Description
    ("InitHRU Context component.")
@Author
    (name = "Olaf David")
@Keywords
    ("Utilities")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/ages/SubSurfaceProcesses.java $")
@VersionInfo
    ("$Id: SubSurfaceProcesses.java 996 2010-02-19 21:17:43Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
public class SubSurfaceProcesses {

    private static final Logger log = Logger.getLogger("oms3.model." +
            SubSurfaceProcesses.class.getSimpleName());

    SN model;
    Temporal timeLoop;
  
    @Description("HRU list")
    @In @Out public List<HRU> hrus;

    @Description("current time")
    @In @Out public Calendar time;

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
    @Out public double soilSatMPS;
    @Out public double soilSatLPS;

    @Out public double rs;
    @Out public double ra;

    @Out public double BioAct;
    @Out public double actLAI;
    @Out public double LAI;
    @Out public double zrootd;
    @Out public double sNO3_Pool;
    @Out public double sNH4_Pool;
    @Out public double sNResiduePool;
    @Out public double sN_activ_pool;
    @Out public double sN_stabel_pool;
    @Out public double BioNAct;
    @Out public double sunhmax;
    @Out public double FPHUact;
    @Out public double nstrs;
    @Out public double wstrs;
    @Out public double tstrs;
    @Out public double nmin;
    @Out public double NYield;
    @Out public double BioYield;
    @Out public double Addresidue_pooln;
    @Out public double Addresidue_pool;
    @Out public double cropid;
    @Out public double gift;

    //  basin aggregated  (Basin weighted)
    @Out public double rain;
    @Out public double snow;
    @Out public double potET;
    @Out public double refET;
    @Out public double actET;
    @Out public double netSnow;
    @Out public double netRain;
    @Out public double throughfall;
    @Out public double interception;
    @Out public double intercStorage;
    @Out public double snowTotSWE;
    @Out public double snowMelt;
    @Out public double soilActMPS;
    @Out public double soilActLPS;
    @Out public double actDPS;
    @Out public double percolation;
    @Out public double actRG1;
    @Out public double actRG2;
    @Out public double outRD1;
    @Out public double outRD2;
    @Out public double outRG1;
    @Out public double outRG2;
    
    CompList<HRU> list;

    public SubSurfaceProcesses(SN model, Temporal timeLoop) {
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
        
        Threads.seq_e(list);

        // HRU weighting
        precip = tmean = rhum = wind = solRad = netRad = snowDepth =
                soilSatMPS=soilSatLPS= rs = ra =
                BioAct = actLAI = LAI = zrootd = sNO3_Pool = sNH4_Pool =
                sNResiduePool = sN_activ_pool = sN_stabel_pool =
                BioNAct = sunhmax = FPHUact = nstrs = wstrs = tstrs =
                nmin = NYield = BioYield = Addresidue_pooln =
                Addresidue_pool = cropid = gift = 0;

        // to be only reset:
        // precip;tmean;rhum;wind;rain;snow;solRad;netRad;refET;potET;actET;
        // netRain;netSnow;throughfall;interception;intercStorage;
        // snowDepth;snowTotSWE;snowMelt;soilSatMPS;soilSatLPS;
        // soilActMPS;soilActLPS;percolation;actRG1;actRG2;
        // catchmentRD1_w;catchmentRD2_w;catchmentRG1_w;catchmentRG2_w;
        // channelStorage_w;channelStorage;catchmentNRG2_w;catchmentNRG1_w;
        // catchmentNRD2_w;catchmentNRD1_w;catchmentSimRunoffN;BioAct;
        // actLAI;LAI;ZRootD;sNO3_Pool;sNH4_Pool;sNResiduePool;sN_activ_pool;
        // sN_stabel_pool;BioNAct;sunhmax;FPHUact;nstrs;wstrs;tstrs;nmin;
        // NYield;BioYield;Addresidue_pooln;Addresidue_pool;cropid;gift;
        // DeepsinkN;DeepsinkW;model_waterloss;Nitrogenloss
        
        for (HRU hru : hrus) {
            double aw = hru.area_weight;
            precip += hru.precip / aw;
            tmean += hru.tmean / aw;
            rhum += hru.rhum / aw;
            wind += hru.wind / aw;
            solRad += hru.solRad / aw;
            netRad += hru.netRad / aw;
            snowDepth += hru.snowDepth / aw;

            soilSatMPS += hru.soilSatMPS / aw;
            soilSatLPS += hru.soilSatLPS / aw;

            rs += hru.rs / aw;
            ra += hru.ra / aw;

            // sn
            BioAct  += hru.BioAct / aw;
            actLAI += hru.actLAI / aw;
            LAI += hru.LAI / aw;
            zrootd += hru.zrootd / aw;
            sNO3_Pool += hru.sNO3_Pool / aw;
            sNH4_Pool += hru.sNH4_Pool / aw;
            sNResiduePool += hru.sNResiduePool / aw;
            sN_activ_pool += hru.sN_activ_pool / aw;
            sN_stabel_pool += hru.sN_stabel_pool / aw;
            BioNAct += hru.BioNAct / aw;
            sunhmax += hru.sunhmax / aw;
            FPHUact += hru.FPHUact / aw;
            nstrs += hru.nstrs / aw;
            wstrs += hru.wstrs / aw;
            tstrs += hru.tstrs / aw;
            nmin += hru.nmin / aw;
            NYield += hru.NYield / aw;
            BioYield += hru.BioYield / aw;
            Addresidue_pooln += hru.Addresidue_pooln / aw;
            Addresidue_pool += hru.Addresidue_pool / aw;
            cropid += hru.cropid / aw;
            gift += hru.gift / aw;
        }

        // basin Weighting
        rain=snow=potET=refET=actET=netSnow=netRain=throughfall=interception=
            intercStorage=snowTotSWE=snowMelt=soilActMPS=soilActLPS=actDPS=percolation=
            actRG1=actRG2=0;

        for (HRU hru : hrus) {
            rain += hru.rain / basin_area;
            snow += hru.snow / basin_area;
            potET += hru.potET / basin_area;
            refET += hru.refET / basin_area;
            actET += hru.actET / basin_area;
            netSnow += hru.netSnow / basin_area;
            netRain += hru.netRain / basin_area;
            throughfall += hru.throughfall / basin_area;
            interception += hru.interception / basin_area;
            intercStorage += hru.intercStorage / basin_area;
            snowTotSWE += hru.snowTotSWE / basin_area;
            snowMelt += hru.snowMelt / basin_area;
            soilActMPS += hru.soilActMPS / basin_area;
            soilActLPS += hru.soilActLPS / basin_area;
            actDPS += hru.actDPS / basin_area;
            percolation += hru.percolation / basin_area;
            actRG1 += hru.actRG1 / basin_area;
            actRG2 += hru.actRG2 / basin_area;
            outRD1 += hru.outRD1 / basin_area;
            outRD2 += hru.outRD2 / basin_area;
            outRG1 += hru.outRG1 / basin_area;
            outRG2 += hru.outRG2 / basin_area;
        }

//        System.out.print(" Subsurface ");
//        for (HRU hru : hrus) {
//            System.out.print(hru.percolation + " ");
//        }
//        System.out.println();
//        if (log.isLoggable(Level.INFO)) {
//            log.info("Basin area :" + basin_area);
//        }
    }

    @Finalize
    public void done() {
        for (Compound compound : list) {
            compound.finalizeComponents();
        }
    }

    public class Processes extends Compound {

        public HRU hru;
        
        J2KProcessLayeredSoilWater2008 soilWater = new J2KProcessLayeredSoilWater2008();   
        J2KSNDormancy dorm = new J2KSNDormancy();                                     
        ETPETP et = new ETPETP();                                                     
        ManageLanduseSzeno man = new ManageLanduseSzeno();                            
        PotCropGrowth pcg = new PotCropGrowth();                                      
        J2KSoilTemplayer soilTemp = new J2KSoilTemplayer();                           
        J2KNSoilLayer soiln = new J2KNSoilLayer();                                    
        J2KProcessGroundwaterSN gw = new J2KProcessGroundwaterSN();
        J2KGroundwaterN gwn = new J2KGroundwaterN();                           

        PlantGrowthStress pgs = new PlantGrowthStress();                         
        J2KProcessHorizonRouting routing = new J2KProcessHorizonRouting();             
        J2KNRoutinglayer routingN = new J2KNRoutinglayer();                            
        
        Processes(HRU hru) {
            this.hru = hru;
        }

        @Initialize
        public void initialize() {
            // soil

            field2in(this, "hru", soilWater);   // P
            field2in(model, "balFile", soilWater);   // P
            field2in(model, "soilMaxDPS", soilWater);   // P
            field2in(model, "soilPolRed", soilWater);
            field2in(model, "soilLinRed", soilWater);
            field2in(model, "soilMaxInfSummer", soilWater);
            field2in(model, "soilMaxInfWinter", soilWater);
            field2in(model, "soilMaxInfSnow", soilWater);
            field2in(model, "soilImpGT80", soilWater);
            field2in(model, "soilImpLT80", soilWater);
            field2in(model, "soilDistMPSLPS", soilWater);
            field2in(model, "soilDiffMPSLPS", soilWater);
            field2in(model, "soilOutLPS", soilWater);
            field2in(model, "soilLatVertLPS", soilWater);
            field2in(model, "soilMaxPerc", soilWater);
            field2in(model, "geoMaxPerc", soilWater);
            field2in(model, "soilConcRD1", soilWater);
            field2in(model, "soilConcRD2", soilWater);
            field2in(model, "BetaW", soilWater);
            field2in(model, "kdiff_layer", soilWater);

            field2in(hru, "area", soilWater);
            field2in(hru, "slope", soilWater);
            field2in(hru.landuse, "sealedGrade", soilWater);
            field2in(hru, "netRain", soilWater);
            field2in(hru, "netSnow", soilWater);
            field2in(hru, "potET", soilWater);
            field2in(hru, "snowDepth", soilWater);
            field2in(hru, "snowMelt", soilWater);
            field2in(hru.soilType, "horizons", soilWater);
            field2in(hru.soilType, "depth", soilWater, "depth_h");
            field2in(hru, "zrootd", soilWater);
            field2in(hru, "maxMPS_h", soilWater);
            field2in(hru, "maxLPS_h", soilWater);
            field2in(hru, "LAI", soilWater);
            field2in(hru.hgeoType, "Kf_geo", soilWater);
            field2in(hru.soilType, "kf", soilWater, "kf_h");
            field2in(hru.soilType, "root", soilWater, "root_h");

            out2field(soilWater, "soilMaxMPS", hru);
            out2field(soilWater, "soilMaxLPS", hru);
            out2field(soilWater, "soilActMPS", hru);
            out2field(soilWater, "soilActLPS", hru);
            out2field(soilWater, "soilSatMPS", hru);
            out2field(soilWater, "soilSatLPS", hru);
            out2field(soilWater, "infiltration", hru);
            out2field(soilWater, "interflow", hru);
            out2field(soilWater, "percolation", hru);
            out2field(soilWater, "outRD1", hru);
            out2field(soilWater, "genRD1", hru);
            out2field(soilWater, "outRD2_h", hru);
            out2field(soilWater, "genRD2_h", hru);
            out2field(soilWater, "infiltration_hor", hru);
            out2field(soilWater, "perco_hor", hru);
            out2field(soilWater, "actETP_h", hru);
            out2field(soilWater, "w_layer_diff", hru);
            out2field(soilWater, "soil_root", hru);

            field2inout(hru, "actET", soilWater);
            field2inout(hru, "inRD2_h", soilWater);
            field2inout(hru, "inRD1", soilWater);
            field2inout(hru, "actMPS_h", soilWater);
            field2inout(hru, "actLPS_h", soilWater);
            field2inout(hru, "satMPS_h", soilWater);
            field2inout(hru, "satLPS_h", soilWater);
            field2inout(hru, "actDPS", soilWater);
            field2in(SubSurfaceProcesses.this, "time", soilWater);
            
//            field2in(this, "hru", soil);

//          // dormacy
            out2in(soilWater, "hru", dorm, "hru");
            field2in(hru, "sunhmax", dorm, "sunhmax");
            field2in(hru, "latitude", dorm, "latitude");
            field2in(hru, "tbase", dorm, "tbase");
            field2in(hru, "tmean", dorm, "tmean");
            field2in(hru, "FPHUact", dorm, "FPHUact");
            out2field(dorm, "dormancy", hru, "dormancy");
            field2inout(hru, "sunhmin", dorm, "sunhmin");

            // et
            out2in(dorm, "hru", et, "hru");
            out2in(soilWater, "actETP_h", et);
            out2in(soilWater, "actET", et);
//            field2in(hru, "actETP_h", et);
//            field2in(hru, "actET", et);
            field2in(hru.soilType, "horizons", et);
            field2in(hru, "LAI", et);
            field2in(hru, "potET", et);

            out2field(et, "aEvap", hru);
            out2field(et, "aTransp", hru);
            out2field(et, "pEvap", hru);
            out2field(et, "pTransp", hru);
            out2field(et, "aEP_h", hru);
            out2field(et, "aTP_h", hru);
            
            // management
            out2in(et, "hru", man, "hru");
            field2in(model, "opti", man);
            field2in(model, "startReduction", man);
            field2in(model, "endReduction", man);

            field2in(SubSurfaceProcesses.this, "time", man);
            field2in(hru, "nstrs", man);
            field2in(hru, "reductionFactor", man);
            field2in(hru, "nmin", man);
            field2in(hru, "BioNoptAct", man);
            field2in(hru, "BioNAct", man);
            field2in(hru, "FPHUact", man);
            out2field(man, "restfert", hru);
            out2field(man, "harvesttype", hru);
            out2field(man, "fertNH4", hru);
            out2field(man, "fertNO3", hru);
            out2field(man, "fertorgNactive", hru);
            out2field(man, "fertorgNfresh", hru);
            out2field(man, "doHarvest", hru);
            out2field(man, "Nredu", hru);
            
            field2inout(hru, "rotPos", man, "rotPos");
            field2inout(hru, "managementPos", man);
            field2inout(hru, "plantExisting", man);
            field2inout(hru, "dayintervall", man);
            field2inout(hru, "gift", man);

            // PotCG
            out2in(man, "hru", pcg, "hru");
            field2in(model, "LExCoef", pcg);   // P
            field2in(model, "rootfactor", pcg);   // P
            field2in(hru, "rotPos", pcg);
            field2in(hru, "area", pcg);
            field2in(hru, "tmean", pcg);
            field2in(hru, "solRad", pcg);
            field2inout(hru, "BioAct", pcg);
            field2in(hru, "dormancy", pcg);
            field2in(hru, "soil_root", pcg);
            field2in(hru, "harvesttype", pcg);
            field2in(hru, "plantExisting", pcg);
            field2in(hru, "doHarvest", pcg);
            field2inout(hru, "CanHeightAct", pcg);
            field2inout(hru, "zrootd", pcg);
            field2inout(hru, "FNPlant", pcg);
            field2inout(hru, "BioagAct", pcg);
            field2inout(hru, "BioNoptAct", pcg);
            field2inout(hru, "frLAImxAct", pcg);
            field2inout(hru, "frLAImx_xi", pcg);
            field2inout(hru, "frRootAct", pcg);
            field2inout(hru, "BioNAct", pcg);
            field2inout(hru, "HarvIndex", pcg);
            field2inout(hru, "FPHUact", pcg);
            field2inout(hru, "LAI", pcg);
            field2inout(hru, "PHUact", pcg);
            field2inout(hru, "BioOpt_delta", pcg);
            field2inout(hru, "plantStateReset", pcg);
            field2inout(hru, "gift", pcg);

            out2field(pcg, "cropid", hru);
            out2field(pcg, "topt", hru);
            out2field(pcg, "tbase", hru);
            out2field(pcg, "NYield_ha", hru);
            out2field(pcg, "NYield", hru);
            out2field(pcg, "BioYield", hru);
            out2field(pcg, "Addresidue_pooln", hru);
            out2field(pcg, "Addresidue_pool", hru);

            // soiltemp
            out2in(pcg, "hru", soilTemp, "hru");
            field2in(model, "temp_lag", soilTemp);   // P
            field2in(model, "sceno", soilTemp);      // P
            field2in(hru, "area", soilTemp);
            field2in(hru, "tmax", soilTemp);
            field2in(hru, "tmin", soilTemp);
            field2in(hru, "tmeanavg", soilTemp);
            field2in(hru.soilType, "depth", soilTemp, "depth_h");
            field2in(hru.soilType, "horizons", soilTemp);
            field2in(hru.soilType, "bulk_density", soilTemp, "bulk_density_h");
            field2in(hru, "satLPS_h", soilTemp);
            field2in(hru, "satMPS_h", soilTemp);
            field2in(hru, "maxMPS_h", soilTemp);
            field2in(hru, "maxLPS_h", soilTemp);
            field2in(hru, "snowTotSWE", soilTemp);
            field2in(hru, "solRad", soilTemp);
            field2in(hru, "BioagAct", soilTemp);
            field2in(hru, "residue_pool", soilTemp);

            out2field(soilTemp, "surfacetemp", hru);
            out2field(soilTemp, "soil_Tempaverage", hru);
            field2inout(hru, "soil_Temp_Layer", soilTemp);

            // soil N
            out2in(soilTemp, "hru", soiln, "hru");
            out2in(soilWater, "infiltration_hor", soiln);
            out2in(soilWater, "w_layer_diff", soiln);
            out2in(soilTemp, "soil_Temp_Layer", soiln);
            out2in(soilWater, "outRD2_h", soiln);
            out2in(soilWater, "perco_hor", soiln);
            out2in(et, "aEP_h", soiln);

            field2in(model, "piadin", soiln);   
            field2in(model, "opti", soiln);   
            field2in(model, "Beta_trans", soiln);   // P
            field2in(model, "Beta_min", soiln);   // P
            field2in(model, "Beta_rsd", soiln);
            field2in(model, "Beta_NO3", soiln);   // P
            field2in(model, "Beta_Ndist", soiln);   // P
            field2in(model, "infil_conc_factor", soiln);   // P
            field2in(model, "denitfac", soiln);   // P
            field2in(model, "deposition_factor", soiln);   // P
            field2in(SubSurfaceProcesses.this, "time", soiln);

            field2in(hru, "area", soiln);
            field2in(hru.soilType, "horizons", soiln);
            field2in(hru.soilType, "depth", soiln, "depth_h");
            field2in(hru, "totaldepth", soiln);
            field2in(hru, "zrootd", soiln);
            field2in(hru, "satLPS_h", soiln);
            field2in(hru, "satMPS_h", soiln);
            field2in(hru, "maxMPS_h", soiln);
            field2in(hru, "maxLPS_h", soiln);
            field2in(hru, "maxFPS_h", soiln);
            field2in(hru.soilType, "corg", soiln, "corg_h");
            field2inout(hru, "NO3_Pool", soiln);
            field2inout(hru, "NH4_Pool", soiln);
            field2inout(hru, "N_activ_pool", soiln);
            field2inout(hru, "N_stabel_pool", soiln);
            field2inout(hru, "residue_pool", soiln);
            field2inout(hru, "N_residue_pool_fresh", soiln);
           
            field2in(hru, "outRD1", soiln);
            field2in(hru, "percolation", soiln);
            field2inout(hru, "SurfaceN_in", soiln);
            field2inout(hru, "InterflowN_in", soiln);
            field2in(hru, "BioNoptAct", soiln);
            field2inout(hru, "BioNAct", soiln);
            field2in(hru, "actETP_h", soiln);
            field2in(hru, "fertNH4", soiln);
            field2in(hru, "fertNO3", soiln);
            field2in(hru, "fertstableorg", soiln);
            field2in(hru, "fertorgNactive", soiln);
            field2in(hru, "fertorgNfresh", soiln);
            field2in(hru, "Addresidue_pool", soiln);
            field2in(hru, "Addresidue_pooln", soiln);
            field2in(hru, "precip", soiln);
            field2in(hru, "dormancy", soiln);

            field2inout(hru, "App_time", soiln);

            out2field(soiln, "nmin", hru);
            out2field(soiln, "sum_Ninput", hru);
            out2field(soiln, "actnup", hru);
            out2field(soiln, "PercoNabs", hru);
            out2field(soiln, "InterflowNabs", hru);
            out2field(soiln, "SurfaceNabs", hru);
            out2field(soiln, "PercoN", hru);
            out2field(soiln, "InterflowN", hru);
            out2field(soiln, "SurfaceN", hru);
            out2field(soiln, "Denit_trans", hru);
            out2field(soiln, "Nitri_rate", hru);
            out2field(soiln, "Volati_trans", hru);
            out2field(soiln, "sinterflowN", hru);
            out2field(soiln, "sinterflowNabs", hru);
            out2field(soiln, "sNResiduePool", hru);
            out2field(soiln, "sNH4_Pool", hru);
            out2field(soiln, "sNO3_Pool", hru);
            out2field(soiln, "sN_stabel_pool", hru);
            out2field(soiln, "sN_activ_pool", hru);

            // GW
            out2in(soiln, "hru", gw);
            field2in(model, "gwRG1Fact", gw, "gwRG1Fact");
            field2in(model, "gwRG2Fact", gw, "gwRG2Fact");
            field2in(model, "gwRG1RG2dist", gw, "gwRG1RG2dist");
            field2in(model, "gwCapRise", gw, "gwCapRise");
            field2in(model, "initRG1", gw);
            field2in(model, "initRG2", gw);

            field2in(hru, "slope", gw);
            field2inout(hru, "maxRG1", gw);
            field2inout(hru, "maxRG2", gw);
            field2in(hru.hgeoType, "RG1_k", gw);
            field2in(hru.hgeoType, "RG2_k", gw);
            field2in(hru, "percolation", gw);
            field2in(hru, "soilMaxMPS", gw);
            out2field(gw, "pot_RG1", hru);
            out2field(gw, "pot_RG2", hru);
            out2field(gw, "outRG1", hru);
            out2field(gw, "outRG2", hru);
            out2field(gw, "genRG1", hru);
            out2field(gw, "genRG2", hru);
            field2inout(hru, "soilActMPS", gw);
            field2inout(hru, "outRD2", gw, "gwExcess");
//            field2inout(hru, "gwExcess", gw, "gwExcess");
            field2inout(hru, "actRG1", gw);
            field2inout(hru, "actRG2", gw);
            field2inout(hru, "inRG1", gw);
            field2inout(hru, "inRG2", gw);

            // GW N
            out2in(gw, "hru", gwn);
            field2in(model, "N_delay_RG1", gwn);
            field2in(model, "N_delay_RG2", gwn);
            field2in(model, "N_concRG1", gwn);
            field2in(model, "N_concRG2", gwn);
            field2in(hru, "maxRG1", gwn);
            field2in(hru, "maxRG2", gwn);
            field2in(hru, "pot_RG1", gwn);
            field2in(hru, "pot_RG2", gwn);
            field2in(hru, "actRG1", gwn);
            field2in(hru, "actRG2", gwn);
            field2in(hru, "inRG1", gwn);
            field2in(hru, "inRG2", gwn);
            field2in(hru, "outRG1", gwn);
            field2in(hru, "outRG2", gwn);
            field2in(hru, "PercoNabs", gwn);
            field2inout(hru, "N_RG1_in", gwn);
            field2inout(hru, "N_RG2_in", gwn);
            field2inout(hru, "N_RG1_out", gwn);
            field2inout(hru, "N_RG2_out", gwn);
            field2inout(hru, "gwExcess", gwn);
            field2inout(hru, "NExcess", gwn);
            field2inout(hru, "NActRG1", gwn);
            field2inout(hru, "NActRG2", gwn);

            // PG Stress
            out2in(gwn, "hru", pgs);
            out2in(et, "aTransp", pgs);
            out2in(et, "pTransp", pgs);
            field2in(hru,"tmean", pgs);
            field2in(hru,"tbase", pgs);
            field2in(hru,"topt", pgs);
            field2in(hru,"BioNoptAct", pgs);
            field2in(hru,"BioNAct", pgs);
            field2in(hru, "BioOpt_delta", pgs);
            field2inout(hru, "BioAct", pgs);
            out2field(pgs, "wstrs", hru);
            out2field(pgs, "nstrs", hru);
            out2field(pgs, "tstrs", hru);

            // routing
            out2in(pgs, "hru", routing, "hru");
            out2in(soilWater, "outRD2_h", routing);

            // N routing
            out2in(routing, "hru", routingN, "hru");
            out2in(soiln, "InterflowNabs", routingN);

//            initializeComponents();
        }
    }
}
