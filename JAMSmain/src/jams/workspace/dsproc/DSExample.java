/*
 * DSExample.java
 * Created on 24. Februar 2009, 21:59
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
package jams.workspace.dsproc;

import jams.data.Attribute;
import jams.data.JAMSCalendar;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class DSExample {

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException, URISyntaxException {

        DataStoreProcessor dsdb = new DataStoreProcessor(new File("/Users/bigr/Documents/BA-Arbeit/trunk/JAMSworldwind/shapefiles/J2000_Dudh-Kosi/output/20131220_142111/HRULoop.dat"));
        
        dsdb.addImportProgressObserver(new Observer() {
            public void update(Observable o, Object arg) {
                System.out.print(arg + " ");
            }
        });
        
        dsdb.createDB();
        TimeSpaceProcessor tsproc = new TimeSpaceProcessor(dsdb);
        System.out.println("\nDatastore loaded");
        
        tsproc.addProcessingProgressObserver(new Observer() {
            public void update(Observable o, Object arg) {
                System.out.print(arg + " ");
            }
        });

        Long[] ids = tsproc.getEntityIDs();
        JAMSCalendar[] dates = tsproc.getTimeSteps();
        
        int count = 0;
        for (AbstractDataStoreProcessor.AttributeData attribute : dsdb.getAttributes()) {
            if (attribute.getName().equals("precip") || attribute.getName().equals("tmean")) {
                attribute.setSelected(true);
                count++;
            }
        }
        
        System.out.println(count);
        
        int i = 0;
        long[] entityIds = new long[ids.length];
        for (Long id : ids) {
            entityIds[i++] = id.longValue();
        }
        
        i = 0;
        String[] dateIds = new String[dates.length];
        for (JAMSCalendar date : dates) {
            dateIds[i++] = date.toString();
        }        
        
        DataMatrix crossProduct = tsproc.getCrossProduct(entityIds, dateIds);
        
                 
        System.out.println("\nHRU IDs:");
        for (String id : crossProduct.getAttributeIDs()) {
            System.out.print(id + " ");
        }
        
        System.out.println("\nDates:");
        for (Object id : crossProduct.getIds()) {
            System.out.print(id + " ");
        }        

        System.out.println("\nValues for 1997-09-16 00:00");
        int rowID = crossProduct.getIDPosition("1997-09-16 00:00");
        double[] rowData = crossProduct.getRow(rowID);
        for (double d : rowData) {
            System.out.print(d + " ");
        }
        
        // access full array
        double[][] data = crossProduct.getArray();
    }
}
