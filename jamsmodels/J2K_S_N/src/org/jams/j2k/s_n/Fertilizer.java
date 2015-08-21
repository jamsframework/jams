/*
 * Fertilizer.java
 *
 * Created on 7. März 2006, 10:37
 *
 * *
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

import java.io.Serializable;

/**
 *
 * @author c5ulbe
 */
public class Fertilizer implements Serializable {
    
    public int fid;
    public String fertnm;
    public float fminn, fminp, forgn;
    public float forgp;
    public float fornh3n;
    public float bactpdb;
    public float bactldb;
    public float bactddb;
    public String desc;
    
    /**
     * Creates a new instance of Fertilizer
     */
    public Fertilizer(String[] vals) {
        fid = Integer.parseInt(vals[0]);
        fertnm = (vals[1]);
        fminn = Float.parseFloat(vals[2]);
        fminp = Float.parseFloat(vals[3]);
        forgn = Float.parseFloat(vals[4]);
        forgp = Float.parseFloat(vals[5]);
        fornh3n = Float.parseFloat(vals[6]);
        bactpdb = Float.parseFloat(vals[7]);
        bactldb = Float.parseFloat(vals[8]);
        bactddb = Float.parseFloat(vals[9]);
        desc = (vals[10]);
    }
    
}
