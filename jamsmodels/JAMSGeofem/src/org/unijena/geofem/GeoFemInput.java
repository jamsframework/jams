/*
 * GeoFemSnowModule.java
 *
 * Created on 18. Mai 2006, 15:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.geofem;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
title="GeoFemInputModule",
        author="Peter Krause",
        description="Computes the input data to provide the correct units etc."
        )
        public class GeoFemInput extends JAMSComponent {
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Entity area",
            unit = "m²"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "precip"
            )
            public Attribute.Double precip;
    
   
        /*
         *  Component run stages
         */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
            double precip = this.precip.getValue() * this.area.getValue();
            this.precip.setValue(precip);
    }
    
    public void cleanup() {
        
        
    }
    
}
