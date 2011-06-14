/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jamsui.juice.documentation;

import jams.JAMSProperties;
import jams.gui.WorkerDlg;
import jams.gui.tools.GUIHelper;
import jams.tools.XMLTools;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;
import org.w3c.dom.Document;

/**
 *
 * @author chris
 */
public class DocumentationWizard {
    JAMSProperties properties;

    public File libaries[];
    public File developerDocumentation;
    public File workspace;
    public Document modelDoc;

    public String docBookPath = "C:/Program Files (x86)/Oxygen XML Editor 11";

    public void runDocuProcess(){
        String xmlFileName= developerDocumentation + "/xmlSavedForDocu.xml";
        String documentationXML = workspace + "/data/Dokumentation_komplett.xml";


        try{
        XMLTools.writeXmlFile(modelDoc,xmlFileName);
        }catch(IOException e){
            System.out.println(e);
        }

        System.out.println("working in workspace:" + workspace);

        Doku.createDocumentation(new File(workspace + "/data"), new File(xmlFileName), new File(workspace+"/data/jar/model.jar"), new File[0]);

        ProcessBuilder pb = new ProcessBuilder(docBookPath + "/xsltproc.exe","--xinclude","--output" ,developerDocumentation + "/tmp.fo",
                docBookPath + "/frameworks/docbook/xsl/fo/docbook.xsl",documentationXML);
        pb.redirectErrorStream(true);
        for (String s : pb.command()){
            System.out.println("arg:" + s + "\n");
        }
        Process process = null;
        try {
            process = pb.start();

            try {
                process.exitValue();
            } catch (Exception e) {
                System.out.println("waiting for xsltproc");
                try {
                    Thread.sleep(300);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
       InputStream is = process.getInputStream();
       InputStreamReader isr = new InputStreamReader(is);
       BufferedReader br = new BufferedReader(isr);
       String line;

       System.out.printf("Output of running:");
       try{
       while ((line = br.readLine()) != null) {
         System.out.println(line);
       }}catch(IOException ioe){
           ioe.printStackTrace();
       }


        ProcessBuilder pb2 = new ProcessBuilder("java","-jar",docBookPath + "/lib/fop.jar","-fo",
                developerDocumentation + "/tmp.fo","-pdf",developerDocumentation + "/doc.pdf");

        pb2.redirectErrorStream(true);
        for (String s : pb.command()){
            System.out.println("arg:" + s + "\n");
        }

        Process process2 = null;
        try {
            process2 = pb2.start();

            try {
                process2.exitValue();
            } catch (Exception e) {
                System.out.println("waiting for fop");
                try {
                    Thread.sleep(300);
                } catch (Exception e2) {
                }
            }
        } catch (IOException ioe2) {            
        }

       is = process2.getInputStream();
       isr = new InputStreamReader(is);
       br = new BufferedReader(isr);
       
       System.out.printf("Output of running:");
       try{
       while ((line = br.readLine()) != null) {
         System.out.println(line);
       }}catch(IOException ioe){
           ioe.printStackTrace();
       }

        /*
        Doku.Parameter_XML(developerDocumentation.getAbsolutePath(),xmlFileName);
        Doku.Documentation_Complete(developerDocumentation.getAbsolutePath(),xmlFileName);

        JOptionPane.showMessageDialog(null, JAMS.resources.getString("your_documentation_was_created_successfully"));*/
        /*System.out.println(developerDocumentation.getAbsolutePath());

        for (File f : libaries){
            System.out.println(f.getAbsolutePath());
        }

        System.out.println(XMLTools.getStringFromDocument(modelDoc));*/
        
    }

    public void createDocumentation(Frame parent, Document doc, JAMSProperties props, File savePath){
        this.properties = props;
        this.workspace = savePath.getParentFile();
        ArrayList<String> libArray = new ArrayList<String>();
        String libs = properties.getProperty("libs");
        StringTokenizer tok = new StringTokenizer(libs,";");
        while(tok.hasMoreTokens()){
            String path = tok.nextToken();
            File f = new File(path);
            if (!f.exists())
                continue;
            else if (f.isDirectory()){
                String fileList[] = f.list();
                for (String subfile : fileList){
                    if (subfile.endsWith("jar")){
                        libArray.add(subfile);
                    }
                }
            }else{
                libArray.add(path);
            }
        }
        libaries = new File[libArray.size()];
        int i=0;
        for (String s : libArray){
            libaries[i++] = new File(s);
        }

        this.modelDoc = doc;

        JFileChooser chooser = GUIHelper.getJFileChooser(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY );

        chooser.setDialogTitle("Select developer documentation directory");
        int returnValue = chooser.showOpenDialog(parent);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            this.developerDocumentation = file;

            WorkerDlg progress = new WorkerDlg(parent, "Generating Documentation");
            progress.setInderminate(true);
            progress.setTask(new Runnable() {

                public void run() {
                    runDocuProcess();
                }
            });
            progress.execute();
        }
    }
}
