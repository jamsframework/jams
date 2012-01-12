/*
 * Neuron.java
 * Created on 12. Mai 2006, 18:21
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */


package optas.optimizer.nn;

import java.util.*;
import java.util.Map.Entry;
import javax.swing.*;
import java.io.FileWriter;

/**
 *
 * @author Christian Fischer
 */

public class InputNeuron extends Neuron {
    
    public InputNeuron() {
        this.initalize();
    }
        
    public void SetInput(double value) {
        input = value;
    }
/*	                 
   public void propagate() {
        //calculate activation
        calc(input);
        
	Iterator<Entry<Neuron,Double>> e = Successors.entrySet().iterator();
	
	while (e.hasNext()) {
	    Entry<Neuron,Double> entr = e.next();	    
	    entr.getKey().addToInput(this.activation*entr.getValue());
	}
    }          */
}
