/*
 * EntityFreezer.java
 * Created on 12. Juni 2008, 10:20
 *
 * This file is a JAMS component
 * Copyright (C) FSU Jena
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

package jams.components.debug;

import org.unijena.jams.data.JAMSEntityCollection;
import org.unijena.jams.data.JAMSFileName;
import org.unijena.jams.model.JAMSComponent;
import org.unijena.jams.model.JAMSComponentDescription;
import org.unijena.jams.model.JAMSVarDescription;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
        title="EntityFreezer",
        author="Sven Kralisch",
        date="12.06.2008",
        description="Serialize a set of entities (a JAMSEntityCollection object)" +
        " and write it to a file. Can be used in conjunction with EntityDeFreezer" +
        " to store a certain state of model entities and reuse it at a later point" +
        ", e.g. for using formerly calculated spatial attribute values as a starting" +
        " point for subsequent simulations."
        )
public class EntityFreezer extends JAMSComponent {

    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Entity collection to be serialized"
            )
            public JAMSEntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Ouput file name"
            )
            public JAMSFileName fileName;
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }

    public void run() {
        
    }

    public void cleanup() {
        
    }
}
