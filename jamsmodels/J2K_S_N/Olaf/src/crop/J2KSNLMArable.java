/*
 * J2KSNLMArable.java
 *
 * Created on 8. MÃ¤rz 2006, 09:40
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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

import java.util.*;

/**
 *
 * @author Ulrike Bende-Michl
 */
public class J2KSNLMArable {

    public J2KSNTillage till;
    public J2KSNFertilizer fert;
    public int jDay;
    public double famount;
    public boolean plant;
    public int harvest;
    public double fracHarvest;

    public J2KSNLMArable(String[] vals,
            HashMap<Integer, J2KSNTillage> tills, HashMap<Integer, J2KSNFertilizer> ferts) {

        jDay = Integer.parseInt(vals[2]);
        if (vals[3].equalsIgnoreCase("-")) {
            till = null;
        } else {
            till = tills.get(Integer.parseInt(vals[3]));
        }

        if (vals[4].equalsIgnoreCase("-")) {
            fert = null;
        } else {
            fert = ferts.get(Integer.parseInt(vals[4]));
        }

        if (vals[5].equalsIgnoreCase("-")) {
            famount = -1;
        } else {
            famount = Double.parseDouble(vals[5]);
        }

        if (vals[6].equalsIgnoreCase("-")) {
            plant = false;
        } else {
            plant = true;
        }

        if (vals[7].equalsIgnoreCase("-")) {
            harvest = -1;
        } else {
            harvest = Integer.parseInt(vals[7]);
        }

        if (vals[8].equalsIgnoreCase("-")) {
            fracHarvest = -1;
        } else {
            fracHarvest = Double.parseDouble(vals[8]);
        }
    }
}

