/*
 * EntityObserver.java
 * Created on 19. Juli 2006, 14:41
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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

package jams.components.io;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
        title="EntityObserver",
        author="Sven Kralisch",
        description="Ouput an entities attributes and their values to the info " +
        "log. The entity must be specified by providing the name and value of " +
        "some identifying attribute."
        )
        public class EntityObserver extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public JAMSEntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Description"
            )
            public JAMSString idAttribute;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Description"
            )
            public JAMSDouble idValue;
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        String s;
        for (JAMSEntity e : entities.getEntities()) {
            if (e.getDouble(idAttribute.getValue()) == idValue.getValue()) {
                getModel().getRuntime().println("******************************************************");
                getModel().getRuntime().println("Entity information for entity " + e);
                getModel().getRuntime().println("******************************************************");
                Object[] keys = e.getKeys();
                for (int i = 0; i < keys.length; i++) {
                    s = String.format("%20s: %s", keys[i], e.getObject(keys[i].toString()));
                    getModel().getRuntime().println(s);
                }
                return;
            }
        }
    }
}
