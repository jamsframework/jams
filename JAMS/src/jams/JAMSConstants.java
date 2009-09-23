/*
 * JAMSConstants.java
 * Created on 2. Oktober 2005, 16:05
 *
 * This file is part of JAMSConstants
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
 * GNU General Publiccc License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package jams;

import java.awt.Font;
import java.util.ResourceBundle;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSConstants {

    /**
     * Verbosity level 0 of 3
     */
    public static final int SILENT = 0;

    /**
     * Verbosity level 1 of 3
     */
    public static final int STANDARD = 1;

    /**
     * Verbosity level 2 of 3
     */
    public static final int VERBOSE = 2;

    /**
     * Verbosity level 3 of 3
     */
    public static final int VVERBOSE = 3;

    /**
     * Resource bundle containing all string literals for some localization
     */
    public static ResourceBundle resources = java.util.ResourceBundle.getBundle("resources/JAMSBundle");

    /**
     * The standard font
     */
    public static final Font STANDARD_FONT = new java.awt.Font("Courier", 0, 11);

    /**
     * Default name of model output file
     */
    public static final String DEFAULT_MODEL_FILENAME = "model.jmp";

    /**
     * Default name of parameter output file
     */
    public static final String DEFAULT_PARAMETER_FILENAME = "default.jap";

}
