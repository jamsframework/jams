/*
 * BaseflowIndex.java
 * Created on 11. December 2008, 10:52
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, Peter Krause
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

package org.unijena.j2k.analysis;

import jams.data.*;
import jams.model.*;
import java.util.Vector;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="BaseflowIndex",
        author="Peter Krause",
        description="Estimates the relative part of baseflow expressed as " +
        "baseflow index BFI. The method is based on Wundt, Kille and Demuth and " +
        "expects a time series of measured daily runoff values for a period of" +
        "10 yrs or more."
        )
        public class BaseflowIndex extends JAMSComponent {
    
    /*
     *  Component variables
     */
    //input values
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the current time step"
            )
            public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "daily runoff value",
            unit = "m³/s"
            )
            public Attribute.Double dailyRunoff;

    //calibration parameters
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the X% lowest values can be interpreted as human " +
            "influenced and removed from the analysis. Default value is 5%.",
            defaultValue="0.05"
            )
            public Attribute.Double lowestValues;

    //output values
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the baseflow index expressed as the ration of reduced" +
            "mean monthly low flows and mean monthly runoff"
            )
            public Attribute.Double bfi;
    
   
    /*
     *  Component run stages
     */
    private Vector<Double> monq = new Vector<Double>();
    private Vector<Double> momq = new Vector<Double>();
    private Vector<Double> basq = new Vector<Double>();
    private Vector<Double> rank = new Vector<Double>();
    private int currentMonth = -1;
    private double actMinimum = 9999.9;
    private double actSum = 0;
    private int dayCount = 0;

    public void run(){
        //check month
        int actMonth = time.get(Attribute.Calendar.MONTH);
        if(currentMonth < 0)
            currentMonth = actMonth;

        //next month has started
        if((actMonth != currentMonth)&&(dayCount > 0)){
            momq.add(actSum / dayCount);
            monq.add(actMinimum);
            //System.out.println("actSum:" + (actSum));
            //System.out.println("dayCount:" + (dayCount));
            //System.out.println("momq:" + (actSum / dayCount));
            //System.out.println("monq:" + (actMinimum));
            
            double dRo = dailyRunoff.getValue();
            actSum = dRo;
            actMinimum = dRo;
            dayCount = 1;
            if(currentMonth < 11)
                currentMonth++;
            else
                currentMonth = 0;
        }
        //calculation and aggregation of daily values
        else{
            dayCount++;
            double dRo = dailyRunoff.getValue();
            //sum for computing monthly mean values
            actSum = actSum + dRo;
            //monthly minimum values
            if(dRo < actMinimum)
                actMinimum = dRo;
        }
    }
    
    public void cleanup() {
        //values of last month
        momq.add(actSum / dayCount);
        monq.add(actMinimum);
        
        //bfi calculation
        int size = monq.size();
        double meanSum = 0;
        double[] minimums = new double[monq.size()];
        for (int c = 0; c < size; c++){
            //summing the mean values
            meanSum += ((Double)momq.get(c)).doubleValue();
            //sorting the minimum values
            int count = 0;
            double actMin = ((Double) monq.get(0)).doubleValue();
            //looking for the smallest value in the vector
            for (int i = 0; i < monq.size(); i++) {
                double val = ((Double) monq.get(i)).doubleValue();
                if (val < actMin) {
                    count = i;
                    actMin = val;
                }
            }
            minimums[c] = actMin;

            //remove the minimum value from the vector
            monq.remove(count);
        }
        
        //removing the X% lowest values
        int nLowVal = (int)(size * this.lowestValues.getValue());
        //regression from X% to 50%
        int med = (int)(size * 0.5);
        
        int currCount = 0;
        for(int i = nLowVal; i < med; i++){
            basq.add(minimums[i]);
            rank.add((double)i+1);
            currCount = i;
        }
        double bq[] = v2da(basq);
        double rv[] = v2da(rank);
        //regression
        double[] regCoeff = org.unijena.j2k.statistics.Regression.calcLinReg(rv,bq);
        double rsq = regCoeff[2];
        
        //now we add elements and check wether rsq increases or decreases
        //the point where rsq starts to decrease is considered as the critical
        //point from which on low flow values are expected to be not only base 
        //flow but do contain interflow
        boolean cont = true;
        double gradient = 0;
        double intercept = 0;
        while (cont && (currCount < momq.size()-1)) {
            currCount++;
            basq.add(minimums[currCount]);
            rank.add((double)currCount + 1);
            bq = v2da(basq);
            rv = v2da(rank);
            //regression
            regCoeff = org.unijena.j2k.statistics.Regression.calcLinReg(rv, bq);
            if (regCoeff[2] >= rsq) {
                cont = true;
                rsq = regCoeff[2];
            } else {
                cont = false;
                //we have to remove the last added values
                basq.remove(basq.size() - 1);
                rank.remove(rank.size() - 1);
                bq = v2da(basq);
                rv = v2da(rank);
                //the regression parameters
                regCoeff = org.unijena.j2k.statistics.Regression.calcLinReg(rv, bq);
                intercept = regCoeff[0];
                gradient = regCoeff[1];
                rsq = regCoeff[2];
                
            }
        }
        //from the critcal point on we fill the baseflow array with values 
        //calculated from the linear regression estimated above
        int startIdx = basq.size() - 1;
        double currRank = ((Double)(rank.get(startIdx))).doubleValue();
        int idx = (int)currRank+1;
        for(int i = idx; i <= momq.size(); i++){
            currRank++;
            double val = intercept + gradient * currRank;
            basq.add(val);
            rank.add(currRank);
        }
        
        //now we can calculate the baseflow index bfi
        bq = v2da(basq);
        double bq_sum = 0;
        for(int i = 0; i < bq.length; i++)
            bq_sum = bq_sum + bq[i];
        
        double longTermBflo = bq_sum / bq.length;
        double longTermMean = meanSum / size;
        
        bfi.setValue(longTermBflo/longTermMean);
        System.out.println("BFI: " + bfi.getValue());
    }
    
    private double[] v2da(Vector inVec){
        double[] outArr = new double[inVec.size()];
        for(int i = 0; i < inVec.size(); i++)
            outArr[i] = ((Double)(inVec.get(i))).doubleValue();
        return outArr;
    }
}
