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
package org.jams.j2k.s_salt;

import org.jams.j2k.s_n.*;
import jams.data.*;
import jams.model.*;
import jams.JAMS;

/**
 *
 * @author Peter Krause & Manfred Fink
 */
@JAMSComponentDescription(title = "J2KNaClMultiRoutinglayer_cb",
author = "Peter Krause, Manfred Fink & Björn Pfennig",
description = "Passes the NaCl output of the entities as N input to the respective reach or unit, including contour banks")
public class J2KNaClMultiRoutinglayer_cb extends JAMSComponent {

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
    public Attribute.Double SurfaceNaCl_in;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RD2 N inflow in kgN")
    public Attribute.DoubleArray InterflowNaCl_in;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RG1 N inflow in kgN")
    public Attribute.Double NaCl_RG1_in;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RG2 N inflow in kgN")
    public Attribute.Double NaCl_RG2_in;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RD1 N outflow in kgN")
    public Attribute.Double SurfaceNaClabs;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RD2 N outflow in kgN")
    public Attribute.DoubleArray InterflowNaClabs;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RG1 N outflow in kgN")
    public Attribute.Double NaCl_RG1_out;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RG2 N outflow in kgN")
    public Attribute.Double NaCl_RG2_out;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "gwExcess")
    public Attribute.Double NaClExcess;
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


        double NaClRD1out = SurfaceNaClabs.getValue();
        double[] NaClRD2out_h = InterflowNaClabs.getValue();
        double NaClRG1out = NaCl_RG1_out.getValue();
        double NaClRG2out = NaCl_RG2_out.getValue();

        double reachNaClRD2in = 0;

        if (toPolyArray.length > 0) {

            for (int a = 0; a < toPolyArray.length; a++) {

                if (toPolyArray[a] != null) {

                    toPoly = toPolyArray[a];
                    
                    polyWeight = polyWeightsArray[a];

                    double[] recDepth = ((Attribute.DoubleArray) toPoly.getObject("depth_h")).getValue();
                    int recHors = recDepth.length;


                    double[] NaClRD2in_h = new double[recHors];

                    double NaClRD1in = toPoly.getDouble("SurfaceNaCl");
                    double[] rdArNaCl = ((Attribute.DoubleArray) toPoly.getObject("InterflowNaCl_in")).getValue();

                    double NaClRG1in = toPoly.getDouble("NaCl_RG1_in");
                    double NaClRG2in = toPoly.getDouble("NaCl_RG2_in");
                    for (int j = 0; j < recHors; j++) {
                        NaClRD2in_h[j] = rdArNaCl[j];
                        for (int i = 0; i < srcHors; i++) {
                            NaClRD2in_h[j] = NaClRD2in_h[j] + (NaClRD2out_h[i] / recHors) * polyWeight;
                        }
                    }


                   
                    NaClRD2in_h[recHors - 1] += NaClExcess.getValue();

                    NaClRD1in = NaClRD1in + NaClRD1out * polyWeight;
                    NaClRG1in = NaClRG1in + NaClRG1out * polyWeight;
                    NaClRG2in = NaClRG2in + NaClRG2out * polyWeight;

                    

                    Attribute.DoubleArray rdANaCl = (Attribute.DoubleArray) toPoly.getObject("InterflowNaCl_in");
                    rdANaCl.setValue(NaClRD2in_h);
                    toPoly.setDouble("SurfaceNaCl_in", NaClRD1in);
                    toPoly.setObject("InterflowNaCl_in", rdANaCl);
                    toPoly.setDouble("NaCl_RG1_in", NaClRG1in);
                    toPoly.setDouble("NaCl_RG2_in", NaClRG2in);

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

                    double RD1in = toReach.getDouble("SurfaceNaCl_in");
                    double RD2in = toReach.getDouble("InterflowNaCl_sum");
                    double RG1in = toReach.getDouble("NaCl_RG1_in");

                    RD1in += anteileKomponentenCB[0] * abflussCBReach_akt;
                    RD2in += anteileKomponentenCB[1] * abflussCBReach_akt;
                    RG1in += anteileKomponentenCB[2] * abflussCBReach_akt;
                    
                    
                    
                    toReach.setDouble("SurfaceNaCl_in", RD1in);
                    toReach.setDouble("InterflowNaCl_sum", RD2in);
                    toReach.setDouble("NaCl_RG1_in", RG1in);

                    entity.setDouble("cbrunofReachNaCl", 0.0);
                }
            }

            for (int a = 0; a < toReachArray.length; a++) {

                toReach = toReachArray[a];
                reachWeight = reachWeightsArray[a];

                double NaClRD1in = toReach.getDouble("SurfaceNaCl_in");

                reachNaClRD2in = toReach.getDouble("InterflowNaCl_sum");

                double NaClRG1in = toReach.getDouble("NaCl_RG1_in");
                double NaClRG2in = toReach.getDouble("NaCl_RG2_in");

                for (int h = 0; h < NaClRD2out_h.length; h++) {
                    reachNaClRD2in = reachNaClRD2in + NaClRD2out_h[h] * reachWeight;
                    NaClRD2out_h[h] = 0;
                }
                NaClRD1in = NaClRD1in + NaClRD1out * reachWeight;
                reachNaClRD2in += NaClExcess.getValue();
                NaClRG1in = NaClRG1in + NaClRG1out * reachWeight;
                NaClRG2in = NaClRG2in + NaClRG2out * reachWeight;



                
                toReach.setDouble("SurfaceNaCl_in", NaClRD1in);
               
                toReach.setDouble("InterflowNaCl_sum", reachNaClRD2in);
           
                toReach.setDouble("NaCl_RG1_in", NaClRG1in);
                
                toReach.setDouble("NaCl_RG2_in", NaClRG2in);

                keinziel = true;


            }

        }
        
        NaClRD1out = 0;
        NaClRG1out = 0;
        NaClRG2out = 0;
        for (int i = 0; i < srcHors; i++) {
                        NaClRD2out_h[i] = 0;
                    }
        NaClExcess.setValue(0);
        SurfaceNaClabs.setValue(0);
        InterflowNaClabs.setValue(NaClRD2out_h);
        NaCl_RG1_out.setValue(NaClRG1out);
        NaCl_RG2_out.setValue(NaClRG2out);
        
        if (keinziel == false) {
            getModel().getRuntime().println("Current entity ID: " + (int) entity.getDouble("ID") + " has no receiver.");
        }
    }

    public void cleanup() {
    }
}
