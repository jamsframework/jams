/*
 * DLinApprox.java
 * Created on 12. Mai 2006, 17:41
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

public class DLinApprox
    implements ActivationFunction {

  private Matrix M;

  public DLinApprox(Matrix M) {
    this.M = new Matrix(M.toString());
  }

  public double calculate(double x) {
    double result;

    int i = 0;

    if (x <= M.element[0][0])
    {
      result = M.element[0][1];
    }
    else if (x >= M.element[M.rows - 1][0])
    {
      result = M.element[M.rows - 1][1];
    }
    else {
      while (x > M.element[i][0]) {
        i++;
      }
      i--;

      result = M.element[i][1];
    }

    return (result);
  }

  public double[] getParams() {
    double[] params = null;
    return (params);
  }

  public String getDescription() {
    return (M.toString());
  }

  //derive polynom, return polynom of degree (n-1)
  public ActivationFunction derive() {
    return (null);
  }

  public int getType() {
    return ActivationFunction.LINAPPROX;
  }
}
