package org.unijena.jams.data;

import com.vividsolutions.jts.geom.Geometry;


/**
*
* @author C. Schwartze
*/

public class JAMSGeometry  extends JAMSSerializableData {
	
	private Geometry geo;
	
	public JAMSGeometry(Geometry geo) {
        this.geo = geo;
    }
	
	public void setValue(Geometry geo) {
        this.geo = geo;
    }
	
	public Geometry getValue() {
		return geo;
	}
	
	public void setValue(String data) {	
    }

}