/*
 * CAProxy.java
 * Created on 07.01.2013, 16:45:45
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

package jams.components.concurrency;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
 @JAMSComponentDescription(
        title="Context Attribute Proxy",
        author="Sven Kralisch",
        description="This is a helper component, serving as a proxy for context attributes.",
        date = "2013-01-07",
        version = "1.0_0"
        )
public class CAProxy extends JAMSComponent {

    /*
     *  Component attributes
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ        // type of access, i.e. READ, WRITE, READWRITE
            )
            public Attribute.Double[] attributes;                // for a list of attribute types, see jams.data.Attribute  
                
    /*
     *  Component run stages
     */
    
    @Override
    public void init() {
    }

    @Override
    public void run() {
    }

    @Override
    public void cleanup() {
    }
}