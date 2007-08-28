/*
 * Ping.java
 * Created on 15. Dezember 2006, 14:13
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
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

package jams.components.debug;

import org.unijena.jams.JAMS;
import org.unijena.jams.model.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
title="Ping",
        author="Sven Kralisch",
        description="Echos a ping at invocation ...",
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
