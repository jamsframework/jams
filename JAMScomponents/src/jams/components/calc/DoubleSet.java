/*
 * DoubleSetter.java
 * Created on 1. Februar 2025, 23:00
 *
 * This file is part of JAMS
 * Copyright (C) 2025 S. Kralisch and P. Krause
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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package jams.components.calc;

import jams.model.*;
import jams.data.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "DoubleSetter",
author = "Sven Kralisch",
date = "2025-02-01",
version = "1.0_0",
description = "DoubleSet can be used to set a number of attributes to one "
        + "or more values. If \"value\" contains only one element, all "
        + "\"attributes\" are set to this value. Otherwise \"value\" must "
        + "contain one element for each attribute, i.e. sizes of \"attributes\" "
        + "and \"value\" must be equal.")
public class DoubleSet extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Double attributes to be set"
            )
            public Attribute.Double[] attributes;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Double values"
            )
            public Attribute.Double[] value;
    
    public void run() {
        if (value.length == 1) {
            for (int i = 0; i < attributes.length; i++) {
                attributes[i].setValue(value[0].getValue());
            }
        } else if (attributes.length == value.length) {
            for (int i = 0; i < attributes.length; i++) {
                attributes[i].setValue(value[i].getValue());
            }            
        }
    }
}
