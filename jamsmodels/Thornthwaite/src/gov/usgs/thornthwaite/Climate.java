/*
 * Climate.java
 * Created on 30. September 2005, 11:37
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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
package gov.usgs.thornthwaite;

import jams.model.*;
import jams.data.*;
import jams.io.GenericDataReader;
import jams.io.JAMSTableDataArray;
import jams.io.JAMSTableDataConverter;
import jams.io.JAMSTableDataStore;
import java.io.File;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription (title = "Thorntwaite model climate reader",
                           author = "Sven Kralisch",
                           date = "30. September 2005",
                           description = "This component reads timeseries of temperature and precipitation from an ASCII file")
public class Climate extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "File containg the data")
    public Attribute.String fileName;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Temperature read from the file")
    public Attribute.Double temp;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Precipitation read from the file")
    public Attribute.Double precip;

    private JAMSTableDataStore store;

    public void init() {
        store = new GenericDataReader(getModel().getWorkspace().getInputDirectory().getPath() + File.separator + fileName.getValue(), false, 4, 6);
    }

    public void run() {

        JAMSTableDataArray da = store.getNext();
        double[] vals = JAMSTableDataConverter.toDouble(da);
        this.temp.setValue(vals[1]);
        this.precip.setValue(vals[2]);

    }

    public void cleanup() {
        store.close();
    }
}
