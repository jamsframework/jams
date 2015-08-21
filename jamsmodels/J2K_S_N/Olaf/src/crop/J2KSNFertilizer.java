/*
 * J2KSNFertilizer.java
 *
 * Created on 7. MÃ¤rz 2006, 10:37
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
package crop;

/**
 *
 * @author c5ulbe
 */
public class J2KSNFertilizer {

    public int fid;
    public String fertnm;
    public double fminn;
    public double fminp;
    public double forgn;
    public double forgp;
    public double fnh4n;
    public double bactpdb;
    public double bactldb;
    public double bactddb;
    public String desc;

    /**
     * Creates a new instance of J2KSNFertilizer
     */
    public J2KSNFertilizer(String[] vals) {
        fid = Integer.parseInt(vals[1]);
        fertnm = vals[2];
        fminn = Double.parseDouble(vals[3]);
        fminp = Double.parseDouble(vals[4]);
        forgn = Double.parseDouble(vals[5]);
        forgp = Double.parseDouble(vals[6]);
        fnh4n = Double.parseDouble(vals[7]);
        bactpdb = Double.parseDouble(vals[8]);
        bactldb = Double.parseDouble(vals[9]);
        bactddb = Double.parseDouble(vals[10]);
        desc = vals[11];
    }
}
