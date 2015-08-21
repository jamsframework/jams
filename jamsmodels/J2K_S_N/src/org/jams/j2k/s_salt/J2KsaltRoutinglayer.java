/*
 * J2KsaltRoutinglayer.java
 * Created on 27. February 2007, 11:20
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe & Manfred Fink
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

package org.jams.j2k.s_salt;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause & Manfred Fink
 */
@JAMSComponentDescription(
        title="J2KsaltRoutinglayer",
        author="Peter Krause & Manfred Fink",
        description="Passes the NaCl output of the entities as NaCl input to the respective reach or unit"
        )
        public class J2KsaltRoutinglayer extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current hru entity"
            )
            public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of reach objects"
            )
            public Attribute.EntityCollection reaches;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 NaCl inflow in kgNaCl"
            )
            public Attribute.Double SurfaceNaCl_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 NaCl inflow in kgNaCl"
            )
            public Attribute.DoubleArray InterflowNaCl_in;
    
/*    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 N inflow in kgN lumped"
            )
            public Attribute.Double InterflowN_sum;
 */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 NaCl inflow in kgNaCl"
            )
            public Attribute.Double NaCl_RG1_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 NaCl inflow in kgNaCl"
            )
            public Attribute.Double NaCl_RG2_in;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 NaCl outflow in kgNaCl"
            )
            public Attribute.Double SurfaceNaClabs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 NaCl outflow in kgNaCl"
            )
            public Attribute.DoubleArray InterflowNaClabs ;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 N outflow in kgNaCl"
            )
            public Attribute.Double NaCl_RG1_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 N outflow in kgNaCl"
            )
            public Attribute.Double NaCl_RG2_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "gwExcess"
            )
            public Attribute.Double NaClExcess;
    
    double[][] fracOut;
    double[] percNOut;
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        Attribute.Entity entity = entities.getCurrent();
        //receiving polygon
        Attribute.Entity toPoly = (Attribute.Entity) entity.getObject("to_poly");
        //receiving reach
        Attribute.Entity toReach = (Attribute.Entity) entity.getObject("to_reach");
        
        
        
        double NRD1out = SurfaceNaClabs.getValue();
        double[] NRD2out_h = InterflowNaClabs.getValue();
        double NRG1out = NaCl_RG1_out.getValue();
        double NRG2out = NaCl_RG2_out.getValue();
        
        double reachNRD2in = 0;
//        System.out.println("NRD2out: " + NRD2out);
        
       if((toPoly != null && toPoly.getId() != -1)){
            double[] srcDepth = ((Attribute.DoubleArray)entity.getObject("depth_h")).getValue();
            double[] recDepth = ((Attribute.DoubleArray)toPoly.getObject("depth_h")).getValue();
            int srcHors = srcDepth.length;
            int recHors = recDepth.length;
            double[] NRD2in_h = new double[recHors];
            //this.calcParts(srcDepth, recDepth);
            
            double NRD1in = toPoly.getDouble("SurfaceNaCl_in");
            double[] rdArN = ((Attribute.DoubleArray)toPoly.getObject("InterflowNaCl_in")).getValue();
/*
            Object o = toPoly.getObject("InterflowN_in");
 
            double[] rdArN = null;
            try {
                rdArN = ((Attribute.DoubleArray)o).getValue();
 
            } catch (Exception e) {
                System.out.println("MIST");
            }*/
            double NRG1in = toPoly.getDouble("NaCl_RG1_in");
            double NRG2in = toPoly.getDouble("NaCl_RG2_in");
//            double NRG1in = 0;
//            double NRG2in = 0;
            for(int j = 0; j < recHors; j++){
                NRD2in_h[j] = rdArN[j];
                for(int i = 0; i < srcHors; i++){
                    //NRD2in_h[j] = NRD2in_h[j] + NRD2out_h[i] * fracOut[i][j];
                    NRD2in_h[j] = NRD2in_h[j] + (NRD2out_h[i] / recHors);
                    //NRG1in = NRG1in + NRD2out_h[i] * this.percNOut[i];
                    //RD2out[i] -= RD2out[i] * fracOut[i][j];
                }
            }
            
            
            for(int i = 0; i < srcHors; i++){
                NRD2out_h[i] = 0;
            }
            NRD2in_h[recHors-1] += NaClExcess.getValue();
            double RD1in = toPoly.getDouble("inRD1");
            
            
            double RG2in = toPoly.getDouble("inRG2");
            
            NRD1in = NRD1in + NRD1out;
            NRG1in = NRG1in + NRG1out;
            NRG2in = NRG2in + NRG2out;
            
            NRD1out = 0;
            NRG1out = 0;
            NRG2out = 0;
            
            SurfaceNaClabs.setValue(0);
            InterflowNaClabs.setValue(NRD2out_h);
            NaCl_RG1_out.setValue(0);
            NaCl_RG2_out.setValue(0);
            NaClExcess.setValue(0);
            
            Attribute.DoubleArray rdAN = (Attribute.DoubleArray)toPoly.getObject("InterflowNaCl_in");
            rdAN.setValue(NRD2in_h);
            toPoly.setDouble("SurfaceNaCl_in",NRD1in);
            toPoly.setObject("InterflowNaCl_in", rdAN);
            toPoly.setDouble("NaCl_RG1_in", NRG1in);
            toPoly.setDouble("NaCl_RG2_in", NRG2in);
        } else if(toReach != null){
            
            double NRD1in = toReach.getDouble("SurfaceNaCl_in");
/*            
            try {
                reachNRD2in = toReach.getDouble("InterflowN_sum");
                
            } catch (Exception e) {
                System.out.println("MIST");
            }
*/            
            
            reachNRD2in = toReach.getDouble("InterflowNaCl_sum");
            
            double NRG1in = toReach.getDouble("NaCl_RG1_in");
            double NRG2in = toReach.getDouble("NaCl_RG2_in");
            
            for(int h = 0; h < NRD2out_h.length; h++){
                reachNRD2in = reachNRD2in + NRD2out_h[h];
                NRD2out_h[h] = 0;
            }
            NRD1in = NRD1in + NRD1out;
            reachNRD2in += NaClExcess.getValue();
            NRG1in = NRG1in + NRG1out;
            NRG2in = NRG2in + NRG2out;
            
            NRD1out = 0;
            
            NRG1out = 0;
            NRG2out = 0;
            
            
            NaClExcess.setValue(0);
            SurfaceNaClabs.setValue(0);
            toReach.setDouble("SurfaceNaCl_in", NRD1in);
            InterflowNaClabs.setValue(NRD2out_h);
            toReach.setDouble("InterflowNaCl_sum", reachNRD2in);
            NaCl_RG1_out.setValue(NRG1out);
            toReach.setDouble("NaCl_RG1_in", NRG1in);
            NaCl_RG2_out.setValue(NRG2out);
            toReach.setDouble("NaCl_RG2_in", NRG2in);
            
        } else{
            System.out.println("Current entity ID: " + entity.getInt("ID") + " has no receiver.");
        }
        
    }
    public void cleanup() {
        
    }
    
  /*  private void calcParts(double[] depthSrc, double[] depthRec){
        int srcHorizons = depthSrc.length;
        int recHorizons = depthRec.length;
        
        double[] upBoundSrc = new double[srcHorizons];
        double[] lowBoundSrc = new double[srcHorizons];
        double low = 0;
        double up = 0;
        for(int i = 0; i < srcHorizons; i++){
            low += depthSrc[i];
            up = low - depthSrc[i];
            upBoundSrc[i] = up;
            lowBoundSrc[i] = low;
            //System.out.println("Src --> up: "+up+", low: "+low);
            
        }
        double[] upBoundRec = new double[recHorizons];
        double[] lowBoundRec = new double[recHorizons];
        low = 0;
        up = 0;
        for(int i = 0; i < recHorizons; i++){
            low += depthRec[i];
            up = low - depthRec[i];
            upBoundRec[i] = up;
            lowBoundRec[i] = low;
            //System.out.println("Rec --> up: "+up+", low: "+low);
        }
        
      
        fracOut = new double[depthSrc.length][depthRec.length];
        percNOut = new double[depthSrc.length];
        for(int i = 0; i < depthSrc.length; i++){
            double sumFrac = 0;
            for(int j = 0; j < depthRec.length; j++){
                if((lowBoundSrc[i] > upBoundRec[j]) && (upBoundSrc[i] < lowBoundRec[j])){
                    double relDepth = Math.min(lowBoundSrc[i], lowBoundRec[j]) - Math.max(upBoundSrc[i], upBoundRec[j]);
                    double fracDepth = relDepth / depthSrc[i];
                    sumFrac += fracDepth;
                    fracOut[i][j] = fracDepth;
                }
            }
            percNOut[i] = 1.0 - sumFrac;
        }
    }*/
}
