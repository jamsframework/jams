/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.worldwind.test;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwindx.examples.util.ShapefileLoader;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class Shapefiles {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        //start("World Wind Shapefiles", AppFrame.class);
        new Shapefiles();
    }
    
    public Shapefiles() {
        Shapefile f = new Shapefile("/Users/bigr/Documents/BA-Arbeit/trunk/JAMSworldwind/shapefiles/J2000_Dudh-Kosi/input/local/gis/hrus/hrus.shp");
        ShapefileLoader loader = new ShapefileLoader();
        Layer l = loader.createLayerFromShapefile(f);
        System.out.println(l);
    }
}
