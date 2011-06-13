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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    public Document modelDoc;
    
    public void runDocuProcess(){
        String xmlFileName= developerDocumentation + "/xmlSavedForDocu.xml";
        try{
        XMLTools.writeXmlFile(modelDoc,xmlFileName);
        }catch(IOException e){
            System.out.println(e);
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

    public void createDocumentation(Frame parent, Document doc, JAMSProperties props){        
        this.properties = props;

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
