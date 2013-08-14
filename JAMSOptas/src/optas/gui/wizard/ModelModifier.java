/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.wizard;

import jams.JAMS;
import jams.JAMSException;
import jams.data.Attribute;
import jams.data.Attribute.TimeInterval;
import jams.meta.ComponentDescriptor;
import jams.meta.ComponentField;
import jams.meta.ContextAttribute;
import jams.meta.ContextDescriptor;
import jams.meta.ModelDescriptor;
import jams.meta.ModelNode;
import jams.meta.OutputDSDescriptor;
import jams.model.GUIComponent;
import jams.model.JAMSContext;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.tree.TreeNode;
import optas.efficiencies.UniversalEfficiencyCalculator;
import optas.optimizer.OptimizerLibrary;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.management.OptimizerParameter;
import optas.optimizer.management.SimpleOptimizationController;

/**
 *
 * @author christian
 */
public class ModelModifier {

    private ModelDescriptor md;
    static final String OPTIMIZER_CONTEXT_NAME = "optimizer";
    static final String OBJECTIVE_COMPONENT_NAME = "objective";

    public ModelModifier(ModelDescriptor md) {
        this.md = md;
    }

    private ContextDescriptor createStandardOptimizerComponent() {
        //optimierer bauen
        try {
            ContextDescriptor cd = new ContextDescriptor(OPTIMIZER_CONTEXT_NAME, SimpleOptimizationController.class);
            cd.setComponentAttribute_("optimizationClassName", OptimizerLibrary.getDefaultOptimizer().getClass().getCanonicalName());
            //cd.setComponentAttribute_("maxn", Integer.toString(Integer.MAX_VALUE));
            return cd;
        } catch (ComponentDescriptor.NullClassException nce) {
            return null;
        }
    }

    public void setOptimizerDescription(OptimizerDescription od) {
        ComponentDescriptor optimizerContext = md.getComponentDescriptor(OPTIMIZER_CONTEXT_NAME);
        optimizerContext.setComponentAttribute_("optimizationClassName", od.getOptimizerClassName());
        //optimizerContext.setComponentAttribute_("maxn", Integer.toString(Integer.MAX_VALUE));
        optimizerContext.setComponentAttribute_("parameterization", getOptimizerParameterString(od));
    }

    public ModelDescriptor getModelDescriptor() {
        return md;
    }

    private String getOptimizerParameterString(OptimizerDescription opt) {
        String parameter = "";
        for (OptimizerParameter p : opt.getPropertyMap().values()) {
            parameter += p.getString() + ";";
        }
        return parameter;
    }

    public boolean updateObjectiveList(Set<Objective> oList){
        TreeSet<Objective> list = new TreeSet<Objective>();
        for (Objective o : oList){
            list.add(o);
        }
        oList = list;
        
        ContextDescriptor optimizerContext = (ContextDescriptor) md.getComponentDescriptor(OPTIMIZER_CONTEXT_NAME);
        try {            
            for (Objective o : oList) {
                if (o.field instanceof ComponentField) {
                    ComponentField cf = (ComponentField) o.field;
                    String attributeName = cf.getParent().getInstanceName() + "_" + cf.getName();

                    optimizerContext.addDynamicAttribute(attributeName, Attribute.Double.class);

                    if (cf.getContextAttributes().size() != 0) {
                        for (ContextAttribute ca : cf.getContextAttributes()) { 
                            //copy to avoid interactions with the iterator .. 
                            HashSet<ComponentField> cpySet = (HashSet<ComponentField>)ca.getFields().clone();
                            for (ComponentField field : cpySet) {
                                field.unlinkFromAttribute(ca);
                                field.linkToAttribute(optimizerContext, attributeName);
                            }
                        }
                    }
                    
                    cf.linkToAttribute(optimizerContext, attributeName);
                    cf.setValue("");    
                    o.field = optimizerContext.getDynamicAttributes().get(attributeName);
                }
                if (o.field instanceof ContextAttribute) {
                    ContextAttribute ca = (ContextAttribute) o.field;
                    if (ca.getContext() == optimizerContext)
                        continue;
                    String attributeName = ca.getContext().getInstanceName() + "_" + ca.getName();
                    
                    optimizerContext.addDynamicAttribute(attributeName, Attribute.Double.class);
                    for (ComponentField field : ca.getFields()){
                        field.unlinkFromAttribute(ca);
                        field.linkToAttribute(optimizerContext, attributeName);
                    }
                    ca.getContext().removeStaticAttribute(ca.getName());
                    o.field = optimizerContext.getDynamicAttributes().get(attributeName);
                }
            }
        } catch (JAMSException ale) {
            ale.printStackTrace();
        }
        String objectiveNames = "";
        
        ComponentField cf = optimizerContext.getComponentFields().get("effValue");
        cf.unlinkFromAttribute();
        for (Objective o : oList) {
            if (o.field instanceof ContextAttribute) {
                ContextAttribute ca = (ContextAttribute) o.field;
                objectiveNames += ca.getName() + ";";
                
                try {
                    cf.linkToAttribute(ca.getContext(), ca.getName(), false);
                } catch (JAMSException ale) {
                    ale.printStackTrace();
                    return false;
                }
            }
        }
        optimizerContext.setComponentAttribute_("effMethodName", objectiveNames);        
        return true;
    }
    
    public boolean updateParameterList(Set<Parameter> pList) {
        ContextDescriptor optimizerContext = (ContextDescriptor) md.getComponentDescriptor(OPTIMIZER_CONTEXT_NAME);
        try {
            //iterate through list and remove all component parameters
            for (Parameter p : pList) {
                if (p.field instanceof ComponentField) {
                    ComponentField cf = (ComponentField) p.field;
                    String attributeName = cf.getParent().getInstanceName() + "_" + cf.getName();

                    optimizerContext.addStaticAttribute(attributeName, Attribute.Double.class, cf.getValue());

                    cf.linkToAttribute(optimizerContext, attributeName);
                    cf.setValue("");    
                    p.field = optimizerContext.getStaticAttributes().get(attributeName);
                }
                if (p.field instanceof ContextAttribute) {
                    ContextAttribute ca = (ContextAttribute) p.field;
                    if (ca.getContext() == optimizerContext)
                        continue;
                    String attributeName = ca.getContext().getInstanceName() + "_" + ca.getName();
                    
                    optimizerContext.addStaticAttribute(attributeName, Attribute.Double.class, ca.getValue());
                    
                    for (ComponentField field : ca.getFields()){
                        field.unlinkFromAttribute(ca);
                        field.linkToAttribute(optimizerContext, attributeName);
                    }
                    for (ComponentDescriptor cd : ca.getContext().getComponentRepository().getComponentDescriptors().values()){
                        for (ComponentField cf : cd.getComponentFields().values()){
                            if ( cf.getContext() != null && cf.getContext().equals(ca.getContext()) ){
                                if (cf.getAttribute().equals(ca.getName())){
                                    cf.linkToAttribute(optimizerContext, attributeName,true);
                                }
                            }
                        }
                    }
                    ca.getContext().removeStaticAttribute(ca.getName());
                    p.field = optimizerContext.getStaticAttributes().get(attributeName);
                }
            }
        } catch (JAMSException ale) {
            ale.printStackTrace();
        }
        String parameterNames = "";
        String boundaries = "";
        String startvalues = "";
        ComponentField cf = optimizerContext.getComponentFields().get("parameterIDs");
        cf.unlinkFromAttribute();
        for (Parameter p : pList) {
            if (p.field instanceof ContextAttribute) {
                ContextAttribute ca = (ContextAttribute) p.field;
                parameterNames += ca.getName() + ";";
                boundaries += "[" + p.getLowerBound() + ">" + p.getUpperBound() + "];";
                String startvalue = null;
                if (p.getStartValue() != null) {
                    startvalue = p.getStartValue().toString();
                }
                if (startvalue != null && startvalues != null) {
                    startvalues += "[" + startvalue + "];";
                } else {
                    startvalues = null;
                }
                try {
                    cf.linkToAttribute(ca.getContext(), ca.getName(), false);
                } catch (JAMSException ale) {
                    ale.printStackTrace();
                    return false;
                }
            }
        }
        optimizerContext.setComponentAttribute_("parameterNames", parameterNames);
        optimizerContext.setComponentAttribute_("boundaries", boundaries);
        if (startvalues == null) {
            optimizerContext.setComponentAttribute_("startvalue", "");
        } else {
            optimizerContext.setComponentAttribute_("startvalue", startvalues);
        }
        return true;
    }

    public void updateObjectiveCalculators(ObjectiveDescription[] odList) throws OPTASWizardException{
        for (ObjectiveDescription od : odList){
            if (od == null)
                continue;
            //first have a look if it is allready 
            String name = od.getName();
            
            ComponentDescriptor measurementContext = md.getComponentDescriptor(od.getMeasurementAttribute().getParentName());
            ComponentDescriptor simulationContext = md.getComponentDescriptor(od.getSimulationAttribute().getParentName());
            
            if (simulationContext != measurementContext){
                throw new OPTASWizardException(JAMS.i18n("Error_during_objective_configuration_context_of_measurement_attribute_does_not_fit_context_of_simulation_attribute(%1 vs. %2)")
                        .replace("%1",measurementContext.getInstanceName()).replace("%2",simulationContext.getInstanceName()));
            }
            if (!(measurementContext instanceof ContextDescriptor)){
                throw new OPTASWizardException(JAMS.i18n("Error_during_objective_configuration:Componente_%1_is_not_a_context!").replace("%1", measurementContext.getInstanceName()));
            }
            ContextDescriptor context = (ContextDescriptor)measurementContext;
            ContextDescriptor modelContext = (ContextDescriptor)md.getComponentDescriptor(md.getModelName());

            ComponentDescriptor cd = md.getComponentDescriptor(name);
            //objective calculator allready available
            boolean allreadyExisting = false;

            if (cd != null){
                //check if this context can be used
                if (UniversalEfficiencyCalculator.class.isAssignableFrom(cd.getClazz())){
                    if (context.getComponentRepository().getComponentDescriptors().containsValue(cd)){
                        allreadyExisting = true;
                        HashMap<String,ComponentField> fields = cd.getComponentFields();
                        fields.get("measurementAttributeName").setValue("measurement");
                        fields.get("measurement").linkToAttribute(context, od.getMeasurementAttribute().getAttributeName(), true);
                        
                        fields.get("simulationAttributeName").setValue("simulation");
                        fields.get("simulation").linkToAttribute(context, od.getSimulationAttribute().getAttributeName(), true);
                        
                        if (fields.get("e1").getContextAttributes().isEmpty()){
                            fields.get("e1").linkToAttribute(modelContext, name + "_e1");
                        }
                        if (fields.get("e2").getContextAttributes().isEmpty()){
                            fields.get("e2").linkToAttribute(modelContext, name + "_e2");
                        }
                        if (fields.get("le1").getContextAttributes().isEmpty()){
                            fields.get("le1").linkToAttribute(modelContext, name + "_e1");
                        }
                        if (fields.get("le2").getContextAttributes().isEmpty()){
                            fields.get("le2").linkToAttribute(modelContext, name + "_e2");
                        } 
                        if (fields.get("ave").getContextAttributes().isEmpty()){
                            fields.get("ave").linkToAttribute(modelContext, name + "_ave");
                        }
                        if (fields.get("r2").getContextAttributes().isEmpty()){
                            fields.get("r2").linkToAttribute(modelContext, name + "_r2");
                        }
                        if (fields.get("bias").getContextAttributes().isEmpty()){
                            fields.get("bias").linkToAttribute(modelContext, name + "_bias");
                        }
                        
                        if (fields.get("e1_normalized").getContextAttributes().isEmpty()){
                            fields.get("e1_normalized").linkToAttribute(modelContext, name + "_e1_normalized");
                        }
                        if (fields.get("e2_normalized").getContextAttributes().isEmpty()){
                            fields.get("e2_normalized").linkToAttribute(modelContext, name + "_e2_normalized");
                        }
                        if (fields.get("le1_normalized").getContextAttributes().isEmpty()){
                            fields.get("le1_normalized").linkToAttribute(modelContext, name + "_le1_normalized");
                        }
                        if (fields.get("le2_normalized").getContextAttributes().isEmpty()){
                            fields.get("le2_normalized").linkToAttribute(modelContext, name + "_le2_normalized");
                        } 
                        if (fields.get("ave_normalized").getContextAttributes().isEmpty()){
                            fields.get("ave_normalized").linkToAttribute(modelContext, name + "_ave_normalized");
                        }
                        if (fields.get("r2_normalized").getContextAttributes().isEmpty()){
                            fields.get("r2_normalized").linkToAttribute(modelContext, name + "_r2_normalized");
                        }
                        if (fields.get("bias_normalized").getContextAttributes().isEmpty()){
                            fields.get("bias_normalized").linkToAttribute(modelContext, name + "_bias_normalized");
                        }
                        //apply filters if there are any
                        if (od.getTimeFilters().get().size() > 0) {
                            String timeIntervalString = "";
                            TimeInterval timeIntervals[] = od.getTimeFilters().toTimeIntervals(od.getModelTimeInterval());
                            for (TimeInterval I : timeIntervals) {
                                timeIntervalString += I.toString() + ";";
                            }
                            fields.get("timeInterval").setValue(timeIntervalString);
                        }else{
                            //otherwise it is not necessary to set any interval.
                            //then the whole period is used
                        }
                        if (od.getTimeAttribute() != null){
                            ComponentDescriptor timeComponent = md.getComponentDescriptor(od.getTimeAttribute().getParentName());
                            if (timeComponent == null || !(timeComponent instanceof ContextDescriptor)){
                                throw new OPTASWizardException(JAMS.i18n("Error_during_objective_configuration:Componente_%1_is_not_a_context!").replace("%1", timeComponent.getInstanceName()));
                            }
                            ContextDescriptor timeContext = (ContextDescriptor)timeComponent;
                            String timeAttributeName = od.getTimeAttribute().getAttributeName();
                            if (!timeContext.getComponentFields().containsKey(timeAttributeName)){
                                throw new OPTASWizardException(JAMS.i18n("Error_during_objective_configuration:Context_%1_does_not_contain_time_attribute!").replace("%1", timeContext.getInstanceName()));
                            }
                            fields.get("time").linkToAttribute(timeContext, timeAttributeName, true);
                        }
                    }
                }
            }
            //create a new component with the required attributes
            if (!allreadyExisting) {
                String originalName = name;
                int counter = 1;
                while (md.getComponentDescriptor(name)!=null){
                    name = originalName + counter++;
                }
                od.setName(name);
                ComponentDescriptor objectiveComponent = new ComponentDescriptor(name, UniversalEfficiencyCalculator.class);
                
                HashMap<String, ComponentField> fields = objectiveComponent.getComponentFields();
                fields.get("measurementAttributeName").setValue("measurement");
                fields.get("measurement").linkToAttribute(context, od.getMeasurementAttribute().getAttributeName(), true);

                fields.get("simulationAttributeName").setValue("simulation");
                fields.get("simulation").linkToAttribute(context, od.getSimulationAttribute().getAttributeName(), true);

                fields.get("e1").linkToAttribute(modelContext, name + "_e1");
                fields.get("e2").linkToAttribute(modelContext, name + "_e2");
                fields.get("le1").linkToAttribute(modelContext, name + "_le1");
                fields.get("le2").linkToAttribute(modelContext, name + "_le2");
                fields.get("ave").linkToAttribute(modelContext, name + "_ave");
                fields.get("r2").linkToAttribute(modelContext, name + "_r2");
                fields.get("bias").linkToAttribute(modelContext, name + "_bias");
                fields.get("e1_normalized").linkToAttribute(modelContext, name + "_e1_normalized");
                fields.get("e2_normalized").linkToAttribute(modelContext, name + "_e2_normalized");
                fields.get("le1_normalized").linkToAttribute(modelContext, name + "_le1_normalized");
                fields.get("le2_normalized").linkToAttribute(modelContext, name + "_le2_normalized");
                fields.get("ave_normalized").linkToAttribute(modelContext, name + "_ave_normalized");
                fields.get("r2_normalized").linkToAttribute(modelContext, name + "_r2_normalized");
                fields.get("bias_normalized").linkToAttribute(modelContext, name + "_bias_normalized");
                                
                //apply filters if there are any
                if (od.getTimeFilters().get().size() > 0) {
                    String timeIntervalString = "";
                    TimeInterval timeIntervals[] = od.getTimeFilters().toTimeIntervals(od.getModelTimeInterval());
                    for (TimeInterval I : timeIntervals) {
                        timeIntervalString += I.toString() + ";";
                    }
                    fields.get("timeInterval").setValue(timeIntervalString);
                } else {
                    //otherwise it is not necessary to set any interval.
                    //then the whole period is used
                }
                if (od.getTimeAttribute() != null) {
                    ComponentDescriptor timeComponent = md.getComponentDescriptor(od.getTimeAttribute().getParentName());
                    if (timeComponent == null || !(timeComponent instanceof ContextDescriptor)) {
                        throw new OPTASWizardException(JAMS.i18n("Error_during_objective_configuration:Componente_%1_is_not_a_context!").replace("%1", timeComponent.getInstanceName()));
                    }
                    ContextDescriptor timeContext = (ContextDescriptor) timeComponent;
                    String timeAttributeName = od.getTimeAttribute().getAttributeName();
                    if (!timeContext.getComponentFields().containsKey(timeAttributeName)) {
                        throw new OPTASWizardException(JAMS.i18n("Error_during_objective_configuration:Context_%1_does_not_contain_time_attribute!").replace("%1", timeContext.getInstanceName()));
                    }
                    fields.get("time").linkToAttribute(timeContext, timeAttributeName, true);
                }
                ModelNode objectiveNode = new ModelNode(objectiveComponent);
                objectiveNode.setType(ModelNode.COMPONENT_TYPE);
                context.getNode().insert(objectiveNode, context.getNode().getChildCount()-1);
            }
        }
    }
    
    public OptimizerDescription addOptimizationContext() {
        try {
            ModelNode rootNode = md.getRootNode();

            ContextDescriptor optimizerContext = createStandardOptimizerComponent();
            ModelNode optimizerNode = new ModelNode(optimizerContext);
            optimizerNode.setType(ModelNode.CONTEXT_TYPE);

            ModelNode contextNode = rootNode;//.clone(new ModelDescriptor(), true, new HashMap<ContextDescriptor, ContextDescriptor>());
            optimizerNode.insert(contextNode, 0);
            //rootNode.removeAllChildren();

            ContextDescriptor containerContext = new ContextDescriptor("container", JAMSContext.class);
            ModelNode containerNode = new ModelNode(containerContext);
            containerNode.setType(ModelNode.CONTEXT_TYPE);
            containerNode.insert(optimizerNode, 0);
            md.setRootNode(containerNode);
            md.registerComponentDescriptor(OPTIMIZER_CONTEXT_NAME, OPTIMIZER_CONTEXT_NAME, optimizerContext);
            md.registerComponentDescriptor("container", "container", containerContext);
        } catch (ComponentDescriptor.NullClassException nce) {
            nce.printStackTrace();
            return null;
        }

        return OptimizerLibrary.getDefaultOptimizer().getDescription();
    }
    
    private boolean configOutput(ArrayList<optas.gui.wizard.Attribute> attributeList) {
        OutputDSDescriptor ds = md.getDatastores().get(OPTIMIZER_CONTEXT_NAME);
        if (ds == null){
            ds = new OutputDSDescriptor((ContextDescriptor) md.getComponentDescriptor(OPTIMIZER_CONTEXT_NAME));
            ds.setName(OPTIMIZER_CONTEXT_NAME);
            md.addOutputDataStore(ds);
        }
        ArrayList<ContextAttribute> outputDSList = ds.getContextAttributes();
        
        for (optas.gui.wizard.Attribute a : attributeList){
            if (a.field instanceof ContextAttribute ){
                ContextAttribute ca = (ContextAttribute)a.field;
                boolean containsAttribute = false;
                for (ContextAttribute outputCa : outputDSList){
                    if (outputCa.getName().equals(ca.getName())){
                        containsAttribute = true;
                        break;
                    }
                }
                if (!containsAttribute){
                    ds.getContextAttributes().add(ca);
                }
            }
        }
        ds.setEnabled(true);
        
        return true;
    }
    
    private boolean disableGUIComponents(){
        Enumeration<TreeNode> nodes = md.getRootNode().breadthFirstEnumeration();
        while(nodes.hasMoreElements()){
            ComponentDescriptor cd = (ComponentDescriptor) ((ModelNode) nodes.nextElement()).getUserObject();
            if (GUIComponent.class.isAssignableFrom(cd.getClazz())){
                cd.setEnabled(false);
            }
        }
        return true;
    }
    //configure datastores
    //disable gui components    
    public void finish(ArrayList<optas.gui.wizard.Attribute> exportAttributes){
        configOutput(exportAttributes);
        disableGUIComponents();
    }
}
