/*
 * TemporalSumAggregator.java
 * Created on 19. Juli 2006, 11:57
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
package jams.components.aggregate;

import jams.data.*;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Ch. Fischer
 */
public class QuantileComputer extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the current modelling time step"
    )
    public Attribute.Calendar time;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "The efficiency time interval, a subset of modelTimeInterval"
            )
            public Attribute.TimeInterval effTimeInterval;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "value attribute")
    public Attribute.Double[] input;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "quantile to be computed",
            lowerBound = 0.0,
            upperBound = 1.0)
    public Attribute.Double quantile;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "quantile to be computed")
    public Attribute.Double[] output;

    ArrayList<Double>[] timeserie = null;

    @Override
    public void run() {
        if (effTimeInterval != null && time != null){
            if (time.before(effTimeInterval.getStart()) || 
                time.after(effTimeInterval.getEnd()))
                return;
        }
        if (timeserie == null) {
            timeserie = new ArrayList[input.length];
            for (int i = 0; i < input.length; i++) {
                timeserie[i] = new ArrayList<Double>();
            }
        }
        for (int i = 0; i < input.length; i++) {
            timeserie[i].add(input[i].getValue());
        }
    }

    @Override
    public void cleanup() {
        if (timeserie.length == 0) {
            return;
        }        
        int position = Math.round((float) timeserie[0].size() * (float) quantile.getValue());
        for (int i = 0; i < timeserie.length; i++) {
            Collections.sort(timeserie[i]);
            output[i].setValue(timeserie[i].get(position));
            System.out.println("Quantile " + output[i]);
        }
    }
}
