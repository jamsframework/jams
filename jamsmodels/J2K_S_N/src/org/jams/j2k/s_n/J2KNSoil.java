/*
 * J2KNSoil.java
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

/**
 *
 * @author Manfred Fink
 */
@JAMSComponentDescription(
        title="J2KNSoil",
        author="Manfred Fink",
        description="Calculates Nitrogen transformation Processes in Soil. Method after SWAT2000"
        )
        public class J2KNSoil extends JAMSComponent  {
    
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
            description = "in mm depth of soil layer"
            )
            public Attribute.Double layerdepth;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in mm depth of soil profile"
            )
            public Attribute.Double totaldepth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in kg/dm³ soil bulk density"
            )
            public Attribute.Double soil_bulk_density;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual LPS in portion of sto_LPS soil water content"
            )
            public Attribute.Double sat_LPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual MPS in portion of sto_MPS soil water content"
            )
            public Attribute.Double sat_MPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum MPS  in l soil water content"
            )
            public Attribute.Double stohru_MPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum LPS  in l soil water content"
            )
            public Attribute.Double stohru_LPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum FPS  in l soil water content"
            )
            public Attribute.Double stohru_FPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "soil temperature in layerdepth in °C"
            )
            public Attribute.Double Soil_Temp_Layer;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Array of state variables LAI "
            )
            public Attribute.DoubleArray LAIArray;   //only for testing

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " in % organic Carbon in soil"
            )
            public Attribute.Double C_org;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " NO3-Pool in kgN/ha"
            )
            public Attribute.Double NO3_Pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " NH4-Pool in kgN/ha"
            )
            public Attribute.Double NH4_Pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " N-Organic Pool with reactive organic matter in kgN/ha"
            )
            public Attribute.Double N_activ_pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " N-Organic Pool with stable organic matter in kgN/ha"
            )
            public Attribute.Double N_stabel_pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Residue in Layer in kgN/ha"
            )
            public Attribute.Double Residue_pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " N-Organic fresh Pool from Residue in kgN/ha"
            )
            public Attribute.Double N_residue_pool_fresh;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " actual evaporation in mm"
            )
            public Attribute.Double aEvap;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " surface runoff in l"
            )
            public Attribute.Double RD1_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " interflow in l"
            )
            public Attribute.Double RD2_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " percolation in l"
            )
            public Attribute.Double D_perco;
/*
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " number of soil layer"
            )
            public Attribute.Double Layer;
 */
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
            description = " plantuptake rate in kgN/ha"
            )
            public Attribute.Double PlantupN;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
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
            public Attribute.Double InterflowN;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in percolation in  in kgN/ha"
            )
            public Attribute.Double PercoN;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in surface runoff in  in kgN"
            )
            public Attribute.Double SurfaceNabs;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in interflow in  in kgN"
            )
            public Attribute.Double InterflowNabs;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in percolation in  in kgN"
            )
            public Attribute.Double PercoNabs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " Nitrate in surface runoff added to HRU layer in in kgN"
            )
            public Attribute.Double SurfaceN_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " Nitrate in interflow in added to HRU layer in kgN"
            )
            public Attribute.Double InterflowN_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " Nitrate in percolation in added to HRU layer in kgN"
            )
            public Attribute.Double PercoN_in;
    
    
    
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
    
    /* addtional variable for test run*/
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " net precipitation in mm"
            )
            public Attribute.Double NetPrecip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " actual Transpiration in mm"
            )
            public Attribute.Double aTransp;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
            )
            public Attribute.Calendar time;
    
    
    /*
     *  Component run stages
     */
    
    
    private double gamma_temp;
    private double gamma_water;
    private double runarea;
    private double runSoil_Temp_Layer;
    private double runlayerdepth;
    private double runsoil_bulk_density;
    private double sto_MPS;
    private double sto_LPS;
    private double sto_FPS;
    
    private double act_LPS;
    private double act_MPS;
    
    private double runnetPrecip;
    private double runLAI;
    private double runC_org;
    private double runNO3_Pool;
    private double runNH4_Pool;
    private double runN_activ_pool;
    private double runN_stabel_pool;
    private double runN_residue_pool_fresh;
    private double runResidue_pool;
    private double runaEvap;
    private double RD1_out_mm;
    private double RD2_out_mm;
    private double d_perco_mm;
    private int layer;
    private double runvolati_trans;
    private double runNH4inp;
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
    private double runpercoN_in;
    
    private double runBeta_trans;
    private double runBeta_min;
    private double runBeta_rsd;
    private double runBeta_NO3;
    private double runaTransp;
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
    
    public void init() throws Attribute.Entity.NoSuchAttributeException{
        
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        int day = time.get(Attribute.Calendar.DAY_OF_YEAR) - 1;
        int i = 1;
        this.gamma_temp = 0;
        this.gamma_water = 0;
        this.runarea = area.getValue();
        this.runSoil_Temp_Layer = Soil_Temp_Layer.getValue();
        this.runlayerdepth = layerdepth.getValue() * 100;
        this.sto_MPS = stohru_MPS.getValue() / runarea;
        this.sto_LPS = stohru_LPS.getValue() / runarea;
//        this.runsto_FPS = stohru_FPS.getValue() / area;
        this.sto_FPS = sto_MPS * 0.3;
        
        this.act_LPS = sat_LPS.getValue() * sto_LPS;
        this.act_MPS = sat_MPS.getValue() * sto_MPS;
        
        this.runnetPrecip = NetPrecip.getValue();
        this.runLAI = this.LAIArray.getValue()[day]; 
        
//        this.C_org = entity.getDouble(aNameC_org.getValue());
        this.runC_org = C_org.getValue();
        this.runNO3_Pool = NO3_Pool.getValue();
        this.runNH4_Pool = NH4_Pool.getValue();
        this.runN_activ_pool = N_activ_pool.getValue();
        this.runN_stabel_pool = N_stabel_pool.getValue();
        this.runN_residue_pool_fresh = N_residue_pool_fresh.getValue();
        this.runResidue_pool = Residue_pool.getValue();
        this.runaEvap = aEvap.getValue();
        this.RD1_out_mm = RD1_out.getValue() / runarea;
        this.RD2_out_mm = RD2_out.getValue() / runarea;
        this.d_perco_mm = D_perco.getValue() / runarea;
//        this.layer = SLayer.getValue();
        this.layer = 1;
        this.runvolati_trans = 0;
        this.runNH4inp = NH4inp.getValue();
        this.runplantupN = PlantupN.getValue();
        this.rundenit_trans = 0;
        this.runsurfaceN = 0;
        this.runinterflowN = 0;
        this.runpercoN = 0;
        this.runsurfaceN_in = SurfaceN_in.getValue() * 10000 / runarea;
        this.runinterflowN_in = InterflowN_in.getValue() * 10000 / runarea;
        this.runpercoN_in = PercoN_in.getValue() * 10000 / runarea;
        
        this.runBeta_trans = Beta_trans.getValue();
        this.runBeta_min = Beta_min.getValue();
        this.runBeta_rsd = Beta_rsd.getValue();
        this.runBeta_NO3 = Beta_NO3.getValue();
        this.runaTransp = aTransp.getValue();
        
        
        
        
        this.datumjul = time.get(Attribute.Calendar.DAY_OF_YEAR);
        
        gamma_temp = 0.9 * (runSoil_Temp_Layer / (runSoil_Temp_Layer * Math.exp(9.93 - 0.312 * runSoil_Temp_Layer))) + 0.1;
        
        if (sto_LPS + sto_MPS + sto_FPS > 0){
            gamma_water = (act_LPS + act_MPS + sto_FPS)/(sto_LPS + sto_MPS + sto_FPS);
        } else{
            gamma_water = 0;
        }
        if (runnetPrecip  > 0){
            
            runNH4inp = 0 * runnetPrecip / runarea; /*for testing*/
            
        }
        precalc_nit_vol = calc_nit_volati();
        
        
        
        if (datumjul == 100 || datumjul == 180) {
            
            runResidue_pool = runResidue_pool + 6000;
            runN_residue_pool_fresh = runN_residue_pool_fresh + 9;
            runNH4_Pool = runNH4_Pool + 100;
            
            
            
        }
        /**/
        
        
        
        if (runaTransp  > 0){
            
            runplantupN = (runaTransp / runarea)  * 0.3; /*for testing*/
            
        }
        
        runvolati_trans = calc_voltalisation();
        
        runnitri_trans = calc_nitrification();
        
        Hum_trans = calc_Hum_trans();
        
        Hum_act_min = calc_Hum_act_min();
        
        
        
        /*Calculations of NPools   Check Order of calculations !!!!!!!!!!!!!!*/
        
        runNH4_Pool = runNH4_Pool + runNH4inp - (runvolati_trans + runnitri_trans);
        
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
        
        
        if (layer < 3){
            delta_ntr = this.calc_Res_N_trans();
            
            
            
            runResidue_pool = runResidue_pool - (delta_ntr * runResidue_pool);
            
            if (runResidue_pool < 0){
                runResidue_pool = 0;
            }
            
            
            
            
            runN_activ_pool = runN_activ_pool + (0.2 * (delta_ntr * runN_residue_pool_fresh));
            
            if (runN_activ_pool < 0){
                runN_activ_pool = 0;
            }
            
            runNO3_Pool = runNO3_Pool + runnitri_trans + Hum_act_min +  runinterflowN_in + runsurfaceN_in +(0.8 * (delta_ntr * runN_residue_pool_fresh));
            
            if (runNO3_Pool > runplantupN){
                
                runNO3_Pool = runNO3_Pool - runplantupN;
                
            }else if (runNO3_Pool <= runplantupN){
                
                runplantupN = runNO3_Pool;
                runNO3_Pool = 0;
                
                
            }
            
            rundenit_trans = calc_denitrification();
            runNO3_Pool = runNO3_Pool -  rundenit_trans;
            
            if (runNO3_Pool < 0){
                runNO3_Pool = 0;
            }
            
            runN_residue_pool_fresh = runN_residue_pool_fresh - (delta_ntr * runN_residue_pool_fresh);
            
            if (runN_residue_pool_fresh < 0){
                runN_residue_pool_fresh = 0;
            }
            
        } else if (layer >= 3){
            
            runNO3_Pool = runNO3_Pool + runnitri_trans + runinterflowN_in + runpercoN_in;
            
            if (runNO3_Pool > runplantupN){
                
                runNO3_Pool = runNO3_Pool - runplantupN;
                
            }else {
                
                runplantupN = runNO3_Pool;
                runNO3_Pool = 0;
                
                
            }
            
            rundenit_trans = calc_denitrification();
            runNO3_Pool = runNO3_Pool -  rundenit_trans;
            
            
            if (runNO3_Pool < 0){
                runNO3_Pool = 0;
            }
            
        }
        /*Calculations of NFluxes (out)*/
        
        concN_mobile = calc_concN_mobile();
        
        runsurfaceN = calc_surfaceN();
        runNO3_Pool = runNO3_Pool - runsurfaceN;
        runinterflowN = calc_interflowN();
        runNO3_Pool = runNO3_Pool - runinterflowN;
        runpercoN = calc_percoN();
        runNO3_Pool = runNO3_Pool - runpercoN;
        
        
        
        
        
        if (runNO3_Pool < 0){
            runNO3_Pool = 0;
        }
        
        if (datumjul == i) {
            
            i++;
           // System.out.println("NO3_Pool = " + runNO3_Pool +" ID =  "+ i);
            i=i;
            
            
            
        }
        
        NO3_Pool.setValue(runNO3_Pool);
        NH4_Pool.setValue(runNH4_Pool);
        N_activ_pool.setValue(runN_activ_pool);
        N_stabel_pool.setValue(runN_stabel_pool);
        N_residue_pool_fresh.setValue(runN_residue_pool_fresh);
        Residue_pool.setValue(runResidue_pool);
        Volati_trans.setValue(runvolati_trans);
        Denit_trans.setValue(rundenit_trans);
        Nitri_trans.setValue(runnitri_trans);
        SurfaceN.setValue(runsurfaceN);
        runsurfaceNabs = runsurfaceN * runarea / 10000;
        SurfaceNabs.setValue(runsurfaceNabs);
        InterflowN.setValue(runinterflowN);
        runinterflowNabs = runinterflowN * runarea / 10000;
        InterflowNabs.setValue(runinterflowNabs);
        PercoN.setValue(runpercoN);
        runpercoNabs = runpercoN * runarea / 10000;
        PercoNabs.setValue(runpercoNabs);
//        System.out.println("percoN = " + percoN +" percoNabs =  "+ percoNabs);
        PlantupN.setValue(this.runplantupN);
    }
    
    private boolean calc_nit_volati(){/*precalculations for nitrifikation and volatlisation */
        double eta_water = 0;
        double eta_temp = 0;
        double eta_volz = 0;
        double eta_nitri = 0;
        double eta_volati = 0;
        
        eta_temp = 0.41 * ((runSoil_Temp_Layer - 5) / 10);
        
        if (act_LPS + act_MPS - sto_FPS < 0.25 * (sto_LPS + sto_MPS - sto_FPS)){
            eta_water = (act_LPS + act_MPS - sto_FPS) / (0.25 * (sto_LPS + sto_MPS - sto_FPS));
        } else if (act_LPS + act_MPS - sto_FPS >= 0.25 * (sto_LPS + sto_MPS - sto_FPS)) {
            eta_water = 1;
        }
        
        eta_volz = 1 -(runlayerdepth / (runlayerdepth + Math.exp(4.706 - (0.305 * runlayerdepth))));
        
        eta_nitri = eta_water * eta_temp;
        
        eta_volati = eta_temp * eta_volz;
        
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
        if (!precalc_nit_vol){
            calc_nit_volati();
        }
        
        if (runSoil_Temp_Layer > 5){
            nitri_trans =  (frac_nitr /(frac_nitr + frac_vol)) * N_nit_vol;
        } else if (runSoil_Temp_Layer <= 5){
            nitri_trans = 0;
        }
        return nitri_trans;
        
    }
    
    private double calc_voltalisation(){
        double volati_trans = 0;
        if (!precalc_nit_vol){
            calc_nit_volati();
        }
        if (runSoil_Temp_Layer > 5){
            volati_trans =  (frac_vol /(frac_nitr + frac_vol)) * N_nit_vol;
        } else if (runSoil_Temp_Layer <= 5){
            volati_trans = 0;
        }
        return volati_trans;
    }
    
    private double calc_denitrification(){
        double denit_trans = 0;
        if (gamma_water > 0.75){
            denit_trans = runNO3_Pool * (1 - Math.exp(-1.4 * gamma_temp * runC_org));
            
        } else if (gamma_water <= 0.75){
            denit_trans = 0;
            
        }
        return denit_trans;
    }
    private double calc_nitrateupmove(){
        double n_upmove = 0;
        n_upmove = 0.1 * runNO3_Pool * (runaEvap / (act_LPS + act_MPS + sto_FPS));
        
        return n_upmove;
    }
    
    private double calc_concN_mobile(){
        double  concN_mobile = 0;
        double concN_temp = 0;
        double  mobilewater = 0;
        double  soilstorage = 0;
        
        soilstorage = sto_LPS + sto_MPS + sto_FPS;
        if (layer == 1) {
            mobilewater = RD1_out_mm + RD2_out_mm + d_perco_mm + 1.e-10;
        }
        
        else if (layer > 1) {
            mobilewater = RD2_out_mm + d_perco_mm + 1.e-10;
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
        
        soilstorage = sto_LPS + sto_MPS + sto_FPS;
        if (layer == 1) {
            mobilewater = RD1_out_mm + RD2_out_mm + d_perco_mm + 1.e-10;
        }
        
        else if (layer > 1) {
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
        if (layer == 1) {
            surfaceN = runBeta_NO3 * RD1_out_mm * concN_mobile;
            surfaceN = Math.min(surfaceN,runNO3_Pool);
        } else if (layer > 1) {
            surfaceN = 0;
        }
        return surfaceN;
    }
    
    private double calc_interflowN(){
        double interflowN = 0;
        if (layer == 1) {
            interflowN = (1 - runBeta_NO3) * RD2_out_mm * concN_mobile;
            interflowN = Math.min(interflowN,runNO3_Pool);
        } else if (layer > 1) {
            interflowN = RD2_out_mm * concN_mobile;
            interflowN = Math.min(interflowN,runNO3_Pool);
        }
        return interflowN;
        
    }
    
    private double calc_percoN(){
        double percoN = 0;
        
        percoN = d_perco_mm * concN_mobile;
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