/*
 * LinApprox.java
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package jams.components.machineLearning;

//import org.unijena.j2k.hydronet.Regression;
/**
 *
 * @author Christian Fischer
 */

public class LogisticFunction
        implements ActivationFunction {

    private double beta;
        
    public LogisticFunction(double beta) {
        this.beta = beta;
    }
    
       
    public double calculate(double x) {
	return 1.0/(1.0 + Math.exp(-beta*x));
    }
    
    public double[] getParams() {
        double[] params = null;
        return (params);
    }
    
    public String getDescription() {
        return ("");
    }
    
//derive polynom, return polynom of degree (n-1)
    public ActivationFunction derive() {
	DLogisticFunction derivation = new DLogisticFunction(beta);
	return derivation;
    }
        
    
    public int getType() {
        return ActivationFunction.LOGISTIC;
    }
}