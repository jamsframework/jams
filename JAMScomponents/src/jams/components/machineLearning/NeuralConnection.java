/*
 * NeuralConnection.java
 *
 * Created on 13. April 2007, 15:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.machineLearning;

import jams.components.machineLearning.Neuron;
/**
 *
 * @author Christian(web)
 */
public class NeuralConnection {
    
    public Neuron src;
    public Neuron dest;
    
    public double Weight;
    public double Weight_Delta;
    public double oldWeightDelta;
    
    static public double momentum = 0.0;
    
    public NeuralConnection() {
	src = null;
	dest = null;
	
	Weight = 0.0;
	Weight_Delta = 0.0;
	oldWeightDelta = 0.0;
    }
    
    public void update() {	 	 
	oldWeightDelta = Weight_Delta + momentum*oldWeightDelta;
	
	if (oldWeightDelta > 1.05)
	    oldWeightDelta = 1.05;
	if (oldWeightDelta < -1.05)
	    oldWeightDelta = -1.05;
	
	Weight_Delta = 0;
	Weight = Weight + oldWeightDelta;
	
	if (Weight > 45.0)
	    Weight = 45.0;
	if (Weight < -45.0)
	    Weight = -45.0;
    }
    
}
