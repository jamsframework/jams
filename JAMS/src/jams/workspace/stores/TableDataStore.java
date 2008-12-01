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
package jams.workspace.stores;

import jams.runtime.JAMSRuntime;
import java.util.HashSet;
import java.util.Set;
import jams.workspace.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import jams.workspace.datatypes.DataValue;
import jams.workspace.DataReader;

/**
 *
 * @author Sven Kralisch
 */
public class TableDataStore extends StandardInputDataStore {

    protected int currentPosition,  maxPosition;
    protected Set<DataReader> dataIOSet = new HashSet<DataReader>();
    protected DataReader[] dataIOArray;
    protected int[] positionArray;

    public TableDataStore(VirtualWorkspace ws) {
        super(ws);
    }
    
    public TableDataStore(VirtualWorkspace ws, String id, Document doc) throws ClassNotFoundException {
        super(ws, id, doc);

        if (ws.getRuntime().getRunState() != JAMSRuntime.RUNSTATE_RUN) {
            return;
        }

        initDataAccess(doc);
    }

    private void initDataAccess(Document doc) {

        Element dataElement = (Element) doc.getElementsByTagName("data").item(0);
        NodeList columns = dataElement.getElementsByTagName("column");

        int colCount = columns.getLength();

        dataIOArray = new DataReader[colCount];
        positionArray = new int[colCount];

        for (int i = 0; i < colCount; i++) {

            Element columnElement = (Element) columns.item(i);

            dataIOArray[i] = dataIO.get(columnElement.getAttribute("dataio"));
            dataIOSet.add(dataIOArray[i]);

            positionArray[i] = Integer.parseInt(columnElement.getAttribute("sourcecolumn"));
        }

        for (DataReader io : dataIOSet) {
            io.init();
        }

        currentPosition = Integer.MAX_VALUE;
        maxPosition = Integer.MAX_VALUE;

    }

    protected void fillBuffer() {

        for (DataReader io : dataIOSet) {

            if (bufferSize > 0) {
                io.fetchValues(bufferSize);
            } else {
                io.fetchValues();
            }
            maxPosition = Math.min(maxPosition, io.getData().length);
            currentPosition = 0;
        }
    }

    @Override
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

    @Override
    public DataSet getNext() {

        DataSet result = new DataSet(positionArray.length);

        for (int i = 0; i < dataIOArray.length; i++) {

            DataSet ds = dataIOArray[i].getData()[currentPosition];
            DataValue[] values = ds.getData();
            result.setData(i, values[positionArray[i]]);

        }
        currentPosition++;

        return result;
    }

    @Override
    public void close() {
        for (DataReader io : dataIOSet) {
            io.cleanup();
        }
    }
}
