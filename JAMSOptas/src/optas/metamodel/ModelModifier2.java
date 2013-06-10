/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.metamodel;

import jams.JAMSException;
import jams.data.Attribute;
import jams.meta.ComponentDescriptor;
import jams.meta.ComponentField;
import jams.meta.ContextAttribute;
import jams.meta.ContextDescriptor;
import jams.meta.ModelDescriptor;
import jams.meta.ModelNode;
import jams.model.JAMSContext;
import java.util.Set;
import optas.optimizer.OptimizerLibrary;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.management.OptimizerParameter;
import optas.optimizer.management.SimpleOptimizationController;

/**
 *
 * @author christian
 */
public class ModelModifier2 {

    private ModelDescriptor md;
    static final String OPTIMIZER_CONTEXT_NAME = "optimizer";
    static final String OBJECTIVE_COMPONENT_NAME = "objective";

    public ModelModifier2(ModelDescriptor md) {
        this.md = md;
    }

    private ContextDescriptor createStandardOptimizerComponent() {
        //optimierer bauen
        try {
            ContextDescriptor cd = new ContextDescriptor(OPTIMIZER_CONTEXT_NAME, SimpleOptimizationController.class);
            cd.setComponentAttribute_("optimizationClassName", OptimizerLibrary.getDefaultOptimizer().getClass().getCanonicalName());
            cd.setComponentAttribute_("maxn", Integer.toString(Integer.MAX_VALUE));
            return cd;
        } catch (ComponentDescriptor.NullClassException nce) {
            return null;
        }
    }

    public void setOptimizerDescription(OptimizerDescription od) {
        ComponentDescriptor optimizerContext = md.getComponentDescriptor(OPTIMIZER_CONTEXT_NAME);
        optimizerContext.setComponentAttribute_("optimizationClassName", od.getOptimizerClassName());
        optimizerContext.setComponentAttribute_("maxn", Integer.toString(Integer.MAX_VALUE));
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

    public boolean updateParameterList(Set<Parameter2> pList) {
        ContextDescriptor optimizerContext = (ContextDescriptor) md.getComponentDescriptor("optimizer");
        try {
            //iterate through list and remove all component parameters
            for (Parameter2 p : pList) {
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
        for (Parameter2 p : pList) {
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
}
