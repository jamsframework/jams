/*
 * DoubleDivide.java
 * Created on 18.05.2016, 15:51:19
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
package jams.components.calc;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Manfred Fink
 */
@JAMSComponentDescription(
        title = "Boolean_Switch",
        author = "Manfred Fink",
        description = "Performes a boolean NOT operation or just a copy of the input variable",
        date = "2017-03-28",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class Boolean_Switch extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Boolean Input"
    )
    public Attribute.Boolean B_input;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Determines whether output is just a copy or performing a NOT operation (true, boolean NOT; false, copy)"
    )
    public Attribute.Boolean Copy_or_Not;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Result"
    )
    public Attribute.Boolean B_Output;

    /*
     *  Component run stages
     */
    @Override
    public void run() {
        
        boolean run_input = B_input.getValue();
        boolean condition = Copy_or_Not.getValue();
        boolean run_output = false;
        
        if (condition){
            run_output = !run_input;
        }else{
            run_output = run_input;
        }
        
        B_Output.setValue(run_output);
        
        
    }
}
