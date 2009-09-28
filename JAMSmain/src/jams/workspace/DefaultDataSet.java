/*
 * DefaultDataSet.java
 * Created on 31. Januar 2008, 21:57
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package jams.workspace;

/**
 *
 * @author Sven Kralisch
 */
public class DefaultDataSet implements DataSet {

    DataValue[] data;

    public DefaultDataSet(int size) {
        data = new DataValue[size];
    }
    
    public DataValue[] getData() {
        return data;
    }

    public void setData(int index, DataValue data) {
        this.data[index] = data;
    }    
    
    public void setData(DataValue[] data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        
        String result = "";

        if (data.length == 0) {
            return result;
        } else {
            result += data[0].getString();
        } 
        
        for (int i = 1; i < data.length; i++) {
            result += "\t" + data[i].getString();
        }
        
        return result;
    }
    
}

