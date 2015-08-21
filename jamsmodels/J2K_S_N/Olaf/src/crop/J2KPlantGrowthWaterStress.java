
/*
 * J2KPlantGrowthTemperatureStress.java
 * Created on 16. Februar 2006, 09:05
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

/**
 *
 * @author c8fima
 */
@Author
    (name = "Manfred Fink")
@Description
    ("Calculates plant growth water stress factor")
@Keywords
    ("Plantgrowth")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/crop/J2KPlantGrowthWaterStress.java $")
@VersionInfo
    ("$Id: J2KPlantGrowthWaterStress.java 966 2010-02-11 20:45:52Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class J2KPlantGrowthWaterStress  {

    @Description("HRU actual Transpiration")
    @Unit("mm")
    @In public double aTransp;
    
    @Description("HRU potential Transpiration")
    @Unit("mm")
    @In public double pTransp;
    
    @Description("plant growth water stress factor")
    @Out public double wstrs;



    @Execute
    public void execute() {
        wstrs = 1 - ((aTransp + 0.000001) / (pTransp + 0.000001));  //original SWAT
    }
}
