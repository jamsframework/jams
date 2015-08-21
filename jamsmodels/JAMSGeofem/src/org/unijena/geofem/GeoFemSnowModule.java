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
title="GeoFemSnowModule",
        author="Peter Krause",
        description="Simple day-degree-approach to account for snow storage and snowmelt."+
        "Depends on a temperature threshold and a day-degree-factor"
        )
        public class GeoFemSnowModule extends JAMSComponent {
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Entity area",
            unit = "m²"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Parameter ddf"
            )
            public Attribute.Double ddf;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Parameter threshold"
            )
            public Attribute.Double t_thres;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "the snow storage "
            )
            public Attribute.Double snowStorage;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "precip"
            )
            public Attribute.Double precip;
    
    
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the tmin input"
            )
            public Attribute.Double tmin;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the tmean input"
            )
            public Attribute.Double tmean;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the tmax input"
            )
            public Attribute.Double tmax;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "the snowmelt output"
            )
            public Attribute.Double snowMelt;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "remaining precip"
            )
            public Attribute.Double restPrecip;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "module active"
            )
            public Attribute.Boolean active;
        /*
         *  Component run stages
         */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        if(this.active == null || this.active.getValue()){
            //System.out.println("RUN ABCModel");
            double snowStorage = this.snowStorage.getValue();
            double precip = this.precip.getValue();
            this.precip.setValue(precip);
            double tmin = this.tmin.getValue();
            double tmean = this.tmean.getValue();
            double tmax = this.tmax.getValue();
            double snowMelt = 0;
            
            double accuTemp = (tmin + tmean) / 2;
            double meltTemp = (tmax + tmean) / 2;
            
            //accumulation
            if(accuTemp <= this.t_thres.getValue()){
                snowStorage = snowStorage + precip;
                precip = 0;
            }
            //snow melt
            if(meltTemp > this.t_thres.getValue() && snowStorage > 0){
                double mt = meltTemp - this.t_thres.getValue();
                double potMelt = mt * this.ddf.getValue() * this.area.getValue();
                if(snowStorage < potMelt){
                    snowMelt = snowStorage;
                    snowStorage = 0;
                } else{
                    snowMelt = potMelt;
                    snowStorage = snowStorage - snowMelt;
                }
            }
            
            //this.precip.setValue(precip);
            this.snowStorage.setValue(snowStorage);
            this.snowMelt.setValue(snowMelt);
            
            this.restPrecip.setValue(precip);
            
        }
    }
    
    public void cleanup() {
        if(this.active == null || this.active.getValue()){
            this.snowStorage.setValue(0.0);
        }
        
    }
    
}
