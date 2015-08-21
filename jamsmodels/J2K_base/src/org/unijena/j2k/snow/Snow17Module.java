/*
 * Snow17Module.java
 *
 * Created on 09. January 2007, 15:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.j2k.snow;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
title="ABCSnowModule",
        author="Peter Krause",
        description="The famous snow-17 snow module. Implementation is based on " +
        "the original sources of Eric Anderson in a reworked version from Victor Koren" +
        "some very minor adaptation were necessary to make the routine JAMS compatible"
        )
        public class Snow17Module extends JAMSComponent {
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "time interval"
            )
            public Attribute.TimeInterval timeInterval;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "time"
            )
            public Attribute.Calendar time;
    
    //entity attributes
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Entity area"
            //unit = "m²"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Entity elevation"
            //unit = "m"
            )
            public Attribute.Double elevation;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Entity latitude"
            //unit = "deg"
            )
            public Attribute.Double latitude;
    
    //input data
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "rain amount"
            )
            public Attribute.Double rain_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "snow amount"
            )
            public Attribute.Double snow_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the temperature"
            )
            public Attribute.Double airTemperature;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the daily minimum temperature"
            )
            public Attribute.Double tmin;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the daily maximum temperature"
            )
            public Attribute.Double tmax;
    
    /******************************************
     * //the states
     *******************************************/
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "mm",
            description = "snow water equivalent [WE]"
            )
            public Attribute.Double swe_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "mm",
            description = "negative snow heat [NEGHS]"
            )
            public Attribute.Double negSnowHeat_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "mm",
            description = "cnhs not clear [CNHS]"
            )
            public Attribute.Double cnhs_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "mm",
            description = "liquid water in snow pack [LIQW]"
            )
            public Attribute.Double liqWater_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "degC",
            description = "Antecedent temperature index [TINDEX]"
            )
            public Attribute.Double tindex_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "mm",
            description = "Cumulated snow water including liquid [ACCMAX]"
            )
            public Attribute.Double maxAccumulation_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "mm",
            description = "Snow liquid water attenuation storage [STORGE]"
            )
            public Attribute.Double liqWaterAttStorage_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "decimal",
            description = "Adjusted areal snow cover fraction [AEADJ]"
            )
            public Attribute.Double aeAdj_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "mm",
            description = "Array of lagged liquid water values [EXLAG]"
            )
            public Attribute.DoubleArray exlag_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "degC",
            description = "Average snow temperature [SNTMP]"
            )
            public Attribute.Double snowTemp_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "cm",
            description = "Average snow depth [SNDPT]"
            )
            public Attribute.Double snowDepth_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "",
            description = "Average snow density [DS]"
            )
            public Attribute.Double snowDens_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "",
            description = "Areal snow cover [AESC]"
            )
            public Attribute.Double arealSnowCover_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "",
            description = "internal state [SB]"
            )
            public Attribute.Double sb_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "",
            description = "internal state [SBWS]"
            )
            public Attribute.Double sbws_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "",
            description = "internal state [SBAESC]"
            )
            public Attribute.Double sbaesc_1;
    
    /***********************************************
     *output variables
     **********************************************/
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "mm",
            description = "rain passed by the snow module [RAIN]"
            )
            public Attribute.Double rainOut_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "mm",
            description = "snow pack runoff [PACKRO]"
            )
            public Attribute.Double packRunoff_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "mm",
            description = "ground melt runoff [GMRO]"
            )
            public Attribute.Double groundMeltRunoff_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "mm",
            description = "pack melt [MELT]"
            )
            public Attribute.Double packMelt_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "mm",
            description = "rain on bare ground [ROBG]"
            )
            public Attribute.Double rainOnBareGround_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "mm",
            description = "refrozen water [SXRFRZ]"
            )
            public Attribute.Double refrozenWater_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "mm",
            description = "total output [RM]"
            )
            public Attribute.Double totalOutput_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "mm",
            description = "net surface energy exchange [QNET]"
            )
            public Attribute.Double netSurfaceEnergyExchange_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "mm",
            description = "snow evaporation"
            )
            public Attribute.Double snowEvap_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            //unit = "degC",
            description = "maximum temperature at timeStep-1"
            )
            public Attribute.Double tmaxPre;
    
    
    /***********************************************
     *calibration parameters
     **********************************************/
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "snow correction factor [SCF]"
            //unit=""
            )
            public Attribute.Double snowCorrectionFactor_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "minimum melt factor [MFMIN]"
            //unit = "mm/degC/6hr"
            )
            public Attribute.Double meltFactorMin_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "maximum melt factor [MFMAX]"
            //unit = "mm/degC/6hr"
            )
            public Attribute.Double meltFactorMax_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "average wind function [UADJ]"
            //unit = "mm/mb"
            )
            public Attribute.Double avgWindFunction_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "minimum SWE for 100% cover [SI]"
            //unit = "mm"
            )
            public Attribute.Double min100CoverSwe_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "ordinates of the areal depletion curve [ADC]"
            //unit = ""
            )
            public Attribute.DoubleArray arealDepletionCurve_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "maximum negative melt factor [NMF]"
            //unit = "mm/degC/6hr"
            )
            public Attribute.Double maxNegativeMeltFactor_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "antecedent temperature index parameter [TIPM]"
            //unit = ""
            )
            public Attribute.Double tipm_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "temperature which delineates rain from snow [PXTEMP]" +
            "not used in this version, because rain and snow comes as input"
            //unit = "degC"
            )
            public Attribute.Double pxtemp_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "percent (decimal) liquid water holding capacity [PLWHC]"
            //unit = "dec"
            )
            public Attribute.Double maxWaterCap_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "constant amount of melt at the snow-soil interface [DAYGM]"
            //unit = "mm/day"
            )
            public Attribute.Double groundMelt_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "base temperature for snowmelt computations during non-rain periods [MBASE]"
            //unit = "degC"
            )
            public Attribute.Double baseTemp_1;
    
    //flags
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "melt factor variation type: [LMFV]" +
            "0 & latitude < 54 default;" +
            "0 & latitude > 54 Alaska type;" +
            "1 user specified "
            //unit = ""
            )
            public Attribute.Integer lmfv_1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "module active"
            )
            public Attribute.Boolean active;
    
    
    //internal vars
    double sfnew, rfmin, sbci;
    int deltaTime, month, date, julDay;
    double packRunoff, groundMeltRunoff, rainOnBareGround, refrozenWater, totalOutput, packMelt, rainMelt, snowEvap;
    double[] exlag;
    double[] arealDepletionCurve = {0,0.07,0.21,0.30,0.36,0.41,0.45,0.51,0.60,0.70,1.0};
    double[] monthlyMeltFactor = {1,1,1,1,1,1,1,1,1,1,1,1}; //SMFV() - USER SPECIFIED MONTHLY MELT FACTORS;
    double precip, rainOut, rainIn, snowIn, airTemp, snowFall;
    double swe, liqWater, snowTemp, sb, sbws, sbaesc, maxAccumulation, aeAdj, tindex, liqWaterAttStorage, snowDepth, snowDens;
    double groundSnowLoss, groundWaterLoss, arealSnowCover, cnhs, negSnowHeat, netSurfaceEnergyExchange;
    boolean meltComputed;
    
    double dqnet, totalPackWater;
    int nosnow = 1;
    
    //unclear internal vars
    double snof, cnhspx;
    
    //calibration parameters
    double groundMelt, avgWindFunction, maxWaterCap;
    
    //CONSTANTS
    //IF SNOWFALL EXCEEDS SNEW/HR--TINDEX=TPX
    final double SNEW = 1.5;
    //     IF RAIN EXCEEDS RMIN/HR--USE RAIN-ON-SNOW MELT EQUATION
    final double RMIN = 0.25;
    //     SBC=STEFAN/BOLTZMAN CONSTANT--MM/(((DEGK/100)**4)*HR)
    final double SBC = 0.0612;
    
    //sublimation rate in mm per day - can be calibrated in PRMS should be calculated in my opinion
    double sublimationRate = 0;
    
        /*
         *  Component run stages
         */
    
    public void init() {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        if(this.active == null || this.active.getValue()){
            //exlag has not been initalised so far
            if(this.exlag_1.getValue() == null){
                double[] ex = {0., 0., 0., 0., 0., 0., 0.};
                this.exlag_1.setValue(ex);
            }
            
            int tu = timeInterval.getTimeUnit();
            if(tu == 6)
                deltaTime = 24 * timeInterval.getTimeUnitCount();
            if(tu == 11)
                deltaTime = timeInterval.getTimeUnitCount();
            
            //intial values
            sfnew = SNEW * deltaTime;//SFNEW=SNEW*DT
            rfmin = RMIN * deltaTime;//RFMIN=RMIN*DT
            sbci = SBC * deltaTime;//SBCI=SBC*DT
            double airPressure = 1012.4 - 11.34 * this.elevation.getValue()/100. + 0.00745 * Math.pow(this.elevation.getValue()/100., 2.4);
            
            //passing vars to local copies or setting initial vals
            //output data
            rainOut = 0;
            packRunoff = 0;
            groundMeltRunoff = 0;
            packMelt = 0;
            rainOnBareGround = 0;
            refrozenWater = 0;
            totalOutput = 0;
            meltComputed = false;
            rainMelt = 0;
            groundSnowLoss = 0;
            netSurfaceEnergyExchange = 0;
            
            //date and time
            this.month = this.time.get(Attribute.Calendar.MONTH);
            this.date = this.time.get(Attribute.Calendar.DATE);
            this.julDay = this.time.get(Attribute.Calendar.DAY_OF_YEAR);
            
            //input data
            rainIn = this.rain_1.getValue();
            snowIn = this.snow_1.getValue();
            precip = rainIn + snowIn;
            airTemp = this.airTemperature.getValue();
            
            //states
            swe = this.swe_1.getValue();
            liqWater = this.liqWater_1.getValue();
            snowTemp = this.snowTemp_1.getValue();
            sb = this.sb_1.getValue();
            sbws = this.sbws_1.getValue();
            sbaesc = this.sbaesc_1.getValue();
            maxAccumulation = this.maxAccumulation_1.getValue();
            aeAdj = this.aeAdj_1.getValue();
            tindex = this.tindex_1.getValue();
            exlag = this.exlag_1.getValue();
            liqWaterAttStorage = this.liqWaterAttStorage_1.getValue();
            snowDepth = this.snowDepth_1.getValue();
            snowDens = this.snowDens_1.getValue();
            arealSnowCover = this.arealSnowCover_1.getValue();
            cnhs = this.cnhs_1.getValue();
            negSnowHeat = this.negSnowHeat_1.getValue();
            
            //unclear states
            cnhspx = 0;
            
            //calibration parameters
            groundMelt = this.groundMelt_1.getValue();
            avgWindFunction = this.avgWindFunction_1.getValue();
            maxWaterCap = this.maxWaterCap_1.getValue();
            //arealDepletionCurve = this.arealDepletionCurve_1.getValue();
            
            double rfrac, sfrac = 0;
            if(precip > 0){
                rfrac = rainIn / precip;
                sfrac = snowIn / precip;
            } else{
                rfrac = 0;
                sfrac = 0;
            }
            
            //nwsmrun();
            pack19(airPressure, sfrac);
            
            //mapping vars and states back
            //output data
            this.rainOut_1.setValue(rainIn);
            this.packRunoff_1.setValue(packRunoff);
            this.groundMeltRunoff_1.setValue(groundMeltRunoff);
            this.packMelt_1.setValue(packMelt);
            this.rainOnBareGround_1.setValue(rainOnBareGround);
            this.refrozenWater_1.setValue(refrozenWater);
            this.totalOutput_1.setValue(totalOutput);
            this.netSurfaceEnergyExchange_1.setValue(netSurfaceEnergyExchange);
            
            //states
            this.swe_1.setValue(swe);
            this.liqWater_1.setValue(liqWater);
            this.snowTemp_1.setValue(snowTemp);
            this.sb_1.setValue(sb);
            this.sbws_1.setValue(sbws);
            this.sbaesc_1.setValue(sbaesc);
            this.maxAccumulation_1.setValue(maxAccumulation);
            this.aeAdj_1.setValue(aeAdj);
            this.tindex_1.setValue(tindex);
            this.exlag_1.setValue(exlag);
            this.liqWaterAttStorage_1.setValue(liqWaterAttStorage);
            this.snowDepth_1.setValue(snowDepth);
            this.snowDens_1.setValue(snowDens);
            this.arealSnowCover_1.setValue(arealSnowCover);
            this.cnhs_1.setValue(cnhs);
            this.negSnowHeat_1.setValue(negSnowHeat);
        }
    }
    
    private boolean pack19(double airPressure, double sfrac){
        if(swe == 0 && precip == 0){
            finish(airTemp);
        } else{
            //cnhspx = 0;
            //rainMelt = 0;
            if(precip == 0){
                return calcGroundMelt(airTemp, airPressure);
            } else{
                if(sfrac > 0){ //105
                    //accumulate snowFall
                    snowTemp = airTemp;
                    if(snowTemp > 0)
                        snowTemp = 0;
                    snowFall = precip * sfrac * this.snowCorrectionFactor_1.getValue();
                    double xxs = swe + liqWater;
                    if(xxs < sbws){
                        if(snowFall >= snof)
                            sbws = swe + liqWater + 0.75 * snowFall;
                    } else{
                        sbws = sbws + 0.75 * snowFall;
                        if((snowFall >= snof) && (sb > xxs))
                            sb = swe + liqWater;
                    }
                    //107
                    swe = swe + snowFall;
                    xxs = swe + liqWater;
                    double xcst = 3.0 * sb;
                    if(xxs >= xcst){
                        maxAccumulation = swe + liqWater;
                        aeAdj = 0.0;
                    }
                    //108
                    cnhspx = -1 * snowTemp * snowFall / 160.0;
                    if(snowFall > sfnew)
                        tindex = snowTemp;
                }
                //109
                rainIn = precip * (1 - sfrac);
                
                /*
                 *PK: pRain commented out because not needed
                 */
                //pRain = pRain + rain;
                
                if(swe == 0){
                    return finish(airTemp);
                }
                /*
                 *PK: dRain commented out because not needed
                 */
                //dRain = dRain + rain;
                double rainTemp = airTemp;
                if(rainTemp < 0)
                    rainTemp = 0;
                rainMelt = 0.0125 * rainIn * rainTemp;
                return calcGroundMelt(airTemp, airPressure);
            }
        }
        return true;
    }
    
    public void cleanup() {
        
        
    }
    
    private boolean calcGroundMelt(double airTemp, double airPressure){
        //melt at ground-snow interface
        if(swe > groundMelt){
            //111
            double gmwlos = (groundMelt / swe) * liqWater;
            groundSnowLoss = groundMelt;
            //compute surface energy exchange for the computational period
            //based on 100% cover and non-rain conditions
            if(!meltComputed){
                melt19(airTemp);//, melt, tindex, cnhs);
                //System.out.println("packMelt: " + packMelt + " cnhs: " + cnhs);
                meltComputed = true;
            }                //115
            //determine melt for the time interval - surface energy exchange
            //is uniform during the computational period
            if(rainIn > rfmin){
                //rain interval
                double ea = 2.7489E8 * Math.exp(-4278.63 / (airTemp + 242.792));
                //assume 90% rel humidity during rain-on snow
                //@todo: replace by measurment
                ea = 0.9 * ea;
                double tak = (airTemp + 273) * 0.01;
                double tak4 = Math.pow(tak, 4);
                double qn = sbci * (tak4 - 55.55);
                double qe = 8.5 * (ea - 6.11) * avgWindFunction;
                double qh = 7.5 * 0.000646 * airPressure * avgWindFunction * airTemp;
                packMelt = qn + qe + qh + rainMelt;
                if(packMelt < 0)
                    packMelt = 0;
            } else{
                //non-rain or light dizzle interval
                packMelt = packMelt + rainMelt;
            }
            //130 calc areal snow cover
            aesc19();
            if(arealSnowCover < 1.0){
                //adjust values by arealSnowCover
                packMelt = packMelt * arealSnowCover;
                cnhs = cnhs * arealSnowCover;
                gmwlos = gmwlos * arealSnowCover;
                groundSnowLoss = groundSnowLoss * arealSnowCover;
                //compute rain falling on bare ground
                rainOnBareGround = (1 - arealSnowCover) * rainIn;
                rainIn = rainIn - rainOnBareGround;
            } else{
                rainOnBareGround = 0;
            }
            double xxs = cnhs + negSnowHeat;
            if(xxs < 0)
                cnhs = -1 * negSnowHeat;
            swe = swe - groundSnowLoss;
            liqWater = liqWater - gmwlos;
            groundMeltRunoff = groundSnowLoss + gmwlos;
            
            if(packMelt > 0){
                if(packMelt >= swe){
                    packMelt = swe + liqWater;
                    netSurfaceEnergyExchange = packMelt;
                    return setNoSnowConditions(airTemp);
                }else{
                    swe = swe - packMelt;
                }
            }
            //net surface energy exchange in mm WE
            netSurfaceEnergyExchange = packMelt - cnhs - cnhspx;
            //compute heat and water balance for the snow cover
            double water = packMelt + rainIn;
            double heat = cnhs + cnhspx;
            double maxLiqWater = maxWaterCap * swe;
            negSnowHeat = negSnowHeat + heat;
            
            //temperature of snow should not be below 52.8°C
            if(negSnowHeat < 0)
                negSnowHeat = 0;
            double xcst = 0.33 * swe;
            double xxs1 = water + liqWater;
            double xxs2 = maxLiqWater + negSnowHeat + maxWaterCap * negSnowHeat;
            
            double excess = 0;
            if(negSnowHeat > xcst)
                negSnowHeat = 0.33 * swe;
            if(xxs1 < xxs2){
                //140
                xxs = water + liqWater;
                if(xxs < negSnowHeat){
                    //all liquid water is refrozen in snow cover
                    swe = swe + water + liqWater;
                    negSnowHeat = negSnowHeat - water - liqWater;
                    refrozenWater = refrozenWater + water + liqWater;
                    liqWater = 0;
                    excess = 0;
                } else{
                    //water exceeds negSnowHeat - liquid water content is increased
                    liqWater = liqWater + water - negSnowHeat;
                    swe = swe + negSnowHeat;
                    
                    //PK: added for consistency
                    packMelt = packMelt - negSnowHeat;
                    
                    //cumulate refrozen water
                    refrozenWater = refrozenWater + negSnowHeat;
                    negSnowHeat = 0;
                    excess = 0;
                }
            } else{
                //excess water exists
                excess = water + liqWater - maxLiqWater - negSnowHeat - maxWaterCap * negSnowHeat;
                liqWater = maxLiqWater + maxWaterCap * negSnowHeat;
                swe = swe + negSnowHeat;
                negSnowHeat = 0;
            }
            //145
            if(negSnowHeat == 0)
                tindex = 0;
            //route excess water through the snow cover
            //rout19(excess);
            noRout(excess);
            //add groundmelt runoff to snow cover outflow
            packRunoff = packRunoff + groundMeltRunoff;
            return finish(airTemp);
            
        } else{
            groundMeltRunoff = swe + liqWater;
            packMelt = 0;
            rainOnBareGround = rainIn;
            rainIn = 0;
            return setNoSnowConditions(airTemp);
        }
    }
    
    /*.......................................
     *     SUBROUTINES COMPUTES SURFACE MELT BASED ON 100 PERCENT
     *        SNOW COVER AND NON-RAIN CONDITIONS.
     *.......................................
     *     INITIALLY WRITTEN BY...
     *        ERIC ANDERSON - HRL   MAY 1980
     *.......................................*/
    private boolean melt19(double airTemp){
        int[] mmd ={301,332,361,26,56,87,117,148,179,209,240,270};
        cnhs = 0;
        packMelt = 0;
        double diff = this.meltFactorMax_1.getValue() - this.meltFactorMin_1.getValue();
        int dayn = julDay;
        double meltFactor = 0;
        double x = 0;
        double adjmf = 0;
        
        if(this.lmfv_1.getValue() == 0){
            if(this.latitude.getValue() < 54){
                //120 melt factor variation for the lower 48
                meltFactor = (Math.sin(julDay * 2 * Math.PI / 366) * diff * 0.5) + (this.meltFactorMax_1.getValue() + this.meltFactorMin_1.getValue()) * 0.5;
            } else{
                //melt factor variation for Alaska
                if(julDay >= 275){
                    //102
                    x = (julDay - 275) / (458 - 275);
                } else if(julDay >= 92){
                    //101
                    x = (275 - julDay) / (275 - 92);
                } else{
                    x = (91 + julDay) / 183;
                }
                //105
                double xx = (Math.sin(julDay * 2 * Math.PI / 366) * 0.5) + 0.5;
                if(x <= 0.48){
                    //111
                    adjmf = 0;
                } else if(x >= 0.7){
                    //112
                    adjmf = 1.0;
                } else{
                    adjmf = (x - 0.48) / (0.7 - 0.48);
                }
                //110
                meltFactor = (xx * adjmf) * diff + this.meltFactorMin_1.getValue();
            }
        } else{
            //user specified melt factor variation
            int mb = month;
            if(dayn < mmd[month])
                mb = mb - 1;
            if(mb < 0)
                mb = 11;
            int ma = mb + 1;
            if(ma == 12)
                ma = 1;
            
            int md = mmd[ma] - mmd[mb];
            if(md < 0)
                md = md + 366;
            int nd = dayn - mmd[mb];
            if(nd < 0)
                nd = nd + 366;
            
            adjmf = monthlyMeltFactor[mb] + (nd / md) * monthlyMeltFactor[ma] - monthlyMeltFactor[mb];
            meltFactor = this.meltFactorMin_1.getValue() + adjmf * diff;
        }
        //125
        double ratio = meltFactor / this.meltFactorMax_1.getValue();
        //compute melt and negative heat exchange index temperatures
        double tmx = airTemp - this.baseTemp_1.getValue();
        if(tmx < 0)
            tmx = 0;
        double tsur = airTemp;
        if(tsur > 0)
            tsur = 0;
        double tnmx = tindex - tsur;
        //negative heat exchange
        double nmrate = ratio * this.maxNegativeMeltFactor_1.getValue();
        cnhs = nmrate * tnmx;
        //update tindex
        tindex = tindex + this.tipm_1.getValue() * (airTemp - tindex);
        if(tindex > 0)
            tindex = 0;
        if(tmx > 0){
            //surface melt
            packMelt = meltFactor * tmx;
        }
        return true;
    }
    
    /*.......................................
     *     THIS SUBROUTINE COMPUTES THE AREAL EXTENT OF SNOW COVER USING THE
     *     AREAL DEPLETION CURVE FOR THE 'SNOW-17 ' OPERATION.
     *......................................
     *     SUBROUTINE INITIALLY WRITTEN BY...
     *     ERIC ANDERSON - HRL   MAY 1980
     *.......................................*/
    private boolean aesc19(){
        double twe = swe + liqWater;
        if(twe > maxAccumulation)
            maxAccumulation = twe;
        if(twe >= aeAdj)
            aeAdj = 0;
        double ai = maxAccumulation;
        
        if(maxAccumulation > this.min100CoverSwe_1.getValue())
            ai = this.min100CoverSwe_1.getValue();
        if(aeAdj > 0)
            ai = aeAdj;
        
        if(twe >= ai){
            //105
            sb = twe;
            sbws = twe;
            //115
            arealSnowCover = 1.0;
        } else if(twe <= sb){
            //110
            //fortran indices from 1 to n
            int r = (int)((twe / ai) * 10 + 1);
            //java indices from 0 to n
            r = r - 1;
            
            int n = r;
            int fn = n;
            r = r - fn;
            arealSnowCover = arealDepletionCurve[n] + (arealDepletionCurve[n+1] - arealDepletionCurve[n]) * r;
            if(arealSnowCover > 1)
                arealSnowCover = 1;
            sb = twe + snof;
            sbws = twe;
            sbaesc = arealSnowCover;
        } else if(twe >= sbws){
            arealSnowCover = 1.0;
        } else{
            arealSnowCover = sbaesc + ((1.0 - sbaesc) * ((twe - sb)/(sbws - sb)));
        }
        //120
        if(arealSnowCover < 0.05)
            arealSnowCover = 0.05;
        if(arealSnowCover > 1)
            arealSnowCover = 1;
        return true;
    }
    
    private boolean snowDepth(double airTemp, double liqW){
        //adjust snow density due to snow fall
        double dhc = 0;
        double sdn = 0;
        double tsnew = airTemp;
        
        if(snowFall > 0){
            sdn = calcNewSnowDensity(airTemp);
            dhc = calcSnowDepth(snowFall, sdn);
            /*if(sdn >= 0.9)
                System.out.println("break");
            if(snowDens >= 0.9)
                System.out.println("break");*/
            tsnew = calcSnowTemperature(dhc, sdn, snowFall, 0.0, airTemp, 0.0, tsnew, 0.0);
            
            double[] depthDensity;
            depthDensity = calcSnowCompaction(snowFall, deltaTime, sdn, 0, 0, 0, tsnew);
            dhc = depthDensity[0];
            sdn = depthDensity[1];
        }
        if(snowDepth > 0.0001){
            
            //calculate old snow compaction / metamorphism
            double shn = dhc + snowDepth;
            double dsn = (sdn * dhc + snowDens * snowDepth) / shn;
            /*if(dsn >= 0.9)
                System.out.println("break");
            if(snowDens >= 0.9)
                System.out.println("break");*/
            snowTemp = calcSnowTemperature(shn, dsn, swe, liqW, airTemp, 0.0, snowTemp, dhc);
            /*if(snowTemp < -900)
                System.out.println("break");*/
            double[] depthDensity;
            depthDensity = calcSnowCompaction(swe, deltaTime, snowDens, liqW, snowFall, refrozenWater, snowTemp);
            snowDepth = depthDensity[0];
            snowDens = depthDensity[1];
            //account for ground melt
            if(groundSnowLoss > 0){
                snowDepth = snowDepth - 0.1 * groundSnowLoss / snowDens;
            }
            if(snowDepth < 0)
                snowDepth = 0;
            //combine new snow fall and old snow pack
            snowTemp = (snowTemp * snowDepth + tsnew * dhc) / (snowDepth + dhc);
            /*if(snowTemp < -900)
                System.out.println("break");*/
            snowDepth = snowDepth + dhc;
            snowDens = 0.1 * swe / snowDepth;
            /*if(snowDens >= 0.9)
                System.out.println("break");*/
        } else{
            //there was no snowpack before this snowfall
            snowDepth = dhc;
            snowDens = sdn;
            //account for groundmelt
            if(groundSnowLoss > 0){
                snowDepth = snowDepth - 0.1 * groundSnowLoss / snowDens;
            }
            if(snowDepth < 0)
                snowDepth = 0;
            snowTemp = tsnew;
        }
        return true;
    }
    
    private double calcNewSnowDensity(double airTemp){//, double depth, double dens){
        
        double dens = 0;
        if(airTemp <= -15){
            dens = 0.05;
        } else{
            dens = 0.05 + 0.0017 * Math.pow((airTemp + 15), 1.5);
        }
        
        
        return dens;
    }
    
    private double calcSnowDepth(double precip, double density){
        double depth = 0;
        //convert precip from mm to cm
        double px = 0.1 * precip;
        //calculate snow depth
        depth = px / density;
        return depth;
    }
    
    private double calcSnowTemperature(double depth, double dens, double we, double liqW, double aTemp, double tempChange, double sTemp, double depthChange){
//@todo DTA is not clear yet, therefore I have set it to airTemperature
        double dta = aTemp;
        
        double newTemp = 0;
        //specific heat capacity of ice, water, and air
        double cice = 2.1E06;
        double ch2o = 4.2E06;
        double cair = 1.0E03;
        //heat wave length in seconds
        double wavel = 43200;
        double shx = 0.01 * depth;
        double dhcx = 0.01 * depthChange;
        double stot = we + liqW;
        double dst = 0.1 * stot / depth;
        double sl = 0.0442 * Math.exp(5.181 * dst);
        double fl = liqW / stot;
        double sc = cice * dens + cair * (1 - dens - fl) + ch2o * fl;
        double alp = Math.sqrt(Math.PI * sc / (wavel * sl));
        
        if(depthChange < 0){
            newTemp = sTemp + dta * ((Math.exp(-alp * dhcx) - Math.exp(-alp * shx)) / (alp * (shx - dhcx)));
        } else{
            newTemp = sTemp + dta * ((Math.exp(-alp * shx))/(alp * shx));
        }
        if(newTemp > 0){
            newTemp = 0;
        }
        /*if(newTemp < -300)
            System.out.println("break");*/
        return newTemp;
    }
    
    /****************************************************************
     **  SUBROUTINE TO CALCULATE SNOW COMPACTION AND METAMORPHISM ***
     **  EQUATIONS OF INCREASING OF SNOW DENSITY WERE OBTAINED AS ***
     **  AN APPROXIMATE SOLUTIONS OF E. ANDERSON DIFFERENTIAL     ***
     **  EQUATIONS (3.29) AND (3.30), (3.31), NOAA TECHNICAL      ***
     **  REPORT NWS 19, by   VICTOR KOREN   03/25/95              ***
     ***************************************************************/
    //returns depth = out[0] and density = out[1]
    private double[] calcSnowCompaction(double we, int timeStep, double dens,
            double liqW, double sFall, double refreeze, double sTemp){
        double c1 = 0.01;
        double c2 = 21.0;
        double c3 = 0.01;
        double c4 = 0.04;
        double rds = 0.2;
        double c5 = 2.0;
        double cx = 46.0;
        
        double[] out = new double[2];
        //conversion into simulation units
        double wx = we * 0.1;
        //calculation of snow depth and density as a result of compaction
        //c1 is the fractional increase in density [ 1 / (cm * hr)]
        //c2 is a constant (cm³/g) Kojima estimated as 21.0 cm³/g
        
        double dsc = 1.0;
        if(wx > 1E-2){
            double b = timeStep * c1 * Math.exp(0.08 * sTemp - c2 * dens);
            dsc = (Math.expm1(b * wx) + 1) / (b * wx);
        }
        //calculate the density change as a result of snow metamorphism
        //c3 is the fractional settling rate at 0 degC for densitities less than threshold density rds
        //c4 is a constant
        //c5 is an empirical adjustment factor that accounts for melt metamorphosis
        double a = c3;
        if(liqW > 0)
            a = a * c5;
        double c = c4 * sTemp;
        if(dens > rds)
            c = c - cx * (dens - rds);
        double dsm = Math.exp(a * timeStep * Math.exp(c));
        //new snow density as a result of compaction / metamorphism
        double dsx = dens * dsc * dsm;
        if(dsx > 0.45)
            dsx = 0.45;
        if(dsx < 0.05)
            dsx = 0.05;
        dens = dsx;
        double dwx = wx - 0.1 * (sFall + refreeze);
        double depth = 0;
        if(dwx > 0)
            depth = dwx / dens;
        else if(wx > 0)
            depth = wx / dsx;
        else
            depth = 0;
        
        out[0] = depth;
        out[1] = dens;
        return out;
    }
    
    /* THIS SUBROUTINE ROUTES EXCESS WATER THROUGH THE SNOW COVER FOR
     * THE 'SNOW-17 ' OPERATION.
     * .......................................
     * SUBROUTINE INITIALLY WRITTEN BY...
     * ERIC ANDERSON - HRL   MAY 1980*/
    private boolean rout19(double excessWater){
        packRunoff = 0;
        double cl = 0.03 * deltaTime / 6.0;
        if(excessWater > 0){
            if((excessWater >= 0.1) || (swe >= 1.0)){
                double n = Math.pow((excessWater * 4), 0.3) + 0.5;
                if(n == 0)
                    n = 1;
                for(int i = 0; i < n; i++){
                    int fi = i;
                    double term = cl * swe * n / (excessWater * (fi - 0.5));
                    if(term > 150)
                        term = 150;
                    double flag = 5.33 * (1 - Math.exp(-1 * term));
                    int L2 = (int)((flag + deltaTime) / deltaTime + 1);
                    int L1 = L2 - 1;
                    int endL1 = L1 * deltaTime;
                    double por2 = (flag + deltaTime - endL1) / deltaTime;
                    double por1 = 1 - por2;
                    if(L2 < 0)
                        L2 = 0;
                    if(L1 < 0)
                        L1 = 0;
                    if(L2 > exlag.length - 1)
                        L2 = exlag.length - 1;
                    if(L1 > exlag.length - 1)
                        L1 = exlag.length - 1;
                    exlag[L2] = exlag[L2] + por2 * excessWater / n;
                    exlag[L1] = exlag[L1] + por1 * excessWater / n;
                }
            } else{
                //120 excessWater or swe small, thus no lag
                exlag[0] = exlag[0] + excessWater;
            }
        }
        //150 attenuate lagged excessWater - function of liqWaterAttStorage and swe
        double xxs = liqWaterAttStorage + exlag[0];
        if(xxs > 0){
            if(xxs >= 0.1){
                //160 effect of attenuation computed using a one-hour timeStep
                double el = exlag[0] / deltaTime;
                double els = el / (25.4 * arealSnowCover);
                double wes = swe / (25.4 * arealSnowCover);
                double term = 500 * els / (Math.pow(wes, 1.3));
                if(term > 150)
                    term = 150;
                double r1 = 1 / (5 * Math.exp(-1 * term) + 1);
                for(int i = 0; i < deltaTime; i++){
                    double os = (liqWaterAttStorage + el) * r1;
                    packRunoff = packRunoff + os;
                    liqWaterAttStorage = liqWaterAttStorage + el - os;
                }
                if(liqWaterAttStorage <= 0.001){
                    packRunoff = packRunoff + liqWaterAttStorage;
                    liqWaterAttStorage = 0;
                }
            } else{
                //no attenuation
                packRunoff = liqWaterAttStorage + exlag[0];
                liqWaterAttStorage = 0;
            }
        }
        //190 downshift water in exlag[]
        for(int i = 1; i < exlag.length; i++){
            exlag[i-1] = exlag[i];
        }
        exlag[exlag.length - 1] = 0;
        return true;
    }
    
    /******************************************
     *PK:
     *dummy function which by-passes the lag and
     *water routing through the snow pack
     *implemented for debug purposes
     ******************************************/
    private boolean noRout(double excessWater){
        packRunoff = packRunoff + excessWater - rainIn;
        excessWater = 0;
        return true;
    }
    
    private boolean setNoSnowConditions(double airTemp){
        //snow gone - set all carryover to no snow conditions
        double tex = 0;
        for(int i = 0; i < exlag.length; i++)
            tex = tex + exlag[i];
        
        packRunoff = groundMeltRunoff + packMelt + tex + liqWaterAttStorage;// + rain;
        zero19();
        arealSnowCover = 0;
        return finish(airTemp);
    }
    
    //sets all carryover values to no-snow conditions
    private boolean zero19(){
        swe = 0;
        negSnowHeat = 0;
        liqWater = 0;
        tindex = 0;
        maxAccumulation = 0;
        sb = 0;
        sbaesc = 0;
        sbws = 0;
        liqWaterAttStorage = 0;
        aeAdj = 0;
        snowDepth = 0;
        snowTemp = 0;
        for(int i = 0; i < exlag.length; i++)
            exlag[i] = 0;
        
        return true;
    }
    
    private boolean finish(double airTemp){
        //compute rain and melt
        totalOutput = packRunoff + rainOnBareGround;
        //set simulated areal snow cover and total swe
        double tex = 0;
        for(int i = 0; i < exlag.length; i++)
            tex = tex + exlag[i];
        
        if(swe > 0){
            //determine snow depth
            double sliq = liqWater + tex + liqWaterAttStorage;
            snowFall = snowFall - packMelt;
            if(snowFall < 0)
                snowFall = 0;
            snowDepth(airTemp, sliq);
        } else{
            snowDepth = 0;
            snowTemp = 0;
            snowDens = 0.1;
        }
        double totalWE = swe + liqWater + tex + liqWaterAttStorage;
        if(totalWE > 0){
            aesc19();
        }
        /*
         *PK: cover commented out because not needed
         */
        //cover = arealSnowCover;
        return true;
    }
    
    
    
    
    public double calcMeltFactor(int jDay, double lat, double mfMax, double mfMin){
        double diff = mfMax - mfMin;
        int dayN;
        double meltFactor = 0;
        double x = 0;
        double adjmf = 0;
        
        /**
         *conversion of julDay to solar year i.e. 1 = 21st of March
         */
        if(julDay > 79)
            dayN = julDay - 79;
        else
            dayN = 366 + julDay - 79;
        
        //meltFactor for lower latitudes
        if(lat < 54){
            meltFactor = (Math.sin(dayN * 2.0 * Math.PI / 366.0) * diff * 0.5) + (mfMax + mfMin) * 0.5;
        }
        //meltFactor for Alaska
        else{
            if(julDay >= 275){
                //102
                x = (dayN - 275) / (458 - 275);
            }
            else if(julDay >= 92){
                //101
                x = (275. - dayN)/(275 - 92);
            }
            else{
                x = (91 + dayN) / 183;
            }
            //105
            double xx = (Math.sin(dayN * 2 * Math.PI/366) * 0.5) + 0.5;
            if(x <= 0.48){
                adjmf = 0;
            }
            else if(x >= 0.7){
                adjmf = 1;
            }
            else{
                adjmf = (x - 0.48)/(0.7 - 0.48);
            }
            meltFactor = (xx * adjmf) * diff + mfMin;
        }
        return meltFactor;
    }
    
    public double callMeltrate(double airTemp, double ratio, double rain, double meltFactor, int subInt, double airPressure){
        double meltRate = 0;
        double tmx = airTemp - baseTemp_1.getValue();
        if(tmx < 0)
            tmx = 0;
        double tsur = airTemp;
        if(tsur > 0)
            tsur = 0;
        
        //negative heat exchange index
        double nmindex = tindex - tsur;
        double nmrate = ratio * maxNegativeMeltFactor_1.getValue();
        cnhs = nmrate * nmindex;
        //calc new tindex for next time step
        tindex = tindex + tipm_1.getValue() * (airTemp - tindex);
        
        //little or no rainfall -- no rainfall melt equation
        if(rain <= rfmin){
            meltRate = meltFactor * tmx + rainMelt;
            if(subInt > 0 && subInt < 3)
                snowEvap = snowEvap + sublimationRate * 0.5;
        }
        //rain on snow melt
        else{
            double ea = 2.7489E8 * Math.exp(-4278.63 / (airTemp + 242.792));
            //0.9 is assumed rel.Humidity - can be subsituted by measured value!
            ea = ea * 0.9;
            double tak4 = Math.pow((airTemp + 273.16) * 0.01, 4);
            double qn = sbci * (tak4 - 55.55);
            double qe = 8.5 * (ea - 6.11) * avgWindFunction_1.getValue();
            double qh = 7.5 * 0.000646 * airPressure * avgWindFunction_1.getValue() * airTemp;
            
            meltRate = qn + qe + qh + rainMelt;
        }
        if(meltRate < 0)
            meltRate = 0;
        
        return meltRate;
        
    }
    
    private double callAeSnow(){
        double aesc = 0;
        
        double twe = swe + liqWater;
        if(twe > maxAccumulation)
            maxAccumulation = twe;
        double ai = maxAccumulation;
        if(maxAccumulation > min100CoverSwe_1.getValue())
            ai = min100CoverSwe_1.getValue();
        
        if(twe >= ai){
            //202
            sb = twe;
            sbws = twe;
            aesc = 1.0;
        }
        else if(twe <= sb){
            //201
            double r = (twe / ai) * 10.0;// + 1.0; //FORTRAN vs. JAVA array notation
            double n = r;
            double fn = n;
            r = r - fn; //PK: by these equation r is always zero, I do not understand this ...
            int idx = (int)n;
            //System.out.println("idx: " + idx);
            aesc = arealDepletionCurve[idx] + (arealDepletionCurve[idx+1] - arealDepletionCurve[idx]) * r;
            if(aesc > 1)
                aesc = 1;
            sb = twe + 1.27;
            sbaesc = aesc;
        }
        else if(twe >= sbws){
            //203
            aesc = 1;
        }
        if(aesc < 0.05)
            aesc = 0.05;
        if(aesc > 1)
            aesc = 1;
        
        return aesc;
    }
    
    private double callHeatWater(double rainInTs){
        double excessWater;
        double water = packMelt + rainInTs;
        double heat = cnhs + cnhspx;
        double liqWMax = maxWaterCap_1.getValue() * swe;
        negSnowHeat = negSnowHeat + heat;
        
        if(negSnowHeat < 0)
            negSnowHeat = 0;
        if(negSnowHeat > (0.33 * swe))
            negSnowHeat = 0.33 * swe;
        
        //is water exceeding the maximum capacity?
        if((water + liqWater) < (liqWMax + negSnowHeat + maxWaterCap_1.getValue() * negSnowHeat)){
            //230
            if(water < negSnowHeat){
                //231
                swe = swe + water;
                negSnowHeat = negSnowHeat - water;
                excessWater = 0;
            }
            else{
                liqWater = liqWater + water - negSnowHeat;
                swe = swe + negSnowHeat;
                negSnowHeat = 0;
                excessWater = 0;
            }
        }
        else{
            excessWater = water + liqWater - liqWMax - negSnowHeat - maxWaterCap_1.getValue() * negSnowHeat;
            liqWater = liqWMax + maxWaterCap_1.getValue() * negSnowHeat;
            swe = swe + negSnowHeat;
            negSnowHeat = 0;
        }
        //232
        if(tindex > 0)
            tindex = 0;
        if(negSnowHeat == 0)
            tindex = 0;
        
        return excessWater;
    }
    
    private double callRoute(int hoursInTimeStep, double excessWater){
        double packro = 0;
        double cl = 0.03 * hoursInTimeStep / 6.0;
        double prior = liqWaterAttStorage + exlag[0];
        boolean hundredfifty = false;
        boolean hundredsixty = false;
        boolean hundredninety = false;
        //lag excess water, function of amount of excess and water equiv
        if(excessWater == 0 && prior == 0){
            //190
            hundredninety = true;
            
        }
        else if(excessWater == 0 && prior >= 0.1){
            //160
            hundredsixty = true;
            
        }
        else if(excessWater < 0.1 || swe < 1.0){
            //120 excess or we small, thus no lag
            exlag[0] = exlag[0] + excessWater;
            hundredfifty = true;
        }
        else{
            double n = Math.pow(excessWater * 4., 0.3) + 0.5;
            if(n == 0)
                n = 0;  //Fortran vs Java array indices
            double fn = n;
            for(int i = 0; i < n; i++){
                double fi = i+1;
                double term = cl * swe * fn / (excessWater * (fi - 0.5));
                if(term > 150)
                    term = 150;
                double flag = 5.33 * (1 - Math.exp(-term));
                double dl2 = (flag + hoursInTimeStep) / hoursInTimeStep + 1.0;
                double dl1 = dl2 - 1;
                double endl1 = dl1 * hoursInTimeStep;
                double por2 = (flag + hoursInTimeStep - endl1) / hoursInTimeStep;
                double por1 = 1.0 - por2;
                
                int l1 = (int)dl1;// - 1; //Java vs. Fortran
                int l2 = (int)dl2;// - 1; //Java vs. Fortran
                
                //System.out.println("L1: " + l1 + " L2: " + l2);
                exlag[l2] = exlag[l2] + por2 * excessWater / fn;
                exlag[l1] = exlag[l1] + por1 * excessWater / fn;
            }
        }
        if(hundredfifty){
        //150
            if((liqWaterAttStorage + exlag[0]) == 0){
                hundredninety = true;
                hundredsixty = false;
            }
            else if((liqWaterAttStorage + exlag[0]) >= 0.1){
                hundredsixty = true;
            }
            else{
                packro = liqWaterAttStorage + exlag[0];
                liqWaterAttStorage = 0;
                hundredninety = true;
            }
        }
        if(hundredsixty){
            double el = exlag[0] / hoursInTimeStep;
            double els = el / (25.4 * arealSnowCover);
            double wes = swe / (25.4 * arealSnowCover);
            double term = 500 * els / (Math.pow(wes,1.3));
            if(term > 150)
                term = 150;
            double r1 = 1.0 / (5 * Math.exp(-term) + 1.0);
            double os = 0;
            for(int i = 0; i < exlag.length; i++){
                os = (liqWaterAttStorage + el) * r1;
                packro = packro + os;
                liqWaterAttStorage = liqWaterAttStorage + el - os;
            }
            if(liqWaterAttStorage > 0.001){
                hundredninety = true;
            }
            else{
                packro = packro + liqWaterAttStorage;
                liqWaterAttStorage = 0;
            }
        }
        hundredninety = true;
        if(hundredninety){
            //downshift water in exlag
            for(int i = 1; i < exlag.length; i++){
                exlag[i-1] = exlag[i];
            }
            exlag[exlag.length - 1 ] = 0;
        }
        
        return packro;
    }
}
