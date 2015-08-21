/*
 * SimpleSOD.java
 * Created on 24.04.2013, 17:44:17
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package sewer;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "Title",
        author = "Author",
        description = "Description",
        date = "YYYY-MM-DD",
        version = "1.0_0")
public class SimpleSOD extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ, // type of access, i.e. READ, WRITE, READWRITE
            description = "height" // description of purpose
            )
    public Attribute.Double h;                // for a list of attribute types, see jams.data.Attribute  
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Coefficient discharge",
            unit = "-")
    public Attribute.Double dischCoeff;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "SOD threshold",
            unit = "m")
    public Attribute.Double threshold;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "pipe height",
            unit = "m")
    public Attribute.Double pipeHeight;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "pipe width",
            unit = "m")
    public Attribute.Double pipeWidth;
    /*
     *  Component run stages
     */
    @Override
    public void init() {
    }

    @Override
    public void run() {

        double g = 9.80665;
        double q;
        double c = threshold.getValue();
        double T = c + pipeHeight.getValue();
        double L = pipeWidth.getValue();

        if (h.getValue() <= T - c) {

            q = dischCoeff.getValue() * L * Math.sqrt(2 * g) * 1 * 1000 * 2.5 * (Math.pow(h.getValue(), 2.5));

        } else {

            q = dischCoeff.getValue() * L * Math.sqrt(2 * g) * 1 * 1000 * (2.5 * Math.pow(T - c, 2.5) + (T - c) * 1.5 * (Math.pow(h.getValue(), 1.5) - Math.pow(T - c, 1.5)));

        }
        
        System.out.println(h.getValue() + "\t" + q);

    }

    @Override
    public void cleanup() {
    }
}