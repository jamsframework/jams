/*
 * ABCGradientDescent.java
 * Created on 30. Juni 2006, 15:12
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

package optas.optimizer;

import java.util.Comparator;
import java.util.Vector;

/**
 *
 * @author Christian Fischer
 */

public class SCEM_UA_Comparator implements Comparator {

    private int col = 0;
    private int order = 1;
    
    public SCEM_UA_Comparator(int col,boolean decreasing_order) {
	this.col = col;
	if (decreasing_order)
	    order = -1;
	else
	    order = 1;
    }

    public int compare(Object d1, Object d2) {

        double[] b1 = (double [])d1;
        double[] b2 = (double [])d2;
        
	if (b1[col] < b2[col])
	    return -1*order;
	else if (b1[col] == b2[col])
	    return 0*order;
	else
	    return 1*order;
    }
} 

