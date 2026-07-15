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
import optas.data.TimeSerieEnsemble;
import org.encog.engine.network.activation.ActivationLinear;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.logic.FeedforwardLogic;
import org.encog.neural.networks.synapse.Synapse;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.util.logging.Logging;
import org.encog.util.obj.SerializeObject;

/**
 *
 * @author chris
 */
public class TimeSerieNeuralNetwork extends TimeSeriesInterpolation {
       
    boolean isTrained = false;
    double error = 0;
    
    BasicNetwork network;

    @Override
    public void setData(SimpleEnsemble x[], TimeSerieEnsemble y) {
        super.setData(x, y);

        network = new BasicNetwork();
        isTrained = false;
    }

    //not necessary to call init .. but forces training
    @Override
    public double init(){
        return trainNetwork();
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
            isTrained = true;
        }catch(IOException ioe){
            ioe.printStackTrace();
            return false;
        }catch(ClassNotFoundException nfe){
            nfe.printStackTrace();
            return false;
        }
        return true;
    }

    private double trainNetwork() {
        if (isTrained) {
            return error;
        }
        return error = trainNetwork(new TreeSet<Integer>());        
    }

    private double trainNetwork(TreeSet<Integer> leaveOutIndex) {
        log("Train Neural Network");
        this.setProgress(0.0);

        Logging.setConsoleLevel(Level.OFF);

        network = new BasicNetwork();
        BasicLayer layerIn = new BasicLayer(new ActivationSigmoid(), true, x.length);
        BasicLayer hidden1 = new BasicLayer(new ActivationSigmoid(), true, m);
        BasicLayer hidden2 = new BasicLayer(new ActivationLinear(), true, m);

        network.addLayer(layerIn);
        network.addLayer(hidden1);
        network.addLayer(hidden2);
        network.setLogic(new FeedforwardLogic());
        //network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 200));
        //network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 1*(m+(int)((x.length+1)/2 + 1) )));
        //network[outputIndex].addLayer(new BasicLayer(new ActivationSigmoid(), true, (int)((x.length+1)/2 + 1) ));
        //network.addLayer(new BasicLayer(new ActivationSigmoid(), true, (int)((x.length+1)/4 + 1) ));        
        /*
        for (int i=0;i<m;i++){
            for (int j=0;j<m;j++){
                Synapse inputToHidden1 = layerIn.getNext().get(0);
                Synapse hidden1ToHidden2 = inputToHidden1.getToLayer().getNext().get(0);

                if (Math.abs(i-j)>20 && i != 0 && j!=0 && i!=m-1 && j!=m-1)
                    network.enableConnection(hidden1ToHidden2, i, j, false);
            }
        }*/
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
                sampleY[j] = getYData(id_i, j);
            }
            xData.add(normalizeX(sampleX));
            yData.add(normalizeY(sampleY));
        }
        
        double xDataArray[][] = xData.toArray(new double[xData.size()][]);
        double yDataArray[][] = yData.toArray(new double[yData.size()][]);
        BasicNeuralDataSet basicNDS = new BasicNeuralDataSet(xDataArray,yDataArray);
        basicNDS.setDescription("testdataset");
        
        Train backpropagation = new ResilientPropagation(network, basicNDS);
        backpropagation.setError(1);
        int epoch = 1;
        int epochMax = 1500;
        do {
            backpropagation.iteration();
            System.out.println("Epoch #" + epoch + " Error:" + backpropagation.getError());
            epoch++;
            setProgress((double)epoch / (double)epochMax);
        } while (backpropagation.getError() > 0.005 && !backpropagation.isTrainingDone() && epoch < epochMax);

        //System.out.println("After "+epoch+" iterations the error is " + backpropagation.getError());
        isTrained = true;
        return 0;
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

    public double[] estimateCrossValidationError(int K, ErrorMethod e){
        if (!network.getProperties().containsKey("CVError")){
            double result[] = super.estimateCrossValidationError(K,e);
            for (int t=0;t<this.m;t++)
                network.setProperty(Integer.toString(t), result[t]);
            network.setProperty("CVError", 1.0);
        }
        double result[] =new double[m];
        for (int t = 0; t < this.m; t++) {
            result[t] = network.getPropertyDouble(Integer.toString(t));
        }

        return result;
    }
}
