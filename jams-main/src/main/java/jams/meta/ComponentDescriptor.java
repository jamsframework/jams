/*
 * ComponentDescriptor.java
 * Created on 8. August 2006, 00:25
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.meta;

import java.lang.reflect.Field;
import java.util.HashMap;
import jams.model.JAMSVarDescription;
import java.util.ArrayList;
import jams.JAMS;
import jams.JAMSException;
import jams.JAMSLogging;
import jams.model.Context;
import jams.model.JAMSComponentDescription;
import jams.model.VersionComments;
import jams.model.VersionComments.Entry;
import jams.tools.StringTools;
import java.text.MessageFormat;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author S. Kralisch
 */
public class ComponentDescriptor extends Observable {

    private String instanceName = "";
    private Class<?> clazz;
    private ArrayList<String> componentFieldList = new ArrayList<String>();
    protected HashMap<String, ComponentField> componentFields = new HashMap<String, ComponentField>();
    private ComponentCollection componentRepository;
    private String version = JAMSComponentDescription.DEFAULT_VERSION;
    private ModelNode node;
    private boolean enabled = true;

    public ComponentDescriptor(String instanceName, Class clazz) {
        JAMSLogging.registerLogger(JAMSLogging.LogOption.CollectAndShow,
                Logger.getLogger(this.getClass().getName()));
        if (clazz == null) {
            throw new NullClassException(JAMS.i18n("Could_not_find_class_for_component_") + instanceName + "_!", JAMS.i18n("Error"));
        }

        JAMSComponentDescription jcd = (JAMSComponentDescription) clazz.getAnnotation(JAMSComponentDescription.class);
        if (jcd != null) {
            version = jcd.version();
        }

        this.clazz = clazz;
        this.instanceName = instanceName;

        init();
    }

    public ComponentDescriptor(String instanceName, Class clazz, String versionID, ComponentCollection md) {
        this(instanceName, clazz);

        if (versionID != null && !versionID.equals(this.version)) {

            String message = MessageFormat.format(JAMS.i18n("ComponentVersionMismatch"), instanceName, this.version, versionID);

            // get the change comments
            VersionComments vc = (VersionComments) clazz.getAnnotation(VersionComments.class);
            if (vc != null) {

                for (Entry entry : vc.entries()) {
                    if (entry.version().compareTo(versionID) > 0) {
                        message += "\n" + "=== version " + entry.version() + " ===\n" + entry.comment();
                    }
                }
            }

            Logger.getLogger(this.getClass().getName()).warning(message);
        }

        register(md);
    }

    public ComponentDescriptor(Class clazz, String versionID, ComponentCollection md) {
        this(clazz.getSimpleName(), clazz, versionID, md);
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

                getComponentFields().put(field.getName(), new ComponentField(field.getName(), field.getType(), accessType, jvd.description(), jvd.defaultValue(), jvd.unit(), this));
                getComponentFieldList().add(field.getName());
            }
        }
    }

    @Override
    public String toString() {
        return getInstanceName();
    }

    public void setComponentAttribute_(String name, String value) {
        ComponentField ca = getComponentFields().get(name);
        if (ca != null) {
            ca.setValue(value);
        }
    }

//    public void outputUnsetAttributes() {
//        for (ComponentField ad : getComponentFields().values()) {
//            if (ad.getAttribute() == null && ad.getContext() == null && ad.getValue() == null) {
//                logger.logger(Level.INFO, JAMS.i18n("Attribute_") + ad.getName() + " (" + ad.getType() + JAMS.i18n(")_not_set_in_component_") + getInstanceName());
//            }
//        }
//    }
    public ComponentDescriptor cloneNode() {

        ComponentDescriptor copy = new ComponentDescriptor(getInstanceName(), getClazz());
        copy.setEnabled(this.isEnabled());
        for (String name : componentFields.keySet()) {
            ComponentField ca = componentFields.get(name);
            ComponentField caCopy = new ComponentField(ca.getName(), ca.getType(), ca.getAccessType(), ca.getDescription(), ca.getDefaultValue(), ca.getUnit(), copy);
            caCopy.setValue(ca.getValue());
            copy.componentFields.put(name, caCopy);
            if (ca.getContextAttributes().size() > 0) {
                caCopy.linkToAttribute(ca.getContext(), ca.getAttribute());
                //copy.linkComponentAttribute(ca.name, ca.getContextAttribute().getContext(), ca.getContextAttribute().getInstanceName());
            }
        }

        return copy;
    }

    public String getInstanceName() {
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
        this.componentRepository.unRegisterComponentDescriptor(this);
        this.componentRepository = null;
    }

    public final void register(ComponentCollection md) {
        this.componentRepository = md;
        setInstanceName(this.instanceName);
    }

    public void setInstanceName(String name) {
        String oldName = this.instanceName;

        this.instanceName = this.componentRepository.registerComponentDescriptor(oldName, name, this);

        if (!oldName.equals(this.instanceName)) {
            this.setChanged();
            this.notifyObservers();
        }

    }
    
    public void setChanged() {
        super.setChanged();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the componentRepository
     */
    public ComponentCollection getComponentRepository() {
        return componentRepository;
    }

    /**
     * @return the versionID
     */
    public String getVersion() {
        return version;
    }

    public class NullClassException extends JAMSException {

        public NullClassException(String message, String header) {
            super(message, header);
        }
    }

    public ComponentField createComponentField(String name, Class type, int accessType, String description, String defaultValue, String unit) {
        return new ComponentField(name, type, accessType, description, defaultValue, unit, this);
    }

    /**
     * @return the type
     */
//    public int getType() {
//        return type;
//    }
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

    /**
     * @return the node
     */
    public ModelNode getNode() {
        return node;
    }

    /**
     * @param node the node to set
     */
    public void setNode(ModelNode node) {
        this.node = node;
    }

    public int getType() {
        if (this.node != null) {
            return this.node.getType();
        } else {
            if (Context.class.isAssignableFrom(clazz)) {
                return ModelNode.CONTEXT_TYPE;
            } else {
                return ModelNode.COMPONENT_TYPE;
            }
        }
    }
}
