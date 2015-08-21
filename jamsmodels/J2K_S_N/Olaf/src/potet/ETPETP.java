/*
 * j2kETP_E_TP.java
 * Created on 25. November 2005, 16:54
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
package potet;

import ages.types.HRU;
import java.util.logging.Level;
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
    ("Calculates evaporation and transpiration from actual ET")
@Keywords
    ("Evapotranspiration")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/potet/ETPETP.java $")
@VersionInfo
    ("$Id: ETPETP.java 996 2010-02-19 21:17:43Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class ETPETP  {

    private static final Logger log =
            Logger.getLogger("oms3.model." + ETPETP.class.getSimpleName());

    @Description("number of layers in soil profile")
    @In public int horizons;

    @Description("Array of state variables LAI ")
    @In public double LAI;

    @Description("HRU actual Evapotranspiration")
    @Unit("mm")
    @In public double[] actETP_h;

    @Description("HRU actual Evapotranspiration")
    @Unit("mm")
    @In public double actET;

    @Description("HRU potential Evapotranspiration")
    @Unit("mm")
    @In public double potET;

    @Description("HRU actual Evaporation")
    @Unit("mm")
    @Out public double aEvap;

    @Description("HRU actual Transpiration")
    @Unit("mm")
    @Out public double aTransp;

    @Description("HRU potential Evaporation")
    @Unit("mm")
    @Out public double pEvap;

    @Description("HRU potential Transpiration")
    @Unit("mm")
    @Out public double pTransp;

    @Description(" actual evaporation")
    @Unit("mm")
    @Out public double[] aEP_h;

    @Description(" actual evaporation")
    @Unit("mm")
    @Out public double[] aTP_h;

    @Description("Current hru object")
    @In @Out public HRU hru;


    @Execute
    public void execute() {
        if (aEP_h == null) {
            aEP_h = new double[horizons];
            aTP_h = new double[horizons];
        }
   
        if (LAI <= 3) {
            aTransp = (actET * LAI) / 3;
            pTransp = (potET * LAI) / 3;
        } else if (LAI > 3) {
            aTransp = actET;
            pTransp = potET;
        }
        aEvap = actET - aTransp;
        pEvap = potET - pTransp;

        for (int i = 0; i < horizons; i++) {
            double actETP = actETP_h[i];
            double actTran = 0;
            if (LAI <= 3) {
                actTran = (actETP * LAI) / 3;
            } else if (LAI > 3) {
                actTran = actETP;
            }
            aTP_h[i] = actTran;
            aEP_h[i] = actETP - actTran;
        }
        if (log.isLoggable(Level.INFO)) {
            log.info("aEvap:" + aEvap);
        }
    }

    public static void main(String[] args) {
        oms3.util.Components.explore(new ETPETP());
    }

}

