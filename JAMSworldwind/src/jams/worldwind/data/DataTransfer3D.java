package jams.worldwind.data;

import gnu.trove.map.hash.TObjectIntHashMap;
import jams.data.JAMSCalendar;
import jams.workspace.dsproc.DataMatrix;
import jams.workspace.stores.ShapeFileDataStore;
import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class DataTransfer3D implements Serializable {

    // ID x attrib x timestep
    private final double[][][] data;

    //fast Hashtables to get values
    private final TObjectIntHashMap<String> hruIdToIndex;
    private final TObjectIntHashMap<String> attributeToIndex;
    private final TObjectIntHashMap<JAMSCalendar> timeStepToIndex;

    //maps the ids to file position
    private int[] idMap = null;

    private ShapeFileDataStore shapefileDataStore;

    public DataTransfer3D(DataMatrix[] m, String[] ids, String[] timesteps, String[] attribs) {

        int numIds, numAttribs, numTimeSteps;

        numAttribs = m.length;
        numTimeSteps = m[0].getRowDimension();
        numIds = m[0].getColumnDimension();

        this.hruIdToIndex = new TObjectIntHashMap<>(numIds);
        this.attributeToIndex = new TObjectIntHashMap<>(numAttribs);
        this.timeStepToIndex = new TObjectIntHashMap<>(numTimeSteps);

        this.data = new double[numIds][numAttribs][numTimeSteps];
        this.shapefileDataStore = null;

        //for all ids
        for (int i = 0; i < numIds; i++) {
            //for all attributes
            for (int j = 0; j < m.length; j++) {
                double[] column = m[j].getCol(i);
                //for alle timesteps
                for (int k = 0; k < column.length; k++) {
                    this.data[i][j][k] = column[k];
//                    JAMSCalendar date = new JAMSCalendar();
//                    date.setValue(timesteps[k]);
//                    this.timeStepToIndex.put(date, k);
                }
//                this.attributeToIndex.put(attribs[j], j);
            }
            this.hruIdToIndex.put(ids[i], i);
        }
        
        for (int j = 0; j < m.length; j++) {
            this.attributeToIndex.put(attribs[j], j);
        }        
        
        for (int k = 0; k < timesteps.length; k++) {
            JAMSCalendar date = new JAMSCalendar();
            date.setValue(timesteps[k]);
            this.timeStepToIndex.put(date, k);
        }        

    }

    public DataTransfer3D(String[] ids, String[] timesteps, String[] attribs) {
        int numIds, numAttribs, numTimeSteps;

        numAttribs = attribs.length;
        numTimeSteps = timesteps.length;
        numIds = ids.length;

        this.hruIdToIndex = new TObjectIntHashMap<>(numIds);
        this.attributeToIndex = new TObjectIntHashMap<>(numAttribs);
        this.timeStepToIndex = new TObjectIntHashMap<>(numTimeSteps);

        this.data = new double[numIds][numAttribs][numTimeSteps];
        this.shapefileDataStore = null;

        //Arrays.sort(ids);
        for (int i = 0; i < ids.length; i++) {
            this.hruIdToIndex.put(ids[i], i);
        }

        for (int i = 0; i < attribs.length; i++) {
            this.attributeToIndex.put(attribs[i], i);
        }

        for (int i = 0; i < timesteps.length; i++) {
            JAMSCalendar calendar = new JAMSCalendar();
            calendar.setValue(timesteps[i]);
            this.timeStepToIndex.put(calendar, i);
        }
    }

    /*
        geht noch nicht, für direkten Zugriff auf getTemporalData
    */
    /*
    public void addDataVales(DataMatrix m, JAMSCalendar calendar) {

        //System.out.println("COL: " + m.getColumnDimension());
        //System.out.println("ROW: " + m.getRowDimension());
        //System.out.println(calendar.toString());
        //if(this.idMap == null) {
        //if (idMap == null) {
            idMap = new int[this.hruIdToIndex.size()];
        
            //String[] tmpids = this.hruIdToIndex.keys(new String[0]);
            TreeSet<String> tmp = new TreeSet<>(this.hruIdToIndex.keySet());
            Iterator<String> it = tmp.iterator();
            int z = 0;
            while (it.hasNext()) {
                idMap[z] = m.getIDPosition(it.next());
                z++;
            }
            //Arrays.sort(idMap);
        //}

        //final String[] attribs = m.getAttributeIDs();
        int k = Integer.valueOf(this.timeStepToIndex.get(calendar));

        //String[] tmpids = this.hruIdToIndex.keys(new String[0]);
        //for (String id : this.hruIdToIndex.keySet()) {
        it = tmp.iterator();
        while (it.hasNext()) {
            //for (int h = 0; h < tmpids.length; h++) {
            int i = Integer.valueOf(this.hruIdToIndex.get(it.next()));
            //for (String attr : this.attributeToIndex.keySet()) {
            //int j = Integer.valueOf(this.attributeToIndex.get(attr));
            for (int n = 0; n < m.getAttributeIDs().length; n++) {
                int j = Integer.valueOf(this.attributeToIndex.get(m.getAttributeIDs()[n]));
                double[] d = m.getCol(n);
                data[i][j][k] = d[idMap[i]];
            }
            //}

        }

        //System.out.println();
/*
         //for all attribs
         for (int i = 0; i < m.getColumnDimension(); i++) {
         double[] col = m.getCol(i);
         //get only selected values from ui
         for (int j = 0; j < col.length; j++) {
         int pos = Arrays.binarySearch(ids, j);
         if (pos >= 0) {

         if (this.attributeToIndex.containsKey(attribs[i])) {
         this.data[j][i][timeStepIndex] = col[i];
         }

         this.hruIdToIndex.put(ids[pos], this.hruIdToIndex.size());
         }
         }

         }
         */
        /*
         //for all ids
         for (int i = 0; i < m.; i++) {
         //for all attributes
         for (int j = 0; j < m.length; j++) {
         double[] column = m[j].getCol(i);
         //for alle timesteps
         for (int k = 0; k < column.length; k++) {
         this.data[i][j][k] = column[k];
         JAMSCalendar date = new JAMSCalendar();
         date.setValue(timesteps[k]);
         this.timeStepToIndex.put(date, k);
         }
         this.attributeToIndex.put(attribs[j], j);
         }
         this.hruIdToIndex.put(ids[i], i);
         }
    }
    */

    public ShapeFileDataStore getShapeFileDataStore() {
        return this.shapefileDataStore;
    }

    public void setShapeFileDataStore(ShapeFileDataStore dataStore) {
        this.shapefileDataStore = dataStore;
    }

    public String getKeyColumn() {
        return this.shapefileDataStore.getKeyColumn();
    }

    public double getValue(String id, String attrib, JAMSCalendar date) {
        if (this.hruIdToIndex.containsKey(id) && this.attributeToIndex.containsKey(attrib) && this.timeStepToIndex.containsKey(date)) {
            int i = this.hruIdToIndex.get(id);
            int j = this.attributeToIndex.get(attrib);
            int k = this.timeStepToIndex.get(date);
            return this.data[i][j][k];
        } else {
            return Double.NaN;
        }
    }

    //public boolean 
    public String[] getSortedIds() {
        Object[] keys = this.hruIdToIndex.keys();
        Arrays.sort(keys);
        //THashSet<String> keys = this.hruIdToIndex.keys();
        //int size = keys.size();
        //ArrayList<String> list = new ArrayList<>(keys);
        /*Collections.sort(list, new Comparator<String>() {
         @Override
         public int compare(String str1, String str2) {
         return Integer.parseInt(str1) - Integer.parseInt(str2);
         }
         });
         */
        //System.out.println("F: " + list.get(0) + " L: " + list.get(size - 1));
        //String[] result = (String[]) list.toArray(new String[size]);
        return Arrays.copyOf(keys, keys.length, String[].class);
    }

    public String[] getSortedAttributes() {
        Object[] keys = this.attributeToIndex.keys();
        Arrays.sort(keys);
        /*
         Set<String> keys = this.attributeToIndex.keySet();
         int size = keys.size();
         ArrayList list = new ArrayList(keys);
         Collections.sort(list);
         //System.out.println("F: " + list.get(0) + " L: " + list.get(size - 1));
         String[] result = (String[]) list.toArray(new String[size]);
         */
        return Arrays.copyOf(keys, keys.length, String[].class);
    }

    public JAMSCalendar[] getSortedTimeSteps() {
        Object[] keys = this.timeStepToIndex.keys();
        Arrays.sort(keys);
        /*
         Set<JAMSCalendar> keys = this.timeStepToIndex.keySet();
         int size = keys.size();
         ArrayList<JAMSCalendar> list = new ArrayList<>(keys);
         Collections.sort(list);
         //System.out.println("F: " + list.get(0) + " L: " + list.get(size - 1));
         JAMSCalendar[] result = (JAMSCalendar[]) list.toArray(new JAMSCalendar[size]);
         */
        return Arrays.copyOf(keys, keys.length, JAMSCalendar[].class);
    }
}
