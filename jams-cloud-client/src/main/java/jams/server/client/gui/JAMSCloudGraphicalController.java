/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.server.client.gui;

import jams.ErrorCatchingRunnable;
import jams.JAMS;
import jams.JAMSLogging;
import jams.SystemProperties;
import jams.gui.CancelableWorkerDlg;
import jams.gui.ObserverWorkerDlg;
import jams.gui.WorkerDlg;
import jams.gui.tools.GUIState;
import jams.server.client.Controller;
import jams.server.client.DownloadListener;
import jams.server.client.FileController;
import jams.server.client.JAMSWorkspaceUploader;
import jams.server.client.JobController;
import jams.server.client.UserController;
import jams.server.client.WorkspaceController;
import jams.server.client.error.DefaultFileUploadErrorHandling;
import jams.server.entities.Job;
import jams.server.entities.User;
import jams.server.entities.Workspace;
import jams.server.entities.WorkspaceFileAssociation;
import jams.tools.LogTools;
import jams.tools.LogTools.ObservableLogHandler;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ResponseProcessingException;

/**
 *
 * @author christian
 */
public class JAMSCloudGraphicalController extends Observable {

    private static final Logger log = Logger.getLogger(JAMSCloudGraphicalController.class.getName());

    //saved data
    private String[] recentUrls = {"http://localhost:8080/jamscloud/webresources"};
    private String user = "jams", pw = "jamscloud", serverUrl = "";
    private boolean saveAccount = true;
    private SystemProperties p;
    private Controller client;
    private Window parent;

    private ObserverWorkerDlg worker;

    private ObservableLogHandler observable = new ObservableLogHandler(new Logger[]{
        Logger.getLogger(Controller.class.getName()),
        Logger.getLogger(FileController.class.getName()),
        Logger.getLogger(WorkspaceController.class.getName()),
        Logger.getLogger(JobController.class.getName()),
        Logger.getLogger(UserController.class.getName())});

    private static JAMSCloudGraphicalController instance = null;

    public enum JAMSCloudEvents {

        CONNECT, CONNECTING, DISCONNECT
    };

    private JAMSCloudGraphicalController(Window parent, SystemProperties p) {
        this.p = p;
        this.parent = parent;
        JAMSLogging.registerLogger(JAMSLogging.LogOption.Show,
                Logger.getLogger(JAMSCloudGraphicalController.class.getName()));

        worker = new ObserverWorkerDlg(new CancelableWorkerDlg(
                new WorkerDlg(parent, "Uploading Workspace")).getWorkerDlg());

        //init logs
        observable.deleteObservers();
        observable.getHandler().setFilter(new Filter() {

            @Override
            public boolean isLoggable(LogRecord record) {
                return record.getLevel() == Level.FINE
                        || record.getLevel() == Level.INFO;
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
    }

    /**
     *
     * @param parent
     * @param p
     * @return
     */
    public static JAMSCloudGraphicalController createInstance(Window parent, SystemProperties p) {
        if (instance == null) {
            instance = new JAMSCloudGraphicalController(parent, p);
        }
        return instance;
    }

    /**
     *
     * @return
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     *
     * @return
     */
    public String[] getRecentUrls() {
        return recentUrls;
    }

    /**
     *
     * @return
     */
    public String getUser() {
        return user;
    }

    /**
     *
     * @return
     */
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    /**
     *
     * @return
     */
    public Controller getClient() {
        return client;
    }

    /**
     *
     */
    public void disconnect() {
        client.close();
        client = null;
        setChanged();
        notifyObservers(JAMSCloudEvents.DISCONNECT);
    }

    /**
     *
     * @return @throws IOException
     */
    public Controller connect() throws IOException {
        if (!isConnected()) {
            return reconnect();
        }
        return client;
    }

    private boolean showReconnectDialog() {
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
        int result = JOptionPane.showConfirmDialog(parent, ob, "Please input server URL / username / password", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            return false;
        }
        user = userName.getText();
        pw = password.getText();
        serverUrl = serverUrls.getSelectedItem().toString();
        saveAccount = cred.isSelected();

        if (p != null) {
            if (saveAccount) {
                p.setProperty("jams_server_user", user);
                p.setProperty("jams_server_pw", pw);
            }
            p.setProperty("jams_server_save_account", Boolean.toString(saveAccount));

            String recentUrls = "";
            String curS = serverUrls.getSelectedItem().toString();
            for (int i = 0; i < serverUrls.getModel().getSize(); i++) {
                if (i >= 9) {
                    break;
                }
                String s = serverUrls.getModel().getElementAt(i).toString();
                if (s.compareTo(curS) != 0) {
                    recentUrls += s + ";";
                }
            }
            recentUrls = curS + ";" + recentUrls;
            p.setProperty("jams_server_recent_urls", recentUrls);
            ((DefaultComboBoxModel) serverUrls.getModel()).insertElementAt(curS, 0);
            try {
                p.save();
            } catch (IOException ioe) {
                log.log(Level.SEVERE, ioe.toString(), ioe);
            }
        }
        return true;
    }

    /**
     *
     * @return @throws IOException
     */
    public Controller reconnect() throws IOException {
        try {
            if (isConnected()) {
                disconnect();
            }
        } catch (Throwable t) {
            LogTools.log(getClass(), Level.WARNING, t, t.toString());
        }
        this.setChanged();
        this.notifyObservers(JAMSCloudEvents.CONNECTING);
        if (showReconnectDialog()) {
            try {
                client = new Controller(serverUrl, user, pw);
                
//                User newUser = new User(28, "ysee", "ylephilippe");
//                newUser.setEmail("ysee.le-philippe@inrae.fr");
//                newUser.setName("Ysee Lephilippe");
//                newUser.setAdmin(0);
//                client.users().createUser(newUser);                
//                System.out.println(newUser);
                
            } catch (ResponseProcessingException pe) {
                if (pe.getResponse().getStatus() == 403) {
                    LogTools.log(getClass(), Level.SEVERE, pe, "You provided a wrong username and/or password or you are not allowed to access this server");
                    if (pe.getResponse().getStatus() == 404) {
                        LogTools.log(getClass(), Level.SEVERE, pe, "The url %1 was not found!".replace("%1", serverUrl));
                    }
                } else {
                    LogTools.log(getClass(), Level.SEVERE, pe, pe.getResponse().getStatusInfo().getReasonPhrase());
                }
            } catch (ProcessingException pe) {
                //TODO: need function processingException to Log
                if (pe.toString().contains("Connection refused")) {
                    LogTools.log(getClass(), Level.SEVERE, pe, "Connection was refused.");
                } else {
                    LogTools.log(getClass(), Level.SEVERE, pe, pe.getMessage().isEmpty() ? pe.toString() : pe.getMessage());
                }
                this.setChanged();
                this.notifyObservers(JAMSCloudEvents.DISCONNECT);
            }
            this.setChanged();
            this.notifyObservers(JAMSCloudEvents.CONNECT);
            return client;
        } else {
            this.setChanged();
            this.notifyObservers(JAMSCloudEvents.DISCONNECT);
            return null;
            //throw new IOException("User canceled connection!");
        }
    }

    private class DownloadRunnable extends ErrorCatchingRunnable {

        Workspace ws;
        WorkspaceFileAssociation wfa;
        File target;
        DownloadProgress listener;
        boolean targetIsFile = false;
        boolean completed = false;

        public DownloadRunnable(Workspace ws, File target, DownloadProgress listener) {
            this.ws = ws;
            this.target = target;
            this.wfa = null;
            this.listener = listener;
        }

        public DownloadRunnable(WorkspaceFileAssociation wfa, File target, DownloadProgress listener) {
            this.wfa = wfa;
            this.ws = null;
            this.target = target;
            this.listener = listener;
        }

        @Override
        public void safeRun() throws Exception {
            try {
                if (ws != null) {
                    client.workspaces().downloadWorkspace(target, ws, listener);
                } else if (wfa != null && targetIsFile) {
                    client.workspaces().downloadFileTo(target, wfa, listener);
                } else if (wfa != null) {
                    client.workspaces().downloadFile(target, wfa, listener);
                }
                completed = true;
            } catch (IOException ioe) {
                if (listener.isCancelled()) {
                    log.info("Download cancelled");
                    return;
                }
                throw ioe;
            }
        }
    }

    /**
     * shows the download progress (amount, rate, remaining time) in a
     * WorkerDlg
     */
    private static class DownloadProgress implements DownloadListener {

        private static final long UPDATE_INTERVAL = 250_000_000L; // ns

        private final WorkerDlg dlg;
        private final JButton cancelButton = new JButton(JAMS.i18n("Cancel"));
        private long start = -1;
        private long lastUpdate = 0;
        private volatile boolean cancelled = false;
        private Runnable abort = null;

        // longest status line, used to fix the dialog width so it does not jump
        private static final String WIDEST_MESSAGE = "1023.9 MB of 1023.9 MB - 1023.9 MB/s - 99 h 59 min left";

        DownloadProgress(WorkerDlg dlg) {
            this.dlg = dlg;
            cancelButton.addActionListener(e -> cancel());
            JPanel panel = dlg.getMainPanel();
            panel.add(cancelButton, BorderLayout.SOUTH);
            Dimension size = panel.getPreferredSize();
            int width = panel.getFontMetrics(new JLabel().getFont()).stringWidth(WIDEST_MESSAGE) + 20;
            panel.setPreferredSize(new Dimension(Math.max(size.width, width), size.height));
            dlg.pack();
        }

        boolean isCancelled() {
            return cancelled;
        }

        private void cancel() {
            Runnable a;
            synchronized (this) {
                if (cancelled) {
                    return;
                }
                cancelled = true;
                a = abort;
            }
            cancelButton.setEnabled(false);
            dlg.setMessage("Cancelling ...");
            if (a != null) {
                new Thread(a, "download-abort").start();
            }
        }

        @Override
        public void setAbortHandler(Runnable abort) {
            boolean abortNow;
            synchronized (this) {
                this.abort = abort;
                abortNow = cancelled;
            }
            if (abortNow) {
                abort.run();
            }
        }

        @Override
        public void progress(long bytesRead, long totalBytes) {
            if (cancelled) {
                return;
            }
            long now = System.nanoTime();
            if (start < 0) {
                start = now;
            } else if (now - lastUpdate < UPDATE_INTERVAL && bytesRead != totalBytes) {
                return;
            }
            lastUpdate = now;

            double seconds = (now - start) / 1e9;
            double rate = seconds > 1 ? bytesRead / seconds : 0;
            StringBuilder text = new StringBuilder(formatBytes(bytesRead));
            if (totalBytes > 0) {
                text.append(" of ").append(formatBytes(totalBytes));
            }
            if (rate > 0) {
                text.append(" - ").append(formatBytes((long) rate)).append("/s");
                if (totalBytes > bytesRead) {
                    text.append(" - ").append(formatDuration((totalBytes - bytesRead) / rate)).append(" left");
                }
            }
            String message = text.toString();
            int permille = totalBytes > 0 ? (int) Math.min(1000, bytesRead * 1000 / totalBytes) : -1;

            SwingUtilities.invokeLater(() -> {
                if (permille >= 0) {
                    dlg.setInderminate(false);
                    dlg.setProgressMax(1000);
                    dlg.setProgress(permille);
                }
                dlg.setMessage(message);
            });
        }

        @Override
        public void unpacking() {
            SwingUtilities.invokeLater(() -> {
                cancelButton.setEnabled(false);
                dlg.setInderminate(true);
                dlg.setMessage("Unpacking ...");
            });
        }

        private static String formatBytes(long bytes) {
            if (bytes < 1024) {
                return bytes + " B";
            }
            String[] units = {"KB", "MB", "GB", "TB"};
            double value = bytes;
            int unit = -1;
            while (value >= 1024 && unit < units.length - 1) {
                value /= 1024;
                unit++;
            }
            return String.format("%.1f %s", value, units[unit]);
        }

        private static String formatDuration(double seconds) {
            long s = Math.round(seconds);
            if (s < 60) {
                return s + " s";
            } else if (s < 3600) {
                return (s / 60) + " min " + (s % 60) + " s";
            }
            return (s / 3600) + " h " + (s % 3600 / 60) + " min";
        }
    }

    /**
     *
     * @param ws
     * @param target
     */
    public void downloadWorkspace(Workspace ws, File target) {
        downloadWorkspace(ws, target, GUIState.getMainWindow());
    }

    /**
     *
     * @param ws
     * @param target
     * @param owner window that owns the progress dialog
     */
    public boolean downloadWorkspace(Workspace ws, File target, Window owner) {
        WorkerDlg dlg = new WorkerDlg(owner, "Download Workspace ... ", "Server is packing the workspace ...");
        dlg.setInderminate(true);
        DownloadRunnable task = new DownloadRunnable(ws, target, new DownloadProgress(dlg));
        dlg.setTask(task);
        dlg.execute();
        return task.completed;
    }

    /**
     *
     * @param wfa
     * @param target
     */
    public void downloadFile(WorkspaceFileAssociation wfa, File target) {
        downloadFile(wfa, target, GUIState.getMainWindow());
    }

    /**
     *
     * @param wfa
     * @param target
     * @param owner window that owns the progress dialog
     */
    public boolean downloadFile(WorkspaceFileAssociation wfa, File target, Window owner) {
        return downloadFile(wfa, target, false, owner);
    }

    /**
     *
     * @param wfa
     * @param file the file to be written
     * @param owner window that owns the progress dialog
     * @return true if the download has been completed
     */
    public boolean downloadFileTo(WorkspaceFileAssociation wfa, File file, Window owner) {
        return downloadFile(wfa, file, true, owner);
    }

    private boolean downloadFile(WorkspaceFileAssociation wfa, File target, boolean targetIsFile, Window owner) {
        WorkerDlg dlg = new WorkerDlg(owner, "Download file ... ", "Waiting for server ...");
        dlg.setInderminate(true);
        DownloadRunnable task = new DownloadRunnable(wfa, target, new DownloadProgress(dlg));
        task.targetIsFile = targetIsFile;
        dlg.setTask(task);
        dlg.execute();
        return task.completed;
    }

    private class UploadWorkspaceRunnable extends ErrorCatchingRunnable {

        private final jams.workspace.Workspace jamsWorkspace;
        private final String fileFilter;
        private final File[] compLibFile;
        private final File rtLibFile;
        private Workspace ws;

        public UploadWorkspaceRunnable(jams.workspace.Workspace jamsWorkspace, File[] compLibFile, File rtLibFile, String fileFilter) {
            this.jamsWorkspace = jamsWorkspace;
            this.compLibFile = compLibFile;
            this.rtLibFile = rtLibFile;
            this.fileFilter = fileFilter;
        }

        @Override
        public void safeRun() throws Exception {
            JAMSWorkspaceUploader uploader = new JAMSWorkspaceUploader(client);
            ws = uploader.uploadWorkspace(jamsWorkspace, compLibFile, rtLibFile, fileFilter, new DefaultFileUploadErrorHandling());
        }

        public Workspace getWorkspace() {
            return ws;
        }
    }

    /**
     *
     * @param jamsWorkspace
     * @param compLibFile
     * @param rtLibFile
     * @param fileFilter
     * @return
     * @throws IOException
     */
    public Workspace uploadWorkspace(
            jams.workspace.Workspace jamsWorkspace,
            File[] compLibFile, File rtLibFile,
            String fileFilter) throws IOException {

        if (!isConnected()) {
            throw new IOException("Not connected to server!");
        }

        if (!rtLibFile.exists() || !rtLibFile.isDirectory()) {
            throw new IOException("Unable to upload workspace! Runtime libraries path is incorrect!");
        }

        observable.deleteObserver(worker);
        observable.addObserver(worker);
        worker.getWorkerDlg().setInderminate(true);
        worker.getWorkerDlg().setModal(true);
        UploadWorkspaceRunnable uploadWsTask = new UploadWorkspaceRunnable(jamsWorkspace, compLibFile, rtLibFile, fileFilter);
        worker.getWorkerDlg().setTask(uploadWsTask);
        worker.getWorkerDlg().execute();

        return uploadWsTask.getWorkspace();
    }

    private class StartJobRunnable implements Runnable {

        private Workspace ws = null;
        private WorkspaceFileAssociation wfa = null;
        private Job job = null;

        public StartJobRunnable(Workspace ws, WorkspaceFileAssociation wfa) {
            this.ws = ws;
            this.wfa = wfa;
        }

        @Override
        public void run() {
            try {
                job = client.jobs().create(ws, wfa);
            } catch (Throwable t) {
                log.log(Level.SEVERE, t.toString(), t);
            }
            if (job != null && job.getId() != -1) {
                log.log(Level.INFO, "Job was started successfully! It has ID = %1.".replace("%1", job.getId().toString()));
            } else {
                log.log(Level.INFO, "Failed to start job!");
            }
        }

        public Job getResult() {
            return job;
        }
    }

    /**
     *
     * @param ws
     * @param modelFile
     * @return
     */
    public Job startJob(Workspace ws, File modelFile) {
        if (ws == null) {
            return null;
        }

        WorkspaceFileAssociation model = null;
        for (WorkspaceFileAssociation wfa : ws.getFiles()) {
            if (wfa.getPath().endsWith(modelFile.getName())) {
                model = wfa;
            }
        }

        return startJob(ws, model);
    }

    /**
     *
     * @param ws
     * @param model
     * @return
     */
    public Job startJob(Workspace ws, WorkspaceFileAssociation model) {
        if (ws == null) {
            return null;
        }

        if (model == null) {
            return null;
        }

        observable.deleteObserver(worker);
        observable.addObserver(worker);
        worker.getWorkerDlg().setInderminate(true);
        worker.getWorkerDlg().setModal(true);
        StartJobRunnable task = new StartJobRunnable(ws, model);
        worker.getWorkerDlg().setTask(task);
        worker.getWorkerDlg().execute();
        return task.getResult();
        
//        SYNC?
    }
}
