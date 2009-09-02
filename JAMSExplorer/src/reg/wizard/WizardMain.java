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

        Wizard explorerWizard = new ExplorerWizard().createWizard();
        WizardDisplayer.showWizard (explorerWizard,
                new Rectangle (20, 20, 800, 400));
        System.exit(0);
    }

}
