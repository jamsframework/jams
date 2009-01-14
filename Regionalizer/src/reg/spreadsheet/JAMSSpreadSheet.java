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

import java.util.Vector;
import java.util.StringTokenizer;
import java.awt.event.*;
import java.awt.*;
import java.awt.Cursor.*;
import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
import java.util.ArrayList;
import jams.data.*;
import jams.model.*;


import jams.gui.LHelper;
import jams.workspace.DataSet;
import jams.workspace.datatypes.DataValue;
import jams.workspace.datatypes.DoubleValue;
import jams.workspace.datatypes.StringValue;
import jams.workspace.stores.TSDataStore;
import java.text.ParseException;
import reg.Regionalizer;

//import jams.components.*;
//import org.unijena.jams.model;
/*
 *
 * @author Robert Riedel
 */
public class JAMSSpreadSheet extends JAMSGUIComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current time")
    public JAMSCalendar time;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Array with data of one row")
    public JAMSDouble[] rowarray;
    /* TESTING VARIABLES */
    //private String[] columnNameArray = headers.getValue();
    //{"test1","test2"};
    
    File templateFile;
    
    private final String title = "";
    private final int COLWIDTH = 8;
    private JPanel panel = new JPanel();
    private String panelname = "spreadsheet";
    private int numberOfColumns = 0;
    private JFrame parent_frame;
    //runtime time check
    private boolean timeRuns = false;
    //private Runnable updateTable;
    private boolean ctsIsOpen = false;
    //private CTSViewer ctstabs;
    //Vectors
    private Vector<Vector> data = new Vector<Vector>();
    private Vector<String> colnames = new Vector<String>();
    private double[] rowdata;
    /* *** Graphical components *** */
    GridBagLayout panellayout = new GridBagLayout();
    GridBagConstraints grid = new GridBagConstraints();
    private JScrollPane scrollpane = new JScrollPane();
    private JScrollPane scrollpane2;
    /* Buttons */
    private JButton calcbutton = new JButton("Calculate");
    private JButton importbutton = new JButton("Import");
    private JButton openbutton = new JButton("Open");
    private JButton savebutton = new JButton("Save");
    private JButton plotButton = new JButton("Time Plot");
    private JButton dataplotButton = new JButton("Data Plot");
    /* Labels */
    private JLabel headerlabel = new JLabel();
    private JLabel titleLabel = new JLabel(title);
    private JLabel calclabel = new JLabel("calclabel");
    private JLabel label2 = new JLabel();
    private JLabel opensavealert = new JLabel("");
    private JLabel plotalert = new JLabel();
    /*CheckBox*/
    /* TextFields */
    private JTextField editField = new JTextField();
    /* Table and TableModel */
    private JAMSTableModel tmodel;
    private JTableHeader tableHeader;
    //private MouseListener mouseListener;
    //private MouseAdapter mouseAdapter;
    JTable table;
    /* ComboBox */
    /* String array contains words of the ComboBox */
    private String[] calclist = {"Sum    ", "Mean   "};
    JComboBox calculations = new JComboBox(calclist);
    private int kindofcalc = 0;

    /* Plot-Test */
    //private ExtendedTSPlot tsplot;
    /* ActionEvents */
    //private ActionListener buttonclicked;
    /* Testvalues
     */
    /* Constructor */
    public JAMSSpreadSheet() {
    }

    public JAMSSpreadSheet(JFrame parent, String[] headers) {
        this.parent_frame = parent;
    }

    /* Methods */
    public JPanel getPanel() {
        //createPanel();
        return panel;
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

        try {

            JFileChooser chooser = new JFileChooser(); //ACHTUNG!!!!!!!!!
            int returnVal = chooser.showSaveDialog(panel);
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

    public void loadTSDS(TSDataStore store, File inputDSDir) throws Exception {

        int colNumber = 0;
        double[] rowBuffer;
        String[] headers;

        templateFile = new File(inputDSDir, store.getID() + ".tpl");
        
        Vector<double[]> arrayVector = new Vector<double[]>();
        Vector<JAMSCalendar> timeVector = new Vector<JAMSCalendar>();

        tmodel = new JAMSTableModel();
        tmodel.setTimeRuns(false);

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

            JAMSCalendar timeval = new JAMSCalendar();
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

    public void open() {


        String text = "";
        String rowtext = "";
        String itemtext = "";
        String[] headerBuff = new String[256];
        int colNumber = 0;
        double[] rowBuffer = null;
        String[] headers = null;

        Vector<double[]> arrayVector = new Vector<double[]>();
        Vector<JAMSCalendar> timeVector = new Vector<JAMSCalendar>();

        boolean headerSet = false;
        int line = 0;
        int k = 0;
        this.tmodel = new JAMSTableModel();
        tmodel.setTimeRuns(true);
        this.timeRuns = true;

        try {


            JFileChooser chooser = new JFileChooser(); //ACHTUNG!!!!!!!!!
            int returnVal = chooser.showOpenDialog(panel);
            File file = chooser.getSelectedFile();
            FileReader fReader = new FileReader(file);

            StringBuffer stBuff = new StringBuffer();
            char[] c = new char[100];
            int i;


            while (fReader.ready()) {
                i = fReader.read(c, 0, c.length);
                stBuff.append(c, 0, i);
            }
            fReader.close();
            text = stBuff.toString();




        } catch (IOException ex) {
            /* FEHLERMELDUNG */
            System.out.println("Lesen fehlgeschlagen!");
        }


        /* Tokenizers */

        StringTokenizer row = new StringTokenizer(text, "\r\n");
        while (row.hasMoreTokens()) {

            rowtext = row.nextToken();
            StringTokenizer item = new StringTokenizer(rowtext, "\t");


            while (item.hasMoreTokens()) {

                itemtext = item.nextToken();

                try {
                    if (line == 0) {

                        headerBuff[k] = itemtext;
                        colNumber++;

                    } else {

                        if (line == 1) { /* headers[k-1] != null &&  */
                            headers = new String[colNumber];
                            for (int l = 0; l < colNumber; l++) {
                                headers[l] = headerBuff[l];
                            }
                            //setColumnNameArray(colnames);
                            rowBuffer = null;

                        } else {

                            if (k == 0) {
                                JAMSCalendar timeval = new JAMSCalendar();
                                timeval.setValue(itemtext);
                                timeVector.add(timeval);

                            } else {
                                rowBuffer[k - 1] = new Double(itemtext);

                            }
                        }
                    }
                } catch (NullPointerException ne) {
                }



                k++;
            }


            if (rowBuffer != null) {

                arrayVector.add(rowBuffer);
            }
            rowBuffer = new double[colNumber - 1];
            ; //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

            k = 0;
            line++;
        }

        this.tmodel = new JAMSTableModel();
        tmodel.setTimeRuns(true);
        timeRuns = true;
        //if(headers != null){

        //}
        tmodel.setTimeVector(timeVector);

        tmodel.setNewDataVector(arrayVector);
        String headertest = "";

        tmodel.setColumnNames(headers);


        updateGUI();





    }
    /* Open */
    ActionListener openAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {

            int result;

            try {
                result = LHelper.showYesNoCancelDlg(getModel().getRuntime().getFrame(), "Do you want to save this sheet before?", "Attention");
            } catch (NullPointerException npe) {
                result = LHelper.showYesNoCancelDlg(parent_frame, "Do you want to save this sheet before?", "Attention");
            }
            if (result == JOptionPane.YES_OPTION) {
                save();
                open();
            }
            if (result == JOptionPane.NO_OPTION) {
                open();
            }


        /*             YesNoDlg yesnodialog = new YesNoDlg(getModel().getRuntime().getFrame(), "Do you want to save this sheet before?");
        yesnodialog.setVisible(true);
         *
         *
        
        if(yesnodialog.getResult().equals("Yes")){
        save();
        open();
        }
        if(yesnodialog.getResult().equals("No")){
        open();
        }
         */


        }
    };

    private void openCTS() {
        /* achtung: nur wenn time mitlÃƒÆ’Ã‚Â¤uft!! */
        JTSConfigurator jts;
        jts = new JTSConfigurator(Regionalizer.getRegionalizerFrame(), this.table);
    //ctstabs.addGraph(table);
    //ctsIsOpen = true;
    }
    
    private void openCTS(File templateFile) {
        /* achtung: nur wenn time mitlÃƒÆ’Ã‚Â¤uft!! */
        JTSConfigurator jts;
        jts = new JTSConfigurator(Regionalizer.getRegionalizerFrame(), this.table, templateFile);
    //ctstabs.addGraph(table);
    //ctsIsOpen = true;
    }

    private void openCXYS() {
        JXYConfigurator jxys;

        try {
            jxys = new JXYConfigurator(Regionalizer.getRegionalizerFrame(), this.table);
        } catch (NullPointerException npe) {
            jxys = new JXYConfigurator(Regionalizer.getRegionalizerFrame(), this.table);
        }
    }
    
    private void openCXYS(File templateFile) {
        JXYConfigurator jxys;

//        try {
            jxys = new JXYConfigurator(Regionalizer.getRegionalizerFrame(), this.table, templateFile);
//        } catch (NullPointerException npe) {
//            jxys = new JXYConfigurator(Regionalizer.getRegionalizerFrame(), this.table);
//        }
    }
    
    ActionListener plotAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {

//             if(table.getValueAt(0, 0).getClass() == JAMSCalendar.class){     
//                openCTS();
            try {
                
                openCTS(templateFile);
                
            } catch (ClassCastException cce) {
                if (timeRuns) {
                    table.setColumnSelectionInterval(1, table.getColumnCount() - 1);
                    openCTS();
                }
            }
//             }
        }
    };
    ActionListener dataplotAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {


            try {
                
                openCXYS(templateFile);

            } catch (ClassCastException cce) {

                if (timeRuns) {
                    table.setColumnSelectionInterval(1, table.getColumnCount() - 1);
                    openCXYS(templateFile);
                }
            }
//             Class test = table.getValueAt(0, table.getSelectedColumns()[0]).getClass();
//             if(test == org.unijena.jams.data.JAMSCalendar.class){
//                 
//             } else {
//                openCXYS();
//             }
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

    //label2.setText("first element: "+(Double)table.getValueAt(rows[0], columns[0]));
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

    public void addRowArray(double[] data) {
        //
        //tmodel.addRowArray(data);
    }
    /*
    public void addValue(double value, int columnIndex){
    // if (columnIndex < numberOfColumns){                       //hier noch else-Verhalten!!
    //data.get(columnIndex).addElement("test"+value);
    tmodel.addValue(value, columnIndex);
    //}
    }
     */

    public void updateValue(double value, int rowIndex, int columnIndex) {
        //tmodel.setValueAt(value, rowIndex, columnIndex);
        //tmodel.getValueAt(value, rowIndex, columnIndex);
    }

    public void updateGUI() {


        //makeTable();
        table.setModel(tmodel);
        scrollpane.setViewportView(table);

        panel.repaint();
    //panel.remove(table);
    //updateTable();
    //add(updateTable(), grid);
    //this.add(scrollpane);
    //panel.updateUI();
    //createPanel();
    }

    public void makeTable() {


        this.table = new JTable(this.tmodel);

        this.tableHeader = table.getTableHeader();
        table.getTableHeader().setReorderingAllowed(false);
        HeaderHandler mouseListener = new HeaderHandler();
        tableHeader.addMouseListener(mouseListener);
        //tableHeader.setMinimumSize(new Dimension(table.getColumnCount()*20,10));

//                    for(int cc = 0; cc < table.getColumnCount(); cc++){
//                        table.getColumnModel().getColumn(cc).setMinWidth(COLWIDTH);
//                    }

        //table.setMinimumSize(new Dimension(table.getColumnCount()*20,600));
        //this.scrollpane = new JScrollPane(table);
        //better than new instance
        //scrollpane.repaint();
        //scrollpane.setSize(800, 600);

        //panel.add(table, grid);


        //add(scrollpane, grid);
        //scrollpane.updateUI();
        //scrollpane.repaint();

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
        
        //dataplotButton.setEnabled(false);
        
        scrollpane.setVerticalScrollBar(new JScrollBar(JScrollBar.VERTICAL));
        scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        scrollpane2 = new JScrollPane(scrollpane);

        if (time != null) {
            tmodel.setTimeRuns(true);
            setColumnNameArray(new String[0]);
        } else {
            setColumnNameArray(new String[0]);
        }
        //setColumnNameArray(headers.getValue());
        makeTable();
        //panel.add(scrollpane,grid);

        //LHelper.addGBComponent(controlpanel, gbl, openbutton, 0, 2, 1, 1, 0, 0);
        //LHelper.addGBComponent(controlpanel, gbl, savebutton, 0, 3, 1, 2, 0, 0);

        LHelper.addGBComponent(controlpanel, gbl, plotButton, 0, 5, 1, 1, 0, 0);
        LHelper.addGBComponent(controlpanel, gbl, dataplotButton, 0, 6, 1, 1, 0, 0);


//              controlpanel.add(openbutton);
//              controlpanel.add(savebutton);
//              controlpanel.add(onthefly);
//              controlpanel.add(plotButton);
//              controlpanel.add(dataplotButton);

        //openbutton.setEnabled(false);
        openbutton.addActionListener(openAction);
        savebutton.addActionListener(saveAction);
        plotButton.addActionListener(plotAction);
        dataplotButton.addActionListener(dataplotAction);

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

    public void addCurrentTime() {
        tmodel.addTime(time);
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