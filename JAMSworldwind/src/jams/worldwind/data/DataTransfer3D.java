package jams.worldwind.data;

import gnu.trove.map.hash.THashMap;
import jams.data.JAMSCalendar;
import jams.workspace.dsproc.DataMatrix;

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
    
    public DataTransfer3D(DataMatrix[] m, String[] ids, String[] timesteps, String[] attribs) {

        int numIds, numAttribs, numTimeSteps;

        numAttribs = m.length;
        numTimeSteps = m[0].getRowDimension();
        numIds = m[0].getColumnDimension();

        this.hruIdToIndex = new THashMap<>(numIds);
        this.attributeToIndex = new THashMap<>(numAttribs);
        this.timeStepToIndex = new THashMap<>(numTimeSteps);

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
}