package jams.components.io;

import java.util.HashMap;
import java.util.Iterator;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.Feature;

import jams.data.JAMSEntity;
import jams.data.JAMSEntityCollection;
import jams.data.JAMSString;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;

import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author C. Schwartze
 */

public class GeomReader extends JAMSComponent {
    
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
        
    	java.net.URL shapeUrl = (new java.io.File(getModel().getWorkspaceDirectory().getPath()+"/"+shapeFileName.getValue()).toURI().toURL());
        ShapefileDataStore store = new ShapefileDataStore(shapeUrl);
        baseShape.setValue(getModel().getWorkspaceDirectory().getPath()+"/"+shapeFileName.getValue()+";"+idName);
        
        Iterator reader = store.getFeatureSource(store.getTypeNames()[0]).getFeatures().iterator();
        
        HashMap<Long, Geometry> geomMap = new HashMap<Long, Geometry>();
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