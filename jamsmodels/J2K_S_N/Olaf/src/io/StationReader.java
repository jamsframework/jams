package io;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms3.annotations.*;
import oms3.io.CSTable;
import oms3.io.DataIO;
import static oms3.annotations.Role.*;

@Description
    ("Reader station xy info")
@Author
    (name = "Olaf David")
@Keywords
    ("I/O")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/io/StationReader.java $")
@VersionInfo
    ("$Id: StationReader.java 893 2010-01-29 16:06:46Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
public class StationReader {

    private static final Logger log = Logger.getLogger("oms3.model." +
            StationReader.class.getSimpleName());
    
    @Description("Data file name")
    @In public File dataFile;

    @Description("Array of station's x coordinate")
    @Out public double[] xCoord;
    
    @Description("Array of station's y coordinate")
    @Out public double[] yCoord;

    @Description("Array of station's elevations")
    @Out public double[] elevation;

    @Execute
    public void exec() throws Exception {
        CSTable table = DataIO.table(dataFile, "Climate");
        int len = table.getColumnCount()-1;
        
        xCoord = new double[len];
        yCoord = new double[len];
        elevation = new double[len];
        
        for (int i = 0; i < len; i++) {
            // first column is 2 (columns start with 1 + skipping date)
            xCoord[i] = Double.parseDouble(table.getColumnInfo(i+2).get("x"));
            yCoord[i] = Double.parseDouble(table.getColumnInfo(i+2).get("y"));
            elevation[i] = Double.parseDouble(table.getColumnInfo(i+2).get("elevation"));
        }
        if (log.isLoggable(Level.INFO)) {
            log.info(dataFile.toString());
            log.info(Arrays.toString(xCoord));
            log.info(Arrays.toString(yCoord));
            log.info(Arrays.toString(elevation));
        }
    }

    public static void main(String[] args) throws Exception {
        StationReader r = new StationReader();
        r.dataFile = new File("C:\\od\\projects\\oms3.prj.ceap\\data\\gehlberg\\tmean.csv");
        r.exec();
    }
}
