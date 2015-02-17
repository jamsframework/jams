/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.server.client.gui;

import jams.JAMSLogging;
import jams.SystemProperties;
import jams.server.client.gui.JAMSCloudGraphicalController.JAMSCloudEvents;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToolBar;

/**
 *
 * @author christian
 */
public class JAMSCloudToolbar extends JToolBar {

    Logger log = Logger.getLogger(JAMSCloudToolbar.class.getName());

    SystemProperties p = null;
    JButton connectButton = new JButton("Connect");
    JProgressBar serverLoad = new JProgressBar();

    boolean isConnected = false;
    JAMSCloudGraphicalController connector = null;

    /**
     *
     * @param p
     */
    public JAMSCloudToolbar(SystemProperties p) {
        JAMSLogging.registerLogger(JAMSLogging.LogOption.Show, log);
        this.p = p;

        JPanel panel = new JPanel(new BorderLayout());
//        panel.setBorder(BorderFactory.createTitledBorder("Server-Load"));

        JPanel loadPanel = new JPanel(new BorderLayout());
        loadPanel.add(new JLabel("Server-Load"), BorderLayout.NORTH);
        loadPanel.add(serverLoad, BorderLayout.CENTER);
        panel.add(loadPanel, BorderLayout.CENTER);
        panel.add(connectButton, BorderLayout.WEST);
        panel.setMaximumSize(new Dimension(300, 50));
        panel.setMinimumSize(new Dimension(150, 10));
        panel.setPreferredSize(new Dimension(200, 32));
        add(panel);
        this.serverLoad.setIndeterminate(false);

        connector = JAMSCloudGraphicalController.createInstance(JAMSCloudToolbar.this.p);
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
                if (isConnected) {
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
        if (event.equals(JAMSCloudEvents.CONNECT)) {
            if (connector.isConnected()) {
                setServerUrl(connector.getServerUrl());
                connectButton.setText("Disconnect");
            }
        } else if (event.equals(JAMSCloudEvents.CONNECTING)) {
            setServerUrl(connector.getServerUrl());
            connectButton.setText("Connecting");
        } else if (event.equals(JAMSCloudEvents.DISCONNECT)) {
            setServerUrl("");
            setLoad(0.0);
            connectButton.setText("Connect");
        }
    }

    /**
     *
     * @param load
     */
    protected void setLoad(double load) {
        Color c = new Color((float) load, 0, 1.0f - (float) load);
        serverLoad.setBackground(c);
        serverLoad.setForeground(c);
        this.serverLoad.getModel().setValue((int) (load * 100.));
    }

    /**
     *
     * @param url
     */
    protected void setServerUrl(String url) {
        if (url.length() > 20) {
            serverLoad.setString(url.substring(0, 20) + "...");
        } else {
            serverLoad.setString(url);
        }
        serverLoad.setStringPainted(true);
    }
}
