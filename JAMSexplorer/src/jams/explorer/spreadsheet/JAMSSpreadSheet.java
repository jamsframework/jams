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
package jams.explorer.spreadsheet;

import jams.JAMS;
import jams.JAMSFileFilter;
import jams.JAMSLogging;
import java.util.Vector;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
import java.util.ArrayList;
import jams.data.*;
import jams.explorer.tools.ShapeFileWriter;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import jams.explorer.JAMSExplorer;
import jams.workspace.dsproc.DataMatrix;
import java.util.TreeMap;
import jams.explorer.gui.StatisticDialogPanel;
import jams.math.statistics.MannKendall;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
    private String name = JAMS.i18n("DEFAULT");
    private JButton savebutton = new JButton(JAMS.i18n("SAVE_DATA"));
//    private JButton loadbutton = new JButton("Import Data");
    private JButton statButton = new JButton(JAMS.i18n("STATISTIK"));
    private JButton plotButton = new JButton(JAMS.i18n("TIME_PLOT"));
    private JButton trendButton = new JButton(JAMS.i18n("Trend_Estimation"));
    private JButton dataplotButton = new JButton(JAMS.i18n("DATA_PLOT"));
    private JButton closeButton = new JButton(JAMS.i18n("CLOSE_TAB"));
    private JCheckBox useTemplateButton = new JCheckBox(JAMS.i18n("USE_TEMPLATE"));
    private JCheckBox useTransposedButton = new JCheckBox(JAMS.i18n("USE_TRANSPOSED"));
    private JButton stpButton = new JButton(JAMS.i18n("STACKED_TIME_PLOT"));
    private JComboBox shapeSelector = new JComboBox();

    /* Labels */
    private JLabel headerlabel = new JLabel();
    private JLabel titleLabel = new JLabel(title);
    private JLabel calclabel = new JLabel(JAMS.i18n("CALCLABEL"));
    /* Table and TableModel */
    private JAMSTableModel tmodel;
    private JTableHeader tableHeader;
    private TSDataStore store;
    private File outputDSDir;
    JTable table;
    /* ComboBox */
 /* String array contains words of the ComboBox */
    private String[] calclist = {JAMS.i18n("SUM____"), JAMS.i18n("MEAN___")};
    JComboBox calculations = new JComboBox(calclist);
    private int kindofcalc = 0;
    private JFileChooser epsFileChooser, templateChooser, datChooser, savefileChooser;
    private JAMSExplorer explorer;

    /* Messages */
    final String ERR_MSG_CTS = JAMS.i18n("NO_DATA_LOADED");
    public static final DataFlavor FLAVOR = DataFlavor.stringFlavor;

    final private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm") {
        {
            setTimeZone(TimeZone.getTimeZone("GMT"));
        }
    };

    public class ProcessingException extends RuntimeException {

        ProcessingException(String message) {
            super(message);
        }

        ProcessingException(String message, Throwable t) {
            super(message, t);
        }
    }

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

    public class ExcelAdapter implements ActionListener {

        private String rowstring, value;
        private Clipboard system;
        private StringSelection stsel;
        private JTable jTable1;

        /**
         * The Excel Adapter is constructed with a JTable on which it enables
         * Copy-Paste and acts as a Clipboard listener.
         */
        public ExcelAdapter(JTable myJTable) {
            jTable1 = myJTable;
            KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
            // Identifying the copy KeyStroke user can modify this
            // to copy on some other Key combination.
            KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
            // Identifying the Paste KeyStroke user can modify this
            //to copy on some other Key combination.
            jTable1.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED);
            jTable1.registerKeyboardAction(this, "Paste", paste, JComponent.WHEN_FOCUSED);
            system = Toolkit.getDefaultToolkit().getSystemClipboard();
        }

        /**
         * Public Accessor methods for the Table on which this adapter acts.
         */
        public JTable getJTable() {
            return jTable1;
        }

        public void setJTable(JTable jTable1) {
            this.jTable1 = jTable1;
        }

        /**
         * This method is activated on the Keystrokes we are listening to in
         * this implementation. Here it listens for Copy and Paste
         * ActionCommands. Selections comprising non-adjacent cells result in
         * invalid selection and then copy action cannot be performed. Paste is
         * done by aligning the upper left corner of the selection with the 1st
         * element in the current selection of the JTable.
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().compareTo("Copy") == 0) {
                StringBuffer sbf = new StringBuffer();
                // Check to ensure we have selected only a contiguous block of
                // cells
                int numcols = jTable1.getSelectedColumnCount();
                int numrows = jTable1.getSelectedRowCount();
                int[] rowsselected = jTable1.getSelectedRows();
                int[] colsselected = jTable1.getSelectedColumns();
                if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0]
                        && numrows == rowsselected.length)
                        && (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0]
                        && numcols == colsselected.length))) {
                    JOptionPane.showMessageDialog(null, "Invalid Copy Selection",
                            "Invalid Copy Selection",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                for (int j = 0; j < numcols; j++) {
                    sbf.append(jTable1.getColumnName(colsselected[j]));
                    if (j < numcols - 1) {
                        sbf.append("\t");
                    }
                }
                sbf.append("\n");

                for (int i = 0; i < numrows; i++) {
                    for (int j = 0; j < numcols; j++) {
                        sbf.append(jTable1.getValueAt(rowsselected[i], colsselected[j]));
                        if (j < numcols - 1) {
                            sbf.append("\t");
                        }
                    }
                    sbf.append("\n");
                }
                stsel = new StringSelection(sbf.toString());
                system = Toolkit.getDefaultToolkit().getSystemClipboard();
                system.setContents(stsel, stsel);
            }
            if (e.getActionCommand().compareTo("Paste") == 0) {
                System.out.println("Trying to Paste");
                int startRow = (jTable1.getSelectedRows())[0];
                int startCol = (jTable1.getSelectedColumns())[0];
                try {
                    String trstring = (String) (system.getContents(this).getTransferData(DataFlavor.stringFlavor));
                    System.out.println("String is:" + trstring);
                    StringTokenizer st1 = new StringTokenizer(trstring, "\n");
                    for (int i = 0; st1.hasMoreTokens(); i++) {
                        rowstring = st1.nextToken();
                        StringTokenizer st2 = new StringTokenizer(rowstring, "\t");
                        for (int j = 0; st2.hasMoreTokens(); j++) {
                            value = (String) st2.nextToken();
                            if (startRow + i < jTable1.getRowCount()
                                    && startCol + j < jTable1.getColumnCount()) {
                                jTable1.setValueAt(value, startRow + i, startCol + j);
                            }
                            System.out.println("Putting " + value + "at row = " + startRow + i + " column = " + startCol + j);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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

        @Override
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
        JAMSLogging.registerLogger(JAMSLogging.LogOption.Show,
                Logger.getLogger(JAMSSpreadSheet.class.getName()));
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

    private String[] getSaveHeaders() {
        int[] selectedColumns = table.getSelectedColumns();
        int[] writeColumns;

//        if (selectedColumns[0] == 0) {
//            writeColumns = new int[selectedColumns.length - 1];
//            for (int i = 0; i < writeColumns.length; i++) {
//                writeColumns[i] = selectedColumns[i + 1];
//            }
//        } else {
        writeColumns = selectedColumns;
//        }

        String[] write_headers = new String[writeColumns.length];

        for (int i = 0; i < writeColumns.length; i++) {

            write_headers[i] = table.getColumnName(writeColumns[i]);

        }
        String[] headers_with_time = new String[write_headers.length + 1];
        headers_with_time[0] = JAMS.i18n("ID");
        java.lang.System.arraycopy(write_headers, 0, headers_with_time, 1, write_headers.length);
//        System.out.println(headers_with_time[0]+headers_with_time[1]);
        return write_headers;
    }

    /**
     * *********** *** Event Handling *** ****l****************************
     */
    public void loadSpreadsheet() {
        int returnVal = getDatChooser().showOpenDialog(JAMSSpreadSheet.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = getDatChooser().getSelectedFile();
            load(file);
        }
    }

    public void saveSpreadsheet() throws IOException {
        // ABSOLUT NEW TEST
        JFileChooser saveDlg = new JFileChooser();
        saveDlg.setFileFilter(JAMSFileFilter.getSdatFilter());
        saveDlg.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
        saveDlg.setCurrentDirectory(explorer.getWorkspace().getDirectory());

        int rc = saveDlg.showSaveDialog(panel);
        if (rc != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String filename = saveDlg.getSelectedFile().getName();
        setOutputDSDir(saveDlg.getCurrentDirectory());
        if (filename != null) {
            if (!filename.contains(".") && (saveDlg.getFileFilter() == JAMSFileFilter.getSdatFilter())) {
                filename += SpreadsheetConstants.FILE_ENDING_DAT;
            }
            if (isOutputSheet()) {
                File file = new File(getOutputDSDir(), filename);
                if (file.exists()) {
                    String fileexists = JAMS.i18n("THE_FILE_") + file + JAMS.i18n("_ALREADY_EXISTS._OVERWRITE?");
                    int result = GUIHelper.showYesNoDlg(parent_frame, fileexists, JAMS.i18n("FILE_ALREADY_EXISTS"));
                    if (result != GUIHelper.YES_OPTION) {
                        return;
                    }
                }
                save(filename, getSaveHeaders());
            } else {
                File file = new File(explorer.getWorkspace().getDirectory().toString() + "/explorer", filename);
                if (file.exists()) {
                    String fileexists = JAMS.i18n("THE_FILE_") + file + JAMS.i18n("_ALREADY_EXISTS._OVERWRITE?");
                    int result = GUIHelper.showYesNoDlg(parent_frame, fileexists, JAMS.i18n("FILE_ALREADY_EXISTS"));
                    if (result != GUIHelper.YES_OPTION) {
                        return;
                    }
                }
                save(filename, getSaveHeaders());
            }
        }
    }

    public void saveAll(String filename) {
        /* Only for Time Series */
        int colcount = tmodel.getColumnCount();
        int rowcount = tmodel.getRowCount();
        String value;
        String[] columnNames = tmodel.getCoulumnNameArray();

        try {

            File file;
            if (isOutputSheet()) {
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
                for (int i = 0; i < colcount; i++) {

                    if (i == colcount - 1) {
                        value = table.getValueAt(k, i).toString();
                        filewriter.write(value, 0, value.length());
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
            ex.printStackTrace();
        }
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
                    Attribute.Calendar timeval = DefaultDataFactory.getDataFactory().createCalendar();
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
                                    GUIHelper.showErrorDlg(this, SpreadsheetConstants.SPREADSHEET_ERR_TSMISSING, JAMS.i18n("ERROR"));
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
                                    Logger.getLogger(JAMSSpreadSheet.class.getName()).log(Level.SEVERE, null, pe2);
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
            GUIHelper.showErrorDlg(this, JAMS.i18n("FILE_NOT_FOUND!"), JAMS.i18n("ERROR!"));
        }
    }

    public void save(String filename, String[] write_headers) throws IOException {
        /* Only for Time Series */
        int colcount = tmodel.getColumnCount();
        int rowcount = tmodel.getRowCount();
        int write_col_cnt = write_headers.length;
        int[] col_index = new int[write_col_cnt];
        String value;
        String[] columnNames = tmodel.getCoulumnNameArray();

        File file;
        if (isOutputSheet()) {
            file = new File(this.getOutputDSDir().toString() + "/" + filename);
        } else {
            file = new File(this.getOutputDSDir().toString() + SpreadsheetConstants.FILE_EXPLORER_DIR_NAME + filename);
        }

        //File file = chooser.getSelectedFile();
        //File file = chooser.getSelectedFile();
        FileWriter filewriter = new FileWriter(file);
        if (!useTransposedButton.isSelected()) {
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

                    value = table.getValueAt(k, col_index[i]).toString();
                    filewriter.write(value, 0, value.length());
                    if (i < write_col_cnt - 1) {
                        filewriter.write("\t");
                    }
                }
                filewriter.write("\r\n");
            }
            filewriter.write(SpreadsheetConstants.LOAD_END);
            filewriter.close();

        } else { //AB hier Das gleich nur Transponiert

            TreeMap<String, Integer> map = new TreeMap<String, Integer>();

            for (int j = 0; j < colcount; j++) {
                String col_string = columnNames[j];
                map.put(col_string, j);
            }
            for (int c = 0; c < write_col_cnt; c++) {
                Integer i = map.get(write_headers[c]);
                col_index[c] = i;
            }
            filewriter.write(SpreadsheetConstants.LOAD_DATA + "\n");

            for (int i = 0; i < write_col_cnt; i++) {
                filewriter.write(columnNames[col_index[i]] + "\t");
                for (int k = 0; k < rowcount; k++) {
                    filewriter.write(table.getValueAt(k, col_index[i]).toString());
                    filewriter.write("\t");
                }
                filewriter.write("\n");
            }

            filewriter.write(SpreadsheetConstants.LOAD_END);
            filewriter.close();
        }
    }

    public void showStatisticsPanel() {
        String[] headers = getSelectedColumnNames();
        double[][] data = getSelectedData();
        StatisticDialogPanel statPanel = new StatisticDialogPanel(parent_frame, true, headers, data);
        statPanel.setVisible(true);
        statPanel.getReturnStatus();
    }

    public void estimateTrends() {
        if (!timeRuns()) {
            throw new ProcessingException(JAMS.i18n("Need temporal data"));
        }
        String dateColumn[] = getValueOfColumn(0);
        String ids[] = getColumnNames();
        Attribute.Calendar startDate, endDate;
        int T = dateColumn.length;

        try {
            startDate = DefaultDataFactory.getDataFactory().createCalendar();
            startDate.setTime(sdf.parse(dateColumn[0]));
            endDate = DefaultDataFactory.getDataFactory().createCalendar();
            endDate.setTime(sdf.parse(dateColumn[dateColumn.length - 1]));
        } catch (ParseException pe) {
            throw new ProcessingException(JAMS.i18n("Unable to parse first or last date!"), pe);
        }
        Attribute.TimeInterval tii = DefaultDataFactory.getDataFactory().createTimeInterval();
        tii.setStart(startDate);
        tii.setEnd(endDate);

        boolean selectedIndices[] = new boolean[T];
        Date[] dates = new Date[T];
        int selectionCount = 0;

        for (int i = 0; i < T; i++) {
            selectedIndices[i] = false;
            try {
                Date date = sdf.parse(dateColumn[i]);
                if (date.getTime() >= startDate.getTimeInMillis()
                        && date.getTime() <= endDate.getTimeInMillis()) {
                    selectedIndices[i] = true;
                    selectionCount++;
                }
                dates[i] = date;
            } catch (ParseException pe) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, JAMS.i18n("Unable to parse date " + dateColumn[i]), pe);
            }
        }

        tii = TimeIntervalInputDialog.showTimeIntervalInputDialog("Select trend estimation time interval", tii);
        if (tii == null) {
            return;
        }
        int K = this.getColumnCount();
        double x[] = new double[selectionCount];
        double y[] = new double[selectionCount];

        double result[][] = new double[K - 1][2];

        for (int i = 1; i < K; i++) {
            String data[] = getValueOfColumn(i);
            int c = 0;
            for (int j = 0; j < T; j++) {
                if (selectedIndices[j]) {
                    x[c] = dates[j].getTime() / 1000;
                    try {
                        y[c] = Double.parseDouble(data[j]);
                    } catch (NumberFormatException nfe) {
                        Logger.getLogger(this.getClass().getName()).log(Level.WARNING, JAMS.i18n("Unable to parse value " + data[j]), nfe);
                    }
                    c++;
                }
            }
            double mk[] = MannKendall.MannKendall(x, y);
            result[i - 1][0] = mk[0];
            result[i - 1][1] = mk[1];
        }
        String id2[] = new String[ids.length - 1];
        for (int i = 1; i < ids.length; i++) {
            id2[i - 1] = ids[i];
        }
        DataMatrix m = new DataMatrix(result, id2, new String[]{"tau", "p"});
        loadMatrix(m, outputDSDir, false);
    }

    /* Save */
    ActionListener saveAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                saveSpreadsheet();
            } catch (Throwable ex) {
                Logger.getLogger(JAMSSpreadSheet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

    ActionListener loadAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                loadSpreadsheet();
            } catch (Throwable fnfexc) {
                Logger.getLogger(JAMSSpreadSheet.class.getName()).log(Level.SEVERE, null, fnfexc);
            }
        }
    };
    ActionListener statisticAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            try {
                showStatisticsPanel();
            } catch (Throwable fnfexc) {
                Logger.getLogger(JAMSSpreadSheet.class.getName()).log(Level.SEVERE, null, fnfexc);
            }
        }
    };

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

    public JComboBox getShapeSelector() {
        return shapeSelector;
    }

    private void formatDoubleArray(double[] rowBuffer) {
        // shorten double values to four decimal digits
        for (int i = 0; i < rowBuffer.length; i++) {
            if (!Double.isNaN(rowBuffer[i]) && !Double.isInfinite(rowBuffer[i])) {
                double testValue = 10000 * rowBuffer[i];

                if (testValue - Math.floor(testValue) < 0.5) {
                    rowBuffer[i] = Math.floor(testValue) / 10000.;
                } else {
                    rowBuffer[i] = Math.ceil(testValue) / 10000.;
                }
                //this works not for values larger LONG.Max
                //rowBuffer[i] = Math.round(rowBuffer[i] * 10000.) / 10000.;
            }
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
                Attribute.Calendar timeval = DefaultDataFactory.getDataFactory().createCalendar();
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
        headers[0] = JAMS.i18n("ID");
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

        DefaultDataSet ds = null;
        // read table values from store
        while (store.hasNext()) {

            try {
                ds = store.getNext();
            } catch (Exception e) {
                e.printStackTrace();
                GUIHelper.showErrorDlg(this, JAMS.i18n("Trying_to_read_past_end_of_datastore"), JAMS.i18n("ERROR"));
                return;
            }

            DataValue[] rowData = ds.getData();

            if (rowData == null) {
                break;
            }

            Attribute.Calendar timeval = DefaultDataFactory.getDataFactory().createCalendar();
            try {
                String timeString = rowData[0].getString();

//                if (store instanceof J2KTSDataStore) {
//                    timeval.setValue(timeString, J2KTSDataStore.DATE_TIME_FORMAT_PATTERN_J2K);
//                } else {
                timeval.setValue(timeString);
//                }
                timeval.setDateFormat(dumpTimeFormat);
            } catch (Exception pe) {
                Logger.getLogger(JAMSSpreadSheet.class.getName()).log(Level.SEVERE, null, pe);
            }
            timeVector.add(timeval);

            rowBuffer = new double[colNumber];
            for (i = 1; i < rowData.length; i++) {
                if ((rowData[i] instanceof DoubleValue) && rowData[i].getDouble() != JAMS.getMissingDataValue()) {
                    rowBuffer[i - 1] = ((DoubleValue) rowData[i]).getDouble();
                } else {
                    rowBuffer[i - 1] = Double.NaN;
                }
            }

//            System.out.print(timeval + " : " + rowBuffer[0]);
            formatDoubleArray(rowBuffer);
//            System.out.println(" : " + rowBuffer[0]);

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

        try {
            if (table.getValueAt(0, 0).getClass().equals(JAMSCalendar.class)) {
                JTSConfigurator jts;
                jts = new JTSConfigurator((JFrame) explorer.getExplorerFrame(), this, explorer);
            } else {
                GUIHelper.showErrorDlg(this, ERR_MSG_CTS, JAMS.i18n("ERROR"));
            }
        } catch (IndexOutOfBoundsException ex) {
            GUIHelper.showErrorDlg(this, ERR_MSG_CTS, JAMS.i18n("ERROR"));
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
            GUIHelper.showErrorDlg(this, ERR_MSG_CTS, JAMS.i18n("ERROR"));
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
        } catch (IndexOutOfBoundsException ex) {
            GUIHelper.showErrorDlg(this, ERR_MSG_CTS, JAMS.i18n("ERROR"));
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

    private void writeToShapeFileAction() {
        String selectedShape = (String) shapeSelector.getSelectedItem();
        if (StringTools.isEmptyString(selectedShape)) {
            GUIHelper.showErrorDlg(explorer.getExplorerFrame(), "No Shapefile selected.");
            return;  // errorMessage?
        }

        ShapeFileDataStore dataStore = (ShapeFileDataStore) explorer.getWorkspace().getInputDataStore(selectedShape);
        if (dataStore == null) {
            GUIHelper.showErrorDlg(explorer.getExplorerFrame(), "No datastore found.");
            return;
        }

        URI inUri = dataStore.getUri();
        if (inUri == null) {
            GUIHelper.showErrorDlg(explorer.getExplorerFrame(), "Can't access Shapefile! path is: \""
                    + dataStore.getShapeFile().getAbsolutePath() + "\"");
            return;
        }

        int[] columns = table.getSelectedColumns();
        if (columns.length == 0) {
            GUIHelper.showErrorDlg(explorer.getExplorerFrame(), "No columns selected.");
            return;
        }

        String keyColumn = dataStore.getKeyColumn();

        File outFile = new File(explorer.getWorkspace().getOutputDataDirectory(), new File(inUri).toPath().getFileName().toString());
        JFileChooser jfc = GUIHelper.getJFileChooser(JAMSFileFilter.getShapeFilter());
        jfc.setSelectedFile(outFile);
        int result = jfc.showOpenDialog(panel);
        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        } else {
            outFile = jfc.getSelectedFile();
        }

        URI outUri = outFile.toURI();

        String[] headers = getSelectedColumnNames();
        double[][] data = getSelectedData();
        double[] ids = getIdValues();

        // create and fill the DataTransfer object
        ShapeFileWriter writer = new ShapeFileWriter();
        writer.setNames(headers);
        writer.setIds(ids);
        writer.setData(data);
        writer.setInShapefileURI(inUri);
        writer.setTargetKeyName(keyColumn);
        writer.setOutShapefileURI(outUri);
        try {
            writer.writeShape();
        } catch (IOException ex) {
            Logger.getLogger(JAMSSpreadSheet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    ActionListener plotAction = new ActionListener() {
        @Override
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
                            Logger.getLogger(JAMSSpreadSheet.class.getName()).log(Level.SEVERE, null, ee);
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
        @Override
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
                            Logger.getLogger(JAMSSpreadSheet.class.getName()).log(Level.SEVERE, null, ee);
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
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, cce.toString(), cce);
            }

        }
    };
    Action joinMapAction = new AbstractAction(JAMS.i18n("WRITE_TO_SHAPE")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                writeToShapeFileAction();
            } catch (Throwable t) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, t, null);
            }
        }
    };

    Action trendAction = new AbstractAction(JAMS.i18n("Trend_Estimation")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                estimateTrends();
            } catch (Throwable t) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, t.toString(), t);
            }
        }
    };

    public boolean timeRuns() {
        return timeRuns;
    }

    /**
     * get id values of table (id-column = 1st column)
     *
     *
     */
    private double[] getIdValues() {

        int rowCount = table.getRowCount();
        double[] ids = new double[rowCount];
        for (int j = 0; j < rowCount; j++) {
            ids[j] = (Double) table.getValueAt(j, 0);
        }
        return ids;
    }

    /**
     * get id values of table (id-column = 1st column)
     *
     *
     */
    private String[] getValueOfColumn(int col) {

        int rowCount = table.getRowCount();
        String[] data = new String[rowCount];
        for (int j = 0; j < rowCount; j++) {
            data[j] = table.getValueAt(j, col).toString();
        }
        return data;
    }

    /**
     * get id values of table (id-column = 1st column)
     *
     *
     */
    private String getCellValue(int row, int col) {
        return table.getValueAt(row, col).toString();
    }

    /**
     * get id values of table (id-column = 1st column)
     *
     *
     */
    private int getColumnCount() {
        return table.getColumnCount();
    }

    /**
     * get id values of table (id-column = 1st column)
     *
     *
     */
    private int getRowCount() {
        return table.getRowCount();
    }

    /**
     * get selected data of table
     *
     *
     */
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
     *
     */
    private String[] getColumnNames() {
        String[] headers = new String[table.getColumnCount()];
        for (int i = 0; i < table.getColumnCount(); i++) {
            headers[i] = table.getColumnName(i);
        }
        return headers;
    }

    /**
     * get all selected column names
     *
     *
     */
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

    /**
     * ************* Math ******************************
     */
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
        table.setAutoCreateRowSorter(true);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        table.setDragEnabled(true);
        //table.setSelectionMode(SINGLE SELECTION);
        table.setCellSelectionEnabled(true);
        //return scrollpane;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        new ExcelAdapter(table);
//        new TableHandler(this.table);
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
//        scrollpane.setVerticalScrollBar(new JScrollBar(JScrollBar.VERTICAL));
        scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollpane.getVerticalScrollBar().setUnitIncrement(100);

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
        GUIHelper.addGBComponent(controlpanel, gbl, trendButton, 0, 14, 1, 1, 0, 0);
        // populate shape-combobox, if shape file is in input stores
        if (updateShapeSelector()) {
            JButton joinMapButton = new JButton(joinMapAction);
            GUIHelper.addGBComponent(controlpanel, gbl, joinMapButton, 0, 16, 1, 1, 0, 0);
            GUIHelper.addGBComponent(controlpanel, gbl, shapeSelector, 0, 17, 1, 1, 0, 0);
        }

//              controlpanel.add(openbutton);
//              controlpanel.add(savebutton);
//              controlpanel.add(onthefly);
//              controlpanel.add(plotButton);
//              controlpanel.add(dataplotButton);
        statButton.addActionListener(statisticAction);
        trendButton.addActionListener(trendAction);
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
     * updates the shape-selector with names of all shapes defined as
     * inputDataStore
     *
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
