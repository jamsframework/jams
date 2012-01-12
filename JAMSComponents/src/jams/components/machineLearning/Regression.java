/*
 * Regression.java
 * Created on 12. Mai 2006, 19:06
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


/**
 * Description:  This Class manages a set of 2D points and provides a method to compute
 *				 a polynom as result of nonlinear regression via solution of a least
 *				 squares minimization problem. Most of the code, especially the classes
 *				 Matrix, Numeric and specialFunctions, comes from Brian Lewis,
 *				 url: http://www.mcs.kent.edu/~blewis/
 * Copyright:    Copyright (c) 2000
 * Company:      FSU Jena
 * @author:      Sven Kralisch
 */

package jams.components.machineLearning;



public class Regression {

    public static int errorCount;

    public static double[] getPolyParams(Matrix D, int degree) throws Exception {
        return getRegressionParams(D, degree);
    }

    public static double[] getExpoParams(Matrix D) throws Exception {

        double[] result;
        //make some preprocessing with the sampling points
        //replace (x,y) pairs by (x,ln(y)) pairs
        for (int i=0; i<D.rows; i++) {
            D.element[i][1] = Math.log(D.element[i][1])/Math.log(Math.E);
        }
        result = getRegressionParams(D, 1);
        result[0] = Math.pow(Math.E, result[0]);

        return result;
    }

    public static double[] getRegressionParams(Matrix D, int degree) throws Exception {

		degree = Math.min(D.rows-1, degree);
        double[] result = new double[degree+1];

        if ((degree > 5) || (degree < 0)) {
			// return identity function
			double[] dummy = {0,1};
			return(dummy);
		}

        //check if we have at least two different sampling points
        int c = 1;
        while ((c<D.rows-1) && (D.element[0][0] == D.element[c][0]))
            c++;

        if (c == D.rows-1) {
            errorCount++;
            throw(new Exception("invalid set of sampling points"));
        }

		int i, j, k;
		// create a model matrix (normal equations)
		j = degree+1;
		Matrix A = new Matrix(D.rows, j, 1);
		for(k=1; k<A.columns; k++){
			for(i=0; i<A.rows; i++){
				A.element[i][k] = Math.pow(D.element[i][0],k);
			}
		}

		// form QR decomposition of A
		Matrix Q = A.Q();
		Matrix R = Matrix.multiply(Q.transpose(), A);
		Matrix b = Matrix.multiply(Q.transpose(), D.sub(0, D.rows-1, 1, 1));

		// truncate the non-square portion...
		b = new Matrix(b.sub(0, R.columns-1, 0, 0));
		R = new Matrix(R.sub(0, R.columns-1, 0, R.columns-1));
		Matrix x = Matrix.divide(b,R);
		// x now contains the model fit

		for (j = 0; j <= degree; j++) {
			result[j] = 0;
		}
		for (j = 0; j < x.rows; j++) {
//			System.out.getRuntime().println(x.element[j][0]);
			result[j] = x.element[j][0];
		}

		return(result);
		// calculate the residual norm...
//		Matrix ax = Matrix.multiply(A,x);
//		Matrix r = Matrix.subtract(ax,b);
//		double ssr = Math.pow(r.norm(),2);

    }
}
