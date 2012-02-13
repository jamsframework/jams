/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jamsui.juice.documentation;

import jams.JAMSProperties;
import jams.SystemProperties;
import jams.gui.ObserverWorkerDlg;
import jams.tools.XMLTools;
import jamsui.juice.documentation.DocumentationException.DocumentationExceptionCause;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Permission;
import java.util.Observable;
import org.w3c.dom.Document;
import javax.swing.JOptionPane;

/**
 *
 * @author chris
 */
public class DocumentationWizard extends Observable{
    private static class ExitTrappedException extends SecurityException {
    }

    private static void forbidSystemExitCall() {
        final SecurityManager securityManager = new SecurityManager() {

            @Override
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

    boolean debug = true;

    final String xmlCacheFileName = "xmlSavedForDocu.xml";
    final String DOCUMENTATION_DIRECTORY = "/documentation/";
        
    private void log(String msg){
        if (debug)
            System.out.println(msg);
    }

    private void stateMessage(String msg){
        this.setChanged();
        this.notifyObservers(msg);
    }
    
    private void runXSLTProcessor(String docBookHome, String documentationHome, String outputXML) throws DocumentationException{
        stateMessage("running xsltproc");

        if ( !(new File(docBookHome + "/docbook/fo/docbook.xsl")).exists() )
            throw new DocumentationException(DocumentationExceptionCause.docBookXSLNotExisting);

//            log(docBookHome + "/xsltproc.exe" + "--xinclude" + "--output" + documentationHome + "/tmp.fo" + docBookHome + "/docbook/fo/docbook.xsl" + outputXML);
        ProcessBuilder pb = new ProcessBuilder(docBookHome + "/xsltproc.exe", "--xinclude", "--output", documentationHome + "/tmp.fo",
                docBookHome + "/docbook/fo/docbook.xsl", outputXML);

        pb.redirectErrorStream(true);
        for (String s : pb.command()) {
            log("argument of xsltproc:" + s + "\n");
        }

        Process process = null;
        try {
            process = pb.start();

            try {
                process.exitValue();
            } catch (Exception e) {
                stateMessage("waiting on xsltproc");
                try {
                    Thread.sleep(300);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    throw new DocumentationException(DocumentationExceptionCause.unknownError, e2.toString());                    
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
       
        InputStreamReader isr = new InputStreamReader(process.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        String line;

        log("xslt-proc messages:");
        try {
            while ((line = br.readLine()) != null) {
                log(line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void runApacheFOP(String inputFile, String outputFile, String optionalLibaries) throws DocumentationException{
        stateMessage("running Apache FOP");
        
        System.setProperty("fop.optional.lib", optionalLibaries);
        try {
            forbidSystemExitCall();
            log(System.getProperty("java.class.path"));
            org.apache.fop.cli.Main.main(new String[]{System.getProperty("java.class.path"), "-fo", inputFile, "-pdf", outputFile});
        } catch (ExitTrappedException t) {
            JOptionPane.showMessageDialog(null, Bundle.resources.getString("Your_documentation_was_created_successfully."));            
            return; //this means succsess
        } catch (Throwable t) {
            t.printStackTrace();
            throw new DocumentationException(DocumentationExceptionCause.ApacheFOPFailed, t.toString());
        } finally {
            enableSystemExitCall();
        }
    }

    DocumentationException innerException = null;
    private void openPDF(final File f) throws DocumentationException {
        stateMessage("showing pdf");
        innerException = null;
        Thread thread = new Thread(new Runnable()  {

            @Override
            public void run() {
                try {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + f.getAbsolutePath());
                } catch (IOException ex) {
                    innerException = new DocumentationException(DocumentationExceptionCause.ViewPDFError, ex.toString());
                    return;
                }
            }
        });
        
        thread.start();

        try {
            thread.isAlive();

        } catch (Exception e) {
            stateMessage("opening pdf");
            if (innerException != null){
                throw innerException;
            }
            try {
                Thread.sleep(300);
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new DocumentationException(DocumentationExceptionCause.unknownError, e2.toString());
            }
        }
    }

    public void runDocumentationProcess(File workspace, Document modelDocument, String docBookHome) throws DocumentationException {
        stateMessage("initializing");

        if (workspace == null)
            throw new DocumentationException(DocumentationExceptionCause.workspaceNull);
        
        String xmlInputFile = workspace + "/" + DOCUMENTATION_DIRECTORY + "/" + xmlCacheFileName;
        String documentationXMLFileName = DOCUMENTATION_DIRECTORY + Bundle.resources.getString("Filename") + ".xml";
        String documentationOutputXML = workspace + documentationXMLFileName;
        File   documentationHome = new File(workspace + DOCUMENTATION_DIRECTORY);
        
        log("docbook-home:" + docBookHome);

        if (docBookHome == null) {
            throw new DocumentationException(DocumentationExceptionCause.docBookPathNull);
        }

        if (!(new File(docBookHome + "/xsltproc.exe")).exists()){
            throw new DocumentationException(DocumentationExceptionCause.xsltProcNotExisting);
        }

        if (documentationHome == null || !documentationHome.exists()){
            throw new DocumentationException(DocumentationExceptionCause.documentationPathNull);
        }

        if (modelDocument == null)
            throw new DocumentationException(DocumentationExceptionCause.docBookPathNull);

        stateMessage("caching model document");

        try {
            XMLTools.writeXmlFile(modelDocument, xmlInputFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DocumentationException(DocumentationExceptionCause.xmlIOError, e.toString());
        }

        log("working in workspace:" + workspace);

        stateMessage("creating documentation");

        DocumentationGenerator generator = new DocumentationGenerator();
        generator.createDocumentation(new File(workspace + "/documentation"), new File(xmlInputFile), modelDocument);

        runXSLTProcessor(docBookHome, documentationHome.getAbsolutePath(), documentationOutputXML);

        runApacheFOP(documentationHome + "/tmp.fo", documentationHome + "/" + Bundle.resources.getString("Filename") + ".pdf", this.properties.getProperty("libs"));

        openPDF(new File(documentationHome, Bundle.resources.getString("Filename") + ".pdf"));

        stateMessage("finished");
    }
    
    private JAMSProperties properties = null;;
    private File workspace = null;
    private Document modelDocument = null;

    public void createDocumentation(Frame parent, Document doc, JAMSProperties props, File savePath) {
        properties = props;
        // ok hier gibt es mehrere möglichkeiten
        workspace = savePath.getParentFile();
        modelDocument = doc;

        ObserverWorkerDlg progress = new ObserverWorkerDlg(parent, Bundle.resources.getString("Generating_Documentation"));
        this.addObserver(progress);
        
        progress.setInderminate(true);
        progress.setTask(new Runnable() {

            @Override
            public void run() {
                try{
                    runDocumentationProcess(workspace, modelDocument, properties.getProperty(SystemProperties.DOCBOOK_HOME_PATH));
                }catch(Exception e){
                    System.out.println(e);
                }
            }
        });
        progress.execute();
    }    
}
