/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package juice.optimizer.wizard;

import jams.JAMSProperties;
import jams.model.JAMSComponent;
import juice.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.w3c.dom.Document;

/**
 *
 * @author Christian Fischer
 *  //TODO: -> HRU Reducer
 *          -> optimizer position
 *          -> time split
 *          -> more than one parameter/eff with same attr name
 *          -> workspace adaption
 *          -> library determination (property file?)
 */
public class OptimizationWizard {
        
    public static class ComponentWrapper{
        public JAMSComponent content;
        public ComponentWrapper(JAMSComponent content){
            this.content = content;
        }
        public String toString(){
            return content.getInstanceName();
        }
    }
    
    public static class Parameter{
        public String name;
        public JAMSComponent component;
        public double lowerBound;
        public double upperBound;
    }
    
    public static class Efficiency{
        public String name;
        public JAMSComponent component;
        public int mode;
    }
    
    public static class ModelData{
        public Document modelDoc;
        public JAMSProperties properties;
    }
    
    final ModelData myData = new ModelData();
    
    stepPane steps[] = new stepPane[8];
                                                                                  
    public void runWizard(Document modelDoc,JAMSProperties properties) {
        final JDialog wizardDlg = new JDialog();
        final JTabbedPane stepPane = new JTabbedPane();
        stepPane.setEnabled(false);
        this.myData.modelDoc = modelDoc;
        this.myData.properties = properties;
        
        //steps[0] = new step1Pane();
        steps[0] = new step2Pane();
        steps[1] = new step3Pane();
        steps[2] = new step4Pane();
        steps[3] = new step5Pane();
        steps[4] = new step6Pane();
        steps[5] = new step7Pane();
        steps[6] = new step8Pane();
        
        //stepPane.addTab("Step 1", null, steps[0].build(), "load model file");
        stepPane.addTab("Step 1", null, steps[0].build(), JUICE.resources.getString("select_parameter"));
        stepPane.addTab("Step 2", null, steps[1].build(), JUICE.resources.getString("specify_feasible_area"));
        stepPane.addTab("Step 3", null, steps[2].build(), JUICE.resources.getString("select_efficiencies"));
        stepPane.addTab("Step 4", null, steps[3].build(), JUICE.resources.getString("specify_modes"));
        stepPane.addTab("Step 5", null, steps[4].build(), JUICE.resources.getString("optimizer_selection"));
        stepPane.addTab("Step 6", null, steps[5].build(), JUICE.resources.getString("output_path"));
        stepPane.addTab("Step 7", null, steps[6].build(), JUICE.resources.getString("finish"));
        
        wizardDlg.setLayout(new BorderLayout());
        wizardDlg.add(stepPane, BorderLayout.CENTER);

        final JButton nextStep = new JButton(JUICE.resources.getString("next"));
        final JButton back = new JButton(JUICE.resources.getString("back"));
        
        ((step2Pane)steps[0]).loadedModel = myData.modelDoc;
        ((step2Pane)steps[0]).properties = myData.properties;
        String error = steps[0].init();
        if (error != null){
            JOptionPane.showMessageDialog(wizardDlg, error); 
            return;
        }
        
        nextStep.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = stepPane.getSelectedIndex();
                String finError = steps[index].finish();
                if (finError != null)   {  
                    JOptionPane.showMessageDialog((Component) e.getSource(), finError); 
                    return;
                }         
                back.setEnabled(true);
                index++;
                switch(index){                    
                    case 1:{
                        ((step3Pane)steps[1]).setSelectedParameters(((step2Pane)steps[0]).getSelection());                    
                        break;
                    }
                    case 2:{
                        ((step4Pane)steps[2]).setModel(((step2Pane)steps[0]).getModel());                    
                        break;
                    }
                    case 3:{
                        ((step5Pane)steps[3]).setSelectedEfficiencies(((step4Pane)steps[2]).getSelectedEfficiencies());                    
                        break;
                    }
                    case 4:{
                        ((step6Pane)steps[4]).setEfficiencyInformation(((step4Pane)steps[2]).getSelectedEfficiencies());                    
                        ((step6Pane)steps[4]).setParameterInformation(((step2Pane)steps[0]).getSelection());                         
                        break;
                    }
                    case 5:{
                        ((step7Pane)steps[5]).setModel(  myData.modelDoc,
                                                        ((step2Pane)steps[0]).getModel() );   
                        ((step7Pane)steps[5]).setModelOptimizationProperties( 
                                                        ((step6Pane)steps[4]).getOptionState_RemoveGUIComponents(),
                                                        ((step6Pane)steps[4]).getOptionState_RemoveNotUsedComponents(),
                                                        ((step6Pane)steps[4]).getOptionState_modelStructureOptimization());    
                                                        
                        ((step7Pane)steps[5]).setOptimizerDescription( ((step6Pane)steps[4]).getOptimizerDescription() );  
                        ((step7Pane)steps[5]).setDialog(wizardDlg);                        
                        break;
                    }
                    case 6:{
                       ((step8Pane)steps[6]).setOutputProperties( ((step7Pane)steps[5]).getModifiedDocument(),
                                                        ((step7Pane)steps[5]).getOutputPath() );                       
                    }
                }                
                if (index < 7){
                    String initError = steps[index].init();
                    if (initError != null)  {  
                        JOptionPane.showMessageDialog((Component) e.getSource(), initError); 
                        return;
                    }else{
                        stepPane.setSelectedIndex(index);                
                        if (index == 6)
                            nextStep.setEnabled(false);
                    }
                }
            }
        });


        JButton exit = new JButton(JUICE.resources.getString("Exit"));
        exit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                wizardDlg.setVisible(false);
                wizardDlg.dispose();
            }
        });        
        back.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = stepPane.getSelectedIndex();
                nextStep.setEnabled(true);
                if (index > 0){                    
                    stepPane.setSelectedIndex(--index);                     
                    if (index == 0)
                        back.setEnabled(false);
                }
                    
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(back);
        buttonPanel.add(exit);
        buttonPanel.add(nextStep);

        wizardDlg.add(buttonPanel, BorderLayout.SOUTH);

        wizardDlg.setPreferredSize(new Dimension(800, 400));
        wizardDlg.setMinimumSize(new Dimension(800, 400));
        wizardDlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        wizardDlg.setVisible(true);
    }

    public static void main(String arg[]) {
        OptimizationWizard wizard = new OptimizationWizard();
        //wizard.runWizard();
    }
}
