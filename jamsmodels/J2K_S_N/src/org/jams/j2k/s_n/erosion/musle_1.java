/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.s_n.erosion;

import jams.data.*;
import jams.model.*;


/**
 *
 * @author c8kiho
 */
public class musle_1 extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current hru object")
    public Attribute.EntityCollection entities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current time")
    public Attribute.Calendar time;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state variable rain")
    public Attribute.Double rain;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "precipitation in mm")
    public Attribute.Double precip;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU statevar RD1")
    public Attribute.Double outRD1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "soil loss in t/d")
    public Attribute.Double gensed;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar sediment inflow")
    public Attribute.Double insed;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU statevar sediment outflow")
    public Attribute.Double outsed;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU statevar sediment outflow")
    public Attribute.Double akksed;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU statevar sediment outflow")
    public Attribute.Double sedpool;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
    description = "snow depth")
    public Attribute.Double snowDepth;
    
    double run_outRD1, run_outsed, run_insed, run_gensed, run_akksed;

    /** Creates a new instance of musle */
    public void init() {
    }

    public void run() {


        double Sfac = 0;
        double Pfac = 0;

        double rainmus = rain.getValue();
        rainmus = Math.ceil(rainmus * 100) / 100; // Formatierung auf 2 Nachkommastellen 0.00

        double precipmus = precip.getValue();
        precipmus = Math.ceil(precipmus * 100) / 100;

        double snow = snowDepth.getValue();
        System.out.println("Schneehohe: " + snow);
                  
        this.run_outRD1 = outRD1.getValue();
        this.run_insed = insed.getValue();


        int Day = time.get(Attribute.Calendar.DAY_OF_YEAR);

        //Regen groesser 15  mm dann erst m?glicher Bodenabtrag
        // Oder der Oberfl?chenabfluss groesser 0 ????
        if (rainmus > 15 && snow == 0) {

          //  if (this.run_outRD1 > 0) {

                this.run_gensed = 0;
                this.run_outsed = 0;
                this.run_akksed = 0;

                Attribute.Entity entity = entities.getCurrent();

                double id = entity.getDouble("ID");
                double are = entity.getDouble("area");
                double slope = entity.getDouble("slope");
                double Cfac = entity.getDouble("C_factor");
                double ROK = entity.getDouble("A_skel_h0");
                double flowlength = entity.getDouble("slopelength");
                double Kfac = entity.getDouble("kvalue_h0"); //[kg*h*N-1*m-2]

                double are_ha = are / 10000; //m2 in ha

                double overflow = (this.run_outRD1 / are) * 10000 ; // outRD1 kommt in Liter/HRU und wird zu mm/ha
                

                //slope-Umrechung von ° in %
                double slopeperc = Math.round((Math.tan(Math.toRadians(slope))) * 100);

                if (slope > 2){ //erosive Hangneigung gr??er 2°
                //Hangneigunsgradient Sfac = S-Factor f?r slope kleiner 9% und g??er gleich 9%
                if (slopeperc >= 9) {
                    Sfac = 16.8 * Math.sin(Math.toRadians(slope)) - 0.5; // steil
                } else {
                    Sfac = 10.8 * Math.sin(Math.toRadians(slope)) + 0.03; // flach
                }
                
                Sfac = Math.ceil(Sfac * 100) / 100; // Formatierung auf 2 Nachkommastellen 0.00

                double rainm = rainmus / are; // Regen in mm / m?
                rainm = Math.ceil(rainm * 100) / 100; // Formatierung auf 2 Nachkommastellen 0.00

                double Lfacbeta = (Math.sin(Math.toRadians(slope)) / 0.0896) / (3 * Math.pow(Math.sin(Math.toRadians(slope)), 0.8) + 0.56);
                Lfacbeta = Math.ceil(Lfacbeta * 100) / 100; // Formatierung auf 2 Nachkommastellen 0.00

                double Lfacm = Lfacbeta / (1 + Lfacbeta);
                Lfacm = Math.ceil(Lfacm * 100) / 100; // Formatierung auf 2 Nachkommastellen 0.00

                double Lfac = Math.pow((flowlength / 22.13), Lfacm);

                Lfac = Math.ceil(Lfac * 100) / 100; // Formatierung auf 2 Nachkommastellen 0.00

                double LSfac = Lfac * Sfac;
                LSfac = Math.ceil(LSfac * 100) / 100;// Formatierung auf 2 Nachkommastellen 0.00

                double Pvorl = 0.4 * 0.02 * slopeperc;
                double HLkrit = 170 * Math.pow((Math.E), (-0.13 * slopeperc));
                if (flowlength < HLkrit) {
                    Pfac = Pvorl;
                } else {
                    Pfac = 1;
                }

                double ROKF = Math.pow((Math.E), (-0.53 * ROK));

                double peaktime = 0; // hour
                if (time.get(Attribute.Calendar.MONTH) >= 4 & time.get(Attribute.Calendar.MONTH) < 10) { //Summer half of the year
                     peaktime = 24;
                    } else {   //Winter half of the year
                     peaktime = 12;
                    }

                double tpeak = (0.278 * overflow * are_ha) / (3.6 * peaktime);
           
                double sedperhainto = (11.8 * Math.pow(((overflow * tpeak) * are_ha), 0.56)) * Kfac * LSfac * Pfac * Cfac * ROKF;
                sedperhainto = Math.ceil(sedperhainto * 100) / 100;// Formatierung auf 2 Nachkommastellen 0.00
                this.run_gensed = sedperhainto;
                }

                gensed.setValue(this.run_gensed);

                double raus = 0;
                double acc = 0;
                double accpool = entity.getDouble("sedpool");
                double wert1 = this.run_gensed - this.run_insed;
                double neuaccpool = accpool - wert1;
                if (neuaccpool < 0) {
                    raus = (-1 * neuaccpool);
                    neuaccpool = 0;

                } else {
                    if (wert1 < 0) {
                        acc = (-1 * wert1);
                        neuaccpool = accpool + acc;
                    }
                }

                sedpool.setValue(neuaccpool);
                outsed.setValue(raus);
                insed.setValue(raus);
                akksed.setValue(acc);

                entity.setDouble("sedpool", neuaccpool);

           // }
       }
    }

    

    public void cleanup() {
    }
}
