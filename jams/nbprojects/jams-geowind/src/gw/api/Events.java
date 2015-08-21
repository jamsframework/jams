/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gw.api;

/**
 *
 * @author od
 */
public class Events {
    
    public static final String GF_UPDATE = "gf_update";
    public static final String GF_SELECTION = "gf_selection";

    // Can be sent by the panel to control other panels
    public static final String WIZARD_ENABLE_NEXT = "gf_selection";
    public static final String WIZARD_ENABLE_ALLNEXT = "gf_selection";
    public static final String WIZARD_DISABLE_NEXT = "gf_selection";
    public static final String WIZARD_DISABLE_ALLNEXT = "gf_selection";

    // sent by the panel manager
    public static final String PANEL_SHOW = "show";
    public static final String PANEL_HIDE = "hide";

//  public static final String WIZARD_ENABLE_NEXT = "gf_selection";
    
}
