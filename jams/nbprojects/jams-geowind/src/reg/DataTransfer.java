/*
 * DataTransfer.java
 * Created on 5. Mai 2009, 15:48
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
package reg;

import java.net.URI;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 *
 * this class represents one data column, which could be transferred
 * between subsystems, e.g. in order to append it to a table
 */
public class DataTransfer {

    /**
     * the name of the parent (e.g. shape)
     */
    private String parentName = null;

    /**
     * the the URI of the parent
     */
    private URI parentURI = null;



    /**
     * the key column of the target structure
     */
    private String targetKeyName = null;
    /**
     * the name of the columns
     */
    private String[] names;
    /**
     * some more words except of the name
     */
    private String[] descriptions;
    /**
     * the ids
     */
    private double[] ids;
    /**
     * the data corresponding to ids
     */
    double[][] data;

    public DataTransfer() {
    }

    public DataTransfer(String theTargetKey, 
            String[] theNames,
            String theParentName,
            URI theParentURI,
            String[] theDescriptions,
            double[] theIds,
            double[][] theData) {
        this.targetKeyName = theTargetKey;
        this.names = theNames;
        this.parentName = theParentName;
        this.parentURI = theParentURI;
        this.descriptions = theDescriptions;
        this.ids = theIds;
        this.data = theData;
    }

    public String getTargetKeyName() {
        return targetKeyName;
    }

    public void setTargetKeyName(String targetKeyName) {
        this.targetKeyName = targetKeyName;
    }


    public double[][] getData() {
        return data;
    }

    public void setData(double[][] data) {
        this.data = data;
    }

    public double[] getIds() {
        return ids;
    }

    public void setIds(double[] ids) {
        this.ids = ids;
    }

    public String[] getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(String[] descriptions) {
        this.descriptions = descriptions;
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }


    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public URI getParentURI() {
        return parentURI;
    }

    public void setParentURI(URI parentURI) {
        this.parentURI = parentURI;
    }


}
