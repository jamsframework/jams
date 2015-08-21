/*
 * Neuron.java
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

public class Neuron {
    protected double sizeOfArea;
    protected double activation;
    protected double delta;
    
    protected double lastInput;
    protected double input;

    protected long ID;
    
    protected Vector ActivationFunctions = new Vector();
                
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
	ID = 0;
    }
    public void reset() {
	this.activation = 0;
	this.input = 0;
	this.delta = 0;
    }
    public void addToInput(double value) {
        input += value;
    }
        
    public void setSizeOfArea(double sizeOfArea) {
        this.sizeOfArea = sizeOfArea;
    }
    
    public double getSizeOfArea() {
        return this.sizeOfArea;
    }    
    
    public void addFilter(GenericFunction af) {
        ActivationFunctions.add(af);
    }
    
    public int getNumberOfFilters() {
	return ActivationFunctions.size();
    }
    
    public GenericFunction getFilter(int i) {	
	return (GenericFunction)ActivationFunctions.get(i);
    }
                 
    protected double calc(double value) {        
        GenericFunction gc;
        ActivationFunction af;
        
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
                
        for (Enumeration e = ActivationFunctions.elements(); e.hasMoreElements(); ) {
            gc = (GenericFunction) e.nextElement();
            af = gc.getDFunction();

            delta += af.calculate(value) * error * buffer;
        }                    
        return delta;
    }
    
    public double getDelta() {
        return (delta);
    }
    
    public void setDelta(double delta) {
        this.delta = delta;
    }
    
    // for input layer
    public void setActivation(double input) {
        activation = input;
        lastInput = input;
    }
    
    public double getActivation() {
        return (activation);
    }
        
    public void resetFunctions() {
        this.ActivationFunctions.clear();
    }

    public double getInput() {
	return this.input;
    }
    
    public void writeData(FileWriter f) {
        //insert debug output here!!
    }
}
