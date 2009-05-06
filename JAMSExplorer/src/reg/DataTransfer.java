/*
 * DataTransfer.java
 * Created on 5. Mai 2009, 15:48
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
package reg;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 *
 * this class represents one data column, which could be transferred
 * between subsystems, e.g. in order to append it to a table
 */
public class DataTransfer {

    /**
     * the name of the
     */
    private String name;
    /**
     * the name of the parent (e.g. shape)
     */
    private String parentName;
    /**
     * some more words except of the name
     */
    private String description;
    /**
     * the ids
     */
    private Object[] ids;
    /**
     * the data corresponding to ids
     */
    double[] data;

    public DataTransfer() {
    }

    public DataTransfer(String name, String parentName, String description, Object[] ids, double[] data) {
        this.name = name;
        this.parentName = parentName;
        this.description = description;
        this.ids = ids;
        this.data = data;
    }

    public double[] getData() {
        return data;
    }

    public void setData(double[] data) {
        this.data = data;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object[] getIds() {
        return ids;
    }

    public void setIds(Object[] ids) {
        this.ids = ids;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
}
