/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.spreadsheet;

//import com.sun.image.codec.jpeg.JPEGCodec;
//import com.sun.image.codec.jpeg.JPEGEncodeParam;
//import com.sun.image.codec.jpeg.JPEGImageEncoder;
import jams.data.JAMSCalendar;
import jams.data.JAMSDataFactory;
import jams.gui.WorkerDlg;
import jams.workspace.DataSet;
import jams.workspace.datatypes.DataValue;
import jams.workspace.datatypes.DoubleValue;
import jams.workspace.stores.InputDataStore;
import java.util.Vector;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.BorderFactory.*;
import javax.swing.GroupLayout.*;


import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import reg.Regionalizer;


/**
 *
 * @author Robert Riedel
 */
public class STPConfigurator extends JFrame{

    JPanel plotpanel;
    JPanel optionpanel;
    JPanel chooserpanel;
    
    JComboBox templateBox[];
    
    JButton plotbutton;
    
    JAMSSpreadSheet sheet;
    JFrame parent;
//    JTable table; //wichtig?
    File templateFile;
            
    int rows, columns, graphCount;
    String[] headers;
    
    Vector<double[]> arrayVector;
    
    Vector<GraphProperties> propVector; //for one plot!!!
    Vector<JAMSCalendar> timeVector;
    
//    static String DATASET_01 = "tmax";
//    static String DATASET_02 = "tmean";
    
    static String DATASET[] = {"tmax","tmean"};
    String[] dataset;
    
//    private JAMSTimePlot jts_01 = new JAMSTimePlot();
//    private JAMSTimePlot jts_02 = new JAMSTimePlot();
    
    private JAMSTimePlot jts[];
    
    InputDataStore store;
    
    int rLeft, rRight = 0;
    boolean invLeft, invRight = false;
    boolean timeFormat_yy, timeFormat_mm, timeFormat_dd, timeFormat_hm = true;
    
    String title, tLeft, tRight, xAxisTitle = "";
    
    JPanel plotPanel;
    
    File templateFiles[];
    int numberOfPlots;
    
    public STPConfigurator(JFrame parent, int numberOfPlots){
        
        this.parent = parent;
        this.setIconImage(parent.getIconImage());
        setTitle("StackedTimePlot Configurator");

        setLayout(new FlowLayout());
        Point parentloc = parent.getLocation();
        setLocation(parentloc.x + 30, parentloc.y + 30);

        this.numberOfPlots = numberOfPlots;
        jts = new JAMSTimePlot[numberOfPlots];
        //this.headers = new String[graphCount];

        setPreferredSize(new Dimension(1024, 768));

        createPanel();

        pack();
        setVisible(true);
    }
    
    private void createPanel(){
        
        setLayout(new BorderLayout());
        plotpanel = new JPanel();
        plotpanel.setLayout(new GridLayout(numberOfPlots, 1));
        optionpanel = new JPanel();
        chooserpanel = new JPanel();
        
        GridBagLayout gbl = new GridBagLayout();
        chooserpanel.setLayout(gbl);
        
        plotbutton = new JButton("Plot");
        
        // PROGRAMME //
        templateFiles = new File[numberOfPlots];
        templateBox = new JComboBox[numberOfPlots];
        
        plotbutton.addActionListener(plotaction);
        dataset = getAccessibleIDs();
        
        for(int c = 0; c < numberOfPlots; c++){
            templateBox[c] = new JComboBox(dataset);
            optionpanel.add(templateBox[c]);
            templateBox[c].setSelectedIndex(c);
            //LHelper.addGBComponent(chooserpanel, gbl, templateBox[c], 0, c, 1, 1, 0, 0);
        }
        optionpanel.add(plotbutton);
        
//        plotpanel = createPlotPanel();
        
        for(int i = 0; i < numberOfPlots; i++){
            String datasetID = (String)templateBox[i].getSelectedItem();
            templateFiles[i] = new File(Regionalizer.getRegionalizerFrame().getWorkspace().getInputDirectory(), datasetID +".ttp");
            loadInputDSData(datasetID);
            loadTemplate(templateFiles[i]);
            jts[i] = new JAMSTimePlot();
            jts[i].setPropVector(propVector);
            jts[i].createPlot();
//            jts[i].getPanel().add(templateBox[i]);
            
            plotpanel.add(jts[i].getPanel());
            plot(i);
            if(i < numberOfPlots - 1) jts[i].removeLegendAndXAxis();
        }
        
        add(plotpanel, BorderLayout.CENTER);
        add(optionpanel, BorderLayout.SOUTH);
        
        repaint();
    }
    
    private void repaintPlotPanel(){
        
        this.remove(plotpanel);
        JPanel plotpanel = new JPanel();
        plotpanel.setLayout(new GridLayout(numberOfPlots, 1));
        
        for(int i = 0; i < numberOfPlots; i++){
            String datasetID = (String)templateBox[i].getSelectedItem();
            templateFiles[i] = new File(Regionalizer.getRegionalizerFrame().getWorkspace().getInputDirectory(), datasetID +".ttp");
            loadInputDSData(datasetID);
            loadTemplate(templateFiles[i]);
            jts[i] = new JAMSTimePlot();
            jts[i].setPropVector(propVector);
            jts[i].createPlot();
//            jts[i].getPanel().add(templateBox[i]);
            
            plotpanel.add(jts[i].getPanel());
            plot(i);
            if(i < numberOfPlots - 1) jts[i].removeLegendAndXAxis();
        }
        
        add(plotpanel, BorderLayout.CENTER);
        pack();
        repaint();
        
    }
   
    private InputDataStore getInputDataStore(String datasetID){
        
        InputDataStore store = Regionalizer.getRegionalizerFrame().getWorkspace().getInputDataStore(datasetID);
        
        return store;
    }
    
    private String[] getAccessibleIDs(){
        
        int totalIDs = 0;
        int accessibleIDs = 0;
        int failedIDs = 0;
        ArrayList<String> accIDList = new ArrayList<String>();
        String[] accIDArray;
        
        Set<String> idSet;
        idSet = Regionalizer.getRegionalizerFrame().getWorkspace().getInputDataStoreIDs();
        totalIDs = idSet.size();
        String idArray[] = new String[totalIDs];
        idArray = idSet.toArray(idArray);
        
        for(int i = 0; i < totalIDs; i++){
            
            File testFile = new File(Regionalizer.getRegionalizerFrame().getWorkspace().getInputDirectory(), idArray[i] +".ttp");
            try {
                FileInputStream fin = new FileInputStream(testFile);
                fin.close();
                accIDList.add(idArray[i]);
            }   catch (IOException ioe) {
                    failedIDs++;
            }
            
        }
        accessibleIDs = accIDList.size();
        accIDArray = new String[accessibleIDs];
        accIDArray = accIDList.toArray(accIDArray);
        
        System.out.println("Accessible IDs: "+accessibleIDs);
        return accIDArray;
    }
    
    private void loadInputDSData(String datasetID){
        
        arrayVector = new Vector<double[]>();
        timeVector = new Vector<JAMSCalendar>();
        
        double rowBuffer[];
        this.store = getInputDataStore(datasetID);

        ArrayList<Object> names = store.getDataSetDefinition().getAttributeValues("NAME");
        columns = store.getDataSetDefinition().getColumnCount();
        headers = new String[columns + 1];
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

            rowBuffer = new double[columns];
            for (i = 1; i < rowData.length; i++) {
                rowBuffer[i - 1] = ((DoubleValue) rowData[i]).getDouble();
            }
            arrayVector.add(rowBuffer);
        }
        rows = arrayVector.size();
    }
    
    private void loadTimeIntervals(File[] templateFiles){
            
            String timeSTART = "";
            String timeEND = "";
            String names ="";
            String name = "";
            
            int no_of_props;
            boolean loadProp = false;
            
            Properties properties = new Properties();
            
            for(int i=0; i<numberOfPlots; i++){

                try {
                    FileInputStream fin = new FileInputStream(templateFiles[i]);
                    properties.load(fin);
                    fin.close();
                } catch (Exception e) {
                }
                
                names = properties.getProperty("names");
                no_of_props = new Integer(properties.getProperty("number"));
                StringTokenizer nameTokenizer = new StringTokenizer(names, ",");
                
                for(int c=0; c<no_of_props; c++){
                    if (nameTokenizer.hasMoreTokens()) {

                        name = nameTokenizer.nextToken();

                        for (int k = 0; k < columns; k++) {
                            if (headers[k].compareTo(name) == 0) { //stringcompare?

                                loadProp = true;
                                break;
                            }
                        }
                        if(loadProp){
                            if(i == 0){
                                timeSTART = (String)properties.getProperty(name + ".timeSTART");
                                timeEND = (String)properties.getProperty(name + ".timeEND");
                            } else {
                                String read_tStart = (String)properties.getProperty(name + ".timeSTART");
                                if(read_tStart.compareTo(timeSTART) < 0) timeSTART = read_tStart;
                                String read_tEnd = (String)properties.getProperty(name + ".timeEND");
                                if(read_tEnd.compareTo(timeEND) > 0) timeEND = read_tEnd;
                            }
                        }
                    }
                }
        }
    }
    //attention: just for ONE tempFile
    private void loadTemplate(File templateFile) {
            
    Properties properties = new Properties();
        boolean load_prop = false;

        String names;
        String name;
        String stroke_color;
        String shape_color;
        String outline_color;
        int no_of_props;
        int returnVal = -1;

        try {
            FileInputStream fin = new FileInputStream(templateFile);
            properties.load(fin);
            fin.close();
        } catch (Exception e) {
        }

        this.propVector = new Vector<GraphProperties>();

        names = (String) properties.getProperty("names");
        System.out.println(names);
        no_of_props = new Integer(properties.getProperty("number"));

        this.graphCount = no_of_props;

        StringTokenizer nameTokenizer = new StringTokenizer(names, ",");

        for (int i = 0; i < no_of_props; i++) {

            load_prop = false;
            GraphProperties gprop = new GraphProperties(this);

            if (nameTokenizer.hasMoreTokens()) {

                name = nameTokenizer.nextToken();

                for (int k = 0; k < columns; k++) {
                    if (headers[k+1].compareTo(name) == 0) {

                        gprop.setSelectedColumn(k);
                        load_prop = true;
                        break;
                    }
                }

                boolean readStart = false, readEnd = false;
                gprop.setTimeSTART(0);
                gprop.setTimeEND(rows - 1);

                if (load_prop) {
                    //Legend Name
                    gprop.setLegendName(properties.getProperty(name + ".legendname", "legend name"));
                    //POSITION left/right
                    gprop.setPosition(properties.getProperty(name + ".position"));
                    //INTERVAL
                    String timeSTART = properties.getProperty(name + ".timeSTART");
                    String timeEND = properties.getProperty(name + ".timeEND");
                    String read = null;

                    System.out.println("start setting intervals...");
                    for (int tc = 0; tc < rows; tc++) {

                        if (readStart && readEnd) {
                            break;
                        }

                        read = gprop.getTimeChoiceSTART().getItemAt(tc).toString();

                        if (!readStart) {
                            //start
                            if (read.equals(timeSTART)) {
                                gprop.setTimeSTART(tc);
                                readStart = true;
                            }
                        } else {
                            //end
                            if (read.equals(timeEND)) {
                                gprop.setTimeEND(tc);
                                readEnd = true;
                            }
                        }
                    }
                    System.out.println("interval set");

//                    gprop.setTimeSTART(0);
//                    gprop.setTimeEND(table.getRowCount() - 1);

                    //NAME
                    gprop.setName(name);

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

                    gprop.setColorLabelColor();
                    propVector.add(gprop);
                    //addPropGroup(gprop);
                }
            }
        }
        System.out.println("Props loaded");

        //////////////// hier implementieren!! /////////////////////////
        //}
        //Titles
        title = (String) properties.getProperty("title");
        tLeft = (String) properties.getProperty("axisLTitle");
        tRight = (String)properties.getProperty("axisRTitle");
        xAxisTitle = (String) properties.getProperty("xAxisTitle");
        //RENDERER
        rLeft = new Integer(properties.getProperty("renderer_left"));
        rRight = new Integer(properties.getProperty("renderer_right"));
        invLeft = new Boolean(properties.getProperty("inv_left"));
        invRight = new Boolean(properties.getProperty("inv_right"));

        //TimeFormat
        timeFormat_yy = new Boolean(properties.getProperty("timeFormat_yy"));
        timeFormat_mm = new Boolean(properties.getProperty("timeFormat_mmy"));
        timeFormat_dd = new Boolean(properties.getProperty("timeFormat_dd"));
        timeFormat_hm = new Boolean(properties.getProperty("timeFormat_hm"));

//        jts.setPropVector(propVector);



    }
    
    private void updatePropVector() {

        for (int i = 0; i < propVector.size(); i++) {
            propVector.get(i).applySTPProperties(arrayVector, timeVector);
        }
    }
    
    public void plot(int plot_index) {

        final int index = plot_index;

        Runnable r = new Runnable() {
            
            
            @Override
            public void run() {

                updatePropVector();

                int l = 0;
                int r = 0;

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
                //2 Renderer einfÃƒÂ¼gen. Typ aus rLeftBox bzw rRightBox holen!
                //Switch/Case Anweisung in den Configurator packen
                //

                /////////////// In dieser Schleife Eigenschaften ÃƒÂ¼bernehmen!! /////////////
                for (int i = 0; i < propVector.size(); i++) {

                    prop = propVector.get(i);

//                prop.setLegendName((String)prop.setColumn.getSelectedItem());
//                prop.setName((String)prop.setColumn.getSelectedItem());

                    if (prop.getPosChoice().getSelectedItem() == "left") {
                        l++;
                        //prop.setRendererType(rLeft);

                        switch (rLeft) {

                            case 0:
                                lsr_L.setSeriesPaint(i - r, prop.getSeriesPaint());
                                //lsr_L.setSeriesPaint(i-r, Color.black);
                                lsr_L.setSeriesStroke(i - r, prop.getSeriesStroke());
                                lsr_L.setSeriesShape(i - r, prop.getSeriesShape());
                                lsr_L.setSeriesShapesVisible(i - r, prop.getShapesVisible());
                                lsr_L.setSeriesLinesVisible(i - r, prop.getLinesVisible());
                                //lsr_L.setDrawOutlines(prop.getOutlineVisible());
                                lsr_L.setUseOutlinePaint(true);
                                lsr_L.setSeriesFillPaint(i - r, prop.getSeriesFillPaint());
                                lsr_L.setUseFillPaint(true);
                                lsr_L.setSeriesOutlineStroke(i - r, prop.getSeriesOutlineStroke());
                                lsr_L.setSeriesOutlinePaint(i - r, prop.getSeriesOutlinePaint());
                                rendererLeft = lsr_L;
                                break;

                            case 1:
                                brr_L.setSeriesPaint(i - r, prop.getSeriesPaint());
                                brr_L.setSeriesStroke(i - r, prop.getSeriesStroke());

                                brr_L.setSeriesOutlineStroke(i - r, prop.getSeriesOutlineStroke());
                                brr_L.setSeriesOutlinePaint(i - r, prop.getSeriesOutlinePaint());


                                rendererLeft = brr_L;
                                //set Margin
                                break;

                            case 2:
                                ar_L.setSeriesPaint(i - r, prop.getSeriesPaint());
                                ar_L.setSeriesStroke(i - r, prop.getSeriesStroke());
                                ar_L.setSeriesShape(i - r, prop.getSeriesShape());
                                ar_L.setSeriesOutlineStroke(i - r, prop.getSeriesOutlineStroke());
                                ar_L.setSeriesOutlinePaint(i - r, prop.getSeriesOutlinePaint());
                                ar_L.setOutline(prop.getOutlineVisible());
                                //ar_L.setSeriesOu

                                rendererLeft = ar_L;

                                break;

                            case 3:
                                str_L.setSeriesPaint(i - r, prop.getSeriesPaint());
                                str_L.setSeriesStroke(i - r, prop.getSeriesStroke());
                                str_L.setSeriesShape(i - r, prop.getSeriesShape());
//                            str_L.setSeriesOutlineStroke(i-r, prop.getSeriesOutlineStroke());
//                            str_L.setSeriesOutlinePaint(i-r, prop.getSeriesOutlinePaint());

                                rendererLeft = str_L;
                                break;

                            case 4:
                                sar_L.setSeriesPaint(i - r, prop.getSeriesPaint());
                                sar_L.setSeriesStroke(i - r, prop.getSeriesStroke());
                                sar_L.setSeriesShape(i - r, prop.getSeriesShape());
                                sar_L.setSeriesOutlineStroke(i - r, prop.getSeriesOutlineStroke());
                                sar_L.setSeriesOutlinePaint(i - r, prop.getSeriesOutlinePaint());
                                sar_L.setOutline(prop.getOutlineVisible());

                                rendererLeft = sar_L;

                                break;

                            case 5:
                                dfr_L.setSeriesPaint(i - r, prop.getSeriesPaint());
                                dfr_L.setSeriesStroke(i - r, prop.getSeriesStroke());
                                dfr_L.setSeriesShape(i - r, prop.getSeriesShape());
                                dfr_L.setSeriesOutlineStroke(i - r, prop.getSeriesOutlineStroke());
                                dfr_L.setSeriesOutlinePaint(i - r, prop.getSeriesOutlinePaint());
                                dfr_L.setShapesVisible(prop.getShapesVisible());


//                            dfr_L.setNegativePaint(prop.getNegativePaint());
//                            dfr_L.setPositivePaint(prop.getNegativePaint());

                                rendererLeft = dfr_L;

                                break;

                            default:
                                lsr_L.setSeriesPaint(i - r, prop.getSeriesPaint());
                                lsr_L.setSeriesStroke(i - r, prop.getSeriesStroke());
                                lsr_L.setSeriesShape(i - r, prop.getSeriesShape());
                                lsr_L.setSeriesShapesVisible(i - r, prop.getShapesVisible());
                                lsr_L.setSeriesLinesVisible(i - r, prop.getLinesVisible());
                                lsr_L.setSeriesOutlineStroke(i - r, prop.getSeriesOutlineStroke());
                                lsr_L.setSeriesOutlinePaint(i - r, prop.getSeriesOutlinePaint());

                                rendererLeft = lsr_L;
                                break;
                        }

                    }
                    if (prop.getPosChoice().getSelectedItem() == "right") {
                        r++;
                        //prop.setRendererType(rRight);
                        switch (rRight) {
                            case 0:
                                lsr_R.setSeriesPaint(i - l, prop.getSeriesPaint());
                                lsr_R.setSeriesStroke(i - l, prop.getSeriesStroke());
                                lsr_R.setSeriesShape(i - l, prop.getSeriesShape());
                                lsr_R.setSeriesShapesVisible(i - l, prop.getShapesVisible());
                                lsr_R.setSeriesLinesVisible(i - l, prop.getLinesVisible());
                                //lsr_R.setDrawOutlines(prop.getOutlineVisible());
                                lsr_R.setUseOutlinePaint(true);
                                lsr_R.setSeriesFillPaint(i - l, prop.getSeriesFillPaint());
                                lsr_R.setUseFillPaint(true);
                                lsr_R.setSeriesOutlineStroke(i - l, prop.getSeriesOutlineStroke());
                                lsr_R.setSeriesOutlinePaint(i - l, prop.getSeriesOutlinePaint());

                                rendererRight = lsr_R;
                                break;

                            case 1:
                                brr_R.setSeriesPaint(i - l, prop.getSeriesPaint());
                                brr_R.setSeriesStroke(i - l, prop.getSeriesStroke());
                                brr_R.setSeriesOutlineStroke(i - l, prop.getSeriesOutlineStroke());
                                brr_R.setSeriesOutlinePaint(i - l, prop.getSeriesOutlinePaint());

                                rendererRight = brr_R;
                                //set Margin
                                break;

                            case 2:
                                ar_R.setSeriesPaint(i - l, prop.getSeriesPaint());
                                ar_R.setSeriesStroke(i - l, prop.getSeriesStroke());
                                ar_R.setSeriesShape(i - l, prop.getSeriesShape());
                                ar_R.setSeriesOutlineStroke(i - l, prop.getSeriesOutlineStroke());
                                ar_R.setSeriesOutlinePaint(i - l, prop.getSeriesOutlinePaint());

                                rendererRight = ar_R;

                                break;

                            case 3:
                                str_R.setSeriesPaint(i - l, prop.getSeriesPaint());
                                str_R.setSeriesStroke(i - l, prop.getSeriesStroke());
                                str_R.setSeriesShape(i - l, prop.getSeriesShape());
                                str_R.setSeriesOutlineStroke(i - l, prop.getSeriesOutlineStroke());
                                str_R.setSeriesOutlinePaint(i - l, prop.getSeriesOutlinePaint());

                                rendererRight = str_R;

                                break;

                            case 4:
                                sar_R.setSeriesPaint(i - l, prop.getSeriesPaint());
                                sar_R.setSeriesStroke(i - l, prop.getSeriesStroke());
                                sar_R.setSeriesShape(i - l, prop.getSeriesShape());
                                sar_R.setSeriesOutlineStroke(i - l, prop.getSeriesOutlineStroke());
                                sar_R.setSeriesOutlinePaint(i - l, prop.getSeriesOutlinePaint());

                                rendererRight = sar_R;

                                break;

                            case 5:
                                dfr_R.setSeriesPaint(i - l, prop.getSeriesPaint());
                                dfr_R.setSeriesStroke(i - l, prop.getSeriesStroke());
                                dfr_R.setSeriesShape(i - l, prop.getSeriesShape());
                                dfr_R.setSeriesOutlineStroke(i - l, prop.getSeriesOutlineStroke());
                                dfr_R.setSeriesOutlinePaint(i - l, prop.getSeriesOutlinePaint());
                                dfr_R.setShapesVisible(prop.getShapesVisible());
                                rendererRight = dfr_R;

                                break;

                            default:
                                lsr_R.setSeriesPaint(i - l, prop.getSeriesPaint());
                                lsr_R.setSeriesStroke(i - l, prop.getSeriesStroke());
                                lsr_R.setSeriesShape(i - l, prop.getSeriesShape());
                                lsr_R.setSeriesShapesVisible(i - l, prop.getShapesVisible());
                                lsr_R.setSeriesLinesVisible(i - l, prop.getLinesVisible());
                                lsr_R.setSeriesOutlineStroke(i - l, prop.getSeriesOutlineStroke());
                                lsr_R.setSeriesOutlinePaint(i - l, prop.getSeriesOutlinePaint());

                                rendererRight = lsr_R;
                                break;
                        }

                        prop.setLegendName(prop.setLegend.getText());
                        prop.setColorLabelColor();
                        System.out.println("...ApplySTPProperties");
                        prop.applySTPProperties(arrayVector, timeVector);
                        System.out.println("...applyed!");
                    }
                }

                ////////////////////////////////////////////////////////////////////////////
                //Renderer direkt ÃƒÂ¼bernehmen! //
                System.out.println("Plot left/right");
                if (l > 0) {
                    jts[index].plotLeft(rendererLeft, tLeft, xAxisTitle, invLeft);
                }
                if (r > 0) {
                    jts[index].plotRight(rendererRight, tRight, xAxisTitle, invRight);
                }
                if (r == 0 && l == 0) {
                    jts[index].plotEmpty();
                }

                jts[index].setTitle(title);
                jts[index].setDateFormat(timeFormat_yy, timeFormat_mm,
                        timeFormat_dd, timeFormat_hm);

            }
        };

        WorkerDlg dlg = new WorkerDlg(parent, "Creating Plot...");
//        Point parentloc = parent.getLocation();
//        dlg.setLocation(parentloc.x + 30, parentloc.y + 30);
//        dlg.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2,
//                Toolkit.getDefaultToolkit().getScreenSize().height / 2);
        dlg.setTask(r);
        dlg.execute();
        
        repaint();
        
    }
    
    public int getRowCount(){
        return rows;
    }
    
    public int getColumnCount(){
        return columns;
    }
    
    public String[] getHeaders(){
        return headers;
    }
    
    public Vector<double[]> getArrayVector(){
        return arrayVector;
    }
    
    ActionListener plotaction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            repaintPlotPanel();
            setVisible(true);
        }
    };

}
