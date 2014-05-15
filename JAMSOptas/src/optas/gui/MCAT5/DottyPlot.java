/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JPanel;
import optas.data.DataSet;
import optas.data.SimpleEnsemble;
import optas.data.StateVariable;
import optas.tools.PatchedChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Christian Fischer
 */
public class DottyPlot extends MCAT5Plot {

	protected XYPlot plot = new XYPlot();
	protected PatchedChartPanel chartPanel = null;

	private void init() {
		//setup renderer
		XYDotRenderer renderer = new XYDotRenderer();
		renderer.setSeriesPaint(0, Color.BLUE);
		renderer.setDotHeight(3);
		renderer.setDotWidth(3);
		//setup plot
		plot.setRenderer(renderer);
		//setup chart
		JFreeChart chart = new JFreeChart(plot);
		chart.setTitle(JAMS.i18n("DOTTY_PLOT"));
		chartPanel = new PatchedChartPanel(chart, true);
		chartPanel.setMinimumDrawWidth(0);
		chartPanel.setMinimumDrawHeight(0);
		chartPanel.setMaximumDrawWidth(MAXIMUM_WIDTH);
		chartPanel.setMaximumDrawHeight(MAXIMUM_HEIGHT);
		chart.getPlot().setBackgroundPaint(Color.white);
		chart.getXYPlot().setDomainGridlinePaint(Color.black);
		redraw();
	}

	public DottyPlot() {
		this.addRequest(new SimpleRequest(JAMS.i18n("Attribute") + " 1", StateVariable.class));
		this.addRequest(new SimpleRequest(JAMS.i18n("Attribute") + " 2", StateVariable.class));

		init();
	}

	@Override
	public JPanel getPanel() {
		return this.chartPanel;
	}

	public void refresh() throws NoDataException {
		if(!this.isRequestFulfilled()) {
			return;
		}

		ArrayList<DataSet> p[] = getData(new int[]{0, 1});
		SimpleEnsemble p1 = (SimpleEnsemble) p[0].get(0);
		SimpleEnsemble p2 = (SimpleEnsemble) p[1].get(0);

		plot.setDomainAxis(new NumberAxis(p1.getName()));
		plot.setRangeAxis(new NumberAxis(p2.getName()));

		XYSeries dataset = new XYSeries(JAMS.i18n("DATA_POINT"));

		int n = p1.getSize();

		for(int i = 0; i < n; i++) {
			dataset.add(p1.getValue(p1.getId(i)), p2.getValue(p2.getId(i)));
		}
		plot.setDataset(0, new XYSeriesCollection(dataset));

		if(plot.getRangeAxis() != null) {
			plot.getRangeAxis().setRange(p2.getMin(), p2.getMax());
		}
		if(plot.getDomainAxis() != null) {
			plot.getDomainAxis().setRange(p1.getMin(), p1.getMax());
		}

	}
}
