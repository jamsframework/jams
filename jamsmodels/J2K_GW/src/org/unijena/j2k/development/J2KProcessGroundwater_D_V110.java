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

import org.unijena.j2k.gw.*;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(title = "J2KProcessGroundwater_D",
        author = "Daniel Varga",
        description = "Component for Calculation of Groundwater Flow with the DARCY-Method",
        version="1.1_0",
        date="2011-01-12"
        )
        /*
         * changes: modified calculation of unsaturated zone, with van-genuchten-equation
         */

public class J2KProcessGroundwater_D_V110 extends JAMSComponent {

    /*
     *  Component variables
     */


    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "The current hru entity"
        )
        public JAMSEntityCollection entities;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Downstream hru entity"
        )
        public JAMSEntity toPoly;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Downstream reach entity"
        )
        public JAMSEntity toReach;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "attribute area",
        unit = "m^2",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble area;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "attribute slope",
        unit = "deg",
        lowerBound = 0,
        upperBound =90
        )
        public JAMSDouble slope;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "maximum GW storage",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble maxGW;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "actual TZ storage",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble actTZ;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "actual GW storage",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble actGW;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "TZ inflow",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble inTZ;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "GW inflow",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble inGW;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "TZ outflow",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble outTZ;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "GW outflow",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble outGW;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "TZ generation",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY)
        public JAMSDouble genTZ;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "GW generation",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble genGW;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "GW saturation",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble satGW;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "TZ saturation",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble satTZ;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "potential GW storage",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble pot_actGW;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "potential GW outflow",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble pot_outGW;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "potential GW generation",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble pot_genGW;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "percolation",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble percolation;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "gwExcess",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble gwExcess;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "maximum soil storage",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble maxSoilStorage;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "actual soil storage",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble actSoilStorage;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "capilary rise factor",
        unit = "-",
        lowerBound = 0,
        upperBound = 1
        )
        public JAMSDouble gwCapRise;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Flow width between adjacent entities (Fließbreite)",
        unit = "m",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble gwFlowWidth;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Distance between adjacent entities",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble gwFlowLength;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "estimated hydraulic conductivity in m/d",
        unit = "m/d",
        lowerBound= 0.0,
        upperBound = 8640
        )
        public JAMSDouble Kf_geo;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "estimated Porosity",
        unit = "-",
        lowerBound= 0.0,
        upperBound = 1.0
        )
        public JAMSDouble Peff;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Thickness of the Aquifer",
        unit = "m",
        lowerBound= 0,
        upperBound = 100
        )
        public JAMSDouble aqThickness;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Heigth of the aquifer base in m a.s.l.",
        unit = "m", //[m ü NN] / [a.s.l.]
        lowerBound= -422,
        upperBound = 8848
        )
        public JAMSDouble baseHeigth;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
        description = "Groundwater Table",
        unit = "m", //[m ü NN] / [a.s.l.]
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble gwTable;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "potential GW-Level, real GW-Level would be calculated in the GWRouting-module",
        unit = "m", //[m ü NN] / [a.s.l.]
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble pot_gwTable;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Reach Colmation Factor [0;1] = [0% ; 100%]",
        unit = "-",
        lowerBound = 0,
        upperBound = 1
        )
    public JAMSDouble alphaC;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Calculation Factor for each HRU (static geographic variables) for use in GWRouting-module",
        unit = "m^2/d",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble calcFactor;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "amount of water lost by deep sink in l/d",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble deepSinkW;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "K-Value for the deepsink in cm/d",
        unit = "cm/d",
        lowerBound= 0.0,
        upperBound = 864000
        )
        public JAMSDouble kSink;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Calibration Factor for K-Value for deepsink",
        defaultValue = "0",
        unit = "-",
        lowerBound= 0.0,
        upperBound = 1000
        )
        public JAMSDouble deepSinkFactor;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "elevation of HRU",
        unit = "m", //[m ü NN] / [a.s.l.]
        lowerBound= -422,
        upperBound = 8848
        )
        public JAMSDouble elevation;

   @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Flurabstand",
        unit = "m",
        lowerBound= 0,
        upperBound = 100
        )
        public JAMSDouble FlurAbstand;

   @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Distribution Factor for Lateral and Vertical flow in the Transfer Zone",
        unit = "-",
        lowerBound = 0,
        upperBound = 10
        )
        public JAMSDouble gwLatVertTZ;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "van Genuchten parameter for unsaturated K-Value",
        unit = "-",
        lowerBound = 0,
        upperBound = 100
        )
        public JAMSDouble gwKfVG;

        double run_maxTZ, run_maxGW,
           run_actTZ, run_actGW,
           run_inTZ,  run_inGW,
           run_outTZ, run_outGW,
           run_genTZ, run_genGW,
           run_potLatTZ, run_potVertTZ,
           run_satTZ, run_satGW,
           run_pot_TZ, run_pot_GW,
           run_pre_outGW, run_pre_actGW,
           run_k_TZ,
           run_TZ_rec,
           run_TZlat_rec, run_TZvert_rec,
           run_GW_rec,
           run_maxSoilStor, run_actSoilStor,
           run_slope, run_area,
           run_percolation, run_interflow,
           run_gwExcess, maxOutflow,
           run_gwDepthUpper, run_gwDepthLower,
           run_gwTableUpper, run_gwTableLower,
           run_Peff, run_Kf_geo,
           run_gwFlowWidth, run_gwFlowLength,
           run_aqThickness,
           run_baseHeigth,
           run_Flurabstand, run_elevation,
           run_ARPosition, oR,

           run_cf, old_gwTable,

           //deepSink
           run_deepSinkW, run_kSink, run_deepSinkFactor,

           actualEntityID, run_upperBorder;
    Attribute.Entity entity;



    /*
     *  Component run stages
     */

    public void init() throws JAMSEntity.NoSuchAttributeException {
    }

    public void run() throws JAMSEntity.NoSuchAttributeException {

        entity = entities.getCurrent();

        // Percolation wird gelesen (geht spaeter direkt in GW)
        this.run_percolation = percolation.getValue();
        
        // diverse Parameter fuer TZ (wird noch eingebaut)
        this.run_slope = slope.getValue();
        this.run_inTZ = inTZ.getValue();                  //[l]
        this.run_gwExcess = gwExcess.getValue();          //[l]
        this.run_maxSoilStor = maxSoilStorage.getValue();
        this.run_actSoilStor = actSoilStorage.getValue();

        // Lesen aller notwendigen Parameter fuer das Grundwasser
        this.run_maxGW = maxGW.getValue();                //[l]
        this.run_actGW = actGW.getValue();                //[l]       
        this.run_inGW = inGW.getValue();                  //[l]
        this.run_Peff = Peff.getValue();                  //[-]
        this.run_Kf_geo = Kf_geo.getValue();              //[m/d]

        // deepsink
        this.run_deepSinkFactor = deepSinkFactor.getValue();
        this.run_kSink = kSink.getValue() / 100;                  //[cm/d] --> [m/d]

        // diverse Parameter fuer TZ (wird noch eingebaut)
        this.run_upperBorder = entity.getDouble("elevation");// - entity.getDouble("depth");


        this.run_gwFlowLength = gwFlowLength.getValue();    //[m]
        this.run_gwFlowWidth = gwFlowWidth.getValue();      //[m]
        this.run_aqThickness = aqThickness.getValue();      //[m]
        this.run_baseHeigth = baseHeigth.getValue();        //[m u NN]
        this.run_elevation = elevation.getValue();

        this.actualEntityID = entity.getDouble("ID");

        // Setzen von notwendigen Parametern fuer das Grundwaser
        this.run_outTZ = 0;
        this.run_genTZ = 0;

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

        this.redistTZ_GW_in();         // inTZ und inGW werden zwischen TZ und GW aufgeteilt

        this.replenishSoilStor();       // kapilarer Aufstieg

        this.calcTransferZone();

        //this.distTZ_GW();              // in Abhaengigkeit der Topographie wird das Perkolationswasser zwischen TZ und GW verteilt
                                        // im Moment:  alles Perkolationswaser geht in den GW

        this.updategwTable();           // das Wasser von t-1  + das percolationswasser wurde verteilt, neuer Wasserstand!


        //Versickern ins tiefe Grundwasser wird ermöglicht, wenn deepSinkFactor > 0 ist
        if (deepSinkFactor.getValue() > 0){
            this.calcDeepSink();
            this.updategwTable();       // neuer Wasserstand nach DeepSink-Berechnung
        } else {
            this.run_deepSinkW = 0;
        }
        
        this.calcDarcyGWout();          // Berechnung des potentiellen GW-Ausflusses nach DARCY, erstmal ohne Beruecksichtigung 
                                        // weiterer lateraler Zufluesse. Berechnungsbasis bildet die Wasserspiegellagendifferenz
                                        // zum Zeitpunkt t-1
        
        this.updategwTable();           // Berechnung des potentiellen GW-Standes, wenn outGW tatsaechlich abgegeben werde sollte

        this.run_satGW = this.run_actGW / this.run_maxGW ;

        actTZ.setValue(this.run_actTZ);
        outTZ.setValue(this.run_outTZ);
        genTZ.setValue(this.run_genTZ);
        inTZ.setValue(this.run_inTZ);
        satTZ.setValue(this.run_satTZ);

        //preOutGW.setValue(this.run_pre_outGW);

        // Wenn Unterlieger eine Reach ist (direkte Weitergabe der Berechneten Werte):
        if (entity.getDouble("type") == 3){
            if (toReach.getDouble("ID") == 1134){
                getModel().getRuntime().println("Current entity ID: 1134.");
            }

        // wenn Unterlieger eine HRU ist
        }else{
            //preActGW.setValue(this.run_pre_actGW);
            /*outGW.setValue(0);
            genGW.setValue(0);
            gwTable.setValue(this.old_gwTable);*/


            //Grundidee: Errechnen eines potentiellen Ausflusses, 
            // der in Abhaengigkeit der Gradienten der beteiligten HRUs im RoutingModul
            // weiter verteilt wird.
            pot_actGW.setValue(this.run_actGW);
            pot_outGW.setValue(this.run_outGW);
            pot_genGW.setValue(this.run_genGW);
            pot_gwTable.setValue(this.run_gwTableUpper);
            
        }
        satGW.setValue(this.run_satGW);
        FlurAbstand.setValue(this.run_Flurabstand);

        actGW.setValue(this.run_actGW);
        inGW.setValue(this.run_inGW);
        outGW.setValue(this.run_outGW);
        genGW.setValue(this.run_genGW);
        gwTable.setValue(this.run_gwTableUpper);
        deepSinkW.setValue(this.run_deepSinkW);
        gwExcess.setValue(this.run_gwExcess);
        actSoilStorage.setValue(this.run_actSoilStor);
        calcFactor.setValue(run_cf);
    }

    public void cleanup() {
    }

    public boolean calcTransferZone(){

        double TZThickness = this.run_upperBorder - this.run_gwTableUpper;

//Test: Grundwasserspiegellage im Boden?
        if (TZThickness <= 0){
            //ja? dann keine Transferzone vorhanden, TZ=0

            this.run_actGW = this.run_actGW + this.run_percolation + this.run_actTZ;    //damit wird das Wasser aus T-1, dass sich in der Transferzone befunden hat an das Grundwasser übergeben
            this.run_actTZ = 0;
            this.run_outTZ = 0;
            this.run_genTZ = 0;
            this.run_satGW = this.run_actGW / this.run_maxGW;

        }else{
             //Berechnung der maximal möglichen Speicherinhalt der Transferzone (zwischen Boden und Grundwasserspiegellage)
            this.run_maxTZ = TZThickness * run_area * run_Peff * 1000;  //[l]

            //Übergabe des Perkolatioswassers an den TZ
            this.run_actTZ = this.run_actTZ + this.run_percolation;

            // ACHTUNG! Wenn TZ voll, dann ist er eigentlich nicht existent, weil er komplett zur gesättigten Zone zugerechnet werden müsste!
            /** testing if inflows can be stored in groundwater storages */
            double delta_TZ = this.run_actTZ - this.run_maxTZ;
            if(delta_TZ > 0){
                this.run_gwExcess = this.run_gwExcess + delta_TZ;
                this.run_actTZ = this.run_maxTZ;
            }
            this.run_satTZ = this.run_actTZ / this.run_maxTZ;


            // Aufspaltung des gespeicherten Wassers der Transferzone nach lateralen und vetrtikal mobilem Wasser
            // Lateraler Ausflus durch Kalibrierungsfaktor gwLatVertTZ möglich, 0 perkolationswasser nur vertikal, >0 lateraler Fluss möglich
            double slope_weight = Math.tan(this.run_slope * (Math.PI / 180.));
            double gradh = ((1 - slope_weight) * this.gwLatVertTZ.getValue());

            if(gradh < 0)
                gradh = 0;
            else if(gradh > 1)
                gradh = 1;

            this.run_potLatTZ = ((1 - gradh) * this.run_actTZ);
            this.run_potVertTZ = (gradh * this.run_actTZ);


            //unsaturated K-value by van Genuchten (1980, 2005)
            double m = 1-1/this.gwKfVG.getValue();
            double Kf_unsat = this.run_Kf_geo * Math.pow(this.run_satTZ, 0.5)* Math.pow(1-Math.pow(1-Math.pow(this.run_satTZ, 1/m),m),2);

            // Vertikaler Ausfluss
            // Perkolationswiderstand durch Kf_geo, Fließlänge (Dicke der Transferzone) und Kalibrierfaktor: gwTZVertFact
            // Q = k * A * grad
            
            run_pot_GW = Kf_unsat * this.run_area * this.run_satTZ / TZThickness;

            if (run_pot_GW > this.run_potVertTZ){
                run_pot_GW = run_potVertTZ;
            }

            this.run_actTZ = this.run_actTZ - this.run_pot_GW;
            this.run_satTZ = this.run_actTZ / this.run_maxTZ;
            this.run_actGW = this.run_actGW + this.run_pot_GW;


            /** testing if inflows can be stored in groundwater storages */
            /*double delta_GW = this.run_actGW - this.run_maxGW;
            if(delta_GW > 0){
                this.run_gwExcess = this.run_gwExcess + delta_GW;
                this.run_actGW = this.run_maxGW;
            }*/

            this.run_satGW = this.run_actGW / this.run_maxGW;

            // Lateraler Ausfluss aus TZ
            // Rückhaltewiderstand für Lateralen Ausfluss: gwTZLatFact = run_TZlat_rec

            /*double k_TZ = 1 / this.run_TZlat_rec;
            if (k_TZ > 1) {
                k_TZ = 1;
            }*/

            // Q = k * A * grad

            //double TZ_out = k_TZ * this.run_actTZ;
            //double TZ_out = this.run_Kf_geo * this.run_slope * this.gwTZLatFact.getValue() * TZThickness * this.run_gwFlowWidth * 1000 * this.run_satTZ;
            double TZ_out = Kf_unsat * this.run_slope * TZThickness * this.run_gwFlowWidth * this.run_satTZ;

            if (TZ_out > this.run_potLatTZ){
                TZ_out = run_potLatTZ;
            }

            this.run_actTZ = this.run_actTZ - TZ_out;
            this.run_satTZ = this.run_actTZ / this.run_maxTZ;

            this.run_outTZ = this.run_outTZ + TZ_out;
        }
        this.run_percolation = 0;
        //dann Minderung der MaxLPS und SatLPS im Bodenmodul für nächsten Zeitschnitt notwendig... Test auch nach der Routine calcGWDarcy...

        return true;
    }

    private boolean calcDeepSink(){
        // Abgabe eines bestimmten Wasservolumens, abhängig vom Durchlässigkeitsbeiwert der unterliegenden Schicht (Aquitarde)
        // und in Abhängigkeit eines kalibrierbaren Gradienten, der durch die Grundwasserspiegellage (Füllung) der HRU bestimmt wird.
        // durch kSink besteht die Möglichkeit unterschiedliche Aquitarden unterhalb des Aquifers zu definieren

        this.run_deepSinkW = this.run_area * this.run_kSink * this.run_gwTableUpper * this.run_deepSinkFactor * 1000; //[l/d]
        
        // hier wird überprüft, ob deepsink innerhalb von I = [0; actGW] ist
        this.run_deepSinkW = Math.min(this.run_deepSinkW,this.run_actGW);
        this.run_deepSinkW = Math.max(this.run_deepSinkW, 0);

        // Berechnung der aktuellen Speicherfüllung und der Sättigung
        this.run_actGW = this.run_actGW - this.run_deepSinkW;
        this.run_satGW = this.run_actGW / this.run_maxGW ;

        return true;
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

    private boolean redistTZ_GW_in() {
        if (this.run_inTZ > 0) {
            double deltaTZ = this.run_maxTZ - this.run_actTZ;
            if (this.run_inTZ <= deltaTZ) {
                this.run_actTZ = this.run_actTZ + this.run_inTZ;
                this.run_inTZ = 0;
            } else {
                this.run_actTZ = this.run_maxTZ;
                this.run_outTZ = this.run_outTZ + this.run_inTZ - deltaTZ;
                this.run_inTZ = 0;
            }
        }

        if (this.run_inGW > 0) {
            double deltaGW = this.run_maxGW - this.run_actGW;
            //if (this.run_inGW <= deltaGW) {
                this.run_actGW = this.run_actGW + this.run_inGW;
                //this.run_pre_actGW = run_actGW;
                this.run_inGW = 0;
            /*} else {
                this.run_actGW = this.run_maxGW;
                this.run_outGW = this.run_outGW + this.run_inGW - deltaGW;
                //this.run_pre_outGW = run_outGW;
                //this.run_pre_actGW = run_actGW;
                this.run_inGW = 0;
            }*/
        }

        return true;
    }

    private boolean distTZ_GW() {
        double slope_weight = Math.tan(this.run_slope * (Math.PI / 180.));
        double gradh = 1;//((1 - slope_weight) * 1  // this.gwTZGWdist.getValue());

        if (gradh < 0) {
            gradh = 0;
        } else if (gradh > 1) {
            gradh = 1;
        }

        this.run_pot_TZ = ((1 - gradh) * this.run_percolation);
        this.run_pot_GW = (gradh * this.run_percolation);

        this.run_actTZ = this.run_actTZ + this.run_pot_TZ;
        this.run_actGW = this.run_actGW + this.run_pot_GW;

        /** testing if inflows can be stored in groundwater storages */
        double delta_GW = this.run_actGW - this.run_maxGW;
        if (delta_GW > 0) {
            /*this.run_actTZ = this.run_actTZ + delta_GW;
            this.run_actGW = this.run_maxGW;
            this.run_pot_TZ = run_pot_TZ + delta_GW;
            this.run_pot_GW = run_pot_GW - delta_GW;
            }
            double delta_TZ = this.run_actTZ - this.run_maxTZ;
            
            
            if (delta_TZ > 0) {*/
            this.run_gwExcess = this.run_gwExcess + delta_GW;  //delta_TZ;
            this.run_actGW = this.run_maxGW;
            //this.run_actTZ = this.run_maxTZ;
        }
        this.run_pre_outGW = this.run_outGW;
        this.run_pre_actGW = this.run_actGW;

        return true;
    }

    private boolean calcDarcyGWout() throws JAMSEntity.NoSuchAttributeException {

        // Ausfluss auf TZ noch alte Variante
        /*double k_TZ = 1 / this.run_TZ_rec;
        if (k_TZ > 1) {
            k_TZ = 1;
        }
        double TZ_out = k_TZ * this.run_actTZ;
        this.run_actTZ = this.run_actTZ - TZ_out;
        this.run_outTZ = this.run_outTZ + TZ_out;*/

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
                        run_cf = gwFlowArea * this.run_Kf_geo / this.run_gwFlowLength;                              //[m²/d]

                        GW_out_m3 = run_cf * (gwDifference);  //[m³/d]

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

        this.run_Flurabstand = this.run_elevation - this.run_gwTableUpper;
        //this.pot_gwTable.setValue(this.run_gwTableUpper);

        return true;
    }
}

/*
 			<component class="org.jams.j2k.s_n.J2KProcessGroundwater" name="J2KProcessGroundwater">
				<jamsvar name="area" provider="HRUContext" providervar="currentEntity.area"/>
				<jamsvar name="slope" provider="HRUContext" providervar="currentEntity.slope"/>
				<jamsvar name="maxTZ" provider="HRUContext" providervar="currentEntity.maxTZ"/>
				<jamsvar name="maxGW" provider="HRUContext" providervar="currentEntity.maxGW"/>
				<jamsvar name="kTZ" provider="HRUContext" providervar="currentEntity.TZ_k"/>
				<jamsvar name="kGW" provider="HRUContext" providervar="currentEntity.GW_k"/>
				<jamsvar name="actTZ" provider="HRUContext" providervar="currentEntity.actTZ"/>
				<jamsvar name="actGW" provider="HRUContext" providervar="currentEntity.actGW"/>
				<jamsvar name="inTZ" provider="HRUContext" providervar="currentEntity.inTZ"/>
				<jamsvar name="inGW" provider="HRUContext" providervar="currentEntity.inGW"/>
				<jamsvar name="outTZ" provider="HRUContext" providervar="currentEntity.outTZ"/>
				<jamsvar name="outGW" provider="HRUContext" providervar="currentEntity.outGW"/>
				<jamsvar name="genTZ" provider="HRUContext" providervar="currentEntity.genTZ"/>
				<jamsvar name="genGW" provider="HRUContext" providervar="currentEntity.genGW"/>
				<jamsvar name="percolation" provider="HRUContext" providervar="currentEntity.percolation"/>
				<jamsvar name="interflow" provider="HRUContext" providervar="currentEntity.outRD2"/>
				<jamsvar name="maxSoilStorage" provider="HRUContext" providervar="currentEntity.maxMPS"/>
				<jamsvar name="actSoilStorage" provider="HRUContext" providervar="currentEntity.actMPS"/>
				<jamsvar name="pot_TZ" provider="HRUContext" providervar="currentEntity.pot_TZ"/>
				<jamsvar name="pot_GW" provider="HRUContext" providervar="currentEntity.pot_GW"/>
				<jamsvar name="partint" provider="HRUContext" providervar="currentEntity.partint"/>
				<jamsvar name="gwTZGWdist" value="0.8"/>
				<jamsvar name="gwTZFact" value="1.0"/>
				<jamsvar name="gwGWFact" value="1.0"/>
				<jamsvar name="gwCapRise" value="0.0"/>
			</component>
 */