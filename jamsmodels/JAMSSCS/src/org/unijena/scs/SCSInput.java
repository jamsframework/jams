/*
 * SCSInput.java
 * Created on 17. July 2006, 17:15
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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
package org.unijena.scs;
import java.util.Locale;
import jams.model.*;
import jams.data.*;

/**
 * Input class for JAMSSCS. Computes additional values for the catchment
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="SCS-Input",
        author="Peter Krause",
        description="Preliminary class for gathering all inputs needed" +
        " for the JAMSSCS Method"
        )
public class SCSInput extends JAMSComponent {
    
    /**
     * The precipitation as input, either from GUI or from Kostra-Files<br>
     * access: READ<br> update: INIT<br> unit: mm
     */
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            unit = "mm",
            description = "the input precip"
            )
            public Attribute.Double inputPrecip;
    
    /**
     * The effective precipitation computed from input precipitation and CN-Value<br>
     * access: WRITE<br> update: RUN<br> unit: mm
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            unit = "mm",
            description = "the effective precip, calculated from input" +
            "precipitation and CN-Value"
            )
            public Attribute.Double effectivePrecip; 
    
    /**
     * The length in kilometer of the main channel or stream<br>
     * access: READ<br> update: INIT<br> unit: km
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            unit = "km",
            description = "the length of the main channel or stream"
            )
            public Attribute.Double streamLength;
    
    /**
     * the maximum elevation in meter of the main channel or stream (source)<br>
     * access: READ<br> update: INIT<br> unit: m<br>
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            unit = "m",
            description = "the maximum elevation of the main stream (source)"
            )
            public Attribute.Double maxElevation;
    
    /**
     * the minimum elevation in meter of the main channel or stream (outlet)<br>
     * access: READ<br> update: INIT<br> unit: m<br>
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            unit = "m",
            description = "the minimum elevation of the main stream (outlet)"
            )
            public Attribute.Double minElevation;
    
    /**
     * the slope of the stream in percent calculated from min, max elevation and length<br>
     *  access: WRITE<br> update: RUN<br> unit: %
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            unit = "%",
            description = "slope of stream, calculated from elevation and length" +
            "of the main channel or stream"
            )
            public Attribute.Double streamSlope;
    
    /**
     * the catchment's mean CN value<br>
     * access: READ<br> update: INIT<br> unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the catchment's mean CN value"
            )
            public Attribute.Double cnValue;
    
    
    
    
    /**
     * run model
     * @throws org.unijena.jams.data.Attribute.Entity.NoSuchAttributeException thrown when a model entity tries to access a non existent attribute
     */
    public void run() throws Attribute.Entity.NoSuchAttributeException {
       /*
        *compute slope in percent
        */
        double slope =(this.maxElevation.getValue() - this.minElevation.getValue()) / (this.streamLength.getValue()*1000);
        this.streamSlope.setValue(slope);
        
        /*
         * calc effective precipitation from input precipitation and CN-Value
         */
        double precipBoundary = ((200 - 2 * this.cnValue.getValue())/ this.cnValue.getValue()) * 25.4;
        
        double termA = Math.pow((inputPrecip.getValue()/25.4) - (200.0/cnValue.getValue())+2.0, 2);
        double termB = (inputPrecip.getValue() / 25.4) + (800 / cnValue.getValue()) - 8.0;
        
        double effPrec = termA / termB * 25.4;
        
        if(this.inputPrecip.getValue() <= precipBoundary){
            effPrec = 0;
        }
        
        this.effectivePrecip.setValue(effPrec);
        getModel().getRuntime().println("Eingangsniederschlag: " + String.format(Locale.US,"%.1f",this.inputPrecip.getValue()));
        getModel().getRuntime().println("Effektivniederschlag: " + String.format(Locale.US,"%.1f",effPrec));
        getModel().getRuntime().println("CN-Wert: " + this.cnValue.getValue());
        
    }
    
}
