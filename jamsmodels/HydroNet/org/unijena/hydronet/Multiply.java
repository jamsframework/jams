/*
 * Multiply.java
 * Created on 27. October 2006, 14:48
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

package org.unijena.hydronet;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author C. Fischer
 */
public class Multiply extends JAMSComponent {
    
    @JAMSVarDescription(
	    access = JAMSVarDescription.AccessType.READWRITE,
	    update = JAMSVarDescription.UpdateType.RUN,
	    description = "value attribute"
	    )
	    public Attribute.Double[] value;
    
    @JAMSVarDescription(
	    access = JAMSVarDescription.AccessType.READWRITE,
	    update = JAMSVarDescription.UpdateType.RUN,
	    description = "multiplier"
	    )
	    public Attribute.Double multiplier;


    @Override
    public void run() throws Attribute.Entity.NoSuchAttributeException {
	double mul = multiplier.getValue();
    
        for (int i=0;i<value.length;i++) {
	    value[i].setValue(value[i].getValue()*mul);
	}
    }
}