/*
 * GlacierModule.java
 * Created on 22. Febuary 2008, 13:57
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package org.unijena.j2k.snow;
;
import java.io.*;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */

    
    
@JAMSComponentDescription(
        title="GlacierModule",
        author="Peter Krause",
        description="Simple process module for glacier simulation. The module " +
        "calculates snow accumulation by a temperature threshold approach and " +
        "snow melt from the glacier with a day-degree-approach. Melt from the " +
        "glacier is implementing by the melt formula according to " +
        "Hock (1998, 1999) in a simple and a more complex form. " +
        "The simple form needs temperature only whereas" +
        "the complex form needs also radiation." +
        "Glacier runoff is calculated by the outflow from two reservoirs. The first" +
        "represents snow falling on the glacier whereas the second represents the" +
        "ice of the glacier. The same idea was implemented in WasimETH first."
        )
    
    public class GlacierModule extends JAMSComponent {
    
        
    /*
     *  Component variables
     */
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the actual air temperature",
            unit="°C"
            )
            public Attribute.Double temperature;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the actual rainfall",
            unit="L/m^2"
            )
            public Attribute.Double rain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the actual snowfall",
            unit="L/m^2"
            )
            public Attribute.Double snow;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "the total precip",
            unit="L/m^2"
            )
            public Attribute.Double precip;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the actual global radiation",
            unit = "MJ/day"
            )
            public Attribute.Double radiation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual snow storage",
            unit = "L/m^2"
            )
            public Attribute.Double snowStorage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area",
            unit="m^2"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "snow runoff of time step before",
            unit = "L"
            )
            public Attribute.Double snowRunofftm1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "ice runoff of time step before",
            unit = "L"
            )
            public Attribute.Double iceRunofftm1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "total runoff of unit", 
            unit = "L"
            )
            public Attribute.Double glacierRunoff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "total runoff of unit",
            unit = "L"
            )
            public Attribute.Double dirQ;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "runoff from glacier melt",
            unit="L"
            )
            public Attribute.Double iceRunoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "runoff from snow melt and rain",
            unit = "L"
            )
            public Attribute.Double snowRunoff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "lateral inflow from glacier entities above",
            unit = "L"
            )
            public Attribute.Double inRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "remaining storage (only for balance calculation)",
            unit = "L"
            )
            public Attribute.Double glacStorage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "mass balance",
            unit = "L"
            )
            public Attribute.Double massBalance;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "generalised melt factor for ice and snow"
            )
            public Attribute.Double meltFactor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "day degree factor for snow"
            )
            public Attribute.Double ddfSnow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "day degree factor for ice"
            )
            public Attribute.Double ddfIce;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "melt coefficient for snow"
            )
            public Attribute.Double alphaSnow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "melt coefficient for ice"
            )
            public Attribute.Double alphaIce;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "routing coefficient for snow"
            )
            public Attribute.Double kSnow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "routing coefficient for ice"
            )
            public Attribute.Double kIce;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "threshold temperature"
            )
            public Attribute.Double tbase;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "melt formula [1 = simple, 2 = complex]"
            )
            public Attribute.Integer meltFormula;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temporal resolution [d | h]"
            )
            public Attribute.String tempRes;
    
    
    
    /*
     *  Component run stages
     */
    
    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException {
        //retreive the actual states and input
        double snowStor = this.snowStorage.getValue();
        double temp = this.temperature.getValue();
        double glacIn = this.rain.getValue() + this.snow.getValue();
        double glacOut = 0;
        
        double n = 0;
        if (this.tempRes.getValue().equals("d")) {
            n = 1;
        } else if (this.tempRes.getValue().equals("h")) {
            n = 24;
        } else if (this.tempRes.getValue().equals("m")) {
            n = 1 / 30;
        }
        //calc potential snow accumulation
        if (this.snow.getValue() > 0) {
            snowStor = snowStor + this.snow.getValue();
            this.snow.setValue(0);
        }
        //calc potential melt
        double snowMelt = 0;
        double iceMelt = 0;
        double totalMelt = 0;
        
        if (temp > this.tbase.getValue()) {
            if (this.meltFormula.getValue() == 1) {
                //simple formula
                //snow melt
                snowMelt = (1 / n) * this.ddfSnow.getValue() * (temp - this.tbase.getValue());
                snowMelt = snowMelt * this.area.getValue();
                if (snowMelt >= snowStor) {
                    snowMelt = snowStor;
                    snowStor = 0;
                } else {
                    snowStor = snowStor - snowMelt;
                }
                //ice melt only when no snow is available
                if(snowStor == 0){
                    iceMelt = (1 / n) * this.ddfIce.getValue() * (temp - this.tbase.getValue());
                    iceMelt = iceMelt * this.area.getValue();
                }
                else
                    iceMelt = 0;
            } else if (this.meltFormula.getValue() == 2) {
                //complex formula
                //snow melt
                snowMelt = (1 / n) * (this.meltFactor.getValue() + this.alphaSnow.getValue() * this.radiation.getValue()) * (temp - this.tbase.getValue());
                snowMelt = snowMelt * this.area.getValue();
                if (snowMelt >= snowStor) {
                    snowMelt = snowStor;
                    snowStor = 0;
                } else {
                    snowStor = snowStor - snowMelt;
                }
                //ice melt only when no snow is available
                if(snowStor == 0){
                    iceMelt = (1 / n) * this.meltFactor.getValue() + this.alphaIce.getValue() * this.radiation.getValue() * (temp - this.tbase.getValue());
                    iceMelt = iceMelt * this.area.getValue();
                }
                else{
                    iceMelt = 0;
                }
            }
            
            totalMelt = snowMelt + iceMelt;
            
        }
        double allIn = snowMelt + this.rain.getValue();
        //route runoff inside glacier
        //snow routing
        double q_snow = this.snowRunofftm1.getValue() * Math.exp(-1/this.kSnow.getValue()) + (snowMelt + this.rain.getValue()) * (1-Math.exp(-1/this.kSnow.getValue()));
        this.rain.setValue(0);
        //ice routing
        double q_ice = this.iceRunofftm1.getValue() * Math.exp(-1/this.kIce.getValue()) + iceMelt * (1-Math.exp(-1/this.kIce.getValue()));
        //calc total glacier runoff
        double tot_q = q_snow + q_ice;

        this.glacStorage.setValue(allIn - q_snow);
        glacOut = tot_q;

        //writing variables back
        this.snowRunofftm1.setValue(q_snow);
        this.iceRunofftm1.setValue(q_ice);
        this.glacierRunoff.setValue(tot_q);
        this.dirQ.setValue(tot_q);
        this.iceRunoff.setValue(q_ice);
        this.snowRunoff.setValue(q_snow);
        this.snowStorage.setValue(snowStor);
        this.precip.setValue(this.precip.getValue()*this.area.getValue());
        this.massBalance.setValue(glacIn - glacOut);
    }
    
    public void cleanup()  throws IOException {
        
    }
}
