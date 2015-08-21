/*
 * InitJ2KProcessLayeredSoilWaterN.java
 * Created on 25. November 2005, 13:21
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, Peter Krause
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
package org.jams.j2k.s_n.init;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "InitJ2KProcessLayeredSoilWaterN",
author = "Peter Krause",
description = "Calculates soil water balance for each HRU without vertical layers")
public class InitJ2KProcessLayeredSoilWaterN extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The hru entities")
    public Attribute.EntityCollection entities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "attribute area")
    public Attribute.Double area;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "field capacity adaptation factor")
    public Attribute.Double FCAdaptation;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "air capacity adaptation factor")
    public Attribute.Double ACAdaptation;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU statevar rooting depth")
    public Attribute.Double rootDepth;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "number of horizons")
    public Attribute.Double horizons;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "soil horizon depths")
    public Attribute.DoubleArray depth_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU attribute maximum MPS")
    public Attribute.DoubleArray maxMPS_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU attribute maximum LPS")
    public Attribute.DoubleArray maxLPS_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU attribute maximum FPS")
    public Attribute.DoubleArray maxFPS_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU state var actual MPS")
    public Attribute.DoubleArray actMPS_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU state var actual LPS")
    public Attribute.DoubleArray actLPS_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU state var saturation of MPS")
    public Attribute.DoubleArray satMPS_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU state var saturation of LPS")
    public Attribute.DoubleArray satLPS_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU state var saturation of whole soil")
    public Attribute.Double satSoil_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "RD2 inflow")
    public Attribute.DoubleArray inRD2_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Soil bulk density in g/cm³")
    public Attribute.DoubleArray bulk_density_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "organic carbon content in %³")
    public Attribute.DoubleArray corg_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "saturated water condutivity of soil layer in cm/d")
    public Attribute.DoubleArray kf_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "ID of soil layer [-]")
    public Attribute.DoubleArray SID;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Indicates whether roots can penetrate or not the soil layer [-]")
    public Attribute.DoubleArray root_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU state var actual Moist")
    public Attribute.DoubleArray actMoist_h;

    /*
     *  Component run stages
     */
    public void init() {
    }

    public void run() {
        Attribute.Entity entity = entities.getCurrent();
        //System.out.println("Entity: " + entity.getId());
        int horizons = (int) this.horizons.getValue();
        double rootDepth = this.rootDepth.getValue() * 10;
        double remRD = rootDepth;
        double[] mxMPS = new double[horizons];
        double[] mxLPS = new double[horizons];
        double[] mxFPS = new double[horizons];
        double[] fxLPS = new double[horizons];
        double[] acMPS = new double[horizons];
        double[] acLPS = new double[horizons];
        double[] stMPS = new double[horizons];
        double[] stLPS = new double[horizons];
        double[] inRD2 = new double[horizons];
        double[] depth = new double[horizons];
        double[] bulk_density = new double[horizons];
        double[] root = new double[horizons];
        double[] corg = new double[horizons];
        double[] Kf_val = new double[horizons];
        double[] actMoist = new double[horizons];

        String aNameFC = "fieldcapacity_h";
        String aNameAC = "aircapacity_h";
        String aNameDC = "deadcapacity_h";
        String KfName = "kf_h";
        String depthName = "depth_h";
        String bulkdensityName = "bulk_density_h";
        String rootname = "root_h";
        String corgName = "corg_h";


        for (int h = 0; h < horizons; h++) {
            depth[h] = entity.getDouble(depthName + h);
            /*     if(remRD >= depth[h] && remRD > 0){
            mxMPS[h] = entity.getDouble(aNameFC+h);
            mxMPS[h] = mxMPS[h] * this.area.getValue();
            remRD = remRD - depth[h];
            }
            else if(remRD > 0){
            double frac = remRD / depth[h];
            mxMPS[h] = entity.getDouble(aNameFC+h) * frac;
            mxMPS[h] = mxMPS[h] * this.area.getValue();
            remRD = remRD - depth[h];
            }*/

            acMPS[h] = 0;
            mxMPS[h] = entity.getDouble(aNameFC + h) * area.getValue();
            mxFPS[h] = entity.getDouble(aNameDC + h) * area.getValue();
            mxLPS[h] = entity.getDouble(aNameAC + h) * area.getValue();
            corg[h] = entity.getDouble(corgName + h);
            root[h] = entity.getDouble(rootname + h);

            bulk_density[h] = entity.getDouble(bulkdensityName + h);
            Kf_val[h] = entity.getDouble(KfName + h);
            acLPS[h] = 0;
            stMPS[h] = 0;
            stLPS[h] = 0;

            inRD2[h] = 0;
            actMoist[h] = 0;
           // acMPS[6] = mxMPS[6];
           // acLPS[6] = mxLPS[6]/2;
            acMPS[0] = mxMPS[0];
            //acMPS[1] = mxMPS[1]/4;
           //acLPS[0] = mxLPS[0]/3;
        }

        this.bulk_density_h.setValue(bulk_density);
        this.corg_h.setValue(corg);
        this.maxFPS_h.setValue(mxFPS);
        this.maxMPS_h.setValue(mxMPS);
        this.maxLPS_h.setValue(mxLPS);
        this.actMPS_h.setValue(acMPS);
        this.actLPS_h.setValue(acLPS);
        this.satMPS_h.setValue(stMPS);
        this.satLPS_h.setValue(stLPS);
        this.inRD2_h.setValue(inRD2);
        this.depth_h.setValue(depth);
        this.satSoil_h.setValue(0);
        this.kf_h.setValue(Kf_val);
        this.root_h.setValue(root);
        this.actMoist_h.setValue(actMoist);

        if (Kf_val.length == horizons) {
            horizons = horizons;
        } else {
            horizons = horizons;
        }

    /* System.out.print("mxLPS: ");
    for(int h = 0; h < horizons; h++){
    System.out.print("\t"+mxLPS[h]);
    }
    System.out.println("");*/
    }

    public void cleanup() {
    }
}
