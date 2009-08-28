/*
 * this class defines the steps for station data
 */

package reg.wizard.tlug;

import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardPanelProvider;
import reg.wizard.tlug.panels.StationParamsPanel;

/**
 *
 * @author hbusch
 */
public class StationDataSteps  extends WizardPanelProvider {

    public StationDataSteps() {
        super (
            new String[] { "stationParams" },
            new String[] { "Parameter festlegen"});
    }

    protected JComponent createPanel(WizardController controller, String id, Map settings) {
        switch (indexOfStep(id)) {
            case 0 :
                return new StationParamsPanel (controller, settings);
            default :
                throw new IllegalArgumentException (id);
        }
    }

}
