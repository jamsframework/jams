

package org.jams.j2k.s_n.wq;

import java.io.*;
import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
        title = "Init_heatflux",
        author = "Marcel Wetzel",
        description = "Init heat flux variables for Reaches at timestep 1",
        version="1.0_0",
        date="2010-12-12"
        )

public class init_heatflux extends JAMSComponent {

    /*
     *  Component variables
     */


    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "init potET energy amount of specific reach",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble potETenergy;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "energy amount of atmospheric longwave radiation",
            unit = "Kcal/(m^2*d)",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble atloRadEnergy;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "energy amount of net solar radiation",
            unit = "Kcal/(m^2*d)",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Rns;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "energy amount of back radiation from the water surface",
            unit = "Kcal/(m^2*d)",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble waterbr;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "heat transfer due to conduction and convection",
            unit = "Kcal/(m^2*d)",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble coco;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the sediment water heat flux",
            unit = "Kcal/(m^2*d)",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble sedheat2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "init bottom sediment layer temperature",
            unit = "°C",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble bottomsedtemp;

    
    /*
     *  Component run stages
     */


    public void init() throws JAMSEntity.NoSuchAttributeException {

    }
    public void run() throws Attribute.Entity.NoSuchAttributeException {
            
         double initvalue = 1;
         potETenergy.setValue(initvalue);
         atloRadEnergy.setValue(initvalue);
         Rns.setValue(initvalue);
         waterbr.setValue(initvalue);
         coco.setValue(initvalue);
         sedheat2.setValue(initvalue);
         bottomsedtemp.setValue(initvalue);
         
         
    }

     public void cleanup() {

    }

}


    
