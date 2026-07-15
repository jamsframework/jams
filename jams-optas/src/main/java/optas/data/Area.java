
package optas.data;

import jams.JAMS;

public class Area extends DataSet {
    
    private double latitude = JAMS.getMissingDataValue();
    private double longitude = JAMS.getMissingDataValue();
    private double elevation = JAMS.getMissingDataValue();
    
    public Area(double latitude, double longitude, double elevation) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
    }
    
    public double getElevation() {
        return this.elevation;
    }

    public double getLatitude() {
        return this.latitude;
    }
    
    public double getLongitude() {
        return this.longitude;
    }
    
}
