package io;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms3.annotations.*;
import oms3.io.CSTable;
import oms3.io.DataIO;
import lib.Regression;
import static oms3.annotations.Role.*;

@Description
    ("Station Reader update info")
@Author
    (name = "Olaf David")
@Keywords
    ("I/O")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/io/StationUpdater.java $")
@VersionInfo
    ("$Id: StationUpdater.java 952 2010-02-11 19:55:02Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
public class StationUpdater {

    private static final Logger log = Logger.getLogger("oms3.model." +
            StationUpdater.class.getSimpleName());
    
    @Description("Data file name")
    @In public File dataFile;

    @In public Calendar startTime;
    @In public Calendar endTime;
    
    @Out public double[] dataArray;
    @Out public double[] regCoeff;
    
    @Out public boolean moreData;
    @Out public Calendar time = new GregorianCalendar();

     /** Table Row Iterator */
    Iterator<String[]> rows;
    SimpleDateFormat f;
    double[] elevation;
    String name;

    private void init0() throws Exception {
        CSTable table = DataIO.table(dataFile, "Climate");
        rows = table.rows().iterator();
        dataArray = new double[table.getColumnCount()-1];
        elevation = new double[table.getColumnCount()-1];
        for (int i = 0; i < elevation.length; i++) {
            elevation[i] = Double.parseDouble(table.getColumnInfo(i+2).get("elevation"));
        }
        f = new SimpleDateFormat(table.getInfo().get("dateFormat"));
        name = table.getInfo().get("name");
        
        Date start = f.parse(table.getInfo().get("dataStart"));
        Date end = f.parse(table.getInfo().get("dataEnd"));
        if (startTime.getTime().before(start) || endTime.getTime().after(end) || endTime.before(startTime)) {
            throw new RuntimeException("Illegal start/end for " + name + " " + 
                    f.format(startTime.getTime()) + " - " + f.format(endTime.getTime()));
        }
        time.setTime(start);
    }

    @Execute
    public void exec() throws Exception {
        String[] row = null;
        if (rows == null) {
            init0();
            while (time.before(startTime)) {
                row = rows.next();
                time.setTime(f.parse(row[1]));
            }
            if (row == null) {                      // first day
                row = rows.next();
                time.setTime(f.parse(row[1]));
            }
            for (int i = 0; i < dataArray.length; i++) {
                // first column is 2 (columns start with 1 + skipping date)
                dataArray[i] = Double.parseDouble(row[i + 2]);
            }
            regCoeff = Regression.calcLinReg(elevation, dataArray);
            if (log.isLoggable(Level.INFO)) {
                System.out.println(name + ": " + f.format(time.getTime()) + " " + Arrays.toString(dataArray));
            }
            moreData = rows.hasNext();
            return;
        }
        if (rows.hasNext()) {
            row = rows.next();
            time.setTime(f.parse(row[1]));
            if (time.equals(endTime) || time.after(endTime)) {
                moreData = false;
                return;
            }
            for (int i = 0; i < dataArray.length; i++) {
                // first column is 2 (columns start with 1 + skipping date)
                dataArray[i] = Double.parseDouble(row[i + 2]);
            }
            regCoeff = Regression.calcLinReg(elevation, dataArray);
            if (log.isLoggable(Level.INFO)) {
                System.out.println(name + ": " + f.format(time.getTime()) + " " + Arrays.toString(dataArray));
            }
        }
        moreData = rows.hasNext();
    }

    public static void main(String[] args) throws Exception {
        StationUpdater r = new StationUpdater();
        r.dataFile = new File("C:\\od\\projects\\oms3.prj.ceap\\data\\duck\\tmean.csv");
        r.startTime = new GregorianCalendar(1979,0,1);
        r.endTime = new GregorianCalendar(1979,0,6);
        
        do {
            r.exec();
            System.out.println(".");
        } while (r.moreData);
        
//        System.out.println(Arrays.toString(r.dataArray));
//        System.out.println(Arrays.toString(r.regCoeff));
//        System.out.println(r.f.format(r.time.getTime()));
    }
}
