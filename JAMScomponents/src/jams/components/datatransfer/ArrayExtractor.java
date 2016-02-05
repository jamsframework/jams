/*
 * DoubleSetter.java
 * Created on 23. Februar 2006, 23:00
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

package jams.components.datatransfer;

import jams.data.*;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
        title="ArrayExtractor",
        author="Sven Kralisch",
        description="Component for converting Attribute.DoubleArray to "
        + "Attribute.Double[], this way allowing to access the double values "
        + "stored in a Attribute.DoubleArray directly within a JAMS model.",
        version="1.0_0",
        date="2006-02-23"
        )
public class ArrayExtractor extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Double attributes to be set"
            )
            public Attribute.DoubleArray in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Double value"
            )
            public Attribute.Double[] out;
    
    @Override
    public void run() {
        int max = Math.min(out.length, in.getValue().length);
        double[] array = this.in.getValue();
        for (int i=0; i<max; i++){
            if (i < array.length)
                out[i].setValue(array[i]);
        }
    }
}
