/*
 * SewerOverflowDevice_3.java
 * Created on 05. October 2012, 17:02
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package sewer;

import jams.data.*;
import jams.model.*;
import java.util.GregorianCalendar;

/**
 *
 * @author Sven Kralisch & Mériem Labbas & Christian Fischer
 */
@JAMSComponentDescription(title = "DoubleTransfer",
        author = "Sven Kralisch & Mériem Labbas & Christian Fischer",
        description = "Component used for the simulation of an overflow device. It takes the different components outflows"
        + "coming from a sewer reach(threshold test) and adds it to the receiving reach river."
        + "The calculation of the water level used to be compared to the threshold is based on flow velocity",
        version = "3.0_0",
        date = "2013-03-25")
public class SewerOverflowDevice_3 extends JAMSComponent {

    /*
     * Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "sewer length",
            unit = "m")
    public Attribute.Double length;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "sewer width",
            unit = "m")
    public Attribute.Double width;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "SOD threshold",
            unit = "m")
    public Attribute.Double threshold;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "SOD slope",
            unit = "deg")
    public Attribute.Double slope;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Is slope provided as proportion of length and elevation difference [m/m]?",
            defaultValue = "false")
    public Attribute.Boolean slopeAsProportion; 
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "SOD roughness")
    public Attribute.Double roughness;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Coefficient discharge",
            unit = "-")
    public Attribute.Double dischCoeff;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "pipe width",
            unit = "m")
    public Attribute.Double pipeWidth;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "pipe height",
            unit = "m")
    public Attribute.Double pipeHeight;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Target river reach")
    public Attribute.Entity to_river;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Target reach's receiving attributes")
    public Attribute.String[] inNames;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "Flow to be transferred to the SOD",
            unit = "L")
    public Attribute.Double[] inValues;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "Actual flow inside the sewer or river reach",
            unit = "L")
    public Attribute.Double[] actValues;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "outflow from the SOD",
            unit = "L")
    public Attribute.Double[] outValues;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Current time step",
            unit = "d")
    public Attribute.Calendar time;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "time interval",
            unit = "d")
    public Attribute.TimeInterval ti;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "sewer overflow sum")
    public Attribute.Double sewerOverflow;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "number of overflow events")
    public Attribute.Double overflowCount;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "normal water level in sewer calculated from the flowVelocity")
    public Attribute.Double waterLevelInitSOD;
    private int seconds;

    public void init() {

        if (ti.getTimeUnit() == GregorianCalendar.MINUTE) {
            seconds = 60 * ti.getTimeUnitCount();
        } else if (ti.getTimeUnit() == GregorianCalendar.HOUR_OF_DAY) {
            seconds = 3600 * ti.getTimeUnitCount();
        } else if (ti.getTimeUnit() == GregorianCalendar.DAY_OF_YEAR) {
            seconds = 24 * 3600 * ti.getTimeUnitCount();
        } else if (ti.getTimeUnit() == GregorianCalendar.MONTH) {
            seconds = time.getActualMaximum(GregorianCalendar.DAY_OF_MONTH) * 24 * 3600 * ti.getTimeUnitCount();
        }
    }
    int ts = 0;
    /*
     * Component run stages
     */

    public void run() {

        if (to_river.isEmpty()) {
            return;
        }

        // calc active and inflow volumes
        double volumeAct = 0, volumeIn = 0;

        for (int i = 0; i < actValues.length; i++) {
            volumeAct = volumeAct + actValues[i].getValue();
        }
        for (int i = 0; i < inValues.length; i++) {
            volumeIn = volumeIn + inValues[i].getValue();
        }

        // calc overall volume
        double volumeAll = volumeAct + volumeIn;

        // calc fractions related to overall volume
        double[] frac = new double[inValues.length];
        
        double slope = this.slope.getValue();
        if (!slopeAsProportion.getValue()) {
            slope = slope / 100;
        }

        for (int i = 0; i < inValues.length; i++) {
            if (volumeAll > 0) {
                frac[i] = (inValues[i].getValue() + actValues[i].getValue()) / volumeAll;
            }
        }
        
        double percIn;
        double percAct;
        if (volumeAll > 0) {
            percIn = volumeIn / volumeAll;
            percAct = volumeAct / volumeAll;
        } else {
            percIn = 0;
            percAct = 0;
        }
        
        // calc average reach water level based on overall volume, reach volume 
        // and flow velocity (beware of to high slope values!)
        double flowVelocity = calcFlowVelocity(volumeAll, width.getValue(), slope, roughness.getValue(), seconds);
        double flowLength = flowVelocity * seconds;
        double waterLevel = volumeAll / (flowLength * width.getValue() * 1000);
        
        // overflow is happening?
        if (waterLevel > threshold.getValue()) {

            double g = 9.80665; //gravitationnal constant
            double q;
            // let's use var names as defined in Faure (2007)
            double c = threshold.getValue();
            double L = pipeWidth.getValue();
            double T = c + pipeHeight.getValue();
            double h = waterLevel - c;

            if (h <= T-c) {  
                q = dischCoeff.getValue() * L * h * Math.sqrt(2 * g * h) * seconds * 1000;   
            } else {  
                q = dischCoeff.getValue() * L * (T - c) * Math.sqrt(2 * g * h) * seconds * 1000;  
            }

            double diffVolume = h * flowLength * width.getValue() * 1000;
            
            q = Math.min(q, diffVolume);           

            for (int i = 0; i < inValues.length; i++) {
                double overflowComp = frac[i] * q;
                inValues[i].setValue(inValues[i].getValue() - overflowComp * percIn);
                actValues[i].setValue(actValues[i].getValue() - overflowComp * percAct);
                to_river.setDouble(inNames[i].getValue(), overflowComp + to_river.getDouble(inNames[i].getValue()));
                outValues[i].setValue(overflowComp);
            }
            sewerOverflow.setValue(q);
            overflowCount.setValue(overflowCount.getValue() + 1);
        } else {
            for (int i = 0; i < inValues.length; i++) {
                outValues[i].setValue(0);
            }
            sewerOverflow.setValue(0);
        }
        waterLevelInitSOD.setValue(waterLevel);
    }
    
    /**
     * Calculates flow velocity in specific reach
     * @param q the runoff in the reach
     * @param width the width of reach
     * @param slope the slope of reach
     * @param rough the roughness of reach
     * @param secondsOfTimeStep the current time step in seconds
     * @return flow_velocity in m/s
     */
    public static double calcFlowVelocity(double q, double width, double slope, double rough, int secondsOfTimeStep){
        double afv = 1;
        double veloc = 0;
        
        /**
         *transfering liter/time to mÂ³/s
         **/
        double q_m = q / (1000 * secondsOfTimeStep);
        double rh = calcHydraulicRadius(afv, q_m, width);
        boolean cont = true;
        while(cont){
            veloc = (rough) * Math.pow(rh, (2.0/3.0)) * Math.sqrt(slope);
            if((Math.abs(veloc - afv)) > 0.001){
                afv = veloc;
                rh = calcHydraulicRadius(afv, q_m, width);
            } else{
                cont = false;
                afv = veloc;
            }
        }
        return afv;
    }
    
    /**
     * Calculates the hydraulic radius of a rectangular
     * stream bed depending on daily runoff and flow_velocity
     * @param v the flow velocity
     * @param q the daily runoff
     * @param width the width of reach
     * @return hydraulic radius in m
     */
    public static double calcHydraulicRadius(double v, double q, double width){
        double A = (q / v);
        
        double rh = A / (width + 2*(A / width));
        
        return rh;
    }    
}
