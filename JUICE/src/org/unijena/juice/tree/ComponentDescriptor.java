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

package org.unijena.juice.tree;

import java.lang.reflect.Field;
import java.util.HashMap;
import org.unijena.jams.gui.LHelper;
import org.unijena.jams.model.JAMSVarDescription;
import org.unijena.juice.JUICE;
import org.unijena.juice.JUICEException;
import org.unijena.juice.ModelView;

/**
 *
 * @author S. Kralisch
 */
public class ComponentDescriptor {
    
    private String name;
    private Class clazz;
    private JAMSTree tree;
    private HashMap<String, ComponentVar> cVars = new HashMap<String, ComponentVar>();
    private HashMap<String, ModelAttribute> modelAttributes = new HashMap<String, ModelAttribute>();
    
    public ComponentDescriptor(String name, Class clazz, JAMSTree tree, ModelView view) {
        if (clazz == null) {
            LHelper.showInfoDlg(JUICE.getJuiceFrame(), "Could not find class for component \"" + name + "\"!", "Error!");
        }
        this.clazz = clazz;
        this.tree = tree;

        try {
            this.setName(name);
        } catch (JUICEException.NameAlreadyUsedException ex) {}
        
        init();
    }
    
    public ComponentDescriptor(Class clazz, JAMSTree tree, ModelView view) {
        this(clazz.getSimpleName(), clazz, tree, view);
    }
    
    private void init() {
        
        Field[] compFields = getClazz().getFields();
        
        for (Field field : compFields) {
            JAMSVarDescription jvd = (JAMSVarDescription) field.getAnnotation(JAMSVarDescription.class);
            
            //check if there actually is a jvd, else this is some other field and we're not interested
            if (jvd != null) {
                int accessType;
                
                if (jvd.access() == JAMSVarDescription.AccessType.READ)
                    accessType = ComponentVar.READ_ACCESS;
                else if (jvd.access() == JAMSVarDescription.AccessType.WRITE)
                    accessType = ComponentVar.WRITE_ACCESS;
                else
                    accessType = ComponentVar.READWRITE_ACCESS;
                
                getCVars().put(field.getName(), new ComponentVar(field.getName(), field.getType(), accessType));
            }
        }
    }
    
    public String toString() {
        return getName();
    }
    
    public ModelAttribute addModelAttribute(String name, Class type, String value) {
        ModelAttribute ma = getModelAttributes().get(name);
        
        if (ma == null) {
            ma = new ModelAttribute(name, type);
            getModelAttributes().put(name, ma);
        }
        
        ma.value = value;
        
        return ma;
    }
    
    public void removeComponentAttr(String attrName) {
        getModelAttributes().remove(attrName);
    }
    
    public void setComponentVar(String name, String value) {
        ComponentVar ca = getCVars().get(name);
        if (ca != null) {
            ca.value = value;
        }
    }
    
    public void setComponentVar(String name, ComponentDescriptor context, String attributeName) {
        ComponentVar var = getCVars().get(name);
        if (var != null) {
            
            ModelAttribute attr = context.getModelAttributes().get(attributeName);
            
            if (attr == null) {
                if (var.accessType == ComponentVar.READ_ACCESS) {
                    //attribute not existing and read access -- bad!
                    //System.out.println("no such attribute in component " + this.getName() + "(" + name + "): " + attributeName);
                    //return;
                } else {
                    //attribute not existing but write access -- will create new
                    //attr = new ModelAttribute(attributeName, var.type);
                    //context.getModelAttributes().put(attributeName, attr);
                }
            }
            
            
            var.context = context;
            var.attribute = attributeName;
        }
    }
    
    public void outputUnsetAttributes() {
        for (ComponentVar ad : getCVars().values()) {
            if (ad.attribute == null && ad.context == null && ad.value == null) {
                System.out.println("Attribute " + ad.name + " (" + ad.type + ") not set in component " + getName());
            }
        }
    }
    
    public ComponentDescriptor clone(JAMSTree target) {
        ModelView view = JUICE.getJuiceFrame().getCurrentView();
        ComponentDescriptor copy = new ComponentDescriptor(getName(), getClazz(), target, view);
        for (String name : cVars.keySet()) {
            ComponentVar ca = cVars.get(name);
            ComponentVar caCopy = new ComponentVar(ca.name, ca.type, ca.accessType);
            caCopy.context = ca.context;
            caCopy.attribute = ca.attribute;
            caCopy.value = ca.value;
            copy.cVars.put(name, caCopy);
        }
        for (String name : modelAttributes.keySet()) {
            ModelAttribute ca = modelAttributes.get(name);
            ModelAttribute caCopy = new ModelAttribute(ca.name, ca.type);
            caCopy.value = ca.value;
            copy.modelAttributes.put(name, caCopy);
        }
        
        return copy;
    }
    
    public String getName() {
        return name;
    }
    
    public Class getClazz() {
        return clazz;
    }
    
    public HashMap<String, ComponentVar> getCVars() {
        return cVars;
    }
    
    public HashMap<String, ModelAttribute> getModelAttributes() {
        return modelAttributes;
    }
    
    public void setName(String name) throws JUICEException.NameAlreadyUsedException {
        String oldName = this.name;
        if (this.tree instanceof ModelTree) {
            ModelTree modelTree = (ModelTree) this.tree;
            
            this.name = modelTree.getView().registerComponentDescriptor(oldName, name, this);
            this.tree.updateUI();
            
            if (!this.name.equals(name)) {
                throw JUICEException.getNameAlreadyUsedException(name);
            }
            
        } else {
            this.name = name;
            this.tree.updateUI();
        }
    }
    
    public JAMSTree getTree() {
        return tree;
    }
    
/*    public void setTree(JAMSTree tree) {
        this.tree = tree;
    }
*/    
    public class ComponentVar {
        public static final int READ_ACCESS = 0, WRITE_ACCESS = 1, READWRITE_ACCESS = 2;
        public String attribute = "", value = "", name = "";
        public Class type = null;
        public int accessType;
        public ComponentDescriptor context;
        public ModelAttribute attribz;
        public ComponentVar(String name, Class type, int accessType) {
            this.name = name;
            this.type = type;
            this.accessType = accessType;
        }
    }
}

