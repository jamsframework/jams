/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.component.indices;

import jams.data.Attribute;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author christian
 */
@JAMSComponentDescription(
        title = "KlimaKennwerte",
        author = "Christian Fischer",
        description = "Calculated standard Klimakennwerte for J2000Klima")
public class PfannschmidtIndices extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Current time")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "daily min temperatur",
            unit = "°C")
    public Attribute.Double tmin;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "daily mean temperatur",
            unit = "°C")
    public Attribute.Double tmean;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "daily max temperatur",
            unit = "°C")
    public Attribute.Double tmax;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the summer index after pfannschmidt (mean of tmax between first and last summer day)")
    public Attribute.Double summerIndex;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the summer index after pfannschmidt (mean of tmax between first and last summer day)")
    public Attribute.Double summerStartDay;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the summer index after pfannschmidt (mean of tmax between first and last summer day)")
    public Attribute.Double winterIndex;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the summer index after pfannschmidt (mean of tmax between first and last summer day)")
    public Attribute.Double winterStartDay;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "switch for histogramm calculation. If 1 than the histogramm is applied otherwise the mean is used",
            defaultValue = "false")
    public Attribute.Boolean histogrammCalculation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "tmp variables")
    public Attribute.Object internalState;

    private final int INDEX_SUMMERINDEX = 0;
    private final int INDEX_WINTERINDEX = 1;
    private final int INDEX_SIZE = 2;

    @Override
    public void run() {
        double dayOfYear = time.get(Calendar.DAY_OF_YEAR);
        boolean isSummerDay = false, isWinterDay = false;

        if (tmax.getValue() >= 25.0) {
            if (summerStartDay.getValue() == 0) {
                summerStartDay.setValue(dayOfYear);
            }
            isSummerDay = true;
        }

        //Frosttage und Frostperioden
        if (tmin.getValue() < 0.0) {
            if (winterStartDay.getValue() == 0) {
                winterStartDay.setValue(dayOfYear);
            }
            isWinterDay = true;
        }

        //pfannschmidt indices
        if (histogrammCalculation.getValue()) {
            PfannschmidtHistogramm((int) dayOfYear, isSummerDay, isWinterDay);

        } else {
            PfannschmidtMean((int) dayOfYear, isSummerDay, isWinterDay);
        }
    }

    private void PfannschmidtMean(int dayOfYear, boolean isSummerDay, boolean isWinterDay) {
        if (internalState.getValue() == null) {
            internalState.setValue(new double[INDEX_SIZE]);
        }
        double[] inTmp = (double[]) internalState.getValue();

        if (summerStartDay.getValue() > 0) {
            inTmp[INDEX_SUMMERINDEX] += tmax.getValue();
            if (isSummerDay) {
                double days = dayOfYear - this.summerStartDay.getValue() + 1;
                this.summerIndex.setValue(inTmp[INDEX_SUMMERINDEX] / days);
            }
        }
        if (winterStartDay.getValue() > 0) {
            inTmp[INDEX_WINTERINDEX] += tmin.getValue();
            if (isWinterDay) {
                double days = dayOfYear - this.winterStartDay.getValue() + 1;
                if (days < 0) {
                    days += 365;
                }
                this.winterIndex.setValue(inTmp[INDEX_WINTERINDEX] / days);
            }
        }
        //reset counters .. 
        if (dayOfYear == 1) {
            summerStartDay.setValue(0);
            inTmp[INDEX_SUMMERINDEX] = 0;
        }
        if (dayOfYear == 180) {
            winterStartDay.setValue(0);
            inTmp[INDEX_WINTERINDEX] = 0;
        }
    }

    private void PfannschmidtHistogramm(int dayOfYear, boolean isSummerDay, boolean isWinterDay) {
        if (internalState.getValue() == null) {
            ArrayList tmp[] = new ArrayList[INDEX_SIZE];
            tmp[0] = new ArrayList<Double>();
            tmp[1] = new ArrayList<Double>();
            internalState.setValue(tmp);
        }
        ArrayList<Double> inTmp[] = (ArrayList[]) internalState.getValue();
        ArrayList<Double> summerHistogramm = inTmp[INDEX_SUMMERINDEX];
        ArrayList<Double> winterHistogramm = inTmp[INDEX_WINTERINDEX];

        if (summerStartDay.getValue() > 0) {
            int tempClass = (int) tmax.getValue();
            //order in one direction 0 1 -1 2 -2 3 -3 ... 
            if (tempClass < 0) {
                tempClass = -2 * tempClass;
            } else {
                tempClass = tempClass * 2 + 1;
            }
            while (summerHistogramm.size() <= tempClass) {
                summerHistogramm.add(0.);
            }
            summerHistogramm.set(tempClass, summerHistogramm.get(tempClass) + 1);
            if (isSummerDay) {
                double wSum = 0;
                double total = 0;
                for (int i = 0; i < summerHistogramm.size(); i++) {
                    double classMid = 0;
                    //negative values
                    if (i % 2 == 0) {
                        classMid = -i / 2 + 0.5;
                    } else { //positive values
                        classMid = (i - 1) / 2 + 0.5;
                    }
                    wSum += classMid * summerHistogramm.get(i);
                    total += summerHistogramm.get(i);
                }
                this.summerIndex.setValue(wSum / total);
            }
        }
        if (winterStartDay.getValue() > 0) {
            int tempClass = (int) tmin.getValue();
            //order in one direction 0 1 -1 2 -2 3 -3 ... 
            if (tempClass < 0) {
                tempClass = -2 * tempClass;
            } else {
                tempClass = tempClass * 2 + 1;
            }
            while (winterHistogramm.size() <= tempClass) {
                winterHistogramm.add(0.);
            }
            winterHistogramm.set(tempClass, winterHistogramm.get(tempClass) + 1.);
            if (isWinterDay) {
                double wSum = 0;
                double total = 0;
                for (int i = 0; i < winterHistogramm.size(); i++) {
                    double classMid = 0;
                    //negative values
                    if (i % 2 == 0) {
                        classMid = -i / 2 + 0.5;
                    } else { //positive values
                        classMid = (i - 1) / 2 + 0.5;
                    }
                    wSum += classMid * winterHistogramm.get(i);
                    total += winterHistogramm.get(i);
                }
                this.winterIndex.setValue(wSum / total);
            }
        }
        //reset counters .. 
        if (dayOfYear == 1) {
            //System.out.println("Start: " + summerStartDay + "\t Index:" + summerIndex);
            summerStartDay.setValue(0);
            for (int i = 0; i < summerHistogramm.size(); i++) {
                inTmp[INDEX_SUMMERINDEX].set(i, 0.);
            }
        }
        if (dayOfYear == 180) {
            //System.out.println("Start: " + winterStartDay + "\t Index:" + winterIndex);
            winterStartDay.setValue(0);
            /*double wSum = 0;
                double total = 0;
                for (int i = 0; i < winterHistogramm.size(); i++) {
                    double classMid = 0;
                    //negative values
                    if (i % 2 == 0) {
                        classMid = -i / 2 + 0.5;
                    } else { //positive values
                        classMid = (i - 1) / 2 + 0.5;
                    }
                    wSum += classMid * winterHistogramm.get(i);
                    total += winterHistogramm.get(i);
                }
                System.out.println("Total:" + total);*/
            for (int i = 0; i < winterHistogramm.size(); i++) {
                inTmp[INDEX_WINTERINDEX].set(i, 0.);
            }
        }
    }
}
