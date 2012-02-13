/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro;

import java.util.ArrayList;

/**
 *
 * @author chris
 */
public class SimilarityBasedOptimizationScheme extends OptimizationScheme{

    @Override
    public String toString(){
        return "Similarity";
    }

    public class Similarity{
        ParameterGroup p1;
        ParameterGroup p2;
        double similariy;

        public Similarity(ParameterGroup p1, ParameterGroup p2, double similarity){
            this.p1 = p1;
            this.p2 = p2;
            this.similariy = similarity;
        }
    }

    public double calcR2(double[]t1, double[]t2){
        double mean = 0;
        for (int t=0;t<T;t++){
            mean += t1[t];
        }
        mean /= (double)T;

        double numerator=0;
        double denumerator=0;
        for (int t=0;t<T;t++){
            numerator += ((t1[t]-t2[t])*(t1[t]-t2[t]));
            denumerator += ((t1[t]-mean)*(t1[t]-mean));

        }
        return 1.0 - numerator/denumerator;
    }

    public double calcSimilarty(ParameterGroup p1, ParameterGroup p2){
        double w[] = new double[n];

        double ts1[] = new double[T];
        double ts2[] = new double[T];

        for (int t=0;t<T;t++){
            for (int i=0;i<n;i++){
                w[i] = weights[i][t];
            }
            p1.w = w;
            p2.w = w;
            ts1[t] = p1.calcNorm();
            ts2[t] = p2.calcNorm();
        }

        return calcR2(ts1,ts2);
    }

    public void calcOptimizationScheme() {
        ArrayList<ParameterGroup> availableGroups = new ArrayList<ParameterGroup>();
        

        for (int i=0;i<n;i++){
            ParameterGroup p = new ParameterGroup(this.parameter,n);
            p = p.createEmptyGroup();
            p.add(i);

            availableGroups.add(p);
        }


        double similarity = 1.0;
        while (similarity > 0.0){
            double maxSimilarity = Double.NEGATIVE_INFINITY;

            ParameterGroup bestGroup1=null, bestGroup2=null;

            for (int i=0;i<availableGroups.size();i++){
                for (int j=i+1;j<availableGroups.size();j++){
                    double testSimilarity = 0;

                    for (int k=0;k<availableGroups.get(i).size;k++){
                        ParameterGroup pk = new ParameterGroup(this.parameter,n);
                        pk = pk.createEmptyGroup();
                        pk.add(availableGroups.get(i).get(k));

                        for (int l=0;l<availableGroups.get(j).size;l++){
                            ParameterGroup pl = new ParameterGroup(this.parameter,n);
                            pl = pl.createEmptyGroup();
                            pl.add(availableGroups.get(j).get(l));

                            testSimilarity += calcSimilarty(pk, pl);
                        }
                    }
                    testSimilarity /= (double)(availableGroups.get(i).size * availableGroups.get(j).size);

                    if (testSimilarity > maxSimilarity){
                        maxSimilarity = testSimilarity;
                        bestGroup1 = availableGroups.get(i);
                        bestGroup2 = availableGroups.get(j);
                    }
                }
            }
            similarity = maxSimilarity;
            availableGroups.remove(bestGroup2);
            bestGroup1.add(bestGroup2);
        }

        double domination[] = new double[availableGroups.size()];
        double w[] = new double[n];
        int j=0;
        for (ParameterGroup p : availableGroups){
            for (int t=0;t<T;t++){
                for (int i=0;i<n;i++){
                    w[i] = this.weights[i][t];
                }
                p.w = w;
                domination[j] += p.calcNorm();
            }
            j++;
        }

        boolean changes = true;
        while(changes){
            changes = false;
            for (int i=0;i<domination.length-1;i++){
                if (domination[i]<domination[i+1]){
                    //flip
                    double tmp = domination[i];
                    ParameterGroup tmp2 = availableGroups.get(i);

                    domination[i] = domination[i+1];
                    availableGroups.set(i, availableGroups.get(i+1));

                    domination[i+1] = tmp;
                    availableGroups.set(i+1, tmp2);
                    changes = true;
                }
            }
        }
        this.solutionGroups = availableGroups;
        ParameterGroup allParameters = new ParameterGroup(this.parameter,n);
        for (ParameterGroup p : solutionGroups){
            this.dominatedTimeStepsForGroup.add(this.calcDominatedTimeSteps(p, allParameters));
            allParameters.sub(p);
        }
    }    
}
