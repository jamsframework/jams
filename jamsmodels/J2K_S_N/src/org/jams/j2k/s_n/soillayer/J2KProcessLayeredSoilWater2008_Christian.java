/*
 * J2KProcessLumpedSoilWater.java
 * Created on 25. November 2005, 13:21
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
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
package org.jams.j2k.s_n.soillayer;

import jams.data.*;
import jams.model.*;
import java.util.Random;

/**
 *
 * @author Peter Krause modifications by Manfred Fink
 */
/*

 */
@JAMSComponentDescription(title = "J2KProcessLayerdSoilWater",
author = "Peter Krause, modifications by Manfred Fink",
description = "Calculates soil water balance for each HRU with vertical layers")
public class J2KProcessLayeredSoilWater2008_Christian extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "time")
    public Attribute.Calendar time;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The current hru entities")
    public Attribute.EntityCollection entities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "attribute area")
    public Attribute.Double area;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "attribute slope")
    public Attribute.Double slope;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "sealed grade")
    public Attribute.Double sealedGrade;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "state variable net rain")
    public Attribute.Double netRain;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "state variable net snow")
    public Attribute.Double netSnow;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state variable potET")
    public Attribute.Double potET;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "state variable actET")
    public Attribute.Double actET;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "snow depth")
    public Attribute.Double snowDepth;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "daily snow melt")
    public Attribute.Double snowMelt;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "horizons")
    public Attribute.Double horizons;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "in cm depth of soil layer")
    public Attribute.DoubleArray layerdepth;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "in m actual depth of roots")
    public Attribute.Double rootdepth;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU attribute maximum MPS")
    public Attribute.DoubleArray maxMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU attribute maximum LPS")
    public Attribute.DoubleArray maxLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU state var actual MPS")
    public Attribute.DoubleArray actMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU state var actual LPS")
    public Attribute.DoubleArray actLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU state var actual depression storage")
    public Attribute.Double actDPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU state var saturation of MPS")
    public Attribute.DoubleArray satMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU state var saturation of LPS")
    public Attribute.DoubleArray satLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU attribute maximum MPS of soil")
    public Attribute.Double soilMaxMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU attribute maximum LPS of soil")
    public Attribute.Double soilMaxLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU state var actual MPS of soil")
    public Attribute.Double soilActMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU state var actual LPS of soil")
    public Attribute.Double soilActLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU state var saturation of MPS of soil")
    public Attribute.Double soilSatMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU state var saturation of LPS of soil")
    public Attribute.Double soilSatLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU state var saturation of whole soil")
    public Attribute.Double satSoil;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU statevar infiltration")
    public Attribute.Double infiltration;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU statevar interflow")
    public Attribute.Double interflow;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU statevar percolation")
    public Attribute.Double percolation;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar RD1 inflow")
    public Attribute.Double inRD1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU statevar RD1 outflow")
    public Attribute.Double outRD1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU statevar RD1 generation")
    public Attribute.Double genRD1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar RD2 inflow")
    public Attribute.DoubleArray inRD2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU statevar RD2 outflow")
    public Attribute.DoubleArray outRD2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU statevar RD2 generation")
    public Attribute.DoubleArray genRD2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "maximum depression storage [mm]")
    public Attribute.Double soilMaxDPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "poly reduction of ETP")
    public Attribute.Double soilPolRed;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "linear reduction of ETP")
    public Attribute.Double soilLinRed;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "maximum infiltration rate in summer [mm/d]")
    public Attribute.Double soilMaxInfSummer;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "maximum infiltration rate in winter [mm/d]")
    public Attribute.Double soilMaxInfWinter;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "maximum infiltration rate on snow [mm/d]")
    public Attribute.Double soilMaxInfSnow;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "maximum infiltration part on sealed areas (gt 80%)")
    public Attribute.Double soilImpGT80;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "maximum infiltration part on sealed areas (lt 80%)")
    public Attribute.Double soilImpLT80;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "MPS/LPS distribution coefficient for inflow")
    public Attribute.Double soilDistMPSLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "MPS/LPS diffusion coefficient")
    public Attribute.Double soilDiffMPSLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "LPS outflow coefficient")
    public Attribute.Double soilOutLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "LPS lateral-vertical distribution coefficient")
    public Attribute.Double soilLatVertLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "maximum percolation rate in soil [mm/d]")
    public Attribute.Double soilMaxPerc;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "maximum percolation rate out of soil [mm/d]")
    public Attribute.Double geoMaxPerc;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "concentration coefficient for RD1")
    public Attribute.Double soilConcRD1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "concentration coefficient for RD2")
    public Attribute.Double soilConcRD2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "water-use distribution parameter for Transpiration")
    public Attribute.Double BetaW;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "intfiltration poritions for the single horizonts")
    public Attribute.DoubleArray infiltration_hor;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "percolation out of the single horizonts")
    public Attribute.DoubleArray perco_hor;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "evapotranspiration out of the single horizonts")
    public Attribute.DoubleArray actETP_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "mps diffusion between layers value")
    public Attribute.DoubleArray w_layer_diff;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Array of state variables LAI ")
    public Attribute.Double LAI;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "estimated hydraulicconductivity in cm/d")
    public Attribute.Double Kf_geo;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "in cm/d soil hydraulic conductivity")
    public Attribute.DoubleArray kf_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "ID of soil")
    public Attribute.Double soilID;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Layer MPS diffusion factor > 1 [-]  default = 10")
    public Attribute.Double kdiff_layer;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Indicates whether roots can penetrate or not the soil layer [-]")
    public Attribute.DoubleArray root_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "max Root depth in soil in m")
    public Attribute.Double soil_root;

    //internal state variables
    double run_actDPS, run_latComp, run_vertComp,
            run_actETP, run_area, run_inRD1, run_outRD1, run_genRD1;
    double[] run_maxMPS, run_maxLPS, run_actMPS, run_actLPS, run_satMPS, run_satLPS, run_inRD2, run_satHor, run_outRD2, run_genRD2;
    int nhor;
    boolean debug;

    @Override
    public void run() {
        double balMPSstart = 0, balMPSend = 0;
        double balLPSstart = 0, balLPSend = 0;
        double balDPSstart = 0, balDPSend = 0;
        double balIn = 0;
        double balOut = 0;
        double balET = 0;

        debug = false;

        this.run_area = area.getValue();
        double run_slope = slope.getValue();

        this.nhor = (int) horizons.getValue();

        double[] infilhor = new double[nhor];
        double[] perchor = new double[nhor];
        double[] actETP_hor = new double[nhor];

        this.run_satHor = new double[nhor];

        this.run_maxMPS = maxMPS.getValue();
        this.run_maxLPS = maxLPS.getValue();
        this.run_actMPS = actMPS.getValue();
        this.run_actLPS = actLPS.getValue();
        this.run_satMPS = satMPS.getValue();
        this.run_satLPS = satLPS.getValue();
        this.run_actDPS = actDPS.getValue();

        this.run_inRD1 = inRD1.getValue();
        this.run_inRD2 = inRD2.getValue();
        double runkf_h[] = kf_h.getValue();

        balIn += this.run_inRD1;
        double run_inRain = netRain.getValue();
        double run_inSnow = netSnow.getValue();
        double run_potETP = potET.getValue();
        this.run_actETP = actET.getValue();
        double run_snowDepth = snowDepth.getValue();
        double run_snowMelt = snowMelt.getValue();

        this.run_genRD2 = new double[nhor];
        this.run_outRD2 = new double[nhor];
        this.run_latComp = 0;
        this.run_vertComp = 0;
        this.run_genRD1 = 0;
        this.run_outRD1 = 0;


        balET = this.run_actETP;
        balDPSstart = this.run_actDPS;

        for (int h = 0; h < nhor; h++) {
            /** determining inflow of infiltration to MPS */
            balIn += this.run_inRD2[h];
            balMPSstart += this.run_actMPS[h];
            balLPSstart += this.run_actLPS[h];
            this.run_genRD2[h] = 0;
            this.run_outRD2[h] = 0;
        }
        //calculation of saturations first
        //3-4% time
        this.calcSoilSaturations(false);
        
        double flux_h_h1[] = this.layer_diffusion();
        w_layer_diff.setValue(flux_h_h1);

        this.calcSoilSaturations(false);

        // redistributing RD1 and RD2 inflow of antecedent unit
        this.redistRD1_RD2_in();

        // calculation of ETP from dep. Storage and open water bodies
        this.calcPreInfEvaporation(run_potETP);
        double preinfep = this.run_actETP;

        // determining available water for infiltration
        double run_infiltration = run_inRain + run_inSnow + run_snowMelt + this.run_actDPS;
        //balance
        balIn += run_inRain + run_inSnow + run_snowMelt;

        if (run_infiltration < 0) {
            /*System.out.println("negative infiltration!");
            System.out.println("inRain: " + this.run_inRain);
            System.out.println("inSnow: " + this.run_inSnow);
            System.out.println("inSnowMelt: " + this.run_snowMelt);
            System.out.println("inDPS: " + this.run_actDPS);*/
        }              
        this.run_actDPS = 0;        
        //infiltration on impervious areas and water bodies
        //is directly routed as DirectRunoff to the next polygon
        // a better implementation would be the next river reach */
        double run_overlandflow = this.calcOverlandflow(sealedGrade.getValue(), run_infiltration);
        run_infiltration = this.calcInfImperv(sealedGrade.getValue(), run_infiltration);      
        //determining maximal infiltration rate
        double maxInf = this.calcMaxInfiltration(run_snowDepth, runkf_h[0]);
        if (maxInf < run_infiltration) {
            //System.out.getRuntime().println("maxInf:");
            double deltaInf = run_infiltration - maxInf;
            this.run_actDPS = this.run_actDPS + deltaInf;
            run_infiltration = maxInf;
        }
        //5%
        double horETP[] = this.calcMPSEvapotranslayer(true, run_potETP, nhor);
        //5% time
        for (int h = 0; h < nhor; h++) {
            // determining inflow of infiltration to MPS
            infilhor[h] = run_infiltration;
            run_infiltration = this.calcMPSInflow(run_infiltration, h);
            run_infiltration = this.calcLPSInflow(run_infiltration, h);
            infilhor[h] -= run_infiltration;
        }

        if (run_infiltration > 0) {            
            this.run_actDPS += run_infiltration;            
        }

        // 50% time
        for (int h = 0; h < this.nhor; h++) {
            // determining inflow of infiltration to MPS
            //this.run_infiltration = this.calcMPSInflow(this.run_infiltration, h);
            //distributing vertComp from antecedent horzion
            this.run_vertComp = this.calcMPSInflow(this.run_vertComp, h);            
            //this.run_vertComp = this.calcLPSInflow(this.run_vertComp, h);
            // determining transpiration from MPS
            this.calcMPSTranspiration(false, horETP[h], h);
            actETP_hor[h] = run_actETP;
            // inflow to LPS
            this.run_vertComp = this.calcLPSInflow(this.run_vertComp, h);
            if (this.run_vertComp > 0) {
                //System.out.getRuntime().println("VertIn is not zero!");
                //we put it back where it came from, the horizon above!
                this.run_vertComp = this.calcMPSInflow(this.run_vertComp, h - 1);
                this.run_vertComp = this.calcLPSInflow(this.run_vertComp, h - 1);                
            }            
            // determining outflow from LPS
            double MobileWater = 0;
            if (this.run_actLPS[h] > 0) {
                MobileWater = this.calcLPSoutflow(h);
            } 
            // Distribution of MobileWater to the lateral (interflow) and
            // vertical (percolation) flowpaths
            this.calcIntfPercRates(MobileWater, runkf_h, run_slope, h);

            perchor[h] = run_vertComp;
            //determining internal area routing
            this.calcRD2_out(h);

            //determining diffusion from LPS to MPS
            this.calcDiffusion(h);            
        }

        //determining direct runoff from depression storage 
        run_overlandflow += this.calcDirectRunoff(run_slope);
        this.calcRD1_out(run_overlandflow);

        double sumactETP = 0;
        for (int h = 0; h < nhor; h++) {
            balMPSend += this.run_actMPS[h];
            balLPSend += this.run_actLPS[h];
            balOut += this.run_outRD2[h];
            sumactETP += actETP_hor[h];
        }
        balDPSend = this.run_actDPS;
        balET = sumactETP + preinfep;
        balOut += balET;
        balOut += this.run_outRD1;
        balOut += this.run_vertComp;
        
        double balance = balIn + (balMPSstart - balMPSend) + (balLPSstart - balLPSend) + (balDPSstart - balDPSend) - balOut;
        if (Math.abs(balance) > 0.00001) //System.out.println("balance error at : " + time.toString() + " --> "+ balance + " in entity: " + entities.getCurrent().getId());
        {
            satMPS.setValue(this.run_satMPS);
        }
        satLPS.setValue(this.run_satLPS);
        actMPS.setValue(this.run_actMPS);
        actLPS.setValue(this.run_actLPS);
        actDPS.setValue(this.run_actDPS);
        actET.setValue(balET);
        inRD1.setValue(this.run_inRD1);
        inRD2.setValue(this.run_inRD2);
        outRD1.setValue(this.run_outRD1);
        outRD2.setValue(this.run_outRD2);
        genRD1.setValue(this.run_genRD1);
        genRD2.setValue(this.run_genRD2);
        percolation.setValue(this.run_vertComp);
        interflow.setValue(this.run_latComp);
        infiltration_hor.setValue(infilhor);
        perco_hor.setValue(perchor);
        actETP_h.setValue(actETP_hor);

        calcSoilSaturationsOutput();        
    }
    @Override
    public void cleanup() {
    }

    private double calcRunSatSoil1() {
        double soilMaxMps = 0;
        double soilActMps = 0;
        double soilMaxLps = 0;
        double soilActLps = 0;

        for (int h = 0; h < nhor; h++) {
            soilMaxMps += this.run_maxMPS[h];
            soilActMps += this.run_actMPS[h];
            soilMaxLps += this.run_maxLPS[h];
            soilActLps += this.run_actLPS[h];
        }

        if (((soilMaxLps > 0) | (soilMaxMps > 0)) & ((soilActLps > 0) | (soilActMps > 0))) {
            return ((soilActLps + soilActMps) / (soilMaxLps + soilMaxMps));
        }
        return 0;
    }

    /*private double calcTopSatsoil() {
        double soilMaxMps = 0;
        double soilActMps = 0;
        double soilMaxLps = 0;
        double soilActLps = 0;

        double upperMaxMps = 0;
        double upperActMps = 0;
        double upperMaxLps = 0;
        double upperActLps = 0;

        double[] infil_depth = new double[nhor];
        double partdepth = 0;
        double soilinfil = 50;

        for (int h = 0; h < nhor; h++) {
            infil_depth[h] += layerdepth.getValue()[h];
            if (infil_depth[h] <= soilinfil || h == 0) {
                upperMaxMps += this.run_maxMPS[h] * layerdepth.getValue()[h];
                upperActMps += this.run_actMPS[h] * layerdepth.getValue()[h];
                upperMaxLps += this.run_maxLPS[h] * layerdepth.getValue()[h];
                upperActLps += this.run_actLPS[h] * layerdepth.getValue()[h];
                partdepth += layerdepth.getValue()[h];
            } else if (infil_depth[h - 1] <= soilinfil) {
                double lowpart = soilinfil - partdepth;
                upperMaxMps += this.run_maxMPS[h] * lowpart;
                upperActMps += this.run_actMPS[h] * lowpart;
                upperMaxLps += this.run_maxLPS[h] * lowpart;
                upperActMps += this.run_actLPS[h] * lowpart;
            }
            soilMaxMps += this.run_maxMPS[h];
            soilActMps += this.run_actMPS[h];
            soilMaxLps += this.run_maxLPS[h];
            soilActLps += this.run_actLPS[h];
        }

        if (((soilMaxLps > 0) | (soilMaxMps > 0)) & ((soilActLps > 0) | (soilActMps > 0))) {
            return ((upperActLps + upperActMps) / (upperMaxLps + upperMaxMps));
        }
        return 0;
    }*/

    private void calcSoilSaturationsOutput() {
        double soilMaxMps = 0;
        double soilActMps = 0;
        double soilMaxLps = 0;
        double soilActLps = 0;
        double soilSatMps = 0;
        double soilSatLps = 0;

        for (int h = 0; h < nhor; h++) {            
            soilMaxMps += this.run_maxMPS[h];
            soilActMps += this.run_actMPS[h];
            soilMaxLps += this.run_maxLPS[h];
            soilActLps += this.run_actLPS[h];
        }

        if (((soilMaxLps > 0) | (soilMaxMps > 0)) & ((soilActLps > 0) | (soilActMps > 0))) {
            soilSatMps = (soilActMps / soilMaxMps);
            soilSatLps = (soilActLps / soilMaxLps);
        }
        soilMaxMPS.setValue(soilMaxMps);
        soilMaxLPS.setValue(soilMaxLps);
        soilActMPS.setValue(soilActMps);
        soilActLPS.setValue(soilActLps);
        soilSatMPS.setValue(soilSatMps);
        soilSatLPS.setValue(soilSatLps);
    }

    private void calcSoilSaturation(int h, boolean debug) {
        if ((this.run_actLPS[h] > 0) && (this.run_maxLPS[h] > 0)) {
            this.run_satLPS[h] = this.run_actLPS[h] / this.run_maxLPS[h];
        } else {
            this.run_satLPS[h] = 0;
        }

        if ((this.run_actMPS[h] > 0) && (this.run_maxMPS[h] > 0)) {
            this.run_satMPS[h] = this.run_actMPS[h] / this.run_maxMPS[h];
        } else {
            this.run_satMPS[h] = 0;
        }

        if (((this.run_maxLPS[h] > 0) | (this.run_maxMPS[h] > 0)) & ((this.run_actLPS[h] > 0) | (this.run_actMPS[h] > 0))) {
            this.run_satHor[h] = ((this.run_actLPS[h] + this.run_actMPS[h]) / (this.run_maxLPS[h] + this.run_maxMPS[h]));
        } else {
            //this.run_satSoil1 = 0;
        }
    }

    private void calcSoilSaturations(boolean debug) {        
        for (int h = 0; h < nhor; h++) {
            calcSoilSaturation(h, debug);
        }        
    }

    private void redistRD1_RD2_in() {
        //RD1 is put to DPS first
        if (this.run_inRD1 > 0) {
            this.run_actDPS += this.run_inRD1;
            this.run_inRD1 = 0;
        }

        for (int h = 0; h < this.nhor; h++) {
            if (this.run_inRD2[h] > 0) {
                this.run_inRD2[h] = this.calcMPSInflow(this.run_inRD2[h], h);
                this.run_inRD2[h] = this.calcLPSInflow(this.run_inRD2[h], h);
                if (this.run_inRD2[h] > 0) {
                    //System.out.getRuntime().println("RD2 of entity " + entity.getDouble("ID") + " and horizon " + h +  " is routed through RD2out: "+this.run_inRD2[h]);
                    this.run_outRD2[h] += this.run_inRD2[h];
                    this.run_inRD2[h] = 0;
                }
            }
        }        
    }

    private double[] layer_diffusion() {
        double flux_h_h1[] = new double[this.nhor - 1];

        for (int h = 0; h < this.nhor - 1; h++) {
            //calculate diffussion factor - order horizontal
            //diffusion only occur when gravitative flux is not dominating
            if ((run_satLPS[h] < 0.05) && (run_satMPS[h] < 0.8 || run_satMPS[h + 1] < 0.8) && (run_satMPS[h] > 0 || run_satMPS[h + 1] > 0)) {
                //calculate gradient
                double gradient_h_h1 = (Math.log10(2 - this.run_satMPS[h]) - Math.log10(2 - this.run_satMPS[h + 1]));

                //calculate resistance

                double satbalance = Math.pow((Math.pow(this.run_satMPS[h], 2) + (Math.pow(this.run_satMPS[h + 1], 2))) / 2.0, 0.5);

                double resistance_h_h1 = Math.log10(satbalance) * -kdiff_layer.getValue();
                //calculate amount of water to equilize saturations in layers

                double avg_sat = ((this.run_maxMPS[h] * this.run_satMPS[h]) + (this.run_maxMPS[h + 1] * this.run_satMPS[h + 1])) / (this.run_maxMPS[h] + this.run_maxMPS[h + 1]);
                double pot_flux = Math.abs((avg_sat - this.run_satMPS[h]) * this.run_maxMPS[h]);

                //calculate water fluxes
                double flux = (pot_flux * gradient_h_h1 / resistance_h_h1);

                if (flux >= 0) {
                    flux_h_h1[h] = Math.min(flux, pot_flux);
                } else {
                    flux_h_h1[h] = Math.max(flux, -pot_flux);
                }
            } else {
                flux_h_h1[h] = 0;
            }
            this.run_actMPS[h] += flux_h_h1[h];
            this.run_actMPS[h + 1] -= flux_h_h1[h];
        }
        return flux_h_h1;
    }

    private void calcPreInfEvaporation(double run_potETP) {
        double deltaETP = run_potETP - this.run_actETP;
        this.run_actETP = run_potETP;

        if (this.run_actDPS > 0) {
            if (this.run_actDPS >= deltaETP) {
                this.run_actDPS -= deltaETP;
            } else {
                this.run_actETP -= (deltaETP - this.run_actDPS);
                this.run_actDPS = 0;
            }
        }
        /** @todo implementation for open water bodies has to be implemented here */
    }

    private double calcOverlandflow(double sealedGrade, double run_infiltration) {
        if (sealedGrade > 0.8) {
            return (1 - soilImpGT80.getValue()) * run_infiltration;
        } else if (sealedGrade > 0 && sealedGrade <= 0.8) {
            return (1 - soilImpLT80.getValue()) * run_infiltration;
        }
        return 0;
    }

    private double calcInfImperv(double sealedGrade, double run_infiltration) {
        double infiltration_result=0;
        if (sealedGrade > 0.8) {            
            return run_infiltration * soilImpGT80.getValue();
        } else if (sealedGrade > 0 && sealedGrade <= 0.8) {            
            return run_infiltration * soilImpLT80.getValue();
        }
        return 0;
    }

    private double calcMaxInfiltration(double run_snowDepth, double runkf_h0) {
        if (run_snowDepth > 0) {
            return this.soilMaxInfSnow.getValue() * runkf_h0 * this.run_area;
        } else {
            return (1 - calcRunSatSoil1()) * soilMaxInfWinter.getValue() * runkf_h0 * this.run_area;
        }
    }

    private double[] calcMPSEvapotranslayer(boolean debug, double run_potETP, int nhor) { //author: Manfred Fink; Method after SWAT
        double[] horETP_local = new double[nhor];
        double pTransp = 0;
        double pEvap = 0;
        double deltaETP = run_potETP - this.run_actETP;
        double soilroot = 0;
        double runrootdepth = (rootdepth.getValue() * 1000) + 10;
        // drifferentiation between evaporation and transpiration
        pTransp = deltaETP;
        double runLAI = LAI.getValue();
        if (runLAI <= 3) {
            pTransp = (deltaETP * runLAI) / 3;
        }
        pEvap = deltaETP - pTransp;
        // EvapoTranspiration loop 1: calculating layer poritions within rootdepth
        for (int i = 0; i < nhor; i++) {
            if (root_h.getValue()[i] == 1.0) {
                soilroot += layerdepth.getValue()[i] * 10;
            }
        }
        double[] transp_hord = new double[nhor];
        double[] evapo_hord = new double[nhor];
        double runlayerdepth = 0;
        for (int i = 0; i < nhor; i++) {
            runlayerdepth += layerdepth.getValue()[i] * 10;
            runrootdepth = Math.min(runrootdepth, soilroot);

            // Transpiration loop 2: calculating transpiration distribution function with depth in layers
            transp_hord[i] = (pTransp * (1 - Math.exp(-BetaW.getValue() * (runlayerdepth / runrootdepth)))) / (1 - Math.exp(-BetaW.getValue()));
            transp_hord[i] = Math.min(transp_hord[i], pTransp);
            // Evaporation loop 2: calculating evaporation distribution function with depth in layers
            evapo_hord[i] = pEvap * (runlayerdepth / (runlayerdepth + (Math.exp(2.374 - (0.00713 * runlayerdepth)))));
            evapo_hord[i] = Math.min(evapo_hord[i], pEvap);

            //allocation of the rest Evap to the lowest horizon ............
            if (i == nhor - 1) {
                evapo_hord[i] = pEvap;
                transp_hord[i] = pTransp;
            }
        }


        double test = 0;
        double horbal = 0;
        for (int i = 0; i < nhor; i++) {
            double transp_hor = transp_hord[i];
            double evapo_hor = evapo_hord[i];
            if (i > 0) {
                transp_hor -= transp_hord[i - 1];
                evapo_hor -= evapo_hord[i - 1];
            }
            horETP_local[i] = transp_hor + evapo_hor;

            if (debug) {
                horbal = horbal + horETP_local[i];
                test = deltaETP - horbal;
            }
        }

        if ((test > 0.0000001 || test < -0.0000001) && debug) {
            //System.out.println("evaporation balance error = " + test);
        }
        this.soil_root.setValue(soilroot / 1000);
        return horETP_local;
    }

    private void calcMPSTranspiration(boolean debug, double horETP, int hor) {
        double maxTrans = 0;
        /** delta ETP */
        double deltaETP = horETP;

        /**linear reduction after MENZEL 1997 was chosen*/
        //if(this.etp_reduction == 0){
        if (this.soilLinRed.getValue() > 0) {
            /** reduction if actual saturation is smaller than linear factor */
            if (this.run_satMPS[hor] < soilLinRed.getValue()) {
                //if(this.sat_MPS < this.etp_linRed){
                double reductionFactor = this.run_satMPS[hor] / soilLinRed.getValue();

                //double reductionFactor = this.sat_MPS / etp_linRed;
                maxTrans = deltaETP * reductionFactor;
            } else {
                maxTrans = deltaETP;
            }
        } /** polynomial reduction after KRAUSE 2001 was chosen */
        else if (soilPolRed.getValue() > 0) {
            //else if(this.etp_reduction == 1){
            double sat_factor = -10. * Math.pow((1 - this.run_satMPS[hor]), soilPolRed.getValue());
            //double sat_factor = Math.pow((1 - this.sat_MPS), etp_polRed);
            double reductionFactor = Math.pow(10, sat_factor);
            maxTrans = Math.min(deltaETP * reductionFactor, deltaETP);
        }
        maxTrans = Math.min(maxTrans, run_actMPS[hor]);

        this.run_actMPS[hor] -= maxTrans;

        /** recalculation actual ETP */
        this.run_actETP = maxTrans;
        this.calcSoilSaturation(hor,debug);

        /* @todo: ETP from water bodies has to be implemented here */
    }

    private double calcMPSInflow(double infiltration, int hor) {
        double inflow = infiltration;

        /**checking if MPS can take all the water */
        if (inflow < (this.run_maxMPS[hor] - this.run_actMPS[hor])) {
            /** if MPS is empty it takes all the water */
            if (this.run_actMPS[hor] == 0) {
                this.run_actMPS[hor] += inflow;
                inflow = 0;
            } /** MPS is partly filled and gets part of the water */
            else {
                double alpha = this.soilDistMPSLPS.getValue();
                //if sat_MPS is 0 the next equation would produce an error,
                //therefore it is set to MPS_sat is set to 0.0000001 in that case
                if (this.run_satMPS[hor] == 0) {
                    this.run_satMPS[hor] = 0.0000001;
                }
                double inMPS = inflow * (1. - Math.exp(-1 * alpha / this.run_satMPS[hor]));
                this.run_actMPS[hor] += inMPS;
                inflow -= inMPS;
            }
        } /** infiltration exceeds storage capacity of MPS */
        else {
            double deltaMPS = this.run_maxMPS[hor] - this.run_actMPS[hor];
            this.run_actMPS[hor] = this.run_maxMPS[hor];
            inflow -= deltaMPS;
        }
        /** updating saturations */
        this.calcSoilSaturation(hor,false);
        return inflow;
    }
    /*
     *problem overflow is put to DPS, we have to deal with that problem
     */

    private double calcLPSInflow(double infiltration, int hor) {
        this.run_actLPS[hor] += infiltration;
        infiltration = 0;
        /** if LPS is saturated depression Storage occurs */
        if (this.run_actLPS[hor] > this.run_maxLPS[hor]) {
            infiltration = (this.run_actLPS[hor] - this.run_maxLPS[hor]);
            this.run_actLPS[hor] = this.run_maxLPS[hor];
        }
        /** updating saturations */
        calcSoilSaturation(hor,false);
        return infiltration;
    }

    private double calcLPSoutflow(int hor) {
        double alpha = this.soilOutLPS.getValue();

        if (this.run_satLPS[hor] == 1.0) {
            this.run_satLPS[hor] = 0.999999;
        }

        double LPSoutflow = Math.pow(this.run_satHor[hor], alpha) * this.run_actLPS[hor];
        LPSoutflow = Math.min(LPSoutflow, this.run_actLPS[hor]);
        LPSoutflow = Math.max(LPSoutflow, 0);

        this.run_actLPS[hor] -= LPSoutflow;

        return LPSoutflow;
    }

    private void calcIntfPercRates(double MobileWater, double runkf_h[], double run_slope, int hor) {
        if (MobileWater > 0) {
            double slope_weight = (Math.tan(run_slope * (Math.PI / 180.))) * this.soilLatVertLPS.getValue();

            /** potential part of percolation */
            double part_perc = (1 - slope_weight);
            part_perc = Math.max(part_perc, 0);
            part_perc = Math.min(part_perc, 1);

            /** potential part of interflow */
            double part_intf = (1 - part_perc);

            this.run_latComp += MobileWater * part_intf;
            this.run_vertComp += MobileWater * part_perc;
            double maxPerc = 0;
            /** checking if percolation rate is limited by parameter */
            if (hor == nhor - 1) {
                maxPerc = this.geoMaxPerc.getValue() * this.run_area * this.Kf_geo.getValue() / 86.4;
                /*if (Kf_geo.getValue() < 10){
                maxPerc = 0;
                }*/
                // 86.4 cm/d "middle" hydraulic conductivity in geology (1 E-5 m/s)
                if (this.run_vertComp > maxPerc) {
                    double rest = this.run_vertComp - maxPerc;
                    this.run_vertComp = maxPerc;
                    this.run_latComp = this.run_latComp + rest;
                }
            } else {
                maxPerc = this.soilMaxPerc.getValue() * this.run_area * runkf_h[hor + 1] / 86.4;
                // 86.4 cm/d "middle" hydraulic conductivity in geology (1 E-5 m/s)
                if (this.run_vertComp > maxPerc) {
                    double rest = this.run_vertComp - maxPerc;
                    this.run_vertComp = maxPerc;
                    this.run_latComp = this.run_latComp + rest;
                }
            }
        } /** no MobileWater available */
        else {
            this.run_latComp = 0;
            this.run_vertComp = 0;
        }
    }

    private double calcDirectRunoff(double run_slope) {
        double directRunoff = 0;
        if (this.run_actDPS > 0) {
            double maxDep = this.soilMaxDPS.getValue() * this.run_area;
            /** depression storage on slopes is half the normal dep. storage */
            if (run_slope > 5.0) {
                maxDep /= 2.0;
            }

            if (this.run_actDPS > maxDep) {
                directRunoff = this.run_actDPS - maxDep;
                this.run_actDPS = maxDep;
            }
        }
        if (directRunoff < 0) {
            //System.out.println("directRunoff is negative! --> " + directRunoff );
        }
        return directRunoff;
    }

    private void calcRD2_out(int h) {
        /** lateral interflow */
        //switched of 15-03-2004
        //double RD2_output_factor = this.conc_index / this.parameter.getDouble("conc_recRD2");
        double RD2_output_factor = 1. / this.soilConcRD2.getValue();
        RD2_output_factor = Math.max(RD2_output_factor, 0);
        RD2_output_factor = Math.min(RD2_output_factor, 1);

        if (RD2_output_factor > 1) {
            RD2_output_factor = 1;
        } else if (RD2_output_factor < 0) {
            RD2_output_factor = 0;
        }

        /** real RD2 output */
        double RD2_output = this.run_latComp * RD2_output_factor;
        /** rest is put back to LPS Storage */
        this.run_actLPS[h] += (this.run_latComp - RD2_output);
        this.run_outRD2[h] += RD2_output;
        this.run_genRD2[h] = this.run_outRD2[h];// - this.in_RD2;
        if (this.run_genRD2[h] < 0) {
            this.run_genRD2[h] = 0;
        }

        this.run_latComp = 0;
    }

    private void calcRD1_out(double run_overlandflow) {
        /** DIRECT OVERLANDFLOW */
        //switched off 15-03-2004
        //double RD1_output_factor = this.conc_index / this.parameter.getDouble("conc_recRD1");
        double RD1_output_factor = 1. / this.soilConcRD1.getValue();
        RD1_output_factor = Math.max(RD1_output_factor, 0);
        RD1_output_factor = Math.min(RD1_output_factor, 1);

        double RD1_output = run_overlandflow * RD1_output_factor;
        /** rest is put back to dep. Storage */
        this.run_actDPS += (run_overlandflow - RD1_output);
        this.run_outRD1 += RD1_output;
        this.run_genRD1 = this.run_outRD1;       
    }

    private void calcDiffusion(int h) {
        double diffusion = 0;       
        double deltaMPS = this.run_maxMPS[h] - this.run_actMPS[h];
        //if sat_MPS is 0 the diffusion equation would produce an error,
        //for this (unlikely) case diffusion is set to zero
        if (this.run_satMPS[h] != 0.0) {
            double diff = this.soilDiffMPSLPS.getValue();
            //new equation like all other exps 04.03.04
            diffusion = this.run_actLPS[h] * (1. - Math.exp((-1. * diff) / this.run_satMPS[h]));
        }
        if (diffusion > this.run_actLPS[h]) {
            diffusion = this.run_actLPS[h];
        }
        /** MPS can take all the water from diffusion */
        if (diffusion < deltaMPS) {
            this.run_actMPS[h] = this.run_actMPS[h] + diffusion;
            this.run_actLPS[h] = this.run_actLPS[h] - diffusion;
        } /** MPS can take only part of the water */
        else {
            double rest = this.run_maxMPS[h] - this.run_actMPS[h];
            this.run_actMPS[h] = this.run_maxMPS[h];
            this.run_actLPS[h] = this.run_actLPS[h] - rest;
        }
        calcSoilSaturation(h,debug);
    }
    static Random generator;

    public static double rnd(double low, double high) {
        return generator.nextDouble() * (high - low) + low;
    }

    public static void main(String[] args) throws Exception {
        long time_ref = 0;
        long time_test = 0;
        double diff = 0;
        generator = new Random(0);
        for (int x = 0; x < 10000; x++) {
            J2KProcessLayeredSoilWater2008_Christian test = new J2KProcessLayeredSoilWater2008_Christian();
            J2KProcessLayeredSoilWater2008 ref = new J2KProcessLayeredSoilWater2008();

            int horz = generator.nextInt(20) + 1;

            ref.time = DefaultDataFactory.getDataFactory().createCalendar();
            ref.time.set(2000, 10, 10, 1, 1, 1);
            test.time = ref.time;

            ref.soil_root = DefaultDataFactory.getDataFactory().createDouble();
            test.soil_root = DefaultDataFactory.getDataFactory().createDouble();

            ref.soilMaxMPS = DefaultDataFactory.getDataFactory().createDouble();
            test.soilMaxMPS = DefaultDataFactory.getDataFactory().createDouble();

            ref.soilMaxLPS = DefaultDataFactory.getDataFactory().createDouble();
            test.soilMaxLPS = DefaultDataFactory.getDataFactory().createDouble();

            ref.soilActMPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilActMPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();

            ref.soilActLPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilActLPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();

            ref.soilSatMPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilSatMPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();

            ref.soilSatLPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilSatLPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();

            ref.infiltration = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.infiltration = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();

            ref.interflow = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.interflow = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();

            ref.percolation = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.percolation = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();

            ref.outRD1 = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.outRD1 = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();

            ref.genRD1 = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.genRD1 = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();

            double area = rnd(10, 100000);
            ref.area = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.area = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.area.setValue(area);
            test.area.setValue(area);

            double slope = rnd(0.01, 2);
            ref.slope = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.slope = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.slope.setValue(slope);
            test.slope.setValue(slope);

            double sealedGrade = rnd(0.01, 2);
            ref.sealedGrade = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.sealedGrade = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.sealedGrade.setValue(sealedGrade);
            test.sealedGrade.setValue(sealedGrade);

            double netRain = rnd(0.0, 200);
            if (netRain > 150) {
                netRain = 0;
            }
            ref.netRain = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.netRain = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.netRain.setValue(netRain);
            test.netRain.setValue(netRain);

            double netSnow = rnd(0.0, 200);
            ref.netSnow = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.netSnow = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            if (netSnow > 100) {
                netSnow = 0;
            }

            ref.netSnow.setValue(netSnow);
            test.netSnow.setValue(netSnow);

            double potET = rnd(0.0, 50);
            ref.potET = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.potET = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.potET.setValue(potET);
            test.potET.setValue(potET);

            double actET = rnd(0.0, 50);
            ref.actET = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.actET = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.actET.setValue(actET);
            test.actET.setValue(actET);

            double snowDepth = rnd(0.0, 100);
            ref.snowDepth = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.snowDepth = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.snowDepth.setValue(snowDepth);
            test.snowDepth.setValue(snowDepth);

            double snowMelt = rnd(0.0, 50);
            ref.snowMelt = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.snowMelt = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.snowMelt.setValue(snowMelt);
            test.snowMelt.setValue(snowMelt);

            ref.horizons = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.horizons = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.horizons.setValue(horz);
            test.horizons = ref.horizons;

            double layerdepth[] = new double[horz];
            double maxMPS[] = new double[horz];
            double maxLPS[] = new double[horz];
            double actMPS[] = new double[horz];
            double actLPS[] = new double[horz];
            double satMPS[] = new double[horz];
            double satLPS[] = new double[horz];
            double kf_h[] = new double[horz];
            double root_h[] = new double[horz];
            double in_RD2[] = new double[horz];
            double out_RD2[] = new double[horz];

            for (int i = 0; i < horz; i++) {
                layerdepth[i] = rnd(1, 100);
                maxMPS[i] = rnd(-0.01, 10);
                maxLPS[i] = rnd(-0.01, 10);

                satMPS[i] = rnd(-0.01, 1);
                satLPS[i] = rnd(-0.01, 1);

                actMPS[i] = rnd(-0.01, maxMPS[i]);
                actLPS[i] = rnd(-0.01, maxLPS[i]);

                kf_h[i] = rnd(0.05, 10.0);
                in_RD2[i] = rnd(0.05, 20.0);

                root_h[i] = generator.nextInt(2);
            }

            ref.inRD2 = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            test.inRD2 = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            ref.inRD2.setValue(in_RD2);
            test.inRD2.setValue(in_RD2.clone());

            ref.infiltration_hor = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            test.infiltration_hor = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            ref.infiltration_hor.setValue(new double[horz]);
            test.infiltration_hor.setValue(new double[horz]);

            ref.perco_hor = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            test.perco_hor = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            ref.perco_hor.setValue(new double[horz]);
            test.perco_hor.setValue(new double[horz]);

            ref.actETP_h = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            test.actETP_h = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            ref.actETP_h.setValue(new double[horz]);
            test.actETP_h.setValue(new double[horz]);

            ref.w_layer_diff = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            test.w_layer_diff = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            ref.w_layer_diff.setValue(new double[horz]);
            test.w_layer_diff.setValue(new double[horz]);

            ref.outRD2 = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            test.outRD2 = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            ref.outRD2.setValue(new double[horz]);
            test.outRD2.setValue(new double[horz]);

            ref.genRD2 = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            test.genRD2 = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            ref.genRD2.setValue(new double[horz]);
            test.genRD2.setValue(new double[horz]);



            ref.layerdepth = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            test.layerdepth = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            ref.layerdepth.setValue(layerdepth);
            test.layerdepth.setValue(layerdepth.clone());

            double rootdepth = rnd(0.0, 10);
            ref.rootdepth = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.rootdepth = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.rootdepth.setValue(rootdepth);
            test.rootdepth.setValue(rootdepth);

            ref.maxMPS = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            test.maxMPS = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            ref.maxMPS.setValue(maxMPS);
            test.maxMPS.setValue(maxMPS.clone());

            ref.maxLPS = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            test.maxLPS = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            ref.maxLPS.setValue(maxLPS);
            test.maxLPS.setValue(maxLPS.clone());

            ref.actMPS = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            test.actMPS = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            ref.actMPS.setValue(actMPS);
            test.actMPS.setValue(actMPS.clone());

            ref.actLPS = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            test.actLPS = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            ref.actLPS.setValue(actLPS);
            test.actLPS.setValue(actLPS.clone());

            ref.satMPS = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            test.satMPS = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            ref.satMPS.setValue(satMPS);
            test.satMPS.setValue(satMPS.clone());

            ref.satLPS = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            test.satLPS = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            ref.satLPS.setValue(satLPS);
            test.satLPS.setValue(satLPS.clone());

            double actDPS = rnd(0.0, 10);
            ref.actDPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.actDPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.actDPS.setValue(actDPS);
            test.actDPS.setValue(actDPS);

            double satSoil = rnd(0.0, 10);
            ref.satSoil = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.satSoil = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.satSoil.setValue(satSoil);
            test.satSoil.setValue(satSoil);

            double inRD1 = rnd(0.0, 50);
            ref.inRD1 = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.inRD1 = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.inRD1.setValue(inRD1);
            test.inRD1.setValue(inRD1);

            double soilMaxDPS = rnd(0.0, 50);
            ref.soilMaxDPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilMaxDPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.soilMaxDPS.setValue(soilMaxDPS);
            test.soilMaxDPS.setValue(soilMaxDPS);

            double soilID = rnd(0.0, 50);
            ref.soilID = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilID = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.soilID.setValue(soilMaxDPS);
            test.soilID.setValue(soilMaxDPS);

            double soilPolRed = rnd(0.05, 10);
            ref.soilPolRed = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilPolRed = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.soilPolRed.setValue(soilPolRed);
            test.soilPolRed.setValue(soilPolRed);

            double soilLinRed = rnd(0.05, 10);
            ref.soilLinRed = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilLinRed = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.soilLinRed.setValue(soilLinRed);
            test.soilLinRed.setValue(soilLinRed);

            double soilMaxInfSummer = rnd(10.05, 250);
            ref.soilMaxInfSummer = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilMaxInfSummer = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.soilMaxInfSummer.setValue(soilMaxInfSummer);
            test.soilMaxInfSummer.setValue(soilMaxInfSummer);

            double soilMaxInfWinter = rnd(10.05, 250);
            ref.soilMaxInfWinter = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilMaxInfWinter = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.soilMaxInfWinter.setValue(soilMaxInfWinter);
            test.soilMaxInfWinter.setValue(soilMaxInfWinter);

            double soilMaxInfSnow = rnd(0.05, 50);
            ref.soilMaxInfSnow = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilMaxInfSnow = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.soilMaxInfSnow.setValue(soilMaxInfSnow);
            test.soilMaxInfSnow.setValue(soilMaxInfSnow);

            double soilImpGT80 = rnd(0.05, 50);
            ref.soilImpGT80 = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilImpGT80 = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.soilImpGT80.setValue(soilImpGT80);
            test.soilImpGT80.setValue(soilImpGT80);

            double soilImpLT80 = rnd(0.05, 50);
            ref.soilImpLT80 = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilImpLT80 = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.soilImpLT80.setValue(soilImpLT80);
            test.soilImpLT80.setValue(soilImpLT80);

            double soilDistMPSLPS = rnd(0.0, 1.0);
            ref.soilDistMPSLPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilDistMPSLPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.soilDistMPSLPS.setValue(soilDistMPSLPS);
            test.soilDistMPSLPS.setValue(soilDistMPSLPS);

            double soilDiffMPSLPS = rnd(0.0, 1.0);
            ref.soilDiffMPSLPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilDiffMPSLPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.soilDiffMPSLPS.setValue(soilDiffMPSLPS);
            test.soilDiffMPSLPS.setValue(soilDiffMPSLPS);

            double soilOutLPS = rnd(0.0, 1.0);
            ref.soilOutLPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilOutLPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.soilOutLPS.setValue(soilOutLPS);
            test.soilOutLPS.setValue(soilOutLPS);

            double soilLatVertLPS = rnd(0.0, 10.0);
            ref.soilLatVertLPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilLatVertLPS = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.soilLatVertLPS.setValue(soilLatVertLPS);
            test.soilLatVertLPS.setValue(soilLatVertLPS);

            double soilMaxPerc = rnd(5.0, 100.0);
            ref.soilMaxPerc = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilMaxPerc = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.soilMaxPerc.setValue(soilMaxPerc);
            test.soilMaxPerc.setValue(soilMaxPerc);

            double geoMaxPerc = rnd(5.0, 100.0);
            ref.geoMaxPerc = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.geoMaxPerc = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.geoMaxPerc.setValue(geoMaxPerc);
            test.geoMaxPerc.setValue(geoMaxPerc);

            double soilConcRD1 = rnd(0.0, 10.0);
            ref.soilConcRD1 = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilConcRD1 = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.soilConcRD1.setValue(soilConcRD1);
            test.soilConcRD1.setValue(soilConcRD1);

            double soilConcRD2 = rnd(0.0, 10.0);
            ref.soilConcRD2 = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.soilConcRD2 = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.soilConcRD2.setValue(soilConcRD2);
            test.soilConcRD2.setValue(soilConcRD2);

            double BetaW = rnd(0.05, 10.0);
            ref.BetaW = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.BetaW = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.BetaW.setValue(BetaW);
            test.BetaW.setValue(BetaW);

            double LAI = rnd(0.05, 10.0);
            ref.LAI = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.LAI = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.LAI.setValue(LAI);
            test.LAI.setValue(LAI);

            double Kf_geo = rnd(0.05, 10.0);
            ref.Kf_geo = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.Kf_geo = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.Kf_geo.setValue(Kf_geo);
            test.Kf_geo.setValue(Kf_geo);


            ref.kf_h = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            test.kf_h = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            ref.kf_h.setValue(kf_h);
            test.kf_h.setValue(kf_h);

            ref.root_h = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            test.root_h = (Attribute.DoubleArray) DefaultDataFactory.getDataFactory().createDoubleArray();
            ref.root_h.setValue(root_h);
            test.root_h.setValue(root_h);

            double kdiff_layer = rnd(1, 20.0);
            ref.kdiff_layer = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            test.kdiff_layer = (Attribute.Double) DefaultDataFactory.getDataFactory().createDouble();
            ref.kdiff_layer.setValue(kdiff_layer);
            test.kdiff_layer.setValue(kdiff_layer);

            if (generator.nextBoolean()) {
                long t1 = System.nanoTime();
                ref.run();
                long t2 = System.nanoTime();
                time_ref += (t2 - t1);

                t1 = System.nanoTime();
                test.run();
                t2 = System.nanoTime();
                time_test += (t2 - t1);
            } else {
                long t1 = System.nanoTime();
                test.run();
                long t2 = System.nanoTime();
                time_test += (t2 - t1);

                t1 = System.nanoTime();
                ref.run();
                t2 = System.nanoTime();
                time_ref += (t2 - t1);
            }


            double dnetRain = Math.abs(test.netRain.getValue() - ref.netRain.getValue());
            double dnetSnow = Math.abs(test.netSnow.getValue() - ref.netSnow.getValue());
            double dactET = Math.abs(test.actET.getValue() - ref.actET.getValue());
            double dactDPS = Math.abs(test.actDPS.getValue() - ref.actDPS.getValue());
            double dsatSoil = Math.abs(test.satSoil.getValue() - ref.satSoil.getValue());
            double dinRD1 = Math.abs(test.inRD1.getValue() - ref.inRD1.getValue());
            double dsoil_root = Math.abs(test.soil_root.getValue() - ref.soil_root.getValue());

            diff = diff + dnetRain + dnetSnow + dactET + dactDPS + dsatSoil + dinRD1 + dsoil_root;

            double dsoilMaxMPS = Math.abs(test.soilMaxMPS.getValue() - ref.soilMaxMPS.getValue());
            double dsoilMaxLPS = Math.abs(test.soilMaxLPS.getValue() - ref.soilMaxLPS.getValue());
            double dsoilActMPS = Math.abs(test.soilActMPS.getValue() - ref.soilActMPS.getValue());
            double dsoilActLPS = Math.abs(test.soilActLPS.getValue() - ref.soilActLPS.getValue());

            diff += dsoilMaxMPS + dsoilMaxLPS + dsoilActMPS + dsoilActLPS;

            double dsoilSatMPS = Math.abs(test.soilSatMPS.getValue() - ref.soilSatMPS.getValue());
            double dsoilSatLPS = Math.abs(test.soilSatLPS.getValue() - ref.soilSatLPS.getValue());
            double dinfiltration = Math.abs(test.infiltration.getValue() - ref.infiltration.getValue());
            double dpercolation = Math.abs(test.percolation.getValue() - ref.percolation.getValue());

            diff += dsoilSatMPS + dsoilSatLPS + dinfiltration + dpercolation;

            double doutRD1 = Math.abs(test.outRD1.getValue() - ref.outRD1.getValue());
            double dgenRD1 = Math.abs(test.genRD1.getValue() - ref.genRD1.getValue());

            diff += doutRD1 + dgenRD1;

            double dsatMPS = 0;
            double dsatLPS = 0;
            double dinRD2 = 0;
            double dinfiltration_hor = 0;
            double dperco_hor = 0;
            double dactETP_h = 0;
            double doutRD2 = 0;
            double dw_layer_diff = 0;
            double dgenRD2 = 0;

            for (int k = 0; k < horz; k++) {
                dsatMPS += Math.abs(test.satMPS.getValue()[k] - ref.satMPS.getValue()[k]);
                dsatLPS += Math.abs(test.satLPS.getValue()[k] - ref.satLPS.getValue()[k]);
                dinRD2 += Math.abs(test.inRD2.getValue()[k] - ref.inRD2.getValue()[k]);
                dinfiltration_hor += Math.abs(test.infiltration_hor.getValue()[k] - ref.infiltration_hor.getValue()[k]);
                dperco_hor += Math.abs(test.perco_hor.getValue()[k] - ref.perco_hor.getValue()[k]);
                dactETP_h += Math.abs(test.actETP_h.getValue()[k] - ref.actETP_h.getValue()[k]);
                if (k < horz - 1) {
                    dw_layer_diff += Math.abs(test.w_layer_diff.getValue()[k] - ref.w_layer_diff.getValue()[k]);
                }
                doutRD2 += Math.abs(test.outRD2.getValue()[k] - ref.outRD2.getValue()[k]);
                dgenRD2 += Math.abs(test.genRD2.getValue()[k] - ref.genRD2.getValue()[k]);
            }

            diff += dsatMPS + dsatLPS + dinRD2 + dinfiltration_hor + dperco_hor + dactETP_h + dw_layer_diff + doutRD2 + dgenRD2;
        }
        System.out.println("Time reference:" + time_ref);
        System.out.println("Time test:" + time_test);
        System.out.println("Speed-Up:" + (double) time_ref / time_test);
        System.out.println("Difference:" + diff);
    }
}
