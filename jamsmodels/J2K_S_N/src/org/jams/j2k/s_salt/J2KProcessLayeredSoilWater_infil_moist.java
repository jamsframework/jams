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
package org.jams.j2k.s_salt;


import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause modifications by Manfred Fink
 */
/*

 */
@JAMSComponentDescription(title = "J2KProcessLayeredSoilWater_infil_moist",
author = "Peter Krause, modifications by Manfred Fink",
description = "Calculates soil water balance for each HRU with vertical layers")
public class J2KProcessLayeredSoilWater_infil_moist extends JAMSComponent {

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
    description = "Switch between wet and dry infiltration (MPS saturation) (-)")
    public Attribute.Double MPS_thershold;
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
    /*    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
    description = "Transpiration compensation factor to calibrate 0 - 1 [-] default = 0"
    )
    public Attribute.Double esco;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
    description = "Evaporation compensation factor to calibrate 0 - 1 [-] default = 0"
    )
    public Attribute.Double epco; */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Layer MPS diffusion factor > 1 [-]  default = 10")
    public Attribute.Double kdiff_layer;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Indicates whether roots can penetrate or not the soil layer [-]")
    public Attribute.DoubleArray root_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "max Root depth in soil in m")
    public Attribute.Double soil_root;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "preferential flow factor [-] 0 - 1 default = 1")
    public Attribute.Double preffac;
    //internal state variables
    double run_actDPS, run_satSoil1, run_inRain, run_inSnow,
            run_snowMelt, run_infiltration, run_latComp, run_vertComp, run_overlandflow, run_potETP, run_actETP, run_snowDepth, run_area, run_slope,
            run_inRD1, soilSatMps, soilSatLps, soilActMps, soilActLps, soilMaxMps, soilMaxLps, run_outRD1, run_genRD1, lowpart, top_satsoil;
    double[] run_maxMPS, run_maxLPS, run_actMPS, run_actLPS, run_satMPS, run_satLPS, run_inRD2, run_satHor, run_outRD2, run_genRD2;
    double[] runlayerdepth, horETP, runkf_h, flux_h_h1;
    int nhor;
    boolean debug;

    /*
     *  Component run stages
     */
    public void init() {
    }

    public void run() {
        double balMPSstart = 0;
        double balMPSend = 0;
        double balLPSstart = 0;
        double balLPSend = 0;
        double balDPSstart = 0;
        double balDPSend = 0;
        double balIn = 0;
        double balOut = 0;
        double balET = 0;
        double sumactETP = 0;
        debug = false;

        //long entityID = entities.getCurrent().getId();
        //if(entityID == 43)
        //    System.out.println("stop!");

        //System.out.getRuntime().println("Processing HRU: " + entity.getDouble("ID"));
        //if(this.time.get(time.DATE) == 23 && this.time.get(time.MONTH)==10 && this.time.get(time.YEAR)==1989){
        //    if(entity.getDouble("ID") == 1027.0)
        //        System.out.getRuntime().println("stop!");
        // }
        this.run_area = area.getValue();
        this.run_slope = slope.getValue();

        this.nhor = (int) horizons.getValue();
        double[] infilhor = new double[nhor];
        double[] perchor = new double[nhor];
        double[] actETP_hor = new double[nhor];

        this.flux_h_h1 = new double[nhor - 1];
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
        this.runkf_h = kf_h.getValue();



        balIn += this.run_inRD1;
        this.run_inRain = netRain.getValue();
        this.run_inSnow = netSnow.getValue();
        this.run_potETP = potET.getValue();
        this.run_actETP = actET.getValue();
        this.run_snowDepth = snowDepth.getValue();
        this.run_snowMelt = snowMelt.getValue();

        this.runlayerdepth = new double[nhor];
        this.run_genRD2 = new double[nhor];
        this.run_outRD2 = new double[nhor];
        this.run_latComp = 0;
        this.run_vertComp = 0;
        this.run_genRD1 = 0;
        this.run_outRD1 = 0;
        this.lowpart = 0;
        this.top_satsoil = 0;

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
        this.calcSoilSaturations(false);

        this.layer_diffusion();

        w_layer_diff.setValue(flux_h_h1);

        this.calcSoilSaturations(false);

        /** redistributing RD1 and RD2 inflow of antecedent unit */
        this.redistRD1_RD2_in();

        /** calculation of ETP from dep. Storage and open water bodies */
        this.calcPreInfEvaporation();
        double preinfep = this.run_actETP;

        /** determining available water for infiltration */
        this.run_infiltration = this.run_inRain + this.run_inSnow
                + this.run_snowMelt
                + this.run_actDPS;

        if (this.run_infiltration < 0) {
            System.out.println("negative infiltration!");
            System.out.println("inRain: " + this.run_inRain);
            System.out.println("inSnow: " + this.run_inSnow);
            System.out.println("inSnowMelt: " + this.run_snowMelt);
            System.out.println("inDPS: " + this.run_actDPS);
        }
        //balance
        balIn += this.run_inRain;
        balIn += this.run_inSnow;
        balIn += this.run_snowMelt;


        this.run_actDPS = 0;
        this.run_inRain = 0;
        this.run_inSnow = 0;
        this.run_snowMelt = 0;

        /** infiltration on impervious areas and water bodies
         *  is directly routed as DirectRunoff to the next polygon
         *  a better implementation would be the next river reach */
        this.calcInfImperv(sealedGrade.getValue());
        this.calcSoilSaturations(false);
        /** determining maximal infiltration rate */
//        double maxInf = this.calcMaxInfiltration(time.get(Attribute.Calendar.DAY_OF_YEAR) + 1);
        double maxInf = this.calcMaxInfiltration();
        if (maxInf < this.run_infiltration) {
            //System.out.getRuntime().println("maxInf:");
            double deltaInf = this.run_infiltration - maxInf;
            this.run_actDPS = this.run_actDPS + deltaInf;
            this.run_infiltration = maxInf;
        }

        horETP = this.calcMPSEvapotranslayer(true, nhor);

        for (int h = 0; h < nhor; h++) {
            /** determining inflow of infiltration to MPS */
            //balMPSstart += this.run_actMPS[h];
            //balLPSstart += this.run_actLPS[h];
            double bak_infiltration = run_infiltration;
            this.run_infiltration = this.calcMPSInflow(this.run_infiltration, h);
            this.run_infiltration = this.calcLPSInflow(this.run_infiltration, h);
            infilhor[h] = (bak_infiltration - run_infiltration);
        }

        if (this.run_infiltration > 0) {
            //System.out.getRuntime().println("Infiltration after: " +  this.run_infiltration);
            this.run_actDPS += this.run_infiltration;
            this.run_infiltration = 0;
        }
        //balDPSstart = this.run_actDPS;
        for (int h = 0; h < this.nhor; h++) {




            /** determining inflow of infiltration to MPS */
            //this.run_infiltration = this.calcMPSInflow(this.run_infiltration, h);
            //distributing vertComp from antecedent horzion
            this.run_vertComp = this.calcMPSInflow(this.run_vertComp, h);
            //this.run_vertComp = this.calcLPSInflow(this.run_vertComp, h);
            /** determining transpiration from MPS */
            this.calcMPSTranspiration(false, h);
            actETP_hor[h] = run_actETP;
            /** inflow to LPS */
            //this.run_infiltration = this.calcLPSInflow(this.run_infiltration, h);
            this.run_vertComp = this.calcLPSInflow(this.run_vertComp, h);

            if (this.run_vertComp > 0) {
                //System.out.getRuntime().println("VertIn is not zero!");
                //we put it back where it came from, the horizon above!
                this.run_vertComp = this.calcMPSInflow(this.run_vertComp, h - 1);
                this.run_vertComp = this.calcLPSInflow(this.run_vertComp, h - 1);
                if (this.run_vertComp > 0) {
                    System.out.println("VertIn is still not zero!");
                }

            }
            /** updating saturations */
            this.calcSoilSaturations(false);

            /** determining outflow from LPS */
            double MobileWater = 0;
            if (this.run_actLPS[h] > 0) {
                MobileWater = this.calcLPSoutflow(h);
            } else {
                MobileWater = 0;
            }

            /** Distribution of MobileWater to the lateral (interflow) and
             * vertical (percolation) flowpaths  */
            //try {
            this.calcIntfPercRates(MobileWater, h);

            /*} catch (Exception e) {
            System.out.println("MIST");
            }*/




            perchor[h] = run_vertComp;


            /** determining internal area routing **/
            this.calcRD2_out(h);

            /** determining diffusion from LPS to MPS */
            this.calcDiffusion(h);

            /** updating saturations */
            this.calcSoilSaturations(false);
            //System.out.getRuntime().println("end of horizon loop");
        }
        if (this.run_overlandflow < 0) {
            System.out.println("overlandflow is negative! --> " + this.run_overlandflow);
        }
        /** determining direct runoff from depression storage */
        this.run_overlandflow = this.run_overlandflow + this.calcDirectRunoff();


//        if (run_overlandflow > 0)
//                System.out.println("test");
        this.calcRD1_out();

        for (int h = 0; h < nhor; h++) {
            balMPSend += this.run_actMPS[h];
            balLPSend += this.run_actLPS[h];
            balOut += this.run_outRD2[h];
            sumactETP += actETP_hor[h];
        }
        balDPSend = this.run_actDPS;
        balET = sumactETP + preinfep - balET;
        balOut += balET;
        balOut += this.run_outRD1;
        balOut += this.run_vertComp;
        balET = sumactETP + preinfep;

        double balance = balIn + (balMPSstart - balMPSend) + (balLPSstart - balLPSend) + (balDPSstart - balDPSend) - balOut;
        if (Math.abs(balance) > 0.00001) //System.out.println("balance error at : " + time.toString() + " --> "+ balance + " in entity: " + entities.getCurrent().getId());
        {
            getModel().getRuntime().println("balance error at : " + time.toString() + " --> " + balance + " in entity: " + entities.getCurrent().getId());
        }
        satMPS.setValue(this.run_satMPS);
        satLPS.setValue(this.run_satLPS);
        actMPS.setValue(this.run_actMPS);
        actLPS.setValue(this.run_actLPS);
        actDPS.setValue(this.run_actDPS);
//        netRain.setValue(this.run_inRain);
//        netSnow.setValue(this.run_inSnow);
        actET.setValue(balET);
        inRD1.setValue(this.run_inRD1);
        inRD2.setValue(this.run_inRD2);
        outRD1.setValue(this.run_outRD1);
        outRD2.setValue(this.run_outRD2);
        genRD1.setValue(this.run_genRD1);
        genRD2.setValue(this.run_genRD2);
        percolation.setValue(this.run_vertComp);
        interflow.setValue(this.run_latComp);

        soilActMPS.setValue(soilActMps);
        soilMaxMPS.setValue(soilMaxMps);
        soilSatMPS.setValue(soilSatMps);
        soilActLPS.setValue(soilActLps);
        soilMaxLPS.setValue(soilMaxLps);
        soilSatLPS.setValue(soilSatLps);
        infiltration_hor.setValue(infilhor);
        perco_hor.setValue(perchor);
        actETP_h.setValue(actETP_hor);
        //System.out.getRuntime().println("RD2_out: " + this.run_outRD2[0] + "\t" + this.run_outRD2[1] + "\t" + this.run_outRD2[2]);
    }

    @Override
    public void cleanup() {
    }

    private boolean calcSoilSaturations(boolean debug) {
        soilMaxMps = 0;
        soilActMps = 0;
        soilMaxLps = 0;
        soilActLps = 0;
        soilSatMps = 0;
        soilSatLps = 0;
        double upperMaxMps = 0;
        double upperActMps = 0;
        double upperMaxLps = 0;
        double upperActLps = 0;
        double[] infil_depth = new double[nhor];
        double partdepth = 0;
        double soilinfil = 50;

        for (int h = 0; h < nhor; h++) {
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
                this.run_satSoil1 = 0;
            }

            infil_depth[h] += layerdepth.getValue()[h];
            if (infil_depth[h] <= soilinfil || h == 0) {
                upperMaxMps += this.run_maxMPS[h] * layerdepth.getValue()[h];
                upperActMps += this.run_actMPS[h] * layerdepth.getValue()[h];
                upperMaxLps += this.run_maxLPS[h] * layerdepth.getValue()[h];
                upperActLps += this.run_actLPS[h] * layerdepth.getValue()[h];
                partdepth += layerdepth.getValue()[h];

            } else if (infil_depth[h - 1] <= soilinfil) {
                lowpart = soilinfil - partdepth;
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
            this.run_satSoil1 = ((soilActLps + soilActMps) / (soilMaxLps + soilMaxMps));
            this.top_satsoil = ((upperActLps + upperActMps) / (upperMaxLps + upperMaxMps));
            soilSatMps = (soilActMps / soilMaxMps);
            soilSatLps = (soilActLps / soilMaxLps);

        } else {
            this.run_satSoil1 = 0;
        }

        return true;
    }

    private boolean redistRD1_RD2_in() {
        //RD1 is put to DPS first
        if (this.run_inRD1 > 0) {
            this.run_actDPS = this.run_actDPS + this.run_inRD1;
            this.run_inRD1 = 0;
        }

        for (int h = 0; h < this.nhor; h++) {
            if (this.run_inRD2[h] > 0) {
                this.run_inRD2[h] = this.calcMPSInflow(this.run_inRD2[h], h);
                this.run_inRD2[h] = this.calcLPSInflow(this.run_inRD2[h], h);
                if (this.run_inRD2[h] > 0) {
                    //System.out.getRuntime().println("RD2 of entity " + entity.getDouble("ID") + " and horizon " + h +  " is routed through RD2out: "+this.run_inRD2[h]);
                    this.run_outRD2[h] = this.run_outRD2[h] + this.run_inRD2[h];
                    this.run_inRD2[h] = 0;
                }
            }
        }
        return true;
    }

    private boolean layer_diffusion() {

        double[] gradient_h_h1 = new double[this.nhor];
        double[] resistance_h_h1 = new double[this.nhor];



        for (int h = 0; h < this.nhor - 1; h++) {


            //calculate diffussion factor - order horizontal
            //diffusion only occur when gravitative flux is not dominating
            if ((run_satLPS[h] < 0.05) && (run_satMPS[h] < 0.8 || run_satMPS[h + 1] < 0.8) && (run_satMPS[h] > 0 || run_satMPS[h + 1] > 0)) {

                //calculate gradient

                gradient_h_h1[h] = (Math.log10(2 - this.run_satMPS[h]) - Math.log10(2 - this.run_satMPS[h + 1]));

                //calculate resistance

                double satbalance = Math.pow((Math.pow(this.run_satMPS[h], 2) + (Math.pow(this.run_satMPS[h + 1], 2))) / 2.0, 0.5);

                resistance_h_h1[h] = Math.log10(satbalance) * -kdiff_layer.getValue();



                //calculate amount of water to equilize saturations in layers

                double avg_sat = ((this.run_maxMPS[h] * this.run_satMPS[h]) + (this.run_maxMPS[h + 1] * this.run_satMPS[h + 1])) / (this.run_maxMPS[h] + this.run_maxMPS[h + 1]);

                double pot_flux = Math.abs((avg_sat - this.run_satMPS[h]) * this.run_maxMPS[h]);


                //calculate water fluxes




                double flux = (pot_flux * gradient_h_h1[h] / resistance_h_h1[h]);



                if (flux >= 0) {
                    flux_h_h1[h] = Math.min(flux, pot_flux);
                    //flux_h1_h[h] = Math.min(Math.min(flux, pot_flux), maxflux);
                } else {
                    //flux_h1_h[h] = Math.max(Math.max(flux, -pot_flux), -maxflux);
                    flux_h_h1[h] = Math.max(flux, -pot_flux);
                }

            } else {

                flux_h_h1[h] = 0;
            }

            this.run_actMPS[h] = this.run_actMPS[h] + flux_h_h1[h];
            this.run_actMPS[h + 1] = this.run_actMPS[h + 1] - flux_h_h1[h];


        }



        return true;
    }

    private boolean calcPreInfEvaporation() {
        double deltaETP = this.run_potETP - this.run_actETP;
        if (this.run_actDPS > 0) {
            if (this.run_actDPS >= deltaETP) {
                this.run_actDPS = this.run_actDPS - deltaETP;
                deltaETP = 0;
                this.run_actETP = this.run_potETP;
            } else {
                deltaETP = deltaETP - this.run_actDPS;
                this.run_actDPS = 0;
                this.run_actETP = this.run_potETP - deltaETP;
            }
        }
        /** @todo implementation for open water bodies has to be implemented here */
        return true;
    }

    private boolean calcInfImperv(double sealedGrade) {
        if (sealedGrade > 0.8) {
            this.run_overlandflow = this.run_overlandflow + (1 - soilImpGT80.getValue()) * this.run_infiltration;
            this.run_infiltration = this.run_infiltration * soilImpGT80.getValue();
        } else if (sealedGrade > 0 && sealedGrade <= 0.8) {
            this.run_overlandflow = this.run_overlandflow + (1 - soilImpLT80.getValue()) * this.run_infiltration;
            this.run_infiltration = this.run_infiltration * soilImpLT80.getValue();
        }
        if (this.run_overlandflow < 0) {
            System.out.println("overlandflow gets negative because of sealing! " + soilImpGT80.getValue() + ", " + soilImpLT80.getValue() + ", " + this.run_infiltration);
        }
        return true;
    }

    private double calcMaxInfiltration(int nowmonth, int horizon) {
        double maxInf = 0;
        this.calcSoilSaturations(false);
        if (this.run_snowDepth > 0) {
            maxInf = this.soilMaxInfSnow.getValue() * this.run_area;
        } else if ((nowmonth >= 5) & (nowmonth <= 10)) {
            maxInf = (1 - this.run_satHor[horizon]) * soilMaxInfSummer.getValue() * this.run_area;
        } else {
            maxInf = (1 - this.run_satHor[horizon]) * soilMaxInfWinter.getValue() * this.run_area;
        }

        return maxInf;
    }

    private double calcMaxInfiltration(int nowday) {
        double maxInf = 0;
        this.calcSoilSaturations(false);
        if (this.run_snowDepth > 0) {
            maxInf = this.soilMaxInfSnow.getValue() * this.runkf_h[0] * this.run_area;
        } else if ((nowday >= 170) & (nowday < 275)) {
            maxInf = (1 - this.run_satSoil1) * soilMaxInfSummer.getValue() * this.runkf_h[0] * this.run_area;
        } else {
            maxInf = (1 - this.run_satSoil1) * soilMaxInfWinter.getValue() * this.runkf_h[0] * this.run_area;
        }

        return maxInf;
    }
    
       private double calcMaxInfiltration() {
        double maxInf = 0;
        this.calcSoilSaturations(false);
        if (this.run_snowDepth > 0) {
            maxInf = this.soilMaxInfSnow.getValue() * this.runkf_h[0] * this.run_area;
        } else if (this.soilSatMps > MPS_thershold.getValue()) {
            maxInf = (1 - this.run_satSoil1) * soilMaxInfSummer.getValue() * this.runkf_h[0] * this.run_area;
        } else {
            maxInf = (1 - this.run_satSoil1) * soilMaxInfWinter.getValue() * this.runkf_h[0] * this.run_area;
        }

        return maxInf;
    }

    private double[] calcMPSEvapotranslayer(boolean debug, int nhor) { //author: Manfred Fink; Method after SWAT
        double[] horETP_local = new double[nhor];
        double sumlayer = 0;
        int i = 0;
        double runrootdepth = (rootdepth.getValue() * 1000) + 10;
        double[] partroot = new double[nhor];

        double runLAI = LAI.getValue();
        double pTransp = 0;
        double pEvap = 0;
        double[] transp_hord = new double[nhor];
        double[] evapo_hord = new double[nhor];
        double[] transp_hor = new double[nhor];
        double[] evapo_hor = new double[nhor];


        double horbal = 0;
        double test = 0;
        double deltaETP = this.run_potETP - this.run_actETP;
        // drifferentiation between evaporation and transpiration

        if (runLAI <= 3) {
            pTransp = (deltaETP * runLAI) / 3;
        } else if (runLAI > 3) {
            pTransp = deltaETP;
        }
        pEvap = deltaETP - pTransp;

        double soilroot = 0;
        // EvapoTranspiration loop 1: calculating layer poritions within rootdepth
        while (i < nhor) {

            sumlayer = sumlayer + layerdepth.getValue()[i] * 10;

            if (root_h.getValue()[i] == 1.0) {
                soilroot = soilroot + layerdepth.getValue()[i] * 10;
            }
            this.runlayerdepth[i] = sumlayer;
            if (runrootdepth > runlayerdepth[0]) {
                if (runrootdepth > runlayerdepth[i] && root_h.getValue()[i] == 1.0) {
                    partroot[i] = 1;

                }
                if (i > 0) {
                    if (runrootdepth > runlayerdepth[i - 1] && root_h.getValue()[i] == 1.0) {
                        partroot[i] = (runrootdepth - runlayerdepth[i - 1]) / (runlayerdepth[i] - runlayerdepth[i - 1]);

                    } else {
                        partroot[i] = 0;
                    }
                }
            } else if (i == 0) {
                partroot[i] = runrootdepth / runlayerdepth[0];

            }
            i++;

        }

        if (runrootdepth >= sumlayer) {
            runrootdepth = sumlayer;
        }

        i = 0;
        while (i < nhor) {


            runrootdepth = Math.min(runrootdepth, soilroot);
            // Transpiration loop 2: calculating transpiration distribution function with depth in layers
            transp_hord[i] = (pTransp * (1 - Math.exp(-BetaW.getValue() * (runlayerdepth[i] / runrootdepth)))) / (1 - Math.exp(-BetaW.getValue()));
            if (transp_hord[i] > pTransp) {
                transp_hord[i] = pTransp;
            }






            // Evaporation loop 2: calculating evaporation distribution function with depth in layers
            evapo_hord[i] = pEvap * (runlayerdepth[i] / (runlayerdepth[i] + (Math.exp(2.374 - (0.00713 * runlayerdepth[i])))));
            if (evapo_hord[i] > pEvap) {
                evapo_hord[i] = pEvap;
            }



            //allocation of the rest Evap to the lowest horizon ............
            if (i == nhor - 1) {
                evapo_hord[i] = pEvap;
                transp_hord[i] = pTransp;
            }



            i++;
        }
        i = 0;

        while (i < nhor) {

            if (i == 0) {

                transp_hor[i] = transp_hord[i];

                evapo_hor[i] = evapo_hord[i];


            } else {

                transp_hor[i] = transp_hord[i] - transp_hord[i - 1];

                evapo_hor[i] = evapo_hord[i] - evapo_hord[i - 1];

            }
            /*
            //calculation of evapotranspiration demand could not be provided by soillayer
            
            double parttrans = transp_hor[i] / (transp_hor[i] + evapo_hor[i]);
            double partevap = 1 - parttrans;
            
            transdemand[i] = transp_hor[i] - (parttrans * (this.run_actLPS[i] + this.run_actMPS[i]));
            
            //transdemand[i] = Math.max(transdemand[i],0);
            
            evapodemand[i] = evapo_hor[i] - (partevap * (this.run_actLPS[i] + this.run_actMPS[i]));
            
            //evapodemand[i] = Math.max(evapodemand[i],0);
            
            
            if  (evapodemand[i] > 0){

            sumevapodemand = evapodemand[i] + sumevapodemand;

            }
            
            
            if (i <= rootlayer && transdemand[i] > 0){

            sumtransdemand = transdemand[i] + sumtransdemand;

            }
            i++;
            }

            // EvapoTranspiration loop 3: calculating of redistribution of demands not met in the upper soil layers
            i = 0;

            while (i < nhor) {
            
            if (transdemand[i] < 0 && i <= rootlayer){
            
            comptrans[i] =  Math.min(-transdemand[i],(sumtransdemand * epco.getValue()));
            sumtransdemand = sumtransdemand - comptrans[i];

            
            }else{
            comptrans[i] = 0;
            }
            
            if (evapodemand[i] < 0){
            
            compevapo[i] =  Math.min(-evapodemand[i],(sumevapodemand * esco.getValue()));
            sumevapodemand = sumevapodemand - compevapo[i];

            
            }else{
            compevapo[i] = 0;
            }
            
            
            
            
            horETP[i] =  transp_hor[i] + comptrans[i] + evapo_hor[i] + compevapo[i];
             */

            horETP_local[i] = transp_hor[i] + evapo_hor[i];

            if (debug) {
                horbal = horbal + horETP_local[i];
                test = deltaETP - horbal;

            }

            i++;
        }


        if ((test > 0.0000001 || test < -0.0000001) && debug) {
            System.out.println("evaporation balance error = " + test);
        }


        this.soil_root.setValue(soilroot / 1000);
        return horETP_local;
    }

    private boolean calcMPSTranspiration(boolean debug, int hor) {
        double maxTrans = 0;
        /** updating saturations */
        this.calcSoilSaturations(debug);

        /** delta ETP */
        double deltaETP = this.horETP[hor];

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
            maxTrans = deltaETP * reductionFactor;
            if (maxTrans > deltaETP) {
                maxTrans = deltaETP;
            }
        }


        if (maxTrans > run_actMPS[hor]) {
            maxTrans = run_actMPS[hor];
        }


        this.run_actMPS[hor] = this.run_actMPS[hor] - maxTrans;

        /** Transpiration from MPS
         * if(deltaETP > 0){
         *
         * /** if enough water is available
         * if(this.run_actMPS[hor] > maxTrans){
         * this.run_actMPS[hor] = this.run_actMPS[hor] - maxTrans;
         * deltaETP = deltaETP - maxTrans;
         * }
         * /** storage is limiting ETP
         * else{
         * deltaETP = deltaETP - this.run_actMPS[hor];
         * this.run_actMPS[hor] = 0;
         * }
         * } **/
        /** recalculation actual ETP */
        this.run_actETP = maxTrans;
        this.calcSoilSaturations(debug);

        /* @todo: ETP from water bodies has to be implemented here */
        return true;
    }

    private double calcMPSInflow(double infiltration, int hor) {
        double inflow = infiltration;

        /** updating saturations */
        this.calcSoilSaturations(false);

        /**checking if MPS can take all the water */
        if (inflow < (this.run_maxMPS[hor] - this.run_actMPS[hor])) {
            /** if MPS is empty it takes all the water */
            if (this.run_actMPS[hor] == 0) {
                this.run_actMPS[hor] = this.run_actMPS[hor] + inflow;
                inflow = 0;
            } /** MPS is partly filled and gets part of the water */
            else {
                double alpha = this.soilDistMPSLPS.getValue();
                //if sat_MPS is 0 the next equation would produce an error,
                //therefore it is set to MPS_sat is set to 0.0000001 in that case
                if (this.run_satMPS[hor] == 0) {
                    this.run_satMPS[hor] = 0.0000001;
                }
                double inMPS = (inflow) * (1. - Math.exp(-1 * alpha / this.run_satMPS[hor]));
                this.run_actMPS[hor] = this.run_actMPS[hor] + inMPS;
                inflow = inflow - inMPS;
            }
        } /** infiltration exceeds storage capacity of MPS */
        else {
            double deltaMPS = this.run_maxMPS[hor] - this.run_actMPS[hor];
            this.run_actMPS[hor] = this.run_maxMPS[hor];
            inflow = inflow - deltaMPS;
        }
        /** updating saturations */
        this.calcSoilSaturations(false);
        return inflow;
    }
    /*
     *problem overflow is put to DPS, we have to deal with that problem
     */

    private double calcLPSInflow(double infiltration, int hor) {
        /** updating saturations */
        this.calcSoilSaturations(false);
        this.run_actLPS[hor] = this.run_actLPS[hor] + infiltration;
        infiltration = 0;
        /** if LPS is saturated depression Storage occurs */
        if (this.run_actLPS[hor] > this.run_maxLPS[hor]) {
            infiltration = (this.run_actLPS[hor] - this.run_maxLPS[hor]);
            //this.run_actDPS = this.run_actDPS + (this.run_actLPS[hor] - this.run_maxLPS[hor]);
            this.run_actLPS[hor] = this.run_maxLPS[hor];
        }
        /** updating saturations */
        this.calcSoilSaturations(false);
        return infiltration;
    }

    private double calcLPSoutflow(int hor) {
        double alpha = this.soilOutLPS.getValue();
        //if soilSat is 1 the outflow equation would produce an error,
        //for this (unlikely) case soilSat is set to 0.999999

        //testing if LPSsat might give a better behaviour
        if (this.run_satLPS[hor] == 1.0) {
            this.run_satLPS[hor] = 0.999999;
        }
        //original function
        //double potLPSoutflow = this.act_LPS * (1. - Math.exp(-1*alpha/(1-this.sat_LPS)));
        //peters second
        //double potLPSoutflow = Math.pow(this.run_satHor[hor], alpha) * this.run_actLPS[hor];



        //Manfreds new
        //double potLPSoutflow = ((1 - (1 / (Math.pow(this.run_satHor[hor], 2) + alpha))) * this.run_actLPS[hor]) * (this.runkf_h[hor] / layerdepth.getValue()[hor]);
        //Peters second + kf and layerdepth
        //double potLPSoutflow = Math.pow(this.run_satHor[hor], alpha) * this.run_actLPS[hor] * (this.runkf_h[hor] / layerdepth.getValue()[hor]);
        //Peters newest:
        double potLPSoutflow = Math.pow(this.run_satHor[hor], alpha) * this.run_actLPS[hor];
        //testing a simple function function out = 1/k * sto
        //double potLPSoutflow = 1 / alpha * this.act_LPS;//Math.pow(this.act_LPS, alpha);
        if (potLPSoutflow > this.run_actLPS[hor]) {
            potLPSoutflow = this.run_actLPS[hor];
        }

        double LPSoutflow = potLPSoutflow;// * ( 1 / this.parameter.getDouble("lps_kfForm"));
        if (LPSoutflow > this.run_actLPS[hor]) {
            LPSoutflow = this.run_actLPS[hor];
        }

        if (LPSoutflow < 0) {
            LPSoutflow = 0;
        }

        this.run_actLPS[hor] = this.run_actLPS[hor] - LPSoutflow;



        return LPSoutflow;
    }

    private boolean calcIntfPercRates(double MobileWater, int hor) {
        if (MobileWater > 0) {
            double slope_weight = (Math.tan(this.run_slope * (Math.PI / 180.))) * this.soilLatVertLPS.getValue();

            /** potential part of percolation */
            double part_perc = (1 - slope_weight);
            if (part_perc > 1) {
                part_perc = 1;
            } else if (part_perc < 0) {
                part_perc = 0;
            }

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

                try {
                    maxPerc = this.soilMaxPerc.getValue() * this.run_area * this.runkf_h[hor + 1] / 86.4;
                } catch (Exception e) {
                    System.out.println("SOILID = " + soilID.getValue());
                }
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
        return true;
    }

    private double calcDirectRunoff() {
        double directRunoff = 0;
        if (this.run_actDPS > 0) {
            double maxDep = 0;

            /** depression storage on slopes is half the normal dep. storage */
            if (this.run_slope > 5.0) {
                maxDep = (this.soilMaxDPS.getValue() * this.run_area) / 2;
            } else {
                maxDep = this.soilMaxDPS.getValue() * this.run_area;
            }

            if (this.run_actDPS > maxDep) {
                directRunoff = this.run_actDPS - maxDep;
                this.run_actDPS = maxDep;
            }
        }
        if (directRunoff < 0) {
            System.out.println("directRunoff is negative! --> " + directRunoff);
        }
        return directRunoff;
    }

    private boolean calcRD2_out(int h) {


        /** lateral interflow */
        //switched of 15-03-2004
        //double RD2_output_factor = this.conc_index / this.parameter.getDouble("conc_recRD2");
        double RD2_output_factor = 1. / this.soilConcRD2.getValue();
        if (RD2_output_factor > 1) {
            RD2_output_factor = 1;
        } else if (RD2_output_factor < 0) {
            RD2_output_factor = 0;
        }

        /** real RD2 output */
        double RD2_output = this.run_latComp * RD2_output_factor;
        /** rest is put back to LPS Storage */
        this.run_actLPS[h] = this.run_actLPS[h] + (this.run_latComp - RD2_output);
        this.run_outRD2[h] = this.run_outRD2[h] + RD2_output;
        this.run_genRD2[h] = this.run_outRD2[h];// - this.in_RD2;
        if (this.run_genRD2[h] < 0) {
            this.run_genRD2[h] = 0;
        }
        //this.in_RD2 = 0;
        this.run_latComp = 0;
        return true;
    }

    private boolean calcRD1_out() {
        /** DIRECT OVERLANDFLOW */
        //switched off 15-03-2004
        //double RD1_output_factor = this.conc_index / this.parameter.getDouble("conc_recRD1");
        double RD1_output_factor = 1. / this.soilConcRD1.getValue();
        if (RD1_output_factor > 1) {
            RD1_output_factor = 1;
        } else if (RD1_output_factor < 0) {
            RD1_output_factor = 0;
        }

        /** real RD1 output */
        double RD1_output = this.run_overlandflow * RD1_output_factor;
        /** rest is put back to dep. Storage */
        this.run_actDPS = this.run_actDPS + (this.run_overlandflow - RD1_output);
        this.run_outRD1 = this.run_outRD1 + RD1_output;
        this.run_genRD1 = this.run_outRD1;// - this.in_RD1;
        //this.in_RD1 = 0;

        this.run_overlandflow = 0;
        return true;
    }

    private boolean calcDiffusion(int h) {
        double diffusion = 0;
        /** updating saturations */
        this.calcSoilSaturations(false);
        double deltaMPS = this.run_maxMPS[h] - this.run_actMPS[h];
        //if sat_MPS is 0 the diffusion equation would produce an error,
        //for this (unlikely) case diffusion is set to zero
        if (this.run_satMPS[h] == 0.0) {
            diffusion = 0;
        } else {
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
        return true;
    }
}
