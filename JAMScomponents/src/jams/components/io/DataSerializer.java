/*
 * DataSerializer.java
 * Created on 24.07.2023, 22:40:45
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.io;

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
        title = "Title",
        author = "Author",
        description = "Description",
        date = "YYYY-MM-DD",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class DataSerializer extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description"
    )
    public Attribute.DoubleArray obsInArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description"
    )
    public Attribute.StringArray obsIDs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description"
    )
    public Attribute.Double simValue;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description"
    )
    public Attribute.Double simID;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Description",
            defaultValue = "True"
    )
    public Attribute.Boolean includeMissing;            
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Description"
    )
    public Attribute.Object simOutArray;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Description"
    )
    public Attribute.Object obsOutArray;
        
    private Map<Double, Integer> order = null;
    
    /*
     *  Component run stages
     */
    @Override
    public void initAll() {
        simOutArray.setValue(new ArrayList<Double>());
        obsOutArray.setValue(new ArrayList<Double>());
    }

    @Override
    public void run() {
        if (order == null) {
            order = new HashMap<>();
            int pos = 0;
            for (String s : obsIDs.getValue()) {
                order.put(Double.valueOf(s), pos++);
            }
        }
        
        if (!order.containsKey(simID.getValue())) {
            return;
        }
        
        int pos = order.get(simID.getValue());
        double obsValue = obsInArray.getValue()[pos];
//        Attribute.Double _obsValue = getModel().getRuntime().getDataFactory().createDouble();
//        _obsValue.setValue(obsValue);
        
        if (includeMissing.getValue()) {
            ((ArrayList) simOutArray.getValue()).add(simValue.getValue());
            ((ArrayList) obsOutArray.getValue()).add(obsValue);            
        } else {
            if (obsValue != Double.POSITIVE_INFINITY) {
                ((ArrayList) simOutArray.getValue()).add(simValue.getValue());
                ((ArrayList) obsOutArray.getValue()).add(obsValue);            
            }            
        }
    }

    @Override
    public void cleanup() {
    }
}
