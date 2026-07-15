/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import optas.data.DataCollection;
import org.jfree.chart.JFreeChart;
import optas.data.Efficiency;
import optas.data.EfficiencyEnsemble;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;
import optas.regression.SimpleInterpolation;
import optas.regression.SimpleNeuralNetwork;
import optas.tools.PatchedChartPanel;
import org.jfree.chart.plot.XYPlot;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class ResponseSurface extends MCAT5Plot {

    XYPlot plot = new XYPlot();
    PatchedChartPanel chartPanel = null;
    JPanel panel = null;
    JPanel mainPanel = null;
    JSlider sliders[] = null;
    
    SimpleEnsemble[] x;
    EfficiencyEnsemble y;

    final int SliderResolution = 100;
    final int RESOLUTION = 100;

    double map[][] = new double[RESOLUTION][RESOLUTION];

    int param1, param2;

    double paramMin1, paramMax1;
    double paramMin2, paramMax2;

    JTextField paramMin1Field = new JTextField(8), paramMax1Field = new JTextField(8);
    JTextField paramMin2Field = new JTextField(8), paramMax2Field = new JTextField(8);

    double point[];

    DataCollection dc = null;
    SimpleNeuralNetwork ANN = null;
    
    public ResponseSurface() {
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"),Efficiency.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER"),Parameter.class,1,10));

        init();
    }

    private JComponent createAdjustmentPanel(SimpleEnsemble p[], EfficiencyEnsemble e){
        JPanel adjustmentPanel = new JPanel(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(adjustmentPanel);

        JPanel sliderPanel = new JPanel(new GridLayout(p.length,1));
        
        for (int i=0;i<p.length;i++){
            JPanel sliderGroup = new JPanel(new FlowLayout());
            sliderGroup.setBorder(BorderFactory.createTitledBorder(p[i].name));
            JSlider slider = new JSlider();

            slider.setBorder(BorderFactory.createTitledBorder("Parameter Space"));
            slider.setMaximum(SliderResolution-1);
            slider.setMinimum(0);
            slider.setMajorTickSpacing(50);
            slider.setMinorTickSpacing(50);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            slider.putClientProperty("parameter", p[i]);
            slider.putClientProperty("index", new Integer(i));
            slider.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent evt) {
                    JSlider slider = (JSlider)evt.getSource();

                    double paramMin = ((SimpleEnsemble)(slider.getClientProperty("paramter"))).getMin();
                    double paramMax = ((SimpleEnsemble)(slider.getClientProperty("paramter"))).getMax();

                    int index = (Integer)slider.getClientProperty("paramter");
                    point[index] = (((paramMax - paramMin) * (double) slider.getValue()) / (double) SliderResolution) + paramMin;                    
                }
            });

            sliderGroup.add(slider);
            sliderPanel.add(sliderGroup);
        }

        JComboBox parameter1Box = new JComboBox(p);
        JComboBox parameter2Box = new JComboBox(p);

        parameter1Box.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                JComboBox src = (JComboBox )e.getSource();
                SimpleEnsemble p = (SimpleEnsemble)src.getSelectedItem();
                paramMin1 = p.getMin();
                paramMax1 = p.getMax();

                paramMin1Field.setText(Double.toString(paramMin1));
                paramMax1Field.setText(Double.toString(paramMax1));

                param1 = src.getSelectedIndex();
            }
        });

        parameter2Box.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                JComboBox src = (JComboBox )e.getSource();
                SimpleEnsemble p = (SimpleEnsemble)src.getSelectedItem();
                paramMin2 = p.getMin();
                paramMax2 = p.getMax();

                paramMin2Field.setText(Double.toString(paramMin2));
                paramMax2Field.setText(Double.toString(paramMax2));

                param2 = src.getSelectedIndex();
            }
        });

        JLabel parameter1Min = new JLabel("min");
        JLabel parameter1Max = new JLabel("max");
        JLabel parameter2Min = new JLabel("min");
        JLabel parameter2Max = new JLabel("max");

        JPanel param1Panel = new JPanel();
        param1Panel.setBorder(BorderFactory.createTitledBorder("Parameter 1"));
        GroupLayout layout1 = new GroupLayout(param1Panel);
        param1Panel.setLayout(layout1);
        layout1.setAutoCreateContainerGaps(true);
        layout1.setAutoCreateGaps(true);
        layout1.setHorizontalGroup(layout1.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(parameter1Box)
                .addGroup(layout1.createSequentialGroup()
                    .addComponent(parameter1Min)
                    .addComponent(paramMin1Field)
                    .addComponent(parameter1Max)
                    .addComponent(paramMax1Field)
                )
        );
        layout1.setVerticalGroup(layout1.createSequentialGroup()
                .addComponent(parameter1Box)
                .addGroup(layout1.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(parameter1Min)
                    .addComponent(paramMin1Field)
                    .addComponent(parameter1Max)
                    .addComponent(paramMax1Field)
                )
        );

        JPanel param2Panel = new JPanel();
        param2Panel.setBorder(BorderFactory.createTitledBorder("Parameter 2"));
        GroupLayout layout2 = new GroupLayout(param2Panel);
        param2Panel.setLayout(layout2);
        layout2.setAutoCreateContainerGaps(true);
        layout2.setAutoCreateGaps(true);
        layout2.setHorizontalGroup(layout2.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(parameter2Box)
                .addGroup(layout2.createSequentialGroup()
                    .addComponent(parameter2Min)
                    .addComponent(paramMin2Field)
                    .addComponent(parameter2Max)
                    .addComponent(paramMax2Field)
                )
        );
        layout2.setVerticalGroup(layout2.createSequentialGroup()
                .addComponent(parameter2Box)
                .addGroup(layout1.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(parameter2Min)
                    .addComponent(paramMin2Field)
                    .addComponent(parameter2Max)
                    .addComponent(paramMax2Field)
                )
        );

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    paramMin1 = Double.parseDouble(paramMin1Field.getText());
                    paramMin2 = Double.parseDouble(paramMin2Field.getText());
                    paramMax1 = Double.parseDouble(paramMax1Field.getText());
                    paramMax2 = Double.parseDouble(paramMax2Field.getText());
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(chartPanel, "ParseException");
                    return;
                }
                redraw();
            }
        });

        GroupLayout layout = new GroupLayout(adjustmentPanel);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        adjustmentPanel.setLayout(layout);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(sliderPanel)
            .addComponent(param1Panel)
            .addComponent(param2Panel)
            .addComponent(refreshButton)
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(sliderPanel)
            .addComponent(param1Panel)
            .addComponent(param2Panel)
            .addComponent(refreshButton)
        );

        return scrollPane;
    }

    private void init(){
        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle("Response Surface");
        chartPanel = new PatchedChartPanel(chart, true);
        chartPanel.setMinimumDrawWidth( 0 );
        chartPanel.setMinimumDrawHeight( 0 );
        chartPanel.setMaximumDrawWidth( MAXIMUM_WIDTH );
        chartPanel.setMaximumDrawHeight( MAXIMUM_HEIGHT );
        
        panel = new JPanel(new BorderLayout());
        panel.add(chartPanel,BorderLayout.WEST);
    }

    public void refresh() throws NoDataException{
        if (!this.isRequestFulfilled())
            return;

        if (dc == null) {
            dc = this.getDataSource();

            Set<String> xSet = this.getDataSource().getDatasets(Parameter.class);
            ArrayList<optas.data.DataSet> p[] = getData(new int[]{0});
            y = (EfficiencyEnsemble) p[1].get(0);

            x = new SimpleEnsemble[xSet.size()];
            int counter = 0;
            for (String name : xSet) {
                x[counter++] = this.getDataSource().getSimpleEnsemble(name);
            }

            mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(panel, BorderLayout.CENTER);
            mainPanel.add(createAdjustmentPanel(x, y), BorderLayout.EAST);

            ANN = (new SimpleNeuralNetwork());
            ANN.setxNormalizationMethod(SimpleInterpolation.NormalizationMethod.Linear );
            ANN.setyNormalizationMethod(SimpleInterpolation.NormalizationMethod.Linear);

            int n = counter;
            ANN.setData(x, y);
            System.out.println("expected error is: " + ANN.estimateCrossValidationError(5, SimpleInterpolation.ErrorMethod.E2));
        }

        int n = x.length;
        double VOI[] = new double[n];

        for (int i=0;i<n;i++){
            VOI[i] = this.point[i];
        }

        for (int i=0;i<this.RESOLUTION;i++){
            double t1 = (double)i / (double)(this.RESOLUTION - 1);
            VOI[this.param1] = this.paramMin1 + t1*(this.paramMax1 - this.paramMin1);

            for (int j=0;j<this.RESOLUTION;j++){
                double t2 = (double)j / (double)(this.RESOLUTION - 1);
                VOI[this.param2] = this.paramMin2 + t2*(this.paramMax2 - this.paramMin2);

                this.map[i][j] = ANN.getInterpolatedValue(VOI)[0];
            }
        }
        //this.g

        /*ArrayList<DataSet> p[] = getData(new int[]{0,1,2});
        timeserie = (TimeSerieEnsemble)p[0].get(0);
        ArrayList<DataSet>  dataInParam   = (ArrayList<DataSet>)p[1];
        Measurement obs = (Measurement) p[2].get(0);

        params = new SimpleEnsemble[dataInParam.size()];
        int timesteps = timeserie.getTimesteps();

        plot.setRangeAxis(new NumberAxis(this.x[param1].name));
        plot.setRangeAxis(1, new NumberAxis(this.x[param2].name));

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));

        getWinSize();

        XYSeries dataset = new XYSeries(obs.name);

        int n = ts.getSize();
        int T = ts.getTimesteps();
        double maxParam = param.getMax(),
                minParam = param.getMin();
        double pixel_map[][] = new double[3][T * this.BOX_COUNT];

        for (int i = 0; i < T; i++) {
            EfficiencyEnsemble eff = new EfficiencyEnsemble("E1(window)", n);
            eff.setPositiveBest(true);

            double cur_obs[] = new double[2 * window_size + 1];
            double cur_sim[] = new double[2 * window_size + 1];

            int counter = 0;
            for (int j = i - window_size; j <= i + window_size; j++) {
                if (j < 0 || j >= T) {
                    cur_obs[counter++] = 0;
                } else {
                    cur_obs[counter++] = obs.getValue(j);
                }
            }
            //calculate efficiency
            for (int k = 0; k < n; k++) {
                counter = 0;
                for (int j = i - window_size; j <= i + window_size; j++) {
                    if (j < 0 || j >= T) {
                        cur_sim[counter++] = 0;
                    } else {
                        cur_sim[counter++] = ts.get(j, ts.getId(k));
                    }
                }
                eff.add(new Integer(ts.getId(k)), Efficiencies.CalculateE(cur_obs, cur_sim, 2));
            }
            //transform to likelihood
            EfficiencyEnsemble likelihood = eff.CalculateLikelihood();
            //double cur_likelihood[] = Efficiencies.CalculateLikelihood(cur_eff);
            //and sort it
            //double sortedData[][] = sortbyEff(timeserie_param.set,cur_likelihood);
            Integer sortedIds[] = likelihood.sort();
            int limit = (int) (n * 0.1);
            double boxes[] = new double[BOX_COUNT];

            for (int j = 0; j < limit; j++) {
                double best = param.getValue(sortedIds[j]);
                int index = (int) ((best - minParam) / (maxParam - minParam) * boxes.length);
                if (index == boxes.length) {
                    index = boxes.length - 1;
                }
                boxes[index] += 1.0 / limit;
            }

            for (int j = 0; j < BOX_COUNT; j++) {
                pixel_map[0][i * BOX_COUNT + j] = obs.getTime(i).getTime();
                pixel_map[1][i * BOX_COUNT + j] = minParam + (maxParam - minParam) * (double) j / (double) BOX_COUNT;
                pixel_map[2][i * BOX_COUNT + j] = 1.0 - boxes[j];
            }
        }

        XYBlockRenderer bg_renderer = new XYBlockRenderer();
        bg_renderer.setPaintScale(new GrayPaintScale(0, 1));
        bg_renderer.setBlockHeight((maxParam - minParam) / BOX_COUNT);
        bg_renderer.setBlockWidth(1.00);
        bg_renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
        DefaultXYZDataset xyz_dataset = new DefaultXYZDataset();
        xyz_dataset.addSeries(0, pixel_map);
        plot.setDataset(1, xyz_dataset);
        plot.setRenderer(1, bg_renderer);
        bg_renderer.setSeriesVisibleInLegend(0, false);
        //at last plot observed data
        double obs_min = obs.getMin();
        double obs_max = obs.getMax();

        for (int i = 0; i < T; i++) {
            dataset.add(obs.getTime(i).getTime(), ((obs.getValue(i) - obs_min) / (obs_max - obs_min)) * (maxParam - minParam) + minParam);
        }
        plot.setDataset(0, new XYSeriesCollection(dataset));

        if (plot.getRangeAxis() != null) {
            plot.getRangeAxis(0).setRange(new Range(minParam, maxParam));
        }
        if (plot.getDomainAxis() != null) {
            plot.getDomainAxis().setRange(new Range(obs.getTime(0).getTime(), obs.getTime(T - 1).getTime()));
        }
        if (plot.getRangeAxis(1) != null) {
            plot.getRangeAxis(1).setRange(new Range(obs_min, obs_max));
        }*/
    }
/*
    public void doInterpolation(int parameter) {
        double weights[][] = new double[RESOLUTION][this.timeserie.getSize()];
        double sum[] = new double[RESOLUTION];

        for (int r = 0; r < RESOLUTION; r++) {
            double p = paramMin + (paramMax - paramMin) * (double) r / (double) RESOLUTION;
            sum[r] = 0;

            for (int i = 0; i < this.timeserie.getSize(); i++) {
                double dist = 0;
                for (int j = 0; j < params.length; j++) {
                    if (j != parameter) {
                        dist += (params[j].getValue(i) - this.point[j]) * (params[j].getValue(i) - this.point[j]);
                    } else {
                        dist += (params[j].getValue(i) - p) * (params[j].getValue(i) - p);
                    }
                }
                weights[r][i] = 1.0/Math.sqrt(dist);
                sum[r] += weights[r][i];
            }
        }
        for (int r = 0; r < RESOLUTION; r++) {
            for (int i = 0; i < this.timeserie.getSize(); i++) {
                weights[r][i] /= sum[r];
            }
            for (int t = 0; t < this.timeserie.getTimesteps();t++){
                interpolatedTS[r][t] = 0;
                for (int i = 0; i < this.timeserie.getSize(); i++) {
                    interpolatedTS[r][t] += timeserie.get(t, i)*weights[r][i];
                }
            }

        }

    }*/
    
    public JPanel getPanel() {
        return panel;
    }
}
