/*
 * Context.java
 * Created on 20.09.2010, 22:56:27
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.meta;

import jams.JAMSException;
import java.util.HashMap;
import jams.JAMS;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ContextDescriptor extends ComponentDescriptor {

    private AttributeRepository dataRepository;
    private HashMap<String, ContextAttribute> staticAttributes = new HashMap<String, ContextAttribute>();
    private HashMap<String, ContextAttribute> dynamicAttributes = new HashMap<String, ContextAttribute>();

    public ContextDescriptor(String instanceName, Class clazz) throws JAMSException {
        super(instanceName, clazz);
        dataRepository = new AttributeRepository();
    }

    public ContextDescriptor(String instanceName, Class clazz, ModelDescriptor md) throws JAMSException {
        this(instanceName, clazz);
        register(md);
    }

    public ContextDescriptor(Class clazz) throws JAMSException {
        this(clazz.getSimpleName(), clazz);
    }

    public AttributeRepository getDataRepository() {
        return dataRepository;
    }

//    public ContextAttribute addToDynamicAttribute(String name, ComponentField field) throws JAMSException {
//
//        ContextAttribute ca = dynamicAttributes.get(name);
//
//        // info wenn attribut mit gleichem namen schon existent und dann zum repo adden!!!
//        if (ca == null) {
//            Class type = field.getClass();
//            ca = new ContextAttribute(name, type, this);
//            dynamicAttributes.put(name, ca);
//        }
//
//        return ca;
//    }

    public ContextAttribute addStaticAttribute(String name, Class type, String value) throws JAMSException {

        ContextAttribute ca = staticAttributes.get(name);

        // info wenn attribut mit gleichem namen schon existent und dann zum repo adden!!!
        if (ca != null) {

            throw new JAMSException(JAMS.resources.getString("Context_attribute_") + name + JAMS.resources.getString("_does_already_exist._")
                    + JAMS.resources.getString("Please_remove_or_chose_a_different_name!"), JAMS.resources.getString("Error_adding_context_attribute"));

        } else {
            ca = new ContextAttribute(name, type, this);
            staticAttributes.put(name, ca);
            getDataRepository().addAttribute(ca);
        }

        ca.setValue(value);

        return ca;
    }

    public void removeStaticAttribute(String name) {
        ContextAttribute ca = staticAttributes.get(name);
        staticAttributes.remove(name);
        getDataRepository().removeAttribute(ca);
    }

    public HashMap<String, ContextAttribute> getStaticAttributes() {
        return staticAttributes;
    }

    public HashMap<String, ContextAttribute> getDynamicAttributes() {
        return dynamicAttributes;
    }

    @Override
    public ContextDescriptor cloneNode() throws JAMSException {

        ContextDescriptor copy = new ContextDescriptor(getName(), getClazz());
        for (String name : componentFields.keySet()) {
            ComponentField ca = componentFields.get(name);
            ComponentField caCopy = new ComponentField(ca.name, ca.type, ca.accessType);
            caCopy.setValue(ca.getValue());
            copy.componentFields.put(name, caCopy);
            if (ca.getContextAttribute() != null) {
                caCopy.linkToAttribute(ca.getContextAttribute().getContext(), ca.getContextAttribute().getName());
                //copy.linkComponentAttribute(ca.name, ca.getContextAttribute().getContext(), ca.getContextAttribute().getName());
            }
        }
        for (String name : staticAttributes.keySet()) {
            ContextAttribute ca = staticAttributes.get(name);
            ContextAttribute caCopy = new ContextAttribute(ca.getName(), ca.getType(), ca.getContext());
            caCopy.setValue(ca.getValue());
            copy.staticAttributes.put(name, caCopy);
        }

        return copy;
    }
}
