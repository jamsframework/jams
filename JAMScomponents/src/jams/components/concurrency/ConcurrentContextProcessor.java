/*
 * ConcurrentContextProcessor.java
 * Created on 28.01.2013, 15:28:19
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.concurrency;

import jams.JAMS;
import jams.JAMSException;
import jams.meta.ComponentDescriptor;
import jams.meta.ComponentField;
import jams.meta.ContextAttribute;
import jams.meta.ContextDescriptor;
import jams.meta.MetaProcessor;
import jams.meta.ModelDescriptor;
import jams.meta.ModelNode;
import jams.meta.OutputDSDescriptor;
import jams.runtime.JAMSRuntime;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ConcurrentContextProcessor implements MetaProcessor {

    private int nthreads = 1;
    private List<String> excludeComponents;
    private String partitionerClassName = "jams.components.concurrency.EntityPartitioner";
    private Map<String, String> partitionerParams = new HashMap();

    @Override
    public void setValue(String key, String value) {
        if (key.equals("scale_factor")) {
            nthreads = (int) Math.floor(java.lang.Runtime.getRuntime().availableProcessors() * Double.parseDouble(value));
        }

        if (key.equals("exclude_component")) {
            excludeComponents = Arrays.asList(value.split(";"));
        }

        if (key.equals("partitioner_class")) {
            partitionerClassName = value;
        }

        if (key.startsWith("partitioner_param_")) {
            String paramName = key.substring(18);
            partitionerParams.put(paramName, value);
        }
    }

    /**
     * This methods will modify the belonging context so that can be executed in
     * parallel. The context will be replaced by a special controller context
     * that contains numThread copies of the original context. The controller
     * context controls the parallel execution of the numThreads contexts.
     * Spatial contexts will be modified by adding a partitioner component which
     * allows to split entity collections for parallel processing
     */
    @Override
    public void process(ContextDescriptor context, ModelDescriptor model, final JAMSRuntime rt) {
              
        if (!context.isEnabled() || (nthreads < 2)
                || !jams.model.JAMSSpatialContext.class.isAssignableFrom(context.getClazz())) {
            rt.getLogger().info(MessageFormat.format(JAMS.i18n("No_concurrent_processing_for_context"), context.getInstanceName()));
            return;
        }

//        model.getLogger().setUseParentHandlers(false);

        // create controller context 
        ContextDescriptor controller = new ContextDescriptor(ConcurrentContext.class, null, model);

        // create container for the controller context as outermost element
        ContextDescriptor cContainer = new ContextDescriptor(controller.getInstanceName() + "Container", jams.model.JAMSContext.class, null, model);

        // create partitioner component
        Class partitionerClass;
        try {
            partitionerClass = Class.forName(partitionerClassName);
        } catch (ClassNotFoundException ex) {
            throw new JAMSException("Error while loading class " + partitionerClassName, ex);
        }
        
        ComponentDescriptor partitioner = new ComponentDescriptor(partitionerClass, null, model);

        // 1. detach context from parent, replace it by cContainer 
        // 2. create n copies of context and attach them to controller
        ModelNode node = context.getNode();
        ModelNode parent = (ModelNode) node.getParent();
        int index = parent.getIndex(node);

        // create new nodes for the controller and its container and
        // insert the controller into the container
        ModelNode cContainerNode = new ModelNode(cContainer);
        cContainerNode.setType(ModelNode.CONTEXT_TYPE);
        ModelNode cNode = new ModelNode(controller.cloneNode());
        cNode.setType(ModelNode.CONTEXT_TYPE);
        cContainerNode.insert(cNode, 0);

        // configure the partitioner component using information from the old spatial context
//            ComponentDescriptor partitionerClone = partitioner.cloneNode();
        ComponentField inEntities = partitioner.getComponentFields().get("inEntities");
        ComponentField outEntities = partitioner.getComponentFields().get("outEntities");

        ComponentField entitiesField = context.getComponentFields().get("entities");
        String entitiesAttributeName = entitiesField.getAttribute();
        ContextDescriptor entitiesProvider = entitiesField.getContext();
        inEntities.linkToAttribute(entitiesProvider, entitiesAttributeName);

        String newAttributeName = entitiesAttributeName + "_1";
        for (int i = 1; i < nthreads; i++) {
            newAttributeName += ";" + entitiesAttributeName + "_" + (i + 1);
        }
        outEntities.linkToAttribute(cContainer, newAttributeName);
        
        for (Entry<String, String> param : partitionerParams.entrySet()) {
            ComponentField paramField = partitioner.getComponentFields().get(param.getKey());
            if (paramField != null) {
            paramField.setValue(param.getValue());
            } else {
                rt.getLogger().info(MessageFormat.format(JAMS.i18n("Tried to set parameter \"{0}\" but it could not be found!"), param.getKey()));
            }
        }

        // create new node for the partioner component and 
        // insert it into the container (in front of the controller)
        ModelNode partitionerNode = new ModelNode(partitioner);
        partitionerNode.setType(ModelNode.COMPONENT_TYPE);
        cContainerNode.insert(partitionerNode, 0);

        // replace the spatial context by the newly created container node
        parent.insert(cContainerNode, index);
        node.removeFromParent();
        rt.println("    Removed context " + context.getInstanceName() + " and added new context " + cContainer.getInstanceName());

        // create a deep copy of the context in order to handle serial
        // component processing and datastore output
        ModelNode serialContextNode = node.clone(new ModelDescriptor(), true, new HashMap<ContextDescriptor, ContextDescriptor>());
        ContextDescriptor serialContext = (ContextDescriptor) serialContextNode.getUserObject();
        serialContext.setInstanceName(context.getInstanceName() + "_Serial");
        cContainerNode.insert(serialContextNode, 2);
        rt.println("    Added new serial context " + serialContext.getInstanceName());

        // iterate over nodes in the serial context and pick up all selected
        // serial components
        ArrayList<ModelNode> serialNodeList = new ArrayList();
        Enumeration<ModelNode> nodeEnum = serialContextNode.depthFirstEnumeration();
        while (nodeEnum.hasMoreElements()) {
            ModelNode n = nodeEnum.nextElement();
            ComponentDescriptor cd = (ComponentDescriptor) n.getUserObject();
//            for (ComponentField cf : cd.getComponentFields().values()) {
//                if (cf.getAccessType() != ComponentField.READ_ACCESS && !cf.getContext().getInstanceName().equals(context.getInstanceName())) {
//                    
//                }
//            }
            if (excludeComponents.contains(cd.getInstanceName())) {
                serialNodeList.add(n);
            }
        }

        // iterate again and remove all nodes except those representing
        // serial components or their ancestors
        nodeEnum = serialContextNode.depthFirstEnumeration();
        ArrayList<ModelNode> removeList = new ArrayList();
        while (nodeEnum.hasMoreElements()) {
            ModelNode n = nodeEnum.nextElement();

            boolean removeNode = true;
            if (serialNodeList.contains(n)) {
                removeNode = false;
            }

            for (ModelNode serialNode : serialNodeList) {
                if (n.isNodeDescendant(serialNode)) {
                    removeNode = false;
                    break;
                }
            }

            if (removeNode && !n.equals(serialContextNode)) {
                removeList.add(n);
            } else {
                ComponentDescriptor cd = (ComponentDescriptor) n.getUserObject();
                cd.register(model);
            }
        }
        for (ModelNode n : removeList) {
            n.removeFromParent();
        }

        // remove serial components from the original spatial context
        removeList = new ArrayList();
        nodeEnum = node.depthFirstEnumeration();
        while (nodeEnum.hasMoreElements()) {
            ModelNode n = nodeEnum.nextElement();
            ComponentDescriptor cd = (ComponentDescriptor) n.getUserObject();
            if (excludeComponents.contains(cd.getInstanceName())) {
                removeList.add(n);
            }
        }
        for (ModelNode n : removeList) {
            n.removeFromParent();
        }


        // create new context for serial iteration
//            ContextDescriptor serialContext = new ContextDescriptor(context.getInstanceName() + "_Serial", jams.model.JAMSSpatialContext.class, model, exHandler);
//            ModelNode serialNode = new ModelNode(serialContext);
//            serialNode.setType(ModelNode.CONTEXT_TYPE);
//            cContainerNode.insert(serialNode, 2);
//            rt.println("    Added new serial context " + context.getInstanceName() + "_Serial");
//            entitiesField = serialContext.getComponentFields().get("entities");
//            entitiesField.linkToAttribute(entitiesProvider, entitiesAttributeName);
//
//            // move all serial components to the serial context and add the 
//            // serial context to the outter container node
//            for (ModelNode n : serialNodeList) {
//                n.removeFromParent();
//
//                if (serialNode.getParent() == null) {
//                }
//            }


        // create "nthread" deep copies of the spatial context and insert 
        // them into the controller context, reconfiguring their "entities"
        // attributes
        for (int i = 0; i < nthreads; i++) {
            ModelNode copy = node.clone(model, true, new HashMap<ContextDescriptor, ContextDescriptor>());
            ContextDescriptor cdCopy = (ContextDescriptor) copy.getUserObject();
            ComponentField entities = cdCopy.getComponentFields().get("entities");
            entities.linkToAttribute(cContainer, entities.getAttribute() + "_" + (i + 1));
            cNode.add(copy);
        }

        // take care of datastores

        ComponentField proxyAttributes = null;
        ComponentDescriptor caProxy = null;
        HashSet<ContextAttribute> attributes = new HashSet();
        HashMap<String, OutputDSDescriptor> stores = model.getDatastores();
        for (OutputDSDescriptor store : stores.values()) {
            if (store.getContext() == context) {

                if (caProxy == null) {
                    // create datastore helper component
                    caProxy = new ComponentDescriptor(context.getInstanceName() + "_DSProxy", CAProxy.class, null, model);
                    ModelNode caProxyNode = new ModelNode(caProxy);
                    caProxyNode.setType(ModelNode.COMPONENT_TYPE);
                    serialContextNode.insert(caProxyNode, 0);

                    proxyAttributes = caProxy.getComponentFields().get("attributes");

                }

                store.setContext(serialContext);
                attributes.addAll(store.getContextAttributes());
            }
        }

        for (ContextAttribute ca : attributes) {
            proxyAttributes.linkToAttribute(serialContext, ca.getName(), false);
        }
        
//        model.getLogger().setUseParentHandlers(true);

    }
}

/*
 *
            <metaprocessor>
                <class name="jams.components.concurrency.ConcurrentContextProcessor"/>
                <property name="nthreads" value="4"/>
            </metaprocessor>
 * 
 */