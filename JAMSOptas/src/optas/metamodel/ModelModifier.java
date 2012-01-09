package optas.metamodel;

import jams.data.JAMSDataFactory;
import jams.io.XMLProcessor;
import jams.runtime.StandardRuntime;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import jams.JAMSProperties;
import org.w3c.dom.Document;
import jams.JAMS;
import jams.model.Model;
import jams.tools.FileTools;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import optas.metamodel.ModificationExecutor.Modification;
import optas.metamodel.ModificationExecutor.WrapElement;
import optas.optimizer.HRUReducer;
import optas.optimizer.management.AdvancedOptimizationController;
import org.w3c.dom.NodeList;

/**
 *
 * @author Christian Fischer
 */
public class ModelModifier {

    final String OPTIMIZER_CONTEXT_NAME = "optimizer";
    final String OBJECTIVE_COMPONENT_NAME = "objective";
    StandardRuntime rt;
    Document loadedModel;
    JAMSProperties properties;
    //HashSet<String> removedComponents = new HashSet<String>();
    ArrayList<AttributeWrapper> objectiveList = new ArrayList<AttributeWrapper>();
    OptimizationDescriptionDocument odd;
    BufferedWriter stream;

    private String schemaName = "generic.odd";

    /**
     * @return the schemaName
     */
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * @param schemaName the schemaName to set
     */
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    static public class WizardException extends Exception {

        String e;

        WizardException(String desc) {
            e = desc;
        }

        @Override
        public String toString() {
            return e;
        }
    }
    final static int kernelMap[] = {2, 3, 5, 6, 7, 8, 12, 13, 15, 16};
    ArrayList<Modification> modificationList = new ArrayList<Modification>();
    
    public ModelModifier(JAMSProperties properties, Document doc, OutputStream stream) throws WizardException {
        if (stream != null)
            this.stream = new BufferedWriter(new OutputStreamWriter(stream));
        else{
            this.stream = new BufferedWriter(new OutputStreamWriter(System.out));
        }

        this.properties = properties;
        this.loadedModel = doc;

        init();
    }

    public ModelModifier(File propertyFile, File modelFile) throws WizardException {
        //default properties
        properties = JAMSProperties.createProperties();
        try {
            properties.load(propertyFile.getAbsolutePath());
        } catch (IOException e) {
            throw new WizardException(JAMS.i18n("error_could_not_load_JAMS_property_file") + e.toString());
        } catch (Exception e) {
            throw new WizardException(JAMS.i18n("error_could_not_load_JAMS_property_file") + e.toString());
        }

        DocumentLoader loader = new DocumentLoader();
        loader.modelFile = JAMSDataFactory.createString();
        loader.modelFile.setValue(modelFile.getName());
        loader.workspaceDir = JAMSDataFactory.createString();
        if (modelFile.getParentFile() != null) {
            loader.workspaceDir.setValue(modelFile.getParentFile().getAbsolutePath());
        } else {
            loader.workspaceDir.setValue("");
        }
        loader.modelDoc = JAMSDataFactory.createDocument();
        String errorString = loader.init_withResponse();
        loadedModel = loader.modelDoc.getValue();
        if (loadedModel == null) {
            throw new WizardException("error_while_loading_model_file:" + errorString);
        }

        init();
    }

    private void init() throws WizardException {
        rt = new StandardRuntime(properties);
        rt.loadModel(loadedModel, null);
        if (rt.getDebugLevel() >= 3) {
            if (rt.getErrorLog().length() > 2) {
                throw new WizardException(rt.getErrorLog());
            }
        }
    }

    private void log(String msg) {
        try {
            this.stream.write(msg);
        } catch (IOException ioe) {
        }
    }

    public void setOptimizationDescriptionDocument(OptimizationDescriptionDocument odd) {
        this.odd = odd;
    }

    public void setOptimizationDescriptionDocument(File oddFile) throws WizardException {
        try {
            XMLDecoder encoder = new XMLDecoder(
                    new BufferedInputStream(
                    new FileInputStream(oddFile)));

            setOptimizationDescriptionDocument((OptimizationDescriptionDocument) encoder.readObject());
            encoder.close();
        } catch (IOException ioe) {
            log("Failed to load optimization description document (" + oddFile + ")!\n" + ioe.toString());
            return;
        }
    }
    
    String[] entityReaderClasses = new String[]{"org.unijena.j2k.io.StandardEntityReader"};

    private Element getEntityReader(Document doc, String entityReaderName) {
        if (entityReaderName != null) {
            ArrayList<Element> list = Tools.getNodeByName(doc, entityReaderName);
            if (list.isEmpty()) {
                //error component does not exist
            } else {
                return list.get(0);
            }
        }
        for (int i = 0; i < entityReaderClasses.length; i++) {
            ArrayList<Element> list = Tools.getNodeByClass(doc, entityReaderClasses[i]);
            if (list.size() > 1) {
                //error more than one option
            }
            if (!list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    private String replaceHRUFileName(Document doc, String newFileName, String entityReaderName) {

        Element entityReaderNode = getEntityReader(doc, entityReaderName);

        NodeList list = entityReaderNode.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Element child = null;
            if (list.item(i) instanceof Element)
                child = (Element) list.item(i);
            else continue;
            if (child.getAttribute("name") == null) {
                continue;
            }
            if (child.getAttribute("name").equals("hruFileName")) {
                String fileName = child.getAttribute("value");
                if (fileName != null) {
                    child.setAttribute("value", newFileName);
                    return fileName;
                }
            }
        }
        return null;
    }

    private Element createSpatialRelaxationComponent(Document doc, String relaxationParameter) throws WizardException {
        String newFileName = "hrus_generated_" + (new Date()).getTime() + ".dat";

        String hruFileName = replaceHRUFileName(doc, newFileName, null);

        Element spatialRelaxationComponent = doc.createElement("component");
        spatialRelaxationComponent.setAttribute("class", HRUReducer.class.getName());
        spatialRelaxationComponent.setAttribute("name", "SpatialRelaxation");

        optas.metamodel.Tools.addAttribute(spatialRelaxationComponent, "srcHRUFileName", hruFileName,
                null, true);
        optas.metamodel.Tools.addAttribute(spatialRelaxationComponent, "dstHRUFileName", newFileName,
                null, true);
        optas.metamodel.Tools.addAttribute(spatialRelaxationComponent, "method", "1",
                null, true);
        optas.metamodel.Tools.addAttribute(spatialRelaxationComponent, "mergeRate", relaxationParameter,
                OPTIMIZER_CONTEXT_NAME, false);

        return spatialRelaxationComponent;
    }

    private ArrayList<Modification> addSpatialRelaxationComponent(Document doc, String attribute ) throws WizardException {
        Element spatialRelaxationComponent = createSpatialRelaxationComponent(doc, attribute);
        Element entityReaderNode = this.getEntityReader(doc, null);

        ArrayList<Modification> list = new ArrayList<Modification>();
        if (entityReaderNode!=null){
            ModificationExecutor.InsertBefore insertElement = new ModificationExecutor.InsertBefore(spatialRelaxationComponent, entityReaderNode);
            list.add(insertElement);
        }else{
            log("warning: could not find any entity reader. spatial relaxation is not possible.");
        }
        
        return list;
    }

    private ArrayList<Modification> configOutput(Document doc, Collection<AttributeWrapper> set) throws WizardException {
        ArrayList<ModificationExecutor.Modification> actionList = new ArrayList<Modification>();

        Map<String, HashSet<String>> outputContexts = new HashMap<String, HashSet<String>>();
       
        for (AttributeWrapper a : set) {
            String attr = a.getAttributeName();
            if (attr != null) {
                String context = a.getContextName();
                if (context != null) {
                    if (!outputContexts.containsKey(context)) {
                        outputContexts.put(context, new HashSet<String>());
                    }
                    outputContexts.get(context).add(attr);
                }
            }
        }
        Iterator<String> iter = outputContexts.keySet().iterator();

        Node model = Tools.getModelNode(doc);

        Element datastoreNode = doc.createElement("datastores");

        while (iter.hasNext()) {
            String context = iter.next();
            
            Element root = doc.createElement("outputdatastore");
            Element trace = doc.createElement("trace");
            root.setAttribute("context", context);
            root.appendChild(trace);

            HashSet<String> attr = outputContexts.get(context);
            Iterator<String> attrIter = attr.iterator();

            while (attrIter.hasNext()) {
                Element attrElement = doc.createElement("attribute");
                attrElement.setAttribute("id", attrIter.next());
                trace.appendChild(attrElement);
            }
            datastoreNode.appendChild(root);
        }
        actionList.add(new ModificationExecutor.InsertElement((Element)model, datastoreNode));
        return actionList;
    }

    private String getStartValueString() {
        String startValues = "";
        for (Parameter p : odd.getParameter().values()) {
            if (!p.isStartValueValid()) {
                return null;
            }
            startValues += p.getStartValue() + ";";
        }
        return startValues;
    }

    private String getBoundaryString() {
        String boundaries = "";
        for (Parameter p : odd.getParameter().values()) {
            boundaries += "[" + p.getLowerBound() + ">" + p.getUpperBound() + "];";
        }
        return boundaries;
    }

    private Element createOptimizerComponent(Document doc, String objectiveList, String parameterList, int mainObjectiveIndex, String relaxationAttribute) {
        log(JAMS.i18n("add_optimization_context"));
        //optimierer bauen
        Element optimizerContext = doc.createElement("contextcomponent");
        optimizerContext.setAttribute("class", AdvancedOptimizationController.class.getName());
        optimizerContext.setAttribute("name", OPTIMIZER_CONTEXT_NAME);

        //TODO .. scheme.odd
        optas.metamodel.Tools.addAttribute(optimizerContext, "optimizationDescriptionFile", getSchemaName(),
                null, true);

        optas.metamodel.Tools.addAttribute(optimizerContext, "maxn", Integer.toString(Integer.MAX_VALUE),
                null, true);

        optas.metamodel.Tools.addAttribute(optimizerContext, "boundaries", getBoundaryString(),
                null, true);

        if (getStartValueString() != null) {
            optas.metamodel.Tools.addAttribute(optimizerContext, "startValue", getStartValueString(),
                    null, true);
        }

        optas.metamodel.Tools.addAttribute(optimizerContext, "effMethodName", objectiveList,
                null, true);

        optas.metamodel.Tools.addAttribute(optimizerContext, "effValue", objectiveList,
                OPTIMIZER_CONTEXT_NAME, false);

        String str = "" + mainObjectiveIndex;
        optas.metamodel.Tools.addAttribute(optimizerContext, "mainObjective", str,
                null, true);


        optas.metamodel.Tools.addAttribute(optimizerContext, "parameterIDs", parameterList,
                OPTIMIZER_CONTEXT_NAME, false);
        optas.metamodel.Tools.addAttribute(optimizerContext, "parameterNames", parameterList,
                null, true);

        optas.metamodel.Tools.addAttribute(optimizerContext, "relaxationParameter", relaxationAttribute,
                OPTIMIZER_CONTEXT_NAME, false);

        return optimizerContext;
    }

    private ArrayList<Modification> addParameter(Collection<Parameter> list,Node root, String optimizerContextName){
        ArrayList<Modification> actionList = new ArrayList<Modification>();
        
        for (Parameter p:list){            
            actionList.add(new ModificationExecutor.ReplaceAttribute(p, p.getChildName(), optimizerContextName));
        }
        return actionList;
    }

    public ModificationExecutor modifyModel() throws WizardException {
        log("checking document");        
        for (Optimization o : this.odd.getOptimization()){
            if (o.getObjective().isEmpty()){
                log("failure: optimization " + o.getName() + " has no objective");
                return null;
            }
        }

        init();

        Model model = rt.getModel();

        Document doc = (Document) loadedModel.cloneNode(true);
        Node root = (Node) doc.getDocumentElement();

        if (!Tools.changeWorkspace(root, odd.getWorkspace())) {
            throw new WizardException(JAMS.i18n("unable_to_change_workspace"));
        }

        ArrayList<Objective> effList = new ArrayList<Objective>();
        effList.addAll(odd.getObjective().values());

        log("optimizing model structure");
        ModelOptimizer modelOptimizer = new ModelOptimizer(doc, model, effList);
        modelOptimizer.optimize(odd.isRemoveGUIComponents(), odd.isRemoveRedundantComponents());
        this.modificationList.addAll(modelOptimizer.getModifications());

        log("analysing objectives");
        ObjectiveAnalyzer objectiveAnalyser = new ObjectiveAnalyzer(effList, OPTIMIZER_CONTEXT_NAME);
        objectiveAnalyser.analyse(doc);
        this.modificationList.addAll(objectiveAnalyser.getModifications());

        log("creating spatial relaxation");
        String relaxationAttribute = "merge_rate";
        this.modificationList.addAll(addSpatialRelaxationComponent(doc, relaxationAttribute));

        log("creating optimizer");
        this.modificationList.addAll(addParameter(odd.getParameter().values(), root, OPTIMIZER_CONTEXT_NAME));
        String parameterString = "";
        for (Parameter p : odd.getParameter().values()){
            parameterString += p.getChildName() + ";";
        }
        int mainObjectiveIndex = objectiveAnalyser.getObjectiveList().size()-1;
        
        Element optimizer = createOptimizerComponent(doc,objectiveAnalyser.getAttributeList(), parameterString, mainObjectiveIndex, relaxationAttribute);
        WrapElement placeOptimizerAction = new WrapElement(optimizer, (Element)XMLProcessor.getFirstComponent(root));
        this.modificationList.add(placeOptimizerAction);

        TreeSet<AttributeWrapper> exportAttributes = new TreeSet<AttributeWrapper>();
        exportAttributes.add(new AttributeWrapper(null, relaxationAttribute, null, OPTIMIZER_CONTEXT_NAME));
        exportAttributes.addAll(objectiveAnalyser.getObjectiveList());

        for (Parameter p : odd.getParameter().values()){
            AttributeWrapper p2 = new AttributeWrapper(null, p.getChildName(), null, OPTIMIZER_CONTEXT_NAME); //this crap .. generalisieren
            exportAttributes.add(p2);
        }        
        this.modificationList.addAll(configOutput(doc, exportAttributes));
        //some adjustments like file separators and data-caching
        Tools.doAdjustments(doc);

        return new ModificationExecutor(doc, this.modificationList);
    }



    public static Document modelModifier(JAMSProperties propertyFile, Document modelFile, OptimizationDescriptionDocument odd) throws WizardException {
        ModelModifier modifyModel = new ModelModifier(propertyFile, modelFile, null);
        modifyModel.setOptimizationDescriptionDocument(odd);

        Document doc = modifyModel.modifyModel().execute();
        /*modifyModel.writeGDLFile("graph.gdl");
        try {
        XMLTools.writeXmlFile(doc, "optimization.jam");
        } catch (Exception e) {
        return;
        }
        System.exit(0);*/
        return doc;
    }

    public static void modelModifier(File propertyFile, File modelFile, File oddFile, File workspace) throws WizardException {
        ModelModifier modifyModel = new ModelModifier(propertyFile, modelFile);
        modifyModel.setOptimizationDescriptionDocument(oddFile);
        modifyModel.odd.setWorkspace(workspace.getAbsolutePath());
        Document doc = modifyModel.modifyModel().execute();

        /*modifyModel.writeGDLFile(workspace.getAbsolutePath() + "/graph.gdl");
        try {
            XMLTools.writeXmlFile(doc, workspace.getAbsolutePath() + "/optimization.jam");
        } catch (Exception e) {
            System.out.println(e.toString());
            return;
        }*/
        //copy odd file
        try {
            FileTools.copyFile(oddFile.getAbsolutePath(), workspace.getAbsolutePath() + "/scheme.odd");
        } catch (IOException ioe) {
            System.out.println("Could not copy odd file!\n" + ioe.toString());
            ioe.printStackTrace();
        }
        System.exit(0);
    }
}
