/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.statistics;

import jams.JAMS;
import jams.data.AbstractDataSupplier;
import jams.data.Attribute;
import jams.math.statistics.MannKendall.*;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;

/**
 *
 * @author christian
 */
public class MannKendall extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "current time interval")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "time interval for trend estimation")
    public Attribute.TimeInterval period;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "window size",
            defaultValue = "100000")
    public Attribute.Integer windowSize;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "value of trend")
    public Attribute.Double[] y;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "value of trend")
    public Attribute.Boolean[] enabled;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "kendalls p value")
    public Attribute.Double[] p;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "kendalls tau value")
    public Attribute.Double[] tau;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "linreg ax+b")
    public Attribute.Double[] a;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "linreg ax+b")
    public Attribute.Double[] b;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "linreg r2")
    public Attribute.Double[] r2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "coefficient of variation")
    public Attribute.Double[] V;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "internal state of the module")
    public Attribute.Entity state;
    
    int n, m;

    WrappedDataSupplier supplier = new WrappedDataSupplier(null);

    private class WrappedDataSupplier extends AbstractDataSupplier<Double, float[]> {

        int counter = 0;

        WrappedDataSupplier(float srcData[]) {
            super(srcData);
        }

        public void setSourceData(float srcData[], int counter) {
            input = srcData;
            this.counter = counter;
        }

        @Override
        public int size() {
            return n;
        }

        @Override
        public Double get(int i) {
            return (double) input[(counter + i) % n];
        }
    }

    @Override
    public void initAll() {
        float timeserie[][] = null;

        int counter = 0;
        boolean isFull = false;
    
        if (period == null) {
            n = windowSize.getValue();
        } else if (windowSize.getValue() != -1){
            period.setTimeUnit(Attribute.Calendar.YEAR);
            period.setTimeUnitCount(1);
            n = Math.min((int) period.getNumberOfTimesteps(), windowSize.getValue());
        } else{
            period.setTimeUnit(Attribute.Calendar.YEAR);
            period.setTimeUnitCount(1);
            n = (int) period.getNumberOfTimesteps();
        }
        m = 0;
        isFull = false;

        for (int i = 0; i < y.length; i++) {
            if (isEnabled(i)) {
                m++;
            }
        }
        //this quite an amount of memory .. 
        timeserie = new float[m][n];
        counter = 0;
        
        state.setObject("timeserie", timeserie);
        state.setObject("lastTimeStep", Long.MIN_VALUE);
        state.setObject("counter", counter);
        state.setObject("isFull", isFull);
    }

    private boolean isEnabled(int i) {
        return (enabled == null || enabled[i].getValue());
    }

    @Override
    public void run() {
        Long lastTimeStep = (Long)state.getObject("lastTimeStep");        
        float timeserie[][] = (float[][])state.getObject("timeserie");
        int counter = (Integer)state.getObject("counter");
        boolean isFull = (Boolean)state.getObject("isFull");

                
        boolean considerData = true;        
        if (time != null) {
            if (time.getTimeInMillis() == JAMS.getMissingDataValue(Long.class)){
                return;
            }
            if (lastTimeStep == time.getTimeInMillis()) {
                considerData = false;
            } else {
                lastTimeStep = time.getTimeInMillis();
            }
            if (period != null) {
                if (time.before(period.getStart())
                        || time.after(period.getEnd())) {
                    considerData = false;
                }
            }
        }

        if (!considerData) {
            return;
        }
        /*if (counter>=n){
         getModel().getRuntime().sendHalt("Error: Capacity of array is not sufficient in Mann-Kendall Test" + "\nTimestep of time period is not consistent with real time step!");
         return;
         }*/
        int c = 0;
        for (int i = 0; i < y.length; i++) {
            if (isEnabled(i)) {
                timeserie[c++][counter] = (float) y[i].getValue();
            }
        }
        //wrap at the end
        counter = (counter + 1) % n;

        if (counter == 0) {
            isFull = true;
        }

        //test if it is the last timestep
        time.add(period.getTimeUnit(), period.getTimeUnitCount());
        if (isFull || !time.before(period.getEnd())) {
            c = 0;
            for (int i = 0; i < y.length; i++) {
                double resultKendall[] = new double[5];
                double resultLinReg[] = new double[4];
                if (isEnabled(i)) {
                    supplier.setSourceData(timeserie[c], counter);
                    resultKendall = jams.math.statistics.MannKendall.Kendall(supplier);
                    resultLinReg = jams.math.statistics.MannKendall.LinearRegression(supplier);
                    c++;
                }
                if (this.tau != null) {
                    this.tau[i].setValue(resultKendall[0]);
                }
                if (this.p != null) {
                    this.p[i].setValue(resultKendall[1]);
                }
                if (this.a != null) {
                    this.a[i].setValue(resultLinReg[0]);
                }
                if (this.b != null) {
                    this.b[i].setValue(resultLinReg[1]);
                }
                if (this.r2 != null) {
                    this.r2[i].setValue(resultLinReg[2]);
                }
                if (this.V != null) {
                    this.V[i].setValue(resultLinReg[3]);
                }
            }
        }
        time.add(period.getTimeUnit(), -period.getTimeUnitCount());
        
        state.setObject("timeserie", timeserie);
        state.setObject("lastTimeStep", lastTimeStep);
        state.setObject("counter", counter);
        state.setObject("isFull", isFull);
    } 
}
