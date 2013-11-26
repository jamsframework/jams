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
import java.util.Arrays;
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
public class HRUReachReducer extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Collection of hru objects")
    public Attribute.String srcHRUFileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Collection of hru objects")
    public Attribute.String srcReachFileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Collection of hru objects")
    public Attribute.String dstHRUFileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Collection of hru objects")
    public Attribute.String dstReachFileName;
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

    private Map<Integer, ArrayList<Entity>> reach2HRUMap = new TreeMap<Integer, ArrayList<Entity>>();

    private int getDownstreamReach(ArrayList<Entity> srcReaches, int reachID){
        Entity reach = null;
        for (int i = 0; i < srcReaches.size(); i++) {
            Entity e = srcReaches.get(i);
            long id = getID(e);
            if (id == reachID) {
                reach = e;
                break;
            }
        }
        if (reach == null) {
            halt("No Reach to id " + reachID);
        }
        return (int)getToReach(reach);
    }

    private int getDownstreamHRU(ArrayList<Entity> srcEntities, ArrayList<Entity> srcReaches, Attribute.Entity e) {
        long dst_id = getToPoly(e);
        long src_id = getID(e);

        if (dst_id == 0) {
            long reach_id = getToReach(e);

            int ownerReach = (int)reach_id;
            while (dst_id == 0) {
                ArrayList<Entity> neighbourHRUs = getHRUsForReach(srcEntities, reach_id);
                if (neighbourHRUs==null){
                    System.out.println("error");
                }
                if ( (reach_id == ownerReach && neighbourHRUs.size() <= 1) || neighbourHRUs.isEmpty()) {
                    reach_id = getDownstreamReach(srcReaches, (int)reach_id);
                    if (reach_id == 0)
                        return 0;
                } else {
                    dst_id = getID(neighbourHRUs.get(0));
                    if (dst_id == src_id) {
                        dst_id = getID(neighbourHRUs.get(1));
                    }
                }
            }
        }
        return (int) dst_id;
    }

    public void mergeHRUs(ArrayList<Attribute.Entity> srcEntities, ArrayList<Attribute.Entity> srcReaches, long srcID, long dstID) {
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
                halt("Entity " + e.getId() + " has no to_poly attribute!");
            }
            if (getID(e) == srcID) {
                src = e;
            }
            if (getID(e) == dstID) {
                dst = e;
            }
        }

        if (dst == null) {
            System.out.println("cannot find hru with id:" + dstID + " during merge with:" + srcID);            
        }

        if (debug != null && debug.getValue()) {
            int srcGroup = groupMap.get((int) srcID);
            int dstGroup = groupMap.get((int) dstID);

            for (Entry<Integer, Integer> e : groupMap.entrySet()) {
                if (e.getValue() == srcGroup) {
                    e.setValue(dstGroup);
                }
            }
        }

        long reachIdSrc = getToReach(src);
        if (reachIdSrc != 0) {
            long reachIdDst = getToReach(dst);
            while (reachIdDst != 0 && reachIdSrc != reachIdDst ){
                mergeReaches(srcReaches, (int)reachIdSrc);
                reachIdSrc = getToReach(src);
                reachIdDst = getToReach(dst);
            }
            reach2HRUMap.get((int) reachIdSrc).remove(src);
            //    System.out.println("error reach2hru map does not contain " + srcID);
        }

        srcEntities.remove(src);

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
            
            double src_weight = src_area / (src_area + dst_area);
            double dst_weight = dst_area / (src_area + dst_area);

            double result_x = src_weight * src_x + dst_weight * dst_x;
            double result_y = src_weight * src_y + dst_weight * dst_y;

            double result_slope = src_weight * src_slope + dst_weight * dst_slope;
            double result_flowLength = src_weight * src_flowLength + dst_weight * dst_flowLength;
            

            dst.setDouble("area", src_area + dst_area);
            dst.setDouble("x", result_x);
            dst.setDouble("y", result_y);
            dst.setDouble("slope", result_slope);
            dst.setDouble("flowlength", result_flowLength);            
        } catch (NoSuchAttributeException nsae) {
            nsae.printStackTrace();
        }

        if (method.getValue() == 3) {
            try {
                double src_flowAmount = src.getDouble("flowAmount");
                double dst_flowAmount = dst.getDouble("flowAmount");

                double result_flowAmount = src_flowAmount + dst_flowAmount;

                dst.setDouble("flowAmount", result_flowAmount);
            } catch (NoSuchAttributeException nsae) {
                nsae.printStackTrace();
            }
        }
    }

    public long getToPoly(Attribute.Entity e) {
        try {
            return (long) e.getDouble("to_poly");
        } catch (NoSuchAttributeException nsae) {
            halt("Entity " + e.getId() + " has no to_poly attribute!");
        }
        return 0;
    }

    public long getToReach(Attribute.Entity e) {
        try {
            return (long) e.getDouble("to_reach");
        } catch (NoSuchAttributeException nsae) {
            halt("Entity " + e.getId() + " has no to_poly attribute!");
        }
        return 0;
    }

    public long getID(Attribute.Entity e) {
        try {
            return (long) e.getDouble("ID");
        } catch (NoSuchAttributeException nsae) {
            halt("Entity " + e.getId() + " has no ID attribute!");
        }
        return 0;
    }

    private int method1(ArrayList<Attribute.Entity> srcEntities,ArrayList<Attribute.Entity> srcReaches) {
        int minIndex = 0;
        double minArea = Double.POSITIVE_INFINITY;

        for (int i = 0; i < srcEntities.size(); i++) {
            try {
                Entity e = srcEntities.get(i);
                double area = srcEntities.get(i).getDouble("area");
                if (getDownstreamHRU(srcEntities, srcReaches, e)==0)
                    continue;
                if (area < minArea) {
                    minIndex = i;
                    minArea = area;
                }
            } catch (NoSuchAttributeException nsae) {
                halt("Entity " + srcEntities.get(i).getId() + " has no area attribute!");
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

    private int method2(ArrayList<Attribute.Entity> srcEntities, ArrayList<Entity> srcReaches) {
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

                int downstreamId = (int) getDownstreamHRU(srcEntities, srcReaches, e);
                if (downstreamId == 0) {
                    continue;
                }
                Entity u = map.get(downstreamId);

                double distance = 0;
                for (Weighting w : weightSet) {
                    double value1 = e.getDouble(w.attribute);
                    double value2 = u.getDouble(w.attribute);

                    if (w.isNumeric) {
                        distance += Math.abs(value1 - value2) * w.weight;
                    } else {
                        distance += (value1 != value2) ? w.weight : 0;
                    }
                }
                if (distance < minDistance) {
                    minDistance = distance;
                    minIndex = i;
                }
            } catch (NoSuchAttributeException nsae) {
                halt("Entity " + srcEntities.get(i).getId() + " has no area attribute!");
            }
        }
        return minIndex;
    }

    public int method3(ArrayList<Attribute.Entity> srcEntities,ArrayList<Attribute.Entity> srcReaches) {
        int minIndex = 0;
        double minFlow = Double.POSITIVE_INFINITY;

        for (int i = 0; i < srcEntities.size(); i++) {
            try {
                Entity e = srcEntities.get(i);
                double flow = e.getDouble("flowAmount");
                if (getDownstreamHRU(srcEntities, srcReaches, e)==0){
                    continue;
                }
                if (flow < minFlow) {
                    minIndex = i;
                    minFlow = flow;
                }
            } catch (NoSuchAttributeException nsae) {
                halt("Entity " + srcEntities.get(i).getId() + " has no area attribute!");
            }
        }
        return minIndex;
    }

    private int method4(ArrayList<Attribute.Entity> srcEntities, ArrayList<Entity> srcReaches) {
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

                int downstreamId = (int) getDownstreamHRU(srcEntities, srcReaches, e);
                if (downstreamId == 0) {
                    continue;
                }
                Entity u = map.get(downstreamId);

                double distance = 0;
                for (Weighting w : weightSet) {
                    double value1 = e.getDouble(w.attribute);
                    double value2 = u.getDouble(w.attribute);

                    if (w.isNumeric) {
                        distance += Math.abs(value1 - value2) * w.weight;
                    } else {
                        distance += (value1 != value2) ? w.weight : 0;
                    }
                }
                distance *= e.getDouble("area");

                if (distance < minDistance) {
                    minDistance = distance;
                    minIndex = i;
                }
            } catch (NoSuchAttributeException nsae) {
                halt("Entity " + srcEntities.get(i).getId() + " has no area attribute!");
            }
        }
        return minIndex;
    }

    private int findNextCandidate(ArrayList<Attribute.Entity> srcEntities, ArrayList<Entity> srcReaches) {
        switch (this.method.getValue()) {
            case 1:
                return method1(srcEntities, srcReaches);
            case 2:
                return method2(srcEntities, srcReaches);
            case 3:
                return method3(srcEntities, srcReaches);
            case 4:
                return method4(srcEntities, srcReaches);
            default:
                return -1;
        }
    }

    private ArrayList<Entity> getHRUsForReach(ArrayList<Attribute.Entity> srcEntities, long reachID) {
        ArrayList<Entity> list = reach2HRUMap.get((int) reachID);
        return list;
    }

    private int mergeReaches(ArrayList<Attribute.Entity> srcReaches, int reachID) {
        //finde upstream/downstream reach
        //im ersten anlauf downstream reach suchen, falls der nicht existiert upstream rech
        Entity reach = null;
        for (int i = 0; i < srcReaches.size(); i++) {
            Entity e = srcReaches.get(i);
            long id = getID(e);
            if (id == reachID) {
                reach = e;
                break;
            }
        }
        if (reach == null) {
            halt("No Reach to id " + reachID);
        }
        long toReach = getToReach(reach);

        int reachIDsrc = 0;
        Entity src = null;
        int reachIDdst = 0;
        Entity dst = null;
        
        //src wird entfernt dst wird behalten
        if (toReach == 0) {
            return 0;
        } else {
            for (int i = 0; i < srcReaches.size(); i++) {
                Entity e = srcReaches.get(i);
                long id = getID(e);
                if (id == toReach) {
                    reachIDsrc = reachID;
                    src = reach;
                    reachIDdst = (int) id;
                    dst = e;
                    break;
                }
            }
        }
        if (src == null || dst == null) {
            log("Failed to merge reach with id:" + reachID);
            StandardEntityReader.writeParas(srcReaches, "test.par", getModel());
        }

        //aktualisiere reach2HruMap
        srcReaches.remove(src);
        ArrayList<Entity> list = reach2HRUMap.get(reachIDsrc);
        for (Entity e : list) {
            e.setDouble("to_reach", reachIDdst);
        }
        reach2HRUMap.get(reachIDdst).addAll(list);
        reach2HRUMap.put(reachIDsrc, null);

        for (int i = 0; i < srcReaches.size(); i++) {
            Entity e = srcReaches.get(i);
            long id = getToReach(e);
            if (id == reachIDsrc) {
                e.setDouble("to_reach", reachIDdst);
            }
        }

        //merge reaches
        try {
            //src wird entfernt
            double src_length = src.getDouble("length");
            double dst_length = dst.getDouble("length");

            double src_slope = src.getDouble("slope");
            double dst_slope = dst.getDouble("slope");

            double src_rough = src.getDouble("rough");
            double dst_rough = dst.getDouble("rough");

            double src_width = src.getDouble("width");
            double dst_width = dst.getDouble("width");

            double src_weight = src_length / (src_length + dst_length);
            double dst_weight = dst_length / (src_length + dst_length);


            double result_length = src_length + dst_length;
            double result_slope = src_weight * src_slope + dst_weight * dst_slope;
            double result_rough = src_weight * src_rough + dst_weight * dst_rough;
            double result_width = src_width + dst_width;

            dst.setDouble("length", result_length);
            dst.setDouble("slope", result_slope);
            dst.setDouble("rough", result_rough);
            dst.setDouble("width", result_width);

        } catch (NoSuchAttributeException nsae) {
            log(nsae.toString());
            nsae.printStackTrace();
        }
        return reachIDdst;
    }

    public void createDstHRUFile(File srcHRUFile, File dstHRUFile, File srcReachFile, File dstReachFile, double beta) {
        groupMap = new TreeMap<Integer, Integer>();
        reach2HRUMap = new TreeMap<Integer, ArrayList<Entity>>();

        //ArrayList<Attribute.Entity> srcEntities = StandardEntityReader.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), srcHRUFileName.getValue()), getModel());
        ArrayList<Attribute.Entity> srcEntities = StandardEntityReader.readParas(srcHRUFile.getAbsolutePath(), getModel());
        ArrayList<Attribute.Entity> srcReaches = StandardEntityReader.readParas(srcReachFile.getAbsolutePath(), getModel());
        
        if (debug != null && debug.getValue()) {
            initGroupMap(srcEntities);
        }
        initReach2HRUMap(srcEntities, srcReaches);
        
        //count unmergable HRUs        
        int mergeHRUs = (int) ((srcEntities.size() - 1.0) * beta);

        hruCount = srcEntities.size();
        log("Total number of HRUs is: " + srcEntities.size());
        //log("Unmergeable number of HRUs is: " + unmergableHRUs);
        log("Number of HRUs to merge: " + mergeHRUs);
        log("Using Method 1 (Eliminate HRU with smallest area!)");

        //method 1 eliminate smallest hrus
        while (mergeHRUs > 0) {
            int index = findNextCandidate(srcEntities, srcReaches);

            long src_id = getID(srcEntities.get(index));
            long dst_id = getDownstreamHRU(srcEntities, srcReaches, srcEntities.get(index));

            mergeHRUs(srcEntities, srcReaches, src_id, dst_id);

            mergeHRUs--;
            hruCount--;
        }
        StandardEntityReader.writeParas(srcEntities, dstHRUFile.getAbsolutePath(), getModel());
        StandardEntityReader.writeParas(srcReaches, dstReachFile.getAbsolutePath(), getModel());
    }

    private void initGroupMap(ArrayList<Attribute.Entity> srcEntities) {
        for (int i = 0; i < srcEntities.size(); i++) {
            long id = getID(srcEntities.get(i));
            groupMap.put((int) id, (int) id);
        }
    }

    private void initReach2HRUMap(ArrayList<Attribute.Entity> srcEntities, ArrayList<Attribute.Entity> srcReaches) {
        for (int i = 0; i < srcReaches.size(); i++) {
            Entity e = srcReaches.get(i);
            int reachID = (int) getID(e);
            reach2HRUMap.put(reachID, new ArrayList<Entity>());
        }
        for (int i = 0; i < srcEntities.size(); i++) {
            Entity e = srcEntities.get(i);
            int reachID = (int) getToReach(e);
            if (reachID != 0) {
                this.reach2HRUMap.get(reachID).add(e);
            }
        }
    }

    @Override
    public void init() {
        long hashCode = this.dstHRUFileName.getValue().hashCode()
                + this.srcHRUFileName.getValue().hashCode()
                + Double.toString(mergeRate.getValue()).hashCode()
                + Integer.toString(method.getValue()).hashCode();
        if (lastHash == hashCode) {
            log("Skip HRU Reducer, because data is identical to last run.");
            return;
        }

        lastHash = hashCode;

        log("###########################################");
        log("Creating reduces HRU File:");
        log("Source HRU: " + this.srcHRUFileName.getValue());
        log("Modified HRU: " + this.dstHRUFileName.getValue());
        log("Merge-rate: " + this.mergeRate);

        createDstHRUFile(
                new File(this.getModel().getWorkspacePath() + "/" + srcHRUFileName.getValue()),
                new File(this.getModel().getWorkspacePath() + "/" + dstHRUFileName.getValue()),
                new File(this.getModel().getWorkspacePath() + "/" + srcReachFileName.getValue()),
                new File(this.getModel().getWorkspacePath() + "/" + dstReachFileName.getValue()),
                this.mergeRate.getValue());

        log("Merge stopped with: " + this.hruCount + "HRUs");
        log("###########################################");

        //print ids and their assoziated groups
        if (debug.getValue()) {
            String nfoFileName = this.dstHRUFileName.getValue().replaceAll(".par", ".nfo");
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(this.getModel().getWorkspacePath() + "/" + nfoFileName)));
                writer.write("HRU ID\tGROUP\n");
                for (Entry<Integer, Integer> e : groupMap.entrySet()) {
                    writer.write(e.getKey() + "\u0009" + e.getValue() + "\n");
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

    private void halt(String msg) {
        if (getModel() != null && getModel().getRuntime() != null) {
            getModel().getRuntime().sendHalt(msg);
        } else {
            System.out.println("msg");
            System.exit(0);
        }
    }

    private void log(String msg) {
        if (getModel() != null && getModel().getRuntime() != null) {
            getModel().getRuntime().sendInfoMsg(msg);
        } else {
            System.out.println(msg);
        }
    }

    public static void main(String[] args) {
        HRUReachReducer reducer = new HRUReachReducer();
        reducer.dstHRUFileName = DefaultDataFactory.getDataFactory().createString();
        reducer.dstHRUFileName.setValue("C:/Arbeit/test.par");
        reducer.srcHRUFileName = DefaultDataFactory.getDataFactory().createString();
        reducer.srcHRUFileName.setValue("C:/Arbeit/ModelData/JAMS-Gehlberg/parameter/hrus_hor_dist.par");
        reducer.method = DefaultDataFactory.getDataFactory().createInteger();
        reducer.method.setValue(4);
        reducer.debug = DefaultDataFactory.getDataFactory().createBoolean();
        reducer.debug.setValue(true);

        reducer.createDstHRUFile(
                new File("C:/Arbeit/ModelData/ARS/J2KIlm/parameter/hrus_hor_dist.par"), new File("C:/Arbeit/hru_test.par"),
                new File("C:/Arbeit/ModelData/ARS/J2KIlm/parameter/reach.par"), new File("C:/Arbeit/reach_test.par"),
                1.00);
    }
}
