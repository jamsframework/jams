/*
 * J2KProcessReachRouting.java
 * Created on 28. November 2005, 10:01
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
package org.unijena.j2k.tools;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
        title = "AreaFraction_evaluator",
        author = "Manfred Fink",
        description = "Sum up the area that meet a defined condition ",
        version = "1.0",
        date = "2015-05-20"
)

public class AreaFraction_evaluator extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Entity parameter area",
            unit = "m^2"
    )
    public Attribute.Double area;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "evaluated value",
            unit = "-"
    )
    public Attribute.Double Value;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Theshhold",
            unit = "-"
    )
    public Attribute.Double Theshhold;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "1, greater than; 2, greater equval; 3, smaller than; 4 smaller equval; 5, equal",
            unit = "-"
    )
    public Attribute.Integer CType;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Sumarized area which meet the condidion",
            unit = "m^2"
    )
    public Attribute.Double AreaSum;

    /*
     *  Component run stages
     */
    public void init() {

    }

    public void run() {

        Double run_areasum = 0.0;

        switch (CType.getValue()) {

            case 1: {

                if (Value.getValue() > Theshhold.getValue()) {
                    run_areasum = run_areasum + area.getValue();
                }
            }
            case 2:{

                if (Value.getValue() >= Theshhold.getValue()) {
                    run_areasum = run_areasum + area.getValue();
                }
            }

            case 3:{

                if (Value.getValue() < Theshhold.getValue()) {
                    run_areasum = run_areasum + area.getValue();
                }
            }

            case 4:{

                if (Value.getValue() <= Theshhold.getValue()) {
                    run_areasum = run_areasum + area.getValue();
                }
            }

            case 5:{

                if (Value.getValue() == Theshhold.getValue()) {
                    run_areasum = run_areasum + area.getValue();
                }
            }

        }
        
        AreaSum.setValue(run_areasum);

    }

    public void cleanup() {

    }
}
