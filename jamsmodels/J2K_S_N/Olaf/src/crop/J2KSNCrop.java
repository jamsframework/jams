/*
 * J2KSNCrop.java
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

import java.util.*;

/**
 *
 * @author Ulrike Bende
 */
public class J2KSNCrop {

    // TODO remove this later.
    public static Map<HashMap<String, Object>, ArrayList<J2KSNCrop>> landuseRotation =
            new HashMap<HashMap<String, Object>, ArrayList<J2KSNCrop>>();
    
    public int cid;
    public int idc;
    public String cpnm;
    public String cropname;
    public double phu;
    public double rue;
    public double hvsti;
    public double mlai;
    public double frgrw1;
    public double frgrw2;
    public double laimx1;
    public double laimx2;
    public double dlai;
    public double chtmx;
    public double rdmx;
    public double topt;
    public double tbase;
    public double cnyld;
    public double cpyld;
    public double bn1;
    public double bn2;
    public double bn3;
    public double bp1;
    public double bp2;
    public double bp3;
    public double wsyf;
    public double gsi;
    public double vpdfr;
    public double frgmax;
    public double wavp;
    public double co2hi;
    public double bioehi;
    public double rsdco_pl;
    public double lid;
    public double hu_ec1;
    public double hu_ec2;
    public double hu_ec3;
    public double hu_ec4;
    public double hu_ec5;
    public double hu_ec6;
    public double hu_ec7;
    public double hu_ec8;
    public double hu_ec9;
    public double lai_min;
    public double endbioN;
    public double maxfert;
    public double bion02;
    public double bion04;
    public double bion06;
    public double bion08;
    
    public ArrayList<J2KSNLMArable> managementList;
    
    /**
     * Creates a new instance of J2KSNCrop
     */
    public J2KSNCrop(String[] vals, ArrayList<J2KSNLMArable> mList) {
        
        cid = Integer.parseInt(vals[1]);
        idc = Integer.parseInt(vals[2]);
        cpnm = vals[3];
        cropname = vals[4];
        phu = Double.parseDouble(vals[5]);
        rue = Double.parseDouble(vals[6]);
        hvsti = Double.parseDouble(vals[7]);
        mlai = Double.parseDouble(vals[8]);
        frgrw1 = Double.parseDouble(vals[9]);
        frgrw2 = Double.parseDouble(vals[10]);
        laimx1 = Double.parseDouble(vals[11]);
        laimx2 = Double.parseDouble(vals[12]);
        dlai = Double.parseDouble(vals[13]);
        chtmx = Double.parseDouble(vals[14]);
        rdmx = Double.parseDouble(vals[15]);
        topt = Double.parseDouble(vals[16]);
        tbase = Double.parseDouble(vals[17]);
        cnyld = Double.parseDouble(vals[18]);
        cpyld = Double.parseDouble(vals[19]);
        bn1 = Double.parseDouble(vals[20]);
        bn2 = Double.parseDouble(vals[21]);
        bn3 = Double.parseDouble(vals[22]);
        bp1 = Double.parseDouble(vals[23]);
        bp2 = Double.parseDouble(vals[24]);
        bp3 = Double.parseDouble(vals[25]);
        wsyf = Double.parseDouble(vals[26]);
        gsi = Double.parseDouble(vals[27]);
        vpdfr = Double.parseDouble(vals[28]);
        frgmax  = Double.parseDouble(vals[29]);
        wavp = Double.parseDouble(vals[30]);
        co2hi  = Double.parseDouble(vals[31]);
        bioehi =  Double.parseDouble(vals[32]);
        rsdco_pl = Double.parseDouble(vals[33]);
        lid = Double.parseDouble(vals[34]);
        hu_ec1 = Double.parseDouble(vals[35]);
        hu_ec2 = Double.parseDouble(vals[36]);
        hu_ec3 = Double.parseDouble(vals[37]);
        hu_ec4 = Double.parseDouble(vals[38]);
        hu_ec5 = Double.parseDouble(vals[39]);
        hu_ec6 = Double.parseDouble(vals[40]);
        hu_ec7 = Double.parseDouble(vals[41]);
        hu_ec8 = Double.parseDouble(vals[42]);
        hu_ec9 = Double.parseDouble(vals[43]);
        lai_min = Double.parseDouble(vals[44]);
        endbioN = Double.parseDouble(vals[45]);
        maxfert = Double.parseDouble(vals[46]);
        bion02 = Double.parseDouble(vals[47]);
        bion04 = Double.parseDouble(vals[48]);
        bion06 = Double.parseDouble(vals[49]);
        bion08 = Double.parseDouble(vals[50]);
        
        this.managementList = mList;
    }
    
}
