/*
 * JAMSVarDescription.java
 * Created on 26. Juni 2005, 22:26
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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

package org.unijena.jams.model;

/**
 *
 * @author S. Kralisch
 */

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface JAMSVarDescription {

    public enum AccessType { READ, WRITE, READWRITE };
    public enum UpdateType { INIT, RUN };
    
    AccessType access();                        // type of access
    String description() default "";            // description of purpose
    String defaultValue() default "";           // default value
    String unit() default "";                   // unit of this var if numeric
    double lowerBound() default 0d;             // lowest allowed value of var if numeric
    double upperBound() default 0d;             // highest allowed value of var if numeric

    //this is obsolete//
    UpdateType update();                        // when to update
}
