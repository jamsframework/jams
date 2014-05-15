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

import jams.server.client.Controller;
import jams.server.client.HTTPClient;
import jams.server.client.JAMSClientException;
import jams.server.entities.Job;
import jams.server.entities.Jobs;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author christian
 */
public class RemoteGUI {

    Jobs activeJobs = null;
    Jobs inactiveJobs = null;
        
    JPanel mainPanel = null;
    JComboBox serverField = new JComboBox();
    JTable activeJobsTable = new JTable(new DefaultTableModel() {

            @Override
            public int getRowCount() {
                if (activeJobs==null)
                    return 0;
                return activeJobs.getJobs().size();
            }

            @Override
            public int getColumnCount() {
                return 4;
            }

            @Override
            public String getColumnName(int columnIndex) {
                switch(columnIndex){
                    case 0: return "ID";
                    case 1: return "Workspace";
                    case 2: return "Name";    
                    case 3: return "Start";    
                }
                return "null";
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch(columnIndex){
                    case 0: return Integer.class;
                    case 1: return Integer.class;
                    case 2: return String.class;
                    case 3: return Date.class;
                }
                return null;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                if (activeJobs==null || rowIndex>=activeJobs.getJobs().size())
                    return null;
                
                Job j = activeJobs.getJobs().get(rowIndex);
                switch(columnIndex){
                    case 0: return j.getId();
                    case 1: return j.getWorkspace().getId();
                    case 2: return j.getWorkspace().getName();
                    case 3: return j.getStartTime();
                }
                return null;
            }
        }
    );
    JTable inactiveJobsTable = new JTable(new DefaultTableModel() {

            @Override
            public int getRowCount() {
                if (inactiveJobs==null)
                    return 0;
                return inactiveJobs.getJobs().size();
            }

            @Override
            public int getColumnCount() {
                return 4;
            }

            @Override
            public String getColumnName(int columnIndex) {
                switch(columnIndex){
                    case 0: return "ID";
                    case 1: return "Workspace";
                    case 2: return "Name";    
                    case 3: return "Finished";    
                }
                return "null";
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch(columnIndex){
                    case 0: return Integer.class;
                    case 1: return Integer.class;
                    case 2: return String.class;
                    case 3: return Date.class;
                }
                return null;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                if (inactiveJobs==null || rowIndex>=inactiveJobs.getJobs().size())
                    return null;
                
                Job j = inactiveJobs.getJobs().get(rowIndex);
                switch(columnIndex){
                    case 0: return j.getId();
                    case 1: return j.getWorkspace().getId();
                    case 2: return j.getWorkspace().getName();
                    case 3: return j.getStartTime(); //TODO
                }
                return null;
            }
        }
    );
        
    Action connectAction = null;

    Controller client = null;

    
    //saved data
    String[] recentUrls={"http://localhost:8080/jams-server/webresources"};
    String user="Blubb", pw="test";
    boolean saveAccount = true;
    private void initActions() {
        for (String url : recentUrls){
            serverField.addItem(url);
        }
        
        connectAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String serverUrl = getServerUrl().toString();
                client = new Controller(new HTTPClient(), serverUrl);

                JLabel jUserName = new JLabel("User Name");                
                JTextField userName = new JTextField();
                JLabel jPassword = new JLabel("Password");
                JTextField password = new JPasswordField();
                if (saveAccount){
                    userName.setText(user);
                    password.setText(pw);
                }
                Object[] ob = {jUserName, userName, jPassword, password};
                int result = JOptionPane.showConfirmDialog(null, ob, "Please input username/password", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    String userNameValue = userName.getText();
                    String passwordValue = password.getText();
                    if (saveAccount){
                        user = userName.getText();
                        pw = password.getText();
                    }
                    try{
                        client.connect(userNameValue, passwordValue);
                        updateView();
                    }catch(JAMSClientException jce){
                        handleException(jce);
                    }
                }                
            }                        
        };                
    }

    private void updateView() throws JAMSClientException{
        if (client == null)
            return;
        
        activeJobs = client.getJobController().getActiveJobs();
        Jobs allJobs = client.getJobController().find();
                        
        if (inactiveJobs==null){
            inactiveJobs = new Jobs();
        }
        inactiveJobs.getJobs().clear();
        for (Job j : allJobs.getJobs()){
            if (!activeJobs.getJobs().contains(j))
                inactiveJobs.add(j);
        }

        ((DefaultTableModel)this.activeJobsTable.getModel()).fireTableDataChanged();
        ((DefaultTableModel)this.inactiveJobsTable.getModel()).fireTableDataChanged();
    }
    
    private void handleException(Exception e) {
        JOptionPane.showMessageDialog(mainPanel, e);
    }

    private URL getServerUrl() {
        String text = serverField.getSelectedItem().toString();
        try {
            URL url = new URL(text);
            return url;
        } catch (MalformedURLException mE) {
            handleException(mE);
            return null;
        }
    }

    public void init() {
        initActions();
        
        mainPanel = new JPanel();
        GroupLayout mainLayout = new GroupLayout(mainPanel);
        mainPanel.setLayout(mainLayout);

        JLabel serverLabel = new JLabel("Server-URL");

        JButton connectButton = new JButton(connectAction);
        connectButton.setText("Connect");
        JButton uploadWs = new JButton("Upload Workspace");
        
        JScrollPane activeJobsScroller = new JScrollPane(activeJobsTable);
        JScrollPane nonActiveJobsScroller = new JScrollPane(inactiveJobsTable);
        activeJobsScroller.setBorder(BorderFactory.createTitledBorder("active Jobs"));
        nonActiveJobsScroller.setBorder(BorderFactory.createTitledBorder("inactive Jobs"));

        JTree workspaceTree = new JTree();
        JScrollPane workspaceTreeScroller = new JScrollPane(workspaceTree);

        workspaceTreeScroller.setBorder(BorderFactory.createTitledBorder("Workspaces"));

        mainLayout.setHorizontalGroup(mainLayout.createParallelGroup()
                .addGroup(mainLayout.createSequentialGroup()
                        .addComponent(serverLabel)
                        .addComponent(serverField)
                        .addComponent(connectButton)
                )
                .addGroup(mainLayout.createSequentialGroup()
                        .addGroup(mainLayout.createParallelGroup()
                                .addComponent(activeJobsScroller)
                                .addComponent(nonActiveJobsScroller)
                        )
                        .addGroup(mainLayout.createParallelGroup()
                                .addComponent(workspaceTreeScroller)
                                .addComponent(uploadWs, 0, 250, 4000)
                        )
                )
        );

        mainLayout.setVerticalGroup(mainLayout.createSequentialGroup()
                .addGroup(mainLayout.createParallelGroup()
                        .addComponent(serverLabel, 0, 25, 25)
                        .addComponent(serverField, 0, 25, 25)
                        .addComponent(connectButton, 0, 25, 25)
                )
                .addGroup(mainLayout.createParallelGroup()
                        .addGroup(mainLayout.createSequentialGroup()
                                .addComponent(activeJobsScroller)
                                .addComponent(nonActiveJobsScroller)
                        )
                        .addGroup(mainLayout.createSequentialGroup()
                                .addComponent(workspaceTreeScroller)
                                .addComponent(uploadWs)
                        )
                )
        );
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public static void main(String[] args) {
        RemoteGUI gui = new RemoteGUI();
        gui.init();

        JPanel panel = gui.getPanel();
        JFrame frame = new JFrame();
        frame.add(panel);
        frame.invalidate();
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
