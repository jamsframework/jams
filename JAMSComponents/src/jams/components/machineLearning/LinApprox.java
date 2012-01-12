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

public class LinApprox
        implements ActivationFunction {
    private Matrix M;
        
    public LinApprox(Matrix M) {
        this.M = new Matrix(M.toString());
        this.order();
    }
    
    public Matrix getData() {
	return M;
    }
    
    protected void swap(Matrix M, int i, int j) {
        double tempx = M.element[i][0];
        double tempy = M.element[i][1];
        
        M.element[i][0] = M.element[j][0];
        M.element[i][1] = M.element[j][1];
        
        M.element[j][0] = tempx;
        M.element[j][1] = tempy;
        
    }
    
    protected void order() {
        /**
         * brute force (n*n) ordering of the sampling points
         */
        double max;
        int index = 0;
        
        for (int k = 0; k < M.rows - 1; k++) {
            for (int j = k + 1; j < M.rows; j++) {
                if (M.element[j][0] < M.element[k][0]) {
                    swap(M, k, j);
                }
            }
        }
    }
    
    public double calculate(double x) {
        double result = 0;
        int i = 0;
        double increase = 0;
        if (x <= M.element[0][0]) {
            result = M.element[0][1];
        } else if (x > M.element[M.rows - 1][0]) {
            result = M.element[M.rows - 1][1];
        } else {
            while (x > M.element[i][0]) {
                i++;
            }
            i--;
            if (M.element[i + 1][0] == M.element[i][0]) {
                result = M.element[i][1];
            } else {
                result = M.element[i][1] +
                        (M.element[i + 1][1] - M.element[i][1]) /
                        (M.element[i + 1][0] - M.element[i][0]) * (x - M.element[i][0]);
            }
        }        
        return (result);
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
        
        double increase;
        Matrix D = new Matrix(M.rows -1, M.columns);
        
        for (int i = 0; i < M.rows - 1; i++) {
            
            if (M.element[i + 1][0] == M.element[i][0]) {
                increase = 0;
            } else {
                increase = (M.element[i + 1][1] - M.element[i][1]) /
                        (M.element[i + 1][0] - M.element[i][0]);
                
            }
            D.element[i][0] = M.element[i][0];
            D.element[i][1] = increase;
            
        }
        
/*        D.element[M.rows][0] = M.element[M.rows - 1][0];
        D.element[M.rows][1] = 0;
        
        D.element[0][0] = M.element[0][0] - 0.01;
        D.element[0][1] = 0;
        
        increase = (M.element[M.rows - 1][1] - M.element[0][1]) /
                (M.element[M.rows - 1][0] - M.element[0][0]);*/
        return new DLinApprox(D);
    }
    
    public int getType() {
        return ActivationFunction.LINAPPROX;
    }
}