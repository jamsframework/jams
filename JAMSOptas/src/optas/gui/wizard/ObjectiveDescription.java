/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.wizard;

import jams.meta.ComponentDescriptor;
import jams.meta.ComponentField;
import java.util.ArrayList;
import java.util.HashMap;
import optas.efficiencies.UniversalEfficiencyCalculator;
import optas.data.TimeFilter;
import optas.data.TimeFilterCollection;
import optas.data.TimeFilterFactory;

/**
 *
 * @author christian
 */
public class ObjectiveDescription {
    private String name;
    private String efficiency;
    private Attribute measurement, simulation, time;
        
    private TimeFilterCollection timeFilters = new TimeFilterCollection();
    private jams.data.Attribute.TimeInterval modelTimeInterval = null;
    
    
    public ObjectiveDescription(String name){
        this.name = name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getName(){
        return name;
    }
    
    public void setTimeAttribute(Attribute time){
        this.time = time;
    }
    
    public Attribute getTimeAttribute(){
        return this.time;
    }
    
    public void setModelTimeInterval(jams.data.Attribute.TimeInterval modelTimeInterval){
        this.modelTimeInterval = modelTimeInterval;
    }
    
    public jams.data.Attribute.TimeInterval getModelTimeInterval(){
        return modelTimeInterval;
    }
    
    public Attribute getMeasurementAttribute(){
        return measurement;
    }
    
    public Attribute getSimulationAttribute(){
        return simulation;
    }
    
    public void setMeasurementAttribute(Attribute measurement){
        this.measurement = measurement;
    }
    
    public void setSimulationAttribute(Attribute simulation){
        this.simulation = simulation;
    }
    
    public void setEfficiency(String efficiency){
        this.efficiency = efficiency;
    }
    
    public TimeFilterCollection getTimeFilters(){
        return this.timeFilters;
    }
    
    public void setTimeFilters(ArrayList<TimeFilter> filters){
        this.timeFilters.set(filters);
    }
    public void setTimeFilters(TimeFilterCollection timeFilters){
        this.timeFilters = timeFilters;
    }
    
    public String getEfficiency(){
        return this.efficiency;
    }
    
    static public ObjectiveDescription importFromComponentDescriptor(ComponentDescriptor cd) throws OPTASWizardException{
        ObjectiveDescription od = new ObjectiveDescription(cd.getInstanceName());
        HashMap<String, ComponentField> map = cd.getComponentFields();

        if (!UniversalEfficiencyCalculator.class.isAssignableFrom(cd.getClazz())){
            throw new OPTASWizardException("TODO");
        }
        
        if (map.get("measurement").getContextAttributes() == null || map.get("measurement").getContextAttributes().size()==0){
            throw new OPTASWizardException("TODO");
        }
        
        if (map.get("simulation").getContextAttributes() == null || map.get("simulation").getContextAttributes().size()==0){
            throw new OPTASWizardException("TODO");
        }
        Attribute measurementAttribute = new Attribute(map.get("measurement").getContextAttributes().get(0));
        Attribute simulationAttribute  = new Attribute(map.get("simulation").getContextAttributes().get(0));
        
        od.setMeasurementAttribute(measurementAttribute);
        od.setSimulationAttribute(simulationAttribute);
        
        if (map.get("timeInterval")==null){
            throw new OPTASWizardException("TODO");
        }
        
        String timeIntervalString = map.get("timeInterval").getValue();
        if (timeIntervalString != null){
            String timeIntervalStrings[] = timeIntervalString.split(";");            
            for (String s : timeIntervalStrings){
                jams.data.Attribute.TimeInterval interval = jams.data.DefaultDataFactory.getDataFactory().createTimeInterval();
                interval.setValue(s);
                TimeFilter tf = TimeFilterFactory.getRangeFilter(interval);
                od.timeFilters.add(tf);
            }                                    
        }
        
        return od;
    }
            
    public String toString(){
        return this.getName();
    }
}
