package jamsui.juice.optimizer.wizard;

import jams.data.JAMSDataFactory;
import jams.io.XMLProcessor;
import jams.runtime.StandardRuntime;
import jamsui.juice.optimizer.wizard.Tools.AttributeWrapper;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import jams.JAMSProperties;
import org.w3c.dom.Document;
import jams.JAMS;
import jams.model.Model;
import jams.tools.XMLTools;
import jamsui.juice.optimizer.wizard.Tools.Efficiency;
import jamsui.juice.optimizer.wizard.Tools.Parameter;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.NodeList;

/**
 *
 * @author Christian Fischer
 */
public class modelModifier {

    static public class AttributeDescription {

        String name;
        String value;
        String context;
        boolean isAttribute;

        AttributeDescription(String name, String context, String value, boolean isAttribute) {
            this.name = name;
            this.value = value;
            this.isAttribute = isAttribute;
            this.context = context;
        }
    }

    static public class OptimizerDescription {

        int method;
        ArrayList<Parameter> parameters;
        ArrayList<Efficiency> efficiencies;
        String optimizerClassName;
        ArrayList<AttributeDescription> attributes;
        ArrayList<AttributeWrapper> outputAttributes;
        boolean optimizationRun;
        boolean removeGUIComponents;
        boolean optimizeModelStructure;
        boolean removeUnusedComponents;

        public OptimizerDescription() {
            parameters = new ArrayList<Parameter>();
            efficiencies = new ArrayList<Efficiency>();
            attributes = new ArrayList<AttributeDescription>();
            outputAttributes = new ArrayList<AttributeWrapper>();

        }
    }
    final static int kernelMap[] = {2, 3, 5, 6, 7, 8, 12, 13, 15, 16};

    private static void loadOptimizationFlags(OptimizerDescription desc, Properties props) {
        String strOptimizeModelStructure = props.getProperty("optimizeModelStructure");
        String strRemoveUnusedComponents = props.getProperty("removeUnusedComponents");
        String strRemoveGUIComponents = props.getProperty("removeGUIComponents");

        if (strOptimizeModelStructure != null) {
            if (strOptimizeModelStructure.equals("1")) {
                desc.optimizeModelStructure = true;
            } else {
                desc.optimizeModelStructure = false;
            }
        }
        if (strRemoveUnusedComponents != null) {
            if (strRemoveUnusedComponents.equals("1")) {
                desc.removeUnusedComponents = true;
            } else {
                desc.removeUnusedComponents = false;
            }
        }
        if (strRemoveGUIComponents != null) {
            if (strRemoveGUIComponents.equals("1")) {
                desc.removeGUIComponents = true;
            } else {
                desc.removeGUIComponents = false;
            }
        }
    }

    private static boolean readParameterConfig(OptimizerDescription desc, Properties props, Node root) {
        int parameterCount = 0;
        if (props.getProperty("n") != null) {
            parameterCount = Integer.parseInt(props.getProperty("n"));
        } else {
            System.err.println("error: parameter count not specified");
            return false;
        }
        double lowerBounds[] = new double[parameterCount],
                upperBounds[] = new double[parameterCount],
                startValue[] = new double[parameterCount];

        String parameterNames[] = new String[parameterCount];
        String parameterOwner[] = new String[parameterCount];
        String strLowerBounds = props.getProperty("lowerbounds");
        String strUpperBounds = props.getProperty("upperbounds");
        String strParameterNames = props.getProperty("parameters");

        if (strLowerBounds == null || strUpperBounds == null || strParameterNames == null) {
            System.err.println("error: parameter name, upper or lower bound not specified");
            return false;
        }

        StringTokenizer tok1 = new StringTokenizer(strLowerBounds, ";");
        StringTokenizer tok2 = new StringTokenizer(strUpperBounds, ";");
        StringTokenizer tok3 = new StringTokenizer(strParameterNames, ";");
        if (tok1.countTokens() != parameterCount || tok2.countTokens() != parameterCount || tok3.countTokens() != parameterCount) {
            System.err.println("error: upper or lower bound count does not match parameter count");
            return false;
        }
        for (int i = 0; i < parameterCount; i++) {
            lowerBounds[i] = Double.parseDouble(tok1.nextToken());
            upperBounds[i] = Double.parseDouble(tok2.nextToken());
            StringTokenizer split = new StringTokenizer(tok3.nextToken(), ".");
            parameterOwner[i] = split.nextToken();
            parameterNames[i] = split.nextToken();
        }

        String strStartValue = props.getProperty("startvalues");
        if (strStartValue != null) {
            StringTokenizer tok = new StringTokenizer(strStartValue, ";");
            if (tok.countTokens() != parameterCount) {
                System.err.println("error: start value bound count does not match parameter count");
                return false;
            }
            for (int i = 0; i < parameterCount; i++) {
                startValue[i] = Double.parseDouble(tok.nextToken());
            }
        }

        for (int i = 0; i < parameterCount; i++) {
            String result = jamsui.juice.optimizer.wizard.Tools.getTypeFromNodeName(root, parameterOwner[i]);
            if (result == null) {
                System.err.println("unknown parameter owner " + parameterOwner[i]);
                return false;
            }
            Parameter p = null;
            if (result.equals("jams.model.contextcomponent")) {
                p = new Parameter(new AttributeWrapper(null, parameterNames[i], null, parameterOwner[i]));
            } else if (result.equals("jams.model.component")) {
                p = new Parameter(new AttributeWrapper(parameterNames[i], null, parameterOwner[i], null));
            }
            p.lowerBound = lowerBounds[i];
            p.upperBound = upperBounds[i];
            if (startValue != null) {
                p.startValue = startValue[i];
                p.startValueValid = true;
            } else {
                p.startValueValid = false;
            }
            desc.parameters.add(p);
        }
        return true;
    }

    static private boolean readNSGA2Method(OptimizerDescription desc, Properties props) {
        desc.optimizerClassName = "jams.components.optimizer.NSGA2";
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        String str_populationSize = props.getProperty("popSize");
        String str_crossoverProbability = props.getProperty("crossoverProbability");
        String str_mutationProbability = props.getProperty("mutationProbability");
        String str_crossoverDistributionIndex = props.getProperty("crossoverDistributionIndex");
        String str_mutationDistributionIndex = props.getProperty("mutationDistributionIndex");
        String str_maxGeneration = props.getProperty("maxGeneration");

        int maxn = 0;
        try {
            maxn = Integer.parseInt(str_maximumNumberOfIterations);
        } catch (Exception e) {
        }
        if (maxn < 1) {
            System.err.println(JAMS.resources.getString("error_maxiter_greater_1"));
            return false;
        }
        desc.attributes.add(new AttributeDescription("maxn", null, Integer.toString(maxn), false));

        int popSize = 0;
        try {
            popSize = Integer.parseInt(str_populationSize);
        } catch (Exception e) {
        }
        if (popSize < 1) {
            System.err.println(JAMS.resources.getString("population_size_must_be_positive"));
            return false;
        }
        desc.attributes.add(new AttributeDescription("populationSize", null, Integer.toString(popSize), false));

        double crossoverProbability = 0;
        try {
            crossoverProbability = Double.parseDouble(str_crossoverProbability);
        } catch (Exception e) {
        }
        if (crossoverProbability < 0 || crossoverProbability > 1) {
            System.err.println(JAMS.resources.getString("crossoverProbability_must_be_between_0_and_1"));
            return false;
        }
        double mutationProbability = 0;
        try {
            mutationProbability = Double.parseDouble(str_mutationProbability);
        } catch (Exception e) {
        }
        if (mutationProbability < 0.5 || mutationProbability > 1) {
            System.err.println(JAMS.resources.getString("mutationProbability_must_be_between_05_and_1"));
            return false;
        }
        double crossoverDistributionIndex = 0;
        try {
            crossoverDistributionIndex = Double.parseDouble(str_crossoverDistributionIndex);
        } catch (Exception e) {
        }
        if (crossoverDistributionIndex < 0.5 || crossoverDistributionIndex > 100) {
            System.err.println(JAMS.resources.getString("crossoverDistributionIndex_must_be_between_05_and_100"));
            return false;
        }
        double mutationDistributionIndex = 0;
        try {
            mutationDistributionIndex = Double.parseDouble(str_mutationDistributionIndex);
        } catch (Exception e) {
        }
        if (mutationDistributionIndex < 0.5 || mutationDistributionIndex > 100) {
            System.err.println(JAMS.resources.getString("mutationDistributionIndex_must_be_between_05_and_100"));
            return false;
        }
        double maxGeneration = 0;
        try {
            maxGeneration = Double.parseDouble(str_maxGeneration);
        } catch (Exception e) {
        }
        if (maxGeneration < 1) {
            System.err.println(JAMS.resources.getString("maxGeneration_must_be_positive"));
            return false;
        }
        desc.attributes.add(new AttributeDescription("populationSize", null, Integer.toString(popSize), false));
        return true;
    }

    static private boolean readMOCOMMethod(OptimizerDescription desc, Properties props) {
        desc.optimizerClassName = "jams.components.optimizer.MOCOM";
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        String str_populationSize = props.getProperty("popSize");
        int maxn = 0;
        try {
            maxn = Integer.parseInt(str_maximumNumberOfIterations);
        } catch (Exception e) {
        }
        if (maxn < 1) {
            System.err.println(JAMS.resources.getString("error_maxiter_greater_1"));
            return false;
        }
        desc.attributes.add(new AttributeDescription("maxn", null, Integer.toString(maxn), false));

        int popSize = 0;
        try {
            popSize = Integer.parseInt(str_populationSize);
        } catch (Exception e) {
        }
        if (popSize < 1) {
            System.err.println(JAMS.resources.getString("population_size_must_be_positive"));
            return false;
        }
        desc.attributes.add(new AttributeDescription("populationSize", null, Integer.toString(popSize), false));
        return true;
    }

    static private boolean readGutmannMethod(OptimizerDescription desc, Properties props) {
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        desc.optimizerClassName = "jams.components.optimizer.GutmannMethod";
        int maxn = 500;
        try {
            maxn = Integer.parseInt(str_maximumNumberOfIterations);
        } catch (Exception e) {
        }
        if (maxn < 1) {
            System.err.println(JAMS.resources.getString("error_maxiter_greater_1"));
            return false;
        }
        desc.attributes.add(new AttributeDescription("maxn", null, Integer.toString(maxn), false));
        return true;
    }

    static private boolean readBranchAndBoundMethod(OptimizerDescription desc, Properties props) {
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        desc.optimizerClassName = "jams.components.optimizer.BranchAndBound";
        int maxn = 500;
        try {
            maxn = Integer.parseInt(str_maximumNumberOfIterations);
        } catch (Exception e) {
        }
        if (maxn < 1) {
            System.err.println(JAMS.resources.getString("error_maxiter_greater_1"));
            return false;
        }
        desc.attributes.add(new AttributeDescription("maxn", null, Integer.toString(maxn), false));
        return true;
    }

    static private boolean readGPSearchMethod(OptimizerDescription desc, Properties props) {
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        String str_kernelMethod = props.getProperty("kernelMethod");
        desc.optimizerClassName = "jams.components.optimizer.GPSearch";
        int maxn = 0, kernel = 1;
        try {
            maxn = Integer.parseInt(str_maximumNumberOfIterations);
            kernel = Integer.parseInt(str_kernelMethod);
        } catch (Exception e) {
        }
        if (maxn < 1) {
            System.err.println(JAMS.resources.getString("error_maxiter_greater_1"));
            return false;
        }
        desc.attributes.add(new AttributeDescription("maxn", null, Integer.toString(maxn), false));
        desc.attributes.add(new AttributeDescription("GPMethod", null, Integer.toString(kernel), false));
        return true;
    }

    static private boolean readRandomSamplerMethod(OptimizerDescription desc, Properties props, boolean isParallel) {
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        String str_excludedFiles = props.getProperty("fileFilter");
        
        if (isParallel)
            desc.optimizerClassName = "jams.parallel.optimizer.ParallelRandomSampler";
        else
            desc.optimizerClassName = "jams.components.optimizer.RandomSampler";
        int maxn = 500;
        try {
            maxn = Integer.parseInt(str_maximumNumberOfIterations);
        } catch (Exception e) {
        }
        if (maxn < 1) {
            System.err.println(JAMS.resources.getString("error_maxiter_greater_1"));
            return false;
        }        
        try {
            if (isParallel) {
                if (str_excludedFiles == null) {
                    str_excludedFiles = "";
                }
                Pattern.compile(str_excludedFiles);
                desc.attributes.add(new AttributeDescription("excludeFiles", null, str_excludedFiles, false));
            }
        } catch (PatternSyntaxException pse) {
            System.err.println(JAMS.resources.getString("There_is_a_problem_with_the_regular_expression!") + "\n" +
                    JAMS.resources.getString("The_pattern_in_question_is") + ": " + pse.getPattern() + "\n" +
                    JAMS.resources.getString("The_description_is") + ": " + pse.getDescription() + "\n");
            return false;
        }
        desc.attributes.add(new AttributeDescription("maxn", null, Integer.toString(maxn), false));        
        return true;
    }

    static private boolean readNelderMeadMethod(OptimizerDescription desc, Properties props) {
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        desc.optimizerClassName = "jams.components.optimizer.NelderMead";
        int maxn = 500;
        try {
            maxn = Integer.parseInt(str_maximumNumberOfIterations);
        } catch (Exception e) {
        }
        if (maxn < 1) {
            System.err.println(JAMS.resources.getString("error_maxiter_greater_1"));
            return false;
        }
        desc.attributes.add(new AttributeDescription("maxn", null, Integer.toString(maxn), false));
        return true;
    }
    
    static private boolean readSCEMethod(OptimizerDescription desc, Properties props, boolean parallel) {
        if (parallel) {
            desc.optimizerClassName = "jams.parallel.optimizer.SimpleParallelSCE";
        } else {
            desc.optimizerClassName = "jams.components.optimizer.SimpleSCE";
        }
        String str_numberOfComplexes = props.getProperty("numberOfComplexes");
        String str_pcento = props.getProperty("pcento");
        String str_peps = props.getProperty("prange");
        String str_kstop = props.getProperty("kstop");
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        String str_excludedFiles = props.getProperty("excludedFiles");

        int numOfComplexes = 2;
        double pcento = 0.1;
        double peps = 0;
        int kstop = 0;
        int maxn = 0;

        try {
            numOfComplexes = Integer.parseInt(str_numberOfComplexes);
            pcento = Double.parseDouble(str_pcento);
            peps = Double.parseDouble(str_peps);
            kstop = Integer.parseInt(str_kstop);
            maxn = Integer.parseInt(str_maximumNumberOfIterations);
        } catch (Exception e) {
        }

        if (numOfComplexes < 1 || numOfComplexes > 100) {
            System.err.println(JAMS.resources.getString("number_of_complexes_have_to_be_an_integer_between_1_and_100"));
            return false;
        }
        if (pcento < 0 || pcento > 1) {
            System.err.println(JAMS.resources.getString("value_of_pcento_have_to_be_between_0_and_1"));
            return false;
        }
        if (peps < 0 || peps > 1) {
            System.err.println(JAMS.resources.getString("value_of_peps_have_to_be_between_0_and_1"));
            return false;
        }
        if (kstop < 1 || kstop > 100) {
            System.err.println(JAMS.resources.getString("kstop_have_to_be_an_integer_between_1_and_100"));
            return false;
        }
        if (maxn < 1) {
            System.err.println(JAMS.resources.getString("error_maxiter_greater_1"));
            return false;
        }
        try {
            if (parallel) {
                if (str_excludedFiles == null) {
                    str_excludedFiles = "";
                }
                Pattern.compile(str_excludedFiles);
                desc.attributes.add(new AttributeDescription("excludeFiles", null, str_excludedFiles, false));
            }
        } catch (PatternSyntaxException pse) {
            System.err.println(JAMS.resources.getString("There_is_a_problem_with_the_regular_expression!") + "\n" +
                    JAMS.resources.getString("The_pattern_in_question_is") + ": " + pse.getPattern() + "\n" +
                    JAMS.resources.getString("The_description_is") + ": " + pse.getDescription() + "\n");
            return false;
        }
        desc.attributes.add(new AttributeDescription("NumberOfComplexes", null, Integer.toString(numOfComplexes), false));
        desc.attributes.add(new AttributeDescription("pcento", null, Double.toString(pcento), false));
        desc.attributes.add(new AttributeDescription("peps", null, Double.toString(peps), false));
        desc.attributes.add(new AttributeDescription("kstop", null, Integer.toString(kstop), false));
        desc.attributes.add(new AttributeDescription("maxn", null, Integer.toString(maxn), false));        
        return true;
    }

    static private boolean readOptimizerMethod(OptimizerDescription desc, Properties props) {
        //read method
        if (props.getProperty("method") != null) {
            desc.method = Integer.parseInt(props.getProperty("method"));
        } else {
            System.err.println("error: optimization method not specified");
            return false;
        }
        switch (desc.method) {
            case 0: {
                if (!readBranchAndBoundMethod(desc, props)) {
                    System.err.println("invalid Branch&Bound method configuration");
                    return false;
                }
                break;
            }
            case 1: {
                if (!readGPSearchMethod(desc, props)) {
                    System.err.println("invalid GPSearch method configuration");
                    return false;
                }
                break;
            }
            case 2: {
                if (!readNelderMeadMethod(desc, props)) {
                    System.err.println("invalid Nelder Mead method configuration");
                    return false;
                }
                break;
            }
            case 3: {
                if (!readSCEMethod(desc, props, true)) {
                    System.err.println("invalid Parallel - SCE method configuration");
                    return false;
                }
                break;
            }
            case 4: {
                if (!readGutmannMethod(desc, props)) {
                    System.err.println("invalid Gutmann/RBF method configuration");
                    return false;
                }
                break;
            }
            case 5: {
                if (!readSCEMethod(desc, props, false)) {
                    System.err.println("invalid SCE method configuration");
                    return false;
                }
                break;
            }
            case 6: {
                if (!readMOCOMMethod(desc, props)) {
                    System.err.println("invalid MOCOM method configuration");
                    return false;
                }
                break;
            }
            case 7: {
                if (!readRandomSamplerMethod(desc, props, true)) {
                    System.err.println("invalid P-SCE method configuration");
                    return false;
                }
                //desc.optimizerClassName = "jams.parallel.optimizer.ParallelRandomSampler";                
                break;
            }
            case 8: {
                if (!readRandomSamplerMethod(desc, props, false)) {
                    System.err.println("invalid RandomSampler method configuration");
                    return false;
                }
                break;
            }
            case 9: {
                if (!readNSGA2Method(desc, props)) {
                    System.err.println("invalid NSGA2 method configuration");
                    return false;
                }
                break;
            }
        }
        return true;
    }

    private static boolean readObjectiveConfig(OptimizerDescription desc, Properties props, Node root) {
        String strObjective = props.getProperty("efficiencies");
        String strObjectiveModes = props.getProperty("efficiency_modes");
        if (strObjective == null || strObjectiveModes == null) {
            System.err.println("error: no objective specified");
            return false;
        }
        StringTokenizer tok1 = new StringTokenizer(strObjective, ";");
        StringTokenizer tok2 = new StringTokenizer(strObjectiveModes, ";");
        int objectiveCount = tok1.countTokens();
        if (tok2.countTokens() != objectiveCount) {
            System.err.println("error: objective count does not match objecitve mode count");
            return false;
        }

        String objectiveName[] = new String[objectiveCount];
        String objectiveContext[] = new String[objectiveCount];
        int objectiveModes[] = new int[objectiveCount];

        for (int i = 0; i < objectiveCount; i++) {
            StringTokenizer split = new StringTokenizer(tok1.nextToken(), ".");
            objectiveContext[i] = split.nextToken();
            objectiveName[i] = split.nextToken();
            objectiveModes[i] = Integer.parseInt(tok2.nextToken());

            String result = jamsui.juice.optimizer.wizard.Tools.getTypeFromNodeName(root, objectiveContext[i]);
            if (result == null) {
                System.err.println("unknown objective owner " + objectiveContext[i]);
                return false;
            }
            Efficiency p = null;
            if (result.equals("jams.model.contextcomponent")) {
                p = new Efficiency(new AttributeWrapper(null, objectiveName[i], null, objectiveContext[i]));
            } else if (result.equals("jams.model.component")) {
                p = new Efficiency(new AttributeWrapper(objectiveName[i], null, objectiveContext[i], null));
            }
            p.mode = objectiveModes[i];
            desc.efficiencies.add(p);
        }
        String effValueString = "";
        String effMethodNameString = "";
        String effModeString = "";
        for (int i=0;i<desc.efficiencies.size();i++){
            effValueString += desc.efficiencies.get(i).attributeName;                
            effMethodNameString += desc.efficiencies.get(i).attributeName;
            effModeString += Integer.toString(desc.efficiencies.get(i).mode);
            if (i < desc.efficiencies.size()-1){
                effValueString += ";";
                effMethodNameString += ";";
                effModeString += ";";
            }                
        }
        desc.attributes.add(new AttributeDescription("effValue", "optimizer", effValueString, true));
        desc.attributes.add(new AttributeDescription("effMethodName", null, effMethodNameString, false));
        desc.attributes.add(new AttributeDescription("mode", null, effModeString, false));    
        
        return true;
    }

    private static boolean readOutputAttributeConfig(OptimizerDescription desc, Properties props, Node root) {
        int outputAttrCount = 0;
        String outputAttrName[] = null;
        String outputAttrContext[] = null;
        String strOutputAttr = props.getProperty("outputAttr");

        if (strOutputAttr != null) {
            StringTokenizer tok = new StringTokenizer(strOutputAttr, ";");
            outputAttrCount = tok.countTokens();
            outputAttrName = new String[outputAttrCount];
            outputAttrContext = new String[outputAttrCount];
            for (int i = 0; i < outputAttrCount; i++) {
                StringTokenizer split = new StringTokenizer(tok.nextToken(), ".");
                outputAttrContext[i] = split.nextToken();
                outputAttrName[i] = split.nextToken();

                String result = jamsui.juice.optimizer.wizard.Tools.getTypeFromNodeName(root, outputAttrContext[i]);
                if (result == null) {
                    System.err.println("unknown objective owner " + outputAttrContext[i]);
                    return false;
                }
                if (result.equals("jams.model.contextcomponent")) {
                    desc.outputAttributes.add(new AttributeWrapper(null, outputAttrName[i], null, outputAttrContext[i]));
                } else if (result.equals("jams.model.component")) {
                    desc.outputAttributes.add(new AttributeWrapper(outputAttrName[i], null, outputAttrContext[i], null));
                }
            }
        }
        return true;
    }

    public static void buildConfigurationString(OptimizerDescription desc) {
        //build param string
        String param_string = "";
        for (int i = 0; i < desc.parameters.size(); i++) {
            if (desc.parameters.get(i).variableName != null) {
                param_string += desc.parameters.get(i).variableName + ";";
            } else {
                param_string += desc.parameters.get(i).attributeName + ";";
            }
        }
        desc.attributes.add(new AttributeDescription("parameterIDs", "optimizer", param_string, true));
        //build boundary string
        String boundary_string = "";
        for (int i = 0; i < desc.parameters.size(); i++) {
            boundary_string += "[" + desc.parameters.get(i).lowerBound + ">" + desc.parameters.get(i).upperBound + "];";
        }

        desc.attributes.add(new AttributeDescription("boundaries", null, boundary_string, false));
        //build startvalue string
        String startvalue_string = "";
        boolean validStartValue = true;
        for (int i = 0; i < desc.parameters.size(); i++) {
            if (desc.parameters.get(i).startValueValid) {
                startvalue_string += desc.parameters.get(i).startValue + ";";
            } else {
                validStartValue = false;
                break;
            }
        }
        if (validStartValue) {
            desc.attributes.add(new AttributeDescription("startValue", null, startvalue_string, false));
        }
    }

    public static OptimizerDescription loadOptimizerIni(Node root, String optimizerIni) {
        OptimizerDescription desc = new OptimizerDescription();
        desc.attributes.add(new AttributeDescription("enable", null, "true", false));

        Properties props = new Properties();
        try {
            props.load(new FileReader(optimizerIni));
        } catch (Exception e) {
            System.err.println("optimizer description file " + optimizerIni + " not found");
            return null;
        }
        loadOptimizationFlags(desc, props);
        desc.optimizationRun = false;

        //read job mode        
        if (props.getProperty("jobMode") == null || !props.getProperty("jobMode").equals("optimizationRun")) {
            return desc;
        }
        desc.optimizationRun = true;
        if (!readParameterConfig(desc, props, root)) {
            System.err.println("error: invalid parameter setup");
            return null;
        }
        if (!readObjectiveConfig(desc, props, root)) {
            System.err.println("error: invalid parameter setup");
            return null;
        }
        if (!readOptimizerMethod(desc, props)) {
            System.err.println("error: invalid method setup");
            return null;
        }
        if (!readOutputAttributeConfig(desc, props, root)) {
            System.err.println("error: invalid output attribute setup");
            return null;
        }
        buildConfigurationString(desc);

        return desc;
    }

    public static boolean configOutput(OptimizerDescription desc, String optimizerContextName, String workspace) {
        Map<String, HashSet<String>> outputContexts = new HashMap<String, HashSet<String>>();
        //parameter and efficiencies are set by default
        for (int i = 0; i < desc.parameters.size(); i++) {
            Parameter attrDesc = desc.parameters.get(i);
            if (attrDesc.attributeName != null) {
                desc.outputAttributes.add(new AttributeWrapper(null, attrDesc.attributeName, null, optimizerContextName));
            } else //this works because, modelModifier will replace all parameter variables by attributes with the same name
            {
                desc.outputAttributes.add(new AttributeWrapper(null, attrDesc.variableName, null, optimizerContextName));
            }
        }
        for (int i = 0; i < desc.efficiencies.size(); i++) {
            Efficiency attrDesc = desc.efficiencies.get(i);
            desc.outputAttributes.add(new AttributeWrapper(null, attrDesc.attributeName, null, optimizerContextName));
        }

        for (int i = 0; i < desc.outputAttributes.size(); i++) {
            String attr = desc.outputAttributes.get(i).attributeName;
            if (attr != null) {
                String context = desc.outputAttributes.get(i).contextName;
                if (context != null) {
                    if (!outputContexts.containsKey(context)) {
                        outputContexts.put(context, new HashSet<String>());
                    }
                    outputContexts.get(context).add(attr);
                }
            }
        }
        Iterator<String> iter = outputContexts.keySet().iterator();

        while (iter.hasNext()) {
            String context = iter.next();

            Document outputDoc = XMLTools.createDocument();
            Element root = outputDoc.createElement("outputdatastore");
            root.setAttribute("context", context);
            outputDoc.appendChild(root);

            Element trace = outputDoc.createElement("trace");
            root.appendChild(trace);

            HashSet<String> attr = outputContexts.get(context);
            Iterator<String> attrIter = attr.iterator();
            while (attrIter.hasNext()) {
                Element attrElement = outputDoc.createElement("attribute");
                attrElement.setAttribute("id", attrIter.next());
                trace.appendChild(attrElement);
            }

            try {
                XMLTools.writeXmlFile(outputDoc, workspace + File.separator + "output" + File.separator + "optimization_wizard_" + context + ".xml");
            } catch (Exception e) {
                System.err.println(JAMS.resources.getString("Error_cant_write_xml_file_because_") + e.toString());
                return false;
            }
        }
        return true;
    }

    final static String fileVarList[] = {"reachFileName","hruFileName","luFileName","stFileName","gwFileName","shapeFileName","shapeFileName1",
    "stylesFileName","shapeFileName1","heightMap"};
    
    public static void doAdjustments(Node root){
        if (root.getNodeName().equals("var") || root.getNodeName().equals("attribute")) {
            Element elem = (Element) root;
            if (elem.hasAttribute("name") && elem.hasAttribute("value")){
                String name  = elem.getAttribute("name");
                String value = elem.getAttribute("value");
                
                if (name.equals("data_caching")){
                    elem.setAttribute("value", "2");
                }
                                
                for (int i=0;i<fileVarList.length;i++){
                    if (fileVarList[i].equals(name)){
                        elem.setAttribute("value", value.replace("\\", "/"));
                    }
                }
            }
        }
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            doAdjustments(childs.item(i));
        }
    }
    
    public static boolean changeWorkspace(Node root, String newWorkspace) {
        if (root.getNodeName().equals("var")) {
            Element elem = (Element) root;
            if (elem.hasAttribute("name")) {
                String varName = elem.getAttribute("name");
                if (varName.equals("workspaceDirectory")) {
                    elem.setAttribute("value", newWorkspace);
                    return true;
                }
            }
        }
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            if (changeWorkspace(childs.item(i), newWorkspace)) {
                return true;
            }
        }
        return false;
    }

    public static void modelModifier(String propertyFile, File modelFile, String optimizerIni, String workspace) {
        String optimizerContextName = "optimizer";
        String infoLog = "";
        JAMSProperties properties = JAMSProperties.createProperties();
        HashSet<String> removedComponents = new HashSet<String>();
        StandardRuntime rt = new StandardRuntime();
        DocumentLoader loader = new DocumentLoader();
        loader.modelFile = JAMSDataFactory.createString();
        loader.modelFile.setValue(modelFile.getName());
        loader.workspaceDir = JAMSDataFactory.createString();
        loader.workspaceDir.setValue(workspace);
        loader.modelDoc = JAMSDataFactory.createDocument();

        String errorString = loader.init_withResponse();
        Document loadedModel = loader.modelDoc.getValue();
        if (loadedModel == null) {
            System.err.println(errorString);
            return;
        }
        changeWorkspace(loadedModel, workspace);
        try {
            properties.load(propertyFile);
        } catch (IOException e) {
            System.err.println("Cant find property file, because:" + e.toString());
        } catch (Exception e2) {
            System.err.println("Error while loading property file, because: " + e2.toString());
        }

        rt.loadModel(loadedModel, properties);
        if (rt.getDebugLevel() >= 3) {
            System.err.println(rt.getErrorLog());
            System.out.println(rt.getInfoLog());
        }

        OptimizerDescription desc = loadOptimizerIni(loadedModel, optimizerIni);
        if (desc == null){
            return;
        }
        Model model = rt.getModel();
        //1. schritt
        //parameter relevante componenten verschieben                
        infoLog += JAMS.resources.getString("create_transitive_hull_of_dependency_graph") + "\n";
        Hashtable<String, HashSet<String>> dependencyGraph = jams.model.metaoptimizer.metaModelOptimizer.getDependencyGraph(loadedModel.getDocumentElement(), model);
        Hashtable<String, HashSet<String>> transitiveClosureOfDependencyGraph =
                jams.model.metaoptimizer.metaModelOptimizer.TransitiveClosure(dependencyGraph);

        Document doc = (Document) loadedModel.cloneNode(true);
        Node root = (Node) doc.getDocumentElement();

        if (desc.removeGUIComponents || !desc.optimizationRun) {
            infoLog = JAMS.resources.getString("removing_GUI_components") + ":\n";
            ArrayList<String> removedGUIComponents = jams.model.metaoptimizer.metaModelOptimizer.RemoveGUIComponents(root);
            for (int i = 0; i < removedGUIComponents.size(); i++) {
                infoLog += "    ***" + removedGUIComponents.get(i) + "\n";
            }
            removedComponents.addAll(removedGUIComponents);
        }
        if (desc.removeUnusedComponents) {
            HashSet<String> effWritingComponents = new HashSet<String>();
            for (int i = 0; i < desc.efficiencies.size(); i++) {
                effWritingComponents.addAll(
                        jams.model.metaoptimizer.metaModelOptimizer.CollectAttributeWritingComponents(
                        (Node) doc.getDocumentElement(),
                        model,
                        desc.efficiencies.get(i).attributeName,
                        desc.efficiencies.get(i).contextName));
            }

            ArrayList<String> removedUnusedComponents = jams.model.metaoptimizer.metaModelOptimizer.RemoveNotListedComponents(root,
                    jams.model.metaoptimizer.metaModelOptimizer.GetRelevantComponentsList(transitiveClosureOfDependencyGraph,
                    effWritingComponents));

            infoLog += JAMS.resources.getString("removing_components_without_relevant_influence") + ":\n";
            for (int i = 0; i < removedUnusedComponents.size(); i++) {
                infoLog += "    ***" + removedUnusedComponents.get(i) + "\n";
                removedComponents.addAll(removedUnusedComponents);
            }
        }
        if (desc.optimizationRun) {
            infoLog += JAMS.resources.getString("add_optimization_context") + "\n";
            //optimierer bauen
            Element optimizerContext = doc.createElement("contextcomponent");
            optimizerContext.setAttribute("class", desc.optimizerClassName);
            optimizerContext.setAttribute("name", optimizerContextName);

            Iterator<AttributeDescription> iter = desc.attributes.iterator();
            while (iter.hasNext()) {
                AttributeDescription attr = iter.next();
                jamsui.juice.optimizer.wizard.Tools.addAttribute(optimizerContext, attr.name, attr.value, attr.context, !attr.isAttribute);
            }

            jamsui.juice.optimizer.wizard.Tools.addParameters(desc.parameters, root, optimizerContextName);
            jamsui.juice.optimizer.wizard.Tools.addEfficiencies(desc.efficiencies, root, optimizerContextName);

            infoLog += JAMS.resources.getString("find_a_position_to_place_optimizer") + "\n";
            //find place for optimization context
            Node firstComponent = XMLProcessor.getFirstComponent(root);
            if (firstComponent == null) {
                System.err.println(JAMS.resources.getString("Error_model_file_does_not_contain_any_components"));
                return;
            }
            //collect all following siblings of firstComponent and add them to contextOptimizer
            Node currentNode = firstComponent;
            ArrayList<Node> followingNodes = new ArrayList<Node>();
            while (currentNode.getNextSibling() != null) {
                followingNodes.add(currentNode);
                currentNode = currentNode.getNextSibling();
            }

            if (firstComponent.getParentNode() == null) {
                System.err.println(JAMS.resources.getString("Error_model_file_does_not_contain_a_model_context"));
                return;
            }

            Node modelContext = firstComponent.getParentNode();
            for (int i = 0; i < followingNodes.size(); i++) {
                modelContext.removeChild(followingNodes.get(i));
                optimizerContext.appendChild(followingNodes.get(i));
            }

            modelContext.appendChild(optimizerContext);

            doc.removeChild(doc.getDocumentElement());
            doc.appendChild(root);
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("model_info.dat"));
            writer.write(infoLog);
            writer.close();
        } catch (Exception e) {
        }
        if (!configOutput(desc, optimizerContextName, workspace)) {
            System.err.println("error: could not configure output!");
            return;
        }
        //show model graph
        jams.model.metaoptimizer.metaModelOptimizer.ExportGDLFile(dependencyGraph, removedComponents, "model.gdl");

        //some adjustments like file separators and data-caching
        doAdjustments(doc);
        try {
            XMLTools.writeXmlFile(doc, "optimization.jam");
        } catch (Exception e) {
            return;
        }
        rt.sendHalt();
        System.exit(0);
    }
}
