/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jamsui.juice.gui;

import jams.JAMS;
import jams.JAMSLogging;
import jams.JAMSProperties;
import jams.SystemProperties;
import jams.gui.tools.GUIHelper;
import jams.server.client.Controller;
import jams.server.client.gui.BrowseJAMSCloudDlg;
import jams.server.client.gui.JAMSCloudGraphicalController;
import jams.server.client.gui.JAMSCloudGraphicalController.JAMSCloudEvents;
import static jams.server.client.gui.JAMSCloudGraphicalController.JAMSCloudEvents.CONNECT;
import static jams.server.client.gui.JAMSCloudGraphicalController.JAMSCloudEvents.CONNECTING;
import static jams.server.client.gui.JAMSCloudGraphicalController.JAMSCloudEvents.DISCONNECT;
import jams.server.client.gui.SynchronizeDlg;
import jams.server.entities.Job;
import jams.server.entities.WorkspaceFileAssociation;
import jams.tools.XMLTools;
import jams.workspace.Workspace;
import jamsui.juice.JUICE;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JToolBar;

/**
 *
 * @author christian
 */
public class JAMSCloudToolbar extends JToolBar {

    private Logger log = Logger.getLogger(JAMSCloudToolbar.class.getName());

    private JUICEFrame juiceFrame;
    private SystemProperties properties = null;
    private JButton connectButton, wsCloudButton, runCloudButton, wsSyncButton;
    ;

    private JProgressBar serverLoad = new JProgressBar();

    private ImageIcon connect1 = new ImageIcon(getClass().getResource("/resources/images/connect1_b.png"));
    private ImageIcon connect2 = new ImageIcon(getClass().getResource("/resources/images/connect2_b.png"));
    private ImageIcon connect3 = new ImageIcon(getClass().getResource("/resources/images/connect3_b.png"));

    private JAMSCloudGraphicalController connector = null;
    private static BrowseJAMSCloudDlg jamsCloudBrowser = null;

    private Action wsCloudAction = new AbstractAction(JAMS.i18n("Browse_WS_Cloud")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            openWSCloud();
        }
    };

    private Action runCloudAction = new AbstractAction(JAMS.i18n("Run_Model_Remote")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                runModelInCloud();
            } catch (IOException ioe) {
                log.log(Level.SEVERE, ioe.toString(), ioe);
            }
        }
    };

    private Action wsSyncAction = new AbstractAction(JAMS.i18n("Sync_WS_Cloud")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            syncWorkspace();
        }
    };

    /**
     *
     * @param p
     */
    public JAMSCloudToolbar(JUICEFrame jf, SystemProperties p) {
        JAMSLogging.registerLogger(JAMSLogging.LogOption.Show, log);

        this.juiceFrame = jf;
        this.properties = p;

        wsCloudButton = new JButton(wsCloudAction);
        wsCloudButton.setText("");
        wsCloudButton.setToolTipText(JAMS.i18n("Browse_WS_Cloud"));
        wsCloudButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ws_cloud2.png")));

        runCloudButton = new JButton(runCloudAction);
        runCloudButton.setText("");
        runCloudButton.setToolTipText(JAMS.i18n("Run_Model_Remote"));
        runCloudButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelRunCloud.png")));

        wsSyncButton = new JButton(wsSyncAction);
        wsSyncButton.setText("");
        wsSyncButton.setToolTipText(JAMS.i18n("Sync_WS_Cloud"));
        wsSyncButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ws_sync.png")));

        connectButton = new JButton();
        connectButton.setText("");
        connectButton.setToolTipText(JAMS.i18n("Connect_to_Cloud"));

//        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
//        this.setLayout(layout);
//        layout.setHorizontalGroup(
//                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addGroup(layout.createSequentialGroup()
//                        .addComponent(connectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
//                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(serverLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
//                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(browseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
//                )
//        );
//        layout.setVerticalGroup(
//                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addComponent(connectButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
//                        .addComponent(serverLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
//                        .addGap(6, 6, 6))
//                .addComponent(browseButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//        );
//        JPanel loadPanel = new JPanel();
//        loadPanel.setLayout(new BoxLayout(loadPanel, BoxLayout.X_AXIS));
//        loadPanel.setBorder(BorderFactory.createEtchedBorder());
//        loadPanel.setMaximumSize(new Dimension(200, 24));
//        loadPanel.setMinimumSize(new Dimension(200, 24));
//        loadPanel.add(serverLoad);
        serverLoad.setMaximumSize(new Dimension(200, 21));
        serverLoad.setPreferredSize(new Dimension(200, 21));

        this.add(connectButton);
        this.add(serverLoad);
        this.add(runCloudButton);
        this.add(wsCloudButton);
        this.add(wsSyncButton);
//        this.add(loadPanel);
        this.serverLoad.setIndeterminate(false);

        connector = JAMSCloudGraphicalController.createInstance(jf, properties);
        connector.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                setConnectionState((JAMSCloudEvents) arg);
            }
        });

        if (connector.isConnected()) {
            setConnectionState(JAMSCloudEvents.CONNECT);
        } else {
            setConnectionState(JAMSCloudEvents.DISCONNECT);
        }

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    if (connector != null && connector.isConnected()) {
                        double load = connector.getClient().getLoad();
                        setLoad(load);
                    }
                } catch (Throwable t) {
                    //LogTools.log(JAMSCloudToolbar.class, Level.WARNING, t, t.toString());
                }
            }
        }, 5000, 5000);

        connectButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (connector.isConnected()) {
                    connector.disconnect();
                } else {
                    try {
                        connector.reconnect();
                    } catch (IOException ioe) {
                        log.log(Level.SEVERE, ioe.toString(), ioe);
                    }
                }
            }
        });
    }

    /**
     *
     * @param event
     */
    protected void setConnectionState(JAMSCloudEvents event) {
        switch (event) {
            case CONNECT:
                if (connector.isConnected()) {
                    serverLoad.setEnabled(true);
                    wsCloudAction.setEnabled(true);
                    runCloudAction.setEnabled(true);
                    wsSyncAction.setEnabled(true);
                    setServerUrl(connector.getServerUrl());
                    connectButton.setIcon(connect3);
                }
                break;
            case CONNECTING:
                setServerUrl("");
                setLoad(0.0);
                serverLoad.setEnabled(false);
                wsCloudAction.setEnabled(false);
                runCloudAction.setEnabled(false);
                wsSyncAction.setEnabled(false);
                connectButton.setIcon(connect2);
                break;
            case DISCONNECT:
                setServerUrl("");
                setLoad(0.0);
                serverLoad.setEnabled(false);
                wsCloudAction.setEnabled(false);
                runCloudAction.setEnabled(false);
                wsSyncAction.setEnabled(false);
                connectButton.setIcon(connect1);
                break;
            default:
                break;
        }
    }

    /**
     *
     * @param load
     */
    protected void setLoad(double load) {
        serverLoad.setToolTipText("Server Load: " + String.format("%.2f", load));
        load = Math.min(load, 1);
        Color c = new Color((float) load, 1.0f - (float) load, 0);
        serverLoad.setBackground(c);
        serverLoad.setForeground(c);
        this.serverLoad.getModel().setValue((int) (load * 100.));
    }

    /**
     *
     * @param url
     */
    protected void setServerUrl(String url) {
        if (url.length() > 30) {
            url = url.substring(url.indexOf("://") + 3);
            url = url.substring(0, url.indexOf("/"));
            if (url.length() > 30) {
                url = url.substring(0, 30) + "...";
            }
        }
        serverLoad.setString(url);
        serverLoad.setStringPainted(true);
    }

    protected void syncWorkspace() {
        try {
            if (juiceFrame.getCurrentView() == null) {
                return;
            }
            Workspace wslocal = juiceFrame.getCurrentView().getWorkspace();
            wslocal.loadConfig();
            SynchronizeDlg dlg = new SynchronizeDlg(juiceFrame, wslocal, properties);
            dlg.setPreferredSize(new Dimension(640, 480));
            GUIHelper.centerOnParent(dlg, true);
            dlg.setVisible(true);
        } catch (IOException ioe) {
            log.log(Level.SEVERE, ioe.toString(), ioe);
        }
    }

    public void openWSCloud() {
        if (jamsCloudBrowser == null) {
            jamsCloudBrowser = new BrowseJAMSCloudDlg(juiceFrame, properties);
            jamsCloudBrowser.init();
            jamsCloudBrowser.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        }
        GUIHelper.centerOnParent(jamsCloudBrowser, true);
        jamsCloudBrowser.setVisible(true);
    }

    //TODO: function is too complex .. 
    public Job runModelInCloud() throws IOException {

        Controller client = connector.getClient();
        if (juiceFrame.getCurrentView() == null) {
            return null;
        }
        Workspace jamsWorkspace = juiceFrame.getCurrentView().getWorkspace();

        String libs = properties.getProperty(JAMSProperties.LIBS_IDENTIFIER);
        String newLibs = "";
        String compLibArray[] = libs.split(";");
        File compLibFile[] = new File[compLibArray.length];
        for (int i = 0; i < compLibArray.length; i++) {
            compLibFile[i] = new File(compLibArray[i]);
            newLibs += "components/" + i + "/" + compLibFile[i].getName() + ";";
        }
        newLibs = newLibs.substring(0, newLibs.length() - 1);

//        File jamsuiLib = getJAMSuiLib();
        File libDir = JAMS.getLibDir();
        String uploadFileFilter = properties.getProperty("uploadFileFilter");
        if (uploadFileFilter == null) {
            uploadFileFilter = "(.*\\.cache)|(.*\\.ser)|(.*\\.svn)|(.*/output/.*)|(.*/documentation/.*)|(.*\\.cdat)|(.*\\.log)";
        }
        //get remote id of my workspace
        jamsWorkspace.loadConfig();

        String title = jamsWorkspace.getTitle();
        jams.server.entities.Workspace ws = null;

        if (title == null || title.isEmpty()) {
            title = JOptionPane.showInputDialog(juiceFrame,
                    JAMS.i18n("The_workspace_you_are_going_to_upload_has_no_name"),
                    JAMS.i18n("Name_of_workspace"),
                    JOptionPane.QUESTION_MESSAGE);
            if (title == null) {
                title = "unnamed";
            } else {
                jamsWorkspace.setTitle(title);
            }
        }

        ws = connector.uploadWorkspace(jamsWorkspace, compLibFile, libDir, uploadFileFilter);
        if (ws == null) {
            return null;
        }

        jamsWorkspace.setID(ws.getId());

        InputStream inputStream;
        jams.server.entities.File f;

        inputStream = XMLTools.writeXmlToStream(juiceFrame.getCurrentView().getModelDoc());
        f = client.files().uploadFile(inputStream);
        ws = client.workspaces().attachFile(ws,
                new WorkspaceFileAssociation(ws, f, WorkspaceFileAssociation.ROLE_MODEL,
                        juiceFrame.getCurrentView().getSavePath().getName()));

        Properties props = (Properties) JUICE.getJamsProperties().getProperties().clone();
        props.setProperty(JAMSProperties.LIBS_IDENTIFIER, newLibs);
        props.setProperty("progressperiod", "1000");
        props.setProperty("progressfilename", "progress.log");
        inputStream = XMLTools.propertiesToStream(props);
        f = client.files().uploadFile(inputStream);
        ws = client.workspaces().attachFile(ws,
                new WorkspaceFileAssociation(ws, f, WorkspaceFileAssociation.ROLE_JAPFILE, "cloud.jap"));

        return connector.startJob(ws, new File(juiceFrame.getCurrentView().getSavePath().getName()));
    }

}
