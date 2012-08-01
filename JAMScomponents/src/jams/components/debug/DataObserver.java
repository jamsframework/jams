/*
 * DataObserver.java
 * Created on 02.06.2012, 01:12:34
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
package jams.components.debug;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(title = "Title",
author = "Author",
description = "Description",
date = "YYYY-MM-DD",
version = "1.0_0")
public class DataObserver extends JAMSComponent {

    /*
     * Component attributes
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ)
    public Attribute.Double[] doubleAttribs;

    /*
     * Component run stages
     */
    @Override
    public void init() {
    }

    private String print(JAMSData[] values) {
        String s = "{";
        
        if (values.length > 0) {
            s += values[0];
        }
        
        for (int i = 1; i < values.length; i++) {
            s += " " + values[i];
        }
        
        s += "}";
        return s;
    }
    
    @Override
    public void run() {
        getModel().getRuntime().println(print(doubleAttribs));
    }

    @Override
    public void cleanup() {
    }
}