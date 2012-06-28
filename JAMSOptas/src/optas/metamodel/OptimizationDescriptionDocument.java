/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.metamodel;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author chris
 */
public class OptimizationDescriptionDocument implements Serializable{

    public static String VERSION = "1.0";

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private ArrayList<optas.metamodel.AttributeWrapper> output = new ArrayList<optas.metamodel.AttributeWrapper>();
    
    private ArrayList<Optimization> optimization = new ArrayList<Optimization>();
    
    private String workspace="";
    private boolean removeRedundantComponents=true;
    private boolean removeGUIComponents=true;
    private boolean adjustModellTimeInterval=false;
    private boolean multimodeSearch=false;

    private String version;

    public OptimizationDescriptionDocument(){
    }

    public void clear(){
        getParameter().clear();
        getObjective().clear();
        getOptimization().clear();
    }

    public void addParameter(optas.metamodel.Parameter p) {
        getParameter().put(p.getId(),p);
    }

    public void addObjective(optas.metamodel.Objective p) {
        getObjective().put(p.getId(),p);
    }

    public void addOptimization(Optimization o) {
        if (!optimization.contains(o))
            getOptimization().add(o);

        for (Objective c : o.getObjective()) {
            if (getObjective().get(c.getId()) == null) {
                getObjective().put(c.getId(), c);
            }
        }
        for (optas.metamodel.Parameter p : o.getParameter()) {
            addParameter(p);
        }
    }

    private AttributeWrapper resolveName(SortedSet<AttributeWrapper> list, AttributeWrapper needle) {
        Iterator<AttributeWrapper> iter = list.iterator();
        
        while (iter.hasNext()) {
            AttributeWrapper attribute = iter.next();
            if (attribute.getChildName()!=null &&
                    attribute.getChildName().equals(needle.getChildName()))
                return attribute;
        }
        System.out.println("error there is no matching for parameter " + needle);
        return null;
    }

    public boolean repair(Document model, SortedSet<AttributeWrapper> parameterList, SortedSet<AttributeWrapper> objectiveList){
        this.getParameter().clear();
        this.getObjective().clear();

        for (Optimization o : optimization) {            
            for (Parameter p : o.getParameter()) //resolve names, if required
            {
                if (p.getComponentName() == null && p.getContextName() == null) {
                    Parameter p_new = (Parameter)resolveName(parameterList, p);
                    if (p_new == null){
                        return false;
                    }
                    p.setAttributeName(p_new.getAttributeName());
                    p.setVariableName(p_new.getVariableName());
                    p.setComponentName(p_new.getComponentName());
                    p.setContextName(p_new.getContextName());
                    addParameter(p);
                }
            }            
                        
            for (Objective obj : o.getObjective()) //resolve names, if required
            {
                if (obj.getMeasurement()==null){
                    obj.setMeasurement(objectiveList.first());
                }
                if (obj.getSimulation()==null){
                    obj.setSimulation(objectiveList.first());
                }
                if (obj.getMeasurement().getParentName() == null) {
                    AttributeWrapper measurement_new = (AttributeWrapper)resolveName(objectiveList, obj.getMeasurement());
                    if (measurement_new == null)
                        return false;
                    obj.setMeasurement(measurement_new);
                }
                if (obj.getSimulation().getParentName() == null) {
                    AttributeWrapper simulation_new = (AttributeWrapper)resolveName(objectiveList, obj.getSimulation());
                    if (simulation_new == null)
                        return false;
                    obj.setSimulation(simulation_new);
                    
                }
                addObjective(obj);
            }                 
        }
//        this.setWorkspace(Tools.getWorkspace((Node)model));
        return true;
    }

    /**
     * @return the parameter
     */
    public SortedMap<Integer, optas.metamodel.Parameter> getParameter() {
        SortedMap<Integer, optas.metamodel.Parameter> parameter = new TreeMap<Integer, Parameter>();

        for (Optimization o : this.getOptimization()){
            for (Parameter obj : o.getParameter())
                parameter.put(obj.getId(), obj);
        }

        return parameter;
    }

    /**
     * @param parameterDesc the parameter to set
     */
    public void setParameter(SortedMap<Integer, optas.metamodel.Parameter> parameter) {
        //this.parameter = parameter;
    }

    /**
     * @return the objectiveDesc
     */
    public SortedMap<Integer, optas.metamodel.Objective> getObjective() {
        SortedMap<Integer, optas.metamodel.Objective> objective = new TreeMap<Integer, optas.metamodel.Objective>();

        for (Optimization o : this.getOptimization()){
            for (Objective obj : o.getObjective())
                objective.put(obj.getId(), obj);
        }

        return objective;
    }
    
    /**
     * @return the optimizationDesc
     */
    public ArrayList<Optimization> getOptimization() {
        return optimization;
    }

    /**
     * @param optimizationDesc the optimizationDesc to set
     */
    public void setOptimization(ArrayList<Optimization> optimization) {
        this.optimization = optimization;
    }

    /**
     * @return the workspace
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * @param workspace the workspace to set
     */
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    /**
     * @return the removeRedundantComponents
     */
    public boolean isRemoveRedundantComponents() {
        return removeRedundantComponents;
    }

    /**
     * @param removeRedundantComponents the removeRedundantComponents to set
     */
    public void setRemoveRedundantComponents(boolean removeRedundantComponents) {
        this.removeRedundantComponents = removeRedundantComponents;
    }

    /**
     * @return the removeGUIComponents
     */
    public boolean isRemoveGUIComponents() {
        return removeGUIComponents;
    }

    /**
     * @param removeGUIComponents the removeGUIComponents to set
     */
    public void setRemoveGUIComponents(boolean removeGUIComponents) {
        this.removeGUIComponents = removeGUIComponents;
    }
    
    /**
     * @return the adjustModellTimeInterval
     */
    public boolean isAdjustModellTimeInterval() {
        return adjustModellTimeInterval;
    }

    /**
     * @param adjustModellTimeInterval the adjustModellTimeInterval to set
     */
    public void setAdjustModellTimeInterval(boolean adjustModellTimeInterval) {
        this.adjustModellTimeInterval = adjustModellTimeInterval;
    }

    /**
     * @return the output
     */
    public ArrayList<optas.metamodel.AttributeWrapper> getOutput() {
        return output;
    }

    /**
     * @param output the output to set
     */
    public void setOutput(ArrayList<optas.metamodel.AttributeWrapper> output) {
        this.output = output;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    public void setMultiModeSearch(boolean multimodeSearch){
        this.multimodeSearch = multimodeSearch;
    }

    public boolean getMultiModeSearch(){
        return this.multimodeSearch;
    }
}
