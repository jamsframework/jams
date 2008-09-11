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
public abstract class DataTracer {

    private JAMSData[] dataObjects;
    private ArrayList<String> attributeNames = new ArrayList<String>();
    private JAMSContext context;
    private JAMSContext[] parents;

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
        attributeNames.removeAll(missingAttributes);
        this.dataObjects = objectList.toArray(new JAMSData[objectList.size()]);

        if (this.dataObjects.length > 0) {
            createHeader();
        }

        return missingAttributes.toArray(new String[missingAttributes.size()]);
    }

    public JAMSData[] getDataObjects() {
        return dataObjects;
    }

    private void createHeader() {

        JAMSContext parent = context;
        ArrayList<JAMSContext> parentList = new ArrayList<JAMSContext>();
        while (parent != context.getModel()) {
            parent = parent.getContext();

            // only ancestors with more than one iteration are considered
            // in order to consider also contexts whose number of executions are
            // still unknown (e.g. parameter sampler), we check for !=1
            if (parent.getNumberOfIterations() != 1) {
                parentList.add(parent);
            }
        }
        this.parents = parentList.toArray(new JAMSContext[parentList.size()]);


        output("@context\n");
        output(this.context.getClass().getName() + "\t" + this.context.getInstanceName() + "\t" + context.getNumberOfIterations() + "\n");
        output("@attributes\n");
        for (String attributeName : this.attributeNames) {
            output(attributeName + "\t");
        }
        output("\n");
        output("@ancestors\n");

        for (JAMSContext p : this.parents) {
            output(p.getClass().getName() + "\t" + p.getInstanceName() + "\t" + p.getNumberOfIterations() + "\n");
        }
    }

    /**
     * This method contains code to be executed as traced JAMSData objects change
     */
    public abstract void trace();

    public void setStartMark() {
        output("@start\n");
        for (JAMSContext parent : parents) {
            output(parent.getTraceMark() + "\n");
        }
    }

    public void setEndMark() {
        output("@end\n");
    }

    protected void output(String str) {
        System.out.print(str);
    }
}