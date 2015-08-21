package org.unijena.j2k.routing;

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.awt.Point;
import java.util.HashMap;

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
public class RasterToReach extends JAMSComponent {

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
    public Attribute.Double reachID;
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
    public Attribute.Double totalToReach;

    double value = 0;
    
    public double Raster_to_Reach(PolygonRasterMap map, double[][] fuellstand_raster, int aufloesung) {
        map.storage = 0;
        for (Point p : map.rasterIDs){
            map.storage += fuellstand_raster[p.x][p.y];            
            value+= fuellstand_raster[p.x][p.y];
            fuellstand_raster[p.x][p.y] = 0;            
        }
        return map.storage;        
    }
    
    @SuppressWarnings("unchecked")
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        double ID = reachID.getValue();
        value = totalToReach.getValue();

        double[][] RD1raster = null;
        double[][] RD2raster = null;
        double[][] RG1raster = null;
        double[][] RG2raster = null;

        double inRD1 = this.inRD1.getValue();
        double inRD2 = this.inRD2.getValue();
        double inRG1 = this.inRG1.getValue();
        double inRG2 = this.inRG2.getValue();


        HashMap<Integer,PolygonRasterMap> actReachList = new HashMap<Integer,PolygonRasterMap>();

        try {
            actReachList = (HashMap<Integer,PolygonRasterMap>) information.getObject("reachlist");
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        try {
            RD1raster = (double[][]) fuellstand.getObject("RD1_raster");
            RD2raster = (double[][]) fuellstand.getObject("RD2_raster");
            RG1raster = (double[][]) fuellstand.getObject("RG1_raster");
            RG2raster = (double[][]) fuellstand.getObject("RG2_raster");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        PolygonRasterMap map = actReachList.get((int)ID);
        inRD1 += Raster_to_Reach(map, RD1raster, 1);
        inRD2 += Raster_to_Reach(map, RD2raster, 1);
        inRG1 += Raster_to_Reach(map, RG1raster, 1);
        inRG2 += Raster_to_Reach(map, RG2raster, 1);

        totalToReach.setValue(value);
        
        this.inRD1.setValue(inRD1);
        this.inRD2.setValue(inRD2);
        this.inRG1.setValue(inRG1);
        this.inRG2.setValue(inRG2);

    }
}
