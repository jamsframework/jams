/*
 * TSDataReader.java
 * Created on 11. November 2005, 10:10
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

package jams.components.machineLearning;

import org.unijena.j2k.statistics.*;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import org.unijena.jams.io.*;
import java.util.*;
import java.io.*;
import org.unijena.jams.JAMS;

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
