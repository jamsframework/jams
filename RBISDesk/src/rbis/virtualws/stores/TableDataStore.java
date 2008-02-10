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

import java.util.ArrayList;
import rbis.virtualws.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import rbis.virtualws.plugins.DataIO;

/**
 *
 * @author Sven Kralisch
 */
public class TableDataStore extends StandardDataStore {
    
    

    public TableDataStore(VirtualWorkspace ws, Document doc) {
        super(ws, doc);
        initDataAccess();
    }

    private void initDataAccess() {
        
        ArrayList<DataIO> dataIOs = new ArrayList<DataIO>();
                
        Element dataElement = (Element) doc.getElementsByTagName("data").item(0);
        NodeList columns = dataElement.getElementsByTagName("column");
        for (int i = 0; i < columns.getLength(); i++) {
            Element columnElement = (Element) columns.item(i);
            dataIOs.add(dataIO.get(columnElement.getAttribute("dataio")));
        }
        
    }

    public boolean hasNext() {
        return false;
    }

    public DataSet getNext() {
        return null;
    }
}
