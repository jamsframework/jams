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
import javax.swing.BorderFactory;
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

    enum PlotType{Sensitivity, Uncertainty, Basic};
    
    private class PlotDesc{
        ImageIcon icon;
        String tooltip,title;
        PlotType type;
        Class clazz;

        PlotDesc(PlotType type, ImageIcon icon, String tooltip, String title, Class clazz){
            this.type = type;
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
        

        registeredPlots.add(new PlotDesc(PlotType.Basic,new ImageIcon(getClass().getResource("/jams/explorer/resources/images/dottyplot.png")),
                JAMS.i18n("CREATE_DOTTY_PLOT"),
                JAMS.i18n("DOTTY_PLOT"),
                DottyPlot.class));

        /*registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/dottyplot.png")),
                "DottyPlot3D",
                "DottyPlot3D",
                DottyPlot3D.class));*/
               
        registeredPlots.add(new PlotDesc(PlotType.Sensitivity,new ImageIcon(getClass().getResource("/jams/explorer/resources/images/sensitivity.png")),
                "Sensitivityanalyzer",
                JAMS.i18n("Sensitivity_Analysis"),
                SensitivityToolbox.class));

        registeredPlots.add(new PlotDesc(PlotType.Sensitivity,new ImageIcon(getClass().getResource("/jams/explorer/resources/images/interaction.png")),
                "Interaction Effects Analyzer",
                JAMS.i18n("Interaction_analysis"),
                ParameterInteractionAnalyser.class));
        
        registeredPlots.add(new PlotDesc(PlotType.Sensitivity,new ImageIcon(getClass().getResource("/jams/explorer/resources/images/temporal_sa.png")),
                JAMS.i18n("Temporal_Sensitivity_Analysis"),
                JAMS.i18n("Temporal_Sensitivity_Analysis"),
                TemporalSensitivityAnalysisGUI.class));
        
        registeredPlots.add(new PlotDesc(PlotType.Basic,new ImageIcon(getClass().getResource("/jams/explorer/resources/images/aposterioriplot.png")),
                JAMS.i18n("CREATE_A_POSTERIORI_DISTRIBUTION_PLOT"),
                JAMS.i18n("A_POSTERIO_PARAMETER_DISTRIBUTION"),
                APosterioriPlot.class));

        registeredPlots.add(new PlotDesc(PlotType.Basic,new ImageIcon(getClass().getResource("/jams/explorer/resources/images/identifiabilityplot.png")),
                JAMS.i18n("IDENTIFIABILITY_PLOT"),
                JAMS.i18n("IDENTIFIABILITY_PLOT"),
                IdentifiabilityPlot.class));

        registeredPlots.add(new PlotDesc(PlotType.Basic,new ImageIcon(getClass().getResource("/jams/explorer/resources/images/bestpredictionplot.png")),
                JAMS.i18n("BEST_PREDICTION_PLOT"),
                JAMS.i18n("BEST_PREDICTION_PLOT"),
                BestPredictionPlot.class));

        registeredPlots.add(new PlotDesc(PlotType.Basic,new ImageIcon(getClass().getResource("/jams/explorer/resources/images/classplot.png")),
                JAMS.i18n("CLASS_PLOT"),
                JAMS.i18n("CLASS_PLOT"),
                ClassPlot.class));

        registeredPlots.add(new PlotDesc(PlotType.Basic,new ImageIcon(getClass().getResource("/jams/explorer/resources/images/dyniaplot.png")),
                JAMS.i18n("DYNIA"),
                JAMS.i18n("DYNIA"),
                DYNIA.class));

        registeredPlots.add(new PlotDesc(PlotType.Uncertainty,new ImageIcon(getClass().getResource("/jams/explorer/resources/images/ParetoOutPlot.png")),
                JAMS.i18n("PARETO_OUTPUT_UNCERTAINITY"),
                JAMS.i18n("PARETO_OUTPUT_UNCERTAINITY"),
                ParetoOutputUncertainty.class));

        registeredPlots.add(new PlotDesc(PlotType.Uncertainty,new ImageIcon(getClass().getResource("/jams/explorer/resources/images/GLUEOutPlot.png")),
                JAMS.i18n("GLUE_OUTPUT_UNCERTAINITY"),
                JAMS.i18n("OUTPUT_UNCERTAINTY_PLOT"),
                GLUEOutputUncertainty.class));

        registeredPlots.add(new PlotDesc(PlotType.Uncertainty,new ImageIcon(getClass().getResource("/jams/explorer/resources/images/GLUEVarPlot.png")),
                JAMS.i18n("GLUE_VARIABLE_UNCERTAINITY"),
                JAMS.i18n("CUMULATIVE_DENSITY_PLOT"),
                GLUEVariableUncertainty.class));

        registeredPlots.add(new PlotDesc(PlotType.Uncertainty,new ImageIcon(getClass().getResource("/jams/explorer/resources/images/normalisedparameter.png")),
                JAMS.i18n("NORMALIZED_PARAMETER_RANGE_PLOT"),
                JAMS.i18n("NORMALISED_PARAMETER_RANGE_PLOT"),
                NormalisedParameterRangePlot.class));
        
        registeredPlots.add(new PlotDesc(PlotType.Uncertainty,new ImageIcon(getClass().getResource("/jams/explorer/resources/images/ParetoBoxPlot.png")),
                JAMS.i18n("PARETO_BOX_PLOT"),
                JAMS.i18n("PARETO_BOX_PLOT"),
                ParetoBoxPlot.class));
                
        registeredPlots.add(new PlotDesc(PlotType.Sensitivity,new ImageIcon(getClass().getResource("/jams/explorer/resources/images/regionalsensitivity.png")),
                JAMS.i18n("REGIONAL_SENSITIVITY_ANALYSIS"),
                JAMS.i18n("REGIONAL_SENSITIVITY_ANALYSIS"),
                RegionalSensitivityAnalyser.class));

        registeredPlots.add(new PlotDesc(PlotType.Sensitivity,new ImageIcon(getClass().getResource("/jams/explorer/resources/images/regionalsensitivity2.png")),
                JAMS.i18n("REGIONAL_SENSITIVITY_ANALYSIS_II"),
                JAMS.i18n("REGIONAL_SENSITIVITY_ANALYSIS_II"),
                RegionalSensitivityAnalyser2.class));

        registeredPlots.add(new PlotDesc(PlotType.Basic,new ImageIcon(getClass().getResource("/jams/explorer/resources/images/regionalsensitivity2.png")),
                JAMS.i18n("REGIONAL_SENSITIVITY_ANALYSIS_II"),
                JAMS.i18n("REGIONAL_SENSITIVITY_ANALYSIS_II"),
                MultiObjectiveDecisionSupport.class));
                
        /*registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/regionalsensitivity2.png")),
                "Experimental I",
                "Experimental I",
                optas.SA.APosterioriPlot.class));

        registeredPlots.add(new PlotDesc(new ImageIcon(getClass().getResource("/reg/resources/images/regionalsensitivity2.png")),
                "Experimental II",
                "Experimental II",
                ParameterInterpolation2.class));*/

        JToolBar toolbarBasic = new JToolBar("Basic Tools");
        toolbarBasic.setBorder(BorderFactory.createTitledBorder("Basic-Analysis"));
        
        JToolBar toolbarSenstivity = new JToolBar("Sensitivity Tools");
        toolbarSenstivity.setBorder(BorderFactory.createTitledBorder("Sensitivity-Analysis"));
        
        JToolBar toolbarUncertainty = new JToolBar("Uncertainty Tools");
        toolbarUncertainty.setBorder(BorderFactory.createTitledBorder("Uncertainty-Analysis"));
                        
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
            if (pd.type == PlotType.Sensitivity){
                toolbarSenstivity.add(button);
            }else if (pd.type == PlotType.Basic){
                toolbarBasic.add(button);
            }else if (pd.type == PlotType.Uncertainty){
                toolbarUncertainty.add(button);
            }
        }
        this.add(toolbarBasic);
        this.add(toolbarSenstivity);
        this.add(toolbarUncertainty);
        
        this.owner = param_owner;        
        this.setVisible(true);
        this.setFloatable(false);
    }
}
