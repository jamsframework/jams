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


package jams.components.machineLearning;

import java.util.*;
import java.util.Map.Entry;
import javax.swing.*;
import java.math.*;
import java.io.FileWriter;
import jams.components.machineLearning.NeuralConnection.*;
/**
 *
 * @author Christian Fischer
 */

public class Neuron {

    protected double activation;
    protected double delta;
    protected double error;
    
    protected double lastInput;
    protected double input;

    protected long ID;
        
    protected Vector<GenericFunction> ActivationFunctions = new Vector<GenericFunction>();
    protected Vector<NeuralConnection> OutputConnection = new Vector<NeuralConnection>();
    protected Vector<NeuralConnection> InputConnection = new Vector<NeuralConnection>();
                    
    static double learningRate = 0.0;
       
    public Neuron() {
        this.initalize();
    }
    
    public void setID(long ID) {
	this.ID = ID;
    }
    
    public void initalize() {
        activation = 0;
        delta = 0;
        lastInput = 0;
    }
    public void reset() {
	this.activation = 0;
	this.input = 0;
	this.delta = 0;
    }
    
    public void addToInput(double value) {
        input += value;
    }
	
    public void addFilter(GenericFunction af) {
        ActivationFunctions.add(af);
    }
            
    public GenericFunction getFilter(int i) {	
	return (GenericFunction)ActivationFunctions.get(i);
    }
     
   public void propagate() {
        //calculate activation
        calc(input);
        //reset input
	input = 0;
        		
	for (int i=0;i<this.OutputConnection.size();i++) {
	    NeuralConnection connection = this.OutputConnection.get(i);
	    
	    connection.dest.addToInput(this.activation*connection.Weight);
	    }		
    }
   
    public void backpropagate() {
	calcDelta(lastInput,error,1);
	//reset error
	error = 0;
	
	for (int i=0;i<this.InputConnection.size();i++) {
	    NeuralConnection connection = this.InputConnection.get(i);
	    
	    connection.src.addToError(this.delta*connection.Weight);
	    }	  		
    }
       
    protected double calc(double value) {        
        GenericFunction gc;
        ActivationFunction af;
        
	lastInput = input;
        //no activation function
        if (ActivationFunctions.size() == 0) {
            activation = input;	    	    
	    return activation;
        }  	
	
	activation = 0;
        for (Enumeration e = ActivationFunctions.elements(); e.hasMoreElements(); ) {
            gc = (GenericFunction) e.nextElement();
            af = gc.getFunction();

            activation += af.calculate(value);
        }
	
        return activation;
    }
        
    protected double calcDelta(double value,double error, double buffer) {
        ActivationFunction af;
        GenericFunction gc;
        delta = 0;                        

	if (ActivationFunctions.size() == 0) {
            return delta = error;
        }     
	
        for (Enumeration e = ActivationFunctions.elements(); e.hasMoreElements(); ) {
            gc = (GenericFunction) e.nextElement();
            af = gc.getDFunction();

            delta += af.calculate(value)* error * buffer; // */this.activation*(1.0-this.activation);
        }                    
	
        return delta;
    }

    protected double updateWeightDelta() {
	for (int i=0;i<this.InputConnection.size();i++) {
	    NeuralConnection connection = this.InputConnection.get(i);
	    
	    connection.Weight_Delta += delta*connection.src.getActivation()*learningRate;	    	    	    	  
	    }	    
	return 0;
    }
    
    protected double adjustWeight() {				
	for (int i=0;i<this.InputConnection.size();i++) {
	    NeuralConnection connection = this.InputConnection.get(i);
	    	    
	    connection.update();
	    }	    
	return 0;
    }

     protected void AddConnection(Neuron Predecessors,Neuron Successor,double weight) {
	NeuralConnection connection = new NeuralConnection();
	connection.Weight = weight;
	connection.Weight_Delta = 0;
	connection.src = Predecessors;
	connection.dest = Successor;
	connection.oldWeightDelta = 0;
	
	Predecessors.OutputConnection.add(connection);
	Successor.InputConnection.add(connection);
    }
     
    protected void AddConnection(Neuron Successor,double weight) {
	NeuralConnection connection = new NeuralConnection();
	connection.Weight = weight;
	connection.Weight_Delta = 0;
	connection.src = this;
	connection.dest = Successor;
	connection.oldWeightDelta = 0;
	
	this.OutputConnection.add(connection);
	Successor.InputConnection.add(connection);
    }
    
    public void addToError(double delta) {
        this.error += delta;
    }
    
    public int getFilterCount() {
	return ActivationFunctions.size();
    }
    
    public double getInput() {
	return this.input;
    }
    
    public long getID() {
	return this.ID;
    }
    
    public double getDelta() {
        return delta;
    }
    
    public double getActivation() {
        return (activation);
    }
        
    public void resetFunctions() {
        this.ActivationFunctions.clear();
    }
    
    public void writeData(FileWriter f) {
        //insert debug output here!!
    }
}
