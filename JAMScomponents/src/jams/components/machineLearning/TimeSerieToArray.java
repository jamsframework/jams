/*
 * TSDataReader.java
 * Created on 11. November 2005, 10:10
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

import jams.data.*;
import jams.model.*;
import java.util.*;

/**
 *
 * @author S. Kralisch
 */
public class TimeSerieToArray extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of data values for current time step"
            )
            public JAMSDoubleArray dataArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of data values for current time step"
            )
            public JAMSEntity CompleteArray;

    HashMap<Integer, double[]> TimeData;    
    Integer time;
    public void init() {
	TimeData = new HashMap<Integer, double[]>();	
	time = 0;
    }
    
    public void run() {
	double curData[] = new double[dataArray.getValue().length];
	
	for (int i=0;i<dataArray.getValue().length;i++) {
	    curData[i] = dataArray.getValue()[i];
	}
	
	TimeData.put(time,curData);
	
	time++;
	
	CompleteArray.setObject("Data",TimeData);
    }
}
