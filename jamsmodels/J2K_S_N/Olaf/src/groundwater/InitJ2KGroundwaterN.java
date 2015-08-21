/*
 * InitJ2KNSoil.java
 * Created on 13. February 2006, 09:03
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
package groundwater;

import oms3.annotations.*;
import static oms3.annotations.Role.*;

/**
 *
 * @author Manfred Fink
 */

// InitJ2KGroundwaterN
@Author
    (name = "Manfred Fink")
@Description
    ("Initialize groundwater N using two different N pools")
@Keywords
    ("Groundwater")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/groundwater/InitJ2KGroundwaterN.java $")
@VersionInfo
    ("$Id: InitJ2KGroundwaterN.java 959 2010-02-11 20:31:47Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class InitJ2KGroundwaterN  {

    @Description("maximum RG1 storage")
    @In public double maxRG1;
    
    @Description("maximum RG2 storage")
    @In public double maxRG2;
    
    @Description("actual RG1 N storage")
    @Unit("kgN")
    @Out public double NActRG1;
    
    @Description("actual RG2 N storage")
    @Unit("kgN")
    @Out public double NActRG2;
    
    @Description("HRU Concentration for RG1")
    @Unit("mgN/l")
    @In public double N_concRG1;
    
    @Description("HRU Concentration for RG2")
    @Unit("mgN/l")
    @In public double N_concRG2;
    
    @Description("Relativ size of the groundwaterN damping tank RG1 0 - 10 to calibrate")
    @Role(PARAMETER)
    @In public double N_delay_RG1;
    
    @Description("Relativ size of the groundwaterN damping tank RG2 0 - 10 to calibrate")
    @Role(PARAMETER)
    @In public double N_delay_RG2;

    @Execute
    public void execute() {
        NActRG1 = (maxRG1 * N_concRG1 / 1000000) * N_delay_RG1;
        NActRG2 = (maxRG2 * N_concRG2 / 1000000) * N_delay_RG2;
    }

}
