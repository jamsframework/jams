package jams.worldwind.data;

import gnu.trove.map.hash.THashMap;
import jams.data.JAMSCalendar;
import jams.workspace.dsproc.DataMatrix;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class DataTransfer3D {

    // ID x attrib x timestep
    private final double[][][] data;

    private final THashMap<String, Integer> hruIdToIndex;
    private final THashMap<String, Integer> attributeToIndex;
    private final THashMap<JAMSCalendar, Integer> timeStepToIndex;

    private final File shapefile;

    public DataTransfer3D(DataMatrix[] m, String[] ids, String[] timesteps, String[] attribs, File shape) {

        int numIds, numAttribs, numTimeSteps;

        numAttribs = m.length;
        numTimeSteps = m[0].getRowDimension();
        numIds = m[0].getColumnDimension();

        this.hruIdToIndex = new THashMap<>(numIds);
        this.attributeToIndex = new THashMap<>(numAttribs);
        this.timeStepToIndex = new THashMap<>(numTimeSteps);
        this.shapefile = shape;

        System.out.println(this.shapefile.toString());

        this.data = new double[numIds][numAttribs][numTimeSteps];

        //for all ids
        for (int i = 0; i < numIds; i++) {
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

    public double getValue(String id, String attrib, JAMSCalendar date) {
        Integer i = this.hruIdToIndex.get(id);
        Integer j = this.attributeToIndex.get(attrib);
        Integer k = this.timeStepToIndex.get(date);
        return this.data[i][j][k];
    }

    public String[] getSortedIds() {
        Set<String> keys = this.hruIdToIndex.keySet();
        int size = keys.size();
        ArrayList<String> list = new ArrayList<>(keys);
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String str1, String str2) {
                return Integer.parseInt(str1) - Integer.parseInt(str2);
            }
        });
        //System.out.println("F: " + list.get(0) + " L: " + list.get(size - 1));
        String[] result = (String[])list.toArray(new String[size]);
        return result;
    }
    
    public String[] getSortedAttributes() {
        Set<String> keys = this.attributeToIndex.keySet();
        int size = keys.size();
        ArrayList list = new ArrayList(keys);
        Collections.sort(list);
        //System.out.println("F: " + list.get(0) + " L: " + list.get(size - 1));
        String[] result = (String[])list.toArray(new String[size]);
        return result;
    }
    
    public JAMSCalendar[] getSortedTimeSteps() {
        Set<JAMSCalendar> keys = this.timeStepToIndex.keySet();
        int size = keys.size();
        ArrayList<JAMSCalendar> list = new ArrayList<>(keys);
        Collections.sort(list);
        //System.out.println("F: " + list.get(0) + " L: " + list.get(size - 1));
        JAMSCalendar[] result = (JAMSCalendar[])list.toArray(new JAMSCalendar[size]);
        return result;        
    }
}
