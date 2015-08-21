/*
 * CalcLanduseStateVars.java
 * Created on 23. November 2005, 13:48
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
<component class="org.unijena.j2k.inputData.CalcLanduseStateVars" name="CalcLanduseStateVars">
    <jamsvar name="entity" provider="InitHRUContext" providervar="currentEntity"/>
    <jamsvar name="LAIArray" provider="InitHRUContext" providervar="currentEntity.LAIArray"/>
    <jamsvar name="effHArray" provider="InitHRUContext" providervar="currentEntity.effHArray"/>
    <jamsvar name="rsc0Array" provider="InitHRUContext" providervar="currentEntity.rsc0Array"/>
    <jamsvar name="elevation" provider="InitHRUContext" providervar="currentEntity.elevation"/>
</component>
*/        
package org.unijena.j2k.regionWK.AP2;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="CalcLanduseStateVars",
        author="Peter Krause",
        description="Calculates landuse state variables for each entity"
        )
        public class CalcLanduseStateVars extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current hru entity"
            )
            public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU statevar LAI values (366)"
            )
            public Attribute.DoubleArray LAIArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU statevar eff. Height values (366)"
            )
            public Attribute.DoubleArray effHArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Monthly stomata resistance values"
            )
            public Attribute.DoubleArray rsc0Array;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute name elevation"
            )
            public Attribute.Double elevation;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Monthly stomata resistance values"
            )
            public Attribute.DoubleArray etMonthFactorArray;
     
          
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temporal resolution [d | h | m]"
            )
            public Attribute.String tempRes;
    
    
    int[] monthMean = {15,45,74,105,135,166,196,227,258,288,319,349};
    int oldmonth = 0;
    double rsc0 = 0;
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
      
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        
        Attribute.Entity entity = entities.getCurrent();
        
        double[] lai_vals = null; 
        double[] effH_vals = null;
        
        if(this.tempRes == null || this.tempRes.getValue().equals("d") || this.tempRes.getValue().equals("h")){
            lai_vals = new double[366];
            effH_vals = new double[366];
        }
        else if(this.tempRes.getValue().equals("m")){
            lai_vals = new double[12];
            effH_vals = new double[12];
        }
        
        double HRUelevation = elevation.getValue();
        double[] lais = new double[4];
        String laiName = "LAI_d";
        for(int i = 0; i < 4; i++){
            int count = i+1;
            String loopName = laiName + count;
            lais[i] = entity.getDouble(loopName);
        }
        double[] effH = new double[4];
        String effName = "effHeight_d";
        for(int i = 0; i < 4; i++){
            int count = i+1;
            String loopName = effName + count;
            effH[i] = entity.getDouble(loopName);
        }
        if(this.tempRes == null || this.tempRes.getValue().equals("d") || this.tempRes.getValue().equals("h")){
            for(int j = 0; j < 366; j++){
                int julDay = j+1;
                lai_vals[j] = this.calcLAI(lais, HRUelevation, julDay);
                effH_vals[j] = this.calcEffHeight(effH, HRUelevation, julDay);
            }
        }
        else if(this.tempRes.getValue().equals("m")){
            for(int j = 0; j < 12; j++){
                int julDay = this.monthMean[j];
                lai_vals[j] = this.calcLAI(lais, HRUelevation, julDay);
                effH_vals[j] = this.calcEffHeight(effH, HRUelevation, julDay);
            }
        }
        
        LAIArray.setValue(lai_vals);
        effHArray.setValue(effH_vals);
        
        double[] rsc0 = new double[12];
        String rsc0Name = "RSC0_";
        for(int i = 0; i < 12; i++){
            int count = i+1;
            String loopName = rsc0Name + count;
            rsc0[i] = entity.getDouble(loopName);
        }
        rsc0Array.setValue(rsc0);
        
        
        double[] emf = new double[12];
        String emfName = "hf";
               
        for(int i = 0; i < 12; i++){
            int count = i+1;
            String loopName = emfName + count;
            emf[i] = entity.getDouble(loopName);
        }
        this.etMonthFactorArray.setValue(emf);

    }
    
    public void cleanup() {
        
    }
    
    /**
     * Calculates LAI for the specific date
     * @param lais - the four LAI values at specific dates
     * @param targetElevation - the elevation of the modelling unit
     * @param julDay - the julian day
     * @return the LAI value
     */
    private double calcLAI(double[] lais, double targetElevation, int julDay){
        int dTime = 0;
        double Lait1 = 0;
        double dLai = 0;
        int d1_400 = 110;
        int d2_400 = 150;
        int d3_400 = 250;
        int d4_400 = 280;
        
        //---------------------------------------
        // Calculation of Julian date of the specific points of LAI and eff. Height change
        //---------------------------------------
        int d1 = (int)(d1_400 + 0.025 * (targetElevation - 400));
        int d2 = (int)(d2_400 + 0.025 * (targetElevation - 400));
        int d3 = (int)(d3_400 - 0.025 * (targetElevation - 400));
        int d4 = (int)(d4_400 - 0.025 * (targetElevation - 400));
        
        double LAI = 0;
        
        if(julDay <= d1){
            LAI = lais[0];
        } else if((julDay > d1) && (julDay <= d2)){
            double LAI_1 = lais[0];
            double LAI_2 = lais[1];
            dTime = d2 - d1;
            dLai  = LAI_2 - LAI_1;
            Lait1 = dLai / dTime;
            LAI  = (Lait1 * (julDay - d1) + LAI_1);
        } else if(julDay > d2 && julDay <= d3){
            double LAI_2 = lais[1];
            double LAI_3 = lais[2];
            dTime = d3 - d2;
            dLai  = LAI_3 - LAI_2;
            Lait1 = dLai / dTime;
            LAI  = (Lait1 * (julDay - d2) + LAI_2);
        } else if(julDay > d3 && julDay <= d4){
            double LAI_3 = lais[2];
            double LAI_4 = lais[3];
            dTime = d4 - d3;
            dLai  = LAI_4 - LAI_3;
            Lait1 = dLai / dTime;
            LAI  = (Lait1 * (julDay - d3) + LAI_3);
        } else if(julDay > d4){
            double LAI_4 = lais[3];
            LAI  = LAI_4;
        }
        
        return LAI;
    }
    
    /**
     * Calculates effective Height for the specific date
     * @param effHeight - the four effective height values at specific dates
     * @param targetElevation - the elevation of the modelling unit
     * @param julDay - the julian day
     * @return the effHeight value
     */
    private double calcEffHeight(double[] effHeight, double targetElevation, int julDay){
        int dTime = 0;
        double effH_t1 = 0;
        double deffH = 0;
        int d1_400 = 110;
        int d2_400 = 150;
        int d3_400 = 250;
        int d4_400 = 280;
        
        //---------------------------------------
        // Calculation of Julian date of the specific points of LAI and eff. Height change
        //---------------------------------------
        int d1 = (int)(d1_400 + 0.025 * (targetElevation - 400));
        int d2 = (int)(d2_400 + 0.025 * (targetElevation - 400));
        int d3 = (int)(d3_400 - 0.025 * (targetElevation - 400));
        int d4 = (int)(d4_400 - 0.025 * (targetElevation - 400));
        
        double effH = 0;
        
        if(julDay <= d1){
            effH = effHeight[0];
        } else if((julDay > d1) && (julDay <= d2)){
            double effH_1 = effHeight[0];
            double effH_2 = effHeight[1];
            dTime = d2 - d1;
            deffH  = effH_2 - effH_1;
            effH_t1 = deffH / dTime;
            effH  = (effH_t1 * (julDay - d1) + effH_1);
        } else if(julDay > d2 && julDay <= d3){
            double effH_2 = effHeight[1];
            double effH_3 = effHeight[2];
            dTime = d3 - d2;
            deffH  = effH_3 - effH_2;
            effH_t1 = deffH / dTime;
            effH  = (effH_t1 * (julDay - d2) + effH_2);
        } else if(julDay > d3 && julDay <= d4){
            double effH_3 = effHeight[2];
            double effH_4 = effHeight[3];
            dTime = d4 - d3;
            deffH  = effH_4 - effH_3;
            effH_t1 = deffH / dTime;
            effH  = (effH_t1 * (julDay - d3) + effH_3);
        } else if(julDay > d4){
            double effH_4 = effHeight[3];
            effH  = effH_4;
        }
        
        return effH;
    }
}
