/*
 * MageOutput.java
 * Created on 06.09.2013, 11:32:40
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package magelink;

import jams.data.*;
import jams.model.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "MageOutput",
        author = "Sven Kralisch & Marko Adamovic",
        description = "Create Mage input files for reach entities",
        date = "2013-09-06",
        version = "1.0_0")
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class MageOutput extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    public Attribute.String lateralOutputFileName;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    public Attribute.String nodeOutputFileName;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    public Attribute.Calendar time;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    public Attribute.TimeInterval modelTimeInterval;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    public Attribute.String mageID;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    public Attribute.Double reachID;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    public Attribute.String mageReachName;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    public Attribute.Double channelStart;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    public Attribute.Double channelEnd;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Description")
    public Attribute.Double[] reachInFlow;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description")
    public Attribute.Double[] reachOutFlow;
    /*
     *  Component run stages
     */
    Map<Double, List> reachInFlows = new HashMap();
    Map<Double, List> nodeOutputs = new HashMap();
    Map<Double, String> midMap = new HashMap();
    Map<Double, double[]> startEndMap = new HashMap();
    boolean doLatOutput = false, doNodeOutput = false;

    @Override
    public void init() {
        if (lateralOutputFileName != null && !lateralOutputFileName.getValue().isEmpty()) {
            doLatOutput = true;
        }
        if (nodeOutputFileName != null && !nodeOutputFileName.getValue().isEmpty()) {
            doNodeOutput = true;
        }
    }

    @Override
    public void run() {

        if (doLatOutput && channelStart.getValue() != -1) {
            List<double[]> ts = reachInFlows.get(reachID.getValue());
            if (ts == null) {
                ts = new ArrayList();
                reachInFlows.put(new Double(reachID.getValue()), ts);
                double[] startEnd = new double[2];
                startEnd[0] = channelStart.getValue();
                startEnd[1] = channelEnd.getValue();
                startEndMap.put(new Double(reachID.getValue()), startEnd);
            }

            double latFlow = 0;
            for (Attribute.Double inflowComponent : reachInFlow) {
                latFlow = latFlow + inflowComponent.getValue();
            }

            long currentTimeInMillis = time.getTimeInMillis();
            long startTimeInMillis = modelTimeInterval.getStart().getTimeInMillis();
            long modelTimeInMinutes = (currentTimeInMillis - startTimeInMillis) / 60000;

            double[] result = new double[2];
            result[0] = modelTimeInMinutes;
            result[1] = latFlow;

            ts.add(result);
        }

        if (doNodeOutput && !mageID.getValue().equals("-")) {
            List<double[]> ts = nodeOutputs.get(reachID.getValue());
            if (ts == null) {
                ts = new ArrayList();
                nodeOutputs.put(new Double(reachID.getValue()), ts);
                midMap.put(new Double(reachID.getValue()), mageID.getValue().toString());
            }
            
            double outFlow = 0;
            for (Attribute.Double outflowComponent : reachOutFlow) {
                outFlow = outFlow + outflowComponent.getValue();
            }            

            long currentTimeInMillis = time.getTimeInMillis();
            long startTimeInMillis = modelTimeInterval.getStart().getTimeInMillis();
            long modelTimeInMinutes = (currentTimeInMillis - startTimeInMillis) / 60000;

            double[] result = new double[2];
            result[0] = modelTimeInMinutes;
            result[1] = outFlow;

            ts.add(result);
        }
    }

    @Override
    public void cleanup() {

        if (doLatOutput) {
            try {
                File f = new File(lateralOutputFileName.getValue());
                if (!f.isAbsolute()) {
                    f = new File(getModel().getWorkspaceDirectory(), f.getPath());
                }
                BufferedWriter writer = new BufferedWriter(new FileWriter(f));

                List<Double> reachList = new ArrayList();
                reachList.addAll(reachInFlows.keySet());

                // sort the array list according to the reach ID
                Collections.sort(reachList, new Comparator<Double>() {
                    @Override
                    public int compare(Double o1, Double o2) {
                        if (o1 < o2) {
                            return -1;
                        } else if (o1 == o2) {
                            return 0;
                        } else {
                            return 1;
                        }
                    }
                });

                for (Double reachID : reachList) {
                    double[] startEnd = startEndMap.get(reachID);
                    writer.write(String.format(Locale.US, "$%10.0f%10.3f%10.3f", reachID, startEnd[0], startEnd[1]));
                    writer.newLine();

                    List<double[]> ts = reachInFlows.get(reachID);
                    for (double[] result : ts) {
                        writer.write(String.format(Locale.US, "%10.0f%10.6f", result[0], result[1]));
                        writer.newLine();
                    }
                }

                writer.close();
            } catch (IOException ex) {
                getModel().getRuntime().println("Error while writing to " + lateralOutputFileName.getValue() + ": " + ex.getMessage());
            }
        }
        if (doNodeOutput) {
            try {

                File f = new File(nodeOutputFileName.getValue());
                if (!f.isAbsolute()) {
                    f = new File(getModel().getWorkspaceDirectory(), f.getPath());
                }
                BufferedWriter writer = new BufferedWriter(new FileWriter(f));

                List<Double> reachList = new ArrayList();
                reachList.addAll(nodeOutputs.keySet());

                // sort the array list according to the reach ID
                Collections.sort(reachList, new Comparator<Double>() {
                    @Override
                    public int compare(Double o1, Double o2) {
                        if (o1 < o2) {
                            return -1;
                        } else if (o1 == o2) {
                            return 0;
                        } else {
                            return 1;
                        }
                    }
                });

                for (Double reachID : reachList) {
                    double[] startEnd = startEndMap.get(reachID);
                    writer.write("$" + midMap.get(reachID));
                    writer.newLine();

                    List<double[]> ts = nodeOutputs.get(reachID);
                    for (double[] result : ts) {
                        writer.write(String.format(Locale.US, "%10.0f%10.6f", result[0], result[1]));
                        writer.newLine();
                    }
                }
                
                writer.close();
            } catch (IOException ex) {
                getModel().getRuntime().println("Error while writing to " + nodeOutputFileName.getValue() + ": " + ex.getMessage());
            }
        }
    }
}