package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import optas.data.DataCollection;
import optas.data.Parameter;
import optas.data.RankingTable;
import optas.data.SimpleEnsemble;
import optas.optimizer.management.SampleFactory;
import optas.optimizer.management.SampleFactory.Sample;
import optas.optimizer.management.Statistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.chart.axis.CategoryLabelPositions;

/**
 *
 * @author Nathan Lighthart
 */
public class ParetoBoxPlot extends MCAT5Plot {
	SimpleEnsemble[] parameterEnsembles;

	public ParetoBoxPlot() {
//		init();
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		DataCollection dc = DataCollection.createFromFile(new File("D:\\2013_08_21_sa_small2.cdat"));

		try {
			DataRequestPanel d = new DataRequestPanel(new ParetoBoxPlot(), dc);
			JFrame plotWindow = new JFrame("test");
			plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			plotWindow.setLayout(new BorderLayout());
			plotWindow.setSize(800, 700);
			plotWindow.add(d, BorderLayout.CENTER);
			plotWindow.pack();
			plotWindow.setVisible(true);
		} catch(NoDataException nde) {
		}
	}
	protected ChartPanel chartPanel = new ChartPanel(null, true);

	@Override
	public void refresh() throws NoDataException {
		SampleFactory factory = new SampleFactory();
		this.getDataSource().constructSample(factory);
		Statistics stats = factory.getStatistics();
		ArrayList<SampleFactory.Sample> paretoFront = stats.getParetoFront();
		RankingTable rt = new RankingTable(paretoFront);
		rt.setAlpha(0.1);
		rt.computeRankings();

		ArrayList<Sample> candidates = new ArrayList<Sample>();
		candidates.addAll(Arrays.asList(rt.getCandidates()));

		DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

		Set<String> parameterSet = this.getDataSource().getDatasets(Parameter.class);
		parameterEnsembles = new SimpleEnsemble[parameterSet.size()];
		int counter = 0;
		for(String name : parameterSet) {
			parameterEnsembles[counter++] = this.getDataSource().getSimpleEnsemble(name);
		}

		List<List<Double>> parameterValues = new ArrayList<List<Double>>(parameterEnsembles.length);
		for(int i = 0; i < parameterEnsembles.length; i++) {
			parameterValues.add(new ArrayList<Double>());
		}
		for(Sample sample : candidates) {
			for(int i = 0; i < sample.x.length; i++) {
				parameterValues.get(i).add(sample.x[i]);
			}
		}

		parameterValues = normalize(parameterValues);

		int parameterNumber = 0;
		for(List<Double> parameter : parameterValues) {
			dataset.add(parameter, "Single Series", parameterEnsembles[parameterNumber].name);
			parameterNumber++;
		}

		createChart(dataset);
	}

	@Override
	public JPanel getPanel() {
		return chartPanel;
	}

	private void createChart(
			final BoxAndWhiskerCategoryDataset dataset) {
		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
				JAMS.i18n("BOX_PLOT"), JAMS.i18n("PARAMETER_SETS"), JAMS.i18n("NORMALISED_RANGE"), dataset, false);
		BoxAndWhiskerRenderer renderer = (BoxAndWhiskerRenderer) chart.getCategoryPlot().getRenderer();
		renderer.setFillBox(false);
		renderer.setSeriesPaint(0, Color.BLACK);
//		renderer.setMeanVisible(false);
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinePaint(Color.BLACK);
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));

		// Add tooltip so the actual min and max can be known
		String name;
		SimpleEnsemble ensemble;
		for(int parameterNumber = 0; parameterNumber < parameterEnsembles.length; parameterNumber++) {
			ensemble = parameterEnsembles[parameterNumber];
			name = ensemble.name;
			domainAxis.addCategoryLabelToolTip(name, name + " (Min: " + ensemble.getMin() + " Max: " + ensemble.getMax() + ")");
		}
		chartPanel.setChart(chart);
	}

	private List<List<Double>> normalize(List<List<Double>> parameterValues) {
		int parameterNumber = 0;
		for(List<Double> list : parameterValues) {
			double max = parameterEnsembles[parameterNumber].getMax();
			double min = parameterEnsembles[parameterNumber].getMin();
			double difference = max - min;
			for(int i = 0; i < list.size(); i++) {
				list.set(i, (list.get(i) - min) / difference);
			}
			parameterNumber++;
		}
		return parameterValues;
	}
}
