

package org.jams.j2k.s_n.wq.ProcessRates;

import org.jams.j2k.s_n.wq.*;
import java.io.*;
import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
        title = "init_ProcessRates_Variables",
        author = "Marcel Wetzel",
        description = "Init process rates variables at timestep 1",
        version="1.0_0",
        date="2012-04-23"
        )

public class init_ProcessRates_Variables extends JAMSComponent {

    /*
     *  Component variables
     */


    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "biomass of heterotrophic organisms",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_H;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "biomass of 1st stage nitrifiers",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_N1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "biomass of 2nd stage nitrifiers",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_N2;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "algae and macrophytes",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_ALG;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "particulate organic material",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_S;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "inert particulate organic material",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_I;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "dissolved organic substances for rapid biodegradation",
            unit = "mgSS/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_S;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "NH4-N",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_NH4;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "NH3-N",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY,
            defaultValue = "0"
            )
            public JAMSDouble S_NH3;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "NO2-N",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_NO2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "NO3-N",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_NO3;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "HPO4-P part of inorganic dissolved phosphorus",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_HPO4;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "H2PO4-P part of inorganic dissolved phosphorus",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY,
            defaultValue = "0"
            )
            public JAMSDouble S_H2PO4;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Hydrogen ions H^+",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY,
            defaultValue = "0"
            )
            public JAMSDouble S_H;

    
    /*
     *  Component run stages
     */


    public void init() throws JAMSEntity.NoSuchAttributeException {

    }
    public void run() throws Attribute.Entity.NoSuchAttributeException {
            
         double initvalue = 0.1;
         X_H.setValue(initvalue);
         X_N1.setValue(initvalue);
         X_N2.setValue(initvalue);
         X_ALG.setValue(initvalue);
         X_S.setValue(initvalue);
         X_I.setValue(initvalue);
         
         S_S.setValue(initvalue);
         S_NH4.setValue(initvalue);
         S_NH3.setValue(initvalue);
         S_NO2.setValue(initvalue);
         S_NO3.setValue(initvalue);
         S_HPO4.setValue(initvalue);
         S_H2PO4.setValue(initvalue);
         S_H.setValue(initvalue);
    }

     public void cleanup() {

    }

}


    
