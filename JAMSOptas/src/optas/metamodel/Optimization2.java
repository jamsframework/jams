/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.metamodel;

import jams.data.Attribute;
import jams.meta.ComponentDescriptor;
import jams.meta.ComponentField;
import jams.meta.ContextAttribute;
import jams.meta.ContextDescriptor;
import jams.meta.ModelDescriptor;
import jams.meta.ModelNode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import optas.metamodel.Tools.Range;
import optas.optimizer.Optimizer;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.OptimizerLibrary;
import optas.optimizer.management.OptimizerParameter;
import optas.optimizer.management.SimpleOptimizationController;

/**
 *
 * @author chris
 */
public class Optimization2 implements Serializable {
    private ModelModifier2 modifier;    
    private ModelAnalyzer2 analyser;
    
    private OptimizerDescription optimizerDescription;
    private Set<Parameter2> parameter = new TreeSet<Parameter2>();
    private Set<ContextAttribute> objective = new TreeSet<ContextAttribute>();
    
    private String name;
        
    public Optimization2(ModelDescriptor md) {
        modifier = new ModelModifier2(md);
        analyser = new ModelAnalyzer2(md);
        if (!importFromModelDescriptor(md)){
            optimizerDescription = modifier.addOptimizationContext();            
        }
    }
       
    public ModelDescriptor getModelDescriptor(){
        return modifier.getModelDescriptor();
    }
    
    public boolean addParameter(Parameter2 p){
        getParameter().add(p);
        if (modifier.updateParameterList(getParameter()))
            return true;
        return false;
    }

    public void addObjective(ContextAttribute c){
        getObjective().add(c);
    }
    
    public Set<Parameter2> getModelParameters(){
        return analyser.getParameters();
    }
    
    /**
     * @return the optimizerDescription
     */
    public OptimizerDescription getOptimizerDescription() {
        return optimizerDescription;
    }

    /**
     * @param optimizerDescription the optimizerDescription to set
     */
    public void setOptimizerDescription(OptimizerDescription optimizerDescription) {
        this.modifier.setOptimizerDescription(optimizerDescription);
        this.optimizerDescription = optimizerDescription;        
    }

    /**
     * @return the parameter
     */
    public Set<Parameter2> getParameter() {
        return parameter;
    }

    /**
     * @param parameter the parameter to set
     */
    public void setParameter(Set<Parameter2> parameter) {
        this.parameter = parameter;
    }

    /**
     * @return the objective
     */
    public Set<ContextAttribute> getObjective() {
        return objective;
    }

    /**
     * @param objective the objective to set
     */
    public void setObjective(Set<ContextAttribute> objective) {
        this.objective = new TreeSet<ContextAttribute>();
        this.objective.addAll(objective);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    private boolean importFromModelDescriptor(ModelDescriptor md){           
        ContextDescriptor optimizerContext = null;
        try{
            optimizerContext = (ContextDescriptor)md.getComponentDescriptor("optimizer");
        }catch(ClassCastException cce){
            return false;
        }
        if (optimizerContext == null)
            return false;
        if (!optimizerContext.getClazz().isAssignableFrom(SimpleOptimizationController.class))
            return false;
        
        ComponentField classNameField = optimizerContext.getComponentFields().get("optimizationClassName");
        if (classNameField == null)
            return false;
        
        String className = classNameField.getValue();
        if (className == null)
            return false;
        
        Optimizer optimizer = OptimizerLibrary.getOptimizer(className);
        if (optimizer == null) 
            return false;
        this.setOptimizerDescription(optimizer.getDescription());
        
        ComponentField setupField = optimizerContext.getComponentFields().get("parameterization");
        if (setupField!=null && setupField.getValue()!=null){
            String setupList[] = setupField.getValue().split(";");
            
            for (String parameterValuePair : setupList) {
                String entry[] = parameterValuePair.split("=");
                String name = entry[0];
                String value = entry[1];

                if (entry.length != 2) {
                    continue;
                }
                OptimizerParameter op = getOptimizerDescription().getPropertyMap().get(name);
                if (op != null) {
                    op.setString(value);
                }
            }
        }
        ComponentField parameterIDfield = optimizerContext.getComponentFields().get("parameterIDs");
        ComponentField rangesField = optimizerContext.getComponentFields().get("boundaries");
        ComponentField startValuesField = optimizerContext.getComponentFields().get("startValues");
        
        if (parameterIDfield == null || rangesField == null)
            return false;
        
        if (rangesField != null      && rangesField.getValue() != null) {                           
            String parameterIDs[] = parameterIDfield.getAttribute().split(";");
            String ranges[] = rangesField.getValue().split(";");
            String startvalues[];

            int n = parameterIDs.length;
            if (ranges.length != n) {
                return false;
            }

            if (startValuesField == null) {
                startvalues = new String[n];
            } else {
                startvalues = startValuesField.getValue().split(";");
            }
            for (int i = 0; i < n; i++) {
                String parameterID = parameterIDs[i];
                String rangeString = ranges[i];
                String startvalueString = startvalues[i];
                ContextAttribute ca = optimizerContext.getDynamicAttributes().get(parameterID);
                if (ca == null) {
                    return false;
                }
                Range range = null;
                try {
                    range = new Range(rangeString);
                } catch (NumberFormatException nfe) {
                    range = new Range(0, 1);
                }
                Parameter2 parameter = new Parameter2(ca, range);
                try {
                    if (startvalueString != null) {
                        parameter.setStartValue(startvalueString);
                    }
                } catch (NumberFormatException nfe) {
                }
                this.addParameter(parameter);
            }
        }
        ComponentField objectiveField = optimizerContext.getComponentFields().get("effValue");
        if (objectiveField == null){
            return false;
        }
        
        if (objectiveField.getAttribute() != null) {
            String objectiveIDs[] = objectiveField.getAttribute().split(";");
            int m = objectiveIDs.length;
            for (int i = 0; i < m; i++) {
                String objectiveID = objectiveIDs[i];
                ContextAttribute ca = optimizerContext.getDynamicAttributes().get(objectiveID);
                addObjective(ca);
            }
        }
        return true;
    }
}
