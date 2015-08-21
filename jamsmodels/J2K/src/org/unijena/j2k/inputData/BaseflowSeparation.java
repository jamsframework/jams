/*
 * BaseflowSeparationSWAT.java
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
title="CalcAbsoluteHumidity",
        author="Peter Krause",
        description="this program estimates groundwater contributions from streamflow records." +
        "It uses a recursive filter technique to seperate base flow and also" +
        "calculates the streamflow recession constant (alpha)" +
        "This JAVA implementation is a reworked version of the FORTRAN implementation" +
        "of the method taken from the SWAT webpage"
        )
        public class BaseflowSeparation extends JAMSComponent {
    
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
            description = "the baseflow values from pass 1"
            )
            public Attribute.DoubleArray baseq;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the filter level [1..3]"
            )
            public Attribute.Integer filterLevel;
    
    final double F1 = 0.925;
    final double F2 = 0.9625;
    /*
     *  Component run stages
     */
    
    public void init() {
        
        
    }
    
    public void run() {
        
        //allocating the output arrays
        double[] strflw = this.strflow.getValue();
        int nrecs = strflw.length;
        double[] surfq = new double[nrecs];
        double[] baseq1 = new double[nrecs];
        double[] baseq2 = new double[nrecs];
        double[] baseq3 = new double[nrecs];
        double[] baseq4 = new double[nrecs];
        double[] baseq5 = new double[nrecs];
        double[] dirq1 = new double[nrecs];
        double[] dirq2 = new double[nrecs];
        double[] dirq3 = new double[nrecs];
        double[] dirq4 = new double[nrecs];
        double[] dirq5 = new double[nrecs];
       
        double sumbf1 = 0;
        double sumbf2 = 0;
        double sumbf3 = 0;
        double sumbf4 = 0;
        double sumbf5 = 0;
        double sumstrflw = 0;
        
        //initialisation
        surfq[0] = strflw[0] / 2; 
        baseq1[0] = strflw[0] / 2;
        baseq2[0] = 0;
        baseq3[0] = 0;
        
        //Pass 1 -- forward
        for(int i = 1; i < nrecs; i++){
            surfq[i] = F1 * surfq[i-1] + F2 * (strflw[i] - strflw[i-1]);
            if(surfq[i] < 0)
                surfq[i] = 0;
            baseq1[i] = strflw[i] - surfq[i];
            if(baseq1[i] < 0)
                baseq1[i] = 0;
            if(baseq1[i] > strflw[i]){
                baseq1[i] = strflw[i];
                surfq[i] = 0;
            }
            dirq1[i] = strflw[i] - baseq1[i];
        }
        //Pass 2 -- forward
        for(int i = 1; i < nrecs; i++){
            surfq[i] = F1 * surfq[i-1] + F2 * (baseq1[i] - baseq1[i-1]);
            if(surfq[i] < 0)
                surfq[i] = 0;
            baseq2[i] = baseq1[i] - surfq[i];
            if(baseq2[i] < 0)
                baseq2[i] = 0;
            if(baseq2[i] > baseq1[i]){
                baseq2[i] = baseq1[i];
                surfq[i] = 0;
            }
            dirq2[i] = strflw[i] - baseq2[i];
        }
        //Pass 3 -- forward
        for(int i = 1; i < nrecs; i++){
            surfq[i] = F1 * surfq[i-1] + F2 * (baseq2[i] - baseq2[i-1]);
            if(surfq[i] < 0)
                surfq[i] = 0;
            baseq3[i] = baseq2[i] - surfq[i];
            if(baseq3[i] < 0)
                baseq3[i] = 0;
            if(baseq3[i] > baseq2[i]){
                baseq3[i] = baseq2[i];
                surfq[i] = 0;
            }
            dirq3[i] = strflw[i] - baseq3[i];
        }
        //Pass 4 -- forward
        for(int i = 1; i < nrecs; i++){
            surfq[i] = F1 * surfq[i-1] + F2 * (baseq3[i] - baseq3[i-1]);
            if(surfq[i] < 0)
                surfq[i] = 0;
            baseq4[i] = baseq3[i] - surfq[i];
            if(baseq4[i] < 0)
                baseq4[i] = 0;
            if(baseq4[i] > baseq3[i]){
                baseq4[i] = baseq3[i];
                surfq[i] = 0;
            }
            dirq4[i] = strflw[i] - baseq4[i];
        }
        //Pass 5 -- forward
        for(int i = 1; i < nrecs; i++){
            surfq[i] = F1 * surfq[i-1] + F2 * (baseq4[i] - baseq4[i-1]);
            if(surfq[i] < 0)
                surfq[i] = 0;
            baseq5[i] = baseq4[i] - surfq[i];
            if(baseq5[i] < 0)
                baseq5[i] = 0;
            if(baseq5[i] > baseq4[i]){
                baseq5[i] = baseq4[i];
                surfq[i] = 0;
            }
            dirq5[i] = strflw[i] - baseq5[i];
        }
        //Perform summary calculations
        for(int i = 0; i < nrecs; i++){
            sumbf1 = sumbf1 + baseq1[i];
            sumbf2 = sumbf2 + baseq2[i];
            sumbf3 = sumbf3 + baseq3[i];
            sumbf4 = sumbf4 + baseq4[i];
            sumbf5 = sumbf5 + baseq5[i];
            sumstrflw = sumstrflw + strflw[i];
        }
        //calculate baseflow fractions
        double bfi1 = sumbf1 / sumstrflw;
        double bfi2 = sumbf2 / sumstrflw;
        double bfi3 = sumbf3 / sumstrflw;
        double bfi4 = sumbf4 / sumstrflw;
        double bfi5 = sumbf5 / sumstrflw;
        
        //selecting output according to filter level
        if(this.filterLevel.getValue() == 1){
            this.surfq.setValue(dirq1);
            this.baseq.setValue(baseq1);
            System.out.println("BFI: " + bfi1);
        }
        else if(this.filterLevel.getValue() == 2){
            this.surfq.setValue(dirq2);
            this.baseq.setValue(baseq2);
            System.out.println("BFI: " + bfi2);
        }
        else if(this.filterLevel.getValue() == 3){
            this.surfq.setValue(dirq3);
            this.baseq.setValue(baseq3);
            System.out.println("BFI: " + bfi3);
        }
        else if(this.filterLevel.getValue() == 4){
            this.surfq.setValue(dirq4);
            this.baseq.setValue(baseq4);
            System.out.println("BFI: " + bfi4);
        }
        else if(this.filterLevel.getValue() == 5){
            this.surfq.setValue(dirq5);
            this.baseq.setValue(baseq5);
            System.out.println("BFI: " + bfi5);
        }
        
        //TODO:
        //integrate the alpha value
    }
    
    public void cleanup() {
        
    }
}
