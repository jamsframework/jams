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

import ages.types.HRU;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/crop/J2KSNDormancy.java $")
@VersionInfo
    ("$Id: J2KSNDormancy.java 963 2010-02-11 20:38:23Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class J2KSNDormancy  {

    private static final Logger log =
            Logger.getLogger("oms3.model." + J2KSNDormancy.class.getSimpleName());
    
    @Description("Maximum sunshine duration")
    @Unit("h")
    @In public double sunhmax;

    @Description("entity lat.")
    @In public double latitude;

    @Description("Plants base growth temperature")
    @Unit("C")
    @In public double tbase;

    @Description("HRU daily mean temperature")
    @Unit("C")
    @In public double tmean;

    @Description("Fraction of actual potential heat units sum")
    @In public double FPHUact;
    
    @Description("indicates dormancy of plants")
    @Out public boolean dormancy;

    @Description("Minimum yearly sunshine duration")
    @Unit("h")
    @In @Out public double sunhmin;

    @Description("Current hru object")
    @In @Out public HRU hru;


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
        if (log.isLoggable(Level.INFO)) {
            log.info("sunhmin:" + sunhmin);
        }
    }

    public static void main(String[] args) {
        oms3.util.Components.explore(new J2KSNDormancy());
    }

}
