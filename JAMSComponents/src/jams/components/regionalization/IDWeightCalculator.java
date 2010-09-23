/*
 * IDWWeights.java
 * Created on 21. Oktober 2008, 00:30
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
package jams.components.regionalization;

import jams.data.*;
import jams.model.*;
import jams.workspace.DataSetDefinition;
import jams.workspace.stores.InputDataStore;
import java.util.ArrayList;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(title = "IDWWeights",
author = "Peter Krause & Sven Kralisch",
date = "21/10/2008",
description = "Get stations coordinates from DataStore and calculate " +
"weights for the regionalisation procedure. Based on " +
"org.unijena.j2k.regionalization.CalcNidwWeights.")
public class IDWeightCalculator extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "entity x-coordinate")
    public JAMSDouble entityX;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "entity y-coordinate")
    public JAMSDouble entityY;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Power of IDW function")
    public JAMSDouble pidw;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "weights for IDW part of regionalisation")
    public JAMSDoubleArray statWeights;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Doug Boyle's famous function",
    defaultValue = "false")
    public JAMSBoolean equalWeights;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "ID of the datastore to read station coordinates from")
    public JAMSString dataStoreID;

    private double[] statX;

    private double[] statY;
    InputDataStore store = null;
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
            getModel().getRuntime().sendHalt("Error accessing datastore \"" +
                    dataStoreID + "\" from " + getInstanceName() + ": Datastore could not be found!");
            return;
        }

        DataSetDefinition dsDef = store.getDataSetDefinition();
        ArrayList<Object> xList = dsDef.getAttributeValues("X");
        ArrayList<Object> yList = dsDef.getAttributeValues("Y");

        if (xList.size() != yList.size()) {
            getModel().getRuntime().sendHalt("Error accessing datastore \"" +
                    dataStoreID + "\" from " + getInstanceName() + ": Number of x and y coordinates differ!");
            return;
        }

        statX = listToDoubleArray(xList);
        statY = listToDoubleArray(yList);
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

    @Override
    public void run() {

        double[] idwWeights = null;
        if (equalWeights == null || !equalWeights.getValue()) {
            idwWeights = IDW.calcIDWeights(entityX.getValue(), entityY.getValue(), statX, statY, pidw.getValue());
        } else if (equalWeights.getValue()) {
            idwWeights = IDW.equalWeights(statX.length);
        }
        statWeights.setValue(idwWeights);
    }

    @Override
    public void cleanup() {
        double[] weights = statWeights.getValue();
        for (int i = 0; i < weights.length; i++) {
            weights[i] = 0;
        }
        if (store!=null){
            try{
                store.close();
            }catch(Exception ioe){
                ioe.printStackTrace();
            }
        }
    }
}