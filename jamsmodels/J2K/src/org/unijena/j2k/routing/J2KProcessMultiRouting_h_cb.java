/*
 * J2KProcessMultiRouting.java
 *
 * Created on 5. Juni 2008, 14:27
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
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
package org.unijena.j2k.routing;

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "J2KProcessMultiRouting_h_cb",
author = "Peter Krause, Holm Kipka, Bjoern Pfennig, Manfred Fink",
description = "Passes the output of the entities as input to the respective reach or unit including")
public class J2KProcessMultiRouting_h_cb extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The current hru entity")
    public Attribute.EntityCollection entities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Collection of reach objects")
    public Attribute.EntityCollection reaches;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Collection of reservoir objects")
    public Attribute.EntityCollection reservoirs;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar RD1 inflow")
    public Attribute.Double inRD1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar RD2 inflow")
    public Attribute.DoubleArray inRD2_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar RD2 inflow")
    public Attribute.DoubleArray outRD2_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar RG1 inflow")
    public Attribute.Double inRG1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar RG2 inflow")
    public Attribute.Double inRG2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar groundwater excess",
    defaultValue = "0")
    public Attribute.Double inGWExcess;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar RD1 outflow")
    public Attribute.Double outRD1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar RG1 outflow")
    public Attribute.Double outRG1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar RG2 outflow")
    public Attribute.Double outRG2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "An- bzw. Ausschalten des Moduls")
    public Attribute.Boolean cbModulAktiv;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "An- bzw. Ausschalten des Moduls")
    public Attribute.Double cbModulRD2out;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "An- bzw. Ausschalten des Moduls")
    public Attribute.Double modulRD2out;
  
    double[][] fracOut;
    double[] percOut;
    boolean modulCBaktiv;
    /*
     *  Component run stages
     */
    public void init() {
        this.modulCBaktiv = cbModulAktiv.getValue();
    }

    public void run() {

        Attribute.Entity entity = entities.getCurrent();

        //receiving polygon
        Attribute.Entity[] toPolyArray = (Attribute.Entity[]) entity.getObject("to_poly");
        //receiving reach
        Attribute.Entity[] toReachArray = (Attribute.Entity[]) entity.getObject("to_reach");
        //receiving reservoir
        Attribute.Entity toReservoir = null;

        Double[] polyWeightsArray = (Double[]) entity.getObject("to_poly_weights");
        Double[] reachWeightsArray = (Double[]) entity.getObject("to_reach_weights");

        Attribute.Entity toPoly, toReach;
        double polyWeight, reachWeight;

        try {
            toReservoir = (Attribute.Entity) entity.getObject("to_reservoir");
        } catch (Attribute.Entity.NoSuchAttributeException e) {
            toReservoir = null;
        }
        double RD1out = outRD1.getValue();
        double[] RD2out = outRD2_h.getValue();
        double RG1out = outRG1.getValue();
        double RG2out = outRG2.getValue();
        //test
        modulRD2out.setValue(0);
        cbModulRD2out.setValue(0);

        boolean keinziel = false;

        double[] srcDepth = ((Attribute.DoubleArray) entity.getObject("depth_h")).getValue();
        int srcHors = srcDepth.length;

        if (toPolyArray.length > 0) {

            for (int a = 0; a < toPolyArray.length; a++) {

                if (toPolyArray[a] != null) {

                    toPoly = toPolyArray[a];
                    polyWeight = polyWeightsArray[a];

                    double RD1in = toPoly.getDouble("inRD1");

                    double[] recDepth = ((Attribute.DoubleArray) toPoly.getObject("depth_h")).getValue();
                    int recHors = recDepth.length;

                    double[] RD2in = new double[recHors];

                    this.calcParts(srcDepth, recDepth);

                    double[] RD2Array = ((Attribute.DoubleArray) toPoly.getObject("inRD2_h")).getValue();
                    double RG1in = toPoly.getDouble("inRG1");

                    for (int j = 0; j < recHors; j++) {
                        RD2in[j] = RD2Array[j];
                        for (int i = 0; i < srcHors; i++) {
                            RD2in[j] = (double) RD2in[j] + (double) RD2out[i] * polyWeight * (double) fracOut[i][j];
                            RG1in = RG1in + RD2out[i] * polyWeight * this.percOut[i];
                        }
                    }

                    RD2in[recHors - 1] += inGWExcess.getValue();

                    if (recHors == 0) {
                        System.out.println("RecHors is null at entity " + entity.getObject("ID"));
                    }

                    double RG2in = toPoly.getDouble("inRG2");

                    RD1in = RD1in + RD1out * polyWeight;
                    RG1in = RG1in + RG1out * polyWeight;
                    RG2in = RG2in + RG2out * polyWeight;

                    Attribute.DoubleArray rdA = (Attribute.DoubleArray) toPoly.getObject("inRD2_h");
                    rdA.setValue(RD2in);
                    toPoly.setDouble("inRD1", RD1in);
                    toPoly.setObject("inRD2_h", rdA);
                    toPoly.setDouble("inRG1", RG1in);
                    toPoly.setDouble("inRG2", RG2in);

                    keinziel = true;
                }
            }
        }

        if (toReachArray.length > 0) {
            
                        //Schleife zur Beruecksichtigung des ContourBank-Abflusses
            if (this.modulCBaktiv == true) {

                double abflussCBReach_akt = entity.getDouble("cbAbfussReach");
                
                if (abflussCBReach_akt > 0) {

                    double[] anteileKomponentenCB = (double[]) entity.getObject("cbAnteileKomponenten");

                    toReach = toReachArray[0];

                    double RD1in = toReach.getDouble("inRD1");
                    double RD2in = toReach.getDouble("inRD2");
                    double RG1in = toReach.getDouble("inRG1");

                    RD1in += anteileKomponentenCB[0] * abflussCBReach_akt;
                    RD2in += anteileKomponentenCB[1] * abflussCBReach_akt;
                    RG1in += anteileKomponentenCB[2] * abflussCBReach_akt;

                    toReach.setDouble("inRD1", RD1in);
                    toReach.setDouble("inRD2", RD2in);
                    //test
                    cbModulRD2out.setValue(RD2in);
                    toReach.setDouble("inRG1", RG1in);

                    entity.setDouble("cbAbfussReach", 0.0);
                }
            }

            for (int a = 0; a < toReachArray.length; a++) {

                toReach = toReachArray[a];
                reachWeight = reachWeightsArray[a];

                double RD1in = toReach.getDouble("inRD1");
                double RD2in = toReach.getDouble("inRD2");

                RD2in = toReach.getDouble("inRD2");
                for (int h = 0; h < srcHors; h++) {
                    RD2in = RD2in + RD2out[h] * reachWeight;
                }

                RD2in += inGWExcess.getValue();

                double RG1in = toReach.getDouble("inRG1");
                double RG2in = toReach.getDouble("inRG2");

                RD1in = RD1in + RD1out * reachWeight;
                RG1in = RG1in + RG1out * reachWeight;
                RG2in = RG2in + RG2out * reachWeight;

                toReach.setDouble("inRD1", RD1in);
                toReach.setDouble("inRD2", RD2in);
                //test
                modulRD2out.setValue(RD2in);
                toReach.setDouble("inRG1", RG1in);
                toReach.setDouble("inRG2", RG2in);

                keinziel = true;
            }
        }

        RD1out = 0;
        for (int i = 0; i < srcHors; i++) {
            RD2out[i] = 0;
        }
        RG1out = 0;
        RG2out = 0;

        outRD1.setValue(RD1out);
        outRD2_h.setValue(RD2out);
        outRG1.setValue(RG1out);
        outRG2.setValue(RG2out);
        inGWExcess.setValue(0);

        if (keinziel == false) {
            getModel().getRuntime().println("Current entity ID: " + (int) entity.getDouble("ID") + " has no receiver.");
        }
    }

    public void cleanup() {
    }

    private void calcParts(double[] depthSrc, double[] depthRec) {
        int srcHorizons = depthSrc.length;
        int recHorizons = depthRec.length;

        double[] upBoundSrc = new double[srcHorizons];
        double[] lowBoundSrc = new double[srcHorizons];
        double low = 0;
        double up = 0;
        for (int i = 0; i < srcHorizons; i++) {
            low += depthSrc[i];
            up = low - depthSrc[i];
            upBoundSrc[i] = up;
            lowBoundSrc[i] = low;
        //System.out.getRuntime().println("Src --> up: "+up+", low: "+low);

        }
        double[] upBoundRec = new double[recHorizons];
        double[] lowBoundRec = new double[recHorizons];
        low = 0;
        up = 0;
        for (int i = 0; i < recHorizons; i++) {
            low += depthRec[i];
            up = low - depthRec[i];
            upBoundRec[i] = up;
            lowBoundRec[i] = low;
        //System.out.getRuntime().println("Rec --> up: "+up+", low: "+low);
        }


        fracOut = new double[depthSrc.length][depthRec.length];
        percOut = new double[depthSrc.length];
        for (int i = 0; i < depthSrc.length; i++) {
            double sumFrac = 0;
            for (int j = 0; j < depthRec.length; j++) {
                if ((lowBoundSrc[i] > upBoundRec[j]) && (upBoundSrc[i] < lowBoundRec[j])) {
                    double relDepth = Math.min(lowBoundSrc[i], lowBoundRec[j]) - Math.max(upBoundSrc[i], upBoundRec[j]);
                    double fracDepth = relDepth / depthSrc[i];
                    sumFrac += fracDepth;
                    fracOut[i][j] = fracDepth;
                }
            }
            percOut[i] = 1.0 - sumFrac;
        }
    }
}