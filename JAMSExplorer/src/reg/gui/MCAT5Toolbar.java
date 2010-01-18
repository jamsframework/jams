/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.gui;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JToolBar;
import reg.gui.MCAT5.APosterioriPlot;
import reg.gui.MCAT5.BestPredictionPlot;
import reg.gui.MCAT5.ClassPlot;
import reg.gui.MCAT5.DYNIA;
import reg.gui.MCAT5.DottyPlot;
import reg.gui.MCAT5.GLUEOutputUncertainty;
import reg.gui.MCAT5.GLUEVariableUncertainty;
import reg.gui.MCAT5.IdentifiabilityPlot;
import reg.gui.MCAT5.NormalisedParameterRangePlot;
import reg.gui.MCAT5.ParetoOutputUncertainty;
import reg.gui.MCAT5.RegionalSensitivityAnalyser;
import reg.gui.MCAT5.RegionalSensitivityAnalyser2;

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
            return name + java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("_TIMESTEPS:") + timeLength;
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
            return name + java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("_TIMESTEPS:") + timeLength;
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
        Vector<ObservationDataSet> observations;
        Vector<SimulationDataSet> simulations;
        Vector<SimulationTimeSeriesDataSet> ts_simulations;
        Vector<ParameterSet> parameters;
        Vector<EfficiencyDataSet> efficiencies;

        public MonteCarloData() {
            numberOfRuns = -1;
            name = java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("DEFAULT");
            observations = new Vector<ObservationDataSet>();
            simulations = new Vector<SimulationDataSet>();
            ts_simulations = new Vector<SimulationTimeSeriesDataSet>();
            parameters = new Vector<ParameterSet>();
            efficiencies = new Vector<EfficiencyDataSet>();
        }

        public String addObservationDataSet(ObservationDataSet p) {
            p.timeLength = p.set.length;
            for (int i = 0; i < ts_simulations.size(); i++) {
                if (ts_simulations.get(i).timeLength != p.timeLength) {
                    return java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("THERE_ARE_TIMESERIES_WITH_DIFFERENT_LENGTH,_OBSERVATION_WAS_NOT_ADDED");
                }
            }
            for (int i = 0; i < observations.size(); i++) {
                if (observations.get(i).timeLength != p.timeLength) {
                    return java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("THERE_ARE_TIMESERIES_WITH_DIFFERENT_LENGTH,_OBSERVATION_WAS_NOT_ADDED");
                }
            }
            observations.add(p);
            return null;
        }

        public Vector<ObservationDataSet> getObservationDataSet() {
            return observations;
        }

        public String addSimulationDataSet(SimulationDataSet p) {
            if (this.numberOfRuns == -1) {
                this.numberOfRuns = p.set.length;
            }
            if (p.set.length != this.numberOfRuns) {
                return java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("THERE_ARE_SERIES_WITH_DIFFERENT_LENGTH,_SIMULATED_DATA_SET_WAS_NOT_ADDED");
            }
            p.parent = this;
            simulations.add(p);
            return null;
        }

        public Vector<SimulationDataSet> getSimulationDataSet() {
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
                    return java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("THERE_ARE_TIMESERIES_WITH_DIFFERENT_LENGTH,_SIMULATION_TIME_SERIES_WERE_NOT_ADDED");
                }
            }
            for (int i = 0; i < observations.size(); i++) {
                if (observations.get(i).timeLength != p.timeLength) {
                    return java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("THERE_ARE_TIMESERIES_WITH_DIFFERENT_LENGTH,_SIMULATION_TIME_SERIES_WERE_NOT_ADDED");
                }
            }
            for (int i = 0; i < p.timeLength; i++) {
                if (p.set[i].set.length != numberOfRuns) {
                    return java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("THERE_ARE_TIMESERIES_WITH_DIFFERENT_LENGTH,_SIMULATION_TIME_SERIES_WERE_NOT_ADDED");
                }
                p.set[i].parent = this;
            }
            p.parent = this;
            ts_simulations.add(p);
            return null;
        }

        public Vector<SimulationTimeSeriesDataSet> getSimulationTimeSeriesDataSet() {
            return ts_simulations;
        }

        public String addParameterSet(ParameterSet p) {
            if (this.numberOfRuns == -1) {
                this.numberOfRuns = p.set.length;
            }
            if (p.set.length != numberOfRuns) {
                return java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("NUMBER_OF_PARAMETER_SETS_DOES_NOT_FIT_NUMBER_OF_MONTO_CARLO_RUNS");
            }
            p.parent = this;
            parameters.add(p);
            return null;
        }

        public Vector<ParameterSet> getParameterSet() {
            return parameters;
        }

        public String addEfficiencyDataSet(EfficiencyDataSet p) {
            if (this.numberOfRuns == -1) {
                this.numberOfRuns = p.set.length;
            }
            if (p.set.length != numberOfRuns) {
                return java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("NUMBER_OF_MODEL_RUNS_DOES_NOT_MATCH");
            }
            p.parent = this;
            efficiencies.add(p);
            return null;
        }

        public Vector<EfficiencyDataSet> getEfficiencyDataSet() {
            return efficiencies;
        }

        @Override
        public String toString() {
            return java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("MONTE_CARLO_SIMULATION");
        }
    }
    JButton dottyPlotObjFct = new JButton(new ImageIcon(getClass().getResource("/reg/resources/images/dottyplot.png")));
    JButton aPosterioParameterDistribution = new JButton(new ImageIcon(getClass().getResource("/reg/resources/images/aposterioriplot.png")));
    JButton identifiabilityPlot = new JButton(new ImageIcon(getClass().getResource("/reg/resources/images/identifiabilityplot.png")));
    JButton DYNIA = new JButton(new ImageIcon(getClass().getResource("/reg/resources/images/dyniaplot.png")));
    JButton RegionalSensitivityAnalysis = new JButton(new ImageIcon(getClass().getResource("/reg/resources/images/regionalsensitivity.png")));
    JButton RegionalSensitivityAnalysis2 = new JButton(new ImageIcon(getClass().getResource("/reg/resources/images/regionalsensitivity2.png")));
    JButton ClassPlot = new JButton(new ImageIcon(getClass().getResource("/reg/resources/images/classplot.png")));
    JButton GLUEVariableUncertainity = new JButton(new ImageIcon(getClass().getResource("/reg/resources/images/GLUEVarPlot.png")));
    JButton GLUEOutputUncertainity = new JButton(new ImageIcon(getClass().getResource("/reg/resources/images/GLUEOutPlot.png")));
    JButton ParetoOutputUncertainity = new JButton(new ImageIcon(getClass().getResource("/reg/resources/images/ParetoOutPlot.png")));
    JButton MultiObjectivePlots = new JButton(new ImageIcon(getClass().getResource("/reg/resources/images/multiobj.png")));
    JButton NormalizedParameterRangePlot = new JButton(new ImageIcon(getClass().getResource("/reg/resources/images/normalisedparameter.png")));
    JButton BestPredictionPlot = new JButton(new ImageIcon(getClass().getResource("/reg/resources/images/bestpredictionplot.png")));

    Window owner;
    
    public MCAT5Toolbar(Window param_owner) {
        super();
        this.owner = param_owner;
        dottyPlotObjFct.setToolTipText(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("CREATE_DOTTY_PLOT"));
        aPosterioParameterDistribution.setToolTipText(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("CREATE_A_POSTERIORI_DISTRIBUTION_PLOT"));
        identifiabilityPlot.setToolTipText(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("IDENTIFIABILITY_PLOT"));
        DYNIA.setToolTipText(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("DYNIA"));
        RegionalSensitivityAnalysis.setToolTipText(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("REGIONAL_SENSITIVITY_ANALYSIS"));
        RegionalSensitivityAnalysis2.setToolTipText(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("REGIONAL_SENSITIVITY_ANALYSIS_II"));
        ClassPlot.setToolTipText(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("CLASS_PLOT"));
        GLUEVariableUncertainity.setToolTipText(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("GLUE_VARIABLE_UNCERTAINITY"));
        GLUEOutputUncertainity.setToolTipText(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("GLUE_OUTPUT_UNCERTAINITY"));
        ParetoOutputUncertainity.setToolTipText(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("PARETO_OUTPUT_UNCERTAINITY"));
        MultiObjectivePlots.setToolTipText(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("MULTI_OBJECTIVE_PLOT"));
        NormalizedParameterRangePlot.setToolTipText(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("NORMALIZED_PARAMETER_RANGE_PLOT"));
        BestPredictionPlot.setToolTipText(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("BEST_PREDICTION_PLOT"));

        dottyPlotObjFct.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                DataRequestDlg.DataRequest req[] = new DataRequestDlg.DataRequest[2];
                req[0] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_PARAMETER, "test2", true);
                req[1] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_EFFICIENCY, "test", false);

                new DataRequestDlg(req,owner) {

                    public void dataCollectAction() {
                        for (int i = 0; i < this.mcData.parameters.size(); i++) {
                            JDialog plotWindow = new JDialog(this);
                            plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                            plotWindow.setLayout(new BorderLayout());
                            plotWindow.add((new DottyPlot(this.mcData.parameters.get(i), this.mcData.efficiencies.get(0)).getPanel()),BorderLayout.CENTER);
                            plotWindow.setVisible(true);
                            plotWindow.setSize(800, 400);
                        }
                    }
                };
            }
        });

        aPosterioParameterDistribution.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                DataRequestDlg.DataRequest req[] = new DataRequestDlg.DataRequest[2];
                req[0] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_PARAMETER, "test2", false);
                req[1] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_EFFICIENCY, "test", false);

                new DataRequestDlg(req,owner) {

                    public void dataCollectAction() {
                        JDialog plotWindow = new JDialog(this);
                        plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                        plotWindow.setLayout(new BorderLayout());
                        plotWindow.add((new APosterioriPlot(this.mcData.parameters.get(0), this.mcData.efficiencies.get(0)).getPanel()),BorderLayout.CENTER);                        
                        plotWindow.setVisible(true);
                        plotWindow.setSize(800, 400);

                    }
                };
            }
        });

        identifiabilityPlot.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        DataRequestDlg.DataRequest req[] = new DataRequestDlg.DataRequest[2];
                        req[0] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_PARAMETER, "test2", false);
                        req[1] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_EFFICIENCY, "test", false);

                        new DataRequestDlg(req,owner) {

                            public void dataCollectAction() {
                                JDialog plotWindow = new JDialog(this);
                                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);                                
                                plotWindow.setLayout(new BorderLayout());
                                plotWindow.add((new IdentifiabilityPlot(this.mcData.parameters.get(0), this.mcData.efficiencies.get(0)).getPanel()),BorderLayout.CENTER);
                                plotWindow.setVisible(true);
                                plotWindow.setSize(800, 400);                                
                            }
                        };
                    }
                });

        DYNIA.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        DataRequestDlg.DataRequest req[] = new DataRequestDlg.DataRequest[3];
                        req[0] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_PARAMETER, "test2", false);
                        req[1] = new DataRequestDlg.DataRequest(DataRequestDlg.SIMULATATED_TIMESERIE, "test", false);
                        req[2] = new DataRequestDlg.DataRequest(DataRequestDlg.OBSERVATED_TIMESERIE, "test", false);

                        new DataRequestDlg(req,owner) {

                            public void dataCollectAction() {
                                JDialog plotWindow = new JDialog(this);
                                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                plotWindow.setLayout(new BorderLayout());
                                plotWindow.add((new DYNIA(this.mcData.ts_simulations.get(0), this.mcData.parameters.get(0), this.mcData.observations.get(0)).getPanel()),BorderLayout.CENTER);                                
                                plotWindow.setVisible(true);
                                plotWindow.setSize(800, 400);

                            }
                        };
                    }
                });

        RegionalSensitivityAnalysis.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        DataRequestDlg.DataRequest req[] = new DataRequestDlg.DataRequest[2];
                        req[0] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_PARAMETER, "test2", false);
                        req[1] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_EFFICIENCY, "test", false);

                        new DataRequestDlg(req,owner) {

                            public void dataCollectAction() {
                                JDialog plotWindow = new JDialog(this);
                                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                plotWindow.setLayout(new BorderLayout());
                                plotWindow.add((new RegionalSensitivityAnalyser(this.mcData.parameters.get(0), this.mcData.efficiencies.get(0)).getPanel()),BorderLayout.CENTER);                                
                                plotWindow.setVisible(true);
                                plotWindow.setSize(800, 400);

                            }
                        };
                    }
                });

        RegionalSensitivityAnalysis2.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        DataRequestDlg.DataRequest req[] = new DataRequestDlg.DataRequest[2];
                        req[0] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_PARAMETER, "test2", false);
                        req[1] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_EFFICIENCY, "test", true);

                        new DataRequestDlg(req,owner) {

                            public void dataCollectAction() {
                                JDialog plotWindow = new JDialog(this);
                                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                EfficiencyDataSet effSet[] = new EfficiencyDataSet[this.mcData.efficiencies.size()];
                                for (int i = 0; i < effSet.length; i++) {
                                    effSet[i] = this.mcData.efficiencies.get(i);
                                }
                                plotWindow.setLayout(new BorderLayout());
                                plotWindow.add((new RegionalSensitivityAnalyser2(this.mcData.parameters.get(0), effSet).getPanel()),BorderLayout.CENTER);                                
                                plotWindow.setVisible(true);
                                plotWindow.setSize(800, 400);

                            }
                        };
                    }
                });

        ClassPlot.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        DataRequestDlg.DataRequest req[] = new DataRequestDlg.DataRequest[2];
                        req[0] = new DataRequestDlg.DataRequest(DataRequestDlg.SIMULATATED_TIMESERIE, "test2", false);
                        req[1] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_EFFICIENCY, "test", true);

                        new DataRequestDlg(req,owner) {

                            public void dataCollectAction() {
                                JDialog plotWindow = new JDialog(this);
                                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                plotWindow.setLayout(new BorderLayout());
                                plotWindow.add((new ClassPlot(this.mcData.ts_simulations.get(0), mcData.efficiencies.get(0)).getPanel()),BorderLayout.CENTER);
                                plotWindow.setVisible(true);
                                plotWindow.setSize(800, 400);

                            }
                        };
                    }
                });

        GLUEVariableUncertainity.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        DataRequestDlg.DataRequest req[] = new DataRequestDlg.DataRequest[2];
                        req[0] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_SIMULATION_VARIABLE, "test2", false);
                        req[1] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_EFFICIENCY, "test", false);

                        new DataRequestDlg(req,owner) {

                            public void dataCollectAction() {
                                JDialog plotWindow = new JDialog(this);
                                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                plotWindow.setLayout(new BorderLayout());
                                GLUEVariableUncertainty dataPlot = new GLUEVariableUncertainty(this.mcData.simulations.get(0), mcData.efficiencies.get(0));
                                plotWindow.add((dataPlot.getPanel1()), BorderLayout.NORTH);
                                plotWindow.add((dataPlot.getPanel2()), BorderLayout.SOUTH);
                                plotWindow.setVisible(true);
                                plotWindow.setSize(800, 900);

                            }
                        };
                    }
                });

        GLUEOutputUncertainity.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        DataRequestDlg.DataRequest req[] = new DataRequestDlg.DataRequest[2];
                        req[0] = new DataRequestDlg.DataRequest(DataRequestDlg.SIMULATATED_TIMESERIE, "test2", false);
                        req[1] = new DataRequestDlg.DataRequest(DataRequestDlg.OBSERVATED_TIMESERIE, "test", false);

                        new DataRequestDlg(req,owner) {

                            public void dataCollectAction() {
                                JDialog plotWindow = new JDialog(this);
                                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                plotWindow.setLayout(new BorderLayout());
                                GLUEOutputUncertainty dataPlot = new GLUEOutputUncertainty(this.mcData.ts_simulations.get(0), mcData.observations.get(0));
                                plotWindow.add(dataPlot.getPanel1(), BorderLayout.NORTH);
                                plotWindow.add(dataPlot.getPanel2(), BorderLayout.SOUTH);
                                plotWindow.setVisible(true);
                                plotWindow.setSize(800, 900);

                            }
                        };
                    }
                });

        ParetoOutputUncertainity.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        DataRequestDlg.DataRequest req[] = new DataRequestDlg.DataRequest[3];
                        req[0] = new DataRequestDlg.DataRequest(DataRequestDlg.SIMULATATED_TIMESERIE, "test2", false);
                        req[1] = new DataRequestDlg.DataRequest(DataRequestDlg.OBSERVATED_TIMESERIE, "test", false);
                        req[2] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_EFFICIENCY, "test2", true);

                        new DataRequestDlg(req,owner) {

                            public void dataCollectAction() {
                                JDialog plotWindow = new JDialog(this);
                                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                plotWindow.setLayout(new BorderLayout());
                                EfficiencyDataSet effSet[] = new EfficiencyDataSet[mcData.efficiencies.size()];
                                for (int i = 0; i < effSet.length; i++) {
                                    effSet[i] = mcData.efficiencies.get(i);
                                }
                                ParetoOutputUncertainty dataPlot = new ParetoOutputUncertainty(this.mcData.ts_simulations.get(0), mcData.observations.get(0), effSet);
                                plotWindow.add(dataPlot.getPanel1(), BorderLayout.CENTER);
                                plotWindow.setVisible(true);
                                plotWindow.setSize(800, 400);

                            }
                        };
                    }
                });

        MultiObjectivePlots.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        DataRequestDlg.DataRequest req[] = new DataRequestDlg.DataRequest[1];
                        req[0] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_EFFICIENCY, "test2", true);

                        new DataRequestDlg(req,owner) {

                            public void dataCollectAction() {
                                EfficiencyDataSet eff[] = new EfficiencyDataSet[mcData.efficiencies.size()];

                                for (int i = 0; i < eff.length; i++) {
                                    eff[i] = mcData.efficiencies.get(i);
                                }
                                Point viewPosition[] = new Point[4];
                                viewPosition[0] = new Point(0, 0);
                                viewPosition[1] = new Point(400, 0);
                                viewPosition[2] = new Point(0, 400);
                                viewPosition[3] = new Point(400, 400);

                                int counter = 0;
                                for (int i = 0; i < eff.length; i++) {
                                    for (int j = i + 1; j < eff.length; j++) {
                                        DottyPlot p = new DottyPlot(eff[i], eff[j]);
                                        JDialog plotWindow = new JDialog(this);
                                        plotWindow.setLayout(new BorderLayout());
                                        plotWindow.add(p.getPanel(),BorderLayout.CENTER);
                                        plotWindow.setVisible(true);
                                        plotWindow.setSize(400, 400);
                                        plotWindow.setLocation(viewPosition[counter % 4]);
                                        counter++;
                                    }
                                }
                            }
                        };
                    }
                });


        NormalizedParameterRangePlot.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        DataRequestDlg.DataRequest req[] = new DataRequestDlg.DataRequest[2];
                        req[0] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_PARAMETER, "test2", true);
                        req[1] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_EFFICIENCY, "test", true);

                        new DataRequestDlg(req,owner) {

                            public void dataCollectAction() {
                                JDialog plotWindow = new JDialog(this);
                                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                                EfficiencyDataSet eff[] = new EfficiencyDataSet[mcData.efficiencies.size()];

                                for (int i = 0; i < eff.length; i++) {
                                    eff[i] = mcData.efficiencies.get(i);
                                }

                                ParameterSet param[] = new ParameterSet[mcData.parameters.size()];

                                for (int i = 0; i < param.length; i++) {
                                    param[i] = mcData.parameters.get(i);
                                }
                                plotWindow.setLayout(new BorderLayout());
                                plotWindow.add((new NormalisedParameterRangePlot(param, eff).getPanel1()),BorderLayout.CENTER);
                                plotWindow.setVisible(true);
                                plotWindow.setSize(800, 400);

                            }
                        };
                    }
                });

        BestPredictionPlot.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        DataRequestDlg.DataRequest req[] = new DataRequestDlg.DataRequest[3];
                        req[0] = new DataRequestDlg.DataRequest(DataRequestDlg.SIMULATATED_TIMESERIE, "test2", false);
                        req[1] = new DataRequestDlg.DataRequest(DataRequestDlg.OBSERVATED_TIMESERIE, "test2", false);
                        req[2] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_EFFICIENCY, "test", true);

                        new DataRequestDlg(req,owner) {

                            public void dataCollectAction() {
                                JDialog plotWindow = new JDialog(this);
                                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                                EfficiencyDataSet eff[] = new EfficiencyDataSet[mcData.efficiencies.size()];

                                for (int i = 0; i < eff.length; i++) {
                                    eff[i] = mcData.efficiencies.get(i);
                                }
                                plotWindow.setLayout(new BorderLayout());
                                plotWindow.add((new BestPredictionPlot(mcData.ts_simulations.get(0), mcData.observations.get(0), eff).getPanel1()),BorderLayout.CENTER);
                                plotWindow.setVisible(true);
                                plotWindow.setSize(800, 400);

                            }
                        };
                    }
                });
        this.add(dottyPlotObjFct);
        this.add(aPosterioParameterDistribution);
        this.add(identifiabilityPlot);
        this.add(DYNIA);
        this.add(RegionalSensitivityAnalysis);
        this.add(RegionalSensitivityAnalysis2);
        this.add(ClassPlot);
        this.add(GLUEVariableUncertainity);
        this.add(GLUEOutputUncertainity);
        this.add(ParetoOutputUncertainity);
        this.add(MultiObjectivePlots);
        this.add(NormalizedParameterRangePlot);
        this.add(BestPredictionPlot);
        this.setVisible(false);
    }
}
