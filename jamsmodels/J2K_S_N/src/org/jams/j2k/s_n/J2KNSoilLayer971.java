/*
 * J2KNSoilLayer.java
 * Created on 27. November 2005, 15:47
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, Manfred Fink
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package org.jams.j2k.s_n;

import jams.data.*;
import jams.model.*;
import jams.io.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author Manfred Fink
 */
@JAMSComponentDescription(
        title="J2KNSoilLayer971",
        author="Manfred Fink",
        description="Calculates Nitrogen transformation Processes in Soil. Method after SWAT2000"
        )
        public class J2KNSoilLayer971 extends JAMSComponent  {
    
    /*
     *  Component variables
     */
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute name area in m²"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " number of soil layers [-]"
            )
            public Attribute.Double Layer;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in cm depth of soil layer"
            )
            public Attribute.DoubleArray layerdepth;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in cm depth of soil profile"
            )
            public Attribute.Double totaldepth;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in m actual depth of roots"
            )
            public Attribute.Double rootdepth;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in kg/dm³ soil bulk density"
            )
            public Attribute.DoubleArray soil_bulk_density;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual LPS in portion of sto_LPS soil water content"
            )
            public Attribute.DoubleArray sat_LPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual MPS in portion of sto_MPS soil water content"
            )
            public Attribute.DoubleArray sat_MPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum MPS  in l soil water content"
            )
            public Attribute.DoubleArray stohru_MPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum LPS  in l soil water content"
            )
            public Attribute.DoubleArray stohru_LPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum FPS  in l soil water content"
            )
            public Attribute.DoubleArray stohru_FPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "soil temperature in layerdepth in °C"
            )
            public Attribute.DoubleArray Soil_Temp_Layer;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " in % organic Carbon in soil"
            )
            public Attribute.DoubleArray C_org;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " NO3-Pool in kgN/ha"
            )
            public Attribute.DoubleArray NO3_Pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " NH4-Pool in kgN/ha"
            )
            public Attribute.DoubleArray NH4_Pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " N-Organic Pool with reactive organic matter in kgN/ha"
            )
            public Attribute.DoubleArray N_activ_pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " N-Organic Pool with stable organic matter in kgN/ha"
            )
            public Attribute.DoubleArray N_stabel_pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = " sum of N-Organic Pool with reactive organic matter in kgN/ha"
            )
            public Attribute.Double sN_activ_pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = " sum of N-Organic Pool with stable organic matter in kgN/ha"
            )
            public Attribute.Double sN_stabel_pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "sum of NO3-Pool in kgN/ha"
            )
            public Attribute.Double sNO3_Pool;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = " sum of NH4-Pool in kgN/ha"
            )
            public Attribute.Double sNH4_Pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = " sum of NResiduePool in kgN/ha"
            )
            public Attribute.Double sNResiduePool;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = " sum of interflowNabs in kgN/ha",
            defaultValue="0"
            )
            public Attribute.Double sinterflowNabs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = " sum of interflowN in kgN/ha"
            )
            public Attribute.Double sinterflowN;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Residue in Layer in kgN/ha"
            )
            public Attribute.DoubleArray Residue_pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " N-Organic fresh Pool from Residue in kgN/ha"
            )
            public Attribute.DoubleArray N_residue_pool_fresh;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " actual evaporation in mm"
            )
            public Attribute.DoubleArray aEP_h;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "mps diffusion between layers value"
            )
            public Attribute.DoubleArray w_layer_diff;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " surface runoff in l"
            )
            public Attribute.Double RD1_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " interflow in l"
            )
            public Attribute.DoubleArray RD2_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " percolation in l"
            )
            public Attribute.Double D_perco ;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " voltalisation rate from NH4_Pool in kgN/ha"
            )
            public Attribute.Double Volati_trans;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " NH4 fertilizer rate in kgN/ha"
            )
            public Attribute.Double NH4inp;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " nitrification rate from  NO3_Pool in kgN/ha"
            )
            public Attribute.Double Nitri_trans;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " denitrification rate from  NO3_Pool in kgN/ha"
            )
            public Attribute.Double Denit_trans;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in surface runoff in  in kgN/ha"
            )
            public Attribute.Double SurfaceN;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in interflow in  in kgN/ha"
            )
            public Attribute.DoubleArray InterflowN;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in percolation in kgN/ha"
            )
            public Attribute.Double PercoN;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in surface runoff in kgN"
            )
            public Attribute.Double SurfaceNabs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in interflow in in kgN"
            )
            public Attribute.DoubleArray InterflowNabs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in percolation in in kgN"
            )
            public Attribute.Double PercoNabs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in surface runoff added to HRU layer in in kgN"
            )
            public Attribute.Double SurfaceN_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " Nitrate in interflow in added to HRU layer in kgN"
            )
            public Attribute.DoubleArray InterflowN_in;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "potential nitrogen content of plants in kgN/ha"
            )
            public Attribute.Double BioNoptAct;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual nitrate nitrogen content of plants in kgN/ha"
            )
            public Attribute.Double BioNAct;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual nitrate uptake of plants in kgN/ha"
            )
            public Attribute.Double actnup;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "intfiltration poritions for the single horizonts in l"
            )
            public Attribute.DoubleArray infiltration_hor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "percolation out ouf the single horizonts in l"
            )
            public Attribute.DoubleArray perco_hor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "percolation out ouf the single horizonts in l"
            )
            public Attribute.DoubleArray actETP_h;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = " Nitrate input due to Fertilisation in kgN/ha"
            )
            public Attribute.Double fertNO3;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " Ammonium input due to Fertilisation in kgN/ha"
            )
            public Attribute.Double fertNH4;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " Stable organig N input due to Fertilisation in kgN/ha"
            )
            public Attribute.Double fertstableorg;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " Activ organig N input due to Fertilisation in kgN/ha"
            )
            public Attribute.Double fertactivorg;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Sum of N input due fertilisation and deposition in kgN/ha"
            )
            public Attribute.Double sum_Ninput;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current organic fertilizer amount added to residue pool"
            )
            public Attribute.Double fertorgNfresh;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " Input of plant residues kg/ha"
            )
            public Attribute.Double inp_biomass;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Nitrogen input of plant residues in kgN/ha"
            )
            public Attribute.Double inpN_biomass;
    
    
    // constants and calibration parameter
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " rate constant between N_activ_pool and N_stabel_pool = 0.00001"
            )
            public Attribute.Double Beta_trans;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " rate factor between N_activ_pool and NO3_Pool to be calibrated 0.001 - 0.003"
            )
            public Attribute.Double Beta_min;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " rate factor between Residue_pool and NO3_Pool to be calibrated 0.1 - 0.02"
            )
            public Attribute.Double Beta_rsd;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " percolation coefitient to calibrate = 0.2"
            )
            public Attribute.Double Beta_NO3;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "nitrogen uptake distribution parameter to calibrate = 1 - 15"
            )
            public Attribute.Double Beta_Ndist;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "infiltration bypass parameter to calibrate = 0 - 1"
            )
            public Attribute.Double infil_conc_factor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "denitrfcation saturation factor normally at 0.95 to calibrate 0 - 1"
            )
            public Attribute.Double denitfac;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "concentration of Nitrate in rain = 0 - 0.05 kgN/(mm * ha)"
            )
            public Attribute.Double deposition_factor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "precipitation in mm"
            )
            public Attribute.Double precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
            )
            public Attribute.Calendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "indicates dormancy of plants"
            )
            public Attribute.Boolean dormancy;
    
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Indicates PIADIN application"
            )
            public Attribute.Integer piadin;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "time in days since the last PIADIN application"
            )
            public Attribute.Integer App_time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Indicates fertilazation optimization with plant demand"
            )
            public Attribute.Double opti;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Mineral nitrogen content in the soil profile down to 60 cm depth"
            )
            public Attribute.Double nmin;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Indicates whether roots can penetrate or not the soil layer [-]"
            )
            public Attribute.DoubleArray root_h; 
    
    
    
    
    
    /*
     *  Component run stages
     */
    
    
    
    
    
    private double gamma_temp;
    private double gamma_water;
    private double runarea;
    private double runSoil_Temp_Layer;
    private double[] runlayerdepth;
    private double runsoil_bulk_density;
    private double sto_MPS;
    private double sto_LPS;
    
    private double sto_FPS;
    private double act_LPS;
    private double act_MPS;
    
    private double runnetPrecip;
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
    
    
    
    private double runplantupN;
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
    private boolean precalc_nit_vol;
    
    private double theta_nit = 0.05; /*fraction of anion excluded soil water. depended from clay content min. 0.01  max. 1*/
    private double fr_actN = 0.02;      /** nitrogen active pool fraction. The fraction of organic nitrogen in the active pool. */
    private double N_nit_vol = 0;       /** NH4 that is converted to  NO3 Pool or volatilation . */
    private double frac_nitr = 0;       /** Fraction of N_nit_vol that is nitrification */
    private double frac_vol = 0;        /** Fraction of N_nit_vol that is volatilasation */
    private double Hum_trans; /*transformation rate from NOrg_acti_Pool to N_stabel_pool and back in kgN/ha */
    private double Hum_act_min; /*mirelaization rate from NOrg_acti_Pool to NO3_Pool in kgN/ha */
    private double runnitri_trans = 0; /*nitrifikation rate from NH4_Pool to NO3_Pool in kgN/ha*/
    private double delta_ntr = 0; /*residue decomposition factor */
    private double concN_mobile = 0; /*NO3 concentration of the mobile soil water in kgN/mm H2O*/
    
    private int datumjul = 0;
    private int app_time = 0;
    double[] hor_by_infilt;
    double[] NO3_Poolvals;
    double[] w_l_diff;
    double[] partnmin;
    double[] diffout;
    
    
    
    public void init() throws Attribute.Entity.NoSuchAttributeException{
        
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
/*         Attribute.Calendar testtime = new Attribute.Calendar();
       testtime.setValue("1993-10-12 07:30");
       if (time.equals(testtime)){
            System.out.println(time.getValue()) ;
        }*/
        
        
        int i = 0;
        
        this.gamma_temp = 0;
        this.gamma_water = 0;
        this.runarea = area.getValue();
        this.app_time = App_time.getValue();
        this.layer = (int)Layer.getValue();
        double runprecip  = precip.getValue();
        sumlayer = 0;
        double runsum_Ninput = 0;
        double sumNO3_Pool = 0;
        double sumNH4_Pool = 0;
        double suminterflowNabs = 0;
        double suminterflowN = 0;
        double sumN_residue_pool = 0;
        double sumN_activ_pool = 0;
        double sumN_stabel_pool = 0;
        double h_infilt_mm_sum = 0;
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
        double[] plantup_hor = new double[layer];
        double[] NO3balance = new double[layer];
        double[] NH4balance = new double[layer];
        double[] Nbalance = new double[layer];
        double[] NO3_Poolalt = new double[layer];
        double[] ConcN_mobile = new double[layer];
        
        this.runsurfaceN = 0;
        
        hor_by_infilt = new double[layer];
        diffout = new double[layer];
        partnmin = new double[layer];
        w_l_diff = new double[layer];
        i = 0;
        /*while (i < layer){
            NO3_Poolalt[i] = NO3_Pool.getValue()[i];
            i++;
        }*/
        NO3_Poolvals = calc_plantuptake();
//       NO3_Poolvals = NO3_Pool.getValue();
        i = 0;
        double sumplant = 0;
        double plantup_h = 0;
       /* while (i < layer){
        
            plantup_h = NO3_Poolalt[i] - NO3_Poolvals[i];
            sumplant = sumplant +  plantup_h;
            i++;
        
        }
        
        if (sumplant != actnup.getValue()){
            double plantdiff = sumplant - actnup.getValue();
            System.out.println("Pflanzenaufnahmefehler = " + plantdiff);
        }*/
        
        /*        calculation of infiltration water that bypasses the horizonts   loop */
        
        i = layer - 1;
        
        
        
        while (i  > 0) {
            
            this.h_infilt_mm = infiltration_hor.getValue()[i] / runarea;
            
            sumh_infilt_mm = sumh_infilt_mm + h_infilt_mm;
            
            hor_by_infilt[i - 1] = sumh_infilt_mm * infil_conc_factor.getValue();
/*
        if (i == 2 & hor_by_infilt[i] > 0){
            System.out.println();
        }*/
            
            i--;
        }
        
        // loops to distibute the layer diffusion water
        for (i = 0 ; i < layer; i++){
            
            diffout[i] = 0;
        }
        
        i = 0;
        
        for (i = 0 ; i < layer-1; i++){
            
            
            this.w_l_diff[i] = w_layer_diff.getValue()[i] / runarea;
            
            
            if (w_l_diff[i] > 0){
                diffout[i+1] = diffout[i+1] + w_l_diff[i];
            }else{
                diffout[i] =  diffout[i] - w_l_diff[i];
            }
            
            
        }
        
        i = 0;
        // horizont processies loop
        while (i < layer) {
            
            this.runSoil_Temp_Layer = Soil_Temp_Layer.getValue()[i];
            
            this.sto_MPS = stohru_MPS.getValue()[i] / runarea;
            this.sto_LPS = stohru_LPS.getValue()[i] / runarea;
            this.sto_FPS = stohru_FPS.getValue()[i] / runarea;
            
            
            this.act_LPS = sat_LPS.getValue()[i] * sto_LPS;
            this.act_MPS = sat_MPS.getValue()[i] * sto_MPS;
            
            
            this.runC_org = C_org.getValue()[i] / 1.72;
            this.runNO3_Pool = NO3_Poolvals[i];
            this.runNH4_Pool = NH4_Pool.getValue()[i];
            this.runN_activ_pool = N_activ_pool.getValue()[i];
            this.runN_stabel_pool = N_stabel_pool.getValue()[i];
            this.runN_residue_pool_fresh = N_residue_pool_fresh.getValue()[i];
            this.runResidue_pool = Residue_pool.getValue()[i];
            
            this.RD1_out_mm = RD1_out.getValue() / runarea;
            this.RD2_out_mm = RD2_out.getValue()[i] / runarea;
            this.d_perco_mm = D_perco.getValue() / runarea;
            this.h_perco_mm = perco_hor.getValue()[i] / runarea;
            
            
            this.runvolati_trans = 0;
            
            this.rundenit_trans = 0;
            
            this.runinterflowN = 0;
            this.runpercoN = 0;
            this.runsurfaceN_in = SurfaceN_in.getValue() * 10000 / runarea;
            this.runinterflowN_in = InterflowN_in.getValue()[i] * 10000 / runarea;
            SurfaceN_in.setValue(0);
            
            
            
            this.runBeta_trans = Beta_trans.getValue();
            this.runBeta_min = Beta_min.getValue();
            this.runBeta_rsd = Beta_rsd.getValue();
            this.runBeta_NO3 = Beta_NO3.getValue();
            
            
            if (fertNH4.getValue() > 0 && piadin.getValue() == 1){
                app_time = 0;
            }
            app_time++;
            
            /*          calculation of amount of nitrogen uptake with epaporation from soil */
            
            int j = 1;
            
            while (j < layer) {
                
                
                
                N_upmove_h = calc_nitrateupmove(j);
                sum_Nupmove = sum_Nupmove + N_upmove_h;
                
                j ++;
            }
            
            
            
            
            
            gamma_temp = 0.9 * (runSoil_Temp_Layer / (runSoil_Temp_Layer * Math.exp(9.93 - 0.312 * runSoil_Temp_Layer))) + 0.1;
            
            if (sto_LPS + sto_MPS + sto_FPS > 0){
                gamma_water = (act_LPS + act_MPS + sto_FPS)/(sto_LPS + sto_MPS + sto_FPS);
            } else{
                gamma_water = 0;
            }
            
            if (runSoil_Temp_Layer > 5){
            
            precalc_nit_vol = calc_nit_volati(i);
            
            runvolati_trans = calc_voltalisation();
            
            runnitri_trans = calc_nitrification();
            
            Hum_trans = calc_Hum_trans();
            
            }else{
            runvolati_trans = 0;
            runnitri_trans = 0;
            }
            
            
            
            /*Calculations of NPools   Check Order of calculations !!!!!!!!!!!!!!*/
            
            runNH4_Pool = runNH4_Pool - (runvolati_trans + runnitri_trans);
            
            if (runNH4_Pool < 0){
                runNH4_Pool = 0;
            }
            
            
            runN_stabel_pool = runN_stabel_pool + Hum_trans;
            
            if (runN_stabel_pool < 0){
                runN_stabel_pool = 0;
            }
            
            runN_activ_pool = runN_activ_pool - Hum_trans;
            
            
            if (runN_activ_pool < 0){
                runN_activ_pool = 0;
            }
            
            Hum_act_min = calc_Hum_act_min();
            
            runN_activ_pool = runN_activ_pool - Hum_act_min;
            
            if (runN_activ_pool < 0){
                runN_activ_pool = 0;
            }
            
            if (i < 1){
                runResidue_pool = runResidue_pool + inp_biomass.getValue() + (fertorgNfresh.getValue() * 10);
                runN_residue_pool_fresh = runN_residue_pool_fresh + inpN_biomass.getValue() + fertorgNfresh.getValue();
/*                if (inpN_biomass.getValue() > 0){
                    System.out.println(time.get(time.DAY_OF_YEAR) + " resisuenadd " + inpN_biomass.getValue());
                }*/
                
                
                
                runNH4_Pool = runNH4_Pool + fertNH4.getValue();
                delta_ntr = this.calc_Res_N_trans();
                a_deposition = deposition_factor.getValue() * runprecip;
                
                
                runResidue_pool = runResidue_pool - (delta_ntr * runResidue_pool);
                
                if (runResidue_pool < 0){
                    runResidue_pool = 0;
                }
                
                runsum_Ninput =  fertactivorg.getValue() + fertNH4.getValue() + fertNO3.getValue() + fertorgNfresh.getValue() + a_deposition;
                //runsum_Ninput =   runinterflowN_in ;
                
                
                
                runN_stabel_pool = runN_stabel_pool + fertstableorg.getValue();
                
                Nactiverespool = 0.2 * (delta_ntr * runN_residue_pool_fresh);
                runN_activ_pool = runN_activ_pool + fertactivorg.getValue() + Nactiverespool;
                
                if (runN_activ_pool < 0){
                    runN_activ_pool = 0;
                }
                NO3respool = 0.8 * (delta_ntr * runN_residue_pool_fresh);
                runNO3_Pool = runNO3_Pool + sum_Nupmove + fertNO3.getValue() + a_deposition + runnitri_trans + Hum_act_min +  runinterflowN_in + runsurfaceN_in + NO3respool;
                
                
//                System.out.println(time.get(time.DAY_OF_YEAR) + " runNO3_Pool " + runNO3_Pool + " sum_Nupmove "+ sum_Nupmove + " fertNO3 "+ fertNO3.getValue() + " a_deposition "+ a_deposition + " runnitri_trans "+ runnitri_trans +" runinterflowN_in "+ runinterflowN_in +" runsurfaceN_in "+ runsurfaceN_in +" NO3respool "+ NO3respool);
                
/*                if (runNO3_Pool > runplantupN){
 
                    runNO3_Pool = runNO3_Pool - runplantupN;
 
                }else if (runNO3_Pool <= runplantupN){
 
                    runplantupN = runNO3_Pool;
                    runNO3_Pool = 0;
 
 
                } */
                
                rundenit_trans = calc_denitrification();
                runNO3_Pool = runNO3_Pool -  rundenit_trans;
                
                if (runNO3_Pool < 0){
                    runNO3_Pool = 0;
                }
                
                runN_residue_pool_fresh = runN_residue_pool_fresh - (delta_ntr * runN_residue_pool_fresh);
                
                if (runN_residue_pool_fresh < 0){
                    runN_residue_pool_fresh = 0;
                }
                
            } else {
                
                //runsum_Ninput = runsum_Ninput + runinterflowN_in;
                
                runNO3_Pool = runNO3_Pool + runnitri_trans + runinterflowN_in + percoNvals[i-1] + Hum_act_min;
                
/*                if (runNO3_Pool > runplantupN){
 
                    runNO3_Pool = runNO3_Pool - runplantupN;
 
                }else {
 
                    runplantupN = runNO3_Pool;
                    runNO3_Pool = 0;
 
 
                }*/
                
                rundenit_trans = calc_denitrification();
                runNO3_Pool = runNO3_Pool -  rundenit_trans;
                
                
                if (runNO3_Pool < 0){
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
            
            
            
            
            
            
            
            
            if (runNO3_Pool < 0){
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
            
            if (i < 5){
            sumNO3_Pool = runNO3_Pool + sumNO3_Pool;
            }
            suminterflowNabs = runinterflowNabs + suminterflowNabs;
            suminterflowN = runinterflowN + suminterflowN;
            Sumvolati_trans = Sumvolati_trans + runvolati_trans;
            Sumdenit_trans = Sumdenit_trans + rundenit_trans;
            Sumnitri_trans = Sumnitri_trans + runnitri_trans;
            
/*             double NH4test1 = NH4_Poolvals[0];
            double NH4test2 = NH4_Pool.getValue()[0];
           if (NH4test1 > NH4test2){
                System.out.println("Wundersame NH4 vermehrung");
            }*/
            
/*            if (i == 0){
 
                NO3balance[i] = NO3_Poolalt[i] + a_deposition + runsurfaceN_in + runnitri_trans + Hum_act_min + sum_Nupmove + runinterflowN_in + fertNO3.getValue() + NO3respool
                        - (runNO3_Pool + plantup_hor[i] + rundenit_trans + runsurfaceN + percoNvals[i] + runinterflowN);
 
                NH4balance[i] = NH4_Pool.getValue()[i] + fertNH4.getValue()
                - (runNH4_Pool + runvolati_trans + runnitri_trans);
 
                Nbalance[i] = NO3_Poolalt[i] + a_deposition + runsurfaceN_in + runnitri_trans + Hum_act_min + sum_Nupmove + runinterflowN_in + fertNO3.getValue() + NO3respool +
                        + NH4_Pool.getValue()[i] + fertNH4.getValue()
                        + N_stabel_pool.getValue()[i] + N_activ_pool.getValue()[i] + N_residue_pool_fresh.getValue()[i] + fertactivorg.getValue() + fertstableorg.getValue() + Nactiverespool
                        - (runNO3_Pool + plantup_hor[i] + rundenit_trans + runsurfaceN + percoNvals[i] + runinterflowN
                        + runNH4_Pool + runvolati_trans + runnitri_trans +
                        runN_activ_pool + runN_stabel_pool + runN_residue_pool_fresh);
 
            }else{
 
                NO3balance[i] = NO3_Poolalt[i]  + runinterflowN_in + percoNvals[i-1] + runnitri_trans + Hum_act_min
                        - (runNO3_Pool + plantup_hor[i] + N_upmove_h[i] + rundenit_trans + percoNvals[i] + runinterflowN);
 
            }
 
            if (NO3balance[i] < -0.00001 || NO3balance[i] > 0.00001){
                String zeit = new String();
                zeit = time.toString();
                System.out.println(zeit +  " Horizont = "  + i + " NO3 Balance " + NO3balance[i]);
 
            }
 
            if (NH4balance[i] < -0.00001 || NH4balance[i] > 0.00001){
                String zeit = new String();
                zeit = time.toString();
                System.out.println(zeit +  " Horizont = "  + i + " NH4 Balance " + NH4balance[i]);
 
            }
 
            if (Nbalance[i] < -0.00001 || Nbalance[i] > 0.00001){
                String zeit = new String();
                zeit = time.toString();
                System.out.println(zeit +  " Horizont = "  + i + " N Balance " + Nbalance[i]);
 
            }*/
            
            
            i++;
        }
        i = 0;
        
        // distribution of diffusion N into the
        for (i = 0; i < layer - 1; i++){
            
            if (w_l_diff[i] < 0){
                diffoutN = w_l_diff[i] * ConcN_mobile[i];
                NO3_Poolvals[i] = NO3_Poolvals[i] + diffoutN;
                NO3_Poolvals[i+1] = NO3_Poolvals[i+1] - diffoutN;
            }else{
                diffoutN = w_l_diff[i] * ConcN_mobile[i+1];
                NO3_Poolvals[i]  = NO3_Poolvals[i] + diffoutN;
                NO3_Poolvals[i+1] = NO3_Poolvals[i+1] - diffoutN;
                
            }
            
            if (opti.getValue() == 1){
            runnmin = (((NO3_Poolvals[i] + NH4_Poolvals[i]) * this.partnmin[i])) + runnmin;
            }
        }
        // writing of pools
        double[] zerosetter = new double[layer];
   
        NO3_Pool.setValue(NO3_Poolvals);
        NH4_Pool.setValue(NH4_Poolvals);
        N_activ_pool.setValue(N_activ_poolvals);
        N_stabel_pool.setValue(N_stabel_poolvals);
        N_residue_pool_fresh.setValue(N_residue_pool_freshvals);
        Residue_pool.setValue(Residue_poolvals);
        // writing of fluxes
        InterflowN.setValue(interflowNvals);
        InterflowNabs.setValue(interflowNabsvals);
        PercoN.setValue(percoNvals[layer -1]);
        PercoNabs.setValue(percoNabsvals[layer -1]);
        SurfaceN.setValue(runsurfaceN);
        runsurfaceNabs = runsurfaceN * runarea / 10000;
        SurfaceNabs.setValue(runsurfaceNabs);
        sum_Ninput.setValue(runsum_Ninput);
        sinterflowNabs.setValue(suminterflowNabs);
        sinterflowN.setValue(suminterflowN);
        // writing of transfomations time
        Volati_trans.setValue(Sumvolati_trans);
        Denit_trans.setValue(Sumdenit_trans);
        Nitri_trans.setValue(Sumnitri_trans);
        sN_stabel_pool.setValue(sumN_stabel_pool);
        sN_activ_pool.setValue(sumN_activ_pool);
        sNH4_Pool.setValue(sumNH4_Pool);
        sNO3_Pool.setValue(sumNO3_Pool);
        sNResiduePool.setValue(sumN_residue_pool);
        App_time.setValue(app_time);
        InterflowN_in.setValue(zerosetter);
        nmin.setValue(runnmin);
        
        
//        System.out.println("percoN = " + percoN +" percoNabs =  "+ percoNabs);
        
        
        
    }
    private double[] calc_plantuptake(){
        double upNO3_Pool = 0;
        double runrootdepth =(rootdepth.getValue() * 100);
        double[] partroot = new double[layer];
        
        
        if (BioNoptAct.getValue() == 0){
            BioNAct.setValue(0);
        }
        double runpotN_up = BioNoptAct.getValue() - BioNAct.getValue();
        
        if (dormancy.getValue()){
            runpotN_up = 0;
        }
        
        
        if (runpotN_up < 0){
            runpotN_up  = 0;
        }
        
//                runpotN_up = 0.3;
        double[] NO3_Poolvals1 = new double[layer];
        double[] potN_up_z = new double[layer];
        double[] demandN_up_z = new double[layer];
        double rootlayer = 0;
        double runBeta_Ndist = Beta_Ndist.getValue();
        double demand3 = 0;
        double demand2 = 0;
        double demand1 = 0;
        double uptake1 = 0;
        int ii = 0;
        int jj = 0;
        int j = 0;
        int i = 0;
        
        NO3_Poolvals1 = NO3_Pool.getValue();
        
        
        
        // plant uptake loop 1: calculating layer poritions within rootdepth
        while (i < layer) {
            
            sumlayer =   sumlayer  +  layerdepth.getValue()[i];
            this.runlayerdepth[i] = sumlayer;
            if (runrootdepth > runlayerdepth[0]){
                if (runrootdepth > runlayerdepth[i]){
                    partroot[i] = 1 ;
                    rootlayer = i;
                }else if (runrootdepth > runlayerdepth[i - 1]){
                    partroot[i] = (runrootdepth - runlayerdepth[i - 1]) /  (runlayerdepth[i] - runlayerdepth[i - 1]);
                    rootlayer = i;
                }else {
                    partroot[i] = 0;
                }
            }else if (i == 0){
                partroot[i] = runrootdepth /  runlayerdepth[0];
                rootlayer = i;
            }
            
            if (opti.getValue() == 1){
             double Nmin_depth = 60;
             if (Nmin_depth > runlayerdepth[0]){
                if (Nmin_depth > runlayerdepth[i]){
                    partnmin[i] = 1 ;
                   
                }else if (Nmin_depth > runlayerdepth[i - 1]){
                    partnmin[i] = (Nmin_depth - runlayerdepth[i - 1]) /  (runlayerdepth[i] - runlayerdepth[i - 1]);
                    
                }else {
                    partnmin[i] = 0;
                }
            }else if (i == 0){
                partnmin[i] = Nmin_depth /  runlayerdepth[0];
                
             
             
            }
           
            
            }
            i++;
            
            
        }
        
        // plant uptake loop 2: calculating N demand by plants and rest NO3_Pools
        while (j <= rootlayer) {
            upNO3_Pool = NO3_Pool.getValue()[j];
            
            if (j == 0){
                potN_up_z[j] = (runpotN_up /(1 - Math.exp(-runBeta_Ndist)))*(1 - Math.exp(-runBeta_Ndist * (runlayerdepth[j] / runrootdepth)));
                if (runlayerdepth[j] > runrootdepth){
                    potN_up_z[j] = runpotN_up;
                }
                demand1 = upNO3_Pool - potN_up_z[j];
                
                uptake1 = potN_up_z[j];
                
            } else if (j > 0 && j < rootlayer){
                
                potN_up_z[j] = ((runpotN_up /(1 - Math.exp(-runBeta_Ndist)))*(1 - Math.exp(-runBeta_Ndist * (runlayerdepth[j] / runrootdepth)))) - uptake1;
                
                demand1 = upNO3_Pool - potN_up_z[j];
                uptake1 = uptake1 + potN_up_z[j];
                
            } else if (j == rootlayer){
                potN_up_z[j] = ((runpotN_up /(1 - Math.exp(-runBeta_Ndist)))*(1 - Math.exp(-runBeta_Ndist))) - uptake1;
                demand1 = (upNO3_Pool * partroot[j]) - potN_up_z[j];
                uptake1 = uptake1 + potN_up_z[j];
            /*
                if (uptake1 == runpotN_up){
                    System.out.println("good");
                }else{
                    System.out.println("bad");
                }
             */
            }
            
            
            
            if (demand1 >= 0){
                
                demandN_up_z[j] = 0;
                
                upNO3_Pool = upNO3_Pool - potN_up_z[j];
                
            }
            
            else{
                
                /*demandN_up_z[j] = upNO3_Pool - potN_up_z[j];
                
                upNO3_Pool = 0;*/

                demandN_up_z[j] = demand1;

                upNO3_Pool = upNO3_Pool - (upNO3_Pool * partroot[j]);
                
            }
            
            NO3_Poolvals1[j] = upNO3_Pool;
            
            j++;
        }
        
        // plant uptake loop 3: summarising rest N demand
        while (ii <= rootlayer) {
            
            demand2 = demandN_up_z[ii] + demand2;
            
            ii++;
        }
        /* switch off of loop 4
        if (demand2 < 0){
            
            // plant uptake loop 4: redistributing rest N demand on rest NO3_Pools within rootdepth
            while (jj < rootlayer) {
                demand3 = demand2;
                
                demand3 = demand3 + NO3_Poolvals1[jj];
                
                NO3_Poolvals1[jj]  = NO3_Poolvals1[jj] + demand2;
                
                if (NO3_Poolvals1[jj] < 0){
                    NO3_Poolvals1[jj] = 0;
                }
                if (demand3 < 0){
                    
                    demand2 = demand3;
                    
                } else{
                    
                    demand2 = 0;
                }
                
                jj++;
            }
        }
        */
        double runactN_up = runpotN_up + demand2;
        
        double bioNact = 0;
        //double nuptake = actnup.getValue();
        bioNact = BioNAct.getValue() + runactN_up;
        //nuptake = nuptake + runactN_up;
        actnup.setValue(runactN_up);
//        System.out.println("runactN_up = " + nuptake);
//        if (runpotN_up > runactN_up){
//        System.out.println("runpotN_up = " + runpotN_up + " runactN_up = " + runactN_up);
//        }
        BioNAct.setValue(bioNact);
        
        
        return NO3_Poolvals1;
    }
    private boolean calc_nit_volati(int i){/*precalculations for nitrification and volatlisation */
        double eta_water = 0;
        double eta_temp = 0;
        double eta_volz = 0;
        double eta_nitri = 0;
        double eta_volati = 0;
        
        eta_temp = 0.41 * ((runSoil_Temp_Layer - 5) / 10);
        
        if (act_LPS + act_MPS < 0.25 * (sto_LPS + sto_MPS )){
            eta_water = (act_LPS + act_MPS + sto_FPS ) / (0.25 * (sto_LPS + sto_MPS + sto_FPS));
        } else if (act_LPS + act_MPS >= 0.25 * (sto_LPS + sto_MPS)) {
            eta_water = 1;
        }
        
        eta_volz = 1 -(runlayerdepth[i] / (runlayerdepth[i] + Math.exp(4.706 - (0.305 * runlayerdepth[i]/20))));
        
        eta_nitri = eta_water * eta_temp;
        
        eta_volati = eta_temp * eta_volz;
        
        if (piadin.getValue() == 1){
            
            eta_nitri = (eta_nitri / 2000) * app_time;
            
        }
        
        //eta_volati = 0;
        
        this.N_nit_vol = runNH4_Pool * (1 - Math.exp(- eta_nitri -eta_volati));
        
        this.frac_nitr = 1 - Math.exp(- eta_nitri);
        
        this.frac_vol = 1 - Math.exp(- eta_volati);
        
        return true;
    }
    
    private double calc_Hum_trans(){
        double N_Hum_trans = 0;
        
        
        N_Hum_trans = runBeta_trans * (runN_activ_pool * ((1 / fr_actN) -1) - runN_stabel_pool);
        
        return N_Hum_trans;
    }
    
    private double calc_Hum_act_min(){
        double N_Hum_act_min = 0;
        
        
        N_Hum_act_min = runBeta_min * Math.sqrt(gamma_temp * gamma_water) * runN_activ_pool;
        
        return N_Hum_act_min;
    }
    
    private double calc_Res_N_trans(){ /*is only allowed in the first layer */
        double epsilon_C_N = 0;
        double gamma_ntr = 0;
        /*double Res_N_trans = 0;
        /*calculation of the c/n ratio */
        epsilon_C_N = (runResidue_pool * 0.58)/(runN_residue_pool_fresh + runNO3_Pool);
        /*calculation of nutrient cycling residue composition factor*/
        gamma_ntr = Math.min(1,Math.exp(-0.693*((epsilon_C_N - 25)/ 25)));
        /*calculation of the decay rate constant*/
        this.delta_ntr = runBeta_rsd * gamma_ntr * Math.sqrt(gamma_temp * gamma_water);
        
        /*Res_N_trans = delta_ntr * N_residue_pool_fresh;
        /*splitting in decomposition 20% and Minteralisation 80%  in run method*/
        return delta_ntr;
    }
    
    private double calc_nitrification(){
        double nitri_trans = 0;
        
        
            nitri_trans =  (frac_nitr /(frac_nitr + frac_vol)) * N_nit_vol;
        
        return nitri_trans;
        
    }
    
    private double calc_voltalisation(){
        double volati_trans = 0;
        
        
            volati_trans =  (frac_vol /(frac_nitr + frac_vol)) * N_nit_vol;
        
        return volati_trans;
    }
    
    private double calc_denitrification(){
        double denit_trans = 0;
        
        if (gamma_water > denitfac.getValue()){
            denit_trans = runNO3_Pool * (1 - Math.exp(-1.4 * gamma_temp * runC_org));
            denit_trans = Math.min(denit_trans,1.0);
        } else if (gamma_water <= denitfac.getValue()){
            denit_trans = 0;
            
        }
        return denit_trans;
    }
    
    private double calc_nitrateupmove(int j){
        double n_upmove = 0;
        double runaEvap = aEP_h.getValue()[j] ;
        double sto_MPS = stohru_MPS.getValue()[j];
        double sto_LPS = stohru_LPS.getValue()[j];
        double sto_FPS = stohru_FPS.getValue()[j];
        double act_LPS = sat_LPS.getValue()[j] * sto_LPS;
        double act_MPS = sat_MPS.getValue()[j] * sto_MPS;
        
        
        
        n_upmove = 0.1 * NO3_Poolvals[j] * (runaEvap / (act_LPS + act_MPS + sto_FPS));
        
        NO3_Poolvals[j] = NO3_Poolvals[j] - n_upmove;
        
        return n_upmove;
    }
    
    private double calc_concN_mobile(int i){
        double  concN_mobile = 0;
        double concN_temp = 0;
        double  mobilewater = 0;
        double  soilstorage = 0;
        
        soilstorage = act_LPS + act_MPS + sto_FPS;
        if (i == 0) {
            mobilewater = (RD1_out_mm * runBeta_NO3) + RD2_out_mm + h_perco_mm + hor_by_infilt[i] + diffout[i] + 1.e-10;
        }
        
        else if (i > 0) {
            mobilewater = RD2_out_mm + h_perco_mm + hor_by_infilt[i] + diffout[i] + 1.e-10;
        }
        if (i == (layer -1)){
            mobilewater = RD2_out_mm + d_perco_mm  + diffout[i] + 1.e-10;
        }
        concN_temp = (runNO3_Pool * (1 - Math.exp(- mobilewater / ((1 - theta_nit) * soilstorage))));
        concN_mobile = concN_temp / mobilewater;
        
        if (concN_mobile < 0){
            concN_mobile = 0;
        }
        return concN_mobile;
    }
    
    
    private double calc_concN_mobile2(){ //simpler Version for tests
        double  concN_mobile = 0;
        double concN_temp = 0;
        double  mobilewater = 0;
        double  soilstorage = 0;
        
        soilstorage = act_LPS + act_MPS + sto_FPS;
        if (layer == 0) {
            mobilewater = RD1_out_mm + RD2_out_mm + d_perco_mm + 1.e-10;
        }
        
        else if (layer > 0) {
            mobilewater = RD2_out_mm + d_perco_mm + 1.e-10;
        }
        concN_mobile = (runNO3_Pool / (mobilewater + ((1 - theta_nit) * soilstorage)));
        
        
        if (concN_mobile < 0){
            concN_mobile = 0;
        }
        return concN_mobile;
    }
    
    private double calc_surfaceN(){
        double surfaceN = 0;
        
        surfaceN = runBeta_NO3 * RD1_out_mm * concN_mobile;  //SWAT orginal
//        surfaceN = RD1_out_mm * concN_mobile;
        surfaceN = Math.min(surfaceN,runNO3_Pool);
        
        return surfaceN;
    }
    
    private double calc_interflowN(int i){
        double interflowN = 0;
        interflowN = RD2_out_mm * concN_mobile;
        interflowN = Math.min(interflowN,runNO3_Pool);
       /* if (i == 0) {
            interflowN = (1.0 - runBeta_NO3) * RD2_out_mm * concN_mobile;
            interflowN = Math.min(interflowN,runNO3_Pool);
        } else if (i > 0) {
            interflowN = RD2_out_mm * concN_mobile;
            interflowN = Math.min(interflowN,runNO3_Pool);
        }
        if (interflowN < 0){
            System.out.println(RD2_out_mm + " = RD2_out_mm " + interflowN +" = interflowN");
        }*/
        return interflowN;
        
    }
    
    private double calc_percoN(int i){
        double percoN = 0;
        if (i < (layer -1)){
            percoN = (hor_by_infilt[i] + h_perco_mm) * concN_mobile;
        }else{
            percoN = d_perco_mm  * concN_mobile;
        }
        percoN = Math.min(percoN,runNO3_Pool);
        return percoN;
    }
    
    
    
    public void cleanup() throws Attribute.Entity.NoSuchAttributeException{
        
    }
}


/*
                        <component class="org.jams.j2k.s_n.J2KNSoil" name="J2KNSoil">
                                <jamsvar name="area" provider="HRUContext" providervar="currentEntity.area"/>
                                <jamsvar name="LAIArray" provider="HRUContext" providervar="currentEntity.LAIArray"/>
                                <jamsvar name="stohru_MPS" provider="HRUContext" providervar="currentEntity.maxMPS"/>
                                <jamsvar name="stohru_LPS" provider="HRUContext" providervar="currentEntity.maxLPS"/>
                                <jamsvar name="sat_MPS" provider="HRUContext" providervar="currentEntity.satMPS"/>
                                <jamsvar name="sat_LPS" provider="HRUContext" providervar="currentEntity.satLPS"/>
                                <jamsvar name="aEvap" provider="HRUContext" providervar="currentEntity.aEvap"/>
                                <jamsvar name="RD1_out" provider="HRUContext" providervar="currentEntity.outRD1"/>
                                <jamsvar name="RD2_out" provider="HRUContext" providervar="currentEntity.outRD2"/>
                                <jamsvar name="D_perco" provider="HRUContext" providervar="currentEntity.percolation"/>
                                <jamsvar name="NO3_Pool" provider="HRUContext" providervar="currentEntity.NO3_Pool"/>
                                <jamsvar name="NH4_Pool" provider="HRUContext" providervar="currentEntity.NH4_Pool"/>
                                <jamsvar name="N_activ_pool" provider="HRUContext" providervar="currentEntity.N_activ_pool"/>
                                <jamsvar name="N_stabel_pool" provider="HRUContext" providervar="currentEntity.N_stabel_pool"/>
                                <jamsvar name="Residue_pool" provider="HRUContext" providervar="currentEntity.residue_pool"/>
                                <jamsvar name="N_residue_pool_fresh" provider="HRUContext" providervar="currentEntity.N_residue_pool_fresh"/>
                                <jamsvar name="Volati_trans" provider="HRUContext" providervar="currentEntity.Volati_rate"/>
                                <jamsvar name="NH4inp" provider="HRUContext" providervar="currentEntity.NH4inp"/>
                                <jamsvar name="PlantupN" provider="HRUContext" providervar="currentEntity.PlantupN"/>
                                <jamsvar name="Nitri_trans" provider="HRUContext" providervar="currentEntity.Nitri_rate"/>
                                <jamsvar name="Denit_trans" provider="HRUContext" providervar="currentEntity.Denit_rate"/>
                                <jamsvar name="SurfaceN" provider="HRUContext" providervar="currentEntity.SurfaceN"/>
                                <jamsvar name="InterflowN" provider="HRUContext" providervar="currentEntity.InterflowN"/>
                                <jamsvar name="PercoN" provider="HRUContext" providervar="currentEntity.PercoN"/>
                                <jamsvar name="SurfaceNabs" provider="HRUContext" providervar="currentEntity.SurfaceNabs"/>
                                <jamsvar name="InterflowNabs" provider="HRUContext" providervar="currentEntity.InterflowNabs"/>
                                <jamsvar name="PercoNabs" provider="HRUContext" providervar="currentEntity.PercoNabs"/>
                                <jamsvar name="SurfaceN_in" provider="HRUContext" providervar="currentEntity.SurfaceN_in"/>
                                <jamsvar name="InterflowN_in" provider="HRUContext" providervar="currentEntity.InterflowN_in"/>
                                <jamsvar name="PercoN_in" provider="HRUContext" providervar="currentEntity.PercoN_in"/>
                                <jamsvar name="Soil_Temp_Layer" provider="HRUContext" providervar="currentEntity.Soil_Temp_Layer"/>
 
                                <jamsvar name="layerdepth" provider="HRUContext" providervar="currentEntity.rootDepth"/>
                                <jamsvar name="totaldepth" provider="HRUContext" providervar="currentEntity.rootDepth"/>
                                <jamsvar name="NetPrecip" provider="HRUContext" providervar="currentEntity.rain"/>
                                <jamsvar name="aTransp" provider="HRUContext" providervar="currentEntity.aTransp"/>
                                <jamsvar name="time" provider="TemporalContext" providervar="current"/>
 
                                <jamsvar name="soil_bulk_density" value="1.3"/>
                                <jamsvar name="C_org" value="1.5"/>
                                <jamsvar name="Beta_trans" value="0.00001"/>
                                <jamsvar name="Beta_min" value="0.002"/>
                                <jamsvar name="Beta_rsd" value="0.05"/>
                                <jamsvar name="Beta_NO3" value="0.05"/>
                        </component>
 
 
 */
