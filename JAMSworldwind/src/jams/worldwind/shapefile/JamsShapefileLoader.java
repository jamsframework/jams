package jams.worldwind.shapefile;

import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwindx.examples.util.ShapefileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class JamsShapefileLoader extends ShapefileLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(JamsShapefileLoader.class);
    
    @Override
    protected PointPlacemarkAttributes createPointAttributes(ShapefileRecord record)
    {
        logger.error("Shapefile point type NOT implemented - no attributes added!");
        return null;
    }

    
    @Override
    protected ShapeAttributes createPolylineAttributes(ShapefileRecord record)
    {
        logger.error("Shapefile polyline type NOT implemented - no attributes added!");
        return null;
    }

    
    @Override
    protected ShapeAttributes createPolygonAttributes(ShapefileRecord record)
    {
        //logger.info(record.getShapeType());
        //logger.info("No.: " + record.getRecordNumber() + " Attributes: " + record.getAttributes().getValue("AREA"));
        ShapeAttributes shapeAttributes = new JamsShapeAttributes(record);
        return shapeAttributes;
    }
    
}
