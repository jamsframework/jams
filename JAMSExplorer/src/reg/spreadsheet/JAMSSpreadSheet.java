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

import jams.JAMSFileFilter;
import java.util.Vector;
import java.awt.event.*;
import java.awt.*;
import java.awt.Cursor.*;
import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
import java.util.ArrayList;
import jams.data.*;

import jams.gui.tools.GUIHelper;
import jams.tools.StringTools;
import jams.workspace.DataValue;
import jams.workspace.DefaultDataSet;
import jams.workspace.datatypes.DoubleValue;
import jams.workspace.stores.InputDataStore;
import jams.workspace.stores.ShapeFileDataStore;
import jams.workspace.stores.TSDataStore;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.net.URI;
import java.text.ParseException;
import java.util.StringTokenizer;
import reg.DataTransfer;
import reg.JAMSExplorer;
import reg.dsproc.DataMatrix;
import reg.gui.StatisticDialogPanel;
import reg.viewer.Viewer;

/*
 *
 * @author Robert Riedel
 */
public class JAMSSpreadSheet extends JPanel {

    File ttpFile;
    File dtpFile;
    private final String title = "";
    private JPanel panel = new JPanel();
    private String panelname = "spreadsheet";
    private JFrame parent_frame;
    private boolean timeRuns = false;
    GridBagLayout panellayout = new GridBagLayout();
    GridBagConstraints grid = new GridBagConstraints();
    private JScrollPane scrollpane = new JScrollPane();
    private boolean output_sheet = false;    //private JScrollPane scrollpane2;
    /* Buttons */
    private String name = java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("DEFAULT");
    private JButton savebutton = new JButton(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("SAVE_DATA"));
//    private JButton loadbutton = new JButton("Import Data");
    private JButton statButton = new JButton(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("STATISTIK"));
    private JButton plotButton = new JButton(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("TIME_PLOT"));
    private JButton dataplotButton = new JButton(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("DATA_PLOT"));
    private JButton closeButton = new JButton(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("CLOSE_TAB"));
    private JCheckBox useTemplateButton = new JCheckBox(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("USE_TEMPLATE"));
    private JCheckBox useTransposedButton = new JCheckBox(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("USE_TRANSPOSED"));
    private JButton stpButton = new JButton(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("STACKED_TIME_PLOT"));
    private JComboBox shapeSelector = new JComboBox();

    /* Labels */
    private JLabel headerlabel = new JLabel();
    private JLabel titleLabel = new JLabel(title);
    private JLabel calclabel = new JLabel(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("CALCLABEL"));
    /* Table and TableModel */
    private JAMSTableModel tmodel;
    private JTableHeader tableHeader;
    private TSDataStore store;
    private File outputDSDir;
    JTable table;
    /* ComboBox */
    /* String array contains words of the ComboBox */
    private String[] calclist = {java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("SUM____"), java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("MEAN___")};
    JComboBox calculations = new JComboBox(calclist);
    private int kindofcalc = 0;
    private JFileChooser epsFileChooser, templateChooser, datChooser , savefileChooser;
    private JAMSExplorer explorer;

    /* Messages */
    final String ERR_MSG_CTS = java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("NO_TIME_SERIES_LOADED");
    public static final DataFlavor FLAVOR = DataFlavor.stringFlavor;

    public class TableDataTransferable implements Transferable {

        //TableData myValue;
        String myValue;

        public TableDataTransferable(String value) {
            myValue = value;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{FLAVOR};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor == FLAVOR;
        }

        public Object getTransferData(DataFlavor flavor) throws
                UnsupportedFlavorException, IOException {
            if (flavor == FLAVOR) {
                return myValue;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }
    }

    public class TableHandler extends TransferHandler {

        JTable myTable;

        public TableHandler(JTable table) {
            myTable = table;
            table.setTransferHandler(this);
            table.setDragEnabled(true);

            table.addMouseMotionListener(new MouseMotionListener() {

                public void mouseDragged(MouseEvent e) {
                    e.consume();
                    JComponent c = (JComponent) e.getSource();
                    TransferHandler handler = c.getTransferHandler();
                    handler.exportAsDrag(c, e, TransferHandler.MOVE);
                }

                public void mouseMoved(MouseEvent e) {
                }
            });
        }

        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            return false;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            if (c == myTable) {
                int rows[] = myTable.getSelectedRows();
                int cols[] = myTable.getSelectedColumns();
                TableModel model = myTable.getModel();

                StringBuffer t = new StringBuffer(rows.length * cols.length * 9);
                //first header
                for (int i = 0; i < cols.length; i++) {
                    t.append(model.getColumnName(cols[i]) + "\t");
                }
                t.append("\n");
                for (int j = 0; j < rows.length; j++) {
                    for (int i = 0; i < cols.length; i++) {
                        try {
                            if (i != cols.length - 1) {
                                t.append(((Double) model.getValueAt(rows[j], cols[i])).doubleValue() + "\t");
                            } else {
                                t.append(((Double) model.getValueAt(rows[j], cols[i])).doubleValue());
                            }
                        } catch (Throwable ta) {
                            t.append("0.0");
                        }
                    }
                    if (j != rows.length - 1) {
                        t.append("\n");
                    }
                }
                return new TableDataTransferable(t.toString());
            } else {
                return super.createTransferable(c);
            }
        }

        //not supported!
        /*public boolean importData(JComponent comp, Transferable t) {
        if (comp == myTable) {
        try {
        Object value = t.getTransferData(FLAVOR);

        int row = myTable.getSelectedRow();
        int col = myTable.getSelectedColumn();

        //insert insertion here ..
        return true;
        } catch (Exception e) {
        }
        }
        return super.importData(comp, t);
        }*/
        public int getSourceActions(JComponent c) {
            if (myTable == c) {
                return DnDConstants.ACTION_COPY;
            } else {
                return super.getSourceActions(c);
            }
        }
    }

    public JAMSSpreadSheet(JAMSExplorer explorer) {
        this.explorer = explorer;
        this.parent_frame = (JFrame) explorer.getExplorerFrame();
    }

    private void close() {
        explorer.getDisplayManager().removeDisplay(name);
    }

    public String getID() {
        return name;
    }

    public void setID(String name) {
        this.name = name;
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
                calclabel.setText(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("SUM:_") + calcsum());
            }
            if (kindofcalc == 1) {
                calclabel.setText(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("MEAN:_") + calcmean());
                //label.setText("MEAN");
            }
        }
    };
    /* Save */
    ActionListener saveAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
        // ABSOLUT NEW TEST
               JFileChooser Save = new JFileChooser();
               Save.setSelectedFile(new File("new file"));   
               Save.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
               Save.setCurrentDirectory(new File ("user.dir"));
                 try{
                     boolean dont_save =true;
                     while(dont_save){
                    int rc = Save.showSaveDialog(panel);
                     if (rc != JFileChooser.APPROVE_OPTION ){
                         dont_save=false;
                 
                     }else{
                         String filename =Save.getSelectedFile().getName();
                         setOutputDSDir(Save.getCurrentDirectory());
                             if (!(filename==null)){
                                 filename +=SpreadsheetConstants.FILE_ENDING_DAT;
                                 if(isOutputSheet()){
                                     File file = new File(getOutputDSDir(),filename);
                                     if(!file.exists()){
                                         save(filename,getSaveHeaders());
                                         dont_save=false;
                                     } else {
                                        String fileexists = java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("THE_FILE_") + file + java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("_ALREADY_EXISTS._OVERWRITE?");
                                        int result = GUIHelper.showYesNoDlg(parent_frame, fileexists, java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("FILE_ALREADY_EXISTS"));
                                        if (result == 0) { //overwrite
                                            filename=Save.getSelectedFile().getName();
                                            filename +=SpreadsheetConstants.FILE_ENDING_DAT;
                                          save(filename,getSaveHeaders());
                                           // saveAll(filename);
                                            dont_save = false;
                                         }
                                     }
                                 }else{
                                     File file = new File(explorer.getWorkspace().getDirectory().toString() + "/explorer", filename);
                                     if(!file.exists()){
                                         save(filename,getSaveHeaders());
                                         dont_save=false;
                                     } else {
                                        String fileexists = java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("THE_FILE_") + file + java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("_ALREADY_EXISTS._OVERWRITE?");
                                        int result = GUIHelper.showYesNoDlg(parent_frame, fileexists, java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("FILE_ALREADY_EXISTS"));
                                        if (result == 0) { //overwrite
                                            filename=Save.getSelectedFile().getName();
                                            filename +=SpreadsheetConstants.FILE_ENDING_DAT;
                                          save(filename,getSaveHeaders());
                                           // saveAll(filename);
                                            dont_save = false;
                                         }
                                     }
                                 }
                             }else{
                                 dont_save=false; //Abbruch
                             }
                     }
                     }
                     } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    private String[] getSaveHeaders() {

        int[] selectedColumns = table.getSelectedColumns();
        int[] writeColumns;

        if (timeRuns && selectedColumns[0] == 0) {
            writeColumns = new int[selectedColumns.length - 1];
            for (int i = 0; i < writeColumns.length; i++) {
                writeColumns[i] = selectedColumns[i + 1];
            }
        } else {
            writeColumns = selectedColumns;
        }

        String[] write_headers = new String[writeColumns.length];

        for (int i = 0; i < writeColumns.length; i++) {

            write_headers[i] = table.getColumnName(writeColumns[i]);

        }
        String[] headers_with_time = new String[write_headers.length + 1];
        headers_with_time[0] = java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("ID");
        java.lang.System.arraycopy(write_headers, 0, headers_with_time, 1, write_headers.length);
        System.out.println(headers_with_time[0]+headers_with_time[1]);
        return headers_with_time;
    }
    ActionListener loadAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {

            int returnVal = -1;

            try {

                returnVal = getDatChooser().showOpenDialog(JAMSSpreadSheet.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = getDatChooser().getSelectedFile();
                    load(file);
                }

            } catch (Exception fnfexc) {
                fnfexc.printStackTrace();
                returnVal = -1;
            }
        }
    };
    ActionListener statisticAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {

            String[] headers = getSelectedColumnNames();
            double[][] data = getSelectedData();
            StatisticDialogPanel statPanel = new StatisticDialogPanel(parent_frame, true, headers, data);
            statPanel.setVisible(true);
            statPanel.getReturnStatus();
        }
    };

    public void save(String filename, String[] write_headers) {
        /* Only for Time Series */
        int colcount = tmodel.getColumnCount();
        int rowcount = tmodel.getRowCount();
        int write_col_cnt = write_headers.length;
        int[] col_index = new int[write_col_cnt];
        String value;
        String[] columnNames = tmodel.getCoulumnNameArray();

//        JFileChooser chooser = new JFileChooser(); //ACHTUNG!!!!!!!!!

//        int returnVal = chooser.showSaveDialog(panel);
//        if (returnVal == JFileChooser.APPROVE_OPTION) {

        try {

            File file;
            if (isOutputSheet()) {
            //        file = new File(regionalizer.getWorkspace().getOutputDataDirectory()+filename);
           //     file = new File(regionalizer.getWorkspace().getDirectory().toString() + "/output/current/" + filename);
                file = new File(this.getOutputDSDir().toString() + "/" + filename);
            } else {
         //      file = new File(explorer.getWorkspace().getDirectory().toString() + SpreadsheetConstants.FILE_EXPLORER_DIR_NAME + filename);
                file = new File(this.getOutputDSDir().toString() + SpreadsheetConstants.FILE_EXPLORER_DIR_NAME + filename);
            }

            //File file = chooser.getSelectedFile();
            //File file = chooser.getSelectedFile();
            FileWriter filewriter = new FileWriter(file);
            if(!useTransposedButton.isSelected()){
            filewriter.write(SpreadsheetConstants.LOAD_HEADERS + "\r\n");
            String col_string = "";
            for (int j = 0; j < colcount; j++) {
                col_string = columnNames[j];
                for (int c = 0; c < write_col_cnt; c++) {

                    if (col_string.compareTo(write_headers[c]) == 0) {
                        if (c == write_col_cnt - 1) {
                            filewriter.write(columnNames[j], 0, columnNames[j].length());
                            col_index[c] = j;
                        } else {
                            filewriter.write(columnNames[j], 0, columnNames[j].length());
                            filewriter.write("\t");
                            col_index[c] = j;
                        }
                    }

                }
            }

            filewriter.write("\r\n" + SpreadsheetConstants.LOAD_DATA);
            filewriter.write("\r\n");
              for (int k = 0; k < rowcount; k++) {
//                        value = table.getValueAt(k, 0).toString();//timeRow
//                        filewriter.write(value, 0, value.length());
//                        filewriter.write("\t");
                for (int i = 0; i < write_col_cnt; i++) {

                    if (i == write_col_cnt - 1) {
                        value = table.getValueAt(k, col_index[i]).toString();
                        filewriter.write(value, 0, value.length());
//                            filewriter.write("\t");
                    } else {

                        value = table.getValueAt(k, col_index[i]).toString();
                        filewriter.write(value, 0, value.length());
                        filewriter.write("\t\t");
                    }
                }
                filewriter.write("\r\n");
            }
            filewriter.write(SpreadsheetConstants.LOAD_END);
            filewriter.close();

            } else{ //AB hier Das gleich nur Transponiert

               
                filewriter.write(SpreadsheetConstants.LOAD_HEADERS + "\r\n");

            String col_string = "";
            for (int j = 0; j < colcount; j++) {
                col_string = columnNames[j];
                for (int c = 0; c < write_col_cnt; c++) {

                    if (col_string.compareTo(write_headers[c]) == 0) {
                        if (c == write_col_cnt - 1) {
                            filewriter.write(columnNames[j], 0, columnNames[j].length());
                            filewriter.write("\r\n");
                            col_index[c] = j;
                        } else {
                            filewriter.write(columnNames[j], 0, columnNames[j].length());
                            filewriter.write("\r\n");
                            col_index[c] = j;
                        }
                    }

                }
            }
        
            filewriter.write("\r\n" + SpreadsheetConstants.LOAD_DATA + " NR.2");
            filewriter.write("\r\n");
            System.out.println("rowcount =" + rowcount + "  write_col_cnt =" + write_col_cnt);
            for (int i = 0;i< write_col_cnt; i++){
                    for (int k = 0 ; k < rowcount ; k++){
                            value =table.getValueAt(k, col_index[i]).toString();
                            filewriter.write(value, 0 , value.length());
                            filewriter.write("\t");
                    }
                    filewriter.write("\r\n");
            }




            filewriter.write(SpreadsheetConstants.LOAD_END);
            filewriter.close();
            }
        } catch (IOException ex) {
        }


//        }
    }

    public void saveAll(String filename) {
        /* Only for Time Series */
        int colcount = tmodel.getColumnCount();
        int rowcount = tmodel.getRowCount();
        //int write_col_cnt = write_headers.length;
        //int[] col_index = new int[write_col_cnt];
        String value;
        String[] columnNames = tmodel.getCoulumnNameArray();
//String wd = System.getProperty("user.dir");
  //      JFileChooser chooser = new JFileChooser(wd); //ACHTUNG!!!!!!!!!

   //    int returnVal = chooser.showSaveDialog(panel);
   //    if (returnVal == JFileChooser.APPROVE_OPTION) {

        try {

            File file;
            if (isOutputSheet()) {
//                    file = new File(regionalizer.getWorkspace().getOutputDataDirectory()+filename);
//                file = new File(regionalizer.getWorkspace().getDirectory().toString() + "/output/current/" + filename);
                file = new File(this.getOutputDSDir().toString() + "/" + filename);
            } else {
                file = new File(explorer.getWorkspace().getDirectory().toString() + SpreadsheetConstants.FILE_EXPLORER_DIR_NAME + filename);
            }

            //File file = chooser.getSelectedFile();
            //File file = chooser.getSelectedFile();
            FileWriter filewriter = new FileWriter(file);

            filewriter.write(SpreadsheetConstants.LOAD_HEADERS + "\r\n");
            String col_string = "";
            for (int j = 0; j < colcount; j++) {
                col_string = columnNames[j];

                if (j == colcount - 1) {
                    filewriter.write(columnNames[j], 0, columnNames[j].length());

                } else {
                    filewriter.write(columnNames[j], 0, columnNames[j].length());
                    filewriter.write("\t");

                }

            }

            filewriter.write("\r\n" + SpreadsheetConstants.LOAD_DATA);
            filewriter.write("\r\n");

            for (int k = 0; k < rowcount; k++) {
//                        value = table.getValueAt(k, 0).toString();//timeRow
//                        filewriter.write(value, 0, value.length());
//                        filewriter.write("\t");
                for (int i = 0; i < colcount; i++) {

                    if (i == colcount - 1) {
                        value = table.getValueAt(k, i).toString();
                        filewriter.write(value, 0, value.length());
//                            filewriter.write("\t");
                    } else {

                        value = table.getValueAt(k, i).toString();
                        filewriter.write(value, 0, value.length());
                        filewriter.write("\t");
                    }
                }
                filewriter.write("\r\n");
            }
            filewriter.write(SpreadsheetConstants.LOAD_END);
            filewriter.close();

        } catch (IOException ex) {
        }


//      }
    }

    public void load(File file) {

        Vector<double[]> arrayVector = new Vector<double[]>();
        Vector<Attribute.Calendar> timeVector = new Vector<Attribute.Calendar>();
        StringTokenizer st = new StringTokenizer("\t");
        String[] headers;

        this.outputDSDir = file.getParentFile();
//        System.out.println("load() outputDSDir:" + outputDSDir.toString());

        ArrayList<String> headerList = new ArrayList<String>();
//        ArrayList<Double> rowList = new ArrayList<Double>();
        double[] rowBuffer;
        boolean b_headers = false;
        boolean b_data = false;
        boolean time_set = false;
        boolean stop = false;

        int file_columns = 0;

        final String ST_DATA = SpreadsheetConstants.LOAD_DATA;
        final String ST_HEADERS = SpreadsheetConstants.LOAD_HEADERS;
        final String ST_END = SpreadsheetConstants.LOAD_END;

        try {
            BufferedReader in = new BufferedReader(new FileReader(file));

            while (in.ready()) {
//                System.out.println("in.ready");
                //NEXT LINE
                String s = in.readLine();
                st = new StringTokenizer(s, "\t");

                String actual_string = "";
                Double val;
                boolean breakpoint = false;

                if (b_data) {
                    int i = 0;
                    Attribute.Calendar timeval = JAMSDataFactory.createCalendar();
                    rowBuffer = new double[file_columns - 1];
                    while (st.hasMoreTokens()) {
                        actual_string = st.nextToken();
                        if (actual_string.compareTo(ST_END) != 0) {
                            if (!time_set) {
//                                System.out.print("time: "+actual_string+"\t");
                                try {
                                    //JAMSCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second)
                                    timeval.setValue(actual_string, "yyyy-MM-dd hh:mm");

                                } catch (ParseException pe) {
                                    GUIHelper.showErrorDlg(this, SpreadsheetConstants.SPREADSHEET_ERR_TSMISSING, java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("ERROR"));
                                    breakpoint = true;
                                    break;
                                    //pe.printStackTrace();
                                }
                                timeVector.add(timeval);
                                time_set = true;
                            } else {
                                try {
//                                    System.out.println("value: "+actual_string+"\t");
                                    val = new Double(actual_string);
                                    rowBuffer[i++] = val.doubleValue();
                                } catch (Exception pe2) {
                                    pe2.printStackTrace();
                                }
                            }
                        } else {
                            stop = true;
                        }
                    }
                    if (breakpoint) {
                        break;
                    }
                    if (!stop) {
                        arrayVector.add(rowBuffer);
                        time_set = false;
                    }

                } else {

                    while (st.hasMoreTokens()) {
                        //NEXT STRING
                        String test = st.nextToken();

                        if (test.compareTo(ST_DATA) == 0) {
                            b_data = true;
                            b_headers = false;
                            file_columns = headerList.size();

                        }
                        if (b_headers) { //TIME HEADER/COL???
                            headerList.add(test);
                        }
                        if (test.compareTo(ST_HEADERS) == 0) {
                            b_headers = true;
                        }
                    }
                }
            }

            in.close();

            headers = new String[file_columns];
            headers = headerList.toArray(headers);

            this.tmodel = new JAMSTableModel();
            tmodel.setTimeRuns(true);
            timeRuns = true;
            tmodel.setTimeVector(timeVector);

            tmodel.setNewDataVector(arrayVector);
            tmodel.setColumnNames(headers);

            updateGUI();

        } catch (Exception eee) {
            GUIHelper.showErrorDlg(this, java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("FILE_NOT_FOUND!"), java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("ERROR!"));
        }
    }
    public JFileChooser getTemplateChooser() {

        File explorerDir;

        if (!isOutputSheet()) {
            explorerDir = new File(explorer.getWorkspace().getDirectory().toString() + SpreadsheetConstants.FILE_EXPLORER_DIR_NAME);
        } else {
            explorerDir = new File(explorer.getWorkspace().getDirectory().toString() + "/output/current");
        }

        if (templateChooser == null) {
            templateChooser = new JFileChooser();
            templateChooser.setFileFilter(JAMSFileFilter.getTtpFilter());
            explorerDir = new File(explorer.getWorkspace().getDirectory().toString() + "/explorer");
            templateChooser.setCurrentDirectory(explorerDir);
        }

        templateChooser.setCurrentDirectory(explorerDir);
        templateChooser.setFileFilter(JAMSFileFilter.getTtpFilter());
        templateChooser.setSelectedFile(new File(""));
        return templateChooser;
    }

    public JFileChooser getDatChooser() {

        if (datChooser == null) {
            datChooser = new JFileChooser();
            datChooser.setFileFilter(JAMSFileFilter.getDatFilter());
            File explorerDir = new File(explorer.getWorkspace().getDirectory().toString() + "/explorer");
            datChooser.setCurrentDirectory(explorerDir);
        }
        datChooser.setFileFilter(JAMSFileFilter.getDatFilter());
        datChooser.setSelectedFile(new File(""));
        return datChooser;
    }

    public JFileChooser getEPSFileChooser() {
        if (epsFileChooser == null) {
            epsFileChooser = new JFileChooser();
            epsFileChooser.setFileFilter(JAMSFileFilter.getEpsFilter());
        }
        epsFileChooser.setSelectedFile(new File(""));
        return epsFileChooser;
    }
    ActionListener closeTabAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            close();
        }
    };

    public File getOutputDSDir() {
        //call only if spreadsheet is output spreadsheet!
        return outputDSDir;
    }
    public void setOutputDSDir(File outputDSDir) {
        this.outputDSDir = outputDSDir;
    }

    private void formatDoubleArray(double[] rowBuffer) {
        // shorten double values to four decimal digits
        for (int i = 0; i < rowBuffer.length; i++) {
            rowBuffer[i] = Math.round(rowBuffer[i] * 10000.) / 10000.;
        }
    }

    public void loadMatrix(DataMatrix m, File outputDSDir, boolean timeSeries) {

//        getTemplateChooser().setCurrentDirectory(outputDSDir);
//        getEPSFileChooser().setCurrentDirectory(outputDSDir.getParentFile());
//        ttpFile = new File(inputDSDir, store.getID() + ".ttp");
//        dtpFile = new File(inputDSDir, store.getID() + ".dtp");

        Vector<double[]> arrayVector = new Vector<double[]>();
        Vector<Attribute.Calendar> timeVector = new Vector<Attribute.Calendar>();

        this.outputDSDir = outputDSDir;


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
                Attribute.Calendar timeval = JAMSDataFactory.createCalendar();
                timeval.setValue(id.toString());
                timeVector.add(timeval);
                rowBuffer = m.getRow(pos);
            } else {
                rowBuffer = new double[m.getColumnDimension() + 1];
                try {
                    rowBuffer[0] = Double.parseDouble(id.toString());
                } catch (Exception e) {
                    rowBuffer[0] = 0.0;
                }
                source = m.getRow(pos);
                System.arraycopy(source, 0, rowBuffer, 1, source.length);
            }

            formatDoubleArray(rowBuffer);

            arrayVector.add(rowBuffer);

            pos++;
        }

        if (timeSeries) {
            tmodel.setTimeVector(timeVector);
        }

        String[] attribtuteIDs = m.getAttributeIDs();
        String[] headers = new String[attribtuteIDs.length + 1];
        headers[0] = java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("ID");
        for (int i = 1; i < headers.length; i++) {
            headers[i] = attribtuteIDs[i - 1];
        }

        tmodel.setNewDataVector(arrayVector);
        tmodel.setColumnNames(headers);

        updateGUI();

    }

    public void loadTSDS(TSDataStore store, File inputDSDir) throws Exception {

        this.store = store;

        String dumpTimeFormat = store.getTimeFormat();

        int colNumber = 0;
        double[] rowBuffer;
        String[] headers;

//        getTemplateChooser().setCurrentDirectory(inputDSDir);
        File explorerDir = new File(explorer.getWorkspace().getDirectory().toString() + SpreadsheetConstants.FILE_EXPLORER_DIR_NAME);
        getTemplateChooser().setCurrentDirectory(explorerDir);
        getEPSFileChooser().setCurrentDirectory(inputDSDir.getParentFile());

        //regionalizer.getWorkspace().getDirectory().toString()+"/explorer";

//        ttpFile = new File(inputDSDir, store.getID() + ".ttp");
//        dtpFile = new File(inputDSDir, store.getID() + ".dtp");

        ttpFile = new File(explorer.getWorkspace().getDirectory().toString() + SpreadsheetConstants.FILE_EXPLORER_DIR_NAME, store.getID() + SpreadsheetConstants.FILE_ENDING_TTP);
//        dtpFile = new File(regionalizer.getWorkspace().getDirectory().toString() + SpreadsheetConstants.FILE_EXPLORER_DIR_NAME, store.getID() + ".dtp");

        Vector<double[]> arrayVector = new Vector<double[]>();
        Vector<Attribute.Calendar> timeVector = new Vector<Attribute.Calendar>();


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
            DefaultDataSet ds = store.getNext();

            DataValue[] rowData = ds.getData();

            if (rowData == null) {
                break;
            }

            Attribute.Calendar timeval = JAMSDataFactory.createCalendar();
            try {
                String timeString = rowData[0].getString();

//                if (store instanceof J2KTSDataStore) {
//                    timeval.setValue(timeString, J2KTSDataStore.DATE_TIME_FORMAT_PATTERN_J2K);
//                } else {
                timeval.setValue(timeString);
//                }
                timeval.setDateFormat(dumpTimeFormat);
            } catch (Exception pe) {
                pe.printStackTrace();
            }
            timeVector.add(timeval);

            rowBuffer = new double[colNumber];
            for (i = 1; i < rowData.length; i++) {
                rowBuffer[i - 1] = ((DoubleValue) rowData[i]).getDouble();
            }

            formatDoubleArray(rowBuffer);

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

    public void setAsOutputSheet() {
        this.output_sheet = true;
    }

    public boolean isOutputSheet() {
        return this.output_sheet;
    }

    private void openCTS() {

        if (table.getValueAt(0, 0).getClass().equals(JAMSCalendar.class)) {
            JTSConfigurator jts;
            jts = new JTSConfigurator((JFrame) explorer.getExplorerFrame(), this, explorer);
        } else {

            GUIHelper.showErrorDlg(this, ERR_MSG_CTS, java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("ERROR"));
        }
        //ctstabs.addGraph(table);
        //ctsIsOpen = true;
    }

    private void openCTS(File templateFile) {


        if (table.getValueAt(0, 0).getClass().equals(JAMSCalendar.class)) {
            JTSConfigurator jts;
            if (useTemplateButton.isSelected()) {
                jts = new JTSConfigurator((JFrame) explorer.getExplorerFrame(), this, templateFile, explorer);
            } else {
                jts = new JTSConfigurator((JFrame) explorer.getExplorerFrame(), this, null, explorer);
            }
        } else {
            GUIHelper.showErrorDlg(this, ERR_MSG_CTS, java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("ERROR"));
        }
        //ctstabs.addGraph(table);
        //ctsIsOpen = true;
    }

    private void openCXYS() {
        JXYConfigurator jxys;

        try {
            jxys = new JXYConfigurator((JFrame) explorer.getExplorerFrame(), this, null, explorer);
        } catch (NullPointerException npe) {
            jxys = new JXYConfigurator((JFrame) explorer.getExplorerFrame(), this, null, explorer);
        }
    }

    private void openCXYS(File templateFile) {
        JXYConfigurator jxys;

        if (useTemplateButton.isSelected()) {
            jxys = new JXYConfigurator((JFrame) explorer.getExplorerFrame(), this, templateFile, explorer);
        } else {
            jxys = new JXYConfigurator((JFrame) explorer.getExplorerFrame(), this, null, explorer);
        }
    }

    private void openSTP() {
        STPConfigurator stp = new STPConfigurator(explorer, this);
    }
    ActionListener plotAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {

            if (useTemplateButton.isSelected()) {

                if (isOutputSheet()) {

                    String fileID = getID();
                    StringTokenizer name_tokenizer = new StringTokenizer(fileID, ".");
                    String filename = "";
                    if (name_tokenizer.hasMoreTokens()) {
                        filename = name_tokenizer.nextToken() + SpreadsheetConstants.FILE_ENDING_TTP;
                    } else {
                        filename = fileID + SpreadsheetConstants.FILE_ENDING_TTP;
                    }

                    ttpFile = new File(getOutputDSDir(), filename);

                } else {
                    ttpFile = new File(explorer.getWorkspace().getDirectory().toString() + SpreadsheetConstants.FILE_EXPLORER_DIR_NAME, store.getID() + SpreadsheetConstants.FILE_ENDING_TTP);
                }

                if (ttpFile != null) {
                    if (ttpFile.exists()) {
                        try {
                            openCTS(ttpFile);
                        } catch (Exception ee) {
                            ee.printStackTrace();
                            try {
                                JFileChooser chooser = getTemplateChooser();
                                if (isOutputSheet()) {
                                    chooser.setCurrentDirectory(outputDSDir);
                                }
                                int returnVal = chooser.showOpenDialog(parent_frame);
                                if (returnVal == JFileChooser.APPROVE_OPTION) {
                                    ttpFile = chooser.getSelectedFile();

                                    openCTS(ttpFile);
                                }
//                            openCTS(ttpFile);

                            } catch (Exception fnfex) {

                                if (timeRuns) {
//                                table.setColumnSelectionInterval(1, table.getColumnCount() - 1);
//                                openCTS();
                                }
                            }

                        }

                    } else {

                        try {
                            JFileChooser chooser = getTemplateChooser();
                            if (isOutputSheet()) {
                                chooser.setCurrentDirectory(outputDSDir);
                            }
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

            if (useTemplateButton.isSelected()) {

                if (isOutputSheet()) {

                    String fileID = getID();
                    StringTokenizer name_tokenizer = new StringTokenizer(fileID, ".");
                    String filename = "";
                    if (name_tokenizer.hasMoreTokens()) {
                        filename = name_tokenizer.nextToken() + SpreadsheetConstants.FILE_ENDING_TTP;
                    } else {
                        filename = fileID + SpreadsheetConstants.FILE_ENDING_TTP;
                    }

                    ttpFile = new File(getOutputDSDir(), filename);

                } else {
                    ttpFile = new File(explorer.getWorkspace().getDirectory().toString() + SpreadsheetConstants.FILE_EXPLORER_DIR_NAME, store.getID() + SpreadsheetConstants.FILE_ENDING_TTP);
                }

                if (ttpFile != null) {
                    if (ttpFile.exists()) {
                        try {
                            openCXYS(ttpFile);
                        } catch (Exception ee) {
                            ee.printStackTrace();
                            try {
                                JFileChooser chooser = getTemplateChooser();
                                if (isOutputSheet()) {
                                    chooser.setCurrentDirectory(outputDSDir);
                                }
                                int returnVal = chooser.showOpenDialog(parent_frame);
                                if (returnVal == JFileChooser.APPROVE_OPTION) {
                                    ttpFile = chooser.getSelectedFile();

                                    openCXYS(ttpFile);
                                }
//                            openCTS(ttpFile);

                            } catch (Exception fnfex) {

                                if (timeRuns) {
//                                table.setColumnSelectionInterval(1, table.getColumnCount() - 1);
//                                openCTS();
                                }
                            }

                        }

                    } else {

                        try {
                            JFileChooser chooser = getTemplateChooser();
                            int returnVal = chooser.showOpenDialog(parent_frame);
                            if (returnVal == JFileChooser.APPROVE_OPTION) {
                                ttpFile = chooser.getSelectedFile();
                            }
                            openCXYS(ttpFile);

                        } catch (Exception fnfex) {

                            if (timeRuns) {
                                table.setColumnSelectionInterval(1, table.getColumnCount() - 1);
                                openCXYS();
                            }
                        }
                    }
                }
            } else {
                openCXYS();
            }
        }
    };
    ActionListener stpAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {

            try {

                openSTP();

            } catch (ClassCastException cce) {
//                if (timeRuns) {
//                    table.setColumnSelectionInterval(1, table.getColumnCount() - 1);
//                    openCXYS(dtpFile);
//                }
            }

        }
    };
    Action joinMapAction = new AbstractAction(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("AUF_KARTE_ZEIGEN")) {

        public void actionPerformed(ActionEvent e) {

            String selectedShape = (String) shapeSelector.getSelectedItem();
            if (StringTools.isEmptyString(selectedShape)) {
                System.out.println("no shape selected.");
                return;  // errorMessage?
            }

            System.out.println("shape selected >" + selectedShape + "<");
            ShapeFileDataStore dataStore = (ShapeFileDataStore) explorer.getWorkspace().getInputDataStore(selectedShape);
            if (dataStore == null) {
                System.out.println("no datastore found.");
                return;
            }

            URI uri = dataStore.getUri();
            String keyColumn = dataStore.getKeyColumn();
            String shapeFileName = dataStore.getFileName();

            int[] columns = table.getSelectedColumns();
            if (columns.length == 0) {
                System.out.println("no columns selected.");
                GUIHelper.showErrorDlg(null, ERR_MSG_CTS, java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("ERROR"));
                return;
            }
            String[] headers = getSelectedColumnNames();
            double[][] data = getSelectedData();
            double[] ids = getIdValues();

            // create and fill the DataTransfer object
            DataTransfer dataTransfer = new DataTransfer();
            dataTransfer.setNames(headers);
            dataTransfer.setIds(ids);
            dataTransfer.setData(data);
            dataTransfer.setParentName(shapeFileName);
            dataTransfer.setParentURI(uri);
            dataTransfer.setTargetKeyName(keyColumn);

            // get the Geowind viewer and pass the DataTransfer object
            System.out.println("start viewer.");
            Viewer viewer = Viewer.getViewer();

            try {
                viewer.addData(dataTransfer);
            } catch (Exception ex) {
                GUIHelper.showErrorDlg(JAMSSpreadSheet.this, java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("ERROR_WHILE_TRYING_TO_DISPLAY_MAP!"), java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("ERROR!"));
            }
        }
    };

    public boolean timeRuns() {
        return timeRuns;
    }

    /**
     * get id values of table
     * (id-column = 1st column)
     *
     **/
    private double[] getIdValues() {

        int rowCount = table.getRowCount();
        double[] ids = new double[rowCount];
        for (int j = 0; j < rowCount; j++) {
            ids[j] = (Double) table.getValueAt(j, 0);
        }
        return ids;

    }

    /**
     * get selected data of table
     * 
     **/
    private double[][] getSelectedData() {

        int[] columns = table.getSelectedColumns();
        if (columns.length == 0) {
            return null;
        }

        int rowCount = table.getRowCount();
        double[][] data = new double[columns.length][rowCount];

        // fill data arrays
        for (int i = 0; i < columns.length; i++) {
            for (int j = 0; j < rowCount; j++) {
                data[i][j] = (Double) table.getValueAt(j, columns[i]);
            }
        }
        return data;
    }

    /**
     * get all selected column names
     *
     **/
    private String[] getSelectedColumnNames() {
        int[] columns = table.getSelectedColumns();
        if (columns.length == 0) {
            return null;
        }

        String[] headers = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            headers[i] = table.getColumnName(columns[i]);
        }
        return headers;
    }

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

        double mean = 0;
        double sum = calcsum();

        int[] rows = table.getSelectedRows();
        int ix = rows.length;
        int[] columns = table.getSelectedColumns();
        int kx = columns.length;

        if (ix == 1) {
            mean = (double) sum / (double) (kx);
        }

        if (kx == 1) {
            mean = (double) sum / (double) (ix);
        }
        if (kx != 1 && ix != 1) {
            mean = (double) sum / (double) (kx * ix);
        }

        return mean;
    }

    public String getPanelName() {
        return this.panelname;
    }

    public void setColumnNameArray(String[] names) {
        tmodel.setColumnNames(names);
    }

    public void updateGUI() {
        table.setModel(tmodel);
        scrollpane.setViewportView(table);
        updateShapeSelector();
        this.repaint();
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
        table.setDragEnabled(true);
        //table.setSelectionMode(SINGLE SELECTION);
        table.setCellSelectionEnabled(true);
        //return scrollpane;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        new TableHandler(this.table);

    }

    public void createPanel() {

        this.setLayout(new BorderLayout(10, 10));
        JPanel controlpanel = new JPanel();
        JPanel helperpanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        controlpanel.setLayout(gbl);
        JPanel headerpanel = new JPanel();
        headerpanel.setLayout(new GridLayout(1, 2));

        useTemplateButton.setEnabled(true);
        useTemplateButton.setSelected(false);
        useTransposedButton.setEnabled(true);
        useTransposedButton.setSelected(false);

//        closeButton.setBackground(SpreadsheetConstants.GUI_COLOR_CLOSETAB);
        //dataplotButton.setEnabled(false);

        scrollpane.setVerticalScrollBar(new JScrollBar(JScrollBar.VERTICAL));
        scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

//        scrollpane2 = new JScrollPane(scrollpane);

        //setColumnNameArray(headers.getValue());
        makeTable();
        //panel.add(scrollpane,grid);

        //GUIHelper.addGBComponent(controlpanel, gbl, openbutton, 0, 2, 1, 1, 0, 0);
        //GUIHelper.addGBComponent(controlpanel, gbl, savebutton, 0, 3, 1, 2, 0, 0);

        GUIHelper.addGBComponent(controlpanel, gbl, closeButton, 0, 5, 1, 1, 0, 0);
        GUIHelper.addGBComponent(controlpanel, gbl, plotButton, 0, 6, 1, 1, 0, 0);
        GUIHelper.addGBComponent(controlpanel, gbl, dataplotButton, 0, 7, 1, 1, 0, 0);
        GUIHelper.addGBComponent(controlpanel, gbl, useTemplateButton, 0, 8, 1, 1, 0, 0);
        GUIHelper.addGBComponent(controlpanel, gbl, useTransposedButton, 0, 9, 1, 1, 0, 0);
//        GUIHelper.addGBComponent(controlpanel, gbl, stpButton, 0, 10, 1, 1, 0, 0);
        GUIHelper.addGBComponent(controlpanel, gbl, savebutton, 0, 11, 1, 1, 0, 0);
//        GUIHelper.addGBComponent(controlpanel, gbl, loadbutton, 0, 12, 1, 1, 0, 0);
        GUIHelper.addGBComponent(controlpanel, gbl, statButton, 0, 13, 1, 1, 0, 0);

        // populate shape-combobox, if shape file is in input stores
        if (updateShapeSelector()) {
            JButton joinMapButton = new JButton(joinMapAction);
            GUIHelper.addGBComponent(controlpanel, gbl, joinMapButton, 0, 14, 1, 1, 0, 0);
            GUIHelper.addGBComponent(controlpanel, gbl, shapeSelector, 0, 15, 1, 1, 0, 0);
        }

//              controlpanel.add(openbutton);
//              controlpanel.add(savebutton);
//              controlpanel.add(onthefly);
//              controlpanel.add(plotButton);
//              controlpanel.add(dataplotButton);

        statButton.addActionListener(statisticAction);
        savebutton.addActionListener(saveAction);
//        loadbutton.addActionListener(loadAction);
        plotButton.addActionListener(plotAction);
        dataplotButton.addActionListener(dataplotAction);
        stpButton.addActionListener(stpAction);
        closeButton.addActionListener(closeTabAction);

        headerpanel.add(titleLabel);
        headerpanel.add(headerlabel);
        helperpanel.add(controlpanel);

        this.add(headerpanel, BorderLayout.NORTH);

        this.add(scrollpane, BorderLayout.CENTER);
        this.add(helperpanel, BorderLayout.EAST);


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
        int col_START = 1; // is this nessesary?
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

//            if(!(timeRuns && viewCol == 0)){

//                if(table.getSelectedColumn() == 0){
//                    col_START = 1;
//                    button = 3;
//                }

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

                case 3:

                default:
                    col_START = table.getColumnModel().getColumn(viewCol).getModelIndex();
                    table.setColumnSelectionInterval(col_START, col_START);
            }
//            }


            table.setRowSelectionInterval(0, table.getRowCount() - 1);
            button = -1;
        }

        public void mouseEntered(MouseEvent e) {

            JTableHeader h = (JTableHeader) e.getSource();

            // Show hand cursor
            h.setCursor(new Cursor(Cursor.HAND_CURSOR));

        }

        public void mouseExited(MouseEvent e) {
            JTableHeader h = (JTableHeader) e.getSource();
            //h.setCursor(new Cursor(-1)); //default curser
        }
    }

    /**
     * updates the shape-selector with names of all shapes defined as inputDataStore
     * @return true, if any shapes found
     */
    private boolean updateShapeSelector() {
        String[] shapeNames = this.explorer.getWorkspace().
                getDataStoreIDs(InputDataStore.TYPE_SHAPEFILEDATASTORE);
        if (shapeNames != null && shapeNames.length > 0) {
            String defaultShapeName = shapeNames[0];
            DefaultComboBoxModel shapeSelectorModel = new DefaultComboBoxModel(shapeNames);
            shapeSelectorModel.setSelectedItem(defaultShapeName);
            shapeSelector.setModel(shapeSelectorModel);
            return true;
        }
        return false;
    }
}
