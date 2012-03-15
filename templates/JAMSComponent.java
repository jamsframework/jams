/*
 * ${name}.java
 * Created on ${date}, ${time}
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

<#if package?? && package != "">
package ${package};

</#if>
import jams.data.*;
import jams.model.*;

/**
 *
 * @author ${user}
 */
 @JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description",
        date = "YYYY-MM-DD",
        version = "1.0_0"
        )
public class ${name} extends JAMSComponent {

    /*
     *  Component attributes
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,       // type of access, i.e. READ, WRITE, READWRITE
            description = "Description",                       // description of purpose
            defaultValue = "0",                                // default value, defaults to "%NULL%"
            unit = "mm",                                       // unit of this var if numeric, defaults to ""
            lowerBound = 0,                                    // lowest allowed value of var if numeric, defaults to "0"
            upperBound = 1000,                                 // highest allowed value of var if numeric, defaults to "0"
            length = 0                                         // length of variable if string, defaults to "0"            
            )
            public Attribute.Double attribName;                // for a list of attribute types, see jams.data.Attribute  
                
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