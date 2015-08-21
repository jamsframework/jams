/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.wizard;

import jams.meta.ComponentField;
import jams.meta.ContextAttribute;

/**
 *
 * @author chris
 */
public class Objective extends Attribute{
            
    public Objective(String name){
        super(name);
    }
    public Objective(Attribute a) {
        super(a);
    }
    
    public Objective(ContextAttribute ca) {
       super(ca);
    }

    public Objective(ComponentField cf) {
       super(cf);
    }
    
    @Override
    public boolean equals(Object o){
        if (o instanceof Objective){
            Objective other = (Objective)o;
            if (other.getAttributeName().equals(this.getAttributeName()))
                if (other.getParentName().equals(this.getParentName()))
                    return true;
        }
        return false;

    }
}
