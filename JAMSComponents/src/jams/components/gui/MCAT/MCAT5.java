/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.gui.MCAT;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import jams.model.JAMSComponent;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author Christian Fischer
 */
public class MCAT5 extends JAMSComponent{
    
    final int EFFICIENCY_E1 = 0;
    final int EFFICIENCY_E2 = 1;
    final int EFFICIENCY_LOGE1 = 2;
    final int EFFICIENCY_LOGE2 = 3;
    final int EFFICIENCY_IOA1 = 4;
    final int EFFICIENCY_IOA2 = 5;
    final int EFFICIENCY_R2 = 6;
    final int EFFICIENCY_WR2 = 7;
    final int EFFICIENCY_DSGRAD = 8;
    final int EFFICIENCY_ABSVOLERROR = 9;
    final int EFFICIENCY_RMSE = 10;
    final int EFFICIENCY_PBIAS = 11;
          
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
    
    public static class ObservationDataSet{
        public double[] set;
        public String name;
        public int timeLength;
        @Override 
        public String toString(){
            return name + " timesteps:" + timeLength;
        }
    };
            
    public static class ParameterSet{
        public double[] set;
        public String name;
        public MonteCarloData parent = null;
        @Override 
        public String toString(){
            return name;
        }
    };
    
    public static class SimulationTimeSeriesDataSet{
        public SimulationDataSet set[];
        public String name;        
        public int timeLength;        
        public MonteCarloData parent = null;
        @Override 
        public String toString(){
            return name + " timesteps:" + timeLength;
        }
    };
    
    public static class SimulationDataSet{
        public double[] set;
        public String name;
        public MonteCarloData parent = null;
        @Override 
        public String toString(){
            return name;
        }
    };
    
    public static class EfficiencyDataSet{
        public double[] set;                
        public String name;
        public boolean isPositveEff;
        public MonteCarloData parent = null;
        
        public SimulationTimeSeriesDataSet source1 = null;
        public ObservationDataSet source2 = null;
        
        EfficiencyDataSet(String name,double[] set,SimulationTimeSeriesDataSet src1,ObservationDataSet src2, boolean isPositiveEff){
            this.name = name;
            this.set = set;
            this.isPositveEff = isPositiveEff;
            this.source1 = src1;
            this.source2 = src2;
        }
        @Override 
        public String toString(){
            return name;
        }
    };
    
    public static class MonteCarloData{
        int numberOfRuns;
        public String name;
        Vector<ObservationDataSet> observations;
        Vector<SimulationDataSet> simulations;
        Vector<SimulationTimeSeriesDataSet> ts_simulations;
        Vector<ParameterSet> parameters;
        Vector<EfficiencyDataSet>  efficiencies;
        
        public MonteCarloData(int numberOfRuns){
            this.numberOfRuns = numberOfRuns;
            name = "default";
            observations = new Vector<ObservationDataSet>();
            simulations = new Vector<SimulationDataSet>();
            ts_simulations = new Vector<SimulationTimeSeriesDataSet>();
            parameters = new Vector<ParameterSet>();
            efficiencies = new Vector<EfficiencyDataSet>();
        }
        public boolean addObservationDataSet(ObservationDataSet p){
            p.timeLength = p.set.length;
            for (int i=0;i<ts_simulations.size();i++)
                if (ts_simulations.get(i).timeLength != p.timeLength)
                    return false;            
            for (int i=0;i<observations.size();i++)
                if (observations.get(i).timeLength != p.timeLength)
                    return false;            
            observations.add(p);
            return true;
        }
        public Vector<ObservationDataSet> getObservationDataSet(){  return observations;    }
        
        public boolean addSimulationDataSet(SimulationDataSet p){
            if (p.set.length != this.numberOfRuns)
                return false;
            p.parent = this;
            simulations.add(p);
            return true;
        }
        public Vector<SimulationDataSet> getSimulationDataSet(){    return simulations;     }
        
        public boolean addSimulationTimeSeriesDataSet(SimulationTimeSeriesDataSet p){
            p.timeLength = p.set.length;
            for (int i=0;i<ts_simulations.size();i++)
                if (ts_simulations.get(i).timeLength != p.timeLength)
                    return false;            
            for (int i=0;i<observations.size();i++)
                if (observations.get(i).timeLength != p.timeLength)
                    return false;              
            for (int i=0;i<p.timeLength;i++){
                if (p.set[i].set.length != numberOfRuns){
                    return false;
                }
                p.set[i].parent = this;
            }            
            p.parent = this;
            ts_simulations.add(p);
            return true;
        }
        public Vector<SimulationTimeSeriesDataSet> getSimulationTimeSeriesDataSet(){    return ts_simulations;}
        
        public boolean addParameterSet(ParameterSet p){
            if (p.set.length != numberOfRuns){
                return false;
            }
            p.parent = this;
            parameters.add(p);
            return true;
        }
        public Vector<ParameterSet> getParameterSet(){  return parameters;  }
        
        public boolean addEfficiencyDataSet(EfficiencyDataSet p){
            if (p.set.length != numberOfRuns){
                return false;
            }
            p.parent = this;
            efficiencies.add(p);
            return true;
        }
        public Vector<EfficiencyDataSet> getEfficiencyDataSet(){
            return efficiencies;
        }
        @Override 
        public String toString(){
            return "Monte Carlo Simulation\n" + "number of runs:" + numberOfRuns;
        }
        
    }             
    JTree tree;  
    DefaultTreeModel treeModel;
    Vector<MonteCarloData> monteCarloData = new Vector<MonteCarloData>();
                
    JDialog dialog = new JDialog();
            
    Vector<ObservationDataSet> selectedObservations = new Vector<ObservationDataSet>();
    Vector<SimulationDataSet> selectedSimulations = new Vector<SimulationDataSet>();;
    Vector<SimulationTimeSeriesDataSet> selectedTSSimulations = new Vector<SimulationTimeSeriesDataSet>();
    Vector<ParameterSet> selectedParameters = new Vector<ParameterSet>();
    Vector<EfficiencyDataSet>  selectedEfficiencies = new Vector<EfficiencyDataSet>();
    
    JPopupMenu eff_popup = null;    
    JMenuItem calcEffMenu[] = new JMenuItem[12];
    
    public void collectTreeSelection(){
        TreeSelectionModel model = tree.getSelectionModel();
        TreePath[] paths = model.getSelectionPaths();
        selectedObservations.clear();
        selectedSimulations.clear();
        selectedTSSimulations.clear();
        selectedParameters.clear();
        selectedEfficiencies.clear();
        if (paths == null)
            return;
        for (int i=0;i<paths.length;i++){
            DefaultMutableTreeNode node = null;
            if ((paths[i].getLastPathComponent() instanceof DefaultMutableTreeNode)){
                node = (DefaultMutableTreeNode)paths[i].getLastPathComponent();
                if (node.getUserObject() instanceof ObservationDataSet)
                    selectedObservations.add((ObservationDataSet)node.getUserObject());
                if (node.getUserObject() instanceof SimulationDataSet)
                    selectedSimulations.add((SimulationDataSet)node.getUserObject());
                if (node.getUserObject() instanceof SimulationTimeSeriesDataSet)
                    selectedTSSimulations.add((SimulationTimeSeriesDataSet)node.getUserObject());
                if (node.getUserObject() instanceof ParameterSet)
                    selectedParameters.add((ParameterSet)node.getUserObject());
                if (node.getUserObject() instanceof EfficiencyDataSet)
                    selectedEfficiencies.add((EfficiencyDataSet)node.getUserObject());
            }
        }
    }
            
    public void CalculateEfficency(int mode){
        if (this.selectedObservations.size() != 1 || this.selectedTSSimulations.size() != 1){
            JOptionPane.showMessageDialog(null, "You have to choose a output data and a simulation data set to calculate an efficency");
            return;
        }
        SimulationTimeSeriesDataSet simTSData = this.selectedTSSimulations.get(0);
        ObservationDataSet obsData = this.selectedObservations.get(0);
                
        if (simTSData.timeLength != obsData.timeLength){
            JOptionPane.showMessageDialog(null, "Selected timeseries have a different length");
            return;
        }
        
        double eff[] = new double[simTSData.parent.numberOfRuns];
        double simulationArray[] = new double[simTSData.timeLength];
        for (int i=0;i<simTSData.parent.numberOfRuns;i++){            
            for (int j=0;j<simTSData.timeLength;j++){
                simulationArray[j] = simTSData.set[j].set[i];
            }
            switch(mode){
                case EFFICIENCY_E1:  eff[i] = Efficiencies.CalculateE(simulationArray,obsData.set,1);break;
                case EFFICIENCY_E2:  eff[i] = Efficiencies.CalculateE(simulationArray,obsData.set,2);break;
                case EFFICIENCY_LOGE1:   eff[i] = Efficiencies.CalculateE(Efficiencies.ArrayLog(simulationArray),
                                                                          Efficiencies.ArrayLog(obsData.set),1);break;
                case EFFICIENCY_LOGE2:   eff[i] = Efficiencies.CalculateE(Efficiencies.ArrayLog(simulationArray),
                                                                          Efficiencies.ArrayLog(obsData.set),2);break;
                case EFFICIENCY_IOA1: eff[i] = Efficiencies.CalculateIndexOfAgreement(simulationArray, obsData.set, 1); break;
                case EFFICIENCY_IOA2: eff[i] = Efficiencies.CalculateIndexOfAgreement(simulationArray, obsData.set, 2); break;
                case EFFICIENCY_R2: eff[i] = Efficiencies.CalculateR2(simulationArray, obsData.set)[2];
                case EFFICIENCY_WR2: { double rCoeff[] = Efficiencies.CalculateR2(simulationArray, obsData.set);
                    if(rCoeff[1] <= 1)  eff[i] = Math.abs(rCoeff[1]) * rCoeff[2];
                    else                eff[i] = Math.pow(Math.abs(rCoeff[1]), -1.0) * rCoeff[2];
                } 
                case EFFICIENCY_DSGRAD: eff[i] = Efficiencies.CalculateDSGrad(simulationArray, obsData.set);
                case EFFICIENCY_ABSVOLERROR: eff[i] = Efficiencies.CalculateAbsVolError(simulationArray, obsData.set);
                case EFFICIENCY_RMSE: eff[i] = Efficiencies.CalculateRMSE(simulationArray, obsData.set);
                case EFFICIENCY_PBIAS: eff[i] = Efficiencies.CalculatePBIAS(simulationArray, obsData.set);
            }
        }
        MonteCarloData mcData = simTSData.parent;
        EfficiencyDataSet effData = null;        
        switch(mode){
            case EFFICIENCY_E1:effData = new EfficiencyDataSet("e1[" + simTSData.name + ";" + obsData.name + "]",eff,simTSData,obsData,true);break;
            case EFFICIENCY_E2:effData = new EfficiencyDataSet("e2[" + simTSData.name + ";" + obsData.name + "]",eff,simTSData,obsData,true);break;
            case EFFICIENCY_LOGE1:effData = new EfficiencyDataSet("le1[" + simTSData.name + ";" + obsData.name + "]",eff,simTSData,obsData,true);break;
            case EFFICIENCY_LOGE2:effData = new EfficiencyDataSet("le2[" + simTSData.name + ";" + obsData.name + "]",eff,simTSData,obsData,true);break;
            case EFFICIENCY_IOA1:effData = new EfficiencyDataSet("ioa1[" + simTSData.name + ";" + obsData.name + "]",eff,simTSData,obsData,true);break;
            case EFFICIENCY_IOA2:effData = new EfficiencyDataSet("ioa2[" + simTSData.name + ";" + obsData.name + "]",eff,simTSData,obsData,true);break;
            case EFFICIENCY_R2:effData = new EfficiencyDataSet("r2[" + simTSData.name + ";" + obsData.name + "]",eff,simTSData,obsData,true);break;
            case EFFICIENCY_WR2:effData = new EfficiencyDataSet("wr2[" + simTSData.name + ";" + obsData.name + "]",eff,simTSData,obsData,true);break;
            case EFFICIENCY_DSGRAD:effData = new EfficiencyDataSet("dsgrad[" + simTSData.name + ";" + obsData.name + "]",eff,simTSData,obsData,true);break;
            case EFFICIENCY_ABSVOLERROR:effData = new EfficiencyDataSet("absvolerr[" + simTSData.name + ";" + obsData.name + "]",eff,simTSData,obsData,true);break;
            case EFFICIENCY_RMSE:effData = new EfficiencyDataSet("rmse[" + simTSData.name + ";" + obsData.name + "]",eff,simTSData,obsData,true);break;
            case EFFICIENCY_PBIAS:effData = new EfficiencyDataSet("pbias[" + simTSData.name + ";" + obsData.name + "]",eff,simTSData,obsData,true);break;            
        }
        effData.parent = simTSData.parent;
        mcData.efficiencies.add(effData);
        //add efficiency
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
        Enumeration enu = root.preorderEnumeration();    
        while (enu.hasMoreElements()){
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)enu.nextElement();
            if (child.toString().compareTo(mcData.name)==0){
                Enumeration inner_enu = child.depthFirstEnumeration(); 
                while(inner_enu.hasMoreElements()){
                    DefaultMutableTreeNode inner_child = (DefaultMutableTreeNode)inner_enu.nextElement();
                    if (inner_child.toString().compareTo("Efficiency - Sets")==0){                        
                        //build expand path                             
                        treeModel.insertNodeInto(new DefaultMutableTreeNode(effData), inner_child, inner_child.getChildCount());                        
                        tree.scrollPathToVisible(new TreePath( inner_child.getPath()   ));     
                        
                        break;
                    }                    
                }
                break;
            }
        }
        tree.validate();
    }
    
    
    public void updateLists(){
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Monte Carlo Data");
        root.setAllowsChildren(true);        
        for (int i=0;i<this.monteCarloData.size();i++){
            MonteCarloData mcData = monteCarloData.get(i);
            DefaultMutableTreeNode mcDataNode = new DefaultMutableTreeNode(mcData.name);
            DefaultMutableTreeNode ObservationNode = new DefaultMutableTreeNode("Observations - Timeseries");
            DefaultMutableTreeNode SimulationNode = new DefaultMutableTreeNode("Simulations - Sets");
            DefaultMutableTreeNode SimulationTSNode = new DefaultMutableTreeNode("Simulation Timeseries - Sets");
            DefaultMutableTreeNode ParameterNode = new DefaultMutableTreeNode("Parameter - Sets");
            DefaultMutableTreeNode EfficiencyNode = new DefaultMutableTreeNode("Efficiency - Sets");
            for (int j=0;j<mcData.observations.size();j++){
                ObservationDataSet obs = mcData.observations.get(j);
                DefaultMutableTreeNode observation = new DefaultMutableTreeNode(obs.name);
                observation.setUserObject(obs);
                ObservationNode.add(observation);
            }
            for (int j=0;j<mcData.simulations.size();j++){
                SimulationDataSet sim = mcData.simulations.get(j);
                DefaultMutableTreeNode simulation = new DefaultMutableTreeNode(sim.name);
                simulation.setUserObject(sim);
                SimulationNode.add(simulation);
            }
            for (int j=0;j<mcData.ts_simulations.size();j++){
                SimulationTimeSeriesDataSet sim = mcData.ts_simulations.get(j);
                DefaultMutableTreeNode simulation = new DefaultMutableTreeNode(sim.name);
                simulation.setUserObject(sim);
                SimulationTSNode.add(simulation);
            }
            for (int j=0;j<mcData.parameters.size();j++){
                ParameterSet param = mcData.parameters.get(j);
                DefaultMutableTreeNode parameter = new DefaultMutableTreeNode(param.name);
                parameter.setUserObject(param);
                ParameterNode.add(parameter);
            }
            for (int j=0;j<mcData.efficiencies.size();j++){
                EfficiencyDataSet eff = mcData.efficiencies.get(j);
                DefaultMutableTreeNode efficiency = new DefaultMutableTreeNode(eff.name);
                efficiency.setUserObject(eff);
                EfficiencyNode.add(efficiency);
            }
            mcDataNode.add(ObservationNode);
            mcDataNode.add(SimulationNode);
            mcDataNode.add(SimulationTSNode);
            mcDataNode.add(ParameterNode);            
            mcDataNode.add(EfficiencyNode);            
            
            root.add(mcDataNode);
            
        }
       
        treeModel.setRoot(root);
        tree.setModel(treeModel);
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override 
            public void valueChanged(TreeSelectionEvent e) {
                collectTreeSelection();
            }
        });
        dialog.validate();
    }
    
    public void createDialog(){          
        JPanel mainPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(16,1));
        
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
                
        dottyPlotObjFct.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {                                     
                if (selectedParameters.size() != 1 || selectedEfficiencies.size() != 1){
                    JOptionPane.showMessageDialog(null, "Error: select one parameter and one efficency!");
                    return;
                }                         
                if (selectedParameters.get(0).parent != selectedEfficiencies.get(0).parent){
                    JOptionPane.showMessageDialog(null, "Error: different monte carlo data!");
                    return;
                }
                JDialog plotWindow = new JDialog();
                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                plotWindow.add( (new DottyPlot(selectedParameters.get(0),selectedEfficiencies.get(0))).getPanel()) ;    
                plotWindow.setVisible(true);
                plotWindow.setSize(800, 400);
            }
        });
        
        aPosterioParameterDistribution.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {  
                if (selectedParameters.size() != 1 || selectedEfficiencies.size() != 1){
                    JOptionPane.showMessageDialog(null, "Error: select one parameter and one efficency!");
                    return;
                }                         
                if (selectedParameters.get(0).parent != selectedEfficiencies.get(0).parent){
                    JOptionPane.showMessageDialog(null, "Error: different monte carlo data!");
                    return;
                }
                JDialog plotWindow = new JDialog();
                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                plotWindow.add( (new APosterioriPlot(selectedParameters.get(0),selectedEfficiencies.get(0))).getPanel()) ;    
                plotWindow.setVisible(true);
                plotWindow.setSize(800, 400);                
            }
        });        
        identifiabilityPlot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {  
                if (selectedParameters.size() != 1 || selectedEfficiencies.size() != 1){
                    JOptionPane.showMessageDialog(null, "Error: select one parameter and one efficency!");
                    return;
                }                         
                if (selectedParameters.get(0).parent != selectedEfficiencies.get(0).parent){
                    JOptionPane.showMessageDialog(null, "Error: different monte carlo data!");
                    return;
                }
                JDialog plotWindow = new JDialog();
                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                plotWindow.add( (new IdentifiabilityPlot(selectedParameters.get(0),selectedEfficiencies.get(0))).getPanel()) ;    
                plotWindow.setVisible(true);
                plotWindow.setSize(800, 400);                                               
            }
        });
        
        DYNIA.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) { 
                if (selectedParameters.size() != 1 || selectedObservations.size() != 1 || selectedTSSimulations.size() != 1){
                    JOptionPane.showMessageDialog(null, "Error: select one parameter, one observation and one time series simulation!");
                    return;
                }                         
                if (selectedParameters.get(0).parent != selectedTSSimulations.get(0).parent) {
                    JOptionPane.showMessageDialog(null, "Error: different monte carlo data!");
                    return;
                }
                JDialog plotWindow = new JDialog();
                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                plotWindow.add( (new DYNIA(   selectedTSSimulations.get(0),
                                              selectedParameters.get(0),
                                              selectedObservations.get(0))).getPanel()) ;    
                plotWindow.setVisible(true);
                plotWindow.setSize(800, 400);                                                                  
            }
        });
        
        RegionalSensitivityAnalysis.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {   
                if (selectedParameters.size() != 1 || selectedEfficiencies.size() != 1 ){
                    JOptionPane.showMessageDialog(null, "Error: select one parameter and one efficiency");
                    return;
                }                         
                if (selectedParameters.get(0).parent != selectedEfficiencies.get(0).parent) {
                    JOptionPane.showMessageDialog(null, "Error: different monte carlo data!");
                    return;
                }
                JDialog plotWindow = new JDialog();
                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                plotWindow.add( (new RegionalSensitivityAnalyser(selectedParameters.get(0),selectedEfficiencies.get(0))).getPanel()) ;    
                plotWindow.setVisible(true);
                plotWindow.setSize(800, 400);                                 
            }
        });
        
        RegionalSensitivityAnalysis2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {   
                if (selectedParameters.size() != 1 || selectedEfficiencies.size() < 1 ){
                    JOptionPane.showMessageDialog(null, "Error: select one parameter and at least an efficiency");
                    return;
                }                         
                for (int i=0;i<selectedEfficiencies.size();i++){
                    if (selectedParameters.get(0).parent != selectedEfficiencies.get(i).parent) {
                        JOptionPane.showMessageDialog(null, "Error: different monte carlo data!");
                        return;
                    }
                }
                JDialog plotWindow = new JDialog();
                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                EfficiencyDataSet effArray[] = new EfficiencyDataSet[selectedEfficiencies.size()];
                for (int i=0;i<effArray.length;i++)
                    effArray[i] = selectedEfficiencies.get(i);
                plotWindow.add( (new RegionalSensitivityAnalyser2(selectedParameters.get(0),effArray)).getPanel()) ;    
                plotWindow.setVisible(true);
                plotWindow.setSize(800, 400);                                                
            }
        });
        
        ClassPlot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {   
                if (selectedTSSimulations.size() != 1 || selectedEfficiencies.size() != 1 ){
                    JOptionPane.showMessageDialog(null, "Error: select one simulation and an efficiency");
                    return;
                }                         
                if (selectedTSSimulations.get(0).parent != selectedEfficiencies.get(0).parent) {
                    JOptionPane.showMessageDialog(null, "Error: different monte carlo data!");
                    return;
                }
                JDialog plotWindow = new JDialog();
                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);                
                plotWindow.add( (new ClassPlot(selectedTSSimulations.get(0),selectedEfficiencies.get(0))).getPanel()) ;    
                plotWindow.setVisible(true);
                plotWindow.setSize(800, 400);                                             
            }
        });
        
        GLUEVariableUncertainity.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {   
                if (selectedSimulations.size() != 1 || selectedEfficiencies.size() != 1 ){
                    JOptionPane.showMessageDialog(null, "Error: select one simulation and an efficiency");
                    return;
                }                         
                if (selectedSimulations.get(0).parent != selectedEfficiencies.get(0).parent) {
                    JOptionPane.showMessageDialog(null, "Error: different monte carlo data!");
                    return;
                }                                
                GLUEVariableUncertainty plot = new GLUEVariableUncertainty(selectedSimulations.get(0),selectedEfficiencies.get(0));
                JDialog plotWindow = new JDialog();                
                plotWindow.add(plot.getPanel1());    
                plotWindow.setVisible(true);
                plotWindow.setSize(800, 400);  
                plotWindow.setLocation(0, 0);
                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);    
                JDialog plotWindow2 = new JDialog();                
                plotWindow2.add(plot.getPanel2());    
                plotWindow2.setVisible(true);
                plotWindow2.setSize(800, 200); 
                plotWindow2.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);    
                plotWindow2.setLocation(0, 400);
            }
        });

        GLUEOutputUncertainity.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {  
                if (selectedTSSimulations.size() != 1 || selectedObservations.size() != 1 ){
                    JOptionPane.showMessageDialog(null, "Error: select one simulation and an efficiency");
                    return;
                }          
                if (selectedTSSimulations.get(0).timeLength != selectedObservations.get(0).timeLength){
                    JOptionPane.showMessageDialog(null, "Error: different length of simulation and observation timeserie");
                    return;
                }
                
                GLUEOutputUncertainty plot = new GLUEOutputUncertainty(selectedTSSimulations.get(0),selectedObservations.get(0));
                JDialog plotWindow = new JDialog();                
                plotWindow.add(plot.getPanel1());    
                plotWindow.setVisible(true);
                plotWindow.setSize(800, 400);  
                plotWindow.setLocation(0, 0);
                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);    
                JDialog plotWindow2 = new JDialog();                
                plotWindow2.add(plot.getPanel2());    
                plotWindow2.setVisible(true);
                plotWindow2.setSize(800, 200); 
                plotWindow2.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);    
                plotWindow2.setLocation(0, 400);                                
            }
        });
                                                          
        ParetoOutputUncertainity.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) { 
                if (selectedTSSimulations.size() != 1 || selectedObservations.size() != 1 || selectedEfficiencies.size() < 1 ){
                    JOptionPane.showMessageDialog(null, "Error: select one timeseries simulation, one observation and at least an efficiency");
                    return;
                }                         
                if (selectedTSSimulations.get(0).timeLength != selectedObservations.get(0).timeLength ){
                    JOptionPane.showMessageDialog(null, "Error: different length of simulation and observation timeserie");
                    return;
                }
                for (int i=0;i<selectedEfficiencies.size();i++){
                    if (selectedTSSimulations.get(0).parent != selectedEfficiencies.get(0).parent) {
                        JOptionPane.showMessageDialog(null, "Error: different monte carlo data!");
                        return;
                    }
                }
                EfficiencyDataSet eff[] = new EfficiencyDataSet[selectedEfficiencies.size()];
                for (int i=0;i<eff.length;i++){
                    eff[i] = selectedEfficiencies.get(i);
                }
                JDialog plotWindow = new JDialog();
                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);                
                plotWindow.add( (new ParetoOutputUncertainty(
                        selectedTSSimulations.get(0),
                        selectedObservations.get(0),
                        eff)).getPanel1());    
                plotWindow.setVisible(true);
                plotWindow.setSize(800, 400);                                                                              
            }
        });
     
        MultiObjectivePlots.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {                     
                if (selectedEfficiencies.size() < 2){
                    JOptionPane.showMessageDialog(null, "Error: select at least two efficiencies!");
                    return;
                }                
                EfficiencyDataSet eff[] = new EfficiencyDataSet[selectedEfficiencies.size()];
                for (int i=0;i<eff.length;i++)
                    eff[i] = (EfficiencyDataSet)selectedEfficiencies.get(i);
                
                Point viewPosition[] = new Point[4];
                viewPosition[0] = new Point(0,0);
                viewPosition[1] = new Point(400,0);
                viewPosition[2] = new Point(0,400);
                viewPosition[3] = new Point(400,400);
                                
                int counter = 0;
                for (int i=0;i<eff.length;i++){
                    for (int j=i+1;j<eff.length;j++){
                        DottyPlot p = new DottyPlot(eff[i],eff[j]);                        
                        JDialog plotWindow = new JDialog();
                        plotWindow.add(p.getPanel());    
                        plotWindow.setVisible(true);
                        plotWindow.setSize(400, 400);                                
                        plotWindow.setLocation(viewPosition[counter%4]);
                        counter++;
                    }
                }                                                
            }
        });
        
        NormalizedParameterRangePlot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {    
                if (selectedParameters.size() < 1 || selectedEfficiencies.size() < 1){
                    JOptionPane.showMessageDialog(null, "Error: select one parameter and one efficency!");
                    return;
                }         
                
                ParameterSet paramSet[] = new ParameterSet[selectedParameters.size()];
                EfficiencyDataSet effSet[] = new EfficiencyDataSet[selectedEfficiencies.size()];
                
                for (int i=0;i<selectedParameters.size();i++){
                    paramSet[i] = selectedParameters.get(i);
                    for (int j=0;j<selectedEfficiencies.size();j++){
                        effSet[j] = selectedEfficiencies.get(j);
                        if (selectedParameters.get(i).parent != selectedEfficiencies.get(j).parent){
                            JOptionPane.showMessageDialog(null, "Error: different monte carlo data!");
                            return;
                        }
                    }                    
                }
                JDialog plotWindow = new JDialog();
                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                plotWindow.add( (new NormalisedParameterRangePlot(paramSet,effSet)).getPanel1()) ;    
                plotWindow.setVisible(true);
                plotWindow.setSize(800, 400);                
            }
        });

        BestPredictionPlot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {       
                if (selectedTSSimulations.size() != 1 || selectedEfficiencies.size() < 1 || selectedObservations.size() != 1){
                    JOptionPane.showMessageDialog(null, "Error: select one simulation, observation and an efficiency");
                    return;
                }                  
                for (int i=0;i<selectedEfficiencies.size();i++){
                    if (selectedTSSimulations.get(0).parent != selectedEfficiencies.get(i).parent) {
                        JOptionPane.showMessageDialog(null, "Error: different monte carlo data!");
                        return;
                    }
                }
                
                EfficiencyDataSet eff[] = new EfficiencyDataSet[selectedEfficiencies.size()];
                for (int i=0;i<eff.length;i++){
                    eff[i] = selectedEfficiencies.get(i);
                }
                
                JDialog plotWindow = new JDialog();
                plotWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);                
                plotWindow.add( (new BestPredictionPlot(selectedTSSimulations.get(0),selectedObservations.get(0),eff)).getPanel1()) ;    
                plotWindow.setVisible(true);
                plotWindow.setSize(800, 400);                                                                                 
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
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel,BoxLayout.Y_AXIS));
        JLabel ParameterLabel = new JLabel("MC Parameter Sets");
        infoPanel.add(ParameterLabel);
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Monte Carlo Data");
        treeModel = new DefaultTreeModel(root);
        tree = new JTree(treeModel);        
        tree.setEditable(true);
        tree.addMouseListener(new MouseListener(){
            @Override public void mouseExited(MouseEvent e){}
            @Override public void mouseEntered(MouseEvent e){}
            @Override public void mouseReleased(MouseEvent e){   
                if ((e.getButton() != MouseEvent.BUTTON3))
                    return;
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getClosestPathForLocation(e.getX(),e.getY() ).getLastPathComponent();                               
                tree.addSelectionPath(new TreePath(node.getPath()));
                if (node.toString().compareTo("Efficiency - Sets")==0){
                    if (selectedObservations.size()==1 && selectedTSSimulations.size()==1){
                        for (int i=0;i<calcEffMenu.length;i++){
                            calcEffMenu[i].setEnabled(true);
                        }
                    }else{
                        for (int i=0;i<calcEffMenu.length;i++){
                            calcEffMenu[i].setEnabled(false);
                        }
                    }
                    eff_popup.show(tree, e.getX(), e.getY());
                }                                                
            }
            @Override public void mousePressed(MouseEvent e){}
            @Override public void mouseClicked(MouseEvent e){}
        });
                     
        eff_popup = new JPopupMenu();
        for (int i=0;i<calcEffMenu.length;i++){
            switch(i){
                case EFFICIENCY_E1:calcEffMenu[i] = new JMenuItem("Calculate Nash-Sutcliffe(E1)"); break;
                case EFFICIENCY_E2:calcEffMenu[i] = new JMenuItem("Calculate Nash-Sutcliffe(E2)"); break;
                case EFFICIENCY_LOGE1:calcEffMenu[i] = new JMenuItem("Calculate log Nash-Sutcliffe(E1)"); break;
                case EFFICIENCY_LOGE2:calcEffMenu[i] = new JMenuItem("Calculate log Nash-Sutcliffe(E2)"); break;
                case EFFICIENCY_IOA1:calcEffMenu[i] = new JMenuItem("Calculate Index of Agreement (IOA1)"); break;
                case EFFICIENCY_IOA2:calcEffMenu[i] = new JMenuItem("Calculate Index of Agreement (IOA2)"); break;
                case EFFICIENCY_R2:calcEffMenu[i] = new JMenuItem("Calculate Regression Coefficient R²"); break;
                case EFFICIENCY_WR2:calcEffMenu[i] = new JMenuItem("Calculate WR2"); break;
                case EFFICIENCY_DSGRAD:calcEffMenu[i] = new JMenuItem("Calculate dsGrad"); break;
                case EFFICIENCY_ABSVOLERROR:calcEffMenu[i] = new JMenuItem("Calculate absolute volume error (absVolErr)"); break;
                case EFFICIENCY_RMSE:calcEffMenu[i] = new JMenuItem("Calculate root mean square error(RMSE)"); break;
                case EFFICIENCY_PBIAS:calcEffMenu[i] = new JMenuItem("Calculate pbias"); break;
            }
            calcEffMenu[i].addActionListener(new ActionListener(){
                @Override public void actionPerformed(ActionEvent e){
                    for (int i=0;i<calcEffMenu.length;i++){
                        if (e.getSource().equals(calcEffMenu[i])){
                            CalculateEfficency(i);
                            break;
                        }
                    }
                    
                }
            });
            eff_popup.add(calcEffMenu[i]);
        }
                                
        JScrollPane listScroller = new JScrollPane(tree);
        listScroller.setPreferredSize(new Dimension(250,400));
        listScroller.setVisible(true);
        infoPanel.add(listScroller);
                                       
        mainPanel.setLayout(new GridBagLayout());        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.30;
        c.gridx = 0;
        c.gridy = 0;
        mainPanel.add(buttonPanel,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.1;
        c.gridx = 1;
        c.gridy = 0;                
        mainPanel.add(new JPanel(),c);  
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 0;                
        mainPanel.add(infoPanel,c);  
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.1;
        c.gridx = 3;
        c.gridy = 0;                
        mainPanel.add(new JPanel(),c);  
        
        
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem open = new JMenuItem("Read Parameter set");
        JMenuItem exit = new JMenuItem("exit");
        menu.add(open);
        menu.add(exit);
        menuBar.add(menu);
        
        dialog.add(mainPanel);
        dialog.setJMenuBar(menuBar);
        dialog.setSize(new Dimension(600,500));
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);    
        
        updateLists();
    }
        
    public static void main(String arg[]){
        MCAT5 mcat5 = new MCAT5();
        Random gen = new Random();
   
        MonteCarloData sample = new MonteCarloData(2000);        
        mcat5.monteCarloData.add(sample);
                        
        int numOfParam = 5;        
        int numOfObs   = 1;
        int numofSimTS = 1;
        int numofSim   = 2;
        int time = 1000;
        ParameterSet sets[] = new ParameterSet[numOfParam];
        ObservationDataSet outputSets[] = new ObservationDataSet[numOfObs];
        SimulationTimeSeriesDataSet simulationSets[] = new SimulationTimeSeriesDataSet[numofSimTS];
        SimulationDataSet simSets[] = new SimulationDataSet[numofSim];
        
        for (int i=0;i<numOfParam;i++){
            sets[i] = new ParameterSet();            
            sets[i].name = "parameter" + i;
            sets[i].set = new double[sample.numberOfRuns];
            for (int j=0;j<sample.numberOfRuns;j++){
                sets[i].set[j] = gen.nextDouble()*10.0;                
            }           
            sample.addParameterSet(sets[i]);
        }
                                
        for (int i=0;i<numOfObs;i++){
            outputSets[i] = new ObservationDataSet();                        
            outputSets[i].timeLength = time;            
            outputSets[i].set = new double[outputSets[i].timeLength];                            
            outputSets[i].name = "Discharge" + i;                  
            for (int k=0;k<outputSets[i].timeLength;k++){
                if (k > 0)
                    outputSets[i].set[k] = outputSets[i].set[k-1] + gen.nextDouble()-0.5;
                else
                    outputSets[i].set[k] = 1.0;
            }
            sample.addObservationDataSet(outputSets[i]);
        }
        
        for (int i=0;i<numofSimTS;i++){
            simulationSets[i] = new SimulationTimeSeriesDataSet();                        
            simulationSets[i].timeLength = time;
            simulationSets[i].set = new SimulationDataSet[time];                                    
            simulationSets[i].name = "MC Simulation TS";            
            
            for (int j=0;j<time;j++){
                simulationSets[i].set[j] = new SimulationDataSet();
                simulationSets[i].set[j].set = new double[sample.numberOfRuns];
                simulationSets[i].set[j].name = simulationSets[i].name + " time:" + j;                
                for (int k=0;k<sample.numberOfRuns;k++){
                    simulationSets[i].set[j].set[k] = outputSets[0].set[j]+(Math.sin((double)(j)/50.0)+sets[0].set[k])*sets[1].set[k]*(gen.nextDouble()-0.5);
                }
            }
            sample.addSimulationTimeSeriesDataSet(simulationSets[i]);
        }
                        
        for (int i=0;i<numofSim;i++){
            simSets[i] = new SimulationDataSet();            
            simSets[i].name = "virtual" + i;
            simSets[i].set = new double[sample.numberOfRuns];
            for (int j=0;j<sample.numberOfRuns;j++){
                simSets[i].set[j] = sets[0].set[j];//+gen.nextDouble();
            }
            sample.addSimulationDataSet(simSets[i]);
        }
        mcat5.createDialog();        
    }
}
