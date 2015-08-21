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
import java.util.*;
import java.lang.Math.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(title = "J2KGroundwater",
author = "Peter Krause modifications Daniel Varga",
description = "Description")
public class InitJ2KProcessGroundwater_D_N_1_newest extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The current hru entity")
    public Attribute.EntityCollection hrus;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The elevation of the current hru entity")
    public Attribute.Double elevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The current reach entity")
    public Attribute.EntityCollection reaches;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "attribute area")
    public Attribute.Double area;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Distance between adjacent entities")
    public Attribute.Double gwFlowLength;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Length of the border arc between adjacent entities")
    public Attribute.Double gwArcLength;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "maximum RG1 storage")
    public Attribute.Double maxRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "maximum GW storage")
    public Attribute.Double maxGW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "maximum GW storage")
    public Attribute.Double satGW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "actual RG1 storage")
    public Attribute.Double actRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "actual GW storage")
    public Attribute.Double actGW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "relative initial RG1 storage")
    public Attribute.Double initRG1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "relative initial GW storage")
    public Attribute.Double initGW;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Downstream hru entity")
    public Attribute.Entity toPoly;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Downstream reach entity")
    public Attribute.Entity toReach;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "entity x-coordinate")
    public Attribute.Double entityX;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "entity y-coordinate")
    public Attribute.Double entityY;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
        description = "HRU Groundwater Level"
        )
        public Attribute.Double gwTable;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
    description = "Reach Water Level")
    public Attribute.Double waterTable_NN;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "estimated hydraulic conductivity in cm/d")
    public Attribute.Double Kf_geo;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "estimated hydraulic conductivity in cm/d")
    public Attribute.Double KfAdaptation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "estimated porosity")
    public Attribute.Double Peff;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "adapted Kf_geo")
    public Attribute.Double Kf_geo_adapt;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Height of the aquifer base in m + NN")
    public Attribute.Double baseHeigth;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Adaptation of Height of the aquifer base in m")
    public Attribute.Double baseHeigth_v;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Minimum Depth of the aquifer in m")
    public Attribute.Double minimumAQ_h;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Groundwater outflow")
    public Attribute.Double pot_outGW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Height of potential groundwater Table (in Init the same as gwTable")
    public Attribute.Double pot_gwTable;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Height of potential groundwater Table (in Init the same as gwTable")
    public Attribute.Double preOutGW;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Height of potential groundwater Table (in Init the same as gwTable")
    public Attribute.Double preActGW;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Flowlength")
    public Attribute.Double fll;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Thickness of the Aquifer")
    public Attribute.Double aqThickness;



    /*
     *  Component run stages
     */

    double run_area, run_Peff, run_Kf_geo, run_KfAdaptation, run_aqThickness, run_baseHeigth,  upFL, downFL, run_Kf_geo_adapted, run_gwFlowLength,
           run_gwDepth, run_gwTable, run_actGW, run_maxGW, run_satGW;
       

    public void init() throws Attribute.Entity.NoSuchAttributeException {

    }

    public void run() throws Attribute.Entity.NoSuchAttributeException {

        run_area = area.getValue();                                 //[m]
        run_Kf_geo = Kf_geo.getValue();                             //[cm/d]
        run_KfAdaptation = KfAdaptation.getValue();                 //[-]
        run_baseHeigth = baseHeigth.getValue();                     //[m]
        
        double run_baseHeigth_v = baseHeigth_v.getValue();          //[m ü NN]

        double min_AQ_h = minimumAQ_h.getValue();
        
        double HRU_elev = elevation.getValue();
        
        //Berechnung der Fließlänge, bzw. der Fließdistanz von HRU zu HRU
        upFL = fll.getValue();

        if (toPoly.getValue() != null) {
            downFL = toPoly.getDouble("fll");
        }
        else if (toReach.getValue() != null) {
            downFL = 0;

        }
	else {
	    // getModel().getRuntime().println("Current entity has no receiver.");
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
        
        //run_gwFlowLength = (upFL / 2) + (downFL / 2);
        run_gwFlowLength = upFL; //Erstmal ein test...

        //Anpassung des Durchlässigkeitsbeiwerts mit Kalibrierungsfaktor und Umrechnung von [cm/d] auf [m/s]
        run_Kf_geo_adapted = run_Kf_geo * run_KfAdaptation / 100;
        
        //Bestimmung der Porosität nach Hölting s. 86
        run_Peff = 0.462 + 0.045 * Math.log(run_Kf_geo_adapted / 86400);
        if (run_Peff < 0.01){
            run_Peff = 0.01;
        }

        // und wieder zurück!
        //run_Kf_geo_adapted = run_Kf_geo_adapted * 86400; // Entfällt wegen Modifikation von Zeile 265 und 268

        //Anpassung der Aquifersohle durch Kalibrierfaktor

        run_baseHeigth = run_baseHeigth + run_baseHeigth_v;
        
        run_aqThickness = HRU_elev - run_baseHeigth;
        
        if (run_aqThickness < min_AQ_h){
            run_baseHeigth = HRU_elev - min_AQ_h;
            run_aqThickness = min_AQ_h;
        }
        
        //Berechnung des Initialzustandes
        run_maxGW = run_aqThickness * run_area * run_Peff * 1000;  //[l]
        run_actGW = run_maxGW * initGW.getValue();

        if (run_actGW <= 0) {
	    // getModel().getRuntime().println("Act GW <= 0.");
	}

        run_satGW = run_actGW / run_maxGW;

        run_gwDepth = run_actGW / 1000 / run_area / run_Peff;      //Mächtigkeit in [m]
        
        run_gwTable = run_gwDepth + run_baseHeigth;                 //Grundwasserspiegellage in [m ü NN]


        //Setzen der Parameter
        baseHeigth.setValue(run_baseHeigth);                        //[m ü NN]
        aqThickness.setValue(run_aqThickness);                        //[m ü NN]
        gwFlowLength.setValue(run_gwFlowLength);                    //[m]
        gwTable.setValue(run_gwTable);                              //[m]
        pot_gwTable.setValue(run_gwTable);                          //[m ü NN]
        Kf_geo_adapt.setValue(run_Kf_geo_adapted);                  //[m/s]
        Peff.setValue(run_Peff);                                    //[-]
        actRG1.setValue(0);                                         //[l]
        actGW.setValue(run_actGW);                                //[l]
        maxGW.setValue(run_maxGW);                                //[l]
        satGW.setValue(run_satGW);
        pot_outGW.setValue(0);                                     //[l]
        preOutGW.setValue(0);                                      //[l]
        preActGW.setValue(run_actGW);                             //[l]
    }

    public void cleanup() {
    }
}
