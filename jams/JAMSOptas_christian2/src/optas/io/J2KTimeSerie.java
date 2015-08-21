/*
 * TSDataReader.java
 * Created on 11. November 2005, 10:10
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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
package optas.io;

import optas.data.time.api.ComparableTimeSerie;
import optas.data.api.DataView;
import optas.data.view.AbstractListView;
import optas.data.time.DefaultComparableTimeSerie;
import optas.data.time.DefaultTimeSerie;
import optas.io.J2KTimeSerieHeader.Dimension;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class J2KTimeSerie extends DefaultTimeSerie<double[]> {

    private J2KTimeSerieHeader header;

    public J2KTimeSerie(String name, J2KTimeSerieHeader header, DataView<double[]> data) {
        super(name, header.getTemporalDomain(), data);
        this.header = header;
    }

    public int getTimeserieCount() {
        return header.getColumnCount();
    }

    public String[] getAttributeNames() {
        return header.getAttributeNames();
    }

    public void setAttributeName(int column, String attributeName) {
        header.setAttributeName(column, attributeName);
    }

    public double getMissingDataValue() {
        return header.getMissingDataValue();
    }

    public String getMissingDataString() {
        return header.getMissingDataString();
    }

    public void setMissingDataValue(double missingDataValue) {
        header.setMissingDataValue(missingDataValue);
    }

    public void setMissingDataString(String missingDataString) {
        header.setMissingDataString(missingDataString);
    }

    public String getNameOfDimension(Dimension dim) {
        return header.getNameOfDimension(dim);
    }

    public void setNameOfDimension(Dimension dim, String name) {
        header.setNameOfDimension(dim, name);
    }

    public double[] getLocation(int column) {
        return header.getLocation(column);
    }

    public void setLocation(int column, double statx, double staty) {
        header.setLocation(column, statx, staty);
    }

    public double[] getElevation() {
        return header.getElevation();
    }

    public double[] getIds() {
        return header.getIds();
    }

    public void setId(int column, double id) {
        header.setId(column, id);
    }

    public void setElevation(int column, double elevation) {
        header.setElevation(column, elevation);
    }

    public void setProjection(String proj, String nameOfXDimension, String nameOfYDimension) {
        header.setProjection(proj, nameOfXDimension, nameOfYDimension);
    }

    public String getProjection() {
        return header.getProjection();
    }

    public ComparableTimeSerie<Double> getColumn(int column) {
        String name = this.getAttributeNames()[column];
        DefaultComparableTimeSerie t = new DefaultComparableTimeSerie<>(name, this.temporalDomain,
                new AbstractListView<Double, DataView<double[]>>(this.values()) {

                    @Override
                    public int getSize() {
                        return J2KTimeSerie.this.getSize();
                    }

                    @Override
                    public Double getValue(int i) {
                        return J2KTimeSerie.this.getValue(i)[column];
                    }

                    @Override
                    public Double setValue(int i, Double value) {
                        Double oldValue = input.getValue(i)[column];
                        input.getValue(i)[column] = value;
                        return oldValue;
                    }
                }
        );
        t.setParent(this.getParent());
        return t;
    }
}
