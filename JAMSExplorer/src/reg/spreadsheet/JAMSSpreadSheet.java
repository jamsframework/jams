/*
 *Description:
 *
 *This component handles the inputdata as a table where you have the 
 *possibility to do fundamental calculations of marked values.
 *There is also an extended plot-function in developement for
 *version 1.00. This will provide online plots directly from the spreadsheet
 *and will have options to change appearance and datasets of the plots
 *after model-run()
 *
 */
package reg.spreadsheet;

import jams.io.JAMSFileFilter;
import java.util.Vector;
import java.awt.event.*;
import java.awt.*;
import java.awt.Cursor.*;
import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
import java.util.ArrayList;
import jams.data.*;


import jams.gui.LHelper;
import jams.workspace.DataSet;
import jams.workspace.datatypes.DataValue;
import jams.workspace.datatypes.DoubleValue;
import jams.workspace.stores.TSDataStore;
import java.text.ParseException;
import reg.JAMSExplorer;
import reg.dsproc.DataMatrix;

//import jams.components.*;
//import org.unijena.jams.model;
/*
 *
 * @author Robert Riedel
 */
public class JAMSSpreadSheet extends JPanel {

    File ttpFile;

    File dtpFile;

    private final String title = "";
    
    private JAMSSpreadSheet thisSpreadSheet;

    private JPanel panel = new JPanel();

    private String panelname = "spreadsheet";

    private int numberOfColumns = 0;

    private JFrame parent_frame;

    private boolean timeRuns = false;

    GridBagLayout panellayout = new GridBagLayout();

    GridBagConstraints grid = new GridBagConstraints();

    private JScrollPane scrollpane = new JScrollPane();

    private JScrollPane scrollpane2;
    /* Buttons */

    private JButton savebutton = new JButton("Save");

    private JButton plotButton = new JButton("Time Plot");

    private JButton dataplotButton = new JButton("Data Plot");
    
    private JButton closeButton = new JButton("Close Tab");

    private JCheckBox useTemplateButton = new JCheckBox("use Template");

    private JButton stpButton = new JButton("Stacked Time Plot");
    /* Labels */

    private JLabel headerlabel = new JLabel();

    private JLabel titleLabel = new JLabel(title);

    private JLabel calclabel = new JLabel("calclabel");
    /* Table and TableModel */

    private JAMSTableModel tmodel;

    private JTableHeader tableHeader;

    private TSDataStore store;

    JTable table;
    /* ComboBox */
    /* String array contains words of the ComboBox */

    private String[] calclist = {"Sum    ", "Mean   "};

    JComboBox calculations = new JComboBox(calclist);

    private int kindofcalc = 0;

    private JFileChooser epsFileChooser,  templateChooser;

    private JAMSExplorer regionalizer;

    /* Constructor */
    public JAMSSpreadSheet() {
    }

    public JAMSSpreadSheet(JAMSExplorer regionalizer, String[] headers) {
        this.regionalizer = regionalizer;
        this.parent_frame = regionalizer.getRegionalizerFrame();
        this.thisSpreadSheet = this;
    }

    /* Methods */
    public JPanel getPanel() {
        //createPanel();
        return panel;
    }

    public TSDataStore getStore() {
        return this.store;
    }

    /* JAMS init() method */
    public void init() {

        /* initializing TableModel */
        tmodel = new JAMSTableModel();

        createPanel();

        updateGUI();
    }
    /************* *** Event Handling *** ****l*****************************/
    ActionListener calcbuttonclick = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            kindofcalc = calculations.getSelectedIndex();

            if (kindofcalc == 0) {
                calclabel.setText("Sum: " + calcsum());
            }
            if (kindofcalc == 1) {
                calclabel.setText("Mean: " + calcmean());
            //label.setText("MEAN");
            }
        }
    };
    /* Save */

    ActionListener saveAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            save();
        }
    };

    public void save() {
        /* Only for Time Series */
        int colcount = tmodel.getColumnCount();
        int rowcount = tmodel.getRowCount();
        String value;
        String[] columnNames = tmodel.getCoulumnNameArray();

        JFileChooser chooser = new JFileChooser(); //ACHTUNG!!!!!!!!!
        int returnVal = chooser.showSaveDialog(panel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            try {

                File file = chooser.getSelectedFile();
                //File file = chooser.getSelectedFile();
                FileWriter filewriter = new FileWriter(file);


                for (int j = 0; j < colcount; j++) {
                    filewriter.write(columnNames[j], 0, columnNames[j].length());
                    filewriter.write("\t");
                }

                filewriter.write("\r\n" + "#");
                filewriter.write("\r\n");

                for (int k = 0; k < rowcount; k++) {
                    for (int i = 0; i < colcount; i++) {

                        value = table.getValueAt(k, i).toString();
                        filewriter.write(value, 0, value.length());
                        filewriter.write("\t");
                    }
                    filewriter.write("\r\n");
                }
                //filewriter.write("#");
                filewriter.close();

            } catch (IOException ex) {
            }
        }
    }

    public JFileChooser getTemplateChooser() {
        if (templateChooser == null) {
            templateChooser = new JFileChooser();
            templateChooser.setFileFilter(JAMSFileFilter.getTtpFilter());
        }
        templateChooser.setFileFilter(JAMSFileFilter.getTtpFilter());
        return templateChooser;
    }

    public JFileChooser getEPSFileChooser() {
        if (epsFileChooser == null) {
            epsFileChooser = new JFileChooser();
            epsFileChooser.setFileFilter(JAMSFileFilter.getEpsFilter());
        }
        return epsFileChooser;
    }
    
    ActionListener closeTabAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            
            regionalizer.getRegionalizerFrame().removeFromTabbedPane(thisSpreadSheet.getPanel());
        }
    };
    
    public void loadMatrix(DataMatrix m, File outputDSDir, boolean timeSeries) {

//        getTemplateChooser().setCurrentDirectory(outputDSDir);
//        getEPSFileChooser().setCurrentDirectory(outputDSDir.getParentFile());
//        ttpFile = new File(inputDSDir, store.getID() + ".ttp");
//        dtpFile = new File(inputDSDir, store.getID() + ".dtp");

        Vector<double[]> arrayVector = new Vector<double[]>();
        Vector<JAMSCalendar> timeVector = new Vector<JAMSCalendar>();
        String[] headers = new String[m.getColumnDimension() + 1];
        double[] rowBuffer, source;
        int pos = 0;

        Object[] ids = m.getIds();

        tmodel = new JAMSTableModel();

        if (timeSeries) {
            timeRuns = true;
            tmodel.setTimeRuns(timeRuns);
        }

        for (Object id : ids) {

            if (timeSeries) {
                JAMSCalendar timeval = JAMSDataFactory.createCalendar();
                timeval.setValue(id.toString());
                timeVector.add(timeval);
                rowBuffer = m.getRow(pos);
            } else {
                rowBuffer = new double[m.getColumnDimension() + 1];
                rowBuffer[0] = Double.parseDouble(id.toString());
                source = m.getRow(pos);
                System.arraycopy(source, 0, rowBuffer, 1, source.length);
            }

            arrayVector.add(rowBuffer);

            pos++;
        }

        if (timeSeries) {
            tmodel.setTimeVector(timeVector);
        }

        for (int i = 0; i < headers.length; i++) {
            headers[i] = "col" + i;
        }

        tmodel.setNewDataVector(arrayVector);
        tmodel.setColumnNames(headers);

        updateGUI();

    }

    public void loadTSDS(TSDataStore store, File inputDSDir) throws Exception {

        this.store = store;

        int colNumber = 0;
        double[] rowBuffer;
        String[] headers;

        getTemplateChooser().setCurrentDirectory(inputDSDir);
        getEPSFileChooser().setCurrentDirectory(inputDSDir.getParentFile());

        ttpFile = new File(inputDSDir, store.getID() + ".ttp");
        dtpFile = new File(inputDSDir, store.getID() + ".dtp");

        Vector<double[]> arrayVector = new Vector<double[]>();
        Vector<JAMSCalendar> timeVector = new Vector<JAMSCalendar>();


        // read table headers from attribute "NAME"
        // @TODO: flexible handling of header attribute
        ArrayList<Object> names = store.getDataSetDefinition().getAttributeValues("NAME");
        colNumber = store.getDataSetDefinition().getColumnCount();
        headers = new String[colNumber + 1];
        headers[0] = "";
        int i = 1;
        for (Object o : names) {
            headers[i++] = (String) o;
        }

        // read table values from store
        while (store.hasNext()) {
            DataSet ds = store.getNext();

            DataValue[] rowData = ds.getData();

            JAMSCalendar timeval = JAMSDataFactory.createCalendar();
            try {
                timeval.setValue(rowData[0].getString(), "dd.MM.yyyy HH:mm");
            } catch (ParseException pe) {
                pe.printStackTrace();
            }
            timeVector.add(timeval);

            rowBuffer = new double[colNumber];
            for (i = 1; i < rowData.length; i++) {
                rowBuffer[i - 1] = ((DoubleValue) rowData[i]).getDouble();
            }
            arrayVector.add(rowBuffer);
        }

        this.tmodel = new JAMSTableModel();
        tmodel.setTimeRuns(true);
        timeRuns = true;
        tmodel.setTimeVector(timeVector);

        tmodel.setNewDataVector(arrayVector);
        tmodel.setColumnNames(headers);

        updateGUI();
    }

    private void openCTS() {

        JTSConfigurator jts;
        jts = new JTSConfigurator(regionalizer.getRegionalizerFrame(), this, null);
    //ctstabs.addGraph(table);
    //ctsIsOpen = true;
    }

    private void openCTS(File templateFile) {

        JTSConfigurator jts;
        if (useTemplateButton.isSelected()) {
            jts = new JTSConfigurator(regionalizer.getRegionalizerFrame(), this, templateFile);
        } else {
            jts = new JTSConfigurator(regionalizer.getRegionalizerFrame(), this, null);
        }
    //ctstabs.addGraph(table);
    //ctsIsOpen = true;
    }

    private void openCXYS() {
        JXYConfigurator jxys;

        try {
            jxys = new JXYConfigurator(regionalizer.getRegionalizerFrame(), this);
        } catch (NullPointerException npe) {
            jxys = new JXYConfigurator(regionalizer.getRegionalizerFrame(), this);
        }
    }

    private void openCXYS(File templateFile) {
        JXYConfigurator jxys;

        if (useTemplateButton.isSelected()) {
            jxys = new JXYConfigurator(regionalizer.getRegionalizerFrame(), this, templateFile);
        } else {
            jxys = new JXYConfigurator(regionalizer.getRegionalizerFrame(), this, null);
        }
    }

    private void openSTP() {
        STPConfigurator stp = new STPConfigurator(regionalizer, 2);
    }
    ActionListener plotAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            
            if (useTemplateButton.isSelected()) {
                if(ttpFile != null){
                    if(ttpFile.exists()){
                        openCTS(ttpFile);
                        
                    } else{
                        
                        try {
                            JFileChooser chooser = getTemplateChooser();
                            int returnVal = chooser.showOpenDialog(parent_frame);
                            if (returnVal == JFileChooser.APPROVE_OPTION) {
                                ttpFile = chooser.getSelectedFile();
                            }
                            openCTS(ttpFile);

                        } catch (Exception fnfex) {
                            
                            if (timeRuns) {
                                table.setColumnSelectionInterval(1, table.getColumnCount() - 1);
                                openCTS();
                            }
                        }
                    }
                }
            } else {
                openCTS();
            }
        }
    };

    ActionListener dataplotAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {

            try {

                openCXYS(dtpFile);

            } catch (ClassCastException cce) {

                if (timeRuns) {
                    table.setColumnSelectionInterval(1, table.getColumnCount() - 1);
                    openCXYS(dtpFile);
                }
            }

        }
    };

    ActionListener stpAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {

//            try {

            openSTP();

//            } catch (ClassCastException cce) {
//
//                if (timeRuns) {
//                    table.setColumnSelectionInterval(1, table.getColumnCount() - 1);
//                    openCXYS(dtpFile);
//                }
//            }

        }
    };

    /*************** Math *******************************/
    private double calcsum() {

        double sum = 0;

        int[] rows = table.getSelectedRows();
        int ix = rows.length;
        int[] columns = table.getSelectedColumns();
        int kx = columns.length;


        //sum= (String) table.getValueAt(rows[0],columns[0]);

        for (int k = 0; k < kx; k++) {

            for (int i = 0; i < ix; i++) {
                //int val=(Integer) table.getValueAt(rows[i], columns[k]);
                //table.getValueAt(rows[i], columns[k])!="-" && 
                if (table.getValueAt(rows[i], columns[k]).getClass() != java.lang.String.class) {
                    sum += (Double) table.getValueAt(rows[i], columns[k]);
                } else {
                    sum += 0;

                }
            }
        }
        return sum;
    }

    private double calcmean() {
        double sum = 0;

        int[] rows = table.getSelectedRows();
        int ix = rows.length;
        int[] columns = table.getSelectedColumns();
        int kx = columns.length;


        //sum= (String) table.getValueAt(rows[0], columns[0]);

        for (int k = 0; k < kx; k++) {

            for (int i = 0; i < ix; i++) {
                //int val=(Integer) table.getValueAt(rows[i], columns[k]);
                if (table.getValueAt(rows[i], columns[k]).getClass() != java.lang.String.class) {
                    sum += (Double) table.getValueAt(rows[i], columns[k]);
                } else {
                    sum += 0;
                }
            }
        }


        double mean = 0;

        if (ix == 1) {
            mean = (double) sum / (double) (kx);
        }

        if (kx == 1) {
            mean = (double) sum / (double) (ix);
        }
        if (kx != 1 && ix != 1) {
            mean = (double) sum / (double) (kx * ix);
        }

        return sum;
    }

    public String getPanelName() {
        String name = this.panelname;
        return name;
    }

    public void setNumberOfColumns(int numberOfColumns) {
        this.numberOfColumns = numberOfColumns;
    }

    public void setColumnNameArray(String[] names) {
        tmodel.setColumnNames(names);
    }

    public void updateGUI() {
        table.setModel(tmodel);
        scrollpane.setViewportView(table);
        panel.repaint();
    }

    public void makeTable() {


        this.table = new JTable(this.tmodel);

        this.tableHeader = table.getTableHeader();
        table.getTableHeader().setReorderingAllowed(false);
        HeaderHandler mouseListener = new HeaderHandler();
        tableHeader.addMouseListener(mouseListener);

        //table.getColumnModel().getColumn(0)
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        table.setDragEnabled(false);
        //table.setSelectionMode(SINGLE SELECTION);
        table.setCellSelectionEnabled(true);
        //return scrollpane;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);



    }

    public void createPanel() {

        panel.setLayout(new BorderLayout(10, 10));
        JPanel controlpanel = new JPanel();
        JPanel helperpanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        controlpanel.setLayout(gbl);
        JPanel headerpanel = new JPanel();
        headerpanel.setLayout(new GridLayout(1, 2));

        useTemplateButton.setEnabled(true);
        useTemplateButton.setSelected(false);
        //dataplotButton.setEnabled(false);

        scrollpane.setVerticalScrollBar(new JScrollBar(JScrollBar.VERTICAL));
        scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        scrollpane2 = new JScrollPane(scrollpane);

        //setColumnNameArray(headers.getValue());
        makeTable();
        //panel.add(scrollpane,grid);

        //LHelper.addGBComponent(controlpanel, gbl, openbutton, 0, 2, 1, 1, 0, 0);
        //LHelper.addGBComponent(controlpanel, gbl, savebutton, 0, 3, 1, 2, 0, 0);
        
        LHelper.addGBComponent(controlpanel, gbl, closeButton, 0, 5, 1, 1, 0, 0);
        LHelper.addGBComponent(controlpanel, gbl, plotButton, 0, 6, 1, 1, 0, 0);
        LHelper.addGBComponent(controlpanel, gbl, dataplotButton, 0, 7, 1, 1, 0, 0);
        LHelper.addGBComponent(controlpanel, gbl, useTemplateButton, 0, 8, 1, 1, 0, 0);
        LHelper.addGBComponent(controlpanel, gbl, stpButton, 0, 9, 1, 1, 0, 0);

//              controlpanel.add(openbutton);
//              controlpanel.add(savebutton);
//              controlpanel.add(onthefly);
//              controlpanel.add(plotButton);
//              controlpanel.add(dataplotButton);

        savebutton.addActionListener(saveAction);
        plotButton.addActionListener(plotAction);
        dataplotButton.addActionListener(dataplotAction);
        stpButton.addActionListener(stpAction);
        closeButton.addActionListener(closeTabAction);

        headerpanel.add(titleLabel);
        headerpanel.add(headerlabel);
        helperpanel.add(controlpanel);

        panel.add(headerpanel, BorderLayout.NORTH);

        panel.add(scrollpane2, BorderLayout.CENTER);
        panel.add(helperpanel, BorderLayout.EAST);


    }

    /* ************** JTable Operations *************** */
    /*
     *Creation of a GridBagConstraints-Object
     */
    private GridBagConstraints makegrid(int xpos, int ypos, int width, int height) {
        GridBagConstraints grid = new GridBagConstraints();
        grid.gridx = xpos;
        grid.gridy = ypos;
        grid.gridwidth = width;
        grid.gridheight = height;
        grid.insets = new Insets(0, 0, 0, 0);
        return grid;
    }

    public void addTime(JAMSCalendar time) {
        tmodel.addTime(time);
    }

    private class HeaderHandler extends MouseAdapter {

        int button = -1;

        int[] selectedColumns;

        int col_START = 0; // is this nessesary?

        int col_END = 0;

        public void mouseClicked(MouseEvent e) {

            JTableHeader h = (JTableHeader) e.getSource();
            TableColumnModel tcm = h.getColumnModel();
            int viewCol = tcm.getColumnIndexAtX(e.getX());

            if (e.isShiftDown()) {
                button = 1;
            } else if (e.isControlDown()) {
                button = 2;
            } else {
                button = -1;
            }

            switch (button) {

                case 1: //SHIFT DOWN
                    col_END = table.getColumnModel().getColumn(viewCol).getModelIndex();
                    //table.setColumnSelectionInterval(col_START,col_END);
                    table.addColumnSelectionInterval(col_START, col_END);
                    break;

//                        
                case 2: //CTRL DOWN
                    selectedColumns = table.getSelectedColumns();
                    col_END = table.getColumnModel().getColumn(viewCol).getModelIndex();
                    table.addColumnSelectionInterval(col_END, col_END);

                    for (int k = 0; k < selectedColumns.length; k++) {

                        if (col_END == selectedColumns[k]) {
                            table.removeColumnSelectionInterval(col_END, col_END);
                            break;
                        }

                    }
                    //table.setColumnSelectionInterval(col_START,col_END);

                    break;

                default:
                    col_START = table.getColumnModel().getColumn(viewCol).getModelIndex();
                    table.setColumnSelectionInterval(col_START, col_START);
            }


            table.setRowSelectionInterval(0, table.getRowCount() - 1);
            button = -1;
        }

        public void mouseEntered(MouseEvent e) {
            JTableHeader h = (JTableHeader) e.getSource();
            h.setCursor(new Cursor(12)); //hand curser
        }

        public void mouseExited(MouseEvent e) {
            JTableHeader h = (JTableHeader) e.getSource();
        //h.setCursor(new Cursor(-1)); //default curser
        }
    }
}