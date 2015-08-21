/*
 * Tillage.java
 *
 * Created on 7. März 2006, 12:53
 *
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


package org.jams.j2k.s_n;

/**
 *
 * @author c5ulbe
 */
public class Tillage {
    
    public int tid;
    public String tillnm, desc;
    public float effmix;
    public float deptil;
    
    
    /**
     * Creates a new instance of Fertilizer
     */
    public Tillage(String[] vals) {
        
        tid = Integer.parseInt(vals[0]);
        tillnm = (vals[1]);
        desc = (vals[2]);
        effmix = Float.parseFloat(vals[3]);
        deptil = Float.parseFloat(vals[4]);
             
       }
    
}
