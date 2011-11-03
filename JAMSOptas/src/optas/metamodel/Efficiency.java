/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.metamodel;

/**
 *
 * @author chris
 */
public class Efficiency extends AttributeWrapper {

    public int mode;

    public Efficiency(AttributeWrapper attr) {
        setAttributeName(attr.getAttributeName());
        setComponentName(attr.getComponentName());
        setContextName(attr.getContextName());
        setVariableName(attr.getVariableName());
        setIsSetByValue(attr.isIsSetByValue());
    }
}
