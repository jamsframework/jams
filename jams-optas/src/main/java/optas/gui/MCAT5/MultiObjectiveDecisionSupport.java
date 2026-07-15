/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
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
import java.util.Hashtable;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import optas.data.DataCollection;
import optas.data.DataSet;
import optas.data.Efficiency;
import optas.data.Measurement;
import optas.data.Parameter;
import optas.data.RankingTable;
import optas.data.SimpleEnsemble;
import optas.data.TimeSerie;
import optas.data.TimeSerieEnsemble;
import optas.gui.MCAT5.MCAT5Plot.SimpleRequest;
import optas.optimizer.management.SampleFactory;
import optas.optimizer.management.SampleFactory.Sample;
import optas.optimizer.management.Statistics;
import optas.tools.ColorRenderer;
import optas.tools.PatchedChartPanel;
import optas.tools.PatchedSpiderWebPlot;
import optas.tools.Tools;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class MultiObjectiveDecisionSupport extends MCAT5Plot {

	final int MAX_OBJCOUNT = 100;
	XYPlot hydroChart = new XYPlot();
	PatchedSpiderWebPlot spiderPlot = new PatchedSpiderWebPlot();
	PatchedChartPanel chartPanel1 = null;
	PatchedChartPanel chartPanel2 = null;
	JSlider objSliders[] = new JSlider[MAX_OBJCOUNT];
	JPanel mainPanel = null;
	JTextField alphaField = new JTextField(5);
	JButton exportDataset = new JButton("Export");
	ArrayList<Sample> candidates = new ArrayList<Sample>();
	TimeSerieEnsemble ts = null;
	ArrayList<Sample> paretoFront = null;
	RankingTable rt = null;
	SimpleEnsemble x[], y[] = null;
	int N, m = 0;
	double alpha = 0.1;
	int selectedId = 0;
	boolean datasetValid = false;
	boolean onAdjustment = false;
	private JTable candidateTable;
	private boolean[] visibleCandidates;

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
		hydroRenderer1.setSeriesOutlinePaint(0, Color.BLUE);
		hydroRenderer1.setSeriesPaint(0, Color.BLUE);
		hydroRenderer1.setStroke(new BasicStroke(2));

		XYLineAndShapeRenderer hydroRenderer2 = new XYLineAndShapeRenderer();

		hydroRenderer2.setBaseLinesVisible(true);
		hydroRenderer2.setBaseShapesVisible(false);
		hydroRenderer2.setSeriesOutlinePaint(0, Color.RED);
		hydroRenderer2.setSeriesPaint(0, Color.RED);
		hydroRenderer2.setStroke(new BasicStroke(2));

		hydroChart.setRenderer(0, hydroRenderer1);
		hydroChart.setRenderer(1, hydroRenderer2);

		hydroChart.getDomainAxis().setLabel(JAMS.i18n("TIME"));
		DateAxis axis = (DateAxis) hydroChart.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));
		hydroChart.setDomainGridlinePaint(Color.black);
		hydroChart.setRangeAxis(new NumberAxis(JAMS.i18n("OUTPUT")));

		//TODO make spiderplot nice
		spiderPlot.setBaseSeriesOutlineStroke(new BasicStroke(2.0f));

		chartPanel1 = new PatchedChartPanel(chart1, true);
		chartPanel1.setMinimumDrawWidth(0);
		chartPanel1.setMinimumDrawHeight(0);
		chartPanel1.setMaximumDrawWidth(MAXIMUM_WIDTH);
		chartPanel1.setMaximumDrawHeight(MAXIMUM_HEIGHT);
		chart1.setTitle(JAMS.i18n("OUTPUT_UNCERTAINTY_PLOT"));
		chartPanel1.getChart().getPlot().setBackgroundPaint(Color.white);

		JFreeChart chart2 = new JFreeChart(spiderPlot);
		chart2.setTitle("Possible solutions");
		chart2.removeLegend();

		chartPanel2 = new PatchedChartPanel(chart2, true);
		chartPanel2.setMinimumDrawWidth(0);
		chartPanel2.setMinimumDrawHeight(0);
		chartPanel2.setMaximumDrawWidth(MAXIMUM_WIDTH);
		chartPanel2.setMaximumDrawHeight(MAXIMUM_HEIGHT);

		chart2.setTitle("");
		chartPanel2.addChartMouseListener(new ChartMouseListener() {
			@Override
			public void chartMouseClicked(ChartMouseEvent cme) {
				if(cme.getTrigger().getButton() != 1) {
					return;
				}
				if(cme.getTrigger().getClickCount() != 1) {
					return;
				}

				if(cme.getEntity() instanceof CategoryItemEntity) {
					CategoryItemEntity entity = ((CategoryItemEntity) cme.getEntity());
					Sample selectedCandidate = null;
					for(int i = 0; i < candidates.size(); i++) {
						if(entity.getRowKey().compareTo("Sample " + i) == 0) {
							selectedCandidate = candidates.get(i);
							setStandardColors();
							spiderPlot.setSeriesPaint(i, Color.red);
							break;
						}
					}

					int id = -1;
					for(int j = 0; j < N; j++) {
						boolean equals = true;
						for(int k = 0; k < m; k++) {
							if(selectedCandidate.F()[k] != y[k].getValue(y[k].getId(j))) {
								equals = false;
							}
						}
						if(equals) {
							updateSimulation(j);
							updateParameterSet(j);
							break;
						}
					}
				}
			}

			@Override
			public void chartMouseMoved(ChartMouseEvent cme) {
				return;//do nothing
			}
		});

		alphaField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rt.setAlpha(alpha);
				redraw();
			}
		});

		exportDataset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//TODO: export or show set of parameters ..
			}
		});

		for(int i = 0; i < MAX_OBJCOUNT; i++) {
			objSliders[i] = new JSlider();
		}

		mainPanel = new JPanel();

		GroupLayout layout = new GroupLayout(mainPanel);
		mainPanel.setLayout(layout);
		JLabel alphaLabel = new JLabel("alpha: ");

		candidateTable = new JTable();
		candidateTable.getTableHeader().setReorderingAllowed(false);
		candidateTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane scrollPane = new JScrollPane(candidateTable);

		GroupLayout.ParallelGroup group1 = layout.createParallelGroup();
		group1.addComponent(chartPanel2, 100, 200, 500);
		group1.addComponent(scrollPane, 100, 200, 500);

		GroupLayout.ParallelGroup group3 = layout.createParallelGroup();
		group3.addGroup(layout.createSequentialGroup()
				.addGap(5, 5, 5)
				.addComponent(alphaLabel)
				.addComponent(alphaField, 30, 50, 70)
				.addGap(5, 5, 100)
				.addComponent(exportDataset));
		for(int i = 0; i < MAX_OBJCOUNT; i++) {
			group3.addComponent(objSliders[i], 100, 200, 500);
		}

		GroupLayout.SequentialGroup group2 = layout.createSequentialGroup();
		group2.addComponent(chartPanel2, 200, 350, 500);
		group2.addComponent(scrollPane, 100, 150, 500);

		GroupLayout.SequentialGroup group4 = layout.createSequentialGroup();
		group4.addGroup(layout.createParallelGroup()
				.addComponent(alphaLabel, 15, 20, 25)
				.addComponent(alphaField, 15, 20, 25)
				.addComponent(exportDataset, 15, 20, 25));
		for(int i = 0; i < MAX_OBJCOUNT; i++) {
			group4.addComponent(objSliders[i], 30, 50, 80);
		}

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(group1)
				.addGroup(group3)
				.addComponent(chartPanel1, 500, 500, 5000));

		layout.setVerticalGroup(layout.createParallelGroup()
				.addGroup(group2)
				.addGroup(group4)
				.addComponent(chartPanel1, 500, 500, 5000)
				.addGap(0, 500, 5000));

		if(hydroChart.getRangeAxis() != null) {
			hydroChart.getRangeAxis().setAutoRange(true);
		}
		if(hydroChart.getDomainAxis() != null) {
			hydroChart.getDomainAxis().setAutoRange(true);
		}

		for(int i = 0; i < objSliders.length; i++) {
			objSliders[i].addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if(onAdjustment) {
						return;
					}

					onAdjustment = true;
					JSlider actSlider = (JSlider) e.getSource();

					int allValues = 0;
					for(JSlider slider : objSliders) {
						allValues += slider.getValue();
					}
					double correction[] = new double[objSliders.length];
					for(int i = 0; i < objSliders.length; i++) {
						if(objSliders[i] != actSlider) {
							correction[i] = (100.0 - allValues) / (objSliders.length - 1.);
						} else {
							correction[i] = 0;
						}
					}
					double redistribution = allValues;
					while(Math.abs(redistribution) > 0.5) {
						int c = 0;
						for(int i = 0; i < objSliders.length; i++) {
							if(correction[i] > 0) {
								if(objSliders[i].getValue() + correction[i] < objSliders[i].getMaximum()) {
									objSliders[i].setValue((int) (objSliders[i].getValue() + correction[i]));
									c++;
								} else {
									correction[i] += (objSliders[i].getValue() - objSliders[i].getMaximum());
									objSliders[i].setValue(objSliders[i].getMaximum());
								}
							} else {
								if(objSliders[i].getValue() + correction[i] > objSliders[i].getMinimum()) {
									objSliders[i].setValue((int) (objSliders[i].getValue() + correction[i]));
									c++;
								} else {
									correction[i] += (objSliders[i].getValue() - objSliders[i].getMinimum());
									objSliders[i].setValue(objSliders[i].getMinimum());
								}
							}
						}
						redistribution = 0;
						for(int i = 0; i < m; i++) {
							redistribution += correction[i];
						}
						for(int i = 0; i < m; i++) {
							if(objSliders[i] == actSlider) {
								continue;
							}
							if(correction[i] != 0) {
								correction[i] = 0;
								continue;
							}
							correction[i] = redistribution / (double) c;
						}
					}

					updateSpiderPlot();

					onAdjustment = false;
				}
			});
		}
	}

	private void setStandardColors() {
		spiderPlot.setSeriesPaint(0, Color.blue);
		spiderPlot.setSeriesPaint(1, Color.GREEN);
		spiderPlot.setSeriesPaint(2, Color.orange);
		spiderPlot.setSeriesPaint(3, Color.magenta);
		spiderPlot.setSeriesPaint(4, Color.PINK);
		spiderPlot.setSeriesPaint(5, Color.yellow);

		spiderPlot.setSeriesPaint(m, Color.BLACK);
	}

	private void updateParameterSet(int index) {
		double selectedParameterSet[] = new double[x.length];
		String parameterName[] = new String[x.length];

		int i = 0;
		for(SimpleEnsemble e : x) {
			selectedParameterSet[i] = e.getValue(e.getId(index));
			parameterName[i] = e.getName();
			i++;
			System.out.println(e.getName() + ":\t" + e.getValue(e.getId(index)));
		}
	}

	private void updateSimulation(int index) {
		if(ts == null) {
			return;
		}

		TimeSeries dataset2 = new TimeSeries(JAMS.i18n("Simulation"));
		int T = ts.getTimesteps();
		double timeseries[] = ts.getValue(ts.getId(index));

		for(int i = 0; i < T; i++) {
			Day d = new Day(ts.getDate((int) i));
			dataset2.add(d, timeseries[i]);
		}

		TimeSeriesCollection sim_runoff = new TimeSeriesCollection();
		sim_runoff.addSeries(dataset2);
		hydroChart.setDataset(1, sim_runoff);
	}

	@Override
	public void refresh() throws NoDataException {
		if(!this.isRequestFulfilled()) {
			return;
		}
		alpha = Tools.readField(alphaField, 0.1);

		initDataSet();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		ArrayList<DataSet> p[] = getData(new int[]{0, 1, 2});

		ts = (TimeSerieEnsemble) p[0].get(0);

		Measurement obs = null;
		if(p[2].size() > 0) {
			obs = (Measurement) p[2].get(0);
		}

		int T = ts.getTimesteps();

		if(obs != null) {
			TimeSeries dataset1 = new TimeSeries(JAMS.i18n("Measurement"));
			for(int i = 0; i < T; i++) {
				Day d = new Day(obs.getTime((int) i));
				dataset1.add(d, obs.getValue(i));
			}
			TimeSeriesCollection obs_runoff = new TimeSeriesCollection();
			obs_runoff.addSeries(dataset1);
			hydroChart.setDataset(0, obs_runoff);
		} else {
			hydroChart.setDataset(0, null);
		}

		updateSimulation(0);
		updateSpiderPlot();
	}

	private void initDataSet() {
		Set<String> xSet = this.getDataSource().getDatasets(Parameter.class);
		x = new SimpleEnsemble[xSet.size()];
		int counter = 0;
		for(String name : xSet) {
			x[counter++] = this.getDataSource().getSimpleEnsemble(name);
		}
		Set<String> ySet = this.getDataSource().getDatasets(Efficiency.class);
		y = new SimpleEnsemble[ySet.size()];
		counter = 0;
		for(String name : ySet) {
			y[counter++] = this.getDataSource().getSimpleEnsemble(name);
		}
		N = this.getDataSource().getSimulationCount();
		m = y.length;

		DecimalFormat format = new DecimalFormat("#.###"); //TODO
		for(int i = 0; i < MAX_OBJCOUNT; i++) {
			if(i < m) {
				objSliders[i].setVisible(true);
				objSliders[i].setMinimum(0);
				objSliders[i].setMaximum(100);
				Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
				for(int j = 0; j < 101; j += 25) {
					double value = ((double) j / 100.);
					labelTable.put(new Integer(j), new JLabel(format.format(value)));
				}
				objSliders[i].setLabelTable(labelTable);
				objSliders[i].setPaintLabels(true);
				objSliders[i].setBorder(BorderFactory.createTitledBorder(y[i].getName()));
			} else {
				objSliders[i].setVisible(false);
				objSliders[i].setPaintLabels(false);
			}
		}

		SampleFactory factory = new SampleFactory();
		this.getDataSource().constructSample(factory);
		Statistics stats = factory.getStatistics();
		paretoFront = stats.getParetoFront();
		rt = new RankingTable(paretoFront);
		rt.setAlpha(alpha);
		rt.computeRankings();

		objSliders[0].setValue(0);

		if(selectedId == -1) {
			selectedId = this.ts.getId(0);
		}

		setStandardColors();

		datasetValid = true;
	}

	private void updateSpiderPlot() {
		if(!datasetValid) {
			return;
		}
		//calculate sample based on weights
		double w[] = new double[MAX_OBJCOUNT];
		for(int i = 0; i < MAX_OBJCOUNT; i++) {
			w[i] = 0.01 * (double) this.objSliders[i].getValue();
		}

		double bestScore = Double.MAX_VALUE;
		Sample bestSample = null;
		for(Sample s : paretoFront) {
			double score = 0;
			for(int i = 0; i < s.F().length; i++) {
				score += w[i] * s.F()[i];
			}
			if(score < bestScore) {
				bestSample = s;
				bestScore = score;
			}

		}

		candidates = new ArrayList<Sample>();
		candidates.addAll(Arrays.asList(rt.getCandidates()));
		candidates.add(bestSample);

		updateCandidateTable();
		drawSpiderPlot();
	}

	private void drawSpiderPlot() {
		DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();

		double[] mins = new double[y.length];
		double[] maxs = new double[y.length];
		for(int i = 0; i < y.length; i++) {
			mins[i] = Double.POSITIVE_INFINITY;
			maxs[i] = Double.NEGATIVE_INFINITY;
		}

		for(int sampleNumber = 0; sampleNumber < candidates.size(); ++sampleNumber) {
			Sample sample = candidates.get(sampleNumber);
			for(int i = 0; i < sample.F().length; i++) {
				if(visibleCandidates[sampleNumber]) {
					double neff = -sample.F()[i];
					//eff is also negativeff i.e lower eff value means better
					mins[i] = Math.min(mins[i], neff);
					maxs[i] = Math.max(maxs[i], neff);

					//TODO invert labels of axes, such that eff instead of -eff is shown
					categoryDataset.addValue(neff, "Sample " + sampleNumber, y[i].name);
				} else {
					categoryDataset.addValue(Double.NaN, "Sample " + sampleNumber, y[i].name);
				}
			}
		}

		this.spiderPlot.setDataset(categoryDataset);

		spiderPlot.setDataInverted(true);
		spiderPlot.setAxisTickVisible(true);
		spiderPlot.setNumberOfTicks(3);
		spiderPlot.setWebFilled(false);

		for(int i = 0; i < y.length; i++) {
			if(mins[i] > 0.0) {
				spiderPlot.setOrigin(i, .9 * mins[i]);
			} else {
				spiderPlot.setOrigin(i, 1.1 * mins[i]);
			}

			if(maxs[i] > 0.0) {
				spiderPlot.setMaxValue(i, 1.1 * maxs[i]);
			} else {
				spiderPlot.setMaxValue(i, 0.9 * maxs[i]);
			}
		}
	}

	private void updateCandidateTable() {
		if(visibleCandidates == null || visibleCandidates.length != candidates.size()) {
			visibleCandidates = new boolean[candidates.size()];
			for(int i = 0; i < visibleCandidates.length; ++i) {
				visibleCandidates[i] = true;
			}
		}
		candidateTable.setModel(new AbstractTableModel() {
			private static final long serialVersionUID = -9062878893282509429L;

			@Override
			public int getRowCount() {
				return candidates.size();
			}

			@Override
			public int getColumnCount() {
				return x.length + 2;
			}

			@Override
			public String getColumnName(int column) {
				switch(column) {
					case 0:
						return "Visible";
					case 1:
						return "Color";
					default:
						return x[column - 2].name;
				}
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				switch(columnIndex) {
					case 0:
						return Boolean.class;
					case 1:
						return Color.class;
					default:
						return Double.class;
				}
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex == 0;
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				switch(columnIndex) {
					case 0:
						return visibleCandidates[rowIndex];
					case 1:
						return (Color) spiderPlot.getSeriesPaint(rowIndex);
					default:
						return candidates.get(rowIndex).x[columnIndex - 2];
				}
			}

			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				boolean value = (Boolean) aValue;
				visibleCandidates[rowIndex] = value;
				drawSpiderPlot();
			}
		});
		candidateTable.getColumnModel().getColumn(1).setCellRenderer(new ColorRenderer(true));
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
		DataCollection dc = DataCollection.createFromFile(new File("E:\\ModelData\\Testgebiete\\J2000\\Gehlberg\\output\\20130824_003244\\2013_08_21_sa_small2.cdat"));

		try {
			DataRequestPanel d = new DataRequestPanel(new MultiObjectiveDecisionSupport(), dc);
			JFrame plotWindow = new JFrame("test");
			plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			plotWindow.setLayout(new BorderLayout());
			plotWindow.setVisible(true);
			plotWindow.setSize(800, 700);
			plotWindow.add(d, BorderLayout.CENTER);
		} catch(NoDataException nde) {
		}

	}
}
