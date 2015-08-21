/*
 * OutfileHeaderGenerator.java
 * Created on 01. October 2007, 17:15
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
 * Creates a model time interval for JAMSSCS. The user has
 * to specify a runtime length in hours and the duration of one time
 * step in seconds. From this information the model's time interval is
 * constructed and can now be used by other components.
 * @author P. Krause
 */
@JAMSComponentDescription(
        title="SCS-Input",
        author="Peter Krause",
        description="Creates a model time interval for JAMSSCS. The user has" +
        "to specify a runtime length in hours and the duration of one time " +
        "step in seconds. From this information the model's time interval is" +
        "constructed and can now be used by other components."
        )
public class OutfileHeaderGenerator extends JAMSComponent {
    
    /**
     * the outfileHeaderString, constructed in this component<br>
     * access: WRITE<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "The outfile header"
            )
            public Attribute.String outfileHeader;
    
    /**
     * catchment area<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: h
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "catchment area",
            unit="km^2"
            )
            public Attribute.Double catchmentArea;
    
    /**
     * the cnValue<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the cnValue",
            unit=""
            )
            public Attribute.Double cnValue;
    
    /**
     * the precip distribution<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the precip distribution",
            unit=""
            )
            public Attribute.String precipDistribution;
    
    /**
     * the streamLength<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the streamLength",
            unit=""
            )
            public Attribute.Double streamLength;
    
    /**
     * the cnValue<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the minimum stream elevation",
            unit=""
            )
            public Attribute.Double minElevation;
    
    /**
     * the maximum stream elevation<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the maximum stream elevation",
            unit=""
            )
            public Attribute.Double maxElevation;
    /**
     * the component's init() method
     * @throws org.unijena.jams.data.Attribute.Entity.NoSuchAttributeException thrown when a model entity tries to access a non existent attribute
     */
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        String header = "";
        header = header + "Einzugsgebietsgr��e: " + String.format(Locale.US,"%.2f",(this.catchmentArea.getValue()/1000000.)) + " km�\n";
        header = header + "Vorfluterl�nge: " + String.format(Locale.US,"%.2f",(this.streamLength.getValue())) + " km\n";
        header = header + "Maximale Vorfluterh�he (Quelle): " + String.format(Locale.US,"%.2f",(this.maxElevation.getValue())) + " m �. NN\n";
        header = header + "Minimale Vorfluterh�he (Auslass): " + String.format(Locale.US,"%.2f",(this.minElevation.getValue())) + " m �. NN\n";
        if(this.precipDistribution.getValue().equalsIgnoreCase("B"))
            header = header + "Niederschlagsverteilung: Blockregen\n";
        if(this.precipDistribution.getValue().equalsIgnoreCase("M"))
            header = header + "Niederschlagsverteilung: Mittenbetont\n";
        if(this.precipDistribution.getValue().equalsIgnoreCase("A"))
            header = header + "Niederschlagsverteilung: Anfangsbetont\n";
        if(this.precipDistribution.getValue().equalsIgnoreCase("E"))
            header = header + "Niederschlagsverteilung: Endbetont\n";
        header = header + "CN-Wert: " + String.format(Locale.US, "%.0f", this.cnValue.getValue());
        
        this.outfileHeader.setValue(header);
    } 
}
