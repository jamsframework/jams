/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.optimizer.management;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author chris
 */
public class OptimizerDescription implements Serializable{
    private String shortName;
    private String optimizerClassName;
    private int id;
    private HashMap<String,OptimizerParameter> propertyMap = new HashMap<String,OptimizerParameter>();
    private boolean multiObjective = false;
    private BooleanOptimizerParameter doSpatialRelaxation = new BooleanOptimizerParameter("spatialRelaxation", "do spatial relaxation", false);
    private BooleanOptimizerParameter assessNonUniqueness = new BooleanOptimizerParameter("assessNonUniqueness", "assess non uniqueness", false);
    private BooleanOptimizerParameter localSearchDuringRelaxation = new BooleanOptimizerParameter("localSearchDuringRelaxation", "local search during relaxation", true);
    private BooleanOptimizerParameter adaptiveRelaxation = new BooleanOptimizerParameter("adaptiveRelacation", "adaptive relaxation", false);

    public OptimizerDescription(){
        this.shortName = "Optimization Configuration";
    }
    public OptimizerDescription(String shortName, int id) {
        this.shortName = shortName;
        this.id = id;
    }

    public OptimizerDescription(String shortName, int id, boolean multiObjective) {
        this.shortName = shortName;
        this.multiObjective = multiObjective;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setOptimizerClassName(String className) {
        this.optimizerClassName = className;
    }

    public String getOptimizerClassName() {
        return this.optimizerClassName;
    }

    public void addParameter(OptimizerParameter param) {
        getPropertyMap().put(param.getName(),param);
    }

    @Override
    public String toString() {
        return getShortName();
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param shortName the shortName to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the propertyMap
     */
    public HashMap<String,OptimizerParameter> getPropertyMap() {
        return propertyMap;
        
    }
    
    /**
     * @return the multiObjective
     */
    public boolean isMultiObjective() {
        return multiObjective;
    }

    /**
     * @param multiObjective the multiObjective to set
     */
    public void setMultiObjective(boolean multiObjective) {
        this.multiObjective = multiObjective;
    }

    /**
     * @param propertyMap the propertyMap to set
     */
    public void setPropertyMap(HashMap<String,OptimizerParameter> propertyMap) {
        this.propertyMap = propertyMap;
    }

    /**
     * @return the doSpatialRelaxation
     */
    public BooleanOptimizerParameter getDoSpatialRelaxation() {
        return doSpatialRelaxation;
    }

    /**
     * @param doSpatialRelaxation the doSpatialRelaxation to set
     */
    public void setDoSpatialRelaxation(BooleanOptimizerParameter doSpatialRelaxation) {
        this.doSpatialRelaxation = doSpatialRelaxation;
    }

    /**
     * @return the localSearchDuringRelaxation
     */
    public BooleanOptimizerParameter getLocalSearchDuringRelaxation() {
        return localSearchDuringRelaxation;
    }

    /**
     * @param localSearchDuringRelaxation the localSearchDuringRelaxation to set
     */
    public void setLocalSearchDuringRelaxation(BooleanOptimizerParameter localSearchDuringRelaxation) {
        this.localSearchDuringRelaxation = localSearchDuringRelaxation;
    }

    /**
     * @return the adaptiveRelacation
     */
    public BooleanOptimizerParameter getAdaptiveRelaxation() {
        return adaptiveRelaxation;
    }

    /**
     * @param adaptiveRelacation the adaptiveRelacation to set
     */
    public void setAdaptiveRelacation(BooleanOptimizerParameter adaptiveRelaxation) {
        this.adaptiveRelaxation = adaptiveRelaxation;
    }

    /**
     * @return the assessNonUniqueness
     */
    public BooleanOptimizerParameter getAssessNonUniqueness() {
        return assessNonUniqueness;
    }

    /**
     * @param assessNonUniqueness the assessNonUniqueness to set
     */
    public void setAssessNonUniqueness(BooleanOptimizerParameter assessNonUniqueness) {
        this.assessNonUniqueness = assessNonUniqueness;
    }
}
