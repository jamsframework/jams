/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.wizard;

import java.io.Serializable;

/**
 *
 * @author chris .. TODO AttributeWrapper is used in modelAnalyer/modifier, but there it can be propably replaced by attribute
 */
public class AttributeWrapper implements Comparable, Serializable {
    private String attributeName;
    private String variableName;
    private String componentName;
    private String contextName;
    private boolean isSetByValue;

    public AttributeWrapper() {
    }

    public AttributeWrapper(String variableName, String attributeName, String componentName, String contextName) {
        this.variableName = variableName;
        this.attributeName = attributeName;
        this.componentName = componentName;
        this.contextName = contextName;
    }
    
    @Override
    public boolean equals(Object a) {
        if (!(a instanceof AttributeWrapper)) {
            return false;
        }
        return ((AttributeWrapper) a).getName().equals(getName());
    }

    @Override
    public int hashCode() {
        int hash = 4;
        hash = 97 * hash + (this.getAttributeName() != null ? this.getAttributeName().hashCode() : 0);
        hash = 97 * hash + (this.getVariableName() != null ? this.getVariableName().hashCode() : 0);
        hash = 97 * hash + (this.getComponentName() != null ? this.getComponentName().hashCode() : 0);
        hash = 97 * hash + (this.getContextName() != null ? this.getContextName().hashCode() : 0);
        return hash;
    }

    public String extendedtoString() {
        return getComponentName() + "." + getVariableName() + "=" + getContextName() + "." + getAttributeName();
    }

    public String getChildName(){
        if (this.attributeName!=null)
            return this.attributeName;
        else
            return this.variableName;
    }
    public String getParentName(){
        if (this.contextName!=null)
            return this.contextName;
        else
            return this.componentName;
    }

    public String getName() {
        if (getContextName() == null && getComponentName() == null) {
            if (getAttributeName() != null) {
                return getAttributeName();
            } else {
                return getVariableName();
            }
        }
        if (getContextName() == null) {
            if (getAttributeName() != null) {
                return getComponentName() + "." + getAttributeName();
            } else {
                return getComponentName() + "." + getVariableName();
            }
        } else {
            if (getAttributeName() != null) {
                return getContextName() + "." + getAttributeName();
            } else {
                return getContextName() + "." + getVariableName();
            }
        }
    }

    @Override
    public String toString() {
        return getName();
        /*if (variableName!=null)
        return variableName;
        return attributeName;*/
    }

    public int compareTo(Object a) {
        return this.getName().compareTo(((AttributeWrapper) a).getName());
    }

    /**
     * @return the attributeName
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * @param attributeName the attributeName to set
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * @return the variableName
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * @param variableName the variableName to set
     */
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    /**
     * @return the componentName
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * @param componentName the componentName to set
     */
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    /**
     * @return the contextName
     */
    public String getContextName() {
        return contextName;
    }

    /**
     * @param contextName the contextName to set
     */
    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    /**
     * @return the isSetByValue
     */
    public boolean isIsSetByValue() {
        return isSetByValue;
    }

    /**
     * @param isSetByValue the isSetByValue to set
     */
    public void setIsSetByValue(boolean isSetByValue) {
        this.isSetByValue = isSetByValue;
    }
}
