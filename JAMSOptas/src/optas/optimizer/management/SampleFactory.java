/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.optimizer.management;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author chris
 */
public class SampleFactory implements Serializable {

    private ArrayList<Sample> sampleList = new ArrayList<Sample>();

    Statistics stats = null;

    public SampleFactory(){
        stats = new Statistics(sampleList);
    }

    public Statistics getStatistics(){
        return this.stats;
    }

    public void injectSample(Sample s){
        this.sampleList.add(s);
    }

    //class for representing samples
    public class Sample implements Serializable {

        public double[] x;
        protected double[] fx;

        private Sample() {
        }

        @SuppressWarnings("LeakingThisInConstructor")
        private Sample(double[] x, double fx[]) {
            if (x == null || fx == null) {
                return;
            }

            this.fx = Arrays.copyOf(fx, fx.length);
            this.x = Arrays.copyOf(x, x.length);            
        }

        public double[] getParameter() {
            return x;
        }

        public double[] F() {
            return this.fx;
        }

        @Override
        public Sample clone() {
            Sample cpy = new Sample();
            cpy.x = new double[x.length];
            cpy.fx = new double[fx.length];
            System.arraycopy(x, 0, cpy.x, 0, x.length);
            System.arraycopy(fx, 0, cpy.fx, 0, fx.length);
            return cpy;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Sample)) {
                return false;
            }
            Sample s = (Sample) obj;
            if (s.x.length != this.x.length) {
                return false;
            }
            if (s.fx.length != this.fx.length) {
                return false;
            }

            for (int i = 0; i < this.x.length; i++) {
                if (s.x[i] != x[i]) {
                    return false;
                }
            }
            return true;
        }
        //automatically gemerated

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 79 * hash + Arrays.hashCode(this.x);
            hash = 79 * hash + Arrays.hashCode(this.fx);
            return hash;
        }

        @Override
        public String toString() {
            String s = "";
            for (int i = 0; i < x.length; i++) {
                s += x[i] + "\t";
            }
            for (int i = 0; i < fx.length; i++) {
                s += fx[i] + "\t";
            }
            return s;
        }
    }

    //class for representing samples
    public class SampleSO extends Sample {        
        private SampleSO() {
        }

        private SampleSO(double[] x, double fx) {
            super(x, new double[]{fx});
            this.fx[0] = fx;
            this.x = super.getParameter();
        }

        @Override
        public SampleSO clone() {
            Sample x = super.clone();
            //this does not produce an add
            SampleSO s = new SampleSO();
            s.fx = x.fx;
            s.x = x.x;
            return s;
        }

        public double f() {
            return fx[0];
        }
    }
    
    public ArrayList<Sample> getSampleList() {
        return this.sampleList;
    }

    //compare samples
    static public class SampleSOComperator implements Comparator {

        private int order = 1;

        public SampleSOComperator(boolean decreasing_order) {
            order = decreasing_order ? -1 : 1;
        }

        public int compare(Object d1, Object d2) {
            return Double.compare(((SampleSO) d1).f(), ((SampleSO) d2).f())*order;
        }
    }
    /*    SampleMO getFromSampleList(int i){
    return (SampleMO)this.sampleList.get(i);
    }*/

    static public class SampleIndexComperator implements Comparator {

        private int order = 1;
        private int index;

        public SampleIndexComperator(int index, boolean decreasing_order) {
            order = decreasing_order ? -1 : 1;
            this.index = index;
        }

        public int compare(Object d1, Object d2) {
            if (((Sample) d1).fx.length != ((Sample) d2).fx.length) {
                return 0;
            }
            int ord = 0;
            int i = index;

            if (((Sample) d1).fx[i] < ((Sample) d2).fx[i]) {
                ord = -1 * order;
            } else if (Math.abs(((Sample) d1).fx[i] - ((Sample) d2).fx[i]) < 0.000000001) {
                ord = 0 * order;
            } else {
                ord = 1 * order;
            }

            return ord;
        }
    }
    //compare samples

    static public class SampleComperator implements Comparator, Serializable {

        private int order = 1;
        private boolean subset[] = null;
                
        public SampleComperator(boolean decreasing_order, boolean subset[]) {
            order = decreasing_order ? -1 : 1;
            this.subset = subset;
        }
        
        public SampleComperator(boolean decreasing_order) {
            order = decreasing_order ? -1 : 1;
        }

        public int compare(Object d1, Object d2) {
            int m = ((Sample) d1).fx.length;
            if (((Sample) d1).fx.length != ((Sample) d2).fx.length) {
                return 0;
            }
            int ord = 0;
            for (int i = 0; i < m; i++) {
                if (subset!=null && !subset[i])
                    continue;
                
                int nextOrd;
                if (((Sample) d1).fx[i] < ((Sample) d2).fx[i]) {
                    nextOrd = -1 * order;
                } else if (Math.abs(((Sample) d1).fx[i] - ((Sample) d2).fx[i]) < 0.000000001) {
                    nextOrd = 0 * order;
                } else {
                    nextOrd = 1 * order;
                }
                if (ord == 0) {
                    ord = nextOrd;
                } else {
                    if (ord != nextOrd && nextOrd != 0) {
                        return 0;
                    }
                }
            }
            return ord;
        }
    }

    public SampleSO getSampleSO(double[] x, double fx) {
        SampleSO s = new SampleSO(x, fx);
        sampleList.add(s);
        this.stats.fireChangeEvent();
        return s;
    }

    public Sample getSample(double x[], double fx[]) {
        Sample s = new Sample(x, fx);
        sampleList.add(s);
        this.stats.fireChangeEvent();
        return s;
    }

    public int getSize() {
        return this.sampleList.size();
    }
}
