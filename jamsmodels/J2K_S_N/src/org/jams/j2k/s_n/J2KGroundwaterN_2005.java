/*
 * J2KGroundwaterN_2005.java
 * Created on 29. May 2007, 15:39
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena, c8fima
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
 * @author Manfred Fink
 */
@JAMSComponentDescription(
        title="J2KGroundwaterN",
        author="Manfred Fink",
        description="Groundwater N module with two different N-Pools with delay and decay processes after SWAT2005"
        )
        public class J2KGroundwaterN_2005 extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual RG1 storage"
            )
            public Attribute.Double actRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual RG2 storage"
            )
            public Attribute.Double actRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual RG1 N storage in kgN"
            )
            public Attribute.Double NActRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual RG2 N storage in kgN"
            )
            public Attribute.Double NActRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 inflow"
            )
            public Attribute.Double inRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 inflow"
            )
            public Attribute.Double inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 outflow"
            )
            public Attribute.Double outRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 outflow"
            )
            public Attribute.Double outRG2;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 N inflow in kgN"
            )
            public Attribute.Double N_RG1_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 N inflow in kgN"
            )
            public Attribute.Double N_RG2_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 N outflow in kgN"
            )
            public Attribute.Double N_RG1_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 N outflow in kgN"
            )
            public Attribute.Double N_RG2_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "N Percolation out of the soil profile in kgN"
            )
            public Attribute.Double PercoNabs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "N Percolation in the RG1 tank in kgN"
            )
            public Attribute.Double percoN_delayRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "N Percolation in the RG2 tank in kgN"
            )
            public Attribute.Double percoN_delayRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU Concentration in mgN/l for RG1"
            )
            public Attribute.Double N_concRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU Concentration in mgN/l for RG2"
            )
            public Attribute.Double N_concRG2;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum RG1 storage"
            )
            public Attribute.Double maxRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum RG2 storage"
            )
            public Attribute.Double maxRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "gwExcess"
            )
            public Attribute.Double gwExcess;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "gwExcess"
            )
            public Attribute.Double NExcess;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "portion of percolation to RG1 in l"
            )
            public Attribute.Double pot_RG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "portion of percolation to RG2 in l"
            )
            public Attribute.Double pot_RG2;
    
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "recision coefficient k RG1"
            )
            public Attribute.Double kRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "recision coefficient k RG2"
            )
            public Attribute.Double kRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "half-live time of nitrate in groundwater RG1 (time to reduce the amount of nitrate to its half) in a. If the value is 0 denitrifikation is inaktive (1 - 5)"
            )
            public Attribute.Double halflife_RG1;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "half-live time of nitrate in groundwater RG2 (time to reduce the amount of nitrate to its half) in a. If the value is 0 denitrifikation is inaktive (1 - 5)"
            )
            public Attribute.Double halflife_RG2;
     
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "amount of denitrificated Nitrate on the current day in kgN out of RG1"
            )
            public Attribute.Double denitRG1;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "amount of denitrificated Nitrate on the current day in kgN out of RG1"
            )
            public Attribute.Double denitRG2;
     
    
    
    
    /*
     *  Component run stages
     */
    
    public void init()  {
        
    }
    
    public void run() {
        
        double runN_RG1_out = 0;
        double runN_RG2_out = 0;
        double RG1_out = outRG1.getValue();
        double RG2_out = outRG2.getValue();
        double ActRG1 = actRG1.getValue();
        double ActRG2 = actRG2.getValue();
        double MaxRG1 = maxRG1.getValue();
        double MaxRG2 = maxRG2.getValue();
        double rungwExcess = gwExcess.getValue();
        double runN_RG1_in = N_RG1_in.getValue();
        double runN_RG2_in = N_RG2_in.getValue();
        
        double runNActRG1 = NActRG1.getValue();
        double runNActRG2 = NActRG2.getValue();
        double runN_concRG1 = 0;
        double runN_concRG2 = 0;
        double runpot_RG1 = pot_RG1.getValue();
        double runpot_RG2 = pot_RG2.getValue();
        double percoN = PercoNabs.getValue();
        double RGNretentinon = 1;
        double N_Excess = NExcess.getValue();
        double partN_Excess = 0;
        double partN_RG1 = 0;
        double partN_RG2 = 0;
        double k_decay_RG1 = 0;
        double k_decay_RG2 = 0;
        double rundenitRG1 = 0;
        double rundenitRG2 = 0;
        
        double runpercoN_delay_RG1 = percoN_delayRG1.getValue();
        double runpercoN_delay_RG2 = percoN_delayRG2.getValue();
        
        
        
        
        double percwatersum = runpot_RG1 + runpot_RG2 + rungwExcess;
        if (percwatersum > 0){
            partN_RG1 = (runpot_RG1 / percwatersum) * percoN;
            partN_RG2 = (runpot_RG2 / percwatersum) * percoN;
            partN_Excess = (rungwExcess / percwatersum) * percoN;
        }
        
        runpercoN_delay_RG1 = (1- Math.exp(-1/kRG1.getValue())) * partN_RG1 + (Math.exp(-1/kRG1.getValue())) * runpercoN_delay_RG1;
        runpercoN_delay_RG2 = (1- Math.exp(-1/kRG2.getValue())) * partN_RG2 + (Math.exp(-1/kRG2.getValue())) * runpercoN_delay_RG2;
        
        
        
        double watersum_RG1 = ActRG1 +  RG1_out;
        double watersum_RG2 = ActRG2 +  RG2_out;
        N_Excess = N_Excess + partN_Excess;
        
        runNActRG1 = runNActRG1 + runN_RG1_in + runpercoN_delay_RG1;
        runNActRG2 = runNActRG2 + runN_RG2_in + runpercoN_delay_RG2;
        
        if (watersum_RG1 > 0){
            runN_concRG1 = runNActRG1 * 1000000 / watersum_RG1;
        } else {
            runN_concRG1 = 0;
        }
        
        if (watersum_RG2 > 0){
            runN_concRG2 = runNActRG2 * 1000000 / watersum_RG2;
        } else {
            runN_concRG2 = 0;
        }
        
        runN_RG1_out = (RG1_out * runN_concRG1) / 1000000; // from mg/l to kg/l
        runN_RG2_out = (RG2_out * runN_concRG2) / 1000000; // from mg/l to kg/l
        
        if (runN_RG1_out  > runNActRG1){
            
            runN_RG1_out =  runNActRG1;
        }
        
        if (runN_RG2_out  > runNActRG2){
            
            runN_RG2_out =  runNActRG2;
        }
        
        runNActRG1 = runNActRG1 - runN_RG1_out;
        runNActRG2 = runNActRG2 - runN_RG2_out;
        
        if (halflife_RG1.getValue() > 0 || halflife_RG2.getValue() > 0) {
        
        k_decay_RG1 = 0.693 / (halflife_RG1.getValue() * 365 );
        k_decay_RG2 = 0.693 / (halflife_RG2.getValue() * 365 );
        
        rundenitRG1 = runNActRG1 - (runNActRG1 * Math.exp(k_decay_RG1 * 1));
        rundenitRG2 = runNActRG2 - (runNActRG2 * Math.exp(k_decay_RG2 * 1));
               
        
        }
        else {
        rundenitRG1 = 0;
        rundenitRG2 = 0;  
        }
        
        runNActRG1 = runNActRG1 - rundenitRG1;
        runNActRG2 = runNActRG2 - rundenitRG2;
//       System.out.println("N_RG1_out = " + N_RG1_out +" RG1_out =  "+ RG1_out);
//       System.out.println("N_RG2_out = " + N_RG2_out +" RG2_out =  "+ RG2_out);
        
        
        denitRG1.setValue(rundenitRG1);
        denitRG2.setValue(rundenitRG2);
        percoN_delayRG1.setValue(runpercoN_delay_RG1);
        percoN_delayRG2.setValue(runpercoN_delay_RG2);
        N_RG1_in.setValue(0);
        N_RG2_in.setValue(0);
        N_RG1_out.setValue(runN_RG1_out);
        N_RG2_out.setValue(runN_RG2_out);
        NActRG1.setValue(runNActRG1);
        NActRG2.setValue(runNActRG2);
        N_concRG1.setValue(runN_concRG1);
        N_concRG2.setValue(runN_concRG2);
        NExcess.setValue(N_Excess);
    }
    
    public void cleanup() {
        
    }
}

/*
                        <component class="org.jams.j2k.s_n.J2KGroundwaterN" name="J2KGroundwaterN">
                                <jamsvar name="inRG1" provider="HRUContext" providervar="currentEntity.inRG1"/>
                                <jamsvar name="inRG2" provider="HRUContext" providervar="currentEntity.inRG2"/>
                                <jamsvar name="outRG1" provider="HRUContext" providervar="currentEntity.outRG1"/>
                                <jamsvar name="outRG2" provider="HRUContext" providervar="currentEntity.outRG2"/>
                                <jamsvar name="actRG1" provider="HRUContext" providervar="currentEntity.actRG1"/>
                                <jamsvar name="actRG2" provider="HRUContext" providervar="currentEntity.actRG1"/>
                                <jamsvar name="N_RG1_out" provider="HRUContext" providervar="currentEntity.N_RG1_out"/>
                                <jamsvar name="N_RG2_out" provider="HRUContext" providervar="currentEntity.N_RG2_out"/>
                                <jamsvar name="N_RG1_in" provider="HRUContext" providervar="currentEntity.N_RG1_in"/>
                                <jamsvar name="N_RG2_in" provider="HRUContext" providervar="currentEntity.N_RG2_in"/>
                                <jamsvar name="N_concRG1" provider="HRUContext" providervar="currentEntity.N_concRG1"/>
                                <jamsvar name="NActRG1" provider="HRUContext" providervar="currentEntity.NActRG1"/>
                                <jamsvar name="N_concRG2" provider="HRUContext" providervar="currentEntity.N_concRG2"/>
                                <jamsvar name="NActRG2" provider="HRUContext" providervar="currentEntity.NActRG2"/>
                                <jamsvar name="pot_RG1" provider="HRUContext" providervar="currentEntity.pot_RG1"/>
                                <jamsvar name="pot_RG2" provider="HRUContext" providervar="currentEntity.pot_RG2"/>
                                <jamsvar name="partint" provider="HRUContext" providervar="currentEntity.partint"/>
                                <jamsvar name="PercoNabs" provider="HRUContext" providervar="currentEntity.PercoNabs"/>
                                <jamsvar name="maxRG1" provider="HRUContext" providervar="currentEntity.maxRG1"/>
                                <jamsvar name="maxRG2" provider="HRUContext" providervar="currentEntity.maxRG2"/>
                                <jamsvar name="InterflowNabs" provider="HRUContext" providervar="currentEntity.InterflowNabs"/>
                                <jamsvar name="N_delay_RG1" globvar="N_delay_RG1"/>
                                <jamsvar name="N_delay_RG2" globvar="N_delay_RG2"/>
                        </component>
 */