/*
 * Ping.java
 * Created on 15. Dezember 2006, 14:13
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
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

package jams.components.debug;

import jams.JAMS;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
title="Ping",
        author="Sven Kralisch",
        description="Echos a ping (with component's name) at invocation ...",
        date="20.12.2006"
        )
        public class Ping extends JAMSComponent {
    
    /*
     *  Component run stages
     */
    
    private long runCounter = 0;
    private long initCounter = 0;
    private long cleanupCounter = 0;
    
    public void init() {
        getModel().getRuntime().println(getInstanceName() + "@init (" + (++initCounter) + ")", JAMS.VERBOSE);
    }
    
    public void run() {
        getModel().getRuntime().println(getInstanceName() + "@run (" + (++runCounter) + ")", JAMS.VERBOSE);
    }
    
    public void cleanup() {
        getModel().getRuntime().println(getInstanceName() + "@cleanup (" + (++cleanupCounter) + ")", JAMS.VERBOSE);
    }
}
