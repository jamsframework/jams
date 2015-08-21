/*
 * EntityPacker.java
 *
 * Created on 6. Oktober 2005, 19:45
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

/**
 *
 * @author S. Kralisch
 */
public class EntityPacker extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READWRITE)
    public Attribute.Entity entity;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ)
    public Attribute.Double snowStorage;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ)
    public Attribute.Double prestor;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ)
    public Attribute.Double remain;

    public void run() {
        entity.setDouble("snowStorage", snowStorage.getValue());
        entity.setDouble("prestor", prestor.getValue());
        entity.setDouble("remain", remain.getValue());
    }
}
