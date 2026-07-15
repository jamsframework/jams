package optas.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import optas.optimizer.management.SampleFactory.Sample;

/**
 *
 * @author Nathan Lighthart
 */
public class RankingTable {

    private RankedSample[] table;
    private double alpha = 0.1;
    private Sample candidates[] = null;
    
    public RankingTable(List<Sample> samples) {
        table = new RankedSample[samples.size()];
        int i = 0;
        for (Sample sample : samples) {
            table[i] = new RankedSample(sample);
            i++;
        }
    }

    public Sample[] getCandidates(){
        return candidates;
    }
    
    public void computeRankings() {
        int m = table[0].sample.F().length;
        candidates = new Sample[m];
        
        for (int efficiency = 0; efficiency < m; efficiency++) {
            Arrays.sort(table, new SampleColumnComparator(efficiency));
            int rank = 1;
            for (RankedSample rs : table) {
                rs.rank[efficiency] += rank;
                rank++;
            }
        }
        
        for (int efficiency = 0; efficiency < m; efficiency++) {
            int maxRank = (int)Math.ceil(alpha*table.length);

            Sample bestSample = null;
            int bestRank   = Integer.MAX_VALUE;
            for (RankedSample rs : table) {
                int curRank = 0;
                if (rs.rank[efficiency] <= maxRank ) {
                    for (int i=0;i<m;i++){
                        /*if (i != m)*/{
                            curRank += rs.rank[i];
                        }
                    }
                    if (curRank < bestRank) {
                        bestRank = curRank;
                        bestSample = rs.sample;
                    }
                }
                
            }
            this.candidates[efficiency] = bestSample;
        }                
    }

    @Override
    public String toString() {
        String s = "";
        for (RankedSample rs : table) {
            s += rs.rank + " ";
            s += Arrays.toString(rs.sample.F()) + "\n";
        }
        return s;
    }
    
    public void setAlpha(double alpha){
        this.alpha = alpha;
    }
    
    public double getAlpha(){
        return alpha;
    }

    private class RankedSample {

        public int rank[];
        public Sample sample;

        public RankedSample(Sample s) {
            rank = new int[s.F().length];
            sample = s;
        }
    }

    private class SampleColumnComparator implements Comparator<RankedSample> {

        private int column;

        public SampleColumnComparator(int col) {
            column = col;
        }

        @Override
        public int compare(RankedSample o1, RankedSample o2) {
            double d = o1.sample.F()[column] - o2.sample.F()[column];
            if (Double.isNaN(o1.sample.F()[column])){
                return -1;
            }
            if (Double.isNaN(o2.sample.F()[column])){
                return  1;
            }
            if (d > 0) {
                return 1;
            } else if (d < 0) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
