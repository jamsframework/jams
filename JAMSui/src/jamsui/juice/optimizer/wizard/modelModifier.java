package jamsui.juice.optimizer.wizard;

import jams.data.JAMSDataFactory;
import jams.io.XMLProcessor;
import jams.runtime.StandardRuntime;
import jamsui.juice.optimizer.wizard.Tools.AttributeWrapper;
import java.io.BufferedWriter;
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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.NodeList;

/**
 *
 * @author Christian Fischer
 */
public class modelModifier {

    static final int BRANCH_AND_BOUND_METHOD = 0;
    static final int GPSEARCH_METHOD = 1;
    static final int NELDERMEAD_METHOD = 2;
    static final int PARALLEL_SCE_METHOD = 3;
    static final int GUTMANN_METHOD = 4;
    static final int SCE_METHOD = 5;
    static final int MOCOM_METHOD = 6;
    static final int PARALLEL_RANDOMSAMPLER_METHOD = 7;
    static final int RANDOMSAMPLER_METHOD = 8;
    static final int NSGA2_METHOD = 9;
    static final int DIRECT_METHOD = 10;
    static final int LATINHYPERCUBE_METHOD = 11;
    static final int MULTIPOINT_RANDOMSAMPLER_METHOD = 12;

    StandardRuntime rt;
    Document loadedModel;
    JAMSProperties properties;

    Hashtable<String, HashSet<String>> dependencyGraph;
    Hashtable<String, HashSet<String>> transitiveClosureOfDependencyGraph;
    HashSet<String> removedComponents = new HashSet<String>();

    InputStream optimizerIniStream;

    static public class WizardException extends Exception{
        String e;

        WizardException(String desc){
            e = desc;
        }

        @Override
        public String toString(){
            return e;
        }
    }

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

        boolean changeWorkspace;
        String newWorkspace;
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

    private static void readParameterConfig(OptimizerDescription desc, Properties props, Node root) throws WizardException{
        int parameterCount = 0;
        if (props.getProperty("n") != null) {
            parameterCount = (int)Double.parseDouble(props.getProperty("n"));
        } else {
            throw new WizardException(JAMS.i18n("error_parameter_count_not_specified"));
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
            throw new WizardException(JAMS.i18n("error_parameter_name_upper_or_lower_bound_not_specified"));
        }

        StringTokenizer tok1 = new StringTokenizer(strLowerBounds, ";");
        StringTokenizer tok2 = new StringTokenizer(strUpperBounds, ";");
        StringTokenizer tok3 = new StringTokenizer(strParameterNames, ";");
        if (tok1.countTokens() != parameterCount || tok2.countTokens() != parameterCount || tok3.countTokens() != parameterCount) {
            throw new WizardException(JAMS.i18n("error_upper_or_lower_bound_count_does_not_match_parameter_count"));
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
                throw new WizardException(JAMS.i18n("error_start_value_bound_count_does_not_match_parameter_count"));
            }
            for (int i = 0; i < parameterCount; i++) {
                startValue[i] = Double.parseDouble(tok.nextToken());
            }
        }else
            startValue = null;

        for (int i = 0; i < parameterCount; i++) {
            String result = jamsui.juice.optimizer.wizard.Tools.getTypeFromNodeName(root, parameterOwner[i]);
            if (result == null) {
                throw new WizardException(JAMS.i18n("unknown parameter owner " + parameterOwner[i]));
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
    }

    static private void readNSGA2Method(OptimizerDescription desc, Properties props) throws WizardException, NumberFormatException, NullPointerException{
        desc.optimizerClassName = "jams.components.optimizer.NSGA2";
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        String str_populationSize = props.getProperty("popSize");
        String str_crossoverProbability = props.getProperty("crossoverProbability");
        String str_mutationProbability = props.getProperty("mutationProbability");
        String str_crossoverDistributionIndex = props.getProperty("crossoverDistributionIndex");
        String str_mutationDistributionIndex = props.getProperty("mutationDistributionIndex");
        String str_maxGeneration = props.getProperty("maxGeneration");

        int maxn = (int)Double.parseDouble(str_maximumNumberOfIterations);
        if (maxn < 1) throw new WizardException(JAMS.i18n("error_maxiter_greater_1"));

        int popSize = (int)Double.parseDouble(str_populationSize);
        if (popSize < 1)  throw new WizardException(JAMS.i18n("population_size_must_be_positive"));

        double mutationProbability = Double.parseDouble(str_mutationProbability);
        if (mutationProbability < 0.5 || mutationProbability > 1)
            throw new WizardException(JAMS.i18n("mutationProbability_must_be_between_05_and_1"));

        double crossoverProbability = Double.parseDouble(str_crossoverProbability);
        if (crossoverProbability < 0 || crossoverProbability > 1)
            throw new WizardException(JAMS.i18n("crossoverProbability_must_be_between_0_and_1"));

        double crossoverDistributionIndex = Double.parseDouble(str_crossoverDistributionIndex);
        if (crossoverDistributionIndex < 0.5 || crossoverDistributionIndex > 100)
            throw new WizardException(JAMS.i18n("crossoverDistributionIndex_must_be_between_05_and_100"));

        double mutationDistributionIndex = Double.parseDouble(str_mutationDistributionIndex);
        if (mutationDistributionIndex < 0.5 || mutationDistributionIndex > 100)
            throw new WizardException(JAMS.i18n("mutationDistributionIndex_must_be_between_05_and_100"));

        double maxGeneration = Double.parseDouble(str_maxGeneration);
        if (maxGeneration < 1)
            throw new WizardException(JAMS.i18n("maxGeneration_must_be_positive"));

        desc.attributes.add(new AttributeDescription("maxn", null, Integer.toString(maxn), false));        
        desc.attributes.add(new AttributeDescription("populationSize", null, Integer.toString(popSize), false));  
        desc.attributes.add(new AttributeDescription("crossoverProbability", null, Double.toString(crossoverProbability), false));
        desc.attributes.add(new AttributeDescription("mutationProbability", null, Double.toString(mutationProbability), false));
        desc.attributes.add(new AttributeDescription("crossoverDistributionIndex", null, Double.toString(crossoverDistributionIndex), false));
        desc.attributes.add(new AttributeDescription("mutationDistributionIndex", null, Double.toString(mutationDistributionIndex), false));
        desc.attributes.add(new AttributeDescription("maxGeneration", null, Integer.toString((int)maxGeneration), false));
    }

    static private void readMOCOMMethod(OptimizerDescription desc, Properties props) throws WizardException, NumberFormatException, NullPointerException{
        desc.optimizerClassName = "jams.components.optimizer.MOCOM";
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        String str_populationSize = props.getProperty("popSize");
        int maxn = (int)Double.parseDouble(str_maximumNumberOfIterations);
        if (maxn < 1) throw new WizardException(JAMS.i18n("error_maxiter_greater_1"));

        int popSize = (int)Double.parseDouble(str_populationSize);
        if (popSize < 1) throw new WizardException(JAMS.i18n("population_size_must_be_positive"));

        desc.attributes.add(new AttributeDescription("maxn", null, Integer.toString(maxn), false));
        desc.attributes.add(new AttributeDescription("populationSize", null, Integer.toString(popSize), false));
    }

    static private void readGutmannMethod(OptimizerDescription desc, Properties props) throws WizardException, NumberFormatException, NullPointerException {
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        desc.optimizerClassName = "jams.components.optimizer.GutmannMethod";
        int maxn = (int)Double.parseDouble(str_maximumNumberOfIterations);;
        if (maxn < 1) throw new WizardException(JAMS.i18n("error_maxiter_greater_1"));
        desc.attributes.add(new AttributeDescription("maxn", null, Integer.toString(maxn), false));
    }

    static private void readDirectMethod(OptimizerDescription desc, Properties props) throws WizardException, NumberFormatException, NullPointerException{
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        desc.optimizerClassName = "jams.components.optimizer.Direct";
        int maxn = (int)Double.parseDouble(str_maximumNumberOfIterations);;
        if (maxn < 1) throw new WizardException(JAMS.i18n("error_maxiter_greater_1"));
    }

    static private void readBranchAndBoundMethod(OptimizerDescription desc, Properties props) throws WizardException, NumberFormatException, NullPointerException{
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        desc.optimizerClassName = "jams.components.optimizer.BranchAndBound";
        int maxn = (int)Double.parseDouble(str_maximumNumberOfIterations);;
        if (maxn < 1) throw new WizardException(JAMS.i18n("error_maxiter_greater_1"));
    }

    static private void readGPSearchMethod(OptimizerDescription desc, Properties props) throws WizardException, NumberFormatException, NullPointerException{
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        String str_kernelMethod = props.getProperty("kernelMethod");
        desc.optimizerClassName = "jams.components.optimizer.GPSearch";
        int maxn = (int)Double.parseDouble(str_maximumNumberOfIterations),
            kernel = (int)Double.parseDouble(str_kernelMethod);
        if (maxn < 1) throw new WizardException(JAMS.i18n("error_maxiter_greater_1"));
       
        desc.attributes.add(new AttributeDescription("maxn", null, Integer.toString(maxn), false));
        desc.attributes.add(new AttributeDescription("GPMethod", null, Integer.toString(kernel), false));
    }

    static private void readRandomSamplerMethod(OptimizerDescription desc, Properties props, boolean isParallel) throws WizardException, NumberFormatException, NullPointerException{
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        String str_excludedFiles = props.getProperty("fileFilter");
        
        if (isParallel)
            desc.optimizerClassName = "jams.parallel.optimizer.ParallelRandomSampler";
        else
            desc.optimizerClassName = "jams.components.optimizer.RandomSampler";
        int maxn = (int)Double.parseDouble(str_maximumNumberOfIterations);;
        if (maxn < 1) throw new WizardException(JAMS.i18n("error_maxiter_greater_1"));
        try {
            if (isParallel) {
                if (str_excludedFiles == null) str_excludedFiles = "";
                Pattern.compile(str_excludedFiles);
                desc.attributes.add(new AttributeDescription("excludeFiles", null, str_excludedFiles, false));
            }
        } catch (PatternSyntaxException pse) {
            throw new WizardException(JAMS.i18n("There_is_a_problem_with_the_regular_expression!") + "\n" +
                    JAMS.i18n("The_pattern_in_question_is") + ": " + pse.getPattern() + "\n" +
                    JAMS.i18n("The_description_is") + ": " + pse.getDescription() + "\n");
        }
        desc.attributes.add(new AttributeDescription("maxn", null, Integer.toString(maxn), false));                
    }

    static private void readLatinHyperCubeRandomSamplerMethod(OptimizerDescription desc, Properties props) throws WizardException, NumberFormatException, NullPointerException{
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        desc.optimizerClassName = "jams.components.optimizer.LatinHyperCubeRandomSampler";
        int maxn = (int)Double.parseDouble(str_maximumNumberOfIterations);;
        if (maxn < 1) throw new WizardException(JAMS.i18n("error_maxiter_greater_1"));
        desc.attributes.add(new AttributeDescription("maxn", null, Integer.toString(maxn), false));
    }

    static private void readMultipointRandomSamplerMethod(OptimizerDescription desc, Properties props) throws WizardException, NumberFormatException, NullPointerException{
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        String str_pfd = props.getProperty("pfd");
        desc.optimizerClassName = "jams.components.optimizer.MultiPointRandomSampler";
        int maxn = (int)Double.parseDouble(str_maximumNumberOfIterations);;
        if (maxn < 1) throw new WizardException(JAMS.i18n("error_maxiter_greater_1"));
        desc.attributes.add(new AttributeDescription("maxn", null, Integer.toString(maxn), false));

        int pfd = (int)Double.parseDouble(str_pfd);;
        if (pfd < 2) throw new WizardException(JAMS.i18n("error_pfd_greater_1"));
        desc.attributes.add(new AttributeDescription("pfd", null, Integer.toString(pfd), false));
    }

    static private void readNelderMeadMethod(OptimizerDescription desc, Properties props) throws WizardException, NumberFormatException, NullPointerException{
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        desc.optimizerClassName = "jams.components.optimizer.NelderMead";
        int maxn = (int)Double.parseDouble(str_maximumNumberOfIterations);;
        if (maxn < 1) throw new WizardException(JAMS.i18n("error_maxiter_greater_1"));
        desc.attributes.add(new AttributeDescription("maxn", null, Integer.toString(maxn), false));
    }
    
    static private void readSCEMethod(OptimizerDescription desc, Properties props, boolean parallel) throws WizardException, NumberFormatException, NullPointerException{
        if (parallel) {
            desc.optimizerClassName = "jams.parallel.optimizer.SimpleParallelSCE";
        } else {
            desc.optimizerClassName = "jams.components.optimizer.SimpleSCE";
        }
        String str_numberOfComplexes = props.getProperty("numberOfComplexes");
        String str_pcento = props.getProperty("pcento");
        String str_peps = props.getProperty("peps");
        String str_kstop = props.getProperty("kstop");
        String str_maximumNumberOfIterations = props.getProperty("maxn");
        String str_excludedFiles = props.getProperty("excludedFiles");

        int numOfComplexes = (int)Double.parseDouble(str_numberOfComplexes);
        double pcento = Double.parseDouble(str_pcento);
        double peps = Double.parseDouble(str_peps);
        int kstop = (int)Double.parseDouble(str_kstop);
        int maxn = (int)Double.parseDouble(str_maximumNumberOfIterations);
                  
        if (numOfComplexes < 1 || numOfComplexes > 100)
            throw new WizardException(JAMS.i18n("number_of_complexes_have_to_be_an_integer_between_1_and_100"));
        if (pcento < 0 || pcento > 1)
            throw new WizardException(JAMS.i18n("value_of_pcento_have_to_be_between_0_and_1"));
        if (peps < 0 || peps > 1)
            throw new WizardException(JAMS.i18n("value_of_peps_have_to_be_between_0_and_1"));
        if (kstop < 1 || kstop > 100) 
            throw new WizardException(JAMS.i18n("kstop_have_to_be_an_integer_between_1_and_100"));
        if (maxn < 1) 
            throw new WizardException(JAMS.i18n("error_maxiter_greater_1"));
                    
        try {
            if (parallel) {
                if (str_excludedFiles == null) {
                    str_excludedFiles = "";
                }
                Pattern.compile(str_excludedFiles);
                desc.attributes.add(new AttributeDescription("excludeFiles", null, str_excludedFiles, false));
            }
        } catch (PatternSyntaxException pse) {
            throw new WizardException(JAMS.i18n("There_is_a_problem_with_the_regular_expression!") + "\n" +
                    JAMS.i18n("The_pattern_in_question_is") + ": " + pse.getPattern() + "\n" +
                    JAMS.i18n("The_description_is") + ": " + pse.getDescription() + "\n");
        }
        desc.attributes.add(new AttributeDescription("NumberOfComplexes", null, Integer.toString(numOfComplexes), false));
        desc.attributes.add(new AttributeDescription("pcento", null, Double.toString(pcento), false));
        desc.attributes.add(new AttributeDescription("peps", null, Double.toString(peps), false));
        desc.attributes.add(new AttributeDescription("kstop", null, Integer.toString(kstop), false));
        desc.attributes.add(new AttributeDescription("maxn", null, Integer.toString(maxn), false));        
    }

    static private void readOptimizerMethod(OptimizerDescription desc, Properties props) throws WizardException, NullPointerException, NumberFormatException{
        //read method
        if (props.getProperty("method") != null) {
            desc.method = (int)Double.parseDouble(props.getProperty("method"));
        } else {
            throw new WizardException(JAMS.i18n("error_optimization_method_not_specified"));
        }
        switch (desc.method) {
            case BRANCH_AND_BOUND_METHOD: {
                readBranchAndBoundMethod(desc, props);
                break;
            }
            case GPSEARCH_METHOD: {
                readGPSearchMethod(desc, props);
                break;
            }
            case NELDERMEAD_METHOD: {
                readNelderMeadMethod(desc, props);
                break;
            }
            case PARALLEL_SCE_METHOD: {
                readSCEMethod(desc, props, true);
                break;
            }
            case GUTMANN_METHOD: {
                readGutmannMethod(desc, props);
                break;
            }
            case SCE_METHOD: {
                readSCEMethod(desc, props, false);
                break;
            }
            case MOCOM_METHOD: {
                readMOCOMMethod(desc, props);
                break;
            }
            case PARALLEL_RANDOMSAMPLER_METHOD: {
                readRandomSamplerMethod(desc, props, true);
                break;
            }
            case RANDOMSAMPLER_METHOD: {
                readRandomSamplerMethod(desc, props, false);
                break;
            }
            case NSGA2_METHOD: {
                readNSGA2Method(desc, props);
                break;
            }
            case DIRECT_METHOD: {
                readDirectMethod(desc, props);
                break;
            }
            case LATINHYPERCUBE_METHOD: {
                readLatinHyperCubeRandomSamplerMethod(desc, props);
                break;
            }
            case MULTIPOINT_RANDOMSAMPLER_METHOD: {
                readMultipointRandomSamplerMethod(desc, props);
                break;
            }
        }
    }

    private static void readObjectiveConfig(OptimizerDescription desc, Properties props, Node root) throws WizardException {
        String strObjective = props.getProperty("efficiencies");
        String strObjectiveModes = props.getProperty("efficiency_modes");
        if (strObjective == null || strObjectiveModes == null) {
            throw new WizardException(JAMS.i18n("error_no_objective"));
        }
        StringTokenizer tok1 = new StringTokenizer(strObjective, ";");
        StringTokenizer tok2 = new StringTokenizer(strObjectiveModes, ";");
        int objectiveCount = tok1.countTokens();
        if (tok2.countTokens() != objectiveCount) {
            throw new WizardException(JAMS.i18n("error_objective_count_does_not_match_objecitve_mode_count"));
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
                throw new WizardException(JAMS.i18n("unknown_objective_owner") + objectiveContext[i]);
                
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
    }

    private static void readOutputAttributeConfig(OptimizerDescription desc, Properties props, Node root) throws WizardException {
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
                    throw new WizardException(JAMS.i18n("unknown_objective_owner") + outputAttrContext[i]);
                }
                if (result.equals("jams.model.contextcomponent")) {
                    desc.outputAttributes.add(new AttributeWrapper(null, outputAttrName[i], null, outputAttrContext[i]));
                } else if (result.equals("jams.model.component")) {
                    desc.outputAttributes.add(new AttributeWrapper(outputAttrName[i], null, outputAttrContext[i], null));
                }
            }
        }
    }

    private static void buildConfigurationString(OptimizerDescription desc){
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

    private OptimizerDescription loadOptimizerIni() throws WizardException{
        OptimizerDescription desc = new OptimizerDescription();
        desc.attributes.add(new AttributeDescription("enable", null, "true", false));

        Properties props = new Properties();
        try {
            props.load(optimizerIniStream);
        } catch (Exception e) {            
            throw new WizardException(JAMS.i18n("Could_not_load_JAMS_property_file"));
        }

        loadOptimizationFlags(desc, props);
        if (props.getProperty("workspace") != null) {            
            desc.newWorkspace = props.getProperty("workspace");
        }else{
            throw new WizardException(JAMS.i18n("Error_unknown_workspace"));
        }

        desc.optimizationRun = false;

        //read job mode        
        if (props.getProperty("jobMode") == null || !props.getProperty("jobMode").equals("optimizationRun")) {
            return desc;
        }
        desc.optimizationRun = true;
        try{
            readParameterConfig(desc, props, loadedModel);
        }catch(WizardException e){
            throw new WizardException(JAMS.i18n("invalid_parameter_setup") + e.toString());
        }

        try{
            readObjectiveConfig(desc, props, loadedModel);
        }catch(WizardException e){
            throw new WizardException(JAMS.i18n("invalid_objective_setup") + e.toString());
        }
        try{
            readOptimizerMethod(desc, props);
        }catch(WizardException e){
            throw new WizardException(JAMS.i18n("invalid_optimizer_setup") + e.toString());
        }catch(NumberFormatException e){
            throw new WizardException(JAMS.i18n("invalid_optimizer_setup") + e.toString());
        }catch(NullPointerException e){
            e.printStackTrace();
            throw new WizardException(JAMS.i18n("invalid_optimizer_setup") + e.toString());            
        }
        try{
            readOutputAttributeConfig(desc, props, loadedModel);
        }catch(WizardException e){
            throw new WizardException(JAMS.i18n("invalid_output_setup") + e.toString());
        }
        buildConfigurationString(desc);

        return desc;
    }

    private static void configOutput(OptimizerDescription desc, String optimizerContextName) throws WizardException {
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
                XMLTools.writeXmlFile(outputDoc, desc.newWorkspace + File.separator + "output" + File.separator + "optimization_wizard_" + context + ".xml");
            } catch (Exception e) {
                throw new WizardException(JAMS.i18n("Error_cant_write_xml_file_because_") + e.toString());
            }
        }
    }

    final static String fileVarList[] = {"reachFileName","hruFileName","luFileName","stFileName","gwFileName","shapeFileName","shapeFileName1",
    "stylesFileName","shapeFileName1","heightMap"};
    
    private static void doAdjustments(Node root){
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
    
    private static boolean changeWorkspace(Node root, String newWorkspace) {
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

    private void init() throws WizardException{
        rt = new StandardRuntime();
        rt.loadModel(loadedModel, properties, null);
        if (rt.getDebugLevel() >= 3) {
            if (rt.getErrorLog().length()>2){
                throw new WizardException(rt.getErrorLog());
            }
        }
    }

    public void writeGDLFile(String path){
        //show model graph
        jams.model.metaoptimizer.metaModelOptimizer.ExportGDLFile(dependencyGraph, removedComponents, path);
    }


    public void setOptimizerIni(String optimizerIni){
        try{
            optimizerIniStream = new ByteArrayInputStream(optimizerIni.getBytes("ISO-8859-1"));
        }catch(UnsupportedEncodingException uee){

        }
    }
    public void setOptimizerIni(File optimizerIni) throws WizardException{
        try{
            optimizerIniStream = new FileInputStream(optimizerIni);
        }catch(FileNotFoundException fnfe){
            optimizerIniStream = null;
            throw new WizardException("optimizer description file " + optimizerIni + " not found");
        }
    }

    public modelModifier(JAMSProperties properties, Document doc) throws WizardException{

        this.properties = properties;
        this.loadedModel = doc;

        init();
    }
    public modelModifier(File propertyFile, File modelFile) throws WizardException{
        //default properties
        properties = JAMSProperties.createProperties();
        try {
            properties.load(propertyFile.getAbsolutePath());
        } catch (IOException e) {
            throw new WizardException(JAMS.i18n("error_could_not_load_JAMS_property_file") + e.toString());
        } catch (Exception e) {
            throw new WizardException(JAMS.i18n("error_could_not_load_JAMS_property_file") + e.toString());
        }

        DocumentLoader loader = new DocumentLoader();
        loader.modelFile = JAMSDataFactory.createString();
        loader.modelFile.setValue(modelFile.getName());
        loader.workspaceDir = JAMSDataFactory.createString();
        if (modelFile.getParentFile()!=null)
            loader.workspaceDir.setValue(modelFile.getParentFile().getAbsolutePath());
        else
            loader.workspaceDir.setValue("");
        loader.modelDoc = JAMSDataFactory.createDocument();
        String errorString = loader.init_withResponse();
        loadedModel = loader.modelDoc.getValue();
        if (loadedModel == null) {
            throw new WizardException("error_while_loading_model_file");
        }

        init();
    }

    public Document modifyModel() throws WizardException {
        String optimizerContextName = "optimizer";
        String infoLog = "";        

        init();

        OptimizerDescription desc = loadOptimizerIni();       
        Model model = rt.getModel();
        //1. schritt
        //parameter relevante componenten verschieben
        infoLog += JAMS.i18n("create_transitive_hull_of_dependency_graph") + "\n";
        dependencyGraph = jams.model.metaoptimizer.metaModelOptimizer.getDependencyGraph(loadedModel.getDocumentElement(), model);
        transitiveClosureOfDependencyGraph = jams.model.metaoptimizer.metaModelOptimizer.TransitiveClosure(dependencyGraph);

        Document doc = (Document) loadedModel.cloneNode(true);
        Node root = (Node) doc.getDocumentElement();
        
        if (!changeWorkspace(root, desc.newWorkspace))
            throw new WizardException(JAMS.i18n("unable_to_change_workspace"));
           
       
        if (desc.removeGUIComponents || !desc.optimizationRun) {
            infoLog = JAMS.i18n("removing_GUI_components") + ":\n";
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

            infoLog += JAMS.i18n("removing_components_without_relevant_influence") + ":\n";
            for (int i = 0; i < removedUnusedComponents.size(); i++) {
                infoLog += "    ***" + removedUnusedComponents.get(i) + "\n";
                removedComponents.addAll(removedUnusedComponents);
            }
        }
        if (desc.optimizationRun) {
            infoLog += JAMS.i18n("add_optimization_context") + "\n";
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

            infoLog += JAMS.i18n("find_a_position_to_place_optimizer") + "\n";
            //find place for optimization context
            Node firstComponent = XMLProcessor.getFirstComponent(root);
            if (firstComponent == null) {
                throw new WizardException(JAMS.i18n("Error_model_file_does_not_contain_any_components"));
            }
            //collect all following siblings of firstComponent and add them to contextOptimizer
            Node currentNode = firstComponent;
            ArrayList<Node> followingNodes = new ArrayList<Node>();
            while (currentNode.getNextSibling() != null) {
                followingNodes.add(currentNode);
                currentNode = currentNode.getNextSibling();
            }

            if (firstComponent.getParentNode() == null) {
                throw new WizardException(JAMS.i18n("Error_model_file_does_not_contain_a_model_context"));
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
        configOutput(desc, optimizerContextName);
        //some adjustments like file separators and data-caching
        doAdjustments(doc);

        return doc;
    }

    public static Document modelModifier(JAMSProperties propertyFile, Document modelFile, String optimizerIni) throws WizardException{
        modelModifier modifyModel = new modelModifier(propertyFile, modelFile);
        modifyModel.setOptimizerIni(optimizerIni);
        Document doc = modifyModel.modifyModel();
        /*modifyModel.writeGDLFile("graph.gdl");
        try {
            XMLTools.writeXmlFile(doc, "optimization.jam");
        } catch (Exception e) {
            return;
        }
        System.exit(0);*/
        return doc;
    }

    public static void modelModifier(File propertyFile, File modelFile, File optimizerIni) throws WizardException{
        modelModifier modifyModel = new modelModifier(propertyFile, modelFile);
        modifyModel.setOptimizerIni(optimizerIni);
        Document doc = modifyModel.modifyModel();
        modifyModel.writeGDLFile("graph.gdl");
        try {
            XMLTools.writeXmlFile(doc, "optimization.jam");
        } catch (Exception e) {
            System.out.println(e.toString());
            return;
        }
        System.exit(0);        
    }
}
