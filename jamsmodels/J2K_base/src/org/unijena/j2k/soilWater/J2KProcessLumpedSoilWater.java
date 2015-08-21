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

package org.unijena.j2k.soilWater;

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="J2KProcessLumpedSoilWater",
        author="Peter Krause",
        description="Calculates soil water balance for each spatial modelling unit",
        version="1.0_0",
        date="2011-05-30")
        public class J2KProcessLumpedSoilWater extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time"
            )
            public Attribute.Calendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current spatial modelling entity"
            )
            public Attribute.Entity entity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area",
            unit="m²"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute slope",
            unit="deg"
            )
            public Attribute.Double slope;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "sealed grade"
            )
            public Attribute.Double sealedGrade;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable net rain",
            unit="L"
            )
            public Attribute.Double netRain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable net snow",
            unit="L"
            )
            public Attribute.Double netSnow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable potET",
            unit="L"
            )
            public Attribute.Double potET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable actET",
            unit="L"
            )
            public Attribute.Double actET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "snow depth",
            unit="mm"
            )
            public Attribute.Double snowDepth;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "daily snow melt",
            unit="L"
            )
            public Attribute.Double snowMelt;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum MPS",
            unit="L"
            )
            public Attribute.Double maxMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum LPS",
            unit="L"
            )
            public Attribute.Double maxLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state var actual MPS",
            unit="L"
            )
            public Attribute.Double actMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state var actual LPS",
            unit="L"
            )
            public Attribute.Double actLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state var actual depression storage",
            unit="L"
            )
            public Attribute.Double actDPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state var saturation of MPS"
            )
            public Attribute.Double satMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state var saturation of LPS"
            )
            public Attribute.Double satLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state var saturation of whole soil"
            )
            public Attribute.Double satSoil;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar infiltration",
            unit="L"
            )
            public Attribute.Double infiltration;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar interflow",
            unit="L"
            )
            public Attribute.Double interflow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar percolation",
            unit="L"
            )
            public Attribute.Double percolation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "statevar RD1 inflow",
            unit="L"
            )
            public Attribute.Double inRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar RD1 outflow",
            unit="L"
            )
            public Attribute.Double outRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar RD1 generation",
            unit="L"
            )
            public Attribute.Double genRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "statevar RD2 inflow",
            unit="L"
            )
            public Attribute.Double inRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar RD2 outflow",
            unit="L"
            )
            public Attribute.Double outRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "statevar RD2 generation",
            unit="L"
            )
            public Attribute.Double genRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum depression storage",
            unit="L"
            )
            public Attribute.Double soilMaxDPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "poly reduction of ETP",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "3.0"
            )
            public Attribute.Double soilPolRed;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "linear reduction of ETP",
            lowerBound = 0.0,
            upperBound = 1.0,
            defaultValue = "0.6"
            )
            public Attribute.Double soilLinRed;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum infiltration rate in summer for one time step",
            lowerBound = 0.0,
            upperBound = 100.0,
            defaultValue = "50.0",
            unit="mm"
            )
            public Attribute.Double soilMaxInfSummer;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum infiltration rate in winter for one time step",
            lowerBound = 0.0,
            upperBound = 100.0,
            defaultValue = "50.0",
            unit="mm"
            )
            public Attribute.Double soilMaxInfWinter;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum infiltration rate on snow for one time step",
            lowerBound = 0.0,
            upperBound = 100.0,
            defaultValue = "50.0",
            unit="mm"
            )
            public Attribute.Double soilMaxInfSnow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum infiltration part on sealed areas (gt 80%)",
            lowerBound = 0.0,
            upperBound = 1.0,
            defaultValue = "0.25"
            )
            public Attribute.Double soilImpGT80;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum infiltration part on sealed areas (lt 80%)",
            lowerBound = 0.0,
            upperBound = 1.0,
            defaultValue = "0.75"
            )
            public Attribute.Double soilImpLT80;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "MPS/LPS distribution coefficient for inflow",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "1.0"
            )
            public Attribute.Double soilDistMPSLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "MPS/LPS diffusion coefficient",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "1.0"
            )
            public Attribute.Double soilDiffMPSLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "LPS outflow coefficient",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "1.0"
            )
            public Attribute.Double soilOutLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "LPS lateral-vertical distribution coefficient",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "1.0"
            )
            public Attribute.Double soilLatVertLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum percolation rate [mm/d]",
            lowerBound = 0.0,
            upperBound = 20.0,
            defaultValue = "5.0",
            unit = "mm d^-1"
            )
            public Attribute.Double soilMaxPerc;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "concentration coefficient for RD1",
            lowerBound = 0.0,
            upperBound = 10.0,
            defaultValue = "2.0"
            )
            public Attribute.Double soilConcRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "concentration coefficient for RD2",
            lowerBound = 1.0,
            upperBound = 20.0,
            defaultValue = "8.0"
            )
            public Attribute.Double soilConcRD2;
    
    //internal state variables
    double run_maxMPS, run_maxLPS, run_actMPS, run_actLPS, run_satMPS, run_actDPS, run_satLPS, run_satSoil, run_inRD1, run_inRD2, run_inRain, run_inSnow,
            run_snowMelt, run_infiltration, run_interflow, run_percolation, run_overlandflow, run_potETP, run_actETP, run_snowDepth, run_area, run_slope,
            run_outRD1, run_outRD2, run_genRD1, run_genRD2;
    
    /*
     *  Component run stages
     */
    
    public void init() {
       

        
    }
    
    public void run() {
        this.run_area = area.getValue();
        this.run_slope = slope.getValue();
        
        this.run_maxMPS = maxMPS.getValue();
        this.run_maxLPS = maxLPS.getValue();
        this.run_actMPS = actMPS.getValue();
        this.run_actLPS = actLPS.getValue();
        this.run_satMPS = satMPS.getValue();
        this.run_satLPS = satLPS.getValue();
        this.run_actDPS = actDPS.getValue();
        
        this.run_inRD1 = inRD1.getValue();
        this.run_inRD2 = inRD2.getValue();
        
        this.run_inRain = netRain.getValue();
        this.run_inSnow = netSnow.getValue();
        
        this.run_potETP = potET.getValue();
        this.run_actETP = actET.getValue();
        this.run_snowDepth = snowDepth.getValue();
        this.run_snowMelt = snowMelt.getValue();
        
        this.run_genRD1 = 0;
        this.run_genRD2 = 0;
        this.run_outRD1 = 0;
        this.run_outRD2 = 0;
        this.run_interflow = 0;
        this.run_percolation = 0;
        
        //calculation of saturations first
        this.calcSoilSaturations(false);
        
        /** redistributing RD1 and RD2 inflow of antecedent unit */
        this.redistRD1_RD2_in();
        
        /** calculation of ETP from dep. Storage and open water bodies */
        this.calcPreInfEvaporation();
        
        /** determining available water for infiltration */
        this.run_infiltration = this.run_inRain + this.run_inSnow
                + this.run_snowMelt
                + this.run_actDPS;
        
        this.run_actDPS = 0;
        this.run_inRain = 0;
        this.run_inSnow = 0;
        this.run_snowMelt = 0;
        
        /** infiltration on impervious areas and water bodies
         *  is directly routed as DirectRunoff to the next polygon
         *  a better implementation would be the next river reach */
        this.calcInfImperv(sealedGrade.getValue());
        
        /** determining maximal infiltration rate */
        double maxInf = this.calcMaxInfiltration(time.get(Attribute.Calendar.MONTH)+1);
        if(maxInf < this.run_infiltration){
            double deltaInf = this.run_infiltration - maxInf;
            this.run_actDPS = this.run_actDPS + deltaInf;
            this.run_infiltration = maxInf;
        }
        
        /** determining inflow of infiltration to MPS */
        this.run_infiltration = this.calcMPSInflow(this.run_infiltration);
        
        /** determining transpiration from MPS */
        this.calcMPSTranspiration(false);
        
        /** inflow to LPS */
        this.run_infiltration = this.calcLPSInflow(this.run_infiltration);
        
        /** updating saturations */
        this.calcSoilSaturations(false);
        
        /** determining outflow from LPS */
        double MobileWater = 0;
        if(this.run_actLPS > 0){
            MobileWater = this.calcLPSoutflow();
        } else
            MobileWater = 0;
        
        /** Distribution of MobileWater to the lateral (interflow) and
         * vertical (percolation) flowpaths  */
        this.calcIntfPercRates(MobileWater);
        
        /** determining direct runoff from depression storage */
        this.run_overlandflow = this.run_overlandflow + this.calcDirectRunoff();
        
        /** determining internal area routing **/
        this.calcRD1_RD2_out();
        
        /** determining diffusion from LPS to MPS */
        this.calcDiffusion();
        
        /** updating saturations */
        this.calcSoilSaturations(false);
        
        satSoil.setValue(this.run_satSoil);
        satMPS.setValue(this.run_satMPS);
        satLPS.setValue(this.run_satLPS);
        actMPS.setValue(this.run_actMPS);
        actLPS.setValue(this.run_actLPS);
        actDPS.setValue(this.run_actDPS);
        netRain.setValue(this.run_inRain);
        netSnow.setValue(this.run_inSnow);
        actET.setValue(this.run_actETP);
        inRD1.setValue(this.run_inRD1);
        inRD2.setValue(this.run_inRD2);
        outRD1.setValue(this.run_outRD1);
        outRD2.setValue(this.run_outRD2);
        genRD1.setValue(this.run_genRD1);
        genRD2.setValue(this.run_genRD2);
        percolation.setValue(this.run_percolation);
        interflow.setValue(this.run_interflow);
    }
    
    public void cleanup() {
        
    }
    
    private boolean calcSoilSaturations(boolean debug){
       
        if((this.run_actLPS > 0) && (this.run_maxLPS > 0)){
            this.run_satLPS = this.run_actLPS / this.run_maxLPS;
        } else
            this.run_satLPS = 0;
        
        if((this.run_actMPS > 0) && (this.run_maxMPS > 0)){
            this.run_satMPS = this.run_actMPS / this.run_maxMPS;
        } else
            this.run_satMPS = 0;
        
        if(((this.run_maxLPS > 0) | (this.run_maxMPS > 0)) & ((this.run_actLPS > 0) | (this.run_actMPS > 0))){
            this.run_satSoil = ((this.run_actLPS + this.run_actMPS) / (this.run_maxLPS + this.run_maxMPS));
        } else{
            this.run_satSoil = 0;
        }
       
        return true;
    }
    
    private boolean redistRD1_RD2_in(){
        if(this.run_inRD1 > 0){
            this.run_actDPS = this.run_actDPS + this.run_inRD1;
            this.run_inRD1 = 0;
        }
        if(this.run_inRD2 > 0){
            this.run_inRD2 = this.calcMPSInflow(this.run_inRD2);
            this.run_inRD2 = this.calcLPSInflow(this.run_inRD2);
            if(this.run_inRD2 > 0){
                getModel().getRuntime().println("RD2 is not null");
            }
        }
        return true;
    }
    
    private boolean calcPreInfEvaporation(){
        double deltaETP = this.run_potETP - this.run_actETP;
        if(this.run_actDPS > 0){
            if(this.run_actDPS >= deltaETP){
                this.run_actDPS = this.run_actDPS - deltaETP;
                deltaETP = 0;
                this.run_actETP = this.run_potETP;
            } else{
                deltaETP = deltaETP - this.run_actDPS;
                this.run_actDPS = 0;
                this.run_actETP = this.run_potETP - deltaETP;
            }
        }
        /** @todo implementation for open water bodies has to be implemented here */
        return true;
    }
    
    private boolean calcInfImperv(double sealedGrade){
        if(sealedGrade > 0.8){
            this.run_overlandflow = this.run_overlandflow + (1 - soilImpGT80.getValue()) * this.run_infiltration;
            this.run_infiltration = this.run_infiltration * soilImpGT80.getValue();
        } else if(sealedGrade > 0 && sealedGrade <= 0.8){
            this.run_overlandflow = this.run_overlandflow +  (1 - soilImpLT80.getValue()) * this.run_infiltration;
            this.run_infiltration = this.run_infiltration * soilImpLT80.getValue();
        }
        return true;
    }
    
    private double calcMaxInfiltration(int nowmonth){
        double maxInf = 0;
        this.calcSoilSaturations(false);
        if(this.run_snowDepth > 0)
            maxInf = this.soilMaxInfSnow.getValue() * this.run_area;
        else if((nowmonth >= 5) & (nowmonth <=10))
            maxInf = (1 - this.run_satSoil) * soilMaxInfSummer.getValue() * this.run_area;
        else
            maxInf = (1 - this.run_satSoil) * soilMaxInfWinter.getValue() * this.run_area;
        
        return maxInf;
    }
    
    
    
    private boolean calcMPSTranspiration(boolean debug){
        double maxTrans = 0;
        /** updating saturations */
        this.calcSoilSaturations(debug);
        
        /** delta ETP */
        double deltaETP = this.run_potETP - this.run_actETP;
        
        /**linear reduction after MENZEL 1997 was chosen*/
        //if(this.etp_reduction == 0){
        if(this.soilLinRed.getValue() > 0){
            /** reduction if actual saturation is smaller than linear factor */
            if(this.run_satMPS < soilLinRed.getValue()){
                //if(this.sat_MPS < this.etp_linRed){
                double reductionFactor = this.run_satMPS / soilLinRed.getValue();
                
                //double reductionFactor = this.sat_MPS / etp_linRed;
                maxTrans = deltaETP * reductionFactor;
                
            } else{
                maxTrans = deltaETP;
            }
        }
        /** polynomial reduction after KRAUSE 2001 was chosen */
        else if(soilPolRed.getValue() > 0){
            //else if(this.etp_reduction == 1){
            double sat_factor = -10. * Math.pow((1 - this.run_satMPS), soilPolRed.getValue());
            //double sat_factor = Math.pow((1 - this.sat_MPS), etp_polRed);
            double reductionFactor = Math.pow(10, sat_factor);
            maxTrans = deltaETP * reductionFactor;
            if(maxTrans > deltaETP)
                maxTrans = deltaETP;
        }
        
        /** Transpiration from MPS */
        if(deltaETP > 0){
            
            /** if enough water is available */
            if(this.run_actMPS > maxTrans){
                this.run_actMPS = this.run_actMPS - maxTrans;
                deltaETP = deltaETP - maxTrans;
            }
            /** storage is limiting ETP */
            else{
                deltaETP = deltaETP - this.run_actMPS;
                this.run_actMPS = 0;
            }
        }
        
        /** recalculation actual ETP */
        this.run_actETP = this.run_potETP - deltaETP;
        this.calcSoilSaturations(debug);
        
        /* @todo: ETP from water bodies has to be implemented here */
        return true;
    }
    
    private double calcMPSInflow(double infiltration){
        double inflow = infiltration;
        
        /** updating saturations */
        this.calcSoilSaturations(false);
        
        /**checking if MPS can take all the water */
        if(inflow < (this.run_maxMPS - this.run_actMPS)){ //christian: this seams wrong to me, because if this test fails mps gets all the water even if it wouldn't because of alpha .. 
            /** if MPS is empty it takes all the water */
            if(this.run_actMPS == 0){
                this.run_actMPS = this.run_actMPS + inflow;
                inflow = 0;
            }
            /** MPS is partly filled and gets part of the water */
            else{
                double alpha = this.soilDistMPSLPS.getValue();
                //if sat_MPS is 0 the next equation would produce an error,
                //therefore it is set to MPS_sat is set to 0.0000001 in that case
                if(this.run_satMPS == 0)
                    this.run_satMPS = 0.0000001;
                double inMPS = (inflow) * (1. - Math.exp(-1*alpha / this.run_satMPS));
                this.run_actMPS = this.run_actMPS + inMPS;
                inflow = inflow - inMPS;
            }
        }
        /** infiltration exceeds storage capacity of MPS */
        else{
            double deltaMPS = this.run_maxMPS - this.run_actMPS;
            this.run_actMPS = this.run_maxMPS;
            inflow = inflow - deltaMPS;
        }
        
        return inflow;
    }
    
    private double calcLPSInflow(double infiltration){
        this.run_actLPS = this.run_actLPS + infiltration;
        infiltration = 0;
        /** if LPS is saturated depression Storage occurs */
        if(this.run_actLPS > this.run_maxLPS){
            this.run_actDPS = this.run_actDPS + (this.run_actLPS - this.run_maxLPS);
            this.run_actLPS = this.run_maxLPS;
        }
        return infiltration;
    }
    
    private double calcLPSoutflow(){
        double alpha = this.soilOutLPS.getValue();
        //if soilSat is 1 the outflow equation would produce an error,
        //for this (unlikely) case soilSat is set to 0.999999
        
        //testing if LPSsat might give a better behaviour
        if(this.run_satLPS == 1.0)
            this.run_satLPS = 0.999999;
        //original function
        //double potLPSoutflow = this.act_LPS * (1. - Math.exp(-1*alpha/(1-this.sat_LPS)));
        double potLPSoutflow = Math.pow(this.run_satSoil, alpha) * this.run_actLPS;
        
        //testing a simple function function out = 1/k * sto
        //double potLPSoutflow = 1 / alpha * this.act_LPS;//Math.pow(this.act_LPS, alpha);
        if(potLPSoutflow > this.run_actLPS)
            potLPSoutflow = this.run_actLPS;
        
        double LPSoutflow = potLPSoutflow;// * ( 1 / this.parameter.getDouble("lps_kfForm"));
        if(LPSoutflow > this.run_actLPS)
            LPSoutflow = this.run_actLPS;
        
        this.run_actLPS = this.run_actLPS - LPSoutflow;
        
        return LPSoutflow;
    }
    
    private boolean calcIntfPercRates(double MobileWater){
        if(MobileWater > 0){
            double slope_weight = (Math.tan(this.run_slope * (Math.PI / 180.))) * this.soilLatVertLPS.getValue();
            
            /** potential part of percolation */
            double part_perc = (1 - slope_weight);
            if(part_perc > 1)
                part_perc = 1;
            else if(part_perc < 0)
                part_perc = 0;
            
            /** potential part of interflow */
            double part_intf = (1 - part_perc);
            
            this.run_interflow = MobileWater * part_intf;
            this.run_percolation = MobileWater * part_perc;
            
            /** checking if percolation rate is limited by parameter */
            double maxPerc = this.soilMaxPerc.getValue() * this.run_area;
            if(this.run_percolation > maxPerc){
                double rest = this.run_percolation - maxPerc;
                this.run_percolation = maxPerc;
                this.run_interflow = this.run_interflow + rest;
            }
        }
        /** no MobileWater available */
        else{
            this.run_interflow = 0;
            this.run_percolation = 0;
        }
        return true;
    }
    
    private double calcDirectRunoff(){
        double directRunoff = 0;
        if(this.run_actDPS > 0){
            double maxDep = 0;
            
            /** depression storage on slopes is half the normal dep. storage */
            if(this.run_slope > 5.0){
                maxDep = (this.soilMaxDPS.getValue() * this.run_area) / 2;
            } else
                maxDep = this.soilMaxDPS.getValue() * this.run_area;
            
            if(this.run_actDPS > maxDep){
                directRunoff = this.run_actDPS - maxDep;
                this.run_actDPS = maxDep;
            }
        }
        return directRunoff;
    }
    
    private boolean calcRD1_RD2_out(){
        /** DIRECT OVERLANDFLOW */
        //switched off 15-03-2004
        //double RD1_output_factor = this.conc_index / this.parameter.getDouble("conc_recRD1");
        double RD1_output_factor = 1. / this.soilConcRD1.getValue();
        if(RD1_output_factor > 1)
            RD1_output_factor = 1;
        else if(RD1_output_factor < 0)
            RD1_output_factor = 0;
        
        /** real RD1 output */
        double RD1_output = this.run_overlandflow * RD1_output_factor;
        /** rest is put back to dep. Storage */
        this.run_actDPS = this.run_actDPS + (this.run_overlandflow - RD1_output);
        this.run_outRD1 = this.run_outRD1 + RD1_output;
        this.run_genRD1 = this.run_outRD1;// - this.in_RD1;
        //this.in_RD1 = 0;
        /** lateral interflow */
        //switched of 15-03-2004
        //double RD2_output_factor = this.conc_index / this.parameter.getDouble("conc_recRD2");
        double RD2_output_factor = 1. / this.soilConcRD2.getValue();
        if(RD2_output_factor > 1)
            RD2_output_factor = 1;
        else if(RD2_output_factor < 0)
            RD2_output_factor = 0;
        
        /** real RD2 output */
        double RD2_output = this.run_interflow * RD2_output_factor;
        /** rest is put back to LPS Storage */
        this.run_actLPS = this.run_actLPS + (this.run_interflow - RD2_output);
        this.run_outRD2 = this.run_outRD2 + RD2_output;
        this.run_genRD2 = this.run_outRD2;// - this.in_RD2;
        if(this.run_genRD2 < 0)
            this.run_genRD2 = 0;
        //this.in_RD2 = 0;
        
        this.run_overlandflow = 0;
        this.run_interflow = 0;
        return true;
    }
    
    private boolean calcDiffusion(){
        double diffusion = 0;
        /** updating saturations */
        this.calcSoilSaturations(false);
        double deltaMPS = this.run_maxMPS - this.run_actMPS;
        //if sat_MPS is 0 the diffusion equation would produce an error,
        //for this (unlikely) case diffusion is set to zero
        if(this.run_satMPS == 0.0){
            diffusion = 0;
        } else{
            double diff = this.soilDiffMPSLPS.getValue();
            
            //new equation like all other exps 04.03.04
            diffusion = this.run_actLPS * (1. - Math.exp((-1. * diff) / this.run_satMPS)); 
        }
        
        if(diffusion > this.run_actLPS)
            diffusion = this.run_actLPS;
        
        
        /** MPS can take all the water from diffusion */
        if(diffusion < deltaMPS){
            this.run_actMPS = this.run_actMPS + diffusion;
            this.run_actLPS = this.run_actLPS - diffusion;
        }
        /** MPS can take only part of the water */
        else{
            double rest = this.run_maxMPS - this.run_actMPS;
            this.run_actMPS = this.run_maxMPS;
            this.run_actLPS = this.run_actLPS - rest;
        }
        return true;
    }
}
