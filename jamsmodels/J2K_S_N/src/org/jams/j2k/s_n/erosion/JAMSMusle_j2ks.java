package org.jams.j2k.s_n.erosion;

import jams.data.*;
import jams.model.*;
import java.util.Calendar;

@JAMSComponentDescription(title = "JAMSMusle_j2ks",
description = "JAMS native Version of MusleMay",
author = "Holm + Manfred")
public class JAMSMusle_j2ks extends JAMSComponent {

//Read access variables
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "The current hru entity")
    public JAMSEntityCollection entities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "in cm depth of soil layer")
    public JAMSDoubleArray layerdepth;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "")
    public JAMSDouble area;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "")
    public JAMSDouble slope;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "")
    public JAMSDouble Cfac;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "")
    public JAMSDouble ROK;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "")
    public JAMSDouble flowlength;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "")
    public JAMSDouble Kfac;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current time")
    public JAMSCalendar time;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar RD1")
    public JAMSDouble outRD1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "snow depth")
    public JAMSDouble snowDepth;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "precipitation mm")
    public JAMSDouble precip;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "surface temperature")
    public JAMSDouble surfacetemp;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "ID")
    public JAMSDouble ID;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Bypass factor for surface runoff sediment from upstream")
    public JAMSDouble Surrun_bypass;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar sediment inflow")
    public JAMSDouble insed;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar sediment outflow")
    public JAMSDouble sedpool;
//Write Access variables
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "HRU statevar sediment outflow")
    public JAMSDouble outsed;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "soil loss")
    public JAMSDouble gensed;

    public void run() throws JAMSEntity.NoSuchAttributeException {

        // passing reads into in's
        Double bysed = 0.0;
        Double area = this.area.getValue();
        Double ID = this.ID.getValue();
        Double slope = this.slope.getValue();
        Double Cfac = this.Cfac.getValue();
        Double ROK = this.ROK.getValue();
        Double slopelength = this.flowlength.getValue();
        Double Kfac = this.Kfac.getValue();

        Double outRD1 = this.outRD1.getValue();
        Double snowDepth = this.snowDepth.getValue();
        Double surfacetemp = this.surfacetemp.getValue();
        Double precip = this.precip.getValue();
        Double insed = this.insed.getValue();
        Double sedpool = this.sedpool.getValue();


        bysed = insed * (Surrun_bypass.getValue());
        insed = insed * (1 - Surrun_bypass.getValue());
        
        // calling the oms3 execute


        Double gensed = 0.0;


        if ((slope > 0) && (surfacetemp > 0.1) && (snowDepth == 0) && (outRD1 > 0)) {

            // slope-Umrechung von � in %
            double slopeperc = Math.tan(Math.toRadians(slope)) * 100;
            //System.out.println("ID: "+ ID  + " slope_deg : " + slope + " slope_perc: " + slopeperc);
            // slope Sfac = S-Factor f?r slope kleiner 9% und g??er gleich 9%
            double Sfac = 0;
            if (slopeperc >= 9) {
                Sfac = 16.8 * Math.sin(Math.toRadians(slope)) - 0.5;   // steil
            } else {
                Sfac = 10.8 * Math.sin(Math.toRadians(slope)) + 0.03; // flach
            }

            double Lfacbeta = (Math.sin(Math.toRadians(slope)) / 0.0896)
                    / (3 * Math.pow(Math.sin(Math.toRadians(slope)), 0.8) + 0.56);
            double Lfacm = Lfacbeta / (1 + Lfacbeta);
            double Lfac = Math.pow(slopelength / 22.13, Lfacm);
            double LSfac = Lfac * Sfac;
            double Pvorl = 0.4 * 0.02 * slopeperc;
            double HLkrit = 170 * Math.pow(Math.E, -0.13 * slopeperc);
            double Pfac = slopelength < HLkrit ? Pvorl : 1;
            double ROKF = Math.pow(Math.E, -0.053 * ROK);

            double peaktime = 0; // hours
            if (time.get(Calendar.MONTH) > 4 & time.get(Calendar.MONTH) < 10) {
                peaktime = 4;               // Summer
            } else {
                peaktime = 14;              // Winter
            }

            if ((precip == 0.0) && (outRD1 > 0)) { // only snowmelting
                //    //System.out.println(" outRD1:" + outRD1/area + " und NS:" + precip + " PeakTime old: "+ peaktime);
                peaktime = 24;
            }

            double area_ha = area / 10000; //m2 to ha
            double area_km2 = area / 1000000; //m2 to km2
            double Qsurf_peak_m3 = ((outRD1 / area) * area) / 1000; // m?


            // double Qsurf_mm_ha = (outRD1 / area) * 10000; Holm orginal
            double Qsurf_mm_ha = (outRD1 / area); //manfred new checked with SWAT sources

            //System.out.println("ID: "+ ID + " Qsurf_peak_m3:" + Qsurf_peak_m3 + " Qsurf_mm_ha:" + Qsurf_mm_ha + " und NS:" + precip + " PeakTime: "+ peaktime);

            double Qpeak = (0.278 * Qsurf_peak_m3 * area_km2) / (3.6 * peaktime);//--> m3/s
            double Lamb = 11.8 * Math.pow((Qsurf_mm_ha * Qpeak * area_ha), 0.56); // SWAT-MULSE Williams, 1995
            double sedperhainto = Lamb * Kfac * LSfac * Pfac * Cfac * ROKF; // SWAT-MULSE Williams, 1995
            //gensed = (sedperhainto / 10000) * area;     // t / HRU
            gensed = sedperhainto;

        }


        double out = 0;
        double bal = gensed - insed;
        double neuaccpool = sedpool - bal;

        if (neuaccpool < 0) {
            out = (-1) * neuaccpool;
            neuaccpool = 0; // sediment pool
        } else {
            if (bal < 0) {
                double acc = (-1) * bal;
                neuaccpool = sedpool + acc;
                if (outRD1 > 0) {
                    out = 0.05 * acc;
                }
            }
        }

        //if (out > 0) {
        //   System.out.println(" ID " + ID + " Pool: " + neuaccpool + " gen: " + gensed + " in: " + insed + " out: " + out);
        //}

        sedpool = neuaccpool;

        out = out + bysed;



        // reading the outs
        this.insed.setValue(0);
        this.sedpool.setValue(sedpool);
        this.outsed.setValue(out);
        this.gensed.setValue(gensed);
    }
}
