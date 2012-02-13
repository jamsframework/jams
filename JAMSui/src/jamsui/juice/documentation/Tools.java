package jamsui.juice.documentation;

import jamsui.juice.documentation.DocumentationException.DocumentationExceptionCause;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Tools{
    public enum Template{ComponentAnnotation, Component, Variable, Parameter, 
    ParameterEntry, ParameterTitle, Bibliography, Structure, Main, MainComponent,
    MainContext}

    public static String getTemplate(Template template) throws DocumentationException{
        switch(template){
            case ComponentAnnotation: return readContent("resources/templates/componentAnnotation.xml");
            case Component: return readContent("resources/templates/component.xml");
            case Variable: return readContent("resources/templates/variable.xml");
            case Parameter: return readContent("resources/templates/parameter.xml");
            case ParameterTitle: return readContent("resources/templates/parameterTitle.xml");
            case ParameterEntry: return readContent("resources/templates/parameterEntry.xml");
            case Bibliography: return readContent("resources/templates/bibliography.xml");
            case Structure: return readContent("resources/templates/structure.xml");
            case Main: return readContent("resources/templates/complete.xml");
            case MainComponent: return readContent("resources/templates/main_component.xml");
            case MainContext: return readContent("resources/templates/main_context.xml");
        }
        return null;
    }

    public static String readContent(String resourceName) throws DocumentationException{
        InputStream stream = Tools.class.getClassLoader().getSystemResourceAsStream(resourceName);
        if (stream == null){
            throw new DocumentationException(DocumentationExceptionCause.templateNotFound, resourceName);
        }
        String content = "";
        try{
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line = null;
        while ( (line = reader.readLine())!=null){
            content += line;
        }
        reader.close();
        stream.close();
        }catch(IOException ioe){
            throw new DocumentationException(DocumentationExceptionCause.templateNotFound, resourceName);
        }
        return content;
    }

    public static void writeContent(File dst, String content) throws DocumentationException{
        try{
            BufferedWriter out = new BufferedWriter(new FileWriter(dst));
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
}