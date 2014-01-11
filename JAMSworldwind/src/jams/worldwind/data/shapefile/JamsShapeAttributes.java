/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.worldwind.data.shapefile;

import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class JamsShapeAttributes extends BasicShapeAttributes {

    private final static Logger logger = LoggerFactory.getLogger(JamsShapeAttributes.class);
    private ShapefileRecord shapeFileRecord;

    /**
     *
     * @param record
     */
    public JamsShapeAttributes(ShapefileRecord record) {
        this.shapeFileRecord = record;
    }
    
    /**
     *
     * @return
     */
    public ShapefileRecord getShapeFileRecord() {
        return shapeFileRecord;
    }
   
}
