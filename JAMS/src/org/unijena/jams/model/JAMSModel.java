/*
 * JAMSModel.java
 * Created on 31. Mai 2006, 17:03
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

package org.unijena.jams.model;

import org.unijena.jams.runtime.JAMSRuntime;

/**
 *
 * @author S. Kralisch
 */

@JAMSComponentDescription(
        title="JAMS model",
        author="Sven Kralisch",
        date="26. September 2005",
        description="This component represents a JAMS model which is special type of context component")
        public class JAMSModel extends JAMSContext {
    
    private JAMSRuntime runtime;
    private String name, author, date;
    
    
    /** Creates a new instance of JAMSModel */
    public JAMSModel(JAMSRuntime runtime) {
        this.runtime = runtime;
    }
    
    public JAMSRuntime getRuntime() {
        return runtime;
    }
    
    public void setRuntime(JAMSRuntime runtime) {
        this.runtime = runtime;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
}
