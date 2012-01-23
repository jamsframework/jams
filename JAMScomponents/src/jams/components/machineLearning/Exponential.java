/*
 * Exponential.java
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

/**
 *
 * @author Christian Fischer
 */

public class Exponential implements ActivationFunction {

    double a;
    double b;
    
    public Exponential(Matrix M) {
        try {
            double[] params;
            params = getParams(M);
            a = params[0];
            b = params[1];
        } catch (Exception e) {
            a = 0;
            b = 0;
       }
    }
    public Exponential() {
        this(1,1);
    }

    public Exponential(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public double calculate(double x) {
        return a * Math.pow(Math.E, b*x);
    }

    public ActivationFunction derive() {
        double aNew = a * b * (Math.log(a)/Math.log(Math.E) + 1);
        return new Exponential(aNew, b);
    }

    public String getDescription() {
        return a+" - "+b;
    }

    public double[] getParams() {
        double[] params = new double[2];
        params[0] = a;
        params[1] = b;
        return params;
    }

    public int getType() {
        return ActivationFunction.EXPO;
    }


    private double[] getParams(Matrix D) throws Exception {
        return Regression.getExpoParams(D);
    }
}