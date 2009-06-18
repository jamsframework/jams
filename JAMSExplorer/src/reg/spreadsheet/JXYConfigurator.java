/*
 * CTSConfigurator.java
 *
 * Created on 2. September 2007, 00:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package reg.spreadsheet;

//import com.sun.image.codec.jpeg.JPEGCodec;
//import com.sun.image.codec.jpeg.JPEGEncodeParam;
//import com.sun.image.codec.jpeg.JPEGImageEncoder;
import java.util.HashMap;
import java.util.Vector;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.*;
import javax.swing.BorderFactory.*;
import javax.swing.border.*;
import javax.swing.GroupLayout.*;

import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import jams.io.JAMSFileFilter;
import jams.gui.GUIHelper;
import jams.gui.WorkerDlg;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 *
 * @author Robert Riedel
 */
public class JXYConfigurator extends JFrame {

    GroupLayout gLayout;

    GroupLayout.SequentialGroup hGroup;

    GroupLayout.SequentialGroup vGroup;

    Group group1;

    Group group2;

    Group group3;

    Group group4;

    Group group5;

    Group group6;

    Group group7;

    Group group8;

    Group group9;

    Group group10;

    Group group11;

    Group group12;

    Group group13;

    Group group14;

    Group group15;
    //private Vector<ActionListener> addAction = new Vector<ActionListener>();    

    private JFrame parent;

    private JFrame thisDlg;

    private JXYConfigurator thisJXY = this;

    private JPanel frame;

    private JPanel mainpanel;

    private JPanel plotpanel;

    private JPanel optionpanel;

    private JPanel graphpanel;

    private JPanel southpanel;
    //private Vector<JPanel> datapanels = new Vector<JPanel>();

    private JPanel edTitlePanel;

    private JPanel edLeftAxisPanel;

    private JPanel edRightAxisPanel;

    private JPanel edTimeAxisPanel;

    private JSplitPane split_hor = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    private JSplitPane split_vert = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    private JPanel[] datapanels;

    private JScrollPane graphScPane;

    private JScrollPane plotScPane;

    private JScrollPane mainScPane;

    private JScrollPane optScPane;

    private JPanel savePanel;

    private String[] headers;

    private File templateFile;
    //private static JFileChooser templateChooser;

    private JButton saveTempButton = new JButton("Save Template");

    private JButton loadTempButton = new JButton("Load Template");

    private JLabel edTitle = new JLabel("Plot Title: ");

    private JLabel edLeft = new JLabel("Left axis title: ");

    private JLabel edXAxis = new JLabel("X axis title");

    private JLabel edRight = new JLabel("Right axis title: ");

    private JLabel rLeftLabel = new JLabel("Renderer left");

    private JLabel rRightLabel = new JLabel("Renderer right");

    private JLabel invLeftLabel = new JLabel("Invert left axis");

    private JLabel invRightLabel = new JLabel("Invert right axis");

    private JTextField edTitleField = new JTextField(14);

    private JTextField edLeftField = new JTextField(14);

    private JTextField edRightField = new JTextField(14);

    private JTextField edXAxisField = new JTextField(14);
    //private String[] types = {"Line","Bar","Area","Line and Base","Dot","Step","StepArea","Difference"};

    private String[] types = {"Line and Shape", "Bar", "Area", "Step", "StepArea", "Difference"};

    private JComboBox rLeftBox = new JComboBox(types);

    private JComboBox rRightBox = new JComboBox(types);

    private JCheckBox invLeftBox = new JCheckBox("Invert left Axis");

    private JCheckBox invRightBox = new JCheckBox("Invert right Axis");

    private ButtonGroup isXAxisGroup = new ButtonGroup();

    private JButton applyButton = new JButton("Apply");

    private JButton addButton = new JButton("Add Graph");

    private JButton saveButton = new JButton("EPS export");

    private Vector<GraphProperties> propVector = new Vector<GraphProperties>();

    private JAMSXYPlot jxys = new JAMSXYPlot();

    public XYRow[] sorted_Row;

    private boolean tempLoaded = true;
//    private String[] headers;
//    //private String[] colors = {"yellow","orange","red","pink","magenta","cyan","blue","green","gray","lightgray","black"};
    //private String[] colors = {"red","blue","green","black","magenta","cyan","yellow","gray","orange","lightgray","pink"};
//    private String[] types = {"Line","Bar","Area","Line and Base","Dot","Difference","Step","StepArea"};
//    private String[] positions = {"left","right"};
    /* test*/
//    private Color[] colors_ = {Color.RED, Color.BLUE};
//    private String[] legendEntries;

    private int x_series_index;

    private int colour_cnt;

    double row_start;

    double row_end;

    private boolean d_start_changed;

    private boolean d_end_changed;

    int[] range = new int[2];

    HashMap<String, Color> colorTable = new HashMap<String, Color>();

    int[] rows, columns;

    JTable table;

    CTSPlot ctsplot;
    /*buttons*/

    int graphCount = 0;
//    Vector<JCheckBox> activate = new Vector<JCheckBox>();
//    Vector<JComboBox> datachoice = new Vector<JComboBox>();
//    Vector<JComboBox> poschoice = new Vector<JComboBox>();
//    Vector<JComboBox> typechoice = new Vector<JComboBox>();
//    Vector<JComboBox> colorchoice = new Vector<JComboBox>();

    JCheckBox[] activate;

    JComboBox[] datachoice;

    JComboBox[] poschoice;

    JComboBox[] typechoice;

    JComboBox[] colorchoice;
    /* ActionListener */

    ActionListener[] activationChange;

    JAMSSpreadSheet sheet;

    /** Creates a new instance of CTSConfigurator */
    public JXYConfigurator() {
        /* open CXYConf */
    }
    /*
    public CTSConfigurator(JAMSTableModel tmodel){
    this.tmodel = tmodel;
    }
     **/

    public JXYConfigurator(JFrame parent, JAMSSpreadSheet sheet) {

        this.setParent(parent);
        this.setIconImage(parent.getIconImage());
        setTitle("XYPlot Viewer");
//        URL url = this.getClass().getResource("/jams/components/gui/resources/JAMSicon16.png");
//        ImageIcon icon = new ImageIcon(url);
//        setIconImage(icon.getImage());

        setLayout(new FlowLayout());
        Point parentloc = parent.getLocation();
        setLocation(parentloc.x + 30, parentloc.y + 30);
//        this.thisJXY = this;
        
        this.sheet = sheet;
        this.table = sheet.table;

        this.rows = table.getSelectedRows();
        this.columns = table.getSelectedColumns();
        this.graphCount = columns.length;
        this.headers = new String[graphCount];/* hier aufpassen bei reselection xxx reselecton -> neue instanz */

        d_start_changed = false;
        d_end_changed = false;

        writeSortedData(columns[0]);

        //setPreferredSize(new Dimension(1024, 768));

        createPanel();

        pack();
        setVisible(true);

    }

    public JXYConfigurator(JFrame parent, JAMSSpreadSheet sheet, File templateFile) {

        this.setParent(parent);
        this.setIconImage(parent.getIconImage());
        setTitle("XYPlot Viewer");
//        URL url = this.getClass().getResource("/jams/components/gui/resources/JAMSicon16.png");
//        ImageIcon icon = new ImageIcon(url);
//        setIconImage(icon.getImage());

        setLayout(new FlowLayout());
        Point parentloc = parent.getLocation();
        setLocation(parentloc.x + 30, parentloc.y + 30);
//        this.thisJXY = this;
        
        this.sheet = sheet;
        this.table = sheet.table;
        this.templateFile = templateFile;

        this.rows = table.getSelectedRows();
        this.columns = table.getSelectedColumns();
        this.graphCount = columns.length;
        this.headers = new String[graphCount];/* hier aufpassen bei reselection xxx reselecton -> neue instanz */

        d_start_changed = false;
        d_end_changed = false;

        if (templateFile == null) {
            tempLoaded = false;
        }

        try {
            writeSortedData(columns[0]);
        } catch (java.lang.ArrayIndexOutOfBoundsException aiofb) {
            writeSortedData(0);
        }

        //setPreferredSize(new Dimension(1024, 768));

        createPanel();

        pack();
        setVisible(true);

    }

    public void createPanel() {

        thisDlg = this;
        colour_cnt = 0;
        /* create ColorMap */
        colorTable.put("yellow", Color.yellow);
        colorTable.put("orange", Color.orange);
        colorTable.put("red", Color.red);
        colorTable.put("pink", Color.pink);
        colorTable.put("magenta", Color.magenta);
        colorTable.put("cyan", Color.cyan);
        colorTable.put("blue", Color.blue);
        colorTable.put("green", Color.green);
        colorTable.put("gray", Color.gray);
        colorTable.put("lightgray", Color.lightGray);
        colorTable.put("black", Color.black);

        JLabel nameLabel = new JLabel("Name");
        JLabel posLabel = new JLabel("Position");
        JLabel typeLabel = new JLabel("Renderer");
        JLabel colorLabel = new JLabel("Colour");
        JLabel dataLabel = new JLabel("Select Data / Legend Entry");
        JLabel timeLabel = new JLabel("Data Range");
        JLabel emptyTimeLabel = new JLabel("    ");
        JLabel legendLabel = new JLabel("Legend Entry");

        nameLabel.setBackground(Color.DARK_GRAY);
        posLabel.setBackground(Color.DARK_GRAY);
        typeLabel.setBackground(Color.DARK_GRAY);
        colorLabel.setBackground(Color.DARK_GRAY);
        dataLabel.setBackground(Color.DARK_GRAY);
        timeLabel.setBackground(Color.DARK_GRAY);


        savePanel = new JPanel();
        GridBagLayout sgbl = new GridBagLayout();
        savePanel.setLayout(sgbl);
        GUIHelper.addGBComponent(savePanel, sgbl, saveButton, 0, 0, 1, 1, 0, 0);
        GUIHelper.addGBComponent(savePanel, sgbl, saveTempButton, 0, 1, 1, 1, 0, 0);
        GUIHelper.addGBComponent(savePanel, sgbl, loadTempButton, 0, 2, 1, 1, 0, 0);

        saveButton.addActionListener(saveImageAction);
        saveTempButton.addActionListener(saveTempListener);
        loadTempButton.addActionListener(loadTempListener);

        plotpanel = new JPanel();
        plotpanel.setLayout(new BorderLayout());

        setLayout(new BorderLayout());
        //frame.setSize(640,80);

        graphScPane = new JScrollPane();

        optionpanel = new JPanel();
        //optionpanel.setLayout(new FlowLayout());
        graphpanel = new JPanel();

        initGroupUI();

        southpanel = new JPanel();
        southpanel.setLayout(new FlowLayout());

        edTitlePanel = new JPanel();
        edTitlePanel.setLayout(new FlowLayout());
        edLeftAxisPanel = new JPanel();
        edLeftAxisPanel.setLayout(new FlowLayout());
        edRightAxisPanel = new JPanel();
        edRightAxisPanel.setLayout(new FlowLayout());

        edTitleField.setText(sheet.getID());
        edTitleField.setSize(40, 10);
        edTitleField.addActionListener(plotbuttonclick);
        //edLeftField.setColumns(20);
        edLeftField.setText("Left Axis Title");
        edLeftField.addActionListener(plotbuttonclick);
        edLeftField.setSize(40, 10);
        //edRightField.setColumns(20);
        edRightField.setText("Right Axis Title");
        edRightField.addActionListener(plotbuttonclick);
        edRightField.setSize(40, 10);

        edXAxisField.setText("x axis title");
        edXAxisField.addActionListener(plotbuttonclick);
        applyButton.addActionListener(plotbuttonclick);

//        edTitlePanel.add(edTitle);
//        edTitlePanel.add(edTitleField);
//        edLeftAxisPanel.add(edLeft);
//        edLeftAxisPanel.add(edLeftField);
//        edRightAxisPanel.add(edRight);
//        edRightAxisPanel.add(edRightField);

        optionpanel.add(edTitle);
        optionpanel.add(edTitleField);
        optionpanel.add(edLeft);
        optionpanel.add(edLeftField);
        optionpanel.add(edRight);
        optionpanel.add(edRightField);
        optionpanel.add(applyButton);

        rLeftBox.setSelectedIndex(0);
        rRightBox.setSelectedIndex(0);

        ////////////////////////// GRAPH AUSFÜHREN ///////////
        if (templateFile != null) {
            try {
                loadTemplate(templateFile);
                tempLoaded = true;
            } catch (Exception fnfe) {
                initGraphLoad();
                tempLoaded = false;
            }
        } else {
            initGraphLoad();
            tempLoaded = false;
        }
        ////////////////////////////////////////////



        createOptionPanel();
        handleRenderer();

        jxys.setPropVector(propVector);
        jxys.createPlot();

        JPanel graphPanel = new JPanel();
        JPanel optPanel = new JPanel();
        graphPanel.add(graphpanel);
        optPanel.add(optionpanel);

        graphScPane = new JScrollPane(graphPanel);
        graphScPane.setPreferredSize(new Dimension(512, 300));
        graphScPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        //savePanel.add(saveButton);

        jxys.getPanel().add(savePanel, BorderLayout.EAST);

        optionpanel.setBorder(new EtchedBorder());
        plotScPane = new JScrollPane(jxys.getPanel());
        optScPane = new JScrollPane(optPanel);
        split_hor.add(optScPane, 0);
        split_hor.add(graphScPane, 1);
        split_vert.add(split_hor, 0);
        split_vert.add(plotScPane, 1);
        add(split_vert);

        plotAllGraphs();

    }

    private void initGraphLoad() {

        Runnable r = new Runnable() {

            GraphProperties prop;

            String[] colors;

            int color_cnt;

            public void run() {
               
                for (int k = 0; k < graphCount; k++) {
                  
                    
                    prop = new GraphProperties(thisJXY);
                   
                    
                    prop.setIndex(k);
                    prop.setSelectedColumn(columns[k]);
                    prop.setXSeries(columns[0]);
                    prop.setSelectedRows(rows);

                    if (k == 0) { //initial x axis
                        
                        prop.getIsXAxisButton().setSelected(true);
                        prop.setIsXSeries(true);
                        prop.getDataChoice().setEnabled(false);
                        prop.getDataChoiceSTART().setEnabled(true);
                        prop.getDataChoiceEND().setEnabled(true);
                        prop.getColorChoice().setEnabled(false);
                        prop.getPosChoice().setEnabled(false);
                        prop.getLegendField().setEnabled(false);
                        prop.getCustomizeButton().setEnabled(false);

                        prop.getAddButton().setEnabled(false);
                        prop.getRemButton().setEnabled(false);
                        prop.getUpButton().setEnabled(false);
                        prop.getDownButton().setEnabled(false);
                    } else {
                        prop.getMaxButton().setEnabled(false);
                        prop.getDataChoiceSTART().setEnabled(false);
                        prop.getDataChoiceEND().setEnabled(false);
                    }
                    prop.getAddButton().setEnabled(true);
                    prop.getRemButton().setEnabled(true);
                    prop.getUpButton().setEnabled(true);
                    prop.getDownButton().setEnabled(true);

                    if (k < 9) {
                        color_cnt = k;
                    } else {
                        color_cnt = 0;
                    }
                    colors = getColorScheme(color_cnt);
                    prop.setSeriesPaint(colorTable.get(colors[0]));
                    prop.setSeriesFillPaint(colorTable.get(colors[1]));
                    prop.setSeriesOutlinePaint(colorTable.get(colors[2]));
                    prop.setLinesVisible(false);
                    prop.setLinesVisBox(false);
                    prop.setName(table.getColumnName(k + 1));
                    prop.setLegendName(table.getColumnName(k + 1));
                    propVector.add(k, prop);

                }

                //initial data intervals
                range = setDataIntervals();

                //set data intervals
                for (int k = 0; k < propVector.size(); k++) {

                    prop = propVector.get(k);

                    prop.setXIntervals(range);
                    if (k == x_series_index) {
                        prop.setDataSTART(row_start);
                        prop.setDataEND(row_end);
                    }


                    prop.applyXYProperties();
                    addPropGroup(propVector.get(k));

                    prop.setColorLabelColor();

                }
            }
        };

        WorkerDlg dlg = new WorkerDlg(parent, "Preparing Data...");
//        Point parentloc = parent.getLocation();
//        dlg.setLocation(parentloc.x + 30, parentloc.y + 30);
        
        dlg.setTask(r);
        dlg.execute();

        finishGroupUI();
    }

    private void writeSortedData(int x_col) {

        int row_nr;
        if (tempLoaded) {
            row_nr = table.getRowCount();
        } else {
            row_nr = rows.length;
        }

        int col_nr = table.getColumnCount();
        sorted_Row = new XYRow[row_nr];
        double[] rowarray;

        for (int i = 0; i < row_nr; i++) {

            rowarray = new double[col_nr];
            for (int j = 0; j < col_nr; j++) {
                try {
                    rowarray[j] = (Double) table.getValueAt(i, j);

                } catch (ClassCastException cce) {
                    rowarray[j] = 0;
                }
            }
            //double[] array = rowarray;
            sorted_Row[i] = new XYRow(rowarray, x_col);

        }
        java.util.Arrays.sort(sorted_Row);
    }

    private void resortData(int x_col) {
        for (int i = 0; i < sorted_Row.length; i++) {
            sorted_Row[i].setCompareIndex(x_col);
        }
        java.util.Arrays.sort(sorted_Row);
    }

    public int[] setDataIntervals() {

//        double row_start = (Double) table.getValueAt(rowSelection[0], x_series_col);
//        double row_end = (Double) table.getValueAt(rowSelection[rowSelection.length - 1], x_series_col);
        int[] range = new int[2];
        int x_column = propVector.get(x_series_index).getSelectedColumn();

        this.row_start = sorted_Row[0].col[x_column];
        this.row_end = sorted_Row[sorted_Row.length - 1].col[x_column];
        range[0] = 0;
        range[1] = sorted_Row.length - 1;

        dStartChanged(false);
        dEndChanged(false);

        return range;
    }

    public void setMaxDataIntervals(GraphProperties x_prop) {

        int[] range = new int[2];
        resortData(x_prop.getSelectedColumn());
        range = setDataIntervals();
        this.range = range;
        x_prop.setXIntervals(range);
        x_prop.setDataSTART(this.row_start);
        x_prop.setDataEND(this.row_end);
        dStartChanged(false);
        dEndChanged(false);
    }

    public void dStartChanged(boolean state) {
        this.d_start_changed = state;
    }

    public void dEndChanged(boolean state) {
        this.d_end_changed = state;
    }

    public int[] setPossibleDataIntervals() {


        double possible_start, possible_end;

        int[] range = new int[2];
        GraphProperties x_prop = propVector.get(x_series_index);

        if (!d_start_changed || !d_end_changed) {
            if (!d_start_changed) {
                range[0] = this.range[0];

            } else {

                double start = x_prop.readDataSTART();
                double end = x_prop.readDataEND();

                int i = 0;
                int x_col = x_prop.getSelectedColumn();
                boolean out_of_boundaries = (start < sorted_Row[0].col[x_col]) || (start > sorted_Row[sorted_Row.length - 1].col[x_col]);

                if (end < start) {
                    end = start;
                }

                if (!out_of_boundaries) {

                    while (!(start >= sorted_Row[i].col[x_col] && start <= sorted_Row[i + 1].col[x_col])) {
                        i++;
                    }

                    if (start == sorted_Row[i].col[x_col]) {
                        start = sorted_Row[i].col[x_col];
                        range[0] = i;
                    } else {
                        start = sorted_Row[i + 1].col[x_col];
                        range[0] = i + 1;
                    }

                } else {
                    if (start < sorted_Row[0].col[x_col]) {
                        start = sorted_Row[0].col[x_col];
                        range[0] = 0;
                    }
                    if (start > sorted_Row[sorted_Row.length - 1].col[x_col]) {
                        start = sorted_Row[sorted_Row.length - 1].col[x_col];
                        range[0] = sorted_Row.length - 1;
                    }
                }
                this.row_start = start;
            }


            if (!d_end_changed) {
                range[1] = this.range[1];

            } else {
                double start = x_prop.readDataSTART();
                double end = x_prop.readDataEND();

                int i = 0;
                int x_col = x_prop.getSelectedColumn();
                boolean out_of_boundaries = (end < sorted_Row[0].col[x_col]) || (end > sorted_Row[sorted_Row.length - 1].col[x_col]);
                if (!out_of_boundaries) {

                    while (!(end >= sorted_Row[i].col[x_col] && end <= sorted_Row[i + 1].col[x_col])) {
                        i++;
                    }

                    if (end == sorted_Row[i + 1].col[x_col]) {
                        end = sorted_Row[i + 1].col[x_col];
                        range[1] = i + 1;
                    } else {
                        end = sorted_Row[i].col[x_col];
                        range[1] = i;
                    }

                } else {
                    if (end < sorted_Row[0].col[x_col]) {
                        end = sorted_Row[0].col[x_col];
                        range[1] = 0;
                    }
                    if (end > sorted_Row[sorted_Row.length - 1].col[x_col]) {
                        end = sorted_Row[sorted_Row.length - 1].col[x_col];
                        range[1] = sorted_Row.length - 1;
                    }
                }
                this.row_end = end;

            }

        } else {



            double start = x_prop.readDataSTART();
            double end = x_prop.readDataEND();

            int i = 0;
            int x_col = x_prop.getSelectedColumn();
            boolean out_of_boundaries = (start < sorted_Row[0].col[x_col]) || (start > sorted_Row[sorted_Row.length - 1].col[x_col]);

            if (end < start) {
                end = start;
            }

            if (!out_of_boundaries) {

                while (!(start >= sorted_Row[i].col[x_col] && start <= sorted_Row[i + 1].col[x_col])) {
                    i++;
                }

                if (start == sorted_Row[i].col[x_col]) {
                    start = sorted_Row[i].col[x_col];
                    range[0] = i;
                } else {
                    start = sorted_Row[i + 1].col[x_col];
                    range[0] = i + 1;
                }
            //            if(start - sorted_Row[i].col[x_col] < sorted_Row[i+1].col[x_col] - start){
            //                start = sorted_Row[i].col[x_col];
            //                range[0] = i;
            //            }
            //            else{
            //                start = sorted_Row[i+1].col[x_col];
            //                range[0] = i+1;
            //            }


            } else {
                if (start < sorted_Row[0].col[x_col]) {
                    start = sorted_Row[0].col[x_col];
                    range[0] = 0;
                }
                if (start > sorted_Row[sorted_Row.length - 1].col[x_col]) {
                    start = sorted_Row[sorted_Row.length - 1].col[x_col];
                    range[0] = sorted_Row.length - 1;
                }
            }

            out_of_boundaries = (end < sorted_Row[0].col[x_col]) || (end > sorted_Row[sorted_Row.length - 1].col[x_col]);
            if (!out_of_boundaries) {

                while (!(end >= sorted_Row[i].col[x_col] && end <= sorted_Row[i + 1].col[x_col])) {
                    i++;
                }

                if (end == sorted_Row[i + 1].col[x_col]) {
                    end = sorted_Row[i + 1].col[x_col];
                    range[1] = i + 1;
                } else {
                    end = sorted_Row[i].col[x_col];
                    range[1] = i;
                }

            } else {
                if (end < sorted_Row[0].col[x_col]) {
                    end = sorted_Row[0].col[x_col];
                    range[1] = 0;
                }
                if (end > sorted_Row[sorted_Row.length - 1].col[x_col]) {
                    end = sorted_Row[sorted_Row.length - 1].col[x_col];
                    range[1] = sorted_Row.length - 1;
                }
            }


            this.row_start = start;
            this.row_end = end;
        }
        this.range = range;
        return range;
    }

    private String[] getColorScheme(int scheme) {

        String[] colors = new String[3]; //0 series, 1 fill, 2 outline

        switch (scheme) {
            case 0:
                colors[0] = "red";
                colors[1] = "red";
                colors[2] = "gray";
                break;

            case 1:
                colors[0] = "blue";
                colors[1] = "blue";
                colors[2] = "black";
                break;

            case 2:
                colors[0] = "green";
                colors[1] = "green";
                colors[2] = "gray";
                break;

            case 3:
                colors[0] = "black";
                colors[1] = "black";
                colors[2] = "yellow";
                break;

            case 4:
                colors[0] = "orange";
                colors[1] = "orange";
                colors[2] = "cyan";
                break;

            case 5:
                colors[0] = "cyan";
                colors[1] = "cyan";
                colors[2] = "black";
                break;

            case 6:
                colors[0] = "magenta";
                colors[1] = "yellow";
                colors[2] = "magenta";
                break;

            case 7:
                colors[0] = "lightgray";
                colors[1] = "orange";
                colors[2] = "lightgray";
                break;

            default:
                colors[0] = "red";
                colors[1] = "blue";
                colors[2] = "red";
                break;
        }
        return colors;
    }

    public void addGraph(GraphProperties prop) {

        AddGraphDlg dlg = new AddGraphDlg();
        dlg.setVisible(true);

        if (dlg.getResult()) {

            int i = propVector.indexOf(prop);
            double d_start, d_end;
            GraphProperties newProp = new GraphProperties(this);
            colour_cnt++;




            newProp.setPosition(dlg.getSide());
            i = dlg.getPosition();
            dlg.dispose();
            newProp.getDataChoice().setSelectedIndex(1);


            newProp.setSeriesPaint(Color.RED);
            newProp.setSeriesFillPaint(Color.RED);
            newProp.setSeriesOutlinePaint(Color.RED);
            newProp.setColorLabelColor();

            if (i > 0) {
                d_start = prop.getDataSTART();
                d_end = prop.getDataEND();
                newProp.setDataSTART(d_start);
                newProp.setDataEND(d_end);
                newProp.getDataChoiceSTART().setEnabled(false);
                newProp.getDataChoiceEND().setEnabled(false);
                newProp.getMaxButton().setEnabled(false);
            }
            propVector.add(i, newProp);

            graphCount = propVector.size();
            initGroupUI();
            //Renderer Box Handler
            handleRenderer();

            for (int k = 0; k < graphCount; k++) {

                newProp = propVector.get(k);
                newProp.setIndex(k);
                //prop.getPlotButton().addActionListener(plotbuttonclick);
                if (newProp.isXSeries()) {
                    x_series_index = k;
                }
                addPropGroup(newProp);



            //graphpanel.add(propVector.get(k-1).getGraphPanel());

            }
            xChanged(propVector.get(x_series_index));
            setMaxDataIntervals(propVector.get(x_series_index));

            finishGroupUI();
            //mainpanel.repaint();
            //frame.updateUI();
            //pack();
            repaint();
        }
    }

    public void removeGraph(GraphProperties prop) {

        if (graphCount > 1) {
            GraphProperties newProp;
            propVector.remove(propVector.indexOf(prop));
            graphCount = propVector.size();

            initGroupUI();

            for (int k = 0; k < graphCount; k++) {

                newProp = propVector.get(k);
//            prop.setIndex(k);
                //prop.getPlotButton().addActionListener(plotbuttonclick);

                if (newProp.isXSeries()) {
                    x_series_index = k;
                }
                addPropGroup(newProp);



            //graphpanel.add(propVector.get(k-1).getGraphPanel());

            }
            xChanged(propVector.get(x_series_index));
            setMaxDataIntervals(propVector.get(x_series_index));

            finishGroupUI();
            //mainpanel.updateUI();
//        pack();
            repaint();
        }
    }

    public void upGraph(GraphProperties prop) {

        int i = propVector.indexOf(prop);


        boolean xChanged = false;
        GraphProperties newProp;
        //GraphProperties xProp = propVector.get(x_series_index);


        if (i - 1 >= 0 && i - 1 < graphCount) {
            propVector.remove(prop);
            propVector.add(i - 1, prop);

            handleRenderer();
            initGroupUI();

            for (int k = 0; k < graphCount; k++) {

                newProp = propVector.get(k);
                //prop.setIndex(k);
                if (newProp.isXSeries()) {
                    x_series_index = k;
                    xChanged = true;
                }


                addPropGroup(newProp);
            }

            //xChanged(propVector.get(x_series_index));
            finishGroupUI();
            repaint();
        }
    }

    public void downGraph(GraphProperties prop) {

        int i = propVector.indexOf(prop);

        boolean xChanged = false;
        if (i < propVector.size()) {
            GraphProperties newProp;

            if (i + 1 >= 0 && i + 1 < graphCount) {

                propVector.remove(prop);
                propVector.add(i + 1, prop);

                graphCount = propVector.size();
                handleRenderer();
                initGroupUI();

                for (int k = 0; k < graphCount; k++) {

                    newProp = propVector.get(k);
                    //newProp.setIndex(k);
                    if (prop.isXSeries()) {
                        x_series_index = k;
                        xChanged = true;
                    }
                    //prop.getPlotButton().addActionListener(plotbuttonclick);

                    addPropGroup(newProp);
                }

                //xChanged(propVector.get(x_series_index));
                finishGroupUI();
                repaint();
            }
        }
    }

    private void updatePropVector() {

//        if(tempLoaded){
//            writeSortedData(columns[0]);
//        }
        for (int i = 0; i < propVector.size(); i++) {
            propVector.get(i).applyXYProperties();
        }
    }

    public int getRendererLeft() {
        return rLeftBox.getSelectedIndex();
    }

    public int getRendererRight() {
        return rRightBox.getSelectedIndex();
    }

    public void xChanged(GraphProperties prop) {

        int index = propVector.indexOf(prop);
        //x_series_col = columns[index];
        x_series_index = index;

        dStartChanged(true);
        dEndChanged(true);

        //resortData(propVector.get(x_series_index).getSelectedColumn());

        for (int i = 0; i < propVector.size(); i++) {
            if (i != index) {
//                propVector.get(i).setXSeries(columns[x_series_index]);
                propVector.get(i).setXSeries(propVector.get(x_series_index).getSelectedColumn());
                propVector.get(i).setXChanged(true);
                propVector.get(i).setIsXSeries(false);
                propVector.get(i).getDataChoice().setEnabled(true);
                propVector.get(i).getDataChoiceSTART().setEnabled(false);
                propVector.get(i).getDataChoiceEND().setEnabled(false);
                propVector.get(i).getColorChoice().setEnabled(true);
                propVector.get(i).getPosChoice().setEnabled(true);
                propVector.get(i).getAddButton().setEnabled(true);
                propVector.get(i).getRemButton().setEnabled(true);
                propVector.get(i).getUpButton().setEnabled(true);
                propVector.get(i).getDownButton().setEnabled(true);
                propVector.get(i).getLegendField().setEnabled(true);
                propVector.get(i).getCustomizeButton().setEnabled(true);
                propVector.get(i).getMaxButton().setEnabled(false);
            //propVector.get(i).applyXYProperties();
            }

        }
        prop.setXChanged(true);
        prop.setIsXSeries(true);
        prop.getIsXAxisButton().setEnabled(true);
        //propVector.get(index).setXSeries(propVector.get(x_series_index).getSelectedColumn());
        prop.setXSeries(prop.getSelectedColumn());
        prop.getDataChoice().setEnabled(false);
        prop.getDataChoiceSTART().setEnabled(true);
        prop.getDataChoiceEND().setEnabled(true);
        prop.getColorChoice().setEnabled(false);
        prop.getPosChoice().setEnabled(false);
        prop.getAddButton().setEnabled(false);
        prop.getRemButton().setEnabled(false);
        prop.getUpButton().setEnabled(false);
        prop.getDownButton().setEnabled(false);
        prop.getLegendField().setEnabled(false);
        prop.getCustomizeButton().setEnabled(false);
        prop.getMaxButton().setEnabled(true);
    //prop.applyXYProperties();
    }

//    public void setENDIntervals(double end){
//        for(int i=0; i<propVector.size(); i++){
//            if(propVector.get(i).isXAxis.isEnabled()) propVector.get(i).setDataEND(end);
//            
//        }
//    }
//    public void plotGraph(GraphProperties prop){
//       
//            //propVector.get(i).applyProperties();
////            if(propVector.get(i).getPosChoice().getSelectedItem() == "left"){
////                jxys.plotLeft(rLeftBox.getSelectedIndex(), edLeftField.getText(), edXAxisField.getText(), invLeftBox.isSelected());
////            }
////            if(propVector.get(i).getPosChoice().getSelectedItem() == "right"){
////                jxys.plotRight(rRightBox.getSelectedIndex(), edRightField.getText(), edXAxisField.getText(), invRightBox.isSelected());
////            }
//            if(prop.getPosChoice().getSelectedItem() == "left"){
//                jxys.plotLeft(rLeftBox.getSelectedIndex(), edLeftField.getText(), edXAxisField.getText(), invLeftBox.isSelected());
//            }
//            if(prop.getPosChoice().getSelectedItem() == "right"){
//                jxys.plotRight(rRightBox.getSelectedIndex(), edRightField.getText(), edXAxisField.getText(), invRightBox.isSelected());
//            }
//    }    
    public void plotAllGraphs() {
        updatePropVector();

        Runnable r = new Runnable() {

            public void run() {

                int l = 0;
                int r = 0;
                int corr = 0;

                int rLeft = rLeftBox.getSelectedIndex();
                int rRight = rRightBox.getSelectedIndex();

                XYItemRenderer rendererLeft = new XYLineAndShapeRenderer();
                XYItemRenderer rendererRight = new XYLineAndShapeRenderer();

                XYLineAndShapeRenderer lsr_R = new XYLineAndShapeRenderer();
                XYBarRenderer brr_R = new XYBarRenderer();
                XYDifferenceRenderer dfr_R = new XYDifferenceRenderer();
                XYAreaRenderer ar_R = new XYAreaRenderer();
                XYStepRenderer str_R = new XYStepRenderer();
                XYStepAreaRenderer sar_R = new XYStepAreaRenderer();

                XYLineAndShapeRenderer lsr_L = new XYLineAndShapeRenderer();
                XYBarRenderer brr_L = new XYBarRenderer();
                XYDifferenceRenderer dfr_L = new XYDifferenceRenderer();
                XYAreaRenderer ar_L = new XYAreaRenderer();
                XYStepRenderer str_L = new XYStepRenderer();
                XYStepAreaRenderer sar_L = new XYStepAreaRenderer();

                GraphProperties prop;
                //2 Renderer einfügen. Typ aus rLeftBox bzw rRightBox holen!
                //Switch/Case Anweisung in den Configurator packen
                //




                /////////////// In dieser Schleife Eigenschaften übernehmen!! /////////////
                for (int i = 0; i < propVector.size(); i++) {

                    prop = propVector.get(i);

                    if (prop.getPosChoice().getSelectedItem() == "left") {
                        if (!propVector.get(i).isXSeries()) {
                            l++;
                            //prop.setRendererType(rLeft);

                            switch (rLeft) {

                                case 0:
                                    lsr_L.setSeriesPaint(i - r - corr, prop.getSeriesPaint());
                                    //lsr_L.setSeriesPaint(i-r, Color.black);
                                    lsr_L.setSeriesStroke(i - r - corr, prop.getSeriesStroke());
                                    lsr_L.setSeriesShape(i - r - corr, prop.getSeriesShape());
                                    lsr_L.setSeriesShapesVisible(i - r - corr, prop.getShapesVisible());
                                    lsr_L.setSeriesLinesVisible(i - r - corr, prop.getLinesVisible());
                                    //lsr_L.setDrawOutlines(prop.getOutlineVisible());
                                    lsr_L.setUseOutlinePaint(true);
                                    lsr_L.setSeriesFillPaint(i - r - corr, prop.getSeriesFillPaint());
                                    lsr_L.setUseFillPaint(true);
                                    lsr_L.setSeriesOutlineStroke(i - r - corr, prop.getSeriesOutlineStroke());
                                    lsr_L.setSeriesOutlinePaint(i - r - corr, prop.getSeriesOutlinePaint());
                                    rendererLeft = lsr_L;
                                    break;

                                case 1:
                                    brr_L.setSeriesPaint(i - r - corr, prop.getSeriesPaint());
                                    brr_L.setSeriesStroke(i - r - corr, prop.getSeriesStroke());

                                    brr_L.setSeriesOutlineStroke(i - r - corr, prop.getSeriesOutlineStroke());
                                    brr_L.setSeriesOutlinePaint(i - r - corr, prop.getSeriesOutlinePaint());


                                    rendererLeft = brr_L;
                                    //set Margin
                                    break;

                                case 2:
                                    ar_L.setSeriesPaint(i - r - corr, prop.getSeriesPaint());
                                    ar_L.setSeriesStroke(i - r - corr, prop.getSeriesStroke());
                                    ar_L.setSeriesShape(i - r - corr, prop.getSeriesShape());
                                    ar_L.setSeriesOutlineStroke(i - r - corr, prop.getSeriesOutlineStroke());
                                    ar_L.setSeriesOutlinePaint(i - r - corr, prop.getSeriesOutlinePaint());
                                    ar_L.setOutline(prop.getOutlineVisible());
                                    //ar_L.setSeriesOu

                                    rendererLeft = ar_L;

                                    break;

                                case 3:
                                    str_L.setSeriesPaint(i - r - corr, prop.getSeriesPaint());
                                    str_L.setSeriesStroke(i - r - corr, prop.getSeriesStroke());
                                    str_L.setSeriesShape(i - r - corr, prop.getSeriesShape());
//                            str_L.setSeriesOutlineStroke(i-r, prop.getSeriesOutlineStroke());
//                            str_L.setSeriesOutlinePaint(i-r, prop.getSeriesOutlinePaint());

                                    rendererLeft = str_L;
                                    break;

                                case 4:
                                    sar_L.setSeriesPaint(i - r - corr, prop.getSeriesPaint());
                                    sar_L.setSeriesStroke(i - r - corr, prop.getSeriesStroke());
                                    sar_L.setSeriesShape(i - r - corr, prop.getSeriesShape());
                                    sar_L.setSeriesOutlineStroke(i - r - corr, prop.getSeriesOutlineStroke());
                                    sar_L.setSeriesOutlinePaint(i - r - corr, prop.getSeriesOutlinePaint());
                                    sar_L.setOutline(prop.getOutlineVisible());

                                    rendererLeft = sar_L;

                                    break;

                                case 5:
                                    dfr_L.setSeriesPaint(i - r - corr, prop.getSeriesPaint());
                                    dfr_L.setSeriesStroke(i - r - corr, prop.getSeriesStroke());
                                    dfr_L.setSeriesShape(i - r - corr, prop.getSeriesShape());
                                    dfr_L.setSeriesOutlineStroke(i - r - corr, prop.getSeriesOutlineStroke());
                                    dfr_L.setSeriesOutlinePaint(i - r - corr, prop.getSeriesOutlinePaint());
                                    dfr_L.setShapesVisible(prop.getShapesVisible());


//                            dfr_L.setNegativePaint(prop.getNegativePaint());
//                            dfr_L.setPositivePaint(prop.getNegativePaint());

                                    rendererLeft = dfr_L;

                                    break;

                                default:
                                    lsr_L.setSeriesPaint(i - r - corr, prop.getSeriesPaint());
                                    lsr_L.setSeriesStroke(i - r - corr, prop.getSeriesStroke());
                                    lsr_L.setSeriesShape(i - r - corr, prop.getSeriesShape());
                                    lsr_L.setSeriesShapesVisible(i - r - corr, prop.getShapesVisible());
                                    lsr_L.setSeriesLinesVisible(i - r - corr, prop.getLinesVisible());
                                    lsr_L.setSeriesOutlineStroke(i - r - corr, prop.getSeriesOutlineStroke());
                                    lsr_L.setSeriesOutlinePaint(i - r - corr, prop.getSeriesOutlinePaint());

                                    rendererLeft = lsr_L;
                                    break;
                            }
                        } else {
                            corr++;
                        }

                    }
                    if (prop.getPosChoice().getSelectedItem() == "right") {
                        if (!propVector.get(i).isXSeries()) {
                            r++;
                        }
                        //prop.setRendererType(rRight);
                        switch (rRight) {
                            case 0:
                                lsr_R.setSeriesPaint(i - l - corr, prop.getSeriesPaint());
                                lsr_R.setSeriesStroke(i - l - corr, prop.getSeriesStroke());
                                lsr_R.setSeriesShape(i - l - corr, prop.getSeriesShape());
                                lsr_R.setSeriesShapesVisible(i - l - corr, prop.getShapesVisible());
                                lsr_R.setSeriesLinesVisible(i - l - corr, prop.getLinesVisible());
                                //lsr_R.setDrawOutlines(prop.getOutlineVisible());
                                lsr_R.setUseOutlinePaint(true);
                                lsr_R.setSeriesFillPaint(i - r - corr, prop.getSeriesFillPaint());
                                lsr_R.setUseFillPaint(true);
                                lsr_R.setSeriesOutlineStroke(i - l - corr, prop.getSeriesOutlineStroke());
                                lsr_R.setSeriesOutlinePaint(i - l - corr, prop.getSeriesOutlinePaint());

                                rendererRight = lsr_R;
                                break;

                            case 1:
                                brr_R.setSeriesPaint(i - l - corr, prop.getSeriesPaint());
                                brr_R.setSeriesStroke(i - l - corr, prop.getSeriesStroke());
                                brr_R.setSeriesOutlineStroke(i - l - corr, prop.getSeriesOutlineStroke());
                                brr_R.setSeriesOutlinePaint(i - l - corr, prop.getSeriesOutlinePaint());

                                rendererRight = brr_R;
                                //set Margin
                                break;

                            case 2:
                                ar_R.setSeriesPaint(i - l - corr, prop.getSeriesPaint());
                                ar_R.setSeriesStroke(i - l - corr, prop.getSeriesStroke());
                                ar_R.setSeriesShape(i - l - corr, prop.getSeriesShape());
                                ar_R.setSeriesOutlineStroke(i - l - corr, prop.getSeriesOutlineStroke());
                                ar_R.setSeriesOutlinePaint(i - l - corr, prop.getSeriesOutlinePaint());

                                rendererRight = ar_R;

                                break;

                            case 3:
                                str_R.setSeriesPaint(i - l - corr, prop.getSeriesPaint());
                                str_R.setSeriesStroke(i - l - corr, prop.getSeriesStroke());
                                str_R.setSeriesShape(i - l - corr, prop.getSeriesShape());
                                str_R.setSeriesOutlineStroke(i - l - corr, prop.getSeriesOutlineStroke());
                                str_R.setSeriesOutlinePaint(i - l - corr, prop.getSeriesOutlinePaint());

                                rendererRight = str_R;

                                break;

                            case 4:
                                sar_R.setSeriesPaint(i - l - corr, prop.getSeriesPaint());
                                sar_R.setSeriesStroke(i - l - corr, prop.getSeriesStroke());
                                sar_R.setSeriesShape(i - l - corr, prop.getSeriesShape());
                                sar_R.setSeriesOutlineStroke(i - l - corr, prop.getSeriesOutlineStroke());
                                sar_R.setSeriesOutlinePaint(i - l - corr, prop.getSeriesOutlinePaint());

                                rendererRight = sar_R;

                                break;

                            case 5:
                                dfr_R.setSeriesPaint(i - l - corr, prop.getSeriesPaint());
                                dfr_R.setSeriesStroke(i - l - corr, prop.getSeriesStroke());
                                dfr_R.setSeriesShape(i - l - corr, prop.getSeriesShape());
                                dfr_R.setSeriesOutlineStroke(i - l - corr, prop.getSeriesOutlineStroke());
                                dfr_R.setSeriesOutlinePaint(i - l - corr, prop.getSeriesOutlinePaint());
                                dfr_R.setShapesVisible(prop.getShapesVisible());
                                rendererRight = dfr_R;

                                break;

                            default:
                                lsr_R.setSeriesPaint(i - l - corr, prop.getSeriesPaint());
                                lsr_R.setSeriesStroke(i - l - corr, prop.getSeriesStroke());
                                lsr_R.setSeriesShape(i - l - corr, prop.getSeriesShape());
                                lsr_R.setSeriesShapesVisible(i - l - corr, prop.getShapesVisible());
                                lsr_R.setSeriesLinesVisible(i - l - corr, prop.getLinesVisible());
                                lsr_R.setSeriesOutlineStroke(i - l - corr, prop.getSeriesOutlineStroke());
                                lsr_R.setSeriesOutlinePaint(i - l - corr, prop.getSeriesOutlinePaint());

                                rendererRight = lsr_R;
                                break;
                        }

                        prop.setLegendName(prop.setLegend.getText());
                        prop.setColorLabelColor();
                        prop.applyXYProperties();
                    }
                }

//            for(int i=0; i<propVector.size(); i++){
//                if(propVector.get(i).getPosChoice().getSelectedItem() == "left"){
//                    if(!propVector.get(i).isXSeries()) l++;
//                    
//                }
//                if(propVector.get(i).getPosChoice().getSelectedItem() == "right"){
//                    if(!propVector.get(i).isXSeries()) r++;
//                }
//            }
                if (l > 0) {
                    jxys.plotLeft(rendererLeft, edLeftField.getText(), edXAxisField.getText(), invLeftBox.isSelected());
                }
                if (r > 0) {
                    jxys.plotRight(rendererRight, edRightField.getText(), edXAxisField.getText(), invRightBox.isSelected());
                }
                if (r == 0 && l == 0) {
                    jxys.plotEmpty();
                }

                jxys.setTitle(edTitleField.getText());
            }
        };

        WorkerDlg dlg = new WorkerDlg(parent, "Creating Plot...");
//        Point parentloc = parent.getLocation();
//        dlg.setLocation(parentloc.x + 30, parentloc.y + 30);
//        dlg.setLocationByPlatform(true);
        dlg.setTask(r);
        dlg.execute();

        repaint();
    ///////////Runnable end /////////////////////

    }

    public void handleRenderer() {
        int r = 0, l = 0, c = 0;
        for (int i = 0; i < propVector.size(); i++) {

            if (propVector.get(i).getPosChoice().getSelectedItem() == "left") {
                if (!propVector.get(i).isXSeries()) {
                    l++;
                } else {
                    c++;
                }
            }
            if (propVector.get(i).getPosChoice().getSelectedItem() == "right") {
                if (!propVector.get(i).isXSeries()) {
                    r++;
                } else {
                    c++;
                }
            }
        }

        if ((l < 2 || l > 2) && rLeftBox.getItemCount() == 6) {
            rLeftBox.removeItemAt(5);
        }

        if ((r < 2 || r > 2) && rRightBox.getItemCount() == 6) {
            rRightBox.removeItemAt(5);
        }

        if ((l == 2) && rLeftBox.getItemCount() == 5) {
            rLeftBox.addItem("Difference");
        }

        if ((r == 2) && rRightBox.getItemCount() == 5) {
            rRightBox.addItem("Difference");
        }
    }

    private void createOptionPanel() {
        GroupLayout optLayout = new GroupLayout(optionpanel);
        optionpanel.setLayout(optLayout);
        optLayout.setAutoCreateGaps(true);
        optLayout.setAutoCreateContainerGaps(true);

        addButton.addActionListener(addbuttonclick);

        GroupLayout.SequentialGroup optHGroup = optLayout.createSequentialGroup();
        GroupLayout.SequentialGroup optVGroup = optLayout.createSequentialGroup();

        optVGroup.addGroup(optLayout.createParallelGroup().addComponent(edTitle).addComponent(edTitleField));
        optVGroup.addGroup(optLayout.createParallelGroup().addComponent(edLeft).addComponent(edLeftField));
        optVGroup.addGroup(optLayout.createParallelGroup().addComponent(edRight).addComponent(edRightField));
        optVGroup.addGroup(optLayout.createParallelGroup().addComponent(edXAxis).addComponent(edXAxisField));
        optVGroup.addGroup(optLayout.createParallelGroup().addComponent(rLeftLabel).addComponent(rLeftBox));
        optVGroup.addGroup(optLayout.createParallelGroup().addComponent(rRightLabel).addComponent(rRightBox));
        optVGroup.addGroup(optLayout.createParallelGroup().addComponent(invLeftBox).addComponent(addButton));
        optVGroup.addGroup(optLayout.createParallelGroup().addComponent(invRightBox).addComponent(applyButton));

        optHGroup.addGroup(optLayout.createParallelGroup().
                addComponent(edTitle).addComponent(edLeft).addComponent(edRight).addComponent(edXAxis).addComponent(rLeftLabel).addComponent(rRightLabel).addComponent(invLeftBox).addComponent(invRightBox));

        optHGroup.addGroup(optLayout.createParallelGroup().
                addComponent(edTitleField).addComponent(edLeftField).addComponent(edRightField).addComponent(edXAxisField).addComponent(rLeftBox).addComponent(rRightBox).addComponent(addButton).addGap(1, 1, 1).addComponent(applyButton));


        optLayout.setHorizontalGroup(optHGroup);
        optLayout.setVerticalGroup(optVGroup);
    }

    private void initGroupUI() {

        graphpanel.removeAll();

        JLabel nameLabel = new JLabel("Name");
        JLabel posLabel = new JLabel("Position");
        JLabel typeLabel = new JLabel("Colour / Position");
        JLabel colorLabel = new JLabel("Type/Colour");
        JLabel dataLabel = new JLabel("Data / Legend Entry");
        JLabel timeLabel = new JLabel("Data Range");
        JLabel emptyTimeLabel = new JLabel("    ");
        JLabel legendLabel = new JLabel("Legend Entry: ");

        gLayout = new GroupLayout(graphpanel);
        graphpanel.setLayout(gLayout);
        gLayout.setAutoCreateGaps(true);
        gLayout.setAutoCreateContainerGaps(true);

        hGroup = gLayout.createSequentialGroup();
        vGroup = gLayout.createSequentialGroup();
        group1 = gLayout.createParallelGroup();
        group2 = gLayout.createParallelGroup();
        group3 = gLayout.createParallelGroup();
        group4 = gLayout.createParallelGroup();
        group5 = gLayout.createParallelGroup();
        group6 = gLayout.createParallelGroup();
        group7 = gLayout.createParallelGroup();
        group8 = gLayout.createParallelGroup();
        group9 = gLayout.createParallelGroup();
        group10 = gLayout.createParallelGroup();
        group11 = gLayout.createParallelGroup();
        group12 = gLayout.createParallelGroup();
        group13 = gLayout.createParallelGroup();
        group14 = gLayout.createParallelGroup();
        group15 = gLayout.createParallelGroup();



        group1.addComponent(dataLabel);
        group2.addComponent(timeLabel);
        group3.addComponent(typeLabel);
        //group4.addComponent(colorLabel);

//        group9.addComponent();
//        group10.addComponent(emptyTimeLabel);
//        group11.addComponent(emptyTimeLabel);
//        group12.addComponent(emptyTimeLabel);
//        group13.addComponent(emptyTimeLabel);
//        group14.addComponent(emptyTimeLabel);
//        group15.addComponent(emptyTimeLabel);

        vGroup.addGroup(gLayout.createParallelGroup(Alignment.LEADING).addComponent(dataLabel).addComponent(timeLabel).addComponent(typeLabel));

//        vGroup.addGroup(gLayout.createParallelGroup(Alignment.BASELINE)
//        .addComponent(dataLabel).addComponent(timeLabel).addComponent(typeLabel));

    }

    private void addPropGroup(GraphProperties prop) {

        isXAxisGroup.add(prop.getIsXAxisButton());
        JLabel space1 = new JLabel(" ");
        JLabel space2 = new JLabel(" ");
        JLabel space3 = new JLabel(" ");
        JLabel space4 = new JLabel(" ");
        JLabel space5 = new JLabel("   ");
        JLabel space6 = new JLabel("   ");
        JTextField lf = prop.getLegendField();

        group6.addComponent(space5);

        group1.addComponent(prop.getDataChoice()).addComponent(lf).addGap(20);
        group2.addComponent(prop.getDataChoiceSTART()).addComponent(prop.getDataChoiceEND());
        group3.addComponent(prop.getColorChoice()).addComponent(prop.getPosChoice());
        group4.addComponent(prop.getCustomizeButton()).addComponent(prop.getMaxButton());

        group9.addComponent(space3);
        group10.addComponent(prop.getIsXAxisButton());
        group11.addComponent(space4);

        group13.addComponent(prop.getColorLabel()).addComponent(prop.getRemButton());
        group14.addComponent(prop.getUpButton());
        group15.addComponent(prop.getDownButton());

        vGroup.addGroup(gLayout.createParallelGroup(Alignment.LEADING).addComponent(prop.getDataChoice()).addComponent(prop.getDataChoiceSTART()).addComponent(space5).addComponent(prop.getColorChoice()).addComponent(space5).addComponent(prop.getCustomizeButton()).addComponent(prop.getIsXAxisButton()).addComponent(prop.getColorLabel()));
        vGroup.addGroup(gLayout.createParallelGroup(Alignment.TRAILING).addComponent(lf).addComponent(prop.getDataChoiceEND()).addComponent(prop.getMaxButton()).addComponent(prop.getPosChoice()).addComponent(space3).addComponent(space4).addComponent(prop.getRemButton()).addComponent(prop.getUpButton()).addComponent(prop.getDownButton()));
        vGroup.addGroup(gLayout.createParallelGroup().addGap(20));

//            vGroup.addGroup(gLayout.createParallelGroup(Alignment.BASELINE)
//            .addComponent(prop.getNameLabel()).addComponent(prop.getPosChoice())
//            .addComponent(prop.getTypeChoice()).addComponent(prop.getColorChoice())
//            .addComponent(prop.getDataChoice()).addComponent(prop.getLegendField())
//            .addComponent(prop.getTimeChoiceSTART()).addComponent(prop.getTimeChoiceEND())
//            .addComponent(space1).addComponent(prop.getPlotButton())
//            .addComponent(space2).addComponent(prop.getAddButton())
//            .addComponent(prop.getRemButton())
//            .addComponent(prop.getUpButton()).addComponent(prop.getDownButton()));
    }

    private void finishGroupUI() {

        hGroup.addGroup(group1);
        hGroup.addGroup(group2);
        hGroup.addGroup(group6);
        hGroup.addGroup(group3);
        hGroup.addGroup(group4);
//        hGroup.addGroup(group5);
//        hGroup.addGroup(group6);
//        hGroup.addGroup(group7);
//        hGroup.addGroup(group8);

        hGroup.addGroup(group9);
        hGroup.addGroup(group10);
        hGroup.addGroup(group11);
        hGroup.addGroup(group12);
        hGroup.addGroup(group13);
        hGroup.addGroup(group14);
        hGroup.addGroup(group15);

        gLayout.setHorizontalGroup(hGroup);
        gLayout.setVerticalGroup(vGroup);


    }

    public JPanel getPanel() {
        return mainpanel;
    }

    public JPanel getCTSPlot() {
        return ctsplot.getPanel();
    }

    public void setParent(JFrame parent) {
        this.parent = parent;
    }

    private void saveTemplate() {

        Properties properties = new Properties();
        int no_of_props = propVector.size();


        String names = "";
        String name;
        String[] legendname;
        String number = "" + no_of_props;
        String stroke_type;
        String stroke_color;
        //String stroke_color_G;
        //String stroke_color_B;
        String lines_vis;
        String shapes_vis;
        String shape_type;
        String size_type;
        String shape_color;
        //String shape_color_G;
        //String shape_color_B;
        String outline_type;
        String outline_color;
        //String outline_color_G;
        //String outline_color_B;
        Color linecolor_load;
        Color fillcolor_load;
        Color outcolor_load;

        //Header Name


        properties.setProperty("number", number);

        //Titles
        properties.setProperty("title", edTitleField.getText());
        properties.setProperty("axisLTitle", edLeftField.getText());
        properties.setProperty("axisRTitle", edRightField.getText());
        properties.setProperty("xAxisTitle", edXAxisField.getText());
        //RENDERER
        properties.setProperty("renderer_left", "" + rLeftBox.getSelectedIndex());
        properties.setProperty("renderer_right", "" + rRightBox.getSelectedIndex());
        properties.setProperty("inv_left", "" + invLeftBox.isSelected());
        properties.setProperty("inv_right", "" + invRightBox.isSelected());

        //X-Col
        properties.setProperty("x_series_index", "" + x_series_index);
        //Rows
//        properties.setProperty("selected_rows.length", "" + rows.length);
//        properties.setProperty("selected_rows", "" + rows);

        for (int i = 0; i < no_of_props; i++) {

            GraphProperties gprop = propVector.get(i);

            name = gprop.getName();
            if (i == 0) {
                names = name;
            } else {
                names = names + "," + name;
            }
            
            if(gprop.isXSeries()){
                //DATA INTERVAL
            //start
            properties.setProperty("dataSTART", ""+row_start);
            //end
            properties.setProperty("dataEND", ""+row_end);
            }

            //Legend Name
            properties.setProperty(name + ".legendname", gprop.getLegendName());
            
//            //DATA INTERVAL
//            //start
//            properties.setProperty(name + ".dataSTART", ""+gprop.getDataSTART());
//            //end
//            properties.setProperty(name + ".dataEND", ""+gprop.getDataEND());
            //POSITION left/right
            properties.setProperty(name + ".position", gprop.getPosition());
            //STROKE
            stroke_type = "" + gprop.getStrokeType();
            properties.setProperty(name + ".linestroke", stroke_type);
            //STROKE COLOR
            linecolor_load = gprop.getSeriesPaint();
            stroke_color = "" + linecolor_load.getRed() + "," + linecolor_load.getGreen() + "," + linecolor_load.getBlue();
            //stroke_color_R = "" + gprop.getSeriesPaint().getRed();
            properties.setProperty(name + ".linecolor", stroke_color);

            //LINES VISIBLE
            lines_vis = "" + gprop.getLinesVisible();
            properties.setProperty(name + ".linesvisible", lines_vis);
            //SHAPES VISIBLE
            shapes_vis = "" + gprop.getShapesVisible();
            properties.setProperty(name + ".shapesvisible", shapes_vis);
            //SHAPE TYPE
            shape_type = "" + gprop.getShapeType();
            properties.setProperty(name + ".shapetype", shape_type);
            //SHAPE SIZE
            size_type = "" + gprop.getSizeType();
            properties.setProperty(name + ".shapesize", size_type);
            //SHAPE COLOR
            fillcolor_load = gprop.getSeriesFillPaint();
            shape_color = "" + fillcolor_load.getRed() + "," + fillcolor_load.getGreen() + "," + fillcolor_load.getBlue();
            properties.setProperty(name + ".shapecolor", shape_color);
//            
            //OUTLINE STROKE
            outline_type = "" + gprop.getOutlineType();
            properties.setProperty(name + ".outlinestroke", outline_type);
            //OUTLINE COLOR
            outcolor_load = gprop.getSeriesOutlinePaint();
            outline_color = "" + outcolor_load.getRed() + "," + outcolor_load.getGreen() + "," + outcolor_load.getBlue();
            properties.setProperty(name + ".outlinecolor", outline_color);
//            
        }
        properties.setProperty("names", names);

        //Save Parameter File

        try {
            JFileChooser chooser = sheet.getTemplateChooser();
            int returnVal = chooser.showSaveDialog(thisDlg);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                FileOutputStream fout = new FileOutputStream(file);
                properties.store(fout, "");

                fout.close();
            }

        } catch (Exception fnfex) {
        }
    }

    private void loadTemplate(File templateFile) {

        //int no_of_props = propVector.size();

        Properties properties = new Properties();
        boolean load_prop = false;

        String names;
        String name;
        String stroke_color;
        String shape_color;
        String outline_color;
        int no_of_props;
        int returnVal = -1;
        
        double data_start = 0;
        double data_end = 0;
        int selected_rows[];

        try {

            FileInputStream fin = new FileInputStream(templateFile);
            properties.load(fin);
            fin.close();

        } catch (Exception fnfexc) {
            returnVal = -1;
        }

        this.propVector = new Vector<GraphProperties>();

        names = properties.getProperty("names");
        no_of_props = new Integer(properties.getProperty("number"));

        this.graphCount = no_of_props;

        StringTokenizer nameTokenizer = new StringTokenizer(names, ",");

        initGroupUI();

        x_series_index = new Integer(properties.getProperty("x_series_index"));

        ///////Runnable //////////////

        for (int i = 0; i < no_of_props; i++) {

            load_prop = false;
            GraphProperties gprop = new GraphProperties(this);
 
            if (i == x_series_index) {
                gprop.setIsXSeries(true);
                gprop.getIsXAxisButton().setSelected(true);
                
                //DATA INTERVAL
                      //start
                      data_start = new Double(properties.getProperty("dataSTART"));
                      //end
                      data_end = new Double(properties.getProperty("dataEND"));
                
            } else {
                gprop.setIsXSeries(false);
            }

            if (nameTokenizer.hasMoreTokens()) {

                name = nameTokenizer.nextToken();


                for (int k = 0; k < table.getColumnCount(); k++) {
                    if (table.getColumnName(k).compareTo(name) == 0) { //stringcompare?

                        gprop.setSelectedColumn(k);
                        load_prop = true;
                        break;
                    }
                }

                if (load_prop) {
                    //Legend Name
                    gprop.setLegendName(properties.getProperty(name + ".legendname", "legend name"));
//                    //DATA INTERVAL
//                      //start
//                      gprop.setDataSTART(new Double(properties.getProperty(name + ".dataSTART")));
//                      //end
//                      gprop.setDataEND(new Double(properties.getProperty(name + ".dataEND")));
                    //POSITION left/right
                    gprop.setPosition(properties.getProperty(name + ".position"));

                    //STROKE
                    gprop.setStroke(new Integer(properties.getProperty(name + ".linestroke", "2")));
                    gprop.setStrokeSlider(gprop.getStrokeType());

                    //STROKE COLOR
                    stroke_color = properties.getProperty(name + ".linecolor", "255,0,0");

                    StringTokenizer colorTokenizer = new StringTokenizer(stroke_color, ",");

                    gprop.setSeriesPaint(new Color(new Integer(colorTokenizer.nextToken()),
                            new Integer(colorTokenizer.nextToken()),
                            new Integer(colorTokenizer.nextToken())));

                    //LINES VISIBLE
                    boolean lv = new Boolean(properties.getProperty(name + ".linesvisible"));
                    gprop.setLinesVisible(lv);
                    gprop.setLinesVisBox(lv);
                    //SHAPES VISIBLE
                    boolean sv = new Boolean(properties.getProperty(name + ".shapesvisible"));
                    gprop.setShapesVisible(sv);
                    gprop.setShapesVisBox(sv);

                    //SHAPE TYPE AND SIZE
                    int stype = new Integer(properties.getProperty(name + ".shapetype", "0"));
                    int ssize = new Integer(properties.getProperty(name + ".shapesize"));
                    gprop.setShape(stype, ssize);
                    gprop.setShapeBox(stype);
                    gprop.setShapeSlider(ssize);

                    //SHAPE COLOR
                    shape_color = properties.getProperty(name + ".shapecolor", "255,0,0");

                    StringTokenizer shapeTokenizer = new StringTokenizer(shape_color, ",");

                    gprop.setSeriesFillPaint(new Color(new Integer(shapeTokenizer.nextToken()),
                            new Integer(shapeTokenizer.nextToken()),
                            new Integer(shapeTokenizer.nextToken())));

                    //OUTLINE STROKE
                    int os = new Integer(properties.getProperty(name + ".outlinestroke"));
                    gprop.setOutlineStroke(os);
                    gprop.setOutlineSlider(os);

                    //OUTLINE COLOR
                    outline_color = properties.getProperty(name + ".outlinecolor", "255,0,0");

                    StringTokenizer outTokenizer = new StringTokenizer(outline_color, ",");

                    gprop.setSeriesOutlinePaint(new Color(new Integer(outTokenizer.nextToken()),
                            new Integer(outTokenizer.nextToken()),
                            new Integer(outTokenizer.nextToken())));

//                gprop.applyXYProperties();


                    gprop.setColorLabelColor();
                    propVector.add(gprop);
                    addPropGroup(gprop);
                }




                //}
                //Titles
                edTitleField.setText(properties.getProperty("title"));
                edLeftField.setText(properties.getProperty("axisLTitle"));
                edRightField.setText(properties.getProperty("axisRTitle"));
                edXAxisField.setText(properties.getProperty("xAxisTitle"));
                //RENDERER
                rLeftBox.setSelectedIndex(new Integer(properties.getProperty("renderer_left")));
                rRightBox.setSelectedIndex(new Integer(properties.getProperty("renderer_right")));
                invLeftBox.setSelected(new Boolean(properties.getProperty("inv_left")));
                invRightBox.setSelected(new Boolean(properties.getProperty("inv_right")));






            }
        }

        xChanged(propVector.get(x_series_index));
//        setMaxDataIntervals(propVector.get(x_series_index));

        //range = setDataIntervals();
        
        //int[] range = new int[2];
        //x_series_index
        
        //setDataIntervals///////////////////
        this.row_start = sorted_Row[0].col[x_series_index];
        this.row_end = sorted_Row[sorted_Row.length - 1].col[x_series_index];
        range[0] = 0;
        range[1] = sorted_Row.length - 1;

        dStartChanged(false);
        dEndChanged(false);
        ////////////////////////////////////
         
        
        for (int c = 0; c < no_of_props; c++) {

            propVector.get(c).setXIntervals(range);
                    if (c == x_series_index) {
                        propVector.get(c).setDataSTART(data_start);
                        propVector.get(c).setDataEND(data_end);
                    }
            propVector.get(c).applyXYProperties();
            
//            propVector.get(c).setXIntervals(range);
        }

//        Runnable r = new Runnable() {
//
//            public void run() {
//                for (int j = 0; j < propVector.size(); j++) {
//
//                    propVector.get(j).applyXYProperties();
//
//                }
//            }
//        };

//        WorkerDlg dlg = new WorkerDlg(parent, "Preparing Data...");
////        Point parentloc = parent.getLocation();
////        dlg.setLocation(parentloc.x + 30, parentloc.y + 30);
////        dlg.setLocationByPlatform(true);
//        dlg.setTask(r);
//        dlg.execute();

        finishGroupUI();

        jxys.setPropVector(propVector);
//        jts.createPlot();
//        handleRenderer();


    /////////////////////////////////////
//        plotAllGraphs();


    }

    private void editProperties() {
        JDialog propDlg = new JDialog(parent, "Properties");
        int ct = headers.length;

        JLabel[] labels = new JLabel[ct];
        JTextField[] textFields = new JTextField[ct];
        JPanel[] inputpanels = new JPanel[ct];

        JPanel proppanel = new JPanel();
        proppanel.setLayout(new GridLayout(ct, 1));

        for (int i = 0; i < ct; i++) {
            labels[i] = new JLabel(headers[i]);
            textFields[i] = new JTextField(headers[i]);
            inputpanels[i] = new JPanel();
            inputpanels[i].setLayout(new FlowLayout());
            inputpanels[i].add(labels[i]);
            inputpanels[i].add(textFields[i]);
        }


        JScrollPane propPane = new JScrollPane(proppanel);


    }
    /****** EVENT HANDLING ******/
    ActionListener saveTempListener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            saveTemplate();
        }
    };

    ActionListener loadTempListener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {

            int returnVal = -1;

            try {
                returnVal = sheet.getTemplateChooser().showOpenDialog(thisDlg);
                File file = sheet.getTemplateChooser().getSelectedFile();


                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    loadTemplate(file);
                    plotAllGraphs();
                }

            } catch (Exception fnfexc) {
                returnVal = -1;
            }


        }
    };

    ActionListener titleListener = new ActionListener() {

        public void actionPerformed(ActionEvent te) {
            ctsplot.getChart().setTitle(edTitleField.getText());
        }
    };

    ActionListener propbuttonclick = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            ctsplot.getChartPanel().doEditChartProperties();
        }
    };

    ActionListener addbuttonclick = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            addGraph(propVector.get(0));
        }
    };

    ActionListener plotbuttonclick = new ActionListener() {

        public void actionPerformed(ActionEvent e) {

            int[] range = setPossibleDataIntervals();
            for (int i = 0; i < propVector.size(); i++) {
                GraphProperties prop = propVector.get(i);

                prop.setXIntervals(range);
//                prop.setDataSTART(row_start);
//                prop.setDataEND(row_end);
            //prop.applyXYProperties();


            }
            plotAllGraphs();
            dStartChanged(false);
            dEndChanged(false);
            setVisible(true);
        }
    };

    ActionListener actChanged = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            //timePlot();
        }
    };

    ActionListener saveImageAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {

//            showHiRes();
            JFileChooser chooser = new JFileChooser(); //ACHTUNG!!!!!!!!!
            chooser.setFileFilter(JAMSFileFilter.getEpsFilter());
            int returnVal = chooser.showSaveDialog(thisDlg);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    jxys.saveAsEPS(file);
                } catch (Exception ex) {
                }
            }
        }
    };

    public void createActionListener() {

        Vector<ActionListener> addAction = new Vector<ActionListener>();

        for (int k = 0; k < graphCount; k++) {
            /* reicht hier ein listener für alle boxes? scheint so... */
            activationChange[k] = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    //timePlot();
                }
            };
        }
    }

    private class HiResPanel extends JPanel {

        BufferedImage bi;

        public HiResPanel(BufferedImage bi) {
            this.bi = bi;

        }

        public void paint(Graphics g) {
            g.drawImage(bi, 0, 0, this);
        }
    }

    private class AddGraphDlg extends JDialog {

        boolean result = false;

        int max;

        String side;

        int side_index;

        int position;

        JSpinner posSpinner;

        JComboBox sideChoice;

        JButton okButton;

        JLabel pos_label;

        JLabel side_label;

        public AddGraphDlg() {
            super(thisDlg, "Add Graph", true);
//            URL url = this.getClass().getResource("/jams/components/gui/resources/JAMSicon16.png");
//            ImageIcon icon = new ImageIcon(url);
//            setIconImage(icon.getImage());
            Point parentloc = thisDlg.getLocation();
            setLocation(parentloc.x + 50, parentloc.y + 50);
            createPanel();
        }

        void createPanel() {
            setLayout(new FlowLayout());

            max = propVector.size();
            String[] posArray = {"left", "right"};
            posSpinner = new JSpinner(new SpinnerNumberModel(max, 0, max, 1));
            sideChoice = new JComboBox(posArray);
            sideChoice.setSelectedIndex(0);
            okButton = new JButton("OK");
            pos_label = new JLabel("position after: ");
            side_label = new JLabel("side: ");

            add(side_label);
            add(sideChoice);
            add(pos_label);
            add(posSpinner);
            add(okButton);
            okButton.addActionListener(ok);
            pack();
        }

        String getSide() {
            if (side_index == 0) {
                side = "left";
            } else {
                side = "right";
            }

            return side;
        }

        int getPosition() {
            return position;
        }

        boolean getResult() {
            return result;
        }
        ActionListener ok = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //e.getActionCommand(); ...

                side_index = sideChoice.getSelectedIndex();
                position = (Integer) posSpinner.getValue();
                result = true;
                setVisible(false);

            }
        };

    }
}

class XYRow implements Comparable<XYRow> {

    double[] col;

    int compare_index;

    public XYRow(double[] rowdata, int compare_index) {
        this.col = rowdata;
        this.compare_index = compare_index;
    }

    public void setCompareIndex(int compare_index) {
        this.compare_index = compare_index;
    }

    public int compareTo(XYRow arg) {

        if (col[compare_index] < arg.col[compare_index]) {
            return -1;
        }
        if (col[compare_index] > arg.col[compare_index]) {
            return 1;
        }
        return 0;

    }
}

