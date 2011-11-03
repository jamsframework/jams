/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.gui;

import jams.gui.WorkerDlg;
import jams.remote.client.Client;
import jams.remote.common.ByteStream.ProgressInfo;
import jams.remote.common.FileInfo;
import jams.remote.common.JAMSConnection;
import jams.remote.common.JAMSConnection.Operation;
import jams.remote.common.JAMSConnection.ReceivePacketListener;
import jams.remote.common.JobState;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author chris
 */
public class RemoteDataRetrival extends JPanel {

    JPanel mainPanel;
    JLabel title;
    JLabel nameLabel = new JLabel("username");
    JTextField usernameField= new JTextField();
    JLabel passwordLabel = new JLabel("password");
    JPasswordField passwordField= new JPasswordField();;
    JButton connectButton = new JButton("Connect");;    
    boolean isConnected;
    JPanel fileState = new JPanel();
    JButton downloadButton;
    JButton closeButton;
    Client client;
    DefaultTableModel jobTableModel = null;
    JTable jobTable;
    JScrollPane jobListScrollPane;
    DefaultTableModel fileTableModel = null;
    JTable fileTable;
    JScrollPane fileListScrollPane;
    ArrayList<JobState> jobList;
    ArrayList<FileInfo> fileList;
    JobState currentSelectedJob;
    FileInfo currentSelectedFile;
    File downloadDirectory;
    LinkedList<FileInfo> downloadQueue = new LinkedList<FileInfo>();
    WorkerDlg dlg = new WorkerDlg(null, "Receiving file");
    ProgressInfo progressInfo = new ProgressInfo();
    JCheckBox showAllFiles = new JCheckBox("show all");
    boolean isShowingAllFiles = false;

    public RemoteDataRetrival(File downloadDirectory) {
        this.downloadDirectory = downloadDirectory;
        init();
        layoutComponents();
    }

    public JDialog getDialog(Frame owner) {
        JDialog dlg = new JDialog(owner, "remote data retrival");
        dlg.getContentPane().setLayout(new BorderLayout());
        dlg.getContentPane().add(this);
        dlg.setPreferredSize(new Dimension(900, 400));
        dlg.setSize(new Dimension(900, 400));
        return dlg;
    }

    private void init() {
        title = new JLabel("Data retrieval from JAMS Server");
               
        connectButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!isConnected)
                    connect();
                else{
                    client.getConnection().close();
                    setConnectState(!client.isClosed());
                }
            }
        });
        setConnectState(false);

        jobTableModel = new DefaultTableModel(new String[]{"ID", "Finished", "Starttime", "Host", "Description"}, 0) {

            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return Long.class;
                    case 1:
                        return Boolean.class;
                    case 2:
                        return Date.class;
                    case 3:
                        return String.class;
                    case 4:
                        return String.class;
                    default:
                        return String.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        jobTable = new JTable(jobTableModel);
        jobTable.getColumn("ID").setPreferredWidth(30);
        jobTable.getColumn("Finished").setPreferredWidth(20);
        jobTable.getColumn("Starttime").setPreferredWidth(80);
        jobTable.getColumn("Host").setPreferredWidth(80);
        jobTable.getColumn("Description").setPreferredWidth(200);

        jobTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                int row = jobTable.getSelectedRow();
                if (row != -1) {
                    currentSelectedJob = jobList.get(row);
                    retriveFileList();
                }
            }
        });

        jobListScrollPane = new JScrollPane(jobTable);

        fileTableModel = new DefaultTableModel(new String[]{"Name", "Type", "Size"}, 0) {

            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1:
                        return String.class;
                    case 2:
                        return Long.class;
                    default:
                        return String.class;
                }
            }
        };

        fileTable = new JTable(fileTableModel);
        fileTable.getColumn("Name").setPreferredWidth(100);
        fileTable.getColumn("Type").setPreferredWidth(60);
        fileTable.getColumn("Size").setPreferredWidth(60);
        fileListScrollPane = new JScrollPane(fileTable);

        fileTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                int row = fileTable.getSelectedRow();
                if (row != -1) {
                    currentSelectedFile = fileList.get(row);
                }
            }
        });

        downloadButton = new JButton("Download");
        downloadButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                download(currentSelectedFile);
            }
        });

        closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (getParent() != null) {
                    getParent().setVisible(true);
                }
            }
        });

        showAllFiles.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setShowAllFiles(showAllFiles.isSelected());
            }
        });
    }

    private void layoutComponents() {
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(title).addGroup(layout.createSequentialGroup()
                .addComponent(nameLabel)
                .addComponent(usernameField).addComponent(passwordLabel)
                .addComponent(passwordField).addComponent(connectButton))
                .addGroup(
                    layout.createSequentialGroup()
                        .addComponent(jobListScrollPane)
                        .addComponent(fileListScrollPane)
                        .addComponent(fileState)));

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                    .addComponent(title)
                    .addGroup(
                        layout.createParallelGroup()
                            .addComponent(nameLabel)
                            .addComponent(usernameField)
                            .addComponent(passwordLabel)
                            .addComponent(passwordField)
                            .addComponent(connectButton))
                            .addGroup(layout.createParallelGroup()
                                .addComponent(jobListScrollPane)
                                .addComponent(fileListScrollPane)
                                .addComponent(fileState)));


        GroupLayout layout2 = new GroupLayout(fileState);
        fileState.setLayout(layout2);
        layout2.setAutoCreateContainerGaps(true);
        layout2.setAutoCreateGaps(true);

        layout2.setHorizontalGroup(
                layout2.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(downloadButton)
                .addComponent(closeButton)
                .addComponent(showAllFiles));

        layout2.setVerticalGroup(
                layout2.createSequentialGroup()
                .addComponent(downloadButton)
                .addComponent(closeButton)
                .addComponent(showAllFiles));

    }

    private void updateFileTable() {
        fileTableModel.setRowCount(0);
        for (FileInfo fi : fileList) {
            String type = null;
            if (fi.getName().endsWith("dat")) {
                type = "data file";
            } else if (fi.getName().endsWith("jam")) {
                type = "model file";
            } else if (fi.getName().endsWith("xml")) {
                type = "xml file";
            } else if (fi.getName().endsWith("jap")) {
                type = "jams properties";
            } else if (fi.getName().endsWith("jmp")) {
                type = "jams model properties";
            } else if (fi.getName().endsWith("log")) {
                type = "log file";
            }
            if (!this.isShowingAllFiles && type != null)
                fileTableModel.addRow(new Object[]{fi.getName(), type, fi.getSize()});
            else if (this.isShowingAllFiles)
                fileTableModel.addRow(new Object[]{fi.getName(), "unknown", fi.getSize()});
        }
    }

    private void setShowAllFiles(boolean isShowingAllFiles){
        this.isShowingAllFiles = isShowingAllFiles;
        updateFileTable();
    }
    
    private void setConnectState(boolean connected) {
        isConnected = connected;
        if (connected) {
            connectButton.setText("Disconnect");
            connectButton.setBackground(Color.green);
        } else {
            connectButton.setText("Connect");
            connectButton.setBackground(Color.red);
        }
    }

    private void download(FileInfo fi) {
        downloadQueue.add(fi);

        dlg.setTitle("Receiving file " + fi.getName() + " there are " + downloadQueue.size() + " files in queue.");
        dlg.setInderminate(false);
        dlg.setTask(new Runnable() {
            public void run() {
                try {
                    if (downloadQueue.isEmpty())
                        return;
                    FileInfo fi = downloadQueue.pop();
                    File dstFile = new File(downloadDirectory.getAbsolutePath() + fi.getName());
                    client.getFile(dstFile, (int) fi.getJob(), fi.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        dlg.setProgress(0);
        dlg.setModal(false);
        dlg.execute();
    }

    private void connect() {
        String username = usernameField.getText();
        char password[] = passwordField.getPassword();
        String passwordString = new String(password);

        client = new Client("sonne.geogr.uni-jena.de", 9000, username, passwordString);
        client.connect();
        client.getConnection().addReceivePacketListener(new ReceivePacketListener() {

            @Override
            public void packetReceived(JAMSConnection connection, Operation operation, int bytes, int bytesTotal, Object o) {
                if (RemoteDataRetrival.this.dlg != null && bytesTotal > 200000){
                    dlg.setProgressMax(bytesTotal);
                    dlg.setProgress(bytes);
                }
            }
        });
        setConnectState(!client.isClosed());
        if (!isConnected) {
            JOptionPane.showMessageDialog(mainPanel, "Failed to connect to server!");
            return;
        } else {
            retriveJobList();
        }
    }

    private void retriveFileList() {        
        try {
            fileList = client.getFileListing(currentSelectedJob);
            updateFileTable();
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(getParent(), "Failed to retrive job list from server!");
            ioe.printStackTrace();
        }
    }

    private void retriveJobList() {
        jobTableModel.setRowCount(0);
        if (!isConnected) {
            return;
        }
        try {
            jobList = client.getJobListing();
            for (JobState state : jobList) {
                jobTableModel.addRow(new Object[]{state.getId(), !state.isIsRunning(), state.getStartDate(), state.getHost(), state.getDescription()});
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(getParent(), "Failed to retrive job list from server!");
            ioe.printStackTrace();
        }
    }
}
