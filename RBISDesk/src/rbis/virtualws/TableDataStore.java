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
package rbis.virtualws;

/**
 *
 * @author Sven Kralisch
 */
public class TableDataStore extends StandardDataStore {

    private String title,  description;
    private DataSetDefinition dsDef;
    private TableDataProvider provider;

    public TableDataStore() {
        this.provider = null;
    }

    public TableDataStore(TableDataProvider provider) {
        this.provider = provider;
    }

    public boolean hasNext() {
        return provider.hasNext();
    }

    public DataSet getNext() {
        return provider.getNext();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DataSetDefinition getDataSetDefinition() {
        return dsDef;
    }

    public void setDataSetDefinition(DataSetDefinition dsDef) {
        this.dsDef = dsDef;
    }
}
