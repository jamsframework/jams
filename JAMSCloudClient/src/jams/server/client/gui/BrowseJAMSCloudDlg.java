/*
 * RemoteGUI.java
 * Created on 07.05.2014, 15:28:54
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.server.client.gui;

import jams.JAMS;
import jams.SystemProperties;
import jams.gui.WorkerDlg;
import jams.gui.tools.GUIHelper;
import jams.server.client.Controller;
import jams.server.client.ObservableLogHandler;
import jams.server.client.Utilities;
import jams.server.client.gui.tree.JAMSServerTreeNodes;
import jams.server.client.gui.tree.JAMSServerTreeNodes.WFANode;
import jams.server.client.gui.tree.JobsTree;
import jams.server.client.gui.tree.WorkspaceTree;
import jams.server.entities.Job;
import jams.server.entities.JobState;
import jams.server.entities.Jobs;
import jams.server.entities.User;
import jams.server.entities.Workspace;
import jams.server.entities.WorkspaceFileAssociation;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author christian
 */
public class BrowseJAMSCloudDlg extends JDialog{
    private static final Logger log = Logger.getLogger( BrowseJAMSCloudDlg.class.getName() );
    
    final String infoLogFile = "info.log";
    final String errorLog = "error.log";
    
    Jobs jobs = null;

    JPanel mainPanel = null;
    JTabbedPane viewPane = new JTabbedPane();
    JComboBox serverField = new JComboBox();
    WorkspaceTree workspaceTree = new WorkspaceTree();
    JobsTree jobsTree = new JobsTree();
    
    JFileChooser jfc = new JFileChooser();
    JFileChooser wsChooser = new JFileChooser();

    JLabel wsNameLabel = new JLabel("name:");
    JTextField wsName = new JTextField(20);
    
    JLabel wsCreationDate = new JLabel("created:");
    JTextField wsCreation = new JTextField(10);
    
    JLabel wsSizeLabel = new JLabel("size:");
    JTextField wsSize = new JTextField(10);
    
    JLabel jobNameLabel = new JLabel("name:");
    JTextField jobName = new JTextField(20);
    
    JLabel jobCreationDate = new JLabel("created:");
    JTextField jobCreation = new JTextField(10);
    
    JLabel jobSizeLabel = new JLabel("size:");
    JTextField jobSize = new JTextField(10);
    
    JLabel statusLabel = new JLabel("status");
    
    GraphicalClient connector = null;
        
    ObservableLogHandler observable = new ObservableLogHandler(new Logger[]{Logger.getLogger(Controller.class.getName())});
    JButton connectButton = null;
    
    Action connectAction = null,
            deleteJobAction = null,
            killJobAction = null,
            deleteWorkspaceAction = null,
            startWorkspaceAction = null,
            downloadWorkspaceAction = null,
            downloadFileAction = null,
            uploadWorkspaceAction = null,
            showFileProperties = null,
            updateJobAction = null,
            deleteAllJobsAction = null,
            deleteAllWorkspacesAction = null,
            showInfoLogAction = null,
            showErrorLogAction = null;

    Controller client = null;
    SystemProperties p = null;
    
    public BrowseJAMSCloudDlg(Window w, SystemProperties p){      
        super(w, JAMS.i18n("JAMS-Cloud"));
        
        MsgBoxLogHandler.registerLogger(Logger.getLogger(Controller.class.getName()));
        MsgBoxLogHandler.registerLogger(Logger.getLogger(BrowseJAMSCloudDlg.class.getName()));
        
        connector = new GraphicalClient(mainPanel, p);        
        this.p = p;
    }
     
    public void init() {
        initActions();

        mainPanel = new JPanel();
        GroupLayout mainLayout = new GroupLayout(mainPanel);
        mainPanel.setLayout(mainLayout);        
        serverField.setBorder(BorderFactory.createTitledBorder(JAMS.i18n("Server-URL")));
        
        connectButton = new JButton(connectAction);        
        connectButton.setText(JAMS.i18n("Connect"));
        JButton uploadWs = new JButton(uploadWorkspaceAction);
        
        JScrollPane jobsScroller = new JScrollPane(jobsTree);        
        jobsScroller.setBorder(BorderFactory.createTitledBorder(JAMS.i18n("Jobs")));

        JScrollPane workspaceTreeScroller = new JScrollPane(workspaceTree);
        workspaceTreeScroller.setBorder(BorderFactory.createTitledBorder(JAMS.i18n("Workspaces")));

        JPanel jobsPanel = new JPanel();
        jobsPanel.setLayout(new BorderLayout());
        
        JPanel wsPanel = new JPanel();
        wsPanel.setLayout(new BorderLayout());        
                                
        JPanel wsInfoPanel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        wsInfoPanel.setLayout(layout);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        
        Insets insetsNone = new Insets(5, 2, 10, 2);
        Insets insetsSpace = new Insets(5, 5, 10, 0);
        
        c.insets = insetsNone;
        wsInfoPanel.add(wsNameLabel, c);
        c.gridx++;
        c.insets = insetsNone;
        wsInfoPanel.add(wsName, c);        
        c.gridx++;
        c.insets = insetsSpace;
        wsInfoPanel.add(wsCreationDate, c);        
        c.gridx++;
        c.insets = insetsNone;
        wsInfoPanel.add(wsCreation, c);        
        c.gridx++;
        c.insets = insetsSpace;
        wsInfoPanel.add(wsSizeLabel, c);       
        c.gridx++;
        c.insets = insetsNone;
        wsInfoPanel.add(wsSize, c);
        
        wsName.setEnabled(false);
        wsCreation.setEnabled(false);
        wsSize.setEnabled(false);
        
        wsPanel.add(workspaceTreeScroller, BorderLayout.CENTER);        
        wsPanel.add(uploadWs, BorderLayout.SOUTH);
        wsPanel.add(wsInfoPanel, BorderLayout.NORTH);
        
        JPanel jobInfoPanel = new JPanel();
        GridBagLayout layout2 = new GridBagLayout();
        jobInfoPanel.setLayout(layout2);
        
        c.insets = insetsNone;
        jobInfoPanel.add(jobNameLabel, c);
        c.gridx++;
        c.insets = insetsNone;
        jobInfoPanel.add(jobName, c);        
        c.gridx++;
        c.insets = insetsSpace;
        jobInfoPanel.add(jobCreationDate, c);        
        c.gridx++;
        c.insets = insetsNone;
        jobInfoPanel.add(jobCreation, c);        
        c.gridx++;
        c.insets = insetsSpace;
        jobInfoPanel.add(jobSizeLabel, c);       
        c.gridx++;
        c.insets = insetsNone;
        jobInfoPanel.add(jobSize, c);
        
        jobName.setEnabled(false);
        jobCreation.setEnabled(false);
        jobSize.setEnabled(false);
        
        jobsPanel.add(jobsScroller, BorderLayout.CENTER);
        jobsPanel.add(jobInfoPanel, BorderLayout.NORTH);
        
        viewPane.addTab(JAMS.i18n("Jobs"), jobsPanel);
        viewPane.addTab(JAMS.i18n("Workspaces"), wsPanel);
                
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        
        JSeparator sep1 = new JSeparator(JSeparator.HORIZONTAL);

        mainLayout.setHorizontalGroup(mainLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addGroup(mainLayout.createSequentialGroup()
                        .addComponent(serverField)
                        .addComponent(connectButton)
                )
                .addComponent(sep1)
                .addGroup(mainLayout.createSequentialGroup()
                        .addGroup(mainLayout.createParallelGroup()
                                .addComponent(viewPane)
                        )                        
                )
                .addComponent(statusLabel)
                
        );

        mainLayout.setVerticalGroup(mainLayout.createSequentialGroup()
                .addGroup(mainLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(serverField, 25, 45, 45)
                        .addGap(5,10,15)
                        .addComponent(connectButton, 25, 25, 25)
                         .addGap(5,10,15)
                )
                .addComponent(sep1,5,10,15)
                .addGroup(mainLayout.createParallelGroup()
                        .addGroup(mainLayout.createSequentialGroup()
                                .addComponent(viewPane)
                        )
                )
                .addComponent(statusLabel)
        );
        
        //Component comp = SwingUtilities.getRoot(mainPanel);                
        workspaceTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("empty")));
        jobsTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("empty")));      
        
        add(mainPanel);
        invalidate();
        pack();        
        
        client = connector.getClient();
        updateData();
    }
    
    private void viewStream(WorkspaceFileAssociation wfa){
        InputStream is = client.getFileController().getFileAsStream(wfa.getFile());
        ViewStreamDlg dlg = new ViewStreamDlg(BrowseJAMSCloudDlg.this, is, wfa.getFileName());
        dlg.setResizable(true);
        GUIHelper.centerOnParent(this, true);
        dlg.setVisible(true);        
    }
    
    private void viewInfoLog(Job job){
        WorkspaceFileAssociation wfa =job.getWorkspace().getFile("info.log");
        if (wfa != null){
            viewStream(wfa);
        }else{
            log.info(JAMS.i18n("Failed_to_show_info_log._The_file_is_not_existing!"));
        }
    }
    
    private void viewErrorLog(Job job){
        WorkspaceFileAssociation wfa =job.getWorkspace().getFile("error.log");
        if (wfa != null){
            viewStream(wfa);
        }else{
            log.info(JAMS.i18n("Failed_to_show_error_log._The_file_is_not_existing!"));
        }
    }
    
    private void deleteJob(Job job){
        if(client.getJobController().remove(job)!=null){
            updateView();
            log.info(JAMS.i18n("Job_deleted!"));
        }else{
            log.info(JAMS.i18n("Failed_to_delete_job!"));
        }
    }
    
    private void deleteAllJobs() {
        int result = JOptionPane.showConfirmDialog(this, 
                JAMS.i18n("Are_you_sure_to_delete_all_jobs"), 
                JAMS.i18n("Please_confirm!"), 
                JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION){
            client.getJobController().removeAll();
            updateView();
            log.info(JAMS.i18n("All_jobs_were_deleted"));
        }
        
    }
    
    private void deleteAllWorkspaces(){
        int result = JOptionPane.showConfirmDialog(this, 
                JAMS.i18n("Are_you_sure_to_delete_all_workspaces"), 
                JAMS.i18n("Please_confirm!"), 
                JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            client.getWorkspaceController().removeAll();
            updateView();
            log.info(JAMS.i18n("All_workspaces_were_deleted"));
        }
    }
    
    private void killJob(Job job){        
        if (client.getJobController().kill(job) != null){            
            log.info(JAMS.i18n("Job_with_id:%1_was_killed!")
                    .replace("%1", Integer.toString(job.getId()))
            );
            updateView();
        }else{
            log.info(JAMS.i18n("Failed_to_kill_job_with_id:%1!")
                    .replace("%1", Integer.toString(job.getId()))
            );
        }
    }
    
    private void updateJob(Job job){
        JobState state = client.getJobController().getState(job);
        if (state != null)
            jobsTree.updateNode(state.getJob());
    }
    
    private void initActions() {
        for (String url : connector.getRecentURLs()) {
            serverField.addItem(url);
        }

        deleteJobAction = new AbstractAction(JAMS.i18n("Delete")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                Job job = jobsTree.getSelectedJob();
                if (job != null){
                    deleteJob(job);
                }                
            }
        };
        
        deleteAllJobsAction = new AbstractAction(JAMS.i18n("Remove_all")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteAllJobs();
            }
        };

        killJobAction = new AbstractAction(JAMS.i18n("Kill")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                Job job = jobsTree.getSelectedJob();
                if (job != null)
                    killJob(job);
            }
        };
        
        updateJobAction = new AbstractAction(JAMS.i18n("Update")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                Job job = jobsTree.getSelectedJob();
                if (job != null){
                    updateJob(job);
                }
            }
        };
        
        showInfoLogAction = new AbstractAction(JAMS.i18n("View_Info_Log")){
            @Override
            public void actionPerformed(ActionEvent e) {
                Job job = jobsTree.getSelectedJob();
                if (job != null){
                    viewInfoLog(job);
                }
            }
        };
        
        showErrorLogAction = new AbstractAction(JAMS.i18n("View_Error_Log")){
            @Override
            public void actionPerformed(ActionEvent e) {
                Job job = jobsTree.getSelectedJob();
                if (job != null){
                    viewErrorLog(job);
                }
            }
        };

        startWorkspaceAction = new AbstractAction(JAMS.i18n("Start_Job")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                WorkspaceFileAssociation wfa = getSelectedFile();
                if (wfa != null){
                    if (wfa.getRole() == WorkspaceFileAssociation.ROLE_MODEL) {
                        startJob(wfa.getWorkspace(), wfa);
                    }
                }
            }
        };

        downloadWorkspaceAction = new AbstractAction(JAMS.i18n("Download")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                Workspace ws = null;
                Job job = jobsTree.getSelectedJob();
                if (job != null){
                    ws = job.getWorkspace();
                }else{
                    ws = workspaceTree.getSelectedWorkspace();
                }
                if (ws == null){
                    return;
                }
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = jfc.showSaveDialog(jfc);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File target = jfc.getSelectedFile();
                    connector.downloadWorkspace(ws, target);
                    log.info(JAMS.i18n("Download_complete"));
                }                                
            }
        };
        
        downloadFileAction = new AbstractAction("Download") {

            @Override
            public void actionPerformed(ActionEvent e) {
                WorkspaceFileAssociation wfa = getSelectedFile();
                if (wfa == null)
                    return;
                                
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = jfc.showSaveDialog(jfc);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File target = jfc.getSelectedFile();
                    connector.downloadFile(wfa, target);
                    log.info(JAMS.i18n("Download_complete"));
                }                                
            }
        };

        deleteWorkspaceAction = new AbstractAction(JAMS.i18n("Delete")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                Workspace ws = workspaceTree.getSelectedWorkspace();
                if (ws != null){
                    if (!ws.isReadOnly()){
                        client.getWorkspaceController().remove(ws);
                        updateView();
                        log.info(JAMS.i18n("Workspace_with_id:%1_was_deleted!")
                                .replace("%1", Integer.toString(ws.getId())));
                        return;
                    }
                }      
                log.severe(JAMS.i18n("Workspace_with_id:%1_was_not_deleted,_since it_is_read-only!")
                                .replace("%1", Integer.toString(ws.getId())));
            }
        };

        deleteAllWorkspacesAction = new AbstractAction(JAMS.i18n("Remove_all")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteAllWorkspaces();                
            }
        };
        
        connectAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        };

        uploadWorkspaceAction = new AbstractAction(JAMS.i18n("Upload_Workspace")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                UploadWorkspaceDlg dialog = new UploadWorkspaceDlg((Window)null, client);
                dialog.setModal(true);
                dialog.setVisible(true);                                
                if (dialog.getUploadSuccessful())
                    updateView();
            }
        };
        
        showFileProperties = new AbstractAction(JAMS.i18n("Properties")) {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                WorkspaceFileAssociation wfa = getSelectedFile();
                if (wfa == null)
                    return;
                FilePropertiesDlg fpDlg = new FilePropertiesDlg((Window)null);
                fpDlg.setFile(wfa);
                fpDlg.addPropertyChangeListener("ROLE", new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getSource() instanceof WorkspaceFileAssociation){
                            WorkspaceFileAssociation wfa = (WorkspaceFileAssociation)evt.getSource();
                            int role = (int)evt.getNewValue();
                            wfa.setRole(role);
                            client.getWorkspaceController().attachFile(wfa.getWorkspace(), wfa.getFile(), role, connector.getUser());
                        }
                    }
                });
                fpDlg.setVisible(true);
            }
        };

        final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm");
        workspaceTree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                Workspace ws = workspaceTree.getSelectedWorkspace();
                if (ws==null){
                    JAMSServerTreeNodes.SortedMutableTreeNode node = jobsTree.getSelectedNode();
                    if (node instanceof WFANode){
                        WFANode wfaNode = (WFANode)node;
                        ws = wfaNode.getWFA().getWorkspace();
                    }
                }
                if (ws != null){
                    wsName.setText(ws.getName());
                    wsCreation.setText(sdf.format(ws.getCreationDate()));
                    wsSize.setText(Utilities.formatSize(ws.getWorkspaceSize()));
                }
            }
        }
        );
        
        jobsTree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                Job job = jobsTree.getSelectedJob();                                
                if (job != null){
                    jobName.setText(job.getWorkspace().getName());
                    if (job.getStartTime() != null)
                        jobCreation.setText(sdf.format(job.getStartTime()));
                    else{
                        jobCreation.setText(JAMS.i18n("not_started"));
                    }                    
                    jobSize.setText(Utilities.formatSize(job.getWorkspace().getWorkspaceSize()));
                }else{
                    JAMSServerTreeNodes.SortedMutableTreeNode node = jobsTree.getSelectedNode();
                    if (node instanceof WFANode){
                        WFANode wfaNode = (WFANode)node;
                        //TODO .. 
                    }
                }
            }
        }
        );
        
        workspaceTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) {
                    return;
                }

                Object userObject = workspaceTree.getUserObjectAtLocation(e);

                if (userObject instanceof WorkspaceFileAssociation) {
                    WorkspaceFileAssociation wfa = ((WorkspaceFileAssociation) userObject);
                    viewStream(wfa);
                }

            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton()!=MouseEvent.BUTTON3){
                    return;
                }
                Object userObject = workspaceTree.getUserObjectAtLocation(e);

                JPopupMenu popup = new JPopupMenu();
                if (userObject instanceof User) {
                    popup.add(new JMenuItem(deleteAllWorkspacesAction));
                }
                if (userObject instanceof WorkspaceFileAssociation) {
                    WorkspaceFileAssociation wfa = (WorkspaceFileAssociation) userObject;

                    if (wfa.getRole() == WorkspaceFileAssociation.ROLE_MODEL && 
                        wfa.getWorkspace() != null && 
                        !wfa.getWorkspace().isReadOnly()) {
                        popup.add(new JMenuItem(startWorkspaceAction));
                    }
                    popup.add(new JMenuItem(downloadFileAction));
                    popup.add(new JSeparator());
                    popup.add(new JMenuItem(showFileProperties));
                } else if (userObject instanceof Workspace) {
                    popup.add(new JMenuItem(downloadWorkspaceAction));
                    Workspace ws = ((Workspace) userObject);
                    if (!ws.isReadOnly()) {
                        popup.add(new JMenuItem(deleteWorkspaceAction));
                    }
                }
                popup.show(e.getComponent(), e.getX(), e.getY());
            }

        });
        
        jobsTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ( e.getClickCount()!=2){
                    return;
                }
                Object userObject = jobsTree.getUserObjectAtLocation(e);
                if (userObject!=null && userObject instanceof WorkspaceFileAssociation) {
                    WorkspaceFileAssociation wfa = ((WorkspaceFileAssociation) userObject);
                    viewStream(wfa);
                }

            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if ( e.getButton() != MouseEvent.BUTTON3){
                    return;
                }
                Object userObject = jobsTree.getUserObjectAtLocation(e);
                if (userObject == null) {
                    return;
                }

                JPopupMenu popup = new JPopupMenu();
                if (userObject instanceof User) {
                    popup.add(new JMenuItem(deleteAllJobsAction));
                }
                if (userObject instanceof WorkspaceFileAssociation) {                     
                    popup.add(new JMenuItem(downloadFileAction));
                    popup.add(new JSeparator());
                    popup.add(new JMenuItem(showFileProperties));
                } else if (userObject instanceof Job) {
                    Job job = (Job)userObject;
                    if (job.getPID() > 0) {
                        popup.add(new JMenuItem(killJobAction));
                    }                    
                    popup.add(new JMenuItem(downloadWorkspaceAction));
                    popup.add(new JMenuItem(deleteJobAction));
                    popup.add(new JSeparator());
                    popup.add(new JMenuItem(showInfoLogAction));
                    popup.add(new JMenuItem(showErrorLogAction));
                }
                popup.show(e.getComponent(), e.getX(), e.getY());
            }            
        });
    }
    
    public boolean connect() {        
        if (connector == null){
            connector = new GraphicalClient(mainPanel ,p);
        }
        client = connector.reconnect();
        
        if (client != null){
            observable.deleteObservers();
            observable.addObserver(new Observer() {
                @Override
                public void update(Observable o, Object arg) {
                    statusLabel.setText(arg.toString());
                }
            });                      
            updateView();
            return true;
        }
        return false;
    }
    
    public boolean isConnected(){
        return connector != null && client != null;
    }
                          
    private void startJob(Workspace ws, WorkspaceFileAssociation f) {
        connector.startJob(ws, f);        
    }

    private WorkspaceFileAssociation getSelectedFile(){                
        Object o = null;
        
        if (jobsTree.isShowing()){
            o = jobsTree.getSelectedNode();
        }else if (workspaceTree.isShowing()){
            o = workspaceTree.getSelectedNode();
        }        
        if (o != null && o instanceof WFANode) {
            return ((WFANode)o).getWFA();
        }
        return null;
    }
    
    private void updateView() {
        if (connector.isConnected()){
            connectButton.setText(JAMS.i18n("Reconnect"));
        }else{
            connectButton.setText(JAMS.i18n("Connect"));
        }
        WorkerDlg dlg = new WorkerDlg(null, JAMS.i18n("Retrieving_data"));
        dlg.setInderminate(true);
        dlg.setTask(new Runnable() {

            @Override
            public void run() {
                updateData();
            }
        });
        dlg.execute();
    }

    private void updateData() {
        if (client == null) {
            return;
        }

        jobs = client.getJobController().find();
        //refresh states .. 
        for (Job job : jobs.getJobs()) {
            if (job.getPID() > 0) {
                if (!client.getJobController().getState(job).isActive()) {
                    job.setPID(-1);
                }
            }
        }
        
        try{
            workspaceTree.generateModel(client.getUser(), client.getWorkspaceController().findAll(null));
            jobsTree.generateModel(client.getUser(), client.getJobController().find());
        }catch(Throwable t){
            log.log(Level.SEVERE, t.getMessage(), t);
        }
    }
       
    public static void main(String[] args) {
        BrowseJAMSCloudDlg gui = new BrowseJAMSCloudDlg(null, null);
        gui.init();
        gui.connectAction.actionPerformed(null);
        GUIHelper.centerOnScreen(gui, true);
        gui.setVisible(true);
    }
}
