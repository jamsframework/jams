/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.wizard;

import jams.JAMSException;
import jams.data.Attribute;
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
                    if (outputCa.getName() == ca.getName()){
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
