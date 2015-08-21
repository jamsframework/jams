/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.wizard;

/**
 *
 * @author christian
 */
public class OPTASWizardException extends Exception {

    String e;

    public OPTASWizardException(String desc) {
        e = desc;
    }

    @Override
    public String toString() {
        return e;
    }
}
