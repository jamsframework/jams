/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unijena.j2k.io;

import java.io.*;
import jams.data.*;
import jams.model.*;
import java.util.*;

/**
 *
 * @author c5pfbj
 */
public class ContourBanksReader extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "An- bzw. Ausschalten des Moduls")
    public Attribute.Boolean cbModulAktiv;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Parameter file name for ContourBanks")
    public Attribute.String contourbankFileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Collection of hru objects")
    public Attribute.EntityCollection hrus;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "ContourBank-Hoehe")
    public Attribute.Double cbWallhoehe;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "ContourBank-Boeschungswinkel vorn")
    public Attribute.Double cbBoeschungswinkelWall_vorn;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "ContourBank-Boeschungswinkel hinten")
    public Attribute.Double cbBoeschungswinkelWall_hinten;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "ContourBank-Grabentiefe direkt hinter dem Wall")
    public Attribute.Double cbTiefeGraben_vorn;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "ContourBank-Grabenbreite")
    public Attribute.Double cbBreiteGraben;
    boolean modulCBaktiv;

    public void init() throws Attribute.Entity.NoSuchAttributeException, FileNotFoundException, IOException {

        this.modulCBaktiv = cbModulAktiv.getValue();

        if (this.modulCBaktiv == true) {

            double wallhoeheCB = this.cbWallhoehe.getValue();
            double boeschungswinkelWall_vorn = this.cbBoeschungswinkelWall_vorn.getValue();
            double boeschungswinkelWall_hinten = this.cbBoeschungswinkelWall_hinten.getValue();            
            double breiteGraben = this.cbBreiteGraben.getValue();

            BufferedReader reader5;
            //HashMap<Integer, Attribute.Entity> hruMap = new HashMap<Integer, Attribute.Entity>();
            HashMap<Double, Attribute.Entity> hruMap = new HashMap<Double, Attribute.Entity>();
            //Iterator<Attribute.Entity> hruIterator;
            Iterator<Attribute.Entity> hruIterator;
            Attribute.Entity aktuelleHRU;

            //put all entities into a HashMap with their ID as key
            hruIterator = hrus.getEntities().iterator();
            while (hruIterator.hasNext()) {
                aktuelleHRU = hruIterator.next();
                aktuelleHRU.setDouble("cbAnzahl", 0.0);
                aktuelleHRU.setDouble("cbGesamtlaenge", 0.0);
                aktuelleHRU.setDouble("cbGrabentiefe_vorn", 0.0);
                aktuelleHRU.setDouble("cbFassungsvermoegenMax", 0.0);
                aktuelleHRU.setDouble("cbSpeicherAkt", 0.0);
                aktuelleHRU.setDouble("cbAbfussReach", 0.0);

                double[] anteileKomponentenCBReach_Array = {0.0, 0.0, 0.0};
                aktuelleHRU.setObject("cbAnteileKomponenten", anteileKomponentenCBReach_Array);

                hruMap.put(aktuelleHRU.getDouble("ID"), aktuelleHRU);
            }

            //Auslesen der ContourBank-Eigenschaften
            reader5 = new BufferedReader(new FileReader(getModel().getWorkspaceDirectory().getPath() + "/" + contourbankFileName.getValue()));

            String HRUsLine = "#";
            while (HRUsLine.startsWith("#")) {
                HRUsLine = reader5.readLine();
            }

            while ((HRUsLine != null) && !HRUsLine.startsWith("#")) {
                //String zeichenkette = "\t";
                String zeichenkette = ",-8888.000,";

                String[] HRUsSplitArray = HRUsLine.split(zeichenkette);

                double HRUsID = Double.parseDouble(HRUsSplitArray[0]);
                double anzahlCB = Double.parseDouble(HRUsSplitArray[1]);
                double gesamtlaengeCB = Double.parseDouble(HRUsSplitArray[2]);

                aktuelleHRU = hruMap.get(HRUsID);
                //double HRUsArea = aktuelleHRU.getDouble("area");
                double HRUsSlope = aktuelleHRU.getDouble("slope");

                //Falls die Tiefe des ContourBank-Grabens groesser ist, als Boden und oberer Grundwasserspeicher gemeinsam, wird sie verringert
                double maechtigkeitRD2_cm = aktuelleHRU.getDouble("depth"); //in cm
                double maechtigkeitRG1_cm = aktuelleHRU.getDouble("depthRG1"); //in cm

                double maechtigkeitRD2_senkrecht_m = (maechtigkeitRD2_cm / 100) / Math.sin(Math.toRadians(90 - HRUsSlope));
                double maechtigkeitRG1_senkrecht_m = (maechtigkeitRG1_cm / 100) / Math.sin(Math.toRadians(90 - HRUsSlope));

                double tiefeGraben_vorn = this.cbTiefeGraben_vorn.getValue();
                double tiefeGraben_hinten = tiefeGraben_vorn + breiteGraben * Math.tan(Math.toRadians(HRUsSlope));

                if (tiefeGraben_hinten >= maechtigkeitRD2_senkrecht_m + maechtigkeitRG1_senkrecht_m) {
                    tiefeGraben_hinten = maechtigkeitRD2_senkrecht_m + maechtigkeitRG1_senkrecht_m - 0.01;
                    tiefeGraben_vorn = tiefeGraben_hinten - breiteGraben * Math.tan(Math.toRadians(HRUsSlope));
                }

                //Berechnung des ContourBank-Querschnitts
                double hoeheWallMitte = wallhoeheCB - wallhoeheCB * Math.tan(Math.toRadians(HRUsSlope)) / Math.tan(Math.toRadians(boeschungswinkelWall_vorn));
                double flaecheDreieck1 = 0.5 * Math.pow(hoeheWallMitte, 2) / Math.tan(Math.toRadians(HRUsSlope));
                double flaecheDreieck2 = 0.5 * Math.pow(hoeheWallMitte, 2) / Math.tan(Math.toRadians(boeschungswinkelWall_hinten));
                double ankatheteDreieck1 = hoeheWallMitte / Math.tan(Math.toRadians(boeschungswinkelWall_hinten));
                double ankatheteDreieck2 = ankatheteDreieck1 * Math.tan(Math.toRadians(boeschungswinkelWall_hinten)) / (Math.tan(Math.toRadians(HRUsSlope)) + Math.tan(Math.toRadians(boeschungswinkelWall_hinten)));
                double flaecheDreieck3 = 0.5 * Math.tan(Math.toRadians(HRUsSlope)) * Math.pow(ankatheteDreieck2, 2);
                double flaecheDreieck4 = 0.5 * Math.tan(Math.toRadians(boeschungswinkelWall_hinten)) * Math.pow(ankatheteDreieck1 - ankatheteDreieck2, 2);
 
                double teilflaeche1 = flaecheDreieck1 - flaecheDreieck2 + flaecheDreieck3 + flaecheDreieck4;
                double teilflaeche2 = tiefeGraben_vorn * breiteGraben;
                double teilflaeche3 = 0.5 * Math.pow(breiteGraben, 2) * Math.tan(Math.toRadians(HRUsSlope));
                double querschnittCB_max = teilflaeche1 + teilflaeche2 + teilflaeche3;

                //Berechnung des ContourBank-Speichers in Abhaengigkeit von Hangneigung, Laenge des Walls und HRU-Flaeche
                //double HRUsSpeicherCBmax = 1000 * HRUsQuerschnittCBmax * HRUsLaengeCB / HRUsArea; //in mm
                double fassungsvermoegenCB_max = 1000 * querschnittCB_max * gesamtlaengeCB; //in l

                aktuelleHRU.setDouble("cbAnzahl", anzahlCB);
                aktuelleHRU.setDouble("cbGesamtlaenge", gesamtlaengeCB);
                aktuelleHRU.setDouble("cbGrabentiefe_vorn", tiefeGraben_vorn);
                aktuelleHRU.setDouble("cbFassungsvermoegenMax", fassungsvermoegenCB_max);

                //Auslesen der jeweils naechsten Zeile
                HRUsLine = reader5.readLine();
            }
        }
    }
}
