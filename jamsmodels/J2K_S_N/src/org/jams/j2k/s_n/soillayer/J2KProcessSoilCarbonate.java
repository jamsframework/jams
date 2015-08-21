/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jams.j2k.s_n.soillayer;

import jams.data.Attribute;
import jams.data.Attribute.Boolean;
import jams.data.Attribute.Calendar;
import jams.data.DefaultDataFactory;
import jams.data.Attribute.Double;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;

/**
 *
 * @author Christian Fischer
 */
@JAMSComponentDescription(title = "J2KProcessLayerdSoilWater",
author = "Christian Fischer",
description = "Calculates soil carbonate transformation")
public class J2KProcessSoilCarbonate extends JAMSComponent {
    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "time")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "attribute area")
    public Attribute.Double area;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "kg / ha")
    public Attribute.Double carbonIn;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "layer count of soil")
    public Attribute.Double nLayer;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "depth of soil layer in cm")
    public Attribute.DoubleArray depth;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "fast AOM1",
    unit = "kg*m^-3")
    public Attribute.DoubleArray addedOrganicMatterConcentrationFast;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "fast AOM2",
    unit = "kg*m^-3")
    public Attribute.DoubleArray addedOrganicMatterConcentrationSlow;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "fast BOM",
    unit = "kg*m^-3")
    public Attribute.DoubleArray microbialBiomassConcentrationFast;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "slow BOM",
    unit = "kg*m^-3")
    public Attribute.DoubleArray microbialBiomassConcentrationSlow;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "fast SOM",
    unit = "kg*m^-3")
    public Attribute.DoubleArray soilOrganicMatterConcentrationFast;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "slow SOM",
    unit = "kg*m^-3")
    public Attribute.DoubleArray soilOrganicMatterConcentrationSlow;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "clay content",
    unit = "kg*kg^-1")
    public Attribute.DoubleArray clayContent;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "temperature of soillayer",
    unit = "°C")
    public Attribute.DoubleArray soilTemperature;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "potential pressure",
    unit = "-cm H2o")
    public Attribute.DoubleArray potentialPressure;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "co2 output")
    public Attribute.Double co2Out;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "2.7E-6")
    public Attribute.Double kSOM1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "1.4E-4")
    public Attribute.Double kSOM2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "1.2E-2")
    public Attribute.Double kAOM1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "5.0E-2")
    public Attribute.Double kAOM2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "1.85E-4")
    public Attribute.Double kDeathRate1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "1.0E-2")
    public Attribute.Double kDeathRate2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "1.8E-3")
    public Attribute.Double mBOM1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "1.0E-2")
    public Attribute.Double mBOM2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "0.6")
    public Attribute.Double EBOM1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "0.6")
    public Attribute.Double EBOM2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "0.00165")
    public Attribute.Double BCS;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "0.00165")
    public Attribute.Double BCF;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "24.75")
    public Attribute.Double BMCS;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "8.25")
    public Attribute.Double BMCF;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "0.8")
    public Attribute.Double fAOM1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "0.2")
    public Attribute.Double fAOM2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "0.5")
    public Attribute.Double fBOM1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "0.1")
    public Attribute.Double fSOM1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "co2 output",
    defaultValue = "0.4")
    public Attribute.Double fSOM2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "co2 output")
    public Attribute.Boolean isInit;

    private double clayContentFunction(double Xc){
        final double XcStar = 0.25; //limit for the effect of clay content in kg/kg
        final double a = 0.02;

        if (Xc < XcStar)
            return 1.0 - a * Xc;
        else
            return 1.0 - a * XcStar;
    }

    private double temperatureFunction(double T){
        if (T < 0)
            return 0;
        else if (T < 20)
            return 0.1 * T;
        else
            return Math.exp(0.47 - 0.027*T + 0.00193*T*T);
    }
//phi in meterwassersäule
    private double pressurePotentialFunction(double phi){
        double logPhi = Math.log10(-phi);
        if (logPhi > 4.5){
            return 0;
        }else if (logPhi > 0.5 ){
            //return 1.0 - Math.log10(phi/Math.sqrt(10.0)) / 4.0;
            return (logPhi+4.5)/4.0;
        }else if (logPhi > -0.5){
            return 1.0;
        }else if (logPhi > -2.0){
            return 0.6 - 0.4*(logPhi-0.5)/1.5; //0.4*Math.log10(-100*phi)/1.5;
        }else{
            return 0.6;
        }
    }

    double deathRate1[] = null;
    double deathRate2[] = null;
    double kBOM1[] = null;
    double kBOM2[] = null;
    double ccf[] = null;
    double tf[] = null;
    double ppf[] = null;


    @Override
    public void init(){
        isInit.setValue(false);
    }

    
    public void init2ndState(){
        int n = (int)nLayer.getValue();
        
        double BCS[] = new double[n];
        double BCF[] = new double[n];
        double BMCS[] = new double[n];
        double BMCF[] = new double[n];
        for (int i=0;i<n;i++){
            BCS[i] = this.BCS.getValue();
            BCF[i] = this.BCF.getValue();
            BMCS[i] = this.BMCS.getValue();
            BMCF[i] = this.BMCF.getValue();
        }
        //abhängig von landnutzung!!
        microbialBiomassConcentrationSlow.setValue(BCS);
        microbialBiomassConcentrationFast.setValue(BCF);
        soilOrganicMatterConcentrationSlow.setValue(BMCS);
        soilOrganicMatterConcentrationFast.setValue(BMCF);

        deathRate1 = new double[n];
        deathRate2 = new double[n];
        kBOM1      = new double[n];
        kBOM2      = new double[n];
        ccf = new double[n];
        tf = new double[n];
        ppf = new double[n];
    }

    @Override
    public void run(){
        if (!isInit.getValue()){
            init2ndState();
            isInit.setValue(true);
        }

        int n= (int)this.nLayer.getValue();
        //input from other modules
        double Xc[] = clayContent.getValue(),
               T[] = soilTemperature.getValue(),
               phi[] = potentialPressure.getValue();

        if (Xc == null || Xc.length != n){
            Xc = new double[n];
        }
        if (T == null || T.length != n){
            T = new double[n];
        }
        if (phi == null || phi.length != n){
            phi = new double[n];
        }
        double cIn = this.carbonIn.getValue() / 10000.0; //kg d^-1
        //storage states
        double AOM1[] = this.addedOrganicMatterConcentrationSlow.getValue();
        if (AOM1 == null || AOM1.length != n){
            AOM1 = new double[n];
        }
        double AOM2[] = this.addedOrganicMatterConcentrationFast.getValue();
        if (AOM2 == null || AOM2.length != n){
            AOM2 = new double[n];
        }
        double BOM1[] = this.microbialBiomassConcentrationSlow.getValue();
        if (BOM1 == null || BOM1.length != n){
            BOM1 = new double[n];
        }
        double BOM2[] = this.microbialBiomassConcentrationFast.getValue();
        if (BOM2 == null || BOM2.length != n){
            BOM2 = new double[n];
        }
        double SOM1[] = this.soilOrganicMatterConcentrationSlow.getValue();
        if (SOM1 == null || SOM1.length != n){
            SOM1 = new double[n];
        }
        double SOM2[] = this.soilOrganicMatterConcentrationFast.getValue();
        if (SOM2 == null || SOM2.length != n){
            SOM2 = new double[n];
        }

        //fixed parameters attention these are values for a daily modell
        double kSOM1 = this.kSOM1.getValue(), // d^-1
                     kSOM2 = this.kSOM2.getValue(), // d^-1
                     kAOM1 = this.kAOM1.getValue(), // d^-1
                     kAOM2 = this.kAOM2.getValue(), // d^-1
                     kDeathRate1 = this.kDeathRate1.getValue(), // d^-1
                     kDeathRate2 = this.kDeathRate2.getValue(),  // d^-1
                     mBOM1 = this.mBOM1.getValue(),  //kg * m^-3 d^-1
                     mBOM2 = this.mBOM2.getValue(),  //kg * m^-3 d^-1
                     EBOM1 = this.EBOM1.getValue(),  //factor
                     EBOM2 = this.EBOM2.getValue();

        //parameter abhängig von landnutzung
        //straw: 0.45 0.55
        //plant residues 0.4 0.6
        //pig slurry 0.9 0.0
        double fAOM1=this.fAOM1.getValue(), fAOM2=this.fAOM2.getValue(); //factor

        double fBOM1 = this.fBOM1.getValue(); //factor
        double fSOM1 = this.fSOM1.getValue(), fSOM2 = this.fSOM2.getValue(); //factor

        double thickness[] = new double[n];
        thickness[0] = depth.getValue()[0];

        for (int i = 1; i < n; i++) {
            thickness[i] = depth.getValue()[i] - depth.getValue()[i - 1];
        }

        cIn = cIn / (thickness[0]/100.0);

        for (int i=0;i<n;i++){
            deathRate1[i] = kDeathRate1 * BOM1[i]; //kg m^-3 d^-1
            deathRate2[i] = kDeathRate2 * BOM2[i]; //kg m^-3 d^-1
            
            //calclation
            kBOM1[i] = deathRate1[i] + mBOM1; //kg m^-3 d^-1
            kBOM2[i] = deathRate2[i] + mBOM2; //kg m^-3 d^-1
            
            //distribute carbonate
            ccf[i] = clayContentFunction(Xc[i]); //factor
            tf[i]  = temperatureFunction(T[i]); //factor
            ppf[i] = pressurePotentialFunction(phi[i]/100.0); //factor
            
            double zetaAOM1 = AOM1[i]*kAOM1*tf[i]*ppf[i]; //kg m^3 d^-1
            double zetaAOM2 = AOM2[i]*kAOM2*tf[i]*ppf[i]; //kg m^3 d^-1
            double zetaBOM1 = BOM1[i]*kBOM1[i]*ccf[i]*tf[i]*ppf[i]; //kg m^3 d^-1
            double zetaBOM2 = BOM2[i]*kBOM2[i]*tf[i]*ppf[i]; //kg m^3 d^-1
            double zetaSOM1 = SOM1[i]*kSOM1*ccf[i]*tf[i]*ppf[i]; //kg m^3 d^-1
            double zetaSOM2 = SOM2[i]*kSOM2*ccf[i]*tf[i]*ppf[i]; //kg m^3 d^-1
            
            double dAOM1 = - zetaAOM1;
            double dAOM2 = - zetaAOM2; //kg m-3 d^-1
            if (i==0){
                dAOM1 += cIn * fAOM1; //kg m-3 d^-1
                dAOM2 += cIn * fAOM2;
            }
            
            double dBOM1 = EBOM1*(zetaSOM1+(1-fSOM1)*zetaSOM2             +fBOM1 *zetaAOM1)          -zetaBOM1;
            double dBOM2 = EBOM2*((1.0-fSOM2)*(deathRate1[i]+deathRate2[i])+(1.0-fBOM1)*zetaAOM1+zetaAOM2) -zetaBOM2;
        
            double dSOM1 = fSOM1*zetaSOM2 - zetaSOM1; //kg m-3 d^-1  wo ist (1-fAOM1-fAOM2)cIn ???
            double dSOM2 = fSOM2*(deathRate1[i]+deathRate2[i])-zetaSOM2; //kg m^-3 d^-1
            
            AOM1[i] = AOM1[i]+dAOM1;
            AOM2[i] = AOM2[i]+dAOM2;
            BOM1[i] = BOM1[i]+dBOM1;
            BOM2[i] = BOM2[i]+dBOM2;
            SOM1[i] = SOM1[i]+dSOM1;
            SOM2[i] = SOM2[i]+dSOM2;
        }
        
        this.addedOrganicMatterConcentrationSlow.setValue(AOM1);
        this.addedOrganicMatterConcentrationFast.setValue(AOM2);
        this.microbialBiomassConcentrationSlow.setValue(BOM1);
        this.microbialBiomassConcentrationFast.setValue(BOM2);
        this.soilOrganicMatterConcentrationSlow.setValue(SOM1);
        this.soilOrganicMatterConcentrationFast.setValue(SOM2);

    }
}
