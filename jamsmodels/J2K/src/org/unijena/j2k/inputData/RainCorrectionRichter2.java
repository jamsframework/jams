/*
 * RainCorrectionRichter2.java
 * Created on 8. May 2008, 09:48
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
package org.unijena.j2k.inputData;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import org.unijena.j2k.statistics.IDW;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title = "RainCorrectionRichter2",
author = "Peter Krause",
description = "Applies correction according to RICHTER 1985 for measured daily precip sums,"
+ "this module allows the consideration of the station location and shelter")
public class RainCorrectionRichter2 extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "time")
    public Attribute.Calendar time;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "the precip values")
    public Attribute.DoubleArray precip;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "temperature for the correction function")
    public Attribute.DoubleArray temperature;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "corrected precip values")
    public Attribute.DoubleArray rcorr;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "Array of temperature station elevations")
    public Attribute.DoubleArray tempElevation;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "Array of temperature station's x coordinate")
    public Attribute.DoubleArray tempXCoord;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "Array of temperature station's y coordinate")
    public Attribute.DoubleArray tempYCoord;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "Regression coefficients for temperature")
    public Attribute.DoubleArray tempRegCoeff;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "Array of precip station elevations")
    public Attribute.DoubleArray rainElevation;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "Array of precip station's x coordinate")
    public Attribute.DoubleArray rainXCoord;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "Array of precip station's y coordinate")
    public Attribute.DoubleArray rainYCoord;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "number of temperature station for IDW")
    public Attribute.Integer tempNIDW;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "power for IDW function")
    public Attribute.Double pIDW;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "regression threshold")
    public Attribute.Double regThres;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "snow_trs")
    public Attribute.Double snow_trs;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "snow_trans")
    public Attribute.Double snow_trans;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "Use caching of regionalised data?")
    public Attribute.Boolean dataCaching;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "Station shelter [1 - no protection; 2 - gentle protection; "
    + "3 - moderate protection; 4 - strong protection]")
    public Attribute.IntegerArray protection;
    
    IDW idw = new IDW();
    
    @Override
    public void run() {

        double[] precip = this.precip.getValue();
        double[] temperature = this.temperature.getValue();

        int n = precip.length;

        double[] rcorr = this.rcorr.getValue();
        //allocate only when it is really needed
        if (rcorr == null || rcorr.length != n) {
            rcorr = new double[n];
        }
        //parameterization of rain stations        
        double[] rainElev = rainElevation.getValue();
        double[] rainX = rainXCoord.getValue();
        double[] rainY = rainYCoord.getValue();

        double rsq = this.tempRegCoeff.getValue()[2];
        double grad = this.tempRegCoeff.getValue()[1];

        idw.init(this.tempXCoord.getValue(), this.tempYCoord.getValue(), this.tempElevation.getValue(), (int) this.pIDW.getValue(), IDW.Projection.ANY);

        //temperature for each rain station
        for (int r = 0; r < n; r++) {
            //use IDW to calc rainTemp
            double rainTemp = 0;
            if (rsq > this.regThres.getValue()) {
                rainTemp = idw.getElevationCorrectedIDW(rainX[r], rainY[r], rainElev[r], grad, temperature, tempNIDW.getValue());
            } else {
                rainTemp = idw.getIDW(rainX[r], rainY[r], temperature, tempNIDW.getValue());
            }
            //determine rain and snow amount of precip
            double pSnow = (snow_trs.getValue() + snow_trans.getValue() - rainTemp)
                    / (2 * snow_trans.getValue());

            //fixing upper and lower bound for pSnow (has to be between 0 and 1
            if (pSnow > 1.0) {
                pSnow = 1.0;
            } else if (pSnow < 0) {
                pSnow = 0;                //dividing input precip into rain and snow
            }
            double rain = (1 - pSnow) * precip[r];
            double snow = pSnow * precip[r];

            //Calculating relative Winderror acc to RICHTER 1995
            double as = 0, bs = 0, ls = 0;
            double ar = 0, br = 0, lr = 0;
            //coefficients according to protection
            switch (protection.getValue()[r]) {
                case 1:
                    lr = 0.642;
                    ar = 0.1801;
                    br = -0.608;
                    ls = 1.102;
                    as = 0.6774;
                    bs = -0.204;
                    break;

                case 2:
                    lr = 0.492;
                    ar = 0.1421;
                    br = -0.505;
                    ls = 0.938;
                    as = 0.5424;
                    bs = -0.211;
                    break;

                case 3:
                    lr = 0.304;
                    ar = 0.1029;
                    br = -0.519;
                    ls = 0.516;
                    as = 0.5424;
                    bs = -0.211;
                    break;

                case 4:
                    lr = 0.270;
                    ar = 0.0584;
                    br = -0.693;
                    ls = 0.326;
                    as = 0.1008;
                    bs = -0.022;
                    break;

                default:
                    getModel().getRuntime().sendInfoMsg("Wrong protection type for rain station");
                    break;
            }

            if (snow > 0) {//if(pSnow >= 1.0){      //set to all snow (5/11/01), rechanged 1.03.02
                if (snow <= 0.1) {
                    snow = snow + (snow * ls);
                } else {
                    double relSnow = as * Math.pow(snow, bs);
                    snow = snow + (snow * relSnow);
                }
            }
            if (rain > 0) { //if(pSnow < 1.0){//
                if (rain < 0.1) {
                    rain += (rain * lr);
                } else {
                    double relRain = ar * Math.pow(rain, br);
                    rain = rain + (rain * relRain);
                }
            }

            //Calculating error from evaporation and wetting acc. to Richter
            double wetErr;
            if (precip[r] < 0.1) {
                wetErr = 0;
            } else {
                if (time.get(Attribute.Calendar.MONTH) >= 4 & time.get(Attribute.Calendar.MONTH) < 10) { //Summer half of the year
                    if (precip[r] >= 9.0) {
                        wetErr = 0.47;
                    } else {
                        wetErr = 0.08 * Math.log(precip[r]) + 0.225;
                    }
                } else {   //Winter half of the year
                    if (precip[r] >= 9.0) {
                        wetErr = 0.3;
                    } else {
                        wetErr = 0.05 * Math.log(precip[r]) + 0.13;
                    }
                }
            }
            //Calculating corrected rain_value
            if (precip[r] == JAMS.getMissingDataValue()) {
                rcorr[r] = JAMS.getMissingDataValue();
            } else {
                rcorr[r] = rain + snow + wetErr;
            }
        }
        this.rcorr.setValue(rcorr);
    }
}
