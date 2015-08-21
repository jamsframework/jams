/*
 * InitJ2KProcessGroundwater.java
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
import java.lang.Math.*;

@JAMSComponentDescription(
        title = "J2KGroundwaterRoutingPreparator",
        author = "Daniel Varga",
        description = "Initialisation for j2k.gw.J2KProcessGroundwater_D",
        version="1.0_0",
        date="2011-01-10"
        )
public class InitJ2KProcessGroundwater_D extends JAMSComponent {

    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "The elevation of the current hru entity",
        unit = "m", //[m ü NN] / [a.s.l.]
        lowerBound= -422,
        upperBound = 8848
        )
        public JAMSDouble elevation;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "attribute area",
        unit = "m^2",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble area;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Distance between adjacent entities",
        unit = "m",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble gwFlowLength;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "relative initial GW storage",
        unit = "-",
        lowerBound= 0.0,
        upperBound = 1.0
        )
        public JAMSDouble initGW;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "maximum GW storage",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble maxGW;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "actual TZ storage",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble actTZ;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "actual GW storage",
        unit = "l",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble actGW;

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

    /*@JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "entity x-coordinate"
        )
        public JAMSDouble entityX;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "entity y-coordinate"
        )
        public JAMSDouble entityY;
    */

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "HRU Groundwater Level",
        unit = "m",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble gwTable;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "estimated hydraulic conductivity in cm/d",
        unit = "cm/d",
        lowerBound= 0.0,
        upperBound = 86400
        )
        public JAMSDouble Kf_geo;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Adaptation Factor for hydraulic conductivity",
        defaultValue = "1",
        unit = "-",
        lowerBound= 0.0,
        upperBound = 100.0
        )
        public JAMSDouble KfAdaptation;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Adaptation factor for porosity",
        defaultValue = "1",
        unit = "-",
        lowerBound= 0.0,
        upperBound = 10.0
        )
        public JAMSDouble PeffAdaptation;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "adapted Kf_geo",
        unit = "m/d",
        lowerBound= 0.0,
        upperBound = 8640
        )
        public JAMSDouble Kf_geo_adapt;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "estimated porosity",
        unit = "-",
        lowerBound= 0.0,
        upperBound = 1.0
        )
        public JAMSDouble Peff;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "Heigth of the aquifer base in m a.s.l.",
        unit = "m", //[m ü NN] / [a.s.l.]
        lowerBound= -422,
        upperBound = 8848
        )
        public JAMSDouble baseHeigth;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Adaptation of Heigth of the aquifer base in m",
        unit = "m",
        defaultValue = "0",
        lowerBound= -100,
        upperBound = 100
        )
        public JAMSDouble baseHeigth_v;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
        update = JAMSVarDescription.UpdateType.RUN,
        description = "depth of soil",
        unit = "m",
        lowerBound= 0,
        upperBound = 10
        )
        public JAMSDouble depthSoil;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Minimum Depth of the aquifer in m",
        unit = "m",
        lowerBound= 0,
        upperBound = 10
        )
        public JAMSDouble minimumAQ_h;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Thickness of the Aquifer",
        unit = "m",
        lowerBound= 0,
        upperBound = 100
        )
        public JAMSDouble aqThickness;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Flowlength",
        unit = "m",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble fll;

    /*
     *  Component run stages
     */

    double run_area, run_Peff, run_Kf_geo, run_KfAdaptation, run_aqThickness, run_baseHeigth,  upFL, downFL, run_Kf_geo_adapted, run_gwFlowLength,
           run_gwDepth, run_gwTable, run_actGW, run_maxGW, run_satGW, run_soilDepth;
       

    public void init() throws JAMSEntity.NoSuchAttributeException {

    }

    public void run() throws JAMSEntity.NoSuchAttributeException {

        run_area = area.getValue();                                 //[m]
        run_Kf_geo = Kf_geo.getValue();                             //[cm/d]
        run_KfAdaptation = KfAdaptation.getValue();                 //[-]
        run_baseHeigth = baseHeigth.getValue();                     //[m]
        
        double run_baseHeigth_v = baseHeigth_v.getValue();          //[m ü NN]

        double min_AQ_h = minimumAQ_h.getValue();
        
        double HRU_elev = elevation.getValue();
        run_soilDepth = depthSoil.getValue();
        
        //Berechnung der Fließlänge, bzw. der Fließdistanz von HRU zu HRU
        upFL = fll.getValue();

        if (toPoly.getValue() != null) {
            downFL = toPoly.getDouble("fll");
        }
        else if (toReach.getValue() != null) {
            downFL = 0;

        }
	else {
	    getModel().getRuntime().println("Current entity has no receiver.");
	}
        
        /*Berechnung mit Koordinaten (alt):
        upX = entityX.getValue();
        upY = entityY.getValue();

        if (toPoly.getValue() != null) {
            downX = toPoly.getDouble("x");
            downY = toPoly.getDouble("y");
        }
        else if (toReach.getValue() != null) {
            downX = toReach.getDouble("x");   
            downY = toReach.getDouble("y");
        }
        
        run_gwFlowLength = Math.pow((Math.pow((upY - downY), 2) + (Math.pow((upX - downX), 2))), 0.5);
        */

        //Berechnung mit abgeleiteter Fließlänge... die Fließdistanz zwischen zwei HRUs entspricht 
        //der Summe der halbierten Fließlängen der HRUs
        
        run_gwFlowLength = (upFL / 2) + (downFL / 2);
        //run_gwFlowLength = upFL; //Erstmal ein test...

        //Anpassung des Durchlässigkeitsbeiwerts mit Kalibrierungsfaktor und Umrechnung von [cm/d] auf [m/d]
        run_Kf_geo_adapted = run_Kf_geo * run_KfAdaptation / 100;
        
        //Bestimmung der Porosität nach Hölting s. 86
        run_Peff = 0.462 + 0.045 * Math.log(run_Kf_geo_adapted / 86400);
        run_Peff = run_Peff * PeffAdaptation.getValue();
        if (run_Peff < 0.01){
            run_Peff = 0.01;
        }

        // und wieder zurück!
        //run_Kf_geo_adapted = run_Kf_geo_adapted * 86400; // Entfällt wegen Modifikation von Zeile 265 und 268

        //Anpassung der Aquifersohle durch Kalibrierfaktor

        run_baseHeigth = run_baseHeigth + run_baseHeigth_v;
        
        run_aqThickness = HRU_elev - run_soilDepth - run_baseHeigth; // Geländehöhe - Bodentiefe - Aquiferbasis
        
        if (run_aqThickness < min_AQ_h){
            run_baseHeigth = HRU_elev - run_soilDepth - min_AQ_h;
            run_aqThickness = min_AQ_h;
        }
        
        //Berechnung des Initialzustandes
        run_maxGW = run_aqThickness * run_area * run_Peff * 1000;  //[l]
        run_actGW = run_maxGW * initGW.getValue();

        if (run_actGW <= 0) {
	    //getModel().getRuntime().println("Act GW <= 0.");
	}

        run_satGW = run_actGW / run_maxGW;

        run_gwDepth = run_actGW / 1000 / run_area / run_Peff;      //Mächtigkeit in [m]
        
        run_gwTable = run_gwDepth + run_baseHeigth;                 //Grundwasserspiegellage in [m ü NN]


        //Setzen der Parameter
        baseHeigth.setValue(run_baseHeigth);                        //[m ü NN]
        aqThickness.setValue(run_aqThickness);                        //[m ü NN]
        gwFlowLength.setValue(run_gwFlowLength);                    //[m]
        gwTable.setValue(run_gwTable);                              //[m]
        Kf_geo_adapt.setValue(run_Kf_geo_adapted);                  //[m/d]
        Peff.setValue(run_Peff);                                    //[-]
        actTZ.setValue(0);                                         //[l]
        actGW.setValue(run_actGW);                                //[l]
        maxGW.setValue(run_maxGW);                                //[l]
    }

    public void cleanup() {
    }
}
