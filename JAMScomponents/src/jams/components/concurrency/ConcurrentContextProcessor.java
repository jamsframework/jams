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

import jams.ExceptionHandler;
import jams.JAMSException;
import jams.meta.ComponentDescriptor;
import jams.meta.ComponentField;
import jams.meta.ContextAttribute;
import jams.meta.ContextDescriptor;
import jams.meta.MetaProcessor;
import jams.meta.ModelDescriptor;
import jams.meta.ModelNode;
import jams.meta.OutputDSDescriptor;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ConcurrentContextProcessor implements MetaProcessor {

    private int nthreads = 1;

    @Override
    public void setValue(String key, String value) {
        if (key.equals("nthreads")) {
            nthreads = Integer.parseInt(value);
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
    public void process(ContextDescriptor context, ModelDescriptor model, ExceptionHandler exHandler) {


        if (!context.isEnabled() || (nthreads < 2)
                || !jams.model.JAMSSpatialContext.class.isAssignableFrom(context.getClazz())) {
            return;
        }

        try {

            // create controller context 
            ContextDescriptor controller = new ContextDescriptor(ConcurrentContext.class, model, exHandler);

            // create container for the controller context as outermost element
            ContextDescriptor cContainer = new ContextDescriptor(controller.getInstanceName() + "Container", jams.model.JAMSContext.class, model, exHandler);

            // create partitioner component
            ComponentDescriptor partitioner = new ComponentDescriptor(EntityPartitioner.class, model, exHandler);

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

            // create new node for the partioner component and 
            // insert it into the container (in front of the controller)
            ModelNode partitionerNode = new ModelNode(partitioner);
            partitionerNode.setType(ModelNode.COMPONENT_TYPE);
            cContainerNode.insert(partitionerNode, 0);

            // replace the spatial context by the newly created container node
            parent.insert(cContainerNode, index);
            node.removeFromParent();

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
            ContextDescriptor storeContext = null;
            ComponentField proxyAttributes = null;
            HashSet<ContextAttribute> attributes = new HashSet();
            HashMap<String, OutputDSDescriptor> stores = model.getDatastores();
            for (OutputDSDescriptor store : stores.values()) {
                if (store.getContext() == context) {

                    if (storeContext == null) {

                        // create new context to iterate over entities
                        storeContext = new ContextDescriptor(context.getInstanceName() + "_Stores", jams.model.JAMSSpatialContext.class, model, exHandler);
                        ModelNode storeNode = new ModelNode(storeContext);
                        storeNode.setType(ModelNode.CONTEXT_TYPE);
                        cContainerNode.insert(storeNode, 2);

                        // configure entities field
                        entitiesField = storeContext.getComponentFields().get("entities");
                        entitiesField.linkToAttribute(entitiesProvider, entitiesAttributeName);

                        // create datastore helper component
                        ComponentDescriptor caProxy = new ComponentDescriptor(context.getInstanceName() + "_DSProxy", CAProxy.class, model, exHandler);
                        ModelNode caProxyNode = new ModelNode(caProxy);
                        caProxyNode.setType(ModelNode.COMPONENT_TYPE);
                        storeNode.insert(caProxyNode, 0);

                        proxyAttributes = caProxy.getComponentFields().get("attributes");

                    }

                    store.setContext(storeContext);
                    attributes.addAll(store.getContextAttributes());
                }
            }

            for (ContextAttribute ca : attributes) {
                proxyAttributes.linkToAttribute(storeContext, ca.getName(), false);
            }            

        } catch (JAMSException ex) {
            exHandler.handle(ex);
        }

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