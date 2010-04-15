/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jamsui.juice.optimizer.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jams.JAMS;
import jams.data.Attribute;
import jamsui.juice.optimizer.wizard.Tools.Efficiency;
import jamsui.juice.optimizer.wizard.Tools.Parameter;

/**
 *
 * @author Christian Fischer
 */
public class step6Pane extends stepPane{
    String possibleOptimizer[] = {  JAMS.resources.getString("SCE"), 
                                    JAMS.resources.getString("Nelder_Mead"), 
                                    JAMS.resources.getString("MOCOM"), 
                                    JAMS.resources.getString("Branch_and_Bound"), 
                                    JAMS.resources.getString("Gutmann"), 
                                    JAMS.resources.getString("Gaussian_Process_Optimizer"),
                                    JAMS.resources.getString("Random_Sampler"),
                                    JAMS.resources.getString("Parallel_Random_Sampler"),
                                    JAMS.resources.getString("Parallel_SCE")};
    boolean multiObjective[] = {    false,
                                    false,
                                    true,
                                    false,
                                    false,
                                    false,
                                    true,
                                    true,
                                    false};
    
    static public class AttributeDescription{
        String name;
        String value;
        String context;
        boolean isAttribute;
        
        AttributeDescription(String name,String context,String value,boolean isAttribute){
            this.name = name;
            this.value = value;
            this.isAttribute = isAttribute;
            this.context = context;
        }
    }
    
    static public class OptimizerDescription{
        ArrayList<Parameter> parameters;
        ArrayList<Efficiency> efficiencies;
        String optimizerClassName;
        ArrayList<AttributeDescription> attributes;
    }
    
    JPanel optimizerOptions[] = new JPanel[9];
    
    JCheckBox removeNotUsedComponents = new JCheckBox(JAMS.resources.getString("remove_unused_components"));
    JCheckBox removeGUIComponents = new JCheckBox(JAMS.resources.getString("remove_GUI_components"));
    JCheckBox modelStructureOptimization = new JCheckBox(JAMS.resources.getString("optimize_model_structure"));
            
    ArrayList<Parameter> param_info = null;
    ArrayList<Efficiency> eff_info = null;
    final Attribute.Boolean isMultiObjective = jams.data.JAMSDataFactory.createBoolean();
    final JComboBox optimizer = new JComboBox(possibleOptimizer);
    final JPanel optimizerPanelWrapper = new JPanel();
    
    OptimizerDescription desc = null;
    
    JTextField SCE_numberOfComplexes = new JTextField("2",6);
    JTextField SCE_pcento = new JTextField("0.1",6);
    JTextField SCE_peps = new JTextField("0.00001",6);
    JTextField SCE_kstop = new JTextField("10",6);
    JTextField SCE_maximumNumberOfIterations = new JTextField("500",6);
        
    JTextField nelderMead_maximumNumberOfIterations = new JTextField("500",6);
    JTextField MOCOM_maximumNumberOfIterations = new JTextField("500",6);
    JTextField MOCOM_populationSize = new JTextField("100",6);
    JTextField BranchAndBound_maximumNumberOfIterations = new JTextField("500",6);
    JTextField RandomSampler_maximumNumberOfIterations = new JTextField("500",6);
    JTextField ParallelRandomSearch_maximumNumberOfIterations = new JTextField("500",6);
    JTextField ParallelRandomSearch_excludedFiles = new JTextField("(.*\\.cache)|(.*\\.jam)|(.*\\.ser)|(.*\\.svn)|(.*output)",6);
    
    JTextField ParallelSCE_maximumNumberOfIterations = new JTextField("500",6);
    JTextField ParallelSCE_numberOfComplexes = new JTextField("2",6);
    JTextField ParallelSCE_pcento = new JTextField("0.1",6);
    JTextField ParallelSCE_peps = new JTextField("0.00001",6);
    JTextField ParallelSCE_kstop = new JTextField("10",6);
    JTextField ParallelSCE_excludedFiles = new JTextField("(.*\\.cache)|(.*\\.jam)|(.*\\.ser)|(.*\\.svn)|(.*output)",6);
        
    JTextField Gutmann_maximumNumberOfIterations = new JTextField("500",6);
    JTextField GPSearch_maximumNumberOfIterations = new JTextField("500",6);
    String methods[] = {"Exponential","Matérn","RQ","Neural Network","Exponential(no noise)","Neural Network (full)","Exponential (simple)","Matrn (simple)","RQ (simple)","Neural Network (simple)"};
    int kernelMap[] = {2,3,5,6,7,8,12,13,15,16};
    JComboBox kernelMethod = new JComboBox(methods);
                        
    public boolean getOptionState_RemoveNotUsedComponents(){
        return this.removeNotUsedComponents.isSelected();
    }
    public boolean getOptionState_RemoveGUIComponents(){
        return this.removeGUIComponents.isSelected();
    }
    public boolean getOptionState_modelStructureOptimization(){
        return this.modelStructureOptimization.isSelected();
    }
    
    public void setParameterInformation(ArrayList<Parameter> param_info){
        this.param_info = param_info;
    }
    
    public void setEfficiencyInformation(ArrayList<Efficiency> eff_info){
        this.eff_info = eff_info;
    }
    
    @Override
    public String init(){
        isMultiObjective.setValue(false);
        
        if (param_info == null)
            return JAMS.resources.getString("error_no_parameter");
        if (eff_info == null)
            return JAMS.resources.getString("error_no_objective");
        
        isMultiObjective.setValue(eff_info.size() > 1);
        if (isMultiObjective.getValue()){
            this.optimizer.setSelectedIndex(2);
        }
        
        optimizerPanelWrapper.add(optimizerOptions[this.optimizer.getSelectedIndex()]);
        
        return null;
    }
    
    @Override
    public JPanel build(){
        panel.setLayout(new BorderLayout());
        panel.setBorder(null);
        panel.add(new JLabel(JAMS.resources.getString("step5_desc")), BorderLayout.NORTH); 
                                                
        JPanel optimizerPanel = new JPanel(new BorderLayout());                
        optimizerPanelWrapper.setBorder(BorderFactory.createLineBorder(Color.black));
        optimizer.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {                
                int index = optimizer.getSelectedIndex();
                optimizerPanelWrapper.removeAll();
                if (optimizerOptions[index] != null){
                    if (isMultiObjective.getValue()){
                        if (!multiObjective[index]){
                            JOptionPane.showMessageDialog(optimizerPanelWrapper, JAMS.resources.getString("multi_objective_optimization_not_supported"));
                            optimizer.setSelectedIndex(2);
                            index = 2;
                        }
                    }
                    optimizerPanelWrapper.add(optimizerOptions[index]);
                }
                panel.validate();
            }
        });
        
        
        optimizerPanel.add(optimizer,BorderLayout.NORTH);
        optimizerPanel.add(optimizerPanelWrapper,BorderLayout.SOUTH);
        
        JPanel metaOptions = new JPanel();
        metaOptions.setLayout(new BoxLayout(metaOptions,BoxLayout.Y_AXIS));
        metaOptions.add(removeNotUsedComponents);
        metaOptions.add(this.removeGUIComponents);
        metaOptions.add(this.modelStructureOptimization);
                                
        //create panels
        //0 -> SCE                                
        JPanel numberOfComplexesPanel = new JPanel(new BorderLayout());        
        numberOfComplexesPanel.add(new JLabel(JAMS.resources.getString("number_of_complexes"),JLabel.LEFT),BorderLayout.WEST);
        numberOfComplexesPanel.add(SCE_numberOfComplexes,BorderLayout.EAST);
                               
        JPanel maximumNumberOfIterationsPanel = new JPanel(new BorderLayout());
        maximumNumberOfIterationsPanel.add(new JLabel(JAMS.resources.getString("maximum_number_of_iterations"),JLabel.LEFT),BorderLayout.WEST);
        maximumNumberOfIterationsPanel.add(SCE_maximumNumberOfIterations,BorderLayout.EAST);
                
        JPanel pcentoPanel = new JPanel(new BorderLayout());
        pcentoPanel.add(new JLabel(JAMS.resources.getString("worst_acceptable_improvement"),JLabel.LEFT),BorderLayout.WEST);
        pcentoPanel.add(SCE_pcento,BorderLayout.EAST);
                
        JPanel pepsPanel = new JPanel(new BorderLayout());
        pepsPanel.add(new JLabel(JAMS.resources.getString("minimal_geometric_population"),JLabel.LEFT),BorderLayout.WEST);
        pepsPanel.add(SCE_peps,BorderLayout.EAST);
                
        JPanel kstopPanel = new JPanel(new BorderLayout());
        kstopPanel.add(new JLabel(JAMS.resources.getString("kStop")),BorderLayout.WEST);
        kstopPanel.add(SCE_kstop,BorderLayout.EAST);
        
        optimizerOptions[0] = new JPanel();
        optimizerOptions[0].setLayout(new BoxLayout(optimizerOptions[0],BoxLayout.Y_AXIS));
        optimizerOptions[0].add(numberOfComplexesPanel);
        optimizerOptions[0].add(maximumNumberOfIterationsPanel);
        optimizerOptions[0].add(pcentoPanel);
        optimizerOptions[0].add(pepsPanel);
        optimizerOptions[0].add(kstopPanel);
        
        //1 -> nelder mead                   
        JPanel maximumNumberOfIterationsPanel2 = new JPanel(new BorderLayout());
        maximumNumberOfIterationsPanel2.add(new JLabel(JAMS.resources.getString("maximum_number_of_iterations"),JLabel.LEFT),BorderLayout.WEST);
        maximumNumberOfIterationsPanel2.add(nelderMead_maximumNumberOfIterations,BorderLayout.EAST);
        
        optimizerOptions[1] = new JPanel();
        optimizerOptions[1].setLayout(new BoxLayout(optimizerOptions[1],BoxLayout.Y_AXIS));
        optimizerOptions[1].add(maximumNumberOfIterationsPanel2);
                
        //2 -> mocom            
        JPanel maximumNumberOfIterationsPanel3 = new JPanel(new BorderLayout());
        maximumNumberOfIterationsPanel3.add(new JLabel(JAMS.resources.getString("maximum_number_of_iterations"),JLabel.LEFT),BorderLayout.WEST);
        maximumNumberOfIterationsPanel3.add(MOCOM_maximumNumberOfIterations,BorderLayout.EAST);
                
        JPanel populationSizePanel = new JPanel(new BorderLayout());        
        populationSizePanel.add(new JLabel(JAMS.resources.getString("individuals_in_population"),JLabel.LEFT),BorderLayout.WEST);
        populationSizePanel.add(MOCOM_populationSize,BorderLayout.EAST);
                                                        
        optimizerOptions[2] = new JPanel();
        optimizerOptions[2].setLayout(new BoxLayout(optimizerOptions[2],BoxLayout.Y_AXIS));
        optimizerOptions[2].add(populationSizePanel);
        optimizerOptions[2].add(maximumNumberOfIterationsPanel3);
                        
        //3 -> branch and bound            
        JPanel maximumNumberOfIterationsPanel4 = new JPanel(new BorderLayout());
        maximumNumberOfIterationsPanel4.add(new JLabel(JAMS.resources.getString("maximum_number_of_iterations"),JLabel.LEFT),BorderLayout.WEST);
        maximumNumberOfIterationsPanel4.add(BranchAndBound_maximumNumberOfIterations,BorderLayout.EAST);
        
        optimizerOptions[3] = new JPanel();
        optimizerOptions[3].setLayout(new BoxLayout(optimizerOptions[3],BoxLayout.Y_AXIS));
        optimizerOptions[3].add(populationSizePanel);
        optimizerOptions[3].add(maximumNumberOfIterationsPanel4);
        
        //4 -> gutmann        
        JPanel maximumNumberOfIterationsPanel5 = new JPanel(new BorderLayout());
        maximumNumberOfIterationsPanel5.add(new JLabel(JAMS.resources.getString("maximum_number_of_iterations"),JLabel.LEFT),BorderLayout.WEST);
        maximumNumberOfIterationsPanel5.add(Gutmann_maximumNumberOfIterations,BorderLayout.EAST);
        
        optimizerOptions[4] = new JPanel();
        optimizerOptions[4].setLayout(new BoxLayout(optimizerOptions[4],BoxLayout.Y_AXIS));
        optimizerOptions[4].add(populationSizePanel);
        optimizerOptions[4].add(maximumNumberOfIterationsPanel5);
        
        //5 -> gp        
        JPanel maximumNumberOfIterationsPanel6 = new JPanel(new BorderLayout());
        maximumNumberOfIterationsPanel6.add(new JLabel(JAMS.resources.getString("maximum_number_of_iterations"),JLabel.LEFT),BorderLayout.WEST);
        maximumNumberOfIterationsPanel6.add(GPSearch_maximumNumberOfIterations,BorderLayout.EAST);
                        
        kernelMethod.setSelectedIndex(4);
        JPanel gpMethodPanel = new JPanel(new BorderLayout());
        gpMethodPanel.add(new JLabel(JAMS.resources.getString("Kernel_Method"),JLabel.LEFT),BorderLayout.WEST);
        gpMethodPanel.add(kernelMethod,BorderLayout.EAST);
        
        optimizerOptions[5] = new JPanel();
        optimizerOptions[5].setLayout(new BoxLayout(optimizerOptions[5],BoxLayout.Y_AXIS));
        optimizerOptions[5].add(gpMethodPanel);
        optimizerOptions[5].add(maximumNumberOfIterationsPanel6);
        
        //6 -> random sampler        
        JPanel maximumNumberOfIterationsPanel7 = new JPanel(new BorderLayout());
        maximumNumberOfIterationsPanel7.add(new JLabel(JAMS.resources.getString("maximum_number_of_iterations"),JLabel.LEFT),BorderLayout.WEST);
        maximumNumberOfIterationsPanel7.add(RandomSampler_maximumNumberOfIterations,BorderLayout.EAST);
                
        optimizerOptions[6] = new JPanel();
        optimizerOptions[6].setLayout(new BoxLayout(optimizerOptions[6],BoxLayout.Y_AXIS));        
        optimizerOptions[6].add(maximumNumberOfIterationsPanel7);
        
        //7 -> parallel random sampler                
        JPanel maximumNumberOfIterationsPanel8 = new JPanel(new BorderLayout());
        maximumNumberOfIterationsPanel8.add(new JLabel(JAMS.resources.getString("maximum_number_of_iterations"),JLabel.LEFT),BorderLayout.WEST);
        maximumNumberOfIterationsPanel8.add(ParallelRandomSearch_maximumNumberOfIterations,BorderLayout.EAST);
                
        JPanel excludedFilesPanel = new JPanel(new BorderLayout());                
        excludedFilesPanel.add(new JLabel(JAMS.resources.getString("do_not_transfer"),JLabel.LEFT),BorderLayout.WEST);
        excludedFilesPanel.add(ParallelRandomSearch_excludedFiles,BorderLayout.EAST);
        
        optimizerOptions[7] = new JPanel();
        optimizerOptions[7].setLayout(new BoxLayout(optimizerOptions[7],BoxLayout.Y_AXIS));        
        optimizerOptions[7].add(maximumNumberOfIterationsPanel8);
        optimizerOptions[7].add(excludedFilesPanel);
        
        //8 -> ParallelSCE                                
        JPanel parallel_numberOfComplexesPanel = new JPanel(new BorderLayout());        
        parallel_numberOfComplexesPanel.add(new JLabel(JAMS.resources.getString("number_of_complexes"),JLabel.LEFT),BorderLayout.WEST);
        parallel_numberOfComplexesPanel.add(ParallelSCE_numberOfComplexes,BorderLayout.EAST);
                                
        JPanel parallel_maximumNumberOfIterationsPanel = new JPanel(new BorderLayout());
        parallel_maximumNumberOfIterationsPanel.add(new JLabel(JAMS.resources.getString("maximum_number_of_iterations"),JLabel.LEFT),BorderLayout.WEST);
        parallel_maximumNumberOfIterationsPanel.add(ParallelSCE_maximumNumberOfIterations,BorderLayout.EAST);
                
        JPanel parallel_pcentoPanel = new JPanel(new BorderLayout());
        parallel_pcentoPanel.add(new JLabel(JAMS.resources.getString("worst_acceptable_improvement"),JLabel.LEFT),BorderLayout.WEST);
        parallel_pcentoPanel.add(ParallelSCE_pcento,BorderLayout.EAST);
                
        JPanel parallel_pepsPanel = new JPanel(new BorderLayout());
        parallel_pepsPanel.add(new JLabel(JAMS.resources.getString("minimal_geometric_population"),JLabel.LEFT),BorderLayout.WEST);
        parallel_pepsPanel.add(ParallelSCE_peps,BorderLayout.EAST);
                
        JPanel parallel_kstopPanel = new JPanel(new BorderLayout());
        parallel_kstopPanel.add(new JLabel(JAMS.resources.getString("kStop")),BorderLayout.WEST);
        parallel_kstopPanel.add(ParallelSCE_kstop,BorderLayout.EAST);
        
        JPanel excludedFilesPanel2 = new JPanel(new BorderLayout());                
        excludedFilesPanel2.add(new JLabel(JAMS.resources.getString("do_not_transfer"),JLabel.LEFT),BorderLayout.WEST);
        excludedFilesPanel2.add(ParallelSCE_excludedFiles,BorderLayout.EAST);
        
        optimizerOptions[8] = new JPanel();
        optimizerOptions[8].setLayout(new BoxLayout(optimizerOptions[8],BoxLayout.Y_AXIS));
        optimizerOptions[8].add(parallel_numberOfComplexesPanel);
        optimizerOptions[8].add(parallel_maximumNumberOfIterationsPanel);
        optimizerOptions[8].add(parallel_pcentoPanel);
        optimizerOptions[8].add(parallel_pepsPanel);
        optimizerOptions[8].add(parallel_kstopPanel);
        optimizerOptions[8].add(excludedFilesPanel2);
        
        panel.add(optimizerPanel,BorderLayout.CENTER);
        panel.add(metaOptions,BorderLayout.SOUTH);
        
        return panel;
    }
    @Override
    public JPanel getPanel(){
        return null;
    }
    
    public OptimizerDescription getOptimizerDescription(){
        return desc;
    }
    @Override
    public String finish(){        
        desc = new OptimizerDescription();
        int index = this.optimizer.getSelectedIndex();
        desc.efficiencies = this.eff_info;
        desc.parameters = this.param_info;
        desc.attributes = new ArrayList<AttributeDescription>();
        
        //build param string
        String param_string = "";
        for (int i=0;i<param_info.size();i++){
            if (param_info.get(i).variableName != null)
                param_string += param_info.get(i).variableName + ";";
            else
                param_string += param_info.get(i).attributeName + ";";
        }
        desc.attributes.add(new AttributeDescription("parameterIDs","optimizer",param_string,true));
        //build boundary string
        String boundary_string = "";
        for (int i=0;i<param_info.size();i++){
            boundary_string += "[" + param_info.get(i).lowerBound + ">" + param_info.get(i).upperBound + "];";
        }
        
        desc.attributes.add(new AttributeDescription("boundaries",null,boundary_string,false));
        //build startvalue string
        String startvalue_string = "";
        boolean validStartValue = true;
        for (int i=0;i<param_info.size();i++){
            if (param_info.get(i).startValueValid)
                startvalue_string += param_info.get(i).startValue + ";";
            else{
                validStartValue = false;
                break;
            }
        }
        if (validStartValue)
            desc.attributes.add(new AttributeDescription("startValue",null,startvalue_string,false));
        
        if (this.isMultiObjective.getValue()){
            //TODO
        }else{
            desc.attributes.add(new AttributeDescription("effValue","optimizer",this.eff_info.get(0).attributeName,true));
            desc.attributes.add(new AttributeDescription("effMethodName",null,this.eff_info.get(0).attributeName,false));
            desc.attributes.add(new AttributeDescription("mode",null,Integer.toString(this.eff_info.get(0).mode),false));
        }
        desc.attributes.add(new AttributeDescription("enable",null,"true",false));
        
        
        
        switch(index){            
            //sce
            case 0:{
                desc.optimizerClassName = "jams.components.optimizer.SimpleSCE";
                int numOfComplexes = 0;
                try{
                    numOfComplexes = Integer.parseInt(SCE_numberOfComplexes.getText());
                }catch(Exception e){}
                if (numOfComplexes < 1 || numOfComplexes > 100)
                    return JAMS.resources.getString("number_of_complexes_have_to_be_an_integer_between_1_and_100");
                desc.attributes.add(new AttributeDescription("NumberOfComplexes",null,Integer.toString(numOfComplexes),false));
                
                double pcento = 0;
                try{
                    pcento = Double.parseDouble(SCE_pcento.getText());
                }catch(Exception e){}
                if (pcento < 0 || pcento > 1)
                    return JAMS.resources.getString("value_of_pcento_have_to_be_between_0_and_1");
                desc.attributes.add(new AttributeDescription("pcento",null,Double.toString(pcento),false));
                
                double peps = 0;
                try{
                    peps = Double.parseDouble(SCE_peps.getText());
                }catch(Exception e){}
                if (peps < 0 || peps > 1)
                    return JAMS.resources.getString("value_of_peps_have_to_be_between_0_and_1");
                desc.attributes.add(new AttributeDescription("peps",null,Double.toString(peps),false));
                
                int kstop = 0;
                try{
                    kstop = Integer.parseInt(SCE_kstop.getText());
                }catch(Exception e){}
                if (kstop < 1 || kstop > 100)
                    return JAMS.resources.getString("kstop_have_to_be_an_integer_between_1_and_100");
                desc.attributes.add(new AttributeDescription("kstop",null,Integer.toString(kstop),false));
                
                int maxn = 0;
                try{
                    maxn = Integer.parseInt(SCE_maximumNumberOfIterations.getText());
                }catch(Exception e){}
                if (maxn < 1 )
                    return JAMS.resources.getString("error_maxiter_greater_1");
                desc.attributes.add(new AttributeDescription("maxn",null,Integer.toString(maxn),false));
        
                break;
            }
            case 1:{       
                desc.optimizerClassName = "jams.components.optimizer.NelderMead";
                
                int maxn = 0;
                try{
                    maxn = Integer.parseInt(nelderMead_maximumNumberOfIterations.getText());
                }catch(Exception e){}
                if (maxn < 1 )
                    return JAMS.resources.getString("error_maxiter_greater_1");
                desc.attributes.add(new AttributeDescription("maxn",null,Integer.toString(maxn),false));        
                                        
                break;
            }
            case 2:{
                desc.optimizerClassName = "jams.components.optimizer.MOCOM";
                
                int maxn = 0;
                try{
                    maxn = Integer.parseInt(MOCOM_maximumNumberOfIterations.getText());
                }catch(Exception e){}
                if (maxn < 1 )
                    return JAMS.resources.getString("error_maxiter_greater_1");
                desc.attributes.add(new AttributeDescription("maxn",null,Integer.toString(maxn),false));        
                   
                int popSize = 0;
                try{
                    popSize = Integer.parseInt(this.MOCOM_populationSize.getText());
                }catch(Exception e){}
                if (popSize < 1 )
                    return JAMS.resources.getString("population_size_must_be_positive");
                desc.attributes.add(new AttributeDescription("populationSize",null,Integer.toString(popSize),false)); 
                
                break;                                
            }
            case 3:{
                desc.optimizerClassName = "jams.components.optimizer.BranchAndBound";
                
                int maxn = 0;
                try{
                    maxn = Integer.parseInt(BranchAndBound_maximumNumberOfIterations.getText());
                }catch(Exception e){}
                if (maxn < 1 )
                    return JAMS.resources.getString("error_maxiter_greater_1");
                desc.attributes.add(new AttributeDescription("maxn",null,Integer.toString(maxn),false));        
                break;
            }
            case 4:{
                desc.optimizerClassName = "jams.components.optimizer.GutmannMethod";
                
                int maxn = 0;
                try{
                    maxn = Integer.parseInt(Gutmann_maximumNumberOfIterations.getText());
                }catch(Exception e){}
                if (maxn < 1 )
                    return JAMS.resources.getString("error_maxiter_greater_1");
                desc.attributes.add(new AttributeDescription("maxn",null,Integer.toString(maxn),false));        
                break;
            }
            case 5:{
                desc.optimizerClassName = "jams.components.optimizer.GPSearch";
                
                int maxn = 0;
                try{
                    maxn = Integer.parseInt(GPSearch_maximumNumberOfIterations.getText());
                }catch(Exception e){}
                if (maxn < 1 )
                    return JAMS.resources.getString("error_maxiter_greater_1");
                desc.attributes.add(new AttributeDescription("maxn",null,Integer.toString(maxn),false));        
                
                int kernel = this.kernelMethod.getSelectedIndex();                
                desc.attributes.add(new AttributeDescription("GPMethod",null,Integer.toString(kernelMap[kernel]),false));        
                break;
            }
            case 6:{
                desc.optimizerClassName = "jams.components.optimizer.RandomSampler";
                
                int maxn = 0;
                try{
                    maxn = Integer.parseInt(RandomSampler_maximumNumberOfIterations.getText());
                }catch(Exception e){}
                if (maxn < 1 )
                    return JAMS.resources.getString("error_maxiter_greater_1");
                desc.attributes.add(new AttributeDescription("maxn",null,Integer.toString(maxn),false));        
                break;
            }
            case 7:{
                desc.optimizerClassName = "jams.parallel.optimizer.ParallelRandomSampler";
                
                int maxn = 0;
                try{
                    maxn = Integer.parseInt(ParallelRandomSearch_maximumNumberOfIterations.getText());
                }catch(Exception e){}
                if (maxn < 1 )
                    return JAMS.resources.getString("error_maxiter_greater_1");
                desc.attributes.add(new AttributeDescription("maxn",null,Integer.toString(maxn),false));        
                break;
            }
            case 8:{
                desc.optimizerClassName = "jams.parallel.optimizer.SimpleParallelSCE";
                                
                int numOfComplexes = 0;
                try{
                    numOfComplexes = Integer.parseInt(ParallelSCE_numberOfComplexes.getText());
                }catch(Exception e){}
                if (numOfComplexes < 1 || numOfComplexes > 100)
                    return JAMS.resources.getString("number_of_complexes_have_to_be_an_integer_between_1_and_100");
                desc.attributes.add(new AttributeDescription("NumberOfComplexes",null,Integer.toString(numOfComplexes),false));
                
                double pcento = 0;
                try{
                    pcento = Double.parseDouble(ParallelSCE_pcento.getText());
                }catch(Exception e){}
                if (pcento < 0 || pcento > 1)
                    return JAMS.resources.getString("value_of_pcento_have_to_be_between_0_and_1");
                desc.attributes.add(new AttributeDescription("pcento",null,Double.toString(pcento),false));
                
                double peps = 0;
                try{
                    peps = Double.parseDouble(ParallelSCE_peps.getText());
                }catch(Exception e){}
                if (peps < 0 || peps > 1)
                    return JAMS.resources.getString("value_of_peps_have_to_be_between_0_and_1");
                desc.attributes.add(new AttributeDescription("peps",null,Double.toString(peps),false));
                
                int kstop = 0;
                try{
                    kstop = Integer.parseInt(ParallelSCE_kstop.getText());
                }catch(Exception e){}
                if (kstop < 1 || kstop > 100)
                    return JAMS.resources.getString("kstop_have_to_be_an_integer_between_1_and_100");
                desc.attributes.add(new AttributeDescription("kstop",null,Integer.toString(kstop),false));
                
                int maxn = 0;
                try{
                    maxn = Integer.parseInt(ParallelSCE_maximumNumberOfIterations.getText());
                }catch(Exception e){}
                if (maxn < 1 )
                    return JAMS.resources.getString("error_maxiter_greater_1");
                desc.attributes.add(new AttributeDescription("maxn",null,Integer.toString(maxn),false));
                
                try{
                    Pattern.compile(ParallelRandomSearch_excludedFiles.getText());
                }catch(PatternSyntaxException pse){
                    return  JAMS.resources.getString("There_is_a_problem_with_the_regular_expression!") +"\n"+
                            JAMS.resources.getString("The_pattern_in_question_is") + ": " + pse.getPattern() + "\n"+
                            JAMS.resources.getString("The_description_is")+": "+pse.getDescription() + "\n";                
                }
                desc.attributes.add(new AttributeDescription("maxn",null,ParallelRandomSearch_excludedFiles.getText(),false));
                
                break;
            }            
        }
        return null;
    }
}
