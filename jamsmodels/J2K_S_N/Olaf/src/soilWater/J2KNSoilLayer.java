package soilWater;

import ages.types.HRU;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms3.annotations.*;
import static oms3.annotations.Role.*;

/**
 *
 * @author Manfred Fink
 */
@Author
    (name = "Manfred Fink")
@Description
    ("Calculates N transformation processes in the soil")
@Keywords
    ("Utilities")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/soilWater/J2KNSoilLayer.java $")
@VersionInfo
    ("$Id: J2KNSoilLayer.java 966 2010-02-11 20:45:52Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class J2KNSoilLayer  {
    private static final Logger log =
            Logger.getLogger("oms3.model." + J2KNSoilLayer.class.getSimpleName());

// Parameter
    @Description("Indicates PIADIN application")
    @Role(PARAMETER)
    @In public int piadin;

    @Description("Indicates fertilazation optimization with plant demand")
    @Role(PARAMETER)
    @In public double opti;

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

// In

    @Description("HRU attribute name area")
    @In public double area;

    @Description("number of soil layers")
    @In public int horizons;

    @Description("depth of soil layer")
    @Unit("cm")
    @In public double[] depth_h;

    @Description("depth of soil profile")
    @Unit("cm")
    @In public double totaldepth;

    @Description(" actual depth of roots")
    @Unit("m")
    @In public double zrootd;

    @Description("actual LPS in portion of sto_LPS soil water content")
    @In public double[] satLPS_h;

    @Description("actual MPS in portion of sto_MPS soil water content")
    @In public double[] satMPS_h;

    @Description("maximum MPS  in l soil water content")
    @In public double[] maxMPS_h;

    @Description("maximum LPS  in l soil water content")
    @In public double[] maxLPS_h;

    @Description("maximum FPS  in l soil water content")
    @In public double[] maxFPS_h;

    @Description("soil temperature in layerdepth")
    @In public double[] soil_Temp_Layer;
    
    @Description("organic Carbon in soil")
    @Unit("%")
    @In public double[] corg_h;

    @Description(" actual evaporation")
    @Unit("mm")
    @In public double[] aEP_h ;

    @Description("mps diffusion between layers value")
    @In public double[] w_layer_diff ;

    @Description("surface runoff")
    @Unit("l")
    @In public double outRD1;

    @Description("interflow")
    @Unit("l")
    @In public double[] outRD2_h ;

    @Description(" percolation")
    @Unit("l")
    @In public double percolation;

    @Description("Current organic fertilizer amount added to residue pool")
    @In public double fertorgNfresh;

    @Description(" Input of plant residues")
    @Unit("kg/ha")
    @In public double Addresidue_pool;

    @Description("Nitrogen input of plant residues")
    @Unit("kgN/ha")
    @In public double Addresidue_pooln;

    @Description("precipitation in mm")
    @In public double precip;

    @Description("Current time")
    @In public java.util.Calendar time;

    @Description("indicates dormancy of plants")
    @In public boolean dormancy;

    @Description("intfiltration poritions for the single horizonts")
    @Unit("l")
    @In public double[] infiltration_hor ;

    @Description("percolation out ouf the single horizonts")
    @Unit("l")
    @In public double[] perco_hor ;

    @Description("percolation out ouf the single horizonts")
    @Unit("l")
    @In public double[] actETP_h ;

    @Description(" Ammonium input due to Fertilisation")
    @Unit("kgN/ha")
    @In public double fertNH4;

    @Description(" Stable organig N input due to Fertilisation")
    @Unit("kgN/ha")
    @In public double fertstableorg;

    @Description(" Activ organig N input due to Fertilisation")
    @Unit("kgN/ha")
    @In public double fertorgNactive;

    @Description("potential nitrogen content of plants")
    @Unit("kgN/ha")
    @In public double BioNoptAct;

    @Description(" Nitrate input due to Fertilisation")
    @Unit("kgN/ha")
    @In public double fertNO3;
    


// Out

    @Description(" sum of N-Organic Pool with reactive organic matter")
    @Unit("kgN/ha")
    @Out public double sN_activ_pool;
    
    @Description(" sum of N-Organic Pool with stable organic matter")
    @Unit("kgN/ha")
    @Out public double sN_stabel_pool;

    @Description("sum of NO3-Pool")
    @Unit("kgN/ha")
    @Out public double sNO3_Pool;

    @Description(" sum of NH4-Pool")
    @Unit("kgN/ha")
    @Out public double sNH4_Pool;

    @Description(" sum of NResiduePool")
    @Unit("kgN/ha")
    @Out public double sNResiduePool;
    
    @Description(" sum of interflowNabs")
    @Unit("kgN/ha")
    @Out public double sinterflowNabs;

    @Description(" sum of interflowN")
    @Unit("kgN/ha")
    @Out public double sinterflowN;
    
    @Description(" voltalisation rate from NH4_Pool")
    @Unit("kgN/ha")
    @Out public double Volati_trans;
    
    @Description(" nitrification rate from  NO3_Pool")
    @Unit("kgN/ha")
    @Out public double Nitri_rate;
    
    @Description(" denitrification rate from  NO3_Pool")
    @Unit("kgN/ha")
    @Out public double Denit_trans;
    
    @Description(" Nitrate in surface runoff")
    @Unit("kgN/ha")
    @Out public double SurfaceN;

    @Description(" Nitrate in interflow")
    @Unit("kgN/ha")
    @Out public double[] InterflowN ;
    
    @Description(" Nitrate in percolation")
    @Unit("kgN/ha")
    @Out public double PercoN;
    
    @Description(" Nitrate in surface runoff")
    @Unit("kgN")
    @Out public double SurfaceNabs;
    
    @Description(" Nitrate in interflow")
    @Unit("kgN")
    @Out public double[] InterflowNabs ;
    
    @Description(" Nitrate in percolation")
    @Unit("kgN")
    @Out public double PercoNabs;
    
    @Description("actual nitrate uptake of plants")
    @Unit("kgN/ha")
    @Out public double actnup;
    
    @Description("Sum of N input due fertilisation and deposition")
    @Unit("kgN/ha")
    @Out public double sum_Ninput;

    @Description("Mineral nitrogen content in the soil profile down to 60 cm depth")
    @Out public double nmin;
    
// In Out
    @Description(" Residue in Layer")
    @Unit("kgN/ha")
    @In @Out public double[] residue_pool ;

    @Description(" N-Organic fresh Pool from Residue")
    @Unit("kgN/ha")
    @In @Out  public double[] N_residue_pool_fresh ;

    @Description(" Nitrate in surface runoff added to HRU layer")
    @Unit("kgN")
    @In @Out public double SurfaceN_in;
    
    @Description(" Nitrate in interflow in added to HRU layer")
    @Unit("kgN")
    @In @Out public double[] InterflowN_in ;
    
    @Description("actual nitrate nitrogen content of plants")
    @Unit("kgN/ha")
    @In @Out public double BioNAct;
    
    @Description("time in days since the last PIADIN application")
    @In @Out public int App_time;

    @Description("NO3-Pool")
    @Unit("kgN/ha")
    @In @Out public double[] NO3_Pool;

    @Description(" NH4-Pool")
    @Unit("kgN/ha")
    @In @Out public double[] NH4_Pool ;

    @Description(" N-Organic Pool with reactive organic matter")
    @Unit("kgN/ha")
    @In @Out public double[] N_activ_pool ;

    @Description(" N-Organic Pool with stable organic matter")
    @Unit("kgN/ha")
    @In @Out public double[] N_stabel_pool ;

    @Description("Current hru object")
    @In @Out public HRU hru;
    
    private double gamma_temp;
    private double gamma_water;
    private double runarea;
    private double runSoil_Temp_Layer;
    private double[] runlayerdepth;
    private double sto_MPS;
    private double sto_LPS;
    private double sto_FPS;
    private double act_LPS;
    private double act_MPS;
    private double runC_org;
    private double runNO3_Pool;
    private double runNH4_Pool;
    private double runN_activ_pool;
    private double runN_stabel_pool;
    private double runN_residue_pool_fresh;
    private double runResidue_pool;
    private double RD1_out_mm;
    private double RD2_out_mm;
    private double d_perco_mm;
    private double h_perco_mm;
    private double h_infilt_mm;
    private int layer;
    private double runvolati_trans;
    private double rundenit_trans;
    private double runsurfaceN;
    private double runinterflowN;
    private double runpercoN;
    private double runsurfaceNabs;
    private double runinterflowNabs;
    private double runpercoNabs;
    private double runsurfaceN_in;
    private double runinterflowN_in;
    private double sumlayer;
    private double runBeta_trans;
    private double runBeta_min;
    private double runBeta_rsd;
    private double runBeta_NO3;
    
    private double theta_nit = 0.05; /*fraction of anion excluded soil water. depended from clay content min. 0.01  max. 1*/
    private double fr_actN = 0.02; /** nitrogen active pool fraction. The fraction of organic nitrogen in the active pool. */
    private double N_nit_vol = 0; /** NH4 that is converted to  NO3 Pool or volatilation . */
    private double frac_nitr = 0; /** Fraction of N_nit_vol that is nitrification */
    private double frac_vol = 0; /** Fraction of N_nit_vol that is volatilasation */
    private double Hum_trans; /*transformation rate from NOrg_acti_Pool to N_stabel_pool and back in kgN/ha */
    private double Hum_act_min; /*mirelaization rate from NOrg_acti_Pool to NO3_Pool in kgN/ha */
    private double runnitri_trans = 0; /*nitrifikation rate from NH4_Pool to NO3_Pool in kgN/ha*/
    private double delta_ntr = 0; /*residue decomposition factor */
    private double concN_mobile = 0; /*NO3 concentration of the mobile soil water in kgN/mm H2O*/

    private int app_time = 0;
    double[] hor_by_infilt;
    double[] NO3_Poolvals;
    double[] w_l_diff;
    double[] partnmin;
    double[] diffout;

    @Execute
    public void execute() {

        gamma_temp = 0;
        gamma_water = 0;
        runarea = area;
        app_time = App_time;
        layer =  horizons;
        sumlayer = 0;

        double runsum_Ninput = 0;
        double sumNO3_Pool = 0;
        double sumNH4_Pool = 0;
        double suminterflowNabs = 0;
        double suminterflowN = 0;
        double sumN_residue_pool = 0;
        double sumN_activ_pool = 0;
        double sumN_stabel_pool = 0;
        double Sumvolati_trans = 0;
        double Sumdenit_trans = 0;
        double Sumnitri_trans = 0;
        double sumh_infilt_mm = 0;
        double sum_Nupmove = 0;
        double N_upmove_h = 0;
        double a_deposition = 0;
        double NO3respool = 0;
        double Nactiverespool = 0;
        double diffoutN = 0;
        double runnmin = 0;
        
//        double[] NO3_Poolvals = new double[layer];
        runlayerdepth = new double[layer];
        double[] NH4_Poolvals = new double[layer];
        double[] N_activ_poolvals = new double[layer];
        double[] N_stabel_poolvals = new double[layer];
        double[] N_residue_pool_freshvals = new double[layer];
        double[] Residue_poolvals = new double[layer];
        double[] interflowNvals = new double[layer];
        double[] percoNvals = new double[layer];
        double[] interflowNabsvals = new double[layer];
        double[] percoNabsvals = new double[layer];
        double[] ConcN_mobile = new double[layer];

        runsurfaceN = 0;

        hor_by_infilt = new double[layer];
        diffout = new double[layer];
        partnmin = new double[layer];
        w_l_diff = new double[layer];

//        if (time.get(Calendar.DAY_OF_YEAR) == 140 && hru.ID == 302) {
//            System.out.println("");
//        }

        NO3_Poolvals = calc_plantuptake();

        /* calculation of infiltration water that bypasses the horizonts   loop */
        int i = layer - 1;
        while (i > 0) {
            h_infilt_mm = infiltration_hor[i] / runarea;
            sumh_infilt_mm = sumh_infilt_mm + h_infilt_mm;
            hor_by_infilt[i - 1] = sumh_infilt_mm * infil_conc_factor;
            i--;
        }

        // loops to distibute the layer diffusion water
        for (i = 0; i < layer; i++) {
            diffout[i] = 0;
        }

        for (i = 0; i < layer - 1; i++) {
            w_l_diff[i] = w_layer_diff[i] / runarea;
            if (w_l_diff[i] > 0) {
                diffout[i + 1] = diffout[i + 1] + w_l_diff[i];
            } else {
                diffout[i] = diffout[i] - w_l_diff[i];
            }
        }

        i = 0;
        // horizont processies loop
        while (i < layer) {
            runSoil_Temp_Layer = soil_Temp_Layer[i];

            sto_MPS = maxMPS_h[i] / runarea;
            sto_LPS = maxLPS_h[i] / runarea;
            sto_FPS = maxFPS_h[i] / runarea;

            act_LPS = satLPS_h[i] * sto_LPS;
            act_MPS = satMPS_h[i] * sto_MPS;

            runC_org = corg_h[i] / 1.72;
            runNO3_Pool = NO3_Poolvals[i];
            runNH4_Pool = NH4_Pool[i];
            runN_activ_pool = N_activ_pool[i];
            runN_stabel_pool = N_stabel_pool[i];
            runN_residue_pool_fresh = N_residue_pool_fresh[i];
            runResidue_pool = residue_pool[i];

            RD1_out_mm = outRD1 / runarea;
            RD2_out_mm = outRD2_h[i] / runarea;
            d_perco_mm = percolation / runarea;
            h_perco_mm = perco_hor[i] / runarea;

            runvolati_trans = 0;
            rundenit_trans = 0;
            runinterflowN = 0;
            runpercoN = 0;
            runsurfaceN_in = SurfaceN_in * 10000 / runarea;
            runinterflowN_in = InterflowN_in[i] * 10000 / runarea;
            SurfaceN_in = 0;
            runBeta_trans = Beta_trans;
            runBeta_min = Beta_min;
            runBeta_rsd = Beta_rsd;
            runBeta_NO3 = Beta_NO3;

            if (fertNH4 > 0 && piadin == 1) {
                app_time = 0;
            }
            app_time++;

            /* calculation of amount of nitrogen uptake with epaporation from soil */
            int j = 1;
            while (j < layer) {
                N_upmove_h = calc_nitrateupmove(j);
                sum_Nupmove = sum_Nupmove + N_upmove_h;
                j++;
            }

            gamma_temp = 0.9 * (runSoil_Temp_Layer / (runSoil_Temp_Layer * Math.exp(9.93 - 0.312 * runSoil_Temp_Layer))) + 0.1;
            if (sto_LPS + sto_MPS + sto_FPS > 0) {
                gamma_water = (act_LPS + act_MPS + sto_FPS) / (sto_LPS + sto_MPS + sto_FPS);
            } else {
                gamma_water = 0;
            }

            if (runSoil_Temp_Layer > 5) {
                calc_nit_volati(i);
                runvolati_trans = calc_voltalisation();
                runnitri_trans = calc_nitrification();
                Hum_trans = calc_Hum_trans();
            } else {
                runvolati_trans = 0;
                runnitri_trans = 0;
            }
            
            /*Calculations of NPools   Check Order of calculations !!!!!!!!!!!!!!*/

            runNH4_Pool = runNH4_Pool - (runvolati_trans + runnitri_trans);
            if (runNH4_Pool < 0) {
                runNH4_Pool = 0;
            }

            runN_stabel_pool = runN_stabel_pool + Hum_trans;
            if (runN_stabel_pool < 0) {
                runN_stabel_pool = 0;
            }

            runN_activ_pool = runN_activ_pool - Hum_trans;
            if (runN_activ_pool < 0) {
                runN_activ_pool = 0;
            }

            Hum_act_min = calc_Hum_act_min();
            runN_activ_pool = runN_activ_pool - Hum_act_min;
            if (runN_activ_pool < 0) {
                runN_activ_pool = 0;
            }

            if (i < 1) {
                runResidue_pool = runResidue_pool + Addresidue_pool + (fertorgNfresh * 10);
                runN_residue_pool_fresh = runN_residue_pool_fresh + Addresidue_pooln + fertorgNfresh;
                /*                if (inpN_biomass > 0){
                System.out.println(time.get(time.DAY_OF_YEAR) + " resisuenadd " + inpN_biomass);
                }*/
                runNH4_Pool = runNH4_Pool + fertNH4;
                delta_ntr = calc_Res_N_trans();
                a_deposition = deposition_factor * precip;

                runResidue_pool = runResidue_pool - (delta_ntr * runResidue_pool);
                if (runResidue_pool < 0) {
                    runResidue_pool = 0;
                }
                runsum_Ninput = fertorgNactive + fertNH4 + fertNO3 + fertorgNfresh + a_deposition;
                //runsum_Ninput =   runinterflowN_in ;

                runN_stabel_pool = runN_stabel_pool + fertstableorg;
                Nactiverespool = 0.2 * (delta_ntr * runN_residue_pool_fresh);
                runN_activ_pool = runN_activ_pool + fertorgNactive + Nactiverespool;

                if (runN_activ_pool < 0) {
                    runN_activ_pool = 0;
                }
                NO3respool = 0.8 * (delta_ntr * runN_residue_pool_fresh);
                runNO3_Pool = runNO3_Pool + sum_Nupmove + fertNO3 + a_deposition + runnitri_trans + Hum_act_min + runinterflowN_in + runsurfaceN_in + NO3respool;

//                System.out.println(time.get(time.DAY_OF_YEAR) + " runNO3_Pool " + runNO3_Pool + " sum_Nupmove "+ sum_Nupmove + " fertNO3 "+ fertNO3 + " a_deposition "+ a_deposition + " runnitri_trans "+ runnitri_trans +" runinterflowN_in "+ runinterflowN_in +" runsurfaceN_in "+ runsurfaceN_in +" NO3respool "+ NO3respool);

                rundenit_trans = calc_denitrification();
                runNO3_Pool = runNO3_Pool - rundenit_trans;
                if (runNO3_Pool < 0) {
                    runNO3_Pool = 0;
                }
                runN_residue_pool_fresh = runN_residue_pool_fresh - (delta_ntr * runN_residue_pool_fresh);
                if (runN_residue_pool_fresh < 0) {
                    runN_residue_pool_fresh = 0;
                }
            } else {
                runNO3_Pool = runNO3_Pool + runnitri_trans + runinterflowN_in + percoNvals[i - 1] + Hum_act_min;
                rundenit_trans = calc_denitrification();
                runNO3_Pool = runNO3_Pool - rundenit_trans;
                if (runNO3_Pool < 0) {
                    runNO3_Pool = 0;
                }
            }
            /*Calculations of NFluxes (out)*/
            concN_mobile = calc_concN_mobile(i);
            ConcN_mobile[i] = concN_mobile;
            if (i == 0) {
                runsurfaceN = calc_surfaceN();
                runNO3_Pool = runNO3_Pool - runsurfaceN;
            }
            runinterflowN = calc_interflowN(i);
            runNO3_Pool = runNO3_Pool - runinterflowN;
            runpercoN = calc_percoN(i);
            runNO3_Pool = runNO3_Pool - runpercoN;

            if (runNO3_Pool < 0) {
                runNO3_Pool = 0;
            }

            runinterflowNabs = runinterflowN * runarea / 10000;
            runpercoNabs = runpercoN * runarea / 10000;

            NO3_Poolvals[i] = runNO3_Pool;
            NH4_Poolvals[i] = runNH4_Pool;
            N_activ_poolvals[i] = runN_activ_pool;
            N_stabel_poolvals[i] = runN_stabel_pool;
            N_residue_pool_freshvals[i] = runN_residue_pool_fresh;
            Residue_poolvals[i] = runResidue_pool;
            interflowNvals[i] = runinterflowN;
            interflowNabsvals[i] = runinterflowNabs;
            percoNvals[i] = runpercoN;
            percoNabsvals[i] = runpercoNabs;
            // time;
            sumN_stabel_pool = runN_stabel_pool + sumN_stabel_pool;
            sumN_activ_pool = runN_activ_pool + sumN_activ_pool;
            sumNH4_Pool = runNH4_Pool + sumNH4_Pool;
            sumN_residue_pool = sumN_residue_pool + runN_residue_pool_fresh;

            if (i < 5) {
                sumNO3_Pool = runNO3_Pool + sumNO3_Pool;
            }
            suminterflowNabs = runinterflowNabs + suminterflowNabs;
            suminterflowN = runinterflowN + suminterflowN;
            Sumvolati_trans = Sumvolati_trans + runvolati_trans;
            Sumdenit_trans = Sumdenit_trans + rundenit_trans;
            Sumnitri_trans = Sumnitri_trans + runnitri_trans;
            i++;
        }
        i = 0;

        // distribution of diffusion N into the
        for (i = 0; i < layer - 1; i++) {
            if (w_l_diff[i] < 0) {
                diffoutN = w_l_diff[i] * ConcN_mobile[i];
                NO3_Poolvals[i] = NO3_Poolvals[i] + diffoutN;
                NO3_Poolvals[i + 1] = NO3_Poolvals[i + 1] - diffoutN;
            } else {
                diffoutN = w_l_diff[i] * ConcN_mobile[i + 1];
                NO3_Poolvals[i] = NO3_Poolvals[i] + diffoutN;
                NO3_Poolvals[i + 1] = NO3_Poolvals[i + 1] - diffoutN;
            }
            if (opti == 1) {
                runnmin = (((NO3_Poolvals[i] + NH4_Poolvals[i]) * partnmin[i])) + runnmin;
            }
        }
        // writing of pools
        double[] zerosetter = new double[layer];
        NO3_Pool = NO3_Poolvals;
        NH4_Pool = NH4_Poolvals;
        N_activ_pool = N_activ_poolvals;
        N_stabel_pool = N_stabel_poolvals;
        N_residue_pool_fresh = N_residue_pool_freshvals;
        residue_pool = Residue_poolvals;
        // writing of fluxes

        InterflowN = interflowNvals;
        InterflowNabs = interflowNabsvals;
        PercoN = percoNvals[layer - 1];
        PercoNabs = percoNabsvals[layer - 1];
        SurfaceN = runsurfaceN;

        runsurfaceNabs = runsurfaceN * runarea / 10000;
        SurfaceNabs = runsurfaceNabs;
        sum_Ninput = runsum_Ninput;
        sinterflowNabs = suminterflowNabs;
        sinterflowN = suminterflowN;
        // writing of transfomations time
        Volati_trans = Sumvolati_trans;
        Denit_trans = Sumdenit_trans;
        Nitri_rate = Sumnitri_trans;
        sN_stabel_pool = sumN_stabel_pool;
        sN_activ_pool = sumN_activ_pool;
        sNH4_Pool = sumNH4_Pool;
        sNO3_Pool = sumNO3_Pool;
        sNResiduePool = sumN_residue_pool;
        App_time = app_time;
        InterflowN_in = zerosetter;
        nmin = runnmin;



//        if (time.get(Calendar.DAY_OF_YEAR) == 13 && hru.ID == 302) {
//            System.out.println("");
//        }

        if (log.isLoggable(Level.INFO)) {
            log.info("nmin:" + nmin);
        }


    }


    private double[] calc_plantuptake() {
        double upNO3_Pool = 0;
        double runrootdepth = (zrootd * 100);
        double[] partroot = new double[layer];

        if (BioNoptAct == 0) {
            BioNAct = 0;
        }
        double runpotN_up = BioNoptAct - BioNAct;
        if (dormancy) {
            runpotN_up = 0;
        }

        if (runpotN_up < 0) {
            runpotN_up = 0;
        }

        double[] potN_up_z = new double[layer];
        double[] demandN_up_z = new double[layer];
        double rootlayer = 0;
        double runBeta_Ndist = Beta_Ndist;
        double demand2 = 0;
        double demand1 = 0;
        double uptake1 = 0;
        int ii = 0;
        int j = 0;
        int i = 0;

        double[] NO3_Poolvals1 = NO3_Pool;

        // plant uptake loop 1: calculating layer poritions within rootdepth
        while (i < layer) {
            sumlayer = sumlayer + depth_h[i];
            runlayerdepth[i] = sumlayer;
            if (runrootdepth > runlayerdepth[0]) {
                if (runrootdepth > runlayerdepth[i]) {
                    partroot[i] = 1;
                    rootlayer = i;
                } else if (runrootdepth > runlayerdepth[i - 1]) {
                    partroot[i] = (runrootdepth - runlayerdepth[i - 1]) / (runlayerdepth[i] - runlayerdepth[i - 1]);
                    rootlayer = i;
                } else {
                    partroot[i] = 0;
                }
            } else if (i == 0) {
                partroot[i] = runrootdepth / runlayerdepth[0];
                rootlayer = i;
            }

            if (opti == 1) {
                double Nmin_depth = 60;
                if (Nmin_depth > runlayerdepth[0]) {
                    if (Nmin_depth > runlayerdepth[i]) {
                        partnmin[i] = 1;

                    } else if (Nmin_depth > runlayerdepth[i - 1]) {
                        partnmin[i] = (Nmin_depth - runlayerdepth[i - 1]) / (runlayerdepth[i] - runlayerdepth[i - 1]);

                    } else {
                        partnmin[i] = 0;
                    }
                } else if (i == 0) {
                    partnmin[i] = Nmin_depth / runlayerdepth[0];
                }
            }
            i++;
        }

        // plant uptake loop 2: calculating N demand by plants and rest NO3_Pools
        while (j <= rootlayer) {
            upNO3_Pool = NO3_Pool[j];
            if (j == 0) {
                potN_up_z[j] = (runpotN_up / (1 - Math.exp(-runBeta_Ndist))) * (1 - Math.exp(-runBeta_Ndist * (runlayerdepth[j] / runrootdepth)));
                if (runlayerdepth[j] > runrootdepth) {
                    potN_up_z[j] = runpotN_up;
                }
                demand1 = upNO3_Pool - potN_up_z[j];
                uptake1 = potN_up_z[j];
            } else if (j > 0 && j < rootlayer) {
                potN_up_z[j] = ((runpotN_up / (1 - Math.exp(-runBeta_Ndist))) * (1 - Math.exp(-runBeta_Ndist * (runlayerdepth[j] / runrootdepth)))) - uptake1;
                demand1 = upNO3_Pool - potN_up_z[j];
                uptake1 = uptake1 + potN_up_z[j];

            } else if (j == rootlayer) {
                potN_up_z[j] = ((runpotN_up / (1 - Math.exp(-runBeta_Ndist))) * (1 - Math.exp(-runBeta_Ndist))) - uptake1;
                demand1 = (upNO3_Pool * partroot[j]) - potN_up_z[j];
                uptake1 = uptake1 + potN_up_z[j];
            }
            if (demand1 >= 0) {
                demandN_up_z[j] = 0;
                upNO3_Pool = upNO3_Pool - potN_up_z[j];
            } else {
                demandN_up_z[j] = upNO3_Pool - potN_up_z[j];
                upNO3_Pool = 0;
            }
            NO3_Poolvals1[j] = upNO3_Pool;
            j++;
        }

        // plant uptake loop 3: summarising rest N demand
        while (ii <= rootlayer) {
            demand2 = demandN_up_z[ii] + demand2;
            ii++;
        }
        
        double runactN_up = runpotN_up + demand2;
        double bioNact = 0;
        bioNact = BioNAct + runactN_up;
        actnup = runactN_up;
        BioNAct = bioNact;
        return NO3_Poolvals1;
    }

    private boolean calc_nit_volati(int i) {
        /*precalculations for nitrification and volatlisation */
        double eta_water = 0;
        double eta_temp = 0;
        double eta_volz = 0;
        double eta_nitri = 0;
        double eta_volati = 0;

        eta_temp = 0.41 * ((runSoil_Temp_Layer - 5) / 10);
        if (act_LPS + act_MPS < 0.25 * (sto_LPS + sto_MPS)) {
            eta_water = (act_LPS + act_MPS + sto_FPS) / (0.25 * (sto_LPS + sto_MPS + sto_FPS));
        } else if (act_LPS + act_MPS >= 0.25 * (sto_LPS + sto_MPS)) {
            eta_water = 1;
        }

        eta_volz = 1 - (runlayerdepth[i] / (runlayerdepth[i] + Math.exp(4.706 - (0.305 * runlayerdepth[i] / 20))));
        eta_nitri = eta_water * eta_temp;
        eta_volati = eta_temp * eta_volz;
        if (piadin == 1) {
            eta_nitri = (eta_nitri / 2000) * app_time;
        }

        N_nit_vol = runNH4_Pool * (1 - Math.exp(-eta_nitri - eta_volati));
        frac_nitr = 1 - Math.exp(-eta_nitri);
        frac_vol = 1 - Math.exp(-eta_volati);
        return true;
    }

    private double calc_Hum_trans() {
        double N_Hum_trans = runBeta_trans * (runN_activ_pool * ((1 / fr_actN) - 1) - runN_stabel_pool);
        return N_Hum_trans;
    }

    private double calc_Hum_act_min() {
        double N_Hum_act_min = runBeta_min * Math.sqrt(gamma_temp * gamma_water) * runN_activ_pool;
        return N_Hum_act_min;
    }

    private double calc_Res_N_trans() { /*is only allowed in the first layer */
        double epsilon_C_N = 0;
        double gamma_ntr = 0;
        /*double Res_N_trans = 0;
        /*calculation of the c/n ratio */
        epsilon_C_N = (runResidue_pool * 0.58) / (runN_residue_pool_fresh + runNO3_Pool);
        /*calculation of nutrient cycling residue composition factor*/
        gamma_ntr = Math.min(1, Math.exp(-0.693 * ((epsilon_C_N - 25) / 25)));
        /*calculation of the decay rate constant*/
        delta_ntr = runBeta_rsd * gamma_ntr * Math.sqrt(gamma_temp * gamma_water);

        /*Res_N_trans = delta_ntr * N_residue_pool_fresh;
        /*splitting in decomposition 20% and Minteralisation 80%  in run method*/
        return delta_ntr;
    }

    private double calc_nitrification() {
        double nitri_trans = 0;
        nitri_trans = (frac_nitr / (frac_nitr + frac_vol)) * N_nit_vol;
        return nitri_trans;
    }

    private double calc_voltalisation() {
        double volati_trans = 0;
        volati_trans = (frac_vol / (frac_nitr + frac_vol)) * N_nit_vol;
        return volati_trans;
    }

    private double calc_denitrification() {
        double denit_trans = 0;
        if (gamma_water > denitfac) {
            denit_trans = runNO3_Pool * (1 - Math.exp(-1.4 * gamma_temp * runC_org));
            denit_trans = Math.min(denit_trans, 1.0);
        } else if (gamma_water <= denitfac) {
            denit_trans = 0;
        }
        return denit_trans;
    }

    private double calc_nitrateupmove(int j) {
        double n_upmove = 0;
        double runaEvap = aEP_h[j];
        double sto_MPS = maxMPS_h[j];
        double sto_LPS = maxLPS_h[j];
        double sto_FPS = maxFPS_h[j];
        double act_LPS = satLPS_h[j] * sto_LPS;
        double act_MPS = satMPS_h[j] * sto_MPS;

        n_upmove = 0.1 * NO3_Poolvals[j] * (runaEvap / (act_LPS + act_MPS + sto_FPS));

        NO3_Poolvals[j] = NO3_Poolvals[j] - n_upmove;
        return n_upmove;
    }

    private double calc_concN_mobile(int i) {
//        double concN_mobile = 0;
        double concN_temp = 0;
        double mobilewater = 0;
        double soilstorage = 0;

        soilstorage = sto_LPS + sto_MPS + sto_FPS;
        if (i == 0) {
            mobilewater = (RD1_out_mm * runBeta_NO3) + RD2_out_mm + h_perco_mm + hor_by_infilt[i] + diffout[i] + 1.e-10;
        } else if (i > 0) {
            mobilewater = RD2_out_mm + h_perco_mm + hor_by_infilt[i] + diffout[i] + 1.e-10;
        }
        if (i == (layer - 1)) {
            mobilewater = RD2_out_mm + d_perco_mm + diffout[i] + 1.e-10;
        }
        concN_temp = (runNO3_Pool * (1 - Math.exp(-mobilewater / ((1 - theta_nit) * soilstorage))));
        concN_mobile = concN_temp / mobilewater;
        if (concN_mobile < 0) {
            concN_mobile = 0;
        }
        return concN_mobile;
    }

    private double calc_surfaceN() {
        double surfaceN = 0;
        surfaceN = runBeta_NO3 * RD1_out_mm * concN_mobile;  //SWAT orginal
        surfaceN = Math.min(surfaceN, runNO3_Pool);
        return surfaceN;
    }

    private double calc_interflowN(int i) {
        double interflowN = 0;
        interflowN = RD2_out_mm * concN_mobile;
        interflowN = Math.min(interflowN, runNO3_Pool);
        return interflowN;
    }

    private double calc_percoN(int i) {
        double percoN = 0;
        if (i < (layer - 1)) {
            percoN = (hor_by_infilt[i] + h_perco_mm) * concN_mobile;
        } else {
            percoN = d_perco_mm * concN_mobile;
        }
        percoN = Math.min(percoN, runNO3_Pool);
        return percoN;
    }

     public static void main(String[] args) {
        oms3.util.Components.explore(new J2KNSoilLayer());
    }
}
