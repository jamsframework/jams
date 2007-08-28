package jams.components.io;

import java.util.HashMap;
import java.util.Iterator;

import org.geotools.data.FeatureReader;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.Feature;

import org.unijena.jams.data.JAMSEntity;
import org.unijena.jams.data.JAMSEntityCollection;
import org.unijena.jams.data.JAMSInteger;
import org.unijena.jams.data.JAMSString;
import org.unijena.jams.model.JAMSComponent;
import org.unijena.jams.model.JAMSVarDescription;

import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author C. Schwartze
 */

public class GeomReader extends JAMSComponent {
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Data file directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "shape file name"
            )
            public JAMSString shapeFileName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Collection of hru objects"
            )
            public JAMSEntityCollection hrus;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Name of identifier column in shape file"
            )
            public JAMSString idName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Original Shape file name"
            )
            public JAMSString baseShape;
    
    public void run() throws Exception {
    	java.net.URL shapeUrl = (new java.io.File(dirName.getValue()+"/"+shapeFileName.getValue()).toURI().toURL());
        ShapefileDataStore store = new ShapefileDataStore(shapeUrl);
        baseShape.setValue(dirName.getValue()+"/"+shapeFileName.getValue()+";"+idName);
        
        Iterator reader = store.getFeatureSource(store.getTypeNames()[0]).getFeatures().iterator();
        
        HashMap<Object, Geometry> geomMap = new HashMap<Object, Geometry>();
        while (reader.hasNext()) {
        	Feature f = (Feature) reader.next();
        	Long id = new Long(f.getAttribute(idName.getValue()).toString());
        	geomMap.put(id, f.getDefaultGeometry());
        }
        
        JAMSEntity e;
        Iterator<JAMSEntity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            long id = new Double(e.getDouble("ID")).longValue();
            e.setGeometry("geom", geomMap.get(id));
        }
    }
    
}