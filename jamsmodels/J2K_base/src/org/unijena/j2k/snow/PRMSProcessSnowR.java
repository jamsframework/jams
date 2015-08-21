/*
 * PRMSProcessSnow.java
 * Created on 17. August 2006, 17:33
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, Peter Krause
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

package org.unijena.j2k.snow;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
title="PRMSProcessSnow",
        author="Peter Krause, and the guys form the USGS",
        description="Adaptation of the PRMS module for snow calculation"
        )
        public class PRMSProcessSnowR extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "time"
            )
            public Attribute.Calendar time;
    
//Entity attributes
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Entity area"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Entity cover density in winter"
            )
            public Attribute.Double covDensWin;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Entity cover density in summer"
            )
            public Attribute.Double covDensSum;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "cover type of entity 0 = bareSoil, 1 = grass, 2 = shrubs, 3 = trees"
            )
            public Attribute.Double covType;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "type of entity 1 = land, 2 = water"
            )
            public Attribute.Double entityType;
    
//state variables recheck read/write access
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "variable snowCrv [PRMS: Scrv]"
            )
            public Attribute.Double snowCurve;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "variable snowCovAreaSave [PRMS: Snowcov_areasv]"
            )
            public Attribute.Double snowCovAreaSave;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "variable albedoSave [PRMS: Salb]"
            )
            public Attribute.Double packAlbedoSave;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "variable lstSave [PRMS: Slst]"
            )
            public Attribute.Double lstSave;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "variable intAlbedo [PRMS: Int_alb]"
            )
            public Attribute.Integer intAlbedo;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "snow surface albedo [PRMS: Albedo]"
            )
            public Attribute.Double packAlbedo;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "precipitation added to snowpack [PRMS: Pk_precip]"
            )
            public Attribute.Double packPrecip;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "temperature of the snowpack [PRMS: Pk_temp]"
            )
            public Attribute.Double packTemp;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "density of the snowpack [PRMS: Pk_den]"
            )
            public Attribute.Double packDensity;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "net snowpack energy balance [PRMS: Tcal]"
            )
            public Attribute.Double packEnergyBal;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "evaporation and sublimation from snowpack [PRMS: Snow_evap]"
            )
            public Attribute.Double snowET;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "snowmelt from snowpack [PRMS: Snowmelt]"
            )
            public Attribute.Double snowMelt;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "snowpack water equivalent [PRMS: Pkwater_equiv]"
            )
            public Attribute.Double snowWaterEquivalent;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "snow-covered area (decimal percent) [PRMS: Snowcov_area]"
            )
            public Attribute.Double snowCovArea;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "indicator that a rain-snow mix event has occured with" +
            "no snowpack present [PRMS: Pptmix_nopack]"
            )
            public Attribute.Boolean pptMixNoSnow;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "var iasw/flag [PRMS: Iasw]"
            )
            public Attribute.Boolean iasw;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "var iso/flag [PRMS: Iso]"
            )
            public Attribute.Integer iso;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "var/flag mso [PRMS: Mso]"
            )
            public Attribute.Integer mso;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "var/flag lso [PRMS: Lso]"
            )
            public Attribute.Integer lso;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "var/flag lst [PRMS: Lst]"
            )
            public Attribute.Boolean lst;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "heat deficit in the lower snowpack [PRMS: Pk_def]"
            )
            public Attribute.Double packDef;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "frozen part of the pack's swe [PRMS: Pk_ice]"
            )
            public Attribute.Double packIce;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "liquid part of the pack's swe [PRMS: freeh2o]"
            )
            public Attribute.Double packFreeWater;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "depth of the snowpack [PRMS: Pk_depth]"
            )
            public Attribute.Double packDepth;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "var pss [PRMS: Pss]"
            )
            public Attribute.Double pss;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "var pst [PRMS: Pst]"
            )
            public Attribute.Double pst;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "captures the old swe of the time step before [PRMS: Pksv]"
            )
            public Attribute.Double packSWEtm1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "captures the old snow of the time step before [PRMS: Snsv]"
            )
            public Attribute.Double snowSave;
    
    
    
//calibration parameters
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "density of new snow [PRMS: Den_init]"
            )
            public Attribute.Double initDens;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Snowpack settlement time constant [PRMS: Settle_const]"
            )
            public Attribute.Double settleConst;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Average maximum snowpack density [PRMS: Den_max]"
            )
            public Attribute.Double maxDens;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Julian date to start looking for spring snowmelt, " +
            "Julian date to start looking for spring snowmelt stage. " +
            "Varies with region depending on length of time that permanent " +
            "snowpack exists [PRMS: Melt_look]"
            )
            public Attribute.Integer meltLook;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Julian date to force snowpack to spring snowmelt stage." +
            "Varies with region depending on length of time that" +
            "permanent snowpack exists' [PRMS: Melt_force]"
            )
            public Attribute.Integer meltForce;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Transmission coefficient for short-wave radiation through" +
            "the winter vegetation canopy in decimal percent [PRMS: Rad_trncf]"
            )
            public Attribute.Double radTransCoef;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Maximum threshold water equivalent for snow depletion." +
            "The maximum threshold snowpack water equivalent below" +
            "which the snow-covered-area curve is applied. " +
            "Varies with elevation (not here!). [PRMS: Snarea_thresh]"
            )
            public Attribute.Double snowAreaThresh;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Albedo reset - rain,  melt stage," +
            "Proportion of rain (decimal percent) in a rain-snow precipitation event" +
            "above which the snow albedo is not reset. Applied during" +
            "the snowpack melt stage [PRMS: Albset_rnm]"
            )
            public Attribute.Double albedoResetRainMelt;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Albedo reset - rain, accumulation stage," +
            "Proportion of rain (decimal percent) in a rain-snow precipitation event" +
            "above which the snow albedo is not reset. Applied during" +
            "the snowpack accumulation stage [PRMS: Albset_rna]"
            )
            public Attribute.Double albedoResetRainAccu;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Albedo reset - snow, melt stage," +
            "Minimum snowfall, in water equivalent, needed to reset" +
            "snow albedo during the snowpack melt stage" +
            "[PRMS: Albset_snm]"
            )
            public Attribute.Double albedoResetSnowMelt;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Albedo reset - snow, accumulation stage" +
            "Minimum snowfall, in water equivalent, needed to reset" +
            "snow albedo during the snowpack accumulation stage" +
            "[PRMS: Albset_sna]"
            )
            public Attribute.Double albedoResetSnowAccu;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Emissivity of air on days without precipitation" +
            "Average emissivity of air on days without precipitation" +
            "[PRMS: Emis_noppt]"
            )
            public Attribute.Double emisNoPrecip;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Proportion (decimal percent) of potential ET that is sublimated from the" +
            "snow surface [PRMS: Potet_sublim]"
            )
            public Attribute.Double petSublimProp;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Free-water holding capacity (decimal percent) of " +
            "snowpack expressed as decimal fraction of total " +
            "snowpack water equivalent [PRMS: Freeh2o_cap]"
            )
            public Attribute.Double packFreeWaterCapacity;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Precip all snow if hru max temperature below this value." +
            "If HRU maximum temperature is less than or equal to this" +
            "value, precipitation is assumed to be snow [PRMS: Tmax_allsnow]"
            )
            public Attribute.Double tmaxAllSnow;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Multiplier for the cecn values [PRMS: n/a]"
            )
            public Attribute.Double cecnFactor;
    
//driving variables
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "minum temperature"
            )
            public Attribute.Double tmin;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "mean temperature"
            )
            public Attribute.Double tmean;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "max temperature"
            )
            public Attribute.Double tmax;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "observed or calculated solar radiation"
            )
            public Attribute.Double solRad;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "observed or calculated shortwave radiation"
            )
            public Attribute.Double swRad;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "total precipitation (rain and snow)"
            )
            public Attribute.Double precip;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "rain amount of precipitation, will be changed during snow modelling"
            )
            public Attribute.Double inRain;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "snow amount of precipitation, will be changed during snow modelling"
            )
            public Attribute.Double inSnow;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "save rain for output"
            )
            public Attribute.Double svRain;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "save snow for output"
            )
            public Attribute.Double svSnow;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual evapotranspiration so far"
            )
            public Attribute.Double aET;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "potential evapotranspiration"
            )
            public Attribute.Double pET;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "module active"
            )
            public Attribute.Boolean active;
    
    //internal variables (has to be discussed with George, Steve and Stevo!!)
    
    //Snow area depletion curve values, 11 values for each curve
    //(0 to 100 percent in 10 percent increments)
    double[] snowAreaCurve = {.05,.24,.47,.43,.56,.75,.82,.88,.93,.99,1.0}; //{0.000,0.010,0.075,0.120,0.200,0.280,0.340,0.450,0.550,0.700,1.000},
    
    //{.05,.25,.47,.48,.54,.58,.61,.64,.66,.68,.70};
    
    
    //Convection condensation energy coefficient, varied monthly,
    //calories per degree C above 0
    double[] cecnCoef = {8.51,8.51,8.95,8.95,8.95,8.95,8.95,8.95,8.95,8.95,8.51,8.51};
    
    //Set to 1 if thunderstorms prevalent during month.
    //Monthly indicator for prevalent storm type:
    //0 = frontal storms prevalent, 1 = convective storms prevalent
    int[] tStorm = {0,0,0,0,1,1,1,1,1,1,0,0};
    
    double[] aCum = {.80,.77,.75,.72,.70,.69,.68,.67,.66,.65,.64,.63,.62,.61,.60};
    double[] aMlt = {.72,.65,.60,.58,.56,.54,.52,.50,.48,.46,.44,.43,.42,.41,.40};
    int MAXALB = aMlt.length - 1;
    
    double runSwRad = 0;
    
    double denInv;
    double setDen;
    double set1;
    boolean pptMix = false;
    boolean newSnow = false;
    double rainPart = 0;
    
    //run variables
    double runPackSwe, runPackPrecip, runRain, runSnow, runPackDef, runPackTemp, runPackIce, runPackFreeWater, runSnowCovArea;
    double runSnowMelt, runPackDensity, runPackDepth, runSnowCovAreaSave, runPst, runPackSWEtm1, runSnowCurve, runLstSave;
    double runPackAlbedoSave, runPss, runTmin, runTmean, runTmax, runPackEnergyBal, runAet, runPet, runSolRad, runPrecip, runTavgC, runSnowSave;
    double runPackAlbedo, runSnowET;
    boolean runLst, runTranspOn;
    //int runLstSave;
    
    //  conversion factor from MJ/m² to langley
    final double MJ2LY = 28.88458966;
    
    //conversion from mm to inch
    final double MM2IN = 0.039370078;
    final double IN2MM = 25.4;
    final double IN2CM = 2.54;
    
    final double NEARZERO = 0.00001;
    
    double c_2_f(double tempC){
        return (((9.0/5.0) * tempC) + 32.0);
    }
    
    public double f_2_c(double tempF){
        return (( tempF - 32.0 ) * (5.0 / 9.0));
    }
    /*
     *  Component run stages
     */
    public void init() {
        if(this.active == null || this.active.getValue()){
            this.aET.setValue(0);
            this.runSnowET = 0;
            this.snowCurve.setValue(0);
            this.snowCovAreaSave.setValue(0);
            this.packAlbedoSave.setValue(0);
            this.lstSave.setValue(0);
            this.packTemp.setValue(0);
            this.packDensity.setValue(0);
            this.packEnergyBal.setValue(0);
            this.snowMelt.setValue(0);
            this.snowWaterEquivalent.setValue(0);
            this.snowCovArea.setValue(0);
            this.lst.setValue(false);
            this.packDef.setValue(0);
            this.packIce.setValue(0);
            this.packFreeWater.setValue(0);
            this.packDepth.setValue(0);
            this.pss.setValue(0);
            this.pst.setValue(0);
            this.packSWEtm1.setValue(0);
            this.packPrecip.setValue(0);
            this.snowSave.setValue(0);
        }
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        if(this.active == null || this.active.getValue()){
            this.run_prms();
        }
    }
    
    
    
    //mapping changed variables back
    private void mapVarsBack(double balIn, double balStorStart){
        
        //if(runPackSwe > 0 && runSnowCovArea == 0)
        //	System.out.println("stop swe > 0 and sca = 0: " + this.time.toString());
        this.runAet = this.runAet + this.runSnowET;
        this.snowET.setValue(this.runSnowET);
        this.aET.setValue(this.runAet * this.area.getValue());
        this.snowCurve.setValue(this.runSnowCurve);
        this.snowCovAreaSave.setValue(this.runSnowCovAreaSave);
        this.packAlbedoSave.setValue(this.runPackAlbedoSave);
        this.lstSave.setValue(this.runLstSave);
        this.packTemp.setValue(this.runPackTemp);
        this.packDensity.setValue(this.runPackDensity);
        this.packEnergyBal.setValue(this.runPackEnergyBal);
        this.snowMelt.setValue(this.runSnowMelt * this.area.getValue());
        this.snowWaterEquivalent.setValue(this.runPackSwe * this.area.getValue());
        this.snowCovArea.setValue(this.runSnowCovArea);
        this.lst.setValue(this.runLst);
        this.packDef.setValue(this.runPackDef);
        this.packIce.setValue(this.runPackIce);
        this.packFreeWater.setValue(this.runPackFreeWater);
        this.packDepth.setValue(this.runPackDepth);
        this.pss.setValue(this.runPss);
        this.pst.setValue(this.runPst);
        this.packSWEtm1.setValue(this.runPackSWEtm1 * this.area.getValue());
        this.packPrecip.setValue(this.runPackPrecip * this.area.getValue());
        this.snowSave.setValue(this.runSnowSave * this.area.getValue());
        this.packAlbedo.setValue(this.runPackAlbedo);
        
        //check this!!
        
        //all snow is cared for in snow module!
        this.inSnow.setValue(0);
        
        double balStorEnd = this.runPackSwe;
        double balOut = this.runSnowMelt + this.runRain + this.runSnowET;
        double balance = balIn  + (balStorStart - balStorEnd) - balOut;
        if(Math.abs(balance) > 0.0001){
            getModel().getRuntime().println(this.time.toString() + " balance error in snow module: "+balance);
            getModel().getRuntime().println("balIn: " + balIn);
            getModel().getRuntime().println("balStorStart: " + balStorStart);
            getModel().getRuntime().println("balStorEnd: " + balStorEnd);
            getModel().getRuntime().println("balOut: " + balOut);
            getModel().getRuntime().println("shit!");
        }
        this.snowWaterEquivalent.setValue(this.runPackSwe * this.area.getValue() * IN2MM);
        this.snowMelt.setValue(this.runSnowMelt * this.area.getValue() * IN2MM);
        this.aET.setValue(this.runAet * this.area.getValue() * IN2MM);
        this.inRain.setValue(this.runRain * this.IN2MM * this.area.getValue());
    }
    public void ppt_to_pack(){
//		 USE PRMS_SNOW, ONLY: NEARZERO, FIVE_NINTHS, INCH2CM, Tmax_allsnow
//		 implicit none
//		 intrinsic ABS
        //usgs Arguments
//		 integer, INTENT(IN) :: Pptmix
//		 integer, INTENT(OUT) :: Iasw, Pptmix_nopack
//		 real, INTENT(IN) :: Tmaxf, Tminf, Tavgc, Net_rain, Net_snow
//		 real, INTENT(INOUT) :: Pkwater_equiv, Snowmelt, Freeh2o
//		 real, INTENT(INOUT) :: Pk_def, Pk_ice, Pk_den, Snowcov_area
//		 real, INTENT(OUT) :: Pk_temp, Pk_depth, Pss, Pst, Pk_precip
        //usgs Local Variables
//		 real :: train, tsnow, caln, pndz, calpr, calps, f_to_c, tmp
        double train, tsnow, caln, pndz, calpr, calps, tmp;
        //usgs Statement Functions
        //f_to_c(tmp) = (tmp-32.0)*FIVE_NINTHS;
        //usgs***********************************************************************
        runPackPrecip = 0.0;
        
        //usgs******Compute temperature of rain and snow
        double tmas = c_2_f(tmaxAllSnow.getValue());
        
        if ( pptMix){// == 1 ) {
            train = f_2_c((runTmax + tmas)*.5);
            
            if ( runPackSwe > 0. ) {
                tsnow = f_2_c((runTmin + tmas)*.5);
            } else
                tsnow = runTavgC;
        } else{
            train = runTavgC;
            
            if ( train < NEARZERO )
                train = f_2_c((runTmax + tmas)*.5);
            
            tsnow = runTavgC;
        }
        if ( train < NEARZERO )
            train = 0.;
        if ( tsnow > -NEARZERO )
            tsnow = 0.;
        
        //usgs******If snowpack already exists, add rain first, then add
        //usgs******snow.  If no antecedent snowpack, rain is already taken care
        //usgs******of, so start snowpack with snow.  This subroutine assumes
        //usgs******that in a mixed event, the rain will be first and turn to
        //usgs******snow as the temperature drops.
        
        if ( runPackSwe > 0. ) {
            if ( runRain > 0. ) {
                runPackSwe = runPackSwe + runRain;
                runPackPrecip = runPackPrecip + runRain;
                if ( runPackDef > 0. ) {
                    caln = (80.+train)*this.IN2CM;
                    pndz = runPackDef/caln;
                    
                    //usgs******Exactly enough rain to bring pack to isothermal
                    
                    if ( Math.abs(runRain-pndz) < NEARZERO ) {
                        runPackDef = 0.;
                        runPackTemp = 0.;
                        runPackIce = runPackIce + runRain;
                        runRain = 0;
                        
                        //usgs******Rain not sufficient to bring pack to isothermal
                    } else if ( runRain < pndz ) {
                        runPackDef = runPackDef - (caln*runRain);
                        runPackTemp = -runPackDef/(runPackSwe*1.27);
                        runPackIce = runPackIce + runRain;
                        runRain = 0;
                    }
                    //usgs******Rain in excess of amount required to bring pack to isothermal
                    else{
                        runPackDef = 0.;
                        runPackTemp = 0.;
                        runPackIce = runPackIce + pndz;
                        runPackFreeWater = runRain - pndz;
                        calpr = train*(runRain-pndz)*IN2CM;
                        calin(calpr);/*, runPackSwe, runPackDef, runPackTemp,
                                                                runPackIce, runPackFreeWater, Snowcov_area, Snowmelt,
                                                                Pk_depth, Pss, Pst, Iasw, Pk_den);*/
                    }
                    
                }
                //usgs******Snowpack isothermal when rain occurred
                else{
                    runPackFreeWater = runPackFreeWater + runRain;
                    calpr = train*runRain*IN2CM;
                    calin(calpr);/*, runPackSwe, runPackDef, runPackTemp,
                                                        runPackIce, runPackFreeWater, Snowcov_area, Snowmelt,
                                                        Pk_depth, Pss, Pst, Iasw, Pk_den);*/
                }
            }
//pk: added for J2k to get the balance right. I assume rain has been treated at this point.
            runRain = 0;
        }
        //usgs******Set switch if rain/snow event with no antecedent snowpack
        else if ( runRain > 0. ) {
            pptMixNoSnow.setValue(true);//1;
        }
        
        if ( runSnow > 0. ) {
            //if(runPackSwe == 0)
            //	 this.runSnowCovArea = 1.0;
            runPackSwe = runPackSwe + runSnow;
            runPackPrecip = runPackPrecip + runSnow;
            runPackIce = runPackIce + runSnow;
            if ( tsnow >= 0. ) {
                runPackTemp = -runPackDef/(runPackSwe*1.27);
            } else{
                calps = tsnow*runSnow*1.27;
                if ( runPackFreeWater > 0. ) {
                    caloss(calps);//, runPackSwe, runPackDef, runPackTemp, runPackIce, Freeh2o);
                } else{
                    runPackDef = runPackDef - calps;
                    runPackTemp = -runPackDef/(runPackSwe*1.27);
                }
            }
        }
    }
    
    
    private void calin(double Cal){
        //USE PRMS_SNOW, ONLY: NEARZERO, Freeh2o_cap
        //  implicit none
        //usgs Arguments
        //integer, INTENT(OUT) :: Iasw
        //real, INTENT(IN) :: Cal
        //real, INTENT(INOUT) :: Pkwater_equiv, Freeh2o, Snowcov_area
        //real, INTENT(INOUT) :: Pk_def, Pk_temp, Pk_ice, Pk_den, Snowmelt
        //real, INTENT(OUT) :: Pk_depth, Pss, Pst
        //usgs Local Variables
        //real :: dif, pmlt, apmlt, apk_ice, pwcap
        double dif, pmlt, apmlt, apk_ice, pwcap;
        //usgs***********************************************************************
        dif = Cal - runPackDef;
        //usgs******calorie deficit exists (Cal < runPackDef)
        if ( dif < -NEARZERO ) {
            runPackDef = runPackDef - Cal;
            runPackTemp = -runPackDef/(runPackSwe*1.27);
            
            //usgs******no calorie deficit or excess (Cal = runPackDef)
        }else if ( dif < NEARZERO ) {
            runPackTemp = 0.;
            runPackDef = 0.;
            
            //usgs******calorie excess, melt snow (Cal > runPackDef)
        }else{
            pmlt = dif/203.2;
            apmlt = pmlt*runSnowCovArea;
            runPackDef = 0.;
            runPackTemp = 0.;
            apk_ice = runPackIce/runSnowCovArea;
            
            //usgs******calories sufficient to melt entire pack
            if ( pmlt > apk_ice ) {
                runSnowMelt = runSnowMelt + runPackSwe;
                runPackSwe = 0.;
                iasw.setValue(false);// = 0;
                runSnowCovArea = 0.;
                runPackDef = 0.;
                runPackTemp = 0.;
                runPackIce = 0.;
                runPackFreeWater = 0.;
                runPackDepth = 0.;
                runPss = 0.;
                runPst = 0.;
                runPackDensity = 0.;
                runPackEnergyBal = 0;
                
                //usgs******calories sufficient to melt part of pack
            }else{
                runPackIce = runPackIce - apmlt;
                runPackFreeWater = runPackFreeWater + apmlt;
                pwcap = packFreeWaterCapacity.getValue()*runPackIce;
                dif = runPackFreeWater - pwcap;
                if ( dif > 0. ) {
                    runSnowMelt = runSnowMelt + dif;
                    runPackFreeWater = pwcap;
                    runPackSwe = runPackSwe - dif;
                    runPackDepth = runPackSwe/runPackDensity;
                    runPss = runPackSwe;
                }
            }
        }
        
        //end subroutine calin
    }
    
    
    
    private void caloss(double Cal){
        //USE PRMS_SNOW, ONLY: NEARZERO
        //  implicit none
        //usgs Arguments
        //real, INTENT(IN) :: Cal, Pkwater_equiv
        //real, INTENT(INOUT) :: Pk_def, Pk_ice, Freeh2o
        //real, INTENT(OUT) :: Pk_temp
        //usgs Local Variables
        //real :: calnd, dif
        double calnd, dif;
        //usgs***********************************************************************
        
        //usgs******if no free water exists in pack
        if ( runPackFreeWater < NEARZERO ) {
            runPackDef = runPackDef - Cal;
            
            //usgs******free water does exist in pack
        }else{
            calnd = runPackFreeWater*203.2;
            dif = Cal + calnd;
            
            //usgs******all free water freezes
            if ( dif < NEARZERO ) {
                if ( dif < 0. ) runPackDef = -dif;
                runPackIce = runPackIce + runPackFreeWater;
                runPackFreeWater = 0.;
                
                //usgs******only part of free water freezes
            }else{
                runPackIce = runPackIce + (-Cal/203.2);
                runPackFreeWater = runPackFreeWater - (-Cal/203.2);
                return;
            }
        }
        
        if ( runPackSwe > 0. )
            runPackTemp = -runPackDef/(runPackSwe*1.27);
        
        //end subroutine caloss
    }
    
    
    
    
    public void snowcov(int K){
        //USE PRMS_SNOW, ONLY: Snarea_curve
        //  implicit none
        //  intrinsic FLOAT, INT
        //usgs Arguments
        //  integer, INTENT(IN) :: K, Newsnow
        //  integer, INTENT(INOUT) :: Iasw
        //  real, INTENT(IN) :: Pkwater_equiv
        //  real, INTENT(IN) :: Snarea_thresh, Net_snow
        //  real, INTENT(OUT) :: Snowcov_area
        //  real, INTENT(INOUT) :: Pst, Scrv, Pksv, Snowcov_areasv
        //usgs Local Variables
        //integer :: jdx, idx
        int jdx, idx;
        //real :: ai, pcty, difx, frac, af, dify
        double ai, pcty, difx, frac, af, dify;
        //usgs***********************************************************************
        runSnowCovArea = snowAreaCurve[10];//Snarea_curve[11][K];
        
        if ( runPackSwe > runPst )
            runPst = runPackSwe;
        ai = runPst;
        if ( ai >= snowAreaThresh.getValue() )
            ai = snowAreaThresh.getValue();
        if ( runPackSwe >= ai ) {
            iasw.setValue(false);// = 0;
        } else{
            if ( !newSnow){//Newsnow == 0 ) {
                if ( iasw.getValue()){//Iasw != 0 ) {
                    if ( runPackSwe > runSnowCurve )
                        return;
                    
                    //usgs******New snow not melted back to original partial area
                    if ( runPackSwe >= runPackSWEtm1 ) {
                        difx = runSnowCovArea - runSnowCovAreaSave;
                        dify = runSnowCurve - runPackSWEtm1;                           //usgsgl1098
                        pcty = 0.0;                                   //usgsgl1098
                        if ( dify > 0.0000001 )                      //usgsrsr0106
                            pcty = (runPackSwe - runPackSWEtm1) / dify;     //usgsgl1098
                        runSnowCovArea = runSnowCovAreaSave + (pcty * difx);
                        return;
                        
                        //usgs******Pack water equivalent back to value before new snow
                    } else{
                        iasw.setValue(false);// = 0;
                    }
                }
                
                //usgs******New snow
            } else{
                if ( iasw.getValue()){//Iasw > 0 ) {
                    runSnowCurve = runPackSwe - (.25 * runSnow);
                } else{
                    iasw.setValue(true);//Iasw = 1;
                    this.runSnowCovAreaSave = this.runSnowCovArea;
                    runPackSWEtm1 = runPackSwe - runSnow;
                    runSnowCurve = runPackSwe - (.25 * runSnow);
                }
                return;
            }
            
            //usgs******Interpolate along snow area depletion curve
            
            frac = runPackSwe / ai;
            idx = (int)(10. * (frac + .2));
            jdx = idx - 1;
            af = (double)(jdx - 1);
            dify = (frac * 10.) - af;
            difx = snowAreaCurve[idx-1] - snowAreaCurve[jdx-1];
            runSnowCovArea = snowAreaCurve[jdx-1] + (dify * difx);
            
        }
        
        //end subroutine snowcov
    }
    
    
    
    private void snalbedo(){
        //USE PRMS_SNOW, ONLY: NEARZERO, Acum, Amlt
        //  implicit none
        //  intrinsic INT
        //  include 'fmodules.inc'
        //usgs Arguments
        //integer, INTENT(IN) :: Newsnow, Iso, Mso, Pptmix
        //integer, INTENT(INOUT) :: Int_alb, Lst
        //real, INTENT(IN) :: Albset_rnm, Albset_snm, Albset_rna, Albset_sna
        //real, INTENT(IN) :: Prmx, Net_snow
        //real, INTENT(INOUT) :: Salb, Slst, Snsv
        //real, INTENT(OUT) :: Albedo
        //usgs Local Variables
        //integer :: l
        int l;
        //usgs***********************************************************************
        if ( !newSnow){// == 0 ) {
            //usgs******If no new snow, check to adjust albedo for shallow snowpack
            if ( runLst){// > 0 ) {
                runLstSave = runPackAlbedoSave - 3.;
                if ( runLstSave-1.0 < NEARZERO )
                    runLstSave = 1.;
                if ( iso.getValue() != 2 ) {
                    if ( runLstSave-5.0 > -NEARZERO )
                        runLstSave = 5.;
                }
                runLst = false;//0;
                runSnowSave = 0.;
            }
            
            //usgs******New snow in melt stage
        } else if ( mso.getValue() == 2 ) {
            if ( rainPart < albedoResetRainMelt.getValue() ) {
                if ( runSnow > albedoResetSnowMelt.getValue() ) {
                    runLstSave = 0.;
                    runLst = false;//0;
                    runSnowSave = 0.;
                } else{
                    runSnowSave = runSnowSave + runSnow;
                    if ( runSnowSave > albedoResetSnowMelt.getValue() ) {
                        runLstSave = 0.;
                        runLst = false;//0;
                        runSnowSave = 0.;
                    } else{
                        if ( !runLst){// == 0 )
                            runPackAlbedoSave = runLstSave;
                        }
                        runLstSave = 0.;
                        runLst = true;//1;
                    }
                }
            }
            
            //usgs******New snow in accumulation stage
        } else{
            if ( !pptMix){// <= 0 ) {
                runLstSave = 0.;
                runLst = false;//0;
            } else if ( rainPart-albedoResetRainAccu.getValue() > -NEARZERO ) {
                runLst = false;//0;
            } else if ( runSnow-albedoResetSnowAccu.getValue() > -NEARZERO ) {
                runLstSave = 0.;
                runLst = false;//0;
            } else{
                runLstSave = runLstSave - 3.;
                if ( runLstSave < NEARZERO )
                    runLstSave = 0.;
                if ( runLstSave-5. > -NEARZERO )
                    runLstSave = 5.;
                runLst = false;//0;
            }
            runSnowSave = 0.;
        }
        
        //usgs******Set counter for days since last snowfall
        l = (int)(runLstSave + .5);
        runLstSave = runLstSave + 1.;
        
        //usgs******compute albedo
        if ( l > 0 ) {
            //usgs******Old snow - Winter accumulation period
            if ( intAlbedo.getValue() != 2 ) {
                if ( l <= MAXALB ) {
                    runPackAlbedo = aCum[0];// = Acum[l];
                }else{
                    l = l - 12;
                    if ( l > MAXALB )
                        l = MAXALB ;
                    runPackAlbedo = aMlt[0];// = Amlt[l];
                }
                //usgs******Old snow - Spring melt period
            }else{
                if ( l > MAXALB )
                    l = MAXALB;
                runPackAlbedo = aMlt[0];//= Amlt[l];
            }
            //usgs******New snow condition
        } else if ( mso.getValue() == 2 ) {
            runPackAlbedo = .81;
            intAlbedo.setValue(2);
        } else{
            runPackAlbedo = .91;
            intAlbedo.setValue(1);
        }
        
        //end subroutine snalbedo
    }
    
    
    
    private double snowbal(double Esv, double Temp, double Niteda, double Tstorm_mo, double Trd, double Cec, double Cst, double Sw){
        //USE PRMS_SNOW, ONLY: NEARZERO, Basin_ppt
        //  implicit none
        //  external calin, caloss
        //usgs Arguments
        //integer, INTENT(IN) :: Niteda, Tstorm_mo
        //integer, INTENT(INOUT) :: Iasw
        //real, INTENT(IN) :: Temp, Esv, Trd, Cec, Cst, Covden_win
        //real, INTENT(IN) :: Emis_noppt
        //real, INTENT(OUT) :: Pst, Pss
        //real, INTENT(INOUT) :: Pk_den, Pk_def, Pk_temp, Pk_ice, Pk_depth
        //real, INTENT(INOUT) :: Pkwater_equiv, Freeh2o
        //real, INTENT(INOUT) :: Snowcov_area, Snowmelt
        //usgs Local Variables
        //real :: air, ts, emis, sno, sky, can
        double air, ts, emis, sno, sky, can;
        //real :: cecsub, Cal, qcond, pk_defsub, pkt, pks, Sw
        double cecsub, Cal, qcond, pk_defsub, pkt, pks;
        //usgs***********************************************************************
        air = .585E-7 * ( Math.pow((Temp + 273.16),4.));
        emis = Esv;
        
        if ( Temp < 0. ) {
            ts = Temp;
            sno = air;
        }else{
            ts = 0.;
            sno = 325.7;
        }
        //if ( Basin_ppt > 0. ) {
        if ( runPrecip > 0. ) {
            if ( Tstorm_mo == 1 ) {
                if ( Niteda == 1 ) {
                    emis = .85;
                    if ( Trd > .33 )
                        emis = emisNoPrecip.getValue();
                }else{
                    if ( Trd > .33 )
                        emis = 1.29 - (.882 * Trd);
                    if ( Trd >= .5 )
                        emis = .95 - (.2 * Trd);
                }
            }
        }
        
        sky = (1. - covDensWin.getValue()) * ((emis * air) - sno);
        can = covDensWin.getValue() * (air - sno);
        cecsub = 0.;
        if ( Temp > 0. ) {
            if ( runPrecip > 0. )
                cecsub = Cec * Temp;
        }
        Cal = sky + can + cecsub + Sw;
        
        if ( ts >= 0. ) {
            if ( Cal > 0. ) {
                calin(Cal);/*, Pkwater_equiv, Pk_def, Pk_temp,
                                                Pk_ice, Freeh2o, Snowcov_area, Snowmelt,
                                                Pk_depth, Pss, Pst, Iasw, Pk_den);*/
                return Cal;
            }
        }
        
        qcond = Cst * (ts - runPackTemp);
        if ( qcond < -NEARZERO ) {
            if ( runPackTemp < 0. ) {
                runPackDef = runPackDef - qcond;
                runPackTemp = -runPackDef / (runPackSwe * 1.27);
            } else{
                caloss(qcond);//, runPackSwe, runPackDef, runPackTemp,Pk_ice, Freeh2o);
            }
            
            //usgs qcond = 0.0
        } else if( qcond < NEARZERO ) {
            if ( runPackTemp >=0. ) {
                if ( Cal > 0. )
                    calin(Cal);/*, runPackSwe, runPackDef, runPackTemp,
                                                        Pk_ice, Freeh2o, Snowcov_area,
                                                        Snowmelt, Pk_depth, Pss, Pst, Iasw,
                                                        Pk_den);*/
            }
            
        } else if ( ts >=0. ) {
            pk_defsub = runPackDef - qcond;
            if ( pk_defsub < 0. ) {
                runPackDef = 0.;
                runPackTemp = 0.;
            } else{
                runPackDef = pk_defsub;
                runPackTemp = -pk_defsub / (runPackSwe * 1.27);
            }
        } else{
            pkt = -ts * runPackSwe * 1.27;
            pks = runPackDef - pkt;
            pk_defsub = pks - qcond;
            if ( pk_defsub < 0. ) {
                runPackDef = pkt;
                runPackTemp = ts;
            } else{
                runPackDef = pk_defsub + pkt;
                runPackTemp = -runPackDef / (runPackSwe * 1.27);
            }
        }
        return Cal;
    }
    
    
    private void snowevap(){
        //USE PRMS_SNOW, ONLY: NEARZERO
        //  implicit none
        //usgs Arguments
        //integer, INTENT(IN) :: Cov_type, Transp_on
        //real, INTENT(IN) :: Covden_win, Covden_sum, Potet_sublim, Potet
        //real, INTENT(IN) :: Snowcov_area, Intcp_evap
        //real, INTENT(INOUT) :: Pkwater_equiv, Pk_ice, Pk_def, Pk_temp
        //real, INTENT(OUT) :: Snow_evap, Freeh2o
        //usgs Local Variables
        //real :: cov, ez, cal
        double cov, ez, cal;
        //usgs***********************************************************************
        if ( this.covType.getValue() > 1 ) {
            cov = this.covDensWin.getValue();
            if ( this.runTranspOn)//Transp_on == 1 )
                cov = this.covDensSum.getValue();
            ez = (petSublimProp.getValue() * runPet * runSnowCovArea) - (runAet * cov);
        }else{
            ez = petSublimProp.getValue() * runPet * runSnowCovArea;
        }
        
        if ( ez < NEARZERO ) {
            runSnowET = 0.;
            
            //usgs******Entirely depletes snowpack
        } else if ( ez >= runPackSwe ) {
            runSnowET = runPackSwe;
            runPackSwe = 0.;
            runPackIce = 0.;
            runPackDef = 0.;
            runPackFreeWater = 0.;
            runPackTemp = 0.;
            runPackEnergyBal = 0;
            //usgs******Partially depletes snowpack
        } else{
            runPackIce = runPackIce - ez;
            cal = runPackTemp * ez * 1.27;
            runPackDef = runPackDef + cal;
            runPackSwe = runPackSwe - ez;
            runSnowET = ez;
        }
    }
    
    
    
    public void cleanup() {
        
    }
    
    public void run_prms(){
                /*System.out.println(time.toString());
                int y = time.get(time.YEAR);
                int m = time.get(time.MONTH);
                int d = time.get(time.DATE);
                if(y == 1996 && m == 10 && d == 9)
                System.out.println("stop");*/
        
        int Ndepl = 0;
        this.denInv = 1. / this.initDens.getValue();
        this.setDen = this.settleConst.getValue() / this.maxDens.getValue();
        this.set1 = 1. / (1. +  this.settleConst.getValue());
        
//		pk: mapping the runVariables and converting to inches
        this.runPackSwe    = (this.snowWaterEquivalent.getValue() * MM2IN / this.area.getValue());
        this.runPackSWEtm1 = (this.packSWEtm1.getValue()  / this.area.getValue());// * MM2IN;
        this.runPackPrecip = 0;
        this.runSnowSave = (this.snowSave.getValue() / this.area.getValue());
        this.runPackDef = this.packDef.getValue();
        this.runPackTemp = this.packTemp.getValue();
        this.runPackIce = this.packIce.getValue();
        this.runPackFreeWater = this.packFreeWater.getValue();
        this.runSnowCovArea = this.snowCovArea.getValue();
        this.runSnowCovAreaSave = this.snowCovAreaSave.getValue();
        this.runSnowMelt = 0;
        this.snowMelt.setValue(0);
        this.runPackDensity = this.packDensity.getValue();
        this.runPackDepth = this.packDepth.getValue();
        this.runPst = this.pst.getValue();
        this.runSnowCurve = this.snowCurve.getValue();
        this.runPackAlbedo = this.packAlbedo.getValue();
        this.runPackAlbedoSave = this.packAlbedoSave.getValue();
        this.runLst = this.lst.getValue();
        this.runLstSave = this.lstSave.getValue();
        this.runPss = this.pss.getValue();
        
        this.runPackEnergyBal = this.packEnergyBal.getValue();
        
        
        //Basin_snowmelt = 0.;
        //Basin_pweqv = 0.;
        //Basin_snowevap = 0.;
        //Basin_snowcov = 0.;
        //Basin_pk_precip = 0.;
        //bsnobal = 0.;
        
//		 timestep = deltim();
        
//		 if ( timestep < TSTEPCHK ){
//		 snorun = 0;
//		 return;
//		 }
        
//		 dattim(now, nowtime);
//		 mo = nowtime(2);
//		 jday = julian(now, calendar);
//		 jwday = julian(now, water);
        
//		 if ( Print_debug == 9 ){
//			 nstep = getstep();
//			 dpint4(snowcomp, nstep, nstep, 1, 2)
//			 dpint4(date, nowtime, 6, 1)
//		 }
        
//		 Orad is computed if no observed value is available
        
        
        
        //* input vars setters
        
        //if ( getvar(Orad) != 0 ) return;
        this.runSolRad = this.solRad.getValue() * this.MJ2LY;
        
        //if ( getvar(Basin_potsw) != 0 ) return;
        //if ( getvar(Swrad) != 0 ) return;
        this.runSwRad = this.swRad.getValue()  * this.MJ2LY;
        
        //if ( getvar(Net_ppt) != 0 ) return;
        this.runPrecip = this.precip.getValue() * this.MM2IN;
        
        //if ( getvar(Net_snow) != 0 ) return;
        this.runSnow = this.inSnow.getValue() / this.area.getValue() * MM2IN;
        
        //if ( getvar(Net_rain) != 0 ) return;
        this.runRain = this.inRain.getValue() / this.area.getValue() * MM2IN;
        
        //if ( getvar(Intcp_evap) != 0 ) return;
        this.runAet = this.aET.getValue() / this.area.getValue() * MM2IN;
        
        //if ( getvar(Pptmix) != 0 ) return;
        if(this.runRain > 0 && this.runSnow > 0){
            this.pptMix = true;
        } else{
            this.pptMix = false;
        }
        
        //if ( getvar(Newsnow) != 0 ) return;
        if(this.runSnow > 0)
            this.newSnow = true;
        else
            this.newSnow = false;
        
//PK: Why do we need a basin value??
        //if ( getvar(Basin_ppt) != 0 ) return;
        this.runPrecip = this.runPrecip;
        
        //if ( getvar(Prmx) != 0 ) return;
        this.rainPart = (this.runRain / this.runPrecip);
        
        //if ( getvar(Tmaxf) != 0 ) return;
        this.runTmax = c_2_f(this.tmax.getValue());
        
        //if ( getvar(Tminf) != 0 ) return;
        this.runTmin = c_2_f(this.tmin.getValue());
        
        //if ( getvar(Tavgc) != 0 ) return;
        this.runTavgC = this.tmean.getValue();
        
        //if ( getvar(Transp_on) != 0 ) return;
        
//PK other than PRMS, have to think about that later
        int julDay = this.time.get(Attribute.Calendar.DAY_OF_YEAR);
        int month = this.time.get(Attribute.Calendar.MONTH);
        if(julDay > 85 && julDay < 305)
            this.runTranspOn = true;
        else
            this.runTranspOn = false;
        
        
        //if ( getvar(Potet) != 0 ) return;
        this.runPet = this.pET.getValue() / this.area.getValue() * MM2IN;
        
//srg	     if ( get var('intcp', 'int_snow', Nhru, 'integer', Int_snow)
//srg	    +      != 0 ) return
        
                 /*if ( jwday == 1 ){
                         Pss = 0.0;
                         Iso = 1;
                         Mso = 1;
                         Lso = 0;
                 }*/
        
        double balIn = this.runSnow + this.runRain;
        double balStorStart = this.runPackSwe;
        
        int year = time.get(Attribute.Calendar.YEAR);
        
        
//		 reset some values if first day of hydrological year
        if(julDay == 305){
            this.runPss = 0;
            iso.setValue(1);
            mso.setValue(1);
            lso.setValue(0);
        }
        
        double trd = runSolRad / runSwRad;
        
//		 do i = 1, Nhru
        runPackPrecip = 0.;
        runPackSWEtm1 = runPackSwe;
        this.pptMixNoSnow.setValue(false);// = 0;
        runSnowMelt = 0.;
        runSnowET = 0.;
//		 do nothing
        if ( this.entityType.getValue() == 2 )
            return;
        if(julDay == this.meltForce.getValue())
            this.iso.setValue(2);
        if(julDay == this.meltLook.getValue())
            this.mso.setValue(2);
        
//		 rsr    if ( runPackSWEtm1.le.NEARZERO .and. Newsnow == 0
//		 rsr +        &&  Int_snow == 0 ) cycle
        
        if ( runPackSWEtm1 < NEARZERO  &&  !newSnow)
            return;
        if ( newSnow  &&  runPackSWEtm1 < NEARZERO )
            runSnowCovArea = 1.0;
        
//		 ******Add rain and/or snow to snowpack
        if ( (runPackSWEtm1 > 0. && runRain > 0.) || runSnow > 0. )
            ppt_to_pack();//Pptmix, Iasw, Tmaxf, Tminf,
        //Tavgc, Pkwater_equiv, Net_rain,
        //Pk_def, Pk_temp, Pk_ice, Freeh2o,
        //Snowcov_area, Snowmelt, Pk_depth,
        //Pss, Pst, Net_snow, Pk_den,
        //Pptmix_nopack, runPackPrecip);
        
//		 call dpreal('pk_temp-ptp', Pk_temp, 1, 1)
//		 call dpreal('tcal-ptp', Tcal, 1, 1)
        
        if ( runPackSwe > 0. ){
            
//			 ******Compute snow-covered area
            //if ( Ndepl > 0 ){
//pk				 int k = Hru_deplcrv;
            snowcov(0);// Iasw, Newsnow, Snowcov_area,
            // Pkwater_equiv, Pst, Snarea_thresh,
            // Net_snow, Scrv, Pksv,
            // Snowcov_areasv);
            //}
            
//			 call dpreal('pk_temp-scov', Pk_temp, 1, 1)
//			 call dpreal('tcal-scov', Tcal, 1, 1)
            
//			 ******Compute albedo
            
            snalbedo();//Newsnow, Iso, Mso, Lst, Snsv,
            //Prmx, Pptmix, Albset_rnm, Net_snow,
            //Albset_snm, Albset_rna, Albset_sna, Albedo,
            //Int_alb, Salb, Slst);
            
//			 ******
//			 call dpreal('pk_temp-alb', Pk_temp, 1, 1)
//			 call dpreal('tcal-alb', Tcal, 1, 1)
            double tminc = f_2_c(runTmin);
            double tmaxc = f_2_c(runTmax);
            double emis = this.emisNoPrecip.getValue();
            if ( runPrecip > 0. ) //Basin_ppt
                emis = 1.;
            double esv = emis;
            double swn = runSwRad*(1.-runPackAlbedo)*this.radTransCoef.getValue();
            double cec = this.cecnCoef[month] * this.cecnFactor.getValue() *.5;
            if ( this.covType.getValue() == 3 )
                cec = cec*.5;
            
//			 ******Compute density and pst
            runPss = runPss + runSnow;
            double dpt1 = ((runSnow*this.denInv) + (this.setDen*runPss) + runPackDepth) * this.set1;
            runPackDepth = dpt1;
            runPackDensity = runPackSwe/dpt1;
            double effk = .0154*runPackDensity;
            double cst = runPackDensity*(Math.sqrt(effk*13751.));
            
//			 ***** Check whether to force spring melt
            if ( this.iso.getValue() == 1 ){
                if ( this.mso.getValue() == 2 ){
                    if ( runPackTemp >= 0. ){
                        lso.setValue(lso.getValue() + 1);
                        if ( lso.getValue() > 4 ){
                            this.iso.setValue(2);
                            lso.setValue(0);
                        }
                    } else{
                        lso.setValue(0);
                    }
                }
            }
            
//			 ******Compute energy balance for night period
            int niteda = 1;
            double sw = 0.;
            double temp = (tminc+runTavgC)*.5;
            double cals = snowbal(esv, temp, niteda, this.tStorm[month], trd, cec, cst, sw);
                         /*
                snowbal(niteda, Tstorm_mo[mo], Iasw, temp, esv,
                            trd, Emis_noppt, Covden_win, cec,
                            Pkwater_equiv, Pk_def, Pk_temp,
                            Pk_ice, Freeh2o, Snowcov_area,
                            Snowmelt, Pk_depth, Pss, Pst,
                            Pk_den, cst, cals, sw);*/
            runPackEnergyBal = cals;
//			 call dpreal('pk_temp-nite', Pk_temp, 1, 1)
//			 call dpreal('tcal-nite', Tcal, 1, 1)
            
//			 ******Compute energy balance for day period
            if ( runPackSwe > 0. ){
                niteda = 2;
                sw = swn;
                temp = (tmaxc+runTavgC)*.5;
                cals = snowbal(esv, temp, niteda, this.tStorm[month], trd, cec, cst, sw);
                                 /*
                  snowbal(niteda, Tstorm_mo[mo], Iasw, temp, esv,
                              trd, Emis_noppt, Covden_win, cec,
                              Pkwater_equiv, Pk_def, Pk_temp,
                              Pk_ice, Freeh2o, Snowcov_area,
                              Snowmelt, Pk_depth, Pss, Pst,
                              Pk_den, cst, cals, sw);*/
                runPackEnergyBal = runPackEnergyBal + cals;
//				 call dpreal('pk_temp-day', Pk_temp, 1, 1)
//				 call dpreal('tcal-day', Tcal, 1, 1)
            }
            
//			 ******Compute snow evaporation
            if ( runPackSwe > 0. ){
                if ( !runTranspOn  || (runTranspOn  &&  this.covType.getValue() <= 1)){
                    snowevap();/*Cov_type, Transp_on, Covden_win,
                                   Covden_sum, Potet_sublim, Potet,
                                   Snowcov_area, Intcp_evap,
                                   Snow_evap, Pkwater_equiv, Pk_ice,
                                   Pk_def, Freeh2o, Pk_temp);*/
                }
            } else{
                runSnowET = 0.;
            }
            
//			 ******
            if ( runPackSwe > 0. ){
                runPackDepth = runPackSwe/runPackDensity;
                runPss = runPackSwe;
                if ( lst.getValue() ){
                    runSnowSave = runSnowSave - runSnowMelt;
                    if ( runSnowSave < NEARZERO )
                        runSnowSave = 0.;
                }
            } else{
                runPackDepth = 0.;
                runPss = 0.;
                runSnowSave = 0.;
                this.lst.setValue(false);
                runPst = 0.;
                iasw.setValue(false);
                runPackAlbedo = 0.;
                runPackDensity = 0.;
                runSnowCovArea = 0.;
                runPackDef = 0.;
                runPackTemp = 0.;
                runPackIce = 0.;
                runPackFreeWater = 0.;
                runPackEnergyBal = 0;
            }
        }
        //Basin_snowmelt = Basin_snowmelt + runSnowMelt*Hru_area;
        //Basin_pweqv = Basin_pweqv + runPackSwe*Hru_area;
        //Basin_snowevap = Basin_snowevap + runSnowET*Hru_area;
        //Basin_snowcov = Basin_snowcov + runSnowCovArea*Hru_area;
        //Basin_pk_precip = Basin_pk_precip + Pk_precip*Hru_area;
        
                 /*if ( Print_debug == 1 ){
                         double hrubal = runPackSWEtm1 - runPackSwe - runSnowET - runSnowMelt;
                         if ( pptMixNowSnow == 1 ){
                                 hrubal = hrubal + runSnow;
                         }
                         else{
                                 hrubal = hrubal + runRain;
                         }
                         if ( Math.abs(hrubal) > 5.0E-6){
                                 System.out.println("Snow rounding issue");
                                 //+ i + hrubal + nowtime(1) + nowtime(2), nowtime(3), runPackSWEtm1,
                                 //                   Pkwater_equiv, Snow_evap, Snowmelt,
                                 //                   Net_ppt, Net_snow, Net_rain,
                                 //                   Newsnow, Pptmix, Pptmix_nopack)
                  
                                 if ( Math.abs(hrubal) > 1.0E-5)
                                         System.out.println("Possible water balance error");
                         }
                         //bsnobal = bsnobal + hrubal;
                 }*/
        
        
        //Basin_snowmelt = Basin_snowmelt/Basin_area;
        //Basin_pweqv = Basin_pweqv/Basin_area;
        //Basin_snowevap = Basin_snowevap/Basin_area;
        //Basin_snowcov = Basin_snowcov/Basin_area;
        //Basin_pk_precip = Basin_pk_precip/Basin_area;
        
                 /*if ( Print_debug == 1 ){
                         System.out.println("some vars");
                         //nowtime(1), jday, bsnobal, Basin_pweqv,
                         //                 Basin_snowmelt, Basin_snowevap, Basin_snowcov
                         if ( Math.abs(bsnobal) > 1.0E-5){
                                 System.out.println("Snow rounding issue");//', bsnobal, nowtime
                                 if ( Math.abs(bsnobal) > 1.0E-4)
                                         System.out.println("Possible water balance error");
                         }
                 }
                 if ( Print_debug == 9 ){
                         //     print 9000, jday, (Net_rain, i=1,Nhru)
                         //     print 9000, jday, (Net_snow, i=1,Nhru)
                         //     print 9000, jday, (Snowmelt, i=1,Nhru)
                         //9000   format(i5, 177f6.3)
                 }*/
        this.mapVarsBack(balIn, balStorStart);
    }
    
}
