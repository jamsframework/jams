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

import jams.JAMSTools;
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


import jams.gui.GUIHelper;
import jams.workspace.DataSet;
import jams.workspace.datatypes.DataValue;
import jams.workspace.datatypes.DoubleValue;
import jams.workspace.stores.InputDataStore;
import jams.workspace.stores.ShapeFileDataStore;
import jams.workspace.stores.TSDataStore;
import java.net.URI;
import java.text.ParseException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import reg.DataTransfer;
import reg.JAMSExplorer;
import reg.dsproc.DataMatrix;
import reg.gui.StatisticDialogPanel;
import reg.viewer.Viewer;

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
    private JPanel panel = new JPanel();
    private String panelname = "spreadsheet";
    private int numberOfColumns = 0;
    private JFrame parent_frame;
    private boolean timeRuns = false;
    GridBagLayout panellayout = new GridBagLayout();
    GridBagConstraints grid = new GridBagConstraints();
    private JScrollPane scrollpane = new JScrollPane();
    
    private boolean output_sheet = false;

    //private JScrollPane scrollpane2;
    /* Buttons */
    private String name = "default";
    private JButton savebutton = new JButton("Save Data");
    private JButton loadbutton = new JButton("Import Data");
    private JButton statButton = new JButton("Statistik");
    private JButton plotButton = new JButton("Time Plot");
    private JButton dataplotButton = new JButton("Data Plot");
    private JButton closeButton = new JButton("Close Tab");
    private JCheckBox useTemplateButton = new JCheckBox("use Template");
    private JButton stpButton = new JButton("Stacked Time Plot");
    private JComboBox shapeSelector = new JComboBox();

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
    private JFileChooser epsFileChooser,  templateChooser, datChooser;
    private JAMSExplorer regionalizer;
    private boolean geoWindEnable = false;
    /* Messages */
    final String ERR_MSG_CTS = "No Time Series Loaded";

    public JAMSSpreadSheet(JAMSExplorer regionalizer) {
        this.regionalizer = regionalizer;
        this.parent_frame = regionalizer.getExplorerFrame();
    }

    public JAMSSpreadSheet(JAMSExplorer regionalizer, boolean geoWindEnable) {
        this(regionalizer);
        this.geoWindEnable = geoWindEnable;
    }

    /* Methods */
    public JPanel getPanel() {
        //createPanel();
        return panel;
    }

    public void closeTab() {
        regionalizer.getExplorerFrame().removeFromTabbedPane(this.name);
        regionalizer.getExplorerFrame().removeFromTabbedPane(this.getPanel());
        regionalizer.getDisplayManager().getSpreadSheets().remove(this.name);
    }

    public String getID() {
        return name;
    }

    public void setID(String name) {
        this.name = name;
        getPanel().setName(name);
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

            int[] selectedColumns = table.getSelectedColumns();
            String[] write_headers = new String[selectedColumns.length];

            for(int i=0; i<selectedColumns.length; i++){
                
                    write_headers[i] = table.getColumnName(selectedColumns[i]);
                
            }
            String[] headers_with_time = new String[write_headers.length+1];
            headers_with_time[0] = null;
            java.lang.System.arraycopy(write_headers, 0, headers_with_time, 1, write_headers.length);

            save("testfile.dat",write_headers);
            //ACTION!
//            try {
//            JFileChooser chooser = new JFileChooser();
//            int returnVal = chooser.showSaveDialog(panel);
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                
//                File file = chooser.getSelectedFile();
//                save(file.getAbsolutePath());
//            }
//        } catch (Exception fnfex) {
//        }
        }
    };

    ActionListener loadAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {

            int returnVal = -1;

            try {

                returnVal = getDatChooser().showOpenDialog(panel);

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
                
                File file = new File(regionalizer.getWorkspace().getDirectory().toString()+"/explorer/"+filename);
                
                //File file = chooser.getSelectedFile();
                //File file = chooser.getSelectedFile();
                FileWriter filewriter = new FileWriter(file);

                filewriter.write("#headers"+"\r\n");
                String col_string = "";
                for (int j = 0; j < colcount; j++) {
                    col_string = columnNames[j];
                    for(int c = 0; c < write_col_cnt; c++){
                        
                        if(col_string.compareTo(write_headers[c]) == 0){
                            if(c == write_col_cnt-1){
                                filewriter.write(columnNames[j], 0, columnNames[j].length());
                                col_index[c] = j;
                            }else{
                                filewriter.write(columnNames[j], 0, columnNames[j].length());
                                filewriter.write("\t");
                                col_index[c] = j;
                            }
                        }
                        
                    }
                }

                filewriter.write("\r\n" + "#data");
                filewriter.write("\r\n");

                for (int k = 0; k < rowcount; k++) {
//                        value = table.getValueAt(k, 0).toString();//timeRow
//                        filewriter.write(value, 0, value.length());
//                        filewriter.write("\t");
                    for (int i = 0; i < write_col_cnt; i++) {

                        if(i == write_col_cnt-1){
                            value = table.getValueAt(k, col_index[i]).toString();
                            filewriter.write(value, 0, value.length());
//                            filewriter.write("\t");
                        }else{

                            value = table.getValueAt(k, col_index[i]).toString();
                            filewriter.write(value, 0, value.length());
                            filewriter.write("\t");
                        }
                    }
                    filewriter.write("\r\n");
                }
                filewriter.write("#end");
                filewriter.close();

            } catch (IOException ex) {
            }
//        }
    }

    private void load(File file){

        Vector<double[]> arrayVector = new Vector<double[]>();
        Vector<JAMSCalendar> timeVector = new Vector<JAMSCalendar>();
        StringTokenizer st = new StringTokenizer("\t");
        String[] headers;

        ArrayList<String> headerList = new ArrayList<String>();
//        ArrayList<Double> rowList = new ArrayList<Double>();
        double[] rowBuffer;
        boolean b_headers = false;
        boolean b_data = false;
        boolean time_set = false;
        boolean stop = false;

        int file_columns = 0;

        final String ST_DATA =      "#data";
        final String ST_HEADERS =   "#headers";
        final String ST_END =       "#end";

        try{
            BufferedReader in = new BufferedReader(new FileReader(file));

            while(in.ready()){
//                System.out.println("in.ready");
                //NEXT LINE
                String s = in.readLine();
                st = new StringTokenizer(s ,"\t");

                String actual_string = "";
                Double val;
                boolean breakpoint=false;

                if(b_data){
                    int i = 0;
                    JAMSCalendar timeval = JAMSDataFactory.createCalendar();
                    rowBuffer = new double[file_columns];
                    while(st.hasMoreTokens()){
                        actual_string = st.nextToken();
                        if(actual_string.compareTo(ST_END) != 0){
                            if(!time_set){
//                                System.out.print("time: "+actual_string+"\t");
                                try {
                                //JAMSCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second)
                                    timeval.setValue(actual_string, "yyyy-MM-dd hh:mm");

                                } catch (ParseException pe) {
                                    GUIHelper.showErrorDlg(panel, " Time Series missing!", "Error");
                                    breakpoint=true;
                                    break;
                                    //pe.printStackTrace();
                                }
                                timeVector.add(timeval);
                                time_set = true;
                            }else{
                                try{
//                                    System.out.println("value: "+actual_string+"\t");
                                    val = new Double(actual_string);
                                    rowBuffer[i++] = val.doubleValue();
                                }catch(Exception pe2){
                                    pe2.printStackTrace();
                                }
                            }
                        }else{
                            stop = true;
                        }
                    }
                    if(breakpoint) break;
                    if(!stop){
                        arrayVector.add(rowBuffer);
                        time_set = false;
                    }

                }else{

                    while(st.hasMoreTokens()){
                        //NEXT STRING
                        String test = st.nextToken();

                        if(test.compareTo(ST_DATA) == 0){
                            b_data = true;
                            b_headers = false;
                            file_columns = headerList.size();

                        }
                        if(b_headers){ //TIME HEADER/COL???
                            headerList.add(test);
                        }
                        if(test.compareTo(ST_HEADERS) == 0){
                            b_headers = true;
                        }

                    }
                }
            }
            headers = new String[file_columns];
            headers = headerList.toArray(headers);
            headers[0] = "";
//            columns = file_columns-1;
//            rows = arrayVector.size();

            this.tmodel = new JAMSTableModel();
            tmodel.setTimeRuns(true);
            timeRuns = true;
            tmodel.setTimeVector(timeVector);

            tmodel.setNewDataVector(arrayVector);
            tmodel.setColumnNames(headers);

        updateGUI();
            //in.close();
//            System.out.println("TimeVectorSize:"+timeVector.size());
//            System.out.println("ArrayVectorSize:"+arrayVector.size());
//

        }catch(Exception eee){
            GUIHelper.showErrorDlg(panel, "File Not Found!", "Error!");
//            eee.printStackTrace();

    }}

    public JFileChooser getTemplateChooser() {
        
        if (templateChooser == null) {
            templateChooser = new JFileChooser();
            templateChooser.setFileFilter(JAMSFileFilter.getTtpFilter());
            File explorerDir = new File(regionalizer.getWorkspace().getDirectory().toString()+"/explorer");
            templateChooser.setCurrentDirectory(explorerDir);
        }
        templateChooser.setFileFilter(JAMSFileFilter.getTtpFilter());
        return templateChooser;
    }

    public JFileChooser getDatChooser() {

        if (datChooser == null) {
            datChooser = new JFileChooser();
            datChooser.setFileFilter(JAMSFileFilter.getDatFilter());
            File explorerDir = new File(regionalizer.getWorkspace().getDirectory().toString()+"/explorer");
            datChooser.setCurrentDirectory(explorerDir);
        }
        datChooser.setFileFilter(JAMSFileFilter.getDatFilter());
        return datChooser;
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

            closeTab();
        }
    };

    public void loadMatrix(DataMatrix m, File outputDSDir, boolean timeSeries) {

//        getTemplateChooser().setCurrentDirectory(outputDSDir);
//        getEPSFileChooser().setCurrentDirectory(outputDSDir.getParentFile());
//        ttpFile = new File(inputDSDir, store.getID() + ".ttp");
//        dtpFile = new File(inputDSDir, store.getID() + ".dtp");

        Vector<double[]> arrayVector = new Vector<double[]>();
        Vector<JAMSCalendar> timeVector = new Vector<JAMSCalendar>();

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

        String[] attribtuteIDs = m.getAttributeIDs();
        String[] headers = new String[attribtuteIDs.length + 1];
        headers[0] = "ID";
        for (int i = 1; i < headers.length; i++) {
            headers[i] = attribtuteIDs[i - 1];
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

//        getTemplateChooser().setCurrentDirectory(inputDSDir);
        File explorerDir = new File(regionalizer.getWorkspace().getDirectory().toString()+"/explorer");
        getTemplateChooser().setCurrentDirectory(explorerDir);
        getEPSFileChooser().setCurrentDirectory(inputDSDir.getParentFile());

        //regionalizer.getWorkspace().getDirectory().toString()+"/explorer";

//        ttpFile = new File(inputDSDir, store.getID() + ".ttp");
//        dtpFile = new File(inputDSDir, store.getID() + ".dtp");

        ttpFile = new File(regionalizer.getWorkspace().getDirectory().toString()+"/explorer", store.getID() + ".ttp");
        dtpFile = new File(regionalizer.getWorkspace().getDirectory().toString()+"/explorer", store.getID() + ".dtp");

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
    
    public void setAsOutputSheet(){
        this.output_sheet = true;
    }
    
    public boolean isOutputSheet(){
        return this.output_sheet;
    }
    
    private void openCTS() {
        
        if(table.getValueAt(0, 0).getClass().equals(JAMSCalendar.class)){
            JTSConfigurator jts;
            jts = new JTSConfigurator(regionalizer.getExplorerFrame(), this, regionalizer);
        }else{
            
            GUIHelper.showErrorDlg(panel, ERR_MSG_CTS, "Error");
        }
    //ctstabs.addGraph(table);
    //ctsIsOpen = true;
    }

    private void openCTS(File templateFile) {

            
        if(table.getValueAt(0, 0).getClass().equals(JAMSCalendar.class)){
            JTSConfigurator jts;
            if (useTemplateButton.isSelected()) {
                jts = new JTSConfigurator(regionalizer.getExplorerFrame(), this, templateFile, regionalizer);
            } else {
                jts = new JTSConfigurator(regionalizer.getExplorerFrame(), this, null, regionalizer);
            }
        }else{
            GUIHelper.showErrorDlg(panel, ERR_MSG_CTS, "Error");
        }
    //ctstabs.addGraph(table);
    //ctsIsOpen = true;
    }

    private void openCXYS() {
        JXYConfigurator jxys;

        try {
            jxys = new JXYConfigurator(regionalizer.getExplorerFrame(), this);
        } catch (NullPointerException npe) {
            jxys = new JXYConfigurator(regionalizer.getExplorerFrame(), this);
        }
    }

    private void openCXYS(File templateFile) {
        JXYConfigurator jxys;

        if (useTemplateButton.isSelected()) {
            jxys = new JXYConfigurator(regionalizer.getExplorerFrame(), this, templateFile);
        } else {
            jxys = new JXYConfigurator(regionalizer.getExplorerFrame(), this, null);
        }
    }

    private void openSTP() {
        STPConfigurator stp = new STPConfigurator(regionalizer, this);
    }
    ActionListener plotAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {

            if (useTemplateButton.isSelected()) {
                if (ttpFile != null) {
                    if (ttpFile.exists()) {
                        try{
                            openCTS(ttpFile);
                        } catch(Exception ee){

                            try {
                            JFileChooser chooser = getTemplateChooser();
                            int returnVal = chooser.showOpenDialog(parent_frame);
                            if (returnVal == JFileChooser.APPROVE_OPTION) {
                                ttpFile = chooser.getSelectedFile();
                                System.out.println("APPROVE_OPTION");
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

            try {

            openSTP();

            } catch (ClassCastException cce) {

                if (timeRuns) {
                    table.setColumnSelectionInterval(1, table.getColumnCount() - 1);
                    openCXYS(dtpFile);
                }
            }

        }
    };
    Action joinMapAction = new AbstractAction("Auf Karte zeigen") {

        public void actionPerformed(ActionEvent e) {

            String selectedShape = (String) shapeSelector.getSelectedItem();
            if (JAMSTools.isEmptyString(selectedShape)) {
                System.out.println("no shape selected.");
                return;  // errorMessage?
            }

            System.out.println("shape selected >" + selectedShape + "<");
            ShapeFileDataStore dataStore = (ShapeFileDataStore) regionalizer.getWorkspace().getInputDataStore(selectedShape);
            if (dataStore == null) {
                System.out.println("no datastore found.");
                return;
            }

            URI uri = dataStore.getUri();
            String keyColumn = dataStore.getKeyColumn();
            String shapeFileName = dataStore.getFileName();

            int[] columns = table.getSelectedColumns();
            if (columns.length == 0) {
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
            Viewer viewer = Viewer.getViewer();

            try {
                viewer.addData(dataTransfer);
            } catch (Exception ex) {
                Logger.getLogger(JAMSSpreadSheet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

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
        GUIHelper.addGBComponent(controlpanel, gbl, stpButton, 0, 9, 1, 1, 0, 0);
        GUIHelper.addGBComponent(controlpanel, gbl, savebutton, 0, 10, 1, 1, 0, 0);
        GUIHelper.addGBComponent(controlpanel, gbl, loadbutton, 0, 11, 1, 1, 0, 0);
        GUIHelper.addGBComponent(controlpanel, gbl, statButton, 0, 12, 1, 1, 0, 0);

        if (JAMSExplorer.GEOWIND_ENABLE && this.geoWindEnable) {

            // populate shape-combobox
            String defaultShapeName = null;
            
            String[] shapeStoreIDs = this.regionalizer.getWorkspace().
                    getDataStoreIDs(InputDataStore.TYPE_SHAPEFILEDATASTORE);
            
            String[] shapeNames = shapeStoreIDs;
            
            if (shapeNames != null && shapeNames.length > 0) {
                defaultShapeName = shapeNames[0];
            }
            DefaultComboBoxModel shapeSelectorModel = new DefaultComboBoxModel(shapeNames);
            shapeSelectorModel.setSelectedItem(defaultShapeName);
            shapeSelector.setModel(shapeSelectorModel);
            JButton joinMapButton = new JButton(joinMapAction);
            GUIHelper.addGBComponent(controlpanel, gbl, joinMapButton, 0, 13, 1, 1, 0, 0);
            GUIHelper.addGBComponent(controlpanel, gbl, shapeSelector, 0, 14, 1, 1, 0, 0);
        }

//              controlpanel.add(openbutton);
//              controlpanel.add(savebutton);
//              controlpanel.add(onthefly);
//              controlpanel.add(plotButton);
//              controlpanel.add(dataplotButton);

        statButton.addActionListener(statisticAction);
        savebutton.addActionListener(saveAction);
        loadbutton.addActionListener(loadAction);
        plotButton.addActionListener(plotAction);
        dataplotButton.addActionListener(dataplotAction);
        stpButton.addActionListener(stpAction);
        closeButton.addActionListener(closeTabAction);

        headerpanel.add(titleLabel);
        headerpanel.add(headerlabel);
        helperpanel.add(controlpanel);

        panel.add(headerpanel, BorderLayout.NORTH);

        panel.add(scrollpane, BorderLayout.CENTER);
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
