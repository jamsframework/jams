/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.wizard;

/**
 *
 * @author chris
 */
public class ComponentWrapper {

    public String componentName;
    public String componentContext;
    public boolean contextComponent;

    public ComponentWrapper(String componentName, String componentContext, boolean contextComponent) {
        this.componentContext = componentContext;
        this.componentName = componentName;
        this.contextComponent = contextComponent;
    }

    @Override
    public String toString() {
        if (contextComponent) {
            return componentName;
        }
        return /*componentContext + "." + */ componentName;
    }
}
