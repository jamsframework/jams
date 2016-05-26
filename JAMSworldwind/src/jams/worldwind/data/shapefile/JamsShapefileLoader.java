package jams.worldwind.data.shapefile;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwindx.examples.util.ShapefileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class JamsShapefileLoader extends ShapefileLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(JamsShapefileLoader.class);
    
    public JamsShapefileLoader() {
        super();
        setNumPolygonsPerLayer(1000000);
    }
    
    @Override
    protected void addRenderablesForPolylines(Shapefile shp, RenderableLayer layer)
    {
        while (shp.hasNext())
        {
            ShapefileRecord record = shp.nextRecord();
        
            if (!Shapefile.isPolylineType(record.getShapeType()))
                continue;
        
            ShapeAttributes attrs = this.createPolylineAttributes(record);
            layer.addRenderable(this.createPolyline(record, attrs));
        }
    }
    
    
    /**
     *
     * @param record
     * @return
     */
    @Override
    protected PointPlacemarkAttributes createPointAttributes(ShapefileRecord record)
    {
        logger.error("Shapefile point type NOT implemented - no attributes added!");
        return null;
    }

    /**
     *
     * @param record
     * @return
     */
    @Override
    protected ShapeAttributes createPolylineAttributes(ShapefileRecord record)
    {
        ShapeAttributes shapeAttributes = new JamsShapeAttributes(record);
        return shapeAttributes;
    }

    /**
     *
     * @param record
     * @return
     */
    @Override
    protected ShapeAttributes createPolygonAttributes(ShapefileRecord record)
    {
        //logger.info(record.getShapeType());
        //logger.info("No.: " + record.getRecordNumber() + " Attributes: " + record.getAttributes().getValue("AREA"));
        ShapeAttributes shapeAttributes = new JamsShapeAttributes(record);
        shapeAttributes.setOutlineOpacity(0.2);
        shapeAttributes.setOutlineWidth(0.4);
        return shapeAttributes;
    }
    
}
