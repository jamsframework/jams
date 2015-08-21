/*
 * J2KSNLMArable.java
 *
 * Created on 8. März 2006, 09:40
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

package org.jams.j2k.s_n.crop;

import java.util.*;

/**
 *
 * @author Ulrike Bende-Michl
 */
public class J2KSNLMArableGW  extends J2KSNLMArable {
    
    public double Irrigation;
/*
    public double startdate;
    public double enddate;
    public double mgtfertilization;
    public double mgttillage;
    public double endcroprot;
 */
    
    /**
     * Creates a new instance of LMArable
     */
    public J2KSNLMArableGW(String[] vals, HashMap<Integer, J2KSNTillage> tills, HashMap<Integer, J2KSNFertilizer> ferts) {
        
        super(vals, tills, ferts);
        
        try {
                        
            if (vals[8].equalsIgnoreCase("-")) {
                Irrigation = -1;
            } else {
                Irrigation = Double.parseDouble(vals[8]);
            }
            
            
        } catch (java.lang.NumberFormatException nfe) {
            System.out.println("Wrong management data format");
        }
    }
}

