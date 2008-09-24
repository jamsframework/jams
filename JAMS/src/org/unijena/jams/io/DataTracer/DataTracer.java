/*
 * DataTracer.java
 * Created on 24. September 2008, 16:09
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package org.unijena.jams.io.DataTracer;

import java.util.HashMap;
import org.unijena.jams.dataaccess.DataAccessor;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public interface DataTracer {

    public DataAccessor[] getAccessorObjects();

    public void registerAttribute(String attributeName);

    public String[] init(HashMap<String, DataAccessor> dataObjectHash);

    public void trace();

    public void setStartMark();

    public void setEndMark();

    public void close();
}
