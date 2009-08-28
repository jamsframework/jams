/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.wizard;

import java.awt.Rectangle;
import javax.swing.UIManager;
import org.netbeans.api.wizard.WizardDisplayer;
import reg.wizard.tlug.ExplorerWizard;

/**
 *
 * @author hbusch
 */
public class WizardMain {

    public static void main (String[] ignored) throws Exception {
        //Use native L&F
        UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());

        WizardDisplayer.showWizard (new ExplorerWizard().createWizard(),
                new Rectangle (20, 20, 500, 400));
        System.exit(0);
    }

}
