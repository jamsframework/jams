/*
 * DSExample.java
 * Created on 24. Februar 2009, 21:59
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package reg.dsproc;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class DSExample {

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {

        DataStoreProcessor dsdb = new DataStoreProcessor("D:/jamsapplication/JAMS-Gehlberg/output/current/HRULoop.dat");
        //dsdb.removeDB();
        dsdb.addImportProgressObserver(new Observer() {

            public void update(Observable o, Object arg) {
                System.out.println("Import progress: " + arg);
            }
        });

        if (!dsdb.existsH2DB()) {
            dsdb.createDB();
        }
        TimeSpaceProcessor tsproc = new TimeSpaceProcessor(dsdb);
        if (!tsproc.isTimeSpaceDatastore()) {
            return;
        }

        tsproc.addProcessingProgressObserver(new Observer() {

            public void update(Observable o, Object arg) {
                //System.out.println("Processing progress: " + arg);
            }
        });

        if (!tsproc.isMonthlyMeanExisiting()) {
            tsproc.calcMonthlyMean();
        }

        // longterm monthly mean values
        for (int i = 1; i <= 12; i++) {
            DataMatrix monthlyMean = tsproc.getMonthlyMean(i);
            double[][] data = monthlyMean.getArray();
//            monthlyMean.output();
        }

        // monthly mean values
        for (int year : tsproc.getYears()) {
            for (int i = 1; i <= 12; i++) {
                DataMatrix monthlyMean = tsproc.getTemporalMean(year + "-" + String.format("%02d", i) + "-%");
                if (monthlyMean == null) {
                    continue;
                }
                double[][] data = monthlyMean.getArray();
//                monthlyMean.output();
            }
        }

    }
}
