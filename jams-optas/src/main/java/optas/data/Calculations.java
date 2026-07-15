/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data;

import java.util.ArrayList;
import java.util.Arrays;
import optas.optimizer.management.SampleFactory.Sample;

/**
 *
 * @author chris
 */
public class Calculations {

    static public double[] calcBounds(ArrayList<Sample> list, int param, int objective, int boxcount, double threshold){
        SimpleEnsemble p = new SimpleEnsemble("test1", list.size());
        EfficiencyEnsemble e = new EfficiencyEnsemble("test2", list.size(), false);
        for (int i=0;i<list.size();i++){
            p.add(i, list.get(i).x[param]);
            e.add(i, list.get(i).F()[objective]);
        }
        return calcBounds(p, e, boxcount, threshold);
    }

    static public double[] calcBounds(SimpleEnsemble p, EfficiencyEnsemble e, int boxcount, double threshold){
        double distribution[] = calcPostioriDistribution(p, e, boxcount);

        /*double min = Double.POSITIVE_INFINITY;
        for (int i=0;i<distribution.length;i++){
            min = Math.min(min, distribution[i]);
        }

        int minIndex = 0;
        while (distribution[minIndex] > threshold * min) {
            minIndex++;
        }

        int maxIndex = distribution.length - 1;
        while (distribution[maxIndex] > threshold * min) {
            maxIndex--;
        }

        if (minIndex >= maxIndex) {
            System.err.println("Error in ParameterSpaceReducer");
        }
        minIndex = Math.max(0,minIndex -1);
        maxIndex = Math.min(maxIndex+1, boxcount-1);
        
        double r1 = (double)minIndex / (double)(boxcount-1);
        double r2 = (double)maxIndex / (double)(boxcount-1);

        double lb = p.getMin() + r1 * (p.getMax() - p.getMin());
        double ub = p.getMin() + r2 * (p.getMax() - p.getMin());*/

        double boxMin = Double.POSITIVE_INFINITY;
        double boxMax = Double.NEGATIVE_INFINITY;

        double confidence = 0.0;
        while(confidence<0.93){
            int pMax = 0;
            for (int i=0;i<boxcount;i++){
                if (distribution[i]>distribution[pMax]){
                    pMax = i;
                }
            }
            confidence += distribution[pMax];
            distribution[pMax] = 0;

            boxMin = Math.min(boxMin, pMax);
            boxMax = Math.max(boxMax, pMax);
        }
        double r1 = (double)boxMin / (double)(boxcount-1);
        double r2 = (double)boxMax / (double)(boxcount-1);

        double lb = p.getMin() + r1 * (p.getMax() - p.getMin());
        double ub = p.getMin() + r2 * (p.getMax() - p.getMin());

        //System.out.println("[" + (min + (boxMin/(double)(boxcount-1))*(max-min)) + "<" + (min + (boxMax/(double)(boxcount-1))*(max-min)) + "]");

        int bestId = e.findArgBest();
        lb = Math.min(p.getValue(bestId), lb);
        ub = Math.max(p.getValue(bestId), ub);

/*        double pmax = p.getValue(p.findArgMax());
        double pmin = p.getValue(p.findArgMin());

        if (ub - lb < 0.2*(pmax-pmin)){
            double mean = (ub + lb) / 2.0;
            lb = Math.min(mean - 0.1*(pmax-pmin), lb);
            ub = Math.max(mean + 0.1*(pmax-pmin), ub);
        }*/

        return new double[]{lb,ub};
    }

    static public double[] calcPostioriDistribution(SimpleEnsemble p, EfficiencyEnsemble e, int boxcount){
        //EfficiencyEnsemble L = e.CalculateLikelihood();

        double boxes[] = new double[boxcount];       
        double elitism[] = new double[boxcount];

        int boxContentCount[] = new int[boxcount];
        Arrays.fill(boxContentCount, 0);

        double min = p.getMin();
        double max = p.getMax();

        double range = max-min;
       
        Integer ids[] = e.sort();

        double tau = 0.1;
        double elitismThreshold = e.getValue(ids[(int)(ids.length*tau)]);
        
        for (int i=0;i<ids.length*tau;i++){
            double fx = e.getValue(ids[i]);
            if (Double.isInfinite(fx) || Double.isNaN(fx))
                continue;

            double x = p.getValue(ids[i]);
            double di = (x - min)/range;
            int index = (int)(di * (double)boxcount);
            if (index == boxcount)
                index = index - 1;
            
            boxes[index] += fx;
            boxContentCount[index]++;

            if (e.isPostiveBest && fx > elitismThreshold) {
                elitism[index]++;
            }
            else if (!e.isPostiveBest && fx < elitismThreshold) {
                elitism[index]++;
            }
        }
        double elitismCountTotal = 0;
        for (int i=0;i<boxcount;i++){
            elitismCountTotal += elitism[i];
        }
        for (int i=0;i<boxcount;i++){           
            elitism[i] /= elitismCountTotal;
        }
        
        double boxMin = Double.POSITIVE_INFINITY;
        double boxMax = Double.NEGATIVE_INFINITY;

        System.out.println("min: " + e.getMin());
        for (int i=0;i<boxcount;i++){
            if (boxContentCount[i]!=0)
                boxes[i] /= (double)boxContentCount[i];

            //boxMin = Math.min(boxMin, boxes[i]);
            //boxMax = Math.max(boxMax, boxes[i]);
            
            System.out.println("box("+i+")" + elitism[i]);
        }

        boxes = Arrays.copyOf(elitism, boxcount);
        double confidence = 0.0;
        while(confidence<0.93){
            int pMax = 0;
            for (int i=0;i<boxcount;i++){
                if (elitism[i]>elitism[pMax]){
                    pMax = i;
                }
            }
            confidence += elitism[pMax];
            elitism[pMax] = 0;

            boxMin = Math.min(boxMin, pMax);
            boxMax = Math.max(boxMax, pMax);
        }
        System.out.println("[" + (min + (boxMin/(double)(boxcount-1))*(max-min)) + "<" + (min + (boxMax/(double)(boxcount-1))*(max-min)) + "]");
        return boxes;
    }
}
