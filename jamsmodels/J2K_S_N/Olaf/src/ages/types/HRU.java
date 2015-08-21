package ages.types;

import java.util.ArrayList;
import crop.J2KSNCrop;

public class HRU {

    // from input parameter file;
    public int ID;
    public double x;
    public double y;
    public double elevation;
    public double area;
    public int type;
    public int to_hruID;
    public int to_reachID;
    public double slope;
    public double aspect;
    public double flowlength;
    public int soilID;
    public int landuseID;
    public int hgeoID;
    //
    //references
    public HRU to_hru;
    public Reach to_reach;
    public Landuse landuse;
    public SoilType soilType;
    public HGeo hgeoType;
    //
    public double area_weight;
    public double latitude;
    public double longitude;
    public double[] slAsCfArray;
    public double[] LAIArray;
    public double LAI;     // ?????
    public double actLAI;     // ?????
    public double[] effHArray;
    public double[] extRadArray;
    public double maxRG1;
    public double maxRG2;
    public double actRG1;
    public double actRG2;
    public double[] statWeightsTmean;
    public double[] statWeightsTmin;
    public double[] statWeightsTmax;
    public double[] statWeightsAhum;
    public double[] statWeightsPrecip;
    public double[] statWeightsSunh;
    public double[] statWeightsWind;
    public double tmean;
    public double tmin;
    public double tmax;
    public double ahum;
    public double precip;
    public double wind;
    public double rhum;
    public double solRad;
    public double netRad;
    public double refET;
    public double ra;
    public double rs;
    public double potET;
    public double rain;
    public double snow;
    public double interception;
    public double throughfall;
    public double intercStorage;
    public double actET;
    public double netRain;
    public double netSnow;
    public double snowTotSWE;
    public double drySWE;
    public double totDens;
    public double dryDens;
    public double snowDepth;
    public double snowAge;
    public double snowColdContent;
    public double snowMelt;
    public double satSoil;
    public double maxMPS;
    public double maxLPS;
    public double actMPS;
    public double actLPS;
    public double satMPS;
    public double satLPS;
    public double inRD1;
    public double inRD2;
    public double[] inRD2_h;
    public double outRD1;
    public double outRD2;   // ?????
    public double interflow;
    public double inRG1;
    public double inRG2;
    public double outRG1;
    public double outRG2;
    public double percolation;
    public double actDPS; // check
    // SN
    public double reductionFactor;
    public ArrayList<J2KSNCrop> landuseRotation;
    public int rotPos;
    public int managementPos;
    // IDW
    public int[] orderTmean;
    public int[] orderTmin;
    public int[] orderTmax;
    public int[] orderAhum;
    public int[] orderPrecip;
    public int[] orderSunh;
    public int[] orderWind;
    // dormacy
    public boolean dormacy;
    public double tbase;
    public double FPHUact;
    // man
    public double CanHeightAct;
    public double FNPlant;
    public double frLAImxAct;
    public double frLAImx_xi;
    public double frRootAct;
    public double HarvIndex;
    public double PHUact;
    public boolean plantStateReset;
    public int cropid;
    public double NYield_ha;
    public double NYield;
    public double BioYield;

    // et
    public double[] actETP_h;
    public double aEvap;
    public double aTransp;
    public double pTransp;
    public double pEvap;
    public double[] aEP_h;
    public double[] aTP_h;
    // PG Stress
    public double nstrs;
    public double tstrs;
    public double wstrs;
    public double BioOpt_delta;
    public double BioAct;
    // N Stress
    public double BioNoptAct;
    public double BioNAct;
    // PG temp stress
    public double topt;
    // GW N
    public double NActRG1;
    public double NActRG2;
    public double N_RG1_in;
    public double N_RG2_in;
    public double N_RG1_out;
    public double N_RG2_out;
    public double PercoNabs;
//    public double N_concRG1;
//    public double N_concRG2;
    public double gwExcess;      // ????
    public double NExcess;
    public double pot_RG1;
    public double pot_RG2;
    public double kRG1;
    public double kRG2;
    public double denitRG1;
    public double denitRG2;
    public double percoN_delayRG1;
    public double percoN_delayRG2;
    // ST
    public double totaldepth;
    public double[] satLPS_h;
    public double[] satMPS_h;
    public double[] maxMPS_h;
    public double[] maxLPS_h;
    public double[] maxFPS_h;
//    public double[] Soil_Temp_Layer;
//    public double[] corg_h;
    public double[] NO3_Pool;
    public double[] NH4_Pool;
    public double[] N_activ_pool;
    public double[] N_stabel_pool;
    public double sN_activ_pool;
    public double sN_stabel_pool;
    public double sNO3_Pool;
    public double sNH4_Pool;
    public double sNResiduePool;
    public double sinterflowNabs;
    public double sinterflowN;
    public double[] residue_pool;
    public double[] N_residue_pool_fresh;
    public double[] w_layer_diff;
    public double[] outRD2_h;
    public double Volati_trans;
    public double Nitri_rate;
    public double Denit_trans;
    public double SurfaceN;
    public double PercoN;
    public double SurfaceNabs;
    public double[] InterflowN;
    public double[] InterflowNabs;
    public double[] InterflowN_in;
    public double SurfaceN_in;
    public double actnup;
    public double[] infiltration_hor;
    public double[] perco_hor;
    public double fertNO3;
    public double fertNH4;
    public double fertstableorg;
    public double fertorgNactive;
    public double fertorgNfresh;
    public double fertactivorg;
    public double restfert;
    public int harvesttype;
    public boolean doHarvest;
    public double Nredu;
    public double dayintervall;
    public double gift;
    //
    public double sum_Ninput;
    public double Addresidue_pool;
    public double Addresidue_pooln;
    public boolean dormancy;
    public int App_time;
    public double nmin;
    // ST
    public double tmeanavg;
    public double tmeansum;
    public int i;

    public double[] bulk_density_h;
    public double surfacetemp;
    public double[] soil_Temp_Layer;
    public double soil_Tempaverage;
    public double BioagAct;
    public double zrootd;

    public double soilMaxMPS;
    public double soilMaxLPS;
    public double soilActMPS;
    public double soilActLPS;
    public double soilSatMPS;
    public double soilSatLPS;
    public double infiltration;
    public double genRD1;
    public double[] genRD2_h;
    public double[] actMPS_h;
    public double[] actLPS_h;
    public double soil_root;
    public boolean plantExisting;
    public double sunhmax;
    public double sunhmin;
    public double sunh;

    public double genRG1;
    public double genRG2;


//    public double rootDepth;
    @Override
    public String toString() {
        return "HRU[id=" + ID + "]";
    }
}

