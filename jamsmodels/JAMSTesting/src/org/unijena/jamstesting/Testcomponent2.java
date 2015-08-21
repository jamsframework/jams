/*
 * Testcomponent.java
 *
 * Created on 8. September 2005, 16:32
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

package org.unijena.jamstesting;
import org.unijena.jams.model.*;
import org.unijena.jams.data.*;

/**
 *
 * @author S. Kralisch
 */

public class Testcomponent2 extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            lowerBound = 0,
            upperBound = 1000,
            unit = "L/min",
            description = "This is a short description"
            )
            public Attribute.Double value;
    
    public void init() {

    }
    
    public void run() {
        System.out.println(value);
    }
    
    public void cleanup() {
        System.out.println(this.getInstanceName() + ": " + value);
    }
    
}
