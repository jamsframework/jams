

package org.jams.j2k.s_n.wq.DissolvedOxygen;

import org.jams.j2k.s_n.wq.*;
import java.io.*;
import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
        title = "Dissolved Oxygen",
        author = "Marcel Wetzel",
        description = "calculates amount of dissolved oxygen for reaches",
        version="1.0_0",
        date="2010-09-07"
        )

public class dissolvedOxygen extends JAMSComponent {

    /*
     *  Component variables
     */

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "dissolved oxygen",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_O2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the oxygen saturation concentration",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = 30
            )
            public JAMSDouble DOsat;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "dissolved oxygen concentration in water body",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = 30
            )
            public JAMSDouble disOxy;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "dissolved oxygen concentration in water body",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = 30
            )
            public JAMSDouble Oxydis;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the reaeration rate for computation of oxygen concentration",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble RateReaer;
  
    /*
     *  Component run stages
     */


    public void init() throws JAMSEntity.NoSuchAttributeException {

    }
    public void run() throws JAMSEntity.NoSuchAttributeException, IOException {

        // calculation of dissolved oxygen amount in water body
            // DO the mean dissolved oxygen amount in water body (mg/l)
            // Reaer the daily reaeration rate (mg/l)

        double DO = 0;
        double Reaer = RateReaer.getValue();
        disOxy.setValue(S_O2.getValue());
        DO = disOxy.getValue() + Reaer;
        disOxy.setValue(DO);
        Oxydis.setValue(DO);

        //System.out.println("dissolved Oxygen: " + DO);

    }

     public void cleanup() {

    }

}


    
