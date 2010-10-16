/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jamsui.juice.optimizer.wizard;

import jamsui.juice.optimizer.wizard.Tools.Parameter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.SortedSet;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.w3c.dom.Document;
import jams.JAMS;
import jams.JAMSProperties;
import jamsui.juice.gui.JUICEFrame;
import jamsui.juice.optimizer.wizard.modelModifier.WizardException;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 *
 * @author chris
 */
public class OptimizationWizard{

    JPanel mainPanel;
    JList  parameterList = new JList(),
           objectiveList = new JList();
    JPanel parameterConfigurationPanel, objectiveConfigurationPanel, optimizerConfigurationPanel;

    Document doc;
    JAMSProperties propertyFile;

    String optimizationModes[] = {JAMS.resources.getString("minimization"),
                                    JAMS.resources.getString("maximization"),
                                    JAMS.resources.getString("absolute_minimization"),
                                    JAMS.resources.getString("absolute_maximization")};
    String workspace = null;

    OptimizerDescription optimizerDesc[];
    JUICEFrame parent = null;

    public class OptimizerParameter {
        public String description;
        public String name;
    }

    public class NumericOptimizerParameter extends OptimizerParameter{
        public double value;
        public double lowerBound;
        public double upperBound;
                
        public NumericOptimizerParameter(String name, String desc,
                double value, double lowerBound, double upperBound){
            this.name = name;
            this.description = desc;
            this.value = value;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }
    }
    
    public class BooleanOptimizerParameter extends OptimizerParameter{
        public boolean value;        
        
        public BooleanOptimizerParameter(String name, String desc, 
                boolean value){
            this.name = name;
            this.description = desc;
            this.value = value;            
        }
    }
    
    public class StringOptimizerParameter extends OptimizerParameter{
        public String value;        
        
        public StringOptimizerParameter(String name, String desc, 
                String value){
            this.name = name;
            this.description = desc;
            this.value = value;            
        }
    }

    public class OptimizerDescription{
        String shortName;
        String optimizerClassName;
        int id;
        ArrayList<OptimizerParameter> propertyMap = new ArrayList<OptimizerParameter>();;

        boolean multiObjective = false;

        public OptimizerDescription(String shortName,int id){
            this.shortName = shortName;
            this.id = id;
        }
        public OptimizerDescription(String shortName,int id,boolean multiObjective){
            this.shortName = shortName;
            this.multiObjective = multiObjective;
            this.id = id;
        }
        public int getId(){
            return id;
        }
        public void setOptimizerClassName(String className){
            this.optimizerClassName = className;
        }
        public String getOptimizerClassName(){
            return this.optimizerClassName;
        }
        public void addParameter(OptimizerParameter param){
            propertyMap.add(param);
        }
        @Override
        public String toString(){
            return shortName;
        }
    }

    class NumericFocusListener implements FocusListener {
        public static final int MODE_LOWERBOUND = 0;
        public static final int MODE_UPPERBOUND = 1;
        public static final int MODE_STARTVALUE = 2;
        public static final int MODE_PARAMETERVALUE = 3;

        public void focusLost(FocusEvent e) {
            JTextField src = (JTextField) e.getSource();
            Parameter p = (Parameter) src.getClientProperty("parameter");
            Integer mode = (Integer) src.getClientProperty("mode");

            try {
                //try to convert text
                double value = Double.parseDouble(src.getText());
                if (mode.intValue() == MODE_PARAMETERVALUE) {
                }

                switch (mode.intValue()) {

                    case MODE_LOWERBOUND:
                        p.lowerBound = Double.parseDouble(src.getText());
                        break;
                    case MODE_UPPERBOUND:
                        p.upperBound = Double.parseDouble(src.getText());
                        break;
                    case MODE_STARTVALUE:
                        p.startValue = Double.parseDouble(src.getText());
                        p.startValueValid = true;
                        break;
                    case MODE_PARAMETERVALUE:
                        NumericOptimizerParameter p2 = (NumericOptimizerParameter) src.getClientProperty("property");
                        p2.value = Double.parseDouble(src.getText());
                        NumericOptimizerParameter param = (NumericOptimizerParameter) src.getClientProperty("property");
                        if (value < param.lowerBound || value > param.upperBound) {
                            throw new NumberFormatException();
                        }
                        break;
                }
            } catch (NumberFormatException nfe) {
                switch (mode.intValue()) {
                    case MODE_LOWERBOUND:
                        src.setText(Double.toString(p.lowerBound));
                        break;
                    case MODE_UPPERBOUND:
                        src.setText(Double.toString(p.upperBound));
                        break;
                    case MODE_STARTVALUE:
                        if (p.startValueValid) {
                            src.setText(Double.toString(p.startValue));
                        } else {
                            src.setText("");
                        }
                        break;
                    case MODE_PARAMETERVALUE:
                        src.setText(Double.toString(((NumericOptimizerParameter) src.getClientProperty("property")).value));
                        break;
                }
            }
        }
        public void focusGained(FocusEvent e){
        }
    }
    class NumericKeyListener implements KeyListener {
            public static final int MODE_LOWERBOUND = 0;
            public static final int MODE_UPPERBOUND = 1;
            public static final int MODE_STARTVALUE = 2;
            public static final int MODE_PARAMETERVALUE = 3;

            public Integer getModeLowerBound(){
                return new Integer(MODE_LOWERBOUND);
            }
            public Integer getModeUpperBound(){
                return new Integer(MODE_UPPERBOUND);
            }
            public Integer getModeStartValue(){
                return new Integer(MODE_STARTVALUE);
            }
            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
                char keyChar = e.getKeyChar();
                if (!java.lang.Character.isDigit(keyChar) && keyChar != '.') {
                    e.consume();
                }
                JTextField src = (JTextField) e.getSource();
                /*
                Parameter p = (Parameter) src.getClientProperty("parameter");
                Integer mode = (Integer) src.getClientProperty("mode");
                try {
                    switch (mode.intValue()) {

                        case MODE_LOWERBOUND:                            
                            p.lowerBound = Double.parseDouble(src.getText() + keyChar);
                            break;
                        case MODE_UPPERBOUND:
                            p.upperBound = Double.parseDouble(src.getText() + keyChar);
                            break;
                        case MODE_STARTVALUE:
                            p.startValue = Double.parseDouble(src.getText() + keyChar);
                            p.startValueValid = true;
                            break;
                        case MODE_PARAMETERVALUE:
                            NumericOptimizerParameter p2 = (NumericOptimizerParameter) src.getClientProperty("property");
                            p2.value = Double.parseDouble(src.getText() + keyChar);
                            break;
                    }
                } catch (NumberFormatException nfe) {
                    e.consume();
                }*/
            }
        };
    NumericKeyListener stdNumericKeyListener = new NumericKeyListener();
    NumericFocusListener stdFocusListener = new NumericFocusListener();
    
    public void setModel(Document doc, JAMSProperties propertyFile){
        this.doc = doc;
        this.propertyFile = propertyFile;
        modelAnalyzer analyzer = new modelAnalyzer(propertyFile, doc);
        fillParameterList(analyzer.getParameters());
        fillObjectiveList(analyzer.getObjectives());
    }

    public void setModel(File modelFile, File propertyFile){
        modelAnalyzer analyzer = new modelAnalyzer(propertyFile, modelFile);
        fillParameterList(analyzer.getParameters());
        fillObjectiveList(analyzer.getObjectives());

        this.doc = analyzer.getModelDoc();
        this.propertyFile = analyzer.getProperties();
    }

    private void fillParameterList(SortedSet<Parameter> parameterSet){
        DefaultListModel model = new DefaultListModel();
        for(Parameter parameter : parameterSet)
            model.addElement(parameter);
        parameterList.setModel(model);
    }

    private void fillObjectiveList(SortedSet<String> objectiveSet){
        DefaultListModel model = new DefaultListModel();
        for(String objective : objectiveSet)
            model.addElement(objective);
        objectiveList.setModel(model);
    }

    public HashMap<String, String> createOptimizationIni(){
        HashMap<String, String> iniMap = new HashMap<String, String>();
        //erzeuge property file
        Object[] objects = this.parameterList.getSelectedValues();
        String parameterIDString = "";
        String objectiveIDString = "";
        String lowerBoundString = "";
        String upperBoundString = "";
        String startValueString = "";
        String objectiveMode = "";
        boolean startValueValid = true;
        iniMap.put("n",Integer.toString(objects.length));
        
        for (Object obj : objects){
            Parameter p = (Parameter)obj;
            parameterIDString += p.extendedtoString2() + ";";
            lowerBoundString += p.lowerBound + ";";
            upperBoundString += p.upperBound + ";";
            if (p.startValueValid)
                startValueString += p.startValue + ";";
            else
                startValueValid = false;
        }

        objects = this.objectiveList.getSelectedValues();
        for (Object obj : objects){
            String objective = (String)obj;
            objectiveIDString += objective + ";";
            Integer mode = (Integer)this.objectiveConfigurationPanel.getClientProperty(objective);
            objectiveMode += mode.toString() + ";";
        }        

        OptimizerDescription desc = (OptimizerDescription)optimizerConfigurationPanel.getClientProperty("optimizer");

        iniMap.put("jobMode","optimizationRun");
        iniMap.put("efficiency_modes",objectiveMode);
        iniMap.put("efficiencies",objectiveIDString);
        iniMap.put("method",Integer.toString(desc.getId()));
        iniMap.put("parameters",parameterIDString);
        iniMap.put("lowerbounds",lowerBoundString);
        iniMap.put("upperbounds",upperBoundString);
        if (startValueValid)
            iniMap.put("startvalues",startValueString);

        for (OptimizerParameter param : desc.propertyMap){
            if (param instanceof NumericOptimizerParameter){
                iniMap.put(param.name,Double.toString(((NumericOptimizerParameter)param).value));
            }
            if (param instanceof StringOptimizerParameter){
                iniMap.put(param.name,((StringOptimizerParameter)param).value);
            }
            if (param instanceof BooleanOptimizerParameter){
                if (((BooleanOptimizerParameter)param).value)
                    iniMap.put(param.name,"1");
                else
                    iniMap.put(param.name,"0");
            }
        }
        if (workspace != null){
            iniMap.put("workspace",workspace);
        }
        return iniMap;
    }
   
    public OptimizerDescription getDefaultOptimizerDescription(String shortName, String className, int id, boolean multiObj){
        OptimizerDescription defDesc = new OptimizerDescription(shortName,id, multiObj);
        defDesc.setOptimizerClassName(className);
        defDesc.addParameter(new NumericOptimizerParameter(
                "maxn",JAMS.resources.getString("maximum_number_of_iterations"),
                500,1,100000));
        defDesc.addParameter(new BooleanOptimizerParameter(
                "removeUnusedComponents",JAMS.resources.getString("remove_unused_components"),
                true));
        defDesc.addParameter(new BooleanOptimizerParameter(
                "optimizeModelStructure",JAMS.resources.getString("optimize_model_structure"),
                false));
        defDesc.addParameter(new BooleanOptimizerParameter(
                "removeGUIComponents",JAMS.resources.getString("remove_GUI_components"),
                true));
        return defDesc;
    }
    public OptimizerDescription getSCEDescription(){
        OptimizerDescription sceDesc = getDefaultOptimizerDescription(JAMS.resources.getString("SCE"),
                                        "jams.components.optimizer.SimpleSCE",5,false);
        sceDesc.addParameter(new NumericOptimizerParameter("numberOfComplexes",
                JAMS.resources.getString("number_of_complexes"),2,1,100));        
        sceDesc.addParameter(new NumericOptimizerParameter(
                "pcento",JAMS.resources.getString("worst_acceptable_improvement"),
                0.05,0.000001,1));
        sceDesc.addParameter(new NumericOptimizerParameter(
                "peps",JAMS.resources.getString("minimal_geometric_population"),
                0.00001,0.000001,1));
        sceDesc.addParameter(new NumericOptimizerParameter(
                "kstop",JAMS.resources.getString("kStop"),
                10,1,100));

        return sceDesc;
    }
    
    public OptimizerDescription getNelderMeadDescription(){
        return getDefaultOptimizerDescription(JAMS.resources.getString("Nelder_Mead"),
                                        "jams.components.optimizer.NelderMead",2,false);
    }
    public OptimizerDescription getMOCOMDescription(){
        OptimizerDescription MOCOMDesc = getDefaultOptimizerDescription(JAMS.resources.getString("MOCOM"),
                                        "jams.components.optimizer.MOCOM",6,true);
        MOCOMDesc.addParameter(new NumericOptimizerParameter(
                "popSize",JAMS.resources.getString("individuals_in_population"),
                500,1,100000));
        return MOCOMDesc;
    }
    public OptimizerDescription getBranchAndBoundDescription(){
        return getDefaultOptimizerDescription(JAMS.resources.getString("Branch_and_Bound"),
                                        "jams.components.optimizer.BranchAndBound",0,false);
    }
    public OptimizerDescription getGutmannDescription(){
        OptimizerDescription GutmannDesc = getDefaultOptimizerDescription(JAMS.resources.getString("Gutmann"),
                                        "jams.components.optimizer.GutmannMethod",4,false);
        GutmannDesc.addParameter(new NumericOptimizerParameter(
                "popSize",JAMS.resources.getString("individuals_in_population"),
                500,1,100000));
        return GutmannDesc;
    }
    public OptimizerDescription getGPDescription(){
        OptimizerDescription GPDesc = getDefaultOptimizerDescription(JAMS.resources.getString("Gaussian_Process_Optimizer"),
                                        "jams.components.optimizer.GPSearch",1,false);
        GPDesc.addParameter(new NumericOptimizerParameter(
                "kernelMethod",JAMS.resources.getString("Kernel_Method"),
                6,0,100));
        return GPDesc;
    }
    public OptimizerDescription getRandomSamplerDescription(){
        return getDefaultOptimizerDescription(JAMS.resources.getString("Random_Sampler"),
                                        "jams.components.optimizer.RandomSampler",8,true);
    }
    public OptimizerDescription getParallelRandomSamplerDescription(){
        OptimizerDescription ParallelRandomSamplerDesc = 
                getDefaultOptimizerDescription(JAMS.resources.getString("Parallel_Random_Sampler"),
                                        "jams.components.optimizer.ParallelRandomSampler",7,true);
        ParallelRandomSamplerDesc.addParameter(new StringOptimizerParameter(
                "fileFilter",JAMS.resources.getString("do_not_transfer"),
                "(.*.cache)|(.*.jam)|(.*.ser)|(.*.svn)|(.*output)"));
        return ParallelRandomSamplerDesc;
    }
    public OptimizerDescription getParallelSCEDescription(){
        OptimizerDescription ParallelSCEDesc = getSCEDescription();
        ParallelSCEDesc.id = 3;
        ParallelSCEDesc.shortName = JAMS.resources.getString("Parallel_SCE");
        ParallelSCEDesc.setOptimizerClassName("jams.components.optimizer.SimpleParallelSCE");
        ParallelSCEDesc.addParameter(new StringOptimizerParameter(
                "excludedFiles",JAMS.resources.getString("do_not_transfer"),
                "(.*.cache)|(.*.jam)|(.*.ser)|(.*.svn)|(.*output)"));
        return ParallelSCEDesc;
    }
    public OptimizerDescription getDIRECTDescription(){
        return getDefaultOptimizerDescription(JAMS.resources.getString("DIRECT"),
                                        "jams.components.optimizer.Direct",10,false);
    }
    public OptimizerDescription getNSGA2Description(){
        OptimizerDescription NSGA2Desc = getDefaultOptimizerDescription(JAMS.resources.getString("NSGA2"),
                                        "jams.components.optimizer.NSGA2",9,true);
        NSGA2Desc.addParameter(new NumericOptimizerParameter(
                "popSize",JAMS.resources.getString("individuals_in_population"),
                500,1,100000));
        NSGA2Desc.addParameter(new NumericOptimizerParameter(
                "crossoverProbability",JAMS.resources.getString("crossoverProbability"),
                0.5,0,1));
        NSGA2Desc.addParameter(new NumericOptimizerParameter(
                "mutationProbability",JAMS.resources.getString("mutationProbability"),
                0.5,0.5,1));
        NSGA2Desc.addParameter(new NumericOptimizerParameter(
                "crossoverDistributionIndex",JAMS.resources.getString("crossoverDistributionIndex"),
                10,1,100));
        NSGA2Desc.addParameter(new NumericOptimizerParameter(
                "mutationDistributionIndex",JAMS.resources.getString("mutationDistributionIndex"),
                10,1,100));
        NSGA2Desc.addParameter(new NumericOptimizerParameter(
                "maxGeneration",JAMS.resources.getString("maxGeneration"),
                10,1,1000));
        return NSGA2Desc;
    }
    public OptimizationWizard(){
        this.parent = null;        
    }
    public OptimizationWizard(JUICEFrame parent){
        this.parent = parent;        
    }

    private void initOptimizerDesc(){
        optimizerDesc = new OptimizerDescription[11];
        optimizerDesc[0] = getSCEDescription();
        optimizerDesc[1] = getNelderMeadDescription();
        optimizerDesc[2] = getMOCOMDescription();
        optimizerDesc[3] = getBranchAndBoundDescription();
        optimizerDesc[4] = getGutmannDescription();
        optimizerDesc[5] = getGPDescription();
        optimizerDesc[6] = getRandomSamplerDescription();
        optimizerDesc[7] = getParallelRandomSamplerDescription();
        optimizerDesc[8] = getParallelSCEDescription();
        optimizerDesc[9] = getDIRECTDescription();
        optimizerDesc[10] = getNSGA2Description();
    }
    public JPanel getPanel(){
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

        JPanel parameterPanel = new JPanel(new BorderLayout());
        JPanel objectivePanel = new JPanel(new BorderLayout());
        JPanel optimizerPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(optimizerPanel,BorderLayout.WEST);
        northPanel.add(buttonPanel,BorderLayout.EAST);

        mainPanel.add(northPanel,c);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        mainPanel.add(parameterPanel,c);
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1.0;
        c.weighty = 1.0;
        mainPanel.add(objectivePanel,c);

        buttonPanel.add(new JButton(JAMS.resources.getString("Create_XML")){{

            addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    HashMap<String, String> ini = createOptimizationIni();
                    String strIni = "";
                    Iterator<Entry<String,String>> iter = ini.entrySet().iterator();
                    if (OptimizationWizard.this.parameterList.getSelectedIndices().length<1){
                        JOptionPane.showMessageDialog(OptimizationWizard.this.mainPanel,JAMS.resources.getString("Error_You_should_select_at_least_one_parameter_for_optimization"));
                        return;
                    }
                    if (OptimizationWizard.this.objectiveList.getSelectedIndices().length<1){
                        JOptionPane.showMessageDialog(OptimizationWizard.this.mainPanel, JAMS.resources.getString("Error_You_should_select_at_least_one_objective_for_optimization"));
                        return;
                    }
                    while(iter.hasNext()){
                        Entry<String,String> entry = iter.next();
                        strIni += entry.getKey() + "=" + entry.getValue() + "\n";
                    }
                    try{
                        Document optDocument = modelModifier.modelModifier(propertyFile, doc, strIni );
                        if (parent != null){
                            parent.newModel(optDocument);
                        
                        }
                    }catch(WizardException we){
                        JOptionPane.showMessageDialog(OptimizationWizard.this.mainPanel, we.toString());
                        return;
                    }
                }
            });
         }},BorderLayout.CENTER);

        parameterPanel.setBorder(BorderFactory.createTitledBorder(
                                    JAMS.resources.getString("Parameter_Configuration")));
        objectivePanel.setBorder(BorderFactory.createTitledBorder(
                                    JAMS.resources.getString("Objective_Configuration")));
        optimizerPanel.setBorder(BorderFactory.createTitledBorder(
                                    JAMS.resources.getString("Optimizer_Configuration")));
        
        parameterConfigurationPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPaneParameterList = new JScrollPane(parameterList);
        scrollPaneParameterList.setPreferredSize(new Dimension(250,250));
        parameterPanel.add(scrollPaneParameterList,BorderLayout.WEST);

        JScrollPane scrollPaneParameterSpecificationPanel = new JScrollPane(parameterConfigurationPanel);
        scrollPaneParameterSpecificationPanel.setPreferredSize(new Dimension(480,250));
        parameterPanel.add(scrollPaneParameterSpecificationPanel,BorderLayout.EAST);

        objectiveConfigurationPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPaneObjectiveList = new JScrollPane(objectiveList);
        scrollPaneObjectiveList.setPreferredSize(new Dimension(250,250));
        objectivePanel.add(scrollPaneObjectiveList,BorderLayout.WEST);

        JScrollPane scrollPaneObjectivePanel = new JScrollPane(objectiveConfigurationPanel);
        scrollPaneObjectivePanel.setPreferredSize(new Dimension(480,250));
        objectivePanel.add(scrollPaneObjectivePanel,BorderLayout.EAST);

        optimizerConfigurationPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPaneOptimizerSpecificationPanel = new JScrollPane(optimizerConfigurationPanel);
        scrollPaneOptimizerSpecificationPanel.setPreferredSize(new Dimension(550,250));
        optimizerPanel.add(scrollPaneOptimizerSpecificationPanel,BorderLayout.CENTER);
                
        parameterPanel.setMinimumSize(new Dimension(750,200));
        objectivePanel.setMinimumSize(new Dimension(750,200));
        optimizerPanel.setMinimumSize(new Dimension(750,250));

        parameterList.addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent evt){
                Object params[] = parameterList.getSelectedValues();

                GridBagConstraints c = new GridBagConstraints();
                int counter = 0;
                parameterConfigurationPanel.removeAll();                
                c.gridx = 0;
                c.gridy = counter;                    
                c.weightx = 1.0;
                c.weighty = 1.0;
                c.insets = new Insets(5,0,5,0);
                c.anchor = GridBagConstraints.NORTHWEST;
                parameterConfigurationPanel.add(new JLabel(JAMS.resources.getString("parameter")),c);
                c.gridx = 1;                
                c.anchor = GridBagConstraints.CENTER;
                parameterConfigurationPanel.add(new JLabel(JAMS.resources.getString("lower_bound")),c);
                c.gridx = 2;                                
                parameterConfigurationPanel.add(new JLabel(JAMS.resources.getString("upper_bound")),c);
                c.gridx = 3;                
                parameterConfigurationPanel.add(new JLabel(JAMS.resources.getString("start_value")),c);

                for (Object o : params){
                    Parameter p = (Parameter)o;
                    counter++;
                    c.insets = new Insets(0,0,2,0);
                    c.gridx = 0;
                    c.gridy = counter;                                       
                    c.anchor = GridBagConstraints.NORTHWEST;

                    parameterConfigurationPanel.add( new JLabel(p.toString()),c);                    
                    c.gridx = 1;                    
                    c.anchor = GridBagConstraints.NORTH ;
                    JTextField lowerBound = new JTextField(Double.toString(p.lowerBound),5);
                    lowerBound.addKeyListener(stdNumericKeyListener);
                    lowerBound.addFocusListener(stdFocusListener);
                    lowerBound.putClientProperty("parameter", p);
                    lowerBound.putClientProperty("mode", NumericKeyListener.MODE_LOWERBOUND);
                    parameterConfigurationPanel.add(lowerBound,c);

                    c.gridx = 2;                    
                    c.anchor = GridBagConstraints.NORTH;
                    JTextField upperBound = new JTextField(Double.toString(p.upperBound),5);
                    upperBound.addKeyListener(stdNumericKeyListener);
                    upperBound.addFocusListener(stdFocusListener);
                    upperBound.putClientProperty("parameter", p);
                    upperBound.putClientProperty("mode", NumericKeyListener.MODE_UPPERBOUND);
                    parameterConfigurationPanel.add(upperBound,c);

                    c.gridx = 3;                    
                    c.anchor = GridBagConstraints.NORTH;
                    if (counter == params.length)
                        if (140>params.length*25)
                            c.insets = new Insets( 0, 0, 140 - params.length*25, 0);

                    JTextField startValue = new JTextField(5);
                    if (p.startValueValid){
                        startValue.setText(Double.toString(p.startValue));
                    }
                    startValue.addKeyListener(stdNumericKeyListener);
                    startValue.addFocusListener(stdFocusListener);
                    startValue.putClientProperty("parameter", p);
                    startValue.putClientProperty("mode", NumericKeyListener.MODE_STARTVALUE);
                    parameterConfigurationPanel.add(startValue,c);
                }                
                parameterConfigurationPanel.updateUI();
                mainPanel.validate();
            }
        });

        objectiveList.addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent evt){
                Object objectives[] = objectiveList.getSelectedValues();

                GridBagConstraints c = new GridBagConstraints();
                int counter = 0;
                objectiveConfigurationPanel.removeAll();
                c.gridx = 0;
                c.gridy = counter;
                c.weightx = 1.0;
                c.weighty = 1.0;
                c.insets = new Insets(5,0,5,0);
                c.anchor = GridBagConstraints.NORTHWEST;
                objectiveConfigurationPanel.add(new JLabel(JAMS.resources.getString("Objective")),c);
                c.gridx = 1;
                c.anchor = GridBagConstraints.CENTER;
                objectiveConfigurationPanel.add(new JLabel(JAMS.resources.getString("mode")),c);

                for (Object o : objectives){
                    String obj = (String)o;
                    counter++;
                    c.insets = new Insets(0,0,2,0);
                    c.gridx = 0;
                    c.gridy = counter;
                    c.anchor = GridBagConstraints.NORTHWEST;
                    objectiveConfigurationPanel.add(new JLabel(obj),c);

                    c.gridx = 1;
                    c.anchor = GridBagConstraints.NORTH ;

                    JComboBox selectObjectiveMode = new JComboBox(optimizationModes);
                    selectObjectiveMode.putClientProperty("name", obj);
                    Object selection = objectiveConfigurationPanel.getClientProperty(obj);
                    if (selection!=null){
                        selectObjectiveMode.setSelectedIndex(((Integer)selection).intValue());
                    }else
                        objectiveConfigurationPanel.putClientProperty(obj, new Integer(0));

                    selectObjectiveMode.addItemListener(new ItemListener(){
                        public void itemStateChanged(ItemEvent e){
                            JComboBox src = (JComboBox)e.getSource();
                            objectiveConfigurationPanel.putClientProperty(
                                    src.getClientProperty("name"),
                                    src.getSelectedIndex());

                        }
                    });
                    objectiveConfigurationPanel.putClientProperty(obj, 0);

                    if (counter == objectives.length)
                        if (140>objectives.length*24)
                            c.insets = new Insets( 0, 0, 140 - objectives.length*24, 0);
                    objectiveConfigurationPanel.add(selectObjectiveMode,c);
                }
                objectiveConfigurationPanel.updateUI();
                mainPanel.validate();
            }
        });

        this.initOptimizerDesc();
        JComboBox selectOptimizer = new JComboBox(optimizerDesc);
        optimizerPanel.add(selectOptimizer,BorderLayout.NORTH);
        selectOptimizer.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e){                
                OptimizerDescription desc = (OptimizerDescription)e.getItem();
                /*if (desc.multiObjective){
                    objectiveList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                }else
                    objectiveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)*/
                optimizerConfigurationPanel.putClientProperty("optimizer", desc);
                optimizerConfigurationPanel.removeAll();
                GridBagConstraints c = new GridBagConstraints();
                int counter = 0;              
                c.gridx = 0;
                c.gridy = counter++;
                c.gridwidth=2;
                c.weightx = 1.0;
                c.weighty = 1.0;
                c.anchor = GridBagConstraints.CENTER;
                c.insets = new Insets(5,0,5,0);
                optimizerConfigurationPanel.add(new JLabel(desc.shortName){
                    {
                        Font f = this.getFont();
                        setFont(f.deriveFont(20.0f));
                    }
                });
                for (OptimizerParameter p : desc.propertyMap){
                    c.gridwidth=1;
                    c.gridx = 0;
                    c.gridy = counter;
                    c.insets = new Insets(0,0,2,0);
                    c.anchor = GridBagConstraints.NORTHWEST;
                    JLabel lbl = new JLabel("<HTML><BODY>"+p.description+"</BODY></HTML>");
                    //lbl.setMaximumSize(new Dimension(225,75));
                    lbl.setVerticalTextPosition(SwingConstants.TOP);
                    lbl.setPreferredSize(new Dimension(440,40));
                    optimizerConfigurationPanel.add(lbl,c);
                    c.gridx = 1;
                    c.gridy = counter++;
                    c.anchor = GridBagConstraints.CENTER;
                    if (counter > desc.propertyMap.size())
                        if (250>desc.propertyMap.size()*40)
                            c.insets = new Insets( 0, 0, 210 - desc.propertyMap.size()*40, 0);
                    if (p instanceof NumericOptimizerParameter){
                        JTextField field = new JTextField(Double.toString(((NumericOptimizerParameter)p).value),5);
                        field.putClientProperty("property", p);
                        field.putClientProperty("mode",new Integer(NumericKeyListener.MODE_PARAMETERVALUE));
                        field.addKeyListener(stdNumericKeyListener);
                        field.addFocusListener(stdFocusListener);
                        optimizerConfigurationPanel.add(field,c);
                        }
                    if (p instanceof BooleanOptimizerParameter){
                        JCheckBox checkBox = new JCheckBox("",((BooleanOptimizerParameter)p).value);
                        checkBox.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e){
                                JCheckBox src = (JCheckBox)e.getSource();
                                BooleanOptimizerParameter p = (BooleanOptimizerParameter)src.getClientProperty("property");
                                if (src.isSelected()){
                                    p.value = true;
                                }else
                                    p.value = false;
                            }
                        });
                        checkBox.putClientProperty("property", p);
                        checkBox.putClientProperty("mode",new Integer(NumericKeyListener.MODE_PARAMETERVALUE));
                        optimizerConfigurationPanel.add(checkBox,c);
                    }
                    if (p instanceof StringOptimizerParameter){
                        JTextField field = new JTextField(((StringOptimizerParameter)p).value,15);
                        field.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e){
                                JTextField src = (JTextField)e.getSource();
                                StringOptimizerParameter p = (StringOptimizerParameter)src.getClientProperty("property");
                                p.value = src.getText();
                            }
                        });
                        field.putClientProperty("property", p);
                        field.putClientProperty("mode",new Integer(NumericKeyListener.MODE_PARAMETERVALUE));
                        optimizerConfigurationPanel.add(field,c);
                    }
                }
                optimizerConfigurationPanel.updateUI();
                mainPanel.updateUI();
                mainPanel.validate();
            }
        });
        selectOptimizer.setSelectedIndex(1);
        return mainPanel;
    }

    public void setWorkspace(String ws){        
        this.workspace = ws.replace("\\", "/");
    }
    public static JFrame createDialog(JUICEFrame parent, Document modelFile, JAMSProperties propertyFile, String workspace){
        JFrame dialog = new JFrame(JAMS.resources.getString("Optimization_Wizard"));
        OptimizationWizard wizard = new OptimizationWizard(parent);
        //dialog
        dialog.getContentPane().add(wizard.getPanel());
        /*wizard.setModel(new File("C:/Arbeit/modeldata/JAMS-Gehlberg/j2k_gehlberg.jam"),
                new File("C:/Arbeit/JAMS/standard.jap"));*/
        wizard.setModel(modelFile, propertyFile);
        wizard.setWorkspace(workspace);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setSize(750,700);
        //dialog.setMaximumSize(new Dimension(750,700));
        dialog.setPreferredSize(new Dimension(750,700));
        dialog.setResizable(false);
        dialog.setAlwaysOnTop(false);
        dialog.pack();
        //dialog.setVisible(true);
        return dialog;
    }

    public static JFrame createDialog(JUICEFrame parent, File modelFile, File propertyFile){
        JFrame dialog = new JFrame(JAMS.resources.getString("Optimization_Wizard"));
        OptimizationWizard wizard = new OptimizationWizard(parent);
        dialog.getContentPane().add(wizard.getPanel());
        /*wizard.setModel(new File("C:/Arbeit/modeldata/JAMS-Gehlberg/j2k_gehlberg.jam"),
                new File("C:/Arbeit/JAMS/standard.jap"));*/
        wizard.setModel(modelFile, propertyFile);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(750,700);
        //dialog.setMaximumSize(new Dimension(750,700));
        //dialog.setPreferredSize(new Dimension(750,700));
        dialog.setResizable(false);
        dialog.pack();
        return dialog;
    }

    public static void main(String arg[]) {
        JDialog dialog = new JDialog();
        OptimizationWizard wizard = new OptimizationWizard();        
        dialog.add(wizard.getPanel());
        wizard.setModel(new File("C:/Arbeit/modeldata/JAMS-Gehlberg/j2k_gehlberg.jam"),
                new File("C:/Arbeit/JAMS/standard.jap"));
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(750,700);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }
    /*public static void main(String arg[]) {
        if (arg.length < 1) {
            System.err.println("error: no arguments");
            return;
        }

        if (arg[0].compareTo("analyze") == 0) {
            if (arg.length < 3) {
                System.err.println("error: not enough arguments");
                return;
            }
            File modelFile = new File(arg[1]);
            String propertyFile = arg[2];
            modelAnalyzer.modelAnalyzer(propertyFile, modelFile);
        } else if (arg[0].compareTo("modify") == 0) {
            if (arg.length < 5) {
                System.err.println("error: not enough arguments");
                return;
            }
            File modelFile = new File(arg[1]);
            String propertyFile = arg[2];
            String optimizationIniFile = arg[3];
            String workspace = arg[4];
            modelModifier.modelModifier(propertyFile, modelFile, optimizationIniFile, workspace);
        } else {
            System.err.println("unknown command: " + arg[0]);
        }
        return;
    }*/
}
