/*
 * J2KNRoutinglayer.java
 * Created on 11. March 2006, 09:21
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe & Manfred Fink
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

import jams.data.*;
import jams.model.*;
import jams.JAMS;

/**
 *
 * @author Peter Krause & Manfred Fink
 */
@JAMSComponentDescription(title = "J2KNMultiRoutinglayer",
author = "Peter Krause & Manfred Fink",
description = "Passes the N output of the entities as N input to the respective reach or unit")
public class J2KNMultiRoutinglayer_cb extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The current hru entity")
    public Attribute.EntityCollection entities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Collection of reach objects")
    public Attribute.EntityCollection reaches;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RD1 N inflow in kgN")
    public Attribute.Double SurfaceN_in;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RD2 N inflow in kgN")
    public Attribute.DoubleArray InterflowN_in;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RG1 N inflow in kgN")
    public Attribute.Double N_RG1_in;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RG2 N inflow in kgN")
    public Attribute.Double N_RG2_in;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RD1 N outflow in kgN")
    public Attribute.Double SurfaceNabs;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RD2 N outflow in kgN")
    public Attribute.DoubleArray InterflowNabs;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RG1 N outflow in kgN")
    public Attribute.Double N_RG1_out;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RG2 N outflow in kgN")
    public Attribute.Double N_RG2_out;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "gwExcess")
    public Attribute.Double NExcess;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "An- bzw. Ausschalten des Moduls")
    public Attribute.Boolean cbModulAktiv;
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

        Double[] polyWeightsArray = (Double[]) entity.getObject("to_poly_weights");
        Double[] reachWeightsArray = (Double[]) entity.getObject("to_reach_weights");

        Attribute.Entity toPoly, toReach;
        double polyWeight, reachWeight;

        boolean keinziel = false;

        double[] srcDepth = ((Attribute.DoubleArray) entity.getObject("depth_h")).getValue();
        int srcHors = srcDepth.length;


        double NRD1out = SurfaceNabs.getValue();
        double[] NRD2out_h = InterflowNabs.getValue();
        double NRG1out = N_RG1_out.getValue();
        double NRG2out = N_RG2_out.getValue();

        double reachNRD2in = 0;

        if (toPolyArray.length > 0) {

            for (int a = 0; a < toPolyArray.length; a++) {

                if (toPolyArray[a] != null) {

                    toPoly = toPolyArray[a];
                    polyWeight = polyWeightsArray[a];

                    double[] recDepth = ((Attribute.DoubleArray) toPoly.getObject("depth_h")).getValue();
                    int recHors = recDepth.length;


                    double[] NRD2in_h = new double[recHors];

                    double NRD1in = toPoly.getDouble("SurfaceNaCl");
                    double[] rdArN = ((Attribute.DoubleArray) toPoly.getObject("InterflowNaCl_in")).getValue();

                    double NRG1in = toPoly.getDouble("NaCl_RG1_in");
                    double NRG2in = toPoly.getDouble("NaCl_RG2_in");
                    for (int j = 0; j < recHors; j++) {
                        NRD2in_h[j] = rdArN[j];
                        for (int i = 0; i < srcHors; i++) {
                            NRD2in_h[j] = NRD2in_h[j] + (NRD2out_h[i] / recHors) * polyWeight;
                        }
                    }


                    for (int i = 0; i < srcHors; i++) {
                        NRD2out_h[i] = 0;
                    }
                    NRD2in_h[recHors - 1] += NExcess.getValue();

                    NRD1in = NRD1in + NRD1out * polyWeight;
                    NRG1in = NRG1in + NRG1out * polyWeight;
                    NRG2in = NRG2in + NRG2out * polyWeight;

                    NRD1out = 0;
                    NRG1out = 0;
                    NRG2out = 0;

                    SurfaceNabs.setValue(0);
                    InterflowNabs.setValue(NRD2out_h);
                    N_RG1_out.setValue(0);
                    N_RG2_out.setValue(0);
                    NExcess.setValue(0);

                    Attribute.DoubleArray rdAN = (Attribute.DoubleArray) toPoly.getObject("InterflowNaCl_in");
                    rdAN.setValue(NRD2in_h);
                    toPoly.setDouble("SurfaceNaCl_in", NRD1in);
                    toPoly.setObject("InterflowNaCl_in", rdAN);
                    toPoly.setDouble("NaCl_RG1_in", NRG1in);
                    toPoly.setDouble("NaCl_RG2_in", NRG2in);

                    keinziel = true;

                }
            }

        }
        if (toReachArray.length > 0) {

            if (this.modulCBaktiv == true) {

                double abflussCBReach_akt = entity.getDouble("cbrunofReachNaCl");

                if (abflussCBReach_akt > 0) {

                    double[] anteileKomponentenCB = (double[]) entity.getObject("cbAnteileKomponenten");

                    toReach = toReachArray[0];

                    double RD1in = toReach.getDouble("SurfaceN_in");
                    double RD2in = toReach.getDouble("InterflowN_sum");
                    double RG1in = toReach.getDouble("N_RG1_in");

                    RD1in += anteileKomponentenCB[0] * abflussCBReach_akt;
                    RD2in += anteileKomponentenCB[1] * abflussCBReach_akt;
                    RG1in += anteileKomponentenCB[2] * abflussCBReach_akt;
                    
                    
                    
                    toReach.setDouble("SurfaceN_in", RD1in);
                    toReach.setDouble("InterflowN_sum", RD2in);
                    toReach.setDouble("N_RG1_in", RG1in);

                    entity.setDouble("cbrunofReachNaCl", 0.0);
                }
            }

            for (int a = 0; a < toReachArray.length; a++) {

                toReach = toReachArray[a];
                reachWeight = reachWeightsArray[a];

                double NRD1in = toReach.getDouble("SurfaceN_in");

                reachNRD2in = toReach.getDouble("InterflowN_sum");

                double NRG1in = toReach.getDouble("N_RG1_in");
                double NRG2in = toReach.getDouble("N_RG2_in");

                for (int h = 0; h < NRD2out_h.length; h++) {
                    reachNRD2in = reachNRD2in + NRD2out_h[h] * reachWeight;
                    NRD2out_h[h] = 0;
                }
                NRD1in = NRD1in + NRD1out * reachWeight;
                reachNRD2in += NExcess.getValue();
                NRG1in = NRG1in + NRG1out * reachWeight;
                NRG2in = NRG2in + NRG2out * reachWeight;

                NRD1out = 0;
                NRG1out = 0;
                NRG2out = 0;


                NExcess.setValue(0);
                SurfaceNabs.setValue(0);
                toReach.setDouble("SurfaceN_in", NRD1in);
                InterflowNabs.setValue(NRD2out_h);
                toReach.setDouble("InterflowN_sum", reachNRD2in);
                N_RG1_out.setValue(NRG1out);
                toReach.setDouble("N_RG1_in", NRG1in);
                N_RG2_out.setValue(NRG2out);
                toReach.setDouble("N_RG2_in", NRG2in);

                keinziel = true;


            }

        }
        if (keinziel == false) {
            getModel().getRuntime().println("Current entity ID: " + (int) entity.getDouble("ID") + " has no receiver.");
        }
    }

    public void cleanup() {
    }
}
