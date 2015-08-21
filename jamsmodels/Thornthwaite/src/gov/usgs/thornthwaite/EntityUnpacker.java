/*
 * EntityUnpacker.java
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
public class EntityUnpacker extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ)
    public Attribute.Entity entity;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE)
    public Attribute.Double latitude;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE)
    public Attribute.Double soilMoistStorCap;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE)
    public Attribute.Double snowStorage;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE)
    public Attribute.Double runoffFactor;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE)
    public Attribute.Double prestor;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE)
    public Attribute.Double remain;

    public void run() throws Exception {
        this.latitude.setValue(entity.getDouble("latitude"));
        this.soilMoistStorCap.setValue(entity.getDouble("soilMoistStorCap"));
        this.snowStorage.setValue(entity.getDouble("snowStorage"));
        this.runoffFactor.setValue(entity.getDouble("runoffFactor"));
        this.prestor.setValue(entity.getDouble("prestor"));
        this.remain.setValue(entity.getDouble("remain"));
    }
}
