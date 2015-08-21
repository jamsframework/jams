/*
 * Climate.java
 * Created on 30. September 2005, 11:37
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

package org.unijena.abc;

import org.unijena.jams.model.*;
import org.unijena.jams.data.*;
import org.unijena.jams.io.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author S. Kralisch
 */
public class LeafRiverDataReader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.String fileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            )
            public Attribute.Double precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            )
            public Attribute.Double obsRunoff;
 
    private JAMSTableDataStore store;
    public void init(){
        store = new GenericDataReader(fileName.getValue(), true, 1, 2);
    }
    
    public void run(){
        
        JAMSTableDataArray da = store.getNext();
        double[] vals = JAMSTableDataConverter.toDouble(da);
        this.precip.setValue(vals[0]);
        this.obsRunoff.setValue(vals[1]);
    }
    
    public void cleanup(){
        store.close();
    }
    
}
