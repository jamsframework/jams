package io;

import staging.j2k.types.HGeo;
import staging.j2k.types.HRU;
import staging.j2k.types.Landuse;
import staging.j2k.types.Reach;
import staging.j2k.types.SoilType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms3.annotations.*;
import oms3.io.CSTable;
import oms3.io.DataIO;
import static oms3.annotations.Role.*;

@Description
    ("HRU and stream reach parameter file reader")
@Author
    (name = "Peter Krause, Sven Kralisch")
@Keywords
    ("I/O")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/io/StandardEntityReader.java $")
@VersionInfo
    ("$Id: StandardEntityReader.java 952 2010-02-11 19:55:02Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
public class StandardEntityReader {

    private static final Logger log = Logger.getLogger("oms3.model." +
            StandardEntityReader.class.getSimpleName());
    
    @Description("HRU parameter file name")
    @Role(PARAMETER)
    @In  public File hruFile;

    @Description("Reach parameter file name")
    @Role(PARAMETER)
    @In public File reachFile;

    @Description("Land use parameter file name")
    @Role(PARAMETER)
    @In public File luFile;

    @Description("Soil Type parameter file name")
    @Role(PARAMETER)
    @In  public File stFile;

    @Description("Hydrogeology parameter file name")
    @Role(PARAMETER)
    @In  public File gwFile;

    @Description("Collection of hru objects")
    @Out public List<HRU> hrus;

    @Description("Collection of reach objects")
    @Out public List<Reach> reaches;

    @Initialize
    public void init() {
        hrus = new ArrayList<HRU>();
        reaches = new ArrayList<Reach>();
    }

    @Execute
    public void execute() throws IOException {
        System.out.println("Reading HRUs ...");
        readHRUs();

        System.out.println("Reading Reaches ...");
        readReaches();

        System.out.println("Creating topology...");
        createTopology();

        System.out.println("Creating HRU references...");
        createOrderedList(hrus, "to_hru");

        System.out.println("Creating reach references...");
        createOrderedList(reaches, "to_reach");

        System.out.println("Reading Landuse ...");
        readLanduse();

        System.out.println("Reading Soil Types ...");
        readSoilTypes();
        
        System.out.println("Reading Hydrogeology ...");
        readGW();
    }

    static double[] lookupDoubleArray(CSTable table, String name, String[] row) {
        List<Double> l = new ArrayList<Double>();
        for (int i = 0; i < row.length; i++) {
            if (table.getColumnName(i).startsWith(name)) {
                l.add(new Double(row[i]));
            }
        }
        double[] d = new double[l.size()];
        for (int i = 0; i < d.length; i++) {
            d[i] = l.get(i).doubleValue();
        }
        return d;
    }

    static double lookupDouble(CSTable table, String name, String[] row) {
        for (int i = 0; i < row.length; i++) {
            if (table.getColumnName(i).equals(name)) {
                return Double.parseDouble(row[i]);
            }
        }
        throw new IllegalArgumentException("cannot find " + name);
    }

    private void createTopology() {
        Map<Integer, HRU> hruMap = new HashMap<Integer, HRU>();
        Map<Integer, Reach> reachMap = new HashMap<Integer, Reach>();

        //put all entities into a Map with their ID as key
        for (HRU hru : hrus) {
            hruMap.put(hru.ID, hru);
        }

        for (Reach reach : reaches) {
            reachMap.put(reach.ID, reach);
        }

        //associate the hru entities with their downstream entity
        for (HRU hru : hrus) {
            hru.to_hru = hruMap.get(hru.to_hruID);
            hru.to_reach = reachMap.get(hru.to_reachID);
        }

        //associate the reach entities with their downstream entity
        for (Reach reach : reaches) {
            reach.to_reach = reachMap.get(reach.to_reachID);
        }
        //TODO: cycle-check
    }

    @SuppressWarnings("unchecked")
    private void createOrderedList(List col, String asso) {
        HashMap<Object, Integer> depthMap = new HashMap<Object, Integer>();
        for (Object o : col) {
            depthMap.put(o, 0);
        }

        boolean mapChanged = true;
        //put all collection elements (keys) and their depth (values) into a HashMap
        while (mapChanged) {
            mapChanged = false;
            Iterator<Object> hruIterator = col.iterator();
            while (hruIterator.hasNext()) {
                Object e = hruIterator.next();
                Object f = getFieldObject(e, asso);
                if (f != null) {
                    int eDepth = depthMap.get(e);
                    int fDepth = depthMap.get(f);
                    if (fDepth <= eDepth) {
                        depthMap.put(f, fDepth + 1);
                        mapChanged = true;
                    }
                }
            }
        }

        //find out which is the max depth of all entities
        int maxDepth = 0;
        for (Object o : col) {
            maxDepth = Math.max(maxDepth, depthMap.get(o));
        }

        //create ArrayList of ArrayList objects, each element keeping the entities of one level
        List<ArrayList<Object>> alList = new ArrayList<ArrayList<Object>>();
        for (int i = 0; i <= maxDepth; i++) {
            alList.add(new ArrayList<Object>());
        }

        //fill the ArrayList objects within the ArrayList with entity objects
        for (Object o : col) {
            int depth = depthMap.get(o);
            alList.get(depth).add(o);
        }

        ArrayList<Object> newList = new ArrayList<Object>();
        //put the entities
        for (int i = 0; i <= maxDepth; i++) {
            for (Object o : alList.get(i)) {
                newList.add(o);
            }
        }
        col.clear();
        col.addAll(newList);
    }

    static Object getFieldObject(Object target, String name) {
        try {
            return target.getClass().getField(name).get(target);
        } catch (Exception ex) {
            throw new RuntimeException("Field Access failed: " + target + "@" + name);
        }
    }

    private void readHRUs() throws IOException {
        CSTable table = DataIO.table(hruFile, "Parameter");
        for (String[] row : table.rows()) {
            HRU hru = new HRU();
            hru.ID = Integer.parseInt(row[1]);
            hru.x = Double.parseDouble(row[2]);
            hru.y = Double.parseDouble(row[3]);
            hru.elevation = Double.parseDouble(row[4]);
            hru.area = Double.parseDouble(row[5]);
            hru.type = Integer.parseInt(row[6]);
            hru.to_hruID = Integer.parseInt(row[7]);
            hru.to_reachID = Integer.parseInt(row[8]);
            hru.slope = Double.parseDouble(row[9]);
            hru.aspect = Double.parseDouble(row[10]);
            hru.flowlength = Double.parseDouble(row[11]);
            hru.soilID = Integer.parseInt(row[12]);
            hru.landuseID = Integer.parseInt(row[13]);
            hru.hgeoID = Integer.parseInt(row[14]);
            hrus.add(hru);
            System.out.print(".");
            if (log.isLoggable(Level.INFO)) {
                log.info(Arrays.toString(row));
            }
        }
        System.out.println("[" + hrus.size() + "]");
    }

    private void readReaches() throws IOException {
        CSTable table = DataIO.table(reachFile, "Parameter");
        for (String[] row : table.rows()) {
            Reach reach = new Reach();
            reach.ID = Integer.parseInt(row[1]);
            reach.length = Double.parseDouble(row[2]);
            reach.to_reachID = Integer.parseInt(row[3]);
            reach.slope = Double.parseDouble(row[4]);
            reach.rough = Double.parseDouble(row[5]);
            reach.width = Double.parseDouble(row[6]);
            reaches.add(reach);
            System.out.print(".");
            if (log.isLoggable(Level.INFO)) {
                log.info(Arrays.toString(row));
            }
        }
        System.out.println("[" + reaches.size() + "]");
    }

    private void readLanduse() throws IOException {
        Map<Integer, Landuse> luMap = new HashMap<Integer, Landuse>();
        CSTable table = DataIO.table(luFile, "Parameter");
        for (String[] row : table.rows()) {
            Landuse lu = new Landuse();
            lu.LID = Integer.parseInt(row[1]);
            lu.RSC0 = lookupDoubleArray(table, "RSC0", row);
            lu.LAI = lookupDoubleArray(table, "LAI", row);
            lu.effHeight = lookupDoubleArray(table, "effHeight", row);
            lu.rootDepth = lookupDouble(table, "rootDepth", row);
            lu.sealedGrade = lookupDouble(table, "sealedGrade", row);
            luMap.put(lu.LID, lu);
            System.out.print(".");
            if (log.isLoggable(Level.INFO)) {
                log.info(Arrays.toString(row));
            }
        }
        for (HRU hru : hrus) {
            hru.landuse = luMap.get(hru.landuseID);
        }
        System.out.println("[" + luMap.size() + "]");
    }

    private void readSoilTypes() throws IOException {
        Map<Integer, SoilType> stMap = new HashMap<Integer, SoilType>();
        CSTable table = DataIO.table(stFile, "Parameter");
        for (String[] row : table.rows()) {
            SoilType st = new SoilType();
            st.SID = Integer.parseInt(row[1]);
            st.depth = Double.parseDouble(row[2]);
            st.kf_min = Double.parseDouble(row[3]);
            st.kf_max = Double.parseDouble(row[4]);
            st.depth_min = Double.parseDouble(row[5]);
            st.cap_rise = Double.parseDouble(row[6]);
            st.aircap = Double.parseDouble(row[7]);
            st.fc_sum = Double.parseDouble(row[8]);
            st.fc = lookupDoubleArray(table, "fc", row);

            stMap.put(st.SID, st);
            System.out.print(".");
            if (log.isLoggable(Level.INFO)) {
                log.info(Arrays.toString(row));
            }
        }
        for (HRU hru : hrus) {
            hru.soilType = stMap.get(hru.soilID);
        }
        System.out.println("[" + stMap.size() + "]");
    }

    private void readGW() throws IOException {
        Map<Integer, HGeo> gwMap = new HashMap<Integer, HGeo>();
        CSTable table = DataIO.table(gwFile, "Parameter");
        for (String[] row : table.rows()) {
            HGeo geo = new HGeo();
            geo.GID = Integer.parseInt(row[1]);
            geo.RG1_max = Double.parseDouble(row[2]);
            geo.RG2_max = Double.parseDouble(row[3]);
            geo.RG1_k = Double.parseDouble(row[4]);
            geo.RG2_k = Double.parseDouble(row[5]);
            geo.RG1_active = Double.parseDouble(row[6]);

            gwMap.put(geo.GID, geo);
            System.out.print(".");
            if (log.isLoggable(Level.INFO)) {
                log.info(Arrays.toString(row));
            }
        }
        for (HRU hru : hrus) {
            hru.hgeoType = gwMap.get(hru.hgeoID);
        }
        System.out.println("[" + gwMap.size() + "]");
    }
}
