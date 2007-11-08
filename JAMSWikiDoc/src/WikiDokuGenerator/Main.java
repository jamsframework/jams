/*
 * Main.java
 *
 * Created on 2. November 2007, 13:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package WikiDokuGenerator;

import java.io.File;
import net.sourceforge.jwbf.bots.MediaWikiBot;
import net.sourceforge.jwbf.contentRep.mw.SimpleArticle;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import java.util.jar.JarFile;
import org.unijena.jams.model.*;


/**
 *
 * @author admin
 */
public class Main {
            
    static MediaWikiBot bot = null;
    
    static String WikiLocation = "http://localhost/wiki/";
    static String WikiUser     = "WikiSysop";
    static String WikiPw       = "secret";
         
    public static boolean initBot() {
        bot = new MediaWikiBot(WikiLocation);
                
        try {
            bot.login(WikiUser, WikiPw);
        }catch(Exception e) {
            System.out.println("Could not login database, because:" + e.toString());
            return false;
        }
        return true;       
    }
    
    public static boolean sendContent(String articleName,String text){
        SimpleArticle a = new SimpleArticle();
        
        a.setText(text);
        a.setLabel(articleName);
        a.setMinorEdit(false);
        
        try {
            bot.writeContent(a);
        }
        catch(Exception e) {
            System.out.println("Could not write content!!, because:" + e.toString());
            return false;
        }
        return true;
    }
    
    public static boolean deleteAll() {
        
        return true;
    }
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
    
    public static void createDoc(String inJar, Class<?> superClass) throws Exception {
        
        JarFile jfile = new JarFile(inJar);
        
        String fileName = null;
        for(int i=inJar.length()-1;i>=0;i--){
            if (inJar.charAt(i)=='\\' || inJar.charAt(i)=='/' ) {
                fileName = inJar.substring(i+1,inJar.length());              
                break;
            }
        }
        if (fileName == null) {
            fileName = inJar;
        }
        
        File file = new File(inJar);
        URLClassLoader loader = new URLClassLoader(new URL[]{file.toURL()});
        
        Enumeration jarentries = jfile.entries();
        
        String compTemplate = readContent(ClassLoader.getSystemResourceAsStream("resources/templates/comptemplate.txt"));
        String varTemplate = readContent(ClassLoader.getSystemResourceAsStream("resources/templates/vartemplate.txt"));
        String packageTemplate = readContent(ClassLoader.getSystemResourceAsStream("resources/templates/packagetemplate.txt"));
        String packageListTemplate = readContent(ClassLoader.getSystemResourceAsStream("resources/templates/pkglisttemplate.txt"));
        
        HashMap<Class, String> componentDescriptions = new HashMap<Class, String>();
        ArrayList<Class> components = new ArrayList<Class>();
        
        //delete old components
        bot.RemoveAllArticlesWithString(jfile.getName());
        
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
                        compDesc = compDesc.replaceAll("%jarFile%", fileName);
                        
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
        
        String package_list = "";
        String oldPackage = "", newPackage;
        
        String mainpage = "";
        
        for (Class clazz : components) {
            newPackage = clazz.getPackage().getName();
            if (!newPackage.equals(oldPackage)) {
                //sending packagelist                
                if (oldPackage.length() >= 1) {
                    String package_desc = packageListTemplate;
                    package_desc = package_desc.replace("%name%",oldPackage);
                    package_desc = package_desc.replace("%complist%",package_list);
                    package_desc = package_desc.replace("%jarFile%",fileName);
                                    
                    mainpage += "===[[package "+oldPackage + "]]===\n" + package_list;

                    package_list = "";
                    sendContent("package_" + oldPackage, package_desc);
                    System.out.println("Sending:" + "package_" + oldPackage);                    
                }
                oldPackage = newPackage;
            }
            String package_item = packageTemplate.replace("%name%",clazz.getName());
            package_list += package_item;  //"<p style='line-height:3px;margin-left:20px;'><a href=\"" + clazz.getName() + ".html\">" + clazz.getSimpleName() + "</a></p>\n";
        }
        
        String jarName = file.getCanonicalFile().getName();
                
        for (Class clazz : components) {
            String compdesc = componentDescriptions.get(clazz);
            String html = compdesc;
            
            
            System.out.println("Sending:" + clazz.getName());
            sendContent(clazz.getName(), html);            
        }
        sendContent(fileName, mainpage);
        System.out.println("Sending:" + fileName);                    
    }

    
    public static void main(String[] args) {                
        Class c = JAMSComponent.class;
        
        String jarDirName = "D:\\JAMSBuild\\j2k\\dist\\";
        Vector<String> DeleteList = new Vector<String>();
        Vector<String> AddList = new Vector<String>();

        int mode = 0;
        for (int i=0;i<args.length-1;i++) {
            jarDirName = args[i];

            if (args[i].compareTo("user") == 0) {
                WikiUser = args[i+1];
                i++;
            }
            if (args[i].compareTo("password") == 0) {
                WikiPw = args[i+1];
                i++;
            }
            if (args[i].compareTo("location") == 0) {
                WikiLocation = args[i+1];
                i++;
            }
            if (args[i].compareTo("del") == 0) {
                DeleteList.add(args[i+1]);
                i++;
            }
            
            if (args[i].compareTo("update") == 0) {
                DeleteList.add(args[i+1]);
                AddList.add(args[i+1]);
                i++;
            }
            
            if (args[i].compareTo("add") == 0) {
                AddList.add(args[i+1]);
                i++;
            }
        }
        
        //try to login
        if (!initBot()) {
            System.out.println("Could not login! Check username, password and wiki location");
            return;
        }
        
        for (int i=0;i<DeleteList.size();i++) {            
            try {
                File jarDir = new File(DeleteList.get(i));
                if( jarDir.exists() ) {
                    File[] files = jarDir.listFiles();
                    for(int k=0; k<files.length; k++) {
                        bot.RemoveAllArticlesWithString(files[k].getName()+"]]<span style=\"color:white\">#</span>");
                    }
                }
                else {
                 bot.RemoveAllArticlesWithString(DeleteList.get(i)+"]]<p><span style=\"color:white\">#</span></p>");   
                }                                    
            }catch(Exception e) {
                System.out.println("Could not remove articles because: " + e.toString());
            }
        }            
        for (int i=0;i<AddList.size();i++) {
            File jarDir = new File(AddList.get(i));
            if( jarDir.exists() ) {
                File[] files = jarDir.listFiles();
                for(int k=0; k<files.length; k++) {
                    try {
                        if (files[k].getCanonicalFile().getName().endsWith(".jar")) {                                            
                            createDoc(files[k].getAbsolutePath(), JAMSComponent.class);
                        }
                    }catch(Exception e) {
                        System.out.println("Could not create doc, because:" + e.toString());
                    }
                }
            }            
        }                               
    }    
}
