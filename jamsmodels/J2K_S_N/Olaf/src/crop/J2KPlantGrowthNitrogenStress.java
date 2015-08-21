
/*
 * J2KPlantGrowthNitrogenStress.java
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

import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Author
    (name = "Manfred Fink")
@Description
    ("Calculates plant growth N stress factor")
@Keywords
    ("Plantgrowth")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/crop/J2KPlantGrowthNitrogenStress.java $")
@VersionInfo
    ("$Id: J2KPlantGrowthNitrogenStress.java 966 2010-02-11 20:45:52Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class J2KPlantGrowthNitrogenStress  {

    @Description("optimal nitrogen content in Biomass")
    @Unit("kgN/ha")
    @In public double BioNoptAct;

    @Description("actual nitrogen content in Biomass")
    @Unit("kgN/ha")
    @In public double BioNAct;
    
    @Description("plant groth nitrogen stress factor")
    @Out public double nstrs;
    
    @Execute
    public void execute() {
        double phi_nit = 200 * (((BioNAct + 0.01) / (BioNoptAct + 0.01)) - 0.5);
        nstrs = 1 - (phi_nit / (phi_nit + Math.exp(3.535 - (0.02597 * phi_nit))));
    }

}
