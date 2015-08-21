/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unijena.j2k.routing;

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.awt.Point;
import java.util.TreeMap;

/**
 *
 * @author sa63kul
 */
@JAMSComponentDescription(
        title = "J2KRasterRouting",
author = "Christin Michel",
description = "")
/**
 *
 * @author sa63kul
 */
public class RasterRouting extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "")
    public Attribute.Entity information;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "")
    public Attribute.Entity fuellstand;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "Catchment outlet RG2 storage")
    public Attribute.Double catchmentSimRunoff;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "Reach statevar simulated Runoff")
    public Attribute.Double simRunoff;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "Reach statevar simulated Runoff")
    public Attribute.Double RD1_RR_Koeff;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "Reach statevar simulated Runoff")
    public Attribute.Double RD2_RR_Koeff;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "Reach statevar simulated Runoff")
    public Attribute.Double RG1_RR_Koeff;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "Reach statevar simulated Runoff")
    public Attribute.Double RG2_RR_Koeff;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "spatial grid resolution")
    public Attribute.Double gridresolution;

    double max=0;
    
    public void RasterRouting(double anteil, Point[][] routing, int dgm_sort[][], int[][] fliessrichtung, double[][] fuellstand, int flgew1[][], double aufloesung) {
        int zeile, spalte;
        //Abarbeitung der Ratserzellen von der tiefsten zur hoechsten
        for (int lauf = 0; lauf < dgm_sort[0].length; lauf++) {
            zeile = dgm_sort[1][lauf]; //Auslesen der x-Koordinate
            spalte = dgm_sort[2][lauf]; //Auslesen der Y-Koordinate

            //Zeile und Spalte sind vermutlich vertauscht
            //
            //  32   16  8
            //  64   X   4
            // 128   1   2

            //schaut, ob flgewpixel in rasterzelle liegt. dann wird hier nicht geroutet.
            if ((int) fuellstand[zeile][spalte] > 0 && fliessrichtung[zeile][spalte] > 0) {
                Point p = routing[zeile][spalte];
                //ist die Konstante Anteil gleich null, so wird kein Wasser weitergegeben
                //ist sie eins so wird alles Wasser in einem Zeitschritt weitergegeben
                if (flgew1[zeile][spalte] == -1) {
                    double tmp = fuellstand[zeile][spalte] * anteil;
                    fuellstand[zeile][spalte] -= tmp;
                    fuellstand[p.x][p.y] += tmp;
                }
                
                /*if (fuellstand[p.x][p.y] > max) {
                    max = fuellstand[p.x][p.y];
                    System.out.println("new maximum at " + p.x + "-" + p.y + " with " + max);
                }*/
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        double[][] RD1raster = null;
        double[][] RD2raster = null;
        double[][] RG1raster = null;
        double[][] RG2raster = null;

        int[][] actfliessrichtung = null;

        int[][] actflgew1 = null;
        int actdgm_sort[][] = null;//new int[3][actdgm.length * actdgm[0].length]; //Array, indem die H?henwerte sortiert gespeichert werden
        double aufloesung= gridresolution.getValue();

        try {
            RD1raster = (double[][]) fuellstand.getObject("RD1_raster");
            RD2raster = (double[][]) fuellstand.getObject("RD2_raster");
            RG1raster = (double[][]) fuellstand.getObject("RG1_raster");
            RG2raster = (double[][]) fuellstand.getObject("RG2_raster");
            actfliessrichtung = (int[][]) information.getObject("fliessrichtung");
            actdgm_sort = (int[][]) information.getObject("dgm_sort");
        } catch (Exception e) {            
            System.out.println(e.toString());
            e.printStackTrace();
        }

        //Bestimmen in welche Rasterzelle
        //die jeweilige Rasterzelle entwaessert (in Abhaengigkeit von der Hangneigung)
        Point[][] actroutingRD1 = null;
        Point[][] actroutingRD2 = null;
        Point[][] actroutingRG1 = null;
        Point[][] actroutingRG2 = null;

        try {
            actroutingRD1 = (Point[][]) information.getObject("routingRD1");
            actroutingRD2 = (Point[][]) information.getObject("routingRD2");
            actroutingRG1 = (Point[][]) information.getObject("routingRG1");
            actroutingRG2 = (Point[][]) information.getObject("routingRG2");
            actflgew1 = (int[][]) information.getObject("flgew1");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        
        double RD1_Koeff = RD1_RR_Koeff.getValue();
        double RD2_Koeff = RD2_RR_Koeff.getValue();
        double RG1_Koeff = RG1_RR_Koeff.getValue();
        double RG2_Koeff = RG2_RR_Koeff.getValue();

        RasterRouting(RD1_Koeff, actroutingRD1, actdgm_sort, actfliessrichtung, RD1raster, actflgew1, aufloesung);
        RasterRouting(RD2_Koeff, actroutingRD2, actdgm_sort, actfliessrichtung, RD2raster, actflgew1, aufloesung);
        RasterRouting(RG1_Koeff, actroutingRG1, actdgm_sort, actfliessrichtung, RG1raster, actflgew1, aufloesung);
        RasterRouting(RG2_Koeff, actroutingRG2, actdgm_sort, actfliessrichtung, RG2raster, actflgew1, aufloesung);

                        
        fuellstand.setObject("RD1_raster", RD1raster);
        fuellstand.setObject("RD2_raster", RD2raster);
        fuellstand.setObject("RG1_raster", RG1raster);
        fuellstand.setObject("RG2_raster", RG2raster);
    }
}
