package jams.worldwind.test;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */

public class GetShapefileAttributes {
    
    final String testshapefile = "../../../JAMSworldwind/shapefiles/JAMS-KOSI/hrus.shp";

    /**
     *
     */
    public GetShapefileAttributes() {
        System.out.println("Working directory : " + System.getProperty("user.dir"));
        Shapefile sf = new Shapefile(new File(testshapefile));
        
        System.out.println("Number of records : " + sf.getNumberOfRecords());
        try {
            while (sf.hasNext()) {
                ShapefileRecord record = sf.nextRecord();
                Set<Entry<String, Object>> att = record.getAttributes().getEntries();

                //print entries
                for ( Iterator<Entry<String,Object>> iterator = att.iterator(); iterator.hasNext(); )
                    System.out.println("Entry             : " + iterator.next());
                System.out.println("Type              : " + record.getShapeType());
            }
        } finally {
        }
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        new GetShapefileAttributes();
    }
}
