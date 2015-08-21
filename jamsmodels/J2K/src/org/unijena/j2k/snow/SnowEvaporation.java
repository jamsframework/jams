/*
 * SnowEvaporation.java
 *
 * Created on 22. Nov 2009, 15:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.j2k.snow;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
title="SnowEvaporation",
        author="Peter Krause",
        description="Very simple snow evaporation module which is estimates" +
        "snow ET as a constant fraction of potET"
        )
        public class SnowEvaporation extends JAMSComponent {
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the potential ET"
            )
            public Attribute.Double potET;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "the actual ET"
            )
            public Attribute.Double actET;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "snow ET coefficient"
            )
            public Attribute.Double set_factor;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "snow water equivalent"
            )
            public Attribute.Double swe;
    
    
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "the snow storage "
            )
            public Attribute.Double snowET;
    
    
        /*
         *  Component run stages
         */
    
    public void init() {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        double snow_et = 0;
        double run_swe = swe.getValue();
        double res_et = this.potET.getValue() * this.set_factor.getValue();
        double run_aET = this.actET.getValue();
        double deltaET = this.potET.getValue() - this.actET.getValue();

        if(res_et > deltaET)
            res_et = deltaET;
        
        if(run_swe >= res_et){
            snow_et = res_et;
            run_swe = run_swe - snow_et;
        }
        else{
            snow_et = run_swe;
            run_swe = 0;
        }
        run_aET = run_aET + snow_et;
        this.actET.setValue(run_aET);
        this.snowET.setValue(snow_et);
        this.swe.setValue(run_swe);

    }
    
    public void cleanup() {
        
        
    }
    
}
