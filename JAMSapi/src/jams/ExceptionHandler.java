/*
 * ExceptionHandler.java
 * Created on 22.02.2011, 11:48:39
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jams;

import java.util.ArrayList;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public interface ExceptionHandler {

    public void handle(JAMSException ex);
    public void handle(ArrayList<JAMSException> exList);

}