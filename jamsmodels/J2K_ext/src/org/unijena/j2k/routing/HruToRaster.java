/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unijena.j2k.routing;

import jams.data.*;
import jams.data.Attribute.Entity;
import jams.model.*;
import java.util.ArrayList;
import java.awt.Point;
import java.util.HashMap;
import java.util.TreeMap;

@JAMSComponentDescription(
        title = "J2KRasterRouting",
author = "Christin Michel",
description = "")
/**
 *
 * @author sa63kul
 */
public class HruToRaster extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "Reach statevar RD1 outflow")
    public Attribute.Double outRD1;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "Reach statevar RD2 outflow")
    public Attribute.Double outRD2;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "Reach statevar RG1 outflow")
    public Attribute.Double outRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "Reach statevar RG2 outflow")
    public Attribute.Double outRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "test variable",
    defaultValue = "0")
    public Attribute.Double totalToRaster;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
    description = "Reach statevar RG2 storage")
    public Attribute.Double hruID;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "")
    public Attribute.Entity information;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
    description = "")
    public Attribute.Entity fuellstand;
    
    double value;
    
    public void HRU_to_Raster(PolygonRasterMap map, double wert, double[][] fuellstand, double[][] verteilung) {
        
        ArrayList<Point> list = map.rasterIDs;        
                
        double durchschnitt = wert / (double)list.size();
        
        for (Point p : list) {            
            fuellstand[p.x][p.y] = durchschnitt;
            value += durchschnitt;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        //Variablendeklaration
        double ID = hruID.getValue();
        value = totalToRaster.getValue();
        
        
        double[][] RD1raster = null;
        double[][] RD2raster = null;
        double[][] RG1raster = null;
        double[][] RG2raster = null;

        //Verteilungsraster -  merkt sich das Verteilungsmuster
        double[][] RD1_vraster = null;
        double[][] RD2_vraster = null;
        double[][] RG1_vraster = null;
        double[][] RG2_vraster = null;

        HashMap<Integer,PolygonRasterMap> acthrulist = new HashMap<Integer,PolygonRasterMap>();

        try {
            acthrulist = (HashMap<Integer,PolygonRasterMap>) information.getObject("hrulist");
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        //Bestimmen in welche Rasterzelle
        //die jeweilige Rasterzelle entwaessert (in Abhaengigkeit von der Hangneigung)
        try {            
            RD1raster = (double[][]) fuellstand.getObject("RD1_raster");
            RD2raster = (double[][]) fuellstand.getObject("RD2_raster");
            RG1raster = (double[][]) fuellstand.getObject("RG1_raster");
            RG2raster = (double[][]) fuellstand.getObject("RG2_raster");
            
            RD1_vraster = (double[][]) information.getObject("RD1_vraster");
            RD2_vraster = (double[][]) information.getObject("RD1_vraster");
            RG1_vraster = (double[][]) information.getObject("RD1_vraster");
            RG2_vraster = (double[][]) information.getObject("RD1_vraster");
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        //HRUToRaster        
        PolygonRasterMap hruRasterMap = acthrulist.get((int)ID);
        
        double RD1out = outRD1.getValue();
        double RD2out = outRD2.getValue();
        double RG1out = outRG1.getValue();
        double RG2out = outRG2.getValue();
                                                   
        outRD1.setValue(0);
        outRD2.setValue(0);
        outRG1.setValue(0);
        outRG2.setValue(0);
        
        //Verteilen des Wassers auf das Raster
        HRU_to_Raster(hruRasterMap, RD1out, RD1raster, RD1_vraster);
        HRU_to_Raster(hruRasterMap, RD2out, RD2raster, RD2_vraster);
        HRU_to_Raster(hruRasterMap, RG1out, RG1raster, RG1_vraster);
        HRU_to_Raster(hruRasterMap, RG2out, RG2raster, RG2_vraster);

        totalToRaster.setValue(value);
    }
}
