/*
 * InputComponent.java
 * Created on 29. August 2006, 14:59
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

package org.unijena.jams.gui.input;

import javax.swing.JComponent;
import org.unijena.jams.gui.*;

/**
 *
 * @author S. Kralisch
 */
public interface InputComponent {
    
    public static final int INPUT_OK = 0, INPUT_WRONG_FORMAT = -1, INPUT_OUT_OF_RANGE  = -2;
    
    public String getValue();
    public void setValue(String value);
    public JComponent getComponent();
    public void setRange(double lower, double upper);
    public boolean verify();
    public int getErrorCode();
    
}
