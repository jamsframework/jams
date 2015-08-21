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
import jams.tools.FileTools;

/**
 *
 * @author S. Kralisch, f?r mehrdimensionale Topologie modifiziert von D.Varga und B.Pfennig
 * 09.10.2008
 */
public class MultiEntityReaderTS_old extends JAMSComponent {

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
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute containing the HRU identifiers",
    defaultValue = "ID")
    public Attribute.String hruIDAttribute;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute containing the reach identifiers",
    defaultValue = "ID")
    public Attribute.String reachIDAttribute;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the HRU to HRU relation in the input file",
    defaultValue = "to_poly")
    public Attribute.String hru2hruAttribute;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the HRU to reach relation in the input file",
    defaultValue = "to_reach")
    public Attribute.String hru2reachAttribute;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the reach to reach relation in the input file",
    defaultValue = "to_reach")
    public Attribute.String reach2reachAttribute;

    public void init() {
        /*
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
        getModel().getRuntime().println("Entities read successfull!", JAMS.VERBOSE);*/

        //read hru parameter
        hrus.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), hruFileName.getValue()), getModel()));

        //assign IDs to all hru entities
        for (Attribute.Entity e : hrus.getEntityArray()) {
            try {
                e.setId((long) e.getDouble(hruIDAttribute.getValue()));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K HRUu parameter file (" + hruFileName.getValue() + ")!");
            }
        }

        //read reach parameter
        reaches.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), reachFileName.getValue()), getModel()));

        //assign IDs to all reach entities
        for (Attribute.Entity e : reaches.getEntityArray()) {
            try {
                e.setId((long) e.getDouble(reachIDAttribute.getValue()));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K Reach parameter file (" + reachFileName.getValue() + ")!");
            }
        }

        //create object associations from id attributes for hrus and reaches
        createTopology();

        //create total order on hrus and reaches that allows processing them subsequently
        getModel().getRuntime().println("Create ordered hru-list", JAMS.VERBOSE);
        createOrderedList(hrus, hru2hruAttribute.getValue());
        getModel().getRuntime().println("HRU entities read successfully", JAMS.STANDARD);
        getModel().getRuntime().println("Create ordered reach-list", JAMS.VERBOSE);
        createOrderedList(reaches, reach2reachAttribute.getValue());
        getModel().getRuntime().println("Reach entities read successfully", JAMS.STANDARD);

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

        Attribute.Entity e, f, r, toPoly, toReach;

        ArrayList<Attribute.Entity> receiverHRUs;
        ArrayList<Attribute.Entity> receiverReaches;
        ArrayList<Double> receiverHRUsWeights;
        ArrayList<Double> receiverReachesWeights;
        ArrayList<Double> receiverArea;

       /* //put all entities into a HashMap with their ID as key
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            hruMap.put(e.getDouble("ID"), e);
        }

        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();
            reachMap.put(e.getDouble("ID"), e);
        }*/

        //put all entities into a HashMap with their ID as key
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            hruMap.put(e.getDouble(hruIDAttribute.getValue()), e);
        }
        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();
            reachMap.put(e.getDouble(reachIDAttribute.getValue()), e);
        }


        Attribute.Entity nullEntity = getModel().getRuntime().getDataFactory().createEntity();        
        hruMap.put(new Double(0), nullEntity);
        reachMap.put(new Double(0), nullEntity);
         //associate the hru entities with their downstream entity
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            toPoly = hruMap.get(e.getDouble(hru2hruAttribute.getValue()));
            toReach = reachMap.get(e.getDouble(hru2reachAttribute.getValue()));

            if ((toPoly == null) || (toReach == null)) {
                getModel().getRuntime().sendErrorMsg("Topological neighbour for HRU with ID "
                        + e.getId() + " could not be found. This may cause errors!");
            }

            e.setObject(hru2hruAttribute.getValue(), toPoly);
            e.setObject(hru2reachAttribute.getValue(), toReach);

        }


        //create empty entities, i.e. those that are linked to in case there is no linkage ;-)
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

                receiverHRUs = new ArrayList<Attribute.Entity>();
                receiverReaches = new ArrayList<Attribute.Entity>();
                receiverHRUsWeights = new ArrayList<Double>();
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

                    sumWeight = Math.round((sumWeight - doubleWeight) * 10000) / 10000;

                    if ((doubleID == 0 && doubleWeight != 0) || (doubleID != 0 && doubleWeight == 0)) {
                        getModel().getRuntime().sendHalt("No. of receivers and their weights do not match!");
                    }

                    //for receiver HRUs
                    if (doubleID > 0) {
                        f = hruMap.get(doubleID);
                        receiverHRUs.add(f);
                        receiverHRUsWeights.add(doubleWeight);
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
                Attribute.Entity[] to_hru_Array = receiverHRUs.toArray(new Attribute.Entity[receiverHRUs.size()]);
                Attribute.Entity[] to_reach_Array = receiverReaches.toArray(new Attribute.Entity[receiverReaches.size()]);
                Double[] to_hru_weights_Array = receiverHRUsWeights.toArray(new Double[receiverHRUsWeights.size()]);
                Double[] to_reach_weights_Array = receiverReachesWeights.toArray(new Double[receiverReachesWeights.size()]);
                Double[] to_hru_bfl_Array = receiverArea.toArray(new Double[receiverHRUsWeights.size()]);

                //creating new Objects for each entity
                e.setObject("to_poly", to_hru_Array);
                e.setObject("to_reach", to_reach_Array);
                e.setObject("to_poly_weights", to_hru_weights_Array);
                e.setObject("to_reach_weights", to_reach_weights_Array);
                e.setObject("bfl", to_hru_bfl_Array);

                sumWeight = 1;

                //next line
                s = reader1.readLine();
                t = reader2.readLine();
            }

        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }

        /*//associate the reach entities with their downstream entity
        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();
            e.setObject("to_reach", reachMap.get(e.getDouble("to-reach")));
        }*/


        //associate the reach entities with their downstream entity
        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();

            toReach = reachMap.get(e.getDouble(reach2reachAttribute.getValue()));

            if (toReach == null) {
                getModel().getRuntime().sendErrorMsg("Topological neighbour for reach with ID "
                        + e.getId() + " could not be found. This may cause errors!");
            }

            e.setObject(reach2reachAttribute.getValue(), toReach);
        }
    }

    protected void createOrderedList(Attribute.EntityCollection col, String asso) {

        Iterator<Attribute.Entity> entityIterator, entityIterator2;
        Attribute.Entity e = null, e_ziel, e_ziel_neu;
        ArrayList<Attribute.Entity> newList = new ArrayList<Attribute.Entity>();
        HashMap<Attribute.Entity, Integer> statusMap = new HashMap<Attribute.Entity, Integer>();
        HashMap<Attribute.Entity, Attribute.Entity> fromHruMap = new HashMap<Attribute.Entity, Attribute.Entity>();
        HashMap<Attribute.Entity, Integer> fromIMap = new HashMap<Attribute.Entity, Integer>();
        HashMap<Attribute.Entity, Integer> depthMap = new HashMap<Attribute.Entity, Integer>();
        Integer eDepth, fDepth;
        boolean aufloesbar = false, unaufloesbar = false, mapChanged;

        //Identifikation und Aufloesung von Zirkeln
        if ((asso.toString()).equals("to_poly")) {

            entityIterator = col.getEntities().iterator();

            marke:
            while (entityIterator.hasNext()) {
                if (aufloesbar == false) {
                    e = entityIterator.next();
                }

                aufloesbar = false;
                e_ziel_neu = e;
                //System.out.println("Untersuche HRU " + e.getDouble("ID") + " auf Zirkelbezuege");

                entityIterator2 = col.getEntities().iterator();
                while (entityIterator2.hasNext()) {
                    Attribute.Entity next = entityIterator2.next();
                    statusMap.put(next, new Integer(0)); //Status 0: Noch nicht markiert, Status 1: Markiert, Bearbeitung aber noch nicht gestartet, Status -2: Bearbeitung gestartet, aber ncch nicht abgeschlossen, STatus -3: Bearabeitung abgeschlossen
                    fromHruMap.put(next, null);
                    fromIMap.put(next, new Integer(0));
                }

                statusMap.put(e, new Integer(1));

                while (statusMap.get(e) != -3) {
                    e_ziel = e_ziel_neu;
                    statusMap.put(e_ziel, new Integer(-2));
                    Attribute.Entity[] e_ziel_to_hru = (Attribute.Entity[]) e_ziel.getObject("to_poly");

                    if (e_ziel_to_hru.length > 0) {

                        for (int i = 0; i < e_ziel_to_hru.length; i++) {

                            if (e_ziel_to_hru[i] != null) {
                                double bfl_weitergabe = 0;
                                Double[] e_ziel_to_hru_weights = (Double[]) e_ziel.getObject("to_poly_weights");
                                bfl_weitergabe = e_ziel.getDouble("BFl") * e_ziel_to_hru_weights[i];
                                Double[] e_ziel_to_hru_bfl = (Double[]) e_ziel.getObject("bfl");
                                e_ziel_to_hru_bfl[i] = bfl_weitergabe;
                                e_ziel.setObject("bfl", e_ziel_to_hru_bfl);

                                if (e == e_ziel_to_hru[i]) {
                                    // Identifikation der kleinsten Beitragenden Flaeche
                                    Attribute.Entity eZirkel = e_ziel, eBflMin = null;
                                    int iZirkel = i, iZirkelMin = -1, teilerMin = -1;
                                    double bflZirkel, bflZirkelMin = -1;

                                    while (eZirkel != null) {

                                        Attribute.Entity[] eZirkel_to_reach = (Attribute.Entity[]) eZirkel.getObject("to_reach");
                                        Attribute.Entity[] eZirkel_to_hru = (Attribute.Entity[]) eZirkel.getObject("to_poly");

                                        int eZirkel_to_hru_length = 0;
                                        for (int l = 0; l < eZirkel_to_hru.length; l++) {
                                            if (eZirkel_to_hru[l] != null) {
                                                eZirkel_to_hru_length += 1;
                                            }
                                        }
                                        if (eZirkel_to_hru_length + eZirkel_to_reach.length > 1) {
                                            Double[] eZirkel_to_hru_bfl = (Double[]) eZirkel.getObject("bfl");
                                            bflZirkel = eZirkel_to_hru_bfl[iZirkel];
                                            if (bflZirkelMin == -1 || bflZirkel < bflZirkelMin) {
                                                aufloesbar = true;
                                                bflZirkelMin = bflZirkel;
                                                eBflMin = eZirkel;
                                                iZirkelMin = iZirkel;
                                                teilerMin = eZirkel_to_hru_length;
                                            }
                                        }
                                        iZirkel = fromIMap.get(eZirkel);
                                        eZirkel = fromHruMap.get(eZirkel);
                                    }
                                    //Zirkelaufloesung
                                    if (eBflMin != null && iZirkelMin != -1 && teilerMin != -1) {

                                        Attribute.Entity[] eBflMin_to_reach = (Attribute.Entity[]) eBflMin.getObject("to_reach");
                                        Attribute.Entity[] eBflMin_to_hru = (Attribute.Entity[]) eBflMin.getObject("to_poly");
                                        Double[] eBflMin_to_reach_weights = (Double[]) eBflMin.getObject("to_reach_weights");
                                        Double[] eBflMin_to_hru_weights = (Double[]) eBflMin.getObject("to_poly_weights");

                                        double eBflMin_to_hruId = eBflMin_to_hru[iZirkelMin].getDouble("ID");

                                        if (eBflMin_to_reach.length > 0) {
                                            double eBflMin_to_reachId = eBflMin_to_reach[0].getDouble("ID");

                                            //Uebergabe des Wassers der zirkelausloesenden Flaeche an ggf. verknuepfte Fliessgewaesserabschnitte
                                            eBflMin_to_reach_weights[0] = eBflMin_to_reach_weights[0] + eBflMin_to_hru_weights[iZirkelMin];
                                            eBflMin.setObject("to_reach_weights", eBflMin_to_reach_weights);

                                            System.out.println("HRU " + eBflMin.getDouble("ID") + ": Unterbrechung der Fliessbeziehung zu HRU " + eBflMin_to_hruId + ". Umleitung in das Fliessgewaessersegment -" + eBflMin_to_reachId + "; Beitragende Flaeche: " + Math.round(eBflMin.getDouble("BFl") * eBflMin_to_hru_weights[iZirkelMin] / 1000.) / 1000. + " qkm");
                                        } else {
                                            //Uebergabe des Wassers der zirkelausloesenden Flaeche an die anderen Fliessbeziehungen der gleichen HRU
                                            for (int s = 0; s < eBflMin_to_hru.length; s++) {
                                                if (eBflMin_to_hru[s] != null && s != iZirkelMin) {
                                                    eBflMin_to_hru_weights[s] = eBflMin_to_hru_weights[s] + eBflMin_to_hru_weights[iZirkelMin] / (teilerMin - 1);
                                                }
                                            }
                                            System.out.println("HRU " + eBflMin.getDouble("ID") + ": Unterbrechung der Fliessbeziehung zu HRU " + eBflMin_to_hruId + ". Verteilung des Wassers auf alle anderen Fliessbeziehungen der HRU;" + " Beitragende Flaeche: " + Math.round(eBflMin.getDouble("BFl") * eBflMin_to_hru_weights[iZirkelMin] / 1000.) / 1000. + " qkm");
                                        }
                                        eBflMin_to_hru[iZirkelMin] = null;
                                        eBflMin.setObject("to_poly", eBflMin_to_hru);
                                        eBflMin_to_hru_weights[iZirkelMin] = null;
                                        eBflMin.setObject("to_poly_weights", eBflMin_to_hru_weights);
                                    } else {
                                        unaufloesbar = true;
                                        System.out.println("Nicht aufloesbarer Zirkel! Fliessbeziehung von HRU " + e_ziel.getDouble("ID") + " zu HRU " + e_ziel_to_hru[i].getDouble("ID") + " kann nicht unterbrochen werden.");
                                    }
                                    continue marke;
                                }
                                if (statusMap.get(e_ziel_to_hru[i]) == 0) {
                                    statusMap.put(e_ziel_to_hru[i], new Integer(1));
                                    fromHruMap.put(e_ziel_to_hru[i], e_ziel);
                                    fromIMap.put(e_ziel_to_hru[i], new Integer(i));
                                }
                            }
                        }
                        for (int i = 0; i < e_ziel_to_hru.length; i++) {
                            if (statusMap.get(e_ziel_to_hru[i]) != null && statusMap.get(e_ziel_to_hru[i]) == 1) {
                                e_ziel_neu = e_ziel_to_hru[i];
                            }
                        }
                    }
                    //Backtracking
                    int anzahl = 0;
                    for (int i = 0; i < e_ziel_to_hru.length; i++) {
                        if (statusMap.get(e_ziel_to_hru[i]) != null && statusMap.get(e_ziel_to_hru[i]) >= 0) {
                            anzahl += 1;
                        }
                    }
                    if (anzahl == 0) {
                        statusMap.put(e_ziel, new Integer(-3));
                        e_ziel_neu = fromHruMap.get(e_ziel);
                    }
                }
            }
            //Abbruch wegen nicht aufl?sbaren Zirkels
            if (unaufloesbar == true) {
                getModel().getRuntime().sendHalt("Nicht aufloesbare Zirkel in HRU-Muster");
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

                    Attribute.Entity[] e_ziel_to_hru;
                    e_ziel_to_hru = (Attribute.Entity[]) e.getObject(asso);

                    if (e_ziel_to_hru.length > 0) {

                        for (int i = 0; i < e_ziel_to_hru.length; i++) {

                            if (e_ziel_to_hru[i] != null) {
                                fDepth = depthMap.get(e_ziel_to_hru[i]);

                                if (fDepth.intValue() <= eDepth.intValue()) {
                                    depthMap.put(e_ziel_to_hru[i], new Integer(eDepth.intValue() + 1));
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
