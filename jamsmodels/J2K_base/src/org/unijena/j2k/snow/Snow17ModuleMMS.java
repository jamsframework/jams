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
        "the original sources of Eric Anderson in a reworked version for the ModularModellingSystem MMS" +
        "some very minor adaptation were necessary to make the routine JAMS compatible"
        )
        public class Snow17ModuleMMS extends JAMSComponent {
    
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
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            //unit = "",
            description = "total pack water"
            )
            public Attribute.Double totalPackWater_1;
    
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
    
    double pmSave; 
    
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
            pmSave = 0;
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
            
            //input data + conversion to mm
            rainIn = this.rain_1.getValue() / this.area.getValue();
            snowIn = this.snow_1.getValue() / this.area.getValue();
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
            
            nwsmrun();
            
            double lagged = 0;
            for(int i = 0; i < exlag.length; i++){
                lagged = lagged + exlag[i];
            }
            totalPackWater = swe + liqWater + liqWaterAttStorage + lagged;
            
            //mapping vars and states back
            //output data
            this.rainOut_1.setValue(rainOut * this.area.getValue());
            this.packRunoff_1.setValue(packRunoff * this.area.getValue());
            this.groundMeltRunoff_1.setValue(groundMeltRunoff);
            this.packMelt_1.setValue(pmSave);
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
            this.liqWaterAttStorage_1.setValue(this.liqWaterAttStorage);
            //this.liqWaterAttStorage_1.setValue(lagged);
            this.snowDepth_1.setValue(snowDepth);
            this.snowDens_1.setValue(snowDens);
            this.arealSnowCover_1.setValue(arealSnowCover);
            this.cnhs_1.setValue(cnhs);
            this.negSnowHeat_1.setValue(negSnowHeat);
            this.totalPackWater_1.setValue(totalPackWater * this.area.getValue());
            this.snow_1.setValue(0);
        }
    }
    
    public void cleanup() {
        
        
    }
    
    public void nwsmrun(){
        this.packRunoff = 0;
        if(swe == 0 && precip == 0){
            //no snow pack no precip --> nothing to do
            return;
        }
        
        //set the 4 6-hour temperature values, calculate the average, save today max
        double[] t6hr = new double[4];
        double tmnpst = tmin.getValue() - tmax.getValue() + tmaxPre.getValue();
        t6hr[0] = 0.95 *  tmin.getValue() + 0.05  * tmaxPre.getValue();
        t6hr[1] = 0.40 *  tmin.getValue() + 0.60  * tmax.getValue();
        t6hr[2] = 0.925 * tmax.getValue() + 0.025 * tmin.getValue() + 0.05 * tmnpst;
        t6hr[3] = 0.33 *  tmax.getValue() + 0.67  * tmnpst;
        
        tmaxPre.setValue(tmax.getValue()); //save today's max for tomorrow's calculation
        
        //BEGINPACK = initial total water in snowpack (SWE + LIQ + LAG + STOR)
        double lagged = 0;
        for(int i = 0; i < exlag.length; i++)
            lagged = lagged + exlag[i];
        
        double beginpack = swe + liqWater + lagged + liqWaterAttStorage;
        
        //initial daily values, including melt coefficients for this day and atm.pressure
        double dsfall = 0;  //daily snowfall
        double drain = 0;   //daily rainfall
        double dqnet = 0;   //daily netRadiation, calculated
        
        //calculate the meltfactor for this day
        double meltFactor = calcMeltFactor(julDay, latitude.getValue(), meltFactorMax_1.getValue(), meltFactorMin_1.getValue());
        double ratio = meltFactor / meltFactorMax_1.getValue();
        
        //air pressure
        double elev100 = this.elevation.getValue() / 100.;
        double airPressure = 1012.4 - 11.34 * elev100 + 0.00745 * Math.pow(elev100, 2.4);
        
        //get time step in hours
        //JAMS specific
        int tsHours = 0;
        int tu = this.timeInterval.getTimeUnit();
        //daily time steps
        if(tu == 6){
            tsHours = 24;
        }
        //hourly time steps
        else if(tu == 5){
            tsHours = this.timeInterval.getTimeUnitCount();
        }
        int ndt = 24 / tsHours;
        tsHours = 24 / ndt;
        //set to 6-hours right now, because everything else is not working
        if(ndt != 4)
            ndt = 4;
        if(tsHours != 6)
            tsHours = 6;
        
        //computing the constants for time-steps
        double sfnew = SNEW * tsHours;
        double rfmin = RMIN * tsHours;
        double sbci  = SBC * tsHours;
        //compute ground melt for time-steps
        double gm = this.groundMelt_1.getValue() / ndt;
        
        nosnow = 0;
        packMelt = 0;
        //the six hour loop starts here
        for(int t = 0; t < ndt; t++){
            snowEvap = 0;
            packMelt = 0;
            double rainInTs = 0;
            rainMelt = 0;
            cnhs = 0;
            cnhspx = 0;
            snowFall = 0;
            groundMeltRunoff = 0;
            rainOnBareGround = 0;
            nosnow = 1;
            
            //flag for substituting GOTOs from the FORTRAN implementation
            boolean stillSnow = true;
            
            //redistribute daily input to 6 hour values
            double precip6hr = precip / ndt;
            double tAir = t6hr[t]; //this is the critical point when used in hourly time steps!!
            double tRain = tAir;
            
            //if no precip so skip directly to ground-melt computations
            
            if(precip6hr > 0){
                
                //snow fall because tAir below threshold
                if(tAir <= pxtemp_1.getValue()){
                    //snowFall
                    snowTemp = tRain;
                    if(snowTemp > 0)
                        snowTemp = 0;
                    snowFall = precip6hr;
                    //PK: added to get the balance right on days with internal temperature around threshold
                    if(rainIn > 0)
                        rainIn = rainIn - snowFall;
                    if(rainIn < 0)
                        rainIn = 0;
                    
                    dsfall = dsfall + snowFall;
                    //add 75% of new snow to water equiv for spatial unit
                    //PK: why??
                    sbws = swe + liqWater + 0.75 * precip6hr;
                    //add snowfall to SWE
                    swe = swe + snowFall;
                    
                    //if swe + liqWater >= 3 * sb, assume a new accumulation period
                    if((swe + liqWater) >= (3. * sb))
                        maxAccumulation = swe + liqWater;
                    
                    //cumulate negative heat when snowTemp < 0
                    cnhspx = -1 * snowTemp * snowFall / 160.;
                    
                    //if new snow is larger than SFNEW, set temperature index snow to air temperature
                    if(precip6hr > sfnew){
                        tindex = airTemp;
                    }
                    
                    //all done, so:
                    precip6hr = 0; //continue at 120
                }
                //100//rainFall because tAir above threshold
                else{
                    //snow pack exists, and will be affected by rain fall
                    if(swe > 0){
                        if(tRain < 0)
                            tRain = 0;
                        rainMelt = 0.0125 * precip6hr * tRain;
                        drain = drain + precip6hr;
                        rainInTs = precip6hr;
                        
                    }
                    //no snow, so all precip goes to snow-free
                    else{
                        rainOut = rainOut + precip6hr;
                        drain = drain + precip6hr;
                        call300();
                        stillSnow = false;
                    }
                }
            }
            //120
            //Groundmelt
            if(stillSnow){
                if(swe <= gm){
                    groundMeltRunoff = swe + liqWater;
                    rainInTs = 0; 
                    rainOnBareGround = precip6hr;
                    //snow is gone so call 300
                    call300();
                    stillSnow = false;
                } else{
                    //water loss from pack liquid water due to groundmelt
                    groundWaterLoss = (gm / swe) * liqWater;
                    groundMeltRunoff = gm + groundWaterLoss;
                }
            }
            if(stillSnow){
                //snowpack energy exchange computations, includes rest of Andersons melt19
                //subroutine. Compute melt and negative heat exchange index temperatures, then Neg Heat Exchange
                //double airTemp, double ratio, double rain, double meltFactor, int subInt, double airPressure
                packMelt = callMeltrate(tAir, ratio, rainInTs, meltFactor, t, airPressure);
                
                /*
                 *areal extent of snow cover, Anderson's subroutine "AESC19", slightly modified
                 *accounts for new snow if any
                 */
                arealSnowCover = callAeSnow();
                
                //adjust a couple of values if arealSnowCover is smaller 1.0
                if(arealSnowCover < 1.0){
                    gm = gm * arealSnowCover;
                    groundMeltRunoff = groundMeltRunoff * arealSnowCover;
                    groundWaterLoss = groundWaterLoss * arealSnowCover;
                    packMelt = packMelt * arealSnowCover;
                    cnhs = cnhs * arealSnowCover;
                    snowEvap = snowEvap * arealSnowCover;
                    
                    rainOnBareGround = rainIn * (1 - arealSnowCover);
                    rainIn = rainIn - rainOnBareGround;
                } else{
                    rainOnBareGround = 0;
                }
                
                //adjust for surface and groundmelt
                swe = swe - gm; //decrease swe by groundmelt
                liqWater = liqWater - groundWaterLoss; //decrease liq. water by ground melt
                //check chns if it brings negSnowHeat to negative
                if((cnhs + negSnowHeat) < 0){
                    //then decrease by absolute difference, i.e. neg negSnowHeat
                    cnhs = -1. * negSnowHeat;
                }
                //surface melt computations, at this point packMelt is always >= 0, so cleaned code
                if(packMelt >= swe){
                    packMelt = swe + liqWater;
                    dqnet = dqnet + packMelt;
                    //all snow gone
                    call300();
                    stillSnow = false;
                } else{
                    swe = swe - packMelt;
                }
            }
            if(stillSnow){
                //adjust for sublimation, if paramter sublate = 0, then same results as orig. snow17
                if(snowEvap >= swe){
                    snowEvap = swe + liqWater;
                    dqnet = dqnet + snowEvap;
                    //all snow gone
                    call300();
                    stillSnow = false;
                } else{
                    swe = swe - snowEvap;
                }
            }
            if(stillSnow){
                //netEnergyExchange: net surface energy exchange for ts when all snow did not melt
                netSurfaceEnergyExchange = packMelt - cnhs - cnhspx + snowEvap;
                dqnet = dqnet + netSurfaceEnergyExchange;
                
                //heat and water balance of the snow pack
                double excessWater = callHeatWater(rainInTs);
                rainInTs = 0;
                //Route of excess water, and add groundMeltRunoff to packRunoff
                packRunoff = packRunoff + callRoute2(tsHours, excessWater);
                packRunoff = packRunoff + groundMeltRunoff;// + packMelt;
            }
            //save some vars
            pmSave = pmSave + packMelt;
            rainOut = rainOut + rainInTs;
            
        }//end of six-hour loop
        if(nosnow > 0){
            lagged = 0;
            for(int i = 0; i < exlag.length; i++){
                lagged = lagged + exlag[i];
            }
            totalPackWater = swe + liqWater + liqWaterAttStorage + lagged;
            arealSnowCover = callAeSnow();
        }
        else{
            totalPackWater = 0;
            for(int i = 0; i < exlag.length; i++){
                exlag[i] = 0;
            }
        }
    }
    
    //routine called when the snow pack has gone during the time step
    public void call300(){
        double lagged = 0;
        for(int i = 0; i < exlag.length; i++){
            lagged = lagged + exlag[i];
            exlag[i] = 0;
        }
        packRunoff = packRunoff + groundMeltRunoff + packMelt + lagged + liqWaterAttStorage;// + rainOut;
        swe = 0;
        negSnowHeat = 0;
        liqWater = 0;
        sb = 0;
        sbaesc = 0;
        sbws = 0;
        maxAccumulation = 0;
        arealSnowCover = 0;
        tindex = 0;
        liqWaterAttStorage = 0;
        //packMelt = 0;
        nosnow = 0;
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
        rainIn = rainIn - rainInTs;
        double heat = cnhs + cnhspx;
        double liqWMax = maxWaterCap_1.getValue() * swe;
        negSnowHeat = negSnowHeat + heat;
        
        if(negSnowHeat < 0)
            negSnowHeat = 0;
        if(negSnowHeat > (0.33 * swe))
            negSnowHeat = 0.33 * swe;
        
        //is water exceeding the maximum storage capacity?
        if((water + liqWater) < (liqWMax + negSnowHeat + maxWaterCap_1.getValue() * negSnowHeat)){
            //230
            if((water + liqWater) < negSnowHeat){
                //231 water is refrozen and negSnowHeat decreased
                swe = swe + water + liqWater;
                negSnowHeat = negSnowHeat - water - liqWater;
                refrozenWater = refrozenWater + water + liqWater;
                liqWater = 0;
                water = 0;
                excessWater = 0;
            }
            else{
                liqWater = liqWater + water - negSnowHeat;
                swe = swe + negSnowHeat;
                refrozenWater = refrozenWater + negSnowHeat;
                negSnowHeat = 0;
                excessWater = 0;
            }
        }
        else{
            excessWater = water + liqWater - liqWMax - negSnowHeat - maxWaterCap_1.getValue() * negSnowHeat;
            liqWater = liqWMax + maxWaterCap_1.getValue() * negSnowHeat;
            
            //PK: adaptation to get the balance right
            //packMelt = packMelt - liqWater;
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
            for(int i = 0; i < hoursInTimeStep; i++){
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
    
    private double callRoute2(int hoursInTimeStep, double excessWater){
        //initial values
        int it = hoursInTimeStep;
        //int it = int(hoursInTimeStep); //this and next if statement makes "it" integer, dt is simulation timestep
        //if (it == 0) 
        //   it =1;
        //end % not in original code, so omitted
        double packro = 0;
        double cl = 0.03 * it / 6; //this is divided over 6 hours - ?? need explanation of what it is??
        //lag excess water first = function of excess and we
        if (excessWater == 0){
            //true go to 150
        }
        else if (excessWater < .1){
            //true go to 120
            //excess is small - therefore no lag
            exlag[0] = exlag[0] + excessWater;
            //goes to 150
        }
        else if (swe < 1){
            //go to 120
            // we small - therefore no lag - small snowpack
            exlag[0] = exlag[0] + excessWater;
            //goes to 150
        }
        else{
            //we > 1 or excess > .1
            //compute lag in hours and prorate excess  
            //eqns based on empirical eqns
            //lag cannot be longer than 6 hours, see documentation for model
            //drains as a porportion of excess liquid water
            int n = (int)Math.floor(Math.pow(excessWater*4, 0.3)+0.5);
        
            if (n == 0){ //when would n = 0?
                n = 1;
            }
            int fn = n;
            int dt = it;
            for(int i = 0; i < n; i++){// i=1:n  % n has to be an integer
                int fi = i+1;
                double term = cl * swe * fn / (excessWater * (fi-0.5)); 
                if(term > 150){  // when would term be 150?  when we = 1500!?
                    term = 150;
                }

                double flagg = 5.33 * ( 1 - Math.exp(-term));
                int l2 = (int)Math.floor(((flagg+dt)/dt)+1);
                int l1 = l2-1;
                int endl1=l1*it;
                double por2=(flagg+dt-endl1)/dt;
                double por1=1-por2;
                //java fortan array idx conversion
                l2 = l2 - 1;
                l1 = l1 - 1;
                exlag[l2] = exlag[l2] + por2 * excessWater/fn;
                exlag[l1] = exlag[l1] + por1 * excessWater/fn;
            }
            //%goes to 150 
        }
        //150 attenuate stored excess water which is function of storge and we
        if ((liqWaterAttStorage + exlag[0]) == 0){
            //go to 190
        }
        else if ((liqWaterAttStorage+exlag[0]) >= 0.1){
            int dt = it;
            //go to 160
            //effect of attenuation computed using a 1-hour time step
            double el = exlag[0] / dt;
            double els=el/(25.4*this.arealSnowCover);
            double wes = swe/(25.4*this.arealSnowCover);
            double term = 500*els/(Math.pow(wes,1.3));
            if (term > 150){
                term = 150;
            }
            double r1 = 1/(5*Math.exp(-term)+1); //withdrawl rate
            for(int i = 0; i < hoursInTimeStep;i++){
                double os = ( liqWaterAttStorage + el)*r1;
                packro = packro+os;
                liqWaterAttStorage=liqWaterAttStorage+el-os;
            }
            if (liqWaterAttStorage <= .001){
                //true 
                packro = packro+liqWaterAttStorage;
                liqWaterAttStorage = 0;
                //false goes to 190
            }
        }
        else{
            //no attenuation
            packro=liqWaterAttStorage+exlag[0];
            liqWaterAttStorage = 0;
            //goto 190
        }
        //190 downshift water in exlag()
        for(int i = 1; i < exlag.length; i++){// i = 2:nexlag
            exlag[i-1] = exlag[i];
        }
        exlag[exlag.length - 1] = 0;
        
        return packro;
    }
}
