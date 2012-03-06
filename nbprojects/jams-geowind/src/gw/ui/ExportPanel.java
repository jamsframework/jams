/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ExportPanel.java
 *
 * Created on 05.06.2009, 11:34:33
 */
package gw.ui;

import gw.ui.layerproperies.ShapefileLayerProperties;
import gw.ui.util.ProxyTableModel;
import jams.tools.StringTools;
import java.io.File;
import java.util.List;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileFilter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import reg.shape.ShapeFactory;

/**
 *
 * @author hbusch
 */
public class ExportPanel extends javax.swing.JDialog {

    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;

    private ShapefileLayerProperties shapefileLayerProperties;

    private JFileChooser fileChooser = new JFileChooser();

    /** Creates new form ExportPanel */
    public ExportPanel(java.awt.Frame parent, boolean modal, ShapefileLayerProperties theLayerProperties) {
        super(parent, modal);
        initComponents();
        this.shapefileLayerProperties = theLayerProperties;
        setupComponents();
    }

    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {

        li_columnList = new JList();
        li_columnList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        li_columnList.setAutoscrolls(true);

        li_columns_scrollPane = new JScrollPane(li_columnList);

        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        i_filename = new java.awt.TextField();
        exportButton = new java.awt.Button();
        cancelButton = new java.awt.Button();
        messageText = new java.awt.Label();
        fileChooseButton = new javax.swing.JButton();
        out_Message = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("gw/resources/language"); // NOI18N
        setTitle(bundle.getString("L_ExportShape")); // NOI18N
        setAlwaysOnTop(true);
        setIconImage(null);
        setName("exportDialog"); // NOI18N

        li_columnList.setEnabled(true);

        jLabel1.setText(bundle.getString("L_COLS")); // NOI18N

        jLabel3.setText(bundle.getString("L_FILENAME")); // NOI18N

        exportButton.setLabel(bundle.getString("L_EXPORT")); // NOI18N
        exportButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        cancelButton.setLabel(bundle.getString("L_EXIT")); // NOI18N

        messageText.setFont(new java.awt.Font("Dialog", 2, 12));
        messageText.setForeground(new java.awt.Color(255, 51, 51));
        messageText.setName("messageText"); // NOI18N
        messageText.setText("error");
        messageText.setVisible(false);

        fileChooseButton.setText(bundle.getString("L_SELECT")); // NOI18N
        fileChooseButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooseButtonActionPerformed(evt);
            }
        });

        out_Message.setForeground(new java.awt.Color(0, 102, 0));
        out_Message.setText("jLabel2");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(70, 70, 70).addComponent(exportButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(74, 74, 74).addComponent(cancelButton, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE).addGap(97, 97, 97)).addGroup(layout.createSequentialGroup().addGap(63, 63, 63).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(out_Message).addContainerGap()).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(jLabel3).addContainerGap()).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(messageText, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE).addContainerGap()).addGroup(layout.createSequentialGroup().addComponent(jLabel1).addGap(58, 58, 58).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(i_filename, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(29, 29, 29).addComponent(fileChooseButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addComponent(li_columns_scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(35, 35, 35)))))));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(36, 36, 36).addComponent(jLabel1)).addGroup(layout.createSequentialGroup().addGap(22, 22, 22).addComponent(li_columns_scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))).addGap(25, 25, 25).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(jLabel3).addGap(27, 27, 27).addComponent(out_Message).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE).addComponent(messageText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(i_filename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(fileChooseButton)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(exportButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(34, 34, 34)));

        messageText.getAccessibleContext().setAccessibleName("errorText");

        getAccessibleContext().setAccessibleName("exportDialog");

        pack();
    }

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {

        out_Message.setText("..");
        out_Message.setVisible(false);


        Object selCols[] = li_columnList.getSelectedValues();
        if (selCols == null || selCols.length == 0) {
            out_Message.setVisible(true);
            out_Message.setText(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_SELECT_FIELDS"));
            System.out.println("Bitte Felder für den Export auswählen !");
            return;
        }

        /*
        Vector<String> vSelCols = li_columnList.getListData();
         */


        String fileName = i_filename.getText();
        if (StringTools.isEmptyString(fileName)) {
            out_Message.setVisible(true);
            out_Message.setText(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_INPUT_FILE"));
            System.out.println("Bitte Datei angeben !");
            return;
        }
        if (!(fileName.endsWith("shp") || fileName.endsWith("SHP"))) {
            fileName += ".shp";
        }


        // selCols -> vSelCols
        Vector<String> vSelCols = new Vector<String>(selCols.length);
        for (Object selCol : selCols) {
            vSelCols.add((String) selCol);
        }

        try {
            // export the table
            ProxyTableModel tableModel = this.shapefileLayerProperties.getTableModel();
            CoordinateReferenceSystem crs = this.shapefileLayerProperties.getLayer().getCRS();
            ShapeFactory.createShape(tableModel, crs, vSelCols, fileName);
            System.out.println(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_OK"));
            out_Message.setVisible(true);
            out_Message.setText(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_EXPORT_OK"));
            cancelButton.setLabel(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_END"));
        } catch (Exception e) {
            out_Message.setVisible(true);
            out_Message.setText("Fehler aufgetreten: " + e.getMessage());
            System.out.println("Fehler aufgetreten: " + e.getMessage());
            return;
        }
    }

    private void fileChooseButtonActionPerformed(java.awt.event.ActionEvent evt) {

        fileChooser.setVisible(true);
        int returnVal = fileChooser.showSaveDialog(this.getRootPane());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File theFile = fileChooser.getSelectedFile();
                if (theFile != null) {
                    i_filename.setText(theFile.getCanonicalPath());
                }
            } catch (Exception e) {
                System.err.println("exception:" + e);
            }
        }
        //fileChooser.setVisible(false);
        System.out.println("FileChooser invisible.");
        return;
    }

    private void setupComponents() {

        // populate columnList
        String[] columnNames = this.shapefileLayerProperties.getTableModel().getColumnNames();
        Vector<String> listData = new Vector<String>();
        for (String columnName : columnNames) {
            listData.add(columnName);
        }
        li_columnList.setListData(listData);

        // init fileChooser
        fileChooser.setDialogTitle(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_SAVE_FILE"));
        fileChooser.setFileFilter(new FileFilter() {

            public boolean accept(File f) {
                return f.isDirectory() || f.getPath().endsWith("shp") || f.getPath().endsWith("SHP");
            }

            public String getDescription() {
                return java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_SHAPEFILES");
            }
        });

        // hide message
        out_Message.setVisible(false);


        // cancel button
        cancelButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        // close window
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        doClose(RET_CANCEL);
    }

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {
        doClose(RET_CANCEL);
    }

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }
    // Variables declaration
    private java.awt.Button cancelButton;

    private java.awt.Button exportButton;

    private javax.swing.JButton fileChooseButton;

    private JList li_columnList;

    private JScrollPane li_columns_scrollPane;

    private java.awt.TextField i_filename;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel3;

    private java.awt.Label messageText;

    private javax.swing.JLabel out_Message;
    // End of variables declaration

    private int returnStatus = RET_CANCEL;

}
