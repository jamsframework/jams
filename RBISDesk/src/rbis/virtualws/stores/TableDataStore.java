/*
 * TableDataStore.java
 * Created on 23. Januar 2008, 15:47
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
package rbis.virtualws.stores;

import java.util.HashSet;
import java.util.Set;
import rbis.virtualws.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import rbis.virtualws.datatypes.DataValue;
import rbis.virtualws.plugins.DataIO;

/**
 *
 * @author Sven Kralisch
 */
public class TableDataStore extends StandardDataStore {

    private int currentPosition,  maxPosition;
    private Set<DataIO> dataIOSet = new HashSet<DataIO>();
    private DataIO[] dataIOArray;
    private int[] positionArray;

    public TableDataStore(VirtualWorkspace ws, Document doc) {
        super(ws, doc);
        initDataAccess();

    }

    private void initDataAccess() {

        Element dataElement = (Element) doc.getElementsByTagName("data").item(0);
        NodeList columns = dataElement.getElementsByTagName("column");

        int colCount = columns.getLength();

        dataIOArray = new DataIO[colCount];
        positionArray = new int[colCount];

        for (int i = 0; i < colCount; i++) {

            Element columnElement = (Element) columns.item(i);

            dataIOArray[i] = dataIO.get(columnElement.getAttribute("dataio"));
            dataIOSet.add(dataIOArray[i]);

            positionArray[i] = Integer.parseInt(columnElement.getAttribute("sourcecolumn"));
        }

        for (DataIO io : dataIOSet) {
            io.init();
        }

        currentPosition = 0;
        maxPosition = Integer.MAX_VALUE;

        fillBuffer();
    }

    private void fillBuffer() {

        for (DataIO io : dataIOSet) {
            io.fetchValues(bufferSize);
            maxPosition = Math.min(maxPosition, io.getValues().length);
            currentPosition = 0;
        }

    }

    public boolean hasNext() {
        if (currentPosition < maxPosition) {
            return true;
        } else {
            fillBuffer();
            if (currentPosition < maxPosition) {
                return true;
            } else {
                return false;
            }
        }
    }

    public DataSet getNext() {

        DataSet result = new DataSet(positionArray.length);

        for (int i = 0; i < positionArray.length; i++) {

            DataSet ds = dataIOArray[i].getValues()[currentPosition];
            DataValue[] values = ds.getData();
            result.setData(i, values[positionArray[i]]);

        }
        currentPosition++;

        return result;
    }
}
