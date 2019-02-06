/*
 * ModelLoader.java
 * Created on 26. September 2005, 16:55
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
 * GNU General Publiccc License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.io;

import jams.JAMSException;
import java.util.*;
import jams.dataaccess.DataAccessor;
import jams.model.*;
import org.w3c.dom.*;
import java.lang.reflect.*;
import jams.JAMS;
import jams.tools.JAMSTools;
import jams.data.*;
import jams.meta.ComponentDescriptor;
import jams.meta.ComponentField;
import jams.meta.ContextAttribute;
import jams.meta.ContextDescriptor;
import jams.meta.ModelNode;
import jams.meta.ModelDescriptor;
import jams.meta.OutputDSDescriptor;
import jams.runtime.JAMSRuntime;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author S. Kralisch
 */
public class ModelLoader {

    private HashMap<String, Component> componentRepository = new HashMap<String, Component>();
    transient private ClassLoader loader;
    private DataFactory dataFactory;
    private Model jamsModel;
    transient private HashMap<Component, ArrayList<Field>> nullFields = new HashMap<Component, ArrayList<Field>>();
    private HashMap<String, Integer> idMap = new HashMap();
    private HashMap<Integer, String> versionMap = new HashMap();
    private int maxID = 0;

    public ModelLoader(JAMSRuntime rt) {

        loader = rt.getClassLoader();
        dataFactory = rt.getDataFactory();
        // create an empty model
        jamsModel = new JAMSModel(rt);
        // this context refers to itself
        jamsModel.setModel(jamsModel);
    }

    /**
     * Loads and returns a new model
     *
     * @param modelDoc The XML document describing the model
     * @return The loaded model
     */
    public Model loadModel(ModelDescriptor md) {

        ModelNode rootNode, node;
//        Node node;
        Component topComponent;

        rootNode = md.getRootNode();

        ContextDescriptor modelContext = (ContextDescriptor) rootNode.getUserObject();
        jamsModel.setName(modelContext.getInstanceName());

        componentRepository.put(jamsModel.getName(), jamsModel);

        /*
         * Element workspaceElement = (Element)
         * root.getElementsByTagName("workspace").item(0);
         * jamsModel.setWorkspaceDirectory(workspaceElement.getAttribute("value"));
         */
        // handle context attributes of the model
        for (ContextAttribute attribute : modelContext.getStaticAttributes().values()) {
            jamsModel.addAttribute(attribute.getName(), attribute.getType().getName(), attribute.getValue());
        }

        // set the workspace dir
//        jamsModel.setWorkspacePath(modelContext.getComponentFields().get("workspaceDirectory").getValue());
        jamsModel.setWorkspacePath(md.getWorkspacePath());

        // handle output datastores
        if (jamsModel.getWorkspace() != null) {

            for (OutputDSDescriptor ds : md.getDatastores().values()) {
                try {
                    Document document = ds.createDocument();
                    jamsModel.getWorkspace().registerOutputDataStore(ds.getName(), document);
                } catch (ParserConfigurationException pce) {
                    jamsModel.getRuntime().handle(pce);
                }
            }
        }

        jamsModel.getRuntime().println(JAMS.i18n("*************************************"), JAMS.STANDARD);
        jamsModel.getRuntime().println(JAMS.i18n("model_____:_") + md.getModelName(), JAMS.STANDARD);
        jamsModel.getRuntime().println(JAMS.i18n("workspace_:_") + md.getWorkspacePath(), JAMS.STANDARD);
        jamsModel.getRuntime().println(JAMS.i18n("modelfile_:_") + md.getModelFilePath(), JAMS.STANDARD);
        jamsModel.getRuntime().println(JAMS.i18n("author____:_") + md.getAuthor(), JAMS.STANDARD);
        jamsModel.getRuntime().println(JAMS.i18n("date______:_") + md.getDate(), JAMS.STANDARD);
        jamsModel.getRuntime().println(JAMS.i18n("*************************************"), JAMS.STANDARD);

        // create the model
        ArrayList<Component> childComponentList = new ArrayList<Component>();
        for (int index = 0; index < rootNode.getChildCount(); index++) {

            node = (ModelNode) rootNode.getChildAt(index);

            try {

                topComponent = loadComponent(node);
                if (topComponent != null) {
                    childComponentList.add(topComponent);
                }

            } catch (ModelSpecificationException iae) {

                jamsModel.getRuntime().handle(iae);

            }
        }
        jamsModel.setComponents(childComponentList);
        jamsModel.setNullFields(nullFields);

        List<Integer> idList = new ArrayList(idMap.values());
        Collections.sort(idList);

        Map<Integer, String> classMap = new HashMap();
        for (String className : idMap.keySet()) {
            classMap.put(idMap.get(className), className);
        }

        jamsModel.getRuntime().println(JAMS.i18n("Components:"), JAMS.STANDARD);
        for (Integer id : idList) {
            jamsModel.getRuntime().println("[id=" + String.format("%03d", id) + ", class=" + classMap.get(id)
                    + ", version=" + versionMap.get(id) + "]", JAMS.STANDARD);
        }

        return jamsModel;
    }

    private void registerComponent(ComponentDescriptor cd) {

        int id;
        String className = cd.getClazz().getName();
        String version = cd.getVersion();

        if (!idMap.containsKey(className)) {
            id = ++maxID;
            idMap.put(className, id);
            versionMap.put(id, version);
        }

    }

    /**
     * Recursively create all components used in the model and add them to the
     * component repository for easy access
     */
    public Component loadComponent(ModelNode rootNode) {

        String componentName, componentClassName, varName, varClassName = "", varValue;
        Component component, childComponent;
        Class<?> componentClazz = null, varClazz = null;
        ArrayList<Component> childComponentList = new ArrayList<Component>();

        ComponentDescriptor rootCd = (ComponentDescriptor) rootNode.getUserObject();

        if (!rootCd.isEnabled()) {
            return null;
        }

        componentName = rootCd.getInstanceName();

        componentClassName = rootCd.getClazz().getName();

        // check if a component with that name is already existing
        Component existingComponent = this.componentRepository.get(componentName);
        if (existingComponent != null) {
            throw new ModelSpecificationException(JAMS.i18n("Component_with_name_") + componentName
                    + JAMS.i18n("_is_already_exisiting_(") + existingComponent.getClass()
                    + JAMS.i18n(")._Please_make_sure_component_names_are_unique!_Stopping_model_loading!"));
        }

        registerComponent(rootCd);

        component = null;
        try {

            // create the Component object
            //jamsModel.getRuntime().println(componentClassName, JAMS.VERBOSE);
            // try to load the class
            componentClazz = loader.loadClass(componentClassName);

            // generate an instance of that class
            component = (Component) componentClazz.getDeclaredConstructor().newInstance();

            // do some basic initAll
            component.setModel(jamsModel);
            component.setInstanceName(componentName);

            if (component instanceof GUIComponent) {
                jamsModel.getRuntime().addGUIComponent((GUIComponent) component);
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
        } finally {
            if (component == null) {
                jamsModel.getRuntime().sendErrorMsg("An error occurred when trying to load component " + componentName);
                return null;
            }
        }

        // put the Component object into the component repository
        this.componentRepository.put(componentName, component);

        for (ComponentField cdField : rootCd.getComponentFields().values()) {

            // process components variable declarations
            varName = cdField.getName();
            // varClassName = element.getAttribute("class");

            // check if component variable exists
            try {
                Field field = JAMSTools.getField(componentClazz, varName);
                varClassName = field.getType().getName();

                if (field.isAnnotationPresent(JAMSVarDescription.class)) {

                    JAMSVarDescription jvd = field.getAnnotation(JAMSVarDescription.class);

                    varValue = cdField.getValue();

                    // set the var object if value provided directly
                    if (varValue != null) {

                        // create the var object
                        varClazz = Class.forName(varClassName);
                        Object variable;

                        if (varClazz.isArray() && JAMSData.class.isAssignableFrom(varClazz.getComponentType())) {
                            String[] varValues = varValue.split(";");
                            Class varComponentClazz = varClazz.getComponentType();
                            JAMSData[] array = (JAMSData[]) Array.newInstance(varComponentClazz, varValues.length);

                            for (int i = 0; i < varValues.length; i++) {
                                array[i] = dataFactory.createInstance(varComponentClazz);
                                array[i].setValue(varValues[i]);
                            }

                            variable = array;

                        } else if (JAMSData.class.isAssignableFrom(varClazz)) {

                            JAMSData jamsVar = dataFactory.createInstance(varClazz);
                            jamsVar.setValue(varValue);
                            variable = jamsVar;

                        } else {

                            throw new ModelSpecificationException(JAMS.i18n("Component_") + componentName + JAMS.i18n(":_variable_") + varName + JAMS.i18n(":_wrong_type!"));

                        }

                        // try to attach the variable to the component's field..
                        Object data;
                        try {
                            data = JAMSTools.setField(component, field, variable);
                        } catch (NoSuchMethodException nsme) {
                            throw new ModelSpecificationException(JAMS.i18n("Component_") + componentName + JAMS.i18n(":_variable_") + varName + JAMS.i18n(":_Access_exception!"), nsme);
                        }

                        // this field can be removed from the null field list
                        nullFields.get(component).remove(field);

                        if (data instanceof JAMSData) {
                            String id = componentName + "." + varName;
                            jamsModel.getRuntime().getDataHandles().put(id, (JAMSData) data);
                        }

                    }

                    if (cdField.getContext() != null) {

                        // obtain providing context name
                        String contextName = cdField.getContext().getInstanceName();

                        // get the context from the component repository
                        Component context = this.componentRepository.get(contextName);

                        // if specified context does not exist, throw exception
                        if (context == null) {
                            throw new ModelSpecificationException(JAMS.i18n("Component_") + "\"" + componentName + JAMS.i18n("_context_") + contextName + JAMS.i18n("_does_not_exist!"));
                        }

                        if (!(context instanceof Context)) {
                            throw new ModelSpecificationException(JAMS.i18n("Component_") + componentName + JAMS.i18n(":_Component_") + contextName + JAMS.i18n("_must_be_of_type_JAMSSpatialContext!"));
                        }

                        Context sc = (Context) context;
                        String attributeName;

                        attributeName = cdField.getAttribute();

                        if (jvd.access() == JAMSVarDescription.AccessType.READ) {
                            sc.addAccess(component, varName, attributeName, DataAccessor.READ_ACCESS);
                        } else if (jvd.access() == JAMSVarDescription.AccessType.WRITE) {
                            sc.addAccess(component, varName, attributeName, DataAccessor.WRITE_ACCESS);
                        } else if (jvd.access() == JAMSVarDescription.AccessType.READWRITE) {
                            sc.addAccess(component, varName, attributeName, DataAccessor.READWRITE_ACCESS);
                        }

                        nullFields.get(component).remove(field);

                    }

//                    jamsModel.getRuntime().println(JAMS.i18n("_var_declaration:_") + varName + " [class=" + varClassName + ", access=" + jvd.access() + ", connection=" + connType + "]", JAMS.VERBOSE);

                    /*
                     * if (jvd.trace() == JAMSVarDescription.UpdateType.INIT) {
                     * JAMSData data = (JAMSData) field.get(component); String
                     * id = componentName + "." + varName;
                     * jamsModel.getRuntime().getDataHandles().put(id, data); }
                     */
                } else {
                    throw new ModelSpecificationException(JAMS.i18n("Component_") + componentName + JAMS.i18n(":_variable_") + varName + JAMS.i18n("_can_not_be_accessed_(missing_annotation)!"));
                }

            } catch (NoSuchFieldException nsfe) {
                throw new ModelSpecificationException(JAMS.i18n("Component_") + componentName + JAMS.i18n(":_variable_") + varName + JAMS.i18n("_not_found!"), nsfe);
            } catch (ClassNotFoundException cnfe) {
                throw new ModelSpecificationException(JAMS.i18n("Component_") + componentName + JAMS.i18n(":_variable_class_") + varClassName + JAMS.i18n("_not_found!"), cnfe);
            } catch (IllegalArgumentException iae) {
                throw new ModelSpecificationException(JAMS.i18n("Component_") + componentName + JAMS.i18n(":_variable_") + varName + JAMS.i18n(":_wrong_type!"), iae);
            } catch (InstantiationException ie) {
                throw new ModelSpecificationException(JAMS.i18n("Component_") + componentName + JAMS.i18n(":_variable_") + varName + JAMS.i18n(":_Instantiation_exception!"), ie);
            } catch (IllegalAccessException iae) {
                throw new ModelSpecificationException(JAMS.i18n("Component_") + componentName + JAMS.i18n(":_variable_") + varName + JAMS.i18n(":_Access_exception!"), iae);
            } catch (Throwable ex) {
                jamsModel.getRuntime().handle(ex);
            }
        }

        if (rootCd instanceof ContextDescriptor) {

            if (!Context.class.isAssignableFrom(component.getClass())) {
                throw new ModelSpecificationException(JAMS.i18n("Attribute_tag_can_only_be_used_inside_context_components!_(component_") + componentName + JAMS.i18n(")"));
            }

            ContextDescriptor contextCd = (ContextDescriptor) rootCd;

            for (ContextAttribute attribute : contextCd.getStaticAttributes().values()) {
                ((Context) component).addAttribute(attribute.getName(), attribute.getType().getName(), attribute.getValue());
            }
        }

        // get element child nodes
        for (int index = 0; index < rootNode.getChildCount(); index++) {

            ModelNode childNode = (ModelNode) rootNode.getChildAt(index);

            // process child components of context components
            childComponent = loadComponent(childNode);
            if (childComponent != null) {
                childComponentList.add(childComponent);
            }
        }

        if (component instanceof Context) {
            ((Context) component).setComponents(childComponentList);
        }

        return component;
    }

    public HashMap<Component, ArrayList<Field>> getNullFields() {
        return nullFields;
    }

    public void setNullFields(HashMap<Component, ArrayList<Field>> nullFields) {
        this.nullFields = nullFields;
    }

    /**
     * @return the idMap
     */
    public HashMap<String, Integer> getIdMap() {
        return idMap;
    }

    class ModelSpecificationException extends JAMSException {

        public ModelSpecificationException(String errorMsg, Throwable e) {
            super(errorMsg, e);
        }

        public ModelSpecificationException(String errorMsg) {
            super(errorMsg);
        }
    }

    private ArrayList<Field> createMembers(Component component) throws IllegalAccessException, InstantiationException {

        Object o;
        Class dataType;
        ArrayList<Field> result = new ArrayList<Field>();

        Field[] fields = component.getClass().getFields();
        for (int i = 0; i < fields.length; i++) {
            o = fields[i].get(component);
            dataType = fields[i].getType();

            if (JAMSData.class.isAssignableFrom(dataType) && fields[i].isAnnotationPresent(JAMSVarDescription.class)) {

                JAMSData dataObject = (JAMSData) o;
                JAMSVarDescription jvd = fields[i].getAnnotation(JAMSVarDescription.class);

                // get variable object or create one if not existing
                if ((dataObject == null) && (!jvd.defaultValue().equals(JAMSVarDescription.NULL_VALUE) || (jvd.access() == JAMSVarDescription.AccessType.WRITE))) {
                    dataObject = dataFactory.createInstance(dataType);
                    fields[i].set(component, dataObject);
                } else {
                    result.add(fields[i]);
                }

                // set value for data object if defined
                if (!jvd.defaultValue().equals(JAMSVarDescription.NULL_VALUE)) {
                    try {
                        dataObject.setValue(jvd.defaultValue());
                    } catch (NumberFormatException nfe) {
                        throw new JAMSException("Invalid default value (" + jvd.defaultValue() + ") for field " + fields[i].getName() + " in component " + component.getInstanceName(), nfe);
                    }
                }

            }
        }
        return result;
    }
}
