/*
 * WasserfallEntityReader.java
 *
 * Created on 5. Juni 2008, 14:22
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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
package org.unijena.j2k.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.unijena.j2k.*;
import jams.data.*;
import jams.model.*;
import java.util.*;
import jams.JAMS;
import java.lang.Math.*;
import jams.tools.JAMSTools;

/**
 *
 * @author S. Kralisch, f?r mehrdimensionale Topologie modifiziert von D.Varga und B.Pfennig
 * 09.10.2008
 */
public class MultiEntityReaderBS extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU parameter file name")
    public Attribute.String hruFileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Reach parameter file name")
    public Attribute.String reachFileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Parameter file name for topological linkage with receiver entities")
    public Attribute.String to_hru_FileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Parameter file name for weighting of receiver entity")
    public Attribute.String bfl_FileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Collection of hru objects")
    public Attribute.EntityCollection hrus;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Collection of reach objects")
    public Attribute.EntityCollection reaches;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Collection of hru objects with their topology")
    public Attribute.EntityCollection topology;

    public void init() {

        //read hru parameter
        hrus.setEntities(J2KFunctions.readParas(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), hruFileName.getValue()), getModel()));

        //read reach parameter
        reaches.setEntities(J2KFunctions.readParas(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), reachFileName.getValue()), getModel()));

        //create object associations from id attributes for hrus and reaches
        createTopology();

        //create total order on hrus and reaches that allows processing them subsequently
        getModel().getRuntime().println("Create ordered hru-list", JAMS.VERBOSE);
        createOrderedList(hrus, "to_poly");
        getModel().getRuntime().println("Create ordered reach-list", JAMS.VERBOSE);
        createOrderedList(reaches, "to_reach");
        getModel().getRuntime().println("Entities read successfull!", JAMS.VERBOSE);

    }

    private void createTopology() {

        BufferedReader reader1;
        BufferedReader reader2;
        StringTokenizer tokenizer_to_hru;
        StringTokenizer tokenizer_weights;
        HashMap<Double, Attribute.Entity> hruMap = new HashMap<Double, Attribute.Entity>();
        HashMap<Double, Attribute.Entity> reachMap = new HashMap<Double, Attribute.Entity>();
        Iterator<Attribute.Entity> hruIterator;
        Iterator<Attribute.Entity> reachIterator;

        Attribute.Entity e, f, r;

        ArrayList<Attribute.Entity> receiverPolys;
        ArrayList<Attribute.Entity> receiverReaches;
        ArrayList<Double> receiverPolysWeights;
        ArrayList<Double> receiverReachesWeights;
        ArrayList<Double> receiverArea;

        //put all entities into a HashMap with their ID as key
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            hruMap.put(e.getDouble("ID"), e);
        }

        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();
            reachMap.put(e.getDouble("ID"), e);
        }

        //create empty entities, i.e. those that are linked to in case there is no linkage ;-)
        Attribute.Entity nullEntity = getModel().getRuntime().getDataFactory().createEntity();
        reachMap.put(new Double(0), nullEntity);

        try {

            reader1 = new BufferedReader(new FileReader(getModel().getWorkspaceDirectory().getPath() + "/" + to_hru_FileName.getValue()));
            reader2 = new BufferedReader(new FileReader(getModel().getWorkspaceDirectory().getPath() + "/" + bfl_FileName.getValue()));

            String s = "#";
            while (s.startsWith("#")) {
                s = reader1.readLine();
            }

            String t = "#";
            while (t.startsWith("#")) {
                t = reader2.readLine();
            }

            while ((s != null) && !s.startsWith("#")) {

                tokenizer_to_hru = new StringTokenizer(s, "\t");
                double eID = Double.parseDouble(tokenizer_to_hru.nextToken());
                double eBFl = Double.parseDouble(tokenizer_to_hru.nextToken());
                String recIDs = tokenizer_to_hru.nextToken();
                e = hruMap.get(eID);

                e.setDouble("BFl", eBFl);

                tokenizer_weights = new StringTokenizer(t, "\t");
                double eID2 = Double.parseDouble(tokenizer_weights.nextToken());
                double eBFl2 = Double.parseDouble(tokenizer_weights.nextToken());
                String recWeights = tokenizer_weights.nextToken();

                //Checks if e1 and e2 are identical
                //Die Tabellen topologie_to_hru und topologie_bfl muessen identisch sortiert sein
                if (eID != eID2) {
                    getModel().getRuntime().sendHalt("One of tables topologie_to_hru or topologie_bfl is missorted");
                }

                receiverPolys = new ArrayList<Attribute.Entity>();
                receiverReaches = new ArrayList<Attribute.Entity>();
                receiverPolysWeights = new ArrayList<Double>();
                receiverReachesWeights = new ArrayList<Double>();
                receiverArea = new ArrayList<Double>();

                //Tokenizer for splitting on ","
                StringTokenizer sTok = new StringTokenizer(recIDs, ","); //Tokenizer for receiver entities
                StringTokenizer tTok = new StringTokenizer(recWeights, ","); //Tokenizer for receiver entities weights

                boolean tschuessnull = false;
                double sumWeight = 1;

                while (sTok.hasMoreTokens() && tschuessnull == false) {
                    String stringID = sTok.nextToken();
                    double doubleID = Double.parseDouble(stringID);
                    String stringWeight = tTok.nextToken();
                    double doubleWeight = Double.parseDouble(stringWeight);
                    //double doubleWeight = Math.round(Double.parseDouble(stringWeight)*10000)/10000;

                    sumWeight = Math.round((sumWeight - doubleWeight) * 10000) / 10000;

                    if ((doubleID == 0 && doubleWeight != 0) || (doubleID != 0 && doubleWeight == 0)) {
                        getModel().getRuntime().sendHalt("No. of receivers and their weights in HRU " + eID + " do not match!");
                    }

                    //for receiver HRUs
                    if (doubleID > 0) {
                        f = hruMap.get(doubleID);
                        receiverPolys.add(f);
                        receiverPolysWeights.add(doubleWeight);
                    }

                    //for receiver Reaches
                    if (doubleID < 0) {
                        double reachID = doubleID * (-1);
                        r = reachMap.get(reachID);
                        receiverReaches.add(r);
                        receiverReachesWeights.add(doubleWeight);
                    }

                    if (doubleID == 0) {
                        tschuessnull = true;
                    }
                }

                sumWeight = Math.abs(sumWeight);
                if (sumWeight >= 0.001) {
                    System.out.println("Error in processing entity with ID " + eID);
                    getModel().getRuntime().sendHalt("Sum of weights is not equal 1! Process entity:" + eID);
                }

                //converting the ArrayLists into Arrays
                Attribute.Entity[] to_poly_Array = receiverPolys.toArray(new Attribute.Entity[receiverPolys.size()]);
                Attribute.Entity[] to_reach_Array = receiverReaches.toArray(new Attribute.Entity[receiverReaches.size()]);
                Double[] to_poly_weights_Array = receiverPolysWeights.toArray(new Double[receiverPolysWeights.size()]);
                Double[] to_reach_weights_Array = receiverReachesWeights.toArray(new Double[receiverReachesWeights.size()]);
                Double[] to_poly_bfl_Array = receiverArea.toArray(new Double[receiverPolysWeights.size()]);

                //creating new Objects for each entity
                e.setObject("to_poly", to_poly_Array);
                e.setObject("to_reach", to_reach_Array);
                e.setObject("to_poly_weights", to_poly_weights_Array);
                e.setObject("to_reach_weights", to_reach_weights_Array);
                e.setObject("bfl_zirkel_betrag", to_poly_bfl_Array);

                sumWeight = 1;

                //next line
                s = reader1.readLine();
                t = reader2.readLine();
            }

        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }

        //associate the reach entities with their downstream entity
        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();
            e.setObject("to_reach", reachMap.get(e.getDouble("to-reach")));
        }
    }

    protected void createOrderedList(Attribute.EntityCollection col, String asso) {

        Iterator<Attribute.Entity> entityIterator, entityIterator2, entityIterator3;
        Attribute.Entity e, e_ziel, e_ziel_temp, e_temp, f;
        ArrayList<Attribute.Entity> newList = new ArrayList<Attribute.Entity>();
        HashMap<Attribute.Entity, Integer> depthMap = new HashMap<Attribute.Entity, Integer>();
        HashMap<Attribute.Entity, Integer> circleMap = new HashMap<Attribute.Entity, Integer>();
        Integer eDepth, fDepth;
        boolean mapChanged = true;
        boolean mapChanged2 = true;
        boolean gefunden = false;
        boolean zirkel = false;
        boolean unaufloesbar = false;
        int arrayfeld_min = 0;
        double bfl_zirkel = 0;
        double bfl_zirkel_temp = 0;
        double bfl_zirkel_min = 0;
        double umgelenkt_gesamt = 0;

        //Identifikation von Zirkeln
        if ((asso.toString()).equals("to_poly")) {

            entityIterator = col.getEntities().iterator();
            while (entityIterator.hasNext()) {

                mapChanged = true;

                entityIterator3 = col.getEntities().iterator();
                while (entityIterator3.hasNext()) {

                    circleMap.put(entityIterator3.next(), new Integer(0));
                }

                e = entityIterator.next();
                circleMap.put(e, new Integer(1));
                System.out.println("Untersuche HRU " + e.getDouble("ID") + " auf Zirkelbezuege");

                while (mapChanged) {

                    mapChanged = false;

                    entityIterator2 = col.getEntities().iterator();
                    while (entityIterator2.hasNext()) {

                        bfl_zirkel = 0;
                        e_ziel = entityIterator2.next();

                        if (circleMap.get(e_ziel) == 1) {

                            mapChanged = true;
                            circleMap.put(e_ziel, new Integer(2));

                            Attribute.Entity[] e_ziel_to_poly = (Attribute.Entity[]) e_ziel.getObject("to_poly");

                            if (e_ziel_to_poly.length > 0) {

                                for (int i = 0; i < e_ziel_to_poly.length; i++) {

                                    f = e_ziel_to_poly[i];

                                    if (f != null) {

                                        if (e.getDouble("ID") == f.getDouble("ID")) {

                                            zirkel = true;

                                            Double[] e_ziel_to_poly_weights = (Double[]) e_ziel.getObject("to_poly_weights");
                                            bfl_zirkel = e_ziel.getDouble("BFl") * e_ziel_to_poly_weights[i];
                                            bfl_zirkel_temp += bfl_zirkel;
                                            bfl_zirkel_min = bfl_zirkel_temp;

                                            //Es wird im Attribut to_zirkel_betrag vermerkt, wie gross die beitragende Flaeche ist, die den Zirkel schliesst
                                            Double[] e_ziel_to_poly_bfl = (Double[]) e_ziel.getObject("bfl_zirkel_betrag");
                                            e_ziel_to_poly_bfl[i] = bfl_zirkel;
                                            e_ziel.setObject("bfl_zirkel_betrag", e_ziel_to_poly_bfl);

                                        } else {

                                            if (circleMap.get(f) == 0) {

                                                circleMap.put(f, new Integer(1));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (zirkel) {

                while (mapChanged2) {

                    mapChanged2 = false;

                    e = null;
                    e_ziel_temp = null;

                    //Finden der kleinsten zirkelloesenden beitragenden Flaeche
                    entityIterator = col.getEntities().iterator();
                    while (entityIterator.hasNext()) {

                        e_temp = entityIterator.next();

                        Double[] e_temp_to_poly_bfl = (Double[]) e_temp.getObject("bfl_zirkel_betrag");

                        if (e_temp_to_poly_bfl.length > 0) {

                            for (int i = 0; i < e_temp_to_poly_bfl.length; i++) {

                                if (e_temp_to_poly_bfl[i] != null) {

                                    bfl_zirkel = e_temp_to_poly_bfl[i];

                                    if (bfl_zirkel > 0) {

                                        //Wird die naechste if-Anweisung ausgeklammert, erfolgt keine Suche nach der kleinsten zirkelloesenden beitragenden Flaeche, sondern es wird eine willkuerliche Auswahl vorgenommen
                                        if (bfl_zirkel <= bfl_zirkel_min) {

                                            bfl_zirkel_min = bfl_zirkel;

                                            Attribute.Entity[] e_temp_to_poly = (Attribute.Entity[]) e_temp.getObject("to_poly");
                                            e = e_temp_to_poly[i];
                                            e_ziel_temp = e_temp;
                                            arrayfeld_min = i;
                                            mapChanged2 = true;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (e != null) {

                        entityIterator3 = col.getEntities().iterator();
                        while (entityIterator3.hasNext()) {
                            circleMap.put(entityIterator3.next(), new Integer(0));
                        }

                        mapChanged = true;
                        gefunden = false;

                        circleMap.put(e, new Integer(1));

                        bfl_zirkel_min = bfl_zirkel_temp;

                        Double[] e_ziel_temp_to_poly_bfl = (Double[]) e_ziel_temp.getObject("bfl_zirkel_betrag");
                        e_ziel_temp_to_poly_bfl[arrayfeld_min] = null;
                        e_ziel_temp.setObject("bfl_zirkel_betrag", e_ziel_temp_to_poly_bfl);

                        //Zirkelaufloesung
                        while (mapChanged && gefunden == false) {

                            mapChanged = false;

                            entityIterator2 = col.getEntities().iterator();
                            while (entityIterator2.hasNext()) {

                                e_ziel = entityIterator2.next();

                                if (circleMap.get(e_ziel) == 1) {

                                    circleMap.put(e_ziel, new Integer(2));

                                    mapChanged = true;

                                    Attribute.Entity[] e_ziel_to_reach = (Attribute.Entity[]) e_ziel.getObject("to_reach");
                                    Attribute.Entity[] e_ziel_to_poly = (Attribute.Entity[]) e_ziel.getObject("to_poly");

                                    int arraylaenge_poly = 0;

                                    for (int i = 0; i < e_ziel_to_poly.length; i++) {

                                        if (e_ziel_to_poly[i] != null) {

                                            arraylaenge_poly += 1;
                                        }
                                    }

                                    if (e_ziel.getDouble("ID") == e_ziel_temp.getDouble("ID")) {

                                        gefunden = true;

                                        if (arraylaenge_poly + e_ziel_to_reach.length > 1) {

                                            Double[] e_ziel_to_poly_weights = (Double[]) e_ziel.getObject("to_poly_weights");
                                            Double[] e_ziel_to_reach_weights = (Double[]) e_ziel.getObject("to_reach_weights");

                                            //Uebergabe des Wassers der zirkelausloesenden Flaeche an ggf. verknuepfte Fliessgewaesserabschnitte
                                            double e_ziel_to_poly_id = e_ziel_to_poly[arrayfeld_min].getDouble("ID");

                                            if (e_ziel_to_reach.length > 0) {

                                                e_ziel_to_reach_weights[0] = e_ziel_to_reach_weights[0] + e_ziel_to_poly_weights[arrayfeld_min];
                                                e_ziel.setObject("to_reach_weights", e_ziel_to_reach_weights);

                                                double e_ziel_to_reach_id = e_ziel_to_reach[0].getDouble("ID");
                                                System.out.println("HRU " + e_ziel.getDouble("ID") + ": Unterbrechung der Fliessbeziehung zu HRU " + e_ziel_to_poly_id + ". Umleitung in das Fliessgewaessersegment -" + e_ziel_to_reach_id + "; Beitragende Flaeche: " + Math.round(e_ziel.getDouble("BFl") * e_ziel_to_poly_weights[arrayfeld_min] / 1000.) / 1000. + " qkm");

                                            } //Uebergabe des Wassers der zirkelausloesenden Flaeche an die anderen Fliessbeziehungen der gleichen HRU
                                            else if (arraylaenge_poly > 1) {

                                                int anzahl = 0;

                                                for (int i = 0; i < e_ziel_to_poly.length; i++) {

                                                    if (e_ziel_to_poly[i] != null && i != arrayfeld_min) {

                                                        anzahl += 1;
                                                    }
                                                }

                                                for (int i = 0; i < e_ziel_to_poly.length; i++) {

                                                    if (e_ziel_to_poly[i] != null && i != arrayfeld_min) {

                                                        e_ziel_to_poly_weights[i] = e_ziel_to_poly_weights[i] + e_ziel_to_poly_weights[arrayfeld_min] / anzahl;
                                                    }
                                                }

                                                System.out.println("HRU " + e_ziel.getDouble("ID") + ": Unterbrechung der Fliessbeziehung zu HRU " + e_ziel_to_poly_id + ". Verteilung des Wassers auf alle anderen Fliessbeziehungen der HRU;" + " Beitragende Flaeche: " + Math.round(e_ziel.getDouble("BFl") * e_ziel_to_poly_weights[arrayfeld_min] / 1000.) / 1000. + " qkm");

                                            }

                                            umgelenkt_gesamt += e_ziel.getDouble("BFl") * e_ziel_to_poly_weights[arrayfeld_min];

                                            e_ziel_to_poly[arrayfeld_min] = null;
                                            e_ziel_to_poly_weights[arrayfeld_min] = null;
                                            e_ziel.setObject("to_poly", e_ziel_to_poly);
                                            e_ziel.setObject("to_poly_weights", e_ziel_to_poly_weights);

                                        }
                                    } else if (arraylaenge_poly > 0) {

                                        for (int i = 0; i < e_ziel_to_poly.length; i++) {

                                            f = e_ziel_to_poly[i];

                                            if (f != null) {

                                                if (circleMap.get(f) == 0) {

                                                    circleMap.put(f, new Integer(1));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            //Abschliessende Kontrolle auf das Vorhandensein unaufloesbarer Zirkel
/*/                entityIterator = col.getEntities().iterator();
            while (entityIterator.hasNext()) {

            mapChanged = true;

            entityIterator3 = col.getEntities().iterator();
            while (entityIterator3.hasNext()) {

            circleMap.put(entityIterator3.next(), new Integer(0));
            }

            e = entityIterator.next();
            circleMap.put(e, new Integer(1));
            System.out.println("Analysing HRU " + e.getDouble("ID") + " for circle flows");

            while (mapChanged) {

            mapChanged = false;

            entityIterator2 = col.getEntities().iterator();
            while (entityIterator2.hasNext()) {

            e_ziel = entityIterator2.next();

            if (circleMap.get(e_ziel) == 1){

            circleMap.put(e_ziel, new Integer(2));

            mapChanged = true;

            Attribute.Entity[] e_ziel_to_poly = (Attribute.Entity[]) e_ziel.getObject("to_poly");

            if (e_ziel_to_poly.length > 0) {

            for (int i = 0; i < e_ziel_to_poly.length; i++){

            f = e_ziel_to_poly[i];

            if (f != null){

            if (e.getDouble("ID") == f.getDouble("ID")){

            System.out.println("Non dissolvable circle released by HRU " + e.getDouble("ID"));
            unaufloesbar = true;

            } else{

            if (circleMap.get(f) == 0){

            circleMap.put(f, new Integer(1));
            }
            }
            }
            }
            }
            }
            }
            }
            }
            if (unaufloesbar == true){

            getModel().getRuntime().sendHalt("Some non-dissolvable circle relations in HRU pattern");
            }/*/

            //System.out.println("Umgelenkte Flaeche: " + Math.round(umgelenkt_gesamt / 10000.) / 100. + " qkm");
            }
        }

        // Aufbau der Topologie
        mapChanged = true;
        entityIterator = col.getEntities().iterator();
        while (entityIterator.hasNext()) {
            depthMap.put(entityIterator.next(), new Integer(0));
        }

        //put all collection elements (keys) and their maximum depth (values) into a HashMap
        while (mapChanged) {
            mapChanged = false;
            entityIterator = col.getEntities().iterator();
            while (entityIterator.hasNext()) {
                e = entityIterator.next();
                eDepth = depthMap.get(e);

                if ((asso.toString()).equals("to_poly")) {

                    Attribute.Entity[] e_ziel_to_poly;
                    e_ziel_to_poly = (Attribute.Entity[]) e.getObject(asso);

                    if (e_ziel_to_poly.length > 0) {

                        for (int i = 0; i < e_ziel_to_poly.length; i++) {
                            f = e_ziel_to_poly[i];

                            if (f != null) {
                                fDepth = depthMap.get(f);

                                if (fDepth.intValue() <= eDepth.intValue()) {
                                    depthMap.put(f, new Integer(eDepth.intValue() + 1));
                                    mapChanged = true;
                                }
                            }
                        }
                    }
                }
                if ((asso.toString()).equals("to_reach")) {

                    Attribute.Entity eff;
                    eff = (Attribute.Entity) e.getObject(asso);
                    if (eff.isEmpty()) {
                        eff = null;
                    }

                    if (eff != null) {
                        fDepth = depthMap.get(eff);

                        if (fDepth.intValue() <= eDepth.intValue()) {
                            depthMap.put(eff, new Integer(eDepth.intValue() + 1));
                            mapChanged = true;
                        }
                    }
                }
            }
        }

        //find out which is the max depth of all entities
        int maxDepth = 0;
        entityIterator = col.getEntities().iterator();
        while (entityIterator.hasNext()) {
            e = entityIterator.next();
            maxDepth = Math.max(maxDepth, depthMap.get(e).intValue());
        }

        //create ArrayList of ArrayList objects, each element keeping the entities of one level
        ArrayList<ArrayList<Attribute.Entity>> alList = new ArrayList<ArrayList<Attribute.Entity>>();

        for (int i = 0; i <= maxDepth; i++) {
            alList.add(new ArrayList<Attribute.Entity>());
        }

        //fill the ArrayList objects within the ArrayList with entity objects
        entityIterator = col.getEntities().iterator();
        
        while (entityIterator.hasNext()) {
            e = entityIterator.next();
            int depth = depthMap.get(e).intValue();
            alList.get(depth).add(e);
        }

        //put the entities
        for (int i = 0; i <= maxDepth; i++) {
            entityIterator = alList.get(i).iterator();
            while (entityIterator.hasNext()) {
                e = entityIterator.next();
                newList.add(e);
            }
        }
        col.setEntities(newList);
    }
}
