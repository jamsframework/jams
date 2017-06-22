/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import Jama.Matrix;
import jams.JAMS;
import jams.data.Attribute;
import jams.data.Attribute.Calendar;
import jams.gui.WorkerDlg;
import jams.gui.input.CalendarInput;
import jams.gui.input.ValueChangeListener;
import jams.gui.tools.GUIState;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import optas.data.DataCollection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import optas.gui.MCAT5.MCAT5Plot.SimpleRequest;
import optas.data.DataSet;
import optas.data.Measurement;
import optas.data.TimeSerie;
import optas.data.TimeSerieEnsemble;
import jams.math.distributions.CDF_Normal;
import optas.regression.GaussianProcessRegression;
import optas.regression.gaussian.HyperParameter;
import optas.regression.gaussian.cov.CovarianceFunction;
import optas.regression.gaussian.mean.MeanFunction;
import optas.regression.likelihood.LikelihoodFunction;
import optas.tools.PatchedChartPanel;
import optas.tools.Tools;
import org.jfree.chart.plot.IntervalMarker;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class GPOutputUncertainty extends MCAT5Plot {

    double beta = 0.1;
    int L = 0;
    TreeSet<Double> ys = new TreeSet<Double>();
    ArrayList<Double> ys_array = new ArrayList<Double>();
    
    Measurement sim = null;
    
    private class NQTTransformation{
        public NQTTransformation(double[]err,double[]sim){
            L = err.length;
            double y2[] = Arrays.copyOf(err, L);
            ys = new TreeSet<Double>();
            //normalize to avoid flattering at the boundaries
            for (int i=0;i<L;i++){
                y2[i] = err[i] / (sim[i]+Math.exp(beta));
                ys.add(y2[i]);
            }
            ys_array = new ArrayList<Double>();
            ys_array.addAll(ys);
        }
        private double single_transform(double err, TreeSet<Double> ys){
            int L = ys.size();
            Double ceil = ys.ceiling(err);
            Double floor  = ys.floor(err);
                    
            if (ceil == null) {
                ceil = ys.last();
            }
            if (floor == null) {
                floor = ys.first();
            }
                    
            double l = ceil-floor;     
            
            double wp = 1.0 - (ceil - err)/l;
            double wm = 1.0 - wp;
            if (l==0){
                wp = 1.0;
                wm = 0.0;
            }
            
            int c1 = ys.headSet(ceil,true).size();
            int c2 = ys.headSet(floor,true).size();
            
            double t1 = (double)c1/(double)(L+1);
            double t2 = (double)(c2)/(double)(L+1);
            
            double p = wp*t1 + wm*t2;
            
            if (p<=0 || p >= 1){
                System.out.println("Error in NQT");
            }
            double Y = CDF_Normal.xnormi(p);
            return Y;
        }
        
        public double[] transform(double y[], double[] sim){            
            double y2[] = new double[y.length];
            for (int i=0;i<y.length;i++){
                y2[i] = y[i] / (sim[i]+Math.exp(beta));
            }
            double Y[] = new double[y.length];
            for (int i=0;i<L;i++){
                Y[i] = single_transform(y2[i], ys);
            }
            return Y;
        }
        
        public double[] inv_transform(double Y[]){
            double y[] = new double[Y.length];
            
            for (int i=0;i<Y.length;i++){
                double p = CDF_Normal.normp(Y[i]);
                
                double index1 = Math.floor((L+1)*p);
                double index2 = Math.ceil((L+1)*p);
    
                double w1 = 1.0 - ((L+1)*p - index1);
                double w2 = 1-w1;
                if (index1 < 0) {
                    index1=0;
                }
                if (index2>=L)
                    index2=L-1;

                if (index2 < 0)
                    index2=0;
                
                if (index1>=L)
                    index1=L-1;

                y[i]=w1*ys_array.get((int)index1) + w2*ys_array.get((int)index2);   
            }
            return y;
        }
    }
    
    XYPlot hydrograph = new XYPlot();
    
    PatchedChartPanel chartPanel1 = null;

    JPanel informationPanel = new JPanel();
    
    JPanel mainPanel = null;
    
    JComboBox covMethods = new JComboBox(CovarianceFunction.getCovFunctions());
    JComboBox meanMethods = new JComboBox(MeanFunction.getMeanFunctions());
    JComboBox likMethods = new JComboBox(LikelihoodFunction.getLikelihoodFunctions());
    JTextField normalizationParameterField = new JTextField(5);
    
    JTextField covParameter[] = null;
    JTextField meanParameter[] = null;
    JTextField likParameter[] = null;
    JTextField betaParameter  = new JTextField(5);
    
    JLabel processInformation = new JLabel();
    
    CalendarInput trainStartDate = new CalendarInput();
    CalendarInput trainEndDate = new CalendarInput();
    
    JButton optimizeParameters = new JButton("Optimize Hyperparameter");
    JButton calcRegression     = new JButton("Calculate Regression");
    
    JTable tsTable = new JTable(new Object[][]{{Boolean.TRUE,"test"}},new String[]{"x","y"});
    
    GaussianProcessRegression gp = new GaussianProcessRegression();
    double x[][] = null;
                
    class TimeSeriesTableModel extends AbstractTableModel {

        private String[] columnNames = new String[]{"select", "name"};
        private Object[][] data = null;

        TimeSeriesTableModel(TimeSerie ts[]) {
            data = new Object[ts.length][2];
            for (int i=0;i<ts.length;i++){
                data[i][0] = Boolean.FALSE;
                data[i][1] = ts[i];
            }
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            if (data != null) {
                return data.length;
            } else {
                return 0;
            }
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];                
        }

        @Override
        public Class getColumnClass(int c) {
            switch (c) {
                case 0:
                    return Boolean.class;
                case 1:
                    return String.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case 0:{
                    if (sim != null){
                        if (this.getValueAt(row, 1).toString().equals(sim.toString())){
                            return false;
                        }
                    }
                    return true;
                }
                case 1:
                    return false;
            }
            return false;
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        @Override
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            
            fireTableCellUpdated(row, col);
        }
    }
    
    public GPOutputUncertainty() {
        this.addRequest(new SimpleRequest(JAMS.i18n("SIMULATED_TIMESERIE"), Measurement.class, 0, 10));
        this.addRequest(new SimpleRequest(JAMS.i18n("OBSERVED_TIMESERIE"), Measurement.class, 0, 1));

        init();
    }

    void setReadyForOptimization(boolean value){
        optimizeParameters.setEnabled(value);
    }
    
    private void init() {
        JFreeChart chart1 = ChartFactory.createTimeSeriesChart(
                JAMS.i18n("OUTPUT_UNCERTAINTY_PLOT"),
                "time",
                "discharge",
                null,
                true,
                true,
                false);

        hydrograph = chart1.getXYPlot();

        chart1.getPlot().setBackgroundPaint(Color.white);
        chart1.getXYPlot().setDomainGridlinePaint(Color.black);

        XYDifferenceRenderer renderer_uncert = new XYDifferenceRenderer(Color.LIGHT_GRAY, Color.LIGHT_GRAY, false);
        XYLineAndShapeRenderer renderer_obs = new XYLineAndShapeRenderer();
        XYLineAndShapeRenderer renderer_sim = new XYLineAndShapeRenderer();
        XYLineAndShapeRenderer renderer_gp  = new XYLineAndShapeRenderer();
                
        renderer_uncert.setBaseFillPaint(Color.LIGHT_GRAY);
        renderer_uncert.setPaint(Color.BLACK);

        renderer_obs.setBaseLinesVisible(true);
        renderer_obs.setBaseShapesVisible(false);
        renderer_obs.setOutlinePaint(Color.BLUE);
        renderer_obs.setPaint(Color.BLUE);
        renderer_obs.setStroke(new BasicStroke(2));

        renderer_sim.setBaseLinesVisible(true);
        renderer_sim.setBaseShapesVisible(false);
        renderer_sim.setOutlinePaint(Color.RED);
        renderer_sim.setPaint(Color.RED);
        renderer_sim.setStroke(new BasicStroke(0.25f));

        renderer_gp.setBaseLinesVisible(true);
        renderer_gp.setBaseShapesVisible(false);
        renderer_gp.setOutlinePaint(Color.ORANGE);
        renderer_gp.setPaint(Color.ORANGE);
        renderer_gp.setStroke(new BasicStroke(1));

        hydrograph.setRenderer(3, renderer_uncert);
        hydrograph.setRenderer(0, renderer_obs);

        hydrograph.setRenderer(1, renderer_sim);
        hydrograph.setRenderer(2, renderer_gp);

        hydrograph.getDomainAxis().setLabel(JAMS.i18n("TIME"));
        DateAxis axis = (DateAxis) hydrograph.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));

        hydrograph.setRangeAxis(new NumberAxis(JAMS.i18n("OUTPUT")));

        chartPanel1 = new PatchedChartPanel(chart1, true);

        chartPanel1.setMinimumDrawWidth( 0 );
        chartPanel1.setMinimumDrawHeight( 0 );
        chartPanel1.setMaximumDrawWidth( MAXIMUM_WIDTH );
        chartPanel1.setMaximumDrawHeight( MAXIMUM_HEIGHT );
        
        mainPanel = new JPanel(new BorderLayout());
        
        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        
        JPanel configurationPanel = new JPanel();        
        configurationPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "GP - Configuration"));
        //informationPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "GP - Information"));
                        
        GroupLayout confPanelLayout = new GroupLayout(configurationPanel);
        configurationPanel.setLayout(confPanelLayout);
        JLabel covMethodLabel = new JLabel("covariance method:");
        JLabel meanMethodLabel = new JLabel("mean method:");
        JLabel likelihoodMethodLabel = new JLabel("likelihood method:");
        JLabel betaParameterLabel = new JLabel("beta");
        
        trainStartDate.setBorder(BorderFactory.createTitledBorder("Training Start"));
        trainEndDate.setBorder(BorderFactory.createTitledBorder("Training End"));
        
        confPanelLayout.setHorizontalGroup(confPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addGroup(confPanelLayout.createSequentialGroup()
                    .addComponent(covMethodLabel)
                    .addGap(5)
                    .addComponent(covMethods,100,150,200)
                )
                .addGroup(confPanelLayout.createSequentialGroup()
                    .addComponent(meanMethodLabel)
                    .addGap(5)
                    .addComponent(meanMethods,100,150,200)
                )
                .addGroup(confPanelLayout.createSequentialGroup()
                    .addComponent(likelihoodMethodLabel)
                    .addGap(5)
                    .addComponent(likMethods,100,150,200)
                )
                .addGroup(confPanelLayout.createSequentialGroup()
                    .addComponent(betaParameterLabel)
                    .addGap(5)
                    .addComponent(betaParameter,100,150,200)
                )
                .addGroup(confPanelLayout.createSequentialGroup()
                    //.addComponent(trainStartLabel)
                    //.addGap(5)
                    .addComponent(trainStartDate,200,275,350)
                )
                .addGroup(confPanelLayout.createSequentialGroup()
                    //.addComponent(trainEndLabel)
                    //.addGap(5)
                    .addComponent(trainEndDate,200,275,350)
                )
        );
        
        confPanelLayout.setVerticalGroup(confPanelLayout.createSequentialGroup()
                .addGroup(confPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(covMethodLabel)
                    .addComponent(covMethods,25,30,35)
                )
                .addGap(5, 10, 15)
                .addGroup(confPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(meanMethodLabel)
                    .addComponent(meanMethods,25,30,35)
                )
                .addGap(5, 10, 15)
                .addGroup(confPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(likelihoodMethodLabel)
                    .addComponent(likMethods,25,30,35)
                )
                .addGap(5,10,15)
                .addGroup(confPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(betaParameterLabel)
                    .addGap(5)
                    .addComponent(betaParameter,25,30,35)
                )
                .addGap(5,10,15)                
                .addGroup(confPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    //.addComponent(trainStartLabel)
                    //.addGap(5)
                    .addComponent(trainStartDate,60,70,80)
                )
                .addGap(5,10,15)                
                .addGroup(confPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    //.addComponent(trainEndLabel)
                    //.addGap(5)
                    .addComponent(trainEndDate,60,70,80)
                )
        );
        
        JScrollPane scrollPane = new JScrollPane(tsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK),"Timeseries"));
        
        JScrollPane infoPanelScrollPane = new JScrollPane(informationPanel);
        infoPanelScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK),"GP - Information"));
        
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(chartPanel1)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(configurationPanel)
                    .addComponent(infoPanelScrollPane,550,550,600)
                    .addComponent(scrollPane,100,150,250)
                )
                );
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(chartPanel1)
                .addGroup(layout.createParallelGroup()
                    .addComponent(configurationPanel,150,200,400)
                    .addComponent(infoPanelScrollPane,150,200,400)
                    .addComponent(scrollPane,150,200,400)
                )
                );

        covMethods.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gp.setCovFunction((CovarianceFunction)covMethods.getSelectedItem());
                updateInfoPanel();
            }
        });
        meanMethods.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                gp.setMeanFunction((MeanFunction)meanMethods.getSelectedItem());
                updateInfoPanel();
            }
        });
        likMethods.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                gp.setLikelihoodFunction(((LikelihoodFunction)likMethods.getSelectedItem()));
                updateInfoPanel();
            }
        });
        
        updateInfoPanel();
                
        setReadyForOptimization(false);
        
        calcRegression.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                redraw();
            }
        });
        
        optimizeParameters.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                WorkerDlg progress = new WorkerDlg(GUIState.getMainWindow(), "Updating plot");
                progress.setInderminate(true);
                progress.setTask(new Runnable() {
                    public void run() {
                        try{
                        GPOutputUncertainty.this.gp.optimizeHyperParameters();
                        HyperParameter hyp = gp.getHyperParameter();
                        for (int i=0;i<hyp.mean.length;i++){
                            GPOutputUncertainty.this.meanParameter[i].setText(Double.toString(hyp.mean[i]));
                        }
                        for (int i=0;i<hyp.cov.length;i++){
                            GPOutputUncertainty.this.covParameter[i].setText(Double.toString(hyp.cov[i]));
                        }
                        for (int i=0;i<hyp.lik.length;i++){
                            GPOutputUncertainty.this.likParameter[i].setText(Double.toString(hyp.lik[i]));
                        }
                        redraw();
                        }catch(Exception e){
                            System.out.println(e.toString());
                            e.printStackTrace();
                        }
                    }
                });
                progress.execute();
            }
        });
        
        ValueChangeListener trainPeriodChangeListener = new ValueChangeListener() {

            @Override
            public void valueChanged() {
                hydrograph.clearDomainMarkers();
                
                Calendar startDate = trainStartDate.getCalendarValue();
                Calendar endDate   = trainEndDate.getCalendarValue();
                if (startDate == null || endDate == null) {
                    return;
                }                
                                
                IntervalMarker marker = new IntervalMarker(startDate.getTimeInMillis(), endDate.getTimeInMillis());
                marker.setPaint(Color.GREEN);
                marker.setAlpha(0.2f);
                hydrograph.addDomainMarker(marker);
            }
        };
        
        this.trainStartDate.addValueChangeListener(trainPeriodChangeListener);
        this.trainEndDate.addValueChangeListener(trainPeriodChangeListener);
        

        redraw();

        if (hydrograph.getRangeAxis() != null) {
            hydrograph.getRangeAxis().setAutoRange(true);
        }
        if (hydrograph.getDomainAxis() != null) {
            hydrograph.getDomainAxis().setAutoRange(true);
        }
    }

    
    private void updateInfoPanel(){
        informationPanel.removeAll();
        
        JPanel covParameterPanel = new JPanel();
        covParameterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "cov. Parameter"));
        
        JPanel meanParameterPanel = new JPanel();
        meanParameterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "mean. Parameter"));
        
        JPanel likParameterPanel = new JPanel();
        likParameterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "lik. Parameter"));
        
        String oldText = "";
        if (processInformation!=null){
            oldText = processInformation.getText();
        }
        processInformation = new JLabel(oldText);
        processInformation.setBorder(BorderFactory.createTitledBorder("Information"));
        
        GroupLayout infoPanelLayout = new GroupLayout(informationPanel);
        informationPanel.setLayout(infoPanelLayout);
        infoPanelLayout.setHorizontalGroup(infoPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addGroup(infoPanelLayout.createSequentialGroup()
                    .addGap(5, 10, 15)
                    .addComponent(optimizeParameters)
                    .addComponent(calcRegression)
                )
                .addGroup(infoPanelLayout.createSequentialGroup()
                    .addGroup(infoPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(covParameterPanel)
                        .addComponent(meanParameterPanel)
                        .addComponent(likParameterPanel)
                    )
                    .addComponent(processInformation)
                )
                );
        
        infoPanelLayout.setVerticalGroup(infoPanelLayout.createSequentialGroup()
                .addGroup(infoPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(optimizeParameters)
                    .addComponent(calcRegression)
                )
                .addGroup(infoPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addGroup(infoPanelLayout.createSequentialGroup()
                        .addComponent(covParameterPanel)
                        .addComponent(meanParameterPanel)
                        .addComponent(likParameterPanel)
                    )
                    .addComponent(processInformation)
                )
                );
        
        HyperParameter hyp = gp.getHyperParameter();
        if (hyp == null){
            return;
        }
        covParameter = new JTextField[hyp.cov.length];
        
        GroupLayout covParameterPanelLayout = new GroupLayout(covParameterPanel);
        covParameterPanel.setLayout(covParameterPanelLayout);
        
        ParallelGroup horzGroup = covParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup vertGroup = covParameterPanelLayout.createSequentialGroup();
        
        for (int i=0;i<hyp.cov.length;i++){
            covParameter[i] = new JTextField(15);
            covParameter[i].setText(Double.toString(hyp.cov[i]));
            JLabel covParameterLabel = new JLabel("cov_p_" + i);
            vertGroup.addGroup(
                    covParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(covParameterLabel)
                    .addComponent(covParameter[i],15,20,25));
            
            horzGroup.addGroup(
                    covParameterPanelLayout.createSequentialGroup()
                    .addComponent(covParameterLabel,100,100,100)
                    .addGap(5)
                    .addComponent(covParameter[i],100,150,200));
        }
        
        covParameterPanelLayout.setHorizontalGroup(horzGroup);
        covParameterPanelLayout.setVerticalGroup(vertGroup);
        
        //setup mean parameter fields
        meanParameter = new JTextField[hyp.mean.length];
                
        GroupLayout meanParameterPanelLayout = new GroupLayout(meanParameterPanel);
        meanParameterPanel.setLayout(meanParameterPanelLayout);
        
        horzGroup = meanParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        vertGroup = meanParameterPanelLayout.createSequentialGroup();
        
        for (int i=0;i<hyp.mean.length;i++){
            meanParameter[i] = new JTextField(15);
            meanParameter[i].setText(Double.toString(hyp.mean[i]));
            JLabel meanParameterLabel = new JLabel("mean_p_" + i);
            vertGroup.addGroup(
                    meanParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(meanParameterLabel)
                    .addComponent(meanParameter[i],15,20,25));
            
            horzGroup.addGroup(
                    meanParameterPanelLayout.createSequentialGroup()
                    .addComponent(meanParameterLabel,100,100,100)
                    .addGap(5)
                    .addComponent(meanParameter[i],100,150,200));
        }
        
        meanParameterPanelLayout.setHorizontalGroup(horzGroup);
        meanParameterPanelLayout.setVerticalGroup(vertGroup);
        
        //setup lik parameter fields
        likParameter = new JTextField[hyp.lik.length];
                
        GroupLayout likParameterPanelLayout = new GroupLayout(likParameterPanel);
        likParameterPanel.setLayout(likParameterPanelLayout);
        
        horzGroup = likParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        vertGroup = likParameterPanelLayout.createSequentialGroup();
        
        for (int i=0;i<hyp.lik.length;i++){
            likParameter[i] = new JTextField(15);
            likParameter[i].setText(Double.toString(hyp.lik[i]));
            JLabel likParameterLabel = new JLabel("lik_p_" + i);
            vertGroup.addGroup(
                    likParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(likParameterLabel)
                    .addComponent(likParameter[i],15,20,25));
            
            horzGroup.addGroup(
                    likParameterPanelLayout.createSequentialGroup()
                    .addComponent(likParameterLabel,100,100,100)
                    .addGap(5)
                    .addComponent(likParameter[i],100,150,200));
        }
        
        likParameterPanelLayout.setHorizontalGroup(horzGroup);
        likParameterPanelLayout.setVerticalGroup(vertGroup);
    }
            
    //int index = 50;    
    int index = 2123;    
    boolean initData = false;
                    
    private double calcE2(double sim[], double obs[]){
        double mean_obs = 0;
        for (int i=0;i<obs.length;i++){
            mean_obs += obs[i];
        }
        mean_obs /= obs.length;
        
        double num=0, denum = 0;
        for (int i=0;i<obs.length;i++){
            num += (sim[i]-obs[i])*(sim[i]-obs[i]);
            denum += (obs[i]-mean_obs)*(obs[i]-mean_obs);
        }
        return 1.0 - num/denum;
    }
    
    private String provideBasicInformation(double nlZ, TimeSerie ts_obs, TimeSerie ts_sim, TimeSerie ts_lb, TimeSerie ts_ub, TimeSerie ts_exp, int trainSamples, int testSamples){
        String htmlFile = "<html><body>%1</body></html>";
        
        String innerText = "";
        
        double obs[] = new double[ts_obs.getTimesteps()];
        double sim[] = new double[ts_sim.getTimesteps()];
        double exp[] = new double[ts_sim.getTimesteps()];
        
        for (int i=0;i<ts_obs.getTimesteps();i++){
            obs[i] = ts_obs.getValue(i);
            sim[i] = ts_sim.getValue(i);
            exp[i] = ts_exp.getValue(i);
        }
        
        double e2 = calcE2(obs,sim);
        double e2_gp = calcE2(obs,exp);
        double mean_uncert = 0, mean_uncert_pos = 0, mean_uncert_neg = 0;
        double hit_ratio = 0.0;
        double mean_obs = 0;
        
        for (int i=0;i<obs.length;i++){
            mean_uncert += (ts_ub.getValue(i) - ts_lb.getValue(i));
            mean_obs += obs[i];
            
            mean_uncert_pos += ts_ub.getValue(i) - sim[i];
            mean_uncert_neg += -(ts_lb.getValue(i) - sim[i]);
            
            if (obs[i] >= ts_lb.getValue(i) && obs[i] <= ts_ub.getValue(i))
                hit_ratio += 1.0;
        }
        mean_uncert /= obs.length;
        mean_uncert_pos /= obs.length;
        mean_uncert_neg /= obs.length;
        hit_ratio /= obs.length;
        mean_obs /= obs.length;
        
        DecimalFormat df = new DecimalFormat("###.##");
        
        innerText += "training samples: " + df.format(trainSamples) + "<br>";
        innerText += "test samples: " + df.format((testSamples - trainSamples)) + "<br>";
        innerText += "marginal liklihood: " + df.format(nlZ) + "<br>";
        innerText += "mean uncertainty: " + df.format(mean_uncert) + " ( " + df.format(100. * mean_uncert/mean_obs) + "% )" + "<br>";
        innerText += "mean uncertainty (+): " + df.format(mean_uncert_pos) + " ( " + df.format(100. * mean_uncert_pos/mean_obs) + "% )" + "<br>";
        innerText += "mean uncertainty (-): " + df.format(mean_uncert_neg) + " ( " + df.format(100. * mean_uncert_neg/mean_obs) + "% )" + "<br>";
        innerText += "hit ratio: " + df.format(hit_ratio) + "<br>";
        innerText += "simulation quality (NSE): " + df.format(e2) + "<br>";
        innerText += "regression quality (NSE): " + df.format(e2_gp) + "<br>";
        
        
        htmlFile = htmlFile.replaceAll("%1", innerText);
        
        return htmlFile;
        
    }
    
    @Override
    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled()) {
            return;
        }
        //collect data
        //1. all timeseries ensembles
        Set<String> tsSet = this.getDataSource().getDatasets(Measurement.class);
        Measurement ts[] = new Measurement[tsSet.size()];
        int counter = 0;
        for (String name : tsSet) {
            ts[counter++] = (Measurement)this.getDataSource().getDataSet(name);
        }
                        
        if (!initData) {
            tsTable.setModel(new TimeSeriesTableModel(ts));
            tsTable.getModel().addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                }
            });
            initData = true;
        }
        //2. measurment
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        ArrayList<DataSet> p[] = getData(new int[]{0, 1});

        Measurement obs = (Measurement) p[1].get(0);
        sim = (Measurement) p[0].get(0);
        int T = obs.getTimesteps();
        
        //3. simulation                        
        ArrayList<Integer> rows = new ArrayList<Integer>();
        
        for (int i=0;i<this.tsTable.getModel().getRowCount();i++){
            if ((Boolean)tsTable.getModel().getValueAt(i, 0)){
                rows.add(i);
            }
        }
        int n = rows.size();
        
        //select simulation in table .. must have!
        for (int i=0;i<tsTable.getModel().getRowCount();i++){
           if (tsTable.getModel().getValueAt(i, 1).toString().compareTo(sim.toString())==0){
               if (tsTable.getModel().getValueAt(i, 0) == Boolean.FALSE){
                   tsTable.getModel().setValueAt(Boolean.TRUE, i, 0);
                   return;
               }
           }
        }
        
        //4. get training period
        Attribute.Calendar startDate = this.trainStartDate.getCalendarValue();
        Attribute.Calendar endDate = this.trainEndDate.getCalendarValue();
        
        //set default if necessary
        if (startDate == null){
            trainStartDate.setDate(sim.getTime(0));
            trainStartDate.setTime(sim.getTime(0));
            startDate = this.trainStartDate.getCalendarValue();
        }
        if (endDate == null){
            trainEndDate.setDate(sim.getTime(sim.getTimesteps()/2));
            trainEndDate.setTime(sim.getTime(sim.getTimesteps()/2));
            endDate = this.trainEndDate.getCalendarValue();
        }
        hydrograph.clearDomainMarkers();
        IntervalMarker marker = new IntervalMarker(startDate.getTimeInMillis(), endDate.getTimeInMillis());
        marker.setPaint(Color.GREEN);
        marker.setAlpha(0.2f);
        hydrograph.addDomainMarker(marker);
        
        int trainStart = 0;
        int trainEnd   = 0;
                
        while (trainStart < T && startDate.getTimeInMillis() > obs.getTime(trainStart++).getTime() ){}
        while (trainEnd   < T && endDate.getTimeInMillis() > obs.getTime(trainEnd++).getTime() ){}
        
        int D = trainEnd - trainStart;
        
        
        //5. collect x data
        x = new double[D][n];
       
        for (int i=0;i<n;i++){
            int row = rows.get(i);
            TimeSerie input_ts = (TimeSerie)tsTable.getModel().getValueAt(row, 1);
            for (int j=trainStart;j<trainEnd;j++){
                x[j-trainStart][i] = input_ts.getValue(j);                
            }
        }
        
        //6. collect y data
        double y[] = new double[D];
        double simArray[] = new double[D];
        for (int i=trainStart;i<trainEnd;i++){            
            y[i-trainStart] = -(obs.getValue(i)-sim.getValue(i));            
            simArray[i-trainStart] = sim.getValue(i);
        }
        
        //7. collecting parameters
        beta = Tools.readField(betaParameter, beta);
                
        NQTTransformation nqt = new NQTTransformation(y, simArray);
        
        double Y[] = nqt.transform(y, simArray);
        
        LikelihoodFunction lik = (LikelihoodFunction)likMethods.getSelectedItem();
        MeanFunction mean = (MeanFunction)meanMethods.getSelectedItem();
        CovarianceFunction cov = (CovarianceFunction)covMethods.getSelectedItem();
        
        gp.setCovFunction(cov);
        gp.setLikelihoodFunction(lik);
        gp.setMeanFunction(mean);
        gp.setTrainingDataset(x, Y);
        
        HyperParameter hyp = gp.getHyperParameter();
        
        if (covParameter != null){
            for (int i=0;i<covParameter.length;i++){
                hyp.cov[i] = Tools.readField(covParameter[i],hyp.cov[i]);
            }
        }
        
        if (meanParameter != null){
            for (int i=0;i<meanParameter.length;i++){
                hyp.mean[i] = Tools.readField(meanParameter[i],hyp.mean[i]);
            }
        }
        
        if (likParameter != null){
            for (int i=0;i<likParameter.length;i++){
                hyp.lik[i] = Tools.readField(likParameter[i],hyp.lik[i]);
            }
        }
                
        double xs[][] = new double[T][n];
        for (int i=0;i<rows.size();i++){
            int row = rows.get(i);
            TimeSerie input_ts = (TimeSerie)tsTable.getModel().getValueAt(row, 1);
            for (int j=0;j<T;j++){
                xs[j][i] = input_ts.getValue(j);
            }
        }
                
        Matrix result[] = gp.inference(xs);
                
        int TEST_LIMIT = 2000;
        double xs_test[][] = new double[TEST_LIMIT][1];
        for (int i=0;i<TEST_LIMIT;i++){
            xs_test[i][0] = (double)i;
        }
        Matrix result2[] = gp.inference(xs_test);
        System.out.println("Lower conf\tUpper conf\texp");
        
        double upperBound2[] = new double[TEST_LIMIT];
        double lowerBound2[] = new double[TEST_LIMIT];
        double expected2[]   = new double[TEST_LIMIT];
        for (int i = 0; i < TEST_LIMIT; i++) {
            upperBound2[i] = result2[0].get(0, i)+2.*Math.sqrt(result2[1].get(0, i));
            lowerBound2[i] = result2[0].get(0, i)-2.*Math.sqrt(result2[1].get(0, i));
            expected2[i] = result2[0].get(0, i);
        }
        upperBound2 = nqt.inv_transform(upperBound2);
        lowerBound2 = nqt.inv_transform(lowerBound2);
        expected2 = nqt.inv_transform(expected2);
        
        for (int i=0;i<TEST_LIMIT;i++){
            System.out.println(i+"\t"+
                (Math.max(i-(i+beta)*lowerBound2[i],0)) + "\t" +
                (Math.max(i-(i+beta)*upperBound2[i],0)) + "\t" +
                (Math.max(i-(i+beta)*expected2[i],0)) + "\t");
        }
        
        TimeSeries dataset1 = new TimeSeries(JAMS.i18n("LOWER_CONFIDENCE_BOUND"));
        TimeSeries dataset2 = new TimeSeries(JAMS.i18n("UPPER_CONFIDENCE_BOUND"));
        TimeSeries dataset3 = new TimeSeries(obs.name);
        TimeSeries dataset4 = new TimeSeries(JAMS.i18n("SIM"));
        TimeSeries dataset5 = new TimeSeries(JAMS.i18n("MEDIAN"));
        
        double upperBound[] = new double[T];
        double lowerBound[] = new double[T];
        double expected[]   = new double[T];
        for (int i = 0; i < T; i++) {
            upperBound[i] = result[0].get(0, i)+2.*Math.sqrt(result[1].get(0, i));
            lowerBound[i] = result[0].get(0, i)-2.*Math.sqrt(result[1].get(0, i));
            expected[i] = result[0].get(0, i);
        }
        upperBound = nqt.inv_transform(upperBound);
        lowerBound = nqt.inv_transform(lowerBound);
        expected = nqt.inv_transform(expected);
        TimeSerie ts_lb = null, ts_ub = null, ts_exp = null;
        try{
            double lb[] = new double[T];
            double ub[] = new double[T];
            double exp[] = new double[T];
            
            for (int i=0;i<T;i++){
                lb[i] = Math.max(sim.getValue(i)-(sim.getValue(i)+beta)*lowerBound[i],0);
                ub[i] = Math.max(sim.getValue(i)-(sim.getValue(i)+beta)*upperBound[i],0);
                exp[i] = Math.max(sim.getValue(i)-(sim.getValue(i)+beta)*expected[i],0);
            }
            
            ts_lb = new TimeSerie(ub, obs.getTimeDomain(), "lb", null);
            ts_ub = new TimeSerie(lb, obs.getTimeDomain(), "ub", null);
            ts_exp = new TimeSerie(exp, obs.getTimeDomain(), "exp", null);
        }catch(DataSet.MismatchException me){
            me.printStackTrace();
        }
        String info = provideBasicInformation(gp.getMarginalLikelihood(), obs, sim, ts_lb, ts_ub, ts_exp, x.length, xs.length);
        this.processInformation.setText(info);
        this.processInformation.revalidate();
        
        for (int i = 0; i < T; i++) {
            Day d = new Day(obs.getTime((int) i));
            dataset1.add(d, ts_lb.getValue(i));
            dataset2.add(d, ts_ub.getValue(i));
            
            //System.out.println((result[0].get(0, i)-2.*result[1].get(0, i)) + "\t" + (result[0].get(0, i)+2.*result[1].get(0, i)) );
            
            dataset3.add(d, obs.getValue(i));
            dataset4.add(d, sim.getValue(i));
            dataset5.add(d, ts_exp.getValue(i));
        }
        TimeSeriesCollection obs_runoff = new TimeSeriesCollection();
        obs_runoff.addSeries(dataset3);
        hydrograph.setDataset(0, obs_runoff);

        TimeSeriesCollection interval = new TimeSeriesCollection();
        interval.addSeries(dataset1);
        interval.addSeries(dataset2);
        
        TimeSeriesCollection simCollection = new TimeSeriesCollection();
        simCollection.addSeries(dataset4);
        hydrograph.setDataset(1, simCollection);
        hydrograph.setDataset(3, interval);
        
        TimeSeriesCollection median_ensemble = new TimeSeriesCollection();
        median_ensemble.addSeries(dataset5);
        hydrograph.setDataset(2, median_ensemble);
        updateInfoPanel();
        setReadyForOptimization(true);
    }

    @Override
    public JPanel getPanel() {
        /*JPanel completePanel = new JPanel(new BorderLayout());
        completePanel.add(mainPanel,BorderLayout.NORTH);
        completePanel.add(chartPanel2,BorderLayout.SOUTH);*/
        return mainPanel;
    }

    public JPanel getPanel1() {
        return mainPanel;
    }
    
    public static void main(String[] args) {
        DataCollection dc = DataCollection.createFromFile(new File("E:/Vortraege und Paper/2013-10-17 KOS/Hymod/output/current/nsga_save2.cdat"));

        try {
            DataRequestPanel d = new DataRequestPanel(new GPOutputUncertainty(), dc);
            JFrame plotWindow = new JFrame("Gaussian Processes Output Uncertainty");
            plotWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            plotWindow.setLayout(new BorderLayout());
            plotWindow.setVisible(true);
            plotWindow.setSize(800, 700);
            plotWindow.add(d, BorderLayout.CENTER);
        } catch (NoDataException nde) {
        }
                    
    }
}
