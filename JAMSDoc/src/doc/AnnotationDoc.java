package doc;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarFile;
import org.unijena.jams.model.*;

public class AnnotationDoc {
    
    public static String readContent(InputStream in) {
        String content="";
        InputStreamReader r = new InputStreamReader(in);
        
        try {
            char[] buffer = new char[in.available()];
            r.read(buffer);
            content = String.copyValueOf(buffer);
            r.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        return content;
    }
    
    public static void writeContent(String file, String content) {
        File f = new File(file);
        char[] buffer = new char[(int) f.length()];
        
        try {
            Writer w = new FileWriter(f);
            w.write(content);
            w.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    static public boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }    
    
    public static void createDoc(String inJar, String outPath, Class<?> superClass) throws Exception {
        
        JarFile jfile = new JarFile(inJar);
        File file = new File(inJar);
        URLClassLoader loader = new URLClassLoader(new URL[]{file.toURL()});
        
        Enumeration jarentries = jfile.entries();
        
        String compTemplate = readContent(ClassLoader.getSystemResourceAsStream("resources/templates/comptemplate.txt"));
        String varTemplate = readContent(ClassLoader.getSystemResourceAsStream("resources/templates/vartemplate.txt"));
//        String overallTemplate = readContent(ClassLoader.getSystemResourceAsStream("resources/templates/overalltemplate.txt"));
        String overallTemplate = readContent(ClassLoader.getSystemResourceAsStream("resources/templates/layouttemplate.txt"));
        HashMap<Class, String> componentDescriptions = new HashMap<Class, String>();
        ArrayList<Class> components = new ArrayList<Class>();
        
        while (jarentries.hasMoreElements()) {
            String entry = jarentries.nextElement().toString();
            if ((entry.endsWith(".class"))) {
                String classString = entry.substring(0,entry.length()-6);
                classString = classString.replaceAll("/", ".");
                try {
                    Class<?> clazz = loader.loadClass(classString);
                    
                    if (superClass.isAssignableFrom(clazz)) {
                        
                        String compDesc = compTemplate;
                        compDesc = compDesc.replaceAll("%package%", clazz.getPackage().toString());
                        compDesc = compDesc.replaceAll("%class%", clazz.getSimpleName());
                        
                        JAMSComponentDescription jcd = (JAMSComponentDescription) clazz.getAnnotation(JAMSComponentDescription.class);
                        
                        if (jcd != null) {
                            compDesc = compDesc.replaceAll("%title%", jcd.title());
                            compDesc = compDesc.replaceAll("%author%", jcd.author());
                            compDesc = compDesc.replaceAll("%date%", jcd.date());
                            compDesc = compDesc.replaceAll("%description%", jcd.description());
                        } else {
                            compDesc = compDesc.replaceAll("%title%", "[none]");
                            compDesc = compDesc.replaceAll("%author%", "[none]");
                            compDesc = compDesc.replaceAll("%date%", "[none]");
                            compDesc = compDesc.replaceAll("%description%", "[none]");
                        }
                        
                        String componentvar;
                        String componentvars = "";
                        boolean interfaceFound = false;
                        
                        for (Field field : clazz.getFields()) {
                            
                            JAMSVarDescription jvd = (JAMSVarDescription) field.getAnnotation(JAMSVarDescription.class);
                            
                            componentvar = varTemplate;
                            componentvar = componentvar.replaceAll("%name%", field.getName());
                            if (jvd != null) {
                                interfaceFound = true;
                                JAMSVarDescription a = (JAMSVarDescription) jvd;
                                componentvar = componentvar.replaceAll("%type%", field.getType().getName());
                                componentvar = componentvar.replaceAll("%access%", jvd.access().toString());
                                componentvar = componentvar.replaceAll("%update%", jvd.update().toString());
                                componentvar = componentvar.replaceAll("%description%", jvd.description());
                                componentvar = componentvar.replaceAll("%unit%", jvd.unit());
                                componentvars += "\n"+componentvar;
                            }
                        }
                        if (!interfaceFound) {
                            componentvars = "No data found!";
                        }
                        compDesc = compDesc.replaceAll("%componentvars%", componentvars);
                        components.add(clazz);
                        componentDescriptions.put(clazz, compDesc);
                    }
                } catch (ClassNotFoundException exc) {}
            }
        }
        
        String menu = "";
        String oldPackage = "", newPackage;
        for (Class clazz : components) {
            newPackage = clazz.getPackage().getName();
            if (!newPackage.equals(oldPackage)) {
                menu += "<br/><i>" + newPackage + "</i><br/>\n";
                oldPackage = newPackage;
            }
            menu += "<p style='line-height:3px;margin-left:20px;'><a href=\"" + clazz.getName() + ".html\">" + clazz.getSimpleName() + "</a></p>\n";
        }
        
        String jarName = file.getCanonicalFile().getName();
        
        outPath = outPath+File.separatorChar+jarName.substring(0,jarName.length()-4)+File.separatorChar;
        File outPathFile = new File(outPath);
        deleteDirectory(outPathFile);
        outPathFile.mkdirs();
        
        for (Class clazz : components) {
            String compdesc = componentDescriptions.get(clazz);
            String html = overallTemplate;
            html = html.replaceAll("%complist%", menu);
            html = html.replaceAll("%compdesc%", "<br/>"+compdesc);
            
            html = html.replaceAll("%jar%", jarName);
            writeContent(outPath+clazz.getName()+".html", html);
        }
        String html = overallTemplate;
        html = html.replaceAll("%complist%", menu);
        html = html.replaceAll("%compdesc%", "<center><h3>Please select a component from the component list..</h3></center>");
        html = html.replaceAll("%jar%", jarName);
        writeContent(outPath+"index.html", html);
        
    }
    
    public static void main(String[] args) throws Exception {
        
        Class c = JAMSComponent.class;
        
        String jarDirName = "//limpopo/home_web/jamscomponents/jars/";
        String outDirName = "//limpopo/home_web/jamscomponents/docs/";

        jarDirName = args[0];
        outDirName = args[1];
        
        File jarDir = new File(jarDirName);
        if( jarDir.exists() ) {
            File[] files = jarDir.listFiles();
            for(int i=0; i<files.length; i++) {
                if (files[i].getCanonicalFile().getName().endsWith(".jar")) {
                    AnnotationDoc.createDoc(files[i].getAbsolutePath(), outDirName, JAMSComponent.class);                    
                }
            }
        }
        
        
        
        
        
        
        
    }
}
