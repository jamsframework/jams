/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import optas.gui.MCAT5.MCAT5Plot.NoDataException;



/**
 *
 * @author Christian Fischer
 */
public class MCAT5Toolbar extends JToolBar {

    public static class ArrayComparator implements Comparator {

        private int col = 0;
        private int order = 1;

        public ArrayComparator(int col, boolean decreasing_order) {
            this.col = col;
            if (decreasing_order) {
                order = -1;
            } else {
                order = 1;
            }
        }

        @Override
        public int compare(Object d1, Object d2) {

            double[] b1 = (double[]) d1;
            double[] b2 = (double[]) d2;

            if (b1[col] < b2[col]) {
                return -1 * order;
            } else if (b1[col] == b2[col]) {
                return 0 * order;
            } else {
                return 1 * order;
            }
        }
    }

    public static class ObservationDataSet {

        public double[] set;
        public String name;
        public int timeLength;

        @Override
        public String toString() {
            return name + JAMS.i18n("_TIMESTEPS:") + timeLength;
        }
    };

    public static class ParameterSet {

        public double[] set;
        public String name;
        public MonteCarloData parent = null;

        @Override
        public String toString() {
            return name;
        }
    };

    public static class SimulationTimeSeriesDataSet {

        public SimulationDataSet set[];
        public String name;
        public int timeLength;
        public MonteCarloData parent = null;

        @Override
        public String toString() {
            return name + JAMS.i18n("_TIMESTEPS:") + timeLength;
        }
    };

    public static class SimulationDataSet {

        public double[] set;
        public String name;
        public MonteCarloData parent = null;

        @Override
        public String toString() {
            return name;
        }
    };

    public static class EfficiencyDataSet {

        public double[] set;
        public String name;
        public boolean isPositveEff;
        public MonteCarloData parent = null;
        public SimulationTimeSeriesDataSet source1 = null;
        public ObservationDataSet source2 = null;

        EfficiencyDataSet(String name, double[] set, SimulationTimeSeriesDataSet src1, ObservationDataSet src2, boolean isPositiveEff) {
            this.name = name;
            this.set = set;
            this.isPositveEff = isPositiveEff;
            this.source1 = src1;
            this.source2 = src2;
        }

        @Override
        public String toString() {
            return name;
        }
    };

    public static class MonteCarloData {

        public int numberOfRuns;
        public String name;
        ArrayList<ObservationDataSet> observations;
        ArrayList<SimulationDataSet> simulations;
        ArrayList<SimulationTimeSeriesDataSet> ts_simulations;
        ArrayList<ParameterSet> parameters;
        ArrayList<EfficiencyDataSet> efficiencies;

        public MonteCarloData() {
            numberOfRuns = -1;
            name = JAMS.i18n("DEFAULT");
            observations = new ArrayList<ObservationDataSet>();
            simulations = new ArrayList<SimulationDataSet>();
            ts_simulations = new ArrayList<SimulationTimeSeriesDataSet>();
            parameters = new ArrayList<ParameterSet>();
            efficiencies = new ArrayList<EfficiencyDataSet>();
        }

        public String addObservationDataSet(ObservationDataSet p) {
            p.timeLength = p.set.length;
            for (int i = 0; i < ts_simulations.size(); i++) {
                if (ts_simulations.get(i).timeLength != p.timeLength) {
                    return JAMS.i18n("THERE_ARE_TIMESERIES_WITH_DIFFERENT_LENGTH,_OBSERVATION_WAS_NOT_ADDED");
                }
            }
            for (int i = 0; i < observations.size(); i++) {
                if (observations.get(i).timeLength != p.timeLength) {
                    return JAMS.i18n("THERE_ARE_TIMESERIES_WITH_DIFFERENT_LENGTH,_OBSERVATION_WAS_NOT_ADDED");
                }
            }
            observations.add(p);
            return null;
        }

        public ArrayList<ObservationDataSet> getObservationDataSet() {
            return observations;
        }

        public String addSimulationDataSet(SimulationDataSet p) {
            if (this.numberOfRuns == -1) {
                this.numberOfRuns = p.set.length;
            }
            if (p.set.length != this.numberOfRuns) {
                return JAMS.i18n("THERE_ARE_SERIES_WITH_DIFFERENT_LENGTH,_SIMULATED_DATA_SET_WAS_NOT_ADDED");
            }
            p.parent = this;
            simulations.add(p);
            return null;
        }

        public ArrayList<SimulationDataSet> getSimulationDataSet() {
            return simulations;
        }

        public String addSimulationTimeSeriesDataSet(SimulationTimeSeriesDataSet p) {
            p.timeLength = p.set.length;
            if (this.numberOfRuns == -1) {
                if (p.set.length != 0) {
                    this.numberOfRuns = p.set[0].set.length;
                }
            }
            for (int i = 0; i < ts_simulations.size(); i++) {
                if (ts_simulations.get(i).timeLength != p.timeLength) {
                    return JAMS.i18n("THERE_ARE_TIMESERIES_WITH_DIFFERENT_LENGTH,_SIMULATION_TIME_SERIES_WERE_NOT_ADDED");
                }
            }
            for (int i = 0; i < observations.size(); i++) {
                if (observations.get(i).timeLength != p.timeLength) {
                    return JAMS.i18n("THERE_ARE_TIMESERIES_WITH_DIFFERENT_LENGTH,_SIMULATION_TIME_SERIES_WERE_NOT_ADDED");
                }
            }
            for (int i = 0; i < p.timeLength; i++) {
                if (p.set[i].set.length != numberOfRuns) {
                    return JAMS.i18n("THERE_ARE_TIMESERIES_WITH_DIFFERENT_LENGTH,_SIMULATION_TIME_SERIES_WERE_NOT_ADDED");
                }
                p.set[i].parent = this;
            }
            p.parent = this;
            ts_simulations.add(p);
            return null;
        }

        public ArrayList<SimulationTimeSeriesDataSet> getSimulationTimeSeriesDataSet() {
            return ts_simulations;
        }

        public String addParameterSet(ParameterSet p) {
            if (this.numberOfRuns == -1) {
                this.numberOfRuns = p.set.length;
            }
            if (p.set.length != numberOfRuns) {
                return JAMS.i18n("NUMBER_OF_PARAMETER_SETS_DOES_NOT_FIT_NUMBER_OF_MONTO_CARLO_RUNS");
            }
            p.parent = this;
            parameters.add(p);
            return null;
        }

        public ArrayList<ParameterSet> getParameterSet() {
            return parameters;
        }

        public String addEfficiencyDataSet(EfficiencyDataSet p) {
            if (this.numberOfRuns == -1) {
                this.numberOfRuns = p.set.length;
            }
            if (p.set.length != numberOfRuns) {
                return JAMS.i18n("NUMBER_OF_MODEL_RUNS_DOES_NOT_MATCH");
            }
            p.parent = this;
            efficiencies.add(p);
            return null;
        }

        public ArrayList<EfficiencyDataSet> getEfficiencyDataSet() {
            return efficiencies;
        }

        @Override
        public String toString() {
            return JAMS.i18n("MONTE_CARLO_SIMULATION");
        }
    }

    private class PlotDesc{
        ImageIcon icon;
        String tooltip,title;
        Class clazz;

        PlotDesc(ImageIcon icon, String tooltip, String title, Class clazz){
            this.icon = icon;
            this.tooltip = tooltip;
            this.title = title;
            this.clazz = clazz;
        }
    }
   
    DataCollectionPanel owner;

    ArrayList<PlotDesc> registeredPlots = new ArrayList<PlotDesc>();

    public static JFrame getDefaultPlotWindow(String title) {
        JFrame plotWindow = new JFrame(title);
        plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        plotWindow.setLayout(new BorderLayout());        
        plotWindow.setVisible(true);
        plotWindow.setSize(800, 700);
        //this.setVisible(false);
        
        return plotWindow;
    }


    public MCAT5Toolbar(DataCollectionPanel param_owner) {
        super();
        

        registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/dottyplot.png")),
                JAMS.i18n("CREATE_DOTTY_PLOT"),
                JAMS.i18n("DOTTY_PLOT"),
                DottyPlot.class));

        /*registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/dottyplot.png")),
                "DottyPlot3D",
                "DottyPlot3D",
                DottyPlot3D.class));

        registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/dottyplot.png")),
                "Temporal Analyzer",
                JAMS.i18n("DOTTY_PLOT"),
                TemporalAnalysisGUI.class));

        registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/dottyplot.png")),
                "Interaction Analyzer",
                JAMS.i18n("DOTTY_PLOT"),
                ParameterInteractionAnalyser.class));
*/
        registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/sensitivity.png")),
                "Sensitivityanalyzer",
                JAMS.i18n("Sensitivity_Analysis"),
                ShowSensitivity.class));

        registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/aposterioriplot.png")),
                JAMS.i18n("CREATE_A_POSTERIORI_DISTRIBUTION_PLOT"),
                JAMS.i18n("A_POSTERIO_PARAMETER_DISTRIBUTION"),
                APosterioriPlot.class));

        registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/identifiabilityplot.png")),
                JAMS.i18n("IDENTIFIABILITY_PLOT"),
                JAMS.i18n("IDENTIFIABILITY_PLOT"),
                IdentifiabilityPlot.class));

        registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/bestpredictionplot.png")),
                JAMS.i18n("BEST_PREDICTION_PLOT"),
                JAMS.i18n("BEST_PREDICTION_PLOT"),
                BestPredictionPlot.class));

        registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/classplot.png")),
                JAMS.i18n("CLASS_PLOT"),
                JAMS.i18n("CLASS_PLOT"),
                ClassPlot.class));

        registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/dyniaplot.png")),
                JAMS.i18n("DYNIA"),
                JAMS.i18n("DYNIA"),
                DYNIA.class));

        registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/ParetoOutPlot.png")),
                JAMS.i18n("PARETO_OUTPUT_UNCERTAINITY"),
                JAMS.i18n("PARETO_OUTPUT_UNCERTAINITY"),
                ParetoOutputUncertainty.class));

        registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/GLUEOutPlot.png")),
                JAMS.i18n("GLUE_OUTPUT_UNCERTAINITY"),
                JAMS.i18n("OUTPUT_UNCERTAINTY_PLOT"),
                GLUEOutputUncertainty.class));

        registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/GLUEVarPlot.png")),
                JAMS.i18n("GLUE_VARIABLE_UNCERTAINITY"),
                JAMS.i18n("CUMULATIVE_DENSITY_PLOT"),
                GLUEVariableUncertainty.class));

        registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/normalisedparameter.png")),
                JAMS.i18n("NORMALIZED_PARAMETER_RANGE_PLOT"),
                JAMS.i18n("NORMALISED_PARAMETER_RANGE_PLOT"),
                NormalisedParameterRangePlot.class));

        registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/regionalsensitivity.png")),
                JAMS.i18n("REGIONAL_SENSITIVITY_ANALYSIS"),
                JAMS.i18n("REGIONAL_SENSITIVITY_ANALYSIS"),
                RegionalSensitivityAnalyser.class));

        registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/regionalsensitivity2.png")),
                JAMS.i18n("REGIONAL_SENSITIVITY_ANALYSIS_II"),
                JAMS.i18n("REGIONAL_SENSITIVITY_ANALYSIS_II"),
                RegionalSensitivityAnalyser2.class));

        /*registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/regionalsensitivity2.png")),
                "Experimental I",
                "Experimental I",
                optas.SA.APosterioriPlot.class));

        registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/regionalsensitivity2.png")),
                "Experimental II",
                "Experimental II",
                ParameterInterpolation2.class));*/

        for (PlotDesc pd : registeredPlots) {
            JButton button = new JButton(pd.icon);
            button.setToolTipText(pd.tooltip);
            button.putClientProperty("plotTitle", pd.title);
            button.putClientProperty("plotClass", pd.clazz);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {                    
                    Class c = (Class)((JButton)evt.getSource()).getClientProperty("plotClass");
                    MCAT5Plot o = null;
                    try{
                        o = (MCAT5Plot)c.getConstructor().newInstance();
                    }catch(Exception e){
                        System.out.println(e.toString());e.printStackTrace();
                    }
                    try{
                        DataRequestPanel d = new DataRequestPanel(o, MCAT5Toolbar.this.owner.getDataCollection());
                        JFrame plotWindow = getDefaultPlotWindow((String)((JButton)evt.getSource()).getClientProperty("plotTitle"));
                        plotWindow.add(d, BorderLayout.CENTER);
                    }catch(NoDataException nde){
                        JOptionPane.showMessageDialog(MCAT5Toolbar.this, nde.toString());
                    }
                }
            });
            this.add(button);
        }

        this.owner = param_owner;        
        this.setVisible(true);
        this.setFloatable(false);
    }
}
