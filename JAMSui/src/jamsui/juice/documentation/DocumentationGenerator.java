package jamsui.juice.documentation;

import jams.JAMSProperties;
import jams.model.JAMSComponentDescription;
import jams.tools.StringTools;
import jams.tools.XMLTools;
import jamsui.juice.JUICE;
import jamsui.juice.documentation.DocumentationException.DocumentationExceptionCause;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author sa63kul
 */
public class DocumentationGenerator {

    private final String AnnotationFileName = "Component_Annotation.";
    private final String templateFileName = "template.xml";
    private final String parameterFileName = "parameter.xml";

    private final String MODELSTRUCTURE_FILENAME = "Modellkomponenten.xml";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
    SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");        
    ClassLoader loader = null;    
    TreeMap<String, String> bibEntrySet = new TreeMap<>();
    TreeMap<String, String> automaticComponentDescriptions = new TreeMap<>();


    private boolean debug = true;
    private void log(String msg) {
        if (debug) {
            System.out.println(msg);
        }
    }

    private static String getAnnotation(Class<?> clazz, String jarFileName) throws ClassNotFoundException, DocumentationException, NoClassDefFoundError {
        if (!jams.model.Component.class.isAssignableFrom(clazz)) {
            return null;
        }

        String compDesc = Tools.getTemplate(Tools.Template.Component);

        //compDesc = compDesc.replace("%package%", clazz.getPackage().toString());
        compDesc = compDesc.replace("%class%", clazz.getSimpleName());
        compDesc = compDesc.replace("%jarFile%", jarFileName);

        JAMSComponentDescription jcd = (JAMSComponentDescription) clazz.getAnnotation(JAMSComponentDescription.class);

        if (jcd != null) {
            compDesc = compDesc.replace("%title%", jcd.title());
            compDesc = compDesc.replace("%author%", jcd.author().replace("&", " "+Bundle.resources.getString("and")+" " ));
            compDesc = compDesc.replace("%date%", jcd.date());
            compDesc = compDesc.replace("%description%", jcd.description());
            compDesc = compDesc.replace("%version%", jcd.version());
        } else {
            compDesc = compDesc.replace("%title%", "[none]");
            compDesc = compDesc.replace("%author%", "[none]");
            compDesc = compDesc.replace("%date%", "[none]");
            compDesc = compDesc.replace("%description%", "[none]");
            compDesc = compDesc.replace("%process%", "[none]");
            compDesc = compDesc.replace("%version%", "[none]");
            compDesc = compDesc.replace("%mail%", "[none]");
        }

        String variables = "";
        boolean interfaceFound = false;

        Field field[] = clazz.getFields();
        
        for (int i = 0; i < field.length; i++) {
            jams.model.JAMSVarDescription jvd = (jams.model.JAMSVarDescription) field[i].getAnnotation(jams.model.JAMSVarDescription.class);
            String variableTemplate = Tools.getTemplate(Tools.Template.Variable);
            variableTemplate = variableTemplate.replace("%name%", field[i].getName());
            if (jvd != null) {
                interfaceFound = true;
                String tmp = field[i].getType().getName();
                tmp = tmp.replace('$', ' ');
                variableTemplate = variableTemplate.replace("%type%", tmp);
                variableTemplate = variableTemplate.replace("%access%", jvd.access().toString());
                variableTemplate = variableTemplate.replace("%update%", jvd.update().toString());
                String desc = jvd.description();
                variableTemplate = variableTemplate.replace("%description%", desc);
                variableTemplate = variableTemplate.replace("%unit%", jvd.unit());
                variableTemplate = variableTemplate.replace("%upperBound%", Double.toString(jvd.upperBound()));
                variableTemplate = variableTemplate.replace("%lowerBound%", Double.toString(jvd.lowerBound()));
                variableTemplate = variableTemplate.replace("%defaultValue%", jvd.defaultValue().toString());
                variables += "\n" + variableTemplate;
            }
        }
        if (!interfaceFound) {
            variables = "No data found!";
        }

        compDesc = compDesc.replace("%componentvars%", variables);
        return compDesc;
    }

    private void processAnnotations(File documentationOutputDir, File jarFile) throws DocumentationException {
        log("generating annotation documentation");

        JarFile jFile = null;
        String jarFileName = jarFile.getName();

        try {
            jFile = new JarFile(jarFile);
        } catch (IOException ioe) {
            throw new DocumentationException(DocumentationExceptionCause.invalidJarFile, jarFile.getName());
        }

        try {
            loader = new URLClassLoader(new URL[]{jarFile.toURL()}, ClassLoader.getSystemClassLoader());
        } catch (MalformedURLException mURLe) {
            mURLe.printStackTrace();
            throw new DocumentationException(DocumentationExceptionCause.unknownError, mURLe.toString());
        }

        Enumeration<JarEntry> jarentries = jFile.entries();

        while (jarentries.hasMoreElements()) {
            ZipEntry entry = jarentries.nextElement();

            if (entry.getName().endsWith(".xml")) {
                String filename = entry.getName().replaceAll("/", ".");
                Tools.extractZipEntry(jFile, entry, new File(documentationOutputDir, filename));
            } else if (entry.getName().endsWith(".png")) {
                String filename = (new File(entry.getName())).getName();
                try {
                    Tools.copyFile(jFile.getInputStream(entry), new FileOutputStream(new File(documentationOutputDir, filename)));
                } catch (IOException ioe) {
                    throw new DocumentationException(DocumentationExceptionCause.zipExtractionError, ioe.toString());
                }
            } else if (entry.getName().endsWith(".class")) {
                String className = entry.getName().substring(0, entry.getName().length() - 6).replace("/", ".");
                try {
                    Class<?> clazz = clazz = loader.loadClass(className);
                    String desc = getAnnotation(clazz, jarFileName);
                    if (desc != null)
                        automaticComponentDescriptions.put(clazz.getName(), desc);
                } catch (java.lang.NoClassDefFoundError e) {
                    log("Warning: Could not load class " + className + " of jar file " + jarFileName);
                } catch (ClassNotFoundException cnfe) {
                    log("Warning: Class not found for entry: " + entry.getName() + " in jar file " + jarFileName);
                }
            }
        }
    }

    private TreeMap<String, ArrayList<String[]>> createParameterXML(Document model, File dstFile) throws DocumentationException {
        TreeMap<String, ArrayList<String[]>> map = Tools.findModelParameter(model);

        String parameterTemplate = Tools.getTemplate(Tools.Template.Parameter);
        String content = "";

        for (String component : map.keySet()) {
            ArrayList<String[]> parameterList = map.get(component);
            String parameterTitle = Tools.getTemplate(Tools.Template.ParameterTitle);
            content += parameterTitle.replace("%title%", component) + "\n";

            for (String[] parameterAndValue : parameterList) {
                String parameterEntry = Tools.getTemplate(Tools.Template.ParameterEntry);
                parameterEntry = parameterEntry.replace("%name%", parameterAndValue[0]);
                parameterEntry = parameterEntry.replace("%value%", parameterAndValue[1]);
                content += parameterEntry + "\n";
            }
        }
        parameterTemplate = parameterTemplate.replace("%title%", Bundle.resources.getString("titel_parameter"));
        parameterTemplate = parameterTemplate.replace("%content%", content);

        Tools.writeContent(dstFile, parameterTemplate);

        return map;
    }

    private String processModelStructure(Node node, Set<String> components) throws DocumentationException{
        if (node.getNodeName().equals("model") || node.getNodeName().equals("contextcomponent") || node.getNodeName().equals("component")){
            Element e = (Element)node;
            String clazz = e.getAttribute("class");
            if (clazz == null)
                clazz = jams.model.JAMSModel.class.getName();

            components.add(clazz);
            String template = null;
            switch (node.getNodeName()) {
                case "model":
                    template = Tools.getTemplate(Tools.Template.ModelStructure_Model);
                    template = template.replace("%type%", Bundle.resources.getString("model"));
                    break;
                case "contextcomponent":
                    template = Tools.getTemplate(Tools.Template.ModelStructure_Context);
                    template = template.replace("%type%", Bundle.resources.getString("contextcomponent"));
                    break;
                case "component":
                    template = Tools.getTemplate(Tools.Template.ModelStructure_Component);
                    template = template.replace("%type%", Bundle.resources.getString("component"));
                    break;
            }

            template = template.replace("%keyword:class%", Bundle.resources.getString("class"));
            template = template.replace("%class%", clazz);
            template = template.replace("%keyword:name%", Bundle.resources.getString("name"));
            template = template.replace("%name%", e.getAttribute("name"));

            if (node.getNodeName().equals("model") || node.getNodeName().equals("contextcomponent")) {
                String subComponents = "";
                NodeList list = node.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    subComponents += processModelStructure(list.item(i), components);
                }

                if (subComponents.isEmpty()) {
                    subComponents = "<listitem></listitem>";
                }

                template = template.replace("%subcomponents%", subComponents);
            }

            return template;
        }
        return null;
    }
    
    private TreeSet<String> createModelStructureXML(File documentationOutputDir, Node modelNode) throws DocumentationException{
        File modelStructureXML = new File(documentationOutputDir, MODELSTRUCTURE_FILENAME);

        TreeSet<String> componentSet = new TreeSet<>();
        String modelStructureTemplate = Tools.getTemplate(Tools.Template.Structure);
        modelStructureTemplate = modelStructureTemplate.replace("%title%", Bundle.resources.getString("titel_modellstruktur"));
        String modelStructureContent = processModelStructure(modelNode, componentSet);
        modelStructureTemplate = modelStructureTemplate.replace("%content%", modelStructureContent);

        Tools.writeContent(modelStructureXML, modelStructureTemplate);

        return componentSet;
    }

    private void createBibliographyXML(File documentationOutputDir) throws DocumentationException{
        File bibliographyXML = new File(documentationOutputDir, "/bibliography_" + Bundle.resources.getString("lang") + ".xml");

        String bibliographyTemplate = Tools.getTemplate(Tools.Template.Bibliography);
        bibliographyTemplate.replace("%language", Bundle.resources.getString("lang"));
        String content = "";
        for (String s : this.bibEntrySet.values()) {
            content += s;
        }

        bibliographyTemplate = bibliographyTemplate.replace("%language%", Bundle.resources.getString("lang"));
        bibliographyTemplate = bibliographyTemplate.replace("%content%", content);

        Tools.writeContent(bibliographyXML, bibliographyTemplate);
    }

    private void createMainDocument(File documentationInputDir, File documentationOutputDir, Node modelNode, Set<String> componentSet) throws DocumentationException{
        //erstellt ein Dokument indem die Struktur (Komponenten und Kontextkomponenten) aufgefuehrt werden.
        //erstellt weiterhin ein Dokument, welches die Komplettdokumentation erzeugt
        File mainXML = new File(documentationOutputDir, Bundle.resources.getString("Filename") + ".xml");
        File templateXML = new File(documentationInputDir, templateFileName);

        String mainTemplate = Tools.getTemplate(Tools.Template.Main);
        String modelName = ((Element) modelNode).getAttribute("name");
        String modelAuthor = ((Element) modelNode).getAttribute("author");
        String modelDate = ((Element) modelNode).getAttribute("date");

        if (modelName == null) {
            log("warning: model is not named");
            modelName = "unknown";
        }
        if (modelAuthor == null) {
            log("warning: model author is not named");
            modelAuthor = "unknown";
        }
        if (modelDate == null) {
            log("warning: model date is not named");
            modelDate = "unknown";
        }

        mainTemplate = mainTemplate.replace("%title%", Bundle.resources.getString("titel_docu"));
        mainTemplate = mainTemplate.replace("%model:name%", modelName);
        mainTemplate = mainTemplate.replace("%model:author%", modelAuthor);
        mainTemplate = mainTemplate.replace("%date%", sdf.format(new Date()));
        mainTemplate = mainTemplate.replace("%release:date%", modelDate);
        mainTemplate = mainTemplate.replace("%copyright:year%", sdfYear.format(new Date()));
        mainTemplate = mainTemplate.replace("%copyright:holder%", Bundle.resources.getString("uni"));

        String includes = "";
        TreeMap<String, String> shortNameMapping = new TreeMap<>();
        for (String component : componentSet) {
            int lastIndex = component.lastIndexOf(".");
            if (lastIndex != -1){
                shortNameMapping.put(component.substring(lastIndex+1), component);
            }else{
                shortNameMapping.put(component, component);
            }
        }

        for (String component : shortNameMapping.keySet()) {
            if (component.isEmpty())
                continue;
            component = shortNameMapping.get(component);
            mergeAutomaticAndManuellDocumention(component, templateXML,
                    new File(documentationOutputDir, "Component_Annotation." + component + ".xml"),
                    new File(documentationOutputDir, component + "_" + Bundle.resources.getString("lang") + ".xml"),
                    new File(documentationOutputDir, component + ".xml"));
            includes += "<xi:include href=\"" + component + ".xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\"/>\n";
        }
        includes += "<xi:include href=\"parameter.xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\"/>\n";
        includes += "<xi:include href=\"bibliography_" + Bundle.resources.getString("lang") + ".xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\"/>\n";

        mainTemplate = mainTemplate.replace("%include%", includes);
        mainTemplate = mainTemplate.replace("%lang%", Bundle.resources.getString("lang"));

        Tools.writeContent(mainXML, mainTemplate);
    }

    public void createDocumentation(File documentationInputDir, File documentationOutputDir, Document document) throws DocumentationException {
        //read annotations from jar files
        String[] libList = StringTools.toArray(JUICE.getJamsProperties().getProperty(JAMSProperties.LIBS_IDENTIFIER), ";");
        ArrayList<File> list = Tools.getJarList(libList);
        for (File f : list) {
            log("lib : " + f);
            processAnnotations(documentationOutputDir, f);
        }
        //write automatic annotations
        for (String component : automaticComponentDescriptions.keySet()){
            String value = automaticComponentDescriptions.get(component);

            log("-" + component);
            if (value == null) {
                log("warning: no annotations for component:" + component);
            }

            String template = Tools.getTemplate(Tools.Template.ComponentAnnotation);
            int lastIndex = component.lastIndexOf(".");
            template = template.replace("%title%", component.substring(lastIndex+1));
            template = template.replace("%content%", value);

            File annotationFile = new File(documentationOutputDir, AnnotationFileName + component + ".xml");
            Tools.writeContent(annotationFile, template);
        }

        createParameterXML(document, new File(documentationOutputDir, parameterFileName));

        NodeList modelNodelist = document.getElementsByTagName("model");
        if (modelNodelist.getLength() != 1) {
            throw new DocumentationException(DocumentationExceptionCause.invalidXML_SeveralModelTags);
        }
        Node modelNode = modelNodelist.item(0);

        TreeSet<String> componentSet = createModelStructureXML(documentationOutputDir, modelNode);

        createMainDocument(documentationInputDir, documentationOutputDir, modelNode, componentSet);

        createBibliographyXML(documentationOutputDir);
    }

    private class Component{
        String author = "";
        String version = "";
        String date = "";
        String title = "";
        String paket = "";
        String classification = "";
        String subtitle = "";

        String descriptionBlock = "";
        ArrayList<Variable> variablen = new ArrayList<>();
    }

    private class Variable{
        String variable = "";
        String description = "";
        String unit = "";
        String range = "";
        String datatype = "";
        String variabletype = "";
        String defaultvalue = "";          
    }

    private Component getComponentMetadataFromLanguageIndependentDescription(String componentName, 
            File languageIndependentComponentDescriptionFile){

        Component component = new Component();
        Document languageIndependentComponentDescription = null;

        try{
            languageIndependentComponentDescription = XMLTools.getDocument(languageIndependentComponentDescriptionFile.getAbsolutePath());
        }catch(FileNotFoundException fnfe){
            log("warning: the documentation of " + componentName + " is incomplete");
            return null;
        }

        //read lag indepent description (based on annotations)
        NodeList tableList = languageIndependentComponentDescription.getElementsByTagName("informaltable");
        if (tableList.getLength() != 2) {
            log("Error Annotation Document must have exaclty two tables found(" + tableList.getLength() + ")");
            return null;
        }

        Element headerTable = (Element) tableList.item(0);

        NodeList titleList = languageIndependentComponentDescription.getElementsByTagName("title");
        if (titleList.getLength() > 0) {
            component.title = titleList.item(0).getTextContent();
        }

        NodeList entryList = headerTable.getElementsByTagName("entry");
        for (int i = 0; i < entryList.getLength(); i++) {
            if (i >= entryList.getLength() - 1) {
                break;
            }

            switch (entryList.item(i).getTextContent()) {
                case "Paket": {
                    component.paket = entryList.item(i + 1).getTextContent();
                    break;
                }
                case "Autor": {
                    component.author = entryList.item(i + 1).getTextContent();
                    break;
                }
                case "Modellprozess": {
                    component.classification = entryList.item(i + 1).getTextContent();
                    break;
                }
                case "Version": {
                    component.version = entryList.item(i + 1).getTextContent();
                    break;
                }
                case "Modifikationsdatum": {
                    component.date = entryList.item(i + 1).getTextContent();
                    break;
                }
            }
        }

        Element contentTable = (Element) tableList.item(1);
        NodeList rows = contentTable.getElementsByTagName("row");

        for (int i = 0; i < rows.getLength(); i++) {
            //skip first 2 rows
            if (i < 2) {
                continue;
            }
            Element row = (Element) rows.item(i);
            NodeList entries = row.getElementsByTagName("entry");
            if (entries.getLength() != 7) {
                log("Invalid Annotation");
            } else {
                Variable v = new Variable();
                v.variable = entries.item(0).getTextContent();
                v.description = entries.item(1).getTextContent();
                v.unit = entries.item(2).getTextContent();
                v.range = entries.item(3).getTextContent();
                v.datatype = entries.item(4).getTextContent();
                v.variabletype = entries.item(5).getTextContent();
                v.defaultvalue = entries.item(6).getTextContent();

                component.variablen.add(v);
            }
        }

        return component;
    }

    private void getComponentMetadataFromLanguageDependentDescription(String componentName, Component component,
            File languageDependentComponentDescriptionFile){
        Document languageDependentComponentDescription = null;

        try{
            languageDependentComponentDescription = XMLTools.getDocument(languageDependentComponentDescriptionFile.getAbsolutePath());
        }catch(FileNotFoundException fnfe){
            log("warning: the documentation of " + componentName + " is incomplete");
            return;
        }

        for (Variable var : component.variablen) {
            var.description = getVariableDescriptionFromLanguageDependentComponentDescription(var.variable, languageDependentComponentDescription);
        }

        NodeList subTitleList = languageDependentComponentDescription.getElementsByTagName("subtitle");
        if (subTitleList.getLength()!=1){
            log("warning: wrong number of subtitles in descriptions of component: " + componentName);
        }else
            component.subtitle = subTitleList.item(0).getTextContent();

        NodeList classificationList = languageDependentComponentDescription.getElementsByTagName("entry");
        for (int i=0;i<classificationList.getLength()-1;i++){
            if (classificationList.item(i).getTextContent().equals("classification"))
                component.classification = classificationList.item(i+1).getTextContent();
        }
            
        NodeList sect2List = languageDependentComponentDescription.getElementsByTagName("sect2");
        if (sect2List.getLength()<2){
            log("warning: wrong number of sect2 blocks in descriptions of component: " + componentName);
            return;
        }else{
            Element variableBlock = (Element)sect2List.item(0);
            for (Variable var : component.variablen){
                NodeList rowList = variableBlock.getElementsByTagName("row");
                for (int i=0;i<rowList.getLength();i++){
                    Node row = rowList.item(i);
                    NodeList entries = row.getChildNodes();
                    if (entries.getLength()>0 && entries.item(0).getTextContent().equals(var.variable)){
                        //if (entries.getLength()>1)
                        //TODO
                    }
                }
            }
        }
        Node descriptionBlock = sect2List.item(1);
        String description = XMLTools.getStringFromNode(descriptionBlock);
        component.descriptionBlock = description.replaceAll("ns3:", "m:");

        NodeList bibliographyList = languageDependentComponentDescription.getElementsByTagName("bibliography");
        if (bibliographyList.getLength()>0){
            Element bibliographyNode = (Element)bibliographyList.item(0);

            NodeList bibEntries = bibliographyNode.getElementsByTagName("biblioentry");
            for (int i=0;i<bibEntries.getLength();i++){
                Element bibEntry = (Element)bibEntries.item(i);

                NodeList abbrevList = bibEntry.getElementsByTagName("abbrev");
                if (abbrevList.getLength()!=1)
                    continue;
                bibEntrySet.put(abbrevList.item(0).getTextContent(), XMLTools.getStringFromNode(bibEntry));
            }
        }
    }

    private void createDocumentFromComponentDescription(Component component, File outputFile) throws DocumentationException{
        //zusammensetzen des end-dokumentes
        String endDocument = Tools.getTemplate(Tools.Template.EndDocument);
        int index = component.title.lastIndexOf(".");
        if (index!=-1)
            component.title = component.title.substring(index+1);
        endDocument = endDocument.replace("%component_title%", component.title);

        /*if (!languageDependentComponentDescriptionFile.equals(templateXML)) {
            endDocument = endDocument.replace("%subtitle%", "");
        } else {
            endDocument = endDocument.replace("%subtitle%", component.subtitle);
        }*/
        endDocument = endDocument.replace("%subtitle%", component.subtitle);
        endDocument = endDocument.replace("%metadataString%", Bundle.resources.getString("metadata"));
        endDocument = endDocument.replace("%classificationString%", Bundle.resources.getString("classification"));
        endDocument = endDocument.replace("%classification%", component.classification);
        endDocument = endDocument.replace("%packageString%", Bundle.resources.getString("package"));
        endDocument = endDocument.replace("%package%", component.paket);
        endDocument = endDocument.replace("%authorString%", Bundle.resources.getString("author"));
        endDocument = endDocument.replace("%author%", component.author);
        endDocument = endDocument.replace("%versionString%", Bundle.resources.getString("version"));
        endDocument = endDocument.replace("%version%", component.version);
        endDocument = endDocument.replace("%dateString%", Bundle.resources.getString("modification_date"));
        endDocument = endDocument.replace("%date%", component.date);

        endDocument = endDocument.replace("%variableTitle%", Bundle.resources.getString("variables"));

        //Eingangsvariablen        
        String  inputVariableContent   = "";
        String  stateVariableContent   = "";
        String  outputVariableContent   = "";

        String variableTemplate = Tools.getTemplate(Tools.Template.VariableDescription);
        variableTemplate = variableTemplate.replace("%variableNameString%", Bundle.resources.getString("variable"));
        variableTemplate = variableTemplate.replace("%descriptionString%", Bundle.resources.getString("description"));
        variableTemplate = variableTemplate.replace("%unitString%", Bundle.resources.getString("unit"));
        variableTemplate = variableTemplate.replace("%dataTypeString%", Bundle.resources.getString("data_type"));

        String inputVariableTemplate = variableTemplate.replace("%VariableString%", Bundle.resources.getString("variable_input"));
        String stateVariableTemplate = variableTemplate.replace("%VariableString%", Bundle.resources.getString("variable_status"));
        String outputVariableTemplate = variableTemplate.replace("%VariableString%", Bundle.resources.getString("variable_output"));

        for (Variable var : component.variablen) {
            //preprocessing
            int lastPoint = var.datatype.lastIndexOf(".");
            if (lastPoint != -1) {
                var.datatype = var.datatype.substring(lastPoint+1);
            }
            if (var.datatype.startsWith("JAMS")) {
                var.datatype = var.datatype.substring(4);
            }
            var.datatype.replace("EntityCollection", "Entity Collection");
            var.datatype.replace("Double;", "Double Array");
            
            String varContent = "<row>\n"
                            + "<entry>" + var.variable + "</entry>\n"
                            + "<entry>" + var.description + "</entry>\n"
                            + "<entry>" + var.unit + "</entry>\n"
                            + "<entry>" + var.datatype + "</entry>\n"
                            + "</row>\n";
            //classification
            switch (var.variabletype) {
                case "READ":                    
                    inputVariableContent += varContent;
                    break;
                case "READWRITE":
                    stateVariableContent += varContent;
                    break;
                case "WRITE":
                    outputVariableContent += varContent;
                    break;
            }
        }

        String inputVariableBlock = "",
               stateVariableBlock = "",
               outputVariableBlock = "";

        if (!inputVariableContent.isEmpty())
            inputVariableBlock = inputVariableTemplate.replace("%content%", inputVariableContent);
        if (!stateVariableContent.isEmpty())
            stateVariableBlock = stateVariableTemplate.replace("%content%", stateVariableContent);
        if (!outputVariableContent.isEmpty())
            outputVariableBlock = outputVariableTemplate.replace("%content%", outputVariableContent);

        endDocument = endDocument.replace("%inputvariableBlock%", inputVariableBlock);
        endDocument = endDocument.replace("%statevariableBlock%", stateVariableBlock);
        endDocument = endDocument.replace("%outputvariableBlock%", outputVariableBlock);

        endDocument = endDocument.replace("%descriptionTitle%", Bundle.resources.getString("description_component"));
        endDocument = endDocument.replace("%description%", component.descriptionBlock);

        Tools.writeContent(outputFile, endDocument);
    }
    //verbindet die automatische Dokumentation aus dem Quellcode mit der manuell erstellten von den Entwicklern und speichert diese in einem enddokument mit dem Namen des Pfades der Komponente
    public void mergeAutomaticAndManuellDocumention(String componentName, File templateXML,
            File languageIndependentComponentDescriptionFile, File languageDependentComponentDescriptionFile,
            File outputFile) throws DocumentationException{
        
        Component component = getComponentMetadataFromLanguageIndependentDescription(componentName, languageIndependentComponentDescriptionFile);
        if (component==null)
            component = new Component();
        getComponentMetadataFromLanguageDependentDescription(componentName, component, languageDependentComponentDescriptionFile);
        
        createDocumentFromComponentDescription(component,outputFile);
        
    }

    private String getVariableDescriptionFromLanguageDependentComponentDescription(String variable, Document languageDependentComponentDescription) {
        NodeList entryList = languageDependentComponentDescription.getElementsByTagName("entry");
        for (int i = 0; i < entryList.getLength() - 1; i++) {
            Node node = entryList.item(i);
            if (node.getTextContent().equals(variable)) {
                return entryList.item(i + 1).getTextContent();
            }
        }
        return null;
    }
}
