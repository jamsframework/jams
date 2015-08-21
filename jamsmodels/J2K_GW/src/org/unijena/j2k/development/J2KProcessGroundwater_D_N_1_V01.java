/*
 * J2KProcessGroundwater.java
 * Created on 25. November 2005, 16:54
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
package org.unijena.j2k.development;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(title = "J2KGroundwater",
author = "Peter Krause modifications Daniel Varga",
description = "Description")
public class J2KProcessGroundwater_D_N_1_V01 extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "attribute area")
    public JAMSDouble area;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "attribute slope")
    public JAMSDouble slope;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "maximum RG1 storage")
    public JAMSDouble maxRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "maximum GW storage")
    public JAMSDouble maxGW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "recision coefficient k RG1")
    public JAMSDouble kRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "recision coefficient k GW")
    public JAMSDouble kGW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "actual RG1 storage")
    public JAMSDouble actRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "actual GW storage")
    public JAMSDouble actGW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "RG1 inflow")
    public JAMSDouble inRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "GW inflow")
    public JAMSDouble inGW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "RG1 outflow")
    public JAMSDouble outRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "GW outflow")
    public JAMSDouble outGW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "RG1 generation")
    public JAMSDouble genRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "GW generation")
    public JAMSDouble genGW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "GW saturation")
    public JAMSDouble satGW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "actual GW storage")
    public JAMSDouble pot_actGW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "GW outflow")
    public JAMSDouble pot_outGW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "GW generation")
    public JAMSDouble pot_genGW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "percolation")
    public JAMSDouble percolation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "gwExcess")
    public JAMSDouble gwExcess;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "maximum soil storage")
    public JAMSDouble maxSoilStorage;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "actual soil storage")
    public JAMSDouble actSoilStorage;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "RG1 correction factor")
    public JAMSDouble gwRG1Fact;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "GW correction factor")
    public JAMSDouble gwGWFact;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "RG1 GW distribution factor")
    public JAMSDouble gwRG1GWdist;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "capilary rise factor")
    public JAMSDouble gwCapRise;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Flow width between adjacent entities (Flie?breite)")
    public JAMSDouble gwFlowWidth;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Distance between adjacent entities")
    public JAMSDouble gwFlowLength;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Downstream hru entity")
    public JAMSEntity toPoly;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Downstream reach entity")
    public JAMSEntity toReach;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "estimated hydraulic conductivity in m/d")
    public JAMSDouble Kf_geo;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "estimated Porosity")
    public JAMSDouble Peff;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Thickness of the Aquifer")
    public JAMSDouble aqThickness;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Heigth of the Aquifer Base in m + NN")
    public JAMSDouble baseHeigth;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Groundwater Table")
    public JAMSDouble gwTable;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "potential GW-Level, real GW-Level would be calculated in the GWRouting-module")
    public JAMSDouble pot_gwTable;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "The current hru entity")
    public JAMSEntityCollection entities;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Reach Colmation Factor [0;1] = [0% ; 100%]")
    public JAMSDouble alphaC;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Calculation Factor for each HRU (static geographic variables) for use in GWRouting-module")
    public JAMSDouble calcFactor;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Inflow + Percolation - StorageDifference")
    public JAMSDouble preOutGW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Inflow + Percolation - StorageDifference")
    public JAMSDouble preActGW;

    double run_maxRG1, run_maxGW, run_actRG1, run_actGW, run_inRG1, run_inGW, run_outRG1, run_outGW, run_genRG1, run_genGW, run_satGW,
            run_k_RG1, run_k_GW, run_RG1_rec, run_GW_rec, run_maxSoilStor, run_actSoilStor, run_slope, run_area,
            run_percolation, run_interflow, run_pot_RG1, run_pot_GW, run_gwExcess, maxOutflow,
            run_gwDepthUpper, run_gwDepthLower, run_gwTableUpper, run_gwTableLower, run_Peff, run_Kf_geo, run_gwFlowWidth, run_aqThickness,
            run_gwFlowLength, run_baseHeigth, run_ARPosition, run_cf, old_gwTable, run_pre_outGW, run_pre_actGW, oR,
            actualEntityID;

    
    /*
     *  Component run stages
     */

    public void init() throws JAMSEntity.NoSuchAttributeException {
    }

    public void run() throws JAMSEntity.NoSuchAttributeException {

        Attribute.Entity entity = entities.getCurrent();

        // Percolation wird gelesen (geht spaeter direkt in GW)
        this.run_percolation = percolation.getValue();
        
        // diverse Parameter fuer RG1 (wird noch eingebaut)
        this.run_slope = slope.getValue();
        this.run_inRG1 = inRG1.getValue();                  //[l]
        this.run_gwExcess = gwExcess.getValue();            //[l]
        this.run_maxSoilStor = maxSoilStorage.getValue();
        this.run_actSoilStor = actSoilStorage.getValue();

        // Lesen aller notwendigen Parameter fuer das Grundwasser
        this.run_maxGW = maxGW.getValue();                //[l]
        this.run_actGW = actGW.getValue();                //[l]       
        this.run_inGW = inGW.getValue();                  //[l]
        this.run_k_GW = kGW.getValue();
        this.run_Peff = Peff.getValue();                    //[-]
        this.run_Kf_geo = Kf_geo.getValue();                //[m/s]
        this.run_gwFlowLength = gwFlowLength.getValue();    //[m]
        this.run_gwFlowWidth = gwFlowWidth.getValue();      //[m]
        this.run_aqThickness = aqThickness.getValue();      //[m]
        this.run_baseHeigth = baseHeigth.getValue();        //[m u NN]

        this.actualEntityID = entity.getDouble("ID");

        // Setzen von notwendigen Parametern fuer das Grundwaser
        this.run_outRG1 = 0;
        this.run_genRG1 = 0;

        this.run_outGW = 0;
        this.run_genGW = 0;

        this.pot_actGW.setValue(this.run_actGW);
        this.pot_outGW.setValue(this.run_outGW);    //0
        this.pot_genGW.setValue(this.run_genGW);    //0
        
        // Berechnung des Recession-Coeffizient (wird nicht gebraucht)
        // this.run_GW_rec = this.run_k_GW * this.gwGWFact.getValue();

        // Lesen des Grundwasserstandes vom Unterlieger (HRU oder Reach)
        if (toPoly.getValue() != null) {
            this.run_gwTableLower = toPoly.getDouble("gwTable");        //[m]
        } else {
            this.run_gwTableLower = toReach.getDouble("waterTable_NN"); //[m]
        }

        // Lesen des Grundwasserstandes in der aktuellen HRU
        this.run_gwTableUpper = gwTable.getValue();                     //[m]

        // Zwischenspeichern des "alten" Grundwasserstandes (Zeitpunkt t-1) zu spaeteren Vergleichszwecken
        this.old_gwTable = this.run_gwTableUpper;

        this.redistRG1_GW_in();         // inRG1 und inGW werden zwischen RG1 und GW aufgeteilt

        this.replenishSoilStor();       // kapilarer Aufstieg
        this.distRG1_GW();              // in Abhaengigkeit der Topographie wird das Perkolationswasser zwischen RG1 und GW verteilt
                                        // im Moment:  alles Perkolationswaser geht in den GW

        this.updategwTable();           // das Wasser von t-1  + das percolationswasser wurde verteilt, neuer Wasserstand!


        //this.calcDeepSink();
        //this.calcExpGWout();
        
        this.calcDarcyGWout();          // Berechnung des potentiellen GW-Ausflusses nach DARCY, erstmal ohne Beruecksichtigung 
                                        // weiterer lateraler Zufluesse. Berechnungsbasis bildet die Wasserspiegellagendifferenz
                                        // zum Zeitpunkt t-1
        
        this.updategwTable();           // Berechnung des potentiellen GW-Standes, wenn outGW tatsaechlich abgegeben werde sollte

        this.run_satGW = this.run_actGW / this.run_maxGW ;

        actRG1.setValue(this.run_actRG1);
        outRG1.setValue(this.run_outRG1);
        genRG1.setValue(this.run_genRG1);
        inRG1.setValue(this.run_inRG1);

        //preOutGW.setValue(this.run_pre_outGW);

        // Wenn Unterlieger eine Reach ist (direkte Weitergabe der Berechneten Werte):
        if (entity.getDouble("type") == 3){
            if (toReach.getDouble("ID") == 1124){
                getModel().getRuntime().println("Current entity ID: 1134.");
            }
            actGW.setValue(this.run_actGW);
            inGW.setValue(this.run_inGW);
            outGW.setValue(this.run_outGW);
            genGW.setValue(this.run_genGW);
            gwTable.setValue(this.run_gwTableUpper);

        // wenn Unterlieger eine HRU ist
        }else{
            //preActGW.setValue(this.run_pre_actGW);
            
            //Grundidee: Errechnen eines potentiellen Ausflusses, 
            // der in Abhaengigkeit der Gradienten der beteiligten HRUs im RoutingModul
            // weiter verteilt wird.
            actGW.setValue(this.run_actGW);
            satGW.setValue(this.run_satGW);
            inGW.setValue(this.run_inGW);
            /*outGW.setValue(0);
            genGW.setValue(0);
            gwTable.setValue(this.old_gwTable);*/
            outGW.setValue(run_outGW);
            genGW.setValue(run_genGW);
            gwTable.setValue(this.run_gwTableUpper);
            pot_actGW.setValue(this.run_actGW);
            pot_outGW.setValue(this.run_outGW);
            pot_genGW.setValue(this.run_genGW);
            pot_gwTable.setValue(this.run_gwTableUpper);
            
        }

        gwExcess.setValue(this.run_gwExcess);
        actSoilStorage.setValue(this.run_actSoilStor);
        calcFactor.setValue(run_cf);
    }

    public void cleanup() {
    }

    public boolean replenishSoilStor() {
        double deltaSoilStor = this.run_maxSoilStor - this.run_actSoilStor;
        double sat_SoilStor = 0;
        double inSoilStor = 0;
        if ((this.run_actSoilStor > 0) && (this.run_maxSoilStor > 0)) {
            sat_SoilStor = this.run_actSoilStor / this.run_maxSoilStor;
        } else {
            sat_SoilStor = 0.000001;
        }
        if (this.run_actGW > deltaSoilStor) {
            double alpha = this.gwCapRise.getValue();
            inSoilStor = (deltaSoilStor) * (1. - Math.exp(-1 * alpha / sat_SoilStor));
        }

        this.run_actSoilStor = this.run_actSoilStor + inSoilStor;
        this.run_actGW = this.run_actGW - inSoilStor;

        return true;
    }

    private boolean redistRG1_GW_in() {
        if (this.run_inRG1 > 0) {
            double deltaRG1 = this.run_maxRG1 - this.run_actRG1;
            if (this.run_inRG1 <= deltaRG1) {
                this.run_actRG1 = this.run_actRG1 + this.run_inRG1;
                this.run_inRG1 = 0;
            } else {
                this.run_actRG1 = this.run_maxRG1;
                this.run_outRG1 = this.run_outRG1 + this.run_inRG1 - deltaRG1;
                this.run_inRG1 = 0;
            }
        }

        if (this.run_inGW > 0) {
            double deltaGW = this.run_maxGW - this.run_actGW;
            if (this.run_inGW <= deltaGW) {
                this.run_actGW = this.run_actGW + this.run_inGW;
                //this.run_pre_actGW = run_actGW;
                this.run_inGW = 0;
            } else {
                this.run_actGW = this.run_maxGW;
                this.run_outGW = this.run_outGW + this.run_inGW - deltaGW;
                //this.run_pre_outGW = run_outGW;
                //this.run_pre_actGW = run_actGW;
                this.run_inGW = 0;
            }
        }

        return true;
    }

    private boolean distRG1_GW() {
        double slope_weight = Math.tan(this.run_slope * (Math.PI / 180.));
        double gradh = 1;//((1 - slope_weight) * 1  // this.gwRG1GWdist.getValue());

        if (gradh < 0) {
            gradh = 0;
        } else if (gradh > 1) {
            gradh = 1;
        }

        this.run_pot_RG1 = ((1 - gradh) * this.run_percolation);
        this.run_pot_GW = (gradh * this.run_percolation);

        this.run_actRG1 = this.run_actRG1 + this.run_pot_RG1;
        this.run_actGW = this.run_actGW + this.run_pot_GW;

        /** testing if inflows can be stored in groundwater storages */
        double delta_GW = this.run_actGW - this.run_maxGW;
        if (delta_GW > 0) {
            /*this.run_actRG1 = this.run_actRG1 + delta_GW;
            this.run_actGW = this.run_maxGW;
            this.run_pot_RG1 = run_pot_RG1 + delta_GW;
            this.run_pot_GW = run_pot_GW - delta_GW;
            }
            double delta_RG1 = this.run_actRG1 - this.run_maxRG1;
            
            
            if (delta_RG1 > 0) {*/
            this.run_gwExcess = this.run_gwExcess + delta_GW;  //delta_RG1;
            this.run_actGW = this.run_maxGW;
            //this.run_actRG1 = this.run_maxRG1;
        }
        this.run_pre_outGW = this.run_outGW;
        this.run_pre_actGW = this.run_actGW;

        return true;
    }

    private boolean calcDarcyGWout() throws JAMSEntity.NoSuchAttributeException {

        // Ausfluss auf RG1 noch alte Variante
        double k_rg1 = 1 / this.run_RG1_rec;
        if (k_rg1 > 1) {
            k_rg1 = 1;
        }
        double rg1_out = k_rg1 * this.run_actRG1;
        this.run_actRG1 = this.run_actRG1 - rg1_out;
        this.run_outRG1 = this.run_outRG1 + rg1_out;

        double GW_out_m3 = 0;
        double GW_out = 0;

        double gwDifference = this.run_gwTableUpper - this.run_gwTableLower;

        //Ausfluss aus GW mit Darcy-Gleichung
        //if (this.run_gwTableUpper >= this.run_gwTableLower) {
            if (toPoly.getValue() != null) {
                    if (gwDifference < 0) {
                        //getModel().getRuntime().println("Negativer Gradient zur HRU! Entity:" + actualEntityID);
                        gwDifference = 0;

                    }
                        double A1 = this.run_area * this.run_Peff;                          //[m²]
                        double A2 = toPoly.getDouble("area") * toPoly.getDouble("Peff");    //[m²]

                        double newHeight = gwDifference * (A1 / (A1 + A2));                  //[m]

                        maxOutflow = (gwDifference - newHeight) * A1;                       //[m³]

                    //potOutflow = (this.run_area * toPoly.getDouble("area") * this.run_Peff * toPoly.getDouble("Peff") /
                    //             (this.run_area * this.run_Peff + toPoly.getDouble("area") * toPoly.getDouble("Peff"))) * gwDifference;

                        double gwFlowArea = this.run_gwFlowWidth * (this.run_gwTableUpper - this.run_baseHeigth);   //[m²]
                        run_cf = gwFlowArea * this.run_Kf_geo / this.run_gwFlowLength;                              //[m²/s]

                        GW_out_m3 = run_cf * (gwDifference);  //[m³/s]

                        if (GW_out_m3 > maxOutflow) {
                            GW_out_m3 = maxOutflow;
                            //getModel().getRuntime().println("Alles raus!" + actualEntityID);
                        
                }
            } else {
                // Randbedingungen bei der Ex- /Infiltration von Vorflutern. Seite 32, 33 David: Grundwasserhydraulik
                // Q = alphaL * deltaH (David: seite 33)
                // H = HRU_H + Reach_H
                // alphaL = (2 * (5 * (H)*kf))/(L + 2 * H / pi * ln(2*H / (pi * U/2

                double reachArea = toReach.getDouble("width") * toReach.getDouble("length");
                if (gwDifference < 0) {
                    //getModel().getRuntime().println("Negativer Gradient! Entity:" + actualEntityID);
                    gwDifference = 0;
                }
                double A1 = this.run_area * this.run_Peff;
                double A2 = reachArea;

                double newHeight = gwDifference * (A1 /(A1 + A2));

                maxOutflow = (gwDifference - newHeight) * A1;

                //double potOutflow = (this.run_area * reachArea * this.run_Peff /
                //             (this.run_area * this.run_Peff + reachArea)) * gwDifference;

 /*             double H = 5 * ((this.run_gwTableUpper - this.run_baseHeigth) + (this.run_gwTableLower - this.run_baseHeigth));  //Hier Summe der Wasserst?nde in HRU und Reach
                double U = toReach.getDouble("width") + 2 * (toReach.getDouble("waterTable_NN") - toReach.getDouble("MEAN_H"));                                               //Hier benetzter Umfang
                double alphaL = (2 * H * (run_Kf_geo)) / (this.gwFlowLength.getValue() + (2 * H / Math.PI) * Math.log(2 * H / (Math.PI * U / 2)));
                //GW_out_m3 = 0.5 * alphaL * this.alphaC.getValue() * (gwDifference - newHeight) * this.run_gwFlowWidth; //toReach.getDouble("length");

                GW_out_m3 = 0.5 * alphaL * this.alphaC.getValue() * gwDifference * this.run_gwFlowWidth; //toReach.getDouble("length");
*/
                //DARCY:
                double gwFlowArea = this.run_gwFlowWidth * (gwDifference);
                run_cf = gwFlowArea * this.run_Kf_geo / this.run_gwFlowLength;

                GW_out_m3 = this.alphaC.getValue() * run_cf * gwDifference;

                //Methode nach MILES 1985
/*              if (gwDifference < 0) {
                getModel().getRuntime().println("Negativer Gradient! Entity:" + actualEntityID);
                gwDifference = 0;
                }
                //Nahbereich: Zone I... etwa Gerinnebreite
                double riverBase = toReach.getDouble("MEAN_H");
                double riverDepth = toReach.getDouble("waterTable_NN") - riverBase;
                double aqUnderRiver = riverBase - this.run_baseHeigth;
                if (aqUnderRiver < 1){
                getModel().getRuntime().println("Aquiferbasis < 1 m! Reach:" + toReach.getDouble("ID"));
                }
                double C = 5.0 *((0.5 * toReach.getDouble("width")) + riverDepth)/(aqUnderRiver + riverDepth);

                GW_out_m3 = run_Kf_geo * gwDifference * C;
*/

                if (GW_out_m3 > maxOutflow) {
                    GW_out_m3 = maxOutflow;
                    //getModel().getRuntime().println("Alles raus in Reach!"  + actualEntityID);
                }
                if (GW_out_m3 < 0) {
                    GW_out_m3 = 0;
                   //getModel().getRuntime().println("Negativer Ausfluss! Entity:" + actualEntityID);
                
               }
            }
        /*} else {
           if (toPoly.getValue() != null) {
               getModel().getRuntime().println("Groundwater-Table in Receiver-HRU is higher. " + toPoly.getDouble("ID"));
           }else{
               getModel().getRuntime().println("Water-Table in Reach is higher. " + toReach.getDouble("ID"));
           }
        }*/

        GW_out = GW_out_m3 * 1000;     //[l/d]
       
        if (this.run_actGW >= GW_out) {
        	this.run_actGW = this.run_actGW - GW_out;
        } else {
            GW_out = this.run_actGW;
            this.run_actGW = 0;
        }

        this.run_outGW = this.run_outGW + GW_out;

        this.run_genGW = GW_out;

        return true;
    }

    private double calcMaxGWOutflow(double gwDifference) throws JAMSEntity.NoSuchAttributeException {
        //potOutflow = A1 * (deltaH - H1) = A2 * H2; mit H1 = H2
        maxOutflow = this.run_area / (this.run_area + toPoly.getDouble("area")) * gwDifference;
        return maxOutflow;
    }

    private boolean updategwTable() throws JAMSEntity.NoSuchAttributeException {

        double gwVolume;
        /*if (toPoly.getValue() != null) {
            run_area = toPoly.getDouble("area");
            gwVolume = toPoly.getDouble("actGW") / 1000;

            if (gwVolume < 0) {
                gwVolume = 0;
            }

            gwVolume = gwVolume + (this.run_outGW / 1000);

            this.run_gwDepthLower = gwVolume / run_area / toPoly.getDouble("Peff");
            this.run_gwTableLower = run_gwDepthLower + this.run_baseHeigth;
            toPoly.setDouble("pot_gwTable", this.run_gwTableLower);
        }*/
        run_area = this.area.getValue();    //[m²]
        gwVolume = this.run_actGW / 1000;   

        this.run_gwDepthUpper = gwVolume / run_area / this.run_Peff;
        this.run_gwTableUpper = run_gwDepthUpper + this.run_baseHeigth;
        //this.pot_gwTable.setValue(this.run_gwTableUpper);

        return true;
    }
}

/*
 			<component class="org.jams.j2k.s_n.J2KProcessGroundwater" name="J2KProcessGroundwater">
				<jamsvar name="area" provider="HRUContext" providervar="currentEntity.area"/>
				<jamsvar name="slope" provider="HRUContext" providervar="currentEntity.slope"/>
				<jamsvar name="maxRG1" provider="HRUContext" providervar="currentEntity.maxRG1"/>
				<jamsvar name="maxGW" provider="HRUContext" providervar="currentEntity.maxGW"/>
				<jamsvar name="kRG1" provider="HRUContext" providervar="currentEntity.RG1_k"/>
				<jamsvar name="kGW" provider="HRUContext" providervar="currentEntity.GW_k"/>
				<jamsvar name="actRG1" provider="HRUContext" providervar="currentEntity.actRG1"/>
				<jamsvar name="actGW" provider="HRUContext" providervar="currentEntity.actGW"/>
				<jamsvar name="inRG1" provider="HRUContext" providervar="currentEntity.inRG1"/>
				<jamsvar name="inGW" provider="HRUContext" providervar="currentEntity.inGW"/>
				<jamsvar name="outRG1" provider="HRUContext" providervar="currentEntity.outRG1"/>
				<jamsvar name="outGW" provider="HRUContext" providervar="currentEntity.outGW"/>
				<jamsvar name="genRG1" provider="HRUContext" providervar="currentEntity.genRG1"/>
				<jamsvar name="genGW" provider="HRUContext" providervar="currentEntity.genGW"/>
				<jamsvar name="percolation" provider="HRUContext" providervar="currentEntity.percolation"/>
				<jamsvar name="interflow" provider="HRUContext" providervar="currentEntity.outRD2"/>
				<jamsvar name="maxSoilStorage" provider="HRUContext" providervar="currentEntity.maxMPS"/>
				<jamsvar name="actSoilStorage" provider="HRUContext" providervar="currentEntity.actMPS"/>
				<jamsvar name="pot_RG1" provider="HRUContext" providervar="currentEntity.pot_RG1"/>
				<jamsvar name="pot_GW" provider="HRUContext" providervar="currentEntity.pot_GW"/>
				<jamsvar name="partint" provider="HRUContext" providervar="currentEntity.partint"/>
				<jamsvar name="gwRG1GWdist" value="0.8"/>
				<jamsvar name="gwRG1Fact" value="1.0"/>
				<jamsvar name="gwGWFact" value="1.0"/>
				<jamsvar name="gwCapRise" value="0.0"/>
			</component>
 */