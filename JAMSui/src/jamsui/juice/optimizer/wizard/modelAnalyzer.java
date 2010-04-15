/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jamsui.juice.optimizer.wizard;

import jams.JAMSProperties;
import jams.data.JAMSDataFactory;
import jams.model.JAMSVarDescription;
import jams.model.JAMSVarDescription.AccessType;
import jams.runtime.StandardRuntime;
import jams.tools.JAMSTools;
import jamsui.juice.optimizer.wizard.Tools.AttributeWrapper;
import jamsui.juice.optimizer.wizard.Tools.ComponentWrapper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Christian Fischer
 */
public class modelAnalyzer {
    public static final int COLLECT_READATTRIBUTES = 0;
    public static final int COLLECT_WRITEATTTRIBUTES = 1;
    
    private static Set<String> getAttributeList(Node root, ComponentWrapper component, StandardRuntime rt, int mode) {
        NodeList childs = root.getChildNodes();
        Element parent = (Element) root;
        HashSet<String> list = new HashSet<String>();
        for (int i = 0; i < childs.getLength(); i++) {
            Node child = childs.item(i);
            if (child.getNodeName().equals("contextcomponent")) {
                Element elem = (Element) child;
                list.addAll(getAttributeList(child, new ComponentWrapper(
                        elem.getAttribute("name"),
                        elem.getAttribute("name"),
                        true), rt, mode));
            }
            if (child.getNodeName().equals("component")) {
                Element elem = (Element) child;
                list.addAll(getAttributeList(child, new ComponentWrapper(
                        elem.getAttribute("name"),
                        parent.getAttribute("name"),
                        false), rt, mode));
            }
            if (child.getNodeName().equals("var")) {
                Element elem = (Element) child;
                String context = elem.getAttribute("context");
                String name = elem.getAttribute("name");
                String attr = elem.getAttribute("attribute");
                if (attr.equals("")) {
                    attr = null;
                }
                if (context.equals("")) {
                    context = null;
                }
                if (name.equals("")) {
                    name = null;
                }
                if (context == null && attr != null) {
                    context = component.componentContext;
                }
                Class clazz = null;
                Field field = null;
                boolean isDouble = true;
                try {
                    clazz = rt.getClassLoader().loadClass(parent.getAttribute("class"));
                    if (clazz != null) {
                        field = JAMSTools.getField(clazz, name);
                    }
                } catch (Exception e) {
                    System.out.println(e.toString() + parent.getAttribute("class"));
                    continue;
                }
                if (field == null) {
                    System.out.println("field is null" + clazz);
                    continue;
                }
                field.getAnnotation(JAMSVarDescription.class);
                Class type = field.getType();
                if (!type.getName().equals("jams.data.JAMSDouble")) {
                    isDouble = false;
                }
                JAMSVarDescription jvd = field.getAnnotation(JAMSVarDescription.class);

                if (isDouble && ((mode == COLLECT_READATTRIBUTES  && (               jvd.access() == AccessType.READ  || attr == null)) ||
                                 (mode == COLLECT_WRITEATTTRIBUTES && (jvd == null || jvd.access() == AccessType.WRITE || jvd.access() == AccessType.READWRITE)))) {
                    AttributeWrapper wrap = new AttributeWrapper(
                            name,
                            attr,
                            parent.getAttribute("name"),
                            context);
                    if (wrap.contextName == null) {
                        if (wrap.attributeName != null) {
                            list.add(wrap.componentName + "." + wrap.attributeName);
                        } else {
                            list.add(wrap.componentName + "." + wrap.variableName);
                        }
                    } else {
                        if (wrap.attributeName != null) {
                            list.add(wrap.contextName + "." + wrap.attributeName);
                        } else {
                            list.add(wrap.contextName + "." + wrap.variableName);
                        }
                    }

                }
            }
            if (child.getNodeName().equals("attribute")) {
                Element elem = (Element) child;
                String attr = elem.getAttribute("name");
                String context = parent.getAttribute("name");
                String clazz = elem.getAttribute("class");

                if (clazz.equals("jams.data.JAMSDouble")) {
                    list.add(context + "." + attr);
                }
            }
        }
        return list;
    }
    
    public static void modelAnalyzer(String propertyFile, File modelFile) {
        DocumentLoader loader = new DocumentLoader();
        loader.modelFile = JAMSDataFactory.createString();
        loader.modelFile.setValue(modelFile.getName());
        loader.workspaceDir = JAMSDataFactory.createString();
        if (modelFile.getParent()!=null)
            loader.workspaceDir.setValue(modelFile.getParent());
        else
            loader.workspaceDir.setValue("");
        loader.modelDoc = JAMSDataFactory.createDocument();

        String errorString = loader.init_withResponse();
        Document loadedModel = loader.modelDoc.getValue();
        if (loadedModel == null) {
            System.err.println(errorString);
            return;

        }
        //default properties      
        JAMSProperties properties = JAMSProperties.createProperties();
        try {
            properties.load(propertyFile);
        } catch (IOException e) {
            System.err.println("Cant find property file, because:" + e.toString());
        } catch (Exception e2) {
            System.err.println("Error while loading property file, because: " + e2.toString());
        }

        StandardRuntime rt = new StandardRuntime();

        rt.loadModel(loadedModel, properties);
        if (rt.getDebugLevel() >= 3) {
            if (rt.getErrorLog().length()>2)
                System.err.println(rt.getErrorLog());
            System.out.println(rt.getInfoLog());
        }

        Node root = jamsui.juice.optimizer.wizard.Tools.getModelNode(loadedModel);

        //Element rootElement = (Element)root;
        Set<String> parameterList = getAttributeList(root, null, rt, COLLECT_READATTRIBUTES);
        Set<String> objectiveList = getAttributeList(root, null, rt, COLLECT_WRITEATTTRIBUTES);
        
        parameterList.removeAll(objectiveList);
        try {
            BufferedWriter paramOut = new BufferedWriter(new FileWriter("model_params.dat"));
            Iterator<String> iter1 = parameterList.iterator();
            while(iter1.hasNext()){                    
                paramOut.write(iter1.next() + "\n");
            }

            paramOut.close();

            BufferedWriter objectiveOut = new BufferedWriter(new FileWriter("model_eff.dat"));
            Iterator<String> iter2 = objectiveList.iterator();
            while(iter2.hasNext()){
                objectiveOut.write(iter2.next() + "\n");
            }

            objectiveOut.close();
        } catch (Exception e) {
            System.err.println(e);e.printStackTrace();
        }

    }
}
