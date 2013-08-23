/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import optas.data.DataCollection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import optas.gui.MCAT5.MCAT5Plot.SimpleRequest;
import optas.data.DataSet;
import optas.data.Efficiency;
import optas.data.Measurement;
import optas.data.RankingTable;
import optas.data.SimpleEnsemble;
import optas.data.TimeSerie;
import optas.data.TimeSerieEnsemble;
import optas.optimizer.management.SampleFactory;
import optas.optimizer.management.SampleFactory.Sample;
import optas.optimizer.management.Statistics;
import optas.tools.PatchedSpiderWebPlot;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class MultiObjectiveDecisionSupport extends MCAT5Plot {
    final int MAX_OBJCOUNT = 10;
    
    XYPlot hydroChart = new XYPlot();
    PatchedSpiderWebPlot spiderPlot = new PatchedSpiderWebPlot();
            
    ChartPanel chartPanel1 = null;
    ChartPanel chartPanel2 = null;
    
    JSlider objSliders[] = new JSlider[10];
    JPanel mainPanel = null;
    
    TimeSerieEnsemble ts = null;
    
    public MultiObjectiveDecisionSupport() {
        this.addRequest(new SimpleRequest(JAMS.i18n("SIMULATED_TIMESERIE"), TimeSerie.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"), Efficiency.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("OBSERVED_TIMESERIE"), Measurement.class, 0, 1));

        init();
    }

    
    private void init() {
        JFreeChart chart1 = ChartFactory.createTimeSeriesChart(
                JAMS.i18n("Visual comparison of hydrographs"),
                "time",
                "discharge",
                null,
                true,
                true,
                false);

        hydroChart = chart1.getXYPlot();
        
        XYLineAndShapeRenderer hydroRenderer1 = new XYLineAndShapeRenderer();
                        
        hydroRenderer1.setBaseLinesVisible(true);
        hydroRenderer1.setBaseShapesVisible(false);
        hydroRenderer1.setSeriesOutlinePaint(0,Color.BLUE);
        hydroRenderer1.setSeriesPaint(0,Color.BLUE);        
        hydroRenderer1.setStroke(new BasicStroke(2));
          
        XYLineAndShapeRenderer hydroRenderer2 = new XYLineAndShapeRenderer();
                        
        hydroRenderer2.setBaseLinesVisible(true);
        hydroRenderer2.setBaseShapesVisible(false);
        hydroRenderer2.setSeriesOutlinePaint(0,Color.RED);
        hydroRenderer2.setSeriesPaint(0,Color.RED);        
        hydroRenderer2.setStroke(new BasicStroke(2));
        
        hydroChart.setRenderer(0,hydroRenderer1);
        hydroChart.setRenderer(1,hydroRenderer2);
        
        
        hydroChart.getDomainAxis().setLabel(JAMS.i18n("TIME"));
        DateAxis axis = (DateAxis) hydroChart.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));

        hydroChart.setRangeAxis(new NumberAxis(JAMS.i18n("OUTPUT")));
              
        chartPanel1 = new ChartPanel(chart1, true);
        chartPanel1.setMinimumDrawWidth( 0 );
        chartPanel1.setMinimumDrawHeight( 0 );
        chartPanel1.setMaximumDrawWidth( MAXIMUM_WIDTH );
        chartPanel1.setMaximumDrawHeight( MAXIMUM_HEIGHT );
        chart1.setTitle(JAMS.i18n("OUTPUT_UNCERTAINTY_PLOT"));
        
        
        JFreeChart chart2 = new JFreeChart(spiderPlot);        
        chart2.setTitle("Possible solutions");
        chart2.removeLegend();

        chartPanel2 = new ChartPanel(chart2, true);
        chartPanel2.setMinimumDrawWidth( 0 );
        chartPanel2.setMinimumDrawHeight( 0 );
        chartPanel2.setMaximumDrawWidth( MAXIMUM_WIDTH );
        chartPanel2.setMaximumDrawHeight( MAXIMUM_HEIGHT );
        
        for (int i=0;i<MAX_OBJCOUNT;i++){
            objSliders[i] = new JSlider();
        }
        mainPanel = new JPanel();
        
        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
                        
        GroupLayout.ParallelGroup group1 = layout.createParallelGroup();
        group1.addComponent(chartPanel2,100,200,500);
        for (int i=0;i<MAX_OBJCOUNT;i++){
            group1.addComponent(objSliders[i],100,200,500);
        }
        
        GroupLayout.SequentialGroup group2 = layout.createSequentialGroup();
        group2.addComponent(chartPanel2,200,300,500);
        for (int i=0;i<MAX_OBJCOUNT;i++){
            group2.addComponent(objSliders[i],30,50,80);
        }
        
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(chartPanel1,500,500,5000)
                .addGroup(group1)
                );
        
        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(chartPanel1,500,500,5000)
                .addGroup(group2)
                );
        
        redraw();

        if (hydroChart.getRangeAxis() != null) {
            hydroChart.getRangeAxis().setAutoRange(true);
        }
        if (hydroChart.getDomainAxis() != null) {
            hydroChart.getDomainAxis().setAutoRange(true);
        }
    }

    private void updateSimulation(int index){
        if (ts == null) {
            return;
        }
        
        TimeSeries dataset2 = new TimeSeries(JAMS.i18n("Simulation"));
        int T = ts.getTimesteps();
        double timeseries[] = ts.getValue(ts.getId(index));
        
        for (int i = 0; i < T; i++) {
            Day d = new Day(ts.getDate((int) i));
            dataset2.add(d, timeseries[i]);
        }
        
        TimeSeriesCollection sim_runoff = new TimeSeriesCollection();
        sim_runoff.addSeries(dataset2);
        hydroChart.setDataset(1, sim_runoff);
    }
    
    		Random rand = new Random(System.currentTimeMillis());
    @Override
    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled()) {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        ArrayList<DataSet> p[] = getData(new int[]{0, 1, 2});

        ts = (TimeSerieEnsemble) p[0].get(0);
                        
        Measurement obs = null;
        if (p[2].size()>0){
            obs = (Measurement) p[2].get(0);
        }
        
        int T = ts.getTimesteps();
                                        
        if (obs!=null){        
            TimeSeries dataset1 = new TimeSeries(JAMS.i18n("Measurement"));
            for (int i=0;i<T;i++){
                Day d = new Day(obs.getTime((int) i));
                dataset1.add(d, obs.getValue(i));
            }
            TimeSeriesCollection obs_runoff = new TimeSeriesCollection();
            obs_runoff.addSeries(dataset1);
            hydroChart.setDataset(0, obs_runoff);
        }else{
            hydroChart.setDataset(0, null);
        }
        
        updateSimulation(0);
		updateSpiderPlot(7);
    }
	
	private void updateSpiderPlot(int n) {
		Set<String> xSet = this.getDataSource().getDatasets(Efficiency.class);
        SimpleEnsemble y[] = new SimpleEnsemble[xSet.size()];
        int counter = 0;
        for (String name : xSet) {
            y[counter++] = this.getDataSource().getSimpleEnsemble(name);
        }
        
        int m = y.length;
        DecimalFormat format = new DecimalFormat("#.###"); //TODO
        for (int i=0;i<MAX_OBJCOUNT;i++){
            if (i < m) {
                objSliders[i].setVisible(true);
                objSliders[i].setMinimum(0);
                objSliders[i].setMaximum(100);                
                Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
                for (int j=0;j<101;j+=25){
                    double value = ((double)j / 100.);
                    labelTable.put(new Integer(j), new JLabel(format.format(value)));
                }
                objSliders[i].setLabelTable(labelTable);
                objSliders[i].setPaintLabels(true);
                objSliders[i].setBorder(BorderFactory.createTitledBorder(y[i].getName()));
            }
            else {
                objSliders[i].setVisible(false);
                objSliders[i].setPaintLabels(false);
            }            
        }
		
		SampleFactory factory = new SampleFactory();
		this.getDataSource().constructSample(factory);
		Statistics stats = factory.getStatistics();
		ArrayList<Sample> paretoFront = stats.getParetoFront();
		RankingTable rt = new RankingTable(paretoFront);
//		System.out.println(rt);
		rt.computeRankings();
//		System.out.println(rt);
		DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
		
		double[] mins = new double[y.length];
		double[] maxs = new double[y.length];
		int sampleNumber = 1;
		for(Sample sample : paretoFront) {
			for(int i = 0; i < sample.F().length; i++) {
				double eff = sample.F()[i];
				mins[i] = Math.min(mins[i], eff);
				maxs[i] = Math.max(maxs[i], eff);
				categoryDataset.addValue(eff, "Sample " + sampleNumber, y[i].name);
			}
			sampleNumber++;
		}

//		categoryDataset.addValue(rand.nextDouble() * 100, "Your own profile", "Fostering a common vision");
//		categoryDataset.addValue(rand.nextDouble() * 100, "Your own profile", "Trusting in \nteam members' \ndecisions and \nsuggestions");
//		categoryDataset.addValue(rand.nextDouble() * 100, "Your own profile", "Motivation");
//		categoryDataset.addValue(rand.nextDouble() * 100, "Your own profile", "Openness to innovations");
//		categoryDataset.addValue(rand.nextDouble() * 100, "Your own profile", "Implementing useful strategies");
//
//		categoryDataset.addValue(100, "Fully developed level (100%)", "Fostering a common vision");
//		categoryDataset.addValue(100, "Fully developed level (100%)", "Trusting in \nteam members' \ndecisions and \nsuggestions");
//		categoryDataset.addValue(100, "Fully developed level (100%)", "Motivation");
//		categoryDataset.addValue(100, "Fully developed level (100%)", "Openness to innovations");
//		categoryDataset.addValue(100, "Fully developed level (100%)", "Implementing useful strategies");

		this.spiderPlot.setDataset(categoryDataset);
		spiderPlot.setAxisTickVisible(true);
		spiderPlot.setNumberOfTicks(3);
		for(int i = 0; i < y.length; i++) {
			spiderPlot.setOrigin(i, .9 * mins[i]);
			spiderPlot.setMaxValue(i, 1.1 * maxs[i]);
		}
	}

    @Override
    public JPanel getPanel() {
        return mainPanel;
    }

    public JPanel getPanel1() {
        return mainPanel;
    }

    public JPanel getPanel2() {
        return chartPanel2;
    }
    
    public static void main(String[] args) {
        DataCollection dc = DataCollection.createFromFile(new File("D:\\fullEnsemble.cdat"));

        try {
            DataRequestPanel d = new DataRequestPanel(new MultiObjectiveDecisionSupport(), dc);
            JFrame plotWindow = new JFrame("test");
            plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            plotWindow.setLayout(new BorderLayout());
            plotWindow.setVisible(true);
            plotWindow.setSize(800, 700);
            plotWindow.add(d, BorderLayout.CENTER);
        } catch (NoDataException nde) {
        }
                    
    }
}
