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

package jams.model;

import java.io.Serializable;

/**
 *
 * @author S. Kralisch
 */

@JAMSComponentDescription(
        title="JAMS Component",
        author="Sven Kralisch",
        date="27. Juni 2005",
        description="This component represents a JAMS component which is the " +
        "main model building block in JAMS")
        public class JAMSComponent implements Serializable {
    
    private String instanceName = getClass().getName();
    private JAMSContext context = null;
    private JAMSModel model = null;
    
    /**
     * Method to be executed at model's init stage
     * @throws java.lang.Exception
     */
    public void init() throws Exception {}

    /**
     * Method to be executed at model's run stage
     * @throws java.lang.Exception
     */
    public void run() throws Exception {}

    /**
     * Method to be executed at model's cleanup stage
     * @throws java.lang.Exception
     */
    public void cleanup() throws Exception {}
        
    /**
     * Gets the name of this component
     * @return The component's instance name
     */
    public String getInstanceName() {
        return instanceName;
    }
    
    /**
     * Sets the name of this component
     * @param instanceName The component's instance name
     */
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
    
    /**
     * Sets the context that this component is child of
     * @param context The parent context
     */
    public void setContext(JAMSContext context) {
        this.context = context;
    }
    
    /**
     * Gets the JAMS model that this component belongs to
     * @return The model
     */
    public JAMSModel getModel() {
        return model;
    }
    
    /**
     * Sets the JAMS model that this component belongs to
     * @param model The model
     */
    public void setModel(JAMSModel model) {
        this.model = model;
    }
}
