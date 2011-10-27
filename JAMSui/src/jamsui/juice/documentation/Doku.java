package jamsui.juice.documentation;

import jams.JAMSProperties;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.tools.StringTools;
import jamsui.juice.JUICE;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.swing.JOptionPane;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author sa63kul
 */
public class Doku {

    public static String GetFileName(String path) {
        String fileName = path;
        for (int i = path.length() - 1; i >= 0; i--) {
            if (path.charAt(i) == '\\' || path.charAt(i) == '/') {
                fileName = path.substring(i + 1, path.length());
                break;
            }
        }
        return fileName;
    }

    static public void createDoc(String directory, String inJar, Class<?> superClass, HashMap<String, String> contentList, Vector<String> componentsList) throws Exception {
        JarFile jfile = new JarFile(inJar);
        File file = new File(inJar);
        URLClassLoader loader = new URLClassLoader(new URL[]{file.toURL()}, ClassLoader.getSystemClassLoader());

        Enumeration jarentries = jfile.entries();


        String compTemplate = "<informaltable>"
                + "<tgroup cols=\"3\">\n"
                + "<colspec colname=\"c1\" colnum=\"1\" colwidth=\"1.0*\"/>\n"
                + "<colspec colname=\"c2\" colnum=\"2\" colwidth=\"5.74*\"/>\n"
                + "<thead>\n"
                + "<row>\n"
                + "<entry namest=\"c1\" nameend=\"c2\">Metainformationen</entry>\n"
                + "</row>\n"
                + "</thead>\n"
                + "<tbody>\n"
                + "<row>\n"
                + "<entry>Komponente</entry>\n"
                + "<entry namest=\"c2\" nameend=\"newCol3\">%class%</entry>\n"
                + "</row>\n"
                + "<row>\n"
                + "<entry>Modellprozess</entry>\n"
                + "<entry namest=\"c2\" nameend=\"newCol3\">bitte ausfuellen</entry>\n"
                + "</row>\n"
                + "<row>\n"
                + "<entry>Paket</entry>\n"
                + "<entry namest=\"c2\" nameend=\"newCol3\">%package%</entry>\n"
                + "</row>\n"
                + "<row>\n"
                + "<entry>Autor</entry>\n"
                + "<entry namest=\"c2\" nameend=\"newCol3\">%author%</entry>\n"
                + "</row>\n"
                + "<row>\n"
                + "<entry>Version</entry>\n"
                + "<entry namest=\"c2\" nameend=\"newCol3\">%version%</entry>\n"
                + "</row>\n"
                + "<row>\n"
                + "<entry>Modifikationsdatum</entry>\n"
                + "<entry namest=\"c2\" nameend=\"newCol3\">%date%</entry>\n"
                + "</row>\n"
                + "</tbody>\n"
                + "</tgroup>\n"
                + "</informaltable>\n"
                + "<informaltable>\n"
                + "<tgroup cols=\"7\">\n"
                + "<colspec colname=\"c1\" colnum=\"1\" colwidth=\"1.0*\"/>\n"
                + "<colspec colname=\"newCol2\" colnum=\"2\" colwidth=\"2.96*\"/>\n"
                + "<colspec colname=\"c2\" colnum=\"3\" colwidth=\"1.15*\"/>\n"
                + "<colspec colname=\"newCol4\" colnum=\"4\" colwidth=\"1.23*\"/>\n"
                + "<colspec colname=\"newCol5\" colnum=\"5\" colwidth=\"1.1*\"/>\n"
                + "<colspec colname=\"newCol6\" colnum=\"6\" colwidth=\"1.25*\"/>\n"
                + "<colspec colname=\"newCol7\" colnum=\"7\" colwidth=\"1.28*\"/>\n"
                + "<thead>\n"
                + "<row>\n"
                + "<entry namest=\"c1\" nameend=\"newCol7\">Variablen</entry>\n"
                + "</row>\n"
                + "<row>\n"
                + "<entry>Variable</entry>\n"
                + "<entry>Beschreibung</entry>\n"
                + "<entry>Einheit</entry>\n"
                + "<entry>Wertebereich</entry>\n"
                + "<entry>Datentyp</entry>\n"
                + "<entry>Variablentyp</entry>\n"
                + "<entry>Defaultwert</entry>\n"
                + "</row>\n"
                + "</thead>\n"
                + "<tbody>\n"
                + "%componentvars%\n"
                + " </tbody>\n"
                + "</tgroup>\n"
                + "</informaltable>\n";





        String varTemplate =
                "<row>\n"
                + "<entry>%name%</entry>\n"
                + "<entry>%description%</entry>\n"
                + "<entry>%unit%</entry>\n"
                + "<entry>%lowerBound%...%upperBound%</entry>\n"
                + "<entry>%type%</entry>\n"
                + "<entry>%access%</entry>\n"
                + "<entry>%defaultValue%</entry>\n"
                + "</row>";

        String packageTemplate = ":[[%name%]] <br />\n";//readContent(ClassLoader.getSystemResourceAsStream("resources/templates/packagetemplate.txt"));
        String packageListTemplate = "";

        HashMap<Class, String> componentDescriptions = new HashMap<Class, String>();
        ArrayList<Class> components = new ArrayList<Class>();

        String fileName = GetFileName(inJar);
        //String idCode   = MakeIDCode(fileName);

        while (jarentries.hasMoreElements()) {
            String entry = jarentries.nextElement().toString();
//            Object entry1=jarentries.nextElement(); //neu
            if (entry.endsWith(".xml")) {
               

                ZipEntry zipentry = jfile.getEntry(entry);
                File efile = new File(zipentry.getName());
                String filename = zipentry.getName();
                filename = filename.replaceAll("/", ".");

                jfile.getInputStream(zipentry);
                InputStream in = new BufferedInputStream(jfile.getInputStream(zipentry));
                OutputStream out = new BufferedOutputStream(new FileOutputStream(directory + filename));
                byte[] buffer = new byte[4096];
                for (;;) {
                    int nBytes = in.read(buffer);
                    if (nBytes <= 0) {
                        break;
                    }
                    out.write(buffer, 0, nBytes);
                }

                /*               while (in.available() > 0)
                {  // write contents of 'in' to 'out'
                out.write(in.read());
                }
                 */
                out.flush();
                out.close();
                in.close();


            }
            
            if (entry.endsWith(".png")) {
               

                ZipEntry zipentry = jfile.getEntry(entry);
                File efile = new File(zipentry.getName());
                String filename = zipentry.getName();
                filename = filename.replaceAll("/", ".");

                   char[] c = filename.toCharArray();
                    int start = 0;
                    boolean first_point=false;
                    filename=filename.replaceAll("/",".");
                    for (int i = c.length - 1; i > 0; i--) {

                        if (c[i] == '.') {
                            start = i;
                            if (first_point==true)
                            {
                                i = 0;
                            }
                            first_point=true;
                        }
                    }
                 
                    filename = filename.substring(start +1, c.length);
                
                jfile.getInputStream(zipentry);
                InputStream in = new BufferedInputStream(jfile.getInputStream(zipentry));
                OutputStream out = new BufferedOutputStream(new FileOutputStream(directory + filename));
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
            if (!entry.endsWith(".class") && !entry.endsWith(".xml") && !entry.endsWith(".png")) {
                continue;
            }
            if (entry.endsWith(".class")) {
                String classString = entry.substring(0, entry.length() - 6);
                classString = classString.replace("/", ".");
                Class<?> clazz = null;
                try {
                    clazz = loader.loadClass(classString);
                } catch (java.lang.NoClassDefFoundError e) {
                    // JOptionPane.showMessageDialog(null, "Class: " + classString + " was not found!, because " + e.toString());
                    System.out.println(e);
                    e.printStackTrace();
                    continue;
                }
                if (clazz == null) {
                    continue;
                }
                if (superClass.isAssignableFrom(clazz)) {
                    String compDesc = compTemplate;
                    compDesc = compDesc.replace("%package%", clazz.getPackage().toString());
                    compDesc = compDesc.replace("%class%", clazz.getSimpleName());
                    compDesc = compDesc.replace("%jarFile%", fileName);
                    //compDesc = compDesc.replace("%IDCODE%", idCode);
                    JAMSComponentDescription jcd = (JAMSComponentDescription) clazz.getAnnotation(JAMSComponentDescription.class);

                    /*  String author="";
                    if (!jcd.author().isEmpty())
                    {
                    author=jcd.author();
                    }

                    if (jcd.author().contains("&"))
                    {
                    author=jcd.author().replace("&", "and");
                    }
                     */
                    if (jcd != null) {
                        compDesc = compDesc.replace("%title%", jcd.title());
                        compDesc = compDesc.replace("%author%", jcd.author().replace("&", "and"));
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

                    String componentvar;
                    String componentvars = "";
                    boolean interfaceFound = false;
                    try {
                        Field field[] = null;
                        try {
                            field = clazz.getFields();
                        } catch (java.lang.NoClassDefFoundError e) {
                            System.out.println(e);
                            e.printStackTrace();
                            //JOptionPane.showMessageDialog(null, "Can't load fields of class:" + clazz.getName() + " ,because:" + e.toString());
                            continue;
                        }
                        for (int i = 0; i < field.length; i++) {
                            jams.model.JAMSVarDescription jvd = (jams.model.JAMSVarDescription) field[i].getAnnotation(jams.model.JAMSVarDescription.class);
                            componentvar = varTemplate;
                            componentvar = componentvar.replace("%name%", field[i].getName());
                            if (jvd != null) {
                                interfaceFound = true;
                                jams.model.JAMSVarDescription a = (jams.model.JAMSVarDescription) jvd;
                                String tmp = field[i].getType().getName();
                                tmp = tmp.replace('$', ' ');
                                componentvar = componentvar.replace("%type%", tmp);
                                componentvar = componentvar.replace("%access%", jvd.access().toString());
                                componentvar = componentvar.replace("%update%", jvd.update().toString());
                                String desc = jvd.description();
                                componentvar = componentvar.replace("%description%", desc);
                                componentvar = componentvar.replace("%unit%", jvd.unit());
                                componentvar = componentvar.replace("%upperBound%", Double.toString(jvd.upperBound()));
                                componentvar = componentvar.replace("%lowerBound%", Double.toString(jvd.lowerBound()));
                                componentvar = componentvar.replace("%defaultValue%", jvd.defaultValue().toString());
                                componentvars += "\n" + componentvar;
                            }
                        }
                        if (!interfaceFound) {
                            componentvars = "No data found!";
                        }
                        compDesc = compDesc.replace("%componentvars%", componentvars);
                        components.add(clazz);
                        componentDescriptions.put(clazz, compDesc);
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(null, "Error:" + exc.toString());
                    }
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
                    package_desc = package_desc.replace("%name%", oldPackage);
                    package_desc = package_desc.replace("%complist%", package_list);
                    package_desc = package_desc.replace("%jarFile%", fileName);
                    //  package_desc = package_desc.replace("%IDCODE%", idCode);
                    mainpage += "===[[package " + oldPackage + "]]===\n" + package_list;

                    package_list = "";
                    contentList.put("package_" + oldPackage, new String(package_desc));
                }
                oldPackage = newPackage;
            }
            String package_item = packageTemplate.replace("%name%", clazz.getName());
            package_list += package_item;
        }

        //send last package
        String package_desc = packageListTemplate;
        package_desc = package_desc.replace("%name%", oldPackage);
        package_desc = package_desc.replace("%complist%", package_list);
        package_desc = package_desc.replace("%jarFile%", fileName);

        //package_desc = package_desc.replace("%IDCODE%", idCode);

        mainpage += "===[[package " + oldPackage + "]]===\n" + package_list;

        package_list = "";
        contentList.put("package_" + oldPackage, new String(package_desc));
        String jarName = file.getCanonicalFile().getName();
        for (Class clazz : components) {
            String compdesc = componentDescriptions.get(clazz);
            String html = compdesc;

            if (componentsList != null) {
                componentsList.add(clazz.getName());
            }
            if (contentList != null) {
                contentList.put(clazz.getName(), html);
            }
        }
        //  contentList.put(new String(fileName),new String(mainpage + "Download: [[Bild:"+fileName.replace(".jar",".zip")+"]]\n"+"<span style=\"color:white\">" + idCode + "</span>"));
    }

    public static void AnnotationUpdate(String directory, String model_jar) {

       
        //legt zu jeder Komponente eine Datei an, in welcher die Bemerkungen aus dem Quellcode enthalten sind





        directory = directory + "/";
        HashMap<String, String> test = new HashMap<String, String>();
        Vector<String> vector = new Vector<String>();
        String path = "";


        try {
            createDoc(directory, model_jar, JAMSComponent.class, test, vector);
            Iterator<String> iter = test.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                String value = test.get(key);
                path = directory + "Component_Annotation." + key + ".xml"; //pfad fï¿½r neue Komponente
                String component_title;
                component_title = key; //ermittelt komponentenname
                char[] c = component_title.toCharArray();
                int start = 0;
                for (int i = c.length - 1; i > 0; i--) {
                    if (c[i] == '.') {
                        start = i;
                        i = 0;
                    }
                }
                component_title = component_title.substring(start + 1, c.length);


                BufferedWriter out = new BufferedWriter(new FileWriter(path));
                out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                out.write("<sect1 xmlns=\"http://docbook.org/ns/docbook\" version=\"5.0\">\n");
                out.write("<title>" + key + "</title>\n");
                out.write("<subtitle/>\n");
                out.write(value);
               
                out.write("</sect1>\n");
                out.close();

            }
        } catch (Exception E) {
            E.printStackTrace();
        }


    }

    public static void createDocumentation(File dataPath, File model, File model_jar, File[] environment_jar) {
        String args[] = new String[environment_jar.length + 4];
        args[0] = "dummy";
        args[1] = dataPath.getAbsolutePath();
        args[2] = model.getAbsolutePath();
        args[3] = model_jar.getAbsolutePath();
        for (int i = 0; i < environment_jar.length; i++) {
            args[i + 4] = environment_jar[i].getAbsolutePath();
        }
        main(args);
    }
    
    private static ArrayList<String> getJarList(String[] libsArray) {
        
        ArrayList<String> result = new ArrayList<String>();

        for (int i = 0; i < libsArray.length; i++) {
            File file = new File(libsArray[i]);

            if (!file.exists()) {
                continue;
            }
            if (file.isDirectory()) {
                File[] f = file.listFiles();
                for (int j = 0; j < f.length; j++) {
                    if (f[j].getName().endsWith(".jar")) {
                        ArrayList<String> subResult = getJarList(new String[]{f[j].toString()});
                        if (!subResult.isEmpty()) {
                            result.addAll(subResult);
                        }
                    }
                }
            } else {
                result.add(file.toString());
            }
        }
        
        return result;
    }    
    
    public static void main(String arg[]) {
        //   Locale loc = Locale.ENGLISH;
        //   Locale.setDefault(loc);
         String directory = arg[1];
        String[] libList = StringTools.toArray(JUICE.getJamsProperties().getProperty(JAMSProperties.LIBS_IDENTIFIER), ";");
        ArrayList<String> list = getJarList(libList);
        for (String s : list) {
            System.out.println("lib : " + s);
             AnnotationUpdate(directory, s);
        }

        String model = arg[2]; 
   
        String pathin_vorlage = directory + "/template.xml";


        ArrayList<ArrayList<String[]>> componentParameter = new ArrayList<ArrayList<String[]>>();
        componentParameter = Parameter_XML(directory, model, componentParameter);
        Documentation_Complete(directory, model, pathin_vorlage, componentParameter);


    }

    public static ArrayList<ArrayList<String[]>> Parameter_XML(String directory, String model, ArrayList<ArrayList<String[]>> component) {
        directory = directory + "/";
        // ArrayList<ArrayList<String[]>> component = new ArrayList<ArrayList<String[]>>();
        ArrayList<String[]> parameterList = new ArrayList<String[]>();
        ArrayList<String[]> AttributeClassParameterList = new ArrayList<String[]>();
        String name = "AttributeClassParameterList";
        String value = "";
        String[] AttributeClassName = new String[2];
        AttributeClassName[0] = name;
        AttributeClassName[1] = value;
        AttributeClassParameterList.add(AttributeClassName);
        String pathout_parameter = directory + "parameter.xml";

        String parameter[][] = new String[1000][2];
        int count = 0;
        name = "";
        value = "";
        try {
            BufferedReader in = new BufferedReader(new FileReader(model));

            String line;
            line = in.readLine();
            while (!line.equals("</model>")) {





                line = in.readLine();
                if (line.contains("<launcher>")) //Informationen bin zum Ende des Launcher
                {
                    parameterList = new ArrayList<String[]>();
                    String[] ParameterAndValue = new String[2];
                    ParameterAndValue[1] = "";
                    ParameterAndValue[0] = "LauncherEinstellungen";
                    parameterList.add(ParameterAndValue);

                }

                if (line.contains("<component")) //Start einer neuen Komponente
                {
                    name = "";
                    value = "";
                    parameterList = new ArrayList<String[]>();
                    String[] ParameterAndValue = new String[2];
                    char[] c = line.toCharArray();
                    int i = 0;
                    while (c.length > i + 4 && !(c[i] == ('n') && c[i + 1] == ('a') && c[i + 2] == ('m') && c[i + 3] == ('e') && c[i + 4] == ('='))) {
                        i++;
                    }
                    int zahl = 0;
                    while (c.length > i + 6 + zahl && c[i + 6 + zahl] != '"') {
                        //Componentenname eleminieren
                        name = name + Character.toString(c[i + 6 + zahl]);
                        zahl++;
                    }
                    System.out.println(name);
                    ParameterAndValue[0] = name;
                    ParameterAndValue[1] = "Komponentenname";
                    parameterList.add(ParameterAndValue);

                }

                if (line.contains("<attribute class") && line.contains("value=")) //Parameter fï¿½r die Menge der Modellparameter
                {
                    String[] ParameterAndValue = new String[2];
                    name = "";
                    value = "";
                 
                    char[] c = line.toCharArray();
                    int i = 0;
                    while (!(c[i] == ('n') && c[i + 1] == ('a') && c[i + 2] == ('m') && c[i + 3] == ('e') && c[i + 4] == ('='))) {
                        i++;
                    }
                    int zahl = 0;
                    while (c[i + 6 + zahl] != '"') {
                        //Parametername eleminieren
                        name = name + Character.toString(c[i + 6 + zahl]);
                        zahl++;
                    }
                  
                    c = line.toCharArray();
                    i = 0;
                    while (!(c[i] == ('v') && c[i + 1] == ('a') && c[i + 2] == ('l') && c[i + 3] == ('u') && c[i + 4] == ('e') && c[i + 5] == ('='))) {
                        i++;
                    }
                    zahl = 0;
                    while (c[i + 7 + zahl] != '"') {
                        //Parameterwert eleminieren 
                        value = value + Character.toString(c[i + 7 + zahl]);
                        zahl++;
                    }
                   
                    parameter[count][0] = name;
                    parameter[count][1] = value;
                    ParameterAndValue[0] = name;
                    ParameterAndValue[1] = value;
                    AttributeClassParameterList.add(ParameterAndValue);
                    count++;

                }
                if (line.contains("value=") && !(line.contains("<attribute class"))) {
                    String[] ParameterAndValue = new String[2];
                    name = "";
                    value = "";
                  
                    char[] c = line.toCharArray();
                    int i = 0;
                    while (c.length > i + 3 && !(c[i] == ('n') && c[i + 1] == ('a') && c[i + 2] == ('m') && c[i + 3] == ('e') && c[i + 4] == ('='))) {
                        i++;
                    }
                    int zahl = 0;
                    while (c.length > i + 6 + zahl && c[i + 6 + zahl] != '"') {
                        //Parametername eleminieren
                        name = name + Character.toString(c[i + 6 + zahl]);
                        zahl++;
                    }
                    
                    c = line.toCharArray();
                    i = 0;

                    while (c.length > i + 4 && !(c[i] == ('v') && c[i + 1] == ('a') && c[i + 2] == ('l') && c[i + 3] == ('u') && c[i + 4] == ('e') && c[i + 5] == ('='))) {
                        i++;
                    }
                    zahl = 0;
                    while (c.length > i + 7 + zahl - 1 && c[i + 7 + zahl] != '"') {
                        //Parameterwert eleminieren 
                        value = value + Character.toString(c[i + 7 + zahl]);
                        zahl++;
                    }
                    
                    parameter[count][0] = name;
                    parameter[count][1] = value;
                    ParameterAndValue[0] = name;
                    ParameterAndValue[1] = value;
                    parameterList.add(ParameterAndValue);
                    count++;
                }
                if (line.contains("</component") || line.contains("</launcher>")) //Ende einer Komponente bzw Ende des Launchers
                {
                    component.add(parameterList);

                }
            }
            component.add(AttributeClassParameterList);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        try {
            BufferedWriter out_parameter = new BufferedWriter(new FileWriter(pathout_parameter));

            //welche komponenten sind im Modell enthalten
            out_parameter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            out_parameter.write("<sect1 xmlns=\"http://docbook.org/ns/docbook\" version=\"5.0\">\n");
            out_parameter.write("<title>" + Bundle.resources.getString("titel_parameter") + "</title>\n");
            out_parameter.write("<para>\n");
            out_parameter.write(" <informaltable>\n");
            out_parameter.write("<tgroup cols=\"3\">\n");
            out_parameter.write("<colspec colname=\"c1\" colnum=\"1\" colwidth=\"1.5*\"/>\n");
            out_parameter.write("<colspec colname=\"c2\" colnum=\"2\" colwidth=\"1.0*\"/>\n");

            out_parameter.write("<thead>\n");
            out_parameter.write("<row>\n");
            out_parameter.write(" <entry namest=\"c1\" nameend=\"c2\">Parameter</entry>\n");
            out_parameter.write("</row>\n");
            out_parameter.write("</thead>\n");
            out_parameter.write("<tbody>\n");

            for (count = 0; count < component.size(); count++) {
                parameterList = new ArrayList<String[]>();

                parameterList = component.get(count);
                for (int count1 = 0; count1 < parameterList.size(); count1++) {
                    String[] ParameterAndValue = new String[2];
                    ParameterAndValue = parameterList.get(count1);
                    if (count1 == 0) {
                        out_parameter.write("<row>\n");
                        out_parameter.write("<entry><emphasis role=\"bold\">" + ParameterAndValue[0] + "</emphasis></entry>\n");
                        out_parameter.write(" <entry namest=\"c2\" nameend=\"newCol3\"/>\n");
                        out_parameter.write("</row>\n");
                    } else {
                        out_parameter.write("<row>\n");
                        out_parameter.write("<entry>" + ParameterAndValue[0] + "</entry>\n");
                        out_parameter.write(" <entry namest=\"c2\" nameend=\"newCol3\">" + ParameterAndValue[1] + "</entry>\n");
                        out_parameter.write("</row>\n");
                    }

                }

            }
            out_parameter.write("</tbody>\n");
            out_parameter.write("</tgroup>\n");
            out_parameter.write("</informaltable>\n");
            out_parameter.write("</para>\n");
            out_parameter.write("</sect1>\n");
            out_parameter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return component;
    }

    public static void Documentation_Complete(String directory, String model, String pathin_vorlage, ArrayList<ArrayList<String[]>> component) {


        //erstellt ein Dokument indem die Struktur (Komponenten und Kontextkomponenten) aufgefuehrt werden.
        //erstellt weiterhin ein Dokument, welches die Komplettdokumentation erzeugt

      
        String Modellname = "";
        String Modellautor = "";
        ArrayList<String> ContextComponent = new ArrayList<String>();
        directory = directory + "/";
        String pathout_txt = directory + "Modellkomponenten.txt";
        String pathout_xml = directory + "Modellkomponenten.xml";
        String pathout_xml_docu_complet = directory + Bundle.resources.getString("Filename");
        String pathout_biblio = directory + "bibliography_" + Bundle.resources.getString("lang") + ".xml";


        ArrayList<String> ComponentList = new ArrayList<String>();
        ArrayList<String> BiblioEntryList = new ArrayList<String>();
        try {

            BufferedWriter bibliography = new BufferedWriter(new FileWriter(pathout_biblio));
            BufferedReader in = new BufferedReader(new FileReader(model));
            BufferedWriter out_txt = new BufferedWriter(new FileWriter(pathout_txt));
            BufferedWriter out_xml = new BufferedWriter(new FileWriter(pathout_xml));
            BufferedWriter out_xml_docu_complet = new BufferedWriter(new FileWriter(pathout_xml_docu_complet));
            String line;

            //ErÃ¶ffnung des Literaturverzeichnisses
            bibliography.write("<bibliography xmlns=\"http://docbook.org/ns/docbook\" version=\"5.0\" xml:lang=\"" + Bundle.resources.getString("lang") + "\">\n");


            //Ende ErÃ¶ffnung des Literaturverzeichnisses

            //StringTokenizer st;
            // int datafound = 0;

            //iteration ï¿½ber line
            line = in.readLine();
            //kopf des docbook Dokuments

            //welche komponenten sind im Modell enthalten
            out_xml.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            out_xml.write("<sect1 xmlns=\"http://docbook.org/ns/docbook\" version=\"5.0\">\n");
            {
                out_xml.write("<title>" + Bundle.resources.getString("titel_modellstruktur") + "</title>\n");
            }




            //komplette Modelldokumentation
            out_xml_docu_complet.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");


            {
                out_xml_docu_complet.write("<article xmlns=\"http://docbook.org/ns/docbook\" version=\"5.0\" " + Bundle.resources.getString("xml_lang") + ">\n");
            }


            while (!line.equals("</model>")) {





                line = in.readLine();
                if (line.contains("author")) {
                    line = line.replace("<model author=\"", "");
                    char[] c = line.toCharArray();
                    int i = 0;
                    while (c[i] != '"') {
                        i++;
                    }
                    Modellautor = line.substring(0, i);
                    while (c.length > i + 4 && !(c[i] == ('n') && c[i + 1] == ('a') && c[i + 2] == ('m') && c[i + 3] == ('e') && c[i + 4] == ('='))) {
                        i++;
                    }
                    int j = 0;
                    while (c[i + 6 + j] != '"') {
                        j++;
                    }
                    Modellname = line.substring(i + 6, i + 6 + j);
                    String titelseite = "<title>" + Bundle.resources.getString("titel_docu") + "</title>\n"
                            + "<subtitle>" + Modellname + "</subtitle>\n"
                            + "<info><author><personname>" + Modellautor + "</personname></author>\n"
                            + "<date>2010-12-21</date>\n"
                            + "<releaseinfo>2010-12-21</releaseinfo>\n"
                            + "<copyright>\n"
                            + "<year>2010</year>\n"
                            + "<holder>" + Bundle.resources.getString("uni") + "</holder>\n"
                            + "</copyright>\n"
                            + "</info>\n";


                    out_xml_docu_complet.write(titelseite);
                    out_xml_docu_complet.write("<xi:include href=\"modeldoc_" + Bundle.resources.getString("lang") + ".xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\"/>\n");
                    out_xml_docu_complet.write("<xi:include href=\"Modellkomponenten.xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\"/>\n");

                }

                if (line.contains("<contextcomponent") || line.contains("<component") || line.contains("</contextcomponent")) {

                    String line1 = line.replace("<c", "c");
                    String line2 = line1.replace("\">", "\"");
                    String line3 = line2.replace("</contextcomponent>", "end contextcomponent");
                    if (line.contains("<contextcomponent")) {
                        out_xml.write("<para><emphasis role=\"bold\">" + Bundle.resources.getString("contextcomponent") + " ");

                    }//Schrift fett
                    if (line.contains("</contextcomponent")) {
                        out_xml.write("<para><emphasis role=\"bold\">");

                    }//Schrift fett
                    if (line.contains("<component")) {
                        out_xml.write("<itemizedlist><listitem><para>");
                        out_xml.write(Bundle.resources.getString("component") + " ");
                    }
                    out_txt.write(line + "\n");

                    char[] c = line3.toCharArray();
                    String line4 = "";
                    int i = 0;
                    if (line.contains("<contextcomponent") || line.contains("<component")) {
                        int azahl = 0;//zï¿½hlt die anfuehrungszeichen
                        int startc = 0;
                        int endc = 0;
                        int startn = 0;
                        int endn = 0;
                        for (i = 0; i < c.length; i++) {

                            if (c[i] == '"' && azahl == 0) {
                                startc = i;
                                azahl++;
                                i++;
                            }
                            if (c[i] == '"' && azahl == 1) {
                                endc = i;
                                azahl++;
                                i++;
                            }
                            if (c[i] == '"' && azahl == 2) {
                                startn = i;
                                azahl++;
                                i++;
                            }
                            if (c[i] == '"' && azahl == 3) {
                                endn = i;
                                azahl++;
                                i++;
                            }

                        }
                        String Klasse = line3.substring(startc, endc);
                        String Name = line3.substring(startn + 1, endn);
                        int start = 0;
                        for (i = endc; i > 0; i--) {
                            if (c[i] == '.') {
                                start = i;
                                i = 0;
                            }
                        }
                        Klasse = line3.substring(start + 1, endc);
                        if (line.contains("<contextcomponent")) {
                            ContextComponent.add(0, Name);
                        }

                        out_xml.write(Bundle.resources.getString("class") + "=<emphasis role=\"italic\">" + Klasse + "</emphasis>   " + Bundle.resources.getString("name") + "=<emphasis role=\"italic\">  " + Name + "</emphasis>");
                        if (line.contains("<component")) {
                            out_xml.write("</para></listitem></itemizedlist>\n");
                        }
                    }
                    if (line3.contains("end contextcomponent")) {
                        out_xml.write("end " + Bundle.resources.getString("contextcomponent") + "  <emphasis role=\"italic\">  " + ContextComponent.get(0) + "</emphasis>");
                        ContextComponent.remove(0);
                        // out_xml.write("<cmdsynopsis><command/><sbr/></cmdsynopsis>\n");
                    }
                    if (line.contains("contextcomponent")) {
                        out_xml.write("</emphasis></para>");
                    }
                   

                    if (line.contains("<contextcomponent") || line.contains("<component")) //wenn Komponente oder Kontextkomponente
                    {
                        //  line1=line3.substring(16);
                        if (line.contains("<contextcomponent")) {
                            line1 = line3.replaceFirst("contextcomponent class=\"", "");
                        } else {
                            line1 = line3.replaceFirst("component class=\"", "");
                        }
                        //String in Zeichen zerlegen um nur den Pfad zu speichern
                        c = line1.toCharArray();
                        line4 = "";
                        i = 0;
                        while (c[i] != ('"')) {


                            //Leerzeichen entfernen!!! 
                            if (c[i] != (' ')) {
                                line4 = line4 + Character.toString(c[i]);
                            }
                            i++;
                            c = line1.toCharArray();

                        }
                        //einfï¿½gen der komponenten in eine verwaltungsdatenstruktur
                        boolean doppelt = false;
                        for (int j = 0; j < ComponentList.size(); j++)//test, ob componente schon in der sturktur enthalten ist, um dopplungen auszuschlieï¿½en
                        {
                            String line5 = ComponentList.get(j);
                            if (line5.equals(line4)) {
                                doppelt = true;
                            }

                        }
                        if (!doppelt) {
                            ComponentList.add(line4);
                            //erstelle verbunddokument
                            Doku_AnnotitionAndManuell(bibliography, BiblioEntryList, pathin_vorlage, directory + "Component_Annotation." + line4 + ".xml", directory + line4 + "_" + Bundle.resources.getString("lang") + ".xml", directory + line4 + "1.xml", directory + line4 + ".xml", component);
                            out_xml_docu_complet.write("<xi:include href=\"" + line4 + ".xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\"/>\n");

                            //out_xml_docu_complet.write("<xi:include href=\""+line4+"_de.xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\"/>\n");
                        }
                        if (ComponentList.size() == 0) {
                            ComponentList.add(line4);
                            out_xml_docu_complet.write("<xi:include href=\"" + line4 + "_" + Bundle.resources.getString("lang") + ".xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\"/>\n");
                        }

                    }
                }
            }

            out_xml.write("</sect1>\n");
            out_xml_docu_complet.write("<xi:include href=\"parameter.xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\"/>\n");
            out_xml_docu_complet.write("<xi:include href=\"bibliography_" + Bundle.resources.getString("lang") + ".xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\"/>\n");
            out_xml_docu_complet.write("</article>");
            out_txt.close();
            out_xml.close();
            out_xml_docu_complet.close();

            bibliography.write("</bibliography>\n");
            bibliography.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


      /*  System.out.print("Anzahl der Komponenten: ");
        System.out.println(ComponentList.size());
        for (int i = 0; i < ComponentList.size(); i++) {
            System.out.println(ComponentList.get(i));
        }*/
    }

    public static void Doku_AnnotitionAndManuell(BufferedWriter bibliography, ArrayList<String> BiblioEntryList, String pathin_vorlage, String pathin_annotation, String pathin_docu, String pathout, String pathout1, ArrayList<ArrayList<String[]>> component) {


        //verbindet die automatische Dokumentation aus dem Quellcode mit der manuell erstellten von den Entwicklern und speichert diese in einem enddokument mit dem Namen des Pfades der Komponente


        String author = "";
        String version = "";
        String date = "";
        String component_title = "";
        String paket = "";
        String classification = "";
        String subtitle = "";
        String variablen[][] = new String[50][7];
        int variablencount = 0;
        try {
            BufferedReader in_annotation = new BufferedReader(new FileReader(pathin_annotation));
            BufferedReader in_docu = new BufferedReader(new FileReader(pathin_docu));

            BufferedReader in_vorlage = new BufferedReader(new FileReader(pathin_docu));
            BufferedWriter out = new BufferedWriter(new FileWriter(pathout));
            BufferedWriter out1 = new BufferedWriter(new FileWriter(pathout1));



            String line_anno;
            line_anno = in_annotation.readLine();
            while (!line_anno.equals("</sect1>")) //Dokument ist zu ende
            {
                line_anno = in_annotation.readLine();
                if (line_anno.contains("<title>")) {
                    //line_anno = in_annotation.readLine();

                    component_title = line_anno.replace("<title>", "");
                    component_title = component_title.replace("</title>", "");
                    char[] c = component_title.toCharArray();
                    int start = 0;
                    for (int i = c.length - 1; i > 0; i--) {
                        if (c[i] == '.') {
                            start = i;
                            i = 0;
                        }
                    }
                    component_title = component_title.substring(start + 1, c.length);
                   

                }
                if (line_anno.contains("Paket")) {
                    line_anno = in_annotation.readLine();
                    paket = line_anno.substring(37); //replace wï¿½re besser
                    paket = paket.replace("</entry>", "");
                    

                }

                if (line_anno.contains("Autor")) {
                    line_anno = in_annotation.readLine();
                    author = line_anno.substring(37); //replace wï¿½re besser
                    author = author.replace("</entry>", "");
                   

                }
                if (line_anno.contains("Version")) {
                    line_anno = in_annotation.readLine();
                    version = line_anno.substring(37);
                    version = version.replace("</entry>", "");
                

                }
                if (line_anno.contains("Modifikationsdatum")) {
                    line_anno = in_annotation.readLine();
                    date = line_anno.substring(37);
                    date = date.replace("</entry>", "");
                   

                }
            }


            //fuer die Variablen
            BufferedReader in_annotation1 = new BufferedReader(new FileReader(pathin_annotation));
            BufferedReader in_docu1 = new BufferedReader(new FileReader(pathin_docu));

            String variable = "";
            String describtion = "";
            String unit = "";
            String range = "";
            String datatype = "";
            String variabletype = "";
            String defaultvalue = "";


            line_anno = in_annotation1.readLine();

            while (!line_anno.equals("</sect1>")) //Dokument ist zu ende
            {


                line_anno = in_annotation1.readLine();
                if (line_anno.contains("Beschreibung")) {
                    while (!line_anno.equals("</sect1>")) //Dokument ist zu ende
                    {
                        line_anno = in_annotation1.readLine();
                        //suchen des ersten Variablenblocks
                        if (line_anno.contains("<row>")) //beginn eines variablenblocks
                        {
                            line_anno = in_annotation1.readLine();
                            variable = line_anno.replace("<entry>", "");
                            variable = variable.replace("</entry>", "");
                          

                            line_anno = in_annotation1.readLine();
                            describtion = line_anno.replace("<entry>", "");
                            describtion = describtion.replace("</entry>", "");
                          

                            line_anno = in_annotation1.readLine();
                            unit = line_anno.replace("<entry>", "");
                            unit = unit.replace("</entry>", "");
                          

                            line_anno = in_annotation1.readLine();
                            range = line_anno.replace("<entry>", "");
                            range = range.replace("</entry>", "");
                       

                            line_anno = in_annotation1.readLine();
                            datatype = line_anno.replace("<entry>", "");
                            datatype = datatype.replace("</entry>", "");
                            

                            line_anno = in_annotation1.readLine();
                            variabletype = line_anno.replace("<entry>", "");
                            variabletype = variabletype.replace("</entry>", "");
                           

                            line_anno = in_annotation1.readLine();
                            defaultvalue = line_anno.replace("<entry>", "");
                            defaultvalue = defaultvalue.replace("</entry>", "");
                        

                            variablen[variablencount][0] = variable;
                            variablen[variablencount][1] = describtion;
                            variablen[variablencount][2] = unit;
                            variablen[variablencount][3] = range;
                            variablen[variablencount][4] = datatype;
                            variablen[variablencount][5] = variabletype;
                            variablen[variablencount][6] = defaultvalue;
                            variablencount++;

                            /* //docu dokument nach dieser variable durchsuchen und werte ersetzen
                            line_docu=in_docu1.readLine();
                            while (!line_docu.equals("</sect1>")) //Dokument ist zu ende
                            {
                            if (line_anno.contains(variable)) //relevanter Variablenblock wurde gefunden
                            {
                            line_docu=in_docu1.readLine();//describtion
                            line_docu=in_docu1.readLine();//unit
                            line_docu=line_docu.replace("<entry/>", "<entry>"+unit+"</entry>");
                            }


                            }
                             */


                        }
                    }

                }
            }

            if (!pathin_docu.equals(pathin_vorlage)) {
                for (int i = 0; i < variablencount; i++) {
                    variablen[i][1] = VariablenDescriptionDeutsch(variablen[i][0], pathin_docu);
                }
            }



            String line_docu;
            int row_count = 0;
            line_docu = in_docu.readLine();

           
            int variablenblock = 0; //testet, ob man bereits im Variablenblock ist
            while (!line_docu.equals("</sect1>")) //Dokument ist zu ende
            {
                if (line_docu.contains("<subtitle>")) {
                    subtitle = line_docu.replace("<subtitle>", "");
                    subtitle = subtitle.replace("</subtitle>", "");
                }
                //{line_docu= "<informaltable>";}
                line_docu = in_docu.readLine();
                if (line_docu.contains(Bundle.resources.getString("classification"))) {
                    line_docu = in_docu.readLine();
                    if (line_docu.isEmpty()) {
                        line_docu = in_docu.readLine();
                    }
                    classification = line_docu.replace("<entry>", "");
                    classification = classification.replace("</entry>", "");
                    if (classification.contains("<entry namest=\"c2\" ")) {
                        classification = " ";
                    }
                }

                //ersetzen eines Variablenblocks

                if (line_docu.contains("<sect2>") && variablenblock == 0) //beginn eines variablenblocks
                {

                    while (!line_docu.contains("</sect2>")) //VariablenBlock ist zu ende
                    {

                        line_docu = in_docu.readLine();
                        if (line_docu.contains("<row>")) //beginn eines variablenblocks
                        {

                            line_docu = in_docu.readLine(); //variable
                           


                            char[] c = line_docu.toCharArray();
                            String line4 = "";
                            int i = 0;
                            while (c[i] != ('/')) {
                                //Leerzeichen entfernen!!!
                                if (c[i] != (' ')) {
                                    line4 = line4 + Character.toString(c[i]);
                                }
                                i++;
                            }
                            variable = line4.replace("<entry>", ""); //extrahieren der relevanten variable
                            variable = variable.replace("<", "");
                            for (i = 0; i < 50; i++) {
                                if (variable.equals(variablen[i][0]))//sucht die relevante variable
                                {
                                    line_docu = in_docu.readLine(); //beschreibung
                                   

                                    line_docu = in_docu.readLine(); //unit
                                    //line_docu="<entry>"+variablen[i][2]+"</entry>"; //code hat vorfahrt
                                    line_docu = line_docu.replace("<entry/>", "<entry>" + variablen[i][2] + "</entry>"); //manuelle docu hat vorfahrt

                                    

                                    line_docu = in_docu.readLine(); //range
                                    //line_docu="<entry>"+variablen[i][3]+"</entry>"; //code hat vorfahrt
                                    line_docu = line_docu.replace("<entry/>", "<entry>" + variablen[i][3] + "</entry>");

                                   

                                    line_docu = in_docu.readLine(); //datatype
                                    //line_docu="<entry>"+variablen[i][4]+"</entry>"; //code hat vorfahrt
                           /* c = variablen[i][4].toCharArray();
                                    int start=0;
                                    for (int j=c.length-1;j>0;j--)
                                    {
                                    if (c[j]=='.') {start=j;j=0;}
                                    }
                                    variablen[i][4]=variablen[i][4].substring(start+1, c.length);
                                     */
                                    line_docu = line_docu.replace("<entry/>", "<entry>" + variablen[i][4] + "</entry>");

                                    

                                    line_docu = in_docu.readLine(); //type
                                    //line_docu="<entry>"+variablen[i][5]+"</entry>"; //code hat vorfahrt
                            /*if (variablen[i][5].equals("READ")) {variablen[i][5]="R";}
                                    if (variablen[i][5].equals("WRITE")) {variablen[i][5]="W";}
                                    if (variablen[i][5].equals("READWRITE")) {variablen[i][5]="RW";}
                                     */

                                    line_docu = line_docu.replace("<entry/>", "<entry>" + variablen[i][5] + "</entry>");

                                    

                                    line_docu = in_docu.readLine(); //defaultvalue
                                    if (variablen[i][6].equals("%NULL%")) {
                                        variablen[i][6] = "n/a";
                                    }
                                    line_docu = "<entry>" + variablen[i][6] + "</entry>"; //code hat vorfahrt
                                    //line_docu=line_docu.replace("<entry/>", "<entry>"+variablen[i][6]+"</entry>");

                                   

                                    i = 50;
                                }

                            }



                        }

                    }//end while
                    if (line_docu.contains("</sect2>")) {

                        variablenblock = 1;
                    }
                }



            }//end while !</sect1>

            out.close();





            //zusammensetzen des end-dokumentes


            out1.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            out1.write("<sect1 version=\"5.0\" xmlns=\"http://docbook.org/ns/docbook\" xmlns:ns6=\"http://www.w3.org/1999/xlink\" xmlns:ns5=\"http://www.w3.org/1999/xhtml\" xmlns:ns4=\"http://www.w3.org/2000/svg\"    xmlns:m=\"http://www.w3.org/1998/Math/MathML\" xmlns:ns=\"http://docbook.org/ns/docbook\">\n");
            out1.write("<title>" + component_title + "</title>\n");
            if (pathin_docu.equals(pathin_vorlage)) {
                out1.write("<subtitle></subtitle>\n");
            } else {
                out1.write("<subtitle>" + subtitle + "</subtitle>\n");
            }
            String Metainformationen = "<informaltable>\n"
                    + "<tgroup cols=\"3\">\n"
                    + "<colspec colname=\"c1\" colnum=\"1\" colwidth=\"1.0*\"/>\n"
                    + "<colspec colname=\"c2\" colnum=\"2\" colwidth=\"2.39*\"/>\n"
                    + "<thead>\n"
                    + "<row>\n"
                    + "<entry namest=\"c1\" nameend=\"c2\">" + Bundle.resources.getString("metadata") + "</entry>\n"
                    + "</row>\n"
                    + "</thead>\n"
                    + "<tbody>\n"
                    + "<row>\n"
                    + "<entry>" + Bundle.resources.getString("classification") + "</entry>\n"
                    + "<entry namest=\"c2\" nameend=\"newCol3\">" + classification + "</entry>\n"
                    + "</row>\n"
                    + "<row>\n"
                    + "<entry>" + Bundle.resources.getString("package") + "</entry>\n"
                    + "<entry namest=\"c2\" nameend=\"newCol3\">" + paket + "</entry>\n"
                    + "</row>\n"
                    + "<row>\n"
                    + "<entry>" + Bundle.resources.getString("author") + "</entry>\n"
                    + "<entry namest=\"c2\" nameend=\"newCol3\">" + author + "</entry>\n"
                    + "</row>\n"
                    + "<row>\n"
                    + "<entry>" + Bundle.resources.getString("version") + "</entry>\n"
                    + "<entry namest=\"c2\" nameend=\"newCol3\">" + version + "</entry>\n"
                    + "</row>\n"
                    + " <row>\n"
                    + "<entry>" + Bundle.resources.getString("modification_date") + "</entry>\n"
                    + "<entry namest=\"c2\" nameend=\"newCol3\">" + date + "</entry>\n"
                    + "</row>\n"
                    + "</tbody>\n"
                    + "</tgroup>\n"
                    + "</informaltable>\n";
            out1.write(Metainformationen);

            out1.write("<sect2>\n");
            out1.write("<title>" + Bundle.resources.getString("variables") + "</title>\n");

            //Eingangsvariablen
            boolean eingangs = false;
            for (int i = 0; i < variablencount; i++) {
                if (variablen[i][5].equals("READ")) {
                    eingangs = true;
                }
            }
            if (eingangs == true) {
                String Eingangsvariablen1 = "<informaltable>\n"
                        + "<tgroup cols=\"4\">\n"
                        + "<colspec colname=\"c1\" colnum=\"1\" colwidth=\"3.73*\"/>\n"
                        + "<colspec colname=\"newCol2\" colnum=\"2\" colwidth=\"6.95*\"/>\n"
                        + "<colspec colname=\"c2\" colnum=\"3\" colwidth=\"1.5*\"/>\n"
                        + "<colspec colname=\"newCol5\" colnum=\"4\" colwidth=\"2.0*\"/>\n"
                        + "<thead>\n"
                        + "<row>\n"
                        + "<entry namest=\"c1\" nameend=\"newCol5\">" + Bundle.resources.getString("variable_input") + "</entry>\n"
                        + "</row>\n"
                        + "<row>\n"
                        + "<entry>" + Bundle.resources.getString("variable") + "</entry>\n"
                        + "<entry>" + Bundle.resources.getString("description") + "</entry>\n"
                        + "<entry>" + Bundle.resources.getString("unit") + "</entry>\n"
                        + "<entry>" + Bundle.resources.getString("data_type") + "</entry>\n"
                        + "</row>\n"
                        + "</thead>\n"
                        + "<tbody>\n";
                out1.write(Eingangsvariablen1);
                for (int i = 0; i < variablencount; i++) {
                    char[] c = variablen[i][4].toCharArray();
                    int start = 0;
                    for (int j = c.length - 1; j > 0; j--) {
                        if (c[j] == '.') {
                            start = j;
                            j = 0;
                        }
                    }
                    if (start != 0) {
                        variablen[i][4] = variablen[i][4].substring(start + 1, c.length);
                    }
                    if (variablen[i][4].contains("JAMS")) {
                        variablen[i][4] = variablen[i][4].substring(4, variablen[i][4].length());
                    }
                    if (variablen[i][4].contains("EntityCollection")) {
                        variablen[i][4] = "Entity Collection";
                    }
                    if (variablen[i][4].equals("Double;")) {
                        variablen[i][4] = "Double Array";
                    }
                    if (variablen[i][5].equals("READ")) {

                        String Variable = "<row>\n"
                                + "<entry>" + variablen[i][0] + "</entry>\n"
                                + "<entry>" + variablen[i][1] + "</entry>\n"
                                + "<entry>" + variablen[i][2] + "</entry>\n"
                                + "<entry>" + variablen[i][4] + "</entry>\n"
                                + "</row>\n";
                        out1.write(Variable);
                    }
                }
                String Eingangsvariablen2 = "</tbody>\n"
                        + "</tgroup>\n"
                        + "</informaltable>\n";
                out1.write(Eingangsvariablen2);
            }

            //Statusvariablen
            boolean status = false;
            for (int i = 0; i < variablencount; i++) {
                if (variablen[i][5].equals("READWRITE")) {
                    status = true;
                }
            }
            if (status == true) {
                String Statusvariablen1 = "<informaltable>\n"
                        + "<tgroup cols=\"4\">\n"
                        + "<colspec colname=\"c1\" colnum=\"1\" colwidth=\"3.73*\"/>\n"
                        + "<colspec colname=\"newCol2\" colnum=\"2\" colwidth=\"6.95*\"/>\n"
                        + "<colspec colname=\"c2\" colnum=\"3\" colwidth=\"1.5*\"/>\n"
                        + "<colspec colname=\"newCol5\" colnum=\"4\" colwidth=\"2.0*\"/>\n"
                        + "<thead>\n"
                        + "<row>\n"
                        + "<entry namest=\"c1\" nameend=\"newCol5\">" + Bundle.resources.getString("variable_status") + "</entry>\n"
                        + "</row>\n"
                        + "<row>\n"
                        + "<entry>" + Bundle.resources.getString("variable") + "</entry>\n"
                        + "<entry>" + Bundle.resources.getString("description") + "</entry>\n"
                        + "<entry>" + Bundle.resources.getString("unit") + "</entry>\n"
                        + "<entry>" + Bundle.resources.getString("data_type") + "</entry>\n"
                        + "</row>\n"
                        + "</thead>\n"
                        + "<tbody>\n";
                out1.write(Statusvariablen1);
                for (int i = 0; i < variablencount; i++) {
                    char[] c = variablen[i][4].toCharArray();
                    int start = 0;
                    for (int j = c.length - 1; j > 0; j--) {
                        if (c[j] == '.') {
                            start = j;
                            j = 0;
                        }
                    }
                    if (start != 0) {
                        variablen[i][4] = variablen[i][4].substring(start + 1, c.length);
                    }
                    if (variablen[i][4].contains("JAMS")) {
                        variablen[i][4] = variablen[i][4].substring(4, variablen[i][4].length());
                    }
                    if (variablen[i][4].contains("EntityCollection")) {
                        variablen[i][4] = "Entity Collection";
                    }
                    if (variablen[i][4].equals("Double;")) {
                        variablen[i][4] = "Double Array";
                    }
                    if (variablen[i][5].equals("READWRITE")) {
                        String Variable = "<row>\n"
                                + "<entry>" + variablen[i][0] + "</entry>\n"
                                + "<entry>" + variablen[i][1] + "</entry>\n"
                                + "<entry>" + variablen[i][2] + "</entry>\n"
                                + "<entry>" + variablen[i][4] + "</entry>\n"
                                + "</row>\n";
                        out1.write(Variable);
                    }
                }
                String Statusvariablen2 = "</tbody>\n"
                        + "</tgroup>\n"
                        + "</informaltable>\n";
                out1.write(Statusvariablen2);
            }

            //Ausgangsvariablen
            boolean ausgangs = false;
            for (int i = 0; i < variablencount; i++) {
                if (variablen[i][5].equals("WRITE")) {
                    ausgangs = true;
                }
            }
            if (ausgangs == true) {
                String Ausgangsvariablen1 = "<informaltable>\n"
                        + "<tgroup cols=\"4\">\n"
                        + "<colspec colname=\"c1\" colnum=\"1\" colwidth=\"3.73*\"/>\n"
                        + "<colspec colname=\"newCol2\" colnum=\"2\" colwidth=\"6.95*\"/>\n"
                        + "<colspec colname=\"c2\" colnum=\"3\" colwidth=\"1.5*\"/>\n"
                        + "<colspec colname=\"newCol5\" colnum=\"4\" colwidth=\"2.0*\"/>\n"
                        + "<thead>\n"
                        + "<row>\n"
                        + "<entry namest=\"c1\" nameend=\"newCol5\">" + Bundle.resources.getString("variable_output") + "</entry>\n"
                        + "</row>\n"
                        + "<row>\n"
                        + "<entry>" + Bundle.resources.getString("variable") + "</entry>\n"
                        + "<entry>" + Bundle.resources.getString("description") + "</entry>\n"
                        + "<entry>" + Bundle.resources.getString("unit") + "</entry>\n"
                        + "<entry>" + Bundle.resources.getString("data_type") + "</entry>\n"
                        + "</row>\n"
                        + "</thead>\n"
                        + "<tbody>\n";
                out1.write(Ausgangsvariablen1);
                for (int i = 0; i < variablencount; i++) {
                    char[] c = variablen[i][4].toCharArray();
                    int start = 0;
                    for (int j = c.length - 1; j > 0; j--) {
                        if (c[j] == '.') {
                            start = j;
                            j = 0;
                        }
                    }
                    if (start != 0) {
                        variablen[i][4] = variablen[i][4].substring(start + 1, c.length);
                    }
                    if (variablen[i][4].contains("JAMS")) {
                        variablen[i][4] = variablen[i][4].substring(4, variablen[i][4].length());
                    }
                    if (variablen[i][4].contains("EntityCollection")) {
                        variablen[i][4] = "Entity Collection";
                    }
                    if (variablen[i][4].equals("Double;")) {
                        variablen[i][4] = "Double Array";
                    }
                    if (variablen[i][5].equals("WRITE")) {
                        if (variablen[i][1].contains("</tbody>")) {
                            variablen[i][1] = " ";
                        }
                        String Variable = "<row>\n"
                                + "<entry>" + variablen[i][0] + "</entry>\n"
                                + "<entry>" + variablen[i][1] + "</entry>\n"
                                + "<entry>" + variablen[i][2] + "</entry>\n"
                                + "<entry>" + variablen[i][4] + "</entry>\n"
                                + "</row>\n";
                        out1.write(Variable);
                    }
                }
                String Ausgangsvariablen2 = "</tbody>\n"
                        + "</tgroup>\n"
                        + "</informaltable>\n";
                out1.write(Ausgangsvariablen2);
            }
            out1.write("<para></para>\n");
            out1.write("</sect2>\n");
            out1.write("<sect2>\n");
            /* //Parameter
            String Parameter1 ="<informaltable>\n"+
            "<tgroup cols=\"3\">\n"+
            "<colspec colname=\"c1\" colnum=\"1\" colwidth=\"4.47*\"/>\n"+
            "<colspec colname=\"newCol2\" colnum=\"2\" colwidth=\"6.23*\"/>\n"+
            "<colspec colname=\"newCol3\" colnum=\"3\" colwidth=\"1*\"/>\n"+
            "<thead>\n"+
            "<row>\n"+
            "<entry namest=\"c1\" nameend=\"newCol3\">Parameter</entry>\n"+
            "</row>\n"+
            "<row>\n"+
            "<entry>Parameter</entry>\n"+
            "<entry>Beschreibung</entry>\n"+
            "<entry>Wert</entry>\n"+
            "</row>\n"+
            "</thead>\n"+
            "<tbody>\n";
            
            ArrayList<String[]> parameterList = new ArrayList<String[]>();
            ArrayList<String[]> AttributeClassParameterList = new ArrayList<String[]>();
            String name="AttributeClassParameterList";
            String value="";
            String[] AttributeClassName=new String[2];

            for (int count=0;count< component.size();count++)
            {
            parameterList = new ArrayList<String[]>();

            parameterList=component.get(count);


            for (int count1=0;count1< parameterList.size();count1++)
            {
            String[] ParameterAndValue=new String[2];
            ParameterAndValue=parameterList.get(count1);
            if (ParameterAndValue[0].equals(component_title))   //richtige Komponente wurde gefunden
            {
            if (count1!=0)
            {
            String Parameter = "<row>\n"+
            "<entry>"+ParameterAndValue[0]+"</entry>\n"+
            "<entry>"+"Beschreibung"+"</entry>\n"+
            "<entry>"+ParameterAndValue[1]+"</entry>\n"+
            "</row>\n";
            out1.write(Parameter);
            }
            }
            }

            }
            String Parameter2 ="</tbody>\n"+
            "</tgroup>\n"+
            "</informaltable>\n";
            
             */

           
            line_docu = in_vorlage.readLine();

            while (!line_docu.contains("<title>Beschreibung der Komponente") && !line_docu.contains("<title>Component Description")) {
                line_docu = in_vorlage.readLine();
            }
            out1.write("<title>" + Bundle.resources.getString("description_component") + "</title>\n");
            line_docu = in_vorlage.readLine();
            while (!line_docu.contains("</sect1>") && !line_docu.contains("<bibliography")) {

                if (line_docu.contains("ns3:")) {
                    line_docu = line_docu.replaceAll("ns3:", "m:");
                }
                out1.write(line_docu + "\n");
               
                line_docu = in_vorlage.readLine();
            }



            if (line_docu.contains("<bibliography")) {
                while (!line_docu.contains("</sect1>")) {

                    line_docu = in_vorlage.readLine();
                    if (line_docu.contains("<biblioentry")) {
                        char[] c = line_docu.toCharArray();
                        int start = 0;
                        int end = 0;
                        for (int i = 0; i < c.length - 1; i++) {
                            if (c[i] == 'i' && c[i + 1] == 'd') {
                                start = i + 4;
                                i = c.length;
                            } //nach id suchen
                        }
                        for (int i = start; i < c.length - 1; i++) {
                            if (c[i] == '"') {
                                end = i;
                                i = c.length;
                            } //nach id suchen
                        }
                        String biblio_id;
                        biblio_id = line_docu.substring(start, end);
                        boolean doppelt = false;
                        for (int j = 0; j < BiblioEntryList.size(); j++)//test, ob Bibliothekseintrag schon in der sturktur enthalten ist, um dopplungen auszuschlieï¿½en
                        {
                            String line5 = BiblioEntryList.get(j);
                            if (line5.equals(biblio_id)) {
                                doppelt = true;
                            }

                        }
                        if (!doppelt) {
                            BiblioEntryList.add(biblio_id);
                            while (!line_docu.contains("</biblioentry>")) {
                                bibliography.write(line_docu + "\n");
                                line_docu = in_vorlage.readLine();
                            }
                            bibliography.write(line_docu + "\n");
                        }
                        if (doppelt) {
                            while (!line_docu.contains("</biblioentry>")) {

                                line_docu = in_vorlage.readLine();
                            }
                        }
                    }
                }

            }

            out1.write("</sect1>");
            out1.close();

            //neues enddokument



        } catch (IOException e) {
            Doku_AnnotitionAndManuell(bibliography, BiblioEntryList, pathin_vorlage, pathin_annotation, pathin_vorlage, pathout, pathout1, component);
        }

    }

    public static String VariablenDescriptionDeutsch(String variable, String pathin_docu) {
        String description = "";



        try {
            BufferedReader in_docu = new BufferedReader(new FileReader(pathin_docu));
            String line = in_docu.readLine();
            boolean gefunden = false;
            while (!line.equals("</sect1>") && !gefunden) //Dokument ist zu ende
            {
                line = in_docu.readLine();
                if (line.contains("<entry>" + variable + "</entry>")) {


                    line = in_docu.readLine();
                    description = description + line;
                    while (!line.contains("</entry>")) {
                        line = in_docu.readLine();
                        description = description + line;
                    }
                    description = description.replace("  ", " ");
                    description = description.replace("<entry>", " ");
                    description = description.replace("</entry>", " ");
                    description = description.replace("  ", " ");
                    description = description.replace("  ", " ");
                    description = description.replace("  ", " ");
                    description = description.replace("  ", " ");
                    description = description.replace("  ", " ");
                    char[] c = description.toCharArray();

                    int i = 0;
                    while (c[i] == (' ')) {
                        i++;
                    }
                    description = description.substring(i, c.length - 1);
                    gefunden = true;
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return description;
    }
}
