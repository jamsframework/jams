/*
 * DataAccess.java
 * Created on 06.09.2018, 10:26:11
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.data;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "DataAccess",
        author = "Sven Kralisch",
        description = "Component to make implicit attributes (e.g. from HRU "
        + "data files) visible",
        date = "2018-09-14",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class DataAccess extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ
    )
    public Attribute.Object[] o;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ
    )
    public Attribute.Double[] d;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ
    )
    public Attribute.String[] s;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ
    )
    public Attribute.Integer[] i;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ
    )
    public Attribute.Long[] l;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ
    )
    public Attribute.Float[] f;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
    }

    @Override
    public void run() {
    }

    @Override
    public void cleanup() {
    }
}
