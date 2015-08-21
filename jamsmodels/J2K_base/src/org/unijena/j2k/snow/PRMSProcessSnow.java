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
        public class PRMSProcessSnow extends JAMSComponent {
    
    /*
     *  Component variables
     */
	@JAMSVarDescription(
	        access = JAMSVarDescription.AccessType.READ,
	        update = JAMSVarDescription.UpdateType.RUN,
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
            description = "precipitation added to snowpack [PRMS: Pk_precip]",
            unit = "mm"
            )
            public Attribute.Double packPrecip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "temperature of the snowpack [PRMS: Pk_temp]",
            unit = "°C"
            )
            public Attribute.Double packTemp;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "density of the snowpack [PRMS: Pk_den]",
            unit = "g/cm^3"
            )
            public Attribute.Double packDensity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "net snowpack energy balance [PRMS: Tcal]"
            )
            public Attribute.Double packEnergyBal;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "evaporation and sublimation from snowpack [PRMS: Snow_evap]",
            unit = "mm"	
            )
            public Attribute.Double snowET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "snowmelt from snowpack [PRMS: Snowmelt]",
            unit = "mm"	
            )
            public Attribute.Double snowMelt;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "snowpack water equivalent [PRMS: Pkwater_equiv]",
            unit = "mm"	
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
            description = "depth of the snowpack [PRMS: Pk_depth]",
            unit = "mm"
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
            description = "captures the old swe of the time step before [PRMS: Snsv]"	
            )
            public Attribute.Double packSWEtm1;
    
   
    
//calibration parameters   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "density of new snow [PRMS: Den_init]",
            unit = "g/cm^3"	
            )
            public Attribute.Double initDens;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Snowpack settlement time constant [PRMS: Settle_const]"
            )
            public Attribute.Double settleConst;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Average maximum snowpack density [PRMS: Den_max]",
            unit = "g/cm^3"	
            )
            public Attribute.Double maxDens;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian date to start looking for spring snowmelt, " +
            		      "Julian date to start looking for spring snowmelt stage. " +
            		      "Varies with region depending on length of time that permanent " +
            		      "snowpack exists [PRMS: Melt_look]",
            unit = "julDay"	
            )
            public Attribute.Integer meltLook;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian date to force snowpack to spring snowmelt stage." +
            		      "Varies with region depending on length of time that" +
            		      "permanent snowpack exists' [PRMS: Melt_force]",
            unit = "julDay"	
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
            		      "[PRMS: Albset_snm]",
            unit = "mm"
            )
            public Attribute.Double albedoResetSnowMelt;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Albedo reset - snow, accumulation stage" +
            		      "Minimum snowfall, in water equivalent, needed to reset" +
            		      "snow albedo during the snowpack accumulation stage" +
            		      "[PRMS: Albset_sna]",
            unit = "mm"
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
            		      "value, precipitation is assumed to be snow [PRMS: Tmax_allsnow]",
            unit = "°C"
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
            description = "minum temperature",
            unit = "°C"
            )
            public Attribute.Double tmin;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "mean temperature",
            unit = "°C"
            )
            public Attribute.Double tmean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "max temperature",
            unit = "°C"
            )
            public Attribute.Double tmax;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "observed or calculated solar radiation",
            unit = "MJ"
            )
            public Attribute.Double solRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "observed or calculated shortwave radiation",
            unit = "MJ"
            )
            public Attribute.Double swRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "total precipitation (rain and snow)",
            unit = "mm"
            )
            public Attribute.Double precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "rain amount of precipitation, will be changed during snow modelling",
            unit = "mm"
            )
            public Attribute.Double inRain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "snow amount of precipitation, will be changed during snow modelling",
            unit = "mm"
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
            description = "actual evapotranspiration so far",
            unit = "mm"
            )
            public Attribute.Double aET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "potential evapotranspiration",
            unit = "mm"
            )
            public Attribute.Double pET;
    
    
    //internal variables (has to be discussed with George, Steve and Stevo!!)
    
    //Snow area depletion curve values, 11 values for each curve 
    //(0 to 100 percent in 10 percent increments)
    double[] snowAreaCurve = {0.000,0.010,0.075,0.120,0.200,0.280,0.340,0.450,0.550,0.700,1.000};
    
    //Convection condensation energy coefficient, varied monthly, 
    //calories per degree C above 0
    double[] cecnCoef = {8.51,8.51,8.95,8.95,8.95,8.95,8.95,8.95,8.95,8.95,8.51,8.51};
    
    //Set to 1 if thunderstorms prevalent during month. 
    //Monthly indicator for prevalent storm type: 
    //0 = frontal storms prevalent, 1 = convective storms prevalent
    int[] tStorm = {1,1,1,1,1,1,1,1,1,1,1,1};
    
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
    double runPackAlbedoSave, runPss, runTmin, runTmean, runTmax, runPackEnergyBal, runAet, runPet, runSolRad;
    boolean runLst, runTranspOn;
    //int runLstSave;
	
    
    /*
     *  Component run stages
     */
    public void init() {
    	this.aET.setValue(0);
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
    	
    }
    

	public void run() throws Attribute.Entity.NoSuchAttributeException{
		this.denInv = 1. / this.initDens.getValue();
		this.setDen = this.settleConst.getValue() / this.maxDens.getValue();
		this.set1 = 1. / (1. +  this.settleConst.getValue());
        //not sure yet, have to ask USGS
    	this.runSwRad = this.swRad.getValue() * 238800; //conversion from MJ to cal
    	this.runSolRad = this.solRad.getValue() * 238800; //conversion from MJ to cal
    	//flag indicating if a mixed rain/snow event has occured
        pptMix = ((this.inRain.getValue() > 0) && (this.inSnow.getValue() > 0));
        //flag indicating that new snow has fallen
        newSnow = (this.inSnow.getValue() > 0);
        //the rain part of a precipitation event
        if(this.precip.getValue() > 0)
			rainPart = this.inRain.getValue() / this.precip.getValue();
        else
        	rainPart = 0;
        
        //mapping the runVariables
        this.runPackSwe = this.snowWaterEquivalent.getValue() / this.area.getValue();
        this.runPackPrecip = 0;
        //convert the volume to mm/m²
        this.runRain = this.inRain.getValue() / this.area.getValue();
        this.runSnow = this.inSnow.getValue() / this.area.getValue();
        this.runAet = this.aET.getValue() / this.area.getValue();
        this.runPet = this.pET.getValue() / this.area.getValue();
        this.runPackSWEtm1 = this.packSWEtm1.getValue()  / this.area.getValue();
        
        this.runPackDef = this.packDef.getValue();
        this.runPackTemp = this.packTemp.getValue();
        this.runPackIce = this.packIce.getValue();
        this.runPackFreeWater = this.packFreeWater.getValue();
        this.runSnowCovArea = this.snowCovArea.getValue();
        this.runSnowCovAreaSave = this.snowCovAreaSave.getValue();
        this.runSnowMelt = 0;
        this.runPackDensity = this.packDensity.getValue();
        this.runPackDepth = this.packDepth.getValue();
        this.runPst = this.pst.getValue();
        this.runSnowCurve = this.snowCurve.getValue();
        this.runPackAlbedoSave = this.packAlbedoSave.getValue();
        this.runLst = this.lst.getValue();
        this.runLstSave = this.lstSave.getValue();
        this.runPss = this.pss.getValue();
        this.runTmin = this.tmin.getValue();
        this.runTmean = this.tmean.getValue();
        this.runTmax = this.tmax.getValue();
        this.runPackEnergyBal = this.packEnergyBal.getValue();
        
        double balIn = this.inSnow.getValue() + this.inRain.getValue();
        double balStorStart = this.snowWaterEquivalent.getValue();
        
        int julDay = this.time.get(Attribute.Calendar.DAY_OF_YEAR);
        int month = this.time.get(Attribute.Calendar.MONTH);
        
        //other than PRMS, have to think about that later
        if(julDay > 85 && julDay < 305)
			this.runTranspOn = true;
        else
        	this.runTranspOn = false;
        
        // reset some values if first day of hydrological year
		if(julDay == 305){
			this.runPss = 0;
			iso.setValue(1);
			mso.setValue(1);
			lso.setValue(0);
		}
		
		double trd = this.runSolRad / this.runSwRad;
		
		double prev_swe = this.runPackSwe;
		this.pptMixNoSnow.setValue(false);
		this.snowET.setValue(0);
		
		if(julDay == this.meltForce.getValue())
			this.iso.setValue(2);
		if(julDay == this.meltLook.getValue())
			this.mso.setValue(2);
		
		// no snow at all, so I am leaving!
		if(prev_swe == 0 && !newSnow){ // && Int_snow == 0){
			//System.out.println(julDay + "\t" + Pkwater_equiv + "\t" + Pk_depth);
			this.mapVarsBack(balIn, balStorStart);
			return;
		}
        
		//new snow on bare ground
		if(newSnow && prev_swe == 0)
			this.runSnowCovArea = 1.0;
		
		//Add rain and/or snow to snowpack (PRMS function name: ppt_to_pack()
		if((prev_swe > 0 && this.precip.getValue() > 0) || this.inSnow.getValue() > 0){
			snowAccumulation();
		}
		
		if(this.runPackSwe > 0){
			//Compute snow-covered area (PRMS function name snowcov()
			/*PRMS implementation, I am not sure about those depletion curveSSS!!
			if(Ndepl > 0){
				int k = Hru_deplcrv;
				snowcov();
			}*/
			calcSnowCoveredArea();
			
			//Compute albedo
			calcSnowAlbedo();
			
			double emis = this.emisNoPrecip.getValue();
			/* original PRMS implementation replaced 
			if(Basin_ppt > 0){
				emis = 1.0;
			}*/
			if(this.precip.getValue() > 0)
				emis = 1.0;
			
			double esv = emis;
			double swn = this.runSwRad * (1 - this.packAlbedo.getValue()) * this.radTransCoef.getValue();
			double cec = (cecnCoef[month] * this.cecnFactor.getValue()) * 0.5;
			
			//reduced in forests. Has to be redesigned for J2k
			if(this.covType.getValue() == 3){
				cec = cec * 0.5;
			}
			//Compute density and pst
			this.runPss = this.runPss + this.runSnow;
			double dpt1 = ((this.runSnow * this.denInv) + (this.setDen * this.runPss) + this.runPackDepth) * this.set1;
			this.runPackDepth = dpt1;
			this.runPackDensity = this.runPackSwe / dpt1;
			double effk = 0.0154 * this.runPackDensity;
			double cst = this.runPackDensity * Math.sqrt(effk * 13751.0 * 2);
//USGS: what are 0.0154 and 13751??
			
			//Check whether to force spring melt
			if(this.iso.getValue() == 1){
				if(this.mso.getValue() == 2){
					if(this.runPackTemp >= 0){
						this.lso.setValue(this.lso.getValue() + 1);
						if(this.lso.getValue() > 4){
							this.iso.setValue(2);
							this.lso.setValue(0);
						}
					}
					else{
						this.lso.setValue(0);
					}
				}
			}
			
			//Compute energy balance for night period
			double niteda = 1;
			double sw = 0;
			double temp = (this.runTmin + this.runTmean) / 2.;
			double calories = calcSnowBalance(niteda, temp, esv, trd, cec, cst, sw, month);
			this.runPackEnergyBal = calories;
			
			//Compute energy balance for day period
			if(this.runPackSwe > 0){
				niteda = 2;
				sw = swn;
				temp = (this.runTmax + this.runTmean) / 2;
				calories = calcSnowBalance(niteda, temp, esv, trd, cec, cst, sw, month);
				this.runPackEnergyBal = this.runPackEnergyBal + calories;
			}
			
			//Compute snow evaporation
			if(this.runPackSwe > 0){
				if(!this.runTranspOn || (this.runTranspOn && this.covType.getValue() <= 1)){
					calcSnowEvaporation();
				}
			}
			else{
				this.snowET.setValue(0);
			}
			
			if(this.runPackSwe > 0){
				this.runPackDepth = this.runPackSwe / this.runPackDensity;
				this.runPss = this.runPackSwe;
				if(this.runLst){
					this.runPackSWEtm1 = this.runPackSWEtm1 - this.runSnowMelt;
					if(this.runPackSWEtm1 < 0){
						this.runPackSWEtm1 = 0;
					}
				}
			}
			else{
				this.runPackDepth = 0;
				this.runPss = 0;
				this.runPackSWEtm1 = 0;
				this.runLst = false;
				this.runPst = 0;
				this.iasw.setValue(false);
				this.packAlbedo.setValue(0);
				this.runPackDensity = 0;
				this.runSnowCovArea = 0;
				this.runPackDef = 0;
				this.runPackIce = 0;
				this.runPackFreeWater = 0;
				this.runPackTemp = 0;
				this.runPackEnergyBal = 0;
			}
		}
		this.mapVarsBack(balIn, balStorStart);
    }
	
	//mapping changed variables back
	private void mapVarsBack(double balIn, double balStorStart){
		this.runAet = this.runAet + this.snowET.getValue();
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
		
		
		
		//check this!!
		this.inRain.setValue(this.runRain * this.area.getValue());
		//all snow is cared for in snow module!
		this.inSnow.setValue(0);
		
		double balStorEnd = this.snowWaterEquivalent.getValue();
        double balOut = this.snowMelt.getValue() + this.inRain.getValue() + this.inSnow.getValue() + this.aET.getValue();
        double balance = balIn  + (balStorStart - balStorEnd) - balOut;
        if(Math.abs(balance) > 0.0001){
            getModel().getRuntime().println(this.time.toString() + " balance error in snow module: "+balance);
            getModel().getRuntime().println("balIn: " + balIn);
            getModel().getRuntime().println("balStorStart: " + balStorStart);
            getModel().getRuntime().println("balStorEnd: " + balStorEnd);
            getModel().getRuntime().println("balOut: " + balOut);
            getModel().getRuntime().println("shit!");
        }
	}
    
    //PRMS: ppt_to_pack()
    private void snowAccumulation(){
		this.runPackPrecip = 0;

		//Compute temperature of rain and snow
		double train, tsnow;
		if(this.pptMix){
			train = (this.runTmax + this.tmaxAllSnow.getValue()) / 2.;

			if(this.runPackSwe > 0){
				tsnow = (this.runTmin + this.tmaxAllSnow.getValue()) / 2.;
			}else{
				tsnow = this.runTmean;
			}
		}else{
			train = this.runTmean;
			if(train < 0){
				train = (this.runTmax + this.tmaxAllSnow.getValue()) / 2.;
			}
			tsnow = this.runTmean;
		}

		if(train < 0)
			train = 0;
		if(tsnow > 0)
			tsnow = 0;
		/* If snowpack already exists, add rain first, then add
		 * snow. If no antecedent snowpack, rain is already taken care
		 * of, so start snowpack with snow. This routine assumes 
		 * that in a mixed event, the rain will be first and turn to
		 * snow as the temperature drops.
		 */
		if(this.runPackSwe > 0){
			if(this.runRain > 0){
				this.runPackSwe = this.runPackSwe + this.runRain;
				this.runPackPrecip = this.runPackPrecip + this.runRain;
				
				//pack is not isothermal
				if(this.runPackDef > 0){
					double caln = (1000 * train); // * 10;// * 2.54; [heatcapacity of water = 1000 cal/kg°C --> calories per mm brought in by 1 mm of rain with temp train]
					double pndz = this.runPackDef / caln;

					//Exactly enought rain to bring pack to isothermal
					if(Math.abs(this.runRain - pndz) < 0){
						this.runPackDef = 0;
						this.runPackTemp = 0;
						this.runPackIce = this.runPackIce + this.runRain;
					}
					//Rain not sufficient to bring pack to isothermal
					else if(this.runRain < pndz){
						this.runPackDef = this.runPackDef - (caln * this.runRain);
						this.runPackTemp = (-1 * this.runPackDef) / (this.runPackSwe * 500.0); //heatcapacity of ice = 500 cal/kg°C
						this.runPackIce = this.runPackIce + this.runRain;
					}
					//Rain in excess of amount required to bring pack to isothermal
					else{
						this.runPackDef = 0;
						this.runPackTemp = 0;
						this.runPackIce = this.runPackIce + pndz;
						this.runPackFreeWater = this.runRain - pndz;
						double calpr = train * (this.runRain - pndz) * 1000;// *
						// 2.54;
						energyGained(calpr);
					}
				}
				// Snowpack isothermal when rain occured
				else {
					this.runPackFreeWater = this.runPackFreeWater + this.runRain;
					double calpr = train * this.runRain * 1000;// * 2.54;
					energyGained(calpr);
				}
			}
			//added for J2k, I assume that all input from rain is used here. Am I right with that, USGS?
			//input as snow is needed in later routines and resetted there.
			this.runRain = 0;
		}
		// Set switch if rain/snow event with no antecedent snowpack
		else if (this.runRain > 0) {
			this.pptMixNoSnow.setValue(true);
		}
		
		if (this.runSnow > 0) {
			this.runPackSwe = this.runPackSwe + this.runSnow;
			this.runPackPrecip = this.runPackPrecip + this.runSnow;
			this.runPackIce = this.runPackIce + this.runSnow;

			if (tsnow >= 0) {
				this.runPackTemp = -1 * this.runPackDef / (this.runPackSwe * 500.0);
			} else {
				double calps = tsnow * this.runSnow * 500.0;
				if (this.runPackFreeWater > 0) {
					energyLost(calps);
				} else {
					this.runPackDef = this.runPackDef - calps;
					this.runPackTemp = -1 * this.runPackDef / (this.runPackSwe * 500.0);
				}
			}
		}
		
		
	}
    
    //PRMS: calin()
    private void energyGained(double calories){
		double dif, pmlt, apmlt, apk_ice, pwcap;
		
		dif = calories - this.runPackDef;
		
		//Calorie deficit exists (calories < this.runPackDef)
		//incoming calories are less than needed to bring pack to isothermal
		//but the pack temperature is rising and PackDef gets lower by the input calories
		if(dif < 0){
			this.runPackDef = this.runPackDef - calories;
			this.runPackTemp = (-1 * this.runPackDef) / (this.runPackSwe * 500.0);
		}
		//no calorie deficit or excess (Cal = this.runPackDef)
		else if(dif == 0){
			this.runPackTemp = 0;
			this.runPackDef = 0;
		}
		//calorie excess, melt snow (calories > this.runPackDef) 
		//(80 calories needed to melt 1g of water. 1mm = 1l = 1kg --> thus we need 80000 cal
		else{
			pmlt = dif / 80000;  //amount of water which can be melted by the calorie excess
			apmlt = pmlt * this.runSnowCovArea;
			this.runPackDef = 0;
			this.runPackTemp = 0;
			apk_ice = this.runPackIce / this.runSnowCovArea;
			
			//calories sufficient to melt entire pack
			if(pmlt > apk_ice){
				this.runSnowMelt = this.runSnowMelt + this.runPackSwe;
				this.runPackSwe = 0;
				this.iasw.setValue(false);
				this.runSnowCovArea = 0;
				this.runPackDef = 0;
				this.runPackTemp = 0;
				this.runPackIce = 0;
				this.runPackFreeWater = 0;
				this.runPackDepth = 0;
				this.runPss = 0;
				this.runPst = 0;
				this.runPackDensity = 0;
				this.runPackEnergyBal = 0;
			}
			//calories sufficient to melt part of pack
			else{
				this.runPackIce = this.runPackIce - apmlt;
				this.runPackFreeWater = this.runPackFreeWater + apmlt;
				pwcap = this.packFreeWaterCapacity.getValue() * this.runPackSwe;
				double wDif = this.runPackFreeWater - pwcap;
				if(wDif > 0){
					this.runSnowMelt = this.runSnowMelt + wDif;
					this.runPackFreeWater = pwcap;
					this.runPackSwe = this.runPackSwe - wDif;
					this.runPackDepth = this.runPackSwe / this.runPackDensity;
					this.runPss = this.runPackSwe;
				}
			}
		}
	}
    
    //PRMS: callos() -- calories are always negative
    private void energyLost(double calories){
		
		//if no free water exists in snowpack
		if(this.runPackFreeWater == 0){
			this.runPackDef = this.runPackDef - calories;
		}
		//free water does exist in pack (800 calories gained from freezing 1mm of water)
		else{
			double calnd = this.runPackFreeWater * 80000; //calories gained from freezing all free water first
			
			//testing if we really need to freeze all water to bring snow pack as close as possible to isothermal
			double dif = calories + calnd;
		
			//all free water freezes
			if(dif < 0){
				this.runPackDef = this.runPackDef - dif; //-1 * dif;
				this.runPackIce = this.runPackIce + this.runPackFreeWater;
				this.runPackFreeWater = 0;
			}
			//only part of free water freezes
			else{
				this.runPackIce = this.runPackIce + ((-1 * calories) / 80000.0);
				this.runPackFreeWater = this.runPackFreeWater - ((-1 * calories) / 80000.0);
				return;
			}
		}
		if(this.runPackSwe > 0){
			this.runPackTemp = (-1 * this.runPackDef) / (this.runPackSwe * 500.0);
		}
	}
    
//  routine to compute snow-covered areas
	//PRMS: snowcov()
    private void calcSnowCoveredArea(){
		double ai, pcty, difx, frac, af, dify;
		int jdx, idx;
		
		//start value is full cover?? PK: 16.08.06
		this.runSnowCovArea = snowAreaCurve[10];
		
		if(this.runPackSwe > this.runPst){
			this.runPst = this.runPackSwe;
		}
		ai = this.runPst;
		if(ai >= this.snowAreaThresh.getValue()){
			ai = this.snowAreaThresh.getValue();
		}
		if(this.runPackSwe > ai){
			this.iasw.setValue(false);
		}else{
			if(!this.newSnow){
				if(this.iasw.getValue()){
					if(this.runPackSwe > this.runSnowCurve){
						return;
					}
					//New snow not melted back to original partial area
					if(this.runPackSwe >= runPackSWEtm1){
						difx = this.runSnowCovArea - this.runSnowCovAreaSave;
						dify = this.runSnowCurve - runPackSWEtm1;
						pcty = 0.0;
						if(dify > 0.0000001){
							pcty = (this.runPackSwe - runPackSWEtm1) / dify;
						}
						this.runSnowCovArea = this.runSnowCovAreaSave + (pcty * difx);
						return;
					}
					//Pack water equivalent back to value before new snow
					else{
						this.iasw.setValue(false);
					}
				}
			}
			//New snow
			else{
				if(this.iasw.getValue()){
					this.runSnowCurve = this.runPackSwe - (0.25 * this.runSnow);
				}
				else{
					this.iasw.setValue(true);
					this.runSnowCovAreaSave = this.runSnowCovArea;
					this.runPackSWEtm1 = this.runPackSwe - this.runSnow;
					this.runSnowCurve = this.runPackSwe - (0.25 * this.runSnow);
				}
				return;
			}
			//Interpolate along snow area depletion curve
			frac = this.runPackSwe / ai;
			idx = (int)(10. * (frac + 0.2)) - 1;
			jdx = idx - 1;
			af = (double)(jdx - 1);
			dify = (frac * 10.) - af;
			difx = snowAreaCurve[idx] - snowAreaCurve[jdx];
			this.runSnowCovArea = snowAreaCurve[jdx] + (difx * dify);
		}
	}
    
	//PRMS: snowalb()
    private void calcSnowAlbedo(){
		//If no new snow, check to adjust albedo for shallow snowpack
		if(!newSnow){
			if(this.runLst){
				this.runLstSave = this.runPackAlbedoSave - 3.0;
				if(this.runLstSave < 1.0){
					this.runLstSave = 1.0;
				}
				if(this.iso.getValue() != 2){
					if(this.runLstSave > 5.0){
						this.runLstSave = 5.0;
					}
				}
				this.runLst = false;
				this.runPackSWEtm1 = 0;
			}
		}
		//New snow in melt stage
		else if(this.mso.getValue() == 2){
			if(this.rainPart < this.albedoResetRainMelt.getValue()){
				if(this.runSnow > this.albedoResetSnowMelt.getValue()){
					this.runLstSave = 0;
					this.runLst = false;
					this.runPackSWEtm1 = 0;
				}
				else{
					this.runPackSWEtm1 = this.runPackSWEtm1 + this.runSnow;
					if(this.runPackSWEtm1 > this.albedoResetSnowMelt.getValue()){
						this.runLstSave = 0;
						this.runLst = false;
						this.runPackSWEtm1 = 0;
					}
					else{
						if(!this.runLst){
							this.runPackAlbedoSave = this.runLstSave;
						}
						this.runLstSave = 0;
						this.runLst = true;
					}
				}
			}
		}
		//New snow in accumulation phase
		else{
			if(!this.pptMix){
				this.runLstSave = 0;
				this.runLst = false;
			}
			else if((this.rainPart - this.albedoResetRainAccu.getValue()) > 0){
				this.runLst = false;
			}
			else if((this.runSnow - this.albedoResetSnowAccu.getValue()) > 0){
				this.runLstSave = 0;
				this.runLst = false;
			}
			else{
				this.runLstSave = this.runLstSave - 3;
				if(this.runLstSave < 0){
					this.runLstSave = 0;
				}
				if(this.runLstSave > 5.0){
					this.runLstSave = 5;
				}
				this.runLst = false;
			}
			this.runPackSWEtm1 = 0;
		}
		
		//Set counter for days since last snowfall
		int i = (int)(this.runLstSave + 0.5);
		this.runLstSave = this.runLstSave + 1;
		
		//compute albedo
		//Old snow condition
		if(i > 0){
			//Old snow - Winter accumulation period
			if(this.intAlbedo.getValue() != 2){
				if(i <= MAXALB){
					this.packAlbedo.setValue(aCum[i]);
				}
				else{
					i = i - 12;
					if(i > MAXALB){
						i = MAXALB;
					}
					this.packAlbedo.setValue(aMlt[i]);
				}
			}
			//Old snow - Spring melt period
			else{
				if(i > MAXALB){
					i = MAXALB;
				}
				this.packAlbedo.setValue(aMlt[i]);
			}
		}
		//New snow condition
		else if(this.mso.getValue() == 2){
			this.packAlbedo.setValue(0.81);
			this.intAlbedo.setValue(2);
		}
		else{
			this.packAlbedo.setValue(0.91);
			this.intAlbedo.setValue(1);
		}
		
	}
    
	//PRSM: snowbal() 
	private double calcSnowBalance(double niteda, double temp, double esv, double trd, double cec, double cst, double sw, int month){
		double air = 0.585E-7 * Math.pow((temp + 273.16), 4);
		double emis = esv;
		
		double ts, sno;
		if(temp < 0){
			ts = temp;
			sno = air;
		}
		else{
			ts = 0;
			sno = 325.7;
		}
		
		//replaced for J2K reason
		//if(Basin_ppt > 0){
		if(this.precip.getValue() > 0){
			if(tStorm[month] == 1){
				if(niteda == 1){
					emis = 0.85;
					if(trd > 0.33){
						emis = this.emisNoPrecip.getValue();
					}
				}
				else{
					if(trd > 0.33){
						emis = 1.29 - (0.882 * trd);
					}
					if(trd >= 0.5){
						emis = 0.95 - (0.2 * trd);
					}
				}
			}
		}
		
		double sky = (1 - this.covDensWin.getValue()) * ((emis * air) - sno);
		double can = this.covDensWin.getValue() * (air - sno);
		double cecsub = 0;
		
		if(temp > 0){
//			replaced for J2K reason
			//if(Basin_ppt > 0){
			if(this.precip.getValue() > 0){
				cecsub = cec * temp;
			}
		}
		double calories = sky + can + cecsub + sw;
		
		if(ts >= 0){
			if(calories > 0){
				this.energyGained(calories);
				return calories;
			}
		}
		
		double qcond = cst * (ts - this.runPackTemp);
		if(qcond < 0){
			if(this.runPackTemp < 0){
				this.runPackDef = this.runPackDef - qcond;
				this.runPackTemp = (-1 * this.runPackDef) / (this.runPackSwe * 500.0);
			}
			else{
				this.energyLost(qcond);
			}
		}
		//qcond == 0
		else if(qcond <= 0){
			if(this.runPackTemp >= 0){
				if(calories > 0){
					this.energyGained(calories);
				}
			}
		}
		else if(ts >= 0){
			double pk_defsub = this.runPackDef - qcond;
			if(pk_defsub < 0){
				this.runPackDef = 0;
				this.runPackTemp = 0;
			}
			else{
				this.runPackDef = pk_defsub;
				this.runPackTemp = (-1 * pk_defsub) / (this.runPackSwe * 500.0);
			}
		}
		else{
			double pkt = (-1 * ts) * this.runPackSwe * 500.0;
			double pks = this.runPackDef - pkt;
			double pk_defsub = pks - qcond;
			if(pk_defsub < 0){
				this.runPackDef = pkt;
				this.runPackTemp = ts;
			}
			else{
				this.runPackDef = pk_defsub + pkt;
				this.runPackTemp = (-1 * this.runPackDef) / (this.runPackSwe * 500.0);
			}
		}
		return calories;
	}
	
	private void calcSnowEvaporation(){
		double ez = 0;
		if(this.covType.getValue() > 1){
			double cov = this.covDensWin.getValue();
			if(this.runTranspOn){
				cov = this.covDensSum.getValue();
			}
			ez = (this.petSublimProp.getValue() * this.runPet * this.runSnowCovArea) - (this.runAet * cov);
		}
		else{
			ez = this.petSublimProp.getValue() * this.runPet * this.runSnowCovArea;
		}
		
		if(ez < 0){
			this.snowET.setValue(0);
		}
		//Entirely depletes snowpack
		else if(ez >= this.runPackSwe){
			this.snowET.setValue(this.runPackSwe);
			this.runPackSwe = 0;
			this.runPackIce = 0;
			this.runPackDef = 0;
			this.runPackFreeWater = 0;
			this.runPackTemp = 0;
			this.runPackEnergyBal = 0;
		}
		//Partially depletes snowpack
		else{
			this.runPackIce = this.runPackIce - ez;
			double cal = this.runPackTemp * ez * 680000.0; //680000 cal needed to evaporate 1kg of water
			this.runPackDef = this.runPackDef + cal;
			this.runPackSwe = this.runPackSwe - ez;
			this.snowET.setValue(ez);
		}
	}
	
	public void cleanup() {
        
    }
}
