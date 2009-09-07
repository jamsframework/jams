/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import reg.gui.MCAT5.DottyPlot;

/**
 *
 * @author Christian Fischer
 */
public class MCAT5Dialog extends JDialog {

    public static class ObservationDataSet {

        public double[] set;
        public String name;
        public int timeLength;

        @Override
        public String toString() {
            return name + " timesteps:" + timeLength;
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
            return name + " timesteps:" + timeLength;
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
            name = "default";
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
                    return "There are timeseries with different length, observation was not added";
                }
            }
            for (int i = 0; i < observations.size(); i++) {
                if (observations.get(i).timeLength != p.timeLength) {                    
                    return "There are timeseries with different length, observation was not added";
                }
            }
            observations.add(p);
            return null;
        }

        public Vector<ObservationDataSet> getObservationDataSet() {
            return observations;
        }

        public String addSimulationDataSet(SimulationDataSet p) {            
            if (this.numberOfRuns == -1)
                this.numberOfRuns = p.set.length;
            
            if (p.set.length != this.numberOfRuns) {      
                return "There are series with different length, simulated data set was not added";
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
            if (this.numberOfRuns == -1)
                if (p.set.length!=0)
                    this.numberOfRuns = p.set[0].set.length;
            for (int i = 0; i < ts_simulations.size(); i++) {
                if (ts_simulations.get(i).timeLength != p.timeLength) {
                    return "There are timeseries with different length, simulation time series were not added";
                }
            }
            for (int i = 0; i < observations.size(); i++) {
                if (observations.get(i).timeLength != p.timeLength) {
                    return "There are timeseries with different length, simulation time series were not added";
                }
            }            
            for (int i = 0; i < p.timeLength; i++) {
                if (p.set[i].set.length != numberOfRuns) {                  
                    return "There are timeseries with different length, simulation time series were not added";
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
            if (this.numberOfRuns == -1)
                this.numberOfRuns = p.set.length;
            
            if (p.set.length != numberOfRuns) {
                return "Number of parameter sets does not fit number of monto carlo runs";
            }
            p.parent = this;
            parameters.add(p);
            return null;
        }

        public Vector<ParameterSet> getParameterSet() {
            return parameters;
        }

        public String addEfficiencyDataSet(EfficiencyDataSet p) {
            if (this.numberOfRuns == -1)
                this.numberOfRuns = p.set.length;
            if (p.set.length != numberOfRuns) {
                return "number of model runs does not match";
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
            return "Monte Carlo Simulation";
        }
    }
    
    JButton dottyPlotObjFct = new JButton("Dotty Plot");
    JButton aPosterioParameterDistribution = new JButton("A Posterio Parameter Distribution");
    JButton identifiabilityPlot = new JButton("Identifiability Plot");
    JButton DYNIA = new JButton("DYNIA");
    JButton RegionalSensitivityAnalysis = new JButton("Regional Sensitivity Analysis");
    JButton RegionalSensitivityAnalysis2 = new JButton("Regional Sensitivity Analysis 2");
    JButton ClassPlot = new JButton("Class Plot");
    JButton GLUEVariableUncertainity = new JButton("GLUE Variable Uncertainity");
    JButton GLUEOutputUncertainity = new JButton("GLUE Output Uncertainity");
    JButton ParetoOutputUncertainity = new JButton("Pareto Output Uncertainity");
    JButton MultiObjectivePlots = new JButton("MultiObjective Plots");
    JButton NormalizedParameterRangePlot = new JButton("Normalized Parameter Range Plot");
    JButton BestPredictionPlot = new JButton("Best Prediction Plot");
    JButton ParameterView = new JButton("Parameter View");

    public MCAT5Dialog() {
        super();
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(14, 1));

        dottyPlotObjFct.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                DataRequestDlg.DataRequest req[] = new DataRequestDlg.DataRequest[2];
                req[0] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_EFFICIENCY,"test",true); 
                req[1] = new DataRequestDlg.DataRequest(DataRequestDlg.ENSEMBLE_PARAMETER,"test2",true); 
                
                new DataRequestDlg(req){
                    public void dataCollectAction(){
                        JDialog plotWindow = new JDialog();
                        plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                        //plotWindow.add((new DottyPlot(selectedParameters.get(0), selectedEfficiencies.get(0))).getPanel());
                        plotWindow.setVisible(true);
                        plotWindow.setSize(800, 400);
                    }
                };
            }
        });

        aPosterioParameterDistribution.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                    
                    }
                });
        identifiabilityPlot.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        
                    }
                });

        DYNIA.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        
                    }
                });

        RegionalSensitivityAnalysis.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        
                    }
                });

        RegionalSensitivityAnalysis2.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        
                    }
                });

        ClassPlot.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        
                    }
                });

        GLUEVariableUncertainity.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        
                    }
                });

        GLUEOutputUncertainity.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        
                    }
                });

        ParetoOutputUncertainity.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        
                    }
                });

        MultiObjectivePlots.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                    } 
                });

        NormalizedParameterRangePlot.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                    }
                });

        BestPredictionPlot.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        
                    }
                });

        buttonPanel.add(dottyPlotObjFct);

        buttonPanel.add(aPosterioParameterDistribution);

        buttonPanel.add(identifiabilityPlot);

        buttonPanel.add(DYNIA);

        buttonPanel.add(RegionalSensitivityAnalysis);

        buttonPanel.add(RegionalSensitivityAnalysis2);

        buttonPanel.add(ClassPlot);

        buttonPanel.add(GLUEVariableUncertainity);

        buttonPanel.add(GLUEOutputUncertainity);

        buttonPanel.add(ParetoOutputUncertainity);

        buttonPanel.add(MultiObjectivePlots);

        buttonPanel.add(NormalizedParameterRangePlot);

        buttonPanel.add(BestPredictionPlot);
        buttonPanel.add(ParameterView);
        
                
        buttonPanel.invalidate();
        buttonPanel.validate();
        
        this.add(buttonPanel);
        this.setMinimumSize(new Dimension(150,400));
        this.setResizable(false);
        
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);     
        
        this.validate();
    }
}
