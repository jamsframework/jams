/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author chris
 */
public abstract class TimeFilter implements Serializable{
    String name;
    boolean isAdditive=false;
    boolean isEnabled = true;
    boolean isInverted = false;

    public String getName(){
        return name;
    }

    public void setAdditive(boolean isAdditive){
        this.isAdditive = isAdditive;
    }
    public boolean isAdditive(){
        return isAdditive;
    }

    public void setEnabled(boolean isEnabled){
        this.isEnabled = isEnabled;
    }
    public boolean isEnabled(){
        return isEnabled;
    }

    public void setInverted(boolean isInverted){
        this.isInverted = isInverted;
    }
    public boolean isInverted(){
        return isInverted;
    }
   
    abstract public boolean isFiltered(Date date);
}
