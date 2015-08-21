/*
 * J2KProcessRouting.java
 * Created on 28. November 2005, 09:21
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

package org.unijena.j2k.routing;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="J2KProcessRouting",
        author="Peter Krause",
        description="Passes the output of the entities as input to the respective reach or unit"
        )
        public class J2KProcessHorizonRouting_musle extends JAMSComponent {
    
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
            description = "HRU statevar RD1 inflow"
            )
            public Attribute.Double inRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar RD2 inflow"
            )
            public Attribute.DoubleArray inRD2_h;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar groundwater excess",
            defaultValue= "0"
            )
            public Attribute.Double inGWExcess;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar RG1 inflow"
            )
            public Attribute.Double inRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar RG2 inflow"
            )
            public Attribute.Double inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar RD1 outflow"
            )
            public Attribute.Double outRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar RD2 outflow"
            )
            public Attribute.DoubleArray outRD2_h;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar RG1 outflow"
            )
            public Attribute.Double outRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar RG2 outflow"
            )
            public Attribute.Double outRG2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar sediment inflow")
    public Attribute.Double insed;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar sediment outflow")
    public Attribute.Double outsed;
    
    double[][] fracOut;
    double[] percOut;
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
        
        double RD1out = outRD1.getValue();
        double sedout = outsed.getValue();
        double[] RD2out = outRD2_h.getValue();
        double RG1out = outRG1.getValue();
        double RG2out = outRG2.getValue();
        
        double RD2inReach = 0;
        
        
        if(!toPoly.isEmpty()){
            int id = (int)((Attribute.Double)entity.getObject("ID")).getValue();
            //System.out.getRuntime().println("to poly ID: " + entity.getObject("ID"));
            //if(id == 36)
            //    System.out.getRuntime().println("stop");
            double[] srcDepth = ((Attribute.DoubleArray)entity.getObject("depth_h")).getValue(); 
            double[] recDepth = ((Attribute.DoubleArray)toPoly.getObject("depth_h")).getValue();
            int srcHors = srcDepth.length;
            int recHors = recDepth.length;
            double[] RD2in = new double[recHors];
            this.calcParts(srcDepth, recDepth);
            
            //System.out.getRuntime().println("to Poly ID: " + toPoly.getDouble("ID"));
            double[] rdAr = ((Attribute.DoubleArray)toPoly.getObject("inRD2_h")).getValue();
            /*for(int i = 0; i < RD2out.length; i++){
                RD2in[i] = RD2in[i] + RD2out[i];
                RD2out[i] = 0;
            }*/
            double RG1in = toPoly.getDouble("inRG1");
            for(int j = 0; j < recHors; j++){
                RD2in[j] = rdAr[j];
                for(int i = 0; i < srcHors; i++){            
                    RD2in[j] = RD2in[j] + RD2out[i] * fracOut[i][j];
                    RG1in = RG1in + RD2out[i] * this.percOut[i];
                    //RD2out[i] -= RD2out[i] * fracOut[i][j];
                }
            }
            for(int i = 0; i < srcHors; i++){ 
                RD2out[i] = 0;
            }
            if(recHors == 0){
                System.out.println("RecHors is null at entity " + entity.getObject("ID"));
            }
            RD2in[recHors-1] += inGWExcess.getValue();
            double RD1in = toPoly.getDouble("inRD1");
            double sedin = toPoly.getDouble("insed");            
            double RG2in = toPoly.getDouble("inRG2");
            
            RD1in = RD1in + RD1out;
            sedin = sedin + sedout;
            RG1in = RG1in + RG1out;
            RG2in = RG2in + RG2out;
            
            RD1out = 0;
            sedout = 0;
            RG1out = 0;
            RG2out = 0;
            
            outRD1.setValue(0);
            outsed.setValue(0);
            outRD2_h.setValue(RD2out);
            outRG1.setValue(0);
            outRG2.setValue(0);
            inGWExcess.setValue(0);
            
            Attribute.DoubleArray rdA = (Attribute.DoubleArray)toPoly.getObject("inRD2_h");
            rdA.setValue(RD2in);
            toPoly.setDouble("inRD1", RD1in);
            toPoly.setDouble("insed", sedin);
            toPoly.setObject("inRD2_h", rdA);
            toPoly.setDouble("inRG1", RG1in);
            toPoly.setDouble("inRG2", RG2in);
        } else if(!toReach.isEmpty()){
            double RD1in = toReach.getDouble("inRD1");
            
            double sedin = toReach.getDouble("insed");
            
            RD2inReach = toReach.getDouble("inRD2");
            for(int h = 0; h < RD2out.length; h++){
                RD2inReach = RD2inReach + RD2out[h];
                RD2out[h] = 0;
            }
            RD2inReach += inGWExcess.getValue();
            double RG1in = toReach.getDouble("inRG1");
            double RG2in = toReach.getDouble("inRG2");
            
            RD1in = RD1in + RD1out;
            sedin = sedin + sedout;
            RG1in = RG1in + RG1out;
            RG2in = RG2in + RG2out;
            
            RD1out = 0;
            sedout = 0;
            RG1out = 0;
            RG2out = 0;
            
            outRD1.setValue(RD1out);
            toReach.setDouble("inRD1", RD1in);
            outsed.setValue(sedout);
            toReach.setDouble("insed", sedin);
            outRD2_h.setValue(RD2out);
            toReach.setDouble("inRD2", RD2inReach);
            outRG1.setValue(RG1out);
            toReach.setDouble("inRG1", RG1in);
            outRG2.setValue(RG2out);
            toReach.setDouble("inRG2", RG2in);
            inGWExcess.setValue(0);
            
                
            
        } else{
            getModel().getRuntime().println("Current entity ID: " + entity.getDouble("ID") + " has no receiver.");
        }
        
    }
    
    public void cleanup() {
        
    }
    
    private void calcParts(double[] depthSrc, double[] depthRec){
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
            //System.out.getRuntime().println("Src --> up: "+up+", low: "+low);
            
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
            //System.out.getRuntime().println("Rec --> up: "+up+", low: "+low);
        }

        
        fracOut = new double[depthSrc.length][depthRec.length];
        percOut = new double[depthSrc.length];
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
            percOut[i] = 1.0 - sumFrac;
        }
    }
}
