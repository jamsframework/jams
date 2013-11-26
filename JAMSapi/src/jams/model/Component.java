/*
 * Component.java
 * Created on 5. November 2009, 16:25
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

package jams.model;

import java.io.Serializable;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public interface Component extends Serializable {

    /**
     * Method to be executed at model's setup stage
     */
    void setup();

    /**
     * Method to be executed at model's init stage
     */
    void init();

    /**
     * Method to be executed at model's run stage
     */
    void run();

    /**
     * Method to be executed at model's cleanup stage
     */
    void cleanup();

    /**
     * Gets the JAMS model that this component belongs to
     * @return The model
     */
    Model getModel();

    /**
     * Gets the parent context of this component
     * @return The parent context of this component, null if this is a model
     * context
     */
    Context getContext();

    /**
     * Gets the name of this component
     * @return The component's instance name
     */
    String getInstanceName();

    /**
     * Sets the JAMS model that this component belongs to
     * @param model The model
     */
    void setModel(Model model);

    /**
     * in the first run after restoring the model from a saved state this method
     * excecuted
     */
    void restore();
    /**
     * Sets the context that this component is child of
     * @param context The parent context
     */
    void setContext(Context context);

    /**
     * Sets the name of this component
     * @param instanceName The component's instance name
     */
    void setInstanceName(String instanceName);

}
