/*
 * InputComponentFactory.java
 * Created on 23. September 2009, 16:12
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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

package jams.gui.input;

import jams.data.Attribute;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import jams.data.DefaultDataFactory;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class InputComponentFactory {

    private static final int JCOMP_HEIGHT = 20;

    private static final int NUMBERINPUT_WIDTH = 100;

    private static final int TEXTINPUT_WIDTH = 250;

    private static final int FILEINPUT_WIDTH = 250;

    /**
     * Create swing input component based on data type
     * @param type Data type
     * @return InputComponent object that provides an editor for the type
     */
    public static InputComponent createInputComponent(Class type) {
        return createInputComponent(type, false);
    }

    /**
     * Create swing input component based on data type
     * @param type Data type
     * @param extEdit A flag defining if the editors will provide extended or
     * limited access to data (e.g. step size for timeintervals)
     * @return InputComponent object that provides an editor for the type
     */
    public static InputComponent createInputComponent(Class type, boolean extEdit) {
        InputComponent ic;

        if (type.isInterface()) {
            type = DefaultDataFactory.getDataFactory().getImplementingClass(type);
        }

        if (Attribute.FileName.class.isAssignableFrom(type)) {
            ic = new FileInput(false);
        } else if (Attribute.DirName.class.isAssignableFrom(type)) {
            ic = new FileInput(true);
        } else if (Attribute.Calendar.class.isAssignableFrom(type)) {
            ic = new CalendarInput();
        } else if (Attribute.TimeInterval.class.isAssignableFrom(type)) {
            ic = new TimeintervalInput(extEdit);
        } else if (Attribute.Boolean.class.isAssignableFrom(type)) {
            ic = new BooleanInput();
        } else if ((Attribute.Integer.class.isAssignableFrom(type)) || (Attribute.Long.class.isAssignableFrom(type))) {
            ic = new IntegerInput();
            ic.getComponent().setPreferredSize(new Dimension(NUMBERINPUT_WIDTH, JCOMP_HEIGHT));
            ic.getComponent().setBorder(BorderFactory.createEtchedBorder());
        } else if ((Attribute.Float.class.isAssignableFrom(type)) || (Attribute.Double.class.isAssignableFrom(type))) {
            ic = new FloatInput();
            ic.getComponent().setPreferredSize(new Dimension(NUMBERINPUT_WIDTH, JCOMP_HEIGHT));
            ic.getComponent().setBorder(BorderFactory.createEtchedBorder());
        } else {
            ic = new TextInput();
            ic.getComponent().setPreferredSize(new Dimension(TEXTINPUT_WIDTH, JCOMP_HEIGHT));
            ic.getComponent().setBorder(BorderFactory.createEtchedBorder());
        }

        //ic.getComponent().setBorder(BorderFactory.createEtchedBorder());
        return ic;
    }

}
