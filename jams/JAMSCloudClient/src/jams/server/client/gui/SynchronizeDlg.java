/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.server.client.gui;

import jams.ErrorCatchingRunnable;
import jams.JAMS;
import jams.SystemProperties;
import jams.gui.ObserverWorkerDlg;
import jams.gui.WorkerDlg;
import jams.gui.tools.GUIHelper;
import jams.server.client.Controller;
import jams.server.client.sync.DirectorySync;
import jams.server.client.sync.FileSync;
import jams.server.client.WorkspaceController;
import jams.server.client.sync.SyncTable;
import jams.server.entities.Job;
import jams.server.entities.Jobs;
import jams.server.entities.Workspace;
import jams.tools.LogTools.ObservableLogHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

/**
 *
 * @author christian
 */
public class SynchronizeDlg extends JDialog {
    static Logger log = Logger.getLogger(SynchronizeDlg.class.getName());
    
    /**
     *
     */
    protected Font defaultFont = new Font("Times New Roman", Font.PLAIN, 10);

    /**
     *
     */
    protected SyncTable syncTable = null;
    
    /**
     *
     */
    protected JButton okButton = new JButton(JAMS.i18n("Synchronize"));

    /**
     *
     */
    protected JButton cancelButton = new JButton(JAMS.i18n("Cancel"));

    /**
     *
     */
    protected JCheckBox showAllJobs = new JCheckBox("show all jobs from user");
    
    /**
     *
     */
    protected jams.workspace.Workspace localWorkspace = null;

    /**
     *
     */
    protected Workspace remoteWs = null;

    /**
     *
     */
    protected Component tableSyncModeCells[];
    
    /**
     *
     */
    protected JLabel checkLabel = new JLabel("<html>"+JAMS.i18n("check")+"</html>");

    /**
     *
     */
    protected JComboBox<Job> jobChooser = new JComboBox();

    /**
     *
     */
    protected JButton syncNewOnly = new JButton(JAMS.i18n("only_new")),
 
    /**
     *
     */
    syncAll     = new JButton(JAMS.i18n("all")),

    /**
     *
     */
    syncNothing = new JButton(JAMS.i18n("none")),

    /**
     *
     */
    invertSelection = new JButton(JAMS.i18n("invert_selection"));
        
    /**
     *
     */
    protected ObserverWorkerDlg syncWorkspaceWorker = new ObserverWorkerDlg(
            new WorkerDlg(SynchronizeDlg.this, JAMS.i18n("Synchronizing_Workspace")));
    
    /**
     *
     */
    protected ObservableLogHandler observable = new ObservableLogHandler(new Logger[]{Logger.getLogger(Controller.class.getName())});
    
    /**
     *
     */
    protected Jobs jobs = null;

    /**
     *
     */
    protected Job selectedJob = null;

    /**
     *
     */
    protected int localWorkspaceID = -1;
    
    /**
     *
     */
    protected JAMSCloudGraphicalController connector = null;

    /**
     *
     */
    protected SystemProperties p;
    
    /**
     *
     * @param w
     * @param localWorkspace
     * @param p
     * @throws IOException
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public SynchronizeDlg(Window w, jams.workspace.Workspace localWorkspace, SystemProperties p) throws IOException{
        super(w,"Synchronize Workspace");
        this.p = p;
        connector = JAMSCloudGraphicalController.createInstance(p);          
        this.localWorkspace = localWorkspace;
                                
        GUIHelper.centerOnParent(this, rootPaneCheckingEnabled);                      
        init();        
        GUIHelper.centerOnParent(this, rootPaneCheckingEnabled);        
    }
        
    @Override
    public void setVisible(boolean flag){
        if (flag == true){
            try{                
                if (!connector.isConnected()){
                    if ( connector.reconnect() == null )
                        return;
                }
            }catch(IOException ioe){
                log.log(Level.SEVERE, "Unable to connect", ioe);
                return;
            }
        }else{
            
        }                
        super.setVisible(flag);
    }
    
    private void setupSelectionButton(JButton bn){
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e){
                JButton bn = (JButton)e.getSource();
                String text = bn.getClientProperty("text").toString();
                bn.setText("<html><u>"+text+"</u></html>");
                bn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e){
                JButton bn = (JButton)e.getSource();
                String text = bn.getClientProperty("text").toString();
                bn.setText("<html>"+text+"</html>");
            }
        };
        
        bn.setBorder(null); 
        bn.setOpaque(false);
        bn.setContentAreaFilled(false);
        bn.setForeground(Color.blue);
        bn.setFont(defaultFont);           
        bn.addMouseListener(ma);
        bn.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        bn.putClientProperty("text", bn.getText());
    }
    
    private void init() throws IOException{                
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(jobChooser, BorderLayout.CENTER);
        northPanel.add(showAllJobs, BorderLayout.EAST);
        
        localWorkspaceID = localWorkspace.getID();
        if (localWorkspaceID == -1){
            showAllJobs.setSelected(true);
        }
        jobs = connector.getClient().jobs().find();
        for (Job job : jobs.getJobs()){
            Workspace ancestor = job.getWorkspace().getAncestor();
            if (ancestor!=null && ancestor.getName().equals(localWorkspace.getTitle())){
                jobChooser.addItem(job);
            }
        }
        jobChooser.setBorder(BorderFactory.createTitledBorder("Available Jobs"));
        jobChooser.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (jobChooser.getSelectedItem()!=null){
                    selectedJob = (Job)jobChooser.getSelectedItem();
                    WorkerDlg worker = new WorkerDlg(SynchronizeDlg.this, "Loading jobs .. ");
                    worker.setTask(new SwingWorker() {

                        @Override
                        protected Object doInBackground() throws Exception {
                            try{
                                syncTable.setServerWorkspace(selectedJob.getWorkspace());
                            }catch(Throwable t){
                                t.printStackTrace();
                            }
                            return null;
                        }
                    });
                    worker.execute();
                }
                
            }
        });
                
        showAllJobs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Job job : jobs.getJobs()) {
                    Workspace ancestor = job.getWorkspace().getAncestor();
                    if (showAllJobs.isSelected() || 
                            (ancestor != null && ancestor.getId() == localWorkspaceID)) {
                        jobChooser.addItem(job);
                    }
                }
            }
        });
        
        syncTable = new SyncTable(connector.getClient(), defaultFont);
        syncTable.setLocalWorkspace(localWorkspace.getDirectory());
        
        if (jobChooser.getItemCount()>0)
            jobChooser.setSelectedItem(jobChooser.getItemAt(0));
        
        JScrollPane tableScroll = new JScrollPane(syncTable);
        mainPanel.add(tableScroll, BorderLayout.CENTER);

        
        setupSelectionButton(syncNewOnly);
        setupSelectionButton(syncAll);
        setupSelectionButton(syncNothing);
        setupSelectionButton(invertSelection);
        checkLabel.setFont(defaultFont);
        
        JPanel buttonPanel = new JPanel();
        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
                
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addGap(5, 10, 15)
                        .addComponent(checkLabel)
                        .addComponent(syncAll)
                        .addComponent(syncNothing)
                        .addComponent(syncNewOnly)
                        .addComponent(invertSelection)
                        .addGap(0, 1000, 1000)
                )
                .addGap(25, 25, 25)
                .addGroup(layout.createSequentialGroup()
                        .addGap(0, 1000, 1000)
                        .addComponent(okButton)
                        .addGap(25, 25, 25)
                        .addComponent(cancelButton)
                        .addContainerGap()
                )
        );
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(checkLabel)
                        .addComponent(syncAll)
                        .addComponent(syncNothing)
                        .addComponent(syncNewOnly)
                        .addComponent(invertSelection)
                )
                .addGroup(layout.createParallelGroup()
                        .addComponent(okButton)
                        .addComponent(cancelButton)
                        
                )
                .addContainerGap()
        );
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(northPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        this.add(mainPanel);
        
        initActions();
        
        setSize(new Dimension(640, 480));
        invalidate();
        pack();
    }
    
    private void initActions() {
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                synchronize();
                SynchronizeDlg.this.setVisible(false);
            }
        });

        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {                
                SynchronizeDlg.this.setVisible(false);
            }
        });
        
        syncNewOnly.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                FileSync fs = syncTable.getModel().getRoot();
                
                fs.applySyncFilter(new FileSync.SyncFilter() {

                    @Override
                    public boolean isFiltered(FileSync fs) {
                        return !fs.isExisting();
                    }

                    @Override
                    public boolean isFiltered(DirectorySync ds) {
                        return !ds.isExisting();
                    }
                });
                
                syncTable.getModel().fireTableDataChanged();
            }
        });
        
        syncAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                FileSync fs = syncTable.getModel().getRoot();
                
                fs.applySyncFilter(new FileSync.SyncFilter() {

                    @Override
                    public boolean isFiltered(FileSync fs) {
                        return true;
                    }

                    @Override
                    public boolean isFiltered(DirectorySync ds) {
                        return true;
                    }
                });
                syncTable.getModel().fireTableDataChanged();
            }
        });
        
        syncNothing.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                FileSync fs = syncTable.getModel().getRoot();
                
                fs.applySyncFilter(new FileSync.SyncFilter() {

                    @Override
                    public boolean isFiltered(FileSync fs) {
                        return false;
                    }

                    @Override
                    public boolean isFiltered(DirectorySync ds) {
                        return false;
                    }
                });
                syncTable.getModel().fireTableDataChanged();
            }
        });
        
        invertSelection.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                FileSync fs = syncTable.getModel().getRoot();
                
                fs.applySyncFilter(new FileSync.SyncFilter() {

                    @Override
                    public boolean isFiltered(FileSync fs) {
                        return !fs.isDoSync();
                    }

                    @Override
                    public boolean isFiltered(DirectorySync ds) {
                        return !ds.isDoSync();
                    }
                });
                syncTable.getModel().fireTableDataChanged();
            }
        });
    }

    private void synchronize() {
        syncWorkspaceWorker.getWorkerDlg().setInderminate(true);
        syncWorkspaceWorker.getWorkerDlg().setTask(new ErrorCatchingRunnable() {

            @Override
            public void safeRun() {
                FileSync root = syncTable.getModel().getRoot();
                
                WorkspaceController wc = connector.getClient().workspaces();
                
                observable.addObserver(syncWorkspaceWorker);
                if (root instanceof DirectorySync){
                    wc.synchronizeWorkspace((DirectorySync)root);
                }
                observable.deleteObserver(syncWorkspaceWorker);                
            }
        });        
        syncWorkspaceWorker.getWorkerDlg().execute();
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        /*Controller client = new Controller("http://localhost:8080/jams-server/webresources");
        client.connect("Blubb", "test");

        JAMSWorkspace wslocal = new JAMSWorkspace(new File("E:/ModelData/JAMS-Gehlberg"), new StandardRuntime(JAMSProperties.createProperties()));
        wslocal.loadConfig();
        
        SynchronizeDlg dlg = new SynchronizeDlg((Window) null, client, wslocal);
        dlg.setSize(new Dimension(640, 480));
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setVisible(true);*/

    }
}
