/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.spatial;

import optas.io.StandardEntityReader;
import java.util.ArrayList;
import jams.data.*;
import jams.data.Attribute.Entity;
import jams.data.Attribute.Entity.NoSuchAttributeException;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Christian Fischer
 */
public class HRUReducer extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Collection of hru objects")
    public Attribute.String srcHRUFileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Collection of hru objects")
    public Attribute.String dstHRUFileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Description")
    public Attribute.Integer method;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Description")
    public Attribute.Double mergeRate;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "debugging mode",
    defaultValue = "false")
    public Attribute.Boolean debug;
    int hruCount;
    long lastHash = -1;
    private Map<Integer, Integer> groupMap = new TreeMap<Integer, Integer>();

    public void mergeHRUs(ArrayList<Attribute.Entity> srcEntities, long srcID, long dstID) {
        //System.out.println("merging HRU " + srcID + " with " + dstID);

        Attribute.Entity src = null;
        Attribute.Entity dst = null;

        for (int i = 0; i < srcEntities.size(); i++) {
            Attribute.Entity e = srcEntities.get(i);
            try {
                if ((long) e.getDouble("to_poly") == srcID) {
                    e.setDouble("to_poly", (double) dstID);
                }
            } catch (NoSuchAttributeException nsae) {
                getModel().getRuntime().sendHalt("Entity " + e.getId() + " has no to_poly attribute!");
            }
            if (getID(e) == srcID) {
                src = e;
            }
            if (getID(e) == dstID) {
                dst = e;
            }
        }
        srcEntities.remove(src);

        if (debug.getValue()) {
            int srcGroup = groupMap.get((int) srcID);
            int dstGroup = groupMap.get((int) dstID);

            for (Entry<Integer, Integer> e : groupMap.entrySet()) {
                if (e.getValue() == srcGroup) {
                    e.setValue(dstGroup);
                }
            }
        }

        try {
            double src_x = src.getDouble("x");
            double dst_x = dst.getDouble("x");

            double src_y = src.getDouble("y");
            double dst_y = dst.getDouble("y");

            double src_area = src.getDouble("area");
            double dst_area = dst.getDouble("area");

            double src_slope = src.getDouble("slope");
            double dst_slope = dst.getDouble("slope");

            double src_flowLength = src.getDouble("flowlength");
            double dst_flowLength = dst.getDouble("flowlength");

            double src_flowAmount = src.getDouble("flowAmount");
            double dst_flowAmount = dst.getDouble("flowAmount");

            double src_weight = src_area / (src_area + dst_area);
            double dst_weight = dst_area / (src_area + dst_area);

            double result_x = src_weight * src_x + dst_weight * dst_x;
            double result_y = src_weight * src_y + dst_weight * dst_y;

            double result_slope = src_weight * src_slope + dst_weight * dst_slope;
            double result_flowLength = src_weight * src_flowLength + dst_weight * dst_flowLength;
            double result_flowAmount = src_flowAmount + dst_flowAmount;

            dst.setDouble("area", src_area + dst_area);
            dst.setDouble("x", result_x);
            dst.setDouble("y", result_y);
            dst.setDouble("slope", result_slope);
            dst.setDouble("flowlength", result_flowLength);
            dst.setDouble("flowAmount", result_flowAmount);

        } catch (NoSuchAttributeException nsae) {
            nsae.printStackTrace();
        }
    }

    public long getToPoly(Attribute.Entity e) {
        try {
            return (long) e.getDouble("to_poly");
        } catch (NoSuchAttributeException nsae) {
            getModel().getRuntime().sendHalt("Entity " + e.getId() + " has no to_poly attribute!");
        }
        return 0;
    }

    public long getID(Attribute.Entity e) {
        try {
            return (long) e.getDouble("ID");
        } catch (NoSuchAttributeException nsae) {
            getModel().getRuntime().sendHalt("Entity " + e.getId() + " has no ID attribute!");
        }
        return 0;
    }

    private int method1(ArrayList<Attribute.Entity> srcEntities) {
        int minIndex = 0;
        double minArea = Double.POSITIVE_INFINITY;

        for (int i = 0; i < srcEntities.size(); i++) {
            try {
                double area = srcEntities.get(i).getDouble("area");
                if (area < minArea && getToPoly(srcEntities.get(i)) != 0) {
                    minIndex = i;
                    minArea = area;
                }
            } catch (NoSuchAttributeException nsae) {
                getModel().getRuntime().sendHalt("Entity " + srcEntities.get(i).getId() + " has no area attribute!");
            }
        }
        return minIndex;
    }

    private class Weighting {

        String attribute;
        double weight;
        boolean isNumeric;

        public Weighting(String attribute, double weight, boolean isNumeric) {
            this.attribute = attribute;
            this.weight = weight;
            this.isNumeric = isNumeric;
        }
    }

    private int method2(ArrayList<Attribute.Entity> srcEntities) {
        int minIndex = 0;
        double minDistance = Double.POSITIVE_INFINITY;

        Set<Weighting> weightSet = new HashSet<Weighting>();
        weightSet.add(new Weighting("elevation", 0.01, true));
        weightSet.add(new Weighting("slope", 1.00, true));
        weightSet.add(new Weighting("aspect", 0.01, true));
        weightSet.add(new Weighting("soilID", 5.0, false));
        weightSet.add(new Weighting("landuseID", 3.5, false));
        weightSet.add(new Weighting("hgeoID", 1.0, false));

        HashMap<Integer, Attribute.Entity> map = new HashMap<Integer, Attribute.Entity>();
        for (int i = 0; i < srcEntities.size(); i++) {
            map.put((int) getID(srcEntities.get(i)), srcEntities.get(i));
        }

        for (int i = 0; i < srcEntities.size(); i++) {
            try {
                Entity e = srcEntities.get(i);
                int upstreamId = (int) getToPoly(e);
                if (upstreamId == 0) {
                    continue;
                }
                Entity u = map.get(upstreamId);

                double distance = 0;
                for (Weighting w : weightSet) {
                    double value1 = e.getDouble(w.attribute);
                    double value2 = u.getDouble(w.attribute);

                    if (w.isNumeric) {
                        distance += Math.abs(value1 - value2) * w.weight;
                    } else {
                        distance += (value1 != value2) ? w.weight : 0;
                    }

                    if (distance < minDistance) {
                        minDistance = distance;
                        minIndex = i;
                    }
                }
            } catch (NoSuchAttributeException nsae) {
                getModel().getRuntime().sendHalt("Entity " + srcEntities.get(i).getId() + " has no area attribute!");
            }
        }
        return minIndex;
    }

    public int method3(ArrayList<Attribute.Entity> srcEntities) {
        int minIndex = 0;
        double minFlow = Double.POSITIVE_INFINITY;

        for (int i = 0; i < srcEntities.size(); i++) {
            try {
                double flow = srcEntities.get(i).getDouble("flowAmount");
                if (flow < minFlow && getToPoly(srcEntities.get(i)) != 0) {
                    minIndex = i;
                    minFlow = flow;
                }
            } catch (NoSuchAttributeException nsae) {
                getModel().getRuntime().sendHalt("Entity " + srcEntities.get(i).getId() + " has no area attribute!");
            }
        }
        return minIndex;
    }

    private int findNextCandidate(ArrayList<Attribute.Entity> srcEntities) {
        switch (this.method.getValue()) {
            case 1:
                return method1(srcEntities);
            case 2:
                return method2(srcEntities);
            case 3:
                return method3(srcEntities);
            default:
                return -1;
        }
    }

    public void createDstHRUFile(File srcHRUFile, File dstHRUFile, double beta) {
        //ArrayList<Attribute.Entity> srcEntities = StandardEntityReader.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), srcHRUFileName.getValue()), getModel());
        ArrayList<Attribute.Entity> srcEntities = StandardEntityReader.readParas(srcHRUFile.getAbsolutePath(), getModel());
        if (debug.getValue()) {
            initGroupMap(srcEntities);
        }
        //count unmergable HRUs
        int unmergableHRUs = 0;
        for (int i = 0; i < srcEntities.size(); i++) {
            long to_poly = getToPoly(srcEntities.get(i));
            if (to_poly == 0) {
                unmergableHRUs++;
            }
        }
        int mergeHRUs = (int) ((srcEntities.size() - unmergableHRUs) * beta);

        hruCount = srcEntities.size();
        getModel().getRuntime().println("Total number of HRUs is: " + srcEntities.size());
        getModel().getRuntime().println("Unmergeable number of HRUs is: " + unmergableHRUs);
        getModel().getRuntime().println("Number of HRUs to merge: " + mergeHRUs);
        getModel().getRuntime().println("Using Method 1 (Eliminate HRU with smallest area!)");

        //method 1 eliminate smallest hrus
        while (mergeHRUs > 0) {
            int index = findNextCandidate(srcEntities);

            long src_id = getID(srcEntities.get(index));
            long dst_id = getToPoly(srcEntities.get(index));
            mergeHRUs(srcEntities, src_id, dst_id);

            mergeHRUs--;
            hruCount--;
        }
        StandardEntityReader.writeParas(srcEntities, dstHRUFile.getAbsolutePath(), getModel());
    }

    private void initGroupMap(ArrayList<Attribute.Entity> srcEntities) {
        for (int i = 0; i < srcEntities.size(); i++) {
            long id = getID(srcEntities.get(i));
            groupMap.put((int) id, (int) id);
        }
    }

    @Override
    public void init() {
        long hashCode = this.dstHRUFileName.getValue().hashCode()
                + this.srcHRUFileName.getValue().hashCode()
                + Double.toString(mergeRate.getValue()).hashCode()
                + Integer.toString(method.getValue()).hashCode();
        if (lastHash == hashCode) {
            System.out.println("Skip HRU Reducer, because data is identical to last run.");
            return;
        }

        lastHash = hashCode;

        getModel().getRuntime().println("###########################################");
        getModel().getRuntime().println("Creating reduces HRU File:");
        getModel().getRuntime().println("Source HRU: " + this.srcHRUFileName.getValue());
        getModel().getRuntime().println("Modified HRU: " + this.dstHRUFileName.getValue());
        getModel().getRuntime().println("Merge-rate: " + this.mergeRate);

        createDstHRUFile(new File(this.getModel().getWorkspacePath() + "/" + srcHRUFileName.getValue()),
                new File(this.getModel().getWorkspacePath() + "/" + dstHRUFileName.getValue()), this.mergeRate.getValue());

        getModel().getRuntime().println("Merge stopped with: " + this.hruCount + "HRUs");
        getModel().getRuntime().println("###########################################");

        //print ids and their assoziated groups
        if (debug.getValue()) {
            String nfoFileName = this.dstHRUFileName.getValue().replaceAll(".par", ".nfo");
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(this.getModel().getWorkspacePath() + "/" + nfoFileName)));
                writer.write("HRU ID\tGROUP\n");
                for (Entry<Integer, Integer> e : groupMap.entrySet()) {
                    writer.write(e.getKey() + "\u0009" + e.getValue()+"\n");
                }
                writer.flush();
                writer.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        return;
    }

    public void cleanup() {
        return;
    }

    public static void main(String[] args) {
        HRUReducer reducer = new HRUReducer();
        reducer.dstHRUFileName = DefaultDataFactory.getDataFactory().createString();
        reducer.dstHRUFileName.setValue("C:/Arbeit/test.par");
        reducer.srcHRUFileName = DefaultDataFactory.getDataFactory().createString();
        reducer.srcHRUFileName.setValue("C:/Arbeit/ModelData/JAMS-Gehlberg/parameter/hrus_hor_dist.par");
        reducer.method = DefaultDataFactory.getDataFactory().createInteger();

        reducer.createDstHRUFile(new File("C:/Arbeit/ModelData/JAMS-Gehlberg/parameter/hrus_hor_dist.par"), new File("C:/Arbeit/test.par"), 0.50);
    }
}
