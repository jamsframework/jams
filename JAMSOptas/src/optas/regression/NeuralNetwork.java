/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.regression;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.logging.Level;
import optas.hydro.data.SimpleEnsemble;
import org.encog.engine.network.activation.ActivationLinear;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.logic.FeedforwardLogic;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.util.logging.Logging;

/**
 *
 * @author chris
 */
public class NeuralNetwork extends Interpolation {
       
    boolean isTrained = false;

    BasicNetwork network[];

    @Override
    public void setData(SimpleEnsemble x[], SimpleEnsemble y[]) {
        super.setData(x, y);

        network = new BasicNetwork[m];
        for (int i=0;i<m;i++){
            network[i] = new BasicNetwork();
        }
        isTrained = false;
    }

    //not necessary to call init .. but forces training
    @Override
    public void init(){
        trainNetwork();
    }
    private void trainNetwork() {
        if (isTrained) {
            return;
        }
/*        for (int i=0;i<m;i++)
            trainNetwork(i,new TreeSet<Integer>());*/
        trainNetwork(0,new TreeSet<Integer>());        
    }

    final int complexityAdjustmentFactor = 3;

    private void trainNetwork(int outputIndex, TreeSet<Integer> leaveOutIndex) {
        log("Train Neural Network");
        this.setProgress(0.0);

        Logging.setConsoleLevel(Level.OFF);

        network[outputIndex] = new BasicNetwork();
        network[outputIndex].addLayer(new BasicLayer(new ActivationSigmoid(), true, x.length));
        network[outputIndex].addLayer(new BasicLayer(new ActivationSigmoid(), true, complexityAdjustmentFactor*(m+(int)((x.length+1)/2 + 1) )));
        network[outputIndex].addLayer(new BasicLayer(new ActivationSigmoid(), true, 1*(m+(int)((x.length+1)/2 + 1) )));
        //network[outputIndex].addLayer(new BasicLayer(new ActivationSigmoid(), true, (int)((x.length+1)/2 + 1) ));
        //network.addLayer(new BasicLayer(new ActivationSigmoid(), true, (int)((x.length+1)/4 + 1) ));
        network[outputIndex].addLayer(new BasicLayer(new ActivationLinear(), true, m));
        network[outputIndex].setLogic(new FeedforwardLogic());
        network[outputIndex].getStructure().finalizeStructure();
        network[outputIndex].reset();

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
        
        Train backpropagation = new ResilientPropagation(network[outputIndex], basicNDS);
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
    }

    @Override
    protected double[][] getValue(TreeSet<Integer> validationSet) {
        isTrained = false;
        /*for (int i=0;i<m;i++)
            trainNetwork(i,validationSet);*/
        trainNetwork(0,validationSet);

        double[][] values = new double[validationSet.size()][];
        int counter = 0;
        for (Integer i : validationSet){
            values[counter++] = getValue(this.getX(i));
        }
        return values;
    }

    

    @Override
    public double[] getValue(double u[]) {
        trainNetwork();

        double singleOutput[] = new double[1];
        double wholeOutput[] = new double[m];
        network[0].compute(normalizeX(u), wholeOutput);
        /*for (int i=0;i<m;i++){
            network[i].compute(normalizeX(u), singleOutput);
            wholeOutput[i] = singleOutput[0];
        }*/
        return denormalizeY(wholeOutput);
    }
}
