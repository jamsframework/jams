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

    BasicNetwork network = new BasicNetwork();
    
    @Override
    public void setData(SimpleEnsemble x[], SimpleEnsemble y) {
        super.setData(x, y);

        isTrained = false;
    }

    private void trainNetwork() {
        trainNetwork(new TreeSet<Integer>());
    }
    private void trainNetwork(TreeSet<Integer> leaveOutIndex) {
        if (isTrained) {
            return;
        }
        Logging.setConsoleLevel(Level.OFF);

        network = new BasicNetwork();
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, x.length));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, (int)((x.length+1)/2 + 1) ));
        //network.addLayer(new BasicLayer(new ActivationSigmoid(), true, (int)((x.length+1)/4 + 1) ));
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
                sampleX[j] = x[j].getValue(id_i);
            }            
            xData.add(normalizeX(sampleX));
            yData.add(new double[]{normalizeY(y.getValue(id_i))});
        }
        double xDataArray[][] = xData.toArray(new double[xData.size()][]);
        double yDataArray[][] = yData.toArray(new double[yData.size()][]);
        BasicNeuralDataSet basicNDS = new BasicNeuralDataSet(xDataArray,yDataArray);
        basicNDS.setDescription("testdataset");
        
        Train backpropagation = new ResilientPropagation(network, basicNDS);
        
        int epoch = 1;

        do {
            backpropagation.iteration();
            System.out.println("Epoch #" + epoch + " Error:" + backpropagation.getError());
            epoch++;            
        } while (backpropagation.getError() > 0.01 && !backpropagation.isTrainingDone() && epoch < 1500);

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

    

    public double getValue(double u[]) {
        trainNetwork();

        double output[] = new double[1];

        network.compute(normalizeX(u), output);
        return denormalizeY(output[0]);
    }
}
