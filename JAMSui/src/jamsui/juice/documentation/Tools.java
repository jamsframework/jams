package jamsui.juice.documentation;

import jamsui.juice.documentation.DocumentationException.DocumentationExceptionCause;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Tools{
    public enum Template{ComponentAnnotation, Component, Variable, Parameter, 
    ParameterEntry, ParameterTitle, Bibliography, Structure, Main, ModelStructure_Component, ModelStructure_Context,
    ModelStructure_Model, EndDocument, VariableDescription}

    public static String getTemplate(String template) throws DocumentationException{
        return readContent("resources/doc/templates/" + template);
    }

    public static String readContent(String resourceName) throws DocumentationException {
        InputStream stream = ClassLoader.getSystemResourceAsStream(resourceName);
        if (stream == null) {
            throw new DocumentationException(DocumentationExceptionCause.templateNotFound, resourceName);
        }
        String content = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                content += line;
            }
            reader.close();
            stream.close();
        } catch (IOException ioe) {
            throw new DocumentationException(DocumentationExceptionCause.templateNotFound, resourceName);
        }
        return content;
    }

    public static void writeContent(File dst, String content) throws DocumentationException{
        try{
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dst),"UTF-8"));


            out.write(content);
            out.close();
        }catch(IOException ioe){
            throw new DocumentationException(DocumentationExceptionCause.writeFailed, ioe.toString());
        }
    }

    public static void extractZipEntry(JarFile jfile, ZipEntry entry, File outFile) throws DocumentationException{
        try {
            InputStream in = new BufferedInputStream(jfile.getInputStream(entry));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
            byte[] buffer = new byte[4096];
            for (;;) {
                int nBytes = in.read(buffer);
                if (nBytes <= 0) {
                    break;
                }
                out.write(buffer, 0, nBytes);
            }

            out.flush();
            out.close();
            in.close();
        } catch (IOException ioe) {
            throw new DocumentationException(DocumentationExceptionCause.zipExtractionError,ioe.toString());
        }
    }

    public static ArrayList<File> getJarList(String[] libsArray) {
        ArrayList<File> result = new ArrayList<File>();

        for (int i = 0; i < libsArray.length; i++) {
            File file = new File(libsArray[i]);

            if (!file.exists()) {
                continue;
            }else if(file.isDirectory()) {
                File[] f = file.listFiles();
                for (int j = 0; j < f.length; j++) {
                    if (f[j].getName().endsWith(".jar")) {
                        ArrayList<File> subResult = getJarList(new String[]{f[j].getAbsolutePath()});
                        if (!subResult.isEmpty()) {
                            result.addAll(subResult);
                        }
                    }
                }
            } else {
                result.add(file);
            }
        }

        return result;
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException{
        byte[] buffer = new byte[4096];
        for (;;) {
            int nBytes = in.read(buffer);
            if (nBytes <= 0) {
                break;
            }
            out.write(buffer, 0, nBytes);
        }
        out.flush();
        out.close();
        in.close();
    }

    public static String getParentNodeName(Node node){
        Element e = (Element)node;
        if (node.getNodeName().equals("component") || node.getNodeName().equals("contextcomponent") || node.getNodeName().equals("model"))
            return e.getAttribute("name");
        else if (node.getNodeName().equals("launcher"))
            return "launcher";
        else{
            Node parent = node.getParentNode();
            if (parent == null){
                return null;
            }else{
                return getParentNodeName(parent);
            }
        }
    }

    public static ArrayList<Node> getComponentList(Node root){
        NodeList list = root.getChildNodes();
        ArrayList<Node> result = new ArrayList<Node>();

        for (int i=0;i<list.getLength();i++){
            Node node = list.item(i);
            if (node.getNodeName().equals("component") ||
                    node.getNodeName().equals("contextcomponent"))
                result.add(node);

            if (node.hasChildNodes()){
                result.addAll(getComponentList(node));
            }
        }
        return result;
    }

    public static TreeMap<String, ArrayList<String[]>> findModelParameter(Document model) throws DocumentationException{
        TreeMap<String, ArrayList<String[]>> parameterMap = new TreeMap<String, ArrayList<String[]>>();
        NodeList modelList = model.getElementsByTagName("model");

        if (modelList.getLength()>1){
            throw new DocumentationException(DocumentationExceptionCause.invalidXML_SeveralModelTags);
        }
        Node modelNode = modelList.item(0);

        NodeList lists[] = new NodeList[2];
        lists[0] = ((Element)modelNode).getElementsByTagName("attribute");
        lists[1] = ((Element)modelNode).getElementsByTagName("var");

        for (NodeList list : lists) {
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                String parent = Tools.getParentNodeName(node);
                String name = ((Element) node).getAttribute("name");
                String value = ((Element) node).getAttribute("value");
                if (name != null && value != null && !value.equals("")) {
                    ArrayList<String[]> parameterList = parameterMap.get(parent);
                    if (parameterList==null){
                        parameterList = new ArrayList<String[]>();
                        parameterMap.put(parent, parameterList);
                    }
                    parameterList.add(new String[]{name, value});
                }
            }
        }
        return parameterMap;
    }
}