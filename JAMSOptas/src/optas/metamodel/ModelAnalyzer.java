/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.metamodel;

import jams.JAMSProperties;
import jams.data.JAMSDataFactory;
import jams.data.JAMSDouble;
import jams.model.JAMSVarDescription;
import jams.model.JAMSVarDescription.AccessType;
import jams.runtime.StandardRuntime;
import jams.tools.JAMSTools;
import optas.metamodel.Tools.Range;
import optas.metamodel.ModelModifier.WizardException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Christian Fischer
 */
public class ModelAnalyzer {
    public static final int COLLECT_READATTRIBUTES = 0;
    public static final int COLLECT_WRITEATTTRIBUTES = 1;
    
    public String error = null;
    StandardRuntime rt;
    Node root;

    Document doc;
    JAMSProperties properties;

    public ModelAnalyzer(JAMSProperties propertyFile, Document modelFile){
        init(propertyFile, modelFile);
    }
    public ModelAnalyzer(File propertyFile, File modelFile) throws WizardException{
        //default properties
        properties = JAMSProperties.createProperties();
        try {
            properties.load(propertyFile.getAbsolutePath());
        } catch (IOException e) {
            //setError("Cant find property file, because:" + e.toString());
        } catch (Exception e2) {
            //setError("Error while loading property file, because: " + e2.toString());
        }

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
            throw new WizardException(errorString);
        }        
        doc = loadedModel;
        init(properties, doc);
    }

    public Document getModelDoc(){
        return doc;
    }
    public JAMSProperties getProperties(){
        return properties;
    }
    private static HashMap<String, Range> getDefaultRangeMap(Node launcherNode) {
        HashMap<String, Range> map = new HashMap<String, Range>();

        NodeList childs = launcherNode.getChildNodes();

        if (!(launcherNode.getNodeName().equals("launcher")
                || launcherNode.getNodeName().equals("group")
                || launcherNode.getNodeName().equals("subgroup")
                || launcherNode.getNodeName().equals("property")
                || launcherNode.getNodeName().equals("model"))) {
            return new HashMap<String, Range>();
        }
        Element parent = (Element) launcherNode;
        if (parent.getNodeName().equals("property")) {
            String attribute = parent.getAttribute("attribute");
            String component = parent.getAttribute("component");
            String range = parent.getAttribute("range");
            if (range != null) {
                String ranges[] = range.split(";");
                if (ranges.length == 2) {
                    map.put(component + "." + attribute, new Range(Double.parseDouble(ranges[0]), Double.parseDouble(ranges[1])));
                }
            }
        }

        for (int i = 0; i < childs.getLength(); i++) {
            map.putAll(getDefaultRangeMap(childs.item(i)));
        }

        return map;
    }

    private static Set<AttributeWrapper> getAttributeList(Node root, ComponentWrapper component, StandardRuntime rt, int mode) {
        NodeList childs = root.getChildNodes();
        Element parent = (Element) root;
        String className = "";
        HashSet<AttributeWrapper> list = new HashSet<AttributeWrapper>();
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
                try {

                    if (parent.getTagName().equals("model")) {
                        className = "jams.model.JAMSModel";
                    } else {
                        className = parent.getAttribute("class");
                    }

                    clazz = rt.getClassLoader().loadClass(className);
                    if (clazz != null) {
                        field = JAMSTools.getField(clazz, name);
                    }
                } catch (Exception e) {
                    System.out.println(e.toString() + className);
                    continue;
                }
                if (field == null) {
                    System.out.println("field is null" + clazz);
                    continue;
                }
                Class type = field.getType();

                //notice: jvd can be null if we are accessing a context
                if (type.isAssignableFrom(JAMSDouble.class)) {
                    JAMSVarDescription jvd = field.getAnnotation(JAMSVarDescription.class);
                    if (((mode == COLLECT_READATTRIBUTES   && (jvd == null || jvd.access() == AccessType.READ  || attr == null)) ||
                         (mode == COLLECT_WRITEATTTRIBUTES && (jvd == null || jvd.access() == AccessType.WRITE || jvd.access() == AccessType.READWRITE)))) {
                    list.add(new AttributeWrapper(name,attr,
                            parent.getAttribute("name"),context));
                    }
                }
                if (type.isAssignableFrom(JAMSDouble[].class)){
                    StringTokenizer tok = new StringTokenizer(attr,";");
                    while(tok.hasMoreTokens()){
                        JAMSVarDescription jvd = field.getAnnotation(JAMSVarDescription.class);
                        String subAttr = tok.nextToken();
                        if (((mode == COLLECT_READATTRIBUTES  && ( jvd == null || jvd.access() == AccessType.READ  || attr == null)) ||
                             (mode == COLLECT_WRITEATTTRIBUTES && (jvd == null || jvd.access() == AccessType.WRITE || jvd.access() == AccessType.READWRITE)))) {
                            list.add(new AttributeWrapper(name,subAttr,
                                parent.getAttribute("name"),context));
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
                    list.add(new AttributeWrapper(
                            null,
                            attr,
                            null,
                            context));
                }
            }
        }
        return list;
    }

    public StandardRuntime getRuntime(){
        return rt;
    }
    private boolean init(JAMSProperties propertyFile, Document modelFile){                
        properties = propertyFile;
        this.doc = modelFile;

        rt = new StandardRuntime(properties);
        try{
            rt.loadModel(doc, null);
        }catch(Throwable t){
            if (rt.getDebugLevel() >= 3) {
                if (rt.getErrorLog().length()>2){
                    setError("++++Error Log+++++\n" + rt.getErrorLog() + 
                             "+++++Info Log+++++\n" + rt.getInfoLog());            
                    return false;
                }
            }
        }

        if (rt.getDebugLevel() >= 3) {
            if (rt.getErrorLog().length()>2){
                setError(rt.getErrorLog());            
                return false;
            }
        }
        root = optas.metamodel.Tools.getModelNode(doc);
        return true;
    }

    private void setError(String error){
        this.error = error;
    }
    public void clearError(){
        error = null;
    }

    private SortedSet<Parameter> getParameters(Node root, StandardRuntime rt) {
        SortedSet<Parameter> result = new TreeSet<Parameter>();

        Set<AttributeWrapper> parameterList = getAttributeList(root, null, rt, COLLECT_READATTRIBUTES);
        Set<AttributeWrapper> objectiveList = getAttributeList(root, null, rt, COLLECT_WRITEATTTRIBUTES);
        parameterList.removeAll(objectiveList);

        HashMap<String, Range> defaultRangeMap = getDefaultRangeMap(root);
        Iterator<AttributeWrapper> iter1 = parameterList.iterator();
        while (iter1.hasNext()) {
            AttributeWrapper variable = iter1.next();
            Range range = defaultRangeMap.get(variable.getName());
            result.add(new Parameter(variable, range));
        }
        return result;
    }

    private SortedSet<AttributeWrapper> getObjectives(Node root, StandardRuntime rt) {
        SortedSet<AttributeWrapper> result = new TreeSet<AttributeWrapper>();
        Set<AttributeWrapper> objectiveList = getAttributeList(root, null, rt, COLLECT_WRITEATTTRIBUTES);
        Iterator<AttributeWrapper> iter1 = objectiveList.iterator();
        while (iter1.hasNext()) {
            result.add(iter1.next());
        }
        return result;
    }
    public SortedSet<AttributeWrapper> getAttributes() {
        SortedSet<AttributeWrapper> r = getObjectives();
        r.addAll(getParameters());
        return r;
    }

    public SortedSet<Parameter> getParameters() {        
        return getParameters(root,rt);
    }
    public SortedSet<AttributeWrapper> getObjectives() {
        return getObjectives(root,rt);
    }
    public static String modelAnalyzer(File propertyFile, File modelFile) throws WizardException {
        ModelAnalyzer analyzer = new ModelAnalyzer(propertyFile, modelFile);
        return modelAnalyzer(analyzer.getProperties(), analyzer.getModelDoc());
    }
    //for compability with php interface
    public static String modelAnalyzer(JAMSProperties propertyFile, Document modelFile) {
        ModelAnalyzer analyzer = new ModelAnalyzer(propertyFile, modelFile);

        SortedSet<Parameter> parameters = analyzer.getParameters();
        SortedSet<AttributeWrapper> objectives = analyzer.getObjectives();
        
        String paramResult = "";
        try {
            BufferedWriter paramOut = new BufferedWriter(new FileWriter("model_params.dat"));
            Iterator<Parameter> iter1 = parameters.iterator();
            while(iter1.hasNext()){   
                Parameter variable = iter1.next();
                paramOut.write(variable.getName() + "\t" + variable.getLowerBound() + "\t" + variable.getUpperBound() + "\n");
            }
            paramOut.close();

            BufferedWriter objectiveOut = new BufferedWriter(new FileWriter("model_eff.dat"));
            Iterator<AttributeWrapper> iter2 = objectives.iterator();
            while(iter2.hasNext()){
                objectiveOut.write(iter2.next().getName() + "\n");
            }
            objectiveOut.close();
        } catch (Exception e) {
            System.err.println(e);e.printStackTrace();
        }

        return paramResult;
    }
}
