/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.aggregate;

import jams.JAMS;
import jams.aggregators.Aggregator.AggregationMode;

import jams.aggregators.TemporalAggregator.AggregationTimePeriod;
import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import java.text.MessageFormat;
import java.util.ArrayList;

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
                    
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "A weight to be used to calculate the weighted aggregate",
    defaultValue="1")
    public Attribute.Double weight;*/
        
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
        
    protected final ArrayList<Attribute.TimeInterval> customTimePeriods = new ArrayList<Attribute.TimeInterval>();
    
    private AggregationTimePeriod outerTimeUnitID = AggregationTimePeriod.YEARLY;
    private AggregationTimePeriod innerTimeUnitID = AggregationTimePeriod.DAILY;
    private AggregationMode outerAggregationModeID[] = null;
    private AggregationMode innerAggregationModeID[] = null;                    
    private boolean isEnabled[] = null;
    private int n = 0;

    boolean isEnabled(int i){
        return isEnabled[i];
    }
    
    protected AggregationMode getOuterAggregationModeID(int i){
        return outerAggregationModeID[i];
    }
    protected AggregationMode getInnerAggregationModeID(int i){
        return innerAggregationModeID[i];
    }
    
    protected AggregationTimePeriod getInnerTimeUnitID(){
        return innerTimeUnitID;
    }
    
    protected AggregationTimePeriod getOuterTimeUnitID(){
        return outerTimeUnitID;
    }
    
    protected int getNumberOfAttributes(){
        return n;
    }
    
    private boolean checkConfiguration(){
        //check for consistency        
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
        /*if (value.length != n){
            getModel().getRuntime().sendInfoMsg("Number of values in parameter \"value\" does not match the number of attributes");
            return false;
        }*/
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
        n = outerAggregationMode.length;
        if (!checkConfiguration()){
            getModel().getRuntime().sendHalt("Configuration of component " + getInstanceName() + " is not valid!");
        }
        
        initEnableArray();
                                                
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
}
