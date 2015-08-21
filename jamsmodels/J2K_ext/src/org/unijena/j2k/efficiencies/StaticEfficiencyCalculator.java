/*
 * StaticEfficiencyCalculator.java
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

import java.util.Vector;

import org.unijena.j2k.statistics.Regression;
import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="StaticEfficiencyCalculator",
        author="Peter Krause",
        description="Calculates various efficiency measures for validation and prediction arrays"
        )
        public class StaticEfficiencyCalculator extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Efficiency method used"
            )
            public Attribute.IntegerArray effMethod;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Prediction value array"
            )
            public Attribute.DoubleArray prediction;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Validation value array"
            )
            public Attribute.DoubleArray validation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Nash-Sutcliffe-efficiency with power 1.0"
            )
            public Attribute.Double e1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Nash-Sutcliffe-efficiency with power 2.0"
            )
            public Attribute.Double e2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "logarithmic Nash-Sutcliffe-efficiency with power 1.0"
            )
            public Attribute.Double le1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "logarithmic Nash-Sutcliffe-efficiency with power 2.0"
            )
            public Attribute.Double le2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Willmot's index of agreement with power 1.0"
            )
            public Attribute.Double ioa1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Willmot's index of agreement with power 2.0"
            )
            public Attribute.Double ioa2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "coefficient of determination r²"
            )
            public Attribute.Double rsq;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "gradient of linear regression"
            )
            public Attribute.Double grad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "weighted r²"
            )
            public Attribute.Double wrsq;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "gradient of double sum regression"
            )
            public Attribute.Double dsGrad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "absolute volume error"
            )
            public Attribute.Double absVolErr;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "root mean square error"
            )
            public Attribute.Double rmse;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "percent bias"
            )
            public Attribute.Double pbias;
    
    
    
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
    
    private double[] valData;
    private double[] preData;
    
    
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
    	this.valData = this.validation.getValue();
        this.preData = this.prediction.getValue();
        getModel().getRuntime().println("", JAMS.STANDARD);
        getModel().getRuntime().println("*************************************************************", JAMS.STANDARD);
        getModel().getRuntime().println("Efficiencies ", JAMS.STANDARD);
        getModel().getRuntime().println("Sampler: " + this.getInstanceName(), JAMS.STANDARD);
        getModel().getRuntime().println("*************************************************************", JAMS.STANDARD);
        
        for(int i = 0; i < effMethod.getValue().length; i++){
            if(effMethod.getValue()[i] == this.E1){
                double e1 = NashSutcliffe.efficiency(preData, valData, 1);
                this.e1.setValue(e1);
                getModel().getRuntime().println("e1:\t\t" + e1, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.E2){
                double e2 = NashSutcliffe.efficiency(preData, valData, 2);
                this.e2.setValue(e2);
                getModel().getRuntime().println("e2:\t\t" + e2, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.LOG_E1){
                double le1 = NashSutcliffe.logEfficiency(preData, valData, 1);
                this.le1.setValue(le1);
                getModel().getRuntime().println("log_e1:\t\t" + le1, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.LOG_E2){
                double le2 = NashSutcliffe.logEfficiency(preData, valData, 2);
                this.le2.setValue(le2);
                getModel().getRuntime().println("log_e2:\t\t" + le2, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.IOA_1){
                double ioa1 = IndexOfAgreement.calc_IOA(preData, valData, 1, getModel());
                this.ioa1.setValue(ioa1);
                getModel().getRuntime().println("ioa1:\t\t" + ioa1, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.IOA_2){
                double ioa2 = IndexOfAgreement.calc_IOA(preData, valData, 2, getModel());
                this.ioa2.setValue(ioa2);
                getModel().getRuntime().println("ioa2:\t\t" + ioa2, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.R2){
                double[] rCoeff = Regression.calcLinReg(valData, preData);
                getModel().getRuntime().println("r²:\t\t" + rCoeff[2], JAMS.STANDARD);
                getModel().getRuntime().println("grad:\t\t" + rCoeff[1], JAMS.STANDARD);
                this.rsq.setValue(rCoeff[2]);
                this.grad.setValue(rCoeff[1]);
            }else if(effMethod.getValue()[i] == this.WR2){
                double[] rCoeff = Regression.calcLinReg(valData, preData);
                double wr;
                if(rCoeff[1] <= 1)
                    wr = Math.abs(rCoeff[1]) * rCoeff[2];
                else
                    wr = Math.pow(Math.abs(rCoeff[1]), -1.0) * rCoeff[2];
                this.wrsq.setValue(wr);
                getModel().getRuntime().println("wr²:\t\t" + wr, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.DSGRAD){
                double dsGrad = DoubleSumAnalysis.dsGrad(valData, preData);
                this.dsGrad.setValue(dsGrad);
                getModel().getRuntime().println("dsGrad:\t\t" + dsGrad, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.ABSVOLERROR){
                double volErr = VolumeError.absVolumeError(valData, preData);
                this.absVolErr.setValue(volErr);
                getModel().getRuntime().println("absVolumeError:\t\t" + volErr, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.RMSE){
                double rmse = PredictionErrors.rootMeanSquareError(valData, preData);
                this.rmse.setValue(rmse);
                getModel().getRuntime().println("RMSE:\t\t" + rmse, JAMS.STANDARD);
            }else if(effMethod.getValue()[i] == this.PBIAS){
                double pbias = VolumeError.pbias(valData, preData);
                this.pbias.setValue(pbias);
                getModel().getRuntime().println("PBIAS:\t\t" + pbias, JAMS.STANDARD);
            }  
        }
        
    }
    
    public void cleanup() {

        
    }
}
