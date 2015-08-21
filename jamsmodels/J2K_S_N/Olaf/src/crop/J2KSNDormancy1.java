/*
 * J2KSNDormancy.java
 * Created on 24. Oktober 2006, 13:15
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
    ("Calculates dormancy of plants using day length -" +
   "dormancy variable is also used to simulate maturity")
@Keywords
    ("Crop")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/crop/J2KSNDormancy1.java $")
@VersionInfo
    ("$Id: J2KSNDormancy1.java 957 2010-02-11 20:24:54Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class J2KSNDormancy1  {
    
    @Description("Maximum sunshine duration")
    @Unit("h")
    @Role(PARAMETER)
    @In public double sunhmax;

    // TODO READWRITE subst
    @Description("Minimum yearly sunshine duration")
    @Unit("h")
    @Role(PARAMETER)
    @In @Out public double sunhmin;
    
    @Description("entity latidute")
    @In public double latitude;

    @Description("indicates dormancy of plants")
    @Out public boolean dormancy;

    @Description("Plants base growth temperature")
    @Unit("C")
    @In public double tbase;
    
    @Description("HRU daily mean temperature")
    @Unit("C")
    @In public double tmean;

    // TODO READWRITE subst
    @Description("Fraction of actual potential heat units sum")
    @In public double FPHUact;

    @Execute
    public void execute() {

        double sunhminrun = 0;
        if (sunhmin > 0) {
            sunhminrun = sunhmin;
        } else {
            sunhminrun = sunhmax;
        }

        sunhminrun = Math.min(sunhminrun, sunhmax);
        double tdorm = 0;
        if (latitude < 20) {
            tdorm = 0;
        } else if (latitude < 40) {
            tdorm = (latitude - 20) / 20;
        } else {
            tdorm = 1;
        }

        if (sunhmax < (sunhminrun + tdorm)) {
            dormancy = true;
        } else {
            if (tmean < tbase) {
                dormancy = true;
            } else {
                dormancy = false;
            }
        }
        if (FPHUact > 1) {
            dormancy = true;
        }
        sunhmin = sunhminrun;
    }
}
