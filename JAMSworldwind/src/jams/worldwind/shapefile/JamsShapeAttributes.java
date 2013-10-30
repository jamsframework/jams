/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.worldwind.shapefile;

import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class JamsShapeAttributes extends BasicShapeAttributes {

    private final static Logger logger = LoggerFactory.getLogger(JamsShapeAttributes.class);
    private ShapefileRecord shapeFileRecord;

    public JamsShapeAttributes(ShapefileRecord record) {
        this.shapeFileRecord = record;
    }
    
    public ShapefileRecord getShapeFileRecord() {
        return shapeFileRecord;
    }
   
}
