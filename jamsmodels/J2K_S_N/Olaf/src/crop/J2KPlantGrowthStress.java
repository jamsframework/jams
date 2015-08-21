
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

import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Author
    (name = "Manfred Fink")
@Description
    ("Calculates overall plant growth stress using water, temperature, and N stresses")
@Keywords
    ("Plantgrowth")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/crop/J2KPlantGrowthStress.java $")
@VersionInfo
    ("$Id: J2KPlantGrowthStress.java 966 2010-02-11 20:45:52Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class J2KPlantGrowthStress  {

    @Description("plant growth nitrogen stress factor")
    @In public double nstrs;
    
    @Description("plant growth temperature stress factor")
    @In public double tstrs;
    
    @Description("plant growth water stress factor")
    @In public double wstrs;
    
    @Description("Plants daily biomass increase")
    @Unit("kg/ha")
    @In public double BioOpt_delta;

    @Description("Biomass sum produced for a given day drymass")
    @Unit("kg/ha")
    @Out public double BioAct;
    
    @Execute
    public void execute() {
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
