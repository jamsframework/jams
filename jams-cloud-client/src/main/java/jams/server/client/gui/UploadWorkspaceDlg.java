/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.server.client.gui;

import jams.gui.CancelableWorkerDlg;
import jams.gui.ObserverWorkerDlg;
import jams.gui.WorkerDlg;
import jams.runtime.StandardRuntime;
import jams.server.client.Controller;
import jams.server.client.JAMSWorkspaceUploader;
import jams.server.client.error.DefaultFileUploadErrorHandling;
import jams.tools.LogTools.ObservableLogHandler;
import jams.workspace.JAMSWorkspace;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author christian
 */
public class UploadWorkspaceDlg extends JDialog{
    JTextField wsName = new JTextField();
    JLabel wsNameLabel = new JLabel("Name of workspace ");
    
    JTextField wsPath = new JTextField();
    JLabel wsPathLabel = new JLabel("Path to workspace ");
    JButton chooseWorkspace = new JButton("Choose");
    
    JTextField compLibPath = new JTextField();
    JLabel compLibPathLabel = new JLabel("Path to components library ");
    JButton chooseCompLib = new JButton("Choose");
    
    JTextField rtLibPath = new JTextField();
    JLabel uiLibPathLabel = new JLabel("Path to executable library ");
    JButton chooseUiLib = new JButton("Choose");
    
    JButton uploadButton = new JButton("Upload");
    JButton cancelButton = new JButton("Cancel");

    boolean uiOk = false, libOk = false, wsOk = false;
    
    JFileChooser jfc = new JFileChooser();
    Controller controller = null;
    JPanel mainPanel = new JPanel();
    
    ObservableLogHandler observable = new ObservableLogHandler(new Logger[]{Logger.getLogger(Controller.class.getName())});
    boolean uploadSuccessful = false;
    
    /**
     *
     * @param w
     * @param controller
     */
    public UploadWorkspaceDlg(Window w, Controller controller){
        super(w, "Upload Workspace");        
        this.controller = controller;
                        
        add(getPanel());
        pack();
        setResizable(false);
    }
    
    /**
     *
     * @param w
     * @param controller
     */
    public UploadWorkspaceDlg(Frame w, Controller controller){
        super(w, "Upload Workspace");
        this.controller = controller;
        
        add(getPanel());
        pack();
        setResizable(false);
    }
    
    /**
     *
     * @param w
     * @param controller
     */
    public UploadWorkspaceDlg(Dialog w, Controller controller){
        super(w, "Upload Workspace");
        this.controller = controller;
        
        add(getPanel());
        pack();
        setResizable(false);
    }
    
    private JPanel getPanel(){        
        GroupLayout mainLayout = new GroupLayout(mainPanel);
        mainPanel.setLayout(mainLayout);
        
        Box bottom = new Box(BoxLayout.X_AXIS);
        bottom.add(Box.createHorizontalGlue());
        bottom.add(uploadButton);
        bottom.add(cancelButton);
        bottom.add(Box.createHorizontalGlue());
        
        mainLayout.setHorizontalGroup(mainLayout.createParallelGroup()
                .addGroup(mainLayout.createSequentialGroup()
                        .addComponent(wsNameLabel, 100, 200, 500)
                        .addContainerGap()
                        .addComponent(wsName, 200, 200, 200)
                )
                .addGroup(mainLayout.createSequentialGroup()
                        .addComponent(wsPathLabel, 100, 200, 500)
                        .addContainerGap()
                        .addComponent(wsPath, 200, 200, 200)
                        .addContainerGap()
                        .addComponent(chooseWorkspace)
                )
                .addGroup(mainLayout.createSequentialGroup()
                        .addComponent(compLibPathLabel, 100, 200, 500)
                        .addContainerGap()
                        .addComponent(compLibPath, 200, 200, 200)
                        .addContainerGap()
                        .addComponent(chooseCompLib)
                )
                .addGroup(mainLayout.createSequentialGroup()
                        .addComponent(uiLibPathLabel, 100, 200, 500)
                        .addContainerGap()
                        .addComponent(rtLibPath, 200, 200, 200)
                        .addContainerGap()
                        .addComponent(chooseUiLib)
                )
                .addComponent(bottom)
        );
        
        mainLayout.setVerticalGroup(mainLayout.createSequentialGroup()
                .addGap(5, 10, 10)
                .addGroup(mainLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(wsNameLabel,25,25,25)
                        .addComponent(wsName,25,25,25)
                )
                .addGap(5, 10, 10)
                .addGroup(mainLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(wsPathLabel,25,25,25)
                        .addComponent(wsPath,25,25,25)
                        .addComponent(chooseWorkspace,25,25,25)
                )
                .addGroup(mainLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(compLibPathLabel,25,25,25)
                        .addComponent(compLibPath,25,25,25)
                        .addComponent(chooseCompLib,25,25,25)
                )
                .addGroup(mainLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(uiLibPathLabel,25,25,25)
                        .addComponent(rtLibPath,25,25,25)
                        .addComponent(chooseUiLib,25,25,25)
                )
                .addGap(20, 20, 20)
                .addComponent(bottom)
        );
        
        chooseWorkspace.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jfc.setMultiSelectionEnabled(false);
                if (jfc.showOpenDialog(wsPath) == JFileChooser.APPROVE_OPTION){
                    File f = jfc.getSelectedFile();
                    wsPath.setText(f.getAbsolutePath());
                }
            }
        });
        
        chooseCompLib.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jfc.setFileFilter(null);
                jfc.setMultiSelectionEnabled(false);
                if (jfc.showOpenDialog(wsPath) == JFileChooser.APPROVE_OPTION){
                    File f = jfc.getSelectedFile();
                    compLibPath.setText(f.getAbsolutePath());
                }
            }
        });
        
        chooseUiLib.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.setFileFilter(new FileFilter() {
                    
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || (f.isFile() && f.getName().endsWith(".jar"));
                    }

                    @Override
                    public String getDescription() {
                        return "Java Archive (.jar)";
                    }
                });
                jfc.setMultiSelectionEnabled(false);
                if (jfc.showOpenDialog(wsPath) == JFileChooser.APPROVE_OPTION){
                    File f = jfc.getSelectedFile();
                    rtLibPath.setText(f.getAbsolutePath());
                }
            }
        });        
        
        rtLibPath.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                File f = new File(rtLibPath.getText());
                if (!f.exists() || !f.isFile() || !f.getName().endsWith(".jar")){
                    rtLibPath.setBackground(Color.red);
                    uiOk = false;
                }else{
                    rtLibPath.setBackground(Color.white);
                    uiOk = true;
                }
                uploadButton.setEnabled(uiOk & wsOk & libOk);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                File f = new File(rtLibPath.getText());
                if (!f.exists() || !f.isFile() || !f.getName().endsWith(".jar")){
                    rtLibPath.setBackground(Color.red);
                    uiOk = false;
                }else{
                    rtLibPath.setBackground(Color.white);
                    uiOk = true;
                }
                uploadButton.setEnabled(uiOk & wsOk & libOk);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                File f = new File(rtLibPath.getText());
                if (!f.exists() || !f.isFile() || !f.getName().endsWith(".jar")){
                    rtLibPath.setBackground(Color.red);
                    uiOk = false;
                }else{
                    rtLibPath.setBackground(Color.white);
                    uiOk = true;
                }
                uploadButton.setEnabled(uiOk & wsOk & libOk);
            }
        });
        
        compLibPath.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                File f = new File(compLibPath.getText());
                if (!f.exists() || !f.isDirectory()){
                    compLibPath.setBackground(Color.red);
                    libOk = false;
                }else{
                    compLibPath.setBackground(Color.white);
                    libOk = true;
                }
                uploadButton.setEnabled(uiOk & wsOk & libOk);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                File f = new File(compLibPath.getText());
                if (!f.exists() || !f.isDirectory()){
                    compLibPath.setBackground(Color.red);
                    libOk = false;
                }else{
                    compLibPath.setBackground(Color.white);
                    libOk = true;
                }
                uploadButton.setEnabled(uiOk & wsOk & libOk);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                File f = new File(compLibPath.getText());
                if (!f.exists() || !f.isDirectory()){
                    compLibPath.setBackground(Color.red);
                    libOk = false;
                }else{
                    compLibPath.setBackground(Color.white);
                    libOk = true;
                }
                uploadButton.setEnabled(uiOk & wsOk & libOk);
            }
        });
        
        wsPath.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                File f = new File(wsPath.getText());
                if (!f.exists() || !f.isDirectory()){
                    wsPath.setBackground(Color.red);
                    wsOk = false;
                }else{
                    wsPath.setBackground(Color.white);
                    wsOk = true;
                }
                uploadButton.setEnabled(uiOk & wsOk & libOk);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                File f = new File(wsPath.getText());
                if (!f.exists() || !f.isDirectory()){
                    wsPath.setBackground(Color.red);
                    wsOk = false;
                }else{
                    wsPath.setBackground(Color.white);
                    wsOk = true;
                }
                uploadButton.setEnabled(uiOk & wsOk & libOk);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                File f = new File(wsPath.getText());
                if (!f.exists() || !f.isDirectory()){
                    wsPath.setBackground(Color.red);
                    wsOk = false;
                }else{
                    wsPath.setBackground(Color.white);
                    wsOk = true;
                }
                uploadButton.setEnabled(uiOk & wsOk & libOk);
            }
        });
        
        this.uploadButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ObserverWorkerDlg worker = null;
                
                Component c = SwingUtilities.getRoot(mainPanel);
                
                if ( c instanceof Frame){
                    worker = new ObserverWorkerDlg(
                             new CancelableWorkerDlg(
                             new WorkerDlg((Frame)c, "Uploading Workspace")).getWorkerDlg());
                }else{
                    worker = new ObserverWorkerDlg(
                             new CancelableWorkerDlg(
                             new WorkerDlg((Frame)c, "Uploading Workspace")).getWorkerDlg());
                }
                observable.deleteObservers();
                observable.addObserver(worker);        
                worker.getWorkerDlg().setInderminate(true);
                worker.getWorkerDlg().setTask(new Runnable() {

                    @Override
                    public void run() {
                        try{
                            uploadWorkspace();                        
                        }catch(IOException ioe){
                            ioe.printStackTrace();
                        }
                    }
                });
               worker.getWorkerDlg().execute();                              
            }
        });
        
        this.cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component c = SwingUtilities.getRoot(mainPanel);
                if (c != null)
                    c.setVisible(false);
            }
        });
        
        return mainPanel;
    }
    
    private void uploadWorkspace() throws IOException{
        uploadSuccessful = false;
        if (!validateInput()){
            JOptionPane.showMessageDialog(this.mainPanel, "Unable to upload workspace! Paths are incorrect!");
        }
        File workspaceDirectory = new File(wsPath.getText());
        File compLibFile   = new File(compLibPath.getText());
        File rtLibFile     = new File(rtLibPath.getText());   
        String name = wsName.getText();
        
        JAMSWorkspace workspace = new JAMSWorkspace(workspaceDirectory, new StandardRuntime(null));
        JAMSWorkspaceUploader uploader = new JAMSWorkspaceUploader(controller);
        uploader.uploadWorkspace(workspace, new File[]{compLibFile}, rtLibFile, "", 
                new DefaultFileUploadErrorHandling() );
        
        uploadSuccessful=true;
    }
    
    /**
     *
     * @return
     */
    public boolean validateInput(){
        File workspaceFile = new File(wsPath.getText());
        File compLibFile   = new File(compLibPath.getText());
        File uiLibFile     = new File(rtLibPath.getText());                
        
        if (!workspaceFile.exists() || !workspaceFile.isDirectory()) 
            return false;
        if (!compLibFile.exists() || !compLibFile.isDirectory())
            return false;
        if (!uiLibFile.exists() || !uiLibFile.isFile())
            return false;
        return true;
    }
    
    boolean getUploadSuccessful(){
        return uploadSuccessful;
    }
}
