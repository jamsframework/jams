/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.worldwind.shapefile;

import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.render.BasicShapeAttributes;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class JamsShapeAttributes extends BasicShapeAttributes {
    
    private ShapefileRecord record;
    
    public JamsShapeAttributes() {
    }
    
    public JamsShapeAttributes(ShapefileRecord record) {
        this.record = record;
    }
    
    public ShapefileRecord getShapefileRecord() {
        return this.record;
    }
}
