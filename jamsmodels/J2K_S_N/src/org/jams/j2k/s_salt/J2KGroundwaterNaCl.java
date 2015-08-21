/*
 * J2KGroundwaterNaCl.java
 * Created on 16. Januar 2006, 15:39
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

package org.jams.j2k.s_salt;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Manfred Fink
 */
@JAMSComponentDescription(
        title="J2KGroundwaterNaCl",
        author="Manfred Fink",
        description="Groundwater NaCl module with two different NaCl-Pools"
        )
        public class J2KGroundwaterNaCl extends JAMSComponent {
    
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
            description = "actual RG1 NaCl storage in kgNaCl"
            )
            public Attribute.Double NaClActRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual RG2 NaCl storage in kgNaCl"
            )
            public Attribute.Double NaClActRG2;
    
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
            description = "RG1 NaCl outflow in kgNaCl"
            )
            public Attribute.Double NaCl_RG1_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 NaCl outflow in kgNaCl"
            )
            public Attribute.Double NaCl_RG2_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "NaCl Percolation out of the soil profile in kgNaCl"
            )
            public Attribute.Double PercoNaClabs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU Concentration in mgNaCl/l for RG1"
            )
            public Attribute.Double NaCl_concRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU Concentration in mgNaCl/l for RG2"
            )
            public Attribute.Double NaCl_concRG2;
    
    
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
            public Attribute.Double NaClExcess;
    
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
            description = "Relativ size of the groundwaterN damping tank RG1 0 - 10 to calibrate in -"
            )
            public Attribute.Double NaCl_delay_RG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Relativ size of the groundwaterN damping tank RG2 0 - 10 to calibrate in -"
            )
            public Attribute.Double NaCl_delay_RG2;
    
    
    /*
     *  Component run stages
     */
    
    public void init()  {
        
    }
    
    public void run() {
        
        double runNaCl_RG1_out = 0;
        double runNaCl_RG2_out = 0;
        double RG1_out = outRG1.getValue();
        double RG2_out = outRG2.getValue();
        double ActRG1 = actRG1.getValue();
        double ActRG2 = actRG2.getValue();
        double MaxRG1 = maxRG1.getValue();
        double MaxRG2 = maxRG2.getValue();
        double rungwExcess = gwExcess.getValue();
        double runNaCl_RG1_in = NaCl_RG1_in.getValue();
        double runNaCl_RG2_in = NaCl_RG2_in.getValue();
        
        double runNaClActRG1 = NaClActRG1.getValue();
        double runNaClActRG2 = NaClActRG2.getValue();
        double runNaCl_concRG1 = 0;
        double runNaCl_concRG2 = 0;
        double runpot_RG1 = pot_RG1.getValue();
        double runpot_RG2 = pot_RG2.getValue();
        double percoNaCl = PercoNaClabs.getValue();
        double RGNretentinon = 1;
        double NaCl_Excess = NaClExcess.getValue();
        double partNaCl_Excess = 0;
        double partNaCl_RG1 = 0;
        double partNaCl_RG2 = 0;
        double percwatersum = runpot_RG1 + runpot_RG2 + rungwExcess;
        if (percwatersum > 0){
            partNaCl_RG1 = (runpot_RG1 / percwatersum) * percoNaCl;
            partNaCl_RG2 = (runpot_RG2 / percwatersum) * percoNaCl;
            partNaCl_Excess = (rungwExcess / percwatersum) * percoNaCl;
        }
        
        double watersum_RG1 = ActRG1 +  RG1_out +  (MaxRG1  * NaCl_delay_RG1.getValue());
        double watersum_RG2 = ActRG2 +  RG2_out +  (MaxRG2  * NaCl_delay_RG2.getValue());
        NaCl_Excess = NaCl_Excess + partNaCl_Excess;
        
        runNaClActRG1 = runNaClActRG1 + runNaCl_RG1_in + partNaCl_RG1;
        runNaClActRG2 = runNaClActRG2 + runNaCl_RG2_in + partNaCl_RG2;
        
        if (watersum_RG1 > 0){
            runNaCl_concRG1 = runNaClActRG1 * 1000000 / watersum_RG1;
        } else {
            runNaCl_concRG1 = 0;
        }
        
        if (watersum_RG2 > 0){
            runNaCl_concRG2 = runNaClActRG2 * 1000000 / watersum_RG2;
        } else {
            runNaCl_concRG2 = 0;
        }
        
        runNaCl_RG1_out = (RG1_out * runNaCl_concRG1) / 1000000; // from mg/l to kg/l
        runNaCl_RG2_out = (RG2_out * runNaCl_concRG2) / 1000000; // from mg/l to kg/l
        
        if (runNaCl_RG1_out  > runNaClActRG1){
            
            runNaCl_RG1_out =  runNaClActRG1;
        }
        
        if (runNaCl_RG2_out  > runNaClActRG2){
            
            runNaCl_RG2_out =  runNaClActRG2;
        }
        
        runNaClActRG1 = runNaClActRG1 - runNaCl_RG1_out;
        runNaClActRG2 = runNaClActRG2 - runNaCl_RG2_out;
//       System.out.println("N_RG1_out = " + N_RG1_out +" RG1_out =  "+ RG1_out);
//       System.out.println("N_RG2_out = " + N_RG2_out +" RG2_out =  "+ RG2_out);
        
        
        NaCl_RG1_in.setValue(0);
        NaCl_RG2_in.setValue(0);
        NaCl_RG1_out.setValue(runNaCl_RG1_out);
        NaCl_RG2_out.setValue(runNaCl_RG2_out);
        NaClActRG1.setValue(runNaClActRG1);
        NaClActRG2.setValue(runNaClActRG2);
        NaCl_concRG1.setValue(runNaCl_concRG1);
        NaCl_concRG2.setValue(runNaCl_concRG2);
        NaClExcess.setValue(NaCl_Excess);
    }
    
    public void cleanup() {
        
    }
}

/*
                        <component class="org.jams.j2k.s_n.J2KGroundwaterNaCl" name="J2KGroundwaterNaCl">
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