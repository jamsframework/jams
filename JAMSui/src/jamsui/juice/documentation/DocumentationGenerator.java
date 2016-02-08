package jamsui.juice.documentation;

import jams.JAMSException;
import jams.JAMSLogging;
import jams.JAMSProperties;
import jams.JAMSVersion;
import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSModel;
import jams.tools.FileTools;
import jams.tools.StringTools;
import jams.tools.XMLTools;
import jamsui.juice.JUICE;
import jamsui.juice.documentation.DocumentationException.DocumentationExceptionCause;
import jamsui.launcher.JAMSui;
import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author sa63kul
 */
public class DocumentationGenerator {

    static final Logger log = Logger.getLogger(DocumentationGenerator.class.getName());

    private static final String DEFAULT_PACKAGE_ID = "<default package>";
    private final String AnnotationFileName = "Component_Annotation.";
    private final String templateFileName = "template";
    private final String parameterFileName = "parameter.xml";
    private final String MODELSTRUCTURE_FILENAME = "structure.xml";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
    SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
    ClassLoader loader = null;
    TreeMap<String, String> bibEntrySet = new TreeMap<String, String>();
    TreeMap<String, String> automaticComponentDescriptions = new TreeMap<String, String>();

    private static String getAnnotation(Class<?> clazz, String jarFileName) throws ClassNotFoundException, DocumentationException, NoClassDefFoundError {
        if (!jams.model.Component.class.isAssignableFrom(clazz)) {
            return null;
        }

        String compDesc = Tools.getTemplate("component.xml");
        Package classPackage = clazz.getPackage();
        if (classPackage != null) {
            compDesc = compDesc.replace("%package%", classPackage.getName());
        } else {
            compDesc = compDesc.replace("%package%", DEFAULT_PACKAGE_ID);
        }
        compDesc = compDesc.replace("%class%", clazz.getSimpleName());
        compDesc = compDesc.replace("%jarFile%", jarFileName);

        JAMSComponentDescription jcd = (JAMSComponentDescription) clazz.getAnnotation(JAMSComponentDescription.class);

        String title = "", author = "", date = "", description = "", version = "";

        if (jcd != null) {
            title = jcd.title();
            author = jcd.author().replace("&", " " + Bundle.resources.getString("and") + " ");
            date = jcd.date();
            description = jcd.description();
            version = jcd.version();
        }

        compDesc = compDesc.replace("%title%", title);
        compDesc = compDesc.replace("%author%", author);
        compDesc = compDesc.replace("%date%", date);
        compDesc = compDesc.replace("%description%", description);
        compDesc = compDesc.replace("%version%", version);

        String variables = "";
        boolean interfaceFound = false;

        Field field[] = clazz.getFields();

        for (int i = 0; i < field.length; i++) {
            jams.model.JAMSVarDescription jvd = (jams.model.JAMSVarDescription) field[i].getAnnotation(jams.model.JAMSVarDescription.class);
            String variableTemplate = Tools.getTemplate("variable.xml");
            variableTemplate = variableTemplate.replace("%name%", field[i].getName());
            if (jvd != null) {
                interfaceFound = true;
                String tmp = field[i].getType().getName();
                tmp = tmp.replace('$', '.');
                tmp = tmp.replace(";", "[]");
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
        log.fine("generating annotation documentation");

        JarFile jFile = null;
        String jarFileName = jarFile.getName();

        try {
            jFile = new JarFile(jarFile);
        } catch (IOException ioe) {
            throw new DocumentationException(DocumentationExceptionCause.invalidJarFile, jarFile.getName());
        }

        try {
            loader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, ClassLoader.getSystemClassLoader());
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
            } else if (entry.getName().endsWith(".png") || entry.getName().endsWith(".jpg")) {
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
                    if (desc != null) {
                        automaticComponentDescriptions.put(clazz.getName(), desc);
                    }
                } catch (java.lang.NoClassDefFoundError e) {
                    log.log(Level.WARNING, "Could not load class " + className + " of jar file " + jarFileName, e);
                } catch (ClassNotFoundException cnfe) {
                    log.log(Level.WARNING, "Class not found for entry: " + entry.getName() + " in jar file " + jarFileName);
                }
            }
        }
    }

    private TreeMap<String, ArrayList<String[]>> createParameterXML(Document model, File dstFile) throws DocumentationException {
        TreeMap<String, ArrayList<String[]>> map = Tools.findModelParameter(model);

        String parameterTemplate = Tools.getTemplate("parameter.xml");
        String content = "";

        for (String component : map.keySet()) {
            ArrayList<String[]> parameterList = map.get(component);
            String parameterTitle = Tools.getTemplate("parameterTitle.xml");
            content += parameterTitle.replace("%title%", component) + "\n";

            for (String[] parameterAndValue : parameterList) {
                String parameterEntry = Tools.getTemplate("parameterEntry.xml");
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

    private String processModelStructure(Node node, Set<String> components) throws DocumentationException {
        if (((Element) node).getAttribute("enabled").equals("false")) {
            return "";
        }
        if (node.getNodeName().equals("model") || node.getNodeName().equals("contextcomponent") || node.getNodeName().equals("component")) {
            Element e = (Element) node;
            String clazz = e.getAttribute("class");
            if (StringTools.isEmptyString(clazz)) {
                clazz = jams.model.JAMSModel.class.getName();
            }

            components.add(clazz);
            String template = null;
            if (node.getNodeName().equals("model")) {
                template = Tools.getTemplate("structureModel.xml");
                template = template.replace("%type%", Bundle.resources.getString("model"));
            } else if (node.getNodeName().equals("contextcomponent")) {
                template = Tools.getTemplate("structureContext.xml");
                template = template.replace("%type%", Bundle.resources.getString("contextcomponent"));
            } else if (node.getNodeName().equals("component")) {
                template = Tools.getTemplate("structureComponent.xml");
                template = template.replace("%type%", Bundle.resources.getString("component"));
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

    private TreeSet<String> createModelStructureXML(File documentationOutputDir, Node modelNode) throws DocumentationException {
        File modelStructureXML = new File(documentationOutputDir, MODELSTRUCTURE_FILENAME);

        TreeSet<String> componentSet = new TreeSet<String>();
        String modelStructureTemplate = Tools.getTemplate("structure.xml");
        modelStructureTemplate = modelStructureTemplate.replace("%title%", Bundle.resources.getString("titel_modellstruktur"));
        String modelStructureContent = processModelStructure(modelNode, componentSet);
        modelStructureTemplate = modelStructureTemplate.replace("%content%", modelStructureContent);

        Tools.writeContent(modelStructureXML, modelStructureTemplate);

        return componentSet;
    }

    private void createBibliographyXML(File documentationOutputDir) throws DocumentationException {
        File bibliographyXML = new File(documentationOutputDir, "/bibliography_" + Bundle.resources.getString("lang") + ".xml");

        String bibliographyTemplate = Tools.getTemplate("bibliography.xml");
        bibliographyTemplate.replace("%language", Bundle.resources.getString("lang"));
        String content = "";
        for (String s : this.bibEntrySet.values()) {
            content += s;
        }

        bibliographyTemplate = bibliographyTemplate.replace("%language%", Bundle.resources.getString("lang"));
        bibliographyTemplate = bibliographyTemplate.replace("%content%", content);

        Tools.writeContent(bibliographyXML, bibliographyTemplate);
    }

    private void createMainDocument(File documentationHome, File documentationOutputDir, Node modelNode, Set<String> componentSet) throws DocumentationException {
        //erstellt ein Dokument indem die Struktur (Komponenten und Kontextkomponenten) aufgefuehrt werden.
        //erstellt weiterhin ein Dokument, welches die Komplettdokumentation erzeugt
        File mainXML = new File(documentationOutputDir, Bundle.resources.getString("Filename") + ".xml");

        String mainTemplate;
        File templateFile = new File(documentationHome, templateFileName + "_" + Bundle.resources.getString("lang") + ".xml");
        if (templateFile.exists()) {
            try {
                mainTemplate = FileTools.fileToString(templateFile.getAbsolutePath(), "UTF-8");
            } catch (IOException ex) {
                mainTemplate = Tools.getTemplate("main.xml");
                ex.printStackTrace();
            }
        } else {
            mainTemplate = Tools.getTemplate("main.xml");

            mainTemplate = mainTemplate.replace("%title%", Bundle.resources.getString("titel_docu"));
            mainTemplate = mainTemplate.replace("%please_adapt_intro%", Bundle.resources.getString("please_adapt_intro"));
            mainTemplate = mainTemplate.replace("%model:intro%", Bundle.resources.getString("model_intro"));

            try {
                Tools.writeContent(templateFile.getAbsoluteFile(), mainTemplate);
            } catch (DocumentationException ex) {
                ex.printStackTrace();
            }
        }

        String modelName = ((Element) modelNode).getAttribute("name");
        String modelAuthor = ((Element) modelNode).getAttribute("author");
        Attribute.Calendar modelDate = DefaultDataFactory.getDataFactory().createCalendar();
        modelDate.setValue(((Element) modelNode).getAttribute("date"));

        String modelDescription = null;
        Node descriptionNode = ((Element) modelNode).getElementsByTagName("description").item(0);
        if (descriptionNode != null) {
            modelDescription = StringEscapeUtils.escapeXml(descriptionNode.getTextContent().trim());
        }

        if (modelName == null) {
            log.log(Level.INFO, "warning: model is not named");
            modelName = "unknown";
        }
        if (modelAuthor == null) {
            log.log(Level.INFO, "warning: model author is not named");
            modelAuthor = "unknown";
        }
        if (modelDescription == null) {
            log.log(Level.INFO, "warning: model date is not named");
        }

        mainTemplate = mainTemplate.replace("%model:name%", modelName);
        mainTemplate = mainTemplate.replace("%model:author%", modelAuthor);
        mainTemplate = mainTemplate.replace("%date%", DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault()).format(new Date()));
        mainTemplate = mainTemplate.replace("%model:date%", modelDate.toString(DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())));
        mainTemplate = mainTemplate.replace("%releaseinfo%", "JAMS version: " + JAMSVersion.getInstance().getVersionDateString());
        if (!StringTools.isEmptyString(modelDescription)) {
            modelDescription = "<abstract><para>" + modelDescription + "</para></abstract>";
        }
        mainTemplate = mainTemplate.replace("%model:description%", modelDescription);
        mainTemplate = mainTemplate.replace("%copyright:year%", sdfYear.format(new Date()));
        mainTemplate = mainTemplate.replace("%copyright:holder%", "");

        String includes = "";
        TreeMap<String, String> shortNameMapping = new TreeMap<String, String>();
        for (String component : componentSet) {
            int lastIndex = component.lastIndexOf(".");
            if (lastIndex != -1) {
                shortNameMapping.put(component.substring(lastIndex + 1), component);
            } else {
                shortNameMapping.put(component, component);
            }
        }

        for (String component : shortNameMapping.keySet()) {
            if (component.isEmpty()) {
                continue;
            }
            component = shortNameMapping.get(component);
            mergeDocumentation(component,
                    new File(documentationOutputDir, AnnotationFileName + component + ".xml"),
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

    private void dumpTempFiles(File documentationOutputDir) {

        if (!documentationOutputDir.isDirectory()) {
            return;
        }

        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.startsWith("Component_Annotation");
            }
        };

        for (String fileName : documentationOutputDir.list(filter)) {
            new File(documentationOutputDir, fileName).delete();
        }
    }

    public void createDocumentation(File documentationHome, File documentationOutputDir, Document document) throws DocumentationException {
        //read annotations from jar files
        String[] libList = StringTools.toArray(JUICE.getJamsProperties().getProperty(JAMSProperties.LIBS_IDENTIFIER), ";");
        ArrayList<File> list = Tools.getJarList(libList);
        for (File f : list) {
            log.finest("lib : " + f);
            processAnnotations(documentationOutputDir, f);
        }
        //also add the JAMSModel documentation which is not found in components libs
        try {
            Class<?> clazz = JAMSModel.class;
            String desc = getAnnotation(clazz, "");
            if (desc != null) {
                automaticComponentDescriptions.put(clazz.getName(), desc);
            }
        } catch (java.lang.NoClassDefFoundError e) {
            log.log(Level.WARNING, "Could not load class JAMSModel", e);
        } catch (ClassNotFoundException cnfe) {
            log.log(Level.WARNING, "Class not found for entry JAMSModel", cnfe);
        }        
        
        //write automatic annotations
        for (String component : automaticComponentDescriptions.keySet()) {
            String value = automaticComponentDescriptions.get(component);

            log.log(Level.FINER, "-" + component);
            if (value == null) {
                log.warning("warning: no annotations for component:" + component);
            }

            String template = Tools.getTemplate("componentAnnotation.xml");
            int lastIndex = component.lastIndexOf(".");
            template = template.replace("%title%", component.substring(lastIndex + 1));
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

        createMainDocument(documentationHome, documentationOutputDir, modelNode, componentSet);

        createBibliographyXML(documentationOutputDir);

        dumpTempFiles(documentationOutputDir);
    }

    private class Component {

        String author = "";
        String version = "";
        String date = "";
        String title = "";
        String paket = "";
        String classification = "";
        String subtitle = "";
        String descriptionBlock = "";
        ArrayList<Variable> variablen = new ArrayList<Variable>();
    }

    private class Variable {

        String variable = "";
        String description = "";
        String unit = "";
        String range = "";
        String datatype = "";
        String variabletype = "";
        String defaultvalue = "";
    }

    private Component getComponentMetadataFromLanguageIndependentDescription(String componentName,
            File languageIndependentComponentDescriptionFile) {

        Component component = new Component();
        Document languageIndependentComponentDescription = null;

        try {
            languageIndependentComponentDescription = XMLTools.getDocument(languageIndependentComponentDescriptionFile.getAbsolutePath());
        } catch (JAMSException fnfe) {
            log.log(Level.WARNING, "the documentation of " + componentName + " is incomplete (" + languageIndependentComponentDescriptionFile.getAbsolutePath() + " is missing).");
            return null;
        }

        //read lag indepent description (based on annotations)
        NodeList tableList = languageIndependentComponentDescription.getElementsByTagName("informaltable");
        if (tableList.getLength() != 2) {
            log.info("Error Annotation Document must have exaclty two tables, found " + tableList.getLength() + "!");
            return null;
        }

        Element headerTable = (Element) tableList.item(0);

        NodeList titleList = languageIndependentComponentDescription.getElementsByTagName("title");
        if (titleList.getLength() > 0) {
            component.title = StringEscapeUtils.escapeXml(titleList.item(0).getTextContent());
        }

        NodeList entryList = headerTable.getElementsByTagName("entry");
        for (int i = 0; i < entryList.getLength(); i++) {
            if (i >= entryList.getLength() - 1) {
                break;
            }

            if (entryList.item(i).getTextContent().equals("Paket")) {
                component.paket = StringEscapeUtils.escapeXml(entryList.item(i + 1).getTextContent());
            } else if (entryList.item(i).getTextContent().equals("Autor")) {
                component.author = StringEscapeUtils.escapeXml(entryList.item(i + 1).getTextContent());
            } else if (entryList.item(i).getTextContent().equals("Modellprozess")) {
                component.classification = StringEscapeUtils.escapeXml(entryList.item(i + 1).getTextContent());
            } else if (entryList.item(i).getTextContent().equals("Version")) {
                component.version = StringEscapeUtils.escapeXml(entryList.item(i + 1).getTextContent());
            } else if (entryList.item(i).getTextContent().equals("Modifikationsdatum")) {
                component.date = StringEscapeUtils.escapeXml(entryList.item(i + 1).getTextContent());
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
                log.log(Level.WARNING, "Invalid Annotation: {0}", entries.toString());
            } else {
                Variable v = new Variable();
                v.variable = StringEscapeUtils.escapeXml(entries.item(0).getTextContent());
                v.description = StringEscapeUtils.escapeXml(entries.item(1).getTextContent());
                v.unit = StringEscapeUtils.escapeXml(entries.item(2).getTextContent());
                v.range = StringEscapeUtils.escapeXml(entries.item(3).getTextContent());
                v.datatype = StringEscapeUtils.escapeXml(entries.item(4).getTextContent());
                v.variabletype = StringEscapeUtils.escapeXml(entries.item(5).getTextContent());
                v.defaultvalue = StringEscapeUtils.escapeXml(entries.item(6).getTextContent());

                component.variablen.add(v);
            }
        }

        return component;
    }

    private void getComponentMetadataFromLanguageDependentDescription(String componentName, Component component,
            File languageDependentComponentDescriptionFile) {
        Document languageDependentComponentDescription = null;

        try {
            languageDependentComponentDescription = XMLTools.getDocument(languageDependentComponentDescriptionFile.getAbsolutePath());
        } catch (JAMSException fnfe) {
            log.log(Level.WARNING, "The documentation of " + componentName + " is incomplete (" + languageDependentComponentDescriptionFile.getAbsolutePath() + " is missing).");
            return;
        }

        for (Variable var : component.variablen) {
            var.description = getVariableDescriptionFromLanguageDependentComponentDescription(var.variable, languageDependentComponentDescription);
        }

        NodeList subTitleList = languageDependentComponentDescription.getElementsByTagName("subtitle");
        if (subTitleList.getLength() != 1) {
            log.log(Level.WARNING, "Wrong number of subtitles in descriptions of component: " + componentName);
        } else {
            component.subtitle = StringEscapeUtils.escapeXml(subTitleList.item(0).getTextContent());
        }

        NodeList classificationList = languageDependentComponentDescription.getElementsByTagName("entry");
        for (int i = 0; i < classificationList.getLength() - 1; i++) {
            if (classificationList.item(i).getTextContent().equals("classification")) {
                component.classification = StringEscapeUtils.escapeXml(classificationList.item(i + 1).getTextContent());
            }
        }

        NodeList sect2List = languageDependentComponentDescription.getElementsByTagName("sect2");
        if (sect2List.getLength() < 2) {
            log.log(Level.WARNING, "wrong number of sect2 blocks in descriptions of component: {0}", componentName);
            return;
        } else {
            Element variableBlock = (Element) sect2List.item(0);
            for (Variable var : component.variablen) {
                NodeList rowList = variableBlock.getElementsByTagName("row");
                for (int i = 0; i < rowList.getLength(); i++) {
                    Node row = rowList.item(i);
                    NodeList entries = row.getChildNodes();
                    if (entries.getLength() > 0 && entries.item(0).getTextContent().equals(var.variable)) {
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
        if (bibliographyList.getLength() > 0) {
            Element bibliographyNode = (Element) bibliographyList.item(0);

            NodeList bibEntries = bibliographyNode.getElementsByTagName("biblioentry");
            for (int i = 0; i < bibEntries.getLength(); i++) {
                Element bibEntry = (Element) bibEntries.item(i);

                NodeList abbrevList = bibEntry.getElementsByTagName("abbrev");
                if (abbrevList.getLength() != 1) {
                    continue;
                }
                bibEntrySet.put(abbrevList.item(0).getTextContent(), XMLTools.getStringFromNode(bibEntry));
            }
        }
    }

    private void createDocumentFromComponentDescription(Component component, File outputFile) throws DocumentationException {
        //zusammensetzen des end-dokumentes
        String endDocument = Tools.getTemplate("componentDocument.xml");
        int index = component.title.lastIndexOf(".");
        if (index != -1) {
            component.title = component.title.substring(index + 1);
        }
        endDocument = endDocument.replace("%component_title%", component.title);

        /*
         * if (!languageDependentComponentDescriptionFile.equals(templateXML)) {
         * endDocument = endDocument.replace("%subtitle%", ""); } else {
         * endDocument = endDocument.replace("%subtitle%", component.subtitle);
         * }
         */
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
        String inputVariableContent = "";
        String stateVariableContent = "";
        String outputVariableContent = "";

        String variableTemplate = Tools.getTemplate("variableDescription.xml");
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
                var.datatype = var.datatype.substring(lastPoint + 1);
            }
            if (var.datatype.startsWith("JAMS")) {
                var.datatype = var.datatype.substring(4);
            }
            var.datatype.replace("EntityCollection", "Entity Collection");
            var.datatype.replace("Double;", "Double Array");

            String unitString = "";
            try {
                unitString = new String(var.unit.getBytes(), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }

            String varContent = "<row>\n"
                    + "<entry>" + var.variable + "</entry>\n"
                    + "<entry>" + var.description + "</entry>\n"
                    + "<entry>" + var.unit + "</entry>\n"
                    + "<entry>" + var.datatype + "</entry>\n"
                    + "</row>\n";
            //classification
            if (var.variabletype.equals("READ")) {
                inputVariableContent += varContent;
            } else if (var.variabletype.equals("READWRITE")) {
                stateVariableContent += varContent;
            } else if (var.variabletype.equals("WRITE")) {
                outputVariableContent += varContent;
            }
        }

        String inputVariableBlock = "",
                stateVariableBlock = "",
                outputVariableBlock = "";

        if (!inputVariableContent.isEmpty()) {
            inputVariableBlock = inputVariableTemplate.replace("%content%", inputVariableContent);
        }
        if (!stateVariableContent.isEmpty()) {
            stateVariableBlock = stateVariableTemplate.replace("%content%", stateVariableContent);
        }
        if (!outputVariableContent.isEmpty()) {
            outputVariableBlock = outputVariableTemplate.replace("%content%", outputVariableContent);
        }

        endDocument = endDocument.replace("%inputvariableBlock%", inputVariableBlock);
        endDocument = endDocument.replace("%statevariableBlock%", stateVariableBlock);
        endDocument = endDocument.replace("%outputvariableBlock%", outputVariableBlock);

        endDocument = endDocument.replace("%descriptionTitle%", Bundle.resources.getString("description_component"));
        endDocument = endDocument.replace("%description%", component.descriptionBlock);

        Tools.writeContent(outputFile, endDocument);
    }
    //verbindet die automatische Dokumentation aus dem Quellcode mit der manuell erstellten von den Entwicklern und speichert diese in einem enddokument mit dem Namen des Pfades der Komponente

    private void mergeDocumentation(String componentName, File languageIndependentComponentDescriptionFile,
            File languageDependentComponentDescriptionFile, File outputFile) throws DocumentationException {

        Component component = getComponentMetadataFromLanguageIndependentDescription(componentName, languageIndependentComponentDescriptionFile);
        if (component == null) {
            component = new Component();
        }
        getComponentMetadataFromLanguageDependentDescription(componentName, component, languageDependentComponentDescriptionFile);

        createDocumentFromComponentDescription(component, outputFile);

    }

    private String getVariableDescriptionFromLanguageDependentComponentDescription(String variable, Document languageDependentComponentDescription) {
        NodeList entryList = languageDependentComponentDescription.getElementsByTagName("entry");
        for (int i = 0; i < entryList.getLength() - 1; i++) {
            Node node = entryList.item(i);
            if (node.getTextContent().equals(variable)) {
                return StringEscapeUtils.escapeXml(entryList.item(i + 1).getTextContent());
            }
        }
        return null;
    }
}
