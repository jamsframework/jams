/*
 * DataTracer.java
 * Created on 28. August 2008, 13:40
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
package org.unijena.jams.dataaccess;

import java.util.ArrayList;
import java.util.HashMap;
import org.unijena.jams.data.JAMSData;
import org.unijena.jams.model.JAMSContext;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class DataTracer {

    private JAMSData[] dataObjects;
    private ArrayList<String> attributeNames = new ArrayList<String>();
    private JAMSContext context;

    public DataTracer(JAMSContext context) {
        this.context = context;
    }

    public void registerAttribute(String attributeName) {
        attributeNames.add(attributeName);
    }

    public String[] init(HashMap<String, JAMSData> dataObjectHash) {

        ArrayList<String> missingAttributes = new ArrayList<String>();
        ArrayList<JAMSData> objectList = new ArrayList<JAMSData>();

        for (String attributeName : attributeNames) {
            JAMSData dataObject = dataObjectHash.get(attributeName);
            if (dataObject != null) {
                objectList.add(dataObject);
            } else {
                missingAttributes.add(attributeName);
            }
        }
        this.dataObjects = objectList.toArray(new JAMSData[objectList.size()]);
        return missingAttributes.toArray(new String[missingAttributes.size()]);
    }

    public JAMSData[] getDataObjects() {
        return dataObjects;
    }
    
    public void println(String str) {
        System.out.println(str);
    }
    
}