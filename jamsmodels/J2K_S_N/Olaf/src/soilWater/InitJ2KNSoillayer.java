/*
 * InitJ2KNSoilhorizons.java
 * Created on 17. February 2006, 14:49
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena, Manfred Fink
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
package soilWater;

import oms3.annotations.*;
import static oms3.annotations.Role.*;

/**
 *
 * @author Manfred Fink
 */

// InitJ2KNSoilhorizons
@Author
    (name = "Manfred Fink")
@Description
    ("Initialize soil N pools and additional variables")
@Keywords
    ("Utilities")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/soilWater/InitJ2KNSoillayer.java $")
@VersionInfo
    ("$Id: InitJ2KNSoillayer.java 996 2010-02-19 21:17:43Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class InitJ2KNSoillayer  {

    @Description("number of horizonss in soil profile")
    @In public int horizons;

    @Description("depth of soil horizons")
    @Unit("cm")
    @In public double[] depth_h;
    
    @Description("soil bulk density")
    @Unit("kg/dmÂ³")
    @In public double[] bulk_density_h;
    
    @Description("organic Carbon in soil")
    @Unit("%")
    @In public double[] corg_h;
    
    @Description("NO3-Pool")
    @Unit("kgN/ha")
    @Out public double[] NO3_Pool;
    
    @Description("NH4-Pool")
    @Unit("kgN/ha")
    @Out public double[] NH4_Pool;
    
    @Description("N-Organic Pool with reactive organic matter")
    @Unit("kgN/ha")
    @Out public double[] N_activ_pool;
    
    @Description("N-Organic Pool with stable organic matter")
    @Unit("kgN/ha")
    @Out public double[] N_stabel_pool;
    
    @Description("Residue in Layer")
    @Unit("kgN/ha")
    @Out public double[] residue_pool;
    
    @Description("N-Organic fresh Pool from Residue")
    @Unit("kgN/ha")
    @Out public double[] N_residue_pool_fresh;
    
    @Description("Nitrate in interflow in added to HRU horizons")
    @Unit("kgN")
    @Out public double[] InterflowN_in;
    
    @Description("Nitrate input due to Fertilisation")
    @Unit("kgN/ha")
    @Out public double fertNO3;
    
    @Description("Ammonium input due to Fertilisation")
    @Unit("kgN/ha")
    @Out public double fertNH4;
    
    @Description("Stable organig N input due to Fertilisation")
    @Unit("kgN/ha")
    @Out public double fertstableorg;
    
    @Description("Activ organig N input due to Fertilisation")
    @Unit("kgN/ha")
    @Out public double fertactivorg;
    
    @Description("flag plant existing yes or no ") // attention its a boolean!
    @Out public boolean plantExisting;
    
    @Description("Actual rooting depth")
    @Unit("m")
    @Out public double zrootd;
    
    @Description("actual LAI")
    @Out public double LAI;
    
    static private double fr_actN = 0.02;

    @Execute
    public void execute() {

        double[] NO3_Poolvals = new double[horizons];
        double[] NH4_Poolvals = new double[horizons];
        double[] N_activ_poolvals = new double[horizons];
        double[] N_stabel_poolvals = new double[horizons];
        double[] N_residue_pool_freshvals = new double[horizons];
        double[] Residue_poolvals = new double[horizons];
        double[] InterflowN_invals = new double[horizons];


        /** nitrogen active pool fraction. The fraction of 
         *  organic nitrogen in the active pool.
         */
        double hor_dept = 0;
        for (int i = 0; i < horizons; i++) {
            double runC_org = corg_h[i] / 1.72;
            double runsoil_bulk_density = bulk_density_h[i];
            double runhorizonsdepth = depth_h[i] * 10; //from cm to mm
            hor_dept = hor_dept + runhorizonsdepth;
            double runResidue_pool = 10;
            double runNO3_Pool = ((7 * Math.exp(-hor_dept / 1000)) * runsoil_bulk_density * runhorizonsdepth) / 1000;
            double runNH4_Pool = 0.1 * runNO3_Pool;
            double orgNhum = 10000 * runC_org / 14;  /*concentration of humic organic nitrogen in the horizons (mg/kg)*/
            double runN_activ_pool = ((orgNhum * fr_actN) * runsoil_bulk_density * runhorizonsdepth) / 100;
            double runN_stabel_pool = ((orgNhum * (1 - fr_actN)) * runsoil_bulk_density * runhorizonsdepth) / 100;
            double runN_residue_pool_fresh = 0.0015 * runResidue_pool;
            
            NO3_Poolvals[i] = runNO3_Pool;
            NH4_Poolvals[i] = runNH4_Pool;
            N_activ_poolvals[i] = runN_activ_pool;
            N_stabel_poolvals[i] = runN_stabel_pool;
            Residue_poolvals[i] = runResidue_pool;
            N_residue_pool_freshvals[i] = runN_residue_pool_fresh;
            InterflowN_invals[i] = 0;
        }

        plantExisting = true;
        NO3_Pool = NO3_Poolvals;
        NH4_Pool = NH4_Poolvals;
        N_activ_pool = N_activ_poolvals;
        N_stabel_pool = N_stabel_poolvals;
        residue_pool = Residue_poolvals;
        N_residue_pool_fresh = N_residue_pool_freshvals;
        InterflowN_in = InterflowN_invals;
        fertNO3 = 0;
        fertNH4 = 0;
        fertstableorg = 0;
        fertactivorg = 0;
        //inp_biomass = 0;
        //inpN_biomass = 0;
        zrootd = 0;
        LAI = 0;
    }

      public static void main(String[] args) {
        oms3.util.Components.explore(new InitJ2KNSoillayer());
    }

}
