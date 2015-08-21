/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data.time.api;

import jams.data.Attribute;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public interface TimeFilter extends Serializable{

    public String getName();

    public void setAdditive(boolean isAdditive);
    public boolean isAdditive();

    public void setEnabled(boolean isEnabled);
    public boolean isEnabled();

    public void setInverted(boolean isInverted);
    public boolean isInverted();   
    
    public boolean isFiltered(Date date);
    
    public String toString(Attribute.TimeInterval domain);
    public String toFile(File f, Attribute.TimeInterval domain) throws IOException;
}
