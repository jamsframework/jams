/*
 * IDWWeightCalculator.java
 * Created on 7. December 2008, 19:45
 *
 * This file is a JAMS component
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
package org.unijena.j2k.regionalisation;

import jams.data.*;
import jams.model.*;
import jams.workspace.DataSetDefinition;
import jams.workspace.stores.InputDataStore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.unijena.j2k.statistics.IDW;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(title = "IDWWeightCalculator",
        author = "Peter Krause and Sven Kralisch",
        date = "2008-12-07",
        version = "1.0_0",
        description = "Get stations coordinates from DataStore and calculate "
        + "inverse distance weights for the regionalisation procedure. Based on "
        + "org.unijena.j2k.regionalization.CalcIDWeights.")
public class IDWWeightCalculator extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "entity x-coordinate")
    public Attribute.Double entityX;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "entity y-coordinate")
    public Attribute.Double entityY;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Power of IDW function")
    public Attribute.Double pidw;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "weights for IDW part of regionalisation")
    public Attribute.DoubleArray statWeights;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "position array to determine best weights")
    public Attribute.IntegerArray statOrder;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Weights for Thiessen polygons",
            defaultValue = "false")
    public Attribute.Boolean equalWeights;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Calculation with geographical coordinates lat, long",
            defaultValue = "false")
    public Attribute.Boolean latLong;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "ID of the datastore to read station coordinates from")
    public Attribute.String dataStoreID;
    private double[] statX;
    private double[] statY;
    InputDataStore store = null;
    IDW idw = new IDW();
    /*
     *  Component run stages
     */

    @Override
    public void init() {
        if (dataStoreID != null) {
            store = getModel().getWorkspace().getInputDataStore(dataStoreID.getValue());
        }

        // check if store exists
        if (store == null) {
            getModel().getRuntime().sendHalt("Error accessing datastore \""
                    + dataStoreID + "\" from " + getInstanceName() + ": Datastore could not be found!");
            return;
        }

        DataSetDefinition dsDef = store.getDataSetDefinition();
        ArrayList<Object> xList = dsDef.getAttributeValues("X");
        ArrayList<Object> yList = dsDef.getAttributeValues("Y");

        if (xList == null || yList == null) {
            getModel().getRuntime().sendHalt("Error accessing datastore \""
                    + dataStoreID + "\" from " + getInstanceName() + ": x or y coordinates are not set!");
        }

        if (xList.size() != yList.size()) {
            getModel().getRuntime().sendHalt("Error accessing datastore \""
                    + dataStoreID + "\" from " + getInstanceName() + ": Number of x and y coordinates differ!");
            return;
        }

        statX = listToDoubleArray(xList);
        statY = listToDoubleArray(yList);

        boolean isLatLon = latLong != null && latLong.getValue();
        if (isLatLon) {
            idw.init(statX, statY, null, (int) pidw.getValue(), IDW.Projection.LATLON);
        } else {
            idw.init(statX, statY, null, (int) pidw.getValue(), IDW.Projection.ANY);
        }
    }

    @Override
    public void run() {
        
        double weights[] = statWeights.getValue();
        int wA[] = statOrder.getValue();

        int n = statX.length;

        if (weights == null || weights.length != n) {
            weights = new double[n];
        }
        if (wA == null || wA.length != n) {
            wA = new int[n];
        }

        if (equalWeights == null || !equalWeights.getValue()) {
            idw.getIDW(entityX.getValue(), entityY.getValue(), null, 1);

            System.arraycopy(idw.getWeights(), 0, weights, 0, n);
            System.arraycopy(idw.getWeightOrder(), 0, wA, 0, n);
            
            
            
        } else if (equalWeights.getValue()) {
            for (int i = 0; i < n; i++) {
                weights[i] = 1. / (double) n;
                wA[i] = i;
            }
        }
        statWeights.setValue(weights);
        statOrder.setValue(wA);
    }

    public void cleanup() {
        Arrays.fill(statWeights.getValue(), 0);
        if (store != null) {
            try {
                store.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private double[] listToDoubleArray(ArrayList<Object> list) {
        double[] result = new double[list.size()];
        int i = 0;
        for (Object o : list) {
            result[i] = ((Double) o).doubleValue();
            i++;
        }
        return result;
    }
}
