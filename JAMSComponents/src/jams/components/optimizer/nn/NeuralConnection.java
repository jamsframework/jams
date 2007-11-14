/*
 * NeuralConnection.java
 *
 * Created on 13. April 2007, 15:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.optimizer.nn;

import jams.components.optimizer.nn.Neuron;
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
    public boolean locked = false;
    
    static public double MAXW = 100000.0;
    static public double momentum = 0.0;
    
    public NeuralConnection() {
	src = null;
	dest = null;
	
	Weight = 0.0;
	Weight_Delta = 0.0;
	oldWeightDelta = 0.0;
    }
    
    public void setLock(boolean lockState) {
        locked = lockState;
    }
    
    public void update() {
        if (locked) {
            return;
        }
	oldWeightDelta = Weight_Delta + momentum*oldWeightDelta;
	
	if (oldWeightDelta > 1.05)
	    oldWeightDelta = 1.05;
	if (oldWeightDelta < -1.05)
	    oldWeightDelta = -1.05;
	
	Weight_Delta = 0;
	Weight = Weight + oldWeightDelta;
	
	if (Weight > MAXW)
	    Weight = MAXW;
	if (Weight < -MAXW)
	    Weight = -MAXW;
    }
    
}
