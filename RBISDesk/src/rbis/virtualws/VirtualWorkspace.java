/*
 * VirtualWorkspace.java
 * Created on 23. Januar 2008, 15:42
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

package rbis.virtualws;

import java.util.HashMap;

public class VirtualWorkspace {

    private String wsTitle;
    private HashMap<String, DataStore> stores = new HashMap<String, DataStore>();

    public void addDataStore(DataStore store, String dsTitle) {
        stores.put(dsTitle, store);
    }

    public void removeDataStore(DataStore store) {
        stores.remove(store);
    }

    public DataStore getDataStore(String dsTitle) {
        return stores.get(dsTitle);
    }

    public String getTitle() {
        return wsTitle;
    }

    public void setTitle(String title) {
        this.wsTitle = title;
    }
}

