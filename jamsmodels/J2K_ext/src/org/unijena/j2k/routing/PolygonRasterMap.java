/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unijena.j2k.routing;

import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author christian
 */
public class PolygonRasterMap{
        int polygonID = 0;
        ArrayList<Point> rasterIDs = new ArrayList<Point>();                
        
        double RD1hru,RD2hru,RG1hru,RG2hru;
        double storage = 0;
        
        public PolygonRasterMap(int id){
            polygonID = id;
        }
        
        public void add(Point p){
            rasterIDs.add(p);
        }
    }
