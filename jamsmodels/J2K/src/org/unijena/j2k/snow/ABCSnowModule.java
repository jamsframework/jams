/*
 * ABCSnowModule.java
 *
 * Created on 18. Mai 2006, 15:20
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
title="ABCSnowModule",
        author="Peter Krause",
        description="Simple day-degree-approach to account for snow storage and snowmelt."+
        "Depends on a temperature threshold and a day-degree-factor"
        )
        public class ABCSnowModule extends JAMSComponent {
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Entity area"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "snow melt coefficient"
            )
            public Attribute.Double cm;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "base temperature"
            )
            public Attribute.Double t_base;
    
    
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "the snow storage "
            )
            public Attribute.Double snowStorage;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "rain amount"
            )
            public Attribute.Double rain;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "snow amount"
            )
            public Attribute.Double snow;
    
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
    
    public void init() {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        if(this.active == null || this.active.getValue()){
            //System.out.println("RUN ABCModel");
            double snowStorage = this.snowStorage.getValue();
            double precip = (this.rain.getValue() + this.snow.getValue());// * this.area.getValue();
            double tmin = this.tmin.getValue();
            double tmean = this.tmean.getValue();
            double tmax = this.tmax.getValue();
            double snowMelt = 0;
            
            double accuTemp = (tmin + tmean) / 2;
            double meltTemp = (tmax + tmean) / 2;
            
            //accumulation
            //snowStorage = snowStorage + this.snow.getValue();
            if(accuTemp <= this.t_base.getValue()){
                snowStorage = snowStorage + precip;
                precip = 0;
            }
            //snow melt
            if(meltTemp > this.t_base.getValue() && snowStorage > 0){
                double mt = meltTemp - this.t_base.getValue();
                double potMelt = mt * this.cm.getValue() * this.area.getValue();
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
            if(precip == 0){
                this.rain.setValue(0);
                this.snow.setValue(0);
            }
            
            this.restPrecip.setValue(precip);
            
        }
    }
    
    public void cleanup() {
        if(this.active == null || this.active.getValue()){
            this.snowStorage.setValue(0.0);
        }
        
    }
    
}
