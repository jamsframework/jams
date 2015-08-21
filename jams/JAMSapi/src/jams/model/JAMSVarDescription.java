/*
 * JAMSVarDescription.java
 * Created on 26. Juni 2005, 22:26
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
package jams.model;

/**
 *
 * @author S. Kralisch
 */
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface JAMSVarDescription {

    public enum AccessType {

        READ, WRITE, READWRITE
    };
    
    public static final String NULL_VALUE = "%NULL%";

    AccessType access();                        // type of access

    String description() default "";            // description of purpose

    String defaultValue() default NULL_VALUE;   // default value

    String unit() default "";                   // unit of this var if numeric

    double lowerBound() default Double.NEGATIVE_INFINITY;             // lowest allowed value of var if numeric

    double upperBound() default Double.POSITIVE_INFINITY;             // highest allowed value of var if numeric

    int length() default 0;                     // length of variable if string

    //this is obsolete//
    public enum UpdateType {

        INIT, RUN
    };

    UpdateType update() default UpdateType.INIT;// when to update
}
