/*
 * HydroNETCalc.java
 * Created on 24. Mai 2006, 16:03
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
 * @author Christian Fischer
 */
public class HydroNETCalc extends JAMSComponent {
    @JAMSComponentDescription(
        title="HydroNETCalc",
        author="Christian Fischer",
        description=""
        )    
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Collection of hru objects"
            )
            public Attribute.Entity NitrogenOutEntity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Collection of hru objects"
            )
            public Attribute.Entity CostOutEntity;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "largest accepted nitrogen value",
            defaultValue= "0.0"
            )
            public Attribute.Double nitrogen_goal;    
    
    @Override
    public void run() throws Attribute.Entity.NoSuchAttributeException {            
	DistNeuron NitrogenOutNeuron = (DistNeuron)NitrogenOutEntity.getObject("NEURON");
	DistNeuron CostOutNeuron = (DistNeuron)CostOutEntity.getObject("NEURON");
	
        NitrogenOutNeuron.calc();
        CostOutNeuron.calc();        	
		
	NitrogenOutNeuron.reset();
	CostOutNeuron.reset();	        
        
        //second step backpropagate        
        NitrogenOutNeuron.setDelta(nitrogen_goal.getValue()-NitrogenOutNeuron.getActivation());
        CostOutNeuron.setDelta(-CostOutNeuron.getActivation());       
    }
}


