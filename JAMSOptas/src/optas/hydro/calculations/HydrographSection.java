/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro.calculations;

import java.util.ArrayList;
import optas.data.TimeSerie;

/**
 *
 * @author chris
 */
public class HydrographSection {

    public int startIndex;
    public int endIndex;
    ArrayList<Double> value;

    public HydrographSection(int startIndex, double value) {
        this.startIndex = startIndex;
        this.value = new ArrayList<Double>();
        this.value.add(new Double(value));
        this.endIndex = startIndex + 1;
    }

     public HydrographSection(int startIndex, int endIndex, TimeSerie t) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.value = new ArrayList<Double>();
        for (int i=startIndex;i<endIndex;i++){
            this.value.add(new Double(t.getValue(i)));
        }
    }

    public void add(double value) {
        this.value.add(new Double(value));
        endIndex++;
    }

    protected int getIntervalLength() {
        return endIndex - startIndex;
    }

    public double at(int index) {
        return this.value.get(index - startIndex);
    }
}
