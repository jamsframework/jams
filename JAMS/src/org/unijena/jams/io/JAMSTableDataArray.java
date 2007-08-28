/*
 * JAMSTableDataArray.java
 *
 * Created on 5. Oktober 2005, 17:19
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
package org.unijena.jams.io;

import org.unijena.jams.data.*;
/**
 *
 * @author S. Kralisch
 */
public class JAMSTableDataArray {
    
    private JAMSCalendar time;
    private String[] values;
    
    public JAMSTableDataArray(JAMSCalendar time, String[] values) {
        this.time = time;
        this.setValues(values);
    }
    
    public JAMSTableDataArray(JAMSCalendar time) {
        this.time = time;
    }
        
    public JAMSCalendar getTime() {
        return time;
    }
    
    public void setTime(JAMSCalendar time) {
        this.time = time;
    }
    
    public String[] getValues() {
        return values;
    }
    
    public void setValues(String[] values) {
        this.values = values;
    }
    
    public int getLength() {
        return values.length;
    }
}


