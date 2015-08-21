/*
 * J2KProcessReachRouting_N.java
 * Created on 28. November 2005, 10:01
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

package org.jams.j2k.s_n;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe & Manfred Fink
 */
@JAMSComponentDescription(
        title="J2KProcessReachRouting_NaCl",
        author="c0krpe & Manfred Fink",
        description="Reach Routing of Water and NaCl to replace J2KProcessReachRouting"
        )
        public class J2KProcessReachRouting_NaCl extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection"
            )
            public Attribute.EntityCollection entities;
    
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute length"
            )
            public Attribute.Double length;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute slope"
            )
            public Attribute.Double slope;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute width"
            )
            public Attribute.Double width;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute roughness"
            )
            public Attribute.Double roughness;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD1 inflow"
            )
            public Attribute.Double inRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD2 inflow"
            )
            public Attribute.Double inRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG1 inflow"
            )
            public Attribute.Double inRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG2 inflow"
            )
            public Attribute.Double inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar RD1 outflow"
            )
            public Attribute.Double outRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar RD2 outflow"
            )
            public Attribute.Double outRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar RG1 outflow"
            )
            public Attribute.Double outRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar RG2 outflow"
            )
            public Attribute.Double outRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar simulated Runoff"
            )
            public Attribute.Double simRunoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD1 storage"
            )
            public Attribute.Double actRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD2 storage"
            )
            public Attribute.Double actRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG1 storage"
            )
            public Attribute.Double actRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG2 storage"
            )
            public Attribute.Double actRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Channel storage"
            )
            public Attribute.Double channelStorage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "flow routing coefficient TA"
            )
            public Attribute.Double flowRouteTA;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "SurfaceN outflow in kgN"
            )
            public Attribute.Double SurfaceNaClabs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "(fast) InterflowN outflow in kgN"
            )
            public Attribute.Double InterflowNaClabs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "(slow) InterflowN outflow in kgN"
            )
            public Attribute.Double NaCl_RG1_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "GoundwaterN outflow in kgN"
            )
            public Attribute.Double NaCl_RG2_out;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD1_N (SurfaceN) storage in kgN"
            )
            public Attribute.Double ActRD1_NaCl;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD2_N ((fast) InterflowN) storage in kgN"
            )
            public Attribute.Double ActRD2_NaCl;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG1_N ((slow) InterflowN) storage in kgN"
            )
            public Attribute.Double ActRG1_NaCl;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG2_N (GoundwaterN) storage in kgN"
            )
            public Attribute.Double ActRG2_NaCl;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach Channel storage N in kgN"
            )
            public Attribute.Double ChannelStorage_NaCl;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Simulated N Runoff in kgN"
            )
            public Attribute.Double SimRunoff_NaCl;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "SurfaceN inflow in kgN"
            )
            public Attribute.Double SurfaceNaCl_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "(fast) InterflowN inflow in kgN"
            )
            public Attribute.Double InterflowNaCl_sum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "(slow) InterflowN inflow in kgN"
            )
            public Attribute.Double NaCl_RG1_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "GoundwaterN inflow in kgN"
            )
            public Attribute.Double NaCl_RG2_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
            )
            public Attribute.Calendar time;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RD1 storage"
            )
            public Attribute.Double catchmentRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RD2 storage"
            )
            public Attribute.Double catchmentRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RG1 storage"
            )
            public Attribute.Double catchmentRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RG2 storage"
            )
            public Attribute.Double catchmentRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet sim runoff"
            )
            public Attribute.Double catchmentSimRunoff;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet NRD1 storage"
            )
            public Attribute.Double catchmentNaClRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet NRD2 storage"
            )
            public Attribute.Double catchmentNaClRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet NRG1 storage"
            )
            public Attribute.Double catchmentNaClRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet NRG2 storage"
            )
            public Attribute.Double catchmentNaClRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet sim Nitrogen runoff"
            )
            public Attribute.Double catchmentSimRunoffNaCl;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet sim Nitrogen runoff"
            )
            public Attribute.Double catchmentSimNaClconc;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "switch whether deep sink is allowed or not",
            defaultValue="0"
            )
            public Attribute.Double deepsink;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "amount of water lost by deep sink in l/d"
            )
            public Attribute.Double DeepsinkW;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "amount of nitrogen lost by deep sink in kg/d"
            )
            public Attribute.Double DeepsinkNaCl;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "K-Value for the riverbed in cm/d"
            )
            public Attribute.Double Ksink;
    
    
    private double depth; 
    private double rh;
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        Attribute.Entity entity = entities.getCurrent();
        Attribute.Entity DestReach = (Attribute.Entity) entity.getObject("to_reach");
//        int datumjul = time.get(time.DAY_OF_YEAR);
        
        double deepsinkW = 0;
        double deepsinkN = 0;
        double Larea = 0;
        double width = this.width.getValue();
        double slope = this.slope.getValue();
        double rough = this.roughness.getValue();
        double length = this.length.getValue();
        
        double RD1act = this.actRD1.getValue() + this.inRD1.getValue();
        double RD2act = this.actRD2.getValue() + this.inRD2.getValue();
        double RG1act = this.actRG1.getValue() + this.inRG1.getValue();
        double RG2act = this.actRG2.getValue() + this.inRG2.getValue();
        
        double RD1act_N = this.ActRD1_NaCl.getValue() + this.SurfaceNaCl_in.getValue();
        double RD2act_N = this.ActRD2_NaCl.getValue() + this.InterflowNaCl_sum.getValue();
        double RG1act_N = this.ActRG1_NaCl.getValue() + this.NaCl_RG1_in.getValue();
        double RG2act_N = this.ActRG2_NaCl.getValue() + this.NaCl_RG2_in.getValue();
        depth = 0;
        
        
        inRD1.setValue(0);
        inRD2.setValue(0);
        inRG1.setValue(0);
        inRG2.setValue(0);
        
        actRD1.setValue(0);
        actRD2.setValue(0);
        actRG1.setValue(0);
        actRG2.setValue(0);
        
        SurfaceNaCl_in.setValue(0);
        InterflowNaCl_sum.setValue(0);
        NaCl_RG1_in.setValue(0);
        NaCl_RG2_in.setValue(0);
        
        ActRD1_NaCl.setValue(0);
        ActRD2_NaCl.setValue(0);
        ActRG1_NaCl.setValue(0);
        ActRG2_NaCl.setValue(0);
        
        double RD1DestIn, RD2DestIn, RG1DestIn, RG2DestIn, RD1DestIn_N, RD2DestIn_N, RG1DestIn_N, RG2DestIn_N;
        if(DestReach.isEmpty()){
            RD1DestIn = 0;//entity.getDouble(aNameCatchmentOutRD1.getValue());
            RD2DestIn = 0;//entity.getDouble(aNameCatchmentOutRD2.getValue());
            RG1DestIn = 0;//entity.getDouble(aNameCatchmentOutRG1.getValue());
            RG2DestIn = 0;//entity.getDouble(aNameCatchmentOutRG2.getValue());
            
            RD1DestIn_N = 0;//entity.getDouble(aNameCatchmentOutRD1.getValue());
            RD2DestIn_N = 0;//entity.getDouble(aNameCatchmentOutRD2.getValue());
            RG1DestIn_N = 0;//entity.getDouble(aNameCatchmentOutRG1.getValue());
            RG2DestIn_N = 0;//entity.getDouble(aNameCatchmentOutRG2.getValue());
            
        } else{
            RD1DestIn = DestReach.getDouble("inRD1");
            RD2DestIn = DestReach.getDouble("inRD2");
            RG1DestIn = DestReach.getDouble("inRG1");
            RG2DestIn = DestReach.getDouble("inRG2");
            
            RD1DestIn_N = DestReach.getDouble("SurfaceNaCl_in");
            RD2DestIn_N = DestReach.getDouble("InterflowNaCl_sum");
            RG1DestIn_N = DestReach.getDouble("NaCl_RG1_in");
            RG2DestIn_N = DestReach.getDouble("NaCl_RG2_in");
            
        }
        
        double q_act_tot = RD1act + RD2act + RG1act + RG2act;
        double q_act_tot_N = RD1act_N + RD2act_N + RG1act_N + RG2act_N;
        
        
        //int ID = (int)entity.getDouble("ID");
        // System.out.println("Processing reach: " + ID);
        if(q_act_tot == 0){
            
            outRD1.setValue(0);
            outRD2.setValue(0);
            outRG1.setValue(0);
            outRG2.setValue(0);
            
            SurfaceNaClabs.setValue(0);
            InterflowNaClabs.setValue(0);
            NaCl_RG1_out.setValue(0);
            NaCl_RG2_out.setValue(0);
            
            //nothing more to do here
            return;
        }
        
        //relative parts of the runoff components for later redistribution
        double RD1_part = RD1act / q_act_tot;
        double RD2_part = RD2act / q_act_tot;
        double RG1_part = RG1act / q_act_tot;
        double RG2_part = RG2act / q_act_tot;
        
        double RD1_part_N = 0;
        double RD2_part_N = 0;
        double RG1_part_N = 0;
        double RG2_part_N = 0;
        
        if(q_act_tot_N == 0){
            
            
            
        } else{
            
            RD1_part_N = RD1act_N / q_act_tot_N;
            RD2_part_N = RD2act_N / q_act_tot_N;
            RG1_part_N = RG1act_N / q_act_tot_N;
            RG2_part_N = RG2act_N / q_act_tot_N;
            
            //calculation of N-Concentration with q_act_tot and q_act_tot_N
            
            
        }
        double N_conc_tot = q_act_tot_N / q_act_tot;
        //calculation of flow velocity
        double flow_veloc = this.calcFlowVelocity(q_act_tot, width, slope, rough, 86400);
        
        //recession coefficient
        double Rk = (flow_veloc / length) * this.flowRouteTA.getValue() * 3600;
        
        //the whole outflow
        double q_act_out;
        
        if(Rk > 0)
            q_act_out = q_act_tot * Math.exp(-1 / Rk);
        else
            q_act_out = 0;
        
        
        
        
        //calculation of N-content in q_act_out
        
        double q_act_out_N = q_act_out * N_conc_tot;
        
        
        
        if (deepsink.getValue()==1.0){
            //calculation of deep sink
            //calculation of leckage area
            Larea = Math.pow(rh,2.0) * length;
            
            //calculation of deep sinks amount
            deepsinkW = Larea * Ksink.getValue() * 10;
            deepsinkN = deepsinkW * N_conc_tot;
            
            deepsinkW = Math.min(deepsinkW,q_act_out);
            deepsinkN = Math.min(deepsinkN,q_act_out_N);
            deepsinkW = Math.max(deepsinkW,0);
            deepsinkN = Math.max(deepsinkN,0);         
 
            
        }else{
          
            deepsinkW = 0;
            deepsinkN = 0;
            
        }
        
              
        DeepsinkW.setValue(deepsinkW);
        DeepsinkNaCl.setValue(deepsinkN);
        
        //the actual outflow from the reach
       
        
        double RD1outdeep = deepsinkW * RD1_part;
        double RD2outdeep = deepsinkW * RD2_part;
        double RG1outdeep = deepsinkW * RG1_part;
        double RG2outdeep = deepsinkW * RG2_part;
        
        double RD1out_Ndeep = deepsinkN * RD1_part_N;
        double RD2out_Ndeep = deepsinkN * RD2_part_N;
        double RG1out_Ndeep = deepsinkN * RG1_part_N;
        double RG2out_Ndeep = deepsinkN * RG2_part_N;
        
        double RD1out = q_act_out * RD1_part - RD1outdeep;
        double RD2out = q_act_out * RD2_part - RD2outdeep;
        double RG1out = q_act_out * RG1_part - RG1outdeep;
        double RG2out = q_act_out * RG2_part - RG2outdeep;
        
        double RD1out_N = q_act_out_N * RD1_part_N - RD1out_Ndeep;
        double RD2out_N = q_act_out_N * RD2_part_N - RD2out_Ndeep;
        double RG1out_N = q_act_out_N * RG1_part_N - RG1out_Ndeep;
        double RG2out_N = q_act_out_N * RG2_part_N - RG2out_Ndeep;
        
        //transferring runoff from this reach to the next one
        RD1DestIn = RD1DestIn + RD1out;
        RD2DestIn = RD2DestIn + RD2out;
        RG1DestIn = RG1DestIn + RG1out;
        RG2DestIn = RG2DestIn + RG2out;
        
        RD1DestIn_N = RD1DestIn_N + RD1out_N;
        RD2DestIn_N = RD2DestIn_N + RD2out_N;
        RG1DestIn_N = RG1DestIn_N + RG1out_N;
        RG2DestIn_N = RG2DestIn_N + RG2out_N;
        
        //reducing the actual storages
        RD1act = RD1act - RD1out - RD1outdeep;
        if (RD1act < 0) RD1act = 0;
        RD2act = RD2act - RD2out - RD2outdeep;
        if (RD2act < 0) RD2act = 0;
        RG1act = RG1act - RG1out - RG1outdeep;
        if (RG1act < 0) RG1act = 0;
        RG2act = RG2act - RG2out - RG1outdeep;
        if (RG2act < 0) RG2act = 0;
        
        RD1act_N = RD1act_N - RD1out_N - RD1out_Ndeep;
        if (RD1act_N < 0) RD1act_N = 0;
        RD2act_N = RD2act_N - RD2out_N - RD2out_Ndeep;
        if (RD2act_N < 0) RD2act_N = 0;
        RG1act_N = RG1act_N - RG1out_N - RG1out_Ndeep;
        if (RG1act_N < 0) RG1act_N = 0;
        RG2act_N = RG2act_N - RG2out_N - RG2out_Ndeep;
        if (RG2act_N < 0) RG2act_N = 0;
        
        double channelStorage = RD1act + RD2act + RG1act + RG2act;
        double channelStorage_N = RD1act_N + RD2act_N + RG1act_N + RG2act_N;
        
        double cumOutflow = RD1out + RD2out + RG1out + RG2out;
        double cumOutflow_N = RD1out_N + RD2out_N + RG1out_N + RG2out_N;
        
        
        
        simRunoff.setValue(cumOutflow);
        SimRunoff_NaCl.setValue(cumOutflow_N);
        this.channelStorage.setValue(channelStorage);
        ChannelStorage_NaCl.setValue(channelStorage_N);
        
        inRD1.setValue(0);
        inRD2.setValue(0);
        inRG1.setValue(0);
        inRG2.setValue(0);
        
        SurfaceNaCl_in.setValue(0);
        InterflowNaCl_sum.setValue(0);
        NaCl_RG1_in.setValue(0);
        NaCl_RG2_in.setValue(0);
        
        actRD1.setValue(RD1act);
        actRD2.setValue(RD2act);
        actRG1.setValue(RG1act);
        actRG2.setValue(RG2act);
        
        ActRD1_NaCl.setValue(RD1act_N);
        ActRD2_NaCl.setValue(RD2act_N);
        ActRG1_NaCl.setValue(RG1act_N);
        ActRG2_NaCl.setValue(RG2act_N);
        
        outRD1.setValue(RD1out);
        outRD2.setValue(RD2out);
        outRG1.setValue(RG1out);
        outRG2.setValue(RG2out);
        
        SurfaceNaClabs.setValue(RD1out_N);
        InterflowNaClabs.setValue(RD2out_N);
        NaCl_RG1_out.setValue(RG1out_N);
        NaCl_RG2_out.setValue(RG2out_N);
        
/*        if(entity.getObject("to_reach") == null){
 
        System.out.println(RD1out + " RD1out " + RD2out + " RD2out "+ RG1out +" RG1out " + RG2out +" RG2out ");
 
        }*/
        if(!DestReach.isEmpty()){
            DestReach.setDouble("inRD1",RD1DestIn);
            DestReach.setDouble("inRD2",RD2DestIn);
            DestReach.setDouble("inRG1",RG1DestIn);
            DestReach.setDouble("inRG2",RG2DestIn);
            
            DestReach.setDouble("SurfaceNaCl_in", RD1DestIn_N);
            DestReach.setDouble("InterflowNaCl_sum", RD2DestIn_N);
            DestReach.setDouble("NaCl_RG1_in", RG1DestIn_N);
            DestReach.setDouble("NaCl_RG2_in", RG2DestIn_N);
            
        }else{
            
            catchmentRD1.setValue(RD1out);
            catchmentRD2.setValue(RD2out);
            catchmentRG1.setValue(RG1out);
            catchmentRG2.setValue(RG2out);
            catchmentSimRunoff.setValue(cumOutflow);
            
            catchmentNaClRD1.setValue(RD1out_N);
            catchmentNaClRD2.setValue(RD2out_N);
            catchmentNaClRG1.setValue(RG1out_N);
            catchmentNaClRG2.setValue(RG2out_N);
            catchmentSimRunoffNaCl.setValue(cumOutflow_N);
            catchmentSimNaClconc.setValue((cumOutflow_N * 1000000)/cumOutflow);
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
    public double calcFlowVelocity(double q, double width, double slope, double rough, int secondsOfTimeStep){
        double afv = 1;
        double veloc = 0;
        
        /**
         *transfering liter/d to m³/s
         **/
        double q_m = q / (1000 * secondsOfTimeStep);
        this.rh = calcHydraulicRadius(afv, q_m, width);
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
