/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.metamodel;

/**
 *
 * @author chris
 */
public class AttributeDescription {

    String name;
    String value;
    String context;
    boolean isAttribute;

    AttributeDescription(String name, String context, String value, boolean isAttribute) {
        this.name = name;
        this.value = value;
        this.isAttribute = isAttribute;
        this.context = context;
    }
}
