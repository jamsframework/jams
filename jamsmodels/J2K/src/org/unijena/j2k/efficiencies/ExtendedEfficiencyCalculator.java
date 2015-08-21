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

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author Peter Krause
 */
@SuppressWarnings("unchecked")
@JAMSComponentDescription(
title="ExtendedEfficiencyCalculator",
        author="Peter Krause",
        description="Calculates various efficiency measures"
        )
        public class ExtendedEfficiencyCalculator extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "time"
            )
            public Attribute.Calendar time;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "The model time interval"
            )
            public Attribute.TimeInterval modelTimeInterval;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "The efficiency time interval"
            )
            public Attribute.TimeInterval effTimeInterval;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "The months to be evaluated interval"
            )
            public Attribute.IntegerArray effMonthList;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Efficiency method used"
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
            description = "custom efficiency measure",
            defaultValue= "0"
            )
            public Attribute.Double effHydro1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "custom efficiency measure",
            defaultValue= "0"
            )
            public Attribute.Double effHydro2;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "fourier transform based efficiency measure",
            defaultValue= "0"
            )
            public Attribute.Double effFourier;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "cosine metric",
            defaultValue= "0"
            )
            public Attribute.Double effCosine;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "similB measure, based on cross power operator",
            defaultValue= "0"
            )
            public Attribute.Double similB;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "likelihood ration based distance measure",
            defaultValue= "0"
            )
            public Attribute.Double LHReff;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "likelihood ration based distance measure",
            defaultValue= "0"
            )
            public Attribute.Double effLowpassFourier;
            
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "full set of predicted values"
            )
            public Attribute.DoubleArray predictionValues;
    
    private final int FOURIER = 13;
    private final int COSINE =  14;
    private final int SIMILB =  15;
    private final int LHR_FOURIER = 16;
    private final int LOWPASS_FOURIER = 17;
    private final int HYDRO_EFF1 = 18;
    private final int HYDRO_EFF2 = 19;
              
    private double[] valData;
    private double[] preData;
    
    private int counter = 0;
    
    private int interValStart = 0;
    private int interValEnd = 0;
    
    private int effTsteps = 0;
    
    private boolean monthly = false;
    private int monthCount = 0;
    
    private int eff_tres;
    private long sdMod,sdEff;
    /*
     *  Component run stages
     */
    
    public void init() {
        
        //some checking if time intervals correlate well
        //....
        //....                
        Attribute.Calendar model_sd = this.modelTimeInterval.getStart().clone();
        Attribute.Calendar model_ed = this.modelTimeInterval.getEnd().clone();
        
        sdMod = model_sd.getTimeInMillis();
        
        long model_tsteps = 0;        
        model_tsteps = modelTimeInterval.getNumberOfTimesteps();
        
        Attribute.Calendar eff_sd = this.effTimeInterval.getStart().clone();
        Attribute.Calendar eff_ed = this.effTimeInterval.getEnd().clone();
        eff_tres = this.effTimeInterval.getTimeUnit();
        sdEff = eff_sd.getTimeInMillis();
                
        //check if effTimeInterval is in the bounds of the model time interval
        //otherwise it will be set to the model interval bounds
        if(eff_sd.before(model_sd)){
            this.effTimeInterval.setStart(model_sd);
            getModel().getRuntime().println("effStartdate was set equal to model startdate", JAMS.STANDARD);
        }
        if(model_ed.before(eff_ed)){
            this.effTimeInterval.setEnd(model_ed);
            getModel().getRuntime().println("effEnddate was set equal to model enddate", JAMS.STANDARD);
        }
        
        
        effTsteps = (int) effTimeInterval.getNumberOfTimesteps();
                        
        getModel().getRuntime().println("effStartdate:\t" + eff_sd.toString(), JAMS.VERBOSE);
        getModel().getRuntime().println("effEnddate:\t" + eff_ed.toString(), JAMS.VERBOSE);
                
        valData = new double[(int)model_tsteps];
        preData = new double[(int)model_tsteps];   
        
        this.counter = 0;
        this.monthCount = 0;
        
        //determine start and end array index for timeInterval        
        if(eff_tres == Attribute.Calendar.DAY_OF_YEAR){
            this.interValStart =(int)((sdEff - sdMod) / (1000 * 60 * 60 * 24));
            this.interValEnd = this.interValStart + this.effTsteps;
        } else if(eff_tres == Attribute.Calendar.HOUR_OF_DAY){
            this.interValStart =(int)((sdEff - sdMod) / (1000 * 60 * 60));
            this.interValEnd = this.interValStart + this.effTsteps;
        } else if(eff_tres == Attribute.Calendar.MONTH){
            Attribute.Calendar modStart = modelTimeInterval.getStart().clone();
            Attribute.Calendar effStart = effTimeInterval.getStart().clone();
            int startStep = 0;
            while(modStart.before(effStart)){
                startStep++;
                modStart.add(Attribute.Calendar.MONTH,1);
            }
            this.interValStart = startStep;
            this.interValEnd = this.interValStart + this.effTsteps;
        } else if(eff_tres == Attribute.Calendar.YEAR){
            Attribute.Calendar modStart = modelTimeInterval.getStart().clone();
            Attribute.Calendar effStart = effTimeInterval.getStart().clone();
            int startStep = 0;
            while(modStart.before(effStart)){
                startStep++;
                modStart.add(Attribute.Calendar.YEAR,1);
            }
            this.interValStart = startStep;
            this.interValEnd = this.interValStart + this.effTsteps;
        }
        
        if(this.effMonthList != null){
            this.monthly = true;
        }
    }
                    
    public void run() {
        if(monthly){
            int month = time.get(Attribute.Calendar.MONTH) + 1;
            for(int i = 0; i < this.effMonthList.getValue().length; i++){
                if(month == this.effMonthList.getValue()[i]){
                    this.valData[counter] = validation.getValue();
                    this.preData[counter] = prediction.getValue();
                    this.counter++;
                    this.monthCount++;
                }
            }
        } else{
            this.valData[counter] = validation.getValue();
            this.preData[counter] = prediction.getValue();
            this.counter++;
        }
    }
    
    public void cleanup() {
        getModel().getRuntime().println("", JAMS.STANDARD);
        getModel().getRuntime().println("*************************************************************", JAMS.STANDARD);
        getModel().getRuntime().println("Efficiencies for period:\t " + this.effTimeInterval.toString(), JAMS.STANDARD);
        getModel().getRuntime().println("Sampler: " + this.getInstanceName(), JAMS.STANDARD);
        getModel().getRuntime().println("*************************************************************", JAMS.STANDARD);
        
        Vector<Double> valVector = new Vector<Double>();
        Vector<Double> preVector = new Vector<Double>();

        this.predictionValues = getModel().getRuntime().getDataFactory().createDoubleArray();
        this.predictionValues.setValue(preData);
        
        for(int i = this.interValStart; i < this.interValEnd; i++){
            //consider valid values only
            if(valData[i] != JAMS.getMissingDataValue() && preData[i] != JAMS.getMissingDataValue()){
                valVector.add(valData[i]);
                preVector.add(preData[i]);
            }
        }
        
        int dataCount = valVector.size();
        if(monthly){
            dataCount = this.monthCount;
        }
        double[] valData_1 = new double[dataCount];
        double[] preData_1 = new double[dataCount];
        
        //converting Vectors to arrays
        for(int i = 0; i < dataCount; i++){
            valData_1[i] = valVector.get(i).doubleValue();
            preData_1[i] = preVector.get(i).doubleValue();
        }
        
        for(int i = 0; i < effMethod.getValue().length; i++){
            if(effMethod.getValue()[i] == this.FOURIER){
                double eff = //NashSutcliffe.efficiency(preData_1, valData_1, 1);
                        complexEfficiency(fourierTransform(realToComplexArray(preData_1)),fourierTransform(realToComplexArray(valData_1)),1.0);
                this.effFourier.setValue(eff);
                getModel().getRuntime().println("fourierEfficiency:\t\t" + String.format(Locale.US,"%.5f",eff), JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.COSINE){
                double eff = cosineMetric(preData_1,valData_1);
                this.effCosine.setValue(eff);
                getModel().getRuntime().println("cosineMetric:\t\t" + String.format(Locale.US,"%.5f",eff), JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.SIMILB){
                double eff = similB(preData_1,valData_1);
                this.similB.setValue(eff);
                getModel().getRuntime().println("similB:\t\t" + String.format(Locale.US,"%.5f",eff), JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.LHR_FOURIER){
                double eff = LikelihoodRationFourierEfficiency(preData_1,valData_1);
                this.LHReff.setValue(eff);
                getModel().getRuntime().println("Likelihood Ration Fourier Efficiency:\t\t" + String.format(Locale.US,"%.5f",eff), JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.LOWPASS_FOURIER){
                double eff = //NashSutcliffe.efficiency(preData_1, valData_1, 1);
                        complexEfficiency(fourierTransform(realToComplexArray(preData_1)),fourierTransform(realToComplexArray(valData_1)),0.9);
                this.effLowpassFourier.setValue(eff);
                getModel().getRuntime().println("lowpass fourierEfficiency:\t\t" + String.format(Locale.US,"%.5f",eff), JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.HYDRO_EFF1){
                double w1 = 1.0, w2 = 1.0, w3 = 1.0;
                double spikeFlowEff = getSpikeFlowEff(preData_1,valData_1);
                double baseFlowEff  = getBaseFlowEff(preData_1,valData_1);
                double dynamicEff = getDynamicEff(preData_1,valData_1);
                this.effHydro1.setValue( 1.0-((w1*spikeFlowEff + w2*baseFlowEff + w3*dynamicEff) / (w1+w2+w3)) );
                getModel().getRuntime().println("effHydro1:\t\t" + String.format(Locale.US,"%.5f",effHydro1.getValue()), JAMS.STANDARD);
                getModel().getRuntime().println("spike:\t\t" + String.format(Locale.US,"%.5f",spikeFlowEff), JAMS.STANDARD);
                getModel().getRuntime().println("baseFlowEff:\t\t" + String.format(Locale.US,"%.5f",baseFlowEff), JAMS.STANDARD);
                getModel().getRuntime().println("dynamicEff:\t\t" + String.format(Locale.US,"%.5f",dynamicEff), JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.HYDRO_EFF2){
                double w1 = 1.0, w2 = 1.0, w3 = 1.0;
                double spikeFlowEff = getSpikeFlowEff2(preData_1,valData_1);
                double baseFlowEff  = getBaseFlowEff(preData_1,valData_1);
                double totEff = totalEff(preData_1,valData_1);
                this.effHydro2.setValue( 1.0-((w1*spikeFlowEff + w2*baseFlowEff + w3*totEff) / (w1+w2+w3)) );
                getModel().getRuntime().println("effHydro2:\t\t" + String.format(Locale.US,"%.5f",effHydro2.getValue()), JAMS.STANDARD);
                getModel().getRuntime().println("spike:\t\t" + String.format(Locale.US,"%.5f",spikeFlowEff), JAMS.STANDARD);
                getModel().getRuntime().println("baseFlowEff:\t\t" + String.format(Locale.US,"%.5f",baseFlowEff), JAMS.STANDARD);
                getModel().getRuntime().println("dynamicEff:\t\t" + String.format(Locale.US,"%.5f",totEff), JAMS.STANDARD);
            }
            
        }
    }
    
    public class ArrayComparator implements Comparator {

        private int order = 1;

        public ArrayComparator(boolean decreasing_order) {
            order = decreasing_order ? -1 : 1;
        }

        public int compare(Object d1, Object d2) {
            if (((double[]) d1)[0] < ((double[]) d2)[0]) {
                return -1 * order;
            } else if (((double[]) d1)[0] == ((double[]) d2)[0]) {
                return 0 * order;
            } else {
                return 1 * order;
            }
        }
    }
    
    boolean isSpike(int index, double sim[],double obs[]){
        int n = obs.length;
        if (index < n-1){
            if (obs[index] < obs[index+1])
                return false;
        }
                
        if (index > 0){
            if (obs[index] < obs[index-1])
                return false;
        }
        return true;
    }


    double getSpikeFlowEff(double sim[],double obs[]){
        int n = sim.length;
        double combinedData[][] = new double[n][3];
        
        for (int i=0;i<n;i++){
            combinedData[i][0] = obs[i];
            combinedData[i][1] = sim[i];
            combinedData[i][2] = i;
        }
        
        Arrays.sort(combinedData, new ArrayComparator(true));
        
        int spikes = 10;
        if (n < spikes)
            spikes = n;
        
        int counter = 0;
        int index = 0;
        
        double sum = 0.0, norm = 0.0;
        
        while (counter < spikes){
            if (isSpike((int)combinedData[index][2],sim,obs)){
                sum += Math.abs(combinedData[index][0]-combinedData[index][1]);
                norm += Math.abs(combinedData[index][0]);
                counter++;
            }
            index++;
            if (index > n)
                break;
        }
        return sum / norm;
    }
    
    double getSpikeFlowEff2(double sim[],double obs[]){
        int n = sim.length;
        double combinedData[][] = new double[n][3];
        
        for (int i=0;i<n;i++){
            combinedData[i][0] = obs[i];
            combinedData[i][1] = sim[i];
            combinedData[i][2] = i;
        }
        
        Arrays.sort(combinedData, new ArrayComparator(true));
        
        int spikes = 10;
        if (n < spikes)
            spikes = n;
        
        int counter = 0;
        int index = -1;
        
        double sum = 0.0, norm = 0.0;
                
        while (counter < spikes){
            if (++index > n){
                spikes = counter;
                break;
            }
                                    
            int org_index = (int)combinedData[index][2];
                                           
            if (!isSpike(org_index,sim,obs))
                continue;
            
            double  sub_sum1 = Math.abs(1.0 - (sim[org_index] / obs[org_index])),
                    sub_sum2 = Math.abs(1.0 - (sim[org_index] / obs[org_index]));
            
            int falling_index = 0;
                                   
            while(org_index + falling_index < n-1){
                if (obs[org_index+falling_index+1]>obs[org_index+falling_index])
                    break;
                sub_sum1 += Math.abs(1.0 - (sim[org_index+falling_index+1] / obs[org_index+falling_index+1]));
                falling_index++;
            }
            
            int raising_index = 0;
                        
            while(org_index - raising_index > 0){
                if (obs[org_index-raising_index-1]>obs[org_index-raising_index])
                    break;
                sub_sum2 += Math.abs(1.0 - (sim[org_index-raising_index-1] / obs[org_index-raising_index-1]));
                raising_index++;
            }
            
            sum += (sub_sum1 / (double)(falling_index+1)) + (sub_sum2 / (double)(raising_index+1));   
            
            counter++;
        }
        return sum / (2.0*(double)spikes);
    }
    
    double totalEff(double sim[],double obs[]){
        double t=0,norm = 0,eWert=0;
        for (int i=0;i<obs.length;i++){
            eWert += obs[i];
        }
        eWert/=obs.length;
        
        for (int i=0;i<sim.length;i++){
            t += Math.abs(sim[i]-obs[i]);
            norm += Math.abs(obs[i]-eWert);
        }
        return t / norm;
    }
    
    double getBaseFlowEff(double sim[],double obs[]){
        int n = sim.length;
        double combinedData[][] = new double[n][3];
        
        for (int i=0;i<n;i++){
            combinedData[i][0] = obs[i];
            combinedData[i][1] = sim[i];
            combinedData[i][2] = i;
        }
        
        Arrays.sort(combinedData, new ArrayComparator(true));
        
        int start_index = (int)(0.2*n);
        int end_index = (int)(0.5*n);
        
        double sum = 0.0, norm = 0.0;
        
        for (int i=start_index;i<end_index;i++){
            sum += Math.abs(combinedData[i][0]-combinedData[i][1]);
            norm += Math.abs(combinedData[i][0]);
        }
        return sum / norm;
    }
    
    double getDynamicEff(double sim[],double obs[]){
        int n = sim.length;
        
        double sum = 0.0, norm = 0.0;
        
        for (int i=0;i<n-1;i++){
            if ( (sim[i] < sim[i+1] && obs[i] < obs[i+1]) ||
                 (sim[i] > sim[i+1] && obs[i] > obs[i+1]) ){
                 sum += Math.abs(obs[i]-sim[i]);
            }else{
                sum += 2.0*Math.abs(obs[i]-sim[i]);
            }
                 
            norm += obs[i];  
        }
        return sum / (2.0*norm);
    }
    
    class Complex{
        double re;
        double im;
        Complex(double re){
            this.re = re;
            this.im = 0.0;
        }
        Complex(double re,double im){
            this.re = re;
            this.im = im;
        }
        
        Complex plus(Complex b){
            return new Complex(re+b.re,im+b.im);
        }
        
        Complex minus(Complex b){
            return new Complex(re-b.re,im-b.im);
        }
        Complex mul(double b){
            return new Complex(re*b,im*b);
        }
        
        
        double abs(){
            return Math.sqrt(re*re + im*im);
        }
    }
    
    double complexEfficiency(Complex[]obs,Complex[]sim,double p){
        int n = obs.length;
        if (obs.length != sim.length)
            return 0.0;
        double eff = 0;
        for (int i=0;i<n*p;i++){
            eff += obs[i].minus(sim[i]).abs();            
        }
        
        return eff;
    }
    
    Complex[] realToComplexArray(double[] array){
        Complex output[] = new Complex[array.length];
        for (int i=0;i<array.length;i++){
            output[i] = new Complex(array[i]);
        }
        return output;
    }
    
    //perfoms a simple fourier transformation 
    Complex[] fourierTransform(Complex[] input){
        Complex output[] = new Complex[input.length];
        for(int cnt = 0;cnt < input.length;cnt++){
            output[cnt] = new Complex(0.0);
        }
        for(int cnt = 0;cnt < input.length;cnt++){
            correctAndRecombine(input[cnt],cnt,input.length,output);
        }//end for loop    
        return output;
    }
    //basic fourier transform step
    void correctAndRecombine(Complex sample,                           
                           int position,
                           int length,
                           Complex[] out){

        //Calculate the complex transform values for
        // each sample in the complex output series.
        for(int cnt = 0; cnt < length; cnt++){
            double angle =
               (2.0*Math.PI*cnt/length)*position;

          //Calculate output based on real input
            out[cnt].re +=
                    sample.re*Math.cos(angle);
            out[cnt].im +=
                    sample.re*Math.sin(angle);

            //Calculate output based on imag input
            out[cnt].re -=
                    sample.im*Math.sin(angle);
            out[cnt].im +=
                    sample.im*Math.cos(angle);
        }
    }
    
    
    double cosineMetric(double obs[],double sim[]){
        double obs_sum = 0;
        double sim_sum = 0;
        double dotprod = 0;
                
        for (int i=0;i<obs.length;i++){
            obs_sum += obs[i]*obs[i];
            sim_sum += sim[i]*sim[i];        
            dotprod += obs[i]*sim[i];
        }
        dotprod /= Math.sqrt(obs_sum*sim_sum);
        return dotprod;
    }
    
    //cross energy operator phi_B
    double phiB(double obs[],double sim[],int t){
        int n = obs.length;
        if (t == 0 || t== n-1){
            return 0.0;
        }
        return obs[t]*sim[t] - 0.5*(obs[t+1]*sim[t-1]+obs[t-1]*sim[t+1]);
    }
    
    //similB similarity metric based on cross energy operator phi_B
    //proposed by Abdel-Ouahab Boudraa, Jean-Christophe Cexus, Mathieu Groussat and Pierre Brunagel
    //"An Energy-Based SimilarityMeasure for Time Series"
    double similB(double obs[],double sim[]){
        double a = 0;
        double b = 0;
                
        int n = obs.length;
        
        for (int t=0;t<n;t++){
            a += Math.abs(phiB(obs,sim,t));
            b += Math.sqrt(Math.pow(phiB(obs,obs,t),2.0)+Math.pow(phiB(sim,sim,t),2.0));            
        }
        
        double similB = Math.sqrt(2.0)*a / b;
        return similB;
    }
    
    double LikelihoodRationFourierEfficiency(double obs[],double sim[]){
         Complex[] ftObs = fourierTransform(realToComplexArray(obs));
         Complex[] ftSim = fourierTransform(realToComplexArray(sim));
         
         double li = 0;
         for (int i=0;i<ftObs.length;i++){
             double a_i = Math.pow(ftObs[i].abs(),2.0);
             double b_i = Math.pow(ftSim[i].abs(),2.0);
             
             double p = 2.0*Math.log(a_i+b_i)-Math.log(a_i)-Math.log(b_i);
             li += p;
         }         
         return 2.0*li;
    }
}
