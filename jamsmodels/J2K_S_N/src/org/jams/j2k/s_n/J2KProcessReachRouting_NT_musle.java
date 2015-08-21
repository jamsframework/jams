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
        title="J2KProcessMultiReachRouting_NT_musle",
        author="c0krpe & Manfred Fink",
        description="Reach Routing of Water and Nitrogen to replace J2KProcessReachRouting including sediment and N-Total transport"
        )
        public class J2KProcessReachRouting_NT_musle extends JAMSComponent {
    
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
            public Attribute.Double SurfaceNabs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "(fast) InterflowN outflow in kgN"
            )
            public Attribute.Double InterflowNabs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "(slow) InterflowN outflow in kgN"
            )
            public Attribute.Double N_RG1_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "GoundwaterN outflow in kgN"
            )
            public Attribute.Double N_RG2_out;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD1_N (SurfaceN) storage in kgN"
            )
            public Attribute.Double ActRD1_N;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD2_N ((fast) InterflowN) storage in kgN"
            )
            public Attribute.Double ActRD2_N;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG1_N ((slow) InterflowN) storage in kgN"
            )
            public Attribute.Double ActRG1_N;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG2_N (GoundwaterN) storage in kgN"
            )
            public Attribute.Double ActRG2_N;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach Channel storage N in kgN"
            )
            public Attribute.Double ChannelStorage_N;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Simulated N Runoff in kgN"
            )
            public Attribute.Double SimRunoff_N;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "SurfaceN inflow in kgN"
            )
            public Attribute.Double SurfaceN_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "(fast) InterflowN inflow in kgN"
            )
            public Attribute.Double InterflowN_sum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "(slow) InterflowN inflow in kgN"
            )
            public Attribute.Double N_RG1_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "GoundwaterN inflow in kgN"
            )
            public Attribute.Double N_RG2_in;
    
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
            public Attribute.Double catchmentNRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet NRD2 storage"
            )
            public Attribute.Double catchmentNRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet NRG1 storage"
            )
            public Attribute.Double catchmentNRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet NRG2 storage"
            )
            public Attribute.Double catchmentNRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet sim Nitrogen runoff"
            )
            public Attribute.Double catchmentSimRunoffN;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet sim Nitrogen runoff"
            )
            public Attribute.Double catchmentSimNconc;
    
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
            public Attribute.Double DeepsinkN;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "amount of nitrogen in residues lost by deep sink in kg/d"
            )
            public Attribute.Double DeepsinkresN;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "amount of ammonium lost by deep sink in kg/d"
            )
            public Attribute.Double DeepsinkNH4;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "amount of aktiv organic nitrogen lost by deep sink in kg/d"
            )
            public Attribute.Double DeepsinkaktivN;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "amount of stable organic nitrogen lost by deep sink in kg/d"
            )
            public Attribute.Double DeepsinkstableN;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "K-Value for the riverbed in cm/d"
            )
            public Attribute.Double Ksink;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "reach statevar sediment inflow")
            public Attribute.Double insed;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "reach statevar sediment outflow")
            public Attribute.Double outsed;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet sediment storage",
            defaultValue= "0"
            )
            public Attribute.Double catchmentSed;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar sed storage"
            )
            public Attribute.Double actsed;

      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "NH4 in surface runoff added to Reach in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double NH4_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Residue-N in surface runoff added to Reach in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double residue_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Activ-N in surface runoff added to Reach in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double activ_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Stable-N in surface runoff added to Reach in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double stable_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "NH4 in surface runoff leaving the Reach in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double NH4_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Residue-N in surface runoff leaving the Reach in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double residue_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Activ-N in surface runoff leaving the Reach in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double activ_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Stable-N in surface runoff leaving the Reach in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double stable_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet NH4 storage"
            )
            public Attribute.Double catchmentNH4;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet N_residue storage"
            )
            public Attribute.Double catchmentN_residue;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet N_activ storage"
            )
            public Attribute.Double catchmentN_activ;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet N_stable storage"
            )
            public Attribute.Double catchmentN_stable;

     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "NH4 in surface runoff leaving the Reach in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double NH4_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Residue-N in surface runoff leaving the Reach in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double residue_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Activ-N in surface runoff leaving the Reach in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double activ_act;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Stable-N in surface runoff leaving the Reach in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double stable_act;


    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "ID")
    public Attribute.Double ID;
    
    
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
        double deepsinkNH4 = 0;
        double deepsinkN_residue = 0;
        double deepsinkN_activ = 0;
        double deepsinkN_stable = 0;
        double Larea = 0;
        double width = this.width.getValue();
        double slope = this.slope.getValue();
        double rough = this.roughness.getValue();
        double length = this.length.getValue();
        
        double RD1act = this.actRD1.getValue() + this.inRD1.getValue();
        double SEDact = actsed.getValue() + insed.getValue();
        
        double RD2act = this.actRD2.getValue() + this.inRD2.getValue();
        double RG1act = this.actRG1.getValue() + this.inRG1.getValue();
        double RG2act = this.actRG2.getValue() + this.inRG2.getValue();
        
        double RD1act_N = this.ActRD1_N.getValue() + this.SurfaceN_in.getValue();
        double RD2act_N = this.ActRD2_N.getValue() + this.InterflowN_sum.getValue();
        double RG1act_N = this.ActRG1_N.getValue() + this.N_RG1_in.getValue();
        double RG2act_N = this.ActRG2_N.getValue() + this.N_RG2_in.getValue();

        double act_NH4 = this.NH4_act.getValue() + this.NH4_in.getValue();
        double act_residue = this.residue_act.getValue() + this.residue_in.getValue();
        double act_activ = this.activ_act.getValue() + this.activ_in.getValue();
        double act_stable = this.stable_act.getValue() + this.stable_in.getValue();

        depth = 0;
        
        
        inRD1.setValue(0);
        insed.setValue(0);
        inRD2.setValue(0);
        inRG1.setValue(0);
        inRG2.setValue(0);
        
        actRD1.setValue(0);
        actsed.setValue(0);
        actRD2.setValue(0);
        actRG1.setValue(0);
        actRG2.setValue(0);
        
        SurfaceN_in.setValue(0);
        InterflowN_sum.setValue(0);
        N_RG1_in.setValue(0);
        N_RG2_in.setValue(0);
        NH4_in.setValue(0);
        residue_in.setValue(0);
        activ_in.setValue(0);
        stable_in.setValue(0);

        
        ActRD1_N.setValue(0);
        ActRD2_N.setValue(0);
        ActRG1_N.setValue(0);
        ActRG2_N.setValue(0);
        NH4_act.setValue(0);
        residue_act.setValue(0);
        activ_act.setValue(0);
        stable_act.setValue(0);

        
        double RD1DestIn, SedDestIn, RD2DestIn, RG1DestIn, RG2DestIn, RD1DestIn_N, RD2DestIn_N, RG1DestIn_N, RG2DestIn_N,NH4DestIn_N,residueDestIn_N,activDestIn_N,stableDestIn_N;
        if(DestReach.isEmpty()){
            RD1DestIn = 0;//entity.getDouble(aNameCatchmentOutRD1.getValue());
            SedDestIn = 0;
            RD2DestIn = 0;//entity.getDouble(aNameCatchmentOutRD2.getValue());
            RG1DestIn = 0;//entity.getDouble(aNameCatchmentOutRG1.getValue());
            RG2DestIn = 0;//entity.getDouble(aNameCatchmentOutRG2.getValue());
            
            RD1DestIn_N = 0;//entity.getDouble(aNameCatchmentOutRD1.getValue());
            RD2DestIn_N = 0;//entity.getDouble(aNameCatchmentOutRD2.getValue());
            RG1DestIn_N = 0;//entity.getDouble(aNameCatchmentOutRG1.getValue());
            RG2DestIn_N = 0;//entity.getDouble(aNameCatchmentOutRG2.getValue());
            NH4DestIn_N = 0;
            residueDestIn_N = 0;
            activDestIn_N = 0;
            stableDestIn_N = 0;


        } else{
            RD1DestIn = DestReach.getDouble("inRD1");
            SedDestIn = DestReach.getDouble("insed");
            RD2DestIn = DestReach.getDouble("inRD2");
            RG1DestIn = DestReach.getDouble("inRG1");
            RG2DestIn = DestReach.getDouble("inRG2");
            
            RD1DestIn_N = DestReach.getDouble("SurfaceN_in");
            RD2DestIn_N = DestReach.getDouble("InterflowN_sum");
            RG1DestIn_N = DestReach.getDouble("N_RG1_in");
            RG2DestIn_N = DestReach.getDouble("N_RG2_in");
            NH4DestIn_N = DestReach.getDouble("NH4_in");
            residueDestIn_N = DestReach.getDouble("residue_in");
            activDestIn_N = DestReach.getDouble("activ_in");
            stableDestIn_N = DestReach.getDouble("stable_in");
            
        }
        
        double q_act_tot = RD1act + RD2act + RG1act + RG2act;
        double q_act_tot_N = RD1act_N + RD2act_N + RG1act_N + RG2act_N;
        
        
        //int ID = (int)entity.getDouble("ID");
        // System.out.println("Processing reach: " + ID);
        if(q_act_tot == 0){
            
            outRD1.setValue(0);
            outsed.setValue(0);
            outRD2.setValue(0);
            outRG1.setValue(0);
            outRG2.setValue(0);
            
            SurfaceNabs.setValue(0);
            InterflowNabs.setValue(0);
            N_RG1_out.setValue(0);
            N_RG2_out.setValue(0);

            NH4_out.setValue(0);
            residue_out.setValue(0);
            activ_out.setValue(0);
            stable_out.setValue(0);
            
            //nothing more to do here
            return;
        }
        
        //relative parts of the runoff components for later redistribution
        double RD1_part = RD1act / q_act_tot;
        double Sed_part = SEDact / q_act_tot;
        double RD2_part = RD2act / q_act_tot;
        double RG1_part = RG1act / q_act_tot;
        double RG2_part = RG2act / q_act_tot;
        
        double RD1_part_N = 0;
        double RD2_part_N = 0;
        double RG1_part_N = 0;
        double RG2_part_N = 0;


        double NH4_conc = act_NH4 / q_act_tot;
        double residue_conc = act_residue / q_act_tot;
        double activ_conc = act_activ / q_act_tot;
        double stable_conc = act_stable / q_act_tot;

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
        double q_act_out_NH4 = q_act_out * NH4_conc;
        double q_act_out_residue = q_act_out * residue_conc;
        double q_act_out_activ = q_act_out * activ_conc;
        double q_act_out_stable = q_act_out * stable_conc;
        
        
        if (deepsink.getValue()==1.0){
            //calculation of deep sink
            //calculation of leckage area
            Larea = Math.pow(rh,2.0) * length;
            
            //calculation of deep sinks amount
            deepsinkW = Larea * Ksink.getValue() * 10;
            deepsinkN = deepsinkW * N_conc_tot;
            deepsinkNH4 = deepsinkW * NH4_conc;
            deepsinkN_residue = deepsinkW * residue_conc;
            deepsinkN_activ = deepsinkW * activ_conc;
            deepsinkN_stable = deepsinkW * stable_conc;
            deepsinkW = Math.min(deepsinkW,q_act_out);
            deepsinkN = Math.min(deepsinkN,q_act_out_N);
            deepsinkW = Math.max(deepsinkW,0);
            deepsinkN = Math.max(deepsinkN,0);
            deepsinkNH4 = Math.min(act_NH4, deepsinkNH4);
            deepsinkN_residue = Math.min(act_residue, deepsinkN_residue);
            deepsinkN_activ = Math.min(act_activ, deepsinkN_activ);
            deepsinkN_stable = Math.min(act_stable, deepsinkN_stable);
 
            
        }else{
          
            deepsinkW = 0;
            deepsinkN = 0;
            deepsinkNH4 = 0;
            deepsinkN_residue = 0;
            deepsinkN_activ = 0;
            deepsinkN_stable = 0;
            
        }
        
              
        DeepsinkW.setValue(deepsinkW);
        DeepsinkN.setValue(deepsinkN);
        DeepsinkNH4.setValue(deepsinkNH4);
        DeepsinkresN.setValue(deepsinkN_residue);
        DeepsinkaktivN.setValue(deepsinkN_activ);
        DeepsinkstableN.setValue(deepsinkN_stable);

        
        //the actual outflow from the reach
       
        
        double RD1outdeep = deepsinkW * RD1_part;
        double Sedoutdeep = deepsinkW * Sed_part;
        double RD2outdeep = deepsinkW * RD2_part;
        double RG1outdeep = deepsinkW * RG1_part;
        double RG2outdeep = deepsinkW * RG2_part;
        
        double RD1out_Ndeep = deepsinkN * RD1_part_N;
        double RD2out_Ndeep = deepsinkN * RD2_part_N;
        double RG1out_Ndeep = deepsinkN * RG1_part_N;
        double RG2out_Ndeep = deepsinkN * RG2_part_N;
        
        double RD1out = q_act_out * RD1_part - RD1outdeep;
        double Sedout = q_act_out * Sed_part - Sedoutdeep;
        double RD2out = q_act_out * RD2_part - RD2outdeep;
        double RG1out = q_act_out * RG1_part - RG1outdeep;
        double RG2out = q_act_out * RG2_part - RG2outdeep;
        
        double RD1out_N = q_act_out_N * RD1_part_N - RD1out_Ndeep;
        double RD2out_N = q_act_out_N * RD2_part_N - RD2out_Ndeep;
        double RG1out_N = q_act_out_N * RG1_part_N - RG1out_Ndeep;
        double RG2out_N = q_act_out_N * RG2_part_N - RG2out_Ndeep;
        double out_NH4 = q_act_out_NH4 - deepsinkNH4;
        double out_residue = q_act_out_residue - deepsinkN_residue;
        double out_activ = q_act_out_activ - deepsinkN_activ;
        double out_stable = q_act_out_stable - deepsinkN_stable;
        
        //transferring runoff from this reach to the next one
        RD1DestIn = RD1DestIn + RD1out;
        SedDestIn = SedDestIn + Sedout;
        //System.out.println(Sedout + " Sedout");
        //System.out.println(SedDestIn + " SedDestIn__");
        RD2DestIn = RD2DestIn + RD2out;
        RG1DestIn = RG1DestIn + RG1out;
        RG2DestIn = RG2DestIn + RG2out;
        
        RD1DestIn_N = RD1DestIn_N + RD1out_N;
        RD2DestIn_N = RD2DestIn_N + RD2out_N;
        RG1DestIn_N = RG1DestIn_N + RG1out_N;
        RG2DestIn_N = RG2DestIn_N + RG2out_N;
        
        NH4DestIn_N = NH4DestIn_N - out_NH4;
        residueDestIn_N = residueDestIn_N - out_residue;
        activDestIn_N = activDestIn_N - out_activ;
        stableDestIn_N = stableDestIn_N - out_stable;

        //reducing the actual storages
        RD1act = RD1act - RD1out - RD1outdeep;
        if (RD1act < 0) RD1act = 0;
        SEDact = SEDact - Sedout - Sedoutdeep;
        if (SEDact < 0) SEDact = 0;
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

        act_NH4 = act_NH4 - out_NH4 - deepsinkNH4;
        act_residue = act_residue - out_residue - deepsinkN_residue;
        act_activ = act_activ - out_activ - deepsinkN_activ;
        act_stable = act_stable - out_stable - deepsinkN_stable;

        
        double channelStorage = RD1act + RD2act + RG1act + RG2act;
        double channelStorage_N = RD1act_N + RD2act_N + RG1act_N + RG2act_N;
        
        double cumOutflow = RD1out + RD2out + RG1out + RG2out;
        double cumOutflow_N = RD1out_N + RD2out_N + RG1out_N + RG2out_N;
        
        double cumSed = Sedout;
        //System.out.println("Reach ID : " + ID.getValue() + " time : " + time.getTime().toString() + " cumSed " + cumSed );

        
        simRunoff.setValue(cumOutflow);
        SimRunoff_N.setValue(cumOutflow_N);
        this.channelStorage.setValue(channelStorage);
        ChannelStorage_N.setValue(channelStorage_N);
        
        inRD1.setValue(0);
        insed.setValue(0);
        inRD2.setValue(0);
        inRG1.setValue(0);
        inRG2.setValue(0);
        
        SurfaceN_in.setValue(0);
        InterflowN_sum.setValue(0);
        N_RG1_in.setValue(0);
        N_RG2_in.setValue(0);
        
        NH4_in.setValue(0);
        residue_in.setValue(0);
        activ_in.setValue(0);
        stable_in.setValue(0);
        
        actRD1.setValue(RD1act);
        actsed.setValue(SEDact);
        actRD2.setValue(RD2act);
        actRG1.setValue(RG1act);
        actRG2.setValue(RG2act);
        
        ActRD1_N.setValue(RD1act_N);
        ActRD2_N.setValue(RD2act_N);
        ActRG1_N.setValue(RG1act_N);
        ActRG2_N.setValue(RG2act_N);

        NH4_act.setValue(act_NH4);
        residue_act.setValue(act_residue);
        activ_act.setValue(act_activ);
        stable_act.setValue(act_stable);
        
        outRD1.setValue(RD1out);
        outsed.setValue(Sedout);
        outRD2.setValue(RD2out);
        outRG1.setValue(RG1out);
        outRG2.setValue(RG2out);
        
        SurfaceNabs.setValue(RD1out_N);
        InterflowNabs.setValue(RD2out_N);
        N_RG1_out.setValue(RG1out_N);
        N_RG2_out.setValue(RG2out_N);
        
        NH4_out.setValue(out_NH4);
        residue_out.setValue(out_residue);
        activ_out.setValue(out_activ);
        stable_out.setValue(out_stable);


        
/*        if(entity.getObject("to_reach") == null){
 
        System.out.println(RD1out + " RD1out " + RD2out + " RD2out "+ RG1out +" RG1out " + RG2out +" RG2out ");
 
        }*/
        if(!DestReach.isEmpty()){
            DestReach.setDouble("inRD1",RD1DestIn);
            DestReach.setDouble("insed",SedDestIn);
            DestReach.setDouble("inRD2",RD2DestIn);
            DestReach.setDouble("inRG1",RG1DestIn);
            DestReach.setDouble("inRG2",RG2DestIn);
            
            DestReach.setDouble("SurfaceN_in", RD1DestIn_N);
            DestReach.setDouble("InterflowN_sum", RD2DestIn_N);
            DestReach.setDouble("N_RG1_in", RG1DestIn_N);
            DestReach.setDouble("N_RG2_in", RG2DestIn_N);
            
            DestReach.setDouble("NH4_in", NH4DestIn_N);
            DestReach.setDouble("residue_in", residueDestIn_N);
            DestReach.setDouble("activ_in", activDestIn_N);
            DestReach.setDouble("stable_in", stableDestIn_N);


            
        }else{
            
            catchmentRD1.setValue(RD1out);
            catchmentSed.setValue(cumSed);
            //System.out.println(cumSed + " CCumOutSed");
            catchmentRD2.setValue(RD2out);
            catchmentRG1.setValue(RG1out);
            catchmentRG2.setValue(RG2out);
            catchmentSimRunoff.setValue(cumOutflow);
            
            catchmentNRD1.setValue(RD1out_N);
            catchmentNRD2.setValue(RD2out_N);
            catchmentNRG1.setValue(RG1out_N);
            catchmentNRG2.setValue(RG2out_N);
            catchmentSimRunoffN.setValue(cumOutflow_N);
            catchmentSimNconc.setValue((cumOutflow_N * 1000000)/cumOutflow);
            catchmentNH4.setValue(out_NH4);
            catchmentN_residue.setValue(out_residue);
            catchmentN_activ.setValue(out_activ);
            catchmentN_residue.setValue(out_stable);
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
