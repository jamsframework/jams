/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.statistics;

import jams.JAMS;
import jams.data.Attribute;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;

/**
 *
 * @author christian
 */
public class MovingAverage extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "current time interval")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "current time interval")
    public Attribute.TimeInterval period;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "value")
    public Attribute.Double[] y;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "moving average value")
    public Attribute.Integer windowSize;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "enabled flag")
    public Attribute.Boolean[] enabled;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "moving average value")
    public Attribute.Double[] movingAvg;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "internal state of the module")
    public Attribute.Entity state;

    int n = 0, m = 0;

    @Override
    public void init() {
        n = windowSize.getValue();
        m = 0;

        for (int i = 0; i < y.length; i++) {
            if (isEnabled(i)) {
                m++;
            }
        }
    }

    @Override
    public void initAll() {
        float timeserie[][] = new float[m][n];;
        int counter = 0;
        boolean isFull = false;

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
        float timeserie[][] = (float[][]) state.getObject("timeserie");
        int counter = (Integer) state.getObject("counter");
        boolean isFull = (Boolean) state.getObject("isFull");

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
            state.setObject("timeserie", timeserie);
            state.setObject("lastTimeStep", lastTimeStep);
            state.setObject("counter", counter);
            state.setObject("isFull", isFull);
            return;
        }
        int c = 0;
        for (int i = 0; i < y.length; i++) {
            double avg = 0;
            if (isEnabled(i)) {
                timeserie[c][counter] = (float) y[i].getValue();
                for (int j = 0; j < n; j++) {
                    avg += timeserie[c][j];
                }
                if (isFull) {
                    movingAvg[i].setValue(avg / n);
                } else {
                    movingAvg[i].setValue(JAMS.getMissingDataValue());
                }
                c++;
            }
        }
        if (counter == n - 1) {
            isFull = true;
        }
        counter = (counter + 1) % n;

        state.setObject("timeserie", timeserie);
        state.setObject("lastTimeStep", lastTimeStep);
        state.setObject("counter", counter);
        state.setObject("isFull", isFull);
    }
}
