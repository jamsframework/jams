/*
 * UTMConversion.java
 *
 * Created on 10. Mai 2006, 09:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lib;

import lib.MathCalc;

/**
 *
 * @author c0krpe
 */
public class UTMConversion {
    
    /**
     * Creates a new instance of UTMConversion
     */
    public UTMConversion() {
    }
    
//    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            
//            public void run() {
//                
//                double[] xy = new double[2];
//                double[] latLong = {54.016666,7.883333};
//                
//                //for(int i = 0; i < 10; i++){
//                xy = latLong2UTM(latLong[0], latLong[1]);
//                //System.out.println("X: " + xy[0] + " Y: " + xy[1]);
//                
//                String zoneStr = getZoneStr(latLong[0], latLong[1]);
//                int zone = getZoneNumber(latLong[0], latLong[1]);
//                latLong = utm2LatLong(xy[0], xy[1], zoneStr);
//              //  System.out.println("Lat: " + latLong[0] + " Long: " + latLong[1]);
//                //}
//            }
//        });
//    }
    
    public static int getZoneNumber(double lat, double lon){
        String zone = getZoneStr(lat, lon);
        int len = zone.length();
        int zsIdx = len - 1;
        char zoneChr = zone.charAt(len - 1);
        String zoneStr = "" + zoneChr;
        // System.out.println("zoneStr:  " + zoneStr);
        
        String numberPart = zone.substring(0, zsIdx);
        int zoneNumber = Integer.parseInt(numberPart);
        
        return zoneNumber;
    }
    
    public static int getZoneNumber(String zoneStr){
        int len = zoneStr.length();
        int zsIdx = len - 1;
        int nmIdx = zsIdx - 1;
        char zoneChr = zoneStr.charAt(len - 1);
        
        //System.out.println("zoneStr:  " + zoneStr);
        
        String numberPart = zoneStr.substring(0, zsIdx);
        int zoneNumber = Integer.parseInt(numberPart);
        //System.out.println("zoneNumber: " + zoneNumber);
        return zoneNumber;
    }
    
    public static boolean isSouth(String zone){
        boolean isSouth = false;
        String[] southLetters = {"A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M"};
        int len = zone.length();
        int zsIdx = len - 1;
        int nmIdx = zsIdx - 1;
        char zoneChr = zone.charAt(len - 1);
        String zoneStr = "" + zoneChr;
        
        for(int i = 0; i < southLetters.length; i++){
            if(zoneStr.equals(southLetters[i])){
                isSouth = true;
            }
        }
        return isSouth;
    }
    
    public static String getZoneStr(double lat, double lon){
        String LetterDesignator;
        if((84 >= lat) && (lat >= 72)) LetterDesignator = "X";
        else if((72 > lat) && (lat >= 64)) LetterDesignator = "W";
        else if((64 > lat) && (lat >= 56)) LetterDesignator = "V";
        else if((56 > lat) && (lat >= 48)) LetterDesignator = "U";
        else if((48 > lat) && (lat >= 40)) LetterDesignator = "T";
        else if((40 > lat) && (lat >= 32)) LetterDesignator = "S";
        else if((32 > lat) && (lat >= 24)) LetterDesignator = "R";
        else if((24 > lat) && (lat >= 16)) LetterDesignator = "Q";
        else if((16 > lat) && (lat >= 8)) LetterDesignator = "P";
        else if(( 8 > lat) && (lat >= 0)) LetterDesignator = "N";
        else if(( 0 > lat) && (lat >= -8)) LetterDesignator = "M";
        else if((-8 > lat) && (lat >= -16)) LetterDesignator = "L";
        else if((-16 > lat) && (lat >= -24)) LetterDesignator = "K";
        else if((-24 > lat) && (lat >= -32)) LetterDesignator = "J";
        else if((-32 > lat) && (lat >= -40)) LetterDesignator = "H";
        else if((-40 > lat) && (lat >= -48)) LetterDesignator = "G";
        else if((-48 > lat) && (lat >= -56)) LetterDesignator = "F";
        else if((-56 > lat) && (lat >= -64)) LetterDesignator = "E";
        else if((-64 > lat) && (lat >= -72)) LetterDesignator = "D";
        else if((-72 > lat) && (lat >= -80)) LetterDesignator = "C";
        else LetterDesignator = "Z"; //This is here as an error flag to show that the Latitude is outside the UTM limits
        
        int zoneNumber;
        //Make sure the longitude is between -180.00 .. 179.9
        double longTemp = (lon + 180) - (int)((lon + 180) / 360) * 360 - 180; // -180.00 .. 179.9;
        
        zoneNumber = (int)((longTemp + 180)/6) + 1;
        if( lat >= 56.0 && lat < 64.0 && longTemp >= 3.0 && longTemp < 12.0 )
            zoneNumber = 32;
        
        String zone = "" + zoneNumber + LetterDesignator;
        //System.out.println("ZoneNumber: " + zone);
        return zone;
    }
    
    
    public static double[] latLong2UTM(double lat, double lon){
        double[] utmXY = new double[2];
        
        //WGS84 constants
        double eqRad = 6378137;            //ellipsoid[ReferenceEllipsoid].EquatorialRadius;
        double eccSquared = 0.00669438;//ellipsoid[ReferenceEllipsoid].eccentricitySquared;
        double k0 = 0.9996;
        
        double longOrigin;
        double eccPrimeSquared;
        
        double latRad  = MathCalc.rad(lat);
        double longRad = MathCalc.rad(lon);
        double longOriginRad;
        int    zoneNumber = getZoneNumber(lat, lon);
        
        //System.out.println("zoneNumber in latLong2UTM: "  + zoneNumber);
        
        longOrigin = (zoneNumber - 1) * 6 - 180 + 3;  //+3 puts origin in middle of zone
        longOriginRad = MathCalc.rad(longOrigin);
        
        eccPrimeSquared = (eccSquared)/(1-eccSquared);
        
        double n = eqRad / Math.sqrt(1-eccSquared* Math.sin(latRad)* Math.sin(latRad));
        double t = Math.tan(latRad)*Math.tan(latRad);
        double c = eccPrimeSquared * Math.cos(latRad) * Math.cos(latRad);
        double a = Math.cos(latRad) * (longRad - longOriginRad);
        
        double m = eqRad *((1  - eccSquared / 4 - 3 * eccSquared * eccSquared / 64 - 5  * eccSquared * eccSquared * eccSquared /  256) * latRad
                - (3  * eccSquared / 8 + 3 * eccSquared * eccSquared / 32 + 45 * eccSquared * eccSquared * eccSquared / 1024) * Math.sin(2 *latRad)
                + (15 * eccSquared * eccSquared / 256 + 45 * eccSquared * eccSquared * eccSquared / 1024) * Math.sin(4 * latRad)
                - (35 * eccSquared * eccSquared * eccSquared / 3072) * Math.sin(6 * latRad));
        
        //utm x - coordinate
        utmXY[0] = (k0 * n * (a + (1 - t + c) * a * a * a / 6
                + (5 - 18 * t + t * t + 72 * c - 58 * eccPrimeSquared) * a * a * a * a * a / 120)
                + 500000.0);
        //utm y -coordinate
        utmXY[1] = (k0 * (m + n * Math.tan(latRad) * (a * a / 2 + (5 - t + 9 * c + 4 * c * c) * a * a * a * a / 24
                + (61 - 58 * t + t * t + 600 * c - 330 * eccPrimeSquared) * a * a * a * a * a * a / 720)));
        if(lat < 0)
            utmXY[1] += 10000000.0; //10000000 meter offset for southern hemisphere
        
        
        return utmXY;
    }
    //converts UTM coords to lat/long.  Equations from USGS Bulletin 1532
    //East Longitudes are positive, West longitudes are negative.
    //North latitudes are positive, South latitudes are negative
    //Lat and Long are in decimal degrees.
    //Converted from C++ of Chuck Gantz chuck.gantz@globalstar.com
    public static double[] utm2LatLong(double utmX, double utmY, String zoneStr){
        double[] latLong = new double[2];
        
        //WGS84 constants
        double eqRad = 6378137;            //ellipsoid[ReferenceEllipsoid].EquatorialRadius;
        double eccSquared = 0.00669438;//ellipsoid[ReferenceEllipsoid].eccentricitySquared;
        double k0 = 0.9996;
        double e1 = ( 1 - Math.sqrt(1-eccSquared)) / (1 + Math.sqrt(1-eccSquared));
        
        utmX = utmX - 500000.0; //remove 500,000 meter offset for longitude
        if(isSouth(zoneStr)){
            utmY = utmY - 10000000.0;//remove 10,000,000 meter offset used for southern hemisphere
        }
        int zone = getZoneNumber(zoneStr);
        double longOrigin = (zone - 1) * 6 - 180 + 3;  //+3 puts origin in middle of zone
        double eccPrimeSquared = (eccSquared) / ( 1 - eccSquared);
        double m  = utmY / k0;
        double mu = m / (eqRad * (1 - eccSquared / 4 - 3 * eccSquared * eccSquared / 64 - 5 * eccSquared*eccSquared * eccSquared / 256));
        double phi1Rad = mu + (  3 * e1 / 2 - 27 * e1 * e1 * e1 / 32) * Math.sin(2 * mu)
        + ( 21 * e1 * e1 / 16 - 55 * e1 * e1 * e1 * e1 / 32) * Math.sin(4 * mu)
        + (151 * e1 * e1 * e1 / 96) * Math.sin(6 * mu);
        
        double n1 = eqRad / Math.sqrt(1 - eccSquared * Math.sin(phi1Rad) * Math.sin(phi1Rad));
        double t1 = Math.tan(phi1Rad) * Math.tan(phi1Rad);
        double c1 = eccPrimeSquared * Math.cos(phi1Rad) * Math.cos(phi1Rad);
        double r1 = eqRad * (1 - eccSquared) / Math.pow(1 - eccSquared * Math.sin(phi1Rad) * Math.sin(phi1Rad), 1.5);
        double d = utmX / (n1 * k0);
        
        double latRad = phi1Rad - (n1 * Math.tan(phi1Rad) / r1) * (d * d / 2 - (5 + 3 * t1 + 10 * c1 - 4 * c1 * c1 - 9 * eccPrimeSquared) * d * d * d * d / 24
                + (61 + 90 * t1 + 298 * c1 + 45 * t1 * t1 - 252 * eccPrimeSquared - 3 * c1 * c1) * d * d * d * d * d * d / 720);
        latLong[0] = MathCalc.deg(latRad);
        
        double longRad = (d - ( 1 + 2 * t1 + c1 ) * d * d * d / 6 + (5 - 2 * c1 + 28 * t1 - 3 * c1 * c1 + 8 * eccPrimeSquared + 24 * t1 * t1)
        * d * d * d * d * d / 120) / Math.cos(phi1Rad);
        latLong[1] = MathCalc.deg(longRad) + longOrigin;
        
        return latLong;
    }
    
}
