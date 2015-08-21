/*
 * StandardEfficiencyCalculator.java
 * Created on 24. November 2005, 09:48
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package org.unijena.j2k.efficiencies;

import java.util.Locale;
import java.util.Vector;

import org.unijena.j2k.statistics.Regression;
import jams.JAMS;
import jams.data.*;
import jams.model.*;
import java.util.GregorianCalendar;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title = "StandardEfficiencyCalculator",
        author = "Peter Krause",
        description = "Calculates various efficiency measures",
        version = "1.0_0",
        date = "2010-10-29"
)
public class StandardEfficiencyCalculator extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the current modelling time step"
    )
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The model time interval"
    )
    public Attribute.TimeInterval modelTimeInterval;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The efficiency time interval, a subset of modelTimeInterval"
    )
    public Attribute.TimeInterval effTimeInterval;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "comma separated list of months (1-12) which should be evaluated, can be left out if not needed"
    )
    public Attribute.IntegerArray effMonthList;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "a comma separated list of integer values indicating which efficiency criteria should be computed"
    )
    public Attribute.IntegerArray effMethod;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Prediction value"
    )
    public Attribute.Double prediction;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Validation value"
    )
    public Attribute.Double validation;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Nash-Sutcliffe-efficiency with power 1.0",
            unit = "n/a",
            lowerBound = Double.NEGATIVE_INFINITY,
            upperBound = 1
    )
    public Attribute.Double e1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Nash-Sutcliffe-efficiency with power 2.0",
            unit = "n/a",
            lowerBound = -Double.NEGATIVE_INFINITY,
            upperBound = 1
    )
    public Attribute.Double e2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Nash-Sutcliffe-efficiency from logarithmic values with power 1.0",
            unit = "n/a",
            lowerBound = Double.NEGATIVE_INFINITY,
            upperBound = 1
    )
    public Attribute.Double le1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Nash-Sutcliffe-efficiency  from logarithmic values with power 2.0",
            unit = "n/a",
            lowerBound = Double.NEGATIVE_INFINITY,
            upperBound = 1
    )
    public Attribute.Double le2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Willmot's index of agreement with power 1.0",
            unit = "n/a",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double ioa1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Willmot's index of agreement with power 2.0",
            unit = "n/a",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double ioa2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "coefficient of determination r^2",
            unit = "n/a",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double rsq;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "gradient of linear regression",
            unit = "n/a",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double grad;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "weighted rsq",
            unit = "n/a",
            lowerBound = 0,
            upperBound = 1)
    public Attribute.Double wrsq;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "gradient of double sum regression",
            unit = "n/a",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double dsGrad;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "absolute volume error",
            unit = "same as values",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double absVolErr;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "root mean square error",
            unit = "same as values",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double rmse;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "percent bias",
            unit = "%",
            lowerBound = Double.NEGATIVE_INFINITY,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double pbias;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "absolute percent bias",
            unit = "%",
            lowerBound = 0,
            upperBound = Double.POSITIVE_INFINITY
    )
    public Attribute.Double apbias;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "full set of predicted values"
    )
    public Attribute.DoubleArray predictionValues;

    private final int E1 = 1;
    private final int E2 = 2;
    private final int LOG_E1 = 3;
    private final int LOG_E2 = 4;
    private final int IOA_1 = 5;
    private final int IOA_2 = 6;
    private final int R2 = 7;
    private final int WR2 = 8;
    private final int DSGRAD = 9;
    private final int ABSVOLERROR = 10;
    private final int RMSE = 11;
    private final int PBIAS = 12;
    private final int APBIAS = 13;

    private double[] valData;
    private double[] preData;

    private int counter = 0;

    private int interValStart = 0;
    private int interValEnd = 0;

    private int effTsteps = 0;

    private boolean monthly = false;
    private int monthCount = 0;

    /*
     *  Component run stages
     */
    public void init() {

        //some checking if time intervals correlate well
        //....
        //....
        this.counter = 0;
        this.monthCount = 0;
        Attribute.Calendar model_sd = this.modelTimeInterval.getStart().clone();
        Attribute.Calendar model_ed = this.modelTimeInterval.getEnd().clone();
        long sdMod = model_sd.getTimeInMillis();

        long model_tsteps = 0;
        /*if(model_tres == model_sd.DAY_OF_YEAR){
         model_tsteps = (edMod - sdMod) / (1000 * 60 * 60 * 24);
         model_tsteps = model_tsteps + 1 + 1;
         }*/
        model_tsteps = modelTimeInterval.getNumberOfTimesteps();

        Attribute.Calendar eff_sd = this.effTimeInterval.getStart().clone();
        Attribute.Calendar eff_ed = this.effTimeInterval.getEnd().clone();
        int eff_tres = this.effTimeInterval.getTimeUnit();
        long sdEff = eff_sd.getTimeInMillis();

        //check if effTimeInterval is in the bounds of the model time interval
        //otherwise it will be set to the model interval bounds
        if (eff_sd.before(model_sd)) {
            this.effTimeInterval.setStart(model_sd);
            getModel().getRuntime().println("effStartdate was set equal to model startdate", JAMS.STANDARD);
        }
        if (model_ed.before(eff_ed)) {
            this.effTimeInterval.setEnd(model_ed);
            getModel().getRuntime().println("effEnddate was set equal to model enddate", JAMS.STANDARD);
        }
        if (eff_ed.before(model_sd)) {
            this.effTimeInterval.setEnd(model_ed);
            getModel().getRuntime().println("effEnddate was set equal to model enddate", JAMS.STANDARD);
        }
        /*if(eff_tres == eff_sd.DAY_OF_YEAR){
         this.effTsteps = (int)((edEff - sdEff) / (1000 * 60 * 60 * 24));
         this.effTsteps = this.effTsteps + 1;
         }*/
        effTsteps = (int) effTimeInterval.getNumberOfTimesteps();

        getModel().getRuntime().println("effStartdate:\t" + eff_sd.toString(), JAMS.VERBOSE);
        getModel().getRuntime().println("effEnddate:\t" + eff_ed.toString(), JAMS.VERBOSE);

        valData = new double[(int) model_tsteps];
        preData = new double[(int) model_tsteps];

        counter = 0;

        //determine start and end array index for timeInterval
        if (eff_tres == GregorianCalendar.DAY_OF_YEAR) {
            this.interValStart = (int) ((sdEff - sdMod) / (1000 * 60 * 60 * 24));
            this.interValEnd = this.interValStart + this.effTsteps;
        } else if (eff_tres == GregorianCalendar.HOUR_OF_DAY) {
            this.interValStart = (int) ((sdEff - sdMod) / (1000 * 60 * 60));
            this.interValEnd = this.interValStart + this.effTsteps;
        } else if (eff_tres == GregorianCalendar.MONTH) {
            Attribute.Calendar modStart = modelTimeInterval.getStart().clone();
            Attribute.Calendar effStart = effTimeInterval.getStart().clone();
            int startStep = 0;
            while (modStart.before(effStart)) {
                startStep++;
                modStart.add(GregorianCalendar.MONTH, 1);
            }
            this.interValStart = startStep;
            this.interValEnd = this.interValStart + this.effTsteps;
        } else if (eff_tres == GregorianCalendar.YEAR) {
            Attribute.Calendar modStart = modelTimeInterval.getStart().clone();
            Attribute.Calendar effStart = effTimeInterval.getStart().clone();
            int startStep = 0;
            while (modStart.before(effStart)) {
                startStep++;
                modStart.add(GregorianCalendar.YEAR, 1);
            }
            this.interValStart = startStep;
            this.interValEnd = this.interValStart + this.effTsteps;
        }

        if (this.effMonthList != null) {
            this.monthly = true;
        }
    }

    public void run() {
        if (monthly) {
            int month = time.get(GregorianCalendar.MONTH) + 1;
            for (int i = 0; i < this.effMonthList.getValue().length; i++) {
                if (month == this.effMonthList.getValue()[i]) {
                    this.valData[counter] = validation.getValue();
                    this.preData[counter] = prediction.getValue();
                    this.counter++;
                    this.monthCount++;
                }
            }
        } else {
            this.valData[counter] = validation.getValue();
            this.preData[counter] = prediction.getValue();
            this.counter++;
        }
    }

    public void cleanup() {
        getModel().getRuntime().println("", JAMS.STANDARD);
        getModel().getRuntime().println("********************************************************************", JAMS.STANDARD);
        getModel().getRuntime().println("Instance:                " + this.getInstanceName(), JAMS.STANDARD);
        getModel().getRuntime().println("Efficiencies for period: " + this.effTimeInterval.toString(), JAMS.STANDARD);
        getModel().getRuntime().println("********************************************************************", JAMS.STANDARD);

        Vector<Double> valVector = new Vector<Double>();
        Vector<Double> preVector = new Vector<Double>();

        this.predictionValues.setValue(preData);

        for (int i = this.interValStart; i < this.interValEnd; i++) {
            //consider valid values only
            if (valData[i] != JAMS.getMissingDataValue() && preData[i] != JAMS.getMissingDataValue()) {
                valVector.add(valData[i]);
                preVector.add(preData[i]);
            }
        }

        int dataCount = valVector.size();
        if (monthly) {
            dataCount = this.monthCount;
        }
        double[] valData_1 = new double[dataCount];
        double[] preData_1 = new double[dataCount];

        //converting Vectors to arrays
        for (int i = 0; i < dataCount; i++) {
            valData_1[i] = valVector.get(i).doubleValue();
            preData_1[i] = preVector.get(i).doubleValue();
        }

        for (int i = 0; i < effMethod.getValue().length; i++) {
            if (effMethod.getValue()[i] == this.E1) {
                double e1 = NashSutcliffe.efficiency(preData_1, valData_1, 1);
                this.e1.setValue(e1);
                getModel().getRuntime().println(String.format(Locale.US, "e1:      %.5f", e1), JAMS.STANDARD);
            } else if (effMethod.getValue()[i] == this.E2) {
                double e2 = NashSutcliffe.efficiency(preData_1, valData_1, 2);
                this.e2.setValue(e2);
                getModel().getRuntime().println(String.format(Locale.US, "e2:      %.5f", e2), JAMS.STANDARD);
            } else if (effMethod.getValue()[i] == this.LOG_E1) {
                double le1 = NashSutcliffe.logEfficiency(preData_1, valData_1, 1);
                this.le1.setValue(le1);
                getModel().getRuntime().println(String.format(Locale.US, "log_e1:  %.5f", le1), JAMS.STANDARD);
            } else if (effMethod.getValue()[i] == this.LOG_E2) {
                double le2 = NashSutcliffe.logEfficiency(preData_1, valData_1, 2);
                this.le2.setValue(le2);
                getModel().getRuntime().println(String.format(Locale.US, "log_e2:  %.5f", le2), JAMS.STANDARD);
            } else if (effMethod.getValue()[i] == this.IOA_1) {
                double ioa1 = IndexOfAgreement.calc_IOA(preData_1, valData_1, 1, getModel());
                this.ioa1.setValue(ioa1);
                getModel().getRuntime().println(String.format(Locale.US, "ioa1:    %.5f", ioa1), JAMS.STANDARD);
            } else if (effMethod.getValue()[i] == this.IOA_2) {
                double ioa2 = IndexOfAgreement.calc_IOA(preData_1, valData_1, 2, getModel());
                this.ioa2.setValue(ioa2);
                getModel().getRuntime().println(String.format(Locale.US, "ioa2:    %.5f", ioa2), JAMS.STANDARD);
            } else if (effMethod.getValue()[i] == this.R2) {
                double[] rCoeff = Regression.calcLinReg(valData_1, preData_1);
                getModel().getRuntime().println(String.format(Locale.US, "rsq:     %.5f", rCoeff[2]), JAMS.STANDARD);
                getModel().getRuntime().println(String.format(Locale.US, "grad:    %.5f", rCoeff[1]), JAMS.STANDARD);
                this.rsq.setValue(rCoeff[2]);
                this.grad.setValue(rCoeff[1]);
            } else if (effMethod.getValue()[i] == this.WR2) {
                double[] rCoeff = Regression.calcLinReg(valData_1, preData_1);
                double wr;
                if (rCoeff[1] <= 1) {
                    wr = Math.abs(rCoeff[1]) * rCoeff[2];
                } else {
                    wr = Math.pow(Math.abs(rCoeff[1]), -1.0) * rCoeff[2];
                }
                this.wrsq.setValue(wr);
                getModel().getRuntime().println(String.format(Locale.US, "wrsq:    %.5f", wr), JAMS.STANDARD);
            } else if (effMethod.getValue()[i] == this.DSGRAD) {
                double dsGrad = DoubleSumAnalysis.dsGrad(valData_1, preData_1);
                this.dsGrad.setValue(dsGrad);
                getModel().getRuntime().println(String.format(Locale.US, "dsGrad:  %.5f", dsGrad), JAMS.STANDARD);
            } else if (effMethod.getValue()[i] == this.ABSVOLERROR) {
                double volErr = VolumeError.absVolumeError(valData_1, preData_1);
                this.absVolErr.setValue(volErr);
                getModel().getRuntime().println(String.format(Locale.US, "AVE:     %.5f", volErr), JAMS.STANDARD);
            } else if (effMethod.getValue()[i] == this.RMSE) {
                double rmse = PredictionErrors.rootMeanSquareError(valData_1, preData_1);
                this.rmse.setValue(rmse);
                getModel().getRuntime().println(String.format(Locale.US, "RMSE:    %.5f", rmse), JAMS.STANDARD);
            } else if (effMethod.getValue()[i] == this.PBIAS) {
                double pbias = VolumeError.pbias(valData_1, preData_1);
                this.pbias.setValue(pbias);
                getModel().getRuntime().println(String.format(Locale.US, "PBIAS:   %.5f", pbias), JAMS.STANDARD);
            } else if (effMethod.getValue()[i] == this.APBIAS) {
                double apbias = VolumeError.pbias2(valData_1, preData_1);
                this.apbias.setValue(apbias);
                getModel().getRuntime().println(String.format(Locale.US, "APBIAS:  %.5f", apbias), JAMS.STANDARD);
            }
        }
        getModel().getRuntime().println("********************************************************************", JAMS.STANDARD);
        getModel().getRuntime().println("", JAMS.STANDARD);
    }
}
