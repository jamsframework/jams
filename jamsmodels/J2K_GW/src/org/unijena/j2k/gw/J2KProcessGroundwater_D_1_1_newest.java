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
package org.unijena.j2k.gw;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(title = "J2KGroundwater",
author = "Peter Krause modifications Daniel Varga",
description = "Description")
public class J2KProcessGroundwater_D_1_1_newest extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "attribute area")
    public Attribute.Double area;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "attribute slope")
    public Attribute.Double slope;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "maximum RG1 storage")
    public Attribute.Double maxRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "maximum RG2 storage")
    public Attribute.Double maxRG2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "recision coefficient k RG1")
    public Attribute.Double kRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "recision coefficient k RG2")
    public Attribute.Double kRG2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "actual RG1 storage")
    public Attribute.Double actRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "actual RG2 storage")
    public Attribute.Double actRG2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RG1 inflow")
    public Attribute.Double inRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RG2 inflow")
    public Attribute.Double inRG2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "RG1 outflow")
    public Attribute.Double outRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "RG2 outflow")
    public Attribute.Double outRG2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "RG1 generation")
    public Attribute.Double genRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "RG2 generation")
    public Attribute.Double genRG2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "percolation")
    public Attribute.Double percolation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "gwExcess")
    public Attribute.Double gwExcess;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "portion of percolation to RG1 in l")
    public Attribute.Double pot_RG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "portion of percolation to RG2 in l")
    public Attribute.Double pot_RG2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "maximum soil storage")
    public Attribute.Double maxSoilStorage;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "actual soil storage")
    public Attribute.Double actSoilStorage;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "RG1 correction factor")
    public Attribute.Double gwRG1Fact;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "RG2 correction factor")
    public Attribute.Double gwRG2Fact;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "RG1 RG2 distribution factor")
    public Attribute.Double gwRG1RG2dist;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "capilary rise factor")
    public Attribute.Double gwCapRise;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Length of the border arc between adjacent entities")
    public Attribute.Double gwArcLength;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Distance between adjacent entities")
    public Attribute.Double gwFlowLength;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Downstream hru entity")
    public Attribute.Entity toPoly;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Downstream reach entity")
    public Attribute.Entity toReach;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "estimated hydraulic conductivity in m/d")
    public Attribute.Double Kf_geo;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "estimated Porosity")
    public Attribute.Double Peff;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Thickness of the Aquifer")
    public Attribute.Double aqThickness;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Heigth of the Aquifer Base in m + NN")
    public Attribute.Double baseHeigth;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Groundwater Level")
    public Attribute.Double gwTable;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The current hru entity")
    public Attribute.EntityCollection entities;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Reach Colmation Factor [0;1] = [0% ; 100%]")
    public Attribute.Double alphaC;
    double run_maxRG1, run_maxRG2, run_actRG1, run_actRG2, run_inRG1, run_inRG2, run_outRG1, run_outRG2, run_genRG1, run_genRG2,
            run_k_RG1, run_k_RG2, run_RG1_rec, run_RG2_rec, run_maxSoilStor, run_actSoilStor, run_slope, run_area,
            run_percolation, run_interflow, run_pot_RG1, run_pot_RG2, run_gwExcess, potOutflow,
            run_gwDepthUpper, run_gwDepthLower, run_gwTableUpper, run_gwTableLower, run_Peff, run_Kf_geo, run_gwArcLength, run_aqThickness,
            run_gwFlowLength, run_baseHeigth;
    Attribute.Entity entity;
    /*
     *  Component run stages
     */

    public void init() throws Attribute.Entity.NoSuchAttributeException {
    }

    public void run() throws Attribute.Entity.NoSuchAttributeException {

        Attribute.Entity entity = entities.getCurrent();

        this.run_maxRG2 = maxRG2.getValue();                //[l]
        this.run_actRG2 = actRG2.getValue();                //[l]
        this.run_inRG1 = inRG1.getValue();                  //[l]
        this.run_inRG2 = inRG2.getValue();                  //[l]
        this.run_gwExcess = gwExcess.getValue();            //[l]
        this.run_maxSoilStor = maxSoilStorage.getValue();
        this.run_actSoilStor = actSoilStorage.getValue();
        this.run_percolation = percolation.getValue();

        this.run_outRG1 = 0;
        this.run_outRG2 = 0;
        this.run_genRG1 = 0;
        this.run_genRG2 = 0;

        this.run_k_RG2 = kRG2.getValue();
        this.run_slope = slope.getValue();

        this.run_Peff = Peff.getValue();                                //[-]
        this.run_Kf_geo = Kf_geo.getValue();                            //[m/s]
        this.run_gwFlowLength = gwFlowLength.getValue();
        this.run_gwArcLength = gwArcLength.getValue();
        this.run_aqThickness = aqThickness.getValue();
        this.run_baseHeigth = baseHeigth.getValue();


        this.run_RG2_rec = this.run_k_RG2 * this.gwRG2Fact.getValue();

        if (toPoly.getValue() != null) {
            this.run_gwTableLower = toPoly.getDouble("gwTable");        //[m]
        } else {
            this.run_gwTableLower = toReach.getDouble("waterTable_NN"); //[m]
        }

        
        this.run_gwTableUpper = gwTable.getValue();                     //[m]
        
        
        this.replenishSoilStor();
        this.redistRG1_RG2_in();
        this.updategwTable();
        this.distRG1_RG2();
        
        //this.calcDeepSink();
        //this.calcExpGWout();
        this.calcDarcyGWout();
        this.updategwTable();

        actRG2.setValue(this.run_actRG2);
        outRG2.setValue(this.run_outRG2);
        genRG2.setValue(this.run_genRG2);
        inRG1.setValue(this.run_inRG1);
        inRG2.setValue(this.run_inRG2);
        gwExcess.setValue(this.run_gwExcess);
        actSoilStorage.setValue(this.run_actSoilStor);
        pot_RG2.setValue(this.run_pot_RG2);
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
        if (this.run_actRG2 > deltaSoilStor) {
            double alpha = this.gwCapRise.getValue();
            inSoilStor = (deltaSoilStor) * (1. - Math.exp(-1 * alpha / sat_SoilStor));
        }

        this.run_actSoilStor = this.run_actSoilStor + inSoilStor;
        this.run_actRG2 = this.run_actRG2 - inSoilStor;

        return true;
    }

    private boolean redistRG1_RG2_in() {
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

        if (this.run_inRG2 > 0) {
            double deltaRG2 = this.run_maxRG2 - this.run_actRG2;
            if (this.run_inRG2 <= deltaRG2) {
                this.run_actRG2 = this.run_actRG2 + this.run_inRG2;
                this.run_inRG2 = 0;
            } else {
                this.run_actRG2 = this.run_maxRG2;
                this.run_outRG2 = this.run_outRG2 + this.run_inRG2 - deltaRG2;
                this.run_inRG2 = 0;
            }
        }

        return true;
    }

    private boolean distRG1_RG2() {
        double slope_weight = Math.tan(this.run_slope * (Math.PI / 180.));
        double gradh = 1;//((1 - slope_weight) * 1  // this.gwRG1RG2dist.getValue());

        if (gradh < 0) {
            gradh = 0;
        } else if (gradh > 1) {
            gradh = 1;
        }

        this.run_pot_RG1 = ((1 - gradh) * this.run_percolation);
        this.run_pot_RG2 = (gradh * this.run_percolation);

        this.run_actRG1 = this.run_actRG1 + this.run_pot_RG1;
        this.run_actRG2 = this.run_actRG2 + this.run_pot_RG2;

        /** testing if inflows can be stored in groundwater storages */
        double delta_RG2 = this.run_actRG2 - this.run_maxRG2;
        if (delta_RG2 > 0) {
            /*this.run_actRG1 = this.run_actRG1 + delta_RG2;
            this.run_actRG2 = this.run_maxRG2;
            this.run_pot_RG1 = run_pot_RG1 + delta_RG2;
            this.run_pot_RG2 = run_pot_RG2 - delta_RG2;
            }
            double delta_RG1 = this.run_actRG1 - this.run_maxRG1;
            
            
            if (delta_RG1 > 0) {*/
            this.run_gwExcess = this.run_gwExcess + delta_RG2;  //delta_RG1;
            this.run_actRG2 = this.run_maxRG2;
            //this.run_actRG1 = this.run_maxRG1;
        }
        return true;
    }

    private boolean calcDarcyGWout() throws Attribute.Entity.NoSuchAttributeException {

        // Ausfluss auf RG1 noch alte Variante
        double k_rg1 = 1 / this.run_RG1_rec;
        if (k_rg1 > 1) {
            k_rg1 = 1;
        }
        double rg1_out = k_rg1 * this.run_actRG1;
        this.run_actRG1 = this.run_actRG1 - rg1_out;
        this.run_outRG1 = this.run_outRG1 + rg1_out;

        double rg2_out_m3 = 0;
        double rg2_out = 0;
        double gwDifference = this.run_gwTableUpper - this.run_gwTableLower;

        //Ausfluss aus RG2 mit Darcy-Gleichung
        if (this.run_gwTableUpper >= this.run_gwTableLower) {
            if (toPoly.getValue() != null) {

                    potOutflow = (this.run_area * toPoly.getDouble("area") * this.run_Peff * toPoly.getDouble("Peff") /
                                 (this.run_area * this.run_Peff + toPoly.getDouble("area") * toPoly.getDouble("Peff"))) * gwDifference;

                    double gwFlowArea = this.run_gwArcLength * (this.run_gwTableUpper - this.run_baseHeigth);
                    rg2_out_m3 = gwFlowArea * this.run_Kf_geo * gwDifference / this.run_gwFlowLength;
                    //[m³/s]   = [m²]       * [m/s]           * [m]          / [m]
                    if (rg2_out_m3 > potOutflow) {
                        rg2_out_m3 = potOutflow;
                        // getModel().getRuntime().println("Alles raus!");
                    }

            } else {
                // Randbedingungen bei der Ex- /Infiltration von Vorflutern. Seite 32, 33 David: Grundwasserhydraulik
                // Q = alphaL * deltaH (David: seite 33)
                // H = HRU_H + Reach_H
                // alphaL = (2 * (5 * (H)*kf))/(L + 2 * H / pi * ln(2*H / (pi * U/2

                double reachArea = toReach.getDouble("width") * toReach.getDouble("length");

                potOutflow = (this.run_area * reachArea * this.run_Peff /
                             (this.run_area * this.run_Peff + reachArea)) * gwDifference;

                double H = 5 * ((this.run_gwTableUpper - this.run_baseHeigth) + (this.run_gwTableLower - this.run_baseHeigth));  //Hier Summe der Wasserstände in HRU und Reach
                double U = toReach.getDouble("width") + 2 * (toReach.getDouble("waterTable_NN") - toReach.getDouble("MEAN_H"));                                               //Hier benetzter Umfang
                double alphaL = (2 * H * (run_Kf_geo)) / (this.gwFlowLength.getValue() + (2 * H / Math.PI) * Math.log(2 * H / (Math.PI * U / 2)));
                rg2_out_m3 = 0.5 * alphaL * this.alphaC.getValue() * gwDifference * toReach.getDouble("length");

                if (rg2_out_m3 > potOutflow) {
                    rg2_out_m3 = potOutflow;
                    // getModel().getRuntime().println("Alles raus!");
                }
            }
        } else {
           if (toPoly.getValue() != null) {
               // getModel().getRuntime().println("Groundwater-Table in Receiver-HRU is higher.");
           }else{
               // getModel().getRuntime().println("Water-Table in Reach is higher.");
           }
        }

        rg2_out = rg2_out_m3 * 86400 * 1000;     //[l/d]

        if (this.run_actRG2 >= rg2_out) {
        	this.run_actRG2 = this.run_actRG2 - rg2_out;
        } else {
            rg2_out = this.run_actRG2;
            this.run_actRG2 = 0;
        }
        this.run_outRG2 = this.run_outRG2 + rg2_out;

        this.run_genRG2 = rg2_out;

        return true;
    }

    private double calcPotGWOutflow(double gwDifference) throws Attribute.Entity.NoSuchAttributeException {
        //potOutflow = A1 * (deltaH - H1) = A2 * H2; mit H1 = H2
        potOutflow = this.run_area / (this.run_area + toPoly.getDouble("area")) * gwDifference;
        return potOutflow;
    }

    private boolean updategwTable() throws Attribute.Entity.NoSuchAttributeException {

        double gwVolume;
        /*if (toPoly.getValue() != null) {
            run_area = toPoly.getDouble("area");
            gwVolume = toPoly.getDouble("actRG2") / 1000;

            if (gwVolume < 0) {
                gwVolume = 0;
            }

            gwVolume = gwVolume + (this.run_outRG2 / 1000);

            this.run_gwDepthLower = gwVolume / run_area / toPoly.getDouble("Peff");
            this.run_gwTableLower = run_gwDepthLower + this.run_baseHeigth;
            toPoly.setDouble("gwTable", this.run_gwTableLower);
        }*/
        run_area = this.area.getValue();
        gwVolume = this.run_actRG2 / 1000;

        this.run_gwDepthUpper = gwVolume / run_area / this.run_Peff;
        this.run_gwTableUpper = run_gwDepthUpper + this.run_baseHeigth;
        this.gwTable.setValue(this.run_gwTableUpper);

        return true;
    }
}

/*
 			<component class="org.jams.j2k.s_n.J2KProcessGroundwater" name="J2KProcessGroundwater">
				<jamsvar name="area" provider="HRUContext" providervar="currentEntity.area"/>
				<jamsvar name="slope" provider="HRUContext" providervar="currentEntity.slope"/>
				<jamsvar name="maxRG1" provider="HRUContext" providervar="currentEntity.maxRG1"/>
				<jamsvar name="maxRG2" provider="HRUContext" providervar="currentEntity.maxRG2"/>
				<jamsvar name="kRG1" provider="HRUContext" providervar="currentEntity.RG1_k"/>
				<jamsvar name="kRG2" provider="HRUContext" providervar="currentEntity.RG2_k"/>
				<jamsvar name="actRG1" provider="HRUContext" providervar="currentEntity.actRG1"/>
				<jamsvar name="actRG2" provider="HRUContext" providervar="currentEntity.actRG2"/>
				<jamsvar name="inRG1" provider="HRUContext" providervar="currentEntity.inRG1"/>
				<jamsvar name="inRG2" provider="HRUContext" providervar="currentEntity.inRG2"/>
				<jamsvar name="outRG1" provider="HRUContext" providervar="currentEntity.outRG1"/>
				<jamsvar name="outRG2" provider="HRUContext" providervar="currentEntity.outRG2"/>
				<jamsvar name="genRG1" provider="HRUContext" providervar="currentEntity.genRG1"/>
				<jamsvar name="genRG2" provider="HRUContext" providervar="currentEntity.genRG2"/>
				<jamsvar name="percolation" provider="HRUContext" providervar="currentEntity.percolation"/>
				<jamsvar name="interflow" provider="HRUContext" providervar="currentEntity.outRD2"/>
				<jamsvar name="maxSoilStorage" provider="HRUContext" providervar="currentEntity.maxMPS"/>
				<jamsvar name="actSoilStorage" provider="HRUContext" providervar="currentEntity.actMPS"/>
				<jamsvar name="pot_RG1" provider="HRUContext" providervar="currentEntity.pot_RG1"/>
				<jamsvar name="pot_RG2" provider="HRUContext" providervar="currentEntity.pot_RG2"/>
				<jamsvar name="partint" provider="HRUContext" providervar="currentEntity.partint"/>
				<jamsvar name="gwRG1RG2dist" value="0.8"/>
				<jamsvar name="gwRG1Fact" value="1.0"/>
				<jamsvar name="gwRG2Fact" value="1.0"/>
				<jamsvar name="gwCapRise" value="0.0"/>
			</component>
 */