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
package jams.components.gui.spreadsheet;

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
import jams.io.JAMSTableDataArray;
import jams.model.*;



//import jams.components.*;
//import org.unijena.jams.model;
/*
 *
 * @author Robert Riedel
 */
public class JAMSSpreadSheet extends JAMSGUIComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Column Name Array")
    public Attribute.StringArray headers;
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
    private final String title = "JAMSSpreadSheet v0.94";
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
    private JCheckBox onthefly = new JCheckBox("On the fly", false);
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
    private String[] calclist = {"Sum    ",
        "Mean   "};
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
        this.headers = getModel().getRuntime().getDataFactory().createStringArray();
        this.headers.setValue(headers);
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

    public void importWSFile() {
        final String CONTEXT = "@context";
        final String JAMSDATADUMP = "#JAMSdatadump";

        final String ATTRIBUTES = "@attributes";
        final String TYPES = "@types";
        final String DATA = "@data";
        final String START = "@start";
        final String END = "@end";

        final String ID = "ID";
        final String TIME = "JAMSCalendar";

        final String METADATA = "@metadata";
        final String TYPE_ = "#TYPE_";

        BufferedReader bReader;
        String text = "";
        String rowtext = "";
        String itemtext = "";
        String[] headerBuff = new String[256];
        int colNumber = 0;
        double[] rowBuffer = null;
        ArrayList<String> header_list = new ArrayList<String>();
        String[] headers = {""};
        String[] types;

        Vector<double[]> arrayVector = new Vector<double[]>();
        Vector<Attribute.Calendar> timeVector = new Vector<Attribute.Calendar>();

        boolean headerSet = false;
        int line = 0;
        int k = 0;
        this.tmodel = new JAMSTableModel();
        tmodel.setTimeRuns(false);
        this.timeRuns = false;

        int timeIndex = -1;
        int idIndex;

        try {


            JFileChooser chooser = new JFileChooser(); //ACHTUNG!!!!!!!!!
            int returnVal = chooser.showOpenDialog(panel);

            if (returnVal == chooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                FileReader fReader = new FileReader(file);
                bReader = new BufferedReader(fReader);
            } else {
                throw new FileNotFoundException();
            }

            StringBuffer stBuff = new StringBuffer();
            char[] c = new char[100];
            int i;



//            while(fReader.ready()){
//                i = fReader.read(c,0,c.length);
//                stBuff.append(c,0,i);
//            }
            //
            //text = stBuff.toString();







            /* Tokenizers */

            if (bReader.ready()) {
                //Read lines
                rowtext = bReader.readLine();

                //TIME-LOOP ///////////////////////////////////////////////////////
                if (rowtext.compareTo(CONTEXT) == 0) {

                    while (bReader.ready()) {
                        rowtext = bReader.readLine();

                        //ATTRIBUTES //////////////////////////////////////////////////////
                        if (rowtext.compareTo(ATTRIBUTES) == 0) {

                            //read attribute line
                            rowtext = bReader.readLine();
                            StringTokenizer attributes = new StringTokenizer(rowtext, "\t");

                            //read headers
                            String att_name = "";
                            while (attributes.hasMoreTokens()) {

                                try {
                                    att_name = attributes.nextToken();
                                    header_list.add(att_name);
                                    if (att_name.compareTo(ID) == 0) {
                                        idIndex = colNumber;
                                    }
                                    colNumber++;
                                } catch (NullPointerException npe) {
                                }
                            }

                            //write headers in array
                            colNumber = header_list.size();
                            headers = new String[colNumber];
                            header_list.toArray(headers);

                        }

                        //TYPES //////////////////////////////////////////////////////////
                        if (rowtext.compareTo(TYPES) == 0) {

                            types = new String[colNumber];

                            //read types line
                            rowtext = bReader.readLine();
                            StringTokenizer typerow = new StringTokenizer(rowtext, "\t");
                            int c_types = 0;

                            //read and write headers
                            String type_name = "";
                            while (typerow.hasMoreTokens()) {

                                try {
                                    type_name = typerow.nextToken();
                                    types[c_types] = type_name;
                                    if (type_name.compareTo(TIME) == 0) {
                                        timeIndex = c_types;
                                        tmodel.setTimeRuns(true);
                                        this.timeRuns = true;
                                    }
                                    c_types++;
                                } catch (NullPointerException npe) {
                                }
                            }
                        }

                        //DATA //////////////////////////////////////////////////////////
                        if (rowtext.compareTo(DATA) == 0) {

                            //read data line
                            rowtext = bReader.readLine();

                            //START /////////////////////////////////////////////////////
                            if (rowtext.compareTo(START) == 0) {

                                //read DATA
                                rowtext = bReader.readLine();
                                while (rowtext.compareTo(END) != 0) {

                                    StringTokenizer datarow = new StringTokenizer(rowtext, "\t");
                                    rowBuffer = new double[colNumber - 1];

                                    for (int col = 0; col < colNumber; col++) {

                                        //timeVector
                                        if (col == timeIndex && timeRuns) {
                                            Attribute.Calendar timeval = getModel().getRuntime().getDataFactory().createCalendar();
                                            timeval.setValue(datarow.nextToken());
                                            timeVector.add(timeval);
                                        } //rowArray
                                        else {
                                            if (col > timeIndex) {
                                                rowBuffer[col - 1] = new Double(datarow.nextToken());
                                            } else {
                                                rowBuffer[col] = new Double(datarow.nextToken());
                                            }
                                        }
                                    }
                                    arrayVector.add(rowBuffer);
                                    rowtext = bReader.readLine();
                                }
                            }
                        }
                    }
                }

                //DUMP FILE /////////////////////////////////////////////////////////////
                if (rowtext.compareTo(JAMSDATADUMP) == 0) {

                    System.out.println("JAMSdatadump");

                    while (bReader.ready()) {
                        rowtext = bReader.readLine();

                        //METADATA AND HEADERS //////////////////////////////////////////////
                        if (rowtext.compareTo(METADATA) == 0) {

                            //read attribute line
                            rowtext = bReader.readLine();
                            StringTokenizer attributes = new StringTokenizer(rowtext, "\t");

                            //read headers
                            String att_name = "";
                            while (attributes.hasMoreTokens()) {



                                try {
                                    att_name = attributes.nextToken();
                                    header_list.add(att_name);
//                            if(att_name.compareTo(ID) == 0) idIndex = colNumber;
//                            colNumber++;

//                            System.out.println("col_number: "+colNumber);
                                    System.out.println("attribute name: " + att_name);

                                } catch (NullPointerException npe) {
                                }
                            }


                            //write headers in array
                            colNumber = header_list.size();
                            headers = new String[colNumber];
                            header_list.toArray(headers);
                            headerSet = true;

                            System.out.println("col_number: " + colNumber);

                        }

                        //TYPES ////////////////////////////////////////////////////////////
//            if(rowtext.compareTo(TYPE_) == 0){
//
//                    types = new String[colNumber];
//
//                    //read types line
//                    rowtext = bReader.readLine();
//                    StringTokenizer typerow = new StringTokenizer(rowtext,"\t");
//                    int c_types = 0;
//
//                    //read and write headers
//                    String type_name = "";
//                    while(typerow.hasMoreTokens()){
//
//                        try{
//                            type_name = typerow.nextToken(); 
//                            types[c_types] = type_name;
//                           
//                                timeIndex = 0;
//                                tmodel.setTimeRuns(true);
//                                this.timeRuns = true;
//                            
//                            c_types++;
//                        }catch(NullPointerException npe){}
//                    }
//                }

                        //DATA //////////////////////////////////////////////////////////
                        if (rowtext.compareTo(DATA) == 0) {

                            JAMSTableDataArray jamstda;
                            //read DATA
                            rowtext = bReader.readLine();
                            while (rowtext.compareTo(END) != 0) {

                                jamstda = new JAMSTableDataArray(rowtext);
                                timeVector.add(jamstda.getTime());

                                String[] theValues = jamstda.getValues();
                                if (theValues != null) {
                                    rowBuffer = new double[theValues.length];
                                    for (int j = 0; j < theValues.length; j++) {
                                        rowBuffer[j] = new Double(theValues[j]);
                                    }
                                }

                                arrayVector.add(rowBuffer);
                                rowtext = bReader.readLine();
                            }
                        }
                    }
                }
            }


            this.tmodel = new JAMSTableModel();
            tmodel.setTimeRuns(true);
            timeRuns = true;
            //if(headers != null){

            //}
            if (timeRuns) {
                tmodel.setTimeVector(timeVector);
            }

            tmodel.setNewDataVector(arrayVector);
            tmodel.setColumnNames(headers);

        } catch (Exception ex) {
            System.out.println("Exception:" + ex.getMessage());
            ex.printStackTrace();

            try {
                GUIHelper.showErrorDlg(getModel().getRuntime().getFrame(), "File reading failed", "Error");
            } catch (NullPointerException npe) {
                GUIHelper.showErrorDlg(parent_frame, "File reading failed", "Error");
            }
            System.out.println("Lesen fehlgeschlagen!");
        }

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
        Vector<Attribute.Calendar> timeVector = new Vector<Attribute.Calendar>();

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
                                Attribute.Calendar timeval = getModel().getRuntime().getDataFactory().createCalendar();
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
                result = GUIHelper.showYesNoCancelDlg(getModel().getRuntime().getFrame(), "Do you want to save this sheet before?", "Attention");
            } catch (NullPointerException npe) {
                result = GUIHelper.showYesNoCancelDlg(parent_frame, "Do you want to save this sheet before?", "Attention");
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
    ActionListener importAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {

            int result;

            try {
                result = GUIHelper.showYesNoCancelDlg(getModel().getRuntime().getFrame(), "Do you want to save this sheet before?", "Attention");
            } catch (NullPointerException npe) {
                result = GUIHelper.showYesNoCancelDlg(parent_frame, "Do you want to save this sheet before?", "Attention");
            }
            if (result == JOptionPane.YES_OPTION) {
                save();
                importWSFile();
            }
            if (result == JOptionPane.NO_OPTION) {
                importWSFile();
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
        /* achtung: nur wenn time mitläuft!! */
        JTSConfigurator jts;



        try {
            jts = new JTSConfigurator(getModel().getRuntime().getFrame(), this.table);
        } catch (NullPointerException npe) {
            jts = new JTSConfigurator(parent_frame, this.table);
        }

//        SwingUtilities.invokeLater( new Runnable(){
//            public void run()
//            {
//                bar.setValue(K);
//                }
//            } );  

        //ctstabs.addGraph(table);
        //ctsIsOpen = true;
        }

    private void openCXYS() {
        JXYConfigurator jxys;

        try {
            jxys = new JXYConfigurator(getModel().getRuntime().getFrame(), this.table);
        } catch (NullPointerException npe) {
            jxys = new JXYConfigurator(parent_frame, this.table);
        }
    }
    ActionListener plotAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {

//             if(table.getValueAt(0, 0).getImplementingClass() == Attribute.Calendar.class){
//                openCTS();


            try {
                openCTS();
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
                Class test = table.getValueAt(0, table.getSelectedColumns()[0]).getClass();
                if (test == Attribute.Calendar.class) {
                    table.setColumnSelectionInterval(1, table.getColumnCount() - 1);

                }
                openCXYS();

            } catch (ClassCastException cce) {

                if (timeRuns) {
                    table.setColumnSelectionInterval(1, table.getColumnCount() - 1);
                    openCXYS();
                }
            }
//             Class test = table.getValueAt(0, table.getSelectedColumns()[0]).getImplementingClass();
//             if(test == org.unijena.jams.data.Attribute.Calendar.class){
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

//////    public void createPanel() {
    public void createPanel() {
//////
//////        /* PANEL */

//////        //this.panel = new JPanel();
//////
//////        panel.setLayout(panellayout);
        panel.setLayout(new BorderLayout(10, 10));
        JPanel controlpanel = new JPanel();
        JPanel helperpanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        controlpanel.setLayout(gbl);
        JPanel headerpanel = new JPanel();
        headerpanel.setLayout(new GridLayout(1, 2));

        scrollpane.setVerticalScrollBar(new JScrollBar(JScrollBar.VERTICAL));
        scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        scrollpane2 = new JScrollPane(scrollpane);
//////        tmodel = new JAMSTableModel();
        //tmodel = new JAMSTableModel();
//////        //hier platz für time schaffen!!!!!!!!!!!!
//////        //
        if (time != null) {
            tmodel.setTimeRuns(true);
            String[] colheads = new String[headers.getValue().length + 1];
            colheads[0] = "Time [yyyy-mm-dd hh:ss]";
            //schleife durch arraycopy ersetzen!!
            for (int i = 1; i <= headers.getValue().length; i++) {
                colheads[i] = headers.getValue()[i - 1];
            }
            setColumnNameArray(colheads);
        } else {
            setColumnNameArray(headers.getValue());
        }
        //setColumnNameArray(headers.getValue());
        makeTable();
        //panel.add(scrollpane,grid);

        GUIHelper.addGBComponent(controlpanel, gbl, importbutton, 0, 0, 1, 1, 0, 0);

        GUIHelper.addGBComponent(controlpanel, gbl, openbutton, 0, 2, 1, 1, 0, 0);
        GUIHelper.addGBComponent(controlpanel, gbl, savebutton, 0, 3, 1, 2, 0, 0);

        GUIHelper.addGBComponent(controlpanel, gbl, plotButton, 0, 5, 1, 1, 0, 0);
        GUIHelper.addGBComponent(controlpanel, gbl, dataplotButton, 0, 6, 1, 1, 0, 0);

        GUIHelper.addGBComponent(controlpanel, gbl, onthefly, 0, 7, 1, 1, 0, 0);

//              controlpanel.add(openbutton);
//              controlpanel.add(savebutton);
//              controlpanel.add(onthefly);
//              controlpanel.add(plotButton);
//              controlpanel.add(dataplotButton);

        //openbutton.setEnabled(false);
        importbutton.addActionListener(importAction);
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

    public void run() {
        //System.out.println("precip: "+value.toString());
        /*for time steps?*/


        if (time == null) {

            rowdata = new double[rowarray.length]; /* performance */
            for (int i = 0; i < rowarray.length; i++) {
                rowdata[i] = rowarray[i].getValue();
            }
            timeRuns = false;
        } else {
            timeRuns = true;
            //TODO: normal im rowdata abspeichern und alles im table model verwalten
//                rowdata = new double[rowarray.length];
//
//                for(int i = 0; i < rowarray.length; i++){
//                    rowdata[i] = rowarray[i].getValue();
//                }

            addCurrentTime();
        }

        tmodel.addRowArray(rowarray);



        if (onthefly.isSelected() == true) {
            updateGUI();
        }

    }

    public void cleanup() {
        updateGUI();
        // panel.removeAll();
        //TODO: cleanup tmodel
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
