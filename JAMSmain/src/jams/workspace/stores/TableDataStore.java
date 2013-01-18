/*
 * TableDataStore.java
 * Created on 23. Januar 2008, 15:47
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package jams.workspace.stores;

import jams.runtime.JAMSRuntime;
import java.util.HashSet;
import java.util.Set;
import jams.workspace.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import jams.workspace.DataReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author Sven Kralisch
 */
public class TableDataStore extends StandardInputDataStore {

    /**
     * The current position of dataset within the buffer
     */
    /**
     * The maximal position within the buffer
     */
    protected int[] currentPosition, maxPosition;

    /**
     * A set of DataReader objects that this datastore uses to read data
     */
    protected Set<DataReader> dataIOSet = new HashSet<DataReader>();

    /**
     * An array of DataReader objects that this datastore uses to read data
     */
    protected DataReader[] dataIOArray;

    /**
     * An array of integers defining which column of the DataReader contains
     * the data
     */
    protected int[] positionArray;

    /**
     * Creates a new TableDataStore object
     * @param ws The workspace that the datastore belongs to
     */
    public TableDataStore(JAMSWorkspace ws) {
        super(ws);
    }

    /**
     * Creates a new TableDataStore object
     * @param ws The workspace that the datastore belongs to
     * @param id A datastore identifier
     * @param doc A XML document that describes this datastore
     * @throws java.lang.ClassNotFoundException
     */
    public TableDataStore(JAMSWorkspace ws, String id, Document doc) throws ClassNotFoundException {
        super(ws, id, doc);

        if (ws.getRuntime().getState() == JAMSRuntime.STATE_STOP) {
            return;
        }

        if (!readCache()) {
            initDataAccess(doc);
        }
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
        
        currentPosition = new int[dataIOArray.length];
        maxPosition = new int[dataIOArray.length];
        
        for (int i = 0; i < dataIOArray.length; i++) {
            dataIOArray[i].init();
            currentPosition[i] = Integer.MAX_VALUE;
            maxPosition[i] = Integer.MAX_VALUE;
        }


    }

    /**
     * Fills the buffer according to the buffer size
     */
    protected void fillBuffer(int i) {
        DataReader io = dataIOArray[i];

        if (bufferSize > 0) {
            io.fetchValues(bufferSize);
        } else {
            io.fetchValues();
        }
        maxPosition[i] = Math.min(maxPosition[i], io.getData().length);
        currentPosition[i] = 0;
    }

    /**
     * Checks if there is another dataset in the datastore
     * @return True, if there is another dataset, false if not
     */
    @Override
    public boolean hasNext() {
        
        for (int i = 0; i < dataIOArray.length; i++) {      
            
            if (currentPosition[i] >= maxPosition[i]) {
                fillBuffer(i);
                if (currentPosition[i] >= maxPosition[i]) {
                    return false;
                }
            }
            
        }
        return true;
    }
    
    /**
     * Gets the next dataset from the datastore
     * @return A dataset object
     */
    @Override
    public DefaultDataSet getNext() {

        DefaultDataSet result = new DefaultDataSet(positionArray.length);

        for (int i = 0; i < dataIOArray.length; i++) {

            DataSet ds = dataIOArray[i].getData()[currentPosition[i]];
            DataValue[] values = ds.getData();
            result.setData(i, values[positionArray[i]]);
            currentPosition[i]++;       

        }
        return result;
    }

    /**
     * Closes the datastore by closing all its belonging data readers
     */
    @Override
    public void close() {
        for (DataReader io : dataIOSet) {
            io.cleanup();
        }
    }

    public void setWorkspace(Workspace ws) throws IOException{
        super.ws = ws;

        for (DataReader io : dataIOSet) {
            io.setWorkspace(ws);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException, ClassNotFoundException{
        out.defaultWriteObject();
    }
    private void writeObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        in.defaultReadObject();
    }
}
