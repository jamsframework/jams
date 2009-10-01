/*
 * ActivationFunction.java
 * Created on 12. Mai 2006, 18:08
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


/**
 *
 * @author Christian Fischer
 */
public interface ActivationFunction {    
        
    static final int POLY = 1;
    static final int EXPO = 2;
    static final int LINAPPROX = 3;  
    static final int LOGISTIC = 4;
    static final int GEN = 5;
    
    public double calculate(double x);
    
    public ActivationFunction derive();
    
    public String getDescription();

    public double[] getParams();
    
    public int getType();
}