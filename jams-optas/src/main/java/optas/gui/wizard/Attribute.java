/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.wizard;

import jams.meta.ComponentField;
import jams.meta.ContextAttribute;
import java.io.Serializable;

/**
 *
 * @author chris
 */
public class Attribute implements Serializable, Comparable {
    
    Object field = null;    
           
    public Attribute(String name){
        field = name;
    }
    public Attribute(Attribute a){        
        this.field = a.field;        
    }
    public Attribute(ContextAttribute ca) {
       field = ca;
    }

    public Attribute(ComponentField cf) {
       field = cf;
    }
        
    @Override
    public String toString(){
        if (this.field instanceof ComponentField){
            ComponentField cf = (ComponentField)field;
            return cf.getParent().getInstanceName() + "." + cf.getName();
        }else if (this.field instanceof ContextAttribute){
            ContextAttribute ca = (ContextAttribute)this.field;
            return ca.getContext().getInstanceName() + "." + ca.getName();
        }else if (this.field instanceof String){
            return field.toString();
        }
        return null;
    }
    
    public String getParentName(){
        if (this.field instanceof ComponentField){
            ComponentField cf = (ComponentField)field;
            return cf.getParent().getInstanceName();
        }else if (this.field instanceof ContextAttribute){
            ContextAttribute ca = (ContextAttribute)this.field;
            return ca.getContext().getInstanceName();
        }
        return null;
    }
    
    public String getAttributeName(){
        if (this.field instanceof ComponentField){
            ComponentField cf = (ComponentField)field;
            return cf.getName();
        }else if (this.field instanceof ContextAttribute){
            ContextAttribute ca = (ContextAttribute)this.field;
            return ca.getName();
        }
        return null;
    }
    
    @Override
    public int compareTo(Object o){
        return this.toString().compareToIgnoreCase(o.toString());
    }
    
    @Override
    public boolean equals(Object o){
        if (o == null || !Attribute.class.isAssignableFrom(o.getClass()))
            return false;
        return this.toString().compareToIgnoreCase(o.toString()) == 0;
    }
}
