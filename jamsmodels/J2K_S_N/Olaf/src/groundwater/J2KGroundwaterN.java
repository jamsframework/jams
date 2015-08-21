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

import ages.types.HRU;
import java.util.logging.Logger;
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
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/groundwater/J2KGroundwaterN.java $")
@VersionInfo
    ("$Id: J2KGroundwaterN.java 992 2010-02-17 22:07:00Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class J2KGroundwaterN  {

    private static final Logger log =
            Logger.getLogger("oms3.model." + J2KGroundwaterN.class.getSimpleName());

// Parameter
    @Description("Relativ size of the groundwaterN damping tank RG1 0 - 10 to calibrate in -")
    @Role(PARAMETER)
    @In public double N_delay_RG1;

    @Description("Relativ size of the groundwaterN damping tank RG2 0 - 10 to calibrate in -")
    @Role(PARAMETER)
    @In public double N_delay_RG2;

    @Description("HRU Concentration for RG1")
    @Role(PARAMETER)
    @Unit("mgN/l")
    @In  public double N_concRG1;

    @Description("HRU Concentration for RG2")
    @Role(PARAMETER)
    @Unit("mgN/l")
    @In  public double N_concRG2;


// In
    @Description("maximum RG1 storage")
    @In public double maxRG1;

    @Description("maximum RG2 storage")
    @In public double maxRG2;

    @Description("portion of percolation to RG1")
    @Unit("l")
    @In public double pot_RG1;

    @Description("portion of percolation to RG2")
    @Unit("l")
    @In public double pot_RG2;

    @Description("actual RG1 storage")
    @In public double actRG1;

    @Description("actual RG2 storage")
    @In public double actRG2;

    @Description("RG1 inflow")
    @In public double inRG1;

    @Description("RG2 inflow")
    @In public double inRG2;

    @Description("RG1 outflow")
    @In public double outRG1;

    @Description("RG2 outflow")
    @In public double outRG2;

    @Description("N Percolation out of the soil profile")
    @Unit("kgN")
    @In public double PercoNabs;

// In Out
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
 
 
    @Description("gwExcess")
    @In @Out public double gwExcess;

    @Description("gwExcess")
    @In @Out public double NExcess;

    @Description("actual RG1 N storage")
    @Unit("kgN")
    @In @Out public double NActRG1;

    @Description("actual RG2 N storage in kgN")
    @Unit("kgN")
    @In @Out public double NActRG2;

    @Description("Current hru object")
    @In @Out public HRU hru;

    boolean init = false;

    @Execute
    public void execute() {
        if (!init) {
              NActRG1 = (maxRG1 * N_concRG1 / 1000000) * N_delay_RG1;
              NActRG2 = (maxRG2 * N_concRG2 / 1000000) * N_delay_RG2;
              init = true;
        }
        
        double runNActRG1 = NActRG1;
        double runNActRG2 = NActRG2;

        double partN_Excess = 0;
        double partN_RG1 = 0;
        double partN_RG2 = 0;

        double percwatersum = pot_RG1 + pot_RG2 + gwExcess;
        if (percwatersum > 0) {
            partN_RG1 = (pot_RG1 / percwatersum) * PercoNabs;
            partN_RG2 = (pot_RG2 / percwatersum) * PercoNabs;
            partN_Excess = (gwExcess / percwatersum) * PercoNabs;
        }

        double watersum_RG1 = actRG1 + outRG1 + (maxRG1 * N_delay_RG1);
        double watersum_RG2 = actRG2 + outRG2 + (maxRG2 * N_delay_RG2);
        NExcess += partN_Excess;

        runNActRG1 += N_RG1_in + partN_RG1;
        runNActRG2 += N_RG2_in + partN_RG2;

        double runN_concRG1 = 0;
        if (watersum_RG1 > 0) {
            runN_concRG1 = runNActRG1 * 1000000 / watersum_RG1;
        }

        double runN_concRG2 = 0;
        if (watersum_RG2 > 0) {
            runN_concRG2 = runNActRG2 * 1000000 / watersum_RG2;
        }

        double runN_RG1_out = (outRG1 * runN_concRG1) / 1000000; // mg/l -> kg/l
        double runN_RG2_out = (outRG2 * runN_concRG2) / 1000000; // mg/l -> kg/l

        if (runN_RG1_out > runNActRG1) {
            runN_RG1_out = runNActRG1;
        }

        if (runN_RG2_out > runNActRG2) {
            runN_RG2_out = runNActRG2;
        }

        runNActRG1 -= runN_RG1_out;
        runNActRG2 -= runN_RG2_out;

        N_RG1_in = 0;
        N_RG2_in = 0;
        N_RG1_out = runN_RG1_out;
        N_RG2_out = runN_RG2_out;
        NActRG1 = runNActRG1;
        NActRG2 = runNActRG2;
//        N_concRG1 = runN_concRG1;
//        N_concRG2 = runN_concRG2;
    }

    public static void main(String[] args) {
        oms3.util.Components.explore(new J2KGroundwaterN());
    }

}

