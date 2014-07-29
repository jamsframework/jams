/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.server.client.gui;

import jams.SystemProperties;
import jams.gui.ObserverWorkerDlg;
import jams.gui.WorkerDlg;
import jams.server.client.Controller;
import jams.server.client.HTTPClient;
import jams.server.client.ObservableLogHandler;
import jams.server.client.WorkspaceController;
import jams.server.entities.Job;
import jams.server.entities.Workspace;
import jams.server.entities.WorkspaceFileAssociation;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 *
 * @author christian
 */
public class GraphicalClient extends Observable {
    private static final Logger log = Logger.getLogger( GraphicalClient.class.getName() );
    
    //saved data
    private String[] recentUrls = {"http://localhost:8080/jams-server/webresources"};
    private String user = "Blubb", pw = "test";
    private boolean saveAccount = true;
    private Controller client = null;
    int debugLevel = 3;
    
    SystemProperties p = null;

    ObserverWorkerDlg worker = new ObserverWorkerDlg(null, "Uploading Workspace");
    ObservableLogHandler observable = new ObservableLogHandler(
            new Logger[]{
                Logger.getLogger(Controller.class.getName()),
            });
            
    static Handler defaultMsgHandler = null;
        
    public GraphicalClient(Component parent, SystemProperties p) {  
        MsgBoxLogHandler.registerLogger(Logger.getLogger( GraphicalClient.class.getName()));
        MsgBoxLogHandler.registerLogger(Logger.getLogger(Controller.class.getName()));
        //MsgBoxLogHandler.registerLogger(Logger.getLogger(HTTPClient.class.getName()));
        //init logs
        observable.deleteObservers();
        observable.getHandler().setFilter(new Filter() {

            @Override
            public boolean isLoggable(LogRecord record) {
                return record.getLevel() == Level.FINE || 
                       record.getLevel() == Level.INFO;
            }
        });        
        observable.addObserver(worker);
                                                
        if (p != null) {
            user = p.getProperty("jams_server_user");
            pw = p.getProperty("jams_server_pw");
            String s = p.getProperty("jams_server_save_account");
            if (s != null) {
                try {
                    saveAccount = Boolean.parseBoolean(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            s = p.getProperty("jams_server_recent_urls");
            if (s != null) {
                recentUrls = s.split(";");
            }
        }
        this.p = p;
    }

    public String[] getRecentURLs() {
        return recentUrls;
    }

    public String getUser() {
        return user;
    }
    
   public boolean isConnected(){
       return client != null;
   }

    public Controller getClient() {
        if (client == null) {
            return reconnect();
        }
        return client;
    }

    public Controller reconnect() {        
        JLabel jServerName = new JLabel("Server");
        JComboBox serverUrls = new JComboBox(recentUrls);
        serverUrls.setEditable(true);
        JLabel jUserName = new JLabel("User Name");
        JTextField userName = new JTextField();
        JLabel jPassword = new JLabel("Password");
        JTextField password = new JPasswordField();
        JLabel jCredentials = new JLabel("Save Credentials");
        JCheckBox cred = new JCheckBox();
        cred.setSelected(saveAccount);
        if (saveAccount) {
            userName.setText(user);
            password.setText(pw);
        }
        Object[] ob = {jServerName, serverUrls, jUserName, userName, jPassword, password, jCredentials, cred};
        int result = JOptionPane.showConfirmDialog(null, ob, "Please input serverUrl/username/password", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String userNameValue = userName.getText();
            String passwordValue = password.getText();
            saveAccount = cred.isSelected();
            if (saveAccount){                
                user = userName.getText();
                pw = password.getText();
            }
            if (p != null){
                p.setProperty("jams_server_user", user);
                p.setProperty("jams_server_pw", pw);
                p.setProperty("jams_server_save_account", Boolean.toString(saveAccount));
                
                String recentUrls = "";
                for (int i=0;i<serverUrls.getModel().getSize();i++){
                    if (i >= 9)
                        break;
                    recentUrls += serverUrls.getModel().getElementAt(i) + ";";
                }
                recentUrls = serverUrls.getSelectedItem().toString() + ";" + recentUrls;
                p.setProperty("jams_server_recent_urls", recentUrls);
                try{                    
                    p.save();
                }catch(IOException ioe){
                    log.log(Level.SEVERE, ioe.toString(), ioe);
                }
            }            
            
            client = new Controller(new HTTPClient(), serverUrls.getSelectedItem().toString() );
            if ( !client.connect(userNameValue, passwordValue) ){                
                client = null;
            }
            return client;
        }else{
            return client = null;
        }
    }
    
    private class DownloadRunnable implements Runnable {

        Workspace ws;
        WorkspaceFileAssociation wfa;
        File target;

        public DownloadRunnable(Workspace ws, File target) {
            this.ws = ws;
            this.target = target;
            this.wfa = null;
        }

        public DownloadRunnable(WorkspaceFileAssociation wfa, File target) {
            this.wfa = wfa;
            this.ws = null;
            this.target = target;
        }

        @Override
        public void run() {
            if (ws != null) {
                client.getWorkspaceController().downloadWorkspace(target, ws);
            } else if (wfa != null) {
                client.getWorkspaceController().downloadFile(target, wfa);
            }
        }
    }
    
    public void downloadWorkspace(Workspace ws, File target) {
        WorkerDlg dlg = new WorkerDlg(null, "Download Workspace ... ");
        dlg.setInderminate(true);
        dlg.setTask(new DownloadRunnable(ws, target));
        dlg.execute();
    }
    
    public void downloadFile(WorkspaceFileAssociation wfa, File target) {
        WorkerDlg dlg = new WorkerDlg(null, "Download file ... ");
        dlg.setInderminate(true);
        dlg.setTask(new DownloadRunnable(wfa, target));
        dlg.execute();
    }
    
    private class UploadWorkspaceRunnable implements Runnable{
        private final int id;
        private final String title;
        private final String fileFilter;
        private final File workspaceDirectory;
        private final File[] compLibFile;
        private final File uiLibFile;
        private Workspace ws;
        
        public UploadWorkspaceRunnable(int id, String title, File wsDirectory,File[] compLibFile, File uiLibFile, String fileFilter){
            this.id = id;
            this.title = title;
            this.workspaceDirectory = wsDirectory;
            this.compLibFile = compLibFile;
            this.uiLibFile = uiLibFile;
            this.fileFilter = fileFilter;
        }
        
        @Override
        public void run() {
            try{
                WorkspaceController wsClient = client.getWorkspaceController();
                ws = wsClient.uploadWorkspace(id, title, workspaceDirectory, compLibFile, uiLibFile, fileFilter);
            }catch(Throwable t){
                t.printStackTrace();
            }
        }
        
        public Workspace getWorkspace(){
            return ws;
        }
    }
    
    public Workspace uploadWorkspace(
            int id,
            String title,
            File workspaceDirectory,
            File[] compLibFile,
            File uiLibFile,
            String fileFilter) {

        if (client == null) {
            return null;
        }
        
        if (!workspaceDirectory.exists() || !workspaceDirectory.isDirectory()) {
            log.info("Unable to upload workspace! Paths are incorrect!");
            return null;
        }

        if (!uiLibFile.exists() || !uiLibFile.isFile()) {
            log.info("Unable to upload workspace! Paths are incorrect!");
            return null;
        }
         
        observable.deleteObserver(worker);
        observable.addObserver(worker);
        worker.setInderminate(true);
        worker.setModal(true);
        UploadWorkspaceRunnable uploadWsTask = new UploadWorkspaceRunnable(id, title, workspaceDirectory, compLibFile, uiLibFile, fileFilter);
        worker.setTask(uploadWsTask);
        worker.execute();

        return uploadWsTask.getWorkspace();
    }
            
    private class StartJobRunnable implements Runnable{
        private Workspace ws = null;
        private WorkspaceFileAssociation wfa =  null;
        private Job job = null;
        
        public StartJobRunnable(Workspace ws, WorkspaceFileAssociation wfa){
            this.ws = ws;
            this.wfa = wfa;
        }
        
        @Override
        public void run() {
            try {
                job = client.getJobController().create(ws, wfa);
            } catch (Throwable t) {
                log.log(Level.SEVERE, t.toString(), t);
            }
            if (job != null && job.getId() != -1) {
                log.log(Level.INFO, "Job was started successfully! It has ID = " + job.getId() + ". You can check the execution state of this job in Dialog \"Remote Control\"");
            } else {
                log.log(Level.INFO, "Failed to start job!");
            }
        }
        
        public Job getResult(){
            return job;
        }
    }
    
    public Job startJob(Workspace ws, File modelFile){
        if (ws == null)
            return null;
                
        WorkspaceFileAssociation model = null;
        for (WorkspaceFileAssociation wfa : ws.getFiles()){
            if (wfa.getPath().endsWith(modelFile.getName())){
                 model = wfa;
            }
        }
        
        return startJob(ws, model);
    }
    
    public Job startJob(Workspace ws, WorkspaceFileAssociation model){
        if (ws == null)
            return null;
                                
        if (model == null)
            return null;
        
        observable.deleteObserver(worker);
        observable.addObserver(worker);
        worker.setInderminate(true);
        worker.setModal(true);
        StartJobRunnable task = new StartJobRunnable(ws, model);
        worker.setTask(task);
        worker.execute();        
        return task.getResult();
    }
}
