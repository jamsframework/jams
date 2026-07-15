/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author chris
 */
public class OptimalOptimizationScheme extends OptimizationScheme{
    //something technical
    ObjectPool<IdentifiableHashSet<ParameterGroup>> spgPool = null;
    ObjectPool<ParameterGroup> pgPool = null;

    @Override
    public String toString(){
        return "Optimal";
    }

    static public class IdentifiableHashSet<T> extends HashSet<T> implements Identifiable{
        static int counter = 0;
        int id;
        IdentifiableHashSet(){
            super();
            id = counter++;
        }
        public int getID(){
            return id;
        }
    }
    
    static public class IntWrapper{
        Integer c;
        IntWrapper(int v){
            c = new Integer(v);
        }
    }
        
    public OptimalOptimizationScheme() {

    }

    private void initObjectPools(){
        IdentifiableHashSet<ParameterGroup> poolData[] = new IdentifiableHashSet[100000];

        for (int i=0;i<poolData.length;i++)
            poolData[i] = new IdentifiableHashSet<ParameterGroup>();
        spgPool = new ObjectPool<IdentifiableHashSet<ParameterGroup>>(poolData);

        ParameterGroup pgPoolData[] = new ParameterGroup[2000000];
        for (int i=0;i<pgPoolData.length;i++)
            pgPoolData[i] = new ParameterGroup(this.parameter,weights.length);

        pgPool = new ObjectPool<ParameterGroup>(pgPoolData);
    }


    public IdentifiableHashSet<ParameterGroup> findSolutions(ParameterGroup availableParameters, double threshold, double oldUnusableSum, int size) {
        IdentifiableHashSet<ParameterGroup> allSolutions = spgPool.allocate();
        allSolutions.clear();

        if (size == 0) {
            if (threshold <= 0) {
                allSolutions.add(availableParameters.createEmptyGroup(pgPool.allocate()));
            }
            return allSolutions;
        }

        double unusableSum = oldUnusableSum;
        int j = 0;

        ParameterGroup group = availableParameters.copy(pgPool.allocate());
        group.sortByWeight();

        while (unusableSum < 1.0 - threshold && group.size>0) {
            int p_j = group.get(j);
            double w_j = group.weight(p_j);
            group.remove(j);
            IdentifiableHashSet<ParameterGroup> solutions = findSolutions(group,
                    threshold - w_j, unusableSum + w_j, size - 1);
            for (ParameterGroup p : solutions) {
                p.add(p_j);
                allSolutions.add(p);
            }
            spgPool.free(solutions);
            unusableSum += w_j;
            j++;
        }
        pgPool.free(group);
        return allSolutions;
    }

    public ParameterGroup calculateOptimalGroup(ParameterGroup availableParameters, double weight[][]) {                
        HashMap<ParameterGroup, IntWrapper> countList = new HashMap<ParameterGroup, IntWrapper>();
        int k = 1;
        while (true) {
            double rank[] = new double[n];
            for (int t = 0; t < T; t++) {
                for (int j = 0; j < n; j++) {
                    rank[j] += weight[j][t];
                }
            }
            ParameterGroup reducedGroup = availableParameters.copy();

            //notice here the difference betwenn max_value and pos. inf
            for (int j = 0; j < n; j++) {
                if (!availableParameters.getMap(j))
                    rank[j] = Double.POSITIVE_INFINITY;
            }
            while (reducedGroup.size>25){
                double min = Double.MAX_VALUE;
                int argMin = 0;
                for (int i=0;i<n;i++){
                    if (rank[i]<min){
                        min = rank[i];
                        argMin = i;                        
                    }
                }
                rank[argMin] = Double.POSITIVE_INFINITY;
                reducedGroup.remove(reducedGroup.getIndex(argMin));
            }

            for (int t = 0; t < T; t++) {
                reducedGroup.w = new double[n];
                for (int j=0;j<n;j++)
                    reducedGroup.w[j] = weight[j][t];
                reducedGroup.w = reducedGroup.normalizeWeight();
                
                IdentifiableHashSet<ParameterGroup> solutions = findSolutions(reducedGroup, threshold, 0, k);

                for (ParameterGroup p : solutions) {
                    IntWrapper c = countList.get(p);
                    if (c == null) {
                        countList.put(p, new IntWrapper(1));
                    } else {
                        c.c++;
                        pgPool.free(p);
                    }
                    //remove small solutions .. 
                    if (pgPool.load()>0.75){
                        ArrayList<ParameterGroup> removeList = new ArrayList<ParameterGroup>();
                        for (ParameterGroup key : countList.keySet()){
                            IntWrapper value = countList.get(key) ;
                            if (value.c.intValue() < 0.01*t){
                                removeList.add(key);
                            }
                        }
                        for (ParameterGroup key : removeList){
                            countList.remove(key);
                            pgPool.free(key);
                        }
                    }
                }
                spgPool.free(solutions);
            }
            if (countList.size()>0){
                int max = 0;
                ParameterGroup argMax = null;
                for (ParameterGroup p : countList.keySet()){
                    IntWrapper value = countList.get(p) ;
                    if (value.c.intValue() > max){
                        max = value.c;
                        argMax = p;
                    }
                }
                if ((double) max / (double) T > 0.1) {
                    return argMax.copy();
                }
            }

            pgPool.reset();
            countList.clear();
            k++;
        }
    }
        
    public void calcOptimizationScheme() {
        initObjectPools();
        solutionGroups.clear();
        dominatedTimeStepsForGroup.clear();

        ParameterGroup allParameters = new ParameterGroup(this.parameter,n);
        while (allParameters.size > 0) {
            ParameterGroup p = calculateOptimalGroup(allParameters, weights);            
            solutionGroups.add(p);
            this.update();
            allParameters.sub(p);
        }
    }   
}
