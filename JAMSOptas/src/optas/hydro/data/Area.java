
package optas.hydro.data;

public class Area extends DataSet {
    
    private double latitude = -9999;
    private double longitude = -9999;
    private double elevation = -9999;
    
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
