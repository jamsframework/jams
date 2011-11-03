/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.hydro.data;

import java.util.Date;

/**
 *
 * @author chris
 */
public abstract class TimeFilter {
    String name;

    public String getName(){
        return name;
    }

    abstract public boolean isFiltered(Date date);
}
