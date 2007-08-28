/*
 * J2KTimeSeriesObject.java
 *
 * Created on 24. April 2006, 08:46
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jamschartfactory.data;

/**
 *
 * @author c0krpe
 */
public class J2KTimeSeriesObject {
    public String fileName;
    public String[] dataCols;
    public double[][] dataMatrix;
    public int[][] dateMatrix;
    public int nTsteps;
    public int nSeries;
    
    /** Creates a new instance of J2KTimeSeriesObject */
    public J2KTimeSeriesObject() {
    }
    
    public void CreateJ2KTimeSeriesObject(){
        J2KDataFileReader dfr = new J2KDataFileReader();
        this.nSeries = dfr.nSeries;
        this.nTsteps = dfr.nTsteps;
        this.fileName = dfr.fileName;
        this.dataCols = dfr.dataCols;
        this.dataMatrix = dfr.dataMatrix;
        this.dateMatrix = dfr.dateMatrix;
    }
    
    public Object[][] dataAsObject(){
        Object[][] obj = new Object[this.nTsteps][this.nSeries];
        for(int i = 0; i < this.nTsteps; i++){
            for(int j = 0; j < this.nSeries; j++){
                obj[i][j] = this.dataMatrix[j][i];
            }
        }
        return obj;
    }
    
}
