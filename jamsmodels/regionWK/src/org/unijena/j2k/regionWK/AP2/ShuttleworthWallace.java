
package org.unijena.j2k.regionWK.AP2;

import java.io.*;
import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
        title="CalcHourlyETP_ShuttleworthWallace",
        author="Corina Manusch",
        description="Calculates potential ETP according Shuttleworth-Wallace"
        )

public class ShuttleworthWallace extends JAMSComponent{
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temporal resolution [d | h | m]"
            )
            public Attribute.String tempRes;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "extinction coefficient of the canopy for incoming radiation"
            )
            public Attribute.Double extCoeff;
     
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable leaf area index LAI"
            )
            public Attribute.Double actLAI;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable canopy height"
            )
            public Attribute.Double absoluteHeight;
    
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "messured wind velocity"
            )
            public Attribute.Double wind;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual temperature"
            )
            public Attribute.Double actT;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "incoming global radiation"
            )
            public Attribute.Double solRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "saturation vapour pressure"
            )
            public Attribute.Double es_T;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual vapour pressure"
            )
            public Attribute.Double ea;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute elevation"
            )
            public Attribute.Double elevation;
     
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable net radiation"
            )
            public Attribute.Double netRad;
     
               
          @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "latent heat of vaporization"
            )
            public Attribute.Double L;
          
          @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "psychrometric constant"
            )
            public Attribute.Double psy;
          
           @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "slope of saturation vapour pressure curve"
            )
            public Attribute.Double sospc;
           
            
             @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "air density at constant pressure"
            )
            public Attribute.Double da;
                     
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "potential transpiration"
            )
            public Attribute.Double potT; 
      
       @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "potential evaporation"
            )
            public Attribute.Double potE; 
       
       @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "potential evapotranspiration"
            )
            public Attribute.Double SWpotET; 
       
           
    /* ***roughness variables*** 
        z0 roughness parameter in m
        d zero-plane displacement in m
        Z canopy source height in m
   */
      static double z0,d,Z;
   
   /* ***resistance variables*** 
    rsa ground to source heigth resistance in s/m
    raa source height to reference height resistance in s/m
    rca leaf to zero plane resistance in s/m
    rcs stomatal resistance in s/m
    rss soil surface resistance in s/m
   */
      static double rsa, raa, rca,rcs,rss;
   
    
   /* ***ETP variables*** 
    lamE latent heat flux from the complete crop in ???
    lamEc latent heat flux from the plant canopy
    lamEs latent heat flux from the soil
   */
   static double lamE,lamEc,lamEs;
   
   /*ETP variables input
    fT temperature factor
    fD vapour pressure deficite factor
    gcs stomatal conductance in m/s
    sospc mean rate of change of saturated vapour pressure with temperature in kPa/K,
    psy psychrometric constant in kPa/K
    tabs absolute temperature in K
   */
   static double fT, fD,gcs, tabs;
   
   /* ***public variables***
    elevation in m
    Ta actual temperature in Â°C
    est saturation vapour pressure in kPa
    ea actual vapour pressure in kPa
    */
    
     double Ta;
     double est;   
     double e_a;   
     double sos_pc;
     double psyC; 
     double glMax;
     double tempFactor;
     double es;
     double ec;
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {        
    }
   
   // public static void main(String[] args)throws IOException {
    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException {     
        
        double LAI        = this.actLAI.getValue();
        double ch          = this.absoluteHeight.getValue();
        double uw         = this.wind.getValue();
        double extC          = this.extCoeff.getValue();
        double globRad          = this.solRad.getValue();
        double netr     = this.netRad.getValue();
        
        if(uw <= 0.01){
            uw = 0.01;
        }
                 
        Ta         = this.actT.getValue();
        est        = this.es_T.getValue();
        e_a         = this.ea.getValue();
        sos_pc     = this.sospc.getValue();
        psyC       = this.psy.getValue();
        
        netr = netr * 1000000/3600; //netRad wird von MJ in W/mÂ² umgerechnet
        
        globRad = globRad * 1000000/3600; //R wird von MJ in W/mÂ² umgerechnet
       
        if(this.tempRes.getValue().equals("d"))
                tempFactor = 86400;
            else if(this.tempRes.getValue().equals("h"))
                tempFactor = 3600;
            else if(this.tempRes.getValue().equals("m"))
                tempFactor = 86400;
        
       roughness(LAI, ch);
   
       resistance(uw,ch,LAI,Ta,extC,globRad,est,e_a);
       
       tabs = Ta + 273.15;
           
        ET_shuttleworth_wallace(netr,LAI, extC);
        
      
       
    }
    
    public void cleanup() {
        
    } 
    /**
     * calculates roughness parameters in m from LAI and vegetation height in m
     * @param LAI the leaf area index
     * @param h the vegetation height in m
     * z0c the roughness length of canopy in m
     * z0s the roughness length of soil in m
     * d the zero-plane displacement in m
     * Z the canopy source height in m
     * cd the drag coefficient in ???
    */  
    public static void roughness(double LAI, double h){
        
        Z = 0.63 * h + 0.13 * h;
        double z0s = 0.01;     
        
        double z0c = 0;         
        
        /*calculates z0c depending on vegetation height h*/
        if(h<=1){
                z0c = 0.13 * h;
         }
         if(h>1 && h<10){
               z0c = 0.0411 * h + 0.0889;
         }
         if(h>=10){
               z0c = 0.05 * h;
          }
        
        /*calculates d and z0 depending on LAI*/
        if(LAI>=4){
            d = h-(z0c/0.3);
            z0 = z0c;
        }
        else{
            
            /*variable cd to calculate d*/
            double cd = Math.pow((-1 + Math.exp(0.909-3.03*(z0c/h))),4)/4;
                             
            d = 1.1*h*Math.log(1+Math.pow(cd*LAI,0.25));
            
            /*variables a,b to calculate z0*/
            double a = 0.3*(h-d);
            double b = z0s+0.3*h*Math.pow(cd * LAI,0.5);
            
            if(a<b){
                z0 = a;
            }
            else{
                z0 = b;
            }
        } 
                     
    } 
    
     /**
     * calculates resistances in s/m 
     * @param LAI the leaf area index
     * @param h the vegetation height in m
     * @param uw the measured wind velocity in m/s
     * @param Ta the actual temperature in Ã?Â°C
     * @param C the extinction coefficient for incoming radiation
     * @param R the incoming global radiation in W/mÃ?Â²
     * @param est the saturation vapour pressure in kPa
     * @param ea the actual vapour pressure in kPa
     *  z0s the roughness length of soil in m
     *  za the reference height in m
     *  zw the measurement height of wind velocity in m
     *  z0w the roughness length of station in m
     *  zb the height of internal boundary layer in m
     *  n a constant (2.5)
     *  k the karman constant 
     *  ab a constant
     *  w the leaf width in m
     *  ptp the ratio of projected leaf area to total leaf surface
     *  ua the wind velocity at reference height
     *  ustar the friction velocity in m/s
     *  Kh the eddy diffusity coefficient at the top of the canopy in mÃ?Â²/s
     *  raa the source to reference height resistance in s/m
     *  rsa the ground to source height resistance in s/m
     *  rca the leaf to zero-plane resistance in s/m
     *  rcs the stomatal resistance in s/m
     *  rss the soil surface resistance in s/m
     *  fT the temperature factor
     *  FD the factor for vapour pressure deficit
     *  gcs the stomatal conductance in m/s
     */  
    public void resistance(double uw,double h, double LAI, double Ta, double C, double R, double est, double ea){
    
    double z0s = 0.01;    
    double za = h + 2;
    double zw = 5;
    double z0w = 0.1;
    double zb = 0.334 * 5000 * 0.875 * Math.pow(z0w, 0.125);
    double k = 0.4;
    
    double ab = 0.01;
    double w = 0.001;
    double ptp = 2;
    
    double n = 0;
    
        
        
        double ua = uw * (Math.log(zb/z0w)* Math.log((za-d)/z0))/(Math.log(zb/z0) * Math.log(zw/z0w));
        
        double ustar = (k*ua)/Math.log((za-d)/z0);
        
               
        double Kh = k*ustar*(h-d);
        
        if(h<=1){
                n = 2.5;
         }
         if(h>1 && h<10){
               n = 2.306+0.194*h;
         }
         if(h>=10){
               n = 4.25;
          }
        
        raa = Math.log((za-d)/(h-d))/(k*ustar) + h/(n*Kh) * (-1 + Math.exp(n*(h-Z)/h));
        
        rsa = (h * Math.exp(n))/ (n* Kh) * (Math.exp((-n*z0s)/h) - Math.exp((-n*Z)/h));
        
         double uh = (ustar/k) * Math.log((h-d)/z0);
        
        rca = (n/ab)*Math.pow(w/uh,0.5) / (ptp * LAI * (Math.exp(-n/2)));
        
        rss = 0;
        
        fT = temp_factor(Ta); 
        fD = vap_press_def(est, ea); 
        
        gcs = canopy_conduct(LAI,fT, fD, C, R);
        
        rcs = 1/gcs;
       
        
        /*System.out.println("za = " + za);
        System.out.println("Kh = " + Kh);
        System.out.println("raa = " + raa);
        System.out.println("rsa = " + rsa);
        System.out.println("rca = " + rca);
        System.out.println("rcs = " + rcs);
        System.out.println("gcs = " + gcs);
        System.out.println("fT = " + fT);
        System.out.println("fD = " + fD);*/
    }
    
    /**
     * calculates evapotranspiration in mm 
     * @param netRad the net radiation in ???
     * @param LAI leaf area index
     * @param C extinction coefficient for radiation
     *  pZ atmospheric pressure in kPa
     *  L latent heat of vaporization in MJ/kg 
     *  elevation elevation in m
     *  tabs absolute temperature in K
     *  Ta actual temperature in Ã?Â°C
     *  sospc mean rate of change of saturated vapour pressure with temperature in MJ/K
     *  psy psychrometric constant in MJ/K
     *  raa aerodynamic resistance between canopy source height and reference height in s/m
     *  rsa aerodynamic resistance between ground and canopy source height in s/m
     *  rss soil surface resistance in s/m
     *  Rc coefficient
     *  Ra coefficient
     *  Rs coefficient
     *  cp specific heat at constant pressure in MJ/kg K
     *  virtTemp virtual temperature in K
     *  da air density at constant pressure in kg/mÃ?Â³
     *  ea actual vapour pressure in kPa
     *  est saturation vapour pressure in kPa
     *  Rns net radiation at soil surface in ???
     *  G soil heat flux in ???
     *  PMc Penman-Monteith ETP from closed canopy in ??? 
     *  PMs Penman-Monteith ETP from bare substrate in ???
     *  Cc coefficient
     *  Cs coefficient
     *  Do vapour pressure deficit within the canopy in kPa
     *  Da vapour pressure deficit at reference height in kPa
     *  lamE latent heat flux from the complete crop in ???
     *  lamEs latent heat flux from the soil surface in ???
     *  lamEc latent heat flux from the plant canopy in ???
     *  ec potential transpiration
     *  es potential evaporation
     * 
    */
    public void ET_shuttleworth_wallace(double netRad, double LAI, double C){
       
        double LH = this.L.getValue();
        LH = LH * 1000000; //L wird  von MJ/kg in J/kg umgerechnet
                
                
        double Ra = (sos_pc+psyC)*raa;
        double Rs = (sos_pc+psyC)*rsa + psyC * rss;
        double Rc = (sos_pc+psyC)*rca + psyC * rcs;
        
        double Cc = 1/(1 + Rc * Ra/(Rs *(Rc + Ra)));
        double Cs = 1/(1 + Rs * Ra/(Rc *(Rs + Ra)));
        
        double cp = 1013;
                      
        double dair = this.da.getValue();
           
       
       /*Strahlungskomponenten*/
       
        //double Rns = netRad * Math.exp(-C*LAI);
       //double G = 0.3 * Rns;
        
        double Rns = 0.2 * netRad;
        double G = 0.5 * Rns;
       
       /*ETP*/
        double Da = est-e_a;
       
       double PMc = (sos_pc*(netRad-G)+(dair*cp*Da-sos_pc*rca*(Rns-G))/(raa+rca))/(sos_pc + psyC*(1+rcs/(raa+rca)));
       double PMs = (sos_pc*(netRad-G)+(dair*cp*Da-sos_pc*rsa*(netRad-Rns))/(raa+rsa))/(sos_pc + psyC*(1+rss/(raa+rsa)));
   
        lamE = Cc * PMc + Cs * PMs; //+ Eint
       
       double Do = Da + (sos_pc*(netRad-G)-(sos_pc+psyC)*lamE)*raa/(dair*cp);
       
       lamEs = (sos_pc*(Rns-G)+dair*cp*Do/rsa)/(sos_pc+psyC*(1+rss/rsa));
       lamEc = (sos_pc*(netRad-Rns)+dair*cp*Do/rca)/(sos_pc+psyC*(1+rcs/rca));
       
       es = lamEs * tempFactor*1/LH;   //*1000000;
       ec = lamEc * tempFactor*1/LH;  //*1000000;
       
       
   /* System.out.println("Ende");
       System.out.println("netRad = " + netRad);
       System.out.println("Rns = " + Rns);
       System.out.println("G = " + G);
       System.out.println("dair = " + dair);
        System.out.println("cp = " + cp);
       System.out.println("Do = " + Do);
       System.out.println("Da = " + Da);
       System.out.println("rsa = " + raa);
       System.out.println("rsa = " + rsa);
       System.out.println("psyC = " + psyC);
       System.out.println("sos_pc = " + sos_pc);
       
       System.out.println("lamEs = " + lamEs);
       System.out.println("lamEs = " + lamE);
       System.out.println("lamEc = " + lamEc);*/
       
       
       double e = es + ec;
       
       this.potE.setValue(es);
       this.potT.setValue(ec);
       this.SWpotET.setValue(e);
       
       //System.out.println("potE = " + potE);
       //System.out.println("potT = " + potT);
       //System.out.println("SWpotET = " + es);
       
    
    }
    
    
    /**
     * calculates temperature factor 
     * @param Ta actual temperature in Ã?Â°C
     *  T1 threshold value for temperature in Ã?Â°C
     *  T2 threshold value for temperature in Ã?Â°C
     *  TL threshold value for temperature in Ã?Â°C
     *  TH threshold value for temperature in Ã?Â°C
     *  fT temperature factor
    */
    public static double temp_factor(double Ta){
         
        double T1 = 10;
        double T2 = 30;
        double TL = 0;
        double TH = 40;
        
        if(Ta < TL || Ta > TH){
            fT = 0;
        }
        if(TL < Ta && Ta < T1){
            fT = 1-Math.pow(((T1-Ta)/(T1-TL)),2);
        }
        if(T1 < Ta && Ta < T2){
            fT = 1;
        }
        if(T2 < Ta && Ta < TH){
            fT = 1-Math.pow(((Ta-T2)/(TH-T2)),2);
        }
        //System.out.println("fT " + fT);
        return fT;
    }
    
    /**
     * calculates factor for vapour pressure deficit 
     * @param est saturation vapour pressure in kPa
     * @param ea actual vapour pressure in kPa
     * cD threshold for vapour pressure deficit in kPa
     * Da vapour pressure deficit at reference height in kPa 
     * @return fD elevation in m -> wird zurueckgegeben
    */
    public static double vap_press_def(double est, double ea){
        
        double cD = 2;
        double Da = est-ea;
        
        fD = cD / (cD + Da);
        
        //System.out.println("fD " + fD);
        return fD;
    }
    
    /**
     * calculates stomatal conductance
     * @param LAI leaf area index
     * @param fT temperaure factor 
     * @param fD c
     * @param C extinktion coeffient for incoming radiation
     * @param R incoming global radiation in W/mÃ?Â²
     *  Rm maximum solar radiation in W/mÃ?Â²
     *  Rzf solar radiation at which stomatal conductance is halfed in W/mÃ?Â²
     *  Rz 
     *  glmin minimum stomatal conductivity in m/s
     *  glmax maximum stomatal conductivity in m/s
     *  gcs stomatal conductivity in m/s
    */
    public static double canopy_conduct(double LAI, double fT, double fD, double C, double R){
        
        int Rm = 1000;
        int Rzf = 100;
        double Rz = (double)Rm/((Rm/Rzf)-2);
    
        double glmin = 0.00001;
        double glmax = 0.53;
        
        gcs = LAI * glmin + fT * fD * (glmax -glmin)*((Rm+Rz)/(Rm*C))*Math.log((Rz+C*R)/(Rz+C*R*Math.exp(-C*LAI)));
        
        //System.out.println("gcs " + gcs);
        return gcs;
        
    }
    
 
}
