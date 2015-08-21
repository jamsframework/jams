
/*
 * J2KPlantGrowthStress.java
 * Created on 16. Februar 2006, 09:18
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c8fima
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
package crop;

import ages.types.HRU;
import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Author
    (name = "Manfred Fink, Olaf David")
@Description
    ("Calculates overall plant growth stress using water, temperature, and N stresses")
@Keywords
    ("Plantgrowth")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/sn/J2KPlantGrowthStress.java $")
@VersionInfo
    ("$Id: J2KPlantGrowthStress.java 893 2010-01-29 16:06:46Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class PlantGrowthStress  {

    @Description("Current hru object")
    @In @Out public HRU hru;

    @Description("HRU actual Transpiration")
    @Unit("mm")
    @In public double aTransp;

    @Description("HRU potential Transpiration")
    @Unit("mm")
    @In public double pTransp;

    @Description("optimal nitrogen content in Biomass")
    @Unit("kgN/ha")
    @In public double BioNoptAct;

    @Description("actual nitrogen content in Biomass")
    @Unit("kgN/ha")
    @In public double BioNAct;

    @Description("Plants daily biomass increase")
    @Unit("kg/ha")
    @In public double BioOpt_delta;

    @Description("daily mean temperature")
    @Unit("degC")
    @In public double tmean;

    @Description("plant groth base temperature")
    @Unit("degC")
    @In public double tbase;

    @Description("plant groth optimal temperature")
    @Unit("degC")
    @In public double topt;

    @Description("plant groth temperature stress factor")
    @Out public double tstrs;
    
    @Description("plant groth nitrogen stress factor")
    @Out public double nstrs;

    @Description("plant growth water stress factor")
    @Out public double wstrs;
    
    @Description("Biomass sum produced for a given day drymass")
    @Unit("kg/ha")
    @In @Out public double BioAct;

    @Execute
    public void execute() {
        // water stress
        wstrs = 1 - ((aTransp + 0.000001) / (pTransp + 0.000001));  //original SWAT

        // nitrogen stress
        double phi_nit = 200 * (((BioNAct + 0.01) / (BioNoptAct + 0.01)) - 0.5);
        nstrs = 1 - (phi_nit / (phi_nit + Math.exp(3.535 - (0.02597 * phi_nit))));


        // temp stress
        tstrs = 0;
        if (tmean <= tbase) {
            tstrs = 1;
        } else if (tmean > tbase && tmean <= topt) {
            tstrs = 1 - (Math.exp(((-0.1054 * Math.pow((topt - tmean), 2)))
                    / Math.pow((tmean - tbase), 2)));
        } else if (tmean > topt && tmean <= ((2 * topt) - tbase)) {
            tstrs = 1 - (Math.exp(((-0.1054 * Math.pow((topt - tmean), 2)))
                    / Math.pow(((2 * topt) - tmean - tbase), 2)));
        } else if (tmean > ((2 * topt) - tbase)) {
            tstrs = 1;
        }
        
        // stress
        double stressfactor = 1 - Math.max(wstrs, (Math.max(tstrs, nstrs)));
        if (stressfactor > 1) {
            stressfactor = 1;
        }
        if (stressfactor < 0) {
            stressfactor = 0;
        }
        BioAct = (stressfactor * BioOpt_delta) + BioAct;
    }
}
