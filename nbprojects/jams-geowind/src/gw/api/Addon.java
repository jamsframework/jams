/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gw.api;

import javax.swing.JPanel;

/** GeoWind Addon Interface.
 *
 * @author od
 */
public interface Addon {

    /**
     * Get the name of the Addon.
     *
     * @return the name
     */
    String getName();

    /**
     * Indicates if this Addon implements a stepwise wizard.
     * 
     * @return true is this is a wizard, false otherwise
     */
    boolean isWizard();

    /**
     * Get the panels that belong to the Addon
     * 
     * @param the view, provided by Geowind
     * @param activate indicator if the addon is being activated or deactivated
     * @return the panels that belong to the addon.
     */
    JPanel[] getPanels(GeoWindView view, boolean activate);
}
