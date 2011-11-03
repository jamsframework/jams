/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.metamodel;

import java.io.Serializable;
import java.util.ArrayList;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.OptimizerLibrary;

/**
 *
 * @author chris
 */
public class Optimization implements Serializable {
    private OptimizerDescription optimizerDescription;
    private ArrayList<optas.metamodel.Parameter> parameter = new ArrayList<optas.metamodel.Parameter>();
    private ArrayList<Objective> objective = new ArrayList<Objective>();
    
    private String name;

    public Optimization() {
        optimizerDescription = OptimizerLibrary.getDefaultOptimizer().getDescription();
        name = "unnamed";
    }

        
    public void addParameter(optas.metamodel.Parameter p){
        getParameter().add(p);
    }

    public void addObjective(Objective c){
        getObjective().add(c);
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
        this.optimizerDescription = optimizerDescription;
    }

    /**
     * @return the parameter
     */
    public ArrayList<optas.metamodel.Parameter> getParameter() {
        return parameter;
    }

    /**
     * @param parameter the parameter to set
     */
    public void setParameter(ArrayList<optas.metamodel.Parameter> parameter) {
        this.parameter = parameter;
    }

    /**
     * @return the objective
     */
    public ArrayList<Objective> getObjective() {
        return objective;
    }

    /**
     * @param objective the objective to set
     */
    public void setObjective(ArrayList<Objective> objective) {
        this.objective = objective;
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
}
