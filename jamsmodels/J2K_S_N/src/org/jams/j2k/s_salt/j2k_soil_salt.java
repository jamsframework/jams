/*
 * j2k_soil_salt.java
 * Created on 20. Februar 2007, 10:08
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c8fima
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
package org.jams.j2k.s_salt;

import jams.data.*;
import jams.model.*;
import jams.io.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
        title = "Title",
        author = "Manfred Fink",
        description = "Calculates salt dynamics in soil"
)
public class j2k_soil_salt extends JAMSComponent {

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
            description = "in mm depth of soil layer"
    )
    public Attribute.DoubleArray layerdepth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in cm depth of soil profile"
    )
    public Attribute.Double totaldepth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in dm actual depth of roots"
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
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " NO3-Pool in kgN/ha"
    )
    public Attribute.DoubleArray NaCl_Pool;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "sum of NO3-Pool in kgN/ha"
    )
    public Attribute.Double sNaCl_Pool;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = " sum of interflowNaClabs in kgNaCl/ha"
    )
    public Attribute.Double sinterflowNaClabs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = " sum of interflowNaCl in kgNaCl/ha"
    )
    public Attribute.Double sinterflowNaCl;

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
    public Attribute.Double D_perco;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " NaCl in surface runoff in  in kgNaCl/ha"
    )
    public Attribute.Double SurfaceNaCl;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " NaCl in interflow in  in kgNaCl/ha"
    )
    public Attribute.DoubleArray InterflowNaCl;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " NaCl in percolation in kgNaCl/ha"
    )
    public Attribute.Double PercoNaCl;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in surface runoff in kgNaCl"
    )
    public Attribute.Double SurfaceNaClabs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in interflow in in kgNaCl"
    )
    public Attribute.DoubleArray InterflowNaClabs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in percolation in in kgNaCl"
    )
    public Attribute.Double PercoNaClabs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in surface runoff added to HRU layer in in kgNaCl"
    )
    public Attribute.Double SurfaceNaCl_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " NaCl in interflow in added to HRU layer in kgNaCl"
    )
    public Attribute.DoubleArray InterflowNaCl_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "potential NaCl content of plants in kgNaCl/ha"
    )
    public Attribute.Double BioSALToptAct;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual NaCl nitrogen content of plants in kgN/ha"
    )
    public Attribute.Double BioSALTAct;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actual NaCl uptake of plants in kgN/ha"
    )
    public Attribute.Double actNaCl_up;

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
            access = JAMSVarDescription.AccessType.READ,
            description = " Input of plant residues kg/ha"
    )
    public Attribute.Double inp_biomass;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Nitrogen input of plant residues in kgN/ha"
    )
    public Attribute.Double inpNaCl_biomass;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Residue in Layer in kgN/ha"
    )
    public Attribute.DoubleArray Residue_pool;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " NaCl-Organic fresh Pool from Residue in kgN/ha"
    )
    public Attribute.DoubleArray NaCl_residue_pool_fresh;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " NaCl-Organic fresh Pool from Residue in kgN/ha"
    )
    public Attribute.Double sNaClResiduePool;

    // constants and calibration parameter
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "infiltration bypass parameter to calibrate = 0 - 1"
    )
    public Attribute.Double infil_conc_factor;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "concentration of Salt in rain = 0 - 0.05 kgNaCl/(mm * ha)"
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
            description = " actual evaporation in mm"
    )
    public Attribute.DoubleArray aTP_h;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " percolation coefitient to calibrate = 0.2"
    )
    public Attribute.Double Beta_NaCl;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " percolation coefitient to calibrate = 0.2"
    )
    public Attribute.Double Beta_rsd;

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

    private double runNaCl_Pool;
    private double RD1_out_mm;
    private double RD2_out_mm;
    private double d_perco_mm;
    private double h_perco_mm;
    private double h_infilt_mm;
    private int layer;

    private double runResidue_pool;
    private double runplantupNaCl;
    private double runsurfaceNaCl;
    private double runinterflowNaCl;
    private double runpercoNaCl;
    private double runsurfaceNaClabs;
    private double runinterflowNaClabs;
    private double runpercoNaClabs;
    private double runsurfaceNaCl_in;
    private double runinterflowNaCl_in;
    private double sumlayer;
    private double runNaCl_residue_pool_fresh;
    private double runBeta_min;
    private double runBeta_rsd;
    private double runBeta_NaCl;

    private double theta_nit = 0.00; /*fraction of anion excluded soil water. depended from clay content min. 0.01  max. 1*/

    private double concSALT_mobile = 0; /*NACL concentration of the mobile soil water in kgN/mm H2O*/

    private double delta_ntr = 0; /*residue decomposition factor */

    private int datumjul = 0;
    private int app_time = 0;
    double[] hor_by_infilt;
    double[] NaCl_Poolvals;
    double[] w_l_diff;
    double[] ConcSALT_mobile;
    double[] diffout;

    public void init() throws Attribute.Entity.NoSuchAttributeException {

    }

    public void run() throws Attribute.Entity.NoSuchAttributeException {
        /*         Attribute.Calendar testtime = new Attribute.Calendar();
         testtime.setValue("1993-10-12 07:30");
         if (time.equals(testtime)){
         System.out.println(time.getValue()) ;
         }*/

        int i = 0;

        this.gamma_temp = 0;
        this.gamma_water = 0;
        this.runarea = area.getValue();
        this.layer = (int) Layer.getValue();
        double runprecip = precip.getValue();
        sumlayer = 0;
        double runsum_Ninput = 0;
        double sumNaCl_Pool = 0;
        double sumNaCl_residue_pool = 0;
        double plantuptake_NaCl = 0;
        double sumplantuptake_NaCl = 0;
        double suminterflowNaClabs = 0;
        double suminterflowNaCl = 0;
        double h_infilt_mm_sum = 0;
        double sumh_infilt_mm = 0;
        double sum_Saltupmove = 0;
        double Salt_upmove_h = 0;
        double a_deposition = 0;
        double NaClrespool = 0;
        double diffoutNa = 0;
//        double[] NO3_Poolvals = new double[layer];
        runlayerdepth = new double[layer];
        double[] NaCl_residue_pool_freshvals = new double[layer];
        double[] Residue_poolvals = new double[layer];
        double[] interflowNaClvals = new double[layer];
        double[] percoNaClvals = new double[layer];
        double[] interflowNaClabsvals = new double[layer];
        double[] percoNaClabsvals = new double[layer];
        double[] plantup_hor = new double[layer];
        double[] NaClbalance = new double[layer];
        double[] NaCl_Poolalt = new double[layer];

        this.runsurfaceNaCl = 0;
        this.runBeta_rsd = Beta_rsd.getValue();

        hor_by_infilt = new double[layer];
        diffout = new double[layer];
        w_l_diff = new double[layer];
        NaCl_Poolvals = new double[layer];
        ConcSALT_mobile = new double[layer];
        i = 0;
        /*while (i < layer){
         NO3_Poolalt[i] = NO3_Pool.getValue()[i];
         i++;
         }*/

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

        while (i > 0) {

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
        for (i = 0; i < layer; i++) {

            NaCl_Poolvals[i] = NaCl_Pool.getValue()[i];
            diffout[i] = 0;
        }

        i = 0;

        for (i = 0; i < layer - 1; i++) {

            this.w_l_diff[i] = w_layer_diff.getValue()[i] / runarea;

            if (w_l_diff[i] > 0) {
                diffout[i + 1] = diffout[i + 1] + w_l_diff[i];
            } else {
                diffout[i] = diffout[i] - w_l_diff[i];
            }

        }

        i = 0;
        // horizont processies loop
        while (i < layer) {

            this.sto_MPS = stohru_MPS.getValue()[i] / runarea;
            this.sto_LPS = stohru_LPS.getValue()[i] / runarea;
            this.sto_FPS = stohru_FPS.getValue()[i] / runarea;

            this.act_LPS = sat_LPS.getValue()[i] * sto_LPS;
            this.act_MPS = sat_MPS.getValue()[i] * sto_MPS;

            runResidue_pool = Residue_pool.getValue()[i];

            this.runNaCl_Pool = NaCl_Pool.getValue()[i];
            /*if (runNaCl_Pool < 0){
             System.out.println(runNaCl_Pool +" = runNaCl_Pool");
             }*/

            this.RD1_out_mm = RD1_out.getValue() / runarea;
            this.RD2_out_mm = RD2_out.getValue()[i] / runarea;
            this.d_perco_mm = D_perco.getValue() / runarea;
            this.h_perco_mm = perco_hor.getValue()[i] / runarea;

            this.runinterflowNaCl = 0;
            this.runpercoNaCl = 0;
            this.runNaCl_residue_pool_fresh = NaCl_residue_pool_fresh.getValue()[i];
            this.runsurfaceNaCl_in = SurfaceNaCl_in.getValue() * 10000 / runarea;

            this.runinterflowNaCl_in = InterflowNaCl_in.getValue()[i] * 10000 / runarea;
            SurfaceNaCl_in.setValue(0);

            /*          calculation of amount of nitrogen uptake with epaporation from soil */
            int j = 1;

            while (j < layer) {

                Salt_upmove_h = calc_saltupmove(j);
                sum_Saltupmove = sum_Saltupmove + Salt_upmove_h;

                j++;
            }

            if (runSoil_Temp_Layer == 0) {
                runSoil_Temp_Layer = 0.00001;
            }

            gamma_temp = 0.9 * (runSoil_Temp_Layer / (runSoil_Temp_Layer * Math.exp(9.93 - 0.312 * runSoil_Temp_Layer)));

            if (sto_LPS + sto_MPS + sto_FPS > 0) {
                gamma_water = (act_LPS + act_MPS + sto_FPS) / (sto_LPS + sto_MPS + sto_FPS);
            } else {
                gamma_water = 0;
            }

            /*Calculations of NPools   Check Order of calculations !!!!!!!!!!!!!!*/
            a_deposition = deposition_factor.getValue() * runprecip;

            double delta_res = this.calc_Res_Salt_trans();

            NaClrespool = (delta_res * runNaCl_residue_pool_fresh);
            runNaCl_Pool = runNaCl_Pool + sum_Saltupmove + a_deposition + NaClrespool;

            runNaCl_residue_pool_fresh = runNaCl_residue_pool_fresh - (delta_res * runNaCl_residue_pool_fresh);

            if (i > 0) {

                runNaCl_Pool = runNaCl_Pool + runinterflowNaCl_in + percoNaClvals[i - 1];
            } else {
                runNaCl_Pool = runNaCl_Pool + runinterflowNaCl_in;
            }

            concSALT_mobile = calc_concSALT_mobile(i);
            ConcSALT_mobile[i] = concSALT_mobile;
            plantuptake_NaCl = calc_plantuptake(i);
            /*if (runNaCl_Pool < 0){
             System.out.println(runNaCl_Pool +" = runNaCl_Pool");
             }*/

            if (i == 0) {
                runsurfaceNaCl = calc_surfaceNaCl();
                runNaCl_Pool = runNaCl_Pool - runsurfaceNaCl;
                runNaCl_Pool = Math.max(0, runNaCl_Pool);
            } else {
                //runsurfaceNaCl = 0;
            }
            
            runinterflowNaCl = calc_interflowNaCl(i);
            runNaCl_Pool = runNaCl_Pool - runinterflowNaCl;
            runpercoNaCl = calc_percoNaCl(i);
            runNaCl_Pool = runNaCl_Pool - runpercoNaCl;

            if (runNaCl_Pool < 0) {
                runNaCl_Pool = 0;
            }

            runinterflowNaClabs = runinterflowNaCl * runarea / 10000;
            runpercoNaClabs = runpercoNaCl * runarea / 10000;

            NaCl_Poolvals[i] = runNaCl_Pool;

            Residue_poolvals[i] = runResidue_pool;
            interflowNaClvals[i] = runinterflowNaCl;
            interflowNaClabsvals[i] = runinterflowNaClabs;
            percoNaClvals[i] = runpercoNaCl;
            percoNaClabsvals[i] = runpercoNaClabs;
            // time;
            sumplantuptake_NaCl = sumplantuptake_NaCl + plantuptake_NaCl;
            sumNaCl_residue_pool = sumNaCl_residue_pool + runNaCl_residue_pool_fresh;
            sumNaCl_Pool = sumNaCl_Pool + runNaCl_Pool;

            suminterflowNaClabs = runinterflowNaClabs + suminterflowNaClabs;
            suminterflowNaCl = runinterflowNaCl + suminterflowNaCl;
            NaCl_residue_pool_freshvals[i] = runNaCl_residue_pool_fresh;

            i++;
        }
        i = 0;

        // distribution of diffusion Na into the
        for (i = 0; i < layer - 1; i++) {

            if (w_l_diff[i] < 0) {
                diffoutNa = w_l_diff[i] * ConcSALT_mobile[i];
                NaCl_Poolvals[i] = NaCl_Poolvals[i] + diffoutNa;
                NaCl_Poolvals[i + 1] = NaCl_Poolvals[i + 1] - diffoutNa;
            } else {
                diffoutNa = w_l_diff[i] * ConcSALT_mobile[i + 1];
                NaCl_Poolvals[i] = NaCl_Poolvals[i] + diffoutNa;
                NaCl_Poolvals[i + 1] = NaCl_Poolvals[i + 1] - diffoutNa;

            }
        }
        // writing of pools
        double[] zerosetter = new double[layer];
        i = 0;
        while (i < layer) {
            zerosetter[i] = 0;
            i++;
        }
        NaCl_Pool.setValue(NaCl_Poolvals);
        NaCl_residue_pool_fresh.setValue(NaCl_residue_pool_freshvals);
        Residue_pool.setValue(Residue_poolvals);
        // writing of fluxes
        InterflowNaCl.setValue(interflowNaClvals);
        InterflowNaClabs.setValue(interflowNaClabsvals);
        PercoNaCl.setValue(percoNaClvals[layer - 1]);
        PercoNaClabs.setValue(percoNaClabsvals[layer - 1]);

        SurfaceNaCl.setValue(runsurfaceNaCl);
        runsurfaceNaClabs = runsurfaceNaCl * runarea / 10000;
        SurfaceNaClabs.setValue(runsurfaceNaClabs);
        sinterflowNaClabs.setValue(suminterflowNaClabs);
        sinterflowNaCl.setValue(suminterflowNaCl);
        actNaCl_up.setValue(sumplantuptake_NaCl);
         // writing of transfomations time

        sNaCl_Pool.setValue(sumNaCl_Pool);
        sNaClResiduePool.setValue(sumNaCl_residue_pool);
        InterflowNaCl_in.setValue(zerosetter);

//        System.out.println("percoN = " + percoN +" percoNabs =  "+ percoNabs);
    }

    private double calc_plantuptake(int i) {
        double plantuptake = 0;

        plantuptake = ConcSALT_mobile[i] * aTP_h.getValue()[i];

        return plantuptake;
    }

    private double calc_saltupmove(int j) {
        double salt_upmove = 0;
        double runaEvap = aEP_h.getValue()[j];
        double sto_MPS = stohru_MPS.getValue()[j];
        double sto_LPS = stohru_LPS.getValue()[j];
        double sto_FPS = stohru_FPS.getValue()[j];
        double act_LPS = sat_LPS.getValue()[j] * sto_LPS;
        double act_MPS = sat_MPS.getValue()[j] * sto_MPS;

        salt_upmove = 0.1 * NaCl_Poolvals[j] * (runaEvap / (act_LPS + act_MPS + sto_FPS));

        NaCl_Poolvals[j] = NaCl_Poolvals[j] - salt_upmove;

        return salt_upmove;
    }

    private double calc_concSALT_mobile(int i) {
        double concSALT_mobile = 0;
        double concSALT_temp = 0;
        double mobilewater = 0;
        double soilstorage = 0;

        soilstorage = act_LPS + act_MPS + sto_FPS;
        if (i == 0) {
            mobilewater = RD1_out_mm + RD2_out_mm + h_perco_mm + hor_by_infilt[i] + diffout[i] + 1.e-10;
        } else if (i > 0) {
            mobilewater = RD2_out_mm + h_perco_mm + hor_by_infilt[i] + diffout[i] + 1.e-10;
        }
        if (i == (layer - 1)) {
            mobilewater = RD2_out_mm + d_perco_mm + diffout[i] + 1.e-10;
        }
//        concSALT_temp = (runNaCl_Pool * (1 - Math.exp(- mobilewater / ((1 - theta_nit) * soilstorage)))); //SWAT Version
//        concSALT_mobile = concSALT_temp / mobilewater;

        concSALT_mobile = runNaCl_Pool / (soilstorage + mobilewater); //linear Version simple mixing

        if (concSALT_mobile < 0) {
            concSALT_mobile = 0;
        }
        return concSALT_mobile;
    }

    private double concSALT_mobile2() { //simpler Version for tests
        double concSALT_mobile = 0;
        double mobilewater = 0;
        double soilstorage = 0;

        soilstorage = sto_LPS + sto_MPS + sto_FPS;
        if (layer == 0) {
            mobilewater = RD1_out_mm + RD2_out_mm + d_perco_mm + 1.e-10;
        } else if (layer > 0) {
            mobilewater = RD2_out_mm + d_perco_mm + 1.e-10;
        }
        concSALT_mobile = (runNaCl_Pool / (mobilewater + ((1 - theta_nit) * soilstorage)));

        if (concSALT_mobile < 0) {
            concSALT_mobile = 0;
        }
        return concSALT_mobile;
    }

    private double calc_surfaceNaCl() {
        double surfaceSALT = 0;

        surfaceSALT = Beta_NaCl.getValue() * RD1_out_mm * concSALT_mobile;  //SWAT orginal
//        surfaceN = RD1_out_mm * concN_mobile;
        surfaceSALT = Math.min(surfaceSALT, runNaCl_Pool);

        return surfaceSALT;
    }

    private double calc_interflowNaCl(int i) {
        double interflowSALT = 0;

        if (i == 0) {
            interflowSALT = (1.0 - Beta_NaCl.getValue()) * RD2_out_mm * concSALT_mobile;
            interflowSALT = Math.min(interflowSALT, runNaCl_Pool);
        } else if (i > 0) {
            interflowSALT = RD2_out_mm * concSALT_mobile;
            interflowSALT = Math.min(interflowSALT, runNaCl_Pool);
        }
        if (interflowSALT < 0) {
            System.out.println(RD2_out_mm + " = RD2_out_mm " + interflowSALT + " = interflowN");
        }
        return interflowSALT;

    }

    private double calc_percoNaCl(int i) {
        double percoSALT = 0;
        if (i < (layer - 1)) {
            percoSALT = (hor_by_infilt[i] + h_perco_mm) * concSALT_mobile;
        } else {
            percoSALT = d_perco_mm * concSALT_mobile;
        }
        percoSALT = Math.min(percoSALT, runNaCl_Pool);
        return percoSALT;
    }

    private double calc_Res_Salt_trans() { /*is only allowed in the first layer */

        double epsilon_C_N = 0;
        double gamma_ntr = 0;
        /*double Res_N_trans = 0;
         /*calculation of the c/n ratio */
        /*epsilon_C_N = (runResidue_pool * 0.58)/(runN_residue_pool_fresh + runNO3_Pool);
         /*calculation of nutrient cycling residue composition factor*/
        epsilon_C_N = 30;
        gamma_ntr = Math.min(1, Math.exp(-0.693 * ((epsilon_C_N - 25) / 25)));
        /*calculation of the decay rate constant*/
        this.delta_ntr = runBeta_rsd * gamma_ntr * Math.sqrt(gamma_temp * gamma_water);

        /*Res_N_trans = delta_ntr * N_residue_pool_fresh;
         /*splitting in decomposition 20% and Minteralisation 80%  in run method*/
        return delta_ntr;
    }

    public void cleanup() throws Attribute.Entity.NoSuchAttributeException {

    }
}
