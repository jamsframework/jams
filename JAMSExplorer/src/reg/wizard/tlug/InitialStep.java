/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.wizard.tlug;

import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardPanelProvider;
import reg.wizard.tlug.panels.DataDecisionPanel;

/**
 *
 * @author hbusch
 */
 class InitialStep extends WizardPanelProvider {
    private static final String WHICH_DATA = "whichData";

    InitialStep () {
        super( "TLUG Regionalizer Wizard", new String[] { WHICH_DATA },
            new String[] { "Art der Daten" } );
    }

    protected JComponent createPanel (final WizardController controller,
        final String id, final Map data) {

        switch ( indexOfStep( id ) ) {

            case 0 :

                return new DataDecisionPanel( controller, data );

            default :
                throw new IllegalArgumentException ( id );
        }
    }
}
