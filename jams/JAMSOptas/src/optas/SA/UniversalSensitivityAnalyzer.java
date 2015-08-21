/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.SA;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;
import optas.SA.SensitivityAnalyzer.SamplingMethod;
import optas.SA.SobolsMethod.Measure;
import optas.core.AbstractDataSerie;
import optas.core.AbstractFunction;
import optas.data.DataCollection;
import optas.data.EfficiencyEnsemble;
import optas.data.SimpleEnsemble;
import optas.optimizer.management.SampleFactory;
import optas.optimizer.management.SampleFactory.Sample;
import optas.regression.SimpleInterpolation;
import optas.regression.SimpleInterpolation.NormalizationMethod;
import optas.regression.SimpleNeuralNetwork;

/**
 *
 * @author chris
 */
public class UniversalSensitivityAnalyzer extends Observable {

    public enum SAMethod {

        RSA, MaximumGradient, ElementaryEffects, ElementaryEffectsNonAbs, ElementaryEffectsVariance, FOSI1, FOSI2, TOSI, Interaction, LinearRegression
    };

    SAMethod method = SAMethod.RSA;

    SensitivityAnalyzer sa = null;
    SimpleInterpolation I = null;

    boolean usingRegression = false;

    NormalizationMethod parameterNormalizationMethod = SimpleInterpolation.NormalizationMethod.Linear;
    NormalizationMethod objectiveNormalizationMethod = SimpleInterpolation.NormalizationMethod.Linear;

    SimpleEnsemble xData[] = null;
    EfficiencyEnsemble yData = null;
    double range[][] = null;
    int sampleCount = 2000;
    int n = 0;

    public void setParameterNormalizationMethod(NormalizationMethod normalizationMethod) {
        this.parameterNormalizationMethod = normalizationMethod;
    }

    public void setObjectiveNormalizationMethod(NormalizationMethod normalizationMethod) {
        this.objectiveNormalizationMethod = normalizationMethod;
    }

    public NormalizationMethod getParameterNormalizationMethod() {
        return this.parameterNormalizationMethod;
    }

    public NormalizationMethod getObjectiveNormalizationMethod() {
        return this.objectiveNormalizationMethod;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
    }

    public SAMethod getMethod() {
        return method;
    }

    public void setMethod(SAMethod method) {
        switch (method) {
            case RSA:
                sa = new optas.SA.RegionalSensitivityAnalysis();
                break;
            case MaximumGradient:
                sa = new optas.SA.GradientSensitivityAnalysis();
                break;
            case ElementaryEffects:
                sa = new optas.SA.MorrisMethod();
                break;
            case ElementaryEffectsNonAbs:
                sa = new optas.SA.MorrisMethod(MorrisMethod.Measure.NonAbsolute);
                break;
            case ElementaryEffectsVariance:
                sa = new optas.SA.MorrisMethod(MorrisMethod.Measure.Variance);
                break;
            case FOSI1:
                sa = new optas.SA.FAST(optas.SA.FAST.Measure.FirstOrder);
                break;
            case FOSI2:
                sa = new optas.SA.SobolsMethod(Measure.FirstOrder);
                break;
            case TOSI:
                sa = new optas.SA.SobolsMethod(Measure.Total);
                break;
            case Interaction:
                sa = new optas.SA.SobolsMethod(Measure.Interaction);
                break;
            case LinearRegression:
                sa = new optas.SA.LinearRegression();
                break;
        }
    }

    public void setSamplingMethod(SamplingMethod sm){
        this.sa.setSamplingMethod(sm);
    }
    
    public boolean isUsingRegression() {
        return this.usingRegression;
    }

    public void setUsingRegression(boolean flag) {
        this.usingRegression = flag;
    }

    public double setup(SimpleEnsemble xData[], EfficiencyEnsemble yData) {
        return setup(xData, yData, new SimpleNeuralNetwork());
    }

    protected void setState(String state){
        setChanged();
        notifyObservers(state);
    }
    
    public double setup(SimpleEnsemble xData[], EfficiencyEnsemble yData, SimpleInterpolation interpolationAlgorithm) {

        setState("setup sensitivity analysis");
        this.xData = xData;
        this.yData = yData;
        this.n = xData.length;
        this.range = new double[n][2];

        double error = 0;
        
        if (usingRegression) {
            this.I = interpolationAlgorithm;
            if (I == null) {
                I = new SimpleNeuralNetwork();
            }
            I.setData(xData, yData);
            I.setxNormalizationMethod(parameterNormalizationMethod);
            I.setyNormalizationMethod(objectiveNormalizationMethod);
            error = I.init();
        }

        for (int i = 0; i < n; i++) {
            range[i][0] = xData[i].getMin();
            range[i][1] = xData[i].getMax();
        }
        if (usingRegression) {
            sa.setModel(new AbstractFunction() {
                @Override
                public int getInputDimension() {
                    return UniversalSensitivityAnalyzer.this.n;
                }

                @Override
                public int getOutputDimension() {
                    return 1;
                }

                @Override
                public double[][] getRange() {
                    return range;
                }

                @Override
                public String[] getInputFactorNames() {
                    String names[] = new String[n];
                    for (int i = 0; i < getInputDimension(); i++) {
                        names[i] = UniversalSensitivityAnalyzer.this.xData[i].getName();
                    }
                    return names;
                }

                @Override
                public String[] getOutputFactorNames() {
                    return new String[]{UniversalSensitivityAnalyzer.this.yData.getName()};
                }

                int counter = 0;
                SampleFactory factory = new SampleFactory();

                @Override
                public double[] evaluate(double[] x) {
                    if (usingRegression) {
                        return I.getInterpolatedValue(x);
                    } else {
                        return null;
                    }
                }

                @Override
                public void log(String msg) {
                    System.out.println(msg);
                }
            });
            sa.setSampleSize(sampleCount);
        } else {
            sa.setModel(new AbstractDataSerie() {
                @Override
                public void reset() {
                    this.counter = 0;
                    factory = new SampleFactory();
                }

                @Override
                public int getInputDimension() {
                    return UniversalSensitivityAnalyzer.this.n;
                }

                @Override
                public int getOutputDimension() {
                    return 1;
                }

                @Override
                public double[][] getRange() {
                    return range;
                }

                @Override
                public String[] getInputFactorNames() {
                    String names[] = new String[n];
                    for (int i = 0; i < getInputDimension(); i++) {
                        names[i] = UniversalSensitivityAnalyzer.this.xData[i].getName();
                    }
                    return names;
                }

                @Override
                public String[] getOutputFactorNames() {
                    return new String[]{UniversalSensitivityAnalyzer.this.yData.getName()};
                }
                int counter = 0;
                SampleFactory factory = new SampleFactory();

                @Override
                public Sample getNext() {
                    double x[] = new double[getInputDimension()];
                    if (counter >= UniversalSensitivityAnalyzer.this.xData[0].getSize()) {
                        return null;
                    }

                    int nextId = UniversalSensitivityAnalyzer.this.xData[0].getId(counter++);

                    for (int i = 0; i < x.length; i++) {
                        x[i] = UniversalSensitivityAnalyzer.this.xData[i].getValue(nextId);
                    }
                    return factory.getSampleSO(x, UniversalSensitivityAnalyzer.this.yData.getValue(nextId));
                }

                @Override
                public void log(String msg) {
                    System.out.println(msg);
                }
            });
            sa.setSampleSize(this.yData.getSize());
        }
        return error;
    }

    public SimpleEnsemble[] getXDataSet() {
        return this.xData;
    }

    public EfficiencyEnsemble getYDataSet() {
        return this.yData;
    }

    public double[][][] getInteractionsUncertainty() {
        setState("calculating uncertainty of interaction effects");

        double result[][][] = new double[n][n][3];

        ArrayList<double[][]> statistics = new ArrayList<double[][]>();

        double mean[][] = new double[n][n];
        double sigma[][] = new double[n][n];
        double min[][] = new double[n][n];
        double max[][] = new double[n][n];

        double currentMaxError = 1000.0;
        double maxAcceptedError = 0.01;

        for (int j = 0; j < n; j++) {
            for (int k = 0; k < n; k++) {
                min[j][k] = Double.MAX_VALUE;
                max[j][k] = Double.MIN_VALUE;
            }
        }

        double z = 1.96;//CDF_Normal.xnormi(1.0-alpha); //should be 1.96
        int i = 0;
        while (i++ < 10 || currentMaxError > maxAcceptedError) {
            //reset interpolator
            setup(xData, yData);
            setState("<html>Calculating Interaction Effects ... <br>Iteration: " + i + "<br>Error: " + String.format(Locale.ENGLISH, "%.4f", currentMaxError) + " ( max: " + String.format(Locale.ENGLISH, "%.4f", maxAcceptedError) + ")</html>");

            double sensitivityIndex[][] = new double[n][n];
            sensitivityIndex = this.getInteractions();
            if (sensitivityIndex == null) {
                return null;
            }

            statistics.add(Arrays.copyOf(sensitivityIndex, n));

            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    mean[j][k] += sensitivityIndex[j][k];
                    sigma[j][k] = 0;
                    min[j][k] = Math.min(min[j][k], sensitivityIndex[j][k]);
                    max[j][k] = Math.max(max[j][k], sensitivityIndex[j][k]);
                }
            }

            double K = statistics.size();

            for (int l = 0; l < K; l++) {
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < n; k++) {
                        double v = (statistics.get(l)[j][k] - (mean[j][k] / K));
                        sigma[j][k] += v * v;
                    }
                }
            }

            currentMaxError = 0.0;
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    sigma[j][k] /= (K - 1);
                    sigma[j][k] = Math.sqrt(sigma[j][k]);

                    double error_mean = z * sigma[j][k] / Math.sqrt(K);
                    currentMaxError = Math.max(error_mean / mean[j][k], currentMaxError);
                }
            }
            System.out.println("current error:" + currentMaxError);
        }

        for (int j = 0; j < n; j++) {
            for (int k = 0; k < n; k++) {
                mean[j][k] /= statistics.size();
            }
        }
        System.out.println("******************************************");
        System.out.println("Uncertainty calculation finished");
        System.out.println("id\tmu\tsigma\tmin\tmax");
        for (int j = 0; j < n; j++) {
            for (int k = 0; k < n; k++) {
                System.out.println((j + 1) + "-" + (k + 1) + "\t" + String.format(Locale.ENGLISH, "%.3f", mean[j][k]) + "\t" + String.format(Locale.ENGLISH, "%.4f", sigma[j][k]) + "\t" + String.format(Locale.ENGLISH, "%.3f", min[j][k]) + "\t" + String.format(Locale.ENGLISH, "%.3f", max[j][k]));
            }
        }
        System.out.println("******************************************");
        for (int j = 0; j < n; j++) {
            for (int k = 0; k < n; k++) {
                result[j][k] = new double[]{mean[j][k] - 1.96 * sigma[j][k], mean[j][k], mean[j][k] + 1.96 * sigma[j][k]};
            }
        }
        return result;
    }

    public double[][] getInteractions() {
        if (sa instanceof SobolsMethod) {
            double s[][] = new double[n][n];
            SobolsMethod v = (SobolsMethod) sa;
            for (int i = 0; i < n; i++) {
                s[i][i] = v.getSensitivity(i);
            }
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    Set<Integer> set = new TreeSet<Integer>();
                    set.add(i);
                    set.add(j);
                    s[i][j] = v.getSensitivity(set)[0];
                    s[j][i] = s[i][j] - s[i][i] - s[j][j];
                }
            }
            return s;
        }
        return null;
    }

    public double[] getSensitivity() {
        double result[] = new double[n];
        for (int i = 0; i < n; i++) {
            double s = sa.getSensitivity(i);
            result[i] = s;
        }
        return result;
    }

    public double[][] getUncertaintyOfSensitivity() {
        double result[][] = new double[n][3];
        double currentMaxError = 1000.0;
        double maxAcceptedError = 0.025;

        double z = 1.96;//CDF_Normal.xnormi(1.0-alpha); //should be 1.96
        int i = 0;
        ErrorStatistics<Double> stat = new ErrorStatistics<Double>(n);
        stat.setQuantileRange(0.0, 0.9);
        
        while (i++ < 10 || currentMaxError > maxAcceptedError) {
            //reset interpolator
            double error = setup(xData, yData);

            setState("<html>Calculating Sensitivity Indicies ... <br>Iteration: " + i + "<br>Error: " + String.format(Locale.ENGLISH, "%.4f", currentMaxError) + " ( max: " + String.format(Locale.ENGLISH, "%.4f", maxAcceptedError) + ")</html>");
            double sensitivityIndex[] = new double[n];
            for (int j = 0; j < n; j++) {
                sensitivityIndex[j] = sa.getSensitivity(j);
            }

            stat.add(error, sensitivityIndex);
            
            double mean[] = stat.getMean();
            double sigma2[] = stat.getVariance();
            int K = stat.getSize();
            
            if (K == 0){
                continue;
            }
                        
            currentMaxError = 0.0;
            for (int j = 0; j < n; j++) {                
                double error_mean = z * Math.sqrt(sigma2[j]) / Math.sqrt(K);
                currentMaxError = Math.max(error_mean / mean[j], currentMaxError);
            }
            System.out.println("current error:" + currentMaxError);
        }

        double mean[] = stat.getMean();
        double min[] = stat.getMin();
        double max[] = stat.getMax();
        double sigma2[] = stat.getVariance();
        double sigma[] = new double[n];
        for (int j = 0; j < n; j++) {  
            sigma[j] = Math.sqrt(sigma2[j]);
        }
        
        System.out.println("******************************************");
        System.out.println("Uncertainty calculation finished");
        System.out.println("id\tmu\tsigma\tmin\tmax");
        for (int j = 0; j < n; j++) {
            System.out.println((j + 1) + "\t" + String.format(Locale.ENGLISH, "%.3f", mean[j]) + "\t" + String.format(Locale.ENGLISH, "%.4f", sigma[j]) + "\t" + String.format(Locale.ENGLISH, "%.3f", min[j]) + "\t" + String.format(Locale.ENGLISH, "%.3f", max[j]));
        }
        System.out.println("******************************************");
        for (int j = 0; j < n; j++) {
            result[j] = new double[]{mean[j] - 1.96 * sigma[j], mean[j], mean[j] + 1.96 * sigma[j]};
        }
        return result;
    }

    public double calculateError() {
        if (usingRegression) {
            setState("<html>Calculating Regression Error ... </html>");
            double error[] = I.estimateCrossValidationError(5, SimpleInterpolation.ErrorMethod.E2);
            double meanCrossValidationError = 0;
            for (int i = 0; i < error.length; i++) {
                meanCrossValidationError += error[i];
            }
            meanCrossValidationError /= error.length;

            return meanCrossValidationError;
        } else {
            return 0.0;
        }
    }

    public static void main2(String[] args) {        
        DataCollection collection = DataCollection.createFromFile(new File("E:\\ModelData\\Testgebiete\\SynthFunction\\Zakharov\\output\\20140403_101642\\zakharov.cdat"));
        String buffer = "";
        for (int i = 100; i < 101; i++) {            
            collection.filter("ID", 0, i, false);
            UniversalSensitivityAnalyzer usa = new UniversalSensitivityAnalyzer();
            usa.setMethod(UniversalSensitivityAnalyzer.SAMethod.RSA);
            usa.setSamplingMethod(SamplingMethod.URS);
            usa.setObjectiveNormalizationMethod(NormalizationMethod.Linear);
            usa.setParameterNormalizationMethod(NormalizationMethod.Linear);
            usa.setSampleCount(50000);
            usa.setUsingRegression(true);
            SimpleEnsemble x1 = collection.getSimpleEnsemble("x1");
            SimpleEnsemble x2 = collection.getSimpleEnsemble("x2");
            EfficiencyEnsemble y = (EfficiencyEnsemble) collection.getDataSet("y");
            usa.setup(new SimpleEnsemble[]{x1, x2}, y);
            usa.getSensitivity();
        }
    }
    
    public static void main(String[] args) {        
        DataCollection collection = DataCollection.createFromFile(new File(args[0]));
        String buffer = "";
        for (int i = 5; i < 251; i+=5) {            
            collection.filter("ID", 0, i, false);
            UniversalSensitivityAnalyzer usa = new UniversalSensitivityAnalyzer();
            usa.setMethod(UniversalSensitivityAnalyzer.SAMethod.RSA);
            usa.setSamplingMethod(SamplingMethod.URS);
            usa.setObjectiveNormalizationMethod(NormalizationMethod.Linear);
            usa.setParameterNormalizationMethod(NormalizationMethod.Linear);
            usa.setSampleCount(50000);
            usa.setUsingRegression(true);
            
            SimpleEnsemble x1 = collection.getSimpleEnsemble("x1");
            SimpleEnsemble x2 = collection.getSimpleEnsemble("x2");
            EfficiencyEnsemble y = (EfficiencyEnsemble) collection.getDataSet("y");
            usa.setup(new SimpleEnsemble[]{x1, x2}, y);

            System.out.println("E2 after " + i + " : " + usa.calculateError());
            
            double r1[][] = usa.getUncertaintyOfSensitivity();
            
            usa.setSamplingMethod(SamplingMethod.URSStatic);
            
            double r2[][] = usa.getUncertaintyOfSensitivity();
            buffer += String.format("%d\t%.3f\t%.3f\t%.3f\t%.3f\t\t%.3f\t%.3f\t%.3f\t%.3f\n", i, r1[0][1], r1[0][2] - r1[0][0], r1[1][1], r1[1][2] - r1[1][0], r2[0][1], r2[0][2] - r2[0][0], r2[1][1], r2[1][2] - r2[1][0]);
            System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\n"+buffer+"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            collection.clearIDFilter();
        }
    }
}
