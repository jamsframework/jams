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
package org.jams.j2k.s_n.erosion;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "J2KProcessMultiRouting_h",
author = "Peter Krause, Holm Kipka, Bjoern Pfennig, Manfred Fink",
description = "Passes the output of the entities as input to the respective reach or unit")
public class MultiRoutingNT extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The current hru entity")
    public Attribute.EntityCollection entities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Collection of reach objects")
    public Attribute.EntityCollection reaches;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "NH4 in surface runoff added to HRU in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double NH4_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Residue-N in surface runoff added to HRU in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double residue_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Activ-N in surface runoff added to HRU in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double activ_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Stable-N in surface runoff added to HRU in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double stable_in;

        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "NH4 in surface runoff leaving the HRU in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double NH4_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Residue-N in surface runoff leaving the HRU in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double residue_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Activ-N in surface runoff leaving the HRU in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double activ_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Stable-N in surface runoff leaving the HRU in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double stable_out;
    
   
    /*
     *  Component run stages
     */
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

        
        double out_NH4 = NH4_out.getValue();
        double out_residue = residue_out.getValue();
        double out_activ = activ_out.getValue();
        double out_stable = stable_out.getValue();
        

        boolean keinziel = false;

       
        if (toPolyArray.length > 0) {

            for (int a = 0; a < toPolyArray.length; a++) {

                if (toPolyArray[a] != null) {

                    toPoly = toPolyArray[a];
                    polyWeight = polyWeightsArray[a];

                    double in_NH4 = toPoly.getDouble("NH4_in");
                    in_NH4 = (out_NH4 * polyWeight) + in_NH4;
                    double in_residue = toPoly.getDouble("residue_in");
                    in_residue = (out_residue * polyWeight) + in_residue;
                    double in_activ = toPoly.getDouble("activ_in");
                    in_activ = (out_activ * polyWeight) + in_activ;
                    double in_stable = toPoly.getDouble("stable_in");
                    in_stable = (out_stable * polyWeight) + in_stable;
                    //System.out.println(entity.getObject("ID")+" ~ "+a+" - to HRU - "+toPoly.getObject("ID")+ sedin);
                    //sedin = sedout * polyWeight;
                    //System.out.println(entity.getObject("ID")+" ~ "+a+" - in - "+ sedin);
                    toPoly.setDouble("NH4_in", in_NH4);
                    toPoly.setDouble("residue_in", in_residue);
                    toPoly.setDouble("activ_in", in_activ);
                    toPoly.setDouble("stable_in", in_stable);
                    
                    keinziel = true;
                }
            }
        }

        if (toReachArray.length > 0 ) {

            for (int a = 0; a < toReachArray.length; a++) {

                toReach = toReachArray[a];
                    
                reachWeight = reachWeightsArray[a];

                // nicht null!!!!
                if (toReachArray[a]!= null){
                
                double in_NH4 = toReach.getDouble("NH4_in");
                double in_residue = toReach.getDouble("residue_in");
                double in_activ = toReach.getDouble("activ_in");
                double in_stable = toReach.getDouble("stable_in");
                
                //System.out.println(toReach.getObject("ID")+" Reach0 ~ "+a+" - to - "+ sedin);
              
                in_NH4 = in_NH4 + out_NH4 * reachWeight;
                in_residue = in_residue + out_residue * reachWeight;
                in_activ = in_activ + out_activ * reachWeight;
                in_stable = in_stable + out_stable * reachWeight;
                

                //System.out.println(toReach.getObject("ID")+" Reach1 ~ "+a+" - to - "+ sedin);
                                
                
                toReach.setDouble("NH4_in",in_NH4);
                toReach.setDouble("residue_in",in_residue);
                toReach.setDouble("activ_in",in_activ);
                toReach.setDouble("stable_in",in_NH4);
                              
                keinziel = true;
                }
            }
        }

        //sedout = 0;
        //sedout = sedout * 0.05;
        //System.out.println(sedout);

        //outsed.setValue(sedout);
        if (keinziel == false) {
            getModel().getRuntime().println("Current entity ID: " + (int) entity.getDouble("ID") + " has no receiver.");
        }
    }

    public void cleanup() {
    }

   
}