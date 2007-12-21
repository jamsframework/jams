package org.unijena.WikiUpload;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.nodes.Node;
import java.util.zip.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public final class WikiUpload extends NodeAction {
    
    private String commmandLinePath = "";
    private String commandString = "";
    private boolean UploadZip = false;
    private boolean UploadDoc = false;
    private boolean DeleteDoc = false;
    private boolean DeleteZip = false;
    
    final JTextField userName = new JTextField();
    final JTextField location = new JTextField();
    final JPasswordField password = new JPasswordField();
    final JTextField jamsDocPath = new JTextField();                
    
    final JCheckBox  checkUploadZip = new JCheckBox("Upload zip - file?",false);                
    final JCheckBox  checkDeleteZip = new JCheckBox("Delete zip - file in JAMSWiki?",false);                
    final JCheckBox  checkUploadDoc = new JCheckBox("Upload documentation?",false);
    final JCheckBox  checkDeleteDoc = new JCheckBox("Delete documentation in JAMSWiki?",false);
        
    JButton okButton = new JButton("OK");            
    JButton cancelButton = new JButton("Cancel");   
        
    final JDialog dialog = new JDialog();
    private boolean cancelFlag = false;
    
    class ProcControlThread extends Thread{
        private String commandString;
        private Component parent = null;
        
        ProcControlThread(String arg,Component parent){
            commandString = arg;
        }
        
        public void run() {
            //now upload that zip file and update corresponding documentation
            try {      
                Process proc = Runtime.getRuntime().exec(commandString);
                
                while (true){
                    try {
                        proc.exitValue();
                    }catch(Exception e){
                        continue;
                    }
                    break;
                }
                int n = proc.getErrorStream().available();
                byte[] msg = new byte[n];
                proc.getErrorStream().read(msg);
                if (n>0){
                    JOptionPane.showMessageDialog(parent,new String(msg));
                }            
            }catch(Exception e){
                JOptionPane.showMessageDialog(parent,"could not start wikiupload program" + e.toString());
                return;
            }    
        }
    }
    
    public boolean enable(Node[] nodes) {
        return true;
    }
    protected void removeNotify(){
        dialog.dispose();
    }
    
     protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
        
        //create dialog        
        dialog.setModal(true);
        GridLayout GLayout_middle = new GridLayout(4,2);
        GridLayout GLayout_middle2 = new GridLayout(4,1);
        
        FlowLayout FLayout_bottom = new FlowLayout();  
        FLayout_bottom.setVgap(5);
        
        GridLayout GLayout_bar = new GridLayout(4,1);
        GLayout_bar.setVgap(0);
                
        final JPanel dlgcontent = new JPanel();
        dlgcontent.setLayout(GLayout_middle);
        
        JPanel dlgcontent2 = new JPanel();
        dlgcontent2.setLayout(FLayout_bottom);

        JPanel dlgcontent3 = new JPanel();
        dlgcontent3.setLayout(GLayout_middle2);
        
        JLabel Title = new JLabel("WikiDoku - Plugin Data");
        Title.setHorizontalAlignment(Title.CENTER);
        
        dialog.setLayout(GLayout_bar);      
        dialog.add(Title);
        
        JLabel userText = new JLabel("Login");     
        JLabel locationText = new JLabel("URL of JAMS - Wiki");      
        
        JLabel jamsDocText = new JLabel("JAMSWikiDoc - Directory:");
        JLabel passwordText = new JLabel("Password:");                    
        
        dlgcontent3.add(checkUploadZip);
        dlgcontent3.add(checkDeleteZip);
        dlgcontent3.add(checkUploadDoc);
        dlgcontent3.add(checkDeleteDoc);        
        
        dlgcontent2.add(okButton);
        dlgcontent2.add(cancelButton);
        
        dlgcontent.add(locationText);
        dlgcontent.add(location);
        dlgcontent.add(userText);
        dlgcontent.add(userName);        
        dlgcontent.add(passwordText);
        dlgcontent.add(password);
        dlgcontent.add(jamsDocText);
        dlgcontent.add(jamsDocPath);
        
        dialog.add(dlgcontent);
        dialog.add(dlgcontent3);
        dialog.add(dlgcontent2);
    }
    
     private String Encrypt(String password){
        //daten speichern
        String cipherPassword = "";
        try{                    
            Key secKey = new SecretKeySpec( "sesamöff".getBytes(), "DES" );
            Cipher cipher2 = Cipher.getInstance( "DES" );
            cipher2.init(Cipher.ENCRYPT_MODE, secKey);                    
            byte[] cipherText = cipher2.doFinal(password.getBytes());
            //save in format number_number_..._
            for (int i=0;i<cipherText.length;i++){
                cipherPassword += cipherText[i] + "_";
                }
            }catch(Exception e){
                JOptionPane.showMessageDialog(null,"there occured an error while ciphering the password:" + e.toString());
                return "";
            }    
        return cipherPassword;   
        }
                    
    private String Decrypt(String WikiPassword){
        try{
            //our key
            Key secKey = new SecretKeySpec( "sesamöff".getBytes(), "DES" );
            Cipher cipher = Cipher.getInstance( "DES" );
            cipher.init(Cipher.DECRYPT_MODE, secKey);
            //crypted password is saved as (number_of_char)_ ...
            //put it into a vector, because we don´t know the length of the password
            Vector<Byte> password = new Vector();
            int firstPos = 0,nextPos = 0;
            while( (nextPos = WikiPassword.indexOf("_",firstPos)) != -1){
                password.add(new Byte(new Integer(WikiPassword.substring(firstPos,nextPos)).byteValue()));
                firstPos = nextPos+1;
            }
            //convert vector into a fixed sized byte array
            byte[] tmp = new byte[password.size()];
            for (int j=0;j<tmp.length;j++){
                tmp[j] = password.get(j).byteValue();
            }
            //now decipher
            byte[] clearText = cipher.doFinal(tmp);
            return new String(clearText);
        }catch(Exception e){
             JOptionPane.showMessageDialog(null,"there occured an error while deciphering the password:" + e.toString());
        }
        return "";
    }
     
    private void popupDialog(final Properties properties,String CompleteJarPath,String ZipPath) {                                
        cancelFlag = false;
        //get settings from project file!
        String WikiUserName = properties.getProperty("WikiUser");
        String WikiLocation = properties.getProperty("WikiLocation");
        String WikiPassword = properties.getProperty("WikiPassword");
        String WikiDocPath  = properties.getProperty("JAMSDocPath");
        String _UploadZip    = properties.getProperty("UploadZip");
        String _UploadDoc    = properties.getProperty("UploadDoc");
        String _DeleteZip    = properties.getProperty("DeleteZip");
        String _DeleteDoc    = properties.getProperty("DeleteDoc");        
        
        if (_UploadZip != null) {
            if (_UploadZip.compareTo("true") == 0)  UploadZip = true;            
            else                                    UploadZip = false;
        }
        if (_UploadDoc != null) {
            if (_UploadDoc.compareTo("true") == 0)  UploadDoc = true;
            else                                    UploadDoc = false;
        }
        if (_DeleteZip != null) {
            if (_DeleteZip.compareTo("true") == 0)  DeleteZip = true;
            else                                    DeleteZip = false;
        }
        if (_DeleteDoc != null) {
            if (_DeleteDoc.compareTo("true") == 0)  DeleteDoc = true;
            else                                    DeleteDoc = false;
        }
                 
        userName.setText(WikiUserName);     
        
        if (WikiLocation == null)
            location.setText("http://jams.uni-jena.de/jamswiki/");
        else
            location.setText(WikiLocation);
        
        //decrypt password
        WikiPassword = Decrypt(WikiPassword);
        
        password.setText(WikiPassword);                
        jamsDocPath.setText(WikiDocPath);
               
        checkUploadZip.setSelected(UploadZip);
        checkDeleteZip.setSelected(DeleteZip);
        checkUploadDoc.setSelected(UploadDoc);
        checkDeleteDoc.setSelected(DeleteDoc);
                                         
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {                
                dialog.setVisible(false);
            }
        });
        
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String cipherPassword = Encrypt(new String(password.getPassword()));
                
                properties.setProperty("WikiUser",userName.getText());
                properties.setProperty("WikiLocation",location.getText());                               
                properties.setProperty("WikiPassword",cipherPassword);                
                properties.setProperty("JAMSDocPath",jamsDocPath.getText());
                UploadZip = checkUploadZip.isSelected();
                UploadDoc = checkUploadDoc.isSelected();
                DeleteZip = checkDeleteZip.isSelected();
                DeleteDoc = checkDeleteDoc.isSelected();
                
                if (UploadZip){ properties.setProperty("UploadZip","true");
                }else{          properties.setProperty("UploadZip","false");}
                if (UploadDoc){ properties.setProperty("UploadDoc","true");}
                else{           properties.setProperty("UploadDoc","false");   }
                if (DeleteDoc){ properties.setProperty("DeleteDoc","true"); }
                else{           properties.setProperty("DeleteDoc","false");}
                if (DeleteZip){ properties.setProperty("DeleteZip","true"); }
                else{           properties.setProperty("DeleteZip","false");}
                                                
                dialog.setVisible(false);
            }
        });
                                                                
        dialog.setSize(400,350);
        dialog.setVisible(true);
        
        //String args[] = {"location",WikiLocation,"user",WikiUserName,"password",WikiPassword,"update",CompleteJarPath,"upload",ZipPath};          
        Vector<String> args = new Vector();
        args.add("location");
        args.add(location.getText() + "/");
        args.add("user");
        args.add(userName.getText());
        args.add("password");
        args.add(new String(password.getPassword()));        
        if (UploadZip){
            args.add("upload");
            args.add(ZipPath);
        }
        if (DeleteZip){
            args.add("unload");
            //extract filename
            int i=ZipPath.length()-1;
            while(i>=0 && ZipPath.charAt(i)!='\\' && ZipPath.charAt(i)!='/'){
                i--;
            }            
            args.add(ZipPath.substring(i+1,ZipPath.length()));
        }
        if (UploadDoc){
            args.add("update");
            args.add(CompleteJarPath);
        }
        else if (DeleteDoc){
            args.add("del");
            args.add(CompleteJarPath);
        }
        commandString = "java" + " -Xdebug " + " -classpath " + jamsDocPath.getText() + "lib\\ -jar " + jamsDocPath.getText() + "\\" + "JAMSWikiDoc.jar ";
        for (int j=0;j<args.size();j++){
            commandString += " " + args.get(j);
        }        
    }

    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length > 1) {
            JOptionPane.showMessageDialog(this.dialog,"There are more than one activated nodes, please select only one node");            
            return;
        }
        //there is only one activated node, so select this one
        org.netbeans.api.project.Project proj = (Project)activatedNodes[0].getLookup().lookup(org.netbeans.api.project.Project.class);                        
        if (proj == null) {
            JOptionPane.showMessageDialog(this.dialog,"No selected project, please select a project node");               
            return;
        }
        //System.out.println(proj.toString());
        
        //every project has a property file, find and open this
        FileObject dir = proj.getProjectDirectory();
        String propertiesFile = dir.getPath() + "/nbproject/" + "project.properties";
        //load properties                
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesFile));
        }catch(Exception e) {
            JOptionPane.showMessageDialog(this.dialog,"Could not read properties file, because:" + e.toString());
            return;
        }
                
        //find libary links in property file
        Enumeration<String> en = (Enumeration<String>)properties.propertyNames();            
        Vector<String> fileCollection = new Vector<String>();
        String classPath = null;
        if ( (classPath = properties.getProperty("javac.classpath")) == null){
            JOptionPane.showMessageDialog(this.dialog,"no classpath variable was found");
            return;
        }
                        
        while(en.hasMoreElements()) {
            String property = en.nextElement();
            //add libraries
            if (property.contains("file.reference") && property.contains(".jar") && classPath.contains(property)) {
                fileCollection.add(dir.getPath() + "/" + properties.getProperty(property));
            }                
            //System.out.println(property);
        }
        //search for output jar
        String distDir = properties.getProperty("dist.dir");
        String distJar = properties.getProperty("dist.jar");
        if (distDir == null || distJar == null){
            JOptionPane.showMessageDialog(this.dialog,"no jar-file could be found in this project");
            return;
        }
        distJar = distJar.replace("${dist.dir}",distDir);
        
        String CompleteJarPath = dir.getPath() + "\\" + distJar;
        fileCollection.add(CompleteJarPath);
        
        //build zip file        
        
        //name of jar
        int i = CompleteJarPath.length()-1;
        while (i>=0 && CompleteJarPath.charAt(i)!='/' && CompleteJarPath.charAt(i)!='\\'){            
            i--;
        }
        String jarName = CompleteJarPath.substring(i+1,CompleteJarPath.length());
        String zipName = jarName.replace(".jar",".zip");
        ZipOutputStream zipOut = null;
        String ZipPath = dir.getPath() + "/" + zipName;
        
        //show op dialog
        popupDialog(properties,CompleteJarPath,ZipPath);
        //save properties
        try{
            properties.store(new FileOutputStream(propertiesFile),null);
        }catch(Exception e){
            JOptionPane.showMessageDialog(this.dialog,"could not write properties file, because:" + e.toString());
        }
                
        try {
            zipOut = new ZipOutputStream( new FileOutputStream(ZipPath));
            
        }catch(Exception e){
            JOptionPane.showMessageDialog(this.dialog,"could not open zip-File, because:" + e.toString() );            
        }
        //put filenames into an array
        File [] fileArray = new File[fileCollection.size()];
        for (int j=0;j<fileCollection.size();j++){
            fileArray[j] = new File(fileCollection.get(j));
        }
        //write to zip file
        byte[] buffer = new byte[18024];
        try{
            for(int j = 0; j < fileArray.length; j++){
                //do not save directories ... 
                String fileName = fileArray[j].getAbsolutePath();
                FileInputStream inFile = new FileInputStream(fileName);                    
                //extract filename:
                int l = fileName.length()-1;
                while(l >= 0 && fileName.charAt(l)!='\\' && fileName.charAt(l)!='/'){
                    l--;
                }
                if (fileName.compareTo(CompleteJarPath) == 0){
                    zipOut.putNextEntry(new ZipEntry(fileName.substring(l+1,fileName.length())));
                }
                else{
                    //store in libs directory
                    String name = "lib/" + fileName.substring(l+1,fileName.length());
                    zipOut.putNextEntry(new ZipEntry(name));
                }
                int len;
                while ((len = inFile.read(buffer)) > 0){
                    zipOut.write(buffer, 0, len);
                }
                inFile.close();
            }
            zipOut.close();             
        }catch(Exception e){
            JOptionPane.showMessageDialog(this.dialog,"an error occured, while zipping file:" + e.toString() );                      
            return;
        }
        
        //get access to wiki database
        String WikiLocation = properties.getProperty("WikiLocation");
        String WikiPassword = properties.getProperty("WikiPassword");
        String WikiUser = properties.getProperty("WikiUser");
        String WikiDocPath = properties.getProperty("JAMSDocPath");
        
        if (WikiLocation == null || WikiPassword == null || WikiUser == null || WikiDocPath == null){
            JOptionPane.showMessageDialog(this.dialog,"some data is missing, have you filled out all forms?");
            return;
        }
        WikiPassword = Decrypt(WikiPassword);
                                                                                
        ProcControlThread execWikiDoc = new ProcControlThread(commandString,this.getToolbarPresenter());

        execWikiDoc.start();
    }
    
    public String getName() {
        return NbBundle.getMessage(WikiUpload.class, "CTL_WikiUpload");
    }
           
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
}
