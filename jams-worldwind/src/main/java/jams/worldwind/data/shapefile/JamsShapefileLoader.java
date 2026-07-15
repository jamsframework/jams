package jams.worldwind.data.shapefile;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.layers.RenderableLayer;
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

    @Override
    protected void addRenderablesForPoints(Shapefile shp, RenderableLayer layer) {
        logger.error("Shapefile point type NOT implemented - no renderables added!");
    }

    @Override
    protected void addRenderablesForPolylines(Shapefile shp, RenderableLayer layer) {
        while (shp.hasNext()) {
            ShapefileRecord record = shp.nextRecord();

            if (!Shapefile.isPolylineType(record.getShapeType())) {
                continue;
            }

            ShapeAttributes attrs = this.createPolylineAttributes(record);
            layer.addRenderable(this.createPolyline(record, attrs));
        }
    }

    /*
     * Anders als die Basisklasse (ein ShapeAttributes fuer alle Records)
     * bekommt jeder Record eigene JamsShapeAttributes, da diese den
     * ShapefileRecord fuer den spaeteren Datenzugriff transportieren.
     */
    @Override
    protected void addRenderablesForSurfacePolygons(Shapefile shp, RenderableLayer layer) {
        while (shp.hasNext()) {
            ShapefileRecord record = shp.nextRecord();

            if (!Shapefile.isPolygonType(record.getShapeType())) {
                continue;
            }

            this.createPolygon(record, this.createPolygonAttributes(record), layer);
        }
    }

    /**
     *
     * @param record
     * @return
     */
    protected ShapeAttributes createPolylineAttributes(ShapefileRecord record) {
        ShapeAttributes shapeAttributes = new JamsShapeAttributes(record);
        return shapeAttributes;
    }

    /**
     *
     * @param record
     * @return
     */
    protected ShapeAttributes createPolygonAttributes(ShapefileRecord record) {
        ShapeAttributes shapeAttributes = new JamsShapeAttributes(record);
        shapeAttributes.setOutlineOpacity(0.2);
        shapeAttributes.setOutlineWidth(0.4);
        return shapeAttributes;
    }

}
