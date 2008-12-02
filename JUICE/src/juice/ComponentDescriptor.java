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
package juice;

import java.lang.reflect.Field;
import java.util.HashMap;
import jams.gui.LHelper;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import juice.gui.ComponentInfoPanel;
import juice.gui.ModelView;
import juice.gui.tree.*;

/**
 *
 * @author S. Kralisch
 */
public class ComponentDescriptor {

    private String instanceName;
    private Class<?> clazz;
    private JAMSTree tree;
    private ArrayList<String> componentAttributeList = new ArrayList<String>();
    private HashMap<String, ComponentAttribute> componentAttributes = new HashMap<String, ComponentAttribute>();
    private HashMap<String, ContextAttribute> contextAttributes = new HashMap<String, ContextAttribute>();
    private AttributeRepository dataRepository;
    private static HashMap<Class, JDialog> compViewDlgs = new HashMap<Class, JDialog>();

    public ComponentDescriptor(String instanceName, Class clazz, JAMSTree tree) {
        if (clazz == null) {
            LHelper.showInfoDlg(JUICE.getJuiceFrame(), JUICE.resources.getString("Could_not_find_class_for_component_") + instanceName + "_!", JUICE.resources.getString("Error!"));
        }
        this.clazz = clazz;
        this.tree = tree;

        try {
            this.setInstanceName(instanceName);
        } catch (JUICEException.NameAlreadyUsedException ex) {
        }

        init();
        dataRepository = new AttributeRepository(this);
    }

    public ComponentDescriptor(Class clazz, JAMSTree tree) {
        this(clazz.getSimpleName(), clazz, tree);
    }

    public void displayMetadataDlg(Frame owner) {

        if (clazz != null) {

            if (compViewDlgs.containsKey(clazz)) {
                compViewDlgs.get(clazz).setVisible(true);
                return;
            }

            JDialog compViewDlg = new JDialog(owner);
            compViewDlg.setLocationByPlatform(true);
            compViewDlg.setTitle(clazz.getCanonicalName());

            compViewDlgs.put(clazz, compViewDlg);
            compViewDlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            ComponentInfoPanel compView = new ComponentInfoPanel();
            compViewDlg.add(new JScrollPane(compView));

            JAMSComponentDescription jcd = (JAMSComponentDescription) clazz.getAnnotation(JAMSComponentDescription.class);
            if (jcd != null) {
                compView.update(clazz.getCanonicalName(), jcd);
            } else {
                compView.reset(clazz.getCanonicalName());
            }

            compView.update(clazz.getFields());

            compViewDlg.setPreferredSize(new Dimension(450, 600));
            compViewDlg.pack();
            compViewDlg.setVisible(true);
        }

    }

    private void init() {

        Field[] compFields = getClazz().getFields();

        for (Field field : compFields) {
            JAMSVarDescription jvd = (JAMSVarDescription) field.getAnnotation(JAMSVarDescription.class);

            //check if there actually is a jvd, else this is some other field and we're not interested
            if (jvd != null) {
                int accessType;

                if (jvd.access() == JAMSVarDescription.AccessType.READ) {
                    accessType = ComponentAttribute.READ_ACCESS;
                } else if (jvd.access() == JAMSVarDescription.AccessType.WRITE) {
                    accessType = ComponentAttribute.WRITE_ACCESS;
                } else {
                    accessType = ComponentAttribute.READWRITE_ACCESS;
                }

                getComponentAttributes().put(field.getName(), new ComponentAttribute(field.getName(), field.getType(), accessType));
                getComponentAttributeList().add(field.getName());
            }
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    public ContextAttribute addContextAttribute(String name, Class type, String value) {
        ContextAttribute ma = getContextAttributes().get(name);

        // info wenn attribut mit gleichem namen schon existent und dann zum repo adden!!!
        if (ma != null) {

            LHelper.showErrorDlg(JUICE.getJuiceFrame(), JUICE.resources.getString("Context_attribute_") + name + JUICE.resources.getString("_does_already_exist._") +
                        JUICE.resources.getString("Please_remove_or_chose_a_different_name!"), JUICE.resources.getString("Error_adding_context_attribute"));
            return null;

        } else {
            ma = new ContextAttribute(name, type, this);
            getContextAttributes().put(name, ma);
            getDataRepository().addAttribute(ma);
        }

        ma.setValue(value);

        return ma;
    }

    public void removeContextAttribute(String name) {
        ContextAttribute ca = getContextAttributes().get(name);
        getContextAttributes().remove(name);
        getDataRepository().removeAttribute(ca);
    }

    public void setComponentAttribute_(String name, String value) {
        ComponentAttribute ca = getComponentAttributes().get(name);
        if (ca != null) {
            ca.setValue(value);
        }
    }

    public void outputUnsetAttributes() {
        for (ComponentAttribute ad : getComponentAttributes().values()) {
            if (ad.getAttribute() == null && ad.getContext() == null && ad.getValue() == null) {
                System.out.println(JUICE.resources.getString("Attribute_") + ad.name + " (" + ad.type + JUICE.resources.getString(")_not_set_in_component_") + getName());
            }
        }
    }

    public ComponentDescriptor clone(JAMSTree target) {
        ModelView view = JUICE.getJuiceFrame().getCurrentView();
        ComponentDescriptor copy = new ComponentDescriptor(getName(), getClazz(), target);
        for (String name : componentAttributes.keySet()) {
            ComponentAttribute ca = componentAttributes.get(name);
            ComponentAttribute caCopy = new ComponentAttribute(ca.name, ca.type, ca.accessType);
            caCopy.setValue(ca.getValue());
            copy.componentAttributes.put(name, caCopy);
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

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    /**
     * @return the componentAttributeList
     */
    public ArrayList<String> getComponentAttributeList() {
        return componentAttributeList;
    }

    public HashMap<String, ComponentAttribute> getComponentAttributes() {
        return componentAttributes;
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

    public ComponentAttribute createComponentAttribute(String name, Class type, int accessType) {
        return new ComponentAttribute(name, type, accessType);
    }

    public class ComponentAttribute {

        public static final int READ_ACCESS = 0;
        public static final int WRITE_ACCESS = 1;
        public static final int READWRITE_ACCESS = 2;
        private String value = "";
        public String name = "";
        public Class type = null;
        public int accessType;

        //must be a vector!!!
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

        public void unlinkFromAttribute() {

            // if access is W or R/W (not R), then the component authomatically
            // removes its context attribute from the context
            if (this.accessType != ComponentAttribute.READ_ACCESS) {

                // check if component attribute has been linked before
                // and unlink if thats the case
                ComponentDescriptor context = this.getContext();
                if (context != null) {
                    AttributeRepository repo = context.getDataRepository();
                    repo.removeAttribute(this.getContextAttribute());
                }
            }
            this.contextAttribute = null;
        }
        /*
        public void linkToAttribute(ComponentDescriptor context, String contextAttributeName) {
        if (!this.type.isArray()) {
        // if the type is not an array, simply create a context attribute
        // and add it to the repository
        linkToAttribute_(context, contextAttributeName);
        } else {
        linkToAttribute_(context, contextAttributeName);
        System.out.println(ComponentDescriptor.this.getName() + " -> " + contextAttributeName);
        // if it is an array, tokenize the attribute string (semicolon-separated)
        // and do the above for every token
        String[] values = JAMSTools.arrayStringAsStringArray(contextAttributeName);
        for (String value : values) {
        linkToAttribute_(context, value);
        }
        }
        }
         */

        public void linkToAttribute(ComponentDescriptor context, String contextAttributeName) {


            // this will be the attribute object to be linked
            ContextAttribute attribute;

            // if access is W or R/W (not R), then the component authomatically
            // creates a new context attribute which is registered at the
            // contexts attribute repository in order to be accessed by
            // other components
            if (this.accessType != ComponentAttribute.READ_ACCESS) {

                // create a context attribute object
                attribute = new ContextAttribute(contextAttributeName, this.type, context);

//                if ((this.accessType == ComponentAttribute.WRITE_ACCESS) && this.type.isArray()) {
//                    System.out.println(ComponentDescriptor.this.getName() + " -> " + contextAttributeName);
//                }

                // check if component attribute has been linked before
                // and unlink if thats the case
                ComponentDescriptor oldContext = this.getContext();
                if (oldContext != null) {
                    AttributeRepository oldRepo = oldContext.getDataRepository();
                    oldRepo.removeAttribute(this.getContextAttribute());
                }

                AttributeRepository newRepo = context.getDataRepository();
                newRepo.addAttribute(attribute);
            } else {
                //check if this one has been already declared by some writing component attribute
                attribute = context.getDataRepository().getAttributeByTypeName(this.type, contextAttributeName);

                // check if still not available
                // this happens, if the attribute has been implicitly declared by an entity set
                if (attribute == null) {
                    attribute = new ContextAttribute(contextAttributeName, this.type, context);
                }
            }

            // finally, set the component attributes context and context attribute
            this.contextAttribute = attribute;
        //ca.context = context;
        //ca.attribute = contextAttributeName;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}

