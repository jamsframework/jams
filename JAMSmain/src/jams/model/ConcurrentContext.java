/*
 * ConcurrentContext.java
 * Created on 12. Januar 2010, 22:55
 *
 * This file is a JAMS component
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
package jams.model;

import jams.io.DataTracer.DataTracer;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription (title = "ConcurrentContext",
                           author = "Sven Kralisch",
                           description = "A context that executes its child components concurrently")
public class ConcurrentContext extends JAMSContext {

    /*
     *  Component run stages
     */

    @Override
    public void run() {

        //super.run();
        //TODO


        for (DataTracer dataTracer : dataTracers) {
            dataTracer.startMark();
        }

        //initEntityData();

        if (runEnumerator == null) {
            runEnumerator = getRunEnumerator();
        }

        runEnumerator.reset();
        while (runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            //comp.updateRun();
            try {
                comp.run();
            } catch (Exception e) {
                getModel().getRuntime().handle(e, comp.getInstanceName());
            }
        }

        updateEntityData();

        for (DataTracer dataTracer : dataTracers) {
            dataTracer.trace();
            dataTracer.endMark();
        }

    }

}
