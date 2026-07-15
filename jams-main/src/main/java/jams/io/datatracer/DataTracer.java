/*
 * DataTracer.java
 * Created on 24. September 2008, 16:09
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
package jams.io.datatracer;

import jams.dataaccess.DataAccessor;
import java.io.Serializable;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public interface DataTracer extends Serializable {

    public DataAccessor[] getAccessorObjects();

    public void updateDataAccessors();

    public void trace();

    public void startMark();

    public void endMark();

    public void close();

    public boolean hasOutput();

    public void setOutput(boolean value);
}
