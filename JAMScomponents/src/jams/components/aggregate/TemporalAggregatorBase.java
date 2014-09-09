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

public abstract class TemporalAggregatorBase extends JAMSComponent {
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "current time")
    public Attribute.Calendar time;
            
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "current time")
    public Attribute.TimeInterval interval;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "id of e.g. the spatial unit",
    defaultValue = "1")
    public Attribute.Double id;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Calculate the average value? If average is false, the sum will be calculated.")
    public Attribute.String[] attributeNames;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The value(s) to be aggregated")
    public Attribute.Double[] value;
            
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "A weight to be used to calculate the weighted aggregate",
    defaultValue="1")
    public Attribute.Double weight;
        
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "aggregationMode: sum; avg; min; max; ind;")
    public Attribute.String[] outerAggregationMode;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "aggregationMode: sum; avg; min; max;")
    public Attribute.String[] innerAggregationMode;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "enable or disable aggregation for the i-th value")
    public Attribute.Boolean[] enabled;
            
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The time period to which the values should be aggregated, possible values are: hourly, daily, monthly, seasonal, halfyear, hydhalfyear, yearly, decadly or custom",
    defaultValue = "daily")
    public Attribute.String outerTimeUnit;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The reference time period for aggregation, e.g. yearly mean of months mean, possible values are: hourly, daily, monthly, seasonal, halfyear, hydhalfyear, yearly, decadly",
    defaultValue = "daily")
    public Attribute.String innerTimeUnit;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "custom time period to which values should be aggregated. Is only used when timeUnit is set to custom.",
    defaultValue = "")
    public Attribute.String customOuterTimePeriod;
        
    Double2ObjectAVLTreeMap<TemporalAggregator> aggregators = new Double2ObjectAVLTreeMap<TemporalAggregator>();
    ArrayList<Attribute.TimeInterval> customTimePeriods = new ArrayList<Attribute.TimeInterval>();
    
    AggregationTimePeriod outerTimeUnitID = AggregationTimePeriod.YEARLY;
    AggregationTimePeriod innerTimeUnitID = AggregationTimePeriod.DAILY;
    AggregationMode outerAggregationModeID[] = null;
    AggregationMode innerAggregationModeID[] = null;                    
    boolean isEnabled[] = null;
        
    int n = 0;
    
    @Override
    public void initAll(){
        //create aggreagators
        for (int i=0;i<n;i++){
            AggregationMode innerMode = innerAggregationModeID[i];
            AggregationMode outerMode = outerAggregationModeID[i];
            
            BasicTemporalAggregator innerAggregator = new BasicTemporalAggregator(
                    DoubleArrayAggregator.create(innerMode, n), 
                    innerTimeUnitID);
            
            TemporalAggregator outerAggregator;
            if (outerMode != AggregationMode.INDEPENDENT){                
               outerAggregator = new CompoundTemporalAggregator(
                                    DoubleArrayAggregator.create(outerMode, n),
                                    innerAggregator,                                
                                    outerTimeUnitID,
                                    customTimePeriods);
            }else{
                outerAggregator = new MultiTemporalAggregator(                                    
                                    innerAggregator,                                
                                    outerTimeUnitID,
                                    customTimePeriods);
            }
            
            outerAggregator.addConsumer(new TemporalAggregator.Consumer<double[]>() {
                @Override
                public void consume(Attribute.Calendar c, double[] v) {
                    try{
                        writeData(c,v);
                    }catch(IOException ioe){
                        ioe.printStackTrace();
                    }
                }
            });
            aggregators.put(this.id.getValue(), outerAggregator);
        }                
    }
    
    public boolean checkConfiguration(){
        //check for consistency
        int n = attributeNames.length;
        if (outerAggregationMode != null && outerAggregationMode.length != n){
            getModel().getRuntime().sendInfoMsg("Number of values in parameter \"outerAggregationMode\" does not match the number of attributes");
            return false;
        }
        if (innerAggregationMode != null && innerAggregationMode.length != n){
            getModel().getRuntime().sendInfoMsg("Number of values in parameter \"innerAggregationMode\" does not match the number of attributes");
            return false;
        }
        if (enabled != null && enabled.length != n){
            getModel().getRuntime().sendInfoMsg("Number of values in parameter \"enabled\" does not match the number of attributes");
            return false;
        }
        if (value.length != n){
            getModel().getRuntime().sendInfoMsg("Number of values in parameter \"value\" does not match the number of attributes");
            return false;
        }
        if (outerTimeUnitID == null){
            getModel().getRuntime().sendInfoMsg("Unknown outer aggregation ID:" + outerTimeUnit);
            return false;
        }
        if (innerTimeUnitID == null){
            getModel().getRuntime().sendInfoMsg("Unknown inner aggregation ID:" + innerTimeUnit);
            return false;
        }
        
        return true;
    }
                   
    private void initEnableArray(){
        isEnabled = new boolean[n];
        
        for (int i = 0; i < n; i++) {
            isEnabled[i] = !(enabled != null && enabled[i] != null && !enabled[i].getValue());
        }
    }
        
    @Override
    public void init(){
        getModel().getRuntime().sendInfoMsg("Init " + this.getInstanceName());
        n = attributeNames.length;               
        initEnableArray();
                                
        if (!checkConfiguration()){
            getModel().getRuntime().sendHalt("Configuration of component " + getInstanceName() + " is not valid!");
        }
        
        outerTimeUnitID = AggregationTimePeriod.fromString(outerTimeUnit.getValue());
        innerTimeUnitID = AggregationTimePeriod.fromString(innerTimeUnit.getValue());
        
        if (innerTimeUnitID == null){
            getModel().getRuntime().sendErrorMsg(MessageFormat.format(JAMS.i18n("Unknown time unit:" + innerTimeUnit.getValue() + ".\nPossible values are daily, monthly, yearly and decadly."), getInstanceName()));
        }
        if (outerTimeUnitID == null){
            getModel().getRuntime().sendErrorMsg(MessageFormat.format(JAMS.i18n("Unknown time unit:" + outerTimeUnit.getValue() + ".\nPossible values are daily, monthly, yearly and decadly."), getInstanceName()));
        }
        
        outerAggregationModeID = new AggregationMode[n];
        innerAggregationModeID = new AggregationMode[n];
        for (int i = 0; i < n; i++) {
            outerAggregationModeID[i] = AggregationMode.fromAbbreviation(outerAggregationMode[i].getValue());
            if (innerAggregationMode!=null && innerAggregationMode.length>i && innerAggregationMode[i]!=null){
                innerAggregationModeID[i] = AggregationMode.fromAbbreviation(innerAggregationMode[i].getValue());            
            }else{
                innerAggregationModeID[i] = AggregationMode.AVERAGE;
            }
        }
        
        customTimePeriods.clear();
        if (!customOuterTimePeriod.getValue().isEmpty()) {
            String periods[] = customOuterTimePeriod.getValue().split(";");

            for (String period : periods) {
                period += " 6 1";
                Attribute.TimeInterval ti = DefaultDataFactory.getDataFactory().createTimeInterval();
                ti.setValue(period);
                this.customTimePeriods.add(ti);
            }
            //check if time periods are overlapping
            for (int i = 0; i < customTimePeriods.size(); i++) {
                Attribute.TimeInterval tii = customTimePeriods.get(i);
                for (int j = 0; j < customTimePeriods.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    Attribute.TimeInterval tij = customTimePeriods.get(j);
                    // sii sij  eii eij
                    if ((tii.getStart().before(tij.getStart()) && tii.getEnd().after(tij.getStart()))
                            || (tii.getEnd().before(tij.getEnd()) && tii.getEnd().after(tij.getEnd()))) {
                        getModel().getRuntime().sendHalt("Error: Time-Interval " + tii + " is overlapping with " + tij + "!");
                    }
                }
            }
        }  
    }

    abstract void writeData(Calendar c, double [] values ) throws IOException;
                               
    private void finish(){
        for (TemporalAggregator a : aggregators.values()){
            a.finish();
        }
        //do whatever is now necessary .. 
    }
    
    private double buffer[] = null;//
    @Override
    public void run(){        
        //get id of entity
        double iid = id.getValue();
                
        TemporalAggregator aggregator = aggregators.get(iid);
        //should never happen
        if (aggregator == null)
            return;
                        
        if (buffer==null){
            buffer = new double[n];
        }
        for (int i=0;i<n;i++){
            buffer[i] = value[i].getValue() / weight.getValue();
        }
        aggregator.aggregate(time, buffer);
                        
        //recheck if this is the last timestep, if so output data        
        //avoid cloning calendars!!
        time.add(interval.getTimeUnit(), interval.getTimeUnitCount());
        boolean isLastTimeStep = time.after(interval.getEnd());
        if (isLastTimeStep){
            finish();
        }        
        time.add(interval.getTimeUnit(), -interval.getTimeUnitCount());
    }
}
