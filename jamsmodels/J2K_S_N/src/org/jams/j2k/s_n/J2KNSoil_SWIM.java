/*
 * J2KNSoil_SWIM.java
 * Created on 23. December 2005, 15:47
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
        public class J2KNSoil_SWIM extends JAMSComponent  {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of hru objects"
            )
            public Attribute.EntityCollection hrus;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current hru entity"
            )
            public Attribute.Entity entity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute name area in m²"
            )
            public Attribute.String aNameArea;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in mm depth of soil layer"
            )
            public Attribute.String aNamelayerdepth;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in mm depth of soil profile"
            )
            public Attribute.String aNametotaldepth;
/*
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in kg/dm³ soil bulk density"
            )
            public Attribute.String aNamesoil_bulk_density;
 */ // preliminary
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in kg/dm³ soil bulk density"
            )
            public Attribute.Double aNamesoil_bulk_density;
//
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual LPS in portion of sto_LPS soil water content"
            )
            public Attribute.String aNamesat_LPS;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual MPS in portion of sto_MPS soil water content"
            )
            public Attribute.String aNamesat_MPS;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum MPS  in l soil water content"
            )
            public Attribute.String aNamestohru_MPS;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum LPS  in l soil water content"
            )
            public Attribute.String aNamestohru_LPS;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum FPS  in l soil water content"
            )
            public Attribute.String aNamestohru_FPS;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "soil temperature in layerdepth in °C"
            )
            public Attribute.String aNameSoil_Temp_Layer;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " in - Leaf area index"
            )
            public Attribute.String aNameLAI;    //only for testing
/*
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " in % organic Carbon in soil"
            )
            public Attribute.String aNameC_org;
 */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " in % organic Carbon in soil"
            )
            public Attribute.Double aNameC_org;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " NO3-Pool in kgN/ha"
            )
            public Attribute.String aNameNO3_Pool;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " NH4-Pool in kgN/ha"
            )
            public Attribute.String aNameNH4_Pool;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " N-Organic Pool with reactive organic matter in kgN/ha"
            )
            public Attribute.String aNameN_activ_pool;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " N-Organic Pool with stable organic matter in kgN/ha"
            )
            public Attribute.String aNameN_stabel_pool;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " Residue in Layer in kgN/ha"
            )
            public Attribute.String aNameResidue_pool;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " N-Organic fresh Pool from Residue in kgN/ha"
            )
            public Attribute.String aNameN_residue_pool_fresh;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " actual evaporation in mm"
            )
            public Attribute.String aNameaEvap;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " surface runoff in l"
            )
            public Attribute.String aNameRD1_out;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " interflow in l"
            )
            public Attribute.String aNameRD2_out;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " percolation in l"
            )
            public Attribute.String aNameD_perco;
/*
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " number of soil layer"
            )
            public Attribute.String aNameLayer;
 */
 /*   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " voltalisation rate from NH4_Pool in kgN/ha"
            )
            public Attribute.String aNameVolati_trans;
  *//*    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " NH4 fertilizer rate in kgN/ha"
            )
            public Attribute.String aNameNH4inp;
   */    @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " plantuptake rate in kgN/ha"
           )
           public Attribute.String aNamePlantupN;
   @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " nitrification rate from  NO3_Pool in kgN/ha"
           )
           public Attribute.String aNameNitri_trans;
   
   @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " denitrification rate from  NO3_Pool in kgN/ha"
           )
           public Attribute.String aNameDenit_trans;
   @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " Nitrate in surface runoff in  in kgN/ha"
           )
           public Attribute.String aNameSurfaceN;
   @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " Nitrate in interflow in  in kgN/ha"
           )
           public Attribute.String aNameInterflowN;
   @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " Nitrate in percolation in  in kgN/ha"
           )
           public Attribute.String aNamePercoN;
   
   @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " Nitrate in surface runoff in  in kgN"
           )
           public Attribute.String aNameSurfaceNabs;
   @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " Nitrate in interflow in  in kgN"
           )
           public Attribute.String aNameInterflowNabs;
   @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " Nitrate in percolation in  in kgN"
           )
           public Attribute.String aNamePercoNabs;
   
   @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " Nitrate in surface runoff added to HRU layer in in kgN"
           )
           public Attribute.String aNameSurfaceN_in;
   
   @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " Nitrate in interflow in added to HRU layer in kgN"
           )
           public Attribute.String aNameInterflowN_in;
   @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " Nitrate in percolation in added to HRU layer in kgN"
           )
           public Attribute.String aNamePercoN_in;
   
   
   
   // constants and calibration parameter
   @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " rate constant between N_activ_pool and N_stabel_pool = 0.00001"
           )
           public Attribute.Double aNameBeta_trans;
   @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " rate factor between N_activ_pool and NO3_Pool to be calibrated 0.001 - 0.003"
           )
           public Attribute.Double aNameBeta_min;
   @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " rate factor between Residue_pool and NO3_Pool to be calibrated 0.1 - 0.02"
           )
           public Attribute.Double aNameBeta_rsd;
   @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " percolation coefitient to calibrate = 0.2"
           )
           public Attribute.Double aNameBeta_NO3;
   
   /* addtional variable for test run*/
   @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " net precipitation in mm"
           )
           public Attribute.String aNameNetPrecip;
   @JAMSVarDescription(
           access = JAMSVarDescription.AccessType.READ,
           description = " actual Transpiration in mm"
           )
           public Attribute.String aNameaTransp;
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
   private double area;
   private double Soil_Temp_Layer;
   private double layerdepth;
   private double soil_bulk_density;
   private double sto_MPS;
   private double sto_LPS;
   private double sto_FPS;
   
   private double act_LPS;
   private double act_MPS;
   
   private double netPrecip;
   private double LAI;
   private double C_org;
   private double C_org_kg_ha = 0;     /*organic Carbon in soil  in kg/ha*/
   private double NO3_Pool;
   //   private double NH4_Pool;
   private double N_activ_pool;
   private double N_stabel_pool;
   private double N_residue_pool_fresh;
   private double Residue_pool;
   private double aEvap;
   private double RD1_out_mm;
   private double RD2_out_mm;
   private double d_perco_mm;
   private int layer;
   private double volati_trans;
   private double NH4inp;
   private double plantupN;
   private double denit_trans;
   private double surfaceN;
   private double interflowN;
   private double percoN;
   private double surfaceNabs;
   private double interflowNabs;
   private double percoNabs;
   private double surfaceN_in;
   private double interflowN_in;
   private double percoN_in;
   
   private double Beta_trans;
   private double Beta_min;
   private double Beta_rsd;
   private double Beta_NO3;
   private double aTransp;
   private boolean precalc_nit_vol;
   
   private double theta_nit = 0.05; /*fraction of anion excluded soil water. depended from clay content min. 0.01  max. 1*/
   private double fr_actN = 0.02;      /** nitrogen active pool fraction. The fraction of organic nitrogen in the active pool. */
   private double N_nit_vol = 0;       /** NH4 that is converted to  NO3 Pool or volatilation . */
   private double frac_nitr = 0;       /** Fraction of N_nit_vol that is nitrification */
   private double frac_vol = 0;        /** Fraction of N_nit_vol that is volatilasation */
   private double Hum_trans; /*transformation rate from NOrg_acti_Pool to N_stabel_pool and back in kgN/ha */
   private double Hum_act_min; /*mirelaization rate from NOrg_acti_Pool to NO3_Pool in kgN/ha */
//    private double nitri_trans = 0; /*nitrifikation rate from NH4_Pool to NO3_Pool in kgN/ha*/
   private double delta_ntr = 0; /*residue decomposition factor */
   private double concN_mobile = 0; /*NO3 concentration of the mobile soil water in kgN/mm H2O*/
   
   private int datumjul = 0;
   
   public void init() throws Attribute.Entity.NoSuchAttributeException{
       
       EntityEnumerator eEnum = hrus.getEntityEnumerator();
       Attribute.Entity[] entities = hrus.getEntityArray();
       
       
       for (int i = 0; i < entities.length; i++) {
           double orgNhum = 0; /*concentration of humic organic nitrogen in the layer (mg/kg)*/
           
//        this.C_org = entity.getDouble(aNameC_org.getValue());
           this.C_org = aNameC_org.getValue();
//        this.soil_bulk_density = entity.getDouble(aNamesoil_bulk_density.getValue());
           this.soil_bulk_density = aNamesoil_bulk_density.getValue();
           this.layerdepth = entities[i].getDouble(aNamelayerdepth.getValue()) * 100; //from dm to mm
           
           Residue_pool = 10;
           NO3_Pool = ((7 * Math.exp(-layerdepth/1000)) * soil_bulk_density * layerdepth)/100;
           //     NH4_Pool = 0.1 * NO3_Pool;
           orgNhum = 10000 * C_org / 14;
           N_activ_pool = ((orgNhum * fr_actN) * soil_bulk_density * layerdepth)/100;
           N_stabel_pool = ((orgNhum * (1 - fr_actN)) * soil_bulk_density * layerdepth)/100;
           N_residue_pool_fresh = 0.0015 * Residue_pool;
           
           
           entities[i].setDouble(aNameNO3_Pool.getValue(),this.NO3_Pool);
           //       entities[i].setDouble(aNameNH4_Pool.getValue(),this.NH4_Pool);
           entities[i].setDouble(aNameN_activ_pool.getValue(),this.N_activ_pool);
           entities[i].setDouble(aNameN_stabel_pool.getValue(),this.N_stabel_pool);
           entities[i].setDouble(aNameResidue_pool.getValue(),this.Residue_pool);
           entities[i].setDouble(aNameN_residue_pool_fresh.getValue(),this.N_residue_pool_fresh);
//            entities[i].setDouble(aNameNH4inp.getValue(),0);
           entities[i].setDouble(aNamePlantupN.getValue(),0);
           entities[i].setDouble(aNameSurfaceN_in.getValue(),0);
           entities[i].setDouble(aNameInterflowN_in.getValue(),0);
           entities[i].setDouble(aNamePercoN_in.getValue(),0);
           entities[i].setDouble(aNameSurfaceNabs.getValue(),0);
           entities[i].setDouble(aNameInterflowNabs.getValue(),0);
           entities[i].setDouble(aNamePercoNabs.getValue(),0);
           
       }
       
       
   }
   
   public void run() throws Attribute.Entity.NoSuchAttributeException{
       int i = 1;
       this.gamma_temp = 0;
       this.gamma_water = 0;
       this.area = entity.getDouble(aNameArea.getValue());
       this.Soil_Temp_Layer = entity.getDouble(aNameSoil_Temp_Layer.getValue());
       this.layerdepth = entity.getDouble(aNamelayerdepth.getValue()) * 100;
       this.sto_MPS = entity.getDouble(aNamestohru_MPS.getValue()) / area;
       this.sto_LPS = entity.getDouble(aNamestohru_LPS.getValue()) / area;
       
//        this.sto_FPS = entity.getDouble(aNamestohru_FPS.getValue()) / area;
       this.sto_FPS = sto_MPS * 0.3;
       
       this.act_LPS = entity.getDouble(aNamesat_LPS.getValue()) * sto_LPS;
       this.act_MPS = entity.getDouble(aNamesat_MPS.getValue()) * sto_MPS;
       
       this.netPrecip = entity.getDouble(aNameNetPrecip.getValue());
       this.LAI = entity.getDouble(aNameLAI.getValue());
       
//        this.C_org = entity.getDouble(aNameC_org.getValue());
       this.C_org = aNameC_org.getValue();
       this.C_org_kg_ha = (C_org * 10000 * (layerdepth / 1000) / (100 / 1.5))*1000 ;
       this.NO3_Pool = entity.getDouble(aNameNO3_Pool.getValue());
//        this.NH4_Pool = entity.getDouble(aNameNH4_Pool.getValue());
       this.N_activ_pool = entity.getDouble(aNameN_activ_pool.getValue());
       this.N_stabel_pool = entity.getDouble(aNameN_stabel_pool.getValue());
       this.N_residue_pool_fresh = entity.getDouble(aNameN_residue_pool_fresh.getValue());
       this.Residue_pool = entity.getDouble(aNameResidue_pool.getValue());
       this.aEvap = entity.getDouble(aNameaEvap.getValue());
       this.RD1_out_mm = entity.getDouble(aNameRD1_out.getValue()) / area;
       this.RD2_out_mm = entity.getDouble(aNameRD2_out.getValue()) / area;
       this.d_perco_mm = entity.getDouble(aNameD_perco.getValue()) / area;
//        this.layer = entity.getInt(aNameLayer.getValue());
       this.layer = 1;
//        this.volati_trans = 0;
//        this.NH4inp = entity.getDouble(aNameNH4inp.getValue());
       this.plantupN = entity.getDouble(aNamePlantupN.getValue());
       this.denit_trans = 0;
       this.surfaceN = 0;
       this.interflowN = 0;
       this.percoN = 0;
       this.surfaceN_in = entity.getDouble(aNameSurfaceN_in.getValue()) * 10000 / area;
       this.interflowN_in = entity.getDouble(aNameInterflowN_in.getValue()) * 10000 / area;
       this.percoN_in = entity.getDouble(aNamePercoN_in.getValue()) * 10000 / area;
       
       this.Beta_trans = aNameBeta_trans.getValue();
       this.Beta_min = aNameBeta_min.getValue();
       this.Beta_rsd = aNameBeta_rsd.getValue();
       this.Beta_NO3 = aNameBeta_NO3.getValue();
       this.aTransp = entity.getDouble(aNameaTransp.getValue());
       
       
       
       
       this.datumjul = time.get(Attribute.Calendar.DAY_OF_YEAR);
//        sto_FPS = 0.3 *(sto_MPS); /*SWAT definition of wilting point */
       

       
       gamma_temp = Soil_Temp_Layer / (Soil_Temp_Layer + Math.exp(6.82 - 0.232 * Soil_Temp_Layer)); /*SWIM <> SWAT */
       
       if (gamma_temp < 0){
          gamma_temp = 0; 
       } 
       
       gamma_water = (act_LPS + act_MPS + sto_FPS) / (sto_LPS + sto_MPS + sto_FPS);
       
       

       
       
       if (datumjul == 100 || datumjul == 180) {
           
           Residue_pool = Residue_pool + 2000;
           N_residue_pool_fresh = 0.0015 * Residue_pool;
//            NH4_Pool = NH4_Pool + 50;
           
           
           
       }
       /**/
       
       
       
       if (aTransp  > 0){
           
           plantupN = (aTransp / area)  * 0.3; /*for testing*/
           
       }
       

       
       Hum_trans = calc_Hum_trans();
       
       Hum_act_min = calc_Hum_act_min();
       
       
       
       /*Calculations of NPools   Check Order of calculations !!!!!!!!!!!!!!*/
       

       
       N_stabel_pool = N_stabel_pool + Hum_trans;
       
       if (N_stabel_pool < 0){
           N_stabel_pool = 0;
       }
       
       N_activ_pool = N_activ_pool - Hum_trans;
       
       if (N_activ_pool < 0){
           N_activ_pool = 0;
       }
       
       
       if (layer < 3){
           delta_ntr = this.calc_Res_N_trans();
           
           
           
           Residue_pool = Residue_pool - (delta_ntr * Residue_pool);
           
           if (Residue_pool < 0){
               Residue_pool = 0;
           }
           
           
           
           
           N_activ_pool = N_activ_pool + (0.2 * (delta_ntr * N_residue_pool_fresh));
           
           if (N_activ_pool < 0){
               N_activ_pool = 0;
           }
           

           
           NO3_Pool = NO3_Pool  + Hum_act_min +  interflowN_in + surfaceN_in +(0.8 * (delta_ntr * N_residue_pool_fresh));
           
           if (NO3_Pool > plantupN){
               
               NO3_Pool = NO3_Pool - plantupN;
               
           }else if (NO3_Pool <= plantupN){
               
               plantupN = NO3_Pool;
               NO3_Pool = 0;
               
               
           }

           
           denit_trans = calc_denitrification();
           NO3_Pool = NO3_Pool -  denit_trans;
           
           if (NO3_Pool < 0){
               NO3_Pool = 0;
           }
           
           N_residue_pool_fresh = N_residue_pool_fresh - (delta_ntr * N_residue_pool_fresh);
           
           if (N_residue_pool_fresh < 0){
               N_residue_pool_fresh = 0;
           }
           
       } else if (layer >= 3){
           

           NO3_Pool = NO3_Pool + interflowN_in + percoN_in;
           
           if (NO3_Pool > plantupN){
               
               NO3_Pool = NO3_Pool - plantupN;
               
           }else {
               
               plantupN = NO3_Pool;
               NO3_Pool = 0;
               
               
           }
           
           denit_trans = calc_denitrification();
           NO3_Pool = NO3_Pool -  denit_trans;
           
           
           if (NO3_Pool < 0){
               NO3_Pool = 0;
           }
           
       }
       /*Calculations of NFluxes (out)*/
       

       concN_mobile = calc_concN_mobile();
       
       surfaceN = calc_surfaceN();
       NO3_Pool = NO3_Pool - surfaceN;
       interflowN = calc_interflowN();
       NO3_Pool = NO3_Pool - interflowN;
       percoN = calc_percoN();
       NO3_Pool = NO3_Pool - percoN;
       
 
       
       if (NO3_Pool > 0){
           NO3_Pool = NO3_Pool;
       } else{
           NO3_Pool = 0;
       }
       
       if (datumjul == i) {
           
           i++;
           System.out.println("NO3_Pool = " + NO3_Pool +"datejul  =  "+ i);
           i=i;
           
           
           
       }
       
       entity.setDouble(aNameNO3_Pool.getValue(),this.NO3_Pool);
//        entity.setDouble(aNameNH4_Pool.getValue(),this.NH4_Pool);
       entity.setDouble(aNameN_activ_pool.getValue(),this.N_activ_pool);
       entity.setDouble(aNameN_stabel_pool.getValue(),this.N_stabel_pool);
       entity.setDouble(aNameN_residue_pool_fresh.getValue(),this.N_residue_pool_fresh);
       entity.setDouble(aNameResidue_pool.getValue(),this.Residue_pool);
//        entity.setDouble(aNameVolati_trans.getValue(),this.volati_trans);
       entity.setDouble(aNameDenit_trans.getValue(),this.denit_trans);
//        entity.setDouble(aNameNitri_trans.getValue(),this.nitri_trans);
       entity.setDouble(aNameSurfaceN.getValue(),this.surfaceN);
       surfaceNabs = surfaceN * area / 10000;
       entity.setDouble(aNameSurfaceNabs.getValue(),this.surfaceNabs);
       
       entity.setDouble(aNameInterflowN.getValue(),this.interflowN);
       interflowNabs = interflowN * area / 10000;
//        System.out.println("interflowNabs"+ interflowNabs);
       entity.setDouble(aNameInterflowNabs.getValue(),this.interflowNabs);
       
       entity.setDouble(aNamePercoN.getValue(),this.percoN);
       percoNabs = percoN * area / 10000;
       entity.setDouble(aNamePercoN.getValue(),this.percoNabs);
       entity.setDouble(aNamePlantupN.getValue(),this.plantupN);
   }
   
 
 
   private double calc_Hum_trans(){
       double N_Hum_trans = 0;
       
       
       N_Hum_trans = Beta_trans * ((N_activ_pool  / fr_actN)  - N_stabel_pool);/*SWIM <> SWAT */
       
       return N_Hum_trans;
   }
   
   private double calc_Hum_act_min(){/*SWIM = SWAT */
       double N_Hum_act_min = 0;
       
       
       N_Hum_act_min = Beta_min * Math.sqrt(gamma_temp * gamma_water) * N_activ_pool;
       
       return N_Hum_act_min;
   }
   
   private double calc_Res_N_trans(){ /*is only allowed in the first layer *//*SWIM = SWAT */
       double epsilon_C_N = 0;
       double gamma_ntr = 0;
        /*double Res_N_trans = 0;*/
        
       
       
       if (N_residue_pool_fresh == 0 && NO3_Pool == 0){
           this.delta_ntr = 0;
           
       } else {
         /*calculation of the c/n ratio */
           epsilon_C_N = (Residue_pool * 0.58) / (N_residue_pool_fresh + NO3_Pool);
           /*calculation of nutrient cycling residue composition factor*/
           gamma_ntr = Math.min(1,Math.exp(-0.693*((epsilon_C_N - 25)/ 25)));
           /*calculation of the decay rate constant*/
           this.delta_ntr = Beta_rsd * gamma_ntr * Math.sqrt(gamma_temp * gamma_water);
       }
        /*Res_N_trans = delta_ntr * N_residue_pool_fresh;
        /*splitting in decomposition 20% and Minteralisation 80%  in run method*/
       return delta_ntr;
   }
   

   private double calc_denitrification(){/*SWIM <> SWAT */
       double denit_trans = 0;
       double  gamma_tempcarb = 0;
       double  gamma_waterdenit = 0;
       double cnd = -0.00001; /*undocumentet shape parameter estimated  min -0.000012 and max -0.000008  (maybe to calibrate)*/
       if (gamma_water > 0.9){
           gamma_waterdenit = 0.06 * (Math.exp((3*(act_LPS + act_MPS + sto_FPS)) / (sto_LPS + sto_MPS + sto_FPS)));
           gamma_tempcarb = 1 - Math.exp(cnd * Soil_Temp_Layer * C_org_kg_ha);
           
           denit_trans = NO3_Pool * gamma_tempcarb * gamma_waterdenit;
           
       } else if (gamma_water <= 0.9){
           denit_trans = 0;
           
       }
       return denit_trans;
   }
   
   private double calc_concN_mobile(){/*SWIM <> SWAT */
       double  concN_mobile = 0;
       double WTOT = 0;
       double NFL = 0;
       
       if (layer == 1) {
           WTOT = RD1_out_mm + RD2_out_mm + d_perco_mm;
       } else if (layer > 1) {
           WTOT = RD2_out_mm + d_perco_mm;
       }
       NFL = (NO3_Pool * (1- Math.exp(-WTOT / (sto_LPS + sto_MPS))));
       
       if (WTOT == 0){
           concN_mobile = 0;
       } else{
           concN_mobile =  NFL / WTOT;
       }
       return concN_mobile;
   }
   
   
   private double calc_surfaceN(){
       double surfaceN = 0;
       if (layer == 1) {
           surfaceN = Beta_NO3 * RD1_out_mm * concN_mobile;
           surfaceN = Math.min(surfaceN,NO3_Pool);
       } else if (layer > 1) {
           surfaceN = 0;
       }
       return surfaceN;
   }
   
   private double calc_interflowN(){
       double interflowN = 0;
       if (layer == 1) {
           interflowN = (1 - Beta_NO3) * RD2_out_mm * concN_mobile;
           interflowN = Math.min(interflowN,NO3_Pool);
       } else if (layer > 1) {
           interflowN = RD2_out_mm * concN_mobile;
           interflowN = Math.min(interflowN,NO3_Pool);
       }
       return interflowN;
       
   }
   
   private double calc_percoN(){
       double percoN = 0;
       
       percoN = d_perco_mm * concN_mobile;
       percoN = Math.min(percoN,NO3_Pool);
       return percoN;
   }
   
   
   public void cleanup() throws Attribute.Entity.NoSuchAttributeException{
       
   }
}
