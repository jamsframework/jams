/*
 * EntityProvider.java
 *
 * Created on 6. Oktober 2005, 19:12
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

import jams.data.*;
import jams.model.*;
import jams.io.*;
import java.util.*;

/**
 *
 * @author S. Kralisch
 */
public class EntityProvider extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ)
    public Attribute.String fileName;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE)
    public Attribute.EntityCollection entities;

    private JAMSTableDataStore store;

    public void init() {

        ArrayList<Attribute.Entity> entityList = new ArrayList<Attribute.Entity>();

        store = new GenericDataReader(fileName.getValue(), false, 4, 6);

        while (store.hasNext()) {

            Attribute.Entity e = this.getModel().getRuntime().getDataFactory().createEntity();
                    
            JAMSTableDataArray da = store.getNext();
            double[] vals = JAMSTableDataConverter.toDouble(da);

            Attribute.Double d;

            d = this.getModel().getRuntime().getDataFactory().createDouble();
            d.setValue(vals[0]);
            e.setObject("latitude", d);

            d = this.getModel().getRuntime().getDataFactory().createDouble();
            d.setValue(vals[1]);
            e.setObject("soilMoistStorCap", d);

            d = this.getModel().getRuntime().getDataFactory().createDouble();
            d.setValue(vals[2]);
            e.setObject("snowStorage", d);

            d = this.getModel().getRuntime().getDataFactory().createDouble();
            d.setValue(vals[3]);
            e.setObject("runoffFactor", d);

            d = this.getModel().getRuntime().getDataFactory().createDouble();
            d.setValue(vals[4]);
            e.setObject("prestor", d);

            d = this.getModel().getRuntime().getDataFactory().createDouble();
            d.setValue(vals[5]);
            e.setObject("remain", d);
            /*
            e.setDouble("latitude", vals[0]);
            e.setDouble("soilMoistStorCap", vals[1]);
            e.setDouble("snowStorage", vals[2]);
            e.setDouble("runoffFactor", vals[3]);
            e.setDouble("prestor", vals[4]);
            e.setDouble("remain", vals[5]);
             */
            entityList.add(e);

        }

        entities.setEntities(entityList);

    }
}
