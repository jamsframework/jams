/*
 * NONeuron.java
 * Created on 12. Mai 2006, 18:21
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

import java.util.Enumeration;
import java.util.Vector;
import javax.swing.*;
import java.io.FileWriter;

/**
 *
 * @author Christian Fischer
 */

public class NONeuron extends Neuron {    
    private Neuron downstreamNeuron = null;
    private double downstreamWeight;
    
    private Neuron outputNeuron = null;
    private double outputWeight;
    
    public NONeuron() {
        super();
    }
        
    public void setDownstreamNeuron(Neuron dsNeuron, double weight) {
        downstreamNeuron = dsNeuron;
        downstreamWeight = weight;
    }
    public double getDownstreamWeight() {
	return downstreamWeight;
    }
    
    public double getOutputWeight() {
	return outputWeight;
    }
    
    public void setOutputNeuron(Neuron dsNeuron, double weight) {
        outputNeuron = dsNeuron;
        outputWeight = weight;
    }
        
    public void propagate() {
        //calculate activation
        calc();
        //reset input
	input = 0;
        //propagate activation to neurons in next layer
        if (this.downstreamNeuron != null)
            this.downstreamNeuron.addToInput(activation*this.downstreamWeight);
        if (this.outputNeuron != null)
            this.outputNeuron.addToInput(activation*this.outputWeight);       
    }
    
     public void backpropagate() {
        double dsDelta = 0;
	if (this.downstreamNeuron != null)
	    dsDelta = this.downstreamNeuron.getDelta()*this.downstreamWeight;
        double outputDelta = this.outputNeuron.getDelta()*this.outputWeight;
        calcDelta(dsDelta+outputDelta);
    }
     
    public double calc() {	
        lastInput = input;
        //no activation function
        if (ActivationFunctions.size() == 0) {
            activation = input;	    	    
	    return activation;
        }  	 			
        return activation = calc(input);
    }
    
    public double calcDelta(double error) {
        return (calcDelta(error, 1));
    }
    
    public double calcDelta(double error, double buffer) {        
        if (ActivationFunctions.size() == 0) {
            return delta = error;
        }                        
        return delta = calcDelta(lastInput,error,buffer);
    }        
}
