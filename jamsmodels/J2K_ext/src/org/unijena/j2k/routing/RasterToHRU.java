package org.unijena.j2k.routing;

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.awt.Point;
import java.util.HashMap;
import java.util.TreeMap;

/**
 *
 * @author sa63kul
 */
@JAMSComponentDescription(
        title = "J2KRasterRouting",
author = "Christin Michel",
description = "Wandelt Rasterebene wieder in HRU-Aufl?sung um")
/**
 *
 * @author sa63kul
 */
public class RasterToHRU extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "")
    public Attribute.Entity information;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "")
    public Attribute.Entity fuellstand;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
    description = "Reach statevar RG2 storage")
    public Attribute.Double hruID;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
    description = "Reach statevar RD1 storage")
    public Attribute.Double actRD1;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
    description = "Reach statevar RD2 storage")
    public Attribute.Double actRD2;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
    description = "Reach statevar RG1 storage")
    public Attribute.Double actRG1;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
    description = "Reach statevar RG2 storage")
    public Attribute.Double actRG2;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
    description = "Reach statevar RD1 storage")
    public Attribute.Double inRD1;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
    description = "Reach statevar RD2 storage")
    public Attribute.Double inRD2;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
    description = "Reach statevar RG1 storage")
    public Attribute.Double inRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
    description = "Reach statevar RG2 storage")
    public Attribute.Double inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
    description = "Reach statevar RG2 storage",
    defaultValue = "0")
    public Attribute.Double totalToHRU;

    double value = 0;
    
    public double Raster_to_HRU(PolygonRasterMap map, double[][] fuellstand_raster, int[][] flgew1) {        
        ArrayList<Point> list = map.rasterIDs;
        
        //Fuellstand auf null setzen
        map.storage = 0;
        for (Point p :list) {            
            if (flgew1[p.x][p.y] == -1) {
                map.storage += fuellstand_raster[p.x][p.y];
                value += fuellstand_raster[p.x][p.y];
                fuellstand_raster[p.x][p.y] = 0;
            }
        }
        
        return map.storage;

    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        double ID = hruID.getValue();
        value = totalToHRU.getValue();
        double[][] RD1raster = null;
        double[][] RD2raster = null;
        double[][] RG1raster = null;
        double[][] RG2raster = null;
        int[][] actflgew1 = null;
        
        //Verteilungsraster -  merkt sich das Verteilungsmuster
        double[][] RD1_vraster = null;
        double[][] RD2_vraster = null;
        double[][] RG1_vraster = null;
        double[][] RG2_vraster = null;


        HashMap<Integer,PolygonRasterMap> acthrulist = new HashMap<Integer,PolygonRasterMap>();
        
        try {
            acthrulist = (HashMap<Integer,PolygonRasterMap>) information.getObject("hrulist");
            RD1raster = (double[][]) fuellstand.getObject("RD1_raster");
            RD2raster = (double[][]) fuellstand.getObject("RD2_raster");
            RG1raster = (double[][]) fuellstand.getObject("RG1_raster");
            RG2raster = (double[][]) fuellstand.getObject("RG2_raster");            
            actflgew1 = (int[][]) information.getObject("flgew1");
            RD1_vraster = (double[][]) information.getObject("RD1_vraster");
            RD2_vraster = (double[][]) information.getObject("RD2_vraster");
            RG1_vraster = (double[][]) information.getObject("RG1_vraster");
            RG2_vraster = (double[][]) information.getObject("RG2_vraster");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        
        PolygonRasterMap hruRasterMap = acthrulist.get((int)ID);
        
        double RD1act = inRD1.getValue() + Raster_to_HRU(hruRasterMap, RD1raster, actflgew1);
        double RD2act = inRD2.getValue() + Raster_to_HRU(hruRasterMap, RD2raster, actflgew1);
        double RG1act = inRG1.getValue() + Raster_to_HRU(hruRasterMap, RG1raster, actflgew1);
        double RG2act = inRG2.getValue() + Raster_to_HRU(hruRasterMap, RG2raster, actflgew1);

        information.setObject("RD1_vraster", RD1_vraster);
        information.setObject("RD2_vraster", RD2_vraster);
        information.setObject("RG1_vraster", RG1_vraster);
        information.setObject("RG2_vraster", RG2_vraster);

        //Verzoegerung des Abflusses um einen Tag
        inRD1.setValue(RD1act);
        inRD2.setValue(RD2act);
        inRG1.setValue(RG1act);
        inRG2.setValue(RG2act);
        
        totalToHRU.setValue(value);
        //Erstellen der Verteilungsmusters innerhalb jeder HRU        
        /*double summe1 = 0;
        double summe2 = 0;
        double summe3 = 0;
        double summe4 = 0;
                        
        int laenge1, laenge2;
        double fuellstand = 0;
        Point p = new Point();
        laenge1 = acthrulist.size();
        for (int j = 0; j < laenge1; j++) {
            p = (Point) ((ArrayList) acthrulist.get(j)).get(0);//sucht ID der HRU
            if (p.x == ID) {

                laenge2 = ((ArrayList) acthrulist.get(j)).size();

                for (int i = 1; i < laenge2; i++) {
                    p = (Point) ((ArrayList) acthrulist.get(j)).get(i);
                    if (actflgew1[p.x][p.y] == -1) {
                        summe1 = summe1 + RD1raster[p.x][p.y];
                        summe2 = summe2 + RD2raster[p.x][p.y];
                        summe3 = summe3 + RG1raster[p.x][p.y];
                        summe4 = summe4 + RG2raster[p.x][p.y];
                    }
                }
                for (int i = 1; i < laenge2; i++) {
                    p = (Point) ((ArrayList) acthrulist.get(j)).get(i);
                    if (actflgew1[p.x][p.y] == -1) {
                        if (summe1 != 0) {
                            RD1_vraster[p.x][p.y] = RD1raster[p.x][p.y] / summe1;
                        } else {
                            RD1_vraster[p.x][p.y] = 1 / laenge2; //gleichmaessig verteilen
                        }
                        if (summe2 != 0) {
                            RD2_vraster[p.x][p.y] = RD2raster[p.x][p.y] / summe2;
                        } else {
                            RD2_vraster[p.x][p.y] = 1 / laenge2; //gleichmaessig verteilen
                        }
                        if (summe3 != 0) {
                            RG1_vraster[p.x][p.y] = RG1raster[p.x][p.y] / summe3;
                        } else {
                            RG1_vraster[p.x][p.y] = 1 / laenge2; //gleichmaessig verteilen
                        }
                        if (summe4 != 0) {
                            RG2_vraster[p.x][p.y] = RG2raster[p.x][p.y] / summe4;

                        } else {
                            RG2_vraster[p.x][p.y] = 1 / laenge2; //gleichmaessig verteilen
                        }
                    }
                }

                j = laenge1;
            }
        }*/
    }
}
