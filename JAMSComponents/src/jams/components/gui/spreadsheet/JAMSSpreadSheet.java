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

import jams.components.gui.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.awt.event.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import java.io.*;

import java.util.Iterator;

import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;

import org.jfree.chart.*;
import org.jfree.data.*;
import org.jfree.data.xy.*;


//not used yet
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

//import jams.components.*;
//import org.unijena.jams.model;
/*
 *
 * @author Robert Riedel
 */

public class JAMSSpreadSheet extends JAMSGUIComponent{
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Column Name Array"
            )
            public JAMSStringArray headers;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current time"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array with data of one row" //currently without time
            )
            public JAMSDouble[] rowarray;
    
    
    /* TESTING VARIABLES */
    //private String[] columnNameArray = headers.getValue();
                                        //{"test1","test2"};
    
    private final String title = "JAMSSpreadSheet v0.88";
    
    private JPanel panel = new JPanel();
    private String panelname="spreadsheet";
    
    private int numberOfColumns=0;
    
    //runtime time check
    private boolean timeRuns = false;
    //private Runnable updateTable;
    private boolean ctsIsOpen = false;
    private CTSViewer ctstabs;
    
    //Vectors
    private Vector<Vector> data = new Vector<Vector>();    
    private Vector<String> colnames = new Vector<String>();
    private double[] rowdata;
    
    /* *** Graphical components *** */
    
    GridBagLayout panellayout = new GridBagLayout();
    GridBagConstraints grid = new GridBagConstraints();
    
    private JScrollPane scrollpane = new JScrollPane();
    
    /* Buttons */
    private JButton calcbutton = new JButton("Calculate");
    private JButton openbutton = new JButton("Import");
    private JButton savebutton = new JButton("Export");
    private JButton plotButton = new JButton("Time Plot");
    
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
    JTable table;
    
    /* ComboBox */
    /* String array contains words of the ComboBox */
    private String[] calclist = {   "Sum    ",
                                    "Mean   "};
    
    JComboBox calculations = new JComboBox(calclist);
    private int kindofcalc=0;
    
    /* Plot-Test */
    //private ExtendedTSPlot tsplot;
    
    /* ActionEvents */
    //private ActionListener buttonclicked;

   
    /* Testvalues
     */
 
    /* Constructer */
    public JAMSSpreadSheet() {
    }
    
    /* Methods */
    public JPanel getPanel() {
        //createPanel();
        return panel;
    }
    
    /* JAMS init() method */
    public void init(){  

         /* initializing TableModel */
        tmodel = new JAMSTableModel();
        
        createPanel();
        
        updateGUI();
        
        
        

       
        
        //this.updateGUI();
        /* creating GUI */
        //createPanel();
    }
    
     /************* *** Event Handling *** *********************************/
    /*
    MouseListener columnSelect = new MouseListener(){
        public void mouseClicked(){}
        public void mousePressed(){}
        public void mouseReleased(){}
        public void mouseEntered(){}
        public void mouseExited(){}
        
    };
    */
    
    ActionListener calcbuttonclick = new ActionListener(){
         public void actionPerformed(ActionEvent e) {
         kindofcalc = calculations.getSelectedIndex();
         
            if(kindofcalc == 0){
                calclabel.setText("Sum: "+ calcsum());
            }
            if(kindofcalc == 1){
                calclabel.setText("Mean: " + calcmean());
                //label.setText("MEAN");
            }
         
         /* testing output
        int[] rows = table.getSelectedRows();
        int[] columns = table.getSelectedColumns();
        System.out.println("erster markierter Wert: "+ (Double)table.getValueAt(rows[0], columns[0]));
        */ 
         
         }
         
    };
    /*
        ActionListener editingAction = new ActionListener(){
         public void actionPerformed(ActionEvent ed) {
         
             double value = Double.parseDouble(editField.getText());
             int[] rows = table.getSelectedRows();
             int[] columns = table.getSelectedColumns();
             table.setValueAt(value, rows[0], columns[0]);//erstmal nur das first element
             updateTable();  //ARRAY OUT OF BOUNDS EXCEPTION
        } 
    };
    */
     

    /* Save */
    ActionListener saveAction = new ActionListener(){
         public void actionPerformed(ActionEvent e) {
             
             save();
             
//             //Vector savedata = new Vector();
//             Vector<double[]> tabledata = tmodel.getDataVector();
//             String[] columnNames = tmodel.getCoulumnNameArray();
                         
//             try{
//                 
//                    JFileChooser chooser = new JFileChooser("c:/Dokumente und Einstellungen/p4riro.DAHME/Eigene Dateien/Java/test"); //ACHTUNG!!!!!!!!!
//                    int returnVal = chooser.showSaveDialog(panel);
//                    File file = chooser.getSelectedFile();
////                    String filename = chooser.getSelectedFile().getName();
//                
//               
//                 FileOutputStream out = new FileOutputStream(file);
//                 ObjectOutputStream vout = new ObjectOutputStream(out);
//                 
//                 //vout.writeObject(timeRuns);
//                 
//                 vout.writeObject(tmodel.getTimeVector());
//                 
//                 vout.writeObject(tmodel.getDataVector());
//                 vout.writeObject(tmodel.getCoulumnNameArray());
//                 vout.close();
//                 out.close();
//                
//                
//              //ganzes tablemodel speichern  
////                    //"c:/Dokumente und Einstellungen/p4riro.DAHME/Eigene Dateien/Java/test/data.out"
////              FileOutputStream out = new FileOutputStream(file);  
////              ObjectOutputStream vout = new ObjectOutputStream(out);
////              vout.writeObject(tmodel.getTableModel());
////              vout.close();
////              out.close();
//             }
//             catch(Exception oute){
//                 System.err.println(oute.toString());
//             }
  
        }

    };  
    
    public void save(){
        /* Only for Time Series */
        int colcount = tmodel.getColumnCount();
        int rowcount = tmodel.getRowCount();
        String value;
        String[] columnNames = tmodel.getCoulumnNameArray();
        
        try{
            
            JFileChooser chooser = new JFileChooser(); //ACHTUNG!!!!!!!!!
            int returnVal = chooser.showSaveDialog(panel);
            File file = chooser.getSelectedFile();
            //File file = chooser.getSelectedFile();
            FileWriter filewriter = new FileWriter(file);
            
            
            for(int j=0;j<colcount;j++){
                filewriter.write("#"+columnNames[j],0,columnNames[j].length()+1);
                filewriter.write("\t");
            }
            
            filewriter.write("\r\n");
                        
            for(int k = 0; k < rowcount; k++){
                for(int i = 0; i < colcount; i++){
   
                    value = table.getValueAt(k,i).toString();
                    filewriter.write(value,0,value.length());
                    filewriter.write("\t"); 
                }
                filewriter.write("\r\n");         
            }
            filewriter.close();   
            
        }catch (IOException ex){            
        }     
    }
    
    public void open(){
        
        
        String text = "";
        String rowtext = "";
        String itemtext = "";
        String[] headers = new String[256];
        int colNumber = 0;
        double[] rowBuffer = new double[colNumber];
        
        boolean headerSet = false;
        int line=0;
        int k=0;
        this.tmodel = new JAMSTableModel();
        tmodel.setTimeRuns(true);
        this.timeRuns = true;
        JAMSCalendar timeval = new JAMSCalendar();
        
        try{
            
            
            JFileChooser chooser = new JFileChooser(); //ACHTUNG!!!!!!!!!
            int returnVal = chooser.showOpenDialog(panel);
            File file = chooser.getSelectedFile();
            FileReader fReader = new FileReader(file);
            
            StringBuffer stBuff = new StringBuffer();
            char[] c = new char[100];
            int i;
            
            
            while(fReader.ready()){
                i = fReader.read(c,0,c.length);
                stBuff.append(c,0,i);
            }
            fReader.close();
            text = stBuff.toString();
            
            
            
            
        }catch(IOException ex){
            /* FEHLERMELDUNG */
            System.out.println("Lesen fehlgeschlagen!");
        }
        
        
        /* Tokenizers */
        
        StringTokenizer row = new StringTokenizer(text,"\r\n");
        while(row.hasMoreTokens()){
            
            System.out.println("line = "+line);
            
            rowtext = row.nextToken();
            
            System.out.println(rowtext);
            StringTokenizer item = new StringTokenizer(rowtext,"\t");
            
            
            while(item.hasMoreTokens()){
                System.out.println(" --item has more tokens");
                System.out.println("k = "+k);
                itemtext = item.nextToken();
                
                try{
                    if(!headerSet){
                        StringTokenizer headerSearch = new StringTokenizer(itemtext,"#",true);
                        
                            if (headerSearch.nextToken() == "#"){
                                headers[k] = headerSearch.nextToken();
                                System.out.println(headers[k]);
                                /* gibt es hier noch trash? */
                            }else{
                                colNumber = k;
                                headerSet = true;

                                System.out.println("headers found");
                            }
                        
                    }else{
                        if(line == 0){ /* headers[k-1] != null &&  */
                            String[] colnames = new String[colNumber];
                            for(int l=0;l<colNumber;l++){
                                colnames[l] = headers[l];
                            }
                            setColumnNameArray(colnames);
                            
                            System.out.println("+++++++ setColumnNameArray ++++++");
                            
                        }else{
                        
                            if(k == 0){
                                timeval.setValue(itemtext);
                                addTime(timeval);
                            }else{
                                rowBuffer[k] = new Double(itemtext);

                            }
                        }
                    }
                }catch(NullPointerException ne){
                    System.out.println("no header found! Please mark header in line 0 with pre-arranged #-symbol!");
                }
                
                
                
                k++;
            }
            if(rowBuffer.length != 0){
                addRowArray(rowBuffer);
            }
            rowBuffer = new double[colNumber]; //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            
            k=0;
            line++;
        }
        
        updateGUI();
        
        
        

        
    }
    
    
    
    
    /* Open */
    ActionListener openAction = new ActionListener(){
         public void actionPerformed(ActionEvent e) {

             open();
             /*
             try{
               
                JFileChooser chooser = new JFileChooser("c:/Dokumente und Einstellungen/p4riro.DAHME/Eigene Dateien/Java/test");//ACHTUNG!!!!!!!!
                
                
                int returnVal = chooser.showOpenDialog(panel);
                    File file = chooser.getSelectedFile();
                    //File file = chooser.getSelectedFile().getAbsoluteFile();

                     FileInputStream in = new FileInputStream(file);
                     ObjectInputStream oin = new ObjectInputStream(in);


                     
                     //timeRuns = (boolean) oin.readBoolean();
                     Vector<JAMSCalendar> timeVector;
                     
                     timeVector = (Vector) oin.readObject();
                    
                     Vector inputdata = (Vector) oin.readObject();
                     String[] columnNames = (String[]) oin.readObject();

                     //tmodel = (JAMSTableModel) oin.readObject();

                     oin.close();
                     in.close();
                     //calclabel.setText("loading");
                     
                     
                     //tmodel.setNewDataVector(inputdata);
                     
                     tmodel = new JAMSTableModel();
                     
                     if(timeVector != null){
                         tmodel.setTimeRuns(true);
                         timeRuns = true;
                         tmodel.setColumnNames(columnNames);    
                         tmodel.setTimeVector(timeVector);
                     } else {
                         tmodel.setTimeRuns(false);
                         timeRuns = false;
                         tmodel.setColumnNames(columnNames);
                     }
                     tmodel.setNewDataVector(inputdata);

                     //tmodel = new JAMSTableModel(inputdata);
                     //updateTable();

                     updateGUI();
                     //calclabel.setText("loading successful");
                 
             }
             catch(Exception ine){
                 System.err.println((ine.toString()));
             }
             
             //createPanel();
             //updateGUI();
             //updateTable();
             //repaint();
              */
        } 
    };      
    
    private void openCTS(){
        /* achtung: nur wenn time mitläuft!! */
        if(!ctsIsOpen){
        ctstabs = new CTSViewer(getModel().getRuntime().getFrame());
        ctstabs.addGraph(table);
        ctsIsOpen = true;
        }
        else{
            ctstabs.addGraph(table);
        }
        //CTSConfigurator ctsconf = new CTSConfigurator(table, getModel().getRuntime().getFrame());
        
    }
    
    private void timePlot(int[] rows, int[] cols){
        try{
                 //aus ts einfach ein array machen!!
                 /* 
                  *Open Panel and Show Plot (after getting data from spreadsheet)
                  */
                 //int[] rows = table.getSelectedRows();
                 //int[] cols = table.getSelectedColumns();

                 TimeSeriesCollection dataset = new TimeSeriesCollection();
                 TimeSeries[] ts = new TimeSeries[cols.length];
                 //TimeSeries ts = new TimeSeries("timeseriesname", Second.class);
                 Vector<JAMSCalendar> timevector = tmodel.getTimeVector();
                 double value;

                 String yAxisName = "";

                     for(int k=0; k<cols.length; k++){

                         ts[k] = new TimeSeries("timeseriesname", Second.class);
                         yAxisName += "  "+table.getColumnName(cols[k]);
                         
                         for(int i=0; i < rows.length; i++){
                             if(cols[k] != 0){
                             value = (Double) table.getValueAt(rows[i], cols[k]);
                             if(value < -9999) {
                                 value = 0;
                             }
                             } else {
                                 value = 0;
                             }
                             ts[k].add(new Second(new Date(timevector.get(i).getTimeInMillis())), value);
                         }

                     dataset.addSeries(ts[k]);
                     }
                 
                 
                 JFreeChart testchart = ChartFactory.createTimeSeriesChart(getInstanceName(),"time",yAxisName,dataset,false,false,false);

                 ChartPanel chartPanel = new ChartPanel(testchart);
                 JDialog frame = new JDialog();
                 frame.setLayout(new FlowLayout());
                 //chartPanel.show();
                 frame.add(chartPanel);
                 frame.pack();
                 frame.setVisible(true);
               } 
               catch(Exception pe){
                     System.err.println(pe.toString() + " plot failure");
               }
    }
    
    private void createEmptyTimePlot(int graphCount){
                 TimeSeriesCollection dataset = new TimeSeriesCollection();
                 TimeSeries[] ts = new TimeSeries[graphCount];
                 
                 Vector<JAMSCalendar> timevector = new Vector<JAMSCalendar>();
                 String yAxisName = "";
                 for(int k=0; k<graphCount; k++){
                     
                     

                         ts[k] = new TimeSeries("timeseriesname", Second.class);
                         //yAxisName += "  "+table.getColumnName(cols[k]); TODO: column übergeben!
                 }
                 
                 try{
                     JFreeChart testchart = ChartFactory.createTimeSeriesChart(getInstanceName(),"time",yAxisName,dataset,false,false,false);

                     ChartPanel chartPanel = new ChartPanel(testchart);
                     JDialog frame = new JDialog();
                     frame.setLayout(new FlowLayout());
                     //chartPanel.show();
                     frame.add(chartPanel);
                     frame.pack();
                     frame.setVisible(true);
                 }
                 catch(Exception pe){
                     System.err.println(pe.toString() + " plot failure");
                 }
    }
    
    ActionListener plotAction = new ActionListener(){
         public void actionPerformed(ActionEvent e) {
           
             /*
             if(timeRuns == true){   
                   //int[] rows = table.getSelectedRows();
                 //int[] cols = table.getSelectedColumns();
               timePlot(table.getSelectedRows(),table.getSelectedColumns());
             }
             else{ //noch als übergeordnetes fenster machen!
                 final JDialog frame = new JDialog();
                 frame.setLayout(new FlowLayout());
                 frame.add(new JLabel("Please load a time series dataset"));
                 JButton ok = new JButton("OK");
                 frame.add(ok);
                 frame.pack();
                 frame.setVisible(true);
                 ok.addActionListener( new ActionListener(){
                     public void actionPerformed(ActionEvent e){
                        frame.setVisible(false);
                    }
                });
                 
             }
             */ 
             openCTS();
         } 
    };  
    
    
 
  


/*************** Math *******************************/   
    private double calcsum(){
        
       double sum=0;
       
        int[] rows = table.getSelectedRows();
        int ix=rows.length;
        int[] columns = table.getSelectedColumns();
        int kx=columns.length;
        
        
        //sum= (String) table.getValueAt(rows[0],columns[0]);
        
        for(int k=0; k<kx; k++ ){
            
            for(int i=0; i<ix; i++){
                //int val=(Integer) table.getValueAt(rows[i], columns[k]);
                //table.getValueAt(rows[i], columns[k])!="-" && 
                if(table.getValueAt(rows[i], columns[k]).getClass() != java.lang.String.class){
                    sum += (Double)table.getValueAt(rows[i], columns[k]);
                } else {
                    sum += 0;
                    
                }
            }
        }
       return sum;
        
        //label2.setText("first element: "+(Double)table.getValueAt(rows[0], columns[0]));
    }
    
    private double calcmean(){
        double sum=0;
       
        int[] rows = table.getSelectedRows();
        int ix=rows.length;
        int[] columns = table.getSelectedColumns();
        int kx=columns.length;
        
        
        //sum= (String) table.getValueAt(rows[0], columns[0]);
        
        for(int k=0; k<kx; k++ ){
            
            for(int i=0; i < ix; i++){
                //int val=(Integer) table.getValueAt(rows[i], columns[k]);
                if(table.getValueAt(rows[i], columns[k]).getClass() != java.lang.String.class){
                    sum +=(Double)table.getValueAt(rows[i], columns[k]);
                }
                else{
                    sum += 0;
                }
            }
        }
        
        
        double mean=0;
        
        if(ix == 1 ){
            mean=(double) sum / (double)(kx);
        }
        
        if(kx == 1 ){
           mean=(double) sum / (double)(ix);
        }
        if(kx!=1 && ix !=1){
           mean=(double) sum / (double)(kx*ix);          
        }
        
        return sum;
    }
    
    public String getPanelName() {
        String name = this.panelname;
        return name;
    }
    
    public void setNumberOfColumns(int numberOfColumns){
        this.numberOfColumns = numberOfColumns;
    }
    
    public void setColumnNameArray(String[] names){
        tmodel.setColumnNames(names);
    }
    
    public void addRowArray(double[] data){
        //
        tmodel.addRowArray(data);
    }
    /*
    public void addValue(double value, int columnIndex){
       // if (columnIndex < numberOfColumns){                       //hier noch else-Verhalten!!
           //data.get(columnIndex).addElement("test"+value);
        tmodel.addValue(value, columnIndex);   
        //}
    }
    */
    
    public void updateValue(double value, int rowIndex, int columnIndex){
        
        //tmodel.setValueAt(value, rowIndex, columnIndex);
        //tmodel.getValueAt(value, rowIndex, columnIndex);
    }
    
    public void updateGUI(){
        
        
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
    
    public void makeTable(){
                    
                    
                    table = new JTable(this.tmodel);
                    this.table=table;
        
                    
                    //this.scrollpane = new JScrollPane(table);
                      //better than new instance
                    //scrollpane.repaint();
                    //scrollpane.setSize(800, 600);

                    //panel.add(table, grid);
                    
                    
                    //add(scrollpane, grid);
                    //scrollpane.updateUI();
                    //scrollpane.repaint();
                    
                    table.setRowSelectionAllowed(true);
                    table.setColumnSelectionAllowed(true);
                    //table.setSelectionMode(SINGLE SELECTION);
                    table.setCellSelectionEnabled(true);
                    
                    //return scrollpane;
                    
                   
    }
    
    
//////    public void createPanel() {
          public void createPanel(){
//////
//////        /* PANEL */
              
//////        //this.panel = new JPanel();
//////
//////        panel.setLayout(panellayout);
              panel.setLayout(new BorderLayout(10,10));
              JPanel controlpanel = new JPanel();
              controlpanel.setLayout(new GridLayout(8,1,24,24));
              JPanel headerpanel = new JPanel();
              headerpanel.setLayout(new GridLayout(1,2));
//////        tmodel = new JAMSTableModel();
              //tmodel = new JAMSTableModel();
//////        //hier platz für time schaffen!!!!!!!!!!!!
//////        //
        if(time != null){
            tmodel.setTimeRuns(true);
            String[] colheads = new String[headers.getValue().length + 1];
            colheads[0] = "Time [yyyy-mm-dd hh:ss]";
            //schleife durch arraycopy ersetzen!!
            for(int i = 1; i<=headers.getValue().length;i++){
                colheads[i]=headers.getValue()[i-1];  
            }
            setColumnNameArray(colheads);
        }else{
            setColumnNameArray(headers.getValue());
        }
        //setColumnNameArray(headers.getValue());
        makeTable();
        //panel.add(scrollpane,grid);

//////        /*

//////
//////
//////        /* JLabels */
//////        /* Header */
              controlpanel.add(openbutton);
              controlpanel.add(savebutton);
              controlpanel.add(onthefly);
              controlpanel.add(plotButton);
              
              //openbutton.setEnabled(false);
              openbutton.addActionListener(openAction);
              savebutton.addActionListener(saveAction);
              plotButton.addActionListener(plotAction);
              
              headerpanel.add(titleLabel);
              headerpanel.add(headerlabel);
        
              panel.add(headerpanel, BorderLayout.NORTH);
              
              panel.add(scrollpane, BorderLayout.CENTER);
              panel.add(controlpanel,BorderLayout.EAST);
              
              
          }
              
//////        grid = makegrid(0,0,1,1);
//////        grid.weightx = 5;
//////        grid.weighty = 32;
//////        grid.fill = GridBagConstraints.BOTH;
//////        panel.add(headerlabel, grid);
              
              
//////
//////        /* label2 
//////        grid = makegrid(0,4,1,1);
//////        grid.weightx = 5;
//////        grid.weighty = 32;
//////        grid.fill = GridBagConstraints.BOTH;
//////        panel.add(onthefly, grid); 
//////
//////        /* opensavealert */
//////        grid = makegrid(0,5,1,1);
//////        grid.weightx = 5;
//////        grid.weighty = 32;
//////        grid.fill = GridBagConstraints.BOTH;
//////        panel.add(opensavealert, grid); 
//////
//////        /* calclabel */ //ONTHEFLY-ERSATZ
//////        grid = makegrid(3,8,1,1);
//////        grid.weightx = 5;
//////        grid.weighty = 32;
//////        grid.fill = GridBagConstraints.BOTH;
//////        panel.add(calclabel, grid);
//////
//////        //ONTHEFLY
//////        grid = makegrid(3,9,1,1);
//////        grid.weightx = 5;
//////        grid.weighty = 32;
//////        grid.fill = GridBagConstraints.BOTH;
//////        panel.add(onthefly, grid);
//////
//////        /* plotalert */
//////        grid = makegrid(0,9,1,1);
//////        grid.weightx = 5;
//////        grid.weighty = 32;
//////        grid.fill = GridBagConstraints.BOTH;
//////        panel.add(plotalert, grid);
//////
//////        /* Choice of Calculations */
//////        //JComboBox calculations = new JComboBox(calclist);
//////        calculations.setEditable(false);
//////        grid = makegrid(3,6,1,1);
//////        grid.weightx = 5;
//////        grid.weighty = 32;
//////        grid.fill = GridBagConstraints.NONE;
//////        panel.add(calculations, grid);
//////
//////        /* Buttons */
//////
//////        /* calc button */
//////        grid = makegrid(3,7,1,1);
//////        grid.weightx = 5;
//////        grid.weighty = 32;
//////        grid.fill = GridBagConstraints.NONE;
//////        panel.add(calcbutton, grid);
//////        //calcbutton.addActionListener(calcbuttonclick);
//////        calcbutton.addActionListener(   new ActionListener(){
//////
//////            public void actionPerformed(ActionEvent e) {
//////            kindofcalc = calculations.getSelectedIndex();
//////
//////                if(kindofcalc == 0){
//////                    calclabel.setText("Sum: "+ calcsum());
//////                }
//////                if(kindofcalc == 1){
//////                    calclabel.setText("Mean: " + calcmean());
//////                    //label.setText("MEAN");
//////                }
//////
//////        // testing output
//////        int[] rows = table.getSelectedRows();
//////        int[] columns = table.getSelectedColumns();
//////        System.out.println("erster markierter Wert: "+ (Double)table.getValueAt(rows[0], columns[0]));
//////
//////
//////                }     
//////            }
//////
//////        );
//////
//////        /* open button */
//////        grid = makegrid(3,3,1,1);
//////        grid.weightx = 5;
//////        grid.weighty = 32;
//////        grid.fill = GridBagConstraints.NONE;
//////        panel.add(openbutton, grid);
//////        openbutton.addActionListener(openAction);
//////
//////        /* save button */
//////        grid = makegrid(3,4,1,1);
//////        grid.weightx = 5;
//////        grid.weighty = 32;
//////        grid.fill = GridBagConstraints.NONE;
//////        panel.add(savebutton, grid);
//////        savebutton.addActionListener(saveAction);
//////
//////        /* plot button */
//////
//////        grid = makegrid(0,1,1,1);
//////        grid.weightx = 5;
//////        grid.weighty = 32;
//////        grid.fill = GridBagConstraints.NONE;
//////        panel.add(plotButton, grid);
//////        plotButton.addActionListener(plotAction);
//////
//////    }
 /* ************** JTable Operations *************** */
    
    /*
     *Creation of a GridBagConstraints-Object
     */
    private GridBagConstraints makegrid(int xpos, int ypos, int width, int height){
     GridBagConstraints grid = new GridBagConstraints();
     grid.gridx = xpos;
     grid.gridy = ypos;
     grid.gridwidth = width;
     grid.gridheight = height;
     grid.insets = new Insets(0, 0, 0, 0);
     return grid;
   }
    
    public void addCurrentTime(){
        tmodel.addTime(time);   
    }
    
    public void addTime(JAMSCalendar time){
        tmodel.addTime(time);
    }

           
    public void run() { 
        
        /*for time steps?*/
        
        
        if(time == null){
            
            rowdata = new double[rowarray.length]; /* performance */
            for(int i = 0; i < rowarray.length; i++){
                rowdata[i] = rowarray[i].getValue();
            }
            timeRuns = false;
        }
        else{
                timeRuns = true;
            //TODO: normal im rowdata abspeichern und alles im table model verwalten
                rowdata = new double[rowarray.length];
                
                
                //rowdata[0] = time.getTimeInMillis() / (3600.0*24.0);
                for(int i = 0; i < rowarray.length; i++){
                rowdata[i] = rowarray[i].getValue();
                }
                addCurrentTime();
        }
        
        
        addRowArray(rowdata);
        
        /*
        for(int j = 0; j<rowarray.length; j++){
            
        
        System.out.print("    "+rowdata[j] );
        
        }
        System.out.println( "\n ");
        if(timeRuns == false){
            System.out.println( "time false ");
        }
         */
      
        //SwingUtilities.invokeLater(updateTable);
        if(onthefly.isSelected()==true){
            updateGUI();
        }
        
        /* Plot-Test 
        
        int graphCount = 2; //configure in xml!!
        
        for(int k=0;k<graphCount;k++){
            tsplot.addData(time,rowarray[k],k);
        }
        /*
         *The run method handles the behavior of the component while JAMS is operating.
         *
         */
        //this.getContext().getNumberOfIterations();
    }
    
    public void cleanup() {  
        updateGUI();
       // panel.removeAll();
        //TODO: cleanup tmodel
    }
    

       
    
    
    /** Creates a new instance of JAMSSpreadSheet */

    
}