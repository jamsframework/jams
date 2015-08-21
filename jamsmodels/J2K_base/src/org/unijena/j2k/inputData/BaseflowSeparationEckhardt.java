/*
 * BaseflowSeparationEckhardt.java
 * Created on 24. November 2005, 09:48
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

package org.unijena.j2k.inputData;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
title="BaseflowSeparationEckhardt",
        author="Peter Krause",
        description="this program estimates groundwater contributions from streamflow records." +
        "Program for baseflow separation. References:" +
        "Eckhardt, K., 2005. How to construct recursive digital" +
        "filters for baseflow separation. Hydrological Processes 19,507-515." +
        "Eckhardt, K., 2008. A comparison of baseflow indices, which" +
        "were calculated with seven different baseflow separation" +
        "methods. Journal of Hydrology 352, 168-173." +
        "This JAVA implementation is based on the original FORTRAN source code provided" +
        "by Klaus Eckhard."
        )
        public class BaseflowSeparationEckhardt extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the measured streamflow values"
            )
            public Attribute.DoubleArray strflow;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "the surface runoff values"
            )
            public Attribute.DoubleArray surfq;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "the baseflow values"
            )
            public Attribute.DoubleArray baseq;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "the baseflow index for the entire time series"
            )
            public Attribute.Double bfi;
    
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
        
    }
    
    public void run() {
        //allocating the output arrays
        double[] strflw = this.strflow.getValue();
        int nrecs = strflw.length;
        double bfimax = 0;
        double[] surfq = new double[nrecs];
        double[] baseq = new double[nrecs];
        boolean[] eval = new boolean[nrecs];
        
        //Determine whether the stream is perennial of ephermal. It is
        //considered perennial if it is waterless during less than 10%
        //of all time steps with measured streamflow along with some variable 
        //initialisation.
        int ndry = 0;
        for(int i = 0; i < nrecs; i++){
            eval[i] = false;
            if(strflw[i] >= 0 && strflw[i] < 0.001)
                ndry++;
        }
        double rel = (double)ndry/(double)nrecs;
        if(rel < 0.1)
            bfimax = 0.8;
        else
            bfimax = 0.5;
        
        //     Only those values are evaluated, which are part of a
        //     recession period of at least five time steps. The streamflow
        //     has to recess since three time steps and continue to recess
        //     for another two time steps: 
        //     strflw(i-3) > strflwo(i-2) > strflw(i-1) > strflw(i) > strflw(i+1) > strflw(i+2).
        for(int i = 3; i < nrecs;i++){
            if(strflw[i-3] > 0 && strflw[i-2] > 0 && strflw[i-1] > 0 && strflw[i] > 0 && strflw[i+1] > 0 && strflw[i+2] > 0){
                if(strflw[i-3] > strflw[i-2] && strflw[i-2] > strflw[i-1] && strflw[i-1] > strflw[i] &&
                   strflw[i] > strflw[i+1] && strflw[i+1] > strflw[i+2]){
                    eval[i] = true;
                }
            }
        }
        
        //compute the recession constant
        double recConst = this.calcRecConstant(strflw, eval);
        
        //calculate the baseflow
        double bsum = 0;
        double strsum = 0;
        baseq[0] = 0.9 * bfimax * strflw[0];
        for(int i = 1; i < nrecs; i++){
            strsum = strsum + strflw[i];
            if(strflw[i] > 0){
                if(baseq[i-1] <= 0)
                    baseq[i] = 0.9 * bfimax * strflw[i];
                else{
                    baseq[i] = ((1. - bfimax) * recConst * baseq[i-1] + 
                                (1. - recConst) * bfimax * strflw[i]) / 
                                (1. - recConst * bfimax);
                    if(baseq[i] > strflw[i])
                        baseq[i] = strflw[i];
                    bsum = bsum + baseq[i];
                }
            }
            surfq[i] = strflw[i] - baseq[i];
        }
        double bfi = bsum/strsum;
        System.out.println("BFI of time series: " + bfi);
        //pass to output
        this.surfq.setValue(surfq);
        this.baseq.setValue(baseq);
        this.bfi.setValue(bfi);
        
        
        //TODO:
        
    }
    
    public void cleanup() {
        
    }
    /*
     Consider only streamflow values y, which are part of a recession
     period, and assume you draw a scatter plot of y(i+1) against y(i).
     If all assumptions were perfectly fullfilled, then all points in
     the scatter plot would lie on a straight line with slope a. Yet, 
     in reality this is not the case. Such a scatter plot shows that 
     the recession between y(i) and y(i+1) can assume quite different 
     speeds.
     Baseflow, however, can be characterised as the most slowly recessing
     streamflow component. This means that the baseflow recession is 
     described by those points, which form the upper bound of the scatter
     plot. Indeed, it is found that these points lie almost on a straight 
     line. Therefore, the recession constant can be found as the slope of
     a straight line, which is fitted to the upper bound of the scatter
     plot.
    */
    private double calcRecConstant(double[] strflw, boolean[] eval){
        double recConst = 0;
        boolean stop = false;
        //In order to find an optimal value of the recession constant, the 
        //recession constant is varied
        for(int i = 0; i < 1000; i++){
            recConst = 1. - (double)i * 0.001;
            
            //the recession constant represents the theoretical value of the 
            //ratio strflw(i+1)/strflw(i) during recession periods. The variable
            //recConst is considered the correct value of the recession constant
            //if none of the measured values strflw[i+1] ist greater than
            //1.02*recConst*strflw[i]
            for(int j = 2; j < strflw.length-1; j++){
                double test = 1.02 * recConst * strflw[i];
                if(eval[i] && strflw[i+1] > test){
                    stop = true;
                    break;
                }
            }
            if(stop)
                break;
        }
        return recConst;
    }
}
