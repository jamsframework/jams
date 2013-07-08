/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.wizard;

import jams.dataaccess.DataAccessor;
import jams.meta.ComponentDescriptor;
import jams.meta.ComponentField;
import jams.meta.ContextAttribute;
import jams.meta.ContextDescriptor;
import jams.meta.ModelDescriptor;
import jams.meta.ModelProperties.ModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import optas.gui.wizard.Parameter.Range;

/**
 *
 * @author Christian Fischer
 */
public class ModelAnalyzer {

    public static final int COLLECT_READATTRIBUTES = 0;
    public static final int COLLECT_WRITEATTTRIBUTES = 1;
    public String error = null;
    
    ModelDescriptor md;
    

    public ModelAnalyzer(ModelDescriptor md) {
        this.md = md;
    }

    

    public ModelDescriptor getModelDescriptor() {
        return md;
    }

    private HashMap<String, Range> getDefaultRangeMap() {
        HashMap<String, Range> map = new HashMap<String, Range>();
        
        String groupNames[] = md.getModelProperties().getAllGroupNames();
        for (String groupName : groupNames){
            ArrayList<Object> properties = md.getModelProperties().getGroup(groupName).getProperties();
            for (Object o : properties){
                if (o instanceof ModelProperty){
                    ModelProperty property = (ModelProperty)o;
                    if (property.var != null && property.var.getParent() != null){
                        String componentName = property.var.getParent().getInstanceName();
                        String propertyName = property.var.getName();
                        double lowerBound = property.lowerBound;
                        double upperBound = property.upperBound;
                        
                        map.put(componentName + "." + propertyName, new Range(lowerBound, upperBound));
                    }
                    if (property.attribute != null && property.attribute.getContext() != null){
                        String componentName = property.attribute.getContext().getInstanceName();
                        String propertyName = property.attribute.getName();
                        double lowerBound = property.lowerBound;
                        double upperBound = property.upperBound;
                        
                        map.put(componentName + "." + propertyName, new Range(lowerBound, upperBound));
                    }
                }
            }
        }
        return map;        
    }

    private Set<Attribute> getAttributeList(int mode) {
        HashMap<String, ComponentDescriptor> map = md.getComponentDescriptors();
        Set<Attribute> list = new TreeSet<Attribute>();
        for (ComponentDescriptor cd : map.values()){
            //parameter are either component values
            for (ComponentField field : cd.getComponentFields().values()){
                String value = field.getValue();
               
                if (!jams.data.Attribute.Double.class.isAssignableFrom(field.getType()))
                    continue;
                
                if (mode == COLLECT_READATTRIBUTES){
                    if (field.getAccessType() != DataAccessor.READ_ACCESS)                        
                        continue;
                     if (value == null)
                        continue;
                     
                     list.add(new Attribute(field));
                }else if (field.getAccessType() != DataAccessor.READ_ACCESS){
                    list.add(new Attribute(field));    
                }
            }
            //or context attributes
            if (cd instanceof ContextDescriptor){
                ContextDescriptor contextDesc = (ContextDescriptor)cd;
                for (ContextAttribute ca : contextDesc.getStaticAttributes().values()){
                    list.add(new Attribute(ca));
                }
            }
        }         
        return list;
    }

    SortedSet<Parameter> result = null;
    public SortedSet<Parameter> getParameters() {
        if (result != null)
            return result;
        result = new TreeSet<Parameter>();

        Set<Attribute> parameterList = getAttributeList(COLLECT_READATTRIBUTES);        
        
        HashMap<String, Range> defaultRangeMap = getDefaultRangeMap();
        Iterator<Attribute> iter1 = parameterList.iterator();
        //match parameters with default values
        while (iter1.hasNext()) {
            Parameter variable = new Parameter(iter1.next());
            if (variable.field instanceof ComponentField){
                ComponentField cf = (ComponentField)variable.field;
                if (cf.getParent().isEnabled()){
                    Range range = defaultRangeMap.get(cf.getParent() + "." + cf.getName());
                    if (range != null) {
                        variable.setLowerBound(range.lowerBound);
                        variable.setUpperBound(range.upperBound);
                    }
                    result.add(variable);
                }
            }
            if (variable.field instanceof ContextAttribute){
                ContextAttribute ca = (ContextAttribute)variable.field;
                if (ca.getContext().isEnabled()) {
                    Range range = defaultRangeMap.get(ca.getContext().getInstanceName() + "." + ca.getName());
                    if (range != null) {
                        variable.setLowerBound(range.lowerBound);
                        variable.setUpperBound(range.upperBound);
                    }
                    result.add(variable);
                }
            }
        }
        return result;
    }

    public SortedSet<Objective> getObjectives() {
        SortedSet<Objective> result = new TreeSet<Objective>();
        Set<Attribute> objectiveList = getAttributeList(COLLECT_WRITEATTTRIBUTES);
        Iterator<Attribute> iter1 = objectiveList.iterator();
        while (iter1.hasNext()) {
            result.add(new Objective(iter1.next()));
        }
        return result;
    }    
}
