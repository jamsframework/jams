/*
 * ComponentDescriptor.java
 * Created on 8. August 2006, 00:25
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package jams.meta;

import java.lang.reflect.Field;
import java.util.HashMap;
import jams.model.JAMSVarDescription;
import java.util.ArrayList;
import jams.JAMS;
import jams.JAMSException;
import jams.model.Context;
import jams.tools.StringTools;
import java.util.Observable;

/**
 *
 * @author S. Kralisch
 */
public class ComponentDescriptor extends Observable {

    public static final int COMPONENT_TYPE = 0, CONTEXT_TYPE = 1;
    private String instanceName = "";
    private Class<?> clazz;
    private ArrayList<String> componentFieldList = new ArrayList<String>();
    protected HashMap<String, ComponentField> componentFields = new HashMap<String, ComponentField>();
    private int type;
    private ModelDescriptor modelDescriptor;

    public ComponentDescriptor(String instanceName, Class clazz) throws JAMSException {

        if (clazz == null) {
            throw new JAMSException(JAMS.resources.getString("Could_not_find_class_for_component_") + instanceName + "_!", JAMS.resources.getString("Error!"));
        }

        this.clazz = clazz;
        this.instanceName = instanceName;

        if (Context.class.isAssignableFrom(clazz)) {
            this.type = CONTEXT_TYPE;
        } else {
            this.type = COMPONENT_TYPE;
        }

        init();

    }

    public ComponentDescriptor(String instanceName, Class clazz, ModelDescriptor md) throws JAMSException {
        this(instanceName, clazz);
        register(md);
    }

    public ComponentDescriptor(Class clazz) throws JAMSException {
        this(clazz.getSimpleName(), clazz);
    }

    private void init() {

        Field[] compFields = getClazz().getFields();

        for (Field field : compFields) {
            JAMSVarDescription jvd = (JAMSVarDescription) field.getAnnotation(JAMSVarDescription.class);

            //check if there actually is a jvd, else this is some other field and we're not interested
            if (jvd != null) {
                int accessType;

                if (jvd.access() == JAMSVarDescription.AccessType.READ) {
                    accessType = ComponentField.READ_ACCESS;
                } else if (jvd.access() == JAMSVarDescription.AccessType.WRITE) {
                    accessType = ComponentField.WRITE_ACCESS;
                } else {
                    accessType = ComponentField.READWRITE_ACCESS;
                }

                getComponentFields().put(field.getName(), new ComponentField(field.getName(), field.getType(), accessType, this));
                getComponentFieldList().add(field.getName());
            }
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    public void setComponentAttribute_(String name, String value) {
        ComponentField ca = getComponentFields().get(name);
        if (ca != null) {
            ca.setValue(value);
        }
    }

    public void outputUnsetAttributes() {
        for (ComponentField ad : getComponentFields().values()) {
            if (ad.getAttribute() == null && ad.getContext() == null && ad.getValue() == null) {
                System.out.println(JAMS.resources.getString("Attribute_") + ad.getName() + " (" + ad.getType() + JAMS.resources.getString(")_not_set_in_component_") + getName());
            }
        }
    }

    public ComponentDescriptor cloneNode() throws JAMSException {

        ComponentDescriptor copy = new ComponentDescriptor(getName(), getClazz());
        for (String name : componentFields.keySet()) {
            ComponentField ca = componentFields.get(name);
            ComponentField caCopy = new ComponentField(ca.getName(), ca.getType(), ca.getAccessType(), copy);
            caCopy.setValue(ca.getValue());
            copy.componentFields.put(name, caCopy);
            if (ca.getContextAttributes() != null) {
                caCopy.linkToAttribute(ca.getContext(), ca.getAttribute());
                //copy.linkComponentAttribute(ca.name, ca.getContextAttribute().getContext(), ca.getContextAttribute().getName());
            }
        }

        return copy;
    }

    public String getName() {
        return instanceName;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    /**
     * @return the componentAttributeList
     */
    public ArrayList<String> getComponentFieldList() {
        return componentFieldList;
    }

    public HashMap<String, ComponentField> getComponentFields() {
        return componentFields;
    }

    public void unregister() {
        this.modelDescriptor.unRegisterComponentDescriptor(this);
        this.modelDescriptor = null;
    }

    public final void register(ModelDescriptor md) throws JAMSException {
        this.modelDescriptor = md;
        setInstanceName(this.instanceName);
    }

    public void setInstanceName(String name) throws JAMSException {
        String oldName = this.instanceName;

        this.instanceName = this.modelDescriptor.registerComponentDescriptor(oldName, name, this);

        if (!this.instanceName.equals(name)) {
            throw new JAMSException(name);
        }

        if (!oldName.equals(this.instanceName)) {
            this.setChanged();
            this.notifyObservers();
        }
    }

    public ComponentField createComponentField(String name, Class type, int accessType) {
        return new ComponentField(name, type, accessType, this);
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    public ArrayList<ComponentField> getParameterFields() {

        ArrayList<ComponentField> fields = new ArrayList<ComponentField>();

        for (String fieldName : getComponentFieldList()) {
            ComponentField field = getComponentFields().get(fieldName);
            if (!StringTools.isEmptyString(field.getValue())) {
                fields.add(field);
            }
        }

        return fields;
    }
}
