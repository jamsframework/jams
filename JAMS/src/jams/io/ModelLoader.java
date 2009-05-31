/*
 * ModelLoader.java
 * Created on 26. September 2005, 16:55
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
 * GNU General Publiccc License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package jams.io;

import java.util.*;
import jams.dataaccess.DataAccessor;
import jams.model.*;
import org.w3c.dom.*;
import java.lang.reflect.*;
import jams.JAMS;
import jams.JAMSProperties;
import jams.JAMSTools;
import jams.data.*;
import jams.runtime.JAMSRuntime;
import java.util.ArrayList;

/**
 *
 * @author S. Kralisch
 */
public class ModelLoader {

    private HashMap<String, JAMSComponent> componentRepository = new HashMap<String, JAMSComponent>();

    private HashMap<String, String> constants = new HashMap<String, String>();

    private ClassLoader loader;

    private JAMSModel jamsModel;

    private JAMSProperties properties;

    transient private HashMap<JAMSComponent, ArrayList<Field>> nullFields = new HashMap<JAMSComponent, ArrayList<Field>>();

    public ModelLoader(Document modelDoc, String[] globvars, JAMSRuntime rt, JAMSProperties properties) {

        this.loader = rt.getClassLoader();
        this.properties = properties;

        if (globvars != null) {
            for (int i = 0; i < globvars.length; i++) {
                StringTokenizer tok = new StringTokenizer(globvars[i], "=");
                if (tok.countTokens() == 2) {
                    constants.put(tok.nextToken(), tok.nextToken());
                }
            }
        }

        jamsModel = new JAMSModel(rt);
        jamsModel.setModel(jamsModel);

        this.loadModel(modelDoc);
    }

    private void loadModel(Document modelDoc) {

        Element root, element;
        Node node;
        JAMSComponent topComponent;

        root = modelDoc.getDocumentElement();

        jamsModel.setName(root.getAttribute("name"));
        jamsModel.setAuthor(root.getAttribute("author"));
        jamsModel.setDate(root.getAttribute("date"));

        /*Element workspaceElement = (Element) root.getElementsByTagName("workspace").item(0);
        jamsModel.setWorkspaceDirectory(workspaceElement.getAttribute("value"));*/


        NodeList childs = root.getChildNodes();

        // first check all childs for globvar nodes!
        for (int index = 0; index < childs.getLength(); index++) {

            node = childs.item(index);
            if (node.getNodeName().equals("globvar")) {
                element = (Element) node;
                if (!constants.containsKey(element.getAttribute("name"))) {
                    constants.put(element.getAttribute("name"), element.getAttribute("value"));
                }
            }

            if (node.getNodeName().equals("attribute")) {
                element = (Element) node;
                jamsModel.addAttribute(element.getAttribute("name"), element.getAttribute("class"), element.getAttribute("value"));
            }

            if (node.getNodeName().equals("var")) {
                element = (Element) node;
                if (element.getAttribute("name").equals("workspaceDirectory")) {
                    jamsModel.setWorkspaceDirectory(element.getAttribute("value"));
                }
            }
        }

        jamsModel.getRuntime().println(JAMS.resources.getString("*************************************"), JAMS.STANDARD);
        jamsModel.getRuntime().println(JAMS.resources.getString("model_____:_") + jamsModel.getName(), JAMS.STANDARD);
        jamsModel.getRuntime().println(JAMS.resources.getString("workspace_:_") + jamsModel.workspaceDirectory.getValue(), JAMS.STANDARD);
        jamsModel.getRuntime().println(JAMS.resources.getString("author____:_") + jamsModel.getAuthor(), JAMS.STANDARD);
        jamsModel.getRuntime().println(JAMS.resources.getString("date______:_") + jamsModel.getDate(), JAMS.STANDARD);
        jamsModel.getRuntime().println(JAMS.resources.getString("*************************************"), JAMS.STANDARD);


        // create the model
        ArrayList<JAMSComponent> childComponentList = new ArrayList<JAMSComponent>();
        for (int index = 0; index < childs.getLength(); index++) {
            node = childs.item(index);
            if (node.getNodeName().equals("contextcomponent") || node.getNodeName().equals("component")) {
                element = (Element) node;

                try {

                    topComponent = loadComponent(element, jamsModel);
                    jamsModel.addComponent(topComponent);
                    childComponentList.add(topComponent);

                } catch (ModelSpecificationException iae) {

                    jamsModel.getRuntime().handle(iae, false);
                    return;

                }
            }
        }
        jamsModel.setComponents(childComponentList);
        jamsModel.setNullFields(nullFields);
    }

    /**
     * Recursively create all components used in the model and add them to the component repository for easy access
     */
    private JAMSComponent loadComponent(Element root, JAMSComponent rootComponent) throws ModelSpecificationException {

        String componentName, componentClassName, varName, varClassName = "", varValue;
        JAMSComponent component, childComponent;
        JAMSData variable;
        Class<?> componentClazz = null, varClazz = null;
        ArrayList<JAMSComponent> childComponentList = new ArrayList<JAMSComponent>();

        componentName = root.getAttribute("name");
        componentClassName = root.getAttribute("class");

        // check if a component with that name is already existing
        JAMSComponent existingComponent = this.componentRepository.get(componentName);
        if (existingComponent != null) {
            throw new ModelSpecificationException(JAMS.resources.getString("Component_with_name_") + componentName +
                    JAMS.resources.getString("_is_already_exisiting_(") + existingComponent.getClass() +
                    JAMS.resources.getString(")._Please_make_sure_component_names_are_unique!_Stopping_model_loading!"));
        }

        jamsModel.getRuntime().println(JAMS.resources.getString("Adding:_") + componentName + " (" + componentClassName + ")", JAMS.STANDARD);

        component = null;
        try {

            // create the JAMSComponent object
            jamsModel.getRuntime().println(componentClassName, JAMS.VERBOSE);

            // try to load the class
            componentClazz = loader.loadClass(componentClassName);

            // generate an instance of that class
            component = (JAMSComponent) componentClazz.newInstance();

            // do some basic setup
            component.setModel(jamsModel);
            component.setInstanceName(componentName);

            if (component instanceof JAMSGUIComponent) {
                JAMSGUIComponent guiComponent = (JAMSGUIComponent) component;
                jamsModel.getRuntime().addGUIComponent(guiComponent);
            }

            // create Objects for component fields and set units and ranges
            ArrayList<Field> nf = createMembers(component);
            nullFields.put(component, nf);

        //createNumericMembers(component);            

        } catch (ClassNotFoundException cnfe) {
            jamsModel.getRuntime().handle(cnfe, false);
            return null;
        } catch (InstantiationException ie) {
            jamsModel.getRuntime().handle(ie, false);
        } catch (IllegalAccessException iae) {
            jamsModel.getRuntime().handle(iae, false);
        } catch (Throwable t) {
            jamsModel.getRuntime().handle(t, false);
        }

        // put the JAMSComponent object into the component repository
        this.componentRepository.put(componentName, component);

        // get element child nodes
        NodeList childs = root.getChildNodes();

        for (int index = 0; index < childs.getLength(); index++) {

            Node node = childs.item(index);

            if (node.getNodeName().equals("contextcomponent") || node.getNodeName().equals("component")) {

                // process child components of context components
                childComponent = loadComponent((Element) node, component);
                if (childComponent != null) {
                    childComponentList.add(childComponent);
                }

            /*
            if (childComponent instanceof JAMSGUIComponent) {
            JAMSGUIComponent guiComponent = (JAMSGUIComponent) childComponent;
            jamsModel.getRuntime().addGUIComponent(guiComponent);
            }
             */

            } else if (node.getNodeName().equals("var")) {

                // process components variable declarations
                Element element = (Element) node;

                varName = element.getAttribute("name");
                // varClassName = element.getAttribute("class");

                // check if component variable exists
                try {
                    Field field = JAMSTools.getField(componentClazz, varName);
                    varClassName = field.getType().getName();

                    if (field.isAnnotationPresent(JAMSVarDescription.class)) {

                        JAMSVarDescription jvd = field.getAnnotation(JAMSVarDescription.class);

                        jamsModel.getRuntime().println("     " + componentName + JAMS.resources.getString("_var_declaration:_") + varName + " (" + varClassName + ", " + jvd.access() + ")", JAMS.VERBOSE);

                        // set the var object if value provided directly
                        if (element.hasAttribute("value")) {

                            // create the var object
                            varClazz = loader.loadClass(varClassName);

                            variable = JAMSDataFactory.createInstance(varClazz);
                            // variable = createInstance(varClazz);

                            varValue = element.getAttribute("value");
                            variable.setValue(varValue);

                            // try to attach the variable to the component's field..
                            JAMSData data;
                            try {
                                data = (JAMSData) JAMSTools.setField(component, field, variable);
                            } catch (NoSuchMethodException nsme) {
                                throw new ModelSpecificationException(JAMS.resources.getString("Component_") + componentName + JAMS.resources.getString(":_variable_") + varName + JAMS.resources.getString(":_Access_exception!"));
                            }

                            // this field can be removed from the null field list
                            nullFields.get(component).remove(field);

                            String id = componentName + "." + varName;
                            jamsModel.getRuntime().getDataHandles().put(id, data);

                        }

                        // set the var object if value provided via globvar
                        // attribute
                        /*
                        if (element.hasAttribute("globvar")) {
                        // create the var object
                        varClazz = loader.loadClass(varClassName);
                        variable = (JAMSData) varClazz.newInstance();
                        // variable = createInstance(varClazz);
                        varValue = element.getAttribute("globvar");
                        variable.setValue(constants.get(varValue));
                        // attach the variable to the component's field..
                        field.set(component, variable);
                        }
                         */

                        if (element.hasAttribute("attribute")) {

                            // obtain providing context
                            JAMSComponent context = this.componentRepository.get(element.getAttribute("context"));

                            if (context == null) {
                                // throw new ModelSpecificationException("Component " + componentName + ": Component \"" + element.getAttribute("context") + "\" not available!");
                                context = jamsModel;
                            }

                            // check if providing context supplies specified variable
                            // ...

                            if (!(context instanceof JAMSContext)) {
                                throw new ModelSpecificationException(JAMS.resources.getString("Component_") + componentName + JAMS.resources.getString(":_Component_") + element.getAttribute("context") + JAMS.resources.getString("_must_be_of_type_JAMSSpatialContext!"));
                            }

                            JAMSContext sc = (JAMSContext) context;
                            String attributeName;

                            attributeName = element.getAttribute("attribute");

                            if (jvd.access() == JAMSVarDescription.AccessType.READ) {
                                sc.addAccess(component, varName, attributeName, DataAccessor.READ_ACCESS);
                            } else if (jvd.access() == JAMSVarDescription.AccessType.WRITE) {
                                sc.addAccess(component, varName, attributeName, DataAccessor.WRITE_ACCESS);
                            } else if (jvd.access() == JAMSVarDescription.AccessType.READWRITE) {
                                sc.addAccess(component, varName, attributeName, DataAccessor.READWRITE_ACCESS);
                            }

                            nullFields.get(component).remove(field);
                        }

                    /*
                    if (jvd.trace() == JAMSVarDescription.UpdateType.INIT) {
                    JAMSData data = (JAMSData) field.get(component);
                    String id = componentName + "." + varName;
                    jamsModel.getRuntime().getDataHandles().put(id, data);
                    }
                     */
                    } else {
                        throw new ModelSpecificationException(JAMS.resources.getString("Component_") + componentName + JAMS.resources.getString(":_variable_") + varName + JAMS.resources.getString("_can_not_be_accessed_(missing_annotation)!"));
                    }

                } catch (NoSuchFieldException nsfe) {
                    throw new ModelSpecificationException(JAMS.resources.getString("Component_") + componentName + JAMS.resources.getString(":_variable_") + varName + JAMS.resources.getString("_not_found!"));
                } catch (ClassNotFoundException cnfe) {
                    throw new ModelSpecificationException(JAMS.resources.getString("Component_") + componentName + JAMS.resources.getString(":_variable_class_") + varClassName + JAMS.resources.getString("_not_found!"));
                } catch (IllegalArgumentException iae) {
                    throw new ModelSpecificationException(JAMS.resources.getString("Component_") + componentName + JAMS.resources.getString(":_variable_") + varName + JAMS.resources.getString(":_wrong_type!"));
                } catch (InstantiationException ie) {
                    throw new ModelSpecificationException(JAMS.resources.getString("Component_") + componentName + JAMS.resources.getString(":_variable_") + varName + JAMS.resources.getString(":_Instantiation_exception!"));
                } catch (IllegalAccessException iae) {
                    throw new ModelSpecificationException(JAMS.resources.getString("Component_") + componentName + JAMS.resources.getString(":_variable_") + varName + JAMS.resources.getString(":_Access_exception!"));
                } catch (Exception ex) {
                    jamsModel.getRuntime().handle(ex);
                }

            // JAMS var declaration

            } else if (node.getNodeName().equals("attribute")) {

                if (!JAMSContext.class.isAssignableFrom(component.getClass())) {
                    throw new ModelSpecificationException(JAMS.resources.getString("Attribute_tag_can_only_be_used_inside_context_components!_(component_") + componentName + JAMS.resources.getString(")"));
                }

                Element element = (Element) node;
                ((JAMSContext) component).addAttribute(element.getAttribute("name"), element.getAttribute("class"), element.getAttribute("value"));

            } /*else if (node.getNodeName().equals("trace")) {
        
        if (!JAMSContext.class.isAssignableFrom(component.getImplementingClass())) {
        throw new ModelSpecificationException("Trace tag can only be used inside context components! (component " + componentName + ")");
        }
        
        Element element = (Element) node;
        ((JAMSContext) component).getDataTracer().registerAttribute(element.getAttribute("attribute"));
        jamsModel.getRuntime().println("Registering trace for " + component.getInstanceName() + "->" + element.getAttribute("attribute"), JAMS.STANDARD);
        
        }*/
        }
        if (component instanceof JAMSContext) {
            ((JAMSContext) component).setComponents(childComponentList);
        }

        return component;
    }

    public HashMap<JAMSComponent, ArrayList<Field>> getNullFields() {
        return nullFields;
    }

    public void setNullFields(HashMap<JAMSComponent, ArrayList<Field>> nullFields) {
        this.nullFields = nullFields;
    }

    class ModelSpecificationException extends Exception {

        public ModelSpecificationException(String errorMsg) {
            super(errorMsg);
        }
    }

    private ArrayList<Field> createMembers(JAMSComponent component) throws IllegalAccessException, InstantiationException {

        Object o;
        Class dataType;
        ArrayList<Field> result = new ArrayList<Field>();

        Field[] fields = component.getClass().getFields();
        for (int i = 0; i < fields.length; i++) {
            o = fields[i].get(component);
            dataType = fields[i].getType();

            if (!dataType.isInterface() && JAMSData.class.isAssignableFrom(dataType) && fields[i].isAnnotationPresent(JAMSVarDescription.class)) {

                JAMSData dataObject = (JAMSData) o;
                JAMSVarDescription jvd = fields[i].getAnnotation(JAMSVarDescription.class);

                // get variable object or create one if not existing
                if ((dataObject == null) && (!jvd.defaultValue().equals(JAMSVarDescription.NULL_VALUE) || (jvd.access() == JAMSVarDescription.AccessType.WRITE))) {
                    dataObject = JAMSDataFactory.createInstance(dataType);
                    fields[i].set(component, dataObject);
                } else {
                    result.add(fields[i]);
                }

                // set value for data object if defined
                if (!jvd.defaultValue().equals(JAMSVarDescription.NULL_VALUE)) {
                    dataObject.setValue(jvd.defaultValue());
                }

            }
        }
        return result;
    }

    /**
     * Returns the loaded model
     * @return The loaded model
     */
    public JAMSModel getModel() {
        return jamsModel;
    }
}
