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

import jams.gui.WorkerDlg;
import jams.server.client.Controller;
import jams.server.client.HTTPClient;
import jams.server.entities.Job;
import jams.server.entities.Jobs;
import jams.server.entities.User;
import jams.server.entities.Workspace;
import jams.server.entities.WorkspaceFileAssociation;
import jams.server.entities.Workspaces;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author christian
 */
public class RemoteGUI {

    Jobs jobs = null;

    JPanel mainPanel = null;
    JComboBox serverField = new JComboBox();
    JTree workspaceTree = new JTree();
    
    public class ColorRenderer extends JLabel
            implements TableCellRenderer {

        boolean isBordered = true;

        public ColorRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true); //MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(
                JTable table, Object arg,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            
            Job job = jobs.getJobs().get(row);
            if (job.getPID() > -1) {
                setBackground(Color.green);
                setForeground(Color.black);
            } else {
                setBackground(Color.gray);
                setForeground(Color.black);
            }
            setText(arg.toString());
            return this;
        }
    }
    final ColorRenderer renderer = new ColorRenderer(true);

    JTable jobsTable = new JTable(new DefaultTableModel() {

        @Override
        public int getRowCount() {
            if (jobs == null) {
                return 0;
            }
            return jobs.getJobs().size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "ID";
                case 1:
                    return "Workspace";
                case 2:
                    return "Name";
                case 3:
                    return "Start";
            }
            return "null";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Integer.class;
                case 1:
                    return Integer.class;
                case 2:
                    return String.class;
                case 3:
                    return Date.class;
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (jobs == null || rowIndex >= jobs.getJobs().size()) {
                return null;
            }

            Job j = jobs.getJobs().get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return j.getId();
                case 1:
                    return j.getWorkspace().getId();
                case 2:
                    return j.getWorkspace().getName();
                case 3:
                    return j.getStartTime();
            }
            return null;
        }
    }
    ) {
        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            if (jobs != null && jobs.getJobs() != null && jobs.getJobs().get(row).getPID() > -1) {
                return renderer;
            }
            return super.getCellRenderer(row, column);
        }
    };

    Action connectAction = null, 
            deleteJobAction = null, 
            deleteWorkspaceAction = null, 
            startWorkspaceAction = null, 
            downloadWorkspaceAction = null;

    Controller client = null;

    //saved data
    String[] recentUrls = {"http://localhost:8080/jams-server/webresources"};
    String user = "Blubb", pw = "test";
    boolean saveAccount = true;

    private void initActions() {
        for (String url : recentUrls) {
            serverField.addItem(url);
        }

        deleteJobAction = new AbstractAction("Delete"){
            
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = jobsTable.getSelectedRow();
                Job job = jobs.getJobs().get(selectedRow);
                client.getJobController().delete(job);                    
            }
        };
        
        startWorkspaceAction = new AbstractAction("Start Job"){
            
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = workspaceTree.getSelectionPath();
                Object o = path.getLastPathComponent();
                if (DefaultMutableTreeNode.class.isAssignableFrom(o.getClass())) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
                    Object o2 = node.getUserObject();
                    if (o2 instanceof WorkspaceNode) {
                        
                    }else if (o2 instanceof WFANode){
                        WFANode wfaNode = (WFANode)o2;
                        if (wfaNode.wfa.getRole() == WorkspaceFileAssociation.ROLE_MODEL){
                            Job job = client.getJobController().create(wfaNode.wfa.getWorkspace(), wfaNode.wfa);
                            if (job != null){
                                
                            }
                            
                        }
                    }
                }              
            }
        };
        
        downloadWorkspaceAction = new AbstractAction("Download"){
            
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = workspaceTree.getSelectionPath();
                Object o = path.getLastPathComponent();
                if (o instanceof WorkspaceNode){
                    
                }
                if (o instanceof WFANode){
                    
                }
            }
        };
        
        deleteWorkspaceAction = new AbstractAction("Delete"){
            
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = workspaceTree.getSelectionPath();
                Object o = path.getLastPathComponent();
                if (DefaultMutableTreeNode.class.isAssignableFrom(o.getClass())) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
                    Object o2 = node.getUserObject();
                    if (o2 instanceof WorkspaceNode) {
                        WorkspaceNode wsNode = (WorkspaceNode)o2;
                        Workspace ws = wsNode.ws;
                        client.getWorkspaceController().remove(ws);
                        updateView();
                    }
                }
                JOptionPane.showMessageDialog(mainPanel, "Removal of this type of object is not supported!");                
            }
        };
        
        connectAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String serverUrl = getServerUrl().toString();
                client = new Controller(new HTTPClient(), serverUrl);

                JLabel jUserName = new JLabel("User Name");
                JTextField userName = new JTextField();
                JLabel jPassword = new JLabel("Password");
                JTextField password = new JPasswordField();
                if (saveAccount) {
                    userName.setText(user);
                    password.setText(pw);
                }
                Object[] ob = {jUserName, userName, jPassword, password};
                int result = JOptionPane.showConfirmDialog(null, ob, "Please input username/password", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    String userNameValue = userName.getText();
                    String passwordValue = password.getText();
                    if (saveAccount) {
                        user = userName.getText();
                        pw = password.getText();
                    }
                    client.connect(userNameValue, passwordValue);
                    updateView();
                }
            }
        };

        jobsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                javax.swing.DefaultListSelectionModel table = (javax.swing.DefaultListSelectionModel) e.getSource();
                int row = table.getLeadSelectionIndex();
                if (row != -1) {

                }
            }
        });

        jobsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int r = jobsTable.rowAtPoint(e.getPoint());
                if (r >= 0 && r < jobsTable.getRowCount()) {
                    jobsTable.setRowSelectionInterval(r, r);
                } else {
                    jobsTable.clearSelection();
                }

                int rowindex = jobsTable.getSelectedRow();
                if (rowindex < 0) {
                    return;
                }
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
                    JPopupMenu popup = new JPopupMenu();
                    popup.add(new JMenuItem(deleteJobAction));
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        
        workspaceTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                TreePath p = workspaceTree.getClosestPathForLocation(e.getX(), e.getY());
                if (p != null){
                    workspaceTree.getSelectionModel().setSelectionPath(p);
                }else{
                    workspaceTree.getSelectionModel().clearSelection();
                }
                if (!e.isPopupTrigger() || !(e.getComponent() instanceof JTree)) {
                    return;                    
                }
                Object lastTreePathObject = p.getLastPathComponent();
                if (DefaultMutableTreeNode.class.isAssignableFrom(lastTreePathObject.getClass())){
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)lastTreePathObject;
                    Object userObject = node.getUserObject();
                    
                    JPopupMenu popup = new JPopupMenu();
                    if (userObject instanceof WFANode){
                        WFANode userObjectWrapper = (WFANode)userObject;
                        if (userObjectWrapper.wfa.getRole() == WorkspaceFileAssociation.ROLE_MODEL){
                            popup.add(new JMenuItem(startWorkspaceAction));                    
                            popup.add(new JMenuItem(downloadWorkspaceAction));
                        }
                    }else if (userObject instanceof WorkspaceNode){
                        popup.add(new JMenuItem(startWorkspaceAction));                    
                        popup.add(new JMenuItem(downloadWorkspaceAction));
                        popup.add(new JMenuItem(deleteWorkspaceAction));
                    }
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
                
                
            }
        });
    }

    private void updateView() {
        WorkerDlg dlg = new WorkerDlg(null, "Retrieving data .. ");
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
        Collections.sort(jobs.getJobs(), new Comparator<Job>() {

            @Override
            public int compare(Job o1, Job o2) {
                if (o1.getPID() == -1 && o2.getPID() != -1) {
                    return -1;
                }
                if (o2.getPID() == -1 && o1.getPID() != -1) {
                    return +1;
                }
                return o1.getId().compareTo(o2.getId());
            }
        });

        workspaceTree.setModel(createWorkspaceTree(client.getUser(), client.getWorkspaceController().findAll(null)));

        ((DefaultTableModel) this.jobsTable.getModel()).fireTableDataChanged();
    }

    private class WFANode {

        WorkspaceFileAssociation wfa;
        String fileName;
        String subdirs[];

        public WFANode(WorkspaceFileAssociation wfa) {
            this.wfa = wfa;
            subdirs = wfa.getPath().split("[/\\\\]");
            if (subdirs.length != 0) {
                fileName = subdirs[subdirs.length - 1];
            } else {
                fileName = "";
            }
        }

        public String[] getSubdirs() {
            return subdirs;
        }

        public String getFileName() {
            return fileName;
        }

        @Override
        public String toString() {
            return fileName;
        }
    }

    private class WorkspaceNode {

        Workspace ws;

        public WorkspaceNode(Workspace ws) {
            this.ws = ws;
        }

        @Override
        public String toString() {
            return ws.getName() + "(id:" + ws.getId() + ")";
        }
    }

    public class SortedMutableTreeNode extends DefaultMutableTreeNode {

        public SortedMutableTreeNode(Object o) {
            super(o);
        }

        public void add(DefaultMutableTreeNode newChild) {
            super.add(newChild);
            Collections.sort(this.children, nodeComparator);
        }

        public void insert(DefaultMutableTreeNode newChild, int childIndex) {
            super.insert(newChild, childIndex);
            Collections.sort(this.children, nodeComparator);
        }

        protected Comparator nodeComparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof SortedMutableTreeNode && o2 instanceof SortedMutableTreeNode) {
                    SortedMutableTreeNode s1 = (SortedMutableTreeNode) o1;
                    SortedMutableTreeNode s2 = (SortedMutableTreeNode) o2;
                    if (!s1.isLeaf() && s2.isLeaf()) {
                        return -1;
                    }
                    if (s1.isLeaf() && !s2.isLeaf()) {
                        return 1;
                    }
                }
                return o1.toString().compareToIgnoreCase(o2.toString());
            }

            public boolean equals(Object obj) {
                return false;
            }
        };
    }

    private void attachWFAtoTree(WorkspaceFileAssociation wfa, SortedMutableTreeNode top) {
        WFANode node = new WFANode(wfa);

        SortedMutableTreeNode currentNode = top;
        for (String dir : node.subdirs) {
            if (node.fileName.equals(dir)) {
                SortedMutableTreeNode newNode = new SortedMutableTreeNode(new WFANode(wfa));
                currentNode.add(newNode);
                break;
            }
            Enumeration enumeration = currentNode.children();
            boolean successful = false;
            while (enumeration.hasMoreElements()) {
                Object o = enumeration.nextElement();
                if (o.toString().equals(dir)) {
                    currentNode = (SortedMutableTreeNode) o;
                    successful = true;
                    break;
                }
            }
            if (!successful) {
                SortedMutableTreeNode newNode = new SortedMutableTreeNode(dir);
                currentNode.add(newNode);
                currentNode = newNode;
            }
        }
    }

    private DefaultTreeModel createWorkspaceTree(User user, Workspaces workspaces) {
        SortedMutableTreeNode root = new SortedMutableTreeNode(user);

        for (Workspace ws : workspaces.getWorkspaces()) {
            root.add(createWorkspaceNode(ws));
        }

        DefaultTreeModel model = new DefaultTreeModel(root);
        return model;
    }

    private DefaultMutableTreeNode createWorkspaceNode(Workspace ws) {
        SortedMutableTreeNode top = new SortedMutableTreeNode(new WorkspaceNode(ws));

        for (WorkspaceFileAssociation wfa : ws.getFiles()) {
            attachWFAtoTree(wfa, top);
        }
        return top;
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

        JScrollPane jobsScroller = new JScrollPane(jobsTable);
        jobsScroller.setBorder(BorderFactory.createTitledBorder("Jobs"));

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
                                .addComponent(jobsScroller)
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
                                .addComponent(jobsScroller)
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
