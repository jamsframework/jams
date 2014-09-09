/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.aggregate;

import it.unimi.dsi.fastutil.doubles.Double2ObjectAVLTreeMap;
import jams.JAMS;
import jams.aggregators.Aggregator.AggregationMode;

import jams.aggregators.BasicTemporalAggregator;
import jams.aggregators.CompoundTemporalAggregator;
import jams.aggregators.DoubleArrayAggregator;
import jams.aggregators.MultiTemporalAggregator;
import jams.aggregators.TemporalAggregator;
import jams.aggregators.TemporalAggregator.AggregationTimePeriod;
import jams.data.Attribute;
import jams.data.Attribute.Calendar;
import jams.data.DefaultDataFactory;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import jams.tools.FileTools;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author christian
 */
@JAMSComponentDescription(
        title = "TimePeriodAggregator",
        author = "Christian Fischer",
        description = "Aggregates timeseries values to a given time period of day, month, year or dekade")
@Deprecated
public class TemporalAggregatorShapefile extends TemporalAggregatorBase {
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "the shapefile to which data should be added.")
    public Attribute.String shpFile;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "the name of the id field in the shapefile.",
    defaultValue="ID")
    public Attribute.String idFieldName;
    
    SimpleOutputDataStore outData[] = null;
    SpatialOutputDataStore outData2[] = null;
    File dbfFileOriginal = null;
    ShapeFileOutputDataStore shpStore[] = null;
    
    boolean isHeaderWritten = false;
    
    Double2ObjectAVLTreeMap<double[]> spatialUnits = new Double2ObjectAVLTreeMap<double[]>();
    double buffer[] = null;
    Calendar lastTimeStep = null;
    
    @Override
    public void init(){
        super.init();
        isHeaderWritten = false;
        outData = new SimpleOutputDataStore[n];        
        outData2 = new SpatialOutputDataStore[n];        
        shpStore = new ShapeFileOutputDataStore[n];
        
        //copy shapefile to output directory
        for (int i = 0; i < n; i++) {            
            if (!isEnabled[i]){
                continue;
            }
                        
            String prefix;
            if (innerAggregationMode!=null){                
                prefix = attributeNames[i].getValue() + " " + JAMS.i18n(outerTimeUnitID.name()) + " " + JAMS.i18n(outerAggregationModeID[i].name()) + " of " + JAMS.i18n(innerTimeUnitID.name()) + " " + JAMS.i18n(innerAggregationModeID[i].name());
            }else{                
                prefix = attributeNames[i].getValue() + " " + JAMS.i18n(outerTimeUnitID.name()) + " " + JAMS.i18n(outerAggregationModeID[i].name());
            }
            
            File f = new File(FileTools.createAbsoluteFileName(getModel().getWorkspace().getOutputDataDirectory().getAbsolutePath(), prefix + ".dat"));
            File f2 = new File(FileTools.createAbsoluteFileName(getModel().getWorkspace().getOutputDataDirectory().getAbsolutePath(), prefix + "_SODS.dat"));
            
            try {
                outData[i] = new SimpleOutputDataStore(f);
                outData2[i] = new SpatialOutputDataStore(f2);
            } catch (IOException ioe) {
                getModel().getRuntime().sendHalt("Can't write to output file:" + f);
            } 
            
            File originalShpFile = new File(FileTools.createAbsoluteFileName(getModel().getWorkspacePath(), shpFile.getValue()));
            File newDBFFile = new File(getModel().getWorkspace().getOutputDataDirectory().getAbsolutePath() + "/" + prefix);
            newDBFFile.mkdirs();
            try {
                shpStore[i] = new ShapeFileOutputDataStore(originalShpFile, newDBFFile);
            } catch (IOException ioe) {
                getModel().getRuntime().sendErrorMsg(MessageFormat.format(ioe.toString(), getInstanceName()));
            }
        }
    }
        
    protected void writeData(Calendar c, double [] values ) throws IOException{
        if (lastTimeStep == null || c.getTimeInMillis() == lastTimeStep.getTimeInMillis()){
            spatialUnits.put(this.id.getValue(), values);
            return;
        }
    
        if (buffer == null || buffer.length != spatialUnits.size()){
            buffer = new double[spatialUnits.size()];
        }
        
        for (int i = 0; i < n; i++) {
            if (!isEnabled[i])
                continue;
            if (!isHeaderWritten) {
                outData[i].setHeader(spatialUnits.keySet());
                outData2[i].setHeader(spatialUnits.keySet());
            }
            int j=0;
            for (double value[] : spatialUnits.values()){
                buffer[j++] = value[i];
            }
            //outData[i].writeData(c.toString(), buffer);
            //outData2[i].writeData(c.toString(), buffer);
        } 
        isHeaderWritten = true;
    }
}
