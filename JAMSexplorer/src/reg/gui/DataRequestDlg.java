/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;
import reg.spreadsheet.JAMSSpreadSheet;

/**
 *
 * @author Christian Fischer
 */
public abstract class DataRequestDlg extends JDialog{
    public static final int OBSERVATED_TIMESERIE    = 1;
    public static final int SIMULATATED_TIMESERIE   = 2;
    public static final int ENSEMBLE_EFFICIENCY     = 3;
    public static final int ENSEMBLE_PARAMETER      = 4;
    public static final int ENSEMBLE_SIMULATION_VARIABLE      = 5;
    
    /*
    public class DataHandler extends TransferHandler {  
        DialogRow request;
               
        public DataHandler(DialogRow t) {
            this.request = t;
            this.request.info.setTransferHandler(this);            
        }

        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            
            return true;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return null;
        }
        
        @Override
        public boolean importData(JComponent comp, Transferable t) {
            try {
                String obj = (String)t.getTransferData(JAMSSpreadSheet.FLAVOR);
                
                StringTokenizer tok = new StringTokenizer(obj,"\n");                
                int n = tok.countTokens()-1;
                double [][] tableData = new double[n][];
                StringTokenizer headerTok = new StringTokenizer(tok.nextToken(),"\t");                
                int m = headerTok.countTokens();
                String []header = new String[m];
                for (int i=0;i<m;i++){
                    header[i] = headerTok.nextToken();
                }
                
                for (int i=0;i<n;i++){
                    String row = tok.nextToken();
                    StringTokenizer rowTok = new StringTokenizer(row,"\t");                    
                    if (m!=rowTok.countTokens())
                        return false;
                                           
                    tableData[i] = new double[m];
                    for (int j=0;j<m;j++){
                        try{
                            tableData[i][j] = Double.valueOf(rowTok.nextToken());
                        }catch(Exception e){
                            tableData[i][j] = 0;
                        }
                    }
                }
                if (m != 1 && n == 1){
                    double [][] tableData2 = new double[m][];
                    for (int i=0;i<m;i++){
                        tableData2[i] = new double[n];
                        for (int j=0;j<n;j++)
                            tableData2[i][j] = tableData[j][i];
                    }
                    tableData = tableData2;
                    int tmp = n;
                    n = m;
                    m = tmp;
                }                
                if (this.request.request.type == OBSERVATED_TIMESERIE){                    
                    if (m != 1 || tableData[0].length == 0){
                        request.info.setText(JAMS.i18n("ONLY_ONE_COLUMN_ALLOWED_FOR_OBSERVATION_DATA"));
                        return false;
                    }
                                        
                    MCAT5Toolbar_.ObservationDataSet obs = new MCAT5Toolbar_.ObservationDataSet();
                    obs.name = JAMS.i18n("OBSDATA:_") + header[0];
                    obs.set = new double[n];
                    for (int i=0;i<n;i++){
                        obs.set[i] = tableData[i][0];
                    }
                    obs.timeLength = m;
                    String result = request.target.addObservationDataSet(obs);
                    if (result != null)
                        request.info.setText(result);
                    else{
                        request.info.setText(obs.toString());
                        request.setAccepted(true);
                    }
                }else if (this.request.request.type == ENSEMBLE_EFFICIENCY){
                    if (m != 1 || tableData[0].length == 0){
                        request.info.setText(JAMS.i18n("ONLY_ONE_COLUMN_ALLOWED_FOR_EFFICIENCY_DATA"));
                        return false;
                    }
                    double effSet[] = new double[n];
                    for (int i=0;i<n;i++){
                        effSet[i] = tableData[i][0];
                    }
                    MCAT5Toolbar_.EfficiencyDataSet eff = new MCAT5Toolbar_.EfficiencyDataSet(JAMS.i18n("EFFDATA:") + header[0],effSet,null,null,true);
                    String result = request.target.addEfficiencyDataSet(eff);
                    if (result != null)
                        request.info.setText(result);
                    else{
                        request.info.setText(eff.toString());
                        request.setAccepted(true);
                    }
                }else if (this.request.request.type == SIMULATATED_TIMESERIE){
                    if (m == 0 || tableData[0].length == 0){
                        request.info.setText(JAMS.i18n("AT_LEAST_ONE_COLUMN_AND_ROW_IS_NEEDED"));
                        return false;
                    }
                    MCAT5Toolbar_.SimulationTimeSeriesDataSet ensemble = new MCAT5Toolbar_.SimulationTimeSeriesDataSet();
                    ensemble.name = JAMS.i18n("SIMDATA");
                    ensemble.timeLength = n;
                    ensemble.set = new MCAT5Toolbar_.SimulationDataSet[n];
                    
                    for (int i=0;i<n;i++){
                        ensemble.set[i] = new MCAT5Toolbar_.SimulationDataSet();
                        ensemble.set[i].name = JAMS.i18n("DATA") + i;
                        ensemble.set[i].set = new double[m];
                        for (int j=0;j<m;j++){              
                            ensemble.set[i].set[j] = tableData[i][j];
                        }
                    }
                                        
                    
                    String result = request.target.addSimulationTimeSeriesDataSet(ensemble);
                    if (result != null)
                        request.info.setText(result);
                    else{
                        request.info.setText(ensemble.toString());
                        request.setAccepted(true);
                    }
                }else if (this.request.request.type == ENSEMBLE_PARAMETER){
                    if (m != 1 || tableData[0].length == 0){
                        request.info.setText(JAMS.i18n("EXACTLY_ONE_COLUMN_AND_AT_LEAST_A_ROW_IS_NEEDED"));
                        return false;
                    }
                    MCAT5Toolbar_.ParameterSet parameterSet = new MCAT5Toolbar_.ParameterSet();
                    parameterSet.name = JAMS.i18n("PARAMSET:") + header[0];
                    parameterSet.set = new double[n];
                    
                    for (int i=0;i<n;i++){
                        parameterSet.set[i] = tableData[i][0];                        
                    }
                                        
                    
                    String result = request.target.addParameterSet(parameterSet);
                    if (result != null)
                        request.info.setText(result);
                    else{
                        request.info.setText(parameterSet.toString());
                        request.setAccepted(true);
                    }
                }else if (this.request.request.type == ENSEMBLE_SIMULATION_VARIABLE){
                    if (m != 1 || tableData[0].length == 0){
                        request.info.setText(JAMS.i18n("EXACTLY_ONE_COLUMN_AND_AT_LEAST_A_ROW_IS_NEEDED"));
                        return false;
                    }
                    MCAT5Toolbar_.SimulationDataSet simDataSet = new MCAT5Toolbar_.SimulationDataSet();
                    simDataSet.name = JAMS.i18n("SIMDATASET:") + header[0];
                    simDataSet.set = new double[n];
                    
                    for (int i=0;i<n;i++){
                        simDataSet.set[i] = tableData[i][0];                        
                    }
                                                            
                    String result = request.target.addSimulationDataSet(simDataSet);                    
                    if (result != null)
                        request.info.setText(result);
                    else{
                        request.info.setText(simDataSet.toString());
                        request.setAccepted(true);                        
                    }
                }
                return true;
            } catch (Exception e) {
                System.err.println(e);
                e.printStackTrace();
            }

            return super.importData(comp, t);
        }
        
    }
    
    public static class DataRequest{
        int         type;
        String      description;
        boolean     multipleAllowed;
        
        public DataRequest(int type, String desc, boolean multiple){
            this.type = type;
            this.description = desc;
            this.multipleAllowed = multiple;
        }
    }
    
    static final ImageIcon icon_ok = new ImageIcon(DataRequestDlg.class.getResource("/reg/resources/images/ok_button.png"));
    static final ImageIcon icon_required = new ImageIcon(DataRequestDlg.class.getResource("/reg/resources/images/not_ok_button.png"));
    static final ImageIcon icon_add = new ImageIcon(DataRequestDlg.class.getResource("/reg/resources/images/add_button.png"));
    static final ImageIcon icon_del = new ImageIcon(DataRequestDlg.class.getResource("/reg/resources/images/remove.png"));
    
    class DialogRow{
        JButton    sign = new JButton("");
        JTextArea  desc  = new JTextArea(JAMS.i18n("DESCRIPTION"));
        JTextArea  info  = new JTextArea("              ");
        JButton     del  = new JButton("");
        
        DialogRow parent = null;
        DialogRow child  = null;
        
        DataRequest request = null;
        MonteCarloData target = null;
        
        JPanel row = new JPanel();
        JPanel subrow = new JPanel();
        
        boolean isAccepted = false;
                        
        DialogRow(DialogRow parent){
            init(parent.request,parent.target,true);
            this.parent = parent;
            DialogRow ancestors = this.parent;
            while (ancestors != null){
                ancestors.row.setMaximumSize(new Dimension(400,ancestors.row.getMaximumSize().height+35));
                ancestors.row.invalidate();
                ancestors = ancestors.parent;
            }
        }
        
        DialogRow(DataRequest request,MonteCarloData mc){
            init(request,mc,false);
        }
        
        void init(DataRequest request,MonteCarloData mc,boolean add){            
            this.request = request;            
            target = mc;

            desc.setFont(new Font("SansSerif", Font.PLAIN, 11));
            info.setFont(new Font("SansSerif", Font.PLAIN, 11));
            
            subrow.setLayout(new GridBagLayout());
            if (add)
                subrow.setBackground(new Color(255,255,128));
            else
                subrow.setBackground(new Color(255,192,192));
            
            GridBagConstraints c = new GridBagConstraints();

            if (add)
                sign.setIcon(icon_add);
            else
                sign.setIcon(icon_required);
            
            desc.setPreferredSize(new Dimension(150,20));
            desc.setMinimumSize(new Dimension(150,20));
            info.setPreferredSize(new Dimension(130,20));
            info.setMinimumSize(new Dimension(130,20));
            sign.setBorderPainted(false);
            sign.setPreferredSize(new Dimension(55,20));
            sign.setMinimumSize(new Dimension(55,20));
            del.setMinimumSize(new Dimension(55,20));
            del.setPreferredSize(new Dimension(55,20));
            
            sign.setMargin(new Insets(0,0,0,0));
            sign.setContentAreaFilled(false);
            sign.setFocusPainted(false);
            del.setContentAreaFilled(false);
            del.setIcon(icon_del);           
            del.setMargin(new Insets(0,0,0,0));
                             
            del.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    if (parent == null){
                        isAccepted = false;
                    }else{
                        
                    }
                }
            });
                
            info.setLineWrap(true);
            info.setEnabled(false);
            info.setOpaque(false);
                        
            desc.setEnabled(false);
            desc.setOpaque(false);
            
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1.0;
            subrow.add(desc,c);            
                                    
            c.gridx = 1;
            subrow.add(info,c);            
            
            c.gridx = 2;    
            c.anchor = GridBagConstraints.CENTER;
            subrow.add(sign,c);  
            
            c.gridx = 3;        
            c.anchor = GridBagConstraints.CENTER;
            subrow.add(del,c);  
                        
            subrow.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
            subrow.setPreferredSize(new Dimension(350,30));
            
            row.setLayout(new BorderLayout());
            row.add(subrow,BorderLayout.NORTH);           
            row.setMaximumSize(new Dimension(400,35));
            row.add(Box.createRigidArea(new Dimension(5,5)),BorderLayout.CENTER);
            row.setMinimumSize(new Dimension(10,35));
            new DataHandler(this);      
            
            if (this.request.type == OBSERVATED_TIMESERIE)
                desc.setText(JAMS.i18n("OBSERVED_TIMESERIE"));
            if (this.request.type == SIMULATATED_TIMESERIE)
                desc.setText(JAMS.i18n("SIMULATED_TIMESERIE"));
            if (this.request.type == ENSEMBLE_EFFICIENCY)
                desc.setText(JAMS.i18n("EFFICIENCY_DATA_SERIE"));
            if (this.request.type == ENSEMBLE_PARAMETER)
                desc.setText(JAMS.i18n("PARAMETER_SETS"));
            if (this.request.type == ENSEMBLE_SIMULATION_VARIABLE)
                desc.setText(JAMS.i18n("ENSEMBLE_SIMULATED_VARIABLE"));
        }
        
        void setAccepted(boolean state){
            if (state){
                sign.setIcon(icon_ok);
                subrow.setBackground(new Color(192,255,192));
            
                if (this.request.multipleAllowed){
                    child = (new DialogRow(this));
                    row.add(child.getComponent(),BorderLayout.SOUTH);                                
                }
            }else{                
                sign.setIcon(icon_required);
                subrow.setBackground(new Color(255,192,192));
            
                if (child != null){
                    if (child.child != null){
                        this.desc.setText(child.desc.getText());
                        this.info.setText(child.info.getText());
                        
                    }
                }    
            }
            isAccepted = state;                        
        }
        
        boolean isAccepted(){
            return isAccepted;
        }
        
        DataRequest getRequest(){
            return request;
        }
        
        MonteCarloData getTarget(){
            return target;
        }
        
        JPanel getComponent(){
            return row;
        }
    };
    
    ArrayList<DialogRow> dataRows = new ArrayList<DialogRow>();
    
    protected MonteCarloData mcData = new MonteCarloData();
    
    DataRequestDlg(DataRequest[]req, Window parent){
        super(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        this.setTitle(JAMS.i18n("DATA_REQUEST"));
        
        JPanel dialogPanel = new JPanel(new BorderLayout());
        
        JTextArea descarea = new JTextArea(JAMS.i18n("THIS_DIALOG_LISTS_ALL_DATA_REQUIRED_TO_PERFOM_THE_REQUESTED_OPERATION._LOAD_THE_DATA_YOU_WANT_TO_USE_INTO") +
                JAMS.i18n("_THE_SPREADSHEET_OF_JAMSEXPLORER_AND_DRAG_AND_DROP_IT_TO_THIS_DIALOG!"));
        descarea.setEnabled(false);
        descarea.setBackground(dialogPanel.getBackground());
        descarea.setForeground(new Color(0,0,0));
        descarea.setFont(new Font("SansSerif", Font.PLAIN, 11));
        descarea.setLineWrap(true);
        descarea.setWrapStyleWord(true);
        descarea.setMinimumSize(new Dimension(500, 100));
        
               
        dialogPanel.add(descarea,BorderLayout.NORTH);
        dialogPanel.add(Box.createRigidArea(new Dimension(50,20)),BorderLayout.WEST);
        dialogPanel.add(Box.createRigidArea(new Dimension(50,20)),BorderLayout.EAST);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));
        mainPanel.setMinimumSize(new Dimension(500, Math.min(req.length*50+150,500)));
        mainPanel.setBorder(BorderFactory.createEtchedBorder());
        
        //top row
        JPanel row = new JPanel();
        JPanel subrow = new JPanel();
        subrow.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
                                                
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0,0,0,0);
        c.ipadx = 0;
        c.weightx = 1.0;
        subrow.add(new JButton(JAMS.i18n("DESCRIPTION")) {

            {
                setMinimumSize(new Dimension(150, 20));
                setPreferredSize(new Dimension(150, 20));
                setEnabled(false);
                setBorderPainted(false);
            }
        }, c);
        
        c.gridx = 1;
        c.weightx = 1.0;

        subrow.add(new JButton(JAMS.i18n("DRAG&DROP_PLACE")) {
            {
                setMinimumSize(new Dimension(130, 20));
                setPreferredSize(new Dimension(130, 20));
                setEnabled(false);
                setBorderPainted(false);
            }
        }, c);
        
        c.weightx = 1.0;
        c.gridx = 2;
        
        subrow.add(new JButton(JAMS.i18n("OK?")) {
            {
                setMinimumSize(new Dimension(55, 20));
                setPreferredSize(new Dimension(55, 20));
                setEnabled(false);
                setBorderPainted(false);
            }
        }, c);

        c.gridx = 3;

        subrow.add(new JButton(JAMS.i18n("DEL")) {
            {
                setMinimumSize(new Dimension(55, 20));
                setPreferredSize(new Dimension(55, 20));
                setEnabled(false);
                setBorderPainted(false);                
            }
        }, c);
                               
        subrow.setPreferredSize(new Dimension(350, 22));

        row.setLayout(new BorderLayout());
        row.add(subrow, BorderLayout.NORTH);
        row.setMaximumSize(new Dimension(400, 22));
        row.add(Box.createRigidArea(new Dimension(5, 5)), BorderLayout.CENTER);
        mainPanel.add(row);
        for (int i=0;i<req.length;i++){
            dataRows.add(new DialogRow(req[i],mcData));
            mainPanel.add(dataRows.get(dataRows.size()-1).getComponent());
        }
        JScrollPane scroller = new JScrollPane(mainPanel);        
        scroller.setMinimumSize(new Dimension(500, Math.min(req.length*50+250,500)));        
        dialogPanel.add(scroller,BorderLayout.CENTER);
        JPanel downUnderPanel = new JPanel();
        downUnderPanel.setLayout(new BorderLayout());
        downUnderPanel.setMinimumSize(new Dimension(100,100));
        downUnderPanel.add(Box.createRigidArea(new Dimension(20,20)),BorderLayout.NORTH);
        downUnderPanel.add(new JButton(JAMS.i18n("OK")){{
            this.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){   
                    for (int i=0;i<dataRows.size();i++)
                        if (!dataRows.get(i).isAccepted())
                            return;
                    
                    dataCollectAction();
                }
            });
        }        
        },BorderLayout.CENTER);
        downUnderPanel.add(Box.createRigidArea(new Dimension(20,20)),BorderLayout.SOUTH);
        dialogPanel.add(downUnderPanel,BorderLayout.SOUTH);
        this.add(dialogPanel);
        int height = Math.min(req.length*50+250,500);
        this.setMinimumSize(new Dimension(500, height));        
        this.setResizable(false);
        this.invalidate();
        this.setVisible(true);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        if (d.width>500 && d.height > height)
            this.setLocation(new Point( (d.width-500) / 2, (d.height-height) / 2));
        
        this.setAlwaysOnTop(true);
    }
    
    abstract public void dataCollectAction();*/
}
