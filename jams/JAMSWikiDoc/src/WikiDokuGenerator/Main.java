/*
 * Main.java
 *
 * Created on 2. November 2007, 13:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package WikiDokuGenerator;

import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import java.awt.Component;
import net.sourceforge.jwbf.bots.MediaWikiBot;
import net.sourceforge.jwbf.contentRep.mw.SimpleArticle;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.jar.JarFile;
import javax.swing.JOptionPane;
import org.unijena.jams.model.*;
import org.unijena.jams.model.JAMSComponent;

public class Main {
    private static final Class[] parameters = new Class[]{URL.class};         
    MediaWikiBot bot = null;
    
    String WikiLocation = "http://localhost/wiki/";
    String WikiUser     = "WikiSysop";
    String WikiPw       = "secret";
    public String statusText = "";
                 
    ClassLoader parentLoader;
    Component parentComponent;
    
    public boolean initBot() {
        bot = new MediaWikiBot(WikiLocation);
         
        try {
            bot.login(WikiUser, WikiPw);
        }catch(Exception e) {
            JOptionPane.showMessageDialog(parentComponent,"Could not login database, because:" + e.toString());    
            return false;
        }
        return true;       
    }
        
    public boolean sendContent(String articleName,String text){
        SimpleArticle a = new SimpleArticle();
        
        a.setText(text);
        a.setLabel(articleName);
        a.setMinorEdit(false);
        
        try {
            bot.writeContent(a);
        }
        catch(Exception e) {
            JOptionPane.showMessageDialog(parentComponent,"Could not write content!!, because:" + e.toString());    
            return false;
        }
        return true;
    }
    
    public static String GetFileName(String path){
        String fileName = path;
        for(int i=path.length()-1;i>=0;i--){
            if (path.charAt(i)=='\\' || path.charAt(i)=='/' ) {
                fileName = path.substring(i+1,path.length());              
                break;
            }
        }        
        return fileName;        
    }
    
    public static String MakeIDCode(String fileName){        
        return "11"+fileName.replace(".","").toUpperCase()+"11";
    }
    
    public void updateDocToWiki(String inJar, Class<?> superClass,Vector<String> components) throws Exception {
        HashMap<String,String> contentList = new HashMap();

        statusText = "Create new Articles";        
        createDoc(inJar,superClass,contentList,null);

        statusText = "Adding Articles of Package " + inJar;
                
         //delete old components   
        if (components == null){
            this.DeleteArticle(inJar);            
            //bot.RemoveAllArticlesWithString(MakeIDCode(GetFileName(inJar)));
        }
        Iterator<Entry<String,String>> e = contentList.entrySet().iterator();
        while(e.hasNext()){
            Entry<String,String> entry = e.next();
            boolean contains = false;
            if (components != null){
                for (int i=0;i<components.size();i++){
                    if ( components.get(i).compareToIgnoreCase(entry.getKey()) == 0){
                        contains = true;                        
                        break;
                    }
                }                
            }
            else{
                contains = true;
            }
            if ( contains ){
                statusText = "Sending page: " + entry.getKey();
                sendContent(entry.getKey(),entry.getValue());
            }
        }
    }
    
    public void createDoc(String inJar, Class<?> superClass,HashMap<String,String> contentList,Vector<String> componentsList) throws Exception {        
        JarFile jfile = new JarFile(inJar);                                        
        File file = new File(inJar);        
        URLClassLoader loader = new URLClassLoader(new URL[]{file.toURL()}, parentLoader);
        
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
                 
        String fileName = GetFileName(inJar);        
        String idCode   = MakeIDCode(fileName);
        
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
                JOptionPane.showMessageDialog(parentComponent,"Class: " + classString + " was not found!, because " + e.toString());  
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
                        JOptionPane.showMessageDialog(parentComponent,"Can't load fields of class:" + clazz.getName() + " ,because:" + e.toString());  
                        continue;
                    }
                    for (int i=0;i<field.length;i++) {                            
                        JAMSVarDescription jvd = (JAMSVarDescription) field[i].getAnnotation(JAMSVarDescription.class);
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
                     JOptionPane.showMessageDialog(parentComponent,"Error:" + exc.toString());  
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
                    contentList.put("package_" + oldPackage,new String(package_desc));                    
                }
                oldPackage = newPackage;
            }
            String package_item = packageTemplate.replace("%name%",clazz.getName());
            package_list += package_item;
        }
        
        //send last package
        String package_desc = packageListTemplate;
        package_desc = package_desc.replace("%name%",oldPackage);
        package_desc = package_desc.replace("%complist%",package_list);
        package_desc = package_desc.replace("%jarFile%",fileName);
        package_desc = package_desc.replace("%IDCODE%", idCode);
        
        mainpage += "===[[package "+oldPackage + "]]===\n" + package_list;

        package_list = "";
        contentList.put("package_" + oldPackage, new String(package_desc));                      
        String jarName = file.getCanonicalFile().getName();        
        for (Class clazz : components) {
            String compdesc = componentDescriptions.get(clazz);
            String html = compdesc;
             
            if (componentsList != null)
                componentsList.add(clazz.getName());            
            if (contentList != null)
                contentList.put(clazz.getName(), html);
        }        
        contentList.put(new String(fileName),new String(mainpage + "Download: [[Bild:"+fileName.replace(".jar",".zip")+"]]\n"+"<span style=\"color:white\">" + idCode + "</span>"));               
    }
        
    public static File[] getFiles(String inFile){
        File jarDir = new File(inFile);
        File[] files = null;
        //does file exist?
        if( jarDir.exists() ) {
            files = jarDir.listFiles();
            //no directory?
            if (files == null) {
                files = new File[1];
                files[0] = jarDir;
            }
        }
        else
            return null;
        return files;
    }
        
    public Main(ClassLoader _parentClassLoader, Component _parentComponent){           
        this.parentLoader = _parentClassLoader;
        this.parentComponent = _parentComponent;
    }
    
    public boolean initUploadProcess(String _WikiUser,String _WikiPw, String _WikiLocation){                               
        WikiUser = _WikiUser;
        WikiPw   = _WikiPw;
        WikiLocation = _WikiLocation;

        statusText = "Login";
        
        //try to login
        if (!initBot()) {
            JOptionPane.showMessageDialog(parentComponent,"Could not login! Check username, password and wiki location");            
            return false;
        }
        return true;
    }
    
    public void finishUploadProcess(){
        statusText = "Upload Process finished successfully";
    }
     
    public boolean UploadFile(String File){
        statusText = "Upload File:" + File;
        bot.uploadFile(File);
        return true;
    }
     
    public boolean DeleteFile(String File){
        try{
            statusText = "Delete File:" + File;
            bot.removeArticle("Bild:"+File);
        }catch(Exception e){
            JOptionPane.showMessageDialog(parentComponent,"Could not remove articles");             
            return false;
        }
        return true;
    }
                    
    public boolean DeleteArticle(String File){
        try {
            File[] files = getFiles(File);
                
            if (files != null){                
                for(int k=0; k<files.length; k++) { 
                    statusText = "Searching for old articles of Package:" + files[k].getName();
                    Vector<String> ArticlesList = bot.SearchForString("11"+files[k].getName().replace(".","").toUpperCase()+"11");
                    for (int i=0;i<ArticlesList.size();i++){
                        statusText = "Removing Article: " + ArticlesList.get(i);
                        bot.removeArticle(ArticlesList.get(i));
                    }
                }
            }
            //search for name
            else {                
                statusText = "Searching for old articles of Package:" + File;
                Vector<String> ArticlesList = bot.SearchForString("11"+File.replace(".","").toUpperCase()+"11");
                for (int i=0;i<ArticlesList.size();i++){
                    statusText = "Removing Article: " + ArticlesList.get(i);
                    bot.removeArticle(ArticlesList.get(i));
                }
            }                                    
        }catch(Exception e) {
            JOptionPane.showMessageDialog(parentComponent,"Could not remove articles");            
        }
        return true;
    }
    
    public boolean AddArticle(String File){            
        File[] files = getFiles(File);
        if (files != null){ 
            for(int k=0; k<files.length; k++) {
                try {
                    if (files[k].getCanonicalFile().getName().endsWith(".jar")) {                                            
                        updateDocToWiki(files[k].getAbsolutePath(),JAMSComponent.class,null);
                    }
                }catch(Exception e) {
                    JOptionPane.showMessageDialog(parentComponent,"Could not create doc");     
                }
            }            
        }
        return true;
    }   
    
    //update components
    public boolean UpdateComponents(String jarFile,Vector<String> components){
        File[] files = getFiles(jarFile);
        if (files != null){
        try{
            updateDocToWiki(files[0].getAbsolutePath(),JAMSComponent.class, components);
        }catch(Exception e){
            JOptionPane.showMessageDialog(parentComponent,"Could not create doc");                 
            }
        }
        return true;
    }
    
    public Vector<String> ListComponents(String jarFile){
        HashMap<String,String> dump = new HashMap();        
        Vector<String> componentsList = new Vector<String>();
        try{
            createDoc(jarFile, JAMSComponent.class,dump,componentsList);
        }catch(Exception e) {
            return null;
        }
        return componentsList;
    }
}
