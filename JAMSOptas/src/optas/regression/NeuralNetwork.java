/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.regression;

import java.util.ArrayList;
import java.util.TreeSet;
import optas.hydro.data.SimpleEnsemble;
import org.encog.engine.network.activation.ActivationLinear;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.logic.FeedforwardLogic;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

/**
 *
 * @author chris
 */
public class NeuralNetwork extends Interpolation {
   
    double expWeights[];
    boolean isTrained = false;

    BasicNetwork network = new BasicNetwork();

    double xRange[];
    double xMin[];

    double yRange, yMin;

    @Override
    public void setWeighting(double w[]) {
        super.setWeighting(w);
        if (expWeights == null) {
            expWeights = new double[n];
        }

        for (int i = 0; i < n; i++) {
            expWeights[i] = Math.exp(w[i]);
        }
    }
    
    @Override
    public void setData(SimpleEnsemble x[], SimpleEnsemble y) {
        super.setData(x, y);

        this.xRange = new double[n];
        this.xMin = new double[n];
        //normalize between -1 and 1
        for (int i=0;i<n;i++){
            double min = x[i].getMin();
            double max = x[i].getMax();

            xRange[i] = 1.0 / (max-min);
            xMin[i] = min;
        }
        yRange = 1.0 / (y.getMax() - y.getMin());
        yMin = y.getMin();

        isTrained = false;
    }

    private void trainNetwork() {
        trainNetwork(new TreeSet<Integer>());
    }
    private void trainNetwork(TreeSet<Integer> leaveOutIndex) {
        if (isTrained) {
            return;
        }
        network = new BasicNetwork();
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, x.length));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, (int)((x.length+1)/2 + 1) ));
        network.addLayer(new BasicLayer(new ActivationLinear(), true, 1));
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
            double[] sampleY = new double[1];
            
            for (int j=0;j<n;j++){
                sampleX[j] = ((x[j].getValue(id_i)-xMin[j])*xRange[j]*2.0)-1.0;
            }
            sampleY[0] = ((y.getValue(id_i)-yMin)*yRange*2.0)-1.0;
            xData.add(sampleX);
            yData.add(sampleY);
        }
        double xDataArray[][] = xData.toArray(new double[xData.size()][]);
        double yDataArray[][] = yData.toArray(new double[yData.size()][]);
        BasicNeuralDataSet basicNDS = new BasicNeuralDataSet(xDataArray,yDataArray);
        basicNDS.setDescription("testdataset");


        /*EncogUtility.trainToError(network, basicNDS, 0.002);
        EncogUtility.t
        double[] array = {0.1, 0.2, 0.3, 0.4, 0.5};
        NeuralData input = new BasicNeuralData(array);
        NeuralData output = network.compute(input);
        EncogUtility.evaluate(network, temporal);
        System.out.println(output.toString());*/

        double alpha = 0.002;
        double beta = 0.003;
        System.out.println("Performing Backpropagation learning this alpha="+alpha+" and momentum="+beta);

        Train backpropagation = new ResilientPropagation(network, basicNDS);

        /*backpropagation.setLearningRate(alpha);
        backpropagation.setMomentum(beta);*/
        int epoch = 1;

        do {
            backpropagation.iteration();
            System.out.println("Epoch #" + epoch + " Error:" + backpropagation.getError());
            epoch++;
            if (backpropagation.getError() > 10000000){
                System.out.println("high error");
            }
        } while (backpropagation.getError() > 0.01 && !backpropagation.isTrainingDone() && epoch < 500);

        System.out.println("After "+epoch+" iterations the error is " + backpropagation.getError());
        isTrained = true;
    }

    protected double[] getValue(TreeSet<Integer> validationSet) {
        isTrained = false;
        trainNetwork(validationSet);

        double[] values = new double[validationSet.size()];
        int counter = 0;
        for (Integer i : validationSet){
            values[counter++] = getValue(this.getX(i));
        }
        return values;
    }

    private double[] normalize(double u[]){
        double normalizedU[] = new double[u.length];
        for (int i=0;i<normalizedU.length;i++){
            normalizedU[i] = ((u[i]-this.xMin[i])*xRange[i]*2.0)-1.0;
        }
        return normalizedU;
    }

    private double normalizeOutput(double y){
        return ((y-yMin)*yRange*2.0)-1.0;
    }

    private double[] denormalize(double u[]){
        double denormalizedU[] = new double[u.length];
        for (int i=0;i<denormalizedU.length;i++){
            denormalizedU[i] = ((u[i]+1.0)/(2.0*xRange[i]))+xMin[i];
        }
        return denormalizedU;
    }

    private double denormalizeOutput(double y){
        return ((y+1.0)/(2.0*yRange))+yMin;
    }

    public double getValue(double u[]) {
        trainNetwork();

        double output[] = new double[1];

        network.compute(normalize(u), output);
        return denormalizeOutput(output[0]);
    }
}
