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
        title = "Condition Evaluator",
        author = "Manfred Fink",
        description = "evaluates diffrent Boolean comparisions of two Double values",
        version = "1.0",
        date = "2014-07-04"
)

public class Condition_evaluator extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "First Value",
            unit = "-"
    )
    public Attribute.Double Fvalue;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Second Value",
            unit = "-"
    )
    public Attribute.Double Svalue;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "1, greater than; 2, greater equval; 3, smaller than; 4 smaller equval; 5, equal",
            unit = "-"
    )
    public Attribute.Integer CType;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "The Result value; 0, false; 1, true",
            unit = "-"
    )
    public Attribute.Double Result;

    /*
     *  Component run stages
     */
    public void init() {

    }

    public void run() {

        Double run_result = 0.0;

        switch (CType.getValue()) {

            case 1: {

                if (Fvalue.getValue() > Svalue.getValue()) {
                    run_result = 1.0;
                }
            }
            case 2:{

                if (Fvalue.getValue() >= Svalue.getValue()) {
                    run_result = 1.0;
                }
            }

            case 3:{

                if (Fvalue.getValue() < Svalue.getValue()) {
                    run_result = 1.0;
                }
            }

            case 4:{

                if (Fvalue.getValue() <= Svalue.getValue()) {
                    run_result = 1.0;
                }
            }

            case 5:{

                if (Fvalue.getValue() == Svalue.getValue()) {
                    run_result = 1.0;
                }
            }

        }
        
        Result.setValue(run_result);

    }

    public void cleanup() {

    }
}
