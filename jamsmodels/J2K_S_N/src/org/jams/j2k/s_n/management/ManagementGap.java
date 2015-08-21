/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.s_n.management;

/**
 *
 * @author c8fima
 */
public class ManagementGap implements ManagementOption {
    
    private int days;
    
    public ManagementGap(int days) {
        this.days = days;
    }

    /**
     * @return the gap
     */
    public int getDays() {
        return days;
    }
    
    
}
