/*
 * DoubleMultiply.java
 * Created on 11.07.2018, 15:51:19
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

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Manfred Fink <Manfred.fink at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "Double_transition",
        author = "Manfred Fink",
        description = "Transfers linear double values according to a multiplyer or a summand and a transition intervall and one independent variable",
        date = "2018-10-09",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class Double_transition extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Values to be transfered (M_S, range and Para_trans must have the same number of entries)"
    )
    public Attribute.Double[] Para;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Multiplier or sumand depending on Add_or_mult"
    )
    public Attribute.Double[] M_S;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "intervall range"
    )
    public Attribute.Double[] range;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "intended denter of control parameter"
    )
    public Attribute.Double independet_pos;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "control parameter"
    )
    public Attribute.Double independet_var;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "intervall range around the control parameter"
    )
    public Attribute.Double trans;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "indicattes if transition is an addition (0) or a multiplication (1)",
            defaultValue = "1"
    )
    public Attribute.Double Add_or_mult;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Values that are transfered "
    )
    public Attribute.Double[] Para_trans;

    /*
     *  Component run stages
     */
    @Override
    public void init() {

    }

    @Override
    public void run() {
        double relpos;
        int i = Para.length;

        if (i == range.length && i == Para_trans.length && i == M_S.length) {

            double start = independet_pos.getValue() - trans.getValue() / 2;
            double end = independet_pos.getValue() + trans.getValue() / 2;

            if (independet_var.getValue() <= end) {
                if (independet_var.getValue() >= start) {
                    
                    relpos =  (independet_var.getValue() - start)/ trans.getValue();
                    
                } else {
                    relpos = 0;
                }
                
            } else {
                 relpos = 1;
            }
            i = i -1;
            while (i >= 0) {
                double result = 0.0;

//                double trans_half = range[i].getValue() / 2;
                
                if (Add_or_mult.getValue() == 1){
                  result = Para[i].getValue() * (M_S[i].getValue() +  (range[i].getValue() * relpos));
                }else if (Add_or_mult.getValue() == 0){
                  result = Para[i].getValue() + (M_S[i].getValue() +  (range[i].getValue() * relpos));  
                }else{
                   getModel().getRuntime().println("Variable Add_or_mult has not a defined value (must be 0 or 1 ) - HALT", JAMS.STANDARD);
                   getModel().getRuntime().sendHalt();
                }

                Para_trans[i].setValue(result);
                i--;
            }

        } else {
             getModel().getRuntime().println("Variable numbers are not equal for the different input variables - HALT", JAMS.STANDARD);
             getModel().getRuntime().sendHalt();
        };
        ;
    }
}
