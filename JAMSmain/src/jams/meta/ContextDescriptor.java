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
import jams.ExceptionHandler;
import jams.data.JAMSDataFactory;
import jams.meta.ComponentField.AttributeLinkException;
import java.text.MessageFormat;
import java.util.Map.Entry;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ContextDescriptor extends ComponentDescriptor {

    private HashMap<String, ContextAttribute> staticAttributes = new HashMap<String, ContextAttribute>();
    private HashMap<String, ContextAttribute> dynamicAttributes = new HashMap<String, ContextAttribute>();

    public ContextDescriptor(String instanceName, Class clazz) throws NullClassException {
        super(instanceName, clazz);
    }

    public ContextDescriptor(String instanceName, Class clazz, ComponentCollection md, ExceptionHandler jeh) throws NullClassException {
        super(instanceName, clazz, md, jeh);
    }

    public ContextDescriptor(Class clazz, ComponentCollection md, ExceptionHandler jeh) throws NullClassException {
        super(clazz, md, jeh);
    }

//    public ContextDescriptor(Class clazz) throws JAMSException {
//        this(clazz.getSimpleName(), clazz);
//    }
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
        public ContextAttribute addStaticAttribute(String name, Class type, String value) throws ContextAttributeException {

        ContextAttribute ca = staticAttributes.get(name);

        // info wenn attribut mit gleichem namen schon existent und dann zum repo adden!!!
        if (ca != null) {

            throw new ContextAttributeException(MessageFormat.format(JAMS.i18n("Context_attribute_does_already_exist"), name),
                    JAMS.i18n("Error_adding_context_attribute"));

        } else {
            ca = new ContextAttribute(name, type, this);
            staticAttributes.put(name, ca);
        }

        ca.setValue(value);

        return ca;
    }

    public class ContextAttributeException extends JAMSException {

        public ContextAttributeException(String message, String header) {
            super(message, header);
        }
    }

    public void removeStaticAttribute(String name) {
        ContextAttribute ca = staticAttributes.get(name);
        staticAttributes.remove(name);
    }

    public HashMap<String, ContextAttribute> getStaticAttributes() {
        return staticAttributes;
    }

    public HashMap<String, ContextAttribute> getDynamicAttributes() {
        return dynamicAttributes;
    }

    public HashMap<String, ContextAttribute> getAttributes(Class<?> type) {
        type = JAMSDataFactory.getBelongingInterface(type);
        HashMap<String, ContextAttribute> result = new HashMap<String, ContextAttribute>();
        for (Entry<String, ContextAttribute> entry : dynamicAttributes.entrySet()) {
            if (type.isAssignableFrom(entry.getValue().getType())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        for (Entry<String, ContextAttribute> entry : staticAttributes.entrySet()) {
            if (type.isAssignableFrom(entry.getValue().getType())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    @Override
    public ContextDescriptor cloneNode() throws AttributeLinkException, NullClassException {

        ContextDescriptor copy = new ContextDescriptor(getName(), getClazz());
        for (String name : componentFields.keySet()) {
            ComponentField ca = componentFields.get(name);
            ComponentField caCopy = new ComponentField(ca.getName(), ca.getType(), ca.getAccessType(), this);
            caCopy.setValue(ca.getValue());
            copy.componentFields.put(name, caCopy);
            if (ca.getContextAttributes().size() > 0) {
                caCopy.linkToAttribute(ca.getContext(), ca.getAttribute());
                //copy.linkComponentAttribute(ca.name, ca.getContextAttribute().getContext(), ca.getContextAttribute().getName());
            }
        }
        for (String name : staticAttributes.keySet()) {
            ContextAttribute ca = staticAttributes.get(name);
            ContextAttribute caCopy = new ContextAttribute(ca.getName(), ca.getType(), copy);
            caCopy.setValue(ca.getValue());
            caCopy.getFields().addAll(ca.getFields());
            copy.staticAttributes.put(name, caCopy);
        }
        for (String name : dynamicAttributes.keySet()) {
            ContextAttribute ca = dynamicAttributes.get(name);
            ContextAttribute caCopy = new ContextAttribute(ca.getName(), ca.getType(), copy);
            caCopy.setValue(ca.getValue());
            caCopy.getFields().addAll(ca.getFields());
            copy.dynamicAttributes.put(name, caCopy);
        }

        return copy;
    }
}
