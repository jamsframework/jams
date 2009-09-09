/*
 * this class defines the steps for station data
 */

package reg.wizard.tlug;

import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.spi.wizard.WizardController;
import reg.wizard.tlug.panels.AddCompsPanel;
import reg.wizard.tlug.panels.StationParamsPanel;

/**
 *
 * @author hbusch
 */
public class StationDataSteps extends DataStepPanelProvider {

    public StationDataSteps() {
        super (
            new String[] { "stationParams", "finishIt" },
            new String[] { "Parameter festlegen", "Konfiguration speichern"});
    }

    protected JComponent createPanel(WizardController controller, String id, Map settings) {
        switch (indexOfStep(id)) {
            case 0 :
                return new StationParamsPanel (controller, settings);
            case 1 :
                return new AddCompsPanel (controller, settings);
            default :
                throw new IllegalArgumentException (id);
        }
    }

}
