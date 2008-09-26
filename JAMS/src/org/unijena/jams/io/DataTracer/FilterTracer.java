/*
 * FilterTracer.java
 * Created on 26. September 2008, 16:44
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

package org.unijena.jams.io.DataTracer;

import org.unijena.jams.data.JAMSEntity;
import org.unijena.jams.dataaccess.DataAccessor;
import org.unijena.jams.model.JAMSContext;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class FilterTracer extends AbstractTracer {

    public FilterTracer(JAMSContext context, Class idClazz) {
        super(context, idClazz);
    }
    
    /**
     * This method contains code to be executed as traced JAMSData objects change
     */
    @Override
    public void trace() {

        DataAccessor[] dataAccessors = this.accessorObjects;
        JAMSEntity[] entities = context.getEntities().getEntityArray();
        for (int j = 0; j < entities.length; j++) {

            output(entities[j].getId());
            output("\t");

            for (int i = 0; i < dataAccessors.length; i++) {
                dataAccessors[i].setIndex(j);
                dataAccessors[i].read();
                output(dataAccessors[i].getComponentObject());
                output("\t");
            }
            output("\n");
        }
    }    

}
