/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.wizard;

import java.awt.Rectangle;
import javax.swing.UIManager;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import reg.wizard.tlug.ExplorerWizard;

/**
 *
 * @author hbusch
 */
public class WizardMain {

    public static void main (String[] ignored) throws Exception {
        //Use native L&F
        UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());

        ExplorerWizard explorerWizard = new ExplorerWizard();
        Wizard wizard = explorerWizard.createWizard();
        Object result = WizardDisplayer.showWizard (wizard,
                new Rectangle (20, 20, 850, 530));
        System.out.println("Result of wizard:"+ result);
        System.exit(0);
    }

}
