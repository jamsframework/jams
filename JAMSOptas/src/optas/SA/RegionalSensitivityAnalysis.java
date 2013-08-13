/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.SA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import optas.data.EfficiencyEnsemble;
import optas.data.SimpleEnsemble;
import optas.optimizer.management.SampleFactory.Sample;

/**
 *
 * @author chris
 */
public class RegionalSensitivityAnalysis extends SensitivityAnalyzer{

    int currentIndex = 0;
    SimpleEnsemble x[] = null;
    
    @Override
    public void calculate(){
        super.calculate();
        
        ArrayList<Integer> behavourialBox = new ArrayList<Integer>();
        ArrayList<Integer> nonBehavourialBox = new ArrayList<Integer>();

        //do default sampling
        ArrayList<Sample> s = getRandomSampling();
        
        x = new SimpleEnsemble[n];
        for (int j=0;j<n;j++){
            x[j] = new SimpleEnsemble(Integer.toString(j), s.size());
        }
        EfficiencyEnsemble y = new EfficiencyEnsemble("y", s.size(), true);

        for (int i=0;i<s.size();i++){
            Sample sample = s.get(i);
            for (int j=0;j<n;j++){
                x[j].add(i, sample.x[j]);
            }
            y.add(i, sample.F()[0]);
        }

        EfficiencyEnsemble likelihood = y.CalculateLikelihood();
        Integer sortedIds[] = likelihood.sort();

        sensitivityIndex =new double[n];

        for (int j = 0; j < sampleSize; j++) {
            if (j > sampleSize / 2) {
                behavourialBox.add(sortedIds[j]);
            } else {
                nonBehavourialBox.add(sortedIds[j]);
            }
        }

        //sort data into boxes
        for (int i = 0; i < n; i++) {            
            this.currentIndex = i;

            Collections.sort(behavourialBox, new Comparator<Integer>() {

                @Override
                public int compare(Integer i, Integer j) {
                    double vi = RegionalSensitivityAnalysis.this.x[currentIndex].getValue(i);
                    double vj = RegionalSensitivityAnalysis.this.x[currentIndex].getValue(j);

                    if (vi < vj){
                        return -1;
                    }else if (vi == vj){
                        return 0;
                    }else{
                        return 1;
                    }
                }
            });

            Collections.sort(nonBehavourialBox, new Comparator<Integer>() {

                @Override
                public int compare(Integer i, Integer j) {
                    double vi = RegionalSensitivityAnalysis.this.x[currentIndex].getValue(i);
                    double vj = RegionalSensitivityAnalysis.this.x[currentIndex].getValue(j);

                    if (vi < vj){
                        return -1;
                    }else if (vi == vj){
                        return 0;
                    }else{
                        return 1;
                    }
                }
            });

            double behavourialDistribution = 0;
            double nonBehavourialDistribution = 0;

            double step1 = 1.0 / (double)behavourialBox.size();
            double step2 = 1.0 / (double)nonBehavourialBox.size();

            int k2 = 0;
            sensitivityIndex[i] = 0;

            /*System.out.println("Processing: " + RegionalSensitivityAnalysis.this.x[currentIndex].getName());
            System.out.println("Values in behavourial box:");
            for (int j=0;j<behavourialBox.size();j++){
                System.out.println("x:" + x[currentIndex].getValue(behavourialBox.get(j)) + "\ty:" + y.getValue(behavourialBox.get(j)));
            }

            System.out.println("Values in non-behavourial box:");
            for (int j=0;j<nonBehavourialBox.size();j++){
                System.out.println("x:" + x[currentIndex].getValue(nonBehavourialBox.get(j)) + "\ty:" + y.getValue(nonBehavourialBox.get(j)));
            }*/

            //System.out.println("Parameter: " + x[currentIndex]);

            for (int k=0;k<behavourialBox.size();k++){
                double value = x[currentIndex].getValue(behavourialBox.get(k));
                double value2 = x[currentIndex].getValue(nonBehavourialBox.get(k2));

                //System.out.println("value1:" + value + "\tvalue2:" + value2);

                while(value2<value && k2 < nonBehavourialBox.size()-1){
                    k2++;
                    value2 = x[currentIndex].getValue(nonBehavourialBox.get(k2));
                    nonBehavourialDistribution += step2;
                }
                behavourialDistribution += step1;
                //  System.out.println(behavourialDistribution + "\t" + nonBehavourialDistribution);
                //System.out.println("y1:" + behavourialDistribution + "\ty2:" + nonBehavourialDistribution);

                if (Math.abs(behavourialDistribution - nonBehavourialDistribution) > sensitivityIndex[i]){
                    sensitivityIndex[i] = Math.abs(behavourialDistribution - nonBehavourialDistribution);
                }
            }
        }
        double sum = 0;
        for (int k=0;k<n;k++){
            sum += sensitivityIndex[k];
        }
        for (int k=0;k<n;k++){
            sensitivityIndex[k] /= sum;
        }
    }

}
