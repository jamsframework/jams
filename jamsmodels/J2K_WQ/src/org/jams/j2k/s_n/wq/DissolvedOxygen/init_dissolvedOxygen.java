

package org.jams.j2k.s_n.wq.DissolvedOxygen;

import org.jams.j2k.s_n.wq.*;
import java.io.*;
import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
        title = "Init dissolved Oxygen",
        author = "Marcel Wetzel",
        description = "init value for dissolved oxygen in water body at timestep 1",
        version="1.0_0",
        date="2010-12-12"
        )

public class init_dissolvedOxygen extends JAMSComponent {

    /*
     *  Component variables
     */

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "dissolved oxygen",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_O2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "init dissolved oxygen in water body",
            unit = "mg/l",
            lowerBound= 0,
            upperBound =20
            )
            public JAMSDouble disOxy;

    
    /*
     *  Component run stages
     */


    public void init() throws JAMSEntity.NoSuchAttributeException {

    }
    public void run() throws Attribute.Entity.NoSuchAttributeException {
            
         double initvalue = 8;
         S_O2.setValue(initvalue);
         
    }

     public void cleanup() {

    }

}


    
