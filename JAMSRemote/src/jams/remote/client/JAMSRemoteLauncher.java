/*
 * JAMSRemoteLauncher.java
 * Created on 20. Juni 2007, 09:20
 *
 * This file is part of JAMS
 * Copyright (C) 2007 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package jams.remote.client;

import jams.remote.server.Server;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.unijena.jams.JAMSProperties;
import org.unijena.jams.JAMSTools;
import org.unijena.jams.gui.LauncherFrame;
import org.unijena.jams.gui.LHelper;
import org.unijena.jams.gui.LogViewDlg;
import org.unijena.jams.gui.input.InputComponent;
import org.unijena.jams.gui.input.ListInput;
import org.unijena.jams.io.XMLIO;
import org.w3c.dom.Element;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSRemoteLauncher extends LauncherFrame {
    
    private static final String BASE_TITLE = "JAMS Remote Launcher";
    private static final String CONNECTED_BUTTON_TEXT = "Close Connection";
    private static final String CLOSED_BUTTON_TEXT = "Connect to Server";
    private static final int SERVER_PANEL_WIDTH = 200;
    private static final String SERVER_LIB_DIR = "libs";
    private static final String MODEL_FILE_NAME = "$model.jam";
    
    private JList list;
    private ListInput serverList;
    private JButton uploadButton, uploadLibsButton, downloadButton, connectButton, refreshButton, cleanWSButton, cleanAccountButton, updateLogsButton;
    private Client client = null;
    private JLabel conClientLabel, maxClientLabel, addressLabel, socketLabel;
    private JTextField baseDirLabel;
    private JTextField account, excludes;
    private JPasswordField password;
    private JComboBox workspaceSelector;
    private LogViewDlg serverInfoDlg = new LogViewDlg(this, 400, 400, "Server Info Log");
    private LogViewDlg serverErrorDlg = new LogViewDlg(this, 400, 400, "Server Error Log");
    private String baseDir;
    
    //private Map<Element, InputComponent> propertyInput;
    
    public JAMSRemoteLauncher(JAMSProperties properties) {
        super(properties);
        adapt();
    }
    
    public JAMSRemoteLauncher(String modelFilename, JAMSProperties properties, String cmdLineArgs) {
        super( modelFilename, properties, cmdLineArgs);
        adapt();
    }
    
    private void adapt() {
        
        Dimension size = this.getSize();
        this.setPreferredSize(new Dimension(size.width+SERVER_PANEL_WIDTH, size.height+50));
        
        JMenu logsMenu = getLogsMenu();
        JMenuItem serverInfoLogItem = new JMenuItem("Server info log");
        serverInfoLogItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                serverInfoDlg.setVisible(true);
            }
        });
        logsMenu.add(serverInfoLogItem);
        
        JMenuItem serverErrorLogItem = new JMenuItem("Server error log");
        serverErrorLogItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                serverErrorDlg.setVisible(true);
            }
        });
        logsMenu.add(serverErrorLogItem);
        
        getRunButton().setEnabled(false);
        
        JPanel serverPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        serverPanel.setLayout(gbl);
        
        LHelper.addGBComponent(serverPanel, gbl, new JLabel("Server List:"), 0, 5, 1, 1, 0, 0);
        
        serverList = new ListInput(false);
        serverList.setPreferredSize(new Dimension(100, 100));
        
        serverList.addListDataObserver(new Observer() {
            public void update(Observable o, Object arg) {
                String servers = "";
                for (String server : serverList.getListData()) {
                    servers += server + ";";
                }
                servers = servers.substring(0, servers.length()-1);
                getProperties().setProperty(JAMSProperties.SERVER_IDENTIFIER, servers);
            }
        });
        
        list = serverList.getListbox();
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    boolean enabled;
                    if (list.isSelectionEmpty()) {
                        enabled = false;
                    } else {
                        enabled = true;
                    }
                    connectButton.setEnabled(enabled);
                }
            }
        });
        
        String server = getProperties().getProperty(JAMSProperties.SERVER_IDENTIFIER);
        if (server != null) {
            Vector<String> listData = new Vector<String>();
            for (String str : JAMSTools.toArray(server, ";")) {
                listData.add(str);
            }
            serverList.setListData(listData);
        }
        
        LHelper.addGBComponent(serverPanel, gbl, serverList, 0, 10, 2, 1, 0, 0);
        
        LHelper.addGBComponent(serverPanel, gbl, new JLabel("Account:"), 0, 11, 1, 1, 0, 0);
        account = new JTextField();
        account.setText(getProperties().getProperty(JAMSProperties.SERVER_ACCOUNT_IDENTIFIER));
        account.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateAccount();
            }
            public void insertUpdate(DocumentEvent e) {
                updateAccount();
            }
            public void removeUpdate(DocumentEvent e) {
                updateAccount();
            }
        });
        LHelper.addGBComponent(serverPanel, gbl, account, 1, 11, 1, 1, 0, 0);
        
        LHelper.addGBComponent(serverPanel, gbl, new JLabel("Password:"), 0, 12, 1, 1, 0, 0);
        password = new JPasswordField();
        password.setText(getProperties().getProperty(JAMSProperties.SERVER_PASSWORD_IDENTIFIER));
        password.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updatePassword();
            }
            public void insertUpdate(DocumentEvent e) {
                updatePassword();
            }
            public void removeUpdate(DocumentEvent e) {
                updatePassword();
            }
        });
        LHelper.addGBComponent(serverPanel, gbl, password, 1, 12, 1, 1, 0, 0);
        
        connectButton = new JButton(CLOSED_BUTTON_TEXT);
        LHelper.addGBComponent(serverPanel, gbl, connectButton, 0, 15, 2, 1, 0, 0);
        connectButton.setEnabled(false);
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ((client != null) && (!client.isClosed())) {
                    close();
                } else {
                    connect((String) list.getSelectedValue(), account.getText(), new String(password.getPassword()));
                }
            }
        });
        
        LHelper.addGBComponent(serverPanel, gbl, new JPanel(), 0, 19, 2, 1, 0, 0);
        LHelper.addGBComponent(serverPanel, gbl, new JLabel("Server Info:"), 0, 20, 1, 1, 0, 0);
        
        JPanel infoPanel = new JPanel();
        GridBagLayout gbl_info = new GridBagLayout();
        infoPanel.setLayout(gbl_info);
        infoPanel.setBorder(BorderFactory.createEtchedBorder());
        
        LHelper.addGBComponent(infoPanel, gbl_info, new JLabel("Runs:"), 0, 0, 1, 1, 0, 0);
        LHelper.addGBComponent(infoPanel, gbl_info, new JLabel("Max Runs:"), 0, 1, 1, 1, 0, 0);
        LHelper.addGBComponent(infoPanel, gbl_info, new JLabel("Address:"), 0, 2, 1, 1, 0, 0);
        LHelper.addGBComponent(infoPanel, gbl_info, new JLabel("Socket:"), 0, 3, 1, 1, 0, 0);
        JLabel test = new JLabel("Base Dir:");
        //test.setBorder(BorderFactory.createEtchedBorder());
        LHelper.addGBComponent(infoPanel, gbl_info, test, 0, 4, 1, 1, 0, 0);
        
        conClientLabel = new JLabel("", JLabel.RIGHT);
        maxClientLabel = new JLabel("", JLabel.RIGHT);
        addressLabel = new JLabel("", JLabel.RIGHT);
        socketLabel = new JLabel("", JLabel.RIGHT);
        //baseDirLabel = new JLabel("", JLabel.RIGHT);
        baseDirLabel = new JTextField();
        baseDirLabel.setColumns(12);
        baseDirLabel.setEditable(false);
        Dimension dim = new Dimension(100,conClientLabel.getFont().getSize());
        conClientLabel.setPreferredSize(dim);
        maxClientLabel.setPreferredSize(dim);
        addressLabel.setPreferredSize(dim);
        socketLabel.setPreferredSize(dim);
        socketLabel.setPreferredSize(dim);
        
        LHelper.addGBComponent(infoPanel, gbl_info, conClientLabel, 2, 0, 1, 1, 0, 0);
        LHelper.addGBComponent(infoPanel, gbl_info, maxClientLabel, 2, 1, 1, 1, 0, 0);
        LHelper.addGBComponent(infoPanel, gbl_info, addressLabel, 2, 2, 1, 1, 0, 0);
        LHelper.addGBComponent(infoPanel, gbl_info, socketLabel, 2, 3, 1, 1, 0, 0);
        LHelper.addGBComponent(infoPanel, gbl_info, baseDirLabel, 2, 4, 1, 1, 0, 0);
        
        refreshButton = new JButton("Refresh Info");
        LHelper.addGBComponent(infoPanel, gbl_info, refreshButton, 0, 6, 3, 1, 0, 0);
        refreshButton.setEnabled(false);
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshInfo();
            }
        });
        
        LHelper.addGBComponent(infoPanel, gbl_info, new JLabel("Workspace:"), 0, 8, 1, 1, 0, 0);
        if (workspaceSelector == null) {
            workspaceSelector = new JComboBox();
        }
        workspaceSelector.setPreferredSize(new Dimension(0,20));
        workspaceSelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    PropertyDescriptor pd = (PropertyDescriptor) e.getItem();
                    getProperties().setProperty(JAMSProperties.WORKSPACE_IDENTIFIER, pd.toString());
                }
            }
        });
        LHelper.addGBComponent(infoPanel, gbl_info, workspaceSelector, 0, 9, 3, 1, 0, 0);
        
        LHelper.addGBComponent(infoPanel, gbl_info, new JLabel("Excludes:"), 0, 10, 1, 1, 0, 0);
        excludes = new JTextField();
        excludes.setToolTipText("Semicolon-separated list of filename suffixes defining " +
                "files to be exluded from file transfer");
        excludes.setText(getProperties().getProperty(JAMSProperties.SERVER_EXCLUDES_IDENTIFIER, "cache;svn;xls"));
        excludes.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateExcludes();
            }
            public void insertUpdate(DocumentEvent e) {
                updateExcludes();
            }
            public void removeUpdate(DocumentEvent e) {
                updateExcludes();
            }
        });
        LHelper.addGBComponent(infoPanel, gbl_info, excludes, 0, 11, 3, 1, 0, 0);
        
        uploadButton = new JButton("Upload WS");
        uploadButton.setToolTipText("Upload whole workspace to server");
        LHelper.addGBComponent(infoPanel, gbl_info, uploadButton, 0, 15, 1, 1, 0, 0);
        uploadButton.setEnabled(false);
        uploadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uploadWorkspace();
            }
        });
        
        downloadButton = new JButton("Download WS");
        downloadButton.setToolTipText("Download whole workspace from server");
        LHelper.addGBComponent(infoPanel, gbl_info, downloadButton, 2, 15, 1, 1, 0, 0);
        downloadButton.setEnabled(false);
        downloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                downloadWorkspace();
            }
        });
        
        uploadLibsButton = new JButton("Upload Libraries");
        uploadLibsButton.setToolTipText("Upload all libraries to server");
        LHelper.addGBComponent(infoPanel, gbl_info, uploadLibsButton, 0, 20, 3, 1, 0, 0);
        uploadLibsButton.setEnabled(false);
        uploadLibsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uploadLibs();
            }
        });
        
        cleanWSButton = new JButton("Clean Workspace");
        cleanWSButton.setToolTipText("Delete all server files inside the workspace");
        LHelper.addGBComponent(infoPanel, gbl_info, cleanWSButton, 0, 25, 1, 1, 0, 0);
        cleanWSButton.setEnabled(false);
        cleanWSButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cleanWorkspace();
            }
        });
        
        cleanAccountButton = new JButton("Clean Account");
        cleanAccountButton.setToolTipText("Delete all server files belonging to current account");
        LHelper.addGBComponent(infoPanel, gbl_info, cleanAccountButton, 2, 25, 1, 1, 0, 0);
        cleanAccountButton.setEnabled(false);
        cleanAccountButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cleanAccount();
            }
        });
        
        updateLogsButton = new JButton("Update Logs");
        updateLogsButton.setToolTipText("Download model logs of current workspace from server");
        LHelper.addGBComponent(infoPanel, gbl_info, updateLogsButton, 0, 28, 3, 1, 0, 0);
        updateLogsButton.setEnabled(false);
        updateLogsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateLogs();
            }
        });
        
        
        LHelper.addGBComponent(serverPanel, gbl, infoPanel, 0, 25, 2, 1, 0, 0);
        
        this.add(new JScrollPane(serverPanel), BorderLayout.WEST);
        
        this.pack();
    }
    
    private void connect(String address, String account, String password) {
        
        int port = Server.STANDARD_PORT;
        String host = "";
        String[] serverData = JAMSTools.toArray(address, ":");
        try {
            host = serverData[0];
            if (serverData.length > 1) {
                port = Integer.parseInt(serverData[1]);
            }
        } catch (Exception ex) {
            System.out.println("Malformed server address: " + address);
            return;
        }
        
        client = new Client(host, port, account, password);
        // add info and error log output
        client.addInfoLogObserver(new Observer() {
            public void update(Observable obs, Object obj) {
                JAMSRemoteLauncher.this.serverInfoDlg.appendText(obj.toString());
            }
        });
        client.addErrorLogObserver(new Observer() {
            public void update(Observable obs, Object obj) {
                JAMSRemoteLauncher.this.serverErrorDlg.appendText(obj.toString());
                LHelper.showErrorDlg(JAMSRemoteLauncher.this, obj.toString(), "Client error");
            }
        });
        
        client.connect();
        
        if (!client.isClosed()) {
            refreshInfo();
            connectButton.setText(CONNECTED_BUTTON_TEXT);
            serverList.setEnabled(false);
            refreshButton.setEnabled(true);
            uploadButton.setEnabled(true);
            downloadButton.setEnabled(true);
            uploadLibsButton.setEnabled(true);
            cleanWSButton.setEnabled(true);
            cleanAccountButton.setEnabled(true);
            updateLogsButton.setEnabled(true);
            getRunButton().setEnabled(true);
        } else {
            serverInfoDlg.appendText("Failed connecting to server!\n");
        }
    }
    
    private void close() {
        try {
            client.stopClient();
            conClientLabel.setText("");
            maxClientLabel.setText("");
            addressLabel.setText("");
            socketLabel.setText("");
            baseDirLabel.setText("");
            connectButton.setText(CLOSED_BUTTON_TEXT);
            serverList.setEnabled(true);
            refreshButton.setEnabled(false);
            uploadButton.setEnabled(false);
            cleanWSButton.setEnabled(false);
            uploadLibsButton.setEnabled(false);
            downloadButton.setEnabled(false);
            cleanAccountButton.setEnabled(false);
            updateLogsButton.setEnabled(false);
            getRunButton().setEnabled(false);
        } catch (IOException ex) {
            client.getErrorLog().print(JAMSTools.getStackTraceString(ex.getStackTrace()));
        }
    }
    
    private void refreshInfo() {
        try {
            conClientLabel.setText("" + client.getRunCount());
            maxClientLabel.setText("" + client.getMaxRunCount());
            String address = client.getServerAddress();
            String[] parts = JAMSTools.toArray(address, ":");
            addressLabel.setText(parts[0]);
            socketLabel.setText(parts[1]);
            baseDir = client.getBaseDir();
            baseDirLabel.setText(baseDir);
        } catch (IOException ex) {
            client.getErrorLog().print(JAMSTools.getStackTraceString(ex.getStackTrace()));
        }
    }
    
    private void updateAccount() {
        getProperties().setProperty(JAMSProperties.SERVER_ACCOUNT_IDENTIFIER, account.getText());
    }
    
    private void updatePassword() {
        getProperties().setProperty(JAMSProperties.SERVER_PASSWORD_IDENTIFIER, new String(password.getPassword()));
    }
    
    private void updateExcludes() {
        getProperties().setProperty(JAMSProperties.SERVER_EXCLUDES_IDENTIFIER, excludes.getText());
    }
    
    protected void loadModelDefinition(String modelFilename, String[] args) {
        super.loadModelDefinition(modelFilename, args);
        fillWorkspaceSelector();
    }
    
    private void fillWorkspaceSelector() {
        
        PropertyDescriptor selectedPd = null;
        
        ArrayList<PropertyDescriptor> propertyList = new ArrayList<PropertyDescriptor>();
        ArrayList<String> nameList = new ArrayList<String>();
        
        Map<InputComponent, Element> inputProperty = getInputMap();
        Map<String, PropertyDescriptor> nameMap = new HashMap<String, PropertyDescriptor>();
        
        if (inputProperty == null) {
            return;
        }
        
        String selectedName = getProperties().getProperty(JAMSProperties.WORKSPACE_IDENTIFIER);
        
        for (InputComponent input : inputProperty.keySet()) {
            PropertyDescriptor pd = new PropertyDescriptor();
            pd.input = input;
            pd.property = inputProperty.get(input);
            String name = pd.toString();
            if (name.equals(selectedName)) {
                selectedPd = pd;
            }
            nameMap.put(name, pd);
            nameList.add(name);
        }
        
        Collections.sort(nameList);
        
        for (String name : nameList) {
            propertyList.add(nameMap.get(name));
        }
        
        if (workspaceSelector == null) {
            workspaceSelector = new JComboBox();
        }
        workspaceSelector.setModel(new DefaultComboBoxModel(new Vector<PropertyDescriptor>(propertyList)));
        workspaceSelector.setSelectedItem(selectedPd);
    }
    
    private void uploadWorkspace() {
        
        PropertyDescriptor pd = (PropertyDescriptor) workspaceSelector.getSelectedItem();
        if (pd == null) {
            return;
        }
        
        try {
            String localWorkspace = pd.input.getValue();
            String remoteWorkspacePath = new File(localWorkspace).toURI().getPath().replaceAll(":", "");
            client.pushDir(remoteWorkspacePath, localWorkspace, ".cache;.svn");
        } catch (Exception ex) {
            client.getErrorLog().print(JAMSTools.getStackTraceString(ex.getStackTrace()));
            client.getInfoLog().print("Failed uploading workspace!\n");
        }
        
    }
    
    private void downloadWorkspace() {
        
        PropertyDescriptor pd = (PropertyDescriptor) workspaceSelector.getSelectedItem();
        if (pd == null) {
            return;
        }
        
        try {
            String localWorkspace = pd.input.getValue();
            String remoteWorkspacePath = new File(localWorkspace).toURI().getPath().replaceAll(":", "");
            if (remoteWorkspacePath.endsWith("/")) {
                remoteWorkspacePath = remoteWorkspacePath.substring(0, remoteWorkspacePath.length()-1);
            }
            client.getDir(remoteWorkspacePath, localWorkspace, excludes.getText());
        } catch (Exception ex) {
            client.getErrorLog().print(JAMSTools.getStackTraceString(ex.getStackTrace()));
            client.getInfoLog().print("Failed downloading workspace!\n");
        }
    }
    
    private void cleanWorkspace() {
        PropertyDescriptor pd = (PropertyDescriptor) workspaceSelector.getSelectedItem();
        if (pd == null) {
            return;
        }
        
        try {
            String localWorkspace = pd.input.getValue();
            String remoteWorkspacePath = new File(localWorkspace).toURI().getPath().replaceAll(":", "");
            client.cleanDir(remoteWorkspacePath);
        } catch (Exception ex) {
            client.getErrorLog().print(JAMSTools.getStackTraceString(ex.getStackTrace()));
            client.getInfoLog().print("Failed cleaning workspace!\n");
        }
    }
    
    private void cleanAccount() {
        PropertyDescriptor pd = (PropertyDescriptor) workspaceSelector.getSelectedItem();
        if (pd == null) {
            return;
        }
        
        try {
            client.cleanDir(".");
        } catch (Exception ex) {
            client.getErrorLog().print(JAMSTools.getStackTraceString(ex.getStackTrace()));
            client.getInfoLog().print("Failed cleaning account!\n");
        }
    }
    
    private void uploadLibs() {
        
        //create lib dir
        try {
            client.createDir(SERVER_LIB_DIR);
        } catch (IOException ex) {
            client.getErrorLog().print(JAMSTools.getStackTraceString(ex.getStackTrace()));
            client.getInfoLog().print("Failed creating server lib dir\n");
        }
        
        //copy necessary libs to the server
        String[] libs = JAMSTools.toArray(getProperties().getProperty(JAMSProperties.LIBS_IDENTIFIER), ";");
        for (String lib : libs) {
            File libFile = new File(lib);
            if (libFile.isDirectory()) {
                try {
                    client.pushDir(SERVER_LIB_DIR, libFile.getAbsolutePath(), ".cache;.svn");
                    client.getInfoLog().print("Uploaded lib dir " + lib + "\n");
                } catch (IOException ex) {
                    client.getErrorLog().print(JAMSTools.getStackTraceString(ex.getStackTrace()));
                    client.getInfoLog().print("Failed uploading lib dir " + lib + "\n");
                }
            } else if (libFile.exists()) {
                try {
                    client.pushFile(SERVER_LIB_DIR + "/" + libFile.getName(), libFile.getAbsolutePath());
                    client.getInfoLog().print("Uploaded lib file " + lib + "\n");
                } catch (IOException ex) {
                    client.getErrorLog().print(JAMSTools.getStackTraceString(ex.getStackTrace()));
                    client.getInfoLog().print("Failed uploading lib file " + lib + "\n");
                }
            }
        }
    }
    
    private void updateLogs() {
        PropertyDescriptor pd = (PropertyDescriptor) workspaceSelector.getSelectedItem();
        if (pd == null) {
            return;
        }
        
        try {
            String localWorkspace = pd.input.getValue();
            String remoteWorkspacePath = new File(localWorkspace).toURI().getPath().replaceAll(":", "");
            
            int offset = getInfoDlg().getText().length();
            String modelInfo = client.getModelInfoLog(remoteWorkspacePath, offset);
            getInfoDlg().appendText(modelInfo);
            getInfoDlg().setVisible(true);
            
            offset = getErrorDlg().getText().length();
            String modelError = client.getModelErrorLog(remoteWorkspacePath, offset);
            getErrorDlg().appendText(modelError);
            getErrorDlg().setVisible(true);
            
        } catch (Exception ex) {
            client.getErrorLog().print(JAMSTools.getStackTraceString(ex.getStackTrace()));
            client.getInfoLog().print("Failed getting model logs!\n");
        }
    }
    
    protected void runModel() {
        
        getInfoDlg().setText("");
        getErrorDlg().setText("");
        getRunButton().setEnabled(false);
        
        // check if provided values are valid
        if (!verifyInputs()) {
            getRunButton().setEnabled(true);
            return;
        }
        
        // set values of document elements to provided
        for (InputComponent ic : getInputMap().keySet()) {
            Element element = getInputMap().get(ic);
            element.setAttribute("value", ic.getValue());
        }
        
        //update workspace value
        PropertyDescriptor pd = (PropertyDescriptor) workspaceSelector.getSelectedItem();
        String localWorkspace = pd.input.getValue();
        String remoteWorkspace = new File(localWorkspace).toURI().getPath().replaceAll(":", "");
        String absoluteRemoteWorkspace = baseDir + "/" + remoteWorkspace;
        absoluteRemoteWorkspace = absoluteRemoteWorkspace.replace("\\", "/");
        pd.property.setAttribute("value", absoluteRemoteWorkspace);
        
        // create a copy of the model document
        //Document modelDocCopy = (Document) getModelDocument().cloneNode(true);
        //String modelDocString = XMLIO.getStringFromDocument(modelDocCopy).replace("\\", "/");
        
        //create local model file
        String localModelFilename = new File(localWorkspace + "/" + MODEL_FILE_NAME).getAbsolutePath();
        try {
            XMLIO.writeXmlFile(getModelDocument(), localModelFilename);
        } catch (IOException ex) {
            client.getErrorLog().print("Model definition file " + localModelFilename + " could not be written!\n");
            getRunButton().setEnabled(true);
            return;
        }
        
        //upload local model file to server
        String remoteModelFilename = remoteWorkspace + "/" + MODEL_FILE_NAME;
        try {
            client.pushFile(remoteModelFilename, localModelFilename);
        } catch (IOException ex) {
            client.getErrorLog().print("Model definition file " + localModelFilename + " could not be" +
                    "transfered to server!\n");
            getRunButton().setEnabled(true);
            return;
        }
        
        String debug = getProperties().getProperty("debug");
        
        client.getInfoLog().print("Starting remote execution\n");
        
        //start execution
        try {
            int result = client.runJAMS(remoteWorkspace, SERVER_LIB_DIR, remoteModelFilename, debug);
/*
            getInfoDlg().appendText(result[0]);
            getErrorDlg().appendText(result[1]);
 */
            client.getInfoLog().print("Remote execution finished\n");
            
        } catch (IOException ex) {
            client.getErrorLog().print(JAMSTools.getStackTraceString(ex.getStackTrace()));
            client.getInfoLog().print("Remote execution failed\n");
        }
        getRunButton().setEnabled(true);
    }
    
    protected String getBaseTitle() {
        return BASE_TITLE;
    }
    
    class PropertyDescriptor {
        
        InputComponent input;
        Element property;
        
        public String toString() {
            return property.getAttribute("name");
        }
    }
    
}
