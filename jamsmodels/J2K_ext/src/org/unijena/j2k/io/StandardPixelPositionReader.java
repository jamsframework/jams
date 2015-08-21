/*
 * StandardLUReader.java
 * Created on 10. November 2005, 10:53
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

package org.unijena.j2k.io;

import org.unijena.j2k.*;
import jams.data.*;
import jams.model.*;
import java.util.*;
import jams.JAMS;
import jams.tools.FileTools;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "StandardPixelPositionReader",
author = "Sven Kralisch",
description = "This component reads an ASCII file containing pixel  "
+ "information and adds them to model entities.",
date = "2005-11-10",
version = "1.1_0")
public class StandardPixelPositionReader extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Land use parameter file name"
            )
            public Attribute.String pixelPositionFile;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of hru objects"
            )
            public Attribute.EntityCollection hrus;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of hru objects"
            )
            public Attribute.String pixelIDAttribute;
    
    
    
    public void init() {
        //read lu parameter
        Attribute.EntityCollection pixels = getModel().getRuntime().getDataFactory().createEntityCollection();
        
        pixels.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(),pixelPositionFile.getValue()), getModel()));
        
        HashMap<Double, Attribute.Entity> pixelMap = new HashMap<Double, Attribute.Entity>();
        Attribute.Entity pixel, e;
        Object[] attrs;
        
        //put all entities into a HashMap with their ID as key
        Iterator<Attribute.Entity> pixelIterator = pixels.getEntities().iterator();
        while (pixelIterator.hasNext()) {
            pixel = pixelIterator.next();
            pixelMap.put(pixel.getDouble(pixelIDAttribute.getValue()),  pixel);
        }
        
        Iterator<Attribute.Entity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            Object cell = e.getObject("Cell_ID");
            if (cell instanceof Attribute.Double) {
                pixel = pixelMap.get(e.getDouble("Cell_ID"));
                e.setObject("Cell_ID", pixel);

                attrs = pixel.getKeys();

                for (int i = 0; i < attrs.length; i++) {
                    //e.setDouble((String) attrs[i], lu.getDouble((String) attrs[i]));
                    Object o = pixel.getObject((String) attrs[i]);
                    if (!(o instanceof Attribute.String)) {
                        e.setObject((String) attrs[i], o);
                    }
                }
            } else if (cell instanceof Attribute.DoubleArray) {
                Attribute.DoubleArray pixelIDs = (Attribute.DoubleArray) cell;

                HashMap<String, Attribute.DoubleArray> data = new HashMap<String, Attribute.DoubleArray>();
                int j=0;
                for (Double pixelID : pixelIDs.getValue()) {
                    pixel = pixelMap.get(pixelID);

                    attrs = pixel.getKeys();
                    for (int i = 0; i < attrs.length; i++) {
                        Attribute.DoubleArray array = data.get((String) attrs[i]);
                        if (array==null){
                            array = DefaultDataFactory.getDataFactory().createDoubleArray();
                            array.setValue(new double[pixelIDs.getValue().length]);
                            data.put((String) attrs[i], array);
                        }                        
                        array.getValue()[j] = pixel.getDouble((String) attrs[i]);                        
                        e.setObject((String) attrs[i], array);
                    }
                    j++;                    
                }
            }
        }
        getModel().getRuntime().println("Pixel parameter file processed ...", JAMS.VERBOSE);
    }
    
    
    
}
