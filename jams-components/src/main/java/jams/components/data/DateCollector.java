/*
 * DateCollector.java
 * Created on 28.11.2019, 22:02:23
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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "DateColllector",
        author = "Sven Kralisch",
        description = "Component for collecting Attribute.Calendar values into "
                + "a list which is wrapped into an Attribute.Object",
        date = "2019-28-11",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class DateCollector extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The input value to collect"
    )
    public Attribute.Calendar input;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "The list to store the values"
    )
    public Attribute.Object list;

    /*
     *  Component run stages
     */
    @Override
    public void initAll() {
        list.setValue(new ArrayList<Attribute.Calendar>());
    }

    @Override
    public void run() {
        
        List<Attribute.Calendar> l = (ArrayList<Attribute.Calendar>) list.getValue();
        l.add(input.getValue());
        
    }
}