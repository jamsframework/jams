package org.unijena.WikiUpload;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.util.Properties;
import java.util.Vector;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.NodeAction;

public final class WikiProperties extends NodeAction {
        
    protected void performAction(final Node[] activatedNodes) {
        Project c = (Project) activatedNodes[0].getCookie(Project.class);
        
        if (activatedNodes.length > 1) {
            System.out.println("There is a problem, because there are more than one activated nodes");
            return;
        }
        
        org.netbeans.api.project.Project proj = (Project)activatedNodes[0].getLookup().lookup(org.netbeans.api.project.Project.class);                        
        System.out.println(proj.toString());
        
        FileObject dir = proj.getProjectDirectory();
        final String propertiesFile = dir.getPath() + "/nbproject/" + "project.properties";
        System.out.println("propertiesFile");
                        
        final Properties properties = new Properties();                
        FileInputStream FIS = null;
        try {                    
            FIS = new FileInputStream(propertiesFile);                
            properties.load(FIS);
        }catch(Exception e1) {
            System.out.println("Could not read properties file, because:" + e1.toString());
        }
        String WikiUserName = properties.getProperty("WikiUser");
        String WikiLocation = properties.getProperty("WikiLocation");
        String WikiPassword = properties.getProperty("WikiPassword");
        String WikiDocPath  = properties.getProperty("JAMSDocPath");

        byte[] key = "sesamöff".getBytes();
        Key secKey = new SecretKeySpec( key, "DES" );
        Cipher cipher = null; //
        
        byte[] cleartext = null;
        if (WikiPassword != null) {
            try {
                cipher = Cipher.getInstance( "DES" );
                cipher.init(Cipher.DECRYPT_MODE, secKey);
                Vector<Byte> password = new Vector();
                int firstPos = 0;
                int nextPos = 0;
                while( (nextPos = WikiPassword.indexOf("_",firstPos)) != -1){
                    password.add(new Byte(new Integer(WikiPassword.substring(firstPos,nextPos)).byteValue()));
                    firstPos = nextPos+1;
                }
                byte[] tmp = new byte[password.size()];
                for (int i=0;i<tmp.length;i++){
                    tmp[i] = password.get(i).byteValue();
                }
                cleartext = cipher.doFinal(tmp);
                WikiPassword = new String(cleartext);
            }catch(Exception e) {
                System.out.println(e.toString());
            }
        }
        try {
            FIS.close();
        }catch(Exception e2){
            System.out.println("Could not close input file, because:" + e2.toString());
            e2.printStackTrace();
        }
                               
        //Dialog erstellen ...
        final JDialog dialog = new JDialog();
        dialog.setModal(true);
        GridLayout GLayout_middle = new GridLayout(4,2);
        FlowLayout FLayout_bottom = new FlowLayout();  
        FLayout_bottom.setVgap(5);
        
        GridLayout GLayout_bar = new GridLayout(3,1);
        GLayout_bar.setVgap(0);
                
        final JPanel dlgcontent = new JPanel();
        dlgcontent.setLayout(GLayout_middle);
        
        JPanel dlgcontent2 = new JPanel();
        dlgcontent2.setLayout(FLayout_bottom);

        JLabel Title = new JLabel("WikiDoku - Plugin Data");
        Title.setHorizontalAlignment(Title.CENTER);
        
        dialog.setLayout(GLayout_bar);      
        dialog.add(Title);
        
        JLabel userText = new JLabel("Login - Name");
        final JTextField userName = new JTextField();
        userName.setText(WikiUserName);
        JLabel locationText = new JLabel("URL des JAMS - Wiki");
        final JTextField location = new JTextField();
        if (WikiLocation == null)
            location.setText("http://jams.uni-jena.de/jamswiki/");
        else
            location.setText(WikiLocation);

        JLabel passwordText = new JLabel("Passwort:");
        final JPasswordField password = new JPasswordField();
        password.setText(WikiPassword);
        
        JLabel jamsDocText = new JLabel("JAMSWikiDoc - Verzeichnis:");
        final JTextField jamsDocPath = new JTextField();                
        jamsDocPath.setText(WikiDocPath);
        
        JButton okButton = new JButton();    
        okButton.setText("Save");
        JButton cancelButton = new JButton();        
        
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {                
                dialog.setVisible(false);
            }
        });
        
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                //daten speichern
                if (activatedNodes.length > 1) {
                    System.out.println("There is a problem, because there are more than one activated nodes");
                    return;
                }   
                String cipherPassword = "";
                try{
                    byte[]key = "sesamöff".getBytes();
                    Key secKey2 = new SecretKeySpec( key, "DES" );
                    Cipher cipher2 = null; 
                    cipher2 = Cipher.getInstance( "DES" );
                    cipher2.init(Cipher.ENCRYPT_MODE, secKey2);
                    byte[] tmp = (new String(password.getPassword()).getBytes());
                    byte[] cipherText = cipher2.doFinal(tmp);
                    for (int i=0;i<cipherText.length;i++){
                        cipherPassword += cipherText[i] + "_";
                    }
                    //cipherPassword = new String(cipherText);
                }catch(Exception e2){
                    System.out.println("Error, because:" + e2.toString());
                }
                properties.setProperty("WikiUser",userName.getText());
                properties.setProperty("WikiLocation",location.getText());                               
                properties.setProperty("WikiPassword",cipherPassword);                
                properties.setProperty("JAMSDocPath",jamsDocPath.getText());

                try{
                    properties.store(new FileOutputStream(propertiesFile),null);
                }catch(Exception e2){
                    System.out.println("could not store data, because:" + e2.toString());
                }
                dialog.setVisible(false);
            }
        });
        
        cancelButton.setText("Cancel");
        
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
        dialog.add(dlgcontent2);
                
        dialog.setSize(400,250);
        dialog.setVisible(true);
    }
    
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }
    
    public String getName() {
        return NbBundle.getMessage(WikiProperties.class, "CTL_WikiProperties");
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {
            Project.class
        };
    }
    
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(Node[] nodes) {
        return true;
    }
}

