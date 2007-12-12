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
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import org.unijena.jams.model.*;
import org.unijena.jams.model.JAMSComponent;

/**
 *
 * @author admin
 */
public class Main {
    private static final Class[] parameters = new Class[]{URL.class};         
    static MediaWikiBot bot = null;
    
    static String WikiLocation = "http://localhost/wiki/";
    static String WikiUser     = "WikiSysop";
    static String WikiPw       = "secret";
         
    static JDialog ProgrammInformation = new JDialog();
    static JProgressBar Progress       = new JProgressBar(0,100);
    
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
    
    static String replaceAll(String source,String expr,String repl){
        String result = "";
        boolean inMatch = false;
        int matchOffset = 0;
        for (int i=0;i<source.length();i++){
            if (inMatch){
                matchOffset++;
                if (matchOffset >= expr.length()){
                    result += repl + source.charAt(i);
                    inMatch = false;
                }
                else if (source.charAt(i)!=expr.charAt(matchOffset)){
                    inMatch = false;
                    result += source.substring(i-matchOffset,i);
                }                
            }
            else{
                if (source.charAt(i) == expr.charAt(0)) {
                    inMatch = true;
                    matchOffset = 0;
                }
                else{
                    result += source.charAt(i);
                }
            }
        }        
        if (inMatch && matchOffset+1 == expr.length()){
            result += repl;
        }
        return result;
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
                
        Progress.setString("Adding Articles of Package " + inJar);
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
        
        String compTemplate =   "<big>'''[[%package%]]'''</big><br />\n" +
                                "==%class%==\n" +
                                ":jar: [[%jarFile%]]\n"+
                                "===Component description===\n"+
                                ":Title: %title%\n"+
                                ":Author: %author%\n"+
                                ":Date: %date%\n"+
                                ":Description: %description%\n"+
                                "\n"+
                                "===Interface description===\n"+
                                "%componentvars%\n"+
                                "<span style=\"color:white\">%IDCODE%</span>\n"; //readContent(ClassLoader.getSystemResourceAsStream("resources/templates/comptemplate.txt"))";
        String varTemplate =    "====%name%====\n" +
                                ":Type: %type%\n"  +
                                ":Access: %access%\n" +
                                ":Description: %description%\n" + 
                                ":Unit: %unit%\n"; // readContent(ClassLoader.getSystemResourceAsStream("resources/templates/vartemplate.txt"));
        String packageTemplate = ":[[%name%]] <br />\n";//readContent(ClassLoader.getSystemResourceAsStream("resources/templates/packagetemplate.txt"));
        String packageListTemplate = "jar: [[%jarFile%]]<span style=\"color:white\">#</span>\n" +
                                     "===Components===\n" +
                                     "%complist%\n" +
                                     "<span style=\"color:white\">%IDCODE%</span>\n"; //readContent(ClassLoader.getSystemResourceAsStream("resources/templates/pkglisttemplate.txt"));
                
        HashMap<Class, String> componentDescriptions = new HashMap<Class, String>();
        ArrayList<Class> components = new ArrayList<Class>();
        
        //delete old components
        String idCode = "11"+fileName.replace(".","").toUpperCase()+"11";
        
        Progress.setString("Remove old articles of Package:" + inJar);
        bot.RemoveAllArticlesWithString(idCode);
        Progress.setString("Building Articles");
        
        while (jarentries.hasMoreElements()) {
            String entry = jarentries.nextElement().toString();
            if (!entry.endsWith(".class"))
                continue;
                
            String classString = entry.substring(0,entry.length()-6);
            classString = classString.replace("/", ".");
            Class<?> clazz = null;
            try {
                clazz = loader.loadClass(classString);
            }catch(java.lang.NoClassDefFoundError e){
                System.out.println("Class: " + classString + " was not found!, because " + e.toString());
                continue;
            }
            if (clazz == null)
                continue;
                
            if (superClass.isAssignableFrom(clazz)) {
                String compDesc = compTemplate;
                compDesc = compDesc.replace("%package%", clazz.getPackage().toString());
                compDesc = compDesc.replace("%class%", clazz.getSimpleName());
                compDesc = compDesc.replace("%jarFile%", fileName);
                compDesc = compDesc.replace("%IDCODE%", idCode);
                JAMSComponentDescription jcd = (JAMSComponentDescription) clazz.getAnnotation(JAMSComponentDescription.class);
                        
                if (jcd != null) {
                    compDesc = compDesc.replace("%title%", jcd.title());
                    compDesc = compDesc.replace("%author%", jcd.author());
                    compDesc = compDesc.replace("%date%", jcd.date());
                    compDesc = compDesc.replace("%description%", jcd.description());
                } else {
                    compDesc = compDesc.replace("%title%", "[none]");
                    compDesc = compDesc.replace("%author%", "[none]");
                    compDesc = compDesc.replace("%date%", "[none]");
                    compDesc = compDesc.replace("%description%", "[none]");
                }
                        
                String componentvar;
                String componentvars = "";
                boolean interfaceFound = false;
                try {    
                    Field field[] = null;
                    try{
                    field = clazz.getFields();
                    }catch(java.lang.NoClassDefFoundError e){
                        System.out.println("Cant load fields of class:" + clazz.getName() + " ,because:" + e.toString());
                        continue;
                    }
                    for (int i=0;i<field.length;i++) {                            
                        JAMSVarDescription jvd = (JAMSVarDescription) field[i].getAnnotation(JAMSVarDescription.class);
                        System.out.println(field[i].toString());    
                        componentvar = varTemplate;
                        componentvar = componentvar.replace("%name%", field[i].getName());
                        if (jvd != null) {
                            interfaceFound = true;
                            JAMSVarDescription a = (JAMSVarDescription) jvd;
                            String tmp = field[i].getType().getName();
                            tmp = tmp.replace('$',' ');
                            componentvar = componentvar.replace("%type%", tmp);
                            componentvar = componentvar.replace("%access%", jvd.access().toString());
                            componentvar = componentvar.replace("%update%", jvd.update().toString());
                            String desc = jvd.description();
                            componentvar = componentvar.replace("%description%",desc );
                            componentvar = componentvar.replace("%unit%", jvd.unit());
                            componentvars += "\n"+componentvar;
                        }
                    }
                    if (!interfaceFound) {
                        componentvars = "No data found!";
                    }
                    compDesc = compDesc.replace("%componentvars%", componentvars);
                    components.add(clazz);
                    componentDescriptions.put(clazz, compDesc);
                } catch (Exception exc) {
                    System.out.println("Error:" + exc.toString());
                }
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
                    package_desc = package_desc.replace("%IDCODE%", idCode);
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
        
        //send last package
        String package_desc = packageListTemplate;
        package_desc = package_desc.replace("%name%",oldPackage);
        package_desc = package_desc.replace("%complist%",package_list);
        package_desc = package_desc.replace("%jarFile%",fileName);
        package_desc = package_desc.replace("%IDCODE%", idCode);
        
        mainpage += "===[[package "+oldPackage + "]]===\n" + package_list;

        package_list = "";
        sendContent("package_" + oldPackage, package_desc);
        Progress.setString("Adding Article:" + "package_" + oldPackage);
        System.out.println("Sending:" + "package_" + oldPackage);         
        
        String jarName = file.getCanonicalFile().getName();
                
        for (Class clazz : components) {
            String compdesc = componentDescriptions.get(clazz);
            String html = compdesc;
                        
            System.out.println("Sending:" + clazz.getName());
            Progress.setString("Adding Article:" + clazz.getName());
            sendContent(clazz.getName(), html);            
        }
        Progress.setString("Adding Article:" + fileName);
        sendContent(fileName, mainpage + "Download: [[Bild:"+fileName.replace(".jar",".zip")+"]]\n"+"<span style=\"color:white\">" + idCode + "</span>");
        System.out.println("Sending:" + fileName);                    
    }

    
    public static void main(String[] args) {                
        Class c = JAMSComponent.class;
                        
        FileOutputStream f = null;
        try {
            f = new FileOutputStream("wikidocu.log");
        }catch(Exception e){
            System.out.println("Kann log nicht erstellen" + e.toString());
        }
        PrintStream p = new PrintStream(f);
        System.setOut(p);
                    
        ProgrammInformation.add(Progress);
        ProgrammInformation.setTitle("Upload Status");
        
        ProgrammInformation.setSize(500,60);
        ProgrammInformation.setLocationByPlatform(true);
        ProgrammInformation.setVisible(true);
        Progress.setValue(0);        
        Progress.setString("Initialize");
                
        String jarDirName = "D:\\JAMSBuild\\j2k\\dist\\";
        Vector<String> DeleteList = new Vector<String>();
        Vector<String> AddList = new Vector<String>();
        Vector<String> UploadList = new Vector<String>();
        Vector<String> UnloadList = new Vector<String>();

        //parse arguments        
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
            
            if (args[i].compareTo("upload") == 0) {
                UploadList.add(args[i+1]);
                i++;
            }
            
            if (args[i].compareTo("unload") == 0) {
                UnloadList.add(args[i+1]);
                i++;
            }
        }
        Progress.setValue(0);
        Progress.setString("Login");
        Progress.setStringPainted(true);
        ProgrammInformation.setAlwaysOnTop(true);
        //try to login
        if (!initBot()) {
            JOptionPane.showMessageDialog(ProgrammInformation,"Could not login! Check username, password and wiki location");            
            ProgrammInformation.dispose();
            return;
        }
        
        double globalOperations = DeleteList.size() + UnloadList.size() + UploadList.size() + AddList.size() + 1;
        double opCounter = 1;
        //upload files
        for (int i=0;i<UploadList.size();i++) {            
            Progress.setValue((int)((opCounter/globalOperations)*100.0));
            opCounter += 1.0;
            Progress.setString("Uploading File:" + UploadList.get(i));
            bot.uploadFile(UploadList.get(i));
        }        
        //delete files
        for (int i=0;i<DeleteList.size();i++) {                    
            Progress.setValue((int)((opCounter/globalOperations)*100.0));
            opCounter += 1.0;
            Progress.setString("Deleting Articles of Package" + DeleteList.get(i));
            try {
                File jarDir = new File(DeleteList.get(i));
                //does file exist?
                if( jarDir.exists() ) {
                    File[] files = jarDir.listFiles();
                    //no directory?
                    if (files == null) {
                        files = new File[1];
                        files[0] = jarDir;
                    }
                    for(int k=0; k<files.length; k++) {                        
                        bot.RemoveAllArticlesWithString("11"+files[k].getName().replace(".","").toUpperCase()+"11");
                    }
                }
                //search for name
                else {
                    bot.RemoveAllArticlesWithString("11"+DeleteList.get(i).replace(".","").toUpperCase()+"11");   
                }                                    
            }catch(Exception e) {
                JOptionPane.showMessageDialog(null,"Could not remove articles");            
                System.out.println("Could not remove articles because: " + e.toString());
            }
        }        
        //delete        
        for (int i=0;i<UnloadList.size();i++) {
            Progress.setValue((int)((opCounter/globalOperations)*100.0));
            opCounter += 1.0;
            Progress.setString("Deleting File:" + UnloadList.get(i));
            try{
                bot.removeArticle("Bild:"+UnloadList.get(i));
            }catch(Exception e){
                JOptionPane.showMessageDialog(null,"Could not remove articles");            
                System.out.println("Could not remove articles because: " + e.toString());
            }
        }
        
        //add files
        for (int i=0;i<AddList.size();i++) {
            Progress.setValue((int)((opCounter/globalOperations)*100.0));
            opCounter += 1.0;            
            Progress.setString("Deleting Articles of Package: " + AddList.get(i));
            
            File jarDir = new File(AddList.get(i));
            //file must exist
            if( jarDir.exists() ) {
                File[] files = jarDir.listFiles();
                //no directory?
                if (files == null) {
                    files = new File[1];
                    files[0] = jarDir;
                }
                for(int k=0; k<files.length; k++) {
                    try {
                        if (files[k].getCanonicalFile().getName().endsWith(".jar")) {                                            
                            createDoc(files[k].getAbsolutePath(), JAMSComponent.class);
                        }
                    }catch(Exception e) {
                        JOptionPane.showMessageDialog(null,"Could not create doc");     
                        System.out.println("Could not create doc, because:" + e.toString());
                    }
                }
            }            
        }
        Progress.setString("Upload Process finished successfully");
        System.out.println("Upload Process finished successfully");
        ProgrammInformation.setVisible(false);
        try {
            f.close();
        }catch(Exception e){
            System.out.println("Could not close file, because:" + e.toString());
        }        
    ProgrammInformation.dispose();
    return;
    }    
}
