/*
 * J2KProcessSnow.java
 * Created on 25. November 2005, 10:19
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
/*
<component class="org.unijena.j2k.snow.J2KProcessSnow" name="J2KProcessSnow">
<jamsvar name="area" provider="HRUContext" providervar="currentEntity.area"/>
<jamsvar name="actSlAsCf" provider="HRUContext" providervar="currentEntity.actSlAsCf"/>
<jamsvar name="minTemp" provider="HRUContext" providervar="currentEntity.tmean"/>
<jamsvar name="meanTemp" provider="HRUContext" providervar="currentEntity.tmean"/>
<jamsvar name="maxTemp" provider="HRUContext" providervar="currentEntity.tmean"/>
<jamsvar name="netRain" provider="HRUContext" providervar="currentEntity.netRain"/>
<jamsvar name="netSnow" provider="HRUContext" providervar="currentEntity.netSnow"/>
<jamsvar name="snowTotSWE" provider="HRUContext" providervar="currentEntity.snowTotSWE"/>
<jamsvar name="drySWE" provider="HRUContext" providervar="currentEntity.drySWE"/>
<jamsvar name="totDens" provider="HRUContext" providervar="currentEntity.totDens"/>
<jamsvar name="dryDens" provider="HRUContext" providervar="currentEntity.dryDens"/>
<jamsvar name="snowDepth" provider="HRUContext" providervar="currentEntity.snowDepth"/>
<jamsvar name="snowAge" provider="HRUContext" providervar="currentEntity.snowAge"/>
<jamsvar name="snowColdContent" provider="HRUContext" providervar="currentEntity.snowColdContent"/>
<jamsvar name="snowMelt" provider="HRUContext" providervar="currentEntity.snowMelt"/>
<jamsvar name="snow_trs" globvar="snow_trs"/>
<jamsvar name="snow_trans" globvar="snow_trans"/>
<jamsvar name="t_factor" value="0.7"/>
<jamsvar name="r_factor" value="0.008"/>
<jamsvar name="g_factor" value="4.04"/>
<jamsvar name="snowCritDens" value="0.45"/>
<jamsvar name="ccf_factor" value="0.001"/>
</component>
 */
package j2k_Himalaya.snow;

//import org.unijena.j2k.snow.*;
//import org.unijena.j2k.snow.*;
import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "J2KProcessSnow1",
author = "Peter Krause",
description = "Calculates snow accumulation, metamorphosis and melt// by Santosh : liq_water -ve disabled+ inSnow adn InRain included")
public class J2KProcessSnow2 extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "time")
    public Attribute.Calendar time;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "id")
    public Attribute.Double id;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The current hru entity")
    public Attribute.Entity entity;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "attribute area")
    public Attribute.Double area;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state var slope-aspect-correction-factor")
    public Attribute.Double actSlAsCf;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "minimum temperature if available, else mean temp")
    public Attribute.Double minTemp;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "mean temperature")
    public Attribute.Double meanTemp;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "maximum temperature if available, else mean temp")
    public Attribute.Double maxTemp;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "state variable net rain")
    public Attribute.Double netRain;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "state variable net snow")
    public Attribute.Double netSnow;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "total snow water equivalent")
    public Attribute.Double snowTotSWE;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "dry snow water equivalent")
    public Attribute.Double drySWE;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "total snow density")
    public Attribute.Double totDens;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "dry snow density")
    public Attribute.Double dryDens;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "snow depth")
    public Attribute.Double snowDepth;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "snow age")
    public Attribute.Double snowAge;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "snow cold content")
    public Attribute.Double snowColdContent;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "daily snow melt")
    public Attribute.Double snowMelt;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "base temperature",
    defaultValue = "0")
    public Attribute.Double baseTemp;
    /*@JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
    description = "Snow parameter TRANS"
    )
    public Attribute.Double snow_trans;*/
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "temperature factor for snowmelt")
    public Attribute.Double t_factor;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "rain factor for snowmelt")
    public Attribute.Double r_factor;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "ground factor for snowmelt")
    public Attribute.Double g_factor;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "critical density")
    public Attribute.Double snowCritDens;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "cold content factor")
    public Attribute.Double ccf_factor;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "0 - ddf, 1 - complex")
    public Attribute.Integer snowMeltFormula;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "module active")
    public Attribute.Boolean active;

//        @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
//    update = JAMSVarDescription.UpdateType.RUN,
//    description = " the input Snow which produce snowmelt")
//    public Attribute.Double inSnow;
//
//    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
//    update = JAMSVarDescription.UpdateType.RUN,
//    description = "Rain in snow part which is input for snowmelt")
//    public Attribute.Double inRain;
    
    double run_area;
    double in_snow;
    double in_rain;
    double run_snowDepth;
    double run_totSWE;
    double run_drySWE;    double run_initDens = 0;
    double run_totDens;
    double run_dryDens;
    double run_snowAge;
    double run_coldContent;
    double run_snowMelt = 0;
    /*
     *  Component run stages
     */

    public void init() {
        if (this.active == null || this.active.getValue()) {
            this.snowDepth.setValue(0.0);
            this.snowTotSWE.setValue(0.0);
            this.drySWE.setValue(0.0);
            this.totDens.setValue(0.0);
            this.dryDens.setValue(0.0);
            this.snowAge.setValue(0);
            this.snowColdContent.setValue(0.0);
        }
    }

    public void run() throws Attribute.Entity.NoSuchAttributeException {
//
//        Attribute.Calendar c = JAMSDataFactory.createCalendar();
//        c.setValue("1997-01-21 00:00");
//
//        if (time.equals(c) && (id.getValue() == 1787)) {
//
//            System.out.println("");
//
//        }

        if (this.active == null || this.active.getValue()) {
            //if(time.get(time.DAY_OF_MONTH) == 7 && entity.getDouble("ID") == 3607)
            //    System.out.getRuntime().println("stop!");
            this.run_area = this.area.getValue();


            double SAC = this.actSlAsCf.getValue();

            this.in_snow = this.netSnow.getValue();
            this.in_rain = this.netRain.getValue();
            double balIn = this.in_snow + this.in_rain;
            
//            this.inSnow.setValue(in_snow);
//            this.inRain.setValue(in_rain);

            double in_minTemp = this.minTemp.getValue();
            double in_meanTemp = this.meanTemp.getValue();
            double in_maxTemp = this.maxTemp.getValue();

            this.run_snowDepth = snowDepth.getValue();
            this.run_totSWE = snowTotSWE.getValue();
            double balStorStart = this.run_totSWE;
            this.run_drySWE = drySWE.getValue();
            this.run_totDens = totDens.getValue();
            this.run_dryDens = dryDens.getValue();
            this.run_snowAge = snowAge.getValue();
            this.run_coldContent = snowColdContent.getValue();

            double critDens = snowCritDens.getValue();
            double coldContentFactor = ccf_factor.getValue();
            double TRS = baseTemp.getValue();
            //double TRANS = snow_trans.getValue();
            double temp_fac = t_factor.getValue();
            double rain_fac = r_factor.getValue();
            double ground_fac = g_factor.getValue();


            this.run_snowMelt = 0;
            double out_netRain = 0;
            double out_netSnow = 0;

            double accuTemp = (in_meanTemp + in_minTemp) / 2.0;
            double meltTemp = (in_meanTemp + in_maxTemp) / 2.0;

            run_coldContent = run_coldContent + this.calcColdContent(in_meanTemp, coldContentFactor);
            if (run_coldContent > 0) {
                run_coldContent = 0;
            }

            if (run_snowDepth > 0) {
                //increasing snow age by one day
                run_snowAge += 1;
            }

            if (in_snow > 0) {

                this.calcSnowAccumulation(accuTemp, run_area, critDens);
            }


            if ((meltTemp >= TRS) && (this.run_snowDepth > 0)) {
                this.calcMetamorphosis(meltTemp, TRS, temp_fac, rain_fac, ground_fac, run_area, SAC, critDens);
            }

            this.calcSnowDensities(run_area);

            this.netRain.setValue(this.in_rain);
            this.netSnow.setValue(this.in_snow);
            this.snowTotSWE.setValue(this.run_totSWE);
            this.drySWE.setValue(this.run_drySWE);
            this.totDens.setValue(this.run_totDens);
            this.dryDens.setValue(this.run_dryDens);
            this.snowDepth.setValue(this.run_snowDepth);
            this.snowAge.setValue(this.run_snowAge);
            this.snowColdContent.setValue(this.run_coldContent);
            if (this.run_snowMelt > 0) {
                int i = 0;
            }
            this.snowMelt.setValue(this.run_snowMelt);
            double balStorEnd = this.run_totSWE;
            double balOut = this.run_snowMelt + this.in_rain + this.in_snow;
            double balance = balIn + (balStorStart - balStorEnd) - balOut;
            if (Math.abs(balance) > 0.0000001 * (balIn + balOut + balStorStart + balStorEnd) * 0.25) {
                getModel().getRuntime().println("balance error in snow module: " + balance);
                getModel().getRuntime().println("balIn: " + balIn);
                getModel().getRuntime().println("balStorStart: " + balStorStart);
                getModel().getRuntime().println("balStorEnd: " + balStorEnd);
                getModel().getRuntime().println("balOut: " + balOut);
                getModel().getRuntime().println("shit!");
            }
            //if(this.run_drySWE > this.run_totSWE)
            //    System.out.getRuntime().println("dry is larger than tot at end at time: " + time.toString() + " in entity: " + entity.getDouble("ID"));
            if (this.run_snowMelt < 0) {
                getModel().getRuntime().println("negative snowmelt!!");
            }
        }
    }

    public void cleanup() {
        if (this.active == null || this.active.getValue()) {
            this.snowDepth.setValue(0.0);
            this.snowTotSWE.setValue(0.0);
            this.drySWE.setValue(0.0);
            this.totDens.setValue(0.0);
            this.dryDens.setValue(0.0);
            this.snowAge.setValue(0);
            this.snowColdContent.setValue(0.0);
        }
    }

    private double calcColdContent(double temperature, double coldContentFactor) {
        double cc_factor = coldContentFactor * 24;
        return (cc_factor * temperature);
    }

    /** calculates snow accumulation for a spatial unit and one daily
     * time step. Snow accumulation is positive if snow falls on snow
     * pack and can be negative if rain on snow occurs. Snow pack settlement
     * because of rain on snow is also covered here following the approach
     * of BERTLE 1966 as presented by KRAUSE 2001; local vars rain and snow
     * are set to zero after accumulation
     * @return true if successfull, false otherwise
     */
    private boolean calcSnowAccumulation(double temp, double area, double critDens) {
        double deltaHeight = 0;

        //increase of snow pack because of snow fall
        if (this.in_snow > 0) {
            double new_snow_density = this.calcNewSnowDensity(temp);
            deltaHeight = this.in_snow / (new_snow_density * area);
            this.run_snowDepth = this.run_snowDepth + deltaHeight;

            //increase of dry and total snow water equivalent by snow precip amount
            //double old_SWE = this.tot_SWE;
            this.run_drySWE = this.run_drySWE + this.in_snow;
            this.run_totSWE = this.run_totSWE + this.in_snow;
            this.in_snow = 0;

            //recalculation of snow Densities
            this.calcSnowDensities(area);

            //resetting snow age
            this.run_snowAge = 0;

            //saving the initial density for snow pack settlement
            this.run_initDens = this.run_dryDens;
        }

        //calculation of snow pack settlement by free water
        if (this.in_rain > 0) {
            this.calcRainSnowSettlement(this.in_rain);
            this.in_rain = 0;
        }
        //if snow pack has vanished, nothing more to do
        if (this.run_snowDepth == 0) {
            return true;
        }

        //Calculation of new snow densities
        this.calcSnowDensities(area);

        /** water from snow pack */
        if (this.run_totDens > critDens) {
            this.run_snowMelt = this.run_snowMelt + calcSnowMeltRunoff(critDens, area);
            //if(this.run_snowMelt < 0)
            //System.out.getRuntime().println("negative SM a");
        } else {
            double pRO = this.calcPotRunoff(critDens, this.run_totDens, this.run_totSWE - this.run_drySWE);
            this.run_snowMelt = this.run_snowMelt + pRO;
            this.run_totSWE = this.run_totSWE - pRO;
            //if(this.run_snowMelt < 0)
            //System.out.getRuntime().println("negative SM b because of: " + pRO);
        }

        //Calculation of new snow densities
        this.calcSnowDensities(area);
        return true;
    }

    /** calculates density of new fallen snow depending
     * on the mean temperature. Follows the approach
     * of KUCHMENT 1983 and VEHVILÄINEN 1992 as presented
     * by HERPERTZ 2002
     * @param tmean the current mean temperature of the spatial unit
     * @return density of new fallen snow
     */
    private double calcNewSnowDensity(double temp) {
        double new_snow_density = 0;
        if (temp > -15) {
            new_snow_density = 0.13 + 0.0135 * temp + 0.00045 * Math.pow(temp, 2);
        } else {
            new_snow_density = 0.02875;
        }

        return new_snow_density;
    }

    private void calcSnowDensities(double area) {
        //Calculation of new snow densities
        if (this.run_snowDepth > 0) {
            this.run_totDens = this.run_totSWE / (area * this.run_snowDepth);
            this.run_dryDens = this.run_drySWE / (area * this.run_snowDepth);
        } else {
            this.run_totDens = 0;
            this.run_dryDens = 0;
        }
    }

    private void calcRainSnowSettlement(double inputWater) {
        /**************************************************************
         * /*Change of snow depth due to setting caused by rain on snow or meltwater
         ***************************************************************/
        double pw = 100;
        if (inputWater > 0) {
            this.run_totSWE = this.run_totSWE + inputWater;
            this.in_rain = 0;
            pw = (this.run_totSWE / this.run_drySWE) * 100.0;
        }

        //determination of settle rate after BERTLE 1966 due to rain on snow
        double ph = 147.4 - 0.474 * pw;

        if (ph > 0) {
            this.run_snowDepth = this.run_snowDepth * (ph / 100.);
            this.calcSnowDensities(this.run_area);
            if (this.run_dryDens > this.snowCritDens.getValue()) {
                double maxSWE = this.snowCritDens.getValue() * run_area * this.run_snowDepth;
                this.run_drySWE = maxSWE;
            }
        } else { //loss of whole snow pack because of heavy rain on few snow or complete melting
            this.run_snowMelt = this.run_snowMelt + this.run_totSWE;
            this.run_snowDepth = 0;
            this.run_totSWE = 0;
            this.run_drySWE = 0;
            this.run_totDens = 0;
            this.run_dryDens = 0;
            this.run_snowAge = 0;
            //if(this.run_snowMelt < 0)
            //    System.out.getRuntime().println("negative SM 0");
        }

    }

    private double calcSnowMeltRunoff(double critDens, double area) {
        /** maximum water capacity of snow pack */
        /** The snow cover can store liquid water in its pores until a certain critical density (critDens). //
        This storage capability is lost nearly completely and irreversibly when a certain amount of liquid water proportionally to the total
        snow water equivalent (between 40% and 45%) is reached.
        This is taken into account in the modeling by the calculation of the maximum water content (WSmax) of the snow cover: */
        double Wsmax = critDens * this.run_snowDepth * area;
        double snowmelt = this.run_totSWE - Wsmax;
        this.run_totSWE = Wsmax;
        // The critical density (critDens) needs to be given by the user. The stored water in the snow cover that goes beyond this threshold is drained:

        this.calcSnowDensities(area);
        return snowmelt;
    }

    private double calcPotRunoff(double crit_dens, double tot_dens, double liq_water) {
        // if(Math.abs(liq_water) > 0.00001 && liq_water < 0)
        //   getModel().getRuntime().println("liq_water is negative: "+liq_water);
        double potRunoff = (1 - Math.exp(-1 * Math.pow((crit_dens / tot_dens), 4))) * liq_water;
        if (potRunoff < 0) {
            potRunoff = 0;
        }
        return potRunoff;
    }

    private boolean calcMetamorphosis(double temp, double TRS, double temp_fac, double rain_fac, double ground_fac, double area, double SAC, double critDens) {
        /**calculation of snowmelt - complex formula*/
        //@todo integration of canopy shadow by LAI
        double potMeltrate = 0;
        potMeltrate = this.calcPotMR_semiComp(temp, TRS, temp_fac, rain_fac, ground_fac, area);

        if (Math.abs(this.run_coldContent) >= potMeltrate) {
            this.run_coldContent = this.run_coldContent + potMeltrate;
            potMeltrate = 0;
        } else {
            potMeltrate = potMeltrate + this.run_coldContent;
            this.run_coldContent = 0;
        }

        potMeltrate = potMeltrate * area;

        /** correcting melt rate due to slope aspect combination of unit */
        //switched on at 10.03.2005
        potMeltrate = potMeltrate * SAC;

        /** decrease of dry snow depth caused by snow melt */
        double deltaSnowDepth = potMeltrate / (this.run_dryDens * area);

        //if(this.run_snowMelt < 0)
        //    System.out.getRuntime().println("negative SM 1");
        /** depletion of whole snow pack */
        if (deltaSnowDepth >= this.run_snowDepth) {
            deltaSnowDepth = this.run_snowDepth;
            this.run_snowDepth = 0;
            this.run_totDens = 0;
            this.run_dryDens = 0;
            this.run_snowMelt = this.run_snowMelt + this.run_totSWE;
            this.run_totSWE = 0;
            this.run_drySWE = 0;
            this.run_snowAge = 0;
            //if(this.run_snowMelt < 0)
            //System.out.getRuntime().println("negative SM 1.5");
            //nothing more to do -- no snow left
            return true;
        }
        //if(this.run_snowMelt < 0)
        //    System.out.getRuntime().println("negative SM 2");

        /** decrease of snow pack due to snow melt */
        this.run_snowDepth = this.run_snowDepth - deltaSnowDepth;

        /** decrease of dry SWE due to snow melt */
        this.run_drySWE = this.run_drySWE - potMeltrate;
        potMeltrate = 0;

        //Calculation of new snow densities
        this.calcSnowDensities(area);

        //if(this.run_snowMelt < 0)
        //    System.out.getRuntime().println("negative SM 3");
        /** potential water from snow pack */
        if (this.run_totDens >= critDens) {
            this.run_snowMelt = this.run_snowMelt + calcSnowMeltRunoff(critDens, area);
            //if(this.run_snowMelt < 0)
            //System.out.getRuntime().println("negative SM 4");
        } else {
            double pRO = this.calcPotRunoff(critDens, this.run_totDens, this.run_totSWE - this.run_drySWE);
            this.run_snowMelt = this.run_snowMelt + pRO;
            this.run_totSWE = this.run_totSWE - pRO;
            //if(this.run_snowMelt < 0)
            //System.out.getRuntime().println("negative SM 5");
        }
        //Calculation of new snow densities
        this.calcSnowDensities(area);

        /** settlement of snow-pack by rain and/or snowmelt */
        this.calcRainSnowSettlement(this.in_rain + potMeltrate);
        this.in_rain = 0;
     
        //if snow pack has vanished, nothing more to do
        if (this.run_snowDepth == 0) {
            return true;
        }

        //Calculation of new snow densities
        this.calcSnowDensities(area);

        /** water from snow pack */
        if (this.run_totDens >= critDens) {
            this.run_snowMelt = this.run_snowMelt + calcSnowMeltRunoff(critDens, area);
            //if(this.run_snowMelt < 0)
            //System.out.getRuntime().println("negative SM 6");
        } else {
            double pRO = this.calcPotRunoff(critDens, this.run_totDens, this.run_totSWE - this.run_drySWE);
            this.run_snowMelt = this.run_snowMelt + pRO;
            this.run_totSWE = this.run_totSWE - pRO;
            //if(this.run_snowMelt < 0)
            //System.out.getRuntime().println("negative SM 7");
        }

        this.calcSnowDensities(area);
        return true;
    }

    private double calcPotMR_semiComp(double temp, double TRS, double temp_fac, double rain_fac, double ground_fac, double area) {
        double meltTemp = temp - TRS;
        double potMR = (temp_fac * meltTemp + ground_fac + rain_fac * (this.in_rain / area) * meltTemp);
        //avoid negative melt rates
        if (potMR < 0) {
            potMR = 0;
        }
        return potMR;

    }
}

