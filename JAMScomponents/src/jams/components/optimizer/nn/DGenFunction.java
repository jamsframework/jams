/*
 * LinApprox.java
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

package jams.components.optimizer.nn;

//import org.unijena.j2k.hydronet.Regression;
/**
 *
 * @author Christian Fischer
 */

public class DGenFunction
        implements ActivationFunction {
    
    private double p,s,q;
        
    public DGenFunction(double s,double p,double q) {
        this.p = p;
	this.s = s;
	this.q = q;
    }
    
       
    public double calculate(double x) {	
	return s*p*Math.cos(p*x);
    }
    
    public double[] getParams() {
        double[] params = null;
        return (params);
    }
    
    public String getDescription() {
        return ("");
    }
    
//derivation is not supported
    public ActivationFunction derive() {		
	return this;
    }
        
    
    public int getType() {
        return ActivationFunction.GEN;
    }
}