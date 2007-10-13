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

package org.unijena.juice;

import java.lang.reflect.Field;
import java.util.HashMap;
import org.unijena.jams.gui.LHelper;
import org.unijena.jams.model.JAMSVarDescription;
import org.unijena.juice.gui.ModelView;
import org.unijena.juice.gui.tree.*;

/**
 *
 * @author S. Kralisch
 */
public class ComponentDescriptor {
    
    private String instanceName;
    private Class clazz;
    private JAMSTree tree;
    private HashMap<String, ComponentAttribute> cVars = new HashMap<String, ComponentAttribute>();
    private HashMap<String, ContextAttribute> contextAttributes = new HashMap<String, ContextAttribute>();
    private AttributeRepository dataRepository;
    
    public ComponentDescriptor(String instanceName, Class clazz, JAMSTree tree) {
        if (clazz == null) {
            LHelper.showInfoDlg(JUICE.getJuiceFrame(), "Could not find class for component \"" + instanceName + "\"!", "Error!");
        }
        this.clazz = clazz;
        this.tree = tree;
        
        try {
            this.setInstanceName(instanceName);
        } catch (JUICEException.NameAlreadyUsedException ex) {}
        
        init();
        dataRepository = new AttributeRepository(this);
    }
    
    public ComponentDescriptor(Class clazz, JAMSTree tree) {
        this(clazz.getSimpleName(), clazz, tree);
    }
    
    private void init() {
        
        Field[] compFields = getClazz().getFields();
        
        for (Field field : compFields) {
            JAMSVarDescription jvd = (JAMSVarDescription) field.getAnnotation(JAMSVarDescription.class);
            
            //check if there actually is a jvd, else this is some other field and we're not interested
            if (jvd != null) {
                int accessType;
                
                if (jvd.access() == JAMSVarDescription.AccessType.READ)
                    accessType = ComponentAttribute.READ_ACCESS;
                else if (jvd.access() == JAMSVarDescription.AccessType.WRITE)
                    accessType = ComponentAttribute.WRITE_ACCESS;
                else
                    accessType = ComponentAttribute.READWRITE_ACCESS;
                
                getComponentAttributes().put(field.getName(), new ComponentAttribute(field.getName(), field.getType(), accessType));
            }
        }
    }
    
    public String toString() {
        return getName();
    }
    
    public ContextAttribute addContextAttribute(String name, Class type, String value) {
        ContextAttribute ma = getContextAttributes().get(name);
        
        // info wenn attribut mit gleichem namen schon existent und dann zum repo adden!!!
        if (ma != null) {
            
            LHelper.showErrorDlg(JUICE.getJuiceFrame(), "Context attribute \"" + name + "\" does already exist. " +
                    "Please remove or chose a different name!", "Error adding context attribute");
            return null;
            
        } else {
            ma = new ContextAttribute(name, type, this);
            getContextAttributes().put(name, ma);
        }
        
        ma.setValue(value);
        
        return ma;
    }
    
    public void removeContextAttribute(String name) {
        getContextAttributes().remove(name);
    }
    
    public void setComponentAttribute_(String name, String value) {
        ComponentAttribute ca = getComponentAttributes().get(name);
        if (ca != null) {
            ca.setValue(value);
        }
    }
    
    public void linkComponentAttribute_(String componentAttributeName, ComponentDescriptor context, String contextAttributeName) {
        
        ComponentAttribute ca = getComponentAttributes().get(componentAttributeName);
        
        if (ca != null) {
            
            // create a context attribute object
            ContextAttribute a = new ContextAttribute(contextAttributeName, ca.type, context);
            
            // if access is W or R/W (not R), then the component authomatically
            // creates a new context attribute which is registered at the
            // contexts attribute repository in order to be accessed by
            // other components
            if (ca.accessType != ComponentAttribute.READ_ACCESS) {
                
                // check if component attribute has been linked before
                // and unlink if thats the case
                if (ca.getContext() != null) {
                    AttributeRepository oldRepo = ca.getContext().getDataRepository();
                    oldRepo.removeAttribute(ca.getContextAttribute());
                }
                
                AttributeRepository newRepo = context.getDataRepository();
                newRepo.addAttribute(a);
            }
            
            // finally, set the component attributes context and context attribute
            ca.contextAttribute = a;
            //ca.context = context;
            //ca.attribute = contextAttributeName;
        }
    }
    
    public void outputUnsetAttributes() {
        for (ComponentAttribute ad : getComponentAttributes().values()) {
            if (ad.getAttribute() == null && ad.getContext() == null && ad.getValue() == null) {
                System.out.println("Attribute " + ad.name + " (" + ad.type + ") not set in component " + getName());
            }
        }
    }
    
    public ComponentDescriptor clone(JAMSTree target) {
        ModelView view = JUICE.getJuiceFrame().getCurrentView();
        ComponentDescriptor copy = new ComponentDescriptor(getName(), getClazz(), target);
        for (String name : cVars.keySet()) {
            ComponentAttribute ca = cVars.get(name);
            ComponentAttribute caCopy = new ComponentAttribute(ca.name, ca.type, ca.accessType);
            caCopy.setValue(ca.getValue());
            copy.cVars.put(name, caCopy);
            if (ca.getContextAttribute() != null) {
                caCopy.linkToAttribute(ca.getContextAttribute().getContext(), ca.getContextAttribute().getName());
                //copy.linkComponentAttribute(ca.name, ca.getContextAttribute().getContext(), ca.getContextAttribute().getName());
            }
        }
        for (String name : contextAttributes.keySet()) {
            ContextAttribute ca = contextAttributes.get(name);
            ContextAttribute caCopy = new ContextAttribute(ca.getName(), ca.getType(), ca.getContext());
            caCopy.setValue(ca.getValue());
            copy.contextAttributes.put(name, caCopy);
        }
        
        return copy;
    }
    
    public String getName() {
        return instanceName;
    }
    
    public Class getClazz() {
        return clazz;
    }
    
    public HashMap<String, ComponentAttribute> getComponentAttributes() {
        return cVars;
    }
    
    public HashMap<String, ContextAttribute> getContextAttributes() {
        return contextAttributes;
    }
    
    public void setInstanceName(String name) throws JUICEException.NameAlreadyUsedException {
        String oldName = this.instanceName;
        if (this.tree instanceof ModelTree) {
            ModelTree modelTree = (ModelTree) this.tree;
            
            this.instanceName = modelTree.getView().registerComponentDescriptor(oldName, name, this);
            this.tree.updateUI();
            
            if (!this.instanceName.equals(name)) {
                throw JUICEException.getNameAlreadyUsedException(name);
            }
            
        } else {
            this.instanceName = name;
            this.tree.updateUI();
        }
    }
    
    public JAMSTree getTree() {
        return tree;
    }
    
    public AttributeRepository getDataRepository() {
        return dataRepository;
    }
    
    public class ComponentAttribute {
        
        public static final int READ_ACCESS = 0;
        public static final int WRITE_ACCESS = 1;
        public static final int READWRITE_ACCESS = 2;
        private String value = "";
        public String name = "";
        public Class type = null;
        public int accessType;
        private ContextAttribute contextAttribute;
        
        public ComponentAttribute(String name, Class type, int accessType) {
            super();
            this.name = name;
            this.type = type;
            this.accessType = accessType;
        }
        
        public String getAttribute() {
            if (contextAttribute != null) {
                return contextAttribute.getName();
            } else {
                return "";
            }
        }
        
        public ComponentDescriptor getContext() {
            if (contextAttribute != null) {
                return contextAttribute.getContext();
            } else {
                return null;
            }
        }
        
        public ContextAttribute getContextAttribute() {
            return contextAttribute;
        }
        
        public String getValue() {
            return value;
        }
                
        public void linkToAttribute(ComponentDescriptor context, String contextAttributeName) {
            
            // create a context attribute object
            ContextAttribute a = new ContextAttribute(contextAttributeName, this.type, context);
            
            // if access is W or R/W (not R), then the component authomatically
            // creates a new context attribute which is registered at the
            // contexts attribute repository in order to be accessed by
            // other components
            if (this.accessType != ComponentAttribute.READ_ACCESS) {
                
                // check if component attribute has been linked before
                // and unlink if thats the case
                ComponentDescriptor oldContext = this.getContext();
                if (oldContext != null) {
                    AttributeRepository oldRepo = oldContext.getDataRepository();
                    oldRepo.removeAttribute(this.getContextAttribute());
                }
                
                AttributeRepository newRepo = context.getDataRepository();
                newRepo.addAttribute(a);
            }
            
            // finally, set the component attributes context and context attribute
            this.contextAttribute = a;
            //ca.context = context;
            //ca.attribute = contextAttributeName;
        }

        public void setValue(String value) {
            this.value = value;
        }
        
    }
}

