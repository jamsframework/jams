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
import jams.JAMSFileFilter;

import jams.gui.tools.GUIHelper;
import jams.workspace.JAMSWorkspace;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import reg.JAMSExplorer;

/**
 *
 * @author Robert Riedel
 */
public class JXYConfigurator extends JFrame {
    
    private JAMSWorkspace workspace;
    
    
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
 

    private JFrame parent;
    private JFrame thisDlg;
    private JXYConfigurator thisJXY = this;
    private JPanel frame;
    private JPanel mainpanel;
    private JPanel plotpanel;
    private JPanel optionpanel;
    private JPanel graphpanel;
    private JPanel southpanel;

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


    private boolean output_ttp = false;

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

    private String[] types = {"Line and Shape", "Bar", "Area", "Step", "StepArea", "Difference"};
    private JComboBox rLeftBox = new JComboBox(types);
    private JComboBox rRightBox = new JComboBox(types);
    private JCheckBox invLeftBox = new JCheckBox("Invert left Axis");
    private JCheckBox invRightBox = new JCheckBox("Invert right Axis");
    private ButtonGroup isXAxisGroup = new ButtonGroup();
    private JButton applyButton = new JButton("PLOT");
    private JButton addButton = new JButton("Add Graph");
    private JButton saveButton = new JButton("EPS export");

    private Vector<GraphProperties> propVector = new Vector<GraphProperties>();
    private Vector<PropertyPanel> panelVector = new Vector<PropertyPanel>();

    private JAMSXYPlot jxys = new JAMSXYPlot();
    public XYRow[] sorted_Row;
    private boolean tempLoaded = true;

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

    JCheckBox[] activate;

    JComboBox[] datachoice;

    JComboBox[] poschoice;

    JComboBox[] typechoice;

    JComboBox[] colorchoice;

    ActionListener[] activationChange;

    JAMSSpreadSheet sheet;


    public JXYConfigurator(JFrame parent, JAMSSpreadSheet sheet, File templateFile, JAMSExplorer explorer) {

        this.setParent(parent);
        this.setIconImage(parent.getIconImage());
        setTitle(SpreadsheetConstants.DLG_TITLE_JXYSCONFIGURATOR);
        
        this.workspace = explorer.getWorkspace();
        
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        explorer.registerChild(this);

        setLayout(new FlowLayout());
        Point parentloc = explorer.getExplorerFrame().getLocation();
        setLocation(parentloc.x + 30, parentloc.y + 30);
        
        this.sheet = sheet;
        this.table = sheet.table;
        this.templateFile = templateFile;

        this.rows = table.getSelectedRows();
        this.columns = table.getSelectedColumns();
        this.graphCount = columns.length;
        this.headers = new String[graphCount];/* hier aufpassen bei reselection xxx reselecton -> neue instanz */

        for(int k=0;k<graphCount;k++){
            headers[k] = table.getColumnName(columns[k]);
        }

        d_start_changed = false;
        d_end_changed = false;

        if (templateFile == null) {
            tempLoaded = false;
        }

        if ((isDataTemplate(templateFile) && table.getColumnCount() > 1) || !tempLoaded){
            
            try {
                writeSortedData(columns[0]);
            } catch (java.lang.ArrayIndexOutOfBoundsException aiofb) {
                writeSortedData(0);
            }

            createPanel();

            pack();
            setVisible(true);


        }else{

            GUIHelper.showErrorDlg(sheet, SpreadsheetConstants.JXY_ERR_NODATATEMPLATE, "Error");
        }
    }

    private boolean isDataTemplate(File templateFile){

        Properties properties = new Properties();
        boolean result = false;

        try{
            FileInputStream fin = new FileInputStream(templateFile);
            properties.load(fin);
            fin.close();
        }catch(Exception ex){
            result = false;
        }
        try{
            String typetest = (String) properties.getProperty("template_type");
            if (typetest.compareTo("DATA") == 0) {
                result = true;
            } else {
                result = false;
            }

        } catch (NullPointerException npe) {
            result = false;
        }


        return result;
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

        graphScPane = new JScrollPane();

        optionpanel = new JPanel();

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

        optionpanel.add(edTitle);
        optionpanel.add(edTitleField);
        optionpanel.add(edLeft);
        optionpanel.add(edLeftField);
        optionpanel.add(edRight);
        optionpanel.add(edRightField);
        optionpanel.add(applyButton);

        rLeftBox.setSelectedIndex(0);
        rLeftBox.addActionListener(rendererColorListener);
        rRightBox.setSelectedIndex(0);
        rRightBox.addActionListener(rendererColorListener);

        ////////////////////////// GRAPH AUSFÜHREN ///////////
        if ((templateFile != null) && (templateFile.exists())) {
            try {
                if(!output_ttp){
                    loadTemplate(templateFile);
                }
                else{
//                    loadOutputTemplate(templateFile);
                    loadTemplate(templateFile);
                    
                }
            } catch (Exception fnfe) {
//                System.out.println("ERROR");
                Logger.getLogger(JXYConfigurator.class.getName()).log(Level.SEVERE, null, fnfe);

                initGraphLoad();
            }
        } else {

            initGraphLoad();
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
        graphScPane.setSize(new Dimension(512, 300));
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

        

            GraphProperties prop;

            String[] colors;

            int color_cnt;

           

                
                for (int k = 0; k < graphCount; k++) {
                  
                    
                    prop = new GraphProperties(thisJXY);
                   
                    
                    prop.setIndex(k);
                    prop.setSelectedColumn(columns[k]);
                    prop.setXSeries(columns[0]);
                    prop.setSelectedRows(rows);

                    if (k == 0) { //initial x axis
                        
                        prop.setIsXSeries(true);
                    } else {
                        prop.setIsXSeries(false);
                    }
//                    prop.getAddButton().setEnabled(true);
//                    prop.getRemButton().setEnabled(true);
//                    prop.getUpButton().setEnabled(true);
//                    prop.getDownButton().setEnabled(true);

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
                    prop.setShapesVisible(true);
//                    prop.setLinesVisBox(false);

                    prop.setStroke(SpreadsheetConstants.JXYS_DEFAULT_STROKE);
//                    prop.setStrokeSlider(SpreadsheetConstants.JXYS_DEFAULT_STROKE);
                    prop.setShape(SpreadsheetConstants.JXYS_DEFAULT_SHAPE, SpreadsheetConstants.JTS_DEFAULT_SHAPE_SIZE);
//                    prop.setShapeSlider(SpreadsheetConstants.JXYS_DEFAULT_SHAPE_SIZE);
//                    prop.setShapeBox(SpreadsheetConstants.JXYS_DEFAULT_SHAPE);
//                    prop.setName(table.getColumnName(k + 1));
//                    prop.setLegendName(table.getColumnName(k + 1));

                    String s =table.getColumnName(columns[k]);
                    prop.setLegendName(s);
                    prop.setName(s);
//                    prop.setLegendField(s);

                    propVector.add(k, prop);

//                    PropertyPanel ppanel = new PropertyPanel(thisJXY, prop);
                   
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

                    PropertyPanel ppanel = new PropertyPanel(thisJXY, prop);
                    panelVector.add(ppanel);

                    if(panelVector.size() > 0){
                    setXGUI(prop,ppanel);
            }
                    addPropGroup(panelVector.get(k));

                }  
            
        

       

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



        dStartChanged(true);
        dEndChanged(true);

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

    private void setXDataIntervals(){

    }

    public int[] setPossibleDataIntervals() {

        
        double possible_start, possible_end;

        int[] range = new int[2];
        GraphProperties x_prop = propVector.get(x_series_index);

        if (!d_start_changed || !d_end_changed) {
           
            if (!d_start_changed) {
                range[0] = this.range[0];

            } else {
//READ DATA START!!!!
                double start = x_prop.getDataSTART();
                double end = x_prop.getDataEND();
                                             
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
//READ DATA START!!!!
                double start = x_prop.getDataSTART();
                double end = x_prop.getDataEND();

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


//READ DATA START!!!!
            double start = x_prop.getDataSTART();
            double end = x_prop.getDataEND();
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
            int pos = dlg.getPosition();
            dlg.dispose();
//            newProp.getDataChoice().setSelectedIndex(1);


             newProp.setSeriesPaint(Color.BLACK);
            newProp.setSeriesFillPaint(Color.BLACK);
            newProp.setSeriesOutlinePaint(Color.BLACK);

            newProp.setSelectedColumn(prop.getSelectedColumn());
            newProp.setLegendName(prop.getLegendName());

            newProp.setLinesVisible(prop.getLinesVisible());
            newProp.setShapesVisible(prop.getShapesVisible());

            newProp.setStroke(prop.getStrokeType());
            newProp.setShape(prop.getShapeType(), prop.getSizeType());
            newProp.setOutlineStroke(prop.getOutlineType());
//            newProp.setColorLabelColor();

            if (pos >= 0) {
                d_start = prop.getDataSTART();
                d_end = prop.getDataEND();
                newProp.setDataSTART(d_start);
                newProp.setDataEND(d_end);
//                newProp.getDataChoiceSTART().setEnabled(false);
//                newProp.getDataChoiceEND().setEnabled(false);
//                newProp.getMaxButton().setEnabled(false);
            }
            PropertyPanel newPPanel = new PropertyPanel(thisJXY, newProp);
            propVector.add(pos, newProp);
            panelVector.add(pos, newPPanel);

            graphCount = propVector.size();
            initGroupUI();
            //Renderer Box Handler
            handleRenderer();

            for (int k = 0; k < graphCount; k++) {

                newProp = propVector.get(k);
                newProp.setIndex(k);
                newPPanel = panelVector.get(k);


                if (newProp.isXSeries()) {
                    x_series_index = k;
                }
                addPropGroup(newPPanel);

            }
            xChanged(propVector.get(x_series_index));
            setMaxDataIntervals(propVector.get(x_series_index));

            finishGroupUI();
 
            repaint();

        }
    }

    public void removeGraph(GraphProperties prop) {

        int i = propVector.indexOf(prop);
//        PropertyPanel ppanel = panelVector.get(i);

        if (graphCount > 1) {
            GraphProperties newProp;
            PropertyPanel newPPanel;
            propVector.remove(i);
            panelVector.remove(i);
            graphCount = propVector.size();

            handleRenderer();

            initGroupUI();

            for (int k = 0; k < graphCount; k++) {

                newProp = propVector.get(k);
                newPPanel = panelVector.get(k);

                if (newProp.isXSeries()) {
                    x_series_index = k;
                }
                addPropGroup(newPPanel);

            }
            xChanged(propVector.get(x_series_index));
            setMaxDataIntervals(propVector.get(x_series_index));

            finishGroupUI();

            repaint();
        }
    }

    public void upGraph(GraphProperties prop) {

        int i = propVector.indexOf(prop);
        PropertyPanel ppanel = panelVector.get(i);

        boolean xChanged = false;
        GraphProperties newProp;
        PropertyPanel newPPanel;

        if (i - 1 >= 0 && i - 1 < graphCount) {
            propVector.remove(prop);
            panelVector.remove(i);
            propVector.add(i - 1, prop);
            panelVector.add(i - 1, ppanel);

            handleRenderer();
            initGroupUI();

            for (int k = 0; k < graphCount; k++) {

                newProp = propVector.get(k);
                newPPanel = panelVector.get(k);

                if (newProp.isXSeries()) {
                    x_series_index = k;
                    xChanged = true;
                }


                addPropGroup(newPPanel);
            }

            finishGroupUI();
            repaint();
        }
    }

    public void downGraph(GraphProperties prop) {

        int i = propVector.indexOf(prop);
        PropertyPanel ppanel = panelVector.get(i);

        boolean xChanged = false;
        if (i < propVector.size()) {
            GraphProperties newProp;
            PropertyPanel newPPanel;

            if (i + 1 >= 0 && i + 1 < graphCount) {

                propVector.remove(prop);
                panelVector.remove(i);
                propVector.add(i + 1, prop);
                panelVector.add(i+1, ppanel);

                graphCount = propVector.size();
                handleRenderer();
                initGroupUI();

                for (int k = 0; k < graphCount; k++) {

                    newProp = propVector.get(k);
                    newPPanel = panelVector.get(k);

                    if (prop.isXSeries()) {
                        x_series_index = k;
                        xChanged = true;
                    }

                    addPropGroup(newPPanel);
                }

                finishGroupUI();
                repaint();
            }
        }
    }

    private void updatePropVector() {

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

        x_series_index = index;

        dStartChanged(true);
        dEndChanged(true);

        for (int i = 0; i < propVector.size(); i++) {

        prop.setXChanged(true);
        prop.setIsXSeries(true);
//        prop.getIsXAxisButton().setEnabled(true);
        //propVector.get(index).setXSeries(propVector.get(x_series_index).getSelectedColumn());
        prop.setXSeries(prop.getSelectedColumn());

            if (i != index) {
//                propVector.get(i).setXSeries(columns[x_series_index]);
                propVector.get(i).setXSeries(propVector.get(x_series_index).getSelectedColumn());
                propVector.get(i).setXChanged(true);
                propVector.get(i).setIsXSeries(false);

            }else{
                prop.setPosition(0);
            }

            if(panelVector.size() > 0){
                setXGUI(propVector.get(i),panelVector.get(i));
            }
        
        }


        
    }

    public void setXGUI(GraphProperties prop, PropertyPanel panel){

        if(prop.isXSeries()){
            panel.isXAxis.setSelected(true);
            panel.setDataSTART(prop.getDataSTART());
            panel.setDataEND(prop.getDataEND());

            panel.customizeButton.setEnabled(false);
            panel.poschoice.setEnabled(false);

            panel.datachoice_START.setEnabled(true);
            panel.datachoice_END.setEnabled(true);
            panel.datachoice_max.setEnabled(true);

            panel.remButton.setEnabled(false);

            panel.poschoice.setSelectedItem(prop.getPosition());
            

        }else{
            panel.isXAxis.setSelected(false);

            panel.customizeButton.setEnabled(true);
            panel.poschoice.setEnabled(true);

            panel.datachoice_START.setEnabled(false);
            panel.datachoice_END.setEnabled(false);
            panel.datachoice_max.setEnabled(false);

            panel.remButton.setEnabled(true);
        }

    }


    public void plotAllGraphs() {
        updatePropVector();

//        Runnable r = new Runnable() {

//            public void run() {

                int l = 0;
                int r = 0;
                int x_pos = 0;
                boolean x_passed = false;

                int index_l = 0;
                int index_r = 0;

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

                /////////////// In dieser Schleife Eigenschaften übernehmen!! /////////////
                for (int i = 0; i < propVector.size(); i++) {

                    prop = propVector.get(i);

                    if(!x_passed) x_pos++;

                    if (prop.getPosition() == 0) {
                        if (!propVector.get(i).isXSeries()) {
                            l++;

                            

                            if((i < x_pos)){
                                index_l = i-r;
                            } else {
                                index_l = i-r-1;
                            }

                           

                            switch (rLeft) {

                                case 0:
                                    lsr_L.setSeriesPaint(index_l, prop.getSeriesPaint());

                                    lsr_L.setSeriesStroke(index_l, prop.getSeriesStroke());
                                    lsr_L.setSeriesShape(index_l, prop.getSeriesShape());
                                    lsr_L.setSeriesShapesVisible(index_l, prop.getShapesVisible());
                                    lsr_L.setSeriesLinesVisible(index_l, prop.getLinesVisible());
                                    lsr_L.setDrawOutlines(prop.getOutlineVisible());
                                    lsr_L.setUseOutlinePaint(true);
                                    lsr_L.setSeriesFillPaint(index_l, prop.getSeriesFillPaint());
                                    lsr_L.setUseFillPaint(true);
                                    lsr_L.setSeriesOutlineStroke(index_l, prop.getSeriesOutlineStroke());
                                    lsr_L.setSeriesOutlinePaint(index_l, prop.getSeriesOutlinePaint());
                                    
                                    rendererLeft = lsr_L;
                                    break;

                                case 1:
                                    brr_L.setSeriesPaint(index_l, prop.getSeriesPaint());
                                    brr_L.setSeriesStroke(index_l, prop.getSeriesStroke());

                                    brr_L.setSeriesOutlineStroke(index_l, prop.getSeriesOutlineStroke());
                                    brr_L.setSeriesOutlinePaint(index_l, prop.getSeriesOutlinePaint());


                                    rendererLeft = brr_L;
                                    //set Margin
                                    break;

                                case 2:
                                    ar_L.setSeriesPaint(index_l, prop.getSeriesPaint());
                                    ar_L.setSeriesStroke(index_l, prop.getSeriesStroke());
                                    ar_L.setSeriesShape(index_l, prop.getSeriesShape());
                                    ar_L.setSeriesOutlineStroke(index_l, prop.getSeriesOutlineStroke());
                                    ar_L.setSeriesOutlinePaint(index_l, prop.getSeriesOutlinePaint());
                                    ar_L.setOutline(prop.getOutlineVisible());
                                   

                                    rendererLeft = ar_L;

                                    break;

                                case 3:
                                    str_L.setSeriesPaint(index_l, prop.getSeriesPaint());
                                    str_L.setSeriesStroke(index_l, prop.getSeriesStroke());
                                    str_L.setSeriesShape(index_l, prop.getSeriesShape());

                                    rendererLeft = str_L;
                                    break;

                                case 4:
                                    sar_L.setSeriesPaint(index_l, prop.getSeriesPaint());
                                    sar_L.setSeriesStroke(index_l, prop.getSeriesStroke());
                                    sar_L.setSeriesShape(index_l, prop.getSeriesShape());
                                    sar_L.setSeriesOutlineStroke(index_l, prop.getSeriesOutlineStroke());
                                    sar_L.setSeriesOutlinePaint(index_l, prop.getSeriesOutlinePaint());
                                    sar_L.setOutline(prop.getOutlineVisible());

                                    rendererLeft = sar_L;

                                    break;

                                case 5:
                                    dfr_L.setSeriesPaint(index_l, prop.getSeriesPaint());
                                    dfr_L.setSeriesStroke(index_l, prop.getSeriesStroke());
                                    dfr_L.setSeriesShape(index_l, prop.getSeriesShape());
                                    dfr_L.setSeriesOutlineStroke(index_l, prop.getSeriesOutlineStroke());
                                    dfr_L.setSeriesOutlinePaint(index_l, prop.getSeriesOutlinePaint());
                                    dfr_L.setShapesVisible(prop.getShapesVisible());

                                    rendererLeft = dfr_L;
                                    break;

                                default:
                                    lsr_L.setSeriesPaint(index_l, prop.getSeriesPaint());
                                    lsr_L.setSeriesStroke(index_l, prop.getSeriesStroke());
                                    lsr_L.setSeriesShape(index_l, prop.getSeriesShape());
                                    lsr_L.setSeriesShapesVisible(index_l, prop.getShapesVisible());
                                    lsr_L.setSeriesLinesVisible(index_l, prop.getLinesVisible());
                                    lsr_L.setSeriesOutlineStroke(index_l, prop.getSeriesOutlineStroke());
                                    lsr_L.setSeriesOutlinePaint(index_l, prop.getSeriesOutlinePaint());

                                    rendererLeft = lsr_L;
                                    break;
                            }
                        } else {
                           // x_pos++;
                            x_passed = true;
//                            l--;
                        }

                    }
                    if (prop.getPosition() == 1) {
                        if (!propVector.get(i).isXSeries()) {
                            r++;

                            if((i < x_pos)){
                                index_r = i-l;
                            } else {
                                index_r = i-l-1;
                            }
                           

                        switch (rRight) {
                            case 0:
                                lsr_R.setSeriesPaint(index_r, prop.getSeriesPaint());
                                lsr_R.setSeriesStroke(index_r, prop.getSeriesStroke());
                                lsr_R.setSeriesShape(index_r, prop.getSeriesShape());
                                lsr_R.setSeriesShapesVisible(index_r, prop.getShapesVisible());
                                lsr_R.setSeriesLinesVisible(index_r, prop.getLinesVisible());
                                lsr_R.setDrawOutlines(prop.getOutlineVisible());
                                lsr_R.setUseOutlinePaint(true);
                                lsr_R.setSeriesFillPaint(index_r, prop.getSeriesFillPaint());
                                lsr_R.setUseFillPaint(true);
                                lsr_R.setSeriesOutlineStroke(index_r, prop.getSeriesOutlineStroke());
                                lsr_R.setSeriesOutlinePaint(index_r, prop.getSeriesOutlinePaint());

                                rendererRight = lsr_R;
                                break;

                            case 1:
                                brr_R.setSeriesPaint(index_r, prop.getSeriesPaint());
                                brr_R.setSeriesStroke(index_r, prop.getSeriesStroke());
                                brr_R.setSeriesOutlineStroke(index_r, prop.getSeriesOutlineStroke());
                                brr_R.setSeriesOutlinePaint(index_r, prop.getSeriesOutlinePaint());

                                rendererRight = brr_R;
                                //set Margin
                                break;

                            case 2:
                                ar_R.setSeriesPaint(index_r, prop.getSeriesPaint());
                                ar_R.setSeriesStroke(index_r, prop.getSeriesStroke());
                                ar_R.setSeriesShape(index_r, prop.getSeriesShape());
                                ar_R.setSeriesOutlineStroke(index_r, prop.getSeriesOutlineStroke());
                                ar_R.setSeriesOutlinePaint(index_r, prop.getSeriesOutlinePaint());

                                rendererRight = ar_R;

                                break;

                            case 3:
                                str_R.setSeriesPaint(index_r, prop.getSeriesPaint());
                                str_R.setSeriesStroke(index_r, prop.getSeriesStroke());
                                str_R.setSeriesShape(index_r, prop.getSeriesShape());
                                str_R.setSeriesOutlineStroke(index_r, prop.getSeriesOutlineStroke());
                                str_R.setSeriesOutlinePaint(index_r, prop.getSeriesOutlinePaint());

                                rendererRight = str_R;

                                break;

                            case 4:
                                sar_R.setSeriesPaint(index_r, prop.getSeriesPaint());
                                sar_R.setSeriesStroke(index_r, prop.getSeriesStroke());
                                sar_R.setSeriesShape(index_r, prop.getSeriesShape());
                                sar_R.setSeriesOutlineStroke(index_r, prop.getSeriesOutlineStroke());
                                sar_R.setSeriesOutlinePaint(index_r, prop.getSeriesOutlinePaint());

                                rendererRight = sar_R;

                                break;

                            case 5:
                                dfr_R.setSeriesPaint(index_r, prop.getSeriesPaint());
                                dfr_R.setSeriesStroke(index_r, prop.getSeriesStroke());
                                dfr_R.setSeriesShape(index_r, prop.getSeriesShape());
                                dfr_R.setSeriesOutlineStroke(index_r, prop.getSeriesOutlineStroke());
                                dfr_R.setSeriesOutlinePaint(index_r, prop.getSeriesOutlinePaint());
                                dfr_R.setShapesVisible(prop.getShapesVisible());

                                rendererRight = dfr_R;
                                break;

                            default:
                                lsr_R.setSeriesPaint(index_r, prop.getSeriesPaint());
                                lsr_R.setSeriesStroke(index_r, prop.getSeriesStroke());
                                lsr_R.setSeriesShape(index_r, prop.getSeriesShape());
                                lsr_R.setSeriesShapesVisible(index_r, prop.getShapesVisible());
                                lsr_R.setSeriesLinesVisible(index_r, prop.getLinesVisible());
                                lsr_R.setSeriesOutlineStroke(index_r, prop.getSeriesOutlineStroke());
                                lsr_R.setSeriesOutlinePaint(index_r, prop.getSeriesOutlinePaint());

                                rendererRight = lsr_R;
                                break;
                        }
                        }


//                        prop.setColorLabelColor();

                    }
                }

                jxys.setPropVector(propVector);

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
//            }
//        };

//        WorkerDlg dlg = new WorkerDlg(parent, "Creating Plot...");
//
//        dlg.setTask(r);
//        dlg.execute();

        repaint();
    ///////////Runnable end /////////////////////

    }

    public void handleRenderer() {
        int r = 0, l = 0, c = 0;
        for (int i = 0; i < propVector.size(); i++) {

            if (propVector.get(i).getPosition() == 0) {
                if (!propVector.get(i).isXSeries()) {
                    l++;
                } else {
                    c++;
                }
            }
            if (propVector.get(i).getPosition() == 1) {
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

        JLabel typeLabel = new JLabel("Position");

        JLabel dataLabel = new JLabel("Data / Legend Entry");
        JLabel timeLabel = new JLabel("Data Range");


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

        vGroup.addGroup(gLayout.createParallelGroup(Alignment.LEADING).addComponent(dataLabel).addComponent(timeLabel).addComponent(typeLabel));

    }

    private void addPropGroup(PropertyPanel ppanel) {

        isXAxisGroup.add(ppanel.getIsXAxisButton());

        JLabel space3 = new JLabel(" ");
        JLabel space4 = new JLabel(" ");
        JLabel space5 = new JLabel("   ");
 
        JLabel lf = ppanel.getLegendLabel();

        group6.addComponent(space5);

        group1.addComponent(ppanel.getDataChoice()).addComponent(lf).addGap(20);
        group2.addComponent(ppanel.getDataChoiceSTART()).addComponent(ppanel.getDataChoiceEND());

        group3.addComponent(ppanel.getMaxButton()).addComponent(ppanel.getPosChoice());

        group4.addComponent(ppanel.getCustomizeButton()).addComponent(space5);

        group9.addComponent(space3);
        group10.addComponent(ppanel.getIsXAxisButton());
        group11.addComponent(space4);

        group13.addComponent(ppanel.getColorLabel()).addComponent(ppanel.getRemButton());
        group14.addComponent(ppanel.getUpButton());
        group15.addComponent(ppanel.getDownButton());

        vGroup.addGroup(gLayout.createParallelGroup(Alignment.LEADING).addComponent(ppanel.getDataChoice()).addComponent(ppanel.getDataChoiceSTART()).addComponent(space5).addComponent(ppanel.getMaxButton()).addComponent(space5).addComponent(ppanel.getCustomizeButton()).addComponent(ppanel.getIsXAxisButton()).addComponent(ppanel.getColorLabel()));
        vGroup.addGroup(gLayout.createParallelGroup(Alignment.TRAILING).addComponent(lf).addComponent(ppanel.getDataChoiceEND()).addComponent(space5).addComponent(ppanel.getPosChoice()).addComponent(space3).addComponent(space4).addComponent(ppanel.getRemButton()).addComponent(ppanel.getUpButton()).addComponent(ppanel.getDownButton()));
        vGroup.addGroup(gLayout.createParallelGroup().addGap(20));


    }

    private void finishGroupUI() {

        hGroup.addGroup(group1);
        hGroup.addGroup(group2);
        hGroup.addGroup(group6);
        hGroup.addGroup(group3);
        hGroup.addGroup(group4);

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

    private String saveTemplate() {

        Properties properties = new Properties();
        int no_of_props = propVector.size();
        
        String store = "";
        try{

            store = sheet.getID();
        } catch (NullPointerException npe){
            store = "DEFAULT";
        }
        String output;

        String names = "";
        String name;
        String[] legendname;
        String number = "" + no_of_props;
        String stroke_type;
        String stroke_color;

        String lines_vis;
        String shapes_vis;
        String shape_type;
        String size_type;
        String shape_color;

        String outline_type;
        String outline_color;
        //String outline_vis;

        Color linecolor_load;
        Color fillcolor_load;
        Color outcolor_load;
        
        //Input / Output Sheet
        if(sheet.isOutputSheet()){
            output = "true";
        }else{
            output = "false";
        }
        
        properties.setProperty("output", output);
        
        //TYPE
        properties.setProperty("template_type", "DATA");


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
            //TIME INTERVAL
                    //start
                    properties.setProperty(name + ".timeSTART", "DEFAULT");
                    //end
                    properties.setProperty(name + ".timeEND", "DEFAULT");

            //Legend Name
            properties.setProperty(name + ".legendname", gprop.getLegendName());
            
//            //DATA INTERVAL
//            //start
//            properties.setProperty(name + ".dataSTART", ""+gprop.getDataSTART());
//            //end
//            properties.setProperty(name + ".dataEND", ""+gprop.getDataEND());
            //POSITION left/right
            properties.setProperty(name + ".position", ""+gprop.getPosition());
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
            //outline_vis = "" + gprop.getOutlineVisible();
            //properties.setProperty(name + ".outlinevis", outline_vis);
            //OUTLINE COLOR
            outcolor_load = gprop.getSeriesOutlinePaint();
            outline_color = "" + outcolor_load.getRed() + "," + outcolor_load.getGreen() + "," + outcolor_load.getBlue();
            properties.setProperty(name + ".outlinecolor", outline_color);
//            
        }
        properties.setProperty("names", names);

        //Save Parameter File
        //Save Parameter File
        String filename = "";
        String inputString = "";
        StringTokenizer name_tokenizer = new StringTokenizer(store,".");
        String storename = "";
        if(name_tokenizer.hasMoreTokens()){
            storename = name_tokenizer.nextToken();
        }else{
            storename=store;
        }
        
        
       try {

            boolean dont_save = true;
            while(dont_save){
                inputString = GUIHelper.showInputDlg(this, SpreadsheetConstants.INFO_MSG_SAVETEMP, storename);
                if(!(inputString == null)){
                    
                    inputString+= ".ttp";
                 

                    if(sheet.isOutputSheet()){
//                       
                        File file = new File(sheet.getOutputDSDir(), inputString);

                        if(!file.exists()){
                            filename = file.getName();
                            FileOutputStream fout = new FileOutputStream(file);
                            properties.store(fout, "");
                            fout.close();
                            dont_save = false;
                        }else{
                            String fileexists = "The File "+file+" already exists.\n Overwrite?";
                            int result = GUIHelper.showYesNoDlg(this, fileexists, "File already exists");
                            if(result==0){ //overwrite
                                filename = file.getName();
                                FileOutputStream fout = new FileOutputStream(file);
                                properties.store(fout, "");
                                fout.close();
                                dont_save = false;
                            }

                        }
                    }else{
                        File file = new File(workspace.getDirectory().toString()+SpreadsheetConstants.FILE_EXPLORER_DIR_NAME, inputString);
                        if(!file.exists()){
                            filename = file.getName();
                            FileOutputStream fout = new FileOutputStream(file);
                            properties.store(fout, "");
                            fout.close();
                            dont_save = false;
                        }else{
                            String fileexists = "The File "+file+" already exists.\n Overwrite?";
                            int result = GUIHelper.showYesNoDlg(this, fileexists, "File already exists");
                            if(result==0){ //overwrite
                                filename = file.getName();
                                FileOutputStream fout = new FileOutputStream(file);
                                properties.store(fout, "");
                                fout.close();
                                dont_save = false;
                            }

                        }
                    }
                    }else{
                        dont_save = false; //CANCEL OPTION!
                    }
                }
//            }
        } catch (Exception fnfex) {
            Logger.getLogger(JXYConfigurator.class.getName()).log(Level.SEVERE, null, fnfex);
        }
        
        return filename;
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
        
        String x_series_index_string = (String) properties.getProperty("x_series_index");
        if(x_series_index_string.compareTo("DEFAULT") != 0){
            x_series_index = new Integer(x_series_index_string);
        }else{

            if(sheet.timeRuns()) x_series_index = 1;
            else x_series_index = 0;
        }
        
        
        
        ///////Runnable //////////////

        for (int i = 0; i < no_of_props; i++) {

            load_prop = false;
            GraphProperties gprop = new GraphProperties(this);


            if (i == x_series_index) {
                gprop.setIsXSeries(true);
//                gprop.getIsXAxisButton().setSelected(true);

                String data_start_string = (String) properties.getProperty("dataSTART");
                //DATA INTERVAL
                if(data_start_string.compareTo("DEFAULT") != 0){
                      //start
                      data_start = new Double(properties.getProperty("dataSTART"));
                      this.dStartChanged(true);
                      //end
                      data_end = new Double(properties.getProperty("dataEND"));
                      this.dEndChanged(true);
                }
                else{
                    this.setPossibleDataIntervals();
                }
                
            } else {

                gprop.setIsXSeries(false);
            }
            gprop.setXSeries(x_series_index);


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
//                    gprop.setLegendField(properties.getProperty(name + ".legendname", "legend name"));

                    //POSITION left/right
                    int pos = new Integer(properties.getProperty(name + ".position"));
                    gprop.setPosition(pos);
                    
                    
                    //NAME
                    gprop.setName(name);
                    
                    //STROKE
                    gprop.setStroke(new Integer(properties.getProperty(name + ".linestroke", "2")));
//                    gprop.setStrokeSlider(gprop.getStrokeType());

                    //STROKE COLOR
                    stroke_color = properties.getProperty(name + ".linecolor", "255,0,0");

                    StringTokenizer colorTokenizer = new StringTokenizer(stroke_color, ",");

                    gprop.setSeriesPaint(new Color(new Integer(colorTokenizer.nextToken()),
                            new Integer(colorTokenizer.nextToken()),
                            new Integer(colorTokenizer.nextToken())));

                    //LINES VISIBLE
                    boolean lv = new Boolean(properties.getProperty(name + ".linesvisible"));
                    gprop.setLinesVisible(lv);
//                    gprop.setLinesVisBox(lv);
                    //SHAPES VISIBLE
                    boolean sv = new Boolean(properties.getProperty(name + ".shapesvisible"));
                    gprop.setShapesVisible(sv);
//                    gprop.setShapesVisBox(sv);

                    //SHAPE TYPE AND SIZE
                    int stype = new Integer(properties.getProperty(name + ".shapetype", "0"));
                    int ssize = new Integer(properties.getProperty(name + ".shapesize"));
                    gprop.setShape(stype, ssize);
//                    gprop.setShapeBox(stype);
//                    gprop.setShapeSlider(ssize);

                    //SHAPE COLOR
                    shape_color = properties.getProperty(name + ".shapecolor", "255,0,0");

                    StringTokenizer shapeTokenizer = new StringTokenizer(shape_color, ",");

                    gprop.setSeriesFillPaint(new Color(new Integer(shapeTokenizer.nextToken()),
                            new Integer(shapeTokenizer.nextToken()),
                            new Integer(shapeTokenizer.nextToken())));

                    //OUTLINE STROKE
                    int os = new Integer(properties.getProperty(name + ".outlinestroke"));
                    gprop.setOutlineStroke(os);
//                    gprop.setOutlineSlider(os);

                    //OUTLINE COLOR
                    outline_color = properties.getProperty(name + ".outlinecolor", "255,0,0");

                    StringTokenizer outTokenizer = new StringTokenizer(outline_color, ",");

                    gprop.setSeriesOutlinePaint(new Color(new Integer(outTokenizer.nextToken()),
                            new Integer(outTokenizer.nextToken()),
                            new Integer(outTokenizer.nextToken())));

//                  gprop.applyXYProperties();


//                  gprop.setColorLabelColor();
                    propVector.add(gprop);

//                    PropertyPanel ppanel = new PropertyPanel(this.thisJXY,gprop);
//                    panelVector.add(ppanel);
//
//                    addPropGroup(ppanel);
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

        for (int c = 0; c < no_of_props; c++) {

            if (c == x_series_index) {
                this.resortData(propVector.get(c).getSelectedColumn());
                propVector.get(c).setDataSTART(data_start);
                propVector.get(c).setDataEND(data_end);
            }
        }
        range = setPossibleDataIntervals();
        for (int c = 0; c < propVector.size(); c++) {
            
            propVector.get(c).setXIntervals(range);
            propVector.get(c).applyXYProperties();
            PropertyPanel ppanel = new PropertyPanel(this.thisJXY, propVector.get(c));
                    panelVector.add(ppanel);
                     if(panelVector.size() > 0){
                    setXGUI(propVector.get(c),ppanel);
                     }

                    addPropGroup(ppanel);

        }



        finishGroupUI();

        jxys.setPropVector(propVector);

    /////////////////////////////////////


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
            String fileID = saveTemplate();
            StringTokenizer name_tokenizer = new StringTokenizer(fileID,".");
            String filename = "";
            if(name_tokenizer.hasMoreTokens()){
                filename = name_tokenizer.nextToken()+SpreadsheetConstants.FILE_ENDING_DAT;
            }else{
                filename = fileID+SpreadsheetConstants.FILE_ENDING_DAT;
            }
//            System.out.println("output_sheet="+sheet.isOutputSheet());
            if(sheet.isOutputSheet()){
//                String[] headers_with_time = new String[headers.length+1];
//                headers_with_time[0] = "ID";
//                java.lang.System.arraycopy(headers, 0, headers_with_time, 1, headers.length);
//                sheet.save(filename, headers_with_time);//String ID zurückgeben
                sheet.saveAll(filename);
                //daten speichern im falle eines output sheets
            }
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

    ActionListener rendererColorListener = new ActionListener() {

        public void actionPerformed(ActionEvent te) {
            for(int i=0;i<panelVector.size();i++){
                panelVector.get(i).setColorLabelColor();

            }
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

        int getSide() {
//            if (side_index == 0) {
//                side = "left";
//            } else {
//                side = "right";
//            }

            return side_index;
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



