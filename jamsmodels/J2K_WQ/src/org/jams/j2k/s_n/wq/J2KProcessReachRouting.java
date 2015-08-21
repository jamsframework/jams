/*
 * J2KProcessReachRouting.java
 * Created on 28. November 2005, 10:01
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

package org.jams.j2k.s_n.wq;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description"
        )
        public class J2KProcessReachRouting extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "The reach collection"
            )
            public JAMSEntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute length"
            )
            public JAMSDouble length;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute slope"
            )
            public JAMSDouble slope;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute width"
            )
            public JAMSDouble width;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute roughness"
            )
            public JAMSDouble roughness;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RD1 inflow"
            )
            public JAMSDouble inRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RD2 inflow"
            )
            public JAMSDouble inRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RG1 inflow"
            )
            public JAMSDouble inRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RG2 inflow"
            )
            public JAMSDouble inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar additional inflow",
            defaultValue= "0"
            )
            public JAMSDouble inAddIn;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RD1 outflow"
            )
            public JAMSDouble outRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RD2 outflow"
            )
            public JAMSDouble outRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RG1 outflow"
            )
            public JAMSDouble outRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RG2 outflow"
            )
            public JAMSDouble outRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar additional outflow",
            defaultValue= "0"
            )
            public JAMSDouble outAddIn;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar simulated Runoff"
            )
            public JAMSDouble simRunoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RD1 storage"
            )
            public JAMSDouble actRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RD2 storage"
            )
            public JAMSDouble actRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RG1 storage"
            )
            public JAMSDouble actRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RG2 storage"
            )
            public JAMSDouble actRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar additional inflow storage",
            defaultValue= "0"
            )
            public JAMSDouble actAddIn;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Channel storage"
            )
            public JAMSDouble channelStorage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "flow routing coefficient TA"
            )
            public JAMSDouble flowRouteTA;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Catchment outlet RD1 storage"
            )
            public JAMSDouble catchmentRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Catchment outlet RD2 storage"
            )
            public JAMSDouble catchmentRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Catchment outlet RG1 storage"
            )
            public JAMSDouble catchmentRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Catchment outlet RG2 storage"
            )
            public JAMSDouble catchmentRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Catchment additional input outlet storage",
            defaultValue= "0"
            )
            public JAMSDouble catchmentAddIn;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Catchment outlet RG2 storage"
            )
            public JAMSDouble catchmentSimRunoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "temporal resolution [d or h]"
            )
            public JAMSString tempRes;
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RG2 storage"
            )
            public JAMSDouble reachID;   
     
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        
        Attribute.Entity entity = entities.getCurrent();
        
        Attribute.Entity DestReach = (Attribute.Entity)entity.getObject("to_reach");
        if (DestReach.isEmpty()) {
            DestReach = null;
        }
        Attribute.Entity DestReservoir = null;
        
        try{
            DestReservoir = (Attribute.Entity)entity.getObject("to_reservoir");
        }catch(Attribute.Entity.NoSuchAttributeException e){
            DestReservoir = null;
        }
        
        double width = this.width.getValue();
        double slope = this.slope.getValue();
        double rough = this.roughness.getValue();
        double length = this.length.getValue();
        
        double RD1act = actRD1.getValue() + inRD1.getValue();
        double RD2act = actRD2.getValue() + inRD2.getValue();
        double RG1act = actRG1.getValue() + inRG1.getValue();
        double RG2act = actRG2.getValue() + inRG2.getValue();
        
     
        double addInAct = actAddIn.getValue() + this.inAddIn.getValue();
        
        inRD1.setValue(0);
        inRD2.setValue(0);
        inRG1.setValue(0);
        inRG2.setValue(0);
        
        inAddIn.setValue(0);
        
        actRD1.setValue(0);
        actRD2.setValue(0);
        actRG1.setValue(0);
        actRG2.setValue(0);
        
        actAddIn.setValue(0);
        
        double RD1DestIn = 0;
        double RD2DestIn = 0;
        double RG1DestIn = 0;
        double RG2DestIn = 0;
        double addInDestIn = 0;
        
        if(DestReach == null && DestReservoir == null){
            RD1DestIn = 0;//entity.getDouble(aNameCatchmentOutRD1.getValue());
            RD2DestIn = 0;//entity.getDouble(aNameCatchmentOutRD2.getValue());
            RG1DestIn = 0;//entity.getDouble(aNameCatchmentOutRG1.getValue());
            RG2DestIn = 0;//entity.getDouble(aNameCatchmentOutRG2.getValue());
            
            addInDestIn = 0;
        } 
        else if(DestReservoir != null){
            RD1DestIn = DestReservoir.getDouble("compRD1");
            RD2DestIn = DestReservoir.getDouble("compRD2");
            RG1DestIn = DestReservoir.getDouble("compRG1");
            RG2DestIn = DestReservoir.getDouble("compRG2");
        }
        else{
            RD1DestIn = DestReach.getDouble("inRD1");
            RD2DestIn = DestReach.getDouble("inRD2");
            RG1DestIn = DestReach.getDouble("inRG1");
            RG2DestIn = DestReach.getDouble("inRG2");
            
            try{
                addInDestIn = DestReach.getDouble("inAddIn");
            }catch(jams.data.Attribute.Entity.NoSuchAttributeException e){
                addInDestIn = 0;
            }
        }
        
        double q_act_tot = RD1act + RD2act + RG1act + RG2act + addInAct;
        
        //int ID = (int)entity.getDouble("ID");
        // System.out.getRuntime().println("Processing reach: " + ID);
        if(q_act_tot == 0){
            outRD1.setValue(0);
            outRD2.setValue(0);
            outRG1.setValue(0);
            outRG2.setValue(0);
            
            this.outAddIn.setValue(0);
            
            //nothing more to do here
            return;
        }
        
        //relative parts of the runoff components for later redistribution
        double RD1_part = RD1act / q_act_tot;
        double RD2_part = RD2act / q_act_tot;
        double RG1_part = RG1act / q_act_tot;
        double RG2_part = RG2act / q_act_tot;
        
        double addInPart = addInAct / q_act_tot;
        
        //calculation of flow velocity
        int sec_inTStep = 0;
        if(this.tempRes.getValue().equals("d"))
            sec_inTStep = 86400;
        else if(this.tempRes.getValue().equals("h"))
            sec_inTStep = 3600;
        double flow_veloc = this.calcFlowVelocity(q_act_tot, length, width, slope, rough, sec_inTStep);
        
        
        //recession coefficient
        double Rk = (flow_veloc / length) * this.flowRouteTA.getValue() * 3600;
        
        //the whole outflow
        double q_act_out;
        if(Rk > 0)
            q_act_out = q_act_tot * Math.exp(-1 / Rk);
        else
            q_act_out = 0;
        
        //the actual outflow from the reach
        double RD1out = q_act_out * RD1_part;
        double RD2out = q_act_out * RD2_part;
        double RG1out = q_act_out * RG1_part;
        double RG2out = q_act_out * RG2_part;
        
        double addInOut = q_act_out * addInPart;
        
        //transferring runoff from this reach to the next one or a reservoir
        RD1DestIn = RD1DestIn + RD1out;
        RD2DestIn = RD2DestIn + RD2out;
        RG1DestIn = RG1DestIn + RG1out;
        RG2DestIn = RG2DestIn + RG2out;
        
        addInDestIn = addInDestIn + addInOut;
        
        //reducing the actual storages
        RD1act = RD1act - q_act_out * RD1_part;
        RD2act = RD2act - q_act_out * RD2_part;
        RG1act = RG1act - q_act_out * RG1_part;
        RG2act = RG2act - q_act_out * RG2_part;
        
        addInAct = addInAct - q_act_out * addInPart;
        
        double channelStorage = RD1act + RD2act + RG1act + RG2act + addInAct;
        
        double cumOutflow = RD1out + RD2out + RG1out + RG2out + addInOut;
        /*if (reachID.getValue()==800)
        {System.out.println(RD1out);
        System.out.println(RD2out);
        System.out.println(RG1out);
        System.out.println(RG2out);
        }
        */
        
        simRunoff.setValue(cumOutflow);
        this.channelStorage.setValue(channelStorage);
        inRD1.setValue(0);
        inRD2.setValue(0);
        inRG1.setValue(0);
        inRG2.setValue(0);
        
        inAddIn.setValue(0);
        
        actRD1.setValue(RD1act);
        actRD2.setValue(RD2act);
        actRG1.setValue(RG1act);
        actRG2.setValue(RG2act);
        
        actAddIn.setValue(addInAct);
        
        outRD1.setValue(RD1out);
        outRD2.setValue(RD2out);
        outRG1.setValue(RG1out);
        outRG2.setValue(RG2out);
        
        outAddIn.setValue(addInOut);
        double verzoegerung;
        //reach
        if(DestReach != null && DestReservoir == null){
            DestReach.setDouble("inRD1",RD1DestIn);
            DestReach.setDouble("inRD2",RD2DestIn);
            DestReach.setDouble("inRG1",RG1DestIn);
            DestReach.setDouble("inRG2",RG2DestIn);
            
            DestReach.setDouble("inAddIn", addInDestIn);
            
        }
        //reservoir
        else if(DestReservoir != null){
            DestReservoir.setDouble("compRD1", RD1DestIn);
            DestReservoir.setDouble("compRD2", RD2DestIn);
            DestReservoir.setDouble("compRG1", RG1DestIn);
            DestReservoir.setDouble("compRG2", RG2DestIn);
        }
        //outlet
        else if(DestReach == null && DestReservoir == null){
            catchmentRD1.setValue(RD1out);
            catchmentRD2.setValue(RD2out);
            catchmentRG1.setValue(RG1out);
            catchmentRG2.setValue(RG2out);
            
            this.catchmentAddIn.setValue(addInOut);
            //neu verzoegerung
            
            
            
            catchmentSimRunoff.setValue(cumOutflow);
        }
        
    }
    
    public void cleanup() {
        
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
    public static double calcFlowVelocity(double q, double length, double width, double slope, double rough, int secondsOfTimeStep){
        double afv = 1;
        double veloc = 0;
        double WS = 0.35;

        /**
         *transfering liter/d to m?/s
         **/
        double q_m = q / (1000 * secondsOfTimeStep);
        double Volume = calcVolume(length,width,WS);
        double h = calcHydraulicDepth(Volume,length,width);
        double Across = width * h;
                //*rh = calcHydraulicRadius(afv, q_m, width);
        boolean cont = true;
        while(cont){
                veloc = q / Across;
                //*veloc = (rough) * Math.pow(rh, (2.0/3.0)) * Math.sqrt(slope);
            if((Math.abs(veloc - afv)) > 0.001){
                afv = veloc;
                WS = h;
                //*rh = calcHydraulicRadius(afv, q_m, width);
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
    /*public static double calcHydraulicRadius(double v, double q, double width){
        double A = (q / v);
        
        double rh = A / (width + 2*(A / width));
        
        return rh;
    }
     */
    public static double calcHydraulicDepth(double V, double length, double width){
        double Asurf = (length * width);
        double h = V / Asurf;

        return h;
     }
     public static double calcVolume(double l, double w, double ws){
        double Asurf = (l * w);
        double Volume = Asurf * ws;

        return Volume;
     }
}