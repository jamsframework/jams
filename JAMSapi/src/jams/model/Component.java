/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.model;

import java.io.Serializable;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public interface Component extends Serializable {

    /**
     * Method to be executed at model's cleanup stage
     * @throws java.lang.Exception
     */
    void cleanup() throws Exception;

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
     * Gets the JAMS model that this component belongs to
     * @return The model
     */
    Model getModel();

    /**
     * Method to be executed at model's init stage
     * @throws java.lang.Exception
     */
    void init() throws Exception;

    /**
     * Method to be executed at model's run stage
     * @throws java.lang.Exception
     */
    void run() throws Exception;

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

    /**
     * Sets the JAMS model that this component belongs to
     * @param model The model
     */
    void setModel(Model model);

}
