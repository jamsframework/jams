/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.regression;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.logging.Level;
import optas.data.SimpleEnsemble;
import org.encog.engine.network.activation.ActivationLinear;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.data.folded.FoldedDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.logic.FeedforwardLogic;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.cross.CrossValidationKFold;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

import org.encog.util.logging.Logging;
import org.encog.util.obj.SerializeObject;

/**
 *
 * @author chris
 */
public class SimpleNeuralNetwork extends SimpleInterpolation {
       
    boolean isTrained = false;
    double error = 0;
    
    BasicNetwork network;

    @Override
    public void setData(SimpleEnsemble x[], SimpleEnsemble y[]) {
        super.setData(x, y);

        network = new BasicNetwork();
        isTrained = false;
    }

    public boolean save(File f){
        try{
            SerializeObject.save(f.getAbsolutePath(), network);
        }catch(IOException ioe){
            ioe.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean load(File f){
        try{
            network = (BasicNetwork)SerializeObject.load(f.getAbsolutePath());
        }catch(IOException ioe){
            ioe.printStackTrace();
            return false;
        }catch(ClassNotFoundException nfe){
            nfe.printStackTrace();
            return false;
        }
        return true;
    }

    //not necessary to call init .. but forces training
    @Override
    public double init(){
        return trainNetwork();
    }
    private double trainNetwork() {
        if (isTrained) {
            return error;
        }
        return error = trainNetwork(new TreeSet<Integer>());        
    }

    int complexityAdjustmentFactor = 3;

    public void setComplexityAdjustmentFactor(int complexityAdjustmentFactor){
        this.complexityAdjustmentFactor = complexityAdjustmentFactor;
    }
    public int getComplexityAdjustmentFactor(){
        return this.complexityAdjustmentFactor;
    }

    private double trainNetwork(TreeSet<Integer> leaveOutIndex) {
        log("Train Neural Network");
        this.setProgress(0.0);

        Logging.setConsoleLevel(Level.OFF);

        network = new BasicNetwork();
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, x.length));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, complexityAdjustmentFactor*(m+(int)((x.length+1)/2 + 1) )));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 1*(m+(int)((x.length+1)/2 + 1) )));
        //network[outputIndex].addLayer(new BasicLayer(new ActivationSigmoid(), true, (int)((x.length+1)/2 + 1) ));
        //network.addLayer(new BasicLayer(new ActivationSigmoid(), true, (int)((x.length+1)/4 + 1) ));
        network.addLayer(new BasicLayer(new ActivationLinear(), true, m));
        network.setLogic(new FeedforwardLogic());
        network.getStructure().finalizeStructure();
        network.reset();

        ArrayList<double[]> xData = new ArrayList<double[]>(),
                            yData = new ArrayList<double[]>();

        for (int i=0;i<this.L;i++){

            int id_i = x[0].getId(i);

            if (leaveOutIndex.contains(id_i))
                continue;

            double[] sampleX = new double[n];
            double[] sampleY = new double[m];
            
            for (int j=0;j<n;j++){                
                sampleX[j] = x[j].getValue(id_i);
            }
            for (int j=0;j<m;j++){
                sampleY[j] = y[j].getValue(id_i);
            }
            xData.add(normalizeX(sampleX));
            yData.add(normalizeY(sampleY));
        }
        /*double yDataArray[][] = new double[yData.size()][1];
        for (int i=0;i<yData.size();i++){
            yDataArray[i][0] = yData.get(i)[outputIndex];
        }*/
        double xDataArray[][] = xData.toArray(new double[xData.size()][]);
        double yDataArray[][] = yData.toArray(new double[yData.size()][]);
        BasicNeuralDataSet basicNDS = new BasicNeuralDataSet(xDataArray,yDataArray);
        basicNDS.setDescription("testdataset");
        
        
        FoldedDataSet folded = new FoldedDataSet(basicNDS);
        Train train = new ResilientPropagation(network, folded);
        CrossValidationKFold trainFolded = new CrossValidationKFold(train,4);
                
        int epoch = 1;
        int epochMax = 10000;
        double improvement = 1;
        
        double errorNow=0;
        double errorLast=0;
        
        do {
            trainFolded.iteration();
            if (epoch % 100 == 0)
                System.out.println("Epoch #" + epoch + " Error:" + trainFolded.getError() + " improvement " + improvement);
            epoch++;
            errorLast = errorNow;
            errorNow  = trainFolded.getError();
            if (epoch>2){
                improvement = 0.95*improvement + 0.05*(1.0-errorNow/errorLast);
            }
            
            setProgress((double)epoch / (double)epochMax);
        } while (improvement > 0.0001 && !trainFolded.isTrainingDone() && epoch < epochMax);

        isTrained = true;
        return trainFolded.getError();
    }

    @Override
    protected double[][] getInterpolatedValue(TreeSet<Integer> validationSet) {
        isTrained = false;

        trainNetwork(validationSet);

        double[][] values = new double[validationSet.size()][];
        int counter = 0;
        for (Integer i : validationSet){
            values[counter++] = getInterpolatedValue(this.getX(i));
        }
        return values;
    }

    

    @Override
    public double[] getInterpolatedValue(double u[]) {
        trainNetwork();

        double wholeOutput[] = new double[m];
        network.compute(normalizeX(u), wholeOutput);
        
        return denormalizeY(wholeOutput);
    }
}
