/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unijena.j2k.soilWater;

import jams.data.*;
import jams.model.*;
import java.lang.Math.*;

/**
 *
 * @author c5pfbj
 */
public class J2KProcessContourBanks extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "An- bzw. Ausschalten des Moduls")
    public Attribute.Boolean cbModulAktiv;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Zufluss in die ContourBank ausschlie?lich ueber die gesaettigte oder aber die gesamte Maechtigkeit von RD2")
    public Attribute.Boolean cbZuflussMgesRD2Aktiv;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Zufluss in die ContourBank ausschlie?lich ueber die gesaettigte oder aber die gesamte Maechtigkeit von RG1")
    public Attribute.Boolean cbZuflussMgesRG1Aktiv;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Kalibrationskoeffizient zur Anpassung des Zuflusses in die ContourBank aus RD2 bzw. RG1")
    public Attribute.Double cbKalibZufluss;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Kalibrationskoeffizient zur Anpassung der Infiltration aus der ContourBank in RD2 bzw. RG1")
    public Attribute.Double cbKalibInfiltration;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Kalibrationskoeffizient zur Anpassung der gesattigten Maechtigkeit von RD2")
    public Attribute.Double cbKalibGesaettigteMaechtigkeit;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Anteil des Wassers, welches aus der ContourBank in RD2 bzw. RG1 infiltriert und anschliessend in unterliegende HRUs verteilt wird")
    public Attribute.Double cbKalibInfiltrationRouting;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Routingkoeffizient zur Anpassung des Grabenabflusses im ContourBank-System")
    public Attribute.Double cbKalibAbflussReach;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The current hru entity")
    public Attribute.EntityCollection hrus;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "temporal resolution [d | h | m]")
    public Attribute.String tempRes;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "attribute slope")
    public Attribute.Double slope;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "ContourBank-Boeschungswinkel hinten")
    public Attribute.Double cbBoeschungswinkelWall_hinten;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "ContourBank-Grabenbreite")
    public Attribute.Double cbBreiteGraben;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Rauhigkeitsbeiwert im ContourBank-Graben")
    public Attribute.Double cbRauhigkeitsbeiwertGraben;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "RD1 outflow")
    public Attribute.Double outRD1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "saturation of LPS")
    public Attribute.Double satLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "RD2 outflow")
    public Attribute.Double outRD2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar RD2 inflow")
    public Attribute.Double inRD2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "maximum RG1 storage")
    public Attribute.Double maxRG1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "actual RG1 storage")
    public Attribute.Double actRG1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "RG1 outflow")
    public Attribute.Double outRG1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RG1 inflow")
    public Attribute.Double inRG1;
    // zusätliches rausschreiben für das Layerinterface
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "conturbanks outflow",
    unit = "l")
    public Attribute.Double cbAbfussReach;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "conturbanks storage",
    unit = "l")
    public Attribute.Double cbSpeicherAkt;
    
   

    boolean modulCBaktiv, zuflussMgesRD2aktiv, zuflussMgesRG1aktiv;
    
    double kalibZufluss, kalibInfiltration, kalibInfiltrationRouting, kalibGesaettigteMaechtigkeit, kalibAbflussReach,
            HRUsSlope, boeschungswinkelWall_hinten, breiteGraben, rauhigkeitsbeiwertGraben, run_outRD1, run_satLPS, run_outRD2, run_inRD2,
            run_maxRG1, run_actRG1, run_outRG1, run_inRG1;

    public void init() {

        this.modulCBaktiv = cbModulAktiv.getValue();

        if (this.modulCBaktiv == true) {

            this.zuflussMgesRD2aktiv = cbZuflussMgesRD2Aktiv.getValue();
            this.zuflussMgesRG1aktiv = cbZuflussMgesRG1Aktiv.getValue();
            //Der Parameter regelt, ob der Zufluss in die ContourBank ausschlie?lich ueber die gesaettigte oder die gesamte Maechtigkeit von RD2 bzw. RG1 erfolgt.
            //Letzteres ist insofern sinnvoll, als das bei sehr geringen Saettigungen und entsprechendem Gradienten trotzdem Zufluss in die ContourBank erfolgen kann.

            this.kalibZufluss = cbKalibZufluss.getValue();
            this.kalibInfiltration = cbKalibInfiltration.getValue();
            this.kalibGesaettigteMaechtigkeit = cbKalibGesaettigteMaechtigkeit.getValue();

            //this.kalibInfiltrationRouting = cbKalibInfiltrationRouting.getValue();
            this.kalibAbflussReach = cbKalibAbflussReach.getValue();

            this.boeschungswinkelWall_hinten = cbBoeschungswinkelWall_hinten.getValue();
            this.breiteGraben = cbBreiteGraben.getValue();
            this.rauhigkeitsbeiwertGraben = cbRauhigkeitsbeiwertGraben.getValue();
        }
    }

    public void run() {
    
        Attribute.Entity aktuelleHRU;
    
        if (this.modulCBaktiv == true) {

            aktuelleHRU = hrus.getCurrent();

            double tiefeGraben_vorn = aktuelleHRU.getDouble("cbGrabentiefe_vorn"); //in m
            double gesamtlaengeCB = aktuelleHRU.getDouble("cbGesamtlaenge"); //in m

            if (this.modulCBaktiv == true && gesamtlaengeCB > 0) {

                double faktorZeit = 86400;

                if (this.tempRes.getValue().equals("h")) {
                    faktorZeit = 3600;
                } else if (this.tempRes.getValue().equals("m")) {
                    faktorZeit = 60;
                }

                this.HRUsSlope = slope.getValue();
                if (this.HRUsSlope < 0.01) {
                    this.HRUsSlope = 0.01;
                }

                double anzahlCB = aktuelleHRU.getDouble("cbAnzahl");
                double fassungsvermoegenCB_max = aktuelleHRU.getDouble("cbFassungsvermoegenMax"); // in l
                double speicherCB_akt = aktuelleHRU.getDouble("cbSpeicherAkt"); // in l

                this.run_outRD1 = outRD1.getValue();

                double kfRD2_max = aktuelleHRU.getDouble("kf_max"); //in m/d
                double maechtigkeitRD2_cm = aktuelleHRU.getDouble("depth"); //in cm
                this.run_satLPS = satLPS.getValue();
                this.run_outRD2 = outRD2.getValue();
                this.run_inRD2 = inRD2.getValue();

                double kfRG1_max = aktuelleHRU.getDouble("Kf_geo"); //in cm/d
                double maechtigkeitRG1_cm = aktuelleHRU.getDouble("depthRG1"); //in cm
                this.run_maxRG1 = maxRG1.getValue();
                this.run_actRG1 = actRG1.getValue();
                this.run_outRG1 = outRG1.getValue();
                this.run_inRG1 = inRG1.getValue();

                //Berechnung des Saettigung des oberen Grundwasserspeichers
                double run_satRG1 = this.run_actRG1 / this.run_maxRG1;

                //Berechnung der KF-Werte
                double kfRD2_mm_h = kfRD2_max * 1000 / 24;
                double kfRD2_m_s = kfRD2_mm_h / 3600000;

                double kfRG1_mm_h = kfRG1_max * 10 / 24;
                double kfRG1_m_s = kfRG1_mm_h / 3600000;

                //Umrechnung der Maechtigkeit des Bodens und des oberen Grundwasserspeichers in m, Berechnung der senkrechten Maechtigkeit des Bodens und des oberen Grundwasserspeichers
                double maechtigkeitRD2_m = maechtigkeitRD2_cm / 100;
                double maechtigkeitRD2_senkrecht_m = maechtigkeitRD2_m / Math.sin(Math.toRadians(90 - this.HRUsSlope));

                double maechtigkeitRG1_m = maechtigkeitRG1_cm / 100;
                double maechtigkeitRG1_senkrecht_m = maechtigkeitRG1_m / Math.sin(Math.toRadians(90 - this.HRUsSlope));

                //Aufruf der Funktionen zur Berechnung der gesaettigten Maechtigkeiten des Bodens und des oberen Grundwasserspeichers
                double gesaettigteMaechtigkeitRD2_vorn_m = 0;
                double gesaettigteMaechtigkeitRD2_hinten_m = 0;

                double[] gesaettigteMaechtigkeitRD2_Array = this.calcGesaettigteMaechtigkeit(this.kalibGesaettigteMaechtigkeit, this.run_satLPS, maechtigkeitRD2_m);
                gesaettigteMaechtigkeitRD2_vorn_m = gesaettigteMaechtigkeitRD2_Array[0];
                gesaettigteMaechtigkeitRD2_hinten_m = gesaettigteMaechtigkeitRD2_Array[1];

                double gesaettigteMaechtigkeitRG1_vorn = 0;
                double gesaettigteMaechtigkeitRG1_hinten = 0;

                double[] gesaettigteMaechtigkeitRG1_Array = this.calcGesaettigteMaechtigkeit(this.kalibGesaettigteMaechtigkeit, run_satRG1, maechtigkeitRG1_m);
                gesaettigteMaechtigkeitRG1_vorn = gesaettigteMaechtigkeitRG1_Array[0];
                gesaettigteMaechtigkeitRG1_hinten = gesaettigteMaechtigkeitRG1_Array[1];

                //Berechnung der ContourBank-Gradienten in Prozent
                int slopeGraben_temp = (int) Math.round(20 * (4.43 * Math.tan(Math.toRadians(this.HRUsSlope)) + 0.0656));
                double slopeGraben = (double) slopeGraben_temp / 2000;

                if (slopeGraben < 0.001) {
                    slopeGraben = 0.001;
                }
                if (slopeGraben > 0.006) {
                    slopeGraben = 0.006;
                }

                //Berechnung des maximalen Querschnitts der ContourBank
                double querschnittCB_max = fassungsvermoegenCB_max / (1000 * gesamtlaengeCB);

                //Berechnung der Querschnittsflaechen fuer den Graben
                double querschnittGraben_unten = tiefeGraben_vorn * this.breiteGraben;
                double querschnittGraben_oben = querschnittGraben_unten + Math.pow(this.breiteGraben, 2) * Math.tan(Math.toRadians(this.HRUsSlope)) + 0.5 * Math.pow(Math.tan(Math.toRadians(this.HRUsSlope)) * this.breiteGraben, 2) / Math.tan(Math.toRadians(90 - this.boeschungswinkelWall_hinten));

                double zuflussRD1_akt = this.run_outRD1;

                double speicherCB_alt = speicherCB_akt;
                double speicherCB_init = speicherCB_akt + zuflussRD1_akt;

                speicherCB_akt = speicherCB_init;

                double speicherCB_akt_temp = 0;

                boolean weiter = true;
                int i = 0;
                while (weiter == true && i < 100) {
                    i++;
                    //Berechnung des aktuellen Speichers einer ContourBank
                    double speicher1CB_akt = speicherCB_akt / anzahlCB;

                    //Aufruf der Funktionen zur Berechnung der Fliessgeschwindigkeit, des benetzten Umfangs und des Wasserstandes in der ContourBank
                    double[] Fliessgeschwindigkeit_Array = this.calcFliessgeschwindigkeit(faktorZeit, this.HRUsSlope, this.boeschungswinkelWall_hinten, tiefeGraben_vorn, this.breiteGraben, this.rauhigkeitsbeiwertGraben, speicher1CB_akt, slopeGraben, querschnittCB_max, querschnittGraben_unten, querschnittGraben_oben);
                    double tiefeGraben_hinten = Fliessgeschwindigkeit_Array[1];
                    double wasserstandCB_akt = Fliessgeschwindigkeit_Array[2];
                    double benetzterUmfangGraben_akt = Fliessgeschwindigkeit_Array[4];
                    double fliessgeschwindigkeit1CB = Fliessgeschwindigkeit_Array[5];
                    double oberflaechenabflussCB_akt = Fliessgeschwindigkeit_Array[6] * anzahlCB;
                    double speicherCB_max = Fliessgeschwindigkeit_Array[7] * anzahlCB;

                    if (oberflaechenabflussCB_akt > 0) {
                        speicherCB_akt = speicherCB_max;
                    }

                    //Aufruf der Funktion zur Berechnung des benetzten Umfanges fuer den Boden bzw. den oberen und unteren Grundwasserspeicher
                    double durchbrochenRD2 = 0, durchbrochenRG1 = 0, benetzterUmfangRD2_akt = 0;
                    double maechtigkeitOberschichtenRD2_senkrecht_m = 0;
                    double maechtigkeitOberschichtenRG1_senkrecht_m = maechtigkeitRD2_senkrecht_m;

                    if (tiefeGraben_hinten >= maechtigkeitRD2_senkrecht_m) {
                        durchbrochenRD2 = 1;
                    }

                    if (durchbrochenRD2 == 0) {
                        benetzterUmfangRD2_akt = this.calcBenetzterUmfangSchichtenNichtDurchbrochen(this.HRUsSlope, tiefeGraben_vorn, this.breiteGraben, tiefeGraben_hinten, wasserstandCB_akt, maechtigkeitOberschichtenRD2_senkrecht_m);
                    } else {
                        benetzterUmfangRD2_akt = this.calcBenetzterUmfangSchichtenDurchbrochen(this.HRUsSlope, tiefeGraben_vorn, maechtigkeitRD2_senkrecht_m, wasserstandCB_akt, maechtigkeitOberschichtenRD2_senkrecht_m);
                    }

                    double benetzterUmfangRG1_akt = this.calcBenetzterUmfangSchichtenNichtDurchbrochen(this.HRUsSlope, tiefeGraben_vorn, this.breiteGraben, tiefeGraben_hinten, wasserstandCB_akt, maechtigkeitOberschichtenRD2_senkrecht_m);

                    //Aufruf der Funktion zur Berechnung des Zuflusses aus Boden bzw. oberen Grundwasserspeicher in die ContourBank und des Abflusses
                    double zuflussRD2_akt = 0;
                    double infiltrationRD2_akt = 0;
                    double gradientRD2_nach_hinten;

                    double zuflussRG1_akt = 0;
                    double infiltrationRG1_akt = 0;
                    double gradientRG1_nach_hinten;

                    double[] zuflussRD2_Array = this.calcZufluss(this.zuflussMgesRD2aktiv, this.kalibZufluss, tiefeGraben_hinten, this.run_outRD2, maechtigkeitOberschichtenRD2_senkrecht_m, maechtigkeitRD2_senkrecht_m, gesaettigteMaechtigkeitRD2_vorn_m, wasserstandCB_akt, durchbrochenRD2);
                    gradientRD2_nach_hinten = zuflussRD2_Array[0];
                    zuflussRD2_akt = zuflussRD2_Array[1];

                    infiltrationRD2_akt = this.calcInfiltration(this.kalibInfiltration, tiefeGraben_vorn, kfRD2_m_s, maechtigkeitRD2_senkrecht_m, gesaettigteMaechtigkeitRD2_hinten_m, wasserstandCB_akt, gradientRD2_nach_hinten, durchbrochenRD2, speicherCB_akt, benetzterUmfangGraben_akt, benetzterUmfangRD2_akt);

                    if (durchbrochenRD2 == 1) {
                        double maechtigkeitRD2undRG1_senkrecht_m = maechtigkeitRD2_senkrecht_m + maechtigkeitRG1_senkrecht_m;

                        double[] zuflussRG1_Array = this.calcZufluss(this.zuflussMgesRG1aktiv, this.kalibZufluss, tiefeGraben_hinten, this.run_outRG1, maechtigkeitOberschichtenRG1_senkrecht_m, maechtigkeitRD2undRG1_senkrecht_m, gesaettigteMaechtigkeitRG1_vorn, wasserstandCB_akt, durchbrochenRG1);
                        gradientRG1_nach_hinten = zuflussRG1_Array[0];
                        zuflussRG1_akt = zuflussRG1_Array[1];

                        infiltrationRG1_akt = this.calcInfiltration(this.kalibInfiltration, tiefeGraben_vorn, kfRG1_m_s, maechtigkeitRD2undRG1_senkrecht_m, gesaettigteMaechtigkeitRG1_hinten, wasserstandCB_akt, gradientRG1_nach_hinten, durchbrochenRG1, speicherCB_akt, benetzterUmfangGraben_akt, benetzterUmfangRG1_akt);
                    }

                    speicherCB_akt_temp = speicherCB_init - infiltrationRD2_akt - infiltrationRG1_akt + zuflussRD2_akt + zuflussRG1_akt;

                    if (speicherCB_akt_temp == 0) {
                        weiter = false;
                    } else if (Math.abs(speicherCB_akt / speicherCB_akt_temp - 1) < 0.01 || speicherCB_akt_temp == speicherCB_init) {  //wenn speicherCB_init == speicherCB_akt_temp wuerde sich die Schleife totlaufen, da der Ausgangspunkt der Berechnungen wieder erreicht ist
                        weiter = false;
                    }

                    //weiter = false;//Bei Verwendung dieser Zeile ist die Iteration auskommentiert
                    if (weiter == true) { //Bei Verwendung dieser Zeile erfolgt die Berechnung der einzelnen Groessen iterativ
                        //Normalerweise wuerde hier speicherCB_akt_temp an speicherCB_akt uebergeben und mit diesem Wert im naechsten Schleifendurchlauf weitergerechnet.
                        //Problematisch ist jedoch, dass dabei ein Ping-Pong-Effekt auftreten kann, d.h. dass die Speicherinhalte zwischen zwei Werten hin und her springen und sich
                        //kaum einanander annaehern. Insofern wird nunmehr mit dem Mittelwert aus speicherCB_akt und speicherCB_akt_temp weitergerechnet und das Problem somit umgangen.
                        speicherCB_akt = (speicherCB_akt + speicherCB_akt_temp) / 2;
                    } else {
                        speicherCB_akt = speicherCB_akt_temp;

                        if (speicherCB_akt_temp > speicherCB_max) {
                            speicherCB_akt = speicherCB_max;
                            oberflaechenabflussCB_akt = oberflaechenabflussCB_akt + (speicherCB_akt_temp - speicherCB_max);
                        }

                        double[] anteileKomponentenCBReach_Array = (double[]) aktuelleHRU.getObject("cbAnteileKomponenten");
                        anteileKomponentenCBReach_Array = this.calcVerhaeltnisseCBKomponenten(speicherCB_alt, anteileKomponentenCBReach_Array, zuflussRD1_akt, zuflussRD2_akt, zuflussRG1_akt);
                        aktuelleHRU.setObject("cbAnteileKomponenten", anteileKomponentenCBReach_Array);

                        //Berechnung der durchschnittlichen Laenge einer ContourBank bzw. ihres aktuellen Speichers
                        double gesamtlaenge1CB = gesamtlaengeCB / anzahlCB;
                        speicher1CB_akt = speicherCB_akt / anzahlCB;

                        //Aufruf der Funktion zur Berechnung des Abflusses im ContourBank-Reach
                        double abflussCBReach_akt = anzahlCB * this.calcAbflussCBReach(this.kalibAbflussReach, gesamtlaenge1CB, speicher1CB_akt, fliessgeschwindigkeit1CB);

                        double infiltrationRD2Routing_akt = 0, infiltrationRG1Routing_akt = 0;

                        if (infiltrationRD2_akt > 0) {
                        //if (infiltrationRD2_akt > 0 && this.kalibInfiltrationRouting > 0) {
                            infiltrationRD2Routing_akt = 1 / anzahlCB * infiltrationRD2_akt; //Das Wasser wird ueber das Routing an die unterliegenden HRUs weiterverteilt
                            //infiltrationRD2Routing_akt = 1 / this.kalibInfiltrationRouting * infiltrationRD2_akt;
                        }

                        double infiltrationRD2Input = infiltrationRD2_akt - infiltrationRD2Routing_akt; //Das Wasser dient im naechsten Zeitschritt als Input in die gleiche HRU

                        if (infiltrationRG1_akt > 0) {
                        //if (infiltrationRG1_akt > 0 && this.kalibInfiltrationRouting > 0) {
                            infiltrationRG1Routing_akt = 1 / anzahlCB * infiltrationRG1_akt;
                            //infiltrationRG1Routing_akt = 1 / this.kalibInfiltrationRouting * infiltrationRG1_akt;
                        }

                        double infiltrationRG1Input = infiltrationRG1_akt - infiltrationRG1Routing_akt;

                        aktuelleHRU.setDouble("cbAbfussReach", abflussCBReach_akt);
                        aktuelleHRU.setDouble("cbSpeicherAkt", speicherCB_akt - abflussCBReach_akt);
                        
                        // zusätliches rausschreiben für das Layerinterface
                        
                        cbAbfussReach.setValue(abflussCBReach_akt);
                        cbSpeicherAkt.setValue(speicherCB_akt - abflussCBReach_akt);
                        
                        //

                        this.outRD1.setValue(oberflaechenabflussCB_akt);
                        this.outRD2.setValue(this.run_outRD2 - zuflussRD2_akt + infiltrationRD2Routing_akt);
                        this.outRG1.setValue(this.run_outRG1 - zuflussRG1_akt + infiltrationRG1Routing_akt);

                        this.inRD2.setValue(infiltrationRD2Input);
                        this.inRG1.setValue(infiltrationRG1Input);
                    }
                }
            }
        }
    }

    //Berechung der Maechtikeit, bis zu welcher der Boden aufgesaettigt ist (Ansatz aus SWAT2000)
    private double[] calcGesaettigteMaechtigkeit(double kalibGesaettigteMaechtigkeit, double saettigung, double maechtigkeitSchicht_m) {

        double gesaettigteMaechtigkeitSchicht;
        double gesaettigteMaechtigkeitSchicht_vorn_m = maechtigkeitSchicht_m;
        double gesaettigteMaechtigkeitSchicht_hinten_m = 0;

        saettigung = kalibGesaettigteMaechtigkeit * saettigung;

        if (saettigung > 1.0) {
            saettigung = 1.0;
        }

        if (saettigung <= 0.5) {
            gesaettigteMaechtigkeitSchicht = maechtigkeitSchicht_m * saettigung / 0.5;
        } else {
            gesaettigteMaechtigkeitSchicht = maechtigkeitSchicht_m * (saettigung - 0.5) / 0.5;
        }

        if (saettigung <= 0.5) {
            gesaettigteMaechtigkeitSchicht_vorn_m = gesaettigteMaechtigkeitSchicht;
        } else {
            gesaettigteMaechtigkeitSchicht_hinten_m = gesaettigteMaechtigkeitSchicht;
        }

        double[] gesaettigteMaechtigkeit_Array = {gesaettigteMaechtigkeitSchicht_vorn_m, gesaettigteMaechtigkeitSchicht_hinten_m};
        return gesaettigteMaechtigkeit_Array;
    }

    //Berechnung der Fliessgeschwindigkeit in der ContourBank (iterativ)
    private double[] calcFliessgeschwindigkeit(double faktorZeit, double HRUsSlope, double boeschungswinkelWall_hinten, double tiefeGraben_vorn, double breiteGraben, double rauhigkeitsbeiwertGraben, double speicher1CB_akt, double slopeGraben, double querschnittCB_max, double querschnittGraben_unten, double querschnittGraben_oben) {

        double speicher1CB_max;
        double oberflaechenabfluss1CB_akt = 0;

        double fliessgeschwindigkeit1CB = 1;
        double fliessgeschwindigkeit1CB_temp = 0;
        double q_m = speicher1CB_akt / (1000 * faktorZeit); //Umwandlung des CB-Speicher von l in m3/Zeitschritt
        double querschnittCB_akt = q_m / fliessgeschwindigkeit1CB;

        speicher1CB_max = 1000 * faktorZeit * querschnittCB_max * fliessgeschwindigkeit1CB; //Berechnung des maximal möglichen ContourBank-Abflusses pro Zeitschritt in l bei konstanter Fliessgeschwindigkeit und konstantem Fliessquerschnitt

        if (querschnittCB_akt > querschnittCB_max) {
            //Normalerweise muesste diese Schleife iterativ gerechnet werden, da querschnittCB_akt durch fliessgeschwindigkeit1CBakt beeinflusst wird
            //und dadurch der oberflaechenabfluss1CB_akt entsteht. Dieser beeintraechtigt dann aber wieder fliessgeschwindigkeit1CBakt usw.

            oberflaechenabfluss1CB_akt = 1000 * faktorZeit * (querschnittCB_akt - querschnittCB_max) * fliessgeschwindigkeit1CB; //Berechnung des neuen outRD1 in l pro Zeitschritt
            querschnittCB_akt = querschnittCB_max;
        }

        double[] benetzterUnfangUndWasserstandCB_Array = this.calcBenetzterUmfangUndWasserstandCB(HRUsSlope, boeschungswinkelWall_hinten, tiefeGraben_vorn, breiteGraben, querschnittGraben_unten, querschnittGraben_oben, querschnittCB_akt);
        double tiefeGraben_hinten = benetzterUnfangUndWasserstandCB_Array[0];
        double wasserstandCB_akt = benetzterUnfangUndWasserstandCB_Array[1];
        double benetzterUmfangCB = benetzterUnfangUndWasserstandCB_Array[2];
        double benetzterUmfangGraben = benetzterUnfangUndWasserstandCB_Array[3];

        double hydraulischerRadiusGraben = querschnittCB_akt / benetzterUmfangCB;

        boolean weiter = true;
        while (weiter == true) {
            fliessgeschwindigkeit1CB_temp = rauhigkeitsbeiwertGraben * Math.pow(hydraulischerRadiusGraben, 2.0 / 3.0) * Math.pow(slopeGraben, 0.5);

            if ((Math.abs(fliessgeschwindigkeit1CB_temp - fliessgeschwindigkeit1CB)) > 0.001) {
                fliessgeschwindigkeit1CB = fliessgeschwindigkeit1CB_temp;
                querschnittCB_akt = q_m / fliessgeschwindigkeit1CB;

                speicher1CB_max = 1000 * faktorZeit * querschnittCB_max * fliessgeschwindigkeit1CB;

                if (querschnittCB_akt > querschnittCB_max) {
                    oberflaechenabfluss1CB_akt = 1000 * faktorZeit * (querschnittCB_akt - querschnittCB_max) * fliessgeschwindigkeit1CB;
                    querschnittCB_akt = querschnittCB_max;
                }

                benetzterUnfangUndWasserstandCB_Array = this.calcBenetzterUmfangUndWasserstandCB(HRUsSlope, boeschungswinkelWall_hinten, tiefeGraben_vorn, breiteGraben, querschnittGraben_unten, querschnittGraben_oben, querschnittCB_akt);
                tiefeGraben_hinten = benetzterUnfangUndWasserstandCB_Array[0];
                wasserstandCB_akt = benetzterUnfangUndWasserstandCB_Array[1];
                benetzterUmfangCB = benetzterUnfangUndWasserstandCB_Array[2];
                benetzterUmfangGraben = benetzterUnfangUndWasserstandCB_Array[3];

            } else {
                weiter = false;
                fliessgeschwindigkeit1CB = fliessgeschwindigkeit1CB_temp;
            }
        }

        double[] calcFliessgeschwindigkeitCBaktArray = {querschnittCB_akt, tiefeGraben_hinten, wasserstandCB_akt, benetzterUmfangCB, benetzterUmfangGraben, fliessgeschwindigkeit1CB, oberflaechenabfluss1CB_akt, speicher1CB_max};
        return calcFliessgeschwindigkeitCBaktArray;
    }

    //Berechnung der aktuellen Wassertiefe und des benetzten Umfangs in der ContourBank in Abhaengigkeit des jeweiligen Fliessquerschnitts
    private double[] calcBenetzterUmfangUndWasserstandCB(double HRUsSlope, double boeschungswinkelWall_hinten, double tiefeGraben_vorn, double breiteGraben, double querschnittGraben_unten, double querschnittGraben_oben, double querschnittCB_akt) {

        double tiefeGraben_hinten = tiefeGraben_vorn + breiteGraben * Math.tan(Math.toRadians(HRUsSlope));
        double wasserstandCB_akt, benetzterUmfangCB, benetzterUmfangGraben;

        if (querschnittCB_akt <= querschnittGraben_unten) {
            wasserstandCB_akt = querschnittCB_akt / breiteGraben;
            benetzterUmfangCB = 2 * wasserstandCB_akt + breiteGraben;
            benetzterUmfangGraben = benetzterUmfangCB;
        } else if (querschnittCB_akt <= querschnittGraben_oben) {
            //Loesung ueber 2. binomische Formel
            double s = 0.5 * Math.tan(Math.toRadians(90 - boeschungswinkelWall_hinten));
            double p = breiteGraben / s;
            double q = (querschnittCB_akt - querschnittGraben_unten) / s;
            double wurzel = Math.sqrt(q + Math.pow(0.5 * p, 2));

            wasserstandCB_akt = -0.5 * p + wurzel + tiefeGraben_vorn;
            benetzterUmfangCB = (wasserstandCB_akt - tiefeGraben_vorn) / Math.sin(Math.toRadians(boeschungswinkelWall_hinten)) + tiefeGraben_vorn + breiteGraben + wasserstandCB_akt;
            benetzterUmfangGraben = tiefeGraben_vorn + breiteGraben + wasserstandCB_akt;
        } else {
            double s = Math.tan(Math.toRadians(90 - boeschungswinkelWall_hinten)) + 1 / Math.tan(Math.toRadians(HRUsSlope));
            double p = 2 * breiteGraben / s;
            double q = 2 * (querschnittCB_akt - querschnittGraben_oben) / s;
            double wurzel = Math.sqrt(q + Math.pow(0.5 * p, 2));

            wasserstandCB_akt = -0.5 * p + wurzel + tiefeGraben_hinten;
            benetzterUmfangCB = (wasserstandCB_akt - tiefeGraben_vorn) / Math.sin(Math.toRadians(boeschungswinkelWall_hinten)) + tiefeGraben_vorn + breiteGraben + tiefeGraben_hinten + (wasserstandCB_akt - tiefeGraben_hinten) / Math.sin(Math.toRadians(HRUsSlope));
            benetzterUmfangGraben = tiefeGraben_vorn + breiteGraben + tiefeGraben_hinten;
        }

        benetzterUmfangCB = 0.001 * Math.round(benetzterUmfangCB * 1000);
        benetzterUmfangGraben = 0.001 * Math.round(benetzterUmfangGraben * 1000);

        double[] benetzterUmfangUndWasserstand_Array = {tiefeGraben_hinten, wasserstandCB_akt, benetzterUmfangCB, benetzterUmfangGraben};
        return benetzterUmfangUndWasserstand_Array;
    }

    //die jeweilige Schicht ist durch die ContourBank vollstaendig durchtrennt, Infiltration erfolgt nur hangabwaerts
    private double calcBenetzterUmfangSchichtenDurchbrochen(double HRUsSlope, double tiefeGraben_vorn, double maechtigkeitSchichtenGesamt_senkrecht_m, double wasserstandCB_akt, double maechtigkeitOberschichten_senkrecht_m) {

        double benetzterUmfang_max = 0, benetzterUmfang_akt = 0;

        if (tiefeGraben_vorn >= maechtigkeitSchichtenGesamt_senkrecht_m) {
            benetzterUmfang_max = maechtigkeitSchichtenGesamt_senkrecht_m - maechtigkeitOberschichten_senkrecht_m;
        } else if (tiefeGraben_vorn < maechtigkeitSchichtenGesamt_senkrecht_m && tiefeGraben_vorn > maechtigkeitOberschichten_senkrecht_m) {
            benetzterUmfang_max = (tiefeGraben_vorn - maechtigkeitOberschichten_senkrecht_m) + (maechtigkeitSchichtenGesamt_senkrecht_m - tiefeGraben_vorn) / Math.tan(Math.toRadians(HRUsSlope));
        } else if (tiefeGraben_vorn < maechtigkeitSchichtenGesamt_senkrecht_m && tiefeGraben_vorn <= maechtigkeitOberschichten_senkrecht_m) {
            benetzterUmfang_max = (maechtigkeitSchichtenGesamt_senkrecht_m - tiefeGraben_vorn) / Math.tan(Math.toRadians(HRUsSlope)) - (maechtigkeitOberschichten_senkrecht_m - tiefeGraben_vorn) / Math.tan(Math.toRadians(HRUsSlope));
        }

        if (tiefeGraben_vorn - wasserstandCB_akt <= maechtigkeitOberschichten_senkrecht_m || wasserstandCB_akt >= tiefeGraben_vorn) {
            benetzterUmfang_akt = benetzterUmfang_max;
        } else if (tiefeGraben_vorn - wasserstandCB_akt > maechtigkeitOberschichten_senkrecht_m && tiefeGraben_vorn - wasserstandCB_akt < maechtigkeitSchichtenGesamt_senkrecht_m) {
            benetzterUmfang_akt = benetzterUmfang_max - (tiefeGraben_vorn - wasserstandCB_akt - maechtigkeitOberschichten_senkrecht_m);
        } else if (tiefeGraben_vorn - wasserstandCB_akt >= maechtigkeitSchichtenGesamt_senkrecht_m) {
            benetzterUmfang_akt = 0;
        }

        if (wasserstandCB_akt == 0) {
            benetzterUmfang_akt = 0;
        }

        benetzterUmfang_akt = 0.001 * Math.round(benetzterUmfang_akt * 1000);
        return benetzterUmfang_akt;
    }

    //die jeweilige Schicht ist durch die ContourBank nicht vollstaendig durchtrennt, Infiltration ist auch nach hinten moeglich
    private double calcBenetzterUmfangSchichtenNichtDurchbrochen(double HRUsSlope, double tiefeGraben_vorn, double breiteGraben, double tiefeGraben_hinten, double wasserstandCB_akt, double maechtigkeitOberschichten_senkrecht_m) {

        double benetzterUmfang_max = 0, benetzterUmfang_akt = 0;

        if (tiefeGraben_vorn >= maechtigkeitOberschichten_senkrecht_m) {
            benetzterUmfang_max = (tiefeGraben_vorn - maechtigkeitOberschichten_senkrecht_m) + breiteGraben + (tiefeGraben_hinten - maechtigkeitOberschichten_senkrecht_m);
        } else if (tiefeGraben_vorn < maechtigkeitOberschichten_senkrecht_m && tiefeGraben_hinten > maechtigkeitOberschichten_senkrecht_m) {
            benetzterUmfang_max = breiteGraben - (maechtigkeitOberschichten_senkrecht_m - tiefeGraben_vorn) / Math.tan(Math.toRadians(HRUsSlope)) + (tiefeGraben_hinten - maechtigkeitOberschichten_senkrecht_m);
        } else if (tiefeGraben_hinten <= maechtigkeitOberschichten_senkrecht_m) {
            benetzterUmfang_max = 0;
        }

        if (tiefeGraben_hinten - wasserstandCB_akt <= maechtigkeitOberschichten_senkrecht_m || wasserstandCB_akt >= tiefeGraben_hinten) {
            benetzterUmfang_akt = benetzterUmfang_max;
        } else if (tiefeGraben_vorn - wasserstandCB_akt < maechtigkeitOberschichten_senkrecht_m && tiefeGraben_hinten - wasserstandCB_akt > maechtigkeitOberschichten_senkrecht_m) {
            benetzterUmfang_akt = benetzterUmfang_max - (tiefeGraben_hinten - wasserstandCB_akt - maechtigkeitOberschichten_senkrecht_m);
        } else if (tiefeGraben_vorn - wasserstandCB_akt >= maechtigkeitOberschichten_senkrecht_m) {
            benetzterUmfang_akt = 2 * wasserstandCB_akt + breiteGraben;
        }

        if (wasserstandCB_akt == 0) {
            benetzterUmfang_akt = 0;
        }

        benetzterUmfang_akt = 0.001 * Math.round(benetzterUmfang_akt * 1000);
        return benetzterUmfang_akt;
    }

    //Zufluss aus RD2 in die ContourBank
    private double[] calcZufluss(boolean zuflussMgesSchichtAktiv, double kalibZufluss, double tiefeGraben_hinten, double run_outSchicht, double maechtigkeitOberschichten_senkrecht_m, double maechtigkeitSchichtenGesamt_senkrecht_m, double gesaettigteMaechtigkeitSchicht_vorn_m, double wasserstandCB_akt, double durchbrochenSchicht) {

        double rueckhaltekoeffizientSchicht;
        double faktorZuflussSchicht = 0, zuflussSchicht_akt = 0;
        double gradientSchicht_nach_hinten = ((maechtigkeitSchichtenGesamt_senkrecht_m - maechtigkeitOberschichten_senkrecht_m) - gesaettigteMaechtigkeitSchicht_vorn_m) - ((tiefeGraben_hinten - maechtigkeitOberschichten_senkrecht_m) - wasserstandCB_akt);

        if (gradientSchicht_nach_hinten < 0) {

            if (zuflussMgesSchichtAktiv == true && gesaettigteMaechtigkeitSchicht_vorn_m > 0) {
                faktorZuflussSchicht = ((tiefeGraben_hinten - maechtigkeitOberschichten_senkrecht_m) - ((maechtigkeitSchichtenGesamt_senkrecht_m - maechtigkeitOberschichten_senkrecht_m) - gesaettigteMaechtigkeitSchicht_vorn_m)) / gesaettigteMaechtigkeitSchicht_vorn_m; //Der Anteil der gesaettigten Maechtigkeit, der in den CB-Graben entwaessert
            }

            if (zuflussMgesSchichtAktiv == false) {
                faktorZuflussSchicht = (tiefeGraben_hinten - maechtigkeitOberschichten_senkrecht_m) / (maechtigkeitSchichtenGesamt_senkrecht_m - maechtigkeitOberschichten_senkrecht_m);
            }

            if (faktorZuflussSchicht < 0) {
                faktorZuflussSchicht = 0;
            } else if (faktorZuflussSchicht > 1) {
                faktorZuflussSchicht = 1;
            }

            rueckhaltekoeffizientSchicht = 10 * kalibZufluss * Math.abs(gradientSchicht_nach_hinten);

            if (rueckhaltekoeffizientSchicht > 0) {
                zuflussSchicht_akt = run_outSchicht * faktorZuflussSchicht * Math.exp(-1 / rueckhaltekoeffizientSchicht);
            }
            if (durchbrochenSchicht == 1) {
                zuflussSchicht_akt = run_outSchicht;
            }
        }

        double[] zuflussSchicht_Array = {gradientSchicht_nach_hinten, zuflussSchicht_akt};
        return zuflussSchicht_Array;
    }

    //Infiltration aus der ContourBank in RD2
    private double calcInfiltration(double kalibInfiltration, double tiefeGraben_vorn, double kfSchicht_m_s, double maechtigkeitSchichtenGesamt_senkrecht_m, double gesaettigteMaechtigkeitSchicht_hinten_m, double wasserstandCB_akt, double gradientSchicht_nach_hinten, double durchbrochenSchicht, double speicherCB_akt, double benetzterUmfangGraben_akt, double benetzterUmfangSchicht_akt) {

        double rueckhaltekoeffizientSchicht, gradientSchicht_nach_vorn;
        double infiltrationSchicht_akt = 0;
        double anteilBenetzterUmfangSchicht = 0;

        if (benetzterUmfangGraben_akt > 0) {
            anteilBenetzterUmfangSchicht = benetzterUmfangSchicht_akt / benetzterUmfangGraben_akt;
        }

        gradientSchicht_nach_vorn = (maechtigkeitSchichtenGesamt_senkrecht_m - gesaettigteMaechtigkeitSchicht_hinten_m) - (tiefeGraben_vorn - wasserstandCB_akt);

        if (gradientSchicht_nach_vorn > 0) {
            rueckhaltekoeffizientSchicht = 10 * kalibInfiltration * 86400 * kfSchicht_m_s * gradientSchicht_nach_vorn;

            if (rueckhaltekoeffizientSchicht > 0) {
                infiltrationSchicht_akt = speicherCB_akt * anteilBenetzterUmfangSchicht * Math.exp(-1 / rueckhaltekoeffizientSchicht);
            }
        }

        if (durchbrochenSchicht == 0) {
            if (gradientSchicht_nach_hinten > 0) {
                rueckhaltekoeffizientSchicht = 10 * kalibInfiltration * 86400 * kfSchicht_m_s * gradientSchicht_nach_hinten;

                if (rueckhaltekoeffizientSchicht > 0) {
                    infiltrationSchicht_akt = speicherCB_akt * anteilBenetzterUmfangSchicht * Math.exp(-1 / rueckhaltekoeffizientSchicht);
                } else {
                    infiltrationSchicht_akt = 0;
                }
            } else if (gradientSchicht_nach_hinten < 0 || kalibInfiltration == 0) {
                infiltrationSchicht_akt = 0;
            }
        }

        return infiltrationSchicht_akt;
    }

    //Gesamtabfluss aus der ContourBank in das angeschlossene Fliessgewaessersegment
    private double calcAbflussCBReach(double kalibAbflussReach, double gesamtlaenge1CB, double speicher1CB_akt, double fliessgeschwindigkeitCB) {

        double rueckhaltekoeffizientReachCB;
        double abflussCBReach_akt = 0;

        rueckhaltekoeffizientReachCB = (fliessgeschwindigkeitCB / gesamtlaenge1CB) * kalibAbflussReach * 3600;

        if (rueckhaltekoeffizientReachCB > 0) {
            abflussCBReach_akt = speicher1CB_akt * Math.exp(-1 / rueckhaltekoeffizientReachCB);
        }

        return abflussCBReach_akt; //in l
    }

    //Berechnung der Verhaeltnisse der einzelnen Abflusskomponenten
    private double[] calcVerhaeltnisseCBKomponenten(double speicherCB_alt, double[] anteileKomponentenCBReach_Array, double zuflussRD1_akt, double zuflussRD2_akt, double zuflussRG1_akt) {

        double anteilRD1 = anteileKomponentenCBReach_Array[0];
        double anteilRD2 = anteileKomponentenCBReach_Array[1];
        double anteilRG1 = anteileKomponentenCBReach_Array[2];

        double speicherCB_nachZufluss = speicherCB_alt + zuflussRD1_akt + zuflussRD2_akt + zuflussRG1_akt;

        if (speicherCB_nachZufluss > 0) {
            anteilRD1 = (anteilRD1 * speicherCB_alt + zuflussRD1_akt) / speicherCB_nachZufluss;
            anteilRD2 = (anteilRD2 * speicherCB_alt + zuflussRD2_akt) / speicherCB_nachZufluss;
            anteilRG1 = (anteilRG1 * speicherCB_alt + zuflussRG1_akt) / speicherCB_nachZufluss;
        } else {
            anteilRD1 = 0;
            anteilRD2 = 0;
            anteilRG1 = 0;
        }

        anteileKomponentenCBReach_Array[0] = anteilRD1;
        anteileKomponentenCBReach_Array[1] = anteilRD2;
        anteileKomponentenCBReach_Array[2] = anteilRG1;

        return anteileKomponentenCBReach_Array;
    }
}