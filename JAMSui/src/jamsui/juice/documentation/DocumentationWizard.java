/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jamsui.juice.documentation;

import jams.JAMSProperties;
import jams.gui.WorkerDlg;
import jams.tools.XMLTools;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Permission;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.w3c.dom.Document;
import javax.swing.JOptionPane;

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
    public String docBookPath = null;

    private static class ExitTrappedException extends SecurityException {
    }

    private static void forbidSystemExitCall() {
        final SecurityManager securityManager = new SecurityManager() {

            public void checkPermission(Permission permission) {
                if (permission.getName().contains("exit")) {
                    throw new ExitTrappedException();
                }else if (permission instanceof FilePermission){
                    FilePermission fp = (FilePermission)permission;
                    if (fp.getActions().equals("delete") && fp.getName().contains(Bundle.resources.getString("Filename"))){
                        throw new ExitTrappedException();
                    }
                }
            }
        };
        System.setSecurityManager(securityManager);
    }

    private static void enableSystemExitCall() {
        System.setSecurityManager(null);
    }

    public void runDocuProcess() {


        developerDocumentation = new File(workspace.getAbsolutePath() + "/documentation/");
        docBookPath = properties.getProperty("docbook-home");
        if (docBookPath == null) {
            JOptionPane.showMessageDialog(null, "Doc-Book Framework not provided!");
            return;
        } else {
            System.out.println("Doc-Book Path is:" + docBookPath);
        }


        String xmlFileName = workspace + "/documentation/xmlSavedForDocu.xml";
        String documentationXML = workspace + "/documentation/" + Bundle.resources.getString("Filename") + ".xml";


        try {
            XMLTools.writeXmlFile(modelDoc, xmlFileName);
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }

        System.out.println("working in workspace:" + workspace);

        Doku.createDocumentation(new File(workspace + "/documentation"), new File(xmlFileName), new File(workspace + "/documentation/jar/model.jar"), new File[0]);

        String cmd = docBookPath + "/xsltproc.exe" + "--xinclude" + "--output" + developerDocumentation + "/tmp.fo" + docBookPath + "/fo/docbook.xsl" + documentationXML;
        System.out.println(cmd);
        ProcessBuilder pb = new ProcessBuilder(docBookPath + "/xsltproc.exe", "--xinclude", "--output", developerDocumentation + "/tmp.fo",
                docBookPath + "/docbook/fo/docbook.xsl", documentationXML);

        pb.redirectErrorStream(true);
        for (String s : pb.command()) {
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
        try {
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        System.setProperty("fop.optional.lib", this.properties.getProperty("libs"));
        try {
            forbidSystemExitCall();
            System.out.println(System.getProperty("java.class.path"));
            org.apache.fop.cli.Main.main(new String[]{System.getProperty("java.class.path"), "-fo", developerDocumentation + "/tmp.fo", "-pdf", developerDocumentation + Bundle.resources.getString("Filename") + "pdf"});
        } catch (ExitTrappedException t) {
            JOptionPane.showMessageDialog(null, Bundle.resources.getString("Your_documentation_was_created_successfully."));
            //normal
        } catch(Throwable t){
            t.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to produce documentation");
        }finally {
            enableSystemExitCall();
        }
    }

    public void createDocumentation(Frame parent, Document doc, JAMSProperties props, File savePath) {
        this.properties = props;
        this.workspace = savePath.getParentFile();
        ArrayList<String> libArray = new ArrayList<String>();
        String libs = properties.getProperty("libs");
        StringTokenizer tok = new StringTokenizer(libs, ";");
        while (tok.hasMoreTokens()) {
            String path = tok.nextToken();
            File f = new File(path);
            if (!f.exists()) {
                continue;
            } else if (f.isDirectory()) {
                String fileList[] = f.list();
                for (String subfile : fileList) {
                    if (subfile.endsWith("jar")) {
                        libArray.add(subfile);
                    }
                }
            } else {
                libArray.add(path);
            }
        }
        libaries = new File[libArray.size()];
        int i = 0;
        for (String s : libArray) {
            libaries[i++] = new File(s);
        }

        this.modelDoc = doc;
        /*
        JFileChooser chooser = GUIHelper.getJFileChooser(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY );

        chooser.setDialogTitle("Select developer documentation directory");
        int returnValue = chooser.showOpenDialog(parent);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        this.developerDocumentation = file;
        }
         */ WorkerDlg progress = new WorkerDlg(parent, Bundle.resources.getString("Generating_Documentation"));
        progress.setInderminate(true);
        progress.setTask(new Runnable() {

            public void run() {
                runDocuProcess();
            }
        });
        progress.execute();
    }
    //}
}
