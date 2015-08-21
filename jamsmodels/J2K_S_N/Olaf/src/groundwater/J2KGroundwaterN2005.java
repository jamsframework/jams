/*
 * J2KGroundwaterN.java
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
package groundwater;

import oms3.annotations.*;
import static oms3.annotations.Role.*;

/**
 *
 * @author Manfred Fink
 */
@Author
    (name = "Manfred Fink")
@Description
    ("Calculates N movement in groundwater using two different N pools")
@Keywords
    ("Groundwater")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/groundwater/J2KGroundwaterN2005.java $")
@VersionInfo
    ("$Id: J2KGroundwaterN2005.java 967 2010-02-11 20:49:49Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class J2KGroundwaterN2005  {

    @Description("half-live time of nitrate in groundwater RG1 (time to reduce the amount of nitrate to its half) in a. If the value is 0 denitrifikation is inaktive (1 - 5)")
    @Role(PARAMETER)
    @In public double halflife_RG1;

    @Description("half-live time of nitrate in groundwater RG2 (time to reduce the amount of nitrate to its half) in a. If the value is 0 denitrifikation is inaktive (1 - 5)")
    @Role(PARAMETER)
    @In public double halflife_RG2;
    
    @Description("actual RG1 storage")
    @In public double actRG1;    
    
    @Description("actual RG2 storage")
    @In public double actRG2;
    
    @Description("actual RG1 N storage")
    @Unit("kgN")
    @In @Out public double NActRG1;
    
    @Description("actual RG2 N storage")
    @Unit("kgN")
    @In @Out public double NActRG2;
    
    @Description("RG1 inflow")
    @In public double inRG1;    
    
    @Description("RG2 inflow")
    @In public double inRG2;    
    
    @Description("RG1 outflow")
    @In public double outRG1;    
    
    @Description("RG2 outflow")
    @In public double outRG2;
    
    // TODO READWRITE subst
    @Description("RG1 N inflow")
    @Unit("kgN")
    @In @Out public double N_RG1_in;
    
    @Description("RG2 N inflow")
    @Unit("kgN")
    @In @Out public double N_RG2_in;
    
    @Description("RG1 N outflow")
    @Unit("kgN")
    @In @Out public double N_RG1_out;
    
    @Description("RG2 N outflow")
    @Unit("kgN")
    @In @Out public double N_RG2_out;
    
    @Description("N Percolation out of the soil profile")
    @Unit("kgN")
    @In public double PercoNabs;
    
    @Description("HRU Concentration for RG1")
    @Unit("mgN/l")
    @In @Out public double N_concRG1;
    
    @Description("HRU Concentration for RG2")
    @Unit("mgN/l")
    @In @Out public double N_concRG2;
    
    @Description("maximum RG1 storage")
    @In public double maxRG1;    
    
    @Description("maximum RG2 storage")
    @In public double maxRG2;
    
    @Description("gwExcess")
    @In public double gwExcess;
    
    @Description("NExcess")
    @In @Out public double NExcess;
    
    @Description("portion of percolation to RG1")
    @Unit("l")
    @In public double pot_RG1;    
   
    @Description("portion of percolation to RG2")
    @Unit("l")
    @In public double pot_RG2;
    
    @Description("recision coefficient k RG1")
    @In public double kRG1;
    
    @Description("recision coefficient k RG2")    
    @In public double kRG2;
    
    @Description("amount of denitrificated Nitrate on the current day out of RG1")
    @Unit("kgN")
    @Out public double denitRG1;
    
    @Description("amount of denitrificated Nitrate on the current day out of RG1")
    @Unit("kgN")
    @Out public double denitRG2;
    
    @Description("N Percolation in the RG1 tank")
    @Unit("kgN")
    @In @Out public double percoN_delayRG1;
    
    @Description("N Percolation in the RG2 tank")
    @Unit("kgN")
    @In @Out public double percoN_delayRG2;
    
    @Execute
    public void execute() {
        double runN_RG1_out = 0;
        double runN_RG2_out = 0;
        double RG1_out = outRG1;
        double RG2_out = outRG2;
        double ActRG1 = actRG1;
        double ActRG2 = actRG2;
        double rungwExcess = gwExcess;
        double runN_RG1_in = N_RG1_in;
        double runN_RG2_in = N_RG2_in;
        double runNActRG1 = NActRG1;
        double runNActRG2 = NActRG2;
        double runN_concRG1 = 0;
        double runN_concRG2 = 0;
        double runpot_RG1 = pot_RG1;
        double runpot_RG2 = pot_RG2;
        double percoN = PercoNabs;
        double N_Excess = NExcess;
        double partN_Excess = 0;
        double partN_RG1 = 0;
        double partN_RG2 = 0;
        double k_decay_RG1 = 0;
        double k_decay_RG2 = 0;
        double rundenitRG1 = 0;
        double rundenitRG2 = 0;
        
        double runpercoN_delay_RG1 = percoN_delayRG1;
        double runpercoN_delay_RG2 = percoN_delayRG2;
        
        double percwatersum = runpot_RG1 + runpot_RG2 + rungwExcess;
        if (percwatersum > 0) {
            partN_RG1 = (runpot_RG1 / percwatersum) * percoN;
            partN_RG2 = (runpot_RG2 / percwatersum) * percoN;
            partN_Excess = (rungwExcess / percwatersum) * percoN;
        }
        
        runpercoN_delay_RG1 = (1- Math.exp(-1/kRG1)) * partN_RG1 + (Math.exp(-1/kRG1)) * runpercoN_delay_RG1;
        runpercoN_delay_RG2 = (1- Math.exp(-1/kRG2)) * partN_RG2 + (Math.exp(-1/kRG2)) * runpercoN_delay_RG2;
        
        double watersum_RG1 = ActRG1 +  RG1_out;
        double watersum_RG2 = ActRG2 +  RG2_out;
        
        N_Excess = N_Excess + partN_Excess;
        
        runNActRG1 = runNActRG1 + runN_RG1_in + runpercoN_delay_RG1;
        runNActRG2 = runNActRG2 + runN_RG2_in + runpercoN_delay_RG2;
        
        if (watersum_RG1 > 0) {
            runN_concRG1 = runNActRG1 * 1000000 / watersum_RG1;
        } else {
            runN_concRG1 = 0;
        }
        
        if (watersum_RG2 > 0) {
            runN_concRG2 = runNActRG2 * 1000000 / watersum_RG2;
        } else {
            runN_concRG2 = 0;
        }
        
        runN_RG1_out = (RG1_out * runN_concRG1) / 1000000; // from mg/l to kg/l
        runN_RG2_out = (RG2_out * runN_concRG2) / 1000000; // from mg/l to kg/l
        
        if (runN_RG1_out > runNActRG1) {
            runN_RG1_out = runNActRG1;
        }
        
        if (runN_RG2_out > runNActRG2) {
            runN_RG2_out = runNActRG2;
        }
        
        runNActRG1 = runNActRG1 - runN_RG1_out;
        runNActRG2 = runNActRG2 - runN_RG2_out;
        
        if (halflife_RG1 > 0 || halflife_RG2 > 0) {
            k_decay_RG1 = 0.693 / (halflife_RG1 * 365 );
            k_decay_RG2 = 0.693 / (halflife_RG2 * 365 );
            
            rundenitRG1 = runNActRG1 - (runNActRG1 * Math.exp(k_decay_RG1 * 1));
            rundenitRG2 = runNActRG2 - (runNActRG2 * Math.exp(k_decay_RG2 * 1));
        } else {
            rundenitRG1 = 0;
            rundenitRG2 = 0;
        }
        
        runNActRG1 = runNActRG1 - rundenitRG1;
        runNActRG2 = runNActRG2 - rundenitRG2;
        
        denitRG1 = rundenitRG1;
        denitRG2 = rundenitRG2;
        percoN_delayRG1 = runpercoN_delay_RG1;
        percoN_delayRG2 = runpercoN_delay_RG2;
        N_RG1_in = 0;
        N_RG2_in = 0;
        N_RG1_out = runN_RG1_out;
        N_RG2_out = runN_RG2_out;
        NActRG1 = runNActRG1;
        NActRG2 = runNActRG2;
        N_concRG1 = runN_concRG1;
        N_concRG2 = runN_concRG2;
        NExcess = N_Excess;
    }
}

