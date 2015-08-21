package io;

import ages.types.HRU;
import ages.types.SoilType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import oms3.annotations.*;
import oms3.io.CSTable;
import oms3.io.DataIO;
import static oms3.annotations.Role.*;

@Description
    ("Soils parameter file reader")
@Author
    (name= "od")
@Keywords
    ("I/O")
@VersionInfo
    ("$Id: LayeredSoilParaReader.java 963 2010-02-11 20:38:23Z odavid $")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/io/LayeredSoilParaReader.java $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")

 public class LayeredSoilParaReader  {
    
    @Description("Soil type parameter file name.")
    @Role(PARAMETER)
    @In public File stFile;

    @Description("Collection of hru objects")
    @In @Out public List<HRU> hrus;

    public static double[] convert(List<Double> l) {
        double[] a = new double[l.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = l.get(i);
        }
        return a;
    }
    
    @Execute
    public void execute() throws IOException {
        List<Double> depth = null;
        List<Double> aircapacity = null;
        List<Double> fieldcapacity = null;
        List<Double> deadcapacity = null;
        List<Double> kf = null;
        List<Double> bulk_density = null;
        List<Double> corg = null;
        List<Double> root = null;

        System.out.println("Reading Soil Parameter ...");

        Map<Integer, SoilType> stMap = new HashMap<Integer, SoilType>();

        CSTable table = DataIO.table(stFile, "Parameter");
        int last = 0;
        for (String[] row : table.rows()) {
            int id = Integer.parseInt(row[1]);
            if (id - last > 1) {
                if (depth != null) {
                    SoilType st = new SoilType();
                    st.SID = (last - depth.size()) / 100;
                    st.horizons = depth.size();
                    st.depth = convert(depth);
                    st.aircapacity = convert(aircapacity);
                    st.fieldcapacity = convert(fieldcapacity);
                    st.deadcapacity = convert(deadcapacity);
                    st.kf = convert(kf);
                    st.bulk_density = convert(bulk_density);
                    st.corg = convert(corg);
                    st.root = convert(root);
                    stMap.put(st.SID, st);
                }
                depth = new ArrayList<Double>();
                aircapacity = new ArrayList<Double>();
                fieldcapacity = new ArrayList<Double>();
                deadcapacity = new ArrayList<Double>();
                kf = new ArrayList<Double>();
                bulk_density = new ArrayList<Double>();
                corg = new ArrayList<Double>();
                root = new ArrayList<Double>();
            }
            last = id;
            depth.add(Double.parseDouble(row[2]));
            aircapacity.add(Double.parseDouble(row[3]));
            fieldcapacity.add(Double.parseDouble(row[4]));
            deadcapacity.add(Double.parseDouble(row[5]));
            kf.add(Double.parseDouble(row[6]));
            bulk_density.add(Double.parseDouble(row[7]));
            corg.add(Double.parseDouble(row[8]));
            root.add(Double.parseDouble(row[9]));
        }
        SoilType st = new SoilType();
        st.horizons = depth.size();
        st.SID = (last - depth.size()) / 100;
        st.depth = convert(depth);
        st.aircapacity = convert(aircapacity);
        st.fieldcapacity = convert(fieldcapacity);
        st.deadcapacity = convert(deadcapacity);
        st.kf = convert(kf);
        st.bulk_density = convert(bulk_density);
        st.corg = convert(corg);
        st.root = convert(root);
        stMap.put(st.SID, st);

        for (HRU hru : hrus) {
            st = stMap.get(hru.soilID);
            if (st == null) {
                throw new RuntimeException("No soil type for " + hru.soilID);
            }
            hru.soilType = st;
        }
    }
}
