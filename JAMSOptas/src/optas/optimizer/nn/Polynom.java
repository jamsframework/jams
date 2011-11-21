/*
 * Polynom.java
 * Created on 12. Mai 2006, 19:06
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
package optas.optimizer.nn;

/**
 *
 * @author Christian Fischer
 */

public class Polynom implements ActivationFunction {
    
    private double[] params;
    
    public Polynom(double[] a) {
        params = a;
    }
    public Polynom(Matrix M, int degree) {
        try {
            params = getParams(M, degree);
        } catch (Exception e) {
            double[] dummy = {0,1};
            params = dummy;
        }
    }
    public Polynom() {
        params = new double[2];
        params[0] = 0;
        params[1] = 1;
    }
    
    public double calculate(double x) {
        double result = 0;
        for (int i=0; i < params.length; i++) {
            result += params[i]*Math.pow(x,i);
        }
        return(result);
    }
    
    public double[] getParams() {
        return(params);
    }
    
    public String getDescription() {
        
/*        String result = new PrintfFormat("%8.2f").sprintf(params[0]);
        for (int i=1; i < params.length; i++) {
            result += " + " + new PrintfFormat("%8.2f").sprintf(params[i]) + "*x^" + i;
        }*/
        return "";//(result);
    }
    public String getDescription2() {
        String result = Double.toString(params[0]);
        for (int i=1; i < params.length; i++) {
            result += " + " + params[i] + "*x^" + i;
        }
        return(result);
    }
    
    private double[] getParams(Matrix D, int degree) throws Exception {
        return Regression.getPolyParams(D, degree);
    }
    
    //derive polynom, return polynom of degree (n-1)
    public ActivationFunction derive() {
        double[] a = new double[params.length-1];
        Polynom drv = new Polynom(a);
        for (int i=1; i < params.length; i++)
            drv.params[i-1] = i*params[i];
        return(drv);
    }
    
    public int getType() {
        return ActivationFunction.POLY;
    }
}
