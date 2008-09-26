/*
 * JAMSComponent.java
 *
 * Created on 27. Juni 2005, 09:53
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

import java.io.Serializable;

/**
 *
 * @author S. Kralisch
 */

@JAMSComponentDescription(
        title="JAMS Component",
        author="Sven Kralisch",
        date="27. Juni 2005",
        description="This component represents a JAMS component which are the model building blocks in JAMS")
        public class JAMSComponent implements Serializable {
    
    protected String instanceName = getClass().getName();
    private JAMSContext context = null;
    private JAMSModel model = null;
    
    public void init() throws Exception {}
    public void run() throws Exception {}
    public void cleanup() throws Exception {}
        
    /**
     * Get the name of this component instance
     * @return The component's instance name
     */
    public String getInstanceName() {
        return instanceName;
    }
    
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
        
    /**
     * Gets the parent context of this component
     * @return The parent context of this component, null if this is a model
     * context
     */
    public JAMSContext getContext() {
        return context;
    }
    
    public void setContext(JAMSContext context) {
        this.context = context;
    }
    
    public JAMSModel getModel() {
        return model;
    }
    
    public void setModel(JAMSModel model) {
        this.model = model;
    }
}
