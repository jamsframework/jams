/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.wizard;

import jams.JAMS;
import jams.meta.ComponentField;
import jams.meta.ContextAttribute;
import jams.meta.ContextDescriptor;
import jams.meta.ModelDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import optas.optimizer.Optimizer;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.OptimizerLibrary;
import optas.optimizer.management.OptimizerParameter;
import optas.optimizer.management.SimpleOptimizationController;
import java.util.logging.Level;
import java.util.logging.Logger;
import optas.gui.wizard.Parameter.Range;
import optas.optimizer.SCE;
/**
 *
 * @author chris
 */
public class Optimization implements Serializable {
    transient private ModelModifier modifier;    
    transient private ModelAnalyzer analyser;
    
    private OptimizerDescription optimizerDescription;
    private Set<Parameter> parameter = new TreeSet<Parameter>();
    private Set<Objective> objective = new TreeSet<Objective>();
    
    private String name;
        
    public Optimization(ModelDescriptor md) throws OPTASWizardException{
        if (md != null){
            modifier = new ModelModifier(md);
            analyser = new ModelAnalyzer(md);
            if (!importFromModelDescriptor(md)){
                optimizerDescription = modifier.addOptimizationContext();            
            }
        }else{
            optimizerDescription = new OptimizerDescription();
        }       
    }
       
    public void update(){
        this.modifier.updateObjectiveList(objective);
        this.modifier.updateParameterList(parameter);
    }
    
    public void finish(){
        ArrayList<Attribute> exportAttributes = new ArrayList<Attribute>();
        exportAttributes.addAll(parameter);
        exportAttributes.addAll(objective);
        this.modifier.finish(exportAttributes);
    }
    
    public ModelDescriptor getModelDescriptor(){
        return modifier.getModelDescriptor();
    }
    
    public boolean addParameter(Parameter p){
        getParameter().add(p);
        if (modifier == null)
            return true;
        
        if (modifier.updateParameterList(getParameter())){
            //do twice to tell the treeset the update
            getParameter().add(p);   
            return true;
        }        
                 
        return false;
    }

    public boolean removeParameter(Parameter p){
        getParameter().remove(p);
        if (modifier == null)
            return true;
        
        if (modifier.updateParameterList(getParameter())){
            return true;
        }
        return false;
    }
    
    public boolean removeObjective(Objective c){
        getObjective().remove(c);
        if (modifier == null)
            return true;
        
        if (modifier.updateObjectiveList(getObjective())){
            return true;
        }
        return false;
    }
    
    public boolean addObjective(Objective c){
        getObjective().add(c);
        if (modifier == null)
            return true;
        if (modifier.updateObjectiveList(getObjective())){
            getObjective().add(c);
            return true;
        }
        return false;
    }
    
    public Set<Parameter> getModelParameters(){
        Set<Parameter> allParameters = analyser.getParameters();
        allParameters.addAll(parameter);
        return allParameters;
    }
    
    public Set<Objective> getModelObjectives(){
        Set<Objective> allObjectives = analyser.getObjectives();
        allObjectives.addAll(objective);
        return allObjectives;
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
        if (modifier != null)            
            this.modifier.setOptimizerDescription(optimizerDescription);
        
        this.optimizerDescription = optimizerDescription;        
    }

    /**
     * @return the parameter
     */
    public Set<Parameter> getParameter() {
        return parameter;
    }

    /**
     * @param parameter the parameter to set
     */
    public void setParameter(Set<Parameter> parameter) {
        this.parameter = parameter;
    }

    /**
     * @return the objective
     */
    public Set<Objective> getObjective() {
        return objective;
    }

    /**
     * @param objective the objective to set
     */
    public void setObjective(Set<Objective> objective) {
        this.objective = new TreeSet<Objective>();
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
    
    private boolean importFromModelDescriptor(ModelDescriptor md) throws OPTASWizardException{           
        ContextDescriptor optimizerContext = null;
        try{
            optimizerContext = (ContextDescriptor)md.getComponentDescriptor("optimizer");
        }catch(ClassCastException cce){
            return false;
        }
        if (optimizerContext == null)
            return false;
        if (!optimizerContext.getClazz().isAssignableFrom(SimpleOptimizationController.class)){
            Logger.getGlobal().log(Level.WARNING, JAMS.i18n("The_model_allready_contains_an_optimization_controller_which_is_not_compatible_with_SimpleOptimizationController!"));
            return false;
        }
        
        ComponentField classNameField = optimizerContext.getComponentFields().get("optimizationClassName");
        if (classNameField == null){
            Logger.getGlobal().log(Level.WARNING, JAMS.i18n("There_is_allready_an_optimization_component_inside_the_model._But_the_optimizer_class_field_is_not_existing."));
            return false;
        }
        String className = classNameField.getValue();
        if (className == null){
            Logger.getGlobal().log(Level.WARNING, JAMS.i18n("There_is_allready_an_optimization_component_in_the_model._But_the_optimizer_class_was_not_set._Using_SCE_instead!"));
            className = SCE.class.getCanonicalName();
        }
        Optimizer optimizer = OptimizerLibrary.getOptimizer(className);
        if (optimizer == null){
            String errorMsg = JAMS.i18n("Unable_to_load_optimization_component") + className + JAMS.i18n(".Please_check_your_optimizer_configuration!");
            Logger.getGlobal().log(Level.WARNING, errorMsg);                        
            throw new OPTASWizardException(errorMsg);
        }
        OptimizerDescription desc = optimizer.getDescription();
                
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
                OptimizerParameter op = desc.getPropertyMap().get(name);
                if (op != null) {
                    op.setString(value);
                }
            }
        }
        this.setOptimizerDescription(desc);
        ComponentField parameterIDfield = optimizerContext.getComponentFields().get("parameterIDs");
        ComponentField rangesField = optimizerContext.getComponentFields().get("boundaries");
        ComponentField startValuesField = optimizerContext.getComponentFields().get("startValue");
        
        if (parameterIDfield == null || rangesField == null){
            Logger.getGlobal().log(Level.WARNING, JAMS.i18n("The_optimization_controller_does_not_contain_either_parameter_ranges_or_parameter_ids!"));
            return false;
        }
        
        if (rangesField != null && rangesField.getValue() != null) {                           
            String parameterIDs[] = parameterIDfield.getAttribute().split(";");
            String ranges[] = rangesField.getValue().split(";");
            String startvalues[];

            int n = parameterIDs.length;
            if (ranges.length != n) {
                Logger.getGlobal().log(Level.SEVERE, JAMS.i18n("Error:_Number_of_parameters_is_not_equal_to_number_of_boundaries._Setting_default_range_of_0_1_for_missing_parameters!"));
                if (ranges.length < n){
                    int k = ranges.length;
                    ranges = Arrays.copyOf(ranges, n);
                    for (int i=k;i<n;i++){
                        ranges[i] = "[0<1];";
                    }
                }else{
                    ranges = Arrays.copyOf(ranges, n);
                }
            }
            
            startvalues = new String[n];
            if (startValuesField != null) {
                int counter = 0;
                StringTokenizer tokStartValue = new StringTokenizer(startValuesField.getValue(), ";");
                ArrayList<double[]> x0List = new ArrayList<double[]>();
                while (tokStartValue.hasMoreTokens()) {
                    String param = tokStartValue.nextToken();
                    param = param.replace("[", "");
                    param = param.replace("]", "");
                    StringTokenizer subTokenizer = new StringTokenizer(param, ",");
                    double x0i[] = new double[subTokenizer.countTokens()];
                    int subCounter = 0;
                    if (x0i.length != n) {
                        Logger.getGlobal().log(Level.SEVERE, (JAMS.i18n("Component") + " " + JAMS.i18n("startvalue_too_many_parameter")));
                    }
                    while (subTokenizer.hasMoreTokens()) {
                        try {
                            x0i[subCounter] = Double.valueOf(subTokenizer.nextToken()).doubleValue();
                            subCounter++;
                        } catch (NumberFormatException e) {
                            Logger.getGlobal().log(Level.SEVERE, JAMS.i18n("unparseable_number") + param);
                        }
                    }
                    x0List.add(x0i);
                    counter++;
                }
                for (int i = 0; i < n; i++) {
                    if (x0List.size()>0 && x0List.get(0).length==n)
                        startvalues[i] = Double.toString(x0List.get(0)[i]);
                }
            }
            
            for (int i = 0; i < n; i++) {
                String parameterID = parameterIDs[i];
                String rangeString = ranges[i];
                String startvalueString = startvalues[i];
                ContextAttribute ca = optimizerContext.getStaticAttributes().get(parameterID);
                if (ca == null) {
                    ca = optimizerContext.getDynamicAttributes().get(parameterID);
                    optimizerContext.addStaticAttribute(parameterID, jams.data.Attribute.Double.class,"0.0");                    
                    if (ca == null){
                        Logger.getGlobal().log(Level.SEVERE, JAMS.i18n("parameter") + " " + parameterID + JAMS.i18n("_was_not_found_in_optimizerContext._Adding_parameter_and_setting_value_to_zero!"));                        
                        ca = optimizerContext.getStaticAttributes().get(parameterID); 
                    }else{
                        Logger.getGlobal().log(Level.SEVERE, JAMS.i18n("parameter") + " " + parameterID + JAMS.i18n("_is_not_static_in_optimizer_context._Adding_static_reference_and_setting_value_to_zero!"));
                    }
                }
                Range range = null;
                try {
                    range = new Range(rangeString);
                } catch (NumberFormatException nfe) {
                    range = new Range(0, 1);
                }
                //make sure we are using always the same objects
                Parameter parameter = new Parameter(ca, range);
                for (Parameter p : this.getModelParameters()){
                    if (p.compareTo(parameter)==0){
                        parameter = p;
                        parameter.setLowerBound(range.lowerBound);
                        parameter.setUpperBound(range.upperBound);
                        break;
                    }
                }                
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
            Logger.getGlobal().log(Level.SEVERE, JAMS.i18n("Optimization_controller_does_not_contain_an_effValue_field!"));
            return false;
        }
        
        if (objectiveField.getAttribute() != null && objectiveField.getAttribute().length()>1) {
            String objectiveIDs[] = objectiveField.getAttribute().split(";");
            int m = objectiveIDs.length;
            for (int i = 0; i < m; i++) {
                String objectiveID = objectiveIDs[i];                
                ContextAttribute ca = objectiveField.getContext().getDynamicAttributes().get(objectiveID);
                if (ca != null && ca.getContext().getInstanceName().compareTo("optimizer")!=0){
                    optimizerContext.addDynamicAttribute(objectiveID, jams.data.Attribute.Double.class);
                    //clone because otherwise linkToAttribute will interact with the iterator!
                    HashSet<ComponentField> cpySet = (HashSet<ComponentField>)ca.getFields().clone();
                    for (ComponentField cf : cpySet){
                        cf.linkToAttribute(optimizerContext, objectiveID, true);
                    }
                    ca = optimizerContext.getDynamicAttributes().get(objectiveID);
                }
                else if (ca == null){
                    optimizerContext.addDynamicAttribute(objectiveID, jams.data.Attribute.Double.class);
                    ca = optimizerContext.getDynamicAttributes().get(objectiveID);
                }
                Objective obj = new Objective(ca);
                for (Objective o : this.getModelObjectives()){
                    if (o.compareTo(obj)==0){
                        obj = o;
                        break;
                    }
                }
                this.addObjective(obj);
            }
        }
        return true;
    }
}
