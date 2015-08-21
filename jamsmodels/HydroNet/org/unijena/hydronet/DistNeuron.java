/*
 * DistNeuron.java
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
/**
 *
 * @author Christian Fischer
 */

public class DistNeuron extends Neuron {    
    private NONeuron NitrNeuron = null;    
    private CostNeuron CostNeuron = null;
    
    private double initialInput;
    private double lastDelta;
    
    protected double minValue = Double.MIN_VALUE,
                     maxValue = Double.MAX_VALUE;
    //lerning rate
    static double eta = 0.00000000001;
    //momentum
    static double alpha = 0.0;
    
    public DistNeuron() {
        super();
        lastDelta = 0;        
    }
     
    public void setMinInput(double min) {
        this.minValue = min;
    }
    
    public void setMaxInput(double max) {
        this.maxValue = max;
    }
    
    public void setNitrNeuron(NONeuron NitrNeuron) {
        this.NitrNeuron = NitrNeuron;
    }
    public void setCostNeuron(CostNeuron CostNeuron) {
        this.CostNeuron = CostNeuron;
    }
                
    public void propagate() {
        //propagate activation to neurons in next layer
        if (this.NitrNeuron != null)
            this.NitrNeuron.addToInput(input);
        if (this.CostNeuron != null)
            this.CostNeuron.addToInput(input);	   	
    }
    
    public void backpropagate() {
        calcDelta(this.NitrNeuron.getDelta()+this.CostNeuron.getDelta());
    }
    
    public double calc() {
        return activation = input;
    }
    
    public void reset() {
	input = 0;
    }
    
    public double calcDelta(double error) {
        return (calcDelta(error, 1));
    }
    
    public double calcDelta(double error, double buffer) {
        return delta = error;        
    }
    
    public void setInitialExternInput(double value) {
        initialInput = value;
	input = initialInput;
	if (this.CostNeuron != null)
	    this.CostNeuron.setInitialInput(value);
	else
	    {
	    System.out.println("argggh distribution neuron without costneuron found!!");
	    }
    }
    
    public double getInitalExternInput() {
	return initialInput;
    }
    
    public void modifyWeight() {
        lastDelta = eta * delta * input + alpha * lastDelta;
        
        input = input + lastDelta;
        	
        if (input < this.minValue)
            input = minValue;
        if (input > this.maxValue)
            input = maxValue;
    }
    
}
