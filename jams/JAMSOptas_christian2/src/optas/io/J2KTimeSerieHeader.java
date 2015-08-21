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
package optas.io;

import jams.data.Attribute;
import jams.data.Attribute.TimeInterval;
import jams.data.DefaultDataFactory;
import java.util.*;
import optas.core.OPTASException;

/**
 *
 * @author S. Kralisch
 */
public class J2KTimeSerieHeader {

    public enum Dimension{X,Y};
    
    private Attribute.Calendar startTime = DefaultDataFactory.getDataFactory().createCalendar();
    private Attribute.Calendar endTime = DefaultDataFactory.getDataFactory().createCalendar();    
    private double missingDataValue = -9999;
    private String missingDataString = "-9999";

    private String projection = "unknown";
    private String nameOfXDimension = "x";
    private String nameOfYDimension = "y";

    private String[] attributeNames = null;
    private double[] ids = null;
    private double[] statx = null;
    private double[] staty = null;
    private double[] statelev = null;
    private String tres = null;

    private int columnCount = 0;
    
    public J2KTimeSerieHeader() {
        init(0);
    }
            
    public void init(int columnCount) {        
        this.columnCount = columnCount;
        attributeNames = new String[columnCount];
        ids = new double[columnCount];
        statx = new double[columnCount];
        staty = new double[columnCount];
        statelev = new double[columnCount];
    }

    public int getColumnCount() {
        return columnCount;
    }

    public String[] getAttributeNames() {
        return attributeNames;
    }

    public void setAttributeName(int i, String attributeName) {
        this.attributeNames[i] = attributeName;
    }

    public void setAttributeNames(String attributeNames[]) {
        if (attributeNames.length != columnCount) {
            throw new IllegalArgumentException("Cannot set attribute names! The number of attributes from the argument (" + attributeNames.length + ") does not match the number of attributes in the J2KTimeSerieIO (" + this.attributeNames.length + ")");
        }
        this.attributeNames = Arrays.copyOf(attributeNames, columnCount);
    }

    public int getTimeUnit() {
        if (tres == null) {
            return -1;
        }
        if (tres.compareTo("d") == 0) {
            return 6;
        } else if (tres.compareTo("h") == 0) {
            return 11;
        } else if (tres.compareTo("m") == 0) {
            return 2;
        } else if (tres.compareTo("y") == 0) {
            return 1;
        }
        throw new OPTASException("Cannot set time unit! Unit " + tres + " is unknown.");
    }

    public int setTimeUnit(int unit) {
        switch (unit) {
            case 1:
                tres = "y";
                break;
            case 2:
                tres = "m";
                break;
            case 6:
                tres = "d";
                break;
            case 11:
                tres = "h";
                break;
        }
        throw new OPTASException("Cannot set time unit! Time unit " + unit + " is not supported!");
    }
    
    public void setTimeUnit(String unit) {
        tres = unit;
        //test if unit is valid        
        getTimeUnit();       
    }

    public double getMissingDataValue() {
        return this.missingDataValue;
    }

    public String getMissingDataString() {
        return this.missingDataString;
    }

    public void setMissingDataValue(double missingDataValue) {
        this.missingDataValue = missingDataValue;
        this.missingDataString = Double.toString(missingDataValue);
    }

    public void setMissingDataString(String missingDataString) {
        this.missingDataString = missingDataString;
        this.missingDataValue = Double.parseDouble(missingDataString);
    }

    public String getNameOfDimension(Dimension dim) {
        switch (dim){
            case X: return nameOfXDimension;
            case Y: return nameOfYDimension;            
        }
        return null; //should not happen
    }
    
    public void setNameOfDimension(Dimension dim, String name){
        switch(dim){
            case X: this.nameOfXDimension = name; break;
            case Y: this.nameOfYDimension = name; break;
        }
    }
    
    public double[] getLocation(int i) {
        return new double[]{statx[i], staty[i]};
    }

    public void setLocation(int i, double statx, double staty) {
        this.statx[i] = statx;
        this.staty[i] = staty;
    }

    public double[] getIds() {
        return this.ids;
    }

    public double[] getElevation() {
        return statelev;
    }
    
    public void setElevation(int i, double elevation) {
        this.statelev[i] = elevation;
    }

    public void setElevation(double elevation[]) {
        if (elevation.length != columnCount) {
            throw new OPTASException("Cannot set elevation! The number of attributes from the argument (" + elevation.length + ") does not match the number of attributes in the J2KTimeSerieIO (" + this.statelev.length + ")");
        }
        statelev = Arrays.copyOf(elevation, getColumnCount());
    }

    public double getId(int i) {
        return this.ids[i];
    }

    public void setIds(double ids[]) {
        if (ids.length != columnCount) {
            throw new OPTASException("Cannot set ids! The number of attributes from the argument (" + ids.length + ") does not match the number of attributes in the J2KTimeSerieIO (" + this.ids.length + ")");
        }
        this.ids = Arrays.copyOf(ids, columnCount);
    }

    public void setId(int i, double id) {
        this.ids[i] = id;
    }

    public void setStartTime(Date d) {
        this.startTime.setTime(d);
    }

    public Date getStartTime() {
        return this.startTime.getTime();
    }

    public void setEndTime(Date d) {
        this.endTime.setTime(d);
    }

    public Date getEndTime() {
        return this.endTime.getTime();
    }

    public void setProjection(String proj, String nameOfXDimension, String nameOfYDimension) {
        this.projection = proj;
        this.nameOfXDimension = nameOfYDimension;
        this.nameOfYDimension = nameOfYDimension;
    }

    public String getProjection() {
        return projection;
    }  
    
    public Attribute.TimeInterval getTemporalDomain(){
        TimeInterval ti = DefaultDataFactory.getDataFactory().createTimeInterval();
        ti.setStart(startTime);
        ti.setEnd(endTime);
        ti.setTimeUnit(getTimeUnit());
        ti.setTimeUnitCount(1);
        return ti;
    }
}
