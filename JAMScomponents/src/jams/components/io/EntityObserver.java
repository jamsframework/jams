/*
 * EntityObserver.java
 * Created on 19. Juli 2006, 14:41
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.io;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription (title = "EntityObserver",
                           author = "Sven Kralisch",
                           description = "Ouput an entities attributes and their values to the info " +
"log. The entity must be specified by providing the name and value of " +
"some identifying attribute.")
public class EntityObserver extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         update = JAMSVarDescription.UpdateType.RUN,
                         description = "Description")
    public JAMSEntityCollection entities;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         update = JAMSVarDescription.UpdateType.INIT,
                         description = "Description")
    public JAMSString idAttribute;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         update = JAMSVarDescription.UpdateType.INIT,
                         description = "Description")
    public JAMSDouble idValue;

    public void run() throws JAMSEntity.NoSuchAttributeException {
        String s;
        for (Attribute.Entity e : entities.getEntities()) {
            if (e.getDouble(idAttribute.getValue()) == idValue.getValue()) {
                getModel().getRuntime().println("******************************************************");
                getModel().getRuntime().println("Entity information for entity " + e);
                getModel().getRuntime().println("******************************************************");
                Object[] keys = e.getKeys();
                for (int i = 0; i < keys.length; i++) {
                    Object value = e.getObject(keys[i].toString());
                    if (value instanceof JAMSDoubleArray) {
                        double[] d = ((JAMSDoubleArray) value).getValue();
                        if (d != null) {
                            for (int j = 0; j < d.length; j++) {
                                s = String.format("%20s: %s", keys[i] + "[" + j + "]", d[j]);
                                getModel().getRuntime().println(s);
                            }
                        } else {
                            s = String.format("%20s: %s", keys[i] + "[]", "null");
                            getModel().getRuntime().println(s);
                        }
                    } else {
                        s = String.format("%20s: %s", keys[i], value);
                        getModel().getRuntime().println(s);
                    }
                }
                return;
            }
        }
    }
}
