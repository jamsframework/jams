/*
 * SEProvider.java
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
import java.util.*;

/**
 *
 * @author S. Kralisch
 */

@JAMSComponentDescription(
        title = "SEProvider",
        author = "Sven Kralisch",
        description= ""
        )
        public class SEReader extends JAMSComponent {
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.EntityCollection entities;
    
    
    public void init() throws Exception {
        
        System.out.println("SEReader init");
        
        Attribute.Entity[] ea = entities.getEntityArray();
        for (int i = 0; i < ea.length; i++) {
            //System.out.println(ea[i].getDouble("attr"));
            ea[i].setDouble("landuse", 47);
        }
        
    }
    
    public void run(){
    }
    
    public void cleanup(){
    }
}
