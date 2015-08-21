/*
 * CostNeuron.java
 * Created on 15. Mai 2006, 19:15
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

public class CostNeuron extends Neuron {                        
//global costfactor for all neurons
    private static double costFactor = 0.0001;    
        
    private Neuron outputNeuron = null;
    private double initialInput;            
    
    public CostNeuron() {
        super();
    }
            
    public void setOutputNeuron(Neuron dsNeuron) {
        outputNeuron = dsNeuron;
    }
    
    public void addToInput(double value) {
        input += value;
    }
    
    public static void setCostFactor(double costFactor) {
        costFactor = costFactor;
    }
    public static double getCostFactor() {
        return costFactor;
    }
    
    //minimal nitrogen input which causes no costs
    public void setInitialInput(double initialWeight) {
        this.initialInput = initialWeight;
    }
    
    public double getInitialInput() {
        return initialInput;
    }
    
    public void propagate() {
        //calculate activation
        calc();
        
        //reset input
        input = 0;

        //propagate activation to neurons in next layer
        if (this.outputNeuron != null)
            this.outputNeuron.addToInput(activation);        
    }
    
    public void backpropagate() {
        calcDelta(this.outputNeuron.getDelta());
    }
    
    public double calc() {
        lastInput = input;
        //no activation function
        if (ActivationFunctions.size() == 0) { 
            return activation = 0;
        }
        if (initialInput == 0) {
            if (lastInput != 0) {
                initialInput = input;
            } 
            else {	
                return activation = 0;
            }
        }
        
        double adaptvalue = lastInput / initialInput;

        activation = calc(adaptvalue) * sizeOfArea * costFactor;
       	
        return activation;
    }
    
    public double calcDelta(double error) {
        return (calcDelta(error, 1));
    }
    
    public double calcDelta(double error, double buffer) {
        if (ActivationFunctions.size() == 0) {            
            return 0;
        }
        
        if (initialInput == 0) {
            if (lastInput != 0) {
                initialInput = this.lastInput;
            } else {
                return delta = 0;
            }
        }        
        double adaptvalue = lastInput / initialInput;
                
        delta = calcDelta(adaptvalue,error,1) * sizeOfArea * costFactor;
                
        return delta;
    }             
}
