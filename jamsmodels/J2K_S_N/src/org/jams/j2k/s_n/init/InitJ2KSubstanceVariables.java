/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.s_n.init;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Bjoern hats geschrieben, aber der Manni hat gesagt wie!
 */

@JAMSComponentDescription(
        title = "InitJ2KSubstanceVariables",
        author = "Manfred Fink",
        description = "Calculates Nitrogen transformation Processes in Soil. Method after SWAT2000 with adaptions Including Nitrogen transported due to Erosion",
        version = "1.2",
        date = "2010-06-02"
)

public class InitJ2KSubstanceVariables extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "",
            unit = "-",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.Double NH4_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "",
            unit = "-",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.Double SurfaceSolubleP_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "",
            unit = "-",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.Double activN_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "",
            unit = "-",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.Double activP_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "",
            unit = "-",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.Double org_in_P;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "",
            unit = "-",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.Double insed;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "",
            unit = "-",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.Double residueN_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "",
            unit = "-",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.Double residue_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "",
            unit = "-",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.Double residue_in_P;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "",
            unit = "-",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.Double stableN_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "",
            unit = "-",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.Double stableP_in;

    public void initAll() {

        NH4_in.setValue(0);
        SurfaceSolubleP_in.setValue(0);
        activN_in.setValue(0);
        activP_in.setValue(0);
        org_in_P.setValue(0);
        insed.setValue(0);
        residueN_in.setValue(0);
        residue_in.setValue(0);
        residue_in_P.setValue(0);
        stableN_in.setValue(0);
        stableP_in.setValue(0);
    }
}
